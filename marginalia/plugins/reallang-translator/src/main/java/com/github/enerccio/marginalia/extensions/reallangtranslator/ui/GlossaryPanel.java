package com.github.enerccio.marginalia.extensions.reallangtranslator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary.Effective;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary.Level;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.GlossaryEntry;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.GlossaryKind;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.BookReal;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.GlossaryService.Target;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.CommonStyles;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangTranslatorLocalization.*;

/**
 * The glossary of a book in one of its languages: the entries of the language, of the folders above the book and of the book, merged as the model gets
 * them, with the level each comes from. An entry is changed where it lives; a word of a higher level can be switched off for the book, and an entry of the
 * book can be moved up to a folder or to the language. The names the model proposed are marked and accepted here.
 */
public class GlossaryPanel extends VerticalLayout {

    private final Manuscript book;
    private final BookReal service;
    private final String languageId;
    private final Grid<Effective> grid = new Grid<>();
    private final Button edit = new Button(T.localized(EDIT), Solid.PEN.create(), _ -> selected(this::edit));
    private final Button delete = new Button(T.localized(DELETE), Solid.TRASH.create(), _ -> selected(this::delete));
    private final Button accept = new Button(T.localized(ACCEPT), Solid.CHECK.create(), _ -> selected(this::accept));
    private final Button toggle = new Button(T.localized(SWITCH_OFF_FOR_THIS_BOOK), Solid.TOGGLE_OFF.create(), _ -> selected(this::toggle));
    private final MenuBar move = new MenuBar();

    public GlossaryPanel(Manuscript book, BookReal service, String languageId) {
        this.book = book;
        this.service = service;
        this.languageId = languageId;
        setPadding(false);
        setSpacing(false);
        setSizeFull();
        setMinWidth("0");

        Span hint = new Span(T.localized(GLOSSARY_HINT));
        hint.addClassName(CommonStyles.HINT);
        Button add = new Button(T.localized(ADD), Solid.PLUS.create(), _ -> add());
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        move.addThemeVariants(com.vaadin.flow.component.menubar.MenuBarVariant.LUMO_TERTIARY);
        HorizontalLayout toolbar = new HorizontalLayout(add, edit, delete, accept, toggle, move);
        toolbar.setWidthFull();
        toolbar.setWrap(true);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();
        grid.setHeight("100%");
        grid.addColumn(item -> item.entry().getSource()).setHeader(T.localized(WORD)).setFlexGrow(1).setResizable(true);
        grid.addColumn(GlossaryPanel::writtenAs).setHeader(T.localized(WRITTEN_AS)).setFlexGrow(1).setResizable(true);
        grid.addColumn(item -> item.entry().getReading()).setHeader(T.localized(READ_AS)).setFlexGrow(1).setResizable(true);
        grid.addColumn(item -> item.entry().getNote()).setHeader(T.localized(NOTE)).setFlexGrow(2).setResizable(true);
        grid.addColumn(GlossaryPanel::levelLabel).setHeader(T.localized(LEVEL)).setFlexGrow(1).setResizable(true);
        grid.addColumn(GlossaryPanel::status).setHeader(T.localized(STATUS)).setFlexGrow(0).setAutoWidth(true);
        grid.addSelectionListener(_ -> updateButtons());
        grid.addItemDoubleClickListener(event -> edit(event.getItem()));

        add(hint, toolbar, grid);
        setFlexGrow(1, grid);
        refresh();
    }

    /** Reads the glossary again. */
    public final void refresh() {
        try {
            grid.setItems(service.glossary(book, languageId));
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_GLOSSARY_CAN_NOT_BE_CHANGED), e.getMessage()));
        }
        updateButtons();
    }

    private static String writtenAs(Effective item) {
        return item.entry().getKind() == GlossaryKind.KEEP ? T.localized(KEEP_IT_AS_IT_IS) : item.entry().getWritten();
    }

    private static String levelLabel(Effective item) {
        return switch (item.level()) {
            case LANGUAGE -> T.localized(LANGUAGE_S).formatted(item.label());
            case FOLDER -> T.localized(FOLDER_S).formatted(item.label());
            case BOOK -> T.localized(THIS_BOOK);
        };
    }

    private static String status(Effective item) {
        if (!item.entry().isEnabled()) {
            return T.localized(SWITCHED_OFF);
        }
        return item.entry().isProposed() ? T.localized(PROPOSED) : "";
    }

    private void selected(java.util.function.Consumer<Effective> action) {
        grid.getSelectedItems().stream().findFirst().ifPresent(action);
    }

    private void updateButtons() {
        Effective item = grid.getSelectedItems().stream().findFirst().orElse(null);
        edit.setEnabled(item != null);
        delete.setEnabled(item != null);
        accept.setVisible(item != null && item.level() == Level.BOOK && item.entry().isProposed());
        // a word of a higher level is switched off by an entry of the book; that entry is switched on again the same way (or deleted, which brings the
        // word of the level above back)
        toggle.setVisible(item != null);
        if (item != null) {
            toggle.setText(item.entry().isEnabled() ? T.localized(SWITCH_OFF_FOR_THIS_BOOK) : T.localized(SWITCH_ON_FOR_THIS_BOOK));
        }
        buildMoveMenu(item);
    }

    /** The menu of where an entry of the book can go: the language and the folders above the book. */
    private void buildMoveMenu(Effective item) {
        move.removeAll();
        move.setVisible(item != null && item.level() == Level.BOOK);
        if (item == null || item.level() != Level.BOOK) {
            return;
        }
        var root = move.addItem(T.localized(MOVE_TO));
        var sub = root.getSubMenu();
        sub.addItem(T.localized(MOVE_TO_THE_LANGUAGE), _ -> moveTo(item, Target.language()));
        try {
            List<Folder> folders = service.glossaryFolders(book);
            for (Folder folder : folders) {
                sub.addItem(T.localized(FOLDER_S).formatted(folder.getName()), _ -> moveTo(item, Target.folder(folder.getUuid())));
            }
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_GLOSSARY_CAN_NOT_BE_CHANGED), e.getMessage()));
        }
    }

    private void add() {
        GlossaryEntry entry = new GlossaryEntry();
        new GlossaryEntryDialog(entry, true, () -> store(Target.book(), entry)).open();
    }

    private void edit(Effective item) {
        GlossaryEntry copy = item.entry().copy();
        new GlossaryEntryDialog(copy, false, () -> store(Target.of(item), copy)).open();
    }

    private void store(Target target, GlossaryEntry entry) {
        try {
            service.putGlossaryEntry(book, languageId, target, entry);
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_GLOSSARY_CAN_NOT_BE_CHANGED), e.getMessage()));
        }
        refresh();
    }

    private void delete(Effective item) {
        ConfirmDialog.show(T.localized(DELETE_THE_ENTRY_S).formatted(item.entry().getSource()), () -> {
            try {
                service.removeGlossaryEntry(book, languageId, Target.of(item), item.entry().getId());
            } catch (Exception e) {
                Notification.error(String.format(T.localized(THE_GLOSSARY_CAN_NOT_BE_CHANGED), e.getMessage()));
            }
            refresh();
        });
    }

    private void accept(Effective item) {
        try {
            service.acceptGlossaryEntry(book, languageId, item.entry().getId());
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_GLOSSARY_CAN_NOT_BE_CHANGED), e.getMessage()));
        }
        refresh();
    }

    private void toggle(Effective item) {
        try {
            if (item.level() == Level.BOOK) {
                GlossaryEntry own = item.entry().copy();
                own.setEnabled(!own.isEnabled());
                service.putGlossaryEntry(book, languageId, Target.book(), own);
            } else {
                service.switchGlossaryEntry(book, languageId, item.entry(), !item.entry().isEnabled());
            }
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_GLOSSARY_CAN_NOT_BE_CHANGED), e.getMessage()));
        }
        refresh();
    }

    private void moveTo(Effective item, Target to) {
        try {
            service.moveGlossaryEntry(book, languageId, item.entry().getId(), to);
        } catch (Exception e) {
            Notification.error(String.format(T.localized(THE_GLOSSARY_CAN_NOT_BE_CHANGED), e.getMessage()));
        }
        refresh();
    }
}
