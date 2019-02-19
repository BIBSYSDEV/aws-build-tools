package no.bibsys.aws.swaggerhub;

import no.bibsys.aws.secrets.SecretsReader;

import java.io.IOException;

/**
 * SwaggerHubInfo contains information about updating the SwaggerDocumentation for an API.
 */
public class SwaggerHubInfo {
    
    private final transient String apiId;
    private final transient String apiVersion;
    private final transient String swaggerOrganization;
    
    private final transient SecretsReader secretsReader;
    
    /**
     *
     * SwaggerHub constructor.
     * @param apiId The id of the API in the SwaggerHub account
     * @param apiVersion The version of the API
     * @param swaggerOrganization The username of the account or the organization
     * @param secretsReader SecretsReader that provides the Rest-API key for updating the SwaggerHub account
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
