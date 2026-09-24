package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;

public class GenerationRequest {

    private GenerationRequestType requestType;
    private ChatMessage node;

    private GenerationRequest() {

    }

    public GenerationRequestType getRequestType() {
        return requestType;
    }

    public ChatMessage getNode() {
        return node;
    }

    public static GenerationRequest newMessage() {
        GenerationRequest generationRequest = new GenerationRequest();
        generationRequest.requestType = GenerationRequestType.NEW_MESSAGE;
        return generationRequest;
    }

    public static GenerationRequest newSwipe(ChatMessage sibling) {
        GenerationRequest generationRequest = new GenerationRequest();
        generationRequest.requestType = GenerationRequestType.NEW_MESSAGE;
        generationRequest.node = sibling;
        return generationRequest;
    }

    public static GenerationRequest regenerate(ChatMessage self) {
        GenerationRequest generationRequest = new GenerationRequest();
        generationRequest.requestType = GenerationRequestType.REGENERATE;
        generationRequest.node = self;
        return generationRequest;
    }
}
