package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.impl.ChatCompletionProtocol;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ProtocolCrudTest extends ExtendableCrudContract<Protocol> {

    @Autowired
    private ProtocolService protocolService;

    @Override
    protected OwnedService<Protocol, ?> service() {
        return protocolService;
    }

    @Override
    protected Protocol newEntity() {
        ChatCompletionProtocol protocol = new ChatCompletionProtocol();
        protocol.setName("Creative");
        protocol.setProtocolType(ProtocolType.CHAT_COMPLETION);
        protocol.setMaxTokens(4096);
        protocol.setReplyTokens(300);
        protocol.setTemperatureEnabled(true);
        protocol.setTemperature(0.9);
        protocol.setTopPEnabled(false);
        protocol.setTopP(0.95);
        protocol.setFrequencyPenaltyEnabled(true);
        protocol.setFrequencyPenalty(-0.5);
        protocol.setPresencePenaltyEnabled(null);
        protocol.setPresencePenalty(null);
        return protocol;
    }

    @Override
    protected void assertCreated(Protocol loaded) {
        assertThat(loaded).isInstanceOf(ChatCompletionProtocol.class);
        assertThat(loaded.getName()).isEqualTo("Creative");
        assertThat(loaded.getProtocolType()).isEqualTo(ProtocolType.CHAT_COMPLETION);
        assertThat(loaded.getMaxTokens()).isEqualTo(4096);
        assertThat(loaded.getReplyTokens()).isEqualTo(300);
        assertThat(loaded.getTemperatureEnabled()).isTrue();
        assertThat(loaded.getTemperature()).isEqualTo(0.9);
        assertThat(loaded.getTopPEnabled()).isFalse();
        assertThat(loaded.getTopP()).isEqualTo(0.95);
        assertThat(loaded.getFrequencyPenaltyEnabled()).isTrue();
        assertThat(loaded.getFrequencyPenalty()).isEqualTo(-0.5);
        // unset flags read as disabled
        assertThat(loaded.getPresencePenaltyEnabled()).isFalse();
        assertThat(loaded.getPresencePenalty()).isNull();
    }

    @Override
    protected void modify(Protocol entity) {
        entity.setName("Precise");
        entity.setTemperature(0.1);
        entity.setPresencePenaltyEnabled(true);
        entity.setPresencePenalty(1.25);
    }

    @Override
    protected void assertModified(Protocol loaded) {
        assertThat(loaded.getName()).isEqualTo("Precise");
        assertThat(loaded.getTemperature()).isEqualTo(0.1);
        assertThat(loaded.getPresencePenaltyEnabled()).isTrue();
        assertThat(loaded.getPresencePenalty()).isEqualTo(1.25);
        assertThat(loaded.getTopP()).isEqualTo(0.95);
    }
}
