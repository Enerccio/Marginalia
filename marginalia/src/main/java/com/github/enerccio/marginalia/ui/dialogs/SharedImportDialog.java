package com.github.enerccio.marginalia.ui.dialogs;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.LorebookPreview;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * "Import from other user": what other users share (lorebooks, protocols) with name, author and a row that opens to show
 * what is in it, and an <em>Import</em> button that makes a copy owned by the current user. The originals are only read here.
 * Use {@link #forLorebooks} and {@link #forProtocols}.
 *
 * @param <T> what is shared
 * @param <R> what importing makes
 */
public class SharedImportDialog<T extends OwnedEntity, R> extends Dialog {

    /** Loads what other users share. */
    @FunctionalInterface
    public interface Loader<T> {
        List<T> load() throws Exception;
    }

    /** Makes the copy of an item. */
    @FunctionalInterface
    public interface Importer<T, R> {
        R importShared(T item) throws Exception;
    }

    private final Localization loc;
    private final Loader<T> loader;
    private final Importer<T, R> importer;
    private final Function<T, String> nameOf;
    private final Function<T, String> descriptionOf;
    private final Function<T, Component> detailsOf;
    private final Consumer<R> onImported;
    private final String emptyText;

    private final Set<String> openItems = new HashSet<>();
    private final Grid<T> grid = new Grid<>();
    private final Span empty = new Span();

    /**
     * @param descriptionOf the one line under the name in the list, null for none (no column)
     * @param detailsOf     what the row shows when it is opened
     * @param onImported    called with the copy after the dialog is closed
     */
    public SharedImportDialog(Localization loc, String emptyText, Loader<T> loader, Function<T, String> nameOf,
                              Function<T, String> descriptionOf, Function<T, Component> detailsOf, Importer<T, R> importer,
                              Consumer<R> onImported) {
        this.loc = loc;
        this.emptyText = emptyText;
        this.loader = loader;
        this.nameOf = nameOf;
        this.descriptionOf = descriptionOf;
        this.detailsOf = detailsOf;
        this.importer = importer;
        this.onImported = onImported;
        create();
    }

    private void create() {
        setHeaderTitle(loc.getValue(L.LABEL_IMPORT_FROM_OTHER_USER));
        setWidth("min(1000px, 95vw)");
        setHeight("min(700px, 90vh)");
        setResizable(true);
        setCloseOnEsc(true);

        Span help = new Span(loc.getValue(L.MSG_IMPORT_FROM_OTHER_USER));
        help.addClassNames(CssStyles.HINT_TEXT, CssStyles.WHITE_SPACE_NORMAL);

        empty.setText(emptyText);
        empty.addClassNames(CssStyles.HINT_TEXT, CssStyles.WHITE_SPACE_NORMAL);

        grid.setSizeFull();
        // a row grows with its content, so a long name breaks into lines and the opened details are as high as they are
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_NO_BORDER);
        grid.setDetailsVisibleOnClick(false);
        grid.addComponentColumn(this::createToggle).setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE);
        grid.addComponentColumn(item -> {
            Span name = new Span(StringUtils.defaultString(nameOf.apply(item)));
            name.addClassNames(CssStyles.TEXT_BOLD, CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
            return name;
        }).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(2);
        grid.addColumn(this::authorOf).setHeader(loc.getValue(L.LABEL_AUTHOR)).setFlexGrow(1);
        if (descriptionOf != null) {
            grid.addComponentColumn(item -> {
                String description = StringUtils.defaultString(descriptionOf.apply(item));
                Span line = new Span(description);
                line.addClassNames(CssStyles.CLIPPED_LINE, CssStyles.TEXT_SECONDARY);
                if (!description.isEmpty()) {
                    UIUtils.addTooltip(line, description);
                }
                return line;
            }).setHeader(loc.getValue(L.LABEL_DESCRIPTION)).setFlexGrow(2);
        }
        grid.addComponentColumn(item -> {
            Button importButton = new Button(loc.getValue(L.LABEL_IMPORT), Solid.FILE_IMPORT.create(), event -> importItem(item));
            importButton.setThemeName("primary small");
            return importButton;
        }).setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_LARGE);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createDetails));

        VerticalLayout content = new VerticalLayout(help, empty, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(true);
        content.setFlexGrow(1, grid);
        add(content);

        getFooter().add(new Button(loc.getValue(L.LABEL_CLOSE), event -> close()));

        reload();
    }

    /**
     * Loads what is shared again.
     */
    private void reload() {
        try {
            List<T> items = new ArrayList<>(loader.load());
            items.sort(Comparator.comparing((T item) -> StringUtils.defaultString(nameOf.apply(item)), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(this::authorOf, String.CASE_INSENSITIVE_ORDER));
            empty.setVisible(items.isEmpty());
            grid.setVisible(!items.isEmpty());
            Set<String> uuids = new HashSet<>();
            items.forEach(item -> uuids.add(item.getUuid()));
            openItems.retainAll(uuids);
            grid.setItems(items);
            items.stream().filter(item -> openItems.contains(item.getUuid())).forEach(item -> grid.setDetailsVisible(item, true));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /** The full name of the owner, else the login. */
    String authorOf(T item) {
        User owner = item.getOwner();
        if (owner == null) {
            return "";
        }
        return StringUtils.defaultIfBlank(owner.getFullName(), owner.getLogin());
    }

    private Button createToggle(T item) {
        Button toggle = new Button((grid.isDetailsVisible(item) ? Solid.ANGLE_UP : Solid.ANGLE_DOWN).create(), event -> {
            boolean open = !grid.isDetailsVisible(item);
            grid.setDetailsVisible(item, open);
            if (open) {
                openItems.add(item.getUuid());
            } else {
                openItems.remove(item.getUuid());
            }
            event.getSource().setIcon((open ? Solid.ANGLE_UP : Solid.ANGLE_DOWN).create());
        });
        toggle.setThemeName("tertiary small");
        return toggle;
    }

    /** What a row shows when it is opened. */
    Component createDetails(T item) {
        Component details = detailsOf.apply(item);
        Div box = new Div(details);
        box.addClassNames(CssStyles.SUBTLE_BOX, CssStyles.MIN_WIDTH_0, CssStyles.MAX_WIDTH_FULL, CssStyles.BORDER_BOX);
        return box;
    }

    private void importItem(T item) {
        try {
            R copy = importer.importShared(item);
            Notification.success(String.format(loc.getValue(L.MSG_IMPORTED_FROM_OTHER_USER), nameOf.apply(item)));
            close();
            if (onImported != null) {
                onImported.accept(copy);
            }
        } catch (IllegalArgumentException e) {
            // the author took it back, or deleted it, while the list was open
            Notification.warning(loc.getValue(L.MSG_NOT_SHARED_ANY_MORE));
            reload();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The dialog with the lorebooks other users share; a row opens to the read only view of the lorebook (with its shared sub
     * lorebooks).
     *
     * @param onImported called with the copy of the lorebook that was made
     */
    public static SharedImportDialog<Lorebook, Lorebook> forLorebooks(Localization loc, LorebookService lorebookService,
                                                                      Consumer<Lorebook> onImported) {
        return new SharedImportDialog<>(loc, loc.getValue(L.MSG_NO_SHARED_LOREBOOKS), lorebookService::findAllShared,
                Lorebook::getName, Lorebook::getDescription,
                lorebook -> {
                    LorebookPreview preview = new LorebookPreview().create();
                    // what the author keeps to themselves is not shown, and not copied either
                    preview.setSubbookFilter(Lorebook::isShared);
                    preview.setHeight("400px");
                    preview.refresh(lorebook);
                    return preview;
                },
                source -> lorebookService.importShared(source, null), onImported);
    }

    /**
     * The dialog with the protocols other users share; a row opens to the settings of the protocol.
     *
     * @param onImported called with the copy of the protocol that was made
     */
    public static SharedImportDialog<Protocol, Protocol> forProtocols(Localization loc, ProtocolService protocolService,
                                                                      Consumer<Protocol> onImported) {
        return new SharedImportDialog<>(loc, loc.getValue(L.MSG_NO_SHARED_PROTOCOLS), protocolService::findAllShared,
                Protocol::getName, null, protocol -> protocolDetails(loc, protocol),
                source -> protocolService.importShared(source, null), onImported);
    }

    private static Component protocolDetails(Localization loc, Protocol protocol) {
        VerticalLayout details = new VerticalLayout();
        details.setPadding(false);
        details.setSpacing(false);
        details.getThemeList().add("spacing-xs");
        details.add(line(loc, L.LABEL_TYPE, protocol.getProtocolType() == null ? null : loc.getValue(loc.getProtocolType(protocol.getProtocolType()))));
        details.add(line(loc, L.LABEL_MAX_TOKENS, protocol.getMaxTokens()));
        details.add(line(loc, L.LABEL_REPLY_TOKENS, protocol.getReplyTokens()));
        details.add(line(loc, L.LABEL_TEMPERATURE, Boolean.TRUE.equals(protocol.getTemperatureEnabled()) ? protocol.getTemperature() : null));
        details.add(line(loc, L.LABEL_TOP_P, Boolean.TRUE.equals(protocol.getTopPEnabled()) ? protocol.getTopP() : null));
        details.add(line(loc, L.LABEL_FREQUENCY_PENALTY, Boolean.TRUE.equals(protocol.getFrequencyPenaltyEnabled()) ? protocol.getFrequencyPenalty() : null));
        details.add(line(loc, L.LABEL_PRESENCE_PENALTY, Boolean.TRUE.equals(protocol.getPresencePenaltyEnabled()) ? protocol.getPresencePenalty() : null));
        return details;
    }

    /**
     * A "label: value" line, nothing when there is no value (the setting is not used).
     */
    private static Component line(Localization loc, L label, Object value) {
        HorizontalLayout line = new HorizontalLayout();
        line.setPadding(false);
        line.setSpacing(false);
        line.setAlignItems(FlexComponent.Alignment.BASELINE);
        if (value == null) {
            line.setVisible(false);
            return line;
        }
        Span labelSpan = new Span(loc.getValue(label) + ": ");
        labelSpan.addClassName(CssStyles.TEXT_SEMIBOLD);
        line.add(labelSpan, new Span(String.valueOf(value)));
        line.setSpacing(true);
        return line;
    }
}
