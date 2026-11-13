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
  fill,
  goto,
  llm,
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
    // the library of extraction prompts is further down the panel
    await page.getByText('Extraction prompts', { exact: true }).first().scrollIntoViewIfNeeded();
    await sleep(500);
    await snap(page, 'plugin-reallang-prompts', { clip: CONTENT });
    await page.getByText('Languages', { exact: true }).first().scrollIntoViewIfNeeded();
    // the editor of a language (the list of the prompts has an Edit button too: the first one is the one of the languages)
    await page.locator('vaadin-grid-cell-content').filter({ hasText: /^Japanese$/ }).first().click();
    await sleep(400);
    await page.getByRole('button', { name: 'Edit', exact: true }).first().click();
    await idle(page, 1200);
    await snapDialog(page, 'plugin-reallang-editor', -1);
    // the glossary of the language is at the end of the form
    await dialog(page).locator('vaadin-grid').last().scrollIntoViewIfNeeded();
    await sleep(600);
    await snapDialog(page, 'plugin-reallang-editor-glossary', -1);
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

  // the word extraction: the fake model lists three terms of the first part and one of the second, the rest has none
  await section(page, 'extraction', async () => {
    await bookTab(page, 'Story');
    await widenSidebar(page, 700);
    await sideTab(page, 'Real language');
    await idle(page, 800);
    await page.getByRole('tab', { name: 'Word extraction', exact: true }).click();
    await idle(page, 900);
    const terms = list => JSON.stringify({ terms: list });
    await llm('/_queue', { reply: terms([
      { source: 'Silvakhor', category: 'Place', written: 'シルヴァコール', reading: '', keep: false, note: 'The ferry landing, in katakana like the other places of the valley.' },
      { source: 'Aethel-Vael', category: 'Language', written: 'アエセル=ヴァエル', reading: '', keep: false, note: 'The language Saellen speaks at home.' },
      { source: 'Lyrshara', category: 'River', written: 'リルシャラ川', reading: '', keep: false, note: 'The river; 川 because Japanese names a river that way.' },
    ]) });
    await llm('/_queue', { reply: terms([
      { source: 'Mizuki', category: 'Person', written: '', reading: '', keep: true, note: 'Her own name, as the text writes it.' },
    ]) });
    for (let i = 0; i < 4; i++) await llm('/_queue', { reply: terms([]) });
    await page.getByRole('button', { name: 'Extract the words', exact: true }).click();
    await sleep(1500);
    await snap(page, '_extraction-running', { clip: { x: 16, y: 130, width: 690, height: 780 } });
    for (let i = 0; i < 60; i++) {
      if (await page.getByRole('button', { name: 'Extract the words', exact: true }).isEnabled().catch(() => false)) break;
      await sleep(1000);
    }
    await idle(page, 1000);
    await page.getByRole('button', { name: 'Extract the words', exact: true }).scrollIntoViewIfNeeded();
    await sleep(400);
    await snap(page, 'plugin-reallang-extraction', { clip: { x: 16, y: 130, width: 690, height: 780 } });
  });

  // the glossary of the book (with the three terms the model proposed): the entry window
  await section(page, 'glossary', async () => {
    // the book has two languages (Japanese and, for Tomáš's muttering, Czech): a glossary tab for each
    await page.getByRole('tab', { name: 'Glossary: Japanese', exact: true }).click();
    await idle(page, 1000);
    await snap(page, 'plugin-reallang-glossary', { clip: { x: 16, y: 130, width: 690, height: 780 } });
    await page.locator('vaadin-grid-cell-content:visible').filter({ hasText: /^Saellen$/ }).first().click();
    await sleep(500);
    await page.getByRole('button', { name: 'Edit', exact: true }).click();
    await idle(page, 900);
    await snapDialog(page, 'plugin-reallang-glossary-entry');
    await closeDialog(page);
  });

  // the source of a speech, and the search and replace over the sources
  await section(page, 'sources', async () => {
    await bookTab(page, 'Story');
    await idle(page, 1200);
    const mark = page.locator('mark.reallang-passage').first();
    await center(mark);
    await sleep(500);
    await mark.click();
    await page.locator('.translator-source-box').first().waitFor({ timeout: 10000 });
    await page.getByRole('button', { name: 'Edit the source', exact: true }).click();
    await idle(page, 900);
    await snapDialog(page, 'plugin-reallang-source');
    await closeDialog(page);
    await page.keyboard.press('Escape');
    await sleep(400);
    await sideTab(page, 'Real language');
    await idle(page, 600);
    await page.getByRole('button', { name: 'Search and replace in the sources', exact: true }).click();
    await idle(page, 900);
    await fill(page, 'Search for', 'Maybe');
    await fill(page, 'Replace with', 'Perhaps');
    await dialog(page).getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByText(/^Found \d+ match/).first().waitFor({ timeout: 30000 }).catch(() => {});
    await idle(page, 1200);
    // the row of the first speech opens: the matches in it
    await dialog(page).locator('vaadin-grid-tree-toggle').first().click().catch(() => {});
    await idle(page, 900);
    await snapDialog(page, 'plugin-reallang-search-replace');
    await closeDialog(page);
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
