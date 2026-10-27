package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.ActivationContext;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Reason;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService.Verdict;
import com.github.enerccio.marginalia.domain.service.impl.LorebookActivationServiceImpl;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationProperties;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables.Scope;
import com.github.enerccio.marginalia.test.ExpectedLog;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Lorebook activation in a real generation ({@code ProcessLorebookStep}): which lorebooks and entries take part, tag
 * and text/regex filtering, ordering, insertion modes and payload templating. Results are read from the generation
 * state after {@link Events#AFTER_PROCESS_LOREBOOK} and from the request the mock LLM received.
 */
class LorebookActivationTest extends GenerationTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    /**
     * What the lorebook step produced.
     *
     * @param activated  names of activated entries, in activation order
     * @param lore       content of the lore block (system prompt)
     * @param userLore   content inserted before the user prompt
     * @param userPrompt processed user prompt after lore insertion
     */
    record Lore(List<String> activated, String lore, String userLore, String userPrompt, List<String> lorebooks,
                LorebookTemplateData templateData) {
    }

    // ------------------------------------------------------------------------------------------------------------
    // fixtures
    // ------------------------------------------------------------------------------------------------------------

    private Lorebook lorebook(String name, Lorebook... subbooks) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setSubbooks(new ArrayList<>(List.of(subbooks)));
        return lorebookService.save(lorebook);
    }

    private Lorebook addSubbooks(Lorebook lorebook, Lorebook... subbooks) throws Exception {
        Lorebook loaded = lorebookService.find(lorebook.getId());
        loaded.getSubbooks().addAll(List.of(subbooks));
        return lorebookService.save(loaded);
    }

    private LorebookEntry entry(Lorebook lorebook, String name, Consumer<LorebookEntry> setup) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload("<" + name + ">");
        setup.accept(entry);
        return lorebookEntryService.save(entry);
    }

    private LorebookEntry entry(Lorebook lorebook, String name) throws Exception {
        return entry(lorebook, name, _ -> {
        });
    }

    private Tag tag(String value) throws Exception {
        Tag tag = new Tag();
        tag.setValue(value);
        return tagService.save(tag);
    }

    private Manuscript book(Lorebook lorebook, String... tags) throws Exception {
        Manuscript manuscript = newManuscript();
        manuscript.setLorebook(lorebook);
        manuscript = manuscriptService.save(manuscript);
        for (String value : tags) {
            tagRelationService.createRelation(tag(value), manuscript);
        }
        return manuscript;
    }

    @SuppressWarnings("unchecked")
    private Lore activate(Manuscript manuscript, TurnInput input) throws Exception {
        AtomicReference<Lore> captured = new AtomicReference<>();
        onEvent(Events.AFTER_PROCESS_LOREBOOK, manuscript, e -> {
            List<LorebookEntry> entries = e.getProperty(GenerationProperties.ACTIVATED_LOREBOOK_ENTRIES);
            List<Lorebook> lorebooks = e.getProperty(GenerationProperties.LOREBOOKS);
            captured.set(new Lore(
                    entries == null ? List.of() : entries.stream().map(LorebookEntry::getName).toList(),
                    e.getPrePromptData().getBackgroundLore(),
                    e.getPrePromptData().getBackgroundUserLore(),
                    e.getPrePromptData().getUserPromptProcessed(),
                    lorebooks == null ? List.of() : lorebooks.stream().map(Lorebook::getName).toList(),
                    e.getProperty(GenerationProperties.LOREBOOK_TEMPLATE_DATA)));
        });

        GenerationRun run = generate(manuscript, input);

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getErrors()).isEmpty();
        assertThat(captured.get()).as("AFTER_PROCESS_LOREBOOK reached").isNotNull();
        return captured.get();
    }

    private Lore activate(Manuscript manuscript, String instructions) throws Exception {
        return activate(manuscript, new TurnInput(null, null, null, instructions));
    }

    private Lore activate(Manuscript manuscript) throws Exception {
        return activate(manuscript, "Continue.");
    }

    // ------------------------------------------------------------------------------------------------------------
    // tests
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void manuscriptWithoutLorebook() throws Exception {
        Manuscript manuscript = book(null);

        Lore lore = activate(manuscript);

        assertThat(lore.activated()).isEmpty();
        assertThat(lore.lore()).isNull();
        assertThat(lore.userPrompt()).isEqualTo("Continue.");
    }

    @Test
    void entriesWithoutConditionsAreAlwaysActive() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "a");
        entry(book, "b");

        Lore lore = activate(book(book));

        assertThat(lore.activated()).containsExactlyInAnyOrder("a", "b");
    }

    // ---- entry state -----------------------------------------------------------------------------------------

    @Test
    void disabledEntriesAreSkipped() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "on");
        entry(book, "off", e -> e.setEnabled(false));

        assertThat(activate(book(book)).activated()).containsExactly("on");
    }

    @Test
    void deletedEntriesAreSkipped() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "kept");
        lorebookEntryService.delete(entry(book, "deleted"), false);

        assertThat(activate(book(book)).activated()).containsExactly("kept");
    }

    @Test
    void blankPayloadIsActivatedButAddsNothing() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "empty", e -> e.setPayload("   "));
        entry(book, "full", e -> e.setOrder(200));

        Lore lore = activate(book(book));

        assertThat(lore.activated()).containsExactly("empty", "full");
        assertThat(lore.lore()).isEqualTo("<full>");
    }

    // ---- lorebooks -------------------------------------------------------------------------------------------

    @Test
    void disabledRootLorebookContributesNothing() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "a");
        Lorebook loaded = lorebookService.find(book.getId());
        loaded.setEnabled(false);
        lorebookService.save(loaded);

        Lore lore = activate(book(book));

        assertThat(lore.activated()).isEmpty();
        assertThat(lore.lorebooks()).isEmpty();
        assertThat(lore.lore()).isEmpty();
    }

    @Test
    void subbookEntriesAreIncluded() throws Exception {
        Lorebook leaf = lorebook("leaf");
        entry(leaf, "leaf entry");
        Lorebook middle = lorebook("middle", leaf);
        entry(middle, "middle entry");
        Lorebook root = lorebook("root", middle);
        entry(root, "root entry");

        Lore lore = activate(book(root));

        assertThat(lore.lorebooks()).containsExactly("root", "middle", "leaf");
        assertThat(lore.activated()).containsExactlyInAnyOrder("root entry", "middle entry", "leaf entry");
    }

    @Test
    void disabledSubbookIsSkippedWithItsSubbooks() throws Exception {
        Lorebook deep = lorebook("deep");
        entry(deep, "deep entry");
        Lorebook off = lorebook("off", deep);
        entry(off, "off entry");
        Lorebook loaded = lorebookService.find(off.getId());
        loaded.setEnabled(false);
        lorebookService.save(loaded);
        Lorebook root = lorebook("root", off);
        entry(root, "root entry");

        Lore lore = activate(book(root));

        assertThat(lore.lorebooks()).containsExactly("root");
        assertThat(lore.activated()).containsExactly("root entry");
    }

    @Test
    void deletedSubbookIsSkipped() throws Exception {
        Lorebook gone = lorebook("gone");
        entry(gone, "gone entry");
        Lorebook root = lorebook("root", gone);
        lorebookService.delete(lorebookService.find(gone.getId()), false);

        assertThat(activate(book(root)).activated()).isEmpty();
    }

    @Test
    void sharedSubbookIsProcessedOnce() throws Exception {
        Lorebook shared = lorebook("shared");
        entry(shared, "shared entry");
        Lorebook a = lorebook("a", shared);
        Lorebook b = lorebook("b", shared);
        Lorebook root = lorebook("root", a, b);

        Lore lore = activate(book(root));

        assertThat(lore.lorebooks()).containsExactly("root", "a", "b", "shared");
        assertThat(lore.activated()).containsExactly("shared entry");
        assertThat(lore.lore()).isEqualTo("<shared entry>");
    }

    @Test
    void cyclicSubbooksTerminate() throws Exception {
        Lorebook a = lorebook("a");
        entry(a, "a entry");
        Lorebook b = lorebook("b", a);
        entry(b, "b entry");
        addSubbooks(a, b);

        Lore lore = activate(book(a));

        assertThat(lore.lorebooks()).containsExactly("a", "b");
        assertThat(lore.activated()).containsExactlyInAnyOrder("a entry", "b entry");
    }

    @Test
    void lorebookCanBeItsOwnSubbook() throws Exception {
        Lorebook self = lorebook("self");
        entry(self, "entry");
        addSubbooks(self, self);

        assertThat(activate(book(self)).activated()).containsExactly("entry");
    }

    // ---- tags ------------------------------------------------------------------------------------------------

    @Test
    void taggedEntryNeedsMatchingManuscriptTag() throws Exception {
        Lorebook book = lorebook("world");
        LorebookEntry dragons = entry(book, "dragons");
        tagRelationService.createRelation(tag("fantasy"), dragons);
        entry(book, "untagged");

        assertThat(activate(book(book)).activated()).containsExactly("untagged");
        assertThat(activate(book(book, "fantasy")).activated()).containsExactlyInAnyOrder("dragons", "untagged");
        assertThat(activate(book(book, "scifi")).activated()).containsExactly("untagged");
    }

    @Test
    void anyOfSeveralTagsIsEnough() throws Exception {
        Lorebook book = lorebook("world");
        LorebookEntry entry = entry(book, "creatures");
        tagRelationService.createRelation(tag("fantasy"), entry);
        tagRelationService.createRelation(tag("horror"), entry);

        assertThat(activate(book(book, "horror", "romance")).activated()).containsExactly("creatures");
    }

    @Test
    void tagMatchIsExact() throws Exception {
        Lorebook book = lorebook("world");
        tagRelationService.createRelation(tag("Fantasy"), entry(book, "dragons"));

        assertThat(activate(book(book, "fantasy")).activated()).isEmpty();
        assertThat(activate(book(book, "Fantasy")).activated()).containsExactly("dragons");
    }

    @Test
    void negativeTagExcludesEntry() throws Exception {
        Lorebook book = lorebook("world");
        tagRelationService.createRelation(tag("grimdark"), entry(book, "kittens"), true);

        assertThat(activate(book(book)).activated()).containsExactly("kittens");
        assertThat(activate(book(book, "grimdark")).activated()).isEmpty();
    }

    @Test
    void negativeTagWinsOverPositiveTag() throws Exception {
        Lorebook book = lorebook("world");
        LorebookEntry entry = entry(book, "mixed");
        tagRelationService.createRelation(tag("fantasy"), entry);
        tagRelationService.createRelation(tag("comedy"), entry, true);

        assertThat(activate(book(book, "fantasy")).activated()).containsExactly("mixed");
        assertThat(activate(book(book, "fantasy", "comedy")).activated()).isEmpty();
    }

    @Test
    void lorebookTagsApplyToItsEntries() throws Exception {
        Lorebook magic = lorebook("magic");
        tagRelationService.createRelation(tag("fantasy"), magic);
        entry(magic, "spells");
        Lorebook root = lorebook("root", magic);
        entry(root, "always");

        assertThat(activate(book(root)).activated()).containsExactly("always");
        assertThat(activate(book(root, "fantasy")).activated()).containsExactlyInAnyOrder("always", "spells");
    }

    @Test
    void lorebookTagIsAddedToEntryTags() throws Exception {
        Lorebook magic = lorebook("magic");
        tagRelationService.createRelation(tag("fantasy"), magic);
        tagRelationService.createRelation(tag("ritual"), entry(magic, "rites"));

        assertThat(activate(book(magic, "ritual")).activated()).containsExactly("rites");
        assertThat(activate(book(magic, "fantasy")).activated()).containsExactly("rites");
    }

    @Test
    void deletedTagRelationsDoNotCount() throws Exception {
        Lorebook book = lorebook("world");
        LorebookEntry entry = entry(book, "dragons");
        Tag fantasy = tag("fantasy");
        tagRelationService.createRelation(fantasy, entry);
        tagRelationService.removeRelation(fantasy, entry);

        assertThat(activate(book(book)).activated()).containsExactly("dragons");
    }

    // ---- filtering -------------------------------------------------------------------------------------------

    @Test
    void textFilterIsCaseInsensitiveSubstring() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "dragon", e -> e.setFiltering("Dragon"));
        Manuscript manuscript = book(book);

        assertThat(activate(manuscript, "a DRAGONFLY lands").activated()).containsExactly("dragon");
        assertThat(activate(manuscript, "a wyvern lands").activated()).isEmpty();
    }

    @Test
    void textFilterIsLiteral() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "literal", e -> e.setFiltering("a.b"));
        Manuscript manuscript = book(book);

        assertThat(activate(manuscript, "xa.bx").activated()).containsExactly("literal");
        assertThat(activate(manuscript, "axb").activated()).isEmpty();
    }

    @Test
    void keywordsOrFilter() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "varro", e -> {
            e.setFiltering(" Captain ,Varro,, ");
            e.setFilteringMode(FilteringMode.KEYWORDS_OR);
        });
        Manuscript manuscript = book(book);

        assertThat(activate(manuscript, "the CAPTAIN arrives").activated()).containsExactly("varro");
        assertThat(activate(manuscript, "Varro waits").activated()).containsExactly("varro");
        assertThat(activate(manuscript, "a sailor").activated()).isEmpty();
    }

    @Test
    void keywordsAndFilter() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "varro", e -> {
            e.setFiltering("Captain, Varro");
            e.setFilteringMode(FilteringMode.KEYWORDS_AND);
        });
        Manuscript manuscript = book(book);

        assertThat(activate(manuscript, "Captain Varro arrives").activated()).containsExactly("varro");
        assertThat(activate(manuscript, "Varro, the captain").activated()).containsExactly("varro");
        assertThat(activate(manuscript, "The captain arrives").activated()).isEmpty();
    }

    @Test
    void keywordsFilterWithoutKeywordsNeverMatches() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "empty", e -> {
            e.setFiltering(" , ");
            e.setFilteringMode(FilteringMode.KEYWORDS_OR);
        });

        assertThat(activate(book(book), "anything, at all").activated()).isEmpty();
    }

    @Test
    void regexFilter() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "royalty", e -> {
            e.setFiltering("\\b(king|queen)s?\\b");
            e.setFilteringMode(FilteringMode.REGEX);
        });
        Manuscript manuscript = book(book);

        assertThat(activate(manuscript, "The QUEEN arrives").activated()).containsExactly("royalty");
        assertThat(activate(manuscript, "Two kings meet").activated()).containsExactly("royalty");
        assertThat(activate(manuscript, "A kingdom falls").activated()).isEmpty();
    }

    @Test
    void regexMatchesAcrossLines() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "span", e -> {
            e.setFiltering("start.*end");
            e.setFilteringMode(FilteringMode.REGEX);
        });

        assertThat(activate(book(book), "start\nmiddle\nend").activated()).containsExactly("span");
    }

    @Test
    void invalidRegexDeactivatesOnlyThatEntry() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "broken", e -> {
            e.setFiltering("([unclosed");
            e.setFilteringMode(FilteringMode.REGEX);
        });
        entry(book, "fine");

        try (ExpectedLog log = ExpectedLog.capture(LorebookActivationServiceImpl.class)) {
            assertThat(activate(book(book), "([unclosed").activated()).containsExactly("fine");

            assertThat(log.warnings()).singleElement().asString().contains("([unclosed").contains("broken");
        }
    }

    @Test
    void filterSeesRenderedUserPrompt() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "harbour", e -> e.setFiltering("harbour"));
        Manuscript manuscript = newManuscript();
        manuscript.setLorebook(book);
        manuscript.setUserPrompt("{{instructions}} Scene: {{sceneSetting}}");
        manuscript = manuscriptService.save(manuscript);

        assertThat(activate(manuscript, new TurnInput("Rainy harbour", null, null, "Walk.")).activated())
                .containsExactly("harbour");
        assertThat(activate(manuscript, new TurnInput("Forest", null, null, "Walk.")).activated()).isEmpty();
    }

    @Test
    void filterDoesNotSeeLorePayloads() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "mentions castle", e -> e.setPayload("The castle is old."));
        entry(book, "about castle", e -> e.setFiltering("castle"));

        assertThat(activate(book(book), "Walk.").activated()).containsExactly("mentions castle");
    }

    @Test
    void filterAndTagsMustBothMatch() throws Exception {
        Lorebook book = lorebook("world");
        LorebookEntry entry = entry(book, "both", e -> e.setFiltering("dragon"));
        tagRelationService.createRelation(tag("fantasy"), entry);

        assertThat(activate(book(book, "fantasy"), "a dragon").activated()).containsExactly("both");
        assertThat(activate(book(book, "fantasy"), "a horse").activated()).isEmpty();
        assertThat(activate(book(book), "a dragon").activated()).isEmpty();
    }

    // ---- ordering and insertion ------------------------------------------------------------------------------

    @Test
    void entriesAreOrderedByOrderAcrossLorebooks() throws Exception {
        Lorebook sub = lorebook("sub");
        entry(sub, "sub 5", e -> e.setOrder(5));
        entry(sub, "sub 50", e -> e.setOrder(50));
        Lorebook root = lorebook("root", sub);
        entry(root, "root 30", e -> e.setOrder(30));
        entry(root, "root 1", e -> e.setOrder(1));

        Lore lore = activate(book(root));

        assertThat(lore.activated()).containsExactly("root 1", "sub 5", "root 30", "sub 50");
        assertThat(lore.lore()).isEqualTo("<root 1>\n\n<sub 5>\n\n<root 30>\n\n<sub 50>");
    }

    @Test
    void sameOrderKeepsCreationOrder() throws Exception {
        Lorebook book = lorebook("world");
        for (String name : List.of("first", "second", "third")) {
            entry(book, name);
            Thread.sleep(5);
        }

        assertThat(activate(book(book)).activated()).containsExactly("first", "second", "third");
    }

    @Test
    void insertionModes() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "lore 1", e -> e.setOrder(1));
        entry(book, "prompt 2", e -> {
            e.setOrder(2);
            e.setInsertionMode(InsertionMode.BEFORE_USER_PROMPT);
        });
        entry(book, "lore 3", e -> e.setOrder(3));
        entry(book, "prompt 4", e -> {
            e.setOrder(4);
            e.setInsertionMode(InsertionMode.BEFORE_USER_PROMPT);
        });

        Lore lore = activate(book(book), "Write the scene.");

        assertThat(lore.lore()).isEqualTo("<lore 1>\n\n<lore 3>");
        assertThat(lore.userLore()).isEqualTo("<prompt 2>\n\n<prompt 4>");
        assertThat(lore.userPrompt()).isEqualTo("<prompt 2>\n\n<prompt 4>\n\nWrite the scene.");
    }

    @Test
    void withoutUserLoreThePromptIsUnchanged() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "lore");

        Lore lore = activate(book(book), "Write.");

        assertThat(lore.userLore()).isEmpty();
        assertThat(lore.userPrompt()).isEqualTo("Write.");
    }

    @Test
    void activatedLoreReachesTheModel() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "Dragons breathe fire.", e -> e.setPayload("Dragons breathe fire."));
        entry(book, "hint", e -> {
            e.setPayload("Remember the dragon.");
            e.setInsertionMode(InsertionMode.BEFORE_USER_PROMPT);
        });

        activate(book(book), "Write the attack.");

        MockLLMRequest request = llm.lastCompletionRequest();
        assertThat(request.getMessages("system")).containsExactly("Dragons breathe fire.");
        assertThat(request.getLastMessage().role()).isEqualTo("user");
        assertThat(request.getLastMessage().content()).isEqualTo("Remember the dragon.\n\nWrite the attack.");
    }

    // ---- templating ------------------------------------------------------------------------------------------

    @Test
    void payloadsAreRenderedWithTurnData() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "pov", e -> e.setPayload("{{user}} is in {{sceneSetting}}; others: {{notChar}}"));

        Lore lore = activate(book(book), new TurnInput("the harbour", "Alice", "Alice, Bob", "Walk."));

        assertThat(lore.lore()).isEqualTo("Alice is in the harbour; others: Bob");
    }

    @Test
    void entriesShareVariablesInActivationOrder() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "reader", e -> {
            e.setOrder(20);
            e.setPayload("mood is {{getvar::mood}}");
        });
        entry(book, "writer", e -> {
            e.setOrder(10);
            e.setPayload("{{setvar::mood::grim}}");
        });

        Lore lore = activate(book(book));

        assertThat(lore.activated()).containsExactly("writer", "reader");
        // the writer renders to nothing and adds no empty paragraph
        assertThat(lore.lore()).isEqualTo("mood is grim");
        assertThat(lore.templateData().templateContext().getVariables().get(Scope.LOCAL, "mood")).isEqualTo("grim");
    }

    @Test
    void inactiveEntriesHaveNoSideEffects() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "inactive", e -> {
            e.setOrder(10);
            e.setEnabled(false);
            e.setPayload("{{setvar::ran::yes}}");
        });
        entry(book, "check", e -> {
            e.setOrder(20);
            e.setPayload("ran={{if {{hasvar::ran}} }}yes{{else}}no{{/if}}");
        });

        assertThat(activate(book(book)).lore()).isEqualTo("ran=no");
    }

    @Test
    void brokenTemplateIsUsedRawAndDoesNotFailGeneration() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "broken", e -> {
            e.setOrder(1);
            e.setPayload("{{#each}} raw {{user}}");
        });
        entry(book, "fine", e -> e.setOrder(2));

        try (ExpectedLog log = ExpectedLog.capture(LorebookActivationServiceImpl.class)) {
            Lore lore = activate(book(book));

            assertThat(lore.lore()).isEqualTo("{{#each}} raw {{user}}\n\n<fine>");
            assertThat(log.warnings()).singleElement().asString().contains("broken");
        }
    }

    @Test
    void importedSillyTavernKeysActivateEntries() throws Exception {
        String worldInfo = """
                {"name": "ST world", "entries": {
                  "0": {"uid": 0, "comment": "always", "content": "<always>", "key": [], "order": 1},
                  "1": {"uid": 1, "comment": "dragons", "content": "<dragons>", "key": ["dragon", "wyrm"], "order": 2},
                  "2": {"uid": 2, "comment": "crowned king", "content": "<crowned king>", "key": ["king"],
                        "keysecondary": ["crown"], "selective": true, "selectiveLogic": 3, "order": 3},
                  "3": {"uid": 3, "comment": "living king", "content": "<living king>", "key": ["king"],
                        "keysecondary": ["dead"], "selectiveLogic": 2, "order": 4, "matchWholeWords": true},
                  "4": {"uid": 4, "comment": "hint", "content": "<hint>", "key": ["/storm(s)?/i"], "order": 5,
                        "position": 4},
                  "5": {"uid": 5, "comment": "constant", "content": "<constant>", "key": ["nothing"], "constant": true,
                        "order": 6},
                  "6": {"uid": 6, "comment": "off", "content": "<off>", "disable": true, "order": 7}
                }}
                """;
        Lorebook imported = lorebookService.importFromSillytavern(worldInfo, "upload.json");
        Manuscript manuscript = book(imported);

        assertThat(activate(manuscript, "A quiet morning.").activated()).containsExactly("always", "constant");
        assertThat(activate(manuscript, "A WYRM circles.").activated()).containsExactly("always", "dragons", "constant");
        assertThat(activate(manuscript, "The king takes the crown.").activated())
                .containsExactly("always", "crowned king", "living king", "constant");
        assertThat(activate(manuscript, "The king is dead.").activated()).containsExactly("always", "constant");
        assertThat(activate(manuscript, "The kingdom waits.").activated()).containsExactly("always", "constant");

        Lore lore = activate(manuscript, "Storms gather.");
        assertThat(lore.activated()).containsExactly("always", "hint", "constant");
        assertThat(lore.lore()).isEqualTo("<always>\n\n<constant>");
        assertThat(lore.userLore()).isEqualTo("<hint>");
    }

    @Test
    void extensionCanChangeActivatedEntries() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "keep", e -> e.setOrder(1));
        entry(book, "drop", e -> e.setOrder(2));
        Manuscript manuscript = book(book);
        onEvent(Events.PROCESS_ACTIVATED_ENTRIES, manuscript, e -> {
            List<LorebookEntry> entries = e.getProperty(GenerationProperties.ACTIVATED_LOREBOOK_ENTRIES);
            entries.removeIf(entry -> entry.getName().equals("drop"));
        });

        Lore lore = activate(manuscript);

        assertThat(lore.lore()).isEqualTo("<keep>");
    }

    @Test
    void verdictsListEveryEntryWithTheReason() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "always", e -> e.setOrder(1));
        entry(book, "hit", e -> {
            e.setOrder(2);
            e.setFiltering("dragon");
        });
        entry(book, "miss", e -> {
            e.setOrder(3);
            e.setFiltering("unicorn");
        });
        entry(book, "off", e -> {
            e.setOrder(4);
            e.setEnabled(false);
        });
        Manuscript manuscript = book(book);
        AtomicReference<List<LorebookEntryVerdict>> captured = new AtomicReference<>();
        onEvent(Events.PROCESS_ACTIVATED_ENTRIES, manuscript, e -> {
            List<LorebookEntryVerdict> verdicts = e.getProperty(GenerationProperties.LOREBOOK_VERDICTS);
            captured.set(new ArrayList<>(verdicts));
            // a listener changing the activated entries does not rewrite the record of the decisions
            List<LorebookEntry> entries = e.getProperty(GenerationProperties.ACTIVATED_LOREBOOK_ENTRIES);
            entries.removeIf(entry -> entry.getName().equals("always"));
        });

        Lore lore = activate(manuscript, "A dragon appears.");

        assertThat(lore.activated()).containsExactly("hit");
        assertThat(captured.get()).extracting(v -> v.entry().getName(), v -> v.verdict().reason(),
                        v -> v.verdict().detail())
                .containsExactly(
                        tuple("always", Reason.CONSTANT, null),
                        tuple("hit", Reason.FILTER_MATCHED, "dragon"),
                        tuple("miss", Reason.FILTER_NOT_MATCHED, "unicorn"),
                        tuple("off", Reason.DISABLED, null));
    }

    @Test
    void everyEntryIsOfferedToExtensions() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "on");
        entry(book, "off", e -> e.setEnabled(false));
        Manuscript manuscript = book(book);
        List<String> seen = new java.util.concurrent.CopyOnWriteArrayList<>();
        onEvent(Events.PROCESS_LOREBOOK_ENTRY, manuscript,
                e -> seen.add(((LorebookEntry) e.getProperty(GenerationProperties.LOREBOOK_ENTRY)).getName()));

        activate(manuscript);

        assertThat(seen).containsExactlyInAnyOrder("on", "off");
    }

    @Test
    void extensionCanChangeTheVerdictOfAnEntry() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "plain", e -> e.setOrder(1));
        entry(book, "forcedOut", e -> e.setOrder(2));
        entry(book, "forcedIn", e -> {
            e.setOrder(3);
            e.setFiltering("unicorn");
        });
        entry(book, "off", e -> {
            e.setOrder(4);
            e.setEnabled(false);
        });
        Manuscript manuscript = book(book);
        AtomicReference<List<LorebookEntryVerdict>> captured = new AtomicReference<>();
        onEvent(Events.PROCESS_LOREBOOK_ENTRY, manuscript, e -> {
            String name = ((LorebookEntry) e.getProperty(GenerationProperties.LOREBOOK_ENTRY)).getName();
            if (name.equals("forcedOut")) {
                assertThat(((Verdict) e.getProperty(GenerationProperties.LOREBOOK_VERDICT)).reason())
                        .isEqualTo(Reason.CONSTANT);
                e.getProperties().put(GenerationProperties.LOREBOOK_VERDICT,
                        new Verdict(Reason.EXTENSION_SKIPPED, "spoiler"));
            } else if (name.equals("forcedIn") || name.equals("off")) {
                e.getProperties().put(GenerationProperties.LOREBOOK_VERDICT,
                        new Verdict(Reason.EXTENSION_ACTIVATED, "needed"));
            }
        });
        onEvent(Events.PROCESS_ACTIVATED_ENTRIES, manuscript,
                e -> captured.set(new ArrayList<>(e.getProperty(GenerationProperties.LOREBOOK_VERDICTS))));

        Lore lore = activate(manuscript);

        assertThat(lore.activated()).containsExactly("plain", "forcedIn", "off");
        assertThat(lore.lore()).isEqualTo("<plain>\n\n<forcedIn>\n\n<off>");
        assertThat(captured.get()).extracting(v -> v.entry().getName(), v -> v.verdict().reason(),
                        v -> v.verdict().detail())
                .containsExactly(
                        tuple("plain", Reason.CONSTANT, null),
                        tuple("forcedOut", Reason.EXTENSION_SKIPPED, "spoiler"),
                        tuple("forcedIn", Reason.EXTENSION_ACTIVATED, "needed"),
                        tuple("off", Reason.EXTENSION_ACTIVATED, "needed"));
    }

    @Test
    void lorebookEntitiesAreNotModifiedByGeneration() throws Exception {
        Lorebook book = lorebook("world");
        LorebookEntry entry = entry(book, "entry", e -> e.setPayload("{{setvar::x::1}}text"));

        activate(book(book));

        LorebookEntry reloaded = lorebookEntryService.find(entry.getId());
        assertThat(reloaded.getPayload()).isEqualTo("{{setvar::x::1}}text");
        assertThat(reloaded.getModification()).hasSameTimeAs(entry.getModification());
        assertThat(lorebookService.find(book.getId()).getSubbooks()).extracting(BaseEntity::getId).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------
    // scan depth: the content of used entries is searched for the filters of the others
    // ------------------------------------------------------------------------------------------------------------

    @Autowired
    private LorebookActivationService activationService;

    private Lorebook scanning(int depth) throws Exception {
        Lorebook book = lorebook("world");
        book.setScanDepth(depth);
        return lorebookService.save(book);
    }

    /**
     * A (found in the prompt) mentions the castle, the castle mentions the moat, the moat is dry.
     */
    private void chain(Lorebook book) throws Exception {
        entry(book, "dragon", e -> {
            e.setOrder(1);
            e.setFiltering("dragon");
            e.setPayload("The dragon sleeps in the castle.");
        });
        entry(book, "castle", e -> {
            e.setOrder(2);
            e.setFiltering("castle");
            e.setPayload("The castle has a moat.");
        });
        entry(book, "moat", e -> {
            e.setOrder(3);
            e.setFiltering("moat");
            e.setPayload("The moat is dry.");
        });
    }

    @Test
    void entriesAreNotUsedByOtherEntriesWithoutScanDepth() throws Exception {
        Lorebook book = scanning(0);
        chain(book);

        Lore lore = activate(book(book), "A dragon appears.");

        assertThat(lore.activated()).containsExactly("dragon");
    }

    @Test
    void contentOfUsedEntriesIsSearchedOncePerDepth() throws Exception {
        Lorebook book = scanning(1);
        chain(book);

        Lore lore = activate(book(book), "A dragon appears.");

        assertThat(lore.activated()).containsExactly("dragon", "castle");
        assertThat(lore.lore()).isEqualTo("The dragon sleeps in the castle.\n\nThe castle has a moat.");
    }

    @Test
    void deeperScanFollowsTheChainAndKeepsTheOrderOfEntries() throws Exception {
        Lorebook book = scanning(2);
        // the later entry in the book is used first by the order, whatever pass found it
        entry(book, "first", e -> {
            e.setOrder(0);
            e.setFiltering("moat");
            e.setPayload("Moats are wet.");
        });
        chain(book);

        Lore lore = activate(book(book), "A dragon appears.");

        assertThat(lore.activated()).containsExactly("first", "dragon", "castle", "moat");
    }

    @Test
    void scanDepthIsLimited() throws Exception {
        Lorebook book = scanning(99);
        for (int i = 0; i <= LorebookActivationService.MAX_SCAN_DEPTH + 2; i++) {
            int n = i;
            entry(book, "link" + i, e -> {
                e.setOrder(n);
                e.setFiltering(n == 0 ? "start" : "key" + (n - 1));
                e.setPayload("key" + n);
            });
        }

        Lore lore = activate(book(book), "start");

        assertThat(lore.activated()).hasSize(LorebookActivationService.MAX_SCAN_DEPTH + 1);
    }

    @Test
    void entryThatPreventsRecursionIsNotSearched() throws Exception {
        Lorebook book = scanning(2);
        chain(book);
        LorebookEntry dragon = lorebookEntryService.findForUser(
                lorebookEntryService.getEntriesForLorebook(book).stream().filter(e -> e.getName().equals("dragon"))
                        .findFirst().orElseThrow().getUuid());
        dragon.setPreventRecursion(true);
        lorebookEntryService.save(dragon);

        Lore lore = activate(book(book), "A dragon appears.");

        assertThat(lore.activated()).containsExactly("dragon");
    }

    @Test
    void contentOfConstantEntriesIsSearchedToo() throws Exception {
        Lorebook book = scanning(1);
        entry(book, "always", e -> {
            e.setOrder(1);
            e.setPayload("There is a castle.");
        });
        entry(book, "castle", e -> {
            e.setOrder(2);
            e.setFiltering("castle");
        });

        Lore lore = activate(book(book), "Nothing to see.");

        assertThat(lore.activated()).containsExactly("always", "castle");
    }

    @Test
    void entriesThatAreOffOrNotForTheBookAreNotFoundByContent() throws Exception {
        Lorebook book = scanning(1);
        entry(book, "always", e -> e.setPayload("castle castle"));
        entry(book, "off", e -> {
            e.setFiltering("castle");
            e.setEnabled(false);
        });
        entry(book, "wrongBook", e -> e.setFiltering("castle"));
        LorebookEntry negative = entry(book, "negative", e -> e.setFiltering("castle"));
        tagRelationService.createRelation(tag("spoilers"), negative, true);

        Lore lore = activate(book(book, "spoilers"), "Nothing to see.");

        assertThat(lore.activated()).containsExactly("always", "wrongBook");
    }

    @Test
    void verdictsSayWhichEntryFoundAnEntryAndInWhichPass() throws Exception {
        Lorebook book = scanning(2);
        chain(book);
        Manuscript manuscript = book(book);
        AtomicReference<List<LorebookEntryVerdict>> captured = new AtomicReference<>();
        onEvent(Events.PROCESS_ACTIVATED_ENTRIES, manuscript,
                e -> captured.set(new ArrayList<>(e.getProperty(GenerationProperties.LOREBOOK_VERDICTS))));

        activate(manuscript, "A dragon appears.");

        assertThat(captured.get()).extracting(v -> v.entry().getName(), v -> v.verdict().reason(),
                        v -> v.verdict().detail(), LorebookEntryVerdict::depth)
                .containsExactly(
                        tuple("dragon", Reason.FILTER_MATCHED, "dragon", 0),
                        tuple("castle", Reason.LORE_MATCHED, "castle <- dragon", 1),
                        tuple("moat", Reason.LORE_MATCHED, "moat <- castle", 2));
    }

    @Test
    void extensionCanRefuseAnEntryFoundByContent() throws Exception {
        Lorebook book = scanning(2);
        chain(book);
        Manuscript manuscript = book(book);
        List<String> events = new java.util.concurrent.CopyOnWriteArrayList<>();
        onEvent(Events.PROCESS_LOREBOOK_ENTRY, manuscript, e -> {
            String name = ((LorebookEntry) e.getProperty(GenerationProperties.LOREBOOK_ENTRY)).getName();
            Integer depth = e.getProperty(GenerationProperties.LOREBOOK_SCAN_DEPTH);
            events.add(name + "@" + depth);
            if (name.equals("castle") && depth == 1) {
                e.getProperties().put(GenerationProperties.LOREBOOK_VERDICT, new Verdict(Reason.EXTENSION_SKIPPED, "no"));
            }
        });
        AtomicReference<List<LorebookEntryVerdict>> captured = new AtomicReference<>();
        onEvent(Events.PROCESS_ACTIVATED_ENTRIES, manuscript,
                e -> captured.set(new ArrayList<>(e.getProperty(GenerationProperties.LOREBOOK_VERDICTS))));

        Lore lore = activate(manuscript, "A dragon appears.");

        // the refused entry is not used and so is not searched, nothing follows from it
        assertThat(lore.activated()).containsExactly("dragon");
        assertThat(events).containsExactly("dragon@0", "castle@0", "moat@0", "castle@1");
        assertThat(captured.get()).extracting(v -> v.entry().getName(), v -> v.verdict().reason())
                .containsExactly(tuple("dragon", Reason.FILTER_MATCHED), tuple("castle", Reason.EXTENSION_SKIPPED),
                        tuple("moat", Reason.FILTER_NOT_MATCHED));
    }

    @Test
    void previewWithoutAGenerationAgreesWithTheGeneration() throws Exception {
        Lorebook book = scanning(2);
        chain(book);
        entry(book, "other", e -> {
            e.setOrder(4);
            e.setFiltering("unicorn");
        });
        Manuscript manuscript = book(book);

        Lore lore = activate(manuscript, "A dragon appears.");

        Lorebook root = lorebookService.find(book.getId());
        List<LorebookEntryVerdict> preview = activationService.evaluateAll(root,
                activationService.collectLorebooks(root), List.of(), "A dragon appears.");
        assertThat(preview.stream().filter(v -> v.verdict().activated()).map(v -> v.entry().getName()).toList())
                .isEqualTo(lore.activated());
        assertThat(preview).extracting(v -> v.verdict().reason()).containsExactly(Reason.FILTER_MATCHED,
                Reason.LORE_MATCHED, Reason.LORE_MATCHED, Reason.FILTER_NOT_MATCHED);
    }

    @Test
    void scanDepthOfSubbooksIsNotUsed() throws Exception {
        Lorebook sub = scanning(3);
        chain(sub);
        Lorebook root = lorebook("root", sub);

        Lore lore = activate(book(root), "A dragon appears.");

        assertThat(lore.activated()).containsExactly("dragon");
    }

    // ------------------------------------------------------------------------------------------------------------
    // activation context: the instructions, or the instructions and the newest parts of the story
    // ------------------------------------------------------------------------------------------------------------

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * The book gets a story of these parts (the first is the oldest) and the last one is where it stands.
     */
    private Manuscript withStory(Manuscript manuscript, String... responses) throws Exception {
        ChatMessage parent = null;
        for (String response : responses) {
            ChatMessage part = new ChatMessage();
            part.setResponse(response);
            part.setTokenCount(response.length());
            parent = parent == null ? chatMessageService.createRoot(manuscript, part) : chatMessageService.addChild(parent, part);
        }
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(parent);
        return manuscriptService.save(loaded);
    }

    private LorebookEntry lookingIntoParts(Lorebook book, String name, String filter, int parts) throws Exception {
        return entry(book, name, e -> {
            e.setFiltering(filter);
            e.setActivationContext(ActivationContext.INCLUDE_PARTS);
            e.setActivationParts(parts);
        });
    }

    @Test
    void entryLooksOnlyIntoTheInstructionsByDefault() throws Exception {
        Lorebook book = lorebook("world");
        LorebookEntry dragon = entry(book, "dragon", e -> e.setFiltering("dragon"));
        Manuscript manuscript = withStory(book(book), "The dragon slept.");

        Lore lore = activate(manuscript, "Anna walks on.");

        assertThat(dragon.getActivationContext()).isEqualTo(ActivationContext.INSTRUCTIONS);
        assertThat(lore.activated()).isEmpty();
    }

    @Test
    void entryWithPartsFindsItsFilterInTheStory() throws Exception {
        Lorebook book = lorebook("world");
        lookingIntoParts(book, "dragon", "dragon", 1);
        Manuscript manuscript = withStory(book(book), "The dragon slept.");

        Lore lore = activate(manuscript, "Anna walks on.");

        assertThat(lore.activated()).containsExactly("dragon");
    }

    @Test
    void entryWithPartsStillFindsItsFilterInTheInstructions() throws Exception {
        Lorebook book = lorebook("world");
        lookingIntoParts(book, "dragon", "dragon", 1);
        Manuscript manuscript = withStory(book(book), "Nothing here.");

        Lore lore = activate(manuscript, "A dragon appears.");

        assertThat(lore.activated()).containsExactly("dragon");
    }

    @Test
    void entryLooksOnlyIntoTheNewestPartsItAsksFor() throws Exception {
        Lorebook book = lorebook("world");
        lookingIntoParts(book, "one", "dragon", 1);
        lookingIntoParts(book, "two", "dragon", 2);
        lookingIntoParts(book, "three", "dragon", 3);
        Manuscript manuscript = withStory(book(book), "The dragon slept.", "Anna woke.", "Anna walked.");

        Lore lore = activate(manuscript, "Anna walks on.");

        assertThat(lore.activated()).containsExactly("three");
    }

    @Test
    void entryWithMorePartsThanTheStoryHasUsesAllOfThem() throws Exception {
        Lorebook book = lorebook("world");
        lookingIntoParts(book, "dragon", "dragon", 10);
        Manuscript manuscript = withStory(book(book), "The dragon slept.", "Anna woke.");

        Lore lore = activate(manuscript, "Anna walks on.");

        assertThat(lore.activated()).containsExactly("dragon");
    }

    @Test
    void keywordsMayBeSplitBetweenThePartsAndTheInstructions() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "both", e -> {
            e.setFilteringMode(FilteringMode.KEYWORDS_AND);
            e.setFiltering("dragon, castle");
            e.setActivationContext(ActivationContext.INCLUDE_PARTS);
        });
        entry(book, "instructionsOnly", e -> {
            e.setFilteringMode(FilteringMode.KEYWORDS_AND);
            e.setFiltering("dragon, castle");
        });
        Manuscript manuscript = withStory(book(book), "The dragon slept.");

        Lore lore = activate(manuscript, "Anna enters the castle.");

        assertThat(lore.activated()).containsExactly("both");
    }

    @Test
    void entryWithPartsInAnEmptyStoryUsesTheInstructions() throws Exception {
        Lorebook book = lorebook("world");
        lookingIntoParts(book, "dragon", "dragon", 2);

        assertThat(activate(book(book), "A dragon appears.").activated()).containsExactly("dragon");
        assertThat(activate(book(book), "Anna walks.").activated()).isEmpty();
    }

    @Test
    void partsAreReadAsFarBackAsTheEnabledEntriesNeed() throws Exception {
        Lorebook book = lorebook("world");
        entry(book, "plain");
        LorebookEntry two = lookingIntoParts(book, "two", "dragon", 2);
        LorebookEntry off = lookingIntoParts(book, "off", "dragon", 7);
        off.setEnabled(false);
        off = lorebookEntryService.save(off);

        List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(book);
        assertThat(activationService.partsNeeded(entries)).isEqualTo(2);
        assertThat(activationService.partsNeeded(entries.stream().filter(e -> e.getName().equals("plain")).toList())).isZero();

        two.setEnabled(false);
        lorebookEntryService.save(two);
        assertThat(activationService.partsNeeded(lorebookEntryService.getEntriesForLorebook(book))).isZero();
    }

    @Test
    void recentPartsAreTheNewestOnesOldestFirst() throws Exception {
        Manuscript manuscript = withStory(book(lorebook("world")), "one", "", "three", "four");
        ChatMessage leaf = manuscriptService.find(manuscript.getId()).getActiveLeaf();

        assertThat(activationService.recentParts(leaf, 2, false)).containsExactly("three", "four");
        assertThat(activationService.recentParts(leaf, 2, true)).containsExactly("one", "three");
        assertThat(activationService.recentParts(leaf, 9, false)).containsExactly("one", "three", "four");
        assertThat(activationService.recentParts(leaf, 0, false)).isEmpty();
        assertThat(activationService.recentParts(null, 3, false)).isEmpty();
    }

    @Test
    void previewWithPartsAgreesWithTheGeneration() throws Exception {
        Lorebook book = lorebook("world");
        lookingIntoParts(book, "dragon", "dragon", 1);
        entry(book, "castle", e -> e.setFiltering("castle"));
        Manuscript manuscript = withStory(book(book), "The dragon slept near the castle.");
        // the generation adds a part, the preview is of the story the turn continues
        ChatMessage leaf = manuscriptService.find(manuscript.getId()).getActiveLeaf();
        Lore lore = activate(manuscript, "Anna walks on.");

        Lorebook root = lorebookService.find(book.getId());
        List<Lorebook> books = activationService.collectLorebooks(root);
        List<String> parts = activationService.recentParts(leaf,
                activationService.partsNeeded(activationService.orderedEntries(books)), false);
        List<LorebookEntryVerdict> preview = activationService.evaluateAll(root, books, List.of(), "Anna walks on.", parts);

        assertThat(preview.stream().filter(v -> v.verdict().activated()).map(v -> v.entry().getName()).toList())
                .isEqualTo(lore.activated()).containsExactly("dragon");
    }
}
