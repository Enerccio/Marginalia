package com.github.enerccio.marginalia.ui.main;

import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Route("/")
@Configurable(preConstruction = true)
public class Main extends LoginCheckRoute {

    @Autowired
    private User user;

    @Autowired
    private SessionPoint sessionPoint;

    @Autowired
    private SettingService settingService;

    public Main() {
        showLogin();
    }

    @Override
    protected String getAppTitle() {
        return "LLLLM";
    }

    @Override
    protected String getAppDescription() {
        return "Language Learning LLM";
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
            VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
            User u = userService.findByName(username);

            user.setId(u.getId());
            user.setLogin(u.getLogin());
            user.setFullName(u.getFullName());

            Workspace workspace = new Workspace();
            add(workspace.create());
            workspace.refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

}
