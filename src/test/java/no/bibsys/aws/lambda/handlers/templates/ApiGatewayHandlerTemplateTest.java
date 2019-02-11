package no.bibsys.aws.lambda.handlers.templates;

import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.bibsys.aws.lambda.handlers.InputClass;
import no.bibsys.aws.lambda.handlers.LocalTest;
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
    private final String apiGatewayMessage;
    private ObjectMapper parser = JsonUtils.newJsonParser();

    private ApiGatewayHandlerTemplate<InputClass, InputClass> template =
            new ApiGatewayHandlerTemplate<InputClass, InputClass>(InputClass.class) {
                @Override
                protected InputClass processInput(InputClass input, Map<String, String> headers, Context context) {
                    return input;

                }
            };

    private ApiGatewayHandlerTemplate<InputClass, InputClass> unauthorizedTemplate =
            new ApiGatewayHandlerTemplate<InputClass, InputClass>(InputClass.class) {
                @Override
                protected InputClass processInput(InputClass input, Map<String, String> headers, Context context) {
                    throw new UnauthorizedException("Unauthorized");
                }
            };

    private ApiGatewayHandlerTemplate<InputClass, InputClass> unknownErrorTemplate =
            new ApiGatewayHandlerTemplate<InputClass, InputClass>(InputClass.class) {
                @Override
                protected InputClass processInput(InputClass input, Map<String, String> headers, Context context)
                        throws IOException {
                    throw new IOException("IOException");
                }
            };

    public ApiGatewayHandlerTemplateTest() throws JsonProcessingException {
        apiGatewayMessage = apiGatewayMessageWithObjectAsBody(InputClass.create());
    }

    @Test
    public void parseInput_inputString_inputObject() throws IOException {
        InputClass actual = (InputClass) template.parseInput(apiGatewayMessage);
        InputClass expected = InputClass.create();
        assertThat(actual, is(equalTo(expected)));

    }

    @Test
    public void processInput_input_intactInput() throws IOException {
        InputStream in = new StringInputStream(apiGatewayMessage);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        template.handleRequest(in, outputStream, new MockContext());
        String output = outputStream.toString(StandardCharsets.UTF_8.name());

        ObjectNode responseNode = (ObjectNode) parser.readTree(output);
        String bodyString = responseNode.get("body").textValue();
        InputClass actual = parser.readValue(bodyString, InputClass.class);
        assertThat(actual, is(equalTo(InputClass.create())));
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
