package com.github.enerccio.marginalia.api;

import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.service.command.BaseCommandScope;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.google.gson.JsonArray;

/**
 * What the commands of the HTTP API are aimed at, given by the application (its books): the list of {@code GET books}, the scope of
 * a command called for one of them ({@code POST books/{uuid}/commands/{name}}) or for none, and the session point of a call. Without
 * one, there are no targets and a command runs in a scope of nothing.
 */
public interface ApiTargets {

    /** No targets: the commands run in a scope that only answers questions with what the caller sent. */
    ApiTargets NONE = new ApiTargets() {
    };

    /**
     * The session point of a call, before the target is known; it gets the languages of the user.
     */
    default SessionPoint newSessionPoint() {
        return new SessionPoint();
    }

    /**
     * The targets of the user of the call (each with at least its {@code uuid} and {@code name}).
     */
    default JsonArray list() throws Exception {
        return new JsonArray();
    }

    /**
     * The scope of a command of the call: aimed at the target with the uuid, or at none.
     *
     * @param uuid         the uuid of the target, null for none
     * @param sessionPoint the one {@link #newSessionPoint()} made for the call
     * @param confirm      the answer to every question the command asks, as the caller sent it
     * @throws ApiException 404 when there is no such target the user may use
     */
    default BaseCommandScope scope(String uuid, SessionPoint sessionPoint, boolean confirm) throws Exception {
        if (uuid != null) {
            throw new ApiException(404, "Not found: " + uuid);
        }
        return new BaseCommandScope() {
            @Override
            public boolean isAgent() {
                return false;
            }

            @Override
            public boolean confirm(CommandContext context, String question) {
                return confirm && !context.isTerminated();
            }
        };
    }
}
