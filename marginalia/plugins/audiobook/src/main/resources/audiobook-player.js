// The audiobook player of the viewer. The body of a function; the arguments of executeJs are
//   $0 bar (the element of the controls)  $1 scroller (the scrolling panel)  $2 content (its children are the parts)
//   $3 play button  $4 pause button  $5 stop button  $6 speed input  $7 status element
//   $8 JSON {id of a part: address of its audio}  $9 title of the book  $10 text "no audio"  $11 text "could not be loaded"
//   $12 the button that offers the voice of the browser at a part without audio
// Everything happens in the browser: the audio of a part is fetched into memory (a blob), the next one while this one plays; the speed stays in
// the browser's localStorage.
const bar = $0, scroller = $1, content = $2;
const playBtn = $3, pauseBtn = $4, stopBtn = $5, speedIn = $6, statusEl = $7;
const urls = JSON.parse($8 || '{}');
const bookTitle = $9 || '';
const noAudioText = $10, loadErrorText = $11;
const offerBtn = $12;

if (bar._abDispose) {
  bar._abDispose();
}

const STORAGE_KEY = 'marginalia.audiobook';
const HEARD = 'audiobook-heard';
const PLAYING = 'audiobook-playing';
const ACTIVE = 'audiobook-active';
const NOTICE = 'audiobook-notice';
const OFFER = 'audiobook-offer';
const LOAD_WAIT_MS = 4000;

let prefs = { rate: 1 };
try {
  const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
  if (isFinite(saved.rate)) prefs.rate = Math.min(2, Math.max(0.75, +saved.rate));
} catch (e) { /* storage blocked: the defaults */ }
function savePrefs() {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs)); } catch (e) { /* blocked */ }
}

const audio = document.getElementById('audiobook-audio') || Object.assign(document.createElement('audio'), { id: 'audiobook-audio' });
audio.preload = 'auto';
if (!audio.isConnected) document.body.appendChild(audio);

let state = 'idle';      // idle | playing | paused
let token = 0;           // a callback of something that was stopped sees another token and does nothing
let part = null;         // the element of the part that is heard
let waiting = null;      // the observer that waits for the next parts to be loaded
const blobs = new Map(); // id -> promise of the address of the blob
// A part without audio: the read-aloud of the browser (the extension tts-reader, when it is there) can read it. It is offered, never started
// unasked; once the listener accepted it in a run, the later parts without audio in that run are read the same way.
let offer = null;        // {element, reader} while the offer is shown
let reader = null;       // the reader that reads a part for us now
let fallbackOk = false;

function setState(next) {
  state = next;
  bar.classList.toggle(PLAYING, next === 'playing');
  bar.classList.toggle(ACTIVE, next !== 'idle');
  if ('mediaSession' in navigator) navigator.mediaSession.playbackState = next === 'playing' ? 'playing' : next === 'paused' ? 'paused' : 'none';
}

function notice(text) {
  statusEl.textContent = text || '';
  bar.classList.toggle(NOTICE, !!text);
}

function parts() {
  return Array.from(content.children).filter(e => e.hasAttribute('data-message-id'));
}
const idOf = element => element.getAttribute('data-message-id');

function mark(element) {
  if (part && part !== element) part.classList.remove(HEARD);
  part = element;
  if (part) part.classList.add(HEARD);
}

// --- the audio of a part ---
function load(id) {
  if (!urls[id]) return null;
  if (!blobs.has(id)) {
    const promise = fetch(urls[id], { credentials: 'same-origin' })
      .then(r => { if (!r.ok) throw new Error('HTTP ' + r.status); return r.blob(); })
      .then(blob => { if (!blob.size) throw new Error('empty'); return URL.createObjectURL(blob); });
    promise.catch(() => blobs.delete(id));
    blobs.set(id, promise);
  }
  return blobs.get(id);
}

// only the part that is heard and the next one are kept in memory
function keepOnly(...ids) {
  for (const [id, promise] of Array.from(blobs.entries())) {
    if (!ids.includes(id)) {
      blobs.delete(id);
      promise.then(url => URL.revokeObjectURL(url)).catch(() => {});
    }
  }
}

function siblingPart(element, step) {
  for (let e = step > 0 ? element.nextElementSibling : element.previousElementSibling; e;
       e = step > 0 ? e.nextElementSibling : e.previousElementSibling) {
    if (e.hasAttribute('data-message-id')) return e;
  }
  return null;
}

// --- playing ---
function stopWaiting() {
  if (waiting) {
    waiting.observer.disconnect();
    clearTimeout(waiting.timer);
    waiting = null;
  }
}

function stopFallback() {
  offer = null;
  bar.classList.remove(OFFER);
  if (reader) {
    const active = reader;
    reader = null;
    active.stop();
  }
}

function halt(message) {
  token++;
  stopWaiting();
  stopFallback();
  audio.pause();
  audio.removeAttribute('src');
  audio.load();
  keepOnly();
  mark(null);
  setState('idle');
  notice(message);
}

function playPart(element, myToken) {
  const id = idOf(element);
  const loading = load(id);
  if (!loading) {
    // a part without audio: what is heard would skip a part of the story, so it stops here and says why
    mark(element);
    halt(noAudioText);
    const detail = { messageId: id, reader: null };
    bar.dispatchEvent(new CustomEvent('marginalia-audio-gap', { bubbles: true, detail }));
    if (detail.reader) {
      if (fallbackOk) startFallback(element, detail.reader);
      else { offer = { element, reader: detail.reader }; bar.classList.add(OFFER); }
    }
    return;
  }
  notice('');
  mark(element);
  element.scrollIntoView({ block: 'start', behavior: 'smooth' });
  const following = siblingPart(element, 1);
  keepOnly(id, following ? idOf(following) : null);
  loading.then(src => {
    if (myToken !== token) return;
    audio.src = src;
    audio.playbackRate = prefs.rate;
    updateSession(element);
    return audio.play();
  }).then(() => {
    if (myToken !== token) return;
    // the next part is fetched while this one plays
    if (following && urls[idOf(following)]) load(idOf(following));
  }).catch(() => {
    if (myToken === token) halt(loadErrorText);
  });
}

// the browser reads the parts without audio and hands back at the first one that has some
function startFallback(element, active) {
  fallbackOk = true;
  offer = null;
  bar.classList.remove(OFFER);
  notice('');
  reader = active;
  active.readFrom(element, e => !!urls[idOf(e)], resume => {
    reader = null;
    token++;
    setState('playing');
    playPart(resume, token);
  });
}

function nextPart(myToken) {
  if (!part || !part.isConnected) { halt(); return; }
  const following = siblingPart(part, 1);
  if (following) {
    playPart(following, myToken);
    return;
  }
  // the end of what is loaded: reaching the bottom makes the reader load the next parts, wait for them
  const last = part;
  stopWaiting();
  const observer = new MutationObserver(() => {
    const more = siblingPart(last, 1);
    if (more && myToken === token) {
      stopWaiting();
      playPart(more, myToken);
    }
  });
  observer.observe(content, { childList: true });
  waiting = { observer, timer: setTimeout(() => { if (myToken === token) halt(); }, LOAD_WAIT_MS) };
  last.scrollIntoView({ block: 'end', behavior: 'smooth' });
}

audio.onended = () => {
  if (state === 'playing' && bar.isConnected) nextPart(token);
};

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
    audio.play();
    setState('playing');
    return;
  }
  const first = startPart();
  if (!first) return;
  fallbackOk = false;
  halt();
  setState('playing');
  playPart(first, token);
}

function pause() {
  if (state !== 'playing') return;
  audio.pause();
  setState('paused');
}

function skip(step) {
  if (!part) return;
  const target = siblingPart(part, step);
  if (!target) return;
  token++;
  stopWaiting();
  setState('playing');
  playPart(target, token);
}

// --- the media keys and the lock screen of the device ---
function updateSession(element) {
  if (!('mediaSession' in navigator) || typeof MediaMetadata !== 'function') return;
  const index = parts().indexOf(element) + 1;
  navigator.mediaSession.metadata = new MediaMetadata({ title: bookTitle, artist: index > 0 ? '#' + index : '' });
}
if ('mediaSession' in navigator) {
  const handlers = { play, pause, stop: () => halt(), nexttrack: () => skip(1), previoustrack: () => skip(-1) };
  for (const [action, handler] of Object.entries(handlers)) {
    try { navigator.mediaSession.setActionHandler(action, handler); } catch (e) { /* not supported */ }
  }
}

// --- controls ---
playBtn.addEventListener('click', play);
pauseBtn.addEventListener('click', pause);
stopBtn.addEventListener('click', () => halt());
offerBtn.addEventListener('click', () => { if (offer) startFallback(offer.element, offer.reader); });
speedIn.value = prefs.rate;
speedIn.addEventListener('input', () => { prefs.rate = +speedIn.value; savePrefs(); audio.playbackRate = prefs.rate; });
const onHide = () => audio.pause();
window.addEventListener('pagehide', onHide);

bar._abDispose = () => {
  window.removeEventListener('pagehide', onHide);
  halt();
  if ('mediaSession' in navigator) {
    for (const action of ['play', 'pause', 'stop', 'nexttrack', 'previoustrack']) {
      try { navigator.mediaSession.setActionHandler(action, null); } catch (e) { /* not supported */ }
    }
  }
  delete bar._abDispose;
};
