package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandRun;
import com.github.enerccio.marginalia.domain.service.impl.CommandServiceImpl;
import com.github.enerccio.marginalia.domain.service.impl.commands.SearchReplaceChatMessageCommand;
import com.github.enerccio.marginalia.domain.service.impl.commands.SearchReplaceLoreEntryCommand;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.phone.PhoneWorkspace;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code /search-replace-parts} and {@code /search-replace-lore-entries}: the search and replace dialogs as commands.
 * They work on the book that is open, ask the user to confirm (the test answers instead of the user) and can't be
 * stopped while they replace.
 */
class SearchReplaceCommandsTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    @Autowired
    private SessionPoint sessionPoint;

    private final CommandServiceImpl commands = new CommandServiceImpl();

    /** What the user answers to the question, and the questions that were asked. */
    private volatile boolean answer = true;
    private final List<String> questions = new CopyOnWriteArrayList<>();

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    private Manuscript manuscript;
    private ChatMessage first, second, other;
    private LorebookEntry harry, dragons;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        commands.registerCommands(new SearchReplaceChatMessageCommand() {
            @Override
            protected boolean confirm(CommandContext context, String question) {
                questions.add(question);
                return answer;
            }
        });
        commands.registerCommands(new SearchReplaceLoreEntryCommand() {
            @Override
            protected boolean confirm(CommandContext context, String question) {
                questions.add(question);
                return answer;
            }
        });

        Lorebook lore = new Lorebook();
        lore.setName("lore");
        lore = lorebookService.save(lore);
        harry = entry(lore, "Harry", "Harry the boy", 10);
        dragons = entry(lore, "Dragons", "Fire", 20);

        manuscript = newManuscript();
        manuscript.setLorebook(lore);
        manuscript = manuscriptService.save(manuscript);
        first = add(null, "Anna met John");
        second = add(first, "John went home");
        other = add(first, "Another John");
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(second);
        manuscript = manuscriptService.save(loaded);
    }

    @AfterEach
    void tearDown() {
        commands.destroy();
        sessionPoint.setWorkspace(null);
        UI.setCurrent(null);
        ui = null;
    }

    private ChatMessage add(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        Thread.sleep(3);  // messages are ordered by creation time
        return parent == null ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(parent, message);
    }

    private LorebookEntry entry(Lorebook lore, String name, String payload, int order) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lore);
        entry.setName(name);
        entry.setPayload(payload);
        entry.setOrder(order);
        return entryService.save(entry);
    }

    private void openBook() throws Exception {
        Workspace workspace = new PhoneWorkspace();
        workspace.create();
        workspace.refresh();
        sessionPoint.setWorkspace(workspace);
        JsonObject state = JsonParser.parseString("{\"dialog\":{\"manuscript\":\"" + manuscript.getUuid()
                + "\",\"tab\":\"prompt\"}}").getAsJsonObject();
        workspace.getManuscriptSection().restoreState(state);
    }

    private String response(ChatMessage message) throws Exception {
        return chatMessageService.find(message.getId()).getResponse();
    }

    private String payload(LorebookEntry entry) throws Exception {
        return entryService.find(entry.getId()).getPayload();
    }

    private String run(String line) throws Exception {
        return (String) commands.executeLine(line).get(30, TimeUnit.SECONDS);
    }

    @Test
    void replacesInTheActiveBranchAfterTheUserConfirms() throws Exception {
        openBook();

        CommandRun run = commands.executeLine("/search-replace-parts John Mike");

        assertThat((String) run.get(30, TimeUnit.SECONDS)).contains("2").contains("Replaced");
        assertThat(response(first)).isEqualTo("Anna met Mike");
        assertThat(response(second)).isEqualTo("Mike went home");
        // not the active branch
        assertThat(response(other)).isEqualTo("Another John");
        assertThat(questions).hasSize(1);
        assertThat(questions.getFirst()).contains("John").contains("Mike");
        // the replace itself can't be stopped
        assertThat(run.isUninterruptable()).isTrue();
    }

    @Test
    void doesNotReplaceWhenTheUserSaysNo() throws Exception {
        openBook();
        answer = false;

        CommandRun run = commands.executeLine("/search-replace-parts John Mike");

        assertThat((String) run.get(30, TimeUnit.SECONDS)).contains("nothing was changed");
        assertThat(response(first)).isEqualTo("Anna met John");
        assertThat(questions).hasSize(1);
        // it could be stopped all the time
        assertThat(run.isUninterruptable()).isFalse();
    }

    @Test
    void asksNothingWhenNothingIsFound() throws Exception {
        openBook();

        assertThat(run("/search-replace-parts Zed Mike")).isEqualTo("Nothing found");
        assertThat(questions).isEmpty();
    }

    @Test
    void optionsAreTheOnesOfTheDialogUnlessGiven() throws Exception {
        openBook();

        // case sensitive by default
        assertThat(run("/search-replace-parts john Mike")).isEqualTo("Nothing found");
        // regex off by default: the dot is a dot
        assertThat(run("/search-replace-parts J.hn Mike")).isEqualTo("Nothing found");

        // regex on: the case option does not apply to it, the expression says (?i) itself
        assertThat(run("/search-replace-parts j.hn Mike true false")).isEqualTo("Nothing found");
        assertThat(run("/search-replace-parts (?i)j.hn Mike true")).contains("Replaced 2");
        assertThat(response(first)).isEqualTo("Anna met Mike");
    }

    @Test
    void canSearchAllBranches() throws Exception {
        openBook();

        // regex, case sensitive, summaries, all branches
        assertThat(run("/search-replace-parts John Mike false true true true")).contains("Replaced 3");
        assertThat(response(other)).isEqualTo("Another Mike");
    }

    @Test
    void anInvalidRegexFailsWithTheReason() throws Exception {
        openBook();

        assertThatThrownBy(() -> run("/search-replace-parts ( x true"))
                .isInstanceOf(ExecutionException.class);
        assertThat(questions).isEmpty();
    }

    @Test
    void failsWhenNoBookIsOpen() throws Exception {
        Workspace workspace = new PhoneWorkspace();
        workspace.create();
        workspace.refresh();
        sessionPoint.setWorkspace(workspace);

        assertThatThrownBy(() -> run("/search-replace-parts John Mike")).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/search-replace-lore-entries Harry Ron")).isInstanceOf(ExecutionException.class);
        assertThat(questions).isEmpty();
    }

    @Test
    void replacesInTheLorebookOfTheOpenBook() throws Exception {
        openBook();

        CommandRun run = commands.executeLine("/search-replace-lore-entries Harry Ron");

        assertThat((String) run.get(30, TimeUnit.SECONDS)).contains("Replaced 2 matches in 1 items");
        assertThat(entryService.find(harry.getId()).getName()).isEqualTo("Ron");
        assertThat(payload(harry)).isEqualTo("Ron the boy");
        assertThat(payload(dragons)).isEqualTo("Fire");
        assertThat(questions).hasSize(1);
        assertThat(run.isUninterruptable()).isTrue();
    }

    @Test
    void lorebookIsNotChangedWhenTheUserSaysNo() throws Exception {
        openBook();
        answer = false;

        assertThat(run("/search-replace-lore-entries Harry Ron")).contains("nothing was changed");
        assertThat(payload(harry)).isEqualTo("Harry the boy");
    }

    @Test
    void lorebookOptionsAreCaseSensitiveUnlessGiven() throws Exception {
        openBook();

        assertThat(run("/search-replace-lore-entries harry Ron")).isEqualTo("Nothing found");
        assertThat(run("/search-replace-lore-entries harry Ron false false")).contains("Replaced 2");
    }

    @Test
    void failsWhenTheBookHasNoLorebook() throws Exception {
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setLorebook(null);
        manuscript = manuscriptService.save(loaded);
        openBook();

        assertThatThrownBy(() -> run("/search-replace-lore-entries Harry Ron")).isInstanceOf(ExecutionException.class);
    }

}
