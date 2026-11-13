---
label: Copias de seguridad de libros
order: 50
---

# Copias de seguridad de libros

Una copia de seguridad de un libro es una instantánea de un solo libro: sus ajustes y prompts, todas las partes en todas las ramas, los resúmenes, las etiquetas y el lorebook. Usa las copias de seguridad para volver a un estado anterior de la historia, para hacer una copia de un libro o para pasar un libro a otra instalación de Marginalia u otro usuario.

Las copias de seguridad de libros son independientes de las [copias de seguridad de la base de datos](../administration/database-backups.md) que los administradores hacen de toda la instalación.

![La pestaña Copias de seguridad de un libro](../../images/book-backups.png)

<a id="taking-a-backup"></a>

## Hacer una copia de seguridad

En la pestaña **Copias de seguridad** del libro, haz clic en **Hacer copia de seguridad**. La copia aparece en la parte superior de la lista con su **Fecha**, el número de **Mensajes** (partes) que contiene y el número de **Imágenes** (las [imágenes de las partes](story-editor.md#images)) que usan sus partes.

Las copias de seguridad también pueden hacerse automáticamente, consulta [más abajo](#automatic-backups).

Una copia contiene todo el árbol de la historia, los resúmenes y las [anotaciones](annotations.md) de las partes; al restaurarla, copiar un libro o importar una copia, las notas vuelven también.

<a id="the-history-of-the-parts"></a>

## El historial de las partes

Una copia de seguridad no incluye el [historial de versiones](version-history.md) de las partes salvo que lo pidas: marca **Incluir el historial de versiones** junto a **Hacer copia de seguridad** (al principio está desmarcada, porque el historial puede ocupar varias veces el tamaño de la historia). Al restaurar una copia de este tipo, como libro o como copia, las versiones vuelven con sus nombres, el razonamiento de las generadas y la papelera del historial; las versiones se recortan hasta el límite del libro al que llegan. Las copias automáticas nunca incluyen el historial. Restaurar una copia sin él da a cada parte una primera versión, como en un libro anterior al historial.

<a id="backups-and-folders"></a>

## Copias de seguridad y carpetas

Una copia recuerda las [carpetas](folders.md) en las que estaba su libro, desde la superior hacia abajo, cada una con su nombre, descripción, idioma y los ajustes de texto que define. **Restaurar como libro nuevo** y **Clonar copia de seguridad** colocan el nuevo libro en la carpeta que indica la copia si todavía la tienes; si no, en las carpetas con los mismos nombres (si ya tienes una, se usa tal cual; si falta, se crea con los ajustes que tenía). Una copia sin carpeta, o con carpetas que no se pueden crear, se restaura en el nivel superior. **Restaurar** en un libro (completo o solo los mensajes) mantiene el libro en su carpeta. Las etiquetas de una carpeta no están en la copia. Las copias antiguas se restauran como antes.

<a id="pictures-in-a-backup"></a>

## Imágenes en una copia de seguridad

Una copia recuerda **qué imágenes** tienen las partes (y sus pies de foto), pero **no los archivos de imagen**. Así las copias, y las automáticas, ocupan poco. Las imágenes siguen en tus [Recursos](../resources.md). Para llevar las imágenes junto con una copia, inclúyelas al [exportarla](#exporting-and-importing).

<a id="restoring-a-backup"></a>

## Restaurar una copia de seguridad

Haz clic en **Restaurar** junto a la copia y elige:

| Opción | Qué se restaura |
|---|---|
| **Restauración completa** | Todo: nombre, descripción, publicación, prompts y otros ajustes del libro, etiquetas, modelo, protocolo, lorebook y todas las partes. |
| **Restaurar solo los mensajes** | Solo las partes (todas las ramas) y los resúmenes. Los ajustes, prompts y lorebook actuales del libro se mantienen tal cual. |

!!!warning
Restaurar reemplaza **todas las partes** del libro por las de la copia. Las partes escritas después de la copia se pierden.
Haz antes una copia de seguridad del estado actual si puede que la necesites de nuevo.
!!!

La ventana del libro se cierra y se vuelve a abrir con el libro restaurado.

Las imágenes de las partes restauradas, y cualquier otro archivo al que apunten las partes (el audio de un [audiolibro](../extensions/audiobook.md), por ejemplo), son los que lista la copia, siempre que sigas teniéndolos en [Recursos](../resources.md). Un recurso que hayas borrado desde entonces ya no está en la parte. Restaurar no devuelve los archivos borrados, salvo que la copia proceda de una exportación con los recursos que [importes](#importing-a-backup).

**El modelo y el protocolo** se buscan entre tus proveedores de inferencia y protocolos: primero el exacto de la copia, y luego uno con el mismo nombre. Si no existe ninguno, el libro conserva su modelo o protocolo actual.

**Los lorebooks** de la copia se comparan con los tuyos. Cuando existe un lorebook con el mismo nombre pero otra identidad, Marginalia pregunta por cada uno si **Vincular al existente** o **Crear nuevo** a partir de la copia. Los lorebooks que no tienes en absoluto pueden importarse (**Importar**) o dejarse fuera (**No importar**).

![Elegir qué hacer con los lorebooks](../../images/lorebook-import.png)

<a id="copying-a-book"></a>

## Copiar un libro

**Clonar copia de seguridad** crea un libro nuevo a partir de la copia, con un nombre que introduces. El libro original no cambia. Úsalo para probar algo radical con una copia del libro, o para empezar un libro nuevo con el mismo comienzo.

Si la copia apunta a recursos (imágenes, el audio de un audiolibro, otros archivos), Marginalia pregunta **Con recursos** o **Sin recursos**:

- *Con recursos* da al nuevo libro **sus propias copias** de ellos (los mismos archivos, nuevas entradas en [Recursos](../resources.md)), así borrar o reemplazar una imagen en un libro no afecta al otro. Los recursos que hayas borrado desde que se hizo la copia se dejan fuera.
- *Sin recursos* clona solo el texto.

![Clonar con o sin recursos](../../images/backup-clone-images.png)
<a id="exporting-and-importing"></a>

## Exportar e importar

**Exportar** descarga la copia de seguridad como un archivo JSON (`<nombre del libro>_backup.json`). Guárdalo como copia adicional o llévalo a otra instalación.

Si la copia apunta a recursos, Marginalia pregunta si quieres empaquetarlos (las imágenes de las partes y cualquier otro archivo que sus datos nombren, como el audio de un audiolibro):

![Exportar una copia de seguridad que tiene imágenes](../../images/backup-export-images.png)

- **Sin recursos** descarga el JSON inmediatamente, como se ha descrito. Los recursos solo se mencionan en él.
- **Con recursos** empaqueta la copia y todos sus recursos en un **archivo ZIP** (`<nombre del libro>_backup.zip`). Una barra de progreso muestra cuántos se han completado; el archivo se construye en disco un archivo cada vez, así que incluso un libro con muchos archivos grandes no consume mucha memoria. El audiolibro de un libro largo ocupa cientos de megabytes: empaquétalo solo cuando lo necesites. Cuando esté listo, haz clic en **Descargar**. El archivo se conserva solo hasta que cierres ese diálogo.

Si un recurso no se puede empaquetar porque lo has eliminado o porque su archivo falta, el diálogo indica cuántos se han omitido.

La casilla **Incluir los ajustes heredados en la exportación** (activada por defecto, junto a los botones de exportación) decide qué dice el archivo sobre los ajustes que el libro recibe de sus [carpetas](folders.md#where-the-settings-of-a-book-come-from). Si está activada, el idioma, la plantilla maestra, el punto de vista, el tiempo verbal, el estilo y las instrucciones que el libro no define por sí mismo se escriben en el archivo como valores propios, tomados de las carpetas en las que estaba el libro cuando se hizo la copia (primero la más cercana) y después de tus ajustes, de modo que el libro se comporta igual en una instalación o cuenta que no tenga esas carpetas ni esos ajustes; los valores predeterminados integrados no se escriben. Si está desactivada, el archivo solo contiene lo que el libro define por sí mismo. El archivo nunca indica en qué carpeta está el libro: dónde se archiva un libro es una decisión local. (La exportación de la historia, [Exportar un libro](export.md), escribe solo el texto y no tiene ajustes).

El ZIP contiene `backup.json` (el mismo JSON), `resources.json` (la lista de los recursos) y los archivos. No necesitas descomprimirlo: ambas formas de devolver una copia aceptan el `.zip` además del `.json`.

Hay dos formas de volver a cargar un archivo de copia de seguridad:

- **Importar copia de seguridad** en la pestaña *Copias de seguridad* añade el archivo a la lista de copias de **este libro**. No cambia nada hasta que lo *Restaures*.
- **Importar copia de seguridad como libro nuevo** en la pestaña *Ajustes* crea un **libro nuevo** a partir del archivo.

Ambas aceptan un archivo `.json` o `.zip`. El archivo se sube al servidor y se lee desde allí, así que una copia grande no supone ningún problema.

<a id="importing-a-backup"></a>

### Importar una copia de seguridad

Cuando el archivo es un ZIP con imágenes, las imágenes se añaden primero a tus [Recursos](../resources.md) y la copia las usa a continuación. Una imagen que ya tienes (el mismo archivo) no se vuelve a guardar: se usa la existente, por lo que importar tu propia exportación no añade nada. Un archivo JSON sin imágenes se importa tal cual.

Al importar **como libro nuevo**, el libro nuevo recibe **sus propias entradas** en Recursos, incluso si ya tienes las mismas imágenes: las entradas apuntan a los mismos archivos, así que no se ocupa espacio en disco dos veces. Si el archivo no tiene imágenes (un JSON simple), el libro nuevo recibe copias de las imágenes que el archivo enumera y que todavía tienes; las que no tienes se omiten. Todo lo que el archivo contenga y no sea una imagen utilizable (que no sea PNG, JPEG, GIF o WebP, o que sea demasiado grande) se ignora.

<a id="importing-a-backup-as-a-new-book"></a>

### Importar una copia de seguridad como libro nuevo

1. Abre la pestaña **Ajustes** y haz clic en **Importar copia de seguridad como libro nuevo**.
2. Escribe el **Nombre del libro nuevo**: la subida se activa una vez que rellenes el nombre.
3. Sube el archivo de la copia de seguridad.
4. Decide sobre los libros de consulta (lorebooks), si se te pregunta.

El libro nuevo aparece en la pestaña *Libros*.

<a id="deleting-backups"></a>

## Eliminar copias de seguridad

El botón de la papelera elimina una copia de seguridad de forma definitiva. Las copias automáticas no se rotan: permanecen hasta que las eliminas.

<a id="automatic-backups"></a>

## Copias de seguridad automáticas

Marginalia puede hacer una copia de seguridad por su cuenta mientras escribes. Define el valor predeterminado para todos los libros en **Ajustes → Ajustes generales → Estrategia de copias de seguridad**:

| Estrategia | |
|---|---|
| **Sin copias de seguridad** | No hay copias automáticas. |
| **Tras N mensajes** | Una copia de seguridad después de cada *N* partes generadas. |
| **Tras N minutos** | Una copia de seguridad después de una parte generada cuando han pasado al menos *N* minutos desde la última copia automática. |

Para usar una estrategia distinta en un solo libro, marca **Ajuste propio de cada manuscrito** en su pestaña *Copias de seguridad* y elige allí la estrategia y el número. Desmarcarla devuelve el libro al valor predeterminado de *Ajustes*.

Las copias automáticas se hacen solo después de generar una parte, así que un libro que solo lees no genera copias nuevas.

<a id="where-backups-are-stored"></a>

## Dónde se guardan las copias de seguridad

Cada copia de seguridad es un archivo JSON en la carpeta de datos de Marginalia:

```
~/.marginalia/data/<tu inicio de sesión>/backups/manuscripts/<id del libro>/
```

Con la [versión Docker](../getting-started/docker-server.md) esta ruta es `./data/data/<inicio de sesión>/...` en el host. Los archivos no están en la base de datos, así que una [copia de seguridad de la base de datos](../administration/database-backups.md) no los contiene. Incluye toda la carpeta `.marginalia` en tus propias copias de seguridad.

Los archivos de imagen tampoco están en las copias de seguridad: se encuentran en la carpeta `data/<tu inicio de sesión>/images`, junto a `backups`. Tampoco están en una [copia de seguridad de la base de datos](../administration/database-backups.md); consulta allí.
