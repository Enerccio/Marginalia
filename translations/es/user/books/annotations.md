---
label: Anotaciones
order: 95
---

# Anotaciones

Una **anotación** es una nota para ti sobre un pasaje de una parte: *escena floja*, *la línea temporal es incorrecta*, *revisar*. Funciona como un comentario en un procesador de textos: el texto de la historia se mantiene limpio, el pasaje solo queda marcado y la nota vive junto a él. Las notas también pueden venir de un modelo o de una [extensión](../extensions/index.md) que revisa la historia.

Una anotación es **para el autor**. No se envía al modelo a menos que lo indiques para esa nota, ver [Dar una nota al modelo](#giving-a-note-to-the-model).

<a id="what-an-annotation-is"></a>

## Qué es una anotación

| | |
|---|---|
| **Pasaje** | Las palabras a las que se refiere la nota, o la **parte entera**. |
| **Nota** | Lo que quieres recordar. |
| **Tipo** | Una palabra opcional que eliges (*continuidad*, *estilo*, *ritmo*...). |
| **Origen** | Quién la escribió: tú, un modelo (*AI*), un *agent* o una *extension*. La marca en el texto tiene un color distinto para cada uno. |
| **Estado** | *Open*, *Resolved*, *Dismissed* o *Orphaned*. Solo las notas abiertas se marcan en el texto. |

Una parte puede tener cualquier número de anotaciones, también en pasajes que se solapan.

<a id="making-an-annotation"></a>

## Hacer una anotación

**En un pasaje.** Selecciona las palabras en el texto de la parte (pueden tener formato; la selección no tiene que ser exacta), luego haz **clic derecho** sobre ella (una pulsación larga en el móvil) y elige **Annotate selection**, o pulsa su [tecla](story-editor.md#keyboard-shortcuts) (`a`). La ventana muestra el pasaje; escribe la nota, un tipo opcional y si el modelo recibe la nota, y pulsa **Save**. El menú (☰) de la parte no tiene esa opción: al abrirlo se quita la selección. El menú del clic derecho tiene **Annotate selection**, [**Redraft selection**](story-editor.md#redrafting-only-a-passage) y **Copy**, y reemplaza el del navegador sobre el texto de una parte, por lo que también tiene **Copy**. Mientras la parte se está editando, se usa la selección del editor: antes se guarda la edición.

**En la parte entera.** Elige **Annotate part** en el menú de la parte.

**Con un modelo.** Elige **Annotate with AI**, ver [más abajo](#letting-a-model-annotate).

**Desde la línea de comandos** (y para extensiones): [`/create-annotation`](../commands/index.md#annotations).

![El menú del clic derecho sobre el texto de una parte](../../images/story-text-menu.png)

![La ventana de una nueva anotación](../../images/annotation-dialog.png)

<a id="reading-the-annotations"></a>

## Leer las anotaciones

Un pasaje con una nota abierta aparece subrayado con una **línea ondulada**, coloreada según quién escribió la nota (azul tú, naranja un modelo, verde un agente, rojo una extensión). Una nota dentro de otra va subrayada más abajo, para que se vean ambas.

![La nota de un pasaje subrayado, abierta en la marca](../../images/annotation-popover.png)

- **Haz clic en el texto subrayado** para ver las notas que cubren ese punto (todas, cuando los pasajes se solapan). Se abren junto al texto; en un móvil, toca el pasaje.
- Una **burbuja de diálogo con un número** en la cabecera de una parte cuenta las notas de la parte que necesitan revisión. Haz clic para verlas todas, también las que se refieren a la parte entera y las que perdieron su pasaje.
- En el **margen** (el menú de engranajes de la historia, **Annotations in the margin**; solo en escritorio, y se recuerda tu elección) las notas de cada parte aparecen en una columna a la izquierda de su texto, en el orden de sus pasajes, en lugar de en el cuadro emergente. La nota bajo el puntero y su pasaje subrayado se iluminan a la vez, y al hacer clic en un pasaje se muestra su nota. Son las mismas notas (resolver, editar...); las notas resueltas y descartadas no se muestran allí, pues tampoco están subrayadas.
- La pestaña **Annotations** lista las notas de todo el libro, agrupadas por parte en el orden de la historia, con un filtro (*Need a look*, *Closed*, *All*). **Show in story** (o el número de la parte) te lleva a la parte.

![Las notas en el margen de una parte](../../images/annotations-margin.png)

![La pestaña Annotations](../../images/annotations-tab.png)

Cada nota muestra quién la escribió, su tipo, su estado, el pasaje, la nota y estas acciones:

| | |
|---|---|
| ✓ **Resolve** | El problema está resuelto. La nota sale del texto, pero permanece en la lista. |
| ✗ **Dismiss** | La nota no merece la pena atenderla. Igual que lo anterior. |
| ↶ **Reopen** | Reabre una nota cerrada. |
| ✎ **Edit** | Cambia la nota y el tipo. |
| 🗑 **Delete** | Borra la nota; se puede recuperar desde la [papelera](../trash.md). |
| **Give to the model with the part** | Ver más abajo. |
| **Attach to selection** | Para una nota sin pasaje: selecciona texto en la parte y luego haz clic en ella. |

<a id="annotations-follow-the-text"></a>

## Las anotaciones siguen al texto

No tienes que preocuparte por dónde está una nota cuando editas. Marginalia mantiene el pasaje de cada nota con las palabras a las que se refería:

- el texto añadido o eliminado **antes** del pasaje lo desplaza;
- una edición **dentro** del pasaje lo hace más largo o más corto;
- las palabras que escribes **justo al principio o al final** del pasaje quedan fuera de él;
- corregir una errata en un pasaje de una sola palabra mantiene la nota en esa palabra.

Cuando el pasaje se **borra** o se reescribe de forma que no se puede encontrar, la nota pasa a *Orphaned* (huérfana). Se conserva: asígnala a otro pasaje (**Attach to selection**), descártala o bórrala. Si recuperas las palabras (deshacer), la nota vuelve a encontrar su pasaje.

Cuando una parte se **genera de nuevo**, el texto es nuevo, así que las notas vuelven a encontrar sus pasajes por las palabras que citaban; las que han perdido sus palabras quedan huérfanas. Cuando dos partes se [fusionan](story-editor.md#merging-a-part-into-the-previous-one), las notas de la posterior pasan a la parte fusionada. Una **rama** de una parte copia sus notas. Las notas forman parte de las [copias de seguridad](backups.md), de las copias de un libro y de las exportaciones de una copia de seguridad.
<a id="letting-a-model-annotate"></a>

## Dejar que un modelo anote

**Annotate with AI** en el menú de una parte pide a un modelo que la revise. Indicas qué debe buscar (*marcar errores de continuidad*,
*diálogo flojo*, *contradice el lore*; si lo dejas vacío, se usa lo que el libro, su carpeta o tus ajustes
[fijan una vez](prompts.md#annotate-prompt), y si no, la tarea del prompt), eliges el modelo y pulsas **Annotate**.

El modelo lee la parte igual que lo hace el [redraft](story-editor.md#redrafting-a-part): la parte, la historia anterior (la más antigua,
en forma de [resúmenes](summaries.md)), todo lo posterior que quepa, el lore y el estilo del libro, y las
notas que la parte ya tiene para no repetirlas. Nunca da posiciones: **cita** el pasaje, y Marginalia
localiza la cita en el texto. Una cita que no se encuentra no se pierde: la nota se propone como una nota sobre la parte entera,
marcada como *not found in the text*.

**No se añade nada al libro hasta que aceptas.** Las notas que propone el modelo aparecen en una lista con una casilla cada una (todas
marcadas); desmarca las que no quieras y pulsa **Add the selected annotations**.

En un ordenador, la ventana no bloquea la historia, y puede plegarse en la esquina de la página mientras el modelo trabaja; consulta
[Trabajar junto a la ventana](story-editor.md#working-beside-the-redraft-and-annotation-windows).

| Option | |
|---|---|
| **Model** | El modelo que revisa. Si está vacío, se usa el **Default model for annotating** de tus [ajustes](../account.md#general-settings), y si tampoco, el modelo del libro. Revisar es análisis, así que a menudo no es el modelo que escribe la prosa. |
| **Answer format** | *Automatic* (estructurado, cuando el modelo puede responder en JSON), *Structured (JSON)*, *Text blocks* (para modelos que no respetan un formato) o *Free critique, then extraction* (el modelo escribe una revisión libre y un segundo modelo, normalmente más barato, la convierte en notas: elígelo en **Model that extracts the notes**). |

![Las notas que propone un modelo para una parte](../../images/annotate-ai-proposals.png)

**Un rango de partes (lote).** **Annotate with AI** en el menú de una parte revisa solo esa parte. Para revisar varias, abre el
menú de ajustes (⚙) de la historia y elige **Annotate a range of parts with AI** (también en el teléfono). Es la misma ventana, con
**From part** y **To part** para elegir (ambos son obligatorios, **Annotate** espera a que estén; no importa cuál sea el anterior;
solo aparecen partes con texto). **Annotate** revisa después las partes del rango una tras otra, con las mismas instrucciones, el mismo formato
de respuesta y el mismo modelo, leyendo cada una con la historia que la rodea como si fuera una parte sola. Las propuestas aparecen bajo el número de
su parte, todas marcadas, y **Add the selected annotations** añade cada una a su propia parte. **Stop** detiene la parte en curso
y no empieza más; lo encontrado hasta entonces se queda en la lista. Una parte cuya respuesta no se puede leer o que no cabe en el contexto
del modelo se omite (la ventana lo indica) y la ejecución continúa; un fallo del modelo termina la ejecución.
Cada parte es una petición al modelo, así que un rango largo cuesta tantas peticiones (y tokens) como partes tenga.

![Anotar un rango de partes: las propuestas aparecen bajo el número de su parte](../../images/annotate-ai-batch.png)

**Datos de las extensiones.** Una [extensión](../extensions/index.md) que sabe más de la historia de lo que dice su texto puede dárselo al
revisor, que entonces marca como errores de continuidad los pasajes que lo contradicen. El [Knowledge Tracker](../extensions/knowledge-tracker.md#annotating-with-what-the-characters-know)
lo hace: indica al modelo lo que cada personaje seguido en la parte sabía al empezar esa parte, de modo que un personaje que usa un nombre que nunca
oyó queda anotado. Los datos están en el [prompt de anotación](prompts.md#annotate-prompt) integrado (un prompt propio tiene que usar
`{%{{{facts}}}%}`); sin esa extensión, el prompt queda como estaba.

Si una respuesta no se puede leer, Marginalia lo vuelve a pedir una vez; si sigue sin poder leerse, se muestra la respuesta del modelo para que
el trabajo no se pierda. El prompt es el [prompt de anotación](prompts.md#annotate-prompt).

<a id="annotating-every-new-part-automatically"></a>

### Anotar automáticamente cada parte nueva

El menú de ajustes (⚙) de la historia tiene **Automatic annotation** (también en el teléfono; una marca indica que está activado). Hace que el libro
revise cada parte justo después de generarla, y opcionalmente la reescriba. Los ajustes se guardan con el libro:

| Option | |
|---|---|
| **Model**, **Protocol** | El modelo que revisa. Si está vacío, se usa el modelo del libro (no el **Default model for annotating** de tus ajustes, con el que arranca la ventana que abres tú mismo). |
| **What to look for** | Las instrucciones, igual que en **Annotate with AI**. Si está vacío, se usa lo que hereda el libro (véase [Prompt de anotación](prompts.md#annotate-prompt)), y si tampoco, la tarea del prompt; si empiezas con el campo vacío, el campo muestra entonces el texto que se usó. |
| **Annotate every generated part automatically** | Lo activa. Después de generar una parte (una parte nueva, un swipe o una regeneración; no una que se detuvo o falló), el modelo la revisa **de inmediato**, sin ventana: el [panel de ejecuciones](story-editor.md#the-panel-of-the-runs) lo muestra como un paso de [lo que sigue a la parte](story-editor.md#what-follows-a-generated-part). |
| **Add the annotations without asking** | Cuando el modelo termina, se añaden las notas que encontró. Sin esta opción, la ventana de anotación se abre con las propuestas (en la página en la que generaste; desde **Review** en el panel, en cualquier página) y espera a que elijas, como cuando la inicias tú. |
| **Then redraft the part to address the annotations** | Solo con la opción anterior. Tras añadir las notas, el modelo [reescribe](story-editor.md#redrafting-a-part) la parte, con las notas abiertas de la parte y sin instrucciones propias. Escribe con el modelo del libro y no toca los resúmenes: la parte es nueva y ningún resumen la describe. Un modelo que no encontró nada no tiene nada que reescribir. |
| **Apply the redraft without asking** | Solo con la reescritura. Cuando el modelo termina, la reescritura sustituye el texto de la parte y las notas que trató se marcan como resueltas. Sin esta opción, o si cambiaste la parte mientras el modelo escribía, la ventana de reescritura se abre con el resultado y espera a que decidas. |

La ventana de reescritura que espera tiene dos pestañas: **Redrafted text** (el texto, su razonamiento y las diferencias con la parte) y **Original text with annotations** (la parte tal como estaba, con los pasajes de las notas abiertas marcados como en la historia, y las notas debajo; haz clic en una marca para ir a su nota, o pasa el cursor sobre cualquiera de las dos para que se iluminen ambas). Mira el original para ver qué se le pidió al modelo que tratara, y después cambia el texto o aplícalo.

![Los ajustes de la anotación automática](../../images/annotate-auto.png)

**Cerrar una ventana que espera, o Stop en el panel, termina lo que sigue a la parte.** No se añade ni se aplica nada más, y
los pasos posteriores (una traducción, el audio) no se ejecutan; el mensaje del panel los nombra, para que puedas hacerlos a mano. Lo que ya
se añadió o se aplicó se queda. Mientras la reescritura se ejecuta, no se puede generar una parte nueva (continuaría desde el texto antiguo); mientras se hacen las
notas, sí.

Cada parte generada cuesta al menos una petición más al modelo (dos con la reescritura). Si generas con un servidor local que solo guarda un prompt en su caché,
la revisión reemplaza la historia en ella y la siguiente generación tiene que volver a leer la historia: elige otro **Model** para la revisión, o
un segundo servidor.
<a id="watching-for-phrases"></a>

## Vigilar frases

Una **lista de frases vigiladas** señala las palabras que no quieres en la prosa: clichés (*un escalofrío le recorrió la espina dorsal*, *tapiz*), los tics de un modelo o un nombre que descartaste. Una parte se revisa con esa lista **sin usar un modelo**, y cada lugar donde aparece una frase se convierte en una anotación de ese pasaje (su origen es *Phrase watch list* y se nombra la entrada que coincidió). A partir de ahí es una nota como cualquier otra: deja que la [reescritura](#redrafting-to-address-the-notes) se ocupe de ella, o usa **Dismiss** para descartar una coincidencia que esté bien (en un diálogo, por ejemplo).

![La lista de frases vigiladas de un libro](../../images/phrase-watch-dialog.png)

Las frases no se prohíben en la salida del modelo. Una prohibición solo detendría una frase cuando ya ha empezado y empujaría al modelo al sinónimo más cercano; una nota sobre el texto terminado permite reescribir el pasaje completo.

**Dónde están las listas.** En los ajustes hay una pestaña **Phrase watch list** con tu lista, que usan todos los libros. Una [carpeta](folders.md#folder-settings) añade una lista para los libros que contiene y para las carpetas de dentro, y un libro tiene la suya, en **Phrase watch list** del menú de ajustes (⚙) de la historia. Las listas se suman. Una entrada con la misma frase que una heredada la reemplaza (para darle otra plantilla, por ejemplo), y una marcada **Off here** la quita para esa carpeta o libro. Las frases heredadas aparecen bajo las tuyas, cada una con **Switch off here**.

**Cómo se encuentra una frase.** Sin distinguir mayúsculas y solo como palabras completas (*tapiz* no se encuentra en *tapices*). Cualquier espacio o salto de línea coincide con cualquier otro, y las comillas y los apóstrofos rectos y curvos coinciden entre sí. Marca **Regular expression** para escribir una [expresión regular](../regular-expressions.md) en su lugar, también sin distinguir mayúsculas. Una expresión que no se puede usar se rechaza al guardar. Como máximo se crean 100 notas en una parte.

**Plantillas de anotación.** Lo que dice una nota es una **plantilla de anotación** de tus ajustes (en la misma pestaña): un nombre, la **Note**, donde `{%{{{match}}}%}` son las palabras encontradas y `{%{{{phrase}}}%}` la entrada de la lista; un **Type** (el color de la marca) y **Give to the model with the part** (véase [Dar una nota al modelo](#giving-a-note-to-the-model)). Cada entrada de una lista elige una; una entrada sin plantilla recibe una nota integrada (*Watched phrase: "…"*), sin tipo y no dada al modelo.

**Cuándo se revisan las partes.** En **Phrase watch list** del libro:

| Opción | |
|---|---|
| **Annotate the watched phrases in every generated part** | Después de generar una parte, se revisa justo después de la [anotación automática](#annotating-every-new-part-automatically) (un paso de [lo que sigue a la parte](story-editor.md#what-follows-a-generated-part), esté o no activa la anotación automática). Cuando el libro también reescribe automáticamente (**Add the annotations without asking** y **Then redraft the part…** de la anotación automática), la reescritura se ocupa también de estas notas. Una reescritura aplicada sin preguntar se vuelve a revisar, y si escribió de nuevo una frase, se reescribe otra vez, como máximo tres veces (las coincidencias de la última quedan como notas para ti). Las partes que escribe un [agente](../agents.md) también se revisan. |
| **Save and scan the story** | Guarda la lista y revisa ahora las partes de la rama activa, en un libro ya escrito: todas, o las que van de **From part** a **To part** (los números de las partes de la rama). El comando `/scan-phrases [from to]` hace lo mismo. |

Un lugar que ya tiene una nota de la lista para la misma frase no recibe una segunda, tampoco si la descartaste, de modo que la historia pueda revisarse de nuevo después de cambiar. Una nota resuelta no cuenta: si la frase sigue ahí (una reescritura la volvió a escribir), recibe una nota nueva. Las plantillas de anotación pertenecen a tus ajustes: un libro exportado con lo que hereda lleva consigo sus frases, y en otra instalación sus notas usan la integrada.

<a id="giving-a-note-to-the-model"></a>

## Dar una nota al modelo

Marca **Give to the model with the part** en una nota y se convierte también en una nota para el modelo: las notas de las partes que están en el prompt entran en la plantilla maestra como `{%{{{annotations}}}%}` (la plantilla integrada tiene un bloque *Author's notes on the story*; una plantilla propia debe usar la variable). Solo se dan las notas **abiertas** con esta marca, y solo de las partes que caben en el prompt. Úsala para lo que el modelo debe tener presente al continuar (*este personaje esconde la carta*), no para observaciones de edición.

<a id="redrafting-to-address-the-notes"></a>

## Reescribir para atender las notas

Cuando una parte tiene notas abiertas, la [reescritura](story-editor.md#redrafting-a-part) tiene una casilla **Deal with the open annotations of the part**. Marcada, el modelo recibe las notas (el pasaje y la nota) junto con tus instrucciones y reescribe la parte para atenderlas. Al aplicar la reescritura con **Apply**, esas notas se marcan como resueltas. En la [reescritura de una selección](story-editor.md#redrafting-only-a-passage) solo se dan las notas cuyo pasaje se solapa con la selección.

<a id="commands"></a>

## Comandos

[`/list-annotations`](../commands/index.md#annotations), `/get-annotation`, `/create-annotation`, `/update-annotation`, `/resolve-annotation`, `/reattach-annotation` y `/delete-annotation` trabajan con las notas desde la línea de comandos. Las extensiones pueden usar los mismos comandos. [`/generate-annotation <part>`](../commands/index.md#annotations) es **Annotate with AI** como comando: el modelo revisa la parte con el modelo del libro (un agente usa su propio modelo cuando *Write the book with the model of the agent* está activo; `profile` elige otro perfil de modelo del agente), con las instrucciones, el formato de respuesta y el prompt del libro. Cada nota que encuentra se añade de inmediato, ya que no hay nadie que las elija. Es una sola petición al modelo, así que se ejecuta por separado y consume tokens.

<a id="annotations-and-agents"></a>

## Anotaciones y agentes

Un [agente](../agents.md) lee y escribe anotaciones con los mismos comandos: puede anotar un libro entero (*continuidad*, *estilo*...), resolver las notas que atendió, editarlas o borrarlas. Trabaja sobre una copia del libro, así que nada llega a tus notas hasta que [revisas el resultado](../agents.md#reviewing-the-result):

- Una nota que creó el agente aparece como una línea **Annotation - Added**. Una que resolvió, descartó o reescribió aparece como **Changed** (con la nota, el tipo, el estado y *give to the model* como campos, cada uno con su casilla **Take this change**). Una que borró aparece como **Deleted**. Las notas de una parte que el agente borró van con la parte y no tienen líneas.
- La línea indica en qué parte está la nota (`3: ` y el comienzo de la nota). **Show in the copy** abre la parte en la copia del agente. Una nota sobre una parte que escribió el agente necesita esa parte: marcar la nota marca también la parte.
- **Only annotations** sobre la lista marca solo las notas. El pasaje de una nota tomada es el mismo cuando el texto de la parte es igual. Cuando tomas el texto nuevo de la parte del agente, o si tu texto difiere, la nota vuelve a buscar sus palabras en el texto de tu libro y, si no las encuentra, pasa a ser una nota sobre la parte entera.
- Una nota que escribiste mientras el agente estaba en pausa entra en su copia cuando se reanuda (y una nota que resolviste queda resuelta allí), así que la conoce. Una nota que tú y el agente cambiasteis a la vez es un conflicto como cualquier otro.
