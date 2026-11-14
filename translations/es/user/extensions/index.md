---
label: Extensiones
order: 930
icon: plug
---

# Extensiones

Las extensiones añaden funciones a Marginalia sin cambiar la aplicación en sí. Marginalia incluye veintitrés extensiones:

| Extensión | Qué añade |
|---|---|
| [Arc Outline](arc-outline.md) | Los arcos del libro y sus capítulos, con su estado, en la barra lateral; el modelo puede escribir un arco por ti. |
| [Anthropic Claude](anthropic.md) | Los modelos Claude de Anthropic como proveedor de inferencia, a través de la API propia de Anthropic, con los resúmenes del pensamiento mostrados como razonamiento. |
| [Audiobook](audiobook.md) | Convierte un libro en audiolibro con un servidor de voz: una voz para el narrador y para cada personaje, un estudio para la versión final, exportación a mp3 con marcas de capítulo. [Configurar el servidor de voz](audiobook-server.md). |
| [Author's Note](authors-note.md) | Una nota para el modelo, insertada en cada generación del libro. |
| [Beta Readers](beta-readers.md) | Lectores que inventas (personajes) y un chat con ellos sobre el libro, individualmente o en grupo. |
| [Book Description Generator](book-description-generator.md) | Escribe el nombre y la descripción del libro con el modelo a partir de una parte de su historia, con el botón de la pestaña About. |
| [Chapter Marker](chapter-marker.md) | Títulos de capítulo en el esquema de la historia, a partir de los encabezados Markdown de las partes. |
| [Character Tracker](character-tracker.md) | Los personajes del libro con sus relaciones, y un mapa de ellos. |
| [Conlang Translator](conlang-translator.md) | El habla de tus personajes en una lengua construida que describes, con el inglés guardado detrás para los modelos y un clic para el lector. |
| [Gemma 4 tools fix](gemma-4-tools-fix.md) | Hace que funcione la llamada a funciones con los modelos Gemma 4 cuya API espera otro rol para los resultados de herramientas. |
| [Google Gemini](google-genai.md) | Los modelos Gemini de Google como proveedor de inferencia, a través de la API propia de Google, con los resúmenes del pensamiento mostrados como razonamiento. |
| [Image Generator](image-generator.md) | Crea una imagen para una parte de la historia: el modelo escribe el prompt, ComfyUI o una API de imágenes de estilo OpenAI la dibuja, y tú la repites y la aceptas. |
| [Prompt History](prompt-history.md) | Guarda las últimas 20 peticiones enviadas a los modelos en tu sesión (y por cada ejecución de agente), con las respuestas y el razonamiento, para depurar extensiones y revisar prompts; se abre desde Ajustes → Ajustes de extensiones o desde la consola de un agente. |
| [Prompt Inspector](prompt-inspector.md) | Muestra el prompt de una generación y pregunta antes de enviarlo al modelo; se activa y desactiva en el menú de ajustes de la historia. |
| [Instruction Exporter](instruction-exporter.md) | Copia al portapapeles las instrucciones, los ajustes de escena y los personajes de la historia. |
| [Knowledge Tracker](knowledge-tracker.md) | Lo que sabe y lo que no sabe cada personaje seguido, incluido en el prompt para que el modelo deje de dar a los personajes conocimientos que no pueden tener. |
| [Lore Generator](lore-generator.md) | Escribe el contenido de una entrada del libro de lore con el modelo, a partir del lore que ya tienes. |
| [Lorebook VCS](lorebook-vcs.md) | Historial de revisiones de las entradas del libro de lore. |
| [Manga Generator](manga-generator.md) | Convierte partes de la historia en un guion de manga de páginas y viñetas, y en etiquetas para la generación de imágenes. |
| [Tools for the model](tools.md) | El botón **Herramientas** de los generadores: qué conjuntos de herramientas (nombres, lugares) puede usar el modelo, y cuáles de sus categorías. |
| [Name generator](name-generator.md) | Nombres para que el modelo los use en lugar de inventarlos: unas 1270 listas de nombres, como cadenas de Markov. |
| [Name provider](name-provider.md) | Nombres reales entre los que el modelo puede elegir: nombres de pila y apellidos de unos 100 países, con la grafía original. |
| [Place provider](place-provider.md) | Lugares reales que el modelo puede consultar: ciudades, mares, bahías, ríos, montañas y más, con coordenadas y países, qué hay alrededor de un lugar y la distancia entre dos. |
| [Real Language Translator](real-language-translator.md) | El habla de tus personajes en una lengua real (japonés para una historia ambientada en Japón), con el inglés guardado detrás para los modelos y un clic para el lector. |
| [Reviewer](reviewer.md) | Reseñas de partes de la historia escritas por el modelo: reacciones de lectores, crítica. |
| [Side Query](side-query.md) | Un chat con el modelo sobre tu libro, junto a la historia. |
| [Converter](sillytavern-converter.md) | Un chat de SillyTavern junto a la historia y los detalles de turnos que el modelo prepara a partir de él, para reescribir el chat como historia; o las partes de un libro anterior (de una copia de seguridad o del propio libro) con sus instrucciones, para rehacerlo. |
| [Story Notes](story-notes.md) | Una pequeña wiki de tus propias páginas (etiquetas, enlaces entre ellas) en la barra lateral, para tramas, ideas y reglas del mundo; el asistente de tramas y los agentes pueden leer y cambiar las páginas que permitas. |
| [Read Aloud](tts-reader.md) | Lee en voz alta un libro publicado en el visor, con la voz de tu dispositivo. |
| [Chinese localization](chinese-localization.md) | Chino simplificado como idioma de la interfaz de usuario. |

<a id="installing"></a>

## Instalación

Un **administrador** instala las extensiones para toda la instalación de Marginalia, en *Admin → Extensions*:
son archivos JAR, que se suben con **Cargar extensión (.jar)**. Consulta [Gestionar extensiones](../administration/extensions.md)
para más detalles.

Una vez instalada, una extensión está disponible para todos los usuarios. Una extensión cargada mientras Marginalia se ejecuta aparece
en las ventanas abiertas después: cierra y vuelve a abrir un libro (o recarga la página) para verla.

!!!warning Usa versiones compatibles
Las extensiones se conectan a la interfaz de usuario de una versión concreta de Marginalia. Una extensión creada para otra versión puede
dejar de funcionar o romper partes de la aplicación. Usa las extensiones que vienen con tu versión de Marginalia.
!!!

Las extensiones anteriores forman parte del código fuente de Marginalia (`marginalia/plugins`) y se compilan por separado;
consulta el [README de los plugins](https://github.com/Enerccio/Marginalia/blob/master/marginalia/plugins/README.md).

<a id="extensions-on-a-phone"></a>

## Extensiones en el teléfono

La [vista para teléfono](../phone.md) tiene sus propias clases, así que una extensión debe ser compatible con ella. Arc Outline, Author's Note, Beta Readers, Chapter Marker,
Character Tracker, Instruction Exporter y Reviewer lo son; Side Query, Lore Generator y Lorebook VCS no añaden nada en un teléfono (consulta [Usar Marginalia en un teléfono](../phone.md#extensions)). Name generator, Name provider, Place provider, Gemma 4 tools fix y Google Gemini no tienen pantallas propias (Google Gemini solo añade un proveedor al diálogo de modelos, que un teléfono también tiene), y Chinese localization está en el selector de idioma del menú del teléfono. Las extensiones se instalan desde la página de Admin,
que un teléfono no tiene.

<a id="extension-settings"></a>

## Ajustes de extensiones

Las extensiones con ajustes añaden una sección en **Ajustes → Ajustes de extensiones** (la tercera pestaña de la página *Ajustes*).
Estos ajustes pertenecen a tu cuenta, como el resto de *Ajustes*, y se guardan con su botón **Guardar**.

<a id="where-extension-data-is-kept"></a>

## Dónde se guardan los datos de las extensiones

Las extensiones guardan sus datos junto con aquello a lo que pertenecen (las reseñas con la parte de la historia, las conversaciones con el libro, las revisiones con el libro de lore), así que se incluyen en las [copias de seguridad de la base de datos](../administration/database-backups.md).
Si también forman parte de las [copias de seguridad de libros](../books/backups.md) y de las exportaciones, se describe en la página de cada extensión.

Desinstalar una extensión no borra sus datos: al volver a instalarla, reaparecen.
