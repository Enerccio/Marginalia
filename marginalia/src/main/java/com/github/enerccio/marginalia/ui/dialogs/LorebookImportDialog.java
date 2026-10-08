package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookImportCandidate;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookMatch;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Resolves lorebooks stored in backup against existing lorebooks. Lorebooks with matching uuid are linked silently,
 * first dialog asks about lorebooks with same name (link or create new), second about lorebooks not found (import).
 */
@Configurable
@Extendable
public class LorebookImportDialog extends Dialog {

    public enum Mode {
        SAME_NAME,
        IMPORT,
    }

    @Autowired
    private Localization loc;

    private final Mode mode;
    private final List<LorebookImportCandidate> candidates;
    private final Runnable onConfirm;

    public LorebookImportDialog(Mode mode, List<LorebookImportCandidate> candidates, Runnable onConfirm) {
        this.mode = mode;
        this.candidates = candidates;
        this.onConfirm = onConfirm;
    }

    /**
     * Asks user about lorebooks that need decision and passes resulting decisions (keyed by lorebook uuid from backup)
     * to callback. Callback is not called when user cancels.
     */
    public static void resolve(List<LorebookImportCandidate> candidates, Consumer<Map<String, LorebookDecision>> onResolved) {
        Runnable askImport = () -> {
            if (candidates.stream().anyMatch(c -> c.getMatch() == LorebookMatch.NOT_FOUND)) {
                LorebookImportDialog dialog = new LorebookImportDialog(Mode.IMPORT, candidates, () -> onResolved.accept(toDecisions(candidates)));
                dialog.create();
                dialog.open();
            } else {
                onResolved.accept(toDecisions(candidates));
            }
        };

        if (candidates.stream().anyMatch(c -> c.getMatch() == LorebookMatch.SAME_NAME)) {
            LorebookImportDialog dialog = new LorebookImportDialog(Mode.SAME_NAME, candidates, askImport);
            dialog.create();
            dialog.open();
        } else {
            askImport.run();
        }
    }

    private static Map<String, LorebookDecision> toDecisions(List<LorebookImportCandidate> candidates) {
        Map<String, LorebookDecision> decisions = new HashMap<>();
        for (LorebookImportCandidate candidate : candidates) {
            if (candidate.getUuid() != null && candidate.getDecision() != null) {
                decisions.put(candidate.getUuid(), candidate.getDecision());
            }
        }
        return decisions;
    }

    public void create() {
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);
        setWidth("900px");
        setHeaderTitle(loc.getValue(mode == Mode.SAME_NAME ? L.LABEL_LOREBOOKS_SAME_NAME : L.LABEL_IMPORT_LOREBOOKS));

        Span message = new Span(loc.getValue(mode == Mode.SAME_NAME ? L.MSG_LOREBOOKS_SAME_NAME : L.MSG_IMPORT_LOREBOOKS));

        Grid<LorebookImportCandidate> grid = new Grid<>();
        grid.setAllRowsVisible(true);
        grid.addColumn(LorebookImportCandidate::getName)
                .setHeader(loc.getValue(L.LABEL_NAME))
                .setFlexGrow(2);
        grid.addColumn(candidate -> loc.getValue(candidate.isRoot() ? L.LABEL_YES : L.LABEL_NO))
                .setHeader(loc.getValue(L.LABEL_USED_BY_BOOK))
                .setFlexGrow(0)
                .setWidth("130px");
        grid.addColumn(LorebookImportCandidate::getEntryCount)
                .setHeader(loc.getValue(L.LABEL_ENTRIES))
                .setFlexGrow(0)
                .setWidth("100px");
        grid.addColumn(this::formatStatus)
                .setHeader(loc.getValue(L.LABEL_STATUS))
                .setFlexGrow(2);
        grid.addComponentColumn(this::createActionComponent)
                .setHeader(loc.getValue(L.LABEL_ACTION))
                .setFlexGrow(1);
        grid.setItems(candidates);

        VerticalLayout layout = new VerticalLayout(message, grid);
        layout.setPadding(false);
        add(layout);

        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), e -> close());

        if (mode == Mode.SAME_NAME) {
            Button continueButton = new Button(loc.getValue(L.LABEL_CONTINUE), e -> {
                close();
                onConfirm.run();
            });
            continueButton.setThemeName("primary");
            getFooter().add(cancelButton, continueButton);
        } else {
            Button skipButton = new Button(loc.getValue(L.LABEL_DONT_IMPORT), e -> {
                candidates.stream()
                        .filter(c -> c.getMatch() == LorebookMatch.NOT_FOUND)
                        .forEach(c -> c.setDecision(LorebookDecision.SKIP));
                close();
                onConfirm.run();
            });
            Button importButton = new Button(loc.getValue(L.LABEL_IMPORT), e -> {
                close();
                onConfirm.run();
            });
            importButton.setThemeName("primary");
            getFooter().add(cancelButton, skipButton, importButton);
        }
    }

    private String formatStatus(LorebookImportCandidate candidate) {
        String status = loc.getValue(loc.getLorebookMatch(candidate.getMatch()));
        if (candidate.getMatch() == LorebookMatch.SAME_NAME && candidate.getMatchedLorebook() != null) {
            status += ": " + candidate.getMatchedLorebook().getName();
        }
        return status;
    }

    private Component createActionComponent(LorebookImportCandidate candidate) {
        if (mode == Mode.SAME_NAME && candidate.getMatch() == LorebookMatch.SAME_NAME) {
            Select<LorebookDecision> select = new Select<>();
            select.setItems(LorebookDecision.LINK, LorebookDecision.CREATE);
            select.setItemLabelGenerator(decision -> loc.getValue(loc.getLorebookDecision(decision)));
            select.setValue(candidate.getDecision());
            select.addValueChangeListener(e -> candidate.setDecision(e.getValue()));
            return select;
        }
        if (mode == Mode.IMPORT && candidate.getMatch() == LorebookMatch.NOT_FOUND) {
            Checkbox checkbox = new Checkbox(loc.getValue(L.LABEL_IMPORT), candidate.getDecision() == LorebookDecision.CREATE);
            checkbox.addValueChangeListener(e -> candidate.setDecision(Boolean.TRUE.equals(e.getValue())
                    ? LorebookDecision.CREATE : LorebookDecision.SKIP));
            return checkbox;
        }
        return new Span(loc.getValue(loc.getLorebookDecision(candidate.getDecision())));
    }
}
