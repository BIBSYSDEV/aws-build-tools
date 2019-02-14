package no.bibsys.aws.git.github;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import no.bibsys.aws.secrets.SecretsReader;
import no.bibsys.aws.tools.Environment;

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
 * <p>
 * Example: <br/> https://github.com/BIBSYSDEV/authority-registry-infrastructure/
 * <ul>
 * <li>owner:BIBSYSDEV</li>
 * <li>repository:authority-registry-infrastructure</li>
 * </ul>
 * </p>
 * </p>
 */

public class GithubConf implements GitInfo {

    public static final String REPO_OWNER = "OWNER";
    public static final String REPOSITORY = "REPOSITORY";
    public static final String BRANCH = "BRANCH";
    public static final String AWS_REGION = "AWS_REGION";

   
    private final transient String owner;
    private final transient String repo;
    private final transient String branch;
    private final transient Region region;
    private final transient SecretsReader secretsReader;
    
    public GithubConf(Environment environment, SecretsReader secretsReader) {
        this.owner = environment.readEnv(REPO_OWNER);
        this.repo = environment.readEnv(REPOSITORY);
        this.branch = environment.readEnv(BRANCH);
        this.region = Region.getRegion(Regions.fromName(environment.readEnv(AWS_REGION)));
        this.secretsReader = secretsReader;
    }
    
    public GithubConf(String owner, String repo, String branch, Region region, SecretsReader secretsReader) {
        this.owner = initOwner(owner);
        this.repo = initRepo(repo);
        this.branch = branch;
        this.region = region;
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
