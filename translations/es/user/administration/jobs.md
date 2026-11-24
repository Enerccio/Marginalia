---
label: Trabajos
order: 86
---

# Trabajos

Todo lo que la aplicación pide a una **IA externa** es un *trabajo*: una generación, un resumen, una anotación, una ejecución de agente, un capítulo convertido en audio, una imagen. Cada trabajo queda registrado con el usuario para el que se ejecutó, cuándo empezó y terminó, cómo acabó, y cada *llamada* que hizo a un modelo o a un servidor de voz o de imágenes, de nuevo con su propio inicio y fin. La pestaña **Jobs** de la página Admin muestra el registro; la página `/jobs` muestra lo que se está ejecutando ahora.

Solo se guardan metadatos: quién, cuándo, cuánto duró, qué IA o servidor (el nombre de la IA, o el host del servidor de voz o de imágenes; nunca la ruta de la dirección ni la consulta), el modelo, cómo terminó y el mensaje de error corto de un fallo. **Nunca se guarda ningún prompt, ninguna respuesta, ningún texto.**

<a id="what-is-one-job"></a>

## Qué es un trabajo

- Una **petición que nada agrupa** (una generación, una anotación, una reescritura, una suplantación del personaje, un resumen de una parte, una pregunta al asistente de ayuda, una petición de un plugin como la traducción de una parte o de una entrada de lorebook) es un trabajo propio con una sola llamada. Su tipo es el tipo de trabajo (`generation`, `annotate`, `summary`...).
- Una **reescritura de los resúmenes** es un trabajo (`summary`) con una llamada por cada resumen escrito.
- Una **sesión de trabajo de una ejecución de agente** es un trabajo (`agent`) con una llamada por cada ronda del agente; una ejecución que se pausa y se reanuda tiene un trabajo por cada sesión, y las peticiones de un subagente son trabajos propios.
- Una **conversión a audio** es un trabajo (`audiobook`) por cada lote de partes, con las peticiones del modelo que etiqueta y cada fragmento hablado como sus llamadas.
- Una imagen creada por el **Image Generator** es un trabajo propio (`image-generator`) con una llamada de imagen.

<a id="what-runs-now-jobs"></a>

## Qué se ejecuta ahora: `/jobs`

![La página /jobs con una generación en curso](../../images/jobs-live.png)

Abre `/jobs` en la dirección de tu Marginalia (por ejemplo `http://localhost:8080/jobs`; el enlace **Open the live view** de la pestaña lo hace). Pide el inicio de sesión como cualquier otra página y no muestra nada a los usuarios que no son administradores. La lista se actualiza cada dos segundos y muestra, para cada trabajo en ejecución, el usuario, el tipo, cuánto lleva ejecutándose, cuántas de sus llamadas están terminadas y qué hay en curso ahora (la IA o el servidor y cuánto espera la llamada), y el libro de la ejecución para la que trabaja el trabajo, con su **Stop**. Es la vista rápida cuando el servidor parece ocupado: una lista larga de llamadas esperando a un mismo modelo te dice por qué todo va lento.

Debajo de los trabajos, **Runs on the books** lista lo que trabaja sobre un libro ahora y evita que partes de él se borren o cambien mientras tanto (véase el [panel de las ejecuciones](../books/story-editor.md#the-panel-of-the-runs), que muestra lo mismo a cada usuario para sus propios libros): el usuario, el libro, qué hace, en qué punto está (lo que dice el panel: *Waiting for the model*, *The model is thinking/writing...*, *Working*, *Waits for you*, y en una generación su etapa, como *INFERENCE* mientras escribe su parte; la ventana de un libro abierto no muestra nada ahí), desde cuándo, y cuántos objetos retiene (el libro, partes). La ventana de un libro abierto también es una ejecución. **Stop** detiene la ejecución como lo haría su usuario. **End**, tras una pregunta, termina una ejecución sin detener su trabajo: lo que retiene se devuelve de inmediato. Úsalo solo para una ejecución que nunca termina; su trabajo puede seguir escribiendo cuando despierte. La mayoría de estas ejecuciones terminan solas: una ejecución en la que nada trabajó durante una hora, y que no espera por ti ni por su modelo, la termina el servidor, y también la ventana de una página que se cerró.

<a id="the-log-the-jobs-tab"></a>

## El registro: la pestaña Jobs

![La pestaña Jobs de la página Admin](../../images/admin-jobs.png)

| | |
|---|---|
| **User, Kind, Status** | Filtros. El *tipo* es lo que es el trabajo (`generation`, `summary`, `annotate`, `agent`, `audiobook`, `image-generator`...); una petición hecha por código que no nombró su trabajo es `untagged`. |
| **Older than (days)** | Solo los trabajos que empezaron hace esa cantidad de días o antes. |
| **Refresh** | Vuelve a leer el registro. |
| **Delete these jobs** | Borra para siempre los trabajos que muestra el filtro y sus llamadas, tras una pregunta que dice cuántos son. Sin filtro, borra todo el registro. |
| **Open the live view** | Abre `/jobs` en una pestaña nueva. |

Haz clic en una fila para ver las llamadas del trabajo: el tipo (modelo de lenguaje, audio, imagen), la IA o el servidor, el modelo, inicio, fin, duración, estado y el error de una llamada fallida. Un trabajo que aparece como fallido hizo una llamada que falló, o el código que lo ejecutaba lo terminó como fallido; una llamada cancelada es una que tú detuviste (o que seguía en curso cuando terminó su trabajo).

![Las llamadas de una conversión de audiolibro](../../images/admin-jobs-calls.png)

Un trabajo fallido muestra qué llamada falló y por qué:

![Una conversión fallida con el error de su llamada de voz](../../images/admin-jobs-failed.png)

**Nada se borra por sí solo.** El registro crece con cada petición, así que límpialo de vez en cuando: pon *Older than (days)* (por ejemplo, 30), revisa lo que aparece en la lista y borra. La [limpieza](cleanup.md) elimina los trabajos de un usuario junto con el usuario.

![Los trabajos con más de 30 días, y la pregunta antes de borrarlos](../../images/admin-jobs-old.png)

![La pregunta antes de borrar los trabajos](../../images/admin-jobs-delete.png)

Un trabajo se escribe en el registro cuando termina; un trabajo que aún se ejecuta solo está en `/jobs`. Si la aplicación se detiene, los trabajos que se estaban ejecutando se pierden (nunca llegaron a terminar). Un trabajo que nadie terminó y que no hace ninguna llamada durante media hora se marca como completado.
