package com.github.enerccio.marginalia.extensions.arcoutline.ui;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.arcoutline.model.ArcOutline;
import com.github.enerccio.marginalia.extensions.arcoutline.model.ArcOutlineSettings;
import com.github.enerccio.marginalia.extensions.arcoutline.model.HeadingOptions;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ChapterHeadings;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import static com.github.enerccio.marginalia.extensions.arcoutline.ArcOutlineLocalization.*;

/**
 * Writes the headings of the linked arcs and chapters into the text of the story, or removes every heading from it. The options are
 * kept in the user settings; the headings it would write are listed as they are set.
 */
@Configurable(preConstruction = true)
public class ChapterHeadingsDialog extends Dialog {

    @Autowired
    private Localization loc;

    private final ArcOutlineService service;
    private final Manuscript manuscript;
    private final ArcOutline outline;
    /** Called after the text changed, so the story shows it. */
    private final Runnable onChanged;

    private final Checkbox includeArcs = new Checkbox(T.localized(ARCS_GET_HEADINGS_TOO));
    private final TextField arcPrefix = new TextField(T.localized(PREFIX_OF_THE_ARCS));
    private final TextField chapterPrefix = new TextField(T.localized(PREFIX_OF_THE_CHAPTERS));
    private final Select<HeadingOptions.Numbering> numbering = new Select<>();
    private final Checkbox restartInArc = new Checkbox(T.localized(NUMBER_THE_CHAPTERS_IN_EVERY_ARC));
    private final Checkbox withTitles = new Checkbox(T.localized(PUT_THE_TITLES_INTO_THE_HEADINGS));
    private final Checkbox wipeFirst = new Checkbox(T.localized(REMOVE_EVERY_OTHER_HEADING_FIRST));
    private final Checkbox revalidate = new Checkbox(T.localized(KEEP_THE_SUMMARIES_VALID));
    private final Div preview = new Div();
    private final Button write = new Button(T.localized(WRITE_THE_HEADINGS));

    public ChapterHeadingsDialog(ArcOutlineService service, Manuscript manuscript, ArcOutline outline, Runnable onChanged) {
        this.service = service;
        this.manuscript = manuscript;
        this.outline = outline;
        this.onChanged = onChanged;

        setHeaderTitle(T.localized(CHAPTER_HEADINGS_IN_THE_TEXT));
        setWidth("560px");
        setMaxWidth("95vw");
        setMaxHeight("90vh");
        setCloseOnOutsideClick(false);

        HeadingOptions options = loadOptions();
        includeArcs.setValue(options.isIncludeArcs());
        arcPrefix.setValue(StringUtils.defaultString(options.getArcPrefix(), T.localized(DEFAULT_ARC_PREFIX)));
        chapterPrefix.setValue(StringUtils.defaultString(options.getChapterPrefix(), T.localized(DEFAULT_CHAPTER_PREFIX)));
        numbering.setLabel(T.localized(NUMBERS));
        numbering.setItems(HeadingOptions.Numbering.values());
        numbering.setItemLabelGenerator(n -> T.localized(n.getLabel()));
        numbering.setValue(options.getNumbering());
        restartInArc.setValue(options.isRestartInArc());
        withTitles.setValue(options.isWithTitles());
        wipeFirst.setValue(options.isWipeFirst());
        revalidate.setValue(options.isRevalidateSummaries());
        arcPrefix.setWidthFull();
        chapterPrefix.setWidthFull();
        numbering.setWidthFull();
        preview.addClassName(ArcOutlineStyles.HEADINGS_PREVIEW);

        includeArcs.addValueChangeListener(_ -> update());
        arcPrefix.addValueChangeListener(_ -> update());
        chapterPrefix.addValueChangeListener(_ -> update());
        numbering.addValueChangeListener(_ -> update());
        restartInArc.addValueChangeListener(_ -> update());
        withTitles.addValueChangeListener(_ -> update());

        Span help = new Span(T.localized(HEADINGS_HELP));
        help.addClassName(ArcOutlineStyles.HEADINGS_HELP);
        VerticalLayout content = new VerticalLayout(help, includeArcs, arcPrefix, chapterPrefix, numbering, restartInArc, withTitles,
                wipeFirst, revalidate, preview);
        content.setPadding(false);
        content.setSpacing(false);
        add(content);

        write.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        write.addClickListener(_ -> writeHeadings());
        Button wipe = new Button(T.localized(REMOVE_ALL_HEADINGS), _ -> ConfirmDialog.show(T.localized(REMOVE_EVERY_HEADING_LINE), this::wipeHeadings));
        wipe.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button cancel = new Button(T.localized(CANCEL), _ -> close());
        getFooter().add(wipe, cancel, write);
        DialogFocus.onOpen(this, chapterPrefix);
        DialogShortcuts.submitWith(write, arcPrefix, chapterPrefix);
        update();
    }

    private HeadingOptions loadOptions() {
        try {
            HeadingOptions options = service.getSettings().getHeadings();
            return options != null ? options : new HeadingOptions();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return new HeadingOptions();
        }
    }

    private HeadingOptions options() {
        HeadingOptions options = new HeadingOptions();
        options.setIncludeArcs(includeArcs.getValue());
        options.setArcPrefix(StringUtils.defaultString(arcPrefix.getValue()));
        options.setChapterPrefix(StringUtils.defaultString(chapterPrefix.getValue()));
        options.setNumbering(numbering.getValue());
        options.setRestartInArc(restartInArc.getValue());
        options.setWithTitles(withTitles.getValue());
        options.setWipeFirst(wipeFirst.getValue());
        options.setRevalidateSummaries(revalidate.getValue());
        return options;
    }

    /** Shows the headings the options give and enables writing them when there are some. */
    private void update() {
        arcPrefix.setVisible(includeArcs.getValue());
        HeadingOptions options = options();
        ChapterHeadings.Plan plan = ChapterHeadings.plan(outline, options, options.getArcPrefix(), options.getChapterPrefix());
        preview.removeAll();
        if (plan.headings().isEmpty()) {
            preview.add(new Span(T.localized(NO_ARC_OR_CHAPTER_IS_LINKED)));
        }
        for (ChapterHeadings.Heading heading : plan.headings()) {
            Span part = new Span(T.localized(IN_PART).formatted(heading.part()));
            part.addClassName(ArcOutlineStyles.HEADINGS_PART);
            Div line = new Div(part, new Span(heading.line()));
            line.addClassName(ArcOutlineStyles.HEADINGS_LINE);
            preview.add(line);
        }
        write.setEnabled(!plan.headings().isEmpty());
    }

    private void saveOptions(HeadingOptions options) {
        try {
            ArcOutlineSettings settings = service.getSettings();
            settings.setHeadings(options);
            service.saveSettings(settings);
        } catch (Exception e) {
            // the options are a convenience, the headings are written anyway
            UIUtils.internalServerError(loc, e);
        }
    }

    private void writeHeadings() {
        HeadingOptions options = options();
        saveOptions(options);
        try {
            report(service.writeHeadings(manuscript, outline, options, options.getArcPrefix(), options.getChapterPrefix()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void wipeHeadings() {
        HeadingOptions options = options();
        saveOptions(options);
        try {
            report(service.wipeHeadings(manuscript, options.isRevalidateSummaries()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void report(ArcOutlineService.HeadingResult result) {
        Notification.success(T.localized(PARTS_CHANGED_SUMMARIES_KEPT).formatted(result.changed(), result.revalidated()));
        if (result.unlinked() > 0 || result.outside() > 0) {
            Notification.warning(T.localized(WITHOUT_A_HEADING).formatted(result.unlinked(), result.outside()));
        }
        if (result.changed() > 0 && onChanged != null) {
            onChanged.run();
        }
        close();
    }
}
