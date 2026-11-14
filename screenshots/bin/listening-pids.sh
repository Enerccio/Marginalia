#!/bin/bash
# listening-pids.sh <port>   Prints the pids that listen on the TCP port (lsof where it is installed, ss otherwise); exit 1 when there are none.
port="$1"
if command -v lsof >/dev/null 2>&1; then
  pids=$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null)
else
  pids=$(ss -ltnpH "sport = :$port" 2>/dev/null | grep -o 'pid=[0-9]*' | cut -d= -f2 | sort -u)
fi
[ -n "$pids" ] || exit 1
echo "$pids"
