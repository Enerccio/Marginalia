# Scores the Aethel-Vael trial (out/out_ae_*.json): valid JSON, verbatim quotes, who speaks, and an AFFIX-AWARE lexicon check:
# a token is known when it is a lexicon word, or a word with the tense prefixes el- va- ki-, the plural -is (after a vowel drop) or the genitive -en,
# or a concatenation of such words (the language builds compounds as one word). Usage: python3 -I trial/score_ae.py
import json, re, glob, os, sys
os.chdir(os.path.dirname(os.path.abspath(__file__)))
lines = open('../aethel-vael.md', encoding='utf-8').read().splitlines()
lex = set()
for l in lines:
    cells = [c.strip() for c in l.strip().strip('|').split('|')]
    if l.strip().startswith('|'):
        for c in cells[1:]:
            w = re.sub(r'[\*\s]', '', c)
            if re.fullmatch(r'[A-Za-z\-]+', w) and len(w) > 1: lex.add(w.lower())
    for m in re.finditer(r"\*\*(\w+)\*\*\s+[–-]", l): lex.add(m.group(1).lower())
    for m in re.finditer(r"^\*\s+'(\w+)'\s+—", l.strip()): lex.add(m.group(1).lower())
lex |= {'ae','lu','vo','aen','von','moris','sae','sael','lia','saelia','vaela','aelva'}
lex = {w for w in lex if w not in {'the','lit','core','vocabulary'}}
V = 'aeiou'
LEX = lex  # the lexicon, plus the words a translation reports as new (set per answer): a reported word can take affixes too
def known(t, depth=0):
    t = t.lower()
    if t in LEX: return True
    if depth > 3 or len(t) < 2: return False
    for p in ('el', 'va', 'ki'):
        if t.startswith(p) and known(t[2:], depth + 1): return True
    if t.endswith('is'):
        stem = t[:-2]
        if known(stem, depth + 1) or any(known(stem + v, depth + 1) for v in V): return True
    if t.endswith('en') and known(t[:-2], depth + 1): return True
    for i in range(2, len(t) - 1):
        if t[:i] in LEX and known(t[i:], depth + 1): return True
    return False
scene_text = {s: open(s + '.txt').read() for s in ['a1', 'a2', 'a3', 'a4']}
quotes = {s: re.findall(r'"([^"]+)"', scene_text[s].split('PART', 1)[1]) for s in scene_text}
def extract(raw):
    raw = re.sub(r'^```(?:json)?\s*|\s*```$', '', raw.strip())
    return json.loads(raw[raw.find('{'):raw.rfind('}') + 1])
for f in sorted(glob.glob('out/out_ae_*.json')):
    name = os.path.basename(f)[4:-5]; sc = name.split('_')[1]
    err = open('out/err_' + name + '.txt').read().strip().splitlines()
    print(f"\n=== {name}  {err[-1] if err else ''}")
    try: d = extract(open(f).read())
    except Exception as e: print('  NOT VALID JSON', e); continue
    tr, nw = d.get('translations', []), d.get('newWords', [])
    print(f"  speeches: {len(tr)} (the part has {len(quotes[sc])} quotations), new words: {len(nw)}")
    reported = set()
    for w in nw:
        for p in re.split(r'[\s\-]+', w['word'].lower()): reported.add(p)
    LEX = lex | reported
    for t in tr:
        q = t['quote']; inpart = any(q in x for x in quotes[sc]) or q in scene_text[sc]
        toks = re.findall(r"[A-Za-z']+", t['text'])
        unknown = sorted({k.lower() for k in toks if not known(k) and k.lower() not in reported and k.lower() not in {'lira','thorn','brom','selene','varek','mira','dorn'}})
        print(f"  [{t['language']}] verbatim={inpart} {t['speaker']}: {t['text']!r}" + (f"   UNKNOWN: {','.join(unknown)}" if unknown and t['language'].lower() != 'none' else ''))
    for w in nw:
        print(f"  new: {w['word']} = {w['gloss']} [{w.get('category')}/{w.get('register')}] {w['rationale'][:100]}")
