# Experiments

The scripts the notes in [`../TODO.md`](../TODO.md) come from (run by hand against the author's server on 2026-11-08, cleaned up afterwards: the
addresses come from the environment, see `common.py`). They are **not** part of the plugin and are not built; the plugin does the same calls from Java.
Python 3 standard library, `ffmpeg`/`ffprobe`, and `tools/LocalLlm.java` (the language model and the control routes of LiteLLM with its one key).

| Script | What it does |
|---|---|
| `common.py` | helpers: the calls, the model switch of the TTS server, the body of `/tts`, the join with pauses |
| `pipeline.py` | text -> pieces (code) -> labels (LLM) -> one swap -> one `/tts` call per piece -> joined audio and the script (`pl_segments.json`) |
| `assemble.py` | loudness of every piece to one level, pauses by position, the script file |
| `model_sweep.py` | `exaggeration` / `cfg_weight` / `temperature` / seed on one text, for one model |
| `conlang.py` | a conlang read by the multilingual model: raw and respelled by the LLM for Finnish, Italian, Spanish |
| `sfx.py` | a sound effect with Stable Audio 3 Small-SFX in ComfyUI (the official graph) |

Not kept: the scripts that only compared the first variants (they are the same calls with other values) and the diagnostics of the wrong Stable Audio graph.
