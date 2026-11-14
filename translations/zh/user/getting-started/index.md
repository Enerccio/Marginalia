---
label: 入门指南
order: 990
icon: rocket
---

# 入门指南

Marginalia 在浏览器中运行。你可以通过两种方式安装：

| | [桌面应用](desktop-app.md) | [服务器（Docker）](docker-server.md) |
|---|---|---|
| 适合 | 一台电脑上的一个人 | 多个用户，或从其他设备访问 |
| 安装 | 下载并运行，无需其他安装 | Docker 和 Docker Compose |
| 地址 | `http://127.0.0.1:8765`（仅限你的电脑） | 端口 `8080`，通常位于带 HTTPS 的反向代理之后 |
| 数据 | `~/.marginalia` | `docker-compose.yml` 旁边的 `./data` |

两个版本是同一个应用，功能相同：书籍、设定集、用户、备份和扩展。
如果想用手机写作，请运行服务器版本，并在手机浏览器中打开它：参见
[在手机上使用 Marginalia](../phone.md)。

Marginalia 没有自带的模型。它需要一个**兼容 OpenAI 的 API** 来生成文本：可以是云服务（OpenAI、OpenRouter……），也可以是运行在你自己电脑上的模型（llama.cpp、LM Studio、Ollama、vLLM……）。请提前准备好 API 地址和 API 密钥（不需要密钥的本地服务器也可以直接使用，参见[添加提供方](../inference-providers.md#adding-a-provider)）。

<a id="steps"></a>

## 步骤

1. 安装 Marginalia：[桌面应用](desktop-app.md)或[服务器](docker-server.md)。
2. 首次启动时[创建管理员账户](first-start.md)。
3. 按照[快速入门](quick-start.md)操作：连接模型、创建写作协议，并写出一本书的第一部分。
4. 可选：打开[示例书籍](example-book.md)，这是一部完成的作品，包含摘要、批注和译文，可以看到一本长篇书籍的样子。
