#!/bin/bash

# Usage: ./rebuild.sh [--force]
# --force rebuilds and restarts even when there are no new commits.
FORCE=0
if [ "$1" = "--force" ]; then
    FORCE=1
elif [ -n "$1" ]; then
    echo "Usage: $0 [--force]"
    exit 1
fi

# Fetch the latest commit info from the remote repository
git fetch origin

# Compare current local commit with remote upstream commit
LOCAL=$(git rev-parse HEAD)
REMOTE=$(git rev-parse @{u})

if [ "$LOCAL" = "$REMOTE" ] && [ "$FORCE" = "0" ]; then
    echo "No updates found in Git repository. Containers remain running."
    exit 0
fi

if [ "$FORCE" = "1" ]; then
    echo "Forced rebuild. Updating services..."
else
    echo "New changes detected. Updating services..."
fi

# Bring down existing containers
docker compose down

# Pull the new code
git pull || { echo "Git pull failed"; exit 1; }

# Build the plugins in a container (compose file in marginalia/plugins, jars end up in its build folder) and
# overwrite the installed ones, they are loaded on the application start
PLUGINS_COMPOSE="marginalia/plugins/docker-compose.yml"
docker compose -f "$PLUGINS_COMPOSE" build || { echo "Plugin build failed"; exit 1; }
docker compose -f "$PLUGINS_COMPOSE" run --rm plugins || { echo "Plugin build failed"; exit 1; }
mkdir -p data/extensions
cp -f marginalia/plugins/build/*.jar data/extensions/ || { echo "Copying plugins failed"; exit 1; }

# Rebuild and start containers in detached mode
docker compose up -d --build