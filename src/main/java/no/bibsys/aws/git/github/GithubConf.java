package no.bibsys.aws.git.github;

import no.bibsys.aws.secrets.SecretsReader;

import java.io.IOException;

/**
 * Helper class for containing the necessary details for retrieving information from a Github repository.
 *
 * <p>Terms:
 * <ul>
 * <li>owner: The owner of the repository</li>
 * <li>repository: The name of the repository</li>
 * <li>branch: Branch we are interested in</li>
 * </ul>
 *
 * <p>Example: <br> https://github.com/BIBSYSDEV/authority-registry-infrastructure/
 * <ul>
 * <li>owner:BIBSYSDEV</li>
 * <li>repository:authority-registry-infrastructure</li>
 * </ul>
 */

public class GithubConf implements GitInfo {
    
    private final transient String owner;
    private final transient String repo;
    private final transient String branch;
    private final transient SecretsReader secretsReader;
    
    public GithubConf(String owner, String repo, String branch, SecretsReader secretsReader) {
        this.owner = initOwner(owner);
        this.repo = initRepo(repo);
        this.branch = branch;
        this.secretsReader = secretsReader;
    }
    
    public String getOwner() {
        return owner;
    }
    
    @Override
    public String getRepository() {
        return repo;
    }
    
    @Override
    public String getBranch() {
        return branch;
    }
    
    public String getOauth() throws IOException {
        return secretsReader.readSecret();
    }
    
    private String initRepo(String repo) {
        return repo;
    }
    
    private String initOwner(String owner) {
        return owner;
    }
}
