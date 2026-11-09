package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorshipTrackerTest {

    private static List<String> generated(AuthorshipTracker tracker) {
        String text = tracker.text();
        List<String> runs = new ArrayList<>();
        for (int[] range : tracker.generatedRanges()) {
            runs.add(text.substring(range[0], range[1]));
        }
        return runs;
    }

    @Test
    void aGeneratedTextIsTheModelsWhole() {
        AuthorshipTracker tracker = new AuthorshipTracker().startWith("The night was dark. Rain fell.");

        assertThat(generated(tracker)).containsExactly("The night was dark. Rain fell.");
        assertThat(tracker.generatedWords()).isEqualTo(6);
        assertThat(tracker.writtenWords()).isZero();
    }

    @Test
    void wordsTheAuthorAddsStayTheAuthorsWhateverComesLater() {
        AuthorshipTracker tracker = new AuthorshipTracker().startWith("The night was dark. Rain fell.");

        tracker.change("The night was dark and cold. Rain fell.", false);
        assertThat(generated(tracker)).containsExactly("The night was dark", ". Rain fell.");
        assertThat(tracker.writtenWords()).isEqualTo(2);

        // a model rewrites the end: its new words are the model's, the words the author added are still the author's
        tracker.change("The night was dark and cold. Thunder rolled.", true);
        assertThat(generated(tracker)).containsExactly("The night was dark", ". Thunder rolled.");
        assertThat(tracker.generatedWords()).isEqualTo(6);
        assertThat(tracker.writtenWords()).isEqualTo(2);

        // a person's typo fix in a generated word makes the word theirs
        tracker.change("The night was dark and cold. Thunder rolled on.", false);
        assertThat(tracker.writtenWords()).isEqualTo(3);
    }

    @Test
    void aNewGenerationDoesNotKeepWhatMatchedTheOldText() {
        AuthorshipTracker tracker = new AuthorshipTracker().startWith("The night was dark.");
        tracker.change("The night was very dark.", false);

        tracker.replaceAll("The day was the brightest.", true);

        assertThat(tracker.writtenWords()).isZero();
        assertThat(tracker.generatedWords()).isEqualTo(5);
    }

    @Test
    void aTextWithNothingBeforeItIsTheWritersWhole() {
        AuthorshipTracker tracker = new AuthorshipTracker();
        tracker.change("Written by hand.", false);

        assertThat(generated(tracker)).isEmpty();
        assertThat(tracker.writtenWords()).isEqualTo(3);
    }

    @Test
    void aTranslationIsComparedToTheGeneratedTextThatWasTranslatedTheSameWay() {
        AuthorshipTracker tracker = new AuthorshipTracker().startWith("The night was dark.");
        tracker.change("The night was dark and cold.", false);

        // "Noc byla tmava" translates the generated text, the author's "a chladna" is translated too and has no counterpart there
        tracker.changeAgainst("Noc byla tmava a chladna.", "Noc byla tmava.");

        assertThat(generated(tracker)).containsExactly("Noc byla tmava", ".");
        assertThat(tracker.generatedWords()).isEqualTo(3);
        assertThat(tracker.writtenWords()).isEqualTo(2);
    }

    @Test
    void aSnapshotPutsBackWhoWroteWhat() {
        AuthorshipTracker tracker = new AuthorshipTracker().startWith("One two three.");
        AuthorshipTracker.Snapshot before = tracker.snapshot();
        tracker.change("One two three four.", false);

        tracker.load(before);

        assertThat(tracker.text()).isEqualTo("One two three.");
        assertThat(tracker.writtenWords()).isZero();
    }

    @Test
    void emptyAndMissingTextsAreFine() {
        AuthorshipTracker tracker = new AuthorshipTracker().startWith(null);
        tracker.change(null, false);

        assertThat(tracker.text()).isEmpty();
        assertThat(tracker.generatedRanges()).isEmpty();
        assertThat(tracker.generatedWords() + tracker.writtenWords()).isZero();
    }
}
