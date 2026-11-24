package com.github.enerccio.marginalia.extensions.storytracker.process;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the active branch of a book as the {@link Part}s a tracker follows.
 */
public final class StoryBranch {

    private StoryBranch() {
    }

    /**
     * The parts of the active branch of the book as it is stored now, the first part of the story first. Empty when the
     * book has no part. The text is the one the model reads ({@link PartTextRegistry#forModel}): it is what an extraction
     * quotes and what the hash of a processed part is made of.
     */
    public static List<Part> read(Manuscript manuscript, ManuscriptService manuscriptService, ChatMessageService chatMessageService,
                                  PartTextRegistry partTexts) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        ChatMessage leaf = (current != null ? current : manuscript).getActiveLeaf();
        List<Part> parts = new ArrayList<>();
        if (leaf != null) {
            for (ChatMessage message : chatMessageService.getBranchFromLeaf(leaf)) {
                parts.add(new Part(message.getUuid(), message.getPovCharacter(), message.getPresentCharacters(), partTexts.forModel(message),
                        message.getId()));
            }
        }
        return parts;
    }
}
