---
label: GPUs
order: 84
---

# GPUs

Una tarjeta gráfica suele cargar un solo modelo a la vez: el modelo de lenguaje, o el de imagen, o el de voz. La pestaña **GPUs** de la página Admin permite que los modelos y los servidores de una misma tarjeta se turnen para usarla, de modo que una imagen o un audiolibro no empiecen mientras el modelo de lenguaje ocupa la tarjeta, y que el modelo de lenguaje vuelva cuando se necesite.

Lo configura una sola vez el administrador; después, cada usuario elige un lock para su IA o sus backends.

<a id="what-it-is-made-of"></a>

## De qué se compone

| | |
|---|---|
| **GPU** | Una tarjeta (o cualquier máquina que cargue un solo modelo a la vez): su nombre y **Also used outside Marginalia** (véase [más abajo](#a-gpu-that-others-use-too)). |
| **GPU lock** | Una forma de usar una GPU: qué está cargado en ella. Por ejemplo, *Language model*, *ComfyUI* y *Speech* en la misma GPU. Un lock tiene dos listas ordenadas de pasos: los **acquire steps** lo cargan (iniciar un contenedor, cargar un modelo, descargar lo que deba salir antes) y los **release steps** liberan la tarjeta de él (detener el contenedor, descargar el modelo). Una tercera lista, los **steps before every job**, se ejecuta antes de cada trabajo del lock, tanto si la GPU lo tiene cargado como si no (antes de los acquire steps si no lo tiene): sirve para comprobar lo que debe asegurarse siempre, porque otra cosa puede haber usado la tarjeta entre tanto. **Jobs at once** limita cuántos trabajos del lock se ejecutan a la vez (un servidor de imágenes que genera una imagen cada vez: 1; 0 para no limitar). |
| **GPU step** | Una petición HTTP: una dirección, un método, cabeceras (`Name: value`, una por línea, guardadas cifradas, de modo que aquí se puede escribir una clave bearer), un cuerpo, un tiempo de espera (vacío: 60 segundos) y una pausa tras la respuesta. Un paso puede usarlo varios locks; por ejemplo, un paso *unload the language model* que el lock de imagen y el de voz ejecutan primero. **Test** envía la petición al momento. |

![La ventana de un lock](../../images/gpu-lock-dialog.png)

Elija el lock en:

- los [ajustes de una IA](../inference-providers.md) (**GPU lock** en los ajustes generales), para el modelo de lenguaje;
- un backend de [Image Generator](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images);
- un backend de [Audiobook](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech).

El campo **GPU lock** solo aparece cuando existe al menos un lock.

<a id="variables-in-the-steps"></a>

## Variables en los pasos

Un paso puede indicar para qué es el trabajo, de modo que un mismo paso sirva para varios modelos y los locks no necesiten editarse al probar otro. En la dirección, las cabeceras y el cuerpo:

| Variable | Es |
|---|---|
| `{{model}}` | el modelo que usa el trabajo: el id del modelo de la IA (tal como aparece en los ajustes de la IA, por ejemplo `openai/big-32b` a través de LiteLLM), el modelo de un backend de audiolibro (`higgs-audio-v3`, `chatterbox-turbo`), del reconocimiento de voz o del backend de imagen; vacío para un trabajo que no usa ninguno (la limpieza de clips) |
| `{{lock}}` | el nombre del lock |
| `{{text_model}}` | solo en un trabajo de generación de texto: el id del modelo de su IA (el mismo que `{{model}}`); vacío en los trabajos de voz e imagen |
| `{{last_text_model}}` | todo trabajo: el modelo de la última generación de texto en este lock, recordado desde el momento en que empieza ese trabajo (así, un trabajo de texto ve el suyo); vacío antes de la primera generación de texto y tras reiniciar Marginalia |

Los filtros cambian el valor: `{{model | strip:openai/}}` quita el prefijo cuando el valor empieza por él (el nombre de ruta de LiteLLM, que el servidor que hay detrás no conoce), `{{model | last}}` conserva lo que sigue al último `/`, `{{model | json}}` escapa el valor para una cadena JSON y `{{model | url}}` lo codifica para una dirección; se pueden encadenar (`{{model | last | json}}`). Los acquire steps y los steps before every job reciben los valores del trabajo; los release steps reciben los del último trabajo del lock que está cargado (el modelo que se va a descargar). **Test** ejecuta un paso con las variables vacías.

<a id="how-the-turns-work"></a>

## Cómo funcionan los turnos

- Cada petición a un modelo, cada imagen y cada lote o muestra de audiolibro toma el lock de su IA o de su backend **durante todo su tiempo de ejecución**.
- Cuando la GPU ya tiene cargado ese lock, el trabajo empieza enseguida y no se ejecuta ningún paso, salvo los **steps before every job** del lock.
- Cuando la GPU tiene cargado otro lock, el trabajo espera a que terminen los trabajos de ese lock. Después se ejecutan los **release steps** de ese lock (todos, aunque alguno falle), luego los steps before every job y los **acquire steps** del nuevo lock (en orden; el primer fallo los detiene y el trabajo falla con el mensaje del paso).
- Si falla un release step, el nuevo lock no se carga en absoluto (la tarjeta podría seguir llena): el trabajo falla con el error y la GPU sigue con el lock que tenía. El siguiente trabajo de otro lock vuelve a intentar liberarla.
- Tras un trabajo, la GPU **sigue cargada** con su lock. Nada se descarga por temporizador: una ventana de imagen que queda abierta, plegada o cerrada no retiene la GPU, y el modelo de imagen sigue cargado hasta que el modelo de lenguaje (u otro lock) necesite la tarjeta.
- Los trabajos esperan en el orden en que llegaron. Una vez que un trabajo espera a otro lock, los nuevos trabajos del lock cargado esperan detrás de él, de modo que un modelo de lenguaje muy ocupado no puede tener una imagen esperando para siempre.
- Una IA o un backend sin lock, o con un lock que se ha borrado, no espera a nada.

Lo que tiene cargado una GPU solo se conoce mientras la aplicación está en marcha: tras un reinicio no se sabe que nada esté cargado, y el primer trabajo ejecuta solo sus acquire steps.

<a id="a-gpu-that-others-use-too"></a>

## Una GPU que también usan otros

Los locks solo ordenan los trabajos de Marginalia. Lo que Marginalia sabe que tiene cargado la tarjeta procede de sus propios trabajos: tras un reinicio de Marginalia, o cuando otro programa, un script o una persona carga un modelo en la tarjeta (una petición al router que despierta el modelo de lenguaje, un flujo encolado en la propia página de ComfyUI), lo que Marginalia cree cargado es erróneo, y un trabajo puede empezar en una tarjeta llena.

- **Also used outside Marginalia** en la GPU hace que Marginalia deje de fiarse de lo que sabe: los **acquire steps se ejecutan antes de cada trabajo**, no solo cuando la GPU cambia de lock (los release steps siguen ejecutándose solo en un cambio). Entonces, los acquire steps de cada lock deben hacer sitio por sí mismos (descargar los otros modelos, detener el otro servidor) y ser seguros de repetir: arrancar un servidor que ya está en marcha, descargar un modelo que no está cargado y detener un contenedor parado deben responder con éxito, o todos los trabajos fallarán. Esas peticiones tienen un coste en cada trabajo.
- Los **steps before every job** hacen lo mismo para un solo lock sin los demás: solo los pasos que deben comprobarse cada vez (descargar los modelos del otro programa, asegurarse de que un servidor responde) se ejecutan antes de cada trabajo, y el resto solo en un cambio.
- El lugar seguro para esto es la propia máquina con GPU, por la que pasan todos los usuarios: un proxy cuyo *start ComfyUI* descarga antes el modelo de lenguaje, o un hook del router que detiene ComfyUI antes de cargar el modelo de lenguaje. Así, nadie puede llenar la tarjeta dos veces, sea cual sea el programa que lo pida.

<a id="the-tab"></a>

## La pestaña

La pestaña enumera las GPU con el lock con el que está cargada cada una ahora (*Unknown* si todavía no se ha ejecutado nada o se descargó) y cuántos trabajos se ejecutan y esperan en ella; después, los locks y los pasos, cada uno con botones para añadir, editar y borrar. **Refresh** vuelve a leer lo que tienen cargado las GPU.

![La pestaña GPUs con una GPU, un lock y los pasos](../../images/admin-gpus.png)

**Unload now** libera una GPU a mano, por ejemplo para usar la tarjeta en algo ajeno a Marginalia: espera a los trabajos en curso, ejecuta los release steps del lock cargado y deja la GPU como desconocida (también cuando ha fallado algún paso; el mensaje indica cuál). El siguiente trabajo vuelve a cargar su lock.

Borrar una GPU borra sus locks; borrar un paso lo quita de los locks que lo ejecutan. Las IA y los backends que nombraban un lock borrado se ejecutan sin ninguno.
<a id="an-example-one-card-several-models"></a>

## Un ejemplo: una tarjeta, varios modelos

Con `llama-server` detrás de LiteLLM y un proxy de estilo [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses) que inicia y detiene contenedores:

| Paso | Solicitud |
|---|---|
| Unload the language model | `POST http://litellm:4000/v1/chat/completions` con la cabecera `X-Llama-Unload: all` (ver [Liberar la GPU para otro modelo](../prompt-cache.md#freeing-the-gpu-for-another-model)) |
| Start ComfyUI / Stop ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |

Si solo lo usa Marginalia, cada bloqueo libera la tarjeta solo para sí mismo y nada más:

| Bloqueo | Pasos de adquisición | Pasos de liberación | Trabajos a la vez |
|---|---|---|---|
| Language model | (ninguno: el router lo carga con la siguiente solicitud) | Unload the language model | 1 |
| ComfyUI images | Start ComfyUI | Stop ComfyUI | 1 |
| ComfyUI speech | Start ComfyUI | Stop ComfyUI | 1 |
| ComfyUI speech recognition | Start ComfyUI | Stop ComfyUI | 1 |
| Clip cleanup | Start ComfyUI | Stop ComfyUI | 1 |

Un lote de voz tras un chat ejecuta *Unload the language model* (la liberación del modelo de lenguaje) y *Start ComfyUI*; el siguiente mensaje del chat ejecuta antes *Stop ComfyUI*. Los bloqueos de ComfyUI están separados a propósito: pasar de uno a otro detiene e inicia ComfyUI, que es lo que libera la memoria que un modelo retiene tras descargarse (fp32 Higgs retiene unos 16 GB de la memoria del servidor), de modo que el modelo de imágenes, Higgs, el reconocimiento y los modelos de limpieza nunca están juntos en ella.

Con **Also used outside Marginalia** cada adquisición debe hacer sitio por sí misma: *Language model* se adquiere con *Stop ComfyUI*, y cada bloqueo de ComfyUI con *Unload the language model, Start ComfyUI*.

Cuando otro programa usa **el mismo `llama-server` router** con un modelo propio (por ejemplo, un modelo pequeño junto a su modelo de voz), *Unload the language model* expulsaría también su modelo, y su propia descarga expulsaría el de Marginalia. `tools/gpu-control` tiene una llamada que descarga todos los modelos del router **excepto** los indicados: un paso `POST http://litellm:4000/gpu/llama/unload` con el cuerpo `{"keep": ["{{model | strip:openai/}}"]}` (su README explica el paso directo). Conviértalo en un **paso antes de cada trabajo** de *Language model*: cada IA de ese bloqueo conserva su propio modelo, sea cual sea el que elija; el otro programa hace lo mismo con su propio modelo antes de cada uno de sus trabajos. Cuando el modelo conservado es el único cargado, la llamada no hace nada y responde de inmediato, así que ninguno de los dos programas necesita saber del otro.

Cuando **un bloqueo atiende el modelo de lenguaje y el modelo de voz** (ambos funcionan en paralelo en la tarjeta), `{{model}}` en ese paso es el modelo de voz durante un trabajo de voz, que ningún modelo del router coincide, y el modelo de lenguaje se descargaría justo después de cada respuesta. Use `{{last_text_model}}` en su lugar, `{"keep": ["{{last_text_model | strip:openai/}}"]}`: cada trabajo del bloqueo conserva el modelo de la última generación de texto, sea cual sea el tipo de trabajo. Antes de la primera generación de texto no se conserva nada, y la primera carga su modelo.

Para ComfyUI, el mismo tipo de paso es `POST http://litellm:4000/gpu/comfy/ensure`: no hace nada cuando ComfyUI responde, inicia el contenedor si está detenido, y lo reinicia cuando está en marcha pero ComfyUI no responde (se ha caído por dentro, se ha bloqueado), y luego espera hasta que esté listo. Como paso antes de cada trabajo de los bloqueos de ComfyUI, garantiza que ComfyUI esté activo, lo haya detenido quien lo haya detenido.

La IA del modelo de lenguaje elige *Language model*, el backend de imágenes *ComfyUI images*, el backend de voz *ComfyUI speech*, el [reconocimiento de voz](../extensions/audiobook.md#speech-recognition) y la [limpieza de clips](../extensions/audiobook.md#cleanup-of-clips) del Audiobook sus propios bloqueos.
