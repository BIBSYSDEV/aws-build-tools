package no.bibsys.aws.swaggerhub;

import com.amazonaws.util.StringInputStream;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class CustomReponse implements CloseableHttpResponse {
    
    public static final String MESSAGE = "message";
    private static final String RANDOM_PROTOCOL = "HTTP";
    private static final int RANDOM_MAJOR_VERSION = 1;
    private static final int RANDOM_MINOR_VERSION = 1;
    private static final String RANDOM_REASON_PHRASE = "OK";
    
    @Override
    public void close() {
    }
    
    @Override
    public StatusLine getStatusLine() {
        return new BasicStatusLine(new ProtocolVersion(RANDOM_PROTOCOL, RANDOM_MAJOR_VERSION, RANDOM_MINOR_VERSION),
                                   HttpStatus.SC_OK, RANDOM_REASON_PHRASE);
    }
    
    @Override
    public void setStatusLine(StatusLine statusline) {
    
    }
    
    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
    
    }
    
    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
    
    }
    
    @Override
    public void setStatusCode(int code) throws IllegalStateException {
    
    }
    
    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
    
    }
    
    @Override
    public HttpEntity getEntity() {
        BasicHttpEntity entity = new BasicHttpEntity();
        try {
            entity.setContent(new StringInputStream(MESSAGE));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return entity;
    }
    
    @Override
    public void setEntity(HttpEntity entity) {
    
    }
    
    @Override
    public Locale getLocale() {
        return null;
    }
    
    @Override
    public void setLocale(Locale loc) {
    
    }
    
    @Override
    public ProtocolVersion getProtocolVersion() {
        return null;
    }
    
    @Override
    public boolean containsHeader(String name) {
        return false;
    }
    
    @Override
    public Header[] getHeaders(String name) {
        return new Header[0];
    }
    
    @Override
    public Header getFirstHeader(String name) {
        return null;
    }
    
    @Override
    public Header getLastHeader(String name) {
        return null;
    }
    
    @Override
    public Header[] getAllHeaders() {
        return new Header[0];
    }
    
    @Override
    public void addHeader(Header header) {
    
    }
    
    @Override
    public void addHeader(String name, String value) {
    
    }
    
    @Override
    public void setHeader(Header header) {
    
    }
    
    @Override
    public void setHeader(String name, String value) {
    
    }
    
    @Override
    public void setHeaders(Header[] headers) {
    
    }
    
    @Override
    public void removeHeader(Header header) {
    
    }
    
    @Override
    public void removeHeaders(String name) {
    
    }
    
    @Override
    public HeaderIterator headerIterator() {
        return null;
    }
    
    @Override
    public HeaderIterator headerIterator(String name) {
        return null;
    }
    
    @Override
    public HttpParams getParams() {
        return null;
    }
    
    @Override
    public void setParams(HttpParams params) {
    
    }
}
