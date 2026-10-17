---
label: Commands
order: 945
icon: terminal
---

# Commands

Commands are small actions you run by typing them, instead of clicking through menus. Marginalia itself comes with only
a couple of example commands; the real use of the command line is that [extensions](../extensions/index.md) can add
commands of their own, for example to look something up, change the book or start a task. What a command does is
described by the command itself – you can read it while you type.

The **command line** is the field next to the menu below the story, in the [story editor](../books/story-editor.md).

## Running a command

1. Click into the command line and type `/`. The list shows all the commands, each with its name and a short
   description. Typing more narrows the list: the filter looks at the name of the command, its title and its description.
2. Pick the command with the mouse, or with the arrow keys and **Enter**. A command that needs nothing runs at once; for
   one that takes arguments, `/name ` is written into the field and you continue with the arguments.
3. Type the arguments and press **Enter**, or click the play button next to the field, to run it. The field is emptied
   as soon as the command starts; a text that is not a command stays, so you can correct it.

Text that does not start with `/` is not a command, and nothing is run. Clicking away from the field or pressing **Esc**
never runs anything, so you cannot start a command by accident.

## The help

While you type, a small window opens above the field. It covers part of the story but moves nothing, and it closes when
you leave the field or run the command. It shows:

- what the command does,
- the forms the command has (see [Forms](#forms-of-a-command)) that fit what you have typed so far, with the argument you
  are typing now highlighted and its description,
- what the command returns,
- a hint when you have typed more arguments than the command takes, or when no command has that name.

While you are still typing the name, the list of commands is the help.

## Arguments

Arguments are separated by spaces, and repeated spaces count as one. To put a space *inside* an argument, you have
three ways:

| You type                     | The command gets                | Why                                        |
|------------------------------|---------------------------------|--------------------------------------------|
| `/hello-world two\ words`    | one argument, `two words`       | `\ ` (backslash, space) is a space         |
| `/hello-world "two words"`   | one argument, `two words`       | double quotes keep everything inside together |
| `/hello-world say\ "hi"`     | one argument, `say hi`          | quotes can start in the middle of an argument |

The other special characters:

| You type                      | You get                    |
|-------------------------------|----------------------------|
| `\\`                          | a backslash                |
| `\"`                          | a double quote, with or without quotes around it: `"say \"hi\""` is `say "hi"` |
| `""`                          | an empty argument          |

A backslash in front of anything else stays as it is, so `C:\books\story` is passed unchanged. If you open a quote and
never close it, everything to the end of the line belongs to that argument.

Arguments are text. A command that wants a number, a yes/no or one of a few options converts the text itself; if the text does
not fit, the command is not run and a notification says so.

## Forms of a command

One command can take different arguments: `/hello-world` with nothing, and `/hello-world something` with one argument
are two **forms** of the same command. Marginalia picks the form by the number of arguments you typed and by what they
look like. If no form fits, a notification says that the command does not take these arguments, and the help shows the
forms it does have.

## The result

When a command returns something, it is shown in a window above the command line. Close it with the cross, with **Esc**,
or just carry on: it disappears when you type something new or run another command. A command that returns nothing shows
no window.

If a command fails, or there is no such command, you get a notification with the reason.

## Commands that take time

Most commands answer at once. A command that needs time shows a window with an animated bar and a **Stop** button, and
the story is locked until the command is done. Under the bar the command can say what it is doing now and, in smaller
letters, the detail of the step. **Stop** cancels the command; it should return immediately. You can try it with
`/hello-world-async`.

## Commands that come with Marginalia

| Command                              | What it does                                                                       |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/hello-world`                       | Returns *Hello world*.                                                             |
| `/hello-world <message>`             | Returns the message it was given.                                                  |
| `/hello-world-async [<message>]`     | The same, but after 10 seconds; shows the **Stop** window while it waits.          |
| `/take-backup`                       | Takes a backup of the book that is open; it appears in the **Backups** tab of the book. Says no book is open when there is none. |

Commands from extensions are listed in the documentation of the extension, and you see them in the list as long as the
extension is loaded.

## Limits

- The command line is in the story tab of the desktop layout. The [phone view](../phone.md) does not have it yet.
- A command runs as you, in your session, and works with the book you have open.
