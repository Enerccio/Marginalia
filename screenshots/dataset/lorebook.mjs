// The lorebook of the demo book, in the Marginalia lorebook file format (imported through Lorebooks -> Import).
// `node dataset/lorebook.mjs` writes dataset/build/lorebook.json
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));

const entry = (name, order, payload, { filtering = '', mode = 'TEXT', insertion = 'IN_LORE_BLOCK', tags = [], negativeTags = [], comment = '' } = {}) => ({
  name, payload, comment, enabled: true, order, filtering, filteringMode: mode, insertionMode: insertion, tags, negativeTags,
});

export const entries = [
  entry('Setting', 10,
`Autumn 1996, Harry's sixth year at Hogwarts. Voldemort is back and the wizarding world is on edge: the Daily Prophet prints lists of missing people, Aurors stand guard on the main street of Hogsmeade, and students are told to travel in groups. The weekend visits to the village are still allowed.
In this story Draco Malfoy is a girl, Draconia Malfoy. Everything else follows the books unless the story changes it.`),
  entry('Style', 15,
`Write in third person, past tense, close to the point of view character. Show feelings through gestures and dialogue rather than naming them. Keep the pace of a novel: several paragraphs per part, no summaries of what happens next, and end on a moment the next part can pick up.`,
    { insertion: 'BEFORE_USER_PROMPT', comment: 'Always active; goes right before the instructions of the part.' }),
  entry('Harry Potter', 20,
`**Harry Potter** - sixteen, Gryffindor, Seeker. Messy black hair, round glasses, a thin scar on his forehead. Brave to the point of recklessness, bad at leaving a mystery alone, and quietly convinced that Draconia is up to something this year. Speaks bluntly; under pressure he gets quieter, not louder.`,
    { filtering: 'Harry' }),
  entry('Draconia Malfoy', 25,
`**Draconia Malfoy** - sixteen, Slytherin, the heir of the Malfoy family. Straight platinum blonde hair, grey eyes, always immaculate. Sharp-tongued and proud; her contempt is a shield. Since the summer she has been pale, sleeps badly and disappears for hours: she was given a task she cannot refuse and cannot talk about. Pretends not to care about Harry, and is the only one who looks at him as if he were an equal.`,
    { filtering: 'Draconia' }),
  entry('Ginny Weasley', 30,
`**Ginny Weasley** - fifteen, Gryffindor, Chaser, Ron's younger sister. Red hair, freckles, a temper everyone has learned to respect. Direct, funny, unimpressed by Harry's brooding. Does not like Draconia and says so.`,
    { filtering: 'Ginny' }),
  entry('Hermione Granger', 35,
`**Hermione Granger** - sixteen, Gryffindor, prefect. Bushy brown hair, a bag that holds too many books. Notices details others miss, hates being wrong, will not let a rule be broken without a lecture - and will break one herself for a friend.`,
    { filtering: 'Hermione' }),
  entry('Ron Weasley', 40,
`**Ron Weasley** - sixteen, Gryffindor, Keeper. Tall, freckled, loyal and easily embarrassed. Jokes when he is worried. Three butterbeers make him loud and give him the hiccups.`,
    { filtering: 'Ron' }),
  entry('Severus Snape', 45,
`**Professor Severus Snape** - Potions master and, this year, teacher of Defence Against the Dark Arts. Black hair to his shoulders, black robes, a voice that never rises. Takes points from Gryffindor for breathing, and watches Draconia more closely than he admits.`,
    { filtering: 'Snape' }),
  entry('Hogsmeade', 50,
`**Hogsmeade** - the only all-wizarding village in Britain. One main street with the Three Broomsticks, Honeydukes, Zonko's and Wizard Wheezes of the Weasley twins; narrow side streets behind them lead to the Shrieking Shack path and to Borgin and Burkes' back door. At dusk the lamps are lit by hand, and in autumn a damp fog rolls down from the hills.`,
    { filtering: 'Hogsmeade' }),
  entry('Room of Requirement', 55,
`**The Room of Requirement** - a room on the seventh floor that becomes whatever the person who needs it needs. Draconia has spent weeks in it, working on a tall cabinet that must never be found.`,
    { filtering: 'Room of Requirement|Vanishing Cabinet|cabinet', mode: 'REGEX', comment: 'Regex filter, case sensitive.' }),
  entry('Crabbe and Goyle', 60,
`**Vincent Crabbe and Gregory Goyle** - Draconia's two shadows. Large, slow, loyal. She uses them as lookouts and does not trust either with a secret.`,
    { filtering: 'Crabbe|Goyle', mode: 'REGEX' }),
];

export const lorebook = {
  format: 'marginalia-lorebook',
  version: 2,
  root: '11111111-1111-4111-8111-111111111111',
  lorebooks: [{
    uuid: '11111111-1111-4111-8111-111111111111',
    name: 'Harry Potter - Half-Blood Prince',
    enabled: true,
    tags: ['genderbend'],
    subbooks: [],
    entries,
  }],
};

// A second lorebook that is deleted again (the Trash and Cleanup screens need something in the trash).
export const bestiary = {
  format: 'marginalia-lorebook',
  version: 2,
  root: '22222222-2222-4222-8222-222222222222',
  lorebooks: [{
    uuid: '22222222-2222-4222-8222-222222222222',
    name: 'Bestiary',
    enabled: true,
    tags: [],
    subbooks: [],
    entries: [
      entry('Dragon', 10, 'A **dragon** - Hungarian Horntail, Norwegian Ridgeback, Chinese Fireball. Dragons are kept in reserves; keeping one as a pet is against the law.', { filtering: 'dragon', insertion: 'IN_LORE_BLOCK' }),
      entry('Hippogriff', 20, 'A **hippogriff** - half horse, half eagle, proud. Bow first and wait for it to bow back; never insult one.', { filtering: 'hippogriff|Buckbeak', mode: 'REGEX' }),
      entry('Thestral', 30, 'A **thestral** - a winged horse that only those who have seen death can see.', { filtering: 'thestral' }),
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
    { uuid: '33333333-3333-4333-8333-333333333333', name: 'Hogwarts Companion', enabled: true, tags: ['companion'],
      subbooks: ['44444444-4444-4444-8444-444444444444', '55555555-5555-4555-8555-555555555555'],
      entries: [
        entry('Castle', 10, 'Hogwarts is a castle of seven floors and a thousand staircases; the staircases change places when they feel like it.', { filtering: 'castle|Hogwarts', mode: 'REGEX' }),
        entry('House points', 20, 'Points are kept in four giant hourglasses in the entrance hall. Prefects and teachers give and take them.', { filtering: 'points' }),
      ] },
    subbook('44444444-4444-4444-8444-444444444444', 'Hogwarts Staff', ['staff'], [
      entry('Professor Slughorn', 10, '**Horace Slughorn** - Potions master, collects promising students and keeps a corner of his office for them.', { filtering: 'Slughorn' }),
      entry('Madam Pomfrey', 20, '**Poppy Pomfrey** - the matron of the hospital wing; does not like questions about who was hurt and how.', { filtering: 'Pomfrey' }),
      entry('Argus Filch', 30, '**Argus Filch** - the caretaker, with a cat called Mrs Norris and a list of things that are forbidden.', { filtering: 'Filch' }),
    ]),
    subbook('55555555-5555-4555-8555-555555555555', 'Hogsmeade Shops', ['places'], [
      entry('Honeydukes', 10, 'The sweet shop of Hogsmeade; its cellar leads to the castle by a secret passage.', { filtering: 'Honeydukes' }),
      entry('Zonko', 20, 'The joke shop; students buy stink pellets there and teachers pretend not to notice.', { filtering: 'Zonko' }),
      entry('Post Office', 30, 'Owls come and go all day from the post office of the village; Aurors watch it this year.', { filtering: 'post office' }),
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
