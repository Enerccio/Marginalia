// Screens that need something temporary: the language selector, an error from the model, the context warning, an
// extension that fails verification, the first part of a new book. Run it on a throwaway copy of step 13-db-backup
// (bin/restore.sh 13-db-backup first and again afterwards): it changes the data.
import path from 'node:path';
import {
    bookTab,
    dialog,
    fill,
    idle,
    llm,
    login,
    main,
    openBook,
    openTab,
    press,
    root,
    section,
    showToasts,
    sleep,
    snap
} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);

  await section(page, 'language', async () => {
    await openTab(page, 'Books');
    await page.mouse.click(146, 774);                           // the Language selector in the footer
    await sleep(600);
    console.log('  languages:', (await page.getByRole('option').allInnerTexts()).join(' | '));
    await snap(page, 'language-selector', { clip: { x: 0, y: 600, width: 292, height: 332 } });
    await page.getByRole('option').filter({ hasText: '日本語' }).first().click();
    await idle(page, 2500);
    await snap(page, 'ui-japanese');
    await page.mouse.click(146, 774);
    await sleep(500);
    await page.getByRole('option').filter({ hasText: 'English' }).first().click();
    await idle(page, 2500);
  });

  await section(page, 'quick-start', async () => {
    await openTab(page, 'Books');
    await press(page, 'Add Book');
    await dialog(page).locator('input').first().fill('Test Book');
    await press(dialog(page), 'Yes');
    await page.getByText('Branch view', { exact: true }).first().waitFor();
    await idle(page, 800);
    await bookTab(page, 'Story');
    await page.mouse.click(1857, 822);
    await idle(page, 600);
    await fill(page, 'Scene Setting', 'Hogsmeade evening, in front of the Three Broomsticks');
    await fill(page, 'POV Character', 'Harry');
    await fill(page, 'Present Characters', 'Harry; Hermione, Ron; Ginny; Draconia, Crabbe, Goyle');
    await fill(page, 'Instructions', 'Harry, Ron and Hermione walk out of the pub after a few butterbeers, Ron hiccuping. Hermione shakes her head: Ron drank too much, but she is not really angry. Ginny walks towards them and tries to get Harry to join her for a visit of Weasleys\' Wizard Wheezes, being very pushy. Harry turns her down gently, his eyes sweeping the street. His worry comes true when he sees Draconia with Crabbe and Goyle on the other side. Draconia stops and sees Harry with Ginny. She starts to walk towards them, furious.');
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await snap(page, 'quick-start-first-part');
    // an error of the model: the answer is a 401, the dialog tells what to fix
    await llm('/_queue', { status: 401, message: 'Incorrect API key provided.' });
    await showToasts(page);
    await press(page, 'Generate', { exact: false, wait: 300 });
    await sleep(3000);
    await snap(page, 'error-dialog', { clip: { x: 0, y: 790, width: 700, height: 142 } });
  });

  await section(page, 'context-warning', async () => {
    // make the shared provider so small that the prompt cannot fit, then generate
    await openTab(page, 'Inference Providers');
    await page.mouse.click(1850, 228);
    await idle(page, 800);
    await fill(dialog(page), 'Max Context Size', '2200');
    await fill(dialog(page), 'Max Response Tokens', '1000');
    await press(dialog(page), 'OK');
    await idle(page, 800);
    await openBook(page);
    await bookTab(page, 'Story');
    await page.mouse.click(1857, 822);
    await idle(page, 600);
    await fill(page, 'Scene Setting', 'Hogsmeade evening');
    await fill(page, 'POV Character', 'Harry');
    await fill(page, 'Present Characters', 'Harry');
    await fill(page, 'Instructions', 'Harry stands alone in the fog and listens to the bell.');
    await showToasts(page);
    await press(page, 'Generate', { exact: false, wait: 800 });
    await sleep(2500);
    await snap(page, 'context-warning', { clip: { x: 0, y: 770, width: 960, height: 162 } });
  });

  await section(page, 'verification', async () => {
    // a plugin whose JAR was patched to name a method that does not exist (out/broken, made by bin/make-broken-plugin.py),
    // and another one that is stopped (Start button)
    await press(page, 'Admin');
    await idle(page, 600);
    await openTab(page, 'Extensions');
    await page.getByRole('button', { name: 'Stop', exact: true }).nth(1).click();          // chaptermarker
    await idle(page, 1500);
    await page.getByRole('button', { name: 'Unload', exact: true }).first().click();        // authorsnote
    await idle(page, 800);
    await press(dialog(page), 'Yes');
    await idle(page, 2000);
    await page.reload();
    await idle(page, 1500);
    await press(page, 'Admin');
    await openTab(page, 'Extensions');
    await page.locator('vaadin-upload input[type=file]').setInputFiles(path.join(root, 'out/broken/authorsnote-1.3.0.jar'));
    await page.getByText('Verification Report', { exact: true }).waitFor({ timeout: 20000 });
    await sleep(500);
    await snap(page, 'extension-verification');
  });
});

// (more sections are appended below)
