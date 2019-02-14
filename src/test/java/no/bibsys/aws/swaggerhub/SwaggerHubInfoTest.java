package no.bibsys.aws.swaggerhub;

import no.bibsys.aws.lambda.handlers.LocalTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

class SwaggerHubInfoTest extends LocalTest {
    
    private final SwaggerHubInfo swaggerHubInfo;
    
    public SwaggerHubInfoTest() throws IOException {
        
        swaggerHubInfo = new SwaggerHubInfo("apiId", "apiVersion", "swaggerhubUser", secretsReader);
    }
    
    @Test
    void getApiId() {
        assertThat(swaggerHubInfo.getApiId(), is(not(emptyOrNullString())));
    }
    
    @Test
    void getApiVersion() {
        assertThat(swaggerHubInfo.getApiVersion(), is(not(emptyOrNullString())));
    }
    
    @Test
    void getSwaggerOrganization() {
        assertThat(swaggerHubInfo.getSwaggerOrganization(), is(not(emptyOrNullString())));
    }
    
    @Test
    void getSwaggerAuth() throws IOException {
        assertThat(swaggerHubInfo.getSwaggerAuth(), is(not(emptyOrNullString())));
    }
    
    @Test
    void getRegion() {
    }
}
