package com.github.enerccio.marginalia.extensions.mangagenerator.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.mangagenerator.MangaGeneratorLog;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.Manga;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaData;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaSection;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaSettings;
import com.github.enerccio.marginalia.extensions.mangagenerator.service.Defaults;
import com.github.enerccio.marginalia.extensions.mangagenerator.service.MangaService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

import static com.github.enerccio.marginalia.extensions.mangagenerator.MangaGeneratorLocalization.T;

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

    public MangaDialog(Manuscript manuscript, MangaService service) {
        this.manuscript = manuscript;
        this.service = service;

        setHeaderTitle(T.localized("Manga Generator"));
        setWidth("96vw");
        setHeight("94vh");
        setMaxWidth("96vw");
        setDraggable(true);
        setResizable(true);
        setCloseOnOutsideClick(false);

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

        Button addMangaButton = iconButton(Solid.PLUS, T.localized("New manga"), e -> addManga());
        renameButton = iconButton(Solid.PEN, T.localized("Rename the manga"), e -> renameManga());
        moveLeftButton = iconButton(Solid.ARROW_LEFT, T.localized("Move the manga to the left"), e -> moveManga(-1));
        moveRightButton = iconButton(Solid.ARROW_RIGHT, T.localized("Move the manga to the right"), e -> moveManga(1));
        deleteMangaButton = iconButton(Solid.TRASH, T.localized("Delete the manga"), e -> deleteManga());

        HorizontalLayout tabBar = new HorizontalLayout(tabs, addMangaButton, renameButton, moveLeftButton, moveRightButton, deleteMangaButton);
        tabBar.setWidthFull();
        tabBar.setAlignItems(FlexComponent.Alignment.CENTER);
        tabBar.addClassName(MangaStyles.BAR);
        tabBar.expand(tabs);

        tokenBudget = numberField(T.localized("Token budget"), 512, "9em", Manga::setTokenBudget);
        tokenBudget.setTooltipText(T.localized("How long a script the model is asked for"));
        splitCount = numberField(T.localized("Parts per section"), 1, "9em", Manga::setSplitCount);
        splitCount.setTooltipText(T.localized("How many parts of the story a new section adapts"));
        contextLeft = numberField(T.localized("Context before"), 0, "9em", Manga::setContextLeft);
        contextLeft.setTooltipText(T.localized("Parts before the adapted ones that a new section gives the model as the background"));
        contextRight = numberField(T.localized("Context after"), 0, "9em", Manga::setContextRight);
        contextRight.setTooltipText(T.localized("Parts after the adapted ones that a new section gives the model as the background"));

        Button scriptPrompt = new Button(T.localized("Script prompt"), e -> editMangaText(T.localized("Script prompt"),
                current().getPromptOverride(), Defaults.DEFAULT_PROMPT, current()::setPromptOverride));
        Button tagsPrompt = new Button(T.localized("Tags prompt"), e -> editMangaText(T.localized("Tags prompt"),
                current().getTagsPromptOverride(), Defaults.DEFAULT_TAGS_PROMPT, current()::setTagsPromptOverride));
        Button rules = new Button(T.localized("Additional rules"), e -> editMangaText(T.localized("Additional rules"),
                current().getAdditionalRules(), "", current()::setAdditionalRules));
        rules.setTooltipText(T.localized("Put in place of {{additionalRules}} in the script prompt"));
        copyAllButton = new Button(T.localized("Copy all"), Solid.COPY.create(), e -> copyAll());
        addSectionButton = new Button(T.localized("Add section"), Solid.PLUS.create(), e -> addSection());
        addSectionButton.setThemeName("primary");

        modelPicker = new ModelProtocolPicker();
        initModelPicker();

        settingsBar = new HorizontalLayout(modelPicker, tokenBudget, splitCount, contextLeft, contextRight, scriptPrompt, tagsPrompt, rules,
                copyAllButton, addSectionButton);
        settingsBar.setWidthFull();
        settingsBar.setWrap(true);
        settingsBar.setAlignItems(FlexComponent.Alignment.END);
        settingsBar.addClassName(MangaStyles.BAR);

        emptyHint = new Span(T.localized("There is no manga in this book yet. Create one with the plus."));
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

        getFooter().add(new Button(T.localized("Close"), e -> close()));
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
            noSections = new Span(T.localized("No sections yet. Add one to start with the first parts of the story."));
            noSections.addClassName(MangaStyles.EMPTY);
            sectionsList.add(noSections);
        }
        if (keepScroll) {
            sectionsPanel.getElement().executeJs(RESTORE_SCROLL, sectionsPanel.getElement(), removed);
        }
    }

    private void addCard(Manga manga, List<MangaSection> sections, int index) {
        MangaSection section = sections.get(index);
        MangaSectionCard card = new MangaSectionCard(service, manuscript, manga, section, index + 1,
                () -> {
                    int position = sections.indexOf(section);
                    return position > 0 ? sections.get(position - 1) : null;
                },
                this::save, this::runChanged, () -> deleteSection(section));
        card.setLocked(running > 0);
        cards.add(card);
        sectionsList.add(card);
    }

    /**
     * Mangas and sections cannot be changed while a model is writing into one: the cards are rebuilt from the data.
     */
    private void runChanged(boolean started) {
        running += started ? 1 : -1;
        cards.forEach(card -> card.setLocked(running > 0));
        updateStructureControls();
    }

    private void updateStructureControls() {
        boolean free = running == 0;
        boolean has = current() != null;
        int index = data.getActiveManga();
        tabs.setEnabled(free);
        renameButton.setEnabled(free && has);
        deleteMangaButton.setEnabled(free && has);
        moveLeftButton.setEnabled(free && has && index > 0);
        moveRightButton.setEnabled(free && has && index < data.getMangas().size() - 1);
        addSectionButton.setEnabled(free);
        copyAllButton.setEnabled(has);
    }

    private void addManga() {
        if (running > 0) {
            return;
        }
        Manga manga = new Manga(T.localized("Manga Script %d").formatted(data.getMangas().size() + 1), service.getSettings());
        data.getMangas().add(manga);
        data.setActiveManga(data.getMangas().size() - 1);
        save();
        refreshTabs();
    }

    private void renameManga() {
        Manga manga = current();
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(T.localized("Rename the manga"));
        TextField name = new TextField(T.localized("Title"));
        name.setWidthFull();
        name.setValue(manga.getTitle());
        DialogFocus.onOpen(dialog, name);
        Button save = new Button(T.localized("Save"), e -> {
            if (StringUtils.isNotBlank(name.getValue())) {
                manga.setTitle(name.getValue().strip());
                save();
                refreshTabs();
            }
            dialog.close();
        });
        save.setThemeName("primary");
        dialog.add(name);
        dialog.getFooter().add(new Button(T.localized("Cancel"), e -> dialog.close()), save);
        dialog.open();
    }

    private void moveManga(int by) {
        int from = data.getActiveManga();
        int to = from + by;
        if (running > 0 || from < 0 || to < 0 || to >= data.getMangas().size()) {
            return;
        }
        data.getMangas().add(to, data.getMangas().remove(from));
        data.setActiveManga(to);
        save();
        refreshTabs();
    }

    private void deleteManga() {
        Manga manga = current();
        ConfirmDialog.show(T.localized("Delete the manga \"%s\"? This cannot be undone.").formatted(manga.getTitle()), () -> {
            int index = data.getActiveManga();
            data.getMangas().remove(manga);
            data.setActiveManga(Math.min(index, data.getMangas().size() - 1));
            save();
            refreshTabs();
        });
    }

    private void editMangaText(String title, String currentText, String inherited, Consumer<String> setter) {
        new TextEditDialog(title, T.localized("Empty or unchanged: the built-in text is used"), currentText, inherited, value -> {
            setter.accept(value);
            save();
        }).open();
    }

    // --- the sections ---

    /**
     * Adds a section for the next parts of the story: the ones after the last section, as many as the manga says.
     */
    private void addSection() {
        Manga manga = current();
        if (manga == null || running > 0) {
            return;
        }
        try {
            int parts = service.branch(manuscript).size();
            if (parts == 0) {
                Notification.warning(T.localized("The story has no parts yet"));
                return;
            }
            List<MangaSection> sections = manga.getSections();
            int start = sections.isEmpty() ? 1 : sections.getLast().getEndPart() + 1;
            if (start > parts) {
                Notification.warning(T.localized("No new parts of the story for another section (the story has %d parts)").formatted(parts));
                return;
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
            addCard(manga, sections, sections.size() - 1);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void deleteSection(MangaSection section) {
        if (running > 0) {
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
            Notification.warning(T.localized("There is no script or tags to copy"));
            return;
        }
        UI.getCurrent().getPage().executeJs("navigator.clipboard.writeText($0)", all);
        Notification.show(T.localized("All scripts and tags copied"));
    }
}
