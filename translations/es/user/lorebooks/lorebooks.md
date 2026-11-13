---
label: Creación de lorebooks
order: 100
---

# Creación de lorebooks

<a id="the-lorebook-editor"></a>

## El editor de lorebooks

Abre la pestaña **Lorebooks**. El editor muestra un lorebook a la vez; elígelo en el campo **Lorebook** de la parte superior.
Los lorebooks pertenecen a tu cuenta: otros usuarios no los ven, a menos que [compartas](#sharing-a-lorebook) uno.

La barra de herramientas tiene dos filas, que se dividen en más líneas en una ventana estrecha: la primera para los lorebooks (de **Lorebook** a **Tag lorebooks**), y la segunda para las entradas del lorebook seleccionado (de **Add Entry** a **Refresh**).

| Botón | |
|---|---|
| **Lorebook** | Selecciona el lorebook que quieres editar. |
| **Only root lorebooks** | Muestra solo los lorebooks que no son sublorebook de otro (el lorebook seleccionado sigue en la lista). |
| **Add Lorebook** | Crea un lorebook nuevo llamado *New Lorebook* y lo selecciona. |
| Clone (icono de dos hojas) | Hace una copia del lorebook seleccionado: pide el nombre (el del original con *(Copy)*) y selecciona la copia. La copia tiene las mismas entradas (con sus ajustes, etiquetas y etiquetas negativas), etiquetas, descripción y datos de extensiones. Sus sublorebooks no se copian: la copia usa los mismos. El original no se modifica. |
| **Delete Lorebook** | Elimina el lorebook seleccionado (tras confirmarlo). |
| Import (icono de archivo) | **Import Marginalia Lorebook**, **Import from SillyTavern**, **Import from archive** (un zip de archivos `.md`) o **Import from other user** (un lorebook que otro usuario [comparte](#sharing-a-lorebook)); véase [Importación y exportación](import-export.md). |
| Export (icono de archivo) | Descarga el lorebook con sus sublorebooks; véase [Importación y exportación](import-export.md#exporting). |
| Tags (icono de etiquetas) | **Tag lorebooks**: pone una etiqueta a muchos lorebooks a la vez; véase [más abajo](#tagging-many-at-once). No se muestra para un lorebook que está abierto en un libro. |
| Description (icono de líneas, el último de la primera fila) | **Edit description**: abre una ventana para escribir de qué trata el lorebook; véase [más abajo](#description). |
| **Add Entry** | Añade una entrada al lorebook; véase [Entradas y activación](entries.md). |
| **Full-screen editor** | Abre las entradas del lorebook en una ventana a pantalla completa con pestañas; véase [Edición a pantalla completa](#editing-in-full-screen). Solo en escritorio. |
| **Search and replace** | Cambia un texto en el nombre, el texto y el comentario de las entradas del lorebook; véase [Buscar y reemplazar](entries.md#search-and-replace). |
| **Mass edit** | Abre todas las entradas del lorebook y de sus sublorebooks en una sola tabla; véase [Edición masiva](#mass-edit). |
| Tags (icono de etiquetas, a la derecha de **Search and replace**, segunda fila) | **Tag entries**: pone una etiqueta a muchas entradas del lorebook seleccionado a la vez; véase [más abajo](#tagging-many-at-once). |
| **Preview activation** | Muestra qué entradas usaría un turno con un texto que escribas, y por qué; véase [Vista previa de la activación](entries.md#preview-activation). |
| **Refresh** | Vuelve a cargar las entradas. |
| Cuadro de búsqueda | Muestra solo las entradas que contienen el texto; véase [Buscar y eliminar muchas entradas](entries.md#searching-and-deleting-many-entries). |
| **Delete selected** | Aparece cuando hay entradas seleccionadas en la lista; las elimina tras confirmarlo. |

Debajo de la barra de herramientas están los ajustes del lorebook seleccionado:

| Campo | |
|---|---|
| **Name** | El nombre del lorebook, que se muestra cuando lo eliges para un libro o como sublorebook. |
| **Sub Lorebooks** | Otros lorebooks cuyas entradas se incluyen con este; véase [más abajo](#sub-lorebooks). Haz clic en el nombre de un sublorebook seleccionado para abrirlo. |
| **Tags** | Etiquetas de todo el lorebook; cuentan como etiquetas de cada una de sus entradas; véase [Etiquetas](entries.md#tags). |
| **Scan depth** | De `0` a `5`: cuántas veces se busca en el contenido de las entradas usadas los filtros de las demás entradas; véase [Entradas traídas por otras entradas](entries.md#entries-brought-in-by-other-entries). Se aplica a las entradas del lorebook que usa un libro y a las de sus sublorebooks, salvo que un sublorebook tenga su propia profundidad (fila siguiente). |
| **Scan depth as a sub lorebook** | Vacío, o de `0` a `5`: la profundidad de análisis de las entradas de este lorebook mientras es sublorebook del que usa un libro. Vacío significa la profundidad de análisis de ese lorebook; `0`, que las entradas de este lorebook nunca son encontradas por el contenido de otras. |
| **Enabled** | Si se desmarca, el lorebook no aporta nada: ni sus entradas ni las de sus sublorebooks. |
| **Share with other users** | Si se marca, otros usuarios pueden ver el lorebook e importar una copia; véase [más abajo](#sharing-a-lorebook). Desactivado en un lorebook nuevo. |

Debajo de los ajustes está la [descripción](#description) del lorebook, si tiene una.

El resto del editor es la lista de [entradas](entries.md). Todos los cambios se guardan a medida que los haces.

### Description

La descripción son unas pocas palabras (o líneas) sobre el lorebook: qué mundo es, para qué sirve, qué libro lo usa. Haz clic en el icono de líneas de la primera fila de la barra de herramientas, escribe la descripción en la ventana y confirma; un texto vacío la elimina. Un lorebook que tiene descripción la muestra en una línea debajo de sus ajustes, cortada con puntos suspensivos cuando no cabe; apoya el puntero del ratón en la línea para leerla completa. La descripción se copia con el lorebook (clonado, exportación, importación, copias de seguridad) y es el texto que otros usuarios ven junto a su nombre cuando lo compartes.

![La ventana de la descripción de un lorebook](../../images/lorebook-description.png)

### Sharing a lorebook

Marca **Share with other users** y cada otro usuario de esta instalación verá el lorebook en **Import from other user** (el botón de importación de la primera fila de la barra de herramientas), con su nombre, su autor (tu nombre completo, o tu login si no tienes uno) y su descripción. Pueden abrir una fila para leer las entradas (y las entradas de los sublorebooks) e importar una copia. Desmárcala y el lorebook desaparece de su lista; las copias que ya hayan hecho siguen siendo suyas.

* Compartir es solo lectura. Nadie más puede cambiar, eliminar o usar tu lorebook en un libro; tu lorebook sigue siendo tuyo y lo ves como antes.
* Una importación crea **lorebooks nuevos del otro usuario**: el lorebook y sus sublorebooks **compartidos**, por profundos que sean, con sus entradas, etiquetas y descripción, enlazados como están los tuyos. Las copias son privadas (no compartidas); lo que hagas después con tu lorebook no les llega, y lo que cambien no llega al tuyo.
* **Solo se muestran y copian los sublorebooks que están compartidos por sí mismos.** Un sublorebook que no está compartido queda fuera junto con todo lo que hay debajo, incluido un lorebook compartido que esté debajo de él. Cuando marcas **Share with other users** en un lorebook que tiene sublorebooks no compartidos, una pregunta pide si compartirlos también: **Yes** comparte el lorebook y todos ellos (por profundos que sean), **No** comparte solo el lorebook, **Cancel** no comparte nada y deja la casilla desmarcada. Un sublorebook que añadas después no se comparte hasta que lo compartas tú.
* Desmarcar la casilla deja de compartir solo ese lorebook; sus sublorebooks siguen compartidos (y aparecen en la lista de los demás por su cuenta) hasta que los desmarques también.
* Los lorebooks eliminados, las copias de trabajo de los agentes y tus propios lorebooks no aparecen en la lista, y no aparece nada para un usuario que haya sido eliminado.

Debajo de los ajustes se muestra la estimación de los tokens de las entradas activadas del lorebook. El enlace **Count with subbooks** junto a ella suma
los tokens de las entradas activadas del lorebook y de todos sus sublorebooks, por profundos que sean, y muestra el total al lado. Como ocurre cuando
se escribe una historia, un sublorebook desactivado queda fuera junto con los lorebooks que hay debajo, y un lorebook que es sublorebook en más de un sitio se cuenta una vez.
El total es un recuento del momento en que haces clic: si cambias una entrada, desaparece; haz clic de nuevo para volver a contar.

Un lorebook de un libro en el que trabaja un agente se muestra de solo lectura (véase [Agentes](../agents.md)), sin **Add Entry**, **Search and replace**, **Mass edit**, **Delete Lorebook** ni los ajustes; **Add Lorebook**, importar, exportar y **Refresh** se mantienen, para que siempre puedas empezar otro lorebook.
<a id="mass-edit"></a>

## Edición masiva

**Edición masiva** abre una ventana con todas las entradas del lorebook seleccionado **y de sus sublorebooks** (a cualquier profundidad) en una única tabla,
ordenadas según el **Orden** de las entradas (las entradas con el mismo orden se agrupan por lorebook y, después, por nombre). Sirve para ordenar un lorebook grande:

- Cambia en su sitio, igual que en el editor, la casilla de *habilitada*, el **Nombre de la entrada**, el **Orden**, las **Etiquetas**, el **Modo de filtrado**, el filtrado, el **Modo de inserción**, las
  **Etiquetas negativas**, el **Contexto de activación** con sus **Partes**, el intervalo de partes (**Desde el mensaje** y **Hasta el mensaje**; vacío: sin límite) y la
  **Nota** de una entrada. Cada entrada ocupa una línea; la tabla se desplaza hacia abajo (y hacia los lados cuando la ventana es estrecha). La fila se mueve a su nueva posición cuando cambias el orden. El texto de una entrada se acorta en la tabla; abre la entrada en
  el lorebook para leer o editar todo el texto.
- La casilla **Lorebook** de una fila indica en qué lorebook está la entrada. Elige otro (el propio lorebook o cualquiera de sus sublorebooks) para mover la entrada allí.
- Para mover varias entradas, márcalas (la casilla del encabezado marca todas), elige **Mover al lorebook** en la parte inferior y pulsa **Mover los marcados**.
- Para eliminar varias entradas, márcalas y pulsa **Eliminar los marcados**; pide confirmación una vez y las envía a la [papelera](../trash.md).
- Un lorebook con el que trabaja un agente está bloqueado: sus entradas aparecen en gris y no se pueden cambiar ni mover, y no se puede mover nada hacia él.
  Esto se comprueba para cada lorebook, así que un lorebook puede estar libre mientras uno de sus sublorebooks está bloqueado (porque lo usa otro libro). **Actualizar** vuelve a leer la ventana para ver si un bloqueo ya ha desaparecido.

El lorebook del editor se actualiza al cerrar la ventana tras un cambio.

![Edición masiva de un lorebook y sus sublorebooks](../../images/lorebook-mass-edit.png)
<a id="editing-in-full-screen"></a>

## Edición a pantalla completa

**Editor a pantalla completa** (segunda fila de la barra de herramientas) abre el lorebook seleccionado en una ventana que ocupa toda la pantalla, para trabajar con entradas largas (el botón de expandir de la fila de una entrada abre la ventana en esa entrada); la pestaña *Lorebook* de la ventana de un libro tiene el mismo botón, para el lorebook que se muestra allí. La ventana lleva como título el nombre del lorebook. Es un conjunto de pestañas como las del [editor de la historia](../books/story-editor.md): arrastra una pestaña para colocarla junto a otra o debajo de ella, y arrastra los divisores entre ellas.

- La pestaña **Entradas** lista las entradas, con el cuadro de búsqueda y los filtros de etiqueta y de estado de la página del lorebook, **Añadir entrada**, **Edición masiva**, **Etiquetar entradas**, **Vista previa de la activación** y **Actualizar** (la edición masiva y las etiquetas muestran sus cambios también en las pestañas abiertas). Con **Incluir sublorebooks** lista también las entradas de los [sublorebooks](#sub-lorebooks), cada una con el nombre de su lorebook; **Añadir entrada** añade entonces la entrada al lorebook elegido junto a él, y la pestaña de una entrada de este tipo indica su lorebook. Esta pestaña no se puede cerrar. Cada fila tiene tres botones:
  - **Abrir** (icono de lápiz, también un doble clic en la fila) muestra la entrada en la pestaña de entrada que usaste por última vez o, si no hay ninguna, abre una en una columna a la derecha de la lista;
  - **Abrir en una pestaña nueva** (icono de flecha) añade una pestaña para la entrada, junto a la pestaña de entrada que usaste por última vez;
  - **Eliminar** (icono de papelera) elimina la entrada tras confirmarlo, y cierra su pestaña.

  Una entrada está abierta en una pestaña como máximo: si la abres de nuevo, se muestra la pestaña que ya tiene.
- Una **pestaña de entrada** tiene, de arriba abajo: el **Nombre**, el **Orden** y **Activado**; **Ajustes**, un panel que puedes plegar, con el resto de ajustes de la entrada (modo y filtro de filtrado, modo de inserción, contexto y partes de activación, etiquetas y etiquetas negativas, el rango de partes, las casillas de recursión y la condición; véase [Entradas y activación](entries.md)); después el **contenido** en un editor de Markdown (cada pestaña recuerda si muestra el texto, el texto y la vista previa, o solo la vista previa, también cuando se vuelve a abrir la ventana), y la **nota** al lado cuando está marcada **Mostrar la nota**. El ojo abre la [vista previa de la activación](entries.md#preview-activation) de esta entrada. La línea inferior muestra los tokens estimados del contenido y, en rojo, qué pasa con su plantilla (se guarda igualmente). Cierra una pestaña con su **×**.
- **Imágenes en las entradas.** Pega una imagen en el contenido, suéltala sobre él o elígela con el botón de imagen de la barra de herramientas (PNG, JPEG, GIF o WebP, hasta 10 MB). Se guarda con tus archivos (la pestaña *Recursos*) y el contenido recibe una línea como `![Mapa](resource:…)` donde estaba el cursor; la vista previa muestra la imagen, y el texto entre corchetes es su pie de imagen. Borra la línea para quitar la imagen de la entrada. El modelo lee la entrada sin estas líneas. Las imágenes en sí solo se envían cuando los ajustes del modelo dicen *Enviar las imágenes del lore* (véase [Proveedores de inferencia](../inference-providers.md)). Las imágenes viajan con el lorebook: un clon, una copia de un lorebook compartido, un archivo de lorebook exportado y una copia de seguridad del libro con sus archivos las incluyen, y la copia recibe imágenes propias. Las exportaciones a SillyTavern y las exportaciones de libros no las incluyen. La pestaña *Lorebook* de la historia las muestra debajo del texto de la entrada. Las imágenes todavía no cuentan en las estimaciones de tokens.
- **Saltar entre entradas.** Ctrl+clic (Cmd+clic en un Mac) sobre el nombre o una palabra clave de otra entrada en el contenido, en el texto o en la vista previa, para abrir esa entrada en la pestaña que usaste por última vez; con Shift también se abre en una pestaña nueva. La línea inferior lista además, tras **Mencionado en:**, las entradas cuyo contenido nombra esta; haz clic en una para abrirla, o Shift+clic para una pestaña nueva. Una entrada se nombra por su nombre y por las palabras de su filtro (cada palabra clave, el texto completo de un filtro *Texto*, lo que coincide con una expresión regular), como palabras completas, sin distinguir mayúsculas de minúsculas. Esto se parece a lo que sigue la [recursión](entries.md), pero no es lo mismo: la recursión también encuentra una palabra clave dentro de una palabra más larga, necesita todas las palabras clave de un filtro *AND* y comprueba las etiquetas y la condición.
- El editor de Markdown tiene una barra de herramientas (negrita, cursiva, tachado, una línea, títulos, cita, código, bloque de código, tabla y los tres tipos de listas) y, a la derecha, las vistas *editar*, *editar con vista previa* (la predeterminada) y *vista previa*. Funcionan las teclas habituales (Ctrl+B, Ctrl+I...); Tab pasa al siguiente campo en lugar de sangrar. Lo que escribes se guarda cuando dejas de escribir un momento y al salir del editor.
- La cabecera muestra los tokens estimados de las entradas activadas, **Contar con los sublibros** y **Buscar y reemplazar**, que muestra el texto reemplazado también en las pestañas abiertas.

Todos los cambios se guardan a medida que los haces, así que **Cerrar** no pierde nada. La ventana recuerda cómo la dejaste, para cada lorebook: la próxima vez que se abre con las mismas entradas en las mismas pestañas, dispuestas de la misma manera (las entradas eliminadas desde entonces no aparecen). La página del lorebook vuelve a mostrar las entradas al cerrar la ventana.

**Cambios hechos en otro lugar.** Un cambio en una pestaña se guarda en la entrada tal como está almacenada en ese momento, así que lo que se cambió en otro sitio (la página del lorebook, la edición masiva, otra ventana) en los *demás* campos de la entrada se conserva, y la pestaña lo muestra. Si el **contenido** o la **nota** se cambiaron en otro sitio desde que la pestaña los mostró, tu texto no se guarda encima: la línea inferior lo indica, con **Conservar mi texto** (guardar el tuyo de todos modos) y **Cargar el texto guardado** (descartar el tuyo). Una entrada eliminada en otro lugar cierra su pestaña.
La ventana también sigue lo que se guarda mientras está abierta: cuando un agente u otra ventana (otra pestaña del navegador, la pestaña de lorebook de la historia) guarda o elimina una entrada, la lista se vuelve a cargar y la pestaña de esa entrada muestra su estado actual (si la entrada se eliminó, su pestaña se cierra). Una pestaña cuyo texto espera a **Conservar mi texto** o **Cargar el texto guardado** se queda como está hasta que respondas. Un cambio guardado en el mismo segundo en que escribes puede aún cruzarse: en ese caso la línea inferior pregunta como se ha indicado.

**Agentes.** La ventana no se ofrece mientras un agente trabaja en un libro que usa el lorebook. Si una ejecución empieza mientras la ventana está abierta, la ventana pasa a solo lectura en tu próximo cambio o al cambiar de pestaña: una barra indica el agente (y dice cuándo tu último cambio no se pudo guardar). Pausa o detén la ejecución y haz clic en **Comprobar de nuevo**: las entradas se cargan tal como las dejó la ejecución y puedes volver a editar.

Las extensiones [Generador de lore](../extensions/lore-generator.md) y [VCS de lorebooks](../extensions/lorebook-vcs.md) funcionan también aquí: la varita está en las filas de la lista y en la fila del nombre de una pestaña de entrada, el panel de revisiones está encima de la lista y la barra de revisiones de una entrada está en su pestaña.

<a id="tagging-many-at-once"></a>

## Etiquetar muchos a la vez

Los dos botones de etiquetas de la barra de herramientas abren una única ventana que pone una etiqueta en muchos elementos, o se la quita: **Etiquetar lorebooks** lista tus lorebooks, **Etiquetar entradas** las entradas del lorebook seleccionado. Elige la **Etiqueta** (o escribe una nueva y pulsa Enter); la lista muestra cada elemento con sus etiquetas, y los elementos que tienen la etiqueta elegida aparecen primero, con la etiqueta resaltada. Los botones **+** y **-** de una fila añaden o quitan la etiqueta a ese elemento; marca varios elementos (la casilla de la cabecera marca todos) y usa **Aplicar la etiqueta a los marcados** o **Quitar la etiqueta de los marcados** en la parte inferior para hacerlo con todos a la vez. Un elemento en el que trabaja un agente (un lorebook de un libro con una ejecución en curso, y por tanto todas sus entradas) aparece atenuado y no se puede cambiar hasta que la ejecución se pausa o se detiene. La ventana también etiqueta [libros](../books/managing-books.md#tagging-many-books-at-once).
Las etiquetas que pone a las entradas son las habituales, véase [Etiquetas](entries.md#tags); las etiquetas negativas se establecen en la propia entrada.

![Etiquetar las entradas de un lorebook](../../images/multi-tag-entries.png)

<a id="using-a-lorebook-in-a-book"></a>

## Usar un lorebook en un libro

Abre el libro y su pestaña **Lorebook**, y elige el lorebook en el campo **Lorebook**. El editor de abajo muestra el lorebook, así que puedes añadir y cambiar entradas mientras escribes. Vacía el campo para escribir sin lorebook.

Un libro usa un único lorebook. Para usar varios, crea un lorebook que los incluya como sublorebooks, y usa ese.

El mismo lorebook puede usarlo cualquier número de libros. Los cambios en él afectan a todos ellos a partir de la siguiente parte generada; las partes ya escritas conservan el lore con el que se escribieron.
<a id="sub-lorebooks"></a>

## Sublorebooks

**Sublorebooks** incluye las entradas de otros lorebooks dentro de este. Una configuración típica:

```
"Saga" (usado por los libros de la serie)
 ├─ "World"       lugares, historia, reglas de la magia
 ├─ "Characters"  el reparto principal
 └─ "Notas del libro 2"
```

- Los sublorebooks pueden tener sublorebooks propios; se incluyen todos los niveles.
- Un lorebook puede ser sublorebook de varios lorebooks. Cuando se llega a él más de una vez, sus entradas se usan
  solo una vez.
- Un lorebook no puede incluirse a sí mismo, directa o indirectamente a través de otros lorebooks: esa selección se rechaza
  con *No se puede seleccionar el sublorebook: se ha detectado un ciclo.*
- Un sublorebook desactivado se deja fuera junto con todo lo que incluye.

![Un lorebook con dos sublorebooks: los tokens con y sin ellos](../../images/lorebook-sub-lorebooks.png)

Las entradas de todos los lorebooks incluidos se juntan y se ordenan por su [orden](entries.md#order-and-insertion),
independientemente del lorebook del que procedan.

<a id="disabling-and-deleting"></a>

## Desactivar y eliminar

Desmarca **Activado** para desactivar un lorebook durante un tiempo, por ejemplo para ver cómo se lee un libro sin él. Los libros
siguen usándolo, y vuelve a funcionar al marcarlo de nuevo.

**Eliminar lorebook** quita el lorebook de tus listas. Los libros que lo usaban ya no reciben sus entradas, y los lorebooks
que lo incluían como sublorebook lo omiten. Los datos permanecen en la base de datos hasta que un administrador ejecute
[Limpieza](../administration/cleanup.md); hasta entonces puedes restaurar el lorebook desde la [Papelera](../trash.md). Las copias en las [copias de seguridad de libros](../books/backups.md) y en los archivos exportados no se ven afectadas.
