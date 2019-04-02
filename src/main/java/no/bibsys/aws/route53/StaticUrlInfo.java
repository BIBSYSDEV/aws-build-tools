package no.bibsys.aws.route53;

import com.google.common.base.Preconditions;
import no.bibsys.aws.cloudformation.Stage;

/**
 * Information for creating a mapping of a dynamic API Gateway url to a static url.
 * <p>Terms:
 * <ul>
 * <li>Zone name: The name of the Route53 Hosted Zone</li>
 * <li>Record set name: The name of a CNAME RecordSet in the Route53 Hosted Zone</li>
 * <li>Domain name: The Name of an API Gateway Custom-Domain-Entry</li>
 * </ul>
 * The Record set name and the Domain entry must be identical with the exception that the Record set name ends with a
 * fullstop (.) while the Domain entry does not.
 * </p>
 */
public class StaticUrlInfo {
    
    public static final String INVALID_ZONE_NAME_ERROR = "The zoneName %s should end with a \".\"";
    public static final String INVALID_REDCORDSET_NAME_ERROR = "The address %s should end with a \".\"";
    private final transient String recordSetName;
    private final transient String domainName;
    private final transient String zoneName;

    private final transient Stage stage;

    public StaticUrlInfo(String zoneName, String recordSetName, Stage stage) {
        this.zoneName = zoneName;
        Preconditions.checkArgument(zoneName.endsWith("."), INVALID_ZONE_NAME_ERROR, zoneName);
    
        this.recordSetName = recordSetName;
        Preconditions.checkArgument(recordSetName.endsWith("."), INVALID_REDCORDSET_NAME_ERROR, recordSetName);
        this.domainName = recordSetName.substring(0, recordSetName.length() - 1);
        this.stage = stage;
    }

    public StaticUrlInfo copy(Stage stage) {
        return new StaticUrlInfo(zoneName, recordSetName, stage);
    }

    public String getRecordSetName() {
        return recordSetName;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getZoneName() {
        return zoneName;
    }

    public Stage getStage() {
        return stage;
    }
}
