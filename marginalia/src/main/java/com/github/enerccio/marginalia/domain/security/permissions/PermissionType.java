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

    /**
     * Messages sent by the server to an address the user typed in their settings (a webhook, for example a ntfy topic)
     * when an agent needs them. Without it nothing is sent, and the settings do not show the address. Only public
     * addresses are called, unless the setting {@code notificationsAllowLocal} of the permission allows the machine
     * itself and private networks.
     */
    NOTIFICATIONS,

    /**
     * The version history of the parts of the user's books: how many versions of a part are kept ({@code historyMaxVersions}
     * of the permission, the ceiling over what a book, a folder or the user set). <b>Unlike the other types a user without
     * the permission has it</b>, with the default of {@code EffectiveHistoryLimit#DEFAULT_MAX_VERSIONS}; a permission that is
     * not granted keeps no history.
     */
    VERSION_HISTORY,

}
