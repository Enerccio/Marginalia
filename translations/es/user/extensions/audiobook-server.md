---
label: Configurar el servidor de voz
order: 28
---

# Configurar el servidor de voz para el Audiobook

La extensión [Audiobook](audiobook.md) necesita un **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, licencia MIT) al que pueda acceder por HTTP. Esta página trata de la parte del servidor. Lo que hay en él se probó en una sola máquina (una AMD Radeon AI PRO R9700
con 32 GB, LiteLLM delante de todo, `llama-server` en modo router); tu hardware y tu proxy pueden necesitar otros valores.

<a id="which-setup-is-yours"></a>

## ¿Qué configuración es la tuya?

| Tu situación | Qué hacer |
|---|---|
| El servidor de voz funciona en **otra máquina**, en la CPU, o en una tarjeta con espacio suficiente **junto al modelo de lenguaje** | Ejecuta el servidor, pon su dirección en un backend y **deja vacías las peticiones antes y después de un lote**. No hace falta nada más. |
| El servidor de voz y el modelo de lenguaje **comparten una tarjeta** y no caben juntos | Necesitas las peticiones antes y después de un lote: **antes** libera la tarjeta y arranca el servidor de voz, **después** lo detiene. El resto de esta página. |
| El modelo de lenguaje es un modelo en la nube | Nada que liberar: ejecuta el servidor de voz donde quieras y deja las peticiones vacías. |

Así que: **sí, las peticiones previas y posteriores solo son necesarias cuando los dos modelos se turnan en una misma GPU.** Son las que hacen los turnos: el plugin las ejecuta una vez alrededor de un *lote* (todo lo que iniciaste con un clic), no alrededor de cada parte.

<a id="1-the-speech-server"></a>

## 1. El servidor de voz

Ejecuta Chatterbox-TTS-Server en un contenedor (o desde el código fuente) de forma que `GET /api/model-info` responda. Puntos importantes:

- **El modelo.** Turbo (`chatterbox-turbo`) es el predeterminado de la extensión y el que mejor suena en las pruebas; el modelo multilingüe solo hace falta para texto en un idioma distinto al del libro. El modelo se define en `config.yaml` del servidor
  (o `POST /save_settings` y `POST /restart_server`). El *Speech model* del backend debe ser el modelo que el servidor tenga cargado tras las peticiones previas a un lote.
- **El japonés necesita `pykakasi` en la imagen.** Sin él, el servidor registra *pykakasi not available - Japanese text processing skipped* y lee mal los kanji. Añade `RUN python3 -m pip install --no-cache-dir pykakasi` a su Dockerfile (antes de `COPY . .`). El mismo omitido silencioso existe para los paquetes de hebreo, ruso y chino.
- **Las voces** son archivos en la carpeta `reference_audio/` del servidor (móntala como volumen para que sobrevivan). La extensión sube ella misma los clips de tus actores (`POST /upload_reference`) antes de un lote, con un nombre formado por el hash del clip. El servidor no tiene llamada para borrar: los clips se acumulan ahí.
- El servidor **no tiene autenticación**. No publiques su puerto; pon un proxy delante si no está en una red privada.
- `restart: "no"` para un contenedor que otra cosa arranca bajo demanda. `restart: unless-stopped` lo arranca al iniciar el sistema y ocupa la tarjeta sin necesidad.

En el backend (*Settings → Extension Settings → Audiobook Settings*) pon su dirección, marca *The server is on this machine or on my network* si está en esta máquina o en tu red, y pulsa **Test connection**: lo que quieres ver es *the server answers, the model is loaded*.
La extensión habla con `/tts` (el conjunto completo de parámetros: voice, language, exaggeration, CFG weight, temperature, speed, seed) y `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`. Si hay un proxy entre tú y el servidor, debe dejar pasar todos estos.

<a id="one-gpu-for-the-model-and-the-speech"></a>

## Una GPU para el modelo y la voz

Una tarjeta que contiene el modelo de lenguaje no tiene espacio para el modelo de voz. El plan es el mismo para cualquier proxy:

```
before a batch:  1. unload the language model
                 2. start the speech server (and wait until it is ready; load its model if it starts empty)
the speech:      all the pieces of all the parts of the batch
after a batch:   3. stop the speech server (the language model loads again by itself with the next request)
```

**El orden no es negociable.** Arrancar el contenedor de voz mientras el modelo de lenguaje ocupa la tarjeta llenó la memoria del host en la prueba (30 GB de RAM, luego la swap, y después el OOM killer se llevó otros servicios): la descarga **debe** ir primero, y la primera petición que falle termina la lista, así que nada arranca con la tarjeta llena. Las peticiones *posteriores* a un lote se ejecutan todas, pase lo que pase con una de ellas.

Por qué *detener* el contenedor en lugar de descargar el modelo dentro del servidor: una descarga dentro del servidor dejó alrededor de 1 GB de la tarjeta ocupado tras muchos cambios de modelo; detener el contenedor libera todo.

<a id="the-requests"></a>

### Las peticiones

Cada petición de las listas es un método, una dirección, cabeceras, un cuerpo y una espera posterior (en segundos). No escribas la clave en las cabeceras (el formulario muestra las cabeceras tal como las escribes): ponla en **Key of the requests around a batch**, o déjalo vacío y se envía la *Api key* del backend. Se envía como `Authorization: Bearer` en cada petición de abajo que no tenga la suya propia. Con un LiteLLM delante de todo y una sola clave, las listas son:

| | Método y dirección | Cabeceras | Cuerpo | Qué hace |
|---|---|---|---|---|
| Antes 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Descarga el modelo de lenguaje; lo responde el propio hook, tarda de 1 a 2 s. |
| Antes 2 | `POST http://litellm:4000/gpu/tts/start` | | | Arranca el contenedor de voz y responde cuando el servidor está listo (unos 13 s). |
| Antes 3 (solo si el servidor arranca sin modelo) | `POST http://litellm:4000/gpu/tts/load` | | | Carga el modelo (de 8 a 14 s, 50 s la primera vez que se descarga). |
| Después 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Detiene el contenedor (menos de un segundo). |

El modelo de lenguaje vuelve solo: la siguiente petición normal hace que el router lo cargue (alrededor de un minuto para un modelo grande; con el [prompt cache hook](../prompt-cache.md) se restaura la caché de tu sesión), así que no hace falta ninguna petición posterior a un lote para ello.

Sin LiteLLM, las mismas peticiones van a lo que arranque y detenga tus contenedores; la única regla es el orden anterior.

<a id="what-answers-those-addresses"></a>

### Qué responde en esas direcciones

- **`X-Llama-Unload`** lo responde el hook `gpu_unload.py` de LiteLLM, descrito con su código en [Liberar la GPU para otro modelo](../prompt-cache.md#freeing-the-gpu-for-another-model) (funciona sin el hook de caché). Con una ruta `openai/*`, el nombre del modelo en el cuerpo debe empezar por `openai/`: otro nombre responde 400 *después* de que los modelos se hayan descargado.
- **`/gpu/tts/start`, `/stop`, `/load`** son **endpoints de pass-through** de LiteLLM hacia el pequeño controlador `tools/gpu-control` del repositorio, que arranca y detiene contenedores por nombre. Su README tiene el archivo compose con el **Docker socket proxy** (así el controlador solo puede arrancar y detener contenedores y nada más; el socket en sí es root en el host) y los ajustes fáciles de equivocar: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. En LiteLLM:

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

  (el contenedor se llama `chatterbox` y `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` le indica al controlador qué esperar; el token del controlador se queda en su `.env` y en el entorno de LiteLLM, Marginalia solo tiene la clave de LiteLLM).
- **Todos los contenedores y LiteLLM comparten una red Docker** (`docker network create ai`, una sola vez). Pon `networks: [default, ai]` y la red externa `ai` en cada archivo compose: un `docker network connect` hecho a mano se pierde cuando un contenedor se recrea.

<a id="checking-it"></a>

## Comprobarlo

1. `curl http://<speech server>/api/model-info` responde (`loaded` true tras un arranque).
2. *Test connection* en el backend dice que el modelo está cargado. Si las peticiones previas a un lote son lo único que arranca el servidor, pruébalo con las listas puestas: *the server answers, no model is loaded* es la respuesta de uno que se ha detenido y arrancado y que todavía tiene que cargar el modelo.
3. Convierte una parte corta en **Convert to audio**. La ventana muestra los fragmentos que se van generando; después el contenedor de voz debería estar detenido y `nvidia-smi` / `nvtop` / `rocm-smi` deben mostrar la tarjeta libre otra vez.
4. Si la primera llamada tras cargar un modelo tarda mucho, es el motor calentándose (una vez, 28 s en la primera prueba, y no se vuelve a ver).
<a id="troubleshooting"></a>

## Resolución de problemas

| Síntoma | Causa |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | El servidor arrancó vacío (un script de arranque que descarga el modelo una vez): añade la petición **Before 3**. |
| Las primeras piezas se generan, después el servidor dice *Model not loaded* y su registro muestra `Request received for /api/unload` | Algo distinto de Marginalia descarga el modelo (la extensión nunca llama a `/api/unload`): normalmente un script de arranque en el contenedor que espera a que el servidor responda y luego descarga el modelo "para que el arranque sea ligero". Su espera solo puede terminar después de la primera pieza, porque el servidor, de un solo hilo, estaba ocupado, y la descarga cae entonces en medio del lote (un traceback de timeout de `urllib` en el registro del contenedor es esa espera del script). El patrón que hay que buscar en el archivo de compose es un `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Sustitúyelo por `command: ["python3", "server.py"]`: un arranque por sí solo deja Turbo cargado, y la descarga solo servía para dejar libre la tarjeta tras un arranque, cosa que no necesita un contenedor que el controlador arranca y detiene. Para saber quién la envió, lee el registro de la extensión (*Admin → Extensions → Log*): enumera cada petición antes y después de un lote con su dirección y la hora en que fue respondida, y cuándo empiezan y terminan las piezas. Una descarga que llega mientras el registro indica que el lote está en curso, o desde una dirección que no aparece en la lista, no es de la extensión. |
| El lote se detiene a mitad (el registro de la extensión dice *Cannot reach*, o el registro del servidor simplemente termina) y un reinicio manual funciona | El servidor murió, normalmente matado por el sistema por acaparar toda la memoria (`dmesg \| grep -i oom` en el host: un proceso `python3` de muchos GB). La extensión entonces lo reinicia por sí misma (las peticiones posteriores al lote, y luego las anteriores) y vuelve a generar la pieza una sola vez; cuando el servidor no puede recuperarse, o desaparece de nuevo al instante, el resto del lote falla de inmediato y el registro explica por qué. Para evitar que un servidor con fugas llegue a ese punto, define **Restart the server after (pieces)** en el backend (por ejemplo 100), y un `mem_limit` en el contenedor para que el sistema mate solo a ese proceso. |
| La conversión falla de inmediato con la respuesta del proxy | Falló la primera petición anterior al lote (una clave incorrecta, o la ruta de descarga no encontrada); la lista se detiene ahí a propósito. |
| El host se queda sin memoria cuando empieza el lote | El modelo de lenguaje seguía en la tarjeta: falta la petición de descarga o no es la primera. |
| El japonés se lee como un sinsentido | `pykakasi` falta en la imagen del servidor; el backend debe ser también el multilingüe para esa pieza. |
| La subida de un clip falla detrás de un proxy | El proxy tiene que pasar un cuerpo multipart (`/upload_reference`) sin modificarlo. No probado a través del pass-through de LiteLLM; usa un archivo de voz del servidor (`predefined:Name.wav`) hasta que funcione. |
| El lote de otro usuario descarga el modelo bajo el tuyo | Los lotes en una misma dirección de servidor comparten una puerta interna de Marginalia, y un lote para otro modelo de voz espera a que terminen los demás. Un servidor de voz y, por ejemplo, un servidor de imágenes en una misma tarjeta necesitan el mismo nombre en **GPU (name)** en ambos backends para esperarse mutuamente (también el del Image Generator). Dos instalaciones de Marginalia en una misma tarjeta no comparten puerta. |
