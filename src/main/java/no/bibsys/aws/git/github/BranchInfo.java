package no.bibsys.aws.git.github;

public class BranchInfo implements GitInfo {

    private transient String repository;
    private transient String branch;

    public BranchInfo() {
        super();
    }

    public BranchInfo(String repository, String branch) {
        this.repository = repository;
        this.branch = branch;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    @Override
    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
