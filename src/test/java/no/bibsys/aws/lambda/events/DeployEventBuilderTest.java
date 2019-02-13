package no.bibsys.aws.lambda.events;

import no.bibsys.aws.tools.IoUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class DeployEventBuilderTest {

    @Test
    public void create_CodePipelineEvent_CodePipelineEvent() throws IOException {
        String input = IoUtils.resourceAsString(Paths.get("events", "mock_codePipeline_event.json"));
        CodePipelineEvent event = (CodePipelineEvent) DeployEventBuilder.create(input);
        assertThat(event.getId(), is(equalTo("aaaaaaaa-aaaa-aaaa-12341-123456789012")));
    }
}
