package no.bibsys.aws.swaggerhub;

import no.bibsys.aws.tools.IoUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SwaggerDriver {
    
    private static final String APPLICATION_JSON_HEADER = "application/json";
    private static final String ACCEPT_HEADER = "accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String AUTHORIZATION = "Authorization";
    private static final String SWAGGERHUB_RESULT_LOG = "SwaggerHubUpdateResultCode:{}";
    private static final String IS_PRIVATE_PARAMETER = "isPrivate";
    private static final String FORCE_PARAMETER = "force";
    private static final String VERSION_PARAMETER = "version";
    private static final String OAS_VERSION_PARAMETER = "oas";
    private static final Logger logger = LoggerFactory.getLogger(SwaggerDriver.class);
    private final transient SwaggerHubInfo swaggerHubInfo;
    private final transient CloseableHttpClient restClient;
    
    public SwaggerDriver(SwaggerHubInfo swaggerHubInfo) {
        this(swaggerHubInfo, newRestClient());
    }
    
    public SwaggerDriver(SwaggerHubInfo swaggerHubInfo, CloseableHttpClient client) {
        this.swaggerHubInfo = swaggerHubInfo;
        this.restClient = client;
    }
    
    public static CloseableHttpClient newRestClient() {
        return HttpClients.createMinimal();
    }
    
    public String executeGet(HttpGet get) throws IOException {
        CloseableHttpResponse response = restClient.execute(get);
        return IoUtils.streamToString(response.getEntity().getContent());
    }
    
    /**
     * It retrieves the OpenAPI specification stored in SwaggerHub for the API specified in the {@code swaggerHubInfo}
     * field.
     *
     * @param apiKey The API key
     * @return The OpenAPI specification stored in SwaggerHub for a specific API.
     * @throws URISyntaxException is thrown if URI incorrectly formatted
     */
    public HttpGet getSpecificationRequest(String apiKey) throws URISyntaxException {
        SwaggerHubUrlFormatter swaggerHubUrlFormatter =
                new SwaggerHubUrlFormatter(swaggerHubInfo, true, Collections.emptyMap());
        return createGetRequest(swaggerHubUrlFormatter, apiKey);
    }
    
    private HttpGet createGetRequest(SwaggerHubUrlFormatter swaggerHubUrlFormatter, String apiKey) {
        URI uri = swaggerHubUrlFormatter.getRequestUrl();
        HttpGet get = new HttpGet(uri);
        addHeaders(get, apiKey);
        return get;
    }
    
    private void addHeaders(HttpUriRequest post, String apiKey) {
        post.addHeader(ACCEPT_HEADER, APPLICATION_JSON_HEADER);
        post.addHeader(CONTENT_TYPE, APPLICATION_JSON_HEADER);
        post.addHeader(AUTHORIZATION, apiKey);
    }
    
    public int executeDelete(HttpDelete delete) throws IOException {
        return executeUpdate(delete);
    }
    
    private int executeUpdate(HttpUriRequest request) throws IOException {
        CloseableHttpResponse response = restClient.execute(request);
        int result = response.getStatusLine().getStatusCode();
        if (logger.isInfoEnabled()) {
            logger.info(SWAGGERHUB_RESULT_LOG, result);
        }
        
        return result;
    }
    
    public int executePost(HttpPost post) throws IOException {
        return executeUpdate(post);
    }
    
    public HttpPost createUpdateRequest(String jsonSpec, String apiKey) throws URISyntaxException {
        
        Map<String, String> parameters = setupRequestParametersForUpdate(swaggerHubInfo.getApiVersion());
        SwaggerHubUrlFormatter formatter = new SwaggerHubUrlFormatter(swaggerHubInfo, false, parameters);
        return createPostRequest(formatter, jsonSpec, apiKey);
    }
    
    private HttpPost createPostRequest(SwaggerHubUrlFormatter formatter, String jsonSpec, String apiKey) {
        HttpPost post = new HttpPost();
        post.setURI(formatter.getRequestUrl());
        addHeaders(post, apiKey);
        addBody(post, jsonSpec);
        return post;
    }
    
    private void addBody(HttpPost post, String jsonSpec) {
        StringEntity stringEntity = new StringEntity(jsonSpec, StandardCharsets.UTF_8);
        post.setEntity(stringEntity);
    }
    
    private Map<String, String> setupRequestParametersForUpdate(String version) {
        Map<String, String> parameters = new ConcurrentHashMap<>();
        parameters.put(IS_PRIVATE_PARAMETER, "false");
        parameters.put(FORCE_PARAMETER, "true");
        parameters.put(VERSION_PARAMETER, version);
        parameters.put(OAS_VERSION_PARAMETER, "3.0.1");
        return parameters;
    }
    
    public HttpDelete createDeleteApiRequest(String apiKey) throws URISyntaxException {
        SwaggerHubUrlFormatter formatter = new SwaggerHubUrlFormatter(swaggerHubInfo, false, Collections.emptyMap());
        return createDeleteRequest(formatter, apiKey);
    }
    
    private HttpDelete createDeleteRequest(SwaggerHubUrlFormatter formatter, String apiKey) {
        HttpDelete delete = new HttpDelete(formatter.getRequestUrl());
        addHeaders(delete, apiKey);
        return delete;
    }
    
    public HttpDelete createDeleteVersionRequest(String apiKey) throws URISyntaxException {
        
        SwaggerHubUrlFormatter formatter = new SwaggerHubUrlFormatter(swaggerHubInfo, true, Collections.emptyMap());
        return createDeleteRequest(formatter, apiKey);
    }
}
