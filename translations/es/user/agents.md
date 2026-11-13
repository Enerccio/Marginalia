---
label: Agentes (experimental)
order: 958
icon: cpu
---

# Agentes (experimental)

!!!warning Experimental
Los agentes son nuevos en la 1.4.0 y se han usado poco con modelos reales. Espera asperezas: llamadas a herramientas que un modelo débil hace mal,
ejecuciones que consumen más tokens de lo que pensabas, una pantalla de revisión que da más trabajo del necesario y un comportamiento que cambia
entre versiones. Tu libro está protegido (el agente trabaja sobre una copia, el resultado se revisa y se hace una copia de seguridad
antes de aplicarlo), pero **guarda tu propia copia de seguridad de un libro antes de aceptar el resultado de un agente**, define un presupuesto de tokens y vigila
las primeras ejecuciones en la [consola](#the-console). Los informes sobre lo que salió mal son bienvenidos.
!!!

Un **agente** es un modelo que trabaja por su cuenta sobre uno de tus libros, durante mucho tiempo, llamando a los [comandos](commands/index.md)
que le permitas como sus herramientas: puede leer la historia, comprobarla frente al libro de lore e informarte. La pestaña **Agentes** es
donde los creas, defines qué puede hacer cada uno, inicias y detienes sus ejecuciones, hablas con ellos en la [consola](#the-console) y los clonas.

**Iniciar** pone en marcha el agente: trabaja en segundo plano, una ronda cada vez, llamando a los comandos que permitiste y a sus propias herramientas de archivos, hasta
que termina, te pregunta algo, alcanza un límite o lo pausas o detienes (ver [Una ejecución](#a-run)). Trabaja sobre una [copia](#the-working-copy) oculta
del libro, así que tu libro nunca se toca mientras trabaja (el libro en sí está [bloqueado](#the-book-is-locked)); cuando termina,
[revisas su resultado](#reviewing-the-result) y aceptas lo que quieras.

La pestaña solo aparece si un administrador te dio el [permiso](administration/permissions.md) **Agentes** (los administradores siempre
lo tienen, pero los límites son los mismos que para todos: los valores predeterminados, salvo que completen la configuración de su propio permiso; así que un administrador que necesite más agentes, ejecuciones o espacio en el espacio de trabajo lo define allí; un límite cambiado llega a una ejecución que está en marcha cuando se pausa y se reanuda). En un teléfono, cada agente es una tarjeta con los mismos botones, ver [En un teléfono](#on-a-phone).

<a id="the-table"></a>

## La tabla

| Columna | |
|---|---|
| **ID del agente** | El identificador del agente. |
| **Nombre** | El nombre que le diste. |
| **Libro** | El libro sobre el que trabaja el agente. |
| **Estado** | Lo que está haciendo su ejecución: *En ejecución*, *Te espera*, *Espera aprobación*, *Pausándose*, *En pausa*, *Deteniéndose*, *Detenida, resultado conservado*, *Terminada, espera la revisión*, *Aplicada*. Un agente sin ejecución dice *No se está ejecutando*, o *Desactivado* cuando está apagado. |
| **Última ejecución** | Cuándo empezó la ejecución más reciente del agente; *nunca* para un agente sin ejecución. |

![La pestaña Agentes](../images/agents-tab.png)

Encima de la tabla, dos campos filtran la lista: **Nombre** y **Libro** muestran solo los agentes cuyo nombre, o cuyo libro, contiene todas las palabras que escribas, en cualquier orden y sin distinguir mayúsculas. Los encabezados **Nombre**, **Libro**, **Estado** y **Última ejecución** ordenan la tabla (púlsalos de nuevo para invertir el orden; los agentes que nunca se ejecutaron aparecen primero cuando la última ejecución está en orden ascendente). En un teléfono, un selector **Ordenar por** encima de las tarjetas las ordena por nombre, por estado o por la ejecución más reciente primero.

Los botones al final de una fila dependen de la ejecución:

| Botón | |
|---|---|
| **Iniciar** | Abre un menú. **Iniciar de inmediato**: el agente trabaja en su tarea (sus instrucciones) por su cuenta y te avisa cuando pregunta o termina. **Iniciar en la consola**: abre la [consola](#the-console) y le indica al agente que no trabaje todavía, sino que te pregunte de inmediato qué quieres que haga; la ejecución es entonces una [conversación](#a-run) que continúa hasta que pulsas **Finalizar**. |
| **Reanudar** | Continúa una ejecución pausada o detenida (aparece en lugar de **Iniciar**). Pide una nota opcional para el agente, ver [Reanudar una ejecución](#resuming-a-run). |
| **Pausar** | Pausa una ejecución que trabaja o te espera. |
| **Pausar ahora** | Aparece mientras el agente trabaja: lo pausa sin esperar a que termine la ronda, ver [Pausar y detener](#a-run). |
| **Consola** | Abre la [consola](#the-console) del agente. |
| **Detener** | Pregunta si **conservar el resultado** (la ejecución puede reanudarse más tarde) o **descartarlo**. |
| **Editar** | Abre el agente. |
| **Clonar** | Copia el agente, ver [Clonar](#cloning). |
| **Eliminar** | Envía el agente, con sus ejecuciones, a la [papelera](trash.md). |

Iniciar se rechaza, indicando el motivo, cuando el agente está desactivado, no tiene modelo o ya tienes todas las ejecuciones que puedes tener a la vez.

![El menú del botón Iniciar](../images/agent-start-menu.png)

Un agente tiene como máximo una ejecución que no ha terminado, y un libro tiene como máximo un agente.
<a id="a-run"></a>

## Una ejecución

Una ejecución es el trabajo del agente. Trabaja por rondas: se le pregunta al modelo una vez, responde con texto y/o con llamadas a sus herramientas, los resultados vuelven al modelo, y así sucesivamente.
Cada ronda queda guardada, de modo que una ejecución puede pausarse y continuar más tarde, incluso después de reiniciar el servidor. El agente dispone de los comandos que hayas permitido y de estos propios:

| Herramienta | |
|---|---|
| Archivos | `list_files`, `read_file`, `write_file`, `append_file`, `replace_in_file`, `regex_replace_in_file`, `grep_files`, `move_file`, `copy_file`, `merge_files`, `unzip_file`, `delete_file` en su propio espacio de trabajo (notas que conserva entre ejecuciones). Junto al espacio de trabajo ve el archivo de solo lectura `/AGENTS.md`, tus instrucciones tal como estaban cuando empezó la ejecución, que puede volver a leer cuando quiera y que nunca puede cambiar. `list_files` y `grep_files` aceptan un patrón de nombre (`glob`, por ejemplo `*.md` o `chapters/*.md`), `copy_file` copia una carpeta con todo lo que contiene (una forma rápida de guardar una versión de las notas), `merge_files` une varios archivos en uno sin que el texto pase por el modelo (el último nombre de una ruta puede ser un patrón como `/chapters/*.md`), `unzip_file` descomprime en una carpeta nueva un [archivo zip](#unpacking-a-zip-file) del espacio de trabajo. `replace_in_file` y `regex_replace_in_file` también aceptan una **carpeta** como ruta: cambian cada aparición en todos los archivos de texto que contiene (solo los archivos cuyo nombre coincide con `glob`, si se indica) e informan `path: n replacements` para cada archivo. Eso es una **simulación** (no se escribe nada, el informe dice qué cambiaría) salvo que el agente pase `dry_run` como false; es todo o nada (si un archivo no puede escribirse, no se cambia ninguno) y se rechaza, sin cambiar nada, cuando más de `max_files` (100 salvo que el agente indique más) archivos cambiarían. Un solo archivo también puede someterse a una simulación. `list_command_results` y `save_command_result` conservan el resultado largo de un comando sin que el agente tenga que volver a escribirlo: los últimos diez resultados se guardan completos (el agente ve cortado uno largo), y uno se guarda en un archivo por su número, el más reciente si no se indica ninguno; después pasa el archivo como `@file:/path`. |
| `@file:/path` | No es una herramienta sino una forma de llamar a una: un argumento de texto de un comando que sea exactamente `@file:/notes/chapter.md` se sustituye por el contenido de ese archivo del espacio de trabajo del agente, de modo que el agente puede escribir un texto largo en un archivo una vez, revisarlo y pasarlo a un comando (el resultado muestra la referencia corta en la llamada, y el comando recibe el texto). `write_file` y `append_file` también lo aceptan como contenido, lo que copia un archivo o lo añade a otro: un archivo largo puede escribirse por partes, cada parte dentro de la respuesta más larga del modelo, y unirse de este modo. |
| `@result` | Como `@file:`, pero para el resultado de un comando: un argumento de texto que sea exactamente `@result` se sustituye por el resultado completo del último comando que llamó el agente, y `@result:3` por el resultado con ese número (`list_command_results` muestra los números). Un texto generado pasa de un comando al siguiente, o a `write_file` y `append_file`, sin que el agente tenga que escribirlo de nuevo. |
| `search_manual`, `read_manual_page` | Buscan algo en el manual de usuario de Marginalia (las mismas páginas que lee el asistente de ayuda): cómo funciona una plantilla de prompt o una macro, qué hace un ajuste de un lorebook. Solo lectura; todos los agentes las tienen. |
| `view_image` | Mira un archivo de imagen de su espacio de trabajo (PNG, JPEG, GIF o WebP): la imagen se muestra al modelo en el siguiente mensaje. Solo lo tiene un agente cuyo modelo está marcado como capaz de ver imágenes (**El modelo ve imágenes** en el agente). Una imagen grande se reduce antes de enviarse, un GIF muestra su primer fotograma, y solo las cuatro imágenes más recientes permanecen delante del modelo (una más antigua se convierte en una nota, y el agente puede volver a mirarla). Las imágenes vienen de ti (**Subir archivo**, o [adjuntas a un mensaje](#the-console)); el agente no puede crear ninguna. |
| Imágenes para comandos | Un comando que envía imágenes a un modelo propio, como [`generate-lore-entry`](extensions/lore-generator.md#commands), acepta un parámetro `images`: una lista de rutas de archivos de imagen del espacio de trabajo (como máximo 8). Es el modelo del comando, no el del agente, el que tiene que verlas, así que el agente puede pasar imágenes incluso cuando su propio modelo no ve imágenes. |
| `update_plan` | Su lista de tareas pendientes. |
| `report` | Un hallazgo para ti (qué encontró, dónde, y su gravedad: *Nota*, *Advertencia* o *Problema*). El agente sigue adelante después de llamarla; lees los hallazgos en la [consola](#the-console) y en la [revisión](#reviewing-the-result), aparte de su informe final. Un hallazgo por llamada, como máximo 200 por ejecución. |
| `get_run_status` | Cuántas rondas y tokens ha usado, cuánto contexto tiene, y la respuesta más larga que puede escribir; su plan; y sus subagentes: una línea para cada uno con su estado, ronda, tokens, lo último que hizo y su tarea. El agente también conoce estos límites de su modelo por su prompt, así que escribe un texto largo por partes. |
| `checkpoint`, `checkpoint_list`, `checkpoint_diff`, `checkpoint_restore`, `checkpoint_delete` | Sus [puntos de control](#checkpoints): guarda un punto de restauración de la copia de trabajo y de su espacio de trabajo (con una etiqueta), los lista, muestra qué ha cambiado desde uno (los ajustes, partes, lorebooks y entradas del libro que se añadieron, cambiaron o eliminaron, y los archivos del espacio de trabajo; con una ruta, solo ese archivo o carpeta, y para un archivo de texto las frases que difieren), vuelve a uno (cuando termina la ronda) y elimina uno que ya no necesita. |
| `get_sync_report` | Solo después de una reanudación que incorporó cambios tuyos: vuelve a listar lo que cambiaste en el libro mientras la ejecución estaba en pausa y qué de ello llegó a la copia del agente, ver [Reanudar una ejecución](#resuming-a-run). |
| `request_compaction` | Compacta su propio contexto: cuando termina la ronda escribe el resumen de su trabajo anterior, como con `/compact`. Se le indica que guarde antes sus notas en un archivo. Ver [Se llena el contexto](#running-a-command-yourself). |
| `ask_user` | Una pregunta para ti. La ejecución espera (*Te espera*). |
| `finish` | Su informe final: la ejecución queda en *Hecho*. El informe se muestra como motivo del estado. |

Así termina un tramo de trabajo una ejecución:

- **Te espera:** el agente respondió sin llamar a ninguna herramienta, o te hizo una pregunta. Contéstale en la [consola](#the-console).
- **Hecho:** el agente llamó a `finish` (en una ejecución que iniciaste *inmediatamente*). Una ejecución que iniciaste *en la consola* es una conversación: el informe del agente te llega como una pregunta, la ejecución espera tu siguiente mensaje, y solo **Finalizar** en la consola (o **Detener**) la termina. El resultado de una ejecución *Hecha* espera tu [revisión](#reviewing-the-result).
- **En pausa**, con el motivo: se alcanzó el límite de rondas, el presupuesto de tokens o el límite de contexto, el modelo no pudo ser consultado (un error del proveedor, tu límite en la IA), o se reinició el servidor. **Reanudar** continúa desde la última ronda completa, ver [Cuando el servidor se detiene](#when-the-server-stops).
- **Fallida:** el ejecutor en sí falló; el motivo indica qué pasó.

**Pausar** surte efecto entre dos rondas: primero terminan la petición al modelo y las llamadas que están en curso. Eso puede tardar mucho (un modelo lento, una generación larga), así que existe **Pausar ahora**: cancela la petición al modelo, cuya respuesta se descarta, y la llamada que está en curso responde a las llamadas de la ronda que no se hicieron como tales, y la ejecución pasa a *En pausa* en un momento. Lo que habría costado la petición descartada se pierde; reanuda como de costumbre. **Detener** hace lo mismo: no espera a la petición ni a la llamada en curso, se cancelan y la ejecución termina al instante (se conserva o se descarta según elijas).

![La pregunta cuando se detiene una ejecución](../images/agent-stop.png)

A un comando en curso se le indica que se detenga, y un comando diseñado para poder detenerse (los que escriben con un modelo, buscan y reemplazan) termina al instante y deja lo que había hecho hasta ese momento. Uno que no puede detenerse puede terminar igualmente; en cualquier caso, al agente se le indica que no se sabe si la llamada llegó a hacerse, y comprueba el estado con un comando de lectura cuando continúa. Los comandos de los subagentes se cancelan igual cuando la ejecución se pausa, se detiene o recibe un mensaje de inmediato.

Mientras el agente espera un lugar libre en su modelo (ver [Un modelo, varios agentes](#one-model-several-agents)) está *En ejecución*; **Pausar** o **Detener** terminan esa espera al instante.
<a id="the-console"></a>

## La consola

**Consola** abre una ventana para la última ejecución del agente, con ocho pestañas: **Ejecución**, **Ejecuciones**, **Espacio de trabajo**, **Puntos de control**, **Contexto**, **Copia del libro**, **Subagentes** y **Diagnóstico**.

![Una ejecución iniciada en la consola: el agente pregunta qué hacer](../images/agent-console-start.png)

**Ejecución** muestra la *transcripción*: lo que le dijiste al agente, lo que respondió (ambos en [Markdown](https://commonmark.org/help/), así que las listas, los encabezados y el énfasis se ven como tales), y cada comando o herramienta de archivos que llamó, plegado en una sola línea con los argumentos (haz clic para ver los argumentos y el resultado, mostrado como texto plano). Cuando el modelo envía su **razonamiento** (lo que pensó antes de responder), aparece un bloque plegado *Razonamiento* antes de la respuesta, y la descarga lo incluye. Que recibas razonamiento depende del modelo y del servidor: el modelo tiene que informarlo, y tu IA tiene que tenerlo activado. Mientras una petición está en curso, la consola muestra lo que el modelo ha escrito hasta ahora (su razonamiento, abierto para que puedas seguirlo, y su texto) en un bloque marcado *Trabajando...* al final de la transcripción, por encima de los mensajes que esperan; se sustituye por los bloques reales cuando termina la ronda. Mientras se muestra, la consola lee la ejecución dos veces por segundo; en caso contrario, cada dos segundos. Esto solo ocurre con el propio agente, no con sus subagentes, y lo que el modelo escribe dentro de una llamada (un archivo largo, por ejemplo) aparece solo cuando termina la ronda. Cada mensaje tiene a la derecha de su primera línea la hora en que se escribió (la hora del día, con la fecha si era otro día; pasa el ratón para ver la hora completa), para que sepas cuánto tiempo trabajó el agente y cuándo se detuvo. El estado de la ejecución, el **contexto** (cuántos tokens usó la última petición del límite del modelo, para que veas lo cerca que está la ejecución de una pausa), las rondas y los tokens usados (y el presupuesto de tokens, cuando lo hay) se mantienen encima de la transcripción, y el cuadro para tu mensaje debajo, mientras la transcripción se desplaza. Lo que el agente espera aparece en la transcripción donde se escribió, como una burbuja propia que permanece allí cuando la ejecución continúa:

![La consola mientras el agente trabaja: un mensaje del autor espera en la cola](../images/agent-console-run.png)

- una **Pregunta del agente**, cuando la ejecución está *Te espera*; escribe la respuesta en el cuadro y pulsa **Enviar** (o **Ctrl+Enter**), y el agente continúa;
- un **Informe del agente**, cuando ha llamado a `finish`.

![La pregunta de un agente, con las respuestas que sugiere](../images/agent-console-question.png)

![El informe del agente al final de una conversación](../images/agent-console-report.png)

![La pregunta de un agente, con las respuestas que sugiere](../images/agent-console-question.png)

![El informe del agente al final de una conversación](../images/agent-console-report.png)

La línea sobre la transcripción muestra el estado de la ejecución y sus cifras: las rondas, el tamaño del contexto frente a su tope, el **Límite de respuesta** (el máximo de tokens que el modelo del agente puede escribir en una respuesta; una llamada que necesite más se corta, así que el agente escribe los textos largos por partes) y los tokens en total. Cuando el agente ha hecho un plan (`update_plan`), aparece ahí un botón **Plan** con los pasos hechos sobre el total (**Plan 2/5**); abre los pasos en un menú emergente, cada uno con una marca de *Hecho*, *En curso* o *Pendiente*. Cuando ha informado de hallazgos (`report`), aparece un botón **Hallazgos** con su número (**Hallazgos 3**), que los abre de la misma forma, cada uno con su gravedad. Los hallazgos se guardan con la ejecución, así que siguen ahí tras una pausa, un reinicio y cuando consultas una ejecución anterior en el historial; el archivo Markdown de una ejecución (**Descargar**) los incluye en una sección **Hallazgos**.

![El plan del agente](../images/agent-console-plan.png)

![Los hallazgos del agente](../images/agent-console-findings.png)

En una conversación también puedes escribir mientras el agente **trabaja**. El mensaje se **pone en cola**: aparece como una burbuja con borde discontinuo al final de la transcripción, y el agente lo lee en cuanto termina la ronda en la que está: la petición al modelo que está en camino y las llamadas que hizo terminan antes, y la siguiente petición ya contiene tu mensaje (varios mensajes en cola viajan juntos como uno). Cuando no debe esperar ni siquiera a eso, pulsa el pequeño botón **Enviar ahora** (con forma de rayo) de la burbuja: el agente descarta la ronda en la que está (la petición al modelo se cancela, un comando que esperaba tu aprobación se rechaza, las llamadas que no se hicieron se marcan como omitidas) y lee tu mensaje a continuación. El botón de la **papelera** que está junto a él retira el mensaje mientras el agente no lo haya leído (lo mismo para un comando en cola). Los comandos que ya se ejecutaron siguen hechos. Los mensajes y comandos en cola se conservan tras un reinicio del servidor (se leen al reanudar), y una ejecución que no es una conversación (iniciada *inmediatamente*) no acepta mensajes mientras trabaja.
<a id="running-a-command-yourself"></a>

### Ejecutar un comando tú mismo

Debajo de la transcripción está la línea de comandos de la aplicación (la de la pestaña de la historia: escribe `/` y encima aparecen la lista y la ayuda de los comandos), con lo que puedes ejecutar sobre el agente: `/compact`, `/clear-workspace` y los [comandos que el agente puede invocar](#allowed-commands) y que solo leen o añaden datos (un comando que cambia o elimina datos no se ofrece, porque pediría que lo confirmaras en una ventana del libro, y una ejecución no tiene ventana). Tú ejecutas el comando, no el agente: se le comunica, en un mensaje tuyo, que lo has ejecutado y qué ha respondido (y que lo que leyó antes puede estar desactualizado si el comando cambió la copia de trabajo), así que nunca cree que hizo una llamada que no hizo. Funciona como un mensaje: cuando la ejecución espera (*Te espera*) o está *En pausa*, el comando se ejecuta al momento y la ejecución sigue como estaba; cuando hay una [conversación](#the-console) en curso, el comando se pone en cola, en una burbuja discontinua marcada como *Comando*, y se ejecuta al final del turno del agente, donde el agente habría esperado (el agente no continúa por ello). El pequeño botón **Ejecutar ahora** de la burbuja descarta la ronda en la que está el agente, ejecuta el comando y deja que el agente continúe desde ahí. Un comando que tarda mucho mantiene ocupada la caja: espera a que termine antes de escribir al agente.

![Un comando del autor en la consola: /compact](../images/agent-console-command.png)

**`/compact [qué destacar]`** libera el contexto. Se pide al agente que lea todo lo anterior y escriba un resumen (qué pediste, qué está hecho, qué encontró, sus notas y su plan, lo que iba a hacer a continuación); ese resumen sustituye a todo lo anterior en lo que lee el modelo. En la transcripción sigues viéndolo todo, y el resumen como un bloque plegado, **Resumen del trabajo anterior**; la exportación también lo conserva. El texto que va después del comando, si lo hay, es lo que el resumen debe destacar. La petición es propia del agente y sus tokens cuentan para la ejecución. La pregunta que se le hace al agente es su **Prompt de compactación**: el del agente, si no, tu predeterminado en [Ajustes](account.md#templates), si no, el del administrador, si no, el integrado. Si el contexto ya está tan lleno que la petición no cabe, se dejan fuera los mensajes más antiguos (el resumen indica cuántos). Hazlo antes de que el contexto esté lleno; la línea de uso indica lo cerca que está. Justo después de la compactación, el contexto que se muestra es una estimación de lo que tendrá la siguiente petición (el resumen, el prompt del sistema y las definiciones de las herramientas); la primera respuesta del modelo tras ella lo sustituye por el número real.

**Contexto que se llena.** Cuando el contexto del modelo supera el 75 % de su límite, el agente recibe un aviso después de cada ronda (un mensaje de la ejecución, que ves en la transcripción): cuánto está lleno, y que debe guardar en un archivo de su espacio de trabajo lo que no pueda perder, actualizar su plan y llamar a `request_compaction`. Por encima del 90 %, el aviso dice que la ejecución se detiene cuando el contexto esté lleno. Si el agente no compacta antes de que el contexto supere la parte **Compactar en** de su definición (90 % salvo que indiques otra cosa), la ejecución lo hace por sí sola: la misma compactación, el resumen empieza por "compactado automáticamente" y no se avisa además al agente. Una compactación que falla no cambia nada, se le comunica al agente, y la ejecución lo vuelve a intentar solo algunas rondas después. La compactación que pide el agente es la misma que la tuya (su **Prompt de compactación**, véase [Crear y editar un agente](#creating-and-editing-an-agent); sus tokens cuentan para la ejecución); si falla, no se cambia nada y se avisa al agente. Los avisos necesitan un límite de contexto: el menor entre el límite del agente y los límites de su modelo.

**Una llamada que el servidor del modelo no pudo leer.** Cuando un modelo escribe un texto largo dentro de una llamada, su respuesta puede terminar a mitad del texto (se alcanza el límite de *una respuesta*, los **reply tokens** del protocolo o los **max completion tokens** de la IA, se aplica un valor por defecto del servidor cuando no se fija ninguno, o el modelo simplemente se detiene; el contexto, por lleno que esté, es otro límite), y el servidor rechaza la llamada incompleta ("Failed to parse tool call arguments as JSON"). Volver a preguntar con la misma conversación cometería el mismo error, así que la ejecución no se limita a pausarse: se le indica al agente en la transcripción que su última respuesta no pudo usarse y que no se hizo nada, y que escriba los textos largos en un archivo de su espacio de trabajo en varios pasos más pequeños; después se le vuelve a preguntar, hasta tres veces seguidas. Si sigue fallando, la ejecución se pausa, y lo que se le dijo queda en la transcripción, de modo que un **Reanudar** hace una pregunta distinta a la que falló. Lo mismo ocurre con un subagente.

Una llamada que se cortó pero llegó a la aplicación (el resultado de la llamada dice "not valid JSON ... cut off in the middle") queda en la transcripción tal como la escribió el modelo, para que la puedas leer, pero se devuelve al modelo con los argumentos vacíos: un servidor que vuelve a leer las llamadas anteriores rechazaría todas las peticiones siguientes, y la ejecución no podría continuar sin un retroceso.

**Llamadas descontroladas.** Un modelo puede caer en un bucle y escribir la misma llamada cientos de veces en una sola respuesta; los resultados llenarían el contexto antes de la siguiente petición. La ejecución hace como máximo 32 llamadas por respuesta, y una repetición de una llamada que solo lee (la misma herramienta con los mismos argumentos, en la misma respuesta) se hace una sola vez. Las llamadas que se dejan fuera no aparecen en la transcripción, y el agente recibe un aviso que lo indica.

**Dar vueltas.** Un modelo también puede repetirse a lo largo de muchas respuestas: la misma consulta (listar archivos, listar libros de referencia...) con el mismo resultado, ronda tras ronda, sin avanzar. La ejecución lo vigila. Cuando el agente hace rondas que solo consultan, no cambian nada y son iguales a otras anteriores (mismas llamadas, mismos resultados), se le indica en la tercera que nada ha cambiado. Si sigue, en la quinta se le quitan sus herramientas durante una petición: tiene que responder en texto plano qué estaba haciendo y qué lo bloquea, y la ejecución se **pausa** con esa respuesta como último mensaje, para que veas por qué se atascó en lugar de pagar cien rondas más. **Reanudar** (o escribirle) le devuelve las herramientas y empieza a contar de nuevo. Una ronda en la que algo cambió, o mientras trabajan subagentes, no se cuenta. Un subagente que se repite se detiene de la misma forma, y lo que dijo es su respuesta al agente que lo inició.

**`/clear-workspace [qué conservar]`** borra los archivos y todas las carpetas del [espacio de trabajo](#the-working-copy) de la ejecución (tras confirmar, como con el botón del espacio de trabajo). Indica después qué conservar, separado por espacios: archivos, carpetas (con todo lo que contienen) o patrones como `*.md` o `chapters/*.md`, una ruta con espacios entre comillas dobles (`/clear-workspace /notes.md "/my drafts" *.png`). Las carpetas por encima de un archivo conservado se mantienen. Si algo indicado no existe, no se borra nada. `/AGENTS.md` y los puntos de control no forman parte del espacio de trabajo y nunca se tocan. Al agente se le indica qué se borró y qué se conservó. En la transcripción, un comando que ejecutaste es una burbuja con el comando y lo que respondió.

Cuando el modelo del agente ve imágenes (véase [Crear y editar un agente](#creating-and-editing-an-agent)), **Adjuntar imagen** está junto a **Enviar**: elige un archivo PNG, JPEG, GIF o WebP, o pega una imagen del portapapeles en la caja de mensajes (**Ctrl+V**), y espera encima de la caja (la cruz la quita) hasta que lo envías. Se pueden adjuntar hasta ocho imágenes a un mensaje; el texto puede estar vacío. Las imágenes se guardan en la carpeta **uploads** del espacio de trabajo (un nombre ya usado recibe un número: `map-2.png`), dentro de los límites y reglas del espacio de trabajo, y al modelo se le muestran junto con tu mensaje. Elimínalas en la pestaña **Espacio de trabajo** cuando ya no las necesites.

Los botones de la parte inferior son **Pausar**, **Reanudar** y **Detener**, que hacen lo que indica la tabla, y **Finalizar**, que termina una ejecución que te espera (su trabajo ha terminado, *Hecho*).

La ventana mira la ejecución cada dos segundos mientras está abierta; **Actualizar** lo hace de inmediato. La pestaña **Ejecución** muestra la última ejecución que no se descartó, de modo que una ejecución terminada o fallida se puede leer allí hasta que el agente inicie una nueva; las anteriores están en **Ejecuciones**.

**Ejecuciones** es el historial del agente: todas las ejecuciones que ha tenido, la más reciente primero, con su fecha, estado, rondas, tokens y resultado. Usa el ojo para **abrir** una ejecución en la pestaña **Ejecución** y volver a leer sus pasos y su informe (la ejecución que abres se queda allí hasta que abras otra), la flecha para **descargar** su transcripción como archivo Markdown (lo que se dijo y cuándo, cada comando con sus argumentos y resultado, el informe, las conversaciones de los subagentes), y la papelera para **eliminar** una ejecución. Una ejecución eliminada va a la [papelera](trash.md). No se puede eliminar una ejecución que sigue en marcha; deténla antes. Las ejecuciones que detuviste y **descartaste** también aparecen en la lista, para que puedas leer lo que hicieron o eliminarlas; la pestaña **Ejecución** solo las muestra cuando las abres.

![La pestaña Ejecuciones](../images/agent-runs.png)

**Espacio de trabajo** muestra los archivos del agente: sus notas e informes, que conserva entre ejecuciones. Haz clic en una carpeta para entrar (**Subir** vuelve atrás), haz clic en un archivo de texto para leerlo, usa las flechas para descargar un archivo o la papelera para eliminarlo, y **Vaciar** borra todo (las notas obsoletas confunden a una ejecución nueva). **Nueva carpeta** crea una carpeta en la abierta y **Subir archivo** coloca allí un archivo de tu ordenador, para que puedas entregar al agente un encargo, una guía de estilo o un borrador. Valen las mismas reglas que para los archivos propios del agente: los nombres no pueden contener `\ < > : " | ? *`, ni terminar en punto o espacio, ni ser demasiado largos o estar demasiado anidados, y se aplican los límites de tamaño de archivo y de número de archivos; un archivo subido debe ser de texto (UTF-8), porque el agente solo puede leer texto, una imagen (PNG, JPEG, GIF o WebP, reconocida por lo que contiene el archivo, no por su nombre; hasta 5 MB, y cuenta contra el total y el número de archivos como cualquier otro) o un archivo zip (hasta 10 MB, que se guarda tal cual; el agente lo descomprime, véase [más abajo](#unpacking-a-zip-file)). Un archivo nunca se reemplaza al subir: elimina antes el antiguo. Los archivos no se pueden modificar mientras el agente trabaja; pausa antes al agente. El espacio de trabajo tiene un límite de tamaño, de tamaño de archivo y de número de archivos, que tu administrador puede fijar en tu [permiso](administration/permissions.md#settings-of-a-permission); la línea debajo de la tabla indica cuánto se usa. El nombre `AGENTS.md` de arriba lo ocupan tus instrucciones, que el agente ve allí (el archivo no se puede escribir ni eliminar).

![La pestaña Espacio de trabajo](../images/agent-console-workspace.png)
<a id="unpacking-a-zip-file"></a>

### Descomprimir un archivo zip

Sube un archivo zip en la pestaña **Espacio de trabajo** (**Subir archivo**) cuando el agente deba trabajar con muchos archivos a la vez, por ejemplo los capítulos de un borrador, y pídele que lo descomprima. El agente tiene la herramienta `unzip_file`, que descomprime `/uploads/draft.zip` en la carpeta nueva `/uploads/draft` (o en la carpeta que se le indique, que no debe existir todavía). La descompresión es todo o nada, y nunca se confía en un archivo comprimido:

- Solo se conservan los archivos de texto (UTF-8), las imágenes (PNG, JPEG, GIF, WebP) y los archivos zip. Las demás entradas se dejan fuera y se nombran en la respuesta del agente. Un zip dentro se guarda tal cual, nunca se descomprime por sí solo.
- Los archivos deben caber en lo que tiene disponible el espacio de trabajo (tamaño total, tamaño de cada archivo, número de archivos); si no, no se descomprime nada.
- Se rechaza el archivo entero si tiene una entrada que sale de la carpeta (`../x`) o que empieza por `/`, o si tiene dos entradas con el mismo nombre (aunque difieran en mayúsculas o minúsculas), un nombre que es a la vez archivo y carpeta, más de 5 000 entradas, o si está cifrado o dañado.
- Se rechaza una bomba zip: un archivo que se descomprime a más de lo que el espacio de trabajo puede contener, o a más de 100 veces su propio tamaño.

<a id="sub-agents"></a>

## Subagentes

Un agente puede encargar un trabajo a un **subagente**: un ayudante con una conversación propia que realiza una tarea y responde con un texto. Tiene los comandos y los archivos del agente (o los del modelo en el que se ejecuta, ver [abajo](#models-for-sub-agents)) y trabaja en la misma copia de trabajo y en el mismo espacio de trabajo (lo que cambie en el libro se queda), pero no conoce la conversación del agente, así que el agente tiene que decirle todo en la tarea. La ventaja es que lo que el subagente lee y prueba no ocupa el contexto del agente: revisar un capítulo, auditar los nombres, redactar una parte.

Ajusta **Máximo de subagentes** en el [agente](#creating-and-editing-an-agent) para indicar cuántos pueden trabajar a la vez. Cero (el valor por defecto) significa ninguno: al agente ni siquiera se le dice que existen.

- Con **uno**, el agente inicia un subagente y espera (queda *aparcado*) hasta que el subagente termina; la llamada devuelve su respuesta.
- Con **más de uno**, iniciar un subagente devuelve el control de inmediato y el agente puede seguir, iniciar otros, consultarlos (`agent_status`), esperar una respuesta (`await_agent`) o detener uno (`cancel_agent`). La llamada que alcanza el límite espera hasta que termina uno de ellos y devuelve lo que terminó.

Reglas que se cumplen en ambos casos:

- Un subagente **lee todo el espacio de trabajo, pero solo escribe en una carpeta propia**, `/tmp/sub1`, `/tmp/sub2`... (se crea al iniciar el subagente, y lo que dejó una ejecución anterior en una carpeta del mismo nombre se borra). Su respuesta se guarda allí como `result.md`, así que cuando una respuesta larga se muestra cortada, el agente lee el texto completo desde ese archivo. Los archivos se encuentran en la pestaña **Espacio de trabajo**; ocupan parte de la cuota del espacio de trabajo y se mantienen hasta que los borres.
- Un subagente se ejecuta en el modelo principal del agente, salvo que el agente pida otro; ver [Modelos para subagentes](#models-for-sub-agents).
- Un comando de un subagente que modifica datos sigue la opción **Comandos destructivos** del agente: con *Preguntar antes*, la misma [ventana](#asking-you) te pregunta a ti, y ese subagente espera tu respuesta (pausar la ejecución la rechaza).
- Un subagente **no puede iniciar subagentes, terminar la ejecución, preguntarte ni crear puntos de control**; termina respondiendo con un texto.
- Todo cuenta en el **presupuesto único de la ejecución**: rondas, tokens, los límites del modelo. Un subagente que alcanza un límite falla y lo dice; después el agente alcanza ese mismo límite.
- Cuántas peticiones llegan a la vez al modelo lo determina las **Peticiones al modelo a la vez** que te haya asignado tu administrador (una si no está definido): con una, el agente y su subagente se turnan.
- Las llamadas que modifican el libro o el espacio de trabajo se hacen de una en una, lo haga quien lo haga, así que dos subagentes nunca escriben en el mismo instante. Aun así, no les pidas a dos que modifiquen la misma parte.
- **Pausar la ejecución pausa también a los subagentes.** Se detienen en su próximo punto estable (entre dos peticiones o dos llamadas, o de inmediato si pausas en ese momento), y lo que dijeron e hicieron se conserva con la ejecución, así que **Reanudar** continúa justo donde estaba todo: los subagentes siguen con su conversación, y una llamada del agente que esperaba a uno (`await_agent`, o `spawn_agent` con un límite de uno) vuelve a esperar, como si nada hubiera pasado. Un subagente que había terminado antes de la pausa sigue disponible para que el agente le pregunte. Detener una ejecución y conservarla hace lo mismo; descartarla, restaurar un punto de control (el libro en el que trabajaban los subagentes se reemplaza) y que el agente termine o te pregunte algo los finaliza de forma definitiva. Un agente que termina su turno mientras sus subagentes siguen trabajando recibe un aviso una vez; si lo vuelve a hacer, se detienen.
- Sus conversaciones no aparecen en la pestaña **Ejecución**. La pestaña **Subagentes** de la consola las muestra: elige uno arriba para leer lo que el agente le dijo, lo que respondió, los comandos que llamó con sus resultados y cómo terminó (la respuesta que dio, o por qué falló o se canceló), en los mismos bloques que la transcripción. Sigue la ejecución mientras trabaja, y también muestra los subagentes de una ejecución anterior (selecciona antes esa ejecución en la pestaña **Ejecuciones**). Una llamada `spawn_agent` en la pestaña **Ejecución** tiene un botón **Subagente** que abre su conversación. Una conversación a la que el agente volvió atrás (un retroceso) aparece en gris y marcada como *olvidado por el agente*. La **download** de la ejecución (en la pestaña **Ejecuciones**) tiene una sección para cada subagente, con su tarea, su estado y cada mensaje y llamada.

![La pestaña Subagentes](../images/agent-subagents.png)

<a id="models-for-sub-agents"></a>

### Modelos para subagentes

El agente tiene un **modelo principal**, que eliges en la parte superior del diálogo. En **Modelos para subagentes** puedes añadir otros, por ejemplo un modelo barato que solo lee o uno con buena fluidez que redacta. Cada uno tiene un **rol** (su nombre), una **descripción** (cuándo usarlo: el agente la lee para elegir), su IA y protocolo, un **límite de contexto** opcional, y qué puede usar:

![Un modelo para subagentes](../images/agent-models.png)

- **Puede usar comandos y herramientas.** Desactivado, un subagente en este modelo solo responde con texto: no recibe comandos ni herramientas.
- **Limitar los comandos.** Desactivado, un subagente en este modelo tiene los comandos del agente. Activado, solo los que marques, y de esos solo los que el agente tenga permitidos (los comandos permitidos al agente siguen siendo el límite superior). Las herramientas para archivos no son comandos, así que todos los subagentes las tienen. El modelo principal tiene la misma opción, para los subagentes que se ejecutan en él (el botón **Editar** de su fila).

Al agente se le indican los roles, las descripciones y los comandos de cada modelo, e inicia un subagente en uno de ellos con el `profile` de `spawn_agent`; si lo omite, usa su propio modelo. La casilla **Los subagentes pueden usar el modelo principal** (activada por defecto) desactiva esa última opción: el agente debe entonces indicar otro modelo, y si no hay ninguno, no recibe herramientas para subagentes. Un subagente tiene los límites de su propio modelo (cuánto puede leer y cuánto puede durar su respuesta), y la pestaña **Subagentes** y los diagnósticos indican en qué modelo se ejecutó cada uno.

<a id="one-model-several-agents"></a>

## Un modelo, varios agentes

Un servidor local suele tener una sola ranura (una GPU): si le pides dos respuestas a la vez, ambas avanzan muy despacio o se agotan por tiempo. Las **Conexiones** del [modelo](inference-providers.md#general-settings) (1, salvo que indiques más) hacen que cada petición a él se turne, en el orden en que llegaron: las de tus agentes, las de los agentes de otros usuarios y tu propia escritura en la ventana de la historia. Una ejecución que espera sigue en *En ejecución*, y **Pausar** o **Detener** interrumpen la espera de inmediato. Entre dos respuestas del modelo, un agente no mantiene ninguna conexión, así que tu generación puede colarse en medio.
<a id="when-you-are-not-there"></a>

## Cuando no estás

Un agente que te necesita, o que termina o falla, avisa a las pantallas abiertas de la aplicación (la insignia de la pestaña, un aviso). Cuando no hay abierta ninguna pantalla tuya, también puede enviar un mensaje breve a una dirección que indiques en [Ajustes](account.md), si un administrador te dio el [permiso *Notificaciones*](administration/permissions.md) (los administradores lo tienen) ("Mensaje a una URL cuando un agente te necesita"): tu tema de **ntfy** (la app de ntfy en el teléfono lo muestra al momento), una URL de mensajes de **Gotify** o un webhook de **Discord**; elige el **Formato** que quiere el servicio. **Enviar un mensaje de prueba** lo intenta con lo que haya en los campos. El mensaje nombra al agente y dice lo que quiere ("espera por ti", "ha terminado", "pide la aprobación del comando ...", "ha fallado", "se ha pausado: motivo") en tu idioma. **También cuando la aplicación está abierta** lo envía en cualquier caso.

El servidor llama a la dirección, así que un administrador puede desactivar esto para todos (Admin, *Agentes*), y solo se llama a direcciones de internet a menos que el administrador también haya marcado **Permitir direcciones de esta máquina y de redes privadas** en tu permiso: para un ntfy o Gotify en la misma máquina o en tu red propia, pídelo. El mensaje de prueba indica cuando la dirección es rechazada. Trata la dirección como una contraseña cuando contiene un secreto: quien la conozca puede leer lo que se envía, el servidor nunca la escribe en su registro, no se siguen las redirecciones y solo se llama a `http` y `https`.

<a id="asking-you"></a>

## Cuando te preguntan

Cuando el agente te necesita, se te avisa estés donde estés en la app:

- un **número en la pestaña Agentes** cuenta los agentes que esperan por ti, y el botón **Consola** de ese agente se resalta;
- aparece un **aviso** con un botón **Abrir** que abre la consola de ese agente.

El agente te necesita cuando *te pregunta algo o espera tu siguiente mensaje*, te pide **aprobar un comando**, *ha terminado*, *ha fallado*, o se *pausó por sí mismo* (se alcanza un límite, no se pudo preguntar al modelo, el servidor se reinició). Una pausa que pediste tú no cuenta.

Los agentes trabajan mientras estás fuera, así que esto no depende de que la aplicación esté abierta: el número sigue ahí cuando vuelves a entrar, y cada ventana del navegador que tengas abierta recibe el aviso. Abrir la consola del agente lo borra (las otras ventanas se actualizan).

Cuando el agente está en **Preguntar antes** y quiere un comando que cambia o elimina datos, la ejecución se detiene en *Espera aprobación* y aparece una ventana sobre la consola con el comando y sus argumentos. **Aprobar** lo deja ejecutarse; **Denegar** le dice al agente que lo rechazaste y sigue adelante. Pausar o detener la ejecución (o detener el servidor) no cuenta como denegación: al agente no se le dice nada, y cuando **Reanudar**, la ventana vuelve a aparecer para el mismo comando. El comando sigue funcionando sobre la [copia](#the-working-copy) del libro. Si la consola está cerrada, la ventana está ahí en cuanto la abres.

![La ventana que pide la aprobación de un comando](../images/agent-approval.png)

<a id="when-the-server-stops"></a>

## Cuando el servidor se detiene

Detener el servidor pausa primero las ejecuciones que están trabajando, como hace **Pausar ahora** (le da al servidor unos segundos): la petición y la llamada en curso se abandonan, la ejecución queda *En pausa* con el motivo "El servidor se ha detenido", y se puede **Reanudar**. Si el servidor se mata o se cae, el siguiente arranque pausa las ejecuciones que encuentre trabajando ("Interrumpida por un reinicio del servidor"). Todo lo que hizo la ejecución se conserva: las rondas guardadas, la copia, el espacio de trabajo, y los mensajes y comandos que tenías en cola. Lo que se pierde es la ronda que no terminó. Al reanudar, se trata la llamada en la que estaba la ejecución:

- Una llamada que **no había empezado** (esperaba tu aprobación, o su turno tras otra llamada) se vuelve a hacer, así que un comando que necesita tu aprobación te vuelve a preguntar. Una llamada que solo lee también se vuelve a hacer.
- Una llamada que **había empezado a cambiar algo** puede haberse hecho en parte: al agente se le dice que su resultado es desconocido y lo comprueba con una herramienta de lectura antes de repetirlo; las llamadas posteriores se marcan como no realizadas.
- Un comando tuyo que se estaba ejecutando cuando el servidor se detuvo sigue cuando sigue la ejecución. `/compact` y `/clear-workspace` se vuelven a ejecutar; cualquier otro comando que había empezado **no se vuelve a ejecutar** (puede haberse hecho en parte): al agente se le dice y lo comprueba. Los mensajes y comandos que llegaron después se entregan como de costumbre.
- El plan del agente y los resultados de sus comandos (`list_command_results`, `@result`) también se conservan, así que el agente los tiene tras el reinicio.
- Las llamadas de un subagente que estaban en curso se tratan como las del agente: las que no habían empezado se hacen, la que había empezado se le informa como "resultado desconocido", y un subagente que esperaba tu aprobación vuelve a preguntar.
- Si el servidor se **mata** (`kill -9`, un corte de luz) mientras un comando se está ejecutando, el comando muere con él; lo que hizo es como se describe arriba.

<a id="the-working-copy"></a>

## La copia de trabajo

Cada ejecución trabaja sobre una **copia de tu libro** y de sus libros de lore que la app hace al empezar la ejecución: los mensajes, resúmenes, etiquetas y ajustes tal como están en ese momento, y el libro de lore con sus sub-libros de lore. La copia está oculta: no aparece en las listas de libros y libros de lore. Lo que haga el agente, lo hace sobre la copia; tu libro sigue como estaba.

Cuando el agente tiene activado **Escribir el libro con el modelo del agente** (por defecto), la copia también recibe el **modelo del agente** (la IA y el protocolo de su perfil de modelo principal) en lugar de la IA y el protocolo de tu libro, cada vez que la ejecución empieza o se reanuda. Así, los comandos que escriben con un modelo (`/next`, `/swipe`, `/regenerate`, `/redraft-part`, `/generate-annotation`, los resúmenes) y los comandos de modelo de las extensiones (el Lore Generator, el Arc Outline) usan el modelo del agente incluso cuando tu libro está configurado con otro, o con ninguno; un `profile` indicado en la llamada sigue prevaleciendo. Con el ajuste desactivado usan el modelo del libro. Esto no es un cambio de tu libro: la revisión no lo lista y al aplicar el resultado la IA y el protocolo de tu libro se quedan como están.

La copia se pone en la [carpeta](books/folders.md) de tu libro, así que el agente escribe con los ajustes que fijan las carpetas (idioma, plantillas, punto de vista, tiempo verbal, estilo y las instrucciones), igual que lo hace el libro. La copia no es contenido de la carpeta: no se cuenta allí y el trabajo en ella no cambia la *Última modificación* de la carpeta. La copia sigue al libro si lo mueves mientras la ejecución está en pausa. Aplicar el resultado nunca mueve tu libro.

Una ejecución en pausa conserva su copia y vuelve a trabajar sobre ella al reanudarla. Una ejecución que está *Hecha*, o que detuviste y conservaste, la mantiene hasta que [te quedas con su resultado o lo descartas](#reviewing-the-result). Cuando la ejecución está *Fallida*, descartas su resultado, o sus cambios se aplican, la copia se borra para siempre, y la siguiente ejecución crea una nueva a partir del libro tal como esté entonces.
<a id="resuming-a-run"></a>

## Reanudar una ejecución

Una ejecución pausada o conservada no bloquea tu libro, así que puedes editarlo mientras tanto. Cuando **Reanudar** la ejecución, su copia de trabajo se pone al día con lo que hiciste:

- Lo que cambiaste tú y no el agente (una parte, una entrada del libro de lore, un ajuste, una etiqueta...) pasa a la copia. Una parte que añadiste también pasa, y el agente puede continuar a partir de ella; lo que borraste se borra de la copia.
- Lo que tú y el agente cambiasteis a textos distintos se queda como lo tiene el agente: no se pierde nada de lo que hizo. Es un [conflicto](#reviewing-the-result) que resuelves al revisar el resultado, con ambos textos delante.
- Lo que cambiaste tú y el agente borró se queda borrado, y lo que borraste tú y el agente cambió se mantiene en la copia; la revisión muestra ambos casos.

No hace falta ningún clic: la sincronización forma parte de reanudar y tarda un momento (unos segundos si cambiaste cientos de elementos). El estado de la copia antes de sincronizar se guarda como un [punto de control](#checkpoints) automático. Al continuar, se le informa al agente de lo ocurrido, usando los nombres con los que conoce los elementos, en un mensaje que dice («Mientras la ejecución estaba pausada, el usuario cambió el libro...»), y puede volver a consultarlo con `get_sync_report`.

**Un conflicto no desaparece por sí solo.** Cada reanudación compara de nuevo la copia con el libro, así que un conflicto se vuelve a informar, y se le vuelve a comunicar al agente, cada vez que reanudas, hasta que lo resuelves: cambiando tu lado del libro (al texto del agente, o de vuelta a lo que era), o en la revisión.

**Una nota al reanudar.** **Reanudar** abre una pequeña ventana con un campo *Nota para el agente (opcional)*. Lo que escribas ahí es lo primero que lee el agente al continuar, después del informe de los cambios que hiciste en el libro. Está pensada para un cambio de dirección («deja el libro de lore en paz», «empieza por el capítulo 3») y, a diferencia de un mensaje en una conversación, funciona en todas las ejecuciones, también en una que no espera tus mensajes. Déjala vacía para reanudar como antes.

![Reanudar una ejecución con una nota](../images/agent-resume.png)

<a id="reviewing-the-result"></a>

## Revisar el resultado

Cuando una ejecución está *Hecho* (o la detuviste con **Detener y conservar el resultado**), la fila del agente y la consola tienen un botón **Revisar los cambios**. Abre una ventana con
todo lo que cambió el agente, comparado con tu libro **tal como está ahora**, una línea por elemento. Si el agente informó de hallazgos, se listan encima de los cambios (**Hallazgos (3)**), así que una auditoría que no cambió nada también tiene algo que mostrar:

![Revisar el resultado de una ejecución](../images/agent-review.png)

- **Libro:** su nombre, descripción, idioma, punto de vista, tiempo verbal, estilo, plantillas, el número de versiones conservadas por parte, sus etiquetas y los datos que una extensión guarda en el libro (un esquema de arco, el registro de personajes).
- **Parte:** una parte cuyo texto, instrucciones, escenario, personaje de punto de vista, personajes presentes o resumen cambió el agente, una parte que escribió, una parte que borró. Las variables que una generación guarda con una parte (y los datos de las extensiones) se asumen junto con ella, como el campo **Datos**.
- **Libro de lore** y **Entrada:** un libro de lore o una entrada que creó el agente, cambió (también movida, renombrada, con otras etiquetas u otros sublibros) o borró.
- **Anotación:** una [nota](books/annotations.md#annotations-and-agents) sobre un pasaje de una parte que el agente creó, cambió (la nota, el tipo, el estado) o borró.

Marca las líneas que quieras y luego pulsa **Aplicar los cambios elegidos**. Los botones sobre la lista marcan **todas**, **ninguna**, **solo las partes nuevas** (la nueva rama que escribió el agente, nada más), **solo las anotaciones** o todo
**sin conflictos**. Una línea que necesita otra (una parte nueva necesita la parte a la que sigue, si esa también es nueva) arrastra a la otra al marcarla, y pierde a sus seguidoras al desmarcarla. Haz clic en una línea para verla
a la derecha: el texto de tu libro ahora y el texto que se escribirá, **que puedes editar** antes de aplicar. Si no escribes nada, se aplica el texto del agente.

Tu libro puede haber cambiado desde que empezó la ejecución (puedes editarlo en cuanto la ejecución no esté trabajando). Lo que cambiaste solo tú no aparece en la lista y se queda. Lo que ambos cambiasteis a lo mismo no es
ningún cambio. Lo que ambos cambiasteis a textos **distintos** es un **conflicto**: empieza desmarcado, y al marcarlo el texto que ves a la derecha reemplaza el texto del libro. **Usar el del libro** pone el texto del libro en la caja (la línea entonces no cambia nada en ese campo), **Usar el del agente** pone el del agente; o escribe tú el resultado. Una línea que no se puede tomar (la parte a la que sigue, o el libro de lore en el que está, se borró de tu libro) lo indica y no se puede marcar.

![Un conflicto en la revisión](../images/agent-review-conflict.png)

Debajo de los textos, **Diferencias** muestra lo que cambia **palabra por palabra**: lo que solo está en el libro ahora aparece tachado en rojo, lo que solo está en el texto a aplicar, en verde (sigue lo que escribas en la caja). En un conflicto muestra también
lo que cambió el agente y lo que cambiaste tú desde que empezó la ejecución, cada uno desde el libro tal como estaba entonces, para que veas quién hizo qué. Una línea que el agente cambió en más de una cosa (una parte cuyo texto y escenario cambiaron, una entrada con contenido nuevo y nota) tiene una casilla **Tomar este cambio** junto a cada una: desmárcala para aplicar la línea sin ese cambio, y el libro conserva su propio valor de ese campo. El lugar de una parte en la historia va con la parte y no tiene casilla,
y lo que el agente creó o borró se toma o se deja completo. **Mostrar en la copia** (en una línea que el agente creó o cambió) abre la [copia del libro](#the-copy-of-the-book) en esa parte o entrada, para leerla en su sitio.

![Las diferencias de un conflicto, palabra por palabra](../images/agent-review-differences.png)
<a id="data-that-extensions-keep-json"></a>

### Datos que guardan las extensiones (JSON)

Las extensiones guardan sus propios datos en un libro, en sus partes, en los libros de lore y en las entradas (un esquema de arco, el registro de personajes, las variables de una generación). Son JSON, y la revisión los muestra **con formato** (sangría y saltos de línea), con la línea
**Combinar datos JSON** junto al texto que se aplicará. Los datos no se toman enteros de una sola versión: lo que solo cambió el agente se toma del agente, lo que solo cambió tu libro se conserva, y lo que cambiaron ambos en **lugares distintos** se combina lugar por lugar. Es un conflicto solo cuando ambos cambiaron el **mismo** lugar a valores distintos (el orden de las claves no importa); mientras no decidas, en ese lugar se usa la versión del agente.

**Combinar datos JSON** abre una ventana con los datos de las tres versiones superpuestos en forma de árbol: *Al empezar la ejecución*, *En el libro ahora*, el *Agente* y el *Resultado*, una columna para cada uno, con una marca de lo que cambió y dónde (el agente, el libro, ambos, un conflicto). Solo se muestran los lugares que cambiaron (desmarca **Solo lo que cambió** para verlos todos); un lugar se abre solo cuando lo abres, así que un documento grande no supone ningún problema.

![Los datos de una extensión superpuestos](../images/agent-json-merge.png)

- Selecciona un lugar y toma sus datos de una versión (**Usar el del agente**, **Usar el del libro**, **Usar el del inicio de la ejecución**), **Omitir** (la clave o el elemento no está en el resultado), o **Combinar desde dentro** (deciden los lugares que hay dentro, cada uno por su cuenta). Una clave o un elemento que solo tiene una versión se copia al resultado tomándolo de esa versión. Tomar un objeto o una lista completa de una versión anula lo que hayas elegido dentro, pero no de forma definitiva: vuelve a combinar desde dentro y tus elecciones regresan.
- Los elementos de una **lista** no tienen nombre, así que se emparejan por lo que son: primero los datos idénticos, después el mismo `uuid`, `id` o `name`, y luego los que más se parecen; lo que sobra, en el mismo número y del mismo tipo, se considera cambiado. Si la suposición es errónea, **Dividir** un elemento en los de cada versión, o **Hacer coincidir con…** otro elemento con el que el primero no comparta ninguna versión.
- **Abrir** (o un doble clic) trabaja solo con un lugar: la ruta encima del árbol te devuelve al nivel anterior.
- La pestaña **Texto** muestra los datos del lugar abierto como texto: los datos del agente puedes **editarlos**, los demás solo leerlos (el libro, el inicio de la ejecución, el resultado), y **Mostrar la diferencia con** marca lo que difiere palabra por palabra. Lo que escribas se comprueba como JSON al volver al árbol, al cambiar lo que se muestra, al abrir otro lugar o al tomar el resultado; si no es JSON válido, te quedas donde estás y se te indica qué falla, sin que nada cambie. El texto válido reemplaza los datos del agente en ese lugar y todo lo que hay dentro se vuelve a comparar.
- **Empezar de nuevo** olvida lo que decidiste y escribiste; **Tomar el resultado** entrega el resultado a la revisión, donde es el texto que se aplicará (la línea se queda como la dejaste mientras la revisión está abierta).

La revisión también tiene **Usar el del agente** y **Usar el del libro** para un campo de este tipo, para tomar una de las dos versiones tal cual, y **Empezar de nuevo** para volver a los datos combinados.

![Los datos del esquema de arco en la revisión, con Combinar datos JSON](../images/agent-review-json.png)

Un libro de lore que usan otros libros se marca como **Compartido**: el cambio va al propio libro de lore, así que los demás libros también lo reciben. Empieza desmarcado.

Aplicar se hace sobre el libro **en su lugar**: las partes conservan sus direcciones, los demás libros mantienen sus enlaces a un libro de lore, y lo que el agente borró va a la papelera. Antes se hace una **copia de seguridad del libro tal como estaba** (está entre las copias de seguridad del libro, con sus libros de lore), y los cambios se hacen todos a la vez o ninguno. **Mostrar la rama en la que terminó el agente** también cambia el libro a la rama en la que estaba el agente, si está entre lo que aplicaste. Los resúmenes de las partes que se editaron quedan desactualizados, igual que tras una edición a mano; los resúmenes propios del agente se toman tal cual.

Cuando los cambios se aplican, la ejecución pasa a *Aplicada* y se borran su copia y sus puntos de control. **Descartar el resultado** tira la copia y deja el libro como está. Hasta que hagas una de las dos cosas, el agente tiene una ejecución sin terminar y no puede empezar otra.

<a id="the-copy-of-the-book"></a>

### La copia del libro

La pestaña **Copia del libro** muestra el libro sobre el que trabaja el agente, para leerlo: la **historia**, una rama cada vez (elige la rama arriba; cada parte con su número, el intercambio que ocupa entre las demás tras la misma parte, sus palabras, el escenario de la escena, el punto de vista y, plegados, sus instrucciones y su resumen) y los **libros de lore** con sus entradas (elige uno para ver su contenido, nota, etiquetas y ajustes). No es una ventana de tu libro: nada de lo que hay en ella se puede cambiar, no se hace copia de seguridad de la copia y ninguna extensión ve un libro abierto. Se lee al abrir la pestaña y al pulsar **Actualizar**, también mientras el agente trabaja, así que muestra lo que el agente ha escrito hasta el momento. Está disponible mientras la ejecución tiene su copia (no después de que el resultado se haya aplicado o descartado).

![La pestaña Copia del libro](../images/agent-book-copy.png)

<a id="diagnostics"></a>

### Diagnóstico

La pestaña **Diagnóstico** cuenta lo que hizo la ejecución a partir de su registro, para averiguar qué necesita un agente de sus herramientas y de su prompt. Funciona con cualquier ejecución, también con una antigua (elígela primero en **Ejecuciones**), y se genera al abrir la pestaña y al pulsar **Actualizar**.

![La pestaña Diagnóstico](../images/agent-diagnostics.png)

- **Resumen**: rondas y llamadas, cuántas llamadas fallaron, el tiempo del modelo, de las llamadas y de las pausas (una espera de 30 minutos o más entre dos pasos es una pausa, no trabajo), el contexto en su máximo y al final, los tokens, las compactaciones, tus mensajes y los avisos que el ejecutor dio al agente (una llamada ilegible, demasiadas llamadas, un contexto lleno).
- **Herramientas**: para cada herramienta, las llamadas, los errores, los resultados que se recortaron, cuántas veces fue la única llamada de una ronda, los caracteres que escribió el agente, cuántas llamadas pasaron un texto como `@file:` o `@result`, cuántas escribieron un texto largo (300 caracteres o más) y cuántas de esas lo copiaron de un resultado que el agente acababa de recibir, los tokens de sus resultados (aproximados, a partir de los caracteres), la proporción sobre todos los resultados, el resultado más largo y el tiempo.
- **Lo que informaron las peticiones**: se registra mientras la ejecución trabaja, así que solo está en las ejecuciones hechas después de que se añadiera esto (una ejecución antigua indica que no tiene datos): cuánto esperaron las peticiones hasta la primera palabra (el servidor leyendo el prompt, así que un tiempo corto significa que su caché estaba caliente), cuánto tardaron en escribirse las respuestas y cuántos tokens por segundo, cuánta parte del prompt no volvió a leer el servidor (la caché; llama.cpp y OpenAI la informan), cuánta parte de las respuestas fue razonamiento, por qué terminaron las respuestas (`length` significa que el límite de una respuesta la cortó), las llamadas que escribió el modelo y no se hicieron (repeticiones, los límites, las políticas de lote), el tiempo que las peticiones esperaron un lugar, los resúmenes escritos y lo que ocuparon, y lo mismo para cada bloque de 25 rondas.
- **Lo que escribió el modelo**: los caracteres de los argumentos, del texto junto a las llamadas y del razonamiento, en total y para cada bloque de 25 rondas con el tiempo del modelo, para ver adónde va su salida y si dejó de usar `@file` en algún momento. **Argumentos más largos** muestra el inicio de las llamadas más grandes, con la ruta de una herramienta de archivos.
- **Rondas perdidas por un patrón**: la misma herramienta llamada ronda tras ronda (tres o más), marcada cuando una búsqueda iba justo antes, y las llamadas repetidas con los mismos argumentos. Estas muestran dónde una herramienta que acepta muchos elementos, o más llamadas en una sola respuesta, ahorraría rondas.
- **Errores** con los argumentos de la primera llamada que recibió cada mensaje (para ver qué envió el modelo cuando una llamada falló), los **resultados más grandes**, las **rondas más lentas**, las **compactaciones** y los **subagentes** con las herramientas que usó cada uno.

**Descargar** entrega el informe en Markdown, **Descargar JSON** en JSON, **Copiar** pone el Markdown en el portapapeles. Los tiempos son las diferencias entre las filas guardadas, así que son aproximados: el tiempo de una respuesta incluye la espera de un lugar libre en el modelo.
<a id="the-context"></a>

### El contexto

La pestaña **Contexto** muestra lo que el modelo lee con su **próxima solicitud**, desglosado, para que veas en qué se gastan los tokens y puedas depurar un agente que se comporta de forma extraña. Se genera al abrir la pestaña y al pulsar **Actualizar** (no mientras la ejecución trabaja: cuenta los tokens de cada parte).

- Tres gráficos: los **tipos de partes** frente al límite del contexto (el prompt del agente, tus instrucciones, las notas del ejecutor, las definiciones de herramientas, el resumen de una compactación, tus mensajes, las respuestas del agente, los resultados de sus llamadas y lo que queda libre), **cada parte en el orden en que se envía** (una barra por cada una, del color de su tipo: una herramienta cuya descripción es enorme o un resultado que llena el contexto destacan), y **el contexto en cada respuesta del modelo**: una barra apilada de lo que lo componía (los mismos tipos, estimados), el tamaño que el modelo **informó** como una línea y el límite como una línea discontinua. Cuando la línea está por encima de las barras, el modelo contó más que la estimación (imágenes, el formato del proveedor); una barra que baja es una compactación o un retroceso.
- Una **tabla de las partes** con el tipo, el nombre, los tokens y la proporción del contexto. Elige una fila para leer **exactamente el texto que se envía al modelo** para esa parte: una sección del mensaje de sistema, la descripción y los parámetros de una herramienta, un mensaje, los argumentos de una llamada o el resultado de una.
- **Descargar** entrega todo el contexto como un único archivo Markdown (el mensaje de sistema con sus secciones, cada herramienta con su descripción y esquema, la conversación mensaje a mensaje, cada texto en un bloque de código); **Copiar** pone el mismo texto en el portapapeles.

![La pestaña Contexto](../images/agent-context.png)

Los tamaños de las partes son estimaciones; el número que informa el modelo es el real (la solicitud y su respuesta). En el gráfico de las respuestas, el prompt, las instrucciones y las herramientas son como están ahora (una respuesta antigua pudo tener otros si los cambiaste), la conversación es como era entonces: un resumen sustituyó lo que había antes, y lo que un retroceso deshizo desaparece de las respuestas posteriores (una compactación que a su vez fue retrocedida no se recupera; las barras posteriores quedan un poco pequeñas). El mensaje de sistema y las herramientas son los que usaría la siguiente solicitud, así que un cambio del prompt, de las instrucciones o de los comandos permitidos se ve al instante. Los mensajes son la transcripción tal como está guardada, sin lo que añade una reanudación (un "sigue", las respuestas a llamadas que una interrupción dejó abiertas). El razonamiento no forma parte del contexto (no se le devuelve al modelo) y las imágenes aparecen con sus nombres de archivo. Lo que una compactación sustituyó tampoco está en el contexto; la pestaña **Ejecución** y la descarga de **Ejecuciones** aún lo tienen.

<a id="checkpoints"></a>

## Puntos de control

Un **punto de control** es un punto de restauración de la copia de trabajo de la ejecución (las partes, resúmenes, lorebooks y los datos de las extensiones, con los identificadores que se dieron al agente) y del espacio de trabajo del agente, para que un trabajo que salió mal pueda deshacerse: el agente vuelve atrás con `checkpoint_restore`, o lo haces tú en la pestaña **Puntos de control** de la consola. Quién los crea:

- **Inicio de la ejecución**, creado cuando empieza la ejecución. Nunca se retira.
- **Automático**, creado por la ejecución antes de una ronda en la que el agente llama a algo que puede cambiar el libro o el espacio de trabajo (un comando que no es una simple lectura, o una herramienta de archivos que escribe), y solo si algo cambió desde el último punto de control. Se conservan los últimos cinco; el más antiguo se borra cuando se crea uno nuevo.
- **Hecho a propósito**, creado por el agente con `checkpoint` (se le indica que cree uno antes de un paso arriesgado) o por ti con **Guardar un punto de control**. Permanecen hasta que se borran.

![La pestaña Puntos de control](../images/agent-checkpoints.png)

Todos los puntos de control de una ejecución comparten una **cuota** que el administrador fija en el [permiso](administration/permissions.md#settings-of-a-permission) (100 MB si está vacía). Una parte se reserva para los automáticos, para que el agente no pueda agotarla toda; un punto de control hecho a propósito que no cabe es **rechazado**, y nada se borra a espaldas de quien lo pidió. Entonces el agente recibe la lista y borra lo que no necesita (no el del inicio, ni el más reciente); tú puedes borrar cualquiera en la pestaña. Cada uno es un archivo (un ZIP del libro como JSON, los pares y los archivos del espacio de trabajo) que puedes **descargar**.

**Restaurar** rehace la copia de trabajo a partir del punto de control: es una copia nueva, la antigua se borra, el espacio de trabajo es el guardado (los archivos escritos después desaparecen) y los identificadores del agente para los elementos que estaban allí siguen funcionando. El estado anterior se guarda primero como punto de control automático, si hay espacio, así que una restauración puede deshacerse. Restaurar tarda casi lo mismo que el inicio de una ejecución (media hora para un libro de dos mil partes). Puedes restaurar mientras el agente no trabaja; el agente se restaura solo entre dos rondas y se le informa de lo que volvió. Los puntos de control desaparecen cuando la ejecución suelta su copia.

**Retroceder** es una restauración que además devuelve la *conversación*. Una restauración sola deja al agente recordando trabajo que ya no está en el libro, así que una ejecución que quieres encaminar en otra dirección necesita un retroceso: la copia de trabajo y el espacio de trabajo vuelven al punto de control, los mensajes posteriores al lugar donde se creó el punto de control aparecen en gris en la transcripción (aún puedes leerlos, y la exportación los tiene) y el agente ya no los lee, y un aviso le cuenta al agente lo que ocurrió, para que rehaga el trabajo si todavía hace falta. La ejecución tiene que estar en pausa o detenida (pausa primero un agente que te espera); después **Reanudar** la ejecución y continúa desde el aviso. Solo se puede retroceder a un punto de control mientras su lugar en la conversación siga formando parte de ella: tras retroceder a un punto anterior, los posteriores (de la conversación que se deshizo) solo pueden restaurarse. Un punto de control creado antes de que existiera el retroceso no conoce su lugar y solo puede restaurarse. Lo que una compactación hizo después del punto de control se deshace con el retroceso; el agente vuelve a leer los mensajes que la compactación había sustituido.

**Volver a un mensaje.** No tienes que elegir un punto de control: cada mensaje que escribiste al agente tiene un botón **Retroceder** debajo en la transcripción de la pestaña **Ejecución**. Devuelve la conversación a antes de ese mensaje: el agente olvida el mensaje y todo lo que vino después, y la copia de trabajo y el espacio de trabajo vuelven al punto de control más reciente creado antes de él (el ejecutor crea uno antes de cada ronda que cambia algo, así que suele caer justo en el mensaje). El estado de una ejecución solo se conoce en sus puntos de control, así que cuando el más cercano es anterior al mensaje, también se olvidan los mensajes entre ese punto de control y el tuyo; la confirmación indica cuál es el punto de control y cuántos mensajes se van. Los mensajes olvidados permanecen en la transcripción, en gris. El botón funciona mientras el agente espera tu respuesta (está en pausa por ello), está en pausa, o se detuvo y se conservó; mientras el agente trabaja está deshabilitado. Después **Reanudar** la ejecución, y continúa desde el aviso.
<a id="creating-and-editing-an-agent"></a>

## Creación y edición de un agente

**Nuevo agente** abre la misma ventana que **Editar**:

![La ventana de un nuevo agente](../images/agent-dialog.png)

| Campo | |
|---|---|
| **Nombre** | Obligatorio. |
| **Activado** | Un agente desactivado no puede iniciarse; sus ejecuciones existentes no se tocan. |
| **Libro** | El libro en el que trabaja el agente, obligatorio. Solo se ofrecen libros que no tienen agente, y el libro de un agente no se puede cambiar después. |
| **Modelo**, **Protocolos** | El modelo y el protocolo con el que habla el agente. Ambos, o ninguno; un agente sin ellos no puede iniciarse. |
| **Escribir el libro con el modelo del agente** | Activado (por defecto): los comandos que tienen modelo escriben algo (el siguiente turno, los swipes, los resúmenes y los comandos de extensiones como el Lore Generator y el Arc Outline) usando el modelo de arriba, sea cual sea el modelo de tu libro. Desactivado: usan el modelo de tu libro (un comando de extensión recurre a tu modelo predeterminado de [Ajustes](account.md) cuando el libro no tiene ninguno). Así es como mezclas modelos: uno potente que piensa y otro más barato o más fluido que escribe. Un comando puede seguir nombrando otro perfil de modelo del agente. |
| **Llamadas en una misma respuesta** | Cuántas de las llamadas que el agente escribe en una respuesta se hacen. *Todos juntos, ignorando las políticas de lote*: todas las llamadas (lo más rápido). *Juntos, los comandos marcados como Solo por separado* (por defecto): las llamadas se hacen juntas, pero un comando cuya columna **Lote** dice *Solo*, como uno que hace una llamada a un modelo, se hace en una respuesta propia, y el agente se lo indica. *Una llamada por respuesta*: solo se hace la primera llamada y el agente escribe la siguiente después de leer el resultado; es más lento (cada llamada es una ronda propia, con el razonamiento del modelo) pero puede reaccionar a cada resultado. Un **error siempre termina la respuesta**, en todos los modos: las llamadas posteriores al fallido no se hacen (se responden con "no realizada porque una llamada anterior falló") y el agente lee primero el error. |
| **El modelo ve imágenes** | Márcalo si el modelo acepta imágenes (la API del modelo no lo indica, así que lo indicas tú). El agente tiene entonces `view_image` y puedes adjuntar imágenes a tus mensajes; sin esta opción nunca recibe una imagen. |
| **Idioma** | El idioma de todo lo que lee el agente: las descripciones de sus comandos, sus resultados y sus errores, y los textos integrados de la ejecución (su prompt del sistema por defecto, los avisos de la ejecución, el prompt de compactación) cuando existen en ese idioma (inglés, japonés, chino); el [idioma del prompt](account.md#prompt-language) de tus ajustes no se aplica a los agentes. El inglés es el predeterminado y la opción más segura, porque los comandos y sus parámetros están en inglés sea cual sea el idioma (las descripciones en otro idioma mezclan ambos), y los modelos, sobre todo los pequeños, usan las herramientas de forma más fiable así. La lista contiene los idiomas instalados en la aplicación (una extensión puede añadir uno, como la localización al chino). **No tiene nada que ver con el idioma del libro**: al agente se le indica que escriba el texto del libro en el idioma del libro, que puede leer con `get-book-info`. Una ejecución conserva el idioma con el que se inició; un cambio se aplica a la siguiente ejecución. |
| **Comandos destructivos** | Qué ocurre cuando el agente quiere un comando que cambia o elimina datos: *Ejecutarlos*, *Preguntar antes* (una [ventana](#asking-you) te pide que lo apruebes) o *Rechazarlos*. |
| **Máximo de turnos**, **Presupuesto de tokens de una ejecución**, **Límite de contexto** | Límites opcionales de una ejecución. El límite que te haya dado tu administrador se aplica en cualquier caso. |
| **Máximo de subagentes** | Cuántos [subagentes](#sub-agents) puede tener trabajando a la vez el agente, de 0 a 10. Vacío o cero significa ninguno. |
| **Modelos para subagentes** | Los [modelos](#models-for-sub-agents) en los que puede ejecutarse un subagente, y si puede ejecutarse en el principal. |
| **Compactar al (% del contexto)** | Cuando el contexto del modelo supera esta proporción de su límite y el agente no ha [compactado su trabajo](#running-a-command-yourself) por sí mismo, la ejecución lo hace: el agente escribe el resumen de su trabajo anterior, que lo sustituye. Vacío equivale a 90, cero nunca (la ejecución se detiene entonces cuando el contexto se llena); en otro caso, de 50 a 99. |
| **Prompt del sistema** | Lo que se le indica al agente que sea. Déjalo vacío y el agente sigue el predeterminado (el tuyo de [Ajustes](account.md#templates), si no el de tu administrador, si no el integrado), que se muestra en gris. El icono de información tiene **Insertar la plantilla predeterminada**, que rellena el predeterminado para que edites una copia; vacía el campo de nuevo para volver a seguir el predeterminado. |
| **Prompt de compactación** | Lo que se le pide al agente cuando lo [`/compact`](#running-a-command-yourself): escribir el resumen que sustituye su trabajo anterior. Vacío sigue el predeterminado, como el prompt del sistema, con el mismo icono de información. |
| **Instrucciones (agents.md)** | Tus instrucciones para el agente, añadidas a su prompt en cada ejecución. El agente puede leerlas, nunca cambiarlas. Vacío sigue tu predeterminado de [Ajustes](account.md#templates), si lo tienes (se muestra en gris); si no, no hay ninguna. El icono de información tiene **Insertar la plantilla predeterminada**, que rellena tu predeterminado para que edites una copia. Debajo, una línea indica cuánto pesan, en tokens, el prompt, las instrucciones y los comandos marcados **en cada petición** (una estimación, con las herramientas de archivos y de la ejecución y las notas del ejecutor encima). |
| **Comandos permitidos** | Véase más abajo. |

![Los prompts del agente y las pistas de la plantilla predeterminada](../images/agent-dialog-prompts.png)

<a id="allowed-commands"></a>

### Comandos permitidos

Al agente solo se le ofrecen los comandos que marcas. La tabla enumera cada comando con su efecto (*Destructivo* cambia o elimina datos; *No destructivo* solo lee o añade). El cuadro **Selección** marca muchos a la vez: **Todo**, **Ninguno** o **Todos los no destructivos** (por defecto, para un agente nuevo), o un rol; marcar un comando tú mismo convierte la selección en *Personalizada*. El campo **Buscar comandos** encima de la tabla muestra solo los comandos que tienen todas las palabras que escribas (en cualquier orden, en el nombre o en la descripción); la selección sigue aplicándose a todos los comandos, también a los ocultos. Los comandos que aporta una extensión siguen permitidos cuando la extensión no está cargada; simplemente no los ves. La columna **Lote** indica si el comando se usa *Juntos* con otras llamadas en una misma respuesta o *Solo* (un comando que hace una llamada a un modelo, como `next`, `swipe`, `regenerate`, `redraft-part`, `generate-annotation` o la compactación, va solo; véase **Llamadas en una misma respuesta** más arriba). Todas las columnas se pueden redimensionar. La columna **Tokens** es lo que la definición del comando (su nombre, descripción y parámetros) añade a cada petición del agente, y la línea bajo las instrucciones suma los marcados: el conjunto completo ocupa unos 20 000 tokens, así que una selección menor (un rol) deja más espacio en el contexto para el trabajo.

![Los comandos permitidos, con los roles del cuadro Selección](../images/agent-commands.png)

Los roles son conjuntos de comandos para una tarea. Todos pueden leer el libro y los lorebooks (todos los comandos que solo leen, también los de extensiones), y ninguno puede eliminar datos:

| Rol | Además de leer |
|---|---|
| **Auditor** | Nada: comprobaciones de continuidad, auditorías de nombres y términos, informes. |
| **Guardián del lore** | Crea y actualiza entradas de lorebook y lorebooks, sus etiquetas y sub-lorebooks. |
| **Editor** | Edita y añade al final de partes, establece sus campos, mantiene los resúmenes (también hace que el modelo los vuelva a escribir) y la información y etiquetas del libro. |
| **Redactor** | Añade partes y ramas, edita y añade al final, deja que el modelo escriba el siguiente turno (`/next`, `/swipe`), mantiene los resúmenes, crea y actualiza entradas de lorebook de lo que inventa. |

Cada comando marcado añade su definición a cada petición, así que un rol es también la forma más barata de tener un prompt pequeño para un modelo débil.
<a id="on-a-phone"></a>

## En un teléfono

La vista para teléfono tiene una pestaña **Agentes** en el cajón, para los usuarios que pueden usar agentes, con el número de agentes que necesitan tu atención junto a su nombre. Cada agente es una tarjeta con su nombre, su estado y los botones de la tabla (se ajustan en varias líneas). Las ventanas —la consola, la revisión y los ajustes del agente— ocupan toda la pantalla, y la revisión muestra la tabla encima del detalle del elemento. Todo funciona igual que en el escritorio, solo que más pequeño. La consola está pensada para el pulgar:

![La pestaña Agentes en un teléfono](../images/phone-agents.png)

![La consola en un teléfono](../images/phone-agent-console.png)

![La pestaña Agentes en un teléfono](../images/phone-agents.png)

![La consola en un teléfono](../images/phone-agent-console.png)

- **Las pestañas son iconos** (ejecución, ejecuciones, espacio de trabajo, puntos de control, contexto, copia del libro, subagentes, diagnóstico); el título de la ventana indica cuál está abierta.
- **Pausar, Pausar ahora, Reanudar, Terminar y Detener son botones de icono en la parte inferior, y solo se muestran los que se pueden usar en ese momento**; **Revisar cambios** mantiene su texto, es lo que espera una ejecución terminada. **Actualizar** y **Cerrar** son iconos en la cabecera.
- **La caja de mensajes tiene una sola línea**, con los iconos **Enviar** y **Adjuntar imagen** junto a ella. La **línea de comandos** (el icono de terminal) está oculta hasta que la abres, para que el teclado no tape la transcripción sin necesidad. Los mensajes de la transcripción usan todo el ancho.
- **Las tablas son listas de tarjetas**: una ejecución, un punto de control, un comando de los ajustes del agente o un elemento de la revisión muestran su nombre y su estado, con los detalles debajo y sus botones abajo, en lugar de columnas. Los archivos del espacio de trabajo muestran su tamaño debajo del nombre, y la barra de herramientas del espacio de trabajo es de iconos. La tabla de contexto conserva el número, el nombre y los tokens; el tipo y la proporción de una parte aparecen debajo del texto de la parte que eliges.

- **La barra superior muestra quién te espera.** Mientras el cajón está cerrado, un número rojo en la barra superior (junto al título de la parte abierta) cuenta los agentes que necesitan tu atención, igual que la insignia de la pestaña **Agentes** en el cajón; tócalo para ir a la pestaña **Agentes**. Se oculta cuando nadie espera, mientras el cajón está abierto y en la propia pestaña Agentes.
- **Desliza entre las partes.** Un deslizamiento lateral sobre el contenido lleva a la siguiente parte del cajón (deslizando hacia la izquierda) o a la anterior (hacia la derecha). No hace nada con el cajón abierto ni mientras la aplicación bloquea la navegación, y un toque que empieza en un campo de texto, una tabla o cualquier elemento que se desplaza lateralmente queda para ese elemento.

Para un trabajo largo con la transcripción o la revisión, una pantalla más grande sigue siendo más cómoda.

<a id="cloning"></a>

## Clonar

**Clonar** pide un nombre y un libro que no tenga agente, y copia la definición del agente y sus archivos (no sus ejecuciones) a un nuevo agente de ese libro. Puedes tener tantos agentes, clones incluidos, como permita tu administrador (cinco, salvo que hayan fijado otro número); al llegar al límite, **Nuevo agente** y **Clonar** quedan desactivados y te indican el motivo.

![Clonar un agente](../images/agent-clone.png)

<a id="lore-notes-of-an-imported-book"></a>

## Notas de lore de un libro importado

Tras [importar](books/story-editor.md#after-an-import) un manuscrito, la aplicación ofrece un agente que toma el lore del libro: **Tomar notas de lore con un agente...** (el mismo elemento está en el menú de ajustes ⚙ de una historia, **Análisis**). Primero pide confirmación:

![La pregunta antes de que el agente empiece](../images/lore-notes-confirm.png)

Después crea un agente llamado **Notas de lore** para el libro, con el modelo y el protocolo del libro, y lo inicia (se abre su consola).

- Lee la historia desde la primera parte hasta la última, unas pocas partes cada vez, y guarda sus notas en los archivos de su espacio de trabajo (personajes, lugares, objetos, términos) para poder continuar tras una pausa. Los resúmenes le ayudan a orientarse; las partes son su fuente.
- Después escribe una entrada para cada personaje, lugar, objeto, grupo o término en el lorebook del libro (si el libro no tiene uno, se crea), usando como filtro los nombres que emplea la historia, en el idioma del libro. Añade datos a una entrada que ya existe y no escribe dos veces lo mismo.
- Puede leer la historia y los lorebooks y escribir entradas; no puede modificar la historia ni borrar nada.

Es un agente como cualquier otro: el libro está [bloqueado](#the-book-is-locked) mientras trabaja, trabaja sobre una [copia](#the-working-copy), y lo que escribió solo es tuyo después de que lo [revises](#reviewing-the-result) y lo incorpores. Lee las entradas antes de hacerlo: solo conoce lo que dice el texto, y un libro largo consume muchas solicitudes. Las instrucciones son las del agente (**Editar** las muestra, y puedes cambiarlas antes de volver a iniciarlo). Se rechaza cuando no tienes permiso para usar agentes, cuando el libro no tiene modelo y cuando el libro ya tiene un agente (un libro solo puede tener uno).

<a id="the-book-is-locked"></a>

## El libro está bloqueado

Mientras una ejecución de un agente trabaja o espera tu respuesta, **su libro está bloqueado**: puedes leerlo, pero nada lo modifica, para que el agente nunca trabaje sobre texto que cambia bajo sus pies. La lista de libros no abre un libro bloqueado, sino que te indica qué agente lo tiene, y generar, editar, buscar y reemplazar, los resúmenes y borrar el libro se rechazan. **Pausa** o **Detén** la ejecución para volver a editar el libro; una ejecución pausada, detenida o terminada no lo bloquea.

![Un libro en el que trabaja un agente, en la lista de libros](../images/agent-locked-book.png)

Los **lorebooks del libro** quedan bloqueados junto con él: el lorebook que usa el libro y sus sublorebooks. En la pestaña Lorebooks, un lorebook así se muestra como en un teléfono, como texto simple y sin ninguna edición, con una nota que indica qué agente lo tiene; **Actualizar** lo muestra de nuevo para editarlo cuando hayas pausado o detenido la ejecución. Los demás lorebooks no se ven afectados.

<a id="agents-and-folders"></a>

### Agentes y carpetas

Un libro en una [carpeta](books/folders.md) sobre el que trabaja un agente conserva su lugar y sus ajustes: mover o borrar una carpeta por encima de él, y cambiar un ajuste que esa carpeta transmite (idioma, plantillas, punto de vista, tiempo verbal, estilo, indicaciones), se rechaza indicando el nombre del libro y del agente. Renombrar una carpeta, o cambiar su descripción, no se rechaza. Una ejecución pausada, detenida o terminada lo libera.

![El lorebook de un libro en el que trabaja un agente](../images/agent-locked-lorebook.png)
