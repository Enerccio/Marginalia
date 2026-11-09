package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.components.ModelText.Substitution;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ModelTextTest {

    // "Anna said: Da ek se za skag. He left."   the speech is in the stored text, the model reads the English
    private static final String STORED = "Anna said: \"Da ek se za skag.\" He left.";
    private static final int FROM = STORED.indexOf("Da ek");
    private static final int TO = STORED.indexOf("skag.") + "skag.".length();

    private static ModelText translated() {
        return ModelText.of(STORED, List.of(new Substitution(FROM, TO, "I see the enemy.")));
    }

    @Test
    void withoutSubstitutionsTheTextIsTheStoredOne() {
        ModelText text = ModelText.of(STORED, List.of());
        assertThat(text.text()).isEqualTo(STORED);
        assertThat(text.changed()).isFalse();
        assertThat(text.storedRange(3, 9)).containsExactly(3, 9);
        assertThat(text.modelRange(3, 9)).containsExactly(3, 9);
        assertThat(ModelText.unchanged(null).text()).isEmpty();
        assertThat(ModelText.of(null, null).text()).isEmpty();
    }

    @Test
    void aSubstitutionReplacesItsRange() {
        ModelText text = translated();
        assertThat(text.text()).isEqualTo("Anna said: \"I see the enemy.\" He left.");
        assertThat(text.stored()).isEqualTo(STORED);
        assertThat(text.changed()).isTrue();
    }

    @Test
    void textOutsideThePassageMapsOneToOne() {
        ModelText text = translated();
        // "He left." after the passage: the model text is shorter, the stored text longer
        int modelHe = text.text().indexOf("He left.");
        int storedHe = STORED.indexOf("He left.");
        assertThat(text.storedRange(modelHe, modelHe + 8)).containsExactly(storedHe, storedHe + 8);
        assertThat(text.modelRange(storedHe, storedHe + 8)).containsExactly(modelHe, modelHe + 8);
        // before the passage
        assertThat(text.storedRange(0, 4)).containsExactly(0, 4);
        assertThat(text.modelRange(0, 4)).containsExactly(0, 4);
    }

    @Test
    void aRangeInsideAPassageGrowsToTheWholePassage() {
        ModelText text = translated();
        int modelFrom = text.text().indexOf("I see");
        // "see the" of the English: the whole speech of the stored text
        int[] stored = text.storedRange(modelFrom + 2, modelFrom + 9);
        assertThat(stored).containsExactly(FROM, TO);
        // a selection inside the conlang: the whole English sentence
        int[] model = text.modelRange(FROM + 3, FROM + 8);
        assertThat(text.text().substring(model[0], model[1])).isEqualTo("I see the enemy.");
    }

    @Test
    void aRangeThatTouchesAPassageKeepsItsOutsideEdge() {
        ModelText text = translated();
        int modelFrom = text.text().indexOf("I see");
        int modelTo = modelFrom + "I see the enemy.".length();
        // ends exactly at the start of the passage
        assertThat(text.storedRange(0, modelFrom)).containsExactly(0, FROM);
        // starts exactly after its end
        assertThat(text.storedRange(modelTo, text.text().length())).containsExactly(TO, STORED.length());
        // from before the passage to the middle of it: ends at the end of the passage
        assertThat(text.storedRange(0, modelFrom + 3)).containsExactly(0, TO);
    }

    @Test
    void severalPassagesKeepTheDrift() {
        String stored = "aa XXXX bb YY cc";
        ModelText text = ModelText.of(stored, List.of(new Substitution(3, 7, "1"), new Substitution(11, 13, "twenty")));
        assertThat(text.text()).isEqualTo("aa 1 bb twenty cc");
        int cc = text.text().indexOf("cc");
        assertThat(text.storedRange(cc, cc + 2)).containsExactly(14, 16);
        int bb = text.text().indexOf("bb");
        assertThat(text.storedRange(bb, bb + 2)).containsExactly(8, 10);
        assertThat(text.modelRange(14, 16)).containsExactly(cc, cc + 2);
    }

    @Test
    void substitutionsAreSortedAndTheFirstGivenWinsAnOverlap() {
        String stored = "0123456789";
        ModelText text = ModelText.of(stored, List.of(new Substitution(6, 8, "B"), new Substitution(2, 5, "A"), new Substitution(4, 7, "overlaps both")));
        assertThat(text.text()).isEqualTo("01A5B89");
    }

    @Test
    void impossibleSubstitutionsAreIgnored() {
        String stored = "0123";
        ModelText text = ModelText.of(stored, List.of(new Substitution(-1, 2, "x"), new Substitution(2, 9, "y"), new Substitution(3, 1, "z"),
                new Substitution(2, 2, ""), new Substitution(0, 1, "ok")));
        assertThat(text.text()).isEqualTo("ok123");
    }

    @Test
    void anInsertionAndADeletionWork() {
        ModelText inserted = ModelText.of("ab", List.of(new Substitution(1, 1, "XY")));
        assertThat(inserted.text()).isEqualTo("aXYb");
        ModelText deleted = ModelText.of("abcd", List.of(new Substitution(1, 3, "")));
        assertThat(deleted.text()).isEqualTo("ad");
        assertThat(deleted.storedRange(1, 2)).containsExactly(3, 4);
    }

    @Test
    void aRangeOutsideTheTextIsRefused() {
        ModelText text = translated();
        assertThatThrownBy(() -> text.storedRange(0, text.text().length() + 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> text.modelRange(5, 2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> text.modelRange(-1, 2)).isInstanceOf(IllegalArgumentException.class);
    }
}
