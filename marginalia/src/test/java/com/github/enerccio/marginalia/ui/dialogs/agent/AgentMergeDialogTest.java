package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Change;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Item;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Kind;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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
}
