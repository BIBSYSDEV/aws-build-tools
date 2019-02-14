package no.bibsys.aws.git.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

class BranchInfoTest {
    
    private static final String ANOTHER_REPO = "anotherRepo";
    private static final String REPOSITORY = "repository";
    private static final String BRANCH = "branch";
    private static final String ANOTHER_BRANCH = "anotherBranch";
    
    private transient BranchInfo branchInfo;
    private transient BranchInfo branchInfoWithNullValues;
    
    @BeforeEach
    public void init() {
        branchInfo = new BranchInfo(REPOSITORY, BRANCH);
        branchInfoWithNullValues = new BranchInfo();
    }
    
    @Test
    void getRepository_branchInfo_repository() {
        assertThat(branchInfo.getRepository(), is(equalTo(REPOSITORY)));
        assertThat(branchInfoWithNullValues.getRepository(), is(equalTo(null)));
    }
    
    @Test
    void setRepository() {
        branchInfo.setRepository(ANOTHER_REPO);
        assertThat(branchInfo.getRepository(), is(equalTo(ANOTHER_REPO)));
    }
    
    @Test
    void getBranch() {
        assertThat(branchInfo.getBranch(), is(equalTo(BRANCH)));
        assertThat(branchInfoWithNullValues.getBranch(), is(equalTo(null)));
        
    }
    
    @Test
    void setBranch() {
        
        branchInfo.setBranch(ANOTHER_BRANCH);
        assertThat(branchInfo.getBranch(), is(equalTo(ANOTHER_BRANCH)));
    }
}
