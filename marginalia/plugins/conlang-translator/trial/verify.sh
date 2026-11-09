#!/bin/bash
# Second pass (see ../TODO.md): a checker request reads translations from out/out_<name>.json against the language and the scene.
# Usage: trial/verify.sh [--think] name:scene ...    e.g.  trial/verify.sh nts1_1:s1 s2_3:s2     (names as in trial/out/out_<name>.json)
# --think leaves the thinking of the model on (the default here is off, 30 seconds a request instead of 4 minutes)
cd "$(dirname "$0")"
THINK=0; [ "$1" = "--think" ] && { THINK=1; shift; }
cat verify_task.txt ../vrakht.md > out/vsystem.txt
python3 -I mkverify.py "$@"
TOOL=../../../../tools/LocalLlm.java
for spec in "$@"; do
  n=${spec%%:*}
  if [ $THINK = 1 ]; then X=(); P=th; else X=(--extra '"chat_template_kwargs":{"enable_thinking":false}'); P=nt; fi
  java "$TOOL" --max-tokens 24000 "${X[@]}" --system-file out/vsystem.txt --user-file out/v_$n.txt > out/vout_${P}_$n.json 2> out/verr_${P}_$n.txt
done
python3 -I vscore.py
