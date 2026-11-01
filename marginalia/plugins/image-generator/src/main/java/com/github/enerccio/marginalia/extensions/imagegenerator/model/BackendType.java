package com.github.enerccio.marginalia.extensions.imagegenerator.model;

/** How a backend is spoken to. */
public enum BackendType {
    /** A ComfyUI server: an API-format workflow with placeholders is posted to {@code /prompt}. */
    COMFYUI,
    /** {@code POST /v1/images/generations} of the OpenAI API (also served by sd.cpp, LiteLLM, many SaaS). */
    OPENAI
}
