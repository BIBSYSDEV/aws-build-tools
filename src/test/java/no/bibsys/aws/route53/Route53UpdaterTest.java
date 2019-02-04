package no.bibsys.aws.route53;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.BasePathMapping;
import com.amazonaws.services.apigateway.model.GetBasePathMappingsResult;
import com.amazonaws.services.apigateway.model.GetDomainNameResult;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.RRType;
import java.util.Optional;
import no.bibsys.aws.cloudformation.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class Route53UpdaterTest {

    private static final String ZONE_NAME = "ZoneName";
    private static final String ZONE_ID = "ZoneId";
    private static final String SAMPLE_RECORD_SET_NAME = "some.url.goes.here.";
    private static final String SAMPLE_API_GATEWAY_REST_API_ID = "apiGatewarRestApiId";
    private static final String DOMAIN_NAME = "DomainName";
    private static final String REGIONAL_DOMAIN_NAME = "RegionalDomainName";
    private static final String BASEPATH = "Basepath";
    private static final String CERTIFICATE_ARN = "certificate";

    private final transient Route53Updater route53Updater;

    public Route53UpdaterTest() {

        AmazonRoute53 client = mockRoute53Client(ZONE_NAME);
        AmazonApiGateway apiGateway = mockApiGatewayClient();
        StaticUrlInfo staticUrlInfo = new StaticUrlInfo(ZONE_NAME, SAMPLE_RECORD_SET_NAME, Stage.TEST);

        route53Updater = new Route53Updater(staticUrlInfo, SAMPLE_API_GATEWAY_REST_API_ID, apiGateway);
        route53Updater.setRoute53Client(client);
    }

    private AmazonRoute53 mockRoute53Client(String zoneName) {
        AmazonRoute53 client = Mockito.mock(AmazonRoute53.class);
        when(client.listHostedZones()).thenReturn(
            new ListHostedZonesResult().withHostedZones(new HostedZone().withId(ZONE_ID).withName(zoneName)));
        return client;
    }

    private AmazonApiGateway mockApiGatewayClient() {
        AmazonApiGateway apiGateway = Mockito.mock(AmazonApiGateway.class);
        when(apiGateway.getDomainName(any())).thenReturn(
            new GetDomainNameResult().withDomainName(DOMAIN_NAME).withRegionalDomainName(REGIONAL_DOMAIN_NAME));
        when(apiGateway.getBasePathMappings(any()))
            .thenReturn(new GetBasePathMappingsResult().withItems(new BasePathMapping().withBasePath(BASEPATH)));
        return apiGateway;
    }

    @Test
    public void updateRecorsrSetsRequest_changeBatchWithOneChange() {

        Optional<ChangeResourceRecordSetsRequest> requestOpt = route53Updater.createUpdateRequest(CERTIFICATE_ARN);
        assertTrue(requestOpt.isPresent());
        assertThat(requestOpt.get().getChangeBatch().getChanges().size(), is(equalTo(1)));
    }

    @Test
    public void updateRecorsrSetsRequest_voidf_ChangeWithChangeActionUpsert() {

        Optional<ChangeResourceRecordSetsRequest> requestOpt = route53Updater.createUpdateRequest(CERTIFICATE_ARN);

        assertTrue(requestOpt.isPresent());
        ChangeResourceRecordSetsRequest request = requestOpt.get();
        Change change = request.getChangeBatch().getChanges().get(0);

        assertThat(change.getAction(), is(equalTo(ChangeAction.UPSERT.toString())));
        assertThat(change.getResourceRecordSet().getType(), is(equalTo(RRType.CNAME.toString())));

        assertThat(change.getResourceRecordSet().getTTL(), is(equalTo(300L)));
    }

    @Test
    public void deleteRecordsSetReqeuset_void_ChangetWithChaggeActionDelete() {
        Optional<ChangeResourceRecordSetsRequest> requestOpt = route53Updater.createDeleteRequest();

        assertTrue(requestOpt.isPresent());
        ChangeResourceRecordSetsRequest request = requestOpt.get();
        Change change = request.getChangeBatch().getChanges().get(0);

        assertThat(change.getAction(), is(equalTo(ChangeAction.DELETE.toString())));
        assertThat(change.getResourceRecordSet().getType(), is(equalTo(RRType.CNAME.toString())));

        assertThat(change.getResourceRecordSet().getTTL(), is(equalTo(300L)));
    }
}
