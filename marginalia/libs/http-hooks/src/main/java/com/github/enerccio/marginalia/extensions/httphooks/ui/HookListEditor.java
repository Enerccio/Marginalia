package com.github.enerccio.marginalia.extensions.httphooks.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.httphooks.HttpHook;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * The settings form of a <b>list</b> of {@link HttpHook}s, the requests made in order around a batch: a row for each (address, method,
 * timeout, wait, headers, body) with a button that removes it, and a button that adds one. The headers are shown opened and stored
 * protected: the caller passes what opens and what protects a text ({@code InferenceServices.revealSecret} / {@code protectSecret}).
 * The texts are the caller's (they are localized by the plugin).
 */
public class HookListEditor extends VerticalLayout {

    /** The texts of the form. */
    public record Labels(String address, String method, String headers, String body, String timeoutSeconds, String waitSeconds,
                         String request, String addRequest, String removeRequest) {
    }

    private final Labels labels;
    private final String rowClassName;
    private final VerticalLayout rows = new VerticalLayout();
    private final List<Row> list = new ArrayList<>();

    /** @param rowClassName a CSS class for the frame of a row, null for none */
    public HookListEditor(Labels labels, String rowClassName) {
        this.labels = labels;
        this.rowClassName = rowClassName;
        setPadding(false);
        setSpacing(true);
        rows.setPadding(false);
        rows.setSpacing(true);
        Button add = new Button(labels.addRequest(), Solid.PLUS.create(), e -> addRow(new HttpHook(), UnaryOperator.identity()));
        add.setThemeName("tertiary small");
        add(rows, add);
    }

    /** Shows the requests. @param reveal opens the protected headers (null: they stay hidden) */
    public void show(List<HttpHook> hooks, UnaryOperator<String> reveal) {
        list.clear();
        rows.removeAll();
        for (HttpHook hook : hooks) {
            addRow(hook, reveal);
        }
    }

    /** The requests as they are shown, the empty ones left out. @param protect protects the headers, they usually hold a secret; the plain text is not kept */
    public List<HttpHook> read(UnaryOperator<String> protect) {
        List<HttpHook> hooks = new ArrayList<>();
        for (Row row : list) {
            HttpHook hook = row.read(protect);
            if (!hook.isEmpty()) {
                hooks.add(hook);
            }
        }
        return hooks;
    }

    private void addRow(HttpHook hook, UnaryOperator<String> reveal) {
        Row row = new Row();
        row.show(hook, reveal);
        list.add(row);
        rows.add(row);
        number();
    }

    private void number() {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).title.setText(labels.request() + " " + (i + 1));
        }
    }

    /** One request: its number, a button that removes it and its fields. */
    private class Row extends VerticalLayout {

        final Span title = new Span();
        private final TextField url = new TextField(labels.address());
        private final TextField method = new TextField(labels.method());
        private final TextArea headers = new TextArea(labels.headers());
        private final TextArea body = new TextArea(labels.body());
        private final IntegerField timeout = new IntegerField(labels.timeoutSeconds());
        private final IntegerField wait = new IntegerField(labels.waitSeconds());

        Row() {
            setPadding(false);
            setSpacing(true);
            if (rowClassName != null) {
                addClassName(rowClassName);
            }
            Button delete = new Button(Solid.TRASH.create(), e -> {
                list.remove(this);
                rows.remove(this);
                number();
            });
            delete.setThemeName("tertiary small");
            delete.setTooltipText(labels.removeRequest());
            delete.setAriaLabel(labels.removeRequest());
            HorizontalLayout head = new HorizontalLayout(title, delete);
            head.setAlignItems(FlexComponent.Alignment.CENTER);

            url.setWidthFull();
            method.setWidth("8em");
            headers.setWidthFull();
            headers.setMinHeight("4em");
            body.setWidthFull();
            body.setMinHeight("4em");
            for (IntegerField f : List.of(timeout, wait)) {
                f.setMin(0);
                f.setMax(f == timeout ? HttpHook.MAX_TIMEOUT_SECONDS : HttpHook.MAX_WAIT_SECONDS);
                f.setStepButtonsVisible(true);
            }
            timeout.setPlaceholder(String.valueOf(HttpHook.DEFAULT_TIMEOUT_SECONDS));
            HorizontalLayout numbers = new HorizontalLayout(method, timeout, wait);
            numbers.setWrap(true);
            add(head, url, numbers, headers, body);
        }

        void show(HttpHook hook, UnaryOperator<String> reveal) {
            url.setValue(StringUtils.defaultString(hook.getUrl()));
            method.setValue(hook.getMethod());
            headers.setValue(StringUtils.defaultString(hook.headersText(reveal)));
            body.setValue(StringUtils.defaultString(hook.getBody()));
            timeout.setValue(hook.getTimeoutSeconds() == HttpHook.DEFAULT_TIMEOUT_SECONDS ? null : hook.getTimeoutSeconds());
            wait.setValue(hook.getWaitSeconds() == 0 ? null : hook.getWaitSeconds());
        }

        HttpHook read(UnaryOperator<String> protect) {
            HttpHook hook = new HttpHook();
            hook.setUrl(StringUtils.defaultIfBlank(url.getValue().strip(), null));
            hook.setMethod(StringUtils.defaultIfBlank(method.getValue().strip(), "POST"));
            hook.setProtectedHeaders(protect.apply(headers.getValue()));
            hook.setBody(StringUtils.defaultIfBlank(body.getValue(), null));
            hook.setTimeoutSeconds(positive(timeout));
            hook.setWaitSeconds(positive(wait));
            return hook;
        }

        private static int positive(IntegerField field) {
            Integer value = field.getValue();
            return value == null || value <= 0 ? 0 : value;
        }
    }
}
