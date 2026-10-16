package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.ManualService;
import com.github.enerccio.marginalia.domain.service.manual.ManualHit;
import com.github.enerccio.marginalia.domain.service.manual.ManualIndex;
import com.github.enerccio.marginalia.domain.service.manual.ManualPage;
import com.github.enerccio.marginalia.domain.service.manual.ManualParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ManualServiceImpl implements ManualService {
    private static final Logger log = LoggerFactory.getLogger(ManualServiceImpl.class);

    private static final String ROOT = "manual/";

    private volatile Map<String, ManualPage> pages = Map.of();
    private volatile ManualIndex index = new ManualIndex(List.of());

    /** Reads the manual from the classpath; called once by Spring. A missing or broken manual only leaves the help empty. */
    public void load() {
        Map<String, ManualPage> loaded = new LinkedHashMap<>();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver(getClass().getClassLoader())
                    .getResources("classpath*:" + ROOT + "**/*.md");
            for (Resource resource : resources) {
                String url = resource.getURL().toString();
                String path = url.substring(url.lastIndexOf("/" + ROOT) + 1 + ROOT.length());
                try (var in = resource.getInputStream()) {
                    String source = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    loaded.put(path, ManualParser.parse(path, source));
                } catch (Exception e) {
                    log.warn("Manual page {} could not be read", path, e);
                }
            }
        } catch (Exception e) {
            log.warn("The manual could not be loaded", e);
        }
        List<ManualPage> sorted = loaded.values().stream().sorted(Comparator.comparing(ManualPage::path)).toList();
        Map<String, ManualPage> byPath = new LinkedHashMap<>();
        sorted.forEach(page -> byPath.put(page.path(), page));
        index = new ManualIndex(sorted);
        pages = Map.copyOf(byPath);
        log.info("Manual loaded: {} pages", sorted.size());
    }

    @Override
    public List<ManualPage> listPages() {
        return pages.values().stream().sorted(Comparator.comparing(ManualPage::path)).toList();
    }

    @Override
    public Optional<ManualPage> getPage(String path) {
        if (path == null) {
            return Optional.empty();
        }
        String p = path.strip();
        int anchor = p.indexOf('#');
        if (anchor >= 0) {
            p = p.substring(0, anchor);
        }
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        if (!p.endsWith(".md")) {
            p = p + ".md";
        }
        return Optional.ofNullable(pages.get(p));
    }

    @Override
    public List<ManualHit> search(String query, int limit) {
        return query == null ? List.of() : index.search(query, limit);
    }
}
