package no.bibsys.aws.secrets;

import no.bibsys.aws.tools.IoUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

public class GithubSignatureCheckerTest {
    
    private final static String secretKey = "SECRETKEY";
    private final SecretsReader secretsReaderWithCorrectKey;
    private final SecretsReader secretsReaderWithAnotherKey;
    private final SecretsReader secretsReaderWithNullKey;
    
    public GithubSignatureCheckerTest() throws IOException {
        secretsReaderWithCorrectKey = Mockito.mock(SecretsReader.class);
        when(secretsReaderWithCorrectKey.readSecret()).thenReturn(secretKey);
    
        secretsReaderWithAnotherKey = Mockito.mock(SecretsReader.class);
        when(secretsReaderWithAnotherKey.readSecret()).thenReturn(secretKey.toLowerCase());
    
        secretsReaderWithNullKey = Mockito.mock(SecretsReader.class);
        when(secretsReaderWithNullKey.readSecret()).thenReturn(null);
    
    }
    
    @Test
    public void verifySecurityToken_correctSecretValueAndBody_validResult() throws IOException {
        String requestBody = IoUtils.resourceAsString(Paths.get("github", "sha_test_githubEvent.json"));
        String header = IoUtils.resourceAsString(Paths.get("github", "sha_test_github_header.txt"));
        
        GithubSignatureChecker signatureChecker = new GithubSignatureChecker(secretsReaderWithCorrectKey);
        boolean verificationResult = signatureChecker.verifySecurityToken(header, requestBody);
        
        assertThat(verificationResult, is(equalTo(true)));
    }
    
    @Test
    public void verifySecurityToken_wrongSecretValueAndBody_invalidResult() throws IOException {
        String requestBody = IoUtils.resourceAsString(Paths.get("github", "sha_test_githubEvent.json"));
        String header = IoUtils.resourceAsString(Paths.get("github", "sha_test_github_header.txt"));
    
        GithubSignatureChecker signatureChecker = new GithubSignatureChecker(secretsReaderWithAnotherKey);
        boolean verificationResult = signatureChecker.verifySecurityToken(header, requestBody);
    
        assertThat(verificationResult, is(equalTo(false)));
    }
    
    @Test
    public void verifySecurityToken_NullValueAndBody_validResult() throws IOException {
        String requestBody = IoUtils.resourceAsString(Paths.get("github", "sha_test_githubEvent.json"));
        String header = IoUtils.resourceAsString(Paths.get("github", "sha_test_github_header.txt"));
        
        GithubSignatureChecker signatureChecker = new GithubSignatureChecker(secretsReaderWithNullKey);
        boolean verificationResult = signatureChecker.verifySecurityToken(header, requestBody);
        
        assertThat(verificationResult, is(equalTo(true)));
    }
}

