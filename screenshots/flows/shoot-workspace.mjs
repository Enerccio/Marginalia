// Screenshots of the workspace tabs (Books, Protocols, Inference Providers, Settings, Lorebooks, Resources, Trash,
// Language). Run on step 13-db-backup or later. Nothing stays changed (dialogs are cancelled, the language is reset).
import {
    CONTENT,
    dialog,
    fill,
    idle,
    login,
    main,
    openTab,
    pick,
    press,
    section,
    sleep,
    snap,
    snapDialog
} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);

  await section(page, 'workspace', async () => {
    await openTab(page, 'Books');
    await snap(page, 'workspace');
    await snap(page, 'books');
    await snap(page, 'dev-ui-map-workspace-not-annotated');
  });

  await section(page, 'protocols', async () => {
    await openTab(page, 'Protocols');
    await snap(page, 'protocols-list');
    await press(page, 'Add Protocol');
    await page.evaluate(() => {
      const o = document.querySelector('vaadin-dialog[opened]').shadowRoot.querySelector('vaadin-dialog-overlay');
      const c = o.shadowRoot.querySelector('[part="content"]');
      c.scrollTop = c.scrollHeight;
    });
    await sleep(300);
    await snapDialog(page, 'protocol-dialog');
    await press(dialog(page), 'Cancel');
  });

  await section(page, 'providers', async () => {
    await openTab(page, 'Inference Providers');
    await snap(page, 'inference-providers-list', { clip: CONTENT });
    await page.mouse.click(1850, 228);                         // pen of Local LLM
    await idle(page, 800);
    const d = dialog(page);
    await d.getByText('Per Type Settings', { exact: true }).click();
    await idle(page, 400);
    await press(d, 'Refresh');
    await sleep(800);
    await pick(d, 'Model', 'marginalia-demo-12b');        // Refresh empties the field (TODO.BUGS.md BUG-64)
    await sleep(300);
    await snap(page, 'inference-provider');
    await d.getByRole('button', { name: 'Test Connection', exact: false }).scrollIntoViewIfNeeded();
    await sleep(300);
    await snapDialog(page, 'inference-provider-2');
    await d.getByText('General Settings', { exact: true }).click();
    await idle(page, 400);
    await snapDialog(page, 'inference-provider-general');
    await press(d, 'Cancel');
  });

  await section(page, 'settings', async () => {
    await openTab(page, 'Settings');
    await snap(page, 'settings-defaults', { clip: CONTENT });
    await page.getByText('Templates', { exact: true }).first().click();
    await idle(page, 800);
    await snap(page, 'settings-templates');
    // the prompts of the agents and the message to a URL are further down (for a user who may use agents)
    await page.getByLabel('Compaction prompt of agents', { exact: true }).scrollIntoViewIfNeeded();
    await page.getByLabel('Send a test message', { exact: true }).scrollIntoViewIfNeeded().catch(() => {});
    await idle(page, 600);
    await snap(page, 'settings-templates-agents');
    await page.getByLabel('Master Template', { exact: true }).scrollIntoViewIfNeeded();
    await page.getByText('Tags', { exact: true }).first().click();
    await idle(page, 800);
    await snap(page, 'settings-tags', { clip: CONTENT });
    // rename f/m to an existing tag: Marginalia asks whether to merge
    await page.mouse.click(1788, 227);
    await idle(page, 600);
    await fill(dialog(page), 'Name', 'genderbend');
    await press(dialog(page), 'Yes');
    await idle(page, 800);
    await snapDialog(page, 'tag-merge');
    await press(dialog(page), 'Cancel');
    await idle(page, 500);
    if (await page.locator('vaadin-dialog[opened], vaadin-confirm-dialog[opened]').count()) await press(dialog(page), 'Cancel');
  });

  await section(page, 'extension-settings', async () => {
    await openTab(page, 'Settings');
    await page.getByText('Extension Settings', { exact: true }).first().click();
    await idle(page, 800);
    // the accordion keeps one panel open and its order follows the load order of the extensions: open each by its name
    await page.getByText('Reviewer Settings', { exact: true }).click();
    await sleep(800);
    await snap(page, 'plugin-reviewer-settings');
    await page.getByText('SideQuery Settings', { exact: true }).click();
    await sleep(800);
    await snap(page, 'plugin-sidequery-settings');
    await page.getByText('Lore Generator', { exact: true }).last().click();
    await sleep(800);
    await snap(page, 'settings-extensions');
    await snap(page, 'plugin-loregenerator-settings');
  });

  await section(page, 'lorebook', async () => {
    await openTab(page, 'Lorebooks');
    await pick(page, 'Lorebook', 'Harry Potter - Half-Blood Prince');
    await sleep(1000);
    // expand the entry of Draconia (the chevron of its row)
    const cell = page.locator('vaadin-grid-cell-content').filter({ hasText: /^$/ }).first();
    const row = page.locator('vaadin-text-field input').filter({ hasText: '' });
    const nameInput = page.locator('input').filter({ has: page.locator('xpath=.') });
    const y = await page.evaluate(() => {
      const inp = [...document.querySelectorAll('vaadin-text-field')].find(f => f.value === 'Draconia Malfoy');
      return inp ? inp.getBoundingClientRect().y + inp.getBoundingClientRect().height / 2 : null;
    });
    if (y == null) throw new Error('entry row not found');
    await page.mouse.click(1757, y);
    await sleep(1000);
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await snap(page, 'lorebook');
    // the entry with its details alone
    await page.evaluate(() => {
      const f = [...document.querySelectorAll('vaadin-text-field')].find(x => x.value === 'Draconia Malfoy');
      f.scrollIntoView({ block: 'start' });
    });
    await sleep(500);
    const top = await page.evaluate(() => {
      const f = [...document.querySelectorAll('vaadin-text-field')].find(x => x.value === 'Draconia Malfoy');
      return f.getBoundingClientRect().y;
    });
    await snap(page, 'lorebook-entry', { clip: { x: 292, y: Math.max(0, top - 18), width: 1599, height: 591 } });
  });

  await section(page, 'resources', async () => {
    await openTab(page, 'Resources');
    await snap(page, 'resources-tab');
    await page.mouse.click(1767, 195);                        // replace content (upload icon) of the first row
    await idle(page, 700);
    await snap(page, 'resources-replace');
    await press(dialog(page), 'Close');
  });

  await section(page, 'trash', async () => {
    await openTab(page, 'Trash');
    await snap(page, 'trash-tab');
    // only the entry, not its deleted lorebook: refused
    await page.mouse.click(338, 457);
    await press(page, 'Restore Selected', { exact: false });
    await idle(page, 600);
    await press(dialog(page), 'Yes');
    await idle(page, 800);
    await snap(page, 'trash-restore-blocked');
    await press(dialog(page), 'Close').catch(async () => press(dialog(page), 'OK'));
    // delete permanently asks first
    await press(page, 'Delete Permanently', { exact: false });
    await idle(page, 600);
    await snapDialog(page, 'trash-delete-permanently');
    await press(dialog(page), 'No').catch(async () => press(dialog(page), 'Cancel'));
  });
});
