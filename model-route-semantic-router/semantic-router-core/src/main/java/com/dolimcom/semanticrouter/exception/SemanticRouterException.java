package com.dolimcom.semanticrouter.exception;

public class SemanticRouterException extends RuntimeException {

    public SemanticRouterException(String message) {
        super(message);
    }

    public SemanticRouterException(String message, Throwable cause) {
        super(message, cause);
    }
}
