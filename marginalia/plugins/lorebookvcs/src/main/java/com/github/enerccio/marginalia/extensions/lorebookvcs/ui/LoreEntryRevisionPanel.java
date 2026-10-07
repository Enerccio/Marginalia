package com.github.enerccio.marginalia.extensions.lorebookvcs.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LoreEntryRevision;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LoreEntryVCSData;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LorebookVCSData;
import com.github.enerccio.marginalia.extensions.lorebookvcs.service.LorebookVCSService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configurable
public class LoreEntryRevisionPanel extends HorizontalLayout {

    @Autowired
    private Localization loc;

    private final LorebookVCSService vcsService;
    private final Lorebook lorebook;
    private final LorebookEntry entry;
    private final Runnable onRevisionApplied;

    private Span indexSpan;
    private Span modifiedSpan;
    private Button deleteBtn;
    private Button prevBtn;
    private ComboBox<Integer> revSelectCombo;
    private Button nextBtn;
    private Button addBtn;

    private LorebookVCSData vcsData;
    private LoreEntryVCSData entryVcsData;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LoreEntryRevisionPanel(LorebookVCSService vcsService,
                                  Lorebook lorebook,
                                  LorebookEntry entry,
                                  Runnable onRevisionApplied) {
        this.vcsService = vcsService;
        this.lorebook = lorebook;
        this.entry = entry;
        this.onRevisionApplied = onRevisionApplied;

        setWidthFull();
        setAlignItems(Alignment.CENTER);
        getStyle().set("padding", "6px 10px");
        getStyle().set("background", "var(--lumo-contrast-5pct)");
        getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        getStyle().set("margin-bottom", "8px");

        buildUI();
        initData();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Automatically sync live edits into active revision when panel closes/detaches
        syncCurrentRevisionFromEntry();
    }

    private void buildUI() {
        Span label = new Span("Revision:");
        label.getStyle().set("font-weight", "bold");

        indexSpan = new Span("None");
        modifiedSpan = new Span("");
        modifiedSpan.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        modifiedSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout infoLayout = new VerticalLayout(indexSpan, modifiedSpan);
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);

        HorizontalLayout leftGroup = new HorizontalLayout(label, infoLayout);
        leftGroup.setAlignItems(Alignment.CENTER);

        deleteBtn = new Button(Solid.MINUS.create(), e -> deleteCurrentRevision());
        deleteBtn.setThemeName("tertiary icon small error");
        deleteBtn.setTooltipText("Delete current revision");

        prevBtn = new Button(Solid.CHEVRON_LEFT.create(), e -> shiftRevision(-1));
        prevBtn.setThemeName("tertiary icon small");
        prevBtn.setTooltipText("Previous revision");

        revSelectCombo = new ComboBox<>();
        revSelectCombo.setWidth("220px");
        revSelectCombo.setItemLabelGenerator(idx -> {
            if (entryVcsData == null || idx < 0 || idx >= entryVcsData.getRevisions().size()) return "";
            LoreEntryRevision r = entryVcsData.getRevisions().get(idx);
            return "Rev " + (idx + 1) + " (" + DATE_FORMAT.format(new Date(r.getLastModified())) + ")";
        });
        revSelectCombo.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                switchToRevision(e.getValue());
            }
        });

        nextBtn = new Button(Solid.CHEVRON_RIGHT.create(), e -> shiftRevision(1));
        nextBtn.setThemeName("tertiary icon small");
        nextBtn.setTooltipText("Next revision");

        addBtn = new Button(Solid.PLUS.create(), e -> createSnapshot());
        addBtn.setThemeName("primary icon small");
        addBtn.setTooltipText("Create snapshot revision");

        HorizontalLayout controls = new HorizontalLayout(deleteBtn, prevBtn, revSelectCombo, nextBtn, addBtn);
        controls.setAlignItems(Alignment.CENTER);
        controls.getStyle().set("margin-left", "auto");

        add(leftGroup, controls);
    }

    private void initData() {
        try {
            vcsData = vcsService.getVCSData(lorebook);
            entryVcsData = vcsData.getOrCreateEntry(entry.getUuid());

            LoreEntryRevision currentSnap = vcsService.createSnapshot(entry);

            if (entryVcsData.getRevisions().isEmpty()) {
                entryVcsData.getRevisions().add(currentSnap);
                entryVcsData.setCurrentRevision(0);
                vcsService.saveVCSData(lorebook, vcsData);
            } else {
                LoreEntryRevision activeRev = entryVcsData.getCurrent();
                if (activeRev != null) {
                    // Sync active revision in-place with entry state rather than appending a new revision
                    copyRevisionState(currentSnap, activeRev);
                    vcsService.saveVCSData(lorebook, vcsData);
                }
            }
            refreshDisplay();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Binds current live entry field values and tags back into the active revision
     */
    public void syncCurrentRevisionFromEntry() {
        if (entryVcsData == null || entryVcsData.getRevisions().isEmpty()) return;
        int currentIdx = entryVcsData.getCurrentRevision();
        if (currentIdx < 0 || currentIdx >= entryVcsData.getRevisions().size()) return;

        try {
            LoreEntryRevision liveState = vcsService.createSnapshot(entry);
            LoreEntryRevision activeRev = entryVcsData.getRevisions().get(currentIdx);
            copyRevisionState(liveState, activeRev);
            vcsService.saveVCSData(lorebook, vcsData);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void copyRevisionState(LoreEntryRevision source, LoreEntryRevision target) {
        target.setName(source.getName());
        target.setEnabled(source.isEnabled());
        target.setOrder(source.getOrder());
        target.setPayload(source.getPayload());
        target.setComment(source.getComment());
        target.setFilteringMode(source.getFilteringMode());
        target.setFiltering(source.getFiltering());
        target.setInsertionMode(source.getInsertionMode());
        target.setPositiveTags(source.getPositiveTags());
        target.setNegativeTags(source.getNegativeTags());
        target.setLastModified(System.currentTimeMillis());
    }

    private void refreshDisplay() {
        if (entryVcsData == null || entryVcsData.getRevisions().isEmpty()) {
            indexSpan.setText("0 / 0");
            modifiedSpan.setText("");
            return;
        }

        int total = entryVcsData.getRevisions().size();
        int currentIdx = entryVcsData.getCurrentRevision();
        if (currentIdx < 0) currentIdx = 0;

        indexSpan.setText((currentIdx + 1) + " / " + total);

        LoreEntryRevision currentRev = entryVcsData.getRevisions().get(currentIdx);
        modifiedSpan.setText("Modified: " + DATE_FORMAT.format(new Date(currentRev.getLastModified())));

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < total; i++) indices.add(i);

        revSelectCombo.setItems(indices);
        revSelectCombo.setValue(currentIdx);

        prevBtn.setEnabled(currentIdx > 0);
        nextBtn.setEnabled(currentIdx < total - 1);
        deleteBtn.setEnabled(total > 1);
    }

    private void createSnapshot() {
        try {
            // First bind live edits into current revision
            syncCurrentRevisionFromEntry();

            // Create new snapshot
            LoreEntryRevision newRev = vcsService.createSnapshot(entry);
            entryVcsData.getRevisions().add(newRev);
            entryVcsData.setCurrentRevision(entryVcsData.getRevisions().size() - 1);
            vcsService.saveVCSData(lorebook, vcsData);
            refreshDisplay();
            Notification.show("Revision snapshot created");
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void deleteCurrentRevision() {
        if (entryVcsData.getRevisions().size() <= 1) {
            Notification.warning("Cannot delete the last remaining revision");
            return;
        }

        try {
            int current = entryVcsData.getCurrentRevision();
            entryVcsData.getRevisions().remove(current);
            int newIdx = Math.max(0, current - 1);

            // Switch without re-syncing deleted revision
            entryVcsData.setCurrentRevision(newIdx);
            LoreEntryRevision rev = entryVcsData.getRevisions().get(newIdx);
            vcsService.applyRevisionToEntry(entry, rev);
            vcsService.saveVCSData(lorebook, vcsData);

            refreshDisplay();

            if (onRevisionApplied != null) {
                onRevisionApplied.run();
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void shiftRevision(int delta) {
        int target = entryVcsData.getCurrentRevision() + delta;
        if (target >= 0 && target < entryVcsData.getRevisions().size()) {
            switchToRevision(target);
        }
    }

    private void switchToRevision(int index) {
        try {
            // Bind live edits on current revision before switching away
            syncCurrentRevisionFromEntry();

            entryVcsData.setCurrentRevision(index);
            LoreEntryRevision rev = entryVcsData.getRevisions().get(index);
            vcsService.applyRevisionToEntry(entry, rev);
            vcsService.saveVCSData(lorebook, vcsData);

            refreshDisplay();

            if (onRevisionApplied != null) {
                onRevisionApplied.run();
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}