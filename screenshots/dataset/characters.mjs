// The cast of the Character Tracker plugin (Characters tab of the story editor).
export const characters = [
  { name: 'Tomáš Hájek', tagline: 'Czech cellar guide, hides his Japanese', note: 'Twenty-nine. Learned Japanese from novels and subtitles, so he speaks it stiff and bookish. Understood her first mutter and pretended he did not.', color: '#b3434a', icon: 'portrait.jpg' },
  { name: 'Mizuki Hayashida', tagline: 'Nurse from Sendai, organises', note: 'Twenty-seven. Natural, everyday Japanese, a little teasing; broken English. Mutters in Japanese when she is alone.', color: '#2e7d6b' },
  { name: 'Saellen', tagline: 'Ferry-woman of the Lyrasil', note: 'Broad, loud, kind. The first to feed them, and the first to teach them a word of Vrakht.', color: '#c97b2c' },
  { name: 'Steing', tagline: 'Life-tax assessor with a ledger', note: 'Thin, fifty-one, patronizing. Writes down the sounds of Japanese as if it were a chant.', color: '#8b6bb5' },
  { name: 'Paskal', tagline: 'Conscript from the steppe', note: 'Nineteen, homesick, cheerful. Plain soldier\'s speech.', color: '#b59a35' },
  { name: 'Aelmoria', tagline: 'The reed-mother', note: 'Speaks Aethel-Vael, soft and flowing. Nobody in the Dominion admits to having met her.', color: '#4f6377' },
];

// [from, to, name, tagline, direction] - direction: 'forward' (from -> to), 'both'
export const relationships = [
  ['Tomáš Hájek', 'Mizuki Hayashida', 'hides from', 'that he understands her', 'forward'],
  ['Mizuki Hayashida', 'Tomáš Hájek', 'teases', 'and lets him translate', 'forward'],
  ['Saellen', 'Mizuki Hayashida', 'feeds', 'on the ferry', 'forward'],
  ['Saellen', 'Tomáš Hájek', 'teaches words to', 'mosh, vat', 'forward'],
  ['Steing', 'Tomáš Hájek', 'writes down', 'every sound he makes', 'forward'],
  ['Steing', 'Mizuki Hayashida', 'writes down', 'her Japanese as a chant', 'forward'],
  ['Paskal', 'Steing', 'escorts', '', 'forward'],
  ['Saellen', 'Aelmoria', 'hums the river song of', 'in Aethel-Vael', 'forward'],
];
