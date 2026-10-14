package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.TagService.TagUsage;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.TextInputDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.text.NumberFormat;

/**
 * All tags of the user with the number of objects related to each. A tag can be renamed or deleted; renaming a tag to
 * the value of another tag asks whether to merge the two.
 */
@Configurable
public class TagsView extends VerticalLayout {

    @Autowired
    private TagService tagService;

    @Autowired
    private Localization loc;

    private final Grid<TagUsage> grid = new Grid<>(TagUsage.class, false);

    public TagsView() {
        setSizeFull();
        setPadding(false);
        grid.setSizeFull();
        add(grid);
        // texts need the injected localization, which is there only after the constructor
        addAttachListener(e -> {
            createColumns();
            refresh();
        });
    }

    private void createColumns() {
        grid.removeAllColumns();
        grid.addColumn(usage -> usage.tag().getValue())
                .setHeader(loc.getValue(L.LABEL_NAME))
                .setSortable(true)
                .setFlexGrow(1);
        grid.addColumn(usage -> NumberFormat.getIntegerInstance().format(usage.objects()))
                .setHeader(loc.getValue(L.LABEL_OBJECTS))
                .setComparator(TagUsage::objects)
                .setSortable(true)
                .setFlexGrow(0).setWidth(UIConstants.NUMBER_COLUMN_SIZE);
        grid.addComponentColumn(usage -> {
            Button renameButton = new Button(Solid.PEN.create(), e -> rename(usage.tag()));
            UIUtils.addTooltip(renameButton, loc.getValue(L.LABEL_RENAME));
            Button deleteButton = new Button(Solid.TRASH.create(), e -> delete(usage));
            deleteButton.setThemeName("error tertiary");
            UIUtils.addTooltip(deleteButton, loc.getValue(L.LABEL_DELETE));
            return new HorizontalLayout(renameButton, deleteButton);
        }).setHeader("").setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_HUGE);
    }

    public void refresh() {
        try {
            grid.setItems(tagService.findUsageForUser());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void rename(Tag tag) {
        new TextInputDialog.Builder(loc.getValue(L.MSG_RENAME_TAG), newValue -> rename(tag, newValue.trim()))
                .inputMessageLabel(loc.getValue(L.LABEL_NAME))
                .initialValue(tag.getValue())
                .messageRequired()
                .showCancel(true)
                .build()
                .open();
    }

    private void rename(Tag tag, String newValue) {
        if (StringUtils.isBlank(newValue) || newValue.equals(tag.getValue())) {
            return;
        }
        try {
            Tag existing = tagService.findRenameConflict(tag, newValue);
            if (existing == null) {
                if (tagService.rename(tag, newValue) == null) {
                    Notification.error(loc.getValue(L.MSG_TAG_RENAME_NOT_FOUND));
                }
                refresh();
                return;
            }
            // renaming makes the tag an alias of the existing one: the user decides whether to merge them
            String message = String.format(loc.getValue(L.MSG_CONFIRM_MERGE_TAG),
                    existing.getValue(), tag.getValue(), tag.getValue(), existing.getValue());
            ConfirmDialog.show(message, loc.getValue(L.LABEL_MERGE), loc.getValue(L.LABEL_CANCEL),
                    () -> merge(tag, existing), () -> {}, false);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void merge(Tag tag, Tag into) {
        try {
            tagService.merge(tag, into);
            refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void delete(TagUsage usage) {
        String message = String.format(loc.getValue(L.MSG_CONFIRM_DELETE_TAG), usage.tag().getValue(), usage.objects());
        ConfirmDialog.show(message, () -> {
            try {
                tagService.delete(usage.tag(), false);
                refresh();
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }
}
