// Arc Outline: installs the plugin, lets the model import the first arc from the story, writes the second by hand, lets
// the model write a third one and its chapters.
// Pictures: the Arcs tab (also the one of the README), the menu of the wand, a chapter with its note, the three generator dialogs, the popover with what the model wrote.
// The model is the fake one: the answers are in dataset/plugins.mjs (arcOutline).
import fs from 'node:fs';
import path from 'node:path';
import {
    bookTab,
    field,
    fill,
    goto,
    idle,
    login,
    main,
    openBook,
    pickSelect,
    press,
    repo,
    sleep,
    snap,
    snapDialog,
    snapPopover,
} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';
import {arcOutline} from '../dataset/plugins.mjs';

/** Installs the plugin through Admin -> Extensions unless it is there (a full rebuild may have it from step 02). */
async function install(page) {
  await page.getByRole('button', { name: 'Admin' }).click();
  await idle(page, 600);
  await page.getByText('Extensions', { exact: true }).first().click();
  await idle(page, 800);
  if (await page.locator('vaadin-grid-cell-content', { hasText: 'arc-outline' }).count()) {
    console.log('  already installed arc-outline');
    return;
  }
  const jar = path.join(repo, 'marginalia/plugins/arc-outline/target/arc-outline-1.3.0.jar');
  if (!fs.existsSync(jar)) throw new Error('not built: ' + jar);
  await page.locator('vaadin-upload input[type=file]').setInputFiles(jar);
  await idle(page, 4000);
  await page.getByRole('button', { name: 'Refresh' }).click();
  await idle(page, 1500);
  console.log('  installed arc-outline');
}

/** The dialog of an arc or a chapter: title, description, note and state. */
async function fillNode(page, node) {
  const d = page.locator('vaadin-dialog[opened]').last();
  await fill(d, 'Title', node.title);
  await fill(d, 'Description', node.description);
  if (node.note) await fill(d, 'Note (private)', node.note);
  if (node.state && node.state !== 'Planned') await pickSelect(d, node.state);
  await press(d, 'Save');
  await idle(page, 800);
}

/** Waits until the model is done: the Generate button is back and "Save as ..." is enabled. */
async function generated(page, d, save) {
  await d.getByRole('button', { name: 'Stop', exact: true }).waitFor({ timeout: 15000 });
  await d.getByRole('button', { name: 'Generate', exact: true }).waitFor({ timeout: 60000 });
  await page.waitForFunction(label => {
    const b = [...document.querySelectorAll('vaadin-dialog[opened] vaadin-button')].find(x => x.textContent.trim() === label);
    return b && !b.hasAttribute('disabled');
  }, save, { timeout: 15000 });
  await sleep(800);
}

await main(async ({ page }) => {
  await login(page);
  await install(page);
  await goto(page, '/');
  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 700);
  await sideTab(page, 'Arcs');

  const arcButtons = name => page.getByRole('button', { name, exact: true });

  // the first arc is imported from the five parts of the story: the wand is a menu
  const imp = arcOutline.importArc;
  await arcButtons('Generate an arc').click();
  await idle(page, 500);
  await page.getByRole('menuitem', { name: 'Import from story' }).click();
  await idle(page, 1200);
  let d = page.locator('vaadin-dialog[opened]').last();
  // a number field commits its value on Enter (or when it loses the focus)
  const chapters = field(d, 'Number of chapters (optional)');
  await chapters.fill(String(imp.chapters));
  await chapters.press('Enter');
  await idle(page, 1500);
  await press(d, 'Generate');
  await generated(page, d, 'Save as arc');
  await snapDialog(page, 'plugin-arcoutline-import', 0);
  await press(d, 'Save as arc');
  await idle(page, 1000);

  // the second arc by hand, with its chapters
  for (const arc of arcOutline.arcs) {
    await arcButtons('Add new arc').click();
    await idle(page, 600);
    await fillNode(page, arc);
    for (const chapter of arc.chapters) {
      await arcButtons('Add chapter').nth(1).click();
      await idle(page, 600);
      await fillNode(page, chapter);
    }
  }

  // the model writes the third arc
  const g = arcOutline.generateArc;
  await arcButtons('Generate an arc').click();
  await idle(page, 500);
  await page.getByRole('menuitem', { name: 'Generate an arc' }).click();
  await idle(page, 1200);
  d = page.locator('vaadin-dialog[opened]').last();
  await fill(d, 'Specification', g.specification);
  await press(d, 'Generate');
  await generated(page, d, 'Save as arc');
  await snapDialog(page, 'plugin-arcoutline-generate-arc', 0);
  await press(d, 'Save as arc');
  await idle(page, 1000);

  // and the chapters of that arc: an exact number gives a field for each chapter
  const c = arcOutline.generateChapters;
  await arcButtons('Generate chapters').nth(2).click();
  await idle(page, 1200);
  d = page.locator('vaadin-dialog[opened]').last();
  await fill(d, 'What you want from the chapters in general', c.general);
  // a number field commits its value on Enter (or when it loses the focus)
  const count = field(d, 'or an exact number');
  await count.fill(String(c.count));
  await count.press('Enter');
  await idle(page, 800);
  for (const [i, spec] of c.specs.entries()) {
    if (spec) await fill(d, `Chapter ${i + 1} (optional)`, spec);
  }
  await press(d, 'Generate');
  await generated(page, d, 'Save as chapters');
  // the left side scrolls: show its end, with the fields of the chapters and the button
  await page.evaluate(() => { const left = document.querySelector('vaadin-dialog[opened] .overflow-auto'); if (left) left.scrollTop = left.scrollHeight; });
  await sleep(500);
  await snapDialog(page, 'plugin-arcoutline-generate-chapters', 0);
  await press(d, 'Save as chapters');
  await idle(page, 1000);

  // the list: the first (finished) and the last arc are closed, so the chapters of the current one show their states
  await arcButtons('Hide chapters').first().click();
  await idle(page, 500);
  await arcButtons('Hide chapters').last().click();
  await idle(page, 500);
  await page.evaluate(() => {
    const list = document.querySelector('.arc-outline-list');
    if (list) list.scrollTop = list.scrollHeight;
    if (document.activeElement && document.activeElement.blur) document.activeElement.blur();
  });
  // away from the buttons, or a tooltip ends up in the picture
  await page.mouse.move(1200, 500);
  await sleep(800);
  await snap(page, 'plugin-arcoutline-tab');

  // the wand is a menu: generate an arc or import it from the story
  await arcButtons('Generate an arc').click();
  await idle(page, 600);
  await page.mouse.move(1200, 500);
  await snap(page, 'plugin-arcoutline-menu', { clip: { x: 40, y: 185, width: 640, height: 190 } });
  await page.keyboard.press('Escape');
  await idle(page, 400);

  // the third level: the note of a chapter under its description, instead of the dialog of the chapter
  await arcButtons('Show the note').click();
  await idle(page, 500);
  await page.evaluate(() => {
    const list = document.querySelector('.arc-outline-list');
    const arc = document.querySelectorAll('.arc-outline-arc')[1];
    if (list && arc) list.scrollTop = arc.offsetTop - list.offsetTop - 8;
    if (document.activeElement && document.activeElement.blur) document.activeElement.blur();
  });
  await page.mouse.move(1200, 500);
  await sleep(600);
  await snap(page, 'plugin-arcoutline-note', { locator: page.locator('.arc-outline-list') });

  // what the model wrote for the third arc: the robot button opens a popover
  await page.locator('.arc-outline-arc').nth(2).getByRole('button', { name: /Generated by the model/ }).click();
  await idle(page, 800);
  await snapPopover(page, 'plugin-arcoutline-generated', 0);
});
