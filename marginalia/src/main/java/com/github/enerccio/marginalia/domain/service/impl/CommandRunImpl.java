package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.command.CommandRun;

/**
 * The run as the service creates it: the way to reach the protected status update of {@link CommandRun}.
 */
class CommandRunImpl extends CommandRun {

    void status(String status, String subStatus) {
        publishStatus(status, subStatus);
    }

    void uninterruptable(boolean uninterruptable) {
        publishUninterruptable(uninterruptable);
    }

}
