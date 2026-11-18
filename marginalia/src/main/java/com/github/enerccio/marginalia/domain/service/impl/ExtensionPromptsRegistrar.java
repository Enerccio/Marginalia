package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasLocalizedPrompts;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.loc.StoryLocalization;
import com.github.enerccio.marginalia.loc.Localization;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registers the built-in prompts of the extensions that bring them ({@link ExtensionHasLocalizedPrompts}) when they
 * are loaded, and removes them when they are unloaded. Without the OSGi service (tests) it does nothing.
 */
public class ExtensionPromptsRegistrar implements OsgiService.ExtensionListener {

    private static final Logger log = LoggerFactory.getLogger(ExtensionPromptsRegistrar.class);

    @Autowired
    private StoryLocalization localization;

    @Autowired
    private ObjectProvider<OsgiService> osgiService;

    private final Map<MarginaliaExtension, List<Defaults>> registered = new ConcurrentHashMap<>();

    /**
     * Listens to the extensions, then takes the ones loaded already (one loaded in between is handled twice, the
     * second time does nothing).
     */
    public void install() {
        OsgiService service = osgiService.getIfAvailable();
        if (service == null) {
            return;
        }
        service.addExtensionListener(this);
        for (OsgiService.LoadedExtension loaded : service.getLoadedExtensions()) {
            extensionLoaded(loaded.bundle(), loaded.extension());
        }
    }

    @Override
    public void extensionLoaded(Bundle bundle, MarginaliaExtension extension) {
        if (!(extension instanceof ExtensionHasLocalizedPrompts hasPrompts)) {
            return;
        }
        List<Defaults> added = new ArrayList<>();
        if (registered.putIfAbsent(extension, added) != null) {
            return;
        }
        try {
            for (Defaults texts : hasPrompts.getPromptDefaults()) {
                try {
                    localization.registerDefaults(texts);
                    added.add(texts);
                } catch (RuntimeException e) {
                    log.error("Extension {} prompts of {} are not registered: {}", extension.getClass().getName(),
                            texts.getLanguage(), e.getMessage());
                }
            }
        } catch (RuntimeException e) {
            log.error("Extension {} failed to provide its prompts: {}", extension.getClass().getName(), e.getMessage(), e);
        }
    }

    @Override
    public void extensionUnloaded(Bundle bundle, MarginaliaExtension extension) {
        List<Defaults> added = registered.remove(extension);
        if (added != null) {
            added.forEach(localization::unregisterDefaults);
        }
    }
}
