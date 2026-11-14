---
label: Papelera
order: 955
icon: trash
---

# Papelera

Eliminar en Marginalia no destruye nada de inmediato: un libro, un lorebook, una parte de la historia o un proveedor de inferencia eliminado
solo se marca como eliminado y se oculta de tus listas. La pestaña **Papelera** muestra estos objetos eliminados y permite recuperarlos.

Los objetos eliminados permanecen en la papelera hasta que un administrador ejecuta la [limpieza](administration/cleanup.md), que los elimina
de la base de datos de forma definitiva. Lo que la limpieza ha eliminado ya no se puede restaurar (solo lo conserva una
[copia de seguridad de la base de datos](administration/database-backups.md) realizada antes).

![La pestaña Papelera](../images/trash-tab.png)

La tabla se carga a medida que haces scroll. Los objetos eliminados más recientemente aparecen primero.

| Columna | |
|---|---|
| (casilla de selección) | Selecciona el objeto para restaurarlo. La casilla del encabezado selecciona todos los objetos. |
| **Tipo** | El tipo de objeto: libro, lorebook, entrada de lorebook, mensaje (una parte de la historia), versión de una parte (una entrada eliminada del [historial de versiones](books/version-history.md#deleting-versions); restaurarla la saca de la papelera del historial), resumen, modelo (proveedor de inferencia), protocolo, etiqueta, agente, ejecución de agente, recurso... |
| **Nombre** | El nombre del objeto. Las partes de la historia y los enlaces de etiquetas no tienen nombre y muestran su id; usa *Contenido extendido* para leer una parte de la historia. |
| **Usuario** | De quién es el objeto. Solo los administradores ven esta columna. |
| **Eliminado** | Cuándo se eliminó el objeto. |
| **Contenido extendido** | Abre los datos guardados del objeto, véase [más abajo](#extended-content). |

Encima de la tabla, **Tipo** muestra solo un tipo de objeto, y **Actualizar** vuelve a cargar la tabla.

<a id="restoring"></a>

## Restauración

Marca los objetos que quieres recuperar y haz clic en **Restaurar la selección**. Marginalia pide confirmación antes de restaurarlos. Los objetos
vuelven a sus listas, exactamente como estaban al eliminarlos, y las demás pestañas se actualizan.

<a id="objects-that-belong-to-other-objects"></a>

### Objetos que pertenecen a otros objetos

Al eliminar un objeto se eliminan también los objetos que le pertenecen, y al restaurarlo se recuperan: un libro recupera sus partes de la historia,
sus resúmenes y sus etiquetas; un lorebook, sus entradas; una parte de la historia, su resumen y las partes situadas debajo; un proveedor de inferencia,
sus números de uso. No necesitas marcarlos: restaura el libro y su historia vuelve con él.
Aparecen en la tabla como filas separadas (un libro con una historia larga llena la tabla con sus partes). Restaurar una de ellas por separado
solo funciona si el objeto al que pertenece no está eliminado. Algo que habías eliminado por su cuenta antes (una entrada, una parte de la historia)
vuelve también con su lorebook o libro, ya que Marginalia no puede distinguirlo de lo que se eliminó junto con él.

Algunos objetos no pueden existir sin otro objeto:

- una entrada necesita su lorebook,
- una parte de la historia y un resumen necesitan su libro,
- un libro necesita su modelo (proveedor de inferencia), su protocolo y su lorebook, siempre que los use,
- un enlace de etiqueta necesita su etiqueta.

Un objeto cuyo lorebook, libro... está eliminado **solo se puede restaurar junto con él**: marca ambos. Si marcas solo
la entrada, Marginalia **no restaura nada** e indica lo que falta:

> Nada se restauró. Estos objetos necesitan un objeto eliminado que no está seleccionado. Selecciónalo también o déjalos eliminados.
> La entrada de lorebook "mosh" necesita el lorebook eliminado "Phrasebook" (lorebook)

![Restauración rechazada porque el lorebook de la entrada también está eliminado](../images/trash-restore-blocked.png)

Marca el objeto que se indica y vuelve a restaurar. Los objetos que ya no están eliminados (restaurados entretanto, o un objeto que no eliminaste) no suponen ningún problema.

!!!info Una parte de la historia restaurada
Una parte de la historia que se eliminó desde el medio de una historia tenía sus partes siguientes movidas a su padre. Restaurarla
devuelve la parte bajo su padre como una nueva rama, **sin** las partes que la seguían: se quedan donde fueron movidas. Restaura un libro completo en lugar de
partes sueltas si quieres que la historia vuelva tal como era.
!!!

<a id="deleting-for-good"></a>

## Eliminación definitiva

No siempre quieres esperar a la limpieza: una parte de la historia o una entrada de lorebook que sabes que ya no necesitarás nunca se puede
eliminar de inmediato. Márcala y haz clic en **Eliminar definitivamente**; Marginalia pide confirmación primero, y **esto no se puede deshacer**.

![Eliminar una parte de la historia definitivamente](../images/trash-delete-permanently.png)

- Solo se pueden eliminar definitivamente **partes de la historia** y **entradas de lorebook** (con sus resúmenes y etiquetas). El botón
  está deshabilitado mientras haya marcado cualquier otra cosa: libros, lorebooks y el resto esperan a la [limpieza](administration/cleanup.md)
  del administrador, así que no puedes eliminar un libro entero por accidente.
- Las partes de la historia situadas debajo de una parte eliminada no se eliminan con ella, estén eliminadas o no: pasan a su padre, como ocurre
  al borrar una parte en el editor. Si la parte era la posición en la que estaba abierto un libro, el libro se abre en el padre.
- Marginalia no elimina nada si algo no se puede quitar (por ejemplo, si una extensión todavía necesita el objeto) e indica
  cuál es.

<a id="extended-content"></a>

### Contenido extendido

Marginalia guarda gran parte de los datos de un objeto (el texto de una parte de la historia, la configuración de una entrada de lorebook, las notas que
guardaron las [extensiones](extensions/index.md)) como JSON junto a él. **Contenido extendido** en una fila lo muestra, para que puedas
revisar lo que contiene un objeto antes de restaurarlo, por ejemplo para encontrar la parte de la historia correcta. Los objetos sin estos
datos (recursos) no tienen enlace; un objeto que no tiene ninguno muestra *Este objeto no tiene contenido extendido.*

<a id="administrators"></a>

## Administradores

Los administradores ven los objetos eliminados de **todos los usuarios** y pueden restaurar cualquiera de ellos. El filtro **Usuario** sobre la
tabla muestra los objetos de un solo usuario; la columna **Usuario** indica de quién son. Los objetos restaurados vuelven a su propietario.

La misma tabla está en *Admin → Cleanup* (**Papelera**), para que veas qué eliminará la siguiente limpieza. Los objetos de un usuario
eliminado no se pueden restaurar: dependen de ese usuario.
