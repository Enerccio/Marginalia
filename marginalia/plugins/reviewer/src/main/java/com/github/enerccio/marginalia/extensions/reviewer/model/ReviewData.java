package com.github.enerccio.marginalia.extensions.reviewer.model;

import java.util.ArrayList;
import java.util.List;

public class ReviewData {
    private List<ReviewItem> reviews = new ArrayList<>();
    private int current = 0;

    public ReviewData() {
        reviews.add(new ReviewItem());
    }

    public List<ReviewItem> getReviews() { return reviews; }
    public void setReviews(List<ReviewItem> reviews) { this.reviews = reviews; }

    public int getCurrent() { return current; }
    public void setCurrent(int current) { this.current = current; }
}
