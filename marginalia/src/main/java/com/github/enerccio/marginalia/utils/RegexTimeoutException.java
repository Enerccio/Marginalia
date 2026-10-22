package com.github.enerccio.marginalia.utils;

/**
 * A regular expression ran out of the time it was given, see {@link TimeSafeText}. The message is written for the one
 * who wrote the expression (a user or a model), who can write a simpler one.
 */
public class RegexTimeoutException extends RuntimeException {

    public RegexTimeoutException(String message) {
        super(message);
    }

}
