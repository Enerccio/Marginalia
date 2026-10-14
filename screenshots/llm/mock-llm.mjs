// Fake OpenAI compatible inference server for the screenshot environment (no dependencies, Node 20+).
//
//   node llm/mock-llm.mjs [port]          default port 8098, base url for the AI: http://localhost:8098/v1
//
// Answers GET /v1/models and POST /v1/chat/completions (streaming SSE and plain JSON).
// What it answers comes from llm/rules.mjs (first matching rule wins) unless a one-shot answer was queued:
//
//   POST /_queue   {"reply":"text","reasoning":"...","status":429,"message":"...","delayMs":0}   (one-shot, FIFO)
//   POST /_mode    {"mode":"slow"|null}      rules can look at ctx.mode (e.g. to produce an error)
//   GET  /_log                              the last requests (messages flattened), newest last
//   POST /_reset                            clears queue, mode and log
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const port = Number(process.argv[2] || process.env.SHOT_LLM_PORT || 8098);
const logFile = path.join(here, '..', '.run', 'llm-requests.jsonl');
fs.mkdirSync(path.dirname(logFile), { recursive: true });

const MODELS = ['marginalia-demo-12b', 'marginalia-demo-reasoner', 'marginalia-demo-small'];
let queue = [];
let mode = null;
let log = [];

const flatten = c => typeof c === 'string' ? c
  : Array.isArray(c) ? c.map(p => p.text ?? '').join('\n') : '';
const tokens = s => Math.max(1, Math.round(s.length / 4));
const sleep = ms => new Promise(r => setTimeout(r, ms));

async function loadRules() {
  // re-import on every request so the rules can be edited while the server runs
  return (await import('./rules.mjs?' + Date.now())).default;
}

function readBody(req) {
  return new Promise(resolve => {
    const chunks = [];
    req.on('data', c => chunks.push(c));
    req.on('end', () => resolve(Buffer.concat(chunks).toString('utf8')));
  });
}

function sendJson(res, status, obj) {
  const data = JSON.stringify(obj);
  res.writeHead(status, { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(data) });
  res.end(data);
}

function splitForStream(text) {
  // word sized pieces keep the stream lively without hundreds of events
  return text.match(/\S+\s*|\s+/g) ?? [];
}

async function chat(req, res, body) {
  const messages = body.messages ?? [];
  const flat = messages.map(m => ({ role: m.role, content: flatten(m.content) }));
  const ctx = {
    body, messages: flat, mode,
    system: flat.filter(m => m.role === 'system').map(m => m.content).join('\n'),
    last: flat.length ? flat[flat.length - 1].content : '',
    text: flat.map(m => m.content).join('\n'),
    model: body.model, stream: !!body.stream,
    wantsJson: !!body.response_format && body.response_format.type !== 'text',
    hasTools: Array.isArray(body.tools) && body.tools.length > 0,
  };
  ctx.isSummary = /summar/i.test(ctx.system.slice(0, 600)) && !ctx.last.includes('Write the next section');
  log.push({ at: new Date().toISOString(), model: body.model, stream: ctx.stream, wantsJson: ctx.wantsJson, messages: flat });
  log = log.slice(-60);
  fs.appendFileSync(logFile, JSON.stringify(log[log.length - 1]) + '\n');

  let answer = queue.shift();
  if (!answer) {
    for (const rule of await loadRules()) {
      if (rule.when(ctx)) {
        answer = typeof rule.reply === 'function' ? await rule.reply(ctx) : rule;
        if (typeof answer === 'string') answer = { reply: answer };
        if (rule.reasoning && !answer.reasoning) answer = { ...answer, reasoning: rule.reasoning };
        if (rule.delayMs && !answer.delayMs) answer = { ...answer, delayMs: rule.delayMs };
        answer.rule = rule.name;
        break;
      }
    }
  }
  answer ??= { reply: 'The mock server has no rule for this request.', rule: '(default)' };
  console.log(`${new Date().toISOString()} chat ${body.model} stream=${ctx.stream} rule=${answer.rule ?? '(queued)'}`);

  if (answer.delayMs) await sleep(answer.delayMs);
  if (answer.status) {
    return sendJson(res, answer.status, { error: { message: answer.message ?? 'error', type: 'server_error', code: null, param: null } });
  }

  const reply = answer.reply ?? '';
  const reasoning = answer.reasoning ?? '';
  const promptTokens = tokens(ctx.text);
  const completionTokens = tokens(reply + reasoning);
  const id = 'chatcmpl-demo-' + Date.now();
  const model = body.model ?? MODELS[0];
  const created = Math.floor(Date.now() / 1000);

  if (!ctx.stream) {
    const message = { role: 'assistant', content: reply, refusal: null };
    if (reasoning) message.reasoning_content = reasoning;
    return sendJson(res, 200, {
      id, object: 'chat.completion', created, model,
      choices: [{ index: 0, message, finish_reason: 'stop', logprobs: null }],
      usage: { prompt_tokens: promptTokens, completion_tokens: completionTokens, total_tokens: promptTokens + completionTokens },
    });
  }

  res.writeHead(200, { 'Content-Type': 'text/event-stream', 'Cache-Control': 'no-cache' });
  const event = (delta, finish = null) => res.write('data: ' + JSON.stringify({
    id, object: 'chat.completion.chunk', created, model,
    choices: [{ index: 0, delta, finish_reason: finish }],
  }) + '\n\n');
  const pause = answer.chunkDelayMs ?? 12;
  let first = true;
  for (const piece of splitForStream(reasoning)) {
    event(first ? { role: 'assistant', reasoning_content: piece } : { reasoning_content: piece });
    first = false; await sleep(pause);
  }
  for (const piece of splitForStream(reply)) {
    event(first ? { role: 'assistant', content: piece } : { content: piece });
    first = false; await sleep(pause);
  }
  event({}, 'stop');
  if (body.stream_options?.include_usage !== false) {
    res.write('data: ' + JSON.stringify({
      id, object: 'chat.completion.chunk', created, model, choices: [],
      usage: { prompt_tokens: promptTokens, completion_tokens: completionTokens, total_tokens: promptTokens + completionTokens },
    }) + '\n\n');
  }
  res.write('data: [DONE]\n\n');
  res.end();
}

http.createServer(async (req, res) => {
  try {
    const url = new URL(req.url, 'http://x');
    const p = url.pathname.replace(/^\/v1/, '');
    const raw = req.method === 'POST' ? await readBody(req) : '';
    const body = raw ? JSON.parse(raw) : {};
    if (req.method === 'GET' && p === '/models') {
      return sendJson(res, 200, { object: 'list', data: MODELS.map(id => ({ id, object: 'model', created: 0, owned_by: 'demo' })) });
    }
    if (req.method === 'POST' && p === '/chat/completions') return await chat(req, res, body);
    if (req.method === 'POST' && p === '/_queue') { queue.push(body); return sendJson(res, 200, { queued: queue.length }); }
    if (req.method === 'POST' && p === '/_mode') { mode = body.mode ?? null; return sendJson(res, 200, { mode }); }
    if (req.method === 'POST' && p === '/_reset') { queue = []; mode = null; log = []; return sendJson(res, 200, {}); }
    if (req.method === 'GET' && p === '/_log') return sendJson(res, 200, log);
    sendJson(res, 404, { error: { message: 'unsupported ' + req.method + ' ' + p } });
  } catch (e) {
    console.error(e);
    if (!res.headersSent) sendJson(res, 500, { error: { message: String(e) } }); else res.end();
  }
}).listen(port, () => console.log(`mock llm on http://localhost:${port}/v1`));
