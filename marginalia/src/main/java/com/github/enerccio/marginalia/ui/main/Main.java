package com.github.enerccio.marginalia.ui.main;

import com.github.enerccio.marginalia.bound.ApplicationPoint;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.OsgiService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.ui.workspace.DesktopWorkspace;
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

@Route("/")
@Configurable(preConstruction = true)
@Extendable
public class Main extends LoginCheckRoute {

    @Autowired
    private User user;

    @Autowired
    private ApplicationPoint applicationPoint;

    @Autowired
    private OsgiService osgiService;

    private CssRegistry cssRegistry;

    public Main() {
        showLogin();
    }

    @Override
    protected String getAppTitle() {
        return "Marginalia";
    }

    @Override
    protected String getAppDescription() {
        return loc.getValue(L.LABEL_APP_DESC);
    }

    @Override
    protected boolean authenticate(String userName, String password) throws Exception {
        return userService.authenticate(userName, password, clientAddress());
    }

    @Override
    protected void proceedWithLogin(String username) {
        try {
            User u = userService.findByName(username);

            user.setId(u.getId());
            user.setLogin(u.getLogin());
            user.setFullName(u.getFullName());

            sessionManager.userLoggedIn(user, VaadinSession.getCurrent());

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
            workspace.refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // called also by the login, before the route is attached, so the registry is created on demand
        if (cssRegistry == null) {
            cssRegistry = new CssRegistry(this, osgiService);
        }
        cssRegistry.attach(attachEvent.getUI());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (cssRegistry != null) {
            cssRegistry.detach();
        }
    }

}
