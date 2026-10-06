package com.github.enerccio.marginalia.domain.service.search;

import java.util.ArrayList;
import java.util.List;

public class Sorter {

    private Ordering ordering;
    private String attribute;

    private Sorter() {

    }

    public Ordering getOrdering() {
        return ordering;
    }

    public String getAttribute() {
        return attribute;
    }

    public enum Ordering {
        ASC, DESC
    }

    public static Sorter sorter(String attribute) {
        return sorter(attribute, Ordering.ASC);
    }

    public static Sorter sorter(String attribute, Ordering ordering) {
        Sorter s = new Sorter();
        s.attribute = attribute;
        s.ordering = ordering;
        return s;
    }

    public static String toOrderBy(String base, List<Sorter> sorters) {
        if (sorters == null || sorters.isEmpty()) {
            return "";
        }

        List<String> orderByList = new ArrayList<>();
        for (Sorter sorter : sorters) {
            String s = base + "." + sorter.attribute;
            if (sorter.ordering == Ordering.DESC) {
                s += " DESC";
            }
            orderByList.add(s);
        }

        return " ORDER BY " + String.join(", ", orderByList);
    }


}
