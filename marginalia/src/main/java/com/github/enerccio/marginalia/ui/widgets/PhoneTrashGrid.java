package com.github.enerccio.marginalia.ui.widgets;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.TrashService.TrashItem;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * {@link TrashGrid} for phones. The filters are below each other, each over the whole width, and the table has only the
 * check box column and one content column, which has everything about the deleted object below each other.
 */
@Configurable
@Extendable
public class PhoneTrashGrid extends TrashGrid {

    public PhoneTrashGrid(boolean allUsers) {
        super(allUsers);
    }

    @Override
    public TrashGrid create() throws Exception {
        super.create();
        // rows grow with their content, so long names break into lines
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        return this;
    }

    /**
     * The description is too long for the small screen, it is behind the info button of the toolbar instead.
     */
    @Override
    protected Component createDescription() {
        return null;
    }

    @Override
    protected Component createToolbar() {
        VerticalLayout toolbar = new VerticalLayout();
        toolbar.setWidthFull();
        toolbar.setPadding(false);
        toolbar.setSpacing(true);

        if (allUsers) {
            userFilter = new ComboBox<>(loc.getValue(L.LABEL_USER));
            userFilter.setPlaceholder(loc.getValue(L.LABEL_ALL_USERS));
            userFilter.setClearButtonVisible(true);
            userFilter.setItemLabelGenerator(User::getLogin);
            userFilter.setWidthFull();
            userFilter.addValueChangeListener(e -> refreshGrid());
            toolbar.add(userFilter);
        }

        typeFilter = new ComboBox<>(loc.getValue(L.LABEL_TYPE));
        typeFilter.setPlaceholder(loc.getValue(L.LABEL_ALL_TYPES));
        typeFilter.setClearButtonVisible(true);
        typeFilter.setItemLabelGenerator(loc::localizeDomainObject);
        typeFilter.setWidthFull();
        typeFilter.addValueChangeListener(e -> refreshGrid());
        toolbar.add(typeFilter);

        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.SYNC.create(), e -> refresh());
        restoreButton = new Button(loc.getValue(L.LABEL_RESTORE_SELECTED), Solid.UNDO.create(), e -> restoreSelected());
        restoreButton.setThemeName("primary");
        restoreButton.setEnabled(false);

        Button infoButton = new Button(Solid.INFO_CIRCLE.create());
        infoButton.setThemeName("tertiary");
        Popover info = new Popover();
        info.setTarget(infoButton);
        info.setWidth("300px");
        Span description = new Span(loc.getValue(L.MSG_TRASH_DESCRIPTION));
        description.addClassName(CssStyles.WHITE_SPACE_NORMAL);
        info.add(description);

        HorizontalLayout buttons = new HorizontalLayout(refreshButton, restoreButton, infoButton);
        buttons.setWidthFull();
        buttons.setPadding(false);
        toolbar.add(buttons);
        return toolbar;
    }

    @Override
    protected void addColumns() {
        grid.addComponentColumn(this::createContent).setHeader("").setFlexGrow(1);
    }

    private Component createContent(TrashItem item) {
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.getThemeList().add("spacing-xs");

        Span name = new Span(item.label());
        name.addClassNames(CssStyles.TEXT_BOLD, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE, CssStyles.MIN_WIDTH_0);
        content.add(name);
        content.add(info(L.LABEL_TYPE, loc.localizeDomainObject(item.key().type())));
        if (allUsers) {
            content.add(info(L.LABEL_USER, item.ownerLogin()));
        }
        if (item.deletedAt() != null) {
            content.add(info(L.LABEL_SOFT_DELETED, loc.getDateHourFormat().format(item.deletedAt())));
        }
        Button extendedContent = contentLink(item);
        if (extendedContent != null) {
            content.add(extendedContent);
        }
        return content;
    }

    private Span info(L label, String value) {
        Span span = new Span(loc.getValue(label) + ": " + (value != null ? value : ""));
        span.addClassNames(CssStyles.HINT_TEXT, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        return span;
    }
}
