package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.traits.MarginaliaTemplateDescription;
import com.github.enerccio.marginalia.loc.M_L;

public class SummaryTemplateData extends TemplateData {

    @MarginaliaTemplateDescription(loc = M_L.DESC_TEMPLATE_BACKGROUND_LORE)
    private String backgroundLore;
    @MarginaliaTemplateDescription(loc = M_L.DESC_TEMPLATE_MANUSCRIPT)
    private String text;

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

