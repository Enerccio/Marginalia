---
label: Libros
order: 960
icon: book
---

# Libros

Un libro es una historia. Le das instrucciones parte a parte, el modelo escribe el texto, y el libro conserva todo lo que lo acompaña: el árbol de la historia con todas sus ramas, los prompts que dan forma al estilo, el lorebook con el mundo y los personajes, los resúmenes y las copias de seguridad.

![Un libro en el editor de historia](../../images/hero-story-editor.png)

<a id="parts-and-branches"></a>

## Partes y ramas

Un libro se compone de **partes**. Cada parte es una respuesta del modelo, escrita a partir de las instrucciones que le diste para ella (escena, personaje con el punto de vista, personajes presentes y lo que debe pasar).

Las partes forman un árbol. Normalmente solo continúas la historia, pero también puedes escribir otra versión de la última parte, o volver atrás y continuar de otra forma desde cualquier parte anterior. Cada camino desde el principio hasta un final es una **rama**. La rama en la que trabajas es la **rama activa**; el editor de historia la muestra, y solo sus partes se envían al modelo. Ver [Ramas y árbol de la historia](branches-and-story-tree.md).

<a id="the-book-window"></a>

## La ventana del libro

Al abrir un libro se muestra su ventana con estas pestañas:

| Pestaña | |
|---|---|
| **Información** | Nombre, etiquetas, descripción, idioma, modelo, protocolo, publicación y recuento de palabras. Ver [Gestión de libros](managing-books.md). |
| **Prompts** | Las plantillas y ajustes de estilo de este libro. Ver [Prompts del libro](prompts.md). |
| **Lorebook** | El lorebook de este libro y sus entradas. Cuando tiene sublorebooks, el segundo cuadro **Lorebook mostrado** cambia la pestaña a cualquiera de ellos (y de vuelta) sin cambiar el lorebook del libro; hacer clic en el nombre de un sublorebook en **Sublorebooks** hace lo mismo. Ver [Lorebooks](../lorebooks/index.md). |
| **Historia** | El editor de historia, donde se escriben y generan las partes. Ver [Editor de historia](story-editor.md). |
| **Vista de ramas** | El árbol de la historia con todas las ramas. Ver [Ramas y árbol de la historia](branches-and-story-tree.md). |
| **Copias de seguridad** | Copias de seguridad del libro. Ver [Copias de seguridad del libro](backups.md). |

Lo que le ocurrió a un libro día a día (palabras, tiempo, tokens, tamaño) y el calendario de los días en que escribiste están en su ventana **Estadísticas**; ver [Estadísticas de escritura](statistics.md).

La historia puede sacarse de Marginalia como archivo desde el menú de ajustes de la pestaña *Historia*; ver [Exportar un libro](export.md).

Un libro sin partes se abre en *Información*, uno con partes en *Historia*. Todos los cambios se guardan al momento; no hay botón *Guardar*. **Salir** cierra el libro.

Mientras se genera una parte, las demás pestañas y *Salir* están desactivadas hasta que termine la generación o la detengas.

<a id="in-this-section"></a>

## En esta sección

- [Gestión de libros](managing-books.md) - la lista de libros, crear, editar y borrar libros
- [Carpetas](folders.md) - organizar libros en carpetas, ajustes que heredan los libros, mover y copiar
- [Editor de historia](story-editor.md) - escribir instrucciones, generar y editar partes
- [Ramas y árbol de la historia](branches-and-story-tree.md) - regenerar, deslizar, bifurcar, la vista de árbol
- [Historial de versiones](version-history.md) - las versiones anteriores de una parte: comparar, restaurar, nombrar, buscar
- [Prompts del libro](prompts.md) - plantillas, punto de vista, tiempo verbal y estilo
- [Resúmenes](summaries.md) - mantener los libros largos dentro del contexto del modelo
- [Exportar un libro](export.md) - la historia como texto, Markdown, HTML, Word, PDF o EPUB
- [Copias de seguridad del libro](backups.md) - copias de seguridad, restaurar, copias de un libro
- [Publicación y visor](publishing.md) - vista de lectura y compartir un libro
