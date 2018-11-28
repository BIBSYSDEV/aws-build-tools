package no.bibsys.aws.git.github;

import java.io.IOException;


public class BranchInfo implements GitInfo {

    private transient String owner;
    private  transient String repository;
    private  transient String branch;



    public BranchInfo(String owner,String repository, String branch) {
        this.repository = repository;
        this.branch = branch;
        this.owner=owner;
    }

    @Override
    public String getOwner() {
        return owner;
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

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public void  setOwner(String owner){
        this.owner=owner;
    }
}
