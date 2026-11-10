#!/bin/bash
. "$(dirname "$0")/env.sh"
pid=$("$SHOT_ROOT/bin/listening-pids.sh" $SHOT_LLM_PORT); [ -n "$pid" ] && kill $pid && echo stopped
