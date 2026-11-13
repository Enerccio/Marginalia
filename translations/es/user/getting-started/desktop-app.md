---
label: Aplicación de escritorio
order: 100
---

# Aplicación de escritorio

La aplicación de escritorio es una versión autocontenida de Marginalia para un solo ordenador. Incluye su propio entorno de ejecución de Java y su propio servidor, escucha solo en tu ordenador y se abre en tu navegador predeterminado.

<a id="download"></a>

## Descarga

Descarga el instalador o el archivo comprimido para tu sistema desde la [página de versiones](https://github.com/Enerccio/Marginalia/releases). Para cada sistema se publican cuatro variantes:

| Archivo | Contenido |
|---|---|
| `marginalia-<version>-<os>-<arch>.deb` / `.dmg` / `.msi` | El instalador: `.deb` en Linux (Debian, Ubuntu y derivados), `.dmg` en macOS, `.msi` en Windows |
| `marginalia-<version>-<os>-<arch>-full.deb` / `.dmg` / `.msi` | El mismo instalador, con todos los [complementos](../extensions/index.md) integrados ya instalados (ver más abajo) |
| `marginalia-<version>-<os>-<arch>-app` | La aplicación: `Marginalia.app` en macOS, `Marginalia.exe` en Windows, `bin/Marginalia` en Linux |
| `marginalia-<version>-<os>-<arch>` | Una carpeta portable que se inicia con un script, con [opciones](#options) de línea de comandos |

Hay versiones para Windows x64, macOS (Apple Silicon e Intel) y Linux (x64 y arm64). `<arch>` en el nombre del archivo es `x86_64` para procesadores Intel y AMD de 64 bits (Windows, macOS Intel, Linux) y `aarch64` para ARM (Apple Silicon, Linux arm64).

<a id="install-and-start"></a>

## Instalación e inicio

+++ macOS
Con el instalador: abre el `.dmg` y arrastra *Marginalia* a *Applications*. Con el archivo comprimido:

1. Extrae el archivo y mueve `Marginalia.app` a *Applications*.
2. Ábrelo. La aplicación todavía no está firmada, así que la primera vez macOS no la deja abrir. Haz clic con el botón derecho en la aplicación y elige *Abrir*, o ejecuta:

   ```sh
   xattr -dr com.apple.quarantine /Applications/Marginalia.app
   ```
+++ Windows
Con el instalador: ejecuta el `.msi`. Se instala para tu usuario (no necesita derechos de administrador), añade Marginalia al menú Inicio y al escritorio, y un `.msi` más reciente actualiza la versión instalada (tus datos se mantienen en `.marginalia`). Con el archivo comprimido:

1. Extrae el archivo.
2. Ejecuta `Marginalia\Marginalia.exe`.
3. SmartScreen puede avisar de un editor desconocido. Haz clic en *More info* → *Run anyway*.
+++ Linux
Con el `.deb` (Debian, Ubuntu y derivados):

```sh
sudo apt install ./marginalia-<version>-linux-<arch>.deb
```

Se instala en `/opt/marginalia` y añade *Marginalia* al menú de aplicaciones; `sudo apt remove marginalia` lo elimina (tus datos en `~/.marginalia` se mantienen). Con el archivo comprimido:

1. Extrae el archivo.
2. Ejecuta `Marginalia/bin/Marginalia`.
+++ Carpeta portable
1. Extrae el archivo.
2. Ejecuta `bin/marginalia` (`bin\marginalia.bat` en Windows).
+++

El inicio tarda unos segundos. Después, Marginalia se abre en tu navegador en `http://127.0.0.1:8765` y muestra el diálogo del [primer inicio](first-start.md).

<a id="installer-with-the-plugins"></a>

### Instalador con los complementos

El instalador **completo** (`-full` en el nombre del archivo) es la misma aplicación y trae todos los complementos que forman parte de Marginalia (los proveedores de Anthropic y Google, Reviewer, Beta Readers, Side Query, Arc Outline, los generadores y el resto). La primera vez que se inicia Marginalia, los complementos se copian en `~/.marginalia/extensions` y aparecen en *Admin → Extensions*; con el instalador normal, los instalas tú mismo uno a uno.

- Un complemento se instala una sola vez. Si lo eliminas en *Admin → Extensions*, permanece eliminado, también después de actualizar a una versión más reciente de Marginalia (que trae los complementos nuevos y los coloca junto a los antiguos; Marginalia usa los más recientes y guarda los JAR antiguos en `extensions/old-versions`). Un complemento que es nuevo en una actualización se instala.
- Instalar el instalador completo sobre el normal (o al revés) conserva tus datos y ajustes, y lo mismo ocurre con cada actualización.
- El instalador normal no elimina complementos. Los complementos que instalaste tú mismo nunca se tocan.

<a id="tray-icon"></a>

## Icono de la bandeja

Mientras se ejecuta, Marginalia muestra su icono (una página de manuscrito con un pequeño árbol ramificado en el margen) en la bandeja del sistema (en la barra de menús en macOS). Su menú tiene dos elementos:

- **Abrir Marginalia**: vuelve a abrir Marginalia en el navegador, por ejemplo después de cerrar la pestaña.
- **Salir de Marginalia**: detiene el servidor. Cerrar la pestaña del navegador no detiene Marginalia.

Iniciar la aplicación una segunda vez mientras ya se está ejecutando solo la abre en el navegador.

![El icono de la bandeja y su menú](../../images/desktop-tray.png)

<a id="where-your-data-is"></a>

## Dónde están tus datos

| Qué | Dónde |
|---|---|
| Base de datos, copias de seguridad, complementos | `~/.marginalia` (`%USERPROFILE%\.marginalia` en Windows) |
| Registros | `~/.marginalia/desktop/logs/marginalia.log` (la ejecución anterior se conserva como `marginalia.log.1`) |

Para llevar Marginalia a otro ordenador, ciérralo y copia la carpeta `.marginalia` (con `secret.key`, que descifra las claves de API guardadas). Antes de cambios grandes, haz una [copia de seguridad de la base de datos](../administration/database-backups.md).

<a id="options"></a>

## Opciones

El lanzador portable acepta estas opciones:

```
marginalia --port 8765        # puerto en el que escuchar; si 8765 está ocupado, se elige uno libre
marginalia --host 127.0.0.1   # dirección en la que escuchar
marginalia --home /path       # directorio que contiene la carpeta .marginalia (por defecto: tu directorio personal)
marginalia --no-browser       # no abrir el navegador
marginalia --no-tray          # sin icono en la bandeja; detener con Ctrl+C
marginalia --help
```

!!!warning Otras direcciones
Con `--host 0.0.0.0` (u otra dirección), Marginalia es accesible desde tu red mediante HTTP sin cifrar. Para acceder desde otros dispositivos, usa mejor el [servidor](docker-server.md) detrás de HTTPS.
!!!

<a id="updating"></a>

## Actualización

Cierra Marginalia, sustituye la aplicación (o la carpeta portable) por la nueva versión y vuelve a iniciarla. Tus datos se mantienen en `.marginalia` y se actualizan automáticamente en el primer inicio; antes de que cambie la estructura de la base de datos, Marginalia guarda una [copia](../administration/database-backups.md#copy-before-an-upgrade) en `db-backups`. Los complementos se compilan para una versión concreta de Marginalia, así que actualízalos al mismo tiempo (ver [Gestión de complementos](../administration/extensions.md)).

<a id="uninstalling"></a>

## Desinstalación

Elimina la aplicación o la carpeta. Para eliminar también tus datos, borra la carpeta `.marginalia`.
