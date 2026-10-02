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

    private final Set<String> expandedBlocks = new HashSet<>();

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
        this.expandedBlocks.clear();
    }

    @Override
    public void onTabLeave() throws Exception {

    }

    @Override
    public void onTabEnter() throws Exception {
        this.manuscript = parent.refreshManuscript();
        renderTree();
    }

    private static class StructuralTask {
        final ChatMessage message;
        final String parentNodeId;

        StructuralTask(ChatMessage message, String parentNodeId) {
            this.message = message;
            this.parentNodeId = parentNodeId;
        }
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

            if (rootMessages.isEmpty()) {
                return;
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

            JsonArray flatNodes = new JsonArray();
            Queue<StructuralTask> queue = new ArrayDeque<>();

            if (rootMessages.size() == 1) {
                queue.add(new StructuralTask(rootMessages.getFirst(), null));
            } else {
                JsonObject syntheticRoot = new JsonObject();
                syntheticRoot.addProperty("id", "synthetic-root");
                syntheticRoot.addProperty("HTMLid", "synthetic-root");
                syntheticRoot.addProperty("HTMLclass", "tree-node non-clickable");
                syntheticRoot.addProperty("innerHTML", buildSyntheticRootHtml());
                flatNodes.add(syntheticRoot);

                for (ChatMessage rootMsg : rootMessages) {
                    queue.add(new StructuralTask(rootMsg, "synthetic-root"));
                }
            }

            while (!queue.isEmpty()) {
                StructuralTask task = queue.poll();
                ChatMessage structMsg = task.message;
                String parentNodeId = task.parentNodeId;

                String structNodeId = String.valueOf(structMsg.getId());
                flatNodes.add(buildSingleMsgJsonObject(structMsg, structNodeId, parentNodeId, childrenMap, activePathIds, activeLeafId));

                List<ChatMessage> structChildren = childrenMap.getOrDefault(structMsg.getId(), Collections.emptyList());

                for (ChatMessage child : structChildren) {
                    List<ChatMessage> section = new ArrayList<>();
                    ChatMessage curr = child;

                    while (curr != null) {
                        List<ChatMessage> currChildren = childrenMap.getOrDefault(curr.getId(), Collections.emptyList());
                        if (currChildren.size() == 1) {
                            section.add(curr);
                            curr = currChildren.getFirst();
                        } else {
                            break;
                        }
                    }

                    String lastEmittedNodeId = structNodeId;
                    int n = section.size();
                    String blockKey = "block-" + structMsg.getId() + "-" + (curr != null ? curr.getId() : (section.isEmpty() ? "end" : section.getLast().getId()));

                    if (n > 20 && !expandedBlocks.contains(blockKey)) {
                        for (int i = 0; i < 10; i++) {
                            ChatMessage m = section.get(i);
                            String mId = String.valueOf(m.getId());
                            flatNodes.add(buildSingleMsgJsonObject(m, mId, lastEmittedNodeId, childrenMap, activePathIds, activeLeafId));
                            lastEmittedNodeId = mId;
                        }

                        List<ChatMessage> collapsedSection = section.subList(10, n - 10);
                        boolean blockHasActivePath = false;
                        boolean blockHasActiveLeaf = false;
                        for (ChatMessage cm : collapsedSection) {
                            if (activePathIds.contains(cm.getId())) {
                                blockHasActivePath = true;
                            }
                            if (activeLeafId != null && activeLeafId.equals(cm.getId())) {
                                blockHasActiveLeaf = true;
                            }
                        }

                        JsonObject blockJson = new JsonObject();
                        blockJson.addProperty("id", blockKey);
                        blockJson.addProperty("parentId", lastEmittedNodeId);
                        blockJson.addProperty("HTMLid", blockKey);

                        List<String> blockCssList = new ArrayList<>();
                        blockCssList.add("tree-node");
                        blockCssList.add("block-node");
                        if (blockHasActivePath) {
                            blockCssList.add("active-path");
                        }
                        if (blockHasActiveLeaf) {
                            blockCssList.add("active-leaf");
                        }
                        blockJson.addProperty("HTMLclass", String.join(" ", blockCssList));

                        JsonObject dataJson = new JsonObject();
                        dataJson.addProperty("nodeId", blockKey);
                        blockJson.add("data", dataJson);

                        int startIdx = 11;
                        int endIdx = n - 10;
                        blockJson.addProperty("innerHTML", buildBlockNodeHtml(blockKey, collapsedSection.size(), startIdx, endIdx));
                        flatNodes.add(blockJson);

                        lastEmittedNodeId = blockKey;

                        for (int i = n - 10; i < n; i++) {
                            ChatMessage m = section.get(i);
                            String mId = String.valueOf(m.getId());
                            flatNodes.add(buildSingleMsgJsonObject(m, mId, lastEmittedNodeId, childrenMap, activePathIds, activeLeafId));
                            lastEmittedNodeId = mId;
                        }

                    } else {
                        for (ChatMessage m : section) {
                            String mId = String.valueOf(m.getId());
                            flatNodes.add(buildSingleMsgJsonObject(m, mId, lastEmittedNodeId, childrenMap, activePathIds, activeLeafId));
                            lastEmittedNodeId = mId;
                        }
                    }

                    if (curr != null) {
                        queue.add(new StructuralTask(curr, lastEmittedNodeId));
                    }
                }
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
            chartConfig.add("nodes", flatNodes);

            treantTree.setTreeConfig(chartConfig.toString());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private JsonObject buildSingleMsgJsonObject(ChatMessage msg,
                                                String nodeId,
                                                String parentNodeId,
                                                Map<Long, List<ChatMessage>> childrenMap,
                                                Set<Long> activePathIds,
                                                Long activeLeafId) {
        JsonObject nodeJson = new JsonObject();
        nodeJson.addProperty("id", nodeId);
        if (parentNodeId != null) {
            nodeJson.addProperty("parentId", parentNodeId);
        }
        nodeJson.addProperty("HTMLid", "node-" + nodeId);

        JsonObject dataJson = new JsonObject();
        dataJson.addProperty("nodeId", nodeId);
        nodeJson.add("data", dataJson);

        List<ChatMessage> children = childrenMap.getOrDefault(msg.getId(), Collections.emptyList());
        boolean isLeaf = children.isEmpty();
        boolean isActiveLeaf = (activeLeafId != null && activeLeafId.equals(msg.getId()));
        boolean isActivePath = activePathIds.contains(msg.getId());

        List<String> classes = new ArrayList<>();
        classes.add("tree-node");
        if (isActivePath) {
            classes.add("active-path");
        }
        if (isActiveLeaf) {
            classes.add("active-leaf");
        }
        if (isLeaf && !isActiveLeaf) {
            classes.add("clickable-leaf");
        } else {
            classes.add("non-clickable");
        }
        nodeJson.addProperty("HTMLclass", String.join(" ", classes));
        nodeJson.addProperty("innerHTML", buildNodeHtml(msg));

        return nodeJson;
    }

    private String buildSyntheticRootHtml() {
        return """
               <div class="tree-node-card">
                   <strong>Root</strong>
               </div>
               """;
    }

    private String buildBlockNodeHtml(String blockId, int messageCount, int startIdx, int endIdx) {
        String title = HtmlUtils.htmlEscape("""
            Collapsed block (%d messages, [%d–%d]). Click to expand.
            """.formatted(messageCount, startIdx, endIdx).trim());

        return """
               <div class="tree-node-card tree-node-block" data-node-id="%s" title="%s">
                   <div class="tree-block-count"><strong>%d messages</strong></div>
                   <div class="tree-block-subtitle">[%d–%d] • Click to expand</div>
               </div>
               """.formatted(blockId, title, messageCount, startIdx, endIdx);
    }

    private String buildNodeHtml(ChatMessage msg) {
        String dateStr = (msg.getCreation() != null) ? loc.getDateHourFormat().format(msg.getCreation()) : "";
        String scene = Objects.toString(msg.getSceneSetting(), "");
        String presentChars = Objects.toString(msg.getPresentCharacters(), "");
        String instructions = Objects.toString(msg.getInstructions(), "");

        List<String> tooltipLines = new ArrayList<>();
        if (StringUtils.isNotBlank(dateStr)) {
            tooltipLines.add("Date: " + dateStr);
        }
        if (StringUtils.isNotBlank(scene)) {
            tooltipLines.add("Scene: " + scene);
        }
        if (StringUtils.isNotBlank(presentChars)) {
            tooltipLines.add("Present: " + presentChars);
        }
        if (StringUtils.isNotBlank(instructions)) {
            tooltipLines.add("Instructions: " + instructions);
        }

        String cardTooltip = HtmlUtils.htmlEscape(String.join("\n", tooltipLines));

        String dateHtml = StringUtils.isNotBlank(dateStr)
                ? """
                  <div class="tree-node-date">%s</div>
                  """.formatted(HtmlUtils.htmlEscape(dateStr))
                : "";

        String sceneHtml = StringUtils.isNotBlank(scene)
                ? """
                  <div class="tree-node-field"><strong>Scene:</strong> %s</div>
                  """.formatted(HtmlUtils.htmlEscape(scene))
                : "";

        String presentHtml = StringUtils.isNotBlank(presentChars)
                ? """
                  <div class="tree-node-field"><strong>Present:</strong> %s</div>
                  """.formatted(HtmlUtils.htmlEscape(presentChars))
                : "";

        String instructionsHtml = StringUtils.isNotBlank(instructions)
                ? """
                  <div class="tree-node-instructions" title="%s"><strong>Instructions:</strong> %s</div>
                  """.formatted(HtmlUtils.htmlEscape(instructions), HtmlUtils.htmlEscape(instructions))
                : "";

        return """
               <div class="tree-node-card" data-node-id="%s" title="%s">
                   %s
                   %s
                   %s
                   %s
               </div>
               """.formatted(msg.getId(), cardTooltip, dateHtml, sceneHtml, presentHtml, instructionsHtml);
    }

    private void onNodeClick(TreantTree.NodeClickEvent event) {
        String nodeIdStr = event.getNodeId();
        if (StringUtils.isBlank(nodeIdStr)) {
            return;
        }

        if (nodeIdStr.startsWith("block-")) {
            if (expandedBlocks.contains(nodeIdStr)) {
                expandedBlocks.remove(nodeIdStr);
            } else {
                expandedBlocks.add(nodeIdStr);
            }
            renderTree();
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