// The Agents tab and everything behind it (user/agents.md). Run on a COPY of 22-arcoutline (the book has an arc outline
// for the JSON merge), it is discarded: the flow gives the administrator more concurrent runs, clones the book three times
// and creates four agents with runs that the fake model answers from its queue (`toolCalls`, as in shoot-help):
//   Continuity editor  (the book)             a conversation: a question, work, findings, edits, a sub-agent, a report; ends Done
//   Lore keeper        (Harry Potter - Lore pass)    asks a question (ask_user), then paused
//   Style editor       (Harry Potter - Style pass)   wants to delete a part: waits for the approval
//   Drafter            (Harry Potter - Draft)        works (the fake model is slow) while the Agents tab is taken
// Pictures: agents-tab, agent-start-menu, agent-dialog, agent-dialog-prompts, agent-commands, agent-models, agent-console-*,
// agent-checkpoints, agent-context, agent-diagnostics, agent-subagents, agent-book-copy, agent-runs, agent-approval, agent-resume,
// agent-stop, agent-review*, agent-clone, agent-locked-*, book-clone, phone-agents*, admin-agents.
import {
  addPermission, bookTab, openBook, closeDialog, dialog, dialogBox, dialogRects, fill, idle, launch, llm, login, logout, main, openPermissions, openTab, pick,
  pickSelect, press, section, showToasts, sleep, snap, snapDialog, users,
} from '../lib/shot.mjs';
import { partAction, parts, sideTab, widenSidebar } from '../lib/story.mjs';
import {
  clickRow, consoleSend, consoleTab, newAgent, queueRounds, rowButton, startAgent, statusOf, waitDialogText, waitStatus,
} from '../lib/agents.mjs';

const HP = 'Harry Potter and Half Blood Prince - FDM';
const LORE_BOOK = 'Harry Potter - Lore pass';
const STYLE_BOOK = 'Harry Potter - Style pass';
const DRAFT_BOOK = 'Harry Potter - Draft';
const FREE_BOOK = 'Harry Potter - Epilogue';          // no agent: the book of a clone of an agent

const PLAN = status => ['Read the parts and the lorebook', 'Check the names, ages and places against the entries',
  'Fix what is certain', 'Report what is not', 'Ask a reader for a second opinion'].map((text, i) => ({ text, status: status[i] }));

await main(async ({ page }) => {
  await llm('/_reset', {});
  await login(page);

  // ---------------------------------------------------------------- the administrator may run three agents at once
  await section(page, 'permission', async () => {
    await press(page, 'Admin');
    await idle(page, 800);
    await openTab(page, 'Users');
    const d = await openPermissions(page, 'admin');
    await addPermission(d, 'Agents');
    await fill(d, 'Concurrent runs', '3');
    await press(d, 'Save');
    await idle(page, 800);
    await press(d, 'Exit');
    // the Agents tab of the workspace is built when the page is; the page of the administrator has it already
    await page.getByRole('button', { name: 'Admin', exact: true }).first().click().catch(() => {});
    await openTab(page, 'Books');
  });

  // ---------------------------------------------------------------- one book for every agent (a book has at most one)
  await section(page, 'clones', async () => {
    await openTab(page, 'Books');
    for (const [i, name] of [LORE_BOOK, STYLE_BOOK, DRAFT_BOOK, FREE_BOOK].entries()) {
      const rows = page.locator('vaadin-grid vaadin-button:has(vaadin-icon[icon$=":clone"])');
      // the clone of the original (the first row sorts by the last change: the original is the one with the full name)
      const cell = page.locator('vaadin-grid-cell-content').filter({ hasText: new RegExp('^\\s*' + HP + '\\s*$') }).first();
      const y = (await cell.boundingBox()).y;
      let best = 0, distance = 1e9;
      for (let k = 0; k < await rows.count(); k++) {
        const box = await rows.nth(k).boundingBox();
        if (box && Math.abs(box.y - y) < distance) { best = k; distance = Math.abs(box.y - y); }
      }
      await rows.nth(best).click();
      await idle(page, 700);
      const d = dialog(page);
      await fill(d, 'Name', name);
      if (i === 0) await snapDialog(page, 'book-clone');
      await press(d, 'Clone book', { wait: 2500 });
    }
    await snap(page, '_books-cloned');
  });

  // ---------------------------------------------------------------- the definitions
  await section(page, 'definitions', async () => {
    await openTab(page, 'Agents');
    // Continuity editor: the book itself, a sub-agent model, the commands of an editor
    let d = await newAgent(page, { name: 'Continuity editor', book: HP });
    await sleep(400);
    await snapDialog(page, 'agent-dialog');
    await fill(d, 'Most sub-agents', '1');
    await fill(d, 'Instructions (agents.md)', 'Check the story against the lorebook. Fix only what is certain (a typo, a wrong name) and report the rest. Keep the voice of the author.');
    // the model profile for sub-agents
    await d.getByRole('button', { name: 'Add a model', exact: true }).click();
    await sleep(800);
    const m = dialog(page);
    await fill(m, 'Role', 'Reader');
    await pick(m, 'Model', 'Local Reasoner');
    await pick(m, 'Protocols', 'Chat Completion');
    await fill(m, 'Description', 'Reads a part or an entry and answers a question about it. Slow but careful; it does not write.');
    await sleep(500);
    await snapDialog(page, 'agent-models');
    await press(m, 'OK');
    await sleep(600);

    // prompts and the info popover of the default template
    const system = d.getByLabel('System prompt', { exact: true }).first();
    await system.scrollIntoViewIfNeeded();
    // the (i) of the system prompt opens the hints with Insert Default Template
    await d.locator('vaadin-text-area').filter({ has: page.locator('label', { hasText: 'System prompt' }) }).locator('vaadin-icon').first().click();
    await sleep(800);
    {
      const r = (await dialogRects(page))[0];
      await snap(page, 'agent-dialog-prompts', { clip: { x: r.x, y: r.y, width: r.width + 280, height: r.height } });   // the hints stick out of the window
    }
    await page.keyboard.press('Escape');
    await sleep(300);

    // the commands: the roles, the grid
    const selection = d.getByLabel('Selection', { exact: true }).first();
    await selection.scrollIntoViewIfNeeded();
    await pick(d, 'Selection', 'Editor');
    await sleep(600);
    await snapDialog(page, 'agent-commands');
    await pick(d, 'Selection', 'All non-destructive');
    await press(d, 'OK', { wait: 1200 });

    // Lore keeper, Style editor (every command: it may ask to delete), Drafter
    d = await newAgent(page, { name: 'Lore keeper', book: LORE_BOOK });
    await pick(d, 'Selection', 'Lore keeper');
    await press(d, 'OK', { wait: 1000 });
    d = await newAgent(page, { name: 'Style editor', book: STYLE_BOOK });
    await pick(d, 'Selection', 'All');
    await press(d, 'OK', { wait: 1000 });
    d = await newAgent(page, { name: 'Drafter', book: DRAFT_BOOK });
    await pick(d, 'Selection', 'Drafter');
    await press(d, 'OK', { wait: 1000 });
    await snap(page, '_agents-defined');
  });

  await section(page, 'start-menu', async () => {
    await (await rowButton(page, 'Continuity editor', 'play')).click();
    await sleep(600);
    const item = page.getByText('Start in console', { exact: true }).first();
    const box = await item.boundingBox();
    const rowBox = await (await rowButton(page, 'Continuity editor', 'play')).boundingBox();
    await snap(page, 'agent-start-menu', { clip: { x: 300, y: Math.max(0, rowBox.y - 24), width: 1590, height: box.y + box.height - rowBox.y + 60 } });
    await page.keyboard.press('Escape');
    await sleep(400);
  });

  // ---------------------------------------------------------------- Continuity editor: a conversation in the console
  await section(page, 'run-editor', async () => {
    await queueRounds([
      { reply: 'Hello. I have read the settings of **Harry Potter and Half Blood Prince - FDM**: five parts on the active branch, one lorebook with eleven entries. What would you like me to do?' },
    ]);
    await startAgent(page, 'Continuity editor', 'Start in console');
    await waitDialogText(page, 'What would you like me to do?');
    await sleep(2500);
    await snapDialog(page, 'agent-console-start');

    // the task; the next rounds are queued before it is sent
    await queueRounds([
      { reasoning: 'The author wants the five parts checked against the lorebook. Start by reading what there is: the book, the parts and the entries, and write a plan.',
        toolCalls: [
          { name: 'update_plan', arguments: { items: PLAN(['in_progress', 'pending', 'pending', 'pending', 'pending']) } },
          { name: 'get-book-info', arguments: {} },
          { name: 'list-parts', arguments: {} },
          { name: 'list-lore-entries', arguments: { limit: 20 } },
        ] },
      { delayMs: 120000, reply: 'Reading the parts one by one.' },
    ]);
    await consoleSend(page, 'Check the five parts against the lorebook. Fix what is certain (a typo, a wrong name) and report the rest. Do not touch the lorebook entries of the characters.');
    await waitDialogText(page, 'Rounds: 2', 30000);
    await sleep(1500);
    // while it works the author may write: the message is queued
    await consoleSend(page, 'Also look at how Ginny is described in part 1.', 600);
    await sleep(1200);
    await snapDialog(page, 'agent-console-run');
  });

  // the author does not want to wait for the slow answer: Send now drops the round and the agent reads the message next
  await section(page, 'run-editor-work', async () => {
    const READ = { name: 'get-part', arguments: {} };
    await queueRounds([
      { toolCalls: [
          { name: 'get-part', arguments: { part: 'h:m1' } },
          { name: 'get-part', arguments: { part: 'h:m5' } },
          { name: 'get-lore-entry', arguments: { entry: 'h:e5' } },
        ] },
      { reasoning: 'Part 1 never says the colour of Ginny\'s hair (fog), the entry says red. That is a note, not an error. Part 5 mentions a map that is in no entry, and calls the lane empty although a door was heard in part 4.',
        toolCalls: [
          { name: 'update_plan', arguments: { items: PLAN(['done', 'done', 'in_progress', 'pending', 'pending']) } },
          { name: 'report', arguments: { severity: 'note', text: 'Part 1: Ginny is only "her hair darker with fog" - the colour (red, in her entry) is never said. Not a contradiction, but a model reading only the part will not know it.' } },
          { name: 'report', arguments: { severity: 'warning', text: 'Part 5: Harry says "I\'ve seen the map". The Marauder\'s Map is in no lorebook entry, so the model is never told that it exists.' } },
          { name: 'report', arguments: { severity: 'problem', text: 'Parts 4 and 5: a door closes and there is lamplight under a doorway behind the Three Broomsticks (part 4), but in part 5 Draconia looks along "the empty stretch of the lane". Either somebody is there or the lane is not empty.' } },
        ] },
      { toolCalls: [
          { name: 'edit-part', arguments: { part: 'h:m5', old: '"Walking. It\'s a free village."', new: '"Walking. It is a free village, Potter."' } },
          { name: 'edit-part', arguments: { part: 'h:m4', old: 'Somewhere ahead a door closed.', new: 'Somewhere ahead a door closed, and a bolt slid home.' } },
          { name: 'create-lore-entry', arguments: { name: 'The Marauder\'s Map', payload: 'A map of Hogwarts and the grounds that shows every person in it as a moving name. Harry has it from the twins; very few know that it exists.', comment: 'Made by the agent: part 5 mentions the map.' } },
          { name: 'update-arc', arguments: { arc: 1, description: 'Harry follows Draconia through a foggy Hogsmeade and finds her crying behind the Three Broomsticks; the Marauder\'s Map is mentioned for the first time.' } },
          { name: 'update-lore-entry', arguments: { entry: 'h:e9', payload: 'Hogsmeade, the only all-wizarding village in Britain, a short walk from the castle. On autumn evenings fog rolls down from the hills; this year Aurors watch the main street and the post office.' } },
          { name: 'create-part', arguments: { parent: 'h:m5', text: 'The bell had stopped by the time he found his voice again. The lane was empty, and he stayed where he was until his own breath had stopped smoking in the lamplight.' } },
        ] },
      { toolCalls: [
          { name: 'write_file', arguments: { path: '/notes/continuity.md', content: '# Continuity notes\n\n* Part 1: the colour of Ginny\'s hair is not said (entry: red).\n* Part 5: the map has no entry (made one).\n* Parts 4-5: a door and lamplight, then an "empty" lane (reported).\n* Fixed: a bolt in part 4, "It is" in part 5.\n' } },
          { name: 'checkpoint', arguments: { label: 'before the second opinion' } },
        ] },
      { toolCalls: [
          { name: 'spawn_agent', arguments: { profile: 'Reader', task: 'Read parts 4 and 5 of the active branch (list-parts, get-part) and say in three sentences whether somebody could be behind the Three Broomsticks in part 5 when Draconia calls the lane empty.' } },
        ] },
      { toolCalls: [
          { name: 'list-parts', arguments: { limit: 10 } },
          { name: 'get-part', arguments: { part: 'h:m4' } },
        ] },
      { reasoning: 'Part 4 ends with Harry in the shadow one step away from her: nobody else is named.',
        reply: 'Nobody else is named in either part. In part 4 the door and the lamplight belong to the back of the inn, where Draconia stands; in part 5 she only means that no one is in the lane itself, so the two parts do not really contradict each other. I would soften "empty" to "empty of people" to be safe.' },
      { toolCalls: [
          { name: 'update_plan', arguments: { items: PLAN(['done', 'done', 'done', 'done', 'done']) } },
          { name: 'finish', arguments: { summary: '## Report\n\nI checked the five parts against the lorebook.\n\n**Fixed**\n* part 5: "It\'s" -> "It is ... Potter"\n* part 4: the door now has a bolt\n\n**Added**\n* a lorebook entry for the Marauder\'s Map; a shorter entry for Hogsmeade\n* a closing part after part 5\n\n**Reported** (3 findings): the colour of Ginny\'s hair in part 1, the map, and the lane that is empty in part 5.\n\nThe reader agrees that parts 4 and 5 do not contradict each other.' } },
        ] },
    ]);
    // Send now (the lightning bolt of the queued bubble)
    await dialog(page).locator('vaadin-button:has(vaadin-icon[icon$=":bolt"])').first().click();
    await waitDialogText(page, 'Report of the agent', 90000);
    await sleep(2500);
    await snapDialog(page, 'agent-console-report');
  });

  // ---------------------------------------------------------------- the console of the finished conversation
  await section(page, 'console-run', async () => {
    const d = dialog(page);
    await d.getByRole('button', { name: /^Plan/ }).first().click();
    await sleep(800);
    await snap(page, 'agent-console-plan', { clip: (await dialogRects(page))[0] });
    await page.keyboard.press('Escape');
    await sleep(400);
    await d.getByRole('button', { name: /^Findings/ }).first().click();
    await sleep(800);
    await snap(page, 'agent-console-findings', { clip: (await dialogRects(page))[0] });
    await page.keyboard.press('Escape');
    await sleep(400);
  });

  await section(page, 'console-tabs', async () => {
    for (const [tab, name] of [['Workspace', 'agent-console-workspace'], ['Checkpoints', 'agent-checkpoints'], ['Context', 'agent-context'],
      ['Book copy', 'agent-book-copy'], ['Diagnostics', 'agent-diagnostics'], ['Runs', 'agent-runs']]) {
      await consoleTab(page, tab, 2500);
      await snapDialog(page, name);
    }
    await consoleTab(page, 'Sub-agents', 2000);
    await snapDialog(page, 'agent-subagents');
    await consoleTab(page, 'Run', 800);
  });
  await section(page, 'console-command', async () => {
    await queueRounds([{ reply: '## Summary of my work\n\nTask: check the five parts against the lorebook, fix what is certain, report the rest.\n\nDone: 3 findings reported (hair colour in part 1, the map, the empty lane), two typos fixed, an entry for the Marauder\'s Map and a shorter Hogsmeade entry made, one closing part written; the reader says parts 4 and 5 do not contradict each other.\n\nNotes are in /notes/continuity.md.' }]);
    const d = dialog(page);
    const line = d.locator('vaadin-combo-box input').first();
    await line.click();
    await line.fill('/compact stress the findings');
    await sleep(500);
    await line.press('Enter');
    await waitDialogText(page, 'Summary of the earlier work', 60000);
    await sleep(2000);
    await snapDialog(page, 'agent-console-command');
  });

  // the conversation ends: Finish, and the run waits for the review
  await section(page, 'finish', async () => {
    await press(dialog(page), 'Finish', { wait: 1500 });
    await waitDialogText(page, 'Done, waits for the review', 20000);
    await closeDialog(page);
    await idle(page, 800);
  });

  // ---------------------------------------------------------------- Lore keeper asks a question, then is paused
  await section(page, 'run-keeper', async () => {
    await queueRounds([
      { toolCalls: [{ name: 'list-lorebooks', arguments: {} }, { name: 'list-lore-entries', arguments: { limit: 20 } }] },
      { toolCalls: [{ name: 'ask_user', arguments: {
          question: 'The lorebook has no entry for **Madam Rosmerta**, who keeps the Three Broomsticks and is in part 1. Shall I write one, and how should she treat Harry?',
          options: ['Write it, friendly to Harry', 'Write it, neutral', 'Leave it out'] } }] },
    ]);
    await startAgent(page, 'Lore keeper', 'Start immediately');
    await waitStatus(page, 'Lore keeper', 'Waiting for you', 60000);
    await clickRow(page, 'Lore keeper', 'terminal', 1500);
    await waitDialogText(page, 'Question of the agent');
    await sleep(1500);
    await snapDialog(page, 'agent-console-question');
    await press(dialog(page), 'Pause', { wait: 1500 });
    await closeDialog(page);
    await waitStatus(page, 'Lore keeper', 'Paused', 20000);
  });

  await section(page, 'resume-stop-dialogs', async () => {
    await clickRow(page, 'Lore keeper', 'play', 900);
    await fill(dialog(page), 'Note for the agent (optional)', 'Make it neutral, she only serves them.');
    await snapDialog(page, 'agent-resume');
    await press(dialog(page), 'Cancel');
    await clickRow(page, 'Lore keeper', 'stop', 900);
    await snapDialog(page, 'agent-stop');
    await closeDialog(page);
  });

  // ---------------------------------------------------------------- Style editor wants to delete a part
  await section(page, 'run-style', async () => {
    // the console is opened after the agent already waits: the window of the approval comes up over it
    await queueRounds([
      { delayMs: 7000, toolCalls: [{ name: 'get-book-info', arguments: {} }, { name: 'list-parts', arguments: {} }] },
      { toolCalls: [{ name: 'delete-part', arguments: { part: 'h:m5' } }] },
    ]);
    await startAgent(page, 'Style editor', 'Start immediately');
    await waitStatus(page, 'Style editor', 'Waiting for approval', 40000);
    await clickRow(page, 'Style editor', 'terminal', 1500);
    await page.getByText('wants to run the command', { exact: false }).first().waitFor({ timeout: 10000 });
    await sleep(1500);
    // the window of the approval stands over the console: the picture is the whole window, with the console behind it
    await snap(page, 'agent-approval');
    // the window can not be closed without a decision: a new page, the agent keeps waiting
    await page.reload();
    await idle(page, 2000);
    await openTab(page, 'Agents');
    await waitStatus(page, 'Style editor', 'Waiting for approval', 20000);
  });

  // ---------------------------------------------------------------- Drafter is working: the Agents tab
  await section(page, 'run-drafter', async () => {
    await queueRounds([{ delayMs: 300000, reply: 'Drafting.' }]);
    await startAgent(page, 'Drafter', 'Start immediately');
    await waitStatus(page, 'Drafter', 'Running', 30000);
    await sleep(800);
    await snap(page, 'agents-tab');
  });

  // the same on a phone: cards in place of the table, the console with icon tabs
  await section(page, 'phone', async () => {
    const phone = await launch({ phone: true });
    try {
      const pp = phone.page;
      await login(pp);
      await sleep(1000);
      await pp.mouse.click(28, 27);                       // the burger of the top bar: the drawer
      await sleep(700);
      await pp.getByRole('tab', { name: /^Agents( \d+)?$/  }).click();
      await idle(pp, 1500);
      await pp.mouse.click(390, 500);                     // outside the drawer: it closes
      await sleep(700);
      await snap(pp, 'phone-agents');
      await pp.locator('vaadin-button:has(vaadin-icon[icon$=":terminal"])').first().click();
      await idle(pp, 2500);
      await snap(pp, 'phone-agent-console');
    } finally {
      await phone.browser.close();
    }
  });

  await section(page, 'locked', async () => {
    await openTab(page, 'Books');
    await sleep(800);
    // the pen of a book an agent works on does not open it: a notice says which agent holds it
    await showToasts(page);
    const cell = page.locator('vaadin-grid-cell-content').filter({ hasText: new RegExp('^\\s*' + DRAFT_BOOK + '\\s*$') }).first();
    const y = (await cell.boundingBox()).y;
    const pens = page.locator('vaadin-grid vaadin-button:has(vaadin-icon[icon$=":pencil"])');
    let best = 0, distance = 1e9;
    for (let k = 0; k < await pens.count(); k++) {
      const box = await pens.nth(k).boundingBox();
      if (box && Math.abs(box.y - y) < distance) { best = k; distance = Math.abs(box.y - y); }
    }
    await pens.nth(best).click();
    await sleep(1200);
    await snap(page, 'agent-locked-book');
    await openTab(page, 'Lorebooks');
    await sleep(1200);
    await snap(page, 'agent-locked-lorebook');
    await openTab(page, 'Agents');
  });

  await section(page, 'drafter-stop', async () => {
    await clickRow(page, 'Drafter', 'stop', 900);
    await press(dialog(page), 'Stop and discard the result', { wait: 1500 });
    await waitStatus(page, 'Drafter', 'Not running', 20000);
  });

  // the agent that waits for the approval is stopped the same way: the lorebook is the author's again
  await section(page, 'style-stop', async () => {
    await clickRow(page, 'Style editor', 'stop', 900);
    await press(dialog(page), 'Stop and discard the result', { wait: 1500 });
    await waitStatus(page, 'Style editor', 'Not running', 20000);
  });

  await section(page, 'clone', async () => {
    await clickRow(page, 'Continuity editor', 'clone', 900);
    await fill(dialog(page), 'Name', 'Continuity editor (epilogue)');
    await sleep(500);
    await snapDialog(page, 'agent-clone');
    await closeDialog(page);
  });

  await section(page, 'admin-agents', async () => {
    await page.getByRole('button', { name: 'Admin', exact: true }).first().click();
    await idle(page, 900);
    await page.getByRole('tab', { name: /^Agents( \d+)?$/  }).last().click();
    await idle(page, 900);
    await snap(page, 'admin-agents');
    await page.getByRole('button', { name: 'Exit', exact: true }).first().click().catch(() => {});
    await idle(page, 600);
  });

  // ---------------------------------------------------------------- the author did not wait: the same things changed in the book
  await section(page, 'author-edits', async () => {
    await openBook(page);
    await bookTab(page, 'Story');
    await partAction(page, -1, 'Edit');
    await idle(page, 600);
    const area = parts(page).last().locator('textarea').first();
    const text = await area.inputValue();
    const from = '"Walking. It\'s a free village."';
    if (!text.includes(from)) throw new Error('text to edit not found');
    await area.fill(text.replace(from, '"Walking. It\'s a free village, I\'m told."'));
    await area.dispatchEvent('change');
    await partAction(page, -1, 'Save');
    await idle(page, 1000);
    // and the arc the agent described otherwise
    await widenSidebar(page, 700);
    await sideTab(page, 'Arcs');
    await page.getByRole('button', { name: 'Edit arc', exact: true }).first().click();
    await idle(page, 700);
    const d = dialog(page);
    await fill(d, 'Description', 'Harry follows Draconia through a foggy Hogsmeade and finds her crying; the church bell ends the evening.');
    await press(d, 'Save');
    await idle(page, 1000);
    await press(page, 'Exit');
    await idle(page, 1000);
  });

  // ---------------------------------------------------------------- reviewing the result
  await section(page, 'review', async () => {
    await openTab(page, 'Agents');
    await clickRow(page, 'Continuity editor', 'code-compare', 3000);
    await snapDialog(page, 'agent-review');
    // a conflict: the sentence that both changed
    const d = dialog(page);
    await d.locator('vaadin-grid-cell-content').filter({ hasText: /^5: / }).first().click();
    await idle(page, 1500);
    await snapDialog(page, 'agent-review-conflict');
    await d.getByText('Differences', { exact: false }).first().scrollIntoViewIfNeeded().catch(() => {});
    await sleep(600);
    await snapDialog(page, 'agent-review-differences');
  });

  // the data of an extension (the arc outline) that both changed: put together place by place
  await section(page, 'json-merge', async () => {
    const d = dialog(page);
    await d.locator('vaadin-grid-cell-content').filter({ hasText: /^Book$/ }).first().click();
    await idle(page, 1500);
    await snapDialog(page, 'agent-review-json');
    await d.getByRole('button', { name: 'Merge JSON data', exact: true }).first().click();
    await idle(page, 2500);
    // open the place that both changed: arcs, the first arc
    const m = dialog(page);
    for (let i = 0; i < 2; i++) {
      await m.locator('vaadin-grid-tree-toggle:not([expanded]):not([leaf])').first().click();
      await sleep(900);
    }
    await snap(page, 'agent-json-merge', { clip: (await dialogRects(page)).at(-1) });
  });
});
