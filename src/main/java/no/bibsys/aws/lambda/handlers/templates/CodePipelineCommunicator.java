package no.bibsys.aws.lambda.handlers.templates;

import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClientBuilder;
import com.amazonaws.services.codepipeline.model.ExecutionDetails;
import com.amazonaws.services.codepipeline.model.FailureDetails;
import com.amazonaws.services.codepipeline.model.FailureType;
import com.amazonaws.services.codepipeline.model.PutJobFailureResultRequest;
import com.amazonaws.services.codepipeline.model.PutJobSuccessResultRequest;
import no.bibsys.aws.lambda.events.CodePipelineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodePipelineCommunicator {
    
    private static final Logger logger = LoggerFactory.getLogger(CodePipelineCommunicator.class);
    private final transient AWSCodePipeline pipeline;
    
    public CodePipelineCommunicator(AWSCodePipeline pipelineClient) {
        this.pipeline = pipelineClient;
    }
    
    public CodePipelineCommunicator() {
        this(AWSCodePipelineClientBuilder.defaultClient());
    }
    
    public void sendSuccessToCodePipeline(CodePipelineEvent input, String outputString) {
        logger.info("sending success");
        PutJobSuccessResultRequest success = new PutJobSuccessResultRequest();
        success.withJobId(input.getId()).withExecutionDetails(new ExecutionDetails().withSummary(outputString));
        pipeline.putJobSuccessResult(success);
        logger.info("sent success");
    }
    
    public void sendFailureToCodePipeline(CodePipelineEvent input, String outputString) {
        FailureDetails failureDetails = new FailureDetails().withMessage(outputString).withType(FailureType.JobFailed);
        PutJobFailureResultRequest failure = new PutJobFailureResultRequest().withJobId(input.getId())
                                                                             .withFailureDetails(failureDetails);
        pipeline.putJobFailureResult(failure);
    }
    
}

