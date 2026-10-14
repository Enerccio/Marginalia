// Texts the fake model answers with for the plugins (Side Query, Reviewer, Lore Generator).

export const sideQuery = [
  {
    question: 'Which threads of the plot are still open?',
    answer: `Open threads so far:

1. **Draconia's task** - she cries behind the Three Broomsticks, has been in the Room of Requirement "every night for weeks" and refuses to say why. The story has not shown what she is building (the lorebook says: a tall cabinet).
2. **Harry's promise to Ginny** - he promised to tell her before he does anything stupid; he has now followed Draconia alone and told nobody.
3. **Hermione's theory** - she thinks Draconia has a reason, not an excuse. Harry has not told her what he saw.
4. **The Aurors in Hogsmeade** - mentioned in the first and in the second part, no consequence yet.

Closed: Ron's hiccups.`,
  },
  {
    question: 'What does Draconia know that Harry does not?',
    answer: `From the text and the lorebook: she was given a task she cannot refuse and cannot talk about, she knows what is in the cabinet, and she knows who is watching her (Snape, probably more than one person). Harry only knows that she spends the nights in the Room of Requirement - he says so in the last part - and that she is afraid of being seen. He does not know *why*.`,
  },
];

export const review = `**r/HarryPotterFanfiction** - "[WIP] Half-Blood Prince, but Draco is a girl - chapter 1 feedback?"

**u/fogbound_reader** (+214): The fog and the hiccups in the first part set the tone perfectly. Ginny pulling Harry aside by the sleeve is such a small, true detail.

> **u/lumos_lurker** (+88): Agree, but the Aurors by the post office feel like a drop-in that goes nowhere.
>> **u/fogbound_reader** (+31): Give it a chapter. The setting entry already promises more guards.

**u/dracofan_1996** (+176): Draconia crying behind the Three Broomsticks is the best moment. "The way someone cries who has never been allowed to" - great line.

**u/grammar_goblin** (-4): Too many "she said, very quietly" endings. Also, "Potter" in the last line after Harry offered an olive branch feels cold - was that intentional?

**u/just_a_pun** (+12): Hogs-meade it to the chapter break, I say.

**u/troll_of_the_bridge** (-37): Harry would never follow a Slytherin into an alley. Unrealistic.
> **u/lumos_lurker** (+61): He followed her in book six for a whole chapter.`;

export const loreGeneration = `**The Three Broomsticks** - the inn of Hogsmeade, run by Madam Rosmerta. A low, warm room of dark wood and crooked beams, always smelling of butterbeer and woodsmoke. Students come in on Hogsmeade weekends; by autumn 1996 there are Aurors at the door and the conversations at the corner tables are quieter than they used to be. Behind the inn a narrow lane leads to the back doors of the village - a good place not to be seen, and Draconia knows it.`;

export const loreCompaction = `Three Broomsticks: Hogsmeade inn, Madam Rosmerta. Dark wood, beams, butterbeer, woodsmoke. Student hangout on Hogsmeade weekends; autumn 1996 Aurors at the door, quiet corner tables. Narrow lane behind the inn to the village back doors - unobserved, Draconia uses it.`;

// Arc Outline: the arcs and chapters the flow writes by hand, and what the model answers when it is asked for an arc
// and for the chapters of it. The answers are JSON in the schema the plugin sends ({name, description, note}).
export const arcOutline = {
  // written by hand: arc 1 is finished, arc 2 is the current one
  arcs: [
    {
      title: 'A Foggy Weekend in Hogsmeade',
      description: 'Harry follows Draconia through a foggy Hogsmeade and finds her crying.',
      note: 'Parts 1-5 of the story. Ends with the church bell and Draconia refusing to explain "tonight".',
      state: 'Finished',
      chapters: [
        { title: 'Fog and Hiccups', description: 'The Three Broomsticks, three butterbeers, Ron\'s hiccups.', state: 'Finished' },
        { title: 'Under the Lamp', description: 'Draconia, Crabbe and Goyle mock Harry on the main street.', state: 'Finished' },
        { title: 'Behind the Three Broomsticks', description: 'Harry follows Draconia and finds her crying in the lane.', state: 'Finished' },
      ],
    },
    {
      title: 'The Room of Requirement',
      description: 'Harry tries to learn what Draconia builds in the Room of Requirement.',
      note: 'Harry has promised Ginny to tell her before he does anything stupid. Hermione suspects Draconia has a reason, not an excuse.',
      state: 'Current',
      chapters: [
        { title: 'A Lost Glove', description: 'Harry makes excuses and slips away from the others.', state: 'Finished' },
        { title: 'Night Watch', description: 'Harry waits outside the seventh floor corridor with the Marauder\'s Map.', state: 'Current' },
        { title: 'The Door That Was Not There', description: 'The wall opens for Draconia and closes again before Harry can look inside.', state: 'Planned' },
      ],
    },
  ],

  // the third arc is written by the model
  generateArc: {
    specification: 'After the Room of Requirement: Ginny and Hermione work out what the cabinet does, while Snape starts to watch Draconia. End on a cliffhanger.',
    reasoning: `The author wants the arc to **follow** "The Room of Requirement" and to move the story from Harry's watching to the girls' investigation.

- Ginny and Hermione need a way in: the Marauder's Map is Harry's, so they follow clues in the library.
- Snape is the pressure from outside; he must not reveal what he knows.
- Cliffhanger: the cabinet answers from the other side.`,
    answer: {
      name: 'The Vanishing Cabinet',
      description: 'Ginny and Hermione find out what the cabinet is for while Snape starts to watch Draconia. It ends when the cabinet answers.',
      note: `Premise: Harry's night watch gets him nowhere, so Ginny and Hermione take over. Hermione traces the dust-sheeted cabinet to a pair of Vanishing Cabinets, one in the castle and one far away; Ginny keeps Harry honest about his promise.

Conflict: the girls need to know what Draconia is building without being seen by her, by Filch or by Snape, who starts to turn up wherever Draconia has just been.

Turning points: the library book with the page torn out; the candle wax on the seventh floor; Snape finding Ginny on the stairs.

Ending: the cabinet in the room hums, a knock comes from inside it, and Draconia, standing in the doorway, is the only one who is not surprised.`,
    },
  },

  // the chapters of that arc
  generateChapters: {
    general: 'Slow and cold, one scene per chapter. Ginny is the point of view of the first one. No fights.',
    count: 3,
    specs: ['Ginny finds the room by accident, following a trail of candle wax.', '', 'End on Snape, not on the cabinet.'],
    reasoning: `The arc has three beats: the girls find the room, learn what the cabinet is, and are caught.

1. **Chapter 1** follows the author's wish: Ginny, the candle wax, the dust sheets.
2. **Chapter 2** is free: the library is the natural place for Hermione's explanation.
3. **Chapter 3** has to end on Snape, so the cabinet's hum is only background.`,
    answer: {
      chapters: [
        {
          name: 'Dust Sheets',
          description: 'Ginny follows a trail of candle wax to the seventh floor and finds the Room of Requirement open, with a cabinet half hidden under dust sheets.',
          note: 'Ginny is the point of view. She has left Harry on his night watch because he is looking at the wrong corridor. The wax leads her past the troll tapestry to a door that was not there a minute ago. The room is cold, full of broken things, and the cabinet in the middle is the only object that has been cleaned. She does not touch it; she counts the footprints in the dust, which are all the same pair.',
        },
        {
          name: 'What the Books Say',
          description: 'Hermione traces the cabinet to a pair of Vanishing Cabinets, one in the castle and one far away.',
          note: 'The library at night. Hermione brings a book with a page torn out and works out what was on it from the gap in the index. Ginny describes the carvings; Hermione recognises the maker\'s mark. They agree not to tell Harry yet, because he would go straight to Draconia. Madam Pince finds them and they leave with the book hidden under a jumper.',
        },
        {
          name: 'Snape at the Door',
          description: 'Snape catches the girls on the stairs; Draconia watches from above with the cabinet humming behind her.',
          note: 'The girls come down from the seventh floor and meet Snape on the landing. He asks nothing, which is worse. Ginny lies badly; Hermione lies well, and he lets her. Above them, in the open door of the room, Draconia stands with the hum of the cabinet behind her. Snape does not look up. The chapter ends on that: he knows exactly where she is.',
        },
      ],
    },
  },
};
