package com.github.enerccio.marginalia.bound;

import com.github.enerccio.marginalia.domain.security.model.User;

/**
 * The request context of a call of the HTTP API ({@code ApiDispatcher}): the user of the key and a {@link SessionPoint} (an {@code AgenticSessionPoint}) that
 * holds the book the call is aimed at (none for a call that is not about a book). No session of the container is made for the call. An
 * asynchronous command started by the call keeps the context until it ends. See {@link DetachedRequestAttributes}.
 */
public final class ApiRequestAttributes extends DetachedRequestAttributes {

    public ApiRequestAttributes(User user, SessionPoint sessionPoint) {
        super("api", user, sessionPoint);
    }
}
