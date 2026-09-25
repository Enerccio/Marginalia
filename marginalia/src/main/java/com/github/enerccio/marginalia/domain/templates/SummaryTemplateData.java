package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;

public class SummaryTemplateData implements TemplateData {

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_BACKGROUND_LORE)
    private String backgroundLore;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_MANUSCRIPT)
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
