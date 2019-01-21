package no.bibsys.aws.lambda.events;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.nio.file.Paths;
import no.bibsys.aws.tools.IoUtils;
import org.junit.jupiter.api.Test;

public class DeployEventBuilderTest {


    @Test
    public void create_CodePipelineEvent_CodePipelineEvent() throws IOException {
        String input = IoUtils.resourceAsString(Paths.get("events", "mock_codePipeline_event.json"));
        CodePipelineEvent event = (CodePipelineEvent) DeployEventBuilder.create(input);
        assertThat(event.getId(), is(equalTo("a0a4b321-beb6-4da6")));
    }

}
