package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import com.github.enerccio.marginalia.utils.MarkdownArchive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.BooleanSupplier;

public class ArchiveImportServiceImpl implements ArchiveImportService {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Override
    @NoTx
    public ImportResult<Manuscript> insertParts(Manuscript manuscript, Insertion place, List<String> texts, RevisionDetail source,
                                                BooleanSupplier stopped, Runnable onStored) throws Exception {
        // every part is a transaction of its own; at the end a part is stored as the active leaf of the book, in the middle the
        // part is put between two parts and what followed the place follows it, so the next part goes after this one
        RevisionOrigin origin = RevisionOrigin.human(source, false).uncounted();
        ChatMessage last = place.end() ? manuscript.getActiveLeaf() : place.after();
        int imported = 0;
        for (String text : texts) {
            if (stopped.getAsBoolean()) {
                break;
            }
            last = place.end() ? chatMessageService.createPart(manuscript, last, text, null, origin)
                    : chatMessageService.insertPart(manuscript, last, text, null, origin);
            imported++;
            onStored.run();
        }
        return new ImportResult<>(manuscriptService.find(manuscript), imported, texts.size());
    }

    @Override
    @NoTx
    public ImportResult<Lorebook> importLorebook(MarkdownArchive.Content content, String name, List<String> names,
                                                 BooleanSupplier stopped, Runnable onStored) throws Exception {
        List<MarkdownArchive.Item> items = content.items();
        List<String> entryNames = content.itemNames();

        String bookName = StringUtils.trimToEmpty(name);
        if (bookName.toLowerCase().endsWith(".zip")) {
            bookName = bookName.substring(0, bookName.length() - 4).trim();
        }
        if (bookName.isEmpty()) {
            bookName = "Imported Archive";
        }

        Lorebook lorebook = new Lorebook();
        lorebook.setName(bookName);
        lorebook.setEnabled(true);
        lorebook = lorebookService.save(lorebook);

        int imported = 0;
        for (int i = 0; i < items.size(); i++) {
            if (stopped.getAsBoolean()) {
                break;
            }
            String given = names != null && i < names.size() ? StringUtils.trimToEmpty(names.get(i)) : "";
            LorebookEntry entry = new LorebookEntry();
            entry.setLorebook(lorebook);
            entry.setName(given.isEmpty() ? entryNames.get(i) : given);
            entry.setPayload(items.get(i).text());
            entry.setOrder(i + 1);
            // without a filter the entry is always sent, which for a whole book of chapters is not wanted
            entry.setEnabled(false);
            lorebookEntryService.save(entry);
            imported++;
            onStored.run();
        }
        return new ImportResult<>(lorebook, imported, items.size());
    }
}
