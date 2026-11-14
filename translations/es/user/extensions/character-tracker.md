---
label: Gestor de personajes
order: 19
---

# Gestor de personajes

El reparto de tu libro en la barra lateral: una lista de personajes con un eslogan, una nota, un icono y un color, las relaciones entre ellos y un mapa de esas relaciones.

<a id="the-characters-tab"></a>

## La pestaña Personajes

La barra lateral izquierda del [editor de historias](../books/story-editor.md) tiene una pestaña **Personajes** junto a *Esquema*. Encima de la lista hay una fila de botones: **Añadir** (un personaje nuevo), un menú con la importación de entradas del lorebook, el **Mapa de relaciones** y un botón de globo de diálogo para el prompt (pasa el ratón para ver los nombres).

El botón del globo de diálogo abre un menú emergente con **En el prompt**, **Profundidad** y un rol, como en la [Nota del autor](authors-note.md); el botón se pone verde mientras los personajes están en el prompt. Cuando *En el prompt* está marcado, cada generación recibe los personajes como un mensaje: una línea con el nombre, el eslogan entre corchetes y la nota de cada personaje, y una lista de las relaciones (`Mara -> Tomas: employs`, `<->` cuando va en ambos sentidos). *Profundidad* es el número de mensajes posteriores a él (`0` el final absoluto, después de tus instrucciones; `1` justo antes de ellas), y el rol es *Sistema*, *Asistente* o *Usuario*. El texto toma sus tokens del espacio reservado para la historia. No se inserta nada mientras el libro no tenga personajes. La configuración se guarda con el libro.

El botón de la varita entre ambos tiene el elemento de menú **Importar entradas del lorebook como personajes**, véase [más abajo](#importing-characters-from-the-lorebook).

La lista está ordenada por nombre. Cada personaje tiene su propio color y muestra su **nombre** con el **eslogan** debajo, en letras más pequeñas (dos líneas como máximo). Los botones de un personaje:

| Botón | |
|---|---|
| flecha | Expande el personaje para mostrar su **icono** y su **nota**. |
| lápiz | Abre el personaje en el diálogo. |
| papelera | Elimina el personaje y todas sus relaciones, previa confirmación. |

Los personajes se guardan con el libro en cuanto los cambias, así que se incluyen en sus copias de seguridad.

![La pestaña Personajes](../../images/plugin-charactertracker-tab.png)

<a id="picking-characters-for-a-turn"></a>

## Elegir personajes para un turno

El formulario de [turno nuevo](../books/story-editor.md) tiene los campos **Personaje POV** y **Personajes presentes**. Siguen siendo texto libre, pero cada uno tiene al final un botón con un icono de persona que lista los personajes del libro, ordenados por nombre. Un nombre elegido en la lista de **Personaje POV** reemplaza lo que haya en el campo; un nombre elegido en la lista de **Personajes presentes** se añade al final, tras una coma, salvo que ya esté. La lista se lee al abrirla, así que un personaje que acabas de añadir aparece en ella. Aun así puedes escribir cualquier nombre, y un nombre en el campo no tiene que ser un personaje del libro.

<a id="the-character-dialog"></a>

## El diálogo de personaje

**Añadir** (el botón con el signo más; su aviso dice *Añadir nuevo personaje*) y el lápiz abren el mismo diálogo:

| Campo | |
|---|---|
| **Nombre** | Obligatorio. |
| **Eslogan** | Unas pocas palabras sobre el personaje, que se muestran en la lista y en el mapa. |
| **Nota** | Lo que quieras recordar. |
| **Icono** | Sube un PNG, JPEG o GIF. Se reduce a 200 x 200 píxeles como máximo, manteniendo las proporciones. |
| **Color** | Fondo del personaje en la lista y en el mapa. |

Los campos se aplican con **Guardar**.

![Un personaje con sus relaciones](../../images/plugin-charactertracker-dialog.png)

Si pulsas **Cancelar** (o Esc) mientras hay cambios sin guardar, incluida una relación que rellenaste pero aún no añadiste, se te pregunta si quieres descartarlos.

![La pregunta al cancelar un personaje con cambios sin guardar](../../images/plugin-charactertracker-unsaved.png)

<a id="relationships"></a>

### Relaciones

Al abrir un personaje existente, el diálogo también lista sus relaciones: las que el personaje apunta (`→`), las que apuntan a él (`←`) y las unidireccionales (`↔`), cada una con un nombre y un eslogan. El botón de papelera elimina una relación.

Para añadir una, busca el otro personaje, elige la **Dirección** (*este personaje → otro*, *otro → este personaje* o *en ambos sentidos*), escribe un **Nombre** (por ejemplo *emplea*, *amigos*) y, opcionalmente, un **Eslogan**, y pulsa **Añadir relación** (el botón está encima de estos campos). Las relaciones se guardan al instante. Un personaje nuevo puede recibir relaciones después de haberse guardado.

<a id="importing-characters-from-the-lorebook"></a>

## Importar personajes desde el lorebook

Si tus personajes ya están en el lorebook, el menú de la varita los crea por ti. **Importar entradas del lorebook como personajes** abre un diálogo con las entradas del lorebook del libro y de todos sus sublorebooks, ordenadas por su *orden*. Cada una muestra su nombre y una casilla, y un icono **i** que muestra el contenido de la entrada en un menú emergente, para echarle un vistazo antes de decidir; la casilla de la cabecera de la lista marca o desmarca todas. **Importar** crea un personaje con el nombre de cada entrada marcada (el eslogan, la nota, el icono y el color quedan vacíos para que los completes), y **Cerrar** sale sin importar nada.

Las entradas no se comparan con los personajes que ya tienes: importar dos veces la misma entrada crea dos personajes.

![Importar personajes desde el lorebook](../../images/plugin-charactertracker-import.png)

<a id="the-relationship-map"></a>

## El mapa de relaciones

**Mapa de relaciones** muestra todas las relaciones del libro como un grafo. Cada personaje que tiene alguna relación es un círculo con su icono (o una caja si no tiene) sobre su color, con su nombre y su eslogan; cada relación es una flecha entre dos personajes con su nombre y su eslogan. Una flecha apunta al destino; una relación unidireccional tiene una flecha en ambos extremos. Los personajes sin relaciones no aparecen en el mapa.

![El mapa de relaciones](../../images/plugin-charactertracker-map.png)

Los personajes se disponen en anillos alrededor de uno: al principio, el que tiene más relaciones. Los personajes con los que se relaciona están en el primer anillo, los suyos en el segundo, y así sucesivamente. **Haz doble clic en un personaje** para disponer el mapa a su alrededor. También puedes arrastrar los personajes, mover el mapa y hacer zoom con la rueda del ratón.

<a id="commands"></a>

## Comandos

Los personajes también son [comandos](../commands/index.md), para la línea de comandos y para los [agentes](../agents.md). Actúan sobre el libro abierto (un agente: sobre su copia de trabajo), y una pestaña Personajes abierta muestra el cambio al instante. Un personaje se indica por su **nombre** (no distingue mayúsculas), o por su **número** en `/list-characters` (el orden de los nombres) cuando dos personajes tienen el mismo nombre. Una relación se indica por su número en `/list-character-relations`. Un comando que cambia un personaje solo toma los campos que le das; un texto vacío borra un eslogan o una nota. El icono solo se puede establecer en el diálogo.

| Comando | Qué hace |
|---|---|
| `/list-characters` | Los personajes ordenados por nombre: eslogan, color, si tienen nota y número de relaciones. Solo lectura. |
| `/get-character character` | Un personaje con su nota y sus relaciones. Solo lectura. |
| `/create-character name [tagline [note [color]]]` | Añade un personaje. `color` es `#rrggbb`. |
| `/update-character character [name [tagline [note [color]]]]` | Cambia un personaje. |
| `/delete-character character` | Elimina un personaje con sus relaciones, previa confirmación. |
| `/list-character-relations [character]` | Las relaciones, o las de un personaje. Solo lectura. |
| `/add-character-relation source target name [tagline [bidirectional]]` | Añade una relación de `source` a `target` (*emplea*), o con `bidirectional` `true` una en la que son iguales (*amigos*). |
| `/update-character-relation relationship [name [tagline]]` | Cambia el nombre o el eslogan de una relación. |
| `/delete-character-relation relationship` | Elimina una relación, previa confirmación. |
| `/character-tracker-map` | Los personajes y sus relaciones como el texto que recibe el prompt. Solo lectura. |

`character-tracker-map` está pensado para la [macro {%{`{{command::character-tracker-map}}`}%}](../templates/macros.md#commands): ponla en la plantilla maestra, en el prompt de usuario o en una entrada del lorebook para colocar los personajes donde quieras en el prompt, en lugar de (o además de) la inserción automática de la pestaña Personajes. Siempre muestra el estado actual de los personajes, esté o no activada la inserción.

Añadir y cambiar no es destructivo; solo los comandos `delete` lo son, y un agente puede configurarse para pedirte confirmación antes de usarlos.

<a id="phone"></a>

## Teléfono

La pestaña está en el cajón de la historia, junto al esquema, como la [Nota del autor](authors-note.md). Los botones de personaje POV y personajes presentes también están en el formulario de turno nuevo del teléfono.
