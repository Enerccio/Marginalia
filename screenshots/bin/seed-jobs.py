#!/usr/bin/env python3
"""seed-jobs.py   Gives the log of the jobs (Admin -> Jobs) some history for the pictures.

The dataset is made in a few minutes, so the log would hold a handful of generations. This writes the rows the application would have
written over the last days (rows of `jobs` and `job_calls`: a generation, an annotation, a rewrite of the summaries, an agent run, an
audiobook conversion with its labelling and speech calls, images, a failed and a cancelled job and three old ones) into the database
of the screenshot environment. Run it with the application STOPPED, after the first start of a version with the migration V28, on a
copy of 13-db-backup (`bin/shoot-jobs.sh` does), never on anything that is kept. The times are counted back from now.

Usage: seed-jobs.py <marginalia.sqlite>
"""
import sqlite3, sys, time, uuid

FIRST_ID = 1_000_000
LLM, AUDIO, IMAGE = 'LLM', 'AUDIO', 'IMAGE'
now = int(time.time() * 1000)
MIN, HOUR, DAY = 60_000, 3_600_000, 86_400_000


def llm(backend='Local LLM', model='marginalia-demo-12b'):
    return (LLM, backend, model)


# (user, kind, label, status, started (ms before now), [(type, backend, model), ...], [duration of each call, ms], failed index or None, error)
JOBS = [
    ('admin', 'generation', None, 'COMPLETED', 4 * MIN, [llm()], [41_000], None, None),
    ('admin', 'annotate', None, 'COMPLETED', 26 * MIN, [llm('Local Reasoner', 'marginalia-demo-reasoner'), llm()], [63_000, 17_500], None, None),
    ('admin', 'summary', 'Rewrite of the summaries, 3 steps', 'COMPLETED', 51 * MIN, [llm()] * 3, [12_000, 14_500, 21_000], None, None),
    ('jana', 'generation', None, 'CANCELLED', 70 * MIN, [llm()], [9_000], None, None),
    ('admin', 'agent', 'Agent run 7', 'COMPLETED', 2 * HOUR, [llm('Local Reasoner', 'marginalia-demo-reasoner')] * 6, [35_000, 22_000, 18_000, 41_000, 27_000, 30_000], None, None),
    ('admin', 'audiobook', 'Conversion of 2 parts', 'COMPLETED', 3 * HOUR,
     [llm(), llm()] + [(AUDIO, 'tts.local', 'chatterbox-turbo')] * 10,
     [38_000, 34_000] + [9_500, 11_200, 8_700, 12_400, 10_100, 9_900, 13_000, 8_200, 10_800, 9_300], None, None),
    ('admin', 'image-generator', None, 'COMPLETED', 5 * HOUR, [(IMAGE, 'images.local', 'flux-schnell')], [38_000], None, None),
    ('admin', 'image-generator', None, 'FAILED', 5 * HOUR + 20 * MIN, [(IMAGE, 'images.local', 'flux-schnell')], [800], 0,
     'Cannot reach images.local: Connection refused'),
    ('admin', 'audiobook', 'Conversion of 1 part', 'FAILED', 8 * HOUR,
     [llm()] + [(AUDIO, 'tts.local', 'chatterbox-turbo')] * 3, [29_000, 9_000, 10_200, 31_000], 3,
     'The speech server has no model loaded (503).'),
    ('jana', 'generation', None, 'COMPLETED', 1 * DAY + 2 * HOUR, [llm()], [47_000], None, None),
    ('jana', 'summary', None, 'COMPLETED', 1 * DAY + 3 * HOUR, [llm()], [19_000], None, None),
    ('admin', 'untagged', None, 'COMPLETED', 2 * DAY, [llm()], [6_000], None, None),
    ('admin', 'generation', None, 'COMPLETED', 45 * DAY, [llm()], [44_000], None, None),
    ('admin', 'generation', None, 'COMPLETED', 46 * DAY, [llm()], [39_000], None, None),
    ('jana', 'annotate', None, 'COMPLETED', 47 * DAY, [llm('Local Reasoner', 'marginalia-demo-reasoner')], [58_000], None, None),
]


def main():
    db = sqlite3.connect(sys.argv[1])
    ids = {login: uid for uid, login in db.execute('select id, login from users')}
    job_id = FIRST_ID
    call_id = FIRST_ID
    for user, kind, label, status, ago, calls, durations, failed, error in sorted(JOBS, key=lambda j: -j[4]):
        started = now - ago
        t = started
        rows = []
        for i, ((ctype, backend, model), dur) in enumerate(zip(calls, durations)):
            call_status = 'FAILED' if i == failed else ('CANCELLED' if status == 'CANCELLED' else 'COMPLETED')
            rows.append((call_id, t, t, str(uuid.uuid4()), job_id, ctype, backend, model, call_status, t, t + dur,
                         error if i == failed else None))
            call_id += 1
            t += dur + 150
        db.execute('insert into jobs (id, creation, is_deleted, modification, uuid, userId, ownerLogin, kind, label, status, startedAt, endedAt, '
                   'callCount, failedCalls) values (?,?,0,?,?,?,?,?,?,?,?,?,?,?)',
                   (job_id, t, t, str(uuid.uuid4()), ids[user], user, kind, label, status, started, t, len(calls), 0 if failed is None else 1))
        db.executemany('insert into job_calls (id, creation, is_deleted, modification, uuid, job_id, callType, backend, model, status, startedAt, '
                       'endedAt, error) values (?,?,0,?,?,?,?,?,?,?,?,?,?)',
                       [(r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9], r[10], r[11]) for r in rows])
        job_id += 1
    db.execute('update jobs_SEQ set next_val = ?', (job_id + 1000,))
    db.execute('update job_calls_SEQ set next_val = ?', (call_id + 1000,))
    db.commit()
    print('seeded %d jobs' % len(JOBS))


main()
