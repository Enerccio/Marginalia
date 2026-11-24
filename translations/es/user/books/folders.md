---
label: Carpetas
order: 98
---

# Carpetas

Las carpetas ordenan tus libros como las carpetas de un disco ordenan los archivos. Una carpeta contiene libros y otras carpetas, tan anidadas como quieras, y la lista de libros muestra una carpeta a la vez. Una carpeta también puede contener **ajustes** (idioma, punto de vista, tiempo verbal, estilo, las plantillas y las instrucciones): los libros que contiene, y los de las carpetas que hay debajo, escriben con ellos salvo que tengan los suyos propios. Ese es el motivo principal para usar carpetas: defines el estilo de una serie una sola vez, y todos los libros de la serie lo tendrán.

![La pestaña Libros con carpetas](../../images/books-folders.png)

<a id="moving-around"></a>

## Moverse

Encima de los libros, la lista muestra las carpetas de la actual, en el orden de las columnas ordenadas. Haz clic en el **nombre** de una carpeta para abrirla. La ruta de botones encima de la lista (`Books / Sci-fi / Expanse`) muestra dónde estás; haz clic en una parte de ella para volver a ese punto.

La casilla **Include subfolders** lista todos los libros que hay debajo de la carpeta actual, a cualquier profundidad (en el nivel superior, eso son todos tus libros), y añade una columna **Folder** con el lugar de cada uno. La carpeta actual y la casilla se recuerdan, también al recargar la página.

La fila de una carpeta muestra cuántos libros hay debajo de ella, a cualquier profundidad. La fila de un libro muestra las palabras y los tokens de su [rama activa](branches-and-story-tree.md). Los filtros **Name** y **Tags** funcionan tanto con carpetas como con libros (las carpetas tienen etiquetas, véase [Etiquetado de carpetas](#tagging-folders)), en la carpeta actual, o debajo de ella con *Include subfolders*.

La columna **Last Modified** de una carpeta sigue a sus libros: cuando cambia un libro que contiene, o uno de los de una carpeta de debajo (o una de sus partes), la carpeta también se actualiza. Ordena por ella para ver dónde escribiste por última vez. Abrir un libro o moverlo no cuenta.

<a id="what-the-buttons-do"></a>

### Qué hacen los botones

| Botón | |
|---|---|
| **New folder** (header) | Crea una carpeta en la actual. El nombre no puede estar vacío, tener más de 255 caracteres ni contener un `/`, y no puede ser el nombre de otra carpeta en el mismo lugar (no importan las mayúsculas). |
| el engranaje de una carpeta | Los [ajustes de la carpeta](#folder-settings). |
| el lápiz de una carpeta | La renombra. |
| la papelera de una carpeta | La borra, véase [Borrar una carpeta](#deleting-a-folder). |
| **Move to folder** (un libro) | Elige la carpeta donde poner el libro. |
| la estrella de un libro | Lo convierte en [favorito](#favorites). |
| **Add Book**, las importaciones | El nuevo libro va a la **carpeta actual**. Un [clon](managing-books.md#cloning-a-book) se pone junto a su original. |

<a id="folder-settings"></a>

## Ajustes de una carpeta

El engranaje de una carpeta abre sus ajustes (la ventana **Organise** tiene el mismo botón, tecla `i`):

![Los ajustes de una carpeta](../../images/folder-settings.png)

- **Name**, **Description** y **Tags**.
- **Language**, y los mismos campos que la pestaña [Prompts](prompts.md) de un libro: plantilla maestra, punto de vista, tiempo verbal, estilo y las instrucciones.
- **Versions kept per part**: cuántas versiones anteriores de una parte guarda el [historial](version-history.md#how-many-versions-are-kept) para los libros de la carpeta y las carpetas de debajo. De todos los límites de arriba de un libro se aplica el más estricto.
- **Extension Settings**, cuando una [extensión](../extensions/index.md) cargada tiene ajustes que heredan los libros; véase [Ajustes de extensiones](prompts.md#settings-of-extensions).
- **Phrase watch list**: las frases que se buscan en los libros de la carpeta, añadidas a las de tus ajustes y a las de las carpetas superiores; véase [Vigilar frases](annotations.md#watching-for-phrases).

Todo es opcional. Un campo vacío se *hereda*, y se indica: el texto gris del campo es el valor que hereda, y la línea debajo indica de dónde viene. Pulsa **Save** para guardar la carpeta; el nombre y la descripción se pueden cambiar en cualquier momento, pero un cambio de idioma o de un ajuste de texto se rechaza mientras un agente trabaja en un libro de la carpeta o de debajo: el mensaje indica el libro; [pausa o detén su ejecución](../agents.md) primero.

<a id="where-the-settings-of-a-book-come-from"></a>

### De dónde vienen los ajustes de un libro

Un libro usa, para cada uno de estos ajustes, el primero de estos que no esté vacío:

1. el **libro** en sí (sus pestañas *About* y *Prompts*),
2. la **carpeta** en la que está el libro,
3. la carpeta encima de esa, y así sucesivamente hasta la raíz,
4. tus **ajustes de usuario** (*Settings → Templates*, véase [Cuenta y ajustes](../account.md)),
5. el **valor predeterminado** integrado.

Un texto en blanco cuenta como no establecido. Esto se busca cuando hace falta (al generar una parte, hacer un resumen, rehacer una parte...); no se copia nada en el libro al meterlo en una carpeta: **mover un libro a otra carpeta cambia cómo escribe**, y cambiar un ajuste de una carpeta cambia cada libro de debajo que no tenga el suyo propio.

La pestaña [Prompts](prompts.md#inherited-settings) de un libro lo muestra: debajo de cada campo está de dónde viene su valor (*Inherited from the folder "Sci-fi"*, *Inherited from your settings* o *Built-in default*).

<a id="language"></a>

### Idioma

El cuadro **Language** de un libro (pestaña [About](managing-books.md#the-about-tab)) y de una carpeta puede dejarse vacío, lo que significa *heredar*: el libro toma el idioma de la carpeta más cercana por encima que establezca uno, y si no, inglés. Un cuadro vacío muestra qué idioma es ese y de dónde viene. El cuadro tiene un botón de borrar; vaciarlo vuelve a heredar (antes se convertía en inglés). Todo lo que muestra o usa el idioma de un libro usa el que tiene con sus carpetas: la separación silábica del texto, la exportación y la tabla de [Mass Change Language](#mass-changes).

<a id="deleting-a-folder"></a>

## Borrar una carpeta

El botón de papelera de una carpeta pregunta qué hacer:

- una **carpeta vacía** se borra tras una confirmación;
- una **carpeta con libros o carpetas dentro** ofrece **Move its content to the folder above** (la opción por defecto) o **Delete everything in it as well**. La segunda vuelve a preguntar, con el número de libros y carpetas, y envía los libros a la [Papelera](../trash.md), desde donde puedes recuperarlos. La carpeta borrada en sí no aparece en la Papelera.

Si una carpeta interior tiene el nombre de una carpeta de la superior, mover el contenido hacia arriba se rechaza (los nombres son únicos): renombra primero una de ellas. Una carpeta con un libro en el que trabaja un agente no se puede borrar ni mover, porque los ajustes de ese libro cambiarían durante la ejecución.
## Organise

**Organise** (en el menú **Manage** de la cabecera de la lista) abre una ventana con dos paneles, cada uno con una carpeta, para mover y copiar libros y carpetas de uno a otro sin abrir cada uno. Se abre con el panel izquierdo en la carpeta en la que estabas y el derecho en la que mostró la última vez.

![La ventana Organise](../../images/organise-dialog.png)

| Tecla | |
|---|---|
| Left / Right / Tab | El otro panel. |
| Up / Down | El cursor. |
| Enter | Abre la carpeta bajo el cursor. |
| Backspace | Sube un nivel. |
| Space or Insert | Marca la entrada bajo el cursor; Shift + Up / Down marca mientras se mueve. |
| Clic en una casilla | Marca o desmarca la entrada; **Shift + clic** marca (o desmarca) cada entrada entre ella y la casilla pulsada antes. |

Una fila de botones en la parte inferior actúa sobre lo que está **marcado** (o, si no hay nada marcado, sobre lo que está bajo el cursor), y muestra su tecla entre corchetes:

| Botón | Tecla | |
|---|---|---|
| **Copy** | `c` | Copia en la carpeta del *otro* panel. |
| **Move** | `m` | Mueve a la carpeta del otro panel. |
| **New folder** | `n` | Crea una carpeta en la carpeta del panel activo. |
| **Rename** | `Shift+r` | Renombra la carpeta o el libro bajo el cursor. |
| **Delete** | `Delete` | Lo borra, como en la lista. |
| **Folder settings** | `i` | La [configuración](#folder-settings) de la carpeta bajo el cursor. |
| **Mass Change Model/Protocol**, **Mass Change Language**, **Mass Rename**, **Tag books** | `p`, `l`, `Shift+n`, `t` | Los [cambios masivos](#mass-changes) para los libros marcados, o para los libros de la carpeta del panel activo (con su propia *Include subfolders*). Los paneles muestran los nuevos nombres tras un renombrado masivo. |
| **Advanced search** | `f` | La [búsqueda](managing-books.md#advanced-search) en la carpeta del panel activo, opcionalmente con *Include subfolders*. Un resultado tiene **Show in folder**, que lleva el panel allí y marca el libro. |
| **Show folder in the list** | `o` | Cierra la ventana y abre la carpeta del panel activo en la lista de libros. |

Al cerrar la ventana se vuelve a leer la lista de libros, así que lo que hiciste, moviste, copiaste, renombraste o borraste se ve al instante (si la carpeta en la que estaba la lista ya no existe, la lista pasa al nivel superior).

Las teclas son acciones de los [atajos de teclado](../account.md#keyboard-shortcuts) en *Settings*: cámbialas o bórralas allí (no hay teclas de función por defecto, porque un Mac y los navegadores las reservan). Los botones muestran la tecla que hayas configurado.

<a id="copying"></a>

### Copiar

Una **copia** de un libro es un [clon](managing-books.md#cloning-a-book); la ventana pregunta si quieres copiar solo la rama actual. Una copia de una carpeta es una carpeta nueva con la configuración, la descripción y las etiquetas del original, y con copias de todos los libros y carpetas que contiene. Una copia puede tardar, así que se ejecuta con una barra de progreso que no se puede detener. Si falla a mitad, se conservan las copias hechas antes, cada una completa.

Un **movimiento** se hace al instante y puede deshacerse moviendo de vuelta. Una carpeta no se puede mover dentro de sí misma ni dentro de una carpeta que esté debajo de ella.

<a id="when-a-name-is-taken"></a>

### Cuando un nombre ya existe

Cuando el destino ya tiene una carpeta con el mismo nombre, la ventana pregunta qué hacer con esa carpeta, con **Apply to all** para aplicar la misma decisión al resto:

| Opción | |
|---|---|
| **Rename** | La carpeta recibe otro nombre. |
| **Skip** | Se deja donde está (y no se copia). |
| **Merge** | Lo que contiene pasa a la carpeta que ya existe; las carpetas con el mismo nombre dentro de ella se fusionan a su vez. |

Los libros nunca chocan: sus nombres pueden repetirse.

<a id="mass-changes"></a>

## Cambios masivos

El menú **Manage → Mass changes** de la lista (y los botones de Organise) cambia muchos libros a la vez. **Mass Change Model/Protocol** pone un modelo y/o un protocolo en los libros, **Mass Change Language** pone un idioma, **Mass Rename** añade un prefijo o un sufijo a sus nombres, reemplaza un texto en ellos o los numera, y **Tag books** y **Tag folders** añaden o quitan una etiqueta. Véase [Gestión de libros](managing-books.md#tagging-many-books-at-once). Desde la lista trabajan sobre los libros de la carpeta actual, con su propia opción **Include subfolders**.

<a id="favorites"></a>

## Favoritos

La estrella de la fila de un libro lo convierte en un **favorito**. Mientras tengas alguno, el nivel superior muestra la carpeta virtual **Favorites** (con una estrella) con los libros favoritos de todas tus carpetas. Es solo una vista: no puedes crear un libro ni una carpeta en ella, y quitar la estrella elimina el libro de la vista, no de su carpeta. Los favoritos se guardan en tu configuración.

<a id="tagging-folders"></a>

## Etiquetar carpetas

Las carpetas tienen etiquetas, igual que los libros. **Manage → Mass changes → Tag folders** etiqueta muchas carpetas a la vez. Las etiquetas de una carpeta no cambian las etiquetas de sus libros, y no forman parte de una [copia de seguridad](backups.md).

<a id="folders-and-other-features"></a>

## Carpetas y otras funciones

- **Copias de seguridad.** Una [copia de seguridad](backups.md#backups-and-folders) recuerda las carpetas de su libro, y un archivo exportado puede incluir la configuración que hereda el libro.
- **Agentes.** Un [agente](../agents.md#agents-and-folders) escribe con la configuración de las carpetas de su libro.
- **Asistente de ayuda.** Si lo permites, el [asistente](../administration/help-assistant.md#your-books) puede buscar tus libros y carpetas.
- **Teléfono.** El [diseño para teléfono](../phone.md#books) muestra las mismas carpetas como tarjetas y permite crearlas, renombrarlas, configurarlas y borrarlas; Organise y los cambios masivos están en el menú **⋮** de su cabecera.
- **Visor.** El [visor](publishing.md#folders-in-the-list) también lista las carpetas y se abre en la que dejaste.
