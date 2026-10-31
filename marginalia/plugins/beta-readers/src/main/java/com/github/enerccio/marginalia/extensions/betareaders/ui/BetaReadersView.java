package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.BookData;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersService.Model;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * The tab of the plugin in the story sidebar: a tab for every open chat of the book, a button that starts a chat and a menu (rename,
 * close, delete, the personas of the book, the chats that were closed).
 */
@Configurable(preConstruction = true)
public class BetaReadersView extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final Manuscript manuscript;
    private final BetaReadersService service;

    private final List<BetaChat> chats = new ArrayList<>();
    private final Map<Tab, BetaChat> tabChats = new HashMap<>();

    private final Tabs tabs = new Tabs();
    private final Button menuButton = new Button(Solid.ELLIPSIS_VERTICAL.create());
    private final ContextMenu menu = new ContextMenu(menuButton);
    private final Div content = new Div();
    private BetaChat active;
    private boolean refreshing;

    public BetaReadersView(Manuscript manuscript, BetaReadersService service) {
        this.manuscript = manuscript;
        this.service = service;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);

        tabs.setWidthFull();
        tabs.addSelectedChangeListener(event -> {
            if (!refreshing && event.getSelectedTab() != null) {
                show(tabChats.get(event.getSelectedTab()));
            }
        });
        Button add = new Button(Solid.PLUS.create(), _ -> newChat());
        add.addThemeName("tertiary");
        add.addThemeName("small");
        add.setTooltipText(T.localized("New chat"));
        add.setAriaLabel(T.localized("New chat"));
        menuButton.addThemeName("tertiary");
        menuButton.addThemeName("small");
        menuButton.setAriaLabel(T.localized("Chats"));
        menuButton.setTooltipText(T.localized("Chats"));
        menu.setOpenOnClick(true);

        HorizontalLayout bar = new HorizontalLayout(tabs, add, menuButton);
        bar.setWidthFull();
        bar.setAlignItems(Alignment.CENTER);
        bar.setSpacing(false);
        bar.setFlexGrow(1, tabs);
        bar.addClassName(BetaReadersStyles.BAR);

        content.setSizeFull();
        content.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);
        add(bar, content);
        setFlexGrow(1, content);

        try {
            chats.addAll(service.getChats(manuscript));
            BookData data = service.loadCurrentBook(manuscript);
            active = chats.stream().filter(chat -> chat.getUuid().equals(data.getActiveChat()) && !chat.isClosed()).findFirst()
                    .orElse(null);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refresh();
    }

    // ------------------------------------------------------------------------------------------------------------
    // Tabs
    // ------------------------------------------------------------------------------------------------------------

    private List<BetaChat> open() {
        return chats.stream().filter(chat -> !chat.isClosed()).toList();
    }

    private void refresh() {
        refreshing = true;
        try {
            tabs.removeAll();
            tabChats.clear();
            List<BetaChat> open = open();
            if (active == null || !open.contains(active)) {
                active = open.isEmpty() ? null : open.getFirst();
            }
            for (BetaChat chat : open) {
                Tab tab = new Tab(chat.getTitle());
                tabChats.put(tab, chat);
                tabs.add(tab);
                if (chat == active) {
                    tabs.setSelectedTab(tab);
                }
            }
        } finally {
            refreshing = false;
        }
        rebuildMenu();
        showPanel();
    }

    private void show(BetaChat chat) {
        if (chat == null || chat == active) {
            return;
        }
        active = chat;
        try {
            service.setActiveChat(manuscript, chat.getUuid());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        rebuildMenu();
        showPanel();
    }

    private void showPanel() {
        content.removeAll();
        if (active == null) {
            Div empty = new Div(T.localized("No chats yet. Start one with the + button."));
            empty.addClassName(BetaReadersStyles.HINT);
            content.add(empty);
            return;
        }
        try {
            content.add(new ChatPanel(manuscript, active, service));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------------------------------------------------------

    private void rebuildMenu() {
        menu.removeAll();
        ContextMenu sub = menu;
        sub.addItem(T.localized("New chat"), _ -> newChat());
        sub.addItem(T.localized("Rename chat"), _ -> rename()).setEnabled(active != null);
        sub.addItem(T.localized("Close chat"), _ -> close()).setEnabled(active != null);
        sub.addItem(T.localized("Delete chat"), _ -> delete()).setEnabled(active != null);
        sub.addSeparator();
        sub.addItem(T.localized("Personas of this book"), _ -> editBookPersonas());
        List<BetaChat> closed = chats.stream().filter(BetaChat::isClosed).toList();
        if (!closed.isEmpty()) {
            sub.addSeparator();
            SubMenu reopen = sub.addItem(T.localized("Closed chats")).getSubMenu();
            closed.forEach(chat -> reopen.addItem(chat.getTitle(), _ -> reopen(chat)));
        }
    }

    private void newChat() {
        try {
            var personas = service.available(manuscript);
            if (personas.isEmpty()) {
                Notification.warning(T.localized("There is no persona to talk to. Add some to the library in the settings, or to this book."));
                return;
            }
            Model model = service.resolveModel(manuscript, null);
            var medium = service.resolveMedium(manuscript, null);
            new NewChatDialog(personas, service.settingsOrDefaults().getMediums(), service.aiChoices(), service.protocolChoices(),
                    new NewChatDialog.Inherited(medium == null ? null : medium.getName(),
                            model.ai() == null ? null : model.ai().getName(),
                            model.protocol() == null ? null : model.protocol().getName()),
                    this::created).open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void created(BetaChat chat) {
        try {
            service.saveChat(manuscript, chat);
            service.setActiveChat(manuscript, chat.getUuid());
            chats.add(chat);
            active = chat;
            refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void rename() {
        BetaChat chat = active;
        if (chat == null) {
            return;
        }
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized("Rename chat"));
        TextField name = new TextField(T.localized("Title"));
        name.setWidthFull();
        name.setValue(chat.getTitle());
        name.setMaxLength(120);
        DialogFocus.onOpen(dialog, name);
        Button save = new Button(T.localized("Save"), _ -> {
            String value = name.getValue() == null ? "" : name.getValue().strip();
            if (!value.isEmpty()) {
                chat.setTitle(value);
                store(chat);
                refresh();
            }
            dialog.close();
        });
        save.addThemeName("primary");
        dialog.add(new VerticalLayout(name));
        dialog.getFooter().add(new Button(T.localized("Cancel"), _ -> dialog.close()), save);
        dialog.open();
    }

    private void close() {
        if (active == null) {
            return;
        }
        active.setClosed(true);
        store(active);
        refresh();
    }

    private void reopen(BetaChat chat) {
        chat.setClosed(false);
        store(chat);
        active = chat;
        try {
            service.setActiveChat(manuscript, chat.getUuid());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refresh();
    }

    private void delete() {
        BetaChat chat = active;
        if (chat == null) {
            return;
        }
        ConfirmDialog.show(T.localized("Delete \"%s\"?").formatted(chat.getTitle()), () -> {
            try {
                service.deleteChat(manuscript, chat.getUuid());
                chats.remove(chat);
                refresh();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }

    private void editBookPersonas() {
        try {
            BookData data = service.loadCurrentBook(manuscript);
            new PersonaLibraryDialog(T.localized("Personas of this book"), data.getPersonas(), personas -> {
                try {
                    BookData current = service.loadCurrentBook(manuscript);
                    current.setPersonas(personas);
                    service.saveBook(manuscript, current);
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            }).open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void store(BetaChat chat) {
        try {
            service.saveChat(manuscript, chat);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
