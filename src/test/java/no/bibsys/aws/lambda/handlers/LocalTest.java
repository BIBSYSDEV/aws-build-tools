package no.bibsys.aws.lambda.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.bibsys.aws.lambda.events.CodePipelineEvent;
import no.bibsys.aws.lambda.handlers.templates.CodePipelineCommunicator;
import no.bibsys.aws.tools.JsonUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalTest {
    private static final String HEADER = "header1";
    private static final String ANOTHER_HEADER = "header2";
    private static final String HEADER_VALUE = "Header1Value";
    private static final String ANOTHER_HEADER_VALUE = "Header2Value";
    private static final String BODY_STRING = "Some body data goes here";
    private static final String BODY_FIELD = "body";

    protected String apiGatewayMessageWitNullBody() throws IOException {
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HEADER, HEADER_VALUE);
        headers.put(ANOTHER_HEADER, ANOTHER_HEADER_VALUE);
        ObjectMapper parser = JsonUtils.newJsonParser();
        ObjectNode node = (ObjectNode) parser.readTree(new ApiGatewayMessageMock(null, headers).toJsonString());
        node.remove(BODY_FIELD);
        return parser.writeValueAsString(node);
    }

    protected String apiGatewayMessageWithSerializedJsonBody() throws JsonProcessingException {
        SampleClass body = SampleClass.create();
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HEADER, HEADER_VALUE);
        headers.put(ANOTHER_HEADER, ANOTHER_HEADER_VALUE);
        return new ApiGatewayMessageMock(body.asJsonString(), headers).toJsonString();
    }
    
    protected String apiGatewayMessageWithObjectAsBody(SampleClass inputClass) throws JsonProcessingException {

        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HEADER, HEADER_VALUE);
        headers.put(ANOTHER_HEADER, ANOTHER_HEADER_VALUE);
        return new ApiGatewayMessageMock(inputClass, headers).toJsonString();
    }

    protected String apiGatewayMessageWithStringAsBody() throws JsonProcessingException {

        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(HEADER, HEADER_VALUE);
        headers.put(ANOTHER_HEADER, ANOTHER_HEADER_VALUE);
        return new ApiGatewayMessageMock(BODY_STRING, headers).toJsonString();
    }

    protected class ApiGatewayMessageMock {

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
    
    
    public static class MockCodePipelineCommunicator extends CodePipelineCommunicator {
        @Override
        public void sendSuccessToCodePipeline(CodePipelineEvent input, String outputString) {
        }
        
        @Override
        public void sendFailureToCodePipeline(CodePipelineEvent input, String outputString) {
        
        }
    }
}
