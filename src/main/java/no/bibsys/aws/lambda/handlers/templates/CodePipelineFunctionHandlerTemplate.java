package no.bibsys.aws.lambda.handlers.templates;

import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClientBuilder;
import com.amazonaws.services.codepipeline.model.ExecutionDetails;
import com.amazonaws.services.codepipeline.model.FailureDetails;
import com.amazonaws.services.codepipeline.model.FailureType;
import com.amazonaws.services.codepipeline.model.PutJobFailureResultRequest;
import com.amazonaws.services.codepipeline.model.PutJobSuccessResultRequest;
import com.amazonaws.services.lambda.runtime.Context;
import no.bibsys.aws.lambda.events.CodePipelineEvent;
import no.bibsys.aws.lambda.events.DeployEvent;
import no.bibsys.aws.lambda.events.DeployEventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Optional;

/**
 * Lambda handler template for lambda functions invoked during a CodePipeline deploy process. The template includes
 * sending success and failure messages to the pipeline.
 *
 * @param <O> The output object of the {@link HandlerTemplate#processInput(Object, String, Context)} method
 */

public abstract class CodePipelineFunctionHandlerTemplate<O> extends HandlerTemplate<DeployEvent, O> {

    private static final Logger logger = LoggerFactory.getLogger(CodePipelineFunctionHandlerTemplate.class);
    private final transient AWSCodePipeline pipeline = AWSCodePipelineClientBuilder.defaultClient();

    public CodePipelineFunctionHandlerTemplate() {
        super(DeployEvent.class);
    }

    @Override
    protected final DeployEvent parseInput(String inputString) throws IOException {
        return DeployEventBuilder.create(inputString);
    }

    @Override
    protected void writeOutput(DeployEvent input, O output) throws IOException {
        String outputString = objectMapper.writeValueAsString(output);

        writeOutput(outputString);
        logger.info(input.getClass().getName());
        logger.info(String.valueOf(input instanceof CodePipelineEvent));

        if (isPipelineEvent(input)) {
            sendSuccessToCodePipeline((CodePipelineEvent) input, outputString);
        }
    }

    private void writeOutput(String outputString) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(outputString);
        }
    }

    @Override
    protected void writeFailure(DeployEvent input, Throwable error) throws IOException {
        String outputString = Optional.ofNullable(error.getMessage()).orElse("Unknown error. Check stacktrace.");
        if (isPipelineEvent(input)) {
            sendFailureToCodePipeline((CodePipelineEvent) input, outputString);
        }
        writeOutput(outputString);
    }

    private void sendSuccessToCodePipeline(CodePipelineEvent input, String outputString) {
        logger.info("sending success");
        CodePipelineEvent codePipelineEvent = input;
        PutJobSuccessResultRequest success = new PutJobSuccessResultRequest();
        success.withJobId(codePipelineEvent.getId())
            .withExecutionDetails(new ExecutionDetails().withSummary(outputString));
        pipeline.putJobSuccessResult(success);
        logger.info("sent success");
    }

    private void sendFailureToCodePipeline(CodePipelineEvent input, String outputString) {
        FailureDetails failureDetails = new FailureDetails().withMessage(outputString).withType(FailureType.JobFailed);
        PutJobFailureResultRequest failure = new PutJobFailureResultRequest().withJobId(input.getId())
            .withFailureDetails(failureDetails);
        pipeline.putJobFailureResult(failure);
    }

    private boolean isPipelineEvent(DeployEvent buildEvent) {
        return buildEvent instanceof CodePipelineEvent;
    }
}
