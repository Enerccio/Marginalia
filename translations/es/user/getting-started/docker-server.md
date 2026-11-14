---
label: Servidor (Docker)
order: 90
---

# Servidor (Docker)

Ejecuta Marginalia como servidor cuando lo usen varias personas o cuando quieras abrirlo desde otros dispositivos. Cada usuario tiene sus propios libros, lorebooks, proveedores de inferencia y protocolos.

<a id="requirements"></a>

## Requisitos

- [Docker](https://docs.docker.com/get-docker/) con Docker Compose
- Unos 1 GB de memoria para Marginalia
- Para acceso por internet: un dominio y un proxy inverso con HTTPS (ver [más abajo](#https-and-reverse-proxy))

<a id="install"></a>

## Instalación

```sh
git clone https://github.com/Enerccio/Marginalia.git
cd Marginalia
docker compose up -d
```

El primer `docker compose up` compila la imagen a partir del código fuente, lo que tarda unos minutos. Después Marginalia escucha en el puerto **8080**. Abre `http://<server>:8080` y crea la cuenta de administrador (ver [Primer arranque](first-start.md)).

<a id="prebuilt-image"></a>

### Imagen precompilada

En lugar de compilar desde el código fuente, puedes usar la imagen publicada con cada versión (`linux/amd64` y `linux/arm64`). En `docker-compose.yml`, sustituye el bloque `build:` por `image:`:

```yaml
services:
  marginalia:
    image: ghcr.io/enerccio/marginalia:latest
```

`latest` es la versión más reciente, `edge` la versión de desarrollo, y `1.1.0` (o `1.1`) fija una versión concreta. El resto del archivo no cambia.

<a id="configuration"></a>

## Configuración

Todo se configura en `docker-compose.yml`:

```yaml
services:
  marginalia:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: marginalia-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    volumes:
      - ./data:/var/marginalia/.marginalia
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/marginalia/.marginalia/heapdump.hprof
```

| Ajuste | Qué hace |
|---|---|
| `ports` | `"8080:8080"` publica Marginalia en el puerto 8080 del host. Usa `"127.0.0.1:8080:8080"` si solo debe acceder un proxy inverso en el mismo host. |
| `volumes` | `./data` guarda la base de datos, las copias de seguridad y las extensiones. Consérvalo e inclúyelo en las copias de seguridad del servidor. Se crea en el primer arranque si no existe; al arrancar, el contenedor lo deja en propiedad del usuario con el que Marginalia se ejecuta dentro del contenedor (`jetty`), de modo que los archivos que contiene pertenecen al ID de ese usuario en el host. |
| `-Xmx1g` | Memoria máxima. Súbela (por ejemplo `-Xmx2g`) para muchos usuarios o libros muy grandes. |

Tras cambiar el archivo, aplícalo con `docker compose up -d`.

<a id="environment-variables"></a>

### Variables de entorno

Los ajustes propios de Marginalia son variables de entorno del servicio, junto a `JAVA_TOOL_OPTIONS`:

```yaml
    environment:
      - JAVA_TOOL_OPTIONS=-Duser.home=/var/marginalia -Xms256m -Xmx1g
      - MARGINALIA_DEFAULT_LANGUAGE=ja
      - MARGINALIA_PERSISTENT_LOGIN_TTL=604800
      - MARGINALIA_TRUSTED_PROXIES=172.16.0.0/12
```

| Variable | Por defecto | Qué hace |
|---|---|---|
| `MARGINALIA_DATA` | `~/.marginalia` (`/var/marginalia/.marginalia` en la imagen) | Carpeta de la base de datos, copias de seguridad, extensiones y la clave que cifra las claves de API. Cámbiala solo con un volumen equivalente (`./data:/your/folder`); el contenedor fija el propietario de esta carpeta al arrancar. |
| `MARGINALIA_DEFAULT_LANGUAGE` | `en` | Idioma de la página de inicio de sesión y de los usuarios que no han elegido uno (`en` o `ja`; el idioma de una extensión no puede usarse aquí). |
| `MARGINALIA_ALLOW_PERSISTENT_LOGIN` | `true` | `false` oculta *Mantener la sesión iniciada*. |
| `MARGINALIA_PERSISTENT_LOGIN_TTL` | `2592000` | Cuánto dura un inicio de sesión guardado, en segundos (30 días). |
| `MARGINALIA_TRUSTED_PROXIES` | vacío | Proxies inversos cuyo encabezado `X-Forwarded-For` se considera fiable, ver [más abajo](#https-and-reverse-proxy). Sustituye a `-DtrustedProxies`. |

Los mismos ajustes pueden darse como opciones de Java, `-Dmarginalia.default-language=ja`, que tienen prioridad sobre las variables. Un valor no válido (texto en lugar de un número de segundos, un idioma desconocido) impide que Marginalia arranque; el motivo está en el registro.

<a id="https-and-reverse-proxy"></a>

## HTTPS y proxy inverso

!!!danger No uses HTTP sin cifrar por internet
Las contraseñas y las cookies de inicio de sesión se envían con cada petición. Por HTTP sin cifrar, cualquiera en el camino puede leerlas.
Pon Marginalia detrás de un proxy inverso con HTTPS.
!!!

La opción *Mantener la sesión iniciada* de la pantalla de inicio de sesión también necesita HTTPS: sus cookies se marcan como seguras, así que los navegadores solo las conservan en conexiones HTTPS.

Marginalia actualiza la página mediante una conexión WebSocket, así que el proxy debe reenviar las actualizaciones a WebSocket. Ejemplos para `marginalia.example.com`:

+++ Caddy
Caddy obtiene el certificado y reenvía los WebSockets automáticamente.

```
marginalia.example.com {
    reverse_proxy localhost:8080
}
```
+++ nginx
```nginx
server {
    listen 443 ssl;
    server_name marginalia.example.com;

    ssl_certificate     /etc/letsencrypt/live/marginalia.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/marginalia.example.com/privkey.pem;

    client_max_body_size 100m;   # uploads of backups and extensions

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 1h;   # long generations keep the connection open
    }
}
```
+++

Los inicios de sesión fallidos se limitan por usuario y por dirección del cliente (tras 30 fallos desde una misma dirección, los inicios de sesión desde ella se rechazan durante un tiempo creciente, de hasta 15 minutos). Marginalia solo se fía del encabezado `X-Forwarded-For` de un proxy que tú indiques: sin él, todos los usuarios parecen venir de la dirección del proxy y 30 fallos de cualquiera bloquean a todos durante un tiempo. Mantén el encabezado en la configuración del proxy (como arriba) y define `MARGINALIA_TRUSTED_PROXIES=<address or range of the proxy>` en el `environment` de `docker-compose.yml`. Un proxy en el host de Docker llega al contenedor desde la red de Docker, normalmente `172.16.0.0/12`; varias entradas se separan con comas. La limitación se guarda en memoria: reiniciar Marginalia la borra.

<a id="models-on-the-same-machine"></a>

## Modelos en la misma máquina

Dentro del contenedor, `localhost` es el propio contenedor, no el servidor. Para usar un servidor de modelos que se ejecuta en el host de Docker (llama.cpp, Ollama, LM Studio...), usa la dirección `http://host.docker.internal:<port>/v1` en el [proveedor de inferencia](../inference-providers.md). En Linux, añade esto al servicio en `docker-compose.yml`:

```yaml
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

El servidor de modelos también debe escuchar en una dirección a la que el contenedor pueda acceder, no solo en `127.0.0.1`.

<a id="updating"></a>

## Actualización

```sh
git pull
docker compose up -d --build
```

Con la [imagen precompilada](#prebuilt-image), usa `docker compose pull` y `docker compose up -d` en su lugar.

La base de datos se actualiza automáticamente al arrancar la nueva versión. Marginalia guarda una [copia de la base de datos](../administration/database-backups.md#copy-before-an-upgrade) antes de cambiar su estructura, pero haz también primero una [copia de seguridad de la base de datos](../administration/database-backups.md) propia.

<a id="automatic-updates"></a>

### Actualizaciones automáticas

`rebuild.sh` (Linux, macOS) y `rebuild.bat` (Windows) en la raíz del repositorio hacen la actualización por ti; ejecútalos a mano o desde una tarea cron o programada. Descargan el remoto de la rama en la que esté el repositorio (sea cual sea) y no hacen nada si no hay novedades (`--force` recompila y reinicia igualmente, por ejemplo tras cambiar `docker-compose.yml`). En caso contrario, `git pull`, compilan los [plugins incluidos](#plugins) y la imagen **mientras Marginalia sigue en marcha**, luego detienen el contenedor, copian los JAR de los plugins en `data/extensions` (sustituyendo los instalados) e inician la nueva versión. Si algún paso falla, el script se detiene con un mensaje; si el fallo es en la descarga o en una compilación, la versión anterior sigue en marcha y la siguiente ejecución vuelve a compilar.

`--stop-first` detiene el contenedor antes de compilar, para un servidor que no tiene memoria suficiente para la aplicación y la compilación a la vez; en ese caso Marginalia está caído durante toda la compilación.

En Linux no hace falta ejecutar los scripts a mano: con el vigilante de actualizaciones instalado, un administrador actualiza el servidor con un botón; ver [Actualizaciones](../administration/updates.md).

## Plugins

La imagen de Docker no contiene las [extensiones](../extensions/index.md), que deben compilarse a partir del mismo código fuente que la aplicación. Para ello no necesitas Java ni Maven: `marginalia/plugins/docker-compose.yml` compila todas en un contenedor, que primero compila la aplicación y después cada plugin, y copia los JAR en `marginalia/plugins/build`:

```sh
docker compose -f marginalia/plugins/docker-compose.yml build
docker compose -f marginalia/plugins/docker-compose.yml run --rm plugins
```

La primera compilación tarda varios minutos (descarga las dependencias de la aplicación). Después copia los JAR en `data/extensions` y reinicia Marginalia, o cárgalos en *Admin → Extensions* (ver [Extensiones](../administration/extensions.md)). `rebuild.sh` / `rebuild.bat` hacen la copia por ti, así que en un servidor que los use, los plugins se actualizan siempre junto con la aplicación.

<a id="health"></a>

## Estado

La imagen incluye una comprobación de estado de Docker: `docker ps` muestra `(health: starting)` mientras Marginalia arranca (hasta dos minutos), y después `(healthy)`. Pasa a `unhealthy` cuando `http://localhost:8080/health` deja de responder con `200`. Docker no reinicia por sí mismo un contenedor no saludable; usa el estado para tu monitorización o una herramienta de autorreparación.

`/health` no requiere inicio de sesión, así que también puede usarlo una herramienta de monitorización o un balanceador de carga (a través del proxy: `https://marginalia.example.com/health`). Responde `200` y `{"status":"UP"}` cuando Marginalia está en marcha y su base de datos responde, y `503` con `{"status":"DOWN",...}` mientras arranca o cuando algo va mal. No muestra nada más (ni versión, ni detalles); el motivo está en el registro.

<a id="logs"></a>

## Registros

```sh
docker compose logs -f marginalia
```
