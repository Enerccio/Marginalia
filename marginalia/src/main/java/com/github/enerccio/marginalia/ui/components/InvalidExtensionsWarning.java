package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.traits.APILocals;
import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * The red bar at the top of the workspace that tells that some extensions are invalid (they do not fit this version of
 * the application, so they are installed but not running) - otherwise it is only seen in Admin → Extensions or when
 * the functionality of the extension is missing. The administrator sees which extensions on hover, the other users are
 * asked to tell the administrator. Nothing is shown (the component is hidden) when every extension is fine.
 */
@Configurable
@Extendable
public class InvalidExtensionsWarning extends HorizontalLayout {

    @Autowired
    private Localization loc;

    // not there where the extension framework is off (the tests)
    @Autowired(required = false)
    private OsgiService osgiService;

    private final boolean administrator;

    /**
     * @param administrator whether the current user is an administrator, who gets the names of the extensions
     */
    public InvalidExtensionsWarning(boolean administrator) {
        this.administrator = administrator;
    }

    /**
     * Builds the bar; the injected services are there only after the constructor.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = InvalidExtensionsWarning.class))
    public InvalidExtensionsWarning create() {
        setWidthFull();
        setSpacing(true);
        setPadding(false);
        setAlignItems(FlexComponent.Alignment.CENTER);
        addClassNames(CssStyles.NOTICE_ERROR, CssStyles.FLEX_SHRINK_0);
        add(Solid.EXCLAMATION_TRIANGLE.create(), new Span());
        refresh();
        return this;
    }

    /**
     * Looks which extensions are invalid now and shows or hides the bar.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "invalid", type = List.class),
                    @APILocals(name = "text", type = Span.class),
                    @APILocals(name = "names", type = StringBuilder.class),
                    @APILocals(name = "bundle", type = Bundle.class)})
    public void refresh() {
        List<Bundle> invalid = osgiService == null ? List.of() : osgiService.getInvalidExtensions();
        setVisible(!invalid.isEmpty());
        if (invalid.isEmpty()) {
            return;
        }
        Span text = (Span) getComponentAt(1);
        if (administrator) {
            text.setText(loc.getValue(L.MSG_INVALID_EXTENSIONS_ADMIN));
            StringBuilder names = new StringBuilder(loc.getValue(L.MSG_INVALID_EXTENSIONS_ADMIN_LIST));
            for (Bundle bundle : invalid) {
                names.append("\n").append(bundle.getSymbolicName()).append(" ").append(bundle.getVersion());
            }
            UIUtils.addTooltip(this, names.toString());
        } else {
            text.setText(loc.getValue(L.MSG_INVALID_EXTENSIONS_USER));
            getElement().removeAttribute("title");
        }
    }

}
