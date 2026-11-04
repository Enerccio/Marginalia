#!/usr/bin/env python3
"""seed-conlang.py   Gives the administrator the language of the Conlang Translator pictures and a translated book.

The plugin has no step of its own in the chain (its pictures are taken by `bin/shoot-conlang.sh` on a copy of the last plugin step), so the
language is written into the database of the screenshot environment, with the application STOPPED:

  * the language `Orrelian` (dataset/conlang-orrelian.json, made by the plugin's model classes) and a changed copy of it,
    `Orrelian (draft)`, for the review of the differences, in the library of the user settings of the administrator;
  * the language chosen by the book *Harry Potter and Half Blood Prince - FDM* (its inherited settings) and the book published (the viewer);
  * the first speech of the second part and the first of the fourth written in Orrelian, each with its passage and the English as its source.

Usage: seed-conlang.py <marginalia.sqlite>
"""
import datetime, gzip, hashlib, json, re, sqlite3, sys, uuid
from pathlib import Path

KEY = 'com.github.enerccio.marginalia.extensions.conlangtranslator'
LIBRARY = KEY + '.languages'
BOOK = 'Harry Potter and Half Blood Prince - FDM'
SPEECHES = {  # the id of the part: the Orrelian text that replaces its first speech (with the quotation marks, as the model writes it)
    2: 'Sen ke dru ve, morael Potter?',
    4: 'Ka vol, morin. Na sen ke.',
}


def load(blob):
    raw = bytes(blob)
    return json.loads(gzip.decompress(raw) if raw[:2] == b'\x1f\x8b' else raw)


def dump(data):
    raw = json.dumps(data).encode()
    return gzip.compress(raw) if len(raw) >= 256 else raw


def draft(language):
    """A copy with a few changes: a word changed, one added, one deleted, a section rewritten."""
    copy = json.loads(json.dumps(language))
    copy['id'] = 'orrelian-draft'
    copy['name'] = 'Orrelian (draft)'
    for entry in copy['lexicon']:
        if entry['word'] == 'mor':
            entry['senses'] = [{'gloss': 'friend, comrade', 'partOfSpeech': '', 'note': ''}]
        if entry['word'] == 'kaeli':
            entry['word'] = 'kaeni'
    copy['lexicon'] = [entry for entry in copy['lexicon'] if entry['word'] != 'vol']
    added = json.loads(json.dumps(next(entry for entry in copy['lexicon'] if entry['word'] == 'sen')))
    added.update({'id': str(uuid.uuid4()), 'word': 'thel', 'senses': [{'gloss': 'sing', 'partOfSpeech': '', 'note': ''}], 'status': 'NEW'})
    copy['lexicon'].append(added)
    for section in copy['rules']:
        if section['title'] == 'Grammar':
            section['text'] += ' A plural of a word that ends in -ae takes -s.'
    return copy


def main(path):
    db = sqlite3.connect(path)
    language = json.loads((Path(__file__).resolve().parent.parent / 'dataset' / 'conlang-orrelian.json').read_text())
    admin = db.execute("select id from users where login = 'admin'").fetchone()[0]
    row = db.execute("select id, extendedContent from settings where DTYPE = 'UserSetting' and userId = ?", (admin,)).fetchone()
    settings = load(row[1])
    settings['attributes_' + LIBRARY] = [language, draft(language)]
    db.execute('update settings set extendedContent = ? where id = ?', (dump(settings), row[0]))

    book_id, book = db.execute('select id, extendedContent from manuscripts where name = ?', (BOOK,)).fetchone()
    data = load(book)
    data['attributes_inheritedSettings'] = {'conlangtranslator.language': language['id'], 'conlangtranslator.reader': 'click'}
    db.execute('update manuscripts set extendedContent = ?, published = 1 where id = ?', (dump(data), book_id))

    next_id = 2000
    now = datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%d %H:%M:%S')
    for part, orrelian in SPEECHES.items():
        stored, owner = db.execute('select extendedContent, userId from messages where id = ?', (part,)).fetchone()
        message = load(stored)
        # the speech without its quotation marks, the first one of the part
        found = re.search(r'"([^"]{20,}?)"', message['response'])
        english = found.group(1)
        start = found.start(1)
        message['response'] = message['response'][:start] + orrelian + message['response'][start + len(english):]
        db.execute('update messages set extendedContent = ? where id = ?', (dump(message), part))
        span = {'language': language['id'], 'source': english, 'speaker': 'Draconia' if part == 2 else 'Hermione', 'understood': True,
                'register': 'formal' if part == 2 else '', 'textHash': hashlib.sha256(orrelian.encode()).hexdigest()[:16],
                'model': 'marginalia-demo-12b', 'time': 1762200000000}
        next_id += 1
        db.execute('insert into annotations(id, creation, is_deleted, modification, uuid, extendedContent, userId, part_id, kind, passageKind, startOffset, endOffset)'
                   ' values (?,?,?,?,?,?,?,?,?,?,?,?)',
                   (next_id, now, 0, now, str(uuid.uuid4()), dump({'attributes_' + KEY: span, 'quote': orrelian, 'contextBefore': '', 'contextAfter': ''}),
                    owner, part, 'PASSAGE', 'conlang-translator.speech', start, start + len(orrelian)))
    db.execute('update annotations_SEQ set next_val = ?', (next_id + 1000,))
    db.commit()
    print('seeded: Orrelian, Orrelian (draft), the book and %d speeches' % len(SPEECHES))


if __name__ == '__main__':
    main(sys.argv[1])
