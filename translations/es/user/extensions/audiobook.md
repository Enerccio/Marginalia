---
label: Audiolibro
order: 27
---

# Audiolibro

Convierte un libro en audiolibro, parte a parte, con un servidor de texto a voz: una voz para el narrador y otra para cada personaje, guardadas
junto con las partes del libro y reproducidas en el lector. Puedes dejarlo funcionar (el modelo indica quién habla y cómo, el servidor de voz
lee) para un borrador rápido, o trabajar las partes una a una en el **Estudio de audiolibro**, viendo exactamente lo que se envía al
servidor de voz y cambiándolo a mano, y exportar el resultado como archivos mp3 con marcas de capítulo.

La extensión no genera la voz por sí misma: se comunica con un **Chatterbox-TTS-Server** que tú ejecutas (consulta
[Configurar el servidor de voz](audiobook-server.md)). El texto del libro nunca se modifica y nunca se envía a nadie más que a los servidores
que tú hayas indicado; el audio se guarda en tu propia instalación, como recursos vinculados a las partes.

<a id="what-you-need"></a>

## Lo que necesitas

- Un servidor de voz y, si comparte la tarjeta gráfica con tu modelo de lenguaje, las peticiones que le hacen sitio y lo detienen después
  ([Configurar el servidor de voz](audiobook-server.md)).
- Voces: un breve **clip de referencia** (de 12 a 30 segundos de una sola persona, sin música) por cada voz que quieras clonar, o uno de los archivos de voz
  que tenga el servidor. Un narrador necesita una voz de lectura tranquila y constante.
- Para un libro con varias voces, un modelo de lenguaje que pueda etiquetar el texto. Por defecto es el modelo del libro.
- **ffmpeg** es opcional. Sin él, todo funciona con mp3. Con él en el servidor (está en la imagen de Docker, y en un ordenador de escritorio se busca en el `PATH` o en las carpetas habituales
  como `/opt/homebrew/bin`; la variable `MARGINALIA_FFMPEG` indica su ruta si está en otro lugar), la exportación obtiene el formato **m4b**, y un administrador puede guardar el audio en wav.

<a id="the-settings"></a>

## Los ajustes

*Ajustes → Ajustes de extensiones → Ajustes de audiolibro* es el panel de tu cuenta; guarda la página de *Ajustes* después de cambiarlo.

![El panel con un backend y los actores](../../images/plugin-audiobook-settings.png)

### Backends

Un backend es un servidor de voz y el modelo con el que habla. Añade uno con **+**.

| Ajuste | Qué hace |
|---|---|
| Nombre | El nombre que aparece en el diálogo de conversión. |
| Dirección | La dirección base del servidor de voz, o de lo que lo reenvía, por ejemplo `http://localhost:8004`. |
| Clave de API | Se envía como token de portador si el servidor (o tu proxy) lo requiere. Se guarda cifrada y no se vuelve a mostrar; escribe una nueva para reemplazarla, o usa **Remove the key**. |
| Modelo de voz | **Chatterbox Turbo** (inglés, con etiquetas de sonido como `[sigh]`, el más rápido y el predeterminado), **Chatterbox** (inglés, con una *exaggeration* de la entonación) o **Chatterbox Multilingual** (23 idiomas). Las peticiones previas a un lote deben dejar este modelo cargado. Un libro que necesite otro modelo usa otro backend. |
| GPU (nombre) | Opcional. Los servidores que comparten una GPU, también los del [Generador de imágenes](image-generator.md), reciben aquí el mismo nombre: su trabajo espera a que los demás terminen, y un backend de otro modelo espera a que los demás hayan acabado. Vacío: solo los lotes a esta dirección esperan entre sí. |
| El servidor está en esta máquina o en mi red | Permite direcciones de la red local. Déjalo desactivado para un servidor en la nube: así no puede hacer que llame a tu propio equipo. |
| Tiempo de espera de una petición | Cuánto esperar a un fragmento de voz. |
| Reiniciar el servidor después de (fragmentos) | Para un servidor de voz que pierde memoria y que el sistema termina en un lote largo: tras este número de fragmentos, las peticiones posteriores al lote y luego las anteriores se vuelven a hacer en mitad del lote (detener, iniciar, cargar), y el trabajo continúa con un servidor nuevo. 0 (el predeterminado) nunca lo hace. Un servidor que se cae se vuelve a iniciar independientemente de este ajuste, consulta [Solución de problemas](audiobook-server.md#troubleshooting). |
| Clave de las peticiones alrededor de un lote | La clave que las peticiones anteriores y posteriores a un lote envían como `Authorization: Bearer`. Se guarda cifrada y no se vuelve a mostrar (**Remove the key** la borra), así no tiene que escribirse en las cabeceras de cada petición, donde sería legible. Vacío: se usa la *Clave de API* de arriba, que es lo que quieres cuando un proxy como LiteLLM sirve todo. Una petición que tenga su propia cabecera `Authorization` la conserva. |
| Voz del narrador | El narrador de un libro que no tiene reparto. Un archivo de voz del servidor se escribe `predefined:Henry.wav`. |
| Valores predeterminados de un segmento | Temperatura, peso CFG, velocidad y tamaño de fragmento con los que empieza un fragmento (el estudio puede cambiarlos para un solo fragmento). |
| Antes de un lote, Después de un lote | Las peticiones que se hacen alrededor de un lote de partes: *antes* libera la tarjeta gráfica e inicia el servidor de voz, *después* lo detiene. Consulta [Una sola GPU para el modelo y la voz](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Vacío no hace nada. |

**Test connection** hace lo mismo que un lote: realiza las peticiones previas a un lote (así un servidor de voz detenido se inicia), pregunta al servidor si está disponible y tiene un modelo cargado,
y realiza las peticiones posteriores al lote. Sin peticiones configuradas, solo pregunta.

Un libro que tenga fragmentos en otro alfabeto (japonés en un libro en inglés) los envía al modelo multilingüe, si tienes un backend
para él, con su idioma; si no tienes ninguno, se quedan con el modelo del libro.

<a id="shared-speech-servers"></a>

### Servidores de voz compartidos

En una instalación con varios usuarios, el administrador puede mantener servidores de voz para todos, de modo que una máquina con GPU se configura una sola vez y nadie necesita una dirección ni una clave. El administrador lo hace en el mismo panel (las últimas secciones, visibles solo para los administradores): **Shared speech servers** abre una ventana con los mismos campos que un backend propio, además de quién puede usarlo: **all users**, o los usuarios elegidos en la lista. Un interruptor ahí, **Users may add speech servers of their own**, decide si todavía se permiten los backends de abajo.

Lo que obtienes:

- **Backend in use** en la parte superior del panel enumera los servidores compartidos que puedes usar (marcados *(shared)*) y los tuyos. La ventana *Convertir a audio* y el estudio usan el que elijas; los comandos también lo usan.
- Solo ves el nombre de un servidor compartido, nunca su dirección, claves o peticiones. Los lotes de todos los usuarios en una misma dirección de servidor esperan entre sí, así que dos usuarios no pueden iniciar la misma GPU a la vez.
- Cuando el administrador te retira un servidor, un trabajo que aún lo nombre será rechazado; elige otro.
- Cuando el administrador desactiva tus propios servidores, el panel lo indica. Los que añadiste se conservan y se vuelven a usar si el interruptor se reactiva.
- La *Voz del narrador* de un servidor compartido debería ser un archivo de voz del servidor (`predefined:Henry.wav`): un clip de referencia es un recurso de un usuario y los demás no pueden leerlo. Las voces de tus actores funcionan como siempre.

<a id="actors"></a>

### Actores

Un **actor** es una voz tuya: un clip de referencia (uno de tus [recursos](../resources.md) de audio, o uno nuevo subido aquí) o un
archivo de voz del servidor, con voces propias para un **estado de ánimo** (un susurro, un grito) o un **idioma** (un clip que habla
japonés). El idioma tiene prioridad sobre el estado de ánimo, y sin una voz para ellos lee la voz del actor. Tus actores se guardan
en tu cuenta; cualquier libro puede asignar un actor a su narrador y a sus personajes.

**Preview** junto a una voz (en el actor, los ajustes y el reparto) tiene una frase corta dicha con ella (en el idioma de la voz, si es de un idioma) y la reproduce, iniciando el servidor de voz si las peticiones previas a un lote lo requieren. Los actores aparecen listados por nombre.

![Un actor con una voz para un estado de ánimo](../../images/plugin-audiobook-actor.png)

<a id="converting-new-parts-by-itself"></a>

### Convertir las partes nuevas por sí mismo

*Convert new parts to audio by itself* es el valor predeterminado para todos tus libros; consulta [Conversión automática](#automatic-conversion).
<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## El reparto y la pronunciación de un libro

Ambos se guardan con el libro (viajan en su copia de seguridad) y se editan desde el diálogo **Convertir a audio**.

**Reparto** indica quién lee: un actor para el **narrador**, otro para **otros personajes** (quien el modelo nombre y el reparto no tenga; si está vacío, lee el narrador) y uno para cada **personaje**. Un personaje tiene un nombre y otros nombres con los que el texto puede referirse a él (*Tom* para *Tomáš Novák*), separados por comas. Un parlamento se asigna al personaje cuando su nombre, uno de los otros nombres, o una palabra del nombre que ningún otro personaje comparte, coincide, sin distinguir mayúsculas ni acentos. **Añadir los personajes del libro** rellena la lista con los personajes del punto de vista y los personajes presentes en las partes.

![El reparto de un libro](../../images/plugin-audiobook-cast.png)

**Pronunciación** es cómo se escribe el texto *para la voz*; el libro en sí nunca se modifica. El **diccionario** sustituye palabras completas (importan las mayúsculas) por lo que se dice en su lugar: una respelling para un nombre o una palabra inventada, o una lectura en kana para el japonés. Para un libro en inglés, las reglas también leen en voz alta otras cosas: números, ordinales, años, horas, dinero y abreviaturas como palabras; un título que sea solo un número, como *Chapter four. The Arch* (la palabra anterior al número es tuya), y las marcas de markdown como `*` se omiten, y un cambio de escena (`***`, `---`) no se lee, pero sí produce una pausa más larga. Desactiva las reglas para un libro en otro idioma; el diccionario sigue aplicándose. **Probar un texto** muestra cómo se leerá un fragmento de texto. Un fragmento cuyo texto hablado cambia se vuelve a generar; los demás se conservan.

![La pronunciación de un libro](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Convertir un libro

*Menú de herramientas del editor de historias (el botón de engranajes) → **Convertir a audio***.

![El diálogo antes de empezar](../../images/plugin-audiobook-convert.png)

1. Elige el **backend** y pulsa **Cargar**: se listan las partes con texto de la rama activa (la primera es la 1) con lo que tiene cada una:
   *No generada*, *Etiquetada* (se guardan las etiquetas del modelo, todavía sin audio), *Generada* (con un botón de reproducir y una descarga) o
   *Generada (el texto ha cambiado)* (el audio se hizo a partir de un texto anterior y se vuelve a hacer); a una parte que el estudio marca como final se le añade *(final)*.
2. Deja desmarcada **Etiquetar las partes con un modelo de lenguaje** para la versión sencilla: el narrador lee cada párrafo. Actívala para que el modelo decida quién habla en cada fragmento de diálogo, cómo (tranquilo, susurro, grito...) y dónde encaja una etiqueta de sonido, y el reparto proporciona las voces. Se usa el modelo del libro; elige otro si quieres. Primero se etiquetan las partes y después se leen. Una parte que ya tiene etiquetas para su texto se lee con ellas también cuando la casilla está desactivada (no se pregunta al modelo), así que volver a procesar un libro etiquetado sin la casilla no convierte sus diálogos en narración simple; **Quitar etiquetas** es la forma de pedir una narración simple o nuevas etiquetas.
3. Marca las partes (la casilla de la cabecera las marca todas) y **planifica** lo que hay que hacer con ellas; la columna **Plan** lo muestra y todavía no se hace nada:
   **Generar** (hace lo que falta; una parte que está al día no se vuelve a hacer), **Regenerar** (borra el audio, quita las etiquetas y genera), **Borrar audio** (los fragmentos y el audio unido de la parte; las etiquetas se mantienen), **Quitar etiquetas** (el modelo se vuelve a consultar la próxima vez que se haga la parte), **Limpiar el plan**. Los pasos se acumulan en una parte (Borrar audio y Quitar etiquetas sin Generar solo limpian); una parte marcada como final se deja fuera de todos los planes.
4. **Iniciar** ejecuta el plan. Cuando quita audio o etiquetas, pregunta antes. Lo que hay que generar pasa a tu cola, que sigue funcionando aunque cierres la ventana
   y cuando sales de la página; una parte que espera o se ejecuta en la cola queda fuera de un plan. El estado, los fragmentos hechos hasta el momento y el mensaje del trabajo se muestran en la fila de la parte.
   Mientras el modelo etiqueta una parte, su fila muestra *Etiquetando fragmentos 17-32 de 120* (se consulta al modelo unos 16 fragmentos cada vez); **haz clic en el estado** de una parte ya etiquetada para leer, en cada petición, el razonamiento del modelo y su respuesta, a medida que llegan. Se guardan en memoria junto con el trabajo, hasta que se borran los trabajos terminados (*Limpiar terminados*) o se reinicia la aplicación.

![La cola: fragmentos hechos hasta el momento, y una descarga cuando una parte está terminada](../../images/plugin-audiobook-queue.png)

Cómo se hace una parte, para que sepas qué esperar:

- El texto lo **divide el programa**, nunca el modelo, en fragmentos de narración y fragmentos de diálogo directo (entre comillas dobles rectas o curvas o corchetes japoneses; un encabezado es narración). El modelo solo *etiqueta* los fragmentos numerados. Un texto con etiquetas de sonido solo se usa cuando es el fragmento con las etiquetas añadidas y nada más ha cambiado, así que el modelo nunca puede cambiar tu libro.
- **Quién habla en un fragmento lo decide el modelo si es diálogo; la narración es del narrador.** Una carta, una nota, un mensaje o los pensamientos de un personaje no llevan comillas, así que el programa los considera narración. Un fragmento así lo lee el personaje (la voz del reparto) solo cuando **empieza en cursiva** (`*...*` o `_..._`) y el modelo nombra al personaje, a partir del contexto (una firma, «la letra era inconfundible»). La narración en tercera persona, también cuando se cuenta desde el punto de vista de un personaje («Ella observó cómo Percy cruzaba la habitación»), es siempre del narrador: el programa ignora un personaje que el modelo asigne a un fragmento sin cursiva. Las cursivas usadas para énfasis, sonidos o palabras extranjeras también son narración. Las palabras son siempre tuyas.
- **La instrucción para el modelo es una plantilla que puedes cambiar.** *Ajustes → Ajustes de la extensión → Ajustes del audiolibro → Prompt de etiquetado* es tu predeterminada; un libro (pestaña *Prompts* →
  *Ajustes de la extensión* → *Audiolibro*) o una carpeta pueden tener la suya propia. Vacía significa la de la carpeta superior, luego la tuya y, por último, la integrada. Se escribe como los otros [prompts](../books/prompts.md): las variables son `{{characters}}`, `{{deliveries}}`, `{{tags}}` y `{{wordsPerTag}}`, y el desplegable de sugerencias tiene **Insertar la plantilla predeterminada**. Una instrucción modificada hace que las etiquetas de las partes que haya que hacer se vuelvan a pedir la próxima vez que se hagan.
- **El volumen de las partes se nivela** cuando está instalado el ffmpeg del programa (el mismo que genera el `.m4b`): el audio unido de cada parte se lleva a un mismo nivel de sonoridad (EBU R128, lineal, para que se mantenga la dinámica; **-18 LUFS** salvo que lo cambies en *Ajustes → Ajustes de la extensión → Ajustes del audiolibro*, donde también puedes desactivarlo). Los fragmentos se conservan tal como los hizo el servidor; solo se nivela la parte unida, para que un audiolibro no salte de volumen de una parte a otra. **Las partes hechas antes** no se tocan hasta que las conviertas de nuevo: eso las vuelve a unir a partir de los fragmentos guardados y las nivela, y no pide nada al servidor de voz (tampoco descarga el modelo de lenguaje). Desactivar la nivelación deja como están las partes ya niveladas. Si ffmpeg falla con una parte, esta conserva su audio tal como estaba y el resultado lo indica.
- Todas las partes de un mismo clic se hacen en **un lote por modelo de voz**: las peticiones anteriores al lote se ejecutan una vez, después se leen todos los fragmentos y, por último, se ejecutan una vez las peticiones posteriores al lote. Así la tarjeta gráfica no cambia de modelo en cada parte.
- Cada fragmento se **guarda según su contenido** (texto, voz, modelo, idioma, ajustes, semilla). Una parte que ya está al día no cuesta nada, un párrafo cambiado es el único fragmento que se vuelve a hacer, y una parte que ha fallado conserva los fragmentos ya hechos cuando pulsas **Reintentar**.
- Los fragmentos se unen con pausas que dependen de su posición (después de un encabezado, entre párrafos, antes de *dijo él*) en un único mp3 por parte, que se guarda junto a la parte.
- Mientras se ejecuta una generación tuya, la voz espera, de modo que el modelo de lenguaje no se descarga debajo.
- Una parte que marcaste como **final** en el estudio se deja intacta, y el diálogo indica cuántas eran.
- **El audiolibro completo** une todas las partes que tienen audio al día en un único mp3, con una marca de capítulo para cada parte, y muestra un enlace de descarga debajo de la lista (se nombran las partes que se han dejado fuera). El mismo archivo es el formato de exportación *un mp3 con marcas de capítulo*.
- **Detener todo** cancela las partes en espera y detiene de inmediato la que se está ejecutando: la petición al modelo de lenguaje (el etiquetado) o al servidor de voz (el fragmento que se está leyendo) se descarta, sin esperar. Un lote detenido antes de empezar no envía las peticiones anteriores, así que el modelo de lenguaje sigue cargado. **Descargar** da el mp3 de una parte terminada.
<a id="the-audiobook-studio"></a>

## El estudio de audiolibros

*Menú Herramientas → **Audiobook studio*** (la voz de la pieza seleccionada, con una vista previa, está junto a su altavoz) es donde se hace el audiolibro final, una parte a la vez: ves el guion, lo cambias, escuchas una pieza y haces la parte.

![El estudio con una réplica seleccionada](../../images/plugin-audiobook-studio.png)

- Mientras la parte se está anotando o generando, una barra indica que algo está ocurriendo y los botones que iniciarían más trabajo esperan.
- **Anotar** crea el guion de la parte (con el modelo de lenguaje si lo pides, si no con el narrador) y muestra las piezas en una tabla: hablante, forma de decir, intensidad, el texto hablado y una marca para una pieza que cambiaste. Mientras se ejecuta aparece junto a él un botón **Detener** que termina la petición al modelo de lenguaje.
- Selecciona una pieza para editar su **hablante**, el **texto hablado**, la **forma de decir**, la **intensidad** (la exageración de la voz, en los modelos que la tienen), el **idioma**, la **semilla**, la **pausa después** de ella. **Aplicar** conserva el cambio y **bloquea** la pieza: una nueva anotación deja una pieza bloqueada tal como está.
  **Desbloquear** la devuelve a la anotación. *La petición que recibe el servidor de voz* muestra la petición exacta.
- **Escuchar** genera el audio de una pieza (solo esa pieza; el servidor de voz se arranca para ella); **Otra toma** la vuelve a generar con una nueva semilla, y las **tomas** se conservan para que elijas la que prefieras (*Usar esta toma y escucharla*). Una buena voz depende a menudo de la semilla.
- **Hacer la parte con este guion** une las piezas tal como están, sin una nueva anotación.
- El **estado** de la parte es *borrador*, *revisada* o *final*. Una parte final no se anota ni se vuelve a convertir, y la exportación puede limitarse a las partes que son finales.

<a id="exporting"></a>

## Exportar

El audiolibro es un formato de **Exportar la historia** del libro: **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** y, cuando ffmpeg está instalado, **Audiobook (one m4b with chapters)**. Se hacen con el audio de las partes que están al día: una parte que no tiene audio, o cuyo audio es más antiguo que su texto o que su guion en el estudio, se deja fuera y la ventana indica cuál. Los archivos mp3 llevan etiquetas ID3 (el título del libro, el capítulo, la pista), y el archivo único tiene marcas de capítulo que reproductores como VLC y la mayoría de aplicaciones de audiolibros muestran; el zip tiene una lista de reproducción. La casilla **Audiobook: only the parts marked final** limita la exportación a las partes que terminaste en el estudio.

El **m4b** es el formato que quieren Apple Books y la mayoría de aplicaciones de audiolibros del teléfono: audio AAC (64 kbit/s, suficiente para la voz) en un contenedor MP4 con los capítulos, el título, el autor y el género. Lo hace ffmpeg, así que el formato solo se ofrece en un servidor que tenga ffmpeg. El audio guardado como mp3 se codifica por segunda vez para ello; el audio guardado como wav se codifica una sola vez.

<a id="how-the-audio-is-stored"></a>

### Cómo se guarda el audio

Un administrador lo elige para toda la instalación en *Settings → Extension Settings → Audiobook Settings* (la última sección, que solo ven los administradores):

- **mp3** (por defecto): pequeño, se une sin decodificador, no necesita ffmpeg. El m4b es un segundo paso con pérdida.
- **wav**: sin pérdida, unas diez veces más grande (una hora de voz son unos 170 MB), necesita ffmpeg. Las piezas se piden al servidor de voz en wav y se unen como wav; el navegador las reproduce tal cual;
  las exportaciones mp3 y zip las codifica ffmpeg (96 kbit/s) y el m4b se codifica una sola vez.

La elección se aplica a lo que se genere desde entonces. Las partes generadas antes conservan su audio y se entienden tal como están; una parte de ambos tipos (una pieza bloqueada del estudio de antes) se une como wav, y una exportación de partes de ambos tipos funciona, pero necesita ffmpeg.

![El diálogo de exportación con los formatos de audiolibro](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Conversión automática

Un libro puede generar por sí mismo el audio de una parte nueva en cuanto la parte es final: tras la anotación automática, la reescritura y las traducciones si el libro las tiene, como último paso de [lo que sigue a una parte generada](../books/story-editor.md#what-follows-a-generated-part)
(en el panel de las ejecuciones hasta que se genera el audio; su **Detener** saca la parte de la cola). Está **desactivada** por defecto. Elígela en la pestaña *Prompts* del libro, en los ajustes de extensión, para una carpeta de libros, o para todos tus libros en los ajustes de audiolibro; gana la más cercana:

- **No**,
- **Sí: el narrador lee la parte**,
- **Sí: etiquetada por el modelo del libro, el reparto pone las voces**; un libro sin modelo recibe la narración simple, y se te avisa.

![El ajuste en un libro](../../images/plugin-audiobook-auto.png)

La parte entra en tu cola, con el reparto y la pronunciación del libro. Un aviso te dice que está en cola y otro que su audio está listo o ha fallado; solo se muestran en la página que inició la generación, y el trabajo aparece en cualquier caso en la ventana *Convert to audio*.

<a id="listening"></a>

## Escuchar

Un libro publicado (o el lector) que tiene partes con audio muestra una barra de reproducción: reproducir, pausar, detener, la velocidad, y la parte que se oye marcada en el texto. Empieza desde la selección o desde la primera parte visible, reproduce las partes en orden y descarga la siguiente mientras suena la actual, y los controles multimedia del dispositivo conocen el libro. No se ofrece el audio generado a partir de un texto anterior de una parte. El audio se sirve al propietario del libro y, mientras el libro esté publicado, a todos los que puedan abrirlo; un libro que se retira de la publicación deja de servirse de inmediato (en la siguiente parte, cuando el reproductor la cargue).

![El reproductor en el lector](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Una parte sin audio

El reproductor se detiene en una parte que no tiene audio, para que no se salte una parte de la historia, y lo indica. Cuando la extensión [Read Aloud](tts-reader.md) está instalada, la barra muestra también un botón de altavoz, **Read this part (and the next ones without audio) with the voice of the browser**. Nada se lee con una voz mecánica a menos que lo pulses. Entonces lee esa parte y las siguientes que no tienen audio con la voz de tu dispositivo, y cuando llega a una parte que tiene audio el reproductor vuelve a tomar el control. Las partes posteriores sin audio en la misma escucha se leen igual sin volver a preguntar; **Detener** la silencia, y volver a iniciar el reproductor vuelve a preguntar. La voz, la velocidad y el tono son los de Read Aloud.

<a id="commands"></a>

## Comandos

Las mismas cosas que [comandos](../commands/index.md) (la línea de comandos del editor de la historia, la macro `command` de una plantilla, un agente). Las partes se numeran como las numera el diálogo de conversión, o se nombran por su identificador.

| Comando | Qué hace |
|---|---|
| `/audiobook-status [from [to]]` | El servidor de voz en uso, la forma en que el libro se convierte por sí mismo, cuántas partes tienen audio (ninguno, listo, más antiguo que el texto, más antiguo que el guion), el estado de cada parte listada en el estudio, y la cola. Lento solo cuando el libro es largo. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Pone las partes en la cola (todo el libro sin argumentos, una parte cuando solo se da `from`). `labelling` `true` usa el modelo del libro; `what` es `audio` o `script` (solo el guion para el estudio, nada se lee en voz alta). Con `wait` `true` el comando espera a que terminen los trabajos, muestra cuánto falta y dice cómo terminó cada uno; detenerlo cancela los trabajos que no han empezado. |
| `/audiobook-stop` | Cancela las partes en espera; la que se está ejecutando se detiene tras su pieza. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` o `final` en el estudio; la parte necesita un guion. |
| `/audiobook-cast` | El narrador, el reserva y los personajes del reparto con sus actores. |

![El resultado de un comando](../../images/plugin-audiobook-command.png)

El audio se genera para **tu** libro, no para la copia de trabajo oculta de un [agente](../agents.md): un agente puede leer el reparto, y recibe una negativa de los demás comandos, que indica que tienes que ejecutarlos tú. Ejecútalos desde la línea de comandos, o encarga a un agente la tarea de preparar el libro (anotar, revisar) y conviértelo tú mismo.
<a id="notifications"></a>

## Notificaciones

Cuando termina un lote de conversiones, el navegador puede avisarte ([notificaciones](../account.md#notifications), para que puedas dejar la pestaña abierta mientras el servidor de voz trabaja). En la lista de notificaciones de la página de *Ajustes* hay dos líneas: **Convert to audio** (los lotes que inicias en el diálogo o con el comando) y **Convert to audio (automatic)** (una parte nueva que se convirtió en audio por sí sola). Un lote que detuviste no notifica; un lote con alguna parte fallida lo indica.

![La lista de notificaciones](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Lo que todavía no hace

- Solo mp3 y wav (no opus); el volumen de las piezas es el que dan las voces.
- No hay efectos de sonido ni ambientes.
- Un solo motor de voz, Chatterbox. Otros motores son el siguiente paso.
- La audición de una voz fuera de un segmento.
- El reparto no forma parte de la copia de seguridad de los *actores* de un libro: un libro restaurado conserva su reparto, pero nombra actores que deben existir en tu cuenta.
