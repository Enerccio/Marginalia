---
label: GPU
order: 84
---

# GPU

Una tarjeta gráfica suele alojar un solo modelo a la vez: el modelo de lenguaje, o el de imágenes, o el de voz. La pestaña **GPU** de la página Admin permite que los modelos y servidores de una misma tarjeta se turnen para usarla, de modo que una imagen o un audiolibro no empiece mientras el modelo de lenguaje ocupa la tarjeta, y que el modelo de lenguaje vuelva a cargarse cuando haga falta.

Lo configuras una vez, como administrador; después, cada usuario elige un bloqueo para su IA o sus backends.

<a id="what-it-is-made-of"></a>

## De qué se compone

| | |
|---|---|
| **GPU** | Una tarjeta (o cualquier máquina que aloje un solo modelo a la vez). Solo su nombre. |
| **Bloqueo de GPU** | Una forma de usar una GPU: lo que está cargado en ella. Por ejemplo, *Modelo de lenguaje*, *ComfyUI* y *Speech* en la misma GPU. Un bloqueo tiene dos listas ordenadas de pasos: los **pasos de adquisición** lo cargan (iniciar un contenedor, cargar un modelo, descargar lo que debe salir antes), los **pasos de liberación** dejan la tarjeta libre de él (detener el contenedor, descargar el modelo). **Trabajos a la vez** limita cuántos trabajos del bloqueo se ejecutan juntos (un servidor de imágenes que genera una imagen cada vez: 1; 0 para no poner límite). |
| **Paso de GPU** | Una petición HTTP: una dirección, un método, cabeceras (`Name: value`, una por línea, guardadas cifradas, así que aquí puede escribirse una clave de tipo bearer), un cuerpo, un tiempo de espera (vacío: 60 segundos) y una pausa tras la respuesta. Un paso puede usarlo varios bloqueos; por ejemplo, un único paso *descargar el modelo de lenguaje* que el bloqueo de imágenes y el de voz ejecutan primero. **Probar** hace la petición en el acto. |

Elige el bloqueo en:

- los [ajustes de una IA](../inference-providers.md) (**Bloqueo de GPU** en los ajustes generales), para el modelo de lenguaje;
- un backend del [Image Generator](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images);
- un backend del [Audiobook](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech).

La casilla **Bloqueo de GPU** solo se muestra cuando existe al menos un bloqueo.

<a id="how-the-turns-work"></a>

## Cómo funcionan los turnos

- Cada petición a un modelo, cada imagen y cada lote o muestra de audiolibro toma el bloqueo de su IA o backend **durante toda su duración**.
- Cuando la GPU ya está cargada con ese bloqueo, el trabajo empieza en el acto y no se ejecuta ningún paso.
- Cuando está cargada con otro bloqueo, el trabajo espera a que terminen los trabajos de ese bloqueo. Después se ejecutan los **pasos de liberación** de ese bloqueo (todos, aunque alguno falle) y luego los **pasos de adquisición** del nuevo bloqueo (en orden; el primer fallo los detiene y el trabajo falla con el mensaje del paso).
- Cuando falla un paso de liberación, el nuevo bloqueo no se carga en absoluto (la tarjeta puede seguir llena): el trabajo falla con el error y la GPU se queda con el bloqueo que tenía. El siguiente trabajo de otro bloqueo volverá a intentar liberarla.
- Tras un trabajo, la GPU **sigue cargada** con su bloqueo. No se descarga nada por temporizador: una ventana de imágenes que se queda abierta, plegada o cerrada no retiene la GPU, y el modelo de imágenes permanece cargado hasta que el modelo de lenguaje (u otro bloqueo) necesite la tarjeta.
- Los trabajos esperan en el orden en que llegaron. Una vez que un trabajo espera a otro bloqueo, los nuevos trabajos del bloqueo cargado esperan detrás de él, de modo que un modelo de lenguaje muy ocupado no puede tener a una imagen esperando para siempre.
- Una IA o backend sin bloqueo, o con un bloqueo que se ha borrado, no espera a nada.

Lo que tiene cargado una GPU solo se conoce mientras la aplicación se ejecuta: tras un reinicio no se sabe que haya nada cargado, y el primer trabajo ejecuta solo sus pasos de adquisición.

<a id="the-tab"></a>

## La pestaña

La pestaña muestra las GPU con el bloqueo con el que está cargada ahora (*Unknown* si aún no se ha ejecutado nada o si se descargó) y cuántos trabajos se ejecutan y esperan en ella; después muestra los bloqueos y los pasos, cada uno con botones para añadir, editar y borrar. **Actualizar** vuelve a leer lo que tienen cargado las GPU.

**Descargar ahora** libera una GPU a mano, por ejemplo para usar la tarjeta en algo ajeno a Marginalia: espera a los trabajos en curso, ejecuta los pasos de liberación del bloqueo cargado y deja la GPU como desconocida (también si algún paso falló; el mensaje indica cuál). El siguiente trabajo volverá a cargar su bloqueo.

Borrar una GPU borra sus bloqueos; borrar un paso lo quita de los bloqueos que lo ejecutan. Las IA y los backends que nombraban un bloqueo borrado se ejecutan sin bloqueo.

<a id="an-example-one-card-three-models"></a>

## Un ejemplo: una tarjeta, tres modelos

Con `llama-server` detrás de LiteLLM y un proxy de estilo [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses) que arranca y detiene contenedores:

| Paso | Petición |
|---|---|
| Descargar el modelo de lenguaje | `POST http://litellm:4000/v1/chat/completions` con la cabecera `X-Llama-Unload: all` (ver [Liberar la GPU para otro modelo](../prompt-cache.md#freeing-the-gpu-for-another-model)) |
| Iniciar ComfyUI / Detener ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |
| Iniciar voz / Detener voz | `POST http://litellm:4000/gpu/tts/start` / `.../stop` |

| Bloqueo | Pasos de adquisición | Pasos de liberación | Trabajos a la vez |
|---|---|---|---|
| Modelo de lenguaje | (ninguno: el router lo carga con la siguiente petición) | (ninguno) | 0 |
| ComfyUI | Descargar el modelo de lenguaje, Iniciar ComfyUI | Detener ComfyUI | 1 |
| Speech | Descargar el modelo de lenguaje, Iniciar voz | Detener voz | 1 |
| Limpieza de clips | Descargar el modelo de lenguaje, Iniciar ComfyUI | Detener ComfyUI | 1 |

La IA del modelo de lenguaje elige *Modelo de lenguaje*, el backend de imágenes elige *ComfyUI* y el backend de voz elige *Speech*. La [limpieza de clips](../extensions/audiobook.md#cleanup-of-clips) del Audiobook elige *Limpieza de clips*: se ejecuta en el mismo ComfyUI que las imágenes, pero un bloqueo propio reinicia ComfyUI entre ambos, de modo que el modelo de imágenes y los modelos de limpieza nunca están juntos en su memoria. Un lote de voz tras un chat ejecuta *Descargar el modelo de lenguaje* e *Iniciar voz*; el siguiente mensaje del chat ejecuta antes *Detener voz*.
