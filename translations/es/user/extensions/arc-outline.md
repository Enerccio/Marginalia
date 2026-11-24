---
label: Esquema de arcos
order: 24
---

# Esquema de arcos

El plan de tu libro en la barra lateral: arcos, cada uno dividido en capítulos, con el estado de cada uno, y un modelo que puede escribir arcos y capítulos por ti. El esquema no forma parte del prompt de la historia; los generadores y la [Side Query](side-query.md) pueden usarlo cuando se lo pidas.

<a id="the-arcs-tab"></a>

## La pestaña Arcs

La barra lateral izquierda del [editor de la historia](../books/story-editor.md) recibe una pestaña **Arcs** junto a *Outline*. **Add new arc** abre el diálogo de un arco:

| Campo | |
|---|---|
| Title | Obligatorio. |
| Description | Qué sucede en él. |
| Note (private) | Notas para ti mismo. |
| State | **Planned**, **Current** o **Finished**. |
| Linked part | La parte de la historia a la que pertenece el arco o capítulo, ver [más abajo](#linking-to-the-story). |

Los capítulos tienen los mismos campos. La lista muestra cada arco como una tarjeta con sus capítulos sangrados debajo. El color de una tarjeta sigue a su estado: gris para planificados, azul para el actual, verde con el título atenuado para los terminados. Los botones de una tarjeta:

| Botón | |
|---|---|
| calendario / reproducir / check | El estado; abre un menú para cambiarlo. |
| chevron (arcos) | Oculta o muestra los capítulos del arco. |
| chevron (capítulos con nota) | Muestra u oculta la nota del capítulo bajo su descripción, para leerla sin abrir el diálogo. |
| flechas | Mueve el arco o capítulo hacia arriba o hacia abajo. |
| más (arcos) | Añade un capítulo al final del arco. |
| varita (arcos) | Pide al modelo que escriba capítulos para el arco, ver [más abajo](#generating-chapters). |
| chincheta | Vincula (fija) el arco o capítulo a la parte más visible de la historia, ver [más abajo](#linking-to-the-story). |
| diana (arcos y capítulos con parte vinculada) | Desplaza la historia hasta la parte a la que está vinculado. |
| lápiz | Lo abre en el diálogo. |
| papelera | Lo elimina (un arco con todos sus capítulos), tras una confirmación. |
| robot (arcos y capítulos generados) | Muestra lo que escribió el modelo, ver [más abajo](#generating-an-arc). |

Junto a la varita, en la parte superior, hay un lápiz con pluma: redacta las instrucciones del siguiente turno a partir del capítulo actual, ver [más abajo](#drafting-the-instructions-for-the-current-chapter).

![La pestaña Arcs: un arco terminado, uno actual y uno planificado](../../images/plugin-arcoutline-tab.png)

<a id="linking-to-the-story"></a>

### Vincular con la historia

Un arco o un capítulo puede estar **vinculado a una parte de la historia**, para saltar del plan al lugar en el texto. El diálogo tiene el campo **Linked part**: el lugar de la parte en la rama activa, numerado desde 1 como lo muestra la pestaña *Outline* (`#12`). Escríbelo, o pulsa el botón **link** junto a él para tomar la parte que es la **más visible** de la historia en ese momento (desplázate primero al lugar y luego abre el diálogo); el botón **target** desplaza la historia hasta la parte del campo. Un arco o capítulo vinculado también tiene el botón target en su tarjeta. La **chincheta** de una tarjeta hace lo mismo que el botón link sin el diálogo: desplázate hasta donde empieza el capítulo y púlsala. El vínculo es el lugar en la rama, así que apunta a otra parte cuando se eliminan o insertan partes antes de él; cámbialo entonces. Una importación desde la historia vincula los capítulos por ti, ver [más abajo](#importing-an-arc-from-the-story).

<a id="chapter-headings-in-the-text"></a>

### Títulos de capítulo en el texto

**Chapter headings in the text** en el menú de la varita, arriba, escribe un encabezado al inicio de la parte a la que está [vinculado](#linking-to-the-story) cada capítulo, por ejemplo `# Chapter 4: The Storm`, de modo que el texto de la historia lleva sus capítulos (la extensión Chapter Marker y las exportaciones los recogen). El diálogo muestra los encabezados que escribiría, con su parte, a medida que cambias las opciones:

| Opción | |
|---|---|
| Arcs get headings too | Desactivado: solo los capítulos reciben un encabezado (`#`) y los arcos se omiten. Activado: cada arco recibe un encabezado (`#`) y sus capítulos un nivel por debajo (`##`). Un arco que no está vinculado por sí mismo va a la parte de su primer capítulo vinculado. |
| Prefix of the arcs / of the chapters | La palabra antes del número, *Part* y *Chapter* al principio. Vacío da `4. The Storm`. |
| Numbers | Arábigos (1, 2, 3), romanos (I, II, III) o ninguno. Los capítulos se numeran según su lugar en el esquema, estén vinculados o no, de modo que un capítulo aún no escrito deja libre su número. |
| Number the chapters from 1 in every arc | Si no, los capítulos se numeran a lo largo de todo el libro. |
| Put the titles into the headings | Desactivado da solo `Chapter 4`. |
| Remove every other heading of the story first | Se elimina cada línea de encabezado de la rama activa antes de escribir los nuevos, para cuando los capítulos se hayan vinculado a otro lugar desde la última vez. |
| Keep the summaries valid | Activado al principio. Un encabezado no cambia lo que sucede, así que los resúmenes que describían la historia antes del cambio vuelven a marcarse como válidos (los que ya estaban desactualizados siguen desactualizados). |

**Write the headings** los inserta; los encabezados con los que ya empieza una parte se reemplazan, así que volver a ejecutarlo tras un cambio en el esquema no los duplica. **Remove all headings** (tras una confirmación) elimina todas las líneas de encabezado del texto de la rama activa. Cada parte modificada conserva su texto anterior en su historial. Un capítulo terminado o actual sin vínculo, y un vínculo más allá del final de la rama activa, no reciben encabezado y se notifican. Las opciones se recuerdan.

<a id="the-note-of-a-chapter"></a>

### La nota de un capítulo

Un capítulo que tiene una nota muestra un chevron a la derecha. Abre la nota bajo la descripción, en la lista, para que puedas leer los detalles de un capítulo (los que escribió el modelo para él, por ejemplo) sin abrir el diálogo; pulsa de nuevo para ocultar la nota. Qué notas están abiertas no se guarda.

![Un capítulo con su nota visible](../../images/plugin-arcoutline-note.png)

<a id="moving-chapters-between-arcs"></a>

### Mover capítulos entre arcos

Un capítulo que se sube desde el primer lugar de su arco pasa al final del arco anterior; uno que se baja desde el último lugar pasa al inicio del arco siguiente. Sigue pulsando la flecha para llevar un capítulo tan lejos como necesites.

<a id="drafting-the-instructions-for-the-current-chapter"></a>

### Redactar las instrucciones del capítulo actual

El lápiz con pluma junto a *Add new arc* abre la ventana que permite al modelo [redactar las instrucciones](../books/story-editor.md#drafting-the-instructions) del siguiente turno, con los datos del capítulo en el que está la historia como su **Plan of the Story**. El capítulo actual es el primero marcado como **Current**; si no hay ninguno, el primero que no esté **Finished**; un arco marcado como actual que aún no tiene capítulos se queda solo. El modelo recibe ese arco y capítulo con sus descripciones y notas, y el título y la descripción (no la nota) del capítulo siguiente, con la advertencia de que la historia no debe llegar todavía allí. La ventana empieza con lo que escribiste en *Instructions* del panel del nuevo turno como guía; **Use as Instructions** pone el borrador en ese panel y lo abre. Sin capítulo actual, el botón lo indica.

Los mismos datos son una fuente para la [Side Query](side-query.md), ver [más abajo](#the-outline-as-a-source-for-other-extensions).

<a id="finishing-an-arc"></a>

### Terminar un arco

Marcar un arco como **Finished** marca también todos sus capítulos como **Finished**. Cambiar un arco a otro estado deja sus capítulos como están.

El esquema se guarda con el libro en cuanto lo cambias, así que se incluye en sus copias de seguridad.

<a id="the-outline-as-a-source-for-other-extensions"></a>

## El esquema como fuente para otras extensiones

El esquema se ofrece a otras extensiones como fuente de datos sobre el libro: en [Side Query](side-query.md) marca **Arc outline** en el popover del plug para enviar los arcos, sus estados, descripciones y capítulos con tus preguntas. Hay dos versiones: **Arc outline** es la concisa (solo descripciones), **Arc outline (with notes)** añade la nota de cada arco y capítulo bajo su descripción, que es donde están los detalles de los arcos y capítulos generados. Tus notas privadas solo se envían si marcas la segunda. Una tercera fuente, **Arc outline: current chapter**, envía solo el arco y el capítulo en el que está la historia (ver [Redactar las instrucciones del capítulo actual](#drafting-the-instructions-for-the-current-chapter)).
<a id="generating-an-arc"></a>

## Generar un arco

El botón de varita junto a **Add new arc** abre un menú con tres elementos: **Generate an arc** pide al modelo que escriba un arco nuevo, **Import from story** resume lo que ya has escrito, véase [más abajo](#importing-an-arc-from-the-story), y **Develop the plot** es una conversación con el modelo sobre hacia dónde va la historia, véase [más abajo](#developing-the-plot-in-a-conversation).

![El menú de la varita](../../images/plugin-arcoutline-menu.png)

 El diálogo de **Generate an arc** tiene dos mitades.

A la izquierda:

| | |
|---|---|
| Prompt | Las instrucciones para el modelo, con tres variables: `{{backgroundLore}}` (las entradas del lorebook del libro y de sus sublorebooks), `{{existingArcs}}` (tus arcos y capítulos, con sus estados y descripciones, pero no tus notas privadas) y `{{specification}}`. Al principio es el prompt de *Settings → Extension Settings → Arc Outline* (el prompt por defecto si no lo has cambiado); lo que edites aquí se aplica solo a esta generación. |
| Specification | Lo que quieres del arco: el tema, los personajes, hacia dónde debe llevar. Puede quedar vacío. |
| Tools | Solo cuando hay instalada una suite de [herramientas para el modelo](tools.md) (nombres, lugares): qué suites, y qué categorías de ellas, puede usar el modelo en lugar de inventar cosas, igual que en el [Generador de lore](lore-generator.md). |
| Model, Protocols | Empiezan con el modelo y el protocolo del libro (tus predeterminados de *Settings* cuando el libro no tiene ninguno). Cámbialos para usar otro modelo solo en esta generación; la elección no se guarda. |
| Generate | Inicia el modelo. Mientras se ejecuta, el botón es un **Stop** rojo y todo lo demás del diálogo está deshabilitado. |

A la derecha ves el **razonamiento** del modelo a medida que se escribe y, en cuanto empieza la respuesta, el **arco** que propone: un nombre, una descripción breve y una nota más larga, mostrado como un arco del esquema (la nota debajo de su descripción) y creciendo a medida que llega la respuesta. La descripción es lo que muestra el esquema, así que se pide al modelo que la mantenga en un par de frases y que ponga los detalles en la nota, que acaba en la **Note** del arco guardado. Cuando el modelo termina, se comprueba su respuesta; si no es un arco válido (por ejemplo, si falta la nota), el diálogo lo indica y el arco no se puede guardar.

![Generando un arco](../../images/plugin-arcoutline-generate-arc.png)

**Save as arc** añade el arco al final del esquema. Para intentarlo de nuevo, pulsa **Generate** otra vez, con el mismo prompt o con uno cambiado, y con la misma especificación o con otra; la respuesta anterior se reemplaza.

<a id="importing-an-arc-from-the-story"></a>

## Importar un arco desde la historia

**Import from story** en el menú de la varita pide al modelo que lea una parte de tu historia y la escriba como un arco con sus capítulos, para que puedas llevar a esquema un libro que ya has escrito. El modelo lee siempre la [rama activa](../books/branches-and-story-tree.md) del libro, tal como está cuando abres el diálogo. El diálogo es como el de los arcos, con estas opciones a la izquierda:

| | |
|---|---|
| From message, To message | El rango de la historia que se envía. Los mensajes de la rama que tienen texto se numeran desde 1; al principio está elegida toda la rama. Bajo cada campo ves el inicio del mensaje al que apunta. |
| Number of chapters (optional) | Elige un número para obtener exactamente esa cantidad de capítulos (una respuesta con otro número se rechaza); déjalo vacío para que el modelo divida la historia donde se divide de forma natural. |
| Link the chapters to the parts of the story | Activado al principio. Los mensajes se numeran desde 1 dentro del rango que envías, y el modelo indica en qué mensaje empieza cada capítulo; el capítulo se [enlaza](#linking-to-the-story) a esa parte de la historia (numerada en toda la rama, así que es correcto sea cual sea el rango), y el arco, a la parte en que empieza su primer capítulo. Una respuesta que nombra un mensaje que no se envió deja el capítulo sin enlazar. |
| State of the imported arc and its chapters | **Finished** al principio, porque la historia está escrita; elige **Current** o **Planned** si lo necesitas. |
| Token counter | Cuántos mensajes tiene el rango y sus tokens aproximados, y los tokens aproximados de todo el prompt (instrucciones, lore, esquema e historia) junto a lo que toma el modelo. Se convierte en una advertencia cuando el prompt no cabe; entonces elige menos mensajes. Sigue al rango, al número de capítulos, al prompt y al modelo. |
| Prompt | Las instrucciones para el modelo, con las variables `{{backgroundLore}}`, `{{existingArcs}}`, `{{story}}` (los mensajes del rango) y `{{numberOfChapters}}`. Al principio es el **Story import prompt** de *Settings → Extension Settings → Arc Outline*. |

![Importando un arco desde la historia](../../images/plugin-arcoutline-import.png)

No hay herramientas aquí, porque los nombres están en la historia. A la derecha ves el razonamiento, y después el arco y sus capítulos según los escribe el modelo. **Save as arc** añade el arco con sus capítulos al final del esquema; cada uno de ellos conserva el razonamiento y la respuesta del modelo y tiene su propio botón de robot. Una historia larga se importa mejor en varios pasos, con un arco por cada rango.
<a id="developing-the-plot-in-a-conversation"></a>

## Desarrollar la trama en una conversación

**Develop the plot** en el menú de la varita abre un chat con el modelo sobre la trama. Nada del libro cambia al conversar: el modelo ve lo que decidas enviarle y responde. Cuando hayas acordado algo, se lo pasas al generador de arcos con un clic.

A la izquierda eliges lo que ve el modelo y cómo ayuda (las elecciones se recuerdan para la próxima vez); a la derecha está el chat:

![El asistente de trama: las opciones a la izquierda, la conversación a la derecha](../../images/plugin-arcoutline-wizard.png)

| | |
|---|---|
| Model, Protocols | Empiezan con el modelo y el protocolo del libro. Si los cambias, el cambio se aplica solo a esta conversación. |
| How the model helps | **Free talk** (tú diriges), **Explore directions** (varias formas distintas en que puede seguir la historia), **Find the problems** (hilos que nunca se resuelven, promesas que nadie cumple, contradicciones con el lore, ritmo) o **Expand an idea** (tu idea desarrollada como premisa, giros, un final y capítulos que encajen después de tus arcos). |
| Send the lorebook | Las entradas del lorebook del libro y de sus sub lorebooks. |
| Send the outline | **Not sent**, **Descriptions only** o **With the notes** (también tus notas privadas). |
| Send the notes | Solo cuando [Story Notes](story-notes.md) está instalado: **Not sent**, **Titles and first lines** (el valor por defecto) o **Full text** de tus notas, sin las páginas que ocultaste a los asistentes. |
| Send the summaries of the story | Los resúmenes hacen las veces de la historia hasta el más reciente de ellos, igual que los envía una generación. |
| Send the latest parts of the story | Las partes posteriores al resumen más reciente (todas, si los resúmenes están desactivados), las más recientes que caben en **Tokens of the story**. Cada parte se muestra al modelo con su número en el esquema de la historia. |
| Tokens of the story | El máximo de tokens de partes de la historia. Vacío: un tercio de lo que admite el modelo. |
| Prompt | Las instrucciones para el modelo, con las variables `{{backgroundLore}}`, `{{outline}}`, `{{notes}}`, `{{summaries}}`, `{{story}}` y `{{mode}}`. El cambio se recuerda. |

Debajo de las opciones, una línea indica cuántos resúmenes y partes de la historia recibe el modelo, y cuántos tokens ocupa la petición de lo que el modelo admite. Si no cabe, la línea se convierte en una advertencia: envía menos o [resume el chat](#chats-branches-and-summaries). El libro se vuelve a leer con cada mensaje, así que un cambio en el esquema o en la historia lo ve la siguiente respuesta.

A la derecha está el chat. Escribe un mensaje y pulsa **Send** (mientras el modelo escribe, el botón es **Stop**; lo que ya llegó se conserva). Cada mensaje tiene botones: copiar, **branch from here**, borrar y, en una respuesta del modelo, también una varita, **Generate an arc from this message**, que abre el generador de arcos con el mensaje como especificación. Si la conversación fue larga, pide primero al modelo que resuma en una especificación lo que decidiste.

<a id="letting-the-model-change-the-book"></a>

### Dejar que el modelo cambie el libro

Debajo de las opciones está **What the model may do**. Si no marcas ninguno de estos grupos, el chat es solo una conversación. Al marcar un grupo, el modelo recibe herramientas para ese grupo y puede usarlas cuando se lo pidas o cuando estéis de acuerdo. Se le indica que mire antes de añadir, que prefiera añadir a reemplazar y que diga lo que hizo:

| Group | The model can |
|---|---|
| **Planner** | listar las escenas del [planificador](../books/story-editor.md#the-planner-tab), añadirlas, cambiarlas, moverlas, borrarlas y marcarlas como hechas. |
| **Lorebook** | buscar y leer el lore, crear entradas y cambiar las existentes (no borrarlas). |
| **Outline** | leer el esquema, añadir arcos y capítulos, cambiarlos y moverlos (no borrarlos). |
| **Notes** (cuando [Story Notes](story-notes.md) está instalado) | buscar y leer tus notas y seguir sus enlaces, añadir una página o añadir texto a una cuando le pidas que guarde algo, y cambiar el texto de una (no borrar una página). Las páginas que ocultaste a los asistentes quedan fuera de su alcance. |
| **Characters** (cuando el [Character Tracker](character-tracker.md) está instalado) | leer los personajes del libro y sus relaciones, añadir personajes y relaciones, cambiarlos (nombre, lema, nota, color) y borrarlos. Solo se pide confirmación antes de borrar; un cambio no la requiere: lo ves en el chat y en la pestaña Characters. El modelo puede nombrar a un personaje por su número en la lista; la pregunta y el chat muestran el nombre del personaje junto a él. |
| **Generators** | llamar a los generadores de arcos y de capítulos y, cuando el [Lore Generator](lore-generator.md) está instalado, generar una entrada de lore. Devuelven un borrador y no guardan nada, salvo que se le indique al modelo; te muestra el borrador y lo guarda cuando estés de acuerdo. |
| **Tools** (botón, cuando hay una [suite de herramientas](tools.md) instalada) | las herramientas de nombres (nombres de personas y lugares), como en los generadores, para que pueda ofrecerte nombres reales. |

- **Ask me before it changes or deletes something** (activado por defecto): los cambios en el texto de algo existente (una entrada de lore, una escena, un arco) y los borrados se te muestran primero, con lo que el modelo quiere pasar, y el modelo espera a que pulses **Allow** o **Refuse**. Añadir una escena, una entrada o un arco no se pregunta: lo ves en el chat y en el planificador, el lorebook o el esquema, y puedes borrarlo.
- Cada herramienta que llamó el modelo aparece bajo su respuesta (una línea con el nombre y una marca: ✓ funcionó, ✗ falló; ábrela para ver lo que se pasó y lo que devolvió). Los mensajes siguientes incluyen las llamadas que hizo el modelo y lo que devolvieron (un resultado largo se recorta), así sabe lo que ya hizo y no crea la misma entrada otra vez; cuentan para el límite de tokens del chat.
- Lo que cuestan las herramientas: sus descripciones forman parte de cada petición, así que un modelo con poco contexto tiene menos espacio. Solo funcionan con modelos que pueden llamar a funciones; el diálogo lo indica cuando un modelo no puede.

![El modelo añadió dos escenas al planificador; sus llamadas aparecen bajo la respuesta](../../images/plugin-arcoutline-wizard-tools.png)

<a id="chats-branches-and-summaries"></a>

### Chats, ramas y resúmenes

Un libro puede tener cuantos chats quieras; eliges uno en la lista sobre los mensajes, **+** inicia uno nuevo, el lápiz lo renombra (hasta entonces lleva el nombre de tu primer mensaje) y el cubo de basura lo borra. Se guardan junto con el libro.

- **Branch** copia el chat tal como está en uno nuevo; el botón de rama de un mensaje copia el chat hasta ese mensaje inclusive, para llevar la conversación por otro camino desde allí. La copia no tiene relación con el original después.
- **Summarize the chat** primero pregunta qué debe conservar o destacar el resumen (opcional, por ejemplo «conserva el giro sobre el traidor y el final que rechazamos»; vacío: decide el modelo). Después pide al modelo un resumen del chat (lo que decidiste, lo que rechazaste, lo que sigue abierto y los nombres que quedaron fijados) y lo añade como un mensaje. A partir de ahí, el modelo recibe ese resumen y los mensajes posteriores, **no** los mensajes anteriores, que siguen en pantalla atenuados. Úsalo cuando el chat se haya vuelto demasiado largo para el modelo, o para empezar desde un estado limpio sin perder las decisiones. Si resumes de nuevo más adelante, el nuevo resumen incluye el anterior. Si quieres que el modelo conserve todo el chat, haz antes una rama.

![La pregunta sobre lo que debe conservar el resumen](../../images/plugin-arcoutline-wizard-summarize.png)

![El chat después del resumen: los mensajes anteriores siguen en pantalla, atenuados](../../images/plugin-arcoutline-wizard-summary.png)
<a id="generating-chapters"></a>

## Generación de capítulos

El botón de la varita mágica de un arco pide al modelo que divida ese arco en capítulos. El diálogo es como el de los arcos, con estas opciones a la izquierda:

| | |
|---|---|
| Qué sabe el modelo de la historia | **Whole story so far**: los arcos hasta este, con todos sus capítulos. **Arc summaries only**: los mismos arcos sin capítulos. **Arc summaries and finished chapters**: los mismos arcos con solo los capítulos que marcaste como *Finished*. Los arcos posteriores a este nunca se envían. |
| Qué quieres de los capítulos en general | Ritmo, tono, lo que debe ocurrir. Puede estar vacío. |
| Número de capítulos | Escríbelo con palabras ("about eight") o elige un número exacto. Un número añade un campo de texto para cada capítulo, donde puedes indicar lo que debe contener; todos son opcionales. Usar uno de los dos borra el otro. Con un número exacto, una respuesta con otra cantidad de capítulos se rechaza. |
| Prompt | Las instrucciones para el modelo, con las variables `{{backgroundLore}}`, `{{outline}}`, `{{numberOfChapters}}`, `{{generalSpecification}}` y `{{chapterSpecification}}`. Empieza con el **Chapter generation prompt** de *Settings → Extension Settings → Arc Outline*. |

A la derecha ves el razonamiento, luego el arco con los capítulos que ya tiene y los capítulos nuevos conforme el modelo los escribe. **Save as chapters** los añade al final del arco, cada uno con una descripción breve y los detalles en su nota (como en los arcos). Cada capítulo nuevo conserva el razonamiento y la respuesta del modelo, y tiene su propio botón del robot.

![Generación de los capítulos de un arco](../../images/plugin-arcoutline-generate-chapters.png)

Un arco o capítulo escrito por el modelo conserva su **razonamiento** y la **respuesta** de la que procede. El botón del robot que lleva abre ambos en un pequeño emergente, sin estorbar.

![Lo que el modelo escribió para un arco](../../images/plugin-arcoutline-generated.png)

<a id="commands"></a>

## Comandos

El esquema y sus generadores también son [comandos](../commands/index.md), para la línea de comandos y para [agentes](../agents.md). Trabajan sobre el libro abierto (en el caso de un agente, sobre su copia de trabajo), y una pestaña Arcs abierta muestra el cambio al instante. Los arcos y los capítulos se indican por **número**, tal como los muestra el esquema: el primer arco es `1`, el primer capítulo de un arco es `1` (los números cambian cuando se añade, elimina o mueve algo anterior a ellos). Un comando que modifica un arco o un capítulo toma solo los campos que le das; un texto vacío borra una descripción o una nota.

| Comando | Qué hace |
|---|---|
| `/get-arc-outline [arc [notes]]` | Los arcos con sus capítulos, estados y descripciones en JSON. Las notas privadas solo con `notes` `true`; `arc` devuelve solo ese arco. Solo lee. |
| `/add-arc title [description [note [state [position]]]]` | Añade un arco, al final o en `position`. |
| `/update-arc arc [title [description [note [state]]]]` | Modifica un arco. `state` es `PLANNED`, `CURRENT` o `FINISHED` (terminar un arco termina sus capítulos). |
| `/move-arc arc position` | Mueve un arco a la posición `position` (1 es la primera). |
| `/delete-arc arc` | Elimina un arco con sus capítulos, previa confirmación. |
| `/add-outline-chapter arc title [description [note [state [position]]]]` | Añade un capítulo a un arco. |
| `/update-outline-chapter arc chapter [title [description [note [state]]]]` | Modifica un capítulo. |
| `/move-outline-chapter arc chapter position [to_arc]` | Mueve un capítulo dentro de su arco, o al arco `to_arc`. |
| `/delete-outline-chapter arc chapter` | Elimina un capítulo, previa confirmación. |
| `/generate-arc [specification [save]]` | Pide al modelo un arco nuevo, como hace *Generate an arc*. |
| `/generate-outline-chapters arc [specification [count [context [save]]]]` | Pide al modelo los capítulos de un arco, como lo hace la varita del arco. `count` es un número exacto de capítulos; `context` es `EVERYTHING` (el valor por defecto), `ARCS_ONLY` o `ARCS_AND_FINISHED`, como en la opción *What the model knows of the story*. |
| `/import-arc-from-story [from [to [count [state [save [mapParts]]]]]]` | Pide al modelo que resuma los mensajes `from` a `to` de la rama activa (numerados entre los mensajes con texto, como en *Import from story*) como un arco con capítulos. `state` (por defecto `FINISHED`) es el estado del arco guardado y sus capítulos. Con `mapParts` `true` cada capítulo se vincula al mensaje en el que empieza (su *Linked part*, como hace la casilla *Map parts* del diálogo), y la respuesta incluye la parte de cada capítulo. |

Los tres comandos de generación usan los mismos prompts que los diálogos (tu *Extension Settings → Arc Outline*, o los valores por defecto), el modelo y el protocolo del libro (tu modelo por defecto si el libro no tiene uno) y ninguna herramienta: quien llame que quiera nombres los obtiene con los comandos del [Name generator](name-generator.md) o del [Name provider](name-provider.md) y los escribe en la especificación. Igual que los diálogos, solo guardan cuando se les pide: con `save` `false` (el valor por defecto) devuelven el arco o los capítulos como JSON y no guardan nada; con `save` `true` los añaden al esquema (un arco al final, los capítulos al final del arco) y la respuesta incluye sus números. Una historia demasiado larga para el modelo es rechazada por `/import-arc-from-story`: elige menos mensajes. Los comandos de generación tardan un rato y se detienen cuando se detiene la ejecución.

Añadir y modificar no es destructivo; solo los dos comandos `delete` lo son, y un agente puede configurarse para pedirte confirmación.
