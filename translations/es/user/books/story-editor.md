---
label: Editor de la historia
order: 90
---

# Editor de la historia

La pestaña **Historia** de un libro es donde escribes. Muestra las partes de la [rama activa](branches-and-story-tree.md)
desde el principio hasta el final.

![El editor de la historia](../../images/hero-story-editor.png)

Esta página describe la distribución de escritorio. En el teléfono la historia es una sola columna y el esquema está en un cajón,
véase [Usar Marginalia en el teléfono](../phone.md#the-story).

| Área | |
|---|---|
| **Esquema** (izquierda) | Una entrada por parte, `#1`, `#2`... con el ID de la parte en la base de datos. Haz clic en una entrada para desplazarte a la parte (si todavía no está cargada, antes se cargan las partes anteriores). La cabecera indica cuántas partes tiene la rama. Una parte que tiene un [resumen](summaries.md) muestra un pequeño icono de documento después de su entrada. La extensión [Chapter Marker](../extensions/chapter-marker.md) muestra aquí los títulos de los capítulos. |
| **Lorebook** (izquierda, junto al Esquema) | Una vista del lore del libro, con un cuadro de búsqueda y una edición rápida del texto de una entrada, véase [La pestaña Lorebook](#the-lorebook-tab). |
| **Anotaciones** (izquierda, junto al Esquema) | Las [notas](annotations.md) sobre pasajes de la historia, para todo el libro. |
| **Planificador** (izquierda, junto al Esquema) | Las escenas que planeas escribir, una lista que rellenas a mano, véase [La pestaña Planificador](#the-planner-tab). |
| **Historia** (centro) | El texto de cada parte, con su menú (☰) y sus metadatos a la derecha, y debajo la Bottom bar. |
| **Bottom bar** | El botón **Resúmenes** (icono de lista) y el menú de ajustes (⚙) a la izquierda, la línea de comandos y el botón **+** a la derecha. |

Así se ve un libro nuevo. Todas estas son pestañas que puedes colocar como quieras, incluida la historia, véase
[Organizar las pestañas](#arranging-the-tabs). El separador entre la barra lateral y la historia se puede arrastrar, y Marginalia
recuerda dónde lo dejaste (se guarda con el libro) y vuelve a esa posición cuando abres el libro de nuevo.

La historia siempre se abre al final, en la parte más reciente. Un libro largo no se dibuja de una vez: solo se cargan las últimas partes,
y las anteriores se añaden arriba a medida que subes, sin mover la parte que estás leyendo. Las partes del otro extremo se descargan,
así que no quedan cargadas más partes que las que indica el ajuste **Partes cargadas a la vez en la historia** de tu [cuenta](../account.md)
(si está vacío, se cargan todas). Si te desplazas más rápido de lo que llegan las partes, una marca *Loading earlier parts* (o *later parts*)
en el borde indica que hay más. Hacer clic en una parte del esquema, o encontrar una con la búsqueda, carga las partes que la rodean, y empezar
una parte nueva te lleva de vuelta al final de la historia.

<a id="searching-the-story"></a>

### Buscar en la historia

Encima del esquema hay un campo de búsqueda para el texto de la **rama en la que estás** (la búsqueda del árbol de historia busca en todas las ramas).
Como un libro largo no se carga de una vez, la búsqueda del navegador (Ctrl+F) no encuentra texto en las partes que no están cargadas; esta sí.

- Escribe lo que buscas; la búsqueda empieza cuando dejas de escribir. Varias palabras deben estar todas en la misma parte, una frase va entre
  comillas dobles (`"old key"`) y `*` representa cualquier secuencia de caracteres (`light*house`). No importan mayúsculas y minúsculas.
- Cada lugar donde aparece una palabra es una coincidencia, y las coincidencias se numeran en toda la rama. Debajo del campo ves
  *Match 3 of 27, in part #5* y una barra con tu avance. La historia se desplaza a la coincidencia y carga las partes que la rodean
  si no están cargadas.
- Los botones **up** y **down** van a la coincidencia anterior y a la siguiente (después de la última vuelve a la primera). **Enter** en el campo
  hace lo mismo que el botón down, **Shift+Enter** lo mismo que el botón up.
- Todas las coincidencias de las partes cargadas se resaltan en el texto, y la coincidencia en la que estás aparece con un color más intenso.
  Borra el campo para quitar el resaltado.

En el teléfono la búsqueda está en el cajón del esquema; el cajón se cierra cuando muestra una coincidencia, así que tendrás que abrirlo de nuevo para ir a la siguiente.

<a id="the-lorebook-tab"></a>

### La pestaña Lorebook

La pestaña **Lorebook** de la barra lateral muestra el lore del lorebook del libro sin salir de la historia, así no tienes que cambiar de pestaña
para comprobar qué dice una entrada o para corregir una palabra en ella. Se vuelve a leer cada vez que regresas a la pestaña *Historia*, así que los
cambios que hiciste en la pestaña *Lorebook* del libro aparecen de inmediato.

- El **cuadro de búsqueda** de arriba muestra solo las entradas que tienen el texto en su nombre, contenido, nota o filtro (no importan mayúsculas y minúsculas).
- El **lápiz** de una entrada abre una pequeña ventana con su nombre, si está en uso, el filtro y cómo se busca, el contenido y la nota; **Guardar** los escribe
  (las etiquetas, el orden y los ajustes de activación se cambian en la pestaña *Lorebook* del libro). No se puede cambiar nada mientras un agente trabaja en el lorebook.
- El **botón de expandir** (⤢) abre el [editor de lorebook a pantalla completa](../lorebooks/lorebooks.md#editing-in-full-screen) (solo en escritorio): el que está junto al
  cuadro de búsqueda abre el lorebook, el de una entrada abre esa entrada, y el de un sublorebook (con *Incluir sublorebooks*) abre ese lorebook. La pestaña se vuelve
  a cargar cuando cierras el editor.

- Cada entrada es una fila con su nombre y el lápiz; la flecha la abre para mostrar su **contenido** (renderizado como Markdown) y el **texto que la activa**
  (el *Texto o patrón de filtrado*, con una nota cuando es una expresión regular).
- **Incluir sublorebooks** muestra el lorebook y cada uno de sus sublorebooks (y los de estos, a cualquier profundidad) como filas propias, con sus entradas debajo;
  haz clic en la flecha de un lorebook para ocultar sus entradas. Sin esta opción solo se listan las entradas del lorebook del libro.
- Las entradas y los lorebooks desactivados (que no se usan en la historia) se muestran atenuados.
- Un libro sin lorebook muestra una nota en su lugar.

![La pestaña Lorebook con dos entradas abiertas](../../images/story-lorebook-tab.png)

Como cualquier pestaña del editor, puede arrastrarse hacia un panel propio, véase más abajo.
<a id="the-planner-tab"></a>

### La pestaña del planificador

La pestaña **Planificador** es una lista de las escenas por las que quieres que avance la historia, en el orden que desees. Cada escena contiene los detalles de un turno que la escribe: un **título** para la lista, el **escenario de la escena**, el **personaje POV**, los **personajes presentes** y las **instrucciones**, los mismos cuatro campos que el formulario de nuevo turno.

![La pestaña Planificador con cuatro escenas, la primera ya escrita](../../images/planner-tab.png)

- **Añadir escena** abre un formulario para una escena nueva; se añade al final de la lista. El lápiz edita una escena, las flechas la suben o la bajan, el cubo de la basura la elimina.
- El **+** de la fila de una escena **inserta una escena nueva** justo debajo, entre esa escena y la siguiente.
- El botón de combinar de una escena la **combina con la siguiente**: se abre un formulario con los detalles de ambas ya unidos (los dos títulos, los escenarios y las instrucciones una debajo de otra, los personajes presentes de ambas y el personaje POV de la primera; si la segunda escena tiene otro, el formulario lo indica). Cambia lo que no encaje y pulsa **Combinar** (o <kbd>Ctrl/Cmd+Enter</kbd>): las dos escenas se convierten en una, en el lugar de la primera. Solo se hace cuando ambas están sin escribir.
- **Importar desde la rama** añade una escena por cada parte de la rama en la que está el libro (desde la primera parte hasta la más reciente) que tenga detalles de turno: su escenario, personaje POV, personajes presentes e instrucciones, usando la primera línea de las instrucciones como título. Las partes sin detalles de turno se omiten, y las escenas se colocan después de las que ya tienes, sin marcar como hechas. Úsalo para planificar la reescritura de una historia que ya has escrito: importa, combina los turnos que pertenecen juntos, inserta lo que falte y después escribe las escenas en una rama nueva o en una copia del libro.
- La casilla delante de una escena indica que está **hecha** (escrita): el título aparece tachado y la escena se atenúa. Nada lo comprueba por ti; marcala cuando hayas escrito la escena. La línea sobre la lista cuenta cuántas están hechas.
- **Haz clic en una escena** para pasar sus detalles al **formulario de nuevo turno** (el formulario se abre con el escenario, el POV, los personajes presentes y las instrucciones de la escena; lo que hubiera se reemplaza) y pulsa **Generar** cuando estés conforme. En el teléfono, el cajón se cierra primero.
- Las escenas se guardan con el libro, así que están en sus copias de seguridad y copias. Un modelo también puede añadir escenas: el [asistente de trama](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) de la extensión Arc Outline y un [agente](../agents.md) pueden leer y modificar el planificador mediante [comandos](../commands/index.md#the-planner); la lista muestra al instante lo que han añadido.

![El formulario para una escena nueva](../../images/planner-scene-dialog.png)

![Al hacer clic en una escena se rellena el formulario de nuevo turno con sus detalles](../../images/planner-new-turn.png)

Como cualquier pestaña del editor, puede arrastrarse fuera y colocarse en un panel propio; véase más abajo.

<a id="arranging-the-tabs"></a>

### Organizar las pestañas

El editor es un conjunto de pestañas: la **Historia** en sí (las partes y la barra inferior bajo ellas), el **Esquema**, la **Lorebook**, y las pestañas que añaden las [extensiones](../extensions/index.md) (Personajes, Arcos, Consulta lateral...). Arrastra una pestaña por su encabezado para reordenarlas (solo en escritorio; el teléfono mantiene una columna con el esquema en un cajón). Un libro nuevo tiene el Esquema y la Lorebook en un panel a la izquierda y la Historia en una columna propia a la derecha; puedes colocar la historia donde quieras, por ejemplo en el centro con el esquema a un lado y el lore al otro, o encima del esquema:

- Suéltala en una de las franjas discontinuas que aparecen encima, entre y debajo de los paneles de una columna para **separarla** en un panel propio dentro de esa columna; los paneles de una columna se apilan unos debajo de otros.
- Suéltala en una de las franjas altas a la izquierda, a la derecha o entre columnas para **separarla hacia un lado**: obtiene una columna propia, junto a las demás. Amplía el editor con los divisores para hacer sitio a más columnas.

Los paneles y las columnas están separados por divisores: arrástralos para dar a cada panel el espacio que necesite. Los tamaños se guardan con la disposición.

![La barra lateral con Personajes en una columna propia y Consulta lateral debajo](../../images/story-editor-tabs.png)

- Suéltala sobre el encabezado de otra pestaña o sobre el cuerpo de otro panel para **unirla** a ese panel (si la sueltas sobre una pestaña, se coloca antes de ella; si la sueltas sobre el cuerpo, queda al final). Un panel que pierde su última pestaña desaparece, y lo mismo ocurre con una columna sin paneles.

La disposición y los tamaños se guardan con el libro, así que son los mismos la próxima vez que lo abras y en cualquier navegador; la pestaña que muestra cada panel se recuerda junto con el resto de tu espacio de trabajo. Una pestaña de una extensión que no esté cargada conserva su lugar hasta que la extensión vuelva.

La historia siempre está en algún sitio: su pestaña no puede cerrarse, y una pestaña que comparte panel con otras queda oculta mientras se selecciona otra. Cualquier elemento que muestre una parte devuelve la historia a primer plano: al hacer clic en una entrada del esquema, en un resultado de la búsqueda, al elegir una rama en el [árbol de historia](branches-and-story-tree.md) o al iniciar una generación, se selecciona antes la pestaña **Historia**. Mientras una generación prepara su prompt (normalmente unos segundos), las demás pestañas están deshabilitadas; cuando el modelo empieza a escribir, vuelven a estar disponibles, véase [más abajo](#while-a-part-is-generated).

<a id="minimizing-tabs"></a>

### Minimizar pestañas

Cada pestaña, salvo la historia, tiene un pequeño botón **⌄** (flecha hacia abajo) junto a su nombre. Lo **minimiza**: la pestaña sale de su panel (un panel cuyas pestañas están todas minimizadas desaparece por el momento) y aparece un icono redondo de ella, un círculo de color con un dibujo (una lista para el esquema, un libro para la lorebook, una burbuja de diálogo para las anotaciones, una lupa para Consulta lateral, etc.; una pestaña de una extensión que no tenga dibujo recibe una o dos letras), en una barra en el borde del editor. Pasa el cursor sobre el icono para ver de qué pestaña se trata. Nada se cierra: la pestaña conserva lo que mostraba. Haz clic en el icono y la pestaña vuelve al panel, a su lugar entre las pestañas, y se muestra.

La barra está visible mientras haya alguna pestaña minimizada. El botón de su parte superior (las flechas circulares) envía la barra al siguiente lado del editor, en el sentido de las agujas del reloj: izquierda, arriba, derecha, abajo. La historia no puede minimizarse, así que siempre hay algo en pantalla.

![Pestañas minimizadas en la barra del borde del editor](../../images/story-minimized-tabs.png)

Los iconos están en orden alfabético por el nombre de las pestañas hasta que lo cambies: arrastra un icono sobre otro y ocupará el lugar de este. Qué pestañas están minimizadas, dónde está la barra y el orden de los iconos se guardan con el libro, junto con la disposición y los tamaños.

<a id="the-default-arrangement"></a>

### La disposición predeterminada

Un libro nuevo se abre con la disposición estándar (el esquema, la lorebook y las anotaciones a la izquierda, la historia a la derecha). Para hacer de tu propia disposición la estándar, coloca las pestañas como te gusten (paneles, tamaños, pestañas minimizadas, lado de la barra) y elige **Guardar la disposición como mi predeterminada** en el menú de ajustes (⚙) de la historia. Todo libro que no tenga una disposición propia (uno que no hayas reorganizado) se abrirá entonces con ella; un libro que hayas organizado conserva la suya. La predeterminada se guarda en tus ajustes de usuario.

Para volver a la disposición predeterminada, elige **Restaurar la disposición predeterminada** en el menú de ajustes (⚙) de la historia y confirma. Será la que guardaste, o la estándar si no guardaste ninguna. Solo devuelve las pestañas a su sitio (y olvida los tamaños y lo que está minimizado): tus textos, la pestaña que muestra cada panel y las pestañas de extensiones se mantienen, estas últimas en el primer panel.
<a id="writing-the-next-part"></a>

## Escribir la siguiente parte

1. Haz clic en **+** en la esquina inferior derecha. Se abre el panel *Instrucciones del nuevo turno*.
2. Rellena los campos. Todos son opcionales, pero el modelo necesita al menos algo de orientación:

   | Campo | |
   |---|---|
   | **Ambientación de la escena** | Dónde y cuándo transcurre la parte, p. ej. *El faro, una noche tormentosa de finales de otoño*. |
   | **Personaje del punto de vista** | Desde el punto de vista de quién se escribe la parte. |
   | **Personajes presentes** | Quién está en la escena, y en qué estado. |
   | **Instrucciones** | Qué debe ocurrir en esta parte. |

   *Ambientación de la escena*, *Personaje del punto de vista* y *Personajes presentes* se rellenan con los datos de la última parte, así que solo cambias lo que es distinto. *Instrucciones* empieza vacío cada vez.
3. Haz clic en **Generar**.

![El panel Instrucciones del nuevo turno](../../images/new-turn-instructions.png)

La nueva parte aparece al final y se va rellenando a medida que el modelo la escribe. En el caso de los [modelos de razonamiento](../inference-providers.md#reasoning-models), el razonamiento aparece primero, bajo **Ver el razonamiento**.

Mientras se genera una parte, el botón **+** se convierte en un botón rojo **Detener**. Al detenerla, se conserva el texto escrito hasta ese momento como parte.

<a id="while-a-part-is-generated"></a>

### Mientras se genera una parte

Durante los primeros segundos, mientras se monta el prompt a partir del libro, la ventana entera del libro espera. En cuanto el modelo empieza a escribir, solo quedan retenidas la nueva parte y las partes anteriores a ella:

- **La nueva parte** no se puede tocar hasta que termine: ni editar, ni menú.
- **Las partes anteriores** siguen en su sitio: puedes editarlas, anotarlas y redactarlas de nuevo, pero no borrarlas, fusionarlas ni ramificarlas, y la historia sigue en su rama (sin cambiar de versión ni de rama). Un cambio en una parte anterior o en el lore no llega a la parte que se está escribiendo, porque su prompt ya se había montado. Sí cuenta para lo que venga después.
- **Todo lo demás está libre**: el lore, los ajustes y el prompt del libro, las pestañas de la barra izquierda (consulta rápida, esquema del arco, notas...) y la barra de comandos. Una petición al modelo espera a una conexión libre del modelo si la generación tiene la única.
- **Esperando**: un segundo turno, buscar y reemplazar, una importación en el libro y la eliminación masiva. Las pestañas de árbol y de copias de seguridad también esperan.

Lo que la ventana atenúa también se rechaza cuando un comando, un agente o una extensión lo pide: Marginalia indica qué lo impide (*Esto está en uso: …*).

**Salir del libro detiene la generación.** El botón de salida pregunta antes; *Sí* equivale a **Detener**: lo que llegó se conserva. Cerrar la pestaña del navegador hace lo mismo, y el navegador pregunta antes de cerrar o recargar la pestaña mientras se genera una parte. Una pérdida breve de la red, o un portátil cerrado durante un minuto, no es salir: la página vuelve y muestra lo que llegó mientras tanto.

Cuando el modelo devuelve un error (servidor no alcanzable, clave de API incorrecta, contexto demasiado largo...), se muestra el error. Si todavía no llegó ningún texto, la historia queda como estaba: una parte nueva o una versión alternativa se elimina, y una parte regenerada recupera su texto anterior. El texto que llegó antes del error se conserva, igual que con **Detener**. Soluciona el problema y vuelve a generar.

Cómo entran los campos en el prompt lo determina el [Prompt de usuario](prompts.md#user-prompt) del libro. Con el prompt de usuario predeterminado, el modelo los recibe como *Ubicación y hora actuales*, *Personaje del punto de vista*, *Personajes presentes en escena / estado* y *Especificaciones de trama para la siguiente parte*.

!!!
El texto se escribe en Markdown: `*cursiva*`, `**negrita**`, `# Título` y demás se muestran con formato. Una parte con un título `#` es un capítulo para la extensión [Marcador de capítulos](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Redactar las instrucciones

¿No sabes qué debería pasar después? **Redactar instrucciones**, debajo del campo *Instrucciones* del panel, deja que el modelo escriba las instrucciones en tu lugar, como lo harías tú. Se abre una ventana con:

| | |
|---|---|
| **Orientación** | De qué debe tratar la parte: una idea general, una dirección ("ella lo confronta por la carta") o nada en absoluto, y el modelo continúa la historia hacia donde lleve. Empieza con lo que ya escribiste en *Instrucciones*. |
| **Plan de la historia** | Solo cuando algo lo proporciona. Lo hace el [Esquema del arco](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter), con los datos del capítulo actual. Solo lectura; el modelo lo sigue. |

**Redactar instrucciones** inicia el modelo y transmite la respuesta a **Instrucciones redactadas**, que puedes cambiar o vaciar antes de seguir; **Redactar de nuevo** lo intenta una vez más, y **Detener** conserva lo que haya llegado. No se cambia nada hasta que haces clic en **Usar como instrucciones**: el texto reemplaza entonces las *Instrucciones* del panel (los demás campos no se tocan) y el panel se vuelve a abrir, listo para **Generar**.

El modelo recibe la historia hasta ese momento (la parte más antigua como [resúmenes](summaries.md), igual que en una generación), la ambientación de la escena, el personaje del punto de vista y los personajes presentes que haya en el panel, el lore de la última parte, tus últimas instrucciones como ejemplos de cómo escribes, tu orientación y el plan. No ve las entradas de tu lorebook que activaría un nuevo turno. La petición es el [prompt de suplantación](prompts.md#impersonate-prompt). El modelo es el del libro; redactar consume tokens como cualquier petición.

![Redactar las instrucciones del siguiente turno](../../images/impersonate-dialog.png)

Lo mismo hace el comando [`/impersonate`](../commands/index.md), que devuelve el texto. Los [agentes](../agents.md) no lo reciben, ya que escriben la historia con instrucciones propias.

<a id="the-settings-menu"></a>

## El menú de ajustes

El menú de ajustes (⚙) de la barra inferior agrupa lo que actúa sobre la historia completa. **Buscar y reemplazar** está arriba; el resto está en submenús (también en el teléfono, salvo los elementos solo de escritorio):

| Submenú | Elementos |
|---|---|
| **Apariencia** | **Cambiar los estilos del texto**, **Resaltar el texto generado** y **Anotaciones en el margen** (solo en escritorio, el último) |
| **Análisis** | **Anotar un intervalo de partes con IA**, **Crear resúmenes**, **Tomar notas de lore con un agente**, **Anotación automática**, **Tokens de la rama**, **Uso del lore**, **Estadísticas** |
| **Importar y exportar** | **Importar**, **Exportar la historia** (y los elementos de las extensiones que importan o exportan, como **Exportar instrucciones**) |
| **Limpieza** | **Borrar el historial del libro**, **Vaciar la papelera del historial**, **Eliminación masiva** |
| **Disposición** (solo escritorio) | **Guardar la disposición como mi predeterminada**, **Restaurar la disposición predeterminada** |

Los elementos que traen las [extensiones](../extensions/index.md) están en el submenú que les corresponde, o al final del menú. Las teclas de los elementos aparecen en *Ajustes → Atajos de teclado*, donde un elemento se nombra con su submenú (*Análisis › Estadísticas*). Las secciones siguientes indican dónde está cada elemento.
<a id="the-part-menu"></a>

## El menú de la parte

Cada parte tiene un menú (☰) en la esquina superior derecha. **Editar**, **Descartar los cambios**, **Regenerar**, **Variante**, **Reescribir**, **Historial** e **Imágenes** están en el propio menú, **Eliminar** es su último elemento, y el resto está en cuatro submenús: **Anotaciones** (*Anotar la parte*, *Anotar con IA*), **Resumen** (*Generar resumen* / *Ver resumen*, *Eliminar resumen*), **Inspeccionar** (*Mostrar el prompt*, *Comparar prompt*, *Uso de tokens*, *Uso del lore*) y **Ramas y fusión** (*Crear una rama*, *Combinar con la parte anterior*). Un submenú sin nada para la parte no se muestra. El Revisor añade su submenú **Revisar** después de *Resumen*. Los elementos:

| Elemento | |
|---|---|
| **Editar** | Convierte el texto en un campo editable. **Guardar** (el mismo elemento del menú) lo guarda. Al iniciar una generación o al salir de la pestaña, las ediciones abiertas también se guardan. |
| **Descartar los cambios** | Solo mientras el texto está editado: cierra el campo sin guardar, la parte conserva el texto con el que se guardó por última vez. Si cambiaste el texto, se te pregunta antes. |
| **Regenerar** | Escribe de nuevo la última parte, reemplazando su texto. Solo en la última parte. |
| **Variante** | Escribe otra versión de la última parte y conserva la anterior como una rama separada. Solo en la última parte. Véase [Ramas y árbol de la historia](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Reescribir** | Permite que el modelo escriba esta parte de nuevo a partir de tus instrucciones, en cualquier parte; véase [más abajo](#redrafting-a-part). No mientras el texto está editado. |
| **Historial (n)** | Las [versiones del texto](version-history.md) de la parte, con el número de versiones: compararlas, restaurar una o ponerle nombre a una. No mientras el texto está editado. |
| **Anotar la parte** | Crea una [nota](annotations.md#making-an-annotation) sobre la parte completa. Para anotar palabras, selecciónalas y haz clic derecho (**Anotar la selección**). |
| **Anotar con IA** | Permite que un modelo [revise la parte](annotations.md#letting-a-model-annotate) y proponga notas. No mientras el texto está editado. Para varias partes a la vez usa **Anotar un intervalo de partes con IA** en el menú de ajustes (⚙), y para cada parte nueva **Anotación automática** (véase [Anotaciones](annotations.md#annotating-every-new-part-automatically)). |
| **Crear una rama** | Inicia una nueva rama desde esta parte, véase [Ramas y árbol de la historia](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Combinar con la parte anterior** | Añade el texto de esta parte a la parte anterior, véase [más abajo](#merging-a-part-into-the-previous-one). No está disponible en la primera parte. |
| **Mostrar el prompt** | Muestra el prompt exacto que se envió al modelo para esta parte. |
| **Uso de tokens** | Muestra cómo se usó el contexto del modelo en esta parte: un gráfico de las secciones del prompt (instrucciones del modelo, lore, resúmenes, historia hasta el momento, tus instrucciones, espacio reservado para la respuesta) y el tamaño de cada mensaje enviado al modelo. Si parte de la historia no cabía, indica cuántas partes se dejaron fuera. Las partes escritas antes de que esto se registrara no tienen gráfico. |
| **Uso del lore** | Enumera las entradas del [libro de lore](../lorebooks/index.md) que entraron en el prompt de esta parte, con los tokens que costó cada una y qué la activó (el prompt, o el contenido de otra entrada cuando el libro de lore analiza más a fondo). Las partes escritas a mano, las escritas antes de que esto se registrara o las escritas para un libro sin libro de lore indican que no se registró nada. Para el libro completo usa **Uso del lore** en el menú de ajustes (⚙), véase [más abajo](#which-lore-entries-were-used). |
| **Imágenes** | Adjunta imágenes a la parte, véase [Imágenes](#images). |
| **Generar resumen** / **Ver resumen** | Resume la historia hasta esta parte, o muestra el resumen; véase [Resúmenes](summaries.md). |
| **Eliminar resumen** | Quita el resumen de esta parte. Para un [metaresumen](summaries.md#meta-summaries) pregunta si deshacerlo o eliminarlo por completo. |
| **Eliminar** | Elimina la parte, véase [más abajo](#deleting-a-part). |

*Regenerar* y *Variante* usan los detalles del turno de la última parte (véase [más abajo](#turn-details)). Para cambiar lo que debe pasar, edítalos primero.

![El menú de una parte, con el submenú Inspeccionar abierto](../../images/story-part-menu.png)

![Uso de tokens de una parte](../../images/token-usage.png)

<a id="part-metadata"></a>

## Metadatos de la parte

Junto a cada parte:

| | |
|---|---|
| **Orden n \| ID en la base de datos: m** | La posición de la parte en la rama, y su ID en la base de datos. |
| **Modelo** | El proveedor de inferencia que escribió la parte. |
| **Tokens: x (y)** | `x` tokens de texto en la parte, `y` tokens en el prompt que la produjo. |
| **Palabras** | Palabras en la parte. |
| **Detalles del turno** | Las instrucciones desde las que se escribió la parte. |

<a id="turn-details"></a>

### Detalles del turno

**Detalles del turno** abre la *Ambientación de la escena*, el *Personaje del punto de vista*, los *Personajes presentes* y las *Instrucciones* desde las que se escribió la parte. En la última parte pueden cambiarse (los cambios se guardan de inmediato) y los usan *Regenerar* y *Variante*. En las partes anteriores son de solo lectura.

![Detalles del turno de una parte](../../images/turn-details.png)

<a id="editing-the-text"></a>

## Editar el texto

El texto generado es tuyo para cambiarlo. Elige **Editar** en el menú de la parte, cambia el texto y elige **Guardar**. Las partes editadas se envían al modelo tal como las editaste.

Editar una parte que cubre un [resumen](summaries.md) deja ese resumen desactualizado; Marginalia lo detecta en la siguiente generación y descarta el resumen. Para que esto no ocurra por sorpresa, **Guardar** revisa primero el nuevo texto: cuando dejaría resúmenes desactualizados, aparece una ventana que indica cuántos son y ofrece **Volver a la edición** (no se guarda nada, el editor queda abierto con tu texto), **Guardar de todos modos** (los resúmenes quedan desactualizados) y **Guardar y revalidar los resúmenes** (el texto se guarda y los resúmenes siguen válidos, para un cambio que no altera lo que dicen, como una errata). La comprobación se hace al usar **Guardar** (el menú, el atajo y el botón del editor); los guardados automáticos, por ejemplo al iniciar una generación con el editor abierto, no preguntan.

<a id="keyboard-shortcuts"></a>

## Atajos de teclado

En un ordenador (el [diseño para teléfono](../phone.md) no tiene) el ciclo de escritura funciona con el teclado:

| Tecla | |
|---|---|
| `+` | Abre el panel *Instrucciones del nuevo turno* con el cursor en *Instrucciones*. |
| `Ctrl+Enter` | Genera desde el panel abierto. `Esc` lo cierra; el teclado vuelve a la historia. |
| `Ctrl+.` | Detiene la generación en curso. |
| `s` / `r` | **Variante** / **Regenerar** la última parte. |
| `e` | Abre el editor de la parte más visible en la pantalla (la última parte si ninguna lo es). |
| `Ctrl+S` | Guarda la parte que se está editando (también mientras el cursor está en su texto). |
| `/` | Lleva el cursor a la barra de comandos (primero se muestra la pestaña de la historia). |
| `Ctrl+Shift+F` | Abre la pestaña *Esquema* y lleva el cursor a la búsqueda de la rama (`Enter` / `Shift+Enter` van a la siguiente / anterior coincidencia). |
| `Ctrl+Shift+H` | Abre la ventana *Buscar y reemplazar* del libro. |
| `a` | **Anotar la selección**: crea una nota sobre el texto seleccionado en una parte. |
| `Alt+AvPág` / `Alt+RePág` | Muestra la siguiente / anterior pestaña, recorriendo las pestañas de todas las hojas en el orden del diseño y volviendo al inicio al llegar al final. |

Una tecla sin `Ctrl`, `Alt` o `Meta` (como `+`, `s`, `r`, `e`, `a`, `/`) funciona solo cuando ningún campo de texto tiene el foco, para que nunca estorbe al escribir. Los atajos funcionan en la pestaña de la historia del libro abierto y no mientras hay otra ventana abierta encima. Los botones muestran su tecla en el texto emergente, y el menú de la última parte la muestra después de *Editar*, *Regenerar* y *Variante*. En un Mac, `Cmd` funciona donde una tecla dice `Ctrl` (salvo que hayas asignado esa tecla `Cmd` a otra acción). Puedes cambiar cada tecla en [Ajustes → Atajos de teclado](../account.md#keyboard-shortcuts).
<a id="deleting-a-part"></a>

## Eliminar una parte

**Eliminar** quita la parte de la historia. Las partes que la seguían se conservan y continúan desde la parte anterior a ella, así que
eliminar una parte en medio de la historia simplemente la saca. Si se elimina la última parte, la parte anterior pasa a ser el
nuevo final de la rama.

<a id="deleting-many-parts-at-once"></a>

### Eliminar varias partes a la vez

El menú de ajustes (⚙) tiene **Eliminación masiva**. Al activarla aparece una casilla a la izquierda de cada parte y se muestra una barra debajo
de la historia con el número de partes marcadas y un botón **Eliminar**. Marca las partes que quieras quitar (las marcas se mantienen
al desplazarte, ya que la historia carga solo una ventana de partes); **Mayús + clic** en una casilla marca (o desmarca) todas las partes
entre ella y la que habías pulsado antes; una parte marcada se muestra con un tono rojo. Después pulsa **Eliminar**: tras una confirmación, las partes
se eliminan una a una mientras se muestra una barra de progreso que no se puede detener. Se eliminan igual que una sola parte: las partes que seguían a una parte eliminada
continúan desde la parte anterior a ella, y las partes eliminadas van a la papelera. Después se vuelve a mostrar la historia y la
eliminación masiva se desactiva. **Cancelar** en la barra, o elegir **Eliminación masiva** de nuevo, la desactiva sin eliminar nada. Iniciar una generación también la desactiva. Una parte marcada que una ventana o una traducción esté usando se queda en la historia, y Marginalia indica qué lo está usando.

![Eliminación masiva con algunas partes marcadas](../../images/story-mass-delete.png)
<a id="redrafting-a-part"></a>

## Rediseñar una parte

**Regenerar** y **Variante** trabajan solo sobre la última parte y la escriben desde cero. **Reescribir** funciona con cualquier parte, también con una antigua en medio de la historia, y conserva lo que no pides cambiar. Elige **Reescribir** en el menú de la parte:

1. Escribe lo que debe cambiar en **Instrucciones para la reescritura**: *añade más diálogo entre los dos*, *alarga la pelea*, *elimina la explicación del mapa*, *mantén el final tal como está*. Si las instrucciones están vacías, la parte se vuelve a escribir con los mismos sucesos.
2. Elige **Extensión** si la parte debe quedar **Más corto** o **Más largo** (por defecto, **La misma extensión**): se añade una frase que lo pide después de tus instrucciones (véase [Prompt de reescritura más corta y más larga](prompts.md#redraft-shorter-and-longer)). Elige **Reescribir**. El texto se muestra en tiempo real en **Texto reescrito**; **Detener** lo termina antes. Elige **Reescribir de nuevo** para intentarlo otra vez, con otras instrucciones si quieres. **Diferencias** muestra qué ha cambiado respecto a la parte, palabra por palabra. Cuando la parte tiene [anotaciones](annotations.md) abiertas, **Atender las anotaciones abiertas de la parte** (marcada desde el principio) también se las entrega al modelo, y **Aplicar** las marca como resueltas.
3. El texto reescrito es un campo que aún puedes editar (su icono de esquina lo hace crecer para mostrar todo el texto), y **Diferencias** sigue tus cambios. **Aplicar** reemplaza el texto de la parte por él; **Cerrar** lo descarta. Nada del libro cambia antes de **Aplicar**.

![La ventana de reescritura: las instrucciones, el texto nuevo y las diferencias](../../images/redraft-dialog.png)

El modelo recibe los [prompts de reescritura](prompts.md#redraft-prompt): la parte, tus instrucciones, la historia anterior a la parte (la historia más antigua en forma de [resúmenes](summaries.md) donde hay resúmenes válidos, el resto como texto, empezando por la parte más reciente si no cabe todo), tanta historia posterior a la parte como quepa (hasta un cuarto del espacio disponible), el lore que los libros de lore dan a esa parte ahora (calculado de nuevo para el turno de la parte, de modo que una edición de una entrada llega a la reescritura; el lore con el que se escribió la parte solo se usa si el libro no tiene libro de lore), el punto de vista, el tiempo verbal y el estilo del libro, y el escenario, el personaje de punto de vista, los personajes presentes y las instrucciones del turno del que procede la parte. La historia posterior a la parte está para que el nuevo texto siga conduciendo a ella: los sucesos en los que se apoyan las partes siguientes deben mantenerse.

La parte conserva su lugar, su resumen, sus imágenes y la forma en que se generó; el texto, sus palabras y sus tokens se reemplazan, y la parte cuenta como editada. Aplicar la reescritura es una edición como **Guardar** en el editor, así que los [resúmenes](summaries.md) que describen el texto antiguo quedan desactualizados, y Marginalia indica cuántos. Un resumen de la propia parte es uno de ellos.

Cuando un resumen describe la parte (la parte tiene uno, o una parte más reciente de la rama lo tiene), la ventana muestra la casilla **Revalidar los resúmenes que esta reescritura deja desactualizados**, desmarcada al principio. Márcala cuando los resúmenes sigan diciendo lo correcto (la reescritura cambió la redacción, no lo que ocurre): entonces **Aplicar** vuelve a calcular su hash y siguen siendo válidos. Déjala desmarcada cuando hayan cambiado los sucesos, y vuelve a hacer los resúmenes.

La ventana no bloquea la historia; véase [Trabajar junto a las ventanas de reescritura y anotación](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Trabajar junto a las ventanas de reescritura y anotación

En un ordenador, las ventanas **Reescribir** y **Anotar con IA** se sitúan a la derecha de la historia y la dejan utilizable: puedes leer, desplazarte y editar la historia mientras el modelo escribe. Arrastra una ventana por su título para moverla, o por su borde para cambiar su tamaño. La flecha (⌄) del título pliega una ventana dentro del [panel de ejecuciones](#the-panel-of-the-runs), en la parte inferior izquierda de la página: el modelo sigue trabajando, **Abrir** (o un clic en la entrada) devuelve la ventana, y **Detener** detiene el modelo (cuando ha terminado, **Cerrar** cierra la ventana). En un teléfono las ventanas siguen tapando la historia, pero se pliegan de la misma manera.

**Cerrar y Detener son dos cosas distintas.** El **Detener** de la ventana detiene el modelo y la ventana queda con lo que ya llegó. **Cerrar** mientras el modelo trabaja no lo detiene: la ventana se pliega en el panel y el modelo sigue en su cola; su respuesta está en la ventana cuando la abres de nuevo. Cierra una ventana cuyo modelo ya terminó, o **Cerrar** en el panel, y desaparece; cerrar el libro o la pestaña del navegador también detiene una ventana plegada. Lo mismo vale para las otras ventanas que esperan a un modelo: un **resumen** (y *Crear resúmenes*, *Reescribir todos los resúmenes*), el [Revisor](../extensions/reviewer.md), la descripción del libro, los generadores de lore y de esquema, los generadores de imágenes y de manga, y el redactor de personajes de Lectores Beta. Los *Estudio* y *Convertir* del audiolibro tampoco bloquean la página; cerrarlos nunca detuvo la conversión.

Mientras una ventana está abierta (o plegada) sobre una parte, la parte muestra qué está haciendo junto a su menú (*Reescribiendo · El libro*), y **Regenerar**, **Variante**, **Reescribir**, **Anotar con IA**, **Fusionar con la anterior** y **Eliminar** están desactivados para esa parte, de modo que la ventana no pierde su parte. Editar la parte sigue siendo posible. Cerrar el libro cierra sus ventanas y detiene sus modelos. Una parte en la que trabaja un [traductor](../extensions/real-language-translator.md) muestra *Traduciendo el habla de la parte...* de la misma manera, y dos traductores nunca trabajan a la vez sobre una misma parte: el segundo indica que la parte está en uso.

- Cuando **Aplicas** una reescritura de la parte completa y la parte se cambió después de abrir la ventana (la editaste entretanto), Marginalia pregunta antes: **Diferencias** compara entonces la parte tal como está ahora con la reescritura, para que veas qué reemplazaría aplicarla; elige **Aplicar de todos modos** o **Cancelar**. Una reescritura que [se aplica sola](annotations.md#annotating-every-new-part-automatically) no hace esto cuando la parte se cambió: la ventana espera a que decidas tú.
- Cuando la parte se eliminó antes de que **Aplices** la reescritura o añadas las anotaciones, la ventana lo indica, no escribe nada y se cierra.

<a id="the-panel-of-the-runs"></a>

### El panel de ejecuciones

Todo lo que trabaja con un modelo sobre tus libros en ese momento aparece en la parte inferior izquierda de cada página: una generación, una reescritura o una anotación, una traducción del traductor [Real Language](../extensions/real-language-translator.md) o [Conlang](../extensions/conlang-translator.md), una reescritura de los resúmenes, la lectura de la historia por parte del Rastreador de conocimiento, una conversión de [Audiolibro](../extensions/audiobook.md), o un comando de la barra de comandos que tarda (*Redactar instrucciones*, un resumen...: su ventana se sitúa al lado y se pliega como las demás). Las ventanas en las que esperas una respuesta también aparecen mientras el modelo responde: **Suplantar**, el asistente de ayuda, una traducción rápida, el diseñador de conlang, un chat de Consulta lateral, Lectores Beta o el asistente de la trama. Lista tus ejecuciones de **todas tus páginas abiertas** (otra pestaña, el teléfono), no solo las iniciadas en esta página. Si no hay nada en marcha, no se muestra nada.

- **Cerrado**, es una barra pequeña: la primera ejecución, cuántas más hay (*Traduciendo el habla de la parte... · 2 más*) y una barra en movimiento mientras un modelo trabaja. Haz clic en ella para abrir la lista; se abre sola cuando pliegas una ventana en ella.
- **Abierto**, cada ejecución muestra lo que hace, el libro, cuánto tiempo lleva, lo que espera y el final de lo que escribe el modelo:
  - *Esperando al modelo: 2 peticiones antes que esta* cuando el modelo está ocupado con otras peticiones (véase *Conexiones* en [Proveedores de inferencia](../inference-providers.md));
  - *El modelo está pensando...* o *El modelo está escribiendo...*, con el final de su pensamiento o de su respuesta debajo;
  - *Te espera* cuando la ejecución espera una decisión tuya (una reescritura por aplicar, una propuesta del Rastreador de conocimiento por revisar, la pregunta del Inspector de prompts antes de una generación, un comando del asistente de la trama que hay que permitir).
- **Detener** detiene la ejecución, esté donde esté iniciada. **Abrir**, o un clic en la entrada, muestra de nuevo una ventana plegada (solo en la página en la que se abrió) o la propia ejecución; es **Revisar** cuando la ejecución te espera. Una ejecución del Rastreador de conocimiento abre su libro en esta página (cuando no hay otro libro abierto aquí; si no, se te pide que cierres ese primero), cambia a la pestaña Conocimiento y muestra la revisión. **Cerrar** cierra una ventana para siempre (y detiene su modelo).
- En un ordenador puedes mover el panel por su agarre (⋮ a su izquierda); se queda donde lo dejes en este navegador. En la mitad inferior de la página crece hacia arriba al abrirlo, como en su esquina, y siempre se mantiene dentro de la página. Cuando está donde se encuentra el botón redondo del [asistente de ayuda](../administration/help-assistant.md) (sobre un libro, ambos están en la esquina inferior izquierda), el botón se aparta y vuelve cuando el lugar queda libre.

Cuando una ejecución que iniciaste termina después de que se cerrara su página, otra página abierta tuya envía su [notificación](../account.md#notifications) (para las ejecuciones que la tienen, como la reescritura de los resúmenes).

<a id="redrafting-only-a-passage"></a>

### Reescribir solo un pasaje

Para reescribir unas frases y dejar el resto de la parte intacto, selecciónalas en el texto de la parte y haz **clic derecho** (una pulsación larga en un teléfono): el menú tiene **Reescribir la selección**. (El menú ☰ de la parte no se ofrece para esto, porque abrirlo puede perder la selección en algunos navegadores.) La ventana funciona como arriba con dos diferencias: muestra el **Texto seleccionado**, y la reescritura es solo el texto que lo reemplaza. El modelo recibe la parte completa con la selección marcada, y la historia y el lore alrededor, de modo que el nuevo pasaje se una a las frases anteriores y posteriores. **Diferencias** compara la selección con el texto reescrito; **Aplicar** pone el texto reescrito en el lugar de la selección y conserva el resto de la parte, incluido el espacio o el salto de línea en los bordes de la selección. Con [anotaciones](annotations.md) abiertas, la casilla ofrece solo las que se solapan con la selección. Si la parte se cambió después de que seleccionaras el texto (en otra ventana), **Aplicar** lo rechaza y tienes que volver a seleccionarlo. Mientras la parte está en edición se usa la selección del editor, y antes se guarda la edición.

<a id="what-follows-a-generated-part"></a>

### Lo que sigue a una parte generada

Cuando una parte que generaste (desde la página o la barra de comandos) está completa, el libro puede hacer más cosas con ella por sí mismo, en este orden:

1. **Anotar**: la [anotación automática](annotations.md#annotating-every-new-part-automatically) revisa la parte.
2. **Reescribir**: la parte se vuelve a escribir para atender las notas.
3. **Traducir**: los traductores [Real Language](../extensions/real-language-translator.md) y [Conlang](../extensions/conlang-translator.md) traducen sus discursos.
4. **Conocimiento**: el [Rastreador de conocimiento](../extensions/knowledge-tracker.md) la lee.
5. **Audio**: el [Audiolibro](../extensions/audiobook.md) la convierte en audio.

Solo se ejecutan los pasos que el libro tiene activados. Se ejecutan en el servidor, así que la página puede cerrarse, y forman una sola entrada en el [panel de ejecuciones](#the-panel-of-the-runs): *Tras la parte 14: Anotar ✓ · Reescribir … · Traducir*, con el texto del modelo y un único **Detener**. Lo que cambia el texto va primero, para que el rastreador y el audio reciban el texto final. Un paso que te espera (notas o una reescritura que elegir) abre su ventana, y la entrada tiene **Revisar** para abrirla en otra página.

- **Detener**, cerrar una ventana que te espera, o un paso que falla la termina ahí: lo hecho se conserva, lo demás no se ejecuta, y un mensaje indica lo que queda por hacer a mano (cada paso tiene su propia acción sobre la parte).
- Mientras la **reescritura** se ejecuta, la siguiente parte no se puede generar (se te dirá con qué ejecución); mientras se ejecutan los otros pasos, sí.
- La parte no se puede eliminar hasta que su texto sea definitivo; generarla de nuevo termina lo que seguía a su versión anterior.
- Un [agente](../agents.md) hace estos pasos con sus propios comandos: nada sigue a una parte que generó un agente.
<a id="merging-a-part-into-the-previous-one"></a>

## Fusionar una parte con la anterior

Un modelo que se detuvo en mitad de una frase (el límite de respuesta, una conexión cortada) no puede seguir de forma fiable
el mismo texto si se le pide que «continúe». Escribe la parte siguiente (*continúa donde terminó la última parte*) y une las dos
con **Combinar con la parte anterior** en el menú de la segunda. Se abre una revisión:

- **Unir los textos con**: una línea vacía, un salto de línea, un espacio o nada. Viene preseleccionado según el final del texto
  anterior: un párrafo tras un texto que terminó una frase, y un espacio tras uno que quedó cortado.
- La **posición de la escena**, el **personaje POV**, los **personajes presentes** y las **instrucciones** de ambos turnos, combinados en campos
  que puedes editar. Lo que los dos turnos tienen en común al principio se conserva una sola vez («Escribe el duelo. Que sea breve» y «Escribe el duelo. Añade lluvia» dan la primera, y «Añade lluvia» en la línea siguiente).
- **Datos**: lo que guardan las dos partes, valor por valor (por ejemplo, las [variables](../templates/macros.md) de la historia
  y los datos de las extensiones), indicando si el valor es el mismo, distinto o está solo en una de las partes. La parte fusionada
  conserva el valor de la **parte posterior** (es donde se encuentra ahora la historia), a menos que elijas para él el valor de la
  parte anterior o lo dejes fuera. Los valores iguales están ocultos salvo que los pidas.

![La revisión antes de fusionar una parte con la anterior](../../images/part-merge-dialog.png)

La parte anterior conserva su lugar, su resumen y cómo se generó (prompt, modelo, tiempos); recibe el texto unido, el razonamiento
unido y las imágenes, y sus palabras y tokens se vuelven a contar. La parte fusionada se elimina (queda en la [Papelera](../trash.md))
y lo que venía después continúa desde la parte anterior. Los resúmenes que abarcan las dos partes quedan desactualizados, como tras una edición. El [historial](version-history.md#backups-copies-and-the-trash) de la parte fusionada se elimina, y la parte que sobrevive tiene el texto unido como versión **Combinada**. No se puede fusionar una parte cuando es la primera del libro, ni cuando la parte anterior tiene otras continuaciones (variantes de la parte): elimínalas o conviértelas antes en una rama.

<a id="images"></a>

## Imágenes

Una parte puede tener imágenes: un mapa, el retrato de un personaje, una ilustración. Elige **Imágenes** en el menú de la parte para abrir
la lista de imágenes de la parte.

![El diálogo Imágenes de una parte](../../images/images-dialog.png)

- **Añadir imagen** (o arrastra un archivo sobre él) sube una imagen PNG, JPEG, GIF o WebP de hasta 10 MB. Las imágenes WebP se
  convierten a PNG al subirlas, porque los archivos de Word y PDF no pueden contener WebP.
- **Pie de imagen** se muestra bajo la imagen. Puede estar vacío.
- **Posición en la parte** es una cuadrícula de 3x3. La columna es el lado del texto (izquierda, centro, derecha), y la fila es el lugar dentro de él:
  arriba es antes del texto, el centro es en mitad del texto, abajo es después de él. Haz clic en un cuadrado para elegirlo.
- **El texto rodea la imagen** permite que el texto corra alrededor de la imagen en lugar de quedar encima y debajo. A la izquierda o a la derecha, el texto
  discurre junto a la imagen. En la columna central el texto queda a ambos lados: unos pocos bloques del texto (unos 400
  caracteres, dos bloques como mínimo) en esa fila se convierten en dos columnas, la primera parte a la izquierda de la imagen y el resto a la
  derecha, de modo que el texto se lee bajando por la columna izquierda y continúa en la derecha. Dos imágenes en el mismo lugar van una encima de otra
  entre las columnas. Cuando el texto no tiene espacio para dos columnas (un solo párrafo, o los bloques ya ocupados por la franja
  de arriba), la imagen ocupa un bloque propio, centrado.
- **Ancho** es el ancho de la imagen como porcentaje del ancho del texto. **Automático** mantiene el tamaño de la
  imagen (nunca más ancha que el texto; una imagen con texto a ambos lados ocupa el 30 %). Una imagen junto a la que corre el texto ocupa como máximo el 60 %,
  y una con texto a ambos lados como máximo el 40 %, para que el texto conserve algo de espacio.
  La altura sigue las proporciones de la imagen.
- El botón de papelera quita la imagen de la parte.

![La posición de una imagen: en el centro, con el texto rodeándola](../../images/images-position.png)

**Vista previa como en el visor** (un botón del diálogo, y la misma entrada en el menú de la parte cuando tiene imágenes) muestra la
parte tal como la muestra el [visor](publishing.md), con el estilo de texto del libro: las imágenes donde las hayas puesto,
y un interruptor para una pantalla ancha o estrecha (de teléfono). Usa el texto del editor tal como está ahora, guardado o no.

![La vista previa: el texto en dos columnas con la imagen entre ellas](../../images/images-preview.png)

El propio editor de la historia no usa la posición: muestra las imágenes bajo el texto de la parte, en forma de lista. La posición
la usan el visor y las [exportaciones](export.md#images).

El lugar del texto donde va una imagen no depende de números guardados con ella: el texto se corta entre sus bloques (párrafos,
listas, citas y bloques de código nunca se cortan), así que las imágenes siguen donde deben cuando editas el texto. El centro es el
lugar entre bloques más cercano al centro del texto; una imagen junto a la que corre el texto por abajo se coloca antes del
último bloque, para que al menos ese bloque discurra junto a ella. Las imágenes en el mismo lugar mantienen el orden de la lista; las que están una junto a otra en el mismo lugar (y no dejan que el texto corra alrededor) se colocan en fila cuando caben. PDF y Word hacen eso con las imágenes que tienen ancho.

Las imágenes **no forman parte del texto**. El modelo nunca las ve, no están en el prompt, no cuentan como tokens y no cambian los [resúmenes](summaries.md). Tampoco son Markdown: un enlace `![](...)` escrito en
el texto no es una imagen de la parte; usa **Imágenes** en su lugar.

Conviene saber:

- **Regenerar** conserva las imágenes de la parte, incluso cuando la generación falla. **Variante** escribe una parte nueva
  (una versión nueva junto a la anterior), que empieza sin imágenes. **Crear una rama** copia las imágenes a la parte nueva.
- Las imágenes pertenecen a tu cuenta. En el [visor](publishing.md), los lectores de un libro publicado también las ven, salvo que desmarques
  **Mostrar las imágenes a los lectores** en la pestaña *Información* del libro.
- Una [copia de seguridad](backups.md) recuerda qué imágenes tiene una parte, pero no los archivos de imagen. Restaurar una copia
  en otra cuenta o en otra instalación deja las partes sin sus imágenes.
- Quitar una imagen de una parte solo la separa de ella. El archivo sigue existiendo y aparece en
  [Recursos](../resources.md), donde puedes descargarlo, reemplazarlo o eliminarlo. Eliminarlo ahí lo quita de todas las
  partes que lo usan. El archivo en sí permanece en la carpeta de datos.

<a id="summaries"></a>

## Resúmenes

El botón **Resúmenes** de la barra inferior (a la izquierda del menú de ajustes ⚙) abre el resumen general de los resúmenes de la
rama, donde se pueden editar, eliminar y fusionar en metarresúmenes. La descripción del botón muestra cuántos
tokens ocupan en el prompt los resúmenes en uso. Consulta [Resúmenes](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Cuánto contexto ocupa la historia

El menú de ajustes (⚙) tiene **Tokens de la rama**. Muestra, antes de generar nada, lo que la historia de la rama activa
ocupa en el prompt: una barra para la historia **sin resúmenes** (todas las partes) y **con resúmenes** (los resúmenes
y las partes posteriores al más reciente), y una tabla con las partes, los tokens y la proporción del **presupuesto del prompt**
(el contexto del modelo del libro menos el espacio reservado para la respuesta). Solo cuentan los resúmenes que aún describen la historia;
los que ya no coinciden se nombran, y se descartan al generar una parte. El lore, la plantilla del sistema y las instrucciones del turno ocupan espacio
del mismo presupuesto. Sin un modelo con tamaño de contexto, la tabla no muestra proporciones.

![Tokens de la rama](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Qué entradas del lore se usaron

**Uso del lore** en el menú de ajustes (⚙, **Análisis**) indica qué entradas del lorebook del libro usaron las generaciones y cuáles no se usaron nunca,
para que un lorebook se pode según lo que hace y no por suposición. Cada generación registra qué entradas entraron en su prompt
(y los tokens de cada una); la ventana suma esos datos en las partes:

- **Partes** elige lo que se cuenta: la **Rama activa**, o **Todas las ramas**, todas las ramas y todas las variantes del libro.
- **Desde la parte** y **Hasta la parte** limitan el rango, numerado como en el esquema (la primera parte de una rama es la 1; en todas las ramas el número es
  la profundidad de la parte en el árbol); déjalos vacíos para el inicio y el final.
- **Mostrar** conserva las entradas que estaban **Nunca usada**, **A veces usada** o **Siempre usada** en las partes contadas.

![La ventana de Uso del lore](../../images/lore-usage.png)

La tabla tiene una línea por cada entrada de los lorebooks del libro, de más usada a menos: su lorebook, si se usó nunca, a veces o siempre,
en cuántas partes (de las partes que tienen registro, con el porcentaje), los tokens que costó en todas ellas y por uso, y la primera y la
última parte que la usaron. Haz clic en un encabezado para ordenar. Una entrada que se usó antes pero que ya no está en los lorebooks del libro (borrada, movida, o
su lorebook está desactivado o ya no es del libro) aparece en la lista marcada como *ya no está en el lorebook*; una borrada no tiene nombre.

Solo se cuentan las partes que tienen registro: una parte escrita a mano, escrita antes de que Marginalia guardara este registro, o escrita cuando el libro no
tenía lorebook no tiene ninguno, y la ventana indica cuántas son. Las entradas *Siempre usada* podrían ser entradas constantes; las *Nunca usada* son candidatas a eliminarse o
a un filtro mejor. [**Vista previa de la activación**](../lorebooks/entries.md#preview-activation) en el editor de lorebooks muestra por qué una entrada no se activa ahora.

<a id="text-styles"></a>

## Estilos de texto

El menú de ajustes (⚙) de la barra inferior tiene **Apariencia → Cambiar los estilos del texto**, un menú con una entrada por estilo, ordenadas por nombre.
Para saber cómo crear un estilo, consulta la [guía de estilos](styling-guide.md).
Un estilo decide cómo se ven los textos de las partes: tipografía, tamaño, colores, alineación, encabezados... La aplicación trae dos
estilos que no se pueden cambiar: **Normal** (el aspecto de la aplicación) y **Book** (un estilo de lectura parecido a un libro, el que usa el
[visor](publishing.md)), y puedes crear los tuyos en [Ajustes → Estilos](../account.md#styles).

- La primera entrada, **Default (...)**, sigue tu *estilo predeterminado* (el marcado en *Ajustes → Estilos*; Normal si no elegiste ninguno). Un libro nuevo no tiene estilo propio y muestra ese.
- Elegir otra entrada guarda el estilo **en el libro**, de modo que reemplaza al predeterminado solo para ese libro; la entrada marcada es
  la que tiene ahora el libro. **Default** deshace la elección.
- Un estilo que borras pasa a ser el predeterminado en los libros que lo usaban.

![Apariencia, Cambiar los estilos del texto: los estilos para elegir](../../images/story-style-menu.png)

En el estilo **Book** los párrafos van justificados y las palabras largas se separan automáticamente con guiones, usando el diccionario del
**Idioma** del libro (que se configura en la [pestaña Acerca de](managing-books.md#the-about-tab)). La separación con guiones la hace el navegador;
en unos pocos idiomas poco comunes no hay diccionario y el texto simplemente no se separa.

Los libros que tenían activado el antiguo interruptor de *Cambiar los estilos del texto* siguen mostrando el estilo Book hasta que elijas otra entrada.

<a id="highlighting-generated-text"></a>

## Resaltar el texto generado

El menú de ajustes (⚙) también tiene **Resaltar el texto generado**, una opción que se puede marcar. Mientras está activada, las palabras que escribió un modelo
se resaltan en todas las partes, para que se note lo que añadiste o reescribiste. Marginalia sigue el texto a través de las
[versiones de la parte](version-history.md): cada versión se superpone a la anterior, y las palabras que tienen en común conservan
a quien las escribió, así que una frase que escribiste dentro de un texto generado sigue siendo tuya aunque el modelo reescriba el resto muchas veces. Una parte
que nunca editaste se resalta entera; una parte que escribiste tú no tiene nada resaltado. Lo que escribe un agente cuenta como del
modelo. La opción no se guarda: al abrir de nuevo la página vuelve a estar desactivada.

Una parte cuyo [historial está desactivado](version-history.md) no tiene versiones que seguir: se compara con el texto que generó
el modelo primero. El resaltado también funciona en el [teléfono](../phone.md).

El mismo recuento da la proporción: la pestaña **Info** del libro tiene **Autoría de la rama** con un botón **Calcular**, que indica
qué porcentaje de las palabras de la rama activa escribiste tú y cuál generó un modelo, y
[la exportación](export.md) puede terminar el archivo con una declaración de ello.

![Texto generado resaltado y el menú de ajustes](../../images/highlight-generated.png)

<a id="commands"></a>

## Comandos

Junto al menú debajo de la historia hay una **línea de comandos**: escribe `/` para ver los comandos, elige uno y pulsa **Enter**
(o el botón de reproducir) para ejecutarlo. Lo que devuelve un comando se muestra en una pequeña ventana encima de la línea. Consulta
[Comandos](../commands/index.md) para saber cómo escribir argumentos, qué muestra la ayuda y cómo detener un comando que tarda
mucho. La línea de comandos solo está, por ahora, en la pestaña de la historia del diseño de escritorio.
<a id="search-and-replace"></a>

## Buscar y reemplazar

El menú de ajustes (⚙) también tiene **Borrar el historial del libro** y **Vaciar la papelera del historial**, ver [Versiones del historial](version-history.md#deleting-versions).

El menú de ajustes (⚙) también tiene **Buscar y reemplazar**, que cambia una palabra, un nombre o una frase en toda la historia de
una vez. La ventana tiene los campos **Buscar** y **Reemplazar por** y estas opciones:

![El menú de ajustes con Buscar y reemplazar](../../images/story-settings-menu.png)

![La ventana Buscar y reemplazar con una búsqueda escrita](../../images/search-replace-dialog.png)

| Opción | Por defecto | Significado |
|---|---|---|
| **Buscar con expresión regular** | desactivada | El texto de búsqueda es una [expresión regular](../regular-expressions.md) y el reemplazo puede usar sus grupos (`$1`). Si no, el texto se busca tal como se escribe. |
| **Distinguir mayúsculas y minúsculas** | activada | Un texto solo se encuentra con la capitalización que escribiste (*John* no encuentra *john*). Desactivada: la capitalización no importa. Bloqueada mientras **Buscar con expresión regular** está activada. |
| **Buscar en los resúmenes** | activada | Los [resúmenes](summaries.md) también se buscan y se cambian. |
| **Buscar en todas las ramas** | desactivada | Se buscan todas las [ramas](branches-and-story-tree.md); si no, solo la activa. |
| **Revalidar todos los resúmenes** | activada | Después del reemplazo, todos los [resúmenes](summaries.md) del libro vuelven a ser válidos, ver más abajo. |

Con una expresión regular, la capitalización siempre se toma tal como se escribe; empieza con `(?i)` para ignorarla. Pulsa **Buscar**
(el botón a la derecha de los campos; en el teléfono, debajo de ellos). La barra de progreso muestra cuánto falta y **Detener**
junto a ella la interrumpe. Una búsqueda no cambia nada.

La tabla enumera cada parte de la historia y cada resumen que tiene alguna coincidencia, con su tipo e id. Abre una fila para ver las
coincidencias en ella: el texto alrededor de la coincidencia tal como está y tal como quedará después del reemplazo. Una coincidencia o un reemplazo
demasiado largo para mostrarse aparece como *[texto buscado]* o *[texto de reemplazo]*.

![Las coincidencias de una búsqueda, con la primera parte abierta](../../images/search-replace-found.png)

Pulsar **Enter** en el campo **Buscar** o **Reemplazar por** hace lo mismo que **Buscar**.

Cuando la búsqueda termina, los campos y opciones se bloquean y el botón **Reemplazar** se activa. **Buscar de nuevo** olvida
lo encontrado y desbloquea la ventana, para que puedas cambiar la búsqueda. **Reemplazar** cambia la historia: la ventana queda congelada
por completo (ni **Detener** funciona) hasta que termina, y luego vuelve a estar vacía. Las partes reemplazadas se marcan como
editadas, como si las hubieras editado tú mismo.

Un reemplazo en el texto que describe un [resumen](summaries.md#when-summaries-become-outdated) dejaría el resumen desactualizado.
Con **Revalidar todos los resúmenes** activada, Marginalia vuelve a calcular el valor de comprobación de cada resumen del libro (los de
todas las ramas, también los que el reemplazo no tocó, y los que ya estaban desactualizados), de modo que todos son válidos
después. Eso también vuelve válido un resumen desactualizado sin mirar su texto, así que úsalo cuando sepas que los resúmenes
siguen diciendo lo correcto.

Con la opción desactivada, solo se actualizan los resúmenes que cubre la búsqueda. Los que no cubre (resúmenes de
ramas que no se buscan, que comparten sus partes anteriores con la rama activa; o todos, si los resúmenes no se buscan) quedan
desactualizados. Si hay tales resúmenes, Marginalia te dice cuántos son y pregunta si seguir. Un
resumen que ya estaba desactualizado sigue así.

![La advertencia sobre resúmenes que quedarían desactualizados](../../images/search-replace-warning.png)

En el [diseño para teléfono](../phone.md) la misma ventana tiene los campos y botones uno debajo de otro, y la tabla tiene una
columna que muestra el tipo y el id, y debajo de ellos la coincidencia y el reemplazo.

La [vista general de resúmenes](summaries.md#search-and-replace-in-the-summaries) tiene la misma ventana, solo para los resúmenes.

La misma búsqueda y reemplazo puede ejecutarse desde la [línea de comandos](../commands/index.md#commands-that-come-with-marginalia)
con `/search-replace-parts`; pregunta antes de reemplazar.

<a id="importing-into-the-book"></a>

## Importar en el libro

El menú de ajustes (⚙) tiene **Importar** (en **Importar y exportar**). Es la ventana de la pestaña Libros que crea un libro nuevo a partir de un archivo (ver [Gestionar libros](managing-books.md#creating-a-book)), pero para el libro en el que estás: las partes se **añaden a la historia** en su lugar. El **Origen** es uno de: el archivo zip de archivos `.md`, un documento (Word, LibreOffice, RTF, Markdown, texto plano, EPUB), el zip de una bóveda de Obsidian o el zip de un proyecto de Scrivener; cada uno tiene el mismo texto y las mismas opciones que allí (las palabras por parte y el nivel de encabezado de los capítulos), y una línea indica qué creará la importación. Los chats de SillyTavern y LM Studio no se ofrecen: son un árbol de ramas, que no tiene cabida en medio de una historia.

**Colocar las partes** indica dónde van:

- **Al final de la historia**: después de la última parte de la rama activa (la primera parte de un libro vacío).
- **Antes de una parte** o **Después de una parte**: elige la parte de la lista (los números son los lugares en la rama activa, con el
  inicio del texto). Las partes se colocan entre dos partes: todo lo que seguía al lugar, con todas sus ramas, pasa detrás de las nuevas
  partes, y ninguna parte se cambia ni se vuelve a generar. El final de la historia se queda donde estaba (la historia tiene tantas partes más). 
  Los resúmenes que cubren el lugar quedan desactualizados, pues describen la historia sin las nuevas partes: la ventana de [resúmenes](summaries.md)
  los muestra y los vuelve a generar.

![La ventana de importación de un libro](../images/book-import-into.png)

Las partes de un archivo son los archivos en el orden de sus nombres, comparando como números los números dentro de los nombres (`2.md`
antes que `10.md`), también en carpetas del archivo. Se omiten las carpetas, los archivos ocultos (`.nombre`), la carpeta `__MACOSX` y los archivos que no son
`.md`. El archivo es el texto de la parte tal cual, no se envía al modelo ni se cambia; todas las partes cuentan como escritas por ti.

La importación muestra una barra de progreso con el número de partes hechas. **Detener** la termina después de la parte que se está guardando: las partes
que ya se añadieron se quedan en la historia (la importación no se deshace), y un mensaje indica cuántas se importaron.

<a id="after-an-import"></a>

### Después de una importación

Cuando las partes están en el libro, una ventana **Importación terminada** ofrece las dos cosas que hacen más fácil escribir una historia larga (un libro recién
importado como nuevo se abre primero, y la ventana aparece delante de él). Ambas también están en el menú de ajustes (⚙, **Análisis**), así que pueden hacerse
más tarde o de nuevo:

- **Crear resúmenes...** escribe los resúmenes de lo importado, ver [Crear resúmenes](summaries.md#create-summaries). Hazlo primero: un agente
  bloquea el libro mientras trabaja.
- **Tomar notas de lore con un agente...** inicia un [agente](../agents.md) que lee la historia y escribe los personajes, lugares, cosas y
  términos que encuentra como entradas del lorebook del libro, ver [Notas de lore](../agents.md#lore-notes-of-an-imported-book).

**Ahora no** cierra la ventana y no hace ninguna de las dos cosas.

![La ventana después de una importación](../../images/import-finished.png)

La misma importación en un lorebook se describe en [Importar y exportar](../lorebooks/import-export.md#importing-an-archive-of-text-files).

<a id="exporting-the-story"></a>

## Exportar la historia

El menú de ajustes (⚙) también tiene **Exportar la historia**, que guarda la rama activa como un archivo de texto, Markdown, HTML, Word, PDF o EPUB.
Ver [Exportar un libro](export.md).

<a id="what-the-model-gets"></a>

## Lo que recibe el modelo

Para cada parte nueva, Marginalia envía:

1. La **plantilla maestra** como mensaje de sistema: rol, [lorebook](../lorebooks/index.md) entradas, punto de vista, tiempo verbal, estilo y [resúmenes](summaries.md). El prompt de jailbreak del
   [proveedor de inferencia](../inference-providers.md#general-settings), si está activado, va primero.
2. Las partes anteriores de la rama activa como respuestas previas del propio modelo, tantas como quepan en el
   [límite de contexto](../protocols.md#limits), de la más reciente a la más antigua. Las partes más antiguas que no quepan se dejan fuera (a menos que un
   [resumen](summaries.md) las cubra).
3. El **prompt de usuario** con las instrucciones para la nueva parte.

*Mostrar el prompt* en cualquier parte muestra exactamente lo que se envió. Ver [Prompts del libro](prompts.md) para las plantillas.
