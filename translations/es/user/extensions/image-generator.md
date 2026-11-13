---
label: Generador de imágenes
order: 13
---

# Generador de imágenes

Crea una imagen para una parte de la historia. El modelo escribe el prompt de la imagen a partir de la parte, tú lo editas, un backend de imágenes dibuja la ilustración, y repites la generación hasta que te convenza y la aceptas. Así se convierte en una imagen de la parte, igual que una que añades a mano con *Imágenes* en el menú de la parte (se muestra en la historia y en las exportaciones).

La extensión no genera imágenes por sí misma: se comunica con un servidor **ComfyUI** o con un servidor que ofrece la API de imágenes de OpenAI (también la hablan stable-diffusion.cpp, LiteLLM y muchos servicios alojados).

<a id="adding-a-backend"></a>

## Añadir un backend

*Ajustes → Extension Settings → Image Generator Settings* muestra la lista de backends. Añade uno (+), rellénalo y guarda la página *Ajustes*.

| Ajuste | Qué hace |
|---|---|
| Name, Type | El nombre que se muestra en la ventana. **ComfyUI** o **OpenAI images API**. |
| Address | La dirección base del servidor, por ejemplo `http://localhost:8188` para ComfyUI. |
| Api key | Se envía como token bearer. Se guarda cifrada y no se vuelve a mostrar; escribe una nueva para reemplazarla, o usa **Remove the key**. |
| Model | Solo para la API de imágenes de OpenAI: el `model` de la petición. |
| Workflow | Solo para ComfyUI: un workflow guardado en ComfyUI con **Save (API Format)**. Escribe `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` y `{{height}}` donde corresponda. |
| The server is on this machine or on my network | Permite direcciones de la red local. Déjalo desactivado para un servidor en la nube: así no se le puede hacer llamar a tu propio equipo. |
| Width, Height, Negative prompt | Los valores con los que empieza la ventana (puedes cambiarlos allí para una sola imagen). |
| Timeout | Cuánto tiempo se espera a que llegue una imagen. |
| Release after idle | Consulta [Una GPU](#one-gpu-for-the-model-and-the-images). |

![Un backend en los ajustes](../../images/plugin-imagegenerator-settings.png)

**Test connection** comprueba la dirección y la clave.

El panel también tiene el **Estilo** que se antepone a cada prompt (el aspecto del libro, por ejemplo *acuarela, luz suave*) y la **Instruction for the prompt writer**, el texto que se le da al modelo para escribir el prompt a partir de una parte. Si está vacío o sin cambios, se usa el integrado.

<a id="making-an-image"></a>

## Generar una imagen

![Generar imagen en el menú de una parte](../../images/plugin-imagegenerator-menu.png)

1. Elige **Generate image** en el menú de una parte. El modelo de la ventana (por defecto, el del libro; se recuerda la última elección) empieza a escribir el prompt de la parte. Puedes detenerlo con **Abort**, cambiar el prompt o pulsar **Write the prompt with the model** para que lo vuelva a escribir. Antes de volver a escribirlo puedes rellenar:
   - **Send lorebook entries**: las entradas del lorebook del libro (y de sus sublorebooks) se envían al modelo como en una generación, para que sepa cómo son los personajes y los lugares. Una entrada se envía cuando sus filtros aparecen en la parte: en su texto, instrucciones, escenario, punto de vista o personajes presentes, o en tus *Details*. También se envían las entradas que están siempre activas. Solo caben tantas como permita el contexto del modelo (un aviso indica cuántas se han dejado fuera). La elección se recuerda.
   - **Details**: lo que la imagen debe mostrar o cómo debe verse (*de noche, primer plano de su cara, el vestido rojo*); el redactor del prompt lo sigue. Solo vale para esta ventana.
2. Elige el backend y pulsa **Generar**. Bajo *Size and negative prompt* puedes cambiar el tamaño y el prompt negativo para esta ventana.
3. Aparece la imagen. **Reroll** crea otra con una nueva semilla, **Discard** la descarta y **Accept** la añade a la parte y cierra la ventana. **Abort** detiene una imagen que se está generando (a ComfyUI se le pide que interrumpa el trabajo).

![La ventana con el prompt escrito por el modelo](../../images/plugin-imagegenerator-prompt.png)

![La imagen, con Accept, Discard y Reroll](../../images/plugin-imagegenerator.png)

La parte abierta muestra la nueva imagen bajo su texto en cuanto se acepta.

<a id="one-gpu-for-the-model-and-the-images"></a>

## Una GPU para el modelo y las imágenes

Con una GPU pequeña, el modelo de lenguaje y el de imágenes no pueden cargarse a la vez. La extensión no gestiona la memoria; cada backend puede tener dos listas de peticiones que tú configuras:

- **Before the first image**: se hacen una vez, cuando la ventana empieza a usar el backend. Por ejemplo, una petición a tu proxy que descargue el modelo de lenguaje y cargue el de imágenes.
- **After the last image**: se hacen cuando aceptas, descartas o cierras la ventana, o cuando han pasado los segundos de **Release after idle** desde la última imagen (así una ventana olvidada no mantiene ocupada la GPU; 0 lo desactiva). En ComfyUI puede ser `POST <address>/free` con el cuerpo `{"unload_models": true, "free_memory": true}`; en un proxy, una petición que vuelva a cargar el modelo de lenguaje.

Con `llama-server` en modo router detrás de LiteLLM, la petición *before* puede descargar el modelo de lenguaje a través del proxy: consulta [Liberar la GPU para otro modelo](../prompt-cache.md#freeing-the-gpu-for-another-model).

Ambos momentos aceptan una **lista de peticiones**, que se hacen una tras otra (**Add a request** añade una; el cubo elimina una). Un cambio de modelo suele necesitar varias: descargar el modelo de lenguaje, arrancar el servidor de imágenes y, tras las imágenes, detenerlo. Cada petición tiene una dirección, un método (`POST` por defecto), cabeceras (`Nombre: valor`, una por línea), un cuerpo, un **timeout** (vacío: 60 segundos, como máximo 600; dale más a un servidor que carga un modelo grande) y **Wait afterwards** (segundos de pausa tras la respuesta, para un servidor que sigue ocupado después de decir que sí). Las cabeceras se guardan cifradas, como la clave de la API (una petición suele llevar `Authorization: Bearer ...`). El backend permanece reservado mientras repites la generación, así que solo la primera imagen paga el cambio de modelo. Si un proxy delante de los modelos los intercambia según el nombre del modelo, deja ambas listas vacías.

Cuando falla una petición de la lista *before*, el resto no se hace y la imagen se detiene (el mensaje indica qué petición falló: *Request 2 of 3: ...*), para que no se cargue nada en una GPU que sigue llena. La lista *after* se ejecuta **completa** pase lo que pase con una de sus peticiones (hay que detener todos los servidores), y el primer fallo se muestra como advertencia. Un backend guardado con la única petición de una versión anterior se muestra como la primera petición de la lista.

**Varias personas en una misma GPU.** Todos los usuarios cuyo backend tenga la misma dirección de servidor (`http://host:port`) pasan por una misma puerta: la petición *before* se hace para la primera ventana que arranca y la *after* cuando termina la última, así que cerrar una ventana no descarga el modelo bajo la imagen de otra, y las imágenes se generan una a una (una segunda ventana espera su turno). Las peticiones son las del usuario que llega primero y las del que sale último.

Un backend también puede indicar la GPU en la que funciona (**GPU (name)**). Los backends que indican la misma GPU esperan unos a otros aunque sus direcciones sean distintas; esto incluye los backends del [Audiolibro](audiobook.md) (un servidor de voz y un servidor de imágenes en una misma tarjeta): un usuario cuyo backend necesita otro modelo espera a que los demás terminen y entonces ejecuta sus propias peticiones, en lugar de sumarse a una carga que no es suya. Si lo dejas vacío, solo esperan entre sí las peticiones a la misma dirección.

<a id="what-is-not-done"></a>

## Lo que no se hace

- Los backends son de cada usuario, no de cada libro.
