package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

/**
 * The first error reported to the user during a generation.
 *
 * @param message text for a person: the description of an inference error, the text of a simple error or the message
 *                of the exception
 * @param cause   the exception, null for an error that was only a text
 */
public record GenerationFailure(String message, Throwable cause) {
}
