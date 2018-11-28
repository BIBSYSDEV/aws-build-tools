package no.bibsys.aws.git.github;

public class GitInfoImpl implements GitInfo {


    protected transient String repository;
    protected transient String branch;

    public GitInfoImpl() {

    }


    public GitInfoImpl(String repository, String branch) {
        this.repository = repository;
        this.branch = branch;
    }


    @Override
    public String getOwner() {
        throw new IllegalStateException("GitInfoImpl does not contain owener information");
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public String getOauth() {
        return null;
    }

    @Override
    public String getBranch() {
        return branch;
    }



    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }


}
