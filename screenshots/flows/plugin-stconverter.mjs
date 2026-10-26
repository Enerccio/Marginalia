// SillyTavern Converter: the ST Converter tab of the story editor with an uploaded chat (dataset/stchat.mjs), the range chosen and the
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
  { firstMessage: 1, lastMessage: 3, povCharacter: 'Harry', presentCharacters: 'Harry, Draconia',
    sceneSetting: 'The last lamp before the bridge in Hogsmeade, a foggy autumn evening. Cold, quiet, nobody else in the street.',
    instructions: 'Draconia waits under the lamp as if she did not wait for anyone. Harry, who followed her from the Three Broomsticks, steps out of the fog. She says he is late and asks what he wants from her.' },
  { firstMessage: 4, lastMessage: 5, povCharacter: 'Harry', presentCharacters: 'Harry, Draconia',
    sceneSetting: 'The same street, a few minutes later, the fog thicker.',
    instructions: 'Harry asks where she goes at night and why she comes back with mud on her robes. Something moves behind her face; she says she only walks, and starts across the bridge.' },
  { firstMessage: 6, lastMessage: 8, povCharacter: 'Harry', presentCharacters: 'Harry, Draconia',
    sceneSetting: 'The middle of the stone bridge, the stream loud with the autumn rain.',
    instructions: 'Harry follows three steps behind. She stops so suddenly that he nearly walks into her and asks why he cares. He answers that someone has to, and he is the only one who noticed.' },
] };

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 700);
  await sideTab(page, 'ST Converter');
  await page.locator('vaadin-upload input[type=file]').last().setInputFiles(path.join(root, 'dataset/build', CHAT_FILE));
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
