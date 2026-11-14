---
label: Comandos
order: 945
icon: terminal
---

# Comandos

Los comandos son pequeñas acciones que se ejecutan escribiéndolas, en lugar de navegar por menús: buscar algo en la historia o
en los lorebooks, buscar y reemplazar, o hacer una copia de seguridad. [Extensiones](../extensions/index.md) añaden comandos propios,
por ejemplo para generar una entrada de lore o para inventar nombres. Los mismos comandos son con los que trabaja un [agente](../agents.md),
así que cada comando hace una sola cosa concreta y dice qué hace: puedes leerlo mientras lo escribes.

La **línea de comandos** es el campo situado junto al menú, debajo de la historia, en el [editor de historias](../books/story-editor.md).

<a id="running-a-command"></a>

## Ejecutar un comando

1. Haz clic en la línea de comandos y escribe `/`. La lista muestra todos los comandos, cada uno con su nombre y una
   descripción breve. Si escribes más, la lista se reduce: el filtro mira el nombre del comando, su título y su descripción.
2. Elige el comando con el ratón, o con las flechas y **Enter**. Un comando que no necesita nada se ejecuta de inmediato; para
   uno que necesita argumentos, se escribe `/name ` en el campo y continúas con los argumentos.
![La lista de comandos, abierta al escribir una barra](../../images/command-list.png)

3. Escribe los argumentos y pulsa **Enter**, o haz clic en el botón de reproducir junto al campo, para ejecutarlo. El campo se vacía
   en cuanto empieza el comando; un texto que no es un comando se queda, para que puedas corregirlo.

El texto que no empieza por `/` no es un comando, y no se ejecuta nada. Hacer clic fuera del campo o pulsar **Esc**
nunca ejecuta nada, así que no puedes iniciar un comando por accidente.

<a id="the-help"></a>

## La ayuda

Mientras escribes, se abre una pequeña ventana encima del campo. Cubre parte de la historia, pero no se mueve, y se cierra cuando
sales del campo o ejecutas el comando. Muestra:

- qué hace el comando,
- las formas del comando (ver [Formas](#forms-of-a-command)) que encajan con lo que has escrito hasta ahora, con el argumento que
  estás escribiendo ahora resaltado y su descripción,
- qué devuelve el comando,
- una indicación cuando has escrito más argumentos de los que el comando admite, o cuando ningún comando tiene ese nombre.

![La ayuda de /search-replace-lore-entries mientras se escribe su segundo argumento](../../images/command-help.png)

Mientras todavía escribes el nombre, la lista de comandos es la ayuda.

<a id="arguments"></a>

## Argumentos

Los argumentos se separan con espacios, y varios espacios seguidos cuentan como uno. Para poner un espacio *dentro* de un
argumento, hay tres maneras:

| Escribes                     | El comando recibe               | Por qué                                    |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words`   | un argumento, `two words`       | `\ ` (barra invertida, espacio) es un espacio |
| `/search-parts "two words"`  | un argumento, `two words`       | las comillas dobles mantienen todo junto   |
| `/search-parts say\ "hi"`    | un argumento, `say hi`          | las comillas pueden empezar en medio de un argumento |

Los demás caracteres especiales:

| Escribes                      | Obtienes                   |
|-------------------------------|----------------------------|
| `\\`                          | una barra invertida        |
| `\"`                          | unas comillas dobles, con o sin comillas alrededor: `"say \"hi\""` es `say "hi"` |
| `""`                          | un argumento vacío         |

Una barra invertida delante de cualquier otra cosa se queda como está, así que `C:\books\story` se pasa sin cambios. Si abres unas
comillas y nunca las cierras, todo hasta el final de la línea pertenece a ese argumento.

Los argumentos son texto. Un comando que necesita un número, un sí/no o una de unas pocas opciones convierte el texto por sí mismo; si el texto no
encaja, el comando no se ejecuta y una notificación lo indica.

<a id="forms-of-a-command"></a>

## Formas de un comando

Un mismo comando puede aceptar argumentos distintos: `/list-parts` sin nada, y `/list-parts 2` con un argumento
son dos **formas** del mismo comando. Marginalia elige la forma según el número de argumentos que escribiste y según su
aspecto. Si ninguna forma encaja, una notificación indica que el comando no acepta esos argumentos, y la ayuda muestra las
formas que sí tiene.

<a id="the-result"></a>

## El resultado

Cuando un comando devuelve algo, se muestra en una ventana encima de la línea de comandos. Ciérrala con la cruz, con **Esc**,
o simplemente sigue: desaparece cuando escribes algo nuevo o ejecutas otro comando. Un comando que no devuelve nada no muestra
ninguna ventana.

![El resultado de /take-backup encima de la línea de comandos](../../images/command-result.png)

Si un comando falla, o no existe, recibes una notificación con el motivo.

<a id="commands-that-take-time"></a>

## Comandos que tardan

La mayoría de los comandos responden al momento. Un comando que necesita tiempo muestra una ventana con una barra animada y un botón **Detener**. La
ventana queda a un lado y la página sigue utilizable; su ⌄ la pliega en el
[panel de ejecuciones](../books/story-editor.md#the-panel-of-the-runs), donde el comando aparece listado con lo que escribe el modelo, en
todas tus páginas. Debajo de la barra, el comando puede indicar qué está haciendo en ese momento y, en letra más pequeña, el detalle del paso.
**Detener** cancela el comando; debería volver de inmediato. Los comandos que hacen que el modelo escriba un resumen o unas notas, como `/generate-annotation`,
son de este tipo.

`/next`, `/swipe` y `/regenerate`, escritos en la barra de comandos de la historia, no tienen esa ventana: la parte nueva se va escribiendo en la historia
como cuando pulsas el botón redondo, el botón redondo lo detiene, y la historia queda retenida igual que en cualquier generación (puedes seguir
trabajando en el lore, en las otras partes y en las pestañas laterales). El resultado del comando aparece encima de la barra cuando la parte
termina. Mientras tanto, la barra no admite un segundo comando de estos (conservas el texto y puedes ejecutarlo más tarde); los demás comandos funcionan.

![La ventana de un comando en ejecución](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## Comandos que cambian datos

Cada comando indica qué hace con los datos que ya existen, y la barra de comandos lo muestra como una pequeña etiqueta junto al
comando, en la lista y en la ayuda:

- **Destructivo** (rojo): el comando cambia o elimina datos existentes, como los comandos de buscar y reemplazar que
  reescriben el libro. Señala la etiqueta para ver la explicación.
- **No destructivo** (verde): el comando solo lee datos o añade datos nuevos, como `/take-backup`, que añade una copia de seguridad y
  no cambia nada más.

La etiqueta es información para ti; no detiene el comando al ejecutarlo. Un [agente](../agents.md) se guía por ella: no se le da un
comando destructivo, o tiene que preguntarte antes, o lo ejecuta, según se haya configurado para ese agente. Un comando que pide
confirmación por sí mismo se describe a continuación.

<a id="commands-that-ask-first"></a>

## Comandos que preguntan antes

Un comando que cambia mucho puede preguntar antes de hacerlo. Los comandos de buscar y reemplazar no tienen vista previa, así que tras la
búsqueda muestran cuántas coincidencias han encontrado en cuántos elementos y qué se reemplazaría por qué, y solo **Sí**
cambia el libro; **No** deja todo como estaba. La pregunta permanece hasta que respondas.

![La pregunta de /search-replace-parts](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Comandos que vienen con Marginalia

| Comando                              | Qué hace                                                                       |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | Buscar y reemplazar en la historia del libro abierto, como la ventana **Buscar y reemplazar** de la pestaña de la historia. Las opciones son `true`/`false`, en este orden, y cada una que omitas toma el valor por defecto de esa ventana ([regex](../regular-expressions.md) no, distingue mayúsculas sí, resúmenes sí, todas las ramas no, revalidar sí); para cambiar una, indica también las anteriores. `maxHits` rechaza un reemplazo que encuentre más coincidencias que ese número, y con `dryRun` `true` no se cambia nada: la respuesta cuenta las coincidencias y muestra algunas, que es como un agente comprueba antes de reemplazar. No hay vista previa: tras la búsqueda se te pregunta si reemplazar el número de coincidencias encontradas, y solo **Sí** cambia el libro. El reemplazo no se puede detener. |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | Lo mismo para las entradas (nombre, texto y comentario) del lorebook del libro abierto. |
| `/take-backup`                       | Hace una copia de seguridad del libro que está abierto; aparece en la pestaña **Copias de seguridad** del libro. Indica que no hay ningún libro abierto cuando no lo hay. |
| `/get-book-info` | El nombre, la configuración, el modelo, los totales (partes, palabras, tokens) y las etiquetas del libro abierto. |
| `/list-parts [branch] [offset] [limit]` | Las partes de una rama: identificador, posición, tamaño, alternativas, estado del resumen y una vista previa breve. |
| `/get-part <part> [branch] [offset] [length] [field]` | El texto de una parte en fragmentos (o su `instructions`, `scene`, `pov`, `present` o `reasoning`) y de qué se hizo. |
| `/list-swipes <part> [branch]` | Las alternativas (swipes) de una parte, con el número de rama que muestra cada una. |
| `/list-branches [offset] [limit]` | Las ramas del libro, numeradas, con en qué se separa cada una de las demás. |
| `/get-part-metrics <part> [branch]` | Tokens, tiempos, modelo y protocolo de una parte. |
| `/show-prompt <part> [branch] [offset] [length]` | El prompt con el que se generó una parte, tal como quedó guardado. |
| `/search-parts <query> [limit]` | Búsqueda de texto completo en las partes de todas las ramas, con fragmentos. |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | Cuántas veces y dónde aparecen nombres o palabras (por defecto: sin regex, sin distinguir mayúsculas, solo partes, rama activa). |
| `/compare-parts <a> <b> [branchA] [branchB]` | Las frases que difieren entre dos partes; `0` como `b` compara la parte `a` con el texto que produjo el modelo en primer lugar. |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | Tokens (con el tokenizador del modelo del libro), palabras y caracteres de un texto, o de una parte cuando el texto es su identificador. |
| `/list-summaries [branch]` | Los resúmenes de una rama, las partes que cubren y si siguen siendo válidos. |
| `/get-summary <part> [branch]` | El resumen de una parte. |
| `/check-summaries [branch]` | Los resúmenes desactualizados y las partes editadas que los dejaron así. |
| `/get-template-variables <scope> [part] [branch]` | Las variables de plantilla del libro (`global`) o de una parte (`part`). |
| `/list-lorebooks` | Los lorebooks del libro abierto (el raíz y sus subbooks) con sus números, número de entradas, etiquetas y subbooks. |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | Las entradas de un lorebook con su configuración y una vista previa; el filtro conserva las entradas cuyo nombre o etiqueta lo contiene. |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | Una entrada: configuración, claves, activación (recursión, contexto, rango de partes, condición), etiquetas, comentario y su texto en fragmentos. |
| `/search-lore <query> [regex] [limit]` | Las entradas de todos los lorebooks del libro cuyo nombre, texto o comentario contienen la consulta, con fragmentos. |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | Qué entradas usaría una generación para un texto, o para un turno como el que produjo una parte, y por qué (sin filtro, filtro encontrado en el texto, o en el contenido de otra entrada cuando el lorebook tiene una [profundidad de escaneo](../lorebooks/entries.md#entries-brought-in-by-other-entries), o no usada porque está desactivada, una etiqueta del libro decide, o el filtro no está en el texto), con el texto del lore y sus tokens. El texto puede ser el identificador de una parte. |
| `/list-annotations [status [part [offset [limit]]]]` | Las [anotaciones](../books/annotations.md) del libro o de una parte: las abiertas, salvo que `status` indique `resolved`, `dismissed`, `orphaned` o `all`. |
| `/get-annotation <annotation>` | Una anotación completa, con el texto alrededor de su pasaje. |
| `/get-book-statistics [range] [side]` | Lo que se hizo en el libro abierto día a día (tokens, palabras, partes, ediciones, tiempo, tamaño): véase [Estadísticas de escritura](../books/statistics.md#commands). |
| `/get-statistics [range] [by] [side]` | Lo mismo para todos tus libros, por día o por libro. No necesita ningún libro abierto. |

<a id="reading-the-story"></a>

### Leer la historia

Los comandos anteriores que leen el libro (`/get-book-info` a `/get-template-variables`) no cambian nada; responden en texto JSON, de modo que un agente también puede usarlos. Cada fila de una respuesta tiene un **`handle`**: el identificador de la parte (o del lorebook, o de la entrada), que no cambia cuando se añaden, mueven o borran partes. Una parte se indica por su identificador, o por su **posición** en una rama, siendo la primera parte de la historia la `1`; una rama, por el identificador de cualquier parte de ella, o por su **número** desde `/list-branches` (los números siguen a la creación de los extremos de las ramas, así que cambian cuando se añade o se borra una rama). Las posiciones y los números son más fáciles de escribir; los identificadores son los que debe usar un agente, y los comandos que cambian la historia solo aceptan identificadores. Sin rama, los comandos trabajan sobre la rama activa. Las listas se paginan con `offset` y `limit`, los textos largos se cortan en fragmentos (`offset`, `length`) y la respuesta indica la longitud total. Como en los demás comandos, las opciones son posicionales: para cambiar una, indica también las anteriores.

Los comandos que leen los lorebooks (`/list-lorebooks` a `/preview-lore-activation`) funcionan igual y solo llegan a los lorebooks del libro abierto: el lorebook raíz y sus subbooks. Un lorebook se indica por su **identificador** o por su **número** desde `/list-lorebooks` (la raíz es `1`), y una entrada por su **identificador** o por su **número dentro del lorebook**, en el orden del lorebook (la primera entrada es `1`); los números cambian cuando se añaden o quitan entradas o subbooks. Sin lorebook se usa el lorebook raíz.
<a id="changing-the-story-and-the-lore"></a>

### Cambiar la historia y el saber

Estos comandos cambian el libro abierto (o la copia de trabajo de un agente, que es adonde van los cambios de un agente). Una parte, un libro de saber o una entrada se indican siempre por su **identificador**, nunca por una posición o un número: estos cambian cuando se añade o se elimina algo, y una posición que era correcta al leerla puede corresponder a otra parte cuando se escribe. Las opciones siguen siendo posicionales, como antes. Un comando que elimina texto o datos es `DESTRUCTIVE` y pide confirmación (un agente se rige por su modo destructivo); añadir y cambiar no la piden. Una edición de la historia puede dejar desactualizados los resúmenes (un resumen lleva el hash de la historia que describe): la respuesta los enumera y dice qué hacer, `/revalidate-summaries` si siguen siendo válidos, `/regenerate-summary` si no.

| Comando | Qué hace |
|---------|----------|
| `/edit-part <part> <old> <new> [field]` | Reemplaza `old` por `new` en el texto de una parte; `old` debe encontrarse exactamente una vez, si no, nada cambia y el error indica cuántas veces apareció. `field` edita en su lugar los caracteres `instructions`, `scene`, `pov` o `present` del turno. |
| `/replace-part-text <part> <text> [field]` | Reemplaza todo el texto de una parte. Pide confirmación primero. |
| `/append-to-part <part> <text> [separator]` | Añade texto al final de una parte, después de una línea en blanco salvo que `separator` indique otra cosa. |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | Cambia de qué se compuso una parte, lo que reutiliza una nueva generación de la misma; un texto vacío borra un campo, y un campo omitido se conserva. |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | Escribe una parte nueva después de la última parte de la rama activa, o después de `parent` (una rama nueva); pasa a ser el final de la rama activa. |
| `/insert-part <text> <part> [before]` | Inserta una parte nueva en la historia justo después de `part` (o justo antes cuando `before` es `true`). Todo lo que seguía a ese lugar, incluidas todas las ramas, sigue ahora a la parte nueva; no se elimina ni se vuelve a generar nada, así que las demás partes conservan sus resúmenes y datos y bajan una posición. Los resúmenes que cubrían ese lugar quedan desactualizados (la respuesta indica cuáles). Al final de la rama equivale a `/create-part`. |
| `/branch-part <part>` | Copia una parte (y su resumen) junto a ella, como una variante nueva que inicia una rama, y hace que la copia pase a ser el final activo. |
| `/delete-part <part>` | Elimina una parte y su resumen; las partes que la seguían se conservan. Pide confirmación primero. |
| `/switch-branch <part>` | Hace activa la rama que pasa por una parte. |
| `/set-summary <part> <text>` | Reemplaza el texto del resumen de una parte. |
| `/remove-summary <part> [unwind]` | Elimina el resumen de una parte (`unwind` recupera lo que había sustituido un metaresumen). Pide confirmación primero. |
| `/revalidate-summaries [part]` | Indica que los resúmenes desactualizados siguen describiendo la historia (toda la rama activa, o solo la de esa parte). Úselo solo cuando lo haya comprobado. |
| `/regenerate-summary <part>` | Hace que el modelo vuelva a generar el resumen de una parte. Lento, consume tokens. |
| `/create-meta-summary <from> <to>` | Hace que el modelo fusione en uno solo los resúmenes desde la parte `from` (la más reciente) hasta `to`. Lento, consume tokens. |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | Hace que el modelo escriba la siguiente parte, como el botón **New turn**. Lento, consume tokens; una generación de un libro se ejecuta a la vez y las demás esperan. `profile` indica uno de los perfiles de modelo de un agente. |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | Hace que el modelo escriba otra versión de la última parte de una rama. Lo que se omite se toma de la parte. |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | Hace que el modelo vuelva a escribir la última parte de una rama, reemplazando su texto. Pide confirmación primero. |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | Hace que el modelo vuelva a escribir cualquier parte según tus instrucciones (véase [Rehacer una parte](../books/story-editor.md#redrafting-a-part)) y reemplaza su texto. `prompt` es una indicación de rehacer que se usa en lugar de la del libro, `profile` un perfil de modelo de un agente, `apply` `false` solo devuelve el texto. `DESTRUCTIVE`; pide confirmación primero. Lento, consume tokens. |
| `/impersonate [instructions] [outline] [prompt]` | Hace que el modelo redacte en tu lugar las instrucciones del siguiente turno (véase [Redactar las instrucciones](../books/story-editor.md#drafting-the-instructions)) y devuelve el texto; no se cambia nada. `instructions` es de qué debe tratar la parte (vacío continúa la historia por donde vaya), `outline` un plan de la historia para el modelo, `prompt` una indicación de impersonate que se usa en lugar de la del libro. Solo para ti en la barra de comandos: un [agente](../agents.md) no lo tiene disponible. Lento, consume tokens. |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | Cambia los ajustes descriptivos del libro; solo los valores que indiques. |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | Reemplaza las plantillas de indicación del libro. Cada una se comprueba antes. `DESTRUCTIVE`: una plantilla defectuosa estropea todas las generaciones posteriores. |
| `/set-book-tags [add] [remove]` | Añade y quita etiquetas del libro (separadas por comas). |
| `/set-book-history-limit [maxVersions]` | Establece cuántas versiones de una parte conserva el [historial](../books/version-history.md#how-many-versions-are-kept) del libro (`0` no conserva ninguna a partir de ahora); sin número, el libro no fija ningún límite. Siguen aplicándose los límites de las carpetas, del usuario y del administrador. No borra ninguna versión. |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | Añade una entrada a un libro de saber (la raíz si no se indica ninguno; un libro que no tiene libro de saber recibe uno con el nombre del libro). `keys` es una lista de palabras clave separadas por comas; `filteringMode` es `KEYWORDS_OR` (por defecto), `KEYWORDS_AND`, `TEXT` o `REGEX`. Un problema de la plantilla o de la expresión regular se guarda y se informa. |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | Cambia los campos que indiques. |
| `/delete-lore-entry <entry>` | Elimina una entrada. Pide confirmación primero. |
| `/move-lore-entry <entry> <lorebook>` | Mueve una entrada a otro libro de saber del libro. |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | Cambia las etiquetas y las etiquetas negativas de una entrada (separadas por comas). |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | Cambia cuándo se usa una entrada: `preventRecursion`, `recursionOnly` y `activationSummaries` (`true` / `false`), `activationContext` (`INSTRUCTIONS` o `INCLUDE_PARTS`) con `activationParts`, el rango de partes `fromMessage` / `toMessage` (0 quita un límite) y la plantilla `condition` (un texto vacío la quita). Un valor dado como texto vacío se deja como está (salvo `condition`). |
| `/create-lorebook <name> [parent]` | Crea un libro de saber que es sublibro de `parent` (la raíz si no se indica ninguno; un libro que no tiene libro de saber recibe uno primero). |
| `/set-lorebook <lorebook> [name] [enabled]` | Renombra un libro de saber o lo activa o desactiva. |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | Cambia qué libros de saber contiene un libro de saber; un libro de saber no puede contenerse a sí mismo. Para un [agente](../agents.md), `/add-subbook` también recupera un libro de saber que eliminó o sacó antes de su padre (se indica por el identificador que tenía), con sus entradas. |
| `/delete-lorebook <lorebook>` | Elimina un libro de saber con sus entradas (no la raíz). Pide confirmación primero. |
<a id="annotations"></a>

### Anotaciones

Los comandos que leen [anotaciones](../books/annotations.md) (`/list-annotations`, `/get-annotation`) responden en JSON como los demás comandos de lectura;
una anotación se indica por su **identificador**, y una parte, por su identificador en los comandos que cambian algo. Estos comandos cambian solo las notas,
nunca la historia, así que solo el borrado pide confirmación. Un agente puede usarlos todos; sus notas llegan a tu libro cuando [revisas el resultado](../books/annotations.md#annotations-and-agents).

| Comando | Qué hace |
|---------|----------|
| `/create-annotation <part> <text> [quote [type]]` | Añade una nota a una parte. `quote` son las palabras del pasaje, copiadas de la parte: la nota queda sobre ese pasaje y lo sigue cuando el texto se edita. Sin cita, o con una que no está en el texto, la nota trata sobre la parte entera (la respuesta lo indica). |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | Cambia la nota, el tipo o si el modelo recibe la nota; un texto o tipo vacío se mantiene como está. |
| `/resolve-annotation <annotation> [status]` | Establece el estado: `resolved` (predeterminado), `dismissed` o `open`. |
| `/reattach-annotation <annotation> [quote]` | Mueve una nota al pasaje que encuentra la cita en su parte; sin cita, la nota trata sobre la parte entera. |
| `/delete-annotation <annotation>` | Borra una nota (puede recuperarse desde la papelera). Pide confirmación. |
| `/generate-annotation <part> [profile]` | Hace que el modelo revise la parte como [Anotar con IA](../books/annotations.md#letting-a-model-annotate) y añade todo lo que encuentra como notas (origen *AI*; una cita que no está en el texto genera una nota sobre la parte entera). No se pide confirmación: las notas son el único cambio, y pueden resolverse, editarse o borrarse. Revisa con el modelo del libro; un agente usa su propio modelo cuando *Escribir el libro con el modelo del agente* está activado (como hacen los comandos del Generador de lore), `profile` es un perfil de modelo de un agente. Se ejecuta por sí solo, tarda y consume tokens. |

<a id="the-planner"></a>

### El planificador

Los comandos del [planificador](../books/story-editor.md#the-planner-tab) del libro. Una escena se indica por su **número** en la lista (desde 1, `/get-planned-scenes`
las muestra); los números cambian cuando una escena se mueve o se borra, así que vuelve a leer la lista después de cambiarla. Solo cambian el planificador, nunca la historia. Añadir,
mover y marcar una escena como hecha no piden confirmación; cambiar el texto de una escena y borrar una son destructivos (un [agente](../agents.md) pide confirmación como para otros
comandos destructivos).

| Comando | Qué hace |
|---------|----------|
| `/get-planned-scenes` | Las escenas con su número, título, ambientación, personaje de punto de vista, personajes presentes, instrucciones y si están hechas. |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Añade una escena al final de la lista. |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Cambia el título y los detalles que se den; lo que se omite se mantiene, y un texto vacío lo borra. |
| `/set-planned-scene-done <scene> [done]` | Marca la escena como escrita (`done` es `true` por defecto) o como no escrita. |
| `/move-planned-scene <scene> <to>` | Mueve la escena al lugar `to` (1 es el primero; si es demasiado grande, el último lugar). |
| `/delete-planned-scene <scene>` | Borra la escena. |

Los comandos que hacen que el modelo escriba o revise tardan: `/generate-annotation` y los dos resúmenes muestran la ventana **Detener**,
`/next`, `/swipe` y `/regenerate` se transmiten en la historia (ver [Comandos que tardan](#commands-that-take-time)).

<a id="commands-that-come-with-extensions"></a>

## Comandos que vienen con extensiones

Los ves en la lista mientras la extensión esté cargada; cada uno se describe en la documentación de su extensión.

| Comando | Extensión | Qué hace |
|---|---|---|
| `/list-lore-templates [<template>]` | [Generador de lore](../extensions/lore-generator.md) | Tus prompts de generación guardados, o el texto de uno. |
| `/generate-lore-entry [<prompt> ...]` | [Generador de lore](../extensions/lore-generator.md) | Genera el texto de una entrada de lore con el lore del libro de lore, lo compacta (salvo que `compact` sea `false`) y lo devuelve. No guarda nada. |
| `/compact-lore-entry <text> [...]` | [Generador de lore](../extensions/lore-generator.md) | Compacta un texto generado en un texto de entrada de lore denso y lo devuelve. No guarda nada. |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Generador de nombres](../extensions/name-generator.md) | Las categorías de nombres inventados, y nombres de una de ellas. |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Proveedor de nombres](../extensions/name-provider.md) | Las listas de nombres reales, y nombres elegidos de una de ellas. |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Proveedor de lugares](../extensions/place-provider.md) | Los tipos de lugares reales, una búsqueda por nombre, tipo y país, los lugares alrededor de un punto, y la distancia entre dos lugares. |
| `/get-arc-outline [<arc> [<notes>]]` | [Esquema de arcos](../extensions/arc-outline.md) | Los arcos y capítulos del esquema. |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Esquema de arcos](../extensions/arc-outline.md) | Añaden, cambian, mueven y borran un arco (`<arc>` es su número). |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Esquema de arcos](../extensions/arc-outline.md) | Lo mismo para los capítulos de un arco. |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Esquema de arcos](../extensions/arc-outline.md) | Los generadores de la pestaña Arcos. Devuelven el resultado y lo añaden al esquema solo con `save`. |
| `/list-characters`, `/get-character <character>` | [Seguidor de personajes](../extensions/character-tracker.md) | Los personajes del libro, y uno con su nota y sus relaciones. |
| `/create-character`, `/update-character`, `/delete-character` | [Seguidor de personajes](../extensions/character-tracker.md) | Añaden, cambian y borran un personaje. |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Seguidor de personajes](../extensions/character-tracker.md) | Las relaciones entre personajes. |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Notas de la historia](../extensions/story-notes.md) | Las páginas de las notas del libro (no las ocultas a los asistentes), una con su texto y enlaces, y una búsqueda. |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Notas de la historia](../extensions/story-notes.md) | Añaden, amplían, cambian, editan y borran una página; `note-text` se hace para el macro {%{`{{command::note-text::Title}}`}%}. |

Los comandos del Generador de lore, del Generador de nombres, del Proveedor de nombres y del Proveedor de lugares solo leen: los que generan devuelven texto y
dejan en tus manos, o en el agente que los llamó, ponerlo en una entrada. Los comandos del Esquema de arcos y del
Seguidor de personajes también cambian el esquema y los personajes del libro abierto; solo sus comandos `delete` son destructivos. Las Notas de la historia
cambian también las páginas del libro abierto; ahí `update-note` y `edit-note` son destructivos igualmente, pues reemplazan texto que ya existe.

<a id="limits"></a>

## Límites

- La línea de comandos está en la pestaña de historia del diseño de escritorio. La [vista de teléfono](../phone.md) todavía no la tiene.
- Un comando se ejecuta como tú, en tu sesión, y trabaja con el libro que tienes abierto.
