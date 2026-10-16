#!/usr/bin/env python3
"""Prints the image table of SCREENSHOTS.md: which flow takes which picture, on which state, and where the manual uses it."""
import glob, os, re, subprocess
root = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
repo = os.path.join(root, '..')
# the state a flow runs on: a chain flow starts from the step before it (bin/chain.txt), the shooting flows as bin/shoot-all.sh says
state = {
  'shoot-admin': '13-db-backup', 'shoot-workspace': '13-db-backup', 'shoot-book': '13-db-backup', 'shoot-book-extra': '13-db-backup', 'shoot-lorebook-tab': '13-db-backup',
  'shoot-phone': '13-db-backup', 'shoot-extra': '13-db-backup (copy, discarded)',
  'shoot-plugin-characters': '21-chaptermarker', 'shoot-export': '21-chaptermarker', 'plugin-instructionexporter': '21-chaptermarker',
  'plugin-chinese': '21-chaptermarker',
  'plugin-googlegenai': '13-db-backup (copy, discarded)', 'shoot-tabs': '22-arcoutline (copy, discarded)',
}
prev = '00-empty'
for line in open(os.path.join(root, 'bin', 'chain.txt'), encoding='utf-8'):
    if not line.strip() or line.startswith('#'): continue
    flow, step = line.split()
    state[flow[:-4]] = 'step ' + prev
    prev = step
rows = {}
for flow in sorted(glob.glob(os.path.join(root, 'flows', '*.mjs'))):
    base = os.path.basename(flow)[:-4]
    if base.startswith('_'): continue
    src = open(flow, encoding='utf-8').read()
    for m in re.finditer(r"snap(?:Dialog|Popover)?\(\s*(?:page|hi\.page)\s*,\s*'([^']+)'", src):
        n = m.group(1)
        if not n.startswith('_'): rows[n] = base
    for n in re.findall(r"'(export-result)\.png'|path\.join\(outDir, '(export-result)\.png'\)", src):
        rows['export-result'] = base
rows.setdefault('export-result', 'shoot-export')
rows['dev-ui-map-workspace'] = 'annotate_ui_maps.py (from the not-annotated one)'
rows['dev-ui-map-book'] = 'annotate_ui_maps.py (from the not-annotated one)'
def used(name):
    out = subprocess.run(['grep', '-rl', 'images/%s.png' % name, os.path.join(repo, 'manual'), os.path.join(repo, 'marginalia/plugins'), '--include=*.md'],
                         capture_output=True, text=True).stdout.split()
    return ', '.join(sorted(os.path.relpath(p, repo) for p in out)) or '-'
print('| Picture | Flow | State | Used in |\n|---|---|---|---|')
for n in sorted(rows):
    flow = rows[n]
    print('| `%s.png` | `%s` | %s | %s |' % (n, flow, state.get(flow, ''), used(n)))
