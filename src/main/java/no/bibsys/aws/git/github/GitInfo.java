package no.bibsys.aws.git.github;

/**
 * Interface for Helper classes containing the necessary details for retrieving information from a Git repository.
 *
 * <p>Terms:
 * <ul>
 * <li>owner: The owner of the repository</li>
 * <li>repository: The name of the repository</li>
 * <li>branch: Branch we are interested in</li>
 * </ul>
 * <p>
 * Example: <br/>
 * https://github.com/BIBSYSDEV/authority-registry-infrastructure/
 * <ul>
 * <li>owner:BIBSYSDEV</li>
 * <li>repository:authority-registry-infrastructure</li>
 * </ul>
 * </p>
 * </p>
 **/


public interface GitInfo {


    String getRepository();

    String getBranch();


}
