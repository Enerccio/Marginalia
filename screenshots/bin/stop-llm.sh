#!/bin/bash
. "$(dirname "$0")/env.sh"
pid=$(lsof -ti tcp:$SHOT_LLM_PORT -sTCP:LISTEN 2>/dev/null); [ -n "$pid" ] && kill $pid && echo stopped
