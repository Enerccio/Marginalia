// Knowledge Tracker: the Knowledge tab with a tracked character, what she knows and does not know, the old knowledge
// and the processing panel. Pictures: plugin-knowledgetracker, plugin-knowledgetracker-processing.
// State: any step from 02-extensions on, with marginalia/plugins/knowledge-tracker/target/knowledge-tracker-1.8.0.jar in
// the extensions folder of the data folder (bin/refresh-plugins.sh only swaps the JARs that are there already: copy this
// one in with the application stopped, the application verifies it when it starts).
import {bookTab, dialog, fill, idle, login, main, openBook, pickSelect, sleep, snap} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';

const save = page => dialog(page).getByRole('button', { name: 'Save', exact: true }).click();

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 700);
  await sideTab(page, 'Knowledge');

  await page.getByRole('button', { name: 'Track a character' }).click();
  await idle(page, 500);
  await fill(dialog(page), 'Name', 'Tomáš');
  await fill(dialog(page), 'Aliases', 'Tomas');
  await save(page);
  await idle(page, 600);

  await page.getByRole('button', { name: 'Show what the character knows' }).first().click();
  await idle(page, 500);
  const items = [
    ['Knows', 'Mizuki said in Japanese, at the arch, that the wall was breathing.'],
    ['Does not know', 'Mizuki has noticed that he understands Japanese.'],
    ['Does not know', 'Anybody lives in this valley.'],
  ];
  for (const [kind, text] of items) {
    await page.getByRole('button', { name: 'Add knowledge' }).first().click();
    await idle(page, 400);
    if (kind !== 'Knows') await pickSelect(dialog(page), kind);
    await fill(dialog(page), 'Text', text);
    await save(page);
    await idle(page, 500);
  }
  // the third one is not true any more
  await page.getByRole('button', { name: /^Retract/ }).nth(2).click();
  await idle(page, 600);
  await page.getByText(/Old knowledge \(1\)/).click();
  await idle(page, 500);

  await page.getByRole('button', { name: 'Process now' }).click();
  await idle(page, 1500);
  await page.evaluate(() => document.activeElement && document.activeElement.blur());
  // the processing panel with the list of the processed parts open (a second picture)
  await page.getByText(/Processed parts \(/).click();
  await idle(page, 500);
  await snap(page, 'plugin-knowledgetracker-processing', { clip: { x: 16, y: 130, width: 690, height: 780 } });
  await page.getByText(/Processed parts \(/).click();
  await idle(page, 500);
  // the sidebar is not tall enough for everything: scroll so that the old knowledge is in the picture
  await page.evaluate(() => { const s = document.querySelector('vaadin-scroller'); if (s) s.scrollTop = s.scrollHeight; });
  await sleep(400);
  await snap(page, 'plugin-knowledgetracker', { clip: { x: 16, y: 130, width: 690, height: 780 } });
});
