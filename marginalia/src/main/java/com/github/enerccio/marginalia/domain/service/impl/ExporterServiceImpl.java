package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.export.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExporterServiceImpl implements ExporterService, InitializingBean {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private final List<Exporter> exporters = new CopyOnWriteArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        registerExporter(new TxtExporter());
        registerExporter(new MarkdownExporter());
        registerExporter(new HtmlExporter());
        registerExporter(new DocxExporter());
        registerExporter(new PdfExporter());
        registerExporter(new EpubExporter());
        registerExporter(new Ao3HtmlExporter());
        registerExporter(new FrontMatterMarkdownExporter());
        registerExporter(new SillyTavernExporter());
        registerExporter(new ObsidianExporter());
        registerExporter(new LatexExporter());
    }

    @Override
    public void registerExporter(Exporter exporter) {
        exporters.add(exporter);
    }

    @Override
    public void unregisterExporter(Exporter exporter) {
        exporters.remove(exporter);
    }

    @Override
    public List<Exporter> getExporters() {
        return List.copyOf(exporters);
    }

    @Override
    @CommonTxReadOnly
    public List<ExportLoreEntry> collectLore(Manuscript manuscript) throws Exception {
        List<ExportLoreEntry> lore = new ArrayList<>();
        if (manuscript.getLorebook() == null) {
            return lore;
        }
        for (Lorebook lorebook : lorebookService.getLorebookTree(manuscript.getLorebook())) {
            if (!lorebook.isEnabled()) {
                continue;
            }
            for (LorebookEntry entry : lorebookEntryService.getEntriesForLorebook(lorebook)) {
                if (entry.isEnabled()) {
                    lore.add(new ExportLoreEntry(lorebook.getName(), entry.getName(), entry.getPayload(), entry.getCachedTags(), entry.getComment()));
                }
            }
        }
        return lore;
    }
}
