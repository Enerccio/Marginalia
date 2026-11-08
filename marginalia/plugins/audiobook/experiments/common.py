"""Shared helpers of the experiments. Settings come from the environment, nothing is hard-coded:

  TTS_URL     the Chatterbox-TTS-Server, for example http://host:8004 (the model switch and /tts go to it directly, a plugin would go through LiteLLM)
  COMFY_URL   ComfyUI, for example http://host:8188
  LOCAL_LLM   path of tools/LocalLlm.java (default: ../../../../tools/LocalLlm.java); it reads the AI of the dev database, so the LLM,
              the unload header and the pass-through routes (/gpu/tts/load, /gpu/tts/unload) all use the one LiteLLM key
Needs python3, ffmpeg and ffprobe on the path. Files are written to the current directory.
"""
import json, os, subprocess, sys, time, urllib.request

TTS = os.environ.get("TTS_URL", "http://localhost:8004")
COMFY = os.environ.get("COMFY_URL", "http://localhost:8188")
LOCAL_LLM = os.environ.get("LOCAL_LLM", os.path.join(os.path.dirname(os.path.abspath(__file__)), "../../../../tools/LocalLlm.java"))
NO_THINKING = '"chat_template_kwargs":{"enable_thinking":false}'

# the sound tags of Chatterbox Turbo
TAGS = r"(laugh|chuckle|sigh|gasp|cough|clear throat|sniff|groan)"


def log(*a, file=None):
    line = "[%s] %s" % (time.strftime("%H:%M:%S"), " ".join(str(x) for x in a))
    print(line, flush=True)
    if file:
        file.write(line + "\n")
        file.flush()


def llm(*args, timeout=1800):
    """One call of tools/LocalLlm.java; returns (stdout, stderr)."""
    p = subprocess.run(["java", LOCAL_LLM] + list(args), capture_output=True, text=True, timeout=timeout)
    return p.stdout, p.stderr


def unload_llm():
    return llm("--header", "X-Llama-Unload: all", "--user", "unload", "--max-tokens", "50")[0].strip()


def start_tts():
    """Starts the Chatterbox container through the pass-through of LiteLLM and waits until it is healthy."""
    return llm("--request", "POST", "/gpu/tts/load", "--timeout-minutes", "15")


def stop_tts():
    return llm("--request", "POST", "/gpu/tts/unload")[0].strip()


def post(path, body, timeout=900, raw=False, base=None):
    req = urllib.request.Request((base or TTS) + path, data=json.dumps(body, ensure_ascii=False).encode(),
                                 headers={"Content-Type": "application/json"}, method="POST")
    with urllib.request.urlopen(req, timeout=timeout) as r:
        data = r.read()
        return data if raw else json.loads(data or b"{}")


def get(path, timeout=30, base=None):
    return json.load(urllib.request.urlopen((base or TTS) + path, timeout=timeout))


def switch_model(repo, log_fn=log):
    """repo: chatterbox-turbo | chatterbox | chatterbox-multilingual. The server loads the model while /restart_server runs."""
    post("/save_settings", {"model": {"repo_id": repo}}, timeout=60)
    t0 = time.time()
    r = post("/restart_server", {}, timeout=1500)
    log_fn("model ->", repo, "%.0f s" % (time.time() - t0), r.get("message"))


def duration(path):
    out = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=nw=1:nk=1", path],
                         capture_output=True, text=True).stdout.strip()
    return float(out)


def tts_body(text, voice, intensity=0.5, seed=42, model=None, language=None, cfg_weight=0.5, temperature=0.8):
    """The body of /tts. voice: ("clone", "file.wav") or ("predefined", "Henry.wav"). The exaggeration follows the intensity of the label."""
    mode, name = voice
    body = {"text": text, "output_format": "mp3", "split_text": True, "chunk_size": 240, "temperature": temperature, "seed": seed,
            "exaggeration": round(0.4 + 0.5 * float(intensity), 2), "cfg_weight": cfg_weight, "speed_factor": 1.0, "voice_mode": mode}
    body["reference_audio_filename" if mode == "clone" else "predefined_voice_id"] = name
    if model == "chatterbox-multilingual" and language:
        body["language"] = language
    return body


def silence(path, seconds):
    subprocess.run(["ffmpeg", "-loglevel", "error", "-y", "-f", "lavfi", "-i", "anullsrc=r=24000:cl=mono", "-t", str(seconds), path], check=True)


def concat(files, pauses, out, bitrate="64k", metadata=None):
    """Joins the files in order with a silence of pauses[i] seconds after file i (None / 0: no pause)."""
    lst = []
    for f, p in zip(files, pauses):
        lst.append("file '%s'" % os.path.abspath(f))
        if p:
            sil = "sil_%03d.wav" % int(p * 1000)
            if not os.path.exists(sil):
                silence(sil, p)
            lst.append("file '%s'" % os.path.abspath(sil))
    open(out + ".list", "w").write("\n".join(lst) + "\n")
    cmd = ["ffmpeg", "-loglevel", "error", "-y", "-f", "concat", "-safe", "0", "-i", out + ".list", "-c:a", "libmp3lame", "-b:a", bitrate]
    for k, v in (metadata or {}).items():
        cmd += ["-metadata", "%s=%s" % (k, v)]
    subprocess.run(cmd + [out], check=True)
    return duration(out)
