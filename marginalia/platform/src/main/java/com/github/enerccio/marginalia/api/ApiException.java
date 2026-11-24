package com.github.enerccio.marginalia.api;

/**
 * A call of the API that ends with the status and the message, thrown by the handlers of {@link ApiDispatcher}.
 */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
