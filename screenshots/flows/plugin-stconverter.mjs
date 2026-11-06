// Converter (sillytavern-converter): the Converter tab of the story editor with an uploaded chat (dataset/stchat.mjs), the range chosen and the
// turn details that the (fake) model prepared from it.
// Pictures: plugin-stconverter-chat (the Chat tab, range chosen), plugin-stconverter-turns (the Turn details tab).
// State: a COPY of 13-db-backup (the chat and the turns are kept with the book, the data is discarded).
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {bookTab, idle, llm, login, main, openBook, root, sleep, snap} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';
import {CHAT_FILE} from '../dataset/stchat.mjs';

execFileSync('node', [path.join(root, 'dataset/stchat.mjs')], { stdio: 'inherit' });

const TURNS = { turns: [
  { firstMessage: 1, lastMessage: 3, povCharacter: 'Mizuki', presentCharacters: 'Mizuki, Tomáš',
    sceneSetting: 'The end of the jetty at the ferry landing, night, a lantern on a pole.',
    instructions: 'Mizuki stands at the end of the jetty, afraid of a woman who shouts at her in a language she does not know. Tomáš cannot translate; he can only tell her that the woman is not asking them to leave, and that she holds out a bowl.' },
  { firstMessage: 4, lastMessage: 5, povCharacter: 'Tomáš', presentCharacters: 'Mizuki, Tomáš',
    sceneSetting: 'The jetty, a minute later, the ferry rocking under them.',
    instructions: 'Mizuki steps onto the planks with the soup joke. Tomáš follows and writes the word the woman shouts in his notebook: mosh, and he thinks it means food.' },
  { firstMessage: 6, lastMessage: 8, povCharacter: 'Mizuki', presentCharacters: 'Mizuki, Tomáš',
    sceneSetting: 'The middle of the deck, the river loud against the hull.',
    instructions: 'Mizuki stops so suddenly that Tomáš nearly walks into her and asks why he pretended in the cellar. He answers that he was afraid a stranger who understood her would sound like a threat, and apologises.' },
] };

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 700);
  await sideTab(page, 'Converter');
  await page.locator('vaadin-upload input[type=file]').first().setInputFiles(path.join(root, 'dataset/build', CHAT_FILE));
  await idle(page, 3000);
  await page.getByText('Convert to turn details', { exact: false }).first().click();
  await idle(page, 600);
  await page.getByRole('button', { name: 'All', exact: true }).click();
  await idle(page, 800);
  await snap(page, 'plugin-stconverter-chat');

  await llm('/_queue', { reply: JSON.stringify(TURNS) });
  await page.getByRole('button', { name: 'Convert range', exact: false }).first().click();
  await page.getByText('Turn details (3)', { exact: true }).first().waitFor({ timeout: 30000 });
  await sleep(1500);
  await snap(page, 'plugin-stconverter-turns');
});
