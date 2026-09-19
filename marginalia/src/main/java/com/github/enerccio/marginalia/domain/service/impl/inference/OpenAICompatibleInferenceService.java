package com.github.enerccio.marginalia.domain.service.impl.inference;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.models.Model;
import com.openai.models.models.ModelListPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OpenAICompatibleInferenceService implements InferenceService {
    private static final Logger log = LoggerFactory.getLogger(OpenAICompatibleInferenceService.class);

    private final OpenAICompatible ai;

    public OpenAICompatibleInferenceService(AI ai) {
        this.ai = ai instanceof OpenAICompatible ? (OpenAICompatible) ai : null;
    }

    @Override
    public AIType getType() {
        return AIType.OPEN_AI_COMPATIBLE;
    }

    @Override
    public List<String> getModels() {
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .baseUrl(ai.getUri())
                .apiKey(ai.getApiKey())
                .build();

        log.info("Testing connection to OpenAI...");

        ModelListPage modelList = client.models().list();
        List<Model> models = modelList.data();

        if (models.isEmpty()) {
            log.info("Connection successful, but no models were returned.");
        } else {
            log.info("Connection successful!");
        }

        return models.stream().map(Model::id).toList();
    }
}
