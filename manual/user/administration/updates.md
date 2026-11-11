---
label: Updates
order: 65
---

# Updates

On a [Docker server](../getting-started/docker-server.md) that was built from a Git checkout, an administrator can
update Marginalia from the **Update** tab of the Admin page, without logging in to the server: the server pulls the
newest version of the branch its checkout is on, builds the application and the
[bundled plugins](../getting-started/docker-server.md#plugins) and restarts.

It needs a small program on the server, the **update watcher**, which is [set up once](#setting-up-the-watcher).
Without it the tab only shows the running version and says that the watcher does not run. That is also what the
[desktop app](../getting-started/desktop-app.md) and a server with the
[prebuilt image](../getting-started/docker-server.md#prebuilt-image) show: they are updated by installing the new
version.

## The Update tab

| | |
|---|---|
| **Running version** | The version and the commit of the Marginalia that runs. |
| **Branch** | The branch the checkout on the server is on and the remote branch it follows (for example `dev`, following `origin/dev`), the newest commit of that remote branch and when the server last looked. The server looks every 15 minutes. |
| Status | *A new version is available* when the newest commit is not the one that runs, *Marginalia is up to date* otherwise. While an update runs, the tab says so. |
| **Last update** | When the last update started from here ended and whether it finished or failed. |

The server follows the branch that is checked out there, whatever it is: to move an installation to another branch,
`git switch` it in the checkout on the server; the tab shows the new branch after the next look.

| Button | |
|---|---|
| **Update and restart** | Asks the server to update, after a confirmation. It also builds and restarts when there is nothing new (use it after changing `docker-compose.yml`, for example). |
| **Refresh** | Reads the status again. The tab does not refresh by itself. |
| **Log** | The output of the last update: what Git and the build wrote. |

## What an update does

1. The new version is built: `git pull`, the plugins, the image. This takes several minutes and **Marginalia keeps
   running** while it lasts. The tab shows *The update is running*; **Log** shows how far it is.
2. Marginalia is stopped, the new plugins are copied into `data/extensions` (replacing the installed ones) and the
   new version starts. This takes a minute or two (see [Health](../getting-started/docker-server.md#health)).
   Everybody who is working is interrupted, and whatever is being generated is stopped: the confirmation tells you
   how many [jobs](jobs.md) run at that moment, so pick a quiet time.
3. The database is upgraded when the new version starts, after a
   [copy of it](database-backups.md#copy-before-an-upgrade) was saved. Make a [database backup](database-backups.md)
   of your own first as well.

When the build fails, nothing is stopped: the version from before keeps running, **Last update** says *failed* and
the log says why. The next update builds again, also when there is nothing new to pull.

## Setting up the watcher

Marginalia runs inside a container and can reach neither the Git checkout nor Docker; giving it that access would
make everybody who gets into the application an administrator of the server. So it only asks: it writes a file into
its [data folder](index.md#the-data-folder), and the watcher, which runs on the server outside the container, does
the work. The watcher is the script `tools/update-watcher/watch.sh` of the checkout; all it ever runs is `rebuild.sh`
of the same checkout (see [Automatic updates](../getting-started/docker-server.md#automatic-updates)).

### Linux with systemd

In the checkout on the server (the folder with `docker-compose.yml`):

```sh
sudo tools/update-watcher/install.sh
```

It installs and starts three units:

| Unit | |
|---|---|
| `marginalia-update.service` | Runs the watcher once. |
| `marginalia-update.path` | Starts it the moment the application asks for an update. |
| `marginalia-update.timer` | Starts it two minutes after the boot and then every 15 minutes, to look for new commits. |

Open **Admin → Update** and click **Refresh**: the tab shows the branch and the newest commit. That is all.

| Option | |
|---|---|
| `--name NAME` | The name of the units (default `marginalia-update`). Two checkouts on one computer (a test and a production installation) each need their own name. |
| `--user USER` | Runs the watcher as this user instead of root, see [below](#who-the-watcher-runs-as). |
| `--uninstall` | Stops and removes the units (with the same `--name`). |

`systemctl status marginalia-update.service` shows the last run and `journalctl -u marginalia-update.service` its
output (the same text as **Log**, and the reason when the watcher itself could not work). The units contain the path
of the checkout: after moving the checkout, run `install.sh` again.

### Without systemd (cron)

Run the watcher every minute; it does nothing when nobody asked for an update and it looked at the remote less than
15 minutes ago, so this costs nothing:

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Put the line into the crontab of root (`sudo crontab -e`), or of another user, see below. An update then starts
within a minute of the click.

### Settings

The watcher reads these environment variables (with systemd: `sudo systemctl edit marginalia-update.service` and
`Environment=NAME=value` lines under `[Service]`):

| Variable | |
|---|---|
| `MARGINALIA_DATA_DIR` | The data folder on the server, when it is not `data` in the checkout (the left side of the volume in `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | The arguments of `rebuild.sh`, `--force` by default. On a server without the memory for the application and the build together, use `--force --stop-first`: Marginalia is then stopped before the build and is down for all of it. |
| `MARGINALIA_CHECK_MINUTES` | How old the status may get before a run without a request looks at the remote again, 15 by default. With systemd the timer decides how often the watcher runs. |

### Who the watcher runs as

By default the watcher runs as **root**: it has to use Docker and to write into the data folder, which belongs to the
user of the container. Git itself is run as the owner of the checkout, so the files of Git stay theirs.

The watcher runs what the branch contains (`rebuild.sh`, the `Dockerfile`), as every update by hand does: follow only
a repository you trust, and keep the checkout writable only for its owner, because whoever can change `rebuild.sh` or
`watch.sh` there runs commands as the user of the watcher.

With `--user USER` (or the crontab of that user) it runs as somebody else. That user must be allowed to use Docker
(which on most systems is as much as root), must own the checkout, and must be able to write into `data/update` and
`data/extensions`, which normally belong to the user of the container.

### Windows and macOS

There is no ready watcher for Docker Desktop. Update with `rebuild.bat` / `rebuild.sh` by hand or from a scheduled
task, see [Automatic updates](../getting-started/docker-server.md#automatic-updates).

## Troubleshooting

| | |
|---|---|
| *The update watcher does not run on the server* | The watcher wrote no status for an hour, or never. Check `systemctl status marginalia-update.timer marginalia-update.path` and `journalctl -u marginalia-update.service`. If the data folder is not `data` in the checkout, set `MARGINALIA_DATA_DIR`. |
| *Branch ... follows no remote branch* | The checkout is on a branch without an upstream (or on a detached commit), so there is nothing to pull. On the server: `git switch <branch>` or `git branch --set-upstream-to=origin/<branch>`. |
| *The server could not reach the Git repository* | `git fetch` failed at the last look (no network, a private repository without credentials for the owner of the checkout). The newest commit shown may be old. |
| The update was requested and nothing happens | With cron it starts within a minute. A request that nobody took for an hour is thrown away, so that it does not start by surprise when the watcher is repaired. |
| *Last update: ..., failed* | Read **Log**. A failed `git pull` usually means the checkout has changes of its own (edited files, local commits): resolve it on the server. A failed build leaves the old version running. |

The files of the watcher are in `update` of the data folder: `status.properties` (what the tab shows), `update.log`
(the last update) and, for a moment, `request`.
