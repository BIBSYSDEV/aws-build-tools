package no.bibsys.aws.secrets;

import java.io.IOException;

public interface SecretsReader {
    
    String readSecret() throws IOException;
}
