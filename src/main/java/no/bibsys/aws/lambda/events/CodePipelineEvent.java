package no.bibsys.aws.lambda.events;

public class CodePipelineEvent implements DeployEvent {
    
    // id is necessary when CodePipeline calls a function
    private final String id;

    public CodePipelineEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}


