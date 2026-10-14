#!/bin/bash
# Stops the application (the Maven JVM and the Jetty JVM listening on the port).
. "$(dirname "$0")/env.sh"
pids=$(lsof -ti tcp:$SHOT_PORT -sTCP:LISTEN 2>/dev/null)
[ -f "$SHOT_ROOT/.run/app.pid" ] && pids="$pids $(cat "$SHOT_ROOT/.run/app.pid")"
for p in $pids; do kill "$p" 2>/dev/null; done
for i in $(seq 1 30); do
  lsof -ti tcp:$SHOT_PORT -sTCP:LISTEN >/dev/null 2>&1 || { rm -f "$SHOT_ROOT/.run/app.pid"; echo stopped; exit 0; }
  sleep 1
done
for p in $pids; do kill -9 "$p" 2>/dev/null; done
echo "killed"
