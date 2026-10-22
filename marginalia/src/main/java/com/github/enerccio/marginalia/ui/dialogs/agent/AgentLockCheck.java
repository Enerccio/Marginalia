package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The check the screens that open a book make: a book an agent works on is locked (the services refuse every change),
 * so the user is told to pause or stop the run instead of getting a window that can't save anything.
 */
@Configurable
@Extendable
public class AgentLockCheck {

    @Autowired
    private Localization loc;

    @Autowired
    private AgentRunService agentRunService;

    /**
     * Whether the book is locked by a run of its agent; when it is, tells the user which agent holds it.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "manuscript", type = Manuscript.class)},
            locals = {@APILocals(name = "locking", type = AgentRun.class),
                    @APILocals(name = "e", type = Exception.class)})
    public boolean refuses(Manuscript manuscript) {
        try {
            AgentRun locking = agentRunService.findLockingRun(manuscript);
            if (locking == null) {
                return false;
            }
            Notification.warning(String.format(loc.getValue(L.MSG_AGENT_BOOK_LOCKED), locking.getAgent().getName()));
            return true;
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return true;
        }
    }
}
