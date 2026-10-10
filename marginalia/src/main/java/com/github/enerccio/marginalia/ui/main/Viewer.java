package com.github.enerccio.marginalia.ui.main;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.SharedStyles;
import com.github.enerccio.marginalia.bound.ApplicationPoint;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.domain.service.search.Sorter.Ordering;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.ui.components.MessageImages;
import com.github.enerccio.marginalia.ui.widgets.BackendTableItem;
import com.github.enerccio.marginalia.ui.widgets.BackendTableProviderBase;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Route("view")
@Configurable(preConstruction = true)
public class Viewer extends LoginCheckRoute implements HasUrlParameter<String> {

    private static final Logger log = LoggerFactory.getLogger(Viewer.class);

    @Autowired
    private User user;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private ApplicationPoint applicationPoint;

    private String manuscriptParam;
    private boolean isUserLoggedIn = false;

    private Grid<ManuscriptWrapper> grid;
    private TextField nameFilter;
    private MultiSelectComboBox<Tag> tagFilter;

    private String currentSortField = "lastOpened";
    private Ordering currentSortOrdering = Ordering.DESC;
    private final ManuscriptFilterValues filterValues = new ManuscriptFilterValues();

    public Viewer() {
        showLogin();
    }

    @Override
    protected String getAppTitle() {
        return loc.getValue(L.LABEL_VIEWER_TITLE);
    }

    @Override
    protected String getAppDescription() {
        return loc.getValue(L.LABEL_VIEWER_DESC);
    }

    @Override
    protected boolean authenticate(String userName, String password) throws Exception {
        return userService.authenticate(userName, password, clientAddress());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {

    }

    @Override
    protected void proceedWithLogin(String username) {
        try {
            User u = userService.findByName(username);

            user.setId(u.getId());
            user.setLogin(u.getLogin());
            user.setFullName(u.getFullName());

            isUserLoggedIn = true;
            sessionManager.userLoggedIn(user, VaadinSession.getCurrent());
            User referenceCopy = new User();
            referenceCopy.setId(u.getId());
            referenceCopy.setLogin(u.getLogin());
            referenceCopy.setFullName(u.getFullName());
            applicationPoint.register(UI.getCurrent(), null, referenceCopy);
            renderContent();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        this.manuscriptParam = parameter;
        if (isUserLoggedIn) {
            renderContent();
        }
    }

    private void renderContent() {
        removeAll();

        if (StringUtils.isBlank(manuscriptParam)) {
            renderGridView();
        } else {
            renderManuscriptReadingView(manuscriptParam);
        }
    }

    private void renderGridView() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span titleSpan = new Span(loc.getValue(L.LABEL_BOOKS));
        titleSpan.getStyle().set("font-size", "var(--lumo-font-size-l)");
        titleSpan.getStyle().set("font-weight", "bold");

        headerLayout.add(titleSpan);

        // Top Filters Layout (stacked nicely for mobile screens)
        VerticalLayout filtersLayout = new VerticalLayout();
        filtersLayout.setWidthFull();
        filtersLayout.setPadding(false);
        filtersLayout.setSpacing(true);

        nameFilter = new TextField();
        nameFilter.setPlaceholder(loc.getValue(L.LABEL_NAME));
        nameFilter.setWidthFull();
        nameFilter.setClearButtonVisible(true);
        nameFilter.setValueChangeMode(ValueChangeMode.TIMEOUT);
        nameFilter.setValueChangeTimeout(200);
        nameFilter.addValueChangeListener(event -> {
            filterValues.setName(event.getValue());
            refreshGrid();
        });

        tagFilter = new MultiSelectComboBox<>();
        tagFilter.setPlaceholder(loc.getValue(L.LABEL_TAGS));
        tagFilter.setWidthFull();
        tagFilter.setItemLabelGenerator(Tag::getValue);
        tagFilter.setClearButtonVisible(true);
        tagFilter.setItems(query -> {
            try {
                String filter = query.getFilter().orElse("");
                return tagService.searchTagsForUser(filter, query.getOffset(), query.getLimit()).stream();
            } catch (Exception e) {
                log.error("Failed to fetch tags for filter: {}", query.getFilter().orElse(""), e);
                return Stream.empty();
            }
        });
        tagFilter.addValueChangeListener(event -> {
            filterValues.setTags(event.getValue().stream().map(Tag::getValue).sorted().toList());
            refreshGrid();
        });

        // Sort buttons
        HorizontalLayout sortButtonsLayout = new HorizontalLayout();
        sortButtonsLayout.setWidthFull();
        sortButtonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Button sortDateBtn = new Button(loc.getValue(L.LABEL_LAST_OPENED), Solid.SORT_AMOUNT_DOWN.create());
        sortDateBtn.setThemeName("small primary");

        Button sortNameBtn = new Button(loc.getValue(L.LABEL_NAME), Solid.SORT_ALPHA_DOWN.create());
        sortNameBtn.setThemeName("small tertiary");

        sortDateBtn.addClickListener(e -> {
            if ("lastOpened".equals(currentSortField)) {
                currentSortOrdering = (currentSortOrdering == Ordering.DESC) ? Ordering.ASC : Ordering.DESC;
            } else {
                currentSortField = "lastOpened";
                currentSortOrdering = Ordering.DESC;
            }
            sortDateBtn.setThemeName("small primary");
            sortNameBtn.setThemeName("small tertiary");
            refreshGrid();
        });

        sortNameBtn.addClickListener(e -> {
            if ("name".equals(currentSortField)) {
                currentSortOrdering = (currentSortOrdering == Ordering.ASC) ? Ordering.DESC : Ordering.ASC;
            } else {
                currentSortField = "name";
                currentSortOrdering = Ordering.ASC;
            }
            sortNameBtn.setThemeName("small primary");
            sortDateBtn.setThemeName("small tertiary");
            refreshGrid();
        });

        sortButtonsLayout.add(sortDateBtn, sortNameBtn);

        filtersLayout.add(nameFilter, tagFilter, sortButtonsLayout);

        // Single Column Grid
        grid = new Grid<>();
        grid.setSizeFull();

        grid.addComponentColumn(wrapper -> {
            VerticalLayout itemLayout = new VerticalLayout();
            itemLayout.setPadding(false);
            itemLayout.setSpacing(false);
            itemLayout.setWidthFull();

            Span nameSpan = new Span(wrapper.getName());
            nameSpan.getStyle().set("font-weight", "bold");
            nameSpan.getStyle().set("font-size", "var(--lumo-font-size-m)");

            HorizontalLayout subRow = new HorizontalLayout();
            subRow.setWidthFull();
            subRow.setSpacing(true);
            subRow.getStyle().set("font-size", "var(--lumo-font-size-xs)");
            subRow.getStyle().set("color", "var(--lumo-secondary-text-color)");
            subRow.getStyle().set("margin-top", "2px");

            String tagsText = wrapper.getTags();
            if (StringUtils.isNotBlank(tagsText)) {
                Span tagsSpan = new Span(tagsText);
                subRow.add(tagsSpan);
            }

            String modText = wrapper.getLastOpened();
            if (StringUtils.isNotBlank(modText)) {
                Span modSpan = new Span(modText);
                modSpan.getStyle().set("margin-left", "auto");
                subRow.add(modSpan);
            }

            itemLayout.add(nameSpan, subRow);
            return itemLayout;
        }).setFlexGrow(1);

        grid.addItemClickListener(event -> {
            ManuscriptWrapper wrapper = event.getItem();
            if (wrapper != null && wrapper.getManuscript() != null) {
                Manuscript manuscript = wrapper.getManuscript();
                try {
                    manuscriptService.markOpened(manuscript);
                } catch (Exception e) {
                    log.warn("Failed to mark manuscript {} as opened", manuscript.getUuid(), e);
                }
                UI.getCurrent().navigate(Viewer.class, manuscript.getUuid());
            }
        });

        mainLayout.add(headerLayout, filtersLayout, grid);
        mainLayout.setFlexGrow(1, grid);

        refreshGrid();
        add(mainLayout);
    }

    private void refreshGrid() {
        if (grid == null) {
            return;
        }
        try {
            List<Sorter> sorters = new ArrayList<>();
            sorters.add(Sorter.sorter(currentSortField, currentSortOrdering));
            if ("lastOpened".equals(currentSortField)) {
                // never opened books have no lastOpened, order them by modification
                sorters.add(Sorter.sorter("modification", currentSortOrdering));
            }

            List<Long> ids = manuscriptService.searchManuscripts(filterValues, sorters.toArray(Sorter[]::new));
            grid.setItems(new ManuscriptBackendProvider(ids));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void renderManuscriptReadingView(String param) {
        Manuscript manuscript = findManuscript(param);

        if (manuscript == null) {
            VerticalLayout notFoundLayout = new VerticalLayout();
            notFoundLayout.setSizeFull();
            notFoundLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
            notFoundLayout.setAlignItems(FlexComponent.Alignment.CENTER);

            Span errorSpan = new Span(loc.getValue(L.MSG_MANUSCRIPT_NOT_FOUND));
            Button backButton = new Button(Solid.ARROW_LEFT.create(), event -> UI.getCurrent().navigate(Viewer.class));
            backButton.setThemeName("primary");

            notFoundLayout.add(errorSpan, backButton);
            add(notFoundLayout);
            return;
        }

        VerticalLayout container = new VerticalLayout();
        container.setSizeFull();
        container.setPadding(false);
        container.setSpacing(false);
        container.getStyle().set("position", "relative");

        // Always apply reading style
        container.addClassName(SharedStyles.MARKDOWN_MANUSCRIPT_STYLES);
        // lang drives the browser's hyphenation dictionary (see hyphens in shared-styles.css)
        container.getElement().setAttribute("lang", manuscript.getLanguage().getCode());

        UI.getCurrent().getPage().executeJs("""
                if (!document.getElementById('marginalia-markdown-fix-style')) {
                  const style = document.createElement('style');
                  style.id = 'marginalia-markdown-fix-style';
                  style.textContent = `
                    .markdown-content pre, .markdown-content code, .markdown-content p, .markdown-content span {
                      white-space: pre-wrap !important;
                      word-break: break-word !important;
                      overflow-wrap: anywhere !important;
                      max-width: 100% !important;
                      box-sizing: border-box !important;
                    }
                  `;
                  document.head.appendChild(style);
                }
                """);

        // Simple floating back button optimized for phone/touch
        Button backButton = new Button(Solid.ARROW_LEFT.create(), event -> UI.getCurrent().navigate(Viewer.class));
        backButton.setThemeName("primary icon");
        backButton.getStyle()
                .set("position", "fixed")
                .set("top", "16px")
                .set("left", "16px")
                .set("z-index", "1000")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.3)")
                .set("border-radius", "50%");

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setSizeFull();
        scrollPanel.getStyle().set("padding", "12px");
        scrollPanel.getStyle().set("overscroll-behavior", "contain");

        VerticalLayout contentWrapper = new VerticalLayout();
        contentWrapper.setWidthFull();
        contentWrapper.setMaxWidth("800px");
        contentWrapper.getStyle().set("margin", "0 auto");
        contentWrapper.getStyle().set("padding-top", "48px");
        contentWrapper.setPadding(false);
        contentWrapper.setSpacing(true);

        try {
            List<ChatMessage> branch = (manuscript.getActiveLeaf() != null)
                    ? chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf())
                    : Collections.emptyList();

            if (branch.isEmpty()) {
                Span emptyLabel = new Span(loc.getValue(L.MSG_NO_ACTIVE_BRANCH));
                contentWrapper.add(UIUtils.centerComponent(emptyLabel));
            } else {
                for (ChatMessage msg : branch) {
                    if (StringUtils.isNotBlank(msg.getResponse())) {
                        Markdown markdown = new Markdown();
                        markdown.addClassName(SharedStyles.CHAT_MESSAGE_MARKDOWN);
                        markdown.setContent(msg.getResponse());
                        markdown.setWidthFull();
                        markdown.getStyle().set("min-width", "0");
                        markdown.getStyle().set("max-width", "100%");
                        markdown.getStyle().set("box-sizing", "border-box");

                        markdown.getElement().executeJs("""
                                const el = this;
                                const enforceWrap = () => {
                                  if (!el) return;
                                  const root = el.shadowRoot || el;
                                  const elements = root.querySelectorAll('pre, code, p, div, span');
                                  elements.forEach(node => {
                                    node.style.setProperty('white-space', 'pre-wrap', 'important');
                                    node.style.setProperty('word-break', 'break-word', 'important');
                                    node.style.setProperty('overflow-wrap', 'anywhere', 'important');
                                    node.style.setProperty('max-width', '100%', 'important');
                                    node.style.setProperty('box-sizing', 'border-box', 'important');
                                  });
                                };
                                enforceWrap();
                                const observer = new MutationObserver(enforceWrap);
                                observer.observe(el.shadowRoot || el, { childList: true, subtree: true, characterData: true });
                                """);

                        contentWrapper.add(markdown);
                    }
                    if (!msg.getImages().isEmpty()) {
                        contentWrapper.add(new MessageImages(msg.getImages()));
                    }
                }
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }

        scrollPanel.add(contentWrapper);
        container.add(backButton, scrollPanel);
        container.setFlexGrow(1, scrollPanel);

        add(container);
    }

    /**
     * Only uuid is accepted, manuscripts that are not published are visible only to their owner. Anything else
     * looks the same as nonexistent manuscript.
     */
    private Manuscript findManuscript(String param) {
        try {
            return manuscriptService.findViewable(param);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return null;
        }
    }

    private class ManuscriptWrapper extends BackendTableItem {

        private final Manuscript manuscript;
        private List<String> tags;

        public ManuscriptWrapper(Manuscript manuscript) {
            super(manuscript.getId());
            this.manuscript = manuscript;
        }

        public Manuscript getManuscript() {
            return manuscript;
        }

        public String getName() {
            return manuscript.getName();
        }

        public String getLastOpened() {
            Date date = manuscript.getLastOpened() != null ? manuscript.getLastOpened() : manuscript.getModification();
            return date != null ? loc.getDateHourFormat().format(date) : "";
        }

        public String getTags() {
            if (tags == null) {
                try {
                    tags = tagRelationService.getTagsForObject(manuscript).stream().map(Tag::getValue).sorted().toList();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                    return "ERROR";
                }
            }
            return String.join(", ", tags);
        }
    }

    private class ManuscriptBackendProvider extends BackendTableProviderBase<ManuscriptWrapper, Manuscript, ManuscriptRepository> {

        public ManuscriptBackendProvider(List<Long> ids) {
            super();
            setIds(ids);
        }

        @Override
        protected ManuscriptWrapper entityToTableItem(Manuscript entity) throws Exception {
            return new ManuscriptWrapper(entity);
        }
    }
}