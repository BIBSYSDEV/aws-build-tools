package no.bibsys.aws.swaggerhub;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.Locale;

public class CustomReponse implements CloseableHttpResponse {
    @Override
    public void close() throws IOException {
    
    }
    
    @Override
    public StatusLine getStatusLine() {
        return new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, "OK");
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
        return null;
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
