---
label: Corrección de herramientas de Gemma 4
order: 15
---

# Corrección de herramientas de Gemma 4

Hace que la llamada a funciones funcione con los modelos Gemma 4 cuya API espera otro rol para los resultados de las llamadas.

<a id="how-it-works"></a>

## Funcionamiento

Cuando el modelo llama a una función, Marginalia le envía el resultado en un mensaje con el rol `tool`. Algunos
servidores que ejecutan Gemma 4 esperan el rol `tool_responses` en su lugar, y responden con un error o ignoran el resultado en caso contrario.

La extensión cambia el rol para esos modelos: cuando el *Modelo* de un [proveedor de inferencia](../inference-providers.md) contiene `gemma4` o
`gemma-4` (en cualquier combinación de mayúsculas y minúsculas), los resultados de las llamadas a funciones a ese modelo se envían como `tool_responses`. Las peticiones a otros
modelos no se modifican. Es un port de la extensión de SillyTavern
[SillyTavern-Gemma4-Tools-Fix](https://github.com/Enerccio/SillyTavern-Gemma4-Tools-Fix).

<a id="usage"></a>

## Uso

No hay nada que configurar. Carga la extensión en *Admin → Extensiones*; se aplicará a la siguiente petición.
