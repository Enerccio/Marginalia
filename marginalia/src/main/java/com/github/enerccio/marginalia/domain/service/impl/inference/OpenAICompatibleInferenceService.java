package com.github.enerccio.marginalia.domain.service.impl.inference;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.domain.traits.SupportedAI;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.models.Model;
import com.openai.models.models.ModelListPage;
import com.openai.models.responses.inputtokens.InputTokenCountParams;
import com.openai.models.responses.inputtokens.InputTokenCountResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SupportedAI(AIType.OPEN_AI_COMPATIBLE)
@Configurable
public class OpenAICompatibleInferenceService implements InferenceService {
    private static final Logger log = LoggerFactory.getLogger(OpenAICompatibleInferenceService.class);

    @Autowired
    private TokenizerService tokenizerService;

    private final OpenAICompatible ai;

    public OpenAICompatibleInferenceService(AI ai) {
        this.ai = (OpenAICompatible) ai;
    }

    @Override
    public List<String> getModels() {
        OpenAIClient client = openClient();

        ModelListPage modelList = client.models().list();
        List<Model> models = modelList.data();

        if (models.isEmpty()) {
            log.info("Connection successful, but no models were returned.");
        } else {
            log.info("Connection successful!");
        }

        return models.stream().map(Model::id).toList();
    }

    @Override
    public long countTokens(String text) throws Exception {
        return tokenizerService.countTokens(ai, text);
    }

    private OpenAIClient openClient() {
        return OpenAIOkHttpClient.builder()
                .baseUrl(ai.getUri())
                .apiKey(ai.getApiKey())
                .build();
    }
}
