---
label: Comandos
order: 945
icon: terminal
---

# Comandos

Los comandos son acciones breves que se ejecutan escribiéndolas, en lugar de navegar por menús: buscar algo en la historia o
en los libros de lore, buscar y reemplazar, hacer una copia de seguridad. [Extensiones](../extensions/index.md) añaden sus propios comandos,
por ejemplo para generar una entrada de lore o para inventar nombres. Los mismos comandos son los que usa un [agente](../agents.md), así que
cada comando hace una cosa clara y dice qué hace: puedes leerlo mientras escribes.

La **línea de comandos** es el campo junto al menú situado debajo de la historia, en el [editor de la historia](../books/story-editor.md).

<a id="running-a-command"></a>

## Ejecutar un comando

1. Haz clic en la línea de comandos y escribe `/`. La lista muestra todos los comandos, cada uno con su nombre y una breve descripción. Si escribes más, la lista se reduce: el filtro observa el nombre del comando, su título y su descripción.
2. Elige el comando con el ratón, o con las teclas de flecha y **Enter**. Un comando que no necesita nada se ejecuta de inmediato; si uno necesita argumentos, se escribe `/nombre ` en el campo y continúas con los argumentos.
![La lista de comandos, abierta al escribir una barra](../../images/command-list.png)

3. Escribe los argumentos y pulsa **Enter**, o haz clic en el botón de reproducción junto al campo, para ejecutarlo. El campo se vacía en cuanto empieza el comando; un texto que no es un comando se mantiene, para que puedas corregirlo.

El texto que no empieza por `/` no es un comando, y no se ejecuta nada. Hacer clic fuera del campo o pulsar **Esc** nunca ejecuta nada, así que no puedes iniciar un comando por accidente.

<a id="the-help"></a>

## La ayuda

Mientras escribes, se abre una pequeña ventana encima del campo. Cubre parte de la historia, pero no mueve nada, y se cierra cuando sales del campo o ejecutas el comando. Muestra:

- qué hace el comando,
- las formas del comando (ver [Formas](#forms-of-a-command)) que encajan con lo que has escrito hasta ahora, con el argumento que estás escribiendo resaltado y su descripción,
- qué devuelve el comando,
- una indicación cuando has escrito más argumentos de los que el comando admite, o cuando ningún comando tiene ese nombre.

![La ayuda de /search-replace-lore-entries mientras se escribe su segundo argumento](../../images/command-help.png)

Mientras todavía escribes el nombre, la lista de comandos es la ayuda.

<a id="arguments"></a>

## Argumentos

Los argumentos se separan con espacios, y los espacios repetidos cuentan como uno. Para poner un espacio *dentro* de un argumento, tienes tres formas:

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

Una barra invertida delante de cualquier otra cosa se mantiene tal cual, así que `C:\books\story` se pasa sin cambios. Si abres unas comillas y nunca las cierras, todo lo que hay hasta el final de la línea pertenece a ese argumento.

Los argumentos son texto. Un comando que necesita un número, un sí o no, o una de unas pocas opciones convierte el texto por sí mismo; si el texto no encaja, el comando no se ejecuta y una notificación lo indica.

<a id="forms-of-a-command"></a>

## Formas de un comando

Un mismo comando puede aceptar distintos argumentos: `/list-parts` sin nada y `/list-parts 2` con un argumento son dos **formas** del mismo comando. Marginalia elige la forma según el número de argumentos que has escrito y según cómo son. Si ninguna forma encaja, una notificación indica que el comando no acepta esos argumentos, y la ayuda muestra las formas que sí tiene.

<a id="the-result"></a>

## El resultado

Cuando un comando devuelve algo, se muestra en una ventana encima de la línea de comandos. Ciérrala con la cruz, con **Esc**, o simplemente sigue trabajando: desaparece cuando escribes algo nuevo o ejecutas otro comando. Un comando que no devuelve nada no muestra ninguna ventana.

![El resultado de /take-backup encima de la línea de comandos](../../images/command-result.png)

Si un comando falla, o no existe, recibes una notificación con el motivo.

<a id="commands-that-take-time"></a>

## Comandos que tardan

La mayoría de los comandos responden al instante. Un comando que necesita tiempo muestra una ventana con una barra animada y un botón **Detener**. La ventana se coloca al costado y la página sigue utilizable; su ⌄ la pliega en el [panel de las ejecuciones](../books/story-editor.md#the-panel-of-the-runs), donde el comando aparece junto con lo que escribe el modelo, en todas tus páginas. Debajo de la barra, el comando puede indicar lo que está haciendo en ese momento y, en letras más pequeñas, el detalle del paso. **Detener** cancela el comando; debería devolver el control de inmediato. Los comandos que hacen que el modelo escriba un resumen o unas notas, como `/generate-annotation`, son de este tipo.

`/next`, `/swipe` y `/regenerate` escritos en la barra de comandos de la historia no tienen esa ventana: la nueva parte se escribe directamente en la historia, como al pulsar el botón redondo; el botón redondo la detiene, y la historia queda bloqueada solo como en cualquier generación (puedes seguir trabajando en el lore, en las demás partes y en las pestañas laterales). El resultado del comando aparece encima de la barra cuando la parte termina. Hasta entonces, la barra no admite un segundo comando de este tipo (conservas el texto y puedes ejecutarlo más tarde); los demás comandos funcionan.

![La ventana de un comando en ejecución](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## Comandos que modifican datos

Cada comando indica lo que hace con los datos que ya existen, y la barra de comandos lo muestra como una pequeña etiqueta junto al comando, en la lista y en la ayuda:

- **Destructivo** (rojo): el comando cambia o elimina datos existentes, como los comandos de buscar y reemplazar que reescriben el libro. Pasa el cursor sobre la etiqueta para ver la explicación.
- **No destructivo** (verde): el comando solo lee datos o añade datos nuevos, como `/take-backup`, que añade una copia de seguridad y no cambia nada más.

La etiqueta es una información para ti; no impide que el comando se ejecute. Un [agente](../agents.md) se guía por ella: no se le da un comando destructivo, o tiene que preguntarte antes, o lo ejecuta, según lo configurado para ese agente. Un comando que pide confirmación por sí mismo se describe más abajo.

<a id="commands-that-ask-first"></a>

## Comandos que piden confirmación primero

Un comando que cambia mucho puede preguntar antes de actuar. Los comandos de buscar y reemplazar no tienen vista previa, así que tras la búsqueda muestran cuántas coincidencias han encontrado, en cuántos elementos, y qué se reemplazaría por qué; solo **Sí** cambia el libro, y **No** deja todo como estaba. La pregunta permanece hasta que respondas.

![La pregunta de /search-replace-parts](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Comandos incluidos en Marginalia

| Comando                              | Qué hace                                                                       |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | Busca y reemplaza en la historia del libro abierto, como la ventana **Buscar y reemplazar** de la pestaña de historia. Las opciones son `true`/`false`, en este orden, y cada una que omitas toma el valor por defecto de esa ventana ([regex](../regular-expressions.md) no, distinguir mayúsculas sí, resúmenes sí, todas las ramas no, revalidar sí); para establecer una, escribe también las anteriores. `maxHits` rechaza un reemplazo que encuentre más coincidencias que ese número, y con `dryRun` en `true` no se cambia nada: la respuesta cuenta las coincidencias y muestra algunas, que es lo que hace un agente antes de reemplazar. No hay vista previa: tras la búsqueda se te pregunta si quieres reemplazar el número de coincidencias encontradas, y solo **Sí** cambia el libro. El reemplazo no se puede detener. |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | Lo mismo para las entradas (nombre, texto y comentario) del lorebook del libro abierto. |
| `/take-backup`                       | Hace una copia de seguridad del libro abierto; aparece en la pestaña **Copias de seguridad** del libro. Indica que no hay ningún libro abierto si no hay ninguno. |
| `/get-book-info` | El nombre, la configuración, el modelo, los totales (partes, palabras, tokens) y las etiquetas del libro abierto. |
| `/list-parts [branch] [offset] [limit]` | Las partes de una rama: identificador, posición, tamaño, swipes, estado del resumen y una vista previa breve. |
| `/get-part <part> [branch] [offset] [length] [field]` | El texto de una parte por fragmentos (o su `instructions`, `scene`, `pov`, `present` o `reasoning`) y de qué se generó. |
| `/list-swipes <part> [branch]` | Las alternativas (swipes) de una parte, con el número de rama que muestra cada una. |
| `/list-branches [offset] [limit]` | Las ramas del libro, numeradas, con el punto en que cada una se separa de las demás. |
| `/get-part-metrics <part> [branch]` | Tokens, tiempos, modelo y protocolo de una parte. |
| `/show-prompt <part> [branch] [offset] [length]` | El prompt con el que se generó una parte, tal como se guardó. |
| `/search-parts <query> [limit]` | Búsqueda de texto completo en las partes de todas las ramas, con fragmentos. |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | Cuántas veces y dónde aparecen nombres o palabras (valores por defecto: sin regex, sin distinguir mayúsculas, solo partes, rama activa). |
| `/compare-parts <a> <b> [branchA] [branchB]` | Las frases que difieren entre dos partes; `0` como `b` compara la parte `a` con el texto que el modelo produjo primero. |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | Tokens (con el tokenizador del modelo del libro), palabras y caracteres de un texto, o de una parte cuando el texto es su identificador. |
| `/list-summaries [branch]` | Los resúmenes de una rama, las partes que cubren y si siguen siendo válidos. |
| `/get-summary <part> [branch]` | El resumen de una parte. |
| `/check-summaries [branch]` | Los resúmenes desactualizados y las partes editadas que los dejaron así. |
| `/get-template-variables <scope> [part] [branch]` | Las variables de plantilla del libro (`global`) o de una parte (`part`). |
| `/list-lorebooks` | Los lorebooks del libro abierto (el raíz y sus subbooks) con sus números, número de entradas, etiquetas y subbooks. |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | Las entradas de un lorebook con sus ajustes y una vista previa; el filtro conserva las entradas cuyo nombre o etiqueta lo contiene. |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | Una entrada: ajustes, claves, activación (recursión, contexto, rango de partes, condición), etiquetas, comentario y su texto por fragmentos. |
| `/search-lore <query> [regex] [limit]` | Las entradas de todos los lorebooks del libro cuyo nombre, texto o comentario contienen la consulta, con fragmentos. |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | Qué entradas usaría una generación para un texto, o para un turno como el que produjo una parte, y por qué (sin filtro, filtro encontrado - en el texto, o en el contenido de otra entrada cuando el lorebook tiene una [profundidad de escaneo](../lorebooks/entries.md#entries-brought-in-by-other-entries) - o no usada porque está desactivada, una etiqueta del libro decide, o el filtro no está en el texto), con el texto de lore y sus tokens. El texto puede ser el identificador de una parte. |
| `/list-annotations [status [part [offset [limit]]]]` | Las [anotaciones](../books/annotations.md) del libro o de una parte: las abiertas, salvo que `status` indique `resolved`, `dismissed`, `orphaned` o `all`. |
| `/get-annotation <annotation>` | Una anotación completa, con el texto alrededor de su pasaje. |
| `/get-book-statistics [range] [side]` | Lo que se hizo en el libro abierto día a día (tokens, palabras, partes, ediciones, tiempo, tamaño): ver [Estadísticas de escritura](../books/statistics.md#commands). |
| `/get-statistics [range] [by] [side]` | Lo mismo para todos tus libros, por día o por libro. No necesita ningún libro abierto. |

<a id="reading-the-story"></a>

### Leer la historia

Los comandos anteriores que leen el libro (`/get-book-info` a `/get-template-variables`) no cambian nada; responden en texto JSON,
así que un agente también puede usarlos. Cada fila de una respuesta tiene un **`handle`**: el identificador de la parte (o del lorebook, o de la entrada),
que no cambia cuando se añaden, mueven o borran partes. Una parte se indica por su identificador, o por su **posición** en una rama,
siendo la primera parte de la historia `1`; una rama se indica por el identificador de cualquiera de sus partes, o por su **número** de `/list-branches`
(los números siguen a la creación de los extremos de las ramas, así que cambian cuando se añade o se borra una rama). Las posiciones y los números
son más fáciles de escribir; los identificadores son los que debe usar un agente, y los comandos que cambian la historia solo aceptan identificadores.
Sin rama, los comandos actúan sobre la rama activa. Las listas se paginan con `offset` y `limit`; los textos largos se cortan en fragmentos
(`offset`, `length`) y la respuesta indica la longitud total. Igual que con los demás comandos, las opciones son posicionales: para establecer una,
escribe también las anteriores.

Los comandos que leen los lorebooks (`/list-lorebooks` a `/preview-lore-activation`) funcionan igual y solo alcanzan los lorebooks del
libro abierto: el lorebook raíz y sus subbooks. Un lorebook se indica por su **identificador** o por su **número** de `/list-lorebooks` (el raíz es `1`),
y una entrada por su **identificador** o por su **número dentro del lorebook**, según el orden del lorebook (la primera entrada es `1`); los números
cambian cuando se añaden o quitan entradas o subbooks. Sin lorebook se usa el lorebook raíz.
<a id="changing-the-story-and-the-lore"></a>

### Cambiar la historia y el lore

Estos comandos cambian el libro abierto (o la copia de trabajo de un agente, que es donde van los cambios de un agente). Una parte, un lorebook o una entrada se indica siempre mediante su **identificador** (*handle*), nunca mediante una posición o un número: estos cambian cuando se añade o se elimina algo, y una posición que era correcta en el momento de leerla puede corresponder a otra parte cuando se escribe. Las opciones son posicionales, como antes. Un comando que elimina texto o datos es `DESTRUCTIVE` y pide confirmación (un agente queda cubierto por su modo destructivo); añadir y modificar no. Un cambio en la historia puede dejar desactualizados los resúmenes (un resumen lleva el hash de la historia que describe): la respuesta los enumera y dice qué hacer: `/revalidate-summaries` si siguen siendo correctos, `/regenerate-summary` si no.

| Comando | Qué hace |
|---------|----------|
| `/edit-part <part> <old> <new> [field]` | Reemplaza `old` por `new` en el texto de una parte; `old` debe encontrarse exactamente una vez, si no, no cambia nada y el error indica cuántas veces se encontró. `field` edita en su lugar los `instructions`, `scene`, `pov` o `present` del turno. |
| `/replace-part-text <part> <text> [field]` | Reemplaza todo el texto de una parte. Pide confirmación antes. |
| `/append-to-part <part> <text> [separator]` | Añade texto al final de una parte, tras una línea en blanco salvo que `separator` indique otra cosa. |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | Cambia con qué se hizo una parte, lo que reutiliza una nueva generación de ella; un texto vacío borra un campo, y un campo omitido se mantiene. |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | Escribe una parte nueva después de la última parte de la rama activa, o después de `parent` (una rama nueva); pasa a ser el final de la rama activa. |
| `/insert-part <text> <part> [before]` | Inserta una parte nueva en la historia justo después de `part` (o justo antes cuando `before` es `true`). Todo lo que seguía a ese lugar, incluidas todas las ramas, pasa a seguir a la nueva parte; no se elimina ni se vuelve a generar nada, así que las demás partes conservan sus resúmenes y datos y bajan una posición. Los resúmenes que cubrían ese lugar quedan desactualizados (la respuesta indica cuáles). Al final de la rama es lo mismo que `/create-part`. |
| `/branch-part <part>` | Copia una parte (y su resumen) junto a ella, como una nueva variante que inicia una rama, y convierte la copia en el final activo. |
| `/delete-part <part>` | Elimina una parte y su resumen; las partes que la seguían se mantienen. Pide confirmación antes. |
| `/switch-branch <part>` | Hace activa la rama que pasa por una parte. |
| `/set-summary <part> <text>` | Reemplaza el texto del resumen de una parte. |
| `/remove-summary <part> [unwind]` | Elimina el resumen de una parte (`unwind` devuelve lo que un metarresumen había reemplazado). Pide confirmación antes. |
| `/revalidate-summaries [part]` | Indica que los resúmenes desactualizados siguen describiendo la historia (toda la rama activa, o solo los de esa parte). Úsalo solo cuando lo hayas comprobado. |
| `/regenerate-summary <part>` | Hace que el modelo vuelva a crear el resumen de una parte. Lento, consume tokens. |
| `/create-meta-summary <from> <to>` | Hace que el modelo fusione en uno solo los resúmenes desde la parte `from` (la más reciente) hasta `to`. Lento, consume tokens. |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | Hace que el modelo escriba la siguiente parte, como el botón **New turn**. Lento, consume tokens; solo se genera una parte del libro a la vez y las demás esperan. `profile` indica uno de los perfiles de modelo de un agente. |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | Hace que el modelo escriba otra versión de la última parte de una rama. Lo que omitas se toma de la parte. |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | Hace que el modelo vuelva a escribir la última parte de una rama, reemplazando su texto. Pide confirmación antes. |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | Hace que el modelo vuelva a escribir cualquier parte según tus instrucciones (véase [Redactar de nuevo una parte](../books/story-editor.md#redrafting-a-part)) y reemplaza su texto. `prompt` es un prompt de redacción que se usa en lugar del del libro, `profile` un perfil de modelo de un agente, `apply` en `false` solo devuelve el texto. `DESTRUCTIVE`; pide confirmación antes. Lento, consume tokens. |
| `/impersonate [instructions] [outline] [prompt]` | Hace que el modelo redacte las instrucciones del siguiente turno en tu lugar (véase [Redactar las instrucciones](../books/story-editor.md#drafting-the-instructions)) y devuelve el texto; no se cambia nada. `instructions` es de qué debe tratar la parte (vacío continúa la historia hacia donde vaya), `outline` un plan de la historia para el modelo, `prompt` un prompt de impersonate que se usa en lugar del del libro. Solo para ti en la barra de comandos: un [agente](../agents.md) no lo tiene disponible. Lento, consume tokens. |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | Cambia los ajustes descriptivos del libro; solo los valores que indiques. |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | Reemplaza las plantillas de prompt del libro. Cada una se comprueba antes. `DESTRUCTIVE`: una plantilla mala estropea todas las generaciones posteriores. |
| `/set-book-tags [add] [remove]` | Añade y quita etiquetas del libro (separadas por comas). |
| `/set-book-history-limit [maxVersions]` | Define cuántas versiones de una parte conserva el [historial](../books/version-history.md#how-many-versions-are-kept) del libro (`0` no conserva ninguna a partir de ahora); sin número, el libro no pone límite. Los límites de las carpetas, del usuario y del administrador siguen aplicándose. No borra ninguna versión. |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | Añade una entrada a un lorebook (el raíz si no se indica ninguno; un libro que no tiene lorebook recibe uno, con el nombre del libro). `keys` es una lista de palabras clave separadas por comas; `filteringMode` es `KEYWORDS_OR` (por defecto), `KEYWORDS_AND`, `TEXT` o `REGEX`. Un problema de la plantilla o de la expresión regular se guarda y se informa. |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | Cambia los campos que indiques. |
| `/delete-lore-entry <entry>` | Elimina una entrada. Pide confirmación antes. |
| `/move-lore-entry <entry> <lorebook>` | Mueve una entrada a otro lorebook del libro. |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | Cambia las etiquetas y las etiquetas negativas de una entrada (separadas por comas). |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | Cambia cuándo se usa una entrada: `preventRecursion`, `recursionOnly` y `activationSummaries` (`true` / `false`), `activationContext` (`INSTRUCTIONS` o `INCLUDE_PARTS`) con `activationParts`, el rango de partes `fromMessage` / `toMessage` (0 quita un límite) y la plantilla `condition` (un texto vacío la quita). Un valor dado como texto vacío se deja como está (salvo `condition`). |
| `/create-lorebook <name> [parent]` | Crea un lorebook que es un sublibro de `parent` (el raíz si no se indica ninguno; un libro que no tiene lorebook recibe uno antes). |
| `/set-lorebook <lorebook> [name] [enabled]` | Renombra un lorebook o lo activa o desactiva. |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | Cambia qué lorebooks contiene un lorebook; un lorebook no puede contenerse a sí mismo. Para un [agente](../agents.md), `/add-subbook` también recupera un lorebook que había eliminado o sacado antes de su padre (lo nombra por el identificador que tenía), con sus entradas. |
| `/delete-lorebook <lorebook>` | Elimina un lorebook con sus entradas (no el raíz). Pide confirmación antes. |
<a id="annotations"></a>

### Anotaciones

Los comandos que leen [anotaciones](../books/annotations.md) (`/list-annotations`, `/get-annotation`) responden en JSON, como los demás comandos de lectura. Una anotación se indica con su **identificador**, y una parte, con su identificador para los comandos que cambian algo. Estos comandos solo cambian las notas, nunca la historia, así que solo la eliminación pide confirmación. Un agente puede usarlos todos; sus notas llegan a tu libro cuando [revisas el resultado](../books/annotations.md#annotations-and-agents).

| Comando | Qué hace |
|---------|--------------|
| `/create-annotation <part> <text> [quote [type]]` | Añade una nota a una parte. `quote` son las palabras del pasaje, copiadas de la parte: la nota se sitúa sobre ese pasaje y lo sigue cuando el texto se edita. Sin cita, o con una que no está en el texto, la nota trata sobre la parte completa (la respuesta lo indica). |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | Cambia la nota, el tipo o si el modelo recibe la nota; un texto o tipo vacío se mantiene como está. |
| `/resolve-annotation <annotation> [status]` | Establece el estado: `resolved` (por defecto), `dismissed` u `open`. |
| `/reattach-annotation <annotation> [quote]` | Mueve una nota al pasaje que la cita encuentra en su parte; sin cita, la nota trata sobre la parte completa. |
| `/delete-annotation <annotation>` | Elimina una nota (se puede restaurar desde la papelera). Pide confirmación. |
| `/generate-annotation <part> [profile]` | Hace que el modelo revise la parte como [Anotar con IA](../books/annotations.md#letting-a-model-annotate) y añade todo lo que encuentra como notas (fuente *IA*; una cita que no está en el texto genera una nota sobre la parte completa). No se pide confirmación: las notas son el único cambio, y se pueden resolver, editar o eliminar. Revisa con el modelo del libro; un agente usa su propio modelo cuando *Escribir el libro con el modelo del agente* está activado (como hacen los comandos de Lore Generator), `profile` es un perfil de modelo de un agente. Se ejecuta solo, tarda y consume tokens. |

<a id="the-planner"></a>

### El planificador

Los comandos del [planificador](../books/story-editor.md#the-planner-tab) del libro. Una escena se indica con su **número** en la lista (desde 1; `/get-planned-scenes` las muestra), y los números cambian cuando se mueve o elimina una escena, así que vuelve a leer la lista después de modificarla. Solo cambian el planificador, nunca la historia. Añadir, mover y marcar una escena como hecha no piden confirmación; cambiar el texto de una escena y eliminarla son acciones destructivas (a un [agente](../agents.md) se le pide confirmación, como con otros comandos destructivos).

| Comando | Qué hace |
|---------|--------------|
| `/get-planned-scenes` | Las escenas con su número, título, ambientación, personaje de punto de vista, personajes presentes, instrucciones y si están hechas. |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Añade una escena al final de la lista. |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Cambia el título y los detalles indicados; lo que se omite se mantiene, y un texto vacío lo borra. |
| `/set-planned-scene-done <scene> [done]` | Marca la escena como escrita (`done` es `true` por defecto) o como no escrita. |
| `/move-planned-scene <scene> <to>` | Mueve la escena al lugar `to` (1 es el primero; si es demasiado grande, va al último lugar). |
| `/delete-planned-scene <scene>` | Elimina la escena. |

Los comandos que hacen que el modelo escriba o revise tardan: `/generate-annotation` y los dos resúmenes muestran la ventana **Detener**; `/next`, `/swipe` y `/regenerate` se escriben en la historia mientras se generan (ver [Comandos que tardan](#commands-that-take-time)).

<a id="commands-that-come-with-extensions"></a>

## Comandos que vienen con extensiones

Estos aparecen en la lista mientras la extensión esté cargada; cada uno se describe en la documentación de su extensión.

| Comando | Extensión | Qué hace |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | Tus prompts de generación guardados, o el texto de uno. |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | Genera el texto de una entrada de lore con el lore del lorebook, lo compacta (salvo que `compact` sea `false`) y lo devuelve. No guarda nada. |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | Compacta un texto generado en un texto denso de entrada de lore y lo devuelve. No guarda nada. |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | Las categorías de nombres inventados, y nombres de una de ellas. |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | Las listas de nombres reales, y nombres elegidos de una de ellas. |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | Los tipos de lugares reales, una búsqueda por nombre, tipo y país, los lugares alrededor de un punto, y la distancia entre dos lugares. |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | Los arcos y capítulos del esquema. |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | Añaden, cambian, mueven y eliminan un arco (`<arc>` es su número). |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | Lo mismo para los capítulos de un arco. |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | Los generadores de la pestaña Arcs. Devuelven el resultado y lo añaden al esquema solo con `save`. |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | Los personajes del libro, y uno con su nota y sus relaciones. |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | Añaden, cambian y eliminan un personaje. |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | Las relaciones entre personajes. |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | Las páginas de las notas del libro (no las ocultas a los asistentes), una con su texto y enlaces, y una búsqueda. |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | Añaden, amplían, cambian, editan y eliminan una página; `note-text` está pensado para el macro {%{`{{command::note-text::Title}}`}%}. |

Los comandos de Lore Generator, Name generator, Name provider y Place provider solo leen: los que generan devuelven texto y dejan en tus manos, o en las del agente que los llamó, incluirlo en una entrada. Los comandos de Arc Outline y Character Tracker también cambian el esquema y los personajes del libro abierto; solo sus comandos `delete` son destructivos. Story Notes también cambia las páginas del libro abierto; allí `update-note` y `edit-note` también son destructivos, porque reemplazan texto que ya existe.

<a id="limits"></a>

## Límites

- La línea de comandos está en la pestaña de la historia del diseño de escritorio. La [vista de teléfono](../phone.md) todavía no la tiene.
- Un comando se ejecuta como tú, en tu sesión, y trabaja con el libro que tienes abierto.
