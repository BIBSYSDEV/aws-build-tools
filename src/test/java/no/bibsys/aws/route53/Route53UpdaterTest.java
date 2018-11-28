package no.bibsys.aws.route53;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.GetDomainNameResult;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.RRType;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import no.bibsys.aws.apigateway.ServerInfo;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.git.github.GitInfo;
import no.bibsys.aws.git.github.GitInfoImpl;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;

public class Route53UpdaterTest {


    private final transient Route53Updater route53Updater;

    public Route53UpdaterTest() {



        String zoneName = "ZoneName";
        AmazonRoute53 client = mockRoute53Client(zoneName);
        AmazonApiGateway apiGateway = mockApiGatewayClient();
        GitInfo gitInfo = new GitInfoImpl("owner", "repository", "branch");
        StaticUrlInfo staticUrlINfo = StaticUrlInfo
            .create(Stage.TEST, zoneName, "some.url.goes.here.");

        route53Updater = new Route53Updater(staticUrlINfo, gitInfo, Stage.TEST, "apiGatewarRestApiId",
            apiGateway);
        route53Updater.setRoute53Client(client);

    }

    private AmazonRoute53 mockRoute53Client(String zoneName) {
        AmazonRoute53 client = Mockito.mock(AmazonRoute53.class);
        when(client.listHostedZones()).thenReturn(
                new ListHostedZonesResult().withHostedZones(new HostedZone().withId("ZoneId").withName(
                    zoneName)));
        return client;
    }

    private AmazonApiGateway mockApiGatewayClient() {
        AmazonApiGateway apiGateway = Mockito.mock(AmazonApiGateway.class);
        when(apiGateway.getDomainName(any())).thenReturn(new GetDomainNameResult().withDomainName("DomainName")
            .withRegionalDomainName("RegionalDomainName"));
        return apiGateway;
    }


    @Test
    public void updateRecorsrSetsRequest_changeBatchWithOneChange() {

        Optional<ChangeResourceRecordSetsRequest> requestOpt = route53Updater
            .createUpdateRequest();
        assertTrue(requestOpt.isPresent());
        assertThat(requestOpt.get().getChangeBatch().getChanges().size(), is(equalTo(1)));

    }


    @Test
    public void updateRecorsrSetsRequest_voidf_ChangeWithChangeActionUpsert(){

        Optional<ChangeResourceRecordSetsRequest> requestOpt = route53Updater.createUpdateRequest();

        assertTrue(requestOpt.isPresent());
        ChangeResourceRecordSetsRequest request = requestOpt.get();
        Change change = request.getChangeBatch().getChanges().get(0);

        assertThat(change.getAction(), is(equalTo(ChangeAction.UPSERT.toString())));
        assertThat(change.getResourceRecordSet().getType(), is(equalTo(RRType.CNAME.toString())));
        assertThat(change.getResourceRecordSet().getName(), CoreMatchers.startsWith("test."));
        assertThat(change.getResourceRecordSet().getName(), not(CoreMatchers.startsWith("test.test.")));
        assertThat(change.getResourceRecordSet().getTTL(), is(equalTo(300L)));
    }


    @Test
    public void deleteRecordsSetReqeuset_void_ChangetWithChaggeActionDelete(){
        Optional<ChangeResourceRecordSetsRequest> requestOpt = route53Updater.createDeleteRequest();

        assertTrue(requestOpt.isPresent());
        ChangeResourceRecordSetsRequest request = requestOpt.get();
        Change change = request.getChangeBatch().getChanges().get(0);

        assertThat(change.getAction(), is(equalTo(ChangeAction.DELETE.toString())));
        assertThat(change.getResourceRecordSet().getType(), is(equalTo(RRType.CNAME.toString())));
        assertThat(change.getResourceRecordSet().getName(), CoreMatchers.startsWith("test."));
        assertThat(change.getResourceRecordSet().getName(), not(CoreMatchers.startsWith("test.test.")));
        assertThat(change.getResourceRecordSet().getTTL(), is(equalTo(300L)));
    }





}
