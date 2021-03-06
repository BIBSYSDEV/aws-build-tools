package no.bibsys.aws.lambda.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.bibsys.aws.lambda.events.exceptions.UnsupportedEventException;
import no.bibsys.aws.tools.IoUtils;
import no.bibsys.aws.tools.JsonUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class DeployEventBuilderTest {
    
    private static final String ID_IN_RESOURCE_FILE = "aaaaaaaa-aaaa-aaaa-12341-123456789012";
    private static final String RANDOM_FIELD = "some";
    private static final String RANDOM_VALUE = "field";
    
    @Test
    public void create_CodePipelineEvent_CodePipelineEvent() throws IOException, UnsupportedEventException {
        String input = IoUtils.resourceAsString(Paths.get("events", "mock_codePipeline_event.json"));
        CodePipelineEvent event = (CodePipelineEvent) DeployEventBuilder.create(input);
        assertThat(event.getId(), is(equalTo(ID_IN_RESOURCE_FILE)));
    }
    
    @Test
    public void create_anotherEvent_CustomTriggeredDeployEvent() throws IOException, UnsupportedEventException {
        ObjectMapper parser = JsonUtils.newJsonParser();
        ObjectNode rootNode = parser.createObjectNode();
        rootNode.put(RANDOM_FIELD, RANDOM_VALUE);
        String json = parser.writeValueAsString(rootNode);
        DeployEvent event = DeployEventBuilder.create(json);
        assertThat(event instanceof CustomTriggeredDeployEvent, is(equalTo(true)));
    }
    
    @Test
    public void create_emptyEventString_CustomTriggeredDeployEvent() throws IOException, UnsupportedEventException {
        DeployEvent event = DeployEventBuilder.create(null);
        assertThat(event instanceof CustomTriggeredDeployEvent, is(equalTo(true)));
    }
    
}
