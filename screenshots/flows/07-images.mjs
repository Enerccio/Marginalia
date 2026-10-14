// Step 07-summaries -> 08-images: the picture of Harry (Wikimedia Commons, see manual/credits.md) on the last part.
import path from 'node:path';
import {
    bookTab,
    dialog,
    fill,
    idle,
    login,
    main,
    openBook,
    openTab,
    press,
    root,
    sleep,
    snap,
    snapDialog
} from '../lib/shot.mjs';
import {partAction} from '../lib/story.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await partAction(page, -1, 'Images');
  await idle(page, 600);
  await page.locator('vaadin-dialog[opened] vaadin-upload input[type=file]').setInputFiles(path.join(root, 'assets/Harry_Potter.jpg'));
  await sleep(4000);
  await fill(dialog(page), 'Caption', 'Harry Potter');
  await sleep(500);
  await snapDialog(page, 'images-dialog');
  await press(dialog(page), 'Close');
  await idle(page, 800);

  // a second upload that is taken off the part again: the file stays in Resources, used by nothing
  await partAction(page, -1, 'Images');
  await page.locator('vaadin-dialog[opened] vaadin-upload input[type=file]').setInputFiles(path.join(root, 'assets/Harry_Potter.jpg'));
  await sleep(4000);
  await snapDialog(page, '_images-2');
  await page.getByRole('button', { name: 'Remove image' }).last().click();
  await sleep(600);
  await press(dialog(page), 'Close');
  await idle(page, 800);
  await snap(page, '_story-with-image');
  // leave the book, look at Resources
  await press(page, 'Exit');
  await idle(page, 800);
  await openTab(page, 'Resources');
  await snap(page, '_resources');
});
