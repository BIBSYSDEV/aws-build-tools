package no.bibsys.aws.cloudformation.helpers;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
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

    private final transient String stackName;
    private final transient AmazonCloudFormation client;
    
    public StackResources(String stackName, AmazonCloudFormation client) {
        this.stackName = stackName;
        this.client = client;
    }
    
    public List<StackResource> getResources(ResourceType resourceType) {
        return getResourcesStream(resourceType).collect(Collectors.toList());
    }
    
    public List<String> getResourceIds(ResourceType resourceType) {
        return getResourcesStream(resourceType).map(StackResource::getPhysicalResourceId).collect(Collectors.toList());
    }
    
    
    private Stream<StackResource> getResourcesStream(ResourceType resourceType) {
        DescribeStackResourcesResult result = client
            .describeStackResources(new DescribeStackResourcesRequest().withStackName(stackName));
        return result.getStackResources().stream()
            .filter(resource -> resource.getResourceType().equals(resourceType.toString()));
    }
    
    /**
     * Lists the physical ids of the resources for a specific {@link ResourceType}.
     *
     * @param resourceType the {@link ResourceType}
     * @return A list with the Physical Resource ids of resources with the specified resource type
     */
    
}
