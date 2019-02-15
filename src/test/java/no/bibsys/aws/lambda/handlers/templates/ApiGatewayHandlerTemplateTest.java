package no.bibsys.aws.lambda.handlers.templates;

import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.bibsys.aws.lambda.handlers.LocalTest;
import no.bibsys.aws.lambda.handlers.SampleClass;
import no.bibsys.aws.tools.JsonUtils;
import no.bibsys.aws.tools.MockContext;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

public class ApiGatewayHandlerTemplateTest extends LocalTest {
    
    private static final String STATUS_CODE = "statusCode";
    private static final String UNAUTHORIZED_EXCEPTION_MESSAGE = "Unauthorized";
    private static final String IO_EXCEPTION_EXCEPTION_MESSAGE = "IOException";
    private static final String BODY_FIELD = "body";
    private final String apiGatewayMessage;
    private ObjectMapper parser = JsonUtils.newJsonParser();
    
    private ApiGatewayHandlerTemplate<SampleClass, SampleClass> template =
            new ApiGatewayHandlerTemplate<SampleClass, SampleClass>(SampleClass.class) {
                @Override
                protected SampleClass processInput(SampleClass input, Map<String, String> headers, Context context) {
                    return input;
                }
            };
    
    private ApiGatewayHandlerTemplate<SampleClass, SampleClass> unauthorizedTemplate =
            new ApiGatewayHandlerTemplate<SampleClass, SampleClass>(SampleClass.class) {
                @Override
                protected SampleClass processInput(SampleClass input, Map<String, String> headers, Context context) {
                    throw new UnauthorizedException(UNAUTHORIZED_EXCEPTION_MESSAGE);
                }
            };
    
    private ApiGatewayHandlerTemplate<SampleClass, SampleClass> unknownErrorTemplate =
            new ApiGatewayHandlerTemplate<SampleClass, SampleClass>(SampleClass.class) {
                @Override
                protected SampleClass processInput(SampleClass input, Map<String, String> headers, Context context)
                        throws IOException {
                    throw new IOException(IO_EXCEPTION_EXCEPTION_MESSAGE);
                }
            };
    
    public ApiGatewayHandlerTemplateTest() throws IOException {
        super();
        apiGatewayMessage = apiGatewayMessageWithObjectAsBody(SampleClass.create());
    }
    
    @Test
    public void parseInput_inputString_inputObject() throws IOException {
        SampleClass actual = template.parseInput(apiGatewayMessage);
        SampleClass expected = SampleClass.create();
        assertThat(actual, is(equalTo(expected)));
        
    }
    
    @Test
    public void processInput_input_intactInput() throws IOException {
        InputStream in = new StringInputStream(apiGatewayMessage);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        template.handleRequest(in, outputStream, new MockContext());
        String output = outputStream.toString(StandardCharsets.UTF_8.name());
        
        ObjectNode responseNode = (ObjectNode) parser.readTree(output);
        String bodyString = responseNode.get(BODY_FIELD).textValue();
        SampleClass actual = parser.readValue(bodyString, SampleClass.class);
        assertThat(actual, is(equalTo(SampleClass.create())));
    }
    
    @Test
    public void parseInput_inputStringUnauthorized_inputObject() throws IOException {
        InputStream in = new StringInputStream(apiGatewayMessage);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        unauthorizedTemplate.handleRequest(in, outputStream, new MockContext());
        String output = outputStream.toString(StandardCharsets.UTF_8.name());
        ObjectNode response = parser.readValue(output, ObjectNode.class);
        assertThat(response.get(STATUS_CODE), is(not(equalTo(HttpStatus.SC_UNAUTHORIZED))));
    }
    
    @Test
    public void parseInput_inputStringUnknownError_inputObject() throws IOException {
        InputStream in = new StringInputStream(apiGatewayMessage);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        unknownErrorTemplate.handleRequest(in, outputStream, new MockContext());
        String output = outputStream.toString(StandardCharsets.UTF_8.name());
        ObjectNode response = parser.readValue(output, ObjectNode.class);
        assertThat(response.get(STATUS_CODE), is(not(equalTo(HttpStatus.SC_INTERNAL_SERVER_ERROR))));
    }
    
}
