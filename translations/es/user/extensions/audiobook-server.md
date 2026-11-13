---
label: Configuración del servidor de voz
order: 28
---

# Configuración del servidor de voz para el audiolibro

La extensión [Audiolibro](audiobook.md) necesita un **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, licencia MIT) al que pueda llegar por HTTP. Esta página trata del lado del servidor. Lo que aparece aquí se probó en una máquina (una AMD Radeon AI PRO R9700
de 32 GB, LiteLLM delante de todo y `llama-server` en modo router); tu hardware y tu proxy pueden necesitar otros valores.

<a id="which-setup-is-yours"></a>

## ¿Cuál es tu configuración?

| Tu situación | Qué hacer |
|---|---|
| El servidor de voz funciona en **otra máquina**, en la CPU, o en una tarjeta con espacio suficiente **junto al modelo de lenguaje** | Ejecuta el servidor, pon su dirección en un backend, **deja vacías las peticiones antes y después de un lote**. No hace falta nada más. |
| El servidor de voz y el modelo de lenguaje **comparten una tarjeta** y no caben juntos | Necesitas las peticiones antes y después de un lote: **antes** libera la tarjeta y arranca el servidor de voz, **después** lo detiene. El resto de esta página. |
| El modelo de lenguaje es un modelo en la nube | Nada que liberar: ejecuta el servidor de voz donde quieras y deja vacías las peticiones. |

Así que: **las peticiones previas y posteriores solo hacen falta cuando los dos modelos se turnan en una misma GPU.** Son las que hacen los turnos: el complemento las hace una vez alrededor de un *lote* (todo lo que iniciaste con un solo clic), no alrededor de cada parte.

<a id="1-the-speech-server"></a>

## 1. El servidor de voz

Ejecuta Chatterbox-TTS-Server en un contenedor (o desde el código fuente) de forma que `GET /api/model-info` responda. Puntos importantes:

- **El modelo.** Turbo (`chatterbox-turbo`) es el predeterminado de la extensión y el que mejor suena en las pruebas; el modelo multilingüe solo hace falta para texto en un idioma distinto al del libro. El modelo se configura en el `config.yaml` del servidor
  (o mediante `POST /save_settings` y `POST /restart_server`). El *Modelo de voz* del backend debe ser el modelo que tiene cargado el servidor después de las peticiones previas a un lote.
- **El japonés necesita `pykakasi` en la imagen.** Sin él, el servidor registra *pykakasi not available - Japanese text processing skipped* y lee mal los kanji. Añade `RUN python3 -m pip install --no-cache-dir pykakasi` a su Dockerfile (antes de `COPY . .`). El mismo omitido silencioso existe para los paquetes de hebreo, ruso y chino.
- **Las voces** son archivos de la carpeta `reference_audio/` del servidor (móntala, para que sobrevivan). La extensión sube por sí misma los clips de tus personajes (`POST /upload_reference`) antes de un lote, con un nombre formado a partir del hash del clip. El servidor no tiene una llamada para borrar: los clips se acumulan allí.
- El servidor **no tiene autenticación**. No publiques su puerto; pon un proxy delante si no está en una red privada.
- `restart: "no"` para un contenedor que algo más arranca bajo demanda. `restart: unless-stopped` lo arranca al iniciar el equipo y ocupa la tarjeta sin ningún beneficio.

En el backend (*Ajustes → Ajustes de extensiones → Ajustes de Audiolibro*) pon su dirección, marca *El servidor está en esta máquina o en mi red* si es el caso, y pulsa **Probar conexión**: lo que quieres ver es *el servidor responde, el modelo está cargado*.
La extensión habla con `/tts` (el conjunto completo de parámetros: voz, idioma, exageración, peso de CFG, temperatura, velocidad, semilla) y con `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`. Si hay un proxy entre tú y el servidor, debe dejar pasar todas estas rutas.

<a id="one-gpu-for-the-model-and-the-speech"></a>

## Una GPU para el modelo y la voz

Una tarjeta que aloja el modelo de lenguaje no tiene espacio para el modelo de voz. El plan es el mismo para cualquier proxy:

```
antes de un lote:  1. descargar el modelo de lenguaje
                   2. arrancar el servidor de voz (y esperar a que esté listo; cargar su modelo si arranca vacío)
la voz:            todas las piezas de todas las partes del lote
después de un lote: 3. detener el servidor de voz (el modelo de lenguaje se vuelve a cargar solo con la siguiente petición)
```

**El orden no es negociable.** Arrancar el contenedor de voz mientras el modelo de lenguaje ocupa la tarjeta llenó la memoria del equipo en la prueba
(30 GB de RAM, luego el swap, y después el OOM killer se llevó otros servicios): la descarga **debe ir primero**, y la primera petición que falle detiene
la lista, así que nada arranca con la tarjeta llena. Las peticiones *posteriores* a un lote se ejecutan todas, pase lo que pase con una de ellas.

Por qué *detener* el contenedor en lugar de descargar el modelo dentro del servidor: una descarga dentro del servidor dejó alrededor de 1 GB de la tarjeta ocupados tras muchos cambios de modelo; detener el contenedor lo libera todo.

<a id="the-requests"></a>

### Las peticiones

Cada petición de las listas es un método, una dirección, cabeceras, un cuerpo y una espera posterior (en segundos). No escribas la clave en las cabeceras (el formulario muestra las cabeceras tal como se escriben): ponla en **Clave de las peticiones alrededor de un lote**, o déjala vacía y se envía la *Clave de API* del backend. Se envía como `Authorization: Bearer` con cada petición de abajo que no tenga la suya propia. Con un LiteLLM
delante de todo y una sola clave, las listas son:

| | Método y dirección | Cabeceras | Cuerpo | Hace |
|---|---|---|---|---|
| Antes 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Descarga el modelo de lenguaje; la responde el propio hook, tarda de 1 a 2 s. |
| Antes 2 | `POST http://litellm:4000/gpu/tts/start` | | | Arranca el contenedor de voz y responde cuando el servidor está listo (unos 13 s). |
| Antes 3 (solo si el servidor arranca sin modelo) | `POST http://litellm:4000/gpu/tts/load` | | | Carga el modelo (de 8 a 14 s, 50 s la primera vez, cuando se descarga). |
| Después 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Detiene el contenedor (menos de un segundo). |

El modelo de lenguaje vuelve solo: la siguiente petición normal hace que el router lo cargue (alrededor de un minuto para un modelo grande; con el
[hook de caché de prompts](../prompt-cache.md) se restaura la caché de tu sesión), así que no hace falta ninguna petición después del lote para él.

Sin LiteLLM, las mismas peticiones van a lo que arranque y detenga tus contenedores; la única regla es el orden de arriba.

<a id="what-answers-those-addresses"></a>

### Qué responde en esas direcciones

- **`X-Llama-Unload`** lo responde el hook `gpu_unload.py` de LiteLLM, descrito con su código en
  [Liberar la GPU para otro modelo](../prompt-cache.md#freeing-the-gpu-for-another-model) (funciona sin el hook de caché). Con una ruta `openai/*` el nombre del modelo en el cuerpo debe empezar por `openai/`: otro nombre responde 400 *después* de que los modelos se hayan descargado.
- **`/gpu/tts/start`, `/stop`, `/load`** son **endpoints pass-through** de LiteLLM hacia el pequeño controlador `tools/gpu-control` del repositorio, que arranca y detiene contenedores por nombre. Su
  README tiene el archivo compose con el **proxy del socket de Docker** (para que el controlador pueda arrancar y detener contenedores y nada más; el socket en sí es root en el host) y los ajustes que se confunden fácilmente: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. En LiteLLM:

  ```yaml
  general_settings:
    pass_through_endpoints:
      - path: "/gpu/tts/start"
        target: "http://gpu-control:8000/chatterbox/start"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/stop"
        target: "http://gpu-control:8000/chatterbox/stop"
        headers: {X-Control-Token: os.environ/GPU_CONTROL_TOKEN}
      - path: "/gpu/tts/load"
        target: "http://chatterbox:8004/restart_server"
  ```

  (el contenedor se llama `chatterbox` y `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` le indica al controlador qué esperar; el token del controlador queda en su `.env` y en el entorno de LiteLLM, Marginalia solo tiene la clave de LiteLLM).
- **Todos los contenedores y LiteLLM comparten una red de Docker** (`docker network create ai`, una sola vez). Pon `networks: [default, ai]` y la red externa `ai` en cada archivo compose: un `docker network connect` hecho a mano se pierde cuando un contenedor se vuelve a crear.

<a id="checking-it"></a>

## Comprobarlo

1. `curl http://<servidor de voz>/api/model-info` responde (`loaded` en true tras un arranque).
2. *Probar conexión* en el backend dice que el modelo está cargado. Si las peticiones antes de un lote son lo único que arranca el servidor, pruébalo con las listas puestas: *el servidor responde, no hay modelo cargado* es la respuesta de uno que se detuvo y se volvió a arrancar y todavía tiene que cargar su modelo.
3. Convierte una parte corta en **Convertir a audio**. La ventana muestra las piezas que se van generando; después el contenedor de voz debería estar detenido y `nvidia-smi` / `nvtop` / `rocm-smi` deberían mostrar la tarjeta libre de nuevo.
4. Si la primera llamada tras cargar un modelo tarda mucho, es el motor calentándose (una vez, 28 s en la primera ejecución, y no se ha vuelto a ver).
<a id="troubleshooting"></a>

## Solución de problemas

| Síntoma | Causa |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | El servidor arrancó vacío (un script de arranque que descarga el modelo una sola vez): añade la solicitud **Before 3**. |
| Las primeras piezas se generan; después el servidor responde *Model not loaded* y su registro muestra `Request received for /api/unload` | Algo distinto de Marginalia descarga el modelo (la extensión nunca llama a `/api/unload`). Normalmente es un script de arranque en el contenedor que espera a que el servidor responda y luego descarga el modelo «para que el arranque sea más ligero». Su espera solo puede terminar después de la primera pieza, porque el servidor, de un solo hilo, estaba ocupado, y entonces la descarga cae en medio del lote (un traceback de timeout de `urllib` en el registro del contenedor es esa espera). El patrón que hay que buscar en el archivo compose es un `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Sustitúyelo por `command: ["python3", "server.py"]`: un arranque por sí solo deja Turbo cargado, y la descarga solo servía para dejar libre la tarjeta tras un arranque, algo que un contenedor que el controlador inicia y detiene no necesita. Para saber quién la envió, lee el registro de la extensión (*Admin → Extensions → Log*): lista cada solicitud antes y después de un lote, con su dirección y la hora en que se respondió, y también cuándo empiezan y terminan las piezas. Una descarga que llega mientras el registro indica que el lote está en marcha, o desde una dirección que no aparece en la lista, no es de la extensión. |
| El lote se detiene a mitad (el registro de la extensión dice *Cannot reach*, o el registro del servidor simplemente termina) y un reinicio manual funciona | El servidor murió, normalmente porque el sistema lo eliminó por consumir toda la memoria (`dmesg \| grep -i oom` en el host: un proceso `python3` de muchos GB). La extensión entonces lo reinicia por sí misma (primero las solicitudes posteriores al lote, luego las anteriores) y rehace la pieza una sola vez. Si el servidor no puede recuperarse, o vuelve a caerse de inmediato, el resto del lote falla al instante y el registro indica el motivo. Para evitar que un servidor con fugas llegue a ese punto, define **Restart the server after (pieces)** en el backend (por ejemplo, 100) y un `mem_limit` en el contenedor, para que el sistema elimine solo ese proceso. |
| La conversión falla enseguida con la respuesta del proxy | La primera solicitud anterior al lote falló (una clave incorrecta o una ruta de descarga no encontrada); la lista se detiene ahí a propósito. |
| El host se queda sin memoria al empezar el lote | El modelo de lenguaje seguía en la tarjeta: falta la solicitud de descarga o no es la primera. |
| El japonés se lee como texto sin sentido | Falta `pykakasi` en la imagen del servidor; el backend también debe ser el multilingüe para esa pieza. |
| La subida de un clip falla detrás de un proxy | El proxy debe pasar sin cambios un cuerpo multipart (`/upload_reference`). No está probado con el paso directo de LiteLLM; usa un archivo de voz del servidor (`predefined:Name.wav`) hasta que funcione. |
| El lote de otro usuario descarga el modelo bajo el tuyo | Los lotes en una misma dirección de servidor comparten una compuerta dentro de Marginalia, y un lote para otro modelo de voz espera a que terminen los demás. Un servidor de voz y, por ejemplo, un servidor de imágenes en una misma tarjeta necesitan el mismo nombre en **GPU (name)** en ambos backends para esperarse mutuamente (también el del Image Generator). Dos instalaciones de Marginalia en una misma tarjeta no comparten compuerta. |
