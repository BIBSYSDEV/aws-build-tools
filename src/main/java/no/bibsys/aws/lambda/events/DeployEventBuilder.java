package no.bibsys.aws.lambda.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.bibsys.aws.lambda.events.exceptions.UnsupportedEventException;
import no.bibsys.aws.tools.JsonUtils;

import java.io.IOException;
import java.util.Objects;

public final class DeployEventBuilder {
    
    private static final String EMPTY_EVENT_STRING = "Event string was empty";
    private static final String CODE_PIPELINE_JOB_FIELD = "CodePipeline.job";
    private static final String ID_FIELD = "id";
    public static final String UNSUPPORTED_EVENT_MESSAGE =
            "Unsupported event. Only CodePipeline events are supported now";
    
    private DeployEventBuilder() {
    }
    
    public static DeployEvent create(String eventJsonString) throws IOException, UnsupportedEventException {
        if (Objects.nonNull(eventJsonString) && !eventJsonString.isEmpty()) {
            return readEventFromString(eventJsonString);
        } else {
            throw new UnsupportedEventException(EMPTY_EVENT_STRING);
        }
    }
    
    private static DeployEvent readEventFromString(String eventJsonString)
            throws IOException, UnsupportedEventException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        JsonNode root = mapper.readTree(eventJsonString);
        if (isCodePipelineEvent(root)) {
            String id = root.get(CODE_PIPELINE_JOB_FIELD).get(ID_FIELD).asText();
            return new CodePipelineEvent(id);
        } else {
            throw new UnsupportedEventException(UNSUPPORTED_EVENT_MESSAGE);
        }
    }

    private static boolean isCodePipelineEvent(JsonNode root) {
        return root.has(CODE_PIPELINE_JOB_FIELD) && root.get(CODE_PIPELINE_JOB_FIELD).has(ID_FIELD);
    }
}
