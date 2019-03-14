package no.bibsys.aws.lambda.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.bibsys.aws.tools.JsonUtils;

import java.io.IOException;
import java.util.Objects;

public final class DeployEventBuilder {
    
    private static final String CODE_PIPELINE_JOB_FIELD = "CodePipeline.job";
    private static final String ID_FIELD = "id";
    
    private DeployEventBuilder() {
    }
    
    public static DeployEvent create(String eventJsonString) throws IOException {
        if (Objects.nonNull(eventJsonString) && !eventJsonString.isEmpty()) {
            return readEventFromString(eventJsonString);
        } else {
            return new CustomTriggeredDeployEvent();
        }
    }
    
    private static DeployEvent readEventFromString(String eventJsonString) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        JsonNode root = mapper.readTree(eventJsonString);
        if (isCodePipelineEvent(root)) {
            String id = root.get(CODE_PIPELINE_JOB_FIELD).get(ID_FIELD).asText();
            return new CodePipelineEvent(id);
        } else {
            return new CustomTriggeredDeployEvent();
        }
    }
    
    private static boolean isCodePipelineEvent(JsonNode root) {
        return root.has(CODE_PIPELINE_JOB_FIELD) && root.get(CODE_PIPELINE_JOB_FIELD).has(ID_FIELD);
    }
}
