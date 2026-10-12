package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.ExtensionService;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.extensions.ExtensionHasLocalization;
import com.github.enerccio.marginalia.extensions.MarginaliaExtension;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Language;
import com.github.enerccio.marginalia.loc.LocalizationBundle;
import com.github.enerccio.marginalia.loc.LocalizationBundleBase;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Activator of the test bundles built by {@link OsgiServiceImplTest}. The class is loaded from the test classpath
 * (boot delegation), so every bundle shares it; the bundle's manifest headers control what it does.
 * <ul>
 *     <li>{@code X-Test-Fail-Start} - the activator throws</li>
 *     <li>{@code X-Test-Fail-Load} - {@code onExtensionLoad} throws</li>
 *     <li>{@code X-Test-Language} - the extension brings a localization bundle of the language with this code</li>
 * </ul>
 * Loads and unloads are counted per {@code symbolicName:version}.
 */
public class TestExtensionActivator implements BundleActivator {

    public static final Map<String, Integer> LOADED = new ConcurrentHashMap<>();
    public static final Map<String, Integer> UNLOADED = new ConcurrentHashMap<>();

    public static String key(Bundle bundle) {
        return bundle.getSymbolicName() + ":" + bundle.getVersion();
    }

    @Override
    public void start(BundleContext context) {
        if (context.getBundle().getHeaders().get("X-Test-Fail-Start") != null) {
            throw new IllegalStateException("activator of " + key(context.getBundle()) + " failed");
        }
        context.registerService(MarginaliaExtension.class, new TestExtension(), null);
    }

    @Override
    public void stop(BundleContext context) {
    }

    public static class TestExtension implements MarginaliaExtension, ExtensionHasLocalization {

        private Bundle bundle;

        @Override
        public List<LocalizationBundle> getLocalizationBundles() {
            String code = bundle == null ? null : bundle.getHeaders().get("X-Test-Language");
            return code == null ? List.of() : List.of(new TestLanguageBundle(Language.of(code)));
        }

        @Override
        public void onExtensionLoad(Bundle bundle, OsgiService parentService, ExtensionService extensionService) {
            this.bundle = bundle;
            LOADED.merge(key(bundle), 1, Integer::sum);
            if (bundle.getHeaders().get("X-Test-Fail-Load") != null) {
                throw new IllegalStateException("load of " + key(bundle) + " failed");
            }
        }

        @Override
        public void onExtensionUnload(Bundle b, OsgiService osgiService, ExtensionService extensionService) {
            UNLOADED.merge(key(b), 1, Integer::sum);
        }
    }

    public static class TestLanguageBundle extends LocalizationBundleBase {

        private final Language language;

        TestLanguageBundle(Language language) {
            this.language = language;
        }

        @Override
        protected void loadMessages() {
            setValue(L.LABEL_YES, "yes in " + language);
        }

        @Override
        public Language getLanguage() {
            return language;
        }

        @Override
        public String getDisplayName() {
            return "Test " + language;
        }

        @Override
        public Locale getLocale() {
            return Locale.ROOT;
        }

        @Override
        public DateFormat getDateFormat() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }

        @Override
        public DateFormat getHourFormat() {
            return new SimpleDateFormat("HH:mm");
        }

        @Override
        public DateFormat getDateHourFormat() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
    }
}
