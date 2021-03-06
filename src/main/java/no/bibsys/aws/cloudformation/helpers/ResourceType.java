package no.bibsys.aws.cloudformation.helpers;

/**
 * Incomplete Enumeration of useful CloudFormation resource types.
 */
public enum ResourceType {

    REST_API, S3_BUCKET;

    public static String REST_API_RESOURCE_TYPE = "AWS::ApiGateway::RestApi";
    public static String S3_BUCKET_RESOURCE_TYPE = "AWS::S3::Bucket";

    @Override
    public String toString() {
        if (this.equals(REST_API)) {
            return REST_API_RESOURCE_TYPE;
        } else if (this.equals(S3_BUCKET)) {
            return S3_BUCKET_RESOURCE_TYPE;
        } else {
            throw new IllegalStateException("Unexpected ResourceType:" + this.name());
        }
    }

}
