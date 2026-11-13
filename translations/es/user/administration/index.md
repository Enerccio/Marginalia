---
label: Administración
order: 910
icon: tools
---

# Administración

Los administradores gestionan la instalación de Marginalia: cuentas de usuario, copias de seguridad de la base de datos, limpieza de datos eliminados, actualizaciones y extensiones. La primera cuenta, creada en el [primer arranque](../getting-started/first-start.md), es administradora; puede dar también el papel de administrador a otros usuarios.

Los administradores son además usuarios normales con sus propios libros: no ven los libros, los lorebooks ni los ajustes de otros usuarios, solo los lorebooks y los protocolos que esos usuarios comparten con todos.

<a id="the-admin-page"></a>

## La página de administración

Los administradores tienen un botón **Admin** en la parte inferior del espacio de trabajo. En la [vista para teléfono](../phone.md#administration) (una ventana de menos de 768 píxeles de ancho) es la pestaña **Admin** del cajón. Abre la página de administración con estas pestañas:

| Pestaña | |
|---|---|
| **Users** | Crear, editar y eliminar cuentas, y dar a los usuarios [permisos](permissions.md). Véase [Users](users.md). |
| **Database Backups** | Copias de seguridad de toda la base de datos, bajo demanda o programadas, y su restauración. Véase [Database backups](database-backups.md). |
| **Cleanup** | Eliminación permanente de datos borrados. Véase [Cleanup](cleanup.md). |
| **Resource Files** | Búsqueda de los archivos de recursos que ya no usa nada, descarga como ZIP y eliminación del disco. Véase [Resource Files](resource-files.md). |
| **Help assistant** | El chat que responde preguntas a partir del manual: el modelo, los prompts y quién paga por él. Véase [Help assistant](help-assistant.md). |
| **Agents** | El prompt de sistema y el prompt de compactación que siguen los agentes cuando ni ellos ni su usuario tienen uno propio. Vacío significa el prompt integrado; el icono de información tiene **Insert Default Template**, que lo rellena para que puedas editar una copia. Lo que los usuarios pueden hacer con los agentes es su [permiso](permissions.md). También **Agents may send messages to the webhooks of users**: el servidor llama a la dirección que un usuario escribió en sus ajustes cuando un agente la necesita y el usuario tiene la aplicación cerrada ([Cuando no estás](../agents.md#when-you-are-not-there)); desactívalo donde no sea aceptable. |
| **Statistics** | Si las [estadísticas de escritura](../books/statistics.md) se registran, cuánto tiempo se conservan, y la limpieza de las estadísticas de un usuario o de todos. Véase [Statistics](statistics.md). |
| **Jobs** | El registro de todo lo que se pidió a una IA externa (quién, cuándo, qué llamadas y cómo terminaron), un filtro que elimina a mano las entradas antiguas, y la página `/jobs` con las tareas que se ejecutan ahora. Véase [Jobs](jobs.md). |
| **Update** | En un servidor Docker: la versión en ejecución, la versión más reciente de la rama que sigue el servidor y el botón que actualiza y reinicia Marginalia. Véase [Updates](updates.md). |
| **Extensions** | Instalación y eliminación de extensiones. Véase [Managing extensions](extensions.md). |

![La pestaña Agents de la página de administración](../../images/admin-agents.png)

<a id="the-data-folder"></a>

## La carpeta de datos

Todo lo que Marginalia almacena está en la carpeta `.marginalia` del directorio personal del usuario que lo ejecuta (`./data` en el host con la [configuración de Docker](../getting-started/docker-server.md)):

| Ruta | |
|---|---|
| `marginalia.sqlite` (con `-wal`, `-shm`) | La base de datos: usuarios, libros, lorebooks, ajustes, todo. |
| `secret.key` | Clave que cifra las claves de API en la base de datos. Se crea en el primer arranque y solo es legible por su propietario (en Linux y macOS); sin ella, las claves de API guardadas no se pueden leer. Si otros usuarios del equipo pueden leerla, el registro lo indica: `chmod 600 secret.key`. |
| `db-backups/` | [Copias de seguridad de la base de datos](database-backups.md). |
| `data/<login>/backups/manuscripts/<book id>/` | [Copias de seguridad de libros](../books/backups.md) de cada usuario. |
| `extensions/` | JAR de las [extensiones](extensions.md) instaladas. |
| `update/` | Los archivos con los que hablan Marginalia y el [vigilante de actualizaciones](updates.md#setting-up-the-watcher) de un servidor Docker: el estado, el registro de la última actualización, la solicitud. |
| `agent-workspaces/<agent id>/` | Los archivos de un [agente](../agents.md#the-working-copy) (sus notas e informes, que conserva entre ejecuciones). |
| `agent-runs/<run id>/` | Lo que guarda una ejecución de un agente: el inicio del libro (para mostrarte los cambios; se elimina con la copia de trabajo), los puntos de control y los hallazgos (se eliminan cuando la limpieza purga la ejecución). |
| `desktop/logs/` | Registros de la [aplicación de escritorio](../getting-started/desktop-app.md). |

Para hacer una copia de seguridad de toda una instalación, detén Marginalia y copia esta carpeta (incluido `secret.key`). Mientras está en marcha, usa las [copias de seguridad de la base de datos](database-backups.md) para la base de datos; las copias de libros y las extensiones son archivos simples y se pueden copiar en cualquier momento.
