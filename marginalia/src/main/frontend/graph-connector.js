import cytoscape from 'cytoscape';

const DOUBLE_TAP_MS = 400;
const FIT_PADDING = 30;

/**
 * Resolves a CSS color expression (e.g. var(--lumo-body-text-color)) to rgba(), which Cytoscape understands; modern
 * browsers can compute colors to forms (color-mix, color(srgb ...)) that it doesn't.
 */
function resolveColor(element, expression, fallback) {
    const probe = document.createElement('span');
    probe.style.display = 'none';
    probe.style.color = expression;
    element.appendChild(probe);
    const computed = getComputedStyle(probe).color;
    element.removeChild(probe);
    if (!computed) {
        return fallback;
    }
    const canvas = document.createElement('canvas');
    canvas.width = 1;
    canvas.height = 1;
    const context = canvas.getContext('2d', {willReadFrequently: true});
    context.clearRect(0, 0, 1, 1);
    context.fillStyle = computed;
    context.fillRect(0, 0, 1, 1);
    const [r, g, b, a] = context.getImageData(0, 0, 1, 1).data;
    return 'rgba(' + r + ',' + g + ',' + b + ',' + (a / 255) + ')';
}

/**
 * Distance of every node from the center, in edges and ignoring their direction; nodes that can't be reached from the
 * center are one level further than the farthest reachable one.
 */
export function computeLevels(cy, centerId) {
    const levels = new Map();
    const center = cy.getElementById(centerId);
    if (center.nonempty()) {
        cy.elements().bfs({
            roots: center,
            directed: false,
            visit: (v, e, u, i, depth) => {
                levels.set(v.id(), depth);
            }
        });
    }
    let max = 0;
    levels.forEach(depth => {
        max = Math.max(max, depth);
    });
    cy.nodes().forEach(node => {
        if (!levels.has(node.id())) {
            levels.set(node.id(), max + 1);
        }
    });
    return levels;
}

/**
 * Arranges the nodes in rings around the center node, one ring for each distance from it.
 */
function layoutAround(cy, centerId, animate) {
    const levels = computeLevels(cy, centerId);
    cy.nodes().removeClass('center');
    cy.getElementById(centerId).addClass('center');
    cy.layout({
        name: 'concentric',
        concentric: node => 1000 - levels.get(node.id()),
        levelWidth: () => 1,
        minNodeSpacing: 40,
        padding: FIT_PADDING,
        fit: true,
        animate: animate,
        animationDuration: 400
    }).run();
}

function buildStyle(element) {
    const text = resolveColor(element, 'var(--lumo-body-text-color)', '#222222');
    const line = resolveColor(element, 'var(--lumo-contrast-50pct)', '#888888');
    const border = resolveColor(element, 'var(--lumo-contrast-30pct)', '#bbbbbb');
    const base = resolveColor(element, 'var(--lumo-base-color)', '#ffffff');
    const primary = resolveColor(element, 'var(--lumo-primary-color)', '#1677ff');
    return [
        {
            selector: 'node',
            style: {
                'label': 'data(label)',
                'text-wrap': 'wrap',
                'text-max-width': '140px',
                'font-size': 11,
                'text-valign': 'center',
                'text-halign': 'center',
                'color': 'data(textColor)',
                'background-color': 'data(color)',
                'shape': 'round-rectangle',
                'width': 'label',
                'height': 'label',
                'padding': '8px',
                'border-width': 1,
                'border-color': border
            }
        },
        {
            // a node with an image: a round picture with the label under it
            selector: 'node[image]',
            style: {
                'shape': 'ellipse',
                'width': 64,
                'height': 64,
                'padding': 0,
                'background-image': 'data(image)',
                'background-fit': 'cover',
                'background-clip': 'node',
                'border-width': 4,
                'border-color': 'data(color)',
                'text-valign': 'bottom',
                'text-margin-y': 4,
                'text-background-color': 'data(color)',
                'text-background-opacity': 1,
                'text-background-padding': 3,
                'text-background-shape': 'roundrectangle'
            }
        },
        {
            selector: 'node.center',
            style: {
                'border-width': 4,
                'border-color': primary
            }
        },
        {
            selector: 'edge',
            style: {
                'curve-style': 'bezier',
                'width': 2,
                'line-color': line,
                'target-arrow-shape': 'triangle',
                'target-arrow-color': line,
                'label': 'data(label)',
                'font-size': 10,
                'color': text,
                'text-wrap': 'wrap',
                'text-max-width': '120px',
                'text-rotation': 'autorotate',
                'text-background-color': base,
                'text-background-opacity': 0.85,
                'text-background-padding': 2
            }
        },
        {
            selector: 'edge[?bothWays]',
            style: {
                'source-arrow-shape': 'triangle',
                'source-arrow-color': line
            }
        }
    ];
}

/**
 * Draws the graph into the element, replacing the one drawn there before. The configuration is
 * {nodes: [{id, label, color, textColor, image?}], edges: [{id, source, target, label, bothWays}], centerId}:
 * the nodes are arranged in rings around the center node, and a double click (or double tap) on a node arranges them
 * around that node.
 */
export function renderGraph(element, config) {
    if (element._graph) {
        element._graph.destroy();
        element._graph = null;
    }
    if (element._graphResize) {
        element._graphResize.disconnect();
        element._graphResize = null;
    }
    element.innerHTML = '';

    const elements = [];
    (config.nodes || []).forEach(node => elements.push({group: 'nodes', data: node}));
    (config.edges || []).forEach(edge => elements.push({group: 'edges', data: edge}));

    const cy = cytoscape({
        container: element,
        elements: elements,
        style: buildStyle(element),
        wheelSensitivity: 0.3,
        minZoom: 0.2,
        maxZoom: 3
    });
    element._graph = cy;

    layoutAround(cy, config.centerId, false);

    let lastTapId = null;
    let lastTapTime = 0;
    cy.on('tap', 'node', event => {
        const id = event.target.id();
        const now = Date.now();
        if (id === lastTapId && now - lastTapTime < DOUBLE_TAP_MS) {
            lastTapId = null;
            layoutAround(cy, id, true);
        } else {
            lastTapId = id;
            lastTapTime = now;
        }
    });

    // the element may have no size yet (a dialog that is still opening): fit the graph when it gets one
    let pendingFit = true;
    element._graphResize = new ResizeObserver(() => {
        cy.resize();
        if (pendingFit && element.clientWidth > 0 && element.clientHeight > 0) {
            pendingFit = false;
            cy.fit(undefined, FIT_PADDING);
        }
    });
    element._graphResize.observe(element);
}

window.renderGraph = renderGraph;
