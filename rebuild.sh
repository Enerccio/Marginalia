#!/bin/bash

# Updates a Docker server from its Git checkout: pulls the branch the checkout is on, builds the plugins and the image
# and restarts the container. The application keeps running while the new version is built, so a failed build leaves
# the old version up.
#
# Usage: ./rebuild.sh [--force] [--stop-first]
# --force       rebuilds and restarts even when there are no new commits.
# --stop-first  stops the container before the build (the order of older versions), for a server that does not have
#               the memory for the application and the build together. The application is down for the whole build.
FORCE=0
STOP_FIRST=0
for ARG in "$@"; do
    case "$ARG" in
        --force) FORCE=1 ;;
        --stop-first) STOP_FIRST=1 ;;
        *)
            echo "Usage: $0 [--force] [--stop-first]"
            exit 1
            ;;
    esac
done

cd "$(dirname "$0")" || exit 1

# Run as root (sudo, a system service) in a checkout of another user: git refuses a repository of another owner, and
# the files it would write into .git would belong to root. So git runs as the owner of the checkout.
if [ "$(id -u)" = "0" ] && command -v runuser >/dev/null 2>&1; then
    OWNER=$(stat -c %U . 2>/dev/null)
    if [ -n "$OWNER" ] && [ "$OWNER" != "root" ] && [ "$OWNER" != "UNKNOWN" ]; then
        OWNER_HOME=$(getent passwd "$OWNER" | cut -d: -f6)
        git() { runuser -u "$OWNER" -- env HOME="$OWNER_HOME" git "$@"; }
    fi
fi

fail() {
    echo "$1"
    exit 1
}

# Fetch the latest commit info of the branch's remote
git fetch || fail "Git fetch failed"

# Compare current local commit with remote upstream commit (the upstream of the branch that is checked out)
LOCAL=$(git rev-parse HEAD) || fail "Not a Git checkout"
REMOTE=$(git rev-parse '@{u}') || fail "The branch has no upstream to update from"

# The commit the running container was built from, written at the end of a successful run. It differs from the
# checkout after a build that failed: the next run then builds again although there is nothing new to pull.
DEPLOYED_FILE=".rebuild-deployed"
DEPLOYED=$(cat "$DEPLOYED_FILE" 2>/dev/null)
[ -z "$DEPLOYED" ] && DEPLOYED="$LOCAL"
write_deployed() {
    echo "$1" > "$DEPLOYED_FILE"
    # written as root: the owner of the checkout runs the script too
    [ -n "${OWNER_HOME:-}" ] && chown "$OWNER" "$DEPLOYED_FILE"
    return 0
}

if [ "$LOCAL" = "$REMOTE" ] && [ "$DEPLOYED" = "$LOCAL" ] && [ "$FORCE" = "0" ]; then
    echo "No updates found in Git repository. Containers remain running."
    exit 0
fi

if [ "$FORCE" = "1" ]; then
    echo "Forced rebuild. Updating services..."
elif [ "$LOCAL" = "$REMOTE" ]; then
    echo "The last build did not finish. Updating services..."
else
    echo "New changes detected. Updating services..."
fi

if [ "$STOP_FIRST" = "1" ]; then
    docker compose down
fi

# Remember what runs now, so that a build that fails after the pull is made again by the next run
write_deployed "$DEPLOYED"

# Pull the new code
git pull || fail "Git pull failed"

# Build the plugins in a container (compose file in marginalia/plugins, jars end up in its build folder)
PLUGINS_COMPOSE="marginalia/plugins/docker-compose.yml"
docker compose -f "$PLUGINS_COMPOSE" build || fail "Plugin build failed"
docker compose -f "$PLUGINS_COMPOSE" run --rm plugins || fail "Plugin build failed"

# Build the image of the application; the old container still runs
docker compose build || fail "Image build failed"

# Stop the old container, overwrite the installed plugins (they are loaded on the application start) and start the
# new one
docker compose down
mkdir -p data/extensions
cp -f marginalia/plugins/build/*.jar data/extensions/ || fail "Copying plugins failed"
docker compose up -d || fail "Starting the container failed"

write_deployed "$(git rev-parse HEAD)"
