package com.github.enerccio.marginalia.ui.main;

import com.github.enerccio.marginalia.bound.ApplicationPoint;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Route("/")
@Configurable(preConstruction = true)
public class Main extends LoginCheckRoute {

    @Autowired
    private User user;

    @Autowired
    private ApplicationPoint applicationPoint;

    public Main() {
        showLogin();
    }

    @Override
    protected String getAppTitle() {
        return "Marginalia";
    }

    @Override
    protected String getAppDescription() {
        return "Collaborative story writing app with LLM";
    }

    @Override
    protected boolean authenticate(String userName, String password) throws Exception {
        return userService.authenticate(userName, password);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {

    }

    @Override
    protected void proceedWithLogin(String username) {
        try {
            User u = userService.findByName(username);

            user.setId(u.getId());
            user.setLogin(u.getLogin());
            user.setFullName(u.getFullName());

            sessionManager.userLoggedIn(user, VaadinSession.getCurrent());

            Workspace workspace = new Workspace();
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

}
