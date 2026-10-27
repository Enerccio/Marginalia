package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessagePrompt;
import com.github.enerccio.marginalia.domain.repository.MessagePromptRepository;
import com.github.enerccio.marginalia.domain.service.MessagePromptService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagePromptServiceImpl extends OwnedServiceImpl<MessagePrompt, MessagePromptRepository> implements MessagePromptService {

    @Override
    @CommonTxReadOnly
    public MessagePrompt findFor(ChatMessage part) throws Exception {
        return getRepository().findByPart(part);
    }

    @Override
    @CommonTxReadOnly
    public Map<Long, MessagePrompt> findAllFor(Collection<ChatMessage> parts) throws Exception {
        Map<Long, MessagePrompt> prompts = new HashMap<>();
        List<Long> ids = parts.stream().map(ChatMessage::getId).filter(java.util.Objects::nonNull).toList();
        for (MessagePrompt prompt : getRepository().findAllByPartIds(ids)) {
            prompts.putIfAbsent(prompt.getMessage().getId(), prompt);
        }
        return prompts;
    }

    @Override
    @CommonTxReadOnly
    public String getBuiltPrompt(ChatMessage part) throws Exception {
        MessagePrompt prompt = findFor(part);
        return prompt == null ? null : prompt.getBuiltPrompt();
    }

    @Override
    @CommonTxReadOnly
    public String getBackgroundLore(ChatMessage part) throws Exception {
        MessagePrompt prompt = findFor(part);
        return prompt == null ? null : prompt.getBackgroundLore();
    }

    @Override
    @CommonTx
    public void store(ChatMessage part, String builtPrompt, String backgroundLore) throws Exception {
        if (part == null || part.getId() == null) {
            throw new IllegalArgumentException("The part is not saved");
        }
        MessagePrompt prompt = getRepository().findByPart(part);
        if (StringUtils.isEmpty(builtPrompt) && StringUtils.isEmpty(backgroundLore)) {
            getRepository().hardDeleteByPartIds(List.of(part.getId()));
            return;
        }
        if (prompt == null) {
            prompt = new MessagePrompt();
            prompt.setMessage(part);
            // the owner of the part, whoever stores it (a worker, an agent merge)
            prompt.setOwner(part.getOwner());
        }
        prompt.setBuiltPrompt(builtPrompt);
        prompt.setBackgroundLore(backgroundLore);
        save(prompt);
    }

    @Override
    @CommonTx
    public void copy(ChatMessage from, ChatMessage to) throws Exception {
        if (to == null || to.getId() == null) {
            return;
        }
        MessagePrompt source = from == null || from.getId() == null ? null : getRepository().findByPart(from);
        store(to, source == null ? null : source.getBuiltPrompt(), source == null ? null : source.getBackgroundLore());
    }

    @Override
    @CommonTx
    public void softDeleteForManuscript(Manuscript manuscript) throws Exception {
        getRepository().softDeleteByManuscript(manuscript);
    }

    @Override
    @CommonTx
    public void softDeleteForParts(Collection<Long> partIds) throws Exception {
        getRepository().softDeleteByPartIds(partIds);
    }

    @Override
    @CommonTx
    public void hardDeleteForParts(Collection<Long> partIds) throws Exception {
        getRepository().hardDeleteByPartIds(partIds);
    }
}
