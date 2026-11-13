---
label: Gestión de libros
order: 100
---

# Gestión de libros
<a id="the-book-list"></a>

## La lista de libros

La pestaña **Libros** muestra tus libros. No se muestran los libros de otros usuarios. La lista muestra una [carpeta](folders.md) a la vez: las carpetas del nivel actual aparecen encima de sus libros. En el nivel superior, sin carpetas creadas, es simplemente la lista de todos tus libros.

![La pestaña Libros](../../images/books.png)

| Columna | |
|---|---|
| **Nombre** | El nombre del libro. |
| **Etiquetas** | Las etiquetas del libro. |
| **Creado** | Cuándo se creó el libro. |
| **Última modificación** | Cuándo se cambió por última vez. |

La lista se ordena por *Última modificación*, del más reciente al más antiguo. Haz clic en el encabezado de una columna para ordenar por ella; si hay varias columnas ordenadas, la lista se ordena por todas ellas, en el orden en que se marcaron.

La fila debajo de los encabezados filtra la lista:

- **Nombre**: libros cuyo nombre *empieza* por el texto. Usa `*` como comodín: `*dragon` encuentra todos los libros con "dragon" en cualquier parte del nombre.
- **Etiquetas**: libros que tienen *todas* las etiquetas seleccionadas.

El botón de actualizar al final de la fila de filtros vuelve a cargar la lista.

Cada fila tiene botones: la estrella convierte el libro en un [favorito](folders.md#favorites), el lápiz lo abre, **Mover a la carpeta** lo coloca en una [carpeta](folders.md), el botón de clonar crea una copia y el botón de papelera lo elimina. La cabecera tiene **Nueva carpeta**, el menú **Gestionar** (**Organizar**, la ventana para [mover y copiar](folders.md#organise) libros y carpetas, **Etiquetar libros**, **Estadísticas** y el submenú **Cambios masivos**) e **Importar**, que crea un libro nuevo a partir de archivos que tengas (véase [Importar un libro](#importing-a-book)).

<a id="a-book-is-open-in-one-window"></a>

### Un libro está abierto en una ventana

Un libro solo puede estar abierto en una ventana a la vez, incluidas las pestañas de tu navegador, otros navegadores y tu teléfono. Cuando abres un libro que está abierto en otro lugar, Marginalia indica desde cuándo y pregunta **Abrir aquí**: entonces el libro se abre aquí y su ventana en el otro lugar se cierra, con un aviso del motivo (si se está generando una parte allí, la pregunta lo indica y **Abrir aquí** detiene la generación; lo escrito hasta ese momento se conserva). **Cancelar** lo deja donde está. Cuando inicias sesión y Marginalia abre el libro que tenías abierto por última vez, no le quita el libro a otra ventana: lo deja cerrado y te indica dónde está. Una ventana de una pestaña que se cerró o de un dispositivo que se apagó no retiene el libro: **Abrir aquí** lo toma de inmediato.

<a id="advanced-search"></a>

### Búsqueda avanzada

**Buscar** (en la cabecera, junto a **Gestionar**; la lupa de la cabecera en un teléfono) abre una ventana que encuentra libros según más criterios de los que permite la fila de filtros. Rellena lo que quieras comprobar, deja vacío el resto y pulsa **Buscar** (o Intro en un campo de texto):

| Campo | Encuentra libros... |
| ----- | -------------- |
| **Nombre** | cuyo nombre contiene el texto (`*` es un comodín). |
| **Etiquetas que incluir** | que tienen las etiquetas: *Todas las etiquetas* o *Cualquiera de las etiquetas*, según elijas junto al campo. |
| **Etiquetas que excluir** | que no tienen las etiquetas: con *Cualquiera de las etiquetas*, se descarta un libro que tenga al menos una de ellas; con *Todas las etiquetas*, solo se descarta un libro que tenga todas. |
| **Created from / to**, **Modified from / to** | creados o modificados por última vez dentro del intervalo; el día de una fecha *to* está incluido. |
| **Number of parts from / to** | con esa cantidad de partes en todas sus ramas (las partes en la papelera no se cuentan). |
| **Texto** | con una parte que tenga todas las palabras del texto. Las palabras, `"frases"` y `*` funcionan como en [la búsqueda en la historia](story-editor.md#searching-the-story). |
| **Incluir versiones** | desactivada (por defecto): solo se busca el texto actual de las partes; activada: un libro también se encuentra si una versión anterior de una de sus partes tiene las palabras; véase [Historial de versiones](version-history.md#searching-the-old-text). |
| **Buscar en todas las ramas** | desactivada (por defecto): el texto se busca solo en la rama en la que está el libro (la historia tal como la ves al abrir el libro); activada: en las partes de todas las ramas. |

Un libro tiene que superar todas las comprobaciones que hayas rellenado. Los libros encontrados aparecen listados debajo con sus etiquetas, primero los modificados más recientemente; un clic en el encabezado de una columna los ordena. El botón de carpeta abre un libro (en su ventana, encima de la búsqueda, de modo que al cerrar el libro vuelves a los resultados). **Vaciar** borra los campos.

![La búsqueda avanzada de libros](../../images/book-search-advanced.png)

<a id="cloning-a-book"></a>

### Clonar un libro

El botón de clonar pide el **Nombre** del nuevo libro (por defecto, el nombre del original con *(Copia)*) y tiene la casilla **Solo la rama actual**, marcada por defecto. El nuevo libro es un libro independiente que recibe:

![Clonar un libro](../../images/book-clone.png)

- la configuración del libro (descripción, idioma, prompts, punto de vista, tiempo verbal, estilo, ajustes de copia de seguridad), los datos que las extensiones guardan en el libro y las etiquetas;
- el mismo **Modelo**, **Protocolo** y lorebook. El lorebook queda *vinculado*, no copiado: un cambio en él se ve en ambos libros;
- la historia con su propia copia de las partes, sus resúmenes e imágenes. Con **Solo la rama actual** solo se copian las partes desde el principio hasta el final de la [rama activa](branches-and-story-tree.md); sin esa opción se copian todas las ramas.

El clon inicia un [historial de versiones](version-history.md#backups-copies-and-the-trash) propio (una versión por parte); el historial del original no se copia.

El clon no se publica, aunque el original sí lo esté. El original no se modifica. Se coloca en la misma [carpeta](folders.md) que el original.

<a id="tagging-many-books-at-once"></a>

### Etiquetar muchos libros a la vez

**Etiquetar libros** (en el menú **Gestionar** de la cabecera) abre una ventana que añade una etiqueta a muchos libros, o la quita de ellos, sin abrir cada uno. Elige la **Etiqueta** (o escribe una nueva y pulsa Intro) y la lista muestra todos los libros con sus etiquetas; los libros que tienen la etiqueta elegida aparecen primero y la muestran resaltada. Cada fila tiene un botón **+** que aplica la etiqueta a ese libro y un botón **-** que la quita. Para hacer lo mismo con varios libros, marca sus casillas (la casilla de la cabecera marca todas) y usa **Aplicar la etiqueta a los marcados** o **Quitar la etiqueta de los marcados** en la parte inferior. Un libro en el que trabaja un agente aparece en gris: está bloqueado, así que no se puede marcar ni cambiar hasta que la ejecución se pause o se detenga. La misma ventana etiqueta [los lorebooks y sus entradas](../lorebooks/lorebooks.md#tagging-many-at-once).

![Etiquetar libros](../../images/multi-tag-books.png)

<a id="changing-the-model-and-protocol-of-many-books"></a>

### Cambiar el modelo y el protocolo de muchos libros

El submenú **Cambios masivos** del menú **Gestionar** tiene el elemento **Cambio masivo de modelo/protocolo**. Abre una ventana que asigna un **Modelo** y/o un **Protocolo** a muchos libros sin abrir cada uno. Deja un cuadro vacío (muestra *No reemplazar*) y esa parte de los libros no cambia: elige solo un modelo para cambiar los modelos y mantener los protocolos. La tabla lista los libros de la [carpeta](folders.md) actual por nombre, con el modelo y el protocolo que tienen ahora; **Incluir subcarpetas** añade los libros que hay debajo. Escribe en el filtro bajo **Nombre** para reducir la lista, marca los libros que quieras cambiar (la casilla de la cabecera marca todos los que se muestran) y pulsa **Reemplazar y salir**. Los libros que el filtro oculta se desmarcan, así que solo se cambia lo que ves. Un libro en el que trabaja un agente aparece en gris: está bloqueado, así que no se puede marcar ni cambiar hasta que la ejecución se pause o se detenga.

![Cambiar el modelo y el protocolo de muchos libros](../../images/mass-change-model-protocol.png)

El elemento **Cambio masivo de idioma** del mismo menú funciona igual para el **Idioma** de los libros: elige el idioma, filtra y marca los libros, y pulsa **Reemplazar y salir**. La tabla muestra el idioma que tiene cada libro ahora, junto con lo que hereda de sus carpetas (véase [Idioma](folders.md#language)).

![Cambiar el idioma de muchos libros](../../images/mass-change-language.png)

<a id="renaming-many-books"></a>

### Renombrar muchos libros

El elemento **Cambio de nombre masivo** renombra a la vez los libros marcados. Elige el modo:

| Modo | Qué hace |
|---|---|
| **Añadir prefijo** | Pone el **Texto que añadir** delante del nombre. |
| **Añadir sufijo** | Pone el **Texto que añadir** detrás del nombre. |
| **Buscar y reemplazar** | Sustituye cada aparición de **Buscar** en el nombre por **Reemplazar por** (vacío lo elimina). El texto se toma tal cual, no como patrón; **Distinguir mayúsculas y minúsculas** está desactivado por defecto. |
| **Numerar los libros** | Numera los libros: el **Patrón de nombre** es el nombre nuevo, con `{name}` para el nombre actual y `{n}` para el número, por ejemplo `{name} {n}`, `{n}. {name}` o `Capítulo {n}`. Al primer libro marcado le corresponde **Empezar en**, y a cada siguiente se le suma el **Paso**; **Dígitos** rellena el número con ceros (`007`). |

![Numerar los libros de todas las carpetas](../../images/mass-rename.png)

La columna **Nombre nuevo** muestra lo que recibiría cada libro marcado mientras escribes, así que revísala antes de pulsar **Renombrar**. La numeración cuenta los libros marcados en el orden de la lista (por nombre). Los espacios al principio y al final del nombre nuevo se eliminan. No se renombra nada, y la ventana permanece abierta, cuando falta un texto, cuando el patrón no tiene `{n}`, cuando ningún nombre cambiaría o cuando algún nombre quedaría vacío. El filtrado, **Incluir subcarpetas** y los libros bloqueados funcionan como en los demás cambios masivos.

**Etiquetar libros** y el elemento **Etiquetar carpetas** actúan igual sobre los libros, o sobre las carpetas, de la carpeta actual.
<a id="creating-a-book"></a>

## Crear un libro

1. Haz clic en **Añadir libro**.
2. Escribe el nombre y confirma.

El libro nuevo se coloca en la [carpeta](folders.md) que muestra la lista, y lo mismo ocurre con los libros que importas. Se abre enseguida. Empieza con tu **Modelo predeterminado** y tu **Protocolo predeterminado** de la pestaña *Ajustes*
(consulta [Cuenta y ajustes](../account.md)), si los has configurado; si no, selecciónalos en la pestaña *Información* antes de
generar.

<a id="importing-a-book"></a>

### Importar un libro

**Importar** en la cabecera de la lista (el botón de archivo en un teléfono) abre una única ventana para todo aquello de lo que puede constar un libro nuevo.
Elige el **Origen**; el texto, los archivos admitidos y las opciones situadas debajo del nombre dependen de él:

| Origen | Subir | Opciones |
|---|---|---|
| **Importar desde un archivo comprimido** | un `.zip` de archivos `.md`, una parte por cada archivo; consulta [Importar en el libro](story-editor.md#importing-into-the-book) | ninguna |
| **Importar desde SillyTavern** | uno o varios chats `.jsonl`; consulta [más abajo](#importing-from-sillytavern) | combinar los mensajes del usuario y del asistente |
| **Importar desde LM Studio** | una o varias conversaciones; consulta [más abajo](#importing-from-lm-studio) | combinar los mensajes del usuario y del asistente |
| **Importar desde un documento** | un manuscrito en un solo archivo (Word, LibreOffice, RTF, Markdown, texto plano, EPUB); consulta [más abajo](#importing-a-finished-manuscript) | palabras por parte, nivel de título de los capítulos |
| **Importar desde una bóveda de Obsidian** | el `.zip` de una bóveda; consulta [más abajo](#from-an-obsidian-vault-or-a-scrivener-project) | lo mismo |
| **Importar desde un proyecto de Scrivener** | el `.zip` de un proyecto `.scriv`; consulta [más abajo](#from-an-obsidian-vault-or-a-scrivener-project) | lo mismo |

Sube el archivo, dale un nombre al libro (se rellena a partir del archivo, del título de un documento o del personaje de un chat; si lo dejas vacío, el libro se llama *New book*) y haz clic en **Importar**. Una ventana de progreso con **Detener** muestra las partes que se van guardando;
al detener, se conserva lo guardado hasta ese momento. Si el origen no puede leer un archivo, se rechaza con una advertencia y las demás subidas se mantienen.

La misma ventana, desde el menú de ajustes (⚙) de un libro, añade las partes a ese libro en el lugar que elijas; consulta
[Importar en el libro](story-editor.md#importing-into-the-book).

![Los orígenes de la ventana de importación](../../images/import-source.png)

![Importar un zip de archivos de texto como libro nuevo](../../images/archive-upload.png)

<a id="importing-a-finished-manuscript"></a>

### Importar un manuscrito terminado

Para un borrador que ya existe, elige **Importar desde un documento** y sube el archivo: un `.docx` (Word), `.odt` (LibreOffice,
OpenOffice), `.rtf` (texto enriquecido, de WordPad, TextEdit o cualquier otro programa), `.md` (un archivo Markdown), `.txt` (texto plano) o `.epub` (un libro electrónico). El texto se convierte en las partes de un libro nuevo, todo ello marcado como escrito por ti (la importación no cuenta como escritura en las [estadísticas](statistics.md) y el historial de una parte dice *Import from a document*).

- **Capítulos.** Un párrafo con estilo *Título* (un título `#` en un archivo Markdown) inicia un capítulo, y cada capítulo empieza con su título (el primer
  título de una parte es su título de capítulo, como en la [exportación](export.md)). **Los capítulos empiezan en el nivel de título** indica cuáles son
  esos títulos: el nivel que elijas y los que estén por encima de él (al principio se elige el nivel más alto que tenga el documento); los títulos
  más profundos se quedan en el texto como títulos más pequeños. Un documento sin títulos es un solo capítulo. El *Título* del documento no
  se importa como texto, sino que da nombre al libro (en un archivo Markdown, el `title:` cabecera superior, o un único título `#` delante de
  los capítulos `##`). Un archivo de texto plano no tiene títulos, así que una línea propia que diga `Chapter 12`, `Part IV: The Return`,
  `Book two` o `Prologue` (también `Epilogue`, `Interlude`, `Preface`, `Foreword`, `Afterword`) inicia un capítulo; en ese caso no hace falta elegir el
  nivel de título.
- **Palabras por parte.** El texto se corta entre párrafos en partes de unas tantas palabras (1000 al principio), en el límite de párrafo más cercano al número. Una parte nunca contiene el final de un capítulo y el comienzo del siguiente, y un capítulo no se corta sin motivo: lo que quedaría sobrante al final de un capítulo, cuando es menos de la mitad del número, se queda con la parte anterior,
  de modo que un capítulo de hasta una vez y media el número es una sola parte. Un párrafo más largo que el número no se corta.
- Una línea bajo el nombre indica cuántos capítulos, palabras y partes creará la importación; cambia a medida que cambias las opciones.
- **Párrafos de un archivo de texto.** Las líneas en blanco separan los párrafos y las líneas de un párrafo se unen (el texto con saltos de línea fijos pasa a ser un solo párrafo); un archivo sin líneas en blanco tiene un párrafo en cada línea. Un archivo Markdown se conserva tal como está escrito, cortado por las líneas en blanco, los títulos y los bloques de código; también se lee texto en Windows-1252 en lugar de UTF-8.
- **Formato.** La cursiva y la negrita se conservan (como Markdown `*cursiva*`, `**negrita**`); el resto del formato (tipos de letra, tamaños, alineación), las imágenes, las cabeceras y pies de página, las notas al pie, los comentarios y el texto eliminado de los cambios controlados no se importan. Las tablas se leen celda a celda.

![Importar un documento de Word, cortado en partes de unas 500 palabras](../../images/document-import.png)

Un archivo `.doc` antiguo debe guardarse primero como `.docx`, `.odt` o `.rtf`.

**Un EPUB** se lee página a página en su orden de lectura. Los títulos de las páginas (`h1` a `h6`) son los capítulos; una página sin título que la tabla de contenidos del libro electrónico nombra recibe ese nombre como título. La portada, la tabla de contenidos, las notas al pie y las lecturas del texto ruby se dejan fuera, y el título del libro electrónico da nombre al libro. Un libro protegido por DRM no se puede leer.

<a id="from-an-obsidian-vault-or-a-scrivener-project"></a>

#### Desde una bóveda de Obsidian o un proyecto de Scrivener

Ambos son carpetas, así que comprime primero la carpeta (en un Mac, **Comprimir** en el Finder; el `.scriv` de Scrivener también es una carpeta) y sube el `.zip`. Las opciones son las
mismas que para un documento.

- **Bóveda de Obsidian.** Cada nota (`.md`) es un capítulo con el nombre de la nota (o el `title:` de su cabecera), en el orden de las rutas, contando los números como números (`Chapter 2` antes que `Chapter 10`). Una carpeta es una parte por encima de sus notas, y los títulos dentro de una nota quedan por debajo de los de la propia nota. Los enlaces `[[Nota|texto]]` se convierten en su texto; las inserciones (`[[archivo]]` con un `!` delante), los comentarios `%%...%%`, los identificadores de bloque y la marca de un callout se eliminan; un resaltado `==texto==` es texto plano. Se omiten las carpetas ocultas (`.obsidian`, `.trash`), los adjuntos y las notas sin texto. Si la bóveda contiene notas que no forman parte de la historia, bórralas antes de comprimir.
- **Proyecto de Scrivener.** Se importa el manuscrito (la carpeta *Draft* del índice, Scrivener 2 y 3), no la investigación, la papelera ni los documentos que no están *incluidos en la compilación*. Una carpeta, o un documento que tiene documentos dentro, es un título (su profundidad en el índice es su nivel, y su título el texto); los documentos que contiene una carpeta sin subcarpetas son sus escenas, una tras otra con un cambio de escena entre ellas (sus títulos no se usan). Un documento situado directamente en el manuscrito, o en una carpeta que contiene otras carpetas, es un capítulo con su título. El nombre del proyecto da nombre al libro.

Sea cual sea el origen, el libro nuevo se abre al terminar la importación (también si la detuviste), y la ventana [**Importación terminada**](story-editor.md#after-an-import) ofrece escribir los resúmenes de las partes y dejar que un agente redacte las notas del lorebook.

<a id="importing-from-sillytavern"></a>

### Importar desde SillyTavern

Para continuar un juego de rol o una historia que empezaste en [SillyTavern](https://github.com/SillyTavern/SillyTavern), usa
**Importar → Importar desde SillyTavern**. En SillyTavern, exporta el chat
(*Manage chat files*, o toma los archivos `.jsonl` de `data/<user>/chats/<character>/`) y luego:

1. Sube **uno o varios** chats `.jsonl`. Un archivo que no es un chat de SillyTavern se rechaza, los demás se mantienen.
2. Dale un nombre al libro (se rellena con el personaje del primer chat).
3. Marca **Combinar los mensajes del usuario y del asistente en una sola parte** si quieres una parte por turno: el mensaje del usuario
   (varios mensajes seguidos se unen con una línea vacía) pasa a ser las *instrucciones* de la parte, y la respuesta que le sigue, su texto. Sin esta opción, cada mensaje es una parte propia, con su texto como texto de la parte, de modo que el libro se lee como se leía el chat.
4. Haz clic en **Importar**. Una ventana de progreso con **Detener** muestra las partes que se van guardando; al detener, se conserva lo guardado.

![Importar un chat de SillyTavern](../../images/sillytavern-import.png)

SillyTavern copia el chat cuando creas una *rama* o un *punto de control*, así que los chats de una misma historia comparten su comienzo. Súbelos juntos y Marginalia encuentra los mensajes comunes y los guarda una sola vez: los chats se convierten en las ramas de un mismo árbol, que puedes recorrer en la [vista de árbol](story-editor.md). Los *swipes* (respuestas alternativas) de un mensaje se convierten en partes hermanas, y el libro se abre al final del chat escrito en último lugar (según la fecha de su último mensaje). Los chats que no comienzan con el mismo mensaje se convierten en primeras partes separadas. Se omiten los mensajes ocultos (de sistema) y los swipes vacíos. El razonamiento y el nombre del modelo de una respuesta se conservan con la parte.

<a id="importing-from-lm-studio"></a>

### Importar desde LM Studio

Para continuar una conversación que tuviste en [LM Studio](https://lmstudio.ai), usa **Importar → Importar desde LM Studio**.
LM Studio guarda cada conversación en un archivo `<number>.conversation.json` dentro de la carpeta `.lmstudio/conversations` de tu carpeta personal (en Windows,
`%USERPROFILE%\.lmstudio\conversations`). Funciona igual que [Importar desde SillyTavern](#importing-from-sillytavern):

1. Sube **una o varias** conversaciones. Un archivo que no es una conversación de LM Studio se rechaza, los demás se mantienen.
2. Dale un nombre al libro (se rellena con el nombre de la conversación, o con el nombre del archivo si no tiene).
3. Marca **Combinar los mensajes del usuario y del asistente en una sola parte** para tener una parte por turno (tu mensaje son las *instrucciones* de la parte), o déjalo desmarcado para que cada mensaje sea una parte propia.
4. Haz clic en **Importar**.

![La ventana de importación desde LM Studio con una conversación subida](../../images/lmstudio-import.png)

Las versiones de un mensaje que LM Studio conserva (las respuestas que regeneraste, el texto que editaste) se convierten en partes hermanas, y el libro se abre en la versión que se mostraba; las conversaciones que comparten su comienzo se unen en un mismo árbol. El razonamiento de un modelo de razonamiento se conserva como el razonamiento de la parte, junto con el nombre del modelo. No se importan el prompt de sistema de la conversación, las llamadas a herramientas ni los archivos adjuntos.

Para empezar un libro a partir de un archivo de copia de seguridad, usa **Importar copia de seguridad como libro nuevo** en la pestaña *Ajustes*; consulta
[Copias de seguridad del libro](backups.md#importing-a-backup-as-a-new-book).
<a id="the-about-tab"></a>

## El separador About

![The About tab of a book](../../images/book-about.png)

| Field | |
|---|---|
| **Name** | El nombre del libro. |
| **Tags** | Etiquetas para ordenar tus libros y para la activación de [lorebook](../lorebooks/entries.md). Escribe para elegir una etiqueta existente o crear una nueva. |
| **Description** | De qué trata el libro. Disponible para las plantillas de prompt como la descripción del libro. |
| **Language** | El idioma en que está escrito el libro. Se usa para la división automática en sílabas del texto en el estilo de lectura del libro y en el [visor](publishing.md); no cambia lo que escribe el modelo. Vacío significa *heredar*: el idioma de la [carpeta](folders.md#language) más cercana por encima del libro que defina uno; si no hay ninguna, inglés. El recuadro muestra cuál y de dónde procede. |
| **Model** | El [proveedor de inferencia](../inference-providers.md) que escribe este libro. |
| **Protocols** | El [protocolo](../protocols.md) con los ajustes de generación. |
| **Versions kept per part** | Cuántas versiones anteriores del texto de una parte conserva el [historial](version-history.md). Vacío significa *heredar*: el número gris del recuadro es el límite que se aplica, y la línea debajo indica qué carpeta, tus ajustes o el administrador lo define. **0** no conserva historial. |
| **Published** | Permite que otros usuarios lean el libro; consulta [Publicación y visor](publishing.md). Una vez marcada la casilla, el enlace de lectura del libro (`<dirección de Marginalia>/view/<ID del libro>`) aparece bajo la casilla; cópialo y envíaselo a los lectores. |
| **Show the images to readers** | Marcada por defecto. Los lectores de un libro publicado ven las imágenes de sus partes; desmarca la casilla para mostrarles solo el texto. Tú siempre ves tus propias imágenes. |

Los cambios se guardan mientras escribes.

Debajo hay recuentos de solo lectura:

| | |
|---|---|
| **Total Word Count**, **Total Token Count** | Todas las partes del libro, en todas las ramas. |
| **Active Branch Word Count**, **Active Branch Token Count** | Solo las partes de la [rama activa](branches-and-story-tree.md). |
| **Authorship of the branch** | Pulsa **Calculate**: cuántos por ciento de las palabras de la rama activa escribiste tú y cuántos generó un modelo, y el número de palabras. Se calcula a partir del [historial de versiones](version-history.md) de cada parte, así que tarda un poco en un libro largo; una parte cuyo historial está desactivado se compara con el texto que generó el modelo primero. Consulta [Resaltado del texto generado](story-editor.md#highlighting-generated-text). |

Un libro necesita un modelo y un protocolo para generar. Sin ellos, la generación se detiene con *Al libro le falta el modelo.* o *Al libro le falta el protocolo.*

<a id="deleting-a-book"></a>

## Eliminar un libro

Pulsa el botón de papelera en la lista de libros y confirma. El libro desaparece de tu lista.

Un libro no se elimina mientras algo trabaja en él: una parte que se está generando, un lote de resúmenes, una traducción, una ventana de reescritura o de anotación, o el libro abierto en otra ventana. Marginalia indica entonces qué lo impide (*This is in use: ...*); espera a que termine o detenlo, y vuelve a eliminar. Lo mismo ocurre al restaurar una [copia de seguridad](backups.md) en el libro mientras otra ventana o una ejecución trabaja en él.

Los libros eliminados no se borran de la base de datos de inmediato. Hasta que un administrador los elimine, junto con todas sus partes, mediante [Limpieza](../administration/cleanup.md), puedes recuperar el libro desde la pestaña [Papelera](../trash.md). Mientras tanto, los datos siguen en las [copias de seguridad de la base de datos](../administration/database-backups.md). Las [copias de seguridad](backups.md) propias del libro son archivos y permanecen donde están.
