package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.components.ActivityRow;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.model.impl.ActivityInterval;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ActivityIntervalRepository;
import com.github.enerccio.marginalia.domain.service.ActivityIntervalService;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class ActivityIntervalServiceImpl extends OwnedServiceImpl<ActivityInterval, ActivityIntervalRepository>
        implements ActivityIntervalService {

    @Autowired
    @Lazy
    private ManuscriptStatisticService statisticService;

    @Override
    @CommonTx
    public ActivityInterval start(Manuscript book, ActivityKind kind, boolean agent, Date startTime) throws Exception {
        ActivityInterval interval = new ActivityInterval();
        interval.setOwner(book.getOwner());
        interval.setBookUuid(book.getUuid());
        interval.setBookName(book.getName());
        interval.setFolderPath(statisticService.folderPathOf(book));
        interval.setKind(kind);
        interval.setAgent(agent);
        interval.setDay(startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        interval.setStartTime(startTime);
        interval.setEndTime(startTime);
        return save(interval);
    }

    @Override
    @CommonTx
    public void extend(Long id, Date endTime) throws Exception {
        getRepository().moveEnd(id, endTime);
    }

    @Override
    @CommonTxReadOnly
    public List<ActivityRow> findRows(StatisticFilter filter, Collection<ActivityKind> kinds) throws Exception {
        return getRepository().findRows(currentUser, filter, kinds);
    }

    @Override
    @CommonTx
    public void clearBook(String bookUuid) throws Exception {
        getRepository().deleteByBook(currentUser, bookUuid);
    }

    @Override
    @CommonTx
    public void clearAll() throws Exception {
        getRepository().deleteAll(currentUser);
    }
}
