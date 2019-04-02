package no.bibsys.aws.secrets;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
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
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AwsSecretsReaderTest {
    
    private static final String EXCEPTION_MESSAGE = "exception message";
    private static final String SECRET_KEY = "github";
    private static final String SECRET_NAME = "github";
    private static final String SECRET_VALUE = "secretValue";
    private static final String ANOTHER_KEY = "someKey";
    private static final String ARBITRARY_VALUE = "someValue";
    private final SecretsReader secretsReaderFindsValue;
    
    public AwsSecretsReaderTest() throws JsonProcessingException {
        ObjectMapper jsonParser = JsonUtils.newJsonParser();
        ObjectNode root = jsonParser.createObjectNode();
        root.put(SECRET_KEY, SECRET_VALUE);
        String keyJson = jsonParser.writeValueAsString(root);
        
        AWSSecretsManager secretsManagerWithValue = Mockito.mock(AWSSecretsManager.class);
        when(secretsManagerWithValue.getSecretValue(any()))
            .thenReturn(new GetSecretValueResult().withSecretString(keyJson));
        
        this.secretsReaderFindsValue = new AwsSecretsReader(secretsManagerWithValue, SECRET_NAME, SECRET_KEY);
    }
    
    @Test
    public void readSecret_existingSecret_secretValue() throws IOException {
        String value = secretsReaderFindsValue.readSecret();
        assertThat(value, is(equalTo(SECRET_VALUE)));
    }
    
    @Test
    public void readSecret_nonExistingSecret_exception() throws IOException {
    
        AWSSecretsManager secretsManagerWithoutValues = Mockito.mock(AWSSecretsManager.class);
        when(secretsManagerWithoutValues.getSecretValue(any()))
            .thenThrow(new ResourceNotFoundException(EXCEPTION_MESSAGE));
        AwsSecretsReader secretsReaderWithNoValue =
            new AwsSecretsReader(secretsManagerWithoutValues, SECRET_NAME, SECRET_KEY);
    
        ResourceNotFoundException thrown =
            assertThrows(ResourceNotFoundException.class, secretsReaderWithNoValue::readSecret);
        assertThat(thrown.getMessage(), containsString(EXCEPTION_MESSAGE));
    }
    
    @Test
    public void readSecret_SecretInInvalidStatus_exception() {
        
        AWSSecretsManager secretsManagerWithoutValues = Mockito.mock(AWSSecretsManager.class);
        when(secretsManagerWithoutValues.getSecretValue(any()))
            .thenThrow(new InvalidParameterException(EXCEPTION_MESSAGE));
        AwsSecretsReader secretsReaderWithNoValue =
            new AwsSecretsReader(secretsManagerWithoutValues, SECRET_NAME, SECRET_KEY);
        
        InvalidParameterException thrown =
            assertThrows(InvalidParameterException.class, secretsReaderWithNoValue::readSecret);
        assertThat(thrown.getMessage(), containsString(EXCEPTION_MESSAGE));
    }
    
    @Test
    public void readSecret_invalidRequest_exception() {
        
        AWSSecretsManager secretsManagerWithoutValues = Mockito.mock(AWSSecretsManager.class);
        when(secretsManagerWithoutValues.getSecretValue(any()))
            .thenThrow(new InvalidRequestException(EXCEPTION_MESSAGE));
        AwsSecretsReader secretsReaderWithNoValue =
            new AwsSecretsReader(secretsManagerWithoutValues, SECRET_NAME, SECRET_KEY);
        
        InvalidRequestException thrown =
            assertThrows(InvalidRequestException.class, secretsReaderWithNoValue::readSecret);
        assertThat(thrown.getMessage(), containsString(EXCEPTION_MESSAGE));
    }
    
    @Test
    public void readSecret_nullStringSecret_exception() {
        
        AWSSecretsManager secretsManagerWithoutValues = Mockito.mock(AWSSecretsManager.class);
        when(secretsManagerWithoutValues.getSecretValue(any()))
            .thenReturn(new GetSecretValueResult().withSecretString(null));
        AwsSecretsReader secretsReaderWithNoValue =
            new AwsSecretsReader(secretsManagerWithoutValues, SECRET_NAME, SECRET_KEY);
        ResourceNotFoundException thrown =
            assertThrows(ResourceNotFoundException.class, secretsReaderWithNoValue::readSecret);
        String errorMessage = String.format(AwsSecretsReader.SECRET_NOT_FOUND_ERROR_MESSAGE, SECRET_NAME, SECRET_KEY);
        assertThat(thrown.getMessage(), containsString(errorMessage));
    }
    
    @Test
    public void readSecret_secretStringWithoutTheCorrectKey_exception() throws JsonProcessingException {
        
        ObjectMapper mapper = JsonUtils.newJsonParser();
        AWSSecretsManager secretsManagerWithoutValues = Mockito.mock(AWSSecretsManager.class);
        ObjectNode secretStringNode = mapper.createObjectNode();
        secretStringNode.put(ANOTHER_KEY, ARBITRARY_VALUE);
        String secretString = mapper.writeValueAsString(secretStringNode);
        when(secretsManagerWithoutValues.getSecretValue(any()))
            .thenReturn(new GetSecretValueResult().withSecretString(secretString));
        AwsSecretsReader secretsReaderWithNoValue =
            new AwsSecretsReader(secretsManagerWithoutValues, SECRET_NAME, SECRET_KEY);
        ResourceNotFoundException thrown =
            assertThrows(ResourceNotFoundException.class, secretsReaderWithNoValue::readSecret);
        String errorMessage =
            String.format(AwsSecretsReader.NON_EXISTING_SECRET_KEY_ERROR_MESSAGE, SECRET_NAME, SECRET_KEY);
        assertThat(thrown.getMessage(), containsString(errorMessage));
    }
    
    
}
