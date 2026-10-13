package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.traits.APIParameter;
import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;

/**
 * A graph of nodes and edges drawn by Cytoscape.js, which can show cycles and nodes with more than one parent (unlike
 * the tree of {@code TreantTree}). It fills the component, which fills its parent (at least 300px high). The nodes
 * are arranged in rings around a center node; a double click on a node arranges them around that node. The user can
 * also drag the nodes and pan and zoom the view.
 */
@NpmPackage(value = "cytoscape", version = "^3.30.0")
@JsModule("./graph-connector.js")
@Extendable
public class GraphView extends Div {

    public GraphView() {
        addClassName(CssStyles.GRAPH_VIEW);
    }

    /**
     * Draws the graph, replacing the one drawn before. The JSON is an object with
     * <ul>
     *     <li>{@code nodes}: objects with {@code id}, {@code label} (may have several lines), {@code color}
     *     (background) and {@code textColor} (CSS colors), and optionally {@code image} (a URL or data URI shown in a
     *     round node, the label goes under it),</li>
     *     <li>{@code edges}: objects with {@code id}, {@code source}, {@code target}, {@code label} and
     *     {@code bothWays} (an arrow at both ends instead of only at the target),</li>
     *     <li>{@code centerId}: the node the graph is arranged around at first.</li>
     * </ul>
     *
     * @param jsonConfig the graph as JSON
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "jsonConfig", type = String.class)})
    public void setGraph(String jsonConfig) {
        JavascriptLibrary.renderGraph(getElement(), jsonConfig);
    }
}
