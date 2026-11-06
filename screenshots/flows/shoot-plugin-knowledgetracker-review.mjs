// Knowledge Tracker, reading with the model: the progress panel (not modal, it floats over the sidebar) and the review of the
// changes the model found. Pictures: plugin-knowledgetracker-progress, plugin-knowledgetracker-review.
// State: like shoot-plugin-knowledgetracker (the plugin JAR copied into the extensions folder), and the fake model running
// (bin/start-llm.sh): its answer to the request is queued here, so the proposal is the same every time.
import {bookTab, dialog, dialogBox, fill, idle, llm, login, main, openBook, pickSelect, snap} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';

const save = page => dialog(page).getByRole('button', { name: 'Save', exact: true }).click();

await main(async ({ page }) => {
  // an answer that an earlier run queued and did not use would be taken instead of ours
  await llm('/_reset', {});
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
  for (const [kind, text] of [
    ['Knows', 'Mizuki said in Japanese, at the arch, that the wall was breathing.'],
    ['Does not know', 'Mizuki has noticed that he understands Japanese.'],
    ['Does not know', 'The phone has no signal here.']]) {
    await page.getByRole('button', { name: 'Add knowledge' }).first().click();
    await idle(page, 400);
    if (kind !== 'Knows') await pickSelect(dialog(page), kind);
    await fill(dialog(page), 'Text', text);
    await save(page);
    await idle(page, 500);
  }

  // the settings: read what the characters learn with the model, five parts per run
  await page.getByRole('button', { name: 'Settings', exact: true }).click();
  await idle(page, 600);
  await page.getByRole('checkbox', { name: 'Read what the characters learn with the model' }).check();
  await idle(page, 500);
  await page.getByLabel('Parts per run', { exact: true }).fill('5');
  await page.getByLabel('Parts per run', { exact: true }).press('Enter');
  await idle(page, 500);
  await page.keyboard.press('Escape');
  await page.mouse.click(300, 700);
  await idle(page, 500);

  // what the model "answers": one retraction and two additions (the second quote is not in the part, the review says so)
  await llm('/_queue', { reply: JSON.stringify({ characters: [{ name: 'Tomáš',
    retract: [{ item: 3, evidence: 'The screen woke at fifty-eight percent, and above the battery the signal bars stood empty.', message: 3 }],
    add: [{ kind: 'knows', text: 'The phone has fifty-eight percent and no signal', evidence: 'The screen woke at fifty-eight percent, and above the battery the signal bars stood empty.', message: 3 },
          { kind: 'knows', text: 'A ferry will come at dawn', evidence: 'the ferryman waved at him from the river', message: 1 }] }] }) });
  await page.getByRole('button', { name: 'Process now' }).click();
  await page.getByText(/changes are waiting for your review/).waitFor({ timeout: 60000 });
  await idle(page, 600);
  await snap(page, 'plugin-knowledgetracker-progress', { clip: { x: 16, y: 130, width: 690, height: 780 } });

  await page.getByRole('button', { name: 'Review changes', exact: true }).click();
  await idle(page, 800);
  await snap(page, 'plugin-knowledgetracker-review', { locator: dialogBox(page, 'Review the changes'), pad: 6 });
  await dialog(page).getByRole('button', { name: 'Apply', exact: true }).click();
  await page.getByText(/^Done:/).waitFor({ timeout: 30000 });
});
