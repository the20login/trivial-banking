package com.github.the20login.trivial.bankng.processing;

public class ProcessingError {
    private final OperationErrorCode errorCode;
    private final String description;

    public ProcessingError(OperationErrorCode errorCode, String description) {
        this.errorCode = errorCode;
        this.description = description;
    }

    public OperationErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ProcessingError{" +
                "errorCode=" + errorCode +
                ", description='" + description + '\'' +
                '}';
    }
}
