package no.bibsys.aws.tools;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public final class JsonUtils {
    
    public static final ObjectMapper jsonParser = createJsonParser();
    public static final ObjectMapper yamlParser = createYamlParser();
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final String JSON_PARSING_ERROR = "Json parsing failed. Trying yaml for string: {}";
    private static final String YAML_PARSING_ERROR = "Could not parse yaml string: {}";
    
    private JsonUtils() {
    }
    
    public static String yamlToJson(String yaml) throws IOException {
        JsonNode root = yamlParser.readTree(yaml);
        return jsonParser.writeValueAsString(root);
    }
    
    /**
     * Use the field directly. It is thread safe.
     *
     * @return the public static field {@code jsonParser}
     */
    @Deprecated
    public static ObjectMapper newJsonParser() {
        return jsonParser;
    }
    
    /**
     * Use the field directly. It is thread safe.
     *
     * @return the public static field {@code yamlParser}
     */
    @Deprecated
    public static ObjectMapper newYamlParser() {
        return yamlParser;
    }
    
    public static String jsonToYaml(String yaml) throws IOException {
        JsonNode root = jsonParser.readTree(yaml);
        return yamlParser.writeValueAsString(root);
    }
    
    public static String removeComments(String jsonWithComments) throws IOException {
        JsonNode jsonNode = jsonParser.readTree(jsonWithComments);
        return jsonParser.writeValueAsString(jsonNode);
    }
    
    private static ObjectMapper createYamlParser() {
        YAMLFactory factory = new YAMLFactory();
    
        return new ObjectMapper(factory);
    }
    
    private static ObjectMapper createJsonParser() {
        JsonFactory jsonFactory =
            new JsonFactory().configure(Feature.ALLOW_COMMENTS, true).configure(Feature.ALLOW_YAML_COMMENTS, true)
                             .configure(Feature.ALLOW_SINGLE_QUOTES, true);
        return new ObjectMapper(jsonFactory);
    }
    
    public static ObjectNode parseJsonOrYaml(String document) throws IOException {
        Optional<ObjectNode> json = Optional.ofNullable(readJson(document));
        if (!json.isPresent()) {
            json = Optional.ofNullable(readYaml(document));
        }
        
        return json.orElseThrow(() -> new IOException(String.format("Not a valid json or yaml %s", document)));
    }
    
    private static ObjectNode readJson(String document) {
        try {
            return (ObjectNode) jsonParser.readTree(document);
        } catch (IOException e) {
            logger.error(JSON_PARSING_ERROR, document);
            logger.error(e.getMessage());
            return null;
        }
    }
    
    private static ObjectNode readYaml(String document) {
        try {
            return (ObjectNode) yamlParser.readTree(document);
        } catch (IOException e) {
            logger.error(YAML_PARSING_ERROR, document);
            logger.error(e.getMessage());
            return null;
        }
    }
}
