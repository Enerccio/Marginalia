package com.github.enerccio.marginalia.bound;

import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionCookieConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class SessionCookieTest {

    private final AtomicReference<String> name = new AtomicReference<>();

    @AfterEach
    void clearSetting() {
        System.clearProperty(SessionCookie.SETTING);
    }

    private ServletContext context() {
        SessionCookieConfig config = (SessionCookieConfig) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{SessionCookieConfig.class}, (_, method, args) -> {
                    if (method.getName().equals("setName")) {
                        name.set((String) args[0]);
                    }
                    return null;
                });
        return (ServletContext) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ServletContext.class}, (_, method, _) ->
                        method.getName().equals("getSessionCookieConfig") ? config : null);
    }

    @Test
    void withoutANameTheContainerDefaultStays() {
        assertThat(SessionCookie.configure(context(), null)).isNull();
        assertThat(name.get()).isNull();
    }

    @Test
    void defaultNameOfTheApplicationIsSet() {
        assertThat(SessionCookie.configure(context(), "JOISESSIONID")).isEqualTo("JOISESSIONID");
        assertThat(name.get()).isEqualTo("JOISESSIONID");
    }

    @Test
    void settingWinsOverTheDefault() {
        System.setProperty(SessionCookie.SETTING, "MYSESSION");
        assertThat(SessionCookie.configure(context(), "JOISESSIONID")).isEqualTo("MYSESSION");
        assertThat(name.get()).isEqualTo("MYSESSION");
    }

    @Test
    void invalidNameIsRefused() {
        assertThat(SessionCookie.configure(context(), "bad name;")).isNull();
        assertThat(name.get()).isNull();
        assertThat(SessionCookie.isValidName("a-b_c.1")).isTrue();
    }
}
