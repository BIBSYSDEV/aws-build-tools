package no.bibsys.aws.lambda.handlers.templates;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.bibsys.aws.lambda.events.exceptions.UnsupportedEventException;
import no.bibsys.aws.tools.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

/**
 * Template for making it easier to use a POJO Lambda handler. The Amazon template RequestHandler does not behave well
 * with ApiGateway.
 *
 * <p>
 * Each class extending the HandlerTemplate should implement the following methods:
 * </p>
 *
 * <p>
 * <ul>
 * <li>Method {@code parseInput} parses an {@code InputStream} into an object of class {@code <I>}</li>.
 * <li>Method {@code processInput} processes an {@code  <I>} into class {@code <O>}</li>.
 * <li>Method {@code writeOutput} writes a success message in the {@code OutputStream}</li>.
 * <li>Method {@code writeFailure} writes a failure message in the {@code OutputStream}</li>.
 * </ul>
 *
 *
 * </p>
 *
 * @param <I> Input class
 * @param <O> Output class
 */
public abstract class HandlerTemplate<I, O> implements RequestStreamHandler {
    
    protected final transient ObjectMapper objectMapper = new ObjectMapper();
    private final transient Class<I> iclass;
    private transient LambdaLogger logger;
    protected transient OutputStream outputStream;
    
    public HandlerTemplate(Class<I> iclass) {
        this.iclass = iclass;
    }
    
    private void init(OutputStream outputStream, Context context) {
        this.outputStream = outputStream;
        
        this.logger = context.getLogger();
    }
    
    protected abstract I parseInput(String inputRequest) throws IOException, UnsupportedEventException;
    
    /**
     * A  function that maps the triple (inputObject, inputRequest,context)  to an output object. The input object of
     * class {@code I} is the output of the abstract method {@link HandlerTemplate#parseInput).
     * <p>
     * The input request is the actual unparsed inputRequest. In the general case the inputObject may contain a subset
     * of the inputRequest information. That is the reason we include the unparsed inputRequest.
     * </p>
     *
     * @param inputObject The object that results from parsing the inputRequest with {@link
     *     HandlerTemplate#parseInput}
     * @param inputRequest The unparsed input request
     * @param context the query context
     * @return the output that is the response of the Lambda function.
     * @throws IOException
     * @throws URISyntaxException
     */
    protected abstract O processInput(I inputObject, String inputRequest, Context context)
            throws IOException, URISyntaxException;
    
    protected abstract void writeOutput(I input, O output) throws IOException;
    
    protected abstract void writeFailure(I input, Throwable exception) throws IOException;
    
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        init(output, context);
        I inputObject = null;
        String inputString = IoUtils.streamToString(input);
        try {
            inputObject = parseInput(inputString);
            O response;
            response = processInput(inputObject, inputString, context);
            writeOutput(inputObject, response);
        } catch (Exception e) {
            logger.log(e.getMessage());
            writeFailure(inputObject, e);
        }
    }
    
    protected Class<I> getIClass() {
        return iclass;
    }
}
