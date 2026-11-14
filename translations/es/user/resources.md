---
label: Recursos
order: 960
icon: image
---

# Recursos

La pestaña **Recursos** enumera los archivos que has subido a Marginalia: las [imágenes de las partes](books/story-editor.md#images) de tus libros y el audio que extensiones como [Audiobook](extensions/audiobook.md) guardan junto a ellos. Esta pestaña sirve para volver a encontrar un archivo, escuchar el audio, descargarlo, sustituirlo por uno mejor sin tocar los libros que lo usan y eliminar lo que ya no necesitas.

![La pestaña Recursos](../images/resources-tab.png)

Los archivos se agrupan según lo que los usa: cada fila de la tabla es un objeto (por ejemplo, una parte de un libro, `ChatMessage #123`) con el número de sus archivos, su tamaño total y la fecha del más reciente. Haz clic en la flecha de la fila para abrir el objeto y ver sus archivos. La tabla se carga a medida que haces scroll y al abrir un grupo, así que sigue siendo rápida aunque tenga miles de archivos. Haz clic en el encabezado de una columna para ordenar por ella; los grupos y los archivos que contienen la siguen. Por defecto, los más recientes aparecen primero.

| Fila / columna | |
|---|---|
| **Usado por** | El objeto al que se añadieron los archivos. *nada* agrupa los archivos que no usa ningún objeto. *(eliminado)* junto al nombre significa que el objeto fue borrado o ya no existe; probablemente los archivos ya no hacen falta. Dentro de un grupo: una pequeña vista previa de una imagen y el nombre del archivo tal como se subió. |
| **Tipo** | El tipo del archivo, por ejemplo `image/png`. |
| **Tamaño** | El tamaño del archivo; en un grupo, la suma de sus archivos. |
| **Creado** | Cuándo se subió el archivo; en un grupo, el archivo más reciente. |

Esto es solo una nota para ti. Marginalia no la aplica: una imagen sigue funcionando aunque la parte a la que se añadió ya no exista, y la misma imagen puede usarla varias partes (por ejemplo, después de [bifurcar](books/branches-and-story-tree.md) la historia, la bifurcación tiene las mismas imágenes que la parte de la que procede).

<a id="play"></a>

## Reproducir

Un archivo de tipo audio (`audio/mpeg`, `audio/wav`...) tiene un botón **Reproducir** (triángulo) delante de los demás botones. Reproduce el archivo en la página; el botón se convierte en **Pausar** mientras suena y vuelve a Reproducir cuando lo detienes o cuando termina el audio. Solo suena un archivo a la vez: al iniciar otro, se detiene el que estaba sonando.

<a id="download"></a>

## Descargar

El botón de descarga (flecha hacia abajo) de la fila guarda el archivo en tu ordenador.

<a id="replace-the-content"></a>

## Sustituir el contenido

El botón de subida de la fila abre un diálogo en el que eliges un archivo nuevo. El nuevo contenido se guarda como un archivo nuevo y el registro pasa a apuntar a él, de modo que **todo lo que usa el registro muestra el contenido nuevo**; no tienes que cambiar las partes. Úsalo para sustituir una imagen por una versión mejor o para corregir una subida equivocada.

- Una imagen solo puede sustituirse por otra imagen (PNG, JPEG, GIF o WebP, de hasta 10 MB, como al [añadir imágenes](books/story-editor.md#images)).
- El nombre del registro pasa a ser el nombre del nuevo archivo.
- El archivo anterior no se elimina de la carpeta de datos; véase [más abajo](#what-delete-does).

![Sustituir el contenido de un recurso](../images/resources-replace.png)

<a id="deleting"></a>

## Eliminar

El botón de papelera de la fila elimina un archivo. Para eliminar varios, márcalos en la primera columna y elige **Eliminar la selección** encima de la tabla. Marcar un grupo (una fila de un objeto) elimina todos sus archivos. La casilla del encabezado selecciona todos los grupos. Marginalia te pide confirmación antes de eliminar.

<a id="what-delete-does"></a>

### Qué hace eliminar

- El archivo desaparece de la tabla y de las partes que lo usan: una parte simplemente deja de mostrar la imagen. El texto, los resúmenes y las exportaciones de la parte no cambian.
- El registro solo se **marca como eliminado**. La [limpieza](administration/cleanup.md) de la administración lo quita de la base de datos de forma definitiva.
- **El archivo permanece en la carpeta de datos** (`data/<your login>/images`), también después de la limpieza. Eliminar un recurso no libera espacio en disco por sí mismo: un administrador retira los archivos que ya no usa nada en la pestaña [Archivos de recursos](administration/resource-files.md) de la administración.
- Mientras la limpieza no haya quitado el registro, puedes recuperarlo desde la [Papelera](trash.md): el archivo sigue en la carpeta de datos, así que la imagen vuelve a funcionar. Después de la limpieza, solo una [copia de seguridad de toda la base de datos](administration/database-backups.md) realizada antes contiene el registro; el archivo en sí sigue en la carpeta.
