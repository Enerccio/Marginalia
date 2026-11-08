# http-hooks

What the plugins that call a server of the user share: the [Image Generator](../../plugins/image-generator/README.md) and, planned, the [Audiobook](../../plugins/audiobook/README.md).
A plain Maven jar that each plugin **embeds** in its bundle (`<Embed-Dependency>http-hooks;inline=true</Embed-Dependency>`); it is not an extension and is never put into the
extensions folder. It depends on the JDK only, but for the settings form `ui.HookListEditor` (Vaadin, from the application), so it can be tested alone. Each plugin has its own copy of every class (plugins do not see each
other's classes): the state of the library, the gates, is per plugin.

| Class | What it does |
|---|---|
| `Http` | The calls: no redirects, a connect timeout, a per request timeout, and a cap on the answer (`CappedBody` stops reading at the limit, or at once when `Content-Length` announces more) so a server can't make the plugin allocate what it likes. `excerpt`, `requireOk` for the messages. |
| `UrlGuard` | `check(url, local)`: only http(s), and for a server that is not local no address in the loopback, private, link-local, shared (100.64.0.0/10), unique-local (fc00::/7), multicast and "this network" ranges, also when an IPv6 address carries an IPv4 one (IPv4-compatible, NAT64). `origin(url)` is `scheme://host:port`. The host is resolved here and again by the client when it connects; the JVM's lookup cache (30 s by default) makes the two answers the same. |
| `HttpHook`, `HookRunner` | A request of the user (address, method, headers, body, `timeoutSeconds`, `waitSeconds`) made around work on a GPU; `runAll` runs a list in order and stops at the first failure, `runAllRegardless` runs all of them and returns the first failure. The headers are kept protected (`protectedHeaders`, made by the plugin with `InferenceServices.protectSecret`); the plain `headers` of a setting saved before are still read. `HookRunner.run(hook, local, reveal)` opens them with the function it is given. |
| `GpuGate` | One GPU, several users. `forAddress(url)` gives the gate of a server; `acquire(before)` runs the hook for the first holder, `release(after)` for the last, `exclusive(cancelled, work)` makes the work of the holders one at a time, in the order they came. |
| `ui.HookListEditor` | The settings form of a list of hooks (a row for each: address, method, timeout, wait, headers, body; add and remove buttons). The texts and the functions that open and protect the headers are the plugin's. |
| `BackendException` | The one exception of all of the above (a plugin throws it for its own failures too). |

Build: `mvn install -DskipTests` here (a plugin needs it in the local repository first); `install-plugins.sh`, `marginalia/plugins/Dockerfile` and the workflows do it. Developer
manual: *Plugin development / Libraries / http-hooks*.
