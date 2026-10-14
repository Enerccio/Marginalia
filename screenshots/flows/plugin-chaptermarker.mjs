// Chapter Marker: Markdown headings in two parts, so that the outline shows chapter titles (and the export gets a table of contents).
import {bookTab, idle, login, main, openBook, sleep, snap} from '../lib/shot.mjs';
import {partAction, parts} from '../lib/story.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  for (const [index, heading] of [[0, '# Chapter 1: The Fog on the Main Street'], [3, '# Chapter 2: Behind the Three Broomsticks']]) {
    await partAction(page, index, 'Edit');
    await idle(page, 600);
    const area = parts(page).nth(index).locator('textarea').first();
    const text = await area.inputValue();
    await area.fill(heading + '\n\n' + text);
    await area.dispatchEvent('change');
    await partAction(page, index, 'Save');
    await idle(page, 900);
  }
  await page.evaluate(() => { document.querySelector('.story-scroll-panel').scrollTop = 0; });
  await sleep(600);
  await snap(page, 'plugin-chaptermarker');
});
