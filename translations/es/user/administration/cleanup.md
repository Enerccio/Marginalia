---
label: Limpieza
order: 80
---

# Limpieza

Cuando los usuarios eliminan algo en Marginalia (un libro, una parte de la historia, un libro de lore, un proveedor de inferencia), desaparece de sus listas, pero permanece en la base de datos. Así eliminar es seguro y las referencias siguen funcionando (un libro sigue conociendo el proveedor que escribió sus partes). Hasta entonces, los usuarios pueden restaurar sus objetos eliminados desde la pestaña [Papelera](../trash.md). **Limpieza** elimina de forma definitiva esos datos borrados, para mantener la base de datos pequeña.

![La pestaña Limpieza](../../images/admin-cleanup.png)

<a id="running-a-cleanup"></a>

## Ejecutar una limpieza

!!!warning
La limpieza no se puede deshacer; lo que elimina desaparece también de la [Papelera](../trash.md). [Crea una copia de seguridad de la base de datos](database-backups.md) antes.
!!!

1. Abre la pestaña **Limpieza** de la página de administración y haz clic en **Analizar**. Todavía no se cambia nada.
2. Revisa el resultado (ver abajo).
3. Haz clic en **Ejecutar la limpieza** y confirma *n objetos se eliminarán de forma permanente*.

La limpieza abarca los datos de todos los usuarios.

La limpieza elimina entradas de la base de datos, y solo los archivos de los [agentes](../agents.md): el espacio de trabajo de un agente purgado y la carpeta de una ejecución purgada. Los archivos de los [recursos](../resources.md) eliminados (imágenes de las partes) permanecen en la carpeta de datos de su propietario hasta que los quites en la pestaña [Archivos de recursos](resource-files.md).

<a id="seeing-what-is-deleted"></a>

## Ver qué está eliminado

**Papelera** debajo de las tablas enumera los objetos eliminados de todos los usuarios, una fila por objeto (ábrela para cargar la tabla), para que puedas ver qué eliminaría una limpieza y restaurar lo que alguien te haya pedido recuperar. Es la misma tabla que la pestaña [Papelera](../trash.md), incluidos los filtros por usuario y tipo y el enlace *Contenido extendido*.

![La sección Papelera de la pestaña Limpieza](../../images/admin-trash.png)

<a id="reading-the-analysis"></a>

## Leer el análisis

La primera tabla tiene una fila por tipo de datos:

| Columna | |
|---|---|
| **Entidad** | El tipo de datos: `Manuscript` (libros), `ChatMessage` (partes de la historia), `Lorebook`, `LorebookEntry`, `AI` (proveedores de inferencia), `Protocol`, `Summary`, `Tag`, `TagRelation`, `User`... |
| **Eliminados** | Cuántos están eliminados. |
| **Por purgar** | Cuántos se eliminarán con *Ejecutar la limpieza*, incluidos los datos que pertenecen a objetos eliminados. |
| **Bloqueados** | Eliminados, pero aún en uso, así que se conservan. |

**Objetos eliminados que aún están en uso** enumera los objetos bloqueados con el nombre y **Referenciado por**: lo que todavía los usa.

<a id="what-is-removed"></a>

## Qué se elimina

La limpieza elimina los objetos eliminados que ya no usa nada vivo, junto con los datos que les pertenecen solo a ellos:

| Eliminado | Se elimina con él |
|---|---|
| un libro | todas sus partes de la historia, resúmenes y enlaces de etiquetas |
| un libro de lore | sus entradas y enlaces de etiquetas; otros libros de lore ya no lo incluyen como subcolección |
| una parte de la historia | la parte (sus hijos ya se habían movido a su padre) y su resumen |
| un proveedor de inferencia o protocolo | se quita de los valores predeterminados de los usuarios en *Ajustes* |
| una etiqueta | sus enlaces a libros y entradas |
| un usuario | todo lo que poseía: libros (con sus partes de la historia y resúmenes), libros de lore, proveedores de inferencia, protocolos, etiquetas y ajustes |

<a id="what-is-kept"></a>

## Qué se conserva

Un objeto eliminado queda **bloqueado** (se conserva) mientras algo vivo todavía lo usa:

| Bloqueado | Porque | Para desbloquearlo |
|---|---|---|
| un proveedor de inferencia o protocolo eliminado | un libro todavía lo usa como su modelo o protocolo | selecciona otro en la pestaña *Información* del libro, o elimina el libro |
| un libro de lore eliminado (con sus entradas) | un libro todavía lo usa | selecciona otro libro de lore en el libro, o quítalo |
| una parte de la historia eliminada | es la última parte de la rama activa de un libro | cambia el libro a otra rama, o elimina el libro |
| un usuario eliminado | algo de otro usuario todavía usa sus datos (un libro que usa su proveedor de inferencia o su libro de lore) | cambia o elimina el libro del otro usuario |

Vuelve a ejecutar *Analizar* después de desbloquear.

<a id="reference-model"></a>

## Modelo de referencia

**Modelo de referencia** al final enumera cada referencia entre los tipos de datos y su política; resulta útil cuando quieres entender por qué algo está bloqueado:

| Política | Significado |
|---|---|
| **Impide la purga** | Un destino eliminado se conserva mientras un objeto vivo lo referencia. |
| **Se purga con el destino** | El objeto que referencia pertenece al destino y se elimina con él (las entradas con su libro de lore). |
| **Posee el destino** | El destino pertenece al objeto que lo referencia (un resumen a su parte). |
| **Se vacía al purgar** | La referencia se quita cuando el destino se purga (subcolecciones de lore, modelo predeterminado). |

Las extensiones pueden participar en la limpieza con sus propias reglas.
