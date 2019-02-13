package no.bibsys.aws.apigateway;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.NotFoundException;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.mocks.MockGetExportResult;
import no.bibsys.aws.tools.Environment;
import no.bibsys.aws.tools.IoUtils;
import no.bibsys.aws.tools.JsonUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ApiGatewayApiInfoTest {
    
    private static final String MOCK_API_ID = "RestApiId";
    private static final String SERVER_URL = "<SERVER_PLACEHOLDER>";
    private static final String SAMPLE_OPENAPI_YML = "openapi.yml";
    private static final String RESOURCES = "openapi";
    
    private AmazonApiGateway apiGateway;
    private AmazonApiGateway apiGatewayApiNotFound;
    private Environment environment;
    
    public ApiGatewayApiInfoTest() throws IOException {
        environment = Mockito.mock(Environment.class);
        when(environment.readEnv(anyString())).thenReturn(Stage.TEST.toString());
        
        String openApiJson = readOpenApiInfoFromResources();
        apiGateway = Mockito.mock(AmazonApiGateway.class);
        when(apiGateway.getExport(any())).thenReturn(new MockGetExportResult(openApiJson));
        
        apiGatewayApiNotFound = Mockito.mock(AmazonApiGateway.class);
        when(apiGatewayApiNotFound.getExport(any())).thenThrow(new NotFoundException(("Not found")));
    }
    
    private String readOpenApiInfoFromResources() throws IOException {
        String openApiYaml = IoUtils.resourceAsString(Paths.get(RESOURCES, SAMPLE_OPENAPI_YML));
        return JsonUtils.yamlToJson(openApiYaml);
    }
    
    @Test
    public void readServerInfo_existingApiGatewayEndpoint_serverUrl() throws IOException {
        ApiGatewayInfo apiGatewayInfo = new ApiGatewayInfo(Stage.TEST, apiGateway, MOCK_API_ID);
        ServerInfo serverInfo = apiGatewayInfo.readServerInfo();
        assertThat(serverInfo.getServerUrl(), is(equalTo(SERVER_URL)));
    }
    
    @Test
    public void readServerInfo_nonExistingApiGatewayEndpoin_NotFoundException() throws IOException {
        ApiGatewayInfo apiGatewayInfo = new ApiGatewayInfo(Stage.TEST, apiGatewayApiNotFound, MOCK_API_ID);
        assertThrows(NotFoundException.class, apiGatewayInfo::readServerInfo);
    }
    
}
