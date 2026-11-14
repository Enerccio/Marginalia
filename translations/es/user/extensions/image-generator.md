---
label: Generador de imágenes
order: 13
---

# Generador de imágenes

Crea una imagen para una parte de la historia. El modelo escribe el prompt de la imagen a partir de la parte, usted lo edita, un backend de imágenes dibuja la ilustración, usted vuelve a generarla hasta que le convenza y la acepta. Así se convierte en una imagen de la parte, como las que añade a mano con *Imágenes* en el menú de la parte (se muestra en la historia y en las exportaciones).

La extensión no genera imágenes por sí misma: se comunica con un servidor **ComfyUI** o con un servidor que use la API de imágenes de OpenAI (también la hablan stable-diffusion.cpp, LiteLLM y muchos servicios alojados).

<a id="adding-a-backend"></a>

## Añadir un backend

*Ajustes → Extension Settings → Image Generator Settings* muestra la lista de backends. Añada uno (+), rellénelo y guarde la página de *Ajustes*.

| Ajuste | Qué hace |
|---|---|
| Name, Type | El nombre que aparece en la ventana. **ComfyUI** o **OpenAI images API**. |
| Address | La dirección base del servidor, por ejemplo `http://localhost:8188` para ComfyUI. |
| Api key | Se envía como token bearer. Se guarda cifrada y no se vuelve a mostrar; escriba una nueva para reemplazarla, o use **Remove the key**. |
| Model | Solo para la API de imágenes de OpenAI: el `model` de la solicitud. |
| Workflow | Solo para ComfyUI: un flujo de trabajo guardado en ComfyUI con **Save (API Format)**. Escriba `{{prompt}}`, `{{negative}}`, `{{seed}}`, `{{width}}` y `{{height}}` donde deben ir. |
| The server is on this machine or on my network | Permite direcciones de la red local. Déjelo desactivado para un servidor en la nube: así no puede hacer que llame a su propia máquina. |
| Width, Height, Negative prompt | Con lo que empieza la ventana (puede cambiarlos allí para una imagen). |
| Timeout | Cuánto tiempo esperar a una imagen. |
| Release after idle | Ver [Una GPU](#one-gpu-for-the-model-and-the-images). |

![Un backend en los ajustes](../../images/plugin-imagegenerator-settings.png)

**Test connection** comprueba la dirección y la clave.

El panel también tiene el **Estilo** que se antepone a cada prompt (el aspecto del libro, por ejemplo *acuarela, luz suave*) y la **Instruction for the prompt writer**, el texto que recibe el modelo para escribir el prompt a partir de una parte. Si está vacío o sin cambios, se usa el integrado.

<a id="making-an-image"></a>

## Crear una imagen

![Generar imagen en el menú de una parte](../../images/plugin-imagegenerator-menu.png)

1. Elija **Generate image** en el menú de una parte. El modelo de la ventana (por defecto el del libro; se recuerda la última elección) empieza a escribir el prompt de la parte. Puede detenerlo con **Abort**, cambiar el prompt, o pulsar **Write the prompt with the model** para volver a escribirlo. Antes de volver a escribirlo puede rellenar:
   - **Send lorebook entries**: las entradas del lorebook del libro (y de sus sub lorebooks) se envían al modelo como en una generación, para que sepa cómo son los personajes y los lugares. Una entrada se envía cuando sus filtros aparecen en la parte: su texto, sus instrucciones, el escenario de la escena, el punto de vista y los personajes presentes, o en sus *Details*. Las entradas que siempre están activas también se envían. Solo caben tantas como permita el contexto del modelo (un aviso indica cuántas se han dejado fuera). La elección se recuerda.
   - **Details**: lo que la imagen debe mostrar o cómo debe verse (*de noche, primer plano de su cara, el vestido rojo*); el escritor del prompt lo sigue. Solo vale para esta ventana.
2. Elija el backend y pulse **Generar**. En *Size and negative prompt* puede cambiar el tamaño y el prompt negativo para esta ventana.
3. Aparece la imagen. **Reroll** crea otra con una nueva semilla, **Discard** la descarta, **Accept** la añade a la parte y cierra la ventana. **Abort** detiene una imagen que se está generando (a ComfyUI se le pide que interrumpa el trabajo).

![La ventana con el prompt escrito por el modelo](../../images/plugin-imagegenerator-prompt.png)

![La imagen, con Accept, Discard y Reroll](../../images/plugin-imagegenerator.png)

La parte abierta muestra la nueva imagen bajo su texto de inmediato.

<a id="one-gpu-for-the-model-and-the-images"></a>

## Una GPU para el modelo y las imágenes

Con una GPU pequeña, el modelo de lenguaje y el modelo de imágenes no se pueden cargar a la vez. La extensión no gestiona la memoria; cada backend puede tener dos listas de solicitudes que usted configura:

- **Before the first image**: se hace una vez, cuando la ventana empieza a usar el backend. Por ejemplo, una solicitud a su proxy que descarga el modelo de lenguaje y carga el de imágenes.
- **After the last image**: se hace cuando acepta, descarta o cierra la ventana, o cuando han pasado los segundos de **Release after idle** desde la última imagen (así una ventana olvidada no retiene la GPU; 0 lo desactiva). En ComfyUI puede ser `POST <address>/free` con el cuerpo `{"unload_models": true, "free_memory": true}`; en un proxy, una solicitud que vuelva a cargar el modelo de lenguaje.

Con `llama-server` en modo router detrás de LiteLLM, la solicitud *before* puede descargar el modelo de lenguaje a través del proxy: vea [Liberar la GPU para otro modelo](../prompt-cache.md#freeing-the-gpu-for-another-model).

Ambos momentos aceptan una **lista de solicitudes**, que se hacen una tras otra (**Add a request** añade una, la papelera la elimina). Un cambio suele necesitar varias: descargar el modelo de lenguaje, luego arrancar el servidor de imágenes y, tras las imágenes, detenerlo. Cada solicitud tiene una dirección, un método (`POST` por defecto), cabeceras (`Name: value`, una por línea), un cuerpo, un **timeout** (vacío: 60 segundos, como máximo 600; déle más tiempo a un servidor que carga un modelo grande) y **Wait afterwards** (segundos de pausa tras la respuesta, para un servidor que sigue ocupado después de decir que sí). Las cabeceras se guardan cifradas, como la clave de API (una solicitud suele llevar `Authorization: Bearer ...`). El backend queda retenido mientras hace reroll, así que solo la primera imagen paga el cambio. Si un proxy delante de los modelos los cambia según el nombre del modelo, deje ambas listas vacías.

Cuando falla una solicitud de la lista *before*, no se hacen las demás y la imagen se detiene (el mensaje indica qué solicitud falló: *Request 2 of 3: ...*), para que nada se cargue en una GPU que sigue llena. La lista *after* se hace **completa** pase lo que pase con una de sus solicitudes (todos los servidores deben detenerse), y el primer fallo se muestra como aviso. Un backend guardado con la solicitud única de una versión anterior la muestra como primera solicitud de la lista.

**Varias personas en una GPU.** Todos los que tengan un backend con la misma dirección de servidor (`http://host:port`) pasan por una sola puerta: la solicitud *before* se hace para la primera ventana que arranca y la *after* cuando termina la última. Así, cerrar una ventana no descarga el modelo bajo la imagen de otra, y las imágenes se hacen de una en una (una segunda ventana espera su turno). Las solicitudes son las del usuario que llega primero y las del que sale último.

Un backend también puede indicar la GPU en la que funciona (**GPU (name)**). Los backends que indican la misma GPU se esperan entre sí aunque sus direcciones sean distintas, y eso incluye los backends del [Audiolibro](audiobook.md) (un servidor de voz y un servidor de imágenes en una misma tarjeta): un usuario cuyo backend necesita otro modelo espera a que terminen los demás y luego ejecuta sus propias solicitudes, en lugar de unirse a una carga que no es suya. Si lo deja vacío, solo esperan entre sí las solicitudes a la misma dirección.

<a id="what-is-not-done"></a>

## Lo que no se hace

- Los backends son por usuario, no por libro.
