---
label: Anotaciones
order: 95
---

# Anotaciones

Una **anotación** es una nota para ti sobre un pasaje de una parte: *escena floja*, *la línea temporal está mal*, *revisar*. Funciona como un
comentario en un procesador de textos: el texto de la historia queda limpio, el pasaje solo se marca y la nota vive junto
a él. Las notas también pueden venir de un modelo o de una [extensión](../extensions/index.md) que revisa la historia.

Una anotación es **para el autor**. No se envía al modelo a menos que lo indiques para esa nota; consulta
[Dar una nota al modelo](#giving-a-note-to-the-model).

<a id="what-an-annotation-is"></a>

## Qué es una anotación

| | |
|---|---|
| **Pasaje** | Las palabras a las que se refiere la nota, o la **parte completa**. |
| **Nota** | Lo que quieres recordar. |
| **Tipo** | Una palabra opcional que eliges (*continuidad*, *estilo*, *ritmo*...). |
| **Origen** | Quién la escribió: tú, un modelo (*IA*), un *agente* o una *extensión*. La marca en el texto tiene un color distinto para cada uno. |
| **Estado** | *Abierta*, *Resuelta*, *Descartada* o *Huérfana*. Solo las notas abiertas se marcan en el texto. |

Una parte puede tener cualquier número de anotaciones, también sobre pasajes que se solapan.

<a id="making-an-annotation"></a>

## Crear una anotación

**Sobre un pasaje.** Selecciona las palabras en el texto de la parte (pueden tener formato; la selección no tiene que ser
exacta), luego haz **clic derecho** sobre ella (una pulsación prolongada en un teléfono) y elige **Anotar la selección**, o pulsa su [tecla](story-editor.md#keyboard-shortcuts) (`a`). La ventana muestra el pasaje; escribe la nota, un
tipo opcional y si el modelo recibe la nota, y pulsa **Guardar**. El menú (☰) de la parte no tiene esa opción: al abrirlo se pierde la selección. El menú del clic derecho tiene **Anotar la selección**, [**Reescribir la selección**](story-editor.md#redrafting-only-a-passage) y **Copiar**, y reemplaza
el del navegador sobre el texto de una parte, por eso también tiene **Copiar**. Mientras la parte se está editando, se usa la selección en el
editor: antes se guarda la edición.

**Sobre la parte completa.** Elige **Anotar la parte** en el menú de la parte.

**Con un modelo.** Elige **Anotar con IA**, consulta [más abajo](#letting-a-model-annotate).

**Desde la línea de comandos** (y para extensiones): [`/create-annotation`](../commands/index.md#annotations).

![El menú del clic derecho sobre el texto de una parte](../../images/story-text-menu.png)

![La ventana de una nueva anotación](../../images/annotation-dialog.png)

<a id="reading-the-annotations"></a>

## Leer las anotaciones

Un pasaje con una nota abierta se subraya con una **línea ondulada**, coloreada según quién escribió la nota (azul tú, naranja un
modelo, verde un agente, rojo una extensión). Una nota dentro de otra se subraya más abajo, para que se vean ambas.

![La nota de un pasaje subrayado, abierta en la marca](../../images/annotation-popover.png)

- **Haz clic en el texto subrayado** para ver las notas que cubren ese punto (todas, si los pasajes se solapan). Se abren
  junto al texto; en un teléfono, toca el pasaje.
- Un **globo de diálogo con un número** en la cabecera de una parte cuenta las notas de la parte que requieren atención. Haz clic en él para
  verlas todas, también las que se refieren a la parte completa y las que han perdido su pasaje.
- En el **margen** (el menú de engranaje de la historia, **Anotaciones en el margen**; solo en escritorio, y se recuerda para ti) las notas de cada parte se muestran en una columna
  a la izquierda de su texto, en el orden de sus pasajes, en lugar de en el globo emergente. La nota bajo el puntero y su pasaje subrayado se iluminan juntos, y al hacer clic en un
  pasaje, su nota se muestra en pantalla. Son las mismas notas (resolver, editar...); las notas resueltas y descartadas no se muestran allí, pues tampoco están subrayadas.
- La pestaña **Anotaciones** enumera las notas de todo el libro, agrupadas por parte en el orden de la historia, con un filtro
  (*Requieren atención*, *Cerradas*, *Todo*). **Mostrar en la historia** (o el número de la parte) te lleva a la parte.

![Las notas en el margen de una parte](../../images/annotations-margin.png)

![La pestaña Anotaciones](../../images/annotations-tab.png)

Cada nota muestra quién la escribió, su tipo, su estado, el pasaje, la nota y estas acciones:

| | |
|---|---|
| ✓ **Resolver** | El problema está resuelto. La nota sale del texto y se queda en la lista. |
| ✗ **Descartar** | La nota no merece la pena. Igual que lo anterior. |
| ↶ **Reabrir** | Devuelve una nota cerrada. |
| ✎ **Editar** | Cambia la nota y el tipo. |
| 🗑 **Eliminar** | Elimina la nota; puede recuperarse desde la [papelera](../trash.md). |
| **Dar al modelo junto con la parte** | Consulta más abajo. |
| **Asociar a la selección** | Para una nota sin pasaje: selecciona texto en la parte y luego haz clic en ella. |

<a id="annotations-follow-the-text"></a>

## Las anotaciones siguen al texto

No tienes que preocuparte por dónde está una nota cuando editas. Marginalia conserva el pasaje de cada nota junto con las palabras a las que
se refería:

- el texto añadido o eliminado **antes** del pasaje lo mueve;
- una edición **dentro** del pasaje lo hace más largo o más corto;
- las palabras que escribes **justo al principio o al final** del pasaje quedan fuera de él;
- corregir una errata en un pasaje de una sola palabra mantiene la nota en esa palabra.

Cuando el pasaje se **elimina** o se reescribe de forma que no se puede encontrar, la nota pasa a **Huérfana**. Se conserva: asóciala
a otro pasaje (**Asociar a la selección**), descártala o elimínala. Si recuperas las palabras (deshacer), la nota vuelve a encontrar
su pasaje.

Cuando una parte se **genera de nuevo**, el texto es nuevo, así que las notas vuelven a encontrar sus pasajes por las palabras que citaban;
las que han perdido sus palabras quedan huérfanas. Cuando dos partes se [fusionan](story-editor.md#merging-a-part-into-the-previous-one), las notas de la posterior pasan a
la parte fusionada. Una **rama** de una parte copia sus notas. Las notas forman parte de las [copias de seguridad](backups.md), de las copias de un libro y
de las exportaciones de una copia de seguridad.
<a id="letting-a-model-annotate"></a>

## Dejar que un modelo anote

**Anotar con IA** en el menú de una parte pide a un modelo que la revise. Indicas qué buscar (*marcar errores de continuidad*,
*diálogo flojo*, *contradice el lore*; si lo dejas vacío se usa lo que el libro, su carpeta o tus ajustes
[tengan definido](prompts.md#annotate-prompt), o si no, la tarea de la plantilla), eliges el modelo y pulsas **Anotar**.

El modelo lee la parte igual que lo hace la [reescritura](story-editor.md#redrafting-a-part): la parte, la historia anterior (la más antigua
en forma de [resúmenes](summaries.md)), todo lo posterior que quepa, el lore y el estilo del libro, y las
notas que ya tiene la parte para no repetirlas. Nunca da posiciones: **cita** el pasaje, y Marginalia
localiza la cita en el texto. Una cita que no se encuentra no se pierde: la nota se propone como una nota sobre la parte entera,
marcada como *no encontrada en el texto*.

**No se añade nada al libro hasta que lo aceptes.** Las notas que propone el modelo aparecen en una lista con una casilla cada una (todas
marcadas); desmarca las que no quieras y elige **Añadir las anotaciones seleccionadas**.

La ventana no bloquea la historia en un ordenador, y puede plegarse en la esquina de la página mientras el modelo trabaja; consulta
[Trabajar junto a las ventanas](story-editor.md#working-beside-the-redraft-and-annotation-windows).

| Opción | |
|---|---|
| **Modelo** | El modelo que revisa. Si está vacío, se usa el **Modelo predeterminado para anotar** de tus [ajustes](../account.md#general-settings), o si no, el modelo del libro. Revisar es análisis, así que a menudo no es el mismo modelo que escribe la prosa. |
| **Formato de la respuesta** | *Automático* (estructurado, cuando el modelo puede responder en JSON), *Estructurado (JSON)*, *Bloques de texto* (para modelos que no respetan un formato) o *Crítica libre y después extracción* (el modelo escribe una revisión libre y un segundo modelo, normalmente más barato, la convierte en notas: elígelo en **Modelo que extrae las notas**). |

![Las notas que propone un modelo para una parte](../../images/annotate-ai-proposals.png)

**Un intervalo de partes (lote).** **Anotar con IA** en el menú de una parte revisa solo esa parte. Para revisar varias, abre el
menú de ajustes (⚙) de la historia y elige **Anotar un intervalo de partes con IA** (también en el teléfono). Es la misma ventana con
**Desde la parte** y **Hasta la parte** para elegir (ambas son obligatorias, **Anotar** espera a que las tengas; no importa cuál
sea la anterior; solo aparecen las partes con texto). **Anotar** revisa entonces las partes del intervalo una tras otra, con las mismas instrucciones, el mismo
formato de respuesta y el mismo modelo, cada una leída con la historia que la rodea como si fuera una sola parte. Las propuestas aparecen bajo el número de
su parte, todas marcadas, y **Añadir las anotaciones seleccionadas** añade cada una a su propia parte. **Detener** para la parte en curso
y no inicia más; lo que se haya encontrado hasta ese momento permanece en la lista. Una parte cuya respuesta no se puede leer o que no cabe
en el contexto del modelo se omite (la ventana lo indica) y la ejecución continúa; un fallo del modelo termina la ejecución.
Cada parte es una petición al modelo, así que un intervalo largo cuesta tantas peticiones (y tokens) como partes.

![Anotar un intervalo de partes: las propuestas aparecen bajo el número de su parte](../../images/annotate-ai-batch.png)

**Datos de las extensiones.** Una [extensión](../extensions/index.md) que sabe más de la historia de lo que dice su texto puede darle esa información al
revisor, que entonces marca los pasajes que la contradicen como errores de continuidad. Lo hace el [Knowledge Tracker](../extensions/knowledge-tracker.md#annotating-with-what-the-characters-know):
le dice al modelo qué sabía cada personaje seguido de la parte cuando la parte empieza, de modo que un personaje que usa un nombre que nunca
oyó queda anotado. Los datos están en la [plantilla de anotación](prompts.md#annotate-prompt) integrada (una plantilla propia debe usar
`{%{{{facts}}}%}`); sin esa extensión, la plantilla queda como estaba.

Si una respuesta no se puede leer, Marginalia lo vuelve a pedir una vez; si sigue sin poder leerse, se muestra la respuesta del modelo para que
el trabajo no se pierda. La plantilla es la de [anotación](prompts.md#annotate-prompt).

<a id="annotating-every-new-part-automatically"></a>

### Anotar automáticamente cada parte nueva

El menú de ajustes (⚙) de la historia tiene **Anotación automática** (también en el teléfono; una marca indica que está activada). Hace que el libro
revise cada parte justo después de generarla, y opcionalmente la reescriba. Los ajustes se guardan con el libro:

| Opción | |
|---|---|
| **Modelo**, **Protocolo** | El modelo que revisa. Si está vacío, se usa el modelo del libro (no el **Modelo predeterminado para anotar** de tus ajustes, con el que empieza la ventana que abres tú mismo). |
| **Qué buscar** | Las instrucciones, como en **Anotar con IA**. Si está vacío se usa lo que el libro hereda (consulta [Plantilla de anotación](prompts.md#annotate-prompt)), o si no, la tarea de la plantilla; cuando lo activas con el campo vacío, el campo muestra entonces el texto que se usó. |
| **Anotar automáticamente cada parte generada** | Lo activa. Tras generar una parte (una parte nueva, un swipe o una regeneración; no una que se detuvo o falló) el modelo la revisa **de inmediato**, sin ventana, y el [panel de ejecuciones](story-editor.md#the-panel-of-the-runs) lo muestra como un paso de [lo que sigue a la parte](story-editor.md#what-follows-a-generated-part). |
| **Añadir las anotaciones sin preguntar** | Cuando el modelo termina, se añaden las notas que encontró. Sin esta opción, la ventana de anotación se abre con las propuestas (en la página en la que generaste; desde **Revisar** en el panel, en cualquier página) y espera a que las elijas, como cuando la inicias tú. |
| **Después reescribir la parte según las anotaciones** | Solo con la opción anterior. Tras añadir las notas, el modelo [reescribe](story-editor.md#redrafting-a-part) la parte con las anotaciones abiertas de la parte y sin instrucciones propias. Escribe con el modelo del libro y no toca los resúmenes: la parte es nueva, así que ningún resumen la describe. Un modelo que no encontró nada no tiene nada que reescribir. |
| **Aplicar la reescritura sin preguntar** | Solo con la reescritura. Cuando el modelo termina, la reescritura reemplaza el texto de la parte y las notas que trató se marcan como resueltas. Sin esta opción, o cuando cambiaste la parte mientras el modelo escribía, la ventana de reescritura se abre con el resultado y espera a que decidas. |

La ventana de reescritura que espera tiene dos pestañas: **Texto reescrito** (el texto, su razonamiento y las diferencias respecto a la parte) y **Texto original con anotaciones** (la parte tal como estaba, con los pasajes de las notas abiertas marcados como en la historia, y las notas debajo; haz clic en una marca para ir a su nota, o pasa el cursor sobre cualquiera de las dos para iluminar ambas). Mira el original para ver qué se le pidió al modelo que tratara, cambia el texto o aplícalo.

![Los ajustes de la anotación automática](../../images/annotate-auto.png)

**Cerrar una ventana que espera, o Detener en el panel, termina lo que sigue a la parte.** No se añade ni aplica nada más, y
los pasos posteriores (una traducción, el audio) no se ejecutan; el mensaje del panel los nombra para que puedas hacerlos a mano. Lo que ya
se añadió o aplicó se mantiene. Mientras la reescritura se ejecuta, no se puede generar una parte nueva (continuaría desde el texto antiguo); mientras se hacen las
notas, sí.

Cada parte generada cuesta al menos una petición más al modelo (dos con la reescritura). Si generas con un servidor local que solo guarda un prompt en su caché,
la revisión reemplaza la historia en ella y la siguiente generación tiene que volver a leer la historia: elige otro **Modelo** para la revisión, o
un segundo servidor.

<a id="giving-a-note-to-the-model"></a>

## Dar una nota al modelo

Marca **Dar al modelo junto con la parte** en una nota y se convierte también en una nota para el modelo: las notas de las partes que
están en el prompt se incluyen en la plantilla maestra como `{%{{{annotations}}}%}` (la plantilla integrada tiene un bloque *Notas del autor sobre
la historia*; una plantilla propia debe usar la variable). Solo se dan las notas **abiertas** con esta marca, y solo de las partes que caben
en el prompt. Úsalo para lo que el modelo debe tener presente al continuar (*este personaje oculta la carta*), no para observaciones de edición.

<a id="redrafting-to-address-the-notes"></a>

## Reescribir para atender las notas

Cuando una parte tiene notas abiertas, la ventana de [reescritura](story-editor.md#redrafting-a-part) tiene una casilla **Tratar las anotaciones abiertas de la parte**. Marcada,
el modelo recibe las notas (el pasaje y la nota) junto con tus instrucciones y reescribe la parte para tratarlas; al **Aplicar** la reescritura, esas notas se marcan
como resueltas. En la [reescritura de una selección](story-editor.md#redrafting-only-a-passage) solo se dan las notas cuyo pasaje se solapa con la selección.
<a id="commands"></a>

## Comandos

[`/list-annotations`](../commands/index.md#annotations), `/get-annotation`, `/create-annotation`, `/update-annotation`,
`/resolve-annotation`, `/reattach-annotation` y `/delete-annotation` trabajan con las notas desde la línea de comandos.
Las extensiones pueden usar los mismos comandos. [`/generate-annotation <part>`](../commands/index.md#annotations) es **Anotar con IA** como comando: el modelo revisa la parte con el modelo del libro
(un agente usa su propio modelo cuando *Escribir el libro con el modelo del agente* está activado; `profile` elige otro perfil de modelo del agente), con las instrucciones, el formato de respuesta y el prompt del libro, y cada nota que encuentra se añade de una vez,
ya que no hay nadie que las elija. Es una sola petición al modelo, así que se ejecuta sola y consume tokens.

<a id="annotations-and-agents"></a>

## Anotaciones y agentes

Un [agente](../agents.md) lee y escribe anotaciones con los mismos comandos: puede anotar un libro entero (*continuity*, *style*...), resolver las notas que ya atendió, editarlas
o eliminarlas. Trabaja sobre una copia del libro, así que nada llega a tus notas hasta que [revisas el resultado](../agents.md#reviewing-the-result):

- Una nota que creó el agente aparece como una línea **Annotation - Added**; una que resolvió, descartó o reescribió es **Modificado** (con la nota, el tipo, el estado y *give to the model* como
  campos, cada uno con su propia casilla **Tomar este cambio**); una que eliminó es **Eliminados**. Las notas de una parte que el agente eliminó van con la parte y no tienen líneas.
- La línea indica en qué parte está la nota (`3: ` y el inicio de la nota); **Mostrar en la copia** abre la parte en la copia del agente. Una nota sobre una parte que el agente escribió necesita
  esa parte: marcar la nota también marca la parte.
- **Solo anotaciones** encima de la lista marca solo las notas. El pasaje de una nota tomada es el mismo cuando el texto de la parte es el mismo; cuando tomas el
  nuevo texto de la parte del agente, o tu propio texto difiere, la nota vuelve a encontrar sus palabras en el texto de tu libro y, si no puede, pasa a ser una nota sobre la parte completa.
- Una nota que escribiste mientras el agente estaba en pausa entra en su copia cuando se reanuda (y una nota que resolviste queda resuelta allí), para que la conozca; una nota que tú y el agente
  modificaron a la vez es un conflicto como cualquier otro.
