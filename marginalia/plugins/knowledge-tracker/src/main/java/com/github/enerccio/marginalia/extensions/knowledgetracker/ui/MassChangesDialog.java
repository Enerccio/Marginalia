package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.*;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * <b>Mass changes</b> of the Knowledge tab: every tracked character with all its items (old ones too) in a grid with check boxes, the
 * ticked items of all the grids are retracted, restored or deleted at once. The tracked characters are also exported to a JSON file
 * and imported from one ({@link KnowledgeTransfer}).
 * <p>
 * The dialog does not keep a state of its own: every action reads the state the tab holds now ({@code state}), changes it and has the
 * tab store it ({@code persist}); the tab calls {@link #reload} when the state was read again (a run, a command). The ticks are kept by
 * the ids of the items, so they survive a reload.
 */
public class MassChangesDialog extends Dialog {

    /** A grid with more items than this gets a fixed height and scrolls, a shorter one shows all its rows. */
    private static final int ROWS_SHOWN = 12;

    private final Supplier<KnowledgeState> state;
    private final Runnable persist;
    private final String bookName;
    private final UI ui = UI.getCurrent();

    /** The ids of the ticked items, by the id of their character. */
    private final Map<String, Set<String>> selected = new HashMap<>();

    private final Div content = new Div();
    private final Span status = new Span();
    private final Button retract = new Button(Solid.BAN.create());
    private final Button restore = new Button(Solid.ROTATE_LEFT.create());
    private final Button delete = new Button(Solid.TRASH.create());
    /** The part numbers of the active branch, by the uuid of the part, as the tab read them. */
    private final Function<String, Integer> positions;
    private boolean updatingSelection;

    /**
     * @param bookName  the name of the book, for the name of the export file
     * @param state     the state the tab holds now
     * @param persist   stores the state and shows it in the tab
     * @param positions the number of a part of the active branch by its uuid, {@code null} for a part that is not on it
     */
    public MassChangesDialog(String bookName, Supplier<KnowledgeState> state, Runnable persist, Function<String, Integer> positions) {
        this.bookName = bookName;
        this.state = state;
        this.persist = persist;
        this.positions = positions;

        setHeaderTitle(T.localized(MASS_CHANGES));
        setWidth("1100px");
        setMaxWidth("100vw");
        setHeight("85vh");
        setMaxHeight("100vh");
        setDraggable(true);
        setResizable(true);

        content.addClassName(KnowledgeTrackerStyles.REVIEW);
        Scroller scroller = new Scroller(content);
        scroller.setSizeFull();
        add(scroller);

        getHeader().add(createTransferBar());

        retract.addThemeVariants(ButtonVariant.LUMO_SMALL);
        retract.setTooltipText(T.localized(NOT_TRUE_ANY_MORE_KEEP_IT_AS_OLD));
        retract.addClickListener(_ -> retractSelected());
        restore.addThemeVariants(ButtonVariant.LUMO_SMALL);
        restore.setTooltipText(T.localized(MAKE_IT_ACTIVE_AGAIN));
        restore.addClickListener(_ -> restoreSelected());
        delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        delete.addClickListener(_ -> deleteSelected());
        Button selectAll = new Button(T.localized(SELECT_ALL), Solid.CHECK_DOUBLE.create(), _ -> selectAll(true));
        selectAll.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        Button deselectAll = new Button(T.localized(DESELECT_ALL), _ -> selectAll(false));
        deselectAll.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        Button close = new Button(T.localized(CLOSE), _ -> close());
        status.addClassName(KnowledgeTrackerStyles.COUNTS);
        Div actions = new Div(status, selectAll, deselectAll, retract, restore, delete, close);
        actions.addClassName(KnowledgeTrackerStyles.ITEM_BAR);
        getFooter().add(actions);

        reload();
    }

    /** Export (a download of the file) and import (an upload) in the header of the dialog. */
    private Div createTransferBar() {
        Anchor export = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) _ -> {
            byte[] data = KnowledgeTransfer.export(state.get()).getBytes(StandardCharsets.UTF_8);
            String name = StringUtils.defaultIfBlank(bookName, "book").replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
            return new DownloadResponse(new ByteArrayInputStream(data), name + "-knowledge.json", "application/json", data.length);
        }), "");
        export.getElement().setAttribute("download", true);
        Button exportButton = new Button(T.localized(EXPORT_TO_JSON), Solid.FILE_EXPORT.create());
        exportButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        export.add(exportButton);

        Upload upload = new Upload(new InMemoryUploadHandler((_, data) -> ui.access(() -> importFile(data))));
        upload.setAcceptedFileTypes(".json", "application/json");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(10 * 1024 * 1024);
        upload.setDropAllowed(false);
        Button importButton = new Button(T.localized(IMPORT_FROM_JSON), Solid.FILE_IMPORT.create());
        importButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        upload.setUploadButton(importButton);
        // the uploaded file is read at once: the list of files would only stand in the way of the next import
        upload.addAllFinishedListener(_ -> upload.clearFileList());

        Div bar = new Div(export, upload);
        bar.addClassName(KnowledgeTrackerStyles.ITEM_BAR);
        return bar;
    }

    private void importFile(byte[] data) {
        String text = new String(data, StandardCharsets.UTF_8);
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        KnowledgeTransfer.ImportResult result;
        try {
            result = KnowledgeTransfer.importInto(state.get(), text);
        } catch (IllegalArgumentException e) {
            Notification.error(T.localized(THE_FILE_IS_NOT_A_KNOWLEDGE_EXPORT).formatted(e.getMessage()));
            return;
        }
        persist.run();
        reload();
        Notification.success(T.localized(IMPORTED_SUMMARY).formatted(result.newCharacters(), result.updatedCharacters(),
                result.itemsAdded(), result.itemsSkipped()));
    }

    /**
     * Shows the characters and items of the state as it is now; ticks of items that are gone are dropped.
     */
    public void reload() {
        KnowledgeState current = state.get();
        content.removeAll();
        Map<String, Set<String>> kept = new HashMap<>();
        if (current.getCharacters().isEmpty()) {
            Div empty = new Div(new Span(T.localized(NO_CHARACTER_IS_TRACKED_YET)));
            empty.addClassName(KnowledgeTrackerStyles.EMPTY);
            content.add(empty);
        }
        for (TrackedCharacter character : current.getCharacters()) {
            Set<String> ticks = new HashSet<>(selected.getOrDefault(character.getId(), Set.of()));
            ticks.retainAll(character.getItems().stream().map(KnowledgeItem::getId).toList());
            kept.put(character.getId(), ticks);
            content.add(createGroup(character, ticks));
        }
        selected.clear();
        selected.putAll(kept);
        updateButtons();
    }

    private Div createGroup(TrackedCharacter character, Set<String> ticks) {
        Div name = new Div(new Span(character.getName()));
        name.addClassName(KnowledgeTrackerStyles.NAME);
        Div counts = new Div(new Span(T.localized(ITEMS_ACTIVE_OLD).formatted(character.activeItems().size(), character.oldItems().size())));
        counts.addClassName(KnowledgeTrackerStyles.COUNTS);
        Div title = new Div(name, counts);
        title.addClassName(KnowledgeTrackerStyles.CARD_TITLE);
        Div header = new Div(title);
        header.addClassName(KnowledgeTrackerStyles.CARD_HEADER);
        Div group = new Div(header);
        group.addClassName(KnowledgeTrackerStyles.REVIEW_GROUP);

        // the newest on top, as in the tab
        List<KnowledgeItem> items = new ArrayList<>(character.getItems());
        Collections.reverse(items);
        if (items.isEmpty()) {
            Div none = new Div(new Span(T.localized(NO_ITEMS)));
            none.addClassName(KnowledgeTrackerStyles.EMPTY);
            group.add(none);
            return group;
        }

        Grid<KnowledgeItem> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_ROW_STRIPES);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn(item -> T.localized(item.getKind() == ItemKind.KNOWS ? KNOWS : DOES_NOT_KNOW))
                .setHeader(T.localized(KIND)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(KnowledgeItem::getText).setHeader(T.localized(TEXT)).setFlexGrow(1);
        grid.addColumn(MassChangesDialog::stateLabel).setHeader(T.localized(STATE)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(MassChangesDialog::originLabel).setHeader(T.localized(ORIGIN)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(item -> {
            Integer position = item.getSourceMessage() == null ? null : positions.apply(item.getSourceMessage());
            return position == null ? "" : "#" + position;
        }).setHeader(T.localized(PART)).setAutoWidth(true).setFlexGrow(0);
        grid.setItems(items);
        if (items.size() <= ROWS_SHOWN) {
            grid.setAllRowsVisible(true);
        } else {
            grid.setHeight("420px");
        }
        updatingSelection = true;
        items.stream().filter(item -> ticks.contains(item.getId())).forEach(grid::select);
        updatingSelection = false;
        grid.addSelectionListener(e -> {
            if (updatingSelection) {
                return;
            }
            Set<String> ids = selected.computeIfAbsent(character.getId(), _ -> new HashSet<>());
            ids.clear();
            e.getAllSelectedItems().forEach(item -> ids.add(item.getId()));
            updateButtons();
        });
        group.add(grid);
        return group;
    }

    private void selectAll(boolean all) {
        selected.clear();
        for (TrackedCharacter character : state.get().getCharacters()) {
            if (all) {
                Set<String> ids = new HashSet<>();
                character.getItems().forEach(item -> ids.add(item.getId()));
                selected.put(character.getId(), ids);
            }
        }
        reload();
    }

    /** The counts of the buttons: how many ticked items each one would change. */
    private void updateButtons() {
        int ticked = 0;
        int active = 0;
        int old = 0;
        int total = 0;
        for (TrackedCharacter character : state.get().getCharacters()) {
            Set<String> ids = selected.getOrDefault(character.getId(), Set.of());
            total += character.getItems().size();
            for (KnowledgeItem item : character.getItems()) {
                if (ids.contains(item.getId())) {
                    ticked++;
                    if (item.isActive()) {
                        active++;
                    } else {
                        old++;
                    }
                }
            }
        }
        status.setText(T.localized(N_OF_N_SELECTED).formatted(ticked, total));
        retract.setText(T.localized(RETRACT_SELECTED).formatted(active));
        retract.setEnabled(active > 0);
        restore.setText(T.localized(RESTORE_SELECTED).formatted(old));
        restore.setEnabled(old > 0);
        delete.setText(T.localized(DELETE_SELECTED).formatted(ticked));
        delete.setEnabled(ticked > 0);
    }

    private void retractSelected() {
        int count = forSelected(TrackedCharacter::retractItems);
        // retracted items stay ticked: the next step (restore, delete) can take them as they are
        persist.run();
        reload();
        Notification.success(T.localized(N_ITEMS_RETRACTED).formatted(count));
    }

    private void restoreSelected() {
        int count = forSelected(TrackedCharacter::restoreItems);
        persist.run();
        reload();
        Notification.success(T.localized(N_ITEMS_RESTORED).formatted(count));
    }

    private void deleteSelected() {
        int ticked = selected.values().stream().mapToInt(Set::size).sum();
        ConfirmDialog.show(T.localized(DELETE_N_ITEMS_FOR_GOOD).formatted(ticked), () -> {
            int count = forSelected(TrackedCharacter::deleteItems);
            selected.clear();
            persist.run();
            reload();
            Notification.success(T.localized(N_ITEMS_DELETED).formatted(count));
        });
    }

    @FunctionalInterface
    private interface ItemsAction {
        int apply(TrackedCharacter character, Collection<String> itemIds);
    }

    /** Applies the action to the ticked items of every character of the state as it is now. */
    private int forSelected(ItemsAction action) {
        int count = 0;
        for (TrackedCharacter character : state.get().getCharacters()) {
            Set<String> ids = selected.get(character.getId());
            if (ids != null && !ids.isEmpty()) {
                count += action.apply(character, ids);
            }
        }
        return count;
    }

    private static String stateLabel(KnowledgeItem item) {
        if (item.isActive()) {
            return T.localized(ACTIVE);
        }
        return T.localized(switch (item.getOldReason()) {
            case REJECTED -> OLD_DECLINED;
            case TIDIED -> OLD_TIDIED;
            case RETRACTED -> OLD_RETRACTED;
        });
    }

    private static String originLabel(KnowledgeItem item) {
        return T.localized(switch (item.getOrigin()) {
            case INITIAL -> INITIAL_STATE;
            case MANUAL -> WRITTEN_BY_HAND;
            case LEARNED -> LEARNED_FROM_THE_STORY;
        });
    }
}
