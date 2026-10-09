---
label: Prompt templates
order: 100
---

# Prompt templates

Templates use [Handlebars](https://handlebarsjs.com/guide/) syntax, extended with SillyTavern
[macros](macros.md). This page covers the variables each template gets and the Handlebars part of the syntax.

{%{

## Variables

A variable in double curly braces is replaced by its value: `{{instructions}}` becomes the instructions you wrote for
the part. An empty value prints nothing.

### Variables of each template

| Template | Variables |
|---|---|
| **Master Template** | `{{backgroundLore}}` activated lorebook entries (*In Lore Block*), `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}`, `{{summaries}}` all summaries of the branch |
| **User Prompt** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}` - the turn instructions |
| **Summary Prompt** | `{{text}}` the parts to summarize, `{{backgroundLore}}` lore active when the summarized part was written |
| **Lorebook entry** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}`, `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` |

### Variables available everywhere

Besides its own variables, every template can use:

| Variable | |
|---|---|
| `{{povCharacter}}`, `{{sceneSetting}}`, `{{presentCharacters}}`, `{{instructions}}` | The turn instructions of the part being written (for a summary: of the summarized part). |
| `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` | The book's narrative settings. |
| `{{manuscriptName}}`, `{{manuscriptDescription}}` | The book's name and description. |

The hints popover (ⓘ) of every template field lists them too.

### Typos

A variable that doesn't exist is printed as the word **`Error`**: `{{instruction}}` (missing *s*) puts *Error* into
the prompt. The template fields (book *Prompts*, *Settings*, lorebook entries) list such names under the field as
*Unknown names (sent to the model as "Error")*. It's only a warning - the template is still saved - so fix the names
it lists. Variables and sections are checked everywhere, also inside `{{#if}}` branches that aren't printed. Names
used only as a condition are not reported, because they don't print *Error*: in `{{#if name}}` an unknown name is
false, in the SillyTavern `{{if name}}` it is taken as plain text, which is true.

## Sections

A section prints its content only when the variable has a value. Inside, `{{.}}` is the value:

```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```

With an empty *Scene Setting*, the whole line disappears - the default templates use this everywhere, so the prompt
has no empty headings.

An **inverted section** prints its content only when the variable is empty:

```handlebars
{{^povCharacter}}Write from an omniscient narrator's point of view.{{/povCharacter}}
```

## Conditions

`{{#if}}` with an optional `{{else}}`:

```handlebars
{{#if presentCharacters}}
Characters present: {{presentCharacters}}
{{else}}
The POV character is alone.
{{/if}}
```

`{{#unless x}}...{{/unless}}` is the opposite of `{{#if x}}`. The SillyTavern form `{{if x}}...{{/if}}` works too and
has more options (negation, variables, comparisons), see [Conditionals](macros.md#conditionals).

Macros without arguments can be used like variables in sections and conditions:

```handlebars
{{#lastMessage}}The story so far ends with: {{.}}{{/lastMessage}}
{{#if summary}}...{{/if}}
```

## Comments and escaping

| Syntax | Result |
|---|---|
| `{{!-- a comment --}}`, `{{! short comment }}` | Removed from the output. Use them for notes in your templates. |
| `\{{instructions}}` | Prints `{{instructions}}` literally instead of the value. |
| `{{{instructions}}}` | Same as `{{instructions}}`. Nothing is HTML-escaped in Marginalia, so `<`, `&` and quotes are printed as they are. |

The SillyTavern comment `{{// ...}}` and escape `\{\{...\}\}` work too, see [Utility](macros.md#utility).

## Order of evaluation

For one generated part, the templates are processed in this order:

1. the **user prompt**;
2. the **lorebook entries** that were activated, in their order;
3. the **master template**.

All of them share one set of [variables](macros.md#variables): a value set with `{{setvar}}` in the user prompt can
be read in lorebook entries and the master template; a value set in a lorebook entry is visible to later entries and
the master template, but not to the user prompt, which was already processed.

Lorebook filters see the processed user prompt, so a macro in the user prompt can produce text that activates
entries.

## What is kept between parts

| | |
|---|---|
| **Local variables** (`{{setvar}}`, `{{.name}}`) | Saved with the generated part. The next part starts with the values of the last part of the active branch, so each branch has its own values. *Regenerate* starts again from the values of the part before. |
| **Global variables** (`{{setglobalvar}}`, `{{$name}}`) | Saved with the book, shared by all branches. They are not undone by *Regenerate* or deleting a part. |
| **Summaries** | Can read variables, but their changes are discarded. |

`{{pick}}` gives the same result every time for the same book, `{{random}}` a new one every time.

## Writing your own templates

Start from the default: open the hints popover and click **Insert Default Template**, then change it. Things to keep:

- the `{{#backgroundLore}}` block in the master template - without it, the lorebook isn't used (only entries with
  *Before User Prompt* still reach the model);
- the `{{#summaries}}` block in the master template - without it, summaries are lost;
- `{{instructions}}` in the user prompt - without it, the model doesn't see what should happen;
- `{{text}}` in the summary prompt - without it, there is nothing to summarize.

}%}
