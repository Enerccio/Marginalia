---
label: Audiolibro
order: 27
---

# Audiolibro

Convierte un libro en audiolibro, parte a parte, con un servidor de texto a voz: una voz para el narrador y una para cada personaje, guardadas junto a las partes del libro y reproducidas en el lector. Puedes dejar que funcione solo (el modelo indica quién habla y cómo, y el servidor de voz lee) para un borrador rápido, o trabajar las partes una a una en el **Audiobook studio**, viendo exactamente lo que se envía al servidor de voz y cambiándolo a mano, y exportar el resultado como archivos mp3 con marcas de capítulo.

La extensión no genera la voz por sí misma: se comunica con un **Chatterbox-TTS-Server** que tú ejecutas (véase [Configurar el servidor de voz](audiobook-server.md)). El texto del libro nunca se modifica ni se envía a nadie más que a los servidores que hayas indicado; el audio se guarda en tu propia instalación, como recursos vinculados a las partes.

<a id="what-you-need"></a>

## Qué necesitas

- Un servidor de voz y, si comparte la tarjeta gráfica con tu modelo de lenguaje, las peticiones que le hacen sitio y lo detienen de nuevo ([Configurar el servidor de voz](audiobook-server.md)).
- Voces: un **clip de referencia** breve (de 12 a 30 segundos de una sola persona, sin música) para cada voz que quieras clonar, o uno de los archivos de voz que tenga el servidor. Un narrador necesita una voz de lectura tranquila y constante.
- Para un libro con varias voces, un modelo de lenguaje que pueda etiquetar el texto. Por defecto es el modelo del libro.
- **ffmpeg** es opcional. Sin él, todo funciona con mp3. Con él en el servidor (está incluido en la imagen de Docker; en un equipo de escritorio se encuentra en `PATH` o en carpetas habituales como `/opt/homebrew/bin`; la variable `MARGINALIA_FFMPEG` indica dónde está si se encuentra en otra ruta) la exportación obtiene el formato **m4b**, y un administrador puede guardar el audio como wav.

<a id="the-settings"></a>

## Los ajustes

*Ajustes → Extension Settings → Audiobook Settings* es el panel de tu cuenta; guarda la página de *Ajustes* después de cambiarlo.

![El panel con un backend y los actores](../../images/plugin-audiobook-settings.png)

### Backends

Un backend es un servidor de voz y el modelo con el que habla. Añade uno con **+**. **Duplicate** copia el seleccionado (dirección, claves, bloqueo de GPU, voz, valores por defecto y las peticiones antes y después de un lote) con un nombre nuevo: es la forma rápida de tener un backend de otro modelo en la misma máquina, por ejemplo Higgs junto a Chatterbox, donde después cambias el modelo, la dirección y las peticiones de inicio y parada.

| Ajuste | Qué hace |
|---|---|
| Name | El nombre que se muestra en el diálogo Convert. |
| Address | La dirección base del servidor de voz, o de lo que lo reenvía, por ejemplo `http://localhost:8004`. |
| Api key | Se envía como token de portador si el servidor (o tu proxy) lo pide. Se guarda cifrada y no se vuelve a mostrar; escribe una nueva para reemplazarla, o usa **Remove the key**. |
| Speech model | **Chatterbox Turbo** (inglés, con etiquetas de sonido como `[sigh]`, el más rápido y el predeterminado), **Chatterbox** (inglés, con una *exaggeration* de la entonación), **Chatterbox Multilingual** (23 idiomas) o **Higgs Audio v3 in ComfyUI** (muchos idiomas, voces que siguen el estado de ánimo de cada línea y sonidos como llanto, jadeos o gemidos; véase [más abajo](#higgs-audio-v3-emotions-and-sounds)). El [bloqueo de GPU](../administration/gpus.md) del backend (o las peticiones antes de un lote) debe dejar cargado este modelo. Un libro que necesite otro modelo usa otro backend. |
| GPU lock | El [bloqueo de GPU](../administration/gpus.md) de la GPU en la que se ejecuta el servidor, cuando la comparte con el modelo de lenguaje u otros servidores (también con el [Image Generator](image-generator.md)): cada lote y cada muestra espera su turno en la GPU, y el bloqueo carga primero el servidor de voz. Solo aparece si el administrador configuró los bloqueos de GPU. |
| The server is on this machine or on my network | Permite direcciones de la red local. Desactívalo para un servidor en la nube: así no puede hacer que llame a tu propia máquina. |
| Timeout of a request | Cuánto tiempo esperar a un fragmento de voz. |
| Restart the server after (pieces) | Para un servidor de voz que pierde memoria y que el sistema termina en un lote largo: tras este número de fragmentos se vuelven a hacer las peticiones posteriores al lote y luego las anteriores, en mitad del lote (detener, iniciar, cargar), y el trabajo continúa con un servidor nuevo. 0 (el valor por defecto) nunca lo hace. Un servidor que se cae se vuelve a iniciar independientemente de este ajuste; véase [Solución de problemas](audiobook-server.md#troubleshooting). |
| Key of the requests around a batch | La clave que las peticiones antes y después de un lote envían como `Authorization: Bearer`. Se guarda cifrada y no se vuelve a mostrar (**Remove the key** la borra), así no tiene que escribirse en las cabeceras de cada petición, donde sería legible. Vacío: se usa *Api key* de arriba, que es lo que quieres cuando un proxy como LiteLLM sirve todo. Una petición que tenga su propia cabecera `Authorization` conserva la suya. |
| Narrator voice | El narrador de un libro que no tiene reparto. Un archivo de voz del servidor se escribe `predefined:Henry.wav`. |
| Precision | Solo para Higgs: la precisión con la que calcula el modelo. **fp32** (el valor por defecto) es la opción segura: en una tarjeta AMD, **bf16** convirtió los fragmentos en galimatías, efectos de sonido y voces erróneas. bf16 necesita aproximadamente la mitad de la memoria gráfica (unos 8 GB en lugar de 16) y es más rápido donde funciona. |
| Send the text of the clip to Higgs | Solo para Higgs, experimental: el [texto del clip](#voices-and-clips) se envía junto con él. Déjalo desactivado: en las pruebas cambió la voz. |
| Check the length of the pieces | Activado por defecto. Un fragmento mucho más largo que su texto (4 s más 0,6 s por palabra: el modelo siguió hablando, o hizo un sonido en lugar de las palabras) se vuelve a generar con la siguiente semilla, hasta **Reintentos** veces; se conserva la primera toma de duración correcta; si no hay ninguna, la más corta. |
| Check the words with speech recognition | Desactivado por defecto; necesita el [reconocimiento de voz](#speech-recognition). Tras cada lote, cada fragmento se transcribe y se compara con su texto. Un fragmento que se aleja más del **Max. word errors (%)** de sus palabras se vuelve a generar con la siguiente semilla y se vuelve a comprobar, hasta **Reintentos** rondas; se conserva la toma con menos palabras erróneas. La conversión indica cuántos fragmentos se generaron de nuevo y cuántos siguen sin coincidir. |
| Defaults of a segment | Temperatura, peso de CFG, velocidad y tamaño de fragmento con los que empieza cada fragmento (el studio puede cambiarlos para uno solo). Higgs usa solo la temperatura. |
| Before a batch, After a batch | Peticiones que se hacen antes y después de cada lote de partes (y de cada muestra), dentro del bloqueo de GPU. Liberar la tarjeta gráfica e iniciar el servidor de voz es tarea del bloqueo de GPU; véase [Una GPU para el modelo y la voz](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Vacío no hace nada. |

**Test connection** hace lo mismo que un lote: toma el bloqueo de GPU (así se inicia un servidor de voz que esté detenido), hace las peticiones previas a un lote, pregunta al servidor si está disponible y tiene un modelo cargado, y hace las peticiones posteriores al lote. Sin bloqueo ni peticiones, solo pregunta.

Un libro que tenga fragmentos en otro alfabeto (japonés en un libro en inglés) los envía al modelo multilingüe, si tienes un backend para él, con su idioma; si no tienes ninguno, se quedan con el modelo del libro. Un libro de Higgs los conserva: Higgs los lee él mismo, con la misma voz.
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3: emociones y sonidos

Con el backend **Higgs Audio v3 en ComfyUI** se oyen las etiquetas del modelo de lenguaje. La dirección del backend es la de tu ComfyUI
(con los nodos de TTS Audio Suite, véase [Configurar el servidor de voz](audiobook-server.md#higgs-audio-v3-in-comfyui)).

- La **entrega** de un trozo de habla se pronuncia: triste, enfadado, alegre, divertido, tenso, asustado, sorprendido, tierno, sarcástico, suplicante, desesperado,
  disgustado, excitado, un susurro o un grito. Una entrega fuerte (intensidad 0,8 o más) es más expresiva; una débil (menos de 0,2) se lee con sencillez.
  El narrador lee con sencillez.
- El modelo de lenguaje puede poner más **sonidos** en el habla que para Chatterbox Turbo: risa, risita, carcajada, suspiro, jadeo, tos, carraspeo,
  resoplido, estornudo, gemido, llanto, sollozo, grito, resuello, gemido de placer, tarareo y chistido.
- El programa también lee la **narración que rodea a cada habla** (en inglés) y da al habla el sonido que nombra, allí donde el modelo no puso ninguno:
  *"You look funny," she giggled.* recibe una risita, *She burst out laughing. "Careful!"* una carcajada, *a soft moan escaped her lips. "Yes..."* un gemido,
  *panting* o *out of breath* un resuello, *cried quietly* o *burst into tears* un llanto. Deja fuera lo que solo parece un sonido: *"Stop!" she cried.*
  (una exclamación), *moaned about the weather*, *the wind moaned*, *the floorboards groaned*, *stifled a giggle*, *didn't laugh*, y un sonido que la narración
  atribuye a otro personaje con nombre. *"You look funny," he mocked her.* recibe una entrega sarcástica o divertida y ningún sonido. La narración en sí se lee tal como está escrita.
- **El narrador siempre se lee con sencillez**: sin sonidos y sin estado de ánimo, también en un habla que el modelo dejó al narrador.
- Higgs no necesita un clip por estado de ánimo: basta un clip de una voz. Copia la grabación del clip, así que una grabación limpia importa más que con Chatterbox
  ([Un buen clip](#a-good-clip)).
- Cada trozo lo genera ComfyUI y su memoria se vacía después (unos segundos por trozo), lo que evita que algunas tarjetas gráficas se bloqueen.
- Higgs suele seguir hablando después de la última palabra de un trozo (un aliento, el comienzo de otro sonido) y el clip termina a mitad de ello, lo que se oye como
  un breve "zas" antes de la pausa. Con **ffmpeg**, la extensión corta esa cola y desvanece cada trozo; sin ella, los trozos quedan tal como los generó Higgs.
- El modelo Higgs Audio v3 tiene una licencia **de investigación y no comercial**: un audiolibro que vendas necesita una licencia de Boson AI.

<a id="speech-recognition"></a>

### Reconocimiento de voz

El **reconocimiento de voz** escribe lo que dice un trozo de habla. Revisa los trozos de un lote (cuando un backend tiene **Comprobar las palabras con reconocimiento de voz**)
y reconoce el [texto de un clip de voz](#voices-and-clips). Elige dónde se ejecuta:

| Ajuste | Qué hace |
|---|---|
| Reconocimiento de voz | **ComfyUI (Granite ASR)**: el modelo de voz Granite de TTS Audio Suite en tu ComfyUI (la dirección es la de ComfyUI). **Compatible con OpenAI (whisper)**: cualquier servidor con `/v1/audio/transcriptions`, como un servidor whisper (faster-whisper, speaches) o la API de OpenAI. |
| Dirección, Clave de API | La dirección de ComfyUI, o la base de la API (con o sin `/v1`); la clave se guarda cifrada. Dirección vacía: sin reconocimiento de voz. |
| Bloqueo de GPU | El [bloqueo de GPU](../administration/gpus.md) que usa el reconocimiento. Con un Higgs en fp32 en una sola tarjeta, dale un bloqueo propio cuyos pasos reinicien ComfyUI: ComfyUI mantiene el modelo de voz en la memoria del servidor, y cargar el reconocimiento junto a él dejó el servidor sin memoria. |
| Modelo del reconocimiento | Vacío: `granite-speech-4.1-2b` para ComfyUI, `whisper-1` para el compatible con OpenAI. |
| Tiempo de espera, El servidor está en esta máquina o en mi red | Igual que para un backend. |

**Probar conexión** comprueba que ComfyUI tiene los nodos, o que el servidor responde. Los trozos de un lote se revisan después del lote, cuando se devuelve el servidor de voz,
de modo que el reconocimiento y el modelo de voz nunca necesitan la tarjeta a la vez.

<a id="shared-speech-servers"></a>

### Servidores de voz compartidos

En una instalación con varios usuarios, el administrador puede mantener servidores de voz para todos, de modo que una máquina con GPU se configura una vez y nadie necesita una dirección ni una clave. El administrador lo hace en el mismo panel (las últimas secciones, visibles solo para administradores): **Servidores de voz compartidos** abre una ventana con los mismos campos que un backend propio, y además quién puede usarlo: **todos los usuarios**, o los usuarios elegidos en la lista. Un interruptor ahí, **Los usuarios pueden añadir servidores de voz propios**, decide si siguen permitidos los backends de abajo.

Lo que obtienes:

- **Backend en uso**, en la parte superior del panel, enumera los servidores compartidos que puedes usar (marcados *(compartido)*) y los tuyos. La ventana *Convertir a audio* y el estudio usan el que elijas; los comandos también.
- Solo ves el nombre de un servidor compartido, nunca su dirección, claves ni peticiones. Los lotes de todos los usuarios en una misma dirección de servidor esperan unos a otros, de modo que dos usuarios no pueden arrancar la misma GPU a la vez.
- Cuando el administrador te quita un servidor, un trabajo que aún lo nombra se rechaza; elige otro.
- Cuando el administrador desactiva tus servidores propios, el panel lo indica. Los que añadiste se conservan y se vuelven a usar si el interruptor se reactiva.
- La *Voz del narrador* de un servidor compartido debe ser un archivo de voz del servidor (`predefined:Henry.wav`): un clip de referencia es un recurso de un solo usuario y los demás no pueden leerlo. Las voces de tus actores funcionan como siempre.

<a id="actors"></a>

### Actores

Un **actor** es una voz tuya: un clip de referencia (uno de tus [recursos](../resources.md) de audio, o uno nuevo subido aquí) o un
archivo de voz del servidor, con voces propias para un **estado de ánimo** (un susurro, un grito) o un **idioma** (un clip que habla
japonés). El idioma tiene prioridad sobre el estado de ánimo, y sin una voz para ellos lee la voz del actor. Tus actores se guardan en
tu cuenta; cualquier libro puede asignar un actor a su narrador y a sus personajes.

**Vista previa** junto a una voz (en el actor, los ajustes y el reparto) tiene unas pocas frases dichas con ella (una breve, en el idioma de la voz si es de un idioma) y la reproduce; cuando la GPU tiene cargado otra cosa, el bloqueo de GPU carga primero el servidor de voz. La flecha junto a ella ofrece otras muestras: **con sonidos** (un suspiro, una risa, un jadeo) para los modelos que las tienen (Chatterbox Turbo, Higgs Audio v3), y para Higgs todos los **estados de ánimo** del etiquetado (triste, susurrado, sarcástico...). Los actores aparecen por su nombre.

Cada muestra toma la GPU solo para sí. La GPU sigue cargada con el servidor de voz después de una muestra, así que las siguientes muestras llegan al instante, hasta que una petición del modelo de lenguaje (u otro servidor) necesite la GPU. Abrir **Voces y clips** carga ya el servidor de voz, de modo que su primera muestra no espera.

![Un actor con una voz para un estado de ánimo](../../images/plugin-audiobook-actor.png)
<a id="voices-and-clips"></a>

### Voces y clips

**Voices and clips** (junto a los actores) es una única ventana para tus actores y los clips de los que se clonan sus voces:

- **Actors**: cada uno con su voz, cuántas otras voces (estados de ánimo, idiomas) tiene y la peor calidad de sus clips; añade, edita o elimina uno.
- **Clips**: cada uno con su duración, su **quality** y dónde se usa (actores, con el estado de ánimo o el idioma, y los narradores de tus backends).
  Selecciona un clip para leer qué le pasa, **Reproducir** para escucharlo, **Replace...** para sustituirlo, **Clean with ffmpeg** (con ffmpeg) o eliminarlo (solo cuando nada lo usa).
- **Replace...** pone otro clip, uno tuyo o uno que subas, en todos los lugares donde se usa el clip. Las partes se vuelven a generar con la nueva voz;
  el clip anterior se conserva hasta que lo elimines.
- **Text of the clip**: las palabras dichas en el clip seleccionado. Escríbelas, o pulsa **Recognize** para que el [reconocimiento de voz](#speech-recognition)
  las transcriba, corrígelas y **Guardar**. El diálogo del actor muestra el mismo campo para el clip de la voz del actor. Una copia limpia y un reemplazo
  heredan el texto. Higgs solo lo recibe cuando el backend lo envía (desactivado por defecto).
- **Clean with ffmpeg** crea una copia limpia junto al clip (menos siseo y zumbido constantes), medida de nuevo, para reproducirla y compararla; sustituye el clip por ella si suena mejor. No elimina la música, los efectos ni otras voces.

<a id="a-good-clip"></a>

#### Un buen clip

Un modelo de voz copia la **grabación**, no solo la voz: la habitación, un siseo, la música bajo una escena de película reaparecen en cada línea, a menudo como un suave "soplido" bajo las palabras que ningún filtro del resultado elimina. Por eso:

- Usa una grabación limpia de un solo hablante con pausas reales (una muestra de un actor de doblaje, una lectura hecha en casa en una habitación tranquila), de 12 a 30 segundos. Un clip recortado de una película o una serie es el peor caso.
- Las marcas de calidad se miden a partir del clip: **Clean** (las pausas están al menos a 45 dB por debajo de la voz), **Fair** (de 35 a 45 dB, o más largo de 30 s, que Chatterbox recorta), **Poor** (sonido de fondo, por debajo de 35 dB; distorsionado; más corto de 8 s). Un clip que no esté limpio se avisa al añadirlo.
- Para el mejor resultado, limpia un clip a mano: la reducción de ruido de [Audacity](https://www.audacityteam.org/) con un perfil de ruido tomado de una pausa del clip, o un eliminador de voz como [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) para un clip con música. La limpieza con ffmpeg solo reduce un ruido constante: en un clip de película ganó unos 3 dB en la prueba y el clip siguió siendo *Poor*.

<a id="converting-new-parts-by-itself"></a>

### Convertir las partes nuevas automáticamente

*Convert new parts to audio by itself* es la opción por defecto para todos tus libros; consulta [Conversión automática](#automatic-conversion).

<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## El reparto y la pronunciación de un libro

Ambos se guardan con el libro (viajan en su copia de seguridad) y se editan desde el diálogo **Convert to audio**.

**Cast** indica quién lee: un actor para el **narrador**, otro para **otros hablantes** (cualquiera que nombre el modelo y que el reparto no tenga; vacío: lo lee el narrador) y uno para cada **personaje**. Un personaje tiene un nombre y otros nombres que el texto puede usar para él (*Tom* para *Tomáš Novák*), separados por comas; un parlamento se asigna al personaje cuando su nombre, uno de los otros nombres o una palabra del nombre que no comparte ningún otro personaje coincide, sin distinguir mayúsculas ni tildes. **Add the characters of the book** rellena la lista con los personajes de punto de vista y presentes en las partes.

![El reparto de un libro](../../images/plugin-audiobook-cast.png)

**Pronunciation** es cómo se escribe el texto *para la voz*; el libro en sí nunca se modifica. El **diccionario** reemplaza palabras completas (distingue mayúsculas) por lo que se dice en su lugar: una reescritura fonética para un nombre o una palabra inventada, o una lectura en kana para el japonés. Para un libro en inglés, las reglas también leen en voz alta otras cosas: números, ordinales, años, horas, dinero y abreviaturas como palabras, un encabezado que es solo un número como *Chapter four. The Arch* (la palabra antes del número la pones tú), las marcas de markdown como `*` se omiten, y un cambio de escena (`***`, `---`) no se lee, sino que es una pausa más larga. Desactiva las reglas para un libro en otro idioma; el diccionario sigue aplicándose. **Try a text** muestra cómo se leerá un fragmento de texto. Un fragmento cuyo texto hablado cambie se vuelve a generar; los demás se conservan.

![La pronunciación de un libro](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Convertir un libro

*Menú de herramientas del editor de historias (el botón de engranajes) → **Convertir a audio***.

![El diálogo antes de iniciar](../../images/plugin-audiobook-convert.png)

1. Elige el **backend** y pulsa **Load**: se listan las partes con texto de la rama activa (la primera es la 1) con su estado:
   *No generada*, *Etiquetada* (se guardan las etiquetas del modelo, todavía sin audio), *Generada* (con un botón de reproducción y una descarga) o
   *Generada (el texto cambió)* (el audio se hizo a partir de un texto anterior y se vuelve a hacer); *(final)* se añade a una parte que el estudio marca como final.
2. Deja **Label the parts with a language model** desactivada para la versión sencilla: el narrador lee cada párrafo. Actívala para que el modelo decida quién habla en cada fragmento de diálogo, cómo (tranquilo, susurrando, gritando...) y dónde encaja una etiqueta de sonido, y el reparto asigna las voces. El modelo del libro está elegido; elige otro si lo prefieres. Las partes se etiquetan primero y luego se leen en voz alta. Una parte que ya tiene etiquetas para su texto se lee también con ellas cuando la casilla está desactivada (no se le pregunta al modelo), así que volver a ejecutar un libro etiquetado sin la casilla no convierte sus diálogos en narración simple; **Remove labels** es la forma de pedir narración simple o nuevas etiquetas.
3. Marca las partes (la casilla del encabezado las marca todas) y **plan** qué hacer con ellas; la columna **Plan** lo muestra, y todavía no se hace nada:
   **Generar** (genera lo que falta; una parte que está al día no se vuelve a generar), **Regenerar** (borra el audio, quita las etiquetas y genera), **Delete audio** (los fragmentos y el audio unido de la parte; las etiquetas se mantienen), **Remove labels** (se vuelve a preguntar al modelo la próxima vez que se genere la parte), **Clear the plan**. Los pasos se suman en una parte (Delete audio y Remove labels sin Generate solo limpian); una parte marcada como final se excluye de todos los planes.
4. **Iniciar** ejecuta el plan. Cuando quita audio o etiquetas, pregunta antes. Lo que haya que generar pasa a tu cola, que sigue funcionando cuando cierras la ventana y cuando sales de la página; una parte que espera o se ejecuta en la cola no se incluye en un plan. El estado, los fragmentos ya hechos y el mensaje del trabajo aparecen en la fila de la parte.
   Mientras el modelo etiqueta una parte, su fila muestra *Labelling pieces 17-32 of 120* (se pregunta al modelo por 16 fragmentos a la vez); **haz clic en el estado** de una parte etiquetada para leer, en cada petición, el razonamiento del modelo y su respuesta, según van llegando. Se guardan en memoria junto con el trabajo, hasta que se borran los trabajos terminados (*Clear finished*) o se reinicia la aplicación.

![La cola: fragmentos hechos hasta ahora, y una descarga cuando una parte está terminada](../../images/plugin-audiobook-queue.png)

Cómo se genera una parte, para que sepas qué esperar:

- El texto lo **divide el programa**, nunca el modelo, en fragmentos de narración y fragmentos de diálogo directo (entre comillas dobles rectas o curvas, o entre corchetes japoneses; un encabezado es narración). El modelo solo *etiqueta* los fragmentos numerados. Un texto con etiquetas de sonido solo se usa si es el fragmento con las etiquetas añadidas y nada más ha cambiado, así que el modelo nunca puede cambiar tu libro.
- **Quién habla en un fragmento lo decide el modelo para el diálogo; la narración es del narrador.** Una carta, una nota, un mensaje o los pensamientos de un personaje no llevan comillas, así que el programa los considera narración. Un fragmento así lo lee el personaje (la voz del reparto) solo cuando **empieza en cursiva** (`*...*` o `_..._`) y el modelo nombra al personaje a partir del contexto (una firma, "la letra era inconfundible"). La narración en tercera persona, también cuando se cuenta desde el punto de vista de un personaje ("Ella vio cómo Percy cruzaba la habitación"), es siempre del narrador: el programa ignora el personaje que el modelo asigne a un fragmento sin cursiva. La cursiva para el énfasis, los sonidos o las palabras extranjeras también es narración. Las palabras siempre son las tuyas.
- **La instrucción al modelo es una plantilla que puedes cambiar.** *Settings → Ajustes de la extensión → Audiobook Settings → Labelling prompt* es tu predeterminada; un libro (pestaña *Prompts* → *Ajustes de la extensión* → *Audiobook*) o una carpeta pueden tener la suya propia. Vacío significa usar la de la carpeta superior, luego la tuya y, por último, la integrada. Se escribe igual que los demás [prompts](../books/prompts.md): las variables son `{{characters}}`, `{{deliveries}}`, `{{tags}}` y `{{wordsPerTag}}`, y el desplegable de sugerencias tiene **Insertar la plantilla predeterminada**. Si cambias la instrucción, las etiquetas de las partes se volverán a pedir la próxima vez que se generen.
- **El volumen de las partes se nivela** cuando está instalado el programa ffmpeg (el mismo que genera `.m4b`): el audio unido de cada parte se lleva a un mismo volumen (EBU R128, lineal, así que la dinámica se mantiene; **-18 LUFS** salvo que lo cambies en *Settings → Ajustes de la extensión → Audiobook Settings*, donde también puedes desactivarlo). Los fragmentos se conservan tal como los generó el servidor; solo se nivela la parte unida, de modo que un audiolibro no cambia de volumen de una parte a otra. **Las partes generadas antes** no se tocan hasta que las conviertas de nuevo: entonces se vuelven a unir a partir de los fragmentos guardados y se nivelan, y no se le pide nada al servidor de voz (tampoco se descarga el modelo de lenguaje). Desactivar el nivelado deja como están las partes que ya se nivelaron. Si ffmpeg falla con una parte, esta conserva su audio tal como estaba y el resultado lo indica.
- Todas las partes de un mismo clic se generan en **un lote por modelo de voz**: las peticiones previas al lote se ejecutan una vez, luego se hablan todos los fragmentos y después se ejecutan una vez las peticiones posteriores al lote. Esto evita que la tarjeta gráfica cambie de modelo en cada parte.
- Cada fragmento se **guarda según su contenido** (texto, voz, modelo, idioma, ajustes, semilla). Una parte que ya está al día no cuesta nada, un párrafo cambiado es el único fragmento que se vuelve a generar, y una parte que falló conserva los fragmentos ya hechos cuando pulsas **Retry**.
- Los fragmentos se unen con pausas que dependen de su posición (después de un encabezado, entre párrafos, antes de *dijo*) en un mp3 por parte, que se guarda junto a ella.
- Mientras se ejecuta una generación tuya, la voz espera, de modo que el modelo de lenguaje no se descarga bajo ella.
- Una parte que marcaste como **final** en el estudio se deja intacta, y el diálogo indica cuántas hay.
- **El audiolibro completo** une todas las partes con audio al día en un único mp3 con una marca de capítulo por parte y muestra un enlace de descarga bajo la lista (se nombran las partes que se omiten). El mismo archivo es el formato de exportación *one mp3 with chapter marks*.
- **Stop all** cancela las partes en espera y detiene de inmediato la que se ejecuta: la petición al modelo de lenguaje (el etiquetado) o al servidor de voz (el fragmento que se está hablando) se descarta, sin esperar. Un lote detenido antes de empezar no envía las peticiones anteriores a él, así que el modelo de lenguaje se mantiene cargado. **Descargar** da el mp3 de una parte terminada.
<a id="the-audiobook-studio"></a>

## El estudio de audiolibros

*Menú Tools → **Audiobook studio*** (la voz de la pieza seleccionada, con una vista previa, está junto a su locutor) es donde se crea el audiolibro final, una parte a la vez: ves el guion, lo cambias, escuchas una pieza y generas la parte.

![El estudio con una pieza seleccionada](../../images/plugin-audiobook-studio.png)

- Mientras la parte se anota o se genera, una barra muestra que algo está en curso, y los botones que iniciarían más trabajo esperan.
- **Anotar** genera el guion de la parte (con el modelo de lenguaje si lo pides, si no con el narrador) y muestra las piezas en una tabla: locutor, entrega, intensidad, el texto hablado y una marca para una pieza que hayas cambiado. Mientras se ejecuta, aparece junto a él un botón **Detener** que termina la petición al modelo de lenguaje.
- Selecciona una pieza para editar su **speaker**, el **spoken text**, la **delivery**, la **intensity** (la exageración de la voz, para los modelos que la tienen; en Higgs, la **delivery** y la **intensity** son las marcas como `<|emotion:sadness|>` al inicio del **spoken text**, que las siguen cuando las cambias), el **language**, la **seed**, el **pause after**. **Aplicar** mantiene el cambio y **bloquea** la pieza: una nueva anotación deja una pieza bloqueada como está. **Unlock** la devuelve a la anotación. *The request the speech server gets* muestra la petición exacta.
- **Hear** genera el audio de una pieza (solo esa pieza; el servidor de voz se inicia para ella). **Another take** lo genera de nuevo con una nueva **seed**, y las **tomas** se guardan para que elijas la que te guste (*Use this take and hear it*). Una buena voz depende a menudo de la **seed**.
- **Make the part from this script** une las piezas tal como están, sin una nueva anotación.
- El **status** de la parte es *draft*, *reviewed* o *final*. Una parte final no se vuelve a anotar ni a convertir, y la exportación puede limitarse a las partes que son finales.

<a id="exporting"></a>

## Exportación

El audiolibro es un formato de *Exportar la historia* del libro: **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** y, cuando ffmpeg está instalado, **Audiobook (one m4b with chapters)**. Se generan con el audio de las partes que están al día: una parte que no tiene audio, o cuyo audio es más antiguo que su texto o que su guion en el estudio, se deja fuera, y la ventana indica cuáles. Los archivos mp3 llevan etiquetas ID3 (el título del libro, el capítulo, la pista), y el archivo único tiene marcas de capítulo que reproductores como VLC y la mayoría de apps de audiolibros muestran; el zip tiene una lista de reproducción. La casilla **Audiobook: only the parts marked final** limita la exportación a las partes que terminaste en el estudio.

El **m4b** es el formato que quieren Apple Books y la mayoría de apps de audiolibros del teléfono: audio AAC (64 kbit/s, suficiente para la voz) en un contenedor MP4 con los capítulos, el título, el autor y el género. ffmpeg lo genera, así que el formato solo se ofrece en un servidor que tenga ffmpeg. El audio guardado como mp3 se codifica por segunda vez para ello; el audio guardado como wav se codifica una sola vez.

<a id="how-the-audio-is-stored"></a>

### Cómo se guarda el audio

Un administrador lo elige para toda la instalación en *Settings → Extension Settings → Audiobook Settings* (la última sección, que solo ven los administradores):

- **mp3** (predeterminado): pequeño, se une sin decodificador, no necesita ffmpeg. El m4b es un segundo paso con pérdida.
- **wav**: sin pérdida, unas diez veces más grande (una hora de voz ocupa unos 170 MB), necesita ffmpeg. Las piezas se piden al servidor de voz como wav y se unen como wav; el navegador lo reproduce tal cual; las exportaciones mp3 y zip las codifica ffmpeg (96 kbit/s) y el m4b se codifica una sola vez.

La elección se aplica a lo que se genere a partir de entonces. Las partes generadas antes conservan su audio y se tratan tal cual; una parte de ambos tipos (una pieza bloqueada del estudio de antes) se une como wav, y una exportación de partes de ambos tipos funciona, pero necesita ffmpeg.

Una exportación de todo el libro se hace a partir de los archivos guardados de las partes, en archivos temporales del servidor, y se descarga desde allí; así que su duración no está limitada por la memoria de la aplicación, pero la carpeta temporal necesita espacio para unas dos veces el audio del libro (con wav, unos 350 MB por hora) mientras se genera.

![El diálogo de exportación con los formatos de audiolibro](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Conversión automática

Un libro puede generar por sí mismo el audio de una parte nueva, en cuanto la parte esté final: después de la anotación automática, la nueva redacción y las traducciones si el libro las tiene, como último paso de [lo que sigue a la parte](../books/story-editor.md#what-follows-a-generated-part) (en el panel de las ejecuciones hasta que se genere el audio; su **Detener** saca la parte de la cola). Viene **desactivada** por defecto. Elígela en la pestaña *Prompts* del libro, en la configuración de extensiones, para una carpeta de libros, o para todos tus libros en la configuración de audiolibros; gana la más cercana:

- **No**,
- **Yes: the narrator reads the part**,
- **Yes: labelled by the model of the book, the cast gives the voices**; un libro sin modelo recibe la narración simple, y se te avisa.

![La opción en un libro](../../images/plugin-audiobook-auto.png)

La parte entra en tu cola, con el reparto y la pronunciación del libro. Un aviso te dice que está en cola y otro que su audio está listo o ha fallado; solo se muestran en la página que inició la generación, pero el trabajo aparece en todo caso en la ventana *Convert to audio*.

<a id="listening"></a>

## Escuchar

Un libro publicado (o el lector) que tenga partes con audio obtiene una barra de reproducción: reproducir, pausar, detener, la velocidad, y la parte que se escucha marcada en el texto. Empieza desde la selección o desde la primera parte visible, reproduce las partes en orden y carga la siguiente mientras suena la actual, y los controles multimedia del dispositivo conocen el libro. No se ofrece el audio generado a partir de una versión anterior del texto de una parte. El audio se sirve al propietario del libro y, mientras el libro esté publicado, a cualquiera que pueda abrirlo; un libro que se retira de la publicación deja de servirse de inmediato (en la siguiente parte que cargue el reproductor).

![El reproductor en el lector](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Una parte sin audio

El reproductor se detiene en una parte que no tiene audio, para que no se salte una parte de la historia, y lo indica. Cuando está instalada la extensión [Read Aloud](tts-reader.md), la barra muestra además un botón de altavoz, **Read this part (and the next ones without audio) with the voice of the browser**. No se lee nada con una voz de máquina si no lo pulsas. Entonces lee esa parte y las siguientes que no tengan audio con la voz de tu dispositivo, y cuando llega a una parte que tiene audio, el reproductor vuelve a tomar el control. Las partes posteriores sin audio en la misma escucha se leen del mismo modo sin volver a preguntar; **Detener** lo silencia, y volver a iniciar el reproductor vuelve a preguntar. La voz, la velocidad y el tono son los de Read Aloud.
<a id="commands"></a>

## Comandos

Las mismas cosas que los [comandos](../commands/index.md) (la línea de comandos del editor de historias, la macro `command` de una plantilla, un agente). Las partes se numeran como las numera el diálogo de conversión, o se nombran por su identificador.

| Comando | Qué hace |
|---|---|
| `/audiobook-status [from [to]]` | El servidor de voz en uso, la forma en que el libro se convierte por sí mismo, cuántas partes tienen audio (ninguna, lista, más antigua que el texto, más antigua que el guion), el estado de cada parte listada en el estudio, y la cola. Solo es lento cuando el libro es largo. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Pone las partes en la cola (el libro entero sin argumentos, o una sola parte cuando solo se indica `from`). `labelling` `true` usa el modelo del libro; `what` es `audio` o `script` (solo el guion para el estudio, no se habla nada). Con `wait` `true` el comando permanece hasta que terminan los trabajos, muestra cuánto falta y dice cómo terminó cada uno; detenerlo cancela los trabajos que aún no han empezado. |
| `/audiobook-stop` | Cancela las partes en espera; la que se está ejecutando se detiene tras su fragmento. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` o `final` en el estudio; la parte necesita un guion. |
| `/audiobook-cast` | El narrador, el respaldo y los personajes del reparto con sus actores. |

![El resultado de un comando](../../images/plugin-audiobook-command.png)

El audio se genera para **tu** libro, no para la copia de trabajo oculta de un [agente](../agents.md): un agente puede leer el reparto, y recibe una negativa de los demás comandos que le indica que tienes que ejecutarlos. Ejecútalos desde la línea de comandos, o encarga a un agente la tarea de preparar el libro (anotar, revisar) y conviértelo tú mismo.

<a id="notifications"></a>

## Notificaciones

Cuando un lote de conversiones termina, el navegador puede avisarte ([notificaciones](../account.md#notifications), para que puedas dejar la pestaña mientras el servidor de voz trabaja). En la lista de notificaciones de la página *Ajustes* hay dos líneas: **Convert to audio** (los lotes que inicias en el diálogo o con el comando) y **Convert to audio (automatic)** (una parte nueva que se convirtió en audio por sí misma). Un lote que detuviste no notifica; un lote con una parte fallida indica que ha fallado.

![La lista de notificaciones](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Lo que no hace (todavía)

- Solo mp3 y wav (no opus); el volumen de los fragmentos es el que dan las voces.
- No hay efectos de sonido ni ambientes.
- Dos motores de voz, Chatterbox y Higgs Audio v3 (en ComfyUI).
- La audición de una voz fuera de un segmento.
- El reparto no forma parte de la copia de seguridad de los *actores* de un libro: un libro restaurado conserva su reparto, pero nombra actores que deben existir en tu cuenta.
