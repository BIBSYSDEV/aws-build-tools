package no.bibsys.aws.lambda.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.bibsys.aws.tools.JsonUtils;

import java.util.Objects;

public class SampleClass {
    private static final String RANDOM_ID = "id";
    private static final String RANDOM_VALUE = "randomField";
    private String id;
    private String randomField;
    
    public SampleClass() {
    }
    
    public SampleClass(String id, String randomField) {
        this.id = id;
        this.randomField = randomField;
    }
    
    public static SampleClass create() {
        return new SampleClass(RANDOM_ID, RANDOM_VALUE);
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getRandomField() {
        return randomField;
    }
    
    public void setRandomField(String randomField) {
        this.randomField = randomField;
    }
    
    public String asJsonString() throws JsonProcessingException {
        return JsonUtils.newJsonParser().writeValueAsString(this);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, randomField);
    }
    
    @Override()
    public boolean equals(Object inputClass) {
        SampleClass that = (SampleClass) inputClass;
        return Objects.nonNull(inputClass) && Objects.equals(id, that.getId()) && Objects.equals(randomField,
                                                                                                 that.getRandomField());
        
    }
    
}
