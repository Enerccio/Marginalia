---
label: Generador de imágenes
order: 13
---

# Generador de imágenes

Crea una imagen para una parte de la historia. El modelo escribe el prompt de la imagen a partir de la parte, tú lo editas, un backend de imágenes dibuja
la ilustración, repites hasta que encaje y la aceptas: pasa a ser una imagen de la parte, como las que añades a mano con *Imágenes* en
el menú de la parte (se muestra en la historia y en las exportaciones).

La extensión no genera imágenes por sí misma: se comunica con un servidor **ComfyUI** o con un servidor que usa la API de imágenes de **OpenAI** (también
la hablan stable-diffusion.cpp, LiteLLM y muchos servicios alojados).

<a id="adding-a-backend"></a>

## Añadir un backend

*Ajustes → Ajustes de extensiones → Ajustes de Generador de imágenes* muestra los backends. Añade uno (+), rellénalo y guarda la página de *Ajustes*.

| Ajuste | Qué hace |
|---|---|
| Name, Type | El nombre que se muestra en la ventana. **ComfyUI** o **OpenAI images API**. |
| Address | La dirección base del servidor, por ejemplo `http://localhost:8188` para ComfyUI. |
| Api key | Se envía como token de portador al servidor de imágenes. Se guarda cifrada y no se vuelve a mostrar; escribe una nueva para reemplazarla, o **Remove the key**. |
| Key of the requests around the images | Se envía como `Authorization: Bearer` en cada petición [antes y después de las imágenes](#one-gpu-for-the-model-and-the-images) que no tenga su propia cabecera `Authorization`, por ejemplo la clave de un proxy LiteLLM. Se guarda cifrada. Vacío: se usa la api key. |
| Model | Solo para la API de imágenes de OpenAI: el `model` de la petición. |
| Workflow | Solo ComfyUI: un workflow guardado en ComfyUI con **Save (API Format)**. Escribe `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` y `{{height}}` donde deban ir, y `{{lora}}` / `{{lora_strength}}` en un cargador de LoRA para [elegir la LoRA en la ventana](#choosing-a-lora). |
| The server is on this machine or on my network | Permite direcciones de la red local. Déjalo desactivado para un servidor en la nube: así no puede hacer que llame a tu propia máquina. |
| Width, Height | Con lo que empieza la ventana (puedes cambiarlos allí para una imagen). |
| Prompt form | Cómo escribe el modelo de lenguaje el prompt para este backend, según su modelo de imágenes: **Tags** (frases cortas separadas por comas, para SDXL, Pony, Illustrious), **Descriptive sentences** (un párrafo, para Flux, Krea, Qwen-Image, Z-Image) o **Free text**, tu propia instrucción para quien escribe el prompt (parte de la que se haya elegido). Un backend creado antes de que existiera este ajuste escribe etiquetas, o usa la instrucción que hubieras definido entonces como texto libre. Las instrucciones integradas están en tu [idioma de prompts](../account.md#prompt-language) y piden el prompt de imagen en inglés, que los modelos de imágenes leen mejor. |
| Timeout | Cuánto tiempo se espera a una imagen. |
| GPU lock | El [bloqueo de GPU](../administration/gpus.md) de la GPU en la que corre el servidor, cuando la comparte con el modelo de lenguaje u otros servidores; véase [Una GPU](#one-gpu-for-the-model-and-the-images). Solo se muestra si el administrador configuró los bloqueos de GPU. |

![Un backend en los ajustes](../../images/plugin-imagegenerator-settings.png)

**Test connection** comprueba la dirección y la clave.

El panel tiene también dos textos que un libro puede cambiar:

- **Style** - se antepone a cada prompt: el aspecto de las imágenes, por ejemplo *watercolor, soft light*. A quien escribe el prompt también se le indica, para que el prompt no describa otro estilo.
- **Negative prompt** - lo que las imágenes no deben mostrar. Un modelo turbo que funciona sin guía (CFG 1) lo ignora. El negative prompt que tenía antes un backend se traslada aquí la primera vez que abras el panel.

Lo que configuras en el panel es tu valor por defecto. Un libro (su pestaña *Prompts*) o una [carpeta](../books/folders.md) pueden definir los suyos en
*Ajustes de extensiones → Generador de imágenes*; un libro usa los suyos, si no los de la carpeta más cercana, y si no los tuyos.

<a id="making-an-image"></a>

## Crear una imagen

![Generar imagen en el menú de una parte](../../images/plugin-imagegenerator-menu.png)

1. Elige **Generate image** en el menú de una parte. Todavía no se envía nada a un modelo: primero rellena lo que quien escribe el prompt debe seguir, y después pulsa **Write the prompt with the model** (o <kbd>Ctrl+Enter</kbd> en *Details*). El modelo de la ventana (por defecto el del libro; se recuerda la última elección) escribe el prompt de la parte; puedes detenerlo con **Abort**, cambiar el prompt, escribirlo tú o volver a escribirlo. Esto se aplica solo a la escritura del prompt, no a la imagen en sí:
   - **Send lorebook entries** - las entradas del lorebook del libro (y de sus sub lorebooks) se envían al modelo como en una generación, para que sepa cómo son los personajes y los lugares. Una entrada se envía cuando sus filtros aparecen en la parte: su texto, instrucciones, escenario de la escena, punto de vista y personajes presentes, o en tus *Details*. Las entradas que están siempre activas también se envían. Solo caben tantas como permita el contexto del modelo (un aviso indica cuántas se han dejado fuera). La elección se recuerda.
   - **Details** - lo que la imagen debe mostrar o cómo debe verse (*de noche, primer plano de su cara, el vestido rojo*); quien escribe el prompt lo sigue. Es solo para esta ventana.
2. Elige el backend y pulsa **Generate**. La imagen se hace a partir del **Prompt** con el **Style** delante, y el **Negative prompt**; ambos empiezan con los del libro (o con tu valor por defecto), y un cambio es solo para esta ventana (el estilo también se indica a quien escribe el prompt). El tamaño empieza con el del backend y puede cambiarse para esta ventana.
3. Aparece la imagen. **Reroll** hace otra con una nueva semilla, **Discard** la descarta, **Accept** la añade a la parte y cierra la ventana. **Abort** detiene una imagen que se está creando (a ComfyUI se le pide que interrumpa el trabajo).

![La ventana con el prompt escrito por el modelo](../../images/plugin-imagegenerator-prompt.png)

![La imagen, con Accept, Discard y Reroll](../../images/plugin-imagegenerator.png)

La parte abierta muestra la nueva imagen bajo su texto de inmediato.

La ventana recuerda lo que usaste en cada parte: los details, el estilo, el negative prompt, el prompt y el tamaño se guardan con la parte cuando se escribe el prompt y cuando generas, y la próxima vez que abras la ventana en esa parte empieza con ellos. Una parte para la que nunca creaste una imagen empieza con el estilo y el negative prompt del libro.

<a id="choosing-a-lora"></a>

## Elegir una LoRA

Una LoRA es un pequeño complemento del modelo de imágenes, normalmente para un estilo. Con ComfyUI puedes elegirla en la ventana en lugar de guardar de nuevo el workflow para cada una:

1. En el workflow exportado, escribe `{{lora}}` como `lora_name` del cargador de LoRA (*Load LoRA*, `LoraLoaderModelOnly`, o
   `LoraLoader`), y `{{lora_strength}}` como su intensidad (`strength_model`; `strength_clip` también para `LoraLoader`).
2. La ventana muestra entonces **LoRA** con los archivos de LoRA del servidor (su carpeta `models/loras`) y **LoRA strength**. También puedes
   escribir el nombre de un archivo que la lista no muestre.
3. **None** (una LoRA vacía) quita los cargadores de LoRA del workflow, así que la imagen se hace sin ellos.

La LoRA y la intensidad con las que generas se recuerdan para el backend y están ahí la próxima vez. Lo mismo ocurre con la lista: un servidor que comparte la GPU a menudo no está en marcha cuando se abre la ventana (las peticiones previas a la primera imagen lo arrancan), así que la ventana ofrece las LoRA que el servidor listó la última vez y vuelve a preguntar tras la primera imagen. Solo cuando nunca ha podido preguntar, un aviso indica que no se pudieron leer las LoRA; aun así puedes escribir el nombre de un archivo.
<a id="one-gpu-for-the-model-and-the-images"></a>

## Una GPU para el modelo y las imágenes

Con una GPU pequeña, el modelo de lenguaje y el modelo de imágenes no pueden cargarse a la vez. El administrador lo configura una sola vez, para todos los modelos y servidores de esa GPU, en [Admin → GPUs](../administration/gpus.md): un **Bloqueo de GPU** para el modelo de lenguaje y otro para el servidor de imágenes, cada uno con las peticiones que lo cargan y las que liberan la GPU. Elige el bloqueo del servidor de imágenes en **Bloqueo de GPU** del backend (y el del modelo de lenguaje en sus [ajustes de IA](../inference-providers.md)).

Cada imagen espera su turno en la GPU:

- Cuando la GPU ya tiene cargado el servidor de imágenes, la imagen empieza al instante; no se descarga nada entre regeneraciones.
- Cuando la GPU tiene cargado el modelo de lenguaje (u otro servidor), la imagen espera a que el modelo responda, el bloqueo del modelo libera la GPU y el bloqueo del servidor de imágenes la carga.
- No se retiene nada entre imágenes: cerrar o plegar la ventana, o dejarla abierta, no ocupa ninguna GPU. La siguiente petición al modelo de lenguaje libera por sí sola la GPU del servidor de imágenes, que permanece cargado hasta entonces.

La ventana muestra *Generating the image* mientras espera su turno.

**Peticiones antes y después de cada imagen.** Un backend también puede tener sus dos listas propias de peticiones, que se ejecutan dentro del bloqueo alrededor de cada imagen: **Before each image** y **After each image**. Liberar la GPU corresponde al bloqueo de GPU; usa estas listas para cualquier otra cosa que el servidor necesite alrededor de una imagen. Cada lista se ejecuta petición a petición, una tras otra (**Add a request** añade una y la papelera elimina una). Cada petición tiene una dirección, un método (`POST` por defecto), cabeceras (`Name: value`, una por línea), un cuerpo, un **timeout** (vacío: 60 segundos, como máximo 600) y **Wait afterwards** (segundos de pausa tras la respuesta). Las cabeceras se guardan cifradas, igual que la clave de API.

Cuando falla una petición de la lista *antes*, no se ejecuta el resto y la imagen se detiene (el mensaje indica qué petición falló: *Request 2 of 3: ...*). La lista *después* se ejecuta **completa** pase lo que pase con una de sus peticiones, y el fallo se escribe en el registro de la extensión; la imagen se conserva. Un backend guardado con la petición única de una versión anterior la muestra como primera petición de la lista.

Los backends guardados antes de que existieran los bloqueos de GPU pierden sus ajustes **GPU (name)** y **Release after idle**: elige un bloqueo de GPU en su lugar y mueve las peticiones que liberaban la GPU (descargar el modelo de lenguaje, `POST <address>/free` de ComfyUI) a los pasos de los bloqueos.

<a id="what-is-not-done"></a>

## Lo que no se hace

- Los backends son por usuario, no por libro.
