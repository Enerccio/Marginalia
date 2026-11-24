---
label: Recursos
order: 960
icon: image
---

# Recursos

La pestaña **Resources** enumera los archivos que has subido a Marginalia: las [imágenes de las partes](books/story-editor.md#images) de tus libros y el audio que extensiones como [Audiobook](extensions/audiobook.md) guardan junto con ellos. La pestaña sirve para volver a encontrar un archivo, escuchar audio, descargarlo, sustituirlo por uno mejor sin tocar los libros que lo usan y eliminar lo que ya no necesitas.

![La pestaña Resources](../images/resources-tab.png)

Los archivos se agrupan según lo que los usa: cada fila de la tabla es un objeto (por ejemplo, una parte de un libro, `ChatMessage #123`) con el número de sus archivos, su tamaño total y la fecha del más reciente. Haz clic en la flecha de la fila para abrir el objeto y ver sus archivos. La tabla se carga a medida que haces scroll y que abres un grupo, así que sigue siendo rápida con miles de archivos. Haz clic en el encabezado de una columna para ordenar por ella; los grupos y los archivos que contienen la siguen. Por defecto, los más recientes aparecen primero.

| Row / column | |
|---|---|
| **Used by** | El objeto al que se añadieron los archivos. *nothing* agrupa los archivos que no están usados por nada. *(gone)* tras el nombre indica que el objeto fue eliminado o ya no existe; probablemente los archivos ya no hacen falta: la [limpieza](administration/cleanup.md) del administrador los elimina cuando nada más los usa. Dentro de un grupo: una pequeña vista previa de una imagen y el nombre del archivo tal como se subió. |
| **Type** | El tipo del archivo, por ejemplo `image/png`. |
| **Size** | El tamaño del archivo; en un grupo, la suma de sus archivos. |
| **Created** | Cuándo se subió el archivo; en un grupo, el archivo más reciente. |

Esta es una nota para ti. Una imagen sigue funcionando después de que la parte a la que se añadió haya desaparecido, y la misma imagen puede ser usada por varias partes (por ejemplo, tras [bifurcar](books/branches-and-story-tree.md) la historia, la rama tiene las mismas imágenes que la parte de la que se creó); la limpieza conserva un archivo mientras alguna parte todavía lo use.

<a id="play"></a>

## Reproducir

Un archivo de tipo audio (`audio/mpeg`, `audio/wav`...) tiene un botón **Play** (triángulo) delante de los demás botones. Reproduce el archivo en la página; el botón se convierte en **Pause** mientras suena y vuelve a Play cuando lo detienes o termina el audio. Solo suena un archivo a la vez: al empezar otro, se detiene el que estaba sonando.

<a id="download"></a>

## Descargar

El botón de descarga (flecha hacia abajo) de la fila guarda el archivo en tu ordenador.

<a id="replace-the-content"></a>

## Reemplazar el contenido

El botón de subida de la fila abre un diálogo en el que eliges un archivo nuevo. El nuevo contenido se guarda como un archivo nuevo y la entrada ahora apunta a él, así que **todo lo que usa la entrada muestra el nuevo contenido**; no tienes que cambiar las partes. Úsalo para sustituir una imagen por una versión mejor o para corregir una subida equivocada.

- Una imagen solo puede reemplazarse por otra imagen (PNG, JPEG, GIF o WebP, de hasta 10 MB, como al [añadir imágenes](books/story-editor.md#images)).
- El nombre de la entrada pasa a ser el nombre del nuevo archivo.
- El archivo anterior no se borra de la carpeta de datos, ver [más abajo](#what-delete-does).

![Reemplazar el contenido de un recurso](../images/resources-replace.png)

<a id="deleting"></a>

## Eliminar

El botón de papelera de la fila elimina un archivo. Para eliminar varios, márcalos en la primera columna y elige **Delete selected** encima de la tabla. Marcar un grupo (una fila de un objeto) elimina todos sus archivos. La casilla del encabezado selecciona todos los grupos. Marginalia pide confirmación antes de eliminar.

<a id="what-delete-does"></a>

### Qué hace la eliminación

- El archivo desaparece de la tabla y de las partes que lo usan: una parte simplemente deja de mostrar la imagen. El texto, los resúmenes y las exportaciones de la parte no cambian.
- La entrada solo queda **marcada como eliminada**. La [limpieza](administration/cleanup.md) de la administración la quita de la base de datos de forma definitiva.
- **El archivo sigue en la carpeta de datos** (`data/<your login>/images`), también después de la limpieza. Eliminar un recurso no libera espacio en disco por sí mismo: un administrador quita los archivos que ya nada usa en la pestaña [Resource Files](administration/resource-files.md) de la administración.
- Mientras la limpieza no elimine la entrada, puedes recuperarla desde la [Trash](trash.md): el archivo sigue en la carpeta de datos, así que la imagen vuelve a funcionar. Después de la limpieza, solo una [copia de seguridad de toda la base de datos](administration/database-backups.md) hecha antes contiene la entrada; el archivo en sí sigue en la carpeta.
