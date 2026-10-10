package com.github.enerccio.marginalia.domain.templates;

public class ExportHeaderTemplateData extends TemplateData {

    private final String title;
    private final String author;

    public ExportHeaderTemplateData(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}
