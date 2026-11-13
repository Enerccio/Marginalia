---
label: Prompts del libro
order: 70
---

# Prompts del libro

La pestaña **Prompts** de un libro contiene los textos con los que Marginalia construye el prompt: dos plantillas, los ajustes narrativos y los prompts de resumen y de reescritura. Deciden el tono del libro mucho más que los ajustes del modelo.

![La pestaña Prompts](../../images/book-prompts.png)

<a id="defaults-and-book-values"></a>

## Valores predeterminados y valores del libro

Cada campo es opcional. Un campo vacío hereda su valor: de la [carpeta](folders.md) más cercana por encima del libro que lo defina; si no, del valor predeterminado de **Ajustes → Templates** (véase [Cuenta y ajustes](../account.md)); y si también está vacío, del valor predeterminado integrado, en el [idioma de los prompts](../account.md#prompt-language) de tus ajustes. El valor heredado aparece en gris en el campo vacío.

Así, define en *Ajustes* lo que comparten todos tus libros, en la carpeta lo que comparte una serie, y en la pestaña *Prompts* solo lo que es propio de un libro. Los cambios se guardan mientras escribes.

<a id="inherited-settings"></a>

### Ajustes heredados

Debajo de un campo vacío, una línea indica de dónde procede su valor, para que se entienda por qué el libro escribe como lo hace:

- *Heredado de la carpeta "Sci-fi"* - una [carpeta](folders.md#where-the-settings-of-a-book-come-from) define el valor (se indica el nombre de la más cercana por encima del libro),
- *Heredado de tus ajustes* - el valor de *Ajustes → Templates*,
- *Valor predeterminado integrado* - nada define el valor.

![La pestaña Prompts de un libro dentro de una carpeta](../../images/book-prompts-inherited.png)

Los mismos campos aparecen en los ajustes de una carpeta, con las mismas líneas. Un texto en blanco cuenta como no definido. Como la búsqueda se hace al construir el prompt, mover el libro a otra carpeta, o cambiar la carpeta, modifica la siguiente parte que generes.

<a id="settings-of-extensions"></a>

### Ajustes de extensiones

Una [extensión](../extensions/index.md) puede añadir ajustes que libros y carpetas heredan de la misma forma (por ejemplo, el medio que una conversación sobre el libro considera que es). Están en una sección **Ajustes de la extensión**, debajo de los prompts, tanto en la pestaña *Prompts* de un libro como en los ajustes de una carpeta, con un **panel plegable por extensión** (todos cerrados, excepto el primero que tenga algo definido, para que no quede oculto un valor que hayas introducido). Funcionan como los campos anteriores: un campo vacío hereda el valor de las carpetas por encima del libro, después de los ajustes propios de la extensión en *Ajustes → Ajustes de la extensión*, y después de su valor predeterminado; la línea bajo el campo indica cuál es. Las plantillas de los [formatos de exportación](export.md#title-page-template) (portada, páginas preliminares y declaración de autoría) están en la misma sección, con un panel por formato. Un valor que definas para una extensión se mantiene en el libro o en la carpeta aunque la extensión no esté cargada.

Cada campo tiene un popover de sugerencias con **Insertar la plantilla predeterminada**, que copia el valor predeterminado en el campo como punto de partida para tu versión. Los campos de plantilla también muestran las **Variables de plantilla disponibles** y los **Macros disponibles**.

![El popover de sugerencias de un campo de plantilla](../../images/template-hints.png)

<a id="the-fields"></a>

## Los campos

| Campo | Se usa en | |
|---|---|---|
| **Plantilla principal** | mensaje de sistema | El marco del prompt: el papel del modelo, y dónde van el trasfondo, los ajustes narrativos y los resúmenes. Una plantilla. |
| **Punto de vista (POV)** | plantilla principal | La voz narrativa, p. ej. *Primera persona*, *Tercera persona limitada*. Texto plano. |
| **Tiempo verbal** | plantilla principal | P. ej. *Pretérito*, *Presente*. Texto plano. |
| **Estilo** | plantilla principal | Pautas de estilo y de tono para la prosa. Texto plano. |
| **Prompt de usuario predeterminado** | último mensaje | La petición de la siguiente parte, construida a partir de las [instrucciones del turno](story-editor.md#writing-the-next-part). Una plantilla. |
| **Prompt de resumen predeterminado** | resúmenes | La petición de un [resumen](summaries.md). Una plantilla. |
| **Prompt de metarresumen predeterminado** | metarresúmenes | La petición de un [metarresumen](summaries.md#meta-summaries), que fusiona resúmenes. Una plantilla. |
| **Redraft System Prompt** | reescrituras | El mensaje de sistema de una [reescritura](story-editor.md#redrafting-a-part): las directrices, la narración y los resúmenes. Una plantilla. |
| **Redraft Prompt** | reescrituras | El último mensaje de una reescritura, la petición en sí: el trasfondo que dan ahora los lorebooks para la parte (así, una edición de una entrada llega a la reescritura; el trasfondo con el que se escribió la parte solo se usa cuando el libro no tiene lorebook), la historia posterior a la parte, la parte y las instrucciones. Una plantilla. |
| **Prompt para reescribir más corto**, **Prompt para reescribir más largo** | reescrituras | La frase que se añade tras tus instrucciones cuando la ventana de [reescritura](story-editor.md#redrafting-a-part) está en **Más corto** o **Más largo**. Texto plano, no una plantilla. Vacío es el texto integrado. |
| **Prompt del sistema de anotación** | anotaciones | El mensaje de sistema de una petición a un modelo para [anotar](annotations.md#letting-a-model-annotate) una parte. Una plantilla. |
| **Prompt de anotación** | anotaciones | El último mensaje de la petición de anotar. Una plantilla. |
| **Annotate Instructions** (*Qué buscar*) | anotaciones | Lo que el modelo busca al anotar (*marcar errores de continuidad*, *diálogos flojos*), el texto de `{%{{{annotateInstructions}}}%}` del prompt de anotación. Texto plano. Defínelo aquí, en una carpeta o en *Ajustes → Templates*, y la ventana *Anotar con IA* empieza con él. |
| **Prompt de suplantación** | instrucciones de redacción | La petición a un modelo para [redactar las instrucciones](story-editor.md#drafting-the-instructions) del siguiente turno en tu lugar. Una plantilla. |

*Punto de vista*, *Tiempo verbal* y *Estilo* se insertan tal cual en la plantilla principal; los macros no funcionan en ellos.

Una plantilla con un error de sintaxis no se guarda; un aviso muestra el error.

<a id="templates"></a>

## Plantillas

Las plantillas son texto con marcadores entre llaves dobles. Cada plantilla tiene sus propias variables:

| Plantilla | Variables |
|---|---|
| Plantilla principal | `{%{{{backgroundLore}}}%}` (entradas activadas del lorebook), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, `{%{{{summaries}}}%}`, `{%{{{annotations}}}%}` (las notas que diste al modelo) |
| Prompt de usuario | `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}` |
| Prompt de resumen | `{%{{{backgroundLore}}}%}` (el trasfondo que dan ahora los lorebooks para la parte), `{%{{{text}}}%}` (las partes que resumir) |
| Prompt de metarresumen | `{%{{{backgroundLore}}}%}` (como arriba), `{%{{{summaryBlocks}}}%}` (los resúmenes que fusionar) |
| Redraft System Prompt, Redraft Prompt | `{%{{{text}}}%}` (la parte), `{%{{{redraftInstructions}}}%}` (lo que quieres cambiar), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{followingStory}}}%}`, `{%{{{backgroundLore}}}%}`, `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, y el turno con el que se escribió la parte: `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}`, y `{%{{{annotations}}}%}` (tus notas abiertas sobre la parte, vacías salvo que las pidas en la ventana de reescritura), y, para la [reescritura de una selección](story-editor.md#redrafting-only-a-passage), `{%{{{selection}}}%}` (el texto seleccionado; vacío cuando se reescribe la parte entera), `{%{{{selectionBefore}}}%}` y `{%{{{selectionAfter}}}%}` (el texto de la parte alrededor de ella) |
| Prompt de suplantación | `{%{{{impersonateInstructions}}}%}` (tu orientación), `{%{{{outline}}}%}` (el plan de la historia que pasa quien llama, p. ej. el capítulo actual del Arc Outline), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{backgroundLore}}}%}` (el trasfondo de la última parte), `{%{{{instructionExamples}}}%}` (tus instrucciones de las tres últimas partes, de la más antigua a la más reciente), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, y el turno para el que son las instrucciones: `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}` |
| Annotate System Prompt, Annotate Prompt | Las variables de los prompts de reescritura, con `{%{{{annotateInstructions}}}%}` (lo que quieres que se revise) en lugar de `{%{{{redraftInstructions}}}%}`, `{%{{{annotations}}}%}` (las notas que ya tiene la parte), `{%{{{facts}}}%}` (lo que las extensiones saben de la historia en esa parte, por ejemplo qué saben los personajes; vacío sin una extensión de este tipo) y `{%{{{outputFormat}}}%}` (cómo se le indica al modelo que responda) |

Una sección imprime su contenido solo cuando la variable no está vacía, así el prompt no tiene encabezados vacíos:

{%{
```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```
}%}

`{%{{{.}}}%}` dentro de una sección es el valor de la variable. Con la *Ambientación de la escena* vacía, se omite la línea completa.

Todas las plantillas también pueden usar los **macros** compatibles con SillyTavern, como `{%{{{user}}}%}` (el personaje desde cuyo punto de vista se narra), `{%{{{description}}}%}` (la descripción del libro), `{%{{{random::a::b}}}%}`, `{%{{{getvar::name}}}%}` y `{%{{{setvar::name::value}}}%}`. Consulta [Plantillas y macros](../templates/index.md) para la sintaxis completa y la referencia de macros.

<a id="master-template"></a>

## Plantilla principal

La plantilla principal se convierte en el **mensaje de sistema**, el primer mensaje del prompt. El valor predeterminado integrado tiene estos bloques:

1. **Papel del sistema** - el modelo es un autor que continúa la historia.
2. **Contexto del mundo y del trasfondo** - las entradas activadas del [lorebook](../lorebooks/index.md) (`backgroundLore`).
3. **Estilo de escritura y mecánica** - punto de vista, tiempo verbal, estilo y reglas generales de prosa.
4. **Resumen de la historia hasta ahora** - los [resúmenes](summaries.md).
5. **Notas del autor sobre la historia** - solo cuando diste [notas al modelo](annotations.md#giving-a-note-to-the-model).

Si el [proveedor de inferencia](../inference-providers.md#general-settings) tiene activado un prompt de *jailbreak*, se coloca antes de la plantilla principal.

<a id="user-prompt"></a>

## Prompt de usuario

El prompt de usuario es el **último mensaje** del prompt: la petición de la parte que hay que escribir. Se rellena con las *Instrucciones del nuevo turno* (o con los *Detalles del turno* en *Regenerar* y *Variante*). El valor predeterminado integrado pide la siguiente sección con el personaje desde cuyo punto de vista se narra, la ubicación, los personajes presentes y las instrucciones de la trama, seguidos de reglas: no añadir personajes nuevos salvo que se pidan, seguir las instrucciones, mantener la continuidad y devolver solo el texto de la historia.

Las entradas del lorebook con el modo de inserción *Antes del prompt de usuario* se colocan justo antes de él; consulta [Entradas y activación](../lorebooks/entries.md).
<a id="redraft-prompt"></a>

## Prompt de redacción

La [reescritura](story-editor.md#redrafting-a-part) de una parte es una sola petición formada por dos prompts: el **Prompt del sistema de reescritura** es el mensaje de sistema, la historia anterior a la parte va detrás como los mensajes previos de la conversación (las partes como respuestas del modelo, tal como las envía una generación), y el **Prompt de reescritura** es el último mensaje del usuario. El prompt predeterminado incorporado indica al modelo que aplique tus instrucciones, que conserve lo que no pediste cambiar, que mantenga la coherencia con la historia anterior y posterior a la parte, que conserve la voz y que responda únicamente con la parte reescrita. Ambos prompts tienen las mismas variables; `{%{{{previousStory}}}%}` es solo una nota de que la historia viaja como mensajes. Deja en el prompt del sistema lo que se mantiene de una reescritura a otra, y en el último prompt lo que cambia (el lore, que no es igual para cada parte, la historia posterior a la parte, la parte y las instrucciones): así, un servidor que recuerda el prompt que ya leyó puede reutilizar gran parte de él. Tu propia versión del último prompt debe conservar las variables que necesita: sin `{%{{{text}}}%}` el modelo no ve la parte. Cada prompt sigue la regla de todos los prompts: el campo del libro, si no, el de *Ajustes → Plantillas*, si no, el incorporado.

Cuando reescribes solo un [fragmento seleccionado](story-editor.md#redrafting-only-a-passage), se usa el mismo prompt con `{%{{{selection}}}%}` rellenado: el prompt predeterminado incorporado muestra entonces la parte con el fragmento entre `[[PASSAGE TO REDRAFT]]` y `[[/PASSAGE TO REDRAFT]]` e indica al modelo que responda solo con el texto que reemplaza el fragmento. Un prompt propio que no conoce `{%{{{selection}}}%}` hace que el modelo responda con la parte completa, y esa respuesta completa reemplaza la selección; añade un bloque `{%{{#selection}}%}...{%{{{/selection}}}%}` (y `{%{{{^selection}}}%}` para el otro caso) para tratarlo.

<a id="redraft-shorter-and-longer"></a>

## Reescribir más corto y más largo

La opción **Extensión** de la ventana de [reescritura](story-editor.md#redrafting-a-part) (**La misma extensión**, **Más corto**, **Más largo**) no modifica los prompts de reescritura: para **Más corto**, el texto de **Prompt para reescribir más corto** (para **Más largo**, el de **Prompt de reescritura más largo**) se añade después de lo que escribiste en *Instrucciones para la reescritura*, en el lugar de `{%{{{redraftInstructions}}}%}`, de modo que también funciona con un prompt de reescritura propio. Si las instrucciones están vacías, es la única instrucción. Los dos textos siguen la regla de todos los prompts: el campo del libro, si no, el de la carpeta más cercana por encima que lo defina, si no, el de *Ajustes → Plantillas*, si no, el texto incorporado, que pide entre un tercio y la mitad menos de extensión (más: la mitad más, hasta el doble de largo) sin cambiar lo que ocurre.

<a id="annotate-prompt"></a>

## Prompt de anotación

Cuando un modelo [anota una parte](annotations.md#letting-a-model-annotate) recibe el mismo tipo de petición que una reescritura: el **Prompt del sistema de anotación** como mensaje de sistema, la historia anterior a la parte como mensajes previos y el **Prompt de anotación** como último mensaje. El prompt predeterminado incorporado convierte al modelo en un editor: encuentra los pasajes que tienen un problema según tus instrucciones, cita cada uno palabra por palabra y escribe una nota breve para ti, sin elogios ni relleno y sin repetir las notas que la parte ya tiene. Cuando una extensión aporta [datos sobre la historia](annotations.md#letting-a-model-annotate) (el Knowledge Tracker: qué saben los personajes), el prompt predeterminado los muestra como *Hechos establecidos* y el editor señala lo que los contradiga. El prompt tiene la variable `{%{{{outputFormat}}}%}`, donde se describe la forma de responder (JSON, bloques de texto o una revisión libre, según el formato de respuesta que elijas); consérvala en un prompt propio, o el modelo no sabrá cómo responder. Ambos prompts de anotación siguen la regla de todos los prompts: el campo del libro, si no, el de *Ajustes → Plantillas*, si no, el incorporado.

**Qué buscar** (*Instrucciones de anotación*) sigue la misma regla, así que puedes configurarlo una vez: en *Ajustes → Plantillas* para todos tus libros, en una carpeta para los libros que contiene, o en la pestaña *Prompts* para un solo libro. La ventana *Anotar con IA* muestra lo que se aplica como texto gris en su campo e indica de dónde viene; deja el campo vacío para usarlo, escribe algo para reemplazarlo en esa ejecución, o haz clic en el icono de información del campo y en **Fill default** para copiar lo que se aplica al campo y editarlo para esa ejecución. Si no hay nada configurado en ningún sitio, se usa la tarea predeterminada del prompt. Lo mismo vale para la anotación automática y para el comando `/generate-annotation`.

<a id="impersonate-prompt"></a>

## Prompt de suplantación

Cuando un modelo [redacta las instrucciones](story-editor.md#drafting-the-instructions) del siguiente turno recibe una sola petición, enviada como único mensaje, igual que la reescritura. El prompt predeterminado incorporado pide al modelo que escriba lo que escribirías tú en *Instrucciones*: una especificación de la trama de la siguiente parte (qué ocurre, quién hace qué, dónde se detiene la parte) que desarrolla tu indicación y sigue el plan, continúa la historia desde donde terminó, está escrita como tus instrucciones anteriores y no es la prosa en sí. Sin indicación, continúa la historia por donde lleve. Tu indicación es donde el prompt coloca `{%{{{impersonateInstructions}}}%}`; consérvala en un prompt propio. El prompt de suplantación sigue la regla de todos los prompts: el campo del libro, si no, el *Prompt de suplantación* de *Ajustes → Plantillas*, si no, el incorporado.

<a id="how-the-prompt-is-put-together"></a>

## Cómo se compone el prompt

```
system     jailbreak (si está activado) + plantilla maestra
user       [ Generate story. ]
assistant  parte #1
user       [ Generate more story. ]
assistant  parte #2
...        (tantas partes anteriores como quepan)
user       lore "Before User Prompt" + prompt del usuario
```

Las partes anteriores se envían como respuestas propias del modelo, así que las continúa con la misma voz. Cuando la historia es más larga que el [límite de contexto](../protocols.md#limits), se dejan fuera primero las partes más antiguas; los [resúmenes](summaries.md) mantienen esas partes en la historia en forma condensada.

**Mostrar el prompt** en el menú de cualquier parte muestra el prompt exacto desde el que se escribió esa parte. **Comparar prompt** lo coloca junto al prompt de una parte anterior y muestra dónde difieren por primera vez, que es donde termina la [caché de prompts](../prompt-cache.md#compare-prompt) de un servidor.

<a id="tips"></a>

## Consejos

- Pon las reglas que valen para cada parte (no introducir personajes nuevos, extensión, formato) en el prompt del usuario: al ser el último mensaje, tiene el efecto más fuerte.
- Pon los datos duraderos sobre el mundo y los personajes en el [lorebook](../lorebooks/index.md), no en *Estilo*.
- Pide una extensión en las instrucciones («unas 800 palabras») en lugar de confiar en el límite de respuesta, que corta el texto.
- Después de cambiar una plantilla, genera una parte y revísala con **Mostrar el prompt**.
