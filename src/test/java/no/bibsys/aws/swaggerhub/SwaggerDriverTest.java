package no.bibsys.aws.swaggerhub;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;



public class SwaggerDriverTest {
    
    private static final String JSON_SPEC = "{\"key\":\"value\"}";
    private static final String API_KEY = "apiKey";
    private final transient String organization = "unit";
    private final transient String apiId = "api-id";
    private final transient String apiKey = "ApIKeY";
    private final transient String apiVersion = "2.1";
    private final transient SwaggerDriver driver;
    
    public SwaggerDriverTest() throws IOException {
        
        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        when(httpClient.execute(any())).thenReturn(new CustomReponse());
        
        this.driver = new SwaggerDriver(
                new SwaggerHubInfo(apiId, apiVersion, organization, Region.getRegion(Regions.EU_WEST_1)), httpClient);
    }
    
    @Test
    public void executePost_updateRequest_updateResult() throws IOException, URISyntaxException {
        HttpPost postRequest = driver.createUpdateRequest(JSON_SPEC, API_KEY);
        int result = driver.executePost(postRequest);
        assertThat(result, is(equalTo(HttpStatus.SC_OK)));
    }
    
    @Test
    public void executeDelete_deleteRequest_deleteResult() throws IOException, URISyntaxException {
        HttpDelete deleteRequest = driver.createDeleteApiRequest(API_KEY);
        int result = driver.executeDelete(deleteRequest);
        assertThat(result, is(equalTo(HttpStatus.SC_OK)));
        
    }
    
    @Test
    public void executeGet_getRequest_getResult() throws IOException, URISyntaxException {
        HttpGet getRequest = driver.getSpecificationRequest(API_KEY);
        String result = driver.executeGet(getRequest);
        assertThat(result, is(equalTo(CustomReponse.MESSAGE)));
    }
    
    @Test
    public void createUpdateRequest_apiVersionAndJsonSpec_UrlIncludesOrganizationAndApiId()
            throws URISyntaxException, IOException {
        HttpPost post = postRequest();
        String path = post.getURI().toURL().getPath();
        assertThat(path, (containsString(organization)));
        assertThat(path, (containsString(apiId)));
    }
    
    @Test
    public void createUpdateRequest_apiVersionAndJsonSpec_UrlDoesNotIncludeApiVersion()
            throws URISyntaxException, IOException {
        HttpPost post = postRequest();
        String path = post.getURI().toURL().getPath();
        assertThat(path, not(containsString(apiVersion)));
    }
    
    @Test
    public void createUpdateRequest_apiVersionAndJsonSpec_requestParametersIncludeApiVersion()
            throws URISyntaxException {
        
        HttpPost post = postRequest();
        String parameters = post.getURI().getQuery();
        assertThat(parameters, containsString("version"));
        assertThat(parameters, containsString(apiVersion));
    }
    
    @Test
    public void createUpdateRequest_apiVersionAndJsonSpec_requestParametersIncludeForceParameter()
            throws URISyntaxException {
        
        HttpPost post = postRequest();
        String parameters = post.getURI().getQuery();
        assertThat(parameters, containsString("force"));
    }
    
    @Test
    public void createUpdateRequest_apiVersionAndJsonSpec_requestParametersIncludePrivateParameter()
            throws URISyntaxException {
        
        HttpPost post = postRequest();
        String parameters = post.getURI().getQuery();
        assertThat(parameters, containsString("isPrivate"));
    }
    
    @Test
    public void createDeleteVersionRequest_apiVersion_urlPathIncludedOrganizationApiIdAndVersion()
            throws URISyntaxException, MalformedURLException {
        HttpDelete delete = deleteVersionRequest();
        assertThat(delete.getURI().toURL().toString(), containsString(organization));
        assertThat(delete.getURI().toURL().toString(), containsString(apiId));
        assertThat(delete.getURI().toURL().toString(), containsString(apiVersion));
        assertThat(delete.getURI().toURL().getQuery(), is(equalTo(null)));
    }
    
    @Test
    public void createDeleteRequest_null_urlPathIncludesOrganizationApiIdButNotVersion()
            throws URISyntaxException, MalformedURLException {
        HttpDelete delete = deleteApiRequest();
        assertThat(delete.getURI().toURL().toString(), containsString(organization));
        assertThat(delete.getURI().toURL().toString(), containsString(apiId));
        assertThat(delete.getURI().toURL().toString(), not(containsString(apiVersion)));
        assertThat(delete.getURI().toURL().getQuery(), is(equalTo(null)));
    }
    
    private HttpDelete deleteApiRequest() throws URISyntaxException {
        return driver.createDeleteApiRequest(apiKey);
    }
    
    private HttpDelete deleteVersionRequest() throws URISyntaxException {
        return driver.createDeleteVersionRequest(apiKey);
    }
    
    private HttpPost postRequest() throws URISyntaxException {
        return driver.createUpdateRequest(JSON_SPEC, apiKey);
    }
}
