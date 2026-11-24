---
label: Gemma 4 工具调用修复
order: 15
---

# Gemma 4 工具调用修复

让函数调用可以与 Gemma 4 模型配合使用，适用于那些 API 要求函数调用结果使用另一种角色的模型。

!!!warning 使用当前聊天模板时无需此扩展
仅在旧版 Gemma 4 模型上加载它。Google 的 Gemma 4 聊天模板使用角色 `tool` 读取结果，这正是 Marginalia 自 2026 年 4 月起发送的角色（之后的每个 GGUF 都包含此模板）。对于这种模板，本扩展会让提示词变差：结果会以一个额外的轮次出现，而模板并不识别这种轮次。llama.cpp 同样会忽略旧的形式，因此在那里只有角色 `tool` 有效。

旧的 GGUF 会保留其制作时使用的模板。要检查你的模型，请用 llama.cpp 的 `/apply-template` 渲染一次函数调用及其结果，然后在模型轮次中查找 `<|tool_response>`：如果找到了，就不要加载此扩展。如果模型是以文本而不是实际调用的方式写出函数调用，则应改用[模型设置](../inference-providers.md)中的 *Function call rule* 来解决。
!!!

<a id="how-it-works"></a>

## 工作原理

当模型调用函数时，Marginalia 会以角色 `tool` 的消息将结果发回给它。一些运行 Gemma 4 的服务器需要角色 `tool_responses`，否则会返回错误或忽略结果。

对于这类模型，本扩展会改变角色：当[推理提供方](../inference-providers.md)的 *Model* 包含 `gemma4` 或 `gemma-4`（不区分大小写）时，函数调用的结果会以角色 `tool_responses` 发送。发往其他模型的请求不受影响。本扩展移植自 SillyTavern 扩展 [SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix)。

<a id="usage"></a>

## 使用方法

无需任何配置。在 *Admin → Extensions* 中加载扩展即可，它会在下一次请求时生效。
