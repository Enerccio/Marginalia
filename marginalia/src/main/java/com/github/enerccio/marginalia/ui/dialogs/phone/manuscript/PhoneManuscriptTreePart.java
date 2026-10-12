package com.github.enerccio.marginalia.ui.dialogs.phone.manuscript;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.search.FulltextHit;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.TreantTree;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialogPart;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * The branch view tab of the {@link PhoneManuscriptDialog}: the tree is the same, the search bar is stacked so that it
 * fits the width of a phone.
 */
@Configurable
@Extendable
public class PhoneManuscriptTreePart implements PhoneManuscriptDialogPart {

    @Autowired
    private Localization loc;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    private final PhoneManuscriptDialog parent;
    private ScrollPanel scrollPanel;
    private TreantTree treantTree;
    private Manuscript manuscript;

    private final Set<String> expandedBlocks = new HashSet<>();

    private static final int MAX_SEARCH_HISTORY = 15;

    private ComboBox<String> searchBox;
    private Span searchStatus;
    private final List<String> searchHistory = new ArrayList<>();
    private boolean updatingSearchBox;
    // the search shown in the tree: query (null: none) and the parts found with the text around the match
    private String lastQuery;
    private final Map<Long, String> hitSnippets = new HashMap<>();

    public PhoneManuscriptTreePart(PhoneManuscriptDialog parent) {
        this.parent = parent;
    }

    @Override
    public Component create() throws Exception {
        treantTree = new TreantTree();
        treantTree.addNodeClickListener(this::onNodeClick);
        treantTree.addNodeGotoListener(this::onNodeGoto);

        scrollPanel = new ScrollPanel(treantTree);
        scrollPanel.setSizeFull();

        VerticalLayout content = new VerticalLayout(createSearchBar(), scrollPanel);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setFlexGrow(1, scrollPanel);

        return content;
    }

    private VerticalLayout createSearchBar() {
        searchBox = new ComboBox<>();
        searchBox.setPlaceholder(loc.getValue(L.LABEL_SEARCH_ALL_BRANCHES));
        // the history of searches; a combo box without items refuses a value
        searchBox.setItems(searchHistory);
        searchBox.setAllowCustomValue(true);
        searchBox.setClearButtonVisible(true);
        searchBox.setWidthFull();
        // typed text is a custom value, it has to be set to become the value
        searchBox.addCustomValueSetListener(event -> searchBox.setValue(event.getDetail()));
        searchBox.addValueChangeListener(event -> {
            if (!updatingSearchBox) {
                search(event.getValue());
            }
        });

        Button searchButton = new Button(loc.getValue(L.LABEL_SEARCH), VaadinIcon.SEARCH.create());
        searchButton.addClickListener(event -> {
            // the combo box has already searched when the click committed a typed text
            String query = StringUtils.trimToNull(searchBox.getValue());
            if (!Objects.equals(query, lastQuery)) {
                search(query);
            }
        });

        searchStatus = new Span();
        searchStatus.addClassName(CssStyles.TEXT_SECONDARY);

        HorizontalLayout searchRow = new HorizontalLayout(searchButton, searchStatus);
        searchRow.setAlignItems(FlexComponent.Alignment.CENTER);
        searchRow.setWidthFull();
        searchRow.setPadding(false);

        VerticalLayout bar = new VerticalLayout(searchBox, searchRow);
        bar.setWidthFull();
        bar.setPadding(false);
        bar.setSpacing(false);
        bar.addClassName(CssStyles.TREE_TOOLBAR);
        return bar;
    }

    /**
     * Searches the text of all branches and highlights the parts found in the tree.
     */
    private void search(String text) {
        String query = StringUtils.trimToNull(text);
        lastQuery = query;
        hitSnippets.clear();
        try {
            if (query == null) {
                searchStatus.setText("");
            } else {
                Manuscript current = parent.refreshManuscript();
                for (FulltextHit hit : chatMessageService.searchFulltext(current, query)) {
                    hitSnippets.put(hit.id(), hit.snippet());
                }
                searchStatus.setText(hitSnippets.isEmpty()
                        ? loc.getValue(L.MSG_SEARCH_NO_RESULTS)
                        : String.format(loc.getValue(L.MSG_SEARCH_RESULTS), hitSnippets.size()));
                remember(query);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        manuscript = parent.getManuscript();
        renderTree();
    }

    private void remember(String query) {
        searchHistory.remove(query);
        searchHistory.addFirst(query);
        while (searchHistory.size() > MAX_SEARCH_HISTORY) {
            searchHistory.removeLast();
        }
        updatingSearchBox = true;
        try {
            searchBox.setItems(searchHistory);
            searchBox.setValue(query);
        } finally {
            updatingSearchBox = false;
        }
    }

    private void clearSearch() {
        lastQuery = null;
        hitSnippets.clear();
        if (searchBox != null) {
            updatingSearchBox = true;
            try {
                searchBox.clear();
            } finally {
                updatingSearchBox = false;
            }
            searchStatus.setText("");
        }
    }

    @Override
    public void setFrozen(boolean frozen) {
        // ignore
    }

    @Override
    public void load(Manuscript manuscript) {
        this.manuscript = manuscript;
        this.expandedBlocks.clear();
        clearSearch();
    }

    @Override
    public void onTabLeave() throws Exception {

    }

    @Override
    public void onTabEnter() throws Exception {
        this.manuscript = parent.refreshManuscript();
        if (lastQuery != null) {
            // the story may have changed since the search
            search(lastQuery);
        } else {
            renderTree();
        }
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

                    if (n > 20 && !expandedBlocks.contains(blockKey) && !hasHit(section.subList(10, n - 10))) {
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
        if (hitSnippets.containsKey(msg.getId())) {
            classes.add("search-hit");
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

    private boolean hasHit(List<ChatMessage> messages) {
        for (ChatMessage message : messages) {
            if (hitSnippets.containsKey(message.getId())) {
                return true;
            }
        }
        return false;
    }

    private String buildSyntheticRootHtml() {
        return """
               <div class="tree-node-card">
                   <strong>%s</strong>
               </div>
               """.formatted(HtmlUtils.htmlEscape(loc.getValue(L.LABEL_ROOT)));
    }

    private String buildBlockNodeHtml(String blockId, int messageCount, int startIdx, int endIdx) {
        String title = HtmlUtils.htmlEscape(
                loc.getValue(L.MSG_TREE_BLOCK_TITLE).formatted(messageCount, startIdx, endIdx));

        return """
               <div class="tree-node-card tree-node-block" data-node-id="%s" title="%s">
                   <div class="tree-block-count"><strong>%s</strong></div>
                   <div class="tree-block-subtitle">[%d–%d] • %s</div>
               </div>
               """.formatted(blockId, title, HtmlUtils.htmlEscape(loc.getValue(L.MSG_TREE_BLOCK_MESSAGES).formatted(messageCount)),
                startIdx, endIdx, HtmlUtils.htmlEscape(loc.getValue(L.LABEL_CLICK_TO_EXPAND)));
    }

    private String buildNodeHtml(ChatMessage msg) {
        String dateStr = (msg.getCreation() != null) ? loc.getDateHourFormat().format(msg.getCreation()) : "";
        String scene = Objects.toString(msg.getSceneSetting(), "");
        String presentChars = Objects.toString(msg.getPresentCharacters(), "");
        String instructions = Objects.toString(msg.getInstructions(), "");

        List<String> tooltipLines = new ArrayList<>();
        if (StringUtils.isNotBlank(dateStr)) {
            tooltipLines.add(loc.getValue(L.LABEL_DATE) + ": " + dateStr);
        }
        if (StringUtils.isNotBlank(scene)) {
            tooltipLines.add(loc.getValue(L.LABEL_SCENE) + ": " + scene);
        }
        if (StringUtils.isNotBlank(presentChars)) {
            tooltipLines.add(loc.getValue(L.LABEL_PRESENT) + ": " + presentChars);
        }
        if (StringUtils.isNotBlank(instructions)) {
            tooltipLines.add(loc.getValue(L.LABEL_INSTRUCTIONS) + ": " + instructions);
        }

        String cardTooltip = HtmlUtils.htmlEscape(String.join("\n", tooltipLines));

        String dateHtml = StringUtils.isNotBlank(dateStr)
                ? """
                  <div class="tree-node-date">%s</div>
                  """.formatted(HtmlUtils.htmlEscape(dateStr))
                : "";

        String sceneHtml = StringUtils.isNotBlank(scene)
                ? """
                  <div class="tree-node-field"><strong>%s:</strong> %s</div>
                  """.formatted(HtmlUtils.htmlEscape(loc.getValue(L.LABEL_SCENE)), HtmlUtils.htmlEscape(scene))
                : "";

        String presentHtml = StringUtils.isNotBlank(presentChars)
                ? """
                  <div class="tree-node-field"><strong>%s:</strong> %s</div>
                  """.formatted(HtmlUtils.htmlEscape(loc.getValue(L.LABEL_PRESENT)), HtmlUtils.htmlEscape(presentChars))
                : "";

        String instructionsHtml = StringUtils.isNotBlank(instructions)
                ? """
                  <div class="tree-node-instructions" title="%s"><strong>%s:</strong> %s</div>
                  """.formatted(HtmlUtils.htmlEscape(instructions), HtmlUtils.htmlEscape(loc.getValue(L.LABEL_INSTRUCTIONS)), HtmlUtils.htmlEscape(instructions))
                : "";

        String hitHtml = "";
        if (hitSnippets.containsKey(msg.getId())) {
            hitHtml = """
                      <div class="tree-node-hit">%s</div>
                      <a class="tree-node-goto" href="#" data-goto-id="%s">%s</a>
                      """.formatted(HtmlUtils.htmlEscape(hitSnippets.get(msg.getId())), msg.getId(),
                    HtmlUtils.htmlEscape(loc.getValue(L.LABEL_SHOW_IN_STORY)));
        }

        return """
               <div class="tree-node-card" data-node-id="%s" title="%s">
                   %s
                   %s
                   %s
                   %s
                   %s
               </div>
               """.formatted(msg.getId(), cardTooltip, dateHtml, sceneHtml, presentHtml, instructionsHtml, hitHtml);
    }

    /**
     * "Show in story" of a found part: shows the part on the active branch (or on the nearest branch below it when it
     * is on another one), switches to the story tab and scrolls to it.
     */
    private void onNodeGoto(TreantTree.NodeGotoEvent event) {
        try {
            long messageId = Long.parseLong(event.getNodeId());
            Manuscript current = parent.refreshManuscript();
            if (current == null) {
                return;
            }

            // node id comes from client, resolve it only among messages of this manuscript
            ChatMessage target = chatMessageService.getAllMessages(current).stream()
                    .filter(m -> m.getId() == messageId)
                    .findFirst()
                    .orElse(null);
            ChatMessage leaf = target == null ? null : chatMessageService.findLeafFor(current, target);
            if (leaf == null) {
                return;
            }

            ChatMessage activeLeaf = current.getActiveLeaf();
            if (activeLeaf == null || !leaf.getId().equals(activeLeaf.getId())) {
                current.setActiveLeaf(leaf);
                parent.save();
            }
            parent.showMessage(target.getId());
        } catch (NumberFormatException e) {
            // not a part of the story
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
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

            // node id comes from client, resolve it only among messages of this manuscript
            List<ChatMessage> allMessages = chatMessageService.getAllMessages(currentManuscript);
            ChatMessage targetMessage = allMessages.stream()
                    .filter(m -> messageId.equals(m.getId()))
                    .findFirst()
                    .orElse(null);
            if (targetMessage == null) {
                return;
            }

            ChatMessage activeLeaf = currentManuscript.getActiveLeaf();

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