package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;

/**
 * Chart drawn by Chart.js. The chart is described by a Chart.js configuration ({@code type}, {@code data},
 * {@code options}) as JSON; the chart is responsive and fills the component, its height is set with
 * {@link #setHeight(String)} (320px by default).
 */
@NpmPackage(value = "chart.js", version = "^4.4.0")
@JsModule("./chart-connector.js")
@Extendable
public class ChartJs extends Div {

    public ChartJs() {
        addClassName(CssStyles.CHART_JS);
    }

    /**
     * Draws the chart, replacing the one drawn before.
     *
     * @param jsonConfig Chart.js configuration as JSON
     */
    public void setChartConfig(String jsonConfig) {
        JavascriptLibrary.renderChart(getElement(), jsonConfig);
    }
}
