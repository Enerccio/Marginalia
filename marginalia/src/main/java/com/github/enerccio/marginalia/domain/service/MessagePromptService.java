package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessagePrompt;
import com.github.enerccio.marginalia.domain.repository.MessagePromptRepository;

import java.util.Collection;
import java.util.Map;

/**
 * The prompt a part was written from (the whole payload sent to the model) and the lore that was in it, kept apart from
 * the part ({@link MessagePrompt}): the parts of a branch are loaded together and a request weighs hundreds of
 * kilobytes, so the part only has {@link ChatMessage#getBuiltPromptTokens()} and whoever needs the text asks here. A
 * part has at most one prompt, one without any text is not kept.
 */
public interface MessagePromptService extends OwnedService<MessagePrompt, MessagePromptRepository> {

    /**
     * The prompt of the part, null when it has none (it was generated before prompts were kept, was typed by the
     * author, the part is of another user).
     */
    MessagePrompt findFor(ChatMessage part) throws Exception;

    /**
     * The prompts of the parts, by the id of the part; the parts without a prompt are not in the map.
     */
    Map<Long, MessagePrompt> findAllFor(Collection<ChatMessage> parts) throws Exception;

    /**
     * The built prompt of the part, null when it has none.
     */
    String getBuiltPrompt(ChatMessage part) throws Exception;

    /**
     * The lore that was in the prompt of the part, null when there is none.
     */
    String getBackgroundLore(ChatMessage part) throws Exception;

    /**
     * Makes this the prompt of the part, replacing the one it had; a prompt of two empty texts removes it.
     */
    void store(ChatMessage part, String builtPrompt, String backgroundLore) throws Exception;

    /**
     * Gives the part the prompt of the other one (what a copy of a part, a branch of it, has), or removes the one it had
     * when the source has none.
     */
    void copy(ChatMessage from, ChatMessage to) throws Exception;

    void softDeleteForManuscript(Manuscript manuscript) throws Exception;

    void softDeleteForParts(Collection<Long> partIds) throws Exception;

    void hardDeleteForParts(Collection<Long> partIds) throws Exception;

}
