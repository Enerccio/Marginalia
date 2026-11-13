---
label: Consulta lateral
order: 1
---

# Consulta lateral

Un chat con el modelo junto a tu historia: pregunta por la trama, comprueba la continuidad, busca nombres o el siguiente capítulo -
sin añadir nada al libro.

!!!
Side Query funciona en el [diseño de teléfono](../phone.md#extensions): su pestaña está en el cajón del esquema de la historia.
!!!

![Side Query junto a la historia](../../images/plugin-sidequery.png)

<a id="the-side-query-tab"></a>

## La pestaña Side Query

La barra lateral izquierda del [editor de historias](../books/story-editor.md) muestra una pestaña **Side Query** junto a *Esquema*. Arrastra el divisor para darle más espacio.

La pestaña contiene conversaciones, cada una en su propia pestaña interna: **+** abre una nueva (*Tab 2*, *Tab 3*...), el lápiz renombra una pestaña, **×** la cierra (la última no se puede cerrar). Con *Enable AI Tab Naming* en el perfil, el modelo pone nombre a una pestaña tras su primera respuesta, salvo que la hayas renombrado tú. Las conversaciones pertenecen al libro y se conservan hasta que cierres su pestaña.

<a id="asking"></a>

## Preguntar

Escribe una pregunta en el cuadro de abajo y haz clic en **SEND**. La respuesta aparece según se va escribiendo; **STOP** (en lugar de *SEND*) la interrumpe.

| Botón | |
|---|---|
| **SEND** | Envía la pregunta. |
| **UNDO** | Elimina el último mensaje. Si era tu pregunta, vuelve al cuadro para editarla. |
| **REGENERATE** | Elimina la última respuesta y vuelve a preguntar. |

Cada mensaje tiene botones para **mover** hacia arriba o hacia abajo, **editarlo**, **copiarlo** al portapapeles y **excluirlo** del contexto (o volver a incluirlo). Los mensajes excluidos siguen en la conversación, pero no se envían al modelo; es útil para descartar una respuesta equivocada sin borrarla.

<a id="what-the-model-sees"></a>

## Qué ve el modelo

Las opciones sobre la conversación determinan qué se envía con tus preguntas:

| Opción | |
|---|---|
| **Lorebook** | El lore del libro (ver abajo). |
| **Chat Logs** *from* … *to* | Las partes de la historia de la rama activa en ese rango, numeradas desde 1 como en el esquema: `1` a `6` son las partes `#1` a `#6`. |
| **Plug button** | Abre una lista con los datos que ofrecen otras extensiones, con una casilla para cada una (ver abajo). |
| **Tokens** | El tamaño aproximado de la petición. |

!!!
*Lorebook* reúne el lore igual que lo hace una generación: el lorebook del libro y sus sublorebooks (se omiten los lorebooks desactivados), las entradas activadas cuyas etiquetas coinciden con las del libro, con los [macros](../templates/macros.md) procesados. No se aplican filtros de palabras clave: como no hay un prompt de historia con el que compararlos, se envía cada entrada aplicable. Las variables que establecen las entradas no se guardan.
!!!

<a id="data-of-other-extensions"></a>

### Datos de otras extensiones

El botón del enchufe abre una ventana emergente con las **fuentes adicionales** que ofrecen otras extensiones; por ejemplo, el [Arc Outline](arc-outline.md) ofrece tu esquema. Cada fuente tiene una casilla; la lista tiene hasta dos partes:

- **Sobre el libro**: una fuente marcada se añade a la petición después del lorebook, bajo su nombre.
- **Sobre las partes de la historia**: una fuente marcada se consulta para cada parte de la historia del rango *Chat Logs* y lo que devuelve se añade después del texto de esa parte. Solo tiene efecto mientras *Chat Logs* esté activado.

La selección se guarda con la pestaña. Si una extensión no está instalada (o está detenida), sus fuentes no aparecen en la lista ni se envían, y vuelven cuando la extensión lo hace. Si no ofrece ninguna, la ventana emergente lo indica.

La petición se construye así:

1. un mensaje de sistema: el *Initial System Query* del perfil, las entradas del lorebook (*Lorebook Context*), las fuentes marcadas sobre el libro y las partes de la historia (*Chat History Context*, etiquetadas con sus números del esquema *Message #1*, *#2*..., cada una seguida de las fuentes marcadas sobre partes de la historia);
2. los mensajes de la conversación que están incluidos, como un chat. Las *Instructions Before User Input* del perfil se colocan al principio de tu última pregunta, antes de su texto.

El modelo y el protocolo vienen del perfil (ver abajo); por defecto, los del libro. Para preguntas sobre la historia, suele bastar con un modelo barato y rápido.

<a id="saved-queries"></a>

## Consultas guardadas

Las preguntas que haces a menudo se pueden guardar:

- **Save prompt** (icono de disquete) guarda el texto del cuadro con el nombre seleccionado en la lista, o pide un nombre;
- **Save prompt as...** lo guarda con un nombre nuevo;
- elige un nombre de la lista para poner el texto guardado en el cuadro;
- el botón de la papelera elimina la consulta guardada seleccionada.

Las consultas guardadas pertenecen a tu cuenta y están disponibles en todos los libros.

Algunas útiles:

- *Enumera todos los personajes con nombre que han aparecido hasta ahora, con una línea sobre cada uno.*
- *¿Qué hilos de la trama siguen abiertos?*
- *Sugiere cinco nombres para un pueblo portuario en este mundo.*

<a id="profiles"></a>

## Perfiles

Los ajustes de Side Query están en **Ajustes → Extension Settings → SideQuery Settings**, organizados en **perfiles** como los del [Reviewer](reviewer.md#profiles). Se usa el perfil seleccionado:

![Ajustes de SideQuery](../../images/plugin-sidequery-settings.png)

| Campo | |
|---|---|
| **Profile**, **New**, **Renombrar**, **Eliminar** | Elige y gestiona los perfiles. *_Default* no se puede renombrar ni eliminar. |
| **SideQuery Model**, **SideQuery Protocol** | El [proveedor de inferencia](../inference-providers.md) y el [protocolo](../protocols.md) a usar. Vacío: el del libro. |
| **Initial System Query** | El inicio del mensaje de sistema, p. ej. el rol del asistente. |
| **Instructions Before User Input** | Texto que se coloca justo antes de tu última pregunta: reglas para las respuestas, p. ej. *Responde brevemente. Cita la historia cuando puedas.* |
| **Enable AI Tab Naming** | Tras la primera respuesta de una pestaña, se pide al modelo un nombre corto para la conversación (una petición adicional). Las pestañas que hayas renombrado conservan su nombre. |

Pulsa **Guardar** en la parte superior de la página *Ajustes* para conservar los cambios.

<a id="storage"></a>

## Almacenamiento

Las conversaciones se guardan con el libro, así que forman parte de las [copias de seguridad del libro](../books/backups.md) y de las copias. Los perfiles y las consultas guardadas se guardan en los ajustes de tu usuario.
