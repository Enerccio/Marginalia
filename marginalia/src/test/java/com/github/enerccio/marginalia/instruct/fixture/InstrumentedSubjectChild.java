package com.github.enerccio.marginalia.instruct.fixture;

/**
 * Subclass that inherits instrumented methods without overriding them.
 */
public class InstrumentedSubjectChild extends InstrumentedSubject {

    public String own(String text) {
        return "child " + text;
    }
}
