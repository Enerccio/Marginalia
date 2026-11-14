---
label: Tareas
order: 86
---

# Tareas

Todo lo que la aplicación pide a una **IA externa** es una *tarea*: una generación, un resumen, una anotación, una ejecución de un agente, un capítulo
convertido a audio, una imagen. Cada tarea queda registrada con el usuario para el que se ejecutó, cuándo empezó y terminó, cómo terminó y cada *llamada*
que hizo a un modelo o a un servidor de voz o de imágenes, también con su propio inicio y fin. La pestaña **Tareas** de la página de Administración muestra el registro;
la página `/jobs` muestra lo que se ejecuta ahora mismo.

Solo se guardan metadatos: quién, cuándo, cuánto duró, qué IA o servidor (el nombre de la IA, o el host del servidor de voz o de imágenes; nunca
la ruta de la dirección ni la consulta), el modelo, cómo terminó y el mensaje de error breve de un fallo. **Nunca se guarda ningún prompt, ninguna respuesta, ningún texto.**

<a id="what-is-one-job"></a>

## Qué es una tarea

- Una **petición que no agrupa nada** (una generación, una anotación, una reescritura, una suplantación, un resumen de una parte, una pregunta al asistente de ayuda, una petición de un plugin como una traducción de una parte o de una entrada del lore) es una tarea propia con una sola llamada. Su tipo es el tipo de trabajo (`generation`, `annotate`, `summary`...).
- Una **reescritura de los resúmenes** es una tarea (`summary`) con una llamada por cada resumen escrito.
- Una **sesión de trabajo de una ejecución de agente** es una tarea (`agent`) con una llamada por cada ronda del agente; una ejecución que se pausa y se reanuda tiene una tarea por cada sesión, y las peticiones de un subagente son tareas propias.
- Una **conversión a audio** es una tarea (`audiobook`) por cada lote de partes, con las peticiones del modelo que etiqueta y cada fragmento leído como llamadas.
- Una imagen creada por el **Generador de imágenes** es una tarea propia (`image-generator`) con una llamada de imagen.

<a id="what-runs-now-jobs"></a>

## Qué se ejecuta ahora: `/jobs`

![La página /jobs con una generación en curso](../../images/jobs-live.png)

Abre `/jobs` en la dirección de tu Marginalia (por ejemplo `http://localhost:8080/jobs`; el enlace **Abrir la vista en directo** de la pestaña lo hace).
Pide el inicio de sesión como cualquier otra página y no muestra nada a los usuarios que no son administradores. La lista se actualiza
cada dos segundos y muestra, para cada tarea en ejecución, el usuario, el tipo, el tiempo que lleva en marcha, cuántas de sus llamadas están terminadas y qué está en curso
ahora (la IA o servidor y cuánto tiempo espera la llamada). Es la vista rápida cuando el servidor parece ocupado: una larga lista de llamadas esperando
a un mismo modelo explica por qué todo va lento.

Debajo de las tareas, **Ejecuciones sobre los libros** lista lo que trabaja ahora sobre un libro y evita que partes de él se borren o cambien mientras tanto
(véase el [panel de las ejecuciones](../books/story-editor.md#the-panel-of-the-runs), que muestra lo mismo a cada usuario para sus propios
libros): el usuario, el libro, qué hace, en qué punto está (lo que indica el panel: *Esperando al modelo*, *El modelo está pensando/escribiendo...*, *Trabajando*, *Espera
a ti*, y, en una generación, su etapa, como *INFERENCE* mientras escribe su parte; la ventana de un libro abierto no muestra nada ahí), desde cuándo y cuántos objetos retiene (el libro, las partes). La ventana de un libro abierto también es una ejecución. **Detener**
detiene la ejecución como lo haría su usuario. **Terminar**, tras una pregunta, termina una ejecución sin detener su trabajo: lo que retenía se libera
de inmediato. Úsalo solo para una ejecución que nunca acaba (su trabajo se quedó atascado y retiene el libro hasta un reinicio); su trabajo aún podría escribir
cuando se despierte.

<a id="the-log-the-jobs-tab"></a>

## El registro: la pestaña Tareas

![La pestaña Tareas de la página de Administración](../../images/admin-jobs.png)

| | |
|---|---|
| **Usuario, Tipo, Estado** | Filtros. El *tipo* es lo que es la tarea (`generation`, `summary`, `annotate`, `agent`, `audiobook`, `image-generator`...); una petición hecha por código que no indicó su trabajo es `untagged`. |
| **Anteriores a (días)** | Solo las tareas que empezaron hace esos días o antes. |
| **Actualizar** | Vuelve a leer el registro. |
| **Eliminar estas tareas** | Borra para siempre las tareas que muestra el filtro y sus llamadas, tras una pregunta que indica cuántas son. Sin ningún filtro, borra todo el registro. |
| **Abrir la vista en directo** | Abre `/jobs` en una pestaña nueva. |

Haz clic en una fila para ver las llamadas de la tarea: el tipo (modelo de lenguaje, audio, imagen), la IA o servidor, el modelo, inicio, fin, duración,
estado y el error de una llamada fallida. Una tarea que aparece como fallida hizo una llamada que falló, o la terminó como fallida el código que la ejecutaba;
una llamada cancelada es una que detuviste (o que seguía en marcha cuando terminó su tarea).

![Las llamadas de una conversión a audiolibro](../../images/admin-jobs-calls.png)

Una tarea fallida muestra qué llamada falló y por qué:

![Una conversión fallida con el error de su llamada de voz](../../images/admin-jobs-failed.png)

**Nada se borra por sí solo.** El registro crece con cada petición, así que límpialo de vez en cuando: indica *Anteriores a (días)*
(por ejemplo 30), revisa lo que aparece en la lista y elimina. La [limpieza](cleanup.md) borra las tareas de un usuario junto con el usuario.

![Las tareas anteriores a 30 días, y la pregunta antes de que se borren](../../images/admin-jobs-old.png)

![La pregunta antes de que se borren las tareas](../../images/admin-jobs-delete.png)

Una tarea se escribe en el registro cuando termina; una tarea que sigue en marcha solo está en `/jobs`. Si la aplicación se detiene, las tareas
que estaban en marcha se pierden (nunca llegaron a terminar). Una tarea que nadie terminó y que no hace ninguna llamada durante media hora se da por completada.
