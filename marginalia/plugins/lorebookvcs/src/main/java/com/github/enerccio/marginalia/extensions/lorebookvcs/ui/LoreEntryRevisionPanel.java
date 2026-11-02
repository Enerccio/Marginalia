package com.github.enerccio.marginalia.extensions.lorebookvcs.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.ManuscriptLockedException;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LoreEntryRevision;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LoreEntryVCSData;
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

import static com.github.enerccio.marginalia.extensions.lorebookvcs.LorebookVCSLocalization.*;

// the constructor already reads the history and reports errors with loc, so the fields are injected before it runs
@Configurable(preConstruction = true)
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
        // on a phone the controls go under the label
        setWrap(true);
        addClassNames(CssStyles.BORDERED_PANEL, CssStyles.PADDING_COMPACT_NARROW);

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
        Span label = new Span(T.localized(REVISION));
        label.addClassName(CssStyles.TEXT_BOLD);

        indexSpan = new Span(T.localized(NONE));
        modifiedSpan = new Span("");
        modifiedSpan.addClassName(CssStyles.HINT_TEXT);

        VerticalLayout infoLayout = new VerticalLayout(indexSpan, modifiedSpan);
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);

        HorizontalLayout leftGroup = new HorizontalLayout(label, infoLayout);
        leftGroup.setAlignItems(Alignment.CENTER);

        deleteBtn = new Button(Solid.MINUS.create(), e -> deleteCurrentRevision());
        deleteBtn.setThemeName("tertiary icon small error");
        deleteBtn.setTooltipText(T.localized(DELETE_CURRENT_REVISION));

        prevBtn = new Button(Solid.CHEVRON_LEFT.create(), e -> shiftRevision(-1));
        prevBtn.setThemeName("tertiary icon small");
        prevBtn.setTooltipText(T.localized(PREVIOUS_REVISION));

        revSelectCombo = new ComboBox<>();
        revSelectCombo.setWidth("220px");
        revSelectCombo.setMaxWidth("100%");
        revSelectCombo.setItemLabelGenerator(idx -> {
            if (entryVcsData == null || idx < 0 || idx >= entryVcsData.getRevisions().size()) return "";
            LoreEntryRevision r = entryVcsData.getRevisions().get(idx);
            return T.localized(REV).formatted(idx + 1, DATE_FORMAT.format(new Date(r.getLastModified())));
        });
        revSelectCombo.addValueChangeListener(e -> {
            if (e.isFromClient() && e.getValue() != null) {
                switchToRevision(e.getValue());
            }
        });

        nextBtn = new Button(Solid.CHEVRON_RIGHT.create(), e -> shiftRevision(1));
        nextBtn.setThemeName("tertiary icon small");
        nextBtn.setTooltipText(T.localized(NEXT_REVISION));

        addBtn = new Button(Solid.PLUS.create(), e -> createSnapshot());
        addBtn.setThemeName("primary icon small");
        addBtn.setTooltipText(T.localized(CREATE_SNAPSHOT_REVISION));

        HorizontalLayout controls = new HorizontalLayout(deleteBtn, prevBtn, revSelectCombo, nextBtn, addBtn);
        controls.setAlignItems(Alignment.CENTER);
        controls.setWrap(true);
        controls.addClassName(CssStyles.MARGIN_LEFT_AUTO);

        add(leftGroup, controls);
    }

    private void initData() {
        try {
            LoreEntryRevision currentSnap = vcsService.createSnapshot(entry);
            entryVcsData = vcsService.updateEntryData(lorebook, entry.getUuid(), data -> {
                LoreEntryRevision activeRev = data.getCurrent();
                if (data.getRevisions().isEmpty()) {
                    data.getRevisions().add(currentSnap);
                    data.setCurrentRevision(0);
                } else if (activeRev != null) {
                    // Sync active revision in-place with entry state rather than appending a new revision
                    copyRevisionState(currentSnap, activeRev);
                }
            });
            refreshDisplay();
        } catch (ManuscriptLockedException locked) {
            // an agent works on the lorebook: the history can be looked at, but opening an entry must not write to it
            LoreEntryVCSData stored = vcsService.getVCSData(lorebook).getEntries().get(entry.getUuid());
            entryVcsData = stored == null ? new LoreEntryVCSData() : stored;
            refreshDisplay();
            for (Button button : List.of(deleteBtn, prevBtn, nextBtn, addBtn)) {
                button.setEnabled(false);
            }
            revSelectCombo.setEnabled(false);
            addBtn.setTooltipText(T.localized(THE_LOREBOOK_IS_WORKED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Binds current live entry field values and tags back into the active revision
     */
    public void syncCurrentRevisionFromEntry() {
        if (entryVcsData == null || entryVcsData.getRevisions().isEmpty()) return;

        try {
            LoreEntryRevision liveState = vcsService.createSnapshot(entry);
            entryVcsData = vcsService.updateEntryData(lorebook, entry.getUuid(), data -> syncActive(data, liveState));
        } catch (ManuscriptLockedException locked) {
            // nothing of the entry could have changed while an agent has the lorebook
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void syncActive(LoreEntryVCSData data, LoreEntryRevision liveState) {
        LoreEntryRevision activeRev = data.getCurrent();
        if (activeRev != null) {
            copyRevisionState(liveState, activeRev);
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
        target.setPreventRecursion(source.isPreventRecursion());
        target.setActivationContext(source.getActivationContext());
        target.setActivationParts(source.getActivationParts());
        target.setFromMessage(source.getFromMessage());
        target.setToMessage(source.getToMessage());
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
        if (currentIdx >= total) currentIdx = total - 1;

        indexSpan.setText((currentIdx + 1) + " / " + total);

        LoreEntryRevision currentRev = entryVcsData.getRevisions().get(currentIdx);
        modifiedSpan.setText(T.localized(MODIFIED).formatted(DATE_FORMAT.format(new Date(currentRev.getLastModified()))));

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
            // Bind live edits into current revision, then add the new snapshot
            LoreEntryRevision liveState = vcsService.createSnapshot(entry);
            LoreEntryRevision newRev = vcsService.createSnapshot(entry);
            entryVcsData = vcsService.updateEntryData(lorebook, entry.getUuid(), data -> {
                syncActive(data, liveState);
                data.getRevisions().add(newRev);
                data.setCurrentRevision(data.getRevisions().size() - 1);
            });
            refreshDisplay();
            Notification.show(T.localized(REVISION_SNAPSHOT_CREATED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void deleteCurrentRevision() {
        if (entryVcsData.getRevisions().size() <= 1) {
            Notification.warning(T.localized(CANNOT_DELETE_THE_LAST_REMAINING));
            return;
        }

        try {
            // Switch without re-syncing deleted revision
            entryVcsData = vcsService.updateEntryData(lorebook, entry.getUuid(), data -> {
                if (data.getRevisions().size() <= 1) return;
                int current = Math.max(0, data.getCurrentRevision());
                if (current < data.getRevisions().size()) {
                    data.getRevisions().remove(current);
                }
                data.setCurrentRevision(Math.max(0, current - 1));
            });
            LoreEntryRevision rev = entryVcsData.getCurrent();
            if (rev != null) {
                vcsService.applyRevisionToEntry(entry, rev);
            }

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
            LoreEntryRevision liveState = vcsService.createSnapshot(entry);
            entryVcsData = vcsService.updateEntryData(lorebook, entry.getUuid(), data -> {
                syncActive(data, liveState);
                if (index >= 0 && index < data.getRevisions().size()) {
                    data.setCurrentRevision(index);
                }
            });
            LoreEntryRevision rev = entryVcsData.getCurrent();
            if (rev != null) {
                vcsService.applyRevisionToEntry(entry, rev);
            }

            refreshDisplay();

            if (onRevisionApplied != null) {
                onRevisionApplied.run();
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
