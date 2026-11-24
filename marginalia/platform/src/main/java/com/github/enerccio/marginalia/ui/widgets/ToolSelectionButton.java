package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.service.ToolService;
import com.github.enerccio.marginalia.domain.service.tool.ToolProviderInfo;
import com.github.enerccio.marginalia.domain.service.tool.ToolSelection;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ToolSelectionDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The button that lets the user choose the tool suites the model may use (the {@link ToolSelectionDialog}), for a dialog of
 * an extension that runs the model with functions. It shows the number of things that are on, hides itself while no
 * suite is installed (checked again when it is attached and after each choice; call {@link #refresh()} when the dialog
 * opens again) and keeps the {@link ToolSelection}, which the owner saves and turns into functions with
 * {@link ToolService#resolve}.
 * <pre>
 * ToolSelectionButton tools = new ToolSelectionButton();
 * tools.setValue(settings.getTools());
 * tools.addValueChangeListener(settings::setTools);
 * </pre>
 */
@Configurable(preConstruction = true)
public class ToolSelectionButton extends Button {

    @Autowired
    private Localization loc;

    @Autowired
    private ToolService toolService;

    private ToolSelection value = new ToolSelection();
    private final List<Consumer<ToolSelection>> listeners = new ArrayList<>();

    public ToolSelectionButton() {
        setIcon(VaadinIcon.TOOLS.create());
        setTooltipText(loc.getValue(L.MSG_TOOLS_TOOLTIP));
        addClickListener(e -> open());
        refresh();
    }

    /** The choice; never null. */
    public ToolSelection getValue() {
        return value;
    }

    /** Shows a choice without telling the listeners. */
    public void setValue(ToolSelection selection) {
        this.value = selection == null ? new ToolSelection() : selection;
        refresh();
    }

    /** Called with the new choice when the user saves the dialog. */
    public void addValueChangeListener(Consumer<ToolSelection> listener) {
        listeners.add(listener);
    }

    /** Looks at the installed suites again: the button is shown only when there is one, with the number that is on. */
    public void refresh() {
        List<ToolProviderInfo> providers = toolService.getProviders();
        setVisible(!providers.isEmpty());
        int count = value.count(providers);
        setText(count > 0 ? String.format(loc.getValue(L.LABEL_TOOLS_COUNT), count) : loc.getValue(L.LABEL_TOOLS));
    }

    private void open() {
        List<ToolProviderInfo> providers = toolService.getProviders();
        if (providers.isEmpty()) {
            refresh();
            return;
        }
        new ToolSelectionDialog(providers, value, saved -> {
            value = saved;
            refresh();
            new ArrayList<>(listeners).forEach(l -> l.accept(saved));
        }).open();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        refresh();
    }
}
