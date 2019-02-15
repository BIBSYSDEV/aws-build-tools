package no.bibsys.aws.apigateway;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.GetExportRequest;
import com.amazonaws.services.apigateway.model.GetExportResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.tools.JsonUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Retrieves Information regarding a specific AWS ApiGateway API.
 */
public class ApiGatewayInfo {
    
    private static final String SERVERS_FIELD = "servers";
    private static final String URL_FIELD = "url";
    private static final String VARIABLES_FIELD = "variables";
    private static final String BASE_PATH_FIELD = "basePath";
    private static final String DEFAULT_FIELD = "default";
    private static final int FIRST_ARRAY_ELEMENT = 0;
    
    private final transient Stage stage;
    private final transient AmazonApiGateway client;
    private final transient String restApiId;
    
    public ApiGatewayInfo(Stage stage, AmazonApiGateway apiGatewayClient, String restApiId) {
        
        this.stage = stage;
        Preconditions.checkNotNull(stage);
        this.client = apiGatewayClient;
        this.restApiId = restApiId;
    }
    
    public ServerInfo readServerInfo() throws IOException {
        Map<String, String> requestParameters = new ConcurrentHashMap<>();
        requestParameters.put("accepts", "application/json");
        JsonNode amazonApiSpec = readOpenApiSpecFromAmazon(requestParameters);
        return generateServerInfo(amazonApiSpec);
    }
    
    private JsonNode readOpenApiSpecFromAmazon(Map<String, String> requestParameters) throws IOException {
        
        GetExportRequest request = createApiExportRequest(requestParameters);
        GetExportResult result = client.getExport(request);
        String swaggerFile = new String(result.getBody().array());
        ObjectMapper parser = JsonUtils.newJsonParser();
        return parser.readTree(swaggerFile);
        
    }
    
    private GetExportRequest createApiExportRequest(Map<String, String> requestParameters) {
        return new GetExportRequest().withRestApiId(restApiId).withStageName(stage.toString())
                                     .withExportType(ApiGatewayConstants.OPEN_API_3).withParameters(requestParameters);
    }
    
    private ServerInfo generateServerInfo(JsonNode openApiSpec) {
        JsonNode serversNode = openApiSpec.get(SERVERS_FIELD).get(FIRST_ARRAY_ELEMENT);
        String serverUrl = serversNode.get(URL_FIELD).asText();
        String apiStage = getStageVariable(serversNode).orElse(null);
        return new ServerInfo(serverUrl, apiStage);
    }
    
    private Optional<String> getStageVariable(JsonNode serversNode) {
        return Optional.ofNullable(serversNode.get(VARIABLES_FIELD)).map(var -> var.get(BASE_PATH_FIELD))
                       .map(basePath -> basePath.get(DEFAULT_FIELD)).map(JsonNode::asText);
    }
    
}
