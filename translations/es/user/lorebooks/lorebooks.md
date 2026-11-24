---
label: Crear lorebooks
order: 100
---

# Crear lorebooks

<a id="the-lorebook-editor"></a>

## El editor de lorebooks

Abre la pestaña **Lorebooks**. El editor muestra un lorebook a la vez; elígelo en el campo **Lorebook** de la parte superior.
Los lorebooks pertenecen a tu cuenta: otros usuarios no los ven, a menos que [compartas](#sharing-a-lorebook) uno.

La barra de herramientas tiene dos filas, que se ajustan en más líneas en una ventana estrecha: la primera para los lorebooks (de **Lorebook** a **Tag lorebooks**), la segunda para las entradas del lorebook seleccionado (de **Add Entry** a **Refresh**).

| Botón | |
|---|---|
| **Lorebook** | Selecciona el lorebook que quieres editar. |
| **Only root lorebooks** | Muestra solo los lorebooks que no son un sub lorebook de otro (el lorebook seleccionado permanece en la lista). |
| **Add Lorebook** | Crea un lorebook nuevo llamado *New Lorebook* y lo selecciona. |
| Clone (icono de dos hojas) | Hace una copia del lorebook seleccionado: pide el nombre (el del original con *(Copy)*) y selecciona la copia. La copia tiene las mismas entradas (con sus ajustes, etiquetas y etiquetas negativas), etiquetas, descripción y datos de extensiones. Sus sub lorebooks no se copian: la copia usa los mismos. El original no se modifica. |
| **Delete Lorebook** | Elimina el lorebook seleccionado (tras confirmarlo). |
| Import (icono de archivo) | **Import Marginalia Lorebook**, **Import from SillyTavern**, **Import from archive** (un zip de archivos `.md`) o **Import from other user** (un lorebook que otro usuario [comparte](#sharing-a-lorebook)); consulta [Importar y exportar](import-export.md). |
| Export (icono de archivo) | Descarga el lorebook con sus sub lorebooks; consulta [Importar y exportar](import-export.md#exporting). |
| Tags (icono de etiquetas) | **Tag lorebooks**: pone una etiqueta en muchos lorebooks a la vez; consulta [más abajo](#tagging-many-at-once). No se muestra para un lorebook que está abierto en un libro. |
| Description (icono de líneas, último de la primera fila) | **Edit description**: abre una ventana para escribir de qué trata el lorebook; consulta [más abajo](#description). |
| **Add Entry** | Añade una entrada al lorebook; consulta [Entradas y activación](entries.md). |
| **Full-screen editor** | Abre las entradas del lorebook en una ventana a pantalla completa con pestañas; consulta [Editar a pantalla completa](#editing-in-full-screen). Solo en escritorio. |
| **Search and replace** | Cambia un texto en el nombre, el texto y el comentario de las entradas del lorebook; consulta [Buscar y reemplazar](entries.md#search-and-replace). |
| **Mass edit** | Abre todas las entradas del lorebook y de sus sub lorebooks en una sola tabla; consulta [Edición masiva](#mass-edit). |
| Tags (icono de etiquetas, a la derecha de **Search and replace**, segunda fila) | **Tag entries**: pone una etiqueta en muchas entradas del lorebook seleccionado a la vez; consulta [más abajo](#tagging-many-at-once). |
| **Preview activation** | Muestra qué entradas usaría un turno con un texto que escribas y por qué; consulta [Vista previa de activación](entries.md#preview-activation). |
| **Refresh** | Vuelve a cargar las entradas. |
| Cuadro de búsqueda | Muestra solo las entradas que contienen el texto; consulta [Buscar y eliminar muchas entradas](entries.md#searching-and-deleting-many-entries). |
| **Delete selected** | Aparece cuando hay entradas seleccionadas en la lista; las elimina tras confirmarlo. |

Debajo de la barra de herramientas están los ajustes del lorebook seleccionado:

| Campo | |
|---|---|
| **Name** | El nombre del lorebook, que se muestra al elegirlo para un libro o como sub lorebook. |
| **Sub Lorebooks** | Otros lorebooks cuyas entradas se incluyen con este; consulta [más abajo](#sub-lorebooks). Haz clic en el nombre de un sub lorebook seleccionado para abrirlo. |
| **Tags** | Etiquetas de todo el lorebook; cuentan como etiquetas de cada una de sus entradas; consulta [Etiquetas](entries.md#tags). |
| **Scan depth** | `0` a `5`: cuántas veces se busca en el contenido de las entradas usadas los filtros de las demás entradas; consulta [Entradas traídas por otras entradas](entries.md#entries-brought-in-by-other-entries). Cuenta para las entradas del lorebook que usa un libro y para las de sus sub lorebooks, salvo que un sub lorebook tenga su propia profundidad (fila siguiente). |
| **Scan depth as a sub lorebook** | Vacío, o `0` a `5`: la profundidad de búsqueda de las entradas de este lorebook mientras es sub lorebook del que usa un libro. Vacío significa la profundidad de búsqueda de ese lorebook; `0` significa que el contenido de otras entradas nunca encuentra las entradas de este lorebook. |
| **Enabled** | Si no está marcado, el lorebook no aporta nada: ni sus entradas ni las de sus sub lorebooks. |
| **Share with other users** | Si está marcado, otros usuarios pueden ver el lorebook e importar una copia; consulta [más abajo](#sharing-a-lorebook). Desactivado en un lorebook nuevo. |

Bajo los ajustes está la [descripción](#description) del lorebook, cuando tiene una.

El resto del editor es la lista de [entradas](entries.md). Todos los cambios se guardan a medida que los haces.

<a id="description"></a>

### Descripción

La descripción son unas pocas palabras (o líneas) sobre el lorebook: qué mundo es, para qué sirve, qué libro lo usa. Haz clic en el icono de líneas de la primera fila de la barra de herramientas, escribe en la ventana y confirma; un texto vacío la elimina. Un lorebook que tiene descripción la muestra en una línea bajo sus ajustes, cortada con puntos suspensivos donde no cabe; apoya el puntero del ratón sobre la línea para leerla completa. La descripción se copia con el lorebook (clonar, exportar, importar, copias de seguridad) y es el texto que otros usuarios ven junto a su nombre cuando lo compartes.

![La ventana de la descripción de un lorebook](../../images/lorebook-description.png)

<a id="sharing-a-lorebook"></a>

### Compartir un lorebook

Marca **Share with other users** y todos los demás usuarios de esta instalación verán el lorebook en **Import from other user** (el botón de importar de la primera fila de la barra de herramientas), con su nombre, su autor (tu nombre completo, o tu login si no tienes uno) y su descripción. Pueden abrir una fila para leer las entradas (y las de los sub lorebooks) e importar una copia. Desmárcalo y el lorebook desaparece de su lista; las copias que ya hicieron siguen siendo suyas.

* Compartir es solo lectura. Nadie más puede cambiar, eliminar ni usar tu lorebook en un libro; tu lorebook sigue siendo tuyo y lo ves como antes.
* Una importación crea **lorebooks nuevos del otro usuario**: el lorebook y sus sub lorebooks **compartidos**, a cualquier profundidad, con sus entradas, etiquetas y descripción, enlazados como están los tuyos. Las copias son privadas (no compartidas); lo que hagas después con tu lorebook no llega a ellas, y lo que cambien no llega al tuyo.
* **Solo se muestran y se copian los sub lorebooks que están compartidos.** Un sub lorebook que no está compartido se deja fuera junto con todo lo que hay debajo de él, también un lorebook compartido que esté debajo. Cuando marcas **Share with other users** en un lorebook que tiene sub lorebooks no compartidos, una pregunta te pide si también los compartes: **Yes** comparte el lorebook y todos ellos (a cualquier profundidad), **No** comparte solo el lorebook, **Cancel** no comparte nada y deja la casilla desmarcada. Un sub lorebook que añadas más tarde no se comparte hasta que lo compartas tú.
* Desmarcar la casilla deja de compartir solo ese lorebook; sus sub lorebooks siguen compartidos (y aparecen en la lista de los demás por su cuenta) hasta que los desmarques también.
* Los lorebooks eliminados, las copias de trabajo de los agentes y tus propios lorebooks no aparecen en la lista, y no aparece nada de un usuario que haya sido eliminado.

Bajo los ajustes se muestra la estimación de los tokens de las entradas activadas del lorebook. El enlace **Count with subbooks** junto a ella suma
los tokens de las entradas activadas del lorebook y de todos sus sub lorebooks, a cualquier profundidad, y muestra el total al lado.
Como cuando se escribe una historia, un sub lorebook desactivado se deja fuera junto con los lorebooks que hay debajo, y un lorebook que es sub lorebook en más de un sitio cuenta una sola vez.
El total es un recuento del momento en que haces clic: si cambias una entrada desaparece, y vuelve a hacer clic para contar otra vez.

Un lorebook de un libro en el que trabaja un agente se muestra de solo lectura (consulta [Agentes](../agents.md)), sin **Add Entry**, **Search and replace**, **Mass edit**, **Delete Lorebook** ni los ajustes; **Add Lorebook**, importar, exportar y **Refresh** siguen disponibles, para que siempre puedas empezar otro lorebook.
<a id="mass-edit"></a>

## Edición masiva

**Mass edit** abre una ventana con todas las entradas del lorebook seleccionado **y de sus sub lorebooks** (a cualquier profundidad) en una sola tabla, ordenadas según el **Order** de las entradas (las entradas con el mismo order se ordenan por lorebook y luego por nombre). Sirve para ordenar un lorebook grande:

- Cambiar la casilla *enabled*, el **Entry Name**, el **Order**, los **Tags**, el **Filtering Mode**, el **Filtering**, el **Insertion Mode**, los **Negative Tags**, el **Activation Context** con sus **Parts**, el rango de partes (**From message** y **To message**; vacío: sin límite) y la **Note** de una entrada directamente, como en el editor. Cada entrada ocupa una línea; la tabla se desplaza hacia abajo (y hacia los lados cuando la ventana es estrecha). La fila se mueve a su nueva posición al cambiar el orden. El texto de una entrada se acorta en la tabla; abre la entrada en el lorebook para leer o editar todo el texto.
- La casilla **Lorebook** de una fila indica en qué lorebook está la entrada. Elige otro (el propio lorebook o cualquiera de sus sub lorebooks) para mover la entrada allí.
- Para mover muchas entradas, márcalas (la casilla del encabezado marca todas), elige **Move to lorebook** en la parte inferior y pulsa **Move checked**.
- Para borrar muchas entradas, márcalas y pulsa **Delete checked**; pide confirmación una sola vez y las mueve a la [papelera](../trash.md).
- Un lorebook con el que trabaja un agente está bloqueado: sus entradas aparecen atenuadas y no se pueden cambiar ni mover, y no se puede mover nada a él. Esto se comprueba para cada lorebook, así que un lorebook puede estar libre mientras uno de sus sub lorebooks está bloqueado (lo está usando otro libro). **Refresh** vuelve a leer la ventana para comprobar si el bloqueo ha desaparecido.

El lorebook del editor se actualiza al cerrar la ventana después de haber hecho algún cambio.

![Edición masiva de un lorebook y sus sub lorebooks](../../images/lorebook-mass-edit.png)
<a id="editing-in-full-screen"></a>

## Edición en pantalla completa

El **editor a pantalla completa** (segunda fila de la barra de herramientas) abre el lorebook seleccionado en una ventana que ocupa toda la pantalla, para trabajar con entradas largas (el botón de expandir en la fila de una entrada abre la ventana en esa entrada); la pestaña *Lorebook* de la ventana de un libro tiene el mismo botón, para el lorebook que se muestra allí. La ventana lleva como título el nombre del lorebook. Es un conjunto de pestañas, como el [editor de la historia](../books/story-editor.md): arrastra una pestaña para colocarla junto a otra o debajo de ella, y arrastra los separadores entre ellas.

![El editor de lorebook a pantalla completa: la lista de entradas y dos entradas abiertas en pestañas](../../images/lorebook-fullscreen.png)

- La pestaña **Entries** lista las entradas, con el cuadro de búsqueda y los filtros de etiqueta y de estado de la página del lorebook, **Add Entry**, **Mass edit**, **Tag entries**, **Preview activation** y **Refresh** (la edición masiva y las etiquetas muestran sus cambios también en las pestañas abiertas). Con **Include sub lorebooks** lista también las entradas de los [sub lorebooks](#sub-lorebooks), cada una con el nombre de su lorebook; **Add Entry** añade entonces la entrada al lorebook elegido junto a él, y la pestaña de una entrada así indica su lorebook. Esta pestaña no se puede cerrar. Cada fila tiene tres botones:
  - **Open** (icono de lápiz, también un doble clic en la fila) muestra la entrada en la pestaña de entrada que usaste por última vez o, si no hay ninguna, abre una en una columna a la derecha de la lista;
  - **Open in new tab** (icono de flecha) añade una pestaña para la entrada, junto a la pestaña de entrada que usaste por última vez;
  - **Delete** (icono de papelera) elimina la entrada tras confirmarlo, y cierra su pestaña.

  Una entrada se abre en una sola pestaña como máximo: si la abres de nuevo, se muestra la pestaña que ya tiene.
- Una **pestaña de entrada** tiene, de arriba abajo: el **Name**, el **Order** y **Enabled**; **Settings**, un panel que puedes plegar, con el resto de ajustes de la entrada (modo y filtro de filtrado, modo de inserción, contexto y partes de activación, etiquetas y etiquetas negativas, el rango de partes, las casillas de recursión y su condición; véase [Entradas y activación](entries.md)); después el **contenido** en un editor de Markdown (cada pestaña recuerda si muestra el texto, el texto y la vista previa, o solo la vista previa, también al volver a abrir la ventana), y la **note** al lado cuando está marcada **Show note**. El ojo abre la [vista previa de activación](entries.md#preview-activation) de esta entrada. La línea inferior muestra los tokens estimados del contenido y, en rojo, lo que va mal en su plantilla (se guarda igualmente). Cierra una pestaña con su **×**.
- **Imágenes en las entradas.** Pega una imagen en el contenido, suéltala sobre él o elígela con el botón de imagen de la barra de herramientas (PNG, JPEG, GIF o WebP, hasta 10 MB). Se guarda con tus archivos (la pestaña *Resources*) y el contenido recibe una línea como `![Map](resource:…)` donde estaba el cursor; la vista previa muestra la imagen, y el texto entre corchetes es su leyenda. Borra la línea para quitar la imagen de la entrada. El modelo lee la entrada sin estas líneas. Las imágenes en sí solo se envían cuando los ajustes del modelo indican *Multimodal (accepts images)* (véase [Proveedores de inferencia](../inference-providers.md)). Las imágenes viajan con el lorebook: un clon, una copia de un lorebook compartido, un archivo de lorebook exportado y una copia de seguridad del libro con sus archivos las llevan, y la copia recibe imágenes propias. Las exportaciones a SillyTavern y las de libros no las incluyen. La pestaña *Lorebook* de la historia las muestra debajo del texto de la entrada. Las imágenes no cuentan aún en las estimaciones de tokens.
- **Saltar entre entradas.** Ctrl+clic (Cmd+clic en un Mac) sobre el nombre o una palabra clave de otra entrada, en el texto o en la vista previa, abre esa entrada en la pestaña que usaste por última vez; con Shift también, se abre en una pestaña nueva. La línea inferior lista además, tras **Mentioned in:**, las entradas cuyo contenido menciona esta; haz clic en una para abrirla, o Shift+clic para una pestaña nueva. Una entrada se identifica por su nombre y por las palabras de su filtro (cada palabra clave, el texto completo de un filtro *Text*, lo que coincide con una expresión regular), como palabras completas, con independencia de mayúsculas y minúsculas. Esto se parece a lo que sigue la [recursión](entries.md), pero no es lo mismo: la recursión encuentra también una palabra clave dentro de una palabra más larga, necesita todas las palabras clave de un filtro *AND* y comprueba las etiquetas y la condición.
- El editor de Markdown tiene una barra de herramientas (negrita, cursiva, tachado, una línea, títulos, cita, código, bloque de código, tabla y los tres tipos de listas) y, a la derecha, las vistas *edit*, *edit with preview* (la predeterminada) y *preview*. Funcionan las teclas habituales (Ctrl+B, Ctrl+I...); Tab pasa al campo siguiente en lugar de sangrar. Lo que escribes se guarda cuando dejas de escribir un momento y al salir del editor.
- La cabecera muestra los tokens estimados de las entradas activadas, **Count with subbooks** y **Search and replace**, que muestra también el texto sustituido en las pestañas abiertas.

Todos los cambios se guardan según los haces, así que **Close** no pierde nada. La ventana recuerda cómo la dejaste, para cada lorebook: la próxima vez que se abra con las mismas entradas en las mismas pestañas, ordenadas igual (se omiten las entradas borradas desde entonces). La página del lorebook muestra las entradas de nuevo al cerrar la ventana.

**Cambios hechos en otro lugar.** Un cambio en una pestaña se guarda en la entrada tal como está almacenada en ese momento, así que lo que se haya cambiado en otro sitio (la página del lorebook, la edición masiva, otra ventana) en los *demás* campos de la entrada se conserva, y la pestaña lo muestra. Si el **contenido** o la **note** se cambiaron en otro sitio desde que la pestaña los mostró, tu texto no se guarda encima: la línea inferior lo indica, con **Keep my text** (guardar el tuyo de todos modos) y **Load the saved text** (descartar el tuyo). Una entrada borrada en otro lugar cierra su pestaña.
La ventana también sigue lo que se guarda mientras está abierta: cuando un agente u otra ventana (otra pestaña del navegador, la pestaña de lorebook de la historia) guarda o borra una entrada, la lista se vuelve a cargar y la pestaña de esa entrada muestra su estado actual (la pestaña de una entrada borrada se cierra). Una pestaña cuyo texto espera a **Keep my text** o **Load the saved text** se deja como está hasta que respondas. Un cambio guardado en el mismo segundo en que escribes puede aún cruzarse: en ese caso la línea inferior pregunta como se indicó antes.

**Agentes.** La ventana no se ofrece mientras un agente trabaja en un libro que usa el lorebook. Si una ejecución empieza con la ventana abierta, la ventana pasa a solo lectura en tu próximo cambio o al cambiar de pestaña: una barra indica el agente (y si tu último cambio no se pudo guardar). Pausa o detén la ejecución y haz clic en **Check again**: las entradas se cargan tal como las dejó la ejecución y puedes volver a editar.

Las extensiones [Lore Generator](../extensions/lore-generator.md) y [Lorebook VCS](../extensions/lorebook-vcs.md) funcionan también aquí: la varita está en las filas de la lista y en la fila del nombre de una pestaña de entrada, el panel de revisiones está encima de la lista y la barra de revisión de una entrada está en su pestaña.

<a id="tagging-many-at-once"></a>

## Etiquetar muchos a la vez

Los dos botones de etiquetas de la barra de herramientas abren una sola ventana que pone una etiqueta en muchos elementos, o se la quita: **Tag lorebooks** lista tus lorebooks, **Tag entries** las entradas del lorebook seleccionado. Elige la **Tag** (o escribe una nueva y pulsa Enter); la lista muestra cada elemento con sus etiquetas, y los elementos que tienen la etiqueta elegida van primero, con la etiqueta resaltada. Los botones **+** y **-** de una fila aplican o quitan la etiqueta a ese elemento; marca varios (la casilla de la cabecera marca todos) y usa **Apply the tag to the checked** o **Remove the tag from the checked** al pie para hacerlo con todos ellos. Un elemento en el que trabaja un agente (un lorebook de un libro con un agente en marcha, y por tanto todas sus entradas) aparece atenuado y no se puede cambiar hasta que la ejecución se pausa o se detiene. La ventana también etiqueta [libros](../books/managing-books.md#tagging-many-books-at-once).
Las etiquetas que pone en las entradas son las habituales, véase [Etiquetas](entries.md#tags); las etiquetas negativas se establecen en la propia entrada.

![Etiquetar las entradas de un lorebook](../../images/multi-tag-entries.png)

<a id="using-a-lorebook-in-a-book"></a>

## Usar un lorebook en un libro

Abre el libro y su pestaña **Lorebook**, y elige el lorebook en el campo **Lorebook**. El editor de abajo muestra el lorebook, así que puedes añadir y cambiar entradas mientras escribes. Vacía el campo para escribir sin lorebook.

Un libro usa un solo lorebook. Para usar varios, crea un lorebook que los incluya como sub lorebooks, y usa ese.

Un mismo lorebook puede usarlo cualquier número de libros. Los cambios en él afectan a todos ellos desde la siguiente parte generada; las partes ya escritas conservan el lore con el que se escribieron.
<a id="sub-lorebooks"></a>

## Lorebooks secundarios

**Sub Lorebooks** incluye las entradas de otros lorebooks dentro de este. Una configuración típica:

```
"Saga" (used by the books of the series)
 ├─ "World"       places, history, rules of magic
 ├─ "Characters"  the main cast
 └─ "Book 2 notes"
```

- Los lorebooks secundarios pueden tener sus propios lorebooks secundarios; se incluyen todos los niveles.
- Un lorebook puede ser lorebook secundario de varios lorebooks. Cuando se llega a él más de una vez, sus entradas se usan solo una vez.
- Un lorebook no puede incluirse a sí mismo, directa ni indirectamente a través de otros lorebooks: esa elección se rechaza con *Cannot select sub lorebook: cycle detected.*
- Un lorebook secundario desactivado se deja fuera junto con todo lo que incluye.

![Un lorebook con dos lorebooks secundarios: los tokens con y sin ellos](../../images/lorebook-sub-lorebooks.png)

Las entradas de todos los lorebooks incluidos se juntan y se ordenan según su [orden](entries.md#order-and-insertion), sin importar de qué lorebook procedan.

<a id="disabling-and-deleting"></a>

## Desactivar y eliminar

Desmarca **Enabled** para desactivar un lorebook durante un tiempo, por ejemplo para ver cómo se lee un libro sin él. Los libros siguen usándolo, y vuelve a funcionar al marcarlo de nuevo.

**Delete Lorebook** elimina el lorebook de tus listas. Los libros que lo usaban ya no reciben sus entradas, y los lorebooks que lo incluían como lorebook secundario lo omiten. Los datos permanecen en la base de datos hasta que un administrador ejecuta la [limpieza](../administration/cleanup.md), y hasta entonces puedes restaurar el lorebook desde la [papelera](../trash.md); las copias en las [copias de seguridad de libros](../books/backups.md) y en los archivos exportados no se ven afectadas.
