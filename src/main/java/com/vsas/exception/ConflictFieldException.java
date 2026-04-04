package com.vsas.exception;

/** Business rule conflict tied to a single request field (for API + UI field errors). */
public class ConflictFieldException extends IllegalArgumentException {

    private final String field;

    public ConflictFieldException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
