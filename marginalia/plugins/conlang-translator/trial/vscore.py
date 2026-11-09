import json, re, glob, os
os.chdir(os.path.dirname(os.path.abspath(__file__)))
def extract(raw):
    raw = re.sub(r'^```(?:json)?\s*|\s*```$', '', raw.strip())
    return json.loads(raw[raw.find('{'):raw.rfind('}') + 1])
for f in sorted(glob.glob('out/vout_*.json')):
    print('\n=====', f)
    try: d = extract(open(f).read())
    except Exception as e: print('  INVALID', e); continue
    for c in d['checks']:
        flag = c['meaning'] != 'same' or c['ruleViolations'] or c['problems']
        print(f"  [{c['meaning']}] {'FLAGGED' if flag else 'ok     '} {c['quote'][:45]!r}")
        if flag:
            print("     back:", c['backTranslation'][:140])
            for r in c['ruleViolations']: print("     rule:", r[:150])
            for r in c['problems']: print("     prob:", r[:150])
            if c['fix']: print("     fix :", c['fix'][:120])
