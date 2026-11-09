// Story Notes: installs the plugin, writes four pages by hand (links between them, one hidden from assistants), lets the plot wizard
// read a page and add to it with the Notes tools, then shows the tab and the dialog of a page.
// Pictures: plugin-storynotes-tab, plugin-storynotes-page, plugin-storynotes-preview, plugin-storynotes-wizard.
// State: a COPY of 23-betareaders (the book has its story, the outline its arcs), discarded. The fake model must run: the answers of the wizard are queued.
import fs from 'node:fs';
import path from 'node:path';
import {
    bookTab,
    DESKTOP,
    fill,
    goto,
    idle,
    llm,
    login,
    main,
    openBook,
    press,
    repo,
    section,
    sleep,
    snap,
    snapDialog,
} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';

/** The pages: the Notes tab lists them by title; "Mizuki's secret" is the one the wizard adds to. */
const pages = [
    {
        title: "Mizuki's secret",
        tags: 'plot, secret',
        text: `Mizuki has known since the river that Tomáš understands Japanese. She has not told him.

**Why she waits:** she wants to see whether he lies to her when it matters, with the clerk at [[The toll house]].

**Payoff:** she says it at the fire, in Japanese. What each of them can follow is in [[Language barrier]].`,
    },
    {
        title: 'The toll house',
        tags: 'plot, act 2',
        text: `A clerk asks for their names and a fee in Vrakht. Tomáš interprets by tone; Mizuki watches whether he lies to her when the clerk is wrong.

This is the test of [[Mizuki's secret]]. Saellen is not there: they are on their own.`,
    },
    {
        title: 'Language barrier',
        tags: 'world, rules',
        text: `Nobody in the valley speaks Japanese. Vrakht is spoken by Saellen and the clerks of the toll houses.

- Tomáš picks up words by tone and repetition.
- Mizuki learns faster, and says less.
- The archway does not translate anything.`,
    },
    {
        title: 'Ending ideas',
        tags: 'ideas',
        hidden: true,
        text: `Only for me, the assistants must not see this.

A: they walk back through the archway and Mizuki stays. B: the valley closes behind them and it was never about the language.`,
    },
];

/** Installs the plugin through Admin -> Extensions unless it is there. */
async function install(page) {
    await page.getByRole('button', { name: 'Admin' }).click();
    await idle(page, 600);
    await page.getByText('Extensions', { exact: true }).first().click();
    await idle(page, 800);
    if (await page.locator('vaadin-grid-cell-content', { hasText: 'story-notes' }).count()) {
        console.log('  already installed story-notes');
        return;
    }
    const jar = path.join(repo, 'marginalia/plugins/story-notes/target/story-notes-1.7.0.jar');
    if (!fs.existsSync(jar)) throw new Error('not built: ' + jar);
    await page.locator('vaadin-upload input[type=file]').setInputFiles(jar);
    await idle(page, 4000);
    await page.getByRole('button', { name: 'Refresh' }).click();
    await idle(page, 1500);
    console.log('  installed story-notes');
}

/** Adds a page with the Add button of the Notes tab. */
async function addPage(page, note) {
    await page.getByRole('button', { name: 'Add', exact: true }).first().click();
    await idle(page, 700);
    const d = page.locator('vaadin-dialog[opened]').last();
    await fill(d, 'Title', note.title);
    await fill(d, 'Tags', note.tags);
    await d.locator('textarea').first().fill(note.text);
    await d.locator('textarea').first().dispatchEvent('change');
    if (note.hidden) await d.locator('vaadin-checkbox').filter({ hasText: 'Visible to assistants' }).click();
    await sleep(700);
    await press(d, 'Save');
    await idle(page, 900);
}

await main(async ({ page }) => {
    await llm('/_reset', {});
    await login(page);
    await goto(page, '/');
    await section(page, 'install', async () => {
        await install(page);
        // the new tab is in the story editors that open from now on
        await goto(page, '/');
    });
    await openBook(page);
    await bookTab(page, 'Story');
    await widenSidebar(page, 700);

    await section(page, 'pages', async () => {
        await sideTab(page, 'Notes');
        for (const note of pages) await addPage(page, note);
    });

    let wizard;
    await section(page, 'wizard', async () => {
        // the plot wizard reads a page and adds a question to it; a taller window shows all the options on the left
        await page.setViewportSize({ width: DESKTOP.width, height: 1300 });
        await sideTab(page, 'Arcs');
        await page.getByRole('button', { name: 'Generate an arc', exact: true }).click();
        await idle(page, 600);
        await page.getByRole('menuitem', { name: 'Develop the plot' }).click();
        await idle(page, 1800);
        wizard = page.locator('vaadin-dialog[opened]').last();
        await wizard.locator('vaadin-checkbox').filter({ hasText: 'Notes:' }).click();
        await idle(page, 600);
        await llm('/_queue', {
            reasoning: 'The author asks what is open in the secret. Read the page, then keep the open question in it.',
            toolCalls: [
                { name: 'get-note', arguments: { note: "Mizuki's secret" } },
                { name: 'append-to-note', arguments: {
                    note: "Mizuki's secret",
                    text: '**Open question:** does she tell him before the toll house or after it? If before, the clerk scene is played with both of them knowing.' } },
            ],
        });
        await llm('/_queue', { reply: `I read **Mizuki's secret** and added the open question to the end of it: whether she tells him *before* the toll house or *after* it. Nothing else in the page changed, and your private page was not touched (I cannot see it).` });
        const input = wizard.locator('textarea[placeholder^="Talk about the plot"]');
        await input.fill("What is still open in Mizuki's secret? Keep it in the note.");
        await input.dispatchEvent('change');
        await wizard.getByRole('button', { name: 'Send', exact: true }).click();
        await wizard.getByText('I read', { exact: false }).first().waitFor({ timeout: 60000 });
        await wizard.getByRole('button', { name: 'Send', exact: true }).waitFor({ timeout: 60000 });
        await idle(page, 900);
        await page.evaluate(() => {
            const list = document.querySelector('.arc-outline-wizard-messages');
            if (list) list.scrollTop = list.scrollHeight;
        });
        await sleep(400);
        await snapDialog(page, 'plugin-storynotes-wizard');
        await wizard.getByRole('button', { name: 'Exit', exact: true }).click();
        await idle(page, 800);
        await page.setViewportSize(DESKTOP);
        await idle(page, 600);
    });

    await section(page, 'tab', async () => {
        await sideTab(page, 'Notes');
        await page.mouse.move(1500, 700);
        await snap(page, 'plugin-storynotes-tab');
    });

    await section(page, 'page', async () => {
        // the page the wizard added to: links to the pages it names and the page that links back (a taller window shows them under the text)
        await page.setViewportSize({ width: DESKTOP.width, height: 1100 });
        await idle(page, 600);
        await page.getByText("Mizuki's secret", { exact: true }).first().click();
        await idle(page, 900);
        const d = page.locator('vaadin-dialog[opened]').last();
        await d.locator('textarea').first().waitFor();
        await sleep(900);
        await snapDialog(page, 'plugin-storynotes-page');
        await d.getByRole('tab', { name: 'Preview', exact: true }).click();
        await idle(page, 900);
        await snapDialog(page, 'plugin-storynotes-preview');
        // a link of the preview opens the note it names, on top of this dialog
        const before = await page.locator('vaadin-dialog[opened]').count();
        await d.locator('vaadin-markdown a', { hasText: 'The toll house' }).first().click();
        await idle(page, 1000);
        if (await page.locator('vaadin-dialog[opened]').count() !== before + 1) throw new Error('the link of the preview did not open the note');
        const linked = page.locator('vaadin-dialog[opened]').last();
        const opened = await linked.locator('input').first().inputValue();
        if (opened !== 'The toll house') throw new Error('the link opened ' + JSON.stringify(opened));
        console.log('  the link of the preview opened', opened);
        await press(linked, 'Cancel');
        await idle(page, 600);
        await press(d, 'Cancel');
        await idle(page, 600);
        await page.setViewportSize(DESKTOP);
        await idle(page, 600);
    });
});
