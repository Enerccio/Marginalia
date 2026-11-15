---
label: GPU
order: 84
---

# GPU

Una tarjeta gráfica suele alojar un solo modelo a la vez: el modelo de lenguaje, el de imágenes o el de voz. La pestaña **GPU** de la página de administración permite que los modelos y servidores de una misma tarjeta se turnen para usarla, de modo que una imagen o un audiolibro no empiece mientras el modelo de lenguaje ocupa la tarjeta, y que el modelo de lenguaje vuelva cuando haga falta.

Lo configura una vez, como administrador; después, cada usuario elige un bloqueo para su IA o sus backends.

<a id="what-it-is-made-of"></a>

## De qué está hecho

| | |
|---|---|
| **GPU** | Una tarjeta (o cualquier equipo que aloje un solo modelo a la vez). Solo su nombre. |
| **Bloqueo de GPU** | Una forma de usar una GPU: lo que está cargado en ella. Por ejemplo, *Modelo de lenguaje*, *ComfyUI* y *Voz* en la misma GPU. Un bloqueo tiene dos listas ordenadas de pasos: los **pasos de adquisición** lo cargan (iniciar un contenedor, cargar un modelo, descargar lo que deba salir antes), los **pasos de liberación** liberan la tarjeta (detener el contenedor, descargar el modelo). **Trabajos a la vez** limita cuántos trabajos del bloqueo se ejecutan juntos (un servidor de imágenes que genera una imagen cada vez: 1; 0 para sin límite). |
| **GPU step** | Una petición HTTP: una dirección, un método, cabeceras (`Name: value`, una por línea, guardadas cifradas, de modo que aquí se puede escribir una clave bearer), un cuerpo, un tiempo de espera (vacío: 60 segundos) y una espera tras la respuesta. Un paso puede usarlo varios bloqueos; por ejemplo, un paso *descargar el modelo de lenguaje* que tanto el bloqueo de imagen como el de voz ejecutan primero. **Probar** hace la petición al momento. |

Elige el bloqueo en:

- los [ajustes de una IA](../inference-providers.md) (**Bloqueo de GPU** en los ajustes generales), para el modelo de lenguaje;
- un backend del [Generador de imágenes](../extensions/image-generator.md#one-gpu-for-the-model-and-the-images);
- un backend del [Audiolibro](../extensions/audiobook-server.md#one-gpu-for-the-model-and-the-speech).

El cuadro **Bloqueo de GPU** solo se muestra cuando existe al menos un bloqueo.

<a id="how-the-turns-work"></a>

## Cómo funcionan los turnos

- Cada petición a un modelo, cada imagen y cada lote o muestra de audiolibro toma el bloqueo de su IA o backend **durante su propia duración**.
- Si la GPU ya está cargada con ese bloqueo, el trabajo empieza al momento y no se ejecuta ningún paso.
- Si está cargada con otro bloqueo, el trabajo espera a que terminen los trabajos de ese bloqueo. Luego se ejecutan los **pasos de liberación** de ese bloqueo (todos, aunque falle alguno) y después los **pasos de adquisición** del nuevo bloqueo (en orden; el primer fallo los detiene y el trabajo falla con el mensaje del paso).
- Si falla un paso de liberación, el nuevo bloqueo no se carga en absoluto (la tarjeta puede seguir llena): el trabajo falla con el error y la GPU se queda con el bloqueo que tenía. El siguiente trabajo de otro bloqueo vuelve a intentar liberarla.
- Tras un trabajo, la GPU **sigue cargada** con su bloqueo. Nada se descarga por temporizador: una ventana de imagen que se deja abierta, plegada o cerrada no retiene la GPU, y el modelo de imagen sigue cargado hasta que el modelo de lenguaje (u otro bloqueo) necesite la tarjeta.
- Los trabajos esperan en el orden en que llegaron. Cuando un trabajo espera a otro bloqueo, los nuevos trabajos del bloqueo cargado esperan detrás de él, de modo que un modelo de lenguaje muy ocupado no puede tener una imagen esperando para siempre.
- Una IA o backend sin bloqueo, o con un bloqueo que se borró, no espera a nada.

Lo que tiene cargado una GPU solo se conoce mientras la aplicación está en marcha: tras un reinicio no se sabe nada cargado, y el primer trabajo ejecuta solo sus pasos de adquisición.

<a id="the-tab"></a>

## La pestaña

La pestaña muestra las GPU con el bloqueo con el que está cargada cada una ahora (*Unknown* si todavía no se ejecutó nada o se descargó) y cuántos trabajos se ejecutan y esperan en ella; después muestra los bloqueos y los pasos, cada uno con botones para añadir, editar y borrar. **Actualizar** vuelve a leer lo que tienen cargado las GPU.

**Descargar ahora** libera una GPU a mano, por ejemplo para usar la tarjeta en algo ajeno a Marginalia: espera a los trabajos en curso, ejecuta los pasos de liberación del bloqueo cargado y deja la GPU como desconocida (también cuando falló un paso; el mensaje indica cuál). El siguiente trabajo vuelve a cargar su bloqueo.

Borrar una GPU borra sus bloqueos; borrar un paso lo quita de los bloqueos que lo ejecutan. Las IA y backends que nombraban un bloqueo borrado se ejecutan sin ninguno.

<a id="an-example-one-card-three-models"></a>

## Un ejemplo: una tarjeta, tres modelos

Con `llama-server` detrás de LiteLLM y un proxy de estilo [`tools/gpu-control`](../extensions/audiobook-server.md#what-answers-those-addresses) que inicia y detiene contenedores:

| Paso | Petición |
|---|---|
| Descargar el modelo de lenguaje | `POST http://litellm:4000/v1/chat/completions` con la cabecera `X-Llama-Unload: all` (ver [Liberar la GPU para otro modelo](../prompt-cache.md#freeing-the-gpu-for-another-model)) |
| Iniciar ComfyUI / Detener ComfyUI | `POST http://litellm:4000/gpu/comfy/start` / `.../stop` |
| Iniciar voz / Detener voz | `POST http://litellm:4000/gpu/tts/start` / `.../stop` |

| Bloqueo | Pasos de adquisición | Pasos de liberación | Trabajos a la vez |
|---|---|---|---|
| Modelo de lenguaje | (ninguno: el enrutador lo carga con la siguiente petición) | (ninguno) | 0 |
| ComfyUI | Descargar el modelo de lenguaje, Iniciar ComfyUI | Detener ComfyUI | 1 |
| Voz | Descargar el modelo de lenguaje, Iniciar voz | Detener voz | 1 |

La IA del modelo de lenguaje elige *Modelo de lenguaje*, el backend de imagen *ComfyUI* y el backend de voz *Voz*. Un lote de voz tras un chat ejecuta *Descargar el modelo de lenguaje* e *Iniciar voz*; el siguiente mensaje del chat ejecuta antes *Detener voz*.
