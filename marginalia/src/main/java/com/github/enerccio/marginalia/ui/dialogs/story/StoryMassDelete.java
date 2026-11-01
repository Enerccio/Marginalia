package com.github.enerccio.marginalia.ui.dialogs.story;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The mass delete of the story tabs: while it is on, every part shows a check box and a bar under the story holds the delete button.
 * The selection is kept by part id, not by card: the story loads only a window of the parts, so the card of a selected part can be
 * dropped and made again while the user scrolls. Deleting asks first, then soft deletes the parts one by one (what followed a deleted
 * part continues from its parent, like the delete of a single part) behind a progress bar that can't be stopped, then the story is
 * drawn again and the mass delete is off. A click on a check box with Shift held ticks (or clears) all the parts between it and the
 * check box clicked before. Both the desktop and the phone story tab use it.
 */
@Configurable
public class StoryMassDelete {

    @Autowired
    private Localization loc;

    @Autowired
    private ChatMessageService chatMessageService;

    private final StoryHost host;
    private final Runnable modeChanged;
    private final Runnable reload;

    private final Set<Long> selected = new HashSet<>();
    // the ids of the parts of the branch in the order of the story, what a range of ticks is taken from
    private List<Long> order = List.of();
    // the part ticked or cleared last, where a click with Shift starts
    private Long anchor;
    private boolean active;
    private HorizontalLayout bar;
    private Span count;
    private Button deleteButton;

    /**
     * @param host        the window of the book, it hands out the book to delete from
     * @param modeChanged the story tab shows or hides the check boxes of its cards (it asks {@link #isActive} and {@link #isSelected})
     * @param reload      the story tab draws the story again from the database
     */
    public StoryMassDelete(StoryHost host, Runnable modeChanged, Runnable reload) {
        this.host = host;
        this.modeChanged = modeChanged;
        this.reload = reload;
    }

    /**
     * The bar with the delete button, hidden while the mass delete is off.
     */
    public Component getBar() {
        if (bar == null) {
            count = new Span();
            deleteButton = new Button(loc.getValue(L.LABEL_DELETE), Solid.TRASH.create(), event -> confirmDelete());
            deleteButton.setThemeName("error primary small");
            Button close = new Button(loc.getValue(L.LABEL_CANCEL), event -> deactivate());
            close.setThemeName("tertiary small");

            bar = new HorizontalLayout(count, deleteButton, close);
            bar.setWidthFull();
            bar.setPadding(false);
            bar.setSpacing(true);
            bar.setAlignItems(FlexComponent.Alignment.CENTER);
            bar.addClassNames(CssStyles.PADDING_COMPACT, CssStyles.BORDER_TOP_SUBTLE);
            bar.setVisible(false);
            refreshBar();
        }
        return bar;
    }

    /**
     * Keeps the browser from selecting text when the check boxes of the parts ({@link CssStyles#MASS_DELETE_BOX}) inside the panel of the
     * story are clicked with Shift. Done once for a panel.
     */
    public void listenTo(Element panel) {
        JavascriptLibrary.noTextSelectionOnShiftClick(panel, CssStyles.MASS_DELETE_BOX);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isSelected(Long messageId) {
        return messageId != null && selected.contains(messageId);
    }

    /**
     * Turns the mass delete on or off.
     */
    public void toggle() {
        if (active) {
            deactivate();
        } else {
            active = true;
            getBar().setVisible(true);
            modeChanged.run();
        }
    }

    /**
     * Turns the mass delete off and forgets the selection.
     */
    public void deactivate() {
        if (!active && selected.isEmpty()) {
            return;
        }
        active = false;
        selected.clear();
        anchor = null;
        refreshBar();
        if (bar != null) {
            bar.setVisible(false);
        }
        modeChanged.run();
    }

    public void setSelected(Long messageId, boolean on) {
        if (messageId == null) {
            return;
        }
        if (on ? selected.add(messageId) : selected.remove(messageId)) {
            refreshBar();
        }
    }

    /**
     * The user clicked the check box of the part. With Shift held, all the parts between it and the one clicked before are ticked or
     * cleared like it.
     */
    public void clicked(Long messageId, boolean on, boolean shift) {
        int from = anchor == null ? -1 : order.indexOf(anchor);
        int to = order.indexOf(messageId);
        if (shift && from >= 0 && to >= 0) {
            for (Long id : order.subList(Math.min(from, to), Math.max(from, to) + 1)) {
                setSelected(id, on);
            }
            // the cards between the two are not the ones clicked
            modeChanged.run();
        } else {
            setSelected(messageId, on);
        }
        anchor = messageId;
    }

    /**
     * The parts of the branch the story shows, in the order of the story. The selected parts that are not among them are forgotten
     * (they were deleted or the story shows another branch now).
     */
    public void setBranch(List<ChatMessage> branch) {
        order = new ArrayList<>(branch.stream().map(ChatMessage::getId).toList());
        if (selected.retainAll(order)) {
            refreshBar();
        }
        if (anchor != null && !order.contains(anchor)) {
            anchor = null;
        }
    }

    public void setEnabled(boolean enabled) {
        getBar();
        bar.setEnabled(enabled);
    }

    private void refreshBar() {
        if (bar == null) {
            return;
        }
        count.setText(String.format(loc.getValue(L.LABEL_N_SELECTED), selected.size()));
        deleteButton.setEnabled(!selected.isEmpty());
    }

    private void confirmDelete() {
        if (selected.isEmpty()) {
            return;
        }
        ConfirmDialog.show(String.format(loc.getValue(L.MSG_CONFIRM_MASS_DELETE), selected.size()), this::delete);
    }

    private void delete() {
        Set<Long> ids = new HashSet<>(selected);
        AtomicReference<Exception> failure = new AtomicReference<>();
        Manuscript start;
        try {
            start = host.refreshManuscript();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }

        ProgressBarDialog progress = new ProgressBarDialog(true);
        progress.setTitle(loc.getValue(L.MSG_DELETING_PARTS));
        progress.setTotal((long) ids.size());
        progress.create();
        progress.setAction(dialog -> {
            try {
                Manuscript book = start;
                // from the end of the story: a part is then deleted while the parts before it are still what the database has
                List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(book.getActiveLeaf());
                for (ChatMessage message : branch.reversed()) {
                    if (ids.contains(message.getId())) {
                        book = chatMessageService.deleteNodeAndMigrateChildren(message, book, false);
                        dialog.updateProgress();
                    }
                }
            } catch (Exception e) {
                failure.set(e);
            }
        });
        progress.setAfterAction(() -> {
            if (failure.get() != null) {
                UIUtils.internalServerError(loc, failure.get());
            }
            deactivate();
            reload.run();
        });
        progress.open();
    }
}
