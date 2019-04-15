package no.bibsys.aws.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static no.bibsys.aws.tools.JsonUtils.jsonParser;
import static no.bibsys.aws.tools.JsonUtils.removeComments;
import static no.bibsys.aws.tools.JsonUtils.yamlParser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonUtilsTest {
    
    private static final String OPENAPIRESOURCES = "openapi";
    private static final String JSON_RESOURCES = "jsonutils";
    private static final String YAML_FILE = "openapi.yml";
    private static final String JSON_WITH_COMMENTS = "json_with_comments.json";
    private static final String EMPTY_DOCUMENT = "";
    private final String yaml;
    private final String jsonWithComments;
    
    public JsonUtilsTest() throws IOException {
        yaml = IoUtils.resourceAsString(Paths.get(OPENAPIRESOURCES, YAML_FILE));
        jsonWithComments = IoUtils.resourceAsString(Paths.get(JSON_RESOURCES, JSON_WITH_COMMENTS));
    }
    
    @Test
    public void yamlToJson_yaml_json() throws IOException {
        String json = JsonUtils.yamlToJson(yaml);
        ObjectNode jsonRoot = jsonParser.readValue(json, ObjectNode.class);
        ObjectNode yamlRoot = yamlParser.readValue(yaml, ObjectNode.class);
        assertThat(jsonRoot, is(equalTo(yamlRoot)));
    }
    
    @Test
    public void jsonToYaml_json_yaml() throws IOException {
        String json = JsonUtils.yamlToJson(yaml);
        ObjectMapper parser = yamlParser;
        String newYaml = JsonUtils.jsonToYaml(json);
        ObjectNode yamlRoot = parser.readValue(yaml, ObjectNode.class);
        ObjectNode newYamlRoot = parser.readValue(newYaml, ObjectNode.class);
        assertThat(newYamlRoot, is(equalTo(yamlRoot)));
    }
    
    @Test
    public void parseYamlOrJsonShouldParseaValidYamlFile() throws IOException {
        ObjectNode expectedYaml = (ObjectNode) yamlParser.readTree(yaml);
        assertThat(JsonUtils.parseJsonOrYaml(yaml), is(equalTo(expectedYaml)));
    }
    
    @Test
    public void parseYamlOrJsonShouldParseaValidJsonFile() throws IOException {
        ObjectNode expectedJson = (ObjectNode) jsonParser.readTree(jsonWithComments);
        assertThat(JsonUtils.parseJsonOrYaml(removeComments(jsonWithComments)), is(equalTo(expectedJson)));
    }
    
    @Test
    public void parseYamlOrJsonShouldThrowExceptionForEmptyJsonFile() {
        
        IOException exception = assertThrows(IOException.class, () -> JsonUtils.parseJsonOrYaml(EMPTY_DOCUMENT));
        assertThat(exception.getMessage(), is(not(emptyString())));
    }
    
    @Test
    public void parseYamlOrJsonShouldThrowExceptionForInvalidYamlFile() {
        
        IOException exception = assertThrows(IOException.class, () -> JsonUtils.parseJsonOrYaml(EMPTY_DOCUMENT));
        assertThat(exception.getMessage(), is(not(emptyString())));
    }
    
    @Test
    public void jsonWithComments_json_objectNode() throws IOException {
        ObjectNode root = jsonParser.readValue(JsonUtils.removeComments(jsonWithComments), ObjectNode.class);
        
        assertThat(root.elements().hasNext(), is(equalTo(true)));
    }
}
