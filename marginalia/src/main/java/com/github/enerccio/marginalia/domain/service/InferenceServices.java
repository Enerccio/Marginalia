package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;

public interface InferenceServices {

    InferenceService forAI(AI ai) throws Exception;

    interface InferenceProvider {

        InferenceService create(AI ai) throws Exception;

    }

}
