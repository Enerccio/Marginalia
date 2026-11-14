---
label: Historial de versiones
order: 85
---

# Historial de versiones de una parte

Cada vez que cambia el texto de una parte, Marginalia guarda el texto que tenía. Puedes consultar las versiones anteriores de una parte,
ver qué cambió entre ellas, recuperar una, dar nombre a una versión para que nunca se pierda y buscar en el texto antiguo.

Una parte cambia cuando el modelo la escribe (la primera generación, **Regenerar**, **Variante**, [**Reescribir**](story-editor.md#redrafting-a-part)),
cuando la editas tú (**Editar** y **Guardar**), con una acción masiva (**Buscar y reemplazar**, una importación, un comando), cuando lo hace un
[agente](../agents.md), una [extensión](../extensions/index.md), al [combinar](story-editor.md#merging-a-part-into-the-previous-one)
dos partes, y al volver a poner una versión. Cada uno de estos casos crea una **versión** con la hora y quién la hizo:

| Origen | |
|---|---|
| **Generada**, **Regenerada**, **Reescrita** | Un modelo escribió el texto. La versión indica qué modelo y protocolo se usaron, y conserva el razonamiento que lo acompañó. |
| **Autor** | Lo escribiste tú. Un cambio hecho por un comando, una acción masiva, una importación o un agente lo indica en la línea de la versión (un agente aparece marcado). |
| **Extensión** | Una extensión cambió el texto. |
| **Combinada** | El texto de una parte que se combinó con esta. |
| **Restaurada** | Una versión que se volvió a poner encima, una copia de la versión a la que hace referencia. |
| **Versión guardada** | Una versión que guardaste a mano con un nombre. |

El historial no es una segunda copia del libro. Las versiones se almacenan aparte de las partes, así que un historial largo no
hace más lenta la historia, y un libro que se exporta, publica o lee en el visor solo tiene el texto actual.

<a id="looking-at-the-history"></a>

## Consultar el historial

Elige **Historial** en el menú (☰) de la parte. El número entre corchetes indica cuántas versiones tiene la parte. (La opción no aparece
mientras se edita el texto de la parte, ni en una parte que todavía no está guardada).

![El menú de una parte con la opción Historial](../../images/history-part-menu.png)

![El historial de una parte: las versiones a la izquierda, la versión seleccionada con los cambios a la derecha](../../images/history-dialog.png)

La lista de la izquierda muestra la versión más reciente arriba. Cada fila tiene la hora, el origen, el número de palabras y el
nombre de la versión, si tiene uno; la versión más reciente aparece marcada como **Actual**: es el texto que tiene la parte ahora. Haz clic en una fila
para ver esa versión a la derecha. Encima del texto, elige con qué se compara:

| | |
|---|---|
| **Versión anterior** | Los cambios respecto a la versión anterior, palabra por palabra. |
| **Texto actual** | Los cambios respecto al texto que tiene la parte ahora. |
| **Solo el texto** | El texto tal como era, sin comparar. |

Una versión que escribió un modelo muestra su razonamiento en un bloque que puedes desplegar. Una versión **Restaurada** indica de qué
versión es copia.

En el teléfono, el diálogo ocupa toda la pantalla: las versiones aparecen como tarjetas encima de la versión seleccionada, y no hace falta usar el ratón.

<a id="putting-a-version-back"></a>

## Volver a poner una versión

Hay dos formas, y se diferencian en lo que ocurre con las versiones posteriores:

| | |
|---|---|
| **Restaurar encima** | El texto de la versión elegida pasa a ser de nuevo el texto de la parte, como una nueva versión **Restaurada** en la parte superior. No se pierde nada: las versiones intermedias siguen en la lista. Úsala para recuperar una redacción anterior. |
| **Volver a este punto** | La parte vuelve a la versión elegida, y **todas las versiones más recientes pasan a la papelera** del historial (se te pide confirmación y se indica cuántas son). Pueden sacarse de la papelera otra vez. Úsala para deshacer toda una serie de cambios. |

Ambas acciones cambian la parte como lo haría una edición: los [resúmenes](summaries.md) que describen el texto antiguo quedan desactualizados, y
Marginalia indica cuántos son, y la parte cuenta como editada. La historia muestra el texto restaurado de inmediato.

<a id="naming-a-version"></a>

## Dar nombre a una versión

Una versión con nombre es una **versión guardada**, y el historial nunca la elimina por sí solo.

- **Guardar versión** en el diálogo guarda el texto que tiene la parte ahora con el nombre que le des.
- **Etiqueta…** pone un nombre a una versión existente, o lo cambia o lo quita.

Úsala antes de probar algo arriesgado: *el borrador que le gustó al editor*, *antes de que se recortara el duelo*.

<a id="deleting-versions"></a>

## Eliminar versiones

- **Eliminar** envía una versión a la **papelera del historial**. Marca **Mostrar las versiones eliminadas** para ver la papelera (las filas aparecen en gris);
  **Sacar de la papelera** devuelve una versión a su lugar en la lista, y **Delete permanently** elimina una para siempre.
  La versión más reciente no se puede eliminar: es el texto de la parte.
- **Vaciar la papelera** elimina para siempre todo lo que hay en la papelera.
- **Borrar el historial** elimina para siempre todas las versiones de la parte excepto la actual. El diálogo indica cuántas versiones se van y
  cuánto espacio ocupan. **Conservar las versiones guardadas y la primera versión generada** (marcada al principio) conserva las que hayas nombrado
  y lo primero que escribió el modelo para la parte.

![Lo que va a eliminar Borrar el historial](../../images/history-clear.png)

El menú de ajustes (⚙) de la historia tiene lo mismo para todo el libro: **Borrar el historial del libro** y **Vaciar the papelera del historial**.

![El menú de ajustes de la historia con las acciones del historial del libro](../../images/history-menu.png)

<a id="how-many-versions-are-kept"></a>

## Cuántas versiones se conservan

Una parte conserva **50 versiones**, salvo que un límite indique otra cosa. Cuando una nueva versión supondría una de más, el historial elimina
primero la versión más antigua de la papelera, y después la versión más antigua que no esté en la papelera. Las versiones en la papelera
cuentan para el límite hasta que se eliminan definitivamente. Tres versiones nunca se eliminan: la más reciente, las que tienen nombre y
la primera que generó el modelo para la parte (tampoco cuentan para el límite).

El límite puede fijarse en cuatro niveles, y **gana el más estricto**:

| Nivel | Dónde |
|---|---|
| El libro | **Versiones conservadas por parte** en la pestaña [Acerca de](managing-books.md#the-about-tab) del libro. |
| Las carpetas | **Versiones conservadas por parte** en los [ajustes de la carpeta](folders.md#folder-settings); se revisan todas las carpetas por encima del libro. |
| Tú | **Versiones conservadas por parte** en la pestaña [Settings](../account.md). |
| El administrador | El [permiso](../administration/permissions.md) **Historial de versiones**, un techo para el usuario. |

Un campo vacío significa que ese nivel no fija ningún límite. **0** no conserva historial alguno (las versiones existentes se mantienen hasta que las borres,
pero no se añade ninguna nueva). El campo muestra, como pista, el límite que hereda y qué nivel lo fija,
por ejemplo *Limitado a 20 por la carpeta "Novelas"*. Un libro no puede subir un límite fijado por encima de él.

<a id="searching-the-old-text"></a>

## Buscar en el texto antiguo

Las búsquedas principales solo miran el texto actual, así que una frase antigua que cambiaste no hace que una parte aparezca. Para encontrarla,
marca **Incluir versiones** (desmarcada al principio, y no se recuerda, para que una búsqueda nunca te sorprenda con texto antiguo):

- en la [búsqueda avanzada de libros](managing-books.md#advanced-search): un libro se encuentra cuando una versión anterior de
  una de sus partes contiene las palabras;
- en la búsqueda de la [vista de ramas](branches-and-story-tree.md#searching-all-branches): una parte que tiene las palabras solo en una versión anterior
  se marca, el resultado dice *Encontrado en la versión del 12 oct 2026 14:05 (Generada)*, y **Mostrar en el historial** abre el historial de la
  parte en esa versión.

![Una parte encontrada a través de una versión anterior](../../images/history-search.png)

Un agente puede hacer lo mismo con el comando `search-parts` y su opción `includeVersions`; cada resultado indica entonces de qué versión
procede. Las versiones de la papelera no se buscan, y **Buscar y reemplazar** nunca cambia una versión.

<a id="backups-copies-and-the-trash"></a>

## Copias de seguridad, copias y la papelera

- Una [copia de seguridad del libro](backups.md#the-history-of-the-parts) puede incluir el historial (**Incluir el historial de versiones**,
  desmarcada al principio, porque puede ser grande). Restaurar una copia de este tipo, como libro o como copia, devuelve las versiones con sus nombres
  y la papelera. Una copia sin él deja una primera versión por parte.
- Un [duplicado de un libro](managing-books.md#cloning-a-book) empieza con su propio historial: una versión por parte, a partir del texto con el que se copió.
- Eliminar una parte o un libro se lleva consigo sus versiones; están en la [papelera](../trash.md) mientras lo esté la parte, y se
  eliminan para siempre con ella.
- Combinar una parte con la anterior elimina el historial de la parte que se combina; el texto aparece en el historial
  de la parte que queda como una versión **Combinada**.
<a id="books-from-before-the-history"></a>

## Libros anteriores a la historia

Cuando Marginalia se actualizó, cada parte que ya existía recibió sus primeras versiones: una parte que escribió un modelo empieza con una versión **Generada** del texto tal como lo escribió el modelo (con su razonamiento y el modelo usado), y tiene una versión **Autor** si la editaste después; cualquier otra parte empieza con una versión **Autor**. Las ediciones anteriores de esas partes no se conocen y no aparecen en la lista.
