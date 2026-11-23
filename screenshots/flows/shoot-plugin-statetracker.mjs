// State Tracker: the State tab with a tracked character, the story clock (settings, the review with the clock, the time of the
// processed parts, a fixed point), a reprocess applied without asking (the log of automatic changes, the fixed point kept), the
// tidy and its review, the history with a tidied value, a revert. Pictures: plugin-statetracker, plugin-statetracker-settings,
// plugin-statetracker-review, plugin-statetracker-clock, plugin-statetracker-clock-dialog, plugin-statetracker-log,
// plugin-statetracker-tidy-review, plugin-statetracker-history, plugin-statetracker-reverted.
// State: any step from 02-extensions on, with marginalia/plugins/state-tracker/target/state-tracker-1.9.0.jar copied into the
// extensions folder of the data folder (the steps hold no state-tracker JAR), and the fake model running (bin/start-llm.sh): the
// answers are queued here, so the result is the same every time.
import {bookTab, dialog, dialogBox, fill, idle, llm, login, main, openBook, snap} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';

const save = page => dialog(page).getByRole('button', { name: 'Save', exact: true }).click();
const QUOTE = 'The screen woke at fifty-eight percent, and above the battery the signal bars stood empty.';

async function addValue(page, button, text) {
  await page.getByRole('button', { name: button }).first().click();
  await idle(page, 400);
  await fill(dialog(page), 'Value', text);
  await save(page);
  await idle(page, 500);
}

await main(async ({ page }) => {
  await llm('/_reset', {});
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 700);
  await sideTab(page, 'State');

  await page.getByRole('button', { name: 'Track a character' }).click();
  await idle(page, 500);
  await fill(dialog(page), 'Name', 'Tomáš');
  await fill(dialog(page), 'Aliases', 'Tomas');
  for (const category of ['Carrying', 'Mood']) {
    await dialog(page).getByRole('checkbox', { name: new RegExp('^' + category) }).check();
    await idle(page, 300);
  }
  await save(page);
  await idle(page, 600);
  for (const text of ['rope', 'coil of rope', 'a cup of tea']) await addValue(page, 'Add an entry', text);
  await addValue(page, 'Set the value', 'furious');
  await snap(page, 'plugin-statetracker');

  // the settings: the story clock (day 1 is the day of the arch), every part in one request, reviewed
  await page.getByRole('button', { name: 'Settings', exact: true }).click();
  await idle(page, 600);
  await page.locator('vaadin-checkbox').filter({ hasText: 'Story clock' }).click();
  await idle(page, 400);
  await page.getByLabel('Day 1 is the day of', { exact: true }).fill('the arch');
  await page.getByLabel('Day 1 is the day of', { exact: true }).press('Tab');
  await idle(page, 400);
  await page.getByLabel('Parts per run', { exact: true }).fill('20');
  await page.getByLabel('Parts per run', { exact: true }).press('Enter');
  await idle(page, 500);
  await snap(page, 'plugin-statetracker-settings');
  await page.keyboard.press('Escape');
  await page.mouse.click(300, 700);
  await idle(page, 500);

  const MOON = 'Above the reeds hung a moon so large and low that it seemed to rest on the stalks';
  const CLIMBED = "The moon had climbed a finger's width.";
  await llm('/_queue', { reply: JSON.stringify({
    clock: [{ message: 1, kind: 'none' }, { message: 2, kind: 'advance', days: 0, timeOfDay: 'night', evidence: MOON },
            { message: 3, kind: 'none' }, { message: 4, kind: 'none' }, { message: 5, kind: 'advance', days: 0, timeOfDay: 'night', evidence: CLIMBED }],
    characters: [{ name: 'Tomáš', changes: [
      { category: 'Carrying', action: 'add', value: 'phone at fifty-eight percent', evidence: QUOTE, message: 3 },
      { category: 'Mood', action: 'set', value: 'uneasy', evidence: QUOTE, message: 3 }] }] }) });
  await page.getByRole('button', { name: 'Process now' }).click();
  await page.getByText(/changes are waiting for your review/).waitFor({ timeout: 60000 });
  await idle(page, 600);
  await page.getByRole('button', { name: 'Review changes', exact: true }).click();
  await idle(page, 800);
  await snap(page, 'plugin-statetracker-review', { locator: dialogBox(page, 'Review the changes'), pad: 6 });
  await dialog(page).getByRole('button', { name: 'Apply', exact: true }).click();
  await page.getByText(/^Done:/).first().waitFor({ timeout: 30000 });
  await idle(page, 600);
  // the progress panel is not modal: it floats over the sidebar until it is closed
  await page.getByRole('button', { name: 'Close', exact: true }).last().click();
  await idle(page, 500);
  await page.locator('vaadin-details-summary').filter({ hasText: /^Processed parts/ }).click();
  await idle(page, 600);
  await snap(page, 'plugin-statetracker-clock');

  // a fixed point on the last part: day 2, morning (the night passed)
  await page.getByRole('button', { name: 'Set the story time' }).first().click();
  await idle(page, 600);
  await page.getByLabel('Day', { exact: true }).fill('2');
  // the item of the open overlay: the time-of-day select of the settings has its own (hidden) items
  await dialog(page).locator('vaadin-select').first().click();
  await idle(page, 400);
  await page.getByRole('option', { name: 'morning', exact: true }).locator('visible=true').first().click();
  await idle(page, 300);
  await snap(page, 'plugin-statetracker-clock-dialog', { locator: dialogBox(page), pad: 6 });
  await save(page);
  await idle(page, 800);

  // reprocess from part 3 without asking: logged, and the fixed point on part 5 stays
  await page.getByRole('button', { name: 'Settings', exact: true }).click();
  await idle(page, 600);
  await page.locator('vaadin-checkbox').filter({ hasText: 'Ask before applying' }).click();
  await idle(page, 400);
  await page.keyboard.press('Escape');
  await page.mouse.click(300, 700);
  await idle(page, 500);
  await llm('/_queue', { reply: JSON.stringify({
    clock: [{ message: 1, kind: 'none' }, { message: 2, kind: 'none' }, { message: 3, kind: 'advance', days: 0, timeOfDay: 'night', evidence: CLIMBED }],
    characters: [{ name: 'Tomáš', changes: [
      { category: 'Carrying', action: 'add', value: 'phone at fifty-eight percent', evidence: QUOTE, message: 1 },
      { category: 'Mood', action: 'set', value: 'uneasy', evidence: QUOTE, message: 1 }] }] }) });
  const processedOpen = page.locator('vaadin-details').filter({ hasText: 'Processed parts' });
  if (!(await processedOpen.getAttribute('opened').then(v => v !== null))) {
    await processedOpen.locator('vaadin-details-summary').click();
    await idle(page, 400);
  }
  await page.locator('.state-tracker-value').filter({ hasText: 'Part 3:' }).getByRole('button', { name: 'Reprocess from here' }).click();
  await idle(page, 500);
  await dialog(page).getByRole('button', { name: 'Yes', exact: true }).click();
  await page.getByText(/^Done:/).first().waitFor({ timeout: 60000 });
  await idle(page, 600);
  await page.getByRole('button', { name: 'Close', exact: true }).last().click();
  await idle(page, 400);
  await page.locator('vaadin-details').filter({ hasText: 'Applied automatically' }).locator('vaadin-details-summary').click();
  await idle(page, 600);
  await snap(page, 'plugin-statetracker-log');

  // the tidy, reviewed: ask before applying again
  await page.getByRole('button', { name: 'Settings', exact: true }).click();
  await idle(page, 600);
  await page.locator('vaadin-checkbox').filter({ hasText: 'Ask before applying' }).click();
  await idle(page, 400);
  await page.keyboard.press('Escape');
  await page.mouse.click(300, 700);
  await idle(page, 500);
  // numbered: 1 rope, 2 coil of rope, 3 a cup of tea, 4 phone at fifty-eight percent, 5 uneasy
  await llm('/_queue', { reply: JSON.stringify({ characters: [{ name: 'Tomáš',
    drop: [{ item: 3, reason: 'the tea was finished long ago' }],
    update: [],
    merge: [{ items: [1, 2], value: 'coil of rope', reason: 'the same rope' }] }] }) });
  await page.getByRole('button', { name: 'Tidy the state' }).click();
  await page.getByText(/changes are waiting for your review/).waitFor({ timeout: 60000 });
  await idle(page, 600);
  await page.getByRole('button', { name: 'Review changes', exact: true }).click();
  await idle(page, 800);
  await snap(page, 'plugin-statetracker-tidy-review', { locator: dialogBox(page, 'Review the tidy'), pad: 6 });
  await dialog(page).getByRole('button', { name: 'Apply', exact: true }).click();
  await page.getByText(/^Tidy:/).first().waitFor({ timeout: 30000 }).catch(() => {});
  await idle(page, 800);
  const close = page.getByRole('button', { name: 'Close', exact: true });
  if (await close.count()) await close.last().click();
  await idle(page, 400);
  for (const summary of await page.locator('vaadin-details-summary').filter({ hasText: /^History/ }).all()) {
    await summary.click();
    await idle(page, 300);
  }
  await idle(page, 500);
  await snap(page, 'plugin-statetracker-history');

  // revert the mood of the automatic run: furious again
  const log = page.locator('vaadin-details').filter({ hasText: 'Applied automatically' });
  if (!(await log.getAttribute('opened').then(v => v !== null))) {
    await log.locator('vaadin-details-summary').click();
    await idle(page, 400);
  }
  await log.locator('.story-tracker-log-row').filter({ hasText: /^Tomáš, Mood/ }).getByRole('button', { name: 'Revert this change' }).click();
  await idle(page, 1000);
  await snap(page, 'plugin-statetracker-reverted');
});
