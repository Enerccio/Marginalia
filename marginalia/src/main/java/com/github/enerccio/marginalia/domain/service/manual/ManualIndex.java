package com.github.enerccio.marginalia.domain.service.manual;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Immutable in-memory BM25 index over the sections of the manual. A word in the heading or the page title counts
 * more than one in the body. Words are lower case letter/digit runs with a very light stemming (plural, -ing, -ed),
 * common English words are ignored.
 */
public final class ManualIndex {

    private static final double K1 = 1.2;
    private static final double B = 0.75;
    private static final int HEADING_WEIGHT = 3;
    private static final int TITLE_WEIGHT = 2;
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "of", "to", "in", "on", "at", "for", "with", "is", "are", "was", "be", "it",
            "this", "that", "do", "does", "how", "what", "why", "can", "i", "my", "me", "you", "your", "if", "as",
            "by", "from", "not", "no", "so", "but", "there", "when", "which", "will", "have", "has");

    private record Entry(ManualSection section, Map<String, Integer> frequencies, int length) {
    }

    private final List<Entry> entries = new ArrayList<>();
    private final Map<String, Integer> documentFrequency = new HashMap<>();
    private final double averageLength;

    public ManualIndex(List<ManualPage> pages) {
        long total = 0;
        for (ManualPage page : pages) {
            for (ManualSection section : page.sections()) {
                Map<String, Integer> frequencies = new HashMap<>();
                int length = 0;
                length += count(section.text(), 1, frequencies);
                length += count(section.heading(), HEADING_WEIGHT, frequencies);
                length += count(section.pageTitle(), TITLE_WEIGHT, frequencies);
                entries.add(new Entry(section, frequencies, length));
                frequencies.keySet().forEach(term -> documentFrequency.merge(term, 1, Integer::sum));
                total += length;
            }
        }
        averageLength = entries.isEmpty() ? 1 : (double) total / entries.size();
    }

    public List<ManualHit> search(String query, int limit) {
        Set<String> terms = new LinkedHashSet<>(tokens(query));
        if (terms.isEmpty() || limit <= 0) {
            return List.of();
        }
        int n = entries.size();
        List<ManualHit> hits = new ArrayList<>();
        for (Entry entry : entries) {
            double score = 0;
            for (String term : terms) {
                Integer tf = entry.frequencies().get(term);
                if (tf == null) {
                    continue;
                }
                int df = documentFrequency.get(term);
                double idf = Math.log(1 + (n - df + 0.5) / (df + 0.5));
                score += idf * tf * (K1 + 1) / (tf + K1 * (1 - B + B * entry.length() / averageLength));
            }
            if (score > 0) {
                hits.add(new ManualHit(entry.section(), score));
            }
        }
        hits.sort(Comparator.comparingDouble(ManualHit::score).reversed());
        return List.copyOf(hits.subList(0, Math.min(limit, hits.size())));
    }

    private static int count(String text, int weight, Map<String, Integer> frequencies) {
        List<String> tokens = tokens(text);
        for (String token : tokens) {
            frequencies.merge(token, weight, Integer::sum);
        }
        return tokens.size() * weight;
    }

    static List<String> tokens(String text) {
        List<String> tokens = new ArrayList<>();
        Matcher m = WORD.matcher(text.toLowerCase(Locale.ROOT));
        while (m.find()) {
            String word = m.group();
            if (!STOP_WORDS.contains(word)) {
                tokens.add(stem(word));
            }
        }
        return tokens;
    }

    private static String stem(String word) {
        if (word.length() > 5 && word.endsWith("ing")) {
            return word.substring(0, word.length() - 3);
        }
        if (word.length() > 4 && word.endsWith("ed")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.length() > 4 && word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.length() > 3 && word.endsWith("es") && !word.endsWith("ses")) {
            return word.substring(0, word.length() - 1);
        }
        if (word.length() > 3 && word.endsWith("s") && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}
