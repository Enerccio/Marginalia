#!/bin/bash
# Downloads the one third party picture the screenshots use: "Harry Potter.jpg" from Wikimedia Commons
# (author Mademoiselle Ortie, CC BY-SA 4.0, credited on manual/credits.md). Not kept in Git (6 MB).
. "$(dirname "$0")/env.sh"
mkdir -p "$SHOT_ROOT/assets"
f="$SHOT_ROOT/assets/Harry_Potter.jpg"
[ -s "$f" ] && exit 0
curl -sL -m 120 -A "marginalia-docs/1.0 (screenshots)" "https://commons.wikimedia.org/wiki/Special:FilePath/Harry_Potter.jpg" -o "$f"
file "$f" | grep -q JPEG || { echo "download of Harry_Potter.jpg failed"; rm -f "$f"; exit 1; }
