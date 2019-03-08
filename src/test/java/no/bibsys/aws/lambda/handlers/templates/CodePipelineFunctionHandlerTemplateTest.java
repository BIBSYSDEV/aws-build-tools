package no.bibsys.aws.lambda.handlers.templates;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.StringInputStream;
import no.bibsys.aws.lambda.events.CodePipelineEvent;
import no.bibsys.aws.lambda.events.DeployEvent;
import no.bibsys.aws.lambda.handlers.LocalTest;
import no.bibsys.aws.tools.IoUtils;
import no.bibsys.aws.tools.MockContext;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class CodePipelineFunctionHandlerTemplateTest extends LocalTest {
    
    private static final String MOCK_CODEPIPELINE_EVENT = "mock_codePipeline_event.json";
    private static final String RESOURCE_FOLDER = "events";
    private static final String ID_VALUE_IN_TEST_FILE = "\"aaaaaaaa-aaaa-aaaa-12341-123456789012\"";
    private static final String EXCEPTION_MESSAGE = "A test exception";
    private static final String NO_PIPELINE_EVENT = "{\"hello\":\"world\"}";
    private static final String CUSTOM_TRIGGERED_PIPELINE_EVENT_SUCCESS = "CustomTriggeredPipelineEvent success";
    
    
    private final CodePipelineFunctionHandlerTemplate<String> successTemplate;
    private final CodePipelineFunctionHandlerTemplate<String> failureTemplate;
    
    public CodePipelineFunctionHandlerTemplateTest() throws IOException {
        successTemplate = new CodePipelineFunctionHandlerTemplate<String>(new MockCodePipelineCommunicator()) {
            @Override
            protected String processInput(DeployEvent inputObject, String apiGatewayQuery, Context context) {
                if (inputObject instanceof CodePipelineEvent)
                    return ((CodePipelineEvent) inputObject).getId();
                else
                    return CUSTOM_TRIGGERED_PIPELINE_EVENT_SUCCESS;
            }
        };
        
        failureTemplate = new CodePipelineFunctionHandlerTemplate<String>(new MockCodePipelineCommunicator()) {
            @Override
            protected String processInput(DeployEvent inputObject, String apiGatewayQuery, Context context)
                    throws IOException {
                throw new IOException(EXCEPTION_MESSAGE);
            }
        };
    }
    
    @Test
    public void handler_jsonCodePipelineEvent_readEventIdInsideTheFunction() throws IOException {
        String event = IoUtils.resourceAsString(Paths.get(RESOURCE_FOLDER, MOCK_CODEPIPELINE_EVENT));
        StringInputStream inputStream = new StringInputStream(event);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        successTemplate.handleRequest(inputStream, outputStream, new MockContext());
        String output = outputStream.toString(StandardCharsets.UTF_8.toString());
        assertThat(output, is(equalTo(ID_VALUE_IN_TEST_FILE)));
    }
    
    @Test
    public void handler_jsonCodePipelineEventFailureTemplate_reportFailure() throws IOException {
        String event = IoUtils.resourceAsString(Paths.get(RESOURCE_FOLDER, MOCK_CODEPIPELINE_EVENT));
        StringInputStream inputStream = new StringInputStream(event);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        failureTemplate.handleRequest(inputStream, outputStream, new MockContext());
        String output = outputStream.toString(StandardCharsets.UTF_8.toString());
        assertThat(output, is(equalTo(EXCEPTION_MESSAGE)));
    }
    
    @Test
    public void handler_notCodePipelineEventSuccessTemplate_noFailure() throws IOException {
        StringInputStream inputStream = new StringInputStream(NO_PIPELINE_EVENT);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        successTemplate.handleRequest(inputStream, outputStream, new MockContext());
        String output = outputStream.toString(StandardCharsets.UTF_8.toString());
        assertThat(output, containsString(CUSTOM_TRIGGERED_PIPELINE_EVENT_SUCCESS));
    }
    
    @Test
    public void handler_emptyPipelineEventSuccessTemplate_noFailure() throws IOException {
        StringInputStream inputStream = new StringInputStream("");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        successTemplate.handleRequest(inputStream, outputStream, new MockContext());
        String output = outputStream.toString(StandardCharsets.UTF_8.toString());
        assertThat(output, containsString(CUSTOM_TRIGGERED_PIPELINE_EVENT_SUCCESS));
    }
}
