// The Conlang Translator: the settings panel, the language editor (rules, lexicon), the review of the differences, the book's choice, the story with a
// translated speech, the range dialog, the export dialog and the viewer. Run by bin/shoot-conlang.sh (it writes the language and the translated book first).
import fs from 'node:fs';
import path from 'node:path';
import {
  bookTab,
  closeDialog,
  CONTENT,
  DESKTOP,
  dialog,
  fill,
  goto,
  idle,
  login,
  main,
  menuItem,
  openBook,
  openTab,
  press,
  section,
  sleep,
  snap,
  snapDialog,
} from '../lib/shot.mjs';
import {openStoryMenu, sideTab, widenSidebar} from '../lib/story.mjs';

await main(async ({ page }) => {
  await login(page);

  await section(page, 'settings', async () => {
    await openTab(page, 'Settings');
    await page.getByText('Extension Settings', { exact: true }).first().click();
    await idle(page, 800);
    await page.getByText('Conlang Translator', { exact: true }).first().click();
    await sleep(1000);
    await snap(page, 'plugin-conlang-settings', { clip: CONTENT });
    // the editor of a language
    await page.locator('vaadin-grid-cell-content').filter({ hasText: /^Vrakht$/ }).first().click();
    await sleep(400);
    await page.getByRole('button', { name: 'Edit', exact: true }).click();
    await idle(page, 1200);
    await snapDialog(page, 'plugin-conlang-editor', -1);
    await dialog(page).getByRole('tab', { name: 'Rules', exact: true }).click();
    await sleep(800);
    await snapDialog(page, 'plugin-conlang-rules', -1);
    await dialog(page).getByRole('tab', { name: 'Lexicon', exact: true }).click();
    await sleep(1000);
    await snapDialog(page, 'plugin-conlang-lexicon', -1);
    // the Names tab: the words a category of names is made of, and twenty names made of them
    // a taller window, so that the grid shows more than three words
    await page.setViewportSize({ width: DESKTOP.width, height: 1180 });
    await sleep(900);
    await dialog(page).getByRole('tab', { name: 'Names', exact: true }).click();
    await sleep(900);
    // only the words of the category that the names are made of
    await dialog(page).locator('input[placeholder="All categories"]').last().click({ force: true });
    await page.getByRole('option', { name: 'People and society', exact: true }).click();
    await sleep(600);
    await dialog(page).getByRole('button', { name: 'Preview names', exact: true }).click();
    await sleep(1500);
    await snapDialog(page, 'plugin-conlang-names', -1);
    await page.setViewportSize(DESKTOP);
    await sleep(600);
    await closeDialog(page);
  });

  // the panel of the settings, with a language selected: the shared start of the next sections
  const panel = async () => {
    await openTab(page, 'Settings');
    await page.getByText('Extension Settings', { exact: true }).first().click();
    await idle(page, 800);
    const header = page.getByText('Conlang Translator', { exact: true }).first();
    await header.click();
    await sleep(800);
    // the accordion may have been left open by the section before: a second click closes it
    if (await page.getByRole('button', { name: 'New language' }).count() === 0) await header.click();
    await sleep(600);
  };
  // a picture of the speech that was clicked with the popover under it: cut around the mark
  const snapAround = async (name, mark) => {
    const box = await mark.boundingBox();
    const x = Math.max(0, box.x - 300);
    await snap(page, name, { clip: { x, y: Math.max(0, box.y - 110), width: Math.min(1000, 1920 - x), height: 330 } });
  };
  const center = mark => mark.evaluate(el => el.scrollIntoView({ block: 'center' }));
  const select = async name => {
    // a click on the selected row selects it away: click again when the buttons say that nothing is selected
    const row = page.locator('vaadin-grid-cell-content').filter({ hasText: new RegExp('^' + name.replace(/[()]/g, '\\$&') + '$') }).first();
    await row.click();
    await sleep(400);
    if (!(await page.getByRole('button', { name: 'Edit', exact: true }).isEnabled())) {
      await row.click();
      await sleep(400);
    }
  };

  await section(page, 'review', async () => {
    await panel();
    await select('Vrakht');
    await page.getByRole('button', { name: 'Compare', exact: true }).click();
    await idle(page, 700);
    await press(dialog(page), 'Review the differences');
    await idle(page, 1500);
    await dialog(page).locator('vaadin-grid-cell-content').filter({ hasText: /^Syntax and grammar$/ }).first().click();
    await sleep(800);
    await snapDialog(page, 'plugin-conlang-review', -1);
    await closeDialog(page);
  });

  await section(page, 'designer', async () => {
    await panel();
    await select('Vrakht');
    await page.getByRole('button', { name: 'Edit', exact: true }).click();
    await idle(page, 1200);
    await dialog(page).getByText('Design with AI', { exact: true }).click();
    await sleep(700);
    await snap(page, 'plugin-conlang-designer-menu', { clip: { x: 370, y: 80, width: 1230, height: 430 } });
    await page.keyboard.press('Escape');
    await sleep(400);
    await closeDialog(page);
    await page.getByRole('button', { name: 'Design a language', exact: true }).click();
    await idle(page, 1000);
    await snapDialog(page, 'plugin-conlang-wizard', -1);
    await closeDialog(page);
  });

  await section(page, 'book', async () => {
    await openBook(page);
    await bookTab(page, 'Prompts');
    await idle(page, 1000);
    await page.getByText('Conlang Translator', { exact: true }).last().evaluate(el => el.scrollIntoView({ block: 'start' }));
    await sleep(500);
    await snap(page, 'plugin-conlang-book', { clip: { x: 16, y: 120, width: 1888, height: 790 } });
  });

  await section(page, 'story', async () => {
    await bookTab(page, 'Story');
    await idle(page, 1500);
    const mark = page.locator('mark.conlang-passage').first();
    await center(mark);
    await sleep(500);
    await mark.click();
    await page.locator('.translator-source-box').first().waitFor({ timeout: 10000 });
    await sleep(500);
    await snapAround('plugin-conlang-story', mark);
    await page.keyboard.press('Escape');
    await sleep(300);
  });

  await section(page, 'source', async () => {
    await bookTab(page, 'Story');
    await idle(page, 1200);
    const mark = page.locator('mark.conlang-passage').first();
    await center(mark);
    await sleep(500);
    await mark.click();
    await page.locator('.translator-source-box').first().waitFor({ timeout: 10000 });
    await page.getByRole('button', { name: 'Edit the source', exact: true }).click();
    await idle(page, 900);
    await snapDialog(page, 'plugin-conlang-source');
    await closeDialog(page);
    await page.keyboard.press('Escape');
    await sleep(300);
  });

  // the Conlang tab of the story: the speakers with the understanding of the characters that do not speak the language
  await section(page, 'tab', async () => {
    await bookTab(page, 'Story');
    await widenSidebar(page, 700);
    await sideTab(page, 'Conlang');
    await idle(page, 1000);
    await snap(page, 'plugin-conlang-tab', { clip: { x: 16, y: 130, width: 690, height: 780 } });
  });

  await section(page, 'search-replace', async () => {
    await bookTab(page, 'Story');
    await widenSidebar(page, 700);
    await sideTab(page, 'Conlang');
    await idle(page, 800);
    await page.getByRole('button', { name: 'Search and replace in the sources', exact: true }).click();
    await idle(page, 900);
    await fill(page, 'Search for', 'Paskal');
    await fill(page, 'Replace with', 'Paskel');
    await dialog(page).getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByText('Found ', { exact: false }).first().waitFor({ timeout: 30000 }).catch(() => {});
    await idle(page, 1200);
    // the row of the first speech opens: the matches in it
    await dialog(page).locator('vaadin-grid-tree-toggle').first().click().catch(() => {});
    await idle(page, 900);
    await snapDialog(page, 'plugin-conlang-search-replace');
    await closeDialog(page);
  });

  await section(page, 'range', async () => {
    await openStoryMenu(page);
    await menuItem(page, 'Translate parts').click();
    await idle(page, 900);
    await snapDialog(page, 'plugin-conlang-range');
    await closeDialog(page);
  });

  await section(page, 'export', async () => {
    await openStoryMenu(page);
    await menuItem(page, 'Export story').click();
    await idle(page, 900);
    await snapDialog(page, 'plugin-conlang-export');
    await closeDialog(page);
  });

  await section(page, 'viewer', async () => {
    const uuid = fs.readFileSync(path.join(path.dirname(new URL(import.meta.url).pathname), '..', '.run', 'book-uuid.txt'), 'utf8').trim();
    await goto(page, '/view/' + uuid);
    await page.getByLabel('Username', { exact: true }).fill('admin');
    await page.getByLabel('Password', { exact: true }).fill('marginalia');
    await page.getByRole('button', { name: 'Login', exact: true }).click();
    await idle(page, 2500);
    const mark = page.locator('mark.conlang-passage').first();
    await center(mark);
    await sleep(500);
    await mark.click();
    await page.locator('.translator-source-box').first().waitFor({ timeout: 10000 });
    await sleep(500);
    await snapAround('plugin-conlang-viewer', mark);
  });
});
