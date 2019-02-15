package no.bibsys.aws.secrets;

import com.amazonaws.regions.Region;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.bibsys.aws.tools.JsonUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AWSSecretsReader implements SecretsReader {
    
    /**
     * Class for reading secrets from Amazon. It reads secrets from the AWS Secrets Manager and not encrypted parameters
     * from AWS SSM (System Manager).
     */
    
    private final transient AWSSecretsManager client;
    private final transient String secretName;
    private final transient String secretKey;
    
    public AWSSecretsReader(AWSSecretsManager client, String secretName, String secretKey) {
        this.client = client;
        this.secretKey = secretKey;
        this.secretName = secretName;
    }
    
    public AWSSecretsReader(String secretName, String secretKey, Region region) {
        this(AWSSecretsManagerClientBuilder.standard().withRegion(region.toString()).build(), secretName, secretKey);
    }
    
    @Override
    public String readSecret() throws IOException {
    
        Optional<GetSecretValueResult> getSecretValueResult = readSecretsForName();
    
        if (getSecretValueResult.map(GetSecretValueResult::getSecretString).isPresent()) {
            String secret = getSecretValueResult.get().getSecretString();
            return readValuesForKeyFromJson(secret).stream().findFirst().orElse(null);
        }
        return null;
    }
    
    private List<String> readValuesForKeyFromJson(String secret) throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        return mapper.readTree(secret).findValuesAsText(secretKey);
    }
    
    private Optional<GetSecretValueResult> readSecretsForName() {
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
        Optional<GetSecretValueResult> getSecretValueResult = Optional.empty();
        try {
            getSecretValueResult = Optional.ofNullable(client.getSecretValue(getSecretValueRequest));
        } catch (InvalidRequestException e) {
            getSecretValueResult = Optional.empty();
        }
        return getSecretValueResult;
    }
}
