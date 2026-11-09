// Manga Generator: the window opened from the settings menu of the story, a manga with a section whose script and tags the (fake) model wrote.
// Pictures: plugin-manga (the window with the script of the section), plugin-manga-tags (the same section on the Tags tab).
// State: a COPY of 13-db-backup (the manga is kept with the book, the data is discarded). The answers are queued in the fake model.
import {bookTab, dialog, idle, llm, login, main, menuItem, openBook, sleep, snapDialog} from '../lib/shot.mjs';
import {clickCogs} from '../lib/story.mjs';

const SCRIPT = `**Page 1**

**Panel 1** (wide establishing shot): A reed field at night, an enormous low moon, a long broken road of light on the black water. The two small figures in the right corner, one with a headlamp.

**Panel 2** (medium): Tomáš on one knee in the mud, the cracked headlamp in his hand, counting his fingers. *Caption: Hands, two. Legs, both working.*

**Panel 3** (close-up): A voice out of the reeds. Mizuki with the strap of her bag wound around her wrist, half of her face in the lamp. **Mizuki:** "Bag is okay. I have bandage."

**Page 2**

**Panel 1** (wide): Tomáš, red-eared, one finger raised toward the dark water. **Tomáš:** "Fifty-eight. No signal." A small phone glows in his palm.

**Panel 2** (medium, low angle): Mizuki's eyes, narrowed. **Mizuki:** "Bad word?"`;

const TAGS = `**Page 1, Panel 1:** masterpiece, best quality, no humans, reeds, fantasy landscape, night, huge moon, black water, moonlight on the water, wide shot
**Page 1, Panel 2:** 1boy, short dark hair, coat, kneeling in mud, headlamp, cracked glass, tired, medium shot
**Page 1, Panel 3:** 1girl, black hair, grey jacket, bag strap around wrist, headlamp light on face, reeds, night, close-up, small smile`;

/** Scrolls the list of the sections back to its top (it follows the text that is written). */
const toTop = d => d.locator('.manga-card').first().evaluate(card => {
  for (let el = card.parentElement; el; el = el.parentElement || el.getRootNode().host) if (el.scrollTop > 0) el.scrollTop = 0;
});

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
  await toTop(d);
  await snapDialog(page, 'plugin-manga');

  await d.getByRole('tab', { name: 'Tags', exact: true }).first().click();
  await idle(page, 700);
  await llm('/_queue', { reply: TAGS });
  await d.locator('vaadin-button:has(vaadin-icon[icon$=":wand-magic-sparkles"])').first().click();
  await d.getByText('masterpiece', { exact: false }).first().waitFor({ timeout: 30000 });
  await sleep(2000);
  await toTop(d);
  await snapDialog(page, 'plugin-manga-tags');
});
