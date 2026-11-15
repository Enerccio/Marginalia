/**
 * <b>http-hooks</b>: what the plugins that call a server of the user share (the Image Generator, the Audiobook). A plain jar that each plugin
 * embeds in its bundle ({@code Embed-Dependency http-hooks;inline=true}); it is never loaded as an extension. See the developer manual,
 * <i>Plugin development / Libraries / http-hooks</i>.
 * <p>
 * {@link com.github.enerccio.marginalia.extensions.httphooks.Http} makes the calls (no redirects, a timeout, a cap on the answer),
 * {@link com.github.enerccio.marginalia.extensions.httphooks.UrlGuard} refuses the private addresses of a server that is not local,
 * {@link com.github.enerccio.marginalia.extensions.httphooks.HttpHook} and {@link com.github.enerccio.marginalia.extensions.httphooks.HookRunner}
 * are the requests a backend makes before and after its work. Taking turns on a shared GPU is not here: the backends name a GPU lock of the
 * application ({@code GpuService}).
 * <p>
 * <b>Rules of a library</b> (each plugin has its own copy of every class): no state two plugins would have to share, objects are never handed
 * from one plugin to the other, and nothing in it depends on the application (the JDK only).
 */
package com.github.enerccio.marginalia.extensions.httphooks;
