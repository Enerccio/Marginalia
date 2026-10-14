#!/bin/bash
# snapshot.sh <name>   Stops the app, stores the whole data folder as steps/<name>.tar.gz, starts the app again.
# restore.sh <name>    Stops the app, replaces the data folder with steps/<name>.tar.gz, starts the app again.
. "$(dirname "$0")/env.sh"
name="$1"; [ -z "$name" ] && { echo "usage: snapshot.sh <name>"; exit 2; }
mkdir -p "$SHOT_ROOT/steps"
"$SHOT_ROOT/bin/stop-app.sh"
tar -C "$SHOT_HOME" -czf "$SHOT_ROOT/steps/$name.tar.gz" .marginalia
echo "saved steps/$name.tar.gz ($(du -h "$SHOT_ROOT/steps/$name.tar.gz" | cut -f1))"
[ "$2" = "--no-start" ] || "$SHOT_ROOT/bin/start-app.sh"
