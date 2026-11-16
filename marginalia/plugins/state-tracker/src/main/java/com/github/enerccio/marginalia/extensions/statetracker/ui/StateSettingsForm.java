package com.github.enerccio.marginalia.extensions.statetracker.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.extensions.statetracker.model.Category;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/**
 * The State Tracker section of the user settings: the user's categories, the catalog books copy from. The form edits a working
 * copy; the Settings screen stores it with the rest ({@link #save}).
 */
@Configurable(preConstruction = true)
public class StateSettingsForm extends VerticalLayout {

    @Autowired
    private Localization loc;

    @Autowired
    private SettingService settingService;

    private final StateService service;
    private final VerticalLayout list = new VerticalLayout();
    private List<Category> categories = new ArrayList<>();
    /** The user pressed Reset: the stored catalog is removed on save, the built-in categories apply. */
    private boolean reset;

    public StateSettingsForm(StateService service) {
        this.service = service;
        setWidthFull();
        setPadding(true);
        setSpacing(true);

        Div help = new Div(new Span(T.localized(THE_CATEGORIES_YOUR_BOOKS_COPY_FROM)));
        help.addClassName(StateTrackerStyles.MUTED);

        Button add = new Button(T.localized(NEW_CATEGORY), Solid.PLUS.create(), _ -> newCategory());
        add.addThemeVariants(ButtonVariant.LUMO_SMALL);
        Button resetButton = new Button(T.localized(RESET_TO_THE_BUILT_IN_CATEGORIES), Solid.ROTATE_LEFT.create(), _ -> {
            categories = new ArrayList<>(StateService.builtInCategories());
            reset = true;
            show();
        });
        resetButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        list.setPadding(false);
        list.setSpacing(false);
        list.addClassName(StateTrackerStyles.CATEGORIES);
        add(help, new HorizontalLayout(add, resetButton), list);
        refresh();
    }

    /** Reads the categories as they are stored. */
    public void refresh() {
        try {
            categories = new ArrayList<>(service.readUserCategories(settingService.getOrCreate(UserSetting.class)));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            categories = new ArrayList<>();
        }
        reset = false;
        show();
    }

    /** Puts the categories into the user settings that the Settings screen stores. */
    public void save(UserSetting userSetting) {
        service.putUserCategories(userSetting, reset ? null : categories);
    }

    private void show() {
        list.removeAll();
        for (Category category : categories) {
            list.add(card(category));
        }
    }

    private Component card(Category category) {
        Div title = new Div(new Span(category.getName()));
        title.addClassNames(StateTrackerStyles.NAME, StateTrackerStyles.CARD_TITLE);
        Button edit = icon(Solid.PEN, T.localized(EDIT), () -> new CategoryDialog(category, false, taken(category), edited -> {
            category.updateFrom(edited);
            changed();
        }).open());
        Button delete = icon(Solid.TRASH, T.localized(DELETE), () -> ConfirmDialog.show(T.localized(DELETE_YOUR_CATEGORY).formatted(category.getName()), () -> {
            categories.remove(category);
            changed();
        }));
        Div header = new Div(title, edit, delete);
        header.addClassName(StateTrackerStyles.CARD_HEADER);
        Div kind = new Div(new Span(CategoryDialog.kindLabel(category.getKind())
                + (category.getInstruction().isBlank() ? "" : " · " + category.getInstruction())));
        kind.addClassName(StateTrackerStyles.MUTED);
        Div card = new Div(header, kind);
        card.addClassName(StateTrackerStyles.CARD);
        return card;
    }

    private void newCategory() {
        new CategoryDialog(null, false, taken(null), created -> {
            categories.add(created);
            changed();
        }).open();
    }

    /** A change after a reset is a catalog of its own: it is stored, not removed. */
    private void changed() {
        reset = false;
        show();
    }

    private Predicate<String> taken(Category self) {
        return name -> categories.stream().anyMatch(c -> c != self && c.getName().strip().equalsIgnoreCase(name.strip()));
    }

    private static Button icon(Solid icon, String label, Runnable action) {
        Button button = new Button(icon.create(), _ -> action.run());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        button.setAriaLabel(label);
        button.setTooltipText(label);
        return button;
    }
}
