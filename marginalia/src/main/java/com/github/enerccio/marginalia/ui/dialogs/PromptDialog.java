package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

@Configurable
public class PromptDialog extends Dialog {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    private Localization loc;

    private final String prompt;
    private TextArea textArea;
    private ComboBox<String> formatCombo;

    public PromptDialog(String prompt) {
        this.prompt = prompt;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_SHOW_PROMPT));
        setWidth("850px");
        setHeight("650px");
        setResizable(true);
        setDraggable(true);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeUndefined();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);

        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setWidthFull();
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        formatCombo = new ComboBox<>();
        formatCombo.setItems("YAML", "JSON", "Raw");
        formatCombo.setValue("YAML");
        formatCombo.setWidth("120px");
        formatCombo.addValueChangeListener(e -> updatePromptView(e.getValue()));

        topBar.add(formatCombo);

        textArea = new TextArea();
        textArea.setSizeFull();
        textArea.setReadOnly(true);

        mainLayout.add(topBar, textArea);
        mainLayout.setFlexGrow(1, textArea);
        add(mainLayout);

        Button closeButton = new Button(loc.getValue(L.LABEL_OK), event -> close());
        closeButton.setThemeName("primary");

        HorizontalLayout footerLayout = new HorizontalLayout(closeButton);
        footerLayout.setWidthFull();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        getFooter().add(footerLayout);

        updatePromptView("YAML");
    }

    private void updatePromptView(String format) {
        if (StringUtils.isBlank(prompt)) {
            textArea.setValue("");
            return;
        }

        switch (format) {
            case "YAML" -> textArea.setValue(formatPromptAsYaml(prompt));
            case "JSON" -> textArea.setValue(formatPromptAsJson(prompt));
            default -> textArea.setValue(prompt);
        }
    }

    private String formatPromptAsYaml(String rawPrompt) {
        try {
            Object parsedObject = parseJsonObject(rawPrompt);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setWidth(Integer.MAX_VALUE);
            options.setSplitLines(false);
            options.setPrettyFlow(true);

            Representer representer = new Representer(options) {
                {
                    this.representers.put(String.class, data -> {
                        String str = (String) data;
                        if (str.contains("\n")) {
                            String[] lines = str.split("\n", -1);
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < lines.length; i++) {
                                sb.append(lines[i].stripTrailing());
                                if (i < lines.length - 1) {
                                    sb.append("\n");
                                }
                            }
                            str = sb.toString();

                            return representScalar(Tag.STR, str, DumperOptions.ScalarStyle.LITERAL);
                        }
                        return representScalar(Tag.STR, str, DumperOptions.ScalarStyle.PLAIN);
                    });
                }
            };

            Yaml yaml = new Yaml(representer, options);
            return yaml.dump(parsedObject);
        } catch (Exception e) {
            return rawPrompt;
        }
    }

    private String formatPromptAsJson(String rawPrompt) {
        try {
            Object parsedObject = parseJsonObject(rawPrompt);
            return gson.toJson(parsedObject);
        } catch (Exception e) {
            return rawPrompt;
        }
    }

    private Object parseJsonObject(String rawPrompt) {
        Object javaObject = gson.fromJson(rawPrompt, Object.class);
        if (javaObject instanceof String strVal && (strVal.startsWith("{") || strVal.startsWith("["))) {
            try {
                javaObject = gson.fromJson(strVal, Object.class);
            } catch (Exception ignored) {
            }
        }
        return javaObject;
    }
}