package no.bibsys.aws.swaggerhub;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

public class SwaggerDriver {


    public static final String APPLICATION_JSON_HEADER = "application/json";
    private static final Logger logger = LoggerFactory.getLogger(SwaggerDriver.class);
    public static final String ACCEPT_HEADER = "accept";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String SWAGGERHUB_RESULT_LOG = "SwaggerHubUpdateResultCode:{}";
    public static final String IS_PRIVATE_PARAMETER = "isPrivate";
    public static final String FORCE_PARAMETER = "force";
    public static final String VERSION_PARAMETER = "version";
    public static final String OAS_VERSION_PARAMETER = "oas";
    private final transient SwaggerHubInfo swaggerHubInfo;


    public SwaggerDriver(SwaggerHubInfo swaggerHubInfo) {
        this.swaggerHubInfo = swaggerHubInfo;
    }

    public String executeGet(HttpGet get) throws IOException {
        CloseableHttpClient client = newRestClient();
        CloseableHttpResponse response = client.execute(get);
        String output = IoUtils.streamToString(response.getEntity().getContent());
        return output;
    }

    private CloseableHttpClient newRestClient() {
        return HttpClients.createMinimal();
    }

    /**
     * It retrieves the OpenAPI specification stored in SwaggerHub for the API specified in the
     * {@code swaggerHubInfo} field.
     *
     * @return The OpenAPI specification stored in SwaggerHub for a specific API.
     */
    public HttpGet getSpecificationRequest(String apiKey) throws URISyntaxException {
        SwaggerHubUrlFormatter swaggerHubUrlFormatter = new SwaggerHubUrlFormatter(swaggerHubInfo,
            true, Collections.emptyMap());
        HttpGet httpGet = createGetRequest(swaggerHubUrlFormatter, apiKey);
        return httpGet;
    }

    private HttpGet createGetRequest(SwaggerHubUrlFormatter swaggerHubUrlFormatter, String apiKey) {
        URI uri = swaggerHubUrlFormatter.getRequestURL();
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
        CloseableHttpClient client = newRestClient();
        CloseableHttpResponse response = client.execute(request);
        int result = response.getStatusLine().getStatusCode();
        if (logger.isInfoEnabled()) {
            logger.info(SWAGGERHUB_RESULT_LOG, result);
        }

        return result;
    }

    public int executePost(HttpPost post) throws IOException {
        return executeUpdate(post);
    }

    public HttpPost createUpdateRequest(String jsonSpec, String apiKey)
        throws URISyntaxException, IOException {

        Map<String, String> parameters = setupRequestParametersForUpdate(
            swaggerHubInfo.getApiVersion());
        SwaggerHubUrlFormatter formatter = new SwaggerHubUrlFormatter(swaggerHubInfo, false,
            parameters);
        HttpPost postOpt = createPostRequest(formatter, jsonSpec, apiKey);
        return postOpt;

    }

    private HttpPost createPostRequest(SwaggerHubUrlFormatter formatter, String jsonSpec,
        String apiKey) {
        HttpPost post = new HttpPost();
        post.setURI(formatter.getRequestURL());
        addHeaders(post, apiKey);
        addBody(post, jsonSpec);
        return post;

    }

    private void addBody(HttpPost post, String jsonSpec) {
        StringEntity stringEntity = new StringEntity(jsonSpec, StandardCharsets.UTF_8);
        post.setEntity(stringEntity);
    }

    private Map<String, String> setupRequestParametersForUpdate(String version) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(IS_PRIVATE_PARAMETER, "false");
        parameters.put(FORCE_PARAMETER, "true");
        parameters.put(VERSION_PARAMETER, version);
        parameters.put(OAS_VERSION_PARAMETER, "3.0.1");
        return parameters;
    }

    public HttpDelete createDeleteApiRequest(String apiKey) throws URISyntaxException {
        SwaggerHubUrlFormatter formatter = new SwaggerHubUrlFormatter(swaggerHubInfo, false,
            Collections.emptyMap());
        HttpDelete delete = createDeleteRequest(formatter, apiKey);
        return delete;
    }

    private HttpDelete createDeleteRequest(SwaggerHubUrlFormatter formatter, String apiKey) {
        HttpDelete delete = new HttpDelete(formatter.getRequestURL());
        addHeaders(delete, apiKey);
        return delete;
    }

    public HttpDelete createDeleteVersionRequest(String apiKey) throws URISyntaxException {

        SwaggerHubUrlFormatter formatter = new SwaggerHubUrlFormatter(swaggerHubInfo, true,
            Collections.emptyMap());
        return createDeleteRequest(formatter, apiKey);
    }

}
