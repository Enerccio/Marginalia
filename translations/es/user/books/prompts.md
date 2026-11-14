---
label: Prompts del libro
order: 70
---

# Prompts del libro

La pestaña **Prompts** de un libro contiene los textos con los que Marginalia construye el prompt: dos plantillas, los ajustes narrativos y los prompts de resumen y de reescritura. Deciden el tono del libro mucho más que los ajustes del modelo.

![La pestaña Prompts](../../images/book-prompts.png)

<a id="defaults-and-book-values"></a>

## Predeterminados y valores del libro

Todos los campos son opcionales. Un campo vacío hereda su valor: de la [carpeta](folders.md) más cercana por encima del libro que lo defina; si no, del valor predeterminado en **Ajustes → Plantillas** (véase [Cuenta y ajustes](../account.md)); y si tampoco hay ninguno, del valor predeterminado integrado, en el [idioma de los prompts](../account.md#prompt-language) de tus ajustes. El valor heredado aparece en gris dentro del campo vacío.

Así, define en *Ajustes* lo que comparten todos tus libros, en la carpeta lo que comparte una serie, y en la pestaña *Prompts* del libro solo lo que es propio de ese libro. Los cambios se guardan mientras escribes.

<a id="inherited-settings"></a>

### Ajustes heredados

Bajo un campo vacío, una línea indica de dónde procede su valor, para que quede claro por qué el libro escribe como escribe:

- *Heredado de la carpeta "Sci-fi"* - una [carpeta](folders.md#where-the-settings-of-a-book-come-from) lo define (se indica el nombre de la más cercana por encima del libro),
- *Heredado de tus ajustes* - el valor de *Ajustes → Plantillas*,
- *Valor predeterminado integrado* - nada lo define.

![La pestaña Prompts de un libro dentro de una carpeta](../../images/book-prompts-inherited.png)

Los mismos campos están en los ajustes de una carpeta, con las mismas líneas. Un texto en blanco cuenta como no definido. Como la búsqueda se hace al construir el prompt, mover el libro a otra carpeta, o cambiar la carpeta, modifica la siguiente parte que generes.

<a id="settings-of-extensions"></a>

### Ajustes de extensiones

Una [extensión](../extensions/index.md) puede añadir ajustes que los libros y las carpetas heredan de la misma forma (por ejemplo, el medio en el que un chat sobre el libro lo trata). Están en una sección **Ajustes de la extensión** debajo de los prompts, en la pestaña *Prompts* de un libro y en los ajustes de una carpeta, con un **panel plegable por extensión** (todos cerrados, salvo el primero que tenga algún valor, para que no quede oculto un valor que hayas puesto). Funcionan como los campos anteriores: un campo vacío hereda de las carpetas por encima del libro, luego de los ajustes propios de la extensión en *Ajustes → Ajustes de la extensión*, y después de su valor predeterminado; la línea bajo el campo indica cuál es. Las plantillas de los [formatos de exportación](export.md#title-page-template) (portada, páginas preliminares y declaración de autoría) están en la misma sección, con un panel por formato. Un valor que definas para una extensión se conserva en el libro o en la carpeta aunque la extensión no esté cargada.

Cada campo tiene un menú de ayuda con **Insertar la plantilla predeterminada**, que copia el valor predeterminado en el campo como punto de partida para tu propia versión. Los campos de plantilla también muestran las **Variables de plantilla disponibles** y las **Macros disponibles**.

![El menú de ayuda de un campo de plantilla](../../images/template-hints.png)

<a id="the-fields"></a>

## Los campos

| Campo | Se usa en | |
|---|---|---|
| **Plantilla principal** | mensaje del sistema | El marco del prompt: el papel del modelo y el lugar donde van el trasfondo, los ajustes narrativos y los resúmenes. Una plantilla. |
| **Punto de vista (POV)** | plantilla principal | La voz narrativa, por ejemplo *Primera persona*, *Tercera persona limitada*. Texto plano. |
| **Tiempo verbal** | plantilla principal | Por ejemplo, *Pretérito*, *Presente*. Texto plano. |
| **Estilo** | plantilla principal | Pautas de estilo y de tono para la prosa. Texto plano. |
| **Prompt de usuario predeterminado** | último mensaje | La petición de la siguiente parte, construida a partir de las [instrucciones del turno](story-editor.md#writing-the-next-part). Una plantilla. |
| **Prompt de resumen predeterminado** | resúmenes | La petición de un [resumen](summaries.md). Una plantilla. |
| **Prompt de metarresumen predeterminado** | metarresúmenes | La petición de un [metarresumen](summaries.md#meta-summaries), que fusiona resúmenes. Una plantilla. |
| **Prompt del sistema de reescritura** | reescrituras | El mensaje del sistema de una [reescritura](story-editor.md#redrafting-a-part): las directrices, la narración y los resúmenes. Una plantilla. |
| **Prompt de reescritura** | reescrituras | El último mensaje de una reescritura, la petición en sí: el trasfondo que los libros de trasfondo dan ahora a la parte (así, una edición de una entrada llega a la reescritura; el trasfondo con el que se escribió la parte solo cuando el libro no tiene libro de trasfondo), la historia posterior a la parte, la parte y las instrucciones. Una plantilla. |
| **Prompt para reescribir más corto**, **Prompt para reescribir más largo** | reescrituras | La frase que se añade tras tus instrucciones cuando la ventana de [reescritura](story-editor.md#redrafting-a-part) está en **Más corto** o **Más largo**. Texto plano, no una plantilla. Vacío equivale al texto integrado. |
| **Prompt del sistema de anotación** | anotaciones | El mensaje del sistema de una petición para que un modelo [anote](annotations.md#letting-a-model-annotate) una parte. Una plantilla. |
| **Prompt de anotación** | anotaciones | El último mensaje de la petición de anotación. Una plantilla. |
| **Instrucciones de anotación** (*Qué buscar*) | anotaciones | Lo que el modelo busca al anotar (*marcar errores de continuidad*, *diálogos flojos*), el texto de `{%{{{annotateInstructions}}}%}` del prompt de anotación. Texto plano. Defínelo aquí, en una carpeta o en *Ajustes → Plantillas*, y la ventana *Anotar con IA* empieza con él. |
| **Prompt de suplantación** | instrucciones de borrador | La petición para que un modelo [redacte las instrucciones](story-editor.md#drafting-the-instructions) del siguiente turno en tu lugar. Una plantilla. |

*Punto de vista*, *Tiempo verbal* y *Estilo* se insertan en la plantilla principal tal cual; las macros no funcionan en ellos.

Una plantilla con un error de sintaxis no se guarda; una advertencia muestra el error.

<a id="templates"></a>

## Plantillas

Las plantillas son texto con marcadores entre llaves dobles. Cada plantilla tiene sus propias variables:

| Plantilla | Variables |
|---|---|
| Plantilla principal | `{%{{{backgroundLore}}}%}` (entradas de libros de trasfondo activadas), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, `{%{{{summaries}}}%}`, `{%{{{annotations}}}%}` (las notas que diste al modelo) |
| Prompt de usuario | `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}` |
| Prompt de resumen | `{%{{{backgroundLore}}}%}` (el trasfondo que los libros de trasfondo dan ahora a la parte), `{%{{{text}}}%}` (las partes que se resumen) |
| Prompt de metarresumen | `{%{{{backgroundLore}}}%}` (igual que arriba), `{%{{{summaryBlocks}}}%}` (los resúmenes que se fusionan) |
| Prompt del sistema de reescritura, Prompt de reescritura | `{%{{{text}}}%}` (la parte), `{%{{{redraftInstructions}}}%}` (lo que quieres cambiar), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{followingStory}}}%}`, `{%{{{backgroundLore}}}%}`, `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, y el turno desde el que se escribió la parte: `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}`, y `{%{{{annotations}}}%}` (tus notas abiertas sobre la parte, vacío salvo que las pidas en la ventana de reescritura), y para la [reescritura de una selección](story-editor.md#redrafting-only-a-passage) `{%{{{selection}}}%}` (el texto seleccionado; vacío cuando se reescribe la parte entera), `{%{{{selectionBefore}}}%}` y `{%{{{selectionAfter}}}%}` (el texto de la parte alrededor de ella) |
| Prompt de suplantación | `{%{{{impersonateInstructions}}}%}` (tu orientación), `{%{{{outline}}}%}` (el plan de la historia que pasa quien llama, por ejemplo el capítulo actual del Arc Outline), `{%{{{previousStory}}}%}`, `{%{{{summaries}}}%}`, `{%{{{backgroundLore}}}%}` (el trasfondo de la última parte), `{%{{{instructionExamples}}}%}` (tus instrucciones de las tres últimas partes, de la más antigua a la más reciente), `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}`, y el turno para el que son las instrucciones: `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}` |
| Prompt del sistema de anotación, Prompt de anotación | Las variables de los prompts de reescritura, con `{%{{{annotateInstructions}}}%}` (lo que quieres que se revise) en lugar de `{%{{{redraftInstructions}}}%}`, `{%{{{annotations}}}%}` (las notas que ya tiene la parte), `{%{{{facts}}}%}` (lo que las extensiones saben de la historia en la parte, por ejemplo qué saben los personajes; vacío sin una extensión de ese tipo) y `{%{{{outputFormat}}}%}` (cómo se le indica al modelo que responda) |

Una sección imprime su contenido solo cuando la variable no está vacía, así que el prompt no tiene encabezados vacíos:

{%{
```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```
}%}

`{%{{{.}}}%}` dentro de una sección es el valor de la variable. Con *Ambientación de la escena* vacía, se omite toda la línea.

Todas las plantillas también pueden usar las **macros** compatibles con SillyTavern, como `{%{{{user}}}%}` (el personaje con el punto de vista), `{%{{{description}}}%}` (la descripción del libro), `{%{{{random::a::b}}}%}`, `{%{{{getvar::name}}}%}` y `{%{{{setvar::name::value}}}%}`. Véase [Plantillas y macros](../templates/index.md) para la sintaxis completa y la referencia de macros.

<a id="master-template"></a>

## Plantilla principal

La plantilla principal se convierte en el **mensaje del sistema**, el primer mensaje del prompt. El valor predeterminado integrado tiene estos bloques:

1. **Papel del sistema** - el modelo es un autor que continúa la historia.
2. **Contexto del mundo y del trasfondo** - las entradas activadas del [libro de trasfondo](../lorebooks/index.md) (`backgroundLore`).
3. **Estilo de escritura y mecánica** - punto de vista, tiempo verbal, estilo y normas generales de prosa.
4. **Resumen de la historia hasta ahora** - los [resúmenes](summaries.md).
5. **Notas del autor sobre la historia** - solo cuando diste [notas al modelo](annotations.md#giving-a-note-to-the-model).

Si el [proveedor de inferencia](../inference-providers.md#general-settings) tiene activado un prompt de jailbreak, se coloca antes de la plantilla principal.

<a id="user-prompt"></a>

## Prompt de usuario

El prompt de usuario es el **último mensaje** del prompt: la petición de la parte que hay que escribir. Se rellena con las *Instrucciones del nuevo turno* (o con los *Detalles del turno* para *Regenerar* y *Variante*). El valor predeterminado integrado pide la siguiente sección con el personaje con el punto de vista, la ubicación, los personajes presentes y las instrucciones de la trama, seguidos de normas: no crear personajes nuevos salvo que se pidan, seguir las instrucciones, mantener la continuidad y devolver solo el texto de la historia.

Las entradas de libros de trasfondo con el modo de inserción *Antes del prompt de usuario* se colocan justo antes de él; véase [Entradas y activación](../lorebooks/entries.md).
<a id="redraft-prompt"></a>

## Prompt de redacción

La [reescritura](story-editor.md#redrafting-a-part) de una parte es una sola petición formada por dos prompts: el **Prompt del sistema de reescritura** es el
mensaje de sistema, la historia anterior a la parte la sigue como los mensajes previos de la conversación (las partes como las respuestas
del modelo, tal como las envía una generación), y el **Prompt de reescritura** es el último mensaje del usuario. El texto predeterminado
integrado indica al modelo que aplique tus instrucciones, que conserve lo que no pediste cambiar, que mantenga la coherencia con la historia
anterior y posterior a la parte, que conserve la voz y que responda solo con la parte reescrita. Ambos prompts tienen las mismas variables; `{%{{{previousStory}}}%}` es solo una
nota de que la historia llega como mensajes. Deja en el prompt del sistema lo que no cambia de una reescritura a otra, y en el
último prompt lo que sí cambia (el lore, que no es igual para cada parte, la historia posterior a la parte, la parte, las instrucciones): un servidor que recuerda el prompt
que leyó puede así reutilizar gran parte de él. Tu versión propia del último prompt debe conservar las variables que necesita: sin `{%{{{text}}}%}` el modelo no
ve la parte. Cada prompt sigue la regla de todos los prompts: el del libro, si lo tiene; si no, el de *Ajustes → Plantillas*; si no, el integrado.

Cuando reescribes solo un [pasaje seleccionado](story-editor.md#redrafting-only-a-passage), se usa el mismo prompt con
`{%{{{selection}}}%}` rellenado: el texto predeterminado integrado muestra entonces la parte con el pasaje entre `[[PASSAGE TO REDRAFT]]` y
`[[/PASSAGE TO REDRAFT]]` e indica al modelo que responda solo con el texto que sustituye al pasaje. Un prompt propio
que no conozca `{%{{{selection}}}%}` hace que el modelo responda con la parte entera, y entonces la respuesta completa sustituye a la selección; añade un bloque `{%{{#selection}}%}...{%{{{/selection}}}%}` (y `{%{{{^selection}}}%}` para el otro caso) para tratarlo.

<a id="redraft-shorter-and-longer"></a>

## Reescribir más corto y más largo

La opción **Extensión** de la ventana de [reescritura](story-editor.md#redrafting-a-part) (**La misma extensión**, **Más corto**, **Más largo**) no
toca los prompts de reescritura: para **Más corto** el texto de **Prompt para reescribir más corto** (para **Más largo**, el de **Prompt de reescritura más largo**)
se añade después de lo que escribiste en *Instrucciones para la reescritura*, en el lugar de `{%{{{redraftInstructions}}}%}`, así que también funciona con un prompt de reescritura
propio. Con las instrucciones vacías, es la única instrucción. Los dos textos siguen la regla de todos los prompts: el del libro, si lo tiene; si no, el de la carpeta más cercana por encima que lo defina; si no, el de *Ajustes → Plantillas*; si no, el texto integrado, que pide entre una tercera parte y la mitad menos (o más: la mitad más hasta el doble de largo) sin cambiar lo que ocurre.

<a id="annotate-prompt"></a>

## Prompt de anotación

Cuando un modelo [anota una parte](annotations.md#letting-a-model-annotate) recibe el mismo tipo de petición que una reescritura: el
**Prompt del sistema de anotación** como mensaje de sistema, la historia anterior a la parte como mensajes previos y el **Prompt de anotación** como
último mensaje. El texto predeterminado integrado convierte al modelo en un editor: busca los pasajes que tienen un problema según tus
instrucciones, cita cada uno letra por letra y te escribe una nota breve, sin elogios ni relleno y sin repetir las notas que la parte ya tiene.
Cuando una extensión aporta [datos sobre la historia](annotations.md#letting-a-model-annotate) (el Knowledge Tracker: qué sabe cada personaje), el prompt
predeterminado los muestra como *Hechos establecidos* y el editor señala lo que los contradiga. El prompt tiene la variable `{%{{{outputFormat}}}%}` donde se
describe la forma de responder (JSON, bloques de texto o una revisión libre, según el formato de respuesta que elijas); consérvala en un prompt propio, o el
modelo no sabrá cómo responder. Ambos prompts de anotación siguen la regla de todos los prompts: el del libro, si lo tiene; si no, el de
*Ajustes → Plantillas*; si no, el integrado.

**Qué buscar** (*Instrucciones de anotación*) sigue la misma regla, así que puedes configurarlo una vez: en *Ajustes → Plantillas* para todos tus
libros, en una carpeta para los libros que contiene, o en la pestaña *Prompts* para un solo libro. La ventana *Anotar con IA* muestra lo que se aplica como
texto gris de su campo e indica de dónde viene; deja el campo vacío para usarlo, escribe algo para sustituirlo en esa ejecución, o haz clic en el icono de información del campo y en **Fill default** para copiar lo que se aplica al campo y editarlo para esa ejecución. Si no hay nada configurado en ningún sitio, la tarea predeterminada del prompt es la que se usa. Lo mismo vale para la anotación automática y el comando `/generate-annotation`.

<a id="impersonate-prompt"></a>

## Prompt de suplantación

Cuando un modelo [redacta las instrucciones](story-editor.md#drafting-the-instructions) del siguiente turno recibe una sola petición, enviada como único
mensaje, igual que la reescritura. El texto predeterminado integrado pide al modelo que escriba lo que escribirías tú en *Instrucciones*: una especificación de trama de la siguiente parte (qué ocurre, quién hace qué, dónde termina la parte) que desarrolla tu indicación y sigue el plan, continúa la historia desde donde se quedó, está escrita como tus propias instrucciones anteriores y no es la prosa en sí. Sin indicación, continúa la historia por donde la lleve. Tu indicación va donde el prompt pone `{%{{{impersonateInstructions}}}%}`; consérvala en un prompt propio. El prompt de suplantación sigue la regla de todos los prompts: el del libro, si lo tiene; si no, el *Prompt de suplantación* de *Ajustes → Plantillas*; si no, el integrado.

<a id="how-the-prompt-is-put-together"></a>

## Cómo se forma el prompt

```
system     jailbreak (if enabled) + master template
user       [ Generate story. ]
assistant  part #1
user       [ Generate more story. ]
assistant  part #2
...        (as many earlier parts as fit)
user       lore "Before User Prompt" + user prompt
```

Las partes anteriores se envían como respuestas propias del modelo, así que lo continúa con la misma voz. Cuando la historia es más larga que el [límite de contexto](../protocols.md#limits), se eliminan primero las partes más antiguas; los [resúmenes](summaries.md) mantienen esas partes en la historia en forma condensada.

**Mostrar el prompt** en el menú de cualquier parte muestra el prompt exacto con el que se escribió esa parte. **Comparar prompt** lo pone junto al prompt de una parte anterior y muestra en qué punto difieren por primera vez, que es donde termina la [caché de prompts](../prompt-cache.md#compare-prompt) de un servidor.

<a id="tips"></a>

## Consejos

- Pon las reglas que se aplican a cada parte (sin personajes nuevos, extensión, formato) en el prompt de usuario: al ser el último
  mensaje, tiene el efecto más fuerte.
- Pon los datos duraderos sobre el mundo y los personajes en el [lorebook](../lorebooks/index.md), no en *Estilo*.
- Pide una extensión en las instrucciones («unas 800 palabras») en lugar de confiar en el límite de respuesta, que corta el
  texto.
- Después de cambiar una plantilla, genera una parte y compruébala con **Mostrar el prompt**.
