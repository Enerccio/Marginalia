import Raphael from 'raphael';
import TreantModule from 'treant-js';

window.Raphael = Raphael;

function resolveTreantConstructor() {
    if (typeof window.Treant === 'function') {
        return window.Treant;
    }
    if (typeof TreantModule === 'function') {
        return TreantModule;
    }
    if (TreantModule && typeof TreantModule.Treant === 'function') {
        return TreantModule.Treant;
    }
    if (TreantModule && typeof TreantModule.default === 'function') {
        return TreantModule.default;
    }
    return window.Treant;
}

function buildTreeFromFlatNodes(flatNodes) {
    if (!flatNodes || flatNodes.length === 0) {
        return null;
    }

    const nodeMap = new Map();
    let rootNode = null;

    for (let i = 0; i < flatNodes.length; i++) {
        const item = flatNodes[i];
        const nodeObj = {
            HTMLid: item.HTMLid || ('node-' + item.id),
            HTMLclass: item.HTMLclass || 'tree-node',
            innerHTML: item.innerHTML || '',
            data: item.data || {},
            children: []
        };
        nodeMap.set(String(item.id), nodeObj);
    }

    for (let i = 0; i < flatNodes.length; i++) {
        const item = flatNodes[i];
        const nodeObj = nodeMap.get(String(item.id));
        const parentId = item.parentId ? String(item.parentId) : null;

        if (parentId && nodeMap.has(parentId)) {
            const parentObj = nodeMap.get(parentId);
            parentObj.children.push(nodeObj);
        } else {
            if (!rootNode) {
                rootNode = nodeObj;
            }
        }
    }

    return rootNode;
}

export function renderTreant(element, chartConfig) {
    element.innerHTML = '';

    if (!element.id) {
        element.id = 'treant-container-' + Math.random().toString(36).substring(2, 9);
    }

    chartConfig.chart = chartConfig.chart || {};
    chartConfig.chart.container = '#' + element.id;

    if (chartConfig.nodes && Array.isArray(chartConfig.nodes)) {
        chartConfig.nodeStructure = buildTreeFromFlatNodes(chartConfig.nodes);
        delete chartConfig.nodes;
    }

    const TreantConstructor = resolveTreantConstructor();

    if (typeof TreantConstructor === 'function') {
        requestAnimationFrame(() => {
            element.innerHTML = '';
            new TreantConstructor(chartConfig);
        });
    } else {
        console.error('Failed to resolve Treant constructor:', TreantModule, window.Treant);
        return;
    }

    if (!element._nodeClickListenerAttached) {
        element.addEventListener('click', (e) => {
            const gotoEl = e.target.closest('[data-goto-id]');
            if (gotoEl) {
                e.preventDefault();
                element.dispatchEvent(new CustomEvent('node-goto', {
                    detail: {nodeId: gotoEl.dataset.gotoId || ''},
                    bubbles: true,
                    composed: true
                }));
                return;
            }

            const nodeEl = e.target.closest('.node');
            if (nodeEl) {
                let nodeId = nodeEl.dataset.nodeId || '';

                if (!nodeId) {
                    const childDataEl = nodeEl.querySelector('[data-node-id]');
                    if (childDataEl) {
                        nodeId = childDataEl.dataset.nodeId || '';
                    }
                }

                if (!nodeId && nodeEl.id && nodeEl.id.startsWith('node-')) {
                    nodeId = nodeEl.id.substring(5);
                }

                const nodeName = nodeEl.querySelector('.node-name')?.textContent || '';
                const nodeTitle = nodeEl.querySelector('.node-title')?.textContent || '';

                element.dispatchEvent(new CustomEvent('node-click', {
                    detail: {
                        nodeId: nodeId,
                        nodeName: nodeName,
                        nodeTitle: nodeTitle
                    },
                    bubbles: true,
                    composed: true
                }));
            }
        });
        element._nodeClickListenerAttached = true;
    }
}

window.renderTreant = renderTreant;