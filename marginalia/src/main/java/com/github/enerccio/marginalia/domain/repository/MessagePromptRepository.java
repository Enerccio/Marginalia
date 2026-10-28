package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessagePrompt;

import java.util.Collection;
import java.util.List;

public interface MessagePromptRepository extends OwnedRepository<MessagePrompt> {

    /**
     * The prompt of the part, null when it has none.
     */
    MessagePrompt findByPart(ChatMessage part) throws Exception;

    /**
     * The prompts of the parts with these ids (the parts without one are not in the list).
     */
    List<MessagePrompt> findAllByPartIds(Collection<Long> partIds) throws Exception;

    int softDeleteByManuscript(Manuscript manuscript) throws Exception;

    int softDeleteByPartIds(Collection<Long> partIds) throws Exception;

    int hardDeleteByPartIds(Collection<Long> partIds) throws Exception;

}
