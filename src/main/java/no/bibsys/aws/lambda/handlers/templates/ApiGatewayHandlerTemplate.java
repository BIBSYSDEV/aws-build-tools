package no.bibsys.aws.lambda.handlers.templates;

import com.amazonaws.services.apigateway.model.UnauthorizedException;
import com.amazonaws.services.lambda.runtime.Context;
import no.bibsys.aws.lambda.responses.GatewayResponse;
import org.apache.http.HttpStatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

/**
 * Template class for implementing Lambda function handlers that get activated through a call to ApiGateway. This class
 * is for processing a HTTP query directly without the usage of a Jersey-server or a SpringBoot template.
 *
 * @param <I> Class of the object in the body field of the ApiGateway message.
 * @param <O> Class of the response object.
 * @see <a href="https://github.com/awslabs/aws-serverless-java-container">The aws-serverless-container </a> for
 *         alternative solutions.
 */
public abstract class ApiGatewayHandlerTemplate<I, O> extends HandlerTemplate<I, O> {
    
    private final transient ApiMessageParser<I> inputParser = new ApiMessageParser<>();
    
    public ApiGatewayHandlerTemplate(Class<I> iclass) {
        super(iclass);
    }
    
    /**
     * Method for parsing the input object from the ApiGateway message.
     *
     * @param inputString the ApiGateway message
     * @return an object of class I
     * @throws IOException when parsing fails
     */
    @Override
    protected I parseInput(String inputString) throws IOException {
        I input = inputParser.getBodyElementFromJson(inputString, getIClass());
        return input;
    }
    
    /**
     * Maps an object I to an object O.
     *
     * @param input the input object of class I
     * @param apiGatewayInputString The message of apiGateway, for extracting the headers and in case we need
     *         other fields during the processing
     * @param context the Context
     * @return an output object of class O
     * @throws IOException        when processing fails
     * @throws URISyntaxException when processing fails
     */
    @Override
    protected final O processInput(I input, String apiGatewayInputString, Context context) throws Exception {
        Map<String, String> headers = inputParser.getHeadersFromJson(apiGatewayInputString);
        return processInput(input, headers, context);
    }
    
    protected abstract O processInput(I input, Map<String, String> headers, Context context)
            throws IOException, URISyntaxException;
    
    /**
     * This is the message for the sucess case. Sends a JSON string containing the response that APIGateway will send to
     * the user.
     *
     * @param input the input object of class I
     * @param output the output object of class O
     * @throws IOException when serializing fails
     */
    @Override
    protected void writeOutput(I input, O output) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            String outputString = objectMapper.writeValueAsString(output);
            GatewayResponse gatewayResponse = new GatewayResponse(outputString);
            String responseJson = objectMapper.writeValueAsString(gatewayResponse);
            writer.write(responseJson);
        }
    }
    
    /**
     * Sends a message to ApiGateway and to the user, in case of failure.
     *
     * @param input the input object of class I
     * @param error the exception
     * @throws IOException when serializing fails
     */
    @Override
    protected void writeFailure(I input, Throwable error) throws IOException {
        if (error instanceof UnauthorizedException) {
            unauthorizedFailure(input, (UnauthorizedException) error);
        } else {
            unknownError(input, error);
        }
    }
    
    protected void writeFailure(I input, Throwable error, int statusCode, String message) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            String outputString = Optional.ofNullable(error.getMessage()).orElse(message);
            GatewayResponse gatewayResponse =
                    new GatewayResponse(outputString, GatewayResponse.defaultHeaders(), statusCode);
            gatewayResponse.setBody(outputString);
            String gateWayResponseJson = objectMapper.writeValueAsString(gatewayResponse);
            writer.write(gateWayResponseJson);
        }
    }
    
    private void unknownError(I input, Throwable error) throws IOException {
        writeFailure(input, error, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unknown error.Check logs");
    }
    
    protected void unauthorizedFailure(I input, UnauthorizedException unauthorizedException) throws IOException {
        writeFailure(input, unauthorizedException, HttpStatus.SC_UNAUTHORIZED, "Unauthorized");
    }
}
