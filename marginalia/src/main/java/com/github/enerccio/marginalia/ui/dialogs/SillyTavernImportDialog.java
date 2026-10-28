package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.SillyTavernChats;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Asks for the chats of SillyTavern ({@code .jsonl}, any number of them), the name of the book and whether the messages of the
 * user and the assistant are merged into one part, and hands the tree made of the chats ({@link SillyTavernChats#merge}) to the
 * callback. A file that is not a chat is refused with a warning, the others stay.
 */
@Configurable
public class SillyTavernImportDialog extends Dialog {

    /**
     * What is done with the chats.
     */
    @FunctionalInterface
    public interface Handler {
        void accept(SillyTavernChats.Tree tree, String bookName) throws Exception;
    }

    @Autowired
    private Localization loc;

    private final Handler handler;
    private final UI ui = UI.getCurrent();
    private final List<SillyTavernChats.Chat> chats = new ArrayList<>();

    private TextField nameField;
    private Checkbox mergeTurns;
    private Span status;
    private Button importButton;

    private SillyTavernImportDialog(Handler handler) {
        this.handler = handler;
    }

    /**
     * Opens the dialog; the callback runs when the user chose to import and the dialog closes when it does not fail.
     */
    public static void show(Handler handler) {
        SillyTavernImportDialog dialog = new SillyTavernImportDialog(handler);
        dialog.create();
        dialog.open();
    }

    private void create() {
        setHeaderTitle(loc.getValue(L.LABEL_IMPORT_FROM_SILLYTAVERN));
        setWidth("500px");

        InMemoryUploadHandler uploadHandler = new InMemoryUploadHandler((metadata, data) -> ui.access(() -> {
            String text = new String(data, StandardCharsets.UTF_8);
            if (text.startsWith("﻿")) {
                text = text.substring(1);
            }
            try {
                chats.add(SillyTavernChats.parse(text, metadata.fileName()));
            } catch (IllegalArgumentException e) {
                Notification.warning(String.format(loc.getValue(L.MSG_INVALID_SILLYTAVERN_CHAT), metadata.fileName()));
                return;
            }
            if (StringUtils.isBlank(nameField.getValue())) {
                SillyTavernChats.Chat first = chats.getFirst();
                nameField.setValue(StringUtils.defaultIfBlank(first.characterName(), first.name()));
            }
            refreshStatus();
        }));
        Upload upload = new Upload(uploadHandler);
        upload.setAcceptedFileTypes(".jsonl", ".json", "application/json", "application/x-ndjson");

        status = new Span();
        nameField = new TextField(loc.getValue(L.LABEL_NAME));
        nameField.setWidthFull();
        mergeTurns = new Checkbox(loc.getValue(L.LABEL_ST_MERGE_TURNS));

        VerticalLayout layout = new VerticalLayout(new Span(loc.getValue(L.MSG_IMPORT_SILLYTAVERN)), upload, status, nameField,
                mergeTurns, new Span(loc.getValue(L.HELP_ST_MERGE_TURNS)));
        layout.setPadding(true);
        add(layout);

        importButton = new Button(loc.getValue(L.LABEL_IMPORT), event -> importChats());
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), event -> close()), importButton);
        refreshStatus();
    }

    private void refreshStatus() {
        status.setText(chats.isEmpty() ? "" : String.format(loc.getValue(L.MSG_ST_CHATS_READY), chats.size()));
        importButton.setEnabled(!chats.isEmpty());
    }

    private void importChats() {
        SillyTavernChats.Tree tree = SillyTavernChats.merge(chats, mergeTurns.getValue());
        if (tree.size() == 0) {
            Notification.warning(loc.getValue(L.MSG_ST_NO_MESSAGES));
            return;
        }
        try {
            handler.accept(tree, nameField.getValue().trim());
            close();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
