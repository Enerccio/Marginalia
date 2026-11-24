---
label: Copias de seguridad de la base de datos
order: 90
---

# Copias de seguridad de la base de datos

Una copia de seguridad de la base de datos es una copia completa de la base de datos de Marginalia: todos los usuarios con todos sus libros, lorebooks, ajustes y datos de extensiones. Úsala para proteger la instalación frente a errores y fallos, y para trasladarla a otro equipo. Para hacer copia de seguridad de un solo libro, los usuarios disponen de [copias de seguridad de libros](../books/backups.md).

![La pestaña Database Backups](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## Crear una copia de seguridad

Haz clic en **Create Backup** en la pestaña **Database Backups**. La copia se realiza mientras Marginalia está en marcha, y es coherente incluso cuando hay gente escribiendo. Aparece en la lista con su **Name**, fecha **Created**, **Type** (*Manual* o *Scheduled*) y **Size**. La barra de herramientas muestra el tamaño actual de la base de datos.

Cada copia tiene:

| Botón | |
|---|---|
| **Download** | Descarga el archivo de la copia (`.sqlite`); guarda una copia fuera del servidor. |
| **Restore on restart** | Restaura la copia, ver [más abajo](#restoring). |
| papelera | Elimina la copia. |

**Upload Backup** añade un archivo de copia (`.sqlite` o `.db`) a la lista, por ejemplo uno descargado de otra instalación. El archivo se comprueba antes y se rechaza con *The database can't be restored: ...* y el motivo cuando no es una base de datos SQLite, está dañado, no es una base de datos de Marginalia (la de otra aplicación) o procede de una versión de Marginalia más nueva que la que se está ejecutando.

!!!warning Las copias contienen secretos
Una copia de la base de datos lo contiene todo, incluidos los hashes de contraseña de todos los usuarios y sus claves de API. Las claves de API están cifradas con el archivo `secret.key` de la [carpeta de datos](index.md#the-data-folder), que no forma parte de la copia; pero quien tenga a la vez la copia y `secret.key` puede leerlas. Guarda las copias descargadas con la misma seguridad que el propio servidor.
!!!

Los archivos se guardan en la carpeta `db-backups` dentro de la [carpeta de datos](index.md#the-data-folder). Las copias que Marginalia hace por sí mismo —`pre-restore-...` antes de una [restauración](#restoring) y `pre-migration-...` antes de una [actualización](#copy-before-an-upgrade)— también aparecen en la lista, como *Manual*, de modo que nunca las borra la rotación de las copias programadas. Las copias de libros y los JAR de extensiones son archivos aparte y no forman parte de una copia de la base de datos.

**Los archivos subidos (las [imágenes de las partes](../books/story-editor.md#images)) tampoco forman parte de una copia de la base de datos.** La base de datos solo los conoce como entradas de la pestaña [Resources](../resources.md); los archivos están en la carpeta `data/<login>/images` de la carpeta de datos. Está bien así: tras una restauración, las entradas son las del momento de la copia y las imágenes que usan siguen en la carpeta. Los archivos de imágenes añadidos después de la copia simplemente quedan sin uso (nada los referencia) y ocupan espacio en disco. Para mantener las imágenes a salvo, incluye toda la carpeta de datos en tus propias copias de seguridad, o empaquétalas en una [copia de seguridad de libro](../books/backups.md#pictures-in-a-backup).

Al restaurar una copia en **otra instalación**, copia también `secret.key` desde la instalación original (con Marginalia detenido); si no, las claves de API guardadas no se podrán descifrar y cada usuario tendrá que volver a introducirlas.

<a id="scheduled-backups"></a>

## Copias programadas

La sección **Scheduled Backups** crea copias automáticamente:

| Campo | |
|---|---|
| **Create backups automatically** | Activa las copias programadas. |
| **Schedule (cron)** | Cuándo crearlas, ver [más abajo](#schedule-syntax). Los tres próximos momentos se muestran debajo del campo. |
| **Keep scheduled backups** | Cuántas copias programadas conservar; las más antiguas se borran. `0` las conserva todas. Las copias manuales y las subidas nunca se borran. |

Haz clic en **Save Schedule** para aplicarla. La línea de estado indica cuándo se hizo la última copia programada y cuándo toca la siguiente.

Si Marginalia no estaba en marcha a una hora programada —lo habitual en la aplicación de escritorio, que solo se ejecuta mientras escribes—, hace la copia perdida unos 30 segundos después del siguiente inicio. Desactivar la programación o cambiarla en esos 30 segundos cancela esa copia.

<a id="schedule-syntax"></a>

### Sintaxis de la programación

La programación es una expresión cron de cinco campos, `minute hour day-of-month month day-of-week`:

| Expresión | Se ejecuta |
|---|---|
| `0 3 * * *` | todos los días a las 3:00 |
| `0 */6 * * *` | cada 6 horas |
| `30 2 * * 1` | los lunes a las 2:30 (día de la semana: `0` o `7` es domingo, `1` lunes...) |
| `0 12 1 * *` | el día 1 de cada mes a mediodía |
| `@daily`, `@weekly`, `@monthly` | una vez al día / semana / mes a medianoche (`@hourly`, `@yearly` también funcionan) |

Las horas son las de la zona horaria del servidor (en Docker, se configura con la variable de entorno `TZ`).

!!!
Para desactivar las copias programadas, desmarca *Create backups automatically* y haz clic en *Save Schedule*. La expresión se conserva, así que puedes volver a activar las copias más tarde. Aun así se sigue comprobando: nunca se guarda una expresión no válida; pero mientras las copias están desactivadas, el campo también puede estar vacío.
!!!

<a id="restoring"></a>

## Restaurar

Una base de datos no se puede reemplazar mientras Marginalia la usa, así que la restauración se realiza en el **próximo inicio**:

1. Haz clic en **Restore on restart** junto a la copia y confirma. La pestaña muestra *Database restore is scheduled. Restart the application to apply it.* — **Cancel Restore** la anula.
2. Reinicia Marginalia: cierra e inicia la [aplicación de escritorio](../getting-started/desktop-app.md), o ejecuta `docker compose restart` en el [servidor](../getting-started/docker-server.md).

Al iniciar, la base de datos actual se traslada a `db-backups/pre-restore-<time>-marginalia.sqlite` —aparece en la lista, así que una restauración puede deshacerse restaurando ese archivo— y la copia ocupa su lugar. Una copia de una versión anterior de Marginalia se actualiza automáticamente.

La copia se vuelve a comprobar antes de colocarla (las mismas comprobaciones que para *Upload Backup*; también *Restore on restart* rechaza una copia que no las supera). Si no pasa la comprobación, o falla su actualización, Marginalia conserva (o restituye) la base de datos actual y arranca con normalidad; el archivo rechazado se traslada a `db-backups/rejected-restore-<time>-marginalia.sqlite` y el registro indica el motivo.

Se pierde todo lo posterior a la copia: libros, partes y ajustes creados desde entonces. Los usuarios que tenían sesión iniciada tienen que volver a iniciarla si su cuenta ha cambiado.

<a id="copy-before-an-upgrade"></a>

## Copia antes de una actualización

Cuando una nueva versión de Marginalia vaya a cambiar la estructura de una base de datos existente, primero guarda una copia como `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`, donde la versión es la que tenía la base de datos. La copia aparece en la lista de copias, así que una actualización puede deshacerse instalando la versión antigua de Marginalia y [restaurando](#restoring) esa copia. Se conservan las tres últimas copias. Una instalación nueva no tiene nada que copiar, y si la copia no se puede hacer (por ejemplo, porque el disco está lleno), el registro lo indica y la actualización continúa.

Una migración que falla a medias se revierte sola y Marginalia no arranca; la copia sirve para una actualización que se completó pero hizo algo que no quieres.

<a id="moving-to-another-computer"></a>

## Traslado a otro equipo

1. Crea una copia y haz clic en **Download**.
2. Instala Marginalia en el nuevo equipo y crea un administrador temporal en el primer inicio.
3. **Upload Backup**, **Restore on restart**, reinicia.
4. Inicia sesión con las cuentas de la copia.

Copia también la carpeta `data` (copias de libros e imágenes de las partes) y la carpeta `extensions` desde la [carpeta de datos](index.md#the-data-folder) antigua. O bien, detén Marginalia y copia la carpeta `.marginalia` completa.
