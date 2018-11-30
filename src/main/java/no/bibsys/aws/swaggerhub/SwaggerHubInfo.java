package no.bibsys.aws.swaggerhub;

import java.io.IOException;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;

public class SwaggerHubInfo {





    private static String AWS_SECRET_NAME = "swaggerapikey";
    private static String AWS_SECRET_KEY = "swaggerapikey";

    private final transient String apiId;
    private final transient String apiVersion;
    private final transient String swaggerOrganization;





    /**
     *  SwaggerHub constructor without the use of {@link Environment}.
     * @param apiId The id of the api
     * @param apiVersion The version of the API documentation. Can be {@code null} if the intended action is for the
     *        whole API
     * @param swaggerOrganization The SwaggerHub organization or account name
     */


    public SwaggerHubInfo(String apiId, String apiVersion, String swaggerOrganization) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.swaggerOrganization = swaggerOrganization;
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

        SecretsReader secretsReader = new SecretsReader(AWS_SECRET_NAME, AWS_SECRET_KEY);
        return secretsReader.readSecret();

    }
}
