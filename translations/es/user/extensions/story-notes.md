---
label: Notas de la historia
order: 27
---

# Notas de la historia

Una pequeña wiki para tu libro, en la barra lateral: páginas para la trama que estás planeando, una idea que aún no estás listo para usar, las reglas de un sistema de magia, lo que el villano realmente quiere. Son tu propio pensamiento, con tus propias palabras, con etiquetas y enlaces entre las páginas. A diferencia de la [Nota del autor](authors-note.md), que es un único texto enviado con cada generación, las notas no se envían al modelo por sí solas. El [asistente de trama](arc-outline.md#developing-the-plot-in-a-conversation) y los agentes pueden leerlas y, si lo permites, modificarlas.

<a id="the-notes-tab"></a>

## La pestaña Notas

La barra lateral izquierda del [editor de la historia](../books/story-editor.md) incluye una pestaña **Notas** junto a *Esquema*. Muestra las páginas del libro ordenadas por título. Encima de la lista están **Añadir** (una página nueva), un filtro por **etiqueta** (que aparece cuando una página tiene alguna etiqueta) y una búsqueda sobre los títulos, las etiquetas y el texto de las páginas.

Cada página muestra su título, la primera línea de su texto y sus etiquetas. Haz clic en la página, o en el lápiz, para abrirla; el cubo de la basura la elimina tras una confirmación. Dos pequeñas marcas pueden acompañar al título: un ojo tachado (la página está [oculta para los asistentes](#what-assistants-can-see)) y una varita (el último cambio vino de un comando, no de ti).

![La pestaña Notas: una página oculta para los asistentes (el ojo) y otra a la que añadió contenido el asistente de trama (la varita)](../../images/plugin-storynotes-tab.png)

<a id="a-page"></a>

## Una página

El diálogo de una página tiene:

| | |
|---|---|
| **Título** | Obligatorio, único en el libro (no distingue mayúsculas), sin corchetes ni barras verticales (`|`). Es lo que enlazan las demás páginas. |
| **Etiquetas** | Separadas por comas. Filtran la lista. |
| **Texto** | Markdown. **Escribir** y **Vista previa** alternan entre el texto y cómo se verá. `[[Title]]` en el texto enlaza con la página que tiene ese título; `[[Title|other words]]` también enlaza con ella, pero muestra *otras palabras* en la vista previa (`[[Language barrier|how they talk]]`). |
| **Visible para los asistentes** | Activado por defecto. Véase más abajo. |

Bajo el texto, el diálogo lista las páginas a las que el texto **enlaza**: un enlace a una página que existe la abre, y un enlace a una que no existe ofrece **Crear** e inicia una página nueva con ese título. Debajo aparecen las páginas que **enlazan aquí**. Al renombrar una página, el `[[links]]` a ella cambia en las demás páginas (las palabras que muestra el enlace se mantienen).

![Una página con las páginas a las que enlaza y la página que enlaza a ella](../../images/plugin-storynotes-page.png)

**Vista previa** muestra el texto como Markdown, con cada `[[link]]` convertido en un enlace: haz clic para abrir esa página (un enlace a una página que no existe inicia una nueva con ese título):

![La vista previa de la misma página](../../images/plugin-storynotes-preview.png)

Nada se guarda antes de **Guardar**; al cerrar una página modificada, la aplicación pregunta antes de descartar los cambios. Si un comando cambió la página mientras la editabas, se te pregunta antes de que tu versión la sobrescriba. **Ctrl/Cmd+Enter** guarda. Las páginas se almacenan con el libro, así que están en sus copias de seguridad y copias.

<a id="what-assistants-can-see"></a>

## Qué pueden ver los asistentes

«Asistentes» son los comandos que traen las notas, y que usan los agentes, las macros y el asistente de trama. Una página con **Visible para los asistentes** desactivado no existe para ellos: no se lista, no se encuentra, no se lee ni se modifica, y un título que solo ella tiene se rechaza cuando intentan crearlo. Desactívalo para aquello que solo tú debes ver. Las páginas que has desactivado siguen en tus copias de seguridad.

Los comandos (para la barra de comandos y para los agentes; el título es la dirección de una página):

| Comando | |
|---|---|
| `/list-notes [<tag>]`, `/get-note <note>`, `/search-notes <query>` | Las páginas, una página con su texto, sus enlaces y las páginas que enlazan a ella, y una búsqueda. |
| `/create-note <title> [<content> [<tags>]]` | Añade una página. |
| `/append-to-note <note> <text>` | Añade un párrafo al final de una página. |
| `/update-note <note> [<title> [<content> [<tags>]]]`, `/edit-note <note> <find> <replace> [<all>]` | Modifican una página, o reemplazan un fragmento de su texto: `find` debe aparecer en la página exactamente como está escrito (importan las mayúsculas) y, a menos que `all` sea `true`, exactamente una vez; con `all` se reemplaza cada aparición, para un nombre o un término en toda la página. La respuesta indica cuántos fragmentos se reemplazaron. Como cambian texto existente, son *destructivos*: un agente configurado para preguntar te consulta primero. |
| `/delete-note <note>` | Elimina una página, tras una confirmación. |
| `/note-text <note>` | El texto de una página, pensado para las macros: {%{`{{command::note-text::Magic}}`}%} lo inserta en una plantilla. Vacío para una página que no existe o está oculta. |

Una página que un comando ha modificado se marca con la varita hasta que la guardes tú mismo.

<a id="with-the-plot-wizard"></a>

## Con el asistente de trama

Cuando esta extensión está instalada, el [asistente de trama](arc-outline.md#developing-the-plot-in-a-conversation) del Esquema del arco tiene una opción **Enviar las notas** (los títulos con sus primeras líneas, el texto completo, o nada) y un grupo de herramientas **Notas**: el modelo puede buscar y leer las páginas, seguir sus enlaces, añadir una página o añadir a una cuando le pides que guarde algo, y modificar el texto de una. Modificar el texto de una página se consulta antes mientras *Pedirme confirmación antes de modificar o eliminar algo* está activado; el asistente no puede eliminar páginas. Consulta la opción y el grupo en la tabla del asistente.

![El asistente de trama con las herramientas de Notas: leyó una página y le añadió la pregunta abierta](../../images/plugin-storynotes-wizard.png)

<a id="other-extensions"></a>

## Otras extensiones

La extensión ofrece dos fuentes de texto a otras extensiones ([Consulta lateral](side-query.md) puede adjuntarlas desde su botón de enchufe): **Notas de la historia (títulos)** y **Notas de la historia (todas)**. Ambas excluyen las páginas ocultas.
