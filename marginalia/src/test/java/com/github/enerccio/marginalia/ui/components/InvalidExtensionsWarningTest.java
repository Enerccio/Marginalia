package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The red bar about invalid extensions: hidden when all is fine, the administrator gets the names on hover, the users
 * are asked to tell the administrator.
 */
class InvalidExtensionsWarningTest extends MarginaliaTestBase {

    // UI.setCurrent keeps a weak reference only
    private UI ui;

    @BeforeEach
    void currentUI() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private static Bundle bundle(String name, String version) {
        return (Bundle) Proxy.newProxyInstance(Bundle.class.getClassLoader(), new Class<?>[]{Bundle.class}, (proxy, method, args) ->
                switch (method.getName()) {
                    case "getSymbolicName" -> name;
                    case "getVersion" -> Version.parseVersion(version);
                    case "toString" -> name;
                    case "hashCode" -> name.hashCode();
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private static OsgiService osgi(List<Bundle> invalid) {
        return (OsgiService) Proxy.newProxyInstance(OsgiService.class.getClassLoader(), new Class<?>[]{OsgiService.class}, (proxy, method, args) -> {
            if (method.getName().equals("getInvalidExtensions")) {
                return invalid;
            }
            throw new UnsupportedOperationException(method.getName());
        });
    }

    private static InvalidExtensionsWarning warning(boolean administrator, List<Bundle> invalid) {
        InvalidExtensionsWarning warning = new InvalidExtensionsWarning(administrator).create();
        ReflectionTestUtils.setField(warning, "osgiService", osgi(invalid));
        warning.refresh();
        return warning;
    }

    private static String text(InvalidExtensionsWarning warning) {
        return warning.getChildren().filter(Span.class::isInstance).map(c -> ((Span) c).getText()).findFirst().orElseThrow();
    }

    @Test
    void hiddenWhenNothingIsInvalid() {
        assertThat(warning(true, List.of()).isVisible()).isFalse();
    }

    @Test
    void administratorSeesTheInvalidExtensionsOnHover() {
        InvalidExtensionsWarning warning = warning(true, List.of(bundle("authorsnote", "1.3.0"), bundle("sidequery", "1.2.0")));

        assertThat(warning.isVisible()).isTrue();
        assertThat(warning.getElement().getAttribute("title")).contains("authorsnote 1.3.0").contains("sidequery 1.2.0");
        assertThat(text(warning)).contains("Hover");
    }

    @Test
    void userIsToldToAskTheAdministratorWithoutNames() {
        InvalidExtensionsWarning warning = warning(false, List.of(bundle("authorsnote", "1.3.0")));

        assertThat(warning.isVisible()).isTrue();
        assertThat(warning.getElement().getAttribute("title")).isNull();
        assertThat(text(warning)).contains("administrator");
    }

    @Test
    void goesAwayWhenTheExtensionIsFixed() {
        InvalidExtensionsWarning warning = warning(true, List.of(bundle("a", "1.0.0")));
        assertThat(warning.isVisible()).isTrue();

        ReflectionTestUtils.setField(warning, "osgiService", osgi(List.of()));
        warning.refresh();

        assertThat(warning.isVisible()).isFalse();
    }
}
