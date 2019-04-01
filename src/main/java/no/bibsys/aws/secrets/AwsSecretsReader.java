package no.bibsys.aws.secrets;

import com.amazonaws.regions.Region;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.bibsys.aws.tools.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Class for reading secrets from Amazon. It reads secrets from the AWS Secrets Manager and not encrypted parameters
 * from AWS SSM (System Manager).
 */
public class AwsSecretsReader implements SecretsReader {
    
    public static final String SECRET_NOT_FOUND_ERROR_MESSAGE = "Could not find secret with name %s and key %s";
    private static final String LOG_ERROR_READING_SECRET =
        "Error while trying to read secret with name {} and with key {}";
    
    private static final Logger logger = LoggerFactory.getLogger(AwsSecretsReader.class);
    public static final String NON_EXISTING_SECRET_KEY_ERROR_MESSAGE =
        "Secret name %s exists, but secretKey %s does not.";
    private final transient AWSSecretsManager client;
    private final transient String secretName;
    private final transient String secretKey;
    
    public AwsSecretsReader(AWSSecretsManager client, String secretName, String secretKey) {
        this.client = client;
        this.secretKey = secretKey;
        this.secretName = secretName;
    }
    
    public AwsSecretsReader(String secretName, String secretKey, Region region) {
        this(AWSSecretsManagerClientBuilder.standard().withRegion(region.toString()).build(), secretName, secretKey);
    }
    
    @Override
    public String readSecret() throws IOException {
        
        Optional<GetSecretValueResult> getSecretValueResult = readSecretsForName();
        
        if (getSecretValueResult.map(GetSecretValueResult::getSecretString).isPresent()) {
            String secret = getSecretValueResult.get().getSecretString();
            return readValuesForKeyFromJson(secret).stream().findFirst().orElseThrow(
                () -> new ResourceNotFoundException(
                    String.format(NON_EXISTING_SECRET_KEY_ERROR_MESSAGE, secretName, secretKey)));
        } else {
            logger.error(LOG_ERROR_READING_SECRET, secretName, secretKey);
            throw new ResourceNotFoundException(String.format(SECRET_NOT_FOUND_ERROR_MESSAGE, secretName, secretKey));
        }
    }
    
    private List<String> readValuesForKeyFromJson(String secret) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        return mapper.readTree(secret).findValuesAsText(secretKey);
    }
    
    private Optional<GetSecretValueResult> readSecretsForName() {
        try {
            GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
            return Optional.ofNullable(client.getSecretValue(getSecretValueRequest));
        } catch (Exception e) {
            logger.error(LOG_ERROR_READING_SECRET, secretName, secretKey);
            throw e;
        }
    }
}
