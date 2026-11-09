#!/usr/bin/env python3
"""Settings sweep of one Chatterbox model on one text and one voice (what the notes in TODO.md compare): exaggeration, cfg_weight, temperature, seed.

  python3 model_sweep.py chatterbox text.txt ref_test.wav       # the models: chatterbox-turbo, chatterbox, chatterbox-multilingual
Restores the Turbo model and stops the TTS at the end.
"""
import sys
from common import *

model, text_file, ref = sys.argv[1:4]
text = open(text_file).read().strip()
GRID = [("ex03_cfg05", 0.3, 0.5, 0.8, 42), ("ex07_cfg05", 0.7, 0.5, 0.8, 42), ("ex13_cfg05", 1.3, 0.5, 0.8, 42), ("ex07_cfg02", 0.7, 0.2, 0.8, 42),
        ("ex13_cfg02", 1.3, 0.2, 0.8, 42), ("ex07_cfg05_t05", 0.7, 0.5, 0.5, 42), ("ex07_cfg05_t10", 0.7, 0.5, 1.0, 42), ("ex05_cfg05_seed7", 0.5, 0.5, 0.8, 7)]
log("unload the LLM:", unload_llm())
start_tts()
try:
    switch_model(model)
    for name, ex, cfg, temp, seed in GRID:
        body = {"text": text, "voice_mode": "clone", "reference_audio_filename": ref, "output_format": "mp3", "split_text": True, "chunk_size": 240,
                "exaggeration": ex, "cfg_weight": cfg, "temperature": temp, "seed": seed, "speed_factor": 1.0}
        t0 = time.time()
        fn = "sw_%s_%s.mp3" % (model, name)
        open(fn, "wb").write(post("/tts", body, raw=True))
        log("%-34s %5.1f s of audio in %4.1f s" % (fn, duration(fn), time.time() - t0))
finally:
    try:
        switch_model("chatterbox-turbo")
    except Exception as e:
        log("RESTORE FAILED:", e)
    log("stop the TTS:", stop_tts())
