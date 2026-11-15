package com.github.enerccio.marginalia.extensions.audiobook.model;

import com.github.enerccio.marginalia.extensions.audiobook.label.Age;
import com.github.enerccio.marginalia.extensions.audiobook.label.Gender;

/**
 * A speaker the cast does not name, as the labelling met them (the guard, the girl at the window): the gender and the age they got first, so a
 * later label does not flip their voice, and how often the user asked for another voice for them. Kept with the cast of the book.
 */
public class CastExtra {

    private String name = "";
    private Gender gender;
    private Age age;
    /** 0, or how often the user asked for another voice (another cell of the grid of {@code VoiceShift}). */
    private int reroll;

    public CastExtra() {
    }

    public CastExtra(String name, Gender gender, Age age) {
        this.name = name;
        this.gender = gender;
        this.age = age;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return Gender.orUnknown(gender);
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Age getAge() {
        return Age.orUnknown(age);
    }

    public void setAge(Age age) {
        this.age = age;
    }

    public int getReroll() {
        return reroll;
    }

    public void setReroll(int reroll) {
        this.reroll = reroll;
    }

    /**
     * Takes what a label says where nothing was known yet (an unknown gender or age is filled in, a known one is never changed).
     *
     * @return whether something changed
     */
    public boolean learn(Gender gender, Age age) {
        boolean changed = false;
        if (getGender() == Gender.UNKNOWN && Gender.orUnknown(gender) != Gender.UNKNOWN) {
            this.gender = gender;
            changed = true;
        }
        if (getAge() == Age.UNKNOWN && Age.orUnknown(age) != Age.UNKNOWN) {
            this.age = age;
            changed = true;
        }
        return changed;
    }
}
