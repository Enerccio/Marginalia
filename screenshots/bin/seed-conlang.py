#!/usr/bin/env python3
"""seed-conlang.py   Gives the administrator the language of the Conlang Translator pictures and a translated part.

The plugin has no step of its own in the chain (its pictures are taken by `bin/shoot-conlang.sh` on a copy of the last plugin step), so the
language is written into the database of the screenshot environment, with the application STOPPED:

  * the language `Vrakht` (dataset/conlang-vrakht.json, the language of the dataset book of the tests, made by the plugin's model classes) and a changed
    copy of it, `Vrakht (draft)`, for the review of the differences, in the library of the user settings of the administrator;
  * the language chosen by the book *Unregistered* (its inherited settings) and the book published (the viewer);
  * one more part after the last one, the part of the dataset book of the tests in which Steing reads a sentence and Paskal answers, with its
    speeches written in Vrakht, each with its passage and the English as its source (dataset/conlang-part.json, made by bin/from-fixture.py's sibling
    dataset/from-fixture.py --conlang).

Usage: seed-conlang.py <marginalia.sqlite>
"""
import datetime, gzip, hashlib, json, sqlite3, sys, uuid
from pathlib import Path

KEY = 'com.github.enerccio.marginalia.extensions.conlangtranslator'
LIBRARY = KEY + '.languages'
BOOK = 'Unregistered'
DATASET = Path(__file__).resolve().parent.parent / 'dataset'


def load(blob):
    raw = bytes(blob)
    return json.loads(gzip.decompress(raw) if raw[:2] == b'\x1f\x8b' else raw)


def dump(data):
    raw = json.dumps(data).encode()
    return gzip.compress(raw) if len(raw) >= 256 else raw


def draft(language):
    """A copy with a few changes: a word changed, one added, one deleted, a section rewritten."""
    copy = json.loads(json.dumps(language))
    copy['id'] = 'vrakht-draft'
    copy['name'] = 'Vrakht (draft)'
    for entry in copy['lexicon']:
        if entry['word'] == 'mosh':
            entry['senses'] = [{'gloss': 'Food / Meal (also: a bowl of it)', 'partOfSpeech': '', 'note': ''}]
        if entry['word'] == 'nakht':
            entry['word'] = 'nakt'
    copy['lexicon'] = [entry for entry in copy['lexicon'] if entry['word'] != 'lun']
    added = json.loads(json.dumps(next(entry for entry in copy['lexicon'] if entry['word'] == 'vat')))
    added.update({'id': str(uuid.uuid4()), 'word': 'thel', 'senses': [{'gloss': 'Sing / Hum', 'partOfSpeech': '', 'note': ''}], 'status': 'NEW'})
    copy['lexicon'].append(added)
    for section in copy['rules']:
        if section['title'] == 'Syntax and grammar':
            section['text'] += '\n\nA plural of a word that ends in -ou takes -s.'
    return copy


def main(path):
    db = sqlite3.connect(path)
    language = json.loads((DATASET / 'conlang-vrakht.json').read_text())
    part = json.loads((DATASET / 'conlang-part.json').read_text())
    admin = db.execute("select id from users where login = 'admin'").fetchone()[0]
    row = db.execute("select id, extendedContent from settings where DTYPE = 'UserSetting' and userId = ?", (admin,)).fetchone()
    settings = load(row[1])
    settings['attributes_' + LIBRARY] = [language, draft(language)]
    db.execute('update settings set extendedContent = ? where id = ?', (dump(settings), row[0]))

    book_id, book, leaf, owner = db.execute('select id, extendedContent, activeLeaf_id, userId from manuscripts where name = ?', (BOOK,)).fetchone()
    data = load(book)
    data['attributes_com.github.enerccio.marginalia.extensions.conlangtranslator.book'] = json.loads((DATASET / 'conlang-book.json').read_text())
    data['attributes_inheritedSettings'] = {'conlangtranslator.language': language['id'], 'conlangtranslator.reader': 'click'}

    # the part after the last one of the book
    message_id = 5000
    now = datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%d %H:%M:%S')
    content = {'instructions': part['instructions'], 'request': '2026-11-06T10:00:00Z', 'presentCharacters': part['present'], 'sceneSetting': part['scene'],
               'protocolUsed': 'Chat Completion', 'responseReasoning': '', 'modelUsed': 'marginalia-demo-12b', 'builtPromptTokens': '4200',
               'response': part['response'], 'promptTokens': '4100', 'povCharacter': part['pov'], 'originalResponse': part['original']}
    db.execute('insert into messages(id, creation, is_deleted, modification, uuid, extendedContent, edited, tokenCount, wordCount, userId, parent_id, parentScript_id, _fulltext)'
               ' values (?,?,?,?,?,?,?,?,?,?,?,?,?)',
               (message_id, now, 0, now, str(uuid.uuid4()), dump(content), 1, part['tokens'], part['words'], owner, leaf, book_id, part['response']))
    db.execute('update messages_SEQ set next_val = ?', (message_id + 1000,))
    db.execute('update manuscripts set extendedContent = ?, published = 1, activeLeaf_id = ? where id = ?', (dump(data), message_id, book_id))

    next_id = 2000
    for passage in part['passages']:
        next_id += 1
        span = dict(passage['span'], language=language['id'], model='marginalia-demo-12b', time=1762200000000,
                    textHash=hashlib.sha256(passage['quote'].encode()).hexdigest()[:16])
        db.execute('insert into annotations(id, creation, is_deleted, modification, uuid, extendedContent, userId, part_id, kind, passageKind, startOffset, endOffset)'
                   ' values (?,?,?,?,?,?,?,?,?,?,?,?)',
                   (next_id, now, 0, now, str(uuid.uuid4()),
                    dump({'attributes_' + KEY: span, 'quote': passage['quote'], 'contextBefore': passage['before'], 'contextAfter': passage['after']}),
                    owner, message_id, 'PASSAGE', 'conlang-translator.speech', passage['start'], passage['end']))
    db.execute('update annotations_SEQ set next_val = ?', (next_id + 1000,))
    db.commit()
    print('seeded: Vrakht, Vrakht (draft), the book and a part with %d speeches' % len(part['passages']))


if __name__ == '__main__':
    main(sys.argv[1])
