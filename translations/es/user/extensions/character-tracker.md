---
label: Seguimiento de personajes
order: 19
---

# Seguimiento de personajes

El reparto de tu libro en la barra lateral: una lista de personajes con un lema, una nota, un icono y un color, las relaciones entre ellos y un mapa de esas relaciones.

<a id="the-characters-tab"></a>

## La pestaña Characters

La barra lateral izquierda del [editor de la historia](../books/story-editor.md) tiene una pestaña **Characters** junto a *Outline*. Encima de la lista hay una fila de botones: **Add** (un personaje nuevo), un menú con la importación de entradas del lorebook, **Relationship map** y un botón con forma de globo de diálogo para el prompt (pasa el cursor para ver los nombres).

El botón del globo de diálogo abre un menú emergente con **In prompt**, **Depth** y un rol, como en la [Nota del autor](authors-note.md). El botón se pone verde mientras los personajes están en el prompt. Cuando **In prompt** está marcado, cada generación recibe los personajes como un mensaje: una línea con el nombre, el lema entre corchetes y la nota de cada personaje, y una lista de las relaciones (`Mara -> Tomas: employs`, `<->` cuando va en ambos sentidos). **Depth** es el número de mensajes que hay después de él (`0` al final del todo, después de tus instrucciones; `1` justo antes de ellas), y el rol es *System*, *Assistant* o *User*. El texto toma sus tokens del espacio reservado para la historia. No se inserta nada mientras el libro no tenga personajes. La configuración se guarda con el libro.

El botón de la varita entre ellos tiene el elemento de menú **Import lore entries as characters**, véase [más abajo](#importing-characters-from-the-lorebook).

La lista está ordenada por nombre. Cada personaje tiene su propio color y muestra su **name** con el **tagline** debajo, en letras más pequeñas (dos líneas como máximo). Los botones de un personaje:

| Botón | |
|---|---|
| flecha hacia abajo | Expande el personaje para mostrar su **icon** y su **note**. |
| lápiz | Abre el personaje en el diálogo. |
| papelera | Elimina el personaje y todas sus relaciones, previa confirmación. |

Los personajes se guardan con el libro en cuanto los cambias, así que se incluyen en sus copias de seguridad.

![La pestaña Characters](../../images/plugin-charactertracker-tab.png)

<a id="picking-characters-for-a-turn"></a>

## Elegir personajes para un turno

El formulario de [nuevo turno](../books/story-editor.md) tiene los campos **POV character** y **Present characters**. Siguen siendo texto libre, pero cada uno tiene al final un botón con icono de persona que muestra la lista de personajes del libro, ordenada por nombre. Un nombre elegido en la lista de **POV character** sustituye lo que haya en el campo; un nombre elegido en la lista de **Present characters** se añade al final, después de una coma, a no ser que ya esté. La lista se lee al abrirla, así que un personaje que acabas de añadir aparece en ella. Aun así, puedes escribir cualquier nombre, y un nombre en el campo no tiene por qué ser un personaje del libro.

<a id="the-character-dialog"></a>

## El diálogo de personaje

**Add** (el botón con el signo más; su aviso dice *Add new character*) y el lápiz abren el mismo diálogo:

| Campo | |
|---|---|
| **Name** | Obligatorio. |
| **Tagline** | Unas palabras sobre el personaje, que se muestran en la lista y en el mapa. |
| **Note** | Lo que quieras recordar. |
| **Icon** | Sube un PNG, JPEG o GIF. Se reduce a 200 x 200 píxeles como máximo, manteniendo las proporciones. |
| **Color** | Fondo del personaje en la lista y en el mapa. |

Los campos se aplican con **Save**.

![Un personaje con sus relaciones](../../images/plugin-charactertracker-dialog.png)

Si pulsas **Cancel** (o Esc) con cambios sin guardar, incluida una relación que has rellenado pero aún no has añadido, se te pregunta si quieres descartarlos.

![La pregunta cuando se cancela un personaje con cambios sin guardar](../../images/plugin-charactertracker-unsaved.png)

<a id="relationships"></a>

### Relaciones

Al abrir un personaje existente, el diálogo también lista sus relaciones: las que el personaje señala (`→`), las que le señalan a él (`←`) y las unidireccionales (`↔`), cada una con un nombre y un lema. El botón de la papelera elimina una relación.

Para añadir una, busca al otro personaje, elige la **Direction** (*this character → other*, *other → this character* o *both ways*), escribe un **Name** (por ejemplo, *employs*, *friends*) y, opcionalmente, un **Tagline**, y pulsa **Add relationship** (el botón está encima de estos campos). Las relaciones se guardan al instante. Un personaje nuevo puede recibir relaciones después de haberse guardado.

<a id="importing-characters-from-the-lorebook"></a>

## Importar personajes desde el lorebook

Si tus personajes ya están en el lorebook, el menú de la varita los crea por ti. **Import lore entries as characters** abre un diálogo con las entradas del lorebook del libro y de todos sus sublorebooks, ordenadas por su *order*. Cada una muestra su nombre y una casilla, y un icono **i** que muestra el contenido de la entrada en un menú emergente, para echarle un vistazo antes de decidir. La casilla de la cabecera de la lista marca o desmarca todas. **Import** crea un personaje con el nombre de cada entrada marcada (el lema, la nota, el icono y el color quedan vacíos para que los rellenes), y **Close** sale sin importar nada.

Las entradas no se comparan con los personajes que ya tienes: si importas la misma entrada dos veces, se crean dos personajes.

![Importar personajes desde el lorebook](../../images/plugin-charactertracker-import.png)

<a id="the-relationship-map"></a>

## El mapa de relaciones

**Relationship map** muestra todas las relaciones del libro como un grafo. Cada personaje que tiene alguna relación es un círculo con su icono (o una caja, si no tiene uno) sobre su color, con su nombre y su lema; cada relación es una flecha entre dos personajes, con su nombre y su lema. Una flecha apunta al destino; una relación unidireccional tiene una flecha en ambos extremos. Los personajes sin relaciones no aparecen en el mapa.

![El mapa de relaciones](../../images/plugin-charactertracker-map.png)

Los personajes se colocan en anillos alrededor de uno: al principio, el que tiene más relaciones. Los personajes con los que está relacionado ocupan el primer anillo, los de ellos el segundo, y así sucesivamente. **Haz doble clic en un personaje** para colocar el mapa alrededor de él. También puedes arrastrar los personajes, mover el mapa y hacer zoom con la rueda del ratón.

<a id="commands"></a>

## Comandos

Los personajes también son [comandos](../commands/index.md), para la línea de comandos y para los [agentes](../agents.md). Trabajan sobre el libro abierto (un agente: sobre su copia de trabajo), y una pestaña Characters abierta muestra el cambio al instante. Un personaje se indica por su **name** (no importan las mayúsculas), o por su **number** en `/list-characters` (el orden de los nombres) cuando dos personajes tienen el mismo nombre. Una relación se indica por su número en `/list-character-relations`. Un comando que cambia un personaje solo toma los campos que le des; un texto vacío borra un lema o una nota. El icono solo se puede establecer en el diálogo.

| Comando | Qué hace |
|---|---|
| `/list-characters` | Los personajes ordenados por nombre: lema, color, si tienen nota y número de relaciones. Solo lectura. |
| `/get-character character` | Un personaje con su nota y sus relaciones. Solo lectura. |
| `/create-character name [tagline [note [color]]]` | Añade un personaje. `color` es `#rrggbb`. |
| `/update-character character [name [tagline [note [color]]]]` | Cambia un personaje. |
| `/delete-character character` | Elimina un personaje con sus relaciones, previa confirmación. |
| `/list-character-relations [character]` | Las relaciones, o las de un personaje. Solo lectura. |
| `/add-character-relation source target name [tagline [bidirectional]]` | Añade una relación de `source` a `target` (*employs*), o con `bidirectional` `true` una en la que ambos son iguales (*friends*). |
| `/update-character-relation relationship [name [tagline]]` | Cambia el nombre o el lema de una relación. |
| `/delete-character-relation relationship` | Elimina una relación, previa confirmación. |
| `/character-tracker-map` | Los personajes y sus relaciones como el texto que recibe el prompt. Solo lectura. |

`character-tracker-map` está pensado para el [macro {%{`{{command::character-tracker-map}}`}%}](../templates/macros.md#commands): colócalo en la plantilla maestra, en el prompt del usuario o en una entrada del lorebook para situar los personajes donde quieras dentro del prompt, en lugar de (o además de) la inserción automática de la pestaña Characters. Siempre muestra el estado actual de los personajes, tanto si la inserción está activada como si no.

Añadir y cambiar no es destructivo; solo lo son los comandos `delete`, sobre los que un agente puede estar configurado para pedirte confirmación.

El [asistente de trama](arc-outline.md) del Arc Outline usa los mismos comandos cuando marcas sus herramientas **Characters**, así que puedes desarrollar el reparto mientras hablas de la trama.

<a id="phone"></a>

## Teléfono

La pestaña está en el cajón de la historia, junto al esquema, como la [Nota del autor](authors-note.md). Los botones de los personajes POV y presentes también están en el formulario de nuevo turno del teléfono.
