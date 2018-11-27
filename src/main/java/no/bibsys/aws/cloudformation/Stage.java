package no.bibsys.aws.cloudformation;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import no.bibsys.aws.tools.Environment;


/**
 * Enumeration of Deployment stages.
 * Deployment stage:
 * <ul>
 * <li>Test: for running tests</li>
 * <li>Final: Production or production-like</li>
 * </ul>
 */
public enum Stage {

    TEST, FINAL;


    public static Stage currentStage() {
        String stageString = new Environment().readEnv("STAGE");
        return Stage.fromString(stageString);
    }

    public static Stage fromString(String stage) {
        if (stage.equalsIgnoreCase(FINAL.name())) {
            return FINAL;
        } else if (stage.equalsIgnoreCase(TEST.name())) {
            return TEST;
        } else {
            throw new IllegalArgumentException("Allowed stages:"
                + String.join(",",
                listStages().stream().map(st -> st.toString()).collect(Collectors.toList())));
        }
    }

    public static List<Stage> listStages() {

        List<Stage> stages = new ArrayList<>();
        stages.add(TEST);
        stages.add(FINAL);
        return stages;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.getDefault());
    }


}
