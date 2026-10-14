// Step 03-providers -> 04-protocols: protocols and the general settings of the administrator.
import {dialog, field, fill, login, main, openTab, pick, press, sleep, snap, snapDialog} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await openTab(page, 'Protocols');

  async function addProtocol({ name, temperature, topP, presence, frequency, shot }) {
    await press(page, 'Add Protocol');
    const d = dialog(page);
    await fill(d, 'Name', name);
    for (const [label, value] of [['Temperature', temperature], ['Top P', topP], ['Frequency Penalty', frequency], ['Presence Penalty', presence]]) {
      if (value == null) continue;
      await field(d, 'Enable ' + label).check();
      await fill(d, label, String(value));
    }
    if (shot) {
      await d.locator('[part="content"], vaadin-dialog-content').first().evaluate(() => {});
      await page.evaluate(() => {
        const o = document.querySelector('vaadin-dialog[opened]').shadowRoot.querySelector('vaadin-dialog-overlay');
        const c = o.shadowRoot.querySelector('[part="content"]');
        c.scrollTop = c.scrollHeight;
      });
      await sleep(300);
      await page.evaluate(() => document.activeElement && document.activeElement.blur());
      await snapDialog(page, '_protocol-dialog');
    }
    await press(d, 'OK');
  }

  await addProtocol({ name: 'Chat Completion', temperature: 0.9, topP: 0.95 });
  await addProtocol({ name: 'Creative Protocol', temperature: 1.1, topP: 0.92, presence: 0.2, shot: true });
  await snap(page, '_protocols-list');

  await openTab(page, 'Settings');
  await pick(page, 'Default Model', 'Local LLM');
  await pick(page, 'Default Protocol', 'Chat Completion');
  await pick(page, 'Backup Strategy', 'No Backups');
  await press(page, 'Save');
  await snap(page, '_settings');
});
