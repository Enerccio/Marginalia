package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;

public class JavaTokkitStrategy implements TokenizerStrategy {

    private final EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    private final Encoding encoding = registry.getEncodingForModel(ModelType.GPT_4O);

    @Override
    public long countTokens(AI ai, String text) {
        return encoding.countTokens(text);
    }

    @Override
    public String getName() {
        return "LOCAL_TOKKIT";
    }
}