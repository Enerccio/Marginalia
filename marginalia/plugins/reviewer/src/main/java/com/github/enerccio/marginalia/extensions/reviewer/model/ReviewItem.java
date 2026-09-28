package com.github.enerccio.marginalia.extensions.reviewer.model;

public class ReviewItem {
    private String text = "";
    private String previous = "";
    private ReviewMetadata metadata = new ReviewMetadata();

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getPrevious() { return previous; }
    public void setPrevious(String previous) { this.previous = previous; }

    public ReviewMetadata getMetadata() { return metadata; }
    public void setMetadata(ReviewMetadata metadata) { this.metadata = metadata; }
}
