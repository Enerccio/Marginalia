// A SillyTavern chat (.jsonl) for the pictures of the SillyTavern import and of the SillyTavern Converter plugin.
// `node dataset/stchat.mjs` writes dataset/build/Draconia - 2026-09-14.jsonl (the name is the one a chat of SillyTavern has: character - date).
// Original text; the user plays Harry, the character is Draconia.
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
export const CHAT_FILE = 'Draconia - 2026-09-14.jsonl';

const messages = [
  ['Draconia', false, 'The fog had come up from the valley and sat in the main street of Hogsmeade like a second, colder village. Draconia stood under the last lamp before the bridge, collar turned up, pretending not to wait for anyone. "You are late, Potter," she said without turning.'],
  ['Harry', true, '*I stop a few steps away, hands where she can see them.* "I did not know I was expected. I followed you from the Three Broomsticks."'],
  ['Draconia', false, '"I know you did. Half the pub knows you did." She finally looked at him, grey eyes sharp in the lamplight. "What do you want from me?"'],
  ['Harry', true, '"The truth. Where you go at night, and why you keep coming back with mud on your robes."'],
  ['Draconia', false, 'Something moved behind her face, quick and ugly, and was gone. "The truth is dull," she said. "I walk. It is a free village." She started across the bridge, and the fog closed behind her.'],
  ['Harry', true, '*I follow, three steps behind.* "Then you will not mind if I walk too."'],
  ['Draconia', false, 'She stopped at the middle of the bridge, so suddenly that he almost walked into her. Below them the stream was loud with the autumn rain. "One question, Potter, and then you go back to your friends. Why do you care?"'],
  ['Harry', true, '"Because someone has to, and I am the only one who noticed." *I say it more gently than I meant to.*'],
];

const lines = [JSON.stringify({ user_name: 'Harry', character_name: 'Draconia', create_date: '2026-09-14@19h02m00s', chat_metadata: {} })];
let when = Date.parse('2026-09-14T19:02:00Z');
for (const [name, isUser, mes] of messages) {
  when += 90_000;
  lines.push(JSON.stringify({ name, is_user: isUser, is_system: false, send_date: new Date(when).toISOString(), mes, extra: {} }));
}

fs.mkdirSync(path.join(here, 'build'), { recursive: true });
const out = path.join(here, 'build', CHAT_FILE);
fs.writeFileSync(out, lines.join('\n') + '\n');
console.log('wrote', path.relative(path.join(here, '..'), out), `(${messages.length} messages)`);
