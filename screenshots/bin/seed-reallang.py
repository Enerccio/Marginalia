#!/usr/bin/env python3
"""seed-reallang.py   Gives the administrator the languages of the Real Language Translator pictures and a translated part.

Like seed-conlang.py (the application is STOPPED): the library of the user settings gets *Japanese* (kana and kanji, with the reading) and *Czech*, the
book *Unregistered* chooses Japanese and is published, and one more part after the last one (the ferry landing of the dataset book of the tests, with
its speeches in Japanese, dataset/reallang-part.json) is added, each speech with its passage and the English as its source.

Usage: seed-reallang.py <marginalia.sqlite>
"""
import datetime, gzip, hashlib, json, sqlite3, sys, uuid
from pathlib import Path

KEY = 'com.github.enerccio.marginalia.extensions.conlangtranslator'
LIBRARY = 'com.github.enerccio.marginalia.extensions.reallangtranslator.languages'
BOOK = 'Unregistered'
DATASET = Path(__file__).resolve().parent.parent / 'dataset'


def load(blob):
    raw = bytes(blob)
    return json.loads(gzip.decompress(raw) if raw[:2] == b'\x1f\x8b' else raw)


def dump(data):
    raw = json.dumps(data).encode()
    return gzip.compress(raw) if len(raw) >= 256 else raw


POLITENESS = ("Choose the level of speech from the relationship of the speaker and the listener (age, rank, closeness, the place): the plain form between close "
              "equals, family and children; the polite desu/masu form with strangers, colleagues and equals; the honorific and humble forms (keigo) to superiors, "
              "customers and elders. Keep the same level for the same pair of people throughout, and change it only when the relationship or the mood changes. "
              "Let the pronouns, the sentence-final particles and the word choice carry the gender and the voice of the speaker.")
NAMES = ("Names of people and places keep their identity: a Japanese name is written the way a Japanese text would write it, a foreign name in katakana (or in "
         "Latin letters when the script is romaji). Family name first for Japanese characters unless the story orders the names otherwise.")
JAPANESE = {'id': 'b24a015e-fb7c-47a1-b590-8663e2c6fa27', 'name': 'Japanese', 'tongue': 'Japanese', 'script': 'KANA_KANJI', 'honorifics': True, 'reading': True,
            'politeness': POLITENESS, 'names': NAMES, 'rules': ''}
CZECH = {'id': 'dab63a83-21f5-4bd0-927c-8a99a88fae9d', 'name': 'Czech', 'tongue': 'Czech', 'script': 'NATIVE', 'honorifics': False, 'reading': False,
         'politeness': 'Informal and short: it is the private language of a man who mutters when he is stressed.', 'names': 'Names stay as they are, with their diacritics.',
         'rules': ''}


def main(path):
    db = sqlite3.connect(path)
    language = JAPANESE
    part = json.loads((DATASET / 'reallang-part.json').read_text())
    admin = db.execute("select id from users where login = 'admin'").fetchone()[0]
    row = db.execute("select id, extendedContent from settings where DTYPE = 'UserSetting' and userId = ?", (admin,)).fetchone()
    settings = load(row[1])
    settings['attributes_' + LIBRARY] = [JAPANESE, CZECH]
    db.execute('update settings set extendedContent = ? where id = ?', (dump(settings), row[0]))

    book_id, book, leaf, owner = db.execute('select id, extendedContent, activeLeaf_id, userId from manuscripts where name = ?', (BOOK,)).fetchone()
    data = load(book)
    data['attributes_com.github.enerccio.marginalia.extensions.reallangtranslator.book'] = json.loads((DATASET / 'reallang-book.json').read_text())
    data['attributes_inheritedSettings'] = {'reallangtranslator.language': language['id']}

    # the part after the last one of the book
    message_id = 5100
    now = datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%d %H:%M:%S')
    content = {'instructions': part['instructions'], 'request': '2026-11-06T10:00:00Z', 'presentCharacters': part['present'], 'sceneSetting': part['scene'],
               'protocolUsed': 'Chat Completion', 'responseReasoning': '', 'modelUsed': 'marginalia-demo-12b', 'builtPromptTokens': '4200',
               'response': part['response'], 'promptTokens': '4100', 'povCharacter': part['pov'], 'originalResponse': part['original']}
    db.execute('insert into messages(id, creation, is_deleted, modification, uuid, extendedContent, edited, tokenCount, wordCount, userId, parent_id, parentScript_id, _fulltext)'
               ' values (?,?,?,?,?,?,?,?,?,?,?,?,?)',
               (message_id, now, 0, now, str(uuid.uuid4()), dump(content), 1, part['tokens'], part['words'], owner, leaf, book_id, part['response']))
    db.execute('update messages_SEQ set next_val = ?', (message_id + 1000,))
    db.execute('update manuscripts set extendedContent = ?, published = 1, activeLeaf_id = ? where id = ?', (dump(data), message_id, book_id))

    next_id = 2100
    for passage in part['passages']:
        next_id += 1
        span = dict(passage['span'], language=language['id'], model='marginalia-demo-12b', time=1762200000000,
                    textHash=hashlib.sha256(passage['quote'].encode()).hexdigest()[:16])
        db.execute('insert into annotations(id, creation, is_deleted, modification, uuid, extendedContent, userId, part_id, kind, passageKind, startOffset, endOffset)'
                   ' values (?,?,?,?,?,?,?,?,?,?,?,?)',
                   (next_id, now, 0, now, str(uuid.uuid4()),
                    dump({'attributes_' + KEY: span, 'quote': passage['quote'], 'contextBefore': passage['before'], 'contextAfter': passage['after']}),
                    owner, message_id, 'PASSAGE', 'reallang-translator.speech', passage['start'], passage['end']))
    db.execute('update annotations_SEQ set next_val = ?', (next_id + 1000,))
    db.commit()
    print('seeded: Japanese, Czech, the book and a part with %d speeches' % len(part['passages']))


if __name__ == '__main__':
    main(sys.argv[1])
