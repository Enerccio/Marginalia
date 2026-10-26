// The features of 1.5.0 that have a screen of their own: annotations (the window, the margin, the tab, the popover of a mark, the
// model that annotates), redraft, merge of two parts, branch tokens, drafting the instructions, then the workspace (keyboard
// shortcuts, the light theme, the advanced search of the books, the import from SillyTavern, the activation preview of a lorebook,
// the resource files of the administration).
// State: a COPY of 13-db-backup, discarded (it adds annotations and changes the settings). Run: node flows/shoot-features.mjs [--only name,name]
import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {
  bookTab, CONTENT, dialog, fill, idle, llm, login, main, menuItem, openBook, openTab, pick, press, root, section,
  sleep, snap, snapDialog, snapPopover
} from '../lib/shot.mjs';
import {clickCogs, clickPlus, partAction, parts, sideTab} from '../lib/story.mjs';
import {CHAT_FILE} from '../dataset/stchat.mjs';

/** Closes the topmost window with its Close or Cancel (never with Exit: when no window opened, that would close the book). */
async function closeTop(page) {
  await dialog(page).getByRole('button', { name: /^(Close|Cancel|No)$/ }).first().click();
  await idle(page, 400);
}

/** Selects the words in the text of the part that has them, as the mouse would; returns the number of the part (0 based). */
async function selectText(page, phrase) {
  const all = parts(page);
  for (let i = 0; i < await all.count(); i++) {
    const found = await all.nth(i).evaluate((el, phrase) => {
      const walker = document.createTreeWalker(el, NodeFilter.SHOW_TEXT);
      for (let node = walker.nextNode(); node; node = walker.nextNode()) {
        const at = node.textContent.indexOf(phrase);
        if (at >= 0) {
          const range = document.createRange();
          range.setStart(node, at);
          range.setEnd(node, at + phrase.length);
          const selection = window.getSelection();
          selection.removeAllRanges();
          selection.addRange(range);
          return true;
        }
      }
      return false;
    }, phrase);
    if (found) {
      await all.nth(i).scrollIntoViewIfNeeded();
      await sleep(300);
      return i;
    }
  }
  throw new Error('no part has the text "' + phrase + '"');
}

await main(async ({ page }) => {
  await execFileSync('node', [path.join(root, 'dataset/stchat.mjs')], { stdio: 'inherit' });
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');

  // ---------------------------------------------------------------- annotations
  await section(page, 'annotation-dialog', async () => {
    await selectText(page, 'How little you know');
    await page.keyboard.press('a');
    await idle(page, 700);
    const d = dialog(page);
    await fill(d, 'Note', 'Draconia never explains how she knew about the map. Pay this off in the next part.');
    await fill(d, 'Type', 'continuity');
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await snapDialog(page, 'annotation-dialog');
    await press(d, 'Save');
    await idle(page, 800);
  });

  await section(page, 'annotation-part', async () => {
    await partAction(page, -1, 'Annotate part');
    const d = dialog(page);
    await fill(d, 'Note', 'The ending is abrupt: give Harry a beat to react before the scene closes.');
    await fill(d, 'Type', 'pacing');
    await press(d, 'Save');
    await idle(page, 800);
  });

  await section(page, 'annotation-popover', async () => {
    const part = page.locator('.chat-message:has([data-annotation])').first();
    await part.scrollIntoViewIfNeeded();
    await part.locator('[data-annotation]').first().click();
    await idle(page, 800);
    await snapPopover(page, 'annotation-popover');
    await page.keyboard.press('Escape');
    await sleep(300);
  });

  await section(page, 'annotations-margin', async () => {
    await clickCogs(page);
    await menuItem(page, 'Annotations in the margin').click();
    await idle(page, 900);
    await page.locator('.chat-message:has([data-annotation])').first().scrollIntoViewIfNeeded();
    await sleep(500);
    await page.mouse.move(900, 400);
    await snap(page, 'annotations-margin');
  });

  await section(page, 'annotations-tab', async () => {
    await sideTab(page, 'Annotations');
    await snap(page, 'annotations-tab');
    await sideTab(page, 'Outline');
  });

  // the right click menu of the text of a part: the selection is the passage the items work on
  await section(page, 'text-menu', async () => {
    await selectText(page, 'hold hands with every Weasley');
    const sel = await page.evaluate(() => { const r = window.getSelection().getRangeAt(0).getBoundingClientRect(); return { x: r.x, y: r.y, width: r.width, height: r.height }; });
    await page.mouse.click(sel.x + sel.width / 2, sel.y + sel.height / 2, { button: 'right' });
    await idle(page, 900);
    const menu = await page.evaluate(() => {
      const boxes = [...document.querySelectorAll('vaadin-context-menu-list-box, vaadin-menu-bar-list-box')]
        .map(e => e.getBoundingClientRect()).filter(r => r.width > 50 && r.height > 20);
      const r = boxes[boxes.length - 1];
      return r ? { x: r.x, y: r.y, width: r.width, height: r.height } : null;
    });
    if (!menu) throw new Error('no context menu opened');
    const left = Math.min(sel.x, menu.x) - 40, top = Math.min(sel.y, menu.y) - 24;
    const right = Math.max(sel.x + sel.width, menu.x + menu.width) + 40, bottom = Math.max(sel.y + sel.height, menu.y + menu.height) + 24;
    await snap(page, 'story-text-menu', { clip: { x: left, y: top, width: right - left, height: bottom - top } });
    await page.keyboard.press('Escape');
    await sleep(300);
  });

  // ---------------------------------------------------------------- the model annotates
  await section(page, 'annotate-ai', async () => {
    await partAction(page, 1, 'Annotate with AI');
    const d = dialog(page);
    await fill(d, 'What to look for', 'Flag anything that contradicts the lorebook, and dialogue that sounds modern.');
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await llm('/_queue', { reply: JSON.stringify({ findings: [
      { quote: 'a hiccup', note: 'Ron is tipsy only in the first part; here the hiccup comes out of nowhere.', type: 'continuity' },
      { quote: 'good time', note: 'Slightly modern phrasing for Draconia; the lore asks for formal, cutting speech.', type: 'style' },
    ] }) });
    await press(d, 'Annotate', { wait: 600 });
    await d.getByText('Proposed annotations', { exact: false }).first().waitFor({ timeout: 30000 });
    await idle(page, 900);
    await snapDialog(page, 'annotate-ai-proposals');
    await closeTop(page);
  });

  // ---------------------------------------------------------------- redraft
  await section(page, 'redraft', async () => {
    await partAction(page, 1, 'Redraft');
    const d = dialog(page);
    await fill(d, 'Instructions for the redraft', 'Make Draconia colder and shorter; keep her last line about hiding.');
    await llm('/_queue', { reply: 'A cold voice cut through the fog. "How touching. Does the Boy Who Lived hold hands with every Weasley?"\n\nDraconia Malfoy stood under the nearest lamp, hands in the pockets of her long green coat. Behind her Crabbe and Goyle studied the cobbles.\n\n"Get lost, Malfoy," Ron said, with great dignity and a hiccup.\n\n"Hiding?" For a heartbeat something moved behind Draconia\'s face, quick and ugly, and was gone. "How little you know."' });
    await press(d, 'Redraft', { wait: 600 });
    await d.getByText('Redrafted text', { exact: false }).first().waitFor({ timeout: 30000 });
    await sleep(2500);
    await d.getByText('Differences', { exact: true }).click();
    await idle(page, 800);
    await snapDialog(page, 'redraft-dialog');
    await closeTop(page);
  });

  // ---------------------------------------------------------------- merge
  await section(page, 'merge', async () => {
    await partAction(page, 2, 'Merge into previous part');
    await idle(page, 900);
    await snapDialog(page, 'part-merge-dialog');
    await closeTop(page);
  });

  await section(page, 'branch-tokens', async () => {
    await clickCogs(page);
    await menuItem(page, 'Branch Tokens').click();
    await idle(page, 1200);
    await snapDialog(page, 'branch-tokens');
    await closeTop(page);
  });

  await section(page, 'impersonate', async () => {
    await clickPlus(page);
    await page.getByRole('button', { name: 'Draft Instructions', exact: true }).click();
    await idle(page, 800);
    const d = dialog(page);
    await fill(d, 'Guidance', 'Harry follows her to the Shrieking Shack; she turns round and finally tells him one true thing.');
    await llm('/_queue', { reply: 'Harry keeps three steps behind Draconia all the way up the hill to the Shrieking Shack. At the gate she stops and turns round. She does not threaten him; she tells him one true thing, quickly, as if it cost her: she is not following orders, she is hiding something from the people who gave them. Harry must decide whether to believe her. End on his choice, not on her answer.' });
    await press(d, 'Draft Instructions', { wait: 600 });
    await d.getByText('Draft Again', { exact: false }).first().waitFor({ timeout: 30000 });
    await sleep(1500);
    await snapDialog(page, 'impersonate-dialog');
    await closeTop(page);
    await page.keyboard.press('Escape');
    await sleep(300);
  });

  // ---------------------------------------------------------------- the workspace
  await page.getByRole('button', { name: 'Exit', exact: true }).last().click();
  await idle(page, 1000);

  await section(page, 'shortcuts', async () => {
    await openTab(page, 'Settings');
    await page.getByText('Keyboard shortcuts', { exact: true }).first().click();
    await idle(page, 900);
    await snap(page, 'settings-shortcuts', { clip: CONTENT });
  });

  await section(page, 'book-search', async () => {
    await openTab(page, 'Books');
    await press(page, 'Search');
    await idle(page, 800);
    await snapDialog(page, 'book-search-advanced');
    await closeTop(page);
  });

  await section(page, 'sillytavern-import', async () => {
    await openTab(page, 'Books');
    await page.locator('vaadin-button:has(vaadin-icon[icon$=":comments"])').first().click();
    await idle(page, 800);
    const d = dialog(page);
    await d.locator('vaadin-upload input[type=file]').setInputFiles(path.join(root, 'dataset/build', CHAT_FILE));
    await idle(page, 2500);
    await snapDialog(page, 'sillytavern-import');
    await closeTop(page);
  });

  await section(page, 'lorebook-preview', async () => {
    await openTab(page, 'Lorebooks');
    await pick(page, 'Lorebook', 'Harry Potter - Half-Blood Prince');
    await idle(page, 1500);
    await press(page, 'Preview activation');
    await idle(page, 800);
    const d = dialog(page);
    await fill(d, 'Text of the turn', 'Harry follows Draconia over the bridge in the fog.');
    await pick(d, 'Tags of the book', 'Harry Potter and Half Blood Prince - FDM');   // a book: its tags are the ones that count
    await idle(page, 1500);
    await snapDialog(page, 'lorebook-preview-activation');
    await closeTop(page);
  });

  await section(page, 'resource-files', async () => {
    // files that no resource points to: what a deleted resource or a replaced picture leaves behind (the scan reads the folders of the users)
    const strays = [['admin', 'jpeg', 2_400_000], ['admin', 'png', 310_000], ['ginny', 'jpeg', 880_000], ['ginny', 'pdf', 95_000]];
    for (const [user, ext, size] of strays) {
      const folder = path.join(process.env.SHOT_HOME ?? path.join(root, '.data'), '.marginalia', 'data', user, 'images');
      fs.mkdirSync(folder, { recursive: true });
      fs.writeFileSync(path.join(folder, `${crypto.randomUUID()}.${ext}`), crypto.randomBytes(size));
    }
    await page.getByRole('button', { name: 'Admin' }).click();
    await idle(page, 700);
    await page.getByText('Resource Files', { exact: true }).first().click();
    await idle(page, 800);
    await press(page, 'Scan files', { wait: 3000 });
    await snap(page, 'admin-resource-files', { clip: CONTENT });
  });

  await section(page, 'theme', async () => {
    await page.getByRole('button', { name: 'Admin' }).click().catch(() => {});
    await openTab(page, 'Books');
    await page.locator('vaadin-button:has(vaadin-icon[icon$=":sun"]), vaadin-button:has(vaadin-icon[icon$=":moon"])').first().click();
    await idle(page, 1500);
    await snap(page, 'theme-light');
    await page.locator('vaadin-button:has(vaadin-icon[icon$=":sun"]), vaadin-button:has(vaadin-icon[icon$=":moon"])').first().click();
    await idle(page, 800);
  });
});
