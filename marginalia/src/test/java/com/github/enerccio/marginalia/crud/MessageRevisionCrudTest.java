package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class MessageRevisionCrudTest extends ExtendableCrudContract<MessageRevision> {

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Override
    protected OwnedService<MessageRevision, ?> service() {
        return messageRevisionService;
    }

    @Override
    protected MessageRevision newEntity() {
        MessageRevision revision = new MessageRevision();
        revision.setSource(RevisionSource.REGENERATE);
        revision.setAgent(true);
        revision.setSourceDetail("model · protocol");
        revision.setLabel("before the duel");
        revision.setWordCount(4);
        revision.setTokenCount(6L);
        revision.setResponse("Anna met John at the bar.");
        revision.setResponseReasoning("They should meet early.");
        return revision;
    }

    @Override
    protected void assertCreated(MessageRevision loaded) {
        assertThat(loaded.getSource()).isEqualTo(RevisionSource.REGENERATE);
        assertThat(loaded.isAgent()).isTrue();
        assertThat(loaded.getSourceDetail()).isEqualTo("model · protocol");
        assertThat(loaded.getLabel()).isEqualTo("before the duel");
        assertThat(loaded.getWordCount()).isEqualTo(4);
        assertThat(loaded.getTokenCount()).isEqualTo(6L);
        assertThat(loaded.getResponse()).isEqualTo("Anna met John at the bar.");
        assertThat(loaded.getResponseReasoning()).isEqualTo("They should meet early.");
        assertThat(loaded.get_fulltext()).contains("Anna met John");
    }

    @Override
    protected void modify(MessageRevision entity) {
        entity.setResponse("Anna met John at the market.");
        entity.setResponseReasoning(null);
        entity.setLabel(null);
        entity.setAgent(false);
    }

    @Override
    protected void assertModified(MessageRevision loaded) {
        assertThat(loaded.getResponse()).isEqualTo("Anna met John at the market.");
        assertThat(loaded.getResponseReasoning()).isNull();
        assertThat(loaded.getLabel()).isNull();
        assertThat(loaded.isAgent()).isFalse();
        assertThat(loaded.getSource()).isEqualTo(RevisionSource.REGENERATE);
    }
}
