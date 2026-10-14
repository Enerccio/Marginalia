// Side Query: a conversation next to the story (two questions).
import {bookTab, field, login, main, openBook, press, sleep, snap} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';
import {sideQuery} from '../dataset/plugins.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 700);
  await sideTab(page, 'Side Query');
  await field(page, 'Lorebook').check();
  await field(page, 'Chat Logs').check();
  await sleep(300);
  const ask = async text => {
    await page.locator('textarea[placeholder="Enter query..."]').fill(text);
    await page.locator('textarea[placeholder="Enter query..."]').dispatchEvent('change');
    await press(page, 'SEND', { exact: false, wait: 500 });
    await sleep(4000);
  };
  for (const q of sideQuery) await ask(q.question);
  // show the end of the conversation
  await page.evaluate(() => {
    const panel = [...document.querySelectorAll('[role="tabpanel"]')].find(p => p.querySelector('textarea[placeholder="Enter query..."]'));
    for (const e of panel.querySelectorAll('*')) {
      if (e.scrollHeight > e.clientHeight + 20 && /auto|scroll/.test(getComputedStyle(e).overflowY)) e.scrollTop = e.scrollHeight;
    }
    document.activeElement && document.activeElement.blur();
  });
  await sleep(500);
  await snap(page, 'plugin-sidequery');
});
