---
label: Gestión de extensiones
order: 70
---

# Gestión de extensiones

La pestaña **Extensions** de la página de administración instala y elimina [extensiones](../extensions/index.md) para toda la instalación.

![La pestaña Extensions](../../images/admin-extensions.png)

<a id="installing"></a>

## Instalar

Haga clic en **Load Extension (.jar)** (o suelte el archivo sobre él) y elija el archivo JAR de la extensión. Se copia a la carpeta `extensions` de la [carpeta de datos](index.md#the-data-folder) y se inicia de inmediato; *Extension loaded successfully.* lo confirma. La extensión aparece en la lista:

| Columna | |
|---|---|
| **ID** | El número interno de la extensión en esta ejecución. |
| **Extension Name** | Su nombre. |
| **Version** | Su versión. El botón **#** junto a ella muestra el hash SHA-256 del JAR de la extensión al pasar el cursor; haga clic para copiar el hash, por ejemplo para compararlo con el publicado por el autor. |
| **State** | *Active* cuando está en ejecución; *Stopped* cuando la detuvo (véase [Parar y arrancar](#stopping-and-starting)); *Installed* o *Resolved* cuando no pudo arrancar (véase [Cuando una extensión falla](#when-an-extension-fails)). |
| **Verification** | *Valid* o *Invalid*; haga clic para leer el informe de la comprobación que hizo Marginalia antes de iniciar la extensión. |
| **Actions** | **Stop** / **Start**, **Log** y **Unload**. |

Los usuarios ven la extensión en las ventanas que abran a partir de ese momento: un libro que ya estaba abierto debe cerrarse y volver a abrirse, o recargarse la página.

También se pueden copiar archivos JAR directamente en la carpeta `extensions`, por ejemplo en un servidor sin acceso a la interfaz; se cargan en el siguiente arranque. Así es también como se puede preparar la configuración de Docker: coloque los JAR en `./data/extensions`. Los JAR también se pueden compilar en Docker; véase [Plugins](../getting-started/docker-server.md#plugins) en la guía del servidor.

!!!warning Use extensiones compiladas para esta versión
Las extensiones modifican partes de la interfaz de una versión concreta de Marginalia. Use extensiones compiladas a partir del mismo código fuente que la aplicación (véase el [README de los plugins](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)).
Una extensión incompatible puede no cargarse o romper partes de la aplicación. La API de extensiones no está congelada en la 1.0, así que cuente con actualizar las extensiones junto con Marginalia.
!!!

<a id="stopping-and-starting"></a>

## Parar y arrancar

**Stop** descarga la extensión de inmediato sin eliminarla: desaparece de las pantallas a las que añadía elementos, pero su JAR y sus datos permanecen, y figura en la lista con el estado *Stopped*. El botón pasa entonces a leer **Start**, que la vuelve a cargar (antes se comprueba, como al arrancar la aplicación; si la comprobación falla, sigue parada y se muestra el informe). Una extensión parada sigue parada después de reiniciar Marginalia. Subir una nueva versión de una extensión parada la arranca.

La parada se recuerda mediante un archivo vacío con el mismo nombre que el JAR, al que se añade `.STOPPED` (`sidequery.jar.STOPPED`), en la carpeta `extensions`. En un servidor sin acceso a la interfaz, créelo para evitar que una extensión arranque, o bórrelo para que la extensión arranque en el siguiente inicio.

<a id="the-extension-log"></a>

## El registro de la extensión

**Log** abre el registro de la extensión: lo que la propia extensión escribió con el registrador que Marginalia ofrece a las extensiones ([Registro](../../developer/plugins/overview.md#logging)). Muestra la última parte del archivo; **Refresh** lo vuelve a leer y **Clear** lo borra. El archivo es `extensions/<name>.jar.log` (por ejemplo `sidequery.jar.log`); cuando supera los 2 MB se mueve a `.log.old` y se inicia uno nuevo. Se borra al descargar la extensión. Las extensiones que no registran nada tienen un registro vacío. Lo que registra la extensión también va al registro de la aplicación (llamado `extension.<name>`, al nivel de cada línea), así que también está en el registro del servidor; los problemas de Marginalia en sí solo aparecen ahí.

![El registro de una extensión](../../images/extension-log.png)

<a id="removing"></a>

## Eliminar

**Unload** pide confirmación y luego detiene la extensión de inmediato y borra su JAR (y su registro) de la carpeta `extensions`. Sus datos se conservan (véase [Dónde se guardan los datos de las extensiones](../extensions/index.md#where-extension-data-is-kept)); volver a cargarla devuelve los datos.

<a id="updating"></a>

## Actualizar

Para instalar una versión más nueva de una extensión, use **Load Extension (.jar)** con el nuevo archivo; el nombre del archivo no importa. La versión instalada se descarga, su JAR se borra y la nueva versión se inicia en su lugar. Si la nueva versión no se puede cargar, la subida muestra el error y la versión anterior sigue en ejecución.

Las extensiones también se pueden instalar, actualizar, parar y eliminar desde un script, con una [clave de API](../account.md#api-keys) que tenga el permiso *Manage the extensions*. Un script puede comparar primero el commit que ejecuta el servidor con el código fuente con el que se compilaron los plugins, para saber si pueden instalarse sin actualizar Marginalia en sí (véase el [manual para desarrolladores](../../developer/api.md#plugins-without-a-full-update)).

<a id="when-an-extension-fails"></a>

## Cuando una extensión falla

Si una extensión subida no se puede cargar, la subida muestra *Failed to load extension: ...* y su JAR no se conserva.

Marginalia comprueba una extensión frente a la versión en la que se ejecuta antes de iniciarla. Una extensión que no encaja (normalmente, una compilada para otra versión de Marginalia) no se carga. Una subida muestra el informe de la comprobación y no se conserva; una extensión en la carpeta `extensions` permanece en la lista, sin iniciar, con **Invalid** en la columna *Verification*, y el informe figura en el registro. La comprobación se repite después de actualizar Marginalia, así que una extensión que funcionaba antes puede ser rechazada después: obtenga la versión de la extensión compilada para la nueva versión de Marginalia y cárguela. Mientras una extensión no sea válida, una **barra roja en la parte superior del espacio de trabajo** avisa a todos: un administrador ve qué extensiones son al pasar el cursor, y al hacer clic en la barra abre esta página; a los demás usuarios se les pide que avisen al administrador (una extensión que usted mismo detuvo no cuenta). Junto al JAR de cada extensión encontrará un pequeño archivo `.valid` o `.invalid` con el informe; se elimina junto con la extensión y puede dejarse tal cual.

![La barra roja sobre el espacio de trabajo cuando una extensión no es válida](../../images/invalid-extensions-warning.png)

![El informe de verificación de una extensión que no encaja, y una extensión parada (botón Start)](../../images/extension-verification.png)

Una extensión en la carpeta `extensions` que no logra arrancar (por ejemplo, una compilada para otra versión) no impide que se carguen las demás. Aparece en la lista con el estado *Installed* o *Resolved* en lugar de *Active*, y se puede eliminar con **Unload**. Un archivo que no es una extensión se omite y no aparece en la lista; bórrelo de la carpeta `extensions`. El registro (`docker compose logs`, o `desktop/logs/marginalia.log` en la aplicación de escritorio) tiene los detalles del error.
