---
label: Seguimiento de estado
order: 29
---

# Seguimiento de estado

Mantiene el registro del **estado** actual de tus personajes: qué llevan puesto, qué portan, el arma que tienen a mano, sus heridas, su ánimo o cualquier otra cosa que definas. Introduce en el prompt el estado de los personajes de la escena. Un modelo olvida con facilidad que la espada se perdió hace dos capítulos o que la capa está empapada. Con State Tracker, el prompt indica, para cada personaje seguido en la escena, qué es cierto de él en este momento.

!!!
Tú eliges quién se sigue y qué se sigue de cada uno. Solo se describe al modelo a los personajes que añadas a la pestaña, y solo mientras estén entre los personajes presentes (*present characters*) de la parte que se escribe. Puedes llevar el estado tú mismo, o dejar que un modelo lea la historia y proponga los cambios (véase [Seguir la historia](#following-the-story)).
!!!

<a id="categories"></a>

## Categorías

Una **categoría** es una cosa que puede seguirse de un personaje. Cada categoría tiene:

- un **nombre**, que es también lo que el prompt llama (*Wearing*, *Weapon at hand*, *Carrying*);
- un **tipo**:
  - **Single value**: un valor corto, y uno nuevo reemplaza al anterior (el arma a mano, la ubicación).
  - **List**: entradas que se añaden y se quitan una a una (lo que se lleva, las heridas).
  - **Description**: un texto que se reescribe entero (la ropa, el ánimo).
- una **instrucción para el modelo**: qué pertenece a la categoría y qué no. El modelo la lee cuando sigue la historia.
- **Only what the text says**: activado, el valor cambia solo cuando el texto lo dice; desactivado, también con consecuencias obvias (mojado tras un baño).
- **Say "none" when empty**: el prompt dice *none* cuando el personaje no tiene valor (un arma perdida) en lugar de omitir la categoría.

<a id="my-categories"></a>

### Mis categorías

Tus propias categorías están en **Settings → Extension Settings → State Tracker**. Los libros copian sus categorías de estas. Hasta que las cambies, tienes cinco integradas: *Wearing*, *Weapon at hand*, *Carrying*, *Injuries* y *Mood*. Añade, edita y borra categorías ahí y pulsa **Save** en la pantalla de Ajustes. **Reset to the built-in categories** devuelve las cinco integradas.

<a id="the-categories-of-a-book"></a>

### Las categorías de un libro

Un libro guarda su **copia propia** de cada categoría que usa, de modo que una copia de seguridad o una exportación del libro lo contiene todo, y borrar una de tus categorías nunca rompe un libro. El botón **Categories of this book** de la pestaña State las enumera:

- **Add from my categories** copia una de tus categorías al libro.
- **New category for this book** crea una categoría que existe solo en este libro (la carga de un barco, una afinidad mágica). Esa categoría aparece marcada como *Only in this book*. **Save to my categories** la añade a tus categorías, para que otros libros también puedan usarla.
- Una copia que ya no es igual a tu categoría se marca como *Differs from your category*. **Update from my categories** toma tu versión. Nada se actualiza sin ti, porque una instrucción cambiada cambia lo que hace el modelo.
- Editar una categoría la cambia solo en este libro. Su tipo no puede cambiar mientras haya personajes con valores en ella.
- Borrar una categoría del libro borra los valores de todos los personajes en ella.

<a id="the-state-tab"></a>

## La pestaña State

La barra lateral izquierda del [editor de la historia](../books/story-editor.md) tiene una pestaña **State**. Todo se guarda en el libro a medida que lo cambias.

![La pestaña State con un personaje seguido](../../images/plugin-statetracker.png)

![El historial de un valor](../../images/plugin-statetracker-history.png)

**Track a character** pide un **name**, opcionalmente **aliases** (los otros nombres con los que aparece el personaje en el campo *Present Characters* de una parte, separados por comas), y las **tracked categories**. La lista ofrece las categorías del libro y las tuyas; elegir una de las tuyas la copia al libro. Un caballo puede seguir solo *Injuries* mientras la heroína sigue todo.

**Import from the Character Tracker** (el icono de persona junto a **Track a character**) empieza a seguir personajes del [Character Tracker](character-tracker.md) del mismo libro. Lista los personajes que el libro aún no sigue (por nombre o alias) con sus frases de presentación, todos marcados. Desmarca los que quieras dejar fuera, marca las **tracked categories** que recibe cada personaje importado (las del libro y las tuyas, como antes) y pulsa **Import**. Solo se toman los nombres; un personaje puede recibir otras categorías después con el icono del lápiz.

Cada personaje seguido tiene una tarjeta, cerrada al principio. Ábrela con la flecha de la tarjeta para ver sus categorías, cada una con su valor o valores actuales:

- **+** fija el valor de una categoría de valor único o de descripción (el valor actual pasa al historial) o añade una entrada a una lista.
- El lápiz edita un valor; la cruz lo **quita**: deja de ser cierto, así que pasa al historial y no se envía al modelo.
- El candado marca un valor como **locked** (una cicatriz, la espada de familia). La [limpieza](#tidying-the-state) deja en paz los valores bloqueados; la historia sí puede cambiarlos.
- **History** muestra los valores pasados de la categoría y por qué terminaron (*replaced*, *removed* o *tidied*) y, cuando el modelo los terminó, la parte que leyó eso. **Restore** vuelve a hacer actual un valor pasado (en una categoría de valor único o de descripción, el actual pasa al historial en su lugar). Delete borra un valor para siempre.

El lápiz del personaje edita su nombre, alias y categorías. Desmarcar una categoría la oculta pero conserva sus valores; volver a marcarla los devuelve. La papelera deja de seguir al personaje y borra todo su estado.
<a id="following-the-story"></a>

## Seguir la historia

El panel **Processing**, en la parte superior de la pestaña State, permite que un modelo lea la historia y determine qué ha cambiado: una capa empapada en el río, un farol entregado a otra persona, una espada desenvainada, una carta perdida. Lee las partes de la rama activa en orden, desde el **Start point** (por defecto, el inicio del libro; las partes anteriores a él nunca se leen), y recuerda hasta dónde llegó.

![La configuración del procesamiento](../../images/plugin-statetracker-settings.png)

![La revisión de los cambios propuestos](../../images/plugin-statetracker-review.png)

- **Process now** lee las partes que todavía no se han procesado; **Process next part** solo la siguiente. Una serie larga pregunta primero cuántas solicitudes va a necesitar. Cuando te desplazas por la lista de personajes más allá de estos botones, **Process now**, **Process next part** y **Tidy the state** también aparecen en la barra superior de la pestaña.
- El modelo recibe los personajes rastreados que aparecen en las partes, con sus categorías, la instrucción de cada una y los valores actuales, y responde con los cambios. Cada cambio necesita una cita de la parte. Los cambios que no encajan (una categoría que el personaje no rastrea, un valor que ya tiene, una cita de una parte que no existe) se descartan.
- Un panel muestra el progreso y lo que el modelo escribe mientras lo escribe; puedes seguir leyendo y escribiendo. Con **Ask before applying** activado (por defecto), la ejecución espera a que decidas: **Review changes** muestra los cambios por personaje, con el valor final tachado y el nuevo valor, que puedes corregir, y la cita. Desmarca lo que esté mal y pulsa **Apply**; **Cancel** deja todo como estaba, y las mismas partes se volverán a proponer la próxima vez.
- Un valor que el modelo leyó muestra la parte de la que procede. **Processed parts** enumera las partes que se leyeron; **Reprocess from here** deshace todo lo leído desde esa parte y las posteriores (los valores que escribiste o corregiste se conservan) y vuelve a leerlas. Hazlo después de reescribir una parte anterior. Una parte regenerada, un swipe o una rama distinta deshacen por sí solos lo que procedía de las partes que ya no existen.

<a id="the-story-clock"></a>

### El reloj de la historia

Con **Story clock** activado (en la configuración, desactivado por defecto), el modelo también sigue el tiempo de la historia. En la misma solicitud que el estado, indica para cada parte lo que esa parte dice sobre el tiempo: *la mañana siguiente*, *tres días después*, *esa noche*, *el cuadragésimo día desde que despertaste*, *el veinte de abril*, un vago *las semanas se confundieron* (marcado como aproximado), o un flashback, un recuerdo o un sueño del pasado, que no mueve el reloj y no cambia nada del estado. El modelo no cuenta: el plugin suma los días.

![La configuración del reloj de la historia en el panel de procesamiento](../../images/plugin-statetracker-clock.png)

![Corregir a mano el tiempo de una parte](../../images/plugin-statetracker-clock-dialog.png)

- **Day 1 is the day of** indica el suceso desde el que se cuentan los días (*the awakening*): la hora se lee *Day 14 after the awakening, evening*. **Day at the start point**, **Time of day** y **Date at the start point** dan la hora anterior a la primera parte que se lee; con una fecha, el reloj también cuenta fechas (*20 April 1453*).
- El panel Processing muestra ahora el **story time**, y cada parte procesada muestra la hora posterior a ella. Un día o una fecha indicados sustituyen a una estimación anterior.
- El botón de reloj de una parte (o de la línea de story time, para la última parte) **sets the time by hand**: un punto fijo (día, momento del día, fecha) que volver a leer la parte no sustituye y desde el que el reloj sigue contando. **Remove the fixed point**, en el mismo diálogo, permite que la lectura del modelo vuelva a contar.
- En la revisión, la hora aparece primero, bajo **Story time**, con la cita de cada parte; desmarca lo que esté mal.
- El bloque del prompt empieza con la hora (*Story time: day 14 after the awakening, evening.*), también cuando ningún personaje rastreado está en la escena.
- Un valor recuerda el día de la historia en que se estableció (*day 4*). La limpieza ve hace cuánto de eso (*a cup of tea, set 9 days ago*) y descarta lo que tuvo tiempo de desvanecerse.
- Con el reloj activado, cada parte se envía al modelo, también las que no tienen ningún personaje rastreado, porque cualquier parte puede mover el tiempo.
- Volver a procesar, regenerar o cambiar de rama deshace lo leído de las partes que ya no están, igual que el estado; los puntos fijos que estableciste se mantienen.

<a id="the-log-of-automatic-changes"></a>

### El registro de cambios automáticos

Lo que se aplica sin revisión (una ejecución con **Ask before applying** desactivado, la limpieza automática, un comando sin `ask`) se escribe en **Applied automatically**, dentro del panel Processing: quién, qué categoría, de qué parte (o *tidied*) y qué cambió (*+ lantern*, *− rope*, *short sword → axe*). Nada del registro es editable, pero cada fila tiene un botón **Revert**. Funciona como si hubieras desmarcado el cambio en la revisión: se elimina el valor que introdujo el cambio y vuelve a ser el valor actual el que tenía antes. Una fila se deja intacta si desde entonces cambiaste el valor a mano (lo editaste, quitaste, restauraste o eliminaste, o diste a una sola categoría otro valor). El registro guarda los últimos 200 cambios.

![El registro de cambios automáticos](../../images/plugin-statetracker-log.png)

<a id="returning-to-an-earlier-point"></a>

### Volver a un punto anterior

Antes de cada ejecución que aplica cambios (una ejecución de la pestaña con o sin revisión, la lectura automática de partes nuevas, los comandos, una limpieza), el rastreador crea un **checkpoint**: todo el estado de sus personajes tal como lo encontró la ejecución, con el reloj de la historia y el punto hasta el que había leído (*Start of the book* antes de la primera ejecución). **Checkpoints**, en el panel Processing, los enumera del más reciente al más antiguo: la parte hasta la que había leído el rastreador, la hora, qué tipo de ejecución vino después y cuántos cambios aplicó. Solo se listan los checkpoints cuya parte está en la rama activa.

![Un cambio deshecho](../../images/plugin-statetracker-reverted.png)

**Return to this point** en una fila deshace esa ejecución y todo lo posterior: los valores de los personajes (actuales y pasados), las marcas del reloj de la historia, las partes procesadas y el registro de cambios automáticos vuelven a ser los del checkpoint, y la siguiente ejecución vuelve a leer las partes posteriores a la suya, incluidas las de la ejecución deshecha. Lo que tenía el rastreador se guarda como un checkpoint (*before a return to a checkpoint*), así que puedes volver a él. Tu configuración, el start point, los ajustes del reloj y las categorías del libro se mantienen como están ahora (una categoría que hayas eliminado desde entonces vuelve cuando los valores del checkpoint la usan). Pide confirmación primero, porque se deshace todo lo leído después del checkpoint, y también los cambios que hayas hecho a mano desde entonces. Úsalo cuando una serie de ejecuciones salió mal, o después de restaurar una copia de seguridad antigua del libro y querer que el rastreador se ponga al día desde un punto en el que confíes.

El rastreador guarda los últimos 20 checkpoints de un libro. Se conservan entre tus archivos (*Resources*, agrupados por libro), no en el libro, así que las copias de seguridad y las copias del libro no los incluyen.

<a id="tidying-the-state"></a>

### Limpiar el estado

Algunos estados se desvanecen sin que la historia lo diga: un ánimo, ropa mojada, quedarse sin aliento, una taza sostenida mucho después de la comida. **Tidy the state**, en el panel Processing, pregunta al modelo por ellos. Lee las últimas partes procesadas junto con los valores actuales de los personajes rastreados que aparecen en esas partes, y para cada valor puede:

![La revisión de una limpieza](../../images/plugin-statetracker-tidy-review.png)

- **drop** el valor: ya no es cierto o ya no merece la pena conservarlo;
- **update** el valor: las partes muestran que ha cambiado (la ira se ha convertido en alivio);
- **merge** entradas de una lista que son lo mismo (*rope* y *coil of rope*).

Los valores que no menciona se quedan como están, y también los valores bloqueados. Cada cambio viene con un motivo. Un valor que escribe la limpieza (una actualización o una fusión) se marca como *by the tidy* con la última parte que leyó, ya que la historia no lo decía; si lo editas, pasa a ser tuyo. Lo revisas como cualquier otra ejecución (o se aplica de inmediato si **Ask before applying** está desactivado). Un valor que la limpieza termina pasa al historial como *tidied*, y **Reprocess from here** en la última parte leída antes de la limpieza lo deshace. Los personajes que no están en las últimas partes se dejan en paz, así que una cuerda que lleva alguien que lleva veinte capítulos ausente se conserva.

Con **Tidy automatically** activado (por defecto), la limpieza también se ejecuta sola al final de una ejecución que aplica sus cambios sin preguntar, una vez que se han leído **Tidy after this many parts** partes (10 por defecto) desde la última limpieza. Una ejecución que revisas nunca inicia una limpieza, para que no se te imponga una segunda revisión.

En la configuración (el engranaje):

- **Process new parts automatically**: cuando una parte está terminada, el modelo la lee (como muy tarde, antes de la siguiente generación). Con **Ask before applying** activado, solo se lee cuando pulsas **Process now**: nunca se te impone una revisión en medio de la escritura.
- **Ask before applying**: desactivado, los cambios se aplican de inmediato y se escriben en el registro (ver más abajo).
- **Parts per run**: cuántas partes lee una solicitud.
- **Tidy automatically** y **Tidy after this many parts**: ver [Limpiar el estado](#tidying-the-state). La limpieza también lee esa cantidad de las últimas partes procesadas.
- **Lorebook for the model**: las entradas que activan las partes (por defecto), todo el lorebook o ninguna.
- **Model for the extraction** y **Protocol for the extraction**: vacío significa el modelo del libro.
- **Use these settings for new books** guarda la configuración de este libro (incluidos el modelo y el protocolo; los separadores se quedan con el libro) como la que tienen los libros que empiezan donde nunca se usó el State Tracker. **Forget the settings for new books** vuelve a la configuración integrada. Los libros que ya usan el rastreador conservan su propia configuración.
## Prompts

El prompt de extracción del estado (**State extraction prompt**) y el de limpieza (**State tidy prompt**) son [ajustes heredados](../books/prompts.md#inherited-settings): un libro usa los suyos; si no tiene, usa los de la carpeta superior más cercana que defina alguno; si no, los tuyos; y si tampoco, el texto integrado en el idioma de tu prompt (la versión en inglés se probó con un modelo real).

Configúralos en la pestaña *Prompts* del libro, en *Extension Settings*, en el diálogo de una carpeta, o para todos tus libros en *Settings → Extension Settings → State Tracker* (si está vacío, se muestra en gris el texto integrado). Mantén el formato de respuesta JSON del texto integrado: el plugin lee la respuesta por ese formato. Las reglas del [reloj de la historia](#the-story-clock) las añade el plugin después del prompt de extracción cuando el libro sigue el tiempo, así que tu prompt no necesita incluirlas.

<a id="in-the-prompt"></a>

## En el prompt

El botón de engranaje guarda los ajustes del libro:

- **In prompt** inserta el estado de los personajes seguidos de la escena en el prompt de cada generación (el engranaje se vuelve verde mientras está activado).
- **Depth** y **Role** indican dónde va, igual que en la Author's Note: *Depth* es el número de mensajes después de él (0 después de las instrucciones del turno, 1 justo antes de ellas).
- **Name separators** indican cómo se divide el campo *Present Characters* (por defecto `,` `;` `/` `|` y saltos de línea). Las notas entre corchetes se ignoran, así que `Mara (asleep), Fiora` encuentra ambos.

El prompt recibe los personajes de la escena que tienen algo que decir:

```
The current state of the characters of this scene. Keep to it; it changes only when the story changes it:

Fiora:
- Wearing: torn green cloak, leather boots
- Weapon at hand: none
- Carrying: rope; lantern
```

Puedes poner el mismo texto en cualquier lugar de una plantilla con la macro {%{`{{command::state-tracker-block::{{presentCharacters}}}}`}%} (cualquier lista de nombres sirve como argumento). La macro imprime el estado tanto si **In prompt** está activado como si no, así que desactívalo cuando uses la macro, o el modelo recibirá el estado dos veces.

<a id="commands"></a>

## Comandos

Para la barra de comandos y para los agentes:

| Comando | Qué hace |
|---|---|
| `state-status` | Los ajustes, las categorías del libro y las tuyas de las que el libro no tiene copia. |
| `list-state-characters` | Los personajes seguidos con sus categorías. |
| `track-state-character` | Empieza a seguir a un personaje: nombre, alias, categorías por nombre. |
| `stop-tracking-state-character` | Deja de seguir a un personaje y borra su estado (pregunta antes). |
| `list-character-state` | Los valores de un personaje, actuales y pasados, numerados. |
| `set-character-state` | Establece un valor: personaje, categoría, texto. |
| `remove-character-state` | Elimina un valor por su número: pasa al historial. |
| `state-tracker-block` | El texto del prompt para una lista de personajes (la macro). |
| `state-process` | Deja que el modelo lea las siguientes partes y aplique los cambios; con `ask true` los devuelve numerados en lugar de aplicarlos. |
| `apply-state-changes` | Aplica los cambios que `state-process` o `state-tidy` devolvieron, todos o los números indicados. |
| `set-story-time` | Establece a mano la hora de la historia tras una parte procesada (día, momento del día, fecha); sin ninguno de ellos, quita el punto fijo. |
| `state-tidy` | Deja que el modelo elimine, actualice o fusione los valores que se han desvanecido; con `ask true` devuelve los cambios numerados en lugar de aplicarlos. |
| `state-reprocess` | Deshace lo leído de una parte y de las posteriores (pregunta antes). |
| `state-set-start` | Establece la parte donde empieza la lectura. |
