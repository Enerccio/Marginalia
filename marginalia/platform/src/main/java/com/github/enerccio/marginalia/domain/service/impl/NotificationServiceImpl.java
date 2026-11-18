package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.settings.NotificationSettings;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.NotificationService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class NotificationServiceImpl implements NotificationService, InitializingBean {

    private static final String BUILT_IN = "marginalia";
    private static final String TITLE = "Marginalia";

    @Autowired
    private SettingService settingService;

    @Autowired
    private Localization loc;

    @Autowired
    private User currentUser;

    private final Map<String, Source> sources = new LinkedHashMap<>();
    private final Map<String, String> owners = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        // the names are asked when the list is shown, in the language of the session that shows it
        register(BUILT_IN, new Source(GENERATION, () -> loc.getValue(L.LABEL_STORY_GENERATION)));
        register(BUILT_IN, new Source(ANNOTATE, () -> loc.getValue(L.LABEL_ANNOTATE_WITH_AI), true));
        register(BUILT_IN, new Source(REDRAFT, () -> loc.getValue(L.LABEL_REDRAFT), true));
        register(BUILT_IN, new Source(IMPERSONATE, () -> loc.getValue(L.LABEL_IMPERSONATE)));
        register(BUILT_IN, new Source(SUMMARY, () -> loc.getValue(L.LABEL_SUMMARY)));
        register(BUILT_IN, new Source(RESUMMARIZE, () -> loc.getValue(L.LABEL_RESUMMARIZE_ALL_SUMMARIES)));
        register(BUILT_IN, new Source(HELP_ASSISTANT, () -> loc.getValue(L.LABEL_HELP_ASSISTANT)));
    }

    @Override
    public synchronized void register(String owner, Source source) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(source.id(), "id");
        Objects.requireNonNull(source.name(), "name");
        // a replaced source goes to the end, as a new one
        sources.remove(source.id());
        sources.put(source.id(), source);
        owners.put(source.id(), owner);
    }

    @Override
    public synchronized void unregister(String id) {
        sources.remove(id);
        owners.remove(id);
    }

    @Override
    public synchronized void unregisterAll(String owner) {
        new ArrayList<>(owners.entrySet()).stream().filter(entry -> Objects.equals(owner, entry.getValue()))
                .map(Map.Entry::getKey).forEach(this::unregister);
    }

    @Override
    public synchronized List<Source> sources() {
        return List.copyOf(sources.values());
    }

    @Override
    public List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        for (Source source : sources()) {
            String name = nameOf(source);
            entries.add(new Entry(source.id(), name, source.id(), false));
            if (source.hasAutomatic()) {
                entries.add(new Entry(source.id() + AUTOMATIC_SUFFIX,
                        name + " (" + loc.getValue(L.LABEL_AUTOMATIC).toLowerCase(Locale.ROOT) + ")", source.id(), true));
            }
        }
        return entries;
    }

    @Override
    @CommonTxReadOnly
    public boolean isEnabled() throws Exception {
        return Boolean.TRUE.equals(read().getEnabled());
    }

    @Override
    @CommonTxReadOnly
    public Set<String> getChosen() throws Exception {
        return new LinkedHashSet<>(read().getChosen());
    }

    @Override
    @CommonTx
    public void save(boolean enabled, Collection<String> chosen) throws Exception {
        NotificationSettings settings = settingService.getOrCreate(NotificationSettings.class);
        Set<String> known = new HashSet<>();
        entries().forEach(entry -> known.add(entry.key()));
        Set<String> result = new LinkedHashSet<>();
        // the choice for a source that is not loaded now (an extension that is stopped) stays
        settings.getChosen().stream().filter(key -> !known.contains(key)).forEach(result::add);
        chosen.stream().filter(known::contains).forEach(result::add);
        settings.setEnabled(enabled);
        settings.setChosen(new ArrayList<>(result));
        settingService.save(settings);
    }

    @Override
    @CommonTxReadOnly
    public Notice prepare(String sourceId, boolean automatic, Outcome outcome, String detail) throws Exception {
        Source source;
        synchronized (this) {
            source = sources.get(sourceId);
        }
        if (source == null) {
            return null;
        }
        NotificationSettings settings = read();
        if (!Boolean.TRUE.equals(settings.getEnabled())) {
            return null;
        }
        // a source without an automatic run has one entry, whatever the run says about itself
        boolean auto = automatic && source.hasAutomatic();
        String key = auto ? sourceId + AUTOMATIC_SUFFIX : sourceId;
        if (!settings.getChosen().contains(key)) {
            return null;
        }
        String name = nameOf(source);
        if (auto) {
            name += " (" + loc.getValue(L.LABEL_AUTOMATIC).toLowerCase(Locale.ROOT) + ")";
        }
        return notice(String.format(loc.getValue(outcome == Outcome.FAILED ? L.MSG_NOTIFICATION_FAILED : L.MSG_NOTIFICATION_FINISHED), name),
                detail, key);
    }

    @Override
    public Notice test() {
        return new Notice(TITLE, loc.getValue(L.MSG_NOTIFICATION_TEST), "test");
    }

    private static Notice notice(String text, String detail, String key) {
        return new Notice(TITLE, StringUtils.isBlank(detail) ? text : text + "\n" + detail.strip(), key);
    }

    private static String nameOf(Source source) {
        String name;
        try {
            name = source.name().get();
        } catch (RuntimeException e) {
            // the name comes from an extension, its failure must not take the list of the settings with it
            name = null;
        }
        return StringUtils.isBlank(name) ? source.id() : name;
    }

    private NotificationSettings read() throws Exception {
        return settingService.findOrBlank(NotificationSettings.class, currentUser);
    }
}
