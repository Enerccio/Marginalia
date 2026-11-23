---
label: Administración
order: 910
icon: tools
---

# Administración

Los administradores gestionan la instalación de Marginalia: las cuentas de usuario, las copias de seguridad de la base de datos, la limpieza de datos eliminados, las actualizaciones y las extensiones. La primera cuenta, creada en el [primer arranque](../getting-started/first-start.md), es de administrador; puede dar también a otros usuarios el papel de administrador.

Los administradores son también usuarios normales con sus propios libros: no ven los libros, los lorebooks ni los ajustes de otros usuarios, solo los lorebooks y protocolos que esos usuarios comparten con todos.

<a id="the-admin-page"></a>

## La página de Administración

Los administradores tienen un botón **Administración** en la parte inferior del espacio de trabajo. En la [vista de teléfono](../phone.md#administration) (una ventana de menos de 768 píxeles de ancho) es la pestaña **Administración** del cajón. Abre la página de administración con estas pestañas:

| Pestaña | |
|---|---|
| **Usuarios** | Crear, editar y eliminar cuentas, y dar a los usuarios [permisos](permissions.md). Véase [Usuarios](users.md). |
| **Copias de seguridad de la base de datos** | Copias de seguridad de toda la base de datos, a demanda o programadas, y su restauración. Véase [Copias de seguridad de la base de datos](database-backups.md). |
| **Limpieza** | Eliminar de forma permanente los datos borrados. Véase [Limpieza](cleanup.md). |
| **Archivos de recursos** | Localizar los archivos de los recursos que ya nada usa, descargarlos en un ZIP y eliminarlos del disco. Véase [Archivos de recursos](resource-files.md). |
| **Asistente de ayuda** | El chat que responde a preguntas sobre el manual: el modelo, los prompts y quién lo paga. Véase [Asistente de ayuda](help-assistant.md). |
| **Agentes** | El prompt de sistema y el prompt de compactación que siguen los agentes cuando ni ellos ni su usuario tienen uno propio. Vacío significa el prompt integrado; el icono de información tiene **Insertar la plantilla predeterminada**, que lo rellena para que puedas editar una copia. Lo que los usuarios pueden hacer con los agentes es su [permiso](permissions.md). También **Los agentes pueden enviar mensajes a los webhooks de los usuarios**: el servidor llama a la dirección que el usuario escribió en sus ajustes cuando un agente lo necesita y tiene la aplicación cerrada ([Cuando no estás](../agents.md#when-you-are-not-there)); desactívalo donde eso no sea aceptable. |
| **Estadísticas** | Si las [estadísticas de escritura](../books/statistics.md) se registran, cuánto tiempo se conservan y cómo borrar las estadísticas de un usuario o de todos. Véase [Estadísticas](statistics.md). |
| **Tareas** | El registro de todo lo que se pidió a una IA externa (quién, cuándo, qué llamadas y cómo terminaron), un filtro que elimina a mano las entradas antiguas, y la página `/jobs` con las tareas que se ejecutan ahora. Véase [Tareas](jobs.md). |
| **GPU** | Una tarjeta gráfica compartida por el modelo de lenguaje y los servidores de imagen y voz: las GPU, los bloqueos de GPU (cómo se carga cada modelo y se libera la tarjeta) y los pasos que ejecutan, qué tiene cargado cada GPU ahora y **Descargar ahora**. Véase [GPU](gpus.md). |
| **Actualizar** | En un servidor Docker: la versión en ejecución, la versión más reciente de la rama que sigue el servidor y el botón que actualiza y reinicia Marginalia. Véase [Actualizaciones](updates.md). |
| **Extensiones** | Instalar y eliminar extensiones. Véase [Gestionar extensiones](extensions.md). |

![La pestaña Agentes de la página de Administración](../../images/admin-agents.png)

<a id="the-data-folder"></a>

## La carpeta de datos

Todo lo que guarda Marginalia está en la carpeta `.marginalia` del directorio personal del usuario que lo ejecuta (`./data` en el host con la [configuración de Docker](../getting-started/docker-server.md)):

| Ruta | |
|---|---|
| `marginalia.sqlite` (con `-wal`, `-shm`) | La base de datos: usuarios, libros, lorebooks, ajustes, todo. |
| `secret.key` | Clave que cifra las claves de API en la base de datos. Se crea en el primer arranque y solo puede leerla su propietario (en Linux y macOS); sin ella, las claves de API guardadas no se pueden leer. Si otros usuarios del equipo pueden leerla, el registro lo indica: `chmod 600 secret.key`. |
| `db-backups/` | [Copias de seguridad de la base de datos](database-backups.md). |
| `data/<login>/backups/manuscripts/<book id>/` | [Copias de seguridad de los libros](../books/backups.md) de cada usuario. |
| `extensions/` | JAR de las [extensiones](extensions.md) instaladas. |
| `update/` | Los archivos por los que se comunican Marginalia y el [vigilante de actualizaciones](updates.md#setting-up-the-watcher) de un servidor Docker: el estado, el registro de la última actualización, la petición. |
| `agent-workspaces/<agent id>/` | Los archivos de un [agente](../agents.md#the-working-copy) (sus notas e informes, que conserva entre ejecuciones). |
| `agent-runs/<run id>/` | Lo que guarda una ejecución de un agente: el inicio del libro (para mostrarte los cambios; se elimina junto con la copia de trabajo), los puntos de control y los hallazgos (se eliminan cuando la limpieza purga la ejecución). |
| `desktop/logs/` | Registros de la [aplicación de escritorio](../getting-started/desktop-app.md). |

Para hacer una copia de seguridad de toda la instalación, detén Marginalia y copia esta carpeta (incluido `secret.key`). Mientras está en marcha, usa las [copias de seguridad de la base de datos](database-backups.md) para la base de datos; las copias de los libros y las extensiones son archivos sencillos y se pueden copiar en cualquier momento.
