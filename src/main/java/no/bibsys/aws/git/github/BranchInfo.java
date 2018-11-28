package no.bibsys.aws.git.github;

import java.io.IOException;


public class BranchInfo implements GitInfo {

    private final transient String repository;
    private final transient String branch;

    public BranchInfo(String repository, String branch) {
        this.repository = repository;
        this.branch = branch;
    }

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public String getOauth() throws IOException {
        return null;
    }

    @Override
    public String getBranch() {
        return branch;
    }
}
