package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;

/**
 * Variables of the redraft prompt: the part the author wants written again, what the author wants changed, the story
 * around the part and the data the part was generated with.
 */
public class RedraftTemplateData extends TemplateData {

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
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_INSTRUCTIONS)
    private String instructions;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_SUMMARIES)
    private String summaries;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_PREVIOUS_STORY)
    private String previousStory;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_PART_TEXT)
    private String text;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_FOLLOWING_STORY)
    private String followingStory;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_REDRAFT_INSTRUCTIONS)
    private String redraftInstructions;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_ANNOTATIONS)
    private String annotations;

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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFollowingStory() {
        return followingStory;
    }

    public void setFollowingStory(String followingStory) {
        this.followingStory = followingStory;
    }

    public String getRedraftInstructions() {
        return redraftInstructions;
    }

    public void setRedraftInstructions(String redraftInstructions) {
        this.redraftInstructions = redraftInstructions;
    }

    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

}
