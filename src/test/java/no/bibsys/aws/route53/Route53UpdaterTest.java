package no.bibsys.aws.route53;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.BasePathMapping;
import com.amazonaws.services.apigateway.model.CreateDomainNameResult;
import com.amazonaws.services.apigateway.model.GetBasePathMappingsResult;
import com.amazonaws.services.apigateway.model.GetDomainNameResult;
import com.amazonaws.services.apigateway.model.NotFoundException;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.RRType;
import no.bibsys.aws.cloudformation.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class Route53UpdaterTest {
    
    private static final String ZONE_NAME = "ZoneName";
    private static final String ZONE_ID = "ZoneId";
    private static final String SAMPLE_RECORD_SET_NAME = "some.url.goes.here.";
    private static final String SAMPLE_API_GATEWAY_REST_API_ID = "apiGatewarRestApiId";
    private static final String DOMAIN_NAME = "DomainName";
    private static final String REGIONAL_DOMAIN_NAME = "RegionalDomainName";
    private static final String BASEPATH = "Basepath";
    private static final String CERTIFICATE_ARN = "certificate";
    private static final String NOT_FOUND_EXCEPTION_MESSAGE = "Not Found";
    private static final int FAILURE = 1;
    private static final String PRIVATE_METHOD = "deletePossiblyExistingMappings";
    private static final int EXACTLY_ONE_RECORDSET = 1;
    
    private final transient AmazonRoute53 route53Client;
    private final transient AmazonApiGateway apiGateway;
    private final transient StaticUrlInfo staticUrlInfo =
        new StaticUrlInfo(ZONE_NAME, SAMPLE_RECORD_SET_NAME, Stage.TEST);
    
    private final transient Route53Updater route53Updater;
    
    public Route53UpdaterTest() {
        route53Client = mockRoute53Client(ZONE_NAME);
        apiGateway = mockApiGatewayClient();
    
        route53Updater = new Route53Updater(staticUrlInfo, SAMPLE_API_GATEWAY_REST_API_ID, apiGateway, route53Client);
    }
    
    @Test
    public void copy_Route53Updater_newInstanceWithNewStage() {
        Route53Updater newUpdater = route53Updater.copy(Stage.FINAL);
        assertThat(newUpdater, is(not(equalTo(route53Updater))));
    }
    
    @Test
    public void updateRecordsSetsRequest_changeBatchWithOneChange() {
        
        Optional<ChangeResourceRecordSetsRequest> requestOpt = route53Updater.createUpdateRequest(CERTIFICATE_ARN);
        assertTrue(requestOpt.isPresent());
        assertThat(requestOpt.get().getChangeBatch().getChanges().size(), is(equalTo(EXACTLY_ONE_RECORDSET)));
    }
    
    @Test
    public void createUpdateRequest_existingDomain_noException() {
        Route53Updater route53Updater = new Route53Updater(staticUrlInfo, SAMPLE_API_GATEWAY_REST_API_ID,
            mockApiGatewayClientThrowingNotFoundExceptionForDeleteDomainName(), route53Client);
        
        Optional<ChangeResourceRecordSetsRequest> requestOpt = route53Updater.createUpdateRequest(CERTIFICATE_ARN);
        assertTrue(requestOpt.isPresent());
        assertThat(requestOpt.get().getChangeBatch().getChanges().size(), is(equalTo(EXACTLY_ONE_RECORDSET)));
    }
    
    @Test
    public void updateRecordsSetsRequest_void_ChangeWithChangeActionUpsert() {
        
        Optional<ChangeResourceRecordSetsRequest> requestOpt = route53Updater.createUpdateRequest(CERTIFICATE_ARN);
        
        assertTrue(requestOpt.isPresent());
        ChangeResourceRecordSetsRequest request = requestOpt.get();
        Change change = request.getChangeBatch().getChanges().get(0);
        
        assertThat(change.getAction(), is(equalTo(ChangeAction.UPSERT.toString())));
        assertThat(change.getResourceRecordSet().getType(), is(equalTo(RRType.CNAME.toString())));
        
        assertThat(change.getResourceRecordSet().getTTL(), is(equalTo(300L)));
    }
    
    @Test
    public void deleteRecordsSetRequset_void_ChangetWithChaggeActionDelete() {
        Optional<ChangeResourceRecordSetsRequest> requestOpt = route53Updater.createDeleteRequest();
        
        assertTrue(requestOpt.isPresent());
        ChangeResourceRecordSetsRequest request = requestOpt.get();
        Change change = request.getChangeBatch().getChanges().get(0);
        
        assertThat(change.getAction(), is(equalTo(ChangeAction.DELETE.toString())));
        assertThat(change.getResourceRecordSet().getType(), is(equalTo(RRType.CNAME.toString())));
        
        assertThat(change.getResourceRecordSet().getTTL(), is(equalTo(300L)));
    }
    
    @Test
    public void deleteRecordsSetRequest_nonExistingDomain_emptyOptional() {
        Route53Updater route53Updater = new Route53Updater(staticUrlInfo, SAMPLE_API_GATEWAY_REST_API_ID,
            mockApiGatewayClientThrowingNotFoundExceptionForGetDomainName(),
                route53Client);
        
        Optional<ChangeResourceRecordSetsRequest> result = route53Updater.createDeleteRequest();
        assertThat(result, is(equalTo(Optional.empty())));
    }
    
    @Test
    public void executeDeleteRequest_request_result() {
        Optional<ChangeResourceRecordSetsResult> result =
            route53Updater.createDeleteRequest().map(route53Updater::executeDeleteRequest);
        
        assertTrue(result.isPresent());
    }
    
    @Test
    public void executUpdateRequest_request_result() {
        Optional<ChangeResourceRecordSetsResult> result =
            route53Updater.createUpdateRequest(CERTIFICATE_ARN).map(route53Updater::executeUpdateRequest);
        
        assertTrue(result.isPresent());
    }
    
    @Test
    public void setRoute53Client_route53Client_newRoute53Client() {
        route53Updater.setRoute53Client(null);
        assertThrows(NullPointerException.class,
            () -> route53Updater.createDeleteRequest().ifPresent(route53Updater::executeDeleteRequest));
    }
    
    private AmazonRoute53 mockRoute53Client(String zoneName) {
        AmazonRoute53 client = Mockito.mock(AmazonRoute53.class);
        when(client.listHostedZones()).thenReturn(
            new ListHostedZonesResult().withHostedZones(new HostedZone().withId(ZONE_ID).withName(zoneName)));
        when(client.changeResourceRecordSets(any())).thenReturn(new ChangeResourceRecordSetsResult());
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
    
    private AmazonApiGateway mockApiGatewayClientThrowingNotFoundExceptionForGetDomainName() {
        AmazonApiGateway apiGateway = Mockito.mock(AmazonApiGateway.class);
        when(apiGateway.getDomainName(any())).thenThrow(new NotFoundException(NOT_FOUND_EXCEPTION_MESSAGE));
        when(apiGateway.getBasePathMappings(any())).thenThrow(new NotFoundException(NOT_FOUND_EXCEPTION_MESSAGE));
        when(apiGateway.deleteDomainName(any())).thenThrow(new NotFoundException(NOT_FOUND_EXCEPTION_MESSAGE));
        when(apiGateway.createDomainName(any())).thenReturn(new CreateDomainNameResult());
        return apiGateway;
    }
    
    private AmazonApiGateway mockApiGatewayClientThrowingNotFoundExceptionForDeleteDomainName() {
        AmazonApiGateway apiGateway = Mockito.mock(AmazonApiGateway.class);
        when(apiGateway.deleteDomainName(any())).thenThrow(new NotFoundException(NOT_FOUND_EXCEPTION_MESSAGE));
        when(apiGateway.getBasePathMappings(any())).thenThrow(new NotFoundException(NOT_FOUND_EXCEPTION_MESSAGE));
        when(apiGateway.createDomainName(any())).thenReturn(new CreateDomainNameResult());
        when(apiGateway.getDomainName(any())).thenReturn(
            new GetDomainNameResult().withDomainName(DOMAIN_NAME).withRegionalDomainName(REGIONAL_DOMAIN_NAME));
        return apiGateway;
    }
}
