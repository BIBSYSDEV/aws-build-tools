package no.bibsys.aws.apigateway;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.BasePathMapping;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingRequest;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingResult;
import com.amazonaws.services.apigateway.model.CreateDomainNameRequest;
import com.amazonaws.services.apigateway.model.CreateDomainNameResult;
import com.amazonaws.services.apigateway.model.DeleteBasePathMappingRequest;
import com.amazonaws.services.apigateway.model.DeleteDomainNameRequest;
import com.amazonaws.services.apigateway.model.EndpointConfiguration;
import com.amazonaws.services.apigateway.model.EndpointType;
import com.amazonaws.services.apigateway.model.GetBasePathMappingsRequest;
import com.amazonaws.services.apigateway.model.GetDomainNameRequest;
import com.amazonaws.services.apigateway.model.NotFoundException;
import no.bibsys.aws.cloudformation.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Class for creating base path mappings for API Gateway Custom Domain Names.
 */

public class ApiGatewayBasePathMapping {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayBasePathMapping.class);
    private final transient AmazonApiGateway apiGatewayClient;
    private final transient String domainName;
    private final transient Stage stage;
    
    public ApiGatewayBasePathMapping(AmazonApiGateway apiGatewayClient, String domainName, Stage stage) {
        this.apiGatewayClient = apiGatewayClient;
        this.stage = stage;
        this.domainName = domainName;
    }
    
    /**
     * Creates a BasePath mapping in an existing custom domain. Ensure that a custom domain already exists by calling
     * {@link #awsCreateCustomDomainName}.
     *
     * @param apiGatewayRestApiId The id of the ApiGateway Rest API.
     * @return the result of the query.
     */
    public CreateBasePathMappingResult awsCreateBasePath(String apiGatewayRestApiId) {
        CreateBasePathMappingRequest createBasePathMappingRequest = newBasePathMappingRequest(apiGatewayRestApiId);
        return apiGatewayClient.createBasePathMapping(createBasePathMappingRequest);
    }
    
    public void awsDeleteBasePathMappings() throws NotFoundException {
        logger.info("Deleting old basepath Mappings");
        
        Stream<DeleteBasePathMappingRequest> deleteRequests = awsGetBasePathMappings().map(
                this::newDeleteBasePathRequest);
        executeDeleteRequests(deleteRequests);
        DeleteDomainNameRequest deleteDomainNameRequest = new DeleteDomainNameRequest().withDomainName(domainName);
        apiGatewayClient.deleteDomainName(deleteDomainNameRequest);
        
    }
    
    /**
     * Returns the target name for the {@link #domainName}.
     *
     * @return The target name of the Custom Domain Name
     */
    public Optional<String> awsGetTargetDomainName() throws NotFoundException {
        try {
            String targetname = apiGatewayClient.getDomainName(new GetDomainNameRequest().withDomainName(domainName))
                                                .getRegionalDomainName();
            return Optional.ofNullable(targetname);
        } catch (NotFoundException e) {
            logger.warn("No Custom Domain Name found for the name {}", domainName);
            return Optional.empty();
        }
    }
    
    /**
     * Creates a Custom Domain in AWS Api Gateway.
     * @param certifcateArn Certificate ARN. Certificate is stored in Certificate Manager
     * @return an {@link Optional} of a  CreateDomainNameResult if the does not exist already or
     * {@code Optional.empty} if the domain pre-exists
     */
    public Optional<CreateDomainNameResult> awsCreateCustomDomainName(String certifcateArn) {
        if (!awsDomainExists()) {
            return Optional.of(awsCreateDomainName(certifcateArn));
            
        }
        return Optional.empty();
    }
    
    private CreateBasePathMappingRequest newBasePathMappingRequest(String restApiId) {
        return new CreateBasePathMappingRequest().withRestApiId(restApiId).withDomainName(domainName).withStage(
                stage.toString());
    }
    
    private void executeDeleteRequests(Stream<DeleteBasePathMappingRequest> deleteRequests) {
        deleteRequests.forEach(apiGatewayClient::deleteBasePathMapping);
    }
    
    private Stream<BasePathMapping> awsGetBasePathMappings() {
        try {
            GetBasePathMappingsRequest listBasePathsRequest = new GetBasePathMappingsRequest().withDomainName(
                    domainName);
            return apiGatewayClient.getBasePathMappings(listBasePathsRequest).getItems().stream();
        } catch (NotFoundException e) {
            return Stream.empty();
        }
    }
    
    private DeleteBasePathMappingRequest newDeleteBasePathRequest(BasePathMapping item) {
        return new DeleteBasePathMappingRequest().withBasePath(item.getBasePath()).withDomainName(domainName);
    }
    
    private CreateDomainNameResult awsCreateDomainName(String certificateArn) {
        CreateDomainNameRequest createDomainNameRequest = createCreateDomainNameRequest(certificateArn);
        return this.apiGatewayClient.createDomainName(createDomainNameRequest);
    
    }
    
    private CreateDomainNameRequest createCreateDomainNameRequest(String certificateArn) {
        return new CreateDomainNameRequest().withRegionalCertificateArn(certificateArn).withDomainName(domainName)
                                            .withEndpointConfiguration(
                                                    new EndpointConfiguration().withTypes(EndpointType.REGIONAL));
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
