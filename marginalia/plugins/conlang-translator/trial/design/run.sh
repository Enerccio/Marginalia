#!/bin/bash
# (first make the requests with DesignGen.java, see its header)
# runs every request of the designer once on the local model (one at a time): trial/design/<name>.out.json
cd "$(dirname "$0")"
for n in words section rewrite review ask stage-sounds stage-grammar samples; do
  java ../../../../../tools/LocalLlm.java --max-tokens 30000 --system-file $n.system.txt --user-file $n.user.txt > $n.out.json 2> $n.err.txt
done
echo done > DONE
