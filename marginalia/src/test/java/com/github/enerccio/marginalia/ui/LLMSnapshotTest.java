package com.github.enerccio.marginalia.ui;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The tree the help assistant's model reads: paths are the simple class names from the root, children are the nearest
 * visible elements, an element that fails does not break the tree.
 */
class LLMSnapshotTest {

    static class Panel extends Div implements LLMInteract {
        private final String state;

        Panel(String state) {
            this.state = state;
        }

        @Override
        public String getLLMState() {
            return state;
        }
    }

    static class Leaf extends Span implements LLMInteract {
    }

    /** Not a component: reports its children itself. */
    static class Part implements LLMInteract {
        private final List<LLMInteract> children;

        Part(LLMInteract... children) {
            this.children = List.of(children);
        }

        @Override
        public List<LLMInteract> getLLMChildren(boolean deep) {
            return children;
        }
    }

    @Test
    void pathsAreSimpleClassNamesFromTheRoot() {
        Panel panel = new Panel("The user is editing **Moby Dick**.");
        Div plain = new Div();
        Leaf leaf = new Leaf();
        plain.add(leaf);
        panel.add(plain);

        LLMSnapshot snapshot = panel.takeLLMSnapshot();

        assertThat(snapshot.path()).isEqualTo("Panel");
        assertThat(snapshot.state()).isEqualTo("The user is editing **Moby Dick**.");
        assertThat(snapshot.children()).extracting(LLMSnapshot::path).containsExactly("Panel/Leaf");
    }

    @Test
    void aBareTextNodeInTheTreeIsNotAProblem() {
        // Component.isVisible() throws for a Text (found in the real UI: a Div with new Text(...) among its children)
        Panel panel = new Panel("");
        Div plain = new Div(new com.vaadin.flow.component.Text("some text"), new Leaf());
        panel.add(plain);

        LLMSnapshot snapshot = panel.takeLLMSnapshot();

        assertThat(snapshot.children()).extracting(LLMSnapshot::path).containsExactly("Panel/Leaf");
        assertThat(panel.takeLLMSnapshot(true).children()).hasSize(1);
    }

    @Test
    void invisibleComponentsAndWhatIsInsideThemAreNotListed() {
        Panel panel = new Panel("");
        Leaf shown = new Leaf();
        Div hidden = new Div(new Leaf());
        hidden.setVisible(false);
        Leaf hiddenLeaf = new Leaf();
        hiddenLeaf.setVisible(false);
        panel.add(shown, hidden, hiddenLeaf);

        assertThat(panel.takeLLMSnapshot().children()).hasSize(1);
    }

    @Test
    void anElementReportsItsOwnChildrenNotTheOnesBelowIt() {
        Panel inner = new Panel("inner");
        inner.add(new Leaf());
        Panel outer = new Panel("outer");
        outer.add(new Div(inner));

        LLMSnapshot snapshot = outer.takeLLMSnapshot();

        assertThat(snapshot.children()).extracting(LLMSnapshot::path).containsExactly("Panel/Panel");
        assertThat(snapshot.children().getFirst().children()).extracting(LLMSnapshot::path)
                .containsExactly("Panel/Panel/Leaf");
    }

    @Test
    void elementsThatAreNotComponentsListTheirChildren() {
        Part part = new Part(new Leaf(), new Part());

        LLMSnapshot snapshot = part.takeLLMSnapshot();

        assertThat(snapshot.path()).isEqualTo("Part");
        assertThat(snapshot.children()).extracting(LLMSnapshot::path).containsExactly("Part/Leaf", "Part/Part");
    }

    @Test
    void siblingsWithTheSameNameAreNumbered() {
        Panel panel = new Panel("");
        Panel first = new Panel("first");
        first.add(new Leaf());
        panel.add(first, new Panel("second"), new Leaf());

        LLMSnapshot snapshot = panel.takeLLMSnapshot();

        assertThat(snapshot.children()).extracting(LLMSnapshot::path)
                .containsExactly("Panel/Panel[1]", "Panel/Panel[2]", "Panel/Leaf");
        assertThat(snapshot.children().getFirst().children()).extracting(LLMSnapshot::path)
                .containsExactly("Panel/Panel[1]/Leaf");
        assertThat(snapshot.find("panel/panel[2]")).get().extracting(LLMSnapshot::state).isEqualTo("second");
    }

    @Test
    void findAcceptsSlashesAndCaseAndReturnsTheSubtree() {
        Panel panel = new Panel("root");
        Panel child = new Panel("child");
        child.add(new Leaf());
        panel.add(child);
        LLMSnapshot snapshot = panel.takeLLMSnapshot();

        assertThat(snapshot.find("")).contains(snapshot);
        assertThat(snapshot.find(null)).contains(snapshot);
        assertThat(snapshot.find("/Panel/Panel/")).get().extracting(LLMSnapshot::state).isEqualTo("child");
        assertThat(snapshot.find("Panel/Panel/Leaf")).isPresent();
        assertThat(snapshot.find("Panel/Nope")).isEmpty();
    }

    @Test
    void rendersMarkdownWithHeadingsByDepth() {
        Panel panel = new Panel("Root state.");
        Panel child = new Panel("Child state.");
        child.add(new Leaf());
        panel.add(child);

        assertThat(panel.takeLLMSnapshot().toMarkdown()).isEqualTo("""
                ## Panel
                Root state.

                ### Panel/Panel
                Child state.

                #### Panel/Panel/Leaf""");
        assertThat(panel.takeLLMSnapshot().toOutline()).isEqualTo("- Panel\n  - Panel/Panel\n    - Panel/Panel/Leaf");
    }

    @Test
    void aDialogDescribesItselfByItsTitle() {
        class TitledDialog extends Dialog implements LLMInteract {
        }
        TitledDialog dialog = new TitledDialog();
        dialog.setHeaderTitle("Edit model");

        assertThat(dialog.takeLLMSnapshot().state()).isEqualTo("A dialog is open, titled \"Edit model\".");
        assertThat(dialog.takeLLMSnapshot().path()).isEqualTo("TitledDialog");
    }

    @Test
    void anElementThatFailsIsListedAndTheRestIsKept() {
        Panel good = new Panel("fine");
        LLMInteract broken = new LLMInteract() {
            @Override
            public String getLLMName() {
                return "Broken";
            }

            @Override
            public String getLLMState() {
                throw new IllegalStateException("boom");
            }
        };

        LLMSnapshot snapshot = new Part(broken, good).takeLLMSnapshot();

        assertThat(snapshot.children()).extracting(LLMSnapshot::path).containsExactly("Part/Broken", "Part/Panel");
        assertThat(snapshot.children().getFirst().state()).contains("could not be read");
        assertThat(snapshot.children().get(1).state()).isEqualTo("fine");
    }

    @Test
    void aCycleIsCutInsteadOfOverflowingTheStack() {
        Part[] loop = new Part[1];
        loop[0] = new Part() {
            @Override
            public List<LLMInteract> getLLMChildren(boolean deep) {
                return List.of(loop[0]);
            }
        };

        LLMSnapshot snapshot = loop[0].takeLLMSnapshot();

        int depth = 0;
        for (LLMSnapshot node = snapshot; !node.children().isEmpty(); node = node.children().getFirst()) {
            depth++;
        }
        assertThat(depth).isLessThanOrEqualTo(LLMSnapshot.MAX_DEPTH);
    }

    @Test
    void ofComponentTreeIsTheRootOfAPage() {
        Div page = new Div();
        page.add(new Panel("a"), new Div(new Leaf()));

        LLMSnapshot snapshot = LLMSnapshot.ofComponentTree("UI", "A page.", page, false);

        assertThat(snapshot.path()).isEqualTo("UI");
        assertThat(snapshot.state()).isEqualTo("A page.");
        assertThat(snapshot.children()).extracting(LLMSnapshot::path).containsExactly("UI/Panel", "UI/Leaf");
    }

    @Test
    void withinAddsTheElementsOfTheComponentToThoseOfThePart() {
        Div created = new Div(new Div(new Leaf()));
        Leaf own = new Leaf();

        LLMSnapshot part = LLMInteract.within(new Part(own), created).takeLLMSnapshot();

        assertThat(part.path()).isEqualTo("Part");
        assertThat(part.children()).extracting(LLMSnapshot::path).containsExactly("Part/Leaf[1]", "Part/Leaf[2]");
        assertThat(LLMInteract.within(own, null)).isSameAs(own);
    }

    static class Books extends Panel {
        Books() {
            super("The list of books.");
            add(new Leaf());
        }
    }

    static class Lorebooks extends Panel {
        Lorebooks() {
            super("The list of lorebooks.");
            add(new Leaf());
        }
    }

    @Test
    void onlyTheActiveAlternativeIsExpandedTheOthersAreListed() {
        Books books = new Books();
        Lorebooks lorebooks = new Lorebooks();
        LLMInteract tabs = new Part() {
            @Override
            public String getLLMName() {
                return "Tabs";
            }

            @Override
            public List<LLMInteract> getLLMChildren(boolean deep) {
                return LLMInteract.options(List.of(books, lorebooks), lorebooks, deep);
            }
        };

        LLMSnapshot snapshot = tabs.takeLLMSnapshot();

        assertThat(snapshot.children()).extracting(LLMSnapshot::path).containsExactly("Tabs/Books", "Tabs/Lorebooks");
        LLMSnapshot inactive = snapshot.find("Tabs/Books").orElseThrow();
        assertThat(inactive.state()).startsWith("Not open");
        assertThat(inactive.children()).isEmpty();
        assertThat(snapshot.find("Tabs/Lorebooks").orElseThrow().state()).isEqualTo("The list of lorebooks.");
        assertThat(snapshot.find("Tabs/Lorebooks").orElseThrow().children()).hasSize(1);
    }

    @Test
    void aDeepSnapshotExpandsTheAlternativesAndMarksTheHiddenComponents() {
        Books books = new Books();
        Lorebooks lorebooks = new Lorebooks();
        Div hidden = new Div(new Leaf());
        hidden.setVisible(false);
        Panel host = new Panel("host") {
            @Override
            public List<LLMInteract> getLLMChildren(boolean deep) {
                List<LLMInteract> children = new java.util.ArrayList<>(LLMInteract.options(List.of(books, lorebooks), lorebooks, deep));
                children.addAll(LLMSnapshot.nearestDescendants(this, deep));
                return children;
            }
        };
        host.add(hidden);

        LLMSnapshot active = host.takeLLMSnapshot();
        LLMSnapshot deep = host.takeLLMSnapshot(true);

        assertThat(active.find("Panel/Books").orElseThrow().children()).isEmpty();
        assertThat(active.find("Panel/Books").orElseThrow().state()).startsWith("Not open");
        assertThat(deep.find("Panel/Books").orElseThrow().state()).isEqualTo("The list of books.");
        assertThat(deep.find("Panel/Books/Leaf")).isPresent();
        assertThat(active.find("Panel/Leaf")).as("hidden").isEmpty();
        assertThat(deep.find("Panel/Leaf").orElseThrow().state()).startsWith("*Hidden");
    }

    /** An element with tabs: the tab that is open decides which ones it offers. */
    static class Tabbed extends Div implements LLMInteract {
        final Tabs tabs = new Tabs();
        final List<String> log = new ArrayList<>();

        Tabbed(String... labels) {
            for (String label : labels) {
                tabs.add(new Tab(label));
            }
            add(tabs);
        }

        @Override
        public Map<String, LLMInteraction> getInteraction() {
            List<Tab> all = tabs.getChildren().filter(Tab.class::isInstance).map(Tab.class::cast).toList();
            return LLMInteract.tabs(all, tabs.getSelectedTab(), tab -> {
                log.add(tab.getLabel());
                tabs.setSelectedTab(tab);
            });
        }
    }

    @Test
    void anElementListsWhatItOffersAndTheMarkdownShowsItOnlyWhenAsked() {
        Tabbed tabbed = new Tabbed("One", "Two", "Three");

        LLMSnapshot snapshot = tabbed.takeLLMSnapshot();

        assertThat(snapshot.interactions()).containsOnlyKeys("open-tab:Two", "open-tab:Three");
        assertThat(snapshot.interactions().get("open-tab:Two")).isEqualTo("Open the tab \"Two\".");
        assertThat(snapshot.toMarkdown()).doesNotContain("open-tab");
        assertThat(snapshot.toMarkdown(true)).contains("Interactions:").contains("- `open-tab:Two`: Open the tab \"Two\".");
    }

    @Test
    void anInteractionIsAppliedAndTheSnapshotAfterItReturned() {
        Tabbed tabbed = new Tabbed("One", "Two", "Three");

        LLMSnapshot after = tabbed.applyInteraction("open-tab:Three");

        assertThat(tabbed.log).containsExactly("Three");
        assertThat(after.interactions()).containsOnlyKeys("open-tab:One", "open-tab:Two");
    }

    @Test
    void aSequenceIsAppliedInOrderAndLookedUpAgainBeforeEachStep() {
        Tabbed tabbed = new Tabbed("One", "Two", "Three");

        // "open-tab:One" is not offered at the start (it is open), only after the first step moved away from it
        LLMSnapshot after = tabbed.applyInteraction(List.of("open-tab:Two", "open-tab:One", "open-tab:Three"));

        assertThat(tabbed.log).containsExactly("Two", "One", "Three");
        assertThat(after.interactions()).containsOnlyKeys("open-tab:One", "open-tab:Two");
    }

    @Test
    void anInteractionThatIsNotOfferedEndsTheSequenceAndSaysWhatWasDone() {
        Tabbed tabbed = new Tabbed("One", "Two", "Three");

        assertThatThrownBy(() -> tabbed.applyInteraction(List.of("open-tab:Two", "open-tab:Two", "open-tab:Three")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("\"open-tab:Two\" is not an interaction of Tabbed after doing open-tab:Two")
                .hasMessageContaining("open-tab:One, open-tab:Three");
        assertThat(tabbed.log).as("what was done stays done").containsExactly("Two");
        assertThat(tabbed.tabs.getSelectedTab().getLabel()).isEqualTo("Two");
    }

    @Test
    void theKeyIsFoundIgnoringCaseAndBlanks() {
        Tabbed tabbed = new Tabbed("One", "Two");

        tabbed.applyInteraction("  OPEN-TAB:two ");

        assertThat(tabbed.log).containsExactly("Two");
    }

    @Test
    void anInteractionThatFailsIsReportedAsSuch() {
        LLMInteract element = new LLMInteract() {
            @Override
            public Map<String, LLMInteraction> getInteraction() {
                return Map.of("boom", new LLMInteraction("Explodes.", () -> {
                    throw new IllegalStateException("no way");
                }));
            }
        };

        assertThatThrownBy(() -> element.applyInteraction("boom")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("\"boom\" failed: no way");
    }

    @Test
    void anElementIsInteractedWithByItsPathInTheSnapshotOfTheTree() {
        Panel panel = new Panel("");
        Tabbed first = new Tabbed("One", "Two");
        Tabbed second = new Tabbed("A", "B");
        panel.add(first, second);
        LLMSnapshot tree = panel.takeLLMSnapshot();

        assertThat(tree.children()).extracting(LLMSnapshot::path).containsExactly("Panel/Tabbed[1]", "Panel/Tabbed[2]");
        tree.interact("Panel/Tabbed[2]", List.of("open-tab:B"));

        assertThat(first.log).isEmpty();
        assertThat(second.log).containsExactly("B");
    }

    @Test
    void anElementWithoutInteractionsOrAnUnknownPathIsRefusedWithAListOfThoseThatHaveThem() {
        Panel panel = new Panel("");
        panel.add(new Tabbed("One", "Two"), new Leaf());
        LLMSnapshot tree = panel.takeLLMSnapshot();

        assertThatThrownBy(() -> tree.interact("Panel/Leaf", List.of("x"))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("offers no interactions").hasMessageContaining("Panel/Tabbed: open-tab:Two");
        assertThatThrownBy(() -> tree.interact("Panel/Nope", List.of("x"))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no element").hasMessageContaining("Panel/Tabbed: open-tab:Two");
    }

    @Test
    void hiddenAndInactiveElementsOfferNothing() {
        Panel panel = new Panel("");
        Tabbed shown = new Tabbed("One", "Two");
        Tabbed hidden = new Tabbed("A", "B");
        hidden.setVisible(false);
        panel.add(shown, hidden);

        LLMSnapshot deep = panel.takeLLMSnapshot(true);

        assertThat(deep.children()).extracting(LLMSnapshot::path).containsExactly("Panel/Tabbed[1]", "Panel/Tabbed[2]");
        assertThat(deep.children().get(0).interactions()).isNotEmpty();
        assertThat(deep.children().get(1).interactions()).as("hidden").isEmpty();
        assertThat(LLMInteract.inactive(shown).getInteraction()).isEmpty();
    }

    @Test
    void aDisabledTabIsNotOffered() {
        Tabbed tabbed = new Tabbed("One", "Two", "Three");
        tabbed.tabs.getTabAt(2).setEnabled(false);

        assertThat(tabbed.getInteraction()).containsOnlyKeys("open-tab:Two");
    }

    @Test
    void anElementWrappedWithItsComponentStillOffersItsInteractions() {
        Tabbed tabbed = new Tabbed("One", "Two");

        LLMInteract wrapped = LLMInteract.within(tabbed, new Div());

        assertThat(wrapped.getInteraction()).containsOnlyKeys("open-tab:Two");
    }
}
