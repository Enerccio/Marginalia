package com.github.enerccio.marginalia.extensions.betareaders.service;

/**
 * The model ended its reply without writing anything.
 */
public class EmptyReplyException extends RuntimeException {

    public EmptyReplyException() {
        super("The model did not answer");
    }
}
