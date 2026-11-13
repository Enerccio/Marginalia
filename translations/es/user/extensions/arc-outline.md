---
label: Esquema de arcos
order: 24
---

# Esquema de arcos

El plan de tu libro en la barra lateral: arcos, cada uno dividido en capítulos, con el estado de cada uno de ellos, y un modelo que puede escribir arcos y capítulos por ti. El esquema no forma parte del prompt de la historia; los generadores y la [Side Query](side-query.md) pueden usarlo cuando se lo pidas.

<a id="the-arcs-tab"></a>

## La pestaña Arcs

La barra lateral izquierda del [editor de historias](../books/story-editor.md) tiene una pestaña **Arcs** junto a *Esquema*. **Add new arc** abre el diálogo de un arco:

| Campo | |
|---|---|
| Title | Obligatorio. |
| Description | Lo que ocurre en él. |
| Note (private) | Notas para ti. |
| State | **Planned**, **Actual** o **Finished**. |
| Linked part | La parte de la historia a la que pertenece el arco o el capítulo, ver [abajo](#linking-to-the-story). |

Los capítulos tienen los mismos campos. La lista muestra cada arco como una tarjeta, con sus capítulos sangrados debajo. El color de una tarjeta sigue a su estado: gris para los planificados, azul para el actual y verde con el título atenuado para los terminados. Los botones de una tarjeta:

| Botón | |
|---|---|
| calendario / play / check | El estado; abre un menú para cambiarlo. |
| chevron (arcos) | Oculta o muestra los capítulos del arco. |
| chevron (capítulos con una nota) | Muestra u oculta la nota del capítulo bajo su descripción, para leerla sin abrir el diálogo. |
| flechas | Mueven el arco o el capítulo hacia arriba o hacia abajo. |
| plus (arcos) | Añade un capítulo al final del arco. |
| wand (arcos) | Pide al modelo que escriba capítulos para el arco, ver [abajo](#generating-chapters). |
| target (arcos y capítulos con una parte vinculada) | Desplaza la historia a la parte a la que está vinculado. |
| pen | Lo abre en el diálogo. |
| trash | Lo elimina (un arco con todos sus capítulos), tras una confirmación. |
| robot (arcos y capítulos generados) | Muestra lo que escribió el modelo, ver [abajo](#generating-an-arc). |

Junto a la varita, arriba, hay un lápiz con una pluma: redacta las instrucciones del siguiente turno a partir del capítulo actual, ver
[abajo](#drafting-the-instructions-for-the-current-chapter).

![La pestaña Arcs: un arco terminado, uno actual y uno planificado](../../images/plugin-arcoutline-tab.png)

<a id="linking-to-the-story"></a>

### Vincular con la historia

Un arco o un capítulo puede **vincularse a una parte de la historia**, para saltar del plan al lugar del texto. El diálogo tiene
el campo **Linked part**: el lugar de la parte en la rama activa, numerado desde 1 como lo muestra la pestaña *Esquema* (`#12`).
Escríbelo, o pulsa el botón **link** junto a él para tomar la parte que es **más visible** en la historia en ese momento (desplázate
primero al lugar y luego abre el diálogo); el botón **target** desplaza la historia a la parte que indica el campo. Un arco o capítulo
vinculado también tiene el botón target en su tarjeta. El vínculo es el lugar dentro de la rama, así que apunta a otra parte cuando se
eliminan o insertan partes antes de ella; en ese caso, cámbialo. Una importación desde la historia vincula los capítulos por ti, ver
[abajo](#importing-an-arc-from-the-story).

<a id="the-note-of-a-chapter"></a>

### La nota de un capítulo

Un capítulo que tiene nota muestra un chevron a la derecha. Al pulsarlo se despliega la nota bajo la descripción, en la lista, para
que puedas leer los detalles de un capítulo (los que escribió el modelo, por ejemplo) sin abrir el diálogo; vuelve a pulsarlo para
ocultarla. Qué notas están abiertas no se guarda.

![Un capítulo con su nota desplegada](../../images/plugin-arcoutline-note.png)

<a id="moving-chapters-between-arcs"></a>

### Mover capítulos entre arcos

Un capítulo que subes desde el primer puesto de su arco pasa al final del arco anterior; uno que bajas desde el último puesto pasa al
inicio del arco siguiente. Sigue pulsando la flecha para llevar un capítulo tan lejos como necesites.

<a id="drafting-the-instructions-for-the-current-chapter"></a>

### Redactar las instrucciones del capítulo actual

El lápiz con una pluma junto a *Add new arc* abre la ventana que permite al modelo
[redactar las instrucciones](../books/story-editor.md#drafting-the-instructions) del siguiente turno, con los datos del capítulo en el que
está la historia como su **Plan de la historia**. El capítulo actual es el primero marcado como **Actual**; si no hay ninguno, el primero
que no esté **Finished**; un arco marcado como actual que todavía no tiene capítulos se usa solo. El modelo recibe ese arco y ese capítulo
con sus descripciones y notas, y el título y la descripción (no la nota) del capítulo siguiente, con la advertencia de que la historia no
debe llegar todavía allí. La ventana empieza con lo que escribiste en *Instrucciones* del panel del nuevo turno como guía; **Use as
Instructions** pone el borrador en ese panel y lo abre. Sin capítulo actual, el botón lo indica.

Los mismos datos son una fuente para la [Side Query](side-query.md), ver [abajo](#the-outline-as-a-source-for-other-extensions).

<a id="finishing-an-arc"></a>

### Terminar un arco

Marcar un arco como **Finished** marca también como **Finished** todos sus capítulos. Cambiar un arco a otro estado deja sus
capítulos como están.

El esquema se guarda con el libro en cuanto lo cambias, así que se incluye en sus copias de seguridad.

<a id="the-outline-as-a-source-for-other-extensions"></a>

## El esquema como fuente para otras extensiones

El esquema se ofrece a otras extensiones como fuente de datos sobre el libro: en la [Side Query](side-query.md), marca
**Arc outline** en el menú del enchufe para enviar los arcos, sus estados, descripciones y capítulos junto con tus preguntas. Hay dos
versiones: **Arc outline** es la breve (solo descripciones), y **Arc outline (with notes)** añade la nota de cada arco y capítulo bajo su
descripción, que es donde están los detalles de los arcos y capítulos generados. Tus notas privadas solo se envían si marcas la segunda.
Una tercera fuente, **Arc outline: current chapter**, envía solo el arco y el capítulo en los que está la historia (ver
[Redactar las instrucciones del capítulo actual](#drafting-the-instructions-for-the-current-chapter)).

<a id="generating-an-arc"></a>

## Generar un arco

El botón de la varita junto a **Add new arc** abre un menú con tres elementos: **Generate an arc** pide al modelo que escriba un arco nuevo,
**Import from story** resume lo que ya has escrito, ver [abajo](#importing-an-arc-from-the-story), y **Develop the plot** es una conversación
con el modelo sobre hacia dónde va la historia, ver [abajo](#developing-the-plot-in-a-conversation).

![El menú de la varita](../../images/plugin-arcoutline-menu.png)

 El diálogo de **Generate an arc** tiene dos mitades.

A la izquierda:

| | |
|---|---|
| Prompt | Las instrucciones para el modelo, con tres variables: `{{backgroundLore}}` (las entradas del lorebook del libro y de sus sub lorebooks), `{{existingArcs}}` (tus arcos y capítulos, con sus estados y descripciones, pero no tus notas privadas) y `{{specification}}`. Empieza con el prompt de *Ajustes → Extension Settings → Arc Outline* (el prompt por defecto si no lo has cambiado); lo que edites aquí se aplica solo a esta generación. |
| Specification | Lo que quieres del arco: el tema, los personajes, hacia dónde debe llevar. Puede estar vacío. |
| Tools | Solo cuando hay instalada una suite de [herramientas para el modelo](tools.md) (nombres, lugares): qué suites, y qué categorías de ellas, puede usar el modelo en lugar de inventar cosas, igual que en el [Generador de lore](lore-generator.md). |
| Model, Protocols | Empiezan con el modelo y el protocolo del libro (tus valores por defecto de *Ajustes* cuando el libro no tiene ninguno). Cámbialos para usar otro modelo solo en esta generación; la elección no se guarda. |
| Generate | Inicia el modelo. Mientras se ejecuta, el botón es un **Detener** rojo y el resto del diálogo está deshabilitado. |

A la derecha ves el **razonamiento** del modelo mientras se escribe y, en cuanto empieza la respuesta, el **arco** que propone: un nombre,
una descripción breve y una nota más larga, mostrado como un arco del esquema (la nota bajo su descripción) y creciendo a medida que llega
la respuesta. La descripción es lo que muestra el esquema, así que se pide al modelo que la limite a un par de frases y que ponga los
detalles en la nota, que acaba en la **Nota** del arco guardado. Cuando el modelo termina, su respuesta se comprueba; si no es un arco válido
(por ejemplo, falta la nota), el diálogo lo indica y el arco no se puede guardar.

![Generando un arco](../../images/plugin-arcoutline-generate-arc.png)

**Save as arc** añade el arco al final del esquema. Para volver a intentarlo, pulsa **Generar** una vez más, con el mismo prompt o
especificación o uno cambiado; la respuesta anterior se reemplaza.
<a id="importing-an-arc-from-the-story"></a>

## Importar un arco desde la historia

**Importar desde la historia** en el menú de la varita pide al modelo que lea una parte de tu historia y la escriba como un arco
con sus capítulos, para que puedas llevar a la escaloneta un libro que ya has escrito. El modelo siempre lee la
[rama activa](../books/branches-and-story-tree.md) del libro, tal como está cuando abres el diálogo. El diálogo es como
el de los arcos, con estas opciones a la izquierda:

| | |
|---|---|
| Desde el mensaje, Hasta el mensaje | El rango de la historia que se envía. Los mensajes de la rama que tienen texto se numeran desde 1; al principio se elige toda la rama. Debajo de cada campo ves el comienzo del mensaje al que apunta. |
| Número de capítulos (opcional) | Elige un número para obtener exactamente esa cantidad de capítulos (una respuesta con otro número se rechaza); deja el campo vacío para que el modelo divida la historia donde se divide de forma natural. |
| Vincular los capítulos a las partes de la historia | Activado al principio. Los mensajes se numeran desde 1 dentro del rango que envías, y el modelo indica en qué mensaje empieza cada capítulo; el capítulo queda [vinculado](#linking-to-the-story) a esa parte de la historia (según la numeración de toda la rama, así que es correcto sea cual sea el rango), y el arco a la parte en la que empieza su primer capítulo. Una respuesta que nombra un mensaje que no se envió deja el capítulo sin vincular. |
| Estado del arco importado y de sus capítulos | **Terminado** al principio, porque la historia está escrita; elige **Actual** o **Planificado** si lo necesitas. |
| Contador de tokens | Cuántos mensajes tiene el rango y sus tokens aproximados, y los tokens aproximados de todo el prompt (instrucciones, lore, escaloneta e historia) junto a lo que toma el modelo. Se convierte en una advertencia cuando el prompt no cabe; en ese caso elige menos mensajes. Se actualiza con el rango, el número de capítulos, el prompt y el modelo. |
| Prompt | Las instrucciones para el modelo, con las variables `{{backgroundLore}}`, `{{existingArcs}}`, `{{story}}` (los mensajes del rango) y `{{numberOfChapters}}`. Empieza con el **Prompt de importación de historia** de *Ajustes → Ajustes de extensiones → Arc Outline*. |

![Importar un arco desde la historia](../../images/plugin-arcoutline-import.png)

Aquí no hay herramientas; los nombres están en la historia. A la derecha ves el razonamiento y luego el arco y sus
capítulos tal como los escribe el modelo. **Guardar como arco** añade el arco con sus capítulos al final de la escaloneta; cada uno
de ellos conserva el razonamiento y la respuesta del modelo y tiene su propio botón de robot. Una historia larga se importa mejor
en varios pasos, un arco por cada rango.
<a id="developing-the-plot-in-a-conversation"></a>

## Desarrollar la trama en una conversación

**Desarrollar la trama** en el menú de la varita abre un chat con el modelo sobre la trama. Hablar no cambia nada del libro: el modelo ve lo que tú decides enviarle, responde, y cuando os hayáis puesto de acuerdo en algo, lo pasas al generador de arcos con un clic.

A la izquierda eliges lo que ve el modelo y cómo ayuda (las elecciones se recuerdan para la próxima vez); a la derecha está el chat:

![El asistente de trama: las opciones a la izquierda, la conversación a la derecha](../../images/plugin-arcoutline-wizard.png)

| | |
|---|---|
| Modelo, Protocolos | Empiezan con el modelo y el protocolo del libro. Cambiarlos solo se aplica a esta conversación. |
| Cómo ayuda el modelo | **Charla libre** (diriges tú), **Explorar direcciones** (varias formas distintas en que puede seguir la historia), **Encontrar los problemas** (hilos que nunca se resuelven, promesas que nadie cumple, contradicciones con el lore, ritmo) o **Desarrollar una idea** (tu idea convertida en premisa, giros, un final y capítulos que encajen después de tus arcos). |
| Enviar el lorebook | Las entradas del lorebook del libro y de sus sublorebooks. |
| Enviar el esquema | **No enviar**, **Solo descripciones** o **Con las notas** (tus notas privadas también). |
| Enviar las notas | Solo cuando [Notas de la historia](story-notes.md) está instalado: **No enviar**, **Títulos y primeras líneas** (el valor por defecto) o **Texto completo** de tus notas, sin las páginas que ocultaste a los asistentes. |
| Enviar los resúmenes de la historia | Los resúmenes sustituyen a la historia hasta el más reciente de ellos, igual que lo envía una generación. |
| Enviar las últimas partes de la historia | Las partes posteriores al resumen más reciente (todas, si los resúmenes están desactivados), las más recientes que caben en **Tokens de la historia**. Cada parte se muestra al modelo con su número en el esquema de la historia. |
| Tokens de la historia | El máximo de tokens de partes de la historia. Vacío: un tercio de lo que admite el modelo. |
| Prompt | Las instrucciones para el modelo, con las variables `{{backgroundLore}}`, `{{outline}}`, `{{notes}}`, `{{summaries}}`, `{{story}}` y `{{mode}}`. El cambio se recuerda. |

Bajo las opciones, una línea indica cuántos resúmenes y partes de la historia recibe el modelo, y cuántos tokens de lo que admite ocupa la petición. Se convierte en una advertencia cuando no cabe; envía menos o [resume el chat](#chats-branches-and-summaries). El libro se vuelve a leer en cada mensaje, así que un cambio del esquema o de la historia lo verá la siguiente respuesta.

A la derecha está el chat. Escribe un mensaje y pulsa **Enviar** (es **Detener** mientras el modelo escribe; lo que ya llegó se conserva). Cada mensaje tiene botones: copiar, **rama desde aquí**, eliminar y, en una respuesta del modelo, también una varita, **Generar un arco desde este mensaje**, que abre el generador de arcos con el mensaje como especificación. Pide primero al modelo que resuma lo decidido como especificación si la conversación fue larga.

<a id="letting-the-model-change-the-book"></a>

### Dejar que el modelo cambie el libro

Bajo las opciones está **Lo que el modelo puede hacer**. Con ninguna de estas casillas marcada, el chat es solo una charla. Marca un grupo y el modelo recibe herramientas para él, que puede usar cuando se lo pidas o lo acordéis; se le indica que mire antes de añadir, que prefiera añadir a reemplazar, y que diga qué hizo:

| Grupo | El modelo puede |
|---|---|
| **Planificador** | listar las escenas del [planificador](../books/story-editor.md#the-planner-tab), añadirlas, cambiarlas, moverlas, eliminarlas y marcarlas como hechas. |
| **Lorebook** | buscar y leer el lore, crear entradas y cambiar las existentes (no eliminarlas). |
| **Esquema** | leer el esquema, añadir arcos y capítulos, cambiarlos y moverlos (no eliminarlos). |
| **Notas** (cuando [Notas de la historia](story-notes.md) está instalado) | buscar y leer tus notas y seguir sus enlaces, añadir una página o añadir a una cuando le pidas que guarde algo, y cambiar el texto de una (no eliminar una página). Las páginas que ocultaste a los asistentes quedan fuera de su alcance. |
| **Generadores** | llamar a los generadores de arcos y de capítulos y, cuando el [Generador de lore](lore-generator.md) está instalado, generar una entrada de lore. Devuelven un borrador y no guardan nada salvo que se le indique al modelo; te enseña el borrador y lo guarda cuando estés de acuerdo. |
| **Herramientas** (botón, cuando hay instalado un [conjunto de herramientas](tools.md)) | las herramientas de nombres (nombres, lugares), como en los generadores, para que pueda ofrecerte nombres reales. |

- **Pregúntame antes de cambiar o eliminar algo** (activado por defecto): cambiar el texto de lo que ya existe (una entrada de lore, una escena, un arco) y eliminar se te muestran primero, con lo que el modelo quiere pasar, y el modelo espera a **Permitir** o **Rechazar**. Añadir una escena, una entrada o un arco no se pregunta: lo ves en el chat y en el planificador, el lorebook o el esquema, y puedes eliminarlo.
- Cada herramienta que llamó el modelo aparece listada bajo su respuesta (una línea con el nombre y una marca: ✓ funcionó, ✗ falló; ábrela para ver qué se pasó y qué devolvió).
  Los mensajes siguientes llevan las llamadas que hizo el modelo y lo que devolvieron (un resultado largo se corta), así sabe lo que ya hizo y no vuelve a crear la misma entrada; cuentan contra el límite de tokens del chat.
- Lo que cuestan las herramientas: sus descripciones forman parte de cada petición, así que un modelo con poco contexto tiene menos espacio. Solo funcionan los modelos que pueden llamar a funciones; el diálogo lo indica cuando uno no puede.

![El modelo añadió dos escenas al planificador; sus llamadas aparecen listadas bajo la respuesta](../../images/plugin-arcoutline-wizard-tools.png)

<a id="chats-branches-and-summaries"></a>

### Chats, ramas y resúmenes

Un libro tiene cuantos chats quieras; elige uno en la lista sobre los mensajes, **+** empieza uno nuevo, el lápiz lo renombra (hasta entonces se llama como tu primer mensaje) y el cubo de basura lo elimina. Se guardan con el libro.

- **Rama** copia el chat tal cual en un chat nuevo; el botón de rama de un mensaje lo copia hasta ese mensaje incluido, para llevar la charla por otro camino desde ahí. La copia no tiene nada que ver con el original después.
- **Resumir el chat** primero pregunta qué debe conservar o destacar el resumen (opcional, por ejemplo "conserva el giro sobre el traidor y el final que rechazamos"; vacío: lo decide el modelo), luego pide al modelo un resumen del chat (lo que decidiste, lo que rechazaste, lo que sigue abierto, los nombres que quedaron fijados) y lo añade como mensaje. A partir de ahí el modelo recibe ese resumen y los mensajes posteriores, **no** los mensajes anteriores, que se quedan en pantalla, atenuados. Úsalo cuando el chat se haya alargado demasiado para el modelo, o para empezar desde un estado limpio sin perder las decisiones. Si resumes de nuevo más adelante, el nuevo resumen cubre al anterior. Crea una rama antes si quieres que el modelo reciba el chat completo.

![La pregunta sobre lo que debe conservar el resumen](../../images/plugin-arcoutline-wizard-summarize.png)

![El chat tras el resumen: los mensajes anteriores se quedan en pantalla, atenuados](../../images/plugin-arcoutline-wizard-summary.png)
<a id="generating-chapters"></a>

## Generación de capítulos

El botón de la varita mágica de un arco pide al modelo que divida ese arco en capítulos. El diálogo es como el de los arcos, con estas opciones a la izquierda:

| | |
|---|---|
| Lo que el modelo sabe de la historia | **Whole story so far**: los arcos hasta este, con todos sus capítulos. **Arc summaries only**: los mismos arcos sin capítulos. **Arc summaries and finished chapters**: los mismos arcos con solo los capítulos que marcaste como *Finished*. Los arcos posteriores a este nunca se envían. |
| Lo que quieres de los capítulos en general | Ritmo, tono, lo que debe ocurrir. Puede estar vacío. |
| Número de capítulos | Escríbelo con palabras («unos ocho») o elige un número exacto. Un número añade un área de texto por cada capítulo, donde puedes indicar lo que debe contener; todas son opcionales. Usar una de las dos cosas borra la otra. Con un número exacto, una respuesta con otro número de capítulos se rechaza. |
| Prompt | Las instrucciones para el modelo, con las variables `{{backgroundLore}}`, `{{outline}}`, `{{numberOfChapters}}`, `{{generalSpecification}}` y `{{chapterSpecification}}`. Empieza con el **Chapter generation prompt** de *Settings → Extension Settings → Arc Outline*. |

A la derecha ves el razonamiento, después el arco con los capítulos que ya tiene y los capítulos nuevos conforme el modelo los escribe. **Save as chapters** los añade al final del arco, cada uno con una descripción breve y los detalles en su nota (como los arcos). Cada capítulo nuevo conserva el razonamiento y la respuesta del modelo, y tiene su propio botón del robot.

![Generación de los capítulos de un arco](../../images/plugin-arcoutline-generate-chapters.png)

Un arco o capítulo escrito por el modelo conserva su **razonamiento** y la **respuesta** de la que procede. El botón del robot que aparece en él abre ambos en un pequeño cuadro emergente, sin estorbar.

![Lo que escribió el modelo para un arco](../../images/plugin-arcoutline-generated.png)

<a id="commands"></a>

## Comandos

El esquema y sus generadores también son [comandos](../commands/index.md), para la línea de comandos y para [agentes](../agents.md). Trabajan sobre el libro abierto (en un agente, sobre su copia de trabajo), y una pestaña Arcs abierta muestra el cambio al instante. Los arcos y los capítulos se indican por **número**, tal como los muestra el esquema: el primer arco es `1`, el primer capítulo de un arco es `1` (los números cambian cuando se añade, elimina o mueve algo antes de ellos). Un comando que cambia un arco o un capítulo solo toma los campos que le des; un texto vacío borra una descripción o una nota.

| Comando | Qué hace |
|---|---|
| `/get-arc-outline [arc [notes]]` | Los arcos con sus capítulos, estados y descripciones en JSON. Las notas privadas solo con `notes` `true`; `arc` devuelve solo ese arco. Solo lectura. |
| `/add-arc title [description [note [state [position]]]]` | Añade un arco, al final o en `position`. |
| `/update-arc arc [title [description [note [state]]]]` | Cambia un arco. `state` es `PLANNED`, `CURRENT` o `FINISHED` (terminar un arco termina sus capítulos). |
| `/move-arc arc position` | Mueve un arco al lugar `position` (1 es el primero). |
| `/delete-arc arc` | Elimina un arco con sus capítulos, tras una confirmación. |
| `/add-outline-chapter arc title [description [note [state [position]]]]` | Añade un capítulo a un arco. |
| `/update-outline-chapter arc chapter [title [description [note [state]]]]` | Cambia un capítulo. |
| `/move-outline-chapter arc chapter position [to_arc]` | Mueve un capítulo dentro de su arco, o al arco `to_arc`. |
| `/delete-outline-chapter arc chapter` | Elimina un capítulo, tras una confirmación. |
| `/generate-arc [specification [save]]` | Pide al modelo un arco nuevo, como hace *Generate an arc*. |
| `/generate-outline-chapters arc [specification [count [context [save]]]]` | Pide al modelo los capítulos de un arco, como hace la varita del arco. `count` es un número exacto de capítulos; `context` es `EVERYTHING` (el valor por defecto), `ARCS_ONLY` o `ARCS_AND_FINISHED`, como la opción *What the model knows of the story*. |
| `/import-arc-from-story [from [to [count [state [save [mapParts]]]]]]` | Pide al modelo que resuma los mensajes desde `from` hasta `to` de la rama activa (numerados entre los mensajes con texto, como en *Import from story*) como un arco con capítulos. `state` (por defecto `FINISHED`) es el estado del arco guardado y de sus capítulos. Con `mapParts` `true`, cada capítulo se vincula al mensaje en el que empieza (su *Linked part*, como hace la casilla *Map parts* del diálogo), y la respuesta incluye la parte de cada capítulo. |

Los tres comandos de generación usan los mismos prompts que los diálogos (tus *Extension Settings → Arc Outline*, o los valores por defecto), el modelo y el protocolo del libro (tu modelo por defecto si el libro no tiene ninguno) y ninguna herramienta. Quien llame a un comando y necesite nombres los obtiene con los comandos del [Generador de nombres](name-generator.md) o del [Proveedor de nombres](name-provider.md), y los escribe en la especificación. Como los diálogos, solo guardan cuando se les pide: con `save` `false` (el valor por defecto) devuelven el arco o los capítulos en JSON y no guardan nada; con `save` `true` los añaden al esquema (un arco al final, capítulos al final del arco) y la respuesta incluye sus números. `/import-arc-from-story` rechaza una historia demasiado larga para el modelo: elige menos mensajes. Los comandos de generación tardan un poco y se detienen cuando se detiene la ejecución.

Añadir y cambiar no es destructivo; solo los dos comandos `delete` lo son, y un agente puede configurarse para que te pregunte antes de usarlos.
