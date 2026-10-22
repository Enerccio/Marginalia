package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Change;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Item;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Kind;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.ui.widgets.WordDiffView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The review of the result of a run as the user sees it: the changes in a list they tick, the texts they can write, and the
 * apply and discard buttons.
 */
class AgentMergeDialogTest extends AgentTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private Manuscript book;
    private AgentRun run;
    private final AtomicInteger changes = new AtomicInteger();

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
        book = manuscript();
        ChatMessage first = new ChatMessage();
        first.setResponse("Once upon a time");
        first = chatMessageService.createRoot(book, first);
        ChatMessage second = new ChatMessage();
        second.setResponse("there was a dragon");
        chatMessageService.addChild(first, second);
        Agent agent = agentService.save(newAgent(book));
        run = agentRunService.start(agent);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private ChatMessage part(Manuscript where, String text) throws Exception {
        return chatMessageService.getAllMessages(manuscriptService.find(where.getId())).stream()
                .filter(m -> text.equals(m.getResponse())).findFirst().orElseThrow();
    }

    private void edit(Manuscript where, String from, String to) throws Exception {
        ChatMessage message = chatMessageService.find(part(where, from).getId());
        message.setResponse(to);
        chatMessageService.save(message);
    }

    private ChatMessage add(String parent, String text) throws Exception {
        Manuscript copy = agentRunService.find(run.getId()).getWorkingCopy();
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return chatMessageService.addChild(chatMessageService.find(part(copy, parent).getId()), message);
    }

    private void finish() throws Exception {
        run = agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.DONE, "report");
    }

    private AgentMergeDialog dialog() {
        AgentMergeDialog dialog = new AgentMergeDialog("agent", run, changes::incrementAndGet);
        dialog.create();
        return dialog;
    }

    private Manuscript copy() throws Exception {
        return agentRunService.find(run.getId()).getWorkingCopy();
    }

    private static List<Item> rows(AgentMergeDialog dialog) {
        return dialog.getGrid().getDataProvider().fetch(new Query<>()).toList();
    }

    private static Item row(AgentMergeDialog dialog, Kind kind, Change change, String label) {
        return rows(dialog).stream().filter(i -> i.kind() == kind && i.change() == change && i.label().contains(label)).findFirst().orElseThrow();
    }

    private static List<Component> descendants(Component component) {
        List<Component> found = new ArrayList<>();
        component.getChildren().forEach(child -> {
            found.add(child);
            found.addAll(descendants(child));
        });
        return found;
    }

    @Test
    void whenTheAgentChangedNothingTheDialogSaysSo() throws Exception {
        finish();

        AgentMergeDialog dialog = dialog();

        assertThat(rows(dialog)).isEmpty();
        assertThat(dialog.getSummaryLabel().getText()).isEqualTo("The agent changed nothing in the book.");
        assertThat(dialog.getApplyButton().isEnabled()).isFalse();
    }

    @Test
    void theFindingsOfTheAgentAreListedAboveTheChangesAndThereIsNoSectionWithoutThem() throws Exception {
        finish();
        assertThat(dialog().getFindingsDetails()).as("the agent reported nothing").isNull();

        fileSystemService.writeFindings(run, "[{\"number\":1,\"severity\":\"warning\",\"text\":\"Kira has green eyes in part 3\",\"time\":\"\"}]");
        AgentMergeDialog dialog = dialog();

        assertThat(dialog.getFindingsDetails()).isNotNull();
        assertThat(dialog.getFindingsDetails().isOpened()).isTrue();
        assertThat(dialog.getFindingsDetails().getSummaryText()).isEqualTo("Findings (1)");
        assertThat(descendants(dialog.getFindingsDetails()))
                .anyMatch(c -> c instanceof com.vaadin.flow.component.html.Span span && "Kira has green eyes in part 3".equals(span.getText()))
                .anyMatch(c -> c instanceof com.vaadin.flow.component.html.Span span && "Warning".equals(span.getText()));
    }

    @Test
    void theChangesWithoutAConflictAreTickedAndTheConflictIsNot() throws Exception {
        add("there was a dragon", "The end.");
        edit(copy(), "there was a dragon", "there was a small dragon");
        finish();
        edit(book, "there was a dragon", "there lived a dragon");

        AgentMergeDialog dialog = dialog();

        Item conflict = row(dialog, Kind.MESSAGE, Change.CHANGED, "there");
        Item added = row(dialog, Kind.MESSAGE, Change.ADDED, "The end");
        assertThat(conflict.conflict()).isTrue();
        assertThat(dialog.getGrid().asMultiSelect().getSelectedItems()).containsExactly(added);
        assertThat(dialog.getApplyButton().getText()).endsWith("(1)");
    }

    @Test
    void pickingANewPartPicksThePartItFollowsAndDroppingItDropsWhatFollowsIt() throws Exception {
        ChatMessage end = add("there was a dragon", "The end.");
        Manuscript copy = copy();
        ChatMessage more = new ChatMessage();
        more.setResponse("Or not.");
        chatMessageService.addChild(chatMessageService.find(end.getId()), more);
        finish();
        AgentMergeDialog dialog = dialog();
        Item first = row(dialog, Kind.MESSAGE, Change.ADDED, "The end");
        Item second = row(dialog, Kind.MESSAGE, Change.ADDED, "Or not");

        dialog.getGrid().asMultiSelect().deselectAll();
        dialog.getGrid().asMultiSelect().select(second);
        assertThat(dialog.getGrid().asMultiSelect().getSelectedItems()).containsExactlyInAnyOrder(first, second);

        dialog.getGrid().asMultiSelect().deselect(first);
        assertThat(dialog.getGrid().asMultiSelect().getSelectedItems()).isEmpty();
        assertThat(copy).isNotNull();
    }

    @Test
    void anItemThatCannotBeTakenOverCannotBePicked() throws Exception {
        add("there was a dragon", "The end.");
        finish();
        chatMessageService.deleteNodeAndMigrateChildren(part(book, "there was a dragon"), manuscriptService.find(book.getId()), false);
        AgentMergeDialog dialog = dialog();
        Item blocked = row(dialog, Kind.MESSAGE, Change.ADDED, "The end");
        assertThat(blocked.applicable()).isFalse();

        dialog.getGrid().asMultiSelect().select(blocked);

        assertThat(dialog.getGrid().asMultiSelect().getSelectedItems()).isEmpty();
    }

    @Test
    void theTextTheUserWritesIsWhatIsApplied() throws Exception {
        edit(copy(), "there was a dragon", "there was a small dragon");
        finish();
        edit(book, "there was a dragon", "there lived a dragon");
        AgentMergeDialog dialog = dialog();
        Item conflict = row(dialog, Kind.MESSAGE, Change.CHANGED, "there");
        dialog.show(conflict);
        List<TextArea> areas = descendants(dialog.getDetail()).stream().filter(TextArea.class::isInstance).map(TextArea.class::cast).toList();
        TextArea inBook = areas.stream().filter(TextArea::isReadOnly).findFirst().orElseThrow();
        TextArea toApply = areas.stream().filter(a -> !a.isReadOnly()).findFirst().orElseThrow();
        assertThat(inBook.getValue()).isEqualTo("there lived a dragon");
        assertThat(toApply.getValue()).isEqualTo("there was a small dragon");

        toApply.setValue("there lived a small dragon");
        dialog.getGrid().asMultiSelect().select(conflict);
        dialog.apply();

        assertThat(chatMessageService.getAllMessages(manuscriptService.find(book.getId()))).extracting(ChatMessage::getResponse)
                .containsExactlyInAnyOrder("Once upon a time", "there lived a small dragon");
        assertThat(changes.get()).isEqualTo(1);
        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.APPLIED);
    }

    private static String spansOf(Component view, String className) {
        return descendants(view).stream().filter(Span.class::isInstance).map(Span.class::cast)
                .filter(span -> span.getClassNames().contains(className)).map(Span::getText).reduce("", String::concat);
    }

    @Test
    void theWordsThatChangeAreShownAndFollowWhatTheUserWrites() throws Exception {
        edit(copy(), "there was a dragon", "there was a small dragon");
        finish();
        edit(book, "there was a dragon", "there lived a dragon");
        AgentMergeDialog dialog = dialog();
        Item conflict = row(dialog, Kind.MESSAGE, Change.CHANGED, "there");

        dialog.show(conflict);

        List<WordDiffView> views = descendants(dialog.getDetail()).stream().filter(WordDiffView.class::isInstance).map(WordDiffView.class::cast).toList();
        // what is applied, what the agent did and what the user did, as it is a conflict
        assertThat(views).hasSize(3);
        assertThat(spansOf(views.get(0), CssStyles.MERGE_DIFF_REMOVED)).isEqualTo("lived");
        assertThat(spansOf(views.get(0), CssStyles.MERGE_DIFF_ADDED)).contains("was").contains("small");
        assertThat(spansOf(views.get(1), CssStyles.MERGE_DIFF_ADDED)).isEqualTo("small ");
        assertThat(spansOf(views.get(2), CssStyles.MERGE_DIFF_ADDED)).isEqualTo("lived");
        TextArea toApply = descendants(dialog.getDetail()).stream().filter(TextArea.class::isInstance).map(TextArea.class::cast)
                .filter(area -> !area.isReadOnly()).findFirst().orElseThrow();

        toApply.setValue("there lived a dragon");

        assertThat(views.get(0).getChildren().map(c -> ((Span) c).getText()).toList()).containsExactly("No difference.");
    }

    @Test
    void aFieldOfAChangedItemCanBeLeftOut() throws Exception {
        ChatMessage copied = chatMessageService.find(part(copy(), "there was a dragon").getId());
        copied.setResponse("there was a small dragon");
        copied.setSceneSetting("a cave");
        chatMessageService.save(copied);
        finish();
        AgentMergeDialog dialog = dialog();
        Item item = row(dialog, Kind.MESSAGE, Change.CHANGED, "there");
        dialog.show(item);
        List<Checkbox> takes = descendants(dialog.getDetail()).stream().filter(Checkbox.class::isInstance).map(Checkbox.class::cast).toList();
        assertThat(takes).hasSize(2).allMatch(Checkbox::getValue);

        takes.get(1).setValue(false);

        assertThat(dialog.getSkipped().get(item.id())).containsExactly("sceneSetting");
        dialog.apply();
        ChatMessage taken = part(book, "there was a small dragon");
        assertThat(taken.getSceneSetting()).isNullOrEmpty();
    }

    @Test
    void anItemWithOneFieldHasNothingToLeaveOut() throws Exception {
        edit(copy(), "there was a dragon", "there was a small dragon");
        finish();
        AgentMergeDialog dialog = dialog();

        dialog.show(row(dialog, Kind.MESSAGE, Change.CHANGED, "there"));

        assertThat(descendants(dialog.getDetail())).noneMatch(Checkbox.class::isInstance);
    }

    @Test
    void theDetailOfAnItemOpensTheCopyAtItsPart() throws Exception {
        add("there was a dragon", "The end.");
        finish();
        AgentMergeDialog dialog = dialog();
        Item added = row(dialog, Kind.MESSAGE, Change.ADDED, "The end");
        dialog.show(added);
        Button inCopy = descendants(dialog.getDetail()).stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> "Show in the copy".equals(button.getText())).findFirst().orElseThrow();

        inCopy.click();

        assertThat(dialog.getCopyDialog()).isNotNull();
        assertThat(dialog.getCopyDialog().isOpened()).isTrue();
        AgentCopyView view = (AgentCopyView) dialog.getCopyDialog().getChildren().filter(AgentCopyView.class::isInstance).findFirst().orElseThrow();
        assertThat(view.getCards()).hasSize(3);
        assertThat(view.getCards()).containsKey(added.sourceUuid());
    }

    @Test
    void anItemTheAgentDeletedHasNothingInTheCopyToShow() throws Exception {
        chatMessageService.deleteNodeAndMigrateChildren(part(copy(), "there was a dragon"), copy(), false);
        finish();
        AgentMergeDialog dialog = dialog();

        dialog.show(row(dialog, Kind.MESSAGE, Change.DELETED, "there was"));

        assertThat(descendants(dialog.getDetail())).noneMatch(c -> c instanceof Button button && "Show in the copy".equals(button.getText()));
    }

    @Test
    void theAgentsTextIsAppliedWhenTheUserWritesNothing() throws Exception {
        add("there was a dragon", "The end.");
        edit(copy(), "there was a dragon", "there was a small dragon");
        finish();
        AgentMergeDialog dialog = dialog();

        dialog.apply();

        assertThat(chatMessageService.getAllMessages(manuscriptService.find(book.getId()))).extracting(ChatMessage::getResponse)
                .containsExactlyInAnyOrder("Once upon a time", "there was a small dragon", "The end.");
        assertThat(changes.get()).isEqualTo(1);
    }

    @Test
    void discardingThrowsTheResultAwayAndLeavesTheBook() throws Exception {
        edit(copy(), "there was a dragon", "there was a small dragon");
        finish();
        AgentMergeDialog dialog = dialog();

        dialog.discard();

        assertThat(agentRunService.find(run.getId()).getStatus()).isEqualTo(AgentRunStatus.STOPPED_DISCARDED);
        assertThat(agentRunService.find(run.getId()).getWorkingCopy()).isNull();
        assertThat(chatMessageService.getAllMessages(manuscriptService.find(book.getId()))).extracting(ChatMessage::getResponse)
                .containsExactlyInAnyOrder("Once upon a time", "there was a dragon");
        assertThat(changes.get()).isEqualTo(1);
    }

    // --- the data of the attributes is JSON, put together in a dialog of its own

    private void setData(Manuscript where, String partText, String json) throws Exception {
        ChatMessage stored = chatMessageService.find(part(where, partText).getId());
        stored.getAttributes().add("templateVariables", com.google.gson.JsonParser.parseString(json));
        chatMessageService.save(stored);
    }

    private static List<Button> buttons(Component component) {
        return descendants(component).stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
    }

    private static Button button(Component component, String text) {
        return buttons(component).stream().filter(b -> text.equals(b.getText())).findFirst()
                .orElseThrow(() -> new AssertionError("no button " + text + " in " + buttons(component).stream().map(Button::getText).toList()));
    }

    @Test
    void theDataOfAFieldIsShownPrettyPrintedWithWhatIsAppliedAndHowToPutItTogether() throws Exception {
        setData(copy(), "there was a dragon", "{\"mood\":\"dark\"}");
        finish();
        setData(book, "there was a dragon", "{\"mood\":\"light\",\"pace\":\"fast\"}");
        AgentMergeDialog dialog = dialog();
        Item item = row(dialog, Kind.MESSAGE, Change.CHANGED, "there");

        dialog.show(item);

        List<TextArea> areas = descendants(dialog.getDetail()).stream().filter(TextArea.class::isInstance).map(TextArea.class::cast).toList();
        assertThat(areas).extracting(TextArea::getValue).contains("{\n  \"mood\": \"light\",\n  \"pace\": \"fast\"\n}",
                "{\n  \"mood\": \"dark\",\n  \"pace\": \"fast\"\n}");
        assertThat(button(dialog.getDetail(), "Merge JSON data")).isNotNull();
        assertThat(descendants(dialog.getDetail()).stream().filter(Span.class::isInstance).map(Span.class::cast).map(Span::getText))
                .anyMatch(text -> text.startsWith("1 conflicts in the data"));
    }

    @Test
    void whatTheUserDecidesInTheJsonDialogIsWhatIsApplied() throws Exception {
        setData(copy(), "there was a dragon", "{\"mood\":\"dark\"}");
        finish();
        setData(book, "there was a dragon", "{\"mood\":\"light\",\"pace\":\"fast\"}");
        AgentMergeDialog dialog = dialog();
        Item item = row(dialog, Kind.MESSAGE, Change.CHANGED, "there");
        dialog.show(item);

        button(dialog.getDetail(), "Merge JSON data").click();
        JsonMergeDialog json = dialog.getJsonDialog();
        assertThat(json).isNotNull();
        var mood = json.getGrid().getDataProvider().fetchChildren(new com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery<>(null, json.getLevel()))
                .filter(n -> n.title().equals("mood")).findFirst().orElseThrow();
        json.getGrid().select(mood);
        button(json, "Use the book's").click();
        json.getTakeButton().click();

        assertThat(dialog.getEdits().get(item.id())).containsEntry("attributes_templateVariables", "{\"mood\":\"light\",\"pace\":\"fast\"}");
        dialog.getGrid().asMultiSelect().select(item);
        dialog.apply();

        assertThat(chatMessageService.find(part(book, "there was a dragon").getId()).getAttributes().get("templateVariables").toString())
                .isEqualTo("{\"mood\":\"light\",\"pace\":\"fast\"}");
    }

    @Test
    void theDialogRemembersWhatWasDecidedAndStartingOverGivesItUp() throws Exception {
        setData(copy(), "there was a dragon", "{\"mood\":\"dark\"}");
        finish();
        setData(book, "there was a dragon", "{\"mood\":\"light\"}");
        AgentMergeDialog dialog = dialog();
        Item item = row(dialog, Kind.MESSAGE, Change.CHANGED, "there");
        dialog.show(item);
        button(dialog.getDetail(), "Merge JSON data").click();
        JsonMergeDialog first = dialog.getJsonDialog();
        var mood = first.getGrid().getDataProvider().fetchChildren(new com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery<>(null, first.getLevel()))
                .findFirst().orElseThrow();
        first.getGrid().select(mood);
        button(first, "Use the book's").click();
        first.getTakeButton().click();
        assertThat(dialog.getEdits().get(item.id())).containsKey("attributes_templateVariables");

        // opened again, the decision is still there
        button(dialog.getDetail(), "Merge JSON data").click();
        assertThat(dialog.getJsonDialog().getLevel().unresolved()).isZero();

        button(dialog.getDetail(), "Start over").click();

        assertThat(dialog.getEdits().getOrDefault(item.id(), java.util.Map.of())).doesNotContainKey("attributes_templateVariables");
        button(dialog.getDetail(), "Merge JSON data").click();
        assertThat(dialog.getJsonDialog().getLevel().unresolved()).isEqualTo(1);
    }

    @Test
    void theAgentsOrTheBooksDataCanBeTakenAsItIs() throws Exception {
        setData(copy(), "there was a dragon", "{\"mood\":\"dark\"}");
        finish();
        setData(book, "there was a dragon", "{\"pace\":\"fast\"}");
        AgentMergeDialog dialog = dialog();
        Item item = row(dialog, Kind.MESSAGE, Change.CHANGED, "there");
        dialog.show(item);

        button(dialog.getDetail(), "Use the agent's").click();
        assertThat(dialog.getEdits().get(item.id())).containsEntry("attributes_templateVariables", "{\"mood\":\"dark\"}");
        button(dialog.getDetail(), "Use the book's").click();
        assertThat(dialog.getEdits().get(item.id())).containsEntry("attributes_templateVariables", "{\"pace\":\"fast\"}");
    }
}
