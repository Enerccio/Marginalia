package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersDefaults;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaWriter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The windows of the libraries work on copies, apply them with Save only, and edit an item in its own window. They are built
 * without a browser: the buttons are found in the component tree and clicked on the server.
 */
class LibraryDialogsTest {

    // UI.setCurrent keeps only a weak reference
    private UI ui;

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private static Stream<Component> all(Component root) {
        Stream<Component> children = root.getChildren().flatMap(LibraryDialogsTest::all);
        if (root instanceof Dialog dialog) {
            children = Stream.concat(children, dialog.getFooter().getElement().getChildren()
                    .map(element -> element.getComponent().orElse(null)).filter(c -> c != null).flatMap(LibraryDialogsTest::all));
        }
        return Stream.concat(Stream.of(root), children);
    }

    private static <C extends Component> Stream<C> all(Component root, Class<C> type) {
        return all(root).filter(type::isInstance).map(type::cast);
    }

    private static Button button(Component root, String text) {
        return all(root, Button.class).filter(button -> text.equals(button.getText()) || text.equals(button.getTooltip().getText()))
                .findFirst().orElseThrow(() -> new AssertionError("No button " + text));
    }

    private static void click(Button button) {
        assertTrue(button.isEnabled(), button.getText() + " is disabled");
        ComponentUtil.fireEvent(button, new ClickEvent<>(button));
    }

    @SuppressWarnings("unchecked")
    private static <T> Grid<T> grid(Component root) {
        return (Grid<T>) all(root, Grid.class).findFirst().orElseThrow();
    }

    private static List<Persona> personas(String... names) {
        List<Persona> personas = new ArrayList<>();
        for (String name : names) {
            personas.add(new Persona(name, "about " + name));
        }
        return personas;
    }

    private static List<String> names(List<Persona> personas) {
        return personas.stream().map(Persona::getName).toList();
    }

    @Test
    void nothingIsAppliedUntilSaveAndTheSourceIsNeverChanged() {
        List<Persona> source = personas("Athena", "Vesper");
        AtomicReference<List<Persona>> applied = new AtomicReference<>();
        PersonaLibraryDialog dialog = new PersonaLibraryDialog("Library", source, applied::set);
        grid(dialog).select(grid(dialog).getListDataView().getItem(0));

        click(button(dialog, "Copy"));

        assertNull(applied.get());
        assertEquals(List.of("Athena", "Vesper"), names(source));
        click(button(dialog, "Save"));
        assertEquals(List.of("Athena", "Athena", "Vesper"), names(applied.get()));
        // the copy is another persona, the first one is the same as in the source
        assertEquals(source.get(0).getUuid(), applied.get().get(0).getUuid());
        assertNotEquals(applied.get().get(0).getUuid(), applied.get().get(1).getUuid());
        assertNotSame(source.get(0), applied.get().get(0));
    }

    @Test
    void cancelAppliesNothing() {
        AtomicReference<List<Persona>> applied = new AtomicReference<>();
        PersonaLibraryDialog dialog = new PersonaLibraryDialog("Library", personas("Athena", "Vesper"), applied::set);
        grid(dialog).select(grid(dialog).getListDataView().getItem(0));
        click(button(dialog, "Copy"));

        click(button(dialog, "Cancel"));

        assertNull(applied.get());
    }

    @Test
    void thePersonasAreMovedUpAndDown() {
        AtomicReference<List<Persona>> applied = new AtomicReference<>();
        PersonaLibraryDialog dialog = new PersonaLibraryDialog("Library", personas("A", "B", "C"), applied::set);
        Grid<Persona> grid = grid(dialog);

        grid.select(grid.getListDataView().getItem(2));
        click(button(dialog, "Move up"));
        click(button(dialog, "Move up"));
        assertFalse(button(dialog, "Move up").isEnabled(), "the first one can't go further up");
        click(button(dialog, "Save"));

        assertEquals(List.of("C", "A", "B"), names(applied.get()));
    }

    @Test
    void theButtonsNeedASelectionAndTheEndsOfTheListLimitTheMoves() {
        PersonaLibraryDialog dialog = new PersonaLibraryDialog("Library", personas("A", "B"), list -> {
        });
        for (String text : List.of("Edit", "Copy", "Delete", "Move up", "Move down")) {
            assertFalse(button(dialog, text).isEnabled(), text);
        }
        assertTrue(button(dialog, "Add").isEnabled());

        Grid<Persona> grid = grid(dialog);
        grid.select(grid.getListDataView().getItem(1));
        assertTrue(button(dialog, "Move up").isEnabled());
        assertFalse(button(dialog, "Move down").isEnabled());
    }

    @Test
    void theStarterPersonasAreAddedWhenTheyAreNotThere() {
        AtomicReference<List<Persona>> applied = new AtomicReference<>();
        // a persona of the user named like a starter one is kept and not added again
        PersonaLibraryDialog dialog = new PersonaLibraryDialog("Library", personas(" athena ", "Mine"), applied::set);

        click(button(dialog, "Add the starter personas"));
        click(button(dialog, "Save"));

        assertEquals(1 + 1 + BetaReadersDefaults.starterPersonas().size() - 1, applied.get().size());
        assertEquals(List.of(" athena ", "Mine"), names(applied.get()).subList(0, 2));
        assertEquals(1, applied.get().stream().filter(p -> p.getName().strip().equalsIgnoreCase("Athena")).count());
    }

    @Test
    void aNewPersonaIsAddedAfterItIsSavedInItsWindow() {
        AtomicReference<List<Persona>> applied = new AtomicReference<>();
        AtomicReference<PersonaDialog> opened = new AtomicReference<>();
        PersonaLibraryDialog library = new PersonaLibraryDialog("Library", personas("Athena"), applied::set) {
            @Override
            protected PersonaDialog createEditor(String header, Persona persona, Predicate<String> nameTaken, Runnable done) {
                opened.set(super.createEditor(header, persona, nameTaken, done));
                return opened.get();
            }
        };

        click(button(library, "Add"));
        Dialog editor = opened.get();
        all(editor, TextField.class).filter(field -> "Name".equals(field.getLabel())).findFirst().orElseThrow().setValue("  Mara ");
        all(editor, TextArea.class).filter(area -> "Description".equals(area.getLabel())).findFirst().orElseThrow().setValue("loves the villain");
        click(button(editor, "Save"));
        click(button(library, "Save"));

        assertEquals(List.of("Athena", "Mara"), names(applied.get()));
        assertEquals("loves the villain", applied.get().get(1).getDescription());
        assertNull(applied.get().get(1).getIcon());
    }

    @Test
    void aPersonaNeedsANameThatNoOtherPersonaHas() {
        List<Persona> items = personas("Athena", "Vesper");
        Persona edited = items.get(1);
        AtomicReference<Boolean> saved = new AtomicReference<>(false);
        PersonaDialog dialog = new PersonaDialog("Edit", edited, name -> name.equalsIgnoreCase("athena"), () -> saved.set(true));
        TextField name = all(dialog, TextField.class).filter(field -> "Name".equals(field.getLabel())).findFirst().orElseThrow();

        name.setValue("   ");
        click(button(dialog, "Save"));
        assertTrue(name.isInvalid());
        assertFalse(saved.get());

        name.setValue("ATHENA");
        click(button(dialog, "Save"));
        assertTrue(name.isInvalid());
        assertFalse(saved.get());
        assertEquals("Vesper", edited.getName(), "nothing is written to the persona before it is valid");

        name.setValue("Mara");
        click(button(dialog, "Save"));
        assertTrue(saved.get());
        assertEquals("Mara", edited.getName());
    }

    @Test
    void theModelWritesANameAndADescriptionForTheUserToKeep() {
        List<String> asked = new ArrayList<>();
        PersonaGenerator generator = (brief, taken, done) -> {
            asked.add(brief + " / " + taken);
            done.accept(new PersonaWriter.Outcome(new PersonaWriter.Written("Rook", "The Plot Hound."), false, null));
            return new CancellationToken();
        };
        Persona edited = new Persona("", "");
        PersonaDialog dialog = new PersonaDialog("New", edited, name -> false, () -> {
        }, generator, () -> List.of("Athena"));
        TextArea brief = all(dialog, TextArea.class).filter(area -> "What the persona is like".equals(area.getLabel())).findFirst().orElseThrow();
        TextField name = all(dialog, TextField.class).filter(field -> "Name".equals(field.getLabel())).findFirst().orElseThrow();
        TextArea description = all(dialog, TextArea.class).filter(area -> "Description".equals(area.getLabel())).findFirst().orElseThrow();

        click(button(dialog, "Write with the model"));
        assertTrue(asked.isEmpty(), "nothing is asked without a wish");

        brief.setValue("a plot hound");
        click(button(dialog, "Write with the model"));

        assertEquals(List.of("a plot hound / [Athena]"), asked);
        assertEquals("Rook", name.getValue());
        assertEquals("The Plot Hound.", description.getValue());
        assertEquals("", edited.getName(), "the persona is changed with Save only");
        assertFalse(brief.isReadOnly());
    }

    @Test
    void aFailedOrEmptyAnswerLeavesTheFieldsAsTheyWere() {
        AtomicReference<PersonaWriter.Outcome> outcome = new AtomicReference<>();
        PersonaGenerator generator = (brief, taken, done) -> {
            done.accept(outcome.get());
            return new CancellationToken();
        };
        PersonaDialog dialog = new PersonaDialog("Edit", new Persona("Mine", "About me"), name -> false, () -> {
        }, generator, List::of);
        TextArea brief = all(dialog, TextArea.class).filter(area -> "What the persona is like".equals(area.getLabel())).findFirst().orElseThrow();
        TextField name = all(dialog, TextField.class).filter(field -> "Name".equals(field.getLabel())).findFirst().orElseThrow();
        brief.setValue("something");

        for (PersonaWriter.Outcome result : List.of(new PersonaWriter.Outcome(null, false, new IllegalStateException("boom")),
                new PersonaWriter.Outcome(null, false, null), new PersonaWriter.Outcome(null, true, null))) {
            outcome.set(result);
            click(button(dialog, "Write with the model"));
        }

        assertEquals("Mine", name.getValue());
    }

    @Test
    void thereIsNoButtonWithoutAGenerator() {
        PersonaDialog dialog = new PersonaDialog("New", new Persona("", ""), name -> false, () -> {
        });

        assertTrue(all(dialog, Button.class).noneMatch(button -> "Write with the model".equals(button.getText())));
    }

    @Test
    void theBuiltInMediumsComeBackByIdEvenWhenRenamed() {
        List<Medium> source = new ArrayList<>(BetaReadersDefaults.builtInMediums());
        source.get(1).setName("Film");
        source.remove(2);
        AtomicReference<List<Medium>> applied = new AtomicReference<>();
        MediumLibraryDialog dialog = new MediumLibraryDialog(source, applied::set);

        click(button(dialog, "Restore the built-in mediums"));
        click(button(dialog, "Save"));

        List<Medium> result = applied.get();
        assertEquals(7, result.size());
        assertEquals("Film", result.stream().filter(m -> m.getId().equals("movie")).findFirst().orElseThrow().getName());
        assertEquals("series", result.get(6).getId());
        assertEquals(6, source.size(), "the source is not changed");
    }

    @Test
    void aMediumNeedsAUniqueNameAndIsWrittenOnSave() {
        Medium medium = new Medium("x", "Old", "old prompt");
        AtomicReference<Boolean> saved = new AtomicReference<>(false);
        MediumDialog dialog = new MediumDialog("Edit", medium, name -> name.equals("Book"), () -> saved.set(true));
        TextField name = all(dialog, TextField.class).filter(field -> "Name".equals(field.getLabel())).findFirst().orElseThrow();
        TextArea prompt = all(dialog, TextArea.class).filter(area -> "Prompt".equals(area.getLabel())).findFirst().orElseThrow();

        name.setValue("Book");
        click(button(dialog, "Save"));
        assertTrue(name.isInvalid());
        assertEquals("Old", medium.getName());

        name.setValue("Radio play");
        prompt.setValue("React as listeners");
        click(button(dialog, "Save"));
        assertTrue(saved.get());
        assertEquals("Radio play", medium.getName());
        assertEquals("React as listeners", medium.getPrompt());
    }
}
