package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.PlannedScene;
import com.github.enerccio.marginalia.domain.service.PlannerService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

/**
 * Creates or edits a scene of the planner: the title and the turn details (scene setting, POV character, present characters and
 * instructions) the scene puts into the new turn form.
 */
class PlannedSceneDialog extends Dialog {

    /**
     * @param header the title of the dialog
     * @param scene  the scene to edit, a scene without a title for a new one (it is not changed: {@code onSave} gets a changed copy)
     * @param onSave gets the scene with the values of the fields
     */
    PlannedSceneDialog(Localization loc, String header, PlannedScene scene, Consumer<PlannedScene> onSave) {
        setHeaderTitle(header);
        setWidth("560px");
        setMaxWidth("95vw");

        TextField title = new TextField(loc.getValue(L.LABEL_TITLE));
        title.setWidthFull();
        title.setMaxLength(PlannerService.MAX_TITLE_LENGTH);
        title.setValue(StringUtils.defaultString(scene.getTitle()));

        TextArea setting = area(loc, L.LABEL_SCENE_SETTING, scene.getSceneSetting());
        TextArea pov = smallArea(loc, L.LABEL_POV_CHARACTER, scene.getPovCharacter());
        TextArea present = smallArea(loc, L.LABEL_PRESENT_CHARACTERS, scene.getPresentCharacters());
        TextArea instructions = area(loc, L.LABEL_INSTRUCTIONS, scene.getInstructions());

        VerticalLayout layout = new VerticalLayout(title, setting, pov, present, instructions);
        layout.setPadding(false);
        layout.getThemeList().add("spacing-xs");
        add(layout);
        DialogFocus.onOpen(this, title);

        Button save = new Button(loc.getValue(L.LABEL_SAVE), _ -> {
            if (StringUtils.isBlank(title.getValue())) {
                title.setInvalid(true);
                title.setErrorMessage(loc.getValue(L.MSG_PLANNER_TITLE_REQUIRED));
                return;
            }
            PlannedScene changed = scene.copy();
            changed.setTitle(title.getValue());
            changed.setSceneSetting(setting.getValue());
            changed.setPovCharacter(pov.getValue());
            changed.setPresentCharacters(present.getValue());
            changed.setInstructions(instructions.getValue());
            close();
            onSave.accept(changed);
        });
        save.setThemeName("primary");
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), _ -> close()), save);
        DialogShortcuts.submitWith(save, title, setting, pov, present, instructions);
    }

    /** POV and present characters can be several lines, but are short: a small text area. */
    private static TextArea smallArea(Localization loc, L label, String value) {
        TextArea area = new TextArea(loc.getValue(label));
        area.setWidthFull();
        area.setValue(StringUtils.defaultString(value));
        ResizableTextArea.install(loc, area, "80px");
        return area;
    }

    private static TextArea area(Localization loc, L label, String value) {
        TextArea area = new TextArea(loc.getValue(label));
        area.setWidthFull();
        area.setMaxLength(PlannerService.MAX_TEXT_LENGTH);
        area.setValue(StringUtils.defaultString(value));
        ResizableTextArea.install(loc, area, "110px");
        return area;
    }
}
