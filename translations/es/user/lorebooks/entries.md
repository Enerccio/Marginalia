---
label: Entradas y activación
order: 90
---

# Entradas y activación

<a id="the-entry-list"></a>

## La lista de entradas

Cada fila del editor de lorebook es una entrada:

| Columna | |
|---|---|
| casilla | **Activado**. Las entradas desactivadas nunca se usan. |
| **Nombre de la entrada** | Tu nombre para la entrada. No se envía al modelo. |
| **Orden** | Posición de la entrada en el prompt, ver [Orden e inserción](#order-and-insertion). Las entradas nuevas reciben `100`. |
| **Etiquetas** | Etiquetas que activan la entrada, ver [Etiquetas](#tags). |
| ⌄ | Muestra los detalles de la entrada. |
| expandir | Abre la entrada en el [editor a pantalla completa](lorebooks.md#editing-in-full-screen) (escritorio). |
| papelera | Elimina la entrada (tras confirmar). |

**Añadir entrada** en la barra de herramientas añade una entrada con orden `100`. Los cambios se guardan mientras escribes.

<a id="searching-and-deleting-many-entries"></a>

### Buscar y eliminar muchas entradas

El cuadro con la lupa junto a los botones muestra solo las entradas que tienen el texto en su nombre, contenido, nota o
filtro (no importan las mayúsculas); bórralo para volver a ver todas. Solo mira las entradas del lorebook seleccionado, no
los sublorebooks. En el diseño de teléfono, el cuadro está encima de la lista.

Junto al cuadro hay otros dos que limitan la lista. **Etiquetas** muestra las entradas que tienen la etiqueta (lista las
etiquetas que tiene al menos una entrada del lorebook; las etiquetas *negativas* no se tienen en cuenta). **Estado** muestra
**Todo**, **Activado**, **Desactivado** o **Constantes (sin claves)**: las entradas que están activadas y no tienen filtro, por
lo que se usan en todas las partes. La búsqueda, la etiqueta y el estado se combinan: una entrada tiene que cumplir todos.
En el diseño de teléfono, los dos cuadros están debajo del cuadro de búsqueda.

La columna de casillas de la izquierda selecciona entradas (la de la cabecera selecciona todas las que se muestran, así que
una búsqueda seguida de la casilla de la cabecera selecciona las coincidencias). Cuando hay algo seleccionado, aparece
**Eliminar la selección** en la barra de herramientas con el número de entradas; pide confirmación una vez y luego las mueve a la [papelera](../trash.md). La [edición masiva](lorebooks.md#mass-edit) también tiene un botón **Eliminar los marcados**. En el teléfono, el menú **⋮** de las entradas tiene **Seleccionar entradas**: aparecen casillas delante de las entradas, y aparece **Eliminar la selección** con el número de las marcadas (una entrada también tiene su propio botón **Eliminar entrada** ahí).


<a id="search-and-replace"></a>

## Buscar y reemplazar

**Buscar y reemplazar** (a la derecha de **Añadir entrada**) cambia una palabra, un nombre o una frase en el nombre, el texto y
el comentario de todas las entradas del lorebook a la vez. Funciona como la [búsqueda y reemplazo de la historia](../books/story-editor.md#search-and-replace),
sin las opciones de resúmenes y ramas: **Buscar** lista las entradas con coincidencias (la tabla muestra sus nombres) y las
coincidencias en ellas tal como están y tal como quedarán, **Reemplazar** cambia las entradas. Tiene las opciones **Buscar con expresión regular** (ver [Expresiones regulares](../regular-expressions.md)) y **Distinguir mayúsculas y minúsculas** (activada por defecto; bloqueada mientras la expresión regular está activa, que
puede usar `(?i)` para ignorar las mayúsculas). Las entradas desactivadas también se buscan. En el teléfono, el botón (un icono junto a la casilla *Activado*) abre la misma ventana en el diseño de teléfono.

![La ventana Buscar y reemplazar de un lorebook, con las coincidencias de una entrada abiertas](../../images/lorebook-search-replace.png)

La línea de comandos del editor de la historia tiene `/search-replace-lore-entries` para el lorebook del libro abierto, ver
[Comandos](../commands/index.md#commands-that-come-with-marginalia).

<a id="entry-details"></a>

## Detalles de la entrada

El botón ⌄ abre los detalles:

![Una entrada con sus detalles](../../images/lorebook-entry.png)

| Campo | |
|---|---|
| **Contenido** | El texto que entra en el prompt. Una [plantilla](#templates-in-entries): puede usar variables y macros. |
| **Modo de filtrado** | **Palabras clave (cualquiera)**, **Palabras clave (todas)**, **Texto** o **Expresión regular**, cómo se compara *Texto o patrón de filtrado*. |
| **Texto o patrón de filtrado** | El filtro, ver [Filtros](#filters). Vacío significa sin filtro. |
| **Modo de inserción** | **En el bloque de lore** o **Antes del prompt de usuario**, ver [Orden e inserción](#order-and-insertion). |
| **Contexto de activación** | Dónde se busca el filtro: **Instrucciones** (la petición de la parte, por defecto) o **Instrucciones y partes de la historia**, ver [Mirar en la historia](#looking-into-the-story). |
| **Partes** | Con *Instrucciones y partes de la historia*: cuántas de las partes más recientes de la historia se buscan junto con la petición, como mínimo 1. |
| **Desde el mensaje** | La entrada se usa solo desde la parte con este número en adelante, ver [Limitar una entrada a algunas partes](#limiting-an-entry-to-some-parts). Vacío: desde el principio. |
| **Hasta el mensaje** | La entrada se usa solo hasta la parte con este número. Vacío: hasta el final. |
| **Condición** | Una plantilla que tiene que dar un valor verdadero para que la entrada se use, ver [Una condición para una entrada](#a-condition-for-an-entry). Vacío: sin condición. |
| **Etiquetas negativas (exclusión)** | Etiquetas que desactivan la entrada, ver [Etiquetas](#tags). |
| **Buscar también en los resúmenes** | Si está marcado, el filtro también se busca en los [resúmenes](../books/summaries.md) de la historia hasta ahora, los que enviaría la siguiente petición (un resumen que ya no coincide con su historia se deja fuera). Necesita un filtro. |
| **No buscar otras entradas en su contenido** | Si está marcado, las palabras del contenido de esta entrada nunca traen otras entradas, ver [Entradas traídas por otras entradas](#entries-brought-in-by-other-entries). |
| **Solo a través de otras entradas** | Si está marcado, la entrada nunca se usa por la petición o por la propia historia; solo cuando su filtro aparece en el contenido de otra entrada que se usó, ver [Entradas traídas por otras entradas](#entries-brought-in-by-other-entries). |
| **Nota** | Tus notas. No se envían al modelo. Las entradas importadas de SillyTavern enumeran aquí los ajustes que no se pudieron importar. |

Escribe el contenido tal como quieres que el modelo lo lea; una descripción breve y objetiva funciona mejor:

> **Capitán Varro** - comandante de la guardia del puerto de Ostra, cincuentón, con la mano izquierda marcada por una cicatriz. Leal al Duque, desconfía
> del gremio de magos. Habla con frases cortas y secas.
<a id="activation"></a>

## Activación

Antes de cada parte generada, Marginalia recorre las entradas del lorebook del libro y de sus [sublorebooks](lorebooks.md#sub-lorebooks). Una entrada se **activa** cuando:

1. la entrada está habilitada, y también su lorebook;
2. la parte que se escribe está dentro de su rango de partes, si tiene uno (**Desde el mensaje**, **Hasta el mensaje**);
3. ninguna de sus **etiquetas negativas** es una etiqueta del libro;
4. no tiene **etiquetas**, o al menos una de ellas es una etiqueta del libro;
5. su **condición**, si tiene una, es verdadera; véase [Una condición para una entrada](#a-condition-for-an-entry);
6. no tiene **filtro**, o el filtro coincide con las instrucciones de la parte (o, con **Contexto de activación** *Instrucciones y partes de la historia*, con las instrucciones y las partes más recientes de la historia).

Todas las condiciones deben cumplirse. Una entrada sin etiquetas ni filtro está siempre activa.

<a id="tags"></a>

### Etiquetas

Las etiquetas conectan las entradas con las **etiquetas del libro** (se definen en la pestaña [Acerca de](../books/managing-books.md#the-about-tab) del libro). Úsalas para compartir un mismo lorebook entre libros que necesitan partes distintas de él:

| Etiquetas de la entrada | Etiquetas negativas | Activa en un libro con la etiqueta... |
|---|---|---|
| - | - | siempre |
| `winter` | - | `winter` |
| `winter`, `book-2` | - | `winter` **o** `book-2` |
| - | `book-1` | cualquiera excepto `book-1` |
| `winter` | `book-1` | `winter`, salvo que también sea `book-1` |

Las etiquetas deben coincidir exactamente. Una etiqueta negativa siempre prevalece sobre una etiqueta.

También se pueden dar etiquetas a un lorebook completo (el campo **Etiquetas** del [lorebook](lorebooks.md); los lorebooks importados desde archivos pueden traerlas). Entonces cuentan como etiquetas de cada una de sus entradas.

<a id="filters"></a>

### Filtros

Un filtro activa la entrada solo cuando la **petición de la parte** la menciona. Se compara con el [prompt de usuario](../books/prompts.md#user-prompt) de la parte tal como se envía: la *Ambientación de la escena*, el *Personaje del punto de vista*, los *Personajes presentes* y las *Instrucciones* que hayas escrito, dentro del texto de la plantilla del prompt de usuario.

| Modo | Coincide cuando |
|---|---|
| **Palabras clave (cualquiera)** | El filtro es una lista de palabras clave separadas por comas (`Varro, Ostra`); la petición contiene al menos una de ellas, sin distinguir mayúsculas. Los espacios alrededor de una palabra clave se ignoran. |
| **Palabras clave (todas)** | La misma lista, pero la petición debe contener todas las palabras clave. |
| **Texto** | El texto aparece en cualquier parte de la petición, sin distinguir mayúsculas. `varro` coincide con *Capitán Varro*. |
| **Expresión regular** | La [expresión regular](../regular-expressions.md#in-a-lorebook-entry-filter) coincide en cualquier parte de la petición, sin distinguir mayúsculas, a través de las líneas. |

Por ejemplo, la expresión regular `\b(varro|captain)\b` coincide con la palabra completa *Varro* o *Captain*, y `varro.*(harbor|docks)` solo coincide cuando se mencionan Varro y el puerto o los muelles, en ese orden.

!!!warning Por defecto el filtro mira la petición, no la historia
Salvo que la entrada mire dentro de la historia (véase [Mirar dentro de la historia](#looking-into-the-story)), un filtro comprueba solo las instrucciones de la nueva parte, no el texto de las partes anteriores. Una entrada sobre Varro se activa cuando Varro se menciona en la escena, los personajes o las instrucciones de la parte que escribes; ponlo en *Personajes presentes* cuando esté en la escena.

La petición también contiene el propio texto de la plantilla del prompt de usuario. Un filtro para una palabra que usa la plantilla (la predeterminada menciona *characters*, *plot*, *tone*...) está siempre activo.
!!!

Una expresión regular no válida nunca coincide; la entrada permanece inactiva.

Combina etiquetas y filtro cuando una entrada solo deba usarse en algunos libros, y allí solo cuando sea relevante.

<a id="looking-into-the-story"></a>

### Mirar dentro de la historia

Por defecto el filtro se busca solo en la petición de la parte, así que una entrada sobre el dragón se usa cuando *tú* mencionas al dragón. Cuando la historia debe mantener la entrada activa —el dragón estaba en la última parte y tu nueva instrucción solo dice «él se aleja volando»—, pon **Contexto de activación** de la entrada en **Instrucciones y partes de la historia** y **Partes** con el número de las partes más recientes que se buscarán junto con la petición (`1` es la parte anterior a la que se escribe, `2` las dos anteriores...).

- El filtro se busca en el texto de esas partes y en la petición a la vez, así que las palabras clave de **Palabras clave (todas)** pueden repartirse entre ellas. La petición siempre cuenta.
- Solo se busca en el texto de las partes, no en sus resúmenes, y solo en las partes de la rama que escribes (una alternativa o una regeneración no busca en la parte que reemplaza).
- Una historia más corta que **Partes** se busca entera. Marginalia lee solo tantas partes como necesitan las entradas activadas, así que un lorebook sin estas entradas no toca la historia en absoluto.
- Las entradas traídas por [otras entradas](#entries-brought-in-by-other-entries) no se buscan en la historia, solo en el contenido de las entradas que se usaron.

<a id="limiting-an-entry-to-some-parts"></a>

### Limitar una entrada a algunas partes

Algunos datos solo son ciertos en una parte de la historia: el disfraz de un personaje que se descubre en el capítulo 5, los rumores antes de que empiece la guerra. Rellena **Desde el mensaje** y/o **Hasta el mensaje** con los números de las partes, tal como los numera el esquema de la historia (la primera parte es `1`; ambos números se incluyen). La entrada se usa entonces solo cuando la parte que se escribe tiene un número dentro del rango, sea cual sea su filtro; una entrada sin filtro es una entrada constante solo dentro del rango.

- Un campo vacío no es un límite: solo **Desde el mensaje** `10` significa «desde la parte 10 en adelante», y solo **Hasta el mensaje** `4` significa «hasta la parte 4».
- La parte que se escribe es la que sigue a la última parte de la rama (una historia de 9 partes escribe la parte `10`). Una alternativa o una regeneración escribe la parte que reemplaza.
- Las partes sin texto cuentan igual que en el esquema. Borrar o insertar partes cambia los números.
- Si **Desde el mensaje** es mayor que **Hasta el mensaje**, la entrada nunca se usa.
- Una entrada fuera de su rango tampoco es traída por [otras entradas](#entries-brought-in-by-other-entries).
- La extensión [Lorebook VCS](../extensions/index.md) guarda el rango en las versiones de una entrada; la importación, la exportación y la clonación de un lorebook también lo conservan.

<a id="a-condition-for-an-entry"></a>

### Una condición para una entrada

La **Condición** es una plantilla, escrita como el [contenido de una entrada](#templates-in-entries), que Marginalia renderiza antes de cada parte. La entrada se usa solo cuando el resultado es **true**: cualquier cosa excepto un resultado vacío, `0`, `false`, `no` y `off` (en cualquier mayúscula). Ve lo mismo que el contenido: el personaje del punto de vista, los personajes presentes, la escena, las instrucciones del turno, la historia hasta ese momento y las variables.

![Una entrada con condición: la entrada se usa solo cuando Tomáš es el punto de vista y Mizuki está presente](../../images/lorebook-condition.png)

| Condición | La entrada se usa cuando |
|---|---|
| `{%{{{#if presentCharacters}}yes{{/if}}}%}` | el turno tiene personajes presentes |
| `{%{{{eval::povCharacter == "Fiora"}}}%}` | el punto de vista es Fiora (véase [Expresiones](../templates/macros.md#expressions)) |
| `{%{{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}}%}` | Fiora es el punto de vista y Mara está presente |
| `{%{{{getvar::revealed}}}%}` | la variable `revealed` tiene un valor (lo fija una parte anterior; véase [Referencia de macros](../templates/macros.md#variables)) |
| `{%{{{command::authors-note}}}%}` | el [comando](../commands/index.md) responde con algo (solo se puede usar un comando hecho para macros) |

Cómo funciona:

- Se comprueba después del interruptor de habilitación, del rango de partes y de las etiquetas, y **antes del filtro**: una entrada cuya condición es falsa no se usa aunque su filtro coincida, y tampoco es traída por [otras entradas](#entries-brought-in-by-other-entries).
- Una condición que **no se puede decidir** cuenta como falsa, y la [vista previa](#preview-activation) indica el motivo: no es una plantilla válida, usa un nombre que no existe (una errata), un comando que no existe o que no está hecho para macros, o se agotó el tiempo. El campo se vuelve rojo en los dos primeros casos mientras escribes.
- Solo lee. Un `{%{{{setvar}}}%}` en una condición no cambia nada, ni para el prompt ni para las otras entradas, y los comandos son los que solo leen.
- Se renderiza una vez por cada entrada y parte, así que `{%{{{random::yes::no}}}%}` no cambia de opinión entre la primera pasada y las pasadas de la profundidad de análisis.
- La [vista previa](#preview-activation) nombra las entradas cuya condición es falsa: *No usada: la condición es falsa*.

  ![La vista previa de la activación: la entrada de Mizuki no se usa porque su condición es falsa](../../images/lorebook-condition-preview.png)
- Todas las condiciones de una parte comparten **60 segundos**. Cuando se agotan, las condiciones que quedan cuentan como falsas (*condición fallida*, tiempo agotado). Una condición que está en marcha no se detiene; el tiempo se comprueba antes de la siguiente.
- La importación y exportación de lorebooks, la clonación y la extensión Lorebook VCS conservan la condición.

<a id="entries-brought-in-by-other-entries"></a>

### Entradas traídas por otras entradas

Normalmente solo la petición de la parte activa entradas. Con una **profundidad de análisis** (el campo del [lorebook](lorebooks.md#the-lorebook-editor); `0`, que es el valor predeterminado, lo desactiva), el contenido de las entradas que se activaron se busca en los filtros de las entradas que no se activaron, como si formara parte de la petición. Una entrada cuyo filtro se encuentra allí también se activa. Esa es la **pasada 1**; el contenido de las entradas que activó se busca en la pasada 2, y así sucesivamente, tantas pasadas como indique la profundidad de análisis (como máximo 5).

Por ejemplo, con una profundidad de análisis de `1`, una entrada *Capitán Varro* (filtro `varro`) se activa con la petición, y su contenido dice «...sirve al Duque de Ostra». Entonces también se activa una entrada *Ostra* (filtro `ostra`), aunque la petición nunca menciona la ciudad. Con una profundidad de análisis de `2`, la entrada *Ostra* traería a su vez una entrada sobre el puerto que menciona.

- Solo cuentan los **filtros**: una entrada que ya se usa porque no tiene filtro no necesita nada, y una entrada desactivada, o excluida por sus etiquetas, no se trae.
- El contenido que se busca es el texto de la entrada tal como está escrito; las plantillas que contiene no se ejecutan.
- Cuenta la **profundidad de análisis** del lorebook que **usa el libro**, tanto para sus propias entradas como para las de sus sublorebooks. Un sublorebook puede tener una profundidad propia (**Profundidad de análisis como sublorebook** en sus ajustes; vacío: la del lorebook que usa el libro): sus entradas solo son encontradas por el contenido de otras en las pasadas hasta esa profundidad, y `0` nunca. Se hacen tantas pasadas como indique la mayor profundidad. El campo de un sublorebook solo cuenta mientras es un sublorebook; cuando un libro lo usa directamente, cuenta su **Profundidad de análisis** normal.
- Una entrada con **No buscar otras entradas en su contenido** marcado puede activarse, pero no trae a nadie.
- Una entrada con **Solo a través de otras entradas** marcado nunca es encontrada por la petición: se usa solo cuando el filtro de la entrada aparece en el contenido de una entrada que se usó. Sin profundidad de análisis, o sin filtro, nunca se usa; la [vista previa](#preview-activation) indica *No usada: se usa solo a través del contenido de otras entradas*.
- El orden en el prompt no depende de cómo se encontró una entrada; véase [Orden e inserción](#order-and-insertion).
- Las extensiones que cambian qué entradas se usan (véase [Extensiones](../extensions/index.md)) también son consultadas sobre las entradas que se traen.
<a id="preview-activation"></a>

## Vista previa de la activación

**Vista previa de la activación** (el botón con el ojo en la barra de herramientas del editor de lorebooks, y en el teléfono junto al botón de búsqueda y reemplazo) responde a la pregunta «¿qué entradas se usarían para este texto?» sin generar nada. Escribe el texto de la petición en **Texto del turno** (la escena, los personajes y las instrucciones, tal como los escribirías) y la tabla muestra cada entrada del lorebook y de sus sublorebooks, en el orden en que se usan:

- una marca de verificación verde para las entradas que se usarían y una marca gris para las demás;
- el lorebook de la entrada, su **Orden**, la **Pasada** (vacía para las entradas que activó el texto, `1`, `2`... para las entradas incorporadas por [otras entradas](#entries-brought-in-by-other-entries));
- el **Motivo**: sin filtro, filtro encontrado en el texto, filtro encontrado en el contenido de otra entrada (con el filtro y la entrada), o por qué no: desactivada, la parte está fuera de su rango, una etiqueta negativa del libro, el libro no tiene una etiqueta de la entrada, la condición es falsa o no se pudo decidir, o no se encontró el filtro.

Las etiquetas también influyen, así que elige un libro en **Etiquetas del libro** para usar sus etiquetas (sin ninguno, no cuenta ninguna etiqueta). Ese libro también proporciona las partes de su historia para las entradas que [miran en la historia](#looking-into-the-story) (sin él, solo buscan en el texto) y el número de la siguiente parte para las entradas [limitadas a algunas partes](#limiting-an-entry-to-some-parts) (sin él, no se comprueba su rango). El texto que escribes son las instrucciones del turno para las [condiciones](#a-condition-for-an-entry); con un libro, también se usan sus variables y el punto de vista, los personajes presentes y la escena de su última parte (los comandos de una condición no se ejecutan en la vista previa). Debajo del texto, una línea indica cuántas entradas se usarían y de cuántos tokens de lore aproximadamente se trata. Se usa la profundidad de análisis del lorebook.

En el [editor a pantalla completa](lorebooks.md#editing-in-full-screen) el ojo también está en la fila del nombre de cada pestaña de entrada: la vista previa se abre para esa entrada, su fila se selecciona y una línea encima de la tabla indica si se usaría y por qué, lo que cambia mientras escribes. Cambia el filtro en la pestaña y luego pulsa **Actualizar** en la vista previa para ver el efecto.

![Vista previa de la activación de un lorebook](../../images/lorebook-preview-activation.png)

La vista previa aplica solo las reglas de Marginalia: el texto es el que escribes (no la plantilla del prompt de usuario que lo rodea), y lo que una [extensión](../extensions/index.md) cambie mientras se genera una parte no forma parte de ella.

<a id="order-and-insertion"></a>

## Orden e inserción

Las entradas activadas se colocan en el prompt ordenadas por **Orden**, de menor a mayor. Las entradas con el mismo orden conservan el orden en que se crearon. Las entradas de todos los lorebooks incluidos se ordenan juntas.

**Modo de inserción** decide dónde va una entrada:

| Modo | Dónde |
|---|---|
| **En el bloque de lore** | Dentro del bloque `{%{{{backgroundLore}}}%}` de la [plantilla maestra](../books/prompts.md#master-template) —*World & lore context* en la plantilla por defecto—, en el mensaje de sistema. Úsalo para el conocimiento de trasfondo. |
| **Antes del prompt de usuario** | Justo antes de la petición de la parte, al final del prompt. El modelo presta más atención a este lugar. Úsalo con moderación, para lo que deba influir en esta parte concreta (el estado actual de un personaje, una regla para la escena). |

Las entradas se separan con una línea vacía. El lore cuenta para el [límite de contexto](../protocols.md#limits); con muchas entradas largas, cabe menos historia en el prompt.

<a id="templates-in-entries"></a>

## Plantillas en las entradas

El contenido de una entrada es una plantilla, como los [prompts del libro](../books/prompts.md#templates). Puede usar:

| Variable | |
|---|---|
| `{%{{{sceneSetting}}}%}`, `{%{{{povCharacter}}}%}`, `{%{{{presentCharacters}}}%}`, `{%{{{instructions}}}%}` | Las instrucciones de la parte. |
| `{%{{{narrativePov}}}%}`, `{%{{{narrativeTense}}}%}`, `{%{{{style}}}%}` | La configuración narrativa del libro. |

y todas las [macros](../templates/macros.md). Por ejemplo, una entrada que se adapta al personaje de punto de vista:

{%{
```handlebars
Varro {{#povCharacter}}is seen through the eyes of {{.}}: {{/povCharacter}}a stern, scarred officer.
```
}%}

Todas las entradas activadas de una parte comparten sus variables en orden de activación: una entrada puede usar `setvar` para establecer un valor que las entradas posteriores (y la plantilla maestra) leen con `getvar`.

Un error de sintaxis en el contenido se muestra debajo del campo. La entrada se guarda igualmente; hasta que se corrija, su contenido se usa como texto plano, sin procesar la plantilla.

<a id="tips"></a>

## Consejos

- Mantén las entradas cortas y factuales; el modelo no necesita prosa, necesita hechos.
- Una entrada por personaje, lugar o concepto, para que cada una pueda activarse por separado.
- Usa entradas siempre activas para el núcleo del mundo, filtros para el reparto y los lugares, y etiquetas para compartir un lorebook entre libros.
- Comprueba qué se activó con **Mostrar el prompt** en una parte generada, o prueba un texto antes de escribir con la [Vista previa de la activación](#preview-activation).
- Usa la profundidad de análisis con moderación: una pasada basta para la mayoría de los libros, y cada pasada puede traer más lore del que querías.
