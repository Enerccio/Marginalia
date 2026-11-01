package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.StylesSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class PartStyleServiceImpl implements PartStyleService {

    private static final Gson GSON = new Gson();

    @Autowired
    private SettingService settingService;

    @Autowired
    private PartStyleRegistry registry;

    @Autowired
    private User currentUser;

    @Override
    @CommonTxReadOnly
    public List<PartStyle> list() throws Exception {
        List<PartStyle> all = new ArrayList<>(registry.getAll());
        all.addAll(userStyles(read()));
        return all;
    }

    @Override
    @CommonTxReadOnly
    public List<PartStyle> listForBooks() throws Exception {
        return list().stream().filter(style -> !style.isForExport()).toList();
    }

    @Override
    @CommonTxReadOnly
    public List<PartStyle> listForExport() throws Exception {
        return list().stream().filter(PartStyle::isForExport).toList();
    }

    @Override
    @CommonTxReadOnly
    public PartStyle find(String id) throws Exception {
        return find(read(), id);
    }

    @Override
    @CommonTxReadOnly
    public PartStyle findChosen(Manuscript manuscript) throws Exception {
        return findChosen(read(), manuscript);
    }

    @Override
    @CommonTxReadOnly
    public PartStyle resolve(Manuscript manuscript) throws Exception {
        StylesSetting setting = read();
        PartStyle chosen = findChosen(setting, manuscript);
        return chosen != null ? chosen : getDefault(setting);
    }

    private PartStyle findChosen(StylesSetting setting, Manuscript manuscript) {
        if (manuscript == null) {
            return null;
        }
        PartStyle chosen = find(setting, manuscript.getPartStyle());
        if (chosen != null && chosen.isForExport()) {
            // a style that was marked for exports after the book chose it
            chosen = null;
        }
        if (chosen == null && StringUtils.isBlank(manuscript.getPartStyle()) && manuscript.getShowBookStyles()) {
            chosen = registry.find(PartStyleRegistry.BOOK);
        }
        return chosen;
    }

    @Override
    @CommonTxReadOnly
    public PartStyle getDefault() throws Exception {
        return getDefault(read());
    }

    @Override
    @CommonTxReadOnly
    public String getDefaultId() throws Exception {
        return read().getDefaultStyle();
    }

    @Override
    @CommonTx
    public void setDefault(String id) throws Exception {
        StylesSetting setting = write();
        PartStyle style = StringUtils.isBlank(id) ? null : find(setting, id);
        if (StringUtils.isNotBlank(id) && style == null) {
            throw new IllegalArgumentException("No style " + id);
        }
        if (style != null && style.isForExport()) {
            throw new IllegalArgumentException("A style for exports can not be the style of the books");
        }
        setting.setDefaultStyle(StringUtils.isBlank(id) ? null : id);
        settingService.save(setting);
    }

    @Override
    @CommonTx
    public PartStyle save(PartStyle style) throws Exception {
        if (style.isBuiltin() || registry.isBuiltin(style.getId())) {
            throw new IllegalArgumentException("A built in style can not be changed");
        }
        if (StringUtils.isBlank(style.getName())) {
            throw new IllegalArgumentException("A style needs a name");
        }
        if (style.getCustomCss() != null && !PartStyle.isBalanced(style.getCustomCss())) {
            throw new IllegalArgumentException("The braces of the CSS are not balanced");
        }
        StylesSetting setting = write();
        if (StringUtils.isBlank(style.getId())) {
            style.setId(UUID.randomUUID().toString());
        }
        style.setName(style.getName().trim());
        // the stored form is a copy, later changes of the caller's object do not reach it
        setting.getStyles().add(style.getId(), GSON.toJsonTree(style));
        settingService.save(setting);
        return style;
    }

    @Override
    @CommonTx
    public PartStyle duplicate(String id, String name) throws Exception {
        PartStyle source = find(write(), id);
        if (source == null) {
            throw new IllegalArgumentException("No style " + id);
        }
        return save(source.copy(null, name));
    }

    @Override
    @CommonTx
    public void delete(String id) throws Exception {
        if (registry.isBuiltin(id)) {
            throw new IllegalArgumentException("A built in style can not be deleted");
        }
        StylesSetting setting = write();
        if (id == null || setting.getStyles().remove(id) == null) {
            throw new IllegalArgumentException("No style " + id);
        }
        if (id.equals(setting.getDefaultStyle())) {
            setting.setDefaultStyle(null);
        }
        settingService.save(setting);
    }

    // reads never write: a user who never touched a style has no row
    private StylesSetting read() throws Exception {
        return settingService.findOrBlank(StylesSetting.class, currentUser);
    }

    private StylesSetting write() throws Exception {
        return settingService.getOrCreate(StylesSetting.class);
    }

    private PartStyle getDefault(StylesSetting setting) {
        PartStyle style = find(setting, setting.getDefaultStyle());
        return style != null && !style.isForExport() ? style : registry.find(PartStyleRegistry.NORMAL);
    }

    private PartStyle find(StylesSetting setting, String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        PartStyle builtin = registry.find(id);
        if (builtin != null) {
            return builtin;
        }
        JsonElement stored = setting.getStyles().get(id);
        return stored == null || !stored.isJsonObject() ? null : parse(id, stored);
    }

    private List<PartStyle> userStyles(StylesSetting setting) {
        List<PartStyle> own = new ArrayList<>();
        JsonObject styles = setting.getStyles();
        for (Map.Entry<String, JsonElement> entry : styles.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                own.add(parse(entry.getKey(), entry.getValue()));
            }
        }
        own.sort(Comparator.comparing((PartStyle style) -> StringUtils.defaultString(style.getName()), String.CASE_INSENSITIVE_ORDER));
        return own;
    }

    private PartStyle parse(String id, JsonElement stored) {
        PartStyle style = GSON.fromJson(stored, PartStyle.class);
        style.setId(id);
        return style;
    }
}
