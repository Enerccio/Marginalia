---
label: Generador de nombre y descripción del libro
order: 21
---

# Generador de nombre y descripción del libro

Permite que el modelo escriba el **nombre** y la **descripción** de un libro a partir de su historia. Tú eliges qué parte de la historia lee, y copias lo que escribió en el libro.

<a id="opening-it"></a>

## Cómo abrirlo

Con la extensión instalada, la pestaña **Información** de la ventana del libro tiene al final un botón **Generate name and description**. También funciona en el [diseño para teléfono](../phone.md#extensions). El diálogo muestra las opciones a la izquierda y el resultado a la derecha.

![La ventana que genera el nombre y la descripción](../../images/plugin-book-description.png)

| Opción | |
|---|---|
| **Modelo**, **Protocolos** | Con qué se ejecuta la generación. Empieza con el modelo y el protocolo del libro; si no los tiene, con tus valores predeterminados de *Ajustes* (consulta [Cuenta y ajustes](../account.md)). La elección es solo para esta generación. Sin modelo, solo muestra una advertencia. |
| **Desde el mensaje**, **Hasta el mensaje** | El rango de la historia, en los números de los mensajes con texto de la rama activa. El inicio del mensaje se muestra debajo del campo, para que sepas cuál es. Empieza con toda la historia. |
| **Usar resúmenes** | Un [resumen](../books/summaries.md) sustituye a los mensajes que cubre, cuando todos están dentro del rango; así un rango largo sale mucho más barato. Un resumen que sale fuera del rango no se usa, tampoco uno que ya no describe la historia (en ese caso se usan los mensajes). Se recuerda para la próxima vez. |
| **Formato de la respuesta** | *Automático* pide JSON con un esquema cuando el servicio del modelo lo admite, y Markdown cuando no. *JSON* exige un esquema. *JSON sin esquema* describe el JSON en el prompt en su lugar, para un servidor que no admite esquema junto con el razonamiento del modelo. *Markdown* pide el nombre como primer título y la descripción debajo. Lo que escriba el modelo se lee como JSON o como Markdown, así que un modelo que ignore la instrucción también se entiende. Se recuerda para la próxima vez. |

Debajo de las opciones, un contador indica cuántos tokens ocupa el rango con el prompt, cuántos admite el modelo, y si la historia se envía tal cual o primero hay que convertirla en notas.

**Generar** inicia el modelo; el mismo botón lo detiene. El nombre y la descripción aparecen a la derecha, cada uno con su propio botón **Copy to book**: pone el texto en el campo de la pestaña Información y guarda el libro de inmediato (el título de la ventana del libro también cambia). Si la respuesta no puede leerse como un nombre y una descripción, el diálogo muestra el texto que escribió el modelo.

<a id="a-story-that-is-too-long"></a>

## Una historia demasiado larga

Cuando el rango supera lo que admite el modelo, la historia se divide en partes que caben, en orden, y el modelo escribe **notas** de cada una (aparecen debajo de *Notes* según llegan). La descripción se escribe entonces a partir de las notas en lugar de la historia. Las notas que siguen siendo demasiado largas se dividen y se anotan de nuevo, hasta cuatro rondas. Un único mensaje más grande que una parte se corta por un párrafo, una línea o una frase.

!!!
Si ni el modelo ni el protocolo tienen definido un tamaño de contexto, la extensión no puede saber qué es demasiado largo y siempre envía la historia tal cual.
!!!

## Prompts

Hay dos prompts: el que escribe el nombre y la descripción, y el que escribe las notas de una parte de una historia larga. Son [ajustes heredados](../books/prompts.md#inherited-settings): un libro usa los suyos; si no, la carpeta superior más cercana que defina uno; si no, los tuyos; si no, el texto integrado.

| Prompt | Variables |
|---|---|
| **Prompt de descripción** | `{{story}}` (la historia, o las notas hechas a partir de ella), `{{fromNotes}}`, `{{bookName}}`, `{{bookDescription}}` |
| **Prompt de notas** | `{{story}}` (la parte), `{{part}}`, `{{parts}}`, `{{bookName}}`, `{{bookDescription}}` |

Configúralos en la pestaña *Prompts* del libro, en *Ajustes de la extensión*, en el diálogo de una carpeta, o para todos tus libros en *Ajustes → Ajustes de la extensión → Book Description Generator*. Las instrucciones que indican al modelo cómo escribir la respuesta (JSON o Markdown) las añade la extensión después del prompt de descripción, así que tu prompt solo tiene que decir qué escribir.
