#!/usr/bin/env python3
"""Translates the user manual (manual/user + manual/index.md) into other languages with Claude Code, incrementally.

Python 3 standard library only. The model is called through the `claude` CLI (`claude -p`, no tools, no session), one request per
chunk of a page, so it runs on a Claude Code login (or an API key) with nothing else to install.

  python3 tools/manual_i18n.py status [--lang de]            which pages are missing / outdated / current (all languages without --lang)
  python3 tools/manual_i18n.py translate --lang de           translate the pages that are missing or whose English source changed
  python3 tools/manual_i18n.py translate --lang de --dry-run only list what would be sent (pages, chunks, words)
  python3 tools/manual_i18n.py translate --lang de --only user/books/prompts --limit 5 --jobs 2 --model haiku
  python3 tools/manual_i18n.py usage --lang de              tokens and cost per translated page (build/i18n-usage/<lang>.jsonl, not tracked)
  python3 tools/manual_i18n.py assemble --lang de --out build/i18n/de/manual   English tree + the translated pages over it
  python3 tools/manual_i18n.py override --lang de            the JSON for `retype build --override` of that language
  python3 tools/manual_i18n.py languages                     the languages that have translated pages, one per line
  python3 tools/manual_i18n.py glossary --lang de [page.md]  the UI labels the translator is told to use (from LocalizationBundle*)

Where things live
  translations/<lang>/<path of the page below manual/>.md   the translated pages (user/books/prompts.md, index.md, ...)
  translations/<lang>/manifest.json                         per page: sha256 of the English source it was made from, model, time
A page is *current* when the hash of today's English file equals the one in the manifest, *outdated* when it differs, *missing* when
there is no file. `translate` only touches missing and outdated pages (`--force` all), so a release that changed 6 pages costs 6 pages.
Images, files and the developer manual are not duplicated: `assemble` copies manual/ and puts the translated pages over it
(a page without a translation stays English, the images are the English screenshots).

How a page is translated
  * split at `## ` headings (outside code fences) into chunks of up to --chunk-words words (the biggest pages have 14k words);
  * the system prompt carries the rules (structure, what is never translated) and a glossary: every UI label of the app
    (`L` keys of LocalizationBundleEN) that the chunk shows in bold/italic/code, with its text in the bundle of that language,
    so the manual says what the screen says;
  * every chunk is checked against its source (same code blocks, same headings, same link targets, same front matter keys);
    a chunk that fails is asked again with the problems named (--retries), a page with a failed chunk is not written;
  * headings get their old English anchor as `<a id="..."></a>` before them when the translation changes the heading's id, so
    the links of other pages (`page.md#english-heading`) keep working.
Chinese has no bundle in the application (it is the plugin `chineselocalization`), so it is translated without a glossary.
"""
import argparse
import concurrent.futures
import datetime
import fnmatch
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import threading

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
MANUAL = os.path.join(ROOT, 'manual')
TRANSLATIONS = os.path.join(ROOT, 'translations')
LOC_DIR = os.path.join(ROOT, 'marginalia', 'src', 'main', 'java', 'com', 'github', 'enerccio', 'marginalia', 'loc')

# code -> (English name, native name, bundle class suffix or None)
LANGUAGES = {
    'ja': ('Japanese', '日本語', 'JP'),
    'fr': ('French', 'Français', 'FR'),
    'es': ('Spanish', 'Español', 'ES'),
    'de': ('German', 'Deutsch', 'DE'),
    'ko': ('Korean', '한국어', 'KO'),
    'it': ('Italian', 'Italiano', 'IT'),
    'zh': ('Simplified Chinese', '简体中文', None),
}
CJK = {'ja', 'ko', 'zh'}

# what is translated: manual/index.md and everything under manual/user (the developer manual stays English)
def source_pages():
    pages = []
    if os.path.isfile(os.path.join(MANUAL, 'index.md')):
        pages.append('index.md')
    for base, _dirs, files in os.walk(os.path.join(MANUAL, 'user')):
        for f in files:
            if f.endswith('.md'):
                pages.append(os.path.relpath(os.path.join(base, f), MANUAL))
    return sorted(pages)


def read(path):
    with open(path, encoding='utf-8') as f:
        return f.read()


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(text)


def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()


# ------------------------------------------------------------------------------------------------ manifest

def manifest_path(lang):
    return os.path.join(TRANSLATIONS, lang, 'manifest.json')


def load_manifest(lang):
    p = manifest_path(lang)
    if os.path.isfile(p):
        return json.loads(read(p))
    return {'language': lang, 'pages': {}}


def save_manifest(lang, m):
    write(manifest_path(lang), json.dumps(m, indent=2, ensure_ascii=False, sort_keys=True) + '\n')


def page_state(lang, page, manifest):
    if not os.path.isfile(os.path.join(TRANSLATIONS, lang, page)):
        return 'missing'
    entry = manifest['pages'].get(page)
    if not entry or entry.get('source_sha') != sha(read(os.path.join(MANUAL, page))):
        return 'outdated'
    return 'current'


# ------------------------------------------------------------------------------------------------ markdown helpers

FENCE = re.compile(r'^\s*(```|~~~)')


def outside_fences(lines):
    """Yields (index, line) for the lines that are not in a fenced code block (the fence lines themselves are not yielded)."""
    fence = None
    for i, line in enumerate(lines):
        m = FENCE.match(line)
        if m:
            if fence is None:
                fence = m.group(1)
            elif m.group(1) == fence:
                fence = None
            continue
        if fence is None:
            yield i, line


def words(text):
    return len(text.split())


def split_front_matter(text):
    if text.startswith('---\n'):
        end = text.find('\n---\n', 4)
        if end != -1:
            return text[:end + 5], text[end + 5:]
    return '', text


def chunk_page(text, max_words):
    """Splits at `## ` headings (outside fences) into chunks of whole sections; a section that is too big is split at `### `."""
    lines = text.split('\n')

    def sections(level):
        marks = [i for i, l in outside_fences(lines) if l.startswith(level + ' ')]
        return marks

    def cut(lo, hi, level):
        seg = lines[lo:hi]
        if words('\n'.join(seg)) <= max_words * 1.5 or level == '####':
            return [(lo, hi)]
        sub = [i for i, l in outside_fences(lines) if lo < i < hi and l.startswith(level + ' ')]
        if not sub:
            return [(lo, hi)]
        out, start = [], lo
        for m in sub + [hi]:
            out.append((start, m))
            start = m
        return out

    marks = sections('##')
    bounds = [0] + marks + [len(lines)]
    secs = []
    for a, b in zip(bounds, bounds[1:]):
        if a == b:
            continue
        secs.extend(cut(a, b, '###') if b - a > 0 else [])
    chunks, cur, cur_words = [], [], 0
    for a, b in secs:
        w = words('\n'.join(lines[a:b]))
        if cur and cur_words + w > max_words:
            chunks.append(cur)
            cur, cur_words = [], 0
        cur.append((a, b))
        cur_words += w
    if cur:
        chunks.append(cur)
    return ['\n'.join(l for a, b in c for l in lines[a:b]) for c in chunks]


def heading_slug(title):
    t = re.sub(r'<[^>]+>', '', title).strip().lower()
    t = re.sub(r'[`*_~]', '', t)
    t = re.sub(r'[^\w\s-]', '', t, flags=re.UNICODE)
    return re.sub(r'\s+', '-', t).strip('-')


def headings(text):
    lines = text.split('\n')
    return [(i, re.match(r'#+', l).group(0), l.lstrip('#').strip()) for i, l in outside_fences(lines) if re.match(r'#{1,6} ', l)]


def link_targets(text):
    lines = text.split('\n')
    body = '\n'.join(l for _i, l in outside_fences(lines))
    body = re.sub(r'`[^`\n]*`', '', body)
    return sorted(re.findall(r'\]\(([^)\s]+)', body))


_LINK_OR_CODE = re.compile(r'`[^`\n]*`|\]\(([^)\s]+)')


def repair_links(src, out):
    """The model likes to translate anchors (`#the-branch-view` -> `#la-vista-de-ramas`), which breaks links between pages. When the answer has
    as many link targets as the source, every target is put back by position. (The headings keep their English ids, see add_anchors.)"""
    want = link_targets_in_order(src)
    if len(want) != len(link_targets_in_order(out)):
        return out
    it = iter(want)
    lines = out.split('\n')
    keep = {i for i, _l in outside_fences(lines)}
    for i in range(len(lines)):
        if i in keep:
            lines[i] = _LINK_OR_CODE.sub(lambda m: m.group(0) if m.group(1) is None else '](' + next(it), lines[i])
    return '\n'.join(lines)


def link_targets_in_order(text):
    out = []
    lines = text.split('\n')
    for _i, l in outside_fences(lines):
        for m in _LINK_OR_CODE.finditer(l):
            if m.group(1) is not None:
                out.append(m.group(1))
    return out


def front_matter_keys(text):
    fm, _ = split_front_matter(text)
    return sorted(re.findall(r'^([A-Za-z_]+):', fm, flags=re.M))


def validate_chunk(lang, src, out):
    problems = []
    if not out.strip():
        return ['the answer is empty']
    ratio = len(out) / max(1, len(src))
    if ratio < (0.2 if lang in CJK else 0.4) or ratio > 3.5:
        problems.append('the length is off (%d characters for %d in the source)' % (len(out), len(src)))
    fences = lambda t: sum(1 for l in t.split('\n') if FENCE.match(l))
    if fences(src) != fences(out):
        problems.append('the number of code fences differs (%d in the source, %d in the answer)' % (fences(src), fences(out)))
    hs, ho = headings(src), headings(out)
    if [h[1] for h in hs] != [h[1] for h in ho]:
        problems.append('the headings differ: the source has %s, the answer %s' % ([h[1] for h in hs], [h[1] for h in ho]))
    if link_targets(src) != link_targets(out):
        a, b = link_targets(src), link_targets(out)
        problems.append('the link targets (the part in parentheses of [text](target) and of images) must stay exactly as in the source; '
                        'missing: %s; unexpected: %s' % ([x for x in a if x not in b][:6], [x for x in b if x not in a][:6]))
    if front_matter_keys(src) != front_matter_keys(out):
        problems.append('the front matter keys differ')
    if src.startswith('---\n') != out.startswith('---\n'):
        problems.append('the front matter block (--- ... ---) at the start must be kept')
    return problems


def add_anchors(source, translated):
    """Headings whose id changed with the translation get their English id back as an anchor line, so `page.md#english-id` still works."""
    hs, ht = headings(source), headings(translated)
    if len(hs) != len(ht):
        return translated
    lines = translated.split('\n')
    for (_i, lv, en), (j, _lv, tr) in reversed(list(zip(hs, ht))):
        en_id, tr_id = heading_slug(en), heading_slug(tr)
        if en_id and en_id != tr_id and len(lv) > 1:   # a page's title (#) is nobody's link target and must stay the first block
            lines[j:j] = ['<a id="%s"></a>' % en_id, '']
    return '\n'.join(lines)


# ------------------------------------------------------------------------------------------------ glossary (UI labels)

_SETVALUE = re.compile(r'setValue\(\s*L\.(\w+)\s*,\s*((?:"(?:[^"\\]|\\.)*"\s*\+?\s*)+)\)', re.S)
_STR = re.compile(r'"((?:[^"\\]|\\.)*)"')
_bundles = {}


def unescape(s):
    s = re.sub(r'\\u([0-9a-fA-F]{4})', lambda m: chr(int(m.group(1), 16)), s)
    return re.sub(r'\\(.)', lambda m: {'n': '\n', 't': '\t'}.get(m.group(1), m.group(1)), s)


def bundle(suffix):
    if suffix not in _bundles:
        p = os.path.join(LOC_DIR, 'LocalizationBundle%s.java' % suffix)
        values = {}
        if os.path.isfile(p):
            for key, body in _SETVALUE.findall(read(p)):
                values[key] = unescape(''.join(_STR.findall(body)))
        _bundles[suffix] = values
    return _bundles[suffix]


def glossary(lang, text, limit=160):
    """{English label: translation} for the labels of the application that `text` shows as **label**, *label* or `label`."""
    suffix = LANGUAGES[lang][2]
    if not suffix:
        return {}
    en, tr = bundle('EN'), bundle(suffix)
    shown = set(re.findall(r'\*\*([^*\n]{2,60})\*\*|(?<!\*)\*([^*\n]{2,60})\*(?!\*)|`([^`\n]{2,60})`', text, flags=re.S))
    shown = {x for tup in shown for x in tup if x}
    found = {}
    for key, value in en.items():
        if key not in tr or value not in shown or '{' in value or '%' in value or '\n' in value:
            continue
        if tr[key] and tr[key] != value:
            found.setdefault(value, [])
            if tr[key] not in found[value]:
                found[value].append(tr[key])
    return dict(sorted(found.items())[:limit]) if len(found) > limit else dict(sorted(found.items()))


# ------------------------------------------------------------------------------------------------ the model

SYSTEM = """You are a technical translator. You translate parts of the user manual of Marginalia, a self-hosted tool for co-writing long fiction with \
language models (a story tree, lorebooks, summaries, prompt templates, plugins), from English into {name}.

Answer with the translated Markdown only: no comment, no preface, no code fence around the answer.

Rules:
1. Keep the structure exactly: the same headings (same levels, same number), paragraphs, list items and nesting, tables (same columns and rows), block quotes, \
admonitions (`!!!`), blank lines, bold and italic marks, and the same number of fenced code blocks.
2. Never translate or change: code blocks and inline `code`, Handlebars/macro text such as `{{{{char}}}}`, file names and paths, URLs, the target of a link \
`[text](target)` and of an image `![alt](target)` (character for character, anchors included: `#branching-from-an-earlier-part` stays English even though the heading it points to is translated), HTML tags and attributes, keyboard keys (Ctrl, Enter, Esc...), \
numbers, and the names Marginalia, SillyTavern, Handlebars, Docker, Jetty, Vaadin, OpenAI, Anthropic, Claude, Gemini, LiteLLM, llama.cpp and other product names.
3. Translate the text of links and the alt text of images, the cells of tables, and the text of headings.
4. Front matter (the block between two `---` lines at the start): translate only the value of `label:` (and `title:` if there is one); keep every other line.
5. The names of buttons, menu items, tabs and fields of the application are written in bold (or italic) in the manual. When the glossary below gives the text \
of a label in {name}, use exactly that text, because the screens of the application show it. A label that is not in the glossary stays in English.
6. Write natural, clear documentation prose in {name} for readers who write fiction, not programmers; keep the tone of the source. Keep terms consistent \
inside the text. Do not add, remove or reorder content.
7. The text you get can be one part of a longer page: translate only what you are given, and do not add front matter or a title that is not there.{glossary}"""


def system_prompt(lang, chunk):
    gl = glossary(lang, chunk)
    block = ''
    if gl:
        block = '\n\nGlossary (English label -> text on the screens in %s):\n%s' % (
            LANGUAGES[lang][0], '\n'.join('%s -> %s' % (k, ' / '.join(v)) for k, v in gl.items()))
    return SYSTEM.format(name=LANGUAGES[lang][0], glossary=block)


def call_claude(system, text, model, effort, timeout):
    cmd = ['claude', '-p', '--model', model, '--system-prompt', system, '--tools', '', '--no-session-persistence',
           '--disable-slash-commands', '--output-format', 'json',
           # without these the request carries the whole Claude Code context of the user (MCP tools, skills, memory: ~50k tokens per call)
           '--strict-mcp-config', '--mcp-config', '{"mcpServers":{}}', '--setting-sources', '']
    if effort:
        cmd += ['--effort', effort]
    # a neutral working folder: no CLAUDE.md of the project in the context
    with tempfile.TemporaryDirectory(prefix='manual-i18n-') as cwd:
        r = subprocess.run(cmd, input=text, capture_output=True, text=True, timeout=timeout, cwd=cwd)
    if r.returncode != 0:
        raise RuntimeError('claude exited with %d: %s' % (r.returncode, (r.stderr or r.stdout).strip()[:500]))
    data = json.loads(r.stdout)
    if data.get('is_error'):
        raise RuntimeError('claude: %s' % str(data.get('result'))[:500])
    u = data.get('usage') or {}
    usage = {'input': u.get('input_tokens', 0), 'output': u.get('output_tokens', 0),
             'cache_write': u.get('cache_creation_input_tokens', 0), 'cache_read': u.get('cache_read_input_tokens', 0),
             'cost_usd': data.get('total_cost_usd', 0.0), 'requests': 1}
    out = data.get('result') or ''
    # a model that wraps the answer in a fence: take it off (the source of a chunk never is one block)
    m = re.match(r'^```(?:markdown|md)?\n(.*)\n```\s*$', out.strip('\n'), re.S)
    if m and not text.lstrip().startswith('```'):
        out = m.group(1)
    return (out.strip('\n') + '\n' if text.endswith('\n') else out.strip('\n')), usage


def add_usage(total, usage):
    for k, v in usage.items():
        total[k] = total.get(k, 0) + v
    return total


def translate_chunk(lang, chunk, args):
    system = system_prompt(lang, chunk)
    request = chunk
    problems = []
    usage = {}
    for attempt in range(args.retries + 1):
        out, used = call_claude(system, request, args.model, args.effort, args.timeout)
        add_usage(usage, used)     # the retries count too
        out = repair_links(chunk, out)
        problems = validate_chunk(lang, chunk, out)
        if not problems:
            return out, usage
        print('  retry (attempt %d): %s' % (attempt + 1, '; '.join(problems)[:600]), flush=True)
        request = ('%s\n\n---\nYour previous translation of the text above was rejected: %s\nTranslate the text above again, '
                   'fixing this; the answer is the translation only.' % (chunk, '; '.join(problems)))
    raise RuntimeError('chunk failed validation: ' + '; '.join(problems))


# ------------------------------------------------------------------------------------------------ commands

def selected_pages(args):
    pages = source_pages()
    if args.only:
        pages = [p for p in pages if any(p == o or p.startswith(o) or fnmatch.fnmatch(p, o) for o in args.only)]
    return pages


def cmd_status(args):
    langs = [args.lang] if args.lang else sorted(LANGUAGES)
    pages = source_pages()
    for lang in langs:
        m = load_manifest(lang)
        states = {p: page_state(lang, p, m) for p in pages}
        counts = {s: sum(1 for v in states.values() if v == s) for s in ('current', 'outdated', 'missing')}
        print('%s (%s): %d current, %d outdated, %d missing of %d pages' % (lang, LANGUAGES[lang][0], counts['current'], counts['outdated'], counts['missing'], len(pages)))
        if args.lang:
            for p in pages:
                if states[p] != 'current':
                    print('  %-9s %s (%d words)' % (states[p], p, words(read(os.path.join(MANUAL, p)))))
    return 0


USAGE_DIR = os.path.join(ROOT, 'build', 'i18n-usage')     # build/ is ignored by git: transient


def log_usage(lang, page, model, source_words, chunks, usage):
    """One JSON line per translated page in build/i18n-usage/<lang>.jsonl (not tracked; `usage` prints the sums)."""
    row = dict(usage, page=page, model=model, words=source_words, chunks=chunks,
               at=datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ'))
    os.makedirs(USAGE_DIR, exist_ok=True)
    with open(os.path.join(USAGE_DIR, lang + '.jsonl'), 'a', encoding='utf-8') as f:
        f.write(json.dumps(row, ensure_ascii=False) + '\n')


def cmd_usage(args):
    p = os.path.join(USAGE_DIR, args.lang + '.jsonl')
    if not os.path.isfile(p):
        print('no usage recorded for', args.lang)
        return 0
    rows = [json.loads(l) for l in read(p).splitlines() if l.strip()]
    keys = ('words', 'input', 'cache_write', 'cache_read', 'output', 'requests', 'cost_usd')
    print('%-46s %6s %7s %7s %7s %7s %4s %8s' % (('page',) + keys))
    tot = dict.fromkeys(keys, 0)
    for r in rows:
        print('%-46s %6d %7d %7d %7d %7d %4d %8.4f' % ((r['page'],) + tuple(r.get(k, 0) for k in keys)))
        for k in keys:
            tot[k] += r.get(k, 0)
    print('%-46s %6d %7d %7d %7d %7d %4d %8.4f' % (('TOTAL (%d pages)' % len(rows),) + tuple(tot[k] for k in keys)))
    return 0


def cmd_translate(args):
    lang = args.lang
    manifest = load_manifest(lang)
    todo = [p for p in selected_pages(args) if args.force or page_state(lang, p, manifest) != 'current']
    if args.limit:
        todo = todo[:args.limit]
    plan = {p: chunk_page(read(os.path.join(MANUAL, p)), args.chunk_words) for p in todo}
    total_words = sum(words(c) for cs in plan.values() for c in cs)
    print('%s: %d pages, %d requests, %d words to translate' % (LANGUAGES[lang][0], len(todo), sum(len(c) for c in plan.values()), total_words))
    if args.dry_run:
        for p in todo:
            print('  %s: %d chunks, %d words' % (p, len(plan[p]), sum(words(c) for c in plan[p])))
        return 0
    lock = threading.Lock()
    failures = []

    def work(item):
        page, chunk_no, chunk = item
        try:
            out, usage = translate_chunk(lang, chunk, args)
            with lock:
                print('  ok   %s %d/%d (%d in, %d out tokens)' % (page, chunk_no + 1, len(plan[page]), usage['input'] + usage['cache_write'] + usage['cache_read'], usage['output']), flush=True)
            return page, chunk_no, (out, usage), None
        except Exception as e:  # noqa
            with lock:
                print('  FAIL %s %d/%d: %s' % (page, chunk_no + 1, len(plan[page]), e), flush=True)
            return page, chunk_no, None, str(e)

    items = [(p, i, c) for p, cs in plan.items() for i, c in enumerate(cs)]
    results = {p: [None] * len(cs) for p, cs in plan.items()}
    done = {p: 0 for p in plan}

    with concurrent.futures.ThreadPoolExecutor(max_workers=args.jobs) as pool:
        for page, i, out, err in pool.map(work, items):
            if err:
                failures.append((page, i, err))
            else:
                results[page][i] = out
            done[page] += 1
            if done[page] == len(plan[page]) and all(r is not None for r in results[page]):
                source = read(os.path.join(MANUAL, page))
                text = add_anchors(source, '\n'.join(r[0].rstrip('\n') for r in results[page]) + '\n')
                page_usage = {}
                for r in results[page]:
                    add_usage(page_usage, r[1])
                log_usage(lang, page, args.model, words(source), len(plan[page]), page_usage)
                write(os.path.join(TRANSLATIONS, lang, page), text)
                manifest['pages'][page] = {
                    'source_sha': sha(source), 'model': args.model, 'chunks': len(plan[page]),
                    'translated_at': datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ'),
                }
                save_manifest(lang, manifest)     # after every page: an interrupted run loses nothing that was finished
    bad = sorted({f[0] for f in failures})
    print('done: %d pages written, %d pages failed%s' % (len(todo) - len(bad), len(bad), (': ' + ', '.join(bad)) if bad else ''))
    return 1 if bad else 0


def cmd_assemble(args):
    out = os.path.abspath(args.out)
    if os.path.exists(out):
        shutil.rmtree(out)
    shutil.copytree(MANUAL, out)
    manifest = load_manifest(args.lang)
    used = skipped = 0
    for p in source_pages():
        src = os.path.join(TRANSLATIONS, args.lang, p)
        state = page_state(args.lang, p, manifest)
        if state == 'missing' or (state == 'outdated' and args.only_current):
            skipped += 1
            continue
        shutil.copyfile(src, os.path.join(out, p))
        used += 1
    print('%s: %d translated pages in %s, %d pages stay English' % (args.lang, used, out, skipped))
    return 0


def retype_settings():
    text = read(os.path.join(ROOT, 'retype.yml'))
    url = re.search(r'^url:\s*(\S+)', text, re.M).group(1).rstrip('/')
    links = []
    block = re.search(r'^links:\n((?:[ \t]+.*\n|\n)+)', text, re.M)
    if block:
        for item in re.split(r'^\s*- ', block.group(1), flags=re.M)[1:]:
            d = dict(re.findall(r'(\w+):\s*(.+)', item.replace('\n  ', '\n')))
            links.append({k: v.strip() for k, v in d.items()})
    return url, links


def translated_languages():
    return [l for l in sorted(LANGUAGES) if os.path.isdir(os.path.join(TRANSLATIONS, l)) and load_manifest(l)['pages']]


def cmd_override(args):
    url, links = retype_settings()
    langs = translated_languages()
    switch = [{'text': 'English', 'link': url + '/', 'icon': 'globe'}] + \
             [{'text': LANGUAGES[l][1], 'link': '%s/%s/' % (url, l), 'icon': 'globe'} for l in langs]
    override = {'links': links + switch}
    if args.lang != 'en':
        override.update({'input': 'build/i18n/%s/manual' % args.lang, 'output': 'docs/%s' % args.lang, 'url': '%s/%s' % (url, args.lang)})
    print(json.dumps(override, ensure_ascii=False))
    return 0


def cmd_languages(args):
    """The languages that have translated pages: each gets its own site (docs/<lang>), see override."""
    for lang in translated_languages():
        print(lang)
    return 0


def cmd_glossary(args):
    text = read(os.path.join(MANUAL, args.page)) if args.page else '\n'.join(read(os.path.join(MANUAL, p)) for p in source_pages())
    gl = glossary(args.lang, text, limit=100000)
    for k, v in gl.items():
        print('%s -> %s' % (k, ' / '.join(v)))
    print('%d labels' % len(gl), file=sys.stderr)
    return 0


def main():
    ap = argparse.ArgumentParser(description=__doc__.split('\n')[0], formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest='cmd', required=True)

    def lang_arg(p, required=True):
        p.add_argument('--lang', required=required, choices=sorted(LANGUAGES) + (['en'] if p.prog.endswith('override') else []))

    p = sub.add_parser('status'); lang_arg(p, False); p.set_defaults(fn=cmd_status)
    p = sub.add_parser('translate'); lang_arg(p)
    p.add_argument('--model', default=os.environ.get('MANUAL_I18N_MODEL', 'haiku'))
    p.add_argument('--effort', default=os.environ.get('MANUAL_I18N_EFFORT'))
    p.add_argument('--only', action='append', help='page filter: a path or a path prefix below manual/ (user/getting-started/) or a glob; repeatable')
    p.add_argument('--limit', type=int, help='at most this many pages')
    p.add_argument('--jobs', type=int, default=2, help='requests at the same time (default 2)')
    p.add_argument('--chunk-words', type=int, default=1500)
    p.add_argument('--retries', type=int, default=2)
    p.add_argument('--timeout', type=int, default=900, help='seconds per request')
    p.add_argument('--force', action='store_true', help='translate current pages again too')
    p.add_argument('--dry-run', action='store_true')
    p.set_defaults(fn=cmd_translate)
    p = sub.add_parser('usage'); lang_arg(p); p.set_defaults(fn=cmd_usage)
    p = sub.add_parser('assemble'); lang_arg(p)
    p.add_argument('--out', required=True)
    p.add_argument('--only-current', action='store_true', help='outdated translations stay English')
    p.set_defaults(fn=cmd_assemble)
    p = sub.add_parser('override'); lang_arg(p); p.set_defaults(fn=cmd_override)
    p = sub.add_parser('languages'); p.set_defaults(fn=cmd_languages)
    p = sub.add_parser('glossary'); lang_arg(p); p.add_argument('page', nargs='?'); p.set_defaults(fn=cmd_glossary)
    args = ap.parse_args()
    sys.exit(args.fn(args))


if __name__ == '__main__':
    main()
