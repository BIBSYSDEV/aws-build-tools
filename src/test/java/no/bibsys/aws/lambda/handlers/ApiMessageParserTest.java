package no.bibsys.aws.lambda.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.bibsys.aws.lambda.handlers.templates.ApiMessageParser;
import no.bibsys.aws.tools.JsonUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class ApiMessageParserTest {

    private static final String HEADER = "header1";
    private static final String ANOTHER_HEADER = "header2";
    private static final String HEADER_VALUE = "Header1Value";
    private static final String ANOTHER_HEADER_VALUE = "Header2Value";
    private static final int NUMBER_OF_HEADERS = 2;
    private static final String BODY_STRING = "Some body data goes here";

    @Test
    public void getHeadersFromJson_headersFieldObject_headersMap() throws IOException {

        ApiMessageParser<InputClass> apiMessageParser = new ApiMessageParser<>();
        Map<String, String> headers = apiMessageParser.getHeadersFromJson(apiGatewayMessageWithObjectAsBody());
        assertThat(headers.size(), is(equalTo(NUMBER_OF_HEADERS)));
        assertThat(headers.get(HEADER), is(equalTo(HEADER_VALUE)));
        assertThat(headers.get(ANOTHER_HEADER), is(equalTo(ANOTHER_HEADER_VALUE)));
    }

    @Test
    public void getBodyElementFromJson_Stringbody_headersMap() throws IOException {
        ApiMessageParser<String> apiMessageParser = new ApiMessageParser<>();
        String body = apiMessageParser.getBodyElementFromJson(apiGatewayMessageWithStringAsBody(), String.class);
        assertThat(body, is(equalTo(BODY_STRING)));
    }

    @Test
    public void getBodyElementFromJson_Objectbody_headersMap() throws IOException {
        ApiMessageParser<InputClass> apiMessageParser = new ApiMessageParser<>();
        InputClass body =
                apiMessageParser.getBodyElementFromJson(apiGatewayMessageWithObjectAsBody(), InputClass.class);
        InputClass expected = inputObject();
        assertThat(body.getId(), is(equalTo(expected.getId())));
        assertThat(body.getRandomField(), is(equalTo(expected.getRandomField())));
    }

    private String apiGatewayMessageWithObjectAsBody() throws JsonProcessingException {
        InputClass body = inputObject();
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HEADER, HEADER_VALUE);
        headers.put(ANOTHER_HEADER, ANOTHER_HEADER_VALUE);
        return new ApiGatewayMessageMock(body, headers).toJsonString();
    }

    private String apiGatewayMessageWithStringAsBody() throws JsonProcessingException {

        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HEADER, HEADER_VALUE);
        headers.put(ANOTHER_HEADER, ANOTHER_HEADER_VALUE);
        return new ApiGatewayMessageMock(BODY_STRING, headers).toJsonString();
    }

    private InputClass inputObject() {
        return new InputClass("id", "randomField");
    }

    private class ApiGatewayMessageMock {

        private final Map<String, String> headers;
        private final Object body;

        public ApiGatewayMessageMock(Object body, Map<String, String> headers) {
            this.body = body;
            this.headers = headers;
        }

        private String toJsonString() throws JsonProcessingException {
            return JsonUtils.newJsonParser().writeValueAsString(this);
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public Object getBody() {
            return body;
        }
    }


    public static class InputClass {
        private String id;
        private String randomField;

        public InputClass() {
        }

        public InputClass(String id, String randomField) {
            this.id = id;
            this.randomField = randomField;
        }

        public String getId() {
            return id;
        }

        public String getRandomField() {
            return randomField;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setRandomField(String randomField) {
            this.randomField = randomField;
        }

    }

}
