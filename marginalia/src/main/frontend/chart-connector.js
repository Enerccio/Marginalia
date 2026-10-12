import {Chart, registerables} from 'chart.js';

Chart.register(...registerables);

/**
 * Draws the Chart.js chart described by the configuration into the element, replacing the chart drawn there before.
 * Text colors follow the Lumo theme of the page.
 */
export function renderChart(element, chartConfig) {
    const style = getComputedStyle(element);
    const textColor = style.getPropertyValue('--lumo-body-text-color').trim();
    const gridColor = style.getPropertyValue('--lumo-contrast-10pct').trim();
    if (textColor) {
        Chart.defaults.color = textColor;
    }
    if (gridColor) {
        Chart.defaults.borderColor = gridColor;
    }

    let canvas = element.querySelector('canvas');
    if (canvas) {
        const previous = Chart.getChart(canvas);
        if (previous) {
            previous.destroy();
        }
    } else {
        canvas = document.createElement('canvas');
        element.appendChild(canvas);
    }

    chartConfig.options = chartConfig.options || {};
    chartConfig.options.responsive = true;
    chartConfig.options.maintainAspectRatio = false;

    new Chart(canvas, chartConfig);
}

window.renderChart = renderChart;
