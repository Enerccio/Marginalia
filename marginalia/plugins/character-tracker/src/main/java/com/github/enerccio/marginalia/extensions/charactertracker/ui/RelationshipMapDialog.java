package com.github.enerccio.marginalia.extensions.charactertracker.ui;

import com.github.enerccio.marginalia.extensions.charactertracker.model.CharacterBook;
import com.github.enerccio.marginalia.extensions.charactertracker.model.CharacterRelation;
import com.github.enerccio.marginalia.extensions.charactertracker.model.RelationDirection;
import com.github.enerccio.marginalia.extensions.charactertracker.model.StoryCharacter;
import com.github.enerccio.marginalia.extensions.charactertracker.service.CharacterTrackerService;
import com.github.enerccio.marginalia.ui.widgets.GraphView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;

/**
 * Map of all relationships of the characters: a node for every character that has a relationship (its icon, name,
 * tagline and color) and an edge for every relationship (name and tagline, an arrow at the target, at both ends when
 * the relationship is unidirectional). The characters are arranged in rings around the character with most
 * relationships; a double click on a character arranges them around it.
 */
public class RelationshipMapDialog extends Dialog {

    private final CharacterBook book;

    public RelationshipMapDialog(CharacterBook book) {
        this.book = book;

        setHeaderTitle("Relationship map");
        setWidth("90vw");
        setHeight("80vh");
        setResizable(true);
        setDraggable(true);

        JsonObject config = buildGraph();
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);
        if (config.getAsJsonArray("nodes").isEmpty()) {
            Span empty = new Span("There are no relationships yet. Add them in the dialog of a character.");
            empty.addClassName(CharacterTrackerStyles.MAP_EMPTY);
            content.add(empty);
        } else {
            Span hint = new Span("Double-click a character to arrange the map around it. Drag to move, scroll to zoom.");
            hint.addClassName(CharacterTrackerStyles.MAP_HINT);
            GraphView graph = new GraphView();
            graph.setGraph(config.toString());
            content.add(hint, graph);
            content.setFlexGrow(1, graph);
        }
        add(content);
        getFooter().add(new Button("Close", _ -> close()));
    }

    /**
     * The graph for {@link GraphView}: characters with a relationship, and the relationships between them.
     */
    JsonObject buildGraph() {
        List<CharacterRelation> relations = book.allRelations();
        relations.sort(Comparator.comparing((CharacterRelation r) -> StringUtils.defaultString(r.getName()),
                String.CASE_INSENSITIVE_ORDER).thenComparing(CharacterRelation::getUuid));

        JsonArray edges = new JsonArray();
        for (CharacterRelation relation : relations) {
            JsonObject edge = new JsonObject();
            edge.addProperty("id", relation.getUuid());
            edge.addProperty("source", relation.getSourceUuid());
            edge.addProperty("target", relation.getTargetUuid());
            edge.addProperty("label", join(relation.getName(), relation.getTagline()));
            edge.addProperty("bothWays", relation.getDirection() == RelationDirection.UNIDIRECTIONAL);
            edges.add(edge);
        }

        JsonArray nodes = new JsonArray();
        StoryCharacter center = null;
        int centerRelations = -1;
        for (StoryCharacter character : book.sortedCharacters()) {
            int count = (int) relations.stream().filter(r -> r.involves(character.getUuid())).count();
            if (count == 0) {
                continue;
            }
            String color = CharacterTrackerService.safeColor(character.getColor());
            JsonObject node = new JsonObject();
            node.addProperty("id", character.getUuid());
            node.addProperty("label", join(character.getName(), character.getTagline()));
            node.addProperty("color", color);
            node.addProperty("textColor", CharacterTrackerService.textColor(color));
            String icon = CharacterTrackerService.iconSource(character.getIcon());
            if (icon != null) {
                node.addProperty("image", icon);
            }
            nodes.add(node);
            // the characters are sorted by name, so the first of those with most relationships wins
            if (count > centerRelations) {
                center = character;
                centerRelations = count;
            }
        }

        JsonObject config = new JsonObject();
        config.add("nodes", nodes);
        config.add("edges", edges);
        if (center != null) {
            config.addProperty("centerId", center.getUuid());
        }
        return config;
    }

    private static String join(String name, String tagline) {
        return StringUtils.isBlank(tagline) ? StringUtils.defaultString(name) : name + "\n" + tagline;
    }
}
