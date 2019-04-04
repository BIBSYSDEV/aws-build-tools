package no.bibsys.aws.lambda.handlers.templates;

import no.bibsys.aws.lambda.handlers.LocalTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

class SimplePipelineLambdaFunctionTest {
    
    private transient SimplePipelineLambdaFunction function;
    
    @BeforeEach
    public void init() {
        CodePipelineCommunicator communicator = new LocalTest.MockCodePipelineCommunicator();
        function = new SimplePipelineLambdaFunction(communicator);
    }
    
    @Test
    public void processInputShouldReturnAnNonNullOutput() {
        String inputObject = "sample";
        Object output = function.processInput(inputObject, inputObject, null);
        assertThat(output, is(not(equalTo(null))));
    }
}
