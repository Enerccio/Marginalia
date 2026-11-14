---
label: Lorebook VCS
order: 9
---

# Lorebook VCS

Historial de revisiones de las [entradas del lorebook](../lorebooks/entries.md): conserva versiones anteriores de tu lore y cambia
entre ellas, por ejemplo el estado del mundo antes y después de una guerra, o un personaje en distintos momentos de la
historia.

!!!
Lorebook VCS funciona en el [diseño de teléfono](../phone.md#extensions): el panel está debajo de la fila *Activado* del lorebook, y las revisiones de una entrada están en la parte superior de su formulario.
!!!

!!!
En el [editor a pantalla completa](../lorebooks/lorebooks.md#editing-in-full-screen), la fila **Lorebook Revisions** está encima de la lista de
entradas, y la barra de revisión de una entrada está debajo de la fila del nombre de su pestaña. Al cambiar de revisión, se muestra de inmediato en la pestaña.
!!!

Una revisión es una instantánea de la entrada completa: nombre, contenido, nota, activado, orden, filtro y modo de filtrado, modo de inserción,
*No buscar otras entradas en su contenido*, etiquetas y etiquetas negativas. (Las revisiones hechas antes de que existiera esa opción se leen como desactivadas).

![La barra de revisión de una entrada](../../images/plugin-lorebookvcs.png)

<a id="entry-revisions"></a>

## Revisiones de entradas

Con la extensión instalada, los detalles (⌄) de cada entrada del lorebook empiezan con una barra de revisión:

```
Revision: 2 / 3            [−]  [‹]  [Rev 2 (2026-10-08 14:30:12) ▾]  [›]  [+]
Modified: 2026-10-08 14:30:12
```

Las revisiones funcionan como ranuras. La entrada muestra siempre una de ellas, la **revisión actual**, y lo que cambias en la
entrada pertenece a esa revisión. La generación siempre usa la entrada tal como está, así que la revisión actual es la que
ve el modelo.

| Control | |
|---|---|
| **+** | Guarda la entrada tal como está ahora como una nueva revisión y la convierte en la actual. |
| **‹** / **›**, o la lista | Cambia a la revisión anterior / siguiente / elegida. Tus ediciones se quedan en la revisión que abandonas; la otra se carga en la entrada. |
| **−** | Elimina la revisión actual y cambia a la anterior. La última revisión que queda no se puede eliminar. |
| *n / total*, *Modified* | Qué revisión es la actual, y cuándo se modificó por última vez. |

La primera vez que se abren los detalles de una entrada, su estado actual se convierte en la revisión 1.

Un uso típico:

1. Abre la entrada de un personaje y haz clic en **+**: la revisión 2 es una copia de la revisión 1.
2. Cambia la revisión 2 para que muestre al personaje después de los acontecimientos del capítulo 10.
3. Escribe. Cuando vuelvas a editar el capítulo 5, cambia a la revisión 1 con **‹**; vuelve a la otra más tarde con **›**.

<a id="lorebook-revisions"></a>

## Revisiones del lorebook

El editor del lorebook tiene una fila **Lorebook Revisions** con dos botones. Actúan sobre el lorebook seleccionado en el
editor:

| Botón | |
|---|---|
| export | Descarga el historial de todas las entradas del lorebook como `<lorebook>_vcs_history.json`. |
| import | **Import Marginalia VCS JSON**: un historial exportado por esta extensión; **Import SillyTavern VCS Extension JSON**: historial de la extensión de historial de versiones de lorebooks de SillyTavern. |

Una importación **reemplaza** el historial del lorebook; si el lorebook ya tiene un historial, o si el archivo se exportó
desde otro lorebook, la importación pide confirmación antes de continuar.

Un historial de Marginalia se asocia primero a las entradas por las propias entradas. Las entradas que no encuentra, por ejemplo
cuando el historial viene de otra copia del lorebook, se asocian por el nombre y el orden de su revisión actual, o solo por el nombre
cuando una única entrada lo tiene. Los historiales que no coinciden con ninguna entrada se omiten; la confirmación
indica cuántos son.

El historial de SillyTavern se asocia a las entradas por su posición en el lorebook, así que impórtalo en un lorebook
[importado del mismo world info](../lorebooks/import-export.md#importing-sillytavern-world-info), antes de añadir o
eliminar entradas. Las claves de activación de sus revisiones pasan a ser el filtro de la entrada, del mismo modo que
lo convierte la [importación de world info](../lorebooks/import-export.md#importing-sillytavern-world-info).

<a id="storage"></a>

## Almacenamiento

El historial se guarda en la base de datos junto con el lorebook, así que las [copias de seguridad de la base de datos](../administration/database-backups.md)
lo incluyen. Los datos que una extensión guarda en un lorebook también se escriben en las [exportaciones de lorebooks](../lorebooks/import-export.md#exporting)
y en los lorebooks de las [copias de seguridad de libros](../books/backups.md), y en las copias de los lorebooks de un libro con las que
trabajan los [agentes](../agents.md). Pero al importar un lorebook se crean entradas nuevas, y el historial pertenece a la identidad de
las entradas antiguas, así que no aparece en ellas. Para trasladar el historial a una copia importada, expórtalo desde el original e
impórtalo en la copia: las entradas se asocian entonces por nombre y orden (ver [Revisiones del lorebook](#lorebook-revisions)).
