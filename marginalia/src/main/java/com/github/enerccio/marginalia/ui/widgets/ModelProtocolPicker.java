package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * A model and a protocol to choose from the ones the user can use, for a dialog that runs the model with a choice of
 * its own (it starts with the model the owner of the dialog wants, the user can change it for this run). The protocol
 * is optional.
 */
@Configurable(preConstruction = true)
public class ModelProtocolPicker extends HorizontalLayout {

    @Autowired
    private Localization loc;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    private final ComboBox<AI> aiCombo;
    private final ComboBox<Protocol> protocolCombo;

    public ModelProtocolPicker() {
        aiCombo = new ComboBox<>(loc.getValue(L.LABEL_MODEL));
        aiCombo.setItemLabelGenerator(AI::getName);
        aiCombo.setClearButtonVisible(true);
        aiCombo.setWidth("260px");

        protocolCombo = new ComboBox<>(loc.getValue(L.LABEL_PROTOCOLS));
        protocolCombo.setItemLabelGenerator(Protocol::getName);
        protocolCombo.setClearButtonVisible(true);
        protocolCombo.setWidth("260px");

        try {
            List<AI> models = aiService.findAllForUser();
            aiCombo.setItems(models);
            List<Protocol> protocols = protocolService.findAllForUser();
            protocolCombo.setItems(protocols);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }

        setAlignItems(FlexComponent.Alignment.BASELINE);
        setPadding(false);
        add(aiCombo, protocolCombo);
    }

    /**
     * Selects the model and the protocol; null clears the choice.
     */
    public void setSelection(AI ai, Protocol protocol) {
        aiCombo.setValue(ai);
        protocolCombo.setValue(protocol);
    }

    /**
     * The chosen model, null when there is none.
     */
    public AI getAi() {
        return aiCombo.getValue();
    }

    /**
     * The chosen protocol, null for none.
     */
    public Protocol getProtocol() {
        return protocolCombo.getValue();
    }

    /**
     * Calls {@code listener} when the model or the protocol is changed, by the user or by {@link #setSelection}.
     */
    public void addSelectionListener(Runnable listener) {
        aiCombo.addValueChangeListener(_ -> listener.run());
        protocolCombo.addValueChangeListener(_ -> listener.run());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        aiCombo.setEnabled(enabled);
        protocolCombo.setEnabled(enabled);
    }
}
