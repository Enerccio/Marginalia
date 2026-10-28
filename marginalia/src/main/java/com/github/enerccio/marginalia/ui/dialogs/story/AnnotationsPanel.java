package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * The annotations of the whole book as a list, a tab of the story editor: grouped by part in the order of the story (the parts of
 * the other branches after them), a filter for the ones that need a look, the ones that are closed and all. Each part has a button
 * that brings it to the screen. The list is made again when the annotations change or the tab is shown.
 */
@Configurable(preConstruction = true)
public class AnnotationsPanel extends VerticalLayout {

    /**
     * Which annotations the list shows.
     */
    enum Filter {
        OPEN(L.LABEL_ANNOTATION_FILTER_OPEN),
        CLOSED(L.LABEL_ANNOTATION_FILTER_CLOSED),
        ALL(L.LABEL_ALL);

        private final L label;

        Filter(L label) {
            this.label = label;
        }

        boolean accepts(AnnotationStatus status) {
            return switch (this) {
                case OPEN -> status == AnnotationStatus.OPEN || status == AnnotationStatus.ORPHANED;
                case CLOSED -> status == AnnotationStatus.RESOLVED || status == AnnotationStatus.DISMISSED;
                case ALL -> true;
            };
        }
    }

    /**
     * The most annotations the list draws at once.
     */
    static final int LIMIT = 100;

    @Autowired
    private Localization loc;

    private final StoryAnnotations annotations;
    private final AnnotationView.Host host;
    private final ComboBox<Filter> filter = new ComboBox<>();
    private final Span summary = new Span();
    private final VerticalLayout list = new VerticalLayout();

    // where the parts of the active branch are, the first part of the story is 1
    private Map<Long, Integer> positions = Map.of();

    public AnnotationsPanel(StoryAnnotations annotations, AnnotationView.Host host) {
        this.annotations = annotations;
        this.host = host;
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassNames(CssStyles.GAP_S, CssStyles.ANNOTATION_PANEL);

        filter.setItems(Filter.values());
        filter.setItemLabelGenerator(item -> loc.getValue(item.label));
        filter.setValue(Filter.OPEN);
        filter.setAllowCustomValue(false);
        filter.setWidthFull();
        filter.addValueChangeListener(event -> refresh());
        summary.addClassName(CssStyles.TEXT_SECONDARY);

        list.setPadding(false);
        list.setSpacing(false);
        list.addClassName(CssStyles.GAP_S);
        list.setWidthFull();

        add(filter, summary, list);
        setFlexGrow(1, list);
        annotations.addChangeListener(this::refresh);
    }

    /**
     * Makes the list again.
     *
     * @param positions where the parts of the active branch are, by the id of the part
     */
    public void refresh(Map<Long, Integer> positions) {
        this.positions = positions == null ? Map.of() : positions;
        refresh();
    }

    public void refresh() {
        list.removeAll();
        List<Annotation> shown = new ArrayList<>();
        int open = 0;
        for (Annotation annotation : annotations.all()) {
            if (annotation.getStatus() == AnnotationStatus.OPEN || annotation.getStatus() == AnnotationStatus.ORPHANED) {
                open++;
            }
            if (filter.getValue().accepts(annotation.getStatus())) {
                shown.add(annotation);
            }
        }
        shown.sort(Comparator.comparingInt((Annotation a) -> positions.getOrDefault(a.getPartId(), Integer.MAX_VALUE))
                .thenComparing(Annotation::getPartId)
                .thenComparingInt(a -> a.getStart() == null ? -1 : a.getStart())
                .thenComparing(Annotation::getId));

        summary.setText(String.format(loc.getValue(L.MSG_ANNOTATIONS_SUMMARY), open, shown.size()));
        if (shown.isEmpty()) {
            Span none = new Span(loc.getValue(L.MSG_ANNOTATIONS_NONE));
            none.addClassName(CssStyles.HINT_TEXT);
            list.add(none);
            return;
        }

        Long currentPart = null;
        int drawn = 0;
        for (Annotation annotation : shown) {
            if (drawn++ >= LIMIT) {
                Span more = new Span(String.format(loc.getValue(L.MSG_ANNOTATIONS_LIMITED), LIMIT, shown.size()));
                more.addClassName(CssStyles.HINT_TEXT);
                list.add(more);
                break;
            }
            if (!annotation.getPartId().equals(currentPart)) {
                currentPart = annotation.getPartId();
                list.add(partHeader(annotation));
            }
            list.add(new AnnotationView(annotation, annotations, host));
        }
    }

    private Span partHeader(Annotation annotation) {
        Integer position = positions.get(annotation.getPartId());
        if (position == null) {
            Span other = new Span(loc.getValue(L.MSG_ANNOTATION_OTHER_BRANCH));
            other.addClassNames(CssStyles.ANNOTATION_PART, CssStyles.TEXT_SECONDARY);
            return other;
        }
        Span header = new Span();
        header.addClassName(CssStyles.ANNOTATION_PART);
        Button go = new Button(String.format(loc.getValue(L.LABEL_ANNOTATION_PART_NUMBER), position),
                event -> host.goToPart(annotation));
        go.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        header.add(go);
        return header;
    }

    /**
     * The number of annotations that need a look, shown on the tab.
     */
    public int openCount() {
        return (int) annotations.all().stream()
                .filter(annotation -> Filter.OPEN.accepts(annotation.getStatus())).count();
    }

}
