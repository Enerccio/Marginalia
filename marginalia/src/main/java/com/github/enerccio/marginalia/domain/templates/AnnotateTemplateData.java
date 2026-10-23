package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;

/**
 * Variables of the annotate prompt: the part to review, what the author wants looked at, the story around the part, the annotations the
 * part has already and the data the part was generated with.
 */
public class AnnotateTemplateData extends TemplateData {

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
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_ANNOTATE_INSTRUCTIONS)
    private String annotateInstructions;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_ANNOTATIONS)
    private String annotations;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_OUTPUT_FORMAT)
    private String outputFormat;

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

    public String getAnnotateInstructions() {
        return annotateInstructions;
    }

    public void setAnnotateInstructions(String annotateInstructions) {
        this.annotateInstructions = annotateInstructions;
    }

    public String getAnnotations() {
        return annotations;
    }

    public void setAnnotations(String annotations) {
        this.annotations = annotations;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

}
