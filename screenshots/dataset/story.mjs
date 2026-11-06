// The story of the demo book "Unregistered" (a Czech cellar guide and a Japanese nurse fall through a bricked archway into the valley of the
// Lyrasil). The first five parts are the first parts of the dataset book of the tests (dataset/unregistered-parts.json, made by
// dataset/from-fixture.py); the swipe, the branch of the reasoning model, the summaries and the book of the second user are written for the
// pictures. The flow types `scene`, `pov`, `present`, `instructions` into the New Turn panel; the fake inference server (llm/rules.mjs)
// recognizes the turn by its instructions and answers with `reply`.
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const parts = JSON.parse(fs.readFileSync(path.join(here, 'unregistered-parts.json'), 'utf8'));

export const BOOK = 'Unregistered';
export const TAGS = ['portal fantasy', 'language barrier', 'slow burn'];
export const LOREBOOK = 'Unregistered - the valley of the Lyrasil';
export const DESCRIPTION = 'A Czech cellar guide who learned Japanese from novels and a Japanese nurse on holiday step through a bricked archway in a Prague cellar and wake in a river valley held by a Vrakht-speaking empire. Neither of them knows the language.';

export const turns = {
  t1: {
    ...parts[0],
    summary: `[LOCATION / SCENE: Prague / evening cellar tour]
* Tomáš counts the last eleven tourists out and bolts the door at nine o'clock -> he is alone in the cellar with Mizuki.
* Mizuki stays at the bricked archway and asks whether the wall is old -> he answers fourteenth century at best, bricks newer, nobody knows who closed it.
* She lays her palm on the brick and murmurs in Japanese that it is breathing -> Tomáš understands every word and answers in English that it is only cold air in the cracks.`,
  },
  t2a: {
    ...parts[1],
    summary: `[LOCATION / SCENE: Reed fields / night, a huge low moon]
* Mizuki wakes face down in wet reeds -> no arch, no cellar, only black water and a swollen moon.
* She finds Tomáš unconscious with a slow pulse, wakes him -> they speak broken English.
* She asks in Japanese where they are -> Tomáš does not react to the Japanese and answers carefully in English.`,
  },
  // the other version of t2a, written by Swipe: same turn details, so the fake server gets it through its queue
  t2b: {
    ...parts[1],
    reply: `Water in her mouth. Mizuki came up coughing, one cheek in cold mud, and for a long moment she did not know whether she was on the ward floor or in a dream of it.

Then the smell reached her, green and rotten and alive, and she opened her eyes on reeds. Reeds in every direction, taller than she was, black against a moon that hung so low and so large that she thought for a second it was a lamp somebody had forgotten to switch off.

"Tomáš," she said in Japanese, very quietly, as if the reeds might hear. "Tomáš, where are we?"

He lay a few steps away with his headlamp still strapped to his wrist, its beam pointed at nothing. She crawled to him and counted his pulse against her second hand out of habit: slow, steady, rude in its calm. When he groaned and opened his eyes, she switched to English without deciding to, and every word came out smaller than she meant.

"Not the cellar," she said. "Please. Tell me this is also the cellar."`,
    summary: `[Mizuki] wakes in reeds under an enormous moon -> asks in Japanese where they are, finds Tomáš.
[Tomáš] unconscious, pulse slow and steady -> wakes, she switches to English: "Tell me this is also the cellar."`,
  },
  t3: {
    ...parts[2],
    summary: `[LOCATION / SCENE: Reed fields / night]
* Tomáš takes stock: phone at 58 percent without signal, headlamp, notebook, water bottle; her bag with a first-aid kit and a phone.
* He thinks and swears in Czech, still hides that he understood her mutter -> decides to follow the river downstream.`,
  },
  t4: {
    ...parts[3],
    summary: `[LOCATION / SCENE: Reed fields / night]
* Mizuki slips in the mud and says in Japanese that she wants to go home -> Tomáš answers in Japanese before he can stop himself.
* Her shock, his embarrassment -> first real conversation: her Japanese is natural, his is stiff and bookish.`,
  },
  t5: {
    ...parts[4],
    summary: `[LOCATION / SCENE: River bank / night]
* Tomáš explains too eagerly what an isekai is and looks for a status window and a summoner -> finds neither.
* Mizuki teases his stiff Japanese and asks where he learned it -> he blames novels and subtitles.
* She decides the order: water, shelter, people -> they walk along the river.`,
  },
  // a branch from t4 written with the reasoning model (Local Reasoner): its reasoning is shown with the part
  t5alt: {
    scene: 'Reed fields, a stretch of dry ground',
    pov: 'Tomáš',
    present: 'Tomáš, Mizuki',
    instructions: 'Mizuki stops walking and asks why he pretended not to understand her in the first place. Tomáš has to answer without making excuses, and she decides whether she believes him.',
    reasoning: `Tomáš's point of view, a dry patch between the reeds, night, a moon that is too large.
Beats: 1. She stops and does not look at him. 2. The question is short and in Japanese, so he cannot hide in English. 3. He tells the truth in the clumsy way he speaks it: he was afraid that a stranger who understood her would sound like a threat. 4. She is angry, but she is also a nurse, so she checks the fact before the feeling. 5. End on a small decision, not a reconciliation.
Keep the Japanese stiff on his side. Do not let anyone cry. No explanation of the wall.`,
    reply: `Mizuki stopped on the last dry tussock and did not turn around. The reeds went on whispering around them, and the moon laid a long, broken road across the water.

"Why," she said in Japanese. Only that.

Tomáš had practised answers to a great many questions in the last hour, but not this one. "You were afraid," he said, slowly, choosing each word as if it might break. "A stranger who understands you, in a place like this - I thought it would sound like a threat. I was wrong. I am sorry."

"You let me talk to myself like a fool." Her voice was low and even, which was worse than shouting. "You watched me be afraid and said nothing."

"Yes."

She turned then, and looked at him in the thin yellow beam of the lamp, the way she must have looked at patients who said they felt fine. He did not look away. After a while something in her shoulders let go, only a little.

"Next time you hide something," she said, "hide it in Czech. I will not understand that, and it will be honest at least."

She stepped onto the next tussock, and he followed her.`,
  },
};

// The edit that makes "Highlight Generated Text" visible: the user rewrites one line of the last part.
export const edit = {
  turn: 't5',
  from: 'He winced. "I know."',
  to: 'He winced. "I know," he said in Czech, because it was the only language in which he could admit it.',
};

// What the model answers to "Create meta summary" for the summaries of t3 and t5.
export const metaSummary = `[LOCATION / SCENE: Reed fields -> the river bank / night under an enormous moon]
* Tomáš and Mizuki step through a bricked archway in a Prague cellar and wake in wet reeds -> he takes stock (phone, headlamp, notebook, her first-aid kit) and decides to follow the river downstream.
* Tomáš hides that he understands Japanese -> Mizuki slips, says in Japanese that she wants to go home, and he answers before he can stop himself.
* On the bank he looks for a status window and a summoner and finds neither -> Mizuki teases his bookish Japanese and decides the order: water, shelter, people.`;

export const summaryReasoning = 'Reading the passage beat by beat, keeping who acts, what changes and what the next part must remember.';

// The regular user (jana) uses the shared provider once, so that usage and limits have numbers.
export const readerBook = 'Night Shift';
export const readerTurn = {
  scene: 'A hospital ward in Sendai, 3 a.m.',
  pov: 'Mizuki',
  present: 'Mizuki; an old man in bed 7',
  instructions: 'Mizuki does her night round alone. The old man in bed seven is awake and wants to talk about the weather, because it is easier than talking about his daughter.',
  reply: `The ward was quiet in the way it only was at three in the morning, a low hum of machines and the soft squeak of Mizuki's shoes on the floor. She checked bed four, bed five, bed six, writing the numbers on the back of her hand because the chart was at the desk.

Bed seven was awake. The old man had propped himself on two pillows and was watching the rain run down the window, his hands folded on the blanket like something he had been told to guard.

"It will stop by morning," he said, before she could ask. "The rain. It always does when somebody important is coming."

"Is somebody coming?" Mizuki asked, and checked his drip, and did not say that his daughter's name was written on the visitor list for the third day in a row and crossed out each time.`,
};

// Her second part, written after the limit was set, so that the limit dialog shows what she used.
export const readerTurn2 = {
  scene: 'A hospital ward in Sendai, 3 a.m.',
  pov: 'Mizuki',
  present: 'Mizuki; the old man',
  instructions: 'The old man asks Mizuki to open the window a hand-width so he can smell the rain. She breaks a rule for him and pretends to check the lock.',
  reply: `"Only a little," the old man said. "A hand. I won't climb out, nurse, I can barely climb in."

Mizuki looked at the sign on the window, the one about draughts and infections, and then at the wet glass. She slid the latch back a hand's width and stood with her back to the room, apparently inspecting the lock. Cold air came in smelling of asphalt and wet leaves, and behind her she heard him breathe it in as if he had been given back something he had lost.

"Thank you," he said, after a while. "Tell nobody."

"Lock's loose," Mizuki said gravely. "I'll write it down."`,
};
