/*
 * The Markdown editor of the application (ui.widgets.MarkdownEditor): @uiw/react-md-editor with our own toolbar.
 *
 * - The toolbar leaves out what makes no sense for text that goes to a model: links, the image markup (the upload is the
 *   application's own), HTML comments, the help link and the full screen mode (the editor sits in a full screen dialog).
 * - The heading buttons have no shortcuts: Ctrl/Cmd+1..6 switch the tabs of the browser.
 * - Tab moves the focus instead of indenting, so the editor can be left by keyboard.
 * - The text reaches the server after a pause in typing and when the editor loses the focus, not with every key: every
 *   change of the value is a save of the entry.
 * - The preview is sanitized (rehype-sanitize): HTML in the text is shown as text, as the model gets it.
 * - The mode the user picks (edit / live / preview) goes to the server as the state "preview", so the page can keep it.
 */
import {type MouseEvent, type ReactElement, useEffect, useRef, useState} from 'react';
import MDEditor, {
    bold,
    checkedListCommand,
    code,
    codeBlock,
    codeEdit,
    codeLive,
    codePreview,
    divider,
    group,
    hr,
    italic,
    orderedListCommand,
    quote,
    strikethrough,
    table,
    title1,
    title2,
    title3,
    title4,
    title5,
    title6,
    unorderedListCommand
} from '@uiw/react-md-editor/nohighlight';
import rehypeSanitize from 'rehype-sanitize';
import {ReactAdapterElement, type RenderHooks} from 'Frontend/generated/flow/ReactAdapter';

// how long the typing has to pause before the text is sent
const SEND_DELAY_MS = 700;

const noShortcut = (command: any) => ({...command, shortcuts: undefined});

const toolbar = [
    bold, italic, strikethrough, hr,
    group([title1, title2, title3, title4, title5, title6].map(noShortcut), {
        name: 'title',
        groupName: 'title',
        buttonProps: {'aria-label': 'Insert title', title: 'Insert title'}
    }),
    divider, quote, code, codeBlock, table,
    divider, unorderedListCommand, orderedListCommand, checkedListCommand
];
const modes = [codeEdit, codeLive, codePreview];
const MODE_CLASS = /w-md-editor-show-(edit|live|preview)/;

// the color scheme of the page: Vaadin writes theme="dark" on <html> for the dark one
function pageColorMode(): 'light' | 'dark' {
    return (document.documentElement.getAttribute('theme') || '').split(' ').includes('dark') ? 'dark' : 'light';
}

function useColorMode(): 'light' | 'dark' {
    const [mode, setMode] = useState(pageColorMode());
    useEffect(() => {
        const observer = new MutationObserver(() => setMode(pageColorMode()));
        observer.observe(document.documentElement, {attributes: true, attributeFilter: ['theme']});
        return () => observer.disconnect();
    }, []);
    return mode;
}

class MarkdownEditorElement extends ReactAdapterElement {

    protected override render(hooks: RenderHooks): ReactElement | null {
        // the value the server has, and what is in the editor (ahead of it while the user types)
        const [value, setValue] = hooks.useState<string>('value');
        const [placeholder] = hooks.useState<string>('placeholder');
        const [readOnly] = hooks.useState<boolean>('readOnly');
        const [preview, setPreview] = hooks.useState<string>('preview');
        const [text, setText] = useState<string>(value ?? '');
        const sent = useRef<string>(value ?? '');
        const pending = useRef<string | null>(null);
        const timer = useRef<number | undefined>(undefined);
        const colorMode = useColorMode();
        const root = useRef<HTMLDivElement>(null);

        // the editor keeps the mode to itself: after a click on its toolbar the mode it shows now (its class) is sent
        const toolbarClicked = (event: MouseEvent) => {
            if (!(event.target as Element).closest?.('.w-md-editor-toolbar')) {
                return;
            }
            window.setTimeout(() => {
                const editor = root.current?.querySelector('.w-md-editor');
                const shown = editor ? MODE_CLASS.exec(editor.className) : null;
                if (shown && shown[1] !== preview) {
                    setPreview(shown[1]);
                }
            }, 0);
        };

        const flush = () => {
            window.clearTimeout(timer.current);
            timer.current = undefined;
            if (pending.current !== null) {
                sent.current = pending.current;
                pending.current = null;
                setValue(sent.current);
            }
        };

        // a value set by the server replaces the text, unless it is the echo of what was sent
        useEffect(() => {
            const incoming = value ?? '';
            if (incoming !== sent.current) {
                sent.current = incoming;
                pending.current = null;
                window.clearTimeout(timer.current);
                setText(incoming);
            }
        }, [value]);

        // what was typed last is not lost when the editor goes away (a closed tab)
        useEffect(() => () => flush(), []);

        const change = (next?: string) => {
            const nextText = next ?? '';
            setText(nextText);
            pending.current = nextText;
            window.clearTimeout(timer.current);
            timer.current = window.setTimeout(flush, SEND_DELAY_MS);
        };

        return (
            <div data-color-mode={colorMode} ref={root} onClickCapture={toolbarClicked}>
                <MDEditor
                    value={text}
                    onChange={change}
                    height="100%"
                    visibleDragbar={false}
                    preview={(preview as any) || 'live'}
                    commands={toolbar}
                    extraCommands={modes}
                    defaultTabEnable={true}
                    textareaProps={{
                        placeholder: placeholder ?? undefined,
                        readOnly: !!readOnly,
                        onBlur: flush
                    }}
                    previewOptions={{rehypePlugins: [[rehypeSanitize]]}}
                />
            </div>
        );
    }
}

customElements.define('marginalia-markdown-editor', MarkdownEditorElement);
