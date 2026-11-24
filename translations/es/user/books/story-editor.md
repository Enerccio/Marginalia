---
label: Editor de la historia
order: 90
---

# Editor de la historia

La pestaña **Story** de un libro es donde escribes. Muestra las partes de la [rama activa](branches-and-story-tree.md) desde el principio hasta el final.

![El editor de la historia](../../images/hero-story-editor.png)

Esta página describe la disposición de escritorio. En un teléfono, la historia es una sola columna y el esquema está en un cajón; consulta [Usar Marginalia en un teléfono](../phone.md#the-story).

| Área | |
|---|---|
| **Outline** (izquierda) | Una entrada por parte, `#1`, `#2`... con el ID de la parte en la base de datos. Haz clic en una entrada para desplazarte a la parte (si aún no están cargadas, primero se cargan las partes anteriores). El encabezado muestra cuántas partes tiene la rama. Una parte que tiene un [resumen](summaries.md) lleva un pequeño icono de documento después de su entrada. La extensión [Chapter Marker](../extensions/chapter-marker.md) muestra aquí los títulos de los capítulos. |
| **Lorebook** (izquierda, junto al Outline) | Una vista del lore del libro, con un cuadro de búsqueda y una edición rápida del texto de una entrada; consulta [La pestaña Lorebook](#the-lorebook-tab). |
| **Annotations** (izquierda, junto al Outline) | Las [notas](annotations.md) sobre pasajes de la historia, para todo el libro. |
| **Planner** (izquierda, junto al Outline) | Las escenas que planeas escribir, una lista que rellenas a mano; consulta [La pestaña Planner](#the-planner-tab). |
| **Story** (centro) | El texto de cada parte, con su menú (☰) y sus metadatos a la derecha, y debajo la barra inferior. |
| **Bottom bar** | El botón **Summaries** (icono de lista) y el menú de ajustes (⚙) a la izquierda, la línea de comandos y el botón **+** a la derecha. |

Así se ve un libro nuevo. Todas estas son pestañas que puedes organizar como quieras, incluida la historia; consulta [Organizar las pestañas](#arranging-the-tabs). El separador entre la barra lateral y la historia se puede arrastrar, y Marginalia recuerda dónde lo dejaste (se guarda con el libro) y vuelve a esa posición al abrir el libro de nuevo.

La historia siempre se abre por abajo, en la parte más reciente. Un libro largo no se dibuja de una vez: solo se cargan las últimas partes, y las anteriores se añaden arriba a medida que subes, sin mover la parte que estás leyendo. Las partes del otro extremo se descargan de nuevo, de modo que no permanecen cargadas más que el valor del ajuste **Parts loaded at once in the story** de tu [cuenta](../account.md) (si está vacío, se cargan todas). Si te desplazas más rápido de lo que llegan las partes, una marca *Loading earlier parts* (o *later parts*) en el borde indica que hay más. Hacer clic en una parte del esquema, o encontrar una con la búsqueda, carga las partes que la rodean, y empezar una parte nueva te devuelve al final de la historia.

<a id="searching-the-story"></a>

### Buscar en la historia

Encima del esquema hay un campo de búsqueda para el texto de la **rama en la que estás** (la búsqueda del árbol de la historia busca en todas las ramas). Como un libro largo no se carga de una vez, la búsqueda del navegador (Ctrl+F) no puede encontrar texto en las partes que no están cargadas; esta sí.

- Escribe lo que buscas; la búsqueda empieza cuando dejas de escribir. Varias palabras deben estar todas en la misma parte; una frase va entre comillas dobles (`"old key"`) y `*` representa cualquier carácter (`light*house`). No importan las mayúsculas.
- Cada lugar donde aparece una palabra es una coincidencia, y se numeran a lo largo de toda la rama. Debajo del campo ves *Match 3 of 27, in part #5* y una barra con tu posición. La historia se desplaza a la coincidencia y carga las partes que la rodean si no están cargadas.
- Los botones **up** y **down** llevan a la coincidencia anterior y a la siguiente (después de la última vuelve a la primera). **Enter** en el campo hace lo mismo que el botón down, **Shift+Enter** lo mismo que el botón up.
- Todas las coincidencias de las partes cargadas se resaltan en el texto, y la coincidencia en la que estás, con un color más intenso. Borra el campo para quitar el resaltado.

En el teléfono, la búsqueda está en el cajón del esquema; el cajón se cierra cuando muestra una coincidencia, así que ábrelo de nuevo para la siguiente.

<a id="the-lorebook-tab"></a>

### La pestaña Lorebook

La pestaña **Lorebook** de la barra lateral muestra el lore del lorebook del libro sin salir de la historia, así que no tienes que cambiar de pestaña para comprobar lo que dice una entrada o corregir una palabra en ella. Se vuelve a leer cada vez que regresas a la pestaña *Story*, así que los cambios que hiciste en la pestaña *Lorebook* del libro se ven al momento.

- El **cuadro de búsqueda** de arriba muestra solo las entradas que tienen el texto en su nombre, contenido, nota o filtro (no importan las mayúsculas).
- El **lápiz** de una entrada abre una pequeña ventana con su nombre, si se usa, el filtro y cómo se busca, el contenido y la nota; **Save** las guarda (las etiquetas, el orden y los ajustes de activación se cambian en la pestaña *Lorebook* del libro). No se puede cambiar nada mientras un agente trabaja en el lorebook.
- El **botón de ampliar** (⤢) abre el [editor de lorebook a pantalla completa](../lorebooks/lorebooks.md#editing-in-full-screen) (solo en escritorio): el que está junto al cuadro de búsqueda del lorebook, el de una entrada en esa entrada, el de un sub lorebook (con *Include sub lorebooks*) en ese lorebook. La pestaña se vuelve a cargar cuando cierras el editor.

- Cada entrada es una fila con su nombre y el lápiz; la flecha la despliega para mostrar su **contenido** (renderizado como Markdown) y el **texto que la activa** (el *Filtering Text/Pattern*, con una nota cuando es una expresión regular).
- **Include sub lorebooks** muestra el lorebook y cada uno de sus sub lorebooks (y los de estos, por profundos que sean) como filas propias, con sus entradas debajo; haz clic en la flecha de un lorebook para ocultar sus entradas. Sin esta opción solo se listan las entradas del lorebook del libro.
- Las entradas y los lorebooks desactivados (que no se usan en la historia) se muestran atenuados.
- Un libro sin lorebook muestra una nota en su lugar.

![La pestaña Lorebook con dos entradas abiertas](../../images/story-lorebook-tab.png)

Como cada pestaña del editor, puede arrastrarse fuera para formar un panel propio; consulta más abajo.
<a id="the-planner-tab"></a>

### La pestaña Planner

La pestaña **Planner** es una lista de las escenas por las que quieres que avance la historia, en el orden que quieras. Cada escena son los detalles de un turno que la escribe: un **título** para la lista, la **ambientación de la escena**, el **personaje POV**, los **personajes presentes** y las **instrucciones**, los mismos cuatro campos del formulario de nuevo turno.

![La pestaña Planner con cuatro escenas, la primera ya escrita](../../images/planner-tab.png)

- **Add scene** abre un formulario para una escena nueva; se añade al final de la lista. El lápiz edita una escena, las flechas la mueven hacia arriba o hacia abajo, y el cubo de basura la elimina.
- El **+** de la fila de una escena **inserta una escena nueva** justo debajo, entre esa escena y la siguiente.
- El botón de fusión de una escena **la combina con la siguiente**: se abre un formulario con los datos de ambas ya unidos (los dos títulos, las ambientaciones y las instrucciones una debajo de otra, los personajes presentes de ambas y el personaje POV de la primera; cuando la segunda tiene otro, el formulario lo indica). Cambia lo que no encaje y pulsa **Merge** (o <kbd>Ctrl/Cmd+Enter</kbd>): las dos escenas se convierten en una, en el lugar de la primera. Solo queda marcada como hecha si ambas lo estaban.
- **Import from the branch** añade una escena por cada parte de la rama en la que está el libro (desde la primera parte hasta la más reciente) que tenga detalles de turno: su ambientación, personaje POV, personajes presentes e instrucciones, con la primera línea de las instrucciones como título. Las partes sin detalles de turno se omiten, y las escenas se añaden después de las que ya tienes, sin marcar como hechas. Úsalo para planificar la reescritura de una historia que ya has escrito: importa, combina los turnos que pertenecen juntos, inserta lo que falta y luego escribe las escenas en una rama nueva o en una copia del libro.
- La casilla delante de una escena indica que está **hecha** (escrita): el título aparece tachado y la escena atenuada. Nada lo comprueba por ti; márcala cuando hayas escrito la escena. La línea sobre la lista cuenta cuántas están hechas.
- **Haz clic en una escena** para volcar sus detalles en el formulario de nuevo turno (se abre el formulario con la ambientación, el POV, los personajes presentes y las instrucciones de la escena; lo que había antes se reemplaza) y pulsa **Generate** cuando estés conforme con ella. En el teléfono, el cajón se cierra primero.

Las escenas se guardan con el libro, así que están en sus copias de seguridad y copias. Un modelo también puede añadir escenas: el [plot wizard](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) de la extensión Arc Outline y un [agente](../agents.md) pueden leer y cambiar el Planner con [comandos](../commands/index.md#the-planner); la lista muestra al instante lo que añadieron.

![El formulario de una escena nueva](../../images/planner-scene-dialog.png)

![Un clic en una escena rellena el formulario de nuevo turno con sus detalles](../../images/planner-new-turn.png)

Como cualquier pestaña del editor, puede arrastrarse fuera y colocarse en un panel propio; véase más abajo.

<a id="arranging-the-tabs"></a>

### Organizar las pestañas

El editor es un conjunto de pestañas: la **Story** en sí (las partes y la barra inferior bajo ellas), el **Outline**, el **Lorebook** y las pestañas que añaden las [extensiones](../extensions/index.md) (Characters, Arcs, Side Query...). Arrastra una pestaña por su cabecera para reordenarlas (solo en escritorio; el teléfono mantiene una columna, con el Outline en un cajón). Un libro nuevo tiene el Outline y el Lorebook en un panel a la izquierda, y la Story en una columna propia a la derecha. Puedes colocar la historia donde quieras: por ejemplo, en el centro, con el Outline a un lado y el Lorebook al otro, o encima del Outline:

- Suéltala sobre una de las franjas discontinuas que aparecen encima, entre y debajo de los paneles de una columna para **separarla** en un panel propio en esa columna; los paneles de una columna se apilan unos debajo de otros.
- Suéltala sobre una de las franjas altas a la izquierda, a la derecha o entre las columnas para **separarla hacia un lado**: obtiene una columna propia, junto a las demás. Amplía el editor con los divisores para dejar sitio a más columnas.

Los paneles y las columnas están separados por divisores: arrástralos para dar a cada panel el espacio que necesite. Los tamaños se guardan con la disposición.

![La barra lateral con Characters en una columna propia y Side Query debajo](../../images/story-editor-tabs.png)

- Suéltala sobre la cabecera de otra pestaña o sobre el cuerpo de otro panel para **unirla** a ese panel (soltarla sobre una pestaña la coloca antes de ella; soltarla sobre el cuerpo la coloca al final). Un panel que pierde su última pestaña desaparece, y lo mismo ocurre con una columna sin paneles.

La disposición y los tamaños se guardan con el libro, así que son los mismos la próxima vez que lo abras y en cualquier navegador; lo mismo ocurre con la pestaña que muestra cada panel. Una pestaña de una extensión que no está cargada conserva su lugar hasta que la extensión vuelva.

La historia siempre está en algún sitio: su pestaña no puede cerrarse, y una pestaña que comparte panel con otras queda oculta mientras se selecciona otra. Cualquier cosa que muestre una parte devuelve la historia a primer plano: hacer clic en una entrada del Outline, en un resultado de la búsqueda o en una rama del [árbol de la historia](branches-and-story-tree.md), e iniciar una generación, seleccionan antes la pestaña **Story**. Mientras una generación prepara su prompt (normalmente unos segundos), las demás pestañas están deshabilitadas; cuando el modelo empieza a escribir, vuelven a estar disponibles, véase [más abajo](#while-a-part-is-generated).

<a id="minimizing-tabs"></a>

### Minimizar pestañas

Cada pestaña, excepto la de la historia, tiene un pequeño botón **⌄** (chevron) junto a su nombre. Ese botón la **minimiza**: la pestaña sale de su panel (un panel cuyas pestañas están todas minimizadas desaparece por el momento) y aparece en una barra en el borde del editor un icono redondo, un círculo de color con un dibujo (una lista para el Outline, un libro para el Lorebook, un bocadillo para las anotaciones, una lupa para Side Query, etc.; una pestaña de una extensión que no tiene dibujo recibe una o dos letras). Pasa el ratón sobre el icono para ver de qué pestaña se trata. Nada se cierra: la pestaña conserva lo que mostraba. Haz clic en el icono y la pestaña vuelve al panel, a su lugar entre las pestañas, y se muestra.

La barra está ahí mientras haya alguna pestaña minimizada. El botón de su parte superior (las flechas circulares) envía la barra al siguiente lado del editor, en el sentido de las agujas del reloj: izquierda, arriba, derecha, abajo. La historia no puede minimizarse, así que siempre hay algo en pantalla.

![Pestañas minimizadas en la barra del borde del editor](../../images/story-minimized-tabs.png)

Los iconos están en orden alfabético por el nombre de las pestañas hasta que lo cambies: arrastra un icono sobre otro y ocupará el lugar de este. Qué pestañas están minimizadas, dónde está la barra y el orden de los iconos se guardan con el libro, junto con la disposición y los tamaños.

<a id="the-default-arrangement"></a>

### La disposición predeterminada

Un libro nuevo se abre con la disposición estándar (el Outline, el Lorebook y las anotaciones a la izquierda, la historia a la derecha). Para hacer de tu propia disposición la estándar, ajusta las pestañas como quieras (paneles, tamaños, pestañas minimizadas, lado de la barra) y elige **Save layout as my default** en el menú de ajustes (⚙) de la historia. Todo libro que no tenga disposición propia (uno que no hayas reorganizado) se abrirá entonces con ella; un libro que hayas organizado conserva la suya. La predeterminada se guarda en tus ajustes de usuario.

Para volver a la disposición predeterminada, elige **Restore default layout** en el menú de ajustes (⚙) de la historia y confírmalo. Es la que guardaste, o la estándar si no guardaste ninguna. Solo devuelve las pestañas a su sitio (y olvida los tamaños y lo que está minimizado): tus textos, la pestaña que muestra cada panel y las pestañas de extensiones se mantienen, estas últimas en el primer panel.
<a id="writing-the-next-part"></a>

## Escribir la siguiente parte

1. Haz clic en **+** en la esquina inferior derecha. Se abre el panel *New Turn Instructions*.
2. Rellena los campos. Todos son opcionales, pero el modelo necesita al menos alguna indicación:

   | Campo | |
   |---|---|
   | **Scene Setting** | Dónde y cuándo transcurre la parte, p. ej. *El faro, una noche tormentosa a finales de otoño*. |
   | **POV Character** | El personaje desde cuyo punto de vista se escribe la parte. |
   | **Present Characters** | Quiénes están en la escena y en qué estado. |
   | **Instructions** | Lo que debe ocurrir en esta parte. |

   *Scene Setting*, *POV Character* y *Present Characters* se rellenan a partir de la última parte, así que solo cambias
   lo que sea distinto. *Instructions* empieza vacío cada vez.
3. Haz clic en **Generate**.

![El panel New Turn Instructions](../../images/new-turn-instructions.png)

La nueva parte aparece al final y se va rellenando mientras el modelo la escribe. En los [modelos de razonamiento](../inference-providers.md#reasoning-models)
el razonamiento aparece primero, bajo **View Reasoning**.

Mientras se genera una parte, el botón **+** se convierte en un botón rojo **Stop**. Al detenerlo, el texto escrito hasta ese momento se conserva como parte.

<a id="while-a-part-is-generated"></a>

### Mientras se genera una parte

Durante los primeros segundos, mientras el prompt se compone a partir del libro, toda la ventana del libro espera. En cuanto el modelo empieza a escribir, solo quedan retenidos la nueva parte y las partes anteriores:

- **La nueva parte** no se puede tocar hasta que termine: ni editar, ni menú.
- **Las partes anteriores** se quedan donde están: puedes editarlas, anotarlas y reescribirlas, pero no borrarlas, fusionarlas ni ramificarlas, y la historia se mantiene en su rama (sin swipe ni cambio de rama). Un cambio en una parte anterior, o en el lore, no llega a la parte que se está escribiendo: su prompt ya se había compuesto. Cuenta para lo que venga después.
- **Todo lo demás está libre**: el lore, los ajustes y el prompt del libro, las pestañas de la barra izquierda (side query, arc outline, notes...), la barra de comandos. Una petición al modelo espera a una conexión libre del modelo si la generación tiene la única.
- **En espera**: un segundo turno, buscar y reemplazar, una importación en el libro y el borrado masivo. Las pestañas de árbol y copias de seguridad también esperan.

Lo que la ventana deja en gris también se rechaza cuando lo pide un comando, un agente o una extensión: Marginalia indica qué lo impide (*This is in use: …*).

**Salir del libro detiene la generación.** El botón de salida pregunta primero; *Yes* equivale a **Stop**: lo que llegó se conserva. Cerrar la pestaña del navegador hace lo mismo, y el navegador pregunta antes de cerrar o recargar la pestaña mientras se genera una parte. Una pérdida breve de la red, o un portátil cerrado un minuto, no es salir: la página vuelve y muestra lo que llegó entretanto.

Cuando el modelo devuelve un error (servidor no accesible, clave de API incorrecta, contexto demasiado largo...), se muestra el error. Si todavía no llegó ningún texto, la historia queda como estaba: una parte nueva o un swipe se elimina, y una parte regenerada recupera su texto anterior. El texto que llegó antes del error se conserva, como con **Stop**. Soluciona el problema y genera de nuevo.

Cómo llegan los campos al prompt lo determina el [User Prompt](prompts.md#user-prompt) del libro. Con el user prompt por defecto, el modelo los recibe como *Current Location & Time*, *Point of View Character*, *Characters Currently Present / State* y *Plot Specifications for the Next Part*.

!!!
El texto se escribe en Markdown: `*italics*`, `**bold**`, `# Heading` y así sucesivamente se muestran con formato. Una parte con un encabezado
`#` es un capítulo para la extensión [Chapter Marker](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Redactar las instrucciones

¿No sabes qué debe pasar después? **Draft Instructions**, debajo del campo *Instructions* del panel, deja que el modelo escriba las instrucciones en tu lugar, como lo harías tú. Se abre una ventana con:

| | |
|---|---|
| **Guidance** | De qué debe tratar la parte: una idea general, una dirección («ella lo confronta por la carta»), o nada en absoluto, y el modelo continúa la historia por donde vaya. Empieza con lo que ya escribiste en *Instructions*. |
| **Plan of the Story** | Solo cuando algo lo proporciona. El [Arc Outline](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter) lo hace, con los datos del capítulo actual. Solo lectura; el modelo lo sigue. |

**Draft Instructions** pone en marcha el modelo y vuelca la respuesta en **Drafted Instructions**, que puedes cambiar o vaciar antes de seguir; **Draft Again** lo intenta una vez más, **Stop** conserva lo que haya llegado. No se cambia nada hasta que hagas clic en **Use as Instructions**: entonces el texto sustituye al *Instructions* del panel (los demás campos no se tocan) y el panel se vuelve a abrir, listo para **Generate**.

El modelo recibe la historia hasta ahora (la historia más antigua como [resúmenes](summaries.md), como en una generación), el escenario, el POV y los personajes presentes que están en el panel, el lore de la última parte, tus últimas instrucciones como ejemplos de cómo las escribes, tu guía y el plan. No ve las entradas de tu lorebook que activaría un nuevo turno. La petición es el [impersonate prompt](prompts.md#impersonate-prompt). El modelo es el del libro; redactar consume tokens como cualquier petición.

![Redactar las instrucciones del siguiente turno](../../images/impersonate-dialog.png)

Lo mismo hace el comando [`/impersonate`](../commands/index.md), que devuelve el texto. Los [agentes](../agents.md) no lo reciben: escriben la historia con instrucciones propias.

<a id="the-settings-menu"></a>

## El menú de ajustes

El menú de ajustes (⚙) de la barra inferior agrupa lo que actúa sobre toda la historia. **Search and replace** está arriba; el resto está en submenús (también en el móvil, excepto los elementos solo de escritorio):

| Submenú | Elementos |
|---|---|
| **Appearance** | **Change Styles For Text**, **Highlight Generated Text** y **Annotations in the margin** (solo escritorio, el último) |
| **Analysis** | **Annotate a range of parts with AI**, **Create summaries**, **Take lore notes with an agent**, **Automatic annotation**, **Phrase watch list**, **Branch Tokens**, **Lore Usage**, **Statistics** |
| **Import & export** | **Import**, **Export story** (y los elementos de las extensiones que importan o exportan, como **Export instructions**) |
| **Cleanup** | **Clear history of the book**, **Empty trash of the history**, **Mass delete** |
| **Layout** (solo escritorio) | **Save layout as my default**, **Restore default layout** |

Los elementos que traen las [extensiones](../extensions/index.md) están en el submenú que les corresponde, o al final del menú. Las teclas de los elementos están en *Settings → Keyboard shortcuts*, donde un elemento se nombra con su submenú (*Analysis › Statistics*). Las secciones siguientes indican dónde está cada elemento.
<a id="the-part-menu"></a>

## El menú de la parte

Cada parte tiene un menú (☰) en su esquina superior derecha. **Edit**, **Discard Changes**, **Regenerate**, **Swipe**, **Redraft**, **History** e **Images** están en el propio menú, **Delete** es su último elemento, y el resto está en cuatro submenús: **Annotations** (*Annotate part*, *Annotate with AI*), **Summary** (*Generate summary* / *View summary*, *Delete summary*), **Inspect** (*Show Prompt*, *Compare Prompt*, *Token Usage*, *Lore Usage*) y **Branch & merge** (*Branch Story*, *Merge into previous part*). Un submenú que no tiene nada para la parte no se muestra. El Reviewer añade su submenú **Review** después de *Summary*. Los elementos:

| Elemento | |
|---|---|
| **Edit** | Convierte el texto en un campo editable. **Save** (el mismo elemento del menú) lo guarda. Iniciar una generación o salir de la pestaña también guarda las ediciones abiertas. |
| **Discard Changes** | Solo mientras el texto está editado: cierra el campo sin guardar, y la parte conserva el texto con el que se guardó por última vez. Si cambiaste el texto, se te pregunta antes. |
| **Regenerate** | Vuelve a escribir la última parte, reemplazando su texto. Solo en la última parte. |
| **Swipe** | Escribe otra versión de la última parte y conserva la anterior como una rama separada. Solo en la última parte. Véase [Ramas y árbol de la historia](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Redraft** | Permite que el modelo vuelva a escribir esta parte a partir de tus instrucciones, en cualquier parte; véase [más abajo](#redrafting-a-part). No mientras el texto está editado. |
| **History (n)** | Las [versiones del texto](version-history.md) de la parte, con el número de versiones: compararlas, restaurar una o darle un nombre. No mientras el texto está editado. |
| **Annotate part** | Añade una [nota](annotations.md#making-an-annotation) sobre la parte completa. Para anotar palabras, selecciónalas y haz clic derecho (**Annotate selection**). |
| **Annotate with AI** | Permite que un modelo [revise la parte](annotations.md#letting-a-model-annotate) y proponga notas. No mientras el texto está editado. Para varias partes a la vez, usa **Annotate a range of parts with AI** en el menú de configuración (⚙), y para cada parte nueva, **Automatic annotation** (véase [Anotaciones](annotations.md#annotating-every-new-part-automatically)). |
| **Branch Story** | Inicia una nueva rama desde esta parte; véase [Ramas y árbol de la historia](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Merge into previous part** | Añade el texto de esta parte a la parte anterior; véase [más abajo](#merging-a-part-into-the-previous-one). No está disponible en la primera parte. |
| **Show Prompt** | Muestra el prompt exacto que se envió al modelo para esta parte. |
| **Token Usage** | Muestra cómo se usó el contexto del modelo para esta parte: un gráfico de las secciones del prompt (instrucciones del modelo, lore, resúmenes, la historia hasta ese momento, tus instrucciones, espacio reservado para la respuesta) y el tamaño de cada mensaje enviado al modelo. Si parte de la historia no cupo, indica cuántas partes se dejaron fuera. Las partes escritas antes de que esto se registrara no tienen gráfico. |
| **Lore Usage** | Enumera las entradas del [lorebook](../lorebooks/index.md) que entraron en el prompt de esta parte, con los tokens que costó cada una y qué la encontró (el prompt, o el contenido de otra entrada cuando el lorebook busca más a fondo). Las partes escritas a mano, las escritas antes de que esto se registrara o las escritas para un libro sin lorebook indican que no se registró nada. Para todo el libro, usa **Lore Usage** en el menú de configuración (⚙), véase [más abajo](#which-lore-entries-were-used). |
| **Images** | Adjunta imágenes a la parte; véase [Imágenes](#images). |
| **Generate summary** / **View summary** | Resume la historia hasta esta parte, o muestra el resumen; véase [Resúmenes](summaries.md). |
| **Delete summary** | Elimina el resumen de esta parte. Para un [metaresumen](summaries.md#meta-summaries), pregunta si quieres deshacerlo o eliminarlo por completo. |
| **Delete** | Elimina la parte; véase [más abajo](#deleting-a-part). |

*Regenerate* y *Swipe* usan los detalles del turno de la última parte (véase [más abajo](#turn-details)). Para cambiar lo que debe ocurrir, edítalos primero.

![El menú de una parte, con el submenú Inspect abierto](../../images/story-part-menu.png)

![Token Usage de una parte](../../images/token-usage.png)

<a id="part-metadata"></a>

## Metadatos de la parte

Junto a cada parte:

| | |
|---|---|
| **Order #n \| DB ID: m** | La posición de la parte en la rama, y su ID en la base de datos. |
| **Model** | El proveedor de inferencia que escribió la parte. |
| **Tokens: x (y)** | `x` tokens de texto en la parte, `y` tokens en el prompt que la produjo. |
| **Words** | Palabras en la parte. |
| **Turn Details** | Las instrucciones desde las que se escribió la parte. |

<a id="turn-details"></a>

### Detalles del turno

**Turn Details** abre la *Scene Setting*, *POV Character*, *Present Characters* e *Instructions* desde las que se escribió la parte. En la última parte pueden cambiarse (los cambios se guardan de inmediato) y los usan *Regenerate* y *Swipe*. En las partes anteriores son de solo lectura.

![Detalles del turno de una parte](../../images/turn-details.png)

<a id="editing-the-text"></a>

## Editar el texto

El texto generado es tuyo para cambiarlo. Elige **Edit** en el menú de la parte, cambia el texto y elige **Save**. Las partes editadas se envían al modelo tal como las editaste.

Editar una parte que cubre un [resumen](summaries.md) deja ese resumen desactualizado; Marginalia lo advierte en la siguiente generación y descarta el resumen. Para que esto no ocurra por sorpresa, **Save** comprueba primero el texto nuevo: cuando dejaría resúmenes desactualizados, aparece una ventana que indica cuántos son y ofrece **Back to editing** (no se guarda nada; el editor queda abierto con tu texto), **Save anyway** (los resúmenes quedan desactualizados) y **Save and revalidate summaries** (el texto se guarda y los resúmenes siguen siendo válidos, para un cambio que no altera lo que dicen, como una errata). La comprobación se hace al usar **Save** (el menú, el atajo y el botón del editor); los guardados automáticos, por ejemplo al iniciar una generación con el editor abierto, no preguntan.

<a id="keyboard-shortcuts"></a>

## Atajos de teclado

En un ordenador (el [diseño para teléfono](../phone.md) no tiene ninguno) el ciclo de escritura funciona con el teclado:

| Tecla | |
|---|---|
| `+` | Abre el panel *New Turn Instructions* con el cursor en *Instructions*. |
| `Ctrl+Enter` | Genera desde el panel abierto. `Esc` lo cierra; el teclado vuelve a la historia. |
| `Ctrl+.` | Detiene la generación en curso. |
| `s` / `r` | **Swipe** / **Regenerate** la última parte. |
| `e` | Abre el editor de la parte más visible en la pantalla (la última parte, si ninguna lo es). |
| `Ctrl+S` | Guarda la parte que se está editando (también con el cursor en su texto). |
| `/` | Lleva el cursor a la barra de comandos (primero se muestra la pestaña de la historia). |
| `Ctrl+Shift+F` | Abre la pestaña *Outline* y lleva el cursor a la búsqueda de la rama (`Enter` / `Shift+Enter` van a la siguiente / anterior coincidencia). |
| `Ctrl+Shift+H` | Abre la ventana *Search and replace* del libro. |
| `a` | **Annotate selection**: hace una nota sobre el texto seleccionado en una parte. |
| `Alt+PageDown` / `Alt+PageUp` | Muestra la pestaña siguiente / anterior, recorriendo las pestañas de todas las hojas en el orden del diseño y volviendo al principio. |

Una tecla sin `Ctrl`, `Alt` o `Meta` (como `+`, `s`, `r`, `e`, `a`, `/`) funciona solo cuando ningún campo de texto tiene el foco, para que nunca estorbe al escribir. Los atajos funcionan en la pestaña de la historia del libro abierto y no mientras otra ventana está abierta encima. Los botones muestran su tecla en la descripción emergente, y el menú de la última parte la muestra después de *Edit*, *Regenerate* y *Swipe*. En un Mac, `Cmd` funciona donde una tecla dice `Ctrl` (salvo que hayas asignado esa tecla `Cmd` a otra acción). Puedes cambiar cada tecla en [Configuración → Atajos de teclado](../account.md#keyboard-shortcuts).
<a id="deleting-a-part"></a>

## Eliminar una parte

**Delete** quita la parte de la historia. Las partes que venían después se mantienen y continúan desde la parte anterior a la eliminada, así que eliminar una parte en medio de la historia solo la saca. Eliminar la última parte hace que la parte anterior pase a ser el nuevo final de la rama.

<a id="deleting-many-parts-at-once"></a>

### Eliminar muchas partes a la vez

El menú de ajustes (⚙) tiene **Mass delete**. Al activarlo aparece una casilla a la izquierda de cada parte y, debajo de la historia, una barra con el número de partes marcadas y un botón **Delete**. Marca las partes que quieras eliminar (las marcas se conservan al desplazarte, porque la historia carga solo una ventana de partes); **Shift + clic** sobre una casilla marca (o desmarca) todas las partes comprendidas entre esa y la que pulsaste antes; una parte marcada se tiñe de rojo. Después pulsa **Delete**: tras una confirmación, las partes se eliminan una a una mientras una barra de progreso avanza y no se puede detener. Se eliminan igual que una sola parte: las partes que seguían a una parte eliminada continúan desde la parte anterior, y las partes eliminadas van a la papelera. Al terminar, se vuelve a mostrar la historia y el modo de eliminación múltiple se desactiva. **Cancel** en la barra, o elegir **Mass delete** de nuevo, lo desactiva sin eliminar nada. Iniciar una generación también lo desactiva. Si una parte marcada está siendo usada por una ventana o una traducción, se queda en la historia y Marginalia indica qué la está usando.

![Eliminación múltiple con algunas partes marcadas](../../images/story-mass-delete.png)

<a id="redrafting-a-part"></a>

## Reescribir una parte

**Regenerate** y **Swipe** actúan solo sobre la última parte y la escriben desde cero. **Redraft** actúa sobre cualquier parte, también sobre una antigua en medio de la historia, y conserva lo que no pides cambiar. Elige **Redraft** en el menú de la parte:

1. Escribe lo que debe cambiar en **Instructions for the redraft**: *añade más diálogo entre los dos*, *haz la pelea más larga*, *quita la explicación del mapa*, *mantén el final tal como está*. Si las instrucciones están vacías, la parte se vuelve a escribir con los mismos sucesos.
2. Elige **Length** si la parte debe quedar **Shorter** o **Longer** (por defecto, **Same length**): se añade después de tus instrucciones una frase que lo pide (véanse [Redraft Shorter Prompt y Redraft Longer Prompt](prompts.md#redraft-shorter-and-longer)). Elige **Redraft**. El texto aparece en **Redrafted text**; **Stop** lo interrumpe antes de tiempo. Elige **Redraft again** para intentarlo de nuevo, con otras instrucciones si quieres. **Differences** muestra qué ha cambiado respecto a la parte, palabra por palabra. Si la parte tiene [anotaciones](annotations.md) abiertas, **Deal with the open annotations of the part** (marcada desde el principio) también se las entrega al modelo, y **Apply** las marca como resueltas.
3. El texto reescrito es un campo que todavía puedes editar (su icono de esquina lo hace crecer hasta mostrar todo el texto), y **Differences** sigue tus cambios. **Apply** reemplaza el texto de la parte por el reescrito; **Close** lo descarta. Nada del libro cambia hasta que pulses **Apply**.

![La ventana de reescritura: las instrucciones, el texto nuevo y las diferencias](../../images/redraft-dialog.png)

El modelo recibe los [prompts de reescritura](prompts.md#redraft-prompt): la parte, tus instrucciones, la historia anterior a la parte (la historia más antigua como [resúmenes](summaries.md) cuando hay resúmenes válidos, y el resto como texto, empezando por la parte más reciente si no cabe todo), tanta historia posterior a la parte como quepa (hasta un cuarto del espacio disponible), el lore que los libros de lore dan ahora a esa parte (calculado de nuevo para el turno de la parte, de modo que una edición de una entrada llega a la reescritura; el lore con el que se escribió la parte solo se usa si el libro no tiene ningún libro de lore), el punto de vista, el tiempo verbal y el estilo del libro, y la escena, el personaje con el punto de vista, los personajes presentes e instrucciones del turno del que procede la parte. La historia posterior a la parte está para que el nuevo texto siga enlazando con ella: los sucesos en los que se apoyan las partes siguientes deben mantenerse.

La parte conserva su lugar, su resumen, sus imágenes y la forma en que se generó; el texto, sus palabras y sus tokens se reemplazan, y la parte cuenta como editada. Aplicar la reescritura es una edición, como **Save** en el editor, así que los [resúmenes](summaries.md) que describen el texto antiguo quedan desactualizados, y Marginalia indica cuántos. Un resumen de la propia parte es uno de ellos.

Cuando un resumen describe la parte (la parte tiene uno, o lo tiene una parte más reciente de la rama), la ventana muestra la casilla **Revalidate the summaries this redraft makes outdated**, desmarcada al principio. Márcala cuando los resúmenes sigan diciendo lo correcto (la reescritura cambió la redacción, no lo que ocurre): **Apply** entonces recalcula su hash y siguen siendo válidos. Déjala desmarcada cuando hayan cambiado los sucesos, y haz los resúmenes de nuevo.

La ventana no bloquea la historia; véase [Trabajar junto a la ventana](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Trabajar junto a las ventanas de reescritura y anotación

En un ordenador, las ventanas **Redraft** y **Annotate with AI** se colocan a la derecha de la historia y la dejan utilizable: puedes leer, desplazarte y editar la historia mientras el modelo escribe. Arrastra una ventana por su título para moverla, o por su borde para cambiar su tamaño. La flecha (⌄) del título pliega la ventana en el [panel de ejecuciones](#the-panel-of-the-runs) de la parte inferior izquierda de la página: el modelo sigue trabajando, **Open** (o un clic en la entrada) vuelve a mostrar la ventana, y **Stop** detiene el modelo (cuando ha terminado, **Close** cierra la ventana). En un teléfono las ventanas siguen tapando la historia, pero se pliegan de la misma manera.

![La ventana de reescritura junto a la historia mientras el modelo escribe, y la ejecución en el panel de la parte inferior izquierda](../../images/redraft-beside-story.png)

**Close y Stop son dos cosas distintas.** El **Stop** de la propia ventana detiene el modelo, y la ventana conserva lo que ya llegó. **Close** mientras el modelo trabaja no lo detiene: la ventana se pliega en el panel y el modelo sigue en su cola; su respuesta está en la ventana cuando la vuelves a abrir. Cierra una ventana cuyo modelo ya terminó, o usa **Close** en el panel, y desaparecerá; cerrar el libro o la pestaña del navegador también detiene una ventana plegada. Lo mismo vale para las demás ventanas que esperan a un modelo: un **summary** (y *Create summaries*, *Rewrite all summaries*), el [Reviewer](../extensions/reviewer.md), la descripción del libro, los generadores de lore y de esquema, los generadores de imágenes y de manga, y el redactor de personas de Beta Readers. Las ventanas *Studio* y *Convert* del audiolibro tampoco bloquean la página; cerrarlas nunca detuvo la conversión.

Mientras una ventana está abierta (o plegada) sobre una parte, la parte muestra en qué está trabajando junto a su menú (*Redrafting · The book*), y **Regenerate**, **Swipe**, **Redraft**, **Annotate with AI**, **Merge into previous** y **Delete** quedan desactivados para esa parte, de modo que la ventana no pierde su parte. Editar la parte sigue siendo posible. Cerrar el libro cierra sus ventanas y detiene sus modelos. Una parte en la que trabaja un [traductor](../extensions/real-language-translator.md) muestra *Translating the speech of the part...* de la misma manera, y dos traductores nunca trabajan a la vez sobre una misma parte: el segundo indica que la parte está en uso.

- Cuando **Apply** una reescritura de la parte completa y la parte se cambió después de abrir la ventana (la editaste entretanto), Marginalia pregunta antes: **Differences** compara entonces la parte tal como está ahora con la reescritura, para que veas qué reemplazaría aplicarla; elige **Apply anyway** o **Cancel**. Una reescritura que [se aplica sola](annotations.md#annotating-every-new-part-automatically) no hace esto cuando la parte se cambió: la ventana espera a que decidas.
- Cuando la parte se eliminó antes de que pulses **Apply** en la reescritura o añadas las anotaciones, la ventana lo indica, no escribe nada y se cierra.
<a id="the-panel-of-the-runs"></a>

### El panel de las ejecuciones

Todo lo que trabaja sobre tus libros con un modelo en este momento aparece en la parte inferior izquierda de cada página: una generación, una redacción de nuevo o una anotación, una traducción por el traductor [Real Language](../extensions/real-language-translator.md) o [Conlang](../extensions/conlang-translator.md), una reescritura de los resúmenes, el Knowledge Tracker leyendo la historia, una conversión a [Audiobook](../extensions/audiobook.md), un comando de la barra de comandos que tarda (*Draft instructions*, un resumen...: su ventana queda a un lado y se pliega como las demás), un [agente](../agents.md) trabajando. También aparecen las ventanas en las que esperas una respuesta mientras el modelo contesta: **Impersonate**, el asistente de ayuda, una traducción rápida, el diseñador de Conlang, un chat de Side Query (y el nombre de su pestaña), Beta Readers o el asistente de la trama. El panel lista tus ejecuciones de **todas tus páginas abiertas** (otra pestaña, el teléfono), no solo las iniciadas en esta página. Si no hay nada en marcha, no se muestra nada.

El panel de las ejecuciones abierto: la redacción plegada dentro, con el final del texto del modelo, Open y Stop

![El panel de las ejecuciones abierto: la redacción plegada dentro, con el final del texto del modelo, Open y Stop](../../images/runs-panel.png)

- **Closed** es una barra pequeña: la primera ejecución, cuántas más hay (*Translating the speech of the part... · 2 more*) y una barra en movimiento mientras un modelo trabaja. Haz clic en ella para abrir la lista; se abre sola cuando pliegas una ventana dentro del panel.
- **Open**, cada ejecución muestra qué hace, el libro, cuánto tiempo lleva, qué espera y el final de lo que escribe el modelo:
  - *Waiting for the model: 2 requests before this one* cuando el modelo está ocupado con otras peticiones (véase
    *Connections* en [Inference providers](../inference-providers.md));
  - *The model is thinking...* o *The model is writing...*, con el final de su razonamiento o de su respuesta debajo;
  - *Waits for you* cuando la ejecución espera una decisión tuya (una redacción que aplicar, una propuesta del Knowledge Tracker que revisar, la pregunta del Prompt Inspector antes de una generación, un comando del asistente de la trama que permitir).
- **Stop** detiene la ejecución, esté donde esté iniciada. **Open**, o un clic en la entrada, muestra de nuevo una ventana plegada (solo en la página en la que se abrió) o la ejecución misma; es **Review** cuando la ejecución espera por ti. Una ejecución del Knowledge Tracker abre su libro en esta página (si no hay otro libro abierto aquí; si lo hay, se te pide que cierres ese primero), cambia a la pestaña Knowledge y muestra la revisión. El **Stop** de un agente lo pausa. **Close** cierra una ventana de forma definitiva (y detiene su modelo).
- **What ended** permanece en la lista durante dos horas, debajo de lo que se ejecuta, con cómo acabó (*Completed*, *Failed*) y cuánto tardó: una traducción, resúmenes, lo que siguió a una parte, la sesión de un agente, una importación, un trabajo que continuó después de que salieras de su página. **Close** lo quita de la lista. Una generación de la historia y las ejecuciones que detuviste no se guardan.
- En un ordenador puedes mover el panel por su agarre (⋮ a su izquierda); se queda donde lo dejes en este navegador. En la mitad inferior de la página crece hacia arriba al abrirlo, igual que en su esquina, y siempre se mantiene dentro de la página. Cuando ocupa el lugar del botón redondo del [asistente de ayuda](../administration/help-assistant.md) (sobre un libro, ambos están en la esquina inferior izquierda), el botón se aparta y vuelve cuando el sitio queda libre.

Cuando una ejecución que iniciaste termina después de que se cerró su página, otra página abierta tuya envía su [notificación](../account.md#notifications) (para las ejecuciones que la tienen, como la reescritura de los resúmenes).

**Lo que se rechaza mientras algo se ejecuta.** La siguiente parte no se genera mientras hay abierta una ventana de [redacción](#redrafting-a-part) del libro (ciérrala, o aplícala, primero), ni mientras un comando de la barra de comandos está a punto de generar. Una parte no se fusiona con la anterior mientras una ventana o una traducción trabaja sobre esa, y la búsqueda y reemplazo deja fuera la parte que una generación está escribiendo (se cuenta como omitida). Un [agente](../agents.md) no arranca en un libro que está abierto en una ventana o sobre el que trabaja otra cosa. Cuando varias ejecuciones esperan a un mismo modelo, tu siguiente generación va antes que la anotación, la traducción o el audio de la parte anterior.

<a id="redrafting-only-a-passage"></a>

### Volver a redactar solo un pasaje

Para reescribir unas pocas frases y dejar el resto de la parte como está, selecciónalas en el texto de la parte y haz **clic derecho** (una pulsación larga en un teléfono): el menú tiene **Redraft selection**. (El menú ☰ de la parte no se ofrece para esto; abrirlo puede perder la selección en algunos navegadores.) La ventana funciona como arriba con dos diferencias: muestra el **Selected text**, y la redacción es solo el texto que lo sustituye. El modelo recibe la parte entera con la selección marcada, y la historia y el lore que la rodean, de modo que el nuevo pasaje enlaza con las frases anteriores y posteriores. **Differences** compara la selección con el texto redactado; **Apply** pone el texto redactado en el lugar de la selección y conserva el resto de la parte, incluidos el espacio o el salto de línea en los bordes de la selección. Con [anotaciones](annotations.md) abiertas, la casilla ofrece solo las que se solapan con la selección. Si la parte se cambió después de que seleccionaras el texto (en otra ventana), **Apply** se niega y tienes que seleccionarlo de nuevo. Mientras la parte se edita se usa la selección del editor, y antes se guarda la edición.


<a id="what-follows-a-generated-part"></a>

### Lo que sigue a una parte generada

Cuando una parte que generaste (desde la página o desde la barra de comandos) está completa, el libro puede hacer más con ella por sí mismo, en este orden:

1. **Annotate**: la [anotación automática](annotations.md#annotating-every-new-part-automatically) revisa la parte.
2. **Mark phrases**: la [lista de vigilancia de frases](annotations.md#watching-for-phrases) anota las frases que encuentra (sin modelo).
3. **Redraft**: la parte se vuelve a escribir para atender las notas.
4. **Translate**: los traductores [Real language](../extensions/real-language-translator.md) y [Conlang](../extensions/conlang-translator.md) traducen sus parlamentos.
5. **Knowledge**: el [Knowledge Tracker](../extensions/knowledge-tracker.md) la lee.
6. **Audio**: el [Audiobook](../extensions/audiobook.md) la convierte en audio.

Solo se ejecutan los pasos que el libro tiene activados. Se ejecutan en el servidor, así que la página puede cerrarse, y forman una sola entrada en el [panel de las ejecuciones](#the-panel-of-the-runs): *After part 14: Annotate ✓ · Redraft … · Translate*, con el texto del modelo y un único **Stop**. Lo que cambia el texto va primero, para que el tracker y el audio reciban el texto final. Un paso que espera por ti (notas o una redacción que elegir) abre su ventana, y la entrada tiene **Review** para abrirla en otra página.

- **Stop**, cerrar una ventana que espera por ti, o un paso que falla lo termina ahí: lo hecho se queda, lo demás no se ejecuta y un mensaje indica qué queda por hacer a mano (cada paso tiene su propia acción sobre la parte).
- Mientras se ejecuta la **redacción**, la siguiente parte no se puede generar (se te dirá por qué ejecución); mientras se ejecutan los demás pasos, sí se puede.
- La parte no se puede borrar hasta que su texto sea definitivo; generarla de nuevo termina lo que siguió a su versión anterior.
- Un [agente](../agents.md) hace estos pasos mediante sus propios comandos: nada sigue a una parte que generó un agente.
<a id="merging-a-part-into-the-previous-one"></a>

## Fusionar una parte con la anterior

Un modelo que se detuvo a mitad de una frase (el límite de respuesta, una conexión cortada) no puede recibir de forma fiable la orden de "continuar" el mismo texto. Escribe en su lugar la siguiente parte (*continúa desde donde se detuvo la parte anterior*) y une las dos con **Merge into previous part** en el menú de la segunda. Se abre una revisión:

- **Join the texts with**: una línea vacía, un salto de línea, un espacio o nada. Viene preseleccionado según el final del texto anterior: un párrafo después de un texto que terminó una frase, un espacio después de uno que quedó cortado.
- La **scene setting**, el **POV character**, los **present characters** y las **instructions** de ambos turnos, combinados en campos que puedes editar. Lo que los dos turnos tienen en común al principio se conserva una sola vez ("Write the duel. Keep it short" y "Write the duel. Add rain" dan el primero, y "Add rain" en la línea siguiente).
- **Data**: lo que conservan las dos partes, valor por valor (por ejemplo, las [variables](../templates/macros.md) de la historia y los datos de las extensiones), indicando si el valor es igual, distinto o solo está en una de las partes. La parte fusionada conserva el valor de la **parte posterior** (es donde se encuentra ahora la historia), salvo que elijas para ella el valor de la parte anterior o lo dejes fuera. Los valores iguales están ocultos a menos que los pidas.

![La revisión antes de fusionar una parte con la anterior](../../images/part-merge-dialog.png)

La parte anterior conserva su lugar, su resumen y cómo se generó (prompt, modelo, tiempos); recibe el texto unido, el razonamiento unido y las imágenes, y sus palabras y tokens se vuelven a contar. La parte que fusionaste se elimina (queda en la [Papelera](../trash.md)) y lo que venía después continúa desde la parte anterior. Los resúmenes que abarcan las dos partes quedan desactualizados, como ocurre tras una edición. El [historial](version-history.md#backups-copies-and-the-trash) de la parte que se fusionó se elimina, y la parte superviviente tiene el texto unido como una versión **Merged**. La parte no se puede fusionar cuando es la primera del libro, o cuando la parte anterior tiene otras continuaciones (variantes de la parte): elimínalas o conviértelas primero en una rama.

<a id="images"></a>

## Imágenes

Una parte puede tener imágenes: un mapa, el retrato de un personaje, una ilustración. Elige **Images** en el menú de la parte para abrir la lista de imágenes de esa parte.

![El diálogo Images de una parte](../../images/images-dialog.png)

- **Add image** (o arrastrar un archivo sobre él) sube una imagen PNG, JPEG, GIF o WebP de hasta 10 MB. Las imágenes WebP se convierten a PNG al subirlas, porque los archivos de Word y PDF no pueden contener WebP.
- **Caption** se muestra debajo de la imagen. Puede estar vacío.
- **Position in the part** es una cuadrícula de 3x3. La columna es el lado del texto (izquierda, centro, derecha) y la fila es el lugar dentro de él: arriba es antes del texto, en medio es a mitad del texto, abajo es después. Haz clic en un cuadro para elegirlo.
- **Text flows around the image** hace que el texto fluya alrededor de la imagen en lugar de quedar encima y debajo. En la izquierda o la derecha, el texto corre junto a la imagen. En la columna central el texto queda a ambos lados: unos pocos bloques del texto (unos 400 caracteres, dos bloques como mínimo) en esa fila se convierten en dos columnas, la primera parte a la izquierda de la imagen y el resto a la derecha, de modo que el texto se lee por la columna izquierda y continúa en la derecha. Dos imágenes en el mismo lugar se colocan una encima de otra entre las columnas. Cuando el texto no tiene espacio para dos columnas (un solo párrafo, o los bloques ya ocupados por la franja de arriba), la imagen ocupa un bloque propio, centrado.
- **Width** es el ancho de la imagen como porcentaje del ancho del texto. **Automatic** mantiene el tamaño de la imagen (nunca más ancha que el texto; una imagen con texto a ambos lados ocupa el 30 %). Una imagen junto a la que fluye el texto ocupa como máximo el 60 %, y una con texto a ambos lados como máximo el 40 %, para que el texto conserve algo de espacio. La altura sigue las proporciones de la imagen.
- El botón de papelera quita la imagen de la parte.

![La posición de una imagen: en el centro, con el texto fluyendo a su alrededor](../../images/images-position.png)

**Preview as in the viewer** (un botón del diálogo, y la misma entrada en el menú de la parte cuando tiene imágenes) muestra la parte tal como la muestra el [visor](publishing.md), con el estilo de texto del libro: las imágenes donde las colocaste y un interruptor para pantalla ancha o estrecha (teléfono). Usa el texto que hay ahora en el editor, guardado o no.

![La vista previa: el texto en dos columnas con la imagen entre ellas](../../images/images-preview.png)

El editor de la historia no usa la posición: muestra las imágenes debajo del texto de la parte, como lista. La posición la usan el visor y las [exportaciones](export.md#images).

El lugar del texto donde va una imagen no depende de números guardados con ella: el texto se corta entre sus bloques (párrafos, listas, citas y bloques de código nunca se cortan), de modo que las imágenes siguen donde deben cuando editas el texto. El centro es el punto entre bloques más cercano al centro del texto; una imagen junto a la que fluye el texto en la parte inferior se coloca antes del último bloque, para que al menos ese bloque corra junto a ella. Las imágenes en el mismo lugar mantienen el orden de la lista; las que están una al lado de otra en el mismo lugar (y no dejan que el texto fluya alrededor) se colocan en fila cuando caben. PDF y Word hacen eso con las imágenes que tienen ancho.

Las imágenes **no forman parte del texto**. El modelo no las ve, no están en el prompt, no cuentan como tokens y no cambian los [resúmenes](summaries.md). Tampoco son Markdown: un enlace `![](...)` escrito en el texto no es una imagen de la parte; usa **Images** en su lugar.

Conviene saber:

- **Regenerate** conserva las imágenes de la parte, también cuando la generación falla. **Swipe** escribe una parte nueva (una versión nueva junto a la antigua), que empieza sin imágenes. **Branch Story** copia las imágenes a la nueva parte.
- Las imágenes pertenecen a tu cuenta. En el [visor](publishing.md), los lectores de un libro publicado también las ven, a menos que desmarques **Show the images to readers** en la pestaña *About* del libro.
- Una [copia de seguridad](backups.md) recuerda qué imágenes tiene una parte, pero no los archivos de imagen. Restaurar una copia en otra cuenta o en otra instalación deja las partes sin sus imágenes.
- Quitar una imagen de una parte solo la aparta de esa parte. El archivo se conserva y sigue listado en [Resources](../resources.md), donde puedes descargarlo, reemplazarlo o eliminarlo. Eliminarlo ahí lo quita de todas las partes que lo usan. El archivo en sí permanece en la carpeta de datos.

<a id="summaries"></a>

## Resúmenes

El botón **Summaries** de la barra inferior (a la izquierda del menú de ajustes ⚙) abre la vista general de los resúmenes de la rama, donde se pueden editar, eliminar y fusionar en metarresúmenes. La descripción del botón muestra cuántos tokens ocupan en el prompt los resúmenes en uso. Consulta [Resúmenes](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Cuánto del contexto ocupa la historia

El menú de ajustes (⚙) tiene **Branch Tokens**. Muestra, antes de generar nada, lo que ocupa la historia de la rama activa en el prompt: una barra para la historia **sin resúmenes** (todas las partes) y otra **con resúmenes** (los resúmenes y las partes posteriores a la más reciente), y una tabla con las partes, los tokens y la proporción del **presupuesto del prompt** (el contexto del modelo del libro menos el espacio reservado para la respuesta). Solo cuentan los resúmenes que todavía describen la historia; los que ya no coinciden se nombran y se descartan cuando se genera una parte. El lore, la plantilla del sistema y las instrucciones del turno ocupan espacio del mismo presupuesto. Sin un modelo con tamaño de contexto, la tabla no muestra proporciones.

![Branch Tokens](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Qué entradas de lore se usaron

**Lore Usage** en el menú de ajustes (⚙, **Analysis**) indica qué entradas del lorebook del libro usaron las generaciones y cuáles no usaron nunca,
para que un lorebook pueda depurarse según lo que hace y no a ojo. Cada generación registra qué entradas entraron en su prompt
(y los tokens de cada una); la ventana suma esos datos de las partes:

- **Parts** elige qué se cuenta: la **Active branch**, o **All branches**, cada rama y cada swipe del libro.
- **From part** y **To part** limitan el rango, numerado como en el esquema (la primera parte de una rama es 1; en todas las ramas el número es la profundidad de la parte en el árbol); déjalos vacíos para el principio y el final.
- **Show** conserva las entradas que estaban **Never used**, **Sometimes used** o **Always used** en las partes contadas.

![La ventana Lore Usage](../../images/lore-usage.png)

La tabla tiene una línea por cada entrada de los lorebooks del libro, la más usada primero: su lorebook, si nunca, a veces o siempre se usó, en cuántas partes (de las partes que tienen registro, con el porcentaje), los tokens que costó en todas ellas y por uso, y la primera y la última parte que la usó. Haz clic en un encabezado para ordenar. Una entrada que se usó antes pero que ya no está en los lorebooks del libro (borrada, movida o cuyo lorebook está desactivado o ya no es del libro) permanece en la lista marcada como *not in the lorebook now*; una borrada no tiene nombre.

Solo se cuentan las partes que tienen registro: una parte escrita a mano, escrita antes de que Marginalia guardara este registro, o escrita cuando el libro no tenía lorebook, no tiene ninguno, y la ventana indica cuántas son. Las entradas *Always used* podrían ser entradas constantes; las *Never used* son candidatas a eliminarse o a un filtro mejor. [**Preview activation**](../lorebooks/entries.md#preview-activation) en el editor de lorebook muestra por qué una entrada no se activa ahora.

<a id="text-styles"></a>

## Estilos de texto

El menú de ajustes (⚙) de la barra inferior tiene **Appearance → Change Styles For Text**, un menú con una entrada por estilo, ordenadas por nombre. Para saber cómo crear un estilo, consulta la [guía de estilos](styling-guide.md).
Un estilo decide cómo se ve el texto de las partes: tipografía, tamaño, colores, alineación, encabezados... La aplicación trae dos estilos que no se pueden cambiar: **Normal** (el aspecto de la aplicación) y **Book** (un estilo de lectura parecido a un libro, el que usa el [visor](publishing.md)), y puedes crear los tuyos en [Settings → Styles](../account.md#styles).

- La primera entrada, **Default (...)**, sigue tu *estilo por defecto* (el marcado en *Settings → Styles*, Normal si no elegiste ninguno). Un libro nuevo no tiene estilo propio y muestra ese.
- Elegir otra entrada guarda el estilo **en el libro**, de modo que reemplaza el valor por defecto solo para ese libro; la entrada marcada es la que tiene el libro ahora. **Default** anula esa elección.
- Un estilo que borras vuelve al valor por defecto en los libros que lo usaban.

![Appearance, Change Styles For Text: los estilos entre los que elegir](../../images/story-style-menu.png)

En el estilo **Book** los párrafos van justificados y las palabras largas se separan automáticamente con guiones, usando el diccionario del **Language** del libro (que se define en la [pestaña About](managing-books.md#the-about-tab)). La separación en sílabas la hace el navegador; en unos pocos idiomas poco comunes no hay diccionario y el texto simplemente no se separa.

Los libros que tenían activado el antiguo interruptor *Change Styles For Text* siguen mostrando el estilo Book hasta que elijas otra entrada.

<a id="highlighting-generated-text"></a>

## Resaltar el texto generado

El menú de ajustes (⚙) también tiene **Highlight Generated Text**, una opción marcable. Mientras está activada, las palabras que escribió un modelo se resaltan en todas las partes, para que lo que añadiste o reescribiste destaque. Marginalia sigue el texto a través de las [versiones de la parte](version-history.md): cada versión se superpone a la anterior, y las palabras que tienen en común conservan a quien las escribió, de modo que una frase que escribiste dentro de un texto generado sigue siendo tuya por muchas veces que el modelo reescriba el resto. Una parte que nunca editaste se resalta entera; una parte que escribiste tú no tiene nada resaltado. Lo que escribe un agente cuenta como del modelo. La opción no se guarda: al abrir de nuevo la página vuelve a estar desactivada.

Una parte cuyo [historial está desactivado](version-history.md) no tiene versiones que seguir: se compara con el texto que el modelo generó por primera vez. El resaltado también funciona en el [teléfono](../phone.md).

El mismo recuento da la proporción: la pestaña **Info** del libro tiene **Authorship of the branch** con un botón **Calculate**, que indica qué porcentaje de las palabras de la rama activa escribiste tú y qué porcentaje generó un modelo, y [la exportación](export.md) puede terminar el archivo con una declaración de ello.

![Texto generado resaltado y el menú de ajustes](../../images/highlight-generated.png)

<a id="commands"></a>

## Comandos

Debajo de la historia, junto al menú, hay una **command line**: escribe `/` para ver los comandos, elige uno y pulsa **Enter** (o el botón de reproducir) para ejecutarlo. Lo que devuelve un comando se muestra en una pequeña ventana encima de la línea. Consulta [Comandos](../commands/index.md) para saber cómo escribir argumentos, qué muestra la ayuda y cómo detener un comando que tarda. Por ahora, la línea de comandos solo está en la pestaña story del diseño de escritorio.
<a id="search-and-replace"></a>

## Buscar y reemplazar

El menú de ajustes (⚙) también tiene **Clear history of the book** y **Empty trash of the history**, véase [Version history](version-history.md#deleting-versions).

El menú de ajustes (⚙) también tiene **Search and replace**, que cambia una palabra, un nombre o una frase en toda la historia de una vez. La ventana tiene los campos **Search for** y **Replace with** y estas opciones:

![The settings menu with Search and replace](../../images/story-settings-menu.png)

![The Search and replace window with a search typed in](../../images/search-replace-dialog.png)

| Opción | Por defecto | Significado |
|---|---|---|
| **Search by regex** | desactivado | El texto de búsqueda es una [expresión regular](../regular-expressions.md) y el reemplazo puede usar sus grupos (`$1`). Si no, el texto se busca tal como se escribe. |
| **Case sensitive** | activado | Un texto solo se encuentra con las mayúsculas y minúsculas con que lo escribiste (*John* no encuentra *john*). Desactivado: no importan. Bloqueado mientras **Search by regex** está activado. |
| **Search in summaries** | activado | Los [resúmenes](summaries.md) también se buscan y se cambian. |
| **Search in all branches** | desactivado | Se buscan todas las [ramas](branches-and-story-tree.md); si no, solo la activa. |
| **Revalidate all summaries** | activado | Después del reemplazo, cada [resumen](summaries.md) del libro vuelve a ser válido; véase más abajo. |

Con una expresión regular, las mayúsculas y minúsculas se toman siempre tal como se escriben; empieza con `(?i)` para ignorarlas. Pulsa **Search** (el botón a la derecha de los campos; en el teléfono, debajo de ellos). La barra de progreso indica cuánto falta, y **Stop**, junto a ella, la interrumpe. Una búsqueda no cambia nada.

La tabla muestra cada parte de la historia y cada resumen que tiene coincidencias, con su tipo y su id. Abre una fila para ver las coincidencias que contiene: el texto alrededor de la coincidencia tal como está y tal como quedará después del reemplazo. Una coincidencia o un reemplazo demasiado largos para mostrarse aparecen como *[searched text]* o *[replace text]*.

![The matches of a search, the first part opened](../../images/search-replace-found.png)

Pulsar **Enter** en el campo **Search for** o **Replace with** hace lo mismo que **Search**.

Cuando la búsqueda termina, los campos y las opciones se bloquean y se activa el botón **Replace**. **Search again** olvida lo encontrado y desbloquea la ventana, para que puedas cambiar la búsqueda. **Replace** cambia la historia: la ventana queda completamente congelada (ni **Stop** funciona) hasta que termina, y luego vuelve a estar vacía. Las partes reemplazadas se marcan como editadas, como si las hubieras editado tú.

Un reemplazo en el texto que describe un [resumen](summaries.md#when-summaries-become-outdated) dejaría el resumen desactualizado.
Con **Revalidate all summaries** activado, Marginalia vuelve a calcular el valor de comprobación de cada resumen del libro (también los de todas las ramas, los que el reemplazo no tocó y los que ya estaban desactualizados), así que todos quedan válidos después. Esto también vuelve válido un resumen desactualizado sin mirar su texto, así que úsalo cuando sepas que los resúmenes siguen diciendo lo correcto.

Con la opción desactivada, solo se actualizan los resúmenes que cubre la búsqueda. Los que no cubre (resúmenes de ramas que no se buscan, que comparten sus partes anteriores con la rama activa, o todos ellos si los resúmenes no se buscan) quedan desactualizados. Si hay algunos así, Marginalia te dice cuántos son y pregunta si quieres continuar. Un resumen que ya estaba desactualizado sigue así.

![The warning about summaries that would become outdated](../../images/search-replace-warning.png)

En el [diseño para teléfono](../phone.md), la misma ventana tiene los campos y los botones uno debajo de otro, y la tabla tiene una sola columna que muestra el tipo y el id, y debajo de ellos la coincidencia y el reemplazo.

El [Resumen de resúmenes](summaries.md#search-and-replace-in-the-summaries) tiene la misma ventana, solo para los resúmenes.

La misma búsqueda y reemplazo se pueden ejecutar desde la [línea de comandos](../commands/index.md#commands-that-come-with-marginalia) con `/search-replace-parts`; pregunta antes de reemplazar.

<a id="importing-into-the-book"></a>

## Importar en el libro

El menú de ajustes (⚙) tiene **Import** (en **Import & export**). Es la ventana de la pestaña Books que crea un libro nuevo a partir de un archivo (véase [Managing books](managing-books.md#creating-a-book)), pero aquí se trata del libro en el que estás: las partes se **añaden a la historia**. El **Source** puede ser un archivo zip con ficheros `.md`, un documento (Word, LibreOffice, RTF, Markdown, texto plano, EPUB), el zip de una bóveda de Obsidian o el zip de un proyecto de Scrivener; cada uno tiene el mismo texto y las mismas opciones que allí (las palabras por parte y el nivel de título de los capítulos), y una línea indica lo que va a crear la importación. Los chats de SillyTavern y LM Studio no se ofrecen: son un árbol de ramas, que no tiene sitio en medio de una historia.

**Put the parts** indica dónde van:

- **At the end of the story**: después de la última parte de la rama activa (la primera parte de un libro vacío).
- **Before a part** o **After a part**: elige la parte de la lista (los números son los lugares de la rama activa, con el inicio del texto). Las partes se colocan entre dos partes: todo lo que seguía al lugar, con todas sus ramas, pasa detrás de las partes nuevas, y ninguna parte se cambia ni se vuelve a generar. El final de la historia se queda donde estaba (la historia es tantas partes más larga). Los resúmenes que cubren ese lugar quedan desactualizados, ya que describen la historia sin las partes nuevas; la ventana de los [resúmenes](summaries.md) los muestra y los vuelve a generar.

![The import window of a book](../images/book-import-into.png)

Las partes de un archivo son los ficheros en el orden de sus nombres, comparando los números dentro de los nombres como números (`2.md` antes que `10.md`), también en las carpetas del archivo. Se omiten las carpetas, los archivos ocultos (`.name`), la carpeta `__MACOSX` y los archivos que no son `.md`. El archivo es el texto de la parte tal cual, no se envía al modelo ni se cambia; todas las partes cuentan como escritas por ti.

La importación muestra una barra de progreso con el número de partes hechas. **Stop** la termina después de la parte que se está guardando: las partes que ya se añadieron se quedan en la historia (la importación no se deshace), y un mensaje indica cuántas se importaron.

<a id="after-an-import"></a>

### Después de una importación

Cuando las partes están en el libro, aparece una ventana **Import finished** que ofrece dos cosas que hacen más fácil escribir una historia larga (un libro recién importado como nuevo se abre primero, y la ventana aparece delante de él). Ambas también están en el menú de ajustes (⚙, **Analysis**), así que se pueden hacer más tarde o de nuevo:

- **Create summaries...** escribe los resúmenes de lo importado; véase [Create summaries](summaries.md#create-summaries). Hazlo primero: un agente bloquea el libro mientras trabaja.
- **Take lore notes with an agent...** inicia un [agente](../agents.md) que lee la historia y escribe los personajes, lugares, cosas y términos que encuentra como entradas del lorebook del libro; véase [Lore notes](../agents.md#lore-notes-of-an-imported-book).

**Not now** cierra la ventana y no hace ninguna de las dos cosas.

![The window after an import](../../images/import-finished.png)

La misma importación en un lorebook se describe en [Import & export](../lorebooks/import-export.md#importing-an-archive-of-text-files).

<a id="exporting-the-story"></a>

## Exportar la historia

El menú de ajustes (⚙) también tiene **Export story**, que guarda la rama activa como un archivo de texto, Markdown, HTML, Word, PDF o EPUB. Véase [Exporting a book](export.md).

<a id="what-the-model-gets"></a>

## Qué recibe el modelo

Para cada parte nueva, Marginalia envía:

1. La **master template** como mensaje de sistema: rol, entradas del [lorebook](../lorebooks/index.md), punto de vista, tiempo verbal, estilo y [resúmenes](summaries.md). El prompt de jailbreak del [proveedor de inferencia](../inference-providers.md#general-settings), si está activado, va primero.
2. Las partes anteriores de la rama activa como respuestas previas del propio modelo, todas las que quepan en el [límite de contexto](../protocols.md#limits), de la más nueva a la más antigua. Las partes más antiguas que no quepan se dejan fuera (salvo que un [resumen](summaries.md) las cubra).
3. El **user prompt** con las instrucciones para la nueva parte.

*Show Prompt* en cualquier parte muestra exactamente lo que se envió. Véase [Book prompts](prompts.md) para las plantillas.
