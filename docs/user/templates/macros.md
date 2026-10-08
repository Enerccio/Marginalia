# Macro reference

Marginalia understands the macros of [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/), so
prompts and world info written for SillyTavern work without changes. Macros work in every
[template](templates.md): master template, user prompt, summary prompt and lorebook entries.

## Syntax

| Form | Example |
|---|---|
| without arguments | `{{user}}` |
| arguments separated by `::` | `{{setvar::mood::nervous}}` |
| first argument after a space | `{{getvar mood}}`, `{{setvar greeting::Hello}}` |
| legacy single `:` | `{{getvar:mood}}`, `{{random:north,south}}` |
| nested macros | `{{getvar::{{user}}_mood}}` |
| scoped (content as the last argument) | `{{setvar backstory}}Long text...{{/setvar}}` |

- Macro names are case-insensitive: `{{User}}` and `{{USER}}` work.
- Spaces around names and arguments are ignored: `{{ getvar :: mood }}`.
- Extra `::` go into the last argument: `{{setvar::x::a::b}}` sets `x` to `a::b`.
- Scoped content is evaluated first, then dedented and trimmed. With `#` after the braces,
  `{{#setvar name}}...{{/setvar}}`, the whitespace is kept exactly.

## Names and participants

Marginalia has no personas or character cards; the names come from the turn instructions of the part being written.

| Macro | Result |
|---|---|
| `{{user}}` | The POV character. |
| `{{char}}` | The POV character. Alias `{{charIfNotGroup}}`. |
| `{{group}}` | The present characters, or the POV character when none are given. Alias `{{groupNotMuted}}`. |
| `{{notChar}}` | The present characters except the POV character (comma or line separated, joined with `, `). |
| `{{description}}` | The book's description. |
| `{{scenario}}` | The scene setting. |
| `{{input}}` | The instructions for the part. |

The SillyTavern markers `<USER>`, `<BOT>`, `<CHAR>`, `<GROUP>` and `<CHARIFNOTGROUP>` (any case) work as well.

With POV character *Alice* and present characters *Alice, Bob, Carol*:
`{{user}}` → *Alice*, `{{group}}` → *Alice, Bob, Carol*, `{{notChar}}` → *Bob, Carol*.

## Story history

| Macro | Result |
|---|---|
| `{{lastMessage}}` | The text of the last part of the active branch. Alias `{{lastCharMessage}}`. |
| `{{lastMessageId}}` | The position of the last part, counted from 0. |
| `{{lastUserMessage}}` | The instructions of the last part. |
| `{{summary}}` | The newest [summary](../books/summaries.md) of the active branch. |

For the first part of a book they are empty. For *Regenerate*, "last part" is the one before the regenerated part.

## Date and time

| Macro | Example result |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - the time at a UTC offset (`UTC-5:30` works too) |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [moment.js format tokens](https://momentjs.com/docs/#/displaying/format/), text in `[brackets]` is printed as it is: `{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - time since the last part was written, `just now` for the first part. Alias `{{idle_duration}}`. |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - the difference between two times; `now`, dates (`2026-01-08`) and ISO times work. Unknown values give nothing. |

Times are in the time zone of the computer or server Marginalia runs on, and always in English. For the
[Docker version](../getting-started/docker-server.md), set the time zone with the `TZ` environment variable, e.g.
`TZ=Europe/Prague`.

## Variables

Variables store values between parts: counters, a character's mood, things the story has established. There are two
kinds:

- **local** variables belong to the story branch: each part remembers their values, and a new part starts with the
  values of the part before it;
- **global** variables belong to the book, shared by all branches.

See [What is kept between parts](templates.md#what-is-kept-between-parts).

| Local | Global | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | The value; empty when not set. |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | Sets the value. Prints nothing. |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | Adds a number, or appends text when either isn't a number. Prints nothing. |
| `{{incvar::name}}` | `{{incglobalvar::name}}` | Adds 1 and prints the new value. A value that isn't a number starts from 0. |
| `{{decvar::name}}` | `{{decglobalvar::name}}` | Subtracts 1 and prints the new value. |
| `{{hasvar::name}}` | `{{hasglobalvar::name}}` | `true` or `false`. |
| `{{deletevar::name}}` | `{{deleteglobalvar::name}}` | Removes the variable. Alias `flushvar` / `flushglobalvar`. |

Numbers are printed without trailing zeros: `1.50` plus `1.5` is `3`.

```handlebars
{{setvar::hp::10}}{{addvar::hp::5}}HP: {{getvar::hp}}      → HP: 15
{{setvar::items::a rope}}{{addvar::items::, a lamp}}{{getvar::items}}   → a rope, a lamp
```

### Shorthands

`{{.name}}` is a local variable, `{{$name}}` a global one. On its own, a shorthand prints the value. With an operator:

| Shorthand | Effect | Prints |
|---|---|---|
| `{{.hp = 20}}` | sets the value | nothing |
| `{{.hp += 5}}`, `{{.hp -= 5}}` | adds / subtracts (`+=` appends text to text) | nothing |
| `{{.turn++}}`, `{{.turn--}}` | adds / subtracts 1 | the new value |
| `{{.hp == 17}}`, `{{.hp != 17}}` | compares | `true` / `false` |
| `{{.hp > 10}}`, `>=`, `<`, `<=` | compares numbers (`false` for text) | `true` / `false` |

Fallbacks and defaults:

- `{{.name || Guest}}` prints *Guest* when the variable is empty or falsy (`0`, `false`, `no`, `off`);
- `{{.name ?? Guest}}` prints *Guest* only when the variable isn't set at all;
- `{{.title ||= Wanderer}}` sets the variable to *Wanderer* if it is empty or falsy, then prints it;
- `{{.title ??= Wanderer}}` sets it only when it isn't set at all, then prints it.

Values can contain macros: `{{.log += -> {{user}}}}`.

A part counter, for example, at the top of the user prompt (`+=` prints nothing, and `{{trim}}` removes the empty
line it leaves):

```handlebars
{{.part += 1}}{{trim}}
This is part {{.part}} of the story.
```

## Conditionals

```handlebars
{{if presentCharacters}}
  Present: {{presentCharacters}}
{{else}}
  {{user}} is alone.
{{/if}}
```

The condition can be:

| Condition | True when |
|---|---|
| a macro name - `{{if user}}` | the macro gives a value |
| a variable - `{{if .mood}}`, `{{if $chapter}}` | the variable has a value |
| a template variable - `{{if sceneSetting}}` | the variable has a value |
| a comparison - `{{if .hp > 20}}` | the comparison is true |
| any text - `{{if yes}}` | always, except for the falsy values |
| negation - `{{if !user}}`, `{{if !.mood}}` | the opposite |

Falsy are an empty value and `0`, `false`, `no`, `off` (any case). Everything else is true.

- The short form `{{if user::text}}` prints *text* when the condition is true.
- Only the branch that is taken is evaluated: a `{{setvar}}` in the other branch doesn't happen.
- `{{if}}` blocks are dedented and trimmed like scoped macros, so they can be indented for readability. `{{#if}}`
  (the [Handlebars if](templates.md#conditions)) keeps the whitespace.
- A `{{if}}` without `{{/if}}` is printed as text.

## Randomness

| Macro | Result |
|---|---|
| `{{random::red::green::blue}}` | One of the options, different every time. Also `{{random:red,green,blue}}` and `{{random}}red, green{{/random}}`. |
| `{{pick::sword::axe::bow}}` | One of the options, but always **the same one** for the same book and the same place in the template. Use it for things that must stay consistent, like a randomly chosen name. |
| `{{roll::2d6+3}}` | A dice roll: `1d20`, `3d4-10`, `d100`; also `{{roll d100}}`, `{{roll:6}}` (one six-sided die). Invalid rolls give nothing. |

## Generation info

| Macro | Result |
|---|---|
| `{{model}}` | The name of the [inference provider](../inference-providers.md) of the book. |
| `{{maxContextTokens}}` | The context limit in tokens (protocol or provider, see [Limits](../protocols.md#limits)). Alias `{{maxContext}}`. |
| `{{maxResponseTokens}}` | The response limit in tokens. Alias `{{maxResponse}}`. |
| `{{maxPrompt}}` | Context minus response: the room for the prompt. |
| `{{lastGenerationType}}` | `normal` for a new part, `regenerate`, `swipe`, or `quiet` in summaries. |

## Utility

| Macro | Result |
|---|---|
| `{{newline}}`, `{{newline::2}}` | One or more line breaks. |
| `{{space}}`, `{{space::4}}` | One or more spaces. |
| `{{trim}}` | Removes the line breaks around it. Useful after lines that only set variables. |
| `{{noop}}` | Nothing. |
| `{{reverse::text}}` | The text reversed. |
| `{{// comment}}` | A comment, removed. `{{//}}` ... `{{///}}` comments out everything between them, macros included. |
| `\{\{user\}\}` | Prints `{{user}}` literally. |

## SillyTavern-only macros

Macros that only make sense in SillyTavern chats - personas and character card fields (`{{persona}}`,
`{{personality}}`, `{{charPrompt}}`, `{{mesExamples}}`...), instruct mode sequences (`{{instructUserPrefix}}`...),
author's notes, swipe IDs, `{{outlet}}` and the like - are recognized and print **nothing**, so imported world info
doesn't leak them into the prompt. `{{isMobile}}` and `{{hasExtension}}` print `false`.

A macro Marginalia doesn't know:

- with arguments (`{{fooBar::x}}`) is printed as it is, like SillyTavern does;
- without arguments (`{{fooBar}}`) is printed as **`Error`**, the same as an unknown
  [template variable](templates.md#typos).
