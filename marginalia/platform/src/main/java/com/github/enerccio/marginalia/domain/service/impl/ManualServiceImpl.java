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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ManualServiceImpl implements ManualService {
    private static final Logger log = LoggerFactory.getLogger(ManualServiceImpl.class);

    private static final String ROOT = "manual/";
    /** The translations, {@code manual-translations/<language>/user/<path of the page>}. */
    private static final String TRANSLATIONS = "manual-translations/";
    /** What a language code of a translation looks like: it goes into a classpath pattern. */
    private static final Pattern LANGUAGE = Pattern.compile("[a-z]{2,3}(-[a-z0-9]{2,8})?");

    private volatile Map<String, ManualPage> pages = Map.of();
    private volatile ManualIndex index = new ManualIndex(List.of());
    /** The translated pages of a language, read when the language is first asked for; empty for a language without translations. */
    private final Map<String, Map<String, ManualPage>> translations = new ConcurrentHashMap<>();

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
        translations.clear();
        log.info("Manual loaded: {} pages", sorted.size());
    }

    @Override
    public List<ManualPage> listPages() {
        return pages.values().stream().sorted(Comparator.comparing(ManualPage::path)).toList();
    }

    @Override
    public Optional<ManualPage> getPage(String path) {
        String key = pathKey(path);
        return key == null ? Optional.empty() : Optional.ofNullable(pages.get(key));
    }

    /** The path as the pages are keyed: no leading slash, no anchor, with {@code .md}; null for none. */
    private static String pathKey(String path) {
        if (path == null) {
            return null;
        }
        String p = path.strip();
        int anchor = p.indexOf('#');
        if (anchor >= 0) {
            p = p.substring(0, anchor);
        }
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        return p.endsWith(".md") ? p : p + ".md";
    }

    @Override
    public List<ManualPage> listPages(String language) {
        Map<String, ManualPage> translated = translated(language);
        if (translated.isEmpty()) {
            return listPages();
        }
        return pages.values().stream().map(page -> translated.getOrDefault(page.path(), page))
                .sorted(Comparator.comparing(ManualPage::path)).toList();
    }

    @Override
    public Optional<ManualPage> getPage(String language, String path) {
        String key = pathKey(path);
        if (key == null) {
            return Optional.empty();
        }
        ManualPage translated = translated(language).get(key);
        return translated != null ? Optional.of(translated) : Optional.ofNullable(pages.get(key));
    }

    @Override
    public List<ManualHit> search(String language, String query, int limit) {
        List<ManualHit> hits = search(query, limit);
        Map<String, ManualPage> translated = translated(language);
        if (translated.isEmpty()) {
            return hits;
        }
        return hits.stream().map(hit -> inLanguage(hit, translated)).toList();
    }

    /**
     * The same section of the translated page: a translation keeps the headings of the page in their order, so the section is the one
     * at the same place; a page that does not match (an old translation of a page that has changed) stays English.
     */
    private ManualHit inLanguage(ManualHit hit, Map<String, ManualPage> translated) {
        ManualPage english = pages.get(hit.section().pagePath());
        ManualPage page = translated.get(hit.section().pagePath());
        if (english == null || page == null || english.sections().size() != page.sections().size()) {
            return hit;
        }
        int at = english.sections().indexOf(hit.section());
        return at < 0 ? hit : new ManualHit(page.sections().get(at), hit.score());
    }

    /** The translated pages of a language by path, empty when the language has none. */
    private Map<String, ManualPage> translated(String language) {
        if (language == null) {
            return Map.of();
        }
        String code = language.strip().toLowerCase(Locale.ROOT);
        if (code.equals(ManualPage.ENGLISH) || !LANGUAGE.matcher(code).matches()) {
            return Map.of();
        }
        return translations.computeIfAbsent(code, this::loadTranslation);
    }

    private Map<String, ManualPage> loadTranslation(String code) {
        Map<String, ManualPage> loaded = new LinkedHashMap<>();
        String marker = "/" + TRANSLATIONS + code + "/user/";
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver(getClass().getClassLoader())
                    .getResources("classpath*:" + TRANSLATIONS + code + "/user/**/*.md");
            for (Resource resource : resources) {
                String url = resource.getURL().toString();
                String path = url.substring(url.lastIndexOf(marker) + marker.length());
                // a page that is gone from the English manual is not offered, the translation is only of what is there
                if (!pages.containsKey(path)) {
                    continue;
                }
                try (var in = resource.getInputStream()) {
                    loaded.put(path, ManualParser.parse(code, path, new String(in.readAllBytes(), StandardCharsets.UTF_8)));
                } catch (Exception e) {
                    log.warn("Translated manual page {} ({}) could not be read", path, code, e);
                }
            }
        } catch (Exception e) {
            log.warn("The translation of the manual into {} could not be loaded", code, e);
        }
        log.info("Manual in {}: {} translated pages", code, loaded.size());
        return Map.copyOf(loaded);
    }

    @Override
    public List<ManualHit> search(String query, int limit) {
        return query == null ? List.of() : index.search(query, limit);
    }
}
