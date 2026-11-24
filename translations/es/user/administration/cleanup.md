---
label: Limpieza
order: 80
---

# Limpieza

Cuando los usuarios eliminan algo en Marginalia (un libro, una parte de historia, un lorebook o un proveedor de inferencia), desaparece
de sus listas, pero sigue en la base de datos. Así, eliminar es seguro y las referencias siguen funcionando (un libro sigue
conociendo el proveedor que escribió sus partes). Mientras tanto, los usuarios pueden restaurar sus objetos eliminados desde la pestaña
[Papelera](../trash.md). **Cleanup** elimina definitivamente esos datos borrados, para mantener la base de datos pequeña.

![La pestaña Cleanup](../../images/admin-cleanup.png)

<a id="running-a-cleanup"></a>

## Ejecutar una limpieza

!!!warning
La limpieza no se puede deshacer, y lo que elimina también desaparece de la [Papelera](../trash.md). [Crea una copia de seguridad de la base de datos](database-backups.md) antes.
!!!

1. Abre la pestaña **Cleanup** de la página de administración y haz clic en **Analyze**. Todavía no se cambia nada.
2. Revisa el resultado (ver más abajo).
3. Haz clic en **Run Cleanup** y confirma *n objects will be permanently removed*.

La limpieza abarca los datos de todos los usuarios.

La limpieza borra entradas de la base de datos, y solo los archivos de los [agentes](../agents.md): el espacio de trabajo de un agente purgado y la carpeta de una ejecución purgada. Los archivos de los [recursos](../resources.md) eliminados y no usados (imágenes de las partes, audio) permanecen en la carpeta de datos de su propietario hasta que los quites en la pestaña [Resource Files](resource-files.md).

<a id="seeing-what-is-deleted"></a>

## Ver qué está eliminado

**Trash** debajo de las tablas lista los objetos eliminados de todos los usuarios, una fila por objeto (ábrelo para cargar la tabla),
para que puedas ver lo que una limpieza eliminaría y restaurar lo que alguien te pidió recuperar. Es la misma tabla
que la pestaña [Trash](../trash.md), incluidos los filtros por usuario y tipo y el enlace *Extended Content*.

![La sección Trash de la pestaña Cleanup](../../images/admin-trash.png)

<a id="reading-the-analysis"></a>

## Leer el análisis

La primera tabla tiene una fila por cada tipo de datos:

| Columna | |
|---|---|
| **Entity** | El tipo de datos: `Manuscript` (libros), `ChatMessage` (partes de historia), `Lorebook`, `LorebookEntry`, `AI` (proveedores de inferencia), `Protocol`, `Summary`, `Tag`, `TagRelation`, `User`... |
| **Deleted** | Cuántos están eliminados. |
| **To purge** | Cuántos se quitarán con *Run Cleanup*, incluidos los datos que pertenecen a objetos eliminados. |
| **Blocked** | Eliminados, pero todavía en uso, así que se conservan. |

**Deleted objects still in use** enumera los objetos bloqueados con el nombre y **Referenced by**: lo que todavía los usa.

<a id="what-is-removed"></a>

## Qué se elimina

La limpieza quita los objetos eliminados que ya no usa nada activo, junto con los datos que pertenecen solo a ellos:

| Eliminado | Se elimina junto con él |
|---|---|
| un libro | todas sus partes de historia, resúmenes y enlaces de etiquetas |
| un lorebook | sus entradas y enlaces de etiquetas; otros lorebooks ya no lo incluyen como lorebook secundario |
| una parte de historia | la parte (sus hijas ya se movieron a su padre) y su resumen |
| un proveedor de inferencia o un protocolo | se borra de los valores predeterminados de los usuarios en *Settings* |
| una etiqueta | sus enlaces con libros y entradas |
| un usuario | todo lo que poseía: libros (con sus partes de historia y resúmenes), lorebooks, proveedores de inferencia, protocolos, etiquetas y ajustes |

La limpieza también quita los [recursos](../resources.md) que ya no usa nada, aunque nadie los haya eliminado: archivos
añadidos a un objeto (el audio de un [audiolibro](../extensions/audiobook.md), una imagen de una parte) cuyo objeto ya no está
en la base de datos, o que esta limpieza elimina, cuando ningún otro objeto los nombra tampoco. Una rama creada a partir de la parte
tiene las mismas imágenes y clips, así que se conservan mientras exista la rama; también los archivos nombrados por un objeto eliminado que todavía
esté en la papelera. Se cuentan en **To purge** de `Resource`, no en *Deleted*. Un archivo que no usa ningún objeto
(*nothing* en la pestaña Resources) se conserva.

<a id="what-is-kept"></a>

## Qué se conserva

Un objeto eliminado está **bloqueado** (se conserva) mientras algo activo todavía lo usa:

| Bloqueado | Porque | Para desbloquearlo |
|---|---|---|
| un proveedor de inferencia o un protocolo eliminado | un libro todavía lo usa como su modelo o protocolo | selecciona otro en la pestaña *About* del libro, o elimina el libro |
| un lorebook eliminado (con sus entradas) | un libro todavía lo usa | selecciona otro lorebook en el libro, o quítalo |
| una parte de historia eliminada | es la última parte de la rama activa de un libro | cambia el libro a otra rama, o elimina el libro |
| un usuario eliminado | algo de otro usuario todavía usa sus datos (un libro que usa su proveedor de inferencia o su lorebook) | cambia o elimina el libro del otro usuario |

Vuelve a ejecutar *Analyze* después de desbloquear.

<a id="reference-model"></a>

## Modelo de referencias

**Reference Model**, al final, enumera todas las referencias entre los tipos de datos y su política: útil cuando
quieras entender por qué algo está bloqueado:

| Política | Significado |
|---|---|
| **Blocks purge** | Un objetivo eliminado se conserva mientras un objeto vivo lo referencia. |
| **Purged with target** | El objeto que referencia pertenece al objetivo y se elimina con él (las entradas con su lorebook). |
| **Owns target** | El objetivo pertenece al objeto que lo referencia (un resumen a su parte). |
| **Cleared on purge** | La referencia se quita cuando se purga el objetivo (lorebooks secundarios, modelo predeterminado). |

Las extensiones pueden participar en la limpieza con sus propias reglas.
