package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;

/**
 * Variables of the impersonate prompt: the story so far, the turn details the author set for the next turn, what the author
 * wants the instructions to be about, and the plan of the story a caller (an extension, a command) gives.
 */
public class ImpersonateTemplateData extends TemplateData {

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_BACKGROUND_LORE)
    private String backgroundLore;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_NARRATIVE_POV)
    private String narrativePov;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_NARRATIVE_TENSE)
    private String narrativeTense;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_STYLE)
    private String style;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_POV_CHARACTER)
    private String povCharacter;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_SCENE_SETTING)
    private String sceneSetting;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_PRESENT_CHARACTERS)
    private String presentCharacters;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_SUMMARIES)
    private String summaries;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_PREVIOUS_STORY)
    private String previousStory;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_INSTRUCTION_EXAMPLES)
    private String instructionExamples;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_IMPERSONATE_OUTLINE)
    private String outline;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_IMPERSONATE_INSTRUCTIONS)
    private String impersonateInstructions;

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore;
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

    public String getSummaries() {
        return summaries;
    }

    public void setSummaries(String summaries) {
        this.summaries = summaries;
    }

    public String getPreviousStory() {
        return previousStory;
    }

    public void setPreviousStory(String previousStory) {
        this.previousStory = previousStory;
    }

    public String getInstructionExamples() {
        return instructionExamples;
    }

    public void setInstructionExamples(String instructionExamples) {
        this.instructionExamples = instructionExamples;
    }

    public String getOutline() {
        return outline;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public String getImpersonateInstructions() {
        return impersonateInstructions;
    }

    public void setImpersonateInstructions(String impersonateInstructions) {
        this.impersonateInstructions = impersonateInstructions;
    }

}
