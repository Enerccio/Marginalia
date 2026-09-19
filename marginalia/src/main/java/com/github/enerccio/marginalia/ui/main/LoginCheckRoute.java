package com.github.enerccio.marginalia.ui.main;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.security.PersistedLoginInfo;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.main.dialogs.UserDialog;
import com.github.enerccio.marginalia.ui.widgets.PermissiveLoginOverlay;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Arrays;
import java.util.List;

@Configurable(preConstruction = true)
public abstract class LoginCheckRoute extends Div {
    private static final Logger log = LoggerFactory.getLogger(LoginCheckRoute.class);

    private static final String ID = "UIRoot";
    protected static final String PERSISTED_LOGIN_COOKIE_LOGIN = "_llllm_rememberMe_login";
    protected static final String PERSISTED_LOGIN_COOKIE_IDENTIFIER = "_llllm_rememberMe_identifier";
    protected static final String PERSISTED_LOGIN_COOKIE_SECRET = "_llllm_rememberMe_secret";
    protected static final String PERSISTED_LOGIN_COOKIE_WAS_SAVED = "_llllm_rememberMe_wasSaved";

    @Autowired
    protected Localization loc;

    @Autowired
    protected UserService userService;

    @Autowired
    private Configuration configuration;

    protected LoginOverlay loginOverlay;

    protected abstract String getAppTitle();
    protected abstract String getAppDescription();
    protected abstract boolean authenticate(String userName, String password) throws Exception;
    protected abstract void proceedWithLogin(String username);

    public LoginCheckRoute() {
        setSizeFull();
        setId(ID);
    }

    protected void showLogin() {
        try {
            if (userService.existsUsers()) {
                String authUser = authenticateFromCookie();
                if (authUser == null) {
                    login();
                } else {
                    performLogin(authUser);
                }
            } else {
                UserDialog userDialog = new UserDialog();
                userDialog.setFirstTime(true);
                userDialog.setOnSave(() -> {
                    UI.getCurrent().getPage().reload();
                });
                userDialog.create();
                userDialog.open();
                onAttach(new AttachEvent(UI.getCurrent(), false));
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    protected String authenticateFromCookie() {
        String cookieLogin = null;
        String cookieIdentifier = null;
        String cookieSecret = null;
        String cookiePrefix = getCookiePrefix();
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ((cookiePrefix + PERSISTED_LOGIN_COOKIE_LOGIN).equals(cookie.getName())) {
                    cookieLogin = cookie.getValue();
                }
                if ((cookiePrefix + PERSISTED_LOGIN_COOKIE_IDENTIFIER).equals(cookie.getName())) {
                    cookieIdentifier = cookie.getValue();
                }
                if ((cookiePrefix + PERSISTED_LOGIN_COOKIE_SECRET).equals(cookie.getName())) {
                    cookieSecret = cookie.getValue();
                }
            }
        }
        try {
            if (StringUtils.isAnyBlank(cookieLogin, cookieIdentifier, cookieSecret)) {
                deleteRememberMeCookies();
                return null;
            }

            User user = userService.findByName(cookieLogin);
            PersistedLoginInfo loginInfo = userService.authenticateFromCookie(user, cookieIdentifier, cookieSecret);
            if (loginInfo != null) {
                addRememberMeCookies(user, loginInfo);
                return user.getLogin();
            }

            deleteRememberMeCookies();
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            return null;
        }
    }

    protected void addRememberMeCookies(User user, PersistedLoginInfo persistedLoginInfo) {
        long ttl = configuration.getPersistentLoginInfoTTL() == null ?
                60 * 60 * 24 * 30 : configuration.getPersistentLoginInfoTTL();
        String cookiePrefix = getCookiePrefix();
        Cookie cookieLogin = new Cookie(cookiePrefix + PERSISTED_LOGIN_COOKIE_LOGIN, user.getLogin());
        Cookie cookieIdentifier = new Cookie(cookiePrefix + PERSISTED_LOGIN_COOKIE_IDENTIFIER, persistedLoginInfo.getIdentifier());
        Cookie cookieSecret = new Cookie(cookiePrefix + PERSISTED_LOGIN_COOKIE_SECRET, persistedLoginInfo.getPlainSecret());
        Cookie cookieSaved = new Cookie(cookiePrefix + PERSISTED_LOGIN_COOKIE_WAS_SAVED, "yes");
        for (Cookie cookie : List.of(cookieLogin, cookieIdentifier, cookieSecret, cookieSaved)) {
            cookie.setPath("/");
            cookie.setMaxAge((int) ttl);
            cookie.setSecure(true);
            VaadinService.getCurrentResponse().addCookie(cookie);
        }
    }

    protected void deleteRememberMeCookies() {
        String cookiePrefix = getCookiePrefix();
        Cookie cookieLogin = new Cookie(cookiePrefix + PERSISTED_LOGIN_COOKIE_LOGIN, "");
        Cookie cookieIdentifier = new Cookie(cookiePrefix + PERSISTED_LOGIN_COOKIE_IDENTIFIER, "");
        Cookie cookieSecret = new Cookie(cookiePrefix + PERSISTED_LOGIN_COOKIE_SECRET, "");
        for (Cookie cookie : List.of(cookieLogin, cookieIdentifier, cookieSecret)) {
            cookie.setPath("/");
            cookie.setMaxAge(0);
            VaadinService.getCurrentResponse().addCookie(cookie);
        }
    }

    private String getCookiePrefix() {
        String rawContext = VaadinRequest.getCurrent().getContextPath();
        String context = (StringUtils.isBlank(rawContext) || "/".equals(rawContext)) ? "root" : rawContext.substring(1);
        return context + "_" + this.getClass().getSimpleName();
    }

    private void login() {
        if (loginOverlay == null) {
            loginOverlay = new PermissiveLoginOverlay();
            loginOverlay.setTitle(getAppTitle());
            loginOverlay.setDescription(getAppDescription());
            loginOverlay.setI18n(getLoginI18n());
            loginOverlay.setForgotPasswordButtonVisible(showForgetPassword());

            Checkbox saveLoginCheckBox = new Checkbox(loc.getValue(L.LABEL_SAVE_LOGIN));
            saveLoginCheckBox.setWidth("100%");
            if (VaadinService.getCurrentRequest().getCookies() != null)
                saveLoginCheckBox.setValue(Arrays.stream(VaadinService.getCurrentRequest().getCookies()).anyMatch(cookie ->
                        cookie.getName().endsWith(this.getClass().getSimpleName() + PERSISTED_LOGIN_COOKIE_WAS_SAVED)));

            loginOverlay.getCustomFormArea().add(saveLoginCheckBox);

            loginOverlay.addLoginListener(event -> {
                String userName = event.getUsername();
                String password = event.getPassword();

                try {
                    boolean authenticated = authenticate(userName, password);

                    if (authenticated) {
                        if (canEnterApp(userName)) {
                            if (saveLoginCheckBox.getValue()) {
                                User user = userService.findByName(userName);
                                PersistedLoginInfo persistedLoginInfo = userService.generateNewPersistentInfo(user);
                                addRememberMeCookies(user, persistedLoginInfo);
                                userService.addPersistedLoginInfo(user, persistedLoginInfo);
                                userService.save(user);
                            } else {
                                String cookiePrefix = getCookiePrefix();
                                Cookie cookieSaved = new Cookie(cookiePrefix + PERSISTED_LOGIN_COOKIE_WAS_SAVED, "");
                                cookieSaved.setMaxAge(0);
                                cookieSaved.setPath("/");
                                VaadinService.getCurrentResponse().addCookie(cookieSaved);
                            }
                            loginOverlay.setOpened(false);
                            performLogin(userName);
                        } else {
                            loginOverlay.setEnabled(true);
                        }
                    } else {
                        loginOverlay.setError(true);
                        loginOverlay.setEnabled(true);
                    }
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
        }

        loginOverlay.setOpened(true);
    }

    private void performLogin(String user) {
        proceedWithLogin(user);

        onAttach(new AttachEvent(UI.getCurrent(), false));
    }

    protected boolean showForgetPassword() {
        return false;
    }

    private LoginI18n getLoginI18n() {
        LoginI18n i18n = LoginI18n.createDefault();

        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle(getAppTitle());
        i18n.getHeader().setDescription(getAppDescription());
        i18n.getForm().setUsername(loc.getValue(L.LABEL_USERNAME));
        i18n.getForm().setPassword(loc.getValue(L.LABEL_PASSWORD));
        i18n.getForm().setTitle(loc.getValue(L.LABEL_LOGIN));
        i18n.getForm().setSubmit(loc.getValue(L.LABEL_LOGIN));

        i18n.getForm().setForgotPassword(loc.getValue(L.LABEL_FORGOT_PASSWORD));
        i18n.getErrorMessage().setTitle(loc.getValue(L.LABEL_AUTHENTICATION_FAILED));
        i18n.getErrorMessage().setMessage(loc.getValue(L.MSG_AUTHENTICATION_FAILED_INFO));

        return i18n;
    }

    public void logout() {
        deleteRememberMeCookies();

        String prefix = getCookiePrefix();
        Cookie cookieSaved = new Cookie(prefix + PERSISTED_LOGIN_COOKIE_WAS_SAVED, "");
        cookieSaved.setMaxAge(0);
        cookieSaved.setPath("/");
        VaadinService.getCurrentResponse().addCookie(cookieSaved);

        logoutWithoutCookieClear();
    }

    public void logoutWithoutCookieClear() {
        UI currentUi = UI.getCurrent();
        if (currentUi != null) {
            String rawContext = VaadinRequest.getCurrent().getContextPath();
            String redirectPath = (rawContext == null || "/".equals(rawContext)) ? "/" : rawContext;

            currentUi.getPage().setLocation(redirectPath);
        }

        VaadinSession currentSession = VaadinSession.getCurrent();
        if (currentSession != null) {
            currentSession.close();
        }

        var wrappedSession = VaadinService.getCurrentRequest().getWrappedSession(false);
        if (wrappedSession != null) {
            wrappedSession.invalidate();
        }
    }

    protected boolean canEnterApp(String userName) throws Exception {
        return true;
    }

    public void exit() {
        UI currentUi = UI.getCurrent();
        if (currentUi != null) {
            currentUi.getPage().setLocation("about:blank");
        }

        VaadinSession currentSession = VaadinSession.getCurrent();
        if (currentSession != null) {
            currentSession.close();
        }
    }
}