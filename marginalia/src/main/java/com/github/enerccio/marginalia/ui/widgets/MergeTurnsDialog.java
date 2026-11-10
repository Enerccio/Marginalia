package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.TurnDetailsMerge;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

/**
 * Makes one turn of two: the fields hold the turn details of both joined ({@link TurnDetailsMerge}: the texts under each
 * other, the present characters of both, the POV character of the first), the author changes what does not fit and merges.
 * Used by the Planner for two scenes and by plugins that keep turn details (the Converter); what is done with the result is
 * up to the caller.
 */
public class MergeTurnsDialog extends Dialog {

    /**
     * The turn details of a turn.
     *
     * @param title the title of a scene of the planner, null for turn details that have none (the dialog then has no field for it)
     */
    public record Turn(String title, String sceneSetting, String povCharacter, String presentCharacters, String instructions) {
    }

    /**
     * @param header         the title of the dialog
     * @param first          the turn that comes first; the merged turn takes its place
     * @param second         the turn that follows it
     * @param maxTitleLength the longest title, used when {@code first} has a title
     * @param onMerge        gets the turn with the values of the fields; the dialog closes when it returns true
     */
    public MergeTurnsDialog(Localization loc, String header, Turn first, Turn second, int maxTitleLength, Predicate<Turn> onMerge) {
        setHeaderTitle(header);
        setWidth("640px");
        setMaxWidth("95vw");

        Span hint = new Span(loc.getValue(L.MSG_MERGE_TURNS_HINT));
        hint.addClassName(CssStyles.HINT_TEXT);

        TextField title = null;
        if (first.title() != null) {
            title = new TextField(loc.getValue(L.LABEL_TITLE));
            title.setWidthFull();
            title.setMaxLength(maxTitleLength);
            title.setValue(TurnDetailsMerge.title(first.title(), second.title(), maxTitleLength));
        }
        TextArea setting = area(loc, L.LABEL_SCENE_SETTING, TurnDetailsMerge.text(first.sceneSetting(), second.sceneSetting()));
        TextField pov = new TextField(loc.getValue(L.LABEL_POV_CHARACTER));
        pov.setWidthFull();
        pov.setValue(TurnDetailsMerge.one(first.povCharacter(), second.povCharacter()));
        String otherPov = StringUtils.trimToEmpty(second.povCharacter());
        if (!otherPov.isEmpty() && !otherPov.equalsIgnoreCase(pov.getValue())) {
            // a turn has one POV character: the author sees that the second turn had another one
            pov.setHelperText(loc.getValue(L.MSG_MERGE_OTHER_POV).formatted(otherPov));
        }
        TextField present = new TextField(loc.getValue(L.LABEL_PRESENT_CHARACTERS));
        present.setWidthFull();
        present.setValue(TurnDetailsMerge.names(first.presentCharacters(), second.presentCharacters()));
        TextArea instructions = area(loc, L.LABEL_INSTRUCTIONS, TurnDetailsMerge.text(first.instructions(), second.instructions()));

        VerticalLayout layout = new VerticalLayout(hint);
        if (title != null) {
            layout.add(title);
        }
        layout.add(setting, pov, present, instructions);
        layout.setPadding(false);
        layout.getThemeList().add("spacing-xs");
        add(layout);
        DialogFocus.onOpen(this, title != null ? title : instructions);

        TextField titleField = title;
        Button merge = new Button(loc.getValue(L.LABEL_MERGE), _ -> {
            if (titleField != null && StringUtils.isBlank(titleField.getValue())) {
                titleField.setInvalid(true);
                titleField.setErrorMessage(loc.getValue(L.MSG_PLANNER_TITLE_REQUIRED));
                return;
            }
            Turn merged = new Turn(titleField != null ? titleField.getValue() : null, setting.getValue(), pov.getValue(),
                    present.getValue(), instructions.getValue());
            if (onMerge.test(merged)) {
                close();
            }
        });
        merge.setThemeName("primary");
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), _ -> close()), merge);
        DialogShortcuts.submitWith(merge, title, setting, pov, present, instructions);
    }

    private static TextArea area(Localization loc, L label, String value) {
        TextArea area = new TextArea(loc.getValue(label));
        area.setWidthFull();
        area.setValue(value);
        ResizableTextArea.install(loc, area, "130px");
        return area;
    }
}
