---
label: Archivos de recursos
order: 85
---

# Archivos de recursos

Los archivos de [recursos](../resources.md) (las imágenes que los usuarios adjuntan a las partes de la historia, los archivos subidos) se guardan en la
carpeta de datos de cada usuario, en `data/<login>/images` y `data/<login>/resources`. Al eliminar un recurso, o al reemplazar su
contenido, solo cambia la base de datos: el archivo permanece en el disco, y ni la [limpieza](cleanup.md) ni ninguna otra acción
lo elimina. **Archivos de recursos** encuentra los archivos que ya no usa nada, permite descargarlos y eliminarlos del
disco para recuperar el espacio.

<a id="cleaning-up-the-files"></a>

## Limpiar los archivos

1. Abre la pestaña **Archivos de recursos** de la página de administración y haz clic en **Analizar archivos**. Una barra de progreso muestra las carpetas que se están leyendo.
   No se cambia nada.
2. La tabla lista los archivos **a los que no apunta ningún recurso**: la ruta (`<user folder>/images/<file>`), el tamaño y la fecha de su
   última modificación. Todos están marcados. Desmarca los que quieras conservar; **Seleccionar todo** y **No seleccionar nada** cambian todos
   ellos, y la flecha del encabezado de la primera columna hace lo mismo. Una línea sobre la tabla indica cuántos de los archivos
   no se usan, su tamaño y cuántos están marcados.
3. Opcionalmente, haz clic en **Descargar como ZIP** para obtener los archivos marcados en un único archivo comprimido (las entradas se nombran según su ruta), o en la
   flecha de una fila para un único archivo. Esa es la copia de seguridad que conviene guardar antes de eliminar.
4. **Eliminar la selección** borra del disco los archivos marcados tras confirmarlo. **Cerrar** vacía la tabla sin
   eliminar nada.

![La pestaña Archivos de recursos tras un análisis](../../images/admin-resource-files.png)

!!!warning
Los archivos eliminados no se pueden recuperar. Si tienes dudas, descárgalos primero como ZIP.
!!!

<a id="what-counts-as-used"></a>

## Qué se considera en uso

Un archivo está en uso cuando **cualquier recurso de cualquier usuario apunta a él**, incluido uno eliminado: un recurso en la
[Papelera](../trash.md) se puede restaurar, y su imagen tiene que volver a funcionar. Por eso, los archivos de los recursos eliminados solo aparecen
en la lista después de que la [limpieza](cleanup.md) haya quitado los recursos de la base de datos, junto con los archivos del contenido que
se reemplazó (el archivo antiguo se queda) y los archivos que dejaron las subidas fallidas.

Los archivos se comparan por nombre (un identificador aleatorio), en las carpetas de todos los usuarios, también en las carpetas `<login>-deleted` de los
usuarios eliminados: sus archivos siguen *en uso* hasta que la limpieza purgue al usuario y a sus recursos.

Solo se leen las carpetas `images` y `resources` dentro de la carpeta de datos, nada más (las copias de seguridad de la base de datos, los
espacios de trabajo de los agentes y demás no se tocan).

Un archivo que un usuario sube después del análisis no se elimina: **Eliminar la selección** vuelve a leer la base de datos y conserva los
archivos a los que apunten recursos en ese momento (el mensaje indica cuántos archivos se conservaron).

El análisis lee la carpeta de datos, lo que puede tardar con muchos archivos; **Detener** en la barra de progreso lo cancela.
