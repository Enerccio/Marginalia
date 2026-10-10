---
label: Exporting a book
order: 55
---

# Exporting a book

Export turns the story into a file you can read, print, send or publish elsewhere. Unlike a
[backup](backups.md) it contains only the text, as a finished book.

Only the [active branch](branches-and-story-tree.md) is exported - the same parts the story editor shows. To export
another version of the story, switch to that branch first.

## Exporting

1. Open the book and go to the **Story** tab.
2. Open the settings menu (⚙) in the bottom bar and choose **Export story**.
3. Fill in the dialog and choose **Export**.
4. Marginalia collects the parts of the branch and writes the file, which takes a moment for a long book. A progress
   bar shows how far it is.
5. When the file is ready, a dialog offers it for download. Choose the **Download** button to save it.

![The Export story dialog](../../images/export-dialog.png)

| Field | |
|---|---|
| **Format** | The file type, see below. |
| **File name** | Name of the downloaded file without the extension, which Marginalia adds. Starts as the name of the book. |
| **Book title** | The title in the file (title page and file properties). Starts as the name of the book; changing it doesn't rename the book. |
| **Author** | Starts as your full name from your [account](../account.md). Empty means no author. |
| **From message**, **To message** | Which parts to export. Parts are counted from 1 in the order of the story, parts without text are not counted. Leave **To message** empty to export to the end. |
| **Include title page** | Starts the file with a first page holding the title and the author. |

![The finished export, ready to download](../../images/export-download.png)

## Formats

| Format | |
|---|---|
| **Plain text** (`.txt`) | Just the text in UTF-8. Formatting such as *italics* is dropped, paragraphs are separated by an empty line. |
| **HTML** (`.html`) | One page with a book-like style included, so it looks the same in every browser. It can be printed and has no external files. |
| **Word document** (`.docx`) | Opens in Word, LibreOffice and similar programs. Justified text, with the title and author set in the file properties. |
| **PDF** (`.pdf`) | A5 pages with embedded fonts that cover Latin, Greek and Cyrillic text. |
| **EPUB** (`.epub`) | An e-book for e-readers and reading apps. Long books are split into several files inside the EPUB so readers stay fast. |

The text of the parts is Markdown. Emphasis, headings, quotes, lists, code blocks and scene breaks (`---`) are kept in
every format that can show them. Raw HTML in the text is shown as text, never run.

[Images](story-editor.md#images) of the parts are placed after the text of their part, with their captions:

| Format | |
|---|---|
| **HTML** | Embedded in the page, so it is still one file. |
| **EPUB**, **Word document**, **PDF** | Embedded in the file and scaled to fit the page. |
| **Plain text** | Marked with `[Image]` or `[Image: caption]`. |

## Table of contents

When the text contains chapters, the file gets a table of contents after the title page. A chapter is a part of the
story that contains a Markdown heading (a line starting with `#`, e.g. `# Chapter 3: The Harbour`) - the same rule the
[Chapter Marker](../extensions/chapter-marker.md) plugin uses in the story sidebar. The first heading of a part is its title, parts
without a heading belong to the chapter before them. A story without any heading has no table of contents.

In PDF, Word and EPUB every chapter starts on a new page. The title page and the table of contents are pages of their
own as well.

The entries are links to the chapters:

| Format | |
|---|---|
| **HTML** | Links to the chapters of the page. |
| **PDF** | Clickable entries with the page of the chapter, and the chapters are also in the outline (bookmarks) your PDF reader shows next to the pages. |
| **EPUB** | The table of contents of the reader, which is also a page of the book after the title page. |
| **Word document** (`.docx`) | Clickable entries with the page of the chapter. Word asks to update the fields when it opens the file, answer **Yes** to fill in the page numbers (LibreOffice fills them in on its own). It is a list of links and not a Word table of contents field. |
| **Plain text** | No table of contents. |

## Page numbers

The PDF and Word files have the page number in the footer of every page except the title page. The number is the
position of the page in the file, so the title page is page 1 and the first page after it is page 2. The other formats
have no pages of their own.

## The title page

With **Include title page**, the file starts with the book title and the author on a page of their own. The page is
made from a built-in template (`DEFAULT_EXPORT_HEADER_TEMPLATE`), which isn't editable in the application yet.

## Good to know

- The export is a snapshot of the branch when you choose **Export**. Parts generated or edited afterwards are not in
  the file.
- The exported file is kept only until you close the download dialog. Export again if you need it later.
- Summaries, lorebooks, instructions and the model's reasoning are not exported, only the text and the images of the parts. To save
  all of it, make a [backup](backups.md).
