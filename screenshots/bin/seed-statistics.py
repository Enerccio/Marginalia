#!/usr/bin/env python3
"""seed-statistics.py   Gives the administrator a year of writing history for the pictures of the statistics windows.

The dataset is made in a few days (and the statistics are only kept since 1.6.0, the older days are rebuilt from the parts), so
the book would show one or two days on its calendar. This writes the rows that the application would have written over the
last year into the database of the screenshot environment (rows of `manuscript_statistics`, `manuscript_sizes` and
`activity_intervals` of the book *Harry Potter and Half Blood Prince - FDM*), so run it with the application STOPPED, on a copy
of 13-db-backup (`bin/shoot-all.sh` does), and never on anything that is kept. The numbers are made by a fixed random generator,
so every run gives the same picture; the days are counted back from today.

  * the last 200 days are "live" (they have times), the older ones are "rebuilt" (marked `reconstructed`, no time),
    and `statisticsSince` of the user says so, like an installation that was upgraded 200 days ago;
  * the last 10 days (today with it) are a streak, and the days with a lot of words are the days of the goal
    (500 words or 30 minutes, which the flow `shoot-statistics` sets in the settings).

Usage: seed-statistics.py <marginalia.sqlite> [--today YYYY-MM-DD]
"""
import argparse, datetime, gzip, json, random, sqlite3, uuid

BOOK = '870177b2-ab5e-49fe-8afb-e54e71e653d3'
NAME = 'Harry Potter and Half Blood Prince - FDM'
OWNER = 1                      # admin
LIVE_DAYS = 200
FIRST_ID = 1_000_000           # ids of the seeded rows; the sequences are moved above them


def ms(moment):
    return int(moment.timestamp() * 1000)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('database')
    ap.add_argument('--today', default=datetime.date.today().isoformat())
    args = ap.parse_args()
    today = datetime.date.fromisoformat(args.today)
    rnd = random.Random(1606)
    db = sqlite3.connect(args.database)
    words_now, tokens_now = db.execute('select sum(wordCount), sum(tokenCount) from messages where is_deleted = 0 and parentScript_id = 1').fetchone()
    taken = {r[0] for r in db.execute('select day from manuscript_statistics where bookUuid = ? and is_agent = 0', (BOOK,))}

    # which days were worked on: denser the nearer to today, more at the weekend, a streak of 10 days at the end, a long one before
    worked = {}
    for back in range(0, 366):
        day = today - datetime.timedelta(days=back)
        if back < 10:
            chance = 1.0
        elif 22 <= back <= 38:
            chance = 0.95                         # the longest streak
        elif back < 60:
            chance = 0.6
        elif back < 150:
            chance = 0.42
        else:
            chance = 0.22
        if day.weekday() >= 5:
            chance = min(1.0, chance + 0.15)
        if rnd.random() < chance:
            words = int(max(30, rnd.gauss(520 if back < 150 else 380, 330)))
            worked[day] = words
    worked[today] = 310                           # today is half way to the goal

    stats, sizes, intervals = [], [], []
    seq = FIRST_ID
    size = 0
    # the book grew by what was written and generated, minus what was removed; the last row is the real size of the book
    plan = sorted(worked.items())
    grown = sum(w + int(w * 2.4) - int(w * 0.35) for _, w in plan)
    scale = (words_now or 2000) / max(1, grown)
    for day, words in plan:
        back = (today - day).days
        live = back < LIVE_DAYS
        iso = day.isoformat()
        if iso in taken:
            continue
        generated = int(words * rnd.uniform(1.6, 3.2))
        removed = int(words * rnd.uniform(0.1, 0.5))
        requests = max(1, generated // 450)
        prefill = requests * rnd.randint(2800, 9000)
        gen_tokens = int(generated * 1.35)
        created = max(1, generated // 700)
        edits = max(1, words // 160)
        hour = rnd.choice([7, 9, 14, 19, 20, 21, 22])
        minute = rnd.randint(0, 59)
        last = datetime.datetime(day.year, day.month, day.day, hour, minute)
        if day == today:
            last = datetime.datetime.now().replace(microsecond=0)
        stats.append((seq, ms(last), ms(last), str(uuid.uuid4()), OWNER, BOOK, NAME, '', iso, 0, prefill, gen_tokens, requests, words,
                      generated, removed, created, 0, edits, 0 if live else 1, ms(last)))
        seq += 1
        size += words + generated - removed
        book_words = int(size * scale)
        sizes.append((seq, ms(last), ms(last), str(uuid.uuid4()), OWNER, BOOK, NAME, '', iso, book_words, int(book_words * 1.3),
                      book_words, int(book_words * 1.3), None, 0, 0 if live else 1, ms(last)))
        seq += 1
        if live:
            # the author edited about as long as the words took (25 words a minute and some thinking), in one to three stretches
            minutes = max(4, int(words / rnd.uniform(14, 30)))
            if day == today:
                minutes = 12
            parts = rnd.randint(1, 3) if day != today else 1
            start = datetime.datetime(day.year, day.month, day.day, hour, minute) - datetime.timedelta(minutes=minutes + 20 * parts)
            if day == today:
                start = last - datetime.timedelta(minutes=minutes + 5)
            cursor = start
            open_from = start - datetime.timedelta(minutes=5)
            for i in range(parts):
                length = max(2, minutes // parts)
                end = cursor + datetime.timedelta(minutes=length)
                intervals.append((seq, ms(end), ms(end), str(uuid.uuid4()), OWNER, BOOK, NAME, '', 'EDIT', 0, iso, ms(cursor), ms(end)))
                seq += 1
                cursor = end + datetime.timedelta(minutes=rnd.randint(3, 15))
            open_to = cursor
            intervals.append((seq, ms(open_to), ms(open_to), str(uuid.uuid4()), OWNER, BOOK, NAME, '', 'OPEN', 0, iso, ms(open_from), ms(open_to)))
            seq += 1
            gen_from = start + datetime.timedelta(minutes=1)
            gen_to = gen_from + datetime.timedelta(minutes=max(1, requests))
            intervals.append((seq, ms(gen_to), ms(gen_to), str(uuid.uuid4()), OWNER, BOOK, NAME, '', 'GENERATE', 0, iso, ms(gen_from), ms(gen_to)))
            seq += 1

    # the last size row is the real one (the application measures the same sum)
    if sizes and words_now:
        sizes[-1] = sizes[-1][:9] + (words_now, tokens_now, words_now, tokens_now) + sizes[-1][13:]

    db.executemany('insert or ignore into manuscript_statistics (id, creation, modification, uuid, userId, bookUuid, bookName, folderPath, day, is_agent, '
                   'prefillTokens, generatedTokens, requests, wordsWritten, wordsGenerated, wordsRemoved, partsCreated, partsDeleted, edits, reconstructed, '
                   'lastChange, is_deleted) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0)', stats)
    db.executemany('insert or ignore into manuscript_sizes (id, creation, modification, uuid, userId, bookUuid, bookName, folderPath, day, bookWords, '
                   'bookTokens, branchWords, branchTokens, leafUuid, baseline, reconstructed, lastChange, is_deleted) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0)', sizes)
    db.executemany('insert into activity_intervals (id, creation, modification, uuid, userId, bookUuid, bookName, folderPath, kind, is_agent, day, '
                   'startTime, endTime, is_deleted) values (?,?,?,?,?,?,?,?,?,?,?,?,?,0)', intervals)
    for table in ('manuscript_statistics', 'manuscript_sizes', 'activity_intervals'):
        db.execute('update %s_SEQ set next_val = ?' % table, (seq + 1000,))

    # the time is recorded since the live days started
    since = (today - datetime.timedelta(days=LIVE_DAYS - 1)).isoformat()
    row = db.execute('select extendedContent from settings where userId = ? and DTYPE = ?', (OWNER, 'UserSetting')).fetchone()
    content = json.loads(gzip.decompress(row[0]))
    content['statisticsSince'] = since
    db.execute('update settings set extendedContent = ? where userId = ? and DTYPE = ?', (gzip.compress(json.dumps(content).encode()), OWNER, 'UserSetting'))
    db.commit()
    print('seeded %d days (%d rows of counts, %d of sizes, %d stretches), time recorded since %s' % (len(worked), len(stats), len(sizes), len(intervals), since))


main()
