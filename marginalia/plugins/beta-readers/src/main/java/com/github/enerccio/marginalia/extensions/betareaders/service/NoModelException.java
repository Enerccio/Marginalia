package com.github.enerccio.marginalia.extensions.betareaders.service;

/**
 * There is no AI to ask: the chat, the settings chain and the book name none that still exists.
 */
public class NoModelException extends IllegalStateException {

    public NoModelException() {
        super("There is no AI to ask");
    }
}
