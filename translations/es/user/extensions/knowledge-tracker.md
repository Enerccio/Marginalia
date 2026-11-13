---
label: Registro de conocimiento
order: 11
---

# Registro de conocimiento

Lleva la cuenta de lo que saben los personajes de tu historia y de lo que **no** saben, y lo incorpora al prompt. Un modelo
tiende a dejar que un personaje sepa cosas sobre personas que nunca ha conocido (su nombre, su oficio, su cara) a menos que
las instrucciones lo impidan; con el Registro de conocimiento, el prompt indica, para cada personaje rastreado en la escena,
qué sabe ese personaje y que no sabe nada más.

!!!
Tú eliges quién se rastrea. Solo se describe al modelo a los personajes que añadas a la pestaña, y únicamente mientras estén
entre los *personajes presentes* de la parte que se está escribiendo. Nada se deduce del texto de la historia.
!!!

<a id="the-knowledge-tab"></a>

## La pestaña Conocimiento

La barra lateral izquierda del [editor de historias](../books/story-editor.md) incluye una pestaña **Conocimiento** junto a *Esquema*. Todo se guarda en el libro a medida que lo cambias.

![La pestaña Conocimiento](../../images/plugin-knowledgetracker.png)

**Seguir a un personaje** pide un **nombre** y, opcionalmente, **alias**: los otros nombres con los que el personaje aparece escrito en el
campo *Personajes presentes* de una parte, separados por comas. Un nombre o un alias solo puede pertenecer a un único personaje rastreado.

Abre un personaje con la flecha de su tarjeta para ver lo que sabe:

| | |
|---|---|
| **Sabe** (verde) / **No sabe** (rojo) | Una frase corta sobre cualquier cosa: una persona, un lugar, un secreto, un suceso (*Peter sabe del complot contra el duque*, *Fiora no sabe que Thomas es su padre*). Escribe explícitamente lo que un personaje **no** sabe, porque eso es lo que el modelo respeta. |
| El orden y **#12** | El conocimiento más reciente va arriba (el antiguo también). Un dato que aprendió el modelo termina con el pequeño número de la parte en la que se aprendió (**#12**): al hacer clic, la historia se desplaza a esa parte y la resalta (en un teléfono, primero se cierra el cajón). Los datos que escribiste no tienen número, y tampoco los que pertenecen a una parte que no está en la rama activa (otra rama, o la parte fue eliminada). |
| **Añadir conocimiento** | Añade un dato, activo de inmediato. Mantenlo en **un solo pensamiento**: un dato que dice dos cosas (*no sabe quién es Thomas, nunca lo ha conocido*) se retira en cuanto una de ellas deja de ser cierta. El botón **editar** cambia el texto y el tipo. |
| **Retirar** (el círculo con una barra) | El dato ya no es cierto (*no ha conocido a Thomas* después de que se conozcan). No se elimina: pasa a ser *conocimiento antiguo* y ya no se envía al modelo. |
| **Bloquear** (el candado, en los datos que aprendió el modelo) | La [revisión de limpieza](#tidying-the-knowledge) nunca toca un dato bloqueado: úsalo para lo que debe permanecer, una traición, un secreto. La historia aún puede contradecir un dato bloqueado (un personaje que pierde la memoria): la lectura de la historia lo retira como de costumbre. |
| **Eliminar** | Borra un dato para siempre, previa confirmación. Retirar lo conserva, así que úsalo si tienes dudas. |
| **Seleccionar datos** (junto a **Añadir conocimiento**) | Elimina muchos datos rápidamente: cada dato del personaje, incluidos los antiguos, recibe una casilla. **Seleccionar todo** / **Deseleccionar todo** marca o desmarca todas, **Eliminar seleccionados (n)** borra para siempre las marcadas tras una confirmación, **Cancelar** sale de la selección. |
| **Ha conocido a** | Las personas que el personaje ha conocido, anotadas por el [procesamiento](#processing). El procesamiento puede anotar un nombre que no es una persona (una nota en el campo de personajes presentes): abre **Editar** y cambia la lista de **Ha conocido a**, un nombre tras otro separados por comas. Los nombres que escribes se mantienen cuando la historia se vuelve a leer; uno que eliminas solo vuelve si la parte en la que se vio se vuelve a leer. |
| **Conocimiento antiguo (n)** | Plegado por defecto. Cada dato antiguo puede **copiarse**, **editarse**, **restaurarse** (vuelve a estar activo, a partir de ahora) o eliminarse. |

<a id="mass-changes"></a>

### Cambios masivos

El botón **Cambios masivos** (el icono de lista junto a **Seguir a un personaje**) abre una ventana con cada personaje rastreado y
todos sus datos, también los antiguos, en una cuadrícula con casillas: el tipo, el texto, el estado (*Activa*, o antiguo: *retirado*,
*rechazado en una revisión*, *ordenado*), de dónde viene (*Estado inicial*, *Escrito a mano*, *Aprendido de la historia*) y la parte en la que se
aprendió. Marca datos en tantas cuadrículas como quieras (la casilla del encabezado de una cuadrícula marca todos sus datos, y
**Seleccionar todo** / **Deseleccionar todo** en la barra inferior marca todos los datos de todos los personajes), y después:

| | |
|---|---|
| **Retirar seleccionados (n)** | Los datos activos marcados pasan a ser conocimiento antiguo, como lo hace **Retirar** con uno solo. |
| **Restaurar seleccionados (n)** | Los datos antiguos marcados vuelven a estar activos, a partir de ahora. |
| **Eliminar seleccionados (n)** | Borra para siempre los datos marcados, previa confirmación. |

Los contadores de los botones indican cuántos de los datos marcados cambia cada uno. Las marcas se mantienen tras un cambio, de modo que los datos
retirados pueden eliminarse a continuación.

**Exportar a JSON** (en la parte superior de la ventana) descarga como archivo los personajes rastreados del libro (nombres, alias, datos con su
tipo, estado, origen y bloqueo, y las personas que han conocido); no incluye nada del procesamiento, así que sirve para cualquier libro.
**Importar desde JSON** lee un archivo de ese tipo y lo incorpora al libro, solo añadiendo: un personaje cuyo nombre el libro ya rastrea (como nombre o alias)
recibe los alias, datos y personas conocidas que aún no tiene (un dato con el mismo tipo y texto se omite), y los demás se convierten en nuevos personajes rastreados; un alias que ya tiene otro personaje se deja fuera.
Un dato importado no tiene detrás ninguna parte de este libro, así que un nuevo procesamiento nunca lo elimina: el que aprendió el modelo cuenta como escrito a mano, y el antiguo sigue siendo antiguo. El mensaje tras la importación indica cuántos personajes se añadieron o actualizaron y cuántos datos se añadieron o ya existían.

<a id="what-goes-into-the-prompt"></a>

## Qué se incluye en el prompt

Para cada personaje rastreado que esté entre los personajes presentes, el prompt recibe un bloque como este:

```
Lo que saben los personajes de esta escena. Un personaje actúa solo según el conocimiento que se le atribuye:

Fiora sabe: Mara, una mensajera del Gremio de las Lámparas y la hermana de su amiga Nell; le debe cuarenta coronas al Gremio de las Lámparas.
Fiora no sabe: que su padre está vivo, ni que Thomas es su padre.
Fiora ha conocido a: Mara.
Fiora está actualmente con: Mara, Thomas.
Fiora nunca ha conocido ni oído hablar de nadie más en esta historia y no sabe nada más de sus secretos, planes o sucesos.
```

Solo se envían los datos activos. La última línea (la línea de **mundo cerrado**) es la que impide que el modelo dé al personaje un conocimiento que no puede tener; cubre lo que cuenta la historia (personas, lugares, secretos, planes, sucesos), no lo que una persona de ese mundo sabe de todos modos, así que un personaje sigue conociendo su propio pueblo. Puedes desactivar esa línea en los ajustes. Un personaje rastreado que no está presente no recibe nada, ni siquiera una línea que diga que no lo sabe.

<a id="who-is-present"></a>

### Quién está presente

El bloque se construye a partir del campo **Personajes presentes** de la parte. El campo es texto libre, así que se lee con flexibilidad:

- el texto entre corchetes se ignora: `Tomáš (cansado), Mizuki` son Tomáš y Mizuki;
- los nombres se separan por los **separadores** (por defecto: `,` `;` un salto de línea, `/`, `|`, `、` `，` `；`);
- lo que va después de ` - ` o `:` es una nota y se descarta (`Mara - herida`);
- las mayúsculas, minúsculas, los acentos y un *the* inicial no importan;
- un nombre tiene que coincidir **completo**: *el hermano de Mara* no es *Mara*. Usa los alias cuando un personaje aparezca escrito de más de una forma. La idea es que una coincidencia perdida cuesta un bloque, mientras que una coincidencia falsa describiría a un personaje que no está en la escena.

El campo *Punto de vista* no se lee por sí solo, así que escribe también al personaje del punto de vista entre los personajes presentes.
<a id="processing"></a>

## Procesamiento

El plugin sigue la historia. **Processing** lee las partes de la rama activa en orden, a partir del **punto de inicio**, y mantiene un **cursor** (la última parte leída). Sin el modelo, anota quién se ha encontrado con quién: un personaje rastreado se ha encontrado con todos los nombres que aparecen en el campo de personajes presentes junto a él, y el bloque los enumera. Con el modelo, además encuentra lo que los personajes **aprendieron** (véase [Lectura de lo que aprenden los personajes](#reading-what-the-characters-learn)).

![El panel de procesamiento](../../images/plugin-knowledgetracker-processing.png)

El panel de la parte superior de la pestaña muestra en qué punto está. Se recarga solo cuando una parte nueva queda definitiva (después de la anotación automática y la reescritura, si el libro las tiene). Si añades, editas o eliminas partes de otra forma, pulsa el botón **Actualizar** (las flechas junto al engranaje) para volver a leer la historia. Contiene:

- **Process now**: lee las partes que todavía no están procesadas.
- **Process next part**: lee solo la primera parte que no está procesada, paso a paso (con el modelo: una petición para esa parte, sin importar lo que indique *Parts per run*). Úsalo para probar la configuración o para ver qué encuentra el modelo en una sola parte. El comando `knowledge-process` con `parts` en 1 hace lo mismo.
- **Start point**: la parte desde la que empieza el procesamiento (*Start of the book* por defecto). Las partes anteriores nunca se leen. Si retrasas el punto de inicio, las partes desde el nuevo inicio se vuelven a leer; si lo adelantas, se conserva lo que ya se había leído.
- **Processed parts**: las últimas partes que se leyeron. Una parte cuyo texto **cambió desde que se procesó** (la editaste o la reescribiste) aparece marcada; nada se vuelve a procesar por sí solo por ese motivo. El botón de cada fila, **Reprocess from here**, deshace lo que provino de esa parte y de las posteriores (los elementos y retractaciones que escribiste a mano se conservan) y vuelve a leerlas.

Lo que hace el plugin por su cuenta, cuando **Process new parts automatically** está activado (desactivado por defecto):

- Antes de que empiece una generación, se procesan las partes escritas desde la última vez, para que el prompt sepa quién se ha encontrado con quién. Una parte que se está reemplazando mediante *Regenerar* o un swipe se deja en paz. Con el modelo, esto lee con el modelo solo cuando **Ask before applying** está desactivado (véase abajo). En caso contrario, solo anota quién se ha encontrado con quién, y lo que encuentre el modelo se inicia desde la pestaña, porque nunca se impone una revisión en medio de una generación.
- Cuando una parte se **regenera** y llega el texto nuevo, se deshace lo que se aprendió del texto anterior.
- Cuando haces **swipe** o cambias a otra rama, el cursor vuelve a la última parte que comparten ambas ramas.
<a id="reading-what-the-characters-learn"></a>

## Leer lo que aprenden los personajes

Marca **Read what the characters learn with the model** en la configuración. Después, **Process now** pide al modelo que indique, para las siguientes partes de la historia, qué **aprendió** cada personaje seguido que aparece en ellas: lo que vio, oyó, leyó o le contaron. También indica qué elementos contradice la historia (*nunca ha conocido a Thomas* después de que se encuentren). La solicitud incluye las partes (con su punto de vista y los personajes presentes), lo que cada personaje seguido sabe en ese momento y, si lo eliges, el libro de lore. El modelo recibe la indicación de omitir lo que cualquier persona de ese mundo ya sabe (quién es el rey) y lo que se deduce del propio personaje (una súcubo sabe que tiene cola), y de no informar de nada de lo que no esté seguro. Un personaje seguido que no está presente en ninguna de las partes no se consulta.

Cada ejecución es una solicitud al modelo. Cuando **Process now** necesitaría más de tres solicitudes (un tramo largo de la historia que aún no se ha procesado, o **Parts per run** con un valor bajo), primero te indica cuántas partes y solicitudes son y cuánto texto aproximado pasa por el modelo, y solo empieza cuando lo confirmas. Cada solicitud también lleva las instrucciones, el libro de lore y el conocimiento de los personajes, así que el coste real es mayor que el del texto solo.

Cuando **Ask before applying** está desactivado, los cambios se aplican de inmediato y cada uno se escribe en el registro **Applied automatically**, en la sección Processing de la pestaña: quién recibió o perdió qué, y de qué parte. Nada del registro es editable, pero cada fila tiene un botón **Revert**: un elemento añadido se rechaza (queda en *Old knowledge*, como ocurre con una adición rechazada en la revisión) y un elemento retirado vuelve a estar activo. Una fila cuyo elemento hayas editado, borrado o retirado a mano desde entonces se deja como está. El registro guarda los últimos 200 cambios.

Con **Ask before applying** desactivado, la nueva parte se lee en cuanto queda definitiva: al terminar la generación o, si el libro tiene activada la anotación automática (y el redactado) o una traducción, cuando esta termina. Así el modelo lee el texto que conservas y trabaja mientras tú lees. Es un paso de [lo que sigue a la parte](../books/story-editor.md#what-follows-a-generated-part). La siguiente generación encuentra entonces el conocimiento al día y no espera. Lo que quede pendiente (una ejecución que detuviste, una ventana que cerraste) se lee antes de la siguiente generación, como antes.

Mientras el modelo trabaja, un pequeño **progress panel** flota sobre la página. No es modal: puedes seguir leyendo y editando la historia, y puedes **arrastrarlo** por su título. Cerrarlo lo oculta, pero el trabajo continúa (**Cancel** lo detiene).

![El panel de progreso](../../images/plugin-knowledgetracker-progress.png)

Mientras el modelo responde, el panel muestra **What the model is writing** a medida que llega: primero su razonamiento y luego la respuesta, para que una espera larga con un modelo lento no sea una barra vacía. Cuando la solicitud termina, el mismo texto queda bajo **What the model thought** (un bloque por cada solicitud de la ejecución, con lo que respondió), también cuando propuso **nada** y no se abre ninguna revisión. La pestaña mantiene un botón **Last run: what the model thought** que vuelve a abrir el panel, para que veas por qué no se añadió nada.

Cuando el modelo ha respondido, el panel lo indica y muestra **Revisar los cambios**; el mismo botón aparece en el panel de procesamiento de la pestaña (*3 changes to review*). **La revisión nunca se abre por sí sola.** Es una tabla de los cambios, agrupada por personaje:

![Revisando los cambios](../../images/plugin-knowledgetracker-review.png)

Bajo la tabla, **What the model thought** (cerrado al principio) contiene el razonamiento del modelo previo a su respuesta, cuando el modelo lo envía. Muestra por qué se propuso un cambio, o por qué falta uno que esperabas, para que juzgues el cambio antes de aplicarlo. Un modelo que no razona no muestra nada ahí.

| | |
|---|---|
| La casilla | Si el cambio se aplica. Al principio todas están marcadas; cada personaje tiene **Check all** / **Uncheck all**. |
| **Retract** / **Add** | Una retirada convierte el elemento en antiguo (se conserva, ver [la pestaña](#the-knowledge-tab)). Una adición es un elemento nuevo, *Knows* o *Does not know*. El texto y el tipo de una adición se pueden **editar** antes de aplicarla. |
| La cita | Las palabras de la parte que lo muestran, y en qué parte están. Una cita que no se encuentra en esa parte se marca: el modelo puede haberla inventado, revísala antes de aceptarla. |

**Aplicar** aplica los cambios marcados y pasa a lo siguiente. **Cancelar** detiene toda la serie y no cambia nada: las mismas partes se proponen de nuevo la próxima vez. Una adición desmarcada se **conserva como conocimiento antiguo** con su cita (puedes restaurarla); una retirada desmarcada deja el elemento como estaba. Lo que dijo el modelo y no se pudo usar (un personaje por el que no se le preguntó, un elemento que no existe, un cambio sin cita, algo que el personaje ya sabe) aparece en *Omitido*. Lo que se aplicó lleva su parte, así que **Reprocess from here** lo vuelve a retirar. Si la historia cambió mientras revisabas (otra rama, una parte borrada), los cambios no se aplican y hay que procesar de nuevo.

Las partes se leen en **runs** de **Parts per run** (1 por defecto); una que no cabe en el contexto del modelo se recorta hasta lo que cabe. Una serie continúa, ejecución tras ejecución, hasta la última parte asentada; en una serie que te pregunta, una ejecución en la que el modelo no encontró nada se salta sin preguntar. Con **Ask before applying** desactivado, los cambios se aplican de inmediato y el panel guarda un registro con una línea por ejecución.

!!!
La indicación se probó con un modelo en unas pocas escenas, no a gran escala: evita que los personajes aprendan lo que no percibieron, pero revisa lo que se propone, sobre todo en los personajes que están en una escena larga. La solicitud se envía al modelo que elijas más abajo, con el coste y la privacidad habituales de ese servicio.
!!!

<a id="tidying-the-knowledge"></a>

## Ordenar el conocimiento

El modelo anota lo que aprende cada personaje, y en una historia larga la lista crece, con más detalles pequeños de los que quieres enviar al modelo en cada solicitud. **Tidy knowledge** (junto a **Process now**, necesita la configuración del modelo) pide al modelo que vuelva a leer lo que sabe un personaje, con las últimas partes procesadas de la historia como contexto, y que:

- **elimine** un elemento que no merece conservarse: trivialidades, un detalle de una escena que todos los presentes vieron (la historia lo conserva), algo que los acontecimientos posteriores ya superaron, o algo que dice lo mismo que otro elemento;
- **combine** varios elementos sobre lo mismo en uno solo que conserve todos los hechos.

Un elemento eliminado o combinado no se borra: pasa a *Old knowledge* y puede restaurarse. Los cambios se revisan, como los de una lectura (**Ask before applying** decide), o se aplican de inmediato y se escriben en el registro con un botón **Revert**.

La pasada solo puede tocar lo que el modelo aprendió y que no has **bloqueado** (**lock**). Los elementos iniciales, los que añadiste o editaste a mano, los bloqueados y los antiguos nunca se modifican (los dos primeros tipos y los bloqueados se muestran al modelo para que vea qué duplicaría un elemento). Un secreto que ninguna parte ha mencionado durante mucho tiempo no es motivo para eliminarlo, y la indicación lo dice; cuando el modelo tiene dudas, conserva el elemento. Un personaje con menos de **Tidy: from this many items** (8) elementos aprendidos por el modelo no se ordena, y **Tidy: parts of context** (10) es cuántas de las últimas partes procesadas se leen.

**Tidy automatically** (una opción, desactivada por defecto) ejecuta la ordenación por sí sola al terminar una ejecución cuyos cambios se aplican sin preguntar (**Ask before applying** desactivado, así que también la lectura automática de las nuevas partes), una vez que el modelo ha leído **Tidy after this many parts** (10) partes desde la última ordenación. Lo que cambia se aplica y se escribe en el registro con un botón **Revert**, y el registro de la ejecución lo indica. Una ejecución que revisas nunca inicia una ordenación por sí sola, y un personaje con muy pocos elementos se omite sin preguntar al modelo.

Lo mismo desde la barra de comandos: `knowledge-tidy` (`ask true` para obtener los cambios como lista; la pestaña abierta muestra entonces el botón de revisión), y `lock-knowledge` / `unlock-knowledge` con el personaje y el número del elemento.
<a id="settings"></a>

## Ajustes

El engranaje junto a *Track a character* (*Seguir a un personaje*):

| Ajuste | |
|---|---|
| **En el prompt** | Inserta el bloque en el prompt de cada generación. El botón se pone verde mientras está activo. |
| **Profundidad** | Dónde va, contado en mensajes desde el final del prompt: `0` después de las instrucciones del turno, `1` justo antes de ellas. Nunca se coloca antes del prompt del sistema. |
| **Rol** | Si se envía como mensaje de *System*, *Assistant* o *User*. |
| **Decir que no se sabe nada más** | La línea de mundo cerrado. |
| **Dar a la anotación lo que saben los personajes** | Activado (por defecto): [Anotar con IA](#annotating-with-what-the-characters-know) recibe lo que los personajes seguidos de la parte sabían al comenzar. |
| **Procesar las partes nuevas automáticamente** | Véase [Procesamiento](#processing). |
| **Leer con el modelo lo que aprenden los personajes** | El procesamiento pide al modelo lo que ha aprendido cada personaje seguido y lo propone. Desactivado (por defecto): el procesamiento solo anota quién se ha encontrado con quién, lo que no cuesta nada. |
| **Preguntar antes de aplicar** | Activado (por defecto): cada propuesta se muestra para revisión. Desactivado: se aplica de inmediato y queda registrada. |
| **Dejar que el modelo lea partes anteriores** | Mientras lee las partes nuevas, el modelo puede leer la parte en la que se aprendió un elemento (los elementos dicen «aprendido en la parte 12»), para ver si lo que lee confirma, repite o contradice ese elemento: un rumor que ahora se ve con los propios ojos, por ejemplo. Como máximo 5 lecturas de una parte cada una por petición; el texto de una parte larga se recorta a lo que cabe en el contexto, y la revisión muestra qué partes ha leído. Cada lectura es una ronda más con el modelo, por lo que está desactivado por defecto. Un modelo que no puede llamar a funciones lee la petición como antes. |
| **Partes por ejecución** | Cuántas partes lee una petición. Un número mayor sale más barato, pero da una respuesta más larga y una revisión más difícil. |
| **Ordenar automáticamente**, **Ordenar tras este número de partes** | La pasada de orden se ejecuta sola al final de una ejecución que aplica sus cambios sin preguntar, cada este número de partes (10). Desactivada por defecto. |
| **Orden: partes de contexto** | Cuántas de las últimas partes procesadas lee la pasada de orden (10). |
| **Orden: a partir de este número de elementos** | Un personaje con menos elementos aprendidos por el modelo (y que no estén bloqueados) no se ordena (8). |
| **Libro de lore para el modelo** | *Entradas que activan las partes* (por defecto: las entradas que usaría una generación para estas partes), *Todo el libro de lore* (cada entrada; cuando no cabe en el contexto, se envían las activadas y la nota lo indica) o *Sin libro de lore*. |
| **Modelo / Protocolo para la extracción** | Vacío: el modelo del libro. Para esto basta con un modelo más barato, y en un servidor que mantiene una única caché de prompt, un modelo aparte evita que se reemplace. |
| **Separadores de este libro** | Cómo se divide el campo de personajes presentes en nombres, uno por línea (un salto de línea es `\n`). Una palabra como `and` divide entre espacios, así que *Jack and Jill* sigue siendo un solo nombre hasta que lo añadas. |
| **Mis separadores (todos los libros)** | Lo mismo para todos tus libros. Los separadores propios del libro tienen prioridad; sin ninguno, se usan los integrados. |

## Prompts

El mensaje del sistema de la lectura (**Knowledge extraction prompt**) y el de la pasada de orden (**Knowledge tidy prompt**) son
[ajustes heredados](../books/prompts.md#inherited-settings): un libro usa los suyos; si no, la carpeta más cercana por encima que defina uno;
si no, los tuyos; si no, el texto integrado que se probó con un modelo real.

Defínelos en la pestaña *Prompts* del libro, bajo *Extension Settings* (*Ajustes de la extensión*), en el diálogo de una carpeta, o para todos tus
libros en *Settings → Extension Settings → Knowledge Tracker* (*Ajustes → Ajustes de la extensión → Knowledge Tracker*; vacío muestra el texto
integrado en gris). Mantén el formato de respuesta JSON del texto integrado: el plugin lee la respuesta por él. Las instrucciones para `read_part`
las añade el plugin cuando el modelo puede leer partes anteriores, así que tu prompt no las necesita.

<a id="annotating-with-what-the-characters-know"></a>

## Anotar con lo que saben los personajes

El bloque de arriba impide que un modelo convierta a un personaje en omnisciente *mientras escribe*. Los mismos registros también ayudan
a encontrar los descuidos que se han colado de todos modos. Cuando un modelo [anota una parte](../books/annotations.md#letting-a-model-annotate)
(**Anotar con IA**, un rango de partes, la anotación automática, `/generate-annotation`), el rastreador le indica lo que los personajes seguidos
de esa parte sabían **al comenzar** la parte, y el editor señala a un personaje que dice, piensa o hace lo que no puede saber: *Fiora saluda a Thomas
por su nombre; los registros dicen que nunca se han conocido.* La nota nombra el hecho.

- **Los hechos son los de la parte, no los del final del libro.** Lo que un personaje aprendió en una parte posterior todavía no se conoce; un elemento
  que una parte posterior retracta seguía siendo cierto. Lo que aporta la propia parte (lo que se aprende, a quién se conoce, lo que deja de ser cierto
  en ella) se lista aparte, para que la primera mitad de la parte no se compruebe contra la segunda.
- **Qué personajes.** Los seguidos que están en los *Personajes presentes* de la parte, son su punto de vista, o se nombran en su texto
  (una palabra completa, sin distinguir mayúsculas, también los alias).
- **Los elementos que no son hechos en ningún momento** se dejan fuera: los que retiraste a mano, los que la pasada de orden eliminó, y los que
  rechazaste en una revisión.
- **Solo lo registrado.** Se le indica al modelo que lo que los registros no dicen no es un hecho, para que no señale a un personaje por algo que
  el rastreador simplemente nunca supo. *Decir que no se sabe nada más* también añade «(nadie más)» a con quién se ha encontrado un personaje.
- Si una parte anterior a la revisada no la leyó el rastreador, los hechos lo indican: falta lo que los personajes aprendieron allí, y
  **Procesar ahora** primero da una revisión mejor.
- Los hechos cuestan tokens en la petición de la anotación, solo para las partes que tienen un personaje seguido. Desactívalos con
  **Dar a la anotación lo que saben los personajes**.

<a id="the-block-as-a-macro"></a>

## El bloque como macro

En lugar de (o además de) la inserción automática, puedes colocar el bloque tú mismo con la
[macro {%{`{{command::knowledge-tracker-block::<characters>}}`}%}](../templates/macros.md#commands). El argumento es la lista de
los personajes de la escena, así que una plantilla puede pasar el campo de la parte:

```
{%{{{command::knowledge-tracker-block::{{presentCharacters}}}}}%}
```

La macro imprime el bloque tanto si **En el prompt** está activo como si no, así que desactívalo cuando uses la macro, o el modelo lo recibe
dos veces. La profundidad y el rol no se aplican a la macro (el bloque forma parte del mensaje que contiene la macro). No imprime nada cuando
ninguno de los personajes listados está seguido. La respuesta de una macro de comando se guarda con la parte de la que continúa la nueva parte,
así que una segunda generación desde la misma parte no ve un cambio que hayas hecho entre medias; regenerar y hacer swipe vuelven a preguntar.

<a id="commands"></a>

## Comandos

Para la [línea de comandos](../commands/index.md) y para agentes:

| Comando | |
|---|---|
| `knowledge-status` | Los ajustes y en qué punto está el procesamiento. |
| `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character` | Los personajes seguidos, referidos por nombre, alias o número. |
| `list-knowledge`, `add-knowledge`, `retract-knowledge`, `restore-knowledge`, `delete-knowledge` | Los elementos de un personaje, referidos por su número en `list-knowledge`. |
| `knowledge-process` | Procesa ahora (`parts`: como máximo ese número). Con el modelo activado, `ask true` devuelve lo que encontró el modelo como cambios numerados y los retiene: todavía no se cambia nada. La pestaña Knowledge abierta muestra un botón **changes found by a command** (*cambios encontrados por un comando*) que abre la revisión habitual (Apply / Cancel); un agente los aplica con `apply-knowledge-changes`. |
| `apply-knowledge-changes` | Aplica los cambios retenidos, todos o los números indicados (igual que **Apply** en la revisión de la pestaña). |
| `knowledge-tidy` | La pasada de orden. Con `ask true` devuelve los cambios como lista numerada y los retiene (la pestaña muestra un botón para revisarlos; `apply-knowledge-changes` los aplica). |
| `lock-knowledge`, `unlock-knowledge` | Bloquean un elemento (personaje, número de elemento) para que la pasada de orden no lo toque, o lo desbloquean. |
| `knowledge-reprocess` | Vuelve a procesar desde una parte (su identificador o posición); pide confirmación primero. Con el modelo activado, solo devuelve el estado, y `knowledge-process` vuelve a leer las partes. |
| `knowledge-set-start` | Fija el punto de inicio; sin parte, el inicio del libro. |
| `knowledge-tracker-block` | El bloque como texto, para la macro. |

La línea de comandos divide sus argumentos por los espacios: pon una lista de personajes entre comillas (`/knowledge-tracker-block "Fiora, Mara"`).

<a id="storage"></a>

## Almacenamiento

Todo se guarda con el libro (en sus atributos), así que forma parte de las [copias de seguridad del libro](../books/backups.md) y de las copias.
