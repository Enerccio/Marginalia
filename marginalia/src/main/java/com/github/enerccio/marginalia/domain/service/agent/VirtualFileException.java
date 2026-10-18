package com.github.enerccio.marginalia.domain.service.agent;

/**
 * A file operation of an agent that did not happen. The message is written for the model and is the only thing of the
 * exception that may reach it: it names virtual paths only, never a real path of the server. The cause, when there is
 * one, is for the log.
 */
public class VirtualFileException extends RuntimeException {

    public VirtualFileException(String message) {
        super(message);
    }

    public VirtualFileException(String message, Throwable cause) {
        super(message, cause);
    }

}
