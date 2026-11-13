---
label: Copias de seguridad de la base de datos
order: 90
---

# Copias de seguridad de la base de datos

Una copia de seguridad de la base de datos es una copia completa de la base de datos de Marginalia: todos los usuarios con todos sus libros, lorebooks, ajustes y datos de extensiones. Úsala para proteger la instalación frente a errores y fallos, y para trasladarla a otro ordenador. Para hacer copia de seguridad de un solo libro, los usuarios disponen de [copias de seguridad de libros](../books/backups.md).

![La pestaña Copias de seguridad de la base de datos](../../images/admin-database-backups.png)

<a id="creating-a-backup"></a>

## Crear una copia de seguridad

Haz clic en **Crear copia de seguridad** en la pestaña **Copias de seguridad de la base de datos**. La copia se realiza mientras Marginalia está en marcha, y es coherente incluso cuando la gente está escribiendo. Aparece en la lista con su **Nombre**, la fecha de **Creado**, el **Tipo** (*Manual* o *Programada*) y el **Tamaño**. La barra de herramientas muestra el tamaño actual de la base de datos.

Cada copia de seguridad tiene:

| Botón | |
|---|---|
| **Descargar** | Descarga el archivo de la copia (`.sqlite`); guarda una copia fuera del servidor. |
| **Restaurar al reiniciar** | Restaura la copia; consulta [más abajo](#restoring). |
| papelera | Elimina la copia de seguridad. |

**Subir copia de seguridad** añade a la lista un archivo de copia (`.sqlite` o `.db`), por ejemplo uno descargado de otra instalación. El archivo se comprueba primero y se rechaza con *The database can't be restored: ...* y el motivo cuando no es una base de datos SQLite, está dañado, no es una base de datos de Marginalia (la base de datos de otra aplicación) o procede de una versión de Marginalia más reciente que la que se está ejecutando.

!!!warning Las copias de seguridad contienen secretos
Una copia de seguridad de la base de datos lo contiene todo, incluidos los hashes de contraseña de todos los usuarios y sus claves de API. Las claves de API están cifradas con el archivo `secret.key` de la [carpeta de datos](index.md#the-data-folder), que no forma parte de la copia; pero cualquiera que tenga la copia y `secret.key` puede leerlas. Guarda las copias descargadas con la misma seguridad que el propio servidor.
!!!

Los archivos se guardan en la carpeta `db-backups` de la [carpeta de datos](index.md#the-data-folder). Las copias que Marginalia hace por su cuenta -`pre-restore-...` antes de una [restauración](#restoring) y `pre-migration-...` antes de una [actualización](#copy-before-an-upgrade)- también aparecen en la lista, marcadas como *Manual*, de modo que la rotación de las copias programadas nunca las elimina. Las copias de seguridad de libros y los JAR de extensiones son archivos separados y no forman parte de una copia de seguridad de la base de datos.

**Los archivos subidos ([imágenes de las partes](../books/story-editor.md#images)) tampoco forman parte de una copia de seguridad de la base de datos.** La base de datos solo conoce esas imágenes como entradas de la pestaña [Recursos](../resources.md); los archivos están en la carpeta `data/<login>/images` de la carpeta de datos. Esto es correcto: tras una restauración, las entradas son las del momento de la copia y las imágenes que utilizan siguen en la carpeta. Los archivos de imágenes añadidas después de la copia quedan huérfanos (nada los referencia) y ocupan espacio en disco. Para mantener las imágenes a salvo, incluye toda la carpeta de datos en tus propias copias de seguridad o empaquétalas en una [copia de seguridad de libro](../books/backups.md#pictures-in-a-backup).

Cuando restaures una copia en **otra instalación**, copia también `secret.key` de la instalación original (con Marginalia detenido); de lo contrario, las claves de API guardadas no podrán descifrarse y cada usuario tendrá que volver a introducirlas.

<a id="scheduled-backups"></a>

## Copias de seguridad programadas

La sección **Copias de seguridad programadas** crea copias automáticamente:

| Campo | |
|---|---|
| **Crear copias de seguridad automáticamente** | Activa las copias programadas. |
| **Programación (cron)** | Cuándo crearlas; consulta [más abajo](#schedule-syntax). Las siguientes tres fechas se muestran debajo del campo. |
| **Conservar copias programadas** | Cuántas copias programadas conservar; las más antiguas se eliminan. `0` conserva todas. Las copias manuales y las subidas nunca se eliminan. |

Haz clic en **Guardar la programación** para aplicarla. La línea de estado indica cuándo se hizo la última copia programada y cuándo toca la siguiente.

Cuando Marginalia no estaba en marcha a una hora programada -lo habitual en la aplicación de escritorio, que solo se ejecuta mientras escribes-, hace la copia omitida unos 30 segundos después del siguiente inicio.

<a id="schedule-syntax"></a>

### Sintaxis de la programación

La programación es una expresión cron de cinco campos, `minuto hora día-del-mes mes día-de-la-semana`:

| Expresión | Se ejecuta |
|---|---|
| `0 3 * * *` | todos los días a las 3:00 |
| `0 */6 * * *` | cada 6 horas |
| `30 2 * * 1` | los lunes a las 2:30 (día de la semana: `0` o `7` es domingo, `1` lunes...) |
| `0 12 1 * *` | el día 1 de cada mes a mediodía |
| `@daily`, `@weekly`, `@monthly` | una vez al día / a la semana / al mes a medianoche (`@hourly` y `@yearly` también funcionan) |

Las horas son las de la zona horaria del servidor (en Docker, configúrala con la variable de entorno `TZ`).

!!!
Para desactivar las copias programadas, desmarca *Crear copias de seguridad automáticamente* y haz clic en *Guardar la programación*. La expresión se conserva, así que puedes volver a activar las copias más adelante. Aun así se valida: nunca se guarda una expresión no válida; pero mientras las copias están desactivadas, el campo también puede quedar vacío.
!!!

<a id="restoring"></a>

## Restaurar

Una base de datos no puede reemplazarse mientras Marginalia la usa, así que la restauración se produce en el **siguiente inicio**:

1. Haz clic en **Restaurar al reiniciar** junto a la copia y confírmalo.
   La pestaña muestra *Database restore is scheduled. Restart the application to apply it.* - **Cancelar la restauración** la deshace.
2. Reinicia Marginalia: cierra y vuelve a abrir la [aplicación de escritorio](../getting-started/desktop-app.md), o ejecuta `docker compose restart` en el [servidor](../getting-started/docker-server.md).

Al iniciarse, la base de datos actual se mueve a `db-backups/pre-restore-<time>-marginalia.sqlite` -aparece en la lista, así que una restauración puede deshacerse restaurando ese archivo- y la copia ocupa su lugar. Una copia de una versión anterior de Marginalia se actualiza automáticamente.

La copia se vuelve a comprobar antes de ser colocada (las mismas comprobaciones que en *Subir copia de seguridad*; también *Restaurar al reiniciar* rechaza una copia que no las supere). Si no supera la comprobación, o falla su actualización, Marginalia conserva (o restituye) la base de datos actual y arranca con normalidad; el archivo rechazado se mueve a `db-backups/rejected-restore-<time>-marginalia.sqlite` y el registro indica el motivo.

Se pierde todo lo posterior a la copia: libros, partes y ajustes creados desde entonces. Los usuarios que tengan la sesión iniciada deberán volver a iniciarla si su cuenta cambió.

<a id="copy-before-an-upgrade"></a>

## Copia antes de una actualización

Cuando una nueva versión de Marginalia va a cambiar la estructura de una base de datos existente, primero guarda una copia como `db-backups/pre-migration-<time>-V<version>-marginalia.sqlite`, donde la versión es la que tenía la base de datos. La copia aparece en la lista de copias de seguridad, de modo que una actualización puede deshacerse instalando la versión anterior de Marginalia y [restaurando](#restoring) esa copia. Se conservan las tres últimas copias. Una instalación nueva no tiene nada que copiar, y si la copia no puede hacerse (por ejemplo, porque el disco está lleno), el registro lo indica y la actualización continúa.

Una migración que falla a mitad se revierte por sí sola y Marginalia no arranca; la copia es para una actualización que terminó correctamente pero hizo algo que no quieres.

<a id="moving-to-another-computer"></a>

## Trasladar a otro ordenador

1. Crea una copia de seguridad y **Descárgala**.
2. Instala Marginalia en el nuevo ordenador y crea un administrador temporal en el primer inicio.
3. **Subir copia de seguridad**, **Restaurar al reiniciar** y reinicia.
4. Inicia sesión con las cuentas de la copia.

Copia también la carpeta `data` (copias de seguridad de libros y las imágenes de las partes) y la carpeta `extensions` de la [carpeta de datos](index.md#the-data-folder) antigua. O bien, detén Marginalia y copia la carpeta `.marginalia` completa.
