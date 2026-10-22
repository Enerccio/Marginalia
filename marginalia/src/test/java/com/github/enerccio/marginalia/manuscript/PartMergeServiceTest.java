package com.github.enerccio.marginalia.manuscript;

import com.github.enerccio.marginalia.domain.components.PartMerge;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.MergePreview;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Merging a part into the part before it ({@link ChatMessageService#mergeIntoPrevious}).
 */
class PartMergeServiceTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    private Manuscript book;

    @BeforeEach
    void createBook() throws Exception {
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        book = manuscriptService.save(m);
    }

    private ChatMessage part(ChatMessage parent, String text, String instructions) throws Exception {
        ChatMessage created = chatMessageService.createPart(book, parent, text, new TurnInput(null, null, null, instructions));
        return chatMessageService.find(created.getId());
    }

    @Test
    void textsAreJoinedAndTheLaterPartIsRemoved() throws Exception {
        ChatMessage first = part(null, "He drew his sword and", "Write the duel. Short");
        ChatMessage second = part(first, "struck.", "Write the duel. Add rain");
        ChatMessage third = part(second, "The end.", "Finish");

        MergePreview preview = chatMessageService.previewMergeIntoPrevious(book, second);
        assertThat(preview.join()).isEqualTo(PartMerge.Join.SPACE);
        assertThat(preview.input().instructions()).isEqualTo("Write the duel. Short\nAdd rain");

        ChatMessage merged = chatMessageService.mergeIntoPrevious(book, second, preview.input(), preview.join(), Set.of(), Set.of());

        assertThat(merged.getId()).isEqualTo(first.getId());
        ChatMessage stored = chatMessageService.find(first.getId());
        assertThat(stored.getResponse()).isEqualTo("He drew his sword and struck.");
        assertThat(stored.getWordCount()).isEqualTo(6);
        assertThat(stored.getInstructions()).isEqualTo("Write the duel. Short\nAdd rain");
        assertThat(chatMessageService.find(second.getId()).isDeleted()).isTrue();

        // what followed the merged part follows the previous one, the active leaf did not move
        assertThat(chatMessageService.find(third.getId()).getParent().getId()).isEqualTo(first.getId());
        Manuscript saved = manuscriptService.find(book);
        assertThat(saved.getActiveLeaf().getId()).isEqualTo(third.getId());
        assertThat(chatMessageService.getBranchFromLeaf(saved.getActiveLeaf())).extracting(ChatMessage::getResponse)
                .containsExactly("He drew his sword and struck.", "The end.");
    }

    @Test
    void activeLeafMovesToThePreviousPartWhenTheMergedOneWasTheLeaf() throws Exception {
        ChatMessage first = part(null, "One.", null);
        ChatMessage second = part(first, "Two.", null);

        chatMessageService.mergeIntoPrevious(book, second, null, PartMerge.Join.PARAGRAPH, Set.of(), Set.of());

        assertThat(manuscriptService.find(book).getActiveLeaf().getId()).isEqualTo(first.getId());
        assertThat(chatMessageService.find(first.getId()).getResponse()).isEqualTo("One.\n\nTwo.");
    }

    @Test
    void attributesFollowTheChoice() throws Exception {
        ChatMessage first = part(null, "One.", null);
        first.getAttributes().add("kept", new com.google.gson.JsonPrimitive("a"));
        first.getAttributes().add("both", new com.google.gson.JsonPrimitive("old"));
        chatMessageService.save(first);
        ChatMessage second = part(first, "Two.", null);
        second.getAttributes().add("both", new com.google.gson.JsonPrimitive("new"));
        second.getAttributes().add("later", new com.google.gson.JsonPrimitive("b"));
        second.getAttributes().add("gone", new com.google.gson.JsonPrimitive("c"));
        chatMessageService.save(second);

        chatMessageService.mergeIntoPrevious(book, second, null, null, Set.of(List.of("both")), Set.of(List.of("gone")));

        com.google.gson.JsonObject attributes = chatMessageService.find(first.getId()).getAttributes();
        assertThat(attributes.get("kept").getAsString()).isEqualTo("a");
        assertThat(attributes.get("both").getAsString()).isEqualTo("old");
        assertThat(attributes.get("later").getAsString()).isEqualTo("b");
        assertThat(attributes.has("gone")).isFalse();
    }

    @Test
    void originalTextAndImagesAreJoined() throws Exception {
        ChatMessage first = part(null, "Generated", null);
        first.setOriginalResponse("Generated");
        first.setImages(List.of(new ImageAttachment("r1", "one")));
        chatMessageService.save(first);
        ChatMessage second = part(first, "typed", null);
        second.setImages(List.of(new ImageAttachment("r2", "two")));
        chatMessageService.save(second);

        chatMessageService.mergeIntoPrevious(book, second, null, PartMerge.Join.SPACE, Set.of(), Set.of());

        ChatMessage stored = chatMessageService.find(first.getId());
        assertThat(stored.getResponse()).isEqualTo("Generated typed");
        // the part nobody generated adds nothing to the original, so the diff shows its text as the author's
        assertThat(stored.getOriginalResponse()).isEqualTo("Generated");
        assertThat(stored.getImages()).extracting(ImageAttachment::resource).containsExactly("r1", "r2");
    }

    @Test
    void firstPartCannotBeMerged() throws Exception {
        ChatMessage first = part(null, "One.", null);

        assertThatThrownBy(() -> chatMessageService.previewMergeIntoPrevious(book, first)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> chatMessageService.mergeIntoPrevious(book, first, null, null, Set.of(), Set.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void partWithSwipesCannotBeMerged() throws Exception {
        ChatMessage first = part(null, "One.", null);
        ChatMessage second = part(first, "Two.", null);
        part(first, "Two, another.", null);

        assertThatThrownBy(() -> chatMessageService.mergeIntoPrevious(book, second, null, null, Set.of(), Set.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chatMessageService.find(second.getId()).isDeleted()).isFalse();
        assertThat(chatMessageService.find(first.getId()).getResponse()).isEqualTo("One.");
    }
}
