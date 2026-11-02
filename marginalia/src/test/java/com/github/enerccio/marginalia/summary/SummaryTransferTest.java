package com.github.enerccio.marginalia.summary;

import com.github.enerccio.marginalia.utils.SummaryTransfer;
import com.github.enerccio.marginalia.utils.SummaryTransfer.Entry;
import com.github.enerccio.marginalia.utils.SummaryTransfer.Item;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The text file of the exported summaries.
 */
class SummaryTransferTest {

    @Test
    void whatIsWrittenIsReadBack() {
        String file = SummaryTransfer.write(List.of(
                new Item("uuid-1", 1, false, "Anna meets John.\nThey talk.\n\nAt length."),
                new Item("uuid-4", 4, true, "A meta summary.")));

        assertThat(file).startsWith(SummaryTransfer.SIGNATURE);
        assertThat(SummaryTransfer.read(file)).containsExactly(
                new Entry("uuid-1", 1, "Anna meets John.\nThey talk.\n\nAt length."),
                new Entry("uuid-4", 4, "A meta summary."));
    }

    @Test
    void theBlankLinesAroundATextAreNotPartOfIt() {
        List<Entry> entries = SummaryTransfer.read("""
                some notes before the first summary

                <!-- summary part="a" order="2" -->


                  indented first line
                last line

                <!--   summary   part="b"   order="oops"   -->
                text of b
                """.replace("\n", "\r\n"));

        assertThat(entries).containsExactly(
                new Entry("a", 2, "  indented first line\nlast line"),
                new Entry("b", 0, "text of b"));
    }

    @Test
    void aSummaryWithoutAPartIsKeptForTheMatchByPosition() {
        assertThat(SummaryTransfer.read("<!-- summary order=\"3\" -->\ntext")).containsExactly(new Entry(null, 3, "text"));
    }

    @Test
    void aFileWithoutSummariesIsRefused() {
        assertThatThrownBy(() -> SummaryTransfer.read("just a text")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SummaryTransfer.read(SummaryTransfer.SIGNATURE)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void normalizeKeepsTheInsideOfTheText() {
        assertThat(SummaryTransfer.normalize("\n \n  a \n\n b  \n\n  \n")).isEqualTo("  a \n\n b  ");
        assertThat(SummaryTransfer.normalize("")).isEmpty();
    }
}
