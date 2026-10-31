package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import com.github.enerccio.marginalia.domain.repository.ManuscriptSizeRepository;
import com.github.enerccio.marginalia.domain.service.ManuscriptSizeService;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class ManuscriptSizeServiceImpl extends OwnedServiceImpl<ManuscriptSize, ManuscriptSizeRepository>
        implements ManuscriptSizeService {

    private static final Logger log = LoggerFactory.getLogger(ManuscriptSizeServiceImpl.class);

    @Autowired
    @Lazy
    private ManuscriptStatisticService statisticService;

    @Override
    @CommonTx
    public void set(Manuscript book, LocalDate day, long bookWords, long bookTokens, long branchWords, long branchTokens,
                    String leafUuid, boolean baseline) {
        if (book == null || book.getUuid() == null || book.getOwner() == null || day == null) {
            return;
        }
        try {
            ManuscriptSize row = getRepository().find(book.getOwner(), book.getUuid(), day);
            if (row == null) {
                row = new ManuscriptSize();
                row.setOwner(book.getOwner());
                row.setBookUuid(book.getUuid());
                row.setDay(day);
            }
            row.setBookName(book.getName());
            row.setFolderPath(statisticService.folderPathOf(book));
            row.setBookWords(Math.max(0, bookWords));
            row.setBookTokens(Math.max(0, bookTokens));
            row.setBranchWords(Math.max(0, branchWords));
            row.setBranchTokens(Math.max(0, branchTokens));
            row.setLeafUuid(leafUuid);
            row.setBaseline(baseline);
            row.setReconstructed(false);
            row.setLastChange(new Date());
            save(row);
        } catch (Exception e) {
            log.warn("Failed to set the size of book {}", book.getUuid(), e);
        }
    }

    @Override
    @CommonTxReadOnly
    public ManuscriptSize sizeAt(String bookUuid, LocalDate day) throws Exception {
        return getRepository().findAtOrBefore(currentUser, bookUuid, day);
    }

    @Override
    @CommonTxReadOnly
    public ManuscriptSize sizeBefore(String bookUuid, LocalDate day) throws Exception {
        return getRepository().findBefore(currentUser, bookUuid, day);
    }

    @Override
    @CommonTxReadOnly
    public List<ManuscriptSize> sizesAt(LocalDate day) throws Exception {
        return getRepository().findAllAtOrBefore(currentUser, day);
    }

    @Override
    @CommonTxReadOnly
    public List<ManuscriptSize> findRange(String bookUuid, LocalDate from, LocalDate to) throws Exception {
        return getRepository().findRange(currentUser, bookUuid, from, to);
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
