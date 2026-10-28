package com.github.enerccio.marginalia.lorebook;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.impl.SillyTavernEntryConverter;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The export of a lorebook as SillyTavern world info: the keys, the position, the tags and the subbooks, and that the
 * importer reads the file again.
 */
class SillyTavernExportTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @BeforeEach
    void logIn() throws Exception {
        login();
    }

    private Lorebook book(String name) throws Exception {
        Lorebook book = new Lorebook();
        book.setName(uniqueName(name));
        return lorebookService.save(book);
    }

    private LorebookEntry entry(Lorebook book, String name, String filtering, FilteringMode mode) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(book);
        entry.setName(name);
        entry.setPayload("About " + name);
        entry.setFiltering(filtering);
        entry.setFilteringMode(mode);
        return lorebookEntryService.save(entry);
    }

    private JsonObject export(Lorebook book) throws Exception {
        return new Gson().fromJson(lorebookService.exportToSillytavern(book), JsonObject.class);
    }

    @Test
    void filterBecomesKeys() throws Exception {
        Lorebook book = book("Keys");
        entry(book, "Always", null, null);
        entry(book, "Text", "harbour", FilteringMode.TEXT);
        entry(book, "Or", "inn, tavern ,pub", FilteringMode.KEYWORDS_OR);
        entry(book, "And", "king, crown", FilteringMode.KEYWORDS_AND);
        entry(book, "Pattern", "storm(y|s)?", FilteringMode.REGEX);

        JsonObject entries = export(book).getAsJsonObject("entries");
        assertThat(entries.keySet()).hasSize(5);
        JsonObject always = byComment(entries, "Always");
        assertThat(always.get("constant").getAsBoolean()).isTrue();
        assertThat(always.getAsJsonArray("key")).isEmpty();
        assertThat(always.get("content").getAsString()).isEqualTo("About Always");

        JsonObject text = byComment(entries, "Text");
        assertThat(text.get("constant").getAsBoolean()).isFalse();
        assertThat(text.getAsJsonArray("key").toString()).isEqualTo("[\"harbour\"]");

        assertThat(byComment(entries, "Or").getAsJsonArray("key").toString()).isEqualTo("[\"inn\",\"tavern\",\"pub\"]");

        JsonObject and = byComment(entries, "And");
        assertThat(and.getAsJsonArray("key").toString()).isEqualTo("[\"king\"]");
        assertThat(and.getAsJsonArray("keysecondary").toString()).isEqualTo("[\"crown\"]");
        assertThat(and.get("selective").getAsBoolean()).isTrue();
        assertThat(and.get("selectiveLogic").getAsInt()).isEqualTo(3);

        assertThat(byComment(entries, "Pattern").getAsJsonArray("key").toString()).isEqualTo("[\"/storm(y|s)?/i\"]");
    }

    @Test
    void positionOrderAndDisabled() throws Exception {
        Lorebook book = book("Position");
        LorebookEntry entry = entry(book, "Late", "x", FilteringMode.TEXT);
        entry.setInsertionMode(InsertionMode.BEFORE_USER_PROMPT);
        entry.setOrder(42);
        entry.setEnabled(false);
        lorebookEntryService.save(entry);
        entry(book, "Early", "y", FilteringMode.TEXT);

        JsonObject entries = export(book).getAsJsonObject("entries");
        JsonObject late = byComment(entries, "Late");
        assertThat(late.get("position").getAsInt()).isEqualTo(4);
        assertThat(late.get("order").getAsInt()).isEqualTo(42);
        assertThat(late.get("disable").getAsBoolean()).isTrue();
        JsonObject early = byComment(entries, "Early");
        assertThat(early.get("position").getAsInt()).isEqualTo(0);
        assertThat(early.get("disable").getAsBoolean()).isFalse();
    }

    @Test
    void tagsBecomeTheCharacterFilter() throws Exception {
        Lorebook book = book("Tags");
        LorebookEntry included = entry(book, "Included", "a", FilteringMode.TEXT);
        tagRelationService.createRelation(tagService.getOrCreateForUser("elf"), included, false);
        tagRelationService.createRelation(tagService.getOrCreateForUser("orc"), included, true);
        LorebookEntry excluded = entry(book, "Excluded", "b", FilteringMode.TEXT);
        tagRelationService.createRelation(tagService.getOrCreateForUser("dwarf"), excluded, true);
        entry(book, "Plain", "c", FilteringMode.TEXT);

        JsonObject entries = export(book).getAsJsonObject("entries");
        JsonObject filter = byComment(entries, "Included").getAsJsonObject("characterFilter");
        assertThat(filter.get("isExclude").getAsBoolean()).isFalse();
        assertThat(filter.getAsJsonArray("names").toString()).isEqualTo("[\"elf\"]");
        JsonObject exclude = byComment(entries, "Excluded").getAsJsonObject("characterFilter");
        assertThat(exclude.get("isExclude").getAsBoolean()).isTrue();
        assertThat(exclude.getAsJsonArray("names").toString()).isEqualTo("[\"dwarf\"]");
        assertThat(byComment(entries, "Plain").has("characterFilter")).isFalse();
    }

    @Test
    void subbooksAreIncludedAndCyclesAreSafe() throws Exception {
        Lorebook root = book("Root");
        Lorebook child = book("Child");
        entry(root, "Root entry", "r", FilteringMode.TEXT);
        entry(child, "Child entry", "c", FilteringMode.TEXT);
        root.setSubbooks(List.of(child));
        lorebookService.save(root);

        JsonObject exported = export(root);
        assertThat(exported.get("name").getAsString()).isEqualTo(root.getName());
        JsonObject entries = exported.getAsJsonObject("entries");
        assertThat(entries.keySet()).containsExactly("0", "1");
        assertThat(entries.getAsJsonObject("0").get("comment").getAsString()).isEqualTo("Root entry");
        assertThat(entries.getAsJsonObject("1").get("comment").getAsString()).isEqualTo("Child entry");
    }

    @Test
    void importReadsTheExportAgain() throws Exception {
        Lorebook book = book("Roundtrip");
        entry(book, "Always", null, null);
        entry(book, "Text", "harbour", FilteringMode.TEXT);
        entry(book, "And", "king, crown", FilteringMode.KEYWORDS_AND);
        LorebookEntry late = entry(book, "Late", "x", FilteringMode.TEXT);
        late.setInsertionMode(InsertionMode.BEFORE_USER_PROMPT);
        lorebookEntryService.save(late);

        Lorebook imported = lorebookService.importFromSillytavern(lorebookService.exportToSillytavern(book), "ignored");

        assertThat(imported.getName()).isEqualTo(book.getName());
        List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(imported);
        assertThat(entries).extracting(LorebookEntry::getName).containsExactlyInAnyOrder("Always", "Text", "And", "Late");
        LorebookEntry always = named(entries, "Always");
        assertThat(always.getFiltering()).isNull();
        assertThat(named(entries, "Text").getFiltering()).isEqualTo("harbour");
        assertThat(named(entries, "Late").getInsertionMode()).isEqualTo(InsertionMode.BEFORE_USER_PROMPT);
        LorebookEntry and = named(entries, "And");
        assertThat(and.getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        Pattern pattern = Pattern.compile(and.getFiltering());
        assertThat(pattern.matcher("the king wears a crown").find()).isTrue();
        assertThat(pattern.matcher("the king wears a hat").find()).isFalse();
    }

    @Test
    void regexBecomesAJavaScriptKey() {
        // flags after the pattern, \Q..\E quotes escaped, the slash escaped
        assertThat(regexKey("(?s)^(?=.*(?:\\Qa.b\\E|c/d))")).isEqualTo("/^(?=.*(?:a\\.b|c\\/d))/si");
        assertThat(regexKey("(?-i)Storm")).isEqualTo("/Storm/");
    }

    private static String regexKey(String expression) {
        return SillyTavernEntryConverter.exportKeys(expression, FilteringMode.REGEX).primary().getFirst();
    }

    private static JsonObject byComment(JsonObject entries, String comment) {
        for (String key : entries.keySet()) {
            JsonObject entry = entries.getAsJsonObject(key);
            if (comment.equals(entry.get("comment").getAsString())) {
                return entry;
            }
        }
        throw new AssertionError("no entry " + comment);
    }

    private static LorebookEntry named(List<LorebookEntry> entries, String name) {
        return entries.stream().filter(e -> name.equals(e.getName())).findFirst().orElseThrow();
    }
}
