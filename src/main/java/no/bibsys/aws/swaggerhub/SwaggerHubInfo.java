package no.bibsys.aws.swaggerhub;

import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;

import java.io.IOException;

public class SwaggerHubInfo {
    
    private final transient String apiId;
    private final transient String apiVersion;
    private final transient String swaggerOrganization;
    
    private final transient SecretsReader secretsReader;
    
    /**
     * SwaggerHub constructor without the use of {@link Environment}.
     *
     * @param apiId The id of the api
     * @param apiVersion The version of the API documentation. Can be {@code null} if the intended action is for
     *         the whole API
     * @param swaggerOrganization The SwaggerHub organization or account name
     */

    public SwaggerHubInfo(String apiId, String apiVersion, String swaggerOrganization, SecretsReader secretsReader) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.swaggerOrganization = swaggerOrganization;
        this.secretsReader = secretsReader;
    }
    
    public String getApiId() {
        return apiId;
    }
    
    public String getApiVersion() {
        return apiVersion;
    }
    
    public String getSwaggerOrganization() {
        return swaggerOrganization;
    }
    
    public String getSwaggerAuth() throws IOException {
        return secretsReader.readSecret();
    }
}
