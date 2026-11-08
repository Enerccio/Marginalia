package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.workspace.parts.ResourcesPart;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ResourcesPartTest extends MarginaliaTestBase {

    @Autowired
    private ResourceService resourceService;

    @SuppressWarnings("unchecked")
    private static TreeGrid<Object> gridOf(Component root) {
        return (TreeGrid<Object>) root.getChildren().filter(TreeGrid.class::isInstance).findFirst().orElseThrow();
    }

    /** The rows below the parent (null: the groups), as the table asks for them. */
    private static List<Object> rows(TreeGrid<Object> grid, Object parent, String sort) {
        var provider = (com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider<Object, ?>) grid.getDataProvider();
        List<QuerySortOrder> orders = sort == null ? List.of() : List.of(new QuerySortOrder(sort, SortDirection.ASCENDING));
        return ((com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider<Object, Object>) provider)
                .fetchChildren(new HierarchicalQuery<>(0, 100, orders, null, null, parent)).toList();
    }

    private static String nameOf(Object row) throws Exception {
        var resource = row.getClass().getDeclaredMethod("resource");
        resource.setAccessible(true);
        Resource r = (Resource) resource.invoke(row);
        return r == null ? null : r.getOriginalName();
    }

    @Test
    void resourcesAreGroupedByTheObjectThatUsesThem() throws Exception {
        User me = login();
        Resource loose = resourceService.upload("loose.txt", uniqueName("loose").getBytes(StandardCharsets.UTF_8), "text/plain");
        Resource a = resourceService.upload("a.txt", uniqueName("a").getBytes(StandardCharsets.UTF_8), "text/plain");
        Resource b = resourceService.upload("b.txt", uniqueName("b").getBytes(StandardCharsets.UTF_8), "text/plain");
        resourceService.link(a, ChatMessage.class, 4242L);
        resourceService.link(b, ChatMessage.class, 4242L);
        long groups = resourceService.countGroupsForUser();
        loginAs(createUser());
        Resource theirs = resourceService.upload("theirs.txt", uniqueName("theirs").getBytes(StandardCharsets.UTF_8), "text/plain");
        resourceService.link(theirs, ChatMessage.class, 4242L);
        loginAs(me);

        TreeGrid<Object> grid = gridOf(new ResourcesPart(null).create());

        List<Object> top = rows(grid, null, null);
        assertThat(top).hasSize((int) groups);
        Object used = top.stream().filter(row -> row.toString().contains("4242")).findFirst().orElseThrow();
        assertThat(grid.getDataProvider().hasChildren(used)).isTrue();
        assertThat(rows(grid, used, "originalName")).extracting(row -> nameOf(row)).containsExactly("a.txt", "b.txt");
        Object none = top.stream().filter(row -> row.toString().contains("clazz=null")).findFirst().orElseThrow();
        assertThat(rows(grid, none, null)).extracting(row -> nameOf(row)).contains("loose.txt").doesNotContain("theirs.txt", "a.txt");
        assertThat(loose.getClazz()).isNull();
    }

    @Test
    void deleteSelectedFollowsTheSelection() throws Exception {
        login();
        Resource resource = resourceService.upload("a.txt", uniqueName("sel").getBytes(StandardCharsets.UTF_8), "text/plain");

        Component root = new ResourcesPart(null).create();
        TreeGrid<Object> grid = gridOf(root);
        Optional<Button> delete = root.getChildren().flatMap(Component::getChildren).flatMap(Component::getChildren)
                .filter(Button.class::isInstance).map(Button.class::cast).filter(b -> !b.isEnabled()).findFirst();
        assertThat(delete).isPresent();

        Object group = rows(grid, null, null).getFirst();
        grid.select(group);
        assertThat(delete.get().isEnabled()).isTrue();
        grid.deselectAll();
        assertThat(delete.get().isEnabled()).isFalse();
        assertThat(resource.getId()).isNotNull();
    }
}
