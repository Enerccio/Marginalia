---
label: Permisos
order: 95
---

# Permisos

Un permiso permite a un usuario hacer algo que no está al alcance de todos. Los administradores conceden permisos a los usuarios uno por uno; un usuario que no tiene ninguno puede hacer todo lo demás en Marginalia como de costumbre (escribir libros, crear libros de conocimiento, usar los modelos que los administradores [comparten](../inference-providers.md#shared-providers)).

| Permiso | Qué permite | Ajustes |
|---|---|---|
| **Crear modelos propios** | Añadir [proveedores de inferencia](../inference-providers.md): sin él, el usuario solo puede usar los proveedores que comparten los administradores; los proveedores que ya tiene siguen funcionando y se pueden seguir editando. | |
| **Asistente de ayuda** | Hacer preguntas al [asistente de ayuda](help-assistant.md), el chat que responde a partir del manual. | **Aplicar límites** y **Permitir navegar por la interfaz**, véase [Ajustes de un permiso](#settings-of-a-permission). |
| **Agentes** | (Experimental, véase [Agentes](../agents.md)) La pestaña [Agentes](../agents.md): crear agentes, clonarlos y ejecutarlos sobre un libro. | **Máximo de agentes**, **Ejecuciones simultáneas**, **Solicitudes simultáneas al modelo**, **Presupuesto de tokens de una ejecución** y las cuotas de copias y archivos, véase [Ajustes de un permiso](#settings-of-a-permission). |
| **Historial de versiones** | El número máximo de [versiones de una parte](../books/version-history.md#how-many-versions-are-kept) que guarda el historial de los libros del usuario. Todos los usuarios lo tienen con 50 (también un usuario sin la fila); un permiso que no está concedido significa que ese usuario no tiene historial. | **Versiones conservadas por parte**, un techo que las carpetas, los libros y el usuario no pueden superar. Un número vacío no establece ningún límite aquí. Un permiso que añadas empieza en 50. |
| **Notificaciones** | El [mensaje a una URL](../agents.md#when-you-are-not-there) que envía el servidor cuando un agente necesita al usuario y no hay ninguna pantalla abierta. El servidor llama a una dirección que escribe el usuario, así que es un permiso propio: sin él, los campos no aparecen en los ajustes del usuario y no se envía nada. | **Permitir direcciones de esta máquina y de redes privadas**, véase [Ajustes de un permiso](#settings-of-a-permission). |

Los administradores tienen siempre todos los permisos, no los necesitan, así que no hay nada que concederles (los botones y las secciones de un administrador nunca dependen de un permiso). Los usuarios nuevos no tienen ningún permiso: concédeles lo que necesiten justo después de [añadirlos](users.md#adding-a-user).

<a id="giving-and-taking-permissions"></a>

## Conceder y quitar permisos

El botón de llave en **Admin → Users**, en la fila del usuario, abre los permisos de ese usuario:

![Los permisos de un usuario](../../images/permissions-dialog.png)

La ventana es una tabla con los permisos que tiene el usuario, con tres columnas:

| Columna | |
|---|---|
| **Nombre** | El permiso. Un permiso que tiene ajustes lleva una flecha delante de su nombre, véase [Ajustes de un permiso](#settings-of-a-permission). |
| **Concedido** | Si el permiso funciona. Un permiso que no está concedido equivale a no tenerlo, pero se conserva con sus ajustes, así que puedes desactivarlo durante un tiempo y volver a activarlo. |
| El botón de papelera | Elimina el permiso (y sus ajustes) de forma definitiva. |

Para conceder un permiso, haz clic en **Añadir permiso** encima de la tabla y elige uno de los permisos que el usuario no tenga; se añade a la tabla, ya concedido. No se guarda nada hasta que hagas clic en **Guardar**:

![Elegir un permiso que añadir](../../images/permissions-add.png)

- **Guardar** está desactivado mientras no haya nada que guardar.
- **Salir** cierra la ventana; si hay cambios sin guardar, pregunta antes, para que ningún cambio se pierda por accidente.
- Un permiso que eliminas se queda en la tabla hasta que guardes, y se elimina de forma definitiva cuando guardas. Antes de eso, puedes recuperarlo cerrando la ventana con **Salir** sin guardar.

Después de guardar, la página del usuario se recarga si ha iniciado sesión, de modo que los botones y las secciones que dependen de los permisos aparecen o desaparecen de inmediato. Tu propia página no se recarga, aunque cambies tus propios permisos.

<a id="settings-of-a-permission"></a>

## Ajustes de un permiso

Algunos permisos tienen ajustes que pertenecen al usuario, no a toda la instalación. La fila de un permiso así empieza con una flecha: haz clic en ella para desplegar los ajustes (se pliegan al abrir la ventana, y un permiso que acabas de añadir aparece desplegado), y haz clic de nuevo para plegarlos. Plegar solo sirve para mantener corta una lista larga; no cambia nada y no cuenta como cambio sin guardar.

![Los ajustes del permiso Asistente de ayuda](../../images/permissions-settings.png)

**Asistente de ayuda**, **Agentes** y **Notificaciones** los tienen. Los del asistente de ayuda:

| Ajuste | |
|---|---|
| **Aplicar límites** | Para un modelo compartido, si las preguntas del usuario cuentan contra su límite y su consumo como cualquier otra solicitud, véase [Quién paga](help-assistant.md#who-pays). Desactivado por defecto. |
| **Permitir navegar por la interfaz** | Permite que el asistente abra una sección, seleccione una pestaña o abra el diálogo para añadir algo cuando el usuario le pide que le muestre el camino, véase [Indicar el camino](help-assistant.md#showing-the-way). Desactivado por defecto. |

Los de los agentes; un campo vacío indica el valor por defecto:

| Ajuste | |
|---|---|
| **Máximo de agentes** | Cuántos agentes puede tener el usuario, incluidos los clones de un agente. Cinco si está vacío. Cero permite al usuario conservar y ejecutar sus agentes, pero no crear ni clonar otro. Un agente eliminado libera su plaza. |
| **Ejecuciones simultáneas** | Cuántas ejecuciones del usuario pueden trabajar al mismo tiempo. Una si está vacío. |
| **Solicitudes simultáneas al modelo (por ejecución)** | Cuántas solicitudes al modelo puede tener en curso a la vez una ejecución, el agente y sus [subagentes](../agents.md#sub-agents) juntos. Una si está vacío: mientras un subagente consulta al modelo, el agente espera, y viceversa. Un número mayor permite que los subagentes trabajen en paralelo con el agente (el modelo del agente también puede tener su propio [límite de solicitudes](../inference-providers.md#general-settings), que se aplica igualmente). |
| **Presupuesto de tokens de una ejecución** | El número máximo de tokens (enviados y generados) que puede consumir una ejecución del usuario. Vacío o cero significa sin límite. |
| **Copias de trabajo** | Cuántas copias de trabajo de libros mantienen a la vez las ejecuciones del usuario. Una ejecución que no ha terminado conserva su copia, también una pausada, y la devuelve cuando termina, se detiene o se elimina. Vacío o cero significa sin límite, aparte del número de agentes. |
| **Archivo más grande (KB)** | El archivo más grande que un agente del usuario puede escribir en su espacio de trabajo. 1024 (1 MB) si está vacío. |
| **Tamaño del espacio de trabajo (MB)** | Cuánto pueden ocupar en total todos los archivos del espacio de trabajo de un agente del usuario. 20 si está vacío. |
| **Archivos en un espacio de trabajo** | Cuántos archivos puede contener el espacio de trabajo de un agente del usuario; las carpetas no cuentan. 500 si está vacío. |
| **Puntos de control de una ejecución (MB)** | Cuánto pueden ocupar en total los [puntos de control](../agents.md#checkpoints) de una ejecución (puntos de restauración de la copia de trabajo y de su espacio de trabajo); una parte se reserva para los automáticos. 100 si está vacío. |

![Los ajustes del permiso Agentes](../../images/permissions-settings-agents.png)

El de las notificaciones:

| Ajuste | |
|---|---|
| **Permitir direcciones de esta máquina y de redes privadas** | Un subpermiso: la casilla solo se puede marcar mientras **Notificaciones** esté concedido (si no, aparece atenuada, y quitar el permiso la desmarca). Sin él, solo se llama a direcciones de internet: ni `localhost`, ni una red privada (`10.x.x.x`, `172.16-31.x.x`, `192.168.x.x`), ni una dirección de enlace local o compartida (`169.254.x.x`, que es donde los servidores en la nube guardan su servicio de metadatos, `100.64-127.x.x`), ni las direcciones locales únicas de IPv6. Márcalo para un usuario cuyo ntfy o Gotify funcione en la misma máquina o en tu propia red. Desactivado por defecto. Un nombre se comprueba por las direcciones a las que resuelve, así que un nombre que apunta a una dirección privada también se rechaza, y también uno que no se puede encontrar. |

Los administradores no necesitan ni el permiso ni el subpermiso: reciben los mensajes y pueden usar direcciones locales. En un servidor compartido con personas en las que no confías del todo, deja el subpermiso desactivado: de lo contrario, el servidor haría solicitudes a cualquier cosa que escriba el usuario, dentro de tu red.

<a id="when-permissions-were-introduced"></a>

## Cuándo se introdujeron los permisos

Antes de los permisos, **Los usuarios pueden crear sus propios modelos** y el interruptor **Activado** del asistente de ayuda (con **Aplicar límites** y **Permitir navegar por la interfaz**) eran ajustes de toda la instalación. Marginalia los pasó a permisos al actualizarse: cada usuario que existía entonces recibió **Crear modelos propios** si todo el mundo podía crear modelos (que era el valor por defecto), y **Asistente de ayuda** con los mismos dos ajustes si el asistente estaba activado. Para ellos no cambia nada hasta que lo modifiques; los usuarios que añadas a partir de ahora empiezan sin ningún permiso.
