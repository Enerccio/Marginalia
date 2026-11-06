package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A real language the speech of a story is translated into, as the user describes it: no lexicon, a short set of rules for the translator. The
 * presets ({@link Presets}) fill it in for the common cases. Kept in the library of the user ({@code RealStore}); a book names one by its id.
 */
public class RealLanguage {

    private String id = UUID.randomUUID().toString();
    /** What the user calls it ("Japanese, romaji for the novel"). */
    private String name = "";
    /** The language as the model should name it ("Japanese"); empty: {@link #name}. The answer says it for the speeches that are in the language. */
    private String tongue = "";
    private Script script = Script.NATIVE;
    /** Whether the honorific suffixes of names are written (san, sama, kun...). */
    private boolean honorifics = true;
    /** Whether the model also writes how the translation is read (the romaji of kana or kanji), shown above the English in the popover. */
    private boolean reading;
    /** How the level of politeness is chosen. */
    private String politeness = "";
    /** How names of people and places are written. */
    private String names = "";
    /** Anything else the translator should hold to. */
    private String rules = "";
    /** The glossary of the language: the names and terms every book in this language writes the same way (the farthest level of the glossary). */
    private List<GlossaryEntry> glossary = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** The language as the model names it. */
    public String getTongue() {
        return tongue == null || tongue.isBlank() ? getName() : tongue.strip();
    }

    public void setTongue(String tongue) {
        this.tongue = tongue;
    }

    public Script getScript() {
        return script == null ? Script.NATIVE : script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public boolean isHonorifics() {
        return honorifics;
    }

    public void setHonorifics(boolean honorifics) {
        this.honorifics = honorifics;
    }

    public boolean isReading() {
        return reading;
    }

    public void setReading(boolean reading) {
        this.reading = reading;
    }

    public String getPoliteness() {
        return politeness == null ? "" : politeness;
    }

    public void setPoliteness(String politeness) {
        this.politeness = politeness;
    }

    public String getNames() {
        return names == null ? "" : names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getRules() {
        return rules == null ? "" : rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    /** The language's own entries of the glossary, never null. */
    public List<GlossaryEntry> getGlossary() {
        if (glossary == null) {
            glossary = new ArrayList<>();
        }
        return glossary;
    }

    public void setGlossary(List<GlossaryEntry> glossary) {
        this.glossary = glossary == null ? new ArrayList<>() : new ArrayList<>(glossary);
    }
}
