package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.workspace.parts.ResourcesPart;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
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
    private static Grid<Resource> gridOf(Component root) {
        return (Grid<Resource>) root.getChildren().filter(Grid.class::isInstance).findFirst().orElseThrow();
    }

    @Test
    void tableIsLazyAndOnlyHoldsTheResourcesOfTheUser() throws Exception {
        User me = login();
        resourceService.upload("b.txt", uniqueName("b").getBytes(StandardCharsets.UTF_8), "text/plain");
        resourceService.upload("a.txt", uniqueName("a").getBytes(StandardCharsets.UTF_8), "text/plain");
        long mine = resourceService.countForUser();
        loginAs(createUser());
        resourceService.upload("theirs.txt", uniqueName("theirs").getBytes(StandardCharsets.UTF_8), "text/plain");
        loginAs(me);

        Component root = new ResourcesPart(null).create();
        Grid<Resource> grid = gridOf(root);

        assertThat(grid.getDataProvider().size(new Query<>())).isEqualTo((int) mine);
        List<Resource> byName = grid.getDataProvider().fetch(new Query<>(0, 100, List.of(new QuerySortOrder("originalName", SortDirection.ASCENDING)), null, null)).toList();
        assertThat(byName).extracting(Resource::getOriginalName).doesNotContain("theirs.txt").isSorted();
        assertThat(grid.getDataProvider().fetch(new Query<>(0, 1, null, null, null)).count()).isEqualTo(1);
    }

    @Test
    void deleteSelectedFollowsTheSelection() throws Exception {
        login();
        Resource resource = resourceService.upload("a.txt", uniqueName("sel").getBytes(StandardCharsets.UTF_8), "text/plain");

        Component root = new ResourcesPart(null).create();
        Grid<Resource> grid = gridOf(root);
        Optional<Button> delete = root.getChildren().flatMap(Component::getChildren).flatMap(Component::getChildren)
                .filter(Button.class::isInstance).map(Button.class::cast).filter(b -> !b.isEnabled()).findFirst();
        assertThat(delete).isPresent();

        grid.select(resource);
        assertThat(delete.get().isEnabled()).isTrue();
        grid.deselectAll();
        assertThat(delete.get().isEnabled()).isFalse();
    }
}
