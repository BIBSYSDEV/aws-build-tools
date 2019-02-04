package no.bibsys.aws.apigateway;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.Test;

public class ServerInfoTest {

    private static final String BASE_PATH = "{basePath}";
    private static final String HTTPS_SAMPLE_API_GATEWAY_URL =
        "https://xxxxxxxx.execute-api.eu-west-1.amazonaws.com/" + BASE_PATH;
    private static final String HTTP_SAMPLE_API_GATEWAY_URL =
        "http://xxxxxxxx.execute-api.eu-west-1.amazonaws.com/" + BASE_PATH;
    private static final String FINAL_STAGE = "final";

    @Test
    public void completeServerUrl_HttpsServerUrlAndStage_workingServerUrl() {
        ServerInfo serverInfo = new ServerInfo(HTTPS_SAMPLE_API_GATEWAY_URL + BASE_PATH, FINAL_STAGE);
        assertThat(serverInfo.serverAddress(), is(equalTo(HTTPS_SAMPLE_API_GATEWAY_URL)));
    }

    @Test
    public void completeServerUrl_HttpServerUrlAndStage_workingServerUrl() {
        ServerInfo serverInfo = new ServerInfo(HTTP_SAMPLE_API_GATEWAY_URL + BASE_PATH, FINAL_STAGE);
        assertThat(serverInfo.serverAddress(), is(equalTo(HTTP_SAMPLE_API_GATEWAY_URL)));
    }
}
