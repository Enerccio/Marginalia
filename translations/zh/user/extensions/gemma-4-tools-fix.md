---
label: Gemma 4 工具修复
order: 15
---

# Gemma 4 工具修复

让 Gemma 4 模型的函数调用正常工作。这些模型的 API 要求函数调用结果使用另一种角色。

<a id="how-it-works"></a>

## 工作原理

模型调用函数时,Marginalia 会把结果放在角色为 `tool` 的消息中发送回去。部分运行 Gemma 4 的服务器要求角色为 `tool_responses`,否则会返回错误或忽略这些结果。

该扩展会为这类模型更改角色:当 [推理提供方](../inference-providers.md) 的 *Model* 包含 `gemma4` 或 `gemma-4`(不区分大小写)时,发送给它的函数调用结果会使用 `tool_responses`。发送给其他模型的请求不受影响。本扩展移植自 SillyTavern 扩展 [SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix)。

<a id="usage"></a>

## 用法

无需任何配置。在 *Admin → Extensions* 中加载该扩展即可,它会从下一次请求开始生效。
