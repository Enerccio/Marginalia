// A SillyTavern chat (.jsonl) for the pictures of the SillyTavern import and of the SillyTavern Converter plugin.
// `node dataset/stchat.mjs` writes dataset/build/Mizuki - 2026-09-14.jsonl (the name is the one a chat of SillyTavern has: character - date).
// Original text; the user plays Tomáš, the character is Mizuki.
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
export const CHAT_FILE = 'Mizuki - 2026-09-14.jsonl';

const messages = [
  ['Mizuki', false, 'The ferry was a flat black shape on the water, and the lantern on its pole swung once, twice, as if somebody on board had decided to be polite. Mizuki stood at the end of the jetty with her bag strapped across her chest, pretending not to be afraid of a woman who was shouting at her in a language she did not know. "Tomáš," she said without turning. "Translate."'],
  ['Tomáš', true, '*I stop a few steps behind her, notebook already open.* "I cannot. It is not English and it is not Japanese. I can only tell you that she is not asking us to leave."'],
  ['Mizuki', false, '"How do you know?" She finally looked at him, and the lantern lit one side of her face. "Because of the tone? You said you do not understand."'],
  ['Tomáš', true, '"The tone. And she is holding out a bowl." *I say it more gently than I meant to.*'],
  ['Mizuki', false, 'Something moved at the corner of her mouth, quick and tired, and was gone. "A bowl," she said in Japanese. "Of course. We fall through a wall, and the first thing the world offers us is soup." She stepped onto the planks, and the ferry rocked under her.'],
  ['Tomáš', true, '*I follow, writing the word she shouts in the margin of the notebook.* "Mosh," *I say, to myself first, and then aloud.* "I think it means food."'],
  ['Mizuki', false, 'She stopped in the middle of the deck, so suddenly that he almost walked into her. Below them the river was loud against the hull. "One question, Tomáš, and then we eat. Why did you pretend, in the cellar?"'],
  ['Tomáš', true, '"Because I was afraid that a stranger who understood you would sound like a threat." *I look at the bowl, not at her.* "I was wrong. I am sorry."'],
];

const lines = [JSON.stringify({ user_name: 'Tomáš', character_name: 'Mizuki', create_date: '2026-09-14@19h02m00s', chat_metadata: {} })];
let when = Date.parse('2026-09-14T19:02:00Z');
for (const [name, isUser, mes] of messages) {
  when += 90_000;
  lines.push(JSON.stringify({ name, is_user: isUser, is_system: false, send_date: new Date(when).toISOString(), mes, extra: {} }));
}

fs.mkdirSync(path.join(here, 'build'), { recursive: true });
const out = path.join(here, 'build', CHAT_FILE);
fs.writeFileSync(out, lines.join('\n') + '\n');
console.log('wrote', path.relative(path.join(here, '..'), out), `(${messages.length} messages)`);
