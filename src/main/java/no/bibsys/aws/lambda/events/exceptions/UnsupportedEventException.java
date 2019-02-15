package no.bibsys.aws.lambda.events.exceptions;

public class UnsupportedEventException extends Exception {
    
    public UnsupportedEventException(String s) {
        super(s);
    }
}
