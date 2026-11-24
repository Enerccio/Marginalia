---
label: Corrección de herramientas de Gemma 4
order: 15
---

# Corrección de herramientas de Gemma 4

Hace que las llamadas a funciones funcionen con los modelos Gemma 4 cuya API espera otro rol para los resultados de las llamadas.

!!!warning No necesaria con una plantilla de chat actual
Cárgala solo para un modelo Gemma 4 antiguo. La plantilla de chat de Gemma 4 de Google lee los resultados con el rol `tool`, el que envía Marginalia, desde abril de 2026 (todos los GGUF posteriores lo tienen). Con una plantilla así, la extensión empeora el prompt: el resultado llega como un turno extra que la plantilla no conoce. llama.cpp también ignora la forma antigua, así que allí el rol `tool` es el único que funciona.

Un GGUF antiguo conserva la plantilla con la que se creó. Para comprobar la tuya, genera una llamada a función y su resultado con `/apply-template` de llama.cpp y busca `<|tool_response>` en el turno del modelo: si está, no cargues la extensión. Un modelo que escribe sus llamadas como texto en lugar de hacerlas se ayuda con la *Function call rule* de los [ajustes del modelo](../inference-providers.md).
!!!

<a id="how-it-works"></a>

## Cómo funciona

Cuando el modelo llama a una función, Marginalia le envía el resultado en un mensaje con el rol `tool`. Algunos servidores que ejecutan Gemma 4 esperan en su lugar el rol `tool_responses`, y en otro caso responden con un error o ignoran el resultado.

La extensión cambia el rol para esos modelos: cuando el *Model* de un [proveedor de inferencia](../inference-providers.md) contiene `gemma4` o `gemma-4` (sin distinguir mayúsculas de minúsculas), los resultados de las llamadas a funciones a ese modelo se envían como `tool_responses`. Las solicitudes a otros modelos no se modifican. Es un port de la extensión de SillyTavern [SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix).

<a id="usage"></a>

## Uso

No hay nada que configurar. Carga la extensión en *Admin → Extensions*; se aplica a la siguiente solicitud.
