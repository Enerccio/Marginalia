// The Real Language Translator: the settings panel, the language editor, the book's choice, the Real language tab of the story, the story with a translated
// speech, the range dialog and the export dialog. Run by bin/shoot-reallang.sh (it writes the languages and the translated part first).
// Pictures: plugin-reallang-settings, -editor, -book, -tab, -story, -range, -export.
import fs from 'node:fs';
import path from 'node:path';
import {
  bookTab,
  closeDialog,
  CONTENT,
  dialog,
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
    await page.getByText('Real Language Translator', { exact: true }).first().click();
    await sleep(1000);
    await snap(page, 'plugin-reallang-settings', { clip: CONTENT });
    // the editor of a language
    await page.locator('vaadin-grid-cell-content').filter({ hasText: /^Japanese$/ }).first().click();
    await sleep(400);
    await page.getByRole('button', { name: 'Edit', exact: true }).click();
    await idle(page, 1200);
    await snapDialog(page, 'plugin-reallang-editor', -1);
    await closeDialog(page);
    // closing the editor counts as an edit of the library: leave the Settings tab without saving anything
    const discard = page.getByRole('button', { name: 'Discard Changes', exact: true });
    if (await discard.isVisible().catch(() => false)) {
      await discard.click();
      await idle(page, 800);
    }
  });

  // a picture of the speech that was clicked with the popover under it: cut around the mark
  const snapAround = async (name, mark) => {
    const box = await mark.boundingBox();
    const x = Math.max(0, box.x - 300);
    await snap(page, name, { clip: { x, y: Math.max(0, box.y - 110), width: Math.min(1000, 1920 - x), height: 330 } });
  };
  const center = mark => mark.evaluate(el => el.scrollIntoView({ block: 'center' }));

  await section(page, 'book', async () => {
    await openBook(page);
    await bookTab(page, 'Prompts');
    await idle(page, 1000);
    await page.getByText('Real Language Translator', { exact: true }).last().evaluate(el => el.scrollIntoView({ block: 'start' }));
    await sleep(500);
    await snap(page, 'plugin-reallang-book', { clip: { x: 16, y: 120, width: 1888, height: 790 } });
  });

  await section(page, 'tab', async () => {
    await bookTab(page, 'Story');
    await widenSidebar(page, 700);
    await sideTab(page, 'Real language');
    await idle(page, 1000);
    await snap(page, 'plugin-reallang-tab', { clip: { x: 16, y: 130, width: 690, height: 780 } });
  });

  await section(page, 'story', async () => {
    await bookTab(page, 'Story');
    await idle(page, 1500);
    const mark = page.locator('mark.reallang-passage').first();
    await center(mark);
    await sleep(500);
    await mark.click();
    await page.locator('.translator-source-box').first().waitFor({ timeout: 10000 });
    await sleep(500);
    await snapAround('plugin-reallang-story', mark);
    await page.keyboard.press('Escape');
    await sleep(300);
  });

  await section(page, 'range', async () => {
    await openStoryMenu(page);
    await menuItem(page, 'Translate parts (real language)').click();
    await idle(page, 900);
    await snapDialog(page, 'plugin-reallang-range');
    await closeDialog(page);
  });

  await section(page, 'export', async () => {
    await openStoryMenu(page);
    await menuItem(page, 'Export story').click();
    await idle(page, 900);
    await snapDialog(page, 'plugin-reallang-export');
    await closeDialog(page);
  });

});
