// The writing statistics (1.6.0): the daily goals in the Settings, the Statistics window of the book (the days, the time, the
// calendar) and of all books (the calendar with the goal), and the phone layout.
// Run on a COPY of 13-db-backup that bin/seed-statistics.py has given a year of history (the application stopped for it, see
// bin/shoot-all.sh): the dataset itself has a day or two of statistics. The goals are set here, through the Settings tab.
import {bookTab, closeDialog, dialog, fill, idle, launch, login, main, openBook, openTab, press, section, sleep, snap, snapDialog, menuItem} from '../lib/shot.mjs';
import {openStoryMenu} from '../lib/story.mjs';

// the tab of the open statistics window
const statTab = (page, name) => dialog(page).getByRole('tab', { name, exact: true }).first();

await main(async ({ page }) => {
  await login(page);

  await section(page, 'goals', async () => {
    await openTab(page, 'Settings');
    await idle(page, 800);
    const words = page.getByLabel('Daily goal: words written', { exact: true });
    await words.scrollIntoViewIfNeeded();
    await words.fill('500');
    await words.dispatchEvent('change');
    const minutes = page.getByLabel('Daily goal: minutes of editing', { exact: true });
    await minutes.fill('30');
    await minutes.dispatchEvent('change');
    await idle(page, 400);
    await press(page, 'Save', { wait: 1200 });
    await words.scrollIntoViewIfNeeded();
    // the two fields, with the field above them
    const top = await words.evaluate(e => e.closest('vaadin-integer-field').getBoundingClientRect().y);
    const bottom = await minutes.evaluate(e => e.closest('vaadin-integer-field').getBoundingClientRect().bottom);
    await snap(page, 'settings-daily-goal', { clip: { x: 300, y: Math.min(top, bottom - 150) - 34, width: 1590, height: Math.abs(bottom - top) + 130 } });
  });

  await section(page, 'book', async () => {
    await openBook(page);
    await bookTab(page, 'Story');
    await idle(page, 800);
    await openStoryMenu(page);
    await menuItem(page, 'Statistics').click();
    await idle(page, 1500);
    await snapDialog(page, 'statistics-book', 0);
    await statTab(page, 'Time').click();
    await idle(page, 1200);
    await snapDialog(page, 'statistics-time', 0);
    await statTab(page, 'Calendar').click();
    await idle(page, 1200);
    await snapDialog(page, 'statistics-calendar', 0);
    await closeDialog(page);
    await idle(page, 600);
  });

  await section(page, 'global', async () => {
    // the Statistics button in the header of the list of books
    await page.locator('vaadin-dialog[opened]').filter({ has: page.getByRole('tab', { name: 'Branch view', exact: true }) })
      .getByRole('button', { name: 'Exit', exact: true }).first().click().catch(() => {});
    await idle(page, 800);
    await openTab(page, 'Books');
    await idle(page, 600);
    await page.getByRole('button', { name: 'Statistics', exact: true }).first().click();
    await idle(page, 1500);
    await snapDialog(page, 'statistics-global', 0);
    await statTab(page, 'Calendar').click();
    await idle(page, 1200);
    await snapDialog(page, 'statistics-global-calendar', 0);
    await closeDialog(page);
  });

  // the phone layout: the list of books has the Statistics button, the window shows fewer weeks
  await section(page, 'phone', async () => {
    const phone = await launch({ phone: true });
    try {
      const p = phone.page;
      await login(p);
      await sleep(800);
      await p.locator('vaadin-button:has(vaadin-icon[icon$=":chart-line"])').first().click();
      await idle(p, 1500);
      await p.getByRole('tab', { name: 'Calendar', exact: true }).first().click();
      await idle(p, 1200);
      // the page of the window scrolls: down to the calendar
      await p.mouse.move(200, 500);
      await p.mouse.wheel(0, 700);
      await sleep(600);
      await snap(p, 'phone-statistics');
    } finally {
      await phone.browser.close();
    }
  });
});
