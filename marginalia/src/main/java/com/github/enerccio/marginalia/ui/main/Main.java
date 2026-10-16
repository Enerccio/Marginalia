package com.github.enerccio.marginalia.ui.main;

import com.github.enerccio.marginalia.bound.ApplicationPoint;
import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.ui.workspace.DesktopWorkspace;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.components.HelpAssistantWidget;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.phone.PhoneWorkspace;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import java.util.List;

@Route("/")
@Configurable(preConstruction = true)
@Extendable
public class Main extends LoginCheckRoute implements LLMInteract {

    @Autowired
    private User user;

    @Autowired
    private ApplicationPoint applicationPoint;

    @Autowired
    private OsgiService osgiService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private SessionPoint sessionPoint;

    private CssRegistry cssRegistry;

    public Main() {
        showLogin();
    }

    /**
     * The workspace of this page is the only child: the components in the page belong to it.
     */
    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = List.class),
            parameters = {@APIParameter(name = "deep", type = boolean.class)},
            locals = {@APILocals(name = "workspace", type = Workspace.class)})
    @Override
    public List<LLMInteract> getLLMChildren(boolean deep) {
        Workspace workspace = applicationPoint.getUIs().get(UI.getCurrent());
        return workspace == null ? List.of() : List.of(workspace);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class))
    @Override
    protected String getAppTitle() {
        return "Marginalia";
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class))
    @Override
    protected String getAppDescription() {
        return loc.getValue(L.LABEL_APP_DESC);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class))
    @Override
    protected String authenticatedSessionUser() {
        return user.getId() == null ? null : user.getLogin();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "userName", type = String.class),
                    @APIParameter(name = "password", type = String.class)})
    @Override
    protected boolean authenticate(String userName, String password) throws Exception {
        return userService.authenticate(userName, password, clientAddress());
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "username", type = String.class)},
            locals = {@APILocals(name = "u", type = User.class),
                    @APILocals(name = "details", type = ExtendedClientDetails.class),
                    @APILocals(name = "workspace", type = Workspace.class),
                    @APILocals(name = "referenceCopy", type = User.class),
                    @APILocals(name = "e", type = Exception.class)})
    @Override
    protected void proceedWithLogin(String username) {
        try {
            User u = userService.findByName(username);

            user.setId(u.getId());
            user.setLogin(u.getLogin());
            user.setFullName(u.getFullName());

            sessionManager.userLoggedIn(user, VaadinSession.getCurrent());

            sessionPoint.setLanguage(loc.resolveLanguage(settingService.getOrCreate(UserSetting.class).getLanguage()));

            ExtendedClientDetails details = UI.getCurrent().getPage().getExtendedClientDetails();
            boolean isPhone = details != null && details.getBodyClientWidth() < 768;

            Workspace workspace = isPhone ? new PhoneWorkspace() : new DesktopWorkspace();
            User referenceCopy = new User();
            referenceCopy.setId(u.getId());
            referenceCopy.setLogin(u.getLogin());
            referenceCopy.setFullName(u.getFullName());
            applicationPoint.register(UI.getCurrent(), workspace, referenceCopy);
            workspace.setOnLogout(this::logout);
            add(workspace.create());
            add(new HelpAssistantWidget());
            workspace.refresh();
            workspace.restoreAutosaved();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "attachEvent", type = AttachEvent.class)})
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // called also by the login, before the route is attached, so the registry is created on demand
        if (cssRegistry == null) {
            cssRegistry = new CssRegistry(this, osgiService);
        }
        cssRegistry.attach(attachEvent.getUI());
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "detachEvent", type = DetachEvent.class)})
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (cssRegistry != null) {
            cssRegistry.detach();
        }
    }

}
