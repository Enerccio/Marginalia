---
label: 指令导出器
order: 12
---

# 指令导出器

将故事的指令（包括场景设定、视角角色和在场角色）复制到剪贴板。

<a id="how-it-works"></a>

## 工作原理

该扩展会在[故事编辑器](../books/story-editor.md)的设置菜单（⚙）中添加 **Export instructions** 项。
它会遍历当前分支的各个部分，从第一部分到故事当前的结尾，收集每个部分生成时所用的字段，
并将它们复制为一段文本。每个已填写的字段都会以其名称加冒号的形式写出，内容另起一行；各部分之间以空行分隔，顺序与故事一致：

![设置菜单中的导出指令](../../images/plugin-instructionexporter.png)

```
Scene Setting:
A harbour town at dawn
POV Character:
Mara
Present Characters:
Mara, the harbourmaster
Instructions:
Mara arrives and is questioned.

Scene Setting:
...
```

空字段会被省略，没有任何已填写字段的部分也会被省略。可将文本粘贴到任何地方：例如用来保存书籍的情节大纲，在另一本书中复用这些指令，或分享给他人。

<a id="usage"></a>

## 使用方法

无需任何配置。打开一本书，在设置菜单中选择*导出指令*；通知会确认文本已复制到剪贴板。如果当前分支中这些字段都未填写，则不会复制任何内容，并会提示你。

只会导出当前分支——若要导出其他分支，请先切换到该分支。

!!!
浏览器只允许页面在通过 `https://` 或 `localhost` 打开时写入剪贴板。若通过普通 `http://` 访问其他地址，该项将不起作用。
!!!
