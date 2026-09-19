package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.AIType;

import java.util.List;

public interface InferenceService {

    AIType getType();

    List<String> getModels();

}
