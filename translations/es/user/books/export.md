---
label: Exportar un libro
order: 55
---

# Exportar un libro

La exportación convierte la historia en un archivo que puedes leer, imprimir, enviar o publicar en otro sitio. A diferencia de una [copia de seguridad](backups.md), contiene solo el texto, como un libro terminado.

Solo se exporta la [rama activa](branches-and-story-tree.md), es decir, las mismas partes que muestra el editor de la historia. Para exportar otra versión de la historia, cambia primero a esa rama.

<a id="exporting"></a>

## Exportar

1. Abre el libro y ve a la pestaña **Historia**.
2. Abre el menú de ajustes (⚙) en la barra inferior y elige **Exportar la historia**.
3. Rellena el diálogo y elige **Exportar**.
4. Marginalia reúne las partes de la rama y escribe el archivo, lo que tarda un momento en un libro largo. Una barra de progreso muestra cuánto falta.
5. Cuando el archivo esté listo, un diálogo ofrece descargarlo. Elige el botón **Descargar** para guardarlo.

![El diálogo Exportar la historia](../../images/export-dialog.png)

| Campo | |
|---|---|
| **Formato** | El tipo de archivo, véase más abajo. |
| **Estilo de impresión** | Solo para los formatos que tienen tipografías y colores (HTML, Word, PDF, EPUB). El aspecto del texto, elegido entre los estilos *para exportaciones*: la aplicación trae uno para cada uno de estos formatos, **Página HTML**, **Libro electrónico (EPUB)**, **Libro en PDF** y **Documento de Word**, que son los aspectos que las exportaciones siempre han tenido y se eligen solos al seleccionar el formato. También puedes crear el tuyo, véase [Estilos de impresión](#print-styles). El estilo que hayas elegido para el formato en [Ajustes → Exports](../account.md#exports) es el que aparece preseleccionado. |
| **Nombre del archivo** | Nombre del archivo descargado sin la extensión, que Marginalia añade. Empieza con el nombre del libro. |
| **Título del libro** | El título del archivo (portada y propiedades del archivo). Empieza con el nombre del libro; cambiarlo no renombra el libro. |
| **Autor** | Empieza con tu nombre completo, tal como figura en tu [cuenta](../account.md). Si está vacío, el libro no tiene autor. |
| **Desde el mensaje**, **To message** | Qué partes exportar. Las partes se numeran desde 1 en el orden de la historia; las partes sin texto no cuentan. Deja **To message** vacío para exportar hasta el final. |
| **Incluir la portada** | Empieza el archivo con una primera página que contiene el título y el autor. |
| **Incluir la declaración de autoría** | Termina el archivo con una breve declaración de quién escribió las palabras: cuántas escribiste tú y cuántas generó un modelo, en palabras y en porcentaje, con los modelos. Algunas editoriales y concursos la piden. Se calcula a partir del [historial de versiones](version-history.md) de las partes exportadas (véase [Resaltado del texto generado](story-editor.md#highlighting-generated-text)). No se ofrece para el chat de SillyTavern. El texto es una plantilla en [Ajustes → Exports](../account.md#exports). |

![La exportación terminada, lista para descargar](../../images/export-download.png)

<a id="formats"></a>

## Formatos

| Formato | |
|---|---|
| **Plain text** (`.txt`) | Solo el texto en UTF-8. Se pierde el formato, como la *cursiva*, y los párrafos se separan con una línea vacía. |
| **Markdown** (`.md`) | El texto tal como está escrito en la historia, para editores como Obsidian o Typora, un sitio estático o un repositorio. El énfasis, los encabezados, las citas, las listas, los bloques de código y los cambios de escena se mantienen en Markdown. El HTML en bruto del texto se conserva tal como se escribió; el lector de Markdown decide si lo muestra. |
| **HTML** (`.html`) | Una sola página con un estilo de libro incluido, para que se vea igual en cualquier navegador. Se puede imprimir y no tiene archivos externos. |
| **Documento de Word** (`.docx`) | Se abre en Word, LibreOffice y programas similares. Texto justificado, con el título y el autor en las propiedades del archivo. |
| **PDF** (`.pdf`) | Páginas A5 con tipografías incrustadas que cubren textos en latín, griego y cirílico, y una tipografía japonesa incluida (Noto Sans JP) para kana, kanji y signos de puntuación japoneses. El texto de otras escrituras (caracteres solo chinos, coreano, árabe) no se dibuja: el PDF no tiene tipografía para ello. |
| **EPUB** (`.epub`) | Un libro electrónico para lectores electrónicos y aplicaciones de lectura. Los libros largos se dividen en varios archivos dentro del EPUB para que los lectores vayan rápido. |
| **HTML para Archive of Our Own** (`.html`) | Para publicar en AO3, véase [más abajo](#for-other-tools). |
| **Markdown con front matter** (`.md`) | Para generadores de sitios estáticos, véase [más abajo](#for-other-tools). |
| **Chat de SillyTavern** (`.jsonl`) | La historia como un chat, véase [más abajo](#for-other-tools). |
| **Bóveda de Obsidian** (`.zip`) | Los capítulos y el libro de contexto como notas enlazadas, véase [más abajo](#for-other-tools). |
| **LaTeX** (`.zip`) | Un proyecto LaTeX del libro (`main.tex` y las imágenes), véase [más abajo](#for-other-tools). |

El texto de las partes es Markdown. El énfasis, los encabezados, las citas, las listas, los bloques de código y los cambios de escena (`---`) se conservan en todos los formatos que pueden mostrarlos. El HTML en bruto del texto se muestra como texto, nunca se ejecuta.

<a id="footnotes"></a>

## Notas al pie

Una nota al pie se escribe en el texto de una parte como `[^1]` donde debe aparecer la marca, y `[^1]: la nota` en una línea propia (la etiqueta puede ser cualquier palabra). Todos los formatos las admiten: **Markdown** conserva la sintaxis; **HTML**, **EPUB** y **HTML para Archive of Our Own** ponen un pequeño número elevado que enlaza con las notas bajo el texto de la parte (AO3 recibe el marcado simple que conserva su editor); **Plain text** escribe la marca como `[1]` y las notas como líneas que empiezan con `[1]` después del texto de la parte, que es el aspecto de una nota al final. **Word** y **PDF** tienen notas al pie reales: la marca es un pequeño número elevado, y la nota aparece al pie de la misma página, en un tipo más pequeño (en el PDF bajo una línea corta; las páginas se distribuyen hasta que cada una tenga el espacio que necesitan sus notas). Word numera y coloca las notas por sí mismo, así que se reajustan si editas el documento. Las extensiones también pueden usarlas: la extensión Conlang Translator escribe el inglés de cada intervención como nota al pie.

<a id="images"></a>

## Imágenes

Las [imágenes](story-editor.md#images) de las partes se colocan donde indica su posición (después del texto, salvo que hayas elegido otro lugar), con sus pies, en el ancho que les diste (un porcentaje del ancho del texto; sin ancho, la imagen conserva su tamaño, reducido para caber en la página). No todos los formatos pueden poner texto junto a una imagen:

| Formato | |
|---|---|
| **HTML**, **EPUB** | Incrustadas (en la página o como archivos del libro). El texto rodea las imágenes por la izquierda o por la derecha; una imagen en el centro queda entre dos columnas de texto. En una pantalla estrecha van encima del texto, y un lector sin columnas (algunos lectores electrónicos) muestra el primer texto, la imagen y el resto uno debajo de otro. Las imágenes contiguas en un mismo lugar se disponen en fila. |
| **Documento de Word** | La imagen que el texto rodea va en una pequeña tabla que Word coloca a un lado o en el centro, y Word envuelve el texto a su alrededor (por ambos lados si está en el centro, línea a línea); el pie va debajo de la imagen. Las imágenes con un ancho que caben una junto a otra forman una fila de una tabla. |
| **PDF** | El texto rodea las imágenes por el costado, y el pie se dibuja debajo de la imagen. Una imagen en el centro queda entre dos columnas de texto alineado a la izquierda (una franja que no cabe al final de una página empieza en la siguiente). Las imágenes con un ancho que caben una junto a otra forman una fila. Una imagen demasiado alta para el resto de la página pasa a la página siguiente. |
| **Markdown** | Incrustadas en el archivo como imágenes `![pie](data:...)`, con el pie en cursiva debajo, así que sigue siendo un solo archivo. Se conserva la posición entre el texto; no el ancho ni el texto que rodea la imagen. No todos los lectores muestran imágenes incrustadas (GitHub no lo hace), y un libro con muchas imágenes tiene líneas muy largas; por eso, incluye imágenes en las partes solo cuando las quieras en una exportación Markdown. |
| **Plain text** | Se marcan con `[Image]` o `[Image: pie]` en su lugar del texto. |
| **HTML para Archive of Our Own** | Se marcan con `[Image: pie]`: AO3 solo admite imágenes desde enlaces. |
| **Bóveda de Obsidian** | Se guardan en la carpeta `Attachments` de la bóveda e se incrustan en la nota de su capítulo, en su lugar, con el pie en cursiva. |
| **LaTeX** | Se guardan en la carpeta `images` del proyecto e se incluyen en el texto en su lugar (una figura con el ancho de la imagen, nunca más ancha que el texto) con el pie debajo. El texto no rodea las imágenes. |
| **Chat de SillyTavern** | Se omiten, porque un chat solo tiene texto. |
<a id="for-other-tools"></a>

## Para otras herramientas

Estos formatos están pensados para una herramienta que los lee, no para leerlos directamente. No tienen la opción *Estilo de impresión*.

**HTML para Archive of Our Own.** AO3 pide el título, el autor y los títulos de los capítulos en campos propios, y descarta lo que no reconoce (bloques de estilo, la página que rodea al texto, las imágenes que no son enlaces), así que el archivo no tiene portada. Ábrelo en un navegador para ver la obra como quedará al pegarla. Cada capítulo (una parte que empieza con un encabezado y las partes siguientes hasta el siguiente encabezado) tiene su título encima de dos comentarios, `BEGIN chapter N` y `END chapter N`: copia lo que hay entre ellos en el editor HTML del capítulo en AO3 y el título en su campo de título. El texto es HTML limpio, sin saltos de línea entre las etiquetas, porque AO3 convierte cada salto de línea en un `<br>`.

**Markdown con front matter.** El mismo Markdown que el formato *Markdown*, pero el archivo empieza con un bloque YAML (`title`, `author`, `lang`, `date` de la exportación) que leen Jekyll, Hugo, Eleventy y generadores similares, en lugar de una portada; **Incluir la portada** no tiene efecto en él. El bloque es una plantilla que puedes cambiar en [Settings → Exports](../account.md#exports) (`{{title}}`, `{{author}}`, `{{language}}`, `{{exportDate}}`; el título y el autor ya están escapados para usarse dentro de comillas dobles).

**Chat de SillyTavern.** Un archivo JSON Lines que lee la importación de chats de SillyTavern: el personaje recibe el nombre del libro y tú eres el autor. Cada parte de la historia es un mensaje del personaje (el texto), y cuando la parte se escribió con instrucciones, estas aparecen antes como un mensaje tuyo. El razonamiento del modelo, si la parte lo tiene, se conserva como razonamiento del mensaje. La portada no se aplica.

**Bóveda de Obsidian.** Un ZIP con una carpeta con el nombre del libro; descomprímelo y abre la carpeta como bóveda. Contiene una nota con el nombre del libro (el front matter, la portada y las listas de capítulos y de lore), una nota por cada capítulo en `Chapters` (con enlaces al capítulo anterior y al siguiente), una nota por cada entrada activa del [lorebook](../lorebooks/index.md) del libro y de sus sub lorebooks en `Lore/<lorebook>` (las etiquetas de la entrada son las etiquetas de la nota, y su comentario es un callout), y las imágenes en `Attachments`. En cada nota, la primera mención del nombre de una entrada (un nombre de al menos tres caracteres, palabras completas, tal como está escrito) es un enlace a su nota, de modo que la vista de grafo muestra en qué capítulo aparece cada entrada. Una parte con un encabezado inicia un capítulo; las partes anteriores al primer encabezado forman la nota *Beginning* (o *Historia* cuando el libro no tiene encabezados). El front matter y la portada son plantillas que puedes cambiar en [Settings → Exports](../account.md#exports).

**LaTeX.** Un ZIP con una carpeta con el nombre del libro. `main.tex` es un `book` que compila con `pdflatex`, `xelatex` o `lualatex`, y las imágenes de las partes están en `images`. La portada es un `titlepage` hecho con la [plantilla de la portada](../account.md#exports). El encabezado de nivel más alto de la historia es un capítulo, el nivel siguiente una sección, y así sucesivamente; no están numerados (una historia nombra sus capítulos por sí misma), pero se listan en un índice. El énfasis, las citas, las listas, los bloques de código y los cambios de escena se convierten en lo que son en LaTeX, las notas al pie son notas al pie reales, los caracteres que significan algo para LaTeX se escriben como texto, y el idioma del libro elige la separación silábica (`babel`) cuando es uno de los comunes. Un libro con texto en japonés, chino o coreano necesita `xelatex` o `lualatex` (`pdflatex` se detiene con un mensaje): el archivo carga `xeCJK` o `luatexja` para ello, y con `xelatex` debes definir una fuente que tenga tus caracteres (la línea que hay que cambiar está en el comentario encima, por ejemplo `\setCJKmainfont{Noto Serif CJK JP}`); si no, solo se usa la fuente china que trae `xeCJK`. Sube el ZIP a [Overleaf](https://www.overleaf.com) o descomprímelo y ejecuta `pdflatex main.tex` (dos veces, para el índice). El estilo de impresión no se aplica: el aspecto es el de la clase, cámbialo en el preámbulo de `main.tex`.

<a id="print-styles"></a>

## Estilos de impresión

La exportación usa los estilos de texto de [Settings → Styles](../account.md#styles) marcados como **For exports**. Los estilos son para libros (la historia y el lector) o para exportaciones: el menú *Cambiar los estilos del texto* y el lector muestran solo el primer tipo, y el diálogo de exportación solo el segundo. Los cuatro predeterminados son los aspectos de los formatos; **Duplicar** uno (o un estilo de libro, y marcar **For exports**) para crear el tuyo, por ejemplo el estilo Libro en papel. El estilo se calcula pensando en el papel: los colores que dependen del tema de la aplicación (el texto claro del tema oscuro) pasan a ser los oscuros para una página blanca.

| Formato | Qué aporta el estilo |
|---|---|
| **HTML** | Toda la hoja de estilos del estilo, incluido el **CSS personalizado**, las **letras capitulares**, el ancho del texto y el fondo; la portada y el índice conservan el aspecto del formato. |
| **EPUB** | La hoja de estilos del estilo sin el CSS personalizado (no todos los lectores electrónicos lo entienden; algunos lectores también definen sus propias fuentes y tamaños). |
| **PDF** | La familia tipográfica (serif, sans o monoespaciada de las fuentes incrustadas, elegida desde la pila de fuentes), el tamaño, la altura de línea, la alineación, el espacio entre párrafos, la sangría de la primera línea, el **color de fondo** de la página (toda la página, incluida la portada), los colores del texto, la cursiva, la negrita, las citas, los encabezados y el cambio de escena, la alineación y el tamaño de los encabezados, y el símbolo del cambio de escena. |
| **Documento de Word** | Lo mismo que el PDF, con la fuente indicada en el estilo (Word muestra la suya propia si el ordenador no la tiene) en lugar de las incrustadas. El fondo es el color de la página: Word lo muestra en pantalla, pero no lo imprime a menos que *Print background colors and images* esté activado en sus opciones de impresión. |

Las letras capitulares, el CSS personalizado (PDF, Word, EPUB), las versalitas de los encabezados y el ancho del texto no se generan en los archivos PDF y Word.

<a id="title-page-template"></a>

## Plantilla de la portada

La portada se escribe a partir de una plantilla, una para cada formato. Cámbiala en [Settings → Exports](../account.md#exports): la plantilla es Markdown con `{{title}}` (el título del libro en el diálogo) y `{{author}}`, por ejemplo `## {{title}}` y una línea `written by {{author}}`. El valor predeterminado es el título como encabezado y el autor en cursiva.

Las mismas plantillas (la portada, el front matter, la declaración de autoría) pueden cambiarse **para un solo libro o para una carpeta**: en la pestaña *Prompts* del libro y en la ventana de la carpeta, **Ajustes de la extensión** tiene un panel para cada formato de exportación, con un campo para cada una de sus plantillas. Se usa primero la versión propia del libro, si no la de la carpeta más cercana por encima, si no la tuya de *Settings → Exports*, si no la predeterminada, de modo que una serie puede tener su propia portada. Un campo vacío hereda el valor, e indica de dónde procede.
<a id="table-of-contents"></a>

## Tabla de contenidos

Cuando el texto contiene capítulos, el archivo incluye una tabla de contenidos después de la portada. Un capítulo es una parte de la historia que contiene un encabezado Markdown (una línea que empieza por `#`, por ejemplo `# Chapter 3: The Harbour`), la misma regla que usa el complemento [Chapter Marker](../extensions/chapter-marker.md) en la barra lateral de la historia. El primer encabezado de una parte es su título; las partes sin encabezado pertenecen al capítulo anterior. Una historia sin ningún encabezado no tiene tabla de contenidos.

![La portada y la tabla de contenidos de un PDF exportado](../../images/export-result.png)

En PDF, Word y EPUB, cada capítulo empieza en una página nueva. La portada y la tabla de contenidos también ocupan cada una su propia página.

Las entradas son enlaces a los capítulos:

| Formato | |
|---|---|
| **HTML** | Enlaza con los capítulos de la página. |
| **PDF** | Entradas en las que se puede hacer clic, con la página del capítulo; además, los capítulos aparecen en el esquema (marcadores) que muestra el lector de PDF junto a las páginas. |
| **EPUB** | La tabla de contenidos del lector, que también es una página del libro después de la portada. |
| **Documento de Word** (`.docx`) | Entradas en las que se puede hacer clic, con la página del capítulo. Word pide actualizar los campos al abrir el archivo; responde **Sí** para rellenar los números de página (LibreOffice los rellena por sí mismo). Es una lista de enlaces y no un campo de tabla de contenidos de Word. |
| **Markdown** | No hay tabla de contenidos. Los encabezados de los capítulos permanecen en el texto, y la mayoría de editores Markdown crean un esquema a partir de ellos. |
| **Texto plano** | No hay tabla de contenidos. |

<a id="page-numbers"></a>

## Números de página

Los archivos PDF y Word muestran el número de página en el pie de cada página, salvo en la portada. El número es la posición de la página dentro del archivo, así que la portada es la página 1 y la primera página después de ella es la 2. Los demás formatos no tienen páginas propias.

<a id="the-title-page"></a>

## La portada

Con **Incluir la portada**, el archivo empieza con el título del libro y el autor en una página propia. La página se genera a partir de una plantilla integrada (`DEFAULT_EXPORT_HEADER_TEMPLATE`), que todavía no se puede editar en la aplicación.

## Good to know

- La exportación es una instantánea de la rama en el momento en que eliges **Exportar**. Las partes generadas o editadas después no están en el archivo.
- El archivo exportado solo se conserva hasta que cierras el diálogo de descarga. Vuelve a exportar si lo necesitas más adelante.
- Los resúmenes, los lorebooks, las instrucciones y el razonamiento del modelo no se exportan; solo el texto y las imágenes de las partes. Para guardarlo todo, haz una [copia de seguridad](backups.md).
