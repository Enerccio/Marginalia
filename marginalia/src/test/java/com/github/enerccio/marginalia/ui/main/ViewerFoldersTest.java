package com.github.enerccio.marginalia.ui.main;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.widgets.FolderBookRow;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The list of the viewer with folders: the folders above the books, going into one and back by the breadcrumb, everything
 * below, the favorites and the place the list opens at (the browser keeps it in a cookie, the tests give the list what the
 * cookie held).
 */
class ViewerFoldersTest extends MarginaliaTestBase {

    /**
     * The viewer without its login, which needs a request.
     */
    private static class NoLoginViewer extends Viewer {
        @Override
        protected void showLogin() {
        }
    }

    /**
     * A UI without a session that records the scripts it is asked to run; the answers of the browser never come.
     */
    private static class RecordingUI extends UI {

        // the values the list stored in the cookie, in order
        final List<String> cookieValues = new ArrayList<>();

        private final Page page = new Page(this) {
            @Override
            public PendingJavaScriptResult executeJs(String expression, Object... params) {
                if (expression.contains("document.cookie =")) {
                    cookieValues.add((String) params[1]);
                }
                return (PendingJavaScriptResult) Proxy.newProxyInstance(getClass().getClassLoader(),
                        new Class<?>[]{PendingJavaScriptResult.class}, (proxy, method, args) -> null);
            }
        };

        @Override
        public Page getPage() {
            return page;
        }
    }

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    private boolean registeredOsgiService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps only a weak reference
    private RecordingUI ui;
    private Viewer viewer;
    private Folder scifi;
    private Folder expanse;
    private Manuscript loose;

    @BeforeEach
    void setUp() throws Exception {
        ui = new RecordingUI();
        UI.setCurrent(ui);
        login();
        scifi = folderService.create("Sci-fi", null);
        expanse = folderService.create("Expanse", scifi);
        loose = book("Loose", null);
        book("Leviathan", scifi);
        book("Caliban", expanse);

        // the test context has no OSGi; the viewer only hands the service to its CSS registry
        if (!beanFactory.containsBean("osgiService")) {
            beanFactory.registerSingleton("osgiService", Proxy.newProxyInstance(OsgiService.class.getClassLoader(),
                    new Class<?>[]{OsgiService.class}, (proxy, method, args) -> null));
            registeredOsgiService = true;
        }
        viewer = new NoLoginViewer();
        ReflectionTestUtils.invokeMethod(viewer, "renderGridView");
        // the list is filled when the cookie is read, which a test has no browser for
        ReflectionTestUtils.invokeMethod(viewer, "restoreLocation", "");
        ReflectionTestUtils.invokeMethod(viewer, "refreshGrid");
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
        // the context is shared with the other tests of the JVM, which would get the stub that answers null to everything
        if (registeredOsgiService) {
            ((DefaultSingletonBeanRegistry) beanFactory).destroySingleton("osgiService");
            registeredOsgiService = false;
        }
    }

    private Manuscript book(String name, Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    @SuppressWarnings("unchecked")
    private Grid<FolderBookRow> grid() {
        return (Grid<FolderBookRow>) ReflectionTestUtils.getField(viewer, "grid");
    }

    private List<FolderBookRow> rows() {
        return grid().getDataProvider().fetch(new Query<>()).toList();
    }

    private List<String> names() {
        return rows().stream().map(row -> row.isFolder() ? "[" + row.getName() + "]" : row.getName()).toList();
    }

    private FolderBookRow row(String name) {
        return rows().stream().filter(row -> row.getName().equals(name)).findFirst().orElseThrow();
    }

    // what the item click listener of the grid does for a folder row
    private void tap(FolderBookRow row) {
        if (row.isFavorites()) {
            ReflectionTestUtils.invokeMethod(viewer, "openFavorites");
        } else {
            ReflectionTestUtils.invokeMethod(viewer, "openFolder", row.getFolder());
        }
    }

    private void click(String text) {
        Button button = ComponentTree.button(viewer, text);
        ComponentUtil.fireEvent(button, new ClickEvent<>(button));
    }

    private void restore(String saved) {
        ReflectionTestUtils.invokeMethod(viewer, "restoreLocation", saved);
        ReflectionTestUtils.invokeMethod(viewer, "refreshGrid");
    }

    @Test
    void theTopLevelListsTheFoldersAboveTheBooks() {
        assertThat(names()).containsExactly("[Sci-fi]", "Loose");
        assertThat(row("Sci-fi").getBooks()).isEqualTo(2);
    }

    @Test
    void aFolderIsOpenedByTappingItAndLeftFromTheBreadcrumb() {
        tap(row("Sci-fi"));
        assertThat(names()).containsExactly("[Expanse]", "Leviathan");
        assertThat(ComponentTree.all(viewer, Button.class).map(Button::getText)).contains(loc.getValue(L.LABEL_BOOKS));

        tap(row("Expanse"));
        assertThat(names()).containsExactly("Caliban");

        click("Sci-fi");
        assertThat(names()).containsExactly("[Expanse]", "Leviathan");

        click(loc.getValue(L.LABEL_BOOKS));
        assertThat(names()).containsExactly("[Sci-fi]", "Loose");

        // every step is put into the cookie, the top level as empty text
        assertThat(ui.cookieValues).containsExactly(scifi.getUuid(), expanse.getUuid(), scifi.getUuid(), "");
    }

    @Test
    void includingSubfoldersListsEverythingBelowWithItsFolder() {
        ComponentTree.all(viewer, Checkbox.class).findFirst().orElseThrow().setValue(true);

        assertThat(names()).containsExactlyInAnyOrder("[Sci-fi]", "[Expanse]", "Loose", "Leviathan", "Caliban");
        assertThat(row("Caliban").getPath()).isEqualTo("Sci-fi/Expanse");
    }

    @Test
    void theListOpensInTheFolderTheCookieHolds() {
        restore(expanse.getUuid());

        assertThat(names()).containsExactly("Caliban");
        assertThat(ComponentTree.all(viewer, Button.class).map(Button::getText)).contains("Sci-fi");
    }

    @Test
    void aFolderThatIsGoneOrNotKnownIsTheTopLevel() throws Exception {
        Folder gone = folderService.create("Gone", null);
        folderService.delete(gone, false);

        restore(gone.getUuid());
        assertThat(names()).containsExactly("[Sci-fi]", "Loose");

        restore("not-a-folder");
        assertThat(names()).containsExactly("[Sci-fi]", "Loose");
    }

    @Test
    void aFolderDeletedWhileOpenSendsTheListToTheTopLevel() throws Exception {
        tap(row("Sci-fi"));
        folderService.deleteWithContent(folderService.find(scifi.getId()));

        ReflectionTestUtils.invokeMethod(viewer, "refreshGrid");

        assertThat(names()).containsExactly("Loose");
    }

    @Test
    void theFavoritesAreAFolderOfTheTopLevel() throws Exception {
        manuscriptService.setFavorite(manuscriptService.find(loose.getId()), true);
        ReflectionTestUtils.invokeMethod(viewer, "refreshGrid");
        assertThat(rows().getFirst().isFavorites()).isTrue();

        tap(rows().getFirst());
        assertThat(names()).containsExactly("Loose");

        assertThat(ui.cookieValues).containsExactly("favorites");

        click(loc.getValue(L.LABEL_BOOKS));
        assertThat(names()).containsExactly(loc.getValue(L.LABEL_FAVORITES), "[Sci-fi]", "Loose");

        // what the cookie holds for the favorites
        restore("favorites");
        assertThat(names()).containsExactly("Loose");
    }
}
