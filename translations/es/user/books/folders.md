---
label: Carpetas
order: 98
---

# Carpetas

Las carpetas ordenan tus libros igual que las carpetas de un disco ordenan los archivos. Una carpeta contiene libros y otras carpetas, con la profundidad que quieras, y la lista de libros muestra una carpeta cada vez. Una carpeta también puede contener **ajustes** (idioma, punto de vista, tiempo verbal, estilo, las plantillas y los prompts): los libros que contiene, y los de las carpetas que hay debajo, escriben con ellos a menos que tengan los suyos propios. Ese es el motivo principal para usar carpetas: define el estilo de una serie una sola vez, y todos los libros de la serie lo tendrán.

![La pestaña Books con carpetas](../../images/books-folders.png)

<a id="moving-around"></a>

## Desplazarse

Encima de los libros, la lista muestra las carpetas de la actual, en el orden de las columnas ordenadas. Haz clic en el **nombre** de una carpeta para abrirla. La ruta de botones encima de la lista (`Books / Sci-fi / Expanse`) indica dónde estás; haz clic en una parte de ella para volver a ese punto.

La casilla **Incluir subcarpetas** muestra todos los libros que hay debajo de la carpeta actual, a cualquier profundidad (en el nivel superior, son todos tus libros), y añade una columna **Carpeta** con la ubicación de cada uno. La carpeta actual y la casilla se recuerdan, también al recargar la página.

Una fila de carpeta muestra cuántos libros hay debajo de ella, a cualquier profundidad. Una fila de libro muestra las palabras y los tokens de su [rama activa](branches-and-story-tree.md). Los filtros **Nombre** y **Etiquetas** funcionan con carpetas y con libros (las carpetas tienen etiquetas, véase [Etiquetar carpetas](#tagging-folders)), en la carpeta actual o debajo de ella con *Incluir subcarpetas*.

La **Última modificación** de una carpeta sigue a sus libros: cuando cambia un libro que contiene, o uno de los de las carpetas que hay debajo (o una de sus partes), la carpeta también se actualiza. Ordena por esa columna para ver dónde escribiste por última vez. Abrir un libro o moverlo no cuenta.

<a id="what-the-buttons-do"></a>

### Qué hacen los botones

| Botón | |
|---|---|
| **Nueva carpeta** (encabezado) | Crea una carpeta dentro de la actual. El nombre no puede estar vacío, tener más de 255 caracteres ni contener un `/`, y no puede ser el nombre de otra carpeta en el mismo lugar (no importan las mayúsculas). |
| el engranaje de una carpeta | Los [ajustes de la carpeta](#folder-settings). |
| el lápiz de una carpeta | La renombra. |
| la papelera de una carpeta | La elimina, véase [Eliminar una carpeta](#deleting-a-folder). |
| **Mover a la carpeta** (de un libro) | Elige la carpeta en la que colocar el libro. |
| la estrella de un libro | Lo convierte en [favorito](#favorites). |
| **Añadir libro**, las importaciones | El nuevo libro va a la **carpeta actual**. Un [clon](managing-books.md#cloning-a-book) se coloca junto a su original. |

<a id="folder-settings"></a>

## Ajustes de la carpeta

El engranaje de una carpeta abre sus ajustes (la ventana **Organizar** tiene el mismo botón, tecla `i`):

![Los ajustes de una carpeta](../../images/folder-settings.png)

- **Nombre**, **Descripción** y **Etiquetas**.
- **Idioma**, y los mismos campos que la pestaña [Prompts](prompts.md) de un libro: plantilla maestra, punto de vista, tiempo verbal, estilo y los prompts.
- **Versiones conservadas por parte**: cuántas versiones anteriores de una parte conserva el [historial](version-history.md#how-many-versions-are-kept) para los libros de la carpeta y para las carpetas que hay debajo. De todos los límites que afectan a un libro, se aplica el más estricto.
- **Ajustes de la extensión**, cuando una [extensión](../extensions/index.md) cargada tiene ajustes que heredan los libros; véase [Ajustes de las extensiones](prompts.md#settings-of-extensions).

Todo es opcional. Un campo vacío se *hereda*, y así se indica: el texto gris del campo es el valor que hereda, y la línea debajo indica de dónde procede. Pulsa **Guardar** para guardar la carpeta; el nombre y la descripción se pueden cambiar en cualquier momento, pero un cambio de idioma o de un ajuste de texto se rechaza mientras un agente trabaja en un libro de la carpeta o de las que hay debajo: el mensaje indica el libro; [pausa o detén su ejecución](../agents.md) primero.

<a id="where-the-settings-of-a-book-come-from"></a>

### De dónde vienen los ajustes de un libro

Un libro usa, para cada uno de estos ajustes, el primero de los siguientes que no esté vacío:

1. el **libro** en sí (sus pestañas *Información* y *Prompts*),
2. la **carpeta** en la que está el libro,
3. la carpeta superior a esa, y así sucesivamente hasta el nivel superior,
4. tus **ajustes de usuario** (*Settings → Templates*, véase [Cuenta y ajustes](../account.md)),
5. el **valor predeterminado integrado**.

Un texto en blanco cuenta como no definido. Esto se busca cuando hace falta (al generar una parte, al crear un resumen, al reescribir una parte...); no se copia nada en el libro al meterlo en una carpeta: **mover un libro a otra carpeta cambia la forma en que escribe**, y cambiar un ajuste de una carpeta modifica todos los libros que hay debajo y que no tienen ajuste propio.

La pestaña [Prompts](prompts.md#inherited-settings) de un libro lo muestra: bajo cada campo aparece de dónde procede su valor (*Inherited from the folder "Sci-fi"*, *Heredado de tus ajustes* o *Valor predeterminado integrado*).

<a id="language"></a>

### Idioma

El cuadro **Idioma** de un libro (la pestaña [Información](managing-books.md#the-about-tab)) y el de una carpeta pueden dejarse vacíos, lo que significa *heredar*: el libro toma el idioma de la carpeta más cercana por encima de él que defina uno, y si ninguna lo define, inglés. Un cuadro vacío muestra qué idioma es ese y de dónde procede. El cuadro tiene un botón para borrarlo; al borrarlo se vuelve a heredar (antes se convertía en inglés). Todo lo que muestra o usa el idioma de un libro usa el que tiene con sus carpetas: la separación silábica del texto, la exportación y la tabla [Mass Change Language](#mass-changes).

<a id="deleting-a-folder"></a>

## Eliminar una carpeta

El botón de la papelera de una carpeta pregunta qué hacer:

- **una carpeta vacía** se elimina tras una confirmación;
- **una carpeta con libros o carpetas dentro** ofrece **Mover su contenido a la carpeta superior** (la opción predeterminada) o **Delete everything in it as well**. La segunda opción vuelve a preguntar, indicando el número de libros y carpetas, y envía los libros a la [Trash](../trash.md), desde donde puedes recuperarlos. La propia carpeta eliminada no aparece en la Trash.

Si una carpeta interior tiene el nombre de una carpeta de la carpeta superior, se rechaza mover el contenido hacia arriba (los nombres son únicos): cambia antes el nombre de una de ellas. Una carpeta con un libro en el que trabaja un agente no se puede eliminar ni mover, porque los ajustes de ese libro cambiarían durante la ejecución.
<a id="organise"></a>

## Organizar

**Organizar** (en el menú **Gestionar** de la cabecera de la lista) abre una ventana con dos paneles, cada uno con una carpeta, para mover y copiar libros y carpetas de uno a otro sin abrir cada uno. Se abre con el panel izquierdo en la carpeta en la que estabas y el derecho en la que mostraba la última vez.

![La ventana Organizar](../../images/organise-dialog.png)

| Tecla | |
|---|---|
| Izquierda / Derecha / Tab | El otro panel. |
| Arriba / Abajo | El cursor. |
| Enter | Abre la carpeta bajo el cursor. |
| Retroceso | Sube un nivel. |
| Espacio o Insert | Marca la entrada bajo el cursor; Mayús + Arriba / Abajo marca mientras te desplazas. |
| Clic en una casilla | Marca o desmarca la entrada; **Mayús + clic** marca (o desmarca) todas las entradas entre ella y la casilla pulsada antes. |

Una fila de botones en la parte inferior actúa sobre lo que está **marcado** (o, si no hay nada marcado, sobre lo que está bajo el cursor), y muestra su tecla entre corchetes:

| Botón | Tecla | |
|---|---|---|
| **Copiar** | `c` | Copia en la carpeta del *otro* panel. |
| **Mover** | `m` | Mueve a la carpeta del otro panel. |
| **Nueva carpeta** | `n` | Crea una carpeta en la carpeta del panel activo. |
| **Renombrar** | `Shift+r` | Renombra la carpeta o el libro bajo el cursor. |
| **Eliminar** | `Delete` | Lo elimina, como en la lista. |
| **Ajustes de la carpeta** | `i` | Los [ajustes](#folder-settings) de la carpeta bajo el cursor. |
| **Cambio masivo de modelo/protocolo**, **Cambio masivo de idioma**, **Cambio de nombre masivo**, **Etiquetar libros** | `p`, `l`, `Shift+n`, `t` | Los [cambios masivos](#mass-changes) para los libros marcados, o para los libros de la carpeta del panel activo (con su propia opción *Incluir subcarpetas*). Los paneles muestran los nombres nuevos tras un cambio de nombre masivo. |
| **Búsqueda avanzada** | `f` | La [búsqueda](managing-books.md#advanced-search) en la carpeta del panel activo, opcionalmente con *Incluir subcarpetas*. Un resultado tiene **Mostrar en la carpeta**, que lleva el panel hasta allí y marca el libro. |
| **Mostrar la carpeta en la lista** | `o` | Cierra la ventana y abre la carpeta del panel activo en la lista de libros. |

Al cerrar la ventana, la lista de libros se vuelve a leer, así que lo que hayas hecho, movido, copiado, renombrado o eliminado se muestra de inmediato (si la carpeta en la que estaba la lista ya no existe, se pasa al nivel superior).

Las teclas son acciones de los [atajos de teclado](../account.md#keyboard-shortcuts) de *Ajustes*: cámbialas o bórralas allí (no hay teclas de función por defecto, porque un Mac y los navegadores las reservan). Los botones muestran la tecla que hayas configurado.

<a id="copying"></a>

### Copiar

Una **copia** de un libro es un [clon](managing-books.md#cloning-a-book); la ventana pregunta si quieres tomar solo la rama actual. Una copia de una carpeta es una carpeta nueva con los ajustes, la descripción y las etiquetas del original, y copias de todos los libros y carpetas que contiene. Una copia puede tardar mucho, así que se ejecuta con una barra de progreso que no se puede detener. Si falla a mitad, se conservan las copias hechas antes, cada una completa.

Un **movimiento** se hace al instante y puede deshacerse moviendo de vuelta. Una carpeta no puede moverse dentro de sí misma ni dentro de una carpeta que esté por debajo de ella.

<a id="when-a-name-is-taken"></a>

### Cuando un nombre ya está en uso

Cuando el destino ya tiene una carpeta con el mismo nombre, la ventana pregunta qué hacer con esa carpeta, con **Aplicar a todos** para el resto:

| Opción | |
|---|---|
| **Renombrar** | La carpeta recibe otro nombre. |
| **Omitir** | Se deja donde está (y no se copia). |
| **Combinar** | Lo que contiene se pasa a la carpeta que ya existe; las carpetas con el mismo nombre dentro se combinan a su vez. |

Los libros nunca chocan: sus nombres pueden repetirse.

<a id="mass-changes"></a>

## Cambios masivos

El menú **Gestionar → Cambios masivos** de la lista (y los botones de Organizar) cambia muchos libros a la vez. **Cambio masivo de modelo/protocolo** pone un modelo y/o un protocolo a los libros, **Cambio masivo de idioma** un idioma, **Cambio de nombre masivo** añade un prefijo o un sufijo a sus nombres, reemplaza un texto en ellos o los numera, y **Etiquetar libros** y **Etiquetar carpetas** añaden o quitan una etiqueta. Véase [Gestionar libros](managing-books.md#tagging-many-books-at-once). Desde la lista actúan sobre los libros de la carpeta actual, con su propia opción **Incluir subcarpetas**.

<a id="favorites"></a>

## Favoritos

La estrella de una fila de libro lo convierte en un **favorito**. Mientras tengas alguno, el nivel superior muestra la carpeta virtual **Favoritos** (una estrella) con los libros favoritos de todas tus carpetas. Es solo una vista: no puedes crear libros ni carpetas en ella, y quitar la estrella elimina el libro de ella, no de su carpeta. Los favoritos se guardan en tus ajustes.

<a id="tagging-folders"></a>

## Etiquetar carpetas

Las carpetas tienen etiquetas, igual que los libros. **Gestionar → Cambios masivos → Etiquetar carpetas** etiqueta muchas carpetas a la vez. Las etiquetas de una carpeta no cambian las etiquetas de sus libros, y no forman parte de una [copia de seguridad](backups.md).

<a id="folders-and-other-features"></a>

## Carpetas y otras funciones

- **Copias de seguridad.** Una [copia de seguridad](backups.md#backups-and-folders) recuerda las carpetas de su libro, y un archivo exportado puede incluir los ajustes que el libro hereda.
- **Agentes.** Un [agente](../agents.md#agents-and-folders) escribe con los ajustes de las carpetas de su libro.
- **Asistente de ayuda.** Si lo permites, el [asistente](../administration/help-assistant.md#your-books) puede consultar tus libros y carpetas.
- **Teléfono.** La [vista de teléfono](../phone.md#books) muestra las mismas carpetas como tarjetas y permite crearlas, renombrarlas, configurarlas y eliminarlas; Organizar y los cambios masivos están en el menú **⋮** de su cabecera.
- **Visor.** El [visor](publishing.md#folders-in-the-list) también lista las carpetas y se abre en la que dejaste.
