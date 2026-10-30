package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.components.RevisionClearing;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.PartEdit;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.TextInputDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.WordDiffView;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.function.Consumer;

/**
 * The versions of the text of a part (the desktop and the phone story tab): who wrote each, when, how long it is, its label; the
 * text of the selected one, as the difference to the previous version or to the text the part has now; its reasoning when a model
 * wrote it. The author can put an earlier version back on top (a new version, nothing is rewritten) or go back to it (the newer
 * versions go to the trash), save the text as a labeled version, label a version, delete it to the trash and take it out again,
 * and clear the history. See {@link MessageRevisionService}.
 * <p>
 * On a phone the dialog fills the screen and the list is above the version instead of beside it.
 */
@Configurable(preConstruction = true)
public class PartHistoryDialog extends Dialog {

    enum Compare {
        PREVIOUS, CURRENT, NONE
    }

    @Autowired
    private Localization loc;

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Autowired
    private ChatMessageService chatMessageService;

    private final Manuscript manuscript;
    private final Consumer<ChatMessage> onChanged;
    private final boolean phone = UIUtils.isPhone();

    private ChatMessage part;

    final Grid<RevisionRow> grid = new Grid<>();
    final Checkbox showDeleted = new Checkbox();
    final Button saveVersion = new Button();
    final Button emptyTrash = new Button();
    final Button clearHistory = new Button();
    final Button restore = new Button();
    final Button rollBack = new Button();
    final Button label = new Button();
    final Button delete = new Button();
    final Button purge = new Button();
    final Button copy = new Button();
    final RadioButtonGroup<Compare> compare = new RadioButtonGroup<>();
    final Span meta = new Span();
    final Span origin = new Span();
    final Span empty = new Span();
    final Div plain = new Div();
    final Div reasoning = new Div();
    final Details reasoningDetails = new Details();
    private final WordDiffView diff;
    private final VerticalLayout detail = new VerticalLayout();

    // the version shown, as stored
    private MessageRevision selected;
    private List<RevisionRow> rows = List.of();
    // the id of the newest version that is not in the trash: the text of the part
    private Long currentId;

    /**
     * Opens the history of the part.
     *
     * @param onChanged gets the part after a version was put back or the part was rolled back to one
     */
    public static void open(Manuscript manuscript, ChatMessage part, Consumer<ChatMessage> onChanged) {
        new PartHistoryDialog(manuscript, part, onChanged).open();
    }

    /**
     * Opens the history of the part with the version selected, for the part found through it.
     *
     * @param versionUuid the uuid of the version; when the part has no such version the list is shown without a selection
     */
    public static void open(Manuscript manuscript, ChatMessage part, Consumer<ChatMessage> onChanged, String versionUuid) {
        create(manuscript, part, onChanged, versionUuid).open();
    }

    static PartHistoryDialog create(Manuscript manuscript, ChatMessage part, Consumer<ChatMessage> onChanged, String versionUuid) {
        PartHistoryDialog dialog = new PartHistoryDialog(manuscript, part, onChanged);
        dialog.rows.stream().filter(row -> row.uuid().equals(versionUuid)).findFirst().ifPresent(dialog.grid::select);
        return dialog;
    }

    PartHistoryDialog(Manuscript manuscript, ChatMessage part, Consumer<ChatMessage> onChanged) {
        this.manuscript = manuscript;
        this.part = part;
        this.onChanged = onChanged;
        diff = new WordDiffView(loc.getValue(L.MSG_MERGE_NO_DIFFERENCE));
        build();
        reload();
    }

    private void build() {
        setHeaderTitle(loc.getValue(L.LABEL_VERSION_HISTORY_OF_PART));
        setWidth("min(1150px, 95vw)");
        setHeight("85vh");
        setResizable(true);
        setDraggable(true);
        UIUtils.fitPhone(this, true);

        saveVersion.setText(loc.getValue(L.LABEL_SAVE_VERSION));
        saveVersion.addClickListener(_ -> askSaveVersion());
        showDeleted.setLabel(loc.getValue(L.LABEL_SHOW_DELETED_VERSIONS));
        showDeleted.addValueChangeListener(_ -> reload());
        emptyTrash.setText(loc.getValue(L.LABEL_EMPTY_VERSION_TRASH));
        emptyTrash.addClickListener(_ -> askEmptyTrash());
        clearHistory.setText(loc.getValue(L.LABEL_CLEAR_HISTORY));
        clearHistory.addClickListener(_ -> clear());
        HorizontalLayout toolbar = new HorizontalLayout(saveVersion, showDeleted, emptyTrash, clearHistory);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setWrap(true);

        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setPartNameGenerator(row -> row.deleted() ? CssStyles.HISTORY_ROW_DELETED : null);
        if (phone) {
            grid.addComponentColumn(this::card);
        } else {
            grid.addColumn(row -> loc.getDateHourFormat().format(row.creation())).setHeader(loc.getValue(L.LABEL_DATE)).setAutoWidth(true).setFlexGrow(0);
            grid.addComponentColumn(this::sourceCell).setHeader(loc.getValue(L.LABEL_SOURCE)).setAutoWidth(true).setFlexGrow(1);
            grid.addColumn(row -> row.wordCount()).setHeader(loc.getValue(L.LABEL_WORDS)).setAutoWidth(true).setFlexGrow(0);
            grid.addColumn(row -> StringUtils.defaultString(row.label())).setHeader(loc.getValue(L.LABEL_VERSION_LABEL)).setAutoWidth(true).setFlexGrow(0);
        }
        grid.addSelectionListener(event -> show(event.getFirstSelectedItem().orElse(null)));

        empty.setText(loc.getValue(L.MSG_HISTORY_EMPTY));
        empty.addClassName(CssStyles.HINT_TEXT);

        buildDetail();

        Component body = phone ? vertical() : horizontal();
        VerticalLayout content = new VerticalLayout(toolbar, empty, body);
        content.setPadding(false);
        content.setSizeFull();
        content.addClassNames(CssStyles.GAP_S, CssStyles.MIN_HEIGHT_0);
        content.setFlexGrow(1, body);
        add(content);

        Button close = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(close);
    }

    private HorizontalLayout horizontal() {
        grid.setWidth("46%");
        grid.setHeight("100%");
        detail.setWidth("54%");
        HorizontalLayout body = new HorizontalLayout(grid, detail);
        body.setSizeFull();
        body.setPadding(false);
        body.addClassName(CssStyles.MIN_HEIGHT_0);
        return body;
    }

    private VerticalLayout vertical() {
        grid.setWidthFull();
        grid.setHeight("38%");
        detail.setWidthFull();
        detail.setHeight("62%");
        VerticalLayout body = new VerticalLayout(grid, detail);
        body.setSizeFull();
        body.setPadding(false);
        body.addClassName(CssStyles.MIN_HEIGHT_0);
        return body;
    }

    private void buildDetail() {
        detail.setPadding(false);
        detail.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.MIN_WIDTH_0, CssStyles.GAP_S);

        meta.addClassName(CssStyles.TEXT_SEMIBOLD);
        origin.addClassName(CssStyles.HINT_TEXT);

        compare.setItems(Compare.values());
        compare.setItemLabelGenerator(c -> loc.getValue(switch (c) {
            case PREVIOUS -> L.LABEL_HISTORY_COMPARE_PREVIOUS;
            case CURRENT -> L.LABEL_HISTORY_COMPARE_CURRENT;
            case NONE -> L.LABEL_HISTORY_COMPARE_NONE;
        }));
        compare.setLabel(loc.getValue(L.LABEL_COMPARE_WITH));
        compare.addThemeName("horizontal");
        compare.setValue(Compare.PREVIOUS);
        compare.addValueChangeListener(_ -> showText());

        plain.addClassNames(CssStyles.PRE_WRAP, CssStyles.OVERFLOW_AUTO, CssStyles.WRAP_ANYWHERE);
        diff.addClassNames(CssStyles.OVERFLOW_AUTO);
        Div text = new Div(plain, diff);
        text.addClassNames(CssStyles.OVERFLOW_AUTO, CssStyles.MIN_HEIGHT_0, CssStyles.BORDERED_PANEL);
        text.setWidthFull();

        reasoning.addClassNames(CssStyles.PRE_WRAP, CssStyles.OVERFLOW_AUTO, CssStyles.WRAP_ANYWHERE);
        reasoningDetails.setSummaryText(loc.getValue(L.LABEL_REASONING));
        reasoningDetails.add(reasoning);
        reasoningDetails.setWidthFull();

        restore.setText(loc.getValue(L.LABEL_RESTORE_ON_TOP));
        restore.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        restore.addClickListener(_ -> restore());
        rollBack.setText(loc.getValue(L.LABEL_ROLL_BACK_TO_HERE));
        rollBack.addClickListener(_ -> askRollBack());
        label.setText(loc.getValue(L.LABEL_EDIT_VERSION_LABEL));
        label.addClickListener(_ -> askLabel());
        delete.addClickListener(_ -> deleteOrUndelete());
        purge.setText(loc.getValue(L.LABEL_DELETE_PERMANENTLY));
        purge.addThemeVariants(ButtonVariant.LUMO_ERROR);
        purge.addClickListener(_ -> askPurge());
        copy.setText(loc.getValue(L.LABEL_COPY_TEXT));
        copy.addClickListener(_ -> copyText());
        HorizontalLayout actions = new HorizontalLayout(restore, rollBack, label, delete, purge, copy);
        actions.setWidthFull();
        actions.setWrap(true);

        detail.add(meta, origin, compare, text, reasoningDetails, actions);
        detail.setFlexGrow(1, text);
    }

    // --- the list ---

    private Component sourceCell(RevisionRow row) {
        Span source = new Span(sourceText(row));
        HorizontalLayout cell = new HorizontalLayout(source);
        cell.setAlignItems(FlexComponent.Alignment.CENTER);
        cell.setSpacing(false);
        cell.addClassName(CssStyles.GAP_S);
        String detailText = detailText(row.sourceDetail());
        if (!detailText.isEmpty()) {
            Span extra = new Span(detailText);
            extra.addClassName(CssStyles.HINT_TEXT);
            cell.add(extra);
        }
        addBadges(cell, row);
        return cell;
    }

    /**
     * A row of the list on a phone: the source and the marks on one line, the time, the size and the label under it.
     */
    private Component card(RevisionRow row) {
        Span source = new Span(sourceText(row));
        source.addClassName(CssStyles.TEXT_SEMIBOLD);
        HorizontalLayout first = new HorizontalLayout(source);
        first.setAlignItems(FlexComponent.Alignment.CENTER);
        first.setWrap(true);
        addBadges(first, row);
        String info = loc.getDateHourFormat().format(row.creation()) + " · " + row.wordCount() + " " + loc.getValue(L.LABEL_WORDS);
        Span second = new Span(info);
        second.addClassName(CssStyles.HINT_TEXT);
        VerticalLayout card = new VerticalLayout(first, second);
        String detailText = detailText(row.sourceDetail());
        if (!detailText.isEmpty()) {
            Span extra = new Span(detailText);
            extra.addClassName(CssStyles.HINT_TEXT);
            card.add(extra);
        }
        card.setPadding(false);
        card.setSpacing(false);
        return card;
    }

    private void addBadges(HorizontalLayout cell, RevisionRow row) {
        if (row.id().equals(currentId)) {
            cell.add(badge(loc.getValue(L.LABEL_HISTORY_CURRENT_VERSION), "success"));
        }
        if (row.label() != null) {
            cell.add(badge(row.label(), "contrast"));
        }
        if (row.deleted()) {
            cell.add(badge(loc.getValue(L.LABEL_SOFT_DELETED), "error"));
        }
    }

    private static Span badge(String text, String variant) {
        Span badge = new Span(text);
        badge.getElement().getThemeList().add("badge " + variant + " small");
        return badge;
    }

    private String sourceText(RevisionRow row) {
        String text = loc.getValue(loc.getRevisionSource(row.source()));
        return row.agent() ? text + " (" + loc.getValue(L.ENUM_DOMAIN_AGENT) + ")" : text;
    }

    /**
     * The detail of a version in the language of the user when the application names it, else as it was written.
     */
    private String detailText(String stored) {
        RevisionDetail known = RevisionDetail.parse(stored);
        return known != null ? loc.getValue(loc.getRevisionDetail(known)) : StringUtils.defaultString(stored);
    }

    /**
     * Reads the list again and keeps the version that is selected.
     */
    private void reload() {
        try {
            Long keep = selected == null ? null : selected.getId();
            ChatMessage latest = chatMessageService.find(part.getId());
            if (latest != null) {
                part = latest;
            }
            List<RevisionRow> live = messageRevisionService.list(part, false);
            currentId = live.isEmpty() ? null : live.getFirst().id();
            rows = showDeleted.getValue() ? messageRevisionService.list(part, true) : live;
            grid.setItems(rows);
            empty.setVisible(rows.isEmpty());
            grid.setVisible(!rows.isEmpty());
            detail.setVisible(!rows.isEmpty());
            boolean trash = messageRevisionService.list(part, true).size() > live.size();
            emptyTrash.setEnabled(trash);
            clearHistory.setEnabled(live.size() > 1 || trash);
            RevisionRow again = rows.stream().filter(row -> row.id().equals(keep)).findFirst().orElse(null);
            if (again == null && !rows.isEmpty()) {
                again = rows.getFirst();
            }
            selected = null;
            if (again != null) {
                grid.select(again);
                // the same row selected again does not fire the listener
                show(again);
            } else {
                grid.deselectAll();
                show(null);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // --- the selected version ---

    void select(Long revisionId) {
        rows.stream().filter(row -> row.id().equals(revisionId)).findFirst().ifPresent(grid::select);
    }

    private void show(RevisionRow row) {
        try {
            selected = row == null ? null : messageRevisionService.find(row.id());
        } catch (Exception e) {
            selected = null;
            UIUtils.internalServerError(loc, e);
        }
        boolean any = selected != null;
        compare.setEnabled(any);
        reasoningDetails.setVisible(any && StringUtils.isNotEmpty(selected.getResponseReasoning()));
        reasoning.setText(any ? StringUtils.defaultString(selected.getResponseReasoning()) : "");
        if (!any) {
            meta.setText(loc.getValue(L.MSG_HISTORY_NO_VERSION_SELECTED));
            origin.setText("");
            plain.setText("");
            diff.removeAll();
            diff.setVisible(false);
            plain.setVisible(true);
            updateActions(null);
            return;
        }
        meta.setText(describe(row));
        origin.setText(restoredFromText());
        origin.setVisible(!origin.getText().isEmpty());
        showText();
        updateActions(row);
    }

    private String describe(RevisionRow row) {
        StringBuilder text = new StringBuilder(sourceText(row));
        String detailText = detailText(row.sourceDetail());
        if (!detailText.isEmpty()) {
            text.append(" · ").append(detailText);
        }
        text.append(" · ").append(loc.getDateHourFormat().format(row.creation()));
        text.append(" · ").append(row.wordCount()).append(' ').append(loc.getValue(L.LABEL_WORDS));
        text.append(" · ").append(row.tokenCount()).append(' ').append(loc.getValue(L.LABEL_TOKENS));
        return text.toString();
    }

    /**
     * For a restored version, the time of the version it is a copy of; the language is the user's, so it is made here.
     */
    private String restoredFromText() {
        if (StringUtils.isEmpty(selected.getRestoredFrom())) {
            return "";
        }
        try {
            MessageRevision from = messageRevisionService.findForUser(selected.getRestoredFrom());
            return from == null ? "" : String.format(loc.getValue(L.MSG_HISTORY_COPY_OF), loc.getDateHourFormat().format(from.getCreation()));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * The text of the selected version: as the difference to what is chosen to compare it with, or as it is.
     */
    private void showText() {
        if (selected == null) {
            return;
        }
        String text = StringUtils.defaultString(selected.getResponse());
        String base = null;
        try {
            base = switch (compare.getValue()) {
                case CURRENT -> StringUtils.defaultString(part.getResponse());
                case PREVIOUS -> previousText();
                case NONE -> null;
            };
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        if (base == null) {
            plain.setText(text);
            plain.setVisible(true);
            diff.setVisible(false);
        } else {
            diff.show(base, text);
            diff.setVisible(true);
            plain.setVisible(false);
        }
    }

    /**
     * The text of the version before the selected one (not in the trash), null when there is none.
     */
    private String previousText() throws Exception {
        List<RevisionRow> live = messageRevisionService.list(part, false);
        int at = indexOf(live, selected.getId());
        // a version in the trash is compared with the live one that is older than it
        int from = at >= 0 ? at + 1 : firstOlder(live);
        return from >= 0 && from < live.size() ? messageRevisionService.find(live.get(from).id()).getResponse() : null;
    }

    private static int indexOf(List<RevisionRow> list, Long id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private int firstOlder(List<RevisionRow> live) {
        for (int i = 0; i < live.size(); i++) {
            if (isOlder(live.get(i), selected)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isOlder(RevisionRow row, MessageRevision than) {
        int byTime = row.creation().compareTo(than.getCreation());
        return byTime < 0 || byTime == 0 && row.id() < than.getId();
    }

    private void updateActions(RevisionRow row) {
        boolean any = row != null;
        boolean trashed = any && row.deleted();
        boolean current = any && row.id().equals(currentId);
        boolean sameText = any && StringUtils.defaultString(part.getResponse()).equals(selected.getResponse());
        restore.setEnabled(any && !sameText);
        rollBack.setEnabled(any && !current);
        label.setEnabled(any);
        delete.setText(loc.getValue(trashed ? L.LABEL_UNDELETE_VERSION : L.LABEL_DELETE));
        delete.setEnabled(any && !current);
        purge.setVisible(trashed);
        copy.setEnabled(any);
    }

    // --- what the author does ---

    void restore() {
        if (selected == null) {
            return;
        }
        if (StringUtils.defaultString(part.getResponse()).equals(selected.getResponse())) {
            Notification.warning(loc.getValue(L.MSG_HISTORY_ALREADY_THE_TEXT));
            return;
        }
        try {
            changed(messageRevisionService.restoreOnTop(manuscript, part, selected), L.MSG_HISTORY_RESTORED);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void askRollBack() {
        if (selected == null) {
            return;
        }
        try {
            long newer = messageRevisionService.list(part, false).stream().filter(row -> !isOlder(row, selected) && !row.id().equals(selected.getId())).count();
            if (newer == 0) {
                rollBack();
            } else {
                ConfirmDialog.show(String.format(loc.getValue(L.MSG_HISTORY_CONFIRM_ROLL_BACK), newer), this::rollBack);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    void rollBack() {
        try {
            changed(messageRevisionService.rollBackTo(manuscript, part, selected), L.MSG_HISTORY_ROLLED_BACK);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The part has another text: tell, hand it on, show the list again.
     */
    private void changed(PartEdit edit, L message) {
        part = edit.message();
        if (edit.outdated().isEmpty()) {
            Notification.success(loc.getValue(message));
        } else {
            Notification.warning(loc.getValue(message) + " " + String.format(loc.getValue(L.MSG_REDRAFT_OUTDATED_SUMMARIES), edit.outdated().size()));
        }
        onChanged.accept(part);
        // the newest version is the text of the part now: that is the one to look at
        selected = null;
        reload();
    }

    private void askSaveVersion() {
        new TextInputDialog.Builder(loc.getValue(L.LABEL_SAVE_VERSION), this::saveVersion)
                .showCancel(true)
                .messageRequired()
                .inputMessageLabel(loc.getValue(L.LABEL_VERSION_LABEL))
                .build().open();
    }

    void saveVersion(String name) {
        try {
            // the card may have an unsaved edit, the version is the saved text
            messageRevisionService.snapshot(manuscript, part, name);
            Notification.success(loc.getValue(L.MSG_HISTORY_SAVED_VERSION));
            reload();
        } catch (IllegalStateException e) {
            Notification.warning(loc.getValue(L.MSG_HISTORY_OFF));
        } catch (IllegalArgumentException e) {
            Notification.warning(loc.getValue(L.MSG_HISTORY_EMPTY));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void askLabel() {
        if (selected == null) {
            return;
        }
        new TextInputDialog.Builder(loc.getValue(L.LABEL_VERSION_LABEL), this::setLabel)
                .showCancel(true)
                .inputMessageLabel(loc.getValue(L.LABEL_VERSION_LABEL))
                .initialValue(selected.getLabel())
                .build().open();
    }

    void setLabel(String name) {
        try {
            messageRevisionService.label(selected, name);
            reload();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    void deleteOrUndelete() {
        if (selected == null) {
            return;
        }
        try {
            if (selected.isDeleted()) {
                messageRevisionService.undelete(selected);
            } else {
                messageRevisionService.trash(selected);
            }
            reload();
        } catch (IllegalStateException e) {
            Notification.warning(loc.getValue(L.MSG_HISTORY_CURRENT_CANT_BE_DELETED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void askPurge() {
        if (selected != null) {
            ConfirmDialog.show(loc.getValue(L.MSG_HISTORY_CONFIRM_DELETE_FOR_GOOD), this::purge);
        }
    }

    void purge() {
        try {
            messageRevisionService.purge(selected);
            selected = null;
            reload();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void askEmptyTrash() {
        ConfirmDialog.show(loc.getValue(L.MSG_HISTORY_CONFIRM_EMPTY_TRASH), this::emptyTrash);
    }

    void emptyTrash() {
        try {
            int removed = messageRevisionService.emptyTrash(part);
            Notification.success(String.format(loc.getValue(L.MSG_HISTORY_TRASH_EMPTIED), removed));
            selected = null;
            reload();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void clear() {
        new ClearHistoryDialog(loc.getValue(L.LABEL_CLEAR_HISTORY), new ClearHistoryDialog.Clearing() {
            @Override
            public RevisionClearing preview(boolean keepSnapshots) throws Exception {
                return messageRevisionService.previewClear(part, keepSnapshots);
            }

            @Override
            public RevisionClearing clear(boolean keepSnapshots) throws Exception {
                return messageRevisionService.clear(part, keepSnapshots);
            }
        }, () -> {
            selected = null;
            reload();
        }).open();
    }

    private void copyText() {
        if (selected != null) {
            JavascriptLibrary.copyToClipboard(UI.getCurrent().getPage(), StringUtils.defaultString(selected.getResponse()));
            Notification.success(loc.getValue(L.MSG_COPIED_TO_CLIPBOARD));
        }
    }
}
