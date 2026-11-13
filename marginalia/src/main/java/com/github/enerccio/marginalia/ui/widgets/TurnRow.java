package com.github.enerccio.marginalia.ui.widgets;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.Consumer;

/**
 * One row of a list of turn details: a box that says the turn is written, the content (a click on it puts the turn into the
 * new turn form) and the buttons in two lines of three: move up, move down, insert below / merge with the next, edit, delete.
 * Used by the Planner for its scenes and by plugins that keep turn details (the Converter), so that both lists look and work
 * the same. A button whose action is not given is left out.
 * <p>
 * Build it with {@link #builder(Component)}.
 */
public class TurnRow extends HorizontalLayout {

    /** What a button of the row does, and whether it can be used now. */
    public record Action(String tooltip, boolean enabled, Runnable run) {
    }

    private TurnRow() {
    }

    public static Builder builder(Component content) {
        return new Builder(content);
    }

    public static class Builder {

        private final Component content;
        private boolean done;
        private String doneTooltip;
        private Consumer<Boolean> onDone;
        private String contentTooltip;
        private Runnable onClick;
        private boolean current;
        private Action moveUp;
        private Action moveDown;
        private Action insert;
        private Action merge;
        private Action edit;
        private Action delete;

        private Builder(Component content) {
            this.content = content;
        }

        /**
         * The box in front of the row.
         *
         * @param tooltip also the label of the box for a screen reader
         * @param onDone  gets the new value when the author changes the box
         */
        public Builder done(boolean done, String tooltip, Consumer<Boolean> onDone) {
            this.done = done;
            this.doneTooltip = tooltip;
            this.onDone = onDone;
            return this;
        }

        public Builder onClick(String tooltip, Runnable onClick) {
            this.contentTooltip = tooltip;
            this.onClick = onClick;
            return this;
        }

        /** Marks the row the author went to last (the "next unchecked" button). */
        public Builder current(boolean current) {
            this.current = current;
            return this;
        }

        public Builder moveUp(Action action) {
            this.moveUp = action;
            return this;
        }

        public Builder moveDown(Action action) {
            this.moveDown = action;
            return this;
        }

        public Builder insert(Action action) {
            this.insert = action;
            return this;
        }

        public Builder merge(Action action) {
            this.merge = action;
            return this;
        }

        public Builder edit(Action action) {
            this.edit = action;
            return this;
        }

        public Builder delete(Action action) {
            this.delete = action;
            return this;
        }

        public TurnRow build() {
            TurnRow row = new TurnRow();
            Checkbox box = new Checkbox(done);
            box.setTooltipText(doneTooltip);
            box.setAriaLabel(doneTooltip);
            box.addValueChangeListener(e -> {
                if (e.isFromClient() && onDone != null) {
                    onDone.accept(e.getValue());
                }
            });

            Div main = new Div(content);
            main.addClassName(CssStyles.PLANNER_SCENE_MAIN);
            main.setTitle(contentTooltip);
            if (onClick != null) {
                main.addClickListener(_ -> onClick.run());
            }

            // two lines of three, so that the text of the turn keeps its room in a narrow tab
            HorizontalLayout moving = line(button(Solid.ARROW_UP, moveUp), button(Solid.ARROW_DOWN, moveDown),
                    button(Solid.PLUS, insert));
            HorizontalLayout changing = line(button(Solid.CODE_MERGE, merge), button(Solid.PEN, edit),
                    button(Solid.TRASH, delete));
            VerticalLayout buttons = new VerticalLayout();
            buttons.setSpacing(false);
            buttons.setPadding(false);
            buttons.setWidth(null);
            for (HorizontalLayout line : List.of(moving, changing)) {
                if (line.getComponentCount() > 0) {
                    buttons.add(line);
                }
            }

            row.add(box, main, buttons);
            row.setWidthFull();
            row.setAlignItems(FlexComponent.Alignment.START);
            row.setSpacing(false);
            row.expand(main);
            row.addClassName(CssStyles.PLANNER_SCENE);
            if (done) {
                row.addClassName(CssStyles.PLANNER_SCENE_DONE);
            }
            if (current) {
                row.addClassName(CssStyles.PLANNER_SCENE_CURRENT);
            }
            return row;
        }

        private static HorizontalLayout line(Button... buttons) {
            HorizontalLayout line = new HorizontalLayout();
            line.setSpacing(false);
            line.setPadding(false);
            for (Button button : buttons) {
                if (button != null) {
                    line.add(button);
                }
            }
            return line;
        }

        private static Button button(Solid icon, Action action) {
            if (action == null) {
                return null;
            }
            ComponentEventListener<ClickEvent<Button>> listener = _ -> action.run().run();
            Button button = new Button(icon.create(), listener);
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            button.setTooltipText(action.tooltip());
            button.setAriaLabel(action.tooltip());
            button.setEnabled(action.enabled());
            return button;
        }
    }
}
