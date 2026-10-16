package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.vaadin.flow.component.Component;

/**
 * The settings of an {@link ExtensionAIFactory}'s provider in the AI dialog. Created by
 * {@link ExtensionAIFactory#createForm} for the type settings tab; the general settings of the AI (name, context,
 * response limit, reasoning...) belong to the dialog.
 */
public interface ExtensionAIForm {

    /**
     * The fields, shown in the tab of the dialog (usually a {@code FormLayout}).
     */
    Component getComponent();

    /**
     * Checks the values before the AI is saved or tested. When they can't be used the form tells the user why (e.g. a
     * notification) and returns false.
     */
    boolean validate();

    /**
     * Writes the values into the settings of the AI ({@link ExtensionAI#getSettings()}). Called on the AI that is
     * saved, or on a throw-away copy to test the connection with the values that are not saved yet.
     */
    void apply(ExtensionAI ai);

}
