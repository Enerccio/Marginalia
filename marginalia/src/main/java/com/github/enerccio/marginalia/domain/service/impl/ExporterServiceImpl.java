package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.export.*;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExporterServiceImpl implements ExporterService, InitializingBean {

    private final List<Exporter> exporters = new CopyOnWriteArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        registerExporter(new TxtExporter());
        registerExporter(new HtmlExporter());
        registerExporter(new DocxExporter());
        registerExporter(new PdfExporter());
        registerExporter(new EpubExporter());
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
}
