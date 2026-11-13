---
label: 首页
icon: home
order: 1000
---

# Marginalia

Marginalia 是一个自托管的写作工具，用于与大型语言模型共同创作长篇小说。你为故事的每一部分编写指令，模型负责撰写正文，Marginalia 则将其周围的一切有序地组织起来：分支故事、世界设定（lore）、让长篇内容能够容纳在模型上下文中的摘要，以及塑造文风的提示设置。

它在浏览器中运行——可以是你自己电脑上的桌面应用，也可以是供你和朋友使用的小型服务器——并且兼容任何 OpenAI 兼容的 API：OpenAI、OpenRouter、LiteLLM、llama.cpp、LM Studio、Ollama、vLLM 以及其他服务。

![故事编辑器](images/hero-story-editor.png)

[!button variant="primary" text="开始使用"](user/getting-started/index.md)
[!button text="下载"](https://github.com/Enerccio/Marginalia/releases)
[!button text="在 GitHub 上查看源码"](https://github.com/Enerccio/Marginalia)

<a id="what-it-does"></a>

## 功能概览

- **带分支故事的书籍** – 重新生成或滑动切换某一部分，从任意位置分支，在故事树中移动，并编辑生成的文本。参见 [书籍](user/books/index.md)。
- **设定集（Lorebook）** – 世界和角色笔记只在真正相关时才会放入提示中：依据书籍标签、文本或正则表达式过滤，支持嵌套设定集。可以导入 SillyTavern 的世界信息。参见 [设定集](user/lorebooks/index.md)。
- **模板与宏** – 提示词是带有 SillyTavern 兼容宏的 Handlebars 模板，因此导入的设定依然可用。参见 [模板与宏](user/templates/index.md)。
- **摘要** – 较早的部分会被压缩，使长篇书籍能够装入上下文窗口。参见 [摘要](user/books/summaries.md)。
- **任意 OpenAI 兼容模型** – 包括推理模型，支持按模型设置限制，并可切换生成设置。参见 [推理服务提供商](user/inference-providers.md) 和 [协议](user/protocols.md)。
- **备份** – 按书籍备份，可恢复和复制；数据库可按计划进行完整备份。参见 [书籍备份](user/books/backups.md) 和 [数据库备份](user/administration/database-backups.md)。
- **多用户** – 每位用户拥有各自的书籍、设定和模型；管理员负责管理用户、备份和清理。参见 [管理](user/administration/index.md)。
- **已发布的书籍** – 一个可以分享的只读阅读视图。参见 [发布与阅读器](user/books/publishing.md)。
- **搜索替换与命令** – 在故事和设定集中搜索替换，故事菜单旁边有一个命令行，可用命令读取和修改书籍。参见 [命令](user/commands/index.md) 和 [正则表达式](user/regular-expressions.md)。
- **代理（Agents）** – 一个模型通过命令独立处理书籍，操作的是副本：你可以观察它、在控制台中与它对话，并在其修改进入书籍之前进行审阅。参见 [代理](user/agents.md)。
- **扩展** – 插件在运行时添加功能：章节标记、设定集历史、AI 审阅、向模型提出附带问题等。参见 [扩展](user/extensions/index.md)。

<a id="which-guide-do-you-need"></a>

## 你需要哪份指南？

|                                           |                                                                                                                                   |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **[用户指南](user/index.md)**           | 你想安装 Marginalia、为他人运行它，或用它写作。从 [入门指南](user/getting-started/index.md) 开始。 |
| **[开发者指南](developer/index.md)** | 你想从源码构建 Marginalia、修改它、编写扩展，或了解其内部工作原理。                       |

<a id="quick-links"></a>

## 快速链接

| 我想要……                   |                                                          |
|--------------------------------|----------------------------------------------------------|
| 在我的电脑上安装 | [桌面应用](user/getting-started/desktop-app.md)       |
| 在服务器上运行 | [服务器（Docker）](user/getting-started/docker-server.md) |
| 连接模型 | [推理服务提供商](user/inference-providers.md)       |
| 写一本书的第一部分 | [快速开始](user/getting-started/quick-start.md)       |
| 使用我的 SillyTavern 设定集 | [导入与导出](user/lorebooks/import-export.md)       |
| 解决问题 | [故障排除与常见问题](user/troubleshooting.md)         |
| 编写扩展 | [插件开发](developer/plugins/index.md)         |
| 报告错误或参与贡献 | [参与贡献](developer/contributing.md)                |

<a id="about"></a>

## 关于

Marginalia 是开源软件，采用 [MIT 许可证](https://github.com/Enerccio/Marginalia/blob/master/LICENSE)。
本手册截图中使用的图片署名见 [致谢](credits.md) 页面。
发行版位于 [GitHub](https://github.com/Enerccio/Marginalia/releases)；错误报告和想法请提交至 [问题跟踪器](https://github.com/Enerccio/Marginalia/issues)。
