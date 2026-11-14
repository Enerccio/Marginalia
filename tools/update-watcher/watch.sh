#!/bin/bash

# The host side of Admin -> Update (manual/user/administration/updates.md).
#
# The application runs in a container and can neither reach the Git checkout nor Docker, so it only asks: it writes
# the file data/update/request. This script runs on the host (a systemd unit installed by install.sh, or cron) and
#   1. when the request is there, removes it and runs rebuild.sh --force, with the output in data/update/update.log,
#   2. fetches the remote of the branch the checkout is on and writes data/update/status.properties, where the
#      application reads the branch, the newest commit and how the last update ended.
#
# It can be run as often as you like: without a request it does nothing until the status is CHECK_MINUTES old.
#
# Environment:
#   MARGINALIA_DATA_DIR      the data folder on the host (default: data in the repository root)
#   MARGINALIA_REBUILD_ARGS  arguments of rebuild.sh (default: --force; "--force --stop-first" for a small server)
#   MARGINALIA_CHECK_MINUTES minutes between two looks at the remote (default: 15)

set -u

REPO="$(cd "$(dirname "$0")/../.." && pwd)" || exit 1
DATA="${MARGINALIA_DATA_DIR:-$REPO/data}"
REBUILD_ARGS="${MARGINALIA_REBUILD_ARGS:---force}"
CHECK_MINUTES="${MARGINALIA_CHECK_MINUTES:-15}"
# a request older than this was made while nothing was watching: it is dropped, not run by surprise
STALE_REQUEST_MINUTES=60

DIR="$DATA/update"
REQUEST="$DIR/request"
STATUS="$DIR/status.properties"
LOG="$DIR/update.log"

cd "$REPO" || exit 1

if [ ! -d "$DATA" ]; then
    echo "The data folder $DATA does not exist yet (start Marginalia once), nothing to do."
    exit 0
fi

if [ ! -d "$DIR" ]; then
    mkdir -p "$DIR" || { echo "Cannot create $DIR: run the watcher as root, or make the folder writable for $(id -un)."; exit 1; }
    # the application (the jetty user of the container, the owner of the data folder) writes the request into it
    if [ "$(id -u)" = "0" ]; then
        chown --reference="$DATA" "$DIR"
    fi
fi

# one run at a time: the timer, the path unit and cron may all start it
exec 9>"$DIR/.lock" || { echo "Cannot write into $DIR: run the watcher as root, or make the folder writable for $(id -un)."; exit 1; }
flock -n 9 || exit 0

# see rebuild.sh: as root in a checkout of another user, git runs as the owner of the checkout
if [ "$(id -u)" = "0" ] && command -v runuser >/dev/null 2>&1; then
    OWNER=$(stat -c %U . 2>/dev/null)
    if [ -n "$OWNER" ] && [ "$OWNER" != "root" ] && [ "$OWNER" != "UNKNOWN" ]; then
        OWNER_HOME=$(getent passwd "$OWNER" | cut -d: -f6)
        git() { runuser -u "$OWNER" -- env HOME="$OWNER_HOME" git "$@"; }
    fi
fi

# how the last update ended is kept over the runs
previous() {
    [ -f "$STATUS" ] && sed -n "s/^$1=//p" "$STATUS" | head -n 1
}
LAST_RUN="$(previous lastRun)"
LAST_RESULT="$(previous lastResult)"
FETCH_FAILED=0

write_status() {
    local tmp
    tmp="$(mktemp "$DIR/.status.XXXXXX")" || return 1
    {
        echo "state=$1"
        echo "branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null)"
        echo "upstream=$(git rev-parse --abbrev-ref '@{u}' 2>/dev/null)"
        echo "local=$(git rev-parse HEAD 2>/dev/null)"
        echo "remote=$(git rev-parse '@{u}' 2>/dev/null)"
        echo "fetchFailed=$FETCH_FAILED"
        echo "checked=$(date +%s)"
        [ -n "$LAST_RUN" ] && echo "lastRun=$LAST_RUN"
        [ -n "$LAST_RESULT" ] && echo "lastResult=$LAST_RESULT"
    } > "$tmp"
    chmod 644 "$tmp"
    mv -f "$tmp" "$STATUS"
}

REQUESTED=0
if [ -e "$REQUEST" ]; then
    if [ -n "$(find "$REQUEST" -mmin +"$STALE_REQUEST_MINUTES" 2>/dev/null)" ]; then
        echo "Dropping an update request older than $STALE_REQUEST_MINUTES minutes."
    else
        REQUESTED=1
        echo "Update requested: $(head -c 200 "$REQUEST" | tr -d '\r' | tr '\n' ' ')"
    fi
    # first, before anything long: the path unit starts the service again while the file is there
    rm -f "$REQUEST" || { echo "Cannot remove $REQUEST: run the watcher as root, or make the folder writable for $(id -un)."; exit 1; }
fi

if [ "$REQUESTED" = "1" ]; then
    write_status running
    # word splitting of the arguments is wanted
    # shellcheck disable=SC2086
    {
        echo "=== Update started $(date '+%Y-%m-%d %H:%M:%S %Z'): rebuild.sh $REBUILD_ARGS"
        "$REPO/rebuild.sh" $REBUILD_ARGS
        RC=$?
        echo "=== Update finished $(date '+%Y-%m-%d %H:%M:%S %Z'), exit code $RC"
        exit $RC
    } 2>&1 | tee "$LOG"
    RC=${PIPESTATUS[0]}
    chmod 644 "$LOG"
    LAST_RUN="$(date +%s)"
    if [ "$RC" = "0" ]; then
        LAST_RESULT=ok
    else
        LAST_RESULT=failed
    fi
elif [ -f "$STATUS" ] && [ -z "$(find "$STATUS" -mmin +"$CHECK_MINUTES" 2>/dev/null)" ] && [ "$(previous state)" = "idle" ]; then
    # looked at the remote a moment ago
    exit 0
fi

git fetch --quiet || FETCH_FAILED=1
write_status idle
