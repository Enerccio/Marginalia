// The screens of 1.7.0 that have no flow of their own: the plugins Book Description Generator, Prompt Inspector and Prompt History (installed here),
// the usage report of the lorebook, and the summaries that became outdated (Revalidate / Resummarize all).
// Pictures: plugin-book-description, plugin-prompt-inspector, plugin-prompt-history, lore-usage, summaries-outdated, summaries-menu-outdated,
// lorebook-description, lorebook-share, and (the second round of 1.7.0) planner-tab, planner-scene-dialog, planner-new-turn, lmstudio-import,
// lorebook-condition, lorebook-condition-preview, summaries-resummarize-confirm, summaries-resummarize.
// State: a COPY of 13-db-backup (plugins are installed, a part is edited, a generation is sent), discarded. The fake model must run.
import fs from 'node:fs';
import path from 'node:path';
import {
  bookListMenu, bookTab, closeDialog, CONTENT, DESKTOP, dialog, field, fill, idle, llm, login, main, menuItem, openBook, openTab, outDir, pick, press, repo, section, sleep, snap, snapDialog,
} from '../lib/shot.mjs';
import {clickPlus, clickSummaries, fillTurn, openStoryMenu, parts, partAction, sideTab, widenSidebar} from '../lib/story.mjs';
import {metaSummary} from '../dataset/story.mjs';

const DESCRIPTION = 'A Czech cellar guide who learned Japanese from novels and a Japanese nurse on holiday step through a bricked archway and wake in a river valley held by an empire that speaks Vrakht. She knows he understands her; he does not know why she has not said so.';

/** The scenes the planner picture shows: what happens after the ferry landing of the story. */
const PLANNED = [
  { title: 'The ferry landing', scene: 'The ferry landing, night', pov: 'Mizuki', present: 'Mizuki, Tomáš, Saellen',
    instructions: 'The lantern belongs to a ferrywoman who shouts at them in Vrakht. Neither of them understands a word; Tomáš listens to the tone.' },
  { title: 'The toll house', scene: 'The toll house on the east bank', pov: 'Tomáš', present: 'Tomáš, Mizuki, a clerk',
    instructions: 'A clerk asks for their names and a fee in Vrakht. Tomáš pays with the only thing of value he has, the headlamp.' },
  { title: 'Mizuki says it', scene: 'A fire at the edge of the reeds', pov: 'Mizuki', present: 'Mizuki, Tomáš',
    instructions: 'Mizuki tells Tomáš that she has known since the river that he understands Japanese. He does not apologise; he explains.' },
  { title: 'The first word', scene: 'The market, morning', pov: 'Tomáš', present: 'Tomáš, Saellen',
    instructions: 'Saellen teaches him a word of Vrakht by pointing. It is a rude one.' },
];

/** A conversation of LM Studio (`~/.lmstudio/conversations/<number>.conversation.json`): a user message, an answer with its thinking, a second user message. */
const text = value => ({ type: 'text', text: value });
const user = value => ({ versions: [{ type: 'singleStep', role: 'user', content: [text(value)] }], currentlySelected: 0 });
const answer = (thinking, value) => ({
  versions: [{ type: 'multiStep', role: 'assistant', senderInfo: { senderName: 'qwen3-30b' }, steps: [
    { type: 'contentBlock', style: { type: 'thinking', ended: true }, content: [text(thinking)] },
    { type: 'contentBlock', content: [text(value)], genInfo: { identifier: 'qwen/qwen3-30b-a3b' } },
  ] }],
  currentlySelected: 0,
});
const LM_STUDIO = {
  name: 'The ferry at night', createdAt: 1793958287215, systemPrompt: 'You write fiction with the author.',
  messages: [
    user('Write the scene where Mizuki and Tomáš reach the ferry landing at night. A lantern, a woman who shouts in a language neither of them knows.'),
    answer('Keep it short. The lantern first, then the shout, then Tomáš listening to the tone rather than the words.',
      'The lantern swung on its pole, and the woman under it shouted again, harder this time. Mizuki took a step back on the wet planks. Tomáš did not translate; he listened to the tone.'),
    user('Good. Now let Mizuki notice that he is not afraid.'),
    answer('One beat, from her side.', 'Mizuki noticed that his hands were steady. Whatever the woman was saying, it was not what he had feared, and he knew it before she did.'),
  ],
};

await main(async ({ page }) => {
  await llm('/_reset', {});
  await login(page);

  await section(page, 'install', async () => {
    await page.getByRole('button', { name: 'Admin' }).click();
    await idle(page, 600);
    await page.getByText('Extensions', { exact: true }).first().click();
    await idle(page, 800);
    for (const name of ['book-description-generator', 'prompt-inspector', 'prompt-history']) {
      const jar = path.join(repo, 'marginalia/plugins', name, 'target', `${name}-1.8.0.jar`);
      if (!fs.existsSync(jar)) throw new Error('not built: ' + jar);
      await page.locator('vaadin-upload input[type=file]').setInputFiles(jar);
      await idle(page, 4000);
      await page.getByRole('button', { name: 'Refresh' }).click();
      await idle(page, 1500);
      console.log('  installed', name);
    }
    await page.reload();
    await idle(page, 2000);
  });

  // ------------------------------------------------------------ Book Description Generator
  await section(page, 'book-description', async () => {
    await openBook(page);
    await bookTab(page, 'About');
    await press(page, 'Generate name and description', { exact: false });
    await idle(page, 1200);
    await llm('/_queue', { reply: JSON.stringify({ name: 'Unregistered', description: DESCRIPTION }) });
    const d = dialog(page);
    await press(d, 'Generate', { wait: 600 });
    await d.getByText('Copy to book', { exact: false }).first().waitFor({ timeout: 30000 });
    await sleep(1500);
    await snapDialog(page, 'plugin-book-description');
    await closeDialog(page);
  });

  // ------------------------------------------------------------ Lore usage report (the parts of the dataset were generated by this version)
  await section(page, 'lore-usage', async () => {
    await bookTab(page, 'Story');
    await openStoryMenu(page);
    await menuItem(page, 'Lore Usage').click();
    await idle(page, 1500);
    await snapDialog(page, 'lore-usage');
    await closeDialog(page);
  });

  // ------------------------------------------------------------ Prompt Inspector, then the history it leaves
  await section(page, 'prompt-inspector', async () => {
    await bookTab(page, 'Story');
    await openStoryMenu(page);
    await menuItem(page, 'Inspect prompt before sending').click();
    await idle(page, 600);
    await fillTurn(page, { scene: 'The ferry landing, night', pov: 'Mizuki', present: 'Mizuki, Tomáš, Saellen',
      instructions: 'A lantern on a pole, a broad woman who shouts at them in a language neither of them knows. Mizuki steps back; Tomáš reads her tone.' });
    await llm('/_queue', { reply: 'The lantern swung on its pole, and the woman under it shouted again, harder this time. Mizuki took a step back on the wet planks. Tomáš did not translate; he listened to the tone, and the tone, to his surprise, was not unkind.' });
    await press(page, 'Generate', { exact: false, wait: 800 });
    await page.getByText('Send this prompt to the model?', { exact: false }).first().waitFor({ timeout: 30000 });
    await sleep(1500);
    await snapDialog(page, 'plugin-prompt-inspector');
    await press(dialog(page), 'Send', { wait: 1500 });
    await page.getByText('The tone, to his surprise', { exact: false }).first().waitFor({ timeout: 30000 }).catch(() => {});
    await idle(page, 1500);
  });

  await section(page, 'prompt-history', async () => {
    await page.getByRole('button', { name: 'Exit', exact: true }).last().click();
    await idle(page, 1000);
    await openTab(page, 'Settings');
    await page.getByText('Extension Settings', { exact: true }).first().click();
    await idle(page, 800);
    await page.getByText('Prompt history', { exact: true }).last().click();
    await sleep(800);
    await press(page, 'Show prompt history', { exact: false });
    await idle(page, 1500);
    await snapDialog(page, 'plugin-prompt-history');
    await closeDialog(page);
  });

  // ------------------------------------------------------------ the description and the sharing of a lorebook
  await section(page, 'lorebook-description', async () => {
    await openTab(page, 'Lorebooks');
    await pick(page, 'Lorebook', 'Unregistered - the valley of the Lyrasil');
    await idle(page, 1200);
    await page.locator('vaadin-button:has(vaadin-icon[icon$=":align-left"])').first().click();
    await idle(page, 900);
    await snapDialog(page, 'lorebook-description');
    await closeDialog(page);
  });

  // ------------------------------------------------------------ outdated summaries
  await section(page, 'summaries-outdated', async () => {
    await openBook(page);
    await bookTab(page, 'Story');
    await partAction(page, 2, 'Edit');
    await idle(page, 600);
    const area = parts(page).nth(2).locator('textarea').first();
    const text = await area.inputValue();
    await area.fill(text.replace('He went on counting.', 'He went on counting, twice, out of habit, and then a third time out of fear.'));
    await area.dispatchEvent('change');
    await partAction(page, 2, 'Save', { askAboutSummaries: true });
    await idle(page, 1500);
    // the question about the summaries that the save would outdate
    await page.getByText('makes 1 summaries outdated', { exact: false }).first().waitFor({ timeout: 10000 });
    await snapDialog(page, 'summaries-save-warning');
    await press(page, 'Save anyway', { wait: 1200 });
    await clickSummaries(page);
    await idle(page, 1200);
    // the meta summary lists the summaries under it: open it, the one that is outdated now says so
    await page.locator('vaadin-dialog[opened] vaadin-grid-tree-toggle, vaadin-dialog[opened] vaadin-icon[icon$=":angle-right"]').first().click().catch(() => {});
    await idle(page, 1200);
    await snapDialog(page, 'summaries-outdated');
  });
  // ------------------------------------------------------------ Resummarize all summaries: the question, then the window that writes them again
  await section(page, 'resummarize', async () => {
    const overview = () => page.locator('vaadin-dialog[opened]').filter({ hasText: 'Resummarize all summaries' });
    if (!(await overview().count())) {
      await openBook(page);
      await bookTab(page, 'Story');
      await clickSummaries(page);
      await idle(page, 1200);
    }
    // three summaries (two and a meta summary): the answers come slowly, so the window can be seen at work
    for (const text of [
      '[LOCATION / SCENE: Prague cellar / evening tour]\n* Tomáš counts the tourists out and is alone with Mizuki at the bricked archway.\n* The wall breathes; she says so in Japanese, and he understands every word.',
      '[LOCATION / SCENE: Reed fields / night]\n* They wake in wet reeds under an enormous moon, with no signal.\n* Mizuki slips and says in Japanese that she wants to go home; he answers before he can stop himself.',
      '[LOCATION / SCENE: Reed fields -> the river bank / night]\n* Tomáš and Mizuki wake in wet reeds and decide to follow the river; his lie about Japanese comes out in the mud.',
    ]) await llm('/_queue', { reply: text, delayMs: 3000 });
    await page.locator('vaadin-menu-bar-button').filter({ hasText: 'Resummarize all summaries' }).first().click();
    await idle(page, 900);
    await snapDialog(page, 'summaries-resummarize-confirm');
    await press(dialog(page), 'Yes', { wait: 1500 });
    await page.getByText('Resummarize all summaries', { exact: true }).last().waitFor({ timeout: 15000 });
    await sleep(2500);
    await snapDialog(page, 'summaries-resummarize');
    // the window ends with Exit when the last summary is written (or closes itself)
    await sleep(9000);
    const exit = dialog(page).getByRole('button', { name: 'Exit', exact: true });
    if (await exit.count()) await exit.first().click();
    await idle(page, 800);
  });

  // ------------------------------------------------------------ the planner: scenes to write, put into the new turn form with a click
  await section(page, 'planner', async () => {
    // the Summaries window of the section before is still open
    const summaries = page.locator('vaadin-dialog[opened]').filter({ hasText: 'Resummarize all summaries' }).last();
    if (await summaries.count()) await summaries.getByRole('button', { name: 'Close', exact: true }).click();
    await idle(page, 500);
    await openBook(page);
    await bookTab(page, 'Story');
    await widenSidebar(page, 700);
    await sideTab(page, 'Planner');
    for (const scene of PLANNED) {
      await press(page, 'Add scene', { wait: 700 });
      const d = dialog(page);
      await fill(d, 'Title', scene.title);
      await fill(d, 'Scene Setting', scene.scene);
      await fill(d, 'POV Character', scene.pov);
      await fill(d, 'Present Characters', scene.present);
      await fill(d, 'Instructions', scene.instructions);
      if (scene === PLANNED[0]) await snapDialog(page, 'planner-scene-dialog');
      await press(d, 'Save', { wait: 900 });
    }
    // the first scene is written
    await page.locator('.planner-scene vaadin-checkbox').first().locator('input').click({ force: true });
    await idle(page, 800);
    await page.mouse.move(1500, 600);
    await sleep(400);
    await snap(page, 'planner-tab', { clip: { x: 16, y: 130, width: 690, height: 780 } });
    // a click on a scene puts its details into the new turn form
    await page.locator('.planner-scene-main').nth(1).click();
    await idle(page, 1200);
    await page.mouse.move(1500, 600);
    await sleep(400);
    await snap(page, 'planner-new-turn');
  });

  // ------------------------------------------------------------ import from LM Studio
  await section(page, 'lmstudio-import', async () => {
    const dir = path.join(outDir, '_lmstudio');
    fs.mkdirSync(dir, { recursive: true });
    const file = path.join(dir, '1793958287215.conversation.json');
    fs.writeFileSync(file, JSON.stringify(LM_STUDIO));
    await page.getByRole('button', { name: 'Exit', exact: true }).last().click();
    await idle(page, 1000);
    await openTab(page, 'Books');
    await bookListMenu(page, 'Import', 'Import from LM Studio');
    await idle(page, 800);
    const d = dialog(page);
    await d.locator('vaadin-upload input[type=file]').setInputFiles(file);
    await idle(page, 2500);
    await snapDialog(page, 'lmstudio-import');
    await dialog(page).getByRole('button', { name: /^(Close|Cancel)$/ }).first().click();
    await idle(page, 500);
  });

  // ------------------------------------------------------------ the condition of a lorebook entry
  await section(page, 'lorebook-condition', async () => {
    // a taller window, so the entry shows down to its Condition and its Note
    await page.setViewportSize({ width: DESKTOP.width, height: 1200 });
    await openTab(page, 'Lorebooks');
    await pick(page, 'Lorebook', 'Unregistered - the valley of the Lyrasil');
    await sleep(1000);
    const y = await page.evaluate(() => {
      const inp = [...document.querySelectorAll('vaadin-text-field')].find(f => f.value === 'Mizuki Hayashida');
      return inp ? inp.getBoundingClientRect().y + inp.getBoundingClientRect().height / 2 : null;
    });
    if (y == null) throw new Error('entry row not found');
    await page.mouse.click(1757, y);   // the chevron: the details of the entry
    await sleep(1000);
    await page.evaluate(() => {
      const f = [...document.querySelectorAll('vaadin-text-field')].find(x => x.value === 'Mizuki Hayashida');
      f.scrollIntoView({ block: 'start' });
    });
    await sleep(500);
    await fill(page, 'Condition', '{{eval::povCharacter == "Tomáš" and presentCharacters has "Mizuki"}}');
    await sleep(600);
    const top = await page.evaluate(() => {
      const f = [...document.querySelectorAll('vaadin-text-field')].find(x => x.value === 'Mizuki Hayashida');
      return f.getBoundingClientRect().y;
    });
    await snap(page, 'lorebook-condition', { clip: { x: 292, y: Math.max(0, top - 18), width: 1599, height: 700 } });
    await page.setViewportSize(DESKTOP);
    await idle(page, 600);
    // the preview says why an entry is not used: the point of view of the last part is not Tomáš
    await press(page, 'Save', { exact: false, wait: 1500 }).catch(() => {});
    await press(page, 'Preview activation');
    await idle(page, 800);
    const d = dialog(page);
    await fill(d, 'Text of the turn', 'Mizuki follows the lantern along the river.');
    await pick(d, 'Tags of the book', 'Unregistered');
    await idle(page, 1800);
    await snapDialog(page, 'lorebook-condition-preview');
    await dialog(page).getByRole('button', { name: /^(Close|Cancel)$/ }).first().click();
  });
});
