// Audiobook: installs the plugin, adds a speech server (the fake model also answers the Chatterbox-TTS-Server calls, see llm/mock-llm.mjs, and labels the
// parts, see the rule `audiobook-labels` of llm/rules.mjs), the actors of the user, the cast and the pronunciation of the book, converts two parts labelled,
// opens the studio, the export dialog, the automatic conversion setting, the command bar, the notification list and the player of the reader.
// Pictures: plugin-audiobook-settings, -actor, -convert, -cast, -pronunciation, -queue, -studio, -export, -auto, -command, -notifications, -player.
// State: a COPY of 13-db-backup (the plugin, its settings and the audio are not part of the other pictures, so the data is discarded).
import fs from 'node:fs';
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {
    bookTab,
    CONTENT,
    dialog,
    goto,
    idle,
    llmUrl,
    login,
    main,
    menuItem,
    openBook,
    openTab,
    press,
    repo,
    root,
    section,
    sleep,
    snap,
    snapDialog
} from '../lib/shot.mjs';

const BAR = 'input[placeholder="Type / for commands"]';
// the lower part of the story tab: the command line with everything that opens above it
const LOWER = { x: 400, y: 330, width: 1500, height: 520 };

/** Types a voice into a combo box of the dialog (the box at that index among the voice fields). */
async function setVoice(d, index, text) {
  const input = d.locator('vaadin-custom-field vaadin-combo-box input').nth(index);
  await input.fill(text);
  await input.press('Enter');
  await sleep(300);
}

/** Picks an actor in the select at that index of the dialog. */
async function pickActor(page, d, index, name) {
  await d.locator('vaadin-select').nth(index).click();
  await sleep(300);
  await page.getByRole('option', { name, exact: true }).click();
  await sleep(300);
}

async function openTools(page, item) {
  await page.locator('vaadin-menu-bar-button:has(vaadin-icon[icon$=":cogs"])').first().click();
  await idle(page, 700);
  await menuItem(page, item).click();
  await idle(page, 1500);
}

await main(async ({ page }) => {
  await login(page);

  await section(page, 'install', async () => {
    await page.getByRole('button', { name: 'Admin' }).click();
    await idle(page, 600);
    await page.getByText('Extensions', { exact: true }).first().click();
    await idle(page, 800);
    const jar = path.join(repo, 'marginalia/plugins/audiobook/target/audiobook-1.7.0.jar');
    if (!fs.existsSync(jar)) throw new Error('not built: ' + jar);
    await page.locator('vaadin-upload input[type=file]').setInputFiles(jar);
    await idle(page, 6000);
    await page.getByRole('button', { name: 'Refresh' }).click();
    await idle(page, 1500);
  });

  await section(page, 'settings', async () => {
    await openTab(page, 'Settings');
    await page.getByRole('tab', { name: 'Extension Settings', exact: true }).click();
    await idle(page, 900);
    await page.getByText('Audiobook Settings', { exact: true }).click();
    await idle(page, 900);
    // the first plus of the panel is the one of the backends (the actors come after them)
    await page.locator('vaadin-button:has(vaadin-icon[icon$=":plus"])').first().click();
    await idle(page, 900);
    await page.getByLabel('Name', { exact: true }).first().fill('Chatterbox');
    await page.getByLabel('Address', { exact: true }).first().fill(llmUrl.replace(/\/v1$/, ''));
    // the narrator voice is a voice field (a combo box inside): the file of a voice of the server
    const narrator = page.locator('vaadin-custom-field').filter({ hasText: 'Narrator voice' }).locator('vaadin-combo-box input').first();
    await narrator.fill('Henry.wav');
    await narrator.press('Enter');
    await page.getByRole('checkbox', { name: 'The server is on this machine or on my network' }).check();
    await idle(page, 600);
    await page.getByRole('button', { name: 'Test connection' }).click();
    await idle(page, 2500);
    // the actors: a name and a voice, and for one of them a voice for a mood
    const newActor = async (name, voice, calm) => {
      await page.locator('vaadin-button:has(vaadin-icon[icon$=":plus"])').last().click();
      await idle(page, 700);
      const d = dialog(page);
      await d.locator('vaadin-text-field input').first().fill(name);
      await setVoice(d, 0, voice);
      if (calm) {
        await d.getByRole('button', { name: 'A voice for a mood or a language' }).click();
        await sleep(300);
        await setVoice(d, 1, calm);
      }
      if (calm) await snapDialog(page, 'plugin-audiobook-actor');
      await press(d, 'Save');
      await idle(page, 900);
    };
    await newActor('Henry', 'Henry.wav');
    await newActor('Tomas', 'Tomas.wav', 'TomasCalm.wav');
    await newActor('Mizuki', 'Mizuki.wav');
    await newActor('Stranger', 'Stranger.wav');
    await page.getByText('Backends', { exact: true }).scrollIntoViewIfNeeded();
    await idle(page, 500);
    await snap(page, 'plugin-audiobook-settings', { clip: CONTENT });
    await press(page, 'Save');
    await idle(page, 1500);
  });

  await section(page, 'cast', async () => {
    await openBook(page);
    await bookTab(page, 'Story');
    await idle(page, 800);
    await openTools(page, 'Convert to audio');
    await press(dialog(page), 'Cast');
    await idle(page, 1000);
    const c = dialog(page);
    await pickActor(page, c, 0, 'Henry');
    await pickActor(page, c, 1, 'Stranger');
    await press(c, 'Add the characters of the book');
    await idle(page, 800);
    await pickActor(page, c, 2, 'Tomas');
    await pickActor(page, c, 3, 'Mizuki');
    await snapDialog(page, 'plugin-audiobook-cast');
    await press(c, 'Save');
    await idle(page, 1200);
    // the pronunciation: a word of the book that is read as it is spelled
    await press(dialog(page), 'Pronunciation');
    await idle(page, 1000);
    const p = dialog(page);
    await p.getByRole('button', { name: 'Add a word' }).click();
    await idle(page, 500);
    await p.locator('vaadin-text-field input').nth(1).fill('Silvakhor');
    await p.locator('vaadin-text-field input').nth(2).fill('Sil-vakhor');
    await snapDialog(page, 'plugin-audiobook-pronunciation');
    await press(p, 'Save');
    await idle(page, 1200);
  });

  await section(page, 'convert', async () => {
    const d = dialog(page);
    await d.getByLabel('Last part', { exact: true }).fill('2');
    await d.getByRole('checkbox', { name: /Label the parts with a language model/ }).check();
    await idle(page, 600);
    await snapDialog(page, 'plugin-audiobook-convert');
    await press(d, 'Start');
    for (let i = 0; i < 90; i++) {
      const text = (await d.innerText()).replace(/Convert to audio|Clear finished/g, '');
      if ((text.match(/Done|Failed/g) || []).length >= 2) break;
      await sleep(1000);
    }
    await idle(page, 800);
    await snapDialog(page, 'plugin-audiobook-queue');
    await press(d, 'Close');
    await idle(page, 600);
  });

  await section(page, 'studio', async () => {
    await openTools(page, 'Audiobook studio');
    const d = dialog(page);
    await d.locator('vaadin-grid-cell-content', { hasText: 'Tomáš' }).first().click();
    await idle(page, 900);
    await snapDialog(page, 'plugin-audiobook-studio');
    await press(d, 'Close');
    await idle(page, 600);
  });

  await section(page, 'export', async () => {
    await page.locator('vaadin-menu-bar-button:has(vaadin-icon[icon$=":cogs"])').first().click();
    await idle(page, 700);
    await menuItem(page, 'Export story').click();
    await idle(page, 1000);
    const d = dialog(page);
    await d.locator('vaadin-select').filter({ hasText: 'Format' }).first().click();
    await sleep(400);
    await page.getByRole('option', { name: /one mp3 with chapter marks/ }).click();
    await sleep(600);
    await snapDialog(page, 'plugin-audiobook-export');
    await press(d, 'Close').catch(() => {});
    await page.keyboard.press('Escape');
    await idle(page, 600);
  });

  await section(page, 'auto', async () => {
    await bookTab(page, 'Prompts');
    await idle(page, 1200);
    // the settings of every extension are a closed panel of an accordion: open the Audiobook one
    const box = page.getByLabel('Convert new parts to audio by itself', { exact: true }).first();
    if (!(await box.isVisible().catch(() => false))) {
      const heading = page.locator('vaadin-accordion-heading').filter({ hasText: 'Audiobook' }).first();
      await heading.scrollIntoViewIfNeeded();
      await heading.click();
      await idle(page, 600);
    }
    await box.scrollIntoViewIfNeeded();
    // scrolling to the check box can push the whole window sideways: back to the left edge
    await box.evaluate(el => { for (let e = el; e; e = e.parentElement || (e.getRootNode() && e.getRootNode().host)) e.scrollLeft = 0; });
    await idle(page, 500);
    await snap(page, 'plugin-audiobook-auto');
  });

  await section(page, 'command', async () => {
    await bookTab(page, 'Story');
    await idle(page, 1000);
    const input = page.locator(BAR);
    await input.click();
    await input.fill('');
    await input.pressSequentially('/audiobook-status', { delay: 10 });
    await sleep(500);
    await input.press('Escape');
    await input.click();
    await input.press('Enter');
    await sleep(3000);
    await snap(page, 'plugin-audiobook-command', { clip: LOWER });
  });

  await section(page, 'notifications', async () => {
    // the window of the book covers the navigation: leave it
    await page.getByRole('button', { name: 'Exit', exact: true }).click();
    await idle(page, 1500);
    await openTab(page, 'Settings');
    await page.getByRole('tab', { name: 'Notifications', exact: true }).click();
    await idle(page, 1000);
    // the list is a grid that loads rows as it scrolls: go to the last rows
    await page.locator('vaadin-grid').first().evaluate(g => g.scrollToIndex(1000));
    await idle(page, 600);
    await page.getByText('Convert to audio (automatic)', { exact: false }).first().scrollIntoViewIfNeeded();
    await idle(page, 500);
    await snap(page, 'plugin-audiobook-notifications', { clip: CONTENT });
  });

  await section(page, 'player', async () => {
    const db = path.join(root, '.data/.marginalia/marginalia.sqlite');
    const uuid = execFileSync('sqlite3', ['-readonly', db, "select uuid from manuscripts where name = 'Unregistered' limit 1"]).toString().trim();
    await goto(page, '/view/' + uuid);
    await page.getByLabel('Username', { exact: true }).fill('admin');
    await page.getByLabel('Password', { exact: true }).fill('marginalia');
    await page.getByRole('button', { name: 'Login', exact: true }).click();
    await idle(page, 3500);
    await page.locator('.audiobook-player').waitFor({ timeout: 10000 });
    // the headphones button opens the bar: play, the speed and the part that is heard
    await page.locator('.audiobook-player button, .audiobook-player vaadin-button, .audiobook-player-play').first().click().catch(() => {});
    await idle(page, 1200);
    await snap(page, 'plugin-audiobook-player', { clip: { x: 1380, y: 0, width: 320, height: 130 } });
  });
});
