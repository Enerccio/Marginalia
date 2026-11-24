package com.github.enerccio.marginalia.extensions.arcoutline.model;

/**
 * A tool the model called while it wrote an answer of the plot wizard, kept with the answer so the author can see what it did.
 */
public class WizardCall {

    private String name;
    /** What the model passed, JSON. */
    private String arguments;
    /** What the model got back, the error message of a failed call. */
    private String result;
    private boolean failed;
    /** Whom or what the call was about as the author knows it (the name of a character), null when the name of the tool says enough. */
    private String subject;

    public WizardCall() {
    }

    public WizardCall(String name, String arguments, String result, boolean failed) {
        this.name = name;
        this.arguments = arguments;
        this.result = result;
        this.failed = failed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }
}
