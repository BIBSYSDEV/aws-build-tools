package no.bibsys.aws.tools;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;

public final class JsonUtils {
    
    public static final ObjectMapper jsonParser = createJsonParser();
    public static final ObjectMapper yamlParser = createYamlParser();
    
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
}
