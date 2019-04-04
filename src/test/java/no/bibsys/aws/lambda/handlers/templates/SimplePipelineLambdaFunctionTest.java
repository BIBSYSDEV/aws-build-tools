package no.bibsys.aws.lambda.handlers.templates;

import no.bibsys.aws.lambda.handlers.LocalTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

class SimplePipelineLambdaFunctionTest extends LocalTest {
    
    private final transient String inputObject = "sample";
    private transient SimplePipelineLambdaFunction function;
    
    public SimplePipelineLambdaFunctionTest() throws IOException {
        super();
    }
    
    @BeforeEach
    public void init() {
        CodePipelineCommunicator communicator = new MockCodePipelineCommunicator();
        function = new SimplePipelineLambdaFunction(communicator);
    }
    
    @Test
    public void processInputShouldReturnAnNonNullOutput() {
        Object output = function.processInput(inputObject, inputObject, null);
        assertThat(output, is(not(equalTo(null))));
    }
}
