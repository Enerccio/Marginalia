// Screenshots of the book window: About, Prompts, Story (hero, turn details, prompt, token usage, images...), Branch view,
// Backups, Export. Run on step 13-db-backup or later. Nothing stays changed.
import {
    bookTab,
    closeDialog,
    fill,
    idle,
    login,
    main,
    openBook,
    section,
    sleep,
    snap,
    snapDialog,
    snapPopover
} from '../lib/shot.mjs';
import {partAction, parts, readingStyle} from '../lib/story.mjs';

const scrollStory = (page, where) => page.evaluate(w => {
  const p = document.querySelector('.story-scroll-panel');
  p.scrollTop = w === 'top' ? 0 : w === 'bottom' ? p.scrollHeight : w;
}, where);

await main(async ({ page }) => {
  await login(page);
  await openBook(page);

  await section(page, 'about', async () => {
    await bookTab(page, 'About');
    await snap(page, 'book-about');
  });

  await section(page, 'prompts', async () => {
    await bookTab(page, 'Prompts');
    await snapDialog(page, 'book-prompts', -1);
    await page.mouse.click(1853, 201);                       // the (i) of the master template: the hints
    await sleep(800);
    await snapPopover(page, 'template-hints');
    await page.keyboard.press('Escape');
    await page.mouse.click(900, 480);
  });

  await section(page, 'story', async () => {
    await bookTab(page, 'Story');
    await readingStyle(page, true);
    // the last part from its top (text, not the picture)
    await page.evaluate(() => {
      const p = document.querySelector('.story-scroll-panel');
      const el = [...document.querySelectorAll('.chat-message')].pop();
      p.scrollTop = el.offsetTop - 10;
    });
    await sleep(500);
    await snap(page, 'dev-ui-map-book-not-annotated');
    await scrollStory(page, 'bottom');
    await sleep(400);

    // the part with the picture
    await snap(page, 'part-with-image');
  });

  await section(page, 'hero', async () => {
    await bookTab(page, 'Story');
    await scrollStory(page, 'bottom');
    await page.mouse.click(1857, 822);
    await idle(page, 600);
    await fill(page, 'Scene Setting', 'Hogsmeade evening, behind the Three Broomsticks');
    await fill(page, 'POV Character', 'Harry');
    await fill(page, 'Present Characters', 'Harry; Draconia');
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await snap(page, 'hero-story-editor');
    await snapPopover(page, 'new-turn-instructions');
    await page.keyboard.press('Escape');
    await sleep(400);
  });

  await section(page, 'turn-details', async () => {
    await bookTab(page, 'Story');
    // the details of a part in the middle of the story, so that the popover has room below its button
    const part = parts(page).nth(2);
    await part.scrollIntoViewIfNeeded();
    await page.evaluate(() => {
      const el = document.querySelectorAll('.chat-message')[2];
      document.querySelector('.story-scroll-panel').scrollTop = el.offsetTop - 20;
    });
    await sleep(400);
    await part.getByRole('button', { name: 'Turn Details' }).click();
    await idle(page, 700);
    await snapPopover(page, 'turn-details');
    await page.keyboard.press('Escape');
    await page.mouse.click(1000, 300);
    await sleep(300);
  });

  await section(page, 'prompt', async () => {
    await partAction(page, -1, 'Show Prompt');
    await idle(page, 800);
    await snapDialog(page, 'dev-view-prompt');
    await closeDialog(page);
  });

  await section(page, 'token-usage', async () => {
    await partAction(page, -1, 'Token Usage');
    await idle(page, 1200);
    await snapDialog(page, 'token-usage');
    await closeDialog(page);
  });

  await section(page, 'highlight', async () => {
    await page.mouse.click(534, 822);
    await idle(page, 400);
    await page.getByText('Highlight Generated Text', { exact: true }).click();
    await idle(page, 800);
    await scrollStory(page, 'bottom');
    await page.mouse.click(534, 822);
    await idle(page, 400);
    await snap(page, 'highlight-generated');
    await page.keyboard.press('Escape');
    await page.mouse.click(534, 822);                          // menu closed again, highlight off
    await idle(page, 300);
    await page.getByText('Highlight Generated Text', { exact: true }).click().catch(() => {});
  });

  await section(page, 'summaries-button', async () => {
    await page.mouse.move(460, 822);
    await sleep(1800);
    await snap(page, 'summaries-menu-button', { clip: { x: 410, y: 790, width: 240, height: 108 } });
  });

  await section(page, 'branch-view', async () => {
    await bookTab(page, 'Branch view');
    await snap(page, 'story-tree');
    await page.mouse.click(250, 160);                       // the search box of the branch view
    await page.keyboard.type('wand');
    await page.keyboard.press('Enter');
    await sleep(1500);
    await snap(page, 'story-tree-search');
  });

  await section(page, 'backups', async () => {
    await bookTab(page, 'Backups');
    await snap(page, 'book-backups');
  });
});
