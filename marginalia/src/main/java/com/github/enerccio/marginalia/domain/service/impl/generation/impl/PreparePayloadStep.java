package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.domain.service.agent.ImageFiles;
import com.github.enerccio.marginalia.domain.service.agent.VirtualPath;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PreparePayloadStep extends GenerationStepBase {
    private static final Logger log = LoggerFactory.getLogger(PreparePayloadStep.class);

    /** The answer to the message with the images of the lore block. */
    static final String LORE_IMAGES_ACKNOWLEDGED = "[ Noted, I will keep these images in mind. ]";

    @Autowired
    private ResourceService resourceService;

    @SuppressWarnings("unchecked")
    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PREPARE_PAYLOAD, () -> {
            List<LLMChatMessage> payload = new ArrayList<>();
            LLMChatMessage system = createSystemPrompt(controller);
            payload.add(system);
            List<LoreImage> loreImages = (List<LoreImage>) controller.getProperties().getOrDefault(GenerationProperties.LORE_IMAGES, List.of());
            LLMChatMessage loreBlockImages = imagesMessage(loreImages.stream()
                    .filter(image -> image.insertion() == InsertionMode.IN_LORE_BLOCK).toList(), null);
            if (loreBlockImages != null) {
                AI ai = controller.getAi();
                if (ai != null && ai.isLoreImagesInSystem() && ai.getAiType() == AIType.OPEN_AI_COMPATIBLE) {
                    // the model's chat template takes images in the system message: they go where the text of the lore block is
                    system.setContent(StringUtils.isBlank(system.getContent()) ? loreBlockImages.getContent()
                            : system.getContent() + "\n\n" + loreBlockImages.getContent());
                    loreBlockImages.getAttachments().forEach(system::addAttachment);
                } else {
                    // the system message of most APIs can't carry images: the lore block's go into a message of their own,
                    // answered, so the story goes on with the user as before
                    payload.add(loreBlockImages);
                    payload.add(createAssistantPrompt(LORE_IMAGES_ACKNOWLEDGED));
                }
            }
            List<String> story = (List<String>) controller.getProperties().get(GenerationProperties.MANUSCRIPT_CHRONICLE);
            if (story != null) {
                boolean first = true;
                for (String blob : story) {
                    if (first) {
                        payload.add(createUserPrompt("[ Generate story. ]"));
                        first = false;
                    } else {
                        payload.add(createUserPrompt("[ Generate more story. ]"));
                    }
                    payload.add(createAssistantPrompt(blob));
                }
            }
            String prompt = controller.getPrePromptData().getUserPromptProcessed();
            LLMChatMessage withPrompt = imagesMessage(loreImages.stream()
                    .filter(image -> image.insertion() != InsertionMode.IN_LORE_BLOCK).toList(), prompt);
            payload.add(withPrompt != null ? withPrompt : createUserPrompt(prompt));
            controller.setPayload(payload);
            controller.emitEvent(Events.AFTER_PREPARE_PAYLOAD, controller::next);
        });
    }

    /**
     * A user message with the images (each resource once, those that can't be read are left out) and a line naming the entries they
     * belong to, after the prompt when there is one; null when no image could be added.
     */
    private LLMChatMessage imagesMessage(List<LoreImage> images, String prompt) {
        if (images.isEmpty()) {
            return null;
        }
        Map<String, LoreImage> unique = new LinkedHashMap<>();
        images.forEach(image -> unique.putIfAbsent(image.resource(), image));
        LLMChatMessage message = new LLMChatMessage();
        message.setRole(LLMRole.USER);
        List<String> names = new ArrayList<>();
        for (LoreImage image : unique.values()) {
            LLMAttachment attachment = attachment(image);
            if (attachment != null) {
                message.addAttachment(attachment);
                names.add((names.size() + 1) + ". " + describe(image));
            }
        }
        if (!message.hasAttachments()) {
            return null;
        }
        String caption = "[ Images of the lore, in order: " + String.join("; ", names) + ". ]";
        message.setContent(StringUtils.isBlank(prompt) ? caption : prompt + "\n\n" + caption);
        return message;
    }

    private static String describe(LoreImage image) {
        String entry = StringUtils.defaultIfBlank(image.entryName(), "?");
        return StringUtils.isBlank(image.caption()) || image.caption().equalsIgnoreCase(entry) ? entry : entry + " (" + image.caption() + ")";
    }

    private LLMAttachment attachment(LoreImage image) {
        try {
            Resource resource = resourceService.findImage(image.resource());
            if (resource == null) {
                log.warn("The image {} of the lore entry {} is not there", image.resource(), image.entryName());
                return null;
            }
            byte[] data = resourceService.getResourceData(resource);
            return ImageFiles.prepare(data, VirtualPath.parse("/lore/" + image.resource())).toAttachment(
                    StringUtils.defaultIfBlank(resource.getOriginalName(), image.resource()));
        } catch (Exception e) {
            log.warn("The image {} of the lore entry {} can't be sent", image.resource(), image.entryName(), e);
            return null;
        }
    }

    private LLMChatMessage createSystemPrompt(GenerationController controller) {
        LLMChatMessage message = new LLMChatMessage();
        message.setRole(LLMRole.SYSTEM);
        PrePromptData data = controller.getPrePromptData();
        // the jailbreak goes first, before everything else in the prompt (its tokens are counted in PrepareContentStep)
        if (StringUtils.isNotBlank(data.getJailbreak())) {
            message.setContent(data.getJailbreak() + "\n\n" + StringUtils.defaultString(data.getSystemPrompt()));
        } else {
            message.setContent(data.getSystemPrompt());
        }
        return message;
    }

    private LLMChatMessage createUserPrompt(String prompt) {
        LLMChatMessage message = new LLMChatMessage();
        message.setRole(LLMRole.USER);
        message.setContent(prompt);
        return message;
    }

    private LLMChatMessage createAssistantPrompt(String prompt) {
        LLMChatMessage message = new LLMChatMessage();
        message.setRole(LLMRole.ASSISTANT);
        message.setContent(prompt);
        return message;
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PREPARE_PAYLOAD;
    }
}
