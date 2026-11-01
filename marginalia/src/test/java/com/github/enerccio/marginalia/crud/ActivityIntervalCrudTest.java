package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.model.impl.ActivityInterval;
import com.github.enerccio.marginalia.domain.service.ActivityIntervalService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityIntervalCrudTest extends OwnedCrudContract<ActivityInterval> {

    private static final Date START = new Date(1_700_000_000_000L);
    private static final Date END = new Date(1_700_000_060_000L);

    @Autowired
    private ActivityIntervalService intervalService;

    @Override
    protected OwnedService<ActivityInterval, ?> service() {
        return intervalService;
    }

    @Override
    protected ActivityInterval newEntity() {
        ActivityInterval interval = new ActivityInterval();
        interval.setBookUuid(java.util.UUID.randomUUID().toString());
        interval.setBookName("The Long Night");
        interval.setFolderPath("");
        interval.setKind(ActivityKind.GENERATE);
        interval.setAgent(true);
        interval.setDay(LocalDate.of(2023, 11, 14));
        interval.setStartTime(START);
        interval.setEndTime(END);
        return interval;
    }

    @Override
    protected void assertCreated(ActivityInterval loaded) {
        assertThat(loaded.getKind()).isEqualTo(ActivityKind.GENERATE);
        assertThat(loaded.isAgent()).isTrue();
        assertThat(loaded.getDay()).isEqualTo(LocalDate.of(2023, 11, 14));
        assertThat(loaded.getStartTime().getTime()).isEqualTo(START.getTime());
        assertThat(loaded.getEndTime().getTime()).isEqualTo(END.getTime());
        assertThat(loaded.getFolderPath()).isEmpty();
    }

    @Override
    protected void modify(ActivityInterval entity) {
        entity.setKind(ActivityKind.RUN);
        entity.setEndTime(new Date(END.getTime() + 60_000));
    }

    @Override
    protected void assertModified(ActivityInterval loaded) {
        assertThat(loaded.getKind()).isEqualTo(ActivityKind.RUN);
        assertThat(loaded.getEndTime().getTime()).isEqualTo(END.getTime() + 60_000);
        assertThat(loaded.getStartTime().getTime()).isEqualTo(START.getTime());
    }
}
