package com.github.enerccio.marginalia.domain;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.HelpAssistantService.Settings;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.InferenceCollector;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The help assistant and the books of the user: nothing of them is offered to the model until the user turns it on, then it
 * can walk the folders, search and read the settings a book resolves to, and sees titles, descriptions and settings but
 * never the text of a story.
 */
class HelpAssistantBooksTest extends MarginaliaTestBase {

    private static final List<LLMChatMessage> PAYLOAD = List.of(LLMChatMessage.of(LLMRole.USER, "Where is my book about lovers?"));

    @Autowired
    private HelpAssistantService helpService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    private OpenAICompatible ai;

    @Override
    protected User createUser() throws Exception {
        User user = super.createUser();
        updatePermission(user, PermissionType.HELPER, permission -> permission.setGranted(true));
        return user;
    }

    @BeforeEach
    void enableTheAssistant() throws Exception {
        loginAdmin();
        ai = createAI();
        ai = (OpenAICompatible) aiService.save(ai);
        helpService.saveSettings(new Settings(ai.getUuid()));
        loginAs(createUser());
    }

    private String ask() throws Exception {
        InferenceCollector collector = new InferenceCollector();
        helpService.ask(PAYLOAD, collector);
        assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);
        // the body is JSON: the quotes and the line ends of the results are escaped
        return llm.getCompletionRequests().getLast().getRawBody().replace("\\\"", "\"").replace("\\n", "\n");
    }

    /** The books: Sci-fi (Czech, a style, a secret prompt) / Expanse / Leviathan with a story, and a loose Notes. */
    private Manuscript fileSomeBooks() throws Exception {
        Folder scifi = folderService.create("Sci-fi", null);
        scifi = folderService.update(scifi, new FolderService.Details("Sci-fi", "Space stories", Language.CZECH,
                Map.of(PromptField.STYLE, "sparse", PromptField.REDRAFT_PROMPT, "SECRET-PROMPT-TEXT")));
        Folder expanse = folderService.create("Expanse", scifi);
        Manuscript book = new Manuscript();
        book.setName("Leviathan");
        book.setDescription("Two true lovers on a station");
        book.setFolder(expanse);
        book = manuscriptService.save(book);
        ChatMessage part = new ChatMessage();
        part.setResponse("They met under the cold moonlit corridor of Ceres.");
        chatMessageService.createRoot(book, part);
        Manuscript notes = new Manuscript();
        notes.setName("Notes");
        manuscriptService.save(notes);
        return book;
    }

    @Test
    void theBooksAreOffUntilTheUserTurnsThemOn() throws Exception {
        fileSomeBooks();
        llm.enqueue(MockLLMResponse.text("answer"));

        assertThat(helpService.isBooksAllowed()).isFalse();
        assertThat(ask()).doesNotContain("list_folder").doesNotContain("search_books").doesNotContain("get_book_settings")
                .doesNotContain("allowed you to look at their books");

        helpService.setBooksAllowed(true);
        llm.enqueue(MockLLMResponse.text("answer"));

        assertThat(helpService.isBooksAllowed()).isTrue();
        assertThat(ask()).contains("list_folder", "search_books", "get_book_settings", "allowed you to look at their books");

        helpService.setBooksAllowed(false);
        assertThat(helpService.isBooksAllowed()).isFalse();
    }

    @Test
    void theChoiceIsTheUsersOwn() throws Exception {
        helpService.setBooksAllowed(true);

        loginAs(createUser());

        assertThat(helpService.isBooksAllowed()).isFalse();
    }

    @Test
    void theModelWalksTheFoldersAndSeesTitlesAndDescriptions() throws Exception {
        fileSomeBooks();
        helpService.setBooksAllowed(true);
        llm.enqueue(MockLLMResponse.toolCall("list_folder", "{}"),
                MockLLMResponse.toolCall("list_folder", "{\"path\":\"sci-fi / Expanse\"}"),
                MockLLMResponse.toolCall("list_folder", "{\"path\":\"Nowhere\"}"),
                MockLLMResponse.text("done"));

        String body = ask();

        assertThat(body).contains("The top level").contains("Sci-fi (1 books) - Space stories").contains("\"Notes\"")
                .contains("Folder Sci-fi/Expanse").contains("\"Leviathan\"").contains("Two true lovers on a station")
                .contains("There is no folder \"Nowhere\" in the top level").contains("The folders there are: Sci-fi.");
        assertThat(body).as("never the text of a story").doesNotContain("moonlit");
    }

    @Test
    void aSearchFindsByTitleDescriptionAndStoryTextAndSaysWhere() throws Exception {
        fileSomeBooks();
        helpService.setBooksAllowed(true);
        llm.enqueue(MockLLMResponse.toolCall("search_books", "{\"query\":\"true lovers\"}"),
                MockLLMResponse.toolCall("search_books", "{\"query\":\"moonlit\"}"),
                MockLLMResponse.toolCall("search_books", "{\"query\":\"moonlit\",\"folder\":\"Fantasy\"}"),
                MockLLMResponse.text("done"));

        String body = ask();

        assertThat(body).contains("\"Leviathan\"").contains("folder: Sci-fi/Expanse").contains("found in: description")
                .contains("found in: story text").contains("There is no folder \"Fantasy\"");
        assertThat(body).as("the text found is not returned").doesNotContain("cold moonlit corridor");
    }

    @Test
    void theSettingsComeWithWhereEachOneComesFromButNotThePromptTexts() throws Exception {
        fileSomeBooks();
        helpService.setBooksAllowed(true);
        llm.enqueue(MockLLMResponse.toolCall("get_book_settings", "{\"book\":\"Sci-fi/Expanse/Leviathan\"}"),
                MockLLMResponse.text("done"));

        String body = ask();

        assertThat(body).contains("Language: Czech - inherited from the folder \"Sci-fi\"")
                .contains("Style: sparse - inherited from the folder \"Sci-fi\"")
                .contains("Redraft prompt: a custom text - inherited from the folder")
                .contains("Point of view: ");
        assertThat(body).doesNotContain("SECRET-PROMPT-TEXT");
    }

    @Test
    void aBookIsFoundByItsIdAndAnAmbiguousTitleIsListed() throws Exception {
        Manuscript book = fileSomeBooks();
        Manuscript twin = new Manuscript();
        twin.setName("Notes");
        manuscriptService.save(twin);
        helpService.setBooksAllowed(true);
        llm.enqueue(MockLLMResponse.toolCall("get_book_settings", "{\"book\":\"" + book.getUuid() + "\"}"),
                MockLLMResponse.toolCall("get_book_settings", "{\"book\":\"Notes\"}"),
                MockLLMResponse.toolCall("get_book_settings", "{\"book\":\"Missing\"}"),
                MockLLMResponse.text("done"));

        String body = ask();

        assertThat(body).contains("## Leviathan").contains("Several books are called").contains("There is no book");
    }

    @Test
    void theBooksOfOtherUsersAreNeverSeen() throws Exception {
        fileSomeBooks();
        loginAs(createUser());
        helpService.setBooksAllowed(true);
        llm.enqueue(MockLLMResponse.toolCall("list_folder", "{\"path\":\"Sci-fi\"}"),
                MockLLMResponse.toolCall("search_books", "{\"query\":\"Leviathan\"}"),
                MockLLMResponse.text("done"));

        String body = ask();

        assertThat(body).contains("There is no folder \"Sci-fi\"").contains("0 books");
        assertThat(body).doesNotContain("moonlit");
    }
}
