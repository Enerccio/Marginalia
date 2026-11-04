import json, re, sys, glob, os
os.chdir(os.path.dirname(os.path.abspath(__file__)))
V = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'vrakht.md')
lines = open(V, encoding='utf-8').read().splitlines()
lex = set()
for i, l in enumerate(lines, 1):
    m = re.match(r'^\s*([A-Za-z][A-Za-z\-]*?)-?\s+-\s+(.+)$', l) or re.match(r'^\s*([A-Za-z][A-Za-z\-]*?)-\s+(.+)$', l)
    if m and i < 568:
        for p in m.group(1).lower().split('-'):
            if p: lex.add(p)
names = {'garrok', 'brann', 'mara', 'torv', 'ilsa'}
scenes = {'s1': 5, 's2': 6, 's3': 5, 's4': 0, 's1schema': 5}
scenes.update({})
expect = {'s2': ['vrakht', 'none', 'none', 'none', 'none', 'vrakht']}  # the last quotation may be split into two entries (both vrakht)
parts = {k: open(k.replace('schema', '') + '.txt').read() for k in ['s1', 's2', 's3', 's4']}
def extract(raw):
    raw = raw.strip()
    raw = re.sub(r'^```(?:json)?\s*|\s*```$', '', raw)
    a, b = raw.find('{'), raw.rfind('}')
    return json.loads(raw[a:b + 1])
for f in sorted(glob.glob('out/out_*.json')):
    name = os.path.basename(f)[4:-5]; sc = re.sub(r'^nt(?=s\d)', '', name.split('_')[0])
    err = open('out/err_' + name + '.txt').read().strip().splitlines()
    meta = err[-1] if err else ''
    print(f"\n=== {name}  {meta}")
    try:
        d = extract(open(f).read())
    except Exception as e:
        print("  NOT VALID JSON:", e); continue
    tr = d.get('translations', []); nw = d.get('newWords', [])
    part = parts[sc.replace('schema', '')]
    print(f"  speeches: {len(tr)} (expected {scenes[sc]}), new words: {len(nw)}")
    newparts = set()
    for w in nw:
        for p in w['word'].lower().split('-'): newparts.add(p)
    langs = []
    for t in tr:
        q = t['quote']; langs.append(t['language'].lower())
        inpart = q in part
        txt = t['text']; toks = re.findall(r"[A-Za-z']+(?:-[A-Za-z']+)*", txt)
        first = toks[0].lower() if toks else ''
        unknown = []
        for tok in toks:
            for p in tok.lower().split('-'):
                if p in names or p in lex or p in newparts: continue
                unknown.append(p)
        flags = []
        if t['language'].lower() != 'none':
            if first not in ('da', 'frag', 'bef') : flags.append(f"first token '{toks[0] if toks else ''}'")
            if re.search(r'\b(ge|mit|lok|von)\s+za\b', txt.lower()): flags.append("za after role particle")
            if unknown: flags.append("UNREPORTED words: " + ",".join(sorted(set(unknown))))
        else:
            if txt: flags.append("text given for 'none'")
        print(f"  [{t['language']}] verbatim={inpart} {t['speaker']}: {txt!r}  {'; '.join(flags)}")
    for w in nw:
        print(f"  new: {w['word']} = {w['gloss']} [{w['category']}] why: {w['rationale'][:110]}")
    if sc in expect:
        ok = langs == expect[sc] or (langs[:5] == expect[sc][:5] and len(langs) == 7 and langs[5:] == ['vrakht', 'vrakht'])
        print("  languages as expected:", ok, langs)
