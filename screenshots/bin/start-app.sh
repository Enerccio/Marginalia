#!/bin/bash
# Starts the production WAR in Jetty (maven plugin) with the screenshot data folder. Logs: screenshots/.run/app.log
. "$(dirname "$0")/env.sh"
mkdir -p "$SHOT_ROOT/.run" "$SHOT_HOME"
cd "$REPO_ROOT/marginalia"
export MAVEN_OPTS="-XX:+EnableDynamicAgentLoading -Djdk.attach.allowAttachSelf=true -Duser.home=$SHOT_HOME -Dmaven.repo.local=$HOME/.m2/repository -Duser.language=en -Duser.country=US"
nohup mvn -s "$HOME/.m2/settings.xml" -DskipTests jetty:run-war -Djetty.http.port=$SHOT_PORT > "$SHOT_ROOT/.run/app.log" 2>&1 &
echo $! > "$SHOT_ROOT/.run/app.pid"
echo "started pid $(cat "$SHOT_ROOT/.run/app.pid"), waiting for $SHOT_URL"
for i in $(seq 1 120); do
  code=$(curl -s -o /dev/null -w '%{http_code}' "$SHOT_URL/" || true)
  if [ "$code" = "200" ] || [ "$code" = "302" ]; then echo "up after ${i}x2s"; exit 0; fi
  sleep 2
done
echo "not up, see $SHOT_ROOT/.run/app.log"; exit 1
