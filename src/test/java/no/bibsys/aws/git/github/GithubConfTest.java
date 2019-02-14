package no.bibsys.aws.git.github;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class GithubConfTest {
    
    private static final String REPO_OWNER = "owner";
    private static final String REPOSITORY = "repository";
    private static final String BRANCH = "branch";
    private static final String SECRET_VALUE = "secretValue";
    private static final Region REGION = Region.getRegion(Regions.EU_WEST_1);
    
    private final transient GithubConf githhubconfWithEnv;
    private transient GithubConf githubConf;
    
    public GithubConfTest() throws IOException {
        SecretsReader secretsReader = Mockito.mock(SecretsReader.class);
        when(secretsReader.readSecret()).thenReturn(SECRET_VALUE);
        githubConf = new GithubConf(REPO_OWNER, REPOSITORY, BRANCH, secretsReader);
    
        Environment environment = Mockito.mock(Environment.class);
        when(environment.readEnv(anyString())).thenAnswer(invocation -> {
            String envVariable = invocation.getArgument(0);
            if (GithubConf.AWS_REGION.equals(envVariable)) {
                return REGION.toString();
            } else {
                return envVariable;
            }
        });
        
        githhubconfWithEnv = new GithubConf(environment, secretsReader);
        
    }
    
    @Test
    void getOwner_GithubConf_notNull() {
        assertThat(githubConf.getOwner(), is(not(equalTo(null))));
        assertThat(githhubconfWithEnv.getOwner(), is(not(equalTo(null))));
    }
    
    @Test
    void getRepository_GithubConf_notNull() {
        assertThat(githubConf.getRepository(), is(not(equalTo(null))));
        assertThat(githhubconfWithEnv.getRepository(), is(not(equalTo(null))));
    }
    
    @Test
    void getBranch_GithubConf_notNull() {
        assertThat(githubConf.getBranch(), is(not(equalTo(null))));
        assertThat(githhubconfWithEnv.getBranch(), is(not(equalTo(null))));
        
    }
    
    @Test
    void getOauth_GithubConf_notNull() throws IOException {
        assertThat(githubConf.getOauth(), is(not(equalTo(null))));
        assertThat(githhubconfWithEnv.getOauth(), is(not(equalTo(null))));
        
    }
}
