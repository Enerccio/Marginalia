// Beta Readers: the tab of the chats in the story sidebar (empty, the new chat window, two chats, the menu, rename, close and reopen,
// the personas of the book, delete). It makes chats, so run it on a copy of the state (22-arcoutline) that is thrown away.
import {bookTab, dialog, idle, login, main, menuItem, openBook, press, sleep, snap, snapDialog, field} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';
import {betaReaders} from '../dataset/plugins.mjs';

const expect = (ok, what) => { if (!ok) throw new Error('expected: ' + what); console.log('  ok:', what); };

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 700);
  await sideTab(page, 'Beta Readers');
  await sleep(500);
  expect(await page.getByText('No chats yet. Start one with the + button.').count() === 1, 'the empty tab says there are no chats');
  await snap(page, '_br-empty');

  const plus = page.locator('vaadin-button[aria-label="New chat"]').first();
  const menu = async () => { await page.locator('vaadin-button[aria-label="Chats"]').first().click(); await idle(page, 500); };

  // a group is the default; the options are folded
  await plus.click();
  await idle(page, 700);
  await snapDialog(page, '_br-new-group');
  let d = dialog(page);
  await d.getByRole('checkbox', { name: 'Athena', exact: true }).check();
  await d.getByRole('checkbox', { name: 'Vesper', exact: true }).check();
  await d.getByText('Medium, model and format', { exact: true }).click();
  await idle(page, 400);
  await snapDialog(page, '_br-new-group-options');
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
  await say(first.question);
  await page.getByText('I would cut the weather', { exact: false }).first().waitFor({ timeout: 30000 });
  await sleep(800);
  expect(await page.getByText('Parts 1-5 sent', { exact: false }).count() === 1, 'the message says which parts it sent');
  expect(await page.getByText('The weather is the point.', { exact: false }).count() === 1, 'the last message of the turn is shown');
  expect(await page.getByText('Reasoning', { exact: true }).count() === 1, 'the reasoning is above the turn');
  expect(await page.getByText('Athena starts, ', { exact: false }).count() === 0, 'the reasoning is collapsed');
  expect(await page.getByText('Include the latest parts (none new)').count() === 1, 'nothing is new after the story was sent');
  await snap(page, '_br-chat');
  await page.getByText('Reasoning', { exact: true }).click();
  await sleep(400);
  await snap(page, '_br-chat-reasoning');
  await page.getByText('Reasoning', { exact: true }).click();
  await say(second.question);
  await page.getByText('Let the motives show', { exact: false }).first().waitFor({ timeout: 30000 });
  await sleep(600);
  expect(await page.getByText('Parts ', { exact: false }).filter({ hasText: 'sent' }).count() === 1, 'the second message sent no parts');
  await snap(page, '_br-chat-second');

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
  await snap(page, '_br-menu');
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
  await snapDialog(page, '_br-book-personas');
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
  expect(await page.getByText('Let the motives show', { exact: false }).count() === 1, 'the conversation is kept');

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
