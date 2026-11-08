#!/usr/bin/env python3
"""The audiobook pipeline by hand: text -> pieces (split in code) -> LLM labels (speaker, delivery, intensity, sound tags) -> unload the LLM
-> start the TTS -> one /tts call per piece with the voice of its speaker -> pieces joined with pauses -> stop the TTS.

  python3 pipeline.py story.txt Narrator=predefined:Henry.wav Tomas=clone:ref_male.wav Mizuki=clone:ref_luna.wav

The reference clips must be on the server already (/upload_reference). Writes pl_segments.json (the "script"), pl_NN.mp3 and book.mp3.
The text of the book is split in code and never given back by the model: a labelled piece that is not the original plus tags is discarded.
"""
import json, re, sys
from common import *

story_file, *cast_args = sys.argv[1:]
CAST = {}
for a in cast_args:
    name, spec = a.split("=", 1)
    mode, voice = spec.split(":", 1)
    CAST[name] = (mode, voice)
NARRATOR = CAST.get("Narrator") or sys.exit("a Narrator=... voice is needed")
CHARACTERS = [n for n in CAST if n != "Narrator"]


def norm(t):
    t = re.sub(r"\[%s\]" % TAGS, "", t)
    t = re.sub(r'["“”#]', "", t)
    return re.sub(r"\s+", " ", t).strip()


# 1. the pieces: narration and direct speech between straight quotes, paragraph by paragraph
story = open(story_file).read().strip()
pieces = []
for para in re.split(r"\n\s*\n", story):
    para = re.sub(r"^#+\s*", "", para.strip())
    pos = 0
    for m in re.finditer(r'"([^"]+)"', para):
        if para[pos:m.start()].strip():
            pieces.append({"kind": "N", "text": para[pos:m.start()].strip()})
        pieces.append({"kind": "D", "text": m.group(1).strip()})
        pos = m.end()
    if para[pos:].strip():
        pieces.append({"kind": "N", "text": para[pos:].strip()})
for i, p in enumerate(pieces):
    p["id"] = i
log("split in code:", len(pieces), "pieces,", sum(1 for p in pieces if p["kind"] == "D"), "of speech")

# 2. the labels (the LLM only labels; the narration gets no tags)
SYSTEM = """You label the pieces of a story chapter for an audiobook with several voices. The pieces are numbered; N is narration, D is direct speech. The characters are: %s. For every piece answer with:
- "id": the number of the piece;
- "speaker": for N always "Narrator"; for D the character who says it, resolved from the context;
- "delivery": how it should be read (neutral, calm, tense, sad, angry, joyful, amused, whisper, shout) and "intensity" 0.0 to 1.0;
- "tagged_text": for N always an empty string. For D the piece word for word, unchanged, except that you may insert a sound tag where the context clearly calls for that sound from the speaking character, and rarely (at most one tag per 80 words of the whole chapter). Allowed tags: [laugh] [chuckle] [sigh] [gasp] [cough] [clear throat] [sniff] [groan], written exactly like that. Do not change, add or remove any other word.
Answer with the JSON only, one entry per piece, in order.""" % ", ".join(CHARACTERS)
SCHEMA = {"type": "object", "additionalProperties": False, "required": ["labels"], "properties": {"labels": {"type": "array", "items": {
    "type": "object", "additionalProperties": False, "required": ["id", "speaker", "delivery", "intensity", "tagged_text"], "properties": {
        "id": {"type": "integer"}, "speaker": {"type": "string", "enum": ["Narrator"] + CHARACTERS},
        "delivery": {"type": "string", "enum": ["neutral", "calm", "tense", "sad", "angry", "joyful", "amused", "whisper", "shout"]},
        "intensity": {"type": "number"}, "tagged_text": {"type": "string"}}}}}}
open("pl_system.txt", "w").write(SYSTEM)
json.dump(SCHEMA, open("pl_schema.json", "w"), ensure_ascii=False)
open("pl_pieces.txt", "w").write("\n".join("[%d] %s: %s" % (p["id"], p["kind"], p["text"]) for p in pieces))
t0 = time.time()
out, err = llm("--system-file", "pl_system.txt", "--user-file", "pl_pieces.txt", "--json-schema", "pl_schema.json", "--max-tokens", "20000",
               "--timeout-minutes", "20", "--extra", NO_THINKING)
labels = {l["id"]: l for l in json.loads(out)["labels"]}
log("labelling %.0f s" % (time.time() - t0))
segments, tags, fixed = [], 0, 0
for p in pieces:
    l = labels.get(p["id"]) or {"delivery": "neutral", "intensity": 0.5, "tagged_text": "", "speaker": "Narrator"}
    text = p["text"]
    if p["kind"] == "D" and l["tagged_text"].strip():
        if norm(l["tagged_text"]) == norm(p["text"]):
            text = l["tagged_text"].strip()
            tags += len(re.findall(r"\[%s\]" % TAGS, text))
        else:
            fixed += 1
    segments.append({"speaker": "Narrator" if p["kind"] == "N" else l["speaker"], "text": text, "delivery": l["delivery"], "intensity": l["intensity"]})
assert norm(" ".join(s["text"] for s in segments)) == norm(story), "the text must be intact by construction"
json.dump(segments, open("pl_segments.json", "w"), ensure_ascii=False, indent=1)
log("%d segments, %d tags, %d pieces fixed" % (len(segments), tags, fixed))

# 3. one swap: the LLM out, the TTS in, every piece, the TTS out
log("unload the LLM:", unload_llm())
start_tts()
files = []
try:
    for i, s in enumerate(segments):
        voice = CAST.get(s["speaker"], NARRATOR)
        data = post("/tts", tts_body(s["text"], voice, s["intensity"]), raw=True)
        fn = "pl_%02d.mp3" % i
        open(fn, "wb").write(data)
        files.append(fn)
finally:
    log("stop the TTS:", stop_tts())
# a flat pause; assemble.py has the pauses by position and the loudness
log("book.mp3: %.1f s" % concat(files, [0.25] * (len(files) - 1) + [0], "book.mp3", "48k"))
