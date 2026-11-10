#!/bin/bash
# shoot-jobs.sh [<state>]   The pictures of the jobs (Admin -> Jobs and /jobs): restores a state (default 13-db-backup, a discarded copy), refreshes the
#   plugin JARs, starts the application once for its migration (V28), writes some history with bin/seed-jobs.py (the application is stopped)
#   and runs flows/shoot-jobs.mjs with the fake model. Prints the newest jobs of the database at the end: the generation of the flow is in it.
. "$(dirname "$0")/env.sh"
cd "$SHOT_ROOT"
state="${1:-13-db-backup}"
"$SHOT_ROOT/bin/start-llm.sh" >/dev/null
"$SHOT_ROOT/bin/restore.sh" "$state" --no-start >/dev/null
"$SHOT_ROOT/bin/refresh-plugins.sh" >/dev/null
"$SHOT_ROOT/bin/start-app.sh" >/dev/null; "$SHOT_ROOT/bin/stop-app.sh" >/dev/null
python3 -I "$SHOT_ROOT/bin/seed-jobs.py" "$SHOT_HOME/.marginalia/marginalia.sqlite"
"$SHOT_ROOT/bin/start-app.sh" >/dev/null
curl -s -X POST "localhost:$SHOT_LLM_PORT/v1/_reset" >/dev/null
node flows/shoot-jobs.mjs 2>&1 | grep -v "^  saved\|dismissed"
sqlite3 "$SHOT_HOME/.marginalia/marginalia.sqlite" "select j.id, j.kind, j.status, j.callCount, (j.endedAt - j.startedAt) as ms, (select group_concat(c.callType || ':' || c.status) from job_calls c where c.job_id = j.id) from jobs j order by j.startedAt desc limit 4"
echo done
