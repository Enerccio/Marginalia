// Step 05-lorebook -> 06-story: the demo book - settings, lorebook, then the story written against the fake model.
import {bookTab, dialog, field, fill, idle, llm, login, main, openTab, pick, press, sleep, snap} from '../lib/shot.mjs';
import {fillTurn, generate, partAction} from '../lib/story.mjs';
import {BOOK, turns} from '../dataset/story.mjs';

await main(async ({ page }) => {
  await login(page);

  // the book (the window opens on the About tab)
  await openTab(page, 'Books');
  await press(page, 'Add Book');
  await dialog(page).locator('input').first().fill(BOOK);
  await press(dialog(page), 'Yes');
  await page.getByText('Branch view', { exact: true }).first().waitFor();
  await idle(page, 1000);

  for (const tag of ['genderbend', 'harry potter', 'f/m']) {
    const input = field(page, 'Tags');
    await input.click(); await input.fill(tag); await input.press('Enter'); await sleep(300);
  }
  await fill(page, 'Description', 'Sixth year, autumn 1996. A rivalry that will not stay a rivalry: Harry follows Draconia Malfoy through the fog of Hogsmeade. A genderbend retelling of Half-Blood Prince.');

  await bookTab(page, 'Lorebook');
  await pick(page, 'Lorebook', 'Harry Potter - Half-Blood Prince');
  await sleep(800);

  await bookTab(page, 'Story');
  await fillTurn(page, turns.t1);
  await generate(page, turns.t1);

  // t2a, and t2b as its swipe (the active branch ends with t2b until we go back in the branch view)
  await fillTurn(page, turns.t2a);
  await generate(page, turns.t2a);
  await llm('/_queue', { reply: turns.t2b.reply });
  await partAction(page, -1, 'Swipe');
  await page.getByText(turns.t2b.reply.trim().split('\n').pop().slice(-40), { exact: false }).first().waitFor({ timeout: 60000 });
  await idle(page, 1500);
  await bookTab(page, 'Branch view');
  // the left card is t2a (older): click it, the book switches to it and shows the Story tab
  await page.mouse.click(820, 480);
  await idle(page, 1500);

  for (const id of ['t3', 't4', 't5']) {
    await fillTurn(page, turns[id]);
    await generate(page, turns[id]);
  }
  await snap(page, '_t5');
  console.log(JSON.stringify((await llm('/_log')).map(r => ({ stream: r.stream, n: r.messages.length, last: r.messages.at(-1)?.content.slice(-120) })), null, 1));
});
