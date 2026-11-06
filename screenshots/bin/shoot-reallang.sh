#!/bin/bash
# shoot-reallang.sh [<state>]   The pictures of the Real Language Translator: restores a state (default 23-betareaders, the last plugin step), refreshes the plugin
#   JARs, writes the language and a translated book into the database (bin/seed-reallang.py, the application is stopped) and runs flows/plugin-reallang.mjs.
#   The plugin's JAR must be built (mvn package in marginalia/plugins/reallang-translator).
. "$(dirname "$0")/env.sh"
cd "$SHOT_ROOT"
state="${1:-23-betareaders}"
"$SHOT_ROOT/bin/start-llm.sh" >/dev/null
"$SHOT_ROOT/bin/restore.sh" "$state" --no-start >/dev/null
# the plugin is not in the steps: copy its JAR in as a new extension, the others are refreshed
cp "$REPO_ROOT/marginalia/plugins/reallang-translator/target/reallang-translator-"*.jar "$SHOT_HOME/.marginalia/extensions/"
"$SHOT_ROOT/bin/refresh-plugins.sh" >/dev/null
# the migrations of the application (V26) have to run before the passages are written: start once, stop, seed, start
"$SHOT_ROOT/bin/start-app.sh" >/dev/null; "$SHOT_ROOT/bin/stop-app.sh" >/dev/null
python3 -I "$SHOT_ROOT/bin/seed-reallang.py" "$SHOT_HOME/.marginalia/marginalia.sqlite"
"$SHOT_ROOT/bin/start-app.sh" >/dev/null
sqlite3 "$SHOT_HOME/.marginalia/marginalia.sqlite" "select uuid from manuscripts where name = 'Unregistered'" > "$SHOT_ROOT/.run/book-uuid.txt"
curl -s -X POST "localhost:$SHOT_LLM_PORT/v1/_reset" >/dev/null
node flows/plugin-reallang.mjs 2>&1 | grep -v "^  saved\|dismissed"
echo done
