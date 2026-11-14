---
label: Translating the manual
order: 150
verified: 8a278a19f
covers:
  - tools/manual_i18n.py
  - tools/retype_build.sh
---

# Translating the manual

The user manual (`manual/index.md` and `manual/user`) can be translated into the languages of the application (`ja`, `fr`,
`es`, `de`, `ko`, `it`, and `zh`, which has no bundle in the application) with Claude Code. The developer manual stays English. The translating is
incremental: only the pages whose English source changed since they were translated are sent again. The tool is
`tools/manual_i18n.py` (Python 3, standard library only; it calls `claude -p`, so it runs on a Claude Code login or API key).

```sh
python3 tools/manual_i18n.py status                          # per language: current / outdated / missing pages
python3 tools/manual_i18n.py translate --lang de --dry-run   # what would be sent (pages, requests, words)
python3 tools/manual_i18n.py translate --lang de             # missing and outdated pages (haiku by default, --model sonnet ...)
python3 tools/manual_i18n.py translate --lang de --only user/books/prompts --limit 3 --jobs 2
python3 tools/manual_i18n.py usage --lang de                  # tokens and cost per translated page (build/i18n-usage/, not tracked)
python3 tools/manual_i18n.py glossary --lang de user/books/prompts.md   # the UI labels the model is told to use
python3 tools/manual_i18n.py assemble --lang de --out build/i18n/de/manual
python3 tools/manual_i18n.py verify --lang de                 # pages that changed code, link targets, URLs or tags of the English page (--list: the paths only)
```

## What is stored

`translations/<lang>/<path below manual/>` holds the translated pages and `translations/<lang>/manifest.json` the SHA-256 of the
English file each page was made from (plus the model and the time). A page is *current* when today's English file has that hash,
*outdated* when it differs, *missing* without a file. The manifest is written after every page, an interrupted run loses nothing
that was finished. Commit the translations; they are the source of the translated site.

## How a page is translated

- A page is split at `## ` headings (outside code fences) into chunks of up to `--chunk-words` (1500) words, the biggest pages have 14000.
- **What must not change never reaches the model.** Before the request the code blocks (` ``` ` and `~~~`), the inline `code`, the targets of links
  and images, the URLs and the HTML tags are replaced by placeholders (`⟦B3⟧` a whole block alone on its line, `⟦C7⟧` inline code, `⟦L2⟧` a link target,
  `⟦U1⟧` a URL, `⟦H4⟧` a tag) and put back, character for character, in the answer, so no translator can change a macro, a command, a path or a
  link; the text of links, the headings and the prose are translated, the text inside a code block is not (a comment in a snippet, the labels of a
  diagram and an example paragraph stay English). An answer that loses, repeats or alters a placeholder is rejected and asked again with the
  placeholders named (an inline one may move in its sentence: the word order of a language can need it).
- The system prompt holds the rules (keep the structure, never translate code, link targets, macros, keys and product names,
  only the `label:` of the front matter) and a **glossary**: the labels of `LocalizationBundleEN` that the chunk shows in bold,
  italic or code, with their text from the bundle of that language, so the manual says what the screen says. A label that is
  not in the bundle (a plugin's) stays English. Add the words to the bundles, not to the translator.
- Every answer is checked against its chunk (same number of code fences and headings, same link and image targets, same front matter keys,
  a plausible length). A chunk that fails is asked again with the problems named (`--retries`, 2); a page with a failed chunk is not written and the run
  exits with 1.
- A heading whose id changes with its translation gets an `<a id="english-id"></a>` line before it, so `page.md#english-id` links of other pages work.

`verify --lang de` is the same rule applied to the pages that are there: it lists those that lost or changed something of the English page (what a
translation has more, the `<a id>` lines, is not looked at), `--list` prints their paths for `translate --force --only`. `ManualLanguageTest` checks the
code blocks and the inline code of every page in every language, so a translation that changes them fails the build.

## The site

`assemble` copies `manual/` and puts the translated pages over it: images and the developer manual are shared (the pictures show the
English screens), an untranslated page is English. `override --lang de` prints the JSON for `retype build --override`: it moves the
input and the output (`docs/de`, `<url>/de`) and adds a language link for English and every language that has translations to the top bar of the
site. Retype has no language switcher of its own, so each language is its own build. `tools/retype_build.sh` (also what the `Manual`
workflow runs) builds the English site, then for every language of `python3 tools/manual_i18n.py languages` (the ones that have
translated pages) the `assemble` and the build of its site, with the label, the footer and the links of `override`, so a language that
has translations has a site. The in-app help links to these sites.

## In the application

The translated pages are also packed into the application (`translations/<language>/user` as `classpath:/manual-translations/`, see
[Packaging](packaging.md)) and the help assistant reads the manual in the language of the user's page, with the links of that
language's site; a page that is not translated is English, and a language without translations gets the English manual (see
[Services](services.md#the-manual)). Outdated translations are used as they are (as `assemble` does), so run `status` before a release
and `translate` for the languages the release is meant to have.
