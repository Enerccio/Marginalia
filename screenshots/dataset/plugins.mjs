// Texts the fake model answers with for the plugins (Side Query, Reviewer, Lore Generator, Arc Outline, Beta Readers).

export const sideQuery = [
  {
    question: 'Which threads of the plot are still open?',
    answer: `Open threads so far:

1. **The archway** - Mizuki says in Japanese that the wall is breathing, and the next part wakes them in the reeds. The story has not shown what the arch is (the lorebook says only that it was bricked up before anyone wrote down why).
2. **Tomáš's secret** - he understood her first mutter and pretended not to. Since part 4 she knows he speaks Japanese; she does not know why he hid it.
3. **The language** - nobody they will meet speaks English or Japanese. The first word of Vrakht is still to come.
4. **The lantern on the river** - mentioned at the end of part 5 as a promise, no one has answered it yet.

Closed: the phone has no signal (58 percent, nothing to be done).`,
  },
  {
    question: 'What does Tomáš know that Mizuki does not?',
    answer: `From the text and the lorebook: he understood every word of what she said at the arch (the wall is breathing) and, in the reeds, of what she said to herself. He also knows where he learned his Japanese (novels and subtitles) and how stiff it sounds. Mizuki knows, since part 4, that he speaks Japanese; she does not know that he heard her at the arch, and has not asked why he hid it.`,
  },
];

export const review = `**r/PortalFantasy** - "[WIP] Unregistered - a Czech guide and a Japanese nurse fall through a wall - chapter 1 feedback?"

**u/reedwalker** (+214): The cellar closing time is a perfect first scene. Counting the tourists out like heads, then coats, then the iron ring - such a small, true detail about a guide.

> **u/lantern_lurker** (+88): Agree, but the breathing wall happens very fast. I would have liked one more paragraph in the cellar.
>> **u/reedwalker** (+31): The next part answers it: she is not looking at the wall, she is looking at him.

**u/kanji_and_kvas** (+176): The line where he understands her murmur and answers in English that it is only cold air - great. You can feel him choosing not to flinch.

**u/grammar_goblin** (-4): Too many "she said, very quietly" endings. Also, "Is it old?" is a nice question and then nobody answers it. Was that intentional?

**u/just_a_pun** (+12): I would have bricked up that arch myself. Good night.

**u/troll_of_the_bridge** (-37): A tour guide who learned Japanese from novels? Unrealistic.
> **u/lantern_lurker** (+61): Have you met a tour guide.`;

export const loreGeneration = `**The Bridge of Silvakhor** - the stone bridge over the Lyrasil, the only crossing between the market village and the fort. Two soldiers watch it day and night; Paskal is one of them in the first weeks of the story. The toll is paid in tally tokens, never in coin, and the ledger house stands at the far end so that nobody crosses without being written down. The river sounds different under the arches: slower, almost like applause, and the villagers say it is the weir answering from upstream.`;

export const loreCompaction = `Bridge of Silvakhor: stone bridge over the Lyrasil between market village and fort. Two soldiers day and night (Paskal early on). Toll in tally tokens, never coin. Ledger house at the far end: everyone crossing is written down. River sounds slower under the arches, like applause (the weir).`;

// Arc Outline: the arcs and chapters the flow writes by hand, and what the model answers when it is asked for an arc
// and for the chapters of it. The answers are JSON in the schema the plugin sends ({name, description, note}).
export const arcOutline = {
  // arc 1 is imported from the story (importArc), arc 2 is written by hand and is the current one
  arcs: [
    {
      title: 'The Lantern on the River',
      description: 'Tomáš and Mizuki follow the river until somebody answers the light.',
      note: 'Tomáš hides that he understands Japanese; Mizuki has found out and has not asked why. Neither of them speaks a word of the language of the valley.',
      state: 'Current',
      chapters: [
        { title: 'Dry Ground', description: 'They leave the reeds and find a bank that holds their weight.', state: 'Finished' },
        {
          title: 'The Lantern',
          description: 'A light on the water, and nobody who will answer a shout in English or Japanese.',
          note: 'Mizuki wants to call out; Tomáš argues for waiting, in Japanese, because he is afraid of what a shout sounds like in a language that nobody here knows. The lantern swings once, twice, and then turns toward them. Whoever is under it does not shout back; they start to row.',
          state: 'Current',
        },
        { title: 'The Ferry-woman', description: 'Saellen shouts the first Vrakht they hear, and it needs no translation.', state: 'Planned' },
      ],
    },
  ],

  // arc 1 is written by the model from the five parts of the story (Import from story), in three chapters
  importArc: {
    chapters: 3,
    reasoning: `The five parts of the story fall into three scenes, so the chapters follow them.

1. **The Bricked Arch**: the cellar, the closing time and the wall that breathes (parts 1-2).
2. **Counting Things**: the reed fields, phone, notebook, headlamp, and the first swearing in Czech (part 3).
3. **Go Home**: Mizuki slips and the Japanese gets out; the isekai talk on the river bank (parts 4-5).`,
    answer: {
      name: 'Through the Wall',
      description: 'A cellar guide and a nurse fall through a bricked arch and learn that they share a language neither of them expected.',
      note: `Premise: the evening tour in a Prague cellar ends with a bricked archway that breathes. Tomáš counts the tourists out; Mizuki stays behind.

Conflict: they wake in a reed field under an enormous moon with no signal. Tomáš understands her Japanese and hides it, which is a lie he will have to explain.

Ending: the lie comes out in the mud, in Japanese, and the two of them start along the river together.`,
      chapters: [
        {
          name: 'The Bricked Arch',
          part: 1,
          description: 'The last tourists, the iron ring, and a wall that breathes.',
          note: 'Tomáš counts the tourists out and bolts the door at nine. Mizuki lingers at the bricked arch, asks whether it is old, and murmurs in Japanese that it is breathing. He understands and answers in English that it is only cold air in the cracks.',
        },
        {
          name: 'Counting Things',
          part: 3,
          description: 'Phone, notebook, headlamp: what they have in the reeds.',
          note: 'Tomáš counts hands, legs, a cracked headlamp and a phone with fifty-eight percent and no signal. Mizuki opens her bag: a first-aid kit, tissues, a roll of bandage. He swears in Czech, and decides to follow the river downstream.',
        },
        {
          name: 'Go Home',
          part: 4,
          description: 'She slips and says it in Japanese; he answers before he can stop himself.',
          note: 'Mizuki falls in a hidden hollow and says in Japanese that she wants to go home. Tomáš answers in Japanese, stiff and bookish. Her shock, his embarrassment: the first real conversation, and the beginning of the walk along the bank.',
        },
      ],
    },
  },

  // the third arc is written by the model
  generateArc: {
    specification: 'After the river bank: they reach the ferry and spend the first night in the village; Steing arrives in the morning. End on a cliffhanger.',
    reasoning: `The author wants the arc to **follow** "The Lantern on the River" and to move the story from the walk to the first contact with the Dominion.

- The ferry is the first place where the language matters: Saellen speaks, they do not understand.
- The village needs a night: the Lyrshara stare, a child touches a sleeve, soldiers watch the bridge.
- Cliffhanger: the assessor with a ledger, and the first sentence read out in Vrakht.`,
    answer: {
      name: 'The Ledger',
      description: 'Saellen takes them across, the village watches, and in the morning a thin man with a ledger reads out a sentence they cannot understand.',
      note: `Premise: the lantern on the river belongs to a ferry. Saellen feeds them and teaches them two words; the river song she hums is in another language than the one she shouts in.

Conflict: the village of Silvakhor stares, the soldiers at the bridge watch, and Tomáš writes sounds into his notebook as fast as he hears them. Mizuki cannot sleep.

Turning points: the first word (mosh, food); the child who touches her sleeve; the soldiers at the bridge.

Ending: morning, a yard, two soldiers, and a thin man with a ledger under his arm who reads out one long sentence and writes it down.`,
    },
  },

  // the chapters of that arc
  generateChapters: {
    general: 'Slow and warm, one scene per chapter. Mizuki is the point of view of the first one. No fights.',
    count: 3,
    specs: ['Mizuki on the ferry: the first word, the first meal.', '', 'End on Steing, not on the soldiers.'],
    reasoning: `The arc has three beats: the ferry, the night in the village, and the morning with the ledger.

1. **Chapter 1** follows the author's wish: Mizuki, the ferry, food and a word.
2. **Chapter 2** is free: the village at night is the natural place for the stares and the child.
3. **Chapter 3** has to end on Steing, so the soldiers stay in the background.`,
    answer: {
      chapters: [
        {
          name: 'Mosh',
          description: 'Saellen feeds them on the ferry and teaches them the first word of Vrakht.',
          note: 'Mizuki is the point of view. The ferry is flat and black, the lantern swings on its pole, and the broad woman who shouted at them puts a bowl in her hands. Saellen says one word, mosh, with a hand to the mouth, and watches until Mizuki says it back. Tomáš writes it down in the notebook the water has not ruined. Nobody explains anything; everybody eats.',
        },
        {
          name: 'The Lyrshara',
          description: 'Night in Silvakhor: the stares, a child, a dog and the soldiers at the bridge.',
          note: 'The village at night. The people the lorebook calls the Lyrshara look at them without speaking; a child touches Mizuki\'s sleeve and a dog follows them to Saellen\'s sister\'s byre. The soldiers on the bridge watch them cross. Mizuki cannot sleep and whispers Japanese to the dark; Tomáš, on the other side of the straw, answers once and then pretends to be asleep.',
        },
        {
          name: 'A Sentence in Vrakht',
          description: 'Morning in the yard: soldiers, a ledger, and a thin man who reads out a sentence that nobody explains.',
          note: 'Morning, the yard, two soldiers with spears, and Paskal among the escort. A thin man with a ledger under his arm stops at the gate: Steing. He reads out a long sentence in Vrakht, with the Hoh tone that Tomáš can hear and cannot decode, and writes something down. The chapter ends on that: he has written them into the ledger, and they have not said a word.',
        },
      ],
    },
  },
};

// Beta Readers: what the personas answer to the questions of the flow. `answer` is what the plugin asks for ({reasoning, messages: [{speaker,
// parts: [{type, text}]}]}); the fake model writes it as JSON, or as `**Name:** *action* speech` paragraphs when the chat wants free text.
const a = text => ({ type: 'action', text });
const s = text => ({ type: 'speech', text });
export const betaPersona = {
  brief: 'A reader who only cares about the language and the logic of the world',
  name: 'Rook',
  description: 'The Linguist. Reads for the languages and keeps a list of every word the characters learn. She notices every translation that is too convenient, likes a fair misunderstanding and cannot stand a foreign language that is only English with the letters moved.',
};

export const betaReaders = [
  {
    kind: 'group',
    question: 'Be honest: does the first chapter hook you?',
    answer: {
      reasoning: 'Athena reads the structure and likes it, Vesper objects to the pace, Athena answers her.',
      messages: [
        { speaker: 'Athena', parts: [a('folds her hands over the pages'), s('The closing time in the cellar is a quiet, confident opening. It promises that something is wrong with the wall before anyone says so.')] },
        { speaker: 'Vesper', parts: [a('snorts'), s('Quiet is one word for it. I counted three paragraphs before anybody does anything. I would cut the tourists.')] },
        { speaker: 'Narrator', parts: [a('Athena raises an eyebrow, but for once she does not argue straight away.')] },
        { speaker: 'Athena', parts: [s('The tourists are the point. They are the last normal thing he counts.')] },
      ],
    },
  },
  {
    kind: 'group',
    question: 'Which part should I rewrite first?',
    answer: {
      reasoning: 'Both agree on one thing for once.',
      messages: [
        { speaker: 'Vesper', parts: [s('The isekai speech on the river bank. He explains the whole genre to a woman who is trying to survive, and nobody has asked.')] },
        { speaker: 'Athena', parts: [a('nods slowly'), s('She is right. Let him look for the status window and say nothing about it.')] },
      ],
    },
  },
  {
    kind: 'single',
    question: 'Read it line by line: what bothers you?',
    answer: {
      reasoning: 'One persona, so one voice: the editor goes through the text.',
      messages: [
        { speaker: 'Quill', parts: [a('uncaps a red pen'), s('Three "suddenly"s in the second part, and a dialogue tag that says she "exclaimed" a whisper. The moon is described as huge four times in the first page; twice is enough.')] },
      ],
    },
  },
  {
    // sent as a scene (Send as scene): what happens in the room, and the personas react
    kind: 'group',
    question: 'The lights go out and the rain on the windows gets louder.',
    answer: {
      reasoning: 'A scene, not a question: the personas react to the dark and go back to the book.',
      messages: [
        { speaker: 'Vesper', parts: [a('lowers her voice'), s('Finally something happens in this room. Keep going, I want to hear what the lantern does in your second chapter.')] },
        { speaker: 'Athena', parts: [a('lights the candle on the table'), s('The power cut suits the book: the cellar hides things and so does the dark. Read us the scene by the lamp.')] },
      ],
    },
  },
];
