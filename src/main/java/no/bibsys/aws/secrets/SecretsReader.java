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
import java.util.Optional;

public class SecretsReader {

    /**
     * Class for reading secrets from Amazon. It reads secrets from the AWS Secrets Manager and not encrypted parameters
     * from AWS SSM (System Manager).
     */

    private final transient AWSSecretsManager client;
    private final transient String secretName;
    private final transient String secretKey;


    public SecretsReader(AWSSecretsManager client, String secretName, String secretKey) {
        this.client = client;
        this.secretKey = secretKey;
        this.secretName = secretName;
    }
    
    public SecretsReader(String secretName, String secretKey, Region region) {
        this(AWSSecretsManagerClientBuilder.standard().withRegion(region.toString()).build(), secretName,
            secretKey);
    }

    public String readSecret() throws IOException {
        ObjectMapper mapper = JsonUtils.newJsonParser();
        Optional<GetSecretValueResult> getSecretValueResult = readSecretsForName();

        if (getSecretValueResult.map(result -> result.getSecretString()).isPresent()) {
            String secret = getSecretValueResult.get().getSecretString();
            String value = mapper.readTree(secret).findValuesAsText(secretKey).stream().findFirst().orElse(null);
            return value;
    
        }
        return null;
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
