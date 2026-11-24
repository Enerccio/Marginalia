---
label: Extensiones
order: 930
icon: plug
---

# Extensiones

Las extensiones añaden funciones a Marginalia sin cambiar la propia aplicación. Marginalia incluye treinta extensiones:

| Extensión | Qué añade |
|---|---|
| [xAI Grok](grok.md) | Los modelos Grok de xAI como proveedor de inferencia, a través de la API propia de xAI, con el razonamiento mostrado aparte de la respuesta. |
| [Arc Outline](arc-outline.md) | Los arcos del libro y sus capítulos, con su estado, en la barra lateral; el modelo puede escribir un arco por ti. |
| [Anthropic Claude](anthropic.md) | Los modelos Claude de Anthropic como proveedor de inferencia, a través de la API propia de Anthropic, con los resúmenes del pensamiento mostrados como razonamiento. |
| [Audiobook](audiobook.md) | Convierte un libro en audiolibro con un servidor de voz: una voz para el narrador y para cada personaje, un estudio para la versión final, exportación en mp3 con marcas de capítulo. [Configurar el servidor de voz](audiobook-server.md). |
| [Author's Note](authors-note.md) | Una nota para el modelo, insertada en cada generación del libro. |
| [Beta Readers](beta-readers.md) | Lectores que inventas (personajes) y un chat con ellos sobre el libro, a solas o en grupo. |
| [Book Description Generator](book-description-generator.md) | Escribe con el modelo el nombre y la descripción del libro a partir de un rango de su historia, con el botón de la pestaña About. |
| [Chapter Marker](chapter-marker.md) | Títulos de capítulo en el esquema de la historia, a partir de los encabezados Markdown de las partes. |
| [Character Tracker](character-tracker.md) | Los personajes del libro con sus relaciones y un mapa de ellos. |
| [Conlang Translator](conlang-translator.md) | El habla de tus personajes en una lengua construida que describes, con el inglés guardado detrás para los modelos y un clic para el lector. |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | Hace que la llamada a funciones funcione con modelos Gemma 4 antiguos cuya API espera otro rol para los resultados de las herramientas; no es necesaria con una plantilla de chat actual. |
| [Google Gemini](google-genai.md) | Los modelos Gemini de Google como proveedor de inferencia, a través de la API propia de Google, con los resúmenes del pensamiento mostrados como razonamiento. |
| [Image Generator](image-generator.md) | Crea una imagen para una parte de la historia: el modelo escribe el prompt, ComfyUI o una API de imágenes de estilo OpenAI la dibuja, y tú la repites o la aceptas. |
| [Prompt History](prompt-history.md) | Guarda las últimas 20 peticiones enviadas a los modelos en tu sesión (y por cada ejecución de agente), con las respuestas y el razonamiento, para depurar extensiones y revisar prompts; se abre desde Settings → Extension Settings o desde la consola de un agente. |
| [Prompt Inspector](prompt-inspector.md) | Muestra el prompt de una generación y pregunta antes de enviarlo al modelo; se activa y desactiva en el menú de ajustes de la historia. |
| [Instruction Exporter](instruction-exporter.md) | Copia al portapapeles las instrucciones, los ajustes de escena y los personajes de la historia. |
| [Knowledge Tracker](knowledge-tracker.md) | Lo que sabe y lo que no sabe cada personaje seguido, incluido en el prompt para que el modelo deje de atribuir a los personajes conocimientos que no pueden tener. |
| [Lore Generator](lore-generator.md) | Escribe con el modelo el contenido de una entrada del libro de lore, a partir del lore que ya tienes. |
| [Lorebook VCS](lorebook-vcs.md) | Historial de revisiones de las entradas del libro de lore. |
| [Manga Generator](manga-generator.md) | Convierte partes de la historia en un guion de manga con páginas y viñetas, y en etiquetas para generar imágenes. |
| [Tools for the model](tools.md) | El botón **Tools** de los generadores: qué conjuntos de herramientas (nombres, lugares) puede usar el modelo, y cuáles de sus categorías. |
| [Name generator](name-generator.md) | Nombres para que el modelo los use en lugar de inventarlos: unas 1270 listas de nombres, en forma de cadenas de Markov. |
| [Name provider](name-provider.md) | Nombres reales entre los que el modelo puede elegir: nombres de pila y apellidos de unos 100 países, con la grafía original. |
| [Place provider](place-provider.md) | Lugares reales que el modelo puede consultar: ciudades, mares, bahías, ríos, montañas y más, con coordenadas y países, qué hay alrededor de un lugar y la distancia entre dos. |
| [Real Language Translator](real-language-translator.md) | El habla de tus personajes en una lengua real (japonés para una historia ambientada en Japón), con el inglés guardado detrás para los modelos y un clic para el lector. |
| [Reviewer](reviewer.md) | Reseñas de partes de la historia escritas por el modelo: reacciones de lectores, crítica. |
| [Side Query](side-query.md) | Un chat con el modelo sobre tu libro, junto a la historia. |
| [Converter](sillytavern-converter.md) | Un chat de SillyTavern junto a la historia y los detalles de los turnos que el modelo prepara a partir de él, para reescribir el chat como historia; o las partes de un libro anterior (de una copia de seguridad o del propio libro) con sus instrucciones, para rehacerlo. |
| [State Tracker](state-tracker.md) | El estado actual de los personajes seguidos (ropa, lo que llevan, el arma que tienen a mano, heridas, ánimo, o tus propias categorías), incluido en el prompt para que el modelo se mantenga fiel a él. |
| [Story Notes](story-notes.md) | Una pequeña wiki de tus propias páginas (etiquetas, enlaces entre ellas) en la barra lateral, para tramas, ideas y reglas del mundo; el asistente de tramas y los agentes pueden leer y modificar las páginas que les permitas. |
| [Read Aloud](tts-reader.md) | Lee en voz alta un libro publicado en el visor, con la voz de tu dispositivo. |
| [Chinese localization](chinese-localization.md) | Chino simplificado como idioma de la interfaz de usuario. |

<a id="installing"></a>

## Instalación

Las extensiones las instala un **administrador** para toda la instalación de Marginalia, en *Admin → Extensions*:
son archivos JAR, que se suben con **Load Extension (.jar)**. Consulta [Gestionar extensiones](../administration/extensions.md)
para más detalles.

Una vez instalada, una extensión está disponible para todos los usuarios. Una extensión cargada mientras Marginalia se ejecuta aparece
en las ventanas que se abran después: cierra y vuelve a abrir un libro (o recarga la página) para verla.

!!!warning Usa versiones coincidentes
Las extensiones se conectan a la interfaz de usuario de una versión concreta de Marginalia. Una extensión creada para otra versión puede
dejar de funcionar o romper partes de la aplicación. Usa las extensiones que vienen con tu versión de Marginalia.
!!!

Las extensiones anteriores forman parte del código fuente de Marginalia (`marginalia/plugins`) y se compilan por separado;
consulta el [README de los plugins](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md).

<a id="extensions-on-a-phone"></a>

## Extensiones en un teléfono

El [diseño para teléfono](../phone.md) tiene sus propias clases, así que una extensión tiene que ser compatible con él. Arc Outline, Author's Note, Beta Readers, Chapter Marker,
Character Tracker, Instruction Exporter y Reviewer lo son; Side Query, Lore Generator y Lorebook VCS no añaden nada en un teléfono (consulta [Usar Marginalia en un teléfono](../phone.md#extensions)). Name generator, Name provider, Place provider, Gemma 4 tools fix y Google Gemini no tienen pantallas propias (Google Gemini solo añade un proveedor al diálogo de modelos, que un teléfono también tiene), y Chinese localization está en el selector de idioma del menú del teléfono. Las extensiones se instalan desde la página de Admin,
que un teléfono no tiene.

<a id="extension-settings"></a>

## Ajustes de extensiones

Las extensiones con ajustes añaden una sección en **Settings → Extension Settings** (la tercera pestaña de la página *Settings*).
Estos ajustes pertenecen a tu cuenta, como el resto de *Settings*, y se guardan con su botón **Save**.

<a id="where-extension-data-is-kept"></a>

## Dónde se guardan los datos de las extensiones

Las extensiones guardan sus datos junto con los datos a los que pertenecen: las reseñas con la parte de la historia, las conversaciones con el
libro, las revisiones con el libro de lore. Por eso se incluyen en las [copias de seguridad de la base de datos](../administration/database-backups.md).
Si también forman parte de las [copias de seguridad de libros](../books/backups.md) y de las exportaciones, se describe en la página de cada extensión.

Desinstalar una extensión no borra sus datos: al volver a instalarla, los datos regresan.
