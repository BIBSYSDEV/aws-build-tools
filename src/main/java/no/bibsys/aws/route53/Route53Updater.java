package no.bibsys.aws.route53;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.kms.model.NotFoundException;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.bibsys.aws.apigateway.ApiGatewayBasePathMapping;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.BranchInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Route53Updater {


    private static final Logger log = LoggerFactory.getLogger(Route53Updater.class);
    private final transient StaticUrlInfo staticUrlINfo;


    private final transient String apiGatewayRestApiId;
    private final transient AmazonApiGateway apiGatewayClient;


    private final transient ApiGatewayBasePathMapping apiGatewayBasePathMapping;


    private transient AmazonRoute53 route53Client;


    public Route53Updater(StaticUrlInfo staticUrlINfo, Stage stage,
        String apiGatewayRestApiId,
        AmazonApiGateway apiGatewayClient) {

        this.staticUrlINfo = staticUrlINfo;

        this.apiGatewayClient = apiGatewayClient;

        this.route53Client = AmazonRoute53ClientBuilder.defaultClient();
        this.apiGatewayRestApiId = apiGatewayRestApiId;

        this.apiGatewayBasePathMapping =
            new ApiGatewayBasePathMapping(apiGatewayClient, staticUrlINfo.getDomainName(), stage);

    }


    public Route53Updater copy(Stage stage) {
        return new Route53Updater(staticUrlINfo,  stage, apiGatewayRestApiId,
            apiGatewayClient);
    }


    public Optional<ChangeResourceRecordSetsRequest> createUpdateRequest() {

        Optional<String> targetDomainName = apiGatewayBasePathMapping.awsGetTargetDomainName();
        return targetDomainName
            .map(domainName -> updateRecordSetsRequest(domainName));

    }


    public Optional<ChangeResourceRecordSetsRequest> createDeleteRequest() {
        try {
            Optional<String> targetDomainName = apiGatewayBasePathMapping.awsGetTargetDomainName();
            return targetDomainName.map(domainName -> deleteRecordSetsRequest(domainName));
        } catch (NotFoundException e) {
            if (log.isWarnEnabled()) {
                log.warn(
                    "Domain Name not found:" + apiGatewayBasePathMapping.awsGetTargetDomainName());
            }

        }
        return Optional.empty();
    }


    public ChangeResourceRecordSetsResult executeUpdateRequest(
        ChangeResourceRecordSetsRequest request,
        String certificateArn) {
        apiGatewayBasePathMapping.awsCreateBasePath(apiGatewayRestApiId, certificateArn);
        return route53Client.changeResourceRecordSets(request);
    }


    public ChangeResourceRecordSetsResult executeDeleteRequest(
        ChangeResourceRecordSetsRequest request) {
        apiGatewayBasePathMapping.awsDeleteBasePathMappings();
        return route53Client.changeResourceRecordSets(request);
    }


    private HostedZone getHostedZone() {
        List<HostedZone> hostedZones = route53Client.listHostedZones().getHostedZones().stream()
            .filter(zone -> zone.getName().equals(staticUrlINfo.getZoneName()))
            .collect(Collectors.toList());
        Preconditions.checkArgument(hostedZones.size() == 1,
            "There should exist exactly one hosted zone with the name " + staticUrlINfo
                .getZoneName());
        return hostedZones.get(0);

    }


    private ChangeResourceRecordSetsRequest deleteRecordSetsRequest(String serverUrl) {
        String hostZoneId = getHostedZone().getId();
        ResourceRecordSet recordSet = createRecordSet(serverUrl);
        Change change = createChange(recordSet, ChangeAction.DELETE);
        ChangeResourceRecordSetsRequest request = new ChangeResourceRecordSetsRequest()
            .withChangeBatch(new ChangeBatch().withChanges(change)).withHostedZoneId(hostZoneId);
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
        ResourceRecordSet recordSet = new ResourceRecordSet()
            .withName(staticUrlINfo.getRecordSetName())
            .withType(RRType.CNAME).withTTL(300L)
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
