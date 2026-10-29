package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The settings of the folders above a book reach the prompt the model gets, the book's own win.
 */
class FolderSettingsGenerationTest extends GenerationTestBase {

    @Autowired
    private FolderService folderService;

    private Folder folder(String name, Folder parent, String style) throws Exception {
        Folder folder = folderService.create(name, parent);
        folder.setStyle(style);
        return folderService.save(folder);
    }

    private Manuscript bookIn(Folder folder, String ownStyle) throws Exception {
        Manuscript book = newManuscript();
        book.setTemplate("STYLE[{{style}}]");
        book.setFolder(folder);
        book.setStyle(ownStyle);
        return manuscriptService.save(book);
    }

    private String systemPrompt() {
        MockLLMRequest request = llm.lastCompletionRequest();
        return String.join("\n", request.getMessages("system"));
    }

    @Test
    void theStyleOfTheFolderIsInTheSystemPrompt() throws Exception {
        Folder scifi = folder("Sci-fi", null, "hard science fiction");
        Folder expanse = folder("Expanse", scifi, null);
        Manuscript book = bookIn(expanse, null);

        assertThat(generate(book, "go").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);

        assertThat(systemPrompt()).contains("STYLE[hard science fiction]");
    }

    @Test
    void theBookAndTheNearestFolderWin() throws Exception {
        Folder scifi = folder("Sci-fi", null, "hard science fiction");
        Folder expanse = folder("Expanse", scifi, "gritty");

        Manuscript inFolder = bookIn(expanse, null);
        assertThat(generate(inFolder, "go").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(systemPrompt()).contains("STYLE[gritty]");

        Manuscript own = bookIn(expanse, "the style of the book");
        assertThat(generate(own, "go").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(systemPrompt()).contains("STYLE[the style of the book]");
    }

    @Test
    void aBookOutsideTheFoldersIsNotAffected() throws Exception {
        folder("Sci-fi", null, "hard science fiction");
        Manuscript book = bookIn(null, "plain");

        assertThat(generate(book, "go").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);

        assertThat(systemPrompt()).contains("STYLE[plain]").doesNotContain("hard science fiction");
    }
}
