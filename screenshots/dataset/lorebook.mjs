// The lorebooks of the pictures, in the Marginalia lorebook file format (imported through Lorebooks -> Import).
// The lorebook of the demo book is the one of the dataset book of the tests (dataset/unregistered-lorebook.json, see dataset/from-fixture.py);
// the other two are written for the pictures of the trash and of the sub lorebooks.
// `node dataset/lorebook.mjs` writes dataset/build/lorebook.json, bestiary.json and companion.json
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));

const entry = (name, order, payload, { filtering = '', mode = 'TEXT', insertion = 'IN_LORE_BLOCK', tags = [], negativeTags = [], comment = '' } = {}) => ({
  name, payload, comment, enabled: true, order, filtering, filteringMode: mode, insertionMode: insertion, tags, negativeTags,
});

export const lorebook = JSON.parse(fs.readFileSync(path.join(here, 'unregistered-lorebook.json'), 'utf8'));
export const entries = lorebook.lorebooks[0].entries;

// A second lorebook that is deleted again (the Trash and Cleanup screens need something in the trash).
export const bestiary = {
  format: 'marginalia-lorebook',
  version: 2,
  root: '22222222-2222-4222-8222-222222222222',
  lorebooks: [{
    uuid: '22222222-2222-4222-8222-222222222222',
    name: 'Phrasebook',
    enabled: true,
    tags: [],
    subbooks: [],
    entries: [
      entry('mosh', 10, '**mosh** (Vrakht) - food. Said with a hand to the mouth; the first word Saellen teaches them on the ferry.', { filtering: 'mosh' }),
      entry('vat', 20, '**vat** (Vrakht) - water. Also the first part of the river\'s name, Vat-veg, the water-road.', { filtering: 'vat|Vat-veg', mode: 'REGEX' }),
      entry('Hoh', 30, '**Hoh** - the tone prefix of an official talking down to someone; Steing uses it even on his friends.', { filtering: 'Hoh' }),
    ],
  }],
};

// A lorebook with two subbooks (for the pictures of Mass edit, Count with subbooks and the tag windows; imported by shoot-books-extra).
const subbook = (uuid, name, tags, entries) => ({ uuid, name, enabled: true, tags, subbooks: [], entries });
export const companion = {
  format: 'marginalia-lorebook',
  version: 2,
  root: '33333333-3333-4333-8333-333333333333',
  lorebooks: [
    { uuid: '33333333-3333-4333-8333-333333333333', name: 'Dominion Companion', enabled: true, tags: ['companion'],
      subbooks: ['44444444-4444-4444-8444-444444444444', '55555555-5555-4555-8555-555555555555'],
      entries: [
        entry('The Dominion', 10, 'The Dominion is a land of ledgers: every person has a line, every line a guarantor, and every guarantor a number of days.', { filtering: 'Dominion|Vrakht', mode: 'REGEX' }),
        entry('Tally tokens', 20, 'A tally token is split in two; the halves are matched at the ledger house when a debt is paid.', { filtering: 'token' }),
      ] },
    subbook('44444444-4444-4444-8444-444444444444', 'Dominion Officials', ['officials'], [
      entry('Lieutenant Mangel', 10, '**Mangel** - lieutenant of the fort, tidy, tired, and the only officer who writes down what he cannot explain.', { filtering: 'Mangel' }),
      entry('Commander Frecht', 20, '**Frecht** - commander of Fort Stein-krog; speaks little and signs everything twice.', { filtering: 'Frecht' }),
      entry('Inspector Kestim', 30, '**Kestim** - the inspector from the capital, who counts the counters.', { filtering: 'Kestim' }),
    ]),
    subbook('55555555-5555-4555-8555-555555555555', 'Valley Places', ['places'], [
      entry('Silvakhor', 10, 'The market village on the far bank of the Lyrasil, with a chapel, a bridge and a ledger house.', { filtering: 'Silvakhor' }),
      entry('The Weir', 20, 'A low dam above the village where the river sounds like applause.', { filtering: 'weir' }),
      entry('Kaelthal', 30, 'A hollow in the hills where the reed-mother is said to sleep.', { filtering: 'Kaelthal' }),
    ]),
  ],
};

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  const out = path.join(here, 'build');
  fs.mkdirSync(out, { recursive: true });
  fs.writeFileSync(path.join(out, 'lorebook.json'), JSON.stringify(lorebook, null, 2));
  fs.writeFileSync(path.join(out, 'bestiary.json'), JSON.stringify(bestiary, null, 2));
  fs.writeFileSync(path.join(out, 'companion.json'), JSON.stringify(companion, null, 2));
  console.log('wrote lorebook.json (' + entries.length + ' entries) and bestiary.json to', out);
}
