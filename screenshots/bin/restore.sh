#!/bin/bash
. "$(dirname "$0")/env.sh"
name="$1"; [ -z "$name" ] && { echo "usage: restore.sh <name>"; ls "$SHOT_ROOT/steps"; exit 2; }
[ -f "$SHOT_ROOT/steps/$name.tar.gz" ] || { echo "no such step: $name"; ls "$SHOT_ROOT/steps"; exit 1; }
"$SHOT_ROOT/bin/stop-app.sh"
rm -rf "$SHOT_HOME/.marginalia"
tar -C "$SHOT_HOME" -xzf "$SHOT_ROOT/steps/$name.tar.gz"
echo "restored $name"
[ "$2" = "--no-start" ] || "$SHOT_ROOT/bin/start-app.sh"
