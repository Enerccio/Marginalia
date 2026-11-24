---
label: Rastreador de conocimiento
order: 11
---

# Rastreador de conocimiento

Lleva el registro de lo que saben los personajes de tu historia y de lo que **no** saben, y lo incluye en el prompt. Un modelo tiende a hacer que un personaje sepa cosas sobre personas que nunca ha conocido (su nombre, su trabajo, su cara) a menos que las instrucciones lo impidan. Con el Rastreador de conocimiento, el prompt indica, para cada personaje rastreado en la escena, qué sabe ese personaje y que no sabe nada más.

!!!
Tú eliges quién se rastrea. Solo se describe al modelo a los personajes que añadas a la pestaña, y solo mientras estén entre los *personajes presentes* de la parte que se está escribiendo. Nada se deduce del texto de la historia.
!!!

<a id="the-knowledge-tab"></a>

## La pestaña Knowledge

La barra lateral izquierda del [editor de historias](../books/story-editor.md) incluye una pestaña **Knowledge** junto a *Outline*. Todo se guarda en el libro a medida que lo cambias.

![La pestaña Knowledge](../../images/plugin-knowledgetracker.png)

**Track a character** pide un **name** y, opcionalmente, **aliases**: los otros nombres con los que aparece el personaje en el campo *Present Characters* de una parte, separados por comas. Un nombre o un alias solo puede pertenecer a un personaje rastreado.

**Import from the Character Tracker** (el icono de persona junto a **Track a character**) empieza a rastrear personajes del [Character Tracker](character-tracker.md) del mismo libro. Muestra los personajes que el libro todavía no rastrea (como nombre o como alias), con sus frases descriptivas, todos marcados. Desmarca los que quieras dejar fuera y pulsa **Import**. Solo se toman los nombres: la frase descriptiva y la nota describen a un personaje, no son algo que este sepa. Con **Their relationships count as meetings**, un personaje importado ha conocido a las personas con las que está relacionado en el Character Tracker (se escriben en su lista **Has met** como si las hubieras escrito tú, de modo que el procesamiento las conserva).

**Who has met whom** (el icono de apretón de manos, junto a la importación) establece de una vez los encuentros entre personajes rastreados, sin escribir nombres. Elige un personaje a la izquierda; a la derecha, cada otro personaje rastreado tiene una casilla, marcada cuando el elegido lo ha conocido. Marca para añadir el encuentro y desmarca para quitarlo; cada cambio se guarda al instante y las tarjetas lo muestran al cerrar la ventana. Un encuentro va en un solo sentido: marcar a Thomas para Fiora no marca a Fiora para Thomas, salvo que **Mutual** esté marcado. En ese caso, cada marca o desmarca se hace en ambos lados a la vez, lo que es más rápido cuando los dos ya se conocen. Se aplica a los cambios que hagas mientras esté marcado, no a los encuentros que ya estaban establecidos. Un encuentro marcado cuenta como tuyo (el procesamiento lo conserva); si desmarcas uno que aparece en la historia, vuelve cuando se vuelve a leer esa parte. Necesita al menos dos personajes rastreados.

Abre un personaje con la flecha de su tarjeta para ver lo que sabe:

| | |
|---|---|
| **Knows** (verde) / **Does not know** (rojo) | Una frase corta sobre cualquier cosa: una persona, un lugar, un secreto, un suceso (*Peter sabe de la conspiración contra el duque*, *Fiora no sabe que Thomas es su padre*). Escribe de forma explícita lo que un personaje **no** sabe, porque eso es lo que el modelo respeta. |
| El orden y **#12** | Lo más reciente va arriba (lo antiguo también). Un elemento que aprendió el modelo termina con el número pequeño de la parte en la que se aprendió (**#12**): al hacer clic, la historia se desplaza a esa parte y la resalta (en el teléfono, primero se cierra el cajón). Los elementos que escribiste no tienen número, y tampoco los que tienen una parte que no está en la rama activa (otra rama, o la parte se eliminó). |
| **Add knowledge** | Añade un elemento, activo al instante. Mantenlo en **un solo pensamiento**: un elemento que dice dos cosas (*no sabe quién es Thomas, nunca lo ha conocido*) se retira en cuanto una de ellas deja de ser cierta. El botón **edit** cambia el texto y el tipo. |
| **Retract** (el círculo con una barra) | El elemento ya no es cierto (*no ha conocido a Thomas* después de que se encuentran). No se borra: pasa a ser *conocimiento antiguo* y ya no se envía al modelo. |
| **Lock** (el candado, en los elementos que aprendió el modelo) | La [pasada de limpieza](#tidying-the-knowledge) nunca ve un elemento bloqueado: úsalo para lo que debe permanecer, una traición, un secreto. La historia puede contradecir un elemento bloqueado (un personaje que pierde la memoria): la lectura de la historia lo retira como de costumbre. |
| **Delete** | Elimina un elemento para siempre, tras una pregunta. Retract lo conserva, así que úsalo si tienes dudas. |
| **Select items** (junto a **Add knowledge**) | Elimina muchos elementos rápidamente: cada elemento del personaje, incluidos los antiguos, recibe una casilla. **Select all** / **Deselect all** marca o desmarca todas, **Delete selected (n)** elimina para siempre las marcadas tras una pregunta, y **Cancel** cancela la selección. |
| **Has met** | Las personas que el personaje ha conocido, anotadas por el [procesamiento](#processing). El procesamiento puede anotar un nombre que no es una persona (una nota en el campo de personajes presentes): abre **Edit** y cambia la lista **Has met**, un nombre tras otro separados por comas. Los nombres que escribas se conservan cuando se vuelve a leer la historia; uno que quites solo vuelve si se vuelve a leer la parte en la que se vio. |
| **Old knowledge (n)** | Plegado de forma predeterminada. Cada elemento antiguo indica por qué lo es: *retracted* (con el número de la parte cuya lectura lo retiró, cuando lo hizo el modelo), *declined* en una revisión, o *tidied*. Se puede **copy**, **edit**, **restore** (vuelve a estar activo, desde ahora) o eliminar. |

### Mass changes

El botón **Mass changes** (el icono de lista junto a **Track a character**) abre una ventana con cada personaje rastreado y todos sus elementos, también los antiguos, en una cuadrícula con casillas: el tipo, el texto, el estado (*Active*, o antiguo: *retracted*, *declined in a review*, *tidied*), el origen (*Initial state*, *Written by hand*, *Learned from the story*) y la parte en la que se aprendió. Marca elementos en todas las cuadrículas que quieras (la casilla del encabezado de una cuadrícula marca todos sus elementos, y **Select all** / **Deselect all** en la barra inferior marca todos los elementos de todos los personajes). Después:

| | |
|---|---|
| **Retract selected (n)** | Los elementos activos marcados pasan a ser conocimiento antiguo, como hace **Retract** con uno solo. |
| **Restore selected (n)** | Los elementos antiguos marcados vuelven a estar activos, desde ahora. |
| **Delete selected (n)** | Elimina para siempre los elementos marcados, tras una pregunta. |
| **Copy selected (n)** | Copia los elementos marcados, de todos los personajes, para pegarlos en otro personaje. |
| **Paste (n)** (en el encabezado de cada personaje) | Añade los elementos copiados a ese personaje. Un elemento pegado cuenta como escrito a mano (uno inicial sigue siendo inicial) y conserva su tipo, su bloqueo y si es antiguo; si el personaje ya tiene uno igual (mismo tipo y texto), se omite. Los elementos copiados se mantienen hasta la próxima copia, también después de cerrar la ventana, así que pueden pegarse en varios personajes. |

Los contadores de los botones indican cuántos elementos marcados cambia cada uno. Las marcas se mantienen tras un cambio, para que después se puedan eliminar los elementos retirados.

**Export to JSON** (en la parte superior de la ventana) descarga los personajes rastreados del libro (nombres, alias, elementos con su tipo, estado, origen y bloqueo, y las personas que han conocido) como un archivo. No incluye nada del procesamiento, así que sirve para cualquier libro. **Import from JSON** lee un archivo de ese tipo y lo incorpora al libro, y solo añade: un personaje cuyo nombre el libro ya rastrea (como nombre o como alias) recibe los alias, elementos y personas conocidas que aún no tiene (un elemento con el mismo tipo y texto se omite), y los demás se convierten en personajes rastreados nuevos. Un alias que ya tiene otro personaje se deja fuera. Un elemento importado no tiene detrás ninguna parte de este libro, así que un nuevo procesamiento nunca lo elimina: uno que aprendió el modelo cuenta como escrito a mano, y uno antiguo sigue siendo antiguo. El mensaje tras la importación indica cuántos personajes se han añadido o actualizado y cuántos elementos se han añadido o ya estaban.
<a id="what-goes-into-the-prompt"></a>

## Qué entra en el prompt

Para cada personaje rastreado que está entre los personajes presentes, el prompt incluye un bloque como este:

```
What the characters of this scene know. A character acts only on the knowledge listed for it:

Fiora knows: Mara, a courier of the Guild of Lamps and the sister of her friend Nell; she owes the Guild of Lamps forty crowns.
Fiora does not know: that her father is alive, or that Thomas is her father.
Fiora has met: Mara.
Fiora is currently with: Mara, Thomas.
Fiora has never met or heard of anyone else in this story and knows nothing else of its secrets, plans or events.
```

Solo se envían los elementos activos. La última línea (la línea de **mundo cerrado**) impide que el modelo dé al personaje conocimientos que no puede tener. Cubre lo que la historia cuenta (personas, lugares, secretos, planes, acontecimientos), no lo que una persona de ese mundo sabe de todos modos, así que un personaje sigue sabiendo cómo es su propio pueblo. Puedes desactivar esa línea en la configuración. Un personaje rastreado que no está presente no recibe nada, ni siquiera una línea que diga que no lo sabe.

<a id="who-is-present"></a>

### Quién está presente

El bloque se crea a partir del campo **Present Characters** de la parte. Es un campo de texto libre, así que se lee de forma flexible:

- el texto entre corchetes se ignora: `Tomáš (tired), Mizuki` es Tomáš y Mizuki;
- los nombres se separan en los **separadores** (por defecto: `,` `;` un salto de línea, `/`, `|`, `、` `，` `；`);
- lo que va después de ` - ` o `:` es una nota y se descarta (`Mara - wounded`);
- no importan las mayúsculas o minúsculas, los acentos ni un artículo inicial como *el* o *la*;
- un nombre tiene que coincidir **por completo**: *el hermano de Mara* no es *Mara*. Usa los alias cuando un personaje aparezca escrito de más de una forma. La idea es que una coincidencia perdida cuesta un bloque, mientras que una coincidencia falsa describiría a un personaje que no está en la escena.

El campo *Point of View* no se lee por sí solo, así que escribe también al personaje con el punto de vista entre los personajes presentes.

<a id="processing"></a>

## Procesamiento

El plugin sigue la historia. **Processing** lee las partes de la rama activa en orden, desde el **punto de inicio**, y mantiene un **cursor** (la última parte que leyó). Sin el modelo, anota quién se ha encontrado con quién: un personaje rastreado se ha encontrado con cada nombre que aparece en el campo de personajes presentes junto con él, y el bloque los enumera. Con el modelo, además encuentra lo que los personajes **aprendieron** (consulta [Lectura de lo que aprenden los personajes](#reading-what-the-characters-learn)).

![El panel de procesamiento](../../images/plugin-knowledgetracker-processing.png)

El panel de la parte superior de la pestaña muestra en qué punto está. Se recarga solo cuando una parte nueva queda final (después de la anotación y la reescritura automáticas, si el libro las tiene). Si añades, editas o eliminas partes de otra forma, pulsa el botón **Refresh** (las flechas junto al engranaje) para volver a leer la historia. Tiene:

- **Process now**: lee las partes que todavía no están procesadas.
- **Process next part**: lee solo la primera parte que no está procesada, paso a paso (con el modelo: una petición para esa parte, sin importar lo que indique *Parts per run*). Úsalo para probar la configuración o para ver lo que el modelo encuentra en una sola parte. El comando `knowledge-process` con `parts` en 1 hace lo mismo.
- **Start point**: la parte en la que empieza el procesamiento (*Start of the book* por defecto). Las partes anteriores nunca se leen. Si lo retrasas, se vuelven a leer las partes desde el nuevo inicio; si lo adelantas, lo ya leído se mantiene.
- **Processed parts**: las partes más recientes que se han leído. Una parte cuyo texto **cambió desde que se procesó** (la editaste o la reescribiste) aparece marcada; nada se vuelve a procesar por sí solo por este motivo. El botón de cada fila, **Reprocess from here**, deshace lo que procedía de esa parte y de las siguientes (los elementos y las retractaciones que escribiste a mano se mantienen) y las vuelve a leer.

Cuando desplazas la lista de personajes hacia abajo, más allá de estos botones, **Process now**, **Process next part** y **Tidy knowledge** también aparecen en la barra superior de la pestaña, para que puedas procesar sin volver arriba.

Lo que hace el plugin por sí solo, cuando **Process new parts automatically** está activado (desactivado por defecto):

- Antes de empezar una generación, se procesan las partes escritas desde la última vez, para que el prompt sepa quién se ha encontrado con quién. Una parte que se está reemplazando con *Regenerate* o con un swipe se deja en paz. Con el modelo, esto lee con el modelo solo cuando **Ask before applying** está desactivado (véase más abajo); en caso contrario, solo anota quién se ha encontrado con quién, y lo que encuentre el modelo se inicia desde la pestaña, porque nunca se te impone una revisión en mitad de una generación.
- Cuando una parte se **regenera** y llega el nuevo texto, se retira lo que se aprendió del texto antiguo.
- Cuando haces **swipe** o cambias a otra rama, el cursor vuelve a la última parte que comparten ambas ramas.
<a id="reading-what-the-characters-learn"></a>

## Leer lo que aprenden los personajes

Marca la casilla **Read what the characters learn with the model** en los ajustes. Entonces, **Process now** pide al modelo, para las siguientes partes de la historia, qué **aprendió** cada personaje seguido que aparece en ellas: lo que vio, oyó, leyó o le contaron. También indica qué elementos contradice la historia (*has never met Thomas* tras conocerse). La petición contiene las partes (con su punto de vista y los personajes presentes), lo que cada personaje seguido sabe ahora y, si lo eliges, el lorebook. Al modelo se le indica que omita lo que una persona de ese mundo sabe de todos modos (quién es el rey) y lo que se deduce del propio personaje (una súcubo sabe que tiene cola), y que no informe de nada de lo que no esté segura. Un personaje seguido que no aparece en ninguna de las partes no se consulta.

Cada ejecución es una sola petición al modelo. Cuando **Process now** necesitaría más de tres (un tramo largo de historia aún no procesado, o **Parts per run** configurado con un valor bajo), primero indica cuántas partes y peticiones son y aproximadamente cuánto texto pasa por el modelo, y empieza solo cuando confirmas. Cada petición además lleva las instrucciones, el lorebook y lo que saben los personajes, así que el coste real es mayor que el del texto solo.

Cuando **Ask before applying** está desactivado, los cambios se aplican enseguida, y cada uno se escribe en el registro **Applied automatically** de la sección Processing de la pestaña: quién recibió o perdió qué, y de qué parte. Nada del registro es editable, pero cada fila tiene un botón **Revert**: un elemento añadido se rechaza (queda en *Old knowledge*, como ocurre con una adición rechazada en la revisión), y un elemento retirado vuelve a estar activo. Una fila cuyo elemento hayas editado, borrado o retirado a mano desde entonces se deja como está. El registro guarda los últimos 200 cambios.

<a id="returning-to-an-earlier-point"></a>

### Volver a un punto anterior

Antes de cada ejecución que cambia el conocimiento (una ejecución de la pestaña con o sin revisión, la lectura automática de nuevas partes, los comandos `knowledge-process` y `knowledge-tidy`, una limpieza y una lectura sin el modelo), el rastreador guarda un **checkpoint**: el estado completo de sus personajes tal como lo encontró la ejecución, con el punto hasta el que había leído (*Start of the book* antes de la primera ejecución). **Checkpoints** en el panel Processing los lista, del más nuevo al más antiguo: la parte hasta la que había leído el rastreador, la hora, qué tipo de ejecución vino después y cuánto añadió y retiró. Solo se listan los checkpoints cuya parte está en la rama activa.

**Return to this point** en una fila deshace esa ejecución y todo lo posterior: los personajes y su conocimiento, las partes procesadas y el registro de cambios automáticos son los del checkpoint, y la siguiente ejecución vuelve a leer las partes posteriores a su parte, incluidas las de la ejecución deshecha. Tus ajustes y el punto de inicio se mantienen como están. Pide confirmación primero, porque se deshace todo lo aprendido después del checkpoint, también los cambios que hayas hecho a mano desde entonces. Lo que tenía el rastreador se guarda como checkpoint (*before a return to a checkpoint*), para que puedas volver a ello. Úsalo cuando una ejecución o una serie de ejecuciones salió mal, o tras restaurar una copia de seguridad antigua del libro y querer que el rastreador se ponga al día desde un punto en el que confíes.

El rastreador guarda los últimos 20 checkpoints de un libro. Se guardan entre tus archivos (*Resources*, agrupados por libro), no dentro del libro, así que las copias de seguridad y las copias del libro no los incluyen.

Con **Ask before applying** desactivado, la nueva parte se lee en cuanto está final: cuando termina la generación o, si el libro tiene activada la anotación automática (y reescritura) o una traducción, cuando eso termina. Así el modelo lee el texto que conservas y trabaja mientras tú lees. Es un paso de [lo que sigue a la parte](../books/story-editor.md#what-follows-a-generated-part). La siguiente generación encuentra entonces el conocimiento al día y no espera. Lo que quede pendiente (una ejecución que paraste, una ventana que cerraste) se lee antes de la siguiente generación, como antes.

Mientras el modelo trabaja, un pequeño **panel de progreso** flota sobre la página. No es modal: puedes seguir leyendo y editando la historia, y puedes **arrastrarlo** por su título. Cerrarlo lo oculta, pero el trabajo continúa (**Cancel** lo detiene).

![El panel de progreso](../../images/plugin-knowledgetracker-progress.png)

Mientras el modelo responde, el panel muestra **What the model is writing** a medida que llega: primero su razonamiento y luego la respuesta, para que una espera larga con un modelo lento no sea una barra en blanco. Cuando termina la petición, el mismo texto se queda bajo **What the model thought** (un bloque por cada petición de la ejecución, con lo que respondió), también cuando no propuso **nada** y no se abre ninguna revisión. La pestaña mantiene un botón **Last run: what the model thought** que vuelve a abrir el panel, para que veas por qué no se añadió nada.

Cuando el modelo ha respondido, el panel lo indica y muestra **Review changes**; el mismo botón está en el panel de procesamiento de la pestaña (*3 changes to review*). **La revisión nunca se abre sola.** Es una tabla de los cambios, agrupados por personaje:

![Revisando los cambios](../../images/plugin-knowledgetracker-review.png)

Bajo la tabla, **What the model thought** (cerrado al principio) contiene el razonamiento del modelo que vino antes de su respuesta, cuando el modelo lo envía. Muestra por qué se propuso un cambio, o por qué falta uno que esperabas, para que puedas juzgar el cambio antes de aplicarlo. Un modelo que no razona no muestra nada ahí.

| | |
|---|---|
| La casilla de verificación | Si el cambio se aplica. Todas están marcadas al principio; cada personaje tiene **Check all** / **Uncheck all**. |
| **Retract** / **Add** | Una retirada hace que un elemento quede antiguo (se conserva, ver [la pestaña](#the-knowledge-tab)). Una adición es un elemento nuevo, *Knows* o *Does not know*. El texto y el tipo de una adición se pueden **editar** antes de aplicarla. |
| La cita | Las palabras de la parte que lo muestran, y de qué parte son. Una cita que no se encuentra en esa parte se marca: el modelo puede haberla inventado, así que compruébala antes de aceptar. |

**Apply** aplica los cambios marcados y pasa a lo siguiente. **Cancel** detiene toda la serie y no cambia nada: las mismas partes se proponen de nuevo la próxima vez. Una adición que desmarques se **conserva como conocimiento antiguo** con su cita (puedes restaurarla). Una retirada desmarcada deja el elemento como estaba. Lo que dijo el modelo y no se pudo usar (un personaje por el que no se le preguntó, un elemento que no existe, un cambio sin cita, algo que el personaje ya sabe) aparece en *Left out*. Lo aplicado conserva su parte, así que **Reprocess from here** lo deshace de nuevo para volver a procesarlo. Si la historia cambió mientras revisabas (otra rama, una parte borrada), los cambios no se aplican y hay que volver a procesar.

Las partes se leen en **tandas** de **Parts per run** (1 por defecto). Una parte que no cabe en el contexto del modelo se recorta a lo que sí cabe. Una serie continúa, tanda tras tanda, hasta la última parte asentada. En una serie que te pregunta, una tanda en la que el modelo no encontró nada se omite sin preguntar. Con **Ask before applying** desactivado, los cambios se aplican enseguida y el panel guarda un registro con una línea por tanda.

!!!
El prompt se probó contra un modelo con unas pocas escenas, no a gran escala: impide que los personajes aprendan lo que no percibieron, pero revisa lo que se propone, sobre todo en personajes que están en una escena larga. La petición se envía al modelo que elijas abajo, con el coste y la privacidad habituales de ese servicio.
!!!
<a id="tidying-the-knowledge"></a>

## Depurar el conocimiento

El modelo anota lo que aprende cada personaje, y en una historia larga la lista crece, con más cosas pequeñas de las que querrías enviar al modelo en cada petición. **Tidy knowledge** (junto a **Process now**, necesita el ajuste del modelo) pide al modelo que vuelva a leer lo que sabe un personaje, con las últimas partes procesadas de la historia como contexto, y que

- **elimine** un elemento que no merece conservarse: trivialidades, un detalle de una escena que todos los presentes vieron (la historia lo conserva), algo que los acontecimientos posteriores han dejado atrás o algo que dice lo mismo que otro elemento;
- **fusione** varios elementos sobre lo mismo en uno solo que conserve todos los datos.

Un elemento eliminado o fusionado no se borra: pasa a *Old knowledge* y puede restaurarse. Los cambios se revisan, como los de una lectura (**Ask before applying** decide), o se aplican de inmediato y se escriben en el registro con un botón **Revert**.

Lo que la pasada puede tocar es solo lo que el modelo aprendió y que no has **lock**eado. Los elementos iniciales, los que añadiste o editaste a mano, los bloqueados y los antiguos nunca se modifican (los dos primeros tipos y los bloqueados se muestran al modelo para que vea qué duplicaría un elemento). Un secreto que ninguna parte ha mencionado durante mucho tiempo no es motivo para eliminarlo, y el prompt lo dice así; cuando el modelo tiene dudas, conserva el elemento. Un personaje con menos de **Tidy: from this many items** (8) elementos que el modelo aprendió no se depura, y **Tidy: parts of context** (10) es el número de las últimas partes procesadas que lee.

**Tidy automatically** (un ajuste, desactivado por defecto) ejecuta la depuración por sí solo al final de una ejecución cuyos cambios se aplican sin preguntar (**Ask before applying** desactivado, así que también la lectura automática de las partes nuevas), una vez que el modelo ha leído **Tidy after this many parts** (10) partes desde la última depuración. Lo que cambia se aplica y se escribe en el registro con un botón **Revert**, y el registro de la ejecución lo indica. Una ejecución que revisas nunca inicia una depuración por sí sola, y un personaje con muy pocos elementos se omite sin consultar al modelo.

Lo mismo desde la barra de comandos: `knowledge-tidy` (`ask true` para obtener los cambios como lista; la pestaña abierta muestra entonces el botón de revisión), y `lock-knowledge` / `unlock-knowledge` con el personaje y el número del elemento.

<a id="settings"></a>

## Ajustes

El engranaje junto a *Track a character*:

| Ajuste | |
|---|---|
| **In prompt** | Inserta el bloque en el prompt de cada generación. El botón se vuelve verde mientras está activo. |
| **Depth** | Dónde va, contado en mensajes desde el final del prompt: `0` después de las instrucciones del turno, `1` justo antes de ellas. Nunca va antes del prompt de sistema. |
| **Role** | Si se envía como mensaje *System*, *Assistant* o *User*. |
| **Say that nothing else is known** | La línea de mundo cerrado. |
| **Give what the characters know to the annotation** | Activado (por defecto): [Anotar con IA](#annotating-with-what-the-characters-know) recibe lo que sabían los personajes seguidos de la parte cuando empieza. |
| **Process new parts automatically** | Véase [Procesamiento](#processing). |
| **Read what the characters learn with the model** | El procesamiento pregunta al modelo qué aprendió cada personaje seguido y lo propone. Desactivado (por defecto): el procesamiento solo anota quién se ha encontrado con quién, lo que no cuesta nada. |
| **Ask before applying** | Activado (por defecto): cada propuesta se muestra para revisión. Desactivado: se aplica de inmediato y se registra. |
| **Let the model read earlier parts** | Mientras lee las partes nuevas, el modelo puede leer la parte en la que se aprendió un elemento (los elementos dicen "learned in part 12"), para ver si lo que lee confirma, repite o contradice ese elemento: un rumor que ahora se ve con los propios ojos, por ejemplo. Como máximo 5 lecturas de una parte cada una por petición; el texto de una parte larga se recorta a lo que cabe en el contexto, y la revisión muestra qué partes ha leído. Cada lectura es una ronda más con el modelo, por lo que está desactivado por defecto. Un modelo que no puede llamar a funciones lee la petición como antes. |
| **Parts per run** | Cuántas partes lee una petición. Un número mayor sale más barato, pero da una respuesta más larga y una revisión más difícil. |
| **Tidy automatically**, **Tidy after this many parts** | La pasada de depuración se ejecuta por sí sola al final de una ejecución que aplica sus cambios sin preguntar, cada este número de partes (10). Desactivado por defecto. |
| **Tidy: parts of context** | Cuántas de las últimas partes procesadas lee la pasada de depuración (10). |
| **Tidy: from this many items** | Un personaje con menos elementos que el modelo aprendió (y que no están bloqueados) no se depura (8). |
| **Lorebook for the model** | *Entries the parts activate* (por defecto: las entradas que usaría una generación para estas partes), *The whole lorebook* (todas las entradas; cuando no cabe en el contexto, se envían las activadas y la nota lo indica) o *No lorebook*. |
| **Model / Protocol for the extraction** | Vacío: el modelo del libro. Para esto basta con un modelo más barato, y en un servidor que mantiene una sola caché de prompts, un modelo aparte evita que lo reemplace. |
| **Separators of this book** | Cómo se divide el campo de personajes presentes en nombres, uno por línea (un salto de línea es `\n`). Una palabra como `and` separa entre espacios, así que *Jack and Jill* sigue siendo un solo nombre hasta que lo añadas. |
| **My separators (all books)** | Lo mismo para todos tus libros. Los separadores propios del libro tienen prioridad; si no hay ninguno, se usan los integrados. |

**Use these settings for new books** (en los ajustes del modelo) guarda los ajustes de este libro, la lectura con el modelo, el modelo y el protocolo incluidos, como los que empieza un libro en el que nunca se usó el Knowledge Tracker, de modo que un libro nuevo lee con tu modelo desde su primera ejecución. Los separadores del libro se quedan con él. **Forget the settings for new books** vuelve a los ajustes integrados. Los libros que ya usan el rastreador conservan sus propios ajustes.

## Prompts

El mensaje de sistema de la lectura (**Knowledge extraction prompt**) y el de la pasada de depuración (**Knowledge tidy prompt**) son [ajustes heredados](../books/prompts.md#inherited-settings): un libro usa los suyos; si no, la carpeta superior más cercana que defina uno; si no, los tuyos; si no, el texto integrado que se probó con un modelo real.

Defínelos en la pestaña *Prompts* del libro, en *Extension Settings*, en el diálogo de una carpeta, o para todos tus libros en *Settings → Extension Settings → Knowledge Tracker* (vacío muestra el texto integrado en gris). Mantén el formato de respuesta JSON del texto integrado: el plugin lee la respuesta gracias a él. El plugin añade las instrucciones para `read_part` cuando el modelo puede leer partes anteriores, así que tu prompt no las necesita.
<a id="annotating-with-what-the-characters-know"></a>

## Anotar con lo que saben los personajes

El bloque de arriba evita que el modelo convierta a un personaje en omnisciente *mientras escribe*. Los mismos registros también ayudan a encontrar los descuidos que se cuelan de todos modos. Cuando un modelo [anota una parte](../books/annotations.md#letting-a-model-annotate) (**Annotate with AI**, un rango de partes, la anotación automática, `/generate-annotation`), el rastreador le indica qué sabían los personajes seguidos de esa parte **cuando la parte empieza**. El editor marca a un personaje que dice, piensa o hace algo que no puede saber: *Fiora saluda a Thomas por su nombre; los registros dicen que nunca lo ha conocido.* La nota nombra el hecho.

- **Los hechos son los de la parte, no los del final del libro.** Lo que un personaje aprende en una parte posterior todavía no se conoce; un dato que una parte posterior retracta seguía siendo cierto. Lo que aporta la propia parte (lo que se aprende, a quién se conoce, lo que deja de ser cierto en ella) se lista aparte, para que la primera mitad de la parte no se compare con la segunda.
- **Qué personajes.** Los seguidos que están en los *Present Characters* de la parte, los que son su punto de vista o los que se nombran en su texto (palabra completa, sin distinguir mayúsculas, incluidos los alias).
- **Los datos que no son hechos en ningún momento** se dejan fuera: los que retiraste a mano, los que eliminó la pasada de orden y los que rechazaste en una revisión.
- **Solo lo registrado.** Se le indica al modelo que lo que los registros no dicen no es un hecho, así que no marca a un personaje por algo que el rastreador simplemente nunca supo. *Say that nothing else is known* también añade "(nobody else)" a quién se ha encontrado un personaje.
- Si una parte anterior a la revisada no la leyó el rastreador, los hechos lo indican: lo que los personajes aprendieron allí falta, y **Process now** primero da una revisión mejor.
- Los hechos cuestan tokens en la petición de la anotación, solo para las partes que tienen un personaje seguido. Desactívalos con **Give what the characters know to the annotation**.

<a id="the-block-as-a-macro"></a>

## El bloque como macro

En lugar de (o además de) la inserción automática, puedes colocar tú mismo el bloque con el [macro {%{`{{command::knowledge-tracker-block::<characters>}}`}%}](../templates/macros.md#commands). El argumento es la lista de personajes de la escena, así que una plantilla puede pasar el campo de la parte:

```
{%{{{command::knowledge-tracker-block::{{presentCharacters}}}}}%}
```

El macro imprime el bloque tanto si **In prompt** está activado como si no, así que desactívalo cuando uses el macro; si no, el modelo recibiría el bloque dos veces. La profundidad y el rol no se aplican al macro (el bloque forma parte del mensaje que contiene el macro). No imprime nada si ninguno de los personajes listados está seguido. La respuesta de un macro de comando se guarda junto a la parte de la que continúa la nueva parte, así que una segunda generación desde la misma parte no ve un cambio que hayas hecho entretanto; regenerar y el swipe vuelven a preguntar.

<a id="commands"></a>

## Comandos

Para la [línea de comandos](../commands/index.md) y para los agentes:

| Comando | |
|---|---|
| `knowledge-status` | La configuración y en qué punto está el procesamiento. |
| `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character` | Los personajes seguidos, referidos por nombre, alias o número. |
| `list-knowledge`, `add-knowledge`, `retract-knowledge`, `restore-knowledge`, `delete-knowledge` | Los datos de un personaje, referidos por su número en `list-knowledge`. |
| `knowledge-process` | Procesa ahora (`parts`: como máximo esa cantidad). Con el modelo activado, `ask true` devuelve lo que el modelo encontró como cambios numerados y los retiene: todavía no se cambia nada. La pestaña Knowledge abierta muestra un botón **changes found by a command** que abre la revisión habitual (Apply / Cancel); un agente los aplica con `apply-knowledge-changes`. |
| `apply-knowledge-changes` | Aplica los cambios retenidos, todos o los números indicados (igual que **Apply** en la revisión de la pestaña). |
| `knowledge-tidy` | La pasada de orden. Con `ask true` devuelve los cambios como una lista numerada y los retiene (la pestaña muestra un botón para revisarlos; `apply-knowledge-changes` los aplica). |
| `lock-knowledge`, `unlock-knowledge` | Bloquea un dato (personaje, número de dato) para que la pasada de orden no lo toque, o lo desbloquea. |
| `knowledge-reprocess` | Vuelve a procesar desde una parte (su identificador o posición); pide confirmación antes. Con el modelo activado solo restaura el estado; `knowledge-process` vuelve a leer las partes. |
| `knowledge-set-start` | Establece el punto de inicio; sin parte, el inicio del libro. |
| `knowledge-tracker-block` | El bloque como texto, para el macro. |

La línea de comandos divide sus argumentos por los espacios: pon entre comillas una lista de personajes (`/knowledge-tracker-block "Fiora, Mara"`).

<a id="storage"></a>

## Almacenamiento

Todo se guarda con el libro (en sus atributos), así que forma parte de las [copias de seguridad del libro](../books/backups.md) y de las copias. Los [puntos de control](#returning-to-an-earlier-point) no: son archivos tuyos vinculados al libro.
