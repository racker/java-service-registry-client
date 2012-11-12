package com.rackspacecloud.service_registry.client.service_registry.exceptions;

public class UnexpectedStatusCodeException extends APIException {
    private String message;
    private int code;
    private int expectedCode;

    public UnexpectedStatusCodeException(int code, int expectedCode) {
        this.code = code;
        this.expectedCode = expectedCode;

        this.message = String.format("Expected status code %d, got %d", this.code, this.expectedCode);
    }
}
