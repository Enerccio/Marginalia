package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.components.TreantTree;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptTreePart;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.vaadin.firitin.layouts.VTabSheet;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The search of the branch view: found parts are counted, and "Show in story" makes the branch of the part active
 * and asks the dialog to show it in the story tab.
 * <pre>
 * root ─ a ─ b1            (active)
 *         └─ b2 ─ c2
 * </pre>
 */
class BranchSearchTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    private Manuscript manuscript;
    private ChatMessage root, a, b1, b2, c2;
    private ManuscriptDialog dialog;
    private Component content;
    private Long shown;

    private ChatMessage child(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        Thread.sleep(3);
        return parent == null
                ? chatMessageService.createRoot(manuscript, message)
                : chatMessageService.addChild(parent, message);
    }

    @BeforeEach
    void createDialog() throws Exception {
        login();
        OpenAICompatible ai = createAI();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        m.setAi(ai);
        manuscript = manuscriptService.save(m);

        root = child(null, "The harbour at dawn");
        a = child(root, "Mara found the lighthouse key");
        b1 = child(a, "A quiet morning");
        b2 = child(a, "The key was stolen");
        c2 = child(b2, "Nobody knew where");
        manuscript.setActiveLeaf(b1);
        manuscript = manuscriptService.save(manuscript);

        // the tree part alone, the dialog only provides the book and receives "show this part"
        dialog = new ManuscriptDialog(manuscript) {
            @Override
            public Manuscript refreshManuscript() throws Exception {
                return manuscript = manuscriptService.find(manuscript);
            }

            @Override
            public Manuscript getManuscript() {
                return manuscript;
            }

            @Override
            public Manuscript save() throws Exception {
                return manuscript = manuscriptService.save(manuscript);
            }

            @Override
            public void showMessage(Long messageId) {
                shown = messageId;
            }
        };
        ManuscriptTreePart treePart = new ManuscriptTreePart(dialog);
        content = treePart.create(new VTabSheet());
        treePart.load(manuscript);
        treePart.onTabEnter();
    }

    private static <T> T find(Component root, Class<T> type) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(c -> Stream.of(c)))
                .map(c -> type.isInstance(c) ? type.cast(c) : null)
                .filter(c -> c != null)
                .findFirst()
                .orElseGet(() -> root.getChildren().map(c -> find(c, type)).filter(c -> c != null).findFirst().orElse(null));
    }

    @Test
    void searchCountsThePartsOfAllBranches() {
        @SuppressWarnings("unchecked")
        ComboBox<String> box = find(content, ComboBox.class);
        Span status = find(box.getParent().orElseThrow(), Span.class);

        box.setValue("key");
        assertThat(status.getText()).contains("2 parts found");

        box.setValue("nothing of this");
        assertThat(status.getText()).isEqualTo("Nothing found.");

        box.clear();
        assertThat(status.getText()).isEmpty();
    }

    @Test
    void showInStorySwitchesTheBranchAndTheTab() throws Exception {
        TreantTree tree = find(content, TreantTree.class);

        ComponentUtil.fireEvent(tree, new TreantTree.NodeGotoEvent(tree, true, String.valueOf(c2.getId())));

        Manuscript reloaded = manuscriptService.find(manuscript);
        assertThat(reloaded.getActiveLeaf().getId()).isEqualTo(c2.getId());
        assertThat(shown).isEqualTo(c2.getId());
    }

    @Test
    void showInStoryKeepsTheBranchWhenThePartIsOnIt() throws Exception {
        TreantTree tree = find(content, TreantTree.class);

        ComponentUtil.fireEvent(tree, new TreantTree.NodeGotoEvent(tree, true, String.valueOf(a.getId())));

        assertThat(manuscriptService.find(manuscript).getActiveLeaf().getId()).isEqualTo(b1.getId());
        assertThat(shown).isEqualTo(a.getId());
    }

    @Test
    void showInStoryIgnoresIdsOfOtherBooks() throws Exception {
        Manuscript other = new Manuscript();
        other.setName(uniqueName("other"));
        other = manuscriptService.save(other);
        ChatMessage foreign = chatMessageService.createRoot(other, new ChatMessage());
        TreantTree tree = find(content, TreantTree.class);

        ComponentUtil.fireEvent(tree, new TreantTree.NodeGotoEvent(tree, true, String.valueOf(foreign.getId())));
        ComponentUtil.fireEvent(tree, new TreantTree.NodeGotoEvent(tree, true, "block-1-2"));

        assertThat(manuscriptService.find(manuscript).getActiveLeaf().getId()).isEqualTo(b1.getId());
        assertThat(shown).isNull();
    }
}
