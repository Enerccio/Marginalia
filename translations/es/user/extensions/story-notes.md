---
label: Notas de la historia
order: 27
---

# Notas de la historia

Una pequeña wiki para tu libro, en la barra lateral: páginas para la trama que estás planeando, una idea que aún no estás listo para usar, las reglas de un sistema de magia, lo que el villano realmente quiere. Son tu propio pensamiento, con tus propias palabras, con etiquetas y enlaces entre las páginas. A diferencia de la [Nota del autor](authors-note.md), que es un único texto enviado con cada generación, las notas no se envían por sí solas al modelo. El [asistente de la trama](arc-outline.md#developing-the-plot-in-a-conversation) y los agentes pueden leerlas y, si lo permites, modificarlas.

<a id="the-notes-tab"></a>

## La pestaña Notas

La barra lateral izquierda del [editor de la historia](../books/story-editor.md) incluye una pestaña **Notes** junto a *Outline*. Lista las páginas del libro ordenadas por título. Encima de la lista están **Add** (una página nueva), un filtro por **tag** (visible una vez que una página tiene una etiqueta) y una búsqueda sobre los títulos, las etiquetas y el texto de las páginas.

Cada página muestra su título, la primera línea de su texto y sus etiquetas. Haz clic en la página, o en el lápiz, para abrirla; el cubo de basura la elimina tras una confirmación. Dos pequeñas marcas pueden acompañar al título: un ojo tachado (la página está [oculta para los asistentes](#what-assistants-can-see)) y una varita mágica (el último cambio vino de un comando, no de ti).

![La pestaña Notas: una página oculta para los asistentes (el ojo) y otra a la que añadió contenido el asistente de la trama (la varita)](../../images/plugin-storynotes-tab.png)

<a id="a-page"></a>

## Una página

El diálogo de una página tiene:

| | |
|---|---|
| **Title** | Obligatorio, único en el libro (no distingue mayúsculas), sin corchetes ni barras verticales (`|`). Es lo que enlazan las demás páginas. |
| **Tags** | Separadas por comas. Filtran la lista. |
| **Text** | Markdown. **Write** y **Preview** alternan entre el texto y su aspecto final. `[[Title]]` en el texto enlaza con la página de ese título; `[[Title|other words]]` también enlaza, pero muestra *other words* en la vista previa (`[[Language barrier|how they talk]]`). |
| **Visible to assistants** | Activado por defecto. Véase más abajo. |

Debajo del texto, el diálogo muestra las páginas a las que el texto **enlaza**: un enlace a una página existente la abre, y un enlace a una que no existe ofrece **Create** y crea una página nueva con ese título. Debajo aparecen las páginas que **enlazan aquí**. Renombrar una página cambia los `[[enlaces]]` hacia ella en las demás páginas (las palabras que muestra un enlace se conservan).

![Una página con las páginas a las que enlaza y la página que enlaza hacia ella](../../images/plugin-storynotes-page.png)

**Preview** muestra el texto como Markdown, con cada `[[enlace]]` como un vínculo: haz clic para abrir esa página (un enlace a una página que no existe crea una nueva con ese título):

![La vista previa de la misma página](../../images/plugin-storynotes-preview.png)

Nada se guarda antes de **Save**; al cerrar una página modificada, se pregunta antes de descartar los cambios. Si un comando cambió la página mientras la editabas, se te pregunta antes de que tu versión la sobrescriba. **Ctrl/Cmd+Enter** guarda. Las páginas se almacenan junto con el libro, de modo que están incluidas en sus copias de seguridad y copias.

<a id="what-assistants-can-see"></a>

## Qué pueden ver los asistentes

Los «asistentes» son los comandos que traen las notas, y que usan los agentes, las macros y el asistente de la trama. Una página con **Visible to assistants** desactivado no existe para ellos: no aparece en listados, no se encuentra, no se lee ni se modifica, y un título que solo ella tiene se rechaza cuando intentan crear una. Desactívalo para lo que solo debas ver tú. Las páginas que hayas desactivado siguen en tus copias de seguridad.

Los comandos (para la barra de comandos y para los agentes; el título es la dirección de una página):

| Comando | |
|---|---|
| `/list-notes [<tag>]`, `/get-note <note>`, `/search-notes <query>` | Las páginas, una página con su texto, sus enlaces y las páginas que enlazan hacia ella, y una búsqueda. |
| `/create-note <title> [<content> [<tags>]]` | Añade una página. |
| `/append-to-note <note> <text>` | Añade un párrafo al final de una página. |
| `/update-note <note> [<title> [<content> [<tags>]]]`, `/edit-note <note> <find> <replace> [<all>]` | Cambia una página, o reemplaza un fragmento de su texto: `find` debe aparecer en la página exactamente como está escrito (importan las mayúsculas) y, a menos que `all` sea `true`, exactamente una vez; con `all` se reemplaza cada aparición, para un nombre o un término en toda la página. La respuesta indica cuántos fragmentos se reemplazaron. Estos comandos cambian texto existente, así que son *destructivos*: un agente configurado para preguntar te consulta antes. |
| `/delete-note <note>` | Elimina una página, tras una confirmación. |
| `/note-text <note>` | El texto de una página, pensado para las macros: {%{`{{command::note-text::Magic}}`}%} lo introduce en una plantilla. Vacío para una página que no existe u oculta. |

Una página que un comando ha cambiado se marca con la varita hasta que la guardes tú mismo.

<a id="with-the-plot-wizard"></a>

## Con el asistente de la trama

Cuando esta extensión está instalada, el [asistente de la trama](arc-outline.md#developing-the-plot-in-a-conversation) del Arc Outline tiene una opción **Send the notes** (los títulos con sus primeras líneas, el texto completo, o nada) y un grupo de herramientas **Notes**: el modelo puede buscar y leer las páginas, seguir sus enlaces, añadir una página o añadir a una cuando le pidas que guarde algo, y cambiar texto en una. Cambiar el texto de una página se consulta antes mientras *Ask me before it changes or deletes something* esté activado; el asistente no puede eliminar páginas. Véase la opción y el grupo en la tabla del asistente.

![El asistente de la trama con las herramientas de Notas: leyó una página y añadió la pregunta abierta a ella](../../images/plugin-storynotes-wizard.png)

<a id="other-extensions"></a>

## Otras extensiones

La extensión ofrece dos fuentes de texto a otras extensiones (la [Side Query](side-query.md) puede adjuntarlas desde su botón de enchufe): **Story notes (titles)** y **Story notes (all)**. Ambas excluyen las páginas ocultas.
