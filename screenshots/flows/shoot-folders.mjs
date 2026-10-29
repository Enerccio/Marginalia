// What 1.6.0 added around the books: folders (the list, their settings, the Organise window), the mass changes, the settings a book
// inherits from its folders, the minimized tabs and the mass delete of the story, the phone list with folders, and the help
// assistant that may look at the books. Run on a COPY of 13-db-backup, it is discarded: the folders are made through the real UI
// (Fan fiction / Harry Potter, Originals, a second book) and the book is moved into the folder with the style.
// Pictures: books-folders, folder-settings, mass-change-model-protocol, mass-change-language, organise-dialog,
// book-prompts-inherited, story-minimized-tabs, story-mass-delete, phone-folders, help-assistant-books.
import {
    bookTab,
    closeDialog,
    dialog,
    fill,
    idle,
    launch,
    llm,
    login,
    main,
    menuItem,
    openTab,
    pick,
    press,
    section,
    sleep,
    snap,
    snapDialog
} from '../lib/shot.mjs';
import {openStoryMenu} from '../lib/story.mjs';

const BOOK = 'Harry Potter and Half Blood Prince - FDM';
const SECOND = 'The Lantern Road';

/** The button with this icon (`cog`, `pencil`, `folder-open`, `star-o`...) in the grid row that has this text. */
async function rowButton(page, text, icon) {
  const cell = page.locator('vaadin-grid-cell-content').filter({ hasText: text }).first();
  const y = (await cell.boundingBox()).y;
  const buttons = page.locator(`vaadin-grid vaadin-button:has(vaadin-icon[icon$=":${icon}"])`);
  let best = null;
  for (let i = 0; i < await buttons.count(); i++) {
    const box = await buttons.nth(i).boundingBox();
    if (box && Math.abs(box.y - y) < 20 && (best === null || Math.abs(box.y - y) < best.distance)) best = { index: i, distance: Math.abs(box.y - y) };
  }
  if (!best) throw new Error(`no ${icon} button in the row of ${text}`);
  return buttons.nth(best.index);
}

const newFolder = async (page, name) => {
  await press(page, 'New folder');
  await dialog(page).locator('input').first().fill(name);
  await press(dialog(page), 'Yes');
  await idle(page, 800);
};
/** Opens a folder of the list (its name is a button). */
const enter = async (page, name) => {
  await page.locator('vaadin-grid vaadin-button:visible').filter({ hasText: name }).first().click();
  await idle(page, 900);
};
const toTop = async page => {
  await page.getByRole('button', { name: 'Books', exact: true }).first().click();
  await idle(page, 900);
};

await main(async ({ page }) => {
  await login(page);
  await openTab(page, 'Books');

  await section(page, 'make', async () => {
    await newFolder(page, 'Fan fiction');
    await newFolder(page, 'Originals');
    await enter(page, 'Fan fiction');
    await newFolder(page, 'Harry Potter');
    await toTop(page);
    // a second book, at the top level
    await press(page, 'Add Book');
    await dialog(page).locator('input').first().fill(SECOND);
    await press(dialog(page), 'Yes');
    await page.getByText('Branch view', { exact: true }).first().waitFor();
    await idle(page, 800);
    await press(page, 'Exit');
    await idle(page, 900);
    // the story book goes into Fan fiction / Harry Potter
    await (await rowButton(page, BOOK, 'folder-open')).click();
    await idle(page, 900);
    await snap(page, '_move-to-folder');
    const target = dialog(page).getByLabel('Folder', { exact: true }).first();
    await target.click();
    await target.fill('Harry Potter');
    await page.getByRole('option').filter({ hasText: 'Harry Potter' }).first().click();
    await sleep(300);
    await press(dialog(page), 'OK');
    await idle(page, 900);
    // the second book is a favorite: the top level then shows the Favorites folder
    await (await rowButton(page, SECOND, 'star-o')).click();
    await idle(page, 900);
  });

  await section(page, 'folder-settings', async () => {
    await (await rowButton(page, 'Fan fiction', 'cog')).click();
    await idle(page, 1200);
    await fill(dialog(page), 'Description', 'Stories in worlds that belong to somebody else.');
    await fill(dialog(page), 'Style', 'Close and unhurried. Long sentences that take their time, concrete details, no summarizing of feelings: show what the character does.');
    await fill(dialog(page), 'Tense', 'Past tense');
    await sleep(500);
    await snapDialog(page, 'folder-settings', -1);
    await press(dialog(page), 'Save');
    await idle(page, 1200);
  });

  await section(page, 'list', async () => {
    await toTop(page).catch(() => {});
    await snap(page, 'books-folders');
  });

  await section(page, 'mass-changes', async () => {
    await press(page, 'Mass changes');
    await idle(page, 500);
    await page.getByRole('menuitem', { name: 'Mass Change Model/Protocol' }).click();
    await idle(page, 1500);
    await snapDialog(page, 'mass-change-model-protocol', -1);
    await closeDialog(page);
    await press(page, 'Mass changes');
    await idle(page, 500);
    await page.getByRole('menuitem', { name: 'Mass Change Language' }).click();
    await idle(page, 1500);
    await snapDialog(page, 'mass-change-language', -1);
    await closeDialog(page);
  });

  await section(page, 'organise', async () => {
    await press(page, 'Organise');
    await idle(page, 1800);
    await snapDialog(page, '_organise-open', -1);
    // the right pane goes into Fan fiction, the cursor stays on the second book of the left pane, which is marked
    await page.keyboard.press('ArrowRight');
    await sleep(300);
    await page.keyboard.press('ArrowDown');
    await page.keyboard.press('Enter');
    await idle(page, 900);
    await page.keyboard.press('ArrowLeft');
    await sleep(300);
    await page.keyboard.press('Space');
    await sleep(500);
    await snapDialog(page, 'organise-dialog', -1);
    await closeDialog(page);
  });

  // the book of the story, now in Fan fiction / Harry Potter: it inherits the style of the folder
  await section(page, 'inherited', async () => {
    await toTop(page).catch(() => {});
    await enter(page, 'Fan fiction');
    await enter(page, 'Harry Potter');
    await (await rowButton(page, BOOK, 'pencil')).click();
    await page.getByText('Branch view', { exact: true }).first().waitFor();
    await idle(page, 1000);
    await bookTab(page, 'Prompts');
    await sleep(600);
    await snapDialog(page, 'book-prompts-inherited', -1);
  });

  await section(page, 'story', async () => {
    await bookTab(page, 'Story');
    await sleep(800);
    // two tabs of the sidebar go to the bar at the edge
    for (let i = 0; i < 2; i++) {
      await page.getByRole('button', { name: 'Minimize', exact: true }).first().click();
      await idle(page, 700);
    }
    await sleep(500);
    await snap(page, 'story-minimized-tabs');
    // and back
    for (let i = 0; i < 2; i++) {
      await page.locator('[aria-label^="Restore"], [title^="Restore"]').first().click();
      await idle(page, 700);
    }

    await openStoryMenu(page);
    await menuItem(page, 'Mass delete').click();
    await idle(page, 900);
    const boxes = page.locator('.chat-message input[type="checkbox"]');
    await boxes.nth(1).click();
    await boxes.nth(2).click();
    await idle(page, 600);
    await snap(page, 'story-mass-delete');
    await page.getByRole('button', { name: 'Cancel', exact: true }).last().click();
    await idle(page, 700);
    await press(page, 'Exit');
    await idle(page, 800);
  });

  await section(page, 'assistant', async () => {
    await llm('/_reset', {});
    await page.getByRole('button', { name: 'Admin', exact: true }).first().click();
    await idle(page, 800);
    await openTab(page, 'Help assistant');
    const panel = page.getByRole('tabpanel').filter({ hasText: 'Reset to default' });
    await pick(panel, 'Model', 'Local LLM');
    await sleep(400);
    await press(panel, 'Save');
    await idle(page, 800);
    await page.reload();
    await idle(page, 1500);
    await page.locator('.help-fab').click();
    await idle(page, 800);
    await page.getByText('Let the assistant look at my books', { exact: true }).click();
    await idle(page, 700);
    await llm('/_queue', { toolCalls: [{ name: 'search_books', arguments: { query: 'Draconia' } }] });
    await llm('/_queue', { reply: 'I found it: **Harry Potter and Half Blood Prince - FDM**, in the folder **Fan fiction / Harry Potter**. The words are in the text of the story.' });
    const ask = page.locator('.help-panel input[type="text"]');
    await ask.fill('Which of my books mentions Draconia?');
    await ask.press('Enter');
    await page.locator('.help-message').getByText('I found it').waitFor();
    await idle(page, 1500);
    await snap(page, 'help-assistant-books', { locator: page.locator('.help-panel'), pad: 10 });
  });

  // the phone list, with the folders
  await section(page, 'phone', async () => {
    const phone = await launch({ phone: true });
    try {
      await login(phone.page);
      await sleep(1000);
      await snap(phone.page, 'phone-folders');
    } finally {
      await phone.browser.close();
    }
  });
});
