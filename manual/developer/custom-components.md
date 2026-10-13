---
label: Custom components
order: 905
covers:
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/components/TreantTree.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/widgets/ChartJs.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/widgets/GraphView.java
  - marginalia/src/main/java/com/github/enerccio/marginalia/ui/JavascriptLibrary.java
  - marginalia/src/main/frontend
verified: 10ebbbeb
---

# Custom components

Marginalia has almost no hand-written frontend code, but three components draw with a JavaScript library that Vaadin
doesn't ship: a tree, a chart and a graph. This page documents them, how they get their library, and how to add
another one. Plugins can use all three; a plugin can't bring a library of its own (see
[below](#components-in-plugins)).

| Component | Library | Draws | Used by |
|---|---|---|---|
| [`TreantTree`](#treanttree) | [Treant.js](https://fperucic.github.io/treant-js/) (and Raphaël) | A tree of HTML nodes. | The story tree (`ManuscriptTreePart`, `PhoneManuscriptTreePart`). |
| [`ChartJs`](#chartjs) | [Chart.js](https://www.chartjs.org/) | Bar, doughnut and other charts. | `TokenUsageDialog`. |
| [`GraphView`](#graphview) | [Cytoscape.js](https://js.cytoscape.org/) | A graph (nodes and edges, cycles allowed). | The *Character Tracker* plugin (relationship map). |

All three work the same way:

1. The Java class is a `Div` annotated with `@NpmPackage` (the library, downloaded by the Vaadin frontend build) and
   `@JsModule` (the connector in `src/main/frontend`).
2. The connector is a small ES module that imports the library, exports a function that draws into an element and
   registers it as `window.<name>`.
3. The Java method sends the data **as JSON** to `JavascriptLibrary.<name>(element, json)`, which runs
   `window.<name>(element, JSON.parse(json))` with the component's element. All scripts the application runs are static
   methods of `JavascriptLibrary` ([User interface](ui.md#browser-side-code)); a component never calls `executeJs` itself.
4. Drawing again with new data replaces what was drawn.

The script is queued by Vaadin until the element is attached, so a component may get its data before it is shown
(e.g. in the constructor of a dialog).

## TreantTree

`ui/components/TreantTree.java`, `frontend/treant-connector.js`. npm packages `treant-js` and `raphael`, Treant's CSS
is imported with `@CssImport`.

`setTreeConfig(json)` takes a Treant configuration with one change: instead of the nested `nodeStructure`, `nodes` is a
**flat list**, and the connector builds the tree:

```json
{
  "chart": {"connectors": {"type": "step"}, "node": {"collapsable": false}},
  "nodes": [
    {"id": "1", "innerHTML": "<b>root</b>", "HTMLclass": "tree-node", "HTMLid": "1", "data": {"nodeId": "1"}},
    {"id": "2", "parentId": "1", "innerHTML": "child", "HTMLclass": "tree-node"}
  ]
}
```

| Node key | |
|---|---|
| `id` | Required, unique. |
| `parentId` | The parent's `id`. **The first node without a known parent is the root, any further one is dropped** - there is exactly one root, so a forest needs one component per tree. |
| `innerHTML` | The content of the node. It is HTML: escape anything the user typed. |
| `HTMLclass`, `HTMLid` | Class(es) and id of the node element. |
| `data` | Kept on the node. |

Events, as `addNodeClickListener` / `addNodeGotoListener`:

- `NodeClickEvent` - a node was clicked: `getNodeId()` (the `data-node-id` of the node or of an element in it, else the
  node's `HTMLid` without a `node-` prefix), `getNodeName()` and `getNodeTitle()` (text of `.node-name` and
  `.node-title` inside it).
- `NodeGotoEvent` - an element with `data-goto-id` inside a node was clicked (the node isn't clicked then):
  `getNodeId()` is that attribute.

The look is `.Treant` and `.Treant .node` in `shared-styles.css`, written with `!important` because Treant sets its own
inline styles. A tree is **only a tree**: a graph with cycles or nodes with several parents has to be cut into trees
and repeated nodes, which is why the graph below exists.

## ChartJs

`ui/widgets/ChartJs.java`, `frontend/chart-connector.js`. npm package `chart.js`.

`setChartConfig(json)` takes a Chart.js configuration (`type`, `data`, `options`) and draws it responsively into the
element (320px high, `chart-js` style in `shared-styles.css`; `setHeight` changes it). The connector takes the text and
grid colors from the Lumo variables of the page, so charts follow the theme. Example: the doughnut and bar charts of
`TokenUsageDialog`.

## GraphView

`ui/widgets/GraphView.java`, `frontend/graph-connector.js`. npm package `cytoscape`. It draws what a tree can't:
nodes that are connected in a cycle or to several others.

`setGraph(json)`:

```json
{
  "nodes": [
    {"id": "a", "label": "Alice\nthe baker", "color": "#ffcc00", "textColor": "#000000"},
    {"id": "b", "label": "Bob", "color": "#102030", "textColor": "#ffffff", "image": "data:image/png;base64,..."}
  ],
  "edges": [
    {"id": "e1", "source": "a", "target": "b", "label": "employs\nsince spring", "bothWays": false}
  ],
  "centerId": "a"
}
```

| Key | |
|---|---|
| node `label` | May have several lines (`\n`). |
| node `color`, `textColor` | CSS colors of the background and the label. Colors end up in the drawing, so pass only validated values. |
| node `image` | Optional URL or data URI. The node is then a round picture with the label under it. |
| edge `source`, `target` | Node ids. An arrow points at the target. |
| edge `bothWays` | An arrow at both ends - a relationship that has no direction. |
| edge `label` | May have several lines. |
| `centerId` | The node the nodes are arranged around at first. |

The nodes are laid out in **rings** around the center (the `concentric` layout): the center in the middle, its
neighbours on the first ring, theirs on the second and so on, with the distance counted in edges, ignoring direction;
nodes the center can't reach are on the outermost ring. **A double click (or double tap) on a node arranges the graph
around that node**, animated, in the browser - there is no round trip to the server. The user can drag nodes and pan
and zoom the view. The component fills its parent (`graph-view` style, at least 300px high), so give the parent a
size, e.g. a `VerticalLayout` with `setSizeFull()` and `setFlexGrow(1, graph)` in a dialog; it follows later size
changes (a resizable dialog) with a `ResizeObserver`.

Theme colors of the lines and labels are read from the Lumo variables when the graph is drawn and passed through a
canvas, because the browser may compute a color to a form that Cytoscape can't parse. A theme change after that isn't
followed until the graph is drawn again.

## Adding a component for another library

1. Add the Java class (a `Div`) with `@NpmPackage(value = "<package>", version = "^x.y.z")` and
   `@JsModule("./<name>-connector.js")`. The Maven build downloads the package with the rest of the frontend; the first
   build after adding one is slow.
2. Write `src/main/frontend/<name>-connector.js`: import the library, export `render<Name>(element, config)` that
   destroys what it drew into `element` before, and set `window.render<Name> = render<Name>`. Take colors from the Lumo
   variables rather than hard-coding them.
3. Add the static method to `JavascriptLibrary` (`element.executeJs("if (window.render<Name>) { window.render<Name>($0, JSON.parse($1)); }", element, json)`).
4. If the element needs a size or a positioning context, add the rule to `shared-styles.css` and its name to `CssStyles`
   (both with the same name).
5. Mark the class `@Extendable` and give each public method `@PublicAPI`, run
   `mvn test -Dtest=PublicAPISignatureTest` and paste what it prints ([Extendable hooks](plugins/extendable.md)).
6. Note it here, in [Project structure](project-structure.md) (components, frontend files) and in the changelog.

## Components in plugins

A plugin can create `TreantTree`, `ChartJs` and `GraphView` like any other Vaadin component: they are classes of the
application, and their library comes with the application's frontend bundle. What a plugin **can't** do is add an npm
package or a JavaScript module to that bundle - it is built with the application. Libraries for plugins therefore have
to be added here, as a component. The only frontend resource a plugin can add is CSS ([UI extensions](plugins/ui-extensions.md),
`ExtensionHasCss`); plugins run their own small scripts with `executeJs`.

## Vaadin add-ons

Besides the libraries above, the application uses two Vaadin add-ons (Maven dependencies, no npm):

| Add-on | Used for |
|---|---|
| [Viritin](https://github.com/viritin/viritin) (`in.virit:viritin`) | `VTabSheet`, the tab sheet of the story editor's left bar, which is also the one extensions add tabs to. |
| [Font Awesome iron iconset](https://github.com/FlowingCode/FontAwesomeIronIconset) (`com.flowingcode.addons`) | The `FontAwesome.Solid` icons (`Solid.PLUS.create()`) used for buttons. |
