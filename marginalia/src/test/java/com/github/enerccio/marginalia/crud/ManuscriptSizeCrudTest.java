package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import com.github.enerccio.marginalia.domain.service.ManuscriptSizeService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ManuscriptSizeCrudTest extends OwnedCrudContract<ManuscriptSize> {

    @Autowired
    private ManuscriptSizeService sizeService;

    @Override
    protected OwnedService<ManuscriptSize, ?> service() {
        return sizeService;
    }

    @Override
    protected ManuscriptSize newEntity() {
        ManuscriptSize size = new ManuscriptSize();
        size.setBookUuid(java.util.UUID.randomUUID().toString());
        size.setBookName("The Long Night");
        size.setFolderPath("Sci-fi");
        size.setDay(LocalDate.of(2026, 10, 31));
        size.setBookWords(1000);
        size.setBookTokens(1500);
        size.setBranchWords(600);
        size.setBranchTokens(900);
        size.setLeafUuid("leaf-1");
        size.setBaseline(true);
        size.setLastChange(new Date());
        return size;
    }

    @Override
    protected void assertCreated(ManuscriptSize loaded) {
        assertThat(loaded.getBookWords()).isEqualTo(1000);
        assertThat(loaded.getBookTokens()).isEqualTo(1500);
        assertThat(loaded.getBranchWords()).isEqualTo(600);
        assertThat(loaded.getBranchTokens()).isEqualTo(900);
        assertThat(loaded.getLeafUuid()).isEqualTo("leaf-1");
        assertThat(loaded.isBaseline()).isTrue();
        assertThat(loaded.isReconstructed()).isFalse();
        assertThat(loaded.getDay()).isEqualTo(LocalDate.of(2026, 10, 31));
    }

    @Override
    protected void modify(ManuscriptSize entity) {
        entity.setBookWords(1200);
        entity.setLeafUuid(null);
        entity.setBaseline(false);
        entity.setReconstructed(true);
    }

    @Override
    protected void assertModified(ManuscriptSize loaded) {
        assertThat(loaded.getBookWords()).isEqualTo(1200);
        assertThat(loaded.getLeafUuid()).isNull();
        assertThat(loaded.isBaseline()).isFalse();
        assertThat(loaded.isReconstructed()).isTrue();
        assertThat(loaded.getBranchWords()).isEqualTo(600);
    }
}
