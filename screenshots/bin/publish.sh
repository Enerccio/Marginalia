#!/bin/bash
# publish.sh [--dry]   Copies the finished pictures from screenshots/out/ (names not starting with _) to manual/images/
#                      and says which are new or changed. It does not run `retype build` and does not commit.
. "$(dirname "$0")/env.sh"
dry=0; [ "$1" = "--dry" ] && dry=1
new=0; changed=0; same=0
for f in "$SHOT_ROOT"/out/*.png; do
  name=$(basename "$f")
  case "$name" in _*) continue;; esac
  target="$REPO_ROOT/manual/images/$name"
  if [ ! -f "$target" ]; then echo "new      $name"; new=$((new+1))
  elif cmp -s "$f" "$target"; then same=$((same+1)); continue
  else echo "changed  $name"; changed=$((changed+1)); fi
  [ $dry = 1 ] || cp "$f" "$target"
done
echo "$new new, $changed changed, $same identical$([ $dry = 1 ] && echo ' (dry run, nothing copied)')"
