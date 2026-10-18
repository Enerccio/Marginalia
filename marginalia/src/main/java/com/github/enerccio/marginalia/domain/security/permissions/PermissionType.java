package com.github.enerccio.marginalia.domain.security.permissions;

import com.github.enerccio.marginalia.domain.security.model.Permission;

/**
 * What a user can be given by an administrator, stored by name in {@link Permission}: do not rename a constant.
 * Administrators do not need them, callers check {@code AdminGuard#isAdmin()} first.
 */
public enum PermissionType {

    /**
     * Use of the help assistant.
     */
    HELPER,

    /**
     * Creating their own AIs, without it the user only uses the AIs administrators share.
     */
    CREATE_AI,

    /**
     * Creating agents and running them. The limits (concurrent runs, tokens of a run) are the settings of the
     * permission, see {@code AgentService#getGrant}.
     */
    AGENTS,

}
