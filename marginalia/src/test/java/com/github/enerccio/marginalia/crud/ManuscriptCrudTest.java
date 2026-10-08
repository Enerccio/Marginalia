package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManuscriptCrudTest extends ExtendableCrudContract<Manuscript> {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private OpenAICompatible ai;
    private Protocol protocol;
    private Lorebook lorebook;

    @BeforeEach
    void createReferences() throws Exception {
        ai = createAI();

        ChatCompletionProtocol p = new ChatCompletionProtocol();
        p.setName("protocol");
        p.setProtocolType(ProtocolType.CHAT_COMPLETION);
        p.setMaxTokens(4096);
        p.setReplyTokens(300);
        protocol = protocolService.save(p);

        Lorebook l = new Lorebook();
        l.setName("lore");
        lorebook = lorebookService.save(l);
    }

    @Override
    protected OwnedService<Manuscript, ?> service() {
        return manuscriptService;
    }

    @Override
    protected Manuscript newEntity() {
        Manuscript manuscript = new Manuscript();
        manuscript.setName("My book");
        manuscript.setDescription("A description");
        manuscript.setAi(ai);
        manuscript.setProtocol(protocol);
        manuscript.setLorebook(lorebook);
        manuscript.setTemplate("{{content}}");
        manuscript.setPov("first person");
        manuscript.setTense("present");
        manuscript.setStyle("terse");
        manuscript.setUserPrompt("user prompt");
        manuscript.setSummaryPrompt("summary prompt");
        manuscript.setShowBookStyles(true);
        manuscript.setBackupStrategy(BackupStrategy.AFTER_N_MESSAGES);
        manuscript.setBackupStrategyValue("10");
        manuscript.setBackupStrategyCurrentValue("3");
        return manuscript;
    }

    @Override
    protected void assertCreated(Manuscript loaded) {
        assertThat(loaded.getName()).isEqualTo("My book");
        assertThat(loaded.getDescription()).isEqualTo("A description");
        assertThat(idOf(loaded.getAi())).isEqualTo(ai.getId());
        assertThat(idOf(loaded.getProtocol())).isEqualTo(protocol.getId());
        assertThat(idOf(loaded.getLorebook())).isEqualTo(lorebook.getId());
        assertThat(loaded.getTemplate()).isEqualTo("{{content}}");
        assertThat(loaded.getPov()).isEqualTo("first person");
        assertThat(loaded.getTense()).isEqualTo("present");
        assertThat(loaded.getStyle()).isEqualTo("terse");
        assertThat(loaded.getUserPrompt()).isEqualTo("user prompt");
        assertThat(loaded.getSummaryPrompt()).isEqualTo("summary prompt");
        assertThat(loaded.getShowBookStyles()).isTrue();
        assertThat(loaded.getBackupStrategy()).isEqualTo(BackupStrategy.AFTER_N_MESSAGES);
        assertThat(loaded.getBackupStrategyValue()).isEqualTo("10");
        assertThat(loaded.getBackupStrategyCurrentValue()).isEqualTo("3");
        assertThat(loaded.isPublished()).isFalse();
        assertThat(loaded.getActiveLeaf()).isNull();
    }

    @Override
    protected void modify(Manuscript entity) {
        entity.setName("Renamed book");
        entity.setDescription("New description");
        entity.setPov(null);
        entity.setBackupStrategy(BackupStrategy.DISABLED);
        entity.setPublished(true);
        entity.setLorebook(null);
    }

    @Override
    protected void assertModified(Manuscript loaded) {
        assertThat(loaded.getName()).isEqualTo("Renamed book");
        assertThat(loaded.getDescription()).isEqualTo("New description");
        assertThat(loaded.getPov()).isNull();
        assertThat(loaded.getTense()).isEqualTo("present");
        assertThat(loaded.getBackupStrategy()).isEqualTo(BackupStrategy.DISABLED);
        assertThat(loaded.isPublished()).isTrue();
        assertThat(loaded.getLorebook()).isNull();
        assertThat(idOf(loaded.getAi())).isEqualTo(ai.getId());
    }

    @Test
    void findViewableReturnsOwnAndPublishedOnly() throws Exception {
        Manuscript priv = create();
        Manuscript published = newEntity();
        published.setPublished(true);
        published = manuscriptService.save(published);
        Manuscript deleted = newEntity();
        deleted.setPublished(true);
        deleted = manuscriptService.save(deleted);
        manuscriptService.delete(deleted, false);

        assertThat(idOf(manuscriptService.findViewable(priv.getUuid()))).isEqualTo(priv.getId());
        assertThat(manuscriptService.findViewable(deleted.getUuid())).isNull();
        assertThat(manuscriptService.findViewable("")).isNull();

        login();
        assertThat(manuscriptService.findViewable(priv.getUuid())).isNull();
        assertThat(idOf(manuscriptService.findViewable(published.getUuid()))).isEqualTo(published.getId());
        assertThat(manuscriptService.findViewable(deleted.getUuid())).isNull();
    }

    @Test
    void markOpenedOnlyTouchesLastOpened() throws Exception {
        Manuscript saved = create();
        Date modification = reload(saved).getModification();
        byte[] content = reload(saved).getExtendedContent();

        manuscriptService.markOpened(saved);

        Manuscript loaded = reload(saved);
        assertThat(loaded.getLastOpened()).isNotNull();
        assertThat(loaded.getModification()).isEqualTo(modification);
        assertThat(loaded.getExtendedContent()).isEqualTo(content);
    }

    @Test
    void markOpenedIgnoresOtherUsersBooks() throws Exception {
        Manuscript saved = create();
        login();

        manuscriptService.markOpened(saved);

        assertThat(reload(saved).getLastOpened()).isNull();
    }

    @Test
    void searchesByNamePrefixAndTags() throws Exception {
        String prefix = uniqueName("Saga");
        Manuscript first = newEntity();
        first.setName(prefix + " one");
        first = manuscriptService.save(first);
        Manuscript second = newEntity();
        second.setName(prefix + " two");
        second = manuscriptService.save(second);
        Manuscript other = newEntity();
        other.setName("Unrelated " + prefix);
        other = manuscriptService.save(other);

        Tag fantasy = newTag("fantasy");
        Tag dark = newTag("dark");
        tagRelationService.createRelation(fantasy, first);
        tagRelationService.createRelation(dark, first);
        tagRelationService.createRelation(fantasy, second);

        ManuscriptFilterValues byName = new ManuscriptFilterValues();
        byName.setName(prefix);
        assertThat(manuscriptService.searchManuscripts(byName, Sorter.sorter("name", Sorter.Ordering.DESC)))
                .containsExactly(second.getId(), first.getId());

        ManuscriptFilterValues byOneTag = new ManuscriptFilterValues();
        byOneTag.setTags(List.of("fantasy"));
        assertThat(manuscriptService.searchManuscripts(byOneTag)).containsExactlyInAnyOrder(first.getId(), second.getId());

        ManuscriptFilterValues byAllTags = new ManuscriptFilterValues();
        byAllTags.setTags(List.of("fantasy", "dark"));
        assertThat(manuscriptService.searchManuscripts(byAllTags)).containsExactly(first.getId());

        assertThat(manuscriptService.searchManuscripts()).contains(first.getId(), second.getId(), other.getId());

        login();
        assertThat(manuscriptService.searchManuscripts(byName)).isEmpty();
    }

    @Test
    void searchTreatsWildcardsLiterally() throws Exception {
        String base = uniqueName("Pct");
        Manuscript literal = newEntity();
        literal.setName(base + "_%x");
        literal = manuscriptService.save(literal);
        Manuscript similar = newEntity();
        similar.setName(base + "ab");
        manuscriptService.save(similar);

        ManuscriptFilterValues filter = new ManuscriptFilterValues();
        filter.setName(base + "_%");
        assertThat(manuscriptService.searchManuscripts(filter)).containsExactly(literal.getId());
    }

    @Test
    void promptSettingsFallBackToUserSettingsAndDefaults() throws Exception {
        Manuscript empty = new Manuscript();
        empty.setName("empty");
        empty = manuscriptService.save(empty);

        assertThat(manuscriptService.getPov(empty)).isEqualTo(Defaults.DEFAULT_POV);
        assertThat(manuscriptService.getTense(empty)).isEqualTo(Defaults.DEFAULT_TENSE);
        assertThat(manuscriptService.getUserPrompt(empty)).isEqualTo(Defaults.DEFAULT_USER_PROMPT);
        assertThat(manuscriptService.getSummaryPrompt(empty)).isEqualTo(Defaults.DEFAULT_SUMMARY_PROMPT);
        assertThat(manuscriptService.getMasterTemplate(empty)).isEqualTo(Defaults.DEFAULT_MASTER_TEMPLATE);
        assertThat(manuscriptService.getBackupStrategy(empty)).isNull();

        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        settings.setDefaultPov("user pov");
        settings.setDefaultSummaryPrompt("user summary");
        settings.setBackupStrategy(BackupStrategy.AFTER_N_MINUTES);
        settings.setBackupStrategyValue("15");
        settingService.save(settings);

        assertThat(manuscriptService.getPov(empty)).isEqualTo("user pov");
        assertThat(manuscriptService.getSummaryPrompt(empty)).isEqualTo("user summary");
        assertThat(manuscriptService.getTense(empty)).isEqualTo(Defaults.DEFAULT_TENSE);
        assertThat(manuscriptService.getBackupStrategy(empty)).isEqualTo(BackupStrategy.AFTER_N_MINUTES);
        assertThat(manuscriptService.getBackupStrategyValue(empty)).isEqualTo("15");

        Manuscript own = create();
        assertThat(manuscriptService.getPov(own)).isEqualTo("first person");
        assertThat(manuscriptService.getUserPrompt(own)).isEqualTo("user prompt");
        assertThat(manuscriptService.getSummaryPrompt(own)).isEqualTo("summary prompt");
        assertThat(manuscriptService.getMasterTemplate(own)).isEqualTo("{{content}}");
        assertThat(manuscriptService.getBackupStrategyValue(own)).isEqualTo("10");
    }

    @Test
    void userSettingFallbackIsPerUser() throws Exception {
        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        settings.setDefaultPov("owner pov");
        settingService.save(settings);

        User other = login();
        Manuscript empty = new Manuscript();
        empty.setName("other's book");
        empty = manuscriptService.save(empty);

        assertThat(empty.getOwner().getId()).isEqualTo(other.getId());
        assertThat(manuscriptService.getPov(empty)).isEqualTo(Defaults.DEFAULT_POV);
    }

    private Tag newTag(String value) throws Exception {
        Tag tag = new Tag();
        tag.setValue(value);
        return tagService.save(tag);
    }
}
