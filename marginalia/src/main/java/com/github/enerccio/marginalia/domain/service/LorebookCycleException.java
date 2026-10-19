package com.github.enerccio.marginalia.domain.service;

/**
 * A subbook that would make a lorebook contain itself, however deep ({@link LorebookService#addSubbook}).
 */
public class LorebookCycleException extends IllegalArgumentException {

    public LorebookCycleException(String message) {
        super(message);
    }
}
