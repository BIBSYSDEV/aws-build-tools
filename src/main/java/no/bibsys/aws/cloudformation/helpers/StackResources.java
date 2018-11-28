package no.bibsys.aws.cloudformation.helpers;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.StackResource;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * It retrieves the Resources of a CloudFormation stack.
 */
public class StackResources {


    private final transient String stackId;

    public StackResources(String stackId) {
        this.stackId = stackId;
    }




    private Stream<StackResource> getResourcesStream(ResourceType resourceType) {
        AmazonCloudFormation client = AmazonCloudFormationClientBuilder.defaultClient();
        DescribeStackResourcesResult result =
                client.describeStackResources(new DescribeStackResourcesRequest().withPhysicalResourceId(stackId));
        return result.getStackResources().stream()
                .filter(resource -> resource.getResourceType().equals(resourceType.toString()));

    }


    public List<StackResource> getResources(ResourceType resourceType) {
        return getResourcesStream(resourceType).collect(Collectors.toList());
    }


    /**
     *  Lists the physical ids of the resources for a specific {@link ResourceType}.
     *
     * @param resourceType the {@link ResourceType}
     * @return A list with the Physical Resource ids of resources with the specified resource type
     */
    public List<String> getResourceIds(ResourceType resourceType) {
        return getResourcesStream(resourceType).map(StackResource::getPhysicalResourceId).collect(Collectors.toList());
    }


}
