---
label: Configurar el servidor de voz
order: 28
---

# Configurar el servidor de voz para el Audiobook

La extensión [Audiobook](audiobook.md) necesita un **Chatterbox-TTS-Server** ([devnen/Chatterbox-TTS-Server](https://github.com/devnen/Chatterbox-TTS-Server),
FastAPI, licencia MIT) al que pueda acceder por HTTP. Esta página trata del lado del servidor. Lo que aquí se describe se probó en una sola máquina (una AMD Radeon AI PRO R9700 con 32 GB, LiteLLM delante de todo, `llama-server` en modo router); tu hardware y tu proxy pueden necesitar otros valores.

<a id="which-setup-is-yours"></a>

## ¿Cuál es tu configuración?

| Tu situación | Qué hacer |
|---|---|
| El servidor de voz se ejecuta en **otra máquina**, en la CPU o en una tarjeta con espacio para él **junto al modelo de lenguaje** | Ejecuta el servidor, pon su dirección en un backend y **no elijas ningún bloqueo de GPU**. No hace falta nada más. |
| El servidor de voz y el modelo de lenguaje **comparten una tarjeta** y no caben juntos | Necesitas un [bloqueo de GPU](../administration/gpus.md): sus pasos de **adquirir** liberan la tarjeta y arrancan el servidor de voz; sus pasos de **liberar** lo detienen. El resto de esta página. |
| El modelo de lenguaje es un modelo en la nube | No hay nada que liberar: ejecuta el servidor de voz donde quieras y no elijas ningún bloqueo de GPU. |

Así que: **las peticiones previas y posteriores solo son necesarias cuando los dos modelos se turnan en una misma GPU.** Son las que hacen los turnos: el plugin las hace una vez alrededor de un *lote* (todo lo que iniciaste con un clic), no alrededor de cada parte.

<a id="1-the-speech-server"></a>

## 1. El servidor de voz

Ejecuta Chatterbox-TTS-Server en un contenedor (o desde el código fuente) de forma que `GET /api/model-info` responda. Cosas importantes:

- **El modelo.** Turbo (`chatterbox-turbo`) es el predeterminado de la extensión y el que mejor suena en las pruebas; el modelo multilingüe solo hace falta para texto en un idioma distinto al del libro. El modelo se configura en `config.yaml` del servidor
  (o en `POST /save_settings` y `POST /restart_server`). El *Speech model* del backend debe ser el modelo que el servidor tiene cargado tras los pasos de adquirir de su bloqueo de GPU.
- **El japonés necesita `pykakasi` en la imagen.** Sin él, el servidor registra *pykakasi not available - Japanese text processing skipped* y lee mal los kanji. Añade `RUN python3 -m pip install --no-cache-dir pykakasi` a su Dockerfile (antes de `COPY . .`). El mismo salto silencioso ocurre con los paquetes de hebreo, ruso y chino.
- **Las voces** son archivos en la carpeta `reference_audio/` del servidor (móntala, para que sobrevivan). La extensión sube ella misma los clips de tus actores (`POST /upload_reference`) antes de un lote, con un nombre formado a partir del hash del clip. El servidor no tiene llamada para borrar: los clips se acumulan allí.
- El servidor **no tiene autenticación**. No publiques su puerto; pon un proxy delante si no está en una red privada.
- `restart: "no"` para un contenedor que otra cosa arranca cuando hace falta. `restart: unless-stopped` lo arranca al iniciar y ocupa la tarjeta sin necesidad.

En el backend (*Settings → Extension Settings → Audiobook Settings*) pon su dirección, marca *The server is on this machine or on my network* si está en esta máquina o en tu red, y pulsa **Test connection**: lo que buscas es *the server answers, the model is loaded*.
La extensión habla con `/tts` (el conjunto completo de parámetros: voz, idioma, exageración, peso de CFG, temperatura, velocidad, semilla) y con `GET /api/model-info`, `/get_predefined_voices`, `/get_reference_files`, `/upload_reference`. Si hay un proxy entre tú y el servidor, debe dejar pasar todos estos.

<a id="higgs-audio-v3-in-comfyui"></a>

## Higgs Audio v3 en ComfyUI

Un backend con el modelo de voz **Higgs Audio v3 in ComfyUI** habla con un [ComfyUI](https://github.com/comfyanonymous/ComfyUI) con los nodos de la
[TTS Audio Suite](https://github.com/diodiogod/TTS-Audio-Suite), no con Chatterbox-TTS-Server. Su dirección es la de ComfyUI (por ejemplo
`http://gpu-server:8188`). Se configuró en la misma AMD R9700, en la imagen `yanwk/comfyui-boot:rocm`.

1. **Instala la suite** en la carpeta `custom_nodes` de ComfyUI (`git clone https://github.com/diodiogod/TTS-Audio-Suite`). Sus paquetes de Python van dentro del
   contenedor. Para conservarlos cuando el contenedor se vuelva a crear, dale a pip una carpeta en un volumen: añade `PIP_USER=true` al entorno del contenedor y monta
   `./storage/local:/root/.local`.
2. **Nunca dejes que pip reemplace el torch de la imagen** (una compilación CUDA reemplazaría la de ROCm). Escribe las versiones instaladas en un archivo e instala con él como
   restricción:
   ```sh
   PIP_USER=false python3 -m pip list --format=freeze | grep -iE '^(torch|torchaudio|torchvision|numpy|numba|transformers)==' > /root/.local/keep.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install -r requirements.txt
   PIP_CONSTRAINT=/root/.local/keep.txt python3 -m pip install librosa
   ```
   No ejecutes `install.py` de la suite completa: podría reinstalar torch. `librosa` no está en su `requirements.txt` y la suite no carga sin él.
3. **El modelo**: `hf download bosonai/higgs-audio-v3-tts-4b --local-dir <ComfyUI>/models/TTS/higgs_audio_v3/higgs-audio-v3-tts-4b` (unos 8 GB), o deja que la
   suite lo descargue la primera vez. Necesita unos 8 GB de la tarjeta gráfica.
4. **Test connection** en el backend muestra *the server answers* con el modelo del nodo cuando ComfyUI y el nodo están disponibles.

Lo que hace la extensión con él: sube los clips del reparto a la carpeta de entrada de ComfyUI (`POST /upload/image`, con un nombre formado a partir del clip), lista
las voces de la suite y los clips con `GET /object_info`, envía un workflow propio a `/prompt` por cada fragmento, espera en `/history` y descarga el audio
desde `/view`: con ffmpeg en el lado de Marginalia como flac, que recorta tras la última palabra, aplica un fundido de salida y guarda como mp3 o wav; sin ffmpeg, como el mp3 de ComfyUI, sin recortar (en ese caso el guardado en wav no funciona). **Después de cada fragmento llama a `POST /free` con
`{"free_memory": true}`**: en la R9700, el tercer fragmento seguido sobre el modelo cargado hizo caer la tarjeta gráfica (`HSA_STATUS_ERROR_EXCEPTION`, ComfyUI se cierra);
vaciar la memoria entre fragmentos mantiene el modelo cargado y lo evita, a costa de unos 3 segundos por fragmento. ComfyUI se arranca y se detiene alrededor de un lote con el mismo tipo de peticiones que el servidor de voz ([las peticiones](#the-requests)).

**Usa fp32 en una tarjeta AMD.** Con la precisión `auto` (bf16 en la R9700 con ROCm 7.2) muchos fragmentos salieron como galimatías, como efectos de sonido (una frase sobre golpear se convirtió en once golpes) o con una voz equivocada, y cambiar la semilla solo cambiaba la suerte. Los mismos fragmentos en fp32 salieron perfectos palabra por palabra. La **Precision** del backend es fp32 por defecto; necesita unos 16 GB de la tarjeta. El reconocimiento de voz Granite de la suite tiene el mismo problema en bf16 (galimatías desde el segundo reconocimiento en adelante), así que la extensión también lo ejecuta en float32.

**La limpieza de clips** ([Limpieza de clips](audiobook.md#cleanup-of-clips)) usa dos nodos más de la misma suite, `VocalRemovalNode` (modelos de separación)
y `VoiceFixerNode`, con `LoadAudio` y `SaveAudio` de ComfyUI; no hace falta instalar nada más. La suite descarga un modelo la primera vez que se usa
(los modelos MelBand Roformer ocupan unos 900 MB cada uno). En la R9700 funcionaron los modelos MelBand (voces, eliminación de ruido) y el voice fixer, unos 25 segundos
para un clip de 16 segundos. Los modelos UVR no: `UVR-DeNoise` no se pudo descargar (la dirección de la suite respondió 404) y `UVR-DeEcho-DeReverb` falló con
`module 'samplerate' has no attribute 'resample'` (el paquete de Python `samplerate` del contenedor no es el que espera la suite). Las limpiezas predeterminadas
usan solo los modelos que funcionaron.

El motor **Fish Audio S2 Pro** de la suite no funcionó en esa tarjeta: su audio salió como números no válidos (NaN). Los pesos de Higgs Audio v3 están bajo una
licencia de investigación y no comercial.
<a id="one-gpu-for-the-model-and-the-speech"></a>

## Una GPU para el modelo y la voz

Una tarjeta que aloja el modelo de lenguaje no tiene espacio para el modelo de voz. El administrador configura el intercambio una sola vez en [Admin → GPUs](../administration/gpus.md), con una GPU y un bloqueo para cada elemento que pueda cargarse en ella, y elige el bloqueo en la IA del modelo de lenguaje y en el backend de voz. El plan es el mismo para cualquier proxy:

```
lock "Speech (Chatterbox)"  acquire: 1. unload the language model
                                     2. start the speech server (and wait until it is ready; load its model if it starts empty)
                            release: 3. stop the speech server
lock "Language model"       acquire: nothing (the router loads the model by itself with the next request)
                            release: nothing (the speech lock unloads it first)
```

Un lote (o una muestra) del backend de voz ejecuta los pasos de adquisición del bloqueo de voz cuando la GPU tiene cargado el modelo de lenguaje, y después todas las piezas; la GPU queda cargada con el servidor de voz. La siguiente petición al modelo de lenguaje espera al lote, ejecuta el paso de liberación del bloqueo de voz (detener el servidor) y continúa. Varios lotes y muestras seguidos pagan el intercambio una sola vez.

**El orden no es negociable.** Arrancar el contenedor de voz mientras el modelo de lenguaje ocupa la tarjeta llenó la memoria del host en la prueba (30 GB de RAM, el swap, y después el OOM killer se llevó otros servicios): la descarga **debe ir primero**. El primer paso de adquisición que falle termina la lista, así que nada arranca sobre una tarjeta llena; y cuando falle un paso de liberación, el siguiente bloqueo no se carga en absoluto (la GPU puede seguir llena) y el trabajo falla con el error. Los pasos de liberación se ejecutan todos, pase lo que pase con uno de ellos.

**Higgs, el reconocimiento de voz y la limpieza de clips en un mismo ComfyUI** tienen cada uno un bloqueo propio, los tres con los mismos pasos (descargar el modelo de lenguaje, arrancar ComfyUI; liberación: detener ComfyUI). ComfyUI mantiene en la memoria del servidor un modelo que ya descargó (Higgs en fp32 ocupa unos 16 GB), y cargar el reconocimiento de voz junto a él dejó sin memoria al servidor en la prueba. Con tres bloqueos, cada cambio reinicia ComfyUI, así que solo uno de ellos está cargado en cada momento.

Por qué *detener* el contenedor en lugar de descargar el modelo dentro del servidor: una descarga dentro del servidor dejó alrededor de 1 GB de la tarjeta ocupado tras muchos cambios de modelo; detener el contenedor libera todo.

<a id="the-steps"></a>

### Los pasos

Cada paso es un método, una dirección, cabeceras, un cuerpo, un tiempo de espera y una espera posterior (en segundos); las cabeceras se guardan cifradas, así que la clave del proxy puede escribirse en ellas (`Authorization: Bearer ...`). Con un LiteLLM delante de todo, los pasos del bloqueo de voz son (*Before 1* a *3* son sus pasos de adquisición, *After 1* su paso de liberación):

| | Método y dirección | Cabeceras | Cuerpo | Qué hace |
|---|---|---|---|---|
| Before 1 | `POST http://litellm:4000/v1/chat/completions` | `X-Llama-Unload: all`, `Content-Type: application/json` | `{"model": "openai/unload", "messages": [{"role": "user", "content": "unload"}]}` | Descarga el modelo de lenguaje; responde el propio hook, tarda de 1 a 2 s. |
| Before 2 | `POST http://litellm:4000/gpu/tts/start` | | | Arranca el contenedor de voz y responde cuando el servidor está listo (unos 13 s). |
| Before 3 (solo si el servidor arranca sin modelo) | `POST http://litellm:4000/gpu/tts/load` | | | Carga el modelo (de 8 a 14 s, 50 s la primera vez que se descarga). |
| After 1 | `POST http://litellm:4000/gpu/tts/stop` | | | Detiene el contenedor (menos de un segundo). |

Para un backend **Higgs**, *Before 2* es `POST http://litellm:4000/gpu/comfy/start` (responde cuando ComfyUI está levantado, unos 15 s; el modelo se carga con la primera pieza), no hay *Before 3*, y *After 1* es `POST http://litellm:4000/gpu/comfy/stop`. Los dos pass-through están en el README de `tools/gpu-control`.

El modelo de lenguaje vuelve solo: la siguiente petición normal hace que el router lo cargue (alrededor de un minuto para un modelo grande; con el [hook de caché de prompts](../prompt-cache.md) se restaura la caché de tu sesión), así que no hace falta ninguna petición tras el lote para ello.

Sin LiteLLM, las mismas peticiones van a lo que arranque y detenga tus contenedores; la única regla es el orden anterior.

<a id="what-answers-those-addresses"></a>

### Qué responde a esas direcciones

- **`X-Llama-Unload`** lo responde el hook `gpu_unload.py` de LiteLLM, descrito con su código en
  [Liberar la GPU para otro modelo](../prompt-cache.md#freeing-the-gpu-for-another-model) (funciona sin el hook de caché). Con una ruta `openai/*`, el nombre del modelo en el cuerpo debe empezar por `openai/`: otro nombre responde 400 *después* de que los modelos se hayan descargado.
- **`/gpu/tts/start`, `/stop`, `/load`** son **endpoints pass-through** de LiteLLM hacia el pequeño controlador `tools/gpu-control` del repositorio, que arranca y detiene contenedores por nombre. Su README contiene el archivo compose con el **proxy del socket de Docker** (así el controlador puede arrancar y detener contenedores y nada más; el socket en sí es root en el host) y los ajustes fáciles de equivocar: `POST=1`, `ALLOW_START=1`, `ALLOW_STOP=1`, **`CONTAINERS=0`**. En LiteLLM:

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

  (el contenedor se llama `chatterbox` y `GPU_CONTROL_CONTAINERS=chatterbox=http://chatterbox:8004/api/model-info` indica al controlador qué esperar; el token del controlador se queda en su `.env` y en el entorno de LiteLLM, Marginalia solo tiene la clave de LiteLLM).
- **Todos los contenedores y LiteLLM comparten una red de Docker** (`docker network create ai`, una vez). Pon `networks: [default, ai]` y la red externa `ai` en cada archivo compose: un `docker network connect` hecho a mano se pierde cuando se recrea un contenedor.

<a id="checking-it"></a>

## Comprobarlo

1. `curl http://<speech server>/api/model-info` responde (`loaded` es true tras un arranque).
2. *Test connection* en el backend indica que el modelo está cargado. Si el bloqueo de GPU es lo único que arranca el servidor, pruébalo con el bloqueo elegido: *the server answers, no model is loaded* es la respuesta de un servidor detenido y arrancado que todavía tiene que cargar el modelo.
3. Convierte una parte corta en **Convert to audio**. La ventana muestra las piezas que se generan; después, el contenedor de voz debe estar detenido y `nvidia-smi` / `nvtop` / `rocm-smi` deben mostrar la tarjeta libre de nuevo.
4. Si la primera llamada tras cargar un modelo tarda mucho, es el motor calentándose (una sola vez, 28 s en la primera ejecución, no se vuelve a ver).
<a id="troubleshooting"></a>

## Solución de problemas

| Síntoma | Causa |
|---|---|
| *No model is loaded* / 503 "TTS engine model is not currently loaded" | El servidor arrancó vacío (un script de inicio que descarga el modelo una vez): añade el paso **Before 3** a los pasos de adquisición del lock. |
| Las primeras piezas se generan, luego el servidor dice *Model not loaded* y su log muestra `Request received for /api/unload` | Algo distinto de Marginalia descarga el modelo (la extensión nunca llama a `/api/unload`): normalmente un script de arranque en el contenedor que espera a que el servidor responda y luego descarga el modelo «para que el arranque sea más ligero». Su espera solo puede terminar después de la primera pieza, porque el servidor de un solo hilo estaba ocupado, y la descarga cae entonces en medio del lote (un traceback de timeout de `urllib` en el log del contenedor es esa espera). El patrón que hay que buscar en el archivo compose es un `command: sh -c "python3 server.py & ... until python3 -c \"...urlopen('.../api/model-info'...)\"; do sleep 3; done; python3 -c \"...urlopen(Request('.../api/unload', method='POST')...)\"; wait"`. Sustitúyelo por `command: ["python3", "server.py"]`: un arranque basta para tener Turbo cargado, y la descarga solo servía para dejar libre la tarjeta tras un arranque, algo que un contenedor que inicia y detiene el controlador no necesita. Para saber quién la envió, lee el log de la extensión (*Admin → Extensions → Log*): lista cada petición antes y después de un lote con su dirección y la hora en que se respondió, y cuándo empiezan y terminan las piezas; los pasos del lock de GPU están en el log de la aplicación. Una descarga que llega mientras el log indica que el lote está en marcha, o desde una dirección que no aparece en la lista, no es de la extensión. |
| El lote se detiene a mitad (el log de la extensión dice *Cannot reach*, o el log del servidor simplemente termina) y reiniciar a mano funciona | El servidor murió, normalmente porque el sistema lo mató por acaparar toda la memoria (`dmesg \| grep -i oom` en el host: un proceso `python3` de muchos GB). La extensión entonces lo reinicia por sí misma (las peticiones posteriores al lote, los pasos de liberación y adquisición del lock de GPU, y luego las peticiones anteriores) y vuelve a generar la pieza una sola vez; si el servidor no se puede recuperar, o vuelve a caerse de inmediato, el resto del lote falla al instante y el log indica por qué. Para evitar que un servidor con fugas llegue a ese punto, configura **Restart the server after (pieces)** en el backend (por ejemplo, 100), y un `mem_limit` en el contenedor para que el sistema mate solo a ese proceso. |
| La conversión falla al instante con la respuesta del proxy | Falló el primer paso de adquisición del lock de GPU (una clave incorrecta, o la ruta de descarga no encontrada); la lista se detiene ahí a propósito. |
| El host se queda sin memoria al empezar el lote | El modelo de lenguaje seguía en la tarjeta: falta el paso de descarga en el lock de voz o no es el primero, o la IA del modelo de lenguaje no tiene un lock de la misma GPU. |
| El japonés se lee como disparates | `pykakasi` falta en la imagen del servidor; el backend también debe ser el multilingüe para esa pieza. |
| La subida de un clip falla detrás de un proxy | El proxy tiene que pasar un cuerpo multipart (`/upload_reference`) sin cambios. No probado con el pass-through de LiteLLM; usa un archivo de voz del servidor (`predefined:Name.wav`) hasta que funcione. |
| El lote de otro usuario descarga el modelo bajo el tuyo | Cada backend y cada IA en una misma tarjeta debe indicar un lock de la misma [GPU](../administration/gpus.md): solo así se turnan. Un backend sin lock no espera a nadie. Dos instalaciones de Marginalia en una misma tarjeta no comparten GPU. |
