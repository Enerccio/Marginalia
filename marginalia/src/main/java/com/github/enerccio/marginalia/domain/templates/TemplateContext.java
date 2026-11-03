package com.github.enerccio.marginalia.domain.templates;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;

/**
 * Environment that macros are evaluated against (who is the POV character, which model is used, what the story looks
 * like so far, variables...).
 * <p>
 * One instance is created per generation and shared by every template processed during that generation (user prompt,
 * all lorebook entries, master template), so a variable set in one lorebook entry is visible in the following entries
 * and in the master template.
 */
public class TemplateContext {

    private String povCharacter;
    private String presentCharacters;
    private String sceneSetting;
    private String instructions;
    private String narrativePov;
    private String narrativeTense;
    private String style;
    private String manuscriptName;
    private String manuscriptDescription;

    private String modelName;
    private Integer maxContextTokens;
    private Integer maxResponseTokens;
    /**
     * SillyTavern-like generation type: normal, regenerate, swipe or quiet.
     */
    private String generationType = "normal";

    /**
     * Story responses of the current branch, oldest first, not including the message being generated.
     */
    private List<String> storyMessages = new ArrayList<>();
    private String lastInstructions;
    private Date lastMessageTime;
    private String latestSummary;
    /**
     * The n-th closest summary of the branch (1 is the closest, the one of {@link #latestSummary}), empty when there is
     * none; looked up when a template asks, a long branch has many. {@code null} when the render has no branch.
     */
    private IntFunction<String> summaryLookup;
    /**
     * Text of the part a regenerate or a swipe replaces, {@code null} for a new part.
     */
    private String currentMessage;

    /**
     * Seed used for stable {{pick}} results - usually the manuscript id.
     */
    private String pickSeed = "";

    private TemplateVariables variables = new TemplateVariables();
    private Clock clock = Clock.systemDefaultZone();
    private Random random = new Random();

    /**
     * Runs {{command::name}}, {@code null} when the render has no book to run commands in.
     */
    private CommandMacroRunner commandRunner;

    /**
     * Copy of this context with copied variables - use it for "dry" renders (token estimation) whose side effects
     * must not leak into the real render.
     */
    public TemplateContext fork() {
        TemplateContext copy = new TemplateContext();
        copy.povCharacter = povCharacter;
        copy.presentCharacters = presentCharacters;
        copy.sceneSetting = sceneSetting;
        copy.instructions = instructions;
        copy.narrativePov = narrativePov;
        copy.narrativeTense = narrativeTense;
        copy.style = style;
        copy.manuscriptName = manuscriptName;
        copy.manuscriptDescription = manuscriptDescription;
        copy.modelName = modelName;
        copy.maxContextTokens = maxContextTokens;
        copy.maxResponseTokens = maxResponseTokens;
        copy.generationType = generationType;
        copy.storyMessages = new ArrayList<>(storyMessages);
        copy.lastInstructions = lastInstructions;
        copy.lastMessageTime = lastMessageTime;
        copy.latestSummary = latestSummary;
        copy.summaryLookup = summaryLookup;
        copy.currentMessage = currentMessage;
        copy.pickSeed = pickSeed;
        copy.variables = variables.copy();
        copy.clock = clock;
        copy.random = random;
        // shared on purpose: the answers it keeps are the same for the estimation render and the real one
        copy.commandRunner = commandRunner;
        return copy;
    }

    /**
     * Value of a context property by name, used as a fallback when a template references a variable that its
     * {@link TemplateData} does not define (so {{povCharacter}} works in every template).
     *
     * @return {@code null} if there is no such property
     */
    public PropertyValue property(String name) {
        if (name == null) {
            return null;
        }
        return switch (name) {
            case "povCharacter" -> new PropertyValue(povCharacter);
            case "presentCharacters" -> new PropertyValue(presentCharacters);
            case "sceneSetting" -> new PropertyValue(sceneSetting);
            case "instructions" -> new PropertyValue(instructions);
            case "narrativePov" -> new PropertyValue(narrativePov);
            case "narrativeTense" -> new PropertyValue(narrativeTense);
            case "style" -> new PropertyValue(style);
            case "manuscriptName" -> new PropertyValue(manuscriptName);
            case "manuscriptDescription" -> new PropertyValue(manuscriptDescription);
            default -> null;
        };
    }

    public record PropertyValue(Object value) {
    }

    public String getPovCharacter() {
        return povCharacter;
    }

    public void setPovCharacter(String povCharacter) {
        this.povCharacter = povCharacter;
    }

    public String getPresentCharacters() {
        return presentCharacters;
    }

    public void setPresentCharacters(String presentCharacters) {
        this.presentCharacters = presentCharacters;
    }

    public String getSceneSetting() {
        return sceneSetting;
    }

    public void setSceneSetting(String sceneSetting) {
        this.sceneSetting = sceneSetting;
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

    public String getManuscriptName() {
        return manuscriptName;
    }

    public void setManuscriptName(String manuscriptName) {
        this.manuscriptName = manuscriptName;
    }

    public String getManuscriptDescription() {
        return manuscriptDescription;
    }

    public void setManuscriptDescription(String manuscriptDescription) {
        this.manuscriptDescription = manuscriptDescription;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getMaxContextTokens() {
        return maxContextTokens;
    }

    public void setMaxContextTokens(Integer maxContextTokens) {
        this.maxContextTokens = maxContextTokens;
    }

    public Integer getMaxResponseTokens() {
        return maxResponseTokens;
    }

    public void setMaxResponseTokens(Integer maxResponseTokens) {
        this.maxResponseTokens = maxResponseTokens;
    }

    public String getGenerationType() {
        return generationType;
    }

    public void setGenerationType(String generationType) {
        this.generationType = generationType;
    }

    public List<String> getStoryMessages() {
        return storyMessages;
    }

    public void setStoryMessages(List<String> storyMessages) {
        this.storyMessages = storyMessages == null ? new ArrayList<>() : storyMessages;
    }

    public String getLastInstructions() {
        return lastInstructions;
    }

    public void setLastInstructions(String lastInstructions) {
        this.lastInstructions = lastInstructions;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getLatestSummary() {
        return latestSummary;
    }

    public void setLatestSummary(String latestSummary) {
        this.latestSummary = latestSummary;
    }

    public IntFunction<String> getSummaryLookup() {
        return summaryLookup;
    }

    public void setSummaryLookup(IntFunction<String> summaryLookup) {
        this.summaryLookup = summaryLookup;
    }

    public String getCurrentMessage() {
        return currentMessage;
    }

    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }

    public String getPickSeed() {
        return pickSeed;
    }

    public void setPickSeed(String pickSeed) {
        this.pickSeed = pickSeed == null ? "" : pickSeed;
    }

    public TemplateVariables getVariables() {
        return variables;
    }

    public void setVariables(TemplateVariables variables) {
        this.variables = variables == null ? new TemplateVariables() : variables;
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public CommandMacroRunner getCommandRunner() {
        return commandRunner;
    }

    public void setCommandRunner(CommandMacroRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }
}
