---
label: Primer inicio
order: 80
---

# Primer inicio

<a id="create-the-administrator"></a>

## Crear el administrador

Cuando Marginalia arranca con una base de datos vacía, pide la primera cuenta en lugar de mostrar la pantalla de inicio de sesión.
Esta cuenta es el **administrador**: puede gestionar usuarios, las copias de seguridad de la base de datos, la limpieza y las extensiones.

![Creación de la cuenta de administrador](../../images/first-start.png)

Rellena:

| Campo | |
|---|---|
| **Nombre de usuario** | El nombre con el que inicias sesión. |
| **Nombre completo** | El nombre que se muestra en la aplicación. |
| **Contraseña**, **Repetir la contraseña** | Ambas deben coincidir. Marginalia también acepta una contraseña vacía; nunca la dejes vacía en un servidor, ya que cualquiera podría entrar como administrador solo con el nombre de usuario. |

*Administrador* está marcado y no se puede cambiar para la primera cuenta. Haz clic en **Aceptar**. La página se recarga y muestra la
pantalla de inicio de sesión.

!!!
Recuerda la contraseña. No existe restablecimiento de contraseña por correo electrónico. Otros administradores pueden asignar una nueva contraseña a un
usuario en *Admin → Users*.
!!!

<a id="log-in"></a>

## Iniciar sesión

Inicia sesión con la nueva cuenta. Con **Mantener la sesión iniciada** marcado, el navegador mantiene la sesión durante 30 días. Esto solo funciona
con HTTPS o en `127.0.0.1` con la aplicación de escritorio (véase [Servidor](docker-server.md#https-and-reverse-proxy)).

![La pantalla de inicio de sesión](../../images/login.png)

La versión de Marginalia y el identificador corto del commit de Git desde el que se compiló (por ejemplo `Marginalia 1.5.0 (93ec118)`) se
muestran debajo del formulario de inicio de sesión.

<a id="the-workspace"></a>

## El espacio de trabajo

Tras iniciar sesión verás el espacio de trabajo. Las pestañas de la izquierda son:

| Pestaña | |
|---|---|
| **Libros** | Tus libros. Al abrir uno se muestra su editor de historias. Véase [Libros](../books/index.md). |
| **Lorebooks** | Notas sobre el mundo y los personajes para el prompt. Véase [Lorebooks](../lorebooks/index.md). |
| **Ajustes** | Tus ajustes personales: modelo y protocolo predeterminados para los libros nuevos, plantillas de prompt, copias de seguridad. Véase [Cuenta y ajustes](../account.md). |
| **Protocolos** | Ajustes de generación. Véase [Protocolos](../protocols.md). |
| **Proveedores de inferencia** | Los modelos a los que se conecta Marginalia. Véase [Proveedores de inferencia](../inference-providers.md). |
| **Recursos** | Los archivos que has subido. Véase [Recursos](../resources.md). |
| **Papelera** | Lo que has eliminado, hasta que un administrador lo limpie. Véase [Papelera](../trash.md). |

En la parte inferior están **Admin** (solo administradores), el selector de **Idioma**, **Cambiar la contraseña** y **Cerrar sesión**
(véase [Administración](../administration/index.md) para las páginas de *Admin*).

![El espacio de trabajo tras el primer inicio de sesión](../../images/workspace.png)

El mismo texto de versión, en letra pequeña, aparece en la esquina inferior izquierda de la página; pasa el cursor sobre él para ver el
identificador completo del commit y la hora del commit y de la compilación. Menciónalo al informar de un problema. Una compilación
ejecutada desde un checkout de desarrollo (`mvn jetty:run`) muestra `dev` en lugar de la versión.

Marginalia recuerda dónde estabas. La próxima vez que inicies sesión (en cualquier dispositivo) abre la misma pestaña, con los mismos
filtros de la lista de libros, el mismo lorebook y las mismas entradas de lorebook abiertas y, si dejaste un libro abierto, ese libro en la
misma pestaña, con la escena, el personaje POV, los personajes presentes y las instrucciones que habías escrito para la siguiente parte. Lo que ya
no existe (un libro, lorebook o etiqueta eliminados) se omite; un libro que fue eliminado no se abre.

Continúa con el [inicio rápido](quick-start.md).
