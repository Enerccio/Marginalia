---
label: Extensions
order: 930
icon: plug
---

# Extensions

Extensions add features to Marginalia without changing the application itself. Four extensions come with Marginalia:

| Extension | What it adds |
|---|---|
| [Chapter Marker](chapter-marker.md) | Chapter titles in the story outline, from Markdown headings in the parts. |
| [Lorebook VCS](lorebook-vcs.md) | Revision history for lorebook entries. |
| [Reviewer](reviewer.md) | Reviews of story parts written by the model - reader reactions, critique. |
| [Side Query](side-query.md) | A chat with the model about your book, next to the story. |

## Installing

Extensions are installed by an **administrator** for the whole Marginalia installation, in *Admin → Extensions*:
they are JAR files, uploaded with **Load Extension (.jar)**. See [Managing extensions](../administration/extensions.md)
for the details.

Once installed, an extension is available to every user. An extension loaded while Marginalia runs shows up in
windows opened afterwards: close and reopen a book (or reload the page) to see it.

!!!warning Use matching versions
Extensions hook into the user interface of one Marginalia version. An extension built for a different version may stop
working or break parts of the application. Use the extensions that come with your version of Marginalia.
!!!

The four extensions above are part of the Marginalia source code (`marginalia/plugins`) and are built separately;
see the [plugins README](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md).

## Extension settings

Extensions with settings add a section to **Settings → Extension Settings** (the third tab of the *Settings* page).
These settings belong to your account, like the rest of *Settings*, and are saved with its **Save** button.

## Where extension data is kept

Extensions keep their data together with the data it belongs to - reviews with the story part, conversations with the
book, revisions with the lorebook - so it is included in [database backups](../administration/database-backups.md).
Whether it is also part of [book backups](../books/backups.md) and exports is described on each extension's page.

Uninstalling an extension doesn't delete its data: installing it again brings it back.
