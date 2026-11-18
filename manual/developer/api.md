---
label: HTTP API
order: 952
covers:
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/api/ApiDispatcher.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/api/ApiDocs.java
  - marginalia/src/main/resources/api-docs/index.html
  - marginalia/src/main/resources/api-docs/api-docs.js
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/api/ApiRequest.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/api/ApiResponse.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/api/ApiException.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/ui/main/ApiServlet.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/security/model/ApiKey.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/security/model/ApiScope.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/security/repository/ApiKeyRepository.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/security/repository/impl/JpaApiKeyRepository.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/security/service/ApiKeyService.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/domain/security/service/impl/ApiKeyServiceImpl.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/bound/DetachedRequestAttributes.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/bound/ApiRequestAttributes.java
  - marginalia/platform/src/main/java/com/github/enerccio/marginalia/ui/components/ApiKeysView.java
  - tools/marginalia_api.py
---

# HTTP API

Programs talk to Marginalia over `/api/v1`: JSON over HTTP with a bearer key. There is no other API; the UI calls the services
directly (see [Architecture](architecture.md)). The API runs the same [commands](commands/index.md) the command bar and the agents
run, and a few administration calls: the version, the [update of the installation](../user/administration/updates.md) and the
[extensions](../user/administration/extensions.md).

## Keys

A key belongs to a user and acts as that user: `ApiKey` (table `api_keys`, migration `V31`), an `ExtendableEntity` owned by the
user. Users create and revoke their keys in **Settings → API keys** (`ApiKeysView`); a user may have keys when they are an
administrator or have the permission `API` (`PermissionType.API`). The key is `mrg_<prefix>_<secret>` (12 characters `a-z0-9`, then
32 random bytes in base64url). `ApiKeyService.create` gives it once (`CreatedKey.token`); the database keeps only the prefix (to
find the key) and a SHA-256 of the whole key (the key has 256 random bits, a slow hash would add nothing). A revoked key is soft
deleted; the [cleanup](../user/administration/cleanup.md) purges the keys with their user.

`ApiKeyService.authenticate(token)` (needs no current user) gives the `Principal` (key, user, access) or null: unknown, deleted,
expired, the user deleted, or the user neither an administrator nor holding the `API` permission any more. It writes `lastUsed` at
most once a minute.

What a key may do is a set of `ApiScope`s, any combination and at least one (stored by name in the extended attribute
`ApiKey.scopes`); every key may call `/version` and `/me`:

| Scope | |
|---|---|
| `READ` | `/books`, the runs, and the commands whose every form is `readOnly`. |
| `WRITE` | Every command; brings `READ` with it (`ApiScope.normalized`). |
| `UPDATE` | `/admin/update`: the status, the log and asking for an update. |
| `EXTENSIONS` | `/admin/extensions`: list, install or replace, stop, start, remove, logs. |

`UPDATE` and `EXTENSIONS` are administration scopes (`isAdministration()`): only an administrator gives them, and `authenticate`
drops them while the owner is not an administrator (the key keeps its other scopes). The services check `AdminGuard` again (from the
database) on every call. A deploy key can so be `EXTENSIONS` alone, or `UPDATE` + `EXTENSIONS` without access to any book.

## The request context

The services take the current user from the session scoped `user` and `sessionPoint` beans. A call of the API has no browser and
gets no container session: `ApiDispatcher` makes an `ApiRequestAttributes` (a `DetachedRequestAttributes`, the base the
`AgentRequestAttributes` of agent runs share) with a copy of the user and an `AgenticSessionPoint` that holds the book of the call,
and runs the call inside it (`InRequestScope`). `ThreadCopyRequestAttributes.create()` hands a detached context on as it is, so an
`async` command started by the call sees the same user on its worker thread; the context is closed when the call answers, or for a
run when the command ends. Even the key is checked inside a detached context (with an empty user), so nothing a service touches can
open a container session. The language of the call is the user's (`UserSetting`).

`ApiServlet` (mapped to `/api/*` in `WebappApplicationInitializer`, before the `/*` of Vaadin) only reads the request: the body up
to 16 MB (256 MB for an extension JAR), the query string by hand (`getParameterMap()` would read a form body), and gives an
`ApiRequest` to the `ApiDispatcher` bean. The dispatcher returns an `ApiResponse`; the tests (`ApiDispatcherTest`) call it without
HTTP.

## Calls

Every call needs `Authorization: Bearer <key>`; without a valid key the answer is `401`. Errors are `{"error": "..."}` with
`400` (bad arguments, bad JSON), `403` (a scope the key does not have, a service that refused), `404`, `409` (a state that does not allow
it), `413` (too large), `422` (a command that reported an error, an extension that does not fit), `429` (too many runs), `500`,
`503` (no extensions in this process).

| Call | Scope | |
|---|---|---|
| `GET /api/v1/version` | any | `version`, `commit`, `commitFull`, `commitTime`, `buildTime` of the running build (`BuildInfo`), `api` (1). |
| `GET /api/v1/me` | any | The user and the key (`scopes` are the effective ones). |
| `GET /api/v1/books` | READ | The books of the user: `uuid`, `name`, `folder`. |
| `GET /api/v1/commands` | READ | The commands the key may run: `name`, `title`, `description`, `async`, `readOnly`, `destructive`, and `forms` (the overloads: `parameters` with `name`, `type`, `optional`, `default`, `allowed`, `min`, `max`, and `returns`). |
| `POST /api/v1/commands/<name>` | READ / WRITE | Runs a command that needs no book. |
| `POST /api/v1/books/<uuid>/commands/<name>` | READ / WRITE | Runs a command on a book of the user (`CommandScopes.forApi`, the book is the cage of the command as for an agent). The books are the application's `ApiTargets` (`BookApiTargets`): the list of `GET books`, the scope of a command and the session point of a call. |
| `GET /api/v1/runs/<id>` | READ | The state of a run of the user. |
| `DELETE /api/v1/runs/<id>` | READ | Stops a run (`409` when it is uninterruptable now). |
| `GET /api/v1/admin/update` | UPDATE | `UpdateService.getStatus()`: `watching`, `running`, `updateAvailable`, `runningCommit`, `remoteCommit`... |
| `POST /api/v1/admin/update` | UPDATE | Asks the update watcher to update and restart (`202`, `409` without a watcher or while one runs). |
| `GET /api/v1/admin/update/log?bytes=N` | UPDATE | The end of the output of the last update, plain text. |
| `GET /api/v1/admin/extensions` | EXTENSIONS | The installed extensions: `jar`, `symbolicName`, `name`, `version`, `state`, `stopped`, `loaded`, `valid`, `sha256`. |
| `GET /api/v1/admin/extensions/<jar>` | EXTENSIONS | One of them. |
| `PUT /api/v1/admin/extensions/<jar>` | EXTENSIONS | The body is the JAR: `OsgiService.installPackage`, replacing an extension with the same symbolic name or file. `422` with the `report` when it does not fit this version (the old one is restored). |
| `DELETE /api/v1/admin/extensions/<jar>` | EXTENSIONS | Uninstalls it and deletes the JAR. |
| `POST /api/v1/admin/extensions/<jar>/stop`, `.../start` | EXTENSIONS | `stopExtension` / `startExtension` (the `.STOPPED` flag). |
| `GET /api/v1/admin/extensions/<jar>/log?bytes=N` | EXTENSIONS | The log of the extension, plain text. |

The body of a command call is a JSON object, every field optional:

```json
{"arguments": {"name": "value"}, "confirm": false, "wait": 30}
```

`arguments` is an object (by name, `CommandService.execute`) or an array (by position, `executePositional`); a number, a text, a
boolean or null is passed as it is, an object or an array as its JSON text. `confirm` answers every question the command asks
(`CommandScope.confirm`; nobody can be asked, so no is the default). `wait` is how many seconds (0 to 300) the call waits for the
command: done in time, the answer is `200` with `{"status": "done", "result": ...}` (the result is the text the command returns),
`{"status": "failed", "error": ...}` with `400`/`403`/`422`/`500`, or `409` `cancelled`; otherwise `202` with
`{"status": "running", "run": "<id>", "progress": ...}`, and the run is asked for by `GET /runs/<id>`. Runs are kept in memory (an
hour after they end, lost at a restart), at most 16 running per user.

## The API page and the OpenAPI description

`ApiDocs` describes the API for people. Three things need no key (`ApiDispatcher.handle` answers them before the key is checked):

- `GET /api/v1/` is the page (`src/main/resources/api-docs/index.html`; `/api/v1` without the slash redirects to it, so its files
  resolve next to it). It is Swagger UI from the `org.webjars:swagger-ui` webjar, served by `ApiDocs.page` from
  `/api/v1/docs/<file>`: only the files the page uses (`swagger-ui-bundle.js`, `swagger-ui.css`, the favicons) and the page's own
  `api-docs.js` / `api-docs.css`, everything else is `404`. No CDN: the desktop application may be offline.
- `GET /api/v1/openapi.json` without an `Authorization` header is the OpenAPI 3.1 document of the fixed calls (`ApiDocs.ROUTES`).
- With a key, `openapi.json` goes through the usual check (a wrong key is `401`) and also has a `POST /books/{book}/commands/<name>`
  for every command the key may run (`ApiDispatcher.runnable`, the same rule as `GET /commands`), its arguments a JSON schema per form
  (`typeSchema`: integer, number, boolean, string, an enum's names; `allowed`, `min`, `max`, the default and whether it is optional),
  the texts in the language of the key's user.

The page's script keeps the key in a variable (never `localStorage`; `persistAuthorization` is off), fetches `openapi.json` with it,
points the server of the document at the page's own URL (a context path works) and adds the key to every call of *Try it out*
(`requestInterceptor`) unless the *Authorize* button of Swagger UI gave one. The validator badge is off (`validatorUrl: null`), so
the page calls nothing but this server.

`ROUTES` is the list of the calls; `ApiDocsTest` reads the `call.is(...)` of `ApiDispatcher` and fails when the two differ, so a
new call is documented when it is added. A call's description says its scope and whether it changes the server.

## Plugins without a full update

`/version` gives the commit the server runs, so a client can tell whether plugins built from a checkout fit it:
`tools/marginalia_api.py can-hot-install` runs `git diff <running commit> HEAD -- marginalia/src/main marginalia/pom.xml pom.xml`;
when nothing changed (and nothing of it is uncommitted), plugins built from HEAD can be installed with
`tools/marginalia_api.py install --plugins` (the same check first, then a `PUT` per JAR). Otherwise the application needs a full
update (`POST /admin/update` or the Update tab). Whatever the client says, `installPackage` verifies every JAR against the running
application (see [Extendable](plugins/extendable.md)) and rolls back one that does not fit.

```sh
export MARGINALIA_URL=https://marginalia.example.org
export MARGINALIA_API_KEY_FILE=~/.marginalia-api-key
python3 tools/marginalia_api.py version
python3 tools/marginalia_api.py can-hot-install && python3 tools/marginalia_api.py install --plugins
python3 tools/marginalia_api.py run get-book-info --book <uuid>
```

The client reads the key from the environment or a file, never from the command line.
