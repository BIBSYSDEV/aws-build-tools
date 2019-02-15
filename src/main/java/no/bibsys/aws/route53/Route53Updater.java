package no.bibsys.aws.route53;

import com.amazonaws.services.apigateway.AmazonApiGateway;
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
import com.google.common.base.Preconditions;
import no.bibsys.aws.apigateway.ApiGatewayBasePathMapping;
import no.bibsys.aws.cloudformation.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Route53Updater {
    
    private static final Logger log = LoggerFactory.getLogger(Route53Updater.class);
    private static final String EXACTLY_ONE_ZONE_MESSAGE = "There should exist exactly one hosted zone with the name ";
    private static final int EXACTLY_ONE_ZONE = 1;
    private static final long STANDARD_TTL = 300L;
    private static final int SINGLE_ZONE_IN_ZONE_LIST = 0;
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
        this.apiGatewayBasePathMapping = new ApiGatewayBasePathMapping(apiGatewayClient, staticUrlInfo.getDomainName(),
                                                                       staticUrlInfo.getStage());
    }
    
    public Route53Updater copy(Stage stage) {
        StaticUrlInfo info = this.staticUrlInfo.copy(stage);
        return new Route53Updater(info, apiGatewayRestApiId, apiGatewayClient, route53Client);
    }
    
    public Optional<ChangeResourceRecordSetsRequest> createUpdateRequest(String certificateArn) {
        //necessary step before creating  the update request
        prepareCustomDomainName(certificateArn);
        
        Optional<String> targetDomainName = apiGatewayBasePathMapping.awsGetTargetDomainName();
        return targetDomainName.map(this::updateRecordSetsRequest);
    }
    
    private void prepareCustomDomainName(String certificateArn) {
        apiGatewayBasePathMapping.awsDeleteBasePathMappings();
        apiGatewayBasePathMapping.awsCreateCustomDomainName(certificateArn);
        apiGatewayBasePathMapping.awsCreateBasePath(apiGatewayRestApiId);
    }
    
    public Optional<ChangeResourceRecordSetsRequest> createDeleteRequest() {
        
        Optional<String> targetDomainName = apiGatewayBasePathMapping.awsGetTargetDomainName();
        return targetDomainName.map(this::deleteRecordSetsRequest);
    }
    
    public ChangeResourceRecordSetsResult executeUpdateRequest(ChangeResourceRecordSetsRequest request) {
        log.info("Executing request:{}", request);
        
        return route53Client.changeResourceRecordSets(request);
    }
    
    public ChangeResourceRecordSetsResult executeDeleteRequest(ChangeResourceRecordSetsRequest request) {
        apiGatewayBasePathMapping.awsDeleteBasePathMappings();
        return route53Client.changeResourceRecordSets(request);
    }
    
    private HostedZone getHostedZone() {
        List<HostedZone> hostedZones = zonesMatchingStaticUrlInfoZoneName();
        Preconditions.checkArgument(hostedZones.size() == EXACTLY_ONE_ZONE,
                                    EXACTLY_ONE_ZONE_MESSAGE + staticUrlInfo.getZoneName());
        return hostedZones.get(SINGLE_ZONE_IN_ZONE_LIST);
    }
    
    private List<HostedZone> zonesMatchingStaticUrlInfoZoneName() {
        return route53Client.listHostedZones().getHostedZones().stream().filter(this::zoneMatchesStaticUrlInfoZoneName)
                            .collect(Collectors.toList());
    }
    
    private boolean zoneMatchesStaticUrlInfoZoneName(HostedZone zone) {
        return zone.getName().equals(staticUrlInfo.getZoneName());
    }
    
    private ChangeResourceRecordSetsRequest deleteRecordSetsRequest(String serverUrl) {
        String hostZoneId = getHostedZone().getId();
        ResourceRecordSet recordSet = createRecordSet(serverUrl);
        Change change = createChange(recordSet, ChangeAction.DELETE);
        ChangeResourceRecordSetsRequest request =
                new ChangeResourceRecordSetsRequest().withChangeBatch(new ChangeBatch().withChanges(change))
                                                     .withHostedZoneId(hostZoneId);
        return request;
    }
    
    private ChangeResourceRecordSetsRequest updateRecordSetsRequest(String serverUrl) {
        String hostedZoneId = getHostedZone().getId();
        
        ResourceRecordSet recordSet = createRecordSet(serverUrl);
        Change change = createChange(recordSet, ChangeAction.UPSERT);
        ChangeBatch changeBatch = new ChangeBatch().withChanges(change);
        ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest();
        request.withChangeBatch(changeBatch).withHostedZoneId(hostedZoneId);
        return request;
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
