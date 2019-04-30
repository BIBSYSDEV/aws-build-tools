package no.bibsys.aws.tools;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvironmentTest {

    Environment environment = new Environment();

    @Test()
    public void getEnv_nonExistingEnvVariable_throwsException() {

        assertThrows(NullPointerException.class, () -> environment.readEnv("something_here"));
    }

    @Test
    public void getEnvOpt_nonExistingEnvVariable_emptyOptional() {
        Optional<String> value = environment.readEnvOpt("something_here");
        assertThat(value, is(equalTo(Optional.empty())));
    }

    @Test
    public void getEnv_existingVariable_envVariableValue() {
        Map<String, String> envVariable = randomEnvVariable();
        String key = envVariable.keySet().iterator().next();
        String expectedValue = envVariable.get(key);
    
        String actualValue = environment.readEnv(key);
        assertThat(actualValue, is(equalTo(expectedValue)));
    }
    
    @Test
    public void getEnvOpt_existingVariable_envVariableValue() {
        Map<String, String> envVariable = randomEnvVariable();
        
        String key = envVariable.keySet().iterator().next();
        String expectedValue = envVariable.get(key);
        
        Optional<String> actualValue = environment.readEnvOpt(key);
        
        assertTrue(actualValue.isPresent());
        assertThat(actualValue.get(), is(equalTo(expectedValue)));
    }
    
    private Map<String, String> randomEnvVariable() {
        Map<String, String> map = System.getenv();
        assertFalse(map.isEmpty());

        String value = null;
        String key = null;
        Iterator<String> keys = map.keySet().iterator();
        while (value == null || value.isEmpty()) {
            key = keys.next();
            value = map.get(key);
        }
        Map<String, String> envVariable = Collections.singletonMap(key, value);
        return envVariable;
    }
}
