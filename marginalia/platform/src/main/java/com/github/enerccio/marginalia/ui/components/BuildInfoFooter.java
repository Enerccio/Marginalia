package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.utils.BuildInfo;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.Tooltip;

/**
 * Tiny version text ({@code Marginalia 1.4.0 (93ec118)}) fixed in the bottom left corner of the page, the details
 * (full commit id, commit and build time) are in its tooltip. It does not take part in the layout.
 */
public class BuildInfoFooter extends Span {

    public BuildInfoFooter() {
        BuildInfo info = BuildInfo.current();
        setText("Marginalia " + info.getShortText());
        addClassName(CssStyles.BUILD_INFO_FOOTER);
        Tooltip.forComponent(this).setText(info.getDetailText());
    }

}
