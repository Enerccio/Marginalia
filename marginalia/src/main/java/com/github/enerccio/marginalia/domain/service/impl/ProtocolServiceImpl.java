package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatCompletionProtocol;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.repository.ProtocolRepository;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import org.apache.commons.lang3.StringUtils;

public class ProtocolServiceImpl extends ExtendableServiceImpl<Protocol, ProtocolRepository> implements ProtocolService {

    @Override
    @CommonTx
    public Protocol cloneProtocol(Protocol source, String name) throws Exception {
        Protocol original = source == null ? null : findForUser(source.getUuid());
        if (original == null) {
            throw new IllegalArgumentException("The protocol does not exist");
        }
        if (!(original instanceof ChatCompletionProtocol)) {
            throw new IllegalArgumentException("The protocol can't be cloned: " + original.getProtocolType());
        }

        Protocol copy = new ChatCompletionProtocol();
        copy.setProtocolType(original.getProtocolType());
        copy.setName(StringUtils.isBlank(name) ? original.getName() : name);
        copy.setMaxTokens(original.getMaxTokens());
        copy.setReplyTokens(original.getReplyTokens());
        copy.setTemperatureEnabled(original.getTemperatureEnabled());
        copy.setTemperature(original.getTemperature());
        copy.setTopPEnabled(original.getTopPEnabled());
        copy.setTopP(original.getTopP());
        copy.setFrequencyPenaltyEnabled(original.getFrequencyPenaltyEnabled());
        copy.setFrequencyPenalty(original.getFrequencyPenalty());
        copy.setPresencePenaltyEnabled(original.getPresencePenaltyEnabled());
        copy.setPresencePenalty(original.getPresencePenalty());
        if (original.getAttributes() != null) {
            copy.setAttributes(original.getAttributes().deepCopy());
        }
        return save(copy);
    }
}
