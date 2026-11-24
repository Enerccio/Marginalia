---
label: Actualizaciones
order: 65
---

# Actualizaciones

En un [servidor Docker](../getting-started/docker-server.md) que se construyó desde un checkout de Git, un administrador puede actualizar Marginalia desde la pestaña **Update** de la página Admin, sin iniciar sesión en el servidor: el servidor descarga la versión más reciente de la rama en la que está su checkout, compila la aplicación y los [plugins incluidos](../getting-started/docker-server.md#plugins), y se reinicia.

Necesita un pequeño programa en el servidor, el **update watcher**, que se [configura una sola vez](#setting-up-the-watcher). Sin él, la pestaña solo muestra la versión en ejecución y dice que el watcher no está en marcha. Eso es también lo que muestran la [aplicación de escritorio](../getting-started/desktop-app.md) y un servidor con la [imagen precompilada](../getting-started/docker-server.md#prebuilt-image): se actualizan instalando la nueva versión.

<a id="the-update-tab"></a>

## La pestaña Update

| | |
|---|---|
| **Running version** | La versión y el commit de Marginalia que se está ejecutando. |
| **Branch** | La rama en la que está el checkout del servidor y la rama remota que sigue (por ejemplo `dev`, que sigue a `origin/dev`), el commit más reciente de esa rama remota y cuándo miró el servidor por última vez. El servidor mira cada 15 minutos. |
| Status | *A new version is available* cuando el commit más reciente no es el que se ejecuta; *Marginalia is up to date* en caso contrario. Mientras se ejecuta una actualización, la pestaña lo indica. |
| **Last update** | Cuándo terminó la última actualización que se inició desde aquí y si terminó bien o falló. |

El servidor sigue la rama que esté extraída allí, sea cual sea: para pasar una instalación a otra rama, cámbiala con `git switch` en el checkout del servidor; la pestaña muestra la nueva rama tras la siguiente consulta.

| Botón | |
|---|---|
| **Update and restart** | Pide al servidor que actualice, previa confirmación. También compila y reinicia cuando no hay nada nuevo (úsalo después de cambiar `docker-compose.yml`, por ejemplo). |
| **Refresh** | Vuelve a leer el estado. La pestaña no se actualiza sola. |
| **Log** | La salida de la última actualización: lo que escribieron Git y la compilación. |

Lo mismo se puede hacer desde un script, con una [clave de API](../account.md#api-keys) que tenga el permiso *Update the installation*: leer el estado, pedir una actualización y leer el registro (véase el [manual para desarrolladores](../../developer/api.md)).

<a id="what-an-update-does"></a>

## Qué hace una actualización

1. Se compila la nueva versión: `git pull`, los plugins, la imagen. Esto tarda varios minutos y **Marginalia sigue en marcha** mientras tanto. La pestaña muestra *The update is running*; **Log** indica por dónde va.
2. Marginalia se detiene, los nuevos plugins se copian en `data/extensions` (sustituyendo a los instalados) y arranca la nueva versión. Esto tarda uno o dos minutos (véase [Salud](../getting-started/docker-server.md#health)). Todas las personas que estén trabajando se interrumpen, y se detiene lo que esté generándose: la confirmación indica cuántos [trabajos](jobs.md) están en marcha en ese momento, así que elige un momento tranquilo.
3. La base de datos se actualiza al arrancar la nueva versión, después de guardar una [copia de ella](database-backups.md#copy-before-an-upgrade). Haz también antes una [copia de seguridad](database-backups.md) de la base de datos por tu cuenta.

Cuando la compilación falla, no se detiene nada: sigue ejecutándose la versión anterior, **Last update** dice *failed* y el registro explica el motivo. La siguiente actualización vuelve a compilar, también cuando no hay nada nuevo que descargar.

<a id="setting-up-the-watcher"></a>

## Configurar el watcher

Marginalia se ejecuta dentro de un contenedor y no puede acceder ni al checkout de Git ni a Docker; dar ese acceso haría administrador del servidor a cualquiera que entrara en la aplicación. Así que solo pide: escribe un archivo en la [carpeta de datos](index.md#the-data-folder), y el watcher, que se ejecuta en el servidor fuera del contenedor, hace el trabajo. El watcher es el script `tools/update-watcher/watch.sh` del checkout; lo único que ejecuta es `rebuild.sh` del mismo checkout (véase [Actualizaciones automáticas](../getting-started/docker-server.md#automatic-updates)).

<a id="linux-with-systemd"></a>

### Linux con systemd

En el checkout del servidor (la carpeta con `docker-compose.yml`):

```sh
sudo tools/update-watcher/install.sh
```

Instala y arranca tres unidades:

| Unidad | |
|---|---|
| `marginalia-update.service` | Ejecuta el watcher una vez. |
| `marginalia-update.path` | Lo arranca en cuanto la aplicación pide una actualización. |
| `marginalia-update.timer` | Lo arranca dos minutos después del arranque del sistema y luego cada 15 minutos, para buscar nuevos commits. |

Abre **Admin → Update** y pulsa **Refresh**: la pestaña muestra la rama y el commit más reciente. Eso es todo.

| Opción | |
|---|---|
| `--name NAME` | El nombre de las unidades (por defecto `marginalia-update`). Dos checkouts en el mismo equipo (una instalación de prueba y otra de producción) necesitan cada uno su propio nombre. |
| `--user USER` | Ejecuta el watcher con este usuario en lugar de root, véase [abajo](#who-the-watcher-runs-as). |
| `--uninstall` | Detiene y elimina las unidades (con el mismo `--name`). |

`systemctl status marginalia-update.service` muestra la última ejecución y `journalctl -u marginalia-update.service` su salida (el mismo texto que **Log**, y el motivo cuando el propio watcher no pudo funcionar). Las unidades contienen la ruta del checkout: tras mover el checkout, ejecuta `install.sh` de nuevo.

<a id="without-systemd-cron"></a>

### Sin systemd (cron)

Ejecuta el watcher cada minuto; no hace nada cuando nadie ha pedido una actualización y miró el remoto hace menos de 15 minutos, así que no cuesta nada:

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Pon la línea en el crontab de root (`sudo crontab -e`), o de otro usuario, véase más abajo. Una actualización arranca entonces en menos de un minuto desde el clic.

<a id="settings"></a>

### Ajustes

El watcher lee estas variables de entorno (con systemd: las líneas `sudo systemctl edit marginalia-update.service` y `Environment=NAME=value` bajo `[Service]`):

| Variable | |
|---|---|
| `MARGINALIA_DATA_DIR` | La carpeta de datos en el servidor, cuando no es `data` en el checkout (el lado izquierdo del volumen en `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | Los argumentos de `rebuild.sh`, `--force` por defecto. En un servidor sin memoria para la aplicación y la compilación juntas, usa `--force --stop-first`: Marginalia se detiene entonces antes de compilar y está caída durante todo el proceso. |
| `MARGINALIA_CHECK_MINUTES` | Cuánto puede tener de antigüedad el estado antes de que una ejecución sin petición vuelva a mirar el remoto; 15 por defecto. Con systemd, el temporizador decide cada cuánto se ejecuta el watcher. |

<a id="who-the-watcher-runs-as"></a>

### Con qué usuario se ejecuta el watcher

Por defecto el watcher se ejecuta como **root**: necesita usar Docker y escribir en la carpeta de datos, que pertenece al usuario del contenedor. Git en sí se ejecuta como el propietario del checkout, así que los archivos de Git siguen siendo suyos.

El watcher ejecuta lo que contenga la rama (`rebuild.sh`, el `Dockerfile`), igual que hace cualquier actualización manual: sigue solo un repositorio en el que confíes, y mantén el checkout escribible solo para su propietario, porque quien pueda modificar `rebuild.sh` o `watch.sh` allí ejecuta comandos como el usuario del watcher.

Con `--user USER` (o el crontab de ese usuario) se ejecuta como otra persona. Ese usuario debe poder usar Docker (lo que en la mayoría de sistemas equivale a root), debe ser dueño del checkout y debe poder escribir en `data/update` y `data/extensions`, que normalmente pertenecen al usuario del contenedor.

<a id="windows-and-macos"></a>

### Windows y macOS

No hay un watcher listo para Docker Desktop. Actualiza con `rebuild.bat` / `rebuild.sh` a mano o desde una tarea programada, véase [Actualizaciones automáticas](../getting-started/docker-server.md#automatic-updates).

<a id="troubleshooting"></a>

## Solución de problemas

| | |
|---|---|
| *The update watcher does not run on the server* | El watcher no escribió ningún estado durante una hora, o nunca. Revisa `systemctl status marginalia-update.timer marginalia-update.path` y `journalctl -u marginalia-update.service`. Si la carpeta de datos no es `data` en el checkout, define `MARGINALIA_DATA_DIR`. |
| *Branch ... follows no remote branch* | El checkout está en una rama sin upstream (o en un commit suelto), así que no hay nada que descargar. En el servidor: `git switch <branch>` o `git branch --set-upstream-to=origin/<branch>`. |
| *The server could not reach the Git repository* | `git fetch` falló en la última consulta (sin red, o un repositorio privado sin credenciales para el propietario del checkout). El commit más reciente que se muestra puede ser antiguo. |
| Se pidió una actualización y no ocurre nada | Con cron arranca en menos de un minuto. Una petición que nadie atendió durante una hora se descarta, para que no arranque por sorpresa cuando se repare el watcher. |
| *Last update: ..., failed* | Lee **Log**. Un `git pull` fallido suele significar que el checkout tiene cambios propios (archivos editados, commits locales): resuélvelo en el servidor. Una compilación fallida deja la versión anterior en marcha. |

Los archivos del watcher están en `update` de la carpeta de datos: `status.properties` (lo que muestra la pestaña), `update.log` (la última actualización) y, durante un momento, `request`.
