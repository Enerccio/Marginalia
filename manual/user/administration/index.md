---
label: Administration
order: 910
icon: tools
---

# Administration

Administrators manage the Marginalia installation: user accounts, database backups, cleanup of deleted data and
extensions. The first account, created on the [first start](../getting-started/first-start.md), is an administrator;
it can make other users administrators too.

Administrators are also normal users with their own books - they don't see other users' books, lorebooks or settings.

## The Admin page

Administrators have an **Admin** button at the bottom of the workspace. It is only in the normal layout: the
[phone layout](../phone.md) (a window narrower than 768 pixels) has no Admin page, so open Marginalia in a wider window (and reload the page) for administration. It opens the admin page with six tabs:

| Tab | |
|---|---|
| **Users** | Creating, editing and deleting accounts, and giving users [permissions](permissions.md). See [Users](users.md). |
| **Database Backups** | Backups of the whole database, on demand or on a schedule, and restoring them. See [Database backups](database-backups.md). |
| **Cleanup** | Permanently removing deleted data. See [Cleanup](cleanup.md). |
| **Help assistant** | The chat that answers questions from the manual: the model, the prompts and who pays for it. See [Help assistant](help-assistant.md). |
| **Agents** | The system prompt and the compaction prompt that agents follow when neither they nor their user have one of their own. Empty is the built-in prompt; the info icon has **Insert Default Template**, which fills it in so you can edit a copy. What users may do with agents is their [permission](permissions.md). Also **Agents may send messages to the webhooks of users**: the server calls the address a user typed in their settings when an agent needs them and they have the application closed ([When you are not there](../agents.md#when-you-are-not-there)); turn it off where that is not acceptable. |
| **Extensions** | Installing and removing extensions. See [Managing extensions](extensions.md). |

## The data folder

Everything Marginalia stores is in the `.marginalia` folder in the home directory of the user that runs it (`./data`
on the host with the [Docker setup](../getting-started/docker-server.md)):

| Path | |
|---|---|
| `marginalia.sqlite` (with `-wal`, `-shm`) | The database: users, books, lorebooks, settings, everything. |
| `secret.key` | Key that encrypts the API keys in the database. Created on the first start; without it, saved API keys can't be read. |
| `db-backups/` | [Database backups](database-backups.md). |
| `data/<login>/backups/manuscripts/<book id>/` | [Book backups](../books/backups.md) of each user. |
| `extensions/` | Installed [extension](extensions.md) JARs. |
| `desktop/logs/` | Logs of the [desktop app](../getting-started/desktop-app.md). |

To back up a whole installation, stop Marginalia and copy this folder (including `secret.key`). While it runs, use
[database backups](database-backups.md) for the database; book backups and extensions are plain files and can be
copied at any time.
