package no.bibsys.aws.cloudformation.helpers;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

class ResourceTypeTest {
    
    @Test
    public void toString_ResourceType_notNullValue() {
        Arrays.asList(ResourceType.values()).forEach(resource -> assertThat(resource, is(not(equalTo(null)))));
        
    }
    
}
