package no.bibsys.aws.apigateway;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.BasePathMapping;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingRequest;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingResult;
import com.amazonaws.services.apigateway.model.CreateDomainNameRequest;
import com.amazonaws.services.apigateway.model.DeleteBasePathMappingRequest;
import com.amazonaws.services.apigateway.model.DeleteDomainNameRequest;
import com.amazonaws.services.apigateway.model.EndpointConfiguration;
import com.amazonaws.services.apigateway.model.EndpointType;
import com.amazonaws.services.apigateway.model.GetBasePathMappingsRequest;
import com.amazonaws.services.apigateway.model.GetDomainNameRequest;
import com.amazonaws.services.apigateway.model.NotFoundException;
import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.stream.Stream;
import no.bibsys.aws.cloudformation.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for creating base path mappings for API Gateway Custom Domain Names.
 */

public class ApiGatewayBasePathMapping {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayBasePathMapping.class);
    private final transient AmazonApiGateway apiGatewayClient;
    private final transient String domainName;
    private final transient Stage stage;


    public ApiGatewayBasePathMapping(AmazonApiGateway apiGatewayClient, String domainName,
        Stage stage) {
        this.apiGatewayClient = apiGatewayClient;
        this.stage = stage;
        this.domainName = domainName;

    }


    public CreateBasePathMappingResult awsCreateBasePath(String apiGatewayRestApiId,
        String certificateArn) {
        awsDeleteBasePathMappings();
        awsCheckAndCreateCustomDomainName(certificateArn);

        CreateBasePathMappingRequest createBasePathMappingRequest = newBasePathMappingRequest(
            apiGatewayRestApiId);

        return apiGatewayClient.createBasePathMapping(createBasePathMappingRequest);
    }


    public void awsDeleteBasePathMappings() {
        logger.info("Deleting old basepath Mappings");

        try {
            Stream<DeleteBasePathMappingRequest> deleteRequests = awsGetBasePathMappings()
                .map(this::newDeleteBasePathRequest);
            executeDeleteRequests(deleteRequests);

            DeleteDomainNameRequest deleteDomainNameRequest = new DeleteDomainNameRequest()
                .withDomainName(domainName);
            apiGatewayClient.deleteDomainName(deleteDomainNameRequest);
        } catch (NotFoundException e) {
            logger.warn("Custom domain name not found");
        }

    }


    public Optional<String> awsGetTargetDomainName() throws NotFoundException {
        try {
            String targetname = apiGatewayClient
                .getDomainName(new GetDomainNameRequest().withDomainName(domainName))
                .getRegionalDomainName();
            return Optional.ofNullable(targetname);
        } catch (NotFoundException e) {
            logger.warn("No Custom Domain Name found for the name {}", domainName);
            return Optional.empty();
        }


    }



    private void awsCheckAndCreateCustomDomainName(String certifcateArn) {
        if (!awsDomainExists()) {
            awsCreateDomainName(certifcateArn);
        }
    }




    @VisibleForTesting
    public CreateBasePathMappingRequest newBasePathMappingRequest(String restApiId) {
        return new CreateBasePathMappingRequest().withRestApiId(restApiId)
            .withDomainName(domainName)
            .withStage(stage.toString());
    }

    private void executeDeleteRequests(Stream<DeleteBasePathMappingRequest> deleteRequests) {
        deleteRequests.forEach(apiGatewayClient::deleteBasePathMapping);
    }



    private Stream<BasePathMapping> awsGetBasePathMappings() {
        try {
            GetBasePathMappingsRequest listBasePathsRequest = new GetBasePathMappingsRequest()
                .withDomainName(domainName);
            return apiGatewayClient.getBasePathMappings(listBasePathsRequest).getItems().stream();
        } catch (NotFoundException e) {
            return Stream.empty();
        }
    }

    private DeleteBasePathMappingRequest newDeleteBasePathRequest(BasePathMapping item) {
        return new DeleteBasePathMappingRequest().withBasePath(item.getBasePath())
            .withDomainName(domainName);
    }


    private void awsCreateDomainName(String certificateArn) {

        CreateDomainNameRequest createDomainNameRequest =
            new CreateDomainNameRequest().withRegionalCertificateArn(certificateArn)
                .withDomainName(domainName)
                .withEndpointConfiguration(
                    new EndpointConfiguration().withTypes(EndpointType.REGIONAL));

        this.apiGatewayClient.createDomainName(createDomainNameRequest);
    }

    private boolean awsDomainExists() {
        try {
            this.apiGatewayClient.getDomainName(new GetDomainNameRequest().withDomainName(domainName));
        } catch (NotFoundException e) {
            return false;
        }

        return true;
    }


}
