#!/bin/bash
# Makes the pictures the screenshots upload. The painting is the cover of the dataset book of the tests (it is in the fixture
# marginalia/src/test/resources/workset/book.zip as resources/1.jpeg): "Interior of a Cottage in Scheveningen" (1887) by Willem Adriaan
# Alexander Liernur, public domain, from Rawpixel (credited on manual/credits.md). Not kept in Git (assets/*.jpg is ignored).
# assets/portrait.jpg is a square crop of it (the teapot on the table) for the portrait of a character.
. "$(dirname "$0")/env.sh"
mkdir -p "$SHOT_ROOT/assets"
f="$SHOT_ROOT/assets/cover.jpg"
if [ ! -s "$f" ]; then
  unzip -p "$SHOT_ROOT/../marginalia/src/test/resources/workset/book.zip" resources/1.jpeg > "$f"
  file "$f" | grep -q JPEG || { echo "taking cover.jpg out of the fixture failed"; rm -f "$f"; exit 1; }
fi
p="$SHOT_ROOT/assets/portrait.jpg"
if [ ! -s "$p" ]; then
  sips -c 300 300 --cropOffset 270 300 "$f" --out "$p" > /dev/null || { echo "crop of portrait.jpg failed"; rm -f "$p"; exit 1; }
fi
