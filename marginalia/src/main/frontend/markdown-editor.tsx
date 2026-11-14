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
 * - The tooltips of the toolbar come from the server (the state "labels", by command name) in the language of the page; the
 *   keyboard shortcut of a command is kept after its text.
 * - Ctrl+click (Cmd+click) in the text or in the preview sends the clicked text and the position in it to the server
 *   (linkClicked, with Shift for a new tab), which finds what it names there: the links between lorebook entries. The
 *   typed text goes with it, so the server has it before it looks.
 * - Images (when the server allows them, the state "images"): the image button, pasting and dropping an image send it to the
 *   server (uploadImage), which stores it and answers the Markdown put in at the cursor. An image whose address is a
 *   resource, ![caption](resource:<uuid>), is shown in the preview from the address the server gives (the state "imageUrls").
 */
import {type MouseEvent, type ReactElement, useEffect, useMemo, useRef, useState} from 'react';
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
import rehypeSanitize, {defaultSchema} from 'rehype-sanitize';
import {ReactAdapterElement, type RenderHooks} from 'Frontend/generated/flow/ReactAdapter';

// how long the typing has to pause before the text is sent
const SEND_DELAY_MS = 700;

const noShortcut = (command: any) => ({...command, shortcuts: undefined});

type Labels = Record<string, string> | null | undefined;

// the text of the server for the command, with the shortcut the editor's own tooltip names: "Bold (ctrl + b)"
function localized(command: any, labels: Labels) {
    const text = labels?.[command.name];
    if (!text) {
        return command;
    }
    const own: string = command.buttonProps?.title ?? '';
    const shortcut = command.shortcuts ? /\([^)]*\)\s*$/.exec(own) : null;
    const title = shortcut ? `${text} ${shortcut[0].trim()}` : text;
    return {...command, buttonProps: {...command.buttonProps, 'aria-label': title, title}};
}

// the sanitized preview, which also lets through the images of resources (their address is put in by urlTransform)
const SANITIZE_SCHEMA = {
    ...defaultSchema,
    protocols: {...defaultSchema.protocols, src: [...(defaultSchema.protocols?.src ?? ['http', 'https']), 'resource']}
};

const IMAGE_TYPES = 'image/png,image/jpeg,image/gif,image/webp';

const IMAGE_ICON = (
    <svg width="12" height="12" viewBox="0 0 20 20" aria-hidden="true">
        <path fill="currentColor"
              d="M15 9c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm4-7H1c-.55 0-1 .45-1 1v14c0 .55.45 1 1 1h18c.55 0 1-.45 1-1V3c0-.55-.45-1-1-1zm-1 13l-6-5-2 2-4-5-4 8V4h16v11z"/>
    </svg>
);

function imageCommand(labels: Labels, pick: () => void) {
    const title = labels?.image ?? 'Add image';
    return {
        name: 'image',
        keyCommand: 'image',
        buttonProps: {'aria-label': title, title},
        icon: IMAGE_ICON,
        execute: () => pick()
    };
}

function toolbarOf(labels: Labels, pickImage: (() => void) | null) {
    const image = pickImage ? [divider, imageCommand(labels, pickImage)] : [];
    return [
        localized(bold, labels), localized(italic, labels), localized(strikethrough, labels), localized(hr, labels),
        group([title1, title2, title3, title4, title5, title6].map(noShortcut).map(command => localized(command, labels)), {
            name: 'title',
            groupName: 'title',
            buttonProps: {
                'aria-label': labels?.title ?? 'Insert title',
                title: labels?.title ?? 'Insert title'
            }
        }),
        divider, localized(quote, labels), localized(code, labels), localized(codeBlock, labels), localized(table, labels),
        divider, localized(unorderedListCommand, labels), localized(orderedListCommand, labels),
        localized(checkedListCommand, labels),
        ...image
    ];
}

function modesOf(labels: Labels) {
    return [codeEdit, codeLive, codePreview].map(command => localized(command, labels));
}
const MODE_CLASS = /w-md-editor-show-(edit|live|preview)/;

// the color scheme of the page: Vaadin writes theme="dark" on <html> for the dark one
function pageColorMode(): 'light' | 'dark' {
    return (document.documentElement.getAttribute('theme') || '').split(' ').includes('dark') ? 'dark' : 'light';
}

// the text node under the mouse in the preview and the offset of the click in it
function textAtPoint(x: number, y: number): { text: string, offset: number } | null {
    const doc = document as any;
    if (doc.caretPositionFromPoint) {
        const position = doc.caretPositionFromPoint(x, y);
        if (position?.offsetNode?.nodeType === Node.TEXT_NODE) {
            return {text: position.offsetNode.data, offset: position.offset};
        }
    } else if (doc.caretRangeFromPoint) {
        const range: Range | null = doc.caretRangeFromPoint(x, y);
        if (range?.startContainer?.nodeType === Node.TEXT_NODE) {
            return {text: (range.startContainer as Text).data, offset: range.startOffset};
        }
    }
    return null;
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
        const [labels] = hooks.useState<Record<string, string>>('labels');
        const [images] = hooks.useState<boolean>('images');
        const [imageUrls] = hooks.useState<Record<string, string>>('imageUrls');
        const fileInput = useRef<HTMLInputElement>(null);
        const toolbar = useMemo(() => toolbarOf(labels, images && !readOnly ? () => fileInput.current?.click() : null),
            [labels, images, readOnly]);
        const modes = useMemo(() => modesOf(labels), [labels]);
        const [text, setText] = useState<string>(value ?? '');
        const sent = useRef<string>(value ?? '');
        const pending = useRef<string | null>(null);
        const timer = useRef<number | undefined>(undefined);
        const colorMode = useColorMode();
        const root = useRef<HTMLDivElement>(null);
        const element = this as any;
        const textNow = useRef<string>(text);
        textNow.current = text;

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

        // Ctrl+click (Cmd+click) on a word: the server looks for a link there
        const linkClicked = (event: MouseEvent) => {
            if (!(event.ctrlKey || event.metaKey) || !element.$server?.linkClicked) {
                return;
            }
            const target = event.target as HTMLElement;
            let clicked: { text: string, offset: number } | null = null;
            if (target instanceof HTMLTextAreaElement) {
                clicked = {text: target.value, offset: target.selectionStart ?? 0};
            } else if (target.closest?.('.wmde-markdown')) {
                clicked = textAtPoint(event.clientX, event.clientY);
            }
            if (!clicked) {
                return;
            }
            event.preventDefault();
            const typed = pending.current;
            window.clearTimeout(timer.current);
            timer.current = undefined;
            if (typed !== null) {
                sent.current = typed;
                pending.current = null;
            }
            element.$server.linkClicked(typed, clicked.text, clicked.offset, event.shiftKey);
        };

        // the Markdown of a stored image goes in at the cursor (or at the end when the text area is not there)
        const insert = (markup: string) => {
            const area = root.current?.querySelector('textarea');
            const current = textNow.current;
            const start = area ? area.selectionStart : current.length;
            const end = area ? area.selectionEnd : current.length;
            change(current.slice(0, start) + markup + current.slice(end));
        };

        const upload = (file: File) => {
            const reader = new FileReader();
            reader.onload = () => {
                const data = String(reader.result ?? '');
                const base64 = data.substring(data.indexOf(',') + 1);
                element.$server?.uploadImage(file.name || 'image', base64).then((markup: string) => {
                    if (markup) {
                        insert(markup);
                    }
                });
            };
            reader.readAsDataURL(file);
        };

        // the image files of a paste or a drop, none when images are not allowed here
        const imageFiles = (files: FileList | null | undefined): File[] =>
            images && !readOnly ? Array.from(files ?? []).filter(file => file.type.startsWith('image/')) : [];

        const previewOptions = useMemo(() => ({
            rehypePlugins: [[rehypeSanitize, SANITIZE_SCHEMA]],
            urlTransform: (url: string) => url.startsWith('resource:')
                ? (imageUrls?.[url.substring('resource:'.length).trim().toLowerCase()] ?? '')
                : url
        }), [imageUrls]);

        const change = (next?: string) => {
            const nextText = next ?? '';
            setText(nextText);
            pending.current = nextText;
            window.clearTimeout(timer.current);
            timer.current = window.setTimeout(flush, SEND_DELAY_MS);
        };

        return (
            <div data-color-mode={colorMode} ref={root} onClickCapture={toolbarClicked} onClick={linkClicked}>
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
                        onBlur: flush,
                        onPaste: event => {
                            const files = imageFiles(event.clipboardData?.files);
                            if (files.length) {
                                event.preventDefault();
                                files.forEach(upload);
                            }
                        },
                        onDragOver: event => {
                            if (images && !readOnly && event.dataTransfer?.types?.includes('Files')) {
                                event.preventDefault();
                            }
                        },
                        onDrop: event => {
                            const files = imageFiles(event.dataTransfer?.files);
                            if (files.length) {
                                event.preventDefault();
                                files.forEach(upload);
                            }
                        }
                    }}
                    previewOptions={previewOptions as any}
                />
                <input type="file" accept={IMAGE_TYPES} multiple hidden ref={fileInput}
                       onChange={event => {
                           const input = event.currentTarget;
                           Array.from(input.files ?? []).forEach(upload);
                           input.value = '';
                       }}/>
            </div>
        );
    }
}

customElements.define('marginalia-markdown-editor', MarkdownEditorElement);
