// The redraft window beside the story and the panel of the runs (1.8.0). Pictures: redraft-beside-story (the window streaming at the right of the story), runs-panel (the window folded into the panel, opened).
// State: 13-db-backup (a copy is discarded: the redraft is not applied).
import {bookTab, dialog, fill, idle, llm, login, main, openBook, press, section, sleep, snap} from '../lib/shot.mjs';
import {partAction} from '../lib/story.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await section(page, 'redraft-window', async () => {
    await partAction(page, 1, 'Redraft');
    await idle(page, 800);
    const d = dialog(page);
    await fill(d, 'Instructions for the redraft', 'Make it colder and shorter; keep the moon.');
    await llm('/_queue', { reply: 'The cold woke her first. It came up through her cheek and the sleeve of her grey jacket, and Mizuki lay still while her eyes opened on black-green stalks taller than her head. Above them hung a moon so large and low that it seemed to rest on the reeds.\n\nThere was no arch. No cellar. No iron ring.\n\n"Tomáš," she said in Japanese. "Where are we?"\n\nHe lay face down a few steps away, one arm over a tussock, his headlamp burning across the black water. She turned him over and found a slow, stubborn pulse.', delayMs: 2500 });
    await llm('/_pace', { chunkDelayMs: 120 });
    await press(d, 'Redraft', { wait: 600 });
    await d.getByText('Redrafted text', { exact: false }).first().waitFor({ timeout: 30000 });
    await sleep(4500);
    await snap(page, 'redraft-beside-story');
    // fold the window into the panel of the runs (the arrow in its title)
    await page.mouse.click(1878, 115);
    await idle(page, 1200);
    await sleep(1500);
    await snap(page, 'runs-panel');
  });
});
