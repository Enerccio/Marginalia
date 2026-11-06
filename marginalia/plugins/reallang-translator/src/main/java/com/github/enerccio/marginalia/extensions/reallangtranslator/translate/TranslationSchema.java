package com.github.enerccio.marginalia.extensions.reallangtranslator.translate;

/**
 * The JSON schema of the answer ({@link TranslationAnswer}), for a service that takes a structured output. Every property is required and there are no
 * others (a strict schema); a model that has nothing to say writes an empty text.
 */
public final class TranslationSchema {

    private TranslationSchema() {
    }

    public static final String JSON = """
            {"type":"object","additionalProperties":false,"required":["translations","names"],"properties":{
            "translations":{"type":"array","items":{"type":"object","additionalProperties":false,
            "required":["quote","speaker","language","text","reading","choice","understood","register"],"properties":{
            "quote":{"type":"string"},"speaker":{"type":"string"},"language":{"type":"string"},"text":{"type":"string"},
            "reading":{"type":"string"},"choice":{"type":"string"},"understood":{"type":"boolean"},"register":{"type":"string"}}}},
            "names":{"type":"array","items":{"type":"object","additionalProperties":false,"required":["source","written","reading","note"],"properties":{
            "source":{"type":"string"},"written":{"type":"string"},"reading":{"type":"string"},"note":{"type":"string"}}}}}}
            """;
}
