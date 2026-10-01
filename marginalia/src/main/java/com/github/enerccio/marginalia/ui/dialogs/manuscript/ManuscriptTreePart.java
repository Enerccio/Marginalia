package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.TreantTree;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.util.HtmlUtils;
import org.vaadin.firitin.layouts.VTabSheet;

import java.util.*;

@Configurable
@Extendable
public class ManuscriptTreePart implements ManuscriptDialogPart {

    @Autowired
    private Localization loc;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    private final ManuscriptDialog parent;
    private ScrollPanel scrollPanel;
    private TreantTree treantTree;
    private Manuscript manuscript;

    public ManuscriptTreePart(ManuscriptDialog parent) {
        this.parent = parent;
    }

    @Override
    public Component create(VTabSheet container) throws Exception {
        treantTree = new TreantTree();
        treantTree.addNodeClickListener(this::onNodeClick);

        scrollPanel = new ScrollPanel(treantTree);
        scrollPanel.setSizeFull();

        container.add(loc.getValue(L.LABEL_BRANCH_TREE), scrollPanel);
        return scrollPanel;
    }

    @Override
    public void setFrozen(boolean frozen) {
        // ignore
    }

    @Override
    public void load(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    @Override
    public void onTabLeave() throws Exception {

    }

    @Override
    public void onTabEnter() throws Exception {
        this.manuscript = parent.refreshManuscript();
        renderTree();
    }

    private void renderTree() {
        if (manuscript == null) {
            return;
        }

        try {
            List<ChatMessage> allMessages = chatMessageService.getAllMessages(manuscript);
            if (allMessages == null || allMessages.isEmpty()) {
                return;
            }

            Map<Long, ChatMessage> idToMsgMap = new HashMap<>();
            Map<Long, List<ChatMessage>> childrenMap = new HashMap<>();
            List<ChatMessage> rootMessages = new ArrayList<>();

            for (ChatMessage msg : allMessages) {
                idToMsgMap.put(msg.getId(), msg);
            }

            for (ChatMessage msg : allMessages) {
                Long parentId = (msg.getParent() != null) ? msg.getParent().getId() : null;
                if (parentId != null && idToMsgMap.containsKey(parentId)) {
                    childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(msg);
                } else {
                    rootMessages.add(msg);
                }
            }

            ChatMessage activeLeaf = manuscript.getActiveLeaf();
            Long activeLeafId = (activeLeaf != null) ? activeLeaf.getId() : null;

            Set<Long> activePathIds = new HashSet<>();
            Long currentId = activeLeafId;
            while (currentId != null && idToMsgMap.containsKey(currentId)) {
                activePathIds.add(currentId);
                ChatMessage msg = idToMsgMap.get(currentId);
                currentId = (msg.getParent() != null) ? msg.getParent().getId() : null;
            }

            if (rootMessages.isEmpty()) {
                return;
            }

            JsonObject rootNodeJson;
            if (rootMessages.size() == 1) {
                rootNodeJson = buildNodeJson(rootMessages.getFirst(), childrenMap, activePathIds, activeLeafId);
            } else {
                rootNodeJson = new JsonObject();
                rootNodeJson.addProperty("HTMLid", "synthetic-root");
                rootNodeJson.addProperty("HTMLclass", "tree-node non-clickable");
                rootNodeJson.addProperty("innerHTML", "<div class=\"tree-node-card\"><strong>Root</strong></div>");
                JsonArray childrenArray = new JsonArray();
                for (ChatMessage rootMsg : rootMessages) {
                    childrenArray.add(buildNodeJson(rootMsg, childrenMap, activePathIds, activeLeafId));
                }
                rootNodeJson.add("children", childrenArray);
            }

            JsonObject chartConfig = new JsonObject();
            JsonObject chart = new JsonObject();

            JsonObject connectors = new JsonObject();
            connectors.addProperty("type", "step");
            chart.add("connectors", connectors);

            JsonObject nodeConfig = new JsonObject();
            nodeConfig.addProperty("collapsable", false);
            chart.add("node", nodeConfig);

            chartConfig.add("chart", chart);
            chartConfig.add("nodeStructure", rootNodeJson);

            treantTree.setTreeConfig(chartConfig.toString());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private JsonObject buildNodeJson(ChatMessage msg, Map<Long, List<ChatMessage>> childrenMap, Set<Long> activePathIds, Long activeLeafId) {
        JsonObject nodeJson = new JsonObject();

        nodeJson.addProperty("HTMLid", "node-" + msg.getId());

        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("nodeId", String.valueOf(msg.getId()));
        nodeJson.add("data", dataJson);

        List<ChatMessage> children = childrenMap.getOrDefault(msg.getId(), Collections.emptyList());
        boolean isLeaf = children.isEmpty();
        boolean isActiveLeaf = (activeLeafId != null && activeLeafId.equals(msg.getId()));
        boolean isActivePath = activePathIds.contains(msg.getId());

        StringBuilder cssClasses = new StringBuilder("tree-node");
        if (isActivePath) {
            cssClasses.append(" active-path");
        }
        if (isActiveLeaf) {
            cssClasses.append(" active-leaf");
        }
        if (isLeaf && !isActiveLeaf) {
            cssClasses.append(" clickable-leaf");
        } else {
            cssClasses.append(" non-clickable");
        }
        nodeJson.addProperty("HTMLclass", cssClasses.toString());

        nodeJson.addProperty("innerHTML", buildNodeHtml(msg));

        JsonArray childrenArray = new JsonArray();
        for (ChatMessage child : children) {
            childrenArray.add(buildNodeJson(child, childrenMap, activePathIds, activeLeafId));
        }
        nodeJson.add("children", childrenArray);

        return nodeJson;
    }

    private String buildNodeHtml(ChatMessage msg) {
        String dateStr = "";
        if (msg.getCreation() != null) {
            dateStr = loc.getDateHourFormat().format(msg.getCreation());
        }

        String scene = Objects.toString(msg.getSceneSetting(), "");
        String presentChars = Objects.toString(msg.getPresentCharacters(), "");
        String instructions = Objects.toString(msg.getInstructions(), "");

        StringBuilder fullTooltip = new StringBuilder();
        if (StringUtils.isNotBlank(dateStr)) {
            fullTooltip.append("Date: ").append(dateStr).append("\n");
        }
        if (StringUtils.isNotBlank(scene)) {
            fullTooltip.append("Scene: ").append(scene).append("\n");
        }
        if (StringUtils.isNotBlank(presentChars)) {
            fullTooltip.append("Present: ").append(presentChars).append("\n");
        }
        if (StringUtils.isNotBlank(instructions)) {
            fullTooltip.append("Instructions: ").append(instructions);
        }

        String cardTooltip = HtmlUtils.htmlEscape(fullTooltip.toString().trim());

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"tree-node-card\" data-node-id=\"").append(msg.getId())
                .append("\" title=\"").append(cardTooltip).append("\">");

        if (StringUtils.isNotBlank(dateStr)) {
            sb.append("<div class=\"tree-node-date\">").append(HtmlUtils.htmlEscape(dateStr)).append("</div>");
        }

        if (StringUtils.isNotBlank(scene)) {
            sb.append("<div class=\"tree-node-field\"><strong>Scene:</strong> ")
                    .append(HtmlUtils.htmlEscape(scene)).append("</div>");
        }

        if (StringUtils.isNotBlank(presentChars)) {
            sb.append("<div class=\"tree-node-field\"><strong>Present:</strong> ")
                    .append(HtmlUtils.htmlEscape(presentChars)).append("</div>");
        }

        if (StringUtils.isNotBlank(instructions)) {
            sb.append("<div class=\"tree-node-instructions\" title=\"").append(HtmlUtils.htmlEscape(instructions)).append("\">")
                    .append("<strong>Instructions:</strong> ").append(HtmlUtils.htmlEscape(instructions))
                    .append("</div>");
        }

        sb.append("</div>");
        return sb.toString();
    }

    private void onNodeClick(TreantTree.NodeClickEvent event) {
        String nodeIdStr = event.getNodeId();
        if (StringUtils.isBlank(nodeIdStr)) {
            return;
        }

        try {
            Long messageId = Long.parseLong(nodeIdStr);
            Manuscript currentManuscript = parent.refreshManuscript();
            if (currentManuscript == null) {
                return;
            }

            ChatMessage targetMessage = chatMessageService.find(messageId);
            if (targetMessage == null) {
                return;
            }

            ChatMessage activeLeaf = currentManuscript.getActiveLeaf();

            List<ChatMessage> allMessages = chatMessageService.getAllMessages(currentManuscript);
            boolean isLeaf = true;
            for (ChatMessage m : allMessages) {
                if (m.getParent() != null && messageId.equals(m.getParent().getId())) {
                    isLeaf = false;
                    break;
                }
            }

            boolean isCurrentActiveLeaf = (activeLeaf != null && messageId.equals(activeLeaf.getId()));

            if (isLeaf && !isCurrentActiveLeaf) {
                currentManuscript.setActiveLeaf(targetMessage);
                parent.save();
                parent.selectStoryPart();
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}