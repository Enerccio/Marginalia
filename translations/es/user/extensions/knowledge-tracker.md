---
label: Registro de conocimiento
order: 11
---

# Registro de conocimiento

Lleva el control de lo que saben los personajes de tu historia, y de lo que **no** saben, y lo incluye en el prompt. Un modelo
tiende a hacer que un personaje sepa cosas sobre personas que nunca ha conocido (su nombre, su oficio, su cara), a menos que las
instrucciones lo impidan; con el Registro de conocimiento, el prompt indica, para cada personaje seguido en la escena,
qué sabe ese personaje y que no sabe nada más.

!!!
Tú eliges a quién se sigue. Solo se describen al modelo los personajes que añades a la pestaña, y solo mientras estén entre los
*personajes presentes* de la parte que se está escribiendo. Nada se deduce del texto de la historia.
!!!

<a id="the-knowledge-tab"></a>

## La pestaña Conocimiento

La barra lateral izquierda del [editor de la historia](../books/story-editor.md) muestra una pestaña **Conocimiento** junto a *Esquema*. Todo se guarda en el libro a medida que lo cambias.

![La pestaña Conocimiento](../../images/plugin-knowledgetracker.png)

**Seguir a un personaje** pide un **nombre** y, opcionalmente, **alias**: los otros nombres con los que aparece el personaje en el
campo *Personajes presentes* de una parte, separados por comas. Un nombre o un alias solo puede pertenecer a un personaje seguido.

Abre un personaje con la flecha de su tarjeta para ver qué sabe:

| | |
|---|---|
| **Sabe** (verde) / **No sabe** (rojo) | Una frase corta sobre cualquier cosa: una persona, un lugar, un secreto, un suceso (*Peter sabe del complot contra el duque*, *Fiora no sabe que Thomas es su padre*). Escribe explícitamente lo que un personaje **no** sabe, porque eso es lo que el modelo respeta. |
| El orden y **#12** | Lo más nuevo queda arriba (lo antiguo también). Un elemento que aprendió el modelo termina con el pequeño número de la parte en la que se aprendió (**#12**): un clic desplaza la historia hasta esa parte y la resalta (en un teléfono, primero cierra el cajón). Los elementos que escribiste no tienen número, y tampoco los tiene uno cuya parte no esté en la rama activa (otra rama, o la parte fue eliminada). |
| **Añadir conocimiento** | Añade un elemento, activo de inmediato. Mantenlo en **un solo pensamiento**: un elemento que dice dos cosas (*no sabe quién es Thomas, nunca lo ha conocido*) se retira en cuanto una de ellas deja de ser cierta. El botón **editar** cambia el texto y el tipo. |
| **Retirar** (el círculo con una barra) | El elemento ya no es cierto (*no ha conocido a Thomas* después de que se encuentren). No se elimina: pasa a ser *conocimiento antiguo* y ya no se envía al modelo. |
| **Bloquear** (el candado, en los elementos que aprendió el modelo) | La [pasada de orden](#tidying-the-knowledge) nunca ve un elemento bloqueado: úsalo para lo que debe permanecer, una traición, un secreto. La historia aún puede contradecir un elemento bloqueado (un personaje que pierde la memoria): la lectura de la historia lo retira como de costumbre. |
| **Eliminar** | Quita un elemento para siempre, previa confirmación. Retirar lo conserva, así que úsalo si tienes dudas. |
| **Seleccionar elementos** (junto a **Añadir conocimiento**) | Elimina muchos elementos rápidamente: cada elemento del personaje, incluidos los antiguos, recibe una casilla. **Seleccionar todo** / **Deseleccionar todo** marca o desmarca todas, **Eliminar seleccionados (n)** quita las marcadas para siempre tras una confirmación, **Cancelar** deja la selección. |
| **Ha conocido a** | Las personas que el personaje ha conocido, anotadas mediante el [procesamiento](#processing). El procesamiento puede anotar un nombre que no es una persona (una nota en el campo de personajes presentes): abre **Editar** y cambia la lista **Ha conocido a**, un nombre tras otro separados por comas. Los nombres que escribes se mantienen cuando la historia se vuelve a leer; uno que quitas solo regresa si se vuelve a leer la parte en la que se vio. |
| **Conocimiento antiguo (n)** | Plegado por defecto. Cada elemento antiguo puede **copiarse**, **editarse**, **restaurarse** (vuelve a estar activo, a partir de ahora) o eliminarse. |

<a id="mass-changes"></a>

### Cambios masivos

El botón **Cambios masivos** (el icono de lista junto a **Seguir a un personaje**) abre una ventana con todos los personajes seguidos y
todos sus elementos, también los antiguos, en una cuadrícula con casillas: el tipo, el texto, el estado (*Activa*, o antigua: *retirado*,
*descartado en una revisión*, *ordenado*), de dónde viene (*Estado inicial*, *Escrito a mano*, *Aprendido de la historia*) y la parte
en la que se aprendió. Marca elementos en tantas cuadrículas como quieras (la casilla del encabezado de una cuadrícula marca todos sus elementos;
**Seleccionar todo** / **Deseleccionar todo** en la barra inferior marca todos los elementos de todos los personajes), y después:

| | |
|---|---|
| **Retirar seleccionados (n)** | Los elementos activos marcados pasan a ser conocimiento antiguo, como lo hace **Retirar** con uno solo. |
| **Restaurar seleccionados (n)** | Los elementos antiguos marcados vuelven a estar activos, a partir de ahora. |
| **Eliminar seleccionados (n)** | Quita para siempre los elementos marcados, previa confirmación. |

Los contadores de los botones indican cuántos de los elementos marcados cambia cada uno. Las marcas se mantienen tras un cambio, de modo que
los elementos retirados pueden eliminarse después.

**Exportar a JSON** (en la parte superior de la ventana) descarga como archivo los personajes seguidos del libro (nombres, alias, elementos con su
tipo, estado, origen y bloqueo, las personas que han conocido); nada del procesamiento, así que sirve para cualquier libro.
**Importar desde JSON** lee un archivo de ese tipo y lo incorpora al libro, y solo añade: un personaje cuyo nombre el libro ya sigue (como nombre o como alias)
recibe los alias, elementos y personas conocidas que aún no tiene (un elemento con el mismo tipo y texto se omite); los demás se convierten en
personajes seguidos nuevos; un alias que ya tiene otro personaje se deja fuera. Un elemento importado no tiene detrás una parte de este libro, así que un
nuevo procesamiento nunca lo elimina: uno que aprendió el modelo cuenta como escrito a mano, y uno antiguo sigue siendo antiguo. El mensaje tras la importación
indica cuántos personajes se añadieron o actualizaron y cuántos elementos se añadieron o ya existían.

<a id="what-goes-into-the-prompt"></a>

## Qué entra en el prompt

Para cada personaje seguido que está entre los personajes presentes, el prompt recibe un bloque como este:

```
What the characters of this scene know. A character acts only on the knowledge listed for it:

Fiora knows: Mara, a courier of the Guild of Lamps and the sister of her friend Nell; she owes the Guild of Lamps forty crowns.
Fiora does not know: that her father is alive, or that Thomas is her father.
Fiora has met: Mara.
Fiora is currently with: Mara, Thomas.
Fiora has never met or heard of anyone else in this story and knows nothing else of its secrets, plans or events.
```

Solo se envían los elementos activos. La última línea (la línea de **mundo cerrado**) es la que impide que el modelo dé al personaje
conocimientos que no puede tener; cubre lo que cuenta la historia (personas, lugares, secretos, planes, sucesos), no lo que una persona de ese
mundo sabe de todos modos, así que un personaje sigue conociendo su propio pueblo. Puedes desactivar esa línea en los ajustes. Un personaje
seguido que no está presente no recibe nada, ni siquiera una línea que diga que no lo sabe.

<a id="who-is-present"></a>

### Quién está presente

El bloque se construye a partir del campo **Personajes presentes** de la parte. El campo es texto libre, así que se lee de forma flexible:

- el texto entre corchetes se ignora: `Tomáš (tired), Mizuki` es Tomáš y Mizuki;
- los nombres se separan en los **separadores** (por defecto: `,` `;` un salto de línea, `/`, `|`, `、` `，` `；`);
- lo que va después de ` - ` o `:` es una nota y se descarta (`Mara - wounded`);
- las mayúsculas o minúsculas, los acentos y un *the* inicial no importan;
- un nombre tiene que coincidir **completo**: *Mara's brother* no es *Mara*. Usa los alias cuando un personaje se escribe de más de una forma. La idea es que una coincidencia que falta cuesta un bloque, mientras que una coincidencia falsa describiría a un personaje que no está en la escena.

El campo *Point of View* no se lee por sí solo, así que escribe también al personaje del punto de vista entre los personajes presentes.
<a id="processing"></a>

## Procesamiento

El plugin sigue la historia. **Procesamiento** lee las partes de la rama activa en orden, desde el **punto de inicio** en adelante,
y mantiene un **cursor** (la última parte que leyó). Sin el modelo, anota quién se ha encontrado con quién: un personaje seguido
se ha encontrado con todos los nombres que están en el campo de personajes presentes junto con él, y el bloque los enumera. Con el
modelo, además encuentra lo que los personajes **aprendieron** (véase [Lectura de lo que aprenden los personajes](#reading-what-the-characters-learn)).

![El panel de procesamiento](../../images/plugin-knowledgetracker-processing.png)

El panel de la parte superior de la pestaña muestra en qué punto está. Se recarga por sí solo cuando una parte pasa a ser final
(después de la anotación automática y la reescritura, si el libro las tiene); si añades, editas o eliminas partes de otra forma,
pulsa el botón **Actualizar** (las flechas junto al engranaje) para volver a leer la historia. Contiene:

- **Procesar ahora**: lee las partes que todavía no están procesadas.
- **Procesar la siguiente parte**: lee solo la primera parte que no está procesada, paso a paso (con el modelo: una petición para esa parte, sin importar lo que indique *Partes por ejecución*). Úsalo para probar la configuración o para ver lo que el modelo encuentra en una sola parte. El comando `knowledge-process` con `parts` en 1 hace lo mismo.
- **Punto de inicio**: la parte desde la que empieza el procesamiento (*Inicio del libro* por defecto). Las partes anteriores nunca se leen. Si lo retrasas, se vuelven a leer las partes desde el nuevo inicio; si lo adelantas, se conserva lo que ya se leyó.
- **Partes procesadas**: las últimas partes que se leyeron. Una parte cuyo texto **cambió desde que se procesó** (la editaste o la reescribiste) aparece marcada; no se vuelve a procesar por sí sola por ese motivo. El botón de cada fila, **Reprocesar desde aquí**, descarta lo que procedía de esa parte y de las posteriores (los elementos y las retractaciones que escribiste a mano se mantienen) y las vuelve a leer.

Lo que hace el plugin por sí solo, cuando **Procesar partes nuevas automáticamente** está activado (desactivado por defecto):

- Antes de iniciar una generación, se procesan las partes escritas desde la última vez, para que el prompt sepa quién se ha encontrado con quién. Una parte que está siendo sustituida por un *Regenerar* o por un cambio de variante se deja en paz. Con el modelo, esto lee con el modelo solo cuando **Preguntar antes de aplicar** está desactivado (véase más abajo); en caso contrario, solo anota quién se ha encontrado con quién, y lo que encuentre el modelo se inicia desde la pestaña, porque nunca se te impone una revisión en mitad de una generación.
- Cuando una parte se **regenera** y llega el texto nuevo, se retira lo que se aprendió del texto anterior.
- Cuando cambias de variante o pasas a otra rama, el cursor vuelve a la última parte que comparten ambas ramas.
<a id="reading-what-the-characters-learn"></a>

## Leer lo que aprenden los personajes

Marca **Leer lo que aprenden los personajes con el modelo** en los ajustes. Entonces **Procesar ahora** pide al modelo, para las
siguientes partes de la historia, qué **aprendió** cada personaje seguido que está en ellas: lo que vio, oyó, leyó o le contaron.
También indica qué elementos contradice la historia (*nunca ha conocido a Thomas* cuando ya se conocen). La petición incluye las
partes (con su punto de vista y los personajes presentes), lo que cada personaje seguido sabe ahora y, según elijas, el libro de
lore; se le indica al modelo que omita lo que una persona de ese mundo sabe de todos modos (quién es el rey) y lo que se deduce de
la propia naturaleza del personaje (una súcubo sabe que tiene cola), y que no informe de nada de lo que no esté seguro. Un personaje
seguido que no está presente en ninguna de las partes no se consulta.

Cada ejecución es una sola petición al modelo. Cuando **Procesar ahora** necesitaría más de tres (un tramo largo de historia que
aún no se procesó, o **Partes por ejecución** puesto bajo), primero te dice cuántas partes y peticiones son y aproximadamente cuánto
texto pasa por el modelo, y solo empieza cuando lo confirmas. Cada petición también lleva las instrucciones, el libro de lore y el
conocimiento de los personajes, así que el coste real es mayor que el del texto solo.

Cuando **Preguntar antes de aplicar** está desactivado, los cambios se aplican de inmediato y cada uno se escribe en el registro
**Aplicados automáticamente** de la sección Procesamiento de la pestaña: quién obtuvo o perdió qué, y de qué parte. Nada del registro
es editable, pero cada fila tiene un botón **Revertir**: un elemento añadido se rechaza (queda en *Conocimiento antiguo*, como ocurre con
una adición rechazada en la revisión), y un elemento retirado vuelve a estar activo. Una fila cuyo elemento hayas editado, borrado o
retirado a mano desde entonces se deja como está. El registro guarda los últimos 200 cambios.

Con **Preguntar antes de aplicar** desactivado, la nueva parte se lee en cuanto queda definitiva: cuando termina la generación o,
si el libro tiene activada la anotación automática (y redacción) o una traducción, cuando esta termina; así el modelo lee el texto
que conservas y trabaja mientras tú lees. Es un paso de [lo que sigue a la parte](../books/story-editor.md#what-follows-a-generated-part). La siguiente generación encuentra entonces
el conocimiento al día y no espera. Lo que quede pendiente (una ejecución que paraste, una ventana que cerraste) se lee antes de la
siguiente generación, como antes.

Mientras el modelo trabaja, un pequeño **panel de progreso** flota sobre la página. **No es modal**: puedes seguir leyendo y editando
la historia, y puedes **arrastrarlo** por su título. Cerrarlo lo oculta, pero el trabajo continúa (**Cancelar** lo detiene).

![El panel de progreso](../../images/plugin-knowledgetracker-progress.png)

Mientras el modelo responde, el panel muestra **Lo que escribe el modelo** a medida que llega: primero su razonamiento y luego la
respuesta, para que una espera larga con un modelo lento no sea una barra en blanco. Cuando la petición termina, el mismo texto
permanece bajo **Lo que pensó el modelo** (un bloque por cada petición de la ejecución, con lo que respondió), también cuando no
propuso **nada** y no se abre ninguna revisión: la pestaña mantiene un botón **Última ejecución: lo que pensó el modelo** que vuelve
a abrir el panel, para que veas por qué no se añadió nada.

Cuando el modelo ha respondido, el panel lo indica y muestra **Revisar los cambios**; el mismo botón está en el panel de procesamiento
de la pestaña (*3 cambios por revisar*). **La revisión nunca se abre por sí sola.** Es una tabla de los cambios, agrupada por personaje:

![Revisar los cambios](../../images/plugin-knowledgetracker-review.png)

Debajo de la tabla, **Lo que pensó el modelo** (cerrado al principio) contiene el razonamiento del modelo que precedió a su respuesta,
cuando el modelo lo envía: muestra por qué se propuso un cambio, o por qué falta uno que esperabas, para que puedas juzgarlo antes de
aplicarlo. Un modelo que no razona no muestra nada ahí.

| | |
|---|---|
| La casilla | Si el cambio se aplica. Todas están marcadas al principio; cada personaje tiene **Marcar todo** / **Desmarcar todo**. |
| **Retirar** / **Añadir** | Una retirada convierte un elemento en antiguo (se conserva, ver [la pestaña](#the-knowledge-tab)). Una adición es un elemento nuevo, *Sabe* o *No sabe*. El texto y el tipo de una adición se pueden **editar** antes de aplicarla. |
| La cita | Las palabras de la parte que lo muestran, y en qué parte están. Una cita que no se encuentra en esa parte se marca: el modelo pudo inventarla, compruébala antes de aceptar. |

**Aplicar** aplica los cambios marcados y pasa a lo siguiente. **Cancelar** detiene toda la serie y no cambia nada: las mismas partes
se proponen de nuevo la próxima vez. Una adición que desmarques se **conserva como conocimiento antiguo** con su cita (puedes restaurarla);
una retirada desmarcada deja el elemento como estaba. Lo que dijo el modelo y no se pudo usar (un personaje por el que no se le preguntó,
un elemento que no existe, un cambio sin cita, algo que el personaje ya sabe) aparece en *Omitido*. Lo que se aplicó lleva su parte, así que
**Reprocesar desde aquí** lo deshace de nuevo. Si la historia cambió mientras revisabas (otra rama, una parte borrada), los cambios no se
aplican y procesas de nuevo.

Las partes se leen en **ejecuciones** de **Partes por ejecución** (1 por defecto); una que no cabe en el contexto del modelo se recorta
hasta lo que cabe. Una serie continúa, ejecución tras ejecución, hasta la última parte asentada; en una serie que te pregunta, una ejecución
en la que el modelo no encontró nada se salta sin preguntar. Con **Preguntar antes de aplicar** desactivado, los cambios se aplican de
inmediato y el panel guarda un registro con una línea por ejecución.

!!!
El prompt se probó con un modelo en unas pocas escenas, no a gran escala: impide que los personajes aprendan lo que no percibieron, pero
revisa lo que se propone, sobre todo para personajes que están en una escena larga. La petición se envía al modelo que elijas abajo, con
el coste y la privacidad habituales de ese servicio.
!!!

<a id="tidying-the-knowledge"></a>

## Depurar el conocimiento

El modelo anota lo que aprende cada personaje, y en una historia larga la lista crece, con más cosas pequeñas de las que quieres enviar
al modelo en cada petición. **Depurar conocimiento** (junto a **Procesar ahora**, necesita el ajuste del modelo) pide al modelo que vuelva
a leer lo que sabe un personaje, con las últimas partes procesadas de la historia como contexto, y que

- **descarte** un elemento que no merece conservarse: trivialidades, un detalle de una escena que todos los presentes vieron (la historia
  lo conserva), algo que los acontecimientos posteriores superaron, o algo que dice lo mismo que otro elemento;
- **fusione** varios elementos sobre lo mismo en uno que conserve todos los datos.

Un elemento descartado o fusionado no se borra: pasa a *Conocimiento antiguo* y puede restaurarse. Los cambios se revisan, como los de una
lectura (**Preguntar antes de aplicar** decide), o se aplican de inmediato y se escriben en el registro con un botón **Revertir**.

Lo que la pasada puede tocar es solo lo que el modelo aprendió y que no has **bloqueado**. Los elementos iniciales, los que añadiste o editaste
a mano, los bloqueados y los antiguos nunca se cambian (los dos primeros tipos y los bloqueados se muestran al modelo para que vea qué
duplicaría un elemento). Un secreto que ninguna parte ha mencionado desde hace mucho no es motivo para descartarlo, y el prompt lo dice; cuando
el modelo duda, conserva el elemento. Un personaje con menos de **Depurar: a partir de estos elementos** (8) elementos aprendidos por el modelo
no se depura, y **Depurar: partes de contexto** (10) es cuántas de las últimas partes procesadas lee.

**Depurar automáticamente** (un ajuste, desactivado por defecto) ejecuta la depuración por sí sola al final de una ejecución cuyos cambios se
aplican sin preguntar (**Preguntar antes de aplicar** desactivado, así que también la lectura automática de las partes nuevas), una vez que el
modelo ha leído **Depurar tras este número de partes** (10) partes desde la última depuración. Lo que cambia se aplica y se escribe en el registro
con un botón **Revertir**, y el registro de la ejecución lo indica. Una ejecución que revisas nunca inicia una depuración por sí sola, y un
personaje con demasiado pocos elementos se omite sin preguntar al modelo.

Lo mismo desde la barra de comandos: `knowledge-tidy` (`ask true` para obtener los cambios como lista; la pestaña abierta muestra entonces el botón de revisión),
y `lock-knowledge` / `unlock-knowledge` con el personaje y el número del elemento.
<a id="settings"></a>

## Ajustes

El engranaje junto a *Track a character*:

| Ajuste | |
|---|---|
| **In prompt** | Inserta el bloque en el prompt de cada generación. El botón se vuelve verde mientras está activado. |
| **Depth** | Dónde va, contado en mensajes desde el final del prompt: `0` después de las instrucciones del turno, `1` justo antes de ellas. Nunca va antes del prompt de sistema. |
| **Role** | Si se envía como mensaje de *System*, *Assistant* o *User*. |
| **Say that nothing else is known** | La línea de mundo cerrado. |
| **Give what the characters know to the annotation** | Activado (por defecto): [Anotar con IA](#annotating-with-what-the-characters-know) recibe lo que los personajes seguidos sabían al comenzar la parte. |
| **Process new parts automatically** | Véase [Procesamiento](#processing). |
| **Read what the characters learn with the model** | El procesamiento pregunta al modelo qué aprendió cada personaje seguido y lo propone. Desactivado (por defecto): el procesamiento solo anota quién ha conocido a quién, lo cual no cuesta nada. |
| **Ask before applying** | Activado (por defecto): cada propuesta se muestra para revisión. Desactivado: se aplica de inmediato y queda registrada. |
| **Let the model read earlier parts** | Mientras lee las partes nuevas, el modelo puede leer la parte en la que se aprendió un elemento (los elementos dicen "aprendido en la parte 12"), para comprobar si lo que lee confirma, repite o contradice ese elemento: un rumor que ahora se ve con los propios ojos, por ejemplo. Como máximo 5 lecturas de una parte cada una por solicitud; el texto de una parte larga se recorta a lo que cabe en el contexto, y la revisión muestra qué partes ha leído. Cada lectura es una ronda más con el modelo, por lo que está desactivado por defecto. Un modelo que no puede llamar a funciones lee la solicitud como antes. |
| **Parts per run** | Cuántas partes lee una solicitud. Un número mayor sale más barato, pero da una respuesta más larga y una revisión más difícil. |
| **Tidy automatically**, **Tidy after this many parts** | La pasada de ordenación se ejecuta sola al final de una ejecución que aplica sus cambios sin preguntar, cada cierto número de partes (10). Desactivada por defecto. |
| **Tidy: parts of context** | Cuántas de las últimas partes procesadas lee la pasada de ordenación (10). |
| **Tidy: from this many items** | Un personaje con menos elementos que el modelo aprendió (y que no están bloqueados) no se ordena (8). |
| **Lorebook for the model** | *Entries the parts activate* (por defecto: las entradas que usaría una generación para estas partes), *The whole lorebook* (todas las entradas; cuando no cabe en el contexto, se envían las activadas y la nota lo indica) o *No lorebook*. |
| **Model / Protocol for the extraction** | Vacío: el modelo del libro. Para esto basta un modelo más barato, y en un servidor que mantiene una sola caché de prompts, un modelo aparte evita que lo reemplace. |
| **Separators of this book** | Cómo se divide el campo de personajes presentes en nombres, uno por línea (un salto de línea es `\n`). Una palabra como `and` divide entre espacios, así que *Jack and Jill* sigue siendo un solo nombre hasta que lo añadas. |
| **My separators (all books)** | Lo mismo para todos tus libros. Los separadores propios del libro tienen prioridad; si no hay ninguno, se usan los integrados. |

## Prompts

El mensaje de sistema de la lectura (**Knowledge extraction prompt**) y el de la pasada de ordenación (**Knowledge tidy prompt**) son
[ajustes heredados](../books/prompts.md#inherited-settings): un libro usa los suyos; si no, los de la carpeta superior más cercana que defina uno;
si no, los tuyos; si no, el texto integrado, probado con un modelo real.

Configúralos en la pestaña *Prompts* del libro, en *Extension Settings*, en el diálogo de una carpeta o, para todos tus libros, en
*Settings → Extension Settings → Knowledge Tracker* (vacío muestra el texto integrado en gris). Mantén el formato de respuesta JSON del
texto integrado: el plugin lee la respuesta por él. Las instrucciones para `read_part` las añade el plugin cuando el modelo puede leer
partes anteriores, así que tu prompt no las necesita.

<a id="annotating-with-what-the-characters-know"></a>

## Anotar con lo que saben los personajes

El bloque anterior impide que un modelo convierta a un personaje en omnisciente *mientras escribe*. Los mismos registros también ayudan a
encontrar los fallos que se hayan colado de todos modos. Cuando un modelo [anota una parte](../books/annotations.md#letting-a-model-annotate) (**Anotar con IA**, un rango de partes, la
anotación automática, `/generate-annotation`), el rastreador le indica lo que los personajes seguidos de esa parte sabían **al comenzar la parte**,
y el editor marca a un personaje que dice, piensa o hace algo que no puede saber: *Fiora saluda a Thomas por su nombre; los registros dicen que nunca lo ha conocido.* La nota nombra el hecho.

- **Los hechos son los de la parte, no los del final del libro.** Lo que un personaje aprendió en una parte posterior todavía no se conoce; un elemento que una parte posterior retracta seguía siendo cierto. Lo que aporta la propia parte (lo que se aprende, a quién se conoce, lo que deja de ser cierto en ella) se lista aparte, para que la primera mitad de la parte no se compare con la segunda.
- **Qué personajes.** Los seguidos que están en los *Personajes presentes* de la parte, son su punto de vista o se nombran en su texto
  (una palabra completa, sin distinguir mayúsculas, también los alias).
- **Los elementos que no son hechos en ningún momento** se dejan fuera: los que retiraste a mano, los que la pasada de ordenación eliminó y los que rechazaste en una revisión.
- **Solo lo registrado.** Al modelo se le indica que lo que los registros no dicen no es un hecho, para que no marque a un personaje por algo que el rastreador simplemente nunca supo. *Say that nothing else is known* añade también "(nadie más)" a con quién ha conocido un personaje.
- Si una parte anterior a la revisada no la leyó el rastreador, los hechos lo indican: falta lo que los personajes aprendieron allí, y
  **Procesar ahora** primero da una revisión mejor.
- Los hechos cuestan tokens en la solicitud de la anotación, solo para las partes que tienen algún personaje seguido. Desactívalos con **Give what
  the characters know to the annotation**.

<a id="the-block-as-a-macro"></a>

## El bloque como macro

En lugar de (o además de) la inserción automática, puedes colocar el bloque tú mismo con el
[macro {%{`{{command::knowledge-tracker-block::<characters>}}`}%}](../templates/macros.md#commands). El argumento es la lista de
personajes de la escena, así que una plantilla puede pasar el campo de la parte:

```
{%{{{command::knowledge-tracker-block::{{presentCharacters}}}}}%}
```

El macro imprime el bloque tanto si **In prompt** está activado como si no, así que desactívalo cuando uses el macro, o el modelo lo recibirá dos veces. La profundidad y el rol no se aplican al macro (el bloque forma parte del mensaje que lo contiene). No imprime nada
cuando ninguno de los personajes listados es seguido. La respuesta de un macro de comando se guarda con la parte de la que continúa la nueva parte,
así que una segunda generación desde la misma parte no ve un cambio que hayas hecho entretanto; regenerar y swipe vuelven a preguntar.

<a id="commands"></a>

## Comandos

Para la [línea de comandos](../commands/index.md) y para agentes:

| Comando | |
|---|---|
| `knowledge-status` | Los ajustes y en qué punto está el procesamiento. |
| `list-tracked-characters`, `add-tracked-character`, `update-tracked-character`, `delete-tracked-character` | Los personajes seguidos, referidos por nombre, alias o número. |
| `list-knowledge`, `add-knowledge`, `retract-knowledge`, `restore-knowledge`, `delete-knowledge` | Los elementos de un personaje, referidos por su número en `list-knowledge`. |
| `knowledge-process` | Procesa ahora (`parts`: como máximo esa cantidad). Con el modelo activado, `ask true` devuelve lo que encontró el modelo como cambios numerados y los retiene: todavía no se cambia nada. La pestaña Knowledge abierta muestra un botón **changes found by a command** que abre la revisión habitual (Apply / Cancel); un agente los aplica con `apply-knowledge-changes`. |
| `apply-knowledge-changes` | Aplica los cambios retenidos, todos o los números indicados (lo mismo que **Apply** en la revisión de la pestaña). |
| `knowledge-tidy` | La pasada de ordenación. Con `ask true` devuelve los cambios como una lista numerada y los retiene (la pestaña muestra un botón para revisarlos; `apply-knowledge-changes` los aplica). |
| `lock-knowledge`, `unlock-knowledge` | Bloquean un elemento (personaje, número de elemento) para que la pasada de ordenación no lo toque, o lo desbloquean. |
| `knowledge-reprocess` | Vuelve a procesar desde una parte (su identificador o posición); pide confirmación antes. Con el modelo activado solo devuelve el estado anterior; `knowledge-process` vuelve a leer las partes. |
| `knowledge-set-start` | Establece el punto de inicio; sin parte, el inicio del libro. |
| `knowledge-tracker-block` | El bloque como texto, para el macro. |

La línea de comandos divide sus argumentos por los espacios: pon una lista de personajes entre comillas (`/knowledge-tracker-block "Fiora, Mara"`).

<a id="storage"></a>

## Almacenamiento

Todo se guarda con el libro (en sus atributos), así que forma parte de las [copias de seguridad del libro](../books/backups.md) y de las copias.
