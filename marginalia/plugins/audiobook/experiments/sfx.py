#!/usr/bin/env python3
"""Sound effects through ComfyUI with Stable Audio 3 Small-SFX, the graph of the official blueprint (checked by hand: this one has no
ConditioningStableAudio, an LCM sampler with 8 steps and cfg 1.0; the graph of Stable Audio Open gives noise with this checkpoint).

  python3 sfx.py "heavy boots climbing stone stairs, echoing in a vault" boots 10 42     # prompt, file name, seconds, seed

Needs the checkpoint stable_audio_3_small_sfx.safetensors in models/checkpoints and the text encoder t5gemma_b_b_ul2.safetensors in models/text_encoders.
ComfyUI caches a job by its graph: the same prompt and seed returns at once. The prompt describes ONE dry sound; a repeating sound is a one-shot placed several times.
"""
import sys, urllib.parse, uuid
from common import *

CKPT, CLIP = "stable_audio_3_small_sfx.safetensors", "t5gemma_b_b_ul2.safetensors"


def graph(prompt, seconds, seed, steps=8, cfg=1.0, negative=""):
    return {
        "1": {"class_type": "CheckpointLoaderSimple", "inputs": {"ckpt_name": CKPT}},
        "2": {"class_type": "CLIPLoader", "inputs": {"clip_name": CLIP, "type": "stable_audio", "device": "default"}},
        "3": {"class_type": "CLIPTextEncode", "inputs": {"text": prompt, "clip": ["2", 0]}},
        "4": {"class_type": "CLIPTextEncode", "inputs": {"text": negative, "clip": ["2", 0]}},
        "5": {"class_type": "EmptyLatentAudio", "inputs": {"seconds": seconds, "batch_size": 1}},
        "7": {"class_type": "KSampler", "inputs": {"model": ["1", 0], "positive": ["3", 0], "negative": ["4", 0], "latent_image": ["5", 0], "seed": seed,
              "steps": steps, "cfg": cfg, "sampler_name": "lcm", "scheduler": "simple", "denoise": 1.0}},
        "8": {"class_type": "VAEDecodeAudio", "inputs": {"samples": ["7", 0], "vae": ["1", 2]}},
        "9": {"class_type": "SaveAudio", "inputs": {"audio": ["8", 0], "filename_prefix": "audio/sfx"}},
    }


def render(prompt, name, seconds=10.0, seed=42, **kw):
    pid = post("/prompt", {"prompt": graph(prompt, seconds, seed, **kw), "client_id": str(uuid.uuid4())}, 60, base=COMFY)["prompt_id"]
    t0 = time.time()
    while time.time() - t0 < 300:
        h = get("/history/" + pid, base=COMFY)
        if pid in h:
            for node, outs in h[pid]["outputs"].items():
                for a in outs.get("audio", []):          # the key is "audio", not "images"
                    q = urllib.parse.urlencode({"filename": a["filename"], "subfolder": a.get("subfolder", ""), "type": a.get("type", "output")})
                    fn = name + ".flac"
                    open(fn, "wb").write(urllib.request.urlopen(COMFY + "/view?" + q, timeout=60).read())
                    return fn
            raise RuntimeError("no audio in the outputs: %s" % json.dumps(h[pid].get("status"))[:300])
        time.sleep(0.3)
    raise RuntimeError("timed out")


if __name__ == "__main__":
    prompt, name = sys.argv[1:3]
    seconds = float(sys.argv[3]) if len(sys.argv) > 3 else 10.0
    seed = int(sys.argv[4]) if len(sys.argv) > 4 else 42
    t0 = time.time()
    fn = render(prompt, name, seconds, seed)
    log("%s: %.1f s of audio in %.1f s" % (fn, duration(fn), time.time() - t0))
