// Manga Generator: the window opened from the settings menu of the story, a manga with a section whose script and tags the (fake) model wrote.
// Pictures: plugin-manga (the window with the script of the section), plugin-manga-tags (the same section on the Tags tab).
// State: a COPY of 13-db-backup (the manga is kept with the book, the data is discarded). The answers are queued in the fake model.
import {bookTab, dialog, idle, llm, login, main, menuItem, openBook, sleep, snapDialog} from '../lib/shot.mjs';
import {clickCogs} from '../lib/story.mjs';

const SCRIPT = `**Page 1**

**Panel 1** (wide establishing shot): Hogsmeade's main street at dusk, the fog rolling between the lamps like a second village. The Three Broomsticks in the left corner, its windows warm and yellow.

**Panel 2** (medium): Harry steps out into the cold, collar up, glancing back at his friends laughing behind the glass. *Caption: He should have stayed with them.*

**Panel 3** (close-up): A cold voice out of the fog. Draconia under the lamp, hands in the pockets of a long green coat, half of her face in the light. **Draconia:** "How touching."

**Page 2**

**Panel 1** (wide): Ron, red-faced, between Harry and Hermione, one finger raised. **Ron:** "Get lost, Malfoy." A small *hic* floats over his head.

**Panel 2** (medium, low angle): Draconia's grey eyes, narrowed. **Draconia:** "Language, Weasley. There are Aurors."`;

const TAGS = `**Page 1, Panel 1:** masterpiece, best quality, no humans, village, medieval fantasy, dusk, thick fog, street lamps, warm light from windows, pub, wide shot
**Page 1, Panel 2:** 1boy, black hair, round glasses, scarf, winter coat, collar up, looking back, glass window, laughing friends in background, medium shot
**Page 1, Panel 3:** 1girl, platinum blonde hair, grey eyes, long green coat, hands in pockets, street lamp, fog, half face in light, close-up, smirk`;

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await clickCogs(page);
  await menuItem(page, 'Manga Generator').click();
  await idle(page, 1200);
  const d = dialog(page);
  await d.locator('vaadin-button:has(vaadin-icon[icon$=":plus"])').first().click();      // a new manga
  await idle(page, 800);
  await d.getByRole('button', { name: 'Add section', exact: true }).click();
  await idle(page, 1000);

  await llm('/_queue', { reply: SCRIPT });
  await d.locator('vaadin-button:has(vaadin-icon[icon$=":wand-magic-sparkles"])').first().click();
  await d.getByText('Panel 3', { exact: false }).first().waitFor({ timeout: 30000 });
  await sleep(2000);
  await snapDialog(page, 'plugin-manga');

  await d.getByRole('tab', { name: 'Tags', exact: true }).first().click();
  await idle(page, 700);
  await llm('/_queue', { reply: TAGS });
  await d.locator('vaadin-button:has(vaadin-icon[icon$=":wand-magic-sparkles"])').first().click();
  await d.getByText('masterpiece', { exact: false }).first().waitFor({ timeout: 30000 });
  await sleep(2000);
  await snapDialog(page, 'plugin-manga-tags');
});
