package no.bibsys.aws.apigateway;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.GetExportRequest;
import com.amazonaws.services.apigateway.model.GetExportResult;
import com.amazonaws.services.apigateway.model.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import no.bibsys.aws.cloudformation.Stage;
import no.bibsys.aws.tools.IoUtils;
import no.bibsys.aws.tools.JsonUtils;

/**
 * Retrieves Information regarding a specific AWS ApiGateway API.
 */
public class ApiGatewayApiInfo {

    private static final String SERVERS_FIELD = "servers";
    private static final String URL_FIELD = "url";
    private static final String VARIABLES_FIELD = "variables";
    private static final String BASE_PATH_FIELD = "basePath";
    private static final String DEFAULT_FIELD = "default";
    private static final String OPENAPI_RESOURCE_FOLDER = "openapi";
    private static final String OPENAPI_YML = "openapi.yml";
    private static final String OPENAPI_TEMPLATE = OPENAPI_YML;
    private static final String SERVER_URL_PLACEHOLDER = "<SERVER_PLACEHOLDER>";
    private static final String STAGE_PLACEHOLDER = "<STAGE_PLACEHOLDER>";
    private final transient Stage stage;
    private final transient AmazonApiGateway client;
    private final transient String restApiId;

    public ApiGatewayApiInfo(Stage stage, AmazonApiGateway apiGatewayClient, String restApiId) {

        this.stage = stage;
        Preconditions.checkNotNull(stage);
        this.client = apiGatewayClient;
        this.restApiId = restApiId;
    }

    public Optional<String> generateOpenApiNoExtensions() throws IOException {
        String openApiTemplate = readOpenApiTemplate();
        Optional<ServerInfo> serverInfo = readServerInfo();
        if (serverInfo.isPresent()) {
            ServerInfo info = serverInfo.get();
            String updatedOpenApiSpecification = injectServerInfo(openApiTemplate, info);
            String openApiJsonSpec = JsonUtils.yamlToJson(updatedOpenApiSpecification);
            return Optional.of(openApiJsonSpec);
        }
        return Optional.empty();
    }

    public Optional<ServerInfo> readServerInfo() throws IOException {
        Map<String, String> requestParameters = new ConcurrentHashMap<>();
        requestParameters.put("accepts", "application/json");
        Optional<JsonNode> amazonApiSpec = readOpenApiSpecFromAmazon(requestParameters);
        return amazonApiSpec.map(apiSpec -> generateServerInfo(apiSpec));
    }

    /**
     * We desire a richer OpenApi documentation than the one that Amazon currently provides. So we read the server
     * address from Api Gateway and we inject the information to the custom OpenApi specification.
     *
     * @param openApiTemplate Custom OpenApi specification
     * @param serverInfo Server URL and URL path variables as produced by ApiGateway
     * @return A Swagger documentation with the correct Server URL
     */
    private String injectServerInfo(String openApiTemplate, ServerInfo serverInfo) throws IOException {

        String replacedSever = openApiTemplate.replace(SERVER_URL_PLACEHOLDER, serverInfo.getServerUrl());
        if (serverInfo.getStage() != null) {
            return replacedSever.replace(STAGE_PLACEHOLDER, serverInfo.getStage());
        } else {
            ObjectMapper yamlParser = JsonUtils.newYamlParser();
            ObjectNode root = (ObjectNode) yamlParser.readTree(replacedSever);
            ArrayNode servers = (ArrayNode) root.get(SERVERS_FIELD);
            ObjectNode server = (ObjectNode) servers.get(0);
            server.remove(VARIABLES_FIELD);
            String removedVariables = yamlParser.writeValueAsString(root);
            return removedVariables;
        }
    }

    public Optional<JsonNode> readOpenApiSpecFromAmazon(Map<String, String> requestParameters) throws IOException {

        try {
            GetExportRequest request = new GetExportRequest().withRestApiId(restApiId).withStageName(stage.toString())
                .withExportType(ApiGatewayConstants.OPEN_API_3).withParameters(requestParameters);
            GetExportResult result = client.getExport(request);
            String swaggerFile = new String(result.getBody().array());
            ObjectMapper parser = JsonUtils.newJsonParser();
            return Optional.ofNullable(parser.readTree(swaggerFile));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    private ServerInfo generateServerInfo(JsonNode openApiSpec) {
        JsonNode serversNode = openApiSpec.get(SERVERS_FIELD).get(0);
        String serverUrl = serversNode.get(URL_FIELD).asText();
        String apiStage = getStageVariable(serversNode).orElse(null);
        return new ServerInfo(serverUrl, apiStage);
    }

    private Optional<String> getStageVariable(JsonNode serversNode) {
        Optional<String> apiStage = Optional.ofNullable(serversNode.get(VARIABLES_FIELD))
            .map(var -> var.get(BASE_PATH_FIELD)).map(basePath -> basePath.get(DEFAULT_FIELD)).map(JsonNode::asText);
        return apiStage;
    }

    private String readOpenApiTemplate() throws IOException {
        return IoUtils.resourceAsString(Paths.get(OPENAPI_RESOURCE_FOLDER, OPENAPI_TEMPLATE));
    }
}
