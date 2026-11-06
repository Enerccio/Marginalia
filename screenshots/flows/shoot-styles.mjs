// The screens of the text styles, the export settings and the pictures of a part (1.6.0): the Styles tab of the Settings with the horror style of the
// styling guide imported (settings-styles, settings-styles-css), the Exports tab (settings-exports), the sub menus of the story editor (story-style-menu,
// story-part-menu), the story in the horror style (story-style-horror), the position of a picture and its preview (images-position, images-preview), and the
// reader with the menu of the styles (viewer-style-menu). Run on a COPY of 13-db-backup: it saves the settings of the administrator (the imported style).
import path from 'node:path';
import {
  bookTab,
  CONTENT,
  dialog,
  goto,
  idle,
  login,
  main,
  menuItem,
  openBook,
  openTab,
  press,
  repo,
  section,
  sleep,
  snap,
  snapDialog,
  users
} from '../lib/shot.mjs';
import {openStoryMenu, parts} from '../lib/story.mjs';

/** The union of the rectangles of the open menus (a menu and its sub menus), as a clip with a little air around it. */
const openMenus = page => page.evaluate(() => {
  const boxes = [...document.querySelectorAll('vaadin-menu-bar-list-box, vaadin-context-menu-list-box')]
    .map(e => e.getBoundingClientRect()).filter(b => b.width > 50 && b.height > 50);
  const x1 = Math.min(...boxes.map(b => b.x)), y1 = Math.min(...boxes.map(b => b.y));
  const x2 = Math.max(...boxes.map(b => b.x + b.width)), y2 = Math.max(...boxes.map(b => b.y + b.height));
  return { x: Math.max(0, x1 - 12), y: Math.max(0, y1 - 12), width: x2 - x1 + 24, height: y2 - y1 + 24 };
});

const horror = path.join(repo, 'manual/files/horror-book.style.json');

await main(async ({ page }) => {
  await login(page);

  await section(page, 'styles', async () => {
    await openTab(page, 'Settings');
    await page.getByRole('tab', { name: 'Styles', exact: true }).click();
    await idle(page, 900);
    // the style of the styling guide: imported, then selected
    await page.locator('input[type=file]').first().setInputFiles(horror);
    await idle(page, 1500);
    await press(page, 'Save');                                   // stored: no "Unsaved changes" in the picture
    await idle(page, 1800);
    await page.locator('vaadin-upload-file [part="remove-button"], vaadin-upload-file vaadin-button[part="remove-button"]').first().click().catch(() => {});
    await page.getByText('Horror book', { exact: false }).first().click();
    await idle(page, 1200);
    await snap(page, 'settings-styles', { clip: CONTENT });
    await page.getByRole('tab', { name: 'Custom CSS', exact: true }).click();
    await idle(page, 900);
    await snap(page, 'settings-styles-css', { clip: CONTENT });
  });

  await section(page, 'exports', async () => {
    await openTab(page, 'Settings');
    await page.getByRole('tab', { name: 'Exports', exact: true }).click();
    await idle(page, 900);
    await page.getByText('HTML (.html)', { exact: true }).click();
    await idle(page, 900);
    await snap(page, 'settings-exports', { clip: CONTENT });
  });

  await section(page, 'menus', async () => {
    await openBook(page);
    await bookTab(page, 'Story');
    await idle(page, 800);
    // Appearance > Change Styles For Text
    await openStoryMenu(page);
    await page.getByRole('menuitem', { name: /^Appearance/ }).first().click();
    await idle(page, 500);
    await page.getByRole('menuitem', { name: /^Change Styles For Text/ }).first().click();
    await idle(page, 600);
    await snap(page, 'story-style-menu', { clip: await openMenus(page) });
    await page.getByRole('menuitem', { name: 'Horror book', exact: true }).first().click();
    await idle(page, 1200);
    // the horror look, scrolled to the start of the last part
    await page.evaluate(() => {
      const p = document.querySelector('.story-scroll-panel');
      const el = [...document.querySelectorAll('.chat-message')].pop();
      if (p && el) p.scrollTop = el.offsetTop - 8;
    });
    await idle(page, 700);
    await snap(page, 'story-style-horror');
    // the menu of a part with a sub menu opened
    const last = parts(page).last();
    await last.locator('.chat-message-content > vaadin-horizontal-layout > vaadin-horizontal-layout > vaadin-button.margin-left-auto').first().click();
    await idle(page, 500);
    await page.getByRole('menuitem', { name: /^Inspect/ }).first().click();
    await idle(page, 600);
    await snap(page, 'story-part-menu', { clip: await openMenus(page) });
    await page.keyboard.press('Escape');
    await page.keyboard.press('Escape');
    await page.mouse.click(1000, 300);
    await idle(page, 600);
  });

  await section(page, 'images', async () => {
    // back to the Normal look: a style with a text width keeps the text in a narrow column, outside of which a picture does not wrap
    await openStoryMenu(page);
    await page.getByRole('menuitem', { name: /^Appearance/ }).first().click();
    await idle(page, 500);
    await page.getByRole('menuitem', { name: /^Change Styles For Text/ }).first().click();
    await idle(page, 600);
    await page.getByRole('menuitem', { name: 'Normal', exact: true }).first().click();
    await idle(page, 1000);
    // the picture of the last part (07-images): at the left of the middle of the text, the text flowing around it
    const last = parts(page).last();
    await last.locator('.chat-message-content > vaadin-horizontal-layout > vaadin-horizontal-layout > vaadin-button.margin-left-auto').first().click();
    await idle(page, 500);
    await menuItem(page, 'Images').click();
    await idle(page, 900);
    const d = dialog(page);
    // the 3x3 picker names its squares (Top left ... Bottom right): the middle left one, the text flowing around the picture
    await page.getByRole('button', { name: 'Center', exact: true }).click();
    await idle(page, 900);
    await d.getByRole('checkbox', { name: 'Text flows around the image', exact: true }).check();
    await idle(page, 900);
    await snapDialog(page, 'images-position');
    await press(dialog(page), 'Preview as in the viewer');
    await idle(page, 1800);
    // the picture sits below the first blocks of the text: scroll the preview until the text beside it shows
    await idle(page, 700);
    await snapDialog(page, 'images-preview', 1);
    await press(dialog(page), 'Close');
    await idle(page, 500);
    await press(dialog(page), 'Close');
    await idle(page, 600);
  });

  await section(page, 'viewer', async () => {
    await login(page, users.admin, '/view');
    await sleep(1200);
    await page.getByText('Unregistered', { exact: false }).first().click();
    await idle(page, 2000);
    await page.locator('vaadin-button, button').filter({ has: page.locator('vaadin-icon[icon$=":paintbrush"], vaadin-icon[icon$=":magic"], vaadin-icon[icon$=":pencil"]') }).last().click().catch(() => {});
    await page.mouse.click(1875, 37);
    await idle(page, 900);
    await snap(page, 'viewer-style-menu', { clip: { x: 1320, y: 0, width: 600, height: 220 } });
  });
});
