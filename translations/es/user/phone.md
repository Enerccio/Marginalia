---
label: Usar Marginalia en un teléfono
order: 985
icon: device-mobile
---

# Usar Marginalia en un teléfono

Marginalia tiene una disposición pensada para los teléfonos. Puedes escribir, editar y regenerar partes, editar lorebooks, gestionar tus modelos y (como administrador) la instalación desde el navegador del teléfono. La disposición para teléfono **no es una copia de la de escritorio**: las mismas cosas se distribuyen en una sola columna, algunas ventanas son más sencillas y no todas las extensiones funcionan en ella. Esta página enumera las diferencias.

Para leer un libro en un teléfono también existe el [visor](books/publishing.md), que está pensado para ello.

<a id="when-the-phone-layout-is-used"></a>

## Cuándo se usa la disposición para teléfono

Marginalia mira el ancho de la ventana del navegador **cuando se carga la página**. Un ancho menor de 768 píxeles activa la disposición para teléfono; cualquier ancho mayor activa el [espacio de trabajo](index.md#the-workspace) normal.

- Las tabletas y los teléfonos en horizontal suelen tener más de 768 píxeles de ancho y reciben la disposición de escritorio.
- La disposición **no cambia mientras trabajas**. Si giras el teléfono o la tableta, o cambias el tamaño de la ventana, la disposición se mantiene hasta que **recargues la página**. Así que, para obtener la disposición de escritorio más amplia en una tableta o en un teléfono girado, gíralo y recarga; para volver a la disposición de teléfono, vuelve a girarlo y recarga de nuevo.
- Una ventana de navegador de escritorio estrecha también recibe la disposición para teléfono, lo que resulta útil para probarla.

Como en el escritorio, la disposición para teléfono se puede añadir a la pantalla de inicio como una aplicación (*Instalar app* / *Añadir a pantalla de inicio* en el menú del navegador).

<a id="the-workspace"></a>

## El espacio de trabajo

Las pestañas de la izquierda se sustituyen por un **cajón**. Toca el botón de menú de la barra superior para abrirlo; toca una pestaña para cambiar a ella. La pestaña en la que estás ocupa toda la pantalla y su nombre aparece en la barra superior.

![El espacio de trabajo del teléfono con el cajón abierto](../images/phone-workspace-drawer.png)

El cajón tiene las mismas pestañas que el escritorio —*Libros*, *Lorebooks*, *Ajustes*, *Protocolos*, *Proveedores de inferencia*, *Agentes* (solo para los usuarios que pueden usar [agentes](agents.md#on-a-phone)), *Recursos* y *Papelera*— y, para un administrador, *Administración* (el botón del escritorio es aquí una pestaña). En la parte inferior están el selector de **Idioma**, **Cambiar la contraseña** y **Cerrar sesión**. Las extensiones pueden añadir botones, pero solo en los lugares que conocen en el teléfono; véase [Extensiones](#extensions).

Mientras la pestaña *Ajustes* tenga cambios sin guardar, el cajón se bloquea como en el escritorio: guarda o descarta primero.

<a id="books"></a>

### Libros

La lista de libros es una columna. Encima están el filtro por nombre, el filtro por etiquetas y los botones de orden (*Última modificación*, *Creado*, *Nombre*; toca el activo de nuevo para invertir el orden), luego **Añadir libro**, la lupa (la [búsqueda avanzada](books/managing-books.md#advanced-search): etiquetas para incluir y excluir, fechas, número de partes y texto) y actualizar. Cada libro tiene botones: la estrella lo convierte en favorito, **Mover a la carpeta** lo coloca en una [carpeta](books/folders.md), el lápiz lo abre y la papelera lo elimina (a la [Papelera](trash.md)).

![La lista de libros en un teléfono](../images/phone-book-list.png)

La lista muestra una [carpeta](books/folders.md) cada vez, como en el escritorio: la ruta de la carpeta en la que estás, como botones para volver atrás, la casilla **Incluir subcarpetas**, después las carpetas por encima de los libros (toca una para entrar; muestra cuántos libros hay debajo) y, en el nivel superior, la carpeta virtual **Favoritos**. Los filtros de nombre y de etiquetas buscan en la carpeta actual, o en todo lo que hay debajo con *Incluir subcarpetas*. **Añadir libro** y las importaciones colocan el nuevo libro en la carpeta en la que estás. La carpeta en la que estás se recuerda.

![Carpetas en un teléfono](../images/phone-folders.png)

![El menú de la lista de libros](../images/phone-books-menu.png)

Una carpeta se crea con el botón **Nueva carpeta** de la cabecera, y se renombra, se configura (descripción, etiquetas, idioma, modelo y protocolo para sus libros) o se elimina desde el botón **⋮** de su fila. El botón **⋮** de la cabecera contiene las ventanas que actúan sobre muchos libros: **Organizar** (los dos paneles de carpetas quedan uno encima de otro en un teléfono; no hay indicación de teclado), **Etiquetar libros**, **Etiquetar carpetas** y los cambios masivos de [carpetas](books/folders.md) (modelo y protocolo, idioma, renombrar). El idioma de un libro puede heredarse también aquí desde una carpeta, y la pestaña *Prompts* muestra de dónde viene cada valor.

### Lorebooks

El teléfono tiene el **editor de lorebooks completo** del escritorio, dispuesto en una columna. El lorebook se elige en un cuadro; el botón **⋮** junto a él abre el menú del lorebook: **Añadir lorebook**, **Clonar**, **Eliminar**, **Importar** (un archivo de lorebook, un archivo de SillyTavern, un archivo zip de archivos Markdown, un lorebook que otro usuario comparte), **Exportar** (el lorebook o el formato de SillyTavern), **Etiquetar lorebooks** y **Editar la descripción**. Debajo de la casilla *Activado*, del recuento de tokens y de los botones **Buscar y reemplazar** y la **vista previa de activación**, la sección **Ajustes** se abre con el nombre, los sub lorebooks, las etiquetas, la profundidad de análisis y *Compartir con otros usuarios* del lorebook. Véase [Lorebooks](lorebooks/index.md).

Debajo están **Añadir entrada** (y el **⋮** con la edición masiva y las etiquetas de muchas entradas), el cuadro de búsqueda y las entradas. Una entrada se abre con su casilla de activación y su nombre en una fila; abierta, tiene un formulario: nombre, orden, etiquetas, modo de filtrado y texto, modo de inserción, etiquetas negativas, el contexto de activación y su rango, *Buscar también en los resúmenes*, *Evitar recursión*, *Solo a través de otras entradas*, el contenido, la nota y **Eliminar entrada**. Todo lo que se cambia se guarda al momento, como en el escritorio. Debajo del cuadro de búsqueda están los cuadros de etiquetas y de estado que limitan la lista; **Seleccionar entradas** en el menú **⋮** pone una casilla delante de cada entrada y muestra **Eliminar la selección** para las marcadas. Lo que no está aquí: la casilla *Solo lorebooks raíz*.

La pestaña **Lorebook** del cajón de la historia muestra el lore del libro como la pestaña del escritorio: un cuadro de búsqueda, las entradas que se abren a su contenido y el lápiz que edita el texto de una entrada.

El mismo editor se usa en la pestaña *Lorebook* de un libro. Cuando el lorebook del libro tiene sub lorebooks, el cuadro **Lorebook mostrado** que aparece debajo cambia la pestaña a cualquiera de ellos, y de vuelta, sin cambiar el lorebook del libro. Mientras un [agente](agents.md) trabaja en un libro que usa el lorebook, el lorebook se muestra como texto y nada de su contenido se puede cambiar.

![Un lorebook en un teléfono](../images/phone-lorebook.png)

![El menú del lorebook](../images/phone-lorebook-menu.png)

![Los ajustes del lorebook](../images/phone-lorebook-settings.png)

![Una entrada abierta en su formulario, con las revisiones del Lorebook VCS encima](../images/phone-lorebook-entry.png)

El botón junto a la casilla *Activado* abre **Buscar y reemplazar** para las entradas del lorebook; véase [Entradas](lorebooks/entries.md#search-and-replace).

![Buscar y reemplazar en un lorebook en un teléfono](../images/phone-lorebook-search-replace.png)

<a id="settings-protocols-providers-resources-trash"></a>

### Ajustes, protocolos, proveedores, recursos, papelera

- Los **Ajustes** son los mismos que en el escritorio, dispuestos en una columna estrecha; **Importar copia de seguridad como libro nuevo** es el botón situado encima de *Descartar los cambios*.
- **Protocolos** y **Proveedores de inferencia** muestran cada entrada como texto con los botones junto a ella (debajo, en el caso de los proveedores, que tienen más). Los diálogos de edición son los del escritorio, así que un protocolo se puede [compartir](protocols.md#sharing-a-protocol) desde el teléfono, y el botón **Importar de otro usuario** encima de la lista importa un protocolo que otro usuario comparte.
- **Recursos** agrupa los archivos según el objeto que los usa (abre un grupo con su flecha). Cada archivo es una tarjeta (una imagen, el nombre, el tipo, el tamaño, la fecha y los botones para descargar, reemplazar y eliminar), con una casilla para *Eliminar la selección*; marcar un grupo elimina todos sus archivos.
- **Papelera** muestra cada objeto eliminado como una tarjeta (nombre, tipo, cuándo se eliminó). La explicación de la papelera está tras el botón *i*; los botones de los filtros pasan a una segunda línea. La restauración funciona como se describe en [Papelera](trash.md).

![Los recursos en un teléfono](../images/phone-resources.png)

![Los proveedores de inferencia en un teléfono, con el botón de información de un proveedor compartido](../images/phone-providers.png)

![La papelera en un teléfono](../images/phone-trash.png)
<a id="the-book-window"></a>

## La ventana del libro

Al abrir un libro se muestra a **pantalla completa**. El botón para cerrar es la **✕** de la esquina superior, y las pestañas (*Información*, *Prompts*, *Lorebook*, *Historia*, *Vista de ramas*, *Copias de seguridad*) forman una sola fila que se desplaza en horizontal. Funcionan igual que en el escritorio (véase [Libros](books/index.md)), con sus campos en una sola columna.

<a id="the-story"></a>

### La historia

La pestaña de historia es una sola columna de partes. Las diferencias respecto al [editor de historia](books/story-editor.md):

![La historia de un libro en un teléfono](../images/phone-book-story.png)

- El **esquema** (y las pestañas que las extensiones añaden junto a él, como la [Nota del autor](extensions/authors-note.md)) está en un **cajón lateral**. Se abre con el botón de la esquina superior izquierda.
- El botón **Resúmenes** y el menú de ajustes (⚙, con *Cambiar los estilos del texto*, *Buscar y reemplazar*, *Importar*, *Eliminación masiva* y *Exportar la historia*) están en la barra superior, no abajo.
- El botón **+** para una parte nueva está abajo.
- La ventana **Resúmenes** tiene su barra de menús (*Buscar y reemplazar*, *Create summaries...*, *Volver a resumir todos los resúmenes*, *Exportar resúmenes*, *Importar resúmenes*) en una fila propia; en una pantalla estrecha los elementos están detrás del botón **⋯**. [Importar resúmenes](books/summaries.md#export-and-import-of-the-summaries) es la misma ventana que en el escritorio, con el ancho de la pantalla.

![Importar resúmenes en un teléfono](../images/phone-summaries-import.png)

- [Anotaciones](books/annotations.md): la pestaña **Anotaciones** está en el cajón lateral, junto al esquema; un toque en un pasaje subrayado abre su nota; **Anotar la parte** y **Anotar con IA** están en el menú de la parte, y **Anotar la selección** en el menú de una pulsación larga sobre el texto seleccionado.
- **Buscar y reemplazar** abre la misma ventana que en el escritorio, con los campos y botones uno debajo de otro:

  ![Buscar y reemplazar en la historia en un teléfono](../images/phone-search-replace.png)

- Una parte es solo su texto, bajo una línea pequeña con dos iconos. El primero es el **menú** de la parte (editar, regenerar, deslizar, ramificar, historial, mostrar prompt, imágenes, resumen, eliminar); el segundo abre una **ventana emergente** con lo que el escritorio muestra en la columna de la derecha de una parte: los identificadores, el modelo, el protocolo, los tokens, las palabras y los detalles del turno.
- **Historial** en el menú de una parte abre el [historial de versiones](books/version-history.md) como página a pantalla completa: las versiones son tarjetas encima de la versión seleccionada, con las mismas acciones que en el escritorio.

  ![El historial de versiones de una parte en un teléfono](../images/phone-history.png)

- **Estadísticas** está en el menú de ajustes de la historia (las estadísticas del libro) y como botón en la cabecera de la lista de libros (todos los libros). La ventana es una página a pantalla completa; su calendario muestra las últimas 16 semanas (un año en el escritorio), véase [Estadísticas de escritura](books/statistics.md).

  ![Las estadísticas de todos los libros en un teléfono](../images/phone-statistics.png)

- El resumen de los [resúmenes](books/summaries.md) mantiene un ancho fijo y se desplaza en horizontal.

<a id="administration"></a>

## Administración

Un administrador tiene la pestaña **Administración** en el cajón lateral, con las mismas pestañas que la [página de escritorio](administration/index.md) en una fila que se desplaza en horizontal: *Usuarios*, *Copias de seguridad de la base de datos*, *Limpieza*, *Archivos de recursos*, *Asistente de ayuda*, *Agentes*, *Actualizar* y *Extensiones*. Las listas (usuarios, copias de seguridad, extensiones) son tarjetas con sus botones debajo del texto, y las barras de herramientas pasan a una segunda línea. Instalar una extensión (*Load Extension*) y subir una copia de seguridad de la base de datos abren el selector de archivos del teléfono. Los demás usuarios no ven la pestaña.

![Los usuarios de la pestaña Administración en un teléfono](../images/phone-admin-users.png)

![Las copias de seguridad de la base de datos en un teléfono](../images/phone-admin-backups.png)

<a id="what-is-missing"></a>

## Lo que falta

El teléfono hace lo que hace el escritorio, con estas diferencias:

| Ausente o distinto en el teléfono | Dónde hacerlo |
|---|---|
| La casilla *Solo lorebooks raíz* y la selección de muchas entradas de lorebook para borrarlas a la vez | Diseño de escritorio. |
| La barra de comandos, los atajos de teclado, la vista de diferencias de una parte regenerada y las notas al margen | Diseño de escritorio; necesitan teclado o una pantalla ancha. |
| Las ventanas con muchas columnas o dos paneles ([resúmenes](books/summaries.md), la conversación de Lectores beta, el Generador de manga) son pequeñas | Funcionan; el modo horizontal es más cómodo. |

Todo lo que no aparece en esta lista funciona igual que en el escritorio: generar, editar, regenerar, deslizar y ramificar partes, resúmenes y metarresúmenes, prompts del libro, el árbol de ramas, copias de seguridad del libro, exportación, recursos, la papelera, lorebooks y la administración.

<a id="extensions"></a>

## Extensiones

La disposición del teléfono tiene sus propias clases, así que una extensión tiene que soportarla explícitamente. Las extensiones que vienen con Marginalia:

| Extensión | En el teléfono |
|---|---|
| [Esquema de arcos](extensions/arc-outline.md) | Funciona. La pestaña Arcos está en el cajón del esquema. |
| [Nota del autor](extensions/authors-note.md) | Funciona. La nota está en el cajón del esquema de la historia. |
| [Lectores beta](extensions/beta-readers.md) | Funciona. La pestaña Lectores beta está en el cajón del esquema; el espacio para la conversación es pequeño, el modo horizontal es más cómodo. |
| [Generador de descripción del libro](extensions/book-description-generator.md) | Funciona. El botón está al final de la pestaña Información; el diálogo muestra las opciones encima del resultado. |
| [Marcador de capítulos](extensions/chapter-marker.md) | Funciona. Los títulos de capítulo están en el cajón del esquema. |
| [Seguidor de personajes](extensions/character-tracker.md) | Funciona. La pestaña Personajes está en el cajón del esquema. |
| [Notas de la historia](extensions/story-notes.md) | Funciona. La pestaña Notas está en el cajón del esquema; el diálogo de página cabe en la pantalla, y una página larga se escribe mejor en horizontal. |
| [Traductor de lenguas construidas](extensions/conlang-translator.md) | En parte. El panel de *Settings → Extension Settings*, el editor, la revisión y el asistente funcionan (la cuadrícula del léxico se desplaza en horizontal), y también el toque sobre un diálogo traducido en el visor; el lado de la historia (**Translate dialogs**, **Translate parts (constructed language)**, el clic en la historia) no se probó en un teléfono, véase [Límites](extensions/conlang-translator.md#limits). |
| [Conversor](extensions/sillytavern-converter.md) | Funciona. La pestaña Conversor está en el cajón del esquema; el rango del chat se elige con dos toques, véase [En un teléfono](extensions/sillytavern-converter.md#on-a-phone). |
| [Exportador de instrucciones](extensions/instruction-exporter.md) | Funciona. **Export instructions** está en el menú de ajustes de la historia. |
| [Generador de manga](extensions/manga-generator.md) | Funciona. **Manga Generator** está en el menú de ajustes de la historia; su ventana es grande, así que el modo horizontal es más cómodo. |
| [Revisor](extensions/reviewer.md) | Funciona. Sus elementos están en el menú de una parte, y sus ajustes en *Settings → Extension Settings*. |
| [Consulta lateral](extensions/side-query.md) | Funciona. La pestaña Side Query está en el cajón del esquema. |
| [Generador de lore](extensions/lore-generator.md) | Funciona. El botón con la varita está en la fila de cada entrada del lorebook; su ventana muestra el prompt encima del resultado. |
| [VCS del lorebook](extensions/lorebook-vcs.md) | Funciona. El panel del lorebook está debajo de su fila *Activado*; las revisiones de una entrada están en la parte superior de su formulario. |
| [Generador de imágenes](extensions/image-generator.md) | Funciona. **Generate image** está en el menú de una parte. |

Las extensiones de otras fuentes pueden comportarse de otra manera o no añadir nada en el teléfono; aparecen donde su autor las haya hecho funcionar. Si una extensión parece faltar, compruébala en la disposición de escritorio. Cómo una extensión soporta el teléfono se describe en el [manual para desarrolladores](../developer/plugins/extendable.md#the-phone-twins).

<a id="tips"></a>

## Consejos

- Escribir un texto largo en el teléfono es más fácil en modo horizontal. La disposición sigue siendo la del teléfono después del turno, hasta que recargues la página.
- Si necesitas algo que en el teléfono no está, gira el teléfono y recarga, o abre Marginalia en una ventana más ancha; tus libros y ajustes son los mismos en todas partes.
- Para acceder a Marginalia desde un teléfono tiene que funcionar como [servidor](getting-started/docker-server.md): la [aplicación de escritorio](getting-started/desktop-app.md) solo escucha en tu ordenador.
