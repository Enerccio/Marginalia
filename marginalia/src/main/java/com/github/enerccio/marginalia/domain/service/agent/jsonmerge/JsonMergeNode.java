package com.github.enerccio.marginalia.domain.service.agent.jsonmerge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * One place of the three versions of a JSON document laid over each other: the base (the data when the run started), the
 * agent's and the book's (the data now). Objects line up by their keys, the elements of a list by {@link ListMatcher}; a place that
 * is not in a version is {@code null} there.
 * <p>
 * Every node decides what the result has in its place: what both versions agree on or only one changed is decided by itself
 * ({@link #autoChoice()}), a place both changed to different data is taken from the agent (a conflict, which the user can look
 * at and decide) unless both are objects or lists, which are merged place by place. The user can overrule any node with
 * {@link #choose}. The children of a node are made when they are asked for, so that a big document is read as far as the user opens it.
 */
public final class JsonMergeNode {

    /**
     * Where the data of the result of a node comes from.
     */
    public enum Choice {
        /** The children decide, each its own place. */
        MERGED,
        /** The data of the agent, or nothing when the agent has none. */
        AGENT,
        /** The data of the book now. */
        BOOK,
        /** The data of the base, as it was when the run started. */
        BASE,
        /** No data, the place is left out of the result. */
        NONE
    }

    public enum Side {
        BASE, BOOK, AGENT
    }

    /**
     * What a node is, to the user.
     */
    public enum Status {
        /** None of the versions changed it. */
        UNCHANGED,
        /** Only the agent changed it. */
        AGENT,
        /** Only the book changed it. */
        BOOK,
        /** Both changed it, in places that go together. */
        BOTH,
        /** Both changed it to the same data. */
        SAME,
        /** Both changed it to different data. */
        CONFLICT
    }

    public enum Container {
        OBJECT, ARRAY
    }

    private final JsonMergeNode parent;
    // the name in the object; null for the root and for the elements of a list
    private final String key;
    private JsonElement base;
    private JsonElement agent;
    private JsonElement book;
    // the place in the list of each version, -1 for none (the elements of a list only)
    private int baseIndex = -1;
    private int agentIndex = -1;
    private int bookIndex = -1;

    private Choice choice;
    private boolean decided;
    private List<JsonMergeNode> children;
    private Boolean conflict;
    // the three versions as they were given, in the root only, to start over
    private JsonElement[] given;

    private JsonMergeNode(JsonMergeNode parent, String key, JsonElement base, JsonElement agent, JsonElement book) {
        this.parent = parent;
        this.key = key;
        this.base = base;
        this.agent = agent;
        this.book = book;
        this.choice = autoChoice();
    }

    /**
     * The whole document. The versions are copied, nothing of what is given is changed; a version that is {@code null} has no data.
     */
    public static JsonMergeNode root(JsonElement base, JsonElement agent, JsonElement book) {
        JsonMergeNode root = new JsonMergeNode(null, null, copy(base), copy(agent), copy(book));
        root.given = new JsonElement[]{copy(base), copy(agent), copy(book)};
        return root;
    }

    /**
     * Goes back to the three versions the document was made of: the agent's data as the agent left it, nothing decided by the user.
     * Only the root can do it.
     */
    public void reset() {
        if (given == null) {
            throw new IllegalStateException("Only the whole document can start over");
        }
        base = copy(given[0]);
        agent = copy(given[1]);
        book = copy(given[2]);
        children = null;
        decided = false;
        choice = autoChoice();
        conflict = null;
    }

    private static JsonElement copy(JsonElement element) {
        return element == null ? null : element.deepCopy();
    }

    private static boolean same(JsonElement a, JsonElement b) {
        return a == null ? b == null : a.equals(b);
    }

    // --- what the node is

    public JsonMergeNode parent() {
        return parent;
    }

    public boolean isRoot() {
        return parent == null;
    }

    /**
     * The name of the member of the object, null for the root and for an element of a list.
     */
    public String key() {
        return key;
    }

    public boolean isListElement() {
        return parent != null && key == null;
    }

    public JsonElement get(Side side) {
        return switch (side) {
            case BASE -> base;
            case BOOK -> book;
            case AGENT -> agent;
        };
    }

    /**
     * The place of the element in the list of the version, -1 when the version has no such element.
     */
    public int indexIn(Side side) {
        return switch (side) {
            case BASE -> baseIndex;
            case BOOK -> bookIndex;
            case AGENT -> agentIndex;
        };
    }

    /**
     * What the node is called: the key, or the place of the element in the list with what says which element it is.
     */
    public String title() {
        if (key != null) {
            return key;
        }
        if (parent == null) {
            return "$";
        }
        int index = agentIndex >= 0 ? agentIndex : bookIndex >= 0 ? bookIndex : baseIndex;
        StringBuilder title = new StringBuilder("[").append(index).append(']');
        JsonElement shown = agent != null ? agent : book != null ? book : base;
        for (String field : ListMatcher.IDENTITY) {
            String identity = ListMatcher.identity(shown, field);
            if (identity != null) {
                title.append(' ').append(identity.length() > 40 ? identity.substring(0, 40) + "…" : identity);
                break;
            }
        }
        return title.toString();
    }

    /**
     * The path from the root, for a breadcrumb: the titles of the nodes above and the node itself.
     */
    public List<JsonMergeNode> path() {
        LinkedList<JsonMergeNode> path = new LinkedList<>();
        for (JsonMergeNode node = this; node != null; node = node.parent) {
            path.addFirst(node);
        }
        return path;
    }

    /**
     * Whether the versions of the node are objects (or lists) all, so that its places can be put together one by one; nothing for
     * a value, and for versions that are not the same kind.
     */
    public Container container() {
        Container found = null;
        for (JsonElement version : new JsonElement[]{base, agent, book}) {
            if (version == null) {
                continue;
            }
            Container container = version.isJsonObject() ? Container.OBJECT : version.isJsonArray() ? Container.ARRAY : null;
            if (container == null || found != null && found != container) {
                return null;
            }
            found = container;
        }
        return found;
    }

    /**
     * Whether the node has places inside it to look at, in any version.
     */
    public boolean hasChildren() {
        if (container() == null) {
            return false;
        }
        for (JsonElement version : new JsonElement[]{base, agent, book}) {
            if (version != null && (version.isJsonObject() ? version.getAsJsonObject().size() : version.getAsJsonArray().size()) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * The places inside the node, made when they are first asked for.
     */
    public List<JsonMergeNode> children() {
        if (children == null) {
            children = container() == null ? List.of() : buildChildren();
        }
        return children;
    }

    public boolean isLoaded() {
        return children != null;
    }

    private List<JsonMergeNode> buildChildren() {
        List<JsonMergeNode> built = new ArrayList<>();
        if (container() == Container.OBJECT) {
            Set<String> keys = new LinkedHashSet<>();
            for (JsonElement version : new JsonElement[]{agent, book, base}) {
                if (version != null) {
                    keys.addAll(version.getAsJsonObject().keySet());
                }
            }
            for (String name : keys) {
                built.add(new JsonMergeNode(this, name, member(base, name), member(agent, name), member(book, name)));
            }
            return built;
        }
        for (ListMatcher.Row row : ListMatcher.align(elements(base), elements(agent), elements(book))) {
            built.add(row(row));
        }
        return built;
    }

    private JsonMergeNode row(ListMatcher.Row row) {
        JsonMergeNode node = new JsonMergeNode(this, null, element(base, row.base()), element(agent, row.agent()), element(book, row.book()));
        node.baseIndex = row.base();
        node.agentIndex = row.agent();
        node.bookIndex = row.book();
        return node;
    }

    private static JsonElement member(JsonElement object, String name) {
        return object == null ? null : object.getAsJsonObject().get(name);
    }

    private static JsonElement element(JsonElement array, int index) {
        return array == null || index < 0 ? null : array.getAsJsonArray().get(index);
    }

    private static List<JsonElement> elements(JsonElement array) {
        List<JsonElement> list = new ArrayList<>();
        if (array != null) {
            array.getAsJsonArray().forEach(list::add);
        }
        return list;
    }

    // --- what changed

    public boolean agentChanged() {
        return !same(base, agent);
    }

    public boolean bookChanged() {
        return !same(base, book);
    }

    private boolean bothChanged() {
        return !same(agent, base) && !same(book, base) && !same(agent, book);
    }

    /**
     * Whether anything inside the node, or the node itself, is not the same in all the versions.
     */
    public boolean changed() {
        return agentChanged() || bookChanged();
    }

    /**
     * Whether the book and the agent both changed the node to different data and the places inside it do not put it together
     * (some of them are in conflict themselves). A conflict is there whatever the user chose, see {@link #unresolved()}.
     */
    public boolean conflict() {
        if (conflict == null) {
            if (!bothChanged()) {
                conflict = false;
            } else if (merges()) {
                conflict = children().stream().anyMatch(JsonMergeNode::conflict);
            } else {
                conflict = true;
            }
        }
        return conflict;
    }

    /**
     * How many places are in conflict in the node and inside it.
     */
    public int conflicts() {
        if (!conflict()) {
            return 0;
        }
        if (!merges()) {
            return 1;
        }
        int count = 0;
        for (JsonMergeNode child : children()) {
            count += child.conflicts();
        }
        return count;
    }

    public Status status() {
        if (!changed()) {
            return Status.UNCHANGED;
        }
        if (conflict()) {
            return Status.CONFLICT;
        }
        if (agentChanged() && bookChanged()) {
            return same(agent, book) ? Status.SAME : Status.BOTH;
        }
        return agentChanged() ? Status.AGENT : Status.BOOK;
    }

    /**
     * The conflicts in the node that the user did not decide: none of the nodes from the one in conflict up has been chosen.
     */
    public int unresolved() {
        return unresolved(false);
    }

    private int unresolved(boolean covered) {
        if (!conflict()) {
            return 0;
        }
        boolean decidedHere = covered || decided && choice != Choice.MERGED;
        if (merges()) {
            int count = 0;
            for (JsonMergeNode child : children()) {
                count += child.unresolved(decidedHere);
            }
            return count;
        }
        return decidedHere || decided ? 0 : 1;
    }

    /**
     * Whether both versions are changed and the places inside them are put together one by one.
     */
    private boolean merges() {
        return bothChanged() && agent != null && book != null && container() != null;
    }

    // --- the result

    /**
     * What a node does by itself: the data both agree on is taken, the data only the agent changed from the agent, the data only
     * the book changed from the book; what both changed is merged place by place when it can be and else is the agent's.
     */
    public Choice autoChoice() {
        if (same(agent, book) || same(book, base)) {
            return Choice.AGENT;
        }
        if (same(agent, base)) {
            return Choice.BOOK;
        }
        return merges() ? Choice.MERGED : Choice.AGENT;
    }

    public Choice choice() {
        return choice;
    }

    /**
     * Whether the user chose, not the node itself.
     */
    public boolean isDecided() {
        return decided;
    }

    /**
     * Takes the data of the result in this place from the version (or from the places inside it), and so makes it so in the places
     * above: their other places keep what they had.
     */
    public void choose(Choice chosen) {
        if (chosen == Choice.MERGED && container() == null) {
            throw new IllegalArgumentException("A value has no places to put together");
        }
        if (parent != null) {
            parent.mergeInPlaces();
        }
        choice = chosen;
        decided = true;
    }

    /**
     * Puts the node together from its places: each takes what the node took as a whole.
     */
    private void mergeInPlaces() {
        if (parent != null) {
            parent.mergeInPlaces();
        }
        if (choice == Choice.MERGED) {
            return;
        }
        Choice before = choice;
        for (JsonMergeNode child : children()) {
            child.choice = before;
            child.decided = false;
        }
        choice = Choice.MERGED;
        decided = true;
    }

    /**
     * What decides the data in this place: the choice of the place above that took one version (or nothing) as a whole, the top-most
     * one, as it rules over everything below it; else the choice of the place itself.
     */
    public Choice effectiveChoice() {
        Choice found = choice;
        for (JsonMergeNode above = parent; above != null; above = above.parent) {
            if (above.choice != Choice.MERGED) {
                found = above.choice;
            }
        }
        return found;
    }

    /**
     * Whether a place above rules over the choice of this one, see {@link #effectiveChoice()}.
     */
    public boolean isOverruled() {
        for (JsonMergeNode above = parent; above != null; above = above.parent) {
            if (above.choice != Choice.MERGED) {
                return true;
            }
        }
        return false;
    }

    /**
     * The data of the result in this place as it ends up in the whole result (see {@link #effectiveChoice()}); null for none.
     */
    public JsonElement effectiveResult() {
        return isOverruled() ? side(effectiveChoice()) : result();
    }

    private JsonElement side(Choice chosen) {
        return switch (chosen) {
            case AGENT -> copy(agent);
            case BOOK -> copy(book);
            case BASE -> copy(base);
            default -> null;
        };
    }

    /**
     * The data of the result in this place; null for none.
     */
    public JsonElement result() {
        return switch (choice) {
            case AGENT -> copy(agent);
            case BOOK -> copy(book);
            case BASE -> copy(base);
            case NONE -> null;
            case MERGED -> put();
        };
    }

    private JsonElement put() {
        Container container = container();
        if (container == Container.OBJECT) {
            JsonObject object = new JsonObject();
            for (JsonMergeNode child : children()) {
                JsonElement value = child.result();
                if (value != null) {
                    object.add(child.key, value);
                }
            }
            return object;
        }
        JsonArray array = new JsonArray();
        for (JsonMergeNode child : children()) {
            JsonElement value = child.result();
            if (value != null) {
                array.add(value);
            }
        }
        return array;
    }

    // --- the agent's data

    /**
     * The data of the agent in this place as the user reads and edits it: pretty printed, empty for none.
     */
    public String agentText() {
        return JsonText.pretty(agent);
    }

    /**
     * Replaces the data of the agent in this place by what the user wrote. Everything inside the node is compared again; the node
     * and the places inside it are chosen as they would be at the start. Empty text means the agent has no data here.
     *
     * @throws JsonText.InvalidJsonException the text is not JSON, nothing is changed
     */
    public void setAgentText(String text) {
        setAgent(JsonText.parse(text));
    }

    public void setAgent(JsonElement data) {
        JsonElement replacement = copy(data);
        if (parent != null) {
            parent.writeAgent(this, replacement);
        }
        agent = replacement;
        children = null;
        choice = autoChoice();
        decided = false;
        invalidate();
        // the places above have other data of the agent now, those the user did not choose are decided again
        for (JsonMergeNode above = parent; above != null; above = above.parent) {
            if (!above.decided) {
                above.choice = above.autoChoice();
            }
        }
    }

    /**
     * Puts the data of the agent of a place of this node into the agent's data of the node itself.
     */
    private void writeAgent(JsonMergeNode place, JsonElement data) {
        if (agent == null) {
            // the agent had nothing here, its data in the places inside is made on the spot
            if (data == null) {
                return;
            }
            JsonElement made = container() == Container.ARRAY ? new JsonArray() : new JsonObject();
            agent = made;
            if (parent != null) {
                parent.writeAgent(this, made);
            }
        }
        if (agent.isJsonObject()) {
            if (data == null) {
                agent.getAsJsonObject().remove(place.key);
            } else {
                agent.getAsJsonObject().add(place.key, data);
            }
            return;
        }
        JsonArray array = agent.getAsJsonArray();
        if (place.agentIndex >= 0 && data != null) {
            array.set(place.agentIndex, data);
        } else if (place.agentIndex >= 0) {
            array.remove(place.agentIndex);
            for (JsonMergeNode sibling : children()) {
                if (sibling.agentIndex > place.agentIndex) {
                    sibling.agentIndex--;
                }
            }
            place.agentIndex = -1;
        } else if (data != null) {
            array.add(data);
            place.agentIndex = array.size() - 1;
        }
    }

    private void invalidate() {
        for (JsonMergeNode node = this; node != null; node = node.parent) {
            node.conflict = null;
        }
    }

    // --- the elements of a list

    /**
     * The elements of the list next to this one that it can be matched with: those of the versions this one has none in.
     */
    public List<JsonMergeNode> matchCandidates() {
        List<JsonMergeNode> candidates = new ArrayList<>();
        if (!isListElement()) {
            return candidates;
        }
        for (JsonMergeNode sibling : parent.children()) {
            if (sibling != this && !overlaps(sibling)) {
                candidates.add(sibling);
            }
        }
        return candidates;
    }

    private boolean overlaps(JsonMergeNode other) {
        return baseIndex >= 0 && other.baseIndex >= 0 || agentIndex >= 0 && other.agentIndex >= 0
                || bookIndex >= 0 && other.bookIndex >= 0;
    }

    /**
     * Whether the element is the same place in more than one version, so that it can be split into the elements of each.
     */
    public boolean canSplit() {
        return isListElement() && (baseIndex >= 0 ? 1 : 0) + (agentIndex >= 0 ? 1 : 0) + (bookIndex >= 0 ? 1 : 0) > 1;
    }

    /**
     * Makes the element of each version a place of its own: the guess that these are the same element was wrong. Returns the new
     * places in the list, in the order of the list.
     */
    public List<JsonMergeNode> split() {
        if (!canSplit()) {
            return List.of(this);
        }
        List<JsonMergeNode> list = parent.children;
        int at = list.indexOf(this);
        List<JsonMergeNode> parts = new ArrayList<>();
        if (agentIndex >= 0) {
            parts.add(parent.row(new ListMatcher.Row(-1, agentIndex, -1)));
        }
        if (bookIndex >= 0) {
            parts.add(parent.row(new ListMatcher.Row(-1, -1, bookIndex)));
        }
        if (baseIndex >= 0) {
            parts.add(parent.row(new ListMatcher.Row(baseIndex, -1, -1)));
        }
        list.remove(at);
        list.addAll(at, parts);
        parent.rowsChanged();
        return parts;
    }

    /**
     * Makes the other element and this one the same place of the list, each from the versions it comes from: the guess that they
     * are different elements was wrong. The new place is compared again.
     *
     * @throws IllegalArgumentException the two have an element of the same version
     */
    public JsonMergeNode match(JsonMergeNode other) {
        if (!isListElement() || !other.isListElement() || parent != other.parent || overlaps(other)) {
            throw new IllegalArgumentException("These two can't be one element");
        }
        ListMatcher.Row row = new ListMatcher.Row(Math.max(baseIndex, other.baseIndex), Math.max(agentIndex, other.agentIndex),
                Math.max(bookIndex, other.bookIndex));
        List<JsonMergeNode> list = parent.children;
        int at = Math.min(list.indexOf(this), list.indexOf(other));
        list.remove(this);
        list.remove(other);
        JsonMergeNode joined = parent.row(row);
        list.add(Math.min(at, list.size()), joined);
        parent.rowsChanged();
        return joined;
    }

    /**
     * The elements of the list are others now, the guess is the user's: the list and the places above it that took one version as a
     * whole are put together from their places (which come to the same as long as the user did not choose, as the versions are
     * what they were).
     */
    private void rowsChanged() {
        for (JsonMergeNode node = this; node != null; node = node.parent) {
            node.conflict = null;
            if (!node.decided) {
                node.choice = Choice.MERGED;
            }
        }
    }
}
