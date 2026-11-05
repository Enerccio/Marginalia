package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.LanguageRef;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization.*;

/**
 * Asks which language of a book an action is for, when the book has more than one: the menu of a part (<i>Translate the speech</i>) and the menu of a
 * selected sentence. A book with one language never shows it, the action just runs.
 */
public final class LanguageChoiceDialog {

    private LanguageChoiceDialog() {
    }

    /**
     * Opens the dialog, or chooses at once when there is nothing to choose.
     *
     * @param languages the languages of the book; with none the action is not called
     * @param withAll   whether <i>All languages</i> is offered (a translation of the whole part can do every language; a selected sentence is one language)
     * @param chosen    called with the id of the language, or null for all of them
     */
    public static void ask(List<LanguageRef> languages, boolean withAll, Consumer<String> chosen) {
        if (languages.isEmpty()) {
            return;
        }
        if (languages.size() == 1) {
            chosen.accept(withAll ? null : languages.getFirst().id());
            return;
        }
        List<String> ids = new ArrayList<>();
        if (withAll) {
            ids.add(null);
        }
        languages.forEach(language -> ids.add(language.id()));
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems(ids);
        group.setItemLabelGenerator(id -> id == null ? TC.localized(ALL_LANGUAGES) : languages.stream().filter(l -> l.id().equals(id)).findFirst()
                .map(LanguageRef::name).orElse(id));
        group.setValue(ids.getFirst());
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(TC.localized(WHICH_LANGUAGE));
        dialog.add(new VerticalLayout(group));
        Button go = new Button(TC.localized(TRANSLATE), event -> {
            dialog.close();
            chosen.accept(group.getValue());
        });
        go.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(new Button(TC.localized(CANCEL), event -> dialog.close()), go);
        dialog.open();
    }
}
