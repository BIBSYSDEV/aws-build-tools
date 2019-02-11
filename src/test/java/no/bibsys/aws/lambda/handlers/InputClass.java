package no.bibsys.aws.lambda.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.bibsys.aws.tools.JsonUtils;

import java.util.Objects;

public class InputClass {
    private String id;
    private String randomField;

    public InputClass() {
    }

    public InputClass(String id, String randomField) {
        this.id = id;
        this.randomField = randomField;
    }

    public static InputClass create() {
        return new InputClass("id", "randomField");
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
        InputClass that = (InputClass) inputClass;
        return Objects.nonNull(inputClass) && Objects.equals(id, that.getId()) && Objects
                .equals(randomField, that.getRandomField());

    }

}
