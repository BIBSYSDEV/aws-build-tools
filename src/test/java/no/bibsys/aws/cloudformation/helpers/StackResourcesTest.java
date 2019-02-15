package no.bibsys.aws.cloudformation.helpers;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.DescribeStackResourcesResult;
import com.amazonaws.services.cloudformation.model.StackResource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class StackResourcesTest {
    
    private static final String LOGICAL_ID = "logicalId";
    private static final String RANDOM_CHAR = "a";
    private static final String ANOTHER_LOGICAL_ID = LOGICAL_ID + RANDOM_CHAR;
    private static final String PHYSICAL_ID = "physicalId";
    private static final String ANOTHER_PHYSICAL_ID = PHYSICAL_ID + RANDOM_CHAR;
    private static final ResourceType RESOURCE_TYPE = ResourceType.REST_API;
    private static final ResourceType UNWANTED_RESOURCE = ResourceType.S3_BUCKET;
    private static final int EXPECTED_NUMBER_OF_RESOURCES = 1;
    private static final int REQUEST = 0;
    private static final String RANDOM_STACK_NAME = "STACK_NAME";
    private final transient StackResources stackResources;
    
    public StackResourcesTest() {
        AmazonCloudFormation client = Mockito.mock(AmazonCloudFormation.class);
        
        when(client.describeStackResources(any())).then((Answer<DescribeStackResourcesResult>) invocation -> {
            DescribeStackResourcesRequest request = invocation.getArgument(REQUEST);
            String stackName = request.getStackName();
            
            StackResource stackResource = new StackResource().withStackName(stackName).withLogicalResourceId(LOGICAL_ID)
                                                             .withPhysicalResourceId(PHYSICAL_ID)
                                                             .withResourceType(RESOURCE_TYPE.toString());
            StackResource annotherResource =
                    new StackResource().withStackName(stackName).withLogicalResourceId(ANOTHER_LOGICAL_ID)
                                       .withPhysicalResourceId(ANOTHER_PHYSICAL_ID)
                                       .withResourceType(UNWANTED_RESOURCE.toString());
            
            return new DescribeStackResourcesResult().withStackResources(stackResource, annotherResource);
        });
    
        this.stackResources = new StackResources(RANDOM_STACK_NAME, client);
    }
    
    @Test
    public void getResources_stackWithResources_nonEmptyList() {
        List<StackResource> list = stackResources.getResources(ResourceType.REST_API);
        assertThat(list.size(), is(equalTo(EXPECTED_NUMBER_OF_RESOURCES)));
    }
    
    @Test
    public void getResourceIds_stackWithResources_nonEmptyList() {
        List<String> list = stackResources.getResourceIds(ResourceType.REST_API);
        assertThat(list.size(), is(equalTo(EXPECTED_NUMBER_OF_RESOURCES)));
    }
    
}
