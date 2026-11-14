// The pictures of the jobs (run by bin/shoot-jobs.sh on a copy of 13-db-backup with bin/seed-jobs.py applied and the fake model running):
// the log in the Admin page (also with the calls of a job opened and the question before the delete), and /jobs with a generation in
// flight. The generation is real: its end is checked in the log, so the flow also shows that a request is recorded.
import {DESKTOP, dialog, field, fill, idle, llm, login, main, openBook, openTab, press, sleep, snap, snapDialog, users} from '../lib/shot.mjs';
import {bookTab} from '../lib/shot.mjs';
import {clickPlus, fillTurn} from '../lib/story.mjs';

await main(async ({ page, context }) => {
  await login(page);

  // ------------------------------------------------------------ a generation that takes its time, and /jobs while it runs
  await openBook(page);
  await bookTab(page, 'Story');
  await llm('/_queue', {
    reply: 'They followed the river until the reeds gave way to a track, and neither of them said a word about the language.',
    delayMs: 20000,
  });
  await fillTurn(page, { scene: 'River bank / dawn', pov: 'Tomáš', present: 'Tomáš, Mizuki', instructions: 'They follow the river downstream.' });
  await press(page, 'Generate', { exact: false, wait: 800 });

  const live = await context.newPage();
  await live.setViewportSize(DESKTOP);
  await login(live, users.admin, '/jobs');
  await live.getByText('generation', { exact: true }).first().waitFor({ timeout: 15000 });
  await sleep(3500);                       // one refresh later the call has been waiting for a few seconds
  await snap(live, 'jobs-live');
  await live.close();

  // the generation ends: the part appears
  await page.getByText('neither of them said a word', { exact: false }).first().waitFor({ timeout: 60000 });
  await idle(page, 1500);
  await page.getByRole('button', { name: 'Exit', exact: true }).first().click();
  await idle(page, 800);

  // ------------------------------------------------------------ the log
  await press(page, 'Admin');
  await idle(page, 800);
  await openTab(page, 'Jobs');
  await idle(page, 1200);
  await snap(page, 'admin-jobs');

  // the calls of the audiobook conversion: the labelling model and the speech
  await page.locator('vaadin-grid-cell-content').filter({ hasText: 'Conversion of 2 parts' }).first().click();
  await idle(page, 1000);
  await snap(page, 'admin-jobs-calls');

  // a failed job, with the error of its call
  await page.locator('vaadin-grid-cell-content').filter({ hasText: 'Conversion of 2 parts' }).first().click();
  await page.locator('vaadin-grid-cell-content').filter({ hasText: 'Conversion of 1 part' }).first().click();
  await idle(page, 1000);
  await snap(page, 'admin-jobs-failed');

  // the clean up by hand: the jobs started more than 30 days ago
  await page.locator('vaadin-grid-cell-content').filter({ hasText: 'Conversion of 1 part' }).first().click();
  await fill(page, 'Older than (days)', '30');
  await press(page, 'Refresh');
  await idle(page, 900);
  await snap(page, 'admin-jobs-old');
  await press(page, 'Delete these jobs');
  await idle(page, 800);
  await snapDialog(page, 'admin-jobs-delete');
  await press(dialog(page), 'No');
});
