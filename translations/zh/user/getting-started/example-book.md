---
label: 示例书
order: 80
---

# 示例书

*Unregistered* 是一部已经完成的书，可以打开、阅读和拆解：约 100 个部分（约 99,000 字），讲述同一个故事，并保留了 Marginalia 各项功能留下的全部痕迹。它既是了解长篇书籍在 [故事编辑器](../books/story-editor.md)、[摘要](../books/summaries.md)、[批注](../books/annotations.md)和[统计](../books/statistics.md)中呈现方式的好例子，也可以在现成的内容上试用各个扩展。

> 一位学会日语的捷克地窖导游（他的日语来自小说）和一位度假的日本护士，穿过布拉格地窖里一道被砖封住的拱门，醒来时身处一个由说 Vrakht 语的帝国统治的河谷。
> 两人都不懂当地语言，而一位人口税评估员发现，这两个人不在他的名册上。

<a id="what-is-in-it"></a>

## 内容概览

| | |
|---|---|
| **故事** | 主线 100 个部分，外加一条 3 个部分的支线，共 32 章（章节标题写在正文中）。 |
| **摘要** | 每章一份摘要，长度不一（每 25 个部分的最后一章保持完整，因为模型是按原文读取的），以及合并最早摘要的元摘要，采用默认[摘要提示词](../books/summaries.md)的紧凑格式。 |
| **批注** | 约 450 条审阅笔记（连贯性、文风、设定、视角、节奏），由本书自己的 *Annotate with AI* 提出，重写时据此修改；每个部分也都有各自的版本（共 360 个，见[版本历史](../books/version-history.md)）。 |
| **Lorebook** | 31 个条目：世界、地点、人物和语言。 |
| **提示词** | 每个部分都保留了撰写它时使用的提示词，以及被激活的 lore：在部分的菜单中使用 **Show Prompt**、**Compare Prompt** 和 **Lore Usage**（[书籍提示词](../books/prompts.md)）。 |
| **Knowledge Tracker** | 七个被追踪的角色，以及每个角色逐部分的已知与未知信息（[Knowledge Tracker](../extensions/knowledge-tracker.md)）。 |
| **译文对话** | 约 1,250 段译文：帝国的 **Vrakht** 语和河谷的 **Aethel-Vael** 语对话（[Conlang Translator](../extensions/conlang-translator.md)），以及**日语**和**捷克语**（[Real Language Translator](../extensions/real-language-translator.md)）。 |
| **章节** | 由 [Arc Outline](../extensions/arc-outline.md) 根据正文生成的故事大纲。 |
| **插图** | 一幅插图，位于第 31 部分。 |

四个扩展的数据都保存在书中，但只有加载了这些扩展时才能看到；未加载时，数据会被保留但被忽略。这本书不包含模型或协议：导入后，请在书籍的设置中选择你自己的模型和协议。

<a id="download"></a>

## 下载

这些文件附在发布版本 [example-book-1](https://github.com/Enerccio/Marginalia/releases/tag/example-book-1) 中：

| 文件 | |
|---|---|
| [`unregistered-book.zip`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-book.zip)（5.7 MB） | 书籍本身：包含插图的备份文件。 |
| [`unregistered-lorebook.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/unregistered-lorebook.json) | 书籍的 Lorebook 单独文件。书籍文件中也包含它；只有在不导入整本书的情况下想查看 Lorebook 时，才需要这个文件。 |
| [`vrakht.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/vrakht.json) | 帝国的人造语言，供 Conlang Translator 使用。 |
| [`aethel-vael.json`](https://github.com/Enerccio/Marginalia/releases/download/example-book-1/aethel-vael.json) | 河谷的人造语言，供 Conlang Translator 使用。 |

<a id="importing-it"></a>

## 导入

1. **语言**（如果你有 Conlang Translator）：在 **Settings → Extension Settings → Conlang Translator** 中点击 **Import**，选择 `vrakht.json`，然后对 `aethel-vael.json` 再做一次（[Conlang Translator](../extensions/conlang-translator.md#languages)）。请先导入语言，再导入书籍，这样书籍的语言设置才有可指向的对象。
2. **书籍：**在书籍窗口的 *Settings* 标签页中点击 **Import Backup as New Book**，输入名称，然后上传 `unregistered-book.zip`（[备份的导入方式](../books/backups.md#importing-a-backup-as-a-new-book)）。当 Marginalia 询问 lorebook 时，选择 **Import**：它就在该文件中。
3. 打开新书，并在其设置中选择你的模型和协议。

译文保存在书中，因此 Vrakht、Aethel-Vael、日语和捷克语的对话会按原译文显示。如果想用 Real Language Translator 翻译更多内容，请在那里创建 *Japanese* 和 *Czech* 语言（插件提供了预设），因为该插件不会随书一起导出其语言。

<a id="where-it-comes-from-and-the-licence"></a>

## 来源与许可

故事、Lorebook 和两种语言都是原创的。大纲、Lorebook、语言以及各部分的规划由 Marginalia 的作者撰写；各部分的正文则是通过 Marginalia 自身的生成循环完成的，**由 AI 模型撰写**（Claude Haiku）：生成、批注、重写、追踪知识、翻译、摘要。这是对该程序的演示，而不是一部出版的小说，它带有模型文笔特有的小毛病。

- 正文、Lorebook 和两种语言以 [CC0 1.0](https://creativecommons.org/publicdomain/zero/1.0/) 形式捐献给公共领域：可用于任何用途。
- 插图为 Willem Adriaan Alexander Liernur 于 1887 年创作的 *Interior of a Cottage in Scheveningen*，属于公共领域，来源为 [Rawpixel](https://www.rawpixel.com/image/13744617/image-face-person-art)。
