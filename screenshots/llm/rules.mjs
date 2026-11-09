// Rules of the fake inference server: the first rule whose `when(ctx)` is true answers.
// ctx: { text, system, last, messages, model, stream, wantsJson, hasTools, mode, body }
// A rule answers with `reply` (string, or function(ctx) returning a string or {reply, reasoning, status, message}),
// optionally `reasoning` and `delayMs`.
import {
    arcOutline,
    betaPersona,
    betaReaders,
    loreCompaction,
    loreGeneration,
    review,
    sideQuery
} from '../dataset/plugins.mjs';
import {metaSummary, readerTurn, readerTurn2, summaryReasoning, turns} from '../dataset/story.mjs';

const storyRules = Object.entries(turns).filter(([id]) => id !== 't2b').map(([id, t]) => ({
  name: 'story-' + id,
  when: ctx => !ctx.isSummary && ctx.last.includes(t.instructions),
  reply: t.reply,
  reasoning: t.reasoning,
}));

// A summary request is one big system message with the text of the parts to summarize. The answer is the
// summaries of every part whose text is in it.
const summaryRule = {
  name: 'summary',
  when: ctx => /compact event ledger/.test(ctx.system) && ctx.messages.length === 1,
  reply: ctx => {
    const found = ['t1', 't2a', 't3', 't4', 't5'].filter(id => ctx.text.includes(turns[id].reply.trim().slice(0, 60)));
    return { reply: found.map(id => turns[id].summary).join('\n'), reasoning: summaryReasoning };
  },
};

const metaRule = {
  name: 'meta-summary',
  when: ctx => /Level-2 Context Consolidation/.test(ctx.system),
  reply: () => ({ reply: metaSummary, reasoning: summaryReasoning }),
};

const readerRule = { name: 'reader', when: ctx => ctx.last.includes(readerTurn.instructions), reply: readerTurn.reply };
const readerRule2 = { name: 'reader-2', when: ctx => ctx.last.includes(readerTurn2.instructions), reply: readerTurn2.reply };

const sideQueryRule = {
  name: 'side-query',
  when: ctx => ctx.system.startsWith('[System Instruction: You are a precise context-parsing AI assistant.]'),
  reply: ctx => (sideQuery.find(q => ctx.last.includes(q.question)) ?? sideQuery[0]).answer,
};

const reviewRule = {
  name: 'review',
  when: ctx => /reddit simulator/i.test(ctx.last),
  reply: () => ({ reply: review, reasoning: 'A forum thread: mixed reactions, a troll, a pun, quoting lines from the part.' }),
};

const loreMarker = loreGeneration.slice(0, 60);
const loreCompactionRule = {
  name: 'lore-compaction',
  when: ctx => ctx.text.includes(loreMarker),
  reply: () => loreCompaction,
};
const loreRule = {
  name: 'lore-generation',
  when: ctx => ctx.text.includes('Write the lore entry for "The Bridge of Silvakhor"'),
  reply: () => ({ reply: loreGeneration, reasoning: 'A bridge needs soldiers, a toll and a reason to matter to the story.' }),
};

// Arc Outline: the prompt for an arc and the prompt for the chapters of an arc are told apart by their first sentence.
const arcRule = {
  name: 'arc-outline-arc',
  when: ctx => ctx.system.includes('design ONE new story arc'),
  reply: () => ({ reply: JSON.stringify(arcOutline.generateArc.answer), reasoning: arcOutline.generateArc.reasoning }),
};
const arcImportRule = {
  name: 'arc-outline-import',
  when: ctx => ctx.system.includes('summarize it as ONE story arc'),
  reply: () => ({ reply: JSON.stringify(arcOutline.importArc.answer), reasoning: arcOutline.importArc.reasoning }),
};
const arcChaptersRule = {
  name: 'arc-outline-chapters',
  when: ctx => ctx.system.includes('divide ONE story arc'),
  reply: () => ({ reply: JSON.stringify(arcOutline.generateChapters.answer), reasoning: arcOutline.generateChapters.reasoning }),
};

// Beta Readers: the personas answer in the schema the plugin sends, or in `**Name:** *action* speech` paragraphs for a chat that wants text.
const betaRule = {
  name: 'beta-readers',
  when: ctx => ctx.system.includes('# How to answer') && /beta reader/i.test(ctx.system),
  reply: ctx => {
    const kind = ctx.system.includes('one-on-one') ? 'single' : 'group';
    const found = betaReaders.find(q => ctx.last.includes(q.question)) ?? betaReaders.find(q => q.kind === kind);
    if (ctx.wantsJson) return { reply: JSON.stringify({ messages: found.answer.messages }), reasoning: found.answer.reasoning };
    return found.answer.messages.map(m => `**${m.speaker}:** ` + m.parts.map(p => p.type === 'action' ? `*${p.text}*` : p.text).join(' ')).join('\n\n');
  },
};

// Beta Readers: a persona written from a few words (the window of a persona)
const betaPersonaRule = {
  name: 'beta-readers-persona',
  when: ctx => ctx.system.includes('You create personas for beta readers'),
  reply: () => `Name: ${betaPersona.name}\nDescription: ${betaPersona.description}`,
};

// Image Generator: the prompt for an image, written from a part of the story
const imagePromptRule = {
  name: 'image-prompt',
  when: ctx => ctx.text.includes('You write prompts for an image generation model'),
  reply: 'watercolor, a reed field at night under an enormous low moon, a young man with a cracked headlamp and a young woman in a grey jacket walking along a black river, tall reeds, a long broken road of moonlight on the water, cold blue and green, small warm light, quiet mood',
};

// Audiobook: the labelling of a part. The pieces come as lines `[id] N: narration` / `[id] D: speech`; narration is the Narrator, the speeches alternate
// between two characters of the book, a speech with "low" or "murmur" in it is a whisper, the first speech gets a valid sound tag.
const audiobookLabelsRule = {
  name: 'audiobook-labels',
  when: ctx => /^\[\d+\] [ND]: /m.test(ctx.last),
  reply: ctx => {
    let speeches = 0;
    const labels = [];
    for (const line of ctx.last.split('\n')) {
      const m = /^\[(\d+)\] (N|D): (.*)$/.exec(line);
      if (!m) continue;
      const id = Number(m[1]);
      if (m[2] === 'N') {
        labels.push({ id, speaker: 'Narrator', delivery: 'NEUTRAL', intensity: 0.5, tagged_text: '' });
      } else {
        labels.push({ id, speaker: speeches % 2 === 0 ? 'Tomáš' : 'Mizuki', delivery: /low|murmur/.test(m[3]) ? 'WHISPER' : 'CALM', intensity: 0.3,
          tagged_text: speeches === 0 ? '[sigh] ' + m[3] : '' });
        speeches++;
      }
    }
    return JSON.stringify({ labels });
  },
};

export default [
  audiobookLabelsRule,
  imagePromptRule,
  betaPersonaRule,
  betaRule,
  arcRule,
  arcImportRule,
  arcChaptersRule,
  loreCompactionRule,
  loreRule,
  reviewRule,
  sideQueryRule,
  readerRule,
  readerRule2,
  summaryRule,
  metaRule,
  ...storyRules,
  { name: 'fallback', when: () => true, reply: 'Nothing scripted for this request.' },
];
