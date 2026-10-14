// Rules of the fake inference server: the first rule whose `when(ctx)` is true answers.
// ctx: { text, system, last, messages, model, stream, wantsJson, hasTools, mode, body }
// A rule answers with `reply` (string, or function(ctx) returning a string or {reply, reasoning, status, message}),
// optionally `reasoning` and `delayMs`.
import {arcOutline, loreCompaction, loreGeneration, review, sideQuery} from '../dataset/plugins.mjs';
import {ginnyTurn, ginnyTurn2, metaSummary, summaryReasoning, turns} from '../dataset/story.mjs';

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
  when: ctx => /Context Ledger/.test(ctx.system) && ctx.messages.length === 1,
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

const ginnyRule = { name: 'ginny', when: ctx => ctx.last.includes(ginnyTurn.instructions), reply: ginnyTurn.reply };
const ginnyRule2 = { name: 'ginny-2', when: ctx => ctx.last.includes(ginnyTurn2.instructions), reply: ginnyTurn2.reply };

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
  when: ctx => ctx.text.includes('Write the lore entry for "The Three Broomsticks"'),
  reply: () => ({ reply: loreGeneration, reasoning: 'An inn needs a keeper, a smell and a reason to matter to the story.' }),
};

// Arc Outline: the prompt for an arc and the prompt for the chapters of an arc are told apart by their first sentence.
const arcRule = {
  name: 'arc-outline-arc',
  when: ctx => ctx.system.includes('design ONE new story arc'),
  reply: () => ({ reply: JSON.stringify(arcOutline.generateArc.answer), reasoning: arcOutline.generateArc.reasoning }),
};
const arcChaptersRule = {
  name: 'arc-outline-chapters',
  when: ctx => ctx.system.includes('divide ONE story arc'),
  reply: () => ({ reply: JSON.stringify(arcOutline.generateChapters.answer), reasoning: arcOutline.generateChapters.reasoning }),
};

export default [
  arcRule,
  arcChaptersRule,
  loreCompactionRule,
  loreRule,
  reviewRule,
  sideQueryRule,
  ginnyRule,
  ginnyRule2,
  summaryRule,
  metaRule,
  ...storyRules,
  { name: 'fallback', when: () => true, reply: 'Nothing scripted for this request.' },
];
