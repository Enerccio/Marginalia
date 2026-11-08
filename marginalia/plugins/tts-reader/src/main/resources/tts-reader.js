// The read-aloud of the viewer. The body of a function; the arguments of executeJs are
//   $0 bar (the element of the controls)   $1 scroller (the scrolling panel)   $2 content (its children are the parts)
//   $3 play button  $4 pause button  $5 stop button  $6 settings button  $7 settings panel
//   $8 voice select  $9 rate input  $10 pitch input  $11 language code of the book  $12 text of the "default voice" option
// Everything happens in the browser: the voices are the ones of the device, the settings stay in its localStorage.
const bar = $0, scroller = $1, content = $2;
const playBtn = $3, pauseBtn = $4, stopBtn = $5, gearBtn = $6, panel = $7;
const voiceSel = $8, rateIn = $9, pitchIn = $10;
const lang = ($11 || '').toLowerCase();
const autoLabel = $12;

const synth = window.speechSynthesis;
if (!synth || typeof window.SpeechSynthesisUtterance !== 'function') {
  bar.classList.add('tts-reader-unsupported');
  return;
}
if (bar._ttsDispose) {
  bar._ttsDispose();
}

const STORAGE_KEY = 'marginalia.tts-reader';
const READING = 'tts-reader-reading';
const PLAYING = 'tts-reader-playing';
const ACTIVE = 'tts-reader-active';
const CHUNK = 200;
const LOAD_WAIT_MS = 4000;

let prefs = { voice: '', rate: 1, pitch: 1 };
try {
  const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
  if (typeof saved.voice === 'string') prefs.voice = saved.voice;
  if (isFinite(saved.rate)) prefs.rate = Math.min(2, Math.max(0.5, +saved.rate));
  if (isFinite(saved.pitch)) prefs.pitch = Math.min(2, Math.max(0.5, +saved.pitch));
} catch (e) { /* storage blocked: the defaults */ }

function savePrefs() {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs)); } catch (e) { /* blocked */ }
}

// --- state ---
let state = 'idle';       // idle | playing | paused
let token = 0;            // a callback of a cancelled utterance sees another token and does nothing
let part = null;          // the element of the part that is read
let chunks = [];
let chunkIndex = 0;
let waiting = null;       // the observer that waits for the next parts to be loaded

function setState(next) {
  state = next;
  bar.classList.toggle(PLAYING, next === 'playing');
  bar.classList.toggle(ACTIVE, next !== 'idle');
}

function parts() {
  return Array.from(content.children).filter(e => e.hasAttribute('data-message-id'));
}

function mark(element) {
  if (part && part !== element) part.classList.remove(READING);
  part = element;
  if (part) part.classList.add(READING);
}

// --- voices ---
function voices() {
  return synth.getVoices() || [];
}

function fillVoices() {
  const all = voices();
  const selected = prefs.voice;
  voiceSel.textContent = '';
  const auto = document.createElement('option');
  auto.value = '';
  auto.textContent = autoLabel;
  voiceSel.appendChild(auto);
  const matches = v => lang && v.lang.toLowerCase().replace('_', '-').split('-')[0] === lang.split('-')[0];
  const sorted = all.slice().sort((a, b) => (matches(b) - matches(a)) || a.name.localeCompare(b.name));
  for (const v of sorted) {
    const option = document.createElement('option');
    option.value = v.name;
    option.textContent = v.name + ' (' + v.lang + ')';
    voiceSel.appendChild(option);
  }
  voiceSel.value = selected;
  if (voiceSel.value !== selected) voiceSel.value = '';
}

function chosenVoice() {
  const all = voices();
  if (prefs.voice) {
    const named = all.find(v => v.name === prefs.voice);
    if (named) return named;
  }
  if (!lang) return null;
  const base = lang.split('-')[0];
  const same = all.filter(v => v.lang.toLowerCase().replace('_', '-').split('-')[0] === base);
  return same.find(v => v.lang.toLowerCase().replace('_', '-') === lang)
      || same.find(v => v.default) || same[0] || null;
}

// --- text ---
function sentences(text) {
  if (typeof Intl !== 'undefined' && Intl.Segmenter) {
    try {
      return Array.from(new Intl.Segmenter(lang || undefined, { granularity: 'sentence' }).segment(text), s => s.segment);
    } catch (e) { /* unknown language tag */ }
  }
  return text.match(/[^.!?。！？…]+[.!?。！？…]*["'”’»)\]]*\s*/g) || [text];
}

// Short chunks: some engines stop after about fifteen seconds of one utterance, and a pause or a change of
// the voice takes effect at the next chunk.
function toChunks(text) {
  const result = [];
  let buffer = '';
  const flush = () => { if (buffer.trim()) result.push(buffer.trim()); buffer = ''; };
  for (let sentence of sentences(text)) {
    while (sentence.length > CHUNK) {
      let cut = sentence.lastIndexOf(' ', CHUNK);
      if (cut < CHUNK / 2) cut = CHUNK;
      if (buffer) flush();
      result.push(sentence.slice(0, cut).trim());
      sentence = sentence.slice(cut);
    }
    if (buffer.length + sentence.length > CHUNK) flush();
    buffer += sentence;
  }
  flush();
  return result.filter(c => /[\p{L}\p{N}]/u.test(c));
}

// the text the reader sees: the translated speech or the hidden source of a click follow what is shown
function textOf(element) {
  return (element.innerText || '').replace(/\s+/g, ' ').trim();
}

// --- reading ---
function stopWaiting() {
  if (waiting) {
    waiting.observer.disconnect();
    clearTimeout(waiting.timer);
    waiting = null;
  }
}

function halt() {
  token++;
  stopWaiting();
  synth.cancel();
  mark(null);
  chunks = [];
  chunkIndex = 0;
  setState('idle');
}

function speakChunk(myToken) {
  if (myToken !== token || !bar.isConnected) return;
  if (chunkIndex >= chunks.length) {
    nextPart(myToken);
    return;
  }
  const utterance = new SpeechSynthesisUtterance(chunks[chunkIndex]);
  const voice = chosenVoice();
  if (voice) {
    utterance.voice = voice;
    utterance.lang = voice.lang;
  } else if (lang) {
    utterance.lang = lang;
  }
  utterance.rate = prefs.rate;
  utterance.pitch = prefs.pitch;
  utterance.onend = () => {
    if (myToken !== token || !bar.isConnected) return;
    chunkIndex++;
    speakChunk(myToken);
  };
  utterance.onerror = event => {
    if (myToken !== token) return;
    if (event.error === 'interrupted' || event.error === 'canceled') return;
    halt();
  };
  synth.speak(utterance);
}

function readPart(element, myToken) {
  mark(element);
  chunks = toChunks(textOf(element));
  chunkIndex = 0;
  element.scrollIntoView({ block: 'start', behavior: 'smooth' });
  speakChunk(myToken);
}

function nextPart(myToken) {
  if (!part || !part.isConnected) { halt(); return; }
  const next = element => {
    for (let e = element.nextElementSibling; e; e = e.nextElementSibling) {
      if (e.hasAttribute('data-message-id')) return e;
    }
    return null;
  };
  const following = next(part);
  if (following) {
    readPart(following, myToken);
    return;
  }
  // the end of what is loaded: reaching the bottom makes the reader load the next parts, wait for them
  const last = part;
  stopWaiting();
  const observer = new MutationObserver(() => {
    const more = next(last);
    if (more && myToken === token) {
      stopWaiting();
      readPart(more, myToken);
    }
  });
  observer.observe(content, { childList: true });
  waiting = { observer, timer: setTimeout(() => { if (myToken === token) halt(); }, LOAD_WAIT_MS) };
  last.scrollIntoView({ block: 'end', behavior: 'smooth' });
}

// where to start: the part with the selection, else the first one in view
function startPart() {
  const selection = window.getSelection();
  if (selection && selection.rangeCount && !selection.isCollapsed) {
    let node = selection.getRangeAt(0).startContainer;
    node = node.nodeType === 1 ? node : node.parentElement;
    const found = node && node.closest ? node.closest('[data-message-id]') : null;
    if (found && content.contains(found)) return found;
  }
  const top = scroller.getBoundingClientRect().top;
  return parts().find(e => e.getBoundingClientRect().bottom > top + 1) || parts()[0] || null;
}

function play() {
  if (state === 'paused') {
    synth.resume();
    setState('playing');
    return;
  }
  const first = startPart();
  if (!first) return;
  halt();
  setState('playing');
  readPart(first, token);
}

function pause() {
  if (state !== 'playing') return;
  synth.pause();
  setState('paused');
}

// --- controls ---
playBtn.addEventListener('click', play);
pauseBtn.addEventListener('click', pause);
stopBtn.addEventListener('click', halt);
gearBtn.addEventListener('click', () => panel.classList.toggle('tts-reader-open'));

rateIn.value = prefs.rate;
pitchIn.value = prefs.pitch;
rateIn.addEventListener('input', () => { prefs.rate = +rateIn.value; savePrefs(); });
pitchIn.addEventListener('input', () => { prefs.pitch = +pitchIn.value; savePrefs(); });
voiceSel.addEventListener('change', () => { prefs.voice = voiceSel.value; savePrefs(); });

fillVoices();
const onVoices = () => fillVoices();
synth.addEventListener('voiceschanged', onVoices);
const onHide = () => synth.cancel();
window.addEventListener('pagehide', onHide);

bar._ttsDispose = () => {
  synth.removeEventListener('voiceschanged', onVoices);
  window.removeEventListener('pagehide', onHide);
  halt();
  delete bar._ttsDispose;
};
