---
label: Carpetas
order: 98
---

# Carpetas

Las carpetas ordenan tus libros igual que las carpetas de un disco ordenan los archivos. Una carpeta contiene libros y otras carpetas, con toda la profundidad que quieras, y la lista de libros muestra una carpeta cada vez. Una carpeta también puede contener **ajustes** (idioma, punto de vista, tiempo verbal, estilo, las plantillas y las instrucciones): los libros que hay en ella, y en las carpetas que cuelgan de ella, escriben con esos ajustes, salvo que tengan los suyos propios. Ese es el motivo principal para usar carpetas: define el estilo de una serie una sola vez, y todos los libros de la serie lo tendrán.

![La pestaña Libros con carpetas](../../images/books-folders.png)

<a id="moving-around"></a>

## Moverse por las carpetas

Encima de los libros, la lista muestra las carpetas de la carpeta actual, en el orden de las columnas por las que está ordenada. Haz clic en el **nombre** de una carpeta para abrirla. La ruta de botones encima de la lista (`Libros / Ciencia ficción / Expanse`) indica dónde estás; haz clic en una parte de ella para volver allí.

La casilla **Incluir subcarpetas** lista todos los libros que hay debajo de la carpeta actual, a cualquier profundidad (en el nivel superior, son todos tus libros), y añade una columna **Carpeta** con el lugar de cada uno. La carpeta actual y la casilla se recuerdan, también al recargar la página.

La fila de una carpeta muestra cuántos libros hay debajo de ella, a cualquier profundidad. La fila de un libro muestra las palabras y los tokens de su [rama activa](branches-and-story-tree.md). Los filtros **Nombre** y **Etiquetas** funcionan tanto en carpetas como en libros (las carpetas tienen etiquetas, véase [Etiquetar carpetas](#tagging-folders)), en la carpeta actual, o debajo de ella con *Incluir subcarpetas*.

La **Última modificación** de una carpeta sigue a sus libros: cuando cambia un libro que hay en ella, o en una carpeta que cuelga de ella (o cambia una de sus partes), la carpeta también se actualiza. Ordena por esta columna para ver dónde escribiste por última vez. Abrir un libro, o moverlo, no cuenta.

<a id="what-the-buttons-do"></a>

### Qué hacen los botones

| Botón | |
|---|---|
| **Nueva carpeta** (cabecera) | Crea una carpeta dentro de la actual. El nombre no puede estar vacío, tener más de 255 caracteres ni contener un `/`, y no puede ser el nombre de otra carpeta del mismo lugar (no importan las mayúsculas). |
| la rueda de una carpeta | Los [ajustes de la carpeta](#folder-settings). |
| el lápiz de una carpeta | La renombra. |
| la papelera de una carpeta | La elimina, véase [Eliminar una carpeta](#deleting-a-folder). |
| **Mover a la carpeta** (un libro) | Elige la carpeta en la que meter el libro. |
| la estrella de un libro | Lo convierte en [favorito](#favorites). |
| **Añadir libro**, las importaciones | El nuevo libro va a la **carpeta actual**. Un [clon](managing-books.md#cloning-a-book) se coloca junto a su original. |

<a id="folder-settings"></a>

## Ajustes de la carpeta

La rueda de una carpeta abre sus ajustes (la ventana **Organizar** tiene el mismo botón, con la tecla `i`):

![Los ajustes de una carpeta](../../images/folder-settings.png)

- **Nombre**, **Descripción** y **Etiquetas**.
- **Idioma**, y los mismos campos que la pestaña [Instrucciones](prompts.md) de un libro: plantilla maestra, punto de vista, tiempo verbal, estilo y las instrucciones.
- **Versiones conservadas por parte**: cuántas versiones anteriores de una parte conserva el [historial](version-history.md#how-many-versions-are-kept) para los libros de la carpeta y para las carpetas que cuelgan de ella. De todos los límites que afectan a un libro, se aplica el más estricto.
- **Ajustes de la extensión**, cuando una [extensión](../extensions/index.md) cargada tiene ajustes que heredan los libros; véase [Ajustes de las extensiones](prompts.md#settings-of-extensions).

Todo es opcional. Un campo vacío se *hereda*, y lo indica: el texto gris del campo es el valor que hereda, y la línea debajo indica de dónde viene. Pulsa **Guardar** para guardar la carpeta; el nombre y la descripción se pueden cambiar en cualquier momento, pero un cambio de idioma o de un ajuste de texto se rechaza mientras un agente trabaja en un libro de la carpeta o de las que cuelgan de ella: el mensaje indica el libro, así que [pausa o detén su ejecución](../agents.md) primero.

<a id="where-the-settings-of-a-book-come-from"></a>

### De dónde vienen los ajustes de un libro

Para cada uno de estos ajustes, un libro usa el primero de estos que no esté vacío:

1. el **propio libro** (sus pestañas *Acerca de* e *Instrucciones*),
2. la **carpeta** en la que está el libro,
3. la carpeta que hay encima de esa, y así sucesivamente hasta la superior,
4. tus **ajustes de usuario** (*Ajustes → Plantillas*, véase [Cuenta y ajustes](../account.md)),
5. el **valor predeterminado integrado**.

Un texto en blanco cuenta como no definido. Esto se consulta cuando hace falta (al generar una parte, al hacer un resumen, al reescribir una parte...); no se copia nada en el libro al meterlo en una carpeta: **mover un libro a otra carpeta cambia cómo escribe**, y cambiar un ajuste de una carpeta cambia cada libro que hay debajo y que no define el suyo propio.

La pestaña [Instrucciones](prompts.md#inherited-settings) de un libro lo muestra: debajo de cada campo está el origen de su valor (*Heredado de la carpeta «Ciencia ficción»*, *Heredado de tus ajustes* o *Valor predeterminado integrado*).

<a id="language"></a>

### Idioma

El cuadro **Idioma** de un libro (la pestaña [Acerca de](managing-books.md#the-about-tab)) y el de una carpeta pueden dejarse vacíos, lo que significa *heredar*: el libro toma el idioma de la carpeta más cercana por encima de él que defina uno; si no hay ninguno, inglés. Un cuadro vacío muestra qué idioma es y de dónde viene. El cuadro tiene un botón para borrarlo; al borrarlo se vuelve a heredar (antes se convertía en inglés). Todo lo que muestra o usa el idioma de un libro usa el que tiene junto con sus carpetas: la separación silábica del texto, la exportación y la tabla de [Cambio masivo de idioma](#mass-changes).

<a id="deleting-a-folder"></a>

## Eliminar una carpeta

El botón de papelera de una carpeta pregunta qué hacer:

- **una carpeta vacía** se elimina tras una confirmación;
- **una carpeta con libros o carpetas dentro** ofrece **Mover su contenido a la carpeta superior** (la opción predeterminada) o **Eliminar también todo lo que contiene**. La segunda vuelve a preguntar, con el número de libros y carpetas, y envía los libros a la [Papelera](../trash.md), desde donde puedes recuperarlos. La carpeta eliminada en sí no aparece en la Papelera.

Si una carpeta interior tiene el nombre de una carpeta de la carpeta superior, se rechaza mover el contenido hacia arriba (los nombres son únicos): renombra antes una de ellas. Una carpeta con un libro en el que trabaja un agente no se puede eliminar ni mover, porque los ajustes de ese libro cambiarían durante la ejecución.
<a id="organise"></a>

## Organizar

**Organizar** (en el menú **Gestionar** de la cabecera de la lista) abre una ventana con dos paneles, cada uno con una carpeta, para mover y copiar libros y carpetas de uno a otro sin abrir cada uno. Se abre con el panel izquierdo en la carpeta en la que estabas y el derecho en la que mostró la última vez.

![La ventana Organizar](../../images/organise-dialog.png)

| Tecla | |
|---|---|
| Izquierda / Derecha / Tab | El otro panel. |
| Arriba / Abajo | El cursor. |
| Enter | Abre la carpeta bajo el cursor. |
| Retroceso | Sube un nivel. |
| Espacio o Insert | Marca la entrada bajo el cursor; Mayús + Arriba / Abajo marca mientras se mueve. |
| Clic en una casilla | Marca o desmarca la entrada; **Mayús + clic** marca (o borra) todas las entradas entre ella y la casilla pulsada antes. |

Una fila de botones en la parte inferior actúa sobre lo que está **marcado** (o, si no hay nada marcado, sobre lo que está bajo el cursor), y muestra su tecla entre corchetes:

| Botón | Tecla | |
|---|---|---|
| **Copiar** | `c` | Copia en la carpeta del *otro* panel. |
| **Mover** | `m` | Mueve a la carpeta del otro panel. |
| **Nueva carpeta** | `n` | Crea una carpeta en la carpeta del panel activo. |
| **Renombrar** | `Shift+r` | Renombra la carpeta o libro bajo el cursor. |
| **Eliminar** | `Delete` | Lo elimina, como en la lista. |
| **Ajustes de la carpeta** | `i` | Los [ajustes](#folder-settings) de la carpeta bajo el cursor. |
| **Cambio masivo de modelo/protocolo**, **Cambio masivo de idioma**, **Cambio de nombre masivo**, **Etiquetar libros** | `p`, `l`, `Shift+n`, `t` | Los [cambios masivos](#mass-changes) para los libros marcados, o para los libros de la carpeta del panel activo (con su propia opción *Incluir subcarpetas*). Los paneles muestran los nuevos nombres tras un cambio de nombre masivo. |
| **Búsqueda avanzada** | `f` | La [búsqueda](managing-books.md#advanced-search) en la carpeta del panel activo, opcionalmente con *Incluir subcarpetas*. Un resultado tiene **Mostrar en la carpeta**, que lleva el panel hasta allí y marca el libro. |
| **Mostrar la carpeta en la lista** | `o` | Cierra la ventana y abre la carpeta del panel activo en la lista de libros. |

Al cerrar la ventana se vuelve a leer la lista de libros, así que lo que hayas hecho, movido, copiado, renombrado o eliminado se ve de inmediato (si la carpeta en la que estaba la lista ya no existe, se pasa al nivel superior).

Las teclas son acciones de los [atajos de teclado](../account.md#keyboard-shortcuts) de *Ajustes*: cámbialas o bórralas allí (no hay teclas de función por defecto, porque un Mac y los navegadores las reservan). Los botones muestran la tecla que hayas configurado.

<a id="copying"></a>

### Copiar

Una **copia** de un libro es un [clon](managing-books.md#cloning-a-book); la ventana pregunta si se toma solo la rama actual. Una copia de una carpeta es una carpeta nueva con los ajustes, la descripción y las etiquetas del original y copias de todos los libros y carpetas que contiene. Una copia puede tardar mucho, así que se ejecuta tras una barra de progreso que no se puede detener. Si falla a mitad, se conservan las copias hechas antes, cada una completa.

Un **movimiento** se hace al instante y se puede deshacer moviendo de vuelta. Una carpeta no se puede mover dentro de sí misma ni dentro de una carpeta que esté por debajo de ella.

<a id="when-a-name-is-taken"></a>

### Cuando un nombre ya está en uso

Cuando el destino ya tiene una carpeta con el mismo nombre, la ventana pregunta qué hacer con esa carpeta, con **Aplicar a todos** para el resto:

| Opción | |
|---|---|
| **Renombrar** | La carpeta recibe otro nombre. |
| **Omitir** | Se queda donde está (y no se copia). |
| **Combinar** | Lo que contiene se pasa a la carpeta que ya existe; las carpetas con el mismo nombre dentro se combinan a su vez. |

Los libros nunca chocan: sus nombres pueden repetirse.

<a id="mass-changes"></a>

## Cambios masivos

El menú **Gestionar → Cambios masivos** de la lista (y los botones de Organizar) cambian muchos libros a la vez. **Cambio masivo de modelo/protocolo** pone un modelo y/o un protocolo en los libros, **Cambio masivo de idioma** un idioma, **Cambio de nombre masivo** añade un prefijo o un sufijo a sus nombres, reemplaza un texto en ellos o los numera, y **Etiquetar libros** y **Etiquetar carpetas** añaden o quitan una etiqueta. Consulta [Gestionar libros](managing-books.md#tagging-many-books-at-once). Desde la lista actúan sobre los libros de la carpeta actual, con su propia opción **Incluir subcarpetas**.

<a id="favorites"></a>

## Favoritos

La estrella de la fila de un libro lo convierte en un **favorito**. Mientras tengas alguno, el nivel superior muestra la carpeta virtual **Favoritos** (una estrella) con los libros favoritos de todas tus carpetas. Es solo una vista: no puedes crear un libro ni una carpeta en ella, y quitar la estrella saca el libro de ella, no de su carpeta. Los favoritos se guardan en tus ajustes.

<a id="tagging-folders"></a>

## Etiquetar carpetas

Las carpetas tienen etiquetas, igual que los libros. **Gestionar → Cambios masivos → Etiquetar carpetas** etiqueta muchas carpetas a la vez. Las etiquetas de una carpeta no cambian las etiquetas de sus libros, y no forman parte de una [copia de seguridad](backups.md).

<a id="folders-and-other-features"></a>

## Carpetas y otras funciones

- **Copias de seguridad.** Una [copia de seguridad](backups.md#backups-and-folders) recuerda las carpetas de su libro, y un archivo exportado puede incluir los ajustes que hereda el libro.
- **Agentes.** Un [agente](../agents.md#agents-and-folders) escribe con los ajustes de las carpetas de su libro.
- **Asistente de ayuda.** Si lo permites, el [asistente](../administration/help-assistant.md#your-books) puede consultar tus libros y carpetas.
- **Teléfono.** La [vista de teléfono](../phone.md#books) muestra las mismas carpetas como tarjetas y permite crearlas, renombrarlas, configurarlas y eliminarlas; Organizar y los cambios masivos están en el menú **⋮** de su cabecera.
- **Visor.** El [visor](publishing.md#folders-in-the-list) también muestra las carpetas y se abre en la que dejaste.
