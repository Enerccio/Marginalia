---
label: Traductor de lengua real
order: 3
---

# Traductor de lengua real

Permite que los personajes de tu historia hablen en una **lengua real**: una historia ambientada en Japón puede escribirse en inglés por ti y el modelo, y el discurso directo de los personajes se convierte en japonés auténtico (kana y kanji, o romaji), con el inglés conservado detrás. Es el [Traductor de conlangs](conlang-translator.md) para una lengua que existe: la misma idea y las mismas pantallas, sin léxico.

!!!
El texto de la historia contiene la **traducción**. Todo lo que lee la historia para un modelo (la siguiente generación, los resúmenes, el escaneo del libro de lore, las anotaciones, las otras extensiones) lee el **inglés** del que se tradujo el discurso, así que escribes y diriges la historia en inglés. Al hacer clic en un discurso traducido se muestra el inglés.
!!!

!!!warning Trabajo en curso
La traducción de partes, rangos y frases, los ajustes, la pestaña del libro, el visor, la exportación y los [comandos](#commands) ya están disponibles. No están todavía un segundo pase que pula la traducción, varias lenguas reales en un mismo libro ni una lengua por personaje, véase [Límites](#limits).
!!!

<a id="the-idea"></a>

## La idea

1. Una **lengua** es un conjunto breve de reglas para el traductor: el **sistema de escritura** (kana y kanji, solo kana, romaji), si se escriben los **sufijos honoríficos** de los nombres, cómo se elige el **nivel de habla** a partir de la relación entre quien habla y quien escucha, cómo se escriben los **nombres** y cualquier otra cosa a la que quieras atenerte. El modelo conoce la lengua en sí, así que no hay léxico. Las lenguas se guardan en tus ajustes, una sola vez, y cualquier número de libros las usa.
2. Un **libro** (o una carpeta de libros) elige su lengua. Una parte se traduce **a mano** desde su menú, **sola** cuando está terminada, o **por rangos**.
3. Una solicitud al modelo por parte clasifica cada discurso directo (¿se dice en la lengua, o en inglés, por este personaje, entendido por el oyente?) y traduce el discurso que lo es, con la voz y el nivel de habla que el personaje y la relación requieren.
4. El inglés se conserva (en los datos del libro, como *origen* del discurso) y lo lee todo modelo. La traducción es el original: lo que cambies a mano en ella se mantiene, y una parte que editas nunca se sobrescribe a espaldas tuyas.

La extensión comprueba lo que puede sin coste: el discurso entrecomillado debe estar en la parte, la lengua debe ser la del libro, y la traducción debe estar en el **sistema de escritura** que pediste (una traducción en romaji con kanji, o una solo en kana con kanji, se conserva y se *notifica*). No puede comprobar el japonés en sí: lee unas cuantas partes antes de una ejecución larga.

<a id="languages"></a>

## Lenguas

Una lengua se edita en **Settings → Ajustes de la extensión → Real Language Translator**.

| Botón | |
|---|---|
| **Add** | Una lengua nueva. Empieza como uno de los preajustes (japonés en kana y kanji, solo en kana, en romaji, u otra lengua), que rellena los campos; cambia lo que quieras. |
| **Editar**, **Eliminar**, **Subir**, **Bajar** | La lengua seleccionada. Al eliminar, se pide confirmación. |
| **Duplicar** | Una copia independiente con un nombre nuevo. Un libro que elige una lengua la comparte con todos los demás libros que la eligen; una copia es la forma de tener una lengua solo para un libro. |

El editor tiene estos campos:

![The languages in the settings](../../images/plugin-reallang-settings.png)

| Campo | |
|---|---|
| **Nombre** | Lo que la llamas ("Japonés, romaji para la novela"). |
| **The language as the model names it** | La palabra con la que responde el modelo para un discurso en la lengua ("Japanese"); vacío: el nombre. |
| **Script** | *Kana and kanji*, *Kana only*, *Romaji (Latin letters)* o *Another script (not checked)*. |
| **Write the honorific suffixes of names** | san, sama, kun... |
| **Also write how it is read (romaji)** | El modelo escribe también la lectura de la traducción; el globo la muestra encima del inglés. |
| **Politeness and speech level**, **Names of people and places**, **Other rules** | Textos breves para el traductor. Los preajustes japoneses traen un texto para los dos primeros (forma llana entre iguales cercanos, forma cortés con desconocidos, keigo con superiores; un nombre extranjero en katakana). |

![The editor of a language](../../images/plugin-reallang-editor.png)

Debajo de la lista están las opciones que los libros y las carpetas **heredan** de ti: la lengua, si una parte se traduce sola, y el modelo y protocolo de la traducción. Pulsa **Guardar** en la pantalla Settings para conservar tus cambios.

<a id="choosing-the-language-of-a-book"></a>

## Elegir la lengua de un libro

La pestaña **Prompts** de la ventana del libro, bajo *Ajustes de la extensión*, tiene los campos de la extensión; la ventana de la carpeta tiene los mismos. Vacío significa *heredar*: la carpeta superior y, después, tus ajustes.

| Campo | |
|---|---|
| **Language of the speech** | La lengua a la que se traduce el libro. Vacío: el libro no se traduce. |
| **Translate a new part by itself** | Una parte se traduce cuando está terminada: tras una generación, tras la anotación automática y la reescritura si el libro las tiene, como paso de [lo que sigue a la parte](../books/story-editor.md#what-follows-a-generated-part) (el panel de las ejecuciones lo muestra, y su **Detener** lo para). |
| **Model of the translation**, **Protocol of the translation** | Vacío: el modelo y el protocolo del libro, si no, tus valores por defecto. |
| **Readers of the published book see** | Lo que muestra el [visor](#what-the-reader-sees). |

![The fields of a book](../../images/plugin-reallang-book.png)

<a id="the-real-language-tab-of-the-book"></a>

## La pestaña Real language del libro

La ventana de la historia tiene una pestaña **Real language** (en el mismo panel que los capítulos y el libro de lore) para la lengua de *este* libro, con **Actualizar** para volver a leerla.

![The Real language tab of the story, with the speakers and their understanding](../../images/plugin-reallang-tab.png)

**Speakers.** Quién habla la lengua **en este libro** (un libro con [más de una lengua](#more-than-one-language-in-a-book) tiene una pestaña *Speakers* para cada una). Por defecto todos la hablan salvo los personajes listados (o solo los listados). El texto puede anular esto ("ella dijo en inglés", "el guardia no entendió ni una palabra"). Para cada personaje anota el **género** y la **voz**: el nivel de habla lo sigue. Escribe aquí la edad, el rango y cómo habla el personaje con los demás ("un joven oficial, cortés con sus superiores, brusco con los amigos"): eso permite que el traductor elija entre la forma llana y el keigo. Los hablantes pertenecen al libro.

Cada personaje puede tener además un nivel de **comprensión**: *no entiende nada*, *conoce unas pocas palabras*, *sigue la mayor parte* o *sigue todo*. Déjalo vacío (o *sigue todo*) y a los modelos no se les dice nada. Úsalo para quienes oyen la lengua sin hablarla: *Tomáš: no entiende nada (japonés)*, *Otros aldeanos: sigue la mayor parte* (el nombre es solo texto, así que vale un grupo). Los niveles son **fijos durante todo el libro** y se entregan a los modelos como información; lo que hace un personaje con ello lo decide el modelo, y siempre puedes decir más en las instrucciones ("Tomáš no entiende lo que dicen"). Llegan a los modelos de dos formas: el **modelo de la historia** recibe un mensaje breve con los niveles (insertado en el prompt como la Nota del autor; la casilla encima de la lista lo desactiva, y el número indica cuántos mensajes desde el final se inserta), y el modelo que **revisa una parte** (*Anotar con IA*) los recibe como una sección de los hechos, para que pueda señalar a un personaje que reacciona a palabras que no puede entender. Si prefieres colocar el texto tú mismo, desactiva la inserción y usa el {%{`{{command::reallang-understanding}}`}%} [macro](../templates/macros.md#commands) en una plantilla; acepta una lista opcional de personajes que conservar.

**Lenguas.** Las otras lenguas del libro, además de la que nombra su campo **Language of the speech**: véase [más abajo](#more-than-one-language-in-a-book).

**Traducción rápida.** Escribe un texto en inglés, opcionalmente quién lo dice y a quién, y pulsa **Traducir**: el modelo lo traduce con las reglas de la lengua (con varias lenguas, elige cuál). No se escribe nada en la historia; copias la traducción.

**Glosario.** Cómo se escriben en la lengua los nombres y las palabras especiales de la historia (una [sección más abajo](#the-glossary-of-names-and-special-words)); una pestaña *Glossary* para cada lengua del libro.
<a id="the-glossary-of-names-and-special-words"></a>

## El glosario de nombres y palabras especiales

Sin un glosario, el modelo vuelve a elegir en cada parte cómo se escribe un nombre: `Saellen` es `サエレン` en una parte y queda en letras latinas en la siguiente, y `Tomáš` se convierte en `トマーシュ` o `トマシュ`. El glosario fija la elección una sola vez. Una entrada tiene

- la **palabra** tal como la escribe la historia (`Saellen`, `witch`), que es la clave de la entrada; no importan las mayúsculas;
- **cómo se escribe** en el idioma (`サエレン`, `魔女`), o *conservarlo tal cual* en el caso de un título, un lugar o un término inventado que no se traduce;
- **cómo se lee** (opcional, en un idioma con lectura: `魔女` se lee `ウィッチ`), y una **nota** para el traductor (el tratamiento honorífico que recibe el personaje, el orden del apellido);
- si se **aplica** o no.

**Tres niveles, combinados.** El glosario de un idioma existe en tres niveles: el **idioma** (en su editor, en *Settings → Extension Settings*: los nombres y términos de todos los libros en ese idioma), las **carpetas** en las que está el libro (una serie define los nombres que comparten sus libros; las carpetas pueden anidarse) y el **libro** (el suyo propio, que es el que más se usa). El modelo recibe la *unión* de los tres; para una misma palabra, **gana el nivel más cercano**, de modo que un libro puede escribir un nombre de otra forma que su carpeta. La pestaña *Glossary* de la pestaña *Real language* muestra todas las entradas, con el nivel del que procede cada una, y permite cambiar una entrada allí donde esté. Una palabra de un nivel superior que este libro no debe usar se **desactiva para este libro** (una entrada del libro que está desactivada; para volver a activarla se hace de la misma manera, o se elimina para recuperar la palabra del nivel superior). Una entrada del libro se puede **subir** a una carpeta o al idioma cuando resulta ser común. El glosario de una carpeta y de un libro se guarda **para cada idioma** del libro, porque un nombre se escribe de forma distinta en cada idioma.

**Qué hace la traducción con él.** El glosario forma parte de la petición, así que el modelo escribe las palabras tal como aparecen en la lista. Un glosario de hasta 100 entradas se envía completo en cada petición (un servidor que guarda el prompt en caché lo lee una sola vez); uno más grande se envía con las entradas que necesita la parte. Después de la respuesta, el complemento **comprueba** el resultado: una réplica cuyo texto en inglés contiene una palabra del glosario, pero cuya traducción no la escribe como indica el glosario, se conserva y se **informa** (el autor decide). Si el modelo **tuvo que elegir** una forma escrita para un nombre que el glosario no recoge, ese nombre se añade al glosario del libro como **propuesta** (la columna *Status* dice *Proposed*): el modelo lo usa a partir de ese momento, *Accept* indica que lo ha revisado y *Edit* permite cambiarlo. Una palabra que ya tiene algún nivel (aunque esté desactivada) no se vuelve a proponer nunca.

![El glosario del libro: las entradas de los tres niveles; un nombre propuesto por el modelo espera a ser aceptado](../../images/plugin-reallang-glossary.png)

![Una entrada del glosario](../../images/plugin-reallang-glossary-entry.png)

![El glosario al final del editor de un idioma](../../images/plugin-reallang-editor-glossary.png)

Los comandos para ello están en la tabla de abajo. Una parte que un libro tradujo antes de que existiera el glosario conserva su traducción; tradúzcala de nuevo para usar el glosario.

<a id="extracting-the-words-by-model"></a>

## Extraer las palabras con un modelo

Escribir el glosario a mano es lento en un libro largo. La pestaña **Word extraction** (junto a las pestañas del glosario de la pestaña *Real language*) hace que el modelo lea un **rango de partes** y enumere los nombres y palabras especiales que encuentra, con la forma que tiene cada uno en el idioma. Todo lo que encuentra y que el glosario aún no tiene se añade al glosario del libro como **propuesta**: el modelo lo usa desde la siguiente petición, y usted lo acepta, lo edita o lo elimina en la pestaña *Glossary*. La historia no cambia.

![La pestaña Word extraction después de una ejecución: los términos que encontró el modelo están en el glosario como propuestas](../../images/plugin-reallang-extraction.png)

- **Language.** Si el libro tiene más de un idioma, el idioma para el que es el glosario.
- **Prompt.** Instrucciones para el modelo: qué buscar y cómo se escribe cada tipo de palabra en el idioma. Elija una de la biblioteca o *None: only my specification*. El complemento incluye un prompt para japonés (lugares y organizaciones reales en su forma habitual en japonés, nombres extranjeros inventados en katakana, nombres japoneses en kanji con su lectura, términos acuñados del mundo de la historia en kanji con lectura en katakana, *gikun*); está marcado como *built-in* y siempre está disponible.
- **Specification.** Texto libre para *este* libro, que se añade después del prompt: qué dejar fuera (personajes secundarios), cómo escribir un tipo de palabra en este libro, una ortografía que usted quiera. Sin prompt, es todo lo que se le dice al modelo.
- **From part / To part**, y después **Extract the words**. Las partes se leen una tras otra (cada parte son minutos del tiempo de un modelo lento); la pestaña muestra *Part 3 of 12*, y **Stop** detiene la ejecución después de la parte que se está leyendo. Una parte que falla se cuenta y la ejecución continúa; un libro sin idioma, sin modelo, o sin prompt ni especificación termina la ejecución.

Las palabras que el glosario ya tiene (en cualquier nivel, aunque estén desactivadas) se le indican al modelo y no se vuelven a proponer, así que una segunda ejecución sobre el mismo rango solo añade lo nuevo. La *categoría* en la que el modelo clasificó un término («Fictional coined term / gikun») se escribe en la nota de la entrada. No necesita describir la salida: el complemento pide un formato fijo, diga lo que diga el prompt.

**La biblioteca de prompts.** *Settings → Extension Settings → Real Language Translator* tiene la lista **Extraction prompts**: sus propios prompts, con un nombre y las instrucciones, para añadir, editar, eliminar y reordenar. **Copy a built-in prompt** pone en su lista una copia del prompt incluido para que la modifique. Los prompts le pertenecen a usted, no a un libro; un libro recuerda cuál eligió y su especificación.

![La biblioteca de prompts de extracción en los ajustes](../../images/plugin-reallang-prompts.png)

<a id="more-than-one-language-in-a-book"></a>

## Más de un idioma en un libro

Una historia con hablantes de japonés y de coreano necesita ambos. Un libro nombra **un idioma primero** (el campo de arriba, que también puede venir dado por una carpeta o por sus ajustes), y la pestaña **Languages** añade **otros idiomas** desde su biblioteca, uno a uno, y los quita de nuevo.

- **Cada idioma tiene sus propios hablantes**, en una pestaña propia. El primer idioma lo hablan todos, salvo los personajes que usted indique, como antes; **un idioma que añada empieza como «solo lo hablan los personajes indicados», sin nadie indicado**, así que no se le pide nada hasta que indique quién lo habla. Indique a un personaje como excepción del primer idioma cuando otro idioma sea el suyo; de lo contrario, se pedirá el primer idioma también para sus réplicas (una réplica que reclaman dos idiomas se queda con el que se pide primero). Un personaje puede hablar más de un idioma (indíquelo en ambos; el texto de la parte le dice al modelo en cuál de ellos está cada réplica).
- **Una petición para cada idioma**: las reglas y las notas de un idioma, y solo las réplicas de sus hablantes. Una parte con réplicas de dos idiomas requiere dos peticiones, una tras otra; la segunda lee la parte con el inglés de las réplicas del primer idioma y deja esas réplicas traducidas. Una parte cuyo punto de vista y personajes presentes están todos fuera de los hablantes de un idioma no se pide para ese idioma (una parte que no nombra a ningún personaje se pide para todos los idiomas que tengan hablantes).
- **Un pasaje conoce su idioma.** **Translate dialogs (real language)** de nuevo para un idioma deja en paz las réplicas de los demás, y **Put the English back** puede aplicarse a un idioma o a todos.
- **Los menús preguntan qué idioma** cuando el libro tiene más de uno: el menú de una parte (uno de los idiomas o *All languages*) y el clic derecho sobre una frase seleccionada (un idioma).
  **Translate parts** tiene un campo *Idioma* (todos los idiomas por defecto); *the parts that are not translated* significa «no traducidas en ese idioma».
- **La ejecución automática** traduce una parte nueva a cada idioma cuyos hablantes puedan aparecer en ella.
># Traducir

<a id="translating"></a>

## Traducción manual

- **A mano:** el menú de una parte tiene el submenú **Traducir** (el clic derecho sobre el texto también lo tiene), y en él **Traducir diálogos (idioma real)** (estos elementos y **Traducir partes (idioma real)** solo se muestran en un libro que tiene un idioma de este plugin). Una parte que ya está traducida se vuelve a traducir **desde su inglés**. Un libro con más de un idioma pregunta primero [cuál](#more-than-one-language-in-a-book).
- **Por sí solo:** con **Traducir una parte nueva por sí solo**, una parte generada se traduce en cuanto es definitiva. Un *regenerar* de una parte le da inglés nuevo y elimina la traducción anterior.
- **Seguimiento de una traducción.** Una traducción de una parte (a mano, de una selección o por sí sola) no bloquea la página: aparece listada en el [panel de las ejecuciones](../books/story-editor.md#the-panel-of-the-runs) abajo a la izquierda, con el final del **razonamiento** del modelo o de su **respuesta** según van llegando, y un botón **Detener**. Mientras tanto la parte muestra *Traduciendo el habla de la parte...*, y nada más puede traducirla. **Detener** cancela la petición al instante y no inicia ninguna más; con varios idiomas, los idiomas que se escribieron antes de ella quedan traducidos. La traducción de un [rango](#translating) tiene su propia ventana, que se mantiene abierta junto a la historia.
- **Diálogos que el modelo se saltó:** a veces una traducción deja un diálogo en inglés. **Traducir → Traducir diálogos que faltan (idioma real)** en el menú de una parte traduce solo los diálogos que siguen en inglés y deja como están los ya traducidos; con texto seleccionado, el clic derecho **Traducir → Traducir diálogos que faltan en la selección (idioma real)** lo hace para los diálogos que toca la selección. Al modelo se le envía la parte tal como está, con los diálogos traducidos incluidos, y se le pide solo los diálogos que siguen en inglés, de modo que la ejecución es más barata que una traducción de la parte completa. Solo se escribe diálogo, así que no sirve para traducir una frase que el modelo no ve como diálogo: para eso usa *Traducir el texto seleccionado*.
- **Una frase que escribiste:** selecciónala en el texto de una parte, haz clic derecho y elige **Traducir → Traducir el texto seleccionado (idioma real)**. El modelo la traduce con la historia que la rodea; el inglés se conserva como fuente. Una selección que contiene un diálogo traducido se rechaza.
- **Un rango:** **Traducir partes (idioma real)** en el menú de herramientas de la historia abre una ventana que se mantiene abierta junto a la historia: *Traducir las partes que no están traducidas*, *Volver a traducir las partes traducidas* o *Devolver el inglés*, de una parte a otra, con un botón **Detener**.

![La ventana que traduce un rango de partes](../../images/plugin-reallang-range.png)

Una traducción se guarda como cualquier cambio del texto: el historial de la parte recibe una versión, la parte **no se marca como editada** y el inglés se conserva. **Un diálogo que cambiaste a mano se mantiene tal cual** cuando la parte se vuelve a traducir o se devuelve al inglés, porque contiene tus palabras.

<a id="what-the-reader-sees"></a>

## Lo que ve el lector

Un diálogo traducido tiene una **línea punteada**; al hacer clic en ella se muestra el inglés en un popover: la lectura (cuando el idioma la requiere), quién lo dice y si el oyente no lo entiende, y una nota roja cuando editaste la traducción después de hacerla.

**Editar la fuente** (un botón del popover, solo en tus propias vistas) abre una ventana con la traducción tal como está en la historia y el inglés que leen los modelos. Úsalo cuando un hablante nativo corrigió el japonés a mano y el inglés dice otra cosa ahora, o cuando el modelo entendió mal el inglés. Al guardar, el diálogo recibe su nuevo inglés, toma la traducción tal como está ahora como la que corresponde a ese inglés (la nota roja desaparece) y los modelos leen el nuevo inglés en su lugar. El texto de la parte no cambia. `reallang-edit-source` hace lo mismo desde la línea de comandos.

**Buscar y reemplazar en las fuentes** (el botón con las flechas en la parte superior de la pestaña *Idioma real*) abre la ventana de buscar y reemplazar de la aplicación sobre los textos en inglés de los diálogos traducidos de la rama activa (texto simple o expresión regular). Solo cambian las fuentes; las palabras de la traducción en sí se reemplazan con la búsqueda y reemplazo del libro.

![La ventana que edita la fuente de un diálogo](../../images/plugin-reallang-source.png)

![Buscar y reemplazar sobre los textos en inglés de los diálogos](../../images/plugin-reallang-search-replace.png)

![Un diálogo traducido con el inglés en un popover](../../images/plugin-reallang-story.png)

El [libro publicado](../books/publishing.md) lo muestra de la forma que pida el libro, con **Los lectores del libro publicado ven**: *la traducción, el inglés al hacer clic* (el valor por defecto), *solo la traducción* (el inglés nunca se envía al navegador del lector) o *solo el inglés*.

<a id="exporting"></a>

## Exportar

Cuando el libro tiene diálogos traducidos, el diálogo de [exportación](../books/export.md) tiene el campo **Diálogos traducidos (idioma real)**: *la traducción tal como está*, *solo el inglés*, *la traducción con el inglés en notas al pie* o *el inglés con la traducción en notas al pie*. El texto guardado del libro nunca se cambia con una exportación.

![El diálogo de exportación con los diálogos traducidos](../../images/plugin-reallang-export.png)

<a id="together-with-the-conlang-translator"></a>

## Junto con el Traductor de lenguas inventadas

Un libro puede tener ambos: un idioma inventado para los elfos y japonés para el pueblo. Cada plugin tiene sus propios hablantes, su propia configuración de idioma y sus propios pasajes, y los pasajes de los dos no se afectan entre sí. El inglés que lee cada modelo tiene ambos de vuelta. La [exportación](../books/export.md) combina las notas al pie de los dos en una sola numeración, y el visor dibuja el habla de ambos en la misma página. Pon el mismo valor en **Los lectores del libro publicado ven** para los dos: una página se dibuja una sola vez, por el plugin que llegue primero.
<a id="commands"></a>

## Comandos

Todo lo que hacen las pantallas también es un comando, tanto para la [línea de comandos](../commands/index.md) como para los [agentes](../agents.md). Un idioma se indica por su **nombre** (no importan mayúsculas ni minúsculas) o por su id; una parte, por su identificador o por su posición en la rama activa (la primera parte es la 1).

| Comando | |
|---|---|
| `reallang-list`, `reallang-create`, `reallang-update`, `reallang-delete` | La biblioteca. `reallang-create` acepta un `preset` (`ja-kana-kanji`, `ja-kana`, `ja-romaji`, `other`). `reallang-delete` pregunta antes. |
| `reallang-set-book-language` | El idioma del libro abierto (el campo **Language of the speech** de sus *Ajustes de la extensión*). |
| `reallang-add-book-language`, `reallang-remove-book-language` | Los otros idiomas del libro abierto. Al quitar uno, se pregunta al usuario. |
| `reallang-status` | El idioma del libro y qué partes están traducidas. |
| `reallang-translate-part`, `reallang-translate-range`, `reallang-translate-sentence`, `reallang-translate-text`, `reallang-revert-to-source` | Traducción: una parte (`missingOnly` traduce solo los parlamentos que siguen en inglés; `within` cita un pasaje para limitarlo a él), un rango (`mode` *new* o *again*), una frase de una parte, un texto que solo se devuelve, y la vuelta al original en inglés. Con el `language` opcional, actúan sobre un idioma del libro (por defecto, sobre todos; en el caso de una frase o un texto, sobre el primero). |
| `reallang-glossary`, `reallang-glossary-set`, `reallang-glossary-remove`, `reallang-glossary-accept`, `reallang-glossary-switch`, `reallang-glossary-move` | El [glosario](#the-glossary-of-names-and-special-words) del libro abierto en un idioma: leer las entradas combinadas con su nivel, añadir o cambiar una entrada en un `level` (`language`, `folder` con el nombre de `folder`, `book`), quitar una (pregunta), aceptar las propuestas, desactivar o activar para el libro una palabra de un nivel superior, y subir una entrada del libro a un nivel superior. |
| `reallang-extract-terms`, `reallang-extraction-set` | La [extracción de palabras](#extracting-the-words-by-model): `reallang-extraction-set` elige el `prompt` y escribe la `specification` del libro abierto (lo que se omite se mantiene); `reallang-extract-terms` lee las partes de from..to y propone términos para el glosario del libro (`language` para el idioma). |
| `reallang-extraction-prompts`, `reallang-extraction-prompt-set`, `reallang-extraction-prompt-remove` | La biblioteca de prompts de extracción: lista los integrados y los tuyos, añade o cambia uno (si nombras uno integrado, se crea una copia), y quita uno (pregunta). |
| `reallang-edit-source` | Fija el original en inglés de un parlamento traducido (`part`, `speech` tal como lo numera `reallang-list-passages`, `source`); la traducción tal como está ahora cuenta como la que le corresponde. Sustituye al original anterior. |
| `reallang-get-source`, `reallang-list-passages` | Lectura: el texto de una parte tal como lo leen los modelos (el inglés), y los parlamentos traducidos de una parte con su lectura e inglés. |
| `reallang-speakers`, `reallang-set-speakers-mode`, `reallang-set-speaker`, `reallang-remove-speaker` | Quién habla el idioma en el libro abierto (`language` indica uno de los idiomas del libro; por defecto, el primero). `reallang-set-speaker` también fija el nivel de `understands`. |
| `reallang-set-understanding-prompt` | Si los niveles de comprensión se insertan solos en el prompt de la historia, y cuántos mensajes desde el final. |
| `reallang-understanding` | Quién no sigue qué idioma, como texto, para la {%{`{{command::reallang-understanding::<characters>}}`}%} [macro](../templates/macros.md#commands) (los personajes son opcionales). |

Un idioma es unas pocas líneas de reglas y no tiene copia de trabajo para una ejecución de agente (el Conlang Translator copia un idioma junto con su léxico): un agente que crea o cambia un idioma modifica tu biblioteca de inmediato. Los hablantes, los ajustes del libro y los pasajes pertenecen al libro, y la copia del libro que usa la ejecución los lleva consigo.

<a id="limits"></a>

## Límites

- **La calidad depende del modelo.** La cortesía es lo más difícil del japonés; las reglas y las notas sobre los caracteres la orientan, y unas pocas partes leídas por una persona deciden si bastan.
- **Los hablantes deciden qué idiomas se piden**, a partir del punto de vista y de los personajes presentes en una parte. Un parlamento de un personaje que la parte no nombra puede pasar desapercibido para un idioma cuyos hablantes están listados; dentro de una petición, decide el texto de la parte.
- **No hay segunda pasada** que reescriba una traducción para hacerla más natural, ni comprobación por retraducción por ahora.
- **No hay furigana.** La lectura es una línea en el globo emergente, no texto ruby en la página.
- **Una parte que traducó el otro plugin de traducción** la lee este modelo con esa traducción en el texto (no con su inglés); solo se le indica que el parlamento de ahí está en otro idioma.
- **En el teléfono** están el panel y el editor; la parte de la historia (menús, ventana de rango, clic en la historia) no se ha probado allí.
