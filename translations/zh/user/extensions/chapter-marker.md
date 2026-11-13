---
label: 章节标记
order: 20
---

# 章节标记

将[故事编辑器](../books/story-editor.md)中的**大纲**转换为目录。

![大纲中的章节](../../images/plugin-chaptermarker.png)

<a id="how-it-works"></a>

## 工作原理

当某个片段的文本中包含 Markdown 标题（即以 `#` 开头的一行）时，该片段在大纲中的条目会以高亮的标题代替通常的 `#n (DB ID: m)` 标签显示：

```
# Chapter 3: The Harbour

Rain had been falling on Ostra since dawn...
```

在大纲中会显示为 **5. Chapter 3: The Harbour**——编号是该片段在分支中的位置，后面跟着标题。没有标题的片段保留其正常标签。

- 标题可以位于片段的任意一行；如有多个标题，使用第一个。
- 任何标题级别都可以使用（`#`、`##`、`###`……）。
- 您一旦保存[编辑](../books/story-editor.md#editing-the-text)，大纲就会立即更新：添加标题即可开始新章节，删除标题即可将该片段合并回上一部分。

<a id="usage"></a>

## 使用方法

无需进行任何配置。您可以自己在片段中写入标题，也可以在指令中要求模型生成，例如：*Start a new chapter titled "The Harbour" with a Markdown heading.*

!!!
默认的[用户提示词](../books/prompts.md#user-prompt)会告诉模型不要编写标题或章节标题。
如果您希望模型编写它们，请在指令中明确说明，或修改用户提示词中的该规则。
!!!

标题属于故事正文的一部分，因此它们也会被发送给模型，并显示在[阅读器](../books/publishing.md)和备份中。

同样的规则也用于查找[导出书籍](../books/export.md#table-of-contents)目录中的章节。
