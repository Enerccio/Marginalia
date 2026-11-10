#!/bin/bash
. "$(dirname "$0")/env.sh"
mkdir -p "$SHOT_ROOT/.run"
pid=$("$SHOT_ROOT/bin/listening-pids.sh" $SHOT_LLM_PORT); [ -n "$pid" ] && { echo "already running ($pid)"; exit 0; }
nohup node --watch-path="$SHOT_ROOT/llm" --watch-path="$SHOT_ROOT/dataset" "$SHOT_ROOT/llm/mock-llm.mjs" $SHOT_LLM_PORT > "$SHOT_ROOT/.run/llm.log" 2>&1 &
sleep 1; curl -s localhost:$SHOT_LLM_PORT/v1/models | head -c 200; echo
