---
label: Gestionar libros
order: 100
---

# Gestionar libros
<a id="the-book-list"></a>

## La lista de libros

La pestaña **Libros** muestra tus libros. Los libros de otros usuarios no aparecen. La lista muestra una [carpeta](folders.md) a la vez: las carpetas de la actual aparecen encima de sus libros; en el nivel superior, sin carpetas creadas, es simplemente la lista de todos tus libros.

![La pestaña Libros](../../images/books.png)

| Columna | |
|---|---|
| **Nombre** | El nombre del libro. |
| **Etiquetas** | Las etiquetas del libro. |
| **Creado** | Cuándo se creó el libro. |
| **Última modificación** | Cuándo se modificó por última vez. |

La lista se ordena por *Última modificación*, de más reciente a más antigua. Haz clic en el encabezado de una columna para ordenar por ella; con varias columnas ordenadas, la lista se ordena por todas ellas en orden.

La fila debajo de los encabezados filtra la lista:

- **Nombre**: libros cuyo nombre *empieza* por el texto. Usa `*` como comodín: `*dragon` encuentra todos los libros con "dragon" en cualquier parte del nombre.
- **Etiquetas**: libros que tienen *todas* las etiquetas seleccionadas.

El botón de actualizar al final de la fila de filtros recarga la lista.

Cada fila tiene botones: la estrella marca el libro como [favorito](folders.md#favorites), el lápiz lo abre, **Mover a la carpeta** lo coloca en una [carpeta](folders.md), el botón de clonar crea una copia y el botón de papelera lo elimina. El encabezado tiene **Nueva carpeta**, el menú **Gestionar** (**Organizar**, la ventana para [mover y copiar](folders.md#organise) libros y carpetas, **Etiquetar libros**, **Estadísticas** y el submenú **Cambios masivos**) e **Importar**, que crea un libro nuevo a partir de archivos que tengas (véase [Importar un libro](#importing-a-book)).

<a id="a-book-is-open-in-one-window"></a>

### Un libro está abierto en una sola ventana

Un libro puede estar abierto en una sola ventana a la vez, incluso entre tus pestañas del navegador, otros navegadores y tu teléfono. Cuando abres un libro que está abierto en otro lugar, Marginalia indica desde cuándo y pregunta **Abrir aquí**: entonces el libro se abre aquí y su ventana en el otro lugar se cierra, indicando el motivo (si se está generando una parte allí, la pregunta lo indica y **Abrir aquí** detiene la generación; lo escrito hasta ese momento se conserva). **Cancelar** lo deja donde está. Cuando inicias sesión y Marginalia abre el libro que tenías abierto por última vez, no le quita el libro a otra ventana: lo deja cerrado e indica dónde está. Una ventana en una pestaña que se cerró o en un dispositivo que se apagó no conserva el libro: **Abrir aquí** lo toma de inmediato.

<a id="advanced-search"></a>

### Búsqueda avanzada

**Buscar** (en el encabezado, junto a **Gestionar**; la lupa del encabezado en un teléfono) abre una ventana que encuentra libros con más criterios de los que permite la fila de filtros. Rellena lo que quieres comprobar, deja el resto vacío y pulsa **Buscar** (o Intro en un campo de texto):

| Campo | Encuentra libros... |
| ----- | -------------- |
| **Nombre** | cuyo nombre contiene el texto (`*` es un comodín). |
| **Etiquetas que incluir** | que tienen las etiquetas: *Todas las etiquetas* o *Cualquiera de las etiquetas*, según elijas junto al campo. |
| **Etiquetas que excluir** | que no tienen las etiquetas: con *Cualquiera de las etiquetas* se excluye un libro que tenga al menos una de ellas; con *Todas las etiquetas* solo se excluye un libro que tenga todas. |
| **Creado desde / hasta**, **Modificado desde / hasta** | creados o modificados por última vez dentro del intervalo; el día de una fecha *hasta* se incluye. |
| **Número de partes desde / hasta** | con esa cantidad de partes en todas sus ramas (las partes en la papelera no se cuentan). |
| **Texto** | con una parte que tenga todas las palabras del texto. Las palabras, `"phrases"` y `*` funcionan como en [la búsqueda de la historia](story-editor.md#searching-the-story). |
| **Incluir versiones** | desactivado (por defecto): solo se busca el texto actual de las partes; activado: un libro también se encuentra si una versión anterior de una de sus partes tiene las palabras, véase [Historial de versiones](version-history.md#searching-the-old-text). |
| **Buscar en todas las ramas** | desactivado (por defecto): el texto se busca solo en la rama en la que está el libro (la historia tal como la ves al abrir el libro); activado: en las partes de todas las ramas. |

Un libro tiene que superar cada comprobación que hayas rellenado. Los libros encontrados se listan debajo con sus etiquetas, los modificados por última vez primero; un clic en el encabezado de una columna los ordena. El botón de carpeta abre un libro (en su ventana encima de la búsqueda, de modo que al cerrar el libro vuelves a los resultados). **Vaciar** vacía los campos.

![La búsqueda avanzada de libros](../../images/book-search-advanced.png)

<a id="cloning-a-book"></a>

### Clonar un libro

El botón de clonar pide el **Nombre** del nuevo libro (el nombre del original con *(Copia)* por defecto) y tiene la casilla **Solo la rama actual**, marcada por defecto. El nuevo libro es un libro independiente que obtiene:

![Clonar un libro](../../images/book-clone.png)

- la configuración del libro (descripción, idioma, prompts, punto de vista, tiempo verbal, estilo, ajustes de copia de seguridad), los datos que las extensiones guardan en el libro, y las etiquetas;
- el mismo **Modelo**, **Protocolo** y **lorebook**. El lorebook está *enlazado*, no copiado: un cambio en él se ve en ambos libros;
- la historia con su propia copia de las partes, sus resúmenes e imágenes. Con **Solo la rama actual** solo se copian las partes desde el principio hasta el final de la [rama activa](branches-and-story-tree.md); sin ella, se copian todas las ramas.

El clon inicia un [historial de versiones](version-history.md#backups-copies-and-the-trash) propio (una versión por parte); el historial del original no se copia.

El clon no se publica, aunque el original sí lo esté. El original no se modifica. Se coloca en la misma [carpeta](folders.md) que el original.

<a id="tagging-many-books-at-once"></a>

### Etiquetar muchos libros a la vez

**Etiquetar libros** (en el menú **Gestionar** del encabezado) abre una ventana que añade una etiqueta a muchos libros, o se la quita, sin abrir cada uno. Elige la **Etiqueta** (o escribe una nueva y pulsa Intro) y la lista muestra cada libro con sus etiquetas; los libros que tienen la etiqueta elegida aparecen primero y la muestran resaltada. Cada fila tiene un botón **+** que aplica la etiqueta a ese libro y un botón **-** que la quita. Para hacer lo mismo con varios libros, marca sus casillas (la casilla del encabezado marca todos) y usa **Aplicar la etiqueta a los marcados** o **Quitar la etiqueta de los marcados** en la parte inferior. Un libro en el que trabaja un agente aparece atenuado: está bloqueado, así que no se puede marcar ni cambiar hasta que la ejecución se pausa o se detiene. La misma ventana etiqueta [lorebooks y sus entradas](../lorebooks/lorebooks.md#tagging-many-at-once).

![Etiquetar libros](../../images/multi-tag-books.png)

<a id="changing-the-model-and-protocol-of-many-books"></a>

### Cambiar el modelo y el protocolo de muchos libros

El submenú **Cambios masivos** del menú **Gestionar** tiene el elemento **Cambio masivo de modelo/protocolo**. Abre una ventana que pone un **Modelo** y/o un **Protocolo** a muchos libros sin abrir cada uno. Deja un cuadro vacío (muestra *No reemplazar*) y esa parte de los libros se queda como está: elige solo un modelo para cambiar los modelos y conservar los protocolos. La tabla lista los libros de la [carpeta](folders.md) actual por nombre, con el modelo y el protocolo que tienen ahora; **Incluir subcarpetas** añade los libros que hay debajo. Escribe en el filtro bajo **Nombre** para reducir la lista, marca los libros que quieras cambiar (la casilla del encabezado marca todos los libros que se muestran) y pulsa **Reemplazar y salir**. Los libros que el filtro oculta se desmarcan, así que solo se cambia lo que ves. Un libro en el que trabaja un agente aparece atenuado: está bloqueado, así que no se puede marcar ni cambiar hasta que la ejecución se pausa o se detiene.

![Cambiar el modelo y el protocolo de muchos libros](../../images/mass-change-model-protocol.png)

El elemento **Cambio masivo de idioma** del mismo menú funciona igual para el **Idioma** de los libros: elige el idioma, filtra y marca los libros, y pulsa **Reemplazar y salir**. La tabla muestra el idioma que tiene cada libro ahora, con lo que hereda de sus carpetas (véase [Idioma](folders.md#language)).

![Cambiar el idioma de muchos libros](../../images/mass-change-language.png)

<a id="renaming-many-books"></a>

### Renombrar muchos libros

El elemento **Cambio de nombre masivo** renombra a la vez los libros marcados. Elige el modo:

| Modo | Qué hace |
|---|---|
| **Añadir prefijo** | Pone el **Texto que añadir** delante del nombre. |
| **Añadir sufijo** | Pone el **Texto que añadir** detrás del nombre. |
| **Buscar y reemplazar** | Sustituye cada aparición de **Buscar** en el nombre por **Reemplazar por** (vacío lo elimina). El texto se toma tal cual, no como patrón; **Distinguir mayúsculas y minúsculas** está desactivado por defecto. |
| **Numerar los libros** | Da números a los libros: el **Patrón de nombre** es el nuevo nombre, con `{name}` para el nombre actual y `{n}` para el número, por ejemplo `{name} {n}`, `{n}. {name}` o `Chapter {n}`. Al primer libro marcado se le asigna **Empezar en**, a cada siguiente se le suma el **Paso**, y **Dígitos** rellena el número con ceros (`007`). |

![Numerar los libros de todas las carpetas](../../images/mass-rename.png)

La columna **Nombre nuevo** muestra lo que recibiría cada libro marcado mientras escribes, así que compruébalo antes de pulsar **Renombrar**. Una numeración cuenta los libros marcados en el orden de la lista (por nombre). Los espacios al principio y al final del nuevo nombre se eliminan. No se renombra nada, y la ventana se queda abierta, cuando falta un texto, cuando el patrón no tiene `{n}`, cuando ningún nombre cambiaría o cuando un nombre quedaría vacío. El filtrado, **Incluir subcarpetas** y los libros bloqueados funcionan como en los demás cambios masivos.

**Etiquetar libros** y el elemento **Etiquetar carpetas** actúan sobre los libros, o sobre las carpetas, de la carpeta actual de la misma manera.
<a id="creating-a-book"></a>

## Crear un libro

1. Haz clic en **Añadir libro**.
2. Escribe el nombre y confirma.

El libro nuevo se guarda en la [carpeta](folders.md) que muestra la lista, y lo mismo ocurre con los libros que importas. Se abre enseguida. Empieza con tu **Modelo predeterminado** y tu **Protocolo predeterminado** de la pestaña *Ajustes*
(véase [Cuenta y ajustes](../account.md)), si los has configurado; en caso contrario, selecciónalos en la pestaña *Información* antes
de generar texto.

<a id="importing-a-book"></a>

### Importar un libro

**Importar** en la cabecera de la lista (el botón de archivo en un teléfono) abre una única ventana para todo lo que puede formar un libro nuevo.
Elige el **Origen**; el texto, los archivos admitidos y las opciones bajo el nombre dependen de él:

| Origen | Subida | Opciones |
|---|---|---|
| **Importar desde un archivo comprimido** | un `.zip` de archivos `.md`, una parte por cada archivo; véase [Importar al libro](story-editor.md#importing-into-the-book) | ninguna |
| **Importar desde SillyTavern** | uno o más chats `.jsonl`; véase [más abajo](#importing-from-sillytavern) | combinar los mensajes del usuario y del asistente |
| **Importar desde LM Studio** | una o más conversaciones; véase [más abajo](#importing-from-lm-studio) | combinar los mensajes del usuario y del asistente |
| **Importar desde un documento** | un manuscrito en un solo archivo (Word, LibreOffice, RTF, Markdown, texto plano, EPUB); véase [más abajo](#importing-a-finished-manuscript) | palabras por parte, nivel de título de los capítulos |
| **Importar desde una bóveda de Obsidian** | la `.zip` de una bóveda; véase [más abajo](#from-an-obsidian-vault-or-a-scrivener-project) | lo mismo |
| **Importar desde un proyecto de Scrivener** | la `.zip` de un proyecto `.scriv`; véase [más abajo](#from-an-obsidian-vault-or-a-scrivener-project) | lo mismo |

Sube el archivo, dale un nombre al libro (se rellena a partir del archivo, del título de un documento o del personaje de un chat; el libro
se llama *New book* si dejas el campo vacío) y haz clic en **Importar**. Una ventana de progreso con **Detener** muestra las partes
que se van guardando; al detenerla se conserva lo ya guardado. Un archivo que el origen no puede leer se rechaza con un aviso, y las demás subidas se mantienen.

La misma ventana, desde el menú de ajustes (⚙) de un libro, añade las partes a ese libro en el lugar que elijas; véase
[Importar al libro](story-editor.md#importing-into-the-book).

![Los orígenes de la ventana de importación](../../images/import-source.png)

![Importar un zip de archivos de texto como libro nuevo](../../images/archive-upload.png)

<a id="importing-a-finished-manuscript"></a>

### Importar un manuscrito terminado

Para un borrador que ya existe, elige **Importar desde un documento** y sube el archivo: un `.docx` (Word), `.odt` (LibreOffice,
OpenOffice), `.rtf` (texto enriquecido, de WordPad, TextEdit o cualquier otro programa), `.md` (un archivo Markdown), `.txt` (texto plano) o `.epub` (un libro electrónico). El texto se convierte en las partes de un libro nuevo, todo él marcado como escrito por ti (la importación no cuenta en las
[estadísticas](statistics.md) como escritura, y el historial de una parte dice *Import from a document*).

- **Capítulos.** Un párrafo con estilo de *Título* (un título `#` en un archivo Markdown) inicia un capítulo, y cada capítulo empieza con su título (el primer
  título de una parte es su título de capítulo, como en la [exportación](export.md)). **Los capítulos empiezan en el nivel de título** indica cuáles
  son esos títulos: el nivel que elijas y los que están por encima (al principio se elige el nivel más alto que tenga el documento); los títulos
  más profundos se quedan en el texto como títulos más pequeños. Un documento sin títulos es un solo capítulo. El *Título* del documento no se
  importa como texto, sino que da nombre al libro (en un archivo Markdown, la `title:` de un encabezado al principio, o un único título `#` delante de
  los `##` capítulos). Un archivo de texto plano no tiene títulos, así que una línea propia que diga `Chapter 12`, `Part IV: The Return`,
  `Book two` o `Prologue` (también `Epilogue`, `Interlude`, `Preface`, `Foreword`, `Afterword`) inicia un capítulo; en ese caso no hace falta elegir
  el nivel de título.
- **Palabras por parte.** El texto se corta entre párrafos en partes de unas tantas palabras (1000 al principio), en el límite de párrafo más cercano
  al número. Una parte nunca contiene el final de un capítulo y el principio del siguiente, y un capítulo no se corta por nada: lo que quedaría al final de un capítulo, cuando es menos de la mitad del número, se queda con la parte anterior,
  así que un capítulo de hasta una vez y media el número es una sola parte. Un párrafo más largo que el número no se corta.
- Una línea bajo el nombre indica cuántos capítulos, palabras y partes creará la importación; cambia a medida que cambias las opciones.
- **Párrafos de un archivo de texto.** Las líneas en blanco separan los párrafos y las líneas de un párrafo se unen (el texto ajustado a una columna
  se convierte en un solo párrafo); un archivo sin líneas en blanco tiene un párrafo por línea. Un archivo Markdown se conserva tal como está escrito,
  cortado en las líneas en blanco, los títulos y los bloques de código; también se lee el texto en Windows-1252 en lugar de UTF-8.
- **Formato.** La cursiva y la negrita se conservan (como Markdown `*italic*`, `**bold**`); el resto del formato (tipos de letra, tamaños,
  alineación), las imágenes, las cabeceras y los pies de página, las notas al pie, los comentarios y el texto eliminado de los cambios registrados no se importan. Las tablas se leen celda por celda.

![Importar un documento de Word, cortado en partes de unas 500 palabras](../../images/document-import.png)

Un archivo `.doc` antiguo hay que guardarlo antes como `.docx`, `.odt` o `.rtf`.

**Un EPUB** se lee página a página en su orden de lectura. Los títulos de las páginas (de `h1` a `h6`) son los capítulos; una página sin
título que el índice del libro electrónico nombre recibe ese nombre como título. La cubierta, el índice, las notas al pie y las
lecturas del texto ruby se dejan fuera, y el título del libro electrónico da nombre al libro. Un libro protegido con DRM no se puede leer.

<a id="from-an-obsidian-vault-or-a-scrivener-project"></a>

#### Desde una bóveda de Obsidian o un proyecto de Scrivener

Ambos son carpetas, así que comprime primero la carpeta (en un Mac, **Comprimir** en el Finder; la `.scriv` de Scrivener también es una carpeta) y sube el `.zip`. Las opciones son las
mismas que para un documento.

- **Bóveda de Obsidian.** Cada nota (`.md`) es un capítulo con el nombre de la nota (o la `title:` de su encabezado), en el orden de las rutas con los números
  contados como números (`Chapter 2` antes que `Chapter 10`). Una carpeta es una parte por encima de sus notas, y los títulos dentro de una nota están por debajo de los de la propia nota. Los enlaces `[[Note|text]]` se convierten en su texto; los incrustados (`[[file]]` con un `!` delante), los comentarios `%%...%%`, los identificadores de bloque y la marca de un aviso se eliminan; un resaltado `==text==` es texto plano. Las carpetas ocultas (`.obsidian`, `.trash`), los adjuntos y las notas sin texto se omiten. Si la
  bóveda contiene notas que no forman parte de la historia, bórralas antes de comprimirla.
- **Proyecto de Scrivener.** Se importa el manuscrito (la carpeta *Draft* del panel de documentos, en Scrivener 2 y 3), no la investigación, la papelera ni los documentos que no están *incluidos en la compilación*. Una carpeta, o un documento que tiene documentos debajo, es un título (su profundidad en el panel es su nivel, y su título el texto); los documentos que son todo lo que contiene una carpeta son sus escenas, una tras otra con un cambio de escena
  entre ellas (sus títulos no se usan). Un documento directamente en el manuscrito o en una carpeta de otras carpetas es un capítulo con su título. El
  nombre del proyecto da nombre al libro.

Sea cual sea el origen, el libro nuevo se abre al terminar la importación (también si la detuviste), y la ventana [**Importación terminada**](story-editor.md#after-an-import) ofrece escribir
los resúmenes de las partes y dejar que un agente redacte las notas del mundo de la historia.

<a id="importing-from-sillytavern"></a>

### Importar desde SillyTavern

Para seguir una partida de rol o una historia que empezaste en [SillyTavern](https://github.com/SillyTavern/SillyTavern), usa
**Importar → Importar desde SillyTavern**. En SillyTavern, exporta el chat
(*Manage chat files*, o toma los archivos `.jsonl` de `data/<user>/chats/<character>/`), y después:

1. Sube **uno o más** chats `.jsonl`. Un archivo que no es un chat de SillyTavern se rechaza, y los demás se mantienen.
2. Dale un nombre al libro (se rellena con el personaje del primer chat).
3. Marca **Combinar los mensajes del usuario y del asistente en una sola parte** si quieres una parte por turno: el mensaje del usuario
   (varios mensajes seguidos se unen con una línea vacía) se convierte en las *instrucciones* de la parte, y la respuesta que sigue, en su texto. Sin esta opción, cada mensaje es una parte propia, con su texto como texto de la parte, de modo que el libro se lee como se leyó el chat.
4. Haz clic en **Importar**. Una ventana de progreso con **Detener** muestra las partes que se van guardando; al detenerla se conserva lo ya guardado.

![Importar un chat de SillyTavern](../../images/sillytavern-import.png)

SillyTavern copia el chat cuando creas una *rama* o un *punto de control*, así que los chats de una misma historia comparten su
principio. Súbelos juntos y Marginalia encuentra los mensajes comunes y los guarda una sola vez: los chats se convierten en las
ramas de un único árbol, que puedes recorrer en la [vista de árbol](story-editor.md). Las *swipes* (respuestas alternativas) de un mensaje se convierten en partes hermanas, y el libro se abre al final del chat escrito en último lugar (según la fecha de su
último mensaje). Los chats que no empiezan con el mismo mensaje se convierten en primeras partes separadas. Los mensajes ocultos (de sistema) se omiten, y también las swipes vacías. El razonamiento y el nombre del modelo de una respuesta se conservan con la parte.

<a id="importing-from-lm-studio"></a>

### Importar desde LM Studio

Para seguir una conversación que tuviste en [LM Studio](https://lmstudio.ai), usa **Importar → Importar desde LM Studio**.
LM Studio guarda cada conversación en un archivo `<number>.conversation.json` de la carpeta `.lmstudio/conversations` de tu carpeta personal (en Windows,
`%USERPROFILE%\.lmstudio\conversations`). Funciona como [Importar desde SillyTavern](#importing-from-sillytavern):

1. Sube **una o más** conversaciones. Un archivo que no es una conversación de LM Studio se rechaza, y los demás se mantienen.
2. Dale un nombre al libro (se rellena con el nombre de la conversación, o con el nombre del archivo si no tiene).
3. Marca **Combinar los mensajes del usuario y del asistente en una sola parte** para una parte por turno (tu mensaje es las *instrucciones* de la parte), o déjala desmarcada para que cada mensaje sea una parte propia.
4. Haz clic en **Importar**.

![La ventana de importación desde LM Studio con una conversación subida](../../images/lmstudio-import.png)

Las versiones de un mensaje que LM Studio conserva (las respuestas que regeneraste, el texto que editaste) se convierten en partes hermanas, y la versión que se mostraba es aquella en la que se abre el libro; las conversaciones que comparten su principio se unen en un mismo árbol. El razonamiento de un modelo de razonamiento se conserva como razonamiento de la parte, junto con el nombre del modelo. El prompt de sistema de la conversación, las llamadas a herramientas y los archivos adjuntos no se importan.

Para empezar un libro a partir de un archivo de copia de seguridad, usa **Importar copia de seguridad como libro nuevo** en la pestaña *Ajustes*; véase
[Copias de seguridad de libros](backups.md#importing-a-backup-as-a-new-book).
<a id="the-about-tab"></a>

## La pestaña Acerca de

![La pestaña Acerca de de un libro](../../images/book-about.png)

| Campo | |
|---|---|
| **Nombre** | El nombre del libro. |
| **Etiquetas** | Etiquetas para ordenar tus libros y para la activación de [lorebook](../lorebooks/entries.md). Escribe para elegir una etiqueta existente o crear una nueva. |
| **Descripción** | De qué trata el libro. Disponible para las plantillas de prompt como la descripción del libro. |
| **Idioma** | El idioma en que está escrito el libro. Se usa para la separación automática en sílabas del texto en el estilo de lectura de libro y en el [visor](publishing.md); no cambia lo que escribe el modelo. Vacío significa *heredar*: el idioma de la [carpeta](folders.md#language) más cercana por encima del libro que establezca uno; si ninguna lo establece, inglés. El cuadro indica cuál es y de dónde viene. |
| **Modelo** | El [proveedor de inferencia](../inference-providers.md) que escribe este libro. |
| **Protocolos** | El [protocolo](../protocols.md) con los ajustes de generación. |
| **Versiones conservadas por parte** | Cuántas versiones anteriores del texto de una parte conserva el [historial](version-history.md). Vacío significa *heredar*: el número gris del cuadro es el límite que se aplica, y la línea debajo indica qué carpeta, tus ajustes o el administrador lo establecen. **0** no conserva ningún historial. |
| **Publicado** | Permite que otros usuarios lean el libro; consulta [Publicación y visor](publishing.md). Una vez marcado, el enlace de lectura del libro (`<address of Marginalia>/view/<book ID>`) aparece debajo de la casilla; cópialo y envíalo a los lectores. |
| **Mostrar las imágenes a los lectores** | Marcado por defecto. Los lectores de un libro publicado ven las imágenes de sus partes; desmárcalo para mostrarles solo el texto. Tú siempre ves tus propias imágenes. |

Los cambios se guardan mientras escribes.

Debajo aparecen los recuentos de solo lectura:

| | |
|---|---|
| **Total de palabras**, **Total de tokens** | Todas las partes del libro, en todas las ramas. |
| **Palabras de la rama activa**, **Tokens de la rama activa** | Solo las partes de la [rama activa](branches-and-story-tree.md). |
| **Autoría de la rama** | Pulsa **Calcular**: qué porcentaje de las palabras de la rama activa escribiste tú y cuál generó un modelo, y el número de palabras. Se calcula a partir del [historial de versiones](version-history.md) de cada parte, así que tarda un rato en un libro largo; una parte cuyo historial está desactivado se compara con el texto que el modelo generó primero. Consulta [Resaltar el texto generado](story-editor.md#highlighting-generated-text). |

Un libro necesita un modelo y un protocolo para generar. Sin ellos, la generación se detiene con *Al libro le falta el modelo.* o
*Al libro le falta el protocolo.*

<a id="deleting-a-book"></a>

## Eliminar un libro

Pulsa el botón de la papelera en la lista de libros y confirma. El libro desaparece de tu lista.

Un libro no se elimina mientras algo trabaja en él: una parte que se está generando, un lote de resúmenes, una traducción, una ventana de reescritura o de anotación, o el libro abierto en otra ventana. Marginalia indica entonces qué lo impide (*Está en uso: ...*); espera a que termine o deténlo, y vuelve a eliminar. Lo mismo ocurre al restaurar una [copia de seguridad](backups.md) en el libro mientras otra ventana o una ejecución trabaja en él.

Los libros eliminados no se borran de la base de datos de inmediato. Hasta que un administrador los elimine, junto con todas sus partes, mediante [Limpieza](../administration/cleanup.md), puedes recuperar el libro desde la pestaña [Papelera](../trash.md). Mientras tanto, los datos siguen en las [copias de seguridad de la base de datos](../administration/database-backups.md). Las [copias de seguridad](backups.md) propias del libro son archivos y permanecen donde están.
