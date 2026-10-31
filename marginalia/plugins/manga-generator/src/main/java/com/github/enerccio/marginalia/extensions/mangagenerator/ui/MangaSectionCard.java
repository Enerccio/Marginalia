package com.github.enerccio.marginalia.extensions.mangagenerator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceErrors;
import com.github.enerccio.marginalia.domain.service.UsageContext;
import com.github.enerccio.marginalia.extensions.mangagenerator.MangaGeneratorLog;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.Manga;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaSection;
import com.github.enerccio.marginalia.extensions.mangagenerator.service.Defaults;
import com.github.enerccio.marginalia.extensions.mangagenerator.service.MangaService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.mangagenerator.MangaGeneratorLocalization.T;

/**
 * One section of a manga: the parts of the story it adapts, and its script and image tags, which can be generated, edited
 * and copied. The card runs one generation at a time; sections run side by side.
 */
@Configurable(preConstruction = true)
public class MangaSectionCard extends VerticalLayout {

    /** Updates of the screen are at most this far apart while the answer streams in. */
    private static final long REFRESH_NANOS = 150_000_000L;

    @Autowired
    private Localization loc;

    private final MangaService service;
    private final Manuscript manuscript;
    private final Manga manga;
    private final MangaSection section;
    private final Supplier<MangaSection> previous;
    private final Runnable onChange;
    private final Consumer<Boolean> onRun;
    private final Runnable onDelete;

    private IntegerField contextStart;
    private IntegerField contextEnd;
    private IntegerField startPart;
    private IntegerField endPart;
    private MenuBar overrides;
    private Tabs modeTabs;
    private Tab scriptTab;
    private Tab tagsTab;
    private Details reasoningDetails;
    private Markdown reasoningMarkdown;
    private Markdown outputMarkdown;
    private Span emptyHint;
    private TextArea editArea;
    private VerticalLayout outputBox;
    private Button generateButton;
    private Button editButton;
    private Button saveEditButton;
    private Button cancelEditButton;
    private Button copyButton;
    private Button requestButton;
    private Button deleteButton;

    private boolean tagsMode;
    private boolean editing;
    private boolean locked;
    private boolean generatingTags;
    private CancellationToken activeToken;

    public MangaSectionCard(MangaService service, Manuscript manuscript, Manga manga, MangaSection section, int number,
                            Supplier<MangaSection> previous, Runnable onChange, Consumer<Boolean> onRun, Runnable onDelete) {
        this.service = service;
        this.manuscript = manuscript;
        this.manga = manga;
        this.section = section;
        this.previous = previous;
        this.onChange = onChange;
        this.onRun = onRun;
        this.onDelete = onDelete;

        setWidthFull();
        setPadding(true);
        setSpacing(true);
        addClassName(MangaStyles.CARD);

        add(buildHeader(number), buildRanges(), buildOutput(), buildButtons());
        refreshView();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (activeToken != null) {
            // the window was closed, what came in so far is kept
            activeToken.cancel();
        }
    }

    // --- building ---

    private HorizontalLayout buildHeader(int number) {
        Span title = new Span(T.localized("Section %d").formatted(number));
        title.addClassName(CssStyles.TEXT_BOLD);

        scriptTab = new Tab(T.localized("Script"));
        tagsTab = new Tab(T.localized("Tags"));
        modeTabs = new Tabs(scriptTab, tagsTab);
        modeTabs.addSelectedChangeListener(e -> {
            tagsMode = e.getSelectedTab() == tagsTab;
            if (editing) {
                setEditing(false, false);
            }
            refreshView();
        });

        overrides = new MenuBar();
        overrides.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        SubMenu menu = overrides.addItem(T.localized("Override")).getSubMenu();
        menu.addItem(T.localized("Script prompt"), e -> editOverride(T.localized("Script prompt of this section"),
                section.getPromptOverride(), inheritedPrompt(), section::setPromptOverride));
        menu.addItem(T.localized("Tags prompt"), e -> editOverride(T.localized("Tags prompt of this section"),
                section.getTagsPromptOverride(), inheritedTagsPrompt(), section::setTagsPromptOverride));
        menu.addItem(T.localized("Additional rules"), e -> editOverride(T.localized("Additional rules of this section"),
                section.getAdditionalRules(), StringUtils.defaultString(manga.getAdditionalRules()), section::setAdditionalRules));
        menu.addItem(T.localized("Rules addendum"), e -> editOverride(T.localized("Rules added to the rules of the manga"),
                section.getAdditionalRulesAddendum(), "", section::setAdditionalRulesAddendum));

        HorizontalLayout header = new HorizontalLayout(title, modeTabs, overrides);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.addClassName(MangaStyles.BAR);
        return header;
    }

    private HorizontalLayout buildRanges() {
        startPart = rangeField(T.localized("Adapt from part"), section.getStartPart(), section::setStartPart);
        endPart = rangeField(T.localized("to part"), section.getEndPart(), section::setEndPart);
        contextStart = rangeField(T.localized("Context from part"), section.getContextStart(), section::setContextStart);
        contextEnd = rangeField(T.localized("to part"), section.getContextEnd(), section::setContextEnd);
        HorizontalLayout ranges = new HorizontalLayout(startPart, endPart, contextStart, contextEnd);
        ranges.setWidthFull();
        ranges.setWrap(true);
        return ranges;
    }

    private IntegerField rangeField(String label, int value, Consumer<Integer> setter) {
        IntegerField field = new IntegerField(label);
        field.setMin(1);
        field.setStepButtonsVisible(true);
        field.setWidth("11em");
        field.setValue(value);
        field.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue() >= 1) {
                setter.accept(e.getValue());
                onChange.run();
            }
        });
        return field;
    }

    private VerticalLayout buildOutput() {
        reasoningMarkdown = new Markdown();
        reasoningDetails = new Details("", reasoningMarkdown);
        reasoningDetails.setWidthFull();

        outputMarkdown = new Markdown();
        outputMarkdown.setWidthFull();
        emptyHint = new Span();
        emptyHint.addClassName(MangaStyles.EMPTY);

        editArea = new TextArea();
        editArea.setWidthFull();
        editArea.setMinHeight("16em");
        editArea.setVisible(false);

        outputBox = new VerticalLayout(outputMarkdown, emptyHint, editArea);
        outputBox.setPadding(false);
        outputBox.setSpacing(false);
        outputBox.setWidthFull();
        outputBox.addClassName(MangaStyles.OUTPUT);

        VerticalLayout output = new VerticalLayout(reasoningDetails, outputBox);
        output.setPadding(false);
        output.setSpacing(false);
        output.setWidthFull();
        return output;
    }

    private HorizontalLayout buildButtons() {
        generateButton = new Button(Solid.WAND_MAGIC_SPARKLES.create(), e -> generateOrAbort());
        generateButton.setThemeName("primary");

        editButton = new Button(Solid.PEN.create(), e -> setEditing(true, false));
        editButton.setTooltipText(T.localized("Edit the text"));
        saveEditButton = new Button(T.localized("Save"), e -> setEditing(false, true));
        saveEditButton.setThemeName("primary");
        cancelEditButton = new Button(T.localized("Cancel"), e -> setEditing(false, false));

        copyButton = new Button(Solid.COPY.create(), e -> copy());
        copyButton.setTooltipText(T.localized("Copy"));
        requestButton = new Button(Solid.EYE.create(), e -> showRequest());
        requestButton.setTooltipText(T.localized("Show the request the model gets"));
        deleteButton = new Button(Solid.TRASH.create(), e -> ConfirmDialog.show(T.localized("Delete this section?"), onDelete));
        deleteButton.setTooltipText(T.localized("Delete"));
        deleteButton.setThemeName("error");

        HorizontalLayout buttons = new HorizontalLayout(generateButton, editButton, saveEditButton, cancelEditButton,
                copyButton, requestButton, deleteButton);
        buttons.setWidthFull();
        buttons.setWrap(true);
        buttons.setAlignItems(Alignment.CENTER);
        return buttons;
    }

    // --- the view ---

    private String text() {
        return tagsMode ? section.getTags() : section.getScript();
    }

    private void refreshView() {
        boolean busy = activeToken != null;

        String reasoning = tagsMode ? section.getTagsReasoning() : section.getReasoning();
        if (StringUtils.isNotBlank(reasoning)) {
            long millis = tagsMode ? section.getTagsReasoningMillis() : section.getReasoningMillis();
            boolean finished = tagsMode ? section.isTagsReasoningFinished() : section.isReasoningFinished();
            String seconds = String.format(Locale.ROOT, "%.1f", millis / 1000.0);
            reasoningDetails.setSummaryText(finished ? T.localized("Thought for %s s").formatted(seconds)
                    : T.localized("Thinking for %s s").formatted(seconds));
            reasoningMarkdown.setContent(reasoning);
            reasoningDetails.setVisible(true);
        } else {
            reasoningDetails.setVisible(false);
        }

        String text = text();
        outputMarkdown.setVisible(!editing && StringUtils.isNotBlank(text));
        outputMarkdown.setContent(text);
        emptyHint.setVisible(!editing && StringUtils.isBlank(text));
        emptyHint.setText(busy && generatingTags == tagsMode
                ? T.localized("Waiting for the model...")
                : tagsMode ? T.localized("No tags yet. Generate them from the script, or write them.")
                : T.localized("No script yet. Generate it, or write it."));

        boolean here = busy && generatingTags == tagsMode;
        generateButton.setText(here ? T.localized("Abort") : tagsMode ? T.localized("Generate tags") : T.localized("Generate script"));
        generateButton.setIcon((here ? Solid.BAN : Solid.WAND_MAGIC_SPARKLES).create());
        generateButton.setEnabled(!editing && (!busy || here));

        editButton.setVisible(!editing);
        editButton.setEnabled(!busy);
        saveEditButton.setVisible(editing);
        cancelEditButton.setVisible(editing);
        copyButton.setEnabled(StringUtils.isNotBlank(text));
        requestButton.setEnabled(!busy && !editing);

        boolean free = !busy && !editing;
        for (IntegerField field : new IntegerField[]{startPart, endPart, contextStart, contextEnd}) {
            field.setEnabled(free);
        }
        overrides.setEnabled(free);
        deleteButton.setEnabled(free && !locked);
    }

    /**
     * The dialog locks the structure (deleting sections) while any section generates.
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
        refreshView();
    }

    private void setEditing(boolean editing, boolean save) {
        if (editing) {
            editArea.setValue(text());
        } else if (save) {
            if (tagsMode) {
                section.setTags(editArea.getValue());
            } else {
                section.setScript(editArea.getValue());
            }
            onChange.run();
        }
        this.editing = editing;
        editArea.setVisible(editing);
        refreshView();
    }

    private void copy() {
        UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", text());
        Notification.show(tagsMode ? T.localized("Tags copied") : T.localized("Script copied"));
    }

    // --- overrides ---

    private String inheritedPrompt() {
        return StringUtils.defaultIfBlank(manga.getPromptOverride(), Defaults.DEFAULT_PROMPT);
    }

    private String inheritedTagsPrompt() {
        return StringUtils.defaultIfBlank(manga.getTagsPromptOverride(), Defaults.DEFAULT_TAGS_PROMPT);
    }

    private void editOverride(String title, String current, String inherited, Consumer<String> setter) {
        new TextEditDialog(title, T.localized("Empty or unchanged: the setting of the manga is used"), current, inherited, value -> {
            setter.accept(value);
            onChange.run();
        }).open();
    }

    // --- the request ---

    private MangaService.Request buildRequest() throws Exception {
        return tagsMode ? service.buildTagsRequest(manuscript, manga, section)
                : service.buildScriptRequest(manuscript, manga, section, previous.get());
    }

    private void showRequest() {
        try {
            MangaService.Request request = buildRequest();
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle(T.localized("Request"));
            dialog.setWidth("70vw");
            dialog.setHeight("80vh");

            Span stats = new Span(statsText(request));
            stats.addClassName(CssStyles.HINT_TEXT);
            TextArea area = new TextArea();
            area.setReadOnly(true);
            area.setWidthFull();
            area.setValue(request.asText());
            VerticalLayout content = new VerticalLayout(stats, area);
            content.setSizeFull();
            content.setPadding(false);
            content.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);
            area.addClassName(CssStyles.MIN_HEIGHT_0);
            content.expand(area);
            dialog.add(content);
            dialog.getFooter().add(new Button(T.localized("Close"), e -> dialog.close()));
            dialog.open();
        } catch (Exception e) {
            Notification.error(messageOf(e));
        }
    }

    private static String statsText(MangaService.Request request) {
        StringBuilder stats = new StringBuilder(T.localized("About %d tokens").formatted(request.promptTokens()));
        if (request.promptLimit() > 0) {
            stats.append(" ").append(T.localized("of %d the model has room for (its context without the reply)").formatted(request.promptLimit()));
        }
        stats.append(". ").append(T.localized("Lore entries: %d").formatted(request.loreEntries()));
        if (request.skippedLore() > 0) {
            stats.append(" ").append(T.localized("(%d more did not fit)").formatted(request.skippedLore()));
        }
        return stats.toString();
    }

    private String messageOf(Throwable e) {
        return e instanceof IllegalStateException && e.getMessage() != null ? e.getMessage() : InferenceErrors.messageOf(loc, e);
    }

    // --- generation ---

    private void generateOrAbort() {
        if (activeToken != null) {
            activeToken.cancel();
            return;
        }
        UI ui = UI.getCurrent();
        boolean tags = tagsMode;
        try {
            MangaService.Request request = buildRequest();
            if (request.exceedsContext()) {
                Notification.warning(T.localized("The request (about %d tokens) is more than the model has room for (%d)").formatted(
                        request.promptTokens(), request.promptLimit()));
            } else if (request.skippedLore() > 0) {
                Notification.show(T.localized("%d lore entries were left out to fit the context").formatted(request.skippedLore()));
            }

            if (tags) {
                section.setTags("");
                section.setTagsReasoning("");
                section.setTagsReasoningMillis(0);
                section.setTagsReasoningFinished(false);
            } else {
                section.setScript("");
                section.setReasoning("");
                section.setReasoningMillis(0);
                section.setReasoningFinished(false);
            }
            generatingTags = tags;
            // the tokens and the time of the request are the book's
            try (UsageContext _ = UsageContext.open(manuscript)) {
                activeToken = service.stream(request, new Streaming(ui, tags));
            }
            onRun.accept(true);
            refreshView();
        } catch (Exception e) {
            activeToken = null;
            MangaGeneratorLog.get().warn("Failed to start the generation: {}", e.getMessage(), e);
            Notification.error(messageOf(e));
            refreshView();
        }
    }

    /**
     * Brings the answer from the inference thread into the card, not more often than every {@link #REFRESH_NANOS}.
     */
    private final class Streaming implements MangaService.StreamListener {

        private record Snapshot(String text, String reasoning, long millis, boolean finished) {
        }

        private final UI ui;
        private final boolean tags;
        private volatile Snapshot snapshot = new Snapshot("", "", 0, false);
        private volatile boolean scheduled;
        private volatile long lastRefresh;

        private Streaming(UI ui, boolean tags) {
            this.ui = ui;
            this.tags = tags;
        }

        @Override
        public void onProgress(String text, String reasoning, long reasoningMillis, boolean reasoningFinished) {
            snapshot = new Snapshot(text, reasoning, reasoningMillis, reasoningFinished);
            long now = System.nanoTime();
            if (!scheduled && now - lastRefresh > REFRESH_NANOS) {
                scheduled = true;
                lastRefresh = now;
                ui.access(() -> {
                    scheduled = false;
                    apply();
                    UIPushGuard.push(ui);
                });
            }
        }

        @Override
        public void onDone(MangaService.Outcome outcome, Throwable error) {
            ui.access(() -> {
                // thinking that was cut short is over too
                Snapshot last = snapshot;
                snapshot = new Snapshot(last.text(), last.reasoning(), last.millis(), !last.reasoning().isEmpty());
                apply();
                activeToken = null;
                onChange.run();
                onRun.accept(false);
                if (outcome == MangaService.Outcome.FAILED) {
                    MangaGeneratorLog.get().warn("Generation failed: {}", error == null ? "" : error.getMessage(), error);
                    Notification.error(InferenceErrors.messageOf(loc, error));
                }
                refreshView();
                UIPushGuard.push(ui);
            });
        }

        private void apply() {
            Snapshot s = snapshot;
            if (tags) {
                section.setTags(s.text());
                section.setTagsReasoning(s.reasoning());
                section.setTagsReasoningMillis(s.millis());
                section.setTagsReasoningFinished(s.finished());
            } else {
                section.setScript(s.text());
                section.setReasoning(s.reasoning());
                section.setReasoningMillis(s.millis());
                section.setReasoningFinished(s.finished());
            }
            refreshView();
        }
    }
}
