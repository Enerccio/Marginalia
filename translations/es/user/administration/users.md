---
label: Usuarios
order: 100
---

# Usuarios

La pestaña **Usuarios** de la página de administración muestra todas las cuentas con su **Nombre de usuario**, **Nombre completo** y si son **Administrador**.

![La pestaña Usuarios](../../images/admin-users.png)

<a id="adding-a-user"></a>

## Añadir un usuario

1. Haz clic en **Añadir usuario**.
2. Rellena **Nombre de usuario**, **Nombre completo**, **Contraseña** y **Repetir la contraseña**.
3. Marca **Administrador** si el usuario debe gestionar la instalación.
4. Haz clic en **Aceptar**.

Entrega el nombre de usuario y la contraseña a la persona; después puede cambiar la contraseña por sí misma (véase [Cuenta y ajustes](../account.md#changing-the-password)).

!!!warning Establece siempre una contraseña
Marginalia acepta una cuenta con contraseña vacía; en ese caso, cualquiera puede iniciar sesión solo con el nombre de usuario. Hazlo únicamente en la aplicación de escritorio, en tu propio ordenador. Al crear un usuario sin contraseña, primero se pide confirmación.
!!!

El nombre de usuario debe ser único.

<a id="editing-a-user"></a>

## Editar un usuario

El botón del lápiz abre el mismo diálogo para un usuario existente:

- cambiar el nombre de usuario, el nombre completo y la marca de administrador;
- dejar ambos campos de contraseña vacíos para mantener la contraseña del usuario tal como está;
- para establecer una **nueva contraseña**, introdúcela en los dos campos de contraseña;
- para **borrar la contraseña**, marca **Borrar la contraseña** (los campos de contraseña se desactivan) y confirma. La cuenta quedará entonces sin contraseña.

- para que los navegadores del usuario vuelvan a pedir inicio de sesión, marca **Borrar los inicios de sesión guardados (recordarme)**; solo se muestra mientras el usuario tenga inicios de sesión guardados. Una sesión que esté abierta seguirá abierta, pero las cookies de «recordarme» del usuario dejarán de funcionar.

No necesitas la contraseña actual del usuario para nada de esto. Una contraseña nueva o borrada cierra todos los inicios de sesión guardados del usuario.

<a id="resetting-a-forgotten-password"></a>

### Restablecer una contraseña olvidada

Marginalia no ofrece restablecimiento de contraseña por correo electrónico. Puedes:

- establecer aquí una nueva contraseña y comunicársela al usuario, que luego podrá cambiarla con **Cambiar la contraseña**; o
- marcar **Borrar la contraseña**, indicar al usuario que inicie sesión con su nombre de usuario y una contraseña **vacía**, y que establezca de inmediato una nueva contraseña con **Cambiar la contraseña** (deja *Contraseña actual* vacía). Así, el administrador nunca conoce la nueva contraseña.

!!!warning
Mientras el usuario no establezca una nueva contraseña, cualquiera que conozca el nombre de usuario puede iniciar sesión en una cuenta con la contraseña borrada. En un servidor, borra la contraseña solo cuando el usuario pueda iniciar sesión enseguida.
!!!

El último administrador no puede perder la marca de administrador: *No se puede eliminar ni degradar al último administrador.*

<a id="users-and-providers"></a>

## Usuarios y proveedores

Añadir un [proveedor de inferencia](../inference-providers.md) requiere el permiso **Crear modelos propios**; los administradores siempre lo tienen. Un usuario sin este permiso solo puede usar los proveedores que los administradores [comparten](../inference-providers.md#shared-providers). Los proveedores que el usuario ya tiene siguen funcionando y pueden seguir editándose. Véase [Permisos](permissions.md).

<a id="permissions"></a>

## Permisos

El botón de la llave en la fila de un usuario abre sus [permisos](permissions.md): lo que el usuario puede hacer además de lo habitual, como añadir sus propios modelos o consultar al asistente de ayuda. Un usuario nuevo no tiene ninguno; concédele lo que necesite después de añadirlo.

<a id="deleting-a-user"></a>

## Eliminar un usuario

El botón de la papelera elimina la cuenta tras confirmarlo. No puedes eliminar tu propia cuenta ni al último administrador.

El usuario ya no puede iniciar sesión, los inicios de sesión guardados en sus dispositivos dejan de funcionar y el nombre de usuario queda libre para una cuenta nueva.

Sus libros, lorebooks, proveedores de inferencia (con sus claves API), protocolos, etiquetas y ajustes desaparecen con la cuenta: nadie puede verlos ya, y la siguiente [limpieza](cleanup.md) los elimina de la base de datos junto con la cuenta.

Los archivos del usuario (imágenes y recursos subidos) se conservan: su carpeta en el directorio de datos se renombra a `<login>-deleted` (`<login>-deleted-2`... si ese nombre ya está ocupado), de modo que una cuenta nueva con el mismo nombre de usuario empieza con una carpeta vacía. Elimina a mano la carpeta renombrada cuando ya no la necesites.
