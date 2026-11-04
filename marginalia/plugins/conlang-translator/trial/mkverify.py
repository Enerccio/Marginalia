import json, re, sys, os
os.chdir(os.path.dirname(os.path.abspath(__file__)))
# usage: mkverify.py name:scene ...   (e.g. nts1_1:s1 s2_3:s2): builds out/v_<name>.txt from out/out_<name>.json and the scene
def extract(raw):
    raw = re.sub(r'^```(?:json)?\s*|\s*```$', '', raw.strip())
    return json.loads(raw[raw.find('{'):raw.rfind('}') + 1])
for out, scene in [a.split(':') for a in sys.argv[1:]]:
    d = extract(open(f'out/out_{out}.json').read())
    tr = [{'quote': t['quote'], 'speaker': t['speaker'], 'language': t['language'], 'text': t['text']} for t in d['translations']]
    nw = [{'word': w['word'], 'gloss': w['gloss']} for w in d['newWords']]
    sc = open(scene + '.txt').read()
    msg = sc + "\n\nTRANSLATIONS\n" + json.dumps(tr, ensure_ascii=False, indent=1) + "\n\nREPORTED NEW WORDS\n" + json.dumps(nw, ensure_ascii=False, indent=1) + "\n"
    open(f'out/v_{out}.txt', 'w').write(msg)
print('ok')
