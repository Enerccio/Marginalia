package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.inputtokens.InputTokenCountParams;

public class OpenAiSdkTokenizerStrategy implements TokenizerStrategy {

    @Override
    public long countTokens(AI aiEntity, String text) throws Exception {
        OpenAICompatible ai = (OpenAICompatible) aiEntity;
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .baseUrl(ai.getUri())
                .apiKey(ai.getApiKey())
                .build();

        return client.responses().inputTokens().count(
                new InputTokenCountParams.Builder()
                        .model(ai.getModel())
                        .input(text)
                        .build()
        ).inputTokens();
    }

    @Override
    public String getName() {
        return "OPENAI_SDK";
    }
}