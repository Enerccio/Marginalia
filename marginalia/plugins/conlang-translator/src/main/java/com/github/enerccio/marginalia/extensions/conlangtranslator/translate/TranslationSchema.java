package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

/**
 * The JSON schema of the answer ({@link TranslationAnswer}), for a service that takes a structured output. Every property is required and
 * there are no others (a strict schema); a model that has nothing to say writes an empty text. Tried on a real model: it works and the model
 * still thinks.
 */
public final class TranslationSchema {

    private TranslationSchema() {
    }

    public static final String JSON = """
            {"type":"object","additionalProperties":false,"required":["translations","newWords"],"properties":{
            "translations":{"type":"array","items":{"type":"object","additionalProperties":false,
            "required":["quote","speaker","language","text","choice","understood","register"],"properties":{
            "quote":{"type":"string"},"speaker":{"type":"string"},"language":{"type":"string"},"text":{"type":"string"},
            "choice":{"type":"string"},"understood":{"type":"boolean"},"register":{"type":"string"}}}},
            "newWords":{"type":"array","items":{"type":"object","additionalProperties":false,
            "required":["word","gloss","category","rationale","register","kind","variantOf"],"properties":{
            "word":{"type":"string"},"gloss":{"type":"string"},"category":{"type":"string"},"rationale":{"type":"string"},
            "register":{"type":"string"},"kind":{"type":"string"},"variantOf":{"type":"string"}}}}}}
            """;
}
