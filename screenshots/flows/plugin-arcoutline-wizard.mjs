// Arc Outline, the plot wizard (Develop the plot): the menu of the wand, a conversation with the options, the tools the model may use (it adds scenes to the
// planner), the question before it changes what exists, and the summary of the chat.
// Pictures: plugin-arcoutline-menu (three items), plugin-arcoutline-wizard, plugin-arcoutline-wizard-tools,
// plugin-arcoutline-wizard-summarize, plugin-arcoutline-wizard-summary.
// State: a COPY of 22-arcoutline (the outline has its arcs, the story its summaries), discarded. The fake model must run: every answer is queued.
import {
    bookTab,
    field,
    fill,
    goto,
    idle,
    llm,
    login,
    main,
    openBook,
    pickSelect,
    press,
    section,
    sleep,
    snap,
    snapDialog,
} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';

/** The check box of a group of tools ("Planner: read and change the scenes"): its label starts with the name of the group. */
const tool = (d, group) => d.locator('vaadin-checkbox').filter({ hasText: group + ':' });

/** Sends a message and waits for the Send button to come back (it is Stop while the model writes). */
async function say(d, message) {
    const input = d.locator('textarea[placeholder^="Talk about the plot"]');
    await input.fill(message);
    await input.dispatchEvent('change');
    await d.getByRole('button', { name: 'Send', exact: true }).click();
}

/** Waits until the answer that says `text` is in the chat and the Send button is back (it is Stop while the model writes). */
async function answered(page, d, text, ms = 60000) {
    await d.getByText(text, { exact: false }).first().waitFor({ timeout: ms });
    await d.getByRole('button', { name: 'Send', exact: true }).waitFor({ timeout: ms });
    await idle(page, 900);
}

/** The end of the chat (the list of messages scrolls on its own; the options on the left are left as they are). */
const chatEnd = page => page.evaluate(() => {
    const list = document.querySelector('.arc-outline-wizard-messages');
    if (list) list.scrollTop = list.scrollHeight;
});

await main(async ({ page }) => {
    await llm('/_reset', {});
    await login(page);
    await goto(page, '/');
    await openBook(page);
    await bookTab(page, 'Story');
    await widenSidebar(page, 700);
    await sideTab(page, 'Arcs');

    let d;
    await section(page, 'menu', async () => {
        // the wand is a menu with three items now
        await page.getByRole('button', { name: 'Generate an arc', exact: true }).click();
        await idle(page, 600);
        await page.mouse.move(1200, 500);
        await snap(page, 'plugin-arcoutline-menu', { clip: { x: 40, y: 185, width: 640, height: 240 } });
        await page.getByRole('menuitem', { name: 'Develop the plot' }).click();
        await idle(page, 1800);
    });

    await section(page, 'talk', async () => {
        d = page.locator('vaadin-dialog[opened]').last();
        await pickSelect(d, 'Explore directions');
        await llm('/_queue', {
            reasoning: 'The author wants directions, not a decision. The ferry landing ends with Mizuki knowing that Tomáš understands Japanese, and with the Dominion between them and home. Three ways: the secret comes out at once, it is kept and costs them something, or it is used.',
            reply: `Three directions from the ferry landing, each built on the same fact: **Mizuki knows Tomáš understands her, and he does not know she knows.**

1. **The fire.** She says it on the first quiet night, in Japanese, and he explains. The lie becomes a debt between them and the valley is only the setting.
2. **The toll house.** A clerk asks for names and a fee in Vrakht. Mizuki lets Tomáš interpret by tone, and watches whether he lies *to her* when the clerk is wrong.
3. **The first word.** Saellen teaches them Vrakht by pointing. The first word is a rude one, and it is Mizuki who learns it faster.

I would take the second: it gives you a scene with a stake and keeps the secret alive for a few more chapters.`,
        });
        await say(d, 'After the ferry landing: Mizuki knows he understands Japanese and has not said so. Where could the story go?');
        await answered(page, d, 'I would take the second');
        await chatEnd(page);
        await sleep(400);
        await snapDialog(page, 'plugin-arcoutline-wizard');
    });

    await section(page, 'tools', async () => {
        // the model may use the planner
        await tool(d, 'Planner').click();
        await idle(page, 600);
        await llm('/_queue', {
            reasoning: 'The author took the second direction. Look at the planner first, then add the toll house and the fire, in that order.',
            toolCalls: [
                { name: 'get-planned-scenes', arguments: {} },
                { name: 'add-planned-scene', arguments: {
                    title: 'The toll house', sceneSetting: 'The toll house on the east bank', povCharacter: 'Tomáš', presentCharacters: 'Tomáš, Mizuki, a clerk',
                    instructions: 'A clerk asks for their names and a fee in Vrakht. Mizuki lets Tomáš interpret by tone and watches whether he lies to her when the clerk is wrong.' } },
                { name: 'add-planned-scene', arguments: {
                    title: 'The fire', sceneSetting: 'A fire at the edge of the reeds, night', povCharacter: 'Mizuki', presentCharacters: 'Mizuki, Tomáš',
                    instructions: 'Mizuki says it, in Japanese: she has known since the river. He does not apologise; he explains.' } },
            ],
        });
        await llm('/_queue', { reply: 'I added two scenes to the planner, after the one that was there: **The toll house** (Tomáš, with the clerk) and **The fire** (Mizuki says that she knows). Nothing else changed.' });
        await say(d, 'Take the second direction. Put the toll house in the planner, and the fire after it.');
        await answered(page, d, 'I added two scenes');
        await chatEnd(page);
        // open the first call: what the model passed and what came back
        const call = d.getByText('add-planned-scene').first();
        if (await call.count()) await call.click().catch(() => {});
        await sleep(600);
        await chatEnd(page);
        await snapDialog(page, 'plugin-arcoutline-wizard-tools');
    });

    await section(page, 'summary', async () => {
        await d.locator('vaadin-button:has(vaadin-icon[icon*="compress"])').first().click({ force: true });
        await idle(page, 900);
        const ask = page.locator('vaadin-dialog[opened]').last();
        await ask.locator('textarea').first().fill('Keep the second direction (the toll house) and the fire; drop the first word.');
        await ask.locator('textarea').first().dispatchEvent('change');
        await snapDialog(page, 'plugin-arcoutline-wizard-summarize', 0);
        await llm('/_queue', {
            reply: `**Decided:** the story goes to the toll house first (Tomáš interprets by tone while Mizuki watches), then to the fire, where she says it. Both are in the planner.
**Rejected:** the first word of Vrakht as a scene of its own.
**Open:** whether the clerk is the same man at the end of the arc. Saellen's entry is unchanged.`,
        });
        await ask.getByRole('button', { name: 'Summarize', exact: true }).click();
        await answered(page, d, 'Decided:');
        await chatEnd(page);
        await sleep(400);
        await snapDialog(page, 'plugin-arcoutline-wizard-summary');
    });
});
