---
label: Aplicación de escritorio
order: 100
---

# Aplicación de escritorio

La aplicación de escritorio es una versión autocontenida de Marginalia para un solo ordenador. Incluye su propio entorno de ejecución de Java y su propio servidor, escucha solo en tu ordenador y se abre en tu navegador predeterminado.

<a id="download"></a>

## Descarga

Descarga el instalador o el archivo comprimido para tu sistema desde la
[página de versiones](https://github.com/Enerccio/Marginalia/releases). Para cada sistema se publican cuatro variantes:

| Archivo | Contenido |
|---|---|
| `marginalia-<version>-<os>-<arch>.deb` / `.dmg` / `.msi` | El instalador: `.deb` en Linux (Debian, Ubuntu y derivados), `.dmg` en macOS, `.msi` en Windows |
| `marginalia-<version>-<os>-<arch>-full.deb` / `.dmg` / `.msi` | El mismo instalador **con todos los [complementos](../extensions/index.md) integrados** ya instalados (ver más abajo) |
| `marginalia-<version>-<os>-<arch>-app` | La aplicación: `Marginalia.app` en macOS, `Marginalia.exe` en Windows, `bin/Marginalia` en Linux |
| `marginalia-<version>-<os>-<arch>` | Una carpeta portátil que se inicia con un script, con [opciones](#options) de línea de comandos |

Hay compilaciones para Windows x64, macOS (Apple Silicon e Intel) y Linux (x64 y arm64). `<arch>` en el
nombre del archivo indica `x86_64` para procesadores Intel y AMD de 64 bits (Windows, macOS Intel, Linux) y `aarch64` para ARM
(Apple Silicon, Linux arm64).

<a id="install-and-start"></a>

## Instalación e inicio

+++ macOS
Con el instalador: abre la `.dmg` y arrastra *Marginalia* a *Aplicaciones*. Con el archivo comprimido:

1. Extrae el archivo y mueve `Marginalia.app` a *Aplicaciones*.
2. Ábrela. La aplicación todavía no está firmada, así que la primera vez macOS se niega a abrirla. Haz clic derecho sobre la aplicación y elige
   *Abrir*, o ejecuta:

   ```sh
   xattr -dr com.apple.quarantine /Applications/Marginalia.app
   ```
+++ Windows
Con el instalador: ejecuta `.msi`. Se instala para tu usuario (no necesita derechos de administrador), añade Marginalia al menú Inicio y al escritorio, y un `.msi` más reciente actualiza la versión instalada (tus datos permanecen en `.marginalia`). Con el archivo comprimido:

1. Extrae el archivo.
2. Ejecuta `Marginalia\Marginalia.exe`.
3. SmartScreen puede advertir de un editor desconocido. Haz clic en *Más información* → *Ejecutar de todos modos*.
+++ Linux
Con el `.deb` (Debian, Ubuntu y derivados):

```sh
sudo apt install ./marginalia-<version>-linux-<arch>.deb
```

Se instala en `/opt/marginalia` y añade *Marginalia* al menú de aplicaciones; `sudo apt remove marginalia` lo elimina (tus datos en `~/.marginalia` permanecen). Con el archivo comprimido:

1. Extrae el archivo.
2. Ejecuta `Marginalia/bin/Marginalia`.
+++ Carpeta portátil
1. Extrae el archivo.
2. Ejecuta `bin/marginalia` (`bin\marginalia.bat` en Windows).
+++

El inicio tarda unos segundos. Después, Marginalia se abre en tu navegador en `http://127.0.0.1:8765` y muestra el diálogo de
[primer inicio](first-start.md).

<a id="installer-with-the-plugins"></a>

### Instalador con los complementos

El instalador **completo** (`-full` en el nombre del archivo) es la misma aplicación y trae todos los complementos que forman parte de Marginalia
(los proveedores de Anthropic y Google, Reviewer, Beta Readers, Side Query, Arc Outline, los generadores y el resto). La
primera vez que Marginalia se inicia, los complementos se copian en `~/.marginalia/extensions` y aparecen en *Admin → Extensiones*;
con el instalador sencillo los instalas tú mismo allí, uno a uno.

- Un complemento se instala una sola vez. Si lo eliminas en *Admin → Extensiones*, permanece eliminado, también después de actualizar a
  una versión más nueva de Marginalia (que trae los complementos nuevos y los coloca junto a los antiguos; Marginalia usa los
  más recientes y conserva los JAR anteriores en `extensions/old-versions`). Un complemento que es nuevo en una actualización sí se instala.
- Instalar el instalador completo sobre el sencillo (o al revés) conserva tus datos y ajustes, y lo mismo ocurre con
  cada actualización.
- El instalador sencillo no elimina complementos. Los complementos que instalaste tú mismo nunca se tocan.

<a id="tray-icon"></a>

## Icono de la bandeja

Mientras se ejecuta, Marginalia muestra su icono (una página de manuscrito con un pequeño árbol ramificado en el margen) en la bandeja del sistema (en la barra de menús en macOS). Su menú tiene dos
elementos:

- **Abrir Marginalia**: abre Marginalia de nuevo en el navegador, por ejemplo después de cerrar la pestaña.
- **Salir de Marginalia**: detiene el servidor. Cerrar la pestaña del navegador no detiene Marginalia.

Iniciar la aplicación una segunda vez mientras ya se ejecuta solo la abre en el navegador.

![El icono de la bandeja y su menú](../../images/desktop-tray.png)

<a id="where-your-data-is"></a>

## Dónde están tus datos

| Qué | Dónde |
|---|---|
| Base de datos, copias de seguridad, extensiones | `~/.marginalia` (`%USERPROFILE%\.marginalia` en Windows) |
| Registros | `~/.marginalia/desktop/logs/marginalia.log` (la ejecución anterior se conserva como `marginalia.log.1`) |

Para trasladar Marginalia a otro ordenador, ciérralo y copia la carpeta `.marginalia` (junto con `secret.key`, que
descifra las claves de API guardadas). Antes de cambios importantes, haz una
[copia de seguridad de la base de datos](../administration/database-backups.md).

<a id="options"></a>

## Opciones

El lanzador portátil acepta estas opciones:

```
marginalia --port 8765        # port to listen on; a free one is picked when 8765 is taken
marginalia --host 127.0.0.1   # address to listen on
marginalia --home /path       # directory that holds the .marginalia folder (default: your home directory)
marginalia --no-browser       # don't open the browser
marginalia --no-tray          # no tray icon; stop with Ctrl+C
marginalia --help
```

!!!warning Otras direcciones
Con `--host 0.0.0.0` (u otra dirección) Marginalia es accesible desde tu red mediante HTTP sin cifrar. Para acceder desde
otros dispositivos, usa el [servidor](docker-server.md) detrás de HTTPS.
!!!

<a id="updating"></a>

## Actualización

Cierra Marginalia, reemplaza la aplicación (o la carpeta portátil) por la nueva versión y vuelve a iniciarla. Tus datos permanecen en
`.marginalia` y se actualizan automáticamente en el primer inicio; antes de que cambie la estructura de la base de datos,
Marginalia guarda una [copia de la misma](../administration/database-backups.md#copy-before-an-upgrade) en `db-backups`. Las extensiones se compilan para una versión concreta de Marginalia, así que
actualízalas al mismo tiempo (ver [Gestionar extensiones](../administration/extensions.md)).

<a id="uninstalling"></a>

## Desinstalación

Elimina la aplicación o la carpeta. Para eliminar también tus datos, borra la carpeta `.marginalia`.
