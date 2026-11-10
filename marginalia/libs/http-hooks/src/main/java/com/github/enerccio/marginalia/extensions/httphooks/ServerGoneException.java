package com.github.enerccio.marginalia.extensions.httphooks;

/**
 * The server did not answer a call at all (refused, reset, timed out) or says it has nothing loaded (503): it is down, crashed or was unloaded,
 * and starting it again may help, unlike a refusal or an error answer of a server that is up.
 */
public class ServerGoneException extends BackendException {

    public ServerGoneException(String message) {
        super(message);
    }

    public ServerGoneException(String message, Throwable cause) {
        super(message, cause);
    }
}
