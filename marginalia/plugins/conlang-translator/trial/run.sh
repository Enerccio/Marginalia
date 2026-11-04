#!/bin/bash
# Real-model trial of the translation request (see ../TODO.md, "Real-model trial"). One request at a time: the server has one slot.
# Usage: trial/run.sh [options of tools/LocalLlm.java, e.g. --extra '"chat_template_kwargs":{"enable_thinking":false}'] ; results in trial/out/
# SAMPLES="s1:3 s2:3 s3:2 s4:1" changes what is run, TAG=nt_ adds a prefix to the file names, TASK=task_ae.txt LANGFILE=../aethel-vael.md picks the prompt and the
# language, MODEL="openai/unsloth|gemma-4-31b-it-MM" the model (default: the first AI of the database; see tools/LocalLlm.java --models).
cd "$(dirname "$0")"
mkdir -p out
cat "${TASK:-task.txt}" "${LANGFILE:-../vrakht.md}" > out/system.txt
TOOL=../../../../tools/LocalLlm.java
for spec in ${SAMPLES:-s1:3 s2:3 s3:2 s4:1}; do
  scene=${spec%%:*}; n=${spec##*:}
  for i in $(seq 1 "$n"); do
    java "$TOOL" ${MODEL:+--model "$MODEL"} --max-tokens 24000 --system-file out/system.txt --user-file "$scene.txt" "$@" \
      > "out/out_${TAG}${scene}_$i.json" 2> "out/err_${TAG}${scene}_$i.txt"
  done
done
echo done > out/DONE
