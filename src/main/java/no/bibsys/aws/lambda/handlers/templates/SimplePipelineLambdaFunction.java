package no.bibsys.aws.lambda.handlers.templates;

import com.amazonaws.services.lambda.runtime.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimplePipelineLambdaFunction extends CodePipelineFunctionHandlerTemplate {
    
    private static final Logger logger = LoggerFactory.getLogger(SimplePipelineLambdaFunction.class);
    private static final String PROCESS_INPUT_MESSAGE = "Call to processInput method";
    private static final String LAMBDA_FUNCTION_OUTPUT = "this is the output";
    
    public SimplePipelineLambdaFunction(CodePipelineCommunicator codePipelineCommunicator) {
        super(codePipelineCommunicator);
    }
    
    @Override
    protected Object processInput(Object inputObject, String apiGatewayQuery, Context context) {
        logger.debug(PROCESS_INPUT_MESSAGE);
        return LAMBDA_FUNCTION_OUTPUT;
    }
}
