---
label: Lectores beta
order: 22
---

# Lectores beta

Lectores inventados, y una conversación con ellos sobre tu libro. Describes un **personaje** (un nombre, un icono y unas líneas sobre cómo lee la persona y qué le importa), y el modelo lo interpreta: reacciona a las partes de tu historia como lo haría ese lector, a solas o en un grupo que discute entre sí. Nada de lo que dicen los lectores forma parte del libro.

![Un chat de grupo con dos personajes](../../images/plugin-betareaders-chat.png)

<a id="the-beta-readers-tab"></a>

## La pestaña Lectores beta

La barra lateral izquierda del [editor de historias](../books/story-editor.md) tiene una pestaña **Lectores beta**, junto a *Esquema*. Tiene una pestaña para cada chat abierto del libro. El **+** inicia un chat, y el menú (⋮) junto a él tiene:

| Elemento | |
|---|---|
| Nuevo chat | Lo mismo que **+**. |
| Renombrar chat | Cambia el título del chat. |
| Cerrar chat | Cierra la pestaña; el chat se conserva y aparece en *Chats cerrados*. |
| Ajustes del chat | Cambia las [opciones del chat](#the-options-of-a-chat). |
| Eliminar chat | Elimina el chat y su historial, previa confirmación. |
| Personajes de este libro | Los personajes que pertenecen solo a este libro, véase [más abajo](#personas). |
| Chats cerrados | Vuelve a abrir un chat cerrado. |

Los chats se guardan con el libro, así que están incluidos en sus copias de seguridad.

![La pestaña, antes del primer chat](../../images/plugin-betareaders-tab.png)

![El menú de los chats](../../images/plugin-betareaders-menu.png)

<a id="personas"></a>

## Personajes

Un personaje tiene un **Nombre**, una **Descripción** (lo que interpreta el modelo: cómo lee el personaje, qué le gusta y qué le molesta) y un **icono** opcional, una imagen reducida a un tamaño pequeño. Sin icono, el personaje se muestra como un círculo con las primeras letras del nombre.

Los personajes de tu cuenta forman la **biblioteca de personajes** en *Ajustes → Ajustes de extensiones → Lectores beta*, que se inicia con seis lectores: Athena, Vesper, Ember, Quill, Nox y Sable. **Editar personajes** abre la biblioteca en una ventana donde puedes añadir, editar, copiar, eliminar y reordenar personajes, subir sus iconos y **Añadir los personajes iniciales** que falten en la biblioteca. Los cambios se aplican con **Guardar**, y **Cancelar** los descarta.

![La biblioteca de personajes](../../images/plugin-betareaders-library.png)

Un libro puede tener personajes propios: *Personajes de este libro* en el menú de los chats abre la misma ventana con los personajes que ofrece solo este libro. Se marcan como *(este libro)* cuando los eliges para un chat.

![Los personajes de un libro](../../images/plugin-betareaders-book-personas.png)

**Escribir con el modelo** en la ventana de un personaje hace la redacción por ti: describe el personaje en unas pocas palabras (*un lector al que solo le importa quién lo hizo y por qué*), pulsa el botón, y el modelo rellena el **Nombre** y la **Descripción** en el estilo de la biblioteca. Son solo campos: cámbialos, o vuelve a escribir, y pulsa **Guardar** cuando estén bien. El modelo que escribe es el de los chats (el del libro para los personajes de un libro, el de los ajustes del plugin para la biblioteca, y si no, tu modelo predeterminado); los nombres que ya están en la lista se le indican para que no elija uno repetido.

![Un personaje escrito por el modelo](../../images/plugin-betareaders-write-persona.png)

Un chat conserva una **copia** de sus personajes, así que un cambio en la biblioteca no altera un chat que ya ha empezado; la copia se cambia cuando lo pides (véase [el grupo](#a-group)).

<a id="starting-a-chat"></a>

## Iniciar un chat

**+** abre la ventana de un chat nuevo.

![La ventana de un chat nuevo](../../images/plugin-betareaders-new-chat.png)

| Campo | |
|---|---|
| Tipo | **Un personaje** o **Un grupo**. No se puede cambiar después. |
| Título | Vacío: los nombres de los personajes. |
| Personaje(s) | Un personaje para un chat con uno; para un grupo, al menos uno (un grupo puede empezar con un personaje y otros se unen después). |

La parte plegada, **Historia, medio, modelo y formato**, contiene las [opciones de un chat](#the-options-of-a-chat). Cada una puede quedar vacía, y entonces se hereda.

Un chat con **un personaje** es una conversación entre tú y ese personaje. Un **grupo** es una conversación de todos los personajes presentes contigo y entre ellos: una respuesta puede tener a varios hablando, uno tras otro, y pueden discrepar, bromear unos con otros y construir sobre lo que dice cada uno. Un mensaje de la respuesta también puede ser una narración (*Narrador*): una breve descripción de lo que hace el personaje.
<a id="talking"></a>

## Conversar

Escribe en el cuadro de abajo y pulsa **Enviar**; la respuesta aparece a medida que se escribe, y **Detener** (el botón es *Detener* mientras el modelo
responde) la corta. Una respuesta es un turno del modelo: el razonamiento del modelo, cuando envía uno (una sección **Razonamiento** cerrada; un modelo que no razona no muestra ninguna) y los mensajes de los personajes, cada uno con lo que hace y lo que dice.

<a id="sending-a-scene"></a>

### Enviar una escena

**Send as scene** (junto a *Enviar*) envía el texto como una escena en lugar de algo que dices: un cambio de escena (*Se apagan las luces y el bar queda en silencio.*), o una descripción de lo que haces (*Pongo el manuscrito sobre la mesa y espero.*). Aparece como una tarjeta **Escena** en el historial y los personajes reaccionan a ella en el momento, como a un mensaje. Usa la escena del chat (véase [abajo](#the-scene)) para indicar dónde empieza la conversación y este botón para lo que ocurre después. Un mensaje de escena se puede editar y borrar como tus mensajes; no envía partes de la historia (el cuadro *Include the latest parts* pertenece a *Enviar*).

![Una escena en el historial de un chat, y la respuesta a ella](../../images/plugin-betareaders-scene.png)

Los personajes del grupo saben quién está presente al final de tu último mensaje solamente (no se guarda nada de ello), así que añadir o quitar un personaje nunca cambia el historial anterior.

<a id="sending-the-story"></a>

### Enviar la historia

El chat no guarda una copia de la historia. Recuerda qué partes **diste** a los personajes, y en cada petición la historia que leen se vuelve a construir a partir de la historia tal como está ahora: las partes que se les dieron, con los resúmenes en lugar de las más antiguas, hasta donde quepan en el contexto del modelo. Las partes van delante del mensaje que las dio, así que una parte aparece en la petición una sola vez y el inicio de la petición sigue siendo el mismo texto cuando das más partes: un servidor de modelos que conserva el inicio del prompt solo lee lo nuevo. Una parte editada se lee como editada y una parte que borraste ya no se lee, lo que cambia la petición desde el mensaje que la dio. Un resumen hecho más tarde sustituye a las partes que describe a partir del siguiente mensaje.

**Include the latest parts** es lo que da a los personajes más historia: añade las partes que aún no se les han dado (véase [abajo](#how-much-of-the-story-the-personas-read)). Déjalo desmarcado para solo charlar y darles la historia más tarde. La etiqueta del cuadro indica cuántas partes añadiría (*Include the story (5 parts)*, *Include the latest parts (2 new)*, *none new*). Se actualiza solo: al añadir, editar o borrar una parte, o cambiar de rama, cambia el número y el cuadro se marca cuando hay algo nuevo. Un mensaje tuyo indica qué partes dio (*Parts 1-5 sent*).

**Última parte** y **Primera parte**, junto al cuadro, son [cuánta historia leen los personajes](#how-much-of-the-story-the-personas-read); cámbialas allí a medida que avanzas en la lectura.

Debajo del cuadro están lo que los personajes han leído (*The personas have read: parts 1-5*) y el tamaño del contexto (*Context: 2,266 of 30,720 tokens*): lo que enviaría el siguiente mensaje, es decir, el historial, lo que has escrito y, cuando **Include the latest parts** está marcado, las partes que añadiría. Cambia al marcar el cuadro o al pausar mientras escribes. Las partes reciben al menos la mitad del contexto; cuando el historial no cabe junto a ellas, los mensajes más antiguos (con las partes que dieron) se dejan fuera de la petición, no se borran, y un aviso indica cuántos.

<a id="changing-the-conversation"></a>

### Cambiar la conversación

| Botón | |
|---|---|
| lápiz (tu mensaje) | Edita tu mensaje; las partes que dio siguen dadas. |
| papelera | Borra tu mensaje, o un mensaje de una respuesta (el resto del turno se mantiene y el modelo ve el turno sin él). |
| Regenerate | Descarta la última respuesta y vuelve a pedirla. |

Cuando una respuesta falla o no hay ninguna, el panel lo indica y ofrece **Ask again**.

![Las acciones de un mensaje](../../images/plugin-betareaders-actions.png)

<a id="a-group"></a>

### Un grupo

*Personas (n)* encima de la conversación indica quién está presente. En un grupo puedes:

- **Incorporar un personaje** desde la biblioteca o el libro, con una nota opcional que explique al grupo por qué ha llegado ("Mara ha leído todo el libro"). El historial recibe una línea (*Mara joined the conversation.*), para que el modelo sepa cuándo.
- **Despedir** a un personaje (el botón de flecha de su fila), de nuevo con una nota. Siguen en la lista, en gris y marcados *(left)*. El último personaje no puede irse.
- **Actualizar desde la biblioteca**: toma la descripción y el icono actuales del personaje de la biblioteca (o del libro) y los lleva al chat.

![Un personaje que se unió al grupo](../../images/plugin-betareaders-joined.png)

![Incorporar un personaje](../../images/plugin-betareaders-bring-in.png)
<a id="the-options-of-a-chat"></a>

## Las opciones de un chat

La ventana de un chat nuevo y **Ajustes del chat** tienen las mismas opciones; cada una puede quedar vacía y entonces el chat la hereda.

| Opción | |
|---|---|
| Tu nombre | Cómo te llaman los personajes; tus mensajes lo muestran en lugar de *Tú*. Vacío: sin nombre. |
| Escena | Dónde y cómo ocurre la conversación, en unas pocas frases: *un bar lluvioso después del cierre*, *una videollamada después del club de lectura*. Sin escena, el chat es un chat en línea, véase [más abajo](#the-scene). Cambiarla en un chat en marcha cambia el prompt del sistema desde la siguiente petición y no deja ninguna línea en el historial; usa [un mensaje de escena](#sending-a-scene) para lo que ocurre a partir de ahora. |
| Medio | Lo que representa el texto: un libro, una película, una serie de televisión, un manga o cómic, un audiodrama, una obra de teatro, un juego o novela visual. Los personajes reaccionan como lo haría el público de ese medio. Cambiarlo en un chat en marcha deja una línea en el historial (*A partir de ahora el texto se lee como: ...*). |
| Modelo | La IA que responde. |
| Protocolo | El [protocolo](../protocols.md) que se usa con él. |
| Formato de respuesta | Cómo se le pide al modelo que escriba: **Automático** (JSON con un esquema cuando el modelo puede seguirlo, texto libre si no), **JSON** (siempre con un esquema), **JSON sin esquema** o **Texto libre**. Véase [el razonamiento](#the-reasoning). |
| Los personajes leen hasta la parte | El [límite de spoilers](#how-much-of-the-story-the-personas-read). Solo en la ventana de un chat nuevo; un chat existente lo tiene debajo del cuadro de texto. |
| Empezar desde la parte | La parte en la que empieza la historia que reciben los personajes. Igual que lo anterior. |
| Usar los resúmenes de la historia | Véase más abajo. |
| Mensajes más largos en un turno | El máximo de mensajes que escriben los personajes en respuesta a un mensaje tuyo (de 1 a 50). Vacío: tu ajuste en *Ajustes → Ajustes de la extensión → Beta Readers*. Una respuesta que lo supere se corta, y se le avisa al modelo del límite. |

![Los ajustes de un chat](../../images/plugin-betareaders-chat-settings.png)

<a id="the-reasoning"></a>

### El razonamiento

El **Razonamiento** que aparece encima de una respuesta es el pensamiento del modelo, tal como lo envía el servidor (igual que en los demás generadores de Marginalia); un modelo que no piensa no tiene ninguno, y no se muestra nada. El plugin no le pide al modelo un razonamiento propio: unas pocas frases escritas bajo un esquema serían más un plan que un pensamiento, y solo estorbarían a un modelo que piensa. El pensamiento que se escribe en el texto entre etiquetas `<think>` también se muestra como razonamiento.

**JSON sin esquema** (en *Ajustes del chat*) pide el mismo JSON solo en el prompt, sin formato de respuesta; úsalo con un servidor que no admita un esquema junto con el pensamiento del modelo.

<a id="the-text-as-it-is-written"></a>

### El texto tal como se escribe

La respuesta se muestra mientras el modelo la escribe, mensaje a mensaje y palabra a palabra: el mensaje que se está escribiendo aparece en cuanto se sabe quién habla, y va creciendo. Una parte que es una acción aparece en cuanto se escribe su tipo, de modo que un gesto nunca parece primero un diálogo.

La conversación sigue la respuesta a medida que crece. Si subes para leer algo anterior, se queda donde estás; si vuelves al final, vuelve a seguirla. Enviar un mensaje siempre lleva al final.

<a id="the-scene"></a>

### La escena

La escena es un texto que entra en el prompt del sistema de cada chat, después del prompt, para que los personajes sepan dónde están y cómo se hablan entre ellos. Es un [ajuste heredado](../books/prompts.md#inherited-settings): el del chat, si lo tiene; si no, el del libro; si no, el de la carpeta más cercana; si no, el de *Ajustes → Ajustes de la extensión → Beta Readers*; si no, el integrado: **un chat en línea**, donde todos escriben mensajes breves desde donde estén. Un prompt propio que tenga `{{scene}}` decide dónde queda la escena; sin él, la escena se añade después del prompt.

<a id="how-much-of-the-story-the-personas-read"></a>

### Cuánta historia leen los personajes

Las partes se numeran desde 1, como en la pestaña *Esquema* de la historia.

- **Los personajes leen hasta la parte** (el campo **Última parte** debajo del cuadro de texto) es el límite de spoilers: los personajes nunca reciben una parte posterior, y los resúmenes se recogen solo de la historia hasta ese punto. Súbelo cuando la historia avance, y el siguiente mensaje ofrecerá como nuevas las partes que quedaron retenidas. Bájalo y los personajes dejan de leer las partes posteriores de inmediato.
- **Empezar desde la parte** (**Primera parte**) deja fuera las partes anteriores, ni siquiera como resúmenes. Úsalo para que los personajes solo lean un capítulo.
- **Usar los resúmenes de la historia** (activado por defecto) permite que la historia empiece con los resúmenes y luego con las partes posteriores al más reciente de ellos, lo que mantiene pequeña una historia larga. Los resúmenes se aplican solo cuando la historia empieza desde su principio.

Una primera parte posterior a la última se rechaza.

Cambiar estos valores bajo el cuadro de texto (se guardan en cuanto dejas de escribir o pulsas un botón de paso) cambia la historia que leen los personajes a partir del siguiente mensaje; no se vuelve a enviar nada, porque la historia se construye para cada mensaje.

![Un chat que lee solo las partes 2 a 3](../../images/plugin-betareaders-spoiler.png)

El lorebook no se envía a los personajes: solo saben lo que les cuenta la historia.

<a id="settings"></a>

## Ajustes

*Ajustes → Ajustes de la extensión → Beta Readers* es el lugar de tus valores por defecto:

![Los ajustes del plugin](../../images/plugin-betareaders-settings.png)

| Ajuste | |
|---|---|
| Medio | El medio de los chats. |
| Modelo de los chats | La IA. Vacío: el modelo del libro. |
| Protocolo de los chats | Vacío: el protocolo del libro. |
| Prompt de un chat con un personaje | El prompt del sistema de un chat uno a uno. Vacío: el prompt integrado. |
| Prompt de un chat de grupo | El prompt del sistema de un chat de grupo. Vacío: el prompt integrado. |
| Escena | Véase [más arriba](#the-scene). Vacío: un chat en línea. |
| Mensajes más largos en un turno | El máximo de mensajes que escriben los personajes en respuesta a un mensaje tuyo, para cada chat que no tenga límite propio (de 1 a 50). Vacío: automático, 3 para un chat con un personaje y unos dos por cada personaje de un grupo (como máximo 8). Un límite corto mantiene las respuestas breves; súbelo cuando los personajes se corten a mitad de la conversación. |
| Biblioteca de personajes | **Editar personajes**, véase [más arriba](#personas). |
| Medios | **Editar medios**: cada medio tiene un nombre y el prompt que indica a los personajes cómo reaccionar ante él. **Restaurar los medios integrados** añade los que faltan. |

`{{medium}}` en un prompt se sustituye por el texto del medio y `{{scene}}` por la escena. Los ajustes se guardan con el botón **Guardar** de la página *Ajustes*.

La escena, el medio, el modelo, el protocolo y los dos prompts son también [ajustes heredados](../books/prompts.md#inherited-settings): la pestaña *Prompts* de un libro y los ajustes de una [carpeta](../books/folders.md) los tienen en la sección **Ajustes de la extensión**. Un libro usa su propio valor; si no, el de la carpeta más cercana; si no, el de *Ajustes*; si no, el integrado. El modelo y el protocolo del libro son el último recurso. Un chat sustituye el medio, el modelo y el protocolo para sí mismo en sus [opciones](#the-options-of-a-chat).

<a id="on-a-phone"></a>

## En el teléfono

Funciona. La pestaña **Beta Readers** está en el cajón de esquema de la historia, y los chats son los mismos que en el ordenador; el espacio para la conversación es pequeño, así que el modo horizontal resulta más cómodo. Véase [Usar Marginalia en el teléfono](../phone.md#extensions).

<a id="storage"></a>

## Almacenamiento

Los personajes y los chats de un libro se guardan en los atributos del libro (así viajan con sus copias de seguridad), los personajes de tu cuenta y los ajustes en los ajustes de usuario. El modelo se consulta con los límites habituales de la IA, y los tokens cuentan como usados por el libro.
