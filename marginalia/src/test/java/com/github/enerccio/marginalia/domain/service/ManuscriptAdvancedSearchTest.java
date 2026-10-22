package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria.TagMatch;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The advanced search of the book list: name, tags to include and to exclude (all or any), dates, number of parts and the
 * text of the active branch or of all branches.
 * <pre>
 * alpha: root ─ a ─ b1 (active)         tags: noir, cozy    3 + 1 parts
 *                └─ b2
 * bravo: root                           tags: noir          1 part
 * charlie: (no parts)                   tags: cozy          0 parts
 * </pre>
 */
class ManuscriptAdvancedSearchTest extends MarginaliaTestBase {

    private static final long DAY = 24L * 60 * 60 * 1000;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private Manuscript alpha, bravo, charlie;
    private String suffix;

    private Manuscript book(String name) throws Exception {
        Manuscript m = new Manuscript();
        m.setName(name);
        return manuscriptService.save(m);
    }

    private ChatMessage part(Manuscript book, ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    private void tag(Manuscript book, Tag tag) throws Exception {
        tagRelationService.createRelation(tag, book);
    }

    @BeforeEach
    void createBooks() throws Exception {
        login();
        suffix = uniqueName("");
        alpha = book("Alpha " + suffix);
        bravo = book("Bravo " + suffix);
        charlie = book("Charlie " + suffix);

        ChatMessage root = part(alpha, null, "The harbour at dawn");
        ChatMessage a = part(alpha, root, "Mara found the lighthouse key");
        ChatMessage b1 = part(alpha, a, "Rain on the roof");
        part(alpha, a, "The storm took the boat");
        alpha = manuscriptService.find(alpha);
        alpha.setActiveLeaf(b1);
        alpha = manuscriptService.save(alpha);

        ChatMessage storm = part(bravo, null, "A storm came");
        bravo = manuscriptService.find(bravo);
        bravo.setActiveLeaf(storm);
        bravo = manuscriptService.save(bravo);

        Tag noir = tagService.getOrCreateForUser("noir-" + suffix);
        Tag cozy = tagService.getOrCreateForUser("cozy-" + suffix);
        tag(alpha, noir);
        tag(alpha, cozy);
        tag(bravo, noir);
        tag(charlie, cozy);
    }

    private ManuscriptSearchCriteria mine() {
        ManuscriptSearchCriteria criteria = new ManuscriptSearchCriteria();
        criteria.setName(suffix);
        return criteria;
    }

    private List<Long> found(ManuscriptSearchCriteria criteria) throws Exception {
        return manuscriptService.searchAdvanced(criteria, Sorter.sorter("name"));
    }

    @Test
    void emptyCriteriaFindsAllTheBooks() throws Exception {
        assertThat(manuscriptService.searchAdvanced(new ManuscriptSearchCriteria()))
                .contains(alpha.getId(), bravo.getId(), charlie.getId());
    }

    @Test
    void nameIsAPartOfTheNameIgnoringCase() throws Exception {
        ManuscriptSearchCriteria criteria = mine();
        criteria.setName("RAVO " + suffix);
        assertThat(found(criteria)).containsExactly(bravo.getId());
        criteria.setName("lph*" + suffix);
        assertThat(found(criteria)).containsExactly(alpha.getId());
    }

    @Test
    void includedTagsAllOrAny() throws Exception {
        ManuscriptSearchCriteria criteria = mine();
        criteria.setIncludeTags(List.of("noir-" + suffix, "cozy-" + suffix));
        criteria.setIncludeMatch(TagMatch.ALL);
        assertThat(found(criteria)).containsExactly(alpha.getId());

        criteria.setIncludeMatch(TagMatch.ANY);
        assertThat(found(criteria)).containsExactly(alpha.getId(), bravo.getId(), charlie.getId());

        criteria.setIncludeTags(List.of("noir-" + suffix));
        assertThat(found(criteria)).containsExactly(alpha.getId(), bravo.getId());
    }

    @Test
    void excludedTagsAnyOrAll() throws Exception {
        ManuscriptSearchCriteria criteria = mine();
        criteria.setExcludeTags(List.of("noir-" + suffix, "cozy-" + suffix));
        criteria.setExcludeMatch(TagMatch.ANY);
        assertThat(found(criteria)).isEmpty();

        criteria.setExcludeMatch(TagMatch.ALL);
        assertThat(found(criteria)).containsExactly(bravo.getId(), charlie.getId());

        criteria.setExcludeTags(List.of("cozy-" + suffix));
        criteria.setExcludeMatch(TagMatch.ANY);
        assertThat(found(criteria)).containsExactly(bravo.getId());
    }

    @Test
    void includeAndExcludeCombine() throws Exception {
        ManuscriptSearchCriteria criteria = mine();
        criteria.setIncludeTags(List.of("noir-" + suffix));
        criteria.setExcludeTags(List.of("cozy-" + suffix));
        assertThat(found(criteria)).containsExactly(bravo.getId());
    }

    @Test
    void createdAndModifiedRanges() throws Exception {
        Date yesterday = new Date(System.currentTimeMillis() - DAY);
        Date tomorrow = new Date(System.currentTimeMillis() + DAY);

        ManuscriptSearchCriteria criteria = mine();
        criteria.setCreatedFrom(yesterday);
        criteria.setCreatedTo(tomorrow);
        criteria.setModifiedFrom(yesterday);
        criteria.setModifiedTo(tomorrow);
        assertThat(found(criteria)).containsExactly(alpha.getId(), bravo.getId(), charlie.getId());

        criteria.setCreatedFrom(tomorrow);
        criteria.setCreatedTo(null);
        assertThat(found(criteria)).isEmpty();

        criteria.setCreatedFrom(null);
        criteria.setModifiedFrom(null);
        criteria.setModifiedTo(yesterday);
        assertThat(found(criteria)).isEmpty();
    }

    @Test
    void numberOfParts() throws Exception {
        ManuscriptSearchCriteria criteria = mine();
        criteria.setMinParts(1);
        assertThat(found(criteria)).containsExactly(alpha.getId(), bravo.getId());

        criteria.setMinParts(2);
        assertThat(found(criteria)).containsExactly(alpha.getId());

        criteria.setMinParts(null);
        criteria.setMaxParts(1);
        assertThat(found(criteria)).containsExactly(bravo.getId(), charlie.getId());

        criteria.setMinParts(4);
        criteria.setMaxParts(4);
        assertThat(found(criteria)).containsExactly(alpha.getId());
    }

    @Test
    void textOfTheActiveBranchOnlyByDefault() throws Exception {
        ManuscriptSearchCriteria criteria = mine();
        criteria.setFulltext("storm");
        // alpha has it only in the branch that is not active
        assertThat(found(criteria)).containsExactly(bravo.getId());

        criteria.setFulltext("rain roof");
        assertThat(found(criteria)).containsExactly(alpha.getId());

        criteria.setFulltext("\"lighthouse key\"");
        assertThat(found(criteria)).containsExactly(alpha.getId());
    }

    @Test
    void textOfAllBranches() throws Exception {
        ManuscriptSearchCriteria criteria = mine();
        criteria.setFulltext("storm");
        criteria.setAllBranches(true);
        assertThat(found(criteria)).containsExactly(alpha.getId(), bravo.getId());
    }

    @Test
    void textCombinesWithTheOtherChecks() throws Exception {
        ManuscriptSearchCriteria criteria = mine();
        criteria.setFulltext("storm");
        criteria.setAllBranches(true);
        criteria.setIncludeTags(List.of("cozy-" + suffix));
        assertThat(found(criteria)).containsExactly(alpha.getId());
    }

    @Test
    void aBookWithoutActivePartHasNoTextInTheBranch() throws Exception {
        ManuscriptSearchCriteria criteria = mine();
        criteria.setFulltext("storm");
        criteria.setName("Bravo " + suffix);
        Manuscript stored = manuscriptService.find(bravo);
        stored.setActiveLeaf(null);
        manuscriptService.save(stored);
        assertThat(found(criteria)).isEmpty();
        criteria.setAllBranches(true);
        assertThat(found(criteria)).containsExactly(bravo.getId());
    }

    @Test
    void anotherUserDoesNotSeeTheBooks() throws Exception {
        loginAs(createUser());
        ManuscriptSearchCriteria criteria = mine();
        criteria.setFulltext("storm");
        criteria.setAllBranches(true);
        assertThat(found(criteria)).isEmpty();
        assertThat(found(mine())).isEmpty();
    }
}
