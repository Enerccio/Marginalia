---
label: Archivos de recursos
order: 85
---

# Archivos de recursos

Los archivos de [recursos](../resources.md) (las imágenes que los usuarios adjuntan a las partes de la historia, los archivos subidos) se guardan en la
carpeta de datos de cada usuario, en `data/<login>/images` y `data/<login>/resources`. Eliminar un recurso, o reemplazar su
contenido, solo cambia la base de datos: el archivo permanece en el disco, y ni la [limpieza](cleanup.md) ni ninguna otra acción
lo elimina. **Archivos de recursos** encuentra los archivos que ya nadie usa, permite descargarlos y los elimina del
disco para recuperar espacio.

<a id="cleaning-up-the-files"></a>

## Limpieza de archivos

1. Abre la pestaña **Archivos de recursos** de la página de administración y haz clic en **Analizar archivos**. Una barra de progreso muestra las carpetas que se están leyendo.
   No se modifica nada.
2. La tabla enumera los archivos **a los que no apunta ningún recurso**: la ruta (`<carpeta del usuario>/images/<archivo>`), el tamaño y la fecha
   de la última modificación. Todos están marcados. Desmarca los que quieras conservar; **Seleccionar todo** y **No seleccionar nada** cambian todos
   ellos, y la flecha de la cabecera de la primera columna hace lo mismo. Una línea sobre la tabla indica cuántos archivos
   no se usan, su tamaño y cuántos están marcados.
3. Opcionalmente, haz clic en **Descargar como ZIP** para obtener los archivos marcados en un único archivo comprimido (las entradas se nombran por su ruta), o en la
   flecha de una fila para descargar un solo archivo. Es la copia de seguridad que conviene guardar antes de eliminar.
4. **Eliminar la selección** borra del disco los archivos marcados tras confirmarlo. **Cerrar** vacía la tabla sin eliminar nada.

![La pestaña Archivos de recursos tras un análisis](../../images/admin-resource-files.png)

!!!warning
Los archivos eliminados no se pueden recuperar. Descárgalos como ZIP primero si tienes dudas.
!!!

<a id="what-counts-as-used"></a>

## Qué se considera usado

Un archivo está en uso cuando **cualquier recurso de cualquier usuario apunta a él**, incluso si el recurso está eliminado: un recurso de la
[papelera](../trash.md) se puede restaurar, y su imagen debe volver a funcionar. Por eso, los archivos de recursos eliminados solo aparecen
en la lista después de que la [limpieza](cleanup.md) haya quitado los recursos de la base de datos, junto con los archivos del contenido que
fue reemplazado (el archivo antiguo queda atrás) y los archivos dejados por subidas fallidas.

Los archivos se comparan por nombre (un identificador aleatorio), en las carpetas de todos los usuarios, incluidas también las carpetas `<login>-deleted` de
los usuarios eliminados: sus archivos siguen *en uso* hasta que la limpieza elimine por completo al usuario y sus recursos.

Solo se leen las carpetas `images` y `resources` de las carpetas de la carpeta de datos, nada más (las copias de seguridad de la base de datos, los
espacios de trabajo de los agentes y demás no se tocan).

Un archivo que un usuario sube después del análisis no se elimina: **Eliminar la selección** vuelve a leer la base de datos y conserva los
archivos a los que apuntan los recursos en ese momento (el mensaje indica cuántos archivos se han conservado).

El análisis lee la carpeta de datos, lo que puede tardar con muchos archivos; **Detener** en la barra de progreso lo cancela.
