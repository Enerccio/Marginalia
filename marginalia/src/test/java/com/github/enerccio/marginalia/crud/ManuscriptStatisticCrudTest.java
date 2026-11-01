package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptStatistic;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ManuscriptStatisticCrudTest extends OwnedCrudContract<ManuscriptStatistic> {

    @Autowired
    private ManuscriptStatisticService statisticService;

    @Override
    protected OwnedService<ManuscriptStatistic, ?> service() {
        return statisticService;
    }

    @Override
    protected ManuscriptStatistic newEntity() {
        ManuscriptStatistic statistic = new ManuscriptStatistic();
        statistic.setBookUuid(java.util.UUID.randomUUID().toString());
        statistic.setBookName("The Long Night");
        statistic.setFolderPath("Sci-fi/Expanse");
        statistic.setDay(LocalDate.of(2026, 10, 31));
        statistic.setAgent(true);
        statistic.setCounts(new StatisticCounts(100, 20, 2, 7, 11, 3, 1, 1, 4));
        statistic.setReconstructed(true);
        statistic.setLastChange(new Date());
        return statistic;
    }

    @Override
    protected void assertCreated(ManuscriptStatistic loaded) {
        assertThat(loaded.getBookName()).isEqualTo("The Long Night");
        assertThat(loaded.getFolderPath()).isEqualTo("Sci-fi/Expanse");
        assertThat(loaded.getDay()).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(loaded.isAgent()).isTrue();
        assertThat(loaded.getCounts()).isEqualTo(new StatisticCounts(100, 20, 2, 7, 11, 3, 1, 1, 4));
        assertThat(loaded.isReconstructed()).isTrue();
        assertThat(loaded.getLastChange()).isNotNull();
    }

    @Override
    protected void modify(ManuscriptStatistic entity) {
        entity.setBookName("The Longer Night");
        entity.setCounts(new StatisticCounts(1, 2, 3, 4, 5, 6, 7, 8, 9));
        entity.setAgent(false);
    }

    @Override
    protected void assertModified(ManuscriptStatistic loaded) {
        assertThat(loaded.getBookName()).isEqualTo("The Longer Night");
        assertThat(loaded.getCounts()).isEqualTo(new StatisticCounts(1, 2, 3, 4, 5, 6, 7, 8, 9));
        assertThat(loaded.isAgent()).isFalse();
        assertThat(loaded.getDay()).isEqualTo(LocalDate.of(2026, 10, 31));
    }
}
