---
label: Traductor de conlangs
order: 17
---

# Traductor de conlangs

Permite que los personajes de tu historia hablen en una lengua construida. Describes la lengua (sus reglas y sus palabras) y el modelo
escribe el **discurso directo** de los personajes que la hablan en esa lengua, mientras la historia conserva el inglés que hay detrás.

!!!
El texto de la historia contiene el **conlang**. Todo lo que lee la historia para un modelo (la siguiente generación, los resúmenes,
el escaneo del libro de lore, las anotaciones, las demás extensiones) lee el **inglés** del que se tradujo el discurso, así que el modelo
nunca tiene que entender tu lengua para seguir con la historia. Al hacer clic en un discurso traducido se muestra el inglés.
!!!

!!!warning Trabajo en curso
La extensión funciona para una persona que usa las pantallas descritas aquí y, mediante [comandos](#commands), para un agente. El teléfono solo tiene
una parte, véase [Límites](#limits).
!!!

<a id="the-idea"></a>

## La idea

1. Un **idioma** son dos cosas: **reglas** (texto, escrito para el modelo como un libro de gramática) y un **léxico** (las palabras, escritas
   sobre todo por el modelo y juzgadas por ti). Los idiomas se guardan una vez en tus ajustes, y cualquier número de libros los usa.
2. Un **libro** (o una carpeta de libros) elige su idioma. Una parte se traduce **a mano** desde su menú, **por sí sola** cuando
   está terminada, o **por un rango** cada vez.
3. Una petición al modelo por parte clasifica cada discurso directo (¿se dice en la lengua, o en la lengua común de la
   historia, por este personaje, entendido por el oyente?) y traduce el discurso que lo es. Una palabra que la lengua no tiene la inventan
   las reglas, **se informa con el motivo**, y se añade al léxico como **nueva**, para que la aceptes, la cambies, la bloquees o la descartes.
4. El inglés se conserva (en los datos del libro, como *origen* del discurso) y lo lee cada modelo. El conlang es el original:
   lo que cambies a mano en él se mantiene, y una parte que editas nunca se sobrescribe sin que lo sepas.
<a id="languages"></a>

## Idiomas

Un idioma se edita en **Ajustes → Ajustes de la extensión → Conlang Translator**, el panel que enumera tus idiomas.

![Los idiomas en los ajustes](../../images/plugin-conlang-settings.png)

| Botón | |
|---|---|
| **New language** | Un idioma vacío. |
| **Design a language** | Un [asistente](#the-designer) en el que el modelo propone cada parte de un idioma nuevo y tú la editas y la aceptas. |
| **Edit** | El [editor](#the-editor) del idioma seleccionado. |
| **Duplicate** | Una copia independiente con un nombre nuevo, palabras incluidas. Úsala para probar un cambio (una copia es también la única forma de tener un idioma *para un solo libro*: un libro que elige un idioma lo comparte con todos los demás libros que lo elijan). |
| **Compare** | Trae los cambios de otro idioma al seleccionado, tras una [revisión](#comparing-and-merging). |
| **Export**, **Import** | Un idioma como documento (JSON): las reglas y el léxico, sin los vínculos con un libro. Importar un idioma cuyo nombre ya existe ofrece revisar las diferencias o crear un idioma nuevo. |
| **Delete** | Tras una pregunta de confirmación. |

Arriba están el **modelo y el protocolo del diseñador**. Debajo de la lista están las opciones que los libros y las carpetas **heredan** de ti: el idioma, si una parte se traduce por sí sola
y el modelo y el protocolo de la traducción. Un libro o una carpeta puede definir los suyos, véase [Elegir el idioma de un
libro](#choosing-the-language-of-a-book). **Guarda** la pantalla de Ajustes para conservar tus cambios (las palabras que una traducción añadió mientras tanto
no se pierden: un idioma que no tocaste se vuelve a leer al guardar la pantalla).

<a id="the-editor"></a>

### El editor

El editor es una ventana con cinco pestañas; trabaja sobre una copia hasta **Save**, y se puede redimensionar, arrastrar y maximizar.

| Pestaña | |
|---|---|
| **Overview** | El nombre, una descripción para ti, la opción **A hyphen is a part of a word** (desactivada: `thal-orun` y `thalorun` son la misma palabra; activada, el guion forma parte de la forma), y el **example book** y el **example lorebook** que lee el diseñador. |
| **Rules** | Las reglas en **secciones** (sonidos y ortografía, gramática, formación de palabras...). Cada sección indica **quién la lee**: *the translator* (se envía en cada petición), *only the designer* (la historia del idioma, notas para crear palabras) o *nobody* (tus propias notas). El plugin no analiza las reglas: son texto para el modelo. |
| **Categories** | Grupos de palabras (*People & Social*, *Nature*, *Particles*...). El léxico se muestra y se envía al modelo bajo estos encabezados. Una categoría que se **always sent** (partículas, pronombres, números) va en cada petición, incluso cuando el léxico es demasiado grande para enviarlo completo. |
| **Registers** | Los estilos de habla (*formal*, *soft*, *vulgar*, *archaic*...) con una nota. Un concepto puede tener una forma para cada registro; el modelo elige según quién habla y a quién. |
| **Lexicon** | Las palabras, véase más abajo. |

![Las reglas de un idioma](../../images/plugin-conlang-rules.png)

<a id="the-lexicon"></a>

### El léxico

![El léxico de un idioma](../../images/plugin-conlang-lexicon.png)

La tabla tiene una **búsqueda** (por palabra o significado), filtros por **categoría** y **estado**, y las acciones **Add**, **Edit**, **Accept**,
**Lock**, **Retire**, **Delete** y **Accept all new**. Una palabra tiene:

- la **forma** (cómo la escribe la historia), opcionalmente la forma en un **script**, una **categoría** y un **tipo** (raíz, compuesta, partícula,
  nombre, número, frase);
- uno o más **significados** (un homófono tiene varios, cada uno con una categoría gramatical y una nota) y las **otras formas del concepto**
  (la palabra formal, suave y vulgar para lo mismo, que se muestran y se envían juntas);
- la **justificación**: por qué el modelo eligió la palabra (la regla o las raíces). La lees para juzgar la palabra, y vuelve a las peticiones posteriores
  para que la siguiente invención siga la misma lógica;
- un **origen**: tuyo, o del modelo (con la parte para la que se inventó, el modelo y la hora). Una palabra del modelo que editas pasa a ser tuya;
- un **estado**: **New** (el modelo la creó y aún no la has revisado), **Accepted**, **Locked** (el modelo puede usarla pero nunca redefinirla) o **Retired**
  (conservada, no se ofrece al modelo: para una palabra que fue sustituida).

Una palabra que el modelo necesita y que falta en el léxico **nunca se descarta por ser nueva**: se añade de inmediato, para que la siguiente parte diga lo mismo
de la misma manera, y queda como *New*. Una traducción que usa palabras que no están en el léxico y que el modelo **no** declaró se marca, porque
nadie puede saber si la palabra es un error.

<a id="names-for-the-name-generator"></a>

### Nombres para el generador de nombres

La pestaña **Names** del editor pone el idioma a disposición del [generador de nombres](name-generator.md): los nombres que da suenan como el idioma. Añade una **categoría de nombres**
(por ejemplo *Places* o *People*) y marca las palabras del léxico de las que se forma. La **búsqueda** y el **filtro de categoría** reducen la lista, **Check all shown** y **Uncheck all shown**
actúan sobre las palabras visibles, y **Preview names** genera 20 nombres a partir de las palabras marcadas en ese momento, antes de guardar nada. El generador de nombres entrena una cadena de Markov con las
palabras marcadas, así que una categoría necesita unas pocas palabras (una docena o más da mejores nombres).

Solo se guarda tu elección, no las palabras: una palabra que cambias se lee tal como está ahora, y una que eliminas o retiras se deja fuera al generar los nombres. Tras guardar los ajustes, la
categoría se ofrece en el generador de nombres (en el diálogo **Tools** del generador de lore y del esquema de arco, en el comando `generate-names`, en una ejecución de un agente) como *Conlang - idioma - categoría*.
Es tuya: otro usuario de la instalación solo ve sus categorías.

![La pestaña Names: una categoría de nombres, las palabras de las que se forma y veinte nombres a partir de ellas](../../images/plugin-conlang-names.png)

<a id="the-designer"></a>

### El diseñador

El menú **Design with AI** del editor pide ayuda al modelo, siempre con el idioma **tal como está** (sus reglas, registros, categorías, léxico y el example lorebook), y
siempre como una **propuesta**: nada se escribe antes de que la revises.

![El menú del diseñador](../../images/plugin-conlang-designer-menu.png)

| Acción | |
|---|---|
| **Generate words...** | Palabras para una categoría, una cantidad de ellas, con una nota ("la cocina de un campamento de sitio"). |
| **Add a section with AI...** | Una sección nueva de las reglas para lo que falta (números, colores, cortesía) y las palabras que necesita. |
| **Rewrite a section...** | El modelo reescribe una sección como se le pida. El modelo devuelve la **sección completa**, así que una sección muy larga es lenta y costosa: mantén las secciones cortas, un tema por sección. |
| **Review the language** | Una lista de contradicciones, huecos y palabras que rompen el sistema de sonidos, cada una con una sección y una corrección. No se cambia nada. |
| **Ask the designer...** | Una conversación; una respuesta con palabras o secciones puede revisarse como cambios. |

**Design a language** en el panel es un asistente de siete pasos (el encargo, los sonidos, la escritura, la gramática, la formación de palabras, las primeras palabras por categoría y una prueba
con frases). Cada paso es una sección que el modelo propone y tú editas, refinas con una nota y aceptas; el idioma existe solo en la ventana hasta **Finish**.

![El asistente que diseña un idioma](../../images/plugin-conlang-wizard.png)

<a id="comparing-and-merging"></a>

### Comparar y fusionar

**Compare** (y la importación de un idioma que ya tienes) abre una ventana con las diferencias entre dos idiomas, cada una en una línea con una casilla: lo que se añadió,
cambió, eliminó o movió entre los campos, reglas, categorías, registros, palabras y categorías de nombres del idioma. Un cambio que ambos lados hicieron de forma distinta es un **conflicto**:
se conserva tu valor hasta que elijas el suyo o escribas el tuyo. Lo que eliges se escribe en el idioma de la pantalla y se guarda al guardar la pantalla de Ajustes. La
misma ventana muestra lo que propone el diseñador.

![La revisión de las diferencias entre dos idiomas](../../images/plugin-conlang-review.png)

<a id="choosing-the-language-of-a-book"></a>

## Elegir el idioma de un libro

La pestaña **Prompts** de la ventana del libro, bajo *Extension Settings*, tiene los campos de la extensión; la ventana de la carpeta tiene los mismos. Vacío significa *heredar*: la carpeta
superior y, después, los ajustes de tu usuario.

![Los campos de un libro](../../images/plugin-conlang-book.png)

| Campo | |
|---|---|
| **Language of the speech** | El idioma al que se traduce el libro. Vacío: el libro no se traduce. |
| **Translate a new part by itself** | Una parte se traduce cuando es definitiva: tras una generación, tras la anotación automática y la reescritura si el libro las tiene, como un paso de [lo que sigue a la parte](../books/story-editor.md#what-follows-a-generated-part) (el panel de las ejecuciones lo muestra, y su **Stop** lo detiene). |
| **Model of the translation**, **Protocol of the translation** | Vacío: el modelo y el protocolo del libro; si no, tus valores por defecto. |
| **Readers of the published book see** | Lo que muestra el [visor](#the-viewer). |
<a id="the-conlang-tab-of-the-book"></a>

## La pestaña Conlang del libro

La ventana de la historia tiene una pestaña **Conlang** (en el mismo panel que los capítulos y el libro de conocimiento; arrástrala donde prefieras) para la lengua de *este* libro. Tiene cuatro pestañas propias, y **Actualizar** vuelve a leer la lengua después de que la hayas cambiado en la ventana del libro. Un libro con [más de una lengua](#more-than-one-language-in-a-book) tiene un campo junto a **Actualizar** que elige a cuál de ellas se refieren las pestañas *Palabras*, *Hablantes* y *Traducción rápida*.

<a id="words"></a>

### Palabras

El léxico de la lengua del libro, con la búsqueda, los filtros y las acciones del [editor](#the-lexicon): consulta una palabra mientras escribes, edítala, acepta las palabras que inventó el modelo, bloquea una palabra o retírala. El cambio se guarda en tu biblioteca al instante (no se pierden las palabras que una traducción añadió entretanto).

<a id="speakers"></a>

### Hablantes

Quién habla la lengua **en este libro**. Los hablantes pertenecen al libro, no a la lengua: la misma lengua la hablan otras personas en otra historia, así que una lengua que duplicas, exportas o comparas no lleva consigo personajes.

Por defecto **todo el mundo habla la lengua excepto los personajes listados** (o, al revés, solo los listados). Esto es solo el valor por defecto: el texto lo anula ("dijo en la lengua común", "el guardia no entendía ni una palabra") y el modelo decide en cada intervención. Para cada personaje puedes anotar el **género** (una lengua que marca el género lo falla cuando la parte solo dice "ella") y la **voz** ("formal, seca, basta"), que el modelo ajusta a los registros de la lengua. Los cambios se guardan en el libro al instante.

Cada personaje puede tener además un nivel de **comprensión**: *no entiende nada*, *conoce unas pocas palabras*, *sigue la mayor parte* o *lo sigue todo*. Déjalo vacío (o en *lo sigue todo*) y a los modelos no se les dice nada. Úsalo para las personas que oyen la lengua sin hablarla: *Tomáš: no entiende nada*, *Otros aldeanos: sigue la mayor parte* (el nombre es solo texto, así que vale un grupo). Los niveles son **fijos para todo el libro** y se entregan a los modelos como información; lo que haga un personaje con ellos depende del modelo, y siempre puedes decir más en las instrucciones ("Tomáš no entiende lo que dicen").
Llegan a los modelos de dos maneras: el **modelo de la historia** recibe un mensaje breve con los niveles (insertado en el prompt como la Nota del autor; la casilla encima de la lista lo desactiva, y el número indica cuántos mensajes desde el final se inserta) y el modelo que **revisa una parte** (*Anotar con IA*) los recibe como una sección de los hechos, para que pueda señalar a un personaje que reacciona a palabras que no puede seguir. Si prefieres colocar tú el texto, desactiva la inserción y usa la [macro](../templates/macros.md#commands) {%{`{{command::conlang-understanding}}`}%} en una plantilla; acepta una lista opcional de personajes que conservar.

![La pestaña Conlang: los hablantes, y dos personajes que no siguen la lengua](../../images/plugin-conlang-tab.png)

<a id="languages"></a>

### Lenguas

Las otras lenguas del libro, además de la que nombra su campo **Idioma del habla**: véase [Más de una lengua en un libro](#more-than-one-language-in-a-book).

<a id="quick-translate"></a>

### Traducción rápida

Escribe un texto en inglés (una frase, un nombre, una línea que aún no has escrito), opcionalmente quién lo dice y a quién, y pulsa **Traducir**: el modelo lo traduce a la lengua del libro con las reglas, los registros y el léxico, y las notas de los personajes mencionados en el texto. Nada se escribe en la historia. El resultado es la traducción, una nota sobre las decisiones tomadas y las **palabras que necesitó la traducción** que faltaban en el léxico, cada una con su significado y el motivo; selecciona las que quieras y pulsa **Importar las palabras seleccionadas**: entran en el léxico como *nuevas*, igual que las palabras de una parte traducida. Las palabras de la traducción que no están en el léxico ni se han señalado aparecen en una lista de advertencias.

<a id="more-than-one-language-in-a-book"></a>

## Más de una lengua en un libro

Una epopeya puede tener una lengua para los elfos, otra para los enanos y una tercera para los orcos. Un libro nombra **primero una lengua** (el campo de arriba, que también puede venir dado por una carpeta o por tus ajustes), y la pestaña **Lenguas** de la pestaña **Conlang** añade **otras lenguas** de tu biblioteca, una a una, y las quita de nuevo.

- **Cada lengua tiene sus propios hablantes.** La pestaña *Hablantes* se refiere a la lengua que elige el campo de arriba. La primera lengua la habla todo el mundo excepto los personajes que listes, como antes; **una lengua que añades empieza como "solo la hablan los personajes listados" sin nadie listado**, así que no se le pide nada hasta que listes quién la habla. Lista a un personaje como excepción de la primera lengua cuando otra lengua es la suya, o se pedirá la primera lengua también para su habla (un habla que reclaman dos lenguas se queda con la que se pida primero). Un personaje puede hablar más de una lengua (lístalas en ambas; el texto de la parte indica al modelo en cuál está una intervención).
- **Una petición por cada lengua.** Una petición contiene las reglas y el léxico de una sola lengua, y solo las intervenciones de sus hablantes; una parte con intervenciones en dos lenguas requiere dos peticiones, una tras otra. La segunda lee la parte con el inglés de las intervenciones de la primera y deja esas intervenciones traducidas. Una parte cuyo punto de vista y personajes presentes están todos fuera de los hablantes de una lengua no se pide para esa lengua (una parte que no nombra a ningún personaje se pide para cada lengua que tenga hablantes).
- **Un pasaje conoce su lengua.** El menú emergente, la exportación y el visor funcionan como antes; **Traducir diálogos (lengua construida)** para una sola lengua deja en paz las intervenciones de las demás, y **Devolver el inglés** puede aplicarse a una lengua o a todas.
- **Los menús preguntan qué lengua** cuando el libro tiene más de una: el menú de una parte (*Traducir diálogos (lengua construida)*: una de las lenguas o *Todas las lenguas*) y el clic derecho sobre una frase seleccionada (una lengua).
  **Traducir partes (lengua construida)** tiene un campo *Idioma* (todas las lenguas por defecto); *las partes que no están traducidas* significa "no traducidas en esa lengua".
- **La ejecución automática** traduce una parte nueva a cada lengua cuyos hablantes puedan aparecer en ella.
<a id="translating"></a>

## Traducción

- **A mano:** el menú de una parte tiene el submenú **Traducir** (el clic derecho sobre el texto también lo tiene), y en él **Traducir diálogos (lengua construida)** (estos elementos y **Traducir partes** solo aparecen en un libro que tenga una lengua de este complemento). Una parte que ya está traducida se vuelve a traducir **desde su inglés**, así que también es así como se retraduce una parte después de que cambien las reglas o el léxico. Un libro con más de una lengua pregunta primero [cuál](#more-than-one-language-in-a-book).
- **Por sí sola:** con **Traducir una parte nueva por sí sola**, una parte generada se traduce en cuanto queda definitiva. Un *regenerar* de una parte le da un inglés nuevo y elimina la traducción anterior.
- **Diálogos que el modelo pasó por alto:** a veces una traducción deja un diálogo en inglés. **Traducir → Traducir diálogos que faltan (lengua construida)** en el menú de una parte traduce solo los diálogos que siguen en inglés y deja como están los ya traducidos; con texto seleccionado, el clic derecho **Traducir → Traducir diálogos que faltan en la selección (lengua construida)** hace lo mismo para los diálogos que toca la selección. Al modelo se le envía la parte tal como está, con los diálogos ya traducidos, y se le pide solo los diálogos que siguen en inglés, así que la ejecución es más barata que una traducción de la parte entera. No se escribe nada que no sea diálogo, por lo que no sirve para traducir una frase que el modelo no ve como diálogo: para eso usa *Traducir el texto seleccionado*.
- **Una frase que escribiste:** selecciónala en el texto de una parte, haz clic derecho y elige **Traducir → Traducir el texto seleccionado (lengua construida)**. El modelo traduce el texto seleccionado (los espacios y un par de comillas que lo rodeen quedan fuera) con la lengua, las notas de los personajes y la historia alrededor; el texto se sustituye por la traducción y el inglés se conserva como fuente del diálogo, de modo que al hacer clic en él se muestra el inglés como en cualquier diálogo traducido. Las palabras que el modelo tuvo que inventar se añaden al léxico como *nuevas*, igual que con una parte traducida. No se admite una selección que contenga un diálogo traducido (devuelve primero el inglés), ni una selección hecha en una parte que haya cambiado entretanto.
- **Un rango:** **Traducir partes (lengua construida)** en el menú de herramientas de la historia abre una ventana que permanece abierta junto a la historia.

  ![Traducir partes](../../images/plugin-conlang-range.png)

  Elige **Desde la parte** y **Hasta la parte** (partes con texto, contadas desde 1 como en la exportación) y qué hacer: *Traducir las partes que no están traducidas*, *Volver a traducir las partes ya traducidas* (después de que cambien las reglas o el léxico), o *Devolver el inglés*, previa confirmación. La ventana indica cuántas partes tiene el rango y cuántas afecta la elección, muestra *Parte n de m* y un botón **Detener** que surte efecto después de la parte que se está haciendo (una petición al modelo que ya ha empezado no puede retirarse). Una parte que falla se cuenta y la ejecución continúa; una lengua o un modelo que falten la terminan. La historia se vuelve a dibujar al terminar la ejecución. Una parte que edites mientras se ejecuta hace que falle esa parte, no toda la ejecución.

Una traducción se guarda como cualquier cambio del texto: el historial de la parte recibe una versión, la parte **no se marca como editada** y el inglés se conserva. **Un diálogo que hayas cambiado a mano se deja como está** cuando la parte se vuelve a traducir o se devuelve al inglés, porque contiene tus palabras.

!!!
Una traducción tarda lo que tarda el modelo en la parte: minutos en un modelo local que piensa. El complemento pide al modelo que piense (la traducción del género, el número y el registro es mucho mejor así), así que dale un límite de salida generoso.
!!!

<a id="what-the-reader-sees"></a>

## Lo que ve el lector

<a id="in-the-story"></a>

### En la historia

Un diálogo traducido tiene una **línea de puntos**. Al hacer clic en él se muestra el inglés en una ventana emergente: quién lo dijo y *(no entendido por el oyente)* cuando el oyente no entiende la lengua; una nota roja indica que editaste el texto de la lengua construida después de la traducción, así que el inglés puede no decir ahora lo que dice el texto.

**Editar la fuente** (un botón de la ventana emergente, solo en tus propias vistas; los lectores de un libro publicado no lo tienen) abre una pequeña ventana con la traducción tal como la tiene la historia y el inglés que leen los modelos. Úsala cuando un hablante nativo corrigió la traducción a mano y el inglés dice ahora otra cosa, o cuando el modelo entendió mal el inglés. Al guardar, el diálogo recibe su nuevo inglés, toma la traducción tal como está ahora como la que pertenece a ese inglés (la nota roja desaparece) y, desde entonces, los modelos leen el nuevo inglés en su lugar. El texto de la parte no cambia, y *Traducir diálogos (lengua construida)* de nuevo sustituye ambos. `conlang-edit-source` hace lo mismo desde la línea de comandos.

![La ventana que edita la fuente de un diálogo](../../images/plugin-conlang-source.png)

**Buscar y reemplazar en las fuentes** (el botón con las flechas en la parte superior de la pestaña Conlang) abre la ventana de búsqueda y reemplazo de la aplicación sobre los inglés fuente de los diálogos traducidos de la rama activa (texto plano o expresión regular, con o sin distinción de mayúsculas; la tabla lista los diálogos que tienen coincidencias por parte y número de diálogo). Úsala para un nombre que aparece escrito de dos formas en el inglés. Solo cambian las fuentes; para cambiar las palabras de la propia traducción usa la búsqueda y reemplazo del libro. Un diálogo cuya fuente se cambió después de la búsqueda se deja como está y se cuenta.

![Buscar y reemplazar sobre los inglés fuente de los diálogos](../../images/plugin-conlang-search-replace.png)

![El inglés detrás de un diálogo traducido](../../images/plugin-conlang-story.png)

<a id="the-viewer"></a>

### El visor

El visor de un [libro publicado](../books/publishing.md) muestra el diálogo traducido de la manera que indica el libro, con el campo **Los lectores del libro publicado ven** (una elección del libro o de sus carpetas; tus valores por defecto nunca deciden lo que ve un lector):

| | |
|---|---|
| **La lengua construida, el inglés al hacer clic** | El valor por defecto. Un clic (un toque en un teléfono) abre la ventana emergente con el inglés. |
| **Solo la lengua construida** | El inglés nunca se envía al navegador del lector. |
| **Solo el inglés** | El libro como si nunca hubiera sido traducido. |

En el modo de clic, la página solo contiene la lengua construida; el inglés de un diálogo se pide al servidor cuando el lector hace clic en él.

![El inglés detrás de un diálogo traducido, en el visor](../../images/plugin-conlang-viewer.png)

<a id="exporting"></a>

## Exportar

Cuando el libro tiene diálogos traducidos, el diálogo de [exportación](../books/export.md) tiene el campo **Diálogos traducidos**:

![El diálogo de exportación](../../images/plugin-conlang-export.png)

| | |
|---|---|
| **La lengua construida, tal como está** | El valor por defecto: el texto tal como está guardado. |
| **Solo el inglés** | El diálogo se sustituye por su inglés. |
| **La lengua construida, el inglés en notas al pie** | Cada diálogo traducido recibe una nota al pie con el inglés (y el hablante). |
| **El inglés, la lengua construida en notas al pie** | Al revés. |

Las notas al pie son las [notas al pie de la exportación](../books/export.md#footnotes): notas enlazadas en HTML y EPUB, notas finales después de cada parte en texto plano, y notas reales al pie de página en Word y PDF. Un diálogo que editaste después de traducirlo se queda como está y no tiene nota al pie. El texto guardado del libro nunca se cambia con una exportación.
<a id="commands"></a>

## Comandos

Todo lo que hacen las pantallas es también un comando, para la [línea de comandos](../commands/index.md) y para los [agentes](../agents.md): un agente puede crear un idioma, ampliarlo, traducir un libro y comprobar el resultado sin
que una persona tenga que pasar por los diálogos. Un idioma se identifica por su **nombre** (no distingue mayúsculas) o por su id; una sección de las reglas, por su título o por su número en `conlang-list-rules`; una categoría, por su nombre o su número; una palabra, por su
forma (el id de `conlang-list-words` solo sirve para distinguir homófonos); una parte, como en los demás comandos de la aplicación, por su identificador o por su posición en la rama activa (la primera parte es la 1). La línea de comandos acepta todos los
argumentos de un comando; un agente puede omitir los opcionales.

| Comando | |
|---|---|
| `conlang-list`, `conlang-get`, `conlang-create`, `conlang-duplicate`, `conlang-update`, `conlang-delete`, `conlang-export`, `conlang-import`, `conlang-set-references` | La biblioteca. `conlang-delete` pide confirmación y no está disponible para un agente. |
| `conlang-list-name-categories`, `conlang-add-name-category`, `conlang-update-name-category`, `conlang-delete-name-category`, `conlang-preview-names` | Las categorías de nombres para el [Generador de nombres](name-generator.md) (la pestaña **Names**). Añadir y actualizar aceptan las palabras del léxico (separadas por `;`) o una categoría completa del léxico; borrar pide confirmación al usuario; la vista previa necesita el Generador de nombres. Los propios comandos del generador, `list-name-categories` y `generate-names`, listan y crean los nombres. |
| `conlang-set-book-language` | El idioma del libro abierto (el campo **Language of the speech** de sus *Ajustes de la extensión*). |
| `conlang-add-book-language`, `conlang-remove-book-language` | Los otros idiomas del libro abierto. Quitar pide confirmación al usuario y no está disponible para un agente. |
| `conlang-list-rules`, `conlang-get-rules`, `conlang-add-rules`, `conlang-update-rules`, `conlang-delete-rules`, `conlang-move-rules` | Las secciones de las reglas. |
| `conlang-list-categories`, `conlang-add-category`, `conlang-update-category`, `conlang-move-category`, `conlang-delete-category` | Las categorías de las palabras. |
| `conlang-list-registers`, `conlang-add-register`, `conlang-update-register`, `conlang-delete-register` | Los registros. |
| `conlang-list-words` | El léxico, paginado, con filtros: categoría, estado (*new*, accepted, locked, retired) y un texto. |
| `conlang-get-word`, `conlang-add-word`, `conlang-update-word`, `conlang-delete-word` | Una palabra. Una palabra que añade o cambia un agente es del modelo y queda **new**; una palabra bloqueada no se puede cambiar ni borrar. |
| `conlang-lock-word`, `conlang-unlock-word`, `conlang-retire-word`, `conlang-restore-word`, `conlang-accept-words` | El estado de las palabras. |
| `conlang-check-lexicon` | Significados repetidos, palabras sin categoría, compuestos con partes que no están en el léxico y los homófonos (a título informativo). No usa modelo. |
| `conlang-status` | El idioma del libro y qué partes están traducidas. |
| `conlang-translate-part`, `conlang-translate-range`, `conlang-translate-sentence`, `conlang-translate-text`, `conlang-revert-to-source` | Traducir: una parte (`missingOnly` traduce solo los parlamentos que todavía están en inglés; `within` cita un pasaje para limitarlo a él), un intervalo (`mode` *new* o *again*), una frase de una parte, un texto que solo se devuelve, y la vuelta al inglés. Un intervalo pide confirmación al usuario para *again*; también la vuelta al inglés. Con el parámetro opcional `language` actúan sobre un idioma del libro (por defecto, sobre todos; en una frase o un texto, sobre el primero). |
| `conlang-edit-source` | Fija el original en inglés de un parlamento traducido (`part`, `speech` según lo numera `conlang-list-passages`, `source`); la traducción tal como está ahora cuenta como la que le corresponde. Sustituye el original anterior. |
| `conlang-check-translation`, `conlang-get-source`, `conlang-list-passages` | Lectura: las palabras de un parlamento traducido que no están en el léxico, el texto de una parte tal como lo leen los modelos (el inglés) y los parlamentos traducidos de una parte con su inglés. |
| `conlang-speakers`, `conlang-set-speakers-mode`, `conlang-set-speaker`, `conlang-remove-speaker` | Quién habla el idioma en el libro abierto (`language` designa uno de los idiomas del libro; por defecto, el primero); `conlang-set-speaker` también fija el nivel de `understands`. |
| `conlang-set-understanding-prompt` | Si los niveles de comprensión se insertan solos en el prompt de la historia y cuántos mensajes desde el final. |
| `conlang-design-words`, `conlang-design-rules`, `conlang-rewrite-rules`, `conlang-design-stage`, `conlang-ask-designer`, `conlang-try-sentences` | El diseñador. Con `ask true` un comando devuelve la propuesta como una lista numerada de cambios y la retiene; no se cambia nada. |
| `conlang-review` | La revisión del idioma por el diseñador: solo hallazgos. |
| `conlang-apply-changes` | Aplica los cambios retenidos, todos o los números indicados (lo mismo que **Apply** en la revisión). |
| `conlang-pending-changes`, `conlang-take-pending-changes`, `conlang-discard-pending-changes` | Lo que una ejecución de agente dejó en el libro, véase [más abajo](#agent-runs). |
| `conlang-understanding` | Quién no sigue qué idioma, como texto, para la macro {%{`{{command::conlang-understanding::<characters>}}`}%} (los personajes son opcionales). |
| `conlang-lexicon`, `conlang-rules` | El léxico y las reglas que envía una petición de traducción, como texto, para las [macros](../templates/macros.md#commands) {%{`{{command::conlang-lexicon::<language>}}`}%} y {%{`{{command::conlang-rules::<language>}}`}%}. |

### Agent runs

Un agente trabaja sobre una **copia del libro**, y los idiomas no forman parte del libro, así que un agente no puede cambiar tu biblioteca a tus espaldas. La primera vez que una ejecución **cambia** un idioma (una palabra, una regla o las palabras
que una traducción inventa), ese idioma se copia en la copia del libro, junto con la versión de la que procede; a partir de ahí, la ejecución lee y escribe en su propia copia. Una ejecución que solo lee, o que solo traduce con las
palabras que ya tiene, nunca copia nada. La copia va adonde vaya la copia del libro: los puntos de control y su restauración, la revisión de la ejecución y su borrado.

Cuando **aplicas** el resultado de la ejecución, el idioma pasa al libro, **no a tu biblioteca**. La pestaña **Conlang** del libro muestra entonces *An agent run changed the language ...* con **Review** y **Drop**.
Review compara tres versiones: el idioma cuando la ejecución lo copió, la versión de la ejecución y tu biblioteca tal como está ahora; así, lo que hayas cambiado en la biblioteca mientras tanto se conserva, y lo que ambos hayáis cambiado es un conflicto que decides tú. Lo mismo desde los comandos: `conlang-pending-changes` lista los cambios como números y `conlang-take-pending-changes` los toma (te lo pide). Un idioma creado por la ejecución se añade completo cuando confirmas.

La ejecución lee la biblioteca hasta que cambia un idioma por primera vez, y a partir de ahí su propia copia: lo que cambies en un idioma mientras la ejecución trabaja no lo ve la ejecución, pero tampoco se pierde.

<a id="limits"></a>

## Límites

- **La calidad es la del modelo.** La extensión comprueba lo que puede sin coste (que el parlamento citado esté en la parte, el idioma, las palabras frente al léxico y sus reglas para compuestos y afijos) e informa del resto; no comprueba tu gramática. Prueba un idioma en unas pocas partes antes de una ejecución larga, y usa **Review the language** del diseñador cuando un error se repita.
- **Un idioma lo comparten los libros que lo usan.** Una palabra que cambies, o una palabra que añada una traducción, cambia el idioma para todos los libros de la serie. Es lo previsto (un mundo), y **Duplicate** es la forma de mantener un idioma aparte.
- **Los cambios de idioma de un agente esperan en el libro.** Los pasas a la biblioteca tú, una vez aplicada la ejecución; un idioma no se bloquea mientras una ejecución trabaja.
- **Los comandos no sustituyen una palabra en la historia**, y una retraducción no puede conservar las traducciones cuyo inglés no cambió (se vuelve a pedir cada parte del intervalo).
- **En el teléfono** están el panel, el editor, la revisión y el asistente, pero la cuadrícula del léxico se desplaza en horizontal; el toque sobre un parlamento en el visor funciona; la parte de la historia (el menú de una parte, la ventana del intervalo, el clic en la historia) no se ha probado allí.
