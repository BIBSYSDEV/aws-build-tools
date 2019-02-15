package no.bibsys.aws.apigateway;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingRequest;
import com.amazonaws.services.apigateway.model.CreateBasePathMappingResult;
import com.amazonaws.services.apigateway.model.CreateDomainNameResult;
import com.amazonaws.services.apigateway.model.DeleteDomainNameRequest;
import com.amazonaws.services.apigateway.model.DeleteDomainNameResult;
import com.amazonaws.services.apigateway.model.GetBasePathMappingsRequest;
import com.amazonaws.services.apigateway.model.GetBasePathMappingsResult;
import com.amazonaws.services.apigateway.model.GetDomainNameRequest;
import com.amazonaws.services.apigateway.model.GetDomainNameResult;
import com.amazonaws.services.apigateway.model.NotFoundException;
import no.bibsys.aws.cloudformation.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ApiGatewayBasePathMappingTest {
    
    private static final String SAMPLE_REST_API_ID = "restApi";
    private static final String NOT_EXISTING_DOMAIN = "anotherDomain";
    
    private static final String DOMAIN_NAME = "domain.name.";
    private static final String CERTIFICATE_STRING = "certificateARN";
    private static final String CERTIFCATE_ARN = CERTIFICATE_STRING;
    private static final String ERROR_MESSAGE = "Domain not found";
    private static final int FIRST_ARRAY_ELEMENT = 0;
    private final transient Stage stage = Stage.TEST;
    private AmazonApiGateway apiGateway;
    
    public ApiGatewayBasePathMappingTest() {
        apiGateway = Mockito.mock(AmazonApiGateway.class);
        
        when(apiGateway.createBasePathMapping(any())).thenAnswer((Answer<CreateBasePathMappingResult>) invocation -> {
            CreateBasePathMappingRequest request = invocation.getArgument(FIRST_ARRAY_ELEMENT);
            CreateBasePathMappingResult result = new CreateBasePathMappingResult();
            result.setRestApiId(request.getRestApiId());
            result.setBasePath(request.getDomainName());
            result.setStage(request.getStage());
            return result;
        });
        when(apiGateway.deleteDomainName(any())).thenAnswer((Answer<DeleteDomainNameResult>) invocation -> {
            DeleteDomainNameRequest request = invocation.getArgument(FIRST_ARRAY_ELEMENT);
            String requestDomainName = request.getDomainName();
            if (DOMAIN_NAME.equals(requestDomainName)) {
                return new DeleteDomainNameResult();
            } else {
                throw new NotFoundException(ERROR_MESSAGE);
            }
        });
        
        when(apiGateway.getBasePathMappings(any())).thenAnswer(invocation -> {
            GetBasePathMappingsRequest request = invocation.getArgument(FIRST_ARRAY_ELEMENT);
            if (DOMAIN_NAME.equals(request.getDomainName())) {
                return new GetBasePathMappingsResult().withItems(Collections.emptyList());
            } else {
                throw new NotFoundException(ERROR_MESSAGE);
            }
        });
        
        when(apiGateway.getDomainName(any())).thenAnswer(invocation -> {
            GetDomainNameRequest request = invocation.getArgument(0);
            if (DOMAIN_NAME.equals(request.getDomainName())) {
                return new GetDomainNameResult().withRegionalDomainName(DOMAIN_NAME);
            } else {
                throw new NotFoundException(ERROR_MESSAGE);
            }
        });
        
        when(apiGateway.createDomainName(any())).thenReturn(new CreateDomainNameResult());
    }
    
    @Test
    public void createBasePathMapping_restApiIdDomainStage_newBasePathMappingWith() {
        ApiGatewayBasePathMapping apiGatewayBasePathMapping =
                new ApiGatewayBasePathMapping(apiGateway, DOMAIN_NAME, stage);
        CreateBasePathMappingResult result = apiGatewayBasePathMapping.awsCreateBasePath(SAMPLE_REST_API_ID);
        assertThat(result.getBasePath(), containsString(DOMAIN_NAME));
        assertThat(result.getRestApiId(), is(SAMPLE_REST_API_ID));
        assertThat(result.getStage(), is(stage.toString()));
    }
    
    @Test
    public void deleteBasePathMappings_restApiDomainStage_deleteCorrectBasepathMappings() {
        ApiGatewayBasePathMapping apiGatewayBasePathMapping =
                new ApiGatewayBasePathMapping(apiGateway, DOMAIN_NAME, stage);
        apiGatewayBasePathMapping.awsDeleteBasePathMappings();
    }
    
    @Test
    public void deleteBasePathMappings_notExistingDomainStage_throwException() {
        ApiGatewayBasePathMapping apiGatewayBasePathMapping =
                new ApiGatewayBasePathMapping(apiGateway, NOT_EXISTING_DOMAIN, stage);
        assertThrows(NotFoundException.class, apiGatewayBasePathMapping::awsDeleteBasePathMappings);
    }
    
    @Test
    public void getTargetDomain_existingDomainName_targetDomain() {
        ApiGatewayBasePathMapping apiGatewayBasePathMapping =
                new ApiGatewayBasePathMapping(apiGateway, DOMAIN_NAME, stage);
        Optional<String> targetDomain = apiGatewayBasePathMapping.awsGetTargetDomainName();
        assertThat(targetDomain.isPresent(), is(true));
        assertThat(targetDomain.get(), is(equalTo(DOMAIN_NAME)));
    }
    
    @Test
    public void getTargetDomain_notExistingDomainName_emptyOptional() {
        ApiGatewayBasePathMapping apiGatewayBasePathMapping =
                new ApiGatewayBasePathMapping(apiGateway, NOT_EXISTING_DOMAIN, stage);
        Optional<String> targetDomain = apiGatewayBasePathMapping.awsGetTargetDomainName();
        assertThat(targetDomain.isPresent(), is(false));
    }
    
    @Test
    public void awsCreateCustomDomainName_notExistingDomainName_newDomainName() {
        ApiGatewayBasePathMapping apiGatewayBasePathMapping =
                new ApiGatewayBasePathMapping(apiGateway, NOT_EXISTING_DOMAIN, stage);
        Optional<CreateDomainNameResult> result = apiGatewayBasePathMapping.awsCreateCustomDomainName(CERTIFCATE_ARN);
        assertThat(result.isPresent(), is(equalTo(true)));
    }
    
    @Test
    public void awsCreateCustomDomainName_existingDomainName_newDomainName() {
        ApiGatewayBasePathMapping apiGatewayBasePathMapping =
                new ApiGatewayBasePathMapping(apiGateway, DOMAIN_NAME, stage);
        Optional<CreateDomainNameResult> result = apiGatewayBasePathMapping.awsCreateCustomDomainName(CERTIFCATE_ARN);
        assertThat(result.isPresent(), is(equalTo(false)));
    }
}
