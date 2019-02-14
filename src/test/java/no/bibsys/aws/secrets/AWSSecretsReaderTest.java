package no.bibsys.aws.secrets;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.bibsys.aws.tools.JsonUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AWSSecretsReaderTest {
    
    private static final String SECRET_KEY = "github";
    private static final String SECRET_NAME = "github";
    private static final String SECRET_VALUE = "secretValue";
    private final SecretsReader secretsReaderFindsValue;
    
    public AWSSecretsReaderTest() throws JsonProcessingException {
        ObjectMapper jsonParser = JsonUtils.newJsonParser();
        ObjectNode root = jsonParser.createObjectNode();
        root.put(SECRET_KEY, SECRET_VALUE);
        String keyJson = jsonParser.writeValueAsString(root);
        
        AWSSecretsManager secretsManagerWithValue = Mockito.mock(AWSSecretsManager.class);
        when(secretsManagerWithValue.getSecretValue(any()))
                .thenReturn(new GetSecretValueResult().withSecretString(keyJson));
        
        this.secretsReaderFindsValue = new AWSSecretsReader(secretsManagerWithValue, SECRET_NAME, SECRET_KEY);
    }
    
    @Test
    public void readSecret_existingSecret_secretValue() throws IOException {
        String value = secretsReaderFindsValue.readSecret();
        assertThat(value, is(equalTo(SECRET_VALUE)));
    }
    
    @Test
    public void readSecret_nonExistingSecrte_excetption() throws IOException {
        String value = secretsReaderFindsValue.readSecret();
        assertThat(value, is(equalTo(SECRET_VALUE)));
    }
}
