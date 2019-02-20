package no.bibsys.aws.git.github;

import no.bibsys.aws.secrets.SecretsReader;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.when;

class GithubConfTest {
    
    private static final String REPO_OWNER = "owner";
    private static final String REPOSITORY = "repository";
    private static final String BRANCH = "branch";
    private static final String SECRET_VALUE = "secretValue";
    
    private transient GithubConf githubConf;
    
    public GithubConfTest() throws IOException {
        SecretsReader secretsReader = Mockito.mock(SecretsReader.class);
        when(secretsReader.readSecret()).thenReturn(SECRET_VALUE);
        githubConf = new GithubConf(REPO_OWNER, REPOSITORY, BRANCH, secretsReader);
    }
    
    @Test
    void getOwner_GithubConf_notNull() {
        assertThat(githubConf.getOwner(), is(not(equalTo(null))));
    }
    
    @Test
    void getRepository_GithubConf_notNull() {
        assertThat(githubConf.getRepository(), is(not(equalTo(null))));
    }
    
    @Test
    void getBranch_GithubConf_notNull() {
        assertThat(githubConf.getBranch(), is(not(equalTo(null))));
    }
    
    @Test
    void getOauth_GithubConf_notNull() throws IOException {
        assertThat(githubConf.getOauth(), is(not(equalTo(null))));
    }
}
