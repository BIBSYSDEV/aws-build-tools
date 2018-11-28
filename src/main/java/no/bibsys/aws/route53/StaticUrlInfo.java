package no.bibsys.aws.route53;

import no.bibsys.aws.cloudformation.Stage;


/**
 * Information for creating a mapping of a dynamic API Gateway url to a static url.
 *<p>Terms:
 * <ul>
 * <li>Zone name: The name of the Route53 Hosted Zone</li>
 * <li>Record set name: The name of a CNAME RecordSet in the Route53 Hosted Zone</li>
 * <li>Domain name: The Name of an API Gateway Custom-Domain-Entry</li>
 * </ul>
 * The Record set name and the Domain entry must be identical with the exception that the Record set name ends with a
 * fullstop (.) while the Domain entry does not.
 * </p>
 */
public  class StaticUrlInfo {


    private final transient String recordSetName;
    private final transient String domainName;
    private final transient String zoneName;



    private final transient Stage stage;

    public StaticUrlInfo(String zoneName, String recordSetName,Stage stage) {
        this.zoneName = zoneName;
        this.recordSetName = recordSetName;
        this.domainName = recordSetName.substring(0, recordSetName.length() - 1);
        this.stage=stage;
    }



    public StaticUrlInfo copy(Stage stage){
        return new StaticUrlInfo(zoneName,recordSetName,stage);
    }

    public String getRecordSetName() {
        if(stage.equals(Stage.FINAL)){
            return recordSetName;
        }
        else{
            return "test."+recordSetName;
        }

    }

    public String getDomainName() {
        if(stage.equals(Stage.FINAL)){
            return domainName;
        }
        else{
            return "test."+domainName;
        }
    }

    public String getZoneName() {
        return zoneName;
    }

    public Stage getStage() {
        return stage;
    }


}
