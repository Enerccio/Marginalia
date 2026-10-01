package com.github.enerccio.marginalia.ui.components;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;

@NpmPackage(value = "raphael", version = "^2.3.0")
@NpmPackage(value = "treant-js", version = "^1.0.1")
@CssImport("treant-js/Treant.css")
@JsModule("./treant-connector.js")
public class TreantTree extends Div {

    public void setTreeConfig(String jsonConfig) {
        getElement().executeJs(
                "if (window.renderTreant) { window.renderTreant($0, JSON.parse($1)); }",
                getElement(),
                jsonConfig
        );
    }

    public Registration addNodeClickListener(ComponentEventListener<NodeClickEvent> listener) {
        return addListener(NodeClickEvent.class, listener);
    }

    @DomEvent("node-click")
    public static class NodeClickEvent extends ComponentEvent<TreantTree> {

        private final String nodeId;
        private final String nodeName;
        private final String nodeTitle;

        public NodeClickEvent(TreantTree source,
                              boolean fromClient,
                              @EventData("event.detail.nodeId") String nodeId,
                              @EventData("event.detail.nodeName") String nodeName,
                              @EventData("event.detail.nodeTitle") String nodeTitle) {
            super(source, fromClient);
            this.nodeId = nodeId;
            this.nodeName = nodeName;
            this.nodeTitle = nodeTitle;
        }

        public String getNodeId() {
            return nodeId;
        }

        public String getNodeName() {
            return nodeName;
        }

        public String getNodeTitle() {
            return nodeTitle;
        }
    }

}