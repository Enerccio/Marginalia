package com.github.enerccio.marginalia.extensions.httphooks;

/** A call to a backend or a hook failed: the address is refused, the server is not reachable, it answers with an error or with something unusable. */
public class BackendException extends Exception {

    public BackendException(String message) {
        super(message);
    }

    public BackendException(String message, Throwable cause) {
        super(message, cause);
    }
}
