---
label: Regular expressions
order: 935
icon: search
---

# Regular expressions

A regular expression (*regex*) is a short pattern that describes text instead of spelling it out: "the word Harry, but
not Harrys", "any two words in quotes", "three or more spaces in a row". Marginalia uses them in two places:

- **Search and replace** of the [story](books/story-editor.md#search-and-replace) and of a
  [lorebook](lorebooks/entries.md#search-and-replace), and the matching [commands](commands/index.md), when **Search by
  regex** is on. The pattern finds the text, the replacement can reuse pieces of it.
- The **Regex** filter of a [lorebook entry](lorebooks/entries.md#activation), which decides when the entry goes into
  the prompt.

The dialect is the one of Java. This page shows what you will use most; the complete reference is linked at the end.

!!!
You do not need regexes for plain words. With **Search by regex** off the text is searched exactly as typed, so a
`.` or a `(` in it is just a dot or a bracket. Turn the regex on only when a single fixed text is not enough.
!!!

## The building blocks

| Write | Matches | Example |
|---|---|---|
| a letter or a digit | that character | `cat` finds *cat* in *concatenate* |
| `.` | any one character (not a line break) | `c.t` finds *cat*, *cot*, *c7t* |
| `\d` `\w` `\s` | a digit, a letter/digit/`_`, a space or line break | `\d\d` finds *42* |
| `[abc]` `[a-z]` | one of the listed characters, or a range | `gr[ae]y` finds *grey* and *gray* |
| `[^abc]` | one character that is *not* listed | `[^"]` is anything but a quote |
| `\b` | the edge of a word | `\bcat\b` finds *cat* but not *concatenate* |
| `^` `$` | the start and the end of a line | `^Chapter` finds *Chapter* at the start of a line |
| `x?` `x*` `x+` | `x` zero or one time, any number of times, at least once | `colou?r` finds *color* and *colour* |
| `x{2,4}` | `x` two to four times | `\.{3}` is three dots in a row |
| `\` | the next character is taken literally | `\.` is a dot, `\?` a question mark, `\(` a bracket |

Brackets make a **group**, and a vertical bar inside a group is "or": `(Mr|Mrs) Dursley` finds *Mr Dursley* and
*Mrs Dursley*, and `\b(Voldemort|You-Know-Who)\b` finds either name. A `?`, `*` or `+` after a group applies to all of
it: `(very )+good` finds *very good* and *very very good*.

The characters `. ? * + ^ $ | \ ( ) [ ] { }` have a meaning of their own. To search for one of them as itself,
put a `\` in front of it: to find *Really?!* write `Really\?!`.

By default a regex ignores nothing: *harry* does not find *Harry*. Put `(?i)` at the start to ignore the case:
`(?i)harry` finds *Harry*, *HARRY* and *harry*. (The Search and replace windows lock **Case sensitive** while the regex
is on, so this is how you switch it off there; the lorebook **Regex** filter always ignores the case.)

A match of no length at all (a pattern like `a*` that can match nothing) is ignored by Search and replace.

## Typical uses in a story

Each row is something you may want to do to a book. **Search** is what you type into *Search for*, **Replace** what you
type into *Replace with*.

| What you want | Search | Replace |
|---|---|---|
| Rename a character, but not part of a longer word (*Harry* but not *Harrys* or *Harrison*) | `\bHarry\b` | `Henry` |
| Rename, whatever the case | `(?i)\bharry\b` | `Henry` |
| Rename a name and its possessive in one go | `\bHarry('s)?\b` | `Henry$1` |
| Turn straight quotes into curly ones | `"([^"]*)"` | `“$1”` |
| Three dots into one ellipsis character | `\.{3}` | `…` |
| Two or more spaces into one | ` {2,}` | ` ` (one space) |
| Remove spaces at the end of lines | ` +$` | (empty) |
| A long dash made of two hyphens | ` ?-- ?` | ` — ` |
| Swap *Surname, Name* to *Name Surname* | `(\w+), (\w+)` | `$2 $1` |
| Find where a word is used, without changing anything | the pattern | (leave empty and only press **Search**) |

To make several spellings one name, search `\b(Voldemort|You-Know-Who|He-Who-Must-Not-Be-Named)\b` and replace it with
`Tom Riddle`.

### Taking the match into the replacement

Everything a regex puts in round brackets is a **group**, numbered from the left by its opening bracket: the replacement
`$1` is the text of the first group, `$2` of the second, `$0` all of the match. In `(\w+), (\w+)` the first group is the
word before the comma, the second the word after it, so `$2 $1` swaps them: *Potter, Harry* becomes *Harry Potter*.

You can name a group, `(?<last>\w+), (?<first>\w+)`, and use the name in the replacement: `${first} ${last}`.

A `$` or a `\` that should stay as itself in the replacement has to be escaped: to put *$5* into the text, write `\$5`.
Without the regex the replacement is always taken exactly as typed.

If the pattern is wrong (a bracket that is never closed) or the replacement names a group the pattern does not have,
a notification says so and nothing happens; a search never changes anything.

### Match only in a context

A *lookahead* `(?=...)` and a *lookbehind* `(?<=...)` look at the text around the match without taking it, so only the
part outside them is replaced:

| Search | Finds |
|---|---|
| `Harry(?= Potter)` | *Harry* only when *Potter* follows (and leaves *Potter* alone) |
| `(?<=Professor )McGonagall` | *McGonagall* only after *Professor* |
| `Harry(?! Potter)` | *Harry* not followed by *Potter* |

### Lazy and greedy

`*` and `+` take as much as they can: in `He said "yes" and "no"` the pattern `".*"` matches the whole `"yes" and "no"`.
Add a `?` (`".*?"`), or say what is not allowed (`"[^"]*"`), to stop at the first closing quote.

The search treats every line on its own: `.` does not cross a line break, and `^` and `$` match at the start and the end
of every line of a part, not only of the whole text. To match a line break write `\n`.

## In a lorebook entry filter

The filter of a lorebook entry set to **Regex** is matched against the request for the part, anywhere in it. It ignores
the case, and `.` does match line breaks there, so `varro.*harbor` finds Varro and the harbor even on different lines.
Typical filters:

- `\bvarro\b` - Varro as a whole word.
- `\b(varro|captain)\b` - Varro or the captain.
- `varro.*(harbor|docks)` - Varro and then, later, the harbor or the docks.
- `dragons?` - *dragon* or *dragons*.
- `(?<!no )magic` - *magic*, unless it stands right after *no*.

An invalid regex never activates the entry, and the reason is written to the log. See
[entries and activation](lorebooks/entries.md#activation) for the rest of how an entry is chosen.

## In the commands

[`/search-replace-parts`](commands/index.md#commands-that-come-with-marginalia) and `/search-replace-lore-entries` take
the regex as the third argument (`true`), after the search and the replacement. The command line splits on spaces, so a
pattern or a replacement with a space is put in double quotes. A backslash in front of a letter is passed on as it is
(`\b`, `\d`), but `\\` becomes a single backslash and `\"` a quote, so a literal backslash in the pattern is `\\\\`.

```
/search-replace-parts "\bHarry\b" Henry true
/search-replace-parts "(\w+), (\w+)" "$2 $1" true
/search-replace-parts "(?i)\bharry\b" Henry true
```

The commands show no list of matches: they ask you whether to replace the number of matches found. When you are not
sure about a pattern, try it first in the **Search and replace** window, which shows every match as it is and as it will
be.

## Tips

- Try a pattern with **Search** first and open a few of the rows: the window shows each match with its surroundings, as it
  is and as it will be. Replace only when the result is what you wanted.
- Start simple and add to the pattern step by step; a pattern that finds too much usually needs `\b` around a word, or `?`
  to stop a `*`.
- A [backup](books/backups.md) before a big replace is cheap (`/take-backup`), and a replace cannot be stopped once it
  has started.
- Changed summaries: when the replace touches the text that a [summary](books/summaries.md) was made from, the window warns
  that the summary would become outdated; see [Search and replace](books/story-editor.md#search-and-replace).

## Further reading

This page covers the basics. Java's full syntax, with every construct, flag and character class, is in the
[documentation of `java.util.regex.Pattern`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html),
and the replacement syntax (`$1`, `${name}`, `\$`) in
[`Matcher.appendReplacement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Matcher.html#appendReplacement(java.lang.StringBuilder,java.lang.String)).
