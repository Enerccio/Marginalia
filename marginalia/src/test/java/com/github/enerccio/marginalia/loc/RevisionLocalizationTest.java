package com.github.enerccio.marginalia.loc;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The sources and the details of the versions of a part are shown in the language of whoever looks at the history.
 */
class RevisionLocalizationTest extends MarginaliaTestBase {

    @Autowired
    private Localization loc;

    @Test
    void everySourceAndEveryDetailHasAText() {
        for (RevisionSource source : RevisionSource.values()) {
            L key = loc.getRevisionSource(source);
            assertThat(key).as(source.name()).isNotNull();
            assertThat(loc.getValue(key)).as(source.name()).isNotBlank();
        }
        for (RevisionDetail detail : RevisionDetail.values()) {
            L key = loc.getRevisionDetail(detail);
            assertThat(key).as(detail.name()).isNotNull();
            assertThat(loc.getValue(key)).as(detail.name()).isNotBlank();
        }
    }

    @Test
    void aDetailIsStoredAsACodeAndFoundAgain() {
        for (RevisionDetail detail : RevisionDetail.values()) {
            assertThat(RevisionDetail.parse(detail.code())).isEqualTo(detail);
        }
        // a model, an extension, an unknown code and nothing are free text
        assertThat(RevisionDetail.parse("gemma · chat")).isNull();
        assertThat(RevisionDetail.parse("reviewer")).isNull();
        assertThat(RevisionDetail.parse("#NO_SUCH_DETAIL")).isNull();
        assertThat(RevisionDetail.parse("COMMAND")).isNull();
        assertThat(RevisionDetail.parse("")).isNull();
        assertThat(RevisionDetail.parse(null)).isNull();
    }
}
