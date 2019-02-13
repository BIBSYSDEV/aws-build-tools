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
    
    private final SecretsReader secretsReader;
    private final static String secretKey = "SECRETKEY";
    
    public GithubSignatureCheckerTest() throws IOException {
        secretsReader = Mockito.mock(SecretsReader.class);
        
        when(secretsReader.readSecret()).thenReturn(secretKey);
    }
    
    @Test
    public void verifySecurityToken_secretValueAndBody_sha1Signature() throws IOException {
        String requestBody = IoUtils.resourceAsString(Paths.get("github", "sha_test_githubEvent.json"));
        String header = IoUtils.resourceAsString(Paths.get("github", "sha_test_github_header.txt"));
        
        GithubSignatureChecker signatureChecker = new GithubSignatureChecker(secretsReader);
        boolean verificationResult = signatureChecker.verifySecurityToken(header, requestBody);
        
        assertThat(verificationResult, is(equalTo(true)));
    }
}
