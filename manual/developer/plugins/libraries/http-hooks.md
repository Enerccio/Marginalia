---
label: http-hooks
order: 51
covers:
  - marginalia/libs/http-hooks
  - marginalia/plugins/image-generator/src/main/java
verified: f7e7de02b
---

# http-hooks

`marginalia/libs/http-hooks` is what the plugins that call a **server of the user** share: the **Image Generator** and the **Audiobook** (text to speech). Read
[Libraries for plugins](index.md) first for how a library is embedded and what that implies (a copy per plugin, no shared state). This one depends on the JDK only, but for the settings form `HookListEditor` in the `ui` package (Vaadin, from the application) and `JobCalls` (the application's `Jobs`), and has its own tests (a fake HTTP server).

## The call: `Http`

`Http.send(request)` is the only way a plugin should call such a server:

- **No redirects** (`Redirect.NEVER`): a server can't answer with a redirect to an address the guard would have refused.
- **A cap on the answer.** `send(request, maxBytes)` reads the body through `CappedBody`, a body subscriber that stops (and cancels the stream) when the limit is reached, or
  before reading anything when `Content-Length` already announces more. The default is `Http.MAX_BYTES` (32 MB); `HookRunner` reads at most 1 MB. A bigger answer is a
  `BackendException` ("is too big"), never an allocation.
- A connect timeout of 10 s and a timeout per request that the caller gives.
- **`ServerGoneException`** (a `BackendException`) is what `send` throws when the server did not answer at all (refused, reset, timed out: "Cannot reach ..."); a plugin can tell a server that is down from one that refused (an HTTP error answer is a plain `BackendException`) and start it again. The Audiobook's `ChatterboxBackend` throws it for a 503 ("no model loaded") too, and `RecoveringBackend` restarts the server and repeats the piece once.
- `excerpt(response)` (the first 400 characters, for an error message) and `requireOk(response, what)`.

The client speaks **HTTP/1.1 only**. The JDK client otherwise offers an upgrade to HTTP/2 on a plain connection (`Upgrade: h2c`), and an ASGI server (uvicorn with httptools: LiteLLM, FastAPI servers such as Chatterbox-TTS-Server) then takes the body of a POST for upgrade data and hands the application an empty request (a 422 "field required"); the JDK's own test server tolerates the header, so only a real server shows it.

## The job log: `JobCalls`

`JobCalls.track(type, autoKind, baseUrl, model, work)` runs the one operation that is the *work* (a speech made, an image generated: not the
check of a connection, not a poll of the server) as a **call** of the job of the thread, so the administrator's overview of jobs sees it (see
`Jobs` in [Services](../../services.md)). It records the host of `baseUrl` (never the path or the query), the model, the start and the end, and how it
ended: a `BackendException` with the message `Cancelled` is a cancelled call, any other failure a failed one (the exception is thrown again). A call
outside of a job becomes a job of its own with the kind `autoKind` (the name of the plugin); a plugin that does a unit of work of many calls (a chapter
converted piece by piece) opens a job around them with `Jobs.open`. The Audiobook wraps `ChatterboxBackend.synthesize` and `HiggsComfyBackend.synthesize`, the Image Generator
`OpenAiImagesBackend.generate` and `ComfyUiBackend.generate` (there the call is the whole run, queued and polled until the image is there).

## The guard: `UrlGuard`

`UrlGuard.check(url, local)` returns the parsed `URI` or throws a `BackendException`. Only `http` and `https` pass. When `local` is false (the setting of the backend: *the
server is on this machine or on my network* is off) every address the host resolves to must be public: not any-local, loopback, site-local, link-local, multicast,
"this network" (0.0.0.0/8), shared (100.64.0.0/10, carrier-grade NAT and Tailscale) or IPv6 unique-local (fc00::/7); an IPv6 address that carries an IPv4 one
(IPv4-compatible, NAT64 `64:ff9b::/96`) is judged by the IPv4 address. Call it for **every** address the plugin is about to use, also the ones a server hands back (the
`url` of an image).

The host is resolved by the guard and again by the HTTP client at connect time. Java caches a lookup for 30 seconds by default (`networkaddress.cache.ttl`), so the client gets
the answer the guard checked; a JVM configured without that cache would let a name that changes its address in between through. That is not closed in the library.

`UrlGuard.origin(url)` is `scheme://host:port` in lower case: what two settings that call the same server have in common.

## The hook: `HttpHook` and `HookRunner`

A hook is a request of the user (address, method, headers, body) the plugin makes around work on a GPU: to unload one model and load another, to wake a server up. It is
stored as JSON in the user's settings, so the **headers are protected**: a header usually is `Authorization: Bearer ...`. The plugin stores `protectedHeaders`
(`InferenceServices.protectSecret`, the key of the installation) and the library opens them with the function the caller passes (`HookRunner.run(hook, local, reveal)`,
`hook.headersText(reveal)`): the library does not depend on the application. The plain `headers` field is the setting of a plugin version that did not protect them; it is read
while nothing is protected, and the settings form clears it on the next save. A text protected by another installation can't be opened: the hook is made without headers.

A swap is usually **several requests** (unload the language model, start a container, load a model), so a plugin keeps lists of hooks and runs them with
`HookRunner.runAll(hooks, local, reveal)` before the work and `HookRunner.runAllRegardless(hooks, local, reveal)` after it:

- `runAll` makes the requests in order and **stops at the first failure** (what comes before the work: if the model could not be unloaded, the next request must not load another
  one). It throws a `BackendException` that says *Request 2 of 3: ...* when there are several. Hooks without an address are skipped.
- `runAllRegardless` makes **every** request (what comes after: every container has to be stopped, also when the first stop failed) and returns the message of the first failure, or null.
- A hook has its own **`timeoutSeconds`** (0: 60 s, at most 600) and **`waitSeconds`** (a pause after the answer, at most 300): a container that is started answers when it is
  up, a server that is still busy after its "yes" needs the wait.

The Image Generator's `BackendConfig` keeps `beforeHooks` and `afterHooks` as lists and reads the single `before` / `after` hook of an older setting as a list of one (setting a list
drops the old one).

## Taking turns on a GPU: not here

Up to 1.8 the library had `GpuGate` (over the application's `SharedGate`), a gate keyed by a GPU name or a server address that the plugins
held around their hooks. Both are gone: taking turns on a shared GPU is a feature of the application, `GpuService` with the GPUs, locks and
steps the administrator sets up (see [GPU locks](../gpu-locks.md)). A backend stores the uuid of a lock, takes it for each job
(`gpuService.acquire(lockUuid, cancelled)`) and runs its own hooks **inside** it. The Image Generator does that per image (`ImageJob`), the
Audiobook per batch or sample (`SpeechBatch`).

## A key for the requests: `HookKeys`

A proxy in front of the models (LiteLLM) wants its key on the unload request too. `HookKeys.withBearer(hooks, key, reveal)` returns copies of the
requests with `Authorization: Bearer <key>` added to every one that has no `Authorization` header of its own (the stored requests are not changed,
empty ones are left out, a blank key returns the list as it is), so a plugin keeps the key encrypted in one field of its backend instead of in the
headers of every request. The Audiobook and the Image Generator pass their *Key of the requests*, else the api key.

## The settings form: `HookListEditor`

`ui.HookListEditor` (Vaadin) is the form of a list of hooks, one row for each request with a button that removes it and one that adds a request. A plugin builds it with
its own localized `Labels`, a CSS class for the frame of a row and the application's `Localization` (`HookListEditor(labels, rowClassName, loc)`:
the headers and the body are then `ResizableTextArea`s; the library is not woven, so the plugin passes the `loc` of its own `@Configurable` form,
created in the constructor body, not in a field initializer; the two-argument constructor leaves them plain), shows a list with `show(hooks, reveal)` and reads it back with `read(protect)` (empty rows left out). The headers are
shown opened and stored protected, so the plugin passes the functions of `InferenceServices` (`revealSecret`, `protectSecret`). The Image Generator and the Audiobook use it for their
before and after lists. Adding or removing a request is reported to the settings page with `SettingsChanges.changed` (see
[UI extensions](../ui-extensions.md)), so it counts as an unsaved change; `show` does not report (it is loading).

## Using it

Add the dependency and `<Embed-Dependency>http-hooks;inline=true</Embed-Dependency>` to the plugin's POM as described in [Libraries for plugins](index.md#using-a-library-in-a-plugin);
the library is a module of the root `pom.xml`. In the plugin's tests use a real local `HttpServer` as the library's tests do (`FakeServer`).
