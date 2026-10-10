package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;

public class MetaSummaryTemplateData extends TemplateData {

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_BACKGROUND_LORE)
    private String backgroundLore;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_SUMMARY_BLOCKS)
    private String summaryBlocks;

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore;
    }

    public String getSummaryBlocks() {
        return summaryBlocks;
    }

    public void setSummaryBlocks(String summaryBlocks) {
        this.summaryBlocks = summaryBlocks;
    }
}
