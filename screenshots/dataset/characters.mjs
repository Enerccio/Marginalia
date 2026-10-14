// The cast of the Character Tracker plugin (Characters tab of the story editor).
export const characters = [
  { name: 'Harry Potter', tagline: 'Gryffindor Seeker, follows Draconia', note: 'Sixth year. Convinced that Draconia is up to something. Keeps his promise to Ginny only when it suits him.', color: '#b3434a', icon: 'Harry_Potter.jpg' },
  { name: 'Draconia Malfoy', tagline: 'Slytherin, carries a secret task', note: 'Pale, sleeps badly, spends the nights in the Room of Requirement. Her sarcasm is a shield.', color: '#2e7d6b' },
  { name: 'Ginny Weasley', tagline: 'Chaser, does not trust Draconia', note: 'Direct and funny. Notices where Harry is looking.', color: '#c97b2c' },
  { name: 'Hermione Granger', tagline: 'Prefect, sees what others miss', note: 'The first to suspect that Draconia has a reason to be in Hogsmeade.', color: '#8b6bb5' },
  { name: 'Ron Weasley', tagline: 'Keeper, three butterbeers too many', note: 'Hiccups. Jokes when he is worried.', color: '#b59a35' },
  { name: 'Severus Snape', tagline: 'Watches Draconia more closely than he admits', note: 'Potions master and Defence teacher.', color: '#4f6377' },
];

// [from, to, name, tagline, direction] - direction: 'forward' (from -> to), 'both'
export const relationships = [
  ['Harry Potter', 'Draconia Malfoy', 'follows', 'since the summer', 'forward'],
  ['Draconia Malfoy', 'Harry Potter', 'distrusts', 'and cannot stop watching him', 'forward'],
  ['Harry Potter', 'Ginny Weasley', 'promised to tell', 'before doing anything stupid', 'forward'],
  ['Harry Potter', 'Ron Weasley', 'best friends', '', 'both'],
  ['Harry Potter', 'Hermione Granger', 'best friends', '', 'both'],
  ['Ginny Weasley', 'Ron Weasley', 'sister of', '', 'forward'],
  ['Hermione Granger', 'Draconia Malfoy', 'wonders about', 'why she is in Hogsmeade', 'forward'],
  ['Severus Snape', 'Draconia Malfoy', 'watches over', 'Unbreakable Vow', 'forward'],
];
