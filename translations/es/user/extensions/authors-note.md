---
label: Nota del autor
order: 23
---

# Nota del autor

Una nota para el modelo que se envía con cada generación del libro: el tono que quieres en este momento, de qué trata el capítulo actual, un recordatorio que el modelo sigue olvidando. A diferencia de las instrucciones de un turno, la nota permanece hasta que la cambies.

<a id="the-authors-note-tab"></a>

## La pestaña Nota del autor

La barra lateral izquierda del [editor de la historia](../books/story-editor.md) incluye una pestaña **Author's Note** junto a *Outline*.
Los cambios se guardan en el libro a medida que escribes y los usa la siguiente generación.

![La pestaña Nota del autor](../../images/plugin-authorsnote.png)

| Campo | |
|---|---|
| **Insert into prompt** | Activa o desactiva la nota sin borrarla. |
| **Insertion depth** | Dónde va la nota, contado en mensajes desde el final del prompt: `0` - después de las instrucciones del turno (el último mensaje), `1` - justo antes de ellas, `2` - antes de la última parte de la historia, y así sucesivamente. Una profundidad grande coloca la nota justo después del prompt del sistema. |
| **Role** | Si la nota se envía como mensaje *System*, *User* o *Assistant*. |
| **Author's Note** | El texto que se envía al modelo. Una nota vacía no se envía. Debajo, una estimación de su tamaño en tokens. |
| **Author's Note (private)** | Notas para ti - planes, recordatorios. Nunca se envían al modelo. |

!!!
Cuanto más cerca esté la nota del final del prompt, más fuerte será su efecto. Profundidad `1` con rol *System* es un buen punto de partida.
La nota no se procesa como una [plantilla](../templates/macros.md): las macros se envían tal como están escritas.
!!!

<a id="how-the-prompt-looks"></a>

## Cómo es el prompt

Una generación envía primero el prompt del sistema, después la historia como un chat (cada parte como una respuesta del modelo a *Generate story*) y, al final, las instrucciones del turno. Con profundidad `1`, la nota se inserta justo antes de las instrucciones:

1. prompt del sistema (plantilla, lore, resúmenes)
2. partes de la historia…
3. **la Nota del autor**
4. las instrucciones del turno

Los tokens de la nota se toman del espacio disponible para la historia: una nota más larga significa menos partes de la historia en el prompt. Si una nota no deja espacio ni para el resto del prompt, la generación se detiene con *Contextual limit not sufficient*.

<a id="the-note-as-a-macro"></a>

## La nota como macro

En lugar de (o además de) la inserción automática, puedes colocar la nota tú mismo con la [macro {%{`{{command::authors-note}}`}%}](../templates/macros.md#commands): ponla en la plantilla maestra, en el prompt de usuario o en una entrada del lorebook, y mostrará el texto de la nota donde quieras dentro del prompt. Muestra la nota tal como está guardada en este momento, tanto si **Insert into prompt** está activado como si no, así que desactívalo cuando coloques la nota mediante macro, o el modelo la recibirá dos veces. La profundidad y el rol no se aplican a la macro (la nota forma parte del mensaje que contiene la macro), y una nota vacía no muestra nada.

<a id="storage"></a>

## Almacenamiento

La nota se guarda con el libro, así que forma parte de las [copias de seguridad del libro](../books/backups.md) y de las copias.
