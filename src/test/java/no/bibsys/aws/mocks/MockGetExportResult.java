package no.bibsys.aws.mocks;

import com.amazonaws.services.apigateway.model.GetExportResult;

import java.nio.ByteBuffer;

public class MockGetExportResult extends GetExportResult {
    
    private final transient String stringBody;
    
    public MockGetExportResult(String body) {
        this.stringBody = body;
    }
    
    @Override
    public ByteBuffer getBody() {
        return ByteBuffer.wrap(stringBody.getBytes());
    }
    
}
