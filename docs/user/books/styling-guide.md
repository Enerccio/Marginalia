# Styling guide: make your book look like your book

A **text style** decides how the text of your parts looks: the font, the colors, the width of the column, the headings, the line
between scenes. This guide takes you from the first change to a finished look, and ends with a complete example to download: a style for a
horror book that uses every option and some CSS of its own.

Where a style is used:

| Where | What happens |
|---|---|
| **The story editor** | The style of the book (or your default) is the look of every part. See [Text styles](story-editor.md#text-styles). |
| **The reader** | A [published book](publishing.md) is read in the style the *reader* chose from their own styles (the Book style until they choose). |
| **Exports** | A style marked **For exports** is chosen in the [export dialog](export.md#print-styles). HTML and EPUB take the stylesheet, PDF and Word take the numbers (see [what each export takes](#what-the-exports-take)). |

## Make your first style

1. Open **Settings → Styles** (the [Styles tab](../account.md#styles)).
2. Select **Book** in the list and press **Duplicate**. The built in styles can not be changed, so you always start from a copy named
   *Book (copy)*. Give it a name.
3. Change a few boxes on the **Options** tab. The **Preview** under the form shows a sample text (a chapter title, emphasis, a quote, a scene break,
   a smaller heading) and follows every change as you type.
4. Press **Save** of the settings. Nothing is stored before that, and **Discard Changes** throws the work away.
5. In the story editor open the settings menu (⚙) → **Appearance** → **Change Styles For Text** and tick your style. It is stored in the
   book. To use the style for all your books, select it in the list and press **Set as default**.

A box left empty keeps the look of the application, so a style only says what you want different. A style with every box empty is the plain look.

## The options

Every box takes a CSS value. Units you will use: `rem` (the size of the text of the page, 1rem is normally 16px), `em` (the size of the
current text), `px`, `%`, and colors as `#336699`, `rgb(51, 102, 153)` or a name such as `ivory`.

### Text

| Option | What it does | Example |
|---|---|---|
| **Font family** | The font of the text. Give a list: the first one the computer has is used, the last should be a generic family (`serif`, `sans-serif`, `monospace`). | `"Garamond", "Georgia", serif` |
| **Font size** | The size of the text. | `1.1rem` |
| **Line height** | The distance between lines. A bare number is a multiple of the font size. | `1.7` |
| **Letter spacing**, **Word spacing** | Extra space between letters, between words. Small values: a tiny amount of letter spacing makes text airy. | `0.01em`, `0.05em` |
| **Text color** | The color of the text. | `#cfc6bb` |
| **Text align** | `left`, `right`, `center` or `justify`. | `justify` |
| **Hyphenation** | Breaks long words at the end of a line. It uses the dictionary of the book's [language](managing-books.md#the-about-tab), so set the language first. Best together with *justify*. | on |
| **Paragraph spacing** | The space under a paragraph. A book usually has little, or none with a first line indent. | `0.3em` |
| **First line indent** | Indents the first line of a paragraph, as in a printed book, except after a heading or a scene break. | `1.5em` |
| **Drop cap** | A large first letter of the first paragraph after a heading. | on |

### Page

| Option | What it does | Example |
|---|---|---|
| **Text width** | A column of its own width, centered. A line of 60 to 75 characters reads best, about `34em` to `40em`. | `38em` |
| **Background color** | The color behind the text; it fills the whole area, not only the text blocks. Choose a text color that reads on it. | `#0c0a0a` |

### Emphasis, links and code

| Option | What it does |
|---|---|
| **Emphasis color** | The color of *italic* text. |
| **Strong color** | The color of **bold** text. |
| **Link color** | The color of links. |
| **Code font family** | The font of `code` and of code blocks. |

### Quotes

| Option | What it does |
|---|---|
| **Quote color** | The color of the text of a quote (`> a line`). |
| **Quote border color** | The color of the bar at the side of a quote. |

### Headings

| Option | What it does |
|---|---|
| **Heading font family** | The font of `#`, `##` and `###` headings. |
| **Heading size** | The size of the first level; the lower ones are smaller in proportion. |
| **Heading color** | The color of headings. |
| **Heading align** | `left`, `center` or `right`. |
| **Heading small caps** | Writes the headings in small capitals. |

### Scene break

A line with `---` in the text is a break between scenes. By default it is three dots. **Scene break text** is what is shown instead
(`* * *`, `~`, `† † †`), **Scene break color** its color.

## Custom CSS

The options cover the common things. For the rest, the **Custom CSS** tab takes CSS of your own, added after everything else and applied only
inside the style. Two ways to write it:

- **Plain declarations** apply to the whole area that holds the text:

  ```css
  box-shadow: inset 0 0 120px rgba(0, 0, 0, 0.8);
  ```

- **Rules** with a selector. Write them with `&` (the area of the style) in front, or without it: either way they only reach text that has this style.

  ```css
  & .chat-message-markdown h1 { text-shadow: 0 0 12px #8e1b17; }
  & .chat-message-markdown a:hover { color: #b3261e; }
  ```

The text of a part sits in an element with the class `chat-message-markdown`, so a selector for something in the text starts there. The
**Generated CSS** field under the CSS box shows the stylesheet the options make: copy its selectors to see what you can override.

| You want to change | Selector (after `& .chat-message-markdown`) |
|---|---|
| paragraphs | `p` |
| headings | `h1`, `h2`, `h3` |
| italic, bold | `em`, `strong` |
| links | `a`, `a:hover` |
| quotes | `blockquote` |
| lists | `ul`, `ol`, `li` |
| code | `code` (inline), `pre` (block) |
| scene break | `hr`, `hr::after` (the symbol) |
| the first line of a paragraph | `p::first-line` |
| selected text | `::selection` |

Things to know:

- **The braces `{ }` must match**, or the style is refused when you save. Comments `/* ... */` are fine.
- A few rules the application needs are `!important` (the text never gets wider than its area, long words wrap, paragraphs keep their line
  breaks). Your CSS can not override those; it can override everything else, because it comes last.
- CSS can change how things look, not what they say. It does not reach the toolbars and tabs around the text.
- Custom CSS is used in the story, the reader and HTML exports. EPUB, PDF and Word do not take it.

## What the exports take

A style for exports is chosen in the [export dialog](export.md#print-styles); **Duplicate** a style and tick **For exports**, or start from one of the four
the application brings. Colors that depend on the theme of the application (the light text of the dark theme) become dark ones for a white
page when exporting.

| | HTML | EPUB | PDF | Word |
|---|---|---|---|---|
| fonts, sizes, line height, spacing, alignment, first line indent | ✓ | ✓ | ✓ | ✓ |
| colors of text, emphasis, strong, quotes, headings, scene break | ✓ | ✓ | ✓ | ✓ |
| background color | ✓ | ✓ | ✓ | ✓ (the color of the page) |
| text width, drop caps, hyphenation, small capitals of headings | ✓ | ✓ | | |
| custom CSS | ✓ | | | |

E-book readers often use their own fonts, sizes and colors whatever the stylesheet says. PDF and Word can only use the fonts the format has: the PDF chooses serif, sans or monospace from the stack you wrote; Word names the first font of
the stack and shows its own if the computer lacks it.

## Share a style: export and import

Under the list of styles, **Export** saves the selected style as a file (`name.style.json`; a built in style can be exported too) and **Import**
reads such a file. An imported style is added to your own styles as a new style, it is not stored before you press **Save**, and it keeps its
name, so rename it if you already have one like it.

The file is JSON:

```json
{
  "format": "marginalia-text-style",
  "version": 1,
  "styles": [
    {
      "name": "My style",
      "forExport": false,
      "options": { "FONT_SIZE": "1.1rem", "TEXT_COLOR": "#222222" },
      "customCss": "& .chat-message-markdown h1 { letter-spacing: 0.1em; }"
    }
  ]
}
```

It can hold several styles, and you can write one by hand. The options are named in capitals (`FONT_SIZE`, `TEXT_COLOR`, `SCENE_BREAK_TEXT`...); the [horror example](#an-example-a-horror-book) has all of them. An option this version of Marginalia
does not know is left out (you are told how many), a file from a newer format version is refused, and so is a style whose custom CSS has
braces that do not match.

## An example: a horror book

[Download the horror style](../../files/horror-book.style.json) (`horror-book.style.json`), import it in *Settings → Styles* and select it
to see it in the preview. It sets **every option** and uses custom CSS for what the options do not cover:

| | |
|---|---|
| **Page** | Almost black background `#0c0a0a`, a column of `38em`, and a dark vignette closing in on the edges (custom CSS). |
| **Text** | Garamond at `1.12rem`, line height `1.8`, justified with hyphenation, a first line indent of `1.6em` and almost no space between paragraphs, like a printed novel. The text is the grey of old paper `#cfc6bb`, with a drop cap after each heading. |
| **Emphasis** | Italics in a dusty rose `#b99a94`, bold in blood red `#b3261e`. |
| **Links** | Sickly green `#8fa07e` that turns red and wavy when the pointer is on it (custom CSS). |
| **Quotes** | Grey text with a dark red bar `#5c1010` and a faint red stain behind (custom CSS). |
| **Headings** | Palatino in small capitals, centered, dark red `#8e1b17`, with a faint glow (custom CSS). |
| **Scene break** | `† † †` in dark red; lists get a cross instead of a dot (custom CSS). |
| **Code** | Courier New, on a nearly black background. |

To try it on a real text, paste this into a part (or the preview shows a similar sample):

```markdown
# Chapter One: The House on Harrow Lane

The rain had been falling for *three days* when Mara found the door. It was not on the plan of the house, and it was **warm** to the touch.

> Do not open it after dark. — scratched into the plaster, in a hand that was not hers

She counted the steps down. Thirteen. At the bottom there were:

- a lantern, still burning;
- a chair, turned to face the wall;
- her own name, written in the dust.

---

By morning the lantern was out, and the [house records](https://example.com) said nobody had lived there for forty years.
```

Then change it: make the background `#14100c` for a candle-lit brown, set **Text width** to `32em` for a narrower page, or remove the custom CSS to see
what the options alone do. When you have a look you like, **Export** it and keep the file with your book.
