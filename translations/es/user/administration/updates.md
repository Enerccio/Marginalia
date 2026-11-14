---
label: Actualizaciones
order: 65
---

# Actualizaciones

En un [servidor Docker](../getting-started/docker-server.md) construido a partir de una copia de trabajo de Git (*checkout*), un administrador puede actualizar Marginalia desde la pestaña **Actualizar** de la página Admin, sin iniciar sesión en el servidor: el servidor descarga la versión más reciente de la rama en la que está su copia, construye la aplicación y los [plugins incluidos](../getting-started/docker-server.md#plugins) y se reinicia.

Necesita un pequeño programa en el servidor, el **vigilante de actualizaciones** (*update watcher*), que se [configura una vez](#setting-up-the-watcher). Sin él, la pestaña solo muestra la versión en ejecución y dice que el vigilante no se está ejecutando. Eso mismo es lo que muestran la [aplicación de escritorio](../getting-started/desktop-app.md) y un servidor con la [imagen precompilada](../getting-started/docker-server.md#prebuilt-image): se actualizan instalando la nueva versión.

<a id="the-update-tab"></a>

## La pestaña Actualizar

| | |
|---|---|
| **Running version** | La versión y el commit de Marginalia que se está ejecutando. |
| **Rama** | La rama en la que está la copia de trabajo en el servidor y la rama remota que sigue (por ejemplo `dev`, siguiendo `origin/dev`), el commit más reciente de esa rama remota y cuándo miró el servidor por última vez. El servidor mira cada 15 minutos. |
| Estado | *A new version is available* cuando el commit más reciente no es el que se está ejecutando, *Marginalia is up to date* en caso contrario. Mientras una actualización está en curso, la pestaña lo indica. |
| **Last update** | Cuándo terminó la última actualización iniciada desde aquí y si terminó correctamente o falló. |

El servidor sigue la rama que esté activa allí, sea la que sea: para cambiar una instalación a otra rama, cambia a ella con `git switch` en la copia de trabajo del servidor; la pestaña mostrará la nueva rama tras la siguiente comprobación.

| Botón | |
|---|---|
| **Actualizar y reiniciar** | Pide al servidor que se actualice, tras una confirmación. También construye y reinicia aunque no haya nada nuevo (úsalo después de cambiar `docker-compose.yml`, por ejemplo). |
| **Actualizar** | Vuelve a leer el estado. La pestaña no se actualiza sola. |
| **Registro** | La salida de la última actualización: lo que escribieron Git y la compilación. |

<a id="what-an-update-does"></a>

## Qué hace una actualización

1. Se construye la nueva versión: `git pull`, los plugins y la imagen. Esto tarda varios minutos y **Marginalia sigue funcionando** mientras tanto. La pestaña muestra *The update is running*; **Registro** muestra por dónde va.
2. Marginalia se detiene, los plugins nuevos se copian en `data/extensions` (sustituyendo a los instalados) y arranca la nueva versión. Esto tarda uno o dos minutos (véase [Salud](../getting-started/docker-server.md#health)). Todo el que esté trabajando se interrumpe, y lo que se esté generando se detiene: la confirmación indica cuántos [trabajos](jobs.md) se están ejecutando en ese momento, así que elige un momento tranquilo.
3. La base de datos se actualiza al arrancar la nueva versión, después de que se guarde una [copia de ella](database-backups.md#copy-before-an-upgrade). Haz también antes tu propia [copia de seguridad de la base de datos](database-backups.md).

Si la compilación falla, no se detiene nada: la versión anterior sigue en marcha, **Last update** indica *failed* y el **Registro** explica el motivo. La siguiente actualización vuelve a compilar, también cuando no haya nada nuevo que descargar.

<a id="setting-up-the-watcher"></a>

## Configurar el vigilante

Marginalia se ejecuta dentro de un contenedor y no puede acceder ni a la copia de trabajo de Git ni a Docker; darle ese acceso convertiría a todo el que entre en la aplicación en administrador del servidor. Así que solo pide: escribe un archivo en su [carpeta de datos](index.md#the-data-folder), y el vigilante, que se ejecuta en el servidor fuera del contenedor, hace el trabajo. El vigilante es el script `tools/update-watcher/watch.sh` de la copia de trabajo; lo único que llega a ejecutar es `rebuild.sh` de esa misma copia (véase [Actualizaciones automáticas](../getting-started/docker-server.md#automatic-updates)).

<a id="linux-with-systemd"></a>

### Linux con systemd

En la copia de trabajo del servidor (la carpeta con `docker-compose.yml`):

```sh
sudo tools/update-watcher/install.sh
```

Instala e inicia tres unidades:

| Unidad | |
|---|---|
| `marginalia-update.service` | Ejecuta el vigilante una vez. |
| `marginalia-update.path` | Lo inicia en cuanto la aplicación pide una actualización. |
| `marginalia-update.timer` | Lo inicia dos minutos después del arranque y luego cada 15 minutos, para buscar nuevos commits. |

Abre **Admin → Actualizar** y haz clic en **Actualizar**: la pestaña muestra la rama y el commit más reciente. Eso es todo.

| Opción | |
|---|---|
| `--name NAME` | El nombre de las unidades (por defecto `marginalia-update`). Dos copias de trabajo en un mismo equipo (una instalación de prueba y otra de producción) necesitan cada una su propio nombre. |
| `--user USER` | Ejecuta el vigilante como este usuario en lugar de root, véase [más abajo](#who-the-watcher-runs-as). |
| `--uninstall` | Detiene y elimina las unidades (con el mismo `--name`). |

`systemctl status marginalia-update.service` muestra la última ejecución y `journalctl -u marginalia-update.service` su salida (el mismo texto que **Registro**, y el motivo cuando el propio vigilante no pudo funcionar). Las unidades contienen la ruta de la copia de trabajo: después de mover la copia de trabajo, ejecuta `install.sh` otra vez.

<a id="without-systemd-cron"></a>

### Sin systemd (cron)

Ejecuta el vigilante cada minuto; no hace nada cuando nadie ha pedido una actualización y miró el remoto hace menos de 15 minutos, así que no supone ningún coste:

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Pon la línea en el crontab de root (`sudo crontab -e`), o en el de otro usuario, véase más abajo. Una actualización empieza entonces en menos de un minuto tras el clic.

<a id="settings"></a>

### Ajustes

El vigilante lee estas variables de entorno (con systemd: líneas `sudo systemctl edit marginalia-update.service` y `Environment=NAME=value` bajo `[Service]`):

| Variable | |
|---|---|
| `MARGINALIA_DATA_DIR` | La carpeta de datos en el servidor, cuando no es `data` dentro de la copia de trabajo (el lado izquierdo del volumen en `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | Los argumentos de `rebuild.sh`, por defecto `--force`. En un servidor sin memoria para la aplicación y la compilación a la vez, usa `--force --stop-first`: Marginalia se detiene entonces antes de la compilación y está caído durante todo el proceso. |
| `MARGINALIA_CHECK_MINUTES` | Cuánto puede tener de antigüedad el estado antes de que una ejecución sin petición vuelva a mirar el remoto; por defecto, 15. Con systemd, el temporizador decide cada cuánto se ejecuta el vigilante. |

<a id="who-the-watcher-runs-as"></a>

### Con qué usuario se ejecuta el vigilante

Por defecto el vigilante se ejecuta como **root**: necesita usar Docker y escribir en la carpeta de datos, que pertenece al usuario del contenedor. Git se ejecuta como propietario de la copia de trabajo, así que los archivos de Git siguen siendo suyos.

El vigilante ejecuta lo que contiene la rama (`rebuild.sh`, el `Dockerfile`), igual que lo hace cualquier actualización manual: sigue solo un repositorio en el que confíes, y deja la copia de trabajo escribible solo para su propietario, porque quien pueda modificar `rebuild.sh` o `watch.sh` allí ejecuta comandos como el usuario del vigilante.

Con `--user USER` (o con el crontab de ese usuario) se ejecuta como otra persona. Ese usuario debe poder usar Docker (lo que en la mayoría de sistemas equivale a tener root), debe ser propietario de la copia de trabajo y debe poder escribir en `data/update` y `data/extensions`, que normalmente pertenecen al usuario del contenedor.

<a id="windows-and-macos"></a>

### Windows y macOS

No hay vigilante preparado para Docker Desktop. Actualiza con `rebuild.bat` / `rebuild.sh` a mano o desde una tarea programada, véase [Actualizaciones automáticas](../getting-started/docker-server.md#automatic-updates).

<a id="troubleshooting"></a>

## Solución de problemas

| | |
|---|---|
| *The update watcher does not run on the server* | El vigilante no escribió estado durante una hora, o nunca. Comprueba `systemctl status marginalia-update.timer marginalia-update.path` y `journalctl -u marginalia-update.service`. Si la carpeta de datos no es `data` dentro de la copia de trabajo, define `MARGINALIA_DATA_DIR`. |
| *Branch ... follows no remote branch* | La copia de trabajo está en una rama sin rama remota asociada (o en un commit suelto), así que no hay nada que descargar. En el servidor: `git switch <branch>` o `git branch --set-upstream-to=origin/<branch>`. |
| *The server could not reach the Git repository* | `git fetch` falló en la última comprobación (sin red, o un repositorio privado sin credenciales para el propietario de la copia de trabajo). El commit más reciente que se muestra puede ser antiguo. |
| La actualización se pidió y no pasa nada | Con cron empieza en menos de un minuto. Una petición que nadie atendió durante una hora se descarta, para que no arranque por sorpresa cuando se repare el vigilante. |
| *Last update: ..., failed* | Lee **Registro**. Un `git pull` fallido suele significar que la copia de trabajo tiene cambios propios (archivos editados, commits locales): resuélvelo en el servidor. Una compilación fallida deja la versión anterior en marcha. |

Los archivos del vigilante están en `update` de la carpeta de datos: `status.properties` (lo que muestra la pestaña), `update.log` (la última actualización) y, durante un momento, `request`.
