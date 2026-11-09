#!/usr/bin/env python3
"""A conlang read by the multilingual model, to audition which language reads it best: the sentences raw (as English and German) and respelled by the
LLM for Finnish, Italian and Spanish (the model has no phoneme input, so the sounds are written in the spelling of a regular language).

  python3 conlang.py "Aethel-Vael, the whispering tongue: soft consonants, fluid vowels, Latinate-elvish, ae like a long open ai" ref_luna.wav "Ae aelis moris viri." "Sae silva moris aeth."
"""
import sys
from common import *

description, ref, *sentences = sys.argv[1:]
system = ("You help a speech engine read an invented language: %s. It has no written description of its sounds; infer them from the spelling and the description.\n"
          "For every sentence give a RESPELLING for each of three languages: Finnish (fi), Italian (it) and Spanish (es), written in the plain spelling of that language "
          "so that a reader of that language reading it aloud would produce the sounds of the sentence as closely as that language allows. Keep the word boundaries "
          "(no hyphens inside words), keep the sentence punctuation, add nothing else. Answer with JSON only." % description)
schema = {"type": "object", "additionalProperties": False, "required": ["items"], "properties": {"items": {"type": "array", "items": {
    "type": "object", "additionalProperties": False, "required": ["text", "fi", "it", "es"], "properties": {k: {"type": "string"} for k in ("text", "fi", "it", "es")}}}}}
open("cl_system.txt", "w").write(system)
json.dump(schema, open("cl_schema.json", "w"))
open("cl_in.txt", "w").write("\n".join(sentences))
out, err = llm("--system-file", "cl_system.txt", "--user-file", "cl_in.txt", "--json-schema", "cl_schema.json", "--max-tokens", "8000", "--timeout-minutes", "15", "--extra", NO_THINKING)
items = json.loads(out)["items"]
for it in items:
    log("  %-34s fi: %-30s it: %-30s es: %s" % (it["text"], it["fi"], it["it"], it["es"]))
conditions = {"en_raw": ("en", "text"), "de_raw": ("de", "text"), "fi_respelled": ("fi", "fi"), "it_respelled": ("it", "it"), "es_respelled": ("es", "es")}
log("unload the LLM:", unload_llm())
start_tts()
try:
    switch_model("chatterbox-multilingual")
    for cname, (lang, key) in conditions.items():
        files = []
        for i, it in enumerate(items):
            fn = "cl_%s_%d.mp3" % (cname, i)
            open(fn, "wb").write(post("/tts", tts_body(it[key], ("clone", ref), 0.5, model="chatterbox-multilingual", language=lang), raw=True))
            files.append(fn)
        log("cl_all_%s.mp3: %.1f s" % (cname, concat(files, [0.7] * (len(files) - 1) + [0], "cl_all_%s.mp3" % cname, "48k")))
finally:
    try:
        switch_model("chatterbox-turbo")
    except Exception as e:
        log("RESTORE FAILED:", e)
    log("stop the TTS:", stop_tts())
