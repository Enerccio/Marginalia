package com.github.enerccio.marginalia.domain.service.impl.generation;

public enum GenerationStepType {

    PREPARE_GENERATION,
    GENERATE_NEW_MESSAGE,
    PROCESS_LOREBOOK,
    PREPARE_CONTENT,
    PREPARE_PAYLOAD,
    INFERENCE,
    CLEANUP

    ;

    public GenerationStepType next() {
        return switch (this) {
            case PREPARE_GENERATION -> GENERATE_NEW_MESSAGE;
            case GENERATE_NEW_MESSAGE -> PROCESS_LOREBOOK;
            case PROCESS_LOREBOOK -> PREPARE_CONTENT;
            case PREPARE_CONTENT -> PREPARE_PAYLOAD;
            case PREPARE_PAYLOAD -> INFERENCE;
            case INFERENCE -> CLEANUP;
            case CLEANUP -> null;
        };
    }

}
