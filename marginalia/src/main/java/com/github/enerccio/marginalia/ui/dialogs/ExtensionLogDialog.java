package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.traits.APILocals;
import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextArea;
import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The log an extension wrote with the logger from {@link OsgiService#createLogger(Bundle)}: its last part, with
 * refresh and clear.
 */
@Configurable
@Extendable
public class ExtensionLogDialog extends Dialog implements LLMInteract {

    private static final int MAX_BYTES = 256 * 1024;

    @Autowired
    private OsgiService osgiService;

    @Autowired
    private Localization loc;

    private final Bundle bundle;
    private final TextArea text = new TextArea();

    public static void show(Bundle bundle, String name) {
        new ExtensionLogDialog(bundle, name).open();
    }

    private ExtensionLogDialog(Bundle bundle, String name) {
        this.bundle = bundle;
        setHeaderTitle(name);
        setWidth("1000px");

        text.setReadOnly(true);
        text.setWidthFull();
        text.setHeight("500px");
        text.addClassName(CssStyles.TEXT_MONOSPACE);
        add(text);

        Button refresh = new Button("", VaadinIcon.REFRESH.create(), e -> load());
        Button clear = new Button("", VaadinIcon.TRASH.create(), e -> clear());
        Button close = new Button("", e -> close());
        // labels need the injected localization, which is there only after the constructor
        addAttachListener(e -> {
            refresh.setText(loc.getValue(L.LABEL_REFRESH));
            clear.setText(loc.getValue(L.LABEL_CLEAR));
            close.setText(loc.getValue(L.LABEL_CLOSE));
            text.setPlaceholder(loc.getValue(L.MSG_EXTENSION_LOG_EMPTY));
            load();
        });
        getFooter().add(refresh, clear, close);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void load() {
        try {
            text.setValue(osgiService.readLog(bundle, MAX_BYTES));
            // newest lines are at the end
            JavascriptLibrary.scrollTextAreaToBottom(text.getElement());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void clear() {
        try {
            osgiService.clearLog(bundle);
            load();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
