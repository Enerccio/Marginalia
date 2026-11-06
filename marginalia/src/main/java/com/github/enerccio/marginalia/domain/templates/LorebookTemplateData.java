package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;

/**
 * Template data for lorebook entry payloads.
 * <p>
 * A single instance is used for all activated entries of one generation, so all entries share one context - variables
 * set by an entry are visible to the entries that follow it (and to the master template).
 */
public class LorebookTemplateData extends TemplateData {

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_POV_CHARACTER)
    private String povCharacter;

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_SCENE_SETTING)
    private String sceneSetting;

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_PRESENT_CHARACTERS)
    private String presentCharacters;

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_INSTRUCTIONS)
    private String instructions;

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_NARRATIVE_POV)
    private String narrativePov;

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_NARRATIVE_TENSE)
    private String narrativeTense;

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_STYLE)
    private String style;

    public String getPovCharacter() {
        return povCharacter;
    }

    public void setPovCharacter(String povCharacter) {
        this.povCharacter = povCharacter;
    }

    public String getSceneSetting() {
        return sceneSetting;
    }

    public void setSceneSetting(String sceneSetting) {
        this.sceneSetting = sceneSetting;
    }

    public String getPresentCharacters() {
        return presentCharacters;
    }

    public void setPresentCharacters(String presentCharacters) {
        this.presentCharacters = presentCharacters;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getNarrativePov() {
        return narrativePov;
    }

    public void setNarrativePov(String narrativePov) {
        this.narrativePov = narrativePov;
    }

    public String getNarrativeTense() {
        return narrativeTense;
    }

    public void setNarrativeTense(String narrativeTense) {
        this.narrativeTense = narrativeTense;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * A copy with the same values whose context is a {@link TemplateContext#fork()} of this one: what the copy sets
     * (variables) does not reach this data. Used to render something without side effects, such as the condition of an
     * entry.
     */
    public LorebookTemplateData fork() {
        LorebookTemplateData copy = new LorebookTemplateData();
        copy.povCharacter = povCharacter;
        copy.sceneSetting = sceneSetting;
        copy.presentCharacters = presentCharacters;
        copy.instructions = instructions;
        copy.narrativePov = narrativePov;
        copy.narrativeTense = narrativeTense;
        copy.style = style;
        copy.setTemplateContext(templateContext().fork());
        return copy;
    }
}
