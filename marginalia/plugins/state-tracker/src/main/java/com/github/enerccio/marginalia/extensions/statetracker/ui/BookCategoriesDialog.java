package com.github.enerccio.marginalia.extensions.statetracker.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.statetracker.model.Category;
import com.github.enerccio.marginalia.extensions.statetracker.model.CategoryCatalog;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.Predicate;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/**
 * The categories of a book: the copies of the user's categories it uses and its own. A copy that differs from the user's category
 * can be updated from it; a category of this book only can be saved to the user's categories. Every change is stored at once.
 */
public class BookCategoriesDialog extends Dialog {

    /** What the dialog needs from the view. */
    public interface Host {
        /** The state of the book, changed in place by the dialog. */
        StateBook state();

        /** The user's categories as stored now. */
        List<Category> userCategories() throws Exception;

        /** Stores the state and shows it. */
        void persist();

        /** Adds a category to the user's catalog and stores it. */
        void saveToMyCategories(Category category) throws Exception;

        /** Shows an unexpected error. */
        void failed(Exception e);
    }

    private final Host host;
    private final VerticalLayout list = new VerticalLayout();
    /** Add from my categories: the user's categories the book has no copy of. */
    private final MenuItem mine;

    public BookCategoriesDialog(Host host) {
        this.host = host;
        setHeaderTitle(T.localized(CATEGORIES_OF_THIS_BOOK));
        setWidth("640px");
        setMaxWidth("96vw");
        setMaxHeight("90vh");

        list.setPadding(false);
        list.setSpacing(false);
        list.addClassName(StateTrackerStyles.CATEGORIES);

        Button add = new Button(T.localized(NEW_CATEGORY_FOR_THIS_BOOK), Solid.PLUS.create(), _ -> newCategory());
        add.addThemeVariants(ButtonVariant.LUMO_SMALL);
        MenuBar fromMine = new MenuBar();
        fromMine.addThemeVariants(MenuBarVariant.LUMO_SMALL);
        mine = fromMine.addItem(T.localized(ADD_FROM_MY_CATEGORIES));
        Div actions = new Div(add, fromMine);
        actions.addClassName(StateTrackerStyles.DIALOG_ACTIONS);
        add(actions, list);

        getFooter().add(new Button(T.localized(CLOSE), _ -> close()));
        refresh();
    }

    private void fillMine() {
        mine.getSubMenu().removeAll();
        try {
            for (Category category : host.userCategories()) {
                if (host.state().copyOf(category.getId()) == null) {
                    mine.getSubMenu().addItem(category.getName(), _ -> {
                        host.state().adopt(category);
                        changed();
                    });
                }
            }
        } catch (Exception e) {
            host.failed(e);
        }
        mine.setEnabled(!mine.getSubMenu().getItems().isEmpty());
    }

    private void refresh() {
        fillMine();
        list.removeAll();
        StateBook state = host.state();
        if (state.getCategories().isEmpty()) {
            Div empty = new Div(new Span(T.localized(THE_BOOK_USES_NO_CATEGORY_YET)));
            empty.addClassName(StateTrackerStyles.MUTED);
            list.add(empty);
            return;
        }
        List<Category> user;
        try {
            user = host.userCategories();
        } catch (Exception e) {
            host.failed(e);
            user = List.of();
        }
        for (Category category : state.getCategories()) {
            list.add(card(category, category.getSourceId() == null ? null : CategoryCatalog.find(user, category.getSourceId())));
        }
    }

    /**
     * @param source the user's category the book's copy came from, {@code null} for a category of this book only or when the user
     *               deleted theirs
     */
    private Component card(Category category, Category source) {
        Div title = new Div(new Span(category.getName()));
        title.addClassNames(StateTrackerStyles.NAME, StateTrackerStyles.CARD_TITLE);
        Div header = new Div(title);
        header.addClassName(StateTrackerStyles.CARD_HEADER);

        if (category.getSourceId() == null) {
            Span badge = new Span(T.localized(ONLY_IN_THIS_BOOK));
            badge.addClassName(StateTrackerStyles.BADGE);
            header.add(badge, button(Solid.FLOPPY_DISK, T.localized(SAVE_TO_MY_CATEGORIES), () -> saveToMine(category)));
        } else if (source != null && !source.sameDefinition(category)) {
            Span badge = new Span(T.localized(DIFFERS_FROM_YOUR_CATEGORY));
            badge.addClassNames(StateTrackerStyles.BADGE, StateTrackerStyles.BADGE_WARNING);
            header.add(badge, button(Solid.ROTATE, T.localized(UPDATE_FROM_MY_CATEGORIES), () -> {
                category.updateFrom(source);
                changed();
            }));
        }
        header.add(button(Solid.PEN, T.localized(EDIT), () -> edit(category)),
                button(Solid.TRASH, T.localized(DELETE), () -> delete(category)));

        Div kind = new Div(new Span(CategoryDialog.kindLabel(category.getKind())
                + (category.getInstruction().isBlank() ? "" : " · " + category.getInstruction())));
        kind.addClassName(StateTrackerStyles.MUTED);

        Div card = new Div(header, kind);
        card.addClassName(StateTrackerStyles.CARD);
        return card;
    }

    private void newCategory() {
        new CategoryDialog(null, false, taken(null), created -> {
            created.setSourceId(null);
            host.state().getCategories().add(created);
            changed();
        }).open();
    }

    private void edit(Category category) {
        new CategoryDialog(category, host.state().isUsed(category.getId()), taken(category), edited -> {
            category.updateFrom(edited);
            changed();
        }).open();
    }

    private void delete(Category category) {
        ConfirmDialog.show(T.localized(DELETE_THE_CATEGORY_FROM_THIS_BOOK).formatted(category.getName(), host.state().trackedBy(category.getId())), () -> {
            host.state().deleteCategory(category.getId());
            changed();
        });
    }

    /** A category of this book only goes to the user's catalog; the book's copy is linked to it from then on. */
    private void saveToMine(Category category) {
        try {
            Category copy = category.copyForCatalog();
            host.saveToMyCategories(copy);
            category.setSourceId(copy.getId());
            changed();
            Notification.success(T.localized(SAVED_TO_YOUR_CATEGORIES));
        } catch (Exception e) {
            host.failed(e);
        }
    }

    private Predicate<String> taken(Category self) {
        return name -> {
            Category other = host.state().findCategoryByName(name);
            return other != null && other != self;
        };
    }

    private void changed() {
        host.persist();
        refresh();
    }

    private static Button button(Solid icon, String label, Runnable action) {
        Button button = new Button(icon.create(), _ -> action.run());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        button.setAriaLabel(label);
        button.setTooltipText(label);
        return button;
    }
}
