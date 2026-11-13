---
label: Recursos
order: 960
icon: image
---

# Recursos

La pestaña **Recursos** muestra los archivos que has subido a Marginalia: las [imágenes de las partes](books/story-editor.md#images) de tus libros y el audio que extensiones como [Audiobook](extensions/audiobook.md) guardan junto a ellos. La pestaña sirve para encontrar un archivo de nuevo, escuchar audio, descargarlo, sustituirlo por una versión mejor sin tocar los libros que lo usan y eliminar lo que ya no necesitas.

![La pestaña Recursos](../images/resources-tab.png)

Los archivos se agrupan según lo que los usa: cada fila de la tabla es un objeto (por ejemplo, una parte de un libro, `ChatMessage #123`) con el número de sus archivos, su tamaño total y la fecha del más reciente. Haz clic en la flecha de la fila para abrir el objeto y ver sus archivos. La tabla se carga al desplazarte y al abrir un grupo, así que sigue siendo rápida aunque tenga miles de archivos. Haz clic en el encabezado de una columna para ordenar por ella; los grupos y los archivos que contienen se ordenan igual. Por defecto, los más recientes aparecen primero.

| Fila / columna | |
|---|---|
| **Usado por** | El objeto al que se añadieron los archivos. *nada* agrupa los archivos que no usa nada. *(gone)* junto al nombre indica que el objeto se eliminó o ya no existe; probablemente los archivos ya no sean necesarios. Dentro de un grupo: una vista previa pequeña de una imagen y el nombre del archivo tal como se subió. |
| **Tipo** | El tipo del archivo, por ejemplo `image/png`. |
| **Tamaño** | El tamaño del archivo; en un grupo, la suma de sus archivos. |
| **Creado** | Cuándo se subió el archivo; en un grupo, el archivo más reciente. |

Esto es solo una nota para ti. Marginalia no la aplica: una imagen sigue funcionando aunque la parte a la que se añadió ya no exista, y la misma imagen puede usarla más de una parte (por ejemplo, después de [ramificar](books/branches-and-story-tree.md) la historia, la rama tiene las mismas imágenes que la parte de la que se creó).

<a id="play"></a>

## Reproducir

Un archivo de tipo audio (`audio/mpeg`, `audio/wav`...) tiene un botón **Reproducir** (triángulo) delante de los demás botones. Reproduce el archivo en la página; el botón se convierte en **Pausar** mientras suena y vuelve a Reproducir cuando lo detienes o termina el audio. Solo se reproduce un archivo a la vez: al iniciar otro, se detiene el que sonaba.

<a id="download"></a>

## Descargar

El botón de descarga (flecha hacia abajo) de la fila guarda el archivo en tu ordenador.

<a id="replace-the-content"></a>

## Reemplazar el contenido

El botón de subida de la fila abre un diálogo en el que eliges un archivo nuevo. El contenido nuevo se guarda como un archivo nuevo y la entrada pasa a apuntar a él, de modo que **todo lo que usa la entrada muestra el contenido nuevo**; no tienes que cambiar las partes. Úsalo para sustituir una imagen por una versión mejor o para corregir una subida equivocada.

- Una imagen solo puede reemplazarse por otra imagen (PNG, JPEG, GIF o WebP, de hasta 10 MB, como al [añadir imágenes](books/story-editor.md#images)).
- El nombre de la entrada pasa a ser el nombre del archivo nuevo.
- El archivo anterior no se elimina de la carpeta de datos; consulta [más abajo](#what-delete-does).

![Reemplazar el contenido de un recurso](../images/resources-replace.png)

<a id="deleting"></a>

## Eliminar

El botón de papelera de la fila elimina un archivo. Para eliminar varios, marca sus casillas en la primera columna y elige **Eliminar la selección** encima de la tabla. Marcar un grupo (una fila de un objeto) elimina todos sus archivos. La casilla del encabezado selecciona todos los grupos. Marginalia te pide confirmación antes de eliminar.

<a id="what-delete-does"></a>

### Qué hace eliminar

- El archivo desaparece de la tabla y de las partes que lo usan: una parte simplemente deja de mostrar la imagen. El texto, los resúmenes y las exportaciones de la parte no cambian.
- La entrada solo se **marca como eliminada**. La [limpieza](administration/cleanup.md) de la administración la quita de la base de datos de forma definitiva.
- **El archivo permanece en la carpeta de datos** (`data/<tu inicio de sesión>/images`), incluso después de la limpieza. Eliminar un recurso no libera espacio en disco por sí solo: un administrador elimina los archivos que ya no usa nada desde la pestaña [Archivos de recursos](administration/resource-files.md) de la administración.
- Hasta que la limpieza quite la entrada, puedes recuperarla desde la [Papelera](trash.md): el archivo sigue en la carpeta de datos, así que la imagen vuelve a funcionar. Después de la limpieza, solo contiene la entrada una [copia de seguridad de toda la base de datos](administration/database-backups.md) hecha antes; el archivo en sí sigue en la carpeta.
