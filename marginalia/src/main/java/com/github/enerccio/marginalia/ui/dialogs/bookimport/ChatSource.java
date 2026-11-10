package com.github.enerccio.marginalia.ui.dialogs.bookimport;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ChatImportFormat;
import com.github.enerccio.marginalia.ui.dialogs.SillyTavernImportRunner;
import com.github.enerccio.marginalia.utils.SillyTavernChats;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The saved chats of a program ({@link ChatImportFormat}: SillyTavern, LM Studio), any number of files, joined into a tree of
 * parts. The option is whether the messages of the user and the assistant are merged into one part.
 */
final class ChatSource extends BookImportSource {

    private final ChatImportFormat format;
    private final List<SillyTavernChats.Chat> chats = new ArrayList<>();
    private final VerticalLayout options;
    private final Checkbox mergeTurns;

    ChatSource(Localization loc, ChatImportFormat format) {
        super(loc);
        this.format = format;
        mergeTurns = new Checkbox(loc.getValue(L.LABEL_ST_MERGE_TURNS));
        options = new VerticalLayout(mergeTurns, new Span(loc.getValue(L.HELP_ST_MERGE_TURNS)));
        options.setPadding(false);
    }

    @Override
    L label() {
        return format.title();
    }

    @Override
    L description() {
        return format.description();
    }

    @Override
    String[] fileTypes() {
        return format.fileTypes();
    }

    @Override
    boolean multiple() {
        return true;
    }

    @Override
    boolean insertable() {
        return false;
    }

    @Override
    Component options() {
        return options;
    }

    @Override
    void read(byte[] data, String fileName) {
        String text = new String(data, StandardCharsets.UTF_8);
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        chats.add(format.parse(text, fileName));
    }

    @Override
    String invalid(String fileName) {
        return String.format(loc.getValue(format.invalid()), fileName);
    }

    @Override
    void reset() {
        chats.clear();
    }

    @Override
    String status() {
        return chats.isEmpty() ? "" : String.format(loc.getValue(L.MSG_ST_CHATS_READY), chats.size());
    }

    @Override
    boolean ready() {
        return !chats.isEmpty();
    }

    @Override
    String suggestedName() {
        if (chats.isEmpty()) {
            return "";
        }
        SillyTavernChats.Chat first = chats.getFirst();
        return StringUtils.defaultIfBlank(first.characterName(), first.name());
    }

    @Override
    BookImportDialog.Run prepare() {
        SillyTavernChats.Tree tree = SillyTavernChats.merge(chats, mergeTurns.getValue());
        if (tree.size() == 0) {
            throw new IllegalArgumentException(loc.getValue(L.MSG_ST_NO_MESSAGES));
        }
        return (manuscript, _, afterwards) -> new SillyTavernImportRunner().run(format, manuscript, tree, afterwards::accept);
    }
}
