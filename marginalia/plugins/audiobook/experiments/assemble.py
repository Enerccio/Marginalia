#!/usr/bin/env python3
"""Assembly of a scene from rendered pieces: every piece to the same loudness (loudnorm I=-20, one pass) with a 10 ms fade in so no piece
starts with a click, the pauses by position (after a heading 1.0 s, a new paragraph 0.6 s, speech followed by "he said" 0.12 s, else 0.25 s),
a script file that lists everything that went into it.

  python3 assemble.py story.txt pl_segments.json "pl_%02d.mp3" scene.mp3
"""
import json, os, re, subprocess, sys
from common import *

story_file, segments_file, pattern, out = sys.argv[1:5]
segs = json.load(open(segments_file))
story = open(story_file).read().strip()
pieces = []                                       # (paragraph, kind) of every piece, the same split as pipeline.py
for pi, para in enumerate(re.split(r"\n\s*\n", story)):
    para = re.sub(r"^#+\s*", "", para.strip())
    pos = 0
    for m in re.finditer(r'"([^"]+)"', para):
        if para[pos:m.start()].strip():
            pieces.append((pi, "N"))
        pieces.append((pi, "D"))
        pos = m.end()
    if para[pos:].strip():
        pieces.append((pi, "N"))
assert len(pieces) == len(segs), (len(pieces), len(segs))
os.makedirs("scene", exist_ok=True)
files, pauses = [], []
for i, s in enumerate(segs):
    norm = "scene/p%02d.wav" % i
    subprocess.run(["ffmpeg", "-loglevel", "error", "-y", "-i", pattern % i, "-ac", "1", "-ar", "24000", "-af", "loudnorm=I=-20:TP=-2:LRA=7,afade=t=in:d=0.01", norm], check=True)
    nxt = pieces[i + 1] if i + 1 < len(segs) else None
    if nxt is None:
        pause = 0.0
    elif i == 0:
        pause = 1.0
    elif nxt[0] != pieces[i][0]:
        pause = 0.6
    elif pieces[i][1] == "D" and nxt[1] == "N" and re.match(r"^(he|she|they|I|[A-Z][a-zá-ž]+) (said|asked|answered|whispered|murmured)", segs[i + 1]["text"]):
        pause = 0.12
    else:
        pause = 0.25
    files.append(norm)
    pauses.append(pause)
    s["pause_after"] = pause
json.dump(segs, open("scene_script.json", "w"), ensure_ascii=False, indent=1)
print("%s: %.1f s, %d pieces" % (out, concat(files, pauses, out), len(files)))
