package com.github.enerccio.marginalia.extensions.translatorcommon.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * What the translator needs to know of a character: who the character is called (name and aliases, matched against the point of view and
 * the present characters of a part), the gender (a language that marks it gets it wrong when the part only says "her"), the voice (the form
 * of a word follows the register: "rough, crude soldier", "formal court speech") and whether the character is on the list of
 * {@link Speakers}. The plugin keeps its own list; it depends on no other plugin.
 */
public class CharacterNote {

    private String id = UUID.randomUUID().toString();
    private String name = "";
    private List<String> aliases = new ArrayList<>();
    private String gender = "";
    private String voice = "";
    /** On the list of the speakers: excluded in the mode {@link SpeakersMode#EVERYONE_EXCEPT}, the only ones in {@link SpeakersMode#ONLY}. */
    private boolean listed;

    public CharacterNote() {
    }

    public CharacterNote(String name) {
        this.name = name;
    }

    /**
     * Whether {@code candidate} is the name or an alias of this character, ignoring case and the spaces around it.
     */
    public boolean isCalled(String candidate) {
        String wanted = normalize(candidate);
        if (wanted.isEmpty()) {
            return false;
        }
        return normalize(name).equals(wanted) || getAliases().stream().anyMatch(alias -> normalize(alias).equals(wanted));
    }

    static String normalize(String name) {
        return name == null ? "" : name.strip().toLowerCase(Locale.ROOT);
    }

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

    public List<String> getAliases() {
        if (aliases == null) {
            aliases = new ArrayList<>();
        }
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getGender() {
        return gender == null ? "" : gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getVoice() {
        return voice == null ? "" : voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public boolean isListed() {
        return listed;
    }

    public void setListed(boolean listed) {
        this.listed = listed;
    }
}
