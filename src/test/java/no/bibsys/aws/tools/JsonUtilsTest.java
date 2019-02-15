package no.bibsys.aws.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class JsonUtilsTest {
    
    private static final String OPENAPIRESOURCES = "openapi";
    private static final String JSON_RESOURCES = "jsonutils";
    private static final String YAML_FILE = "openapi.yml";
    private static final String JSON_WITH_COMMENTS = "json_with_comments.json";
    private final String yaml;
    private final String jsonWithComments;
    
    public JsonUtilsTest() throws IOException {
        yaml = IoUtils.resourceAsString(Paths.get(OPENAPIRESOURCES, YAML_FILE));
        jsonWithComments = IoUtils.resourceAsString(Paths.get(JSON_RESOURCES, JSON_WITH_COMMENTS));
        
    }
    
    @Test
    public void yamlToJson_yaml_json() throws IOException {
        String json = JsonUtils.yamlToJson(yaml);
        ObjectMapper parser = JsonUtils.newJsonParser();
        ObjectNode jsonRoot = parser.readValue(json, ObjectNode.class);
        ObjectNode yamlRoot = JsonUtils.newYamlParser().readValue(yaml, ObjectNode.class);
        assertThat(jsonRoot, is(equalTo(yamlRoot)));
        
    }
    
    @Test
    public void jsonToYaml_json_yaml() throws IOException {
        String json = JsonUtils.yamlToJson(yaml);
        
        ObjectMapper parser = JsonUtils.newYamlParser();
        String newYaml = JsonUtils.jsonToYaml(json);
        ObjectNode yamlRoot = parser.readValue(yaml, ObjectNode.class);
        ObjectNode newYamlRoot = parser.readValue(newYaml, ObjectNode.class);
        assertThat(newYamlRoot, is(equalTo(yamlRoot)));
        
    }
    
    @Test
    public void jsonWithComments_json_objectNode() throws IOException {
        ObjectMapper parser = JsonUtils.newJsonParser();
        ObjectNode root = parser.readValue(JsonUtils.removeComments(jsonWithComments), ObjectNode.class);
        
        assertThat(root.elements().hasNext(), is(equalTo(true)));
        
    }
    
}
