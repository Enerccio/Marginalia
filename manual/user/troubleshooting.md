---
label: Troubleshooting & FAQ
order: 900
icon: question
---

# Troubleshooting & FAQ

## Where are the logs?

Most problems leave a trace in the log:

| Installation | Log |
|---|---|
| Desktop app | `~/.marginalia/desktop/logs/marginalia.log` (`%USERPROFILE%\.marginalia\desktop\logs` on Windows); the previous run is in `marginalia.log.1` |
| Docker | `docker compose logs -f marginalia` |

Error dialogs in the application (*Internal Server Error*) show the error as well; the log has the full details.

## Installing and starting

**macOS says the app "is damaged" or "can't be opened".**
The app isn't signed yet. Right-click it and choose *Open*, or run
`xattr -dr com.apple.quarantine /Applications/Marginalia.app`. See [Desktop app](getting-started/desktop-app.md).

**Windows SmartScreen blocks the app.**
Click *More info* → *Run anyway*.

**The browser opens a different port than 8765.**
Port 8765 was taken, so the desktop app picked a free one. Use **Open Marginalia** in the tray menu, or start it with
`--port` to fix the port.

**"Marginalia did not start in time".**
The desktop app couldn't start its server. The log says why - often another program holding the files, or a broken
extension (see [below](#extensions)).

**Docker: port 8080 is already in use.**
Change the left side of the port mapping in `docker-compose.yml`, e.g. `"8081:8080"`, and open port 8081.

**Marginalia is slow or stops with "OutOfMemoryError".**
Raise the memory: `-Xmx1g` (or more) in `JAVA_TOOL_OPTIONS` in `docker-compose.yml`.

## Logging in

**"Save login" doesn't keep me logged in.**
Saved logins need HTTPS on a server. Put Marginalia behind a reverse proxy with HTTPS, see
[Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy).

**I forgot my password.**
An administrator can set a new one in *Admin → Users* (see [Users](administration/users.md#editing-a-user)).
There is no password reset by e-mail.

**Nobody can log in as an administrator any more.**
Without an administrator, the password can only be reset in the database:

1. Stop Marginalia and copy `marginalia.sqlite` from the [data folder](administration/index.md#the-data-folder) as a
   backup.
2. Remove the password of the administrator with the `sqlite3` tool:
   ```sh
   sqlite3 marginalia.sqlite "UPDATE users SET passwordHash = NULL WHERE login = 'admin';"
   ```
   (use the right user name).
3. Start Marginalia, log in with that user name and an **empty** password, and immediately set a new password with
   **Change Password**.

**"Server connection lost" or the page keeps reloading.**
Behind a reverse proxy, the proxy must forward WebSockets and allow long requests - see the nginx example in
[Server (Docker)](getting-started/docker-server.md#https-and-reverse-proxy). With the desktop app, the app was probably
quit; start it again.

## Models and generation

**"Failed to download model list".**
Check that the URL ends with `/v1`, the API key is right and the model server is running. Some services don't list
their models - type the model ID instead. See [Inference providers](inference-providers.md#per-type-settings).

**Docker: a model server on the same machine can't be reached.**
Inside the container, `localhost` is the container. Use `http://host.docker.internal:<port>/v1` and make the model
server listen on all addresses, see [Models on the same machine](getting-started/docker-server.md#models-on-the-same-machine).

**"Book is missing model." / "Book is missing protocol."**
Select them on the book's *About* tab. Set *Default Model* and *Default Protocol* in *Settings* so new books get them.

**"Contextual limit not sufficient."**
The prompt without any story is already bigger than the room the context leaves after the response. Raise
*Max Context Size* of the provider (or *Max Context Tokens* of the protocol), lower *Max Response Tokens*, or shorten
the templates, lorebook entries and summaries. See [Limits](protocols.md#limits).

**The model reports that the context is too long.**
Marginalia's token count is a little off for some models. Lower *Max Context Size* by a few hundred tokens.

**The text stops in the middle of a sentence.**
The response limit was reached. Raise *Max Response Tokens* (or the protocol's *Max Reply Tokens*) - reasoning models
need much more, because the reasoning counts too.

**Requests fail when reasoning is enabled.**
The API doesn't support `reasoning_effort`. Turn off *Enable Reasoning* on the provider.

**After an error, there is an empty part at the end of the story.**
A failed request leaves the part it was writing. Delete it, fix the cause and generate again. *Regenerate* also leaves
the part empty when it fails, so its previous text is lost - see
[Story editor](books/story-editor.md#writing-the-next-part).

**Swipe adds a new part instead of another version.**
That is a known problem of the current version. Use *Branch Story* on the last part and *Regenerate* the copy, see
[Trying another version of the last part](books/branches-and-story-tree.md#trying-another-version-of-the-last-part).

**"Invalidated summaries for messages with IDs ... Continue generation without those summaries?"**
A part covered by a summary was edited or deleted. Answer *Yes* to continue without the outdated summaries, then
generate new ones. See [When summaries become outdated](books/summaries.md#when-summaries-become-outdated).

**The prompt contains the word "Error".**
A template uses a variable or macro that doesn't exist, often a typo like `instruction` instead of `instructions`.
See [Typos](templates/templates.md#typos).

## Lorebooks

**An entry isn't used.** Go through the conditions in [Activation](lorebooks/entries.md#activation):

- Is the entry enabled? Is its lorebook enabled - and every sub lorebook on the way to it?
- Does it have tags? Then the book needs one of them (book tags are on the *About* tab). Negative tags exclude it.
- Was the lorebook imported from a file or a book backup? It may carry lorebook tags you can't see in the editor; they
  apply to all its entries.
- Does it have a filter? Filters only see the **instructions of the part being written**, not the earlier story -
  mention the character in *Present Characters* or the instructions.
- Is the filter a regular expression? An invalid one never matches.

**Show Prompt** on a generated part shows which lore was sent.

**The book's Lorebook tab shows a different lorebook than the one the book uses.**
The editor on that tab has its own selector. The lorebook the book uses is the one in the field at the top of the tab.

## Prompts and settings

**My changes in Settings are gone.**
Settings are saved only with **Save**; switching tabs reloads them.

**My summary prompt isn't used.**
The *Default Summary Prompt* in *Settings* isn't saved in the current version, and a book's own summary prompt is
erased when another field of its *Prompts* tab is edited. Set the book's summary prompt as the last change on the tab,
and check it before generating summaries.

**Macros in Style, Point of View or Tense don't work.**
These fields are plain text; only templates process macros. Put the macro into the master template or user prompt.

## Backups

**I restored a database backup, but nothing changed.**
A restore is applied on the next start. Restart Marginalia. See [Restoring](administration/database-backups.md#restoring).

**Book backups are missing after moving to another computer.**
Book backups are files, not part of the database. Copy the `data` folder too, see
[The data folder](administration/index.md#the-data-folder).

## Extensions

**An installed extension doesn't show up.**
Close and reopen the book, or reload the page.

**An extension fails to load and isn't listed in Admin → Extensions.**
Stop Marginalia, delete its JAR from the `extensions` folder, start again. See
[When an extension fails](administration/extensions.md#when-an-extension-fails).

**After updating an extension, the old version still runs.**
Unload the old version first, then load the new one - or restart Marginalia.

## FAQ

**Is my writing sent anywhere?**
Only to the inference providers you configure: the prompt for each part (and token counting requests) goes to that
provider's API. With a model running on your own computer, nothing leaves it. Marginalia has no telemetry.

**Which model should I use?**
Any model that writes good fiction and has a large enough context; a context of 32 000 tokens or more is comfortable
for long books. Try a few with the same book - switching the provider is one click on the *About* tab.

**Can several people work on one book?**
No. Every book belongs to one user. Other users can read it when it's [published](books/publishing.md).

**Can I use Marginalia offline?**
Yes, with a model running locally (llama.cpp, LM Studio, Ollama...). Marginalia itself needs no internet connection.

**How do I get my book out of Marginalia?**
Read it in the [viewer](books/publishing.md), or [export a book backup](books/backups.md#exporting-and-importing)
(JSON with all parts and branches). There is no export to text, EPUB or Word yet.

**Does it work on a phone?**
The [viewer](books/publishing.md) is made for phones, and Marginalia can be added to the home screen like an app
(*Install app* / *Add to Home Screen* in the browser menu). The editor works best on a larger screen.

**Is there a light theme or another language?**
Not yet: the interface is in English, with a dark theme.
