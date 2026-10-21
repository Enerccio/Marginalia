package com.github.enerccio.marginalia.ui.main;

import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The way back into a workspace that can not be opened any more (the book it opens again freezes the browser, e.g. a
 * huge book and no limit of the loaded parts): after the login, as for {@link Main}, it forgets the saved state of the
 * workspace of the user ({@link UserSetting#getWorkspaceState}: the open books, tabs, filters...) and goes to the
 * workspace, which opens empty. It builds no workspace itself, so nothing it would restore can get in its way.
 */
@Route("reset")
@Configurable(preConstruction = true)
@Extendable
public class Reset extends LoginCheckRoute {

    private static final Logger log = LoggerFactory.getLogger(Reset.class);

    @Autowired
    private User user;

    @Autowired
    private SettingService settingService;

    public Reset() {
        showLogin();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class))
    @Override
    protected String getAppTitle() {
        return "Marginalia";
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class))
    @Override
    protected String getAppDescription() {
        return loc.getValue(L.LABEL_APP_DESC);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class))
    @Override
    protected String authenticatedSessionUser() {
        return user.getId() == null ? null : user.getLogin();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "userName", type = String.class),
                    @APIParameter(name = "password", type = String.class)})
    @Override
    protected boolean authenticate(String userName, String password) throws Exception {
        return userService.authenticate(userName, password, clientAddress());
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "username", type = String.class)},
            locals = {@APILocals(name = "u", type = User.class),
                    @APILocals(name = "setting", type = UserSetting.class),
                    @APILocals(name = "rawContext", type = String.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    protected void proceedWithLogin(String username) {
        try {
            User u = userService.findByName(username);

            user.setId(u.getId());
            user.setLogin(u.getLogin());
            user.setFullName(u.getFullName());

            UserSetting setting = settingService.getOrCreate(UserSetting.class);
            setting.setWorkspaceState(new JsonObject());
            settingService.save(setting);
            log.info("Workspace state of user {} was reset", u.getLogin());

            String rawContext = VaadinRequest.getCurrent().getContextPath();
            UI.getCurrent().getPage().setLocation((rawContext == null || rawContext.isEmpty()) ? "/" : rawContext);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

}
