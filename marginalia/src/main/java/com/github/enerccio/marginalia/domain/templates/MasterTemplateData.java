package com.github.enerccio.marginalia.domain.templates;



public class MasterTemplateData implements TemplateData {

    private String backgroundLore;
    private String manuscript;
    private String narrativePov;
    private String narrativeTense;
    private String style;

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore;
    }

    public String getManuscript() {
        return manuscript;
    }

    public void setManuscript(String manuscript) {
        this.manuscript = manuscript;
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
}
