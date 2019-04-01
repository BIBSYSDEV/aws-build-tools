package no.bibsys.aws.route53;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.BadRequestException;
import com.amazonaws.services.apigateway.model.NotFoundException;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import no.bibsys.aws.apigateway.ApiGatewayBasePathMapping;
import no.bibsys.aws.cloudformation.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Route53Updater {
    
    public static final String HOSTED_ZONE_NOT_FOUND_MESSAGE =
        "Either none or too many Route 53 hosted zones found for the url {}";
    private static final String PREPARED_CUSTOM_DOMAIN_NAME_DEBUG_MESSAGE = "prepared custom domain name";
    private static final String PREPARING_CUSTOM_DOMAIN_NAME_DEBUG_MESSAGE = "Preparing Custom Domain Name";
    private static final Logger log = LoggerFactory.getLogger(Route53Updater.class);
    private static final int EXACTLY_ONE_ZONE = 1;
    private static final long STANDARD_TTL = 300L;
    private static final int SINGLE_ZONE_IN_ZONE_LIST = 0;
    private static final String NOT_EXISTING_BASEPATH_MAPPING_WARNING = "Not existing Basepath Mapping";
    private final transient StaticUrlInfo staticUrlInfo;
    
    private final transient String apiGatewayRestApiId;
    private final transient AmazonApiGateway apiGatewayClient;
    
    private final transient ApiGatewayBasePathMapping apiGatewayBasePathMapping;
    private transient AmazonRoute53 route53Client;
    
    public Route53Updater(StaticUrlInfo staticUrlInfo, String apiGatewayRestApiId, AmazonApiGateway apiGatewayClient,
        AmazonRoute53 route53Client) {
        
        this.staticUrlInfo = staticUrlInfo;
        this.apiGatewayClient = apiGatewayClient;
        this.apiGatewayRestApiId = apiGatewayRestApiId;
        this.route53Client = route53Client;
        this.apiGatewayBasePathMapping =
            new ApiGatewayBasePathMapping(apiGatewayClient, staticUrlInfo.getDomainName(), staticUrlInfo.getStage());
    }
    
    public Route53Updater copy(Stage stage) {
        StaticUrlInfo info = this.staticUrlInfo.copy(stage);
        return new Route53Updater(info, apiGatewayRestApiId, apiGatewayClient, route53Client);
    }
    
    /**
     * Create a request for creating a CNAME entry in the hosted zone and link it to the URL of the service as this is
     * specified in the {@link StaticUrlInfo} field of the class. The entry will have the name given by {@link
     * StaticUrlInfo#getRecordSetName()} and will link to {@link StaticUrlInfo#getDomainName()}.
     *
     * @param certificateArn
     * @return An {@link Optional} with a {@link ChangeResourceRecordSetsRequest} if the domain name exists and the
     *     certificate is valid.
     *     <p>
     *     An empty {@link Optional} if the domain name does not exist.
     *     </p>
     *     <p>
     *     An empty {@link Optional} if the certificate ARN is not valid
     *     </p>
     *     <p>
     *     An empty {@link Optional} for a BadRequestException
     *     </p>
     */
    public Optional<ChangeResourceRecordSetsRequest> createUpdateRequestForRecordSets(String certificateArn) {
        //necessary step before creating  the update request
        try {
            prepareCustomDomainName(certificateArn);
        } catch (BadRequestException e) {
            log.error("Failed to prepare a custom domain name", e);
            return Optional.empty();
        }
        
        Optional<String> targetDomainName = apiGatewayBasePathMapping.awsGetTargetDomainName();
        return targetDomainName.flatMap(this::updateRecordSetsRequest);
    }
    
    public Optional<ChangeResourceRecordSetsRequest> createDeleteRequest() {
        
        Optional<String> targetDomainName = apiGatewayBasePathMapping.awsGetTargetDomainName();
        return targetDomainName.flatMap(this::deleteRecordSetsRequest);
    }
    
    public ChangeResourceRecordSetsResult executeUpdateRequest(ChangeResourceRecordSetsRequest request) {
        log.info("Executing request:{}", request);
        
        return route53Client.changeResourceRecordSets(request);
    }
    
    public ChangeResourceRecordSetsResult executeDeleteRequest(ChangeResourceRecordSetsRequest request) {
        apiGatewayBasePathMapping.awsDeleteBasePathMappings();
        return route53Client.changeResourceRecordSets(request);
    }
    
    
    private void prepareCustomDomainName(String certificateArn) {
        log.info(PREPARING_CUSTOM_DOMAIN_NAME_DEBUG_MESSAGE);
        deletePossiblyExistingMappings();
        apiGatewayBasePathMapping.awsCreateCustomDomainName(certificateArn);
        apiGatewayBasePathMapping.awsCreateBasePath(apiGatewayRestApiId);
        log.info(PREPARED_CUSTOM_DOMAIN_NAME_DEBUG_MESSAGE);
    }
    
    private void deletePossiblyExistingMappings() {
        try {
            apiGatewayBasePathMapping.awsDeleteBasePathMappings();
        } catch (NotFoundException e) {
            log.warn(NOT_EXISTING_BASEPATH_MAPPING_WARNING);
        }
    }
    
    private Optional<HostedZone> getHostedZone() {
        List<HostedZone> hostedZones = zonesMatchingStaticUrlInfoZoneName();
        if (hostedZones.size() == EXACTLY_ONE_ZONE) {
            return Optional.ofNullable(hostedZones.get(SINGLE_ZONE_IN_ZONE_LIST));
        } else {
            log.warn(HOSTED_ZONE_NOT_FOUND_MESSAGE, staticUrlInfo.getZoneName());
            return Optional.empty();
        }
    }
    
    private List<HostedZone> zonesMatchingStaticUrlInfoZoneName() {
        return route53Client.listHostedZones().getHostedZones().stream().filter(this::zoneMatchesStaticUrlInfoZoneName)
                            .collect(Collectors.toList());
    }
    
    private boolean zoneMatchesStaticUrlInfoZoneName(HostedZone zone) {
        return zone.getName().equals(staticUrlInfo.getZoneName());
    }
    
    private Optional<ChangeResourceRecordSetsRequest> deleteRecordSetsRequest(String serverUrl) {
        final Optional<String> hostZoneId = getHostedZone().map(HostedZone::getId);
        
        ResourceRecordSet recordSet = createRecordSet(serverUrl);
        Change change = createChange(recordSet, ChangeAction.DELETE);
        
        Optional<ChangeResourceRecordSetsRequest> request = hostZoneId.map(
            zoneId -> new ChangeResourceRecordSetsRequest().withChangeBatch(new ChangeBatch().withChanges(change))
                                                           .withHostedZoneId(zoneId));
        
        return request;
    }
    
    private Optional<ChangeResourceRecordSetsRequest> updateRecordSetsRequest(String serverUrl) {
        Optional<String> hostedZoneId = getHostedZone().map(HostedZone::getId);
        
        ResourceRecordSet recordSet = createRecordSet(serverUrl);
        Change change = createChange(recordSet, ChangeAction.UPSERT);
        ChangeBatch changeBatch = new ChangeBatch().withChanges(change);
        
        return hostedZoneId
            .map(zoneId -> new ChangeResourceRecordSetsRequest().withChangeBatch(changeBatch).withHostedZoneId(zoneId));
    }
    
    private Change createChange(ResourceRecordSet recordSet, ChangeAction changeAction) {
        Change change = new Change();
        change.withAction(changeAction).withResourceRecordSet(recordSet);
        return change;
    }
    
    private ResourceRecordSet createRecordSet(String serverUrl) {
        ResourceRecordSet recordSet =
            new ResourceRecordSet().withName(staticUrlInfo.getRecordSetName()).withType(RRType.CNAME)
                                   .withTTL(STANDARD_TTL)
                                   .withResourceRecords(new ResourceRecord().withValue(serverUrl));
        return recordSet;
    }
    
    public void setRoute53Client(AmazonRoute53 route53Client) {
        this.route53Client = route53Client;
    }
    
    public ApiGatewayBasePathMapping getApiGatewayBasePathMapping() {
        return apiGatewayBasePathMapping;
    }
}
