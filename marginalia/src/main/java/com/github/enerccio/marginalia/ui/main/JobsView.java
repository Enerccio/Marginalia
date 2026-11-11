package com.github.enerccio.marginalia.ui.main;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.bound.SessionManager;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.ui.jobs.ActiveJobsGrid;
import com.github.enerccio.marginalia.ui.jobs.RunsGrid;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * {@code /jobs}: the jobs that run now, and the runs that work on the books (which book, what they hold, Stop and End), for an
 * administrator who wants to see at a glance whether the server is busy. It logs in like the other pages and shows nothing to other
 * users; the history of the jobs is in the Admin page.
 */
@Route("jobs")
@Configurable(preConstruction = true)
public class JobsView extends LoginCheckRoute {

    @Autowired
    private User user;

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    private SessionManager sessionManager;

    public JobsView() {
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
    protected String authenticatedSessionUser() {
        return user.getId() == null ? null : user.getLogin();
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

            removeAll();
            VerticalLayout page = new VerticalLayout();
            page.setSizeFull();
            if (adminGuard.isAdmin()) {
                page.add(new H3(loc.getValue(L.LABEL_ACTIVE_JOBS)), new ActiveJobsGrid().create(),
                        new H3(loc.getValue(L.LABEL_RUNS_ON_BOOKS)), new RunsGrid().create());
            } else {
                Span denied = new Span(loc.getValue(L.MSG_ADMIN_ONLY));
                denied.addClassName(CssStyles.TEXT_SECONDARY);
                page.add(denied);
            }
            add(page);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
