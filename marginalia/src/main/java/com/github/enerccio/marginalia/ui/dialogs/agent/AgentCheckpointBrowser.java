package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.CheckpointService;
import com.github.enerccio.marginalia.domain.service.CheckpointService.CheckpointException;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Info;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointText;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.TextInputDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * The checkpoints of a run: restore points of its working copy and its workspace. The user sees who made each one (the
 * start of the run, the runner before a round that changes something, or the agent or the user on purpose), saves one,
 * restores one or rewinds to one (the conversation goes back with it), downloads the archive and deletes one. They share a quota; what the agent can do with them is in the
 * checkpoint tools. Saving and restoring change the copy, so they wait until the agent does not work.
 */
@Configurable
public class AgentCheckpointBrowser extends VerticalLayout implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private CheckpointService checkpointService;

    private final Supplier<AgentRun> run;
    private final BooleanSupplier busy;
    private final Runnable restored;
    private final Runnable rewound;

    private Grid<Info> grid;
    private Span usageLabel;
    private Span emptyLabel;
    private Button saveButton;
    // the checkpoints the conversation can go back to, read with the list
    private Set<String> rewindable = Set.of();
    // what the grid shows now, to leave it alone while nothing changed
    private String shownRows;

    /**
     * @param run      the run whose checkpoints are shown, as it is now; null for none
     * @param busy     whether the agent works now, the copy is then only to look at
     * @param restored called after a checkpoint was restored, the console shows the run again
     */
    public AgentCheckpointBrowser(Supplier<AgentRun> run, BooleanSupplier busy, Runnable restored) {
        this(run, busy, restored, restored);
    }

    /**
     * @param rewound called after the run was rewound: the messages after the checkpoint are marked, the console shows the
     *                transcript again from its start
     */
    public AgentCheckpointBrowser(Supplier<AgentRun> run, BooleanSupplier busy, Runnable restored, Runnable rewound) {
        this.run = run;
        this.busy = busy;
        this.restored = restored;
        this.rewound = rewound;
    }

    public void create() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_AUTO, CssStyles.GAP_S);

        Span help = new Span(loc.getValue(L.HELP_AGENT_CHECKPOINTS));
        help.addClassName(CssStyles.HINT_TEXT);
        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.ARROWS_ROTATE.create(), event -> refresh());
        saveButton = new Button(loc.getValue(L.LABEL_SAVE_CHECKPOINT), Solid.FLOPPY_DISK.create(), event ->
                new TextInputDialog.Builder(loc.getValue(L.LABEL_SAVE_CHECKPOINT), this::save)
                        .inputMessageLabel(loc.getValue(L.LABEL_CHECKPOINT_LABEL)).showCancel(true).build().open());
        for (Button button : List.of(refreshButton, saveButton)) {
            button.addThemeVariants(ButtonVariant.LUMO_SMALL);
        }
        HorizontalLayout toolbar = new HorizontalLayout(saveButton, refreshButton);
        toolbar.setWidthFull();
        toolbar.setWrap(true);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);

        grid = new Grid<>();
        grid.setWidthFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addColumn(Info::id).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(0).setWidth("80px");
        grid.addColumn(info -> loc.getValue(kindText(info))).setHeader(loc.getValue(L.LABEL_TYPE)).setFlexGrow(1);
        grid.addColumn(Info::round).setHeader(loc.getValue(L.LABEL_ROUNDS)).setFlexGrow(0).setWidth("80px");
        grid.addColumn(Info::label).setHeader(loc.getValue(L.LABEL_CHECKPOINT_LABEL)).setFlexGrow(3);
        grid.addColumn(info -> CheckpointText.bytes(info.bytes())).setHeader(loc.getValue(L.LABEL_SIZE)).setFlexGrow(1);
        grid.addColumn(info -> DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .format(Instant.ofEpochMilli(info.created()).atZone(ZoneId.systemDefault())))
                .setHeader(loc.getValue(L.LABEL_CREATED)).setFlexGrow(2);
        grid.addComponentColumn(this::actions).setHeader("").setFlexGrow(0).setWidth("210px");

        usageLabel = new Span();
        usageLabel.addClassNames(CssStyles.HINT_TEXT);
        emptyLabel = new Span(loc.getValue(L.MSG_AGENT_CHECKPOINTS_EMPTY));
        emptyLabel.addClassNames(CssStyles.HINT_TEXT);

        add(help, toolbar, grid, emptyLabel, usageLabel);
        refresh();
    }

    private static L kindText(Info info) {
        return switch (info.kind()) {
            case START -> L.ENUM_CHECKPOINT_START;
            case AUTOMATIC -> L.ENUM_CHECKPOINT_AUTOMATIC;
            case MANUAL -> L.ENUM_CHECKPOINT_MANUAL;
        };
    }

    private Component actions(Info info) {
        boolean working = busy.getAsBoolean();
        Button restoreButton = new Button(Solid.ROTATE_LEFT.create(), event ->
                ConfirmDialog.open(String.format(loc.getValue(L.MSG_AGENT_CONFIRM_RESTORE_CHECKPOINT), info.id()),
                        () -> restore(info), () -> {}));
        restoreButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        restoreButton.setTooltipText(loc.getValue(working ? L.MSG_AGENT_CHECKPOINT_BUSY : L.LABEL_RESTORE));
        restoreButton.setEnabled(!working);

        AgentRun current = run.get();
        boolean paused = current != null && current.getStatus().isResumable();
        Button rewindButton = new Button(Solid.BACKWARD_STEP.create(), event ->
                ConfirmDialog.open(String.format(loc.getValue(L.MSG_AGENT_CONFIRM_REWIND_CHECKPOINT), info.id()),
                        () -> rewind(info), () -> {}));
        rewindButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        rewindButton.setTooltipText(loc.getValue(working ? L.MSG_AGENT_CHECKPOINT_BUSY : !paused ? L.MSG_AGENT_REWIND_PAUSE_FIRST
                : !rewindable.contains(info.id()) ? L.MSG_AGENT_REWIND_UNAVAILABLE : L.LABEL_REWIND));
        rewindButton.setEnabled(!working && paused && rewindable.contains(info.id()));

        Button downloadButton = new Button(Solid.DOWNLOAD.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        downloadButton.setTooltipText(loc.getValue(L.LABEL_DOWNLOAD));
        Anchor download = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) downloadEvent -> {
            try {
                Path file = checkpointService.file(run.get(), info.id());
                return new DownloadResponse(Files.newInputStream(file), "checkpoint-" + info.id() + ".zip", "application/zip",
                        Files.size(file));
            } catch (Exception e) {
                throw new IOException(e);
            }
        }), "");
        download.getElement().setAttribute("download", true);
        download.add(downloadButton);

        Button deleteButton = new Button(Solid.TRASH.create(), event ->
                ConfirmDialog.open(loc.getValue(L.MSG_CONFIRM_DELETE), () -> delete(info), () -> {}));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText(loc.getValue(L.LABEL_DELETE));

        HorizontalLayout actions = new HorizontalLayout(restoreButton, rewindButton, download, deleteButton);
        actions.setSpacing(false);
        return actions;
    }

    void save(String label) {
        if (isBusy()) {
            return;
        }
        try {
            Info info = checkpointService.makeManual(run.get(), label).checkpoint();
            Notification.success(String.format(loc.getValue(L.MSG_AGENT_CHECKPOINT_SAVED), info.id()));
        } catch (CheckpointException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refresh();
    }

    void restore(Info info) {
        if (isBusy()) {
            return;
        }
        try {
            CheckpointService.Restored result = checkpointService.restore(run.get(), info.id());
            Notification.success(String.format(loc.getValue(L.MSG_AGENT_CHECKPOINT_RESTORED), info.id(), result.removedFiles().size()));
            restored.run();
        } catch (CheckpointException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refresh();
    }

    void rewind(Info info) {
        if (isBusy()) {
            return;
        }
        try {
            CheckpointService.Rewound result = checkpointService.rewind(run.get(), info.id());
            Notification.success(String.format(loc.getValue(L.MSG_AGENT_CHECKPOINT_REWOUND), info.id(), result.forgotten(),
                    result.restored().removedFiles().size()));
            rewound.run();
        } catch (CheckpointException | IllegalStateException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refresh();
    }

    void delete(Info info) {
        try {
            checkpointService.delete(run.get(), info.id(), false);
        } catch (CheckpointException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refresh();
    }

    private boolean isBusy() {
        if (busy.getAsBoolean()) {
            Notification.warning(loc.getValue(L.MSG_AGENT_CHECKPOINT_BUSY));
            refresh();
            return true;
        }
        return false;
    }

    /**
     * Reads the checkpoints again, and the buttons that depend on whether the agent works and has a working copy.
     */
    public void refresh() {
        if (grid == null) {
            return;
        }
        try {
            AgentRun current = run.get();
            List<Info> infos = current == null ? List.of() : checkpointService.list(current);
            rewindable = current == null ? Set.of() : checkpointService.rewindable(current);
            // the same rows again would replace them, and the selection of the user in them; the buttons of a row depend on the state
            String shown = busy.getAsBoolean() + ":" + (current == null ? null : current.getStatus()) + ":" + rewindable + ":" + infos;
            if (!shown.equals(shownRows)) {
                shownRows = shown;
                grid.setItems(infos);
            }
            emptyLabel.setVisible(infos.isEmpty());
            boolean canSave = current != null && current.getWorkingCopy() != null && !busy.getAsBoolean();
            saveButton.setEnabled(canSave);
            saveButton.setTooltipText(busy.getAsBoolean() ? loc.getValue(L.MSG_AGENT_CHECKPOINT_BUSY) : null);
            if (current == null || infos.isEmpty()) {
                UIUtils.setTextIfChanged(usageLabel, "");
            } else {
                CheckpointService.Usage usage = checkpointService.usage(current);
                UIUtils.setTextIfChanged(usageLabel, String.format(loc.getValue(L.MSG_AGENT_CHECKPOINTS_USAGE), CheckpointText.bytes(usage.usedBytes()),
                        CheckpointText.bytes(usage.quotaBytes()), CheckpointText.bytes(usage.reserveBytes())));
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // for the tests of the browser

    Grid<Info> getGrid() {
        return grid;
    }

    Set<String> getRewindable() {
        return rewindable;
    }

    Button getSaveButton() {
        return saveButton;
    }

    Span getUsageLabel() {
        return usageLabel;
    }
}
