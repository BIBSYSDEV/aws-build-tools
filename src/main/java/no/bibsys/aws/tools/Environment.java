package no.bibsys.aws.tools;

import java.util.Objects;
import java.util.Optional;

public class Environment {

    public Optional<String> readEnvOpt(String variableName) {
        return Optional.ofNullable(System.getenv().get(variableName)).filter(value -> !value.isEmpty());
    }

    public String readEnv(String variableName) {
        String value = System.getenv().get(variableName);
        Objects.requireNonNull(value, variableName + " env variable was not found");
        return value;
    }
}
