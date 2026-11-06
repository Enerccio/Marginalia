#!/usr/bin/env python3
"""Takes the story of the pictures from the dataset book "Unregistered" of the tests (marginalia/src/test/resources/workset).

Writes dataset/unregistered-parts.json (the first parts: scene, pov, present characters, instructions and the text the model wrote),
dataset/unregistered-lorebook.json (the lorebook of the book, in the file format of the lorebook export) dataset/conlang-part.json and dataset/reallang-part.json (later parts with
speeches in Vrakht and in Japanese and their passages, for bin/seed-conlang.py and bin/seed-reallang.py). The files are in Git, so the
screenshots do not need the test resources; run this script only to take a different part of the book.
"""
import json
import re
import sys
import zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
FIXTURE = HERE.parent.parent / 'marginalia' / 'src' / 'test' / 'resources' / 'workset'
PARTS = int(sys.argv[1]) if len(sys.argv) > 1 else 6

backup = json.loads(zipfile.ZipFile(FIXTURE / 'book.zip').read('backup.json'))['backup']
children = {}
for message in backup['messages']:
    children.setdefault(message.get('parentUuid'), []).append(message)
chain, current = [], children[None][0]
while current:
    chain.append(current)
    nxt = children.get(current['uuid'])
    current = nxt[0] if nxt else None

parts = []
for message in chain[:PARTS]:
    e = message['extendedContent']
    instructions = re.sub(r'\s*About \d+ words\.?', '', e['instructions']).strip()
    parts.append({
        'scene': e['sceneSetting'], 'pov': e['povCharacter'], 'present': e['presentCharacters'],
        'instructions': instructions, 'reply': e['originalResponse'].strip(),
    })
def translated_part(number, kind, language=None):
    """The part of the book with speeches written in Vrakht (seed-conlang.py puts it after the last part of the book of the pictures)."""
    message = chain[number - 1]
    e = message['extendedContent']
    passages = []
    for annotation in message.get('annotations', []):
        if annotation.get('passageKind') != kind:
            continue
        extended = annotation['extendedContent']
        span = extended['attributes_com.github.enerccio.marginalia.extensions.conlangtranslator']
        if language and span.get('language') != language:
            continue
        keep = {k: v for k, v in span.items() if k in ('source', 'speaker', 'understood', 'register', 'reading')}
        quote = extended['quote']
        assert e['response'][annotation['start']:annotation['end']] == quote, 'offsets of a passage do not match'
        passages.append({'quote': quote, 'start': annotation['start'], 'end': annotation['end'], 'before': extended.get('contextBefore', ''),
                         'after': extended.get('contextAfter', ''), 'span': keep})
    return {'scene': e['sceneSetting'], 'pov': e['povCharacter'], 'present': e['presentCharacters'],
            'instructions': re.sub(r'\s*About \d+ words\.?', '', e['instructions']).strip(), 'response': e['response'], 'original': e['originalResponse'],
            'tokens': message['tokenCount'], 'words': message['wordCount'], 'passages': passages}


book_settings = backup['extendedContent']
(HERE / 'conlang-book.json').write_text(json.dumps(book_settings['attributes_com.github.enerccio.marginalia.extensions.conlangtranslator.book'], ensure_ascii=False, indent=1) + '\n', encoding='utf-8')
(HERE / 'reallang-book.json').write_text(json.dumps(book_settings['attributes_com.github.enerccio.marginalia.extensions.reallangtranslator.book'], ensure_ascii=False, indent=1) + '\n', encoding='utf-8')
(HERE / 'conlang-part.json').write_text(json.dumps(translated_part(11, 'conlang-translator.speech'), ensure_ascii=False, indent=1) + '\n', encoding='utf-8')
# the part with the speech in Japanese (the language of the fixture book: b24a015e...)
(HERE / 'reallang-part.json').write_text(json.dumps(translated_part(7, 'reallang-translator.speech', 'b24a015e-fb7c-47a1-b590-8663e2c6fa27'), ensure_ascii=False, indent=1) + '\n', encoding='utf-8')
(HERE / 'unregistered-parts.json').write_text(json.dumps(parts, ensure_ascii=False, indent=1) + '\n', encoding='utf-8')
(HERE / 'unregistered-lorebook.json').write_bytes((FIXTURE / 'lorebook.json').read_bytes())
print('wrote', len(parts), 'parts and the lorebook')
