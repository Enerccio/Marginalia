---
label: Actualizaciones
order: 65
---

# Actualizaciones

En un [servidor Docker](../getting-started/docker-server.md) creado a partir de un checkout de Git, un administrador puede
actualizar Marginalia desde la pestaña **Update** de la página de administración, sin iniciar sesión en el servidor: el servidor descarga
la versión más reciente de la rama en la que está su checkout, compila la aplicación y los
[plugins incluidos](../getting-started/docker-server.md#plugins) y se reinicia.

Hace falta un pequeño programa en el servidor, el **vigilante de actualizaciones**, que se [configura una vez](#setting-up-the-watcher).
Sin él, la pestaña solo muestra la versión en ejecución e indica que el vigilante no está activo. Lo mismo ocurre con la
[aplicación de escritorio](../getting-started/desktop-app.md) y con un servidor que usa la
[imagen precompilada](../getting-started/docker-server.md#prebuilt-image): en ambos casos la actualización consiste en instalar la nueva
versión.

<a id="the-update-tab"></a>

## La pestaña Update

| | |
|---|---|
| **Running version** | La versión y el commit de Marginalia que se está ejecutando. |
| **Branch** | La rama en la que está el checkout del servidor y la rama remota que sigue (por ejemplo `dev`, siguiendo `origin/dev`), el commit más reciente de esa rama remota y cuándo consultó el servidor por última vez. El servidor consulta cada 15 minutos. |
| Status | *A new version is available* cuando el commit más reciente no es el que se ejecuta, *Marginalia is up to date* en caso contrario. Mientras una actualización está en curso, la pestaña lo indica. |
| **Last update** | Cuándo terminó la última actualización iniciada desde aquí y si acabó bien o con error. |

El servidor sigue la rama que esté activa en su checkout, sea cual sea: para cambiar una instalación a otra rama,
ejecuta `git switch` en el checkout del servidor; la pestaña mostrará la nueva rama en la siguiente consulta.

| Button | |
|---|---|
| **Actualizar y reiniciar** | Pide al servidor que se actualice, previa confirmación. También compila y reinicia aunque no haya nada nuevo (útil, por ejemplo, después de cambiar `docker-compose.yml`). |
| **Actualizar** | Vuelve a leer el estado. La pestaña no se actualiza sola. |
| **Registro** | La salida de la última actualización: lo que escribieron Git y la compilación. |

<a id="what-an-update-does"></a>

## Qué hace una actualización

1. Se compila la nueva versión: `git pull`, los plugins y la imagen. Esto tarda varios minutos y **Marginalia sigue
   funcionando** mientras tanto. La pestaña muestra *The update is running*; **Registro** indica por dónde va.
2. Marginalia se detiene, los nuevos plugins se copian a `data/extensions` (sustituyendo a los instalados) y se arranca
   la nueva versión. Esto tarda uno o dos minutos (véase [Estado](../getting-started/docker-server.md#health)).
   Todas las personas que estén trabajando se interrumpen y se detiene lo que se esté generando: la confirmación indica
   cuántos [trabajos](jobs.md) hay en ejecución en ese momento, así que elige un momento tranquilo.
3. La base de datos se actualiza al arrancar la nueva versión, después de que se haya guardado una
   [copia](database-backups.md#copy-before-an-upgrade). Haz también tu propia [copia de seguridad de la base de datos](database-backups.md)
   antes.

Si la compilación falla, no se detiene nada: la versión anterior sigue en ejecución, **Last update** indica *failed* y
el registro explica por qué. La siguiente actualización vuelve a compilar, aunque no haya nada nuevo que descargar.

<a id="setting-up-the-watcher"></a>

## Configurar el vigilante

Marginalia se ejecuta dentro de un contenedor y no puede acceder ni al checkout de Git ni a Docker; darle ese acceso
convertiría a cualquiera que entrase en la aplicación en administrador del servidor. Por eso solo hace una petición: escribe un archivo en su
[carpeta de datos](index.md#the-data-folder), y el vigilante, que se ejecuta en el servidor fuera del contenedor, realiza el
trabajo. El vigilante es el script `tools/update-watcher/watch.sh` del checkout; lo único que ejecuta es `rebuild.sh`
del mismo checkout (véase [Actualizaciones automáticas](../getting-started/docker-server.md#automatic-updates)).

<a id="linux-with-systemd"></a>

### Linux con systemd

En el checkout del servidor (la carpeta con `docker-compose.yml`):

```sh
sudo tools/update-watcher/install.sh
```

Esto instala y arranca tres unidades:

| Unit | |
|---|---|
| `marginalia-update.service` | Ejecuta el vigilante una vez. |
| `marginalia-update.path` | Lo arranca en cuanto la aplicación pide una actualización. |
| `marginalia-update.timer` | Lo arranca dos minutos después del inicio del equipo y luego cada 15 minutos, para buscar nuevos commits. |

Abre **Admin → Actualizar** y haz clic en **Actualizar**: la pestaña muestra la rama y el commit más reciente. Eso es todo.

| Option | |
|---|---|
| `--name NAME` | El nombre de las unidades (por defecto `marginalia-update`). Dos checkouts en el mismo equipo (una instalación de prueba y una de producción) necesitan cada uno su propio nombre. |
| `--user USER` | Ejecuta el vigilante con este usuario en lugar de root; véase [más abajo](#who-the-watcher-runs-as). |
| `--uninstall` | Detiene y elimina las unidades (con el mismo `--name`). |

`systemctl status marginalia-update.service` muestra la última ejecución y `journalctl -u marginalia-update.service` su
salida (el mismo texto que **Registro**, y el motivo cuando el propio vigilante no pudo funcionar). Las unidades contienen la ruta
del checkout: si cambias el checkout de sitio, vuelve a ejecutar `install.sh`.

<a id="without-systemd-cron"></a>

### Sin systemd (cron)

Ejecuta el vigilante cada minuto; no hace nada si nadie ha pedido una actualización y si consultó el remoto hace menos de
15 minutos, así que no tiene coste:

```
* * * * * /path/to/Marginalia/tools/update-watcher/watch.sh >> /var/log/marginalia-update.log 2>&1
```

Añade la línea al crontab de root (`sudo crontab -e`) o al de otro usuario, véase más abajo. Entonces la actualización
empieza en menos de un minuto desde el clic.

<a id="settings"></a>

### Ajustes

El vigilante lee estas variables de entorno (con systemd: `sudo systemctl edit marginalia-update.service` y líneas
`Environment=NOMBRE=valor` bajo `[Service]`):

| Variable | |
|---|---|
| `MARGINALIA_DATA_DIR` | La carpeta de datos del servidor, cuando no es `data` dentro del checkout (el lado izquierdo del volumen en `docker-compose.yml`). |
| `MARGINALIA_REBUILD_ARGS` | Los argumentos de `rebuild.sh`, por defecto `--force`. En un servidor sin memoria suficiente para la aplicación y la compilación a la vez, usa `--force --stop-first`: Marginalia se detiene antes de compilar y queda fuera de servicio durante todo el proceso. |
| `MARGINALIA_CHECK_MINUTES` | Antigüedad máxima del estado antes de que una ejecución sin petición vuelva a consultar el remoto; 15 por defecto. Con systemd, es el temporizador quien decide con qué frecuencia se ejecuta el vigilante. |

<a id="who-the-watcher-runs-as"></a>

### Con qué usuario se ejecuta el vigilante

Por defecto el vigilante se ejecuta como **root**: necesita usar Docker y escribir en la carpeta de datos, que pertenece al
usuario del contenedor. Git se ejecuta como propietario del checkout, así que los archivos de Git siguen siendo suyos.

El vigilante ejecuta lo que contiene la rama (`rebuild.sh`, el `Dockerfile`), igual que hace cualquier actualización manual:
usa solo un repositorio en el que confíes, y deja el checkout escribible únicamente por su propietario, porque quien pueda modificar `rebuild.sh` o
`watch.sh` allí ejecutará comandos con el usuario del vigilante.

Con `--user USER` (o con el crontab de ese usuario) se ejecuta con otro usuario. Ese usuario debe tener permiso para usar Docker
(lo que en la mayoría de sistemas equivale a root), debe ser propietario del checkout y debe poder escribir en `data/update` y
`data/extensions`, que normalmente pertenecen al usuario del contenedor.

<a id="windows-and-macos"></a>

### Windows y macOS

No hay un vigilante preparado para Docker Desktop. Actualiza con `rebuild.bat` / `rebuild.sh` a mano o desde una tarea
programada, véase [Actualizaciones automáticas](../getting-started/docker-server.md#automatic-updates).

<a id="troubleshooting"></a>

## Solución de problemas

| | |
|---|---|
| *The update watcher does not run on the server* | El vigilante no ha escrito ningún estado durante una hora, o nunca lo ha hecho. Comprueba `systemctl status marginalia-update.timer marginalia-update.path` y `journalctl -u marginalia-update.service`. Si la carpeta de datos no es `data` dentro del checkout, define `MARGINALIA_DATA_DIR`. |
| *Branch ... follows no remote branch* | El checkout está en una rama sin upstream (o en un commit suelto), así que no hay nada que descargar. En el servidor: `git switch <branch>` o `git branch --set-upstream-to=origin/<branch>`. |
| *The server could not reach the Git repository* | `git fetch` falló en la última consulta (sin red, o un repositorio privado sin credenciales para el propietario del checkout). El commit más reciente que se muestra puede ser antiguo. |
| The update was requested and nothing happens | Con cron empieza en menos de un minuto. Una petición que nadie atendió durante una hora se descarta, para que no arranque por sorpresa cuando se repare el vigilante. |
| *Last update: ..., failed* | Lee **Registro**. Un `git pull` fallido suele indicar que el checkout tiene cambios propios (archivos editados, commits locales): resuélvelo en el servidor. Si la compilación falla, la versión anterior sigue en ejecución. |

Los archivos del vigilante están en `update` dentro de la carpeta de datos: `status.properties` (lo que muestra la pestaña), `update.log`
(la última actualización) y, durante un momento, `request`.
