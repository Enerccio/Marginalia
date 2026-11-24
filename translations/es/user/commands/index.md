---
label: Comandos
order: 945
icon: terminal
---

# Comandos

Los comandos son pequeñas acciones que ejecutas escribiéndolas, en lugar de navegar por menús: buscar algo en la historia o en los lorebooks, buscar y reemplazar, hacer una copia de seguridad. [Extensiones](../extensions/index.md) añaden comandos propios, por ejemplo para generar una entrada de lore o para inventar nombres. Los mismos comandos son con los que trabaja un [agente](../agents.md), así que cada comando hace una cosa clara y dice qué hace: puedes leerlo mientras escribes.

La **línea de comandos** es el campo junto al menú situado debajo de la historia, en el [editor de historias](../books/story-editor.md).

<a id="running-a-command"></a>

## Ejecutar un comando

1. Haz clic en la línea de comandos y escribe `/`. La lista muestra todos los comandos, cada uno con su nombre y una breve descripción. Si escribes más, la lista se reduce: el filtro busca en el nombre del comando, su título y su descripción.
2. Elige el comando con el ratón, o con las teclas de flecha y **Enter**. Un comando que no necesita nada se ejecuta de inmediato; para uno que necesita argumentos, `/name ` se escribe en el campo y continúas con los argumentos.
![La lista de comandos, abierta al escribir una barra](../../images/command-list.png)

3. Escribe los argumentos y pulsa **Enter**, o haz clic en el botón de reproducción junto al campo, para ejecutarlo. El campo se vacía en cuanto el comando empieza; un texto que no es un comando se queda, para que puedas corregirlo.

Un texto que no empieza por `/` no es un comando, y no se ejecuta nada. Hacer clic fuera del campo o pulsar **Esc** nunca ejecuta nada, así que no puedes iniciar un comando por accidente.

<a id="the-help"></a>

## La ayuda

Mientras escribes, se abre una pequeña ventana encima del campo. Cubre parte de la historia pero no mueve nada, y se cierra cuando sales del campo o ejecutas el comando. Muestra:

- qué hace el comando,
- las formas del comando (ver [Formas](#forms-of-a-command)) que encajan con lo que has escrito hasta ahora, con el argumento que estás escribiendo ahora resaltado y su descripción,
- qué devuelve el comando,
- una indicación cuando has escrito más argumentos de los que el comando admite, o cuando ningún comando tiene ese nombre.

![La ayuda de /search-replace-lore-entries mientras se escribe su segundo argumento](../../images/command-help.png)

Mientras sigues escribiendo el nombre, la lista de comandos es la ayuda.

<a id="arguments"></a>

## Argumentos

Los argumentos se separan con espacios, y los espacios repetidos cuentan como uno. Para poner un espacio *dentro* de un argumento, tienes tres formas:

| Escribes | El comando recibe | Por qué |
|------------------------------|---------------------------------|--------------------------------------------|
| `/search-parts two\ words` | un argumento, `two words` | `\ ` (barra invertida, espacio) es un espacio |
| `/search-parts "two words"` | un argumento, `two words` | las comillas dobles mantienen todo junto |
| `/search-parts say\ "hi"` | un argumento, `say hi` | las comillas pueden empezar en mitad de un argumento |

Los demás caracteres especiales:

| Escribes | Obtienes |
|-------------------------------|----------------------------|
| `\\` | una barra invertida |
| `\"` | unas comillas dobles, con o sin comillas alrededor: `"say \"hi\""` es `say "hi"` |
| `""` | un argumento vacío |

Una barra invertida delante de cualquier otra cosa se queda tal cual, así que `C:\books\story` se pasa sin cambios. Si abres unas comillas y no las cierras, todo hasta el final de la línea pertenece a ese argumento.

Los argumentos son texto. Un comando que quiere un número, un sí/no o una de unas pocas opciones convierte el texto por sí mismo; si el texto no encaja, el comando no se ejecuta y una notificación lo dice.

<a id="forms-of-a-command"></a>

## Formas de un comando

Un mismo comando puede admitir distintos argumentos: `/list-parts` sin nada, y `/list-parts 2` con un argumento son dos **formas** del mismo comando. Marginalia elige la forma según el número de argumentos que escribiste y según cómo son. Si ninguna forma encaja, una notificación indica que el comando no acepta estos argumentos, y la ayuda muestra las formas que sí tiene.

<a id="the-result"></a>

## El resultado

Cuando un comando devuelve algo, se muestra en una ventana encima de la línea de comandos. Ciérrala con la cruz, con **Esc**, o simplemente sigue: desaparece cuando escribes algo nuevo o ejecutas otro comando. Un comando que no devuelve nada no muestra ninguna ventana.

![El resultado de /take-backup encima de la línea de comandos](../../images/command-result.png)

Si un comando falla, o no existe, recibes una notificación con el motivo.

<a id="commands-that-take-time"></a>

## Comandos que tardan

La mayoría de los comandos responden al instante. Un comando que necesita tiempo muestra una ventana con una barra animada y un botón **Stop**. La ventana se queda a un lado y la página sigue utilizable; su ⌄ la pliega dentro del [panel de ejecuciones](../books/story-editor.md#the-panel-of-the-runs), donde el comando aparece junto con lo que escribe el modelo, en todas tus páginas. Debajo de la barra, el comando puede indicar qué está haciendo en ese momento y, en letra más pequeña, el detalle del paso. **Stop** cancela el comando; debería devolver el control de inmediato. Los comandos que hacen que el modelo escriba un resumen o notas, como `/generate-annotation`, son de este tipo.

`/next`, `/swipe` y `/regenerate` escritos en la barra de comandos de la historia no tienen esa ventana: la nueva parte se va escribiendo en la historia como cuando pulsas el botón redondo, el botón redondo la detiene, y la historia queda retenida igual que en cualquier generación (puedes seguir trabajando en el lore, las otras partes y las pestañas laterales). El resultado del comando aparece encima de la barra cuando la parte termina. Hasta entonces, la barra no admite un segundo comando de estos (conservas el texto y puedes ejecutarlo más tarde); los demás comandos funcionan.

![La ventana de un comando en ejecución](../../images/command-running.png)

<a id="commands-that-change-data"></a>

## Comandos que cambian datos

Cada comando dice qué hace con los datos que ya existen, y la barra de comandos lo muestra con una pequeña etiqueta junto al comando, en la lista y en la ayuda:

- **Destructivo** (rojo) - el comando cambia o elimina datos existentes, como los comandos de buscar y reemplazar que reescriben el libro. Señala la etiqueta para ver la explicación.
- **No destructivo** (verde) - el comando solo lee datos o añade datos nuevos, como `/take-backup`, que añade una copia de seguridad y no cambia nada más.

La etiqueta es información para ti; no impide que el comando se ejecute cuando lo lanzas. Un [agente](../agents.md) se guía por ella: no se le da un comando destructivo, o tiene que preguntarte antes, o lo ejecuta, según lo establecido para ese agente. Un comando que pide confirmación por sí mismo se describe a continuación.

<a id="commands-that-ask-first"></a>

## Comandos que preguntan antes

Un comando que cambia mucho puede preguntar antes de actuar. Los comandos de buscar y reemplazar no tienen vista previa, así que tras la búsqueda muestran cuántas coincidencias encontraron, en cuántos elementos, y qué se reemplazaría por qué; solo **Yes** cambia el libro, y **No** deja todo como estaba. La pregunta permanece hasta que contestas.

![La pregunta de /search-replace-parts](../../images/command-confirm.png)
<a id="commands-that-come-with-marginalia"></a>

## Comandos incluidos en Marginalia

| Comando                              | Qué hace                                                                       |
|--------------------------------------|------------------------------------------------------------------------------------|
| `/search-replace-parts <search> <replace> [regex] [caseSensitive] [summaries] [allBranches] [revalidateSummaries] [maxHits] [dryRun]` | Busca y reemplaza en la historia del libro abierto, como la ventana **Search and replace** de la pestaña de la historia. Las opciones son `true`/`false`, en este orden, y cada una que omitas toma el valor por defecto de esa ventana ([expresión regular](../regular-expressions.md) no, distinguir mayúsculas sí, resúmenes sí, todas las ramas no, revalidar sí); para establecer una, indícalas también antes. `maxHits` rechaza un reemplazo que encuentre más coincidencias que esa cifra, y con `dryRun` `true` no se cambia nada: la respuesta cuenta las coincidencias y muestra algunas, que es lo que hace un agente antes de reemplazar. No hay vista previa: tras la búsqueda se pregunta si reemplazar el número de coincidencias encontradas, y solo **Yes** cambia el libro. El reemplazo no se puede detener. |
| `/search-replace-lore-entries <search> <replace> [regex] [caseSensitive] [maxHits] [dryRun]` | Lo mismo para las entradas (nombre, texto y comentario) del lorebook del libro abierto. |
| `/take-backup`                       | Hace una copia de seguridad del libro abierto; aparece en la pestaña **Backups** del libro. Indica que no hay ningún libro abierto cuando no hay ninguno. |
| `/get-book-info` | El nombre, los ajustes, el modelo, los totales (partes, palabras, tokens) y las etiquetas del libro abierto. |
| `/list-parts [branch] [offset] [limit]` | Las partes de una rama: handle, posición, tamaño, swipes, estado del resumen y una vista previa breve. |
| `/get-part <part> [branch] [offset] [length] [field]` | El texto de una parte por fragmentos (o su `instructions`, `scene`, `pov`, `present` o `reasoning`) y lo que se usó para crearla. |
| `/list-swipes <part> [branch]` | Las alternativas (swipes) de una parte, con el número de rama que muestra cada una. |
| `/list-branches [offset] [limit]` | Las ramas del libro, numeradas, con el punto en que cada una se separa de las demás. |
| `/get-part-metrics <part> [branch]` | Tokens, tiempos, modelo y protocolo de una parte. |
| `/show-prompt <part> [branch] [offset] [length]` | El prompt con el que se generó una parte, tal como se guardó. |
| `/search-parts <query> [limit]` | Búsqueda de texto completo en las partes de todas las ramas, con fragmentos. |
| `/find-occurrences <terms> [regex] [caseSensitive] [summaries] [allBranches] [limit]` | Con qué frecuencia y dónde aparecen nombres o palabras (por defecto: sin regex, sin distinguir mayúsculas, solo partes, rama activa). |
| `/compare-parts <a> <b> [branchA] [branchB]` | Las frases que difieren entre dos partes; `0` como `b` compara la parte `a` con el texto que el modelo produjo primero. |
| `/count-tokens <text>`, `/count-tokens <part> <branch>` | Tokens (con el tokenizador del modelo del libro), palabras y caracteres de un texto, o de una parte cuando el texto es su handle. |
| `/list-summaries [branch]` | Los resúmenes de una rama, las partes que cubren y si siguen siendo válidos. |
| `/get-summary <part> [branch]` | El resumen de una parte. |
| `/check-summaries [branch]` | Los resúmenes desactualizados y las partes editadas que los dejaron así. |
| `/get-template-variables <scope> [part] [branch]` | Las variables de plantilla del libro (`global`) o de una parte (`part`). |
| `/list-lorebooks` | Los lorebooks del libro abierto (el raíz y sus subbooks) con sus números, número de entradas, etiquetas y subbooks. |
| `/list-lore-entries [lorebook] [filter] [offset] [limit]` | Las entradas de un lorebook con sus ajustes y una vista previa; el filtro conserva las entradas cuyo nombre o etiqueta lo contiene. |
| `/get-lore-entry <entry> [lorebook] [offset] [length]` | Una entrada: ajustes, claves, activación (recursión, contexto, rango de partes, condición), etiquetas, comentario y su texto por fragmentos. |
| `/search-lore <query> [regex] [limit]` | Las entradas de todos los lorebooks del libro cuyo nombre, texto o comentario contienen la consulta, con fragmentos. |
| `/preview-lore-activation <text> [limit]`, `/preview-lore-activation <part> <branch> <limit>` | Qué entradas usaría una generación para un texto, o para un turno como el que creó una parte, y por qué (sin filtro; filtro encontrado, en el texto o en el contenido de otra entrada cuando el lorebook tiene una [profundidad de escaneo](../lorebooks/entries.md#entries-brought-in-by-other-entries); o no usada porque está desactivada, una etiqueta del libro lo decide, o el filtro no está en el texto), con el texto del lore y sus tokens. El texto puede ser el handle de una parte. |
| `/list-annotations [status [part [offset [limit]]]]` | Las [anotaciones](../books/annotations.md) del libro o de una parte: las abiertas, salvo que `status` indique `resolved`, `dismissed`, `orphaned` o `all`. |
| `/get-annotation <annotation>` | Una anotación completa, con el texto alrededor de su pasaje. |
| `/get-book-statistics [range] [side]` | Lo que se hizo en el libro abierto día a día (tokens, palabras, partes, ediciones, tiempo, tamaño): véase [Estadísticas de escritura](../books/statistics.md#commands). |
| `/get-statistics [range] [by] [side]` | Lo mismo para todos tus libros, por día o por libro. No necesita ningún libro abierto. |

<a id="reading-the-story"></a>

### Leer la historia

Los comandos anteriores que leen el libro (`/get-book-info` a `/get-template-variables`) no cambian nada; responden en texto JSON,
así que un agente también puede usarlos. Cada fila de una respuesta tiene un **`handle`**: el identificador de la parte (o del lorebook, o de la entrada), que no cambia al añadir, mover o borrar partes. Una parte se indica por su handle, o por su **posición** en una rama, siendo la primera parte de la historia la `1`; una rama, por el handle de cualquiera de sus partes, o por su **número** desde `/list-branches` (los números siguen a la creación de los extremos de las ramas, así que cambian al añadir o borrar una rama). Las posiciones y los números son más fáciles de escribir, los handles son los que debe usar un agente, y los comandos que cambian la historia solo aceptan handles. Sin rama, los comandos actúan sobre la rama activa. Las listas se paginan con `offset` y `limit`, los textos largos se cortan en fragmentos (`offset`, `length`) y la respuesta indica la longitud total. Como con los demás comandos, las opciones son posicionales: para establecer una, indícalas también antes.

Los comandos que leen los lorebooks (`/list-lorebooks` a `/preview-lore-activation`) funcionan igual y solo alcanzan los lorebooks del libro abierto: el lorebook raíz y sus subbooks. Un lorebook se indica por su **handle** o por su **número** desde `/list-lorebooks` (el raíz es `1`), y una entrada por su **handle** o por su **número en el lorebook**, según el orden del lorebook (la primera entrada es `1`); los números cambian al añadir o quitar entradas o subbooks. Sin lorebook se usa el lorebook raíz.
<a id="changing-the-story-and-the-lore"></a>

### Cambiar la historia y el lorebook

Estos comandos cambian el libro abierto (o la copia de trabajo de un agente, que es donde van a parar los cambios de un agente). Una parte, un lorebook o una entrada se indica siempre por su **identificador**, nunca por una posición o un número, porque las posiciones cambian cuando se añade o se elimina algo, y una posición que era correcta al leerla puede corresponder a otra parte cuando se escribe. Las opciones son posicionales, como antes. Un comando que elimina texto o datos es `DESTRUCTIVE` y pide confirmación (un agente se rige por su modo destructivo); añadir y cambiar no la piden. Una edición de la historia puede dejar desactualizados los resúmenes (un resumen lleva el hash de la historia que describe): la respuesta los enumera e indica qué hacer, `/revalidate-summaries` si siguen siendo correctos, `/regenerate-summary` si no.

| Comando | Qué hace |
|---------|----------|
| `/edit-part <part> <old> <new> [field]` | Reemplaza `old` por `new` en el texto de una parte; `old` debe encontrarse exactamente una vez, si no, no cambia nada y el error indica cuántas veces apareció. `field` edita en su lugar los caracteres `instructions`, `scene`, `pov` o `present` del turno. |
| `/replace-part-text <part> <text> [field]` | Reemplaza todo el texto de una parte. Pide confirmación antes. |
| `/append-to-part <part> <text> [separator]` | Añade texto al final de una parte, tras una línea en blanco salvo que `separator` indique otra cosa. |
| `/set-part-fields <part> [instructions] [scene] [pov] [present]` | Cambia de qué se generó una parte, algo que reutiliza una nueva generación de esa parte; un texto vacío borra un campo, y un campo que no se incluye se conserva. |
| `/create-part <text> [parent] [instructions] [scene] [pov] [present]` | Escribe una parte nueva después de la última parte de la rama activa, o después de `parent` (una rama nueva); pasa a ser el final de la rama activa. |
| `/insert-part <text> <part> [before]` | Inserta una parte nueva en la historia justo después de `part` (o justo antes, cuando `before` es `true`). Todo lo que seguía a ese punto, incluidas todas las ramas, sigue ahora a la parte nueva; no se borra ni se vuelve a generar nada, así que las demás partes conservan sus resúmenes y datos y bajan una posición. Los resúmenes que cubrían ese punto quedan desactualizados (la respuesta indica cuáles). Al final de la rama, equivale a `/create-part`. |
| `/branch-part <part>` | Copia una parte (y su resumen) junto a ella, como una variante nueva que inicia una rama, y convierte la copia en el final activo. |
| `/delete-part <part>` | Elimina una parte y su resumen; las partes que le seguían se conservan. Pide confirmación antes. |
| `/switch-branch <part>` | Hace activa la rama que pasa por una parte. |
| `/set-summary <part> <text>` | Reemplaza el texto del resumen de una parte. |
| `/remove-summary <part> [unwind]` | Elimina el resumen de una parte (`unwind` recupera lo que había sustituido un metarresumen). Pide confirmación antes. |
| `/revalidate-summaries [part]` | Indica que los resúmenes desactualizados siguen describiendo la historia (toda la rama activa, o solo la de esa parte). Úsalo solo cuando lo hayas comprobado. |
| `/regenerate-summary <part>` | Hace que el modelo vuelva a generar el resumen de una parte. Es lento y consume tokens. |
| `/create-meta-summary <from> <to>` | Hace que el modelo fusione los resúmenes desde la parte `from` (la más reciente) hasta `to` en uno solo. Es lento y consume tokens. |
| `/next [scene] [pov] [present] [instructions] [after] [profile]` | Hace que el modelo escriba la parte siguiente, igual que el botón **Nuevo turno**. Es lento y consume tokens; solo se genera un libro a la vez y las demás esperan. `profile` indica uno de los perfiles de modelo de un agente. |
| `/swipe [part] [scene] [pov] [present] [instructions] [profile]` | Hace que el modelo escriba otra versión de la última parte de una rama. Lo que no se indica se toma de la parte. |
| `/regenerate [part] [scene] [pov] [present] [instructions] [profile]` | Hace que el modelo vuelva a escribir la última parte de una rama, reemplazando su texto. Pide confirmación antes. |
| `/redraft-part <part> [instructions] [prompt] [profile] [apply]` | Hace que el modelo vuelva a escribir cualquier parte según tus instrucciones (consulta [Reescribir una parte](../books/story-editor.md#redrafting-a-part)) y reemplaza su texto. `prompt` es un prompt de reescritura que se usa en lugar del del libro, `profile` un perfil de modelo de un agente, `apply` `false` solo devuelve el texto. `DESTRUCTIVE`; pide confirmación antes. Es lento y consume tokens. |
| `/impersonate [instructions] [outline] [prompt]` | Hace que el modelo redacte en tu lugar las instrucciones del turno siguiente (consulta [Redactar las instrucciones](../books/story-editor.md#drafting-the-instructions)) y devuelve el texto; no cambia nada. `instructions` es de qué debe tratar la parte (vacío continúa la historia por donde vaya), `outline` un plan de la historia para el modelo, `prompt` un prompt de suplantación que se usa en lugar del del libro. Solo está disponible para ti en la barra de comandos: un [agente](../agents.md) no lo tiene. Es lento y consume tokens. |
| `/set-book-info [name] [description] [pov] [tense] [style] [language]` | Cambia los ajustes descriptivos del libro; solo los valores que indiques. |
| `/set-book-prompts [template] [userPrompt] [summaryPrompt] [metaSummaryPrompt]` | Reemplaza las plantillas de prompt del libro. Cada una se comprueba antes. `DESTRUCTIVE`: una plantilla defectuosa estropea todas las generaciones posteriores. |
| `/set-book-tags [add] [remove]` | Añade y quita etiquetas del libro (separadas por comas). |
| `/set-book-history-limit [maxVersions]` | Indica cuántas versiones de una parte guarda el [historial](../books/version-history.md#how-many-versions-are-kept) del libro (`0` no guarda ninguna a partir de ese momento); sin número, el libro no pone límite. Los límites de las carpetas, del usuario y del administrador siguen aplicando. No borra ninguna versión. |
| `/create-lore-entry <name> <payload> [lorebook] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode] [tags]` | Añade una entrada a un lorebook (la raíz cuando no se indica ninguno; un libro que no tiene lorebook recibe uno, con el nombre del libro). `keys` es una lista de palabras clave separadas por comas; `filteringMode` es `KEYWORDS_OR` (por defecto), `KEYWORDS_AND`, `TEXT` o `REGEX`. Un problema de la plantilla o de la expresión regular se guarda y se informa. |
| `/update-lore-entry <entry> [name] [payload] [comment] [enabled] [order] [keys] [filteringMode] [insertionMode]` | Cambia los campos que indiques. |
| `/delete-lore-entry <entry>` | Elimina una entrada. Pide confirmación antes. |
| `/move-lore-entry <entry> <lorebook>` | Mueve una entrada a otro lorebook del libro. |
| `/set-lore-entry-tags <entry> [add] [remove] [addNegative] [removeNegative]` | Cambia las etiquetas y las etiquetas negativas de una entrada (separadas por comas). |
| `/set-lore-entry-activation <entry> [preventRecursion] [recursionOnly] [activationContext] [activationParts] [activationSummaries] [fromMessage] [toMessage] [condition]` | Cambia cuándo se usa una entrada: `preventRecursion`, `recursionOnly` y `activationSummaries` (`true` / `false`), `activationContext` (`INSTRUCTIONS` o `INCLUDE_PARTS`) con `activationParts`, el rango de partes `fromMessage` / `toMessage` (0 quita un límite) y la plantilla `condition` (un texto vacío la quita). Un valor dado como texto vacío se deja como está (salvo `condition`). |
| `/create-lorebook <name> [parent]` | Crea un lorebook que es un sublibro de `parent` (la raíz cuando no se indica ninguno; un libro que no tiene lorebook recibe uno primero). |
| `/set-lorebook <lorebook> [name] [enabled]` | Renombra un lorebook o lo activa y desactiva. |
| `/add-subbook <lorebook> <subbook>`, `/remove-subbook <lorebook> <subbook>` | Cambia qué lorebooks contiene un lorebook; un lorebook no puede contenerse a sí mismo. Para un [agente](../agents.md), `/add-subbook` también recupera un lorebook que eliminó o sacó antes de su padre (se indica por el identificador que tenía), con sus entradas. |
| `/delete-lorebook <lorebook>` | Elimina un lorebook con sus entradas (no la raíz). Pide confirmación antes. |
<a id="annotations"></a>

### Anotaciones

Los comandos que leen [anotaciones](../books/annotations.md) (`/list-annotations`, `/get-annotation`) responden en JSON, como los demás comandos de lectura;
una anotación se indica por su **identificador** y una parte por su identificador en los comandos que cambian algo. Estos comandos solo cambian las notas,
nunca la historia, así que solo el borrado pide confirmación. Un agente puede usarlos todos; sus notas llegan a tu libro cuando [revisas el resultado](../books/annotations.md#annotations-and-agents).

| Comando | Qué hace |
|---------|--------------|
| `/create-annotation <part> <text> [quote [type]]` | Añade una nota a una parte. `quote` son las palabras del pasaje, copiadas de la parte: la nota se sitúa sobre ese pasaje y lo sigue cuando el texto se edita. Sin cita, o con una que no está en el texto, la nota se refiere a toda la parte (la respuesta lo indica). |
| `/update-annotation <annotation> <text> [type [sendToPrompt]]` | Cambia la nota, el tipo o si el modelo recibe la nota; un texto o tipo vacío se mantiene como está. |
| `/resolve-annotation <annotation> [status]` | Establece el estado: `resolved` (por defecto), `dismissed` u `open`. |
| `/reattach-annotation <annotation> [quote]` | Mueve una nota al pasaje que encuentra la cita dentro de su parte; sin cita, la nota se refiere a toda la parte. |
| `/delete-annotation <annotation>` | Borra una nota (se puede restaurar desde la papelera). Pide confirmación antes. |
| `/scan-phrases [from to]` | Revisa las partes de la rama activa con la [lista de vigilancia de frases](../books/annotations.md) del libro y añade una nota por cada coincidencia nueva (sin modelo). Sin argumentos, toda la rama; con dos partes, las partes desde una hasta la otra. Responde cuántas notas ha creado. |
| `/generate-annotation <part> [profile]` | Hace que el modelo revise la parte como [Annotate with AI](../books/annotations.md#letting-a-model-annotate) y añade todo lo que encuentre como notas (origen *AI*; una cita que no está en el texto crea una nota sobre toda la parte). No pide confirmación: las notas son el único cambio, y pueden resolverse, editarse o borrarse. Revisa con el modelo del libro; un agente usa su propio modelo cuando *Write the book with the model of the agent* está activado (como hacen los comandos de Lore Generator); `profile` es un perfil de modelo de un agente. Se ejecuta solo, tarda y consume tokens. |

<a id="the-planner"></a>

### El planificador

Los comandos del [planificador](../books/story-editor.md#the-planner-tab) del libro. Una escena se indica por su **número** en la lista (desde 1; `/get-planned-scenes`
muestra las escenas). Los números cambian cuando una escena se mueve o se borra, así que vuelve a leer la lista después de cambiarla. Solo cambian el planificador, nunca la historia. Añadir,
mover y marcar una escena como terminada no piden confirmación; cambiar el texto de una escena y borrar una son destructivos (a un [agente](../agents.md) se le pide confirmación como para otros
comandos destructivos).

| Comando | Qué hace |
|---------|--------------|
| `/get-planned-scenes` | Las escenas con su número, título, ambientación, personaje con el punto de vista, personajes presentes, instrucciones y si están terminadas. |
| `/add-planned-scene <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Añade una escena al final de la lista. |
| `/update-planned-scene <scene> <title> [instructions [sceneSetting [povCharacter [presentCharacters]]]]` | Cambia el título y los datos que se indiquen; lo que se omite se conserva, y un texto vacío lo borra. |
| `/set-planned-scene-done <scene> [done]` | Marca la escena como escrita (`done` es `true` por defecto) o como no escrita. |
| `/move-planned-scene <scene> <to>` | Mueve la escena al lugar `to` (1 es el primero; si el número es demasiado grande, el último lugar). |
| `/delete-planned-scene <scene>` | Borra la escena. |

Los comandos que hacen que el modelo escriba o revise tardan: `/generate-annotation` y los dos resúmenes muestran la ventana **Stop**;
`/next`, `/swipe` y `/regenerate` se escriben en la historia según se generan (ver [Comandos que tardan](#commands-that-take-time)).

<a id="commands-that-come-with-extensions"></a>

## Comandos que vienen con las extensiones

Los ves en la lista mientras la extensión esté cargada; cada uno se describe en la documentación de su extensión.

| Comando | Extensión | Qué hace |
|---|---|---|
| `/list-lore-templates [<template>]` | [Lore Generator](../extensions/lore-generator.md) | Tus prompts de generación guardados, o el texto de uno. |
| `/generate-lore-entry [<prompt> ...]` | [Lore Generator](../extensions/lore-generator.md) | Genera el texto de una entrada de lore con el lore del lorebook, lo compacta (salvo que `compact` sea `false`) y lo devuelve. No guarda nada. |
| `/compact-lore-entry <text> [...]` | [Lore Generator](../extensions/lore-generator.md) | Compacta un texto generado en una entrada de lore densa y la devuelve. No guarda nada. |
| `/list-name-categories [<filter>]`, `/generate-names <category> <count> [...]` | [Name generator](../extensions/name-generator.md) | Las categorías de nombres inventados, y nombres de una de ellas. |
| `/list-real-name-lists`, `/pick-real-names <list> <count>` | [Name provider](../extensions/name-provider.md) | Las listas de nombres reales, y nombres escogidos de una de ellas. |
| `/list-place-types`, `/search-places <query> [...]`, `/places-near <place> <radius_km> [...]`, `/distance-between <from> <to>` | [Place provider](../extensions/place-provider.md) | Los tipos de lugares reales, una búsqueda por nombre, tipo y país, los lugares alrededor de un punto y la distancia entre dos lugares. |
| `/get-arc-outline [<arc> [<notes>]]` | [Arc Outline](../extensions/arc-outline.md) | Los arcos y capítulos del esquema. |
| `/add-arc`, `/update-arc`, `/move-arc`, `/delete-arc` | [Arc Outline](../extensions/arc-outline.md) | Añaden, cambian, mueven y borran un arco (`<arc>` es su número). |
| `/add-outline-chapter`, `/update-outline-chapter`, `/move-outline-chapter`, `/delete-outline-chapter` | [Arc Outline](../extensions/arc-outline.md) | Lo mismo para los capítulos de un arco. |
| `/generate-arc`, `/generate-outline-chapters <arc>`, `/import-arc-from-story` | [Arc Outline](../extensions/arc-outline.md) | Los generadores de la pestaña Arcs. Devuelven el resultado y solo lo añaden al esquema con `save`. |
| `/list-characters`, `/get-character <character>` | [Character Tracker](../extensions/character-tracker.md) | Los personajes del libro, y uno con su nota y sus relaciones. |
| `/create-character`, `/update-character`, `/delete-character` | [Character Tracker](../extensions/character-tracker.md) | Añaden, cambian y borran un personaje. |
| `/list-character-relations`, `/add-character-relation`, `/update-character-relation`, `/delete-character-relation` | [Character Tracker](../extensions/character-tracker.md) | Las relaciones entre personajes. |
| `/list-notes`, `/get-note <note>`, `/search-notes <query>` | [Story Notes](../extensions/story-notes.md) | Las páginas de las notas del libro (no las ocultas a los asistentes), una con su texto y enlaces, y una búsqueda. |
| `/create-note`, `/append-to-note`, `/update-note`, `/edit-note`, `/delete-note`, `/note-text <note>` | [Story Notes](../extensions/story-notes.md) | Añaden, amplían, cambian, editan y borran una página; `note-text` está hecho para la macro {%{`{{command::note-text::Title}}`}%}. |

Los comandos de Lore Generator, Name generator, Name provider y Place provider solo leen: los que generan devuelven texto y
dejan en tus manos, o en las del agente que los llamó, meterlo en una entrada. Los comandos de Arc Outline y de
Character Tracker también cambian el esquema y los personajes del libro abierto; solo sus comandos `delete` son destructivos. Story Notes
cambia también las páginas del libro abierto; ahí `update-note` y `edit-note` también son destructivos, porque reemplazan texto que ya existe.

<a id="limits"></a>

## Límites

- La línea de comandos está en la pestaña de la historia del diseño de escritorio. La [vista de teléfono](../phone.md) todavía no la tiene.
- Un comando se ejecuta como tú, en tu sesión, y trabaja con el libro que tienes abierto.
