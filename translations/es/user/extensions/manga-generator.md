---
label: Generador de manga
order: 8
---

# Generador de manga

Convierte tu historia en un manga: el modelo adapta partes del libro en un guion de páginas y viñetas, y luego escribe etiquetas de generación de imágenes para cada viñeta, listas para Stable Diffusion, Forge, Midjourney y similares. Es un port de la extensión [MangaGenerator](https://github.com/Enerccio/SillyTavern-MangaGenerator) de SillyTavern.

<a id="opening-it"></a>

## Abrirlo

Elige **Manga Generator** en el menú de ajustes (⚙) del [editor de historias](../books/story-editor.md). Se abre una ventana grande; no se añade nada a la pantalla de la historia. Puedes arrastrar la ventana y cambiar su tamaño.

## Mangas

Un libro puede tener varios mangas, que se muestran como pestañas en la parte superior de la ventana. Los botones junto a las pestañas añaden un manga (+), lo renombran, lo mueven a la izquierda o a la derecha y lo borran. Un manga se guarda con el libro, así que está incluido en las copias de seguridad y en las copias del libro.

Debajo de las pestañas están los ajustes del manga abierto:

| Ajuste | Qué hace |
|---|---|
| Modelo y protocolo | Adónde van las peticiones. Por defecto, el modelo y el protocolo del libro; se recuerda la última elección. |
| Presupuesto de tokens | Cuánta extensión se pide al modelo para el guion (`{{tokenBudget}}` en el prompt). |
| Partes por sección | Cuántas partes de la historia adapta una sección nueva. |
| Contexto antes / después | Cuántas partes más antes y después de esas se dan al modelo como trasfondo. |
| Prompt de guion, prompt de etiquetas | Sustituyen los prompts integrados de este manga. **Restablecer el valor predeterminado** devuelve el texto integrado. |
| Reglas adicionales | Va en lugar de `{{additionalRules}}` en el prompt del guion, para tus propias reglas (sin efectos de sonido, un estilo...). |
| Copiar todo | Copia al portapapeles todos los guiones del manga y, después, todos los textos de etiquetas. |
| Modo continuo | Genera el manga por sí solo, ver [Modo continuo](#continuous-mode). |

<a id="settings"></a>

## Ajustes

*Ajustes → Extension Settings → Manga Generator Settings* contiene lo que empieza un **nuevo** manga: el presupuesto de tokens, las partes por sección, el contexto antes y después, el prompt del guion, el prompt de etiquetas y las reglas adicionales, además de un texto que se antepone a cada petición, para instrucciones que valen para todos tus mangas. Un prompt vacío o sin cambios es el integrado. Un manga que ya creaste conserva sus propios ajustes. El panel se guarda con el botón **Guardar** de la página *Ajustes*.

<a id="sections"></a>

## Secciones

**Add section** añade una sección para las partes de la historia que siguen a la última sección (la primera empieza en la parte 1). Las partes se numeran desde 1, como en el esquema. Una sección muestra:

- **Adapt from part / to part**: las partes que el modelo convierte en viñetas.
- **Context from part / to part**: las partes que el modelo lee. Las que quedan fuera del rango adaptado solo sirven de trasfondo (eventos anteriores o lo que viene después), y se le indica al modelo que no las adapte. Cambia cualquiera de los cuatro números cuando quieras otro fragmento.

Cada sección tiene dos vistas, **Script** y **Etiquetas**:

1. **Generate script** escribe el guion en la sección en tiempo real. El razonamiento de un modelo que razona se muestra encima (*Thought for 12.3 s*). El guion continúa la numeración de páginas de la sección anterior y no repite lo que ese guion ya cubre.
2. Cambia a **Etiquetas** y pulsa **Generate tags**: para cada viñeta del guion, se generan etiquetas para los personajes y para la escena.

![La ventana del Generador de manga con el guion de una sección](../../images/plugin-manga.png)

Cualquiera de los dos textos se puede **editar** a mano (el lápiz) y **copiar**. Una generación se puede detener con **Abort**, y lo que el modelo escribió hasta ese momento se conserva. Las secciones se generan de forma independiente, así que puedes lanzar varias a la vez. Mientras algún modelo está escribiendo, no se pueden borrar secciones ni cambiar mangas.

![La misma sección en la vista de Etiquetas](../../images/plugin-manga-tags.png)

**Override** da a una sección su propio prompt de guion, su propio prompt de etiquetas o sus propias reglas adicionales, o bien un **Rules addendum** que se añade a las reglas del manga en lugar de sustituirlas. El ojo muestra la petición exacta que recibiría el modelo, con su tamaño.

<a id="continuous-mode"></a>

## Modo continuo

**Continuous mode** (junto a *Add section*) deja que el manga se escriba solo, un tipo de texto cada vez, de modo que el modelo recibe el mismo prompt una y otra vez (y un servidor que mantiene una caché de prompts puede reutilizarlo). Elige qué genera en el cuadro junto al botón:

- **Scripts only**: recorre las secciones en orden y genera el guion de cada sección que no tenga ninguno (una sección con guion se deja como está). Cuando todas tienen uno, añade una sección para las siguientes partes de la historia, como lo hace *Add section*, con *Parts per section* y el contexto del manga; genera su guion y sigue, hasta que la historia no tenga más partes para otra sección.
- **Tags only**: genera las etiquetas de cada sección que tenga guion pero no etiquetas. No añade secciones; termina cuando todos los guiones tienen sus etiquetas.

En ambos casos termina cuando pulsas **Stop continuous mode** (el botón cambia a ese texto; lo que el modelo escribió hasta entonces se conserva). Un fallo del modelo, una sección sin texto en sus partes o una respuesta vacía también lo detienen, con un mensaje. Mientras se ejecuta, las secciones no se pueden generar ni cambiar a mano, y los mangas no se pueden modificar. Cerrar la ventana mientras se ejecuta la pliega en el [panel de ejecuciones](../books/story-editor.md#the-panel-of-the-runs) y el proceso sigue; cerrarla desde ese panel lo detiene. Cada sección en la que trabaja se desplaza a la vista. Para obtener ambas cosas, ejecuta primero *Scripts only* y después *Tags only*.

<a id="the-lore-and-the-size-of-the-context"></a>

## El lore y el tamaño del contexto

El modelo recibe el texto de las partes de la sección, y con él el lore que ese texto requiere: entradas del lorebook del libro (y de sus sub lorebooks) cuyos filtros aparecen en ese texto, encontradas igual que en una generación, incluida la profundidad de escaneo. También llegan las entradas que están siempre activas. Así, los personajes y lugares de esas partes llegan al modelo con sus descripciones, que necesitan las etiquetas de imagen.

Todo ello tiene que caber en el **tamaño de contexto** del modelo: el contexto de la IA y del protocolo, menos el espacio reservado para la respuesta. Cuando las entradas no caben, se omiten las últimas (un aviso indica cuántas; el ojo también lo muestra). Haz la sección más pequeña o el lore más corto. Una petición que no cabe ni sin lore se envía igualmente, con una advertencia.

<a id="what-is-different-from-sillytavern"></a>

## Qué es distinto de SillyTavern

- Un mensaje del chat es una **parte** de la historia; no hay nombres delante de los mensajes, ni persona ni tarjeta de personaje: los personajes vienen del lorebook.
- El «trigger» de información del mundo de la extensión no hace falta: el lore se activa por el propio texto.
- El tamaño de contexto se toma de la IA y del protocolo; no hay ningún ajuste para ello.
- Una sección nueva toma 3 partes con 1 parte de contexto a cada lado, porque una parte de una historia es mucho más larga que un mensaje de chat. Cámbialo en los ajustes (más abajo).
- Las pestañas se mueven con los botones de flecha, no arrastrándolas.
