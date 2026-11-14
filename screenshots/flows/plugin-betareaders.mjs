// Beta Readers: installs the plugin, shows its settings and the tab of the chats in the story sidebar (empty, the new chat window,
// two chats, the menu, rename, close and reopen, the personas of the book, delete). It makes chats, so run it on a copy of the
// state (22-arcoutline) that is thrown away; the chain takes the next step (23-betareaders) after it.
// Pictures: the settings panel and the persona library, the empty tab, the new chat window with its options, the conversation with the
// reasoning open, the actions of a message, the personas joined, the settings of a chat, the spoiler limit, the menu, the personas of the book.
import fs from 'node:fs';
import path from 'node:path';
import {
  bookTab,
  dialog,
  field,
  goto,
  idle,
  login,
  main,
  menuItem,
  openBook,
  openTab,
  pick,
  press,
  repo,
  sleep,
  snap,
  snapDialog
} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';
import {betaPersona, betaReaders} from '../dataset/plugins.mjs';

const expect = (ok, what) => { if (!ok) throw new Error('expected: ' + what); console.log('  ok:', what); };

/** Installs the plugin through Admin -> Extensions unless it is there (a full rebuild may have it from step 02). */
async function install(page) {
  await page.getByRole('button', { name: 'Admin' }).click();
  await idle(page, 600);
  await page.getByText('Extensions', { exact: true }).first().click();
  await idle(page, 800);
  if (await page.locator('vaadin-grid-cell-content', { hasText: 'beta-readers' }).count()) {
    console.log('  already installed beta-readers');
    return;
  }
  const jar = path.join(repo, 'marginalia/plugins/beta-readers/target/beta-readers-1.9.0.jar');
  if (!fs.existsSync(jar)) throw new Error('not built: ' + jar);
  await page.locator('vaadin-upload input[type=file]').setInputFiles(jar);
  await idle(page, 4000);
  await page.getByRole('button', { name: 'Refresh' }).click();
  await idle(page, 1500);
  console.log('  installed beta-readers');
}

/** The system messages of the requests the fake model got since `offset` (the log keeps the requests of all runs). */
function systemMessagesSince(offset) {
  const file = path.join(repo, 'screenshots/.run/llm-requests.jsonl');
  const text = fs.readFileSync(file).subarray(offset).toString('utf8');
  return text.split('\n').filter(Boolean).map(line => {
    try { return JSON.parse(line).messages?.[0]?.content ?? ''; } catch { return ''; }
  });
}

await main(async ({ page }) => {
  const logFile = path.join(repo, 'screenshots/.run/llm-requests.jsonl');
  const logStart = fs.existsSync(logFile) ? fs.statSync(logFile).size : 0;
  await login(page);
  await install(page);
  await goto(page, '/');

  // the settings of the plugin and the library of the personas
  await openTab(page, 'Settings');
  await page.getByText('Extension Settings', { exact: true }).first().click();
  await idle(page, 800);
  await page.getByText('Beta Readers', { exact: true }).last().click();
  await sleep(800);
  await snap(page, 'plugin-betareaders-settings');
  await press(page, 'Edit personas');
  await idle(page, 700);
  await snapDialog(page, 'plugin-betareaders-library');
  await press(dialog(page), 'Cancel');
  await idle(page, 500);

  // a persona added in the library is kept when the Settings page is saved (the plugin stores its part when the page has stored its own)
  await press(page, 'Edit personas');
  await idle(page, 700);
  await press(dialog(page), 'Add');
  await idle(page, 600);
  let pd = dialog(page);
  // the model writes the name and the description from a few words
  await field(pd, 'What the persona is like').fill(betaPersona.brief);
  await field(pd, 'What the persona is like').dispatchEvent('change');
  await press(pd, 'Write with the model');
  await page.waitForFunction(value => [...document.querySelectorAll('vaadin-dialog[opened] input')].some(i => i.value === value), betaPersona.name, { timeout: 30000 });
  expect(await field(pd, 'Name').inputValue() === betaPersona.name, 'the model wrote the name');
  expect((await field(pd, 'Description').inputValue()).startsWith('The Linguist.'), 'the model wrote the description');
  await snapDialog(page, 'plugin-betareaders-write-persona');
  await field(pd, 'Name').fill('Rook');
  await field(pd, 'Name').dispatchEvent('change');
  await field(pd, 'Description').fill('Reads for the words and wants to know what they mean.');
  await field(pd, 'Description').dispatchEvent('change');
  await press(pd, 'Save');
  await sleep(500);
  await press(dialog(page), 'Save');
  await sleep(500);
  expect(await field(page, 'Persona library').inputValue() === 'Personas: 7 (edited)', 'the library has 7 personas, not saved yet');
  await page.getByRole('button', { name: 'Save', exact: true }).first().click();
  await idle(page, 1000);
  await page.reload({ waitUntil: 'load' });
  await idle(page, 1500);
  await login(page).catch(() => {});
  await openTab(page, 'Settings');
  await page.getByText('Extension Settings', { exact: true }).first().click();
  await idle(page, 800);
  await page.getByText('Beta Readers', { exact: true }).last().click();
  await sleep(800);
  expect(await field(page, 'Persona library').inputValue() === 'Personas: 7', 'the persona is still there after a reload');

  // and it goes again, so that the pictures after it show the six
  await press(page, 'Edit personas');
  await idle(page, 700);
  await dialog(page).locator('vaadin-grid-cell-content', { hasText: 'Rook' }).first().click();
  await press(dialog(page), 'Delete');
  await idle(page, 500);
  await press(dialog(page), 'Yes', { exact: false }).catch(async () => press(dialog(page), 'Confirm', { exact: false }));
  await sleep(500);
  await press(dialog(page), 'Save');
  await sleep(500);
  await page.getByRole('button', { name: 'Save', exact: true }).first().click();
  await idle(page, 1000);
  await page.reload({ waitUntil: 'load' });
  await idle(page, 1500);
  await login(page).catch(() => {});
  await openTab(page, 'Settings');
  await page.getByText('Extension Settings', { exact: true }).first().click();
  await idle(page, 800);
  await page.getByText('Beta Readers', { exact: true }).last().click();
  await sleep(800);
  expect(await field(page, 'Persona library').inputValue() === 'Personas: 6', 'the persona is deleted and the library is stored again');

  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 700);
  await sideTab(page, 'Beta Readers');
  await sleep(500);
  expect(await page.getByText('No chats yet. Start one with the + button.').count() === 1, 'the empty tab says there are no chats');
  await snap(page, 'plugin-betareaders-tab');

  const plus = page.locator('vaadin-button[aria-label="New chat"]').first();
  const menu = async () => { await page.locator('vaadin-button[aria-label="Chats"]').first().click(); await idle(page, 500); };

  // a group is the default; the options are folded
  await plus.click();
  await idle(page, 700);
  await snapDialog(page, '_br-new-group');
  let d = dialog(page);
  await d.getByRole('checkbox', { name: 'Athena', exact: true }).check();
  await d.getByRole('checkbox', { name: 'Vesper', exact: true }).check();
  await d.getByText('Story, medium, model and format', { exact: true }).click();
  await idle(page, 400);
  await snapDialog(page, 'plugin-betareaders-new-chat');
  await press(d, 'Create');
  await sleep(700);
  expect(await page.getByRole('tab', { name: 'Athena, Vesper', exact: true }).count() === 1, 'a tab "Athena, Vesper"');
  await snap(page, '_br-group');

  // the conversation: the first message brings the story, the second one nothing (nothing is new)
  const box = page.locator('textarea[placeholder="Write to the beta readers..."]');
  const say = async text => {
    await box.fill(text);
    await box.dispatchEvent('change');
    await page.getByRole('button', { name: 'Send', exact: true }).click();
  };
  const [first, second, quill] = betaReaders;
  expect(await page.getByText('Include the story (5 parts)').count() === 1, 'the first message offers the story, 5 parts');
  expect(await page.getByText('Context:', { exact: false }).count() >= 1, 'the context counter is shown');
  // the counter is the size of the request the next message would make: with the story, and with what is typed
  const tokens = async () => Number((await page.getByText('Context:', { exact: false }).first().innerText()).match(/Context: ([\d,]+)/)[1].replace(/,/g, ''));
  const withStory = await tokens();
  await page.getByText('Include the story (5 parts)').click();
  await sleep(1200);
  const withoutStory = await tokens();
  expect(withStory > withoutStory + 100, `the counter counts the story that would be sent (${withStory} against ${withoutStory} without it)`);
  await box.fill('The moon is the point of the first chapter. '.repeat(60));
  await sleep(2000);
  const typed = await tokens();
  expect(typed > withoutStory + 100, `the counter follows what is typed (${typed} against ${withoutStory})`);
  await box.fill('');
  await page.getByText('Include the story (5 parts)').click();
  await sleep(1200);
  expect(await tokens() === withStory, 'the counter is back with the story ticked and nothing typed');
  await say(first.question);
  await page.getByText('I would cut the tourists', { exact: false }).first().waitFor({ timeout: 30000 });
  await sleep(800);
  expect(await page.getByText('Parts 1-5 sent', { exact: false }).count() === 1, 'the message says which parts it sent');
  expect(await page.getByText('The tourists are the point.', { exact: false }).count() === 1, 'the last message of the turn is shown');
  expect(await page.getByText('Reasoning', { exact: true }).count() === 1, 'the reasoning is above the turn');
  expect(await page.getByText('Athena starts, ', { exact: false }).count() === 0, 'the reasoning is collapsed');
  expect(await page.getByText('Include the latest parts (none new)').count() === 1, 'nothing is new after the story was sent');
  await snap(page, '_br-chat');
  await page.getByText('Reasoning', { exact: true }).click();
  await sleep(400);
  await snap(page, 'plugin-betareaders-chat');
  await page.getByText('Reasoning', { exact: true }).click();
  await say(second.question);
  await page.getByText('Let him look for the status window', { exact: false }).first().waitFor({ timeout: 30000 });
  await sleep(600);
  console.log('  (messages that say which parts they sent:', await page.getByText('Parts ', { exact: false }).filter({ hasText: 'sent' }).count() + ')');
  await snap(page, '_br-chat-second');

  // editing and deleting what was said, and asking again
  const confirm = async () => { await press(dialog(page), 'Yes', { exact: false }).catch(async () => press(dialog(page), 'Confirm', { exact: false })); await sleep(700); };
  const button = label => page.locator(`vaadin-button[aria-label="${label}"]`);
  await snap(page, 'plugin-betareaders-actions');
  await button('Edit message').first().click();
  await idle(page, 600);
  await dialog(page).locator('textarea').first().fill('Be honest: does the first chapter hook you at all?');
  await dialog(page).locator('textarea').first().dispatchEvent('change');
  await press(dialog(page), 'Save');
  await sleep(600);
  expect(await page.getByText('hook you at all?', { exact: false }).count() === 1, 'a message of the user is edited');
  await button('Delete message').nth(1).click();   // the first is the delete button of the user card, the second is Athena's
  await confirm();
  expect(await page.getByText('quiet, confident opening', { exact: false }).count() === 0, 'one message of a turn is deleted');
  expect(await page.getByText('I would cut the tourists', { exact: false }).count() === 1, 'the rest of the turn stays');
  await page.getByRole('button', { name: 'Regenerate', exact: true }).click();
  // the old reply goes at once; the new one is there when the last turn can be regenerated again
  await sleep(800);
  await page.getByRole('button', { name: 'Regenerate', exact: true }).waitFor({ timeout: 30000 });
  await sleep(500);
  expect(await page.getByText('Let him look for the status window', { exact: false }).count() === 1, 'the last reply is asked for again, and there is one');
  expect(await page.getByRole('button', { name: 'Regenerate', exact: true }).count() === 1, 'only the last turn can be regenerated');
  await button('Delete message').first().click();   // the first message of the user sent the story
  await confirm();
  expect(await page.getByText('Parts 1-5 sent').count() === 0, 'the message that sent the story is deleted');
  expect(await page.getByText('Include the story (5 parts)').count() === 1, 'the story is to be sent again');

  // personas join and leave, are updated from the library
  await page.getByText('Personas (2)', { exact: true }).click();
  await sleep(400);
  await snap(page, '_br-personas');
  await page.getByRole('button', { name: 'Bring in a persona' }).click();
  await idle(page, 600);
  await pick(dialog(page), 'Persona', 'Nox');
  await dialog(page).locator('textarea').first().fill('Nox has read ahead and is not telling.');
  await dialog(page).locator('textarea').first().dispatchEvent('change');
  await snapDialog(page, 'plugin-betareaders-bring-in');
  await press(dialog(page), 'Bring in');
  await sleep(700);
  expect(await page.getByText('Nox joined the conversation.').count() === 1, 'a persona joins and the history says so');
  expect(await page.getByText('Personas (3)', { exact: true }).count() === 1, 'three personas are present');
  await button('Send away').nth(1).click();   // Vesper
  await idle(page, 600);
  await dialog(page).locator('textarea').first().fill('Vesper has to go.');
  await dialog(page).locator('textarea').first().dispatchEvent('change');
  await press(dialog(page), 'Send away');
  await sleep(700);
  expect(await page.getByText('Vesper left the conversation.').count() === 1, 'a persona leaves and the history says so');
  expect(await page.getByText('(left)').count() >= 1, 'the persona that left is greyed and marked');
  await button('Update from the library').first().click();
  await sleep(700);
  expect(await page.getByText('is updated from the library.', { exact: false }).count() >= 1, 'a persona is updated from the library');
  await say('Nox, you first this time.');
  await page.getByText('Let him look for the status window', { exact: false }).first().waitFor({ timeout: 30000 });
  await sleep(1500);
  await snap(page, 'plugin-betareaders-joined');

  // the settings of the chat: another medium leaves a line in the history
  await menu();
  await menuItem(page, 'Chat settings').click();
  await idle(page, 600);
  await pick(dialog(page), 'Medium', 'Manga / comic');
  await field(dialog(page), 'Scene').fill('A rainy bar after closing time. The chairs are on the tables.');
  await field(dialog(page), 'Scene').dispatchEvent('change');
  await snapDialog(page, 'plugin-betareaders-chat-settings');
  await press(dialog(page), 'Save');
  await sleep(800);
  expect(await page.getByText('The scene is now: A rainy bar after closing time.').count() === 0, 'the scene change leaves no line in the history');
  await say('One more thing: the ending?');
  // the reply is there when the last turn offers to be regenerated
  await sleep(500);
  await page.getByRole('button', { name: 'Regenerate' }).waitFor({ timeout: 30000 });
  await sleep(800);
  const systems = systemMessagesSince(logStart).filter(system => system.includes('beta reader'));
  expect(systems.some(system => system.includes('The conversation is an online chat')), 'the built-in scene (an online chat) is in the system prompt of a chat that sets none');
  expect(systems.some(system => system.includes('The scene of the conversation:\nA rainy bar after closing time.')), 'the scene of the chat is in its system prompt');
  expect(await page.getByText('From now on the text is read as: Manga / comic').count() === 1, 'the medium change is a line of the history');
  expect(await page.getByText('Medium: Manga / comic').count() === 1, 'the chat shows its medium');
  expect(await page.getByText('There is no reply yet.').count() === 0, 'a change of the medium does not leave a question without a reply');
  await snap(page, '_br-chat-medium');

  // a scene: what happens, not something the author says; a card of its own, and the personas react to it
  await box.fill('The lights go out and the rain on the windows gets louder. Somebody laughs in the dark.');
  await box.dispatchEvent('change');
  await page.getByRole('button', { name: 'Send as scene', exact: true }).click();
  await page.getByText('The power cut suits the book', { exact: false }).first().waitFor({ timeout: 30000 });
  await sleep(1200);
  expect(await page.getByText('The lights go out', { exact: false }).count() >= 1, 'the scene is in the history');
  expect(systemMessagesSince(logStart).length > 0, 'the model was asked');
  // the pointer leaves the button (its tooltip covers the line below)
  await page.mouse.move(1100, 300);
  await sleep(900);
  await snap(page, 'plugin-betareaders-scene');

  // the spoiler limit and the first part: the personas read parts 2-3 only, then up to part 5
  await plus.click();
  await idle(page, 700);
  d = dialog(page);
  await d.getByRole('checkbox', { name: 'Ember', exact: true }).check();
  await field(d, 'Title').fill('Spoiler free');
  await field(d, 'Title').dispatchEvent('change');
  await d.getByText('Story, medium, model and format', { exact: true }).click();
  await idle(page, 400);
  await field(d, 'Personas read up to part').fill('2');
  await field(d, 'Personas read up to part').dispatchEvent('change');
  await field(d, 'Start from part').fill('3');
  await field(d, 'Start from part').dispatchEvent('change');
  await press(d, 'Create');
  expect(await page.getByText('The first part can\'t come after the last one.').count() >= 1, 'a first part after the last one is refused');
  await field(d, 'Personas read up to part').fill('3');
  await field(d, 'Personas read up to part').dispatchEvent('change');
  await field(d, 'Start from part').fill('2');
  await field(d, 'Start from part').dispatchEvent('change');
  await snapDialog(page, '_br-new-spoiler');
  await press(d, 'Create');
  await sleep(800);
  expect(await page.getByText('Reads up to part 3').count() === 1, 'the chat says how far the personas read');
  expect(await page.getByText('Include the story (2 parts)').count() === 1, 'two parts are offered: 2 and 3');
  await say('Does the beginning work without the rest?');
  await page.getByText('I would cut the tourists', { exact: false }).first().waitFor({ timeout: 30000 });
  await sleep(800);
  expect(await page.getByText('Parts 2-3 sent').count() === 1, 'parts 2-3 were sent');
  expect(await page.getByText('The personas have read: parts 2-3').count() === 1, 'the panel says what the personas have read');
  await snap(page, 'plugin-betareaders-spoiler');
  // the last part is changed under the text box, no window needed (it is saved when the typing pauses)
  await field(page, 'Last part').fill('5');
  await field(page, 'Last part').dispatchEvent('change');
  await sleep(1800);
  expect(await page.getByText('Include the latest parts (2 new)').count() === 1, 'a wider limit offers the two parts that were held back');
  await say('And now with the end?');
  await page.getByText('I would cut the tourists', { exact: false }).nth(1).waitFor({ timeout: 30000 });
  await sleep(800);
  expect(await page.getByText('The personas have read: parts 2-5').count() === 1, 'now the personas have read parts 2-5');
  await snap(page, '_br-spoiler-wider');

  // a chat with one persona, a title of its own
  await plus.click();
  await idle(page, 700);
  d = dialog(page);
  await d.getByRole('radio', { name: 'One persona', exact: true }).check();
  await idle(page, 400);
  await d.getByRole('radio', { name: 'Quill', exact: true }).check();
  await field(d, 'Title').fill('Line edit');
  await field(d, 'Title').dispatchEvent('change');
  await snapDialog(page, '_br-new-single');
  await press(d, 'Create');
  await sleep(700);
  expect(await page.getByRole('tab', { name: 'Line edit', exact: true }).count() === 1, 'a tab "Line edit"');
  await snap(page, '_br-single');
  await say(quill.question);
  await page.getByText('exclaimed', { exact: false }).first().waitFor({ timeout: 30000 });
  await sleep(600);
  await snap(page, '_br-single-chat');

  // nothing is created without a persona
  await plus.click();
  await idle(page, 700);
  await press(dialog(page), 'Create');
  expect(await page.getByText('Choose at least one persona').count() >= 1, 'a group without a persona is refused');
  await press(dialog(page), 'Cancel');

  // the menu
  await menu();
  await snap(page, 'plugin-betareaders-menu');
  await menuItem(page, 'Rename chat').click();
  await idle(page, 600);
  d = dialog(page);
  await field(d, 'Title').fill('Second pass');
  await field(d, 'Title').dispatchEvent('change');
  await press(d, 'Save');
  await sleep(500);
  expect(await page.getByRole('tab', { name: 'Second pass', exact: true }).count() === 1, 'the chat is renamed');

  // close it, open it again from the menu
  await menu();
  await menuItem(page, 'Close chat').click();
  await sleep(600);
  expect(await page.getByRole('tab', { name: 'Second pass', exact: true }).count() === 0, 'the closed chat has no tab');
  await menu();
  await menuItem(page, 'Closed chats').click();
  await idle(page, 500);
  await snap(page, '_br-closed');
  await page.getByRole('menuitem', { name: 'Second pass' }).click();
  await sleep(600);
  expect(await page.getByRole('tab', { name: 'Second pass', exact: true }).count() === 1, 'the chat is open again');

  // the personas of the book
  await menu();
  await menuItem(page, 'Personas of this book').click();
  await idle(page, 700);
  await snapDialog(page, 'plugin-betareaders-book-personas');
  await press(dialog(page), 'Add');
  await idle(page, 600);
  d = dialog(page);
  await field(d, 'Name').fill('Mara');
  await field(d, 'Name').dispatchEvent('change');
  await field(d, 'Description').fill('A reader of this book only, who loves the minor characters.');
  await field(d, 'Description').dispatchEvent('change');
  await press(d, 'Save');
  await sleep(500);
  await press(dialog(page), 'Save');
  await sleep(600);
  await plus.click();
  await idle(page, 700);
  expect(await dialog(page).getByRole('checkbox', { name: 'Mara (this book)', exact: true }).count() === 1, 'the persona of the book can be chosen, marked as its own');
  await snapDialog(page, '_br-new-with-book-persona');
  await press(dialog(page), 'Cancel');

  // the chats stay after the book is opened again
  await page.reload({ waitUntil: 'load' });
  await idle(page, 1500);
  await login(page).catch(() => {});
  await openBook(page).catch(() => {});
  await bookTab(page, 'Story');
  await sideTab(page, 'Beta Readers');
  expect(await page.getByRole('tab', { name: 'Second pass', exact: true }).count() === 1, 'the chats are kept');
  expect(await page.getByRole('tab', { name: 'Athena, Vesper', exact: true }).count() === 1, 'the group is kept');
  await page.getByRole('tab', { name: 'Athena, Vesper', exact: true }).click();
  await sleep(800);
  expect(await page.getByText('Let him look for the status window', { exact: false }).count() === 1, 'the conversation is kept');

  // delete
  await menu();
  await menuItem(page, 'Delete chat').click();
  await idle(page, 500);
  await snapDialog(page, '_br-delete');
  await press(dialog(page), 'Yes', { exact: false }).catch(async () => press(dialog(page), 'Confirm', { exact: false }));
  await sleep(600);
  expect(await page.getByRole('tab', { name: 'Athena, Vesper', exact: true }).count() === 0, 'the group is deleted');
  expect(await page.getByRole('tab', { name: 'Second pass', exact: true }).count() === 1, 'the other chat stays');
  await snap(page, '_br-after-delete');
});
