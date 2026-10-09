package com.github.enerccio.marginalia.extensions.authorsnote.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.authorsnote.model.AuthorsNoteData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

@Configurable
public class AuthorsNoteService {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private InferenceServices inferenceServices;

    /**
     * Buffer for the headers of the inserted message, like {@code PrepareContentStep} adds for every story part.
     */
    private static final long MESSAGE_BUFFER_TOKENS = 100;

    private final Gson gson = new Gson();

    public AuthorsNoteData getAuthorsNote(Manuscript manuscript) {
        JsonObject attrs = manuscript.getAttributes();
        AuthorsNoteData data = null;
        if (attrs != null && attrs.has(AuthorsNoteData.KEY)) {
            data = gson.fromJson(attrs.get(AuthorsNoteData.KEY), AuthorsNoteData.class);
        }
        if (data == null) {
            data = new AuthorsNoteData();
        }
        if (data.getRole() == null) {
            data.setRole(LLMRole.SYSTEM);
        }
        return data;
    }

    public void saveAuthorsNote(Manuscript manuscript, AuthorsNoteData data) throws Exception {
        manuscript = manuscriptService.find(manuscript);
        JsonObject attrs = manuscript.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            manuscript.setAttributes(attrs);
        }
        attrs.add(AuthorsNoteData.KEY, gson.toJsonTree(data));
        manuscriptService.save(manuscript);
    }

    /**
     * Author's note of the book as it is stored now - the book of a running generation was loaded when it started,
     * the note may have been edited since.
     */
    public AuthorsNoteData loadCurrent(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return getAuthorsNote(current != null ? current : manuscript);
    }

    /**
     * Tokens the note takes in the prompt of {@code manuscript}'s model, 0 when it is not inserted.
     */
    public long countTokens(Manuscript manuscript, AuthorsNoteData data) throws Exception {
        if (!isInserted(data)) {
            return 0;
        }
        return inferenceServices.forAI(manuscript.getAi()).countTokens(data.getNote().trim()) + MESSAGE_BUFFER_TOKENS;
    }

    /**
     * Approximate size of {@code text} for the book's model - cheap enough to call while the user types.
     */
    public long estimateTokens(Manuscript manuscript, String text) throws Exception {
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        return inferenceServices.forAI(manuscript.getAi()).countTokensApprox(text.trim());
    }

    private boolean isInserted(AuthorsNoteData data) {
        return data.isEnabled() && StringUtils.isNotBlank(data.getNote());
    }

    /**
     * Returns a copy of {@code payload} with the note inserted {@link AuthorsNoteData#getDepth() depth} messages
     * before its end, or {@code payload} itself when the note is disabled or empty. The note never goes before the
     * leading system prompt.
     */
    public List<LLMChatMessage> insertNote(List<LLMChatMessage> payload, AuthorsNoteData data) {
        if (payload == null || !isInserted(data)) {
            return payload;
        }
        int min = !payload.isEmpty() && payload.getFirst().getRole() == LLMRole.SYSTEM ? 1 : 0;
        int index = Math.clamp(payload.size() - Math.max(0, data.getDepth()), min, payload.size());

        List<LLMChatMessage> result = new ArrayList<>(payload);
        result.add(index, LLMChatMessage.of(data.getRole(), data.getNote().trim()));
        return result;
    }
}
