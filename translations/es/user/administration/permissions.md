---
label: Permisos
order: 95
---

# Permisos

Un permiso permite a un usuario hacer algo que no está al alcance de todos. Los administradores dan permisos a los usuarios uno a uno;
un usuario que no tiene ninguno puede hacer todo lo demás en Marginalia como de costumbre (escribir libros, crear lorebooks, usar los modelos que
los administradores [comparten](../inference-providers.md#shared-providers)).

| Permiso | Qué permite | Ajustes |
|---|---|---|
| **Create own models** | Añadir [proveedores de inferencia](../inference-providers.md): sin este permiso el usuario solo puede usar los proveedores que comparten los administradores; los proveedores que ya tiene siguen funcionando y se pueden seguir editando. | |
| **Help assistant** | Preguntar al [asistente de ayuda](help-assistant.md), el chat que responde a partir del manual. | **Apply limits** y **Allow navigating the interface**, ver [Ajustes de un permiso](#settings-of-a-permission). |
| **Agents** | (Experimental, ver [Agents](../agents.md)) La pestaña [Agents](../agents.md): crear agentes, clonarlos y ejecutarlos sobre un libro. | **Most agents**, **Concurrent runs**, **Requests to the model at once**, **Token budget of a run** y las cuotas de copias y archivos, ver [Ajustes de un permiso](#settings-of-a-permission). |
| **Version history** | El número máximo de [versiones de una parte](../books/version-history.md#how-many-versions-are-kept) que guarda el historial de los libros del usuario. Todo usuario lo tiene con 50 (también un usuario sin la fila); un permiso que no está concedido significa que ese usuario no tiene historial. | **Versions kept per part**, un techo que las carpetas, los libros y el usuario no pueden superar. Un número vacío no pone límite aquí. Un permiso que añades empieza en 50. |
| **API access** | Claves de la [API HTTP](../account.md#api-keys): programas que actúan como el usuario en sus libros. Quitar el permiso detiene las claves del usuario. Los administradores no necesitan permiso, y solo ellos pueden dar a una clave acceso a la actualización o a las extensiones. | |
| **Notifications** | El [mensaje a una URL](../agents.md#when-you-are-not-there) que el servidor envía cuando un agente necesita al usuario y no hay ninguna pantalla abierta. El servidor llama a una dirección que escribe el usuario, por eso es un permiso propio: sin él los campos no aparecen en los ajustes del usuario y no se envía nada. | **Allow addresses on this machine and in private networks**, ver [Ajustes de un permiso](#settings-of-a-permission). |

Los administradores tienen siempre todos los permisos, no los necesitan, así que no hay nada que darles (los botones y secciones de un administrador nunca dependen de un permiso). Los usuarios nuevos no tienen ningún permiso: dales lo que necesiten justo después de [añadirlos](users.md#adding-a-user).

<a id="giving-and-taking-permissions"></a>

## Dar y quitar permisos

El botón de llave en **Admin → Users**, en la fila del usuario, abre los permisos de ese usuario:

![Los permisos de un usuario](../../images/permissions-dialog.png)

La ventana es una tabla de los permisos que tiene el usuario, con tres columnas:

| Columna | |
|---|---|
| **Name** | El permiso. Un permiso que tiene ajustes lleva una flecha delante de su nombre, ver [Ajustes de un permiso](#settings-of-a-permission). |
| **Granted** | Si el permiso funciona. Un permiso que no está concedido equivale a no tenerlo, pero se conserva con sus ajustes, así que puedes desactivarlo un tiempo y volver a activarlo. |
| El botón de la papelera | Elimina el permiso (y sus ajustes) de forma definitiva. |

Para dar un permiso, haz clic en **Add permission** encima de la tabla y elige uno de los permisos que el usuario no tiene;
se añade a la tabla, concedido. No se guarda nada hasta que hagas clic en **Save**:

![Elegir un permiso para añadir](../../images/permissions-add.png)

- **Save** está desactivado mientras no haya nada que guardar.
- **Exit** cierra la ventana; si hay cambios sin guardar, pregunta antes, así que un cambio nunca se descarta por accidente.
- Un permiso que eliminas permanece en la tabla hasta que guardes, y se elimina de forma definitiva cuando lo haces. Antes de eso, recupéralo cerrando la ventana con **Exit** sin guardar.

Tras guardar, la página del usuario se recarga si ha iniciado sesión, de modo que los botones y secciones que dependen de los permisos aparecen o desaparecen al momento. Tu propia página no se recarga, aunque cambies tus propios permisos.

<a id="settings-of-a-permission"></a>

## Ajustes de un permiso

Algunos permisos tienen ajustes que pertenecen al usuario, no a toda la instalación. La fila de un permiso así empieza con una flecha: haz clic para desplegar los ajustes (se pliegan al abrir la ventana, y un permiso que acabas de añadir aparece desplegado), y vuelve a hacer clic para plegarlos. Plegar es solo una forma de mantener corta una lista larga; no cambia nada y no cuenta como cambio sin guardar.

![Los ajustes del permiso Help assistant](../../images/permissions-settings.png)

**Help assistant**, **Agents** y **Notifications** tienen ajustes. Los del asistente de ayuda:

| Ajuste | |
|---|---|
| **Apply limits** | Para un modelo compartido, si las preguntas del usuario cuentan contra su límite y su uso como cualquier otra petición, ver [Quién paga](help-assistant.md#who-pays). Desactivado por defecto. |
| **Allow navigating the interface** | Permite que el asistente abra una sección, seleccione una pestaña o abra el diálogo para añadir algo cuando el usuario le pide que le muestre el camino, ver [Mostrar el camino](help-assistant.md#showing-the-way). Desactivado por defecto. |

Los de los agentes; un campo vacío indica el valor por defecto:

| Ajuste | |
|---|---|
| **Most agents** | Cuántos agentes puede tener el usuario, incluidos los clones de un agente. Cinco si está vacío. Cero permite al usuario conservar y ejecutar sus agentes, pero no crear ni clonar otro. Un agente eliminado libera su lugar. |
| **Concurrent runs** | Cuántas ejecuciones del usuario pueden funcionar a la vez. Una si está vacío. |
| **Requests to the model at once (per run)** | Cuántas peticiones al modelo puede tener en vuelo a la vez una ejecución, el agente y sus [subagentes](../agents.md#sub-agents) juntos. Una si está vacío: mientras un subagente pide algo al modelo, el agente espera, y viceversa. Más permite que los subagentes trabajen en paralelo con el agente (el modelo del agente puede tener también su propio [límite de peticiones](../inference-providers.md#general-settings), que se aplica igualmente). |
| **Token budget of a run** | El máximo de tokens (enviados y generados) que puede gastar una ejecución del usuario. Vacío o cero significa sin límite. |
| **Working copies** | Cuántas copias de trabajo de libros mantienen a la vez las ejecuciones del usuario. Una ejecución que no ha terminado conserva su copia, también una pausada, y la devuelve cuando termina, se detiene o se elimina. Vacío o cero significa sin más límite que el número de agentes. |
| **Largest file (KB)** | El archivo más grande que un agente del usuario puede escribir en su espacio de trabajo. 1024 (1 MB) si está vacío. |
| **Workspace size (MB)** | Cuánto pueden ocupar en total todos los archivos del espacio de trabajo de un agente del usuario. 20 si está vacío. |
| **Files in a workspace** | Cuántos archivos puede contener el espacio de trabajo de un agente del usuario; las carpetas no cuentan. 500 si está vacío. |
| **Checkpoints of a run (MB)** | Cuánto pueden ocupar en total los [puntos de control](../agents.md#checkpoints) de una ejecución (puntos de restauración de la copia de trabajo y de su espacio de trabajo); parte de ese espacio se reserva para los automáticos. 100 si está vacío. |

![Los ajustes del permiso Agents](../../images/permissions-settings-agents.png)

Los de las notificaciones:

| Ajuste | |
|---|---|
| **Allow addresses on this machine and in private networks** | Un subpermiso: la casilla solo puede marcarse mientras **Notifications** está concedido (si no, aparece en gris, y quitar el permiso la desmarca). Sin ella solo se llama a direcciones de internet: nada de `localhost`, nada de una red privada (`10.x.x.x`, `172.16-31.x.x`, `192.168.x.x`), nada de una dirección de enlace local o compartida (`169.254.x.x`, que es donde los hosts en la nube guardan su servicio de metadatos, `100.64-127.x.x`), ni las direcciones locales únicas de IPv6. Márcala para un usuario cuyo ntfy o Gotify funcione en la misma máquina o en tu propia red. Desactivado por defecto. Un nombre se comprueba por las direcciones a las que se resuelve, así que un nombre que apunta a una dirección privada también se rechaza, y también uno que no se puede encontrar. |

Los administradores no necesitan ni el permiso ni el subpermiso: reciben los mensajes y pueden usar direcciones locales. En un servidor compartido con personas en las que no confías del todo, deja desactivado el subpermiso: de lo contrario, el servidor haría peticiones a cualquier cosa que escriba el usuario, dentro de tu red.

<a id="when-permissions-were-introduced"></a>

## Cuándo se introdujeron los permisos

Antes de los permisos, **Users can create their own models** y el interruptor **Enabled** del asistente de ayuda (con **Apply limits** y **Allow navigating the interface**) eran ajustes de toda la instalación. Marginalia los pasó a permisos al actualizarse: cada usuario que existía entonces recibió **Create own models** si todos podían crear modelos (que era el valor por defecto), y **Help assistant** con los mismos dos ajustes si el asistente estaba activado. Nada cambia para ellos hasta que lo modifiques; los usuarios que añadas a partir de ahora empiezan sin ningún permiso.
