---
label: Gestión de extensiones
order: 70
---

# Gestión de extensiones

La pestaña **Extensiones** de la página de administración instala y elimina [extensiones](../extensions/index.md) para toda la instalación.

![La pestaña Extensiones](../../images/admin-extensions.png)

<a id="installing"></a>

## Instalación

Haga clic en **Cargar extensión (.jar)** (o arrastre el archivo sobre el botón) y elija el archivo JAR de la extensión. Se copia a la carpeta `extensions` de la [carpeta de datos](index.md#the-data-folder) y se inicia de inmediato; *Extension loaded successfully.* confirma que se ha cargado. La extensión aparece en la lista:

| Columna | |
|---|---|
| **ID** | El número interno de la extensión en esta ejecución. |
| **Nombre de la extensión** | Su nombre. |
| **Versión** | Su versión. El botón **#** que aparece junto a ella muestra el hash SHA-256 del JAR de la extensión al pasar el cursor sobre él; haga clic para copiar el hash, por ejemplo para compararlo con el que publica el autor. |
| **Estado** | *Activa* cuando está en ejecución; *Detenida* cuando la ha detenido usted (véase [Detención e inicio](#stopping-and-starting)); *Instalada* o *Resuelta* cuando no ha conseguido iniciarse (véase [Cuando una extensión falla](#when-an-extension-fails)). |
| **Verificación** | *Válido* o *No válido*; haga clic para leer el informe de la comprobación que hizo Marginalia antes de iniciar la extensión. |
| **Acciones** | **Detener** / **Iniciar**, **Registro** y **Descargar**. |

Los usuarios ven la extensión en las ventanas que abran a partir de ese momento; un libro que ya estuviera abierto debe cerrarse y volver a abrirse, o bien recargarse la página.

Los archivos JAR también pueden copiarse directamente en la carpeta `extensions`, por ejemplo en un servidor sin acceso a la interfaz; se cargarán en el próximo inicio. Así es también como puede prepararse la configuración con Docker: coloque los JAR en `./data/extensions`. Los JAR también pueden compilarse en Docker; véase [Plugins](../getting-started/docker-server.md#plugins) en la guía del servidor.

!!!warning Use extensiones compiladas para esta versión
Las extensiones modifican partes de la interfaz de usuario de una versión concreta de Marginalia. Use extensiones compiladas a partir del mismo código fuente que la aplicación (véase el [README de los plugins](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md)). Una extensión incompatible puede no cargarse o romper partes de la aplicación. La API de extensiones no está congelada en la 1.0, así que cuente con actualizar las extensiones junto con Marginalia.
!!!

<a id="stopping-and-starting"></a>

## Detención e inicio

**Detener** descarga la extensión de inmediato sin eliminarla: desaparece de las pantallas en las que se había añadido, pero su JAR y sus datos se conservan, y figura en la lista con el estado *Detenida*. El botón pasa entonces a leer **Iniciar**, que vuelve a cargarla (antes se comprueba, igual que al arrancar la aplicación; si la comprobación falla, permanece detenida y se muestra el informe). Una extensión detenida sigue detenida después de reiniciar Marginalia. Subir una nueva versión de una extensión detenida la inicia.

La detención se recuerda mediante un archivo vacío con el mismo nombre que el JAR y la extensión `.STOPPED` (`sidequery.jar.STOPPED`) en la carpeta `extensions`. En un servidor sin acceso a la interfaz, cree ese archivo para impedir que una extensión se inicie, o elimínelo para que la extensión se inicie en el próximo arranque.

<a id="the-extension-log"></a>

## El registro de la extensión

**Registro** abre el registro de la extensión: lo que la propia extensión escribió con el logger que Marginalia facilita a las extensiones ([Logging](../../developer/plugins/overview.md#logging)). Muestra la última parte del archivo; **Actualizar** lo vuelve a leer y **Vaciar** lo borra. El archivo es `extensions/<nombre>.jar.log` (por ejemplo, `sidequery.jar.log`); cuando supera los 2 MB se traslada a `.log.old` y se empieza uno nuevo. Se borra al descargar la extensión. Las extensiones que no escriben nada en el registro tienen un registro vacío. Lo que registra la extensión también va al registro de la aplicación (con el nombre `extension.<nombre>`, al nivel de cada línea), por lo que aparece también en el registro del servidor; los problemas de Marginalia en sí solo figuran allí.

![El registro de una extensión](../../images/extension-log.png)

<a id="removing"></a>

## Eliminación

**Descargar** pide confirmación y, a continuación, detiene la extensión de inmediato y borra su JAR (y su registro) de la carpeta `extensions`. Sus datos se conservan (véase [Dónde se guardan los datos de las extensiones](../extensions/index.md#where-extension-data-is-kept)); al volver a cargarla, los datos regresan.

<a id="updating"></a>

## Actualización

Para instalar una versión más reciente de una extensión, use **Cargar extensión (.jar)** con el archivo nuevo; el nombre del archivo no importa. La versión instalada se descarga, su JAR se borra y la nueva versión se inicia en su lugar. Si la nueva versión no consigue cargarse, la subida muestra el error y la versión anterior sigue en funcionamiento.

<a id="when-an-extension-fails"></a>

## Cuando una extensión falla

Si una extensión subida no puede cargarse, la subida muestra *Failed to load extension: ...* y su JAR no se conserva.

Marginalia comprueba una extensión contra la versión en la que se ejecuta antes de iniciarla. Una extensión que no encaja (normalmente, una compilada para otra versión de Marginalia) no se carga. Al subirla se muestra el informe de la comprobación y no se conserva; una extensión que está en la carpeta `extensions` permanece en la lista, sin iniciarse, con **No válido** en la columna *Verificación*, y el informe queda en el registro. La comprobación se repite después de actualizar Marginalia, por lo que una extensión que funcionaba antes puede ser rechazada después: obtenga la versión de la extensión compilada para la nueva versión de Marginalia y cárguela. Mientras una extensión no sea válida, una **barra roja en la parte superior del espacio de trabajo** avisa a todos: un administrador ve qué extensiones son al pasar el cursor sobre ella y abre esta página haciendo clic en la barra; los demás usuarios deben avisar al administrador (una extensión que usted mismo haya detenido no se cuenta). Junto al JAR de cada extensión encontrará un pequeño archivo `.valid` o `.invalid` con el informe; se elimina junto con la extensión y puede dejarse tal cual.

![La barra roja sobre el espacio de trabajo cuando una extensión no es válida](../../images/invalid-extensions-warning.png)

![El informe de verificación de una extensión que no encaja, y una extensión detenida (botón Iniciar)](../../images/extension-verification.png)

Una extensión de la carpeta `extensions` que no consigue iniciarse (por ejemplo, una compilada para otra versión) no impide que se carguen las demás. Aparece en la lista con el estado *Instalada* o *Resuelta* en lugar de *Activa*, y puede eliminarse con **Descargar**. Un archivo que no es una extensión se omite y no aparece en la lista; elimínelo de la carpeta `extensions`. El registro (`docker compose logs`, o `desktop/logs/marginalia.log` en la aplicación de escritorio) contiene los detalles del error.
