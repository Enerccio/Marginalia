// Step 02-extensions -> 03-providers: the inference providers (all pointing at the fake server).
// Order matters: the General Settings tab is filled first, then Per Type Settings, then OK. Switching back to
// General Settings after typing the key makes the dialog forget the key (see SCREENSHOTS.md, known quirks).
import {dialog, field, fill, idle, llmUrl, login, main, openTab, pick, press, sleep, snap} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await openTab(page, 'Inference Providers');

  async function addProvider({ name, url, key = 'demo-key', model, ctx = 32768, resp = 2048, shared = false, reasoning = null, refresh = false }) {
    await press(page, 'Add Inference Provider');
    const d = dialog(page);
    await fill(d, 'Name', name);
    if (shared) await field(d, 'Shared with all users').check();
    await fill(d, 'Max Context Size', String(ctx));
    await fill(d, 'Max Response Tokens', String(resp));
    if (reasoning) {
      await field(d, 'Enable Reasoning').check();
      await idle(page, 300);
      await pick(d, 'Reasoning Effort', reasoning);
    }
    await d.getByText('Per Type Settings', { exact: true }).click();
    await idle(page, 400);
    await fill(d, 'OpenAI URL (ends with /v1)', url);
    await fill(d, 'Api Key', key);
    if (refresh) {
      await press(d, 'Refresh');
      await sleep(800);
    }
    await pick(d, 'Model', model);
    await idle(page, 400);
    await press(d, 'OK');
  }

  await addProvider({ name: 'Local LLM', url: llmUrl, model: 'marginalia-demo-12b', shared: true, refresh: true });
  await addProvider({ name: 'Local Reasoner', url: llmUrl, model: 'marginalia-demo-reasoner', resp: 4096, reasoning: 'Medium', refresh: true });
  await addProvider({ name: 'Featherless.ai', url: 'https://api.featherless.ai/v1', model: 'meta-llama/Meta-Llama-3.1-70B-Instruct', ctx: 16384, resp: 1024 });
  await snap(page, '_prov-list');
});
