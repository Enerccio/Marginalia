---
label: Translating the manual
order: 150
verified: 8a8764342
covers:
  - tools/manual_i18n.py
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
```

## What is stored

`translations/<lang>/<path below manual/>` holds the translated pages and `translations/<lang>/manifest.json` the SHA-256 of the
English file each page was made from (plus the model and the time). A page is *current* when today's English file has that hash,
*outdated* when it differs, *missing* without a file. The manifest is written after every page, an interrupted run loses nothing
that was finished. Commit the translations; they are the source of the translated site.

## How a page is translated

- A page is split at `## ` headings (outside code fences) into chunks of up to `--chunk-words` (1500) words, the biggest pages have 14000.
- The system prompt holds the rules (keep the structure, never translate code, link targets, macros, keys and product names,
  only the `label:` of the front matter) and a **glossary**: the labels of `LocalizationBundleEN` that the chunk shows in bold,
  italic or code, with their text from the bundle of that language, so the manual says what the screen says. A label that is
  not in the bundle (a plugin's) stays English. Add the words to the bundles, not to the translator.
- Every answer is checked against its chunk (same number of code fences and headings, same link and image targets, same front matter keys,
  a plausible length). A chunk that fails is asked again with the problems named (`--retries`, 2); a page with a failed chunk is not written and the run
  exits with 1.
- A heading whose id changes with its translation gets an `<a id="english-id"></a>` line before it, so `page.md#english-id` links of other pages work.

## The site

`assemble` copies `manual/` and puts the translated pages over it: images and the developer manual are shared (the pictures show the
English screens), an untranslated page is English. `override --lang de` prints the JSON for `retype build --override`: it moves the
input and the output (`docs/de`, `<url>/de`) and adds a language link for English and every language that has translations to the top bar of the
site. Retype has no language switcher of its own, so each language is its own build. Building the sites in the `Manual` workflow
is not wired yet: per language `assemble`, then `retype build --override "$(python3 tools/manual_i18n.py override --lang de)"`
(the English build needs the same links: `override --lang en`).
