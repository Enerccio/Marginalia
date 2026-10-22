#!/bin/bash
# shoot-all.sh   Retakes all screenshots that are not taken by the setup chain itself (bin/chain.txt takes those on the way).
#   Needs the chain to be done (bin/rebuild.sh). Pictures go to screenshots/out/; bin/publish.sh copies them to manual/images.
# What state a part runs on:
#   13-db-backup   the story, summaries, pictures, users, trash, backups - before any plugin data (core screens)
#   21-chaptermarker  everything, with the plugin data (plugin screens, export)
#   22-arcoutline     everything incl. the Arc Outline (the sidebar with the tabs dragged apart)
. "$(dirname "$0")/env.sh"
cd "$SHOT_ROOT"
"$SHOT_ROOT/bin/start-llm.sh" >/dev/null
python3 bin/make-broken-plugin.py >/dev/null
run() { echo "=== $*"; node "flows/$@" 2>&1 | grep -v "^  saved\|dismissed"; }
# the steps hold the plugin JARs of the day they were made: swap in the ones that were built now, then start
on() { "$SHOT_ROOT/bin/restore.sh" "$1" --no-start >/dev/null; "$SHOT_ROOT/bin/refresh-plugins.sh" >/dev/null; "$SHOT_ROOT/bin/start-app.sh" >/dev/null; curl -s -X POST "localhost:$SHOT_LLM_PORT/v1/_reset" >/dev/null; }

on 13-db-backup
run shoot-admin.mjs
run shoot-workspace.mjs
run shoot-book.mjs
run shoot-book-extra.mjs
run shoot-lorebook-tab.mjs
run shoot-phone.mjs
# these change the data: every one on a fresh copy
for part in language quick-start context-warning verification; do
  on 13-db-backup
  run shoot-extra.mjs --only $part
done
on 13-db-backup
run shoot-commands.mjs                           # /take-backup adds a backup to the book: a copy, discarded
on 13-db-backup
run shoot-search-replace.mjs                     # looks only, but leaves the workspace on the Lorebooks tab: a copy, discarded
on 13-db-backup
run shoot-help.mjs                               # chooses the model of the help assistant (installation wide), a permission for the administrator: a copy, discarded
on 13-db-backup
run shoot-permissions.mjs                        # changes the permissions of ginny: a copy, discarded
on 13-db-backup
run plugin-googlegenai.mjs                       # installs the Gemini plugin: a copy, discarded
on 13-db-backup
run shoot-books-extra.mjs                        # imports a lorebook with subbooks and a book from a zip: a copy, discarded
on 22-arcoutline
run shoot-agents.mjs                             # four agents with runs (the fake model answers from its queue), three more books: a copy, discarded
on 22-arcoutline
run shoot-tabs.mjs                               # drags the sidebar tabs apart (saved with the book): a copy, discarded
# the red bar for invalid extensions: the Author's Note JAR is replaced by the broken one and its report is deleted, so that the
# application verifies it again at the start
on 13-db-backup
"$SHOT_ROOT/bin/stop-app.sh" >/dev/null
cp out/broken/authorsnote-1.4.0.jar "$SHOT_HOME/.marginalia/extensions/authorsnote-1.4.0.jar"
rm -f "$SHOT_HOME/.marginalia/extensions/authorsnote-1.4.0.valid"
"$SHOT_ROOT/bin/start-app.sh" >/dev/null
run shoot-invalid-extensions.mjs                  # a copy, discarded
# plugin screens
on 21-chaptermarker
run shoot-plugin-characters.mjs
on 21-chaptermarker                                # the sidebar width of the Characters flow is saved with the book: the other flows click on the menu below the story
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
