package com.github.enerccio.marginalia.extensions.mangagenerator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.mangagenerator.MangaGeneratorExtension;
import com.github.enerccio.marginalia.extensions.mangagenerator.MangaGeneratorLog;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.Manga;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaData;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaSection;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaSettings;
import com.github.enerccio.marginalia.extensions.mangagenerator.service.MangaService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.enerccio.marginalia.extensions.mangagenerator.MangaGeneratorLocalization.*;

/**
 * The window of the manga generator, opened from the settings menu of the story: the mangas of the book as tabs, the settings of the
 * open one, and its sections, each with the script and the image tags of the parts of the story it adapts.
 */
@Configurable(preConstruction = true)
public class MangaDialog extends Dialog {

    @Autowired
    private Localization loc;

    private final MangaService service;
    private final Manuscript manuscript;
    private MangaData data;

    private Tabs tabs;
    private Button renameButton;
    private Button moveLeftButton;
    private Button moveRightButton;
    private Button deleteMangaButton;
    private HorizontalLayout settingsBar;
    private IntegerField tokenBudget;
    private IntegerField splitCount;
    private IntegerField contextLeft;
    private IntegerField contextRight;
    private Button addSectionButton;
    private Button continuousButton;
    /** What the continuous mode generates: true the tags of the scripts that are there, false the scripts (and new sections). */
    private Select<Boolean> continuousWhat;
    private Button copyAllButton;
    private Span emptyHint;
    /** The note that stands in the place of the sections while there are none, null otherwise. */
    private Span noSections;
    private VerticalLayout sectionsList;
    private ScrollPanel sectionsPanel;
    private ModelProtocolPicker modelPicker;

    /**
     * Remembers where the sections are scrolled: the panel and everything inside a card that scrolls (the box of the script, the
     * editor), the inner ones by the number of the card and the path of child indexes to them. Put on the panel once.
     */
    private static final String REMEMBER_SCROLL = """
            const root = $0;
            if (root._scrollMemory) return;
            const memory = {top: 0, inner: {}};
            const path = (card, node) => {
              const steps = [];
              for (let n = node; n !== card; n = n.parentElement) {
                steps.unshift(Array.prototype.indexOf.call(n.parentElement.children, n));
              }
              return steps.join('.');
            };
            root.addEventListener('scroll', e => {
              const target = e.target;
              if (target === root) {
                memory.top = root.scrollTop;
                return;
              }
              const card = target.closest && target.closest('.manga-card');
              if (!card || !root.contains(card)) return;
              const cards = Array.from(root.querySelectorAll('.manga-card'));
              memory.inner[cards.indexOf(card) + ':' + path(card, target)] = target.scrollTop;
            }, {capture: true, passive: true});
            root._scrollMemory = memory;
            """;

    /**
     * Scrolls the panel and the insides of the cards back to where they were before the sections were built again ($1 is the
     * number of the card that was removed, -1 when none: the cards after it moved up).
     */
    private static final String RESTORE_SCROLL = """
            const root = $0;
            const removed = $1;
            if (!root._scrollMemory) return;
            const saved = JSON.parse(JSON.stringify(root._scrollMemory));
            const apply = () => {
              root.scrollTop = saved.top;
              const cards = Array.from(root.querySelectorAll('.manga-card'));
              for (const [key, top] of Object.entries(saved.inner)) {
                const split = key.indexOf(':');
                let index = parseInt(key.substring(0, split), 10);
                if (index === removed) continue;
                if (removed >= 0 && index > removed) index--;
                let node = cards[index];
                for (const step of key.substring(split + 1).split('.').filter(part => part !== '')) {
                  node = node && node.children[parseInt(step, 10)];
                }
                if (node) node.scrollTop = top;
              }
            };
            apply();
            requestAnimationFrame(() => requestAnimationFrame(apply));
            setTimeout(apply, 300);
            """;

    private final List<MangaSectionCard> cards = new ArrayList<>();
    /** Set while the widgets are filled in from the data, so that this does not count as the user's change. */
    private boolean binding;
    private int running;
    // not modal: closing it while a model writes folds it into the corner, the generation goes on
    private final FloatingWindow floating;
    /** The continuous mode is on: sections are added and generated one after the other until the story runs out or the user stops it. */
    private boolean continuous;

    public MangaDialog(Manuscript manuscript, MangaService service) {
        this.manuscript = manuscript;
        this.service = service;

        setHeaderTitle(T.localized(MANGA_GENERATOR));
        setWidth("96vw");
        setHeight("94vh");
        setMaxWidth("96vw");
        setCloseOnOutsideClick(false);
        // Esc would fold a running window by accident
        setCloseOnEsc(false);
        // closed for good (also from the corner, or with the book), it stops what is generated
        floating = new FloatingWindow(this, "manga-generator",
                () -> T.localized(MANGA_GENERATOR) + " \u00b7 " + manuscript.getName(), () -> stopContinuous(null)).book(manuscript);

        try {
            data = service.loadData(manuscript);
        } catch (Exception e) {
            MangaGeneratorLog.get().error("Failed to read the data of the book: {}", e.getMessage(), e);
            UIUtils.internalServerError(loc, e);
            data = new MangaData();
        }

        buildUI();
        refreshTabs();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // the sections cancel what they generate when the window is closed; folded into the corner, it goes on
        if (!floating.isMinimised()) {
            continuous = false;
        }
    }

    // --- building ---

    private void buildUI() {
        tabs = new Tabs();
        tabs.setWidthFull();
        tabs.addSelectedChangeListener(e -> {
            if (!binding) {
                data.setActiveManga(tabs.getSelectedIndex());
                save();
                showManga();
            }
        });

        Button addMangaButton = iconButton(Solid.PLUS, T.localized(NEW_MANGA), e -> addManga());
        renameButton = iconButton(Solid.PEN, T.localized(RENAME_THE_MANGA), e -> renameManga());
        moveLeftButton = iconButton(Solid.ARROW_LEFT, T.localized(MOVE_THE_MANGA), e -> moveManga(-1));
        moveRightButton = iconButton(Solid.ARROW_RIGHT, T.localized(MOVE_THE_MANGA_TO_THE_RIGHT), e -> moveManga(1));
        deleteMangaButton = iconButton(Solid.TRASH, T.localized(DELETE_THE_MANGA), e -> deleteManga());

        HorizontalLayout tabBar = new HorizontalLayout(tabs, addMangaButton, renameButton, moveLeftButton, moveRightButton, deleteMangaButton);
        tabBar.setWidthFull();
        tabBar.setAlignItems(FlexComponent.Alignment.CENTER);
        tabBar.addClassName(MangaStyles.BAR);
        tabBar.expand(tabs);

        tokenBudget = numberField(T.localized(TOKEN_BUDGET), 512, "9em", Manga::setTokenBudget);
        tokenBudget.setTooltipText(T.localized(HOW_LONG_A_SCRIPT));
        splitCount = numberField(T.localized(PARTS_PER_SECTION), 1, "9em", Manga::setSplitCount);
        splitCount.setTooltipText(T.localized(HOW_MANY_PARTS));
        contextLeft = numberField(T.localized(CONTEXT_BEFORE), 0, "9em", Manga::setContextLeft);
        contextLeft.setTooltipText(T.localized(PARTS_BEFORE_THE_ADAPTED_ONES));
        contextRight = numberField(T.localized(CONTEXT_AFTER), 0, "9em", Manga::setContextRight);
        contextRight.setTooltipText(T.localized(PARTS_AFTER_THE_ADAPTED_ONES));

        Button scriptPrompt = new Button(T.localized(SCRIPT_PROMPT), e -> editMangaText(T.localized(SCRIPT_PROMPT),
                current().getPromptOverride(), T.defaults().prompt(), current()::setPromptOverride));
        Button tagsPrompt = new Button(T.localized(TAGS_PROMPT), e -> editMangaText(T.localized(TAGS_PROMPT),
                current().getTagsPromptOverride(), T.defaults().tagsPrompt(), current()::setTagsPromptOverride));
        Button rules = new Button(T.localized(ADDITIONAL_RULES), e -> editMangaText(T.localized(ADDITIONAL_RULES),
                current().getAdditionalRules(), "", current()::setAdditionalRules));
        rules.setTooltipText(T.localized(PUT_IN_PLACE_OF_ADDITIONALRULES));
        copyAllButton = new Button(T.localized(COPY_ALL), Solid.COPY.create(), e -> copyAll());
        addSectionButton = new Button(T.localized(ADD_SECTION), Solid.PLUS.create(), e -> addSection());
        addSectionButton.setThemeName("primary");
        continuousWhat = new Select<>();
        continuousWhat.setItems(false, true);
        continuousWhat.setItemLabelGenerator(tags -> tags ? T.localized(TAGS_ONLY) : T.localized(SCRIPTS_ONLY));
        continuousWhat.setValue(false);
        continuousWhat.addValueChangeListener(e -> updateContinuousButton());
        continuousWhat.setWidth("10em");
        continuousWhat.setTooltipText(T.localized(WHAT_THE_CONTINUOUS_MODE_GENERATES));
        continuousButton = new Button(Solid.FORWARD.create(), e -> toggleContinuous());
        continuousButton.setThemeName("primary");
        updateContinuousButton();

        modelPicker = new ModelProtocolPicker();
        initModelPicker();

        settingsBar = new HorizontalLayout(modelPicker, tokenBudget, splitCount, contextLeft, contextRight, scriptPrompt, tagsPrompt, rules,
                copyAllButton, addSectionButton, continuousWhat, continuousButton);
        settingsBar.setWidthFull();
        settingsBar.setWrap(true);
        settingsBar.setAlignItems(FlexComponent.Alignment.END);
        settingsBar.addClassName(MangaStyles.BAR);

        emptyHint = new Span(T.localized(THERE_IS_NO_MANGA));
        emptyHint.addClassName(MangaStyles.EMPTY);

        sectionsList = new VerticalLayout();
        sectionsList.setWidthFull();
        sectionsList.setPadding(true);
        sectionsList.setSpacing(true);
        sectionsPanel = new ScrollPanel();
        sectionsPanel.setWidthFull();
        sectionsPanel.addClassName(CssStyles.MIN_HEIGHT_0);
        sectionsPanel.add(sectionsList);
        sectionsPanel.getElement().executeJs(REMEMBER_SCROLL, sectionsPanel.getElement());

        VerticalLayout content = new VerticalLayout(tabBar, settingsBar, emptyHint, sectionsPanel);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);
        content.setFlexGrow(1, sectionsPanel);
        add(content);

        getFooter().add(new Button(T.localized(CLOSE), e -> close()));
    }

    private static Button iconButton(Solid icon, String tooltip, com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>> listener) {
        Button button = new Button(icon.create(), listener);
        button.setThemeName("tertiary icon small");
        button.setTooltipText(tooltip);
        return button;
    }

    private IntegerField numberField(String label, int min, String width, java.util.function.BiConsumer<Manga, Integer> setter) {
        IntegerField field = new IntegerField(label);
        field.setMin(min);
        field.setStepButtonsVisible(true);
        field.setWidth(width);
        field.addValueChangeListener(e -> {
            if (!binding && current() != null && e.getValue() != null && e.getValue() >= min) {
                setter.accept(current(), e.getValue());
                save();
            }
        });
        return field;
    }

    /**
     * The model of the requests: the one chosen last, else the one of the book. A choice is kept in the settings of the user.
     */
    private void initModelPicker() {
        try {
            MangaSettings settings = service.getSettings();
            modelPicker.setSelection(service.resolveAI(settings, manuscript), service.resolveProtocol(settings, manuscript));
            modelPicker.addSelectionListener(() -> {
                try {
                    MangaSettings changed = service.getSettings();
                    changed.setSelectedAiId(modelPicker.getAi() == null ? null : modelPicker.getAi().getId());
                    changed.setSelectedProtocolId(modelPicker.getProtocol() == null ? null : modelPicker.getProtocol().getId());
                    service.saveSettings(changed);
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // --- the mangas ---

    private Manga current() {
        return data.getCurrent();
    }

    private void save() {
        try {
            service.saveData(manuscript, data);
        } catch (Exception e) {
            MangaGeneratorLog.get().error("Failed to save the data of the book: {}", e.getMessage(), e);
            UIUtils.internalServerError(loc, e);
        }
    }

    private void refreshTabs() {
        binding = true;
        try {
            tabs.removeAll();
            for (Manga manga : data.getMangas()) {
                tabs.add(new Tab(manga.getTitle()));
            }
            if (data.getCurrent() == null && !data.getMangas().isEmpty()) {
                data.setActiveManga(0);
            } else if (data.getMangas().isEmpty()) {
                data.setActiveManga(-1);
            }
            tabs.setSelectedIndex(data.getActiveManga());
        } finally {
            binding = false;
        }
        showManga();
    }

    private void showManga() {
        showManga(false, -1);
    }

    /**
     * Builds the sections again.
     *
     * @param keepScroll scroll the panel and the insides of the cards back to where they were (the sections of the same manga)
     * @param removed    the number of the card that is not there any more, -1 when none
     */
    private void showManga(boolean keepScroll, int removed) {
        Manga manga = current();
        boolean has = manga != null;
        emptyHint.setVisible(!has);
        settingsBar.setVisible(has);
        sectionsPanel.setVisible(has);
        renameButton.setEnabled(has);
        deleteMangaButton.setEnabled(has);
        updateStructureControls();

        sectionsList.removeAll();
        noSections = null;
        cards.clear();
        if (!has) {
            return;
        }
        binding = true;
        try {
            tokenBudget.setValue(manga.getTokenBudget());
            splitCount.setValue(manga.getSplitCount());
            contextLeft.setValue(manga.getContextLeft());
            contextRight.setValue(manga.getContextRight());
        } finally {
            binding = false;
        }
        List<MangaSection> sections = manga.getSections();
        for (int i = 0; i < sections.size(); i++) {
            addCard(manga, sections, i);
        }
        if (sections.isEmpty()) {
            noSections = new Span(T.localized(NO_SECTIONS_YET_ADD_ONE));
            noSections.addClassName(MangaStyles.EMPTY);
            sectionsList.add(noSections);
        }
        if (keepScroll) {
            sectionsPanel.getElement().executeJs(RESTORE_SCROLL, sectionsPanel.getElement(), removed);
        }
    }

    private MangaSectionCard addCard(Manga manga, List<MangaSection> sections, int index) {
        MangaSection section = sections.get(index);
        MangaSectionCard card = new MangaSectionCard(service, manuscript, manga, section, index + 1,
                () -> {
                    int position = sections.indexOf(section);
                    return position > 0 ? sections.get(position - 1) : null;
                },
                this::save, this::runChanged, () -> deleteSection(section));
        card.setWindow(floating);
        card.setLocked(busy());
        card.setFrozen(continuous);
        cards.add(card);
        sectionsList.add(card);
        return card;
    }

    /**
     * Mangas and sections cannot be changed while a model is writing into one: the cards are rebuilt from the data.
     */
    private void runChanged(boolean started) {
        running += started ? 1 : -1;
        lockCards();
        updateStructureControls();
    }

    private void lockCards() {
        floating.setRunning(busy());
        cards.forEach(card -> {
            card.setWindow(floating);
        card.setLocked(busy());
            card.setFrozen(continuous);
        });
    }

    /**
     * Whether a model is writing into a section, or the continuous mode is on (and is about to start the next one).
     */
    private boolean busy() {
        return running > 0 || continuous;
    }

    private void updateStructureControls() {
        boolean free = !busy();
        boolean has = current() != null;
        int index = data.getActiveManga();
        tabs.setEnabled(free);
        renameButton.setEnabled(free && has);
        deleteMangaButton.setEnabled(free && has);
        moveLeftButton.setEnabled(free && has && index > 0);
        moveRightButton.setEnabled(free && has && index < data.getMangas().size() - 1);
        addSectionButton.setEnabled(free);
        copyAllButton.setEnabled(has);
        continuousButton.setEnabled(has && (continuous || running == 0));
        continuousWhat.setEnabled(has && !busy());
    }

    private void addManga() {
        if (busy()) {
            return;
        }
        Manga manga = new Manga(T.localized(MANGA_SCRIPT).formatted(data.getMangas().size() + 1), service.getSettings());
        data.getMangas().add(manga);
        data.setActiveManga(data.getMangas().size() - 1);
        save();
        refreshTabs();
    }

    private void renameManga() {
        Manga manga = current();
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized(RENAME_THE_MANGA));
        TextField name = new TextField(T.localized(TITLE));
        name.setWidthFull();
        name.setValue(manga.getTitle());
        DialogFocus.onOpen(dialog, name);
        Button save = new Button(T.localized(SAVE), e -> {
            if (StringUtils.isNotBlank(name.getValue())) {
                manga.setTitle(name.getValue().strip());
                save();
                refreshTabs();
            }
            dialog.close();
        });
        save.setThemeName("primary");
        dialog.add(name);
        dialog.getFooter().add(new Button(T.localized(CANCEL), e -> dialog.close()), save);
        DialogShortcuts.submitWith(save, name);
        dialog.open();
    }

    private void moveManga(int by) {
        int from = data.getActiveManga();
        int to = from + by;
        if (busy() || from < 0 || to < 0 || to >= data.getMangas().size()) {
            return;
        }
        data.getMangas().add(to, data.getMangas().remove(from));
        data.setActiveManga(to);
        save();
        refreshTabs();
    }

    private void deleteManga() {
        Manga manga = current();
        ConfirmDialog.show(T.localized(DELETE_THE_MANGA_THIS_CANNOT).formatted(manga.getTitle()), () -> {
            int index = data.getActiveManga();
            data.getMangas().remove(manga);
            data.setActiveManga(Math.min(index, data.getMangas().size() - 1));
            save();
            refreshTabs();
        });
    }

    private void editMangaText(String title, String currentText, String inherited, Consumer<String> setter) {
        new TextEditDialog(title, T.localized(EMPTY_OR_UNCHANGED_THE_BUILT), currentText, inherited, value -> {
            setter.accept(value);
            save();
        }).open();
    }

    // --- the sections ---

    /**
     * Adds a section for the next parts of the story: the ones after the last section, as many as the manga says.
     */
    private void addSection() {
        if (current() == null || busy()) {
            return;
        }
        addNextSection(false);
    }

    /**
     * @param quiet do not tell the user that the story has no part left for a section (the caller does)
     * @return the card of the new section, null when it was not added
     */
    private MangaSectionCard addNextSection(boolean quiet) {
        Manga manga = current();
        try {
            int parts = service.branch(manuscript).size();
            if (parts == 0) {
                if (!quiet) {
                    Notification.warning(T.localized(THE_STORY_HAS_NO_PARTS));
                }
                return null;
            }
            List<MangaSection> sections = manga.getSections();
            int start = sections.isEmpty() ? 1 : sections.getLast().getEndPart() + 1;
            if (start > parts) {
                if (!quiet) {
                    Notification.warning(T.localized(NO_NEW_PARTS).formatted(parts));
                }
                return null;
            }
            int end = Math.min(parts, start + Math.max(1, manga.getSplitCount()) - 1);

            MangaSection section = new MangaSection();
            section.setStartPart(start);
            section.setEndPart(end);
            section.setContextStart(Math.max(1, start - manga.getContextLeft()));
            section.setContextEnd(Math.min(parts, end + manga.getContextRight()));
            sections.add(section);
            save();
            // only the new card is added: the sections that are there keep what they show and where they are scrolled
            if (noSections != null) {
                sectionsList.remove(noSections);
                noSections = null;
            }
            return addCard(manga, sections, sections.size() - 1);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return null;
        }
    }

    // --- the continuous mode ---

    private void toggleContinuous() {
        if (continuous) {
            stopContinuous(null);
            return;
        }
        if (current() == null || running > 0) {
            return;
        }
        continuous = true;
        lockCards();
        updateStructureControls();
        updateContinuousButton();
        continueWork();
    }

    /**
     * Turns the continuous mode off and stops what is being generated; what came in so far is kept.
     *
     * @param message what to tell the user, null for nothing
     */
    private void stopContinuous(String message) {
        boolean wasOn = continuous;
        continuous = false;
        cards.forEach(MangaSectionCard::abort);
        lockCards();
        updateStructureControls();
        updateContinuousButton();
        if (wasOn && message != null) {
            Notification.show(message);
        }
    }

    private void updateContinuousButton() {
        continuousButton.setText(continuous ? T.localized(STOP_CONTINUOUS_MODE) : T.localized(CONTINUOUS_MODE));
        continuousButton.setIcon((continuous ? Solid.STOP : Solid.FORWARD).create());
        continuousButton.setTooltipText(continuousWhat.getValue()
                ? T.localized(GENERATE_THE_TAGS)
                : T.localized(GENERATE_THE_SCRIPT));
    }

    /**
     * The next step of the continuous mode. Scripts: the script of the first section that has none, else a new section for the next
     * parts of the story, until there is no part of the story left. Tags: the tags of the first section that has a script but no tags,
     * until there is none. Every step calls this again when its answer is over.
     */
    private void continueWork() {
        boolean tags = continuousWhat.getValue();
        while (continuous) {
            for (MangaSectionCard card : cards) {
                if (tags ? card.hasText(false) && !card.hasText(true) : !card.hasText(false)) {
                    generateStep(card, tags);
                    return;
                }
            }
            if (tags) {
                continuousOver(false);
                stopContinuous(T.localized(CONTINUOUS_MODE_IS_FINISHED));
                return;
            }
            MangaSectionCard added = addNextSection(true);
            if (added == null) {
                continuousOver(false);
                stopContinuous(T.localized(CONTINUOUS_MODE_IS_FINISHED_THERE));
                return;
            }
        }
    }

    /**
     * The continuous mode ended by itself (it ran out of work, or a step failed; the user stopping it is silent): tells the user, the
     * automatic run of the source of what it wrote.
     */
    private void continuousOver(boolean failed) {
        DesktopNotifier notifier = new DesktopNotifier(UI.getCurrent());
        String source = continuousWhat.getValue() ? MangaGeneratorExtension.NOTIFY_TAGS : MangaGeneratorExtension.NOTIFY_SCRIPT;
        if (failed) {
            notifier.failed(source, true, manuscript.getName());
        } else {
            notifier.finished(source, true, manuscript.getName());
        }
    }

    private void generateStep(MangaSectionCard card, boolean tags) {
        card.scrollIntoView();
        boolean started = card.generate(tags, outcome -> {
            if (outcome != MangaService.Outcome.COMPLETED) {
                if (outcome == MangaService.Outcome.FAILED) {
                    continuousOver(true);
                }
                stopContinuous(T.localized(CONTINUOUS_MODE_STOPPED));
            } else if (!card.hasText(tags)) {
                // an empty answer would be asked for again and again
                stopContinuous(T.localized(CONTINUOUS_MODE_STOPPED_THE_MODEL));
            } else {
                continueWork();
            }
        });
        if (!started) {
            stopContinuous(T.localized(CONTINUOUS_MODE_STOPPED));
        }
    }

    private void deleteSection(MangaSection section) {
        if (busy()) {
            return;
        }
        int index = current().getSections().indexOf(section);
        current().getSections().remove(section);
        save();
        showManga(true, index);
    }

    private void copyAll() {
        Manga manga = current();
        String scripts = manga.getSections().stream().map(MangaSection::getScript).filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n\n"));
        String tags = manga.getSections().stream().map(MangaSection::getTags).filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n\n"));
        String all = scripts + (scripts.isEmpty() || tags.isEmpty() ? "" : "\n\n") + tags;
        if (all.isBlank()) {
            Notification.warning(T.localized(THERE_IS_NO_SCRIPT));
            return;
        }
        UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", all);
        Notification.show(T.localized(ALL_SCRIPTS_AND_TAGS_COPIED));
    }
}
