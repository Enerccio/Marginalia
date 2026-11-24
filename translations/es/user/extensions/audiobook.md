---
label: Audiolibro
order: 27
---

# Audiolibro

Convierte un libro en audiolibro, parte a parte, con un servidor de texto a voz: una voz para el narrador y otra para cada personaje, guardadas con las partes del libro y reproducidas en el lector. Puedes dejar que funcione solo (el modelo indica quién habla y cómo, y el servidor de voz lee) para un borrador rápido, o trabajar las partes una a una en el **Audiobook studio**, viendo exactamente lo que se envía al servidor de voz y cambiándolo a mano, y exportar el resultado como archivos mp3 con marcas de capítulo.

La extensión no genera la voz por sí misma: se comunica con un **Chatterbox-TTS-Server** que tú ejecutas (véase [Configurar el servidor de voz](audiobook-server.md)). El texto del libro nunca se modifica y nunca se envía a nadie que no sean los servidores que hayas indicado; el audio se guarda en tu propia instalación, como recursos vinculados a las partes.

<a id="what-you-need"></a>

## Lo que necesitas

- Un servidor de voz y, si comparte la tarjeta gráfica con tu modelo de lenguaje, las peticiones que le hacen sitio y lo detienen después ([Configurar el servidor de voz](audiobook-server.md)).
- Voces: un **clip de referencia** corto (de 12 a 30 segundos de una sola persona, sin música) para cada voz que quieras clonar, o uno de los archivos de voz que tenga el servidor. Un narrador necesita una voz de lectura tranquila y constante.
- Para un libro con varias voces, un modelo de lenguaje que pueda etiquetar el texto. Por defecto es el modelo del libro.
- **ffmpeg** es opcional. Sin él, todo funciona con mp3. Con él en el servidor (está en la imagen de Docker, y en un ordenador de escritorio se encuentra en `PATH` o en carpetas habituales como `/opt/homebrew/bin`; la variable `MARGINALIA_FFMPEG` indica dónde está si se encuentra en otro lugar), la exportación obtiene el formato **m4b**, y un administrador puede guardar el audio como wav.

<a id="the-settings"></a>

## Los ajustes

*Settings → Extension Settings → Audiobook Settings* es el panel de tu cuenta; guarda la página *Settings* después de cambiarlo.

![El panel con un backend y los actores](../../images/plugin-audiobook-settings.png)

### Backends

Un backend es un servidor de voz y el modelo con el que habla. Añade uno con **+**. **Duplicate** copia el seleccionado (dirección, claves, bloqueo de GPU, voz, valores por defecto y las peticiones antes y después de un lote) con un nombre nuevo: es la forma rápida de tener un backend de otro modelo en la misma máquina, por ejemplo Higgs junto a Chatterbox, donde luego cambias el modelo, la dirección y las peticiones de arranque y parada.

| Ajuste | Qué hace |
|---|---|
| Name | El nombre que aparece en el diálogo Convert. |
| Address | La dirección base del servidor de voz, o de lo que lo reenvía a él, por ejemplo `http://localhost:8004`. |
| Api key | Se envía como token bearer si el servidor (o tu proxy) lo pide. Se guarda cifrada y no se vuelve a mostrar; escribe una nueva para reemplazarla, o usa **Remove the key**. |
| Speech model | **Chatterbox Turbo** (inglés, con etiquetas de sonido como `[sigh]`, la más rápida y la predeterminada), **Chatterbox** (inglés, con una *exaggeration* de la entonación), **Chatterbox Multilingual** (23 idiomas) o **Higgs Audio v3 in ComfyUI** (muchos idiomas, voces que adaptan el ánimo de cada línea y sonidos como llanto, jadeos o un gemido; véase [más abajo](#higgs-audio-v3-emotions-and-sounds)). El [bloqueo de GPU](../administration/gpus.md) del backend (o las peticiones antes de un lote) debe dejar cargado este modelo. Un libro que necesite otro modelo usa otro backend. |
| GPU lock | El [bloqueo de GPU](../administration/gpus.md) de la GPU en la que se ejecuta el servidor, cuando la comparte con el modelo de lenguaje u otros servidores (también con el [Image Generator](image-generator.md)): cada lote y cada muestra toma su turno en la GPU, y el bloqueo carga primero el servidor de voz. Solo se muestra si el administrador configuró los bloqueos de GPU. |
| The server is on this machine or on my network | Permite direcciones de la red local. Déjalo desactivado para un servidor en la nube: así no se le puede hacer llamar a tu propia máquina. |
| Timeout of a request | Cuánto esperar a un fragmento de voz. |
| Restart the server after (pieces) | Para un servidor de voz que pierde memoria y que el sistema mata en un lote largo: tras este número de fragmentos, se vuelven a hacer las peticiones posteriores al lote y luego las anteriores, en mitad del lote (parar, arrancar, cargar), y el trabajo continúa con un servidor nuevo. 0 (el predeterminado) nunca lo hace. Un servidor que se cae se vuelve a arrancar aunque este ajuste diga otra cosa; véase [Solución de problemas](audiobook-server.md#troubleshooting). |
| Key of the requests around a batch | La clave que llevan las peticiones antes y después de un lote como `Authorization: Bearer`. Se guarda cifrada y no se vuelve a mostrar (**Remove the key** la borra), así que no tiene que escribirse en las cabeceras de cada petición, donde sería legible. Vacía: se usa la *Api key* de arriba, que es lo que quieres cuando un proxy como LiteLLM sirve todo. Una petición que tenga su propia cabecera `Authorization` la conserva. |
| Narrator voice | La voz del narrador de un libro que no tiene reparto. Un archivo de voz del servidor se escribe `predefined:Henry.wav`. |
| Precision | Solo Higgs: la precisión en la que calcula el modelo. **fp32** (el predeterminado) es la opción segura: en una tarjeta AMD, **bf16** convirtió fragmentos en galimatías, efectos de sonido y voces equivocadas. bf16 necesita aproximadamente la mitad de la memoria gráfica (unos 8 GB en lugar de 16) y es más rápido donde funciona. |
| Send the text of the clip to Higgs | Solo Higgs, experimental: el [texto del clip](#voices-and-clips) se envía con él. Déjalo desactivado: en las pruebas cambió la voz. |
| Check the length of the pieces | Activado por defecto. Un fragmento mucho más largo que su texto (4 s más 0,6 s por palabra: el modelo siguió hablando, o hizo un sonido en lugar de las palabras) se vuelve a generar con la siguiente semilla, hasta **Retries** veces; se conserva la primera toma de la duración correcta, y si ninguna lo es, la más corta. |
| Check the words with speech recognition | Desactivado por defecto; necesita el [reconocimiento de voz](#speech-recognition). Tras cada lote, cada fragmento se transcribe y se compara con su texto. Un fragmento que se aleja más de **Max. word errors (%)** de sus palabras se vuelve a generar con la siguiente semilla y se vuelve a comprobar, hasta **Retries** rondas; se conserva la toma con menos palabras erróneas. La conversión indica cuántos fragmentos se volvieron a generar y cuántos siguen sin coincidir. |
| Defaults of a segment | Temperatura, peso de CFG, velocidad y tamaño de trozo con los que empieza un fragmento (el studio puede cambiarlos para un fragmento concreto). Higgs solo usa la temperatura. |
| Before a batch, After a batch | Peticiones hechas antes y después de cada lote de partes (y de cada muestra), dentro del bloqueo de GPU. Liberar la tarjeta gráfica y arrancar el servidor de voz es tarea del bloqueo de GPU; véase [Una GPU para el modelo y la voz](audiobook-server.md#one-gpu-for-the-model-and-the-speech). Vacío no hace nada. |

**Test connection** hace lo mismo que un lote: toma el bloqueo de GPU (así se arranca un servidor de voz que esté parado), hace las peticiones previas a un lote, pregunta al servidor si está disponible y tiene un modelo cargado, y hace las peticiones posteriores al lote. Sin bloqueo ni peticiones, solo pregunta.

Un libro que tiene fragmentos en otra escritura (japonés en un libro en inglés) los envía al modelo multilingüe, si tienes un backend para él, con su idioma; se quedan con el modelo del libro si no tienes ninguno. Un libro de Higgs los conserva: Higgs los lee él mismo, con la misma voz.
<a id="higgs-audio-v3-emotions-and-sounds"></a>

### Higgs Audio v3: emociones y sonidos

Con el backend **Higgs Audio v3 en ComfyUI** se escuchan las etiquetas del modelo de lenguaje. La dirección del backend es la de tu ComfyUI
(con los nodos de TTS Audio Suite, véase [Configurar el servidor de voz](audiobook-server.md#higgs-audio-v3-in-comfyui)).

- La **entrega** de un fragmento de habla se hace hablada: triste, enojado, alegre, divertido, tenso, temeroso, sorprendido, tierno, sarcástico, suplicante, desesperado,
  asqueado, excitado, susurrado o gritado. Una entrega fuerte (intensidad 0,8 o más) es más expresiva; una débil (por debajo de 0,2) se lee de forma neutra.
  El narrador lee de forma neutra.
- El modelo de lenguaje puede poner más **sonidos** en el habla que con Chatterbox Turbo: risa, risita, risilla, suspiro, jadeo, tos, carraspeo,
  resoplido, estornudo, quejido, llanto, sollozo, grito, resuello, gemido, tarareo y chistido.
- El programa también lee la **narración alrededor de cada habla** (en inglés) y da al habla el sonido que nombra, cuando el modelo no puso ninguno: *"You look funny," she giggled.* obtiene una risilla, *She burst out laughing. "Careful!"* una risa, *a soft moan escaped her lips. "Yes..."* un gemido,
  *panting* o *out of breath* un jadeo, y *cried quietly* o *burst into tears* llanto. Deja de lado lo que solo parece un sonido: *"Stop!" she cried.*
  (una exclamación), *moaned about the weather*, *the wind moaned*, *the floorboards groaned*, *stifled a giggle*, *didn't laugh*, y un sonido que la narración da a otro personaje con nombre. *"You look funny," he mocked her.* recibe una entrega sarcástica o divertida y ningún sonido. La narración en sí se lee tal como está escrita.
- **La narración se lee siempre de forma neutra**: sin sonidos y sin estado de ánimo, también para un habla que el modelo dejó al narrador.
- Higgs no necesita un clip por estado de ánimo: basta un clip de una voz. Copia la grabación del clip, así que uno limpio importa más que con Chatterbox
  ([Un buen clip](#a-good-clip)).
- Cada fragmento lo genera ComfyUI y su memoria se vacía después (unos segundos por fragmento), lo que evita que algunas tarjetas gráficas se bloqueen.
- Higgs suele seguir después de la última palabra de un fragmento (una respiración, el inicio de otro sonido) y el clip termina en medio de ello, lo que se oye como un breve "whoosh" antes de la pausa. Con **ffmpeg** la extensión corta esa cola y hace un fundido de salida en cada fragmento; sin ella, los fragmentos quedan tal como los generó Higgs.
- El modelo Higgs Audio v3 tiene una **licencia de investigación y no comercial**: un audiolibro que vendas necesita una licencia de Boson AI.

<a id="speech-recognition"></a>

### Reconocimiento de voz

**Speech recognition** escribe lo que dice un fragmento de habla. Revisa los fragmentos de un lote (cuando un backend tiene **Check the words with speech
recognition** activado) y reconoce el [texto de un clip de voz](#voices-and-clips). Elige dónde se ejecuta:

| Ajuste | Qué hace |
|---|---|
| Speech recognition | **ComfyUI (Granite ASR)**: el modelo de voz Granite del TTS Audio Suite en tu ComfyUI (la dirección es la de ComfyUI). **OpenAI-compatible (whisper)**: cualquier servidor con `/v1/audio/transcriptions`, como un servidor whisper (faster-whisper, speaches) o la API de OpenAI. |
| Address, Api key | La dirección de ComfyUI, o la base de la API (con o sin `/v1`); la clave se guarda cifrada. Dirección vacía: sin reconocimiento de voz. |
| GPU lock | El [bloqueo de GPU](../administration/gpus.md) que toma el reconocimiento. En una sola tarjeta con Higgs en fp32, dale un bloqueo propio cuyos pasos reinician ComfyUI: ComfyUI mantiene el modelo de voz en la memoria del servidor, y cargar el reconocimiento junto a él dejó el servidor sin memoria. |
| Model of the recognition | Vacío: `granite-speech-4.1-2b` para ComfyUI, `whisper-1` para el compatible con OpenAI. |
| Timeout, The server is on this machine or on my network | Como para un backend. |

**Test connection** comprueba que ComfyUI tiene los nodos, o que el servidor responde. Los fragmentos de un lote se revisan después del lote, cuando se devuelve el servidor de voz, así que el reconocimiento y el modelo de voz nunca necesitan la tarjeta al mismo tiempo.

<a id="cleanup-of-clips"></a>

### Limpieza de clips

**Cleanup of clips** limpia un clip de referencia en un ComfyUI con el [TTS Audio Suite](audiobook-server.md#higgs-audio-v3-in-comfyui) (el mismo que para las obras de Higgs). El resultado es una nueva **versión** del clip en [Voices and clips](#voices-and-clips); el clip en sí se conserva.

| Ajuste | Qué hace |
|---|---|
| Address, Api key | La dirección de ComfyUI; la clave se guarda cifrada. Dirección vacía: sin limpieza. |
| GPU lock | El [bloqueo de GPU](../administration/gpus.md) que toma la limpieza. Dale un bloqueo propio, no el del habla ni el del reconocimiento de voz, cuyos pasos reinician ComfyUI, para que los modelos de limpieza nunca se carguen junto al modelo de voz. |
| Timeout, The server is on this machine or on my network | Como para un backend (el tiempo máximo es para una limpieza: 10 minutos por defecto, porque ComfyUI descarga un modelo la primera vez que se usa). |
| Cleanup | Las limpiezas (presets) entre las que elegir en Voices and clips, por nombre. Cada una es una lista de pasos que se ejecutan uno tras otro. **Default cleanups** restaura las que trae. |

Un paso es uno de:

- **Separation model**: un modelo de la suite que divide el audio en dos, la parte *extracted* y la parte *remaining*, y conserva una de ellas. Qué parte contiene la voz depende del modelo: un modelo de voces (`MelBandRoformer`) extrae la voz; un modelo de eliminación de ruido o de reverberación extrae el ruido o la sala, así que conserva la *remaining*. **Aggressiveness** (0 a 20) decide cuánto se quita.
- **Voice fixer**: restaura una voz degradada (un teléfono, una grabación antigua). Cambia el sonido de la voz más que una separación.
- **ffmpeg (here)**: el filtro de **Clean with ffmpeg**, que ejecuta la aplicación entre los pasos en ComfyUI.
- **Normalize loudness (here)**: sube (o baja) el nivel del clip a la sonoridad que indiques (-18 LUFS, como en el audiolibro, por defecto), con ffmpeg en la aplicación. Solo cambia el nivel; los picos se mantienen por debajo del recorte. Ponlo al final, después de los pasos que quitan algo (dejan la voz más baja). Una limpieza solo con este paso (y el filtro de ffmpeg) no usa ComfyUI ni su bloqueo de GPU.

Las limpiezas que trae son: *Voice from music* (la voz de una película o una canción), *Remove noise*, *Remove echo* (la sala y el eco), *Raise a quiet voice* (solo el paso de sonoridad), *Restore the voice* y *Film or game clip* (la voz fuera de la música, luego la sala, luego el ruido: quitar primero el ruido hizo que la sala de un clip de película destacara). *Remove echo* necesita un arreglo en algunas configuraciones de ComfyUI, véase [el servidor de voz](audiobook-server.md#higgs-audio-v3-in-comfyui). **Test connection** comprueba que ComfyUI tiene los nodos y rellena la lista de modelos con los de tu servidor. ComfyUI descarga los modelos la primera vez que una limpieza los usa.

<a id="shared-speech-servers"></a>

### Servidores de voz compartidos

En una instalación con varios usuarios, el administrador puede mantener servidores de voz para todos, de modo que una máquina con GPU se configura una vez y nadie necesita una dirección ni una clave. El administrador lo hace en el mismo panel (las últimas secciones, vistas solo por administradores): **Shared speech servers** abre una ventana con los mismos campos que un backend propio, más quién puede usarlo: **all users**, o los usuarios elegidos en la lista. Un interruptor ahí, **Users may add speech servers of their own**, decide si siguen permitidos los backends propios que aparecen más abajo.

Lo que obtienes:

- **Backend in use** en la parte superior del panel lista los servidores compartidos que puedes usar (marcados *(shared)*) y los tuyos. La ventana *Convert to audio* y el estudio usan el que elijas; los comandos también lo usan.
- Solo ves el nombre de un servidor compartido, nunca su dirección, claves o peticiones. Los lotes de todos los usuarios en una misma dirección de servidor esperan unos a otros, así que dos usuarios no pueden arrancar la misma GPU a la vez.
- Cuando el administrador te quita un servidor, un trabajo que todavía lo nombre se rechaza; elige otro.
- Cuando el administrador desactiva tus propios servidores, el panel lo indica. Los que añadiste se conservan y se vuelven a usar si el interruptor se vuelve a activar.
- El *Narrator voice* de un servidor compartido debe ser un archivo de voz del servidor (`predefined:Henry.wav`): un clip de referencia es un recurso de un solo usuario y los demás no pueden leerlo. Las voces de tus actores funcionan como siempre.
### Actors

Un **actor** es una voz tuya: un clip de referencia (uno de tus [recursos](../resources.md) de audio, o uno nuevo que subas aquí) o un archivo de voz del servidor. Puede tener voces propias para un **estado de ánimo** (un susurro, un grito) o un **idioma** (un clip que habla japonés). El idioma tiene prioridad sobre el estado de ánimo, y si no hay voz para ellos, se usa la voz del actor. Tus actores se guardan en tu cuenta; cualquier libro puede dar un actor a su narrador y a sus personajes.

Un actor puede tener una **tagline**, una nota corta tuya: el personaje que interpreta, un libro, *profunda y antigua*, cualquier etiqueta que te ayude a elegir la voz adecuada. Los selectores de un actor (los ajustes, el reparto) la muestran bajo el nombre, y *Voices and clips* bajo el nombre en la lista de actores. En el reparto, escribe en una casilla de actor para encontrar un actor por una parte de su nombre o de su tagline.

**Preview** junto a una voz (en el actor, los ajustes y el reparto) reproduce unas frases dichas con ella (una corta en el idioma de la voz, para un idioma). Si la GPU tiene cargado otra cosa, el bloqueo de GPU carga primero el servidor de voz. La flecha junto a él ofrece otras muestras: **with sounds** (un suspiro, una risa, un jadeo) para los modelos que las dicen (Chatterbox Turbo, Higgs Audio v3), y para Higgs, cada **estado de ánimo** del etiquetado (triste, susurrado, sarcástico...). Cuando la voz es un clip, el botón con el archivo de audio reproduce el **reference clip** tal como está guardado, al instante (sin servidor de voz ni GPU). Los actores aparecen listados por nombre.

Cada muestra toma la GPU solo para sí. La GPU sigue cargada con el servidor de voz después de una muestra, así que las siguientes muestras llegan al instante, hasta que una petición del modelo de lenguaje (u otro servidor) necesite la GPU.

![Un actor con una voz para un estado de ánimo](../../images/plugin-audiobook-actor.png)

### Voices and clips

**Voices and clips** (junto a los actores) es una sola ventana para tus actores y para los clips de los que se clonan sus voces. Tiene tres pestañas, al principio una junto a otra: **Actors**, **Clips** y **Clip**. Arrastra una pestaña para ponerla junto a otra, debajo de ella o juntas en el mismo grupo; la ventana se vuelve a abrir como la dejaste.

- **Actors**: cada actor con su voz, cuántas otras voces tiene (estados de ánimo, idiomas) y la peor calidad de sus clips; añade, edita o borra uno.
  La mira de un actor cuya voz es un clip selecciona ese clip en **Clips**, abriendo las carpetas que haya encima.
- **Clips**: tus clips en **folders** (por ejemplo, uno por personaje), con las **versions** hechas a partir de un clip debajo, cada una con su duración, su **quality** y dónde se usa (actores, con el estado de ánimo o el idioma, y los narradores de tus backends). **Add a clip** sube uno.
  - Las carpetas empiezan cerradas; lo que abras queda abierto mientras la ventana esté abierta, también tras un cambio de clip.
  - Marca clips (las carpetas no tienen casilla) para trabajar con varios a la vez. **Merge** crea un clip nuevo con los clips marcados, uno tras otro, en el orden de la lista y sin pausa entre ellos: una referencia más larga a partir de tomas cortas de una voz. Se nombra según ellos, va a su carpeta si comparten una y recibe sus textos cuando cada uno tiene uno; los clips marcados se conservan. Los clips wav del mismo formato se unen tal cual; los clips mp3 o wav de formatos distintos necesitan ffmpeg. **Delete checked** borra los clips marcados tras preguntar; un clip que todavía se usa se conserva, y el mensaje lo nombra.
- **Clip**: el clip seleccionado.
  - **Name** y **Folder**: renombran el clip (el archivo conserva su nombre) y lo ponen en una carpeta, una nueva escribiendo su nombre. Una versión se queda en la carpeta del clip del que se hizo, y dice de qué se hizo y cómo.
  - Su **waveform**: haz clic para poner el cabezal de reproducción ahí y pulsar **Play**, o arrastra sobre él para seleccionar un rango (los campos **From** y **To** se actualizan, hasta la centésima de segundo, y la selección las sigue). La lupa amplía la selección para ajustar un borde con precisión; la otra muestra de nuevo el clip completo. **Play the selection** reproduce el rango, **Play around the cut** reproduce un segundo y medio a cada lado del corte, sin él: cómo suena el clip cuando se quita ese rango.
  - **Download** lo descarga (con el nombre del clip y de su versión), lee qué le pasa, **Replace...** lo sustituye o **Delete** lo borra (solo cuando nada lo usa; sus versiones suben al clip del que se hicieron).
  - **Clean with ffmpeg** hace una versión con menos siseo y zumbido constantes. No quita música, efectos ni otras voces.
  - **Clean with AI** hace una versión con la [limpieza](#cleanup-of-clips) elegida en ComfyUI. Se ejecuta en segundo plano: puedes seguir trabajando, y un mensaje avisa cuando la versión está lista.
  - **Trim** hace una versión solo con el rango seleccionado del clip (**Keep this part**) o sin él (**Cut this part out**), para un sonido que ninguna limpieza quita: un animal, una tos, una puerta. Los empalmes se fundidos para que no suenen a clic. Un clip wav se recorta sin ffmpeg; un mp3 lo necesita.
  - **Use this version** pone una versión en todos los lugares donde se usa el clip del que se hizo. Las partes se vuelven a generar con la nueva voz, y el clip antiguo se conserva hasta que lo borres.
  - **Try the voice**: escribe un texto y pulsa **Speak**: el backend en uso lo dice con este clip exactamente como lo escribiste (sin etiquetado ni sonidos añadidos), con la **Seed** que indiques (vacío: 42, la de las conversiones), y lo reproduce. El ⓘ enumera las etiquetas que lee el modelo del backend, en su propia sintaxis (`[cough]` para Chatterbox Turbo, `<|sfx:cough|>` o `<|emotion:sadness|>` para Higgs); un clic inserta una en el texto. Usa el bloqueo de GPU del backend y aparece en los trabajos como una conversión. La limpieza y el reconocimiento de un clip también son trabajos.
  - **Text of the clip**: las palabras dichas en el clip. Escríbelas, o pulsa **Recognize** para que el [reconocimiento de voz](#speech-recognition) las transcriba, corrígelas y **Save**. El diálogo del actor muestra el mismo campo para el clip de la voz del actor. Una versión y un reemplazo heredan el texto. Higgs solo lo recibe cuando el backend lo envía (desactivado por defecto).
- **Replace...** pone otro clip, uno tuyo o uno que subas ahí, en todos los lugares donde se usa el clip.

Los selectores de una voz (el actor, el narrador de un backend) muestran un clip como *carpeta / clip › versión*.

<a id="a-good-clip"></a>

#### Un buen clip

Un modelo de voz copia la **grabación**, no solo la voz: la habitación, un siseo, la música bajo una escena de película vuelven en cada línea, a menudo como un "whoosh" suave bajo las palabras que ningún filtro del resultado elimina. Por eso:

- Usa una grabación limpia de un solo hablante con pausas reales (una muestra de un actor de doblaje, una lectura hecha en casa en una habitación tranquila), de 12 a 30 segundos. Un clip recortado de una película o de una serie es el peor caso.
- Las marcas de calidad se miden a partir del clip: **Clean** (las pausas están al menos a 45 dB por debajo de la voz), **Fair** (de 35 a 45 dB, o más largo de 30 s, que Chatterbox corta), **Poor** (sonido de fondo, por debajo de 35 dB; distorsionado; más corto de 8 s). Un clip que no es Clean se avisa al añadirlo.
- Un modelo de separación conserva lo que suena a voz: el ulular de un búho o el canto de un pájaro se quedan (en la prueba con varios modelos). Para quitar un sonido así, usa **Trim**.
- Limpia un clip con IA ([Limpieza de clips](#cleanup-of-clips)): en la prueba, la limpieza *Film or game clip* llevó un clip con siseo, zumbido y eco de *Poor* a *Clean* (las pausas pasaron de 38 a 89 dB por debajo de la voz). Escucha la versión antes de usarla: un modelo de separación también puede quitar una respiración o el final de una palabra suave. La limpieza con ffmpeg solo reduce un ruido constante: en un clip de película ganó unos 3 dB y el clip siguió en *Poor*.
- Sin ComfyUI, limpia un clip a mano: la reducción de ruido de [Audacity](https://www.audacityteam.org/) con un perfil de ruido tomado de una pausa del clip, o un eliminador de voces como [Ultimate Vocal Remover](https://github.com/Anjok07/ultimatevocalremovergui) para un clip con música.

<a id="converting-new-parts-by-itself"></a>

### Convertir las partes nuevas por sí mismo

*Convert new parts to audio by itself* es el valor por defecto en todos tus libros; consulta [Conversión automática](#automatic-conversion).
<a id="the-cast-and-the-pronunciation-of-a-book"></a>

## El reparto y la pronunciación de un libro

Ambos se guardan con el libro (viajan en su copia de seguridad) y se editan desde el diálogo **Convert to audio**.

**Cast** indica quién lee: un actor para el **narrator**, otro para **other speakers** (quien el modelo nombre y el reparto no tenga; vacío: el narrador lee) y uno para cada **character**. Un personaje tiene un nombre y otros nombres con los que el texto puede referirse a él (*Tom* para *Tomáš Novák*), separados por comas. Una intervención se asigna al personaje cuando coincide su nombre, uno de los otros nombres o una palabra del nombre que ningún otro personaje comparta, sin distinguir mayúsculas ni tildes. **Add the characters of the book** rellena la lista con los personajes del punto de vista y los personajes presentes en las partes.

![El reparto de un libro](../../images/plugin-audiobook-cast.png)

**Unnamed speakers** (el guardia, el posadero, la chica de la ventana) pueden tener cada uno una voz propia a partir de una o dos grabaciones. Asigna al reparto actores base: **Other men**, **Other women** y, si lo deseas, **Children** (vacío: se usa la voz de mujeres, elevada). El etiquetado indica entonces también el género y la edad de cada hablante que el reparto no nombra, y cada uno habla con una *variación* del clip base de su tipo: un poco más agudo o más grave, otros formantes, más rápido o más lento, más oscuro o más brillante. Un adolescente recibe la voz de los hombres, una adolescente y un niño la de las mujeres, y un hablante mayor una voz más grave y lenta. La variación depende del nombre del hablante y del libro, así que el mismo hablante suena igual en cada capítulo y en cada ejecución. Si dejas vacíos los tres, cada hablante sin nombre lo lee **Other speakers**, como antes.

Los hablantes encontrados hasta ahora aparecen bajo los actores base con el género y la edad que recibieron primero (una parte posterior no los cambia). Puedes corregir el género o la edad, escucharlo, darle **another variation** (el dado) o quitarlo de la lista. Un hablante modificado se vuelve a generar la próxima vez que se conviertan las partes.

Una variación necesita **ffmpeg** con el filtro *rubberband* en el servidor (los paquetes de Debian y Ubuntu, que instala la imagen de Docker, normalmente se compilan con él; `ffmpeg -filters | grep rubberband` indica si el tuyo lo incluye). Cada variación se genera una vez, cuando se necesita por primera vez, y se guarda como una copia oculta de su clip, así que viaja con tus copias de seguridad. Sin ffmpeg, el reparto lo indica: los hablantes sin nombre comparten la voz base de su tipo, y las variaciones ya generadas siguen usándose. Una variación solo modifica un clip tuyo; un actor cuya voz es una voz del servidor de voz se usa tal cual.

**Pronunciation** es cómo se escribe el texto *para la voz*; el libro en sí nunca se cambia. El **dictionary** reemplaza palabras completas (importan las mayúsculas) por lo que se debe decir en su lugar: una reescritura fonética para un nombre o una palabra inventada, o una lectura en kana para el japonés. En un libro en inglés, las reglas también leen en voz alta otras cosas: números, ordinales, años, horas, cantidades de dinero y abreviaturas, convertidos en palabras; un título que es solo un número se lee como *Chapter four. The Arch* (la palabra antes del número la escribes tú); las marcas de markdown como `*` se omiten; y una separación de escena (`***`, `---`) no se lee, sino que se convierte en una pausa más larga. Desactiva las reglas en un libro en otro idioma; el diccionario sigue aplicándose. **Try a text** muestra cómo se leerá un fragmento de texto. Un fragmento cuyo texto hablado cambia se vuelve a generar; los demás se conservan.

![La pronunciación de un libro](../../images/plugin-audiobook-pronunciation.png)
<a id="converting-a-book"></a>

## Convertir un libro

*Menú Tools del editor de historias (el botón de engranajes) → **Convert to audio***.

![El diálogo antes de empezar](../../images/plugin-audiobook-convert.png)

1. Elige el **backend** y pulsa **Load**: se listan las partes con texto de la rama activa (la primera es la 1) con lo que tiene cada una:
   *Not generated*, *Labelled* (las etiquetas del modelo quedan guardadas, todavía sin audio), *Generated* (con un botón de reproducción y una descarga) o
   *Generated (the text changed)* (el audio se hizo a partir de un texto anterior y se vuelve a hacer); a una parte que el estudio marca como final se le añade *(final)*.
2. Deja **Label the parts with a language model** desactivado para la versión sencilla: el narrador lee cada párrafo. Actívalo para que el modelo decida quién habla en cada fragmento de diálogo, cómo (tranquilo, susurrando, gritando...) y dónde encaja una etiqueta de sonido, y el reparto proporciona las voces. El modelo del libro viene elegido; elige otro si lo prefieres. Primero se etiquetan las partes y después se leen. Una parte que ya tiene etiquetas para su texto se lee con ellas también cuando la casilla está desactivada (no se consulta al modelo), así que volver a ejecutar un libro etiquetado sin la casilla no convierte sus diálogos en narración simple; **Remove labels** es la forma de pedir una narración simple o etiquetas nuevas.
3. Marca las partes (la casilla del encabezado las marca todas) y **plan** qué hacer con ellas; la columna **Plan** lo muestra y todavía no se hace nada:
   **Generate** (genera lo que falta; una parte que está al día no se vuelve a generar), **Regenerate** (borra el audio, quita las etiquetas y genera), **Delete audio** (los fragmentos y el audio unido de la parte; las etiquetas se mantienen), **Remove labels** (el modelo se vuelve a consultar la próxima vez que se haga la parte), **Clear the plan**. Los pasos se acumulan en una parte (Delete audio y Remove labels sin Generate solo limpian); una parte marcada como final queda fuera de todos los planes.
4. **Start** lleva a cabo el plan. Cuando quita audio o etiquetas, pregunta antes. Lo que haya que generar pasa a tu cola, que sigue funcionando al cerrar la ventana y al salir de la página; una parte que espera o está en la cola queda fuera de un plan. El estado, los fragmentos ya hechos y el mensaje del trabajo se muestran en la fila de la parte.
   Mientras el modelo etiqueta una parte, su fila muestra *Labelling pieces 17-32 of 120* (el modelo se consulta de 16 fragmentos en 16 fragmentos); **haz clic en el estado** de una parte ya etiquetada para leer, en cada petición, el razonamiento del modelo y su respuesta, a medida que llegan. Cuando termina el etiquetado de la parte (también si falla o se detiene), todo queda guardado con la parte, cada ejecución después de las anteriores; puedes volver a leerlo o borrarlo con **Reasoning** en el estudio. Las etiquetas nuevas y el audio nuevo no lo modifican, y se incluye en las copias de seguridad y exportaciones del libro con el audio.

![La cola: fragmentos ya hechos, y una descarga cuando una parte está terminada](../../images/plugin-audiobook-queue.png)

Cómo se hace una parte, para que sepas qué esperar:

- El texto lo **divide el programa**, nunca el modelo, en fragmentos de narración y fragmentos de diálogo directo (entre comillas dobles rectas o curvas o corchetes japoneses; un título es narración). El modelo solo *etiqueta* los fragmentos numerados. Un texto con etiquetas de sonido solo se usa cuando es el fragmento con las etiquetas añadidas y nada más ha cambiado, así que el modelo nunca puede modificar tu libro.
- **Quién habla en un fragmento lo decide el modelo para el diálogo; la narración es del narrador.** Una carta, una nota, un mensaje o los pensamientos de un personaje no llevan comillas, así que el programa los considera narración. Ese fragmento lo lee el personaje (la voz del reparto) solo cuando **empieza en cursiva** (`*...*` o `_..._`) y el modelo nombra al personaje a partir del contexto (una firma, «la letra era inconfundible»). La narración en tercera persona, también cuando se cuenta desde el punto de vista de un personaje («Ella lo vio cruzar la habitación con Percy»), siempre es del narrador: el programa ignora el personaje que el modelo asigne a un fragmento sin cursiva. Las cursivas para énfasis, sonidos o palabras extranjeras también son narración. Las palabras siempre son tuyas.
- **La instrucción para el modelo es una plantilla que puedes cambiar.** *Settings → Extension Settings → Audiobook Settings → Labelling prompt* es tu valor por defecto; un libro (pestaña *Prompts* → *Extension Settings* → *Audiobook*) o una carpeta pueden tener la suya propia. Si está vacía, se usa la de la carpeta superior, después la tuya y, por último, la integrada. Se escribe como los demás [prompts](../books/prompts.md): las variables son `{{characters}}`, `{{deliveries}}`, `{{tags}}` y `{{wordsPerTag}}`, y el menú emergente de sugerencias tiene **Insert Default Template**. Si cambias la instrucción, las etiquetas de las partes que se vayan a hacer se volverán a pedir la próxima vez que se generen.
- **El volumen de las partes se nivela** cuando está instalado el programa ffmpeg (el mismo que hace el `.m4b`): el audio unido de cada parte se lleva a un mismo volumen (EBU R128, lineal, para que se mantenga la dinámica; **-18 LUFS** salvo que lo cambies en *Settings → Extension Settings → Audiobook Settings*, donde también puedes desactivarlo). Los fragmentos se guardan tal como los generó el servidor; solo se nivela la parte unida, para que un audiolibro no salte de volumen de una parte a otra. **Las partes hechas antes** no se tocan hasta que las conviertas de nuevo: eso une otra vez los fragmentos guardados y los nivela, y no pide nada al servidor de voz (tampoco descarga el modelo de lenguaje). Desactivar el nivelado deja como están las partes ya niveladas. Si ffmpeg falla con una parte, esta conserva su audio tal como estaba y el resultado lo indica.
- Todas las partes de un mismo clic se hacen en **un solo lote por modelo de voz**: las peticiones anteriores al lote se ejecutan una vez, después se leen todos los fragmentos y, por último, se ejecutan una vez las peticiones posteriores al lote. Esto evita que la tarjeta gráfica cambie de modelo en cada parte.
- Cada fragmento se **guarda por su contenido** (texto, voz, modelo, idioma, ajustes y semilla). Una parte que ya está al día no cuesta nada, un párrafo cambiado es lo único que se vuelve a generar, y una parte que falló conserva los fragmentos ya hechos cuando pulsas **Retry**.
- Los fragmentos se unen con pausas que dependen de su posición (después de un título, entre párrafos, antes de *he said*) en un único mp3 por parte, que se guarda con la parte.
- Mientras se ejecuta una generación tuya, la voz espera, de modo que el modelo de lenguaje no se descarga mientras tanto.
- Una parte que marcas como **final** en el estudio se deja intacta, y el diálogo indica cuántas hay.
- **El audiolibro completo** une en un mp3 todas las partes que tienen audio al día, con una marca de capítulo para cada parte, y muestra un enlace de descarga debajo de la lista (se nombran las partes que quedan fuera). El mismo archivo es el formato de exportación *one mp3 with chapter marks*.
- **Stop all** cancela las partes en espera y detiene la que se está ejecutando de inmediato: la petición al modelo de lenguaje (el etiquetado) o al servidor de voz (el fragmento que se está leyendo) se descarta, sin esperar. Un lote detenido antes de empezar no envía las peticiones que le preceden, así que el modelo de lenguaje sigue cargado. Una parte cuyos fragmentos estaban todos hechos antes de la parada sigue recibiendo su audio (solo se une); las demás conservan los fragmentos que tienen, y la siguiente ejecución hace solo los que faltan. **Download** te da el mp3 de una parte terminada.
<a id="the-audiobook-studio"></a>

## El estudio de audiolibros

*Menú Herramientas → **Audiobook studio*** (la voz de la pieza seleccionada, con una vista previa, está junto a su altavoz) es donde se crea el audiolibro final, una parte a la vez: ves el guion, lo cambias, escuchas una pieza y haces la parte.

![The studio with a speech selected](../../images/plugin-audiobook-studio.png)

- Mientras la parte se anota o se genera, una barra indica que algo está en marcha, y los botones que iniciarían más trabajo esperan.
- **Annotate** crea el guion de la parte (con el modelo de lenguaje si lo pides, o con el narrador si no) y muestra las piezas en una tabla: hablante, forma de decir, intensidad, el texto hablado y una marca para una pieza que hayas cambiado. Mientras se ejecuta, aparece junto a él un botón **Stop**, que termina la petición al modelo de lenguaje.
- **Reasoning** muestra lo que el modelo de lenguaje dijo cada vez que etiquetó la parte (la fecha de la ejecución y las piezas de cada petición, su razonamiento y su respuesta), para que veas por qué una línea fue a un hablante o recibió una forma de decir. **Delete the reasoning** la elimina; las etiquetas y el audio se mantienen.
- Selecciona una pieza para editar su **speaker**, el **spoken text**, la **delivery**, la **intensity** (la exageración de la voz, para los modelos que la tienen; en Higgs, la forma de decir y la intensidad son las marcas como `<|emotion:sadness|>` al principio del texto hablado, que las siguen cuando las cambias), el **language**, la **seed** y la **pause after**. **Apply** conserva el cambio y **bloquea** la pieza: una nueva anotación deja una pieza bloqueada tal como está.
  **Unlock** la devuelve a la anotación. *The request the speech server gets* muestra la petición exacta.
- **Hear** genera el audio de una pieza (solo esa pieza; el servidor de voz se arranca para ella). **Another take** la genera de nuevo con una nueva semilla, y las **takes** se conservan para que elijas la que te guste (*Use this take and hear it*). Una buena voz depende a menudo de la semilla.
- **Make the part from this script** une las piezas tal como están, sin una nueva anotación.
- El **status** de la parte es *draft*, *reviewed* o *final*. Una parte final no se anota ni se convierte de nuevo, y la exportación puede limitarse a las partes finales.

<a id="exporting"></a>

## Exportar

El audiolibro es un formato de *Export story* del libro: **Audiobook (one mp3 per chapter, in a zip)**, **Audiobook (one mp3 with chapter marks)** y, cuando ffmpeg está instalado, **Audiobook (one m4b with chapters)**. Se componen con el audio de las partes que están al día: una parte sin audio, o con audio más antiguo que su texto o que su guion en el estudio, se deja fuera, y la ventana indica cuáles. Los archivos mp3 llevan etiquetas ID3 (el título del libro, el capítulo, la pista), y el archivo único tiene marcas de capítulo que reproductores como VLC y la mayoría de las aplicaciones de audiolibros muestran; el zip incluye una lista de reproducción. La casilla **Audiobook: only the parts marked final** limita la exportación a las partes que terminaste en el estudio.

El **m4b** es el formato que quieren Apple Books y la mayoría de las aplicaciones de audiolibros del teléfono: audio AAC (64 kbit/s, suficiente para la voz) en un contenedor MP4 con los capítulos, el título, el autor y el género. Lo genera ffmpeg, así que el formato solo se ofrece en un servidor que tenga ffmpeg. El audio almacenado como mp3 se codifica una segunda vez para ello; el audio almacenado como wav se codifica una sola vez.

<a id="how-the-audio-is-stored"></a>

### Cómo se almacena el audio

Un administrador lo elige para toda la instalación en *Settings → Extension Settings → Audiobook Settings* (la última sección, que solo ven los administradores):

- **mp3** (predeterminado): pequeño, se une sin decodificador y sin necesidad de ffmpeg. El m4b es un segundo paso con pérdida.
- **wav**: sin pérdida, unas diez veces más grande (una hora de voz son unos 170 MB), necesita ffmpeg. Las piezas se piden al servidor de voz en wav y se unen como wav; el navegador las reproduce tal cual; las exportaciones mp3 y zip las codifica ffmpeg (96 kbit/s) y el m4b se codifica una sola vez.

La elección se aplica a lo que se genere a partir de entonces. Las partes generadas antes conservan su audio y se entienden tal como son; una parte de ambos tipos (una pieza bloqueada del estudio de antes) se une como wav, y una exportación de partes de ambos tipos funciona, pero necesita ffmpeg.

Una exportación del libro completo se hace a partir de los archivos almacenados de las partes, en archivos temporales del servidor, y se descarga desde allí; así que su duración no está limitada por la memoria de la aplicación, pero la carpeta temporal necesita espacio para unas dos veces el audio del libro (con wav, unos 350 MB por hora) mientras se genera.

![The export dialog with the audiobook formats](../../images/plugin-audiobook-export.png)

<a id="automatic-conversion"></a>

## Conversión automática

Un libro puede generar el audio de una nueva parte por sí mismo, en cuanto la parte está final: después de la anotación automática, la redacción de nuevo y las traducciones, si el libro las tiene, como último paso de [lo que sigue a la parte](../books/story-editor.md#what-follows-a-generated-part) (en el panel de las ejecuciones hasta que se genera el audio; su **Stop** saca la parte de la cola). Viene **off** por defecto. Elígela en la pestaña *Prompts* del libro, dentro de los ajustes de extensiones, para una carpeta de libros, o para todos tus libros en los ajustes de audiolibros; gana la más cercana:

- **No**,
- **Yes: the narrator reads the part**,
- **Yes: labelled by the model of the book, the cast gives the voices**; un libro sin modelo obtiene la narración sencilla, y se te avisa.

![The setting in a book](../../images/plugin-audiobook-auto.png)

La parte entra en tu cola, con el reparto y la pronunciación del libro. Un aviso te dice que está en cola y otro que su audio está listo o ha fallado; solo se muestran en la página que inició la generación, y el trabajo aparece en cualquier caso en la ventana *Convert to audio*.

<a id="listening"></a>

## Escuchar

Un libro publicado (o el lector) que tiene partes con audio obtiene una barra de reproducción: reproducir, pausar, detener, la velocidad, y la parte que se escucha marcada en el texto. Empieza desde la selección o desde la primera parte visible, reproduce las partes en orden y descarga la siguiente mientras se reproduce la actual, y los controles multimedia del dispositivo conocen el libro. El audio generado a partir de un texto anterior de una parte no se ofrece. El audio se sirve al dueño del libro y, mientras el libro está publicado, a cualquiera que pueda abrirlo; un libro que se retira de la publicación deja de servirse de inmediato (en la siguiente parte que cargue el reproductor).

![The player in the reader](../../images/plugin-audiobook-player.png)

<a id="a-part-without-audio"></a>

### Una parte sin audio

El reproductor se detiene en una parte que no tiene audio, para que no se salte una parte de la historia, y lo dice. Cuando la extensión [Read Aloud](tts-reader.md) está instalada, la barra muestra también un botón de altavoz, **Read this part (and the next ones without audio) with the voice of the browser**. No se lee nada con una voz de máquina a menos que lo pulses. Entonces lee esa parte y las siguientes que no tengan audio con la voz de tu dispositivo, y cuando llega a una parte que tiene audio, el reproductor retoma el control. Las partes posteriores sin audio en la misma escucha se leen igual sin preguntar de nuevo; **Stop** lo silencia, y volver a iniciar el reproductor vuelve a preguntar. La voz, la velocidad y el tono son los de Read Aloud.
<a id="commands"></a>

## Comandos

Lo mismo que los [comandos](../commands/index.md) (la línea de comandos del editor de historias, la macro `command` de una plantilla, un agente). Las partes se numeran como las numera el diálogo Convert, o se nombran por su identificador.

| Comando | Qué hace |
|---|---|
| `/audiobook-status [from [to]]` | Muestra el servidor de voz en uso, la forma en que el libro se convierte por sí mismo, cuántas partes tienen audio (ninguna, lista, más antigua que el texto, más antigua que el guion), el estado de cada parte listada en el estudio y la cola. Solo es lento cuando el libro es largo. |
| `/audiobook-convert [from [to [labelling [what [wait]]]]]` | Pone las partes en la cola (el libro entero sin argumentos, o una sola parte cuando solo se indica `from`). `labelling` `true` usa el modelo del libro; `what` es `audio` o `script` (solo el guion para el estudio, no se habla nada). Con `wait` `true` el comando permanece hasta que terminen los trabajos, muestra cuánto falta y dice cómo terminó cada uno; detenerlo cancela los trabajos que no han empezado. |
| `/audiobook-stop` | Cancela las partes en espera; la que se está ejecutando se detiene tras su fragmento. |
| `/audiobook-set-part-status part status` | `draft`, `reviewed` o `final` en el estudio; la parte necesita un guion. |
| `/audiobook-cast` | El narrador, el de reserva y los personajes del reparto con sus actores, los actores base de los hablantes sin nombre y los hablantes sin nombre encontrados hasta ahora (género, edad, variación). |

![El resultado de un comando](../../images/plugin-audiobook-command.png)

El audio se hace para **tu** libro, no para la copia de trabajo oculta de un [agente](../agents.md): un agente puede leer el reparto, pero recibe una negativa de los demás comandos que indica que tienes que ejecutarlos tú. Ejecútalos desde la línea de comandos, o encarga a un agente la tarea de preparar el libro (anotar, revisar) y conviértelo tú mismo.

<a id="notifications"></a>

## Notificaciones

Cuando un lote de conversiones termina, el navegador puede avisarte ([notificaciones](../account.md#notifications), para que puedas dejar la pestaña mientras el servidor de voz trabaja). En la lista de notificaciones de la página *Settings* hay dos líneas: **Convert to audio** (los lotes que inicias en el diálogo o con el comando) y **Convert to audio (automatic)** (una parte nueva que se convirtió en audio por sí misma). Un lote que detuviste no notifica; un lote con una parte fallida indica que falló.

![La lista de notificaciones](../../images/plugin-audiobook-notifications.png)

<a id="what-it-does-not-do-yet"></a>

## Lo que todavía no hace

- Solo mp3 y wav (no opus); el volumen de las partes es el que dan las voces.
- Sin efectos de sonido ni ambiente.
- Dos motores de voz, Chatterbox y Higgs Audio v3 (en ComfyUI).
- La escucha previa de una voz fuera de un segmento.
- El reparto no forma parte de la copia de seguridad de tus *actores*: un libro restaurado conserva su reparto, pero nombra actores que deben existir en tu cuenta.
