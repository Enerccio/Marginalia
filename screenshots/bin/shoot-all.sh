#!/bin/bash
# shoot-all.sh   Retakes all screenshots that are not taken by the setup chain itself (bin/chain.txt takes those on the way).
#   Needs the chain to be done (bin/rebuild.sh). Pictures go to screenshots/out/; bin/publish.sh copies them to manual/images.
# What state a part runs on:
#   13-db-backup   the story, summaries, pictures, users, trash, backups - before any plugin data (core screens)
#   21-chaptermarker  everything, with the plugin data (plugin screens, export)
. "$(dirname "$0")/env.sh"
cd "$SHOT_ROOT"
"$SHOT_ROOT/bin/start-llm.sh" >/dev/null
python3 bin/make-broken-plugin.py >/dev/null
run() { echo "=== $*"; node "flows/$@" 2>&1 | grep -v "^  saved\|dismissed"; }
on() { "$SHOT_ROOT/bin/restore.sh" "$1" >/dev/null; curl -s -X POST "localhost:$SHOT_LLM_PORT/v1/_reset" >/dev/null; }

on 13-db-backup
run shoot-admin.mjs
run shoot-workspace.mjs
run shoot-book.mjs
run shoot-book-extra.mjs
run shoot-phone.mjs
# these change the data: every one on a fresh copy
for part in language quick-start context-warning verification; do
  on 13-db-backup
  run shoot-extra.mjs --only $part
done
# plugin screens
on 21-chaptermarker
run shoot-plugin-characters.mjs
run plugin-instructionexporter.mjs
run shoot-export.mjs
run plugin-chinese.mjs
echo "=== annotated UI maps"
SP="${PILLOW_PATH:-$SHOT_ROOT/.run/pil}"
[ -d "$SP" ] || pip3 install --quiet --target "$SP" pillow
mkdir -p out/maps && cp out/dev-ui-map-workspace-not-annotated.png out/dev-ui-map-book-not-annotated.png out/maps/ \
  && PYTHONPATH="$SP" python3 ../tools/annotate_ui_maps.py --images out/maps \
  && cp out/maps/dev-ui-map-workspace.png out/maps/dev-ui-map-book.png out/
on 21-chaptermarker
echo done
