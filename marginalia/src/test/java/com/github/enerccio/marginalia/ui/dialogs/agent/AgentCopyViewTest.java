package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The read only view of the copy of the book of a run, as the user sees it.
 */
class AgentCopyViewTest extends AgentTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private Manuscript book;
    private AgentRun run;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
        Lorebook root = new Lorebook();
        root.setName("lore");
        root.setSubbooks(new ArrayList<>());
        root = lorebookService.save(root);
        LorebookEntry dragon = new LorebookEntry();
        dragon.setLorebook(root);
        dragon.setName("dragon");
        dragon.setPayload("a red dragon");
        lorebookEntryService.save(dragon);
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setLorebook(root);
        book = manuscriptService.save(manuscript);
        ChatMessage first = new ChatMessage();
        first.setResponse("Once upon a time");
        first = chatMessageService.createRoot(book, first);
        ChatMessage second = new ChatMessage();
        second.setResponse("there was a dragon");
        second = chatMessageService.addChild(first, second);
        book.setActiveLeaf(second);
        book = manuscriptService.save(book);
        run = agentRunService.start(agentService.save(newAgent(book)));
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private AgentCopyView view() {
        AgentCopyView view = new AgentCopyView(() -> {
            try {
                return agentRunService.find(run.getId());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        view.create();
        return view;
    }

    private static String textOf(Component component) {
        return ComponentTree.all(component).filter(c -> c instanceof Span || c instanceof Markdown)
                .map(c -> c instanceof Markdown markdown ? markdown.getContent() : c.getElement().getText())
                .reduce("", (a, b) -> a + "\n" + b);
    }

    private ChatMessage inCopy(String text) throws Exception {
        Manuscript copy = agentRunService.find(run.getId()).getWorkingCopy();
        return chatMessageService.getAllMessages(copy).stream().filter(m -> text.equals(m.getResponse())).findFirst().orElseThrow();
    }

    @Test
    void theStoryOfTheCopyIsShownPartByPart() throws Exception {
        AgentCopyView view = view();

        view.refresh();

        assertThat(view.getNoCopyLabel().isVisible()).isFalse();
        assertThat(view.getPartsBox().getComponentCount()).isEqualTo(2);
        assertThat(textOf(view.getPartsBox())).contains("#1", "Once upon a time", "#2", "there was a dragon");
        assertThat(view.getBranchBox().getValue()).isNotNull();
    }

    @Test
    void whatTheAgentWroteInTheCopyIsShownAfterARefreshAndTheBookIsNotInIt() throws Exception {
        AgentCopyView view = view();
        view.refresh();
        ChatMessage copied = chatMessageService.find(inCopy("there was a dragon").getId());
        copied.setResponse("there was a small dragon");
        chatMessageService.save(copied);

        view.refresh();

        assertThat(textOf(view.getPartsBox())).contains("there was a small dragon").doesNotContain("there was a dragon");
    }

    @Test
    void aPartOfAnotherBranchIsShownWithItsBranchAndTheBranchCanBePicked() throws Exception {
        ChatMessage other = new ChatMessage();
        other.setResponse("or a tower");
        other = chatMessageService.addChild(chatMessageService.find(inCopy("Once upon a time").getId()), other);
        AgentCopyView view = view();

        view.showPart(other.getUuid());

        assertThat(textOf(view.getPartsBox())).contains("or a tower").doesNotContain("there was a dragon");
        assertThat(view.getCards()).containsKey(other.getUuid());
        assertThat(view.getBranchBox().getListDataView().getItemCount()).isEqualTo(2);
        view.getBranchBox().setValue(view.getBranchBox().getListDataView().getItems().filter(branch -> branch.active()).findFirst().orElseThrow());
        assertThat(textOf(view.getPartsBox())).contains("there was a dragon");
    }

    @Test
    void theLorebooksAreListedAndAnEntryShowsItsText() throws Exception {
        AgentCopyView view = view();
        view.refresh();

        List<AgentCopyView.Row> rows = view.getLoreGrid().getListDataView().getItems().toList();
        assertThat(rows).extracting(AgentCopyView.Row::name).containsExactly("lore", "dragon");

        view.getLoreGrid().select(rows.get(1));

        assertThat(ComponentTree.all(view.getLoreDetail()).filter(c -> c instanceof com.vaadin.flow.component.textfield.TextArea)
                .map(c -> ((com.vaadin.flow.component.textfield.TextArea) c).getValue())).contains("a red dragon");
    }

    @Test
    void anEntryTheViewWasToldAboutIsSelectedInTheLorebooks() throws Exception {
        AgentCopyView view = view();
        Manuscript copy = agentRunService.find(run.getId()).getWorkingCopy();
        Lorebook root = lorebookService.find(copy.getLorebook());
        LorebookEntry dragon = lorebookEntryService.getEntriesForLorebook(root).getFirst();

        view.showEntry(dragon.getUuid());

        assertThat(view.getTabs().getSelectedIndex()).isEqualTo(1);
        assertThat(view.getLoreGrid().getSelectedItems()).extracting(AgentCopyView.Row::name).containsExactly("dragon");
    }

    @Test
    void aRunWithoutACopyShowsOnlyTheNote() throws Exception {
        run = agentRunService.stop(agentRunService.pause(agentRunService.find(run.getId())), false);
        AgentCopyView view = view();

        view.refresh();

        assertThat(view.getNoCopyLabel().isVisible()).isTrue();
    }
}
