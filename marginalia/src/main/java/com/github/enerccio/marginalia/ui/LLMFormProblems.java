package com.github.enerccio.marginalia.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasPlaceholder;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * What is wrong with the fields of a form right now, for the help assistant's model, see {@link
 * LLMInteract#getLLMProblems()}: the fields that are marked invalid (with the message they show) and the required ones
 * that are empty. Only the label of a field and what it says about itself, never its value (a password, a title, a
 * text of a story).
 */
public final class LLMFormProblems {

    /** More than this is not a list the model can use, the user has to start with the first ones. */
    static final int MAX_PROBLEMS = 15;

    private LLMFormProblems() {
    }

    /**
     * The problems of the visible fields inside the component. The fields of a nested {@link LLMInteract} (it reports
     * its own) and of what is hidden are left out.
     */
    public static List<String> scan(Component root) {
        List<String> problems = new ArrayList<>();
        root.getChildren().forEach(child -> collect(child, problems));
        return problems;
    }

    private static void collect(Component component, List<String> problems) {
        if (problems.size() >= MAX_PROBLEMS || LLMSnapshot.isHidden(component)) {
            return;
        }
        if (component instanceof HasValue<?, ?> field) {
            String problem = problemOf(component, field);
            if (problem != null) {
                problems.add(problem);
            }
        }
        if (!(component instanceof LLMInteract)) {
            component.getChildren().forEach(child -> collect(child, problems));
        }
    }

    private static String problemOf(Component component, HasValue<?, ?> field) {
        if (component instanceof HasValidation validation && validation.isInvalid()) {
            String message = validation.getErrorMessage();
            return "\"" + name(component) + "\": " + (StringUtils.isBlank(message) ? "the value is not accepted" : message.strip());
        }
        if (field.isRequiredIndicatorVisible() && !field.isReadOnly() && component.getElement().isEnabled() && field.isEmpty()) {
            return "\"" + name(component) + "\": required and empty";
        }
        return null;
    }

    /** What the user reads as the name of the field: its label, else its aria label, else its placeholder. */
    private static String name(Component component) {
        String label = component instanceof HasLabel hasLabel ? hasLabel.getLabel() : null;
        if (StringUtils.isBlank(label)) {
            label = component.getElement().getAttribute("aria-label");
        }
        if (StringUtils.isBlank(label) && component instanceof HasPlaceholder placeholder) {
            label = placeholder.getPlaceholder();
        }
        return StringUtils.isBlank(label) ? "a field without a label" : label.strip();
    }
}
