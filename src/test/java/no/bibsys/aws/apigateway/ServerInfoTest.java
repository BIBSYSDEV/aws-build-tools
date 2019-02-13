package no.bibsys.aws.apigateway;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

public class ServerInfoTest {

    private static final String BASE_PATH = "/{basePath}";
    private static final String SAMPLE_API_GATEWAY_URL = "xxxxxxxx.execute-api.eu-west-1.amazonaws.com";

    private static final String FINAL_STAGE = "final";
    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";

    @Test
    public void completeServerUrl_HttpsServerUrlAndStage_workingServerUrl() {
        ServerInfo serverInfo = new ServerInfo(HTTPS_PREFIX + SAMPLE_API_GATEWAY_URL + BASE_PATH, FINAL_STAGE);
        assertThat(serverInfo.serverAddress(), is(equalTo(SAMPLE_API_GATEWAY_URL)));
    }
    
    @Test
    public void completeServerUrl_HttpsServerUrlAndStageWithSlash_workingServerUrl() {
        ServerInfo serverInfo = new ServerInfo(HTTPS_PREFIX + SAMPLE_API_GATEWAY_URL + BASE_PATH,
                                               ServerInfo.PATH_SEPARATOR + FINAL_STAGE);
        assertThat(serverInfo.serverAddress(), is(equalTo(SAMPLE_API_GATEWAY_URL)));
    }
    

    @Test
    public void completeServerUrl_HttpServerUrlAndStage_workingServerUrl() {
        ServerInfo serverInfo = new ServerInfo(HTTP_PREFIX + SAMPLE_API_GATEWAY_URL + BASE_PATH, FINAL_STAGE);
        assertThat(serverInfo.serverAddress(), is(equalTo(SAMPLE_API_GATEWAY_URL)));
    }
    
    @Test
    public void getStage_ServerInfoObject_notNull() {
        ServerInfo serverInfo = new ServerInfo(HTTP_PREFIX + SAMPLE_API_GATEWAY_URL + BASE_PATH, FINAL_STAGE);
        assertThat(serverInfo.getStage(), is(not(equalTo(null))));
    }
}
