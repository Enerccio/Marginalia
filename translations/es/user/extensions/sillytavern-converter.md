---
label: Convertidor
order: 16
---

# Convertidor

!!!
Esta extensión se llamaba *SillyTavern Converter* antes de la 1.6.0. Solo cambió el nombre; sus archivos, los datos guardados con tus libros y las disposiciones guardadas son los mismos.
!!!

Reescribe un chat de rol de [SillyTavern](https://github.com/SillyTavern/SillyTavern) como una historia, turno a turno: carga el
chat (o una conversación de [LM Studio](https://lmstudio.ai)) en un libro, deja que el modelo lo divida en *detalles de turno* usando el chat y el lore del libro, y pasa cada turno
al nuevo formulario de turno de la historia con un solo clic.

La fuente no tiene por qué ser un chat: el convertidor también puede tomar las partes de otro libro tuyo (de un archivo de copia de seguridad o
del propio libro) y darte sus instrucciones una por una, para [reescribir un libro](#redrafting-a-book).

Para hacer un libro directamente a partir de un chat (cada mensaje, o cada par de usuario y asistente, una parte), consulta
[Importar desde SillyTavern](../books/managing-books.md#importing-from-sillytavern). El convertidor es para el otro sentido: tienes el
chat al lado y escribes la historia de nuevo, con el modelo, a partir de instrucciones hechas del chat.

!!!
El Convertidor también funciona en la [disposición para teléfono](../phone.md#extensions), consulta [En el teléfono](#on-a-phone).
!!!

<a id="the-converter-tab"></a>

## La pestaña Convertidor

La barra lateral izquierda del [editor de la historia](../books/story-editor.md) tiene una pestaña **Convertidor** (texto pequeño, y la pestaña se desplaza
cuando la barra lateral es baja). Encima de sus dos listas está la sección **Convertir a detalles de turno**, cerrada hasta que la abres para que
las listas tengan espacio. Contiene:

| | |
|---|---|
| **Modelo** y **Protocolo** | Con lo que funciona la varita. Empiezan con el modelo y el protocolo del libro; la elección se guarda con el libro. |
| **Desde**, **Hasta** | El rango de mensajes sobre el que trabaja la varita. Escribe los números, haz clic en el chat o usa **Todo**. |
| Recuento de tokens | Cuántos tokens ocupa la petición: los mensajes del rango más el prompt y el lore (contados una vez). Úsalo para que el rango quepa en el contexto del modelo. |
| **Convertir rango** | La varita: pide al modelo los detalles de turno del rango. Pasa a ser **Detener** mientras el modelo trabaja. |
| Botón del lápiz | Edita el prompt de la varita (variables `{{backgroundLore}}`, `{{chat}}`, `{{firstMessage}}`, `{{lastMessage}}`, `{{previousTurn}}`), con un botón **Predeterminado**. |
| **Alinear a turnos** | Mueve el rango a los límites de los turnos, ver más abajo. Se guarda con el libro. |
| **Modo automático** | Muestra los controles que convierten todo el rango por lotes, ver más abajo. |

<a id="align-to-turns"></a>

### Alinear a turnos

Un turno de la historia son los mensajes del usuario (las instrucciones) seguidos del mensaje del personaje. Con **Alinear a turnos** marcado,
cada rango que elijas (haciendo clic, escribiendo los números o con **Todo**) se mueve a los límites de los turnos:
el inicio retrocede al primer mensaje de su turno, y el final retrocede al último mensaje del personaje anterior a él,
de modo que el rango nunca termina en un mensaje del usuario que pertenece al turno siguiente. El final del chat se queda donde está. Un
rango que cae por completo en los mensajes del usuario antes de una respuesta se extiende hacia delante hasta esa respuesta.

<a id="auto-mode"></a>

### Modo automático

**Modo automático** muestra una línea con **Mensajes por lote**, el botón **Convertir por lotes** y cuántas peticiones son, con
los tokens que suman en total. El botón divide el rango actual en lotes de ese número de mensajes y los envía
al modelo uno tras otro (tras preguntar), igual que hace la varita para un solo rango. Con **Alinear a turnos** cada lote termina al final de un turno (el último mensaje del personaje dentro del tamaño, o el final del turno cuando es más largo
que el tamaño), así que ningún turno se corta en dos; sin él, los lotes se cortan exactamente por el número, y un turno en el límite
puede partirse en dos.

Mientras se ejecuta, el chat muestra el lote en el que va, la línea de estado indica qué lote de cuántos es, y el botón pasa a ser
**Detener**. Los detalles de turno de cada lote se guardan en cuanto está listo, como con la varita (los turnos que tocan el rango
y que no has marcado se reemplazan). Cuando terminan todos los lotes, el rango queda como lo tenías y se abre la pestaña **Detalles de turno**. Un lote cuya respuesta no se puede usar (vacía, o que no coincide con lo pedido) se vuelve a enviar, hasta tres veces en total. Cuando detienes, o un lote sigue fallando, la notificación dice por qué y el rango es lo que queda, para que puedas continuar desde ahí.

<a id="what-a-range-continues-from"></a>

### Desde dónde continúa un rango

El prompt le dice al modelo los detalles del último turno que termina antes del rango (su escenario, el personaje de punto de vista, los personajes presentes
y las instrucciones, en `{{previousTurn}}`), para que los turnos del rango mantengan el mismo punto de vista y escenario hasta que el chat
los cambie. Así es como un lote del modo automático continúa del anterior: los detalles de turno
de un lote se guardan antes de enviar el siguiente. Cuando no hay turno antes del rango, esa parte del prompt se omite.

<a id="the-chat"></a>

## El chat

Sube el archivo `.jsonl` de un chat (en SillyTavern está en `data/<user>/chats/<character>/`, o expórtalo desde el
chat), o una conversación de LM Studio (`<number>.conversation.json` en la carpeta `.lmstudio/conversations` de tu carpeta personal). Se distinguen
por lo que contiene el archivo. El chat no debe tener ramas: de cada mensaje se lee el swipe que se mostraba (en LM Studio, la versión
que se mostraba), y se omiten los mensajes de sistema ocultos y los vacíos; el prompt del sistema de una conversación de LM Studio, su razonamiento
y las llamadas a herramientas no son mensajes del chat. Si tu chat tiene ramas, importa primero la que quieres reescribir (consulta
[Importar desde SillyTavern](../books/managing-books.md#importing-from-sillytavern) para unir ramas en un árbol).

La pestaña **Chat** es una tabla de mensajes: una casilla, el número y el mensaje, con el nombre de quien habló en negrita encima del
texto (los mensajes del usuario se marcan con `»`).

- Marca la casilla de un mensaje por el que ya has pasado, para ver qué queda. El botón con la doble flecha
  encima de la tabla se desplaza al siguiente mensaje sin marcar (después del que marcaste o pulsaste por última vez, volviendo al
  principio al llegar al final).
- **Haz clic** en un mensaje para convertirlo en el rango; **Mayús+clic** en otro para extender el rango hasta él. El rango se
  resalta y el recuento de tokens lo sigue.
- El botón con la cruz encima de la tabla quita el chat y los detalles de turno del libro (tras preguntar).

![La pestaña Convertidor con un chat y el rango elegido](../../images/plugin-stconverter-chat.png)

<a id="turn-details"></a>

## Detalles de turno

Usa la varita y se abre la pestaña **Detalles de turno** con lo que preparó el modelo, en el orden del chat. Una fila es un turno:
los mensajes de los que se compone y, en un solo elemento, el escenario, el personaje de punto de vista, los personajes que están presentes y
las instrucciones, cada uno bajo una línea en negrita con su nombre.
Al modelo se le indica que cubra cada mensaje del rango exactamente una vez; los números fuera del rango se recortan a él.

- **Haz clic en una fila** para abrir el formulario de *nuevo turno* de la historia y copiar en él los cuatro campos del turno. Cambia
  lo que quieras y genera. (La casilla y el botón de borrar de una fila no abren el formulario).
- Marca la casilla de un turno que hayas usado; la doble flecha va al siguiente turno sin marcar.
- El botón de combinar de un turno **lo combina con el siguiente**: se abre un formulario con los detalles de ambos ya unidos (los
  escenarios y las instrucciones uno debajo de otro, los personajes presentes de los dos, el personaje de punto de vista del primero; cuando el
  segundo turno tiene otro, el formulario lo indica). Cambia lo que no encaje y pulsa **Combinar**
  (o <kbd>Ctrl/Cmd+Enter</kbd>): las dos filas se convierten en una que cubre los mensajes de ambas, marcada solo si lo estaban las dos.
- Las flechas de un turno lo **mueven arriba o abajo** en la lista, para escribir los turnos en otro orden del que tiene el chat
  (un turno conserva los mensajes que cubre). Los turnos hechos a partir de las partes de un libro no se pueden mover; su orden es el de las partes.
- Volver a usar la varita para un rango **reemplaza** los turnos anteriores que tocan el rango, excepto los marcados; los nuevos turnos
  pasan a su lugar en el orden del chat y se mantiene el orden que hayas dado moviendo turnos.

![Los detalles de turno que preparó el modelo](../../images/plugin-stconverter-turns.png)
<a id="redrafting-a-book"></a>

## Rehacer un libro

Para volver a escribir un libro parte por parte, carga el antiguo como origen. La pestaña muestra **Upload a chat**, **Load a backup** y **Pick a book** mientras no tenga origen:

- **Pick a book** abre una lista de tus libros (sin incluir el que tienes abierto). Se cargan las partes de la rama en la que está el libro, desde la primera parte hasta la que el libro tiene ahora.
- **Load a backup** toma un archivo de copia de seguridad de un libro: el JSON, el `.zip` con las imágenes, o un archivo que descargaste de las [copias de seguridad](../books/backups.md) de un libro. La copia solo se lee, no se restaura nada. La rama es la que tenía el libro cuando se hizo la copia.

De cada parte el convertidor conserva las instrucciones, el escenario, el personaje POV, los personajes presentes y el texto de la parte, y nada más (ni resúmenes, ni lorebook, ni razonamiento, ni imágenes, ni otras ramas, ni ajustes), así un libro largo ocupa poco. Un libro que no tiene partes no carga nada, y una copia cuyas partes no llevan de vuelta a la primera parte se rechaza como dañada.

La pestaña tiene entonces dos listas, igual que con un chat pero sin la varita mágica (el libro ya tiene sus instrucciones, por lo que la sección **Convert to turn details** no aparece):

- **Partes** es el texto de cada parte, con la misma casilla y la flecha hacia la siguiente parte sin marcar. Lee el texto antiguo junto a la historia que escribes.
- **Turn details** tiene una fila por parte con su escenario, personaje POV, personajes presentes e instrucciones. **Haz clic en una fila** para pasar esos datos al nuevo formulario de turno de la historia, igual que con un chat, y genera la nueva parte. Marca los turnos que ya hayas hecho. Dos turnos pueden **fusionarse** como con un chat (el botón de fusión del primero), para escribir como una sola dos partes del libro antiguo; los turnos fusionados se guardan con el libro.

Solo se guarda un origen por libro. Para cargar otro, elimina el actual con la cruz (tras confirmarlo); esto también olvida las marcas hechas sobre él.

<a id="on-a-phone"></a>

## En el teléfono

En el diseño para teléfono, la pestaña **Converter** está en el cajón de la historia, junto al esquema y las demás pestañas de extensiones (abre el cajón con el botón de la parte superior izquierda de la historia). Todo lo anterior funciona igual, con estas diferencias:

- No hay tecla Shift, así que el **rango se elige con dos toques**: toca un mensaje para empezar el rango y toca otro para terminarlo (el rango va de uno al otro, en cualquier orden); el siguiente toque empieza un rango nuevo. También puedes escribir **Desde** y **Hasta** o usar **Todos**.
- Los detalles de turno no tienen columna para los números de los mensajes: van en la primera línea de cada turno (**Msgs**).
- **Toca un turno** y el cajón se cierra y se abre el nuevo formulario de turno de la historia con los cuatro campos rellenos.
- La pestaña es más cómoda en modo horizontal; los diálogos (el aviso de la varita mágica, la elección de un libro) caben en la pantalla.

<a id="storage"></a>

## Almacenamiento

El archivo subido se guarda como un [recurso](../resources.md) tuyo (un chat de SillyTavern puede ocupar megabytes, con todo lo que SillyTavern añade a los mensajes), vinculado al libro. Las partes de un libro se guardan de la misma forma, como un pequeño archivo JSON (`<book name>.redraft.json`) con las instrucciones y el texto; el libro solo guarda las marcas. Las marcas, los detalles de turno, el modelo y el prompt se guardan en los atributos del libro, por lo que están en las [copias de seguridad de libros](../books/backups.md) y en las copias.

!!!warning
El archivo del chat no está en las copias de seguridad de libros, y una copia de un libro apunta al mismo archivo. Tras una restauración, las marcas y los detalles de turno siguen ahí, pero el convertidor pide el chat de nuevo (sube el mismo archivo; esto sustituye los detalles de turno y las marcas). Lo mismo ocurre con las partes de un libro: carga de nuevo el libro o la copia y vuelve a marcar. Eliminar el origen con la cruz borra el recurso.
!!!
