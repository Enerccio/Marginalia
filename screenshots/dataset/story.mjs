// The story of the demo book. The flow types `scene`, `pov`, `present`, `instructions` into the New Turn panel;
// the fake inference server (llm/rules.mjs) recognizes the turn by its instructions and answers with `reply`.
export const BOOK = 'Harry Potter and Half Blood Prince - FDM';

export const turns = {
  t1: {
    scene: 'Hogsmeade evening, main street',
    pov: 'Harry',
    present: 'Harry, Hermione, Ron, Ginny',
    instructions: 'Harry, Ron and Hermione walk out of the Three Broomsticks after a few butterbeers, Ron hiccuping loudly. Ginny catches up with them and pulls Harry aside.',
    reply: `The Three Broomsticks let them out into the cold with a gust of warm air and the smell of butterbeer. Dusk had turned the village the colour of a bruise. Along the main street the lamps were being lit one by one, and a thin fog crept down from the hills to meet them.

"Hic -" Ron stopped in the middle of the road, one hand pressed to his chest, and looked offended at his own body. "Sorry. 'Mione, s'not funny."

"I didn't say anything," Hermione said, though her mouth was twitching. "Three butterbeers, Ronald. Three. You were warned."

Harry laughed and hooked an arm under Ron's elbow to steer him away from a lamp post. It felt good, for a minute, to be sixteen and walking home with his friends and nothing worse to worry about than a hiccup. Then he noticed the two Aurors by the post office, watching every student who passed, and the feeling went out of him like air from a punctured ball.

"Harry!" Quick footsteps behind them. Ginny, her hair darker with fog, her scarf already half off. She did not slow down; she caught his sleeve and pulled him two steps aside, out of earshot of the others. "You promised you would tell me. Before you did anything stupid."

"I haven't done anything," Harry said.

"Yet," said Ginny, and held on to his sleeve a little harder than she needed to.`,
    summary: `[Location] Hogsmeade main street, dusk, fog from the hills, Aurors by the post office.
[Ron Weasley] leaves the Three Broomsticks after three butterbeers -> persistent hiccups.
[Hermione Granger] teases Ron, mild reprimand.
[Harry Potter] steers Ron, notices the Aurors -> mood drops.
[Ginny Weasley] catches up, pulls Harry aside -> reminds him of his promise to tell her before he does anything stupid.`,
  },
  t2a: {
    scene: 'Hogsmeade evening, main street',
    pov: 'Harry',
    present: 'Harry, Hermione, Ron, Ginny, Draconia, Crabbe, Goyle',
    instructions: 'Draconia insults Harry, calling him a show-off for letting a girl pull him around in public. Ginny joins the argument, defending him.',
    reply: `A cold voice cut through the fog. "How touching. Does the Boy Who Lived hold hands with every Weasley, or is there a rota?"

Draconia Malfoy stood under the nearest lamp as if she had been placed there, hands in the pockets of a long green coat, platinum hair pinned back so tightly it looked painful. Behind her Crabbe and Goyle studied the cobbles. She smiled with only half of her face, and Harry thought, not for the first time, that she had been getting thinner all autumn.

"Get lost, Malfoy," Ron said, with great dignity and a hiccup.

"Language, Weasley. There are Aurors." Her grey eyes went back to Harry. "Though I understand you have a taste for public displays. Hogsmeade, a Saturday, a girl towing you around by the sleeve - I'm told that is what you call a good time."

Ginny let go of his sleeve and turned on her heel, so fast that her scarf swung out like a whip. "He doesn't need to explain himself to you. Neither do I. Go back to wherever you're hiding, Malfoy, and take your bodyguards with you."

"Hiding?" For a heartbeat something moved behind Draconia's face, quick and ugly, and was gone. "How little you know."

Harry stepped between them before it could turn into a wand fight in the middle of the main street. He did not like that look. It was not the look of someone who wanted to win an argument.`,
    summary: `[Draconia Malfoy] appears under a lamp with Crabbe and Goyle -> mocks Harry for "public displays".
[Ron Weasley] tells her to get lost (hiccup).
[Ginny Weasley] lets go of Harry's sleeve -> defends him, tells Draconia to go back to hiding.
[Draconia Malfoy] reacts sharply to the word "hiding" -> "How little you know."
[Harry Potter] steps between them, notices that she does not look like someone who wants to win an argument.`,
  },
  // the other version of t2a, written by Swipe: same turn details, so the fake server gets it through its queue
  t2b: {
    scene: 'Hogsmeade evening, main street',
    pov: 'Harry',
    present: 'Harry, Hermione, Ron, Ginny, Draconia, Crabbe, Goyle',
    instructions: 'Draconia insults Harry, calling him a show-off for letting a girl pull him around in public. Ginny joins the argument, defending him.',
    reply: `Draconia's drawl came out of the fog before she did. "Potter. Out on a Saturday night with half of Gryffindor's nursery. Do they let you stay up this late?"

Ginny's hand fell from Harry's sleeve. Her mouth set into a thin line, and she looked at the cobbles as though she had decided that Malfoy was not worth the effort. It would have been unlike her any other week.

Hermione stepped forward instead, her prefect badge catching the lamplight. "Draconia. The Aurors are twenty yards away and none of us has done anything to you. Go home."

"Or what, Granger? You'll deduct points?" She was smiling, but it did not reach her eyes, and when she looked past Hermione at Harry the smile slipped. For one moment she looked simply tired. Then she turned on her heel and the fog closed behind her.`,
    summary: `[Draconia Malfoy] teases Harry for being out late with "Gryffindor's nursery".
[Ginny Weasley] says nothing, drops Harry's sleeve.
[Hermione Granger] steps in, points at the Aurors, tells Draconia to go home.
[Draconia Malfoy] answers with a thin smile, looks tired when she sees Harry -> leaves into the fog.`,
  },
  t3: {
    scene: 'Hogsmeade evening, main street',
    pov: 'Harry',
    present: 'Harry, Hermione, Ron (tipsy), Ginny',
    instructions: 'Ron wants to discuss what just happened. Hermione wonders if Draconia might have a reason to be in Hogsmeade tonight. Harry watches the fog behind them.',
    reply: `They walked the rest of the way to the castle path in a bunch, Ron propped between Harry and Hermione and still hiccuping at intervals.

"I'm just saying," Ron said, for the fourth time, "she's up to something. She's always up to something, but now she's *really* up to something."

"That is almost an insight," said Hermione. "Don't spoil it by repeating it."

Harry said nothing. He was watching the fog behind them, where a slim figure in a green coat had gone into one of the side streets without Crabbe or Goyle. Alone. At night. In a village full of Aurors that her family had every reason to avoid.

"Think about it," Hermione went on, lower. "Why would Draconia be in Hogsmeade at all? She hates the village. She says the shops are common. And look at her, Harry, she's lost weight, she doesn't sleep - it was in her face just now. Whatever she is doing, she is not doing it because she wants to."

"You're feeling sorry for her?" Ron gaped.

"I'm saying she might have a reason," said Hermione. "That isn't the same thing."

Ginny had fallen back a little. She was looking at Harry, and he knew that she had seen exactly where he was looking.`,
    summary: `[Ron Weasley] insists that Draconia is "really" up to something.
[Hermione Granger] notes that Draconia hates the village, has lost weight and does not sleep -> suggests she has a reason, not an excuse.
[Harry Potter] watches a figure in a green coat enter a side street alone.
[Ginny Weasley] falls back, sees where Harry is looking.`,
  },
  t4: {
    scene: 'Hogsmeade evening, side streets behind the Three Broomsticks',
    pov: 'Harry',
    present: 'Harry; Draconia',
    instructions: 'Harry slips away from his friends, saying he lost a glove, and follows Draconia into the side streets until he is behind the Three Broomsticks.',
    reply: `It was easy to lose them. Ron wanted to sit down on the verge and Hermione was occupied with that, and Ginny had gone ahead without a word. Harry said he had dropped his glove, that he would only be a minute, and turned back before anyone could ask what glove.

The fog had thickened. The sound of the village shrank behind him until it was only the drip of water from the eaves and his own footsteps. He took the first side street where she had gone, then a second, narrower, between a blacksmith's yard and the blind back of a shop. Somewhere ahead a door closed.

He slowed. Through the mist he saw a line of lamplight under a doorway and a slender shadow in front of it. Draconia had not gone to Borgin and Burkes, or to any of the shops. She stood with her back against the brick behind the Three Broomsticks, one hand over her mouth, shoulders rising and falling as if she had been running.

She was crying, Harry realised. Quietly, furiously, the way someone cries who has never been allowed to.

He had one step still to take to leave the shadow, and no idea what he would say when he did.`,
    summary: `[Harry Potter] excuses himself ("lost a glove") -> follows Draconia through two side streets, fog thickening.
[Environment] Drip of water, muffled village sounds, a door closing ahead.
[Draconia Malfoy] found behind the Three Broomsticks, hand over her mouth, crying silently.
[Harry Potter] unseen, one step from the shadow, has no plan.`,
  },
  t5: {
    scene: 'Hogsmeade evening, behind the Three Broomsticks',
    pov: 'Harry',
    present: 'Harry; Draconia',
    instructions: 'Draconia notices Harry and raises her wand, afraid that someone will see them. Harry asks what she is doing in the Room of Requirement. She refuses to answer.',
    reply: `He must have made a sound, because her head came up and her wand was in her hand before he could speak.

"Stop it," she hissed, though he had not moved. Her mercury-grey eyes darted left and right along the empty stretch of the lane. "Someone will see us, you idiot."

"No one is here." Harry kept his hands open and slightly away from his sides. "Draconia. What are you doing?"

"Walking. It's a free village." She wiped her face with the back of her wrist, a quick angry gesture, as though she could take the evidence off like dirt. "Go back to your friends."

"You've been in the Room of Requirement every night for weeks. I've seen the map." He watched that land. Her wand arm shook, only slightly. "Whatever it is -"

"You don't know what it is." Her voice dropped, and with it all the mockery. "You don't know anything. Do you think I'd be out here if I had a choice?"

"Then tell me what the choice is."

For a long moment she looked at him as if she wanted to. Then the church bell down in the valley rang the hour, and she flinched, and the wand dropped to her side. "Not tonight, Potter," she said, very quietly. "And if you follow me again, I won't be the one who regrets it."`,
    summary: `[Draconia Malfoy] hears Harry -> draws her wand, hisses that someone will see them.
[Harry Potter] hands open, asks what she is doing; mentions the Room of Requirement and the map.
[Draconia Malfoy] denies, says he knows nothing; the church bell makes her flinch -> lowers the wand, refuses to explain "tonight", warns him not to follow her again.`,
  },
  // a branch from t4 written with the reasoning model (Local Reasoner): its reasoning is shown with the part
  t5alt: {
    scene: 'Hogsmeade evening, behind the Three Broomsticks',
    pov: 'Harry',
    present: 'Harry; Draconia',
    instructions: 'Draconia notices Harry following her and loses her temper. She pushes him against the wall and demands to know who sent him; Harry has to convince her that nobody did.',
    reasoning: `Harry's point of view, behind the Three Broomsticks, fog, evening.
Beats: 1. She hears him and reacts with force, not with words. 2. She is afraid of being watched, so the threat must be quiet. 3. Harry stays calm and does not draw his wand. 4. She asks who sent him - this tells us she expects to be followed. 5. End on her doubt, not on a fight.
Keep her sharp but visibly shaking: the hand with the wand trembles, her breath is uneven. Do not reveal the task. No violence beyond the push.`,
    reply: `She had her wand under his chin before he finished stepping out of the shadow, and the brick was cold through his robes where she had slammed him against it.

"Who sent you?" Draconia's voice was low and even, which was worse than shouting. "Snape? Dumbledore? Did they tell you to follow me?"

"Nobody sent me." Harry kept very still. The tip of the wand shook against his throat, only a little, but he could feel it. "I saw you leave. That's all."

"That's all." She laughed, a breath without any sound in it. "You expect me to believe that Harry Potter just happened to be walking behind the Three Broomsticks in the fog."

"I expect you to look at me," Harry said. "If someone had sent me, I wouldn't be standing here without my wand."

For a moment her grey eyes moved over his face as if she were reading something written in small letters. Then the wand dropped, very slowly, and she took one step back. She did not look relieved. She looked like someone who had just found out how alone she was.`,
  },
};

// The edit that makes "Highlight Generated Text" visible: the user rewrites one line of the last part.
export const edit = {
  turn: 't5',
  from: '"Then tell me what the choice is."',
  to: '"Then tell me what the choice is," Harry said, and for the first time he meant it as an offer, not a challenge.',
};

// What the model answers to "Create meta summary" for the summaries of t3 and t5.
export const metaSummary = `[Location] Hogsmeade, main street at dusk (fog, Aurors by the post office) -> the side streets behind the Three Broomsticks.
* Ron leaves the Three Broomsticks after three butterbeers (hiccups) -> Hermione teases him; Ginny catches up and pulls Harry aside, reminding him of his promise to tell her before he does anything stupid.
* Draconia appears with Crabbe and Goyle and mocks Harry's "public displays" -> Ron and Ginny answer; "hiding" touches a nerve -> Hermione later points out that she has lost weight and does not sleep: she has a reason, not an excuse.
* Harry excuses himself ("lost a glove") and follows Draconia through two side streets -> finds her crying behind the Three Broomsticks -> she draws her wand, forbids him to follow, refuses to explain "tonight" when the church bell rings.`;

export const summaryReasoning = 'Reading the passage beat by beat, keeping who acts, what changes and what the next part must remember.';

// The regular user (ginny) uses the shared provider once, so that usage and limits have numbers.
export const ginnyBook = 'Quidditch Tryouts';
export const ginnyTurn = {
  scene: 'The Quidditch pitch, early morning',
  pov: 'Ginny',
  present: 'Ginny; a first-year in the stands',
  instructions: 'Ginny practises alone on the pitch before breakfast. A first-year watches from the stands and does not dare to say a word.',
  reply: `The grass was still grey with frost when Ginny kicked off, and the whole pitch smelled of cold earth. She flew three laps with the Quaffle tucked under one arm, then let herself drop, so low that her toes brushed the grass, and pulled up again with a laugh she had not meant to let out.

Somebody in the stands gasped. A first-year, a thin boy with a scarf wound twice around his neck, was sitting in the very last row with his knees drawn up, trying to look as if he had not been watching.

"You can come down, you know," Ginny called. "I don't bite. Not before breakfast."`,
};

// Her second part, written after the limit was set, so that the limit dialog shows what she used.
export const ginnyTurn2 = {
  scene: 'The Quidditch pitch, early morning',
  pov: 'Ginny',
  present: 'Ginny; the first-year',
  instructions: 'The first-year climbs down and admits he wants to try out for Seeker. Ginny hands him a broom and tells him to catch the Snitch before breakfast.',
  reply: `The boy came down the steps one at a time, as if the stairs might change their minds. Up close he was even smaller than he had looked from the stands, and his ears were red from the cold.

"I want to try out," he said, very fast, before he could lose his nerve. "For Seeker. I know I'm only a first-year. I know it's silly."

"It's not silly. It's early." Ginny pushed her own broom into his hands and fished the Snitch out of her pocket, a tiny golden flicker that fought her fingers. "Rule one: you don't talk about it, you catch it. You have until the first bell for breakfast."

He stared at the broom, then at the sky, which was turning from grey to pink above the goalposts. Then he kicked off so hard that he nearly fell off backwards, and Ginny laughed until her sides hurt.`,
};
