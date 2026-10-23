package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.bound.AgenticSessionPoint;
import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.CommandScopes;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.test.AgenticRequestAttributes;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@code list-annotations}, {@code get-annotation} and the commands that change the annotations, run in a session (the book is
 * held by the {@link AgenticSessionPoint}). Parts and annotations are addressed by handle (their uuid).
 * <p>
 * The story: 1 "Anna met John at the bar." with two children, 2 "They talked for hours. Then she left." (active) and
 * 2 "Another John." (a swipe).
 */
class AnnotationCommandsTest extends GenerationTestBase {

    @Autowired
    private CommandService commands;

    @Autowired
    private CommandScopes commandScopes;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AnnotationService annotationService;

    private AgenticRequestAttributes session;
    private Manuscript manuscript;
    private ChatMessage first, second, other;
    private boolean answer = true;

    @BeforeEach
    void setUp() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        first = add(null, "Anna met John at the bar.");
        second = add(first, "They talked for hours. Then she left.");
        other = add(first, "Another John.");
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(second);
        manuscript = manuscriptService.save(loaded);
        session = AgenticRequestAttributes.install(new AgenticSessionPoint(manuscript));
    }

    @AfterEach
    void tearDown() {
        session.close();
    }

    private ChatMessage add(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        message.setWordCount(chatMessageService.countWords(text));
        Thread.sleep(3);  // messages are ordered by creation time
        return parent == null ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(parent, message);
    }

    private CommandScope scope() {
        CommandScope session = commandScopes.session();
        return new CommandScope() {
            @Override
            public Manuscript getManuscript() throws Exception {
                return session.getManuscript();
            }

            @Override
            public Lorebook getLorebook() throws Exception {
                return session.getLorebook();
            }

            @Override
            public AgentRun getRun() {
                return session.getRun();
            }

            @Override
            public boolean confirm(CommandContext context, String question) {
                return answer;
            }

            @Override
            public ChatMessage resolveMessage(String uuid) throws Exception {
                return session.resolveMessage(uuid);
            }

            @Override
            public Lorebook resolveLorebook(String uuid) throws Exception {
                return session.resolveLorebook(uuid);
            }

            @Override
            public LorebookEntry resolveEntry(String uuid) throws Exception {
                return session.resolveEntry(uuid);
            }
        };
    }

    private JsonObject run(String line) throws Exception {
        Object result = commands.executeLine(line, false, scope()).get(30, TimeUnit.SECONDS);
        return JsonParser.parseString((String) result).getAsJsonObject();
    }

    private Annotation reload(String handle) throws Exception {
        return annotationService.findForUser(handle);
    }

    private Annotation annotate(ChatMessage part, String passage, String note) throws Exception {
        int start = chatMessageService.find(part.getId()).getResponse().indexOf(passage);
        return annotationService.create(manuscript, part, start, start + passage.length(), note, AnnotationSource.AUTHOR, null, null);
    }

    private static List<String> handles(JsonObject answer) {
        JsonArray rows = answer.getAsJsonArray("annotations");
        return rows.asList().stream().map(row -> row.getAsJsonObject().get("handle").getAsString()).toList();
    }

    // ------------------------------------------------------------------------------------------------ create

    @Test
    void anAnnotationIsCreatedOnTheQuote() throws Exception {
        JsonObject result = run("/create-annotation " + second.getUuid() + " \"Weak scene\" \"They talked for hours.\" pacing");

        Annotation annotation = reload(result.get("handle").getAsString());
        assertThat(annotation.getText()).isEqualTo("Weak scene");
        assertThat(annotation.getType()).isEqualTo("pacing");
        assertThat(annotation.getSource()).isEqualTo(AnnotationSource.AUTHOR);
        assertThat(annotation.getStart()).isZero();
        assertThat(annotation.getEnd()).isEqualTo("They talked for hours.".length());
        assertThat(result.get("part").getAsString()).isEqualTo(second.getUuid());
        assertThat(result.get("position").getAsInt()).isEqualTo(2);
        assertThat(result.get("status").getAsString()).isEqualTo("open");
        assertThat(result.get("start").getAsInt()).isZero();
        assertThat(result.get("quote").getAsString()).isEqualTo("They talked for hours.");
        assertThat(result.has("hint")).isFalse();
    }

    @Test
    void withoutAQuoteTheNoteIsAboutTheWholePart() throws Exception {
        JsonObject result = run("/create-annotation " + first.getUuid() + " \"Rewrite this\"");

        Annotation annotation = reload(result.get("handle").getAsString());
        assertThat(annotation.hasRange()).isFalse();
        assertThat(annotation.isNotLocated()).isFalse();
        assertThat(result.has("start")).isFalse();
        assertThat(result.has("hint")).isFalse();
    }

    @Test
    void aQuoteThatIsNotThereMakesAWholePartNoteAndSaysSo() throws Exception {
        JsonObject result = run("/create-annotation " + first.getUuid() + " \"Contradicts the lore\" \"A dragon landed on the roof of the church\"");

        Annotation annotation = reload(result.get("handle").getAsString());
        assertThat(annotation.hasRange()).isFalse();
        assertThat(annotation.isNotLocated()).isTrue();
        assertThat(result.get("notLocated").getAsBoolean()).isTrue();
        assertThat(result.get("hint").getAsString()).contains("reattach-annotation");
    }

    @Test
    void aPartIsGivenByItsHandleOnly() {
        assertThatThrownBy(() -> run("/create-annotation 1 \"note\" \"Anna\""))
                .isInstanceOf(ExecutionException.class).hasMessageContaining("handle");
    }

    @Test
    void aPartOfAnotherBookIsRefused() throws Exception {
        Manuscript otherBook = manuscriptService.save(newManuscript());
        ChatMessage stranger = new ChatMessage();
        stranger.setResponse("Not in this book.");
        stranger = chatMessageService.createRoot(otherBook, stranger);
        String handle = stranger.getUuid();

        assertThatThrownBy(() -> run("/create-annotation " + handle + " \"note\" \"Not\""))
                .isInstanceOf(ExecutionException.class);
    }

    // ------------------------------------------------------------------------------------------------ list, get

    @Test
    void theOpenAnnotationsAreListedInTheOrderOfTheStory() throws Exception {
        Annotation inSecond = annotate(second, "Then she left.", "Abrupt");
        Annotation early = annotate(second, "They talked", "Weak");
        Annotation inFirst = annotate(first, "Anna", "Name?");
        Annotation inOther = annotate(other, "Another", "A swipe");
        Annotation resolved = annotate(first, "the bar", "Which bar?");
        annotationService.setStatus(manuscript, resolved, AnnotationStatus.RESOLVED);

        JsonObject open = run("/list-annotations");

        assertThat(handles(open)).containsExactly(inFirst.getUuid(), early.getUuid(), inSecond.getUuid(), inOther.getUuid());
        assertThat(open.get("total").getAsInt()).isEqualTo(4);
        JsonObject row = open.getAsJsonArray("annotations").get(2).getAsJsonObject();
        assertThat(row.get("note").getAsString()).isEqualTo("Abrupt");
        assertThat(row.get("part").getAsString()).isEqualTo(second.getUuid());
        assertThat(row.get("position").getAsInt()).isEqualTo(2);
        assertThat(row.get("start").getAsInt()).isEqualTo(second.getResponse().indexOf("Then"));
        // a part of another branch has no position
        assertThat(open.getAsJsonArray("annotations").get(3).getAsJsonObject().has("position")).isFalse();

        assertThat(handles(run("/list-annotations resolved"))).containsExactly(resolved.getUuid());
        assertThat(handles(run("/list-annotations all"))).hasSize(5);
    }

    @Test
    void theListCanBeLimitedToAPartAndPaged() throws Exception {
        Annotation a = annotate(second, "They talked", "one");
        Annotation b = annotate(second, "Then she left.", "two");
        annotate(first, "Anna", "three");

        assertThat(handles(run("/list-annotations open " + second.getUuid()))).containsExactly(a.getUuid(), b.getUuid());
        // a position works as well on a read
        assertThat(handles(run("/list-annotations open 2"))).containsExactly(a.getUuid(), b.getUuid());
        JsonObject page = run("/list-annotations open " + second.getUuid() + " 1 1");
        assertThat(handles(page)).containsExactly(b.getUuid());
        assertThat(page.get("total").getAsInt()).isEqualTo(2);
        assertThat(page.get("offset").getAsInt()).isEqualTo(1);
        assertThat(page.get("truncated").getAsBoolean()).isFalse();
        assertThat(run("/list-annotations open " + second.getUuid() + " 0 1").get("truncated").getAsBoolean()).isTrue();
    }

    @Test
    void anUnknownStatusIsRefused() {
        assertThatThrownBy(() -> run("/list-annotations nonsense")).isInstanceOf(ExecutionException.class)
                .hasMessageContaining("nonsense");
    }

    @Test
    void getShowsTheWholeQuoteAndItsSurroundings() throws Exception {
        Annotation annotation = annotate(second, "Then she left.", "Abrupt");

        JsonObject result = run("/get-annotation " + annotation.getUuid());

        assertThat(result.get("note").getAsString()).isEqualTo("Abrupt");
        assertThat(result.get("quote").getAsString()).isEqualTo("Then she left.");
        assertThat(result.get("before").getAsString()).isEqualTo("They talked for hours. ");
        assertThat(result.get("after").getAsString()).isEmpty();
        assertThat(result.get("sendToPrompt").getAsBoolean()).isFalse();
    }

    @Test
    void anAnnotationOfAnotherBookIsNotFound() throws Exception {
        Manuscript otherBook = manuscriptService.save(newManuscript());
        ChatMessage stranger = new ChatMessage();
        stranger.setResponse("Not in this book.");
        stranger = chatMessageService.createRoot(otherBook, stranger);
        Annotation foreign = annotationService.create(otherBook, stranger, 0, 3, "Foreign", AnnotationSource.AUTHOR, null, null);

        assertThatThrownBy(() -> run("/get-annotation " + foreign.getUuid())).isInstanceOf(ExecutionException.class);
        assertThatThrownBy(() -> run("/resolve-annotation " + foreign.getUuid())).isInstanceOf(ExecutionException.class);
        assertThat(handles(run("/list-annotations all"))).doesNotContain(foreign.getUuid());
    }

    // ------------------------------------------------------------------------------------------------ change

    @Test
    void updateChangesTheNoteTheTypeAndTheFlag() throws Exception {
        Annotation annotation = annotate(second, "Then she left.", "Abrupt");

        run("/update-annotation " + annotation.getUuid() + " \"Too abrupt\"");
        assertThat(reload(annotation.getUuid()).getText()).isEqualTo("Too abrupt");

        run("/update-annotation " + annotation.getUuid() + " \"\" pacing true");
        Annotation updated = reload(annotation.getUuid());
        assertThat(updated.getText()).isEqualTo("Too abrupt");
        assertThat(updated.getType()).isEqualTo("pacing");
        assertThat(updated.isSendToPrompt()).isTrue();

        run("/update-annotation " + annotation.getUuid() + " \"Fine\" pacing false");
        assertThat(reload(annotation.getUuid()).isSendToPrompt()).isFalse();
    }

    @Test
    void resolveClosesAndReopens() throws Exception {
        Annotation annotation = annotate(second, "Then she left.", "Abrupt");

        assertThat(run("/resolve-annotation " + annotation.getUuid()).get("status").getAsString()).isEqualTo("resolved");
        assertThat(reload(annotation.getUuid()).getStatus()).isEqualTo(AnnotationStatus.RESOLVED);

        run("/resolve-annotation " + annotation.getUuid() + " dismissed");
        assertThat(reload(annotation.getUuid()).getStatus()).isEqualTo(AnnotationStatus.DISMISSED);

        run("/resolve-annotation " + annotation.getUuid() + " open");
        assertThat(reload(annotation.getUuid()).getStatus()).isEqualTo(AnnotationStatus.OPEN);

        assertThatThrownBy(() -> run("/resolve-annotation " + annotation.getUuid() + " orphaned"))
                .isInstanceOf(ExecutionException.class).hasMessageContaining("orphaned");
    }

    @Test
    void reattachMovesTheAnnotationToTheQuote() throws Exception {
        Annotation annotation = annotate(second, "Then she left.", "Abrupt");
        chatMessageService.storeResponse(manuscript, second, "They talked for hours.");
        assertThat(reload(annotation.getUuid()).getStatus()).isEqualTo(AnnotationStatus.ORPHANED);

        JsonObject result = run("/reattach-annotation " + annotation.getUuid() + " \"talked for hours\"");

        Annotation moved = reload(annotation.getUuid());
        assertThat(moved.getStatus()).isEqualTo(AnnotationStatus.OPEN);
        assertThat(moved.getStart()).isEqualTo(5);
        assertThat(moved.getEnd()).isEqualTo(5 + "talked for hours".length());
        assertThat(result.get("quote").getAsString()).isEqualTo("talked for hours");

        assertThatThrownBy(() -> run("/reattach-annotation " + annotation.getUuid() + " \"a dragon landed on the roof\""))
                .isInstanceOf(ExecutionException.class).hasMessageContaining("quote");

        run("/reattach-annotation " + annotation.getUuid());
        assertThat(reload(annotation.getUuid()).hasRange()).isFalse();
    }

    @Test
    void deleteAsksAndRemovesTheAnnotation() throws Exception {
        Annotation annotation = annotate(second, "Then she left.", "Abrupt");

        answer = false;
        Object declined = commands.executeLine("/delete-annotation " + annotation.getUuid(), false, scope()).get(30, TimeUnit.SECONDS);
        assertThat((String) declined).containsIgnoringCase("nothing was changed");
        assertThat(reload(annotation.getUuid())).isNotNull();

        answer = true;
        JsonObject result = run("/delete-annotation " + annotation.getUuid());
        assertThat(result.get("deleted").getAsBoolean()).isTrue();
        assertThat(reload(annotation.getUuid())).isNull();
        assertThat(annotationService.find(annotation.getId()).isDeleted()).isTrue();
    }

    @Test
    void theCommandsOfAnnotationsAreOfferedToAnAgent() {
        for (String name : List.of("create-annotation", "update-annotation", "resolve-annotation", "reattach-annotation", "delete-annotation")) {
            List<CommandDefinition> definitions = commands.getCommands(name);
            assertThat(definitions).as(name).isNotEmpty();
            assertThat(definitions).as(name).allMatch(CommandDefinition::availableForAgent);
        }
        for (String name : List.of("list-annotations", "get-annotation")) {
            assertThat(commands.getCommands(name)).as(name).isNotEmpty()
                    .allMatch(definition -> definition.availableForAgent() && definition.readOnly());
        }
    }
}
