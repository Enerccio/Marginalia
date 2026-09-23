package com.github.enerccio.marginalia.domain.service.impl.generation;

import java.util.HashMap;
import java.util.Map;

public enum GenerationStepType {

    PREPARE_GENERATION,
    PREPARE_CONSTANTS,
    PROCESS_LOREBOOK,
    PREPARE_CONTENT,
    PREPARE_PAYLOAD,
    GENERATE_NEW_MESSAGE,
    INFERENCE,
    CLEANUP

    ;

    private static GenerationStepType[] stepAll = values();
    private static Map<GenerationStepType, Integer> ordinals = new HashMap<>();

    static {
        for (int i=0; i<stepAll.length; i++) {
            ordinals.put(stepAll[i], i);
        }
    }

    public GenerationStepType next() {
        int ord = ordinals.get(this);
        if (ord < stepAll.length - 1) {
            return stepAll[ord + 1];
        }
        return null;
    }

}
