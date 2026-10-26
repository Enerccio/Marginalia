package com.github.enerccio.marginalia;

import com.github.enerccio.marginalia.loc.Language;
import com.github.enerccio.marginalia.loc.LocalizationBundleEN;
import com.github.enerccio.marginalia.loc.LocalizationImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The settings that an environment variable (or {@code -D}) can override, see {@code container-config.xml}.
 */
class ConfigurationSettingsTest {

    /**
     * The settings only, the folders (and the secret key) are not created.
     */
    private static class NoFolders extends Configuration {
        @Override
        public void afterPropertiesSet() {
        }
    }

    /**
     * The placeholders of the {@code configuration} bean as in {@code container-config.xml}, resolved against the
     * given environment and file.
     */
    private Configuration configure(Map<String, Object> environment, Map<String, Object> file) {
        try (GenericApplicationContext context = new GenericApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(
                    new SystemEnvironmentPropertySource("test-environment", environment));
            PropertySourcesPlaceholderConfigurer placeholders = new PropertySourcesPlaceholderConfigurer();
            placeholders.setEnvironment(context.getEnvironment());
            placeholders.setLocalOverride(false);
            placeholders.setProperties(toProperties(file));
            context.addBeanFactoryPostProcessor(placeholders);
            context.registerBean("configuration", Configuration.class, NoFolders::new, definition -> {
                definition.getPropertyValues().add("folder", "${marginalia.data:}");
                definition.getPropertyValues().add("allowPersistentLogin",
                        "${marginalia.allow-persistent-login:${allowPersistentLogin:true}}");
                definition.getPropertyValues().add("persistentLoginInfoTTL",
                        "${marginalia.persistent-login-ttl:${persistentLoginTTL:2592000}}");
                definition.getPropertyValues().add("trustedProxies",
                        "${marginalia.trusted-proxies:${trustedProxies:}}");
            });
            context.refresh();
            return context.getBean(Configuration.class);
        }
    }

    private static java.util.Properties toProperties(Map<String, Object> file) {
        java.util.Properties properties = new java.util.Properties();
        properties.putAll(file);
        return properties;
    }

    @Test
    void defaultsWithoutAnySetting() {
        Configuration configuration = configure(Map.of(), Map.of());

        assertThat(configuration.getFolder()).isNull();
        assertThat(configuration.isAllowPersistentLogin()).isTrue();
        assertThat(configuration.getPersistentLoginInfoTTL()).isEqualTo(2592000L);
        assertThat(configuration.getClientAddressResolver().hasTrustedProxies()).isFalse();
    }

    @Test
    void configurationFileIsTheFallback() {
        Configuration configuration = configure(Map.of(),
                Map.of("allowPersistentLogin", "false", "persistentLoginTTL", "60", "trustedProxies", "127.0.0.1"));

        assertThat(configuration.isAllowPersistentLogin()).isFalse();
        assertThat(configuration.getPersistentLoginInfoTTL()).isEqualTo(60L);
        assertThat(configuration.getClientAddressResolver().hasTrustedProxies()).isTrue();
    }

    @Test
    void environmentVariablesOverrideTheFile() {
        Configuration configuration = configure(
                Map.of("MARGINALIA_DATA", "/srv/marginalia",
                        "MARGINALIA_ALLOW_PERSISTENT_LOGIN", "false",
                        "MARGINALIA_PERSISTENT_LOGIN_TTL", "3600",
                        "MARGINALIA_TRUSTED_PROXIES", "10.0.0.0/8"),
                Map.of("allowPersistentLogin", "true", "persistentLoginTTL", "60", "trustedProxies", ""));

        assertThat(configuration.getFolder()).isEqualTo(new File("/srv/marginalia"));
        assertThat(configuration.isAllowPersistentLogin()).isFalse();
        assertThat(configuration.getPersistentLoginInfoTTL()).isEqualTo(3600L);
        assertThat(configuration.getClientAddressResolver().isTrusted("10.1.2.3")).isTrue();
    }

    @Test
    void emptyVariableKeepsTheDefault() {
        Configuration configuration = configure(
                Map.of("MARGINALIA_DATA", "", "MARGINALIA_PERSISTENT_LOGIN_TTL", ""), Map.of());

        assertThat(configuration.getFolder()).isNull();
        assertThat(configuration.getPersistentLoginInfoTTL()).isEqualTo(60 * 60 * 24 * 30L);
    }

    @Test
    void defaultLanguageFromCode() {
        LocalizationImpl localization = new LocalizationImpl();
        localization.setBundles(List.of(new LocalizationBundleEN()));

        localization.setDefaultLanguageCode("");
        assertThat(localization.getDefaultLanguage()).isEqualTo(Language.EN);

        localization.setDefaultLanguageCode(" EN ");
        assertThat(localization.getDefaultLanguage()).isEqualTo(Language.EN);

        localization.setDefaultLanguageCode("xx");
        assertThatThrownBy(localization::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("xx");
    }
}
