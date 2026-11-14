---
label: Editor de la historia
order: 90
---

# Editor de la historia

La pestaña **Historia** de un libro es donde escribes. Muestra las partes de la [rama activa](branches-and-story-tree.md)
desde el principio hasta el final.

![El editor de la historia](../../images/hero-story-editor.png)

Esta página describe la disposición para el escritorio. En el teléfono la historia es una sola columna y el esquema va en un cajón; consulta
[Usar Marginalia en el teléfono](../phone.md#the-story).

| Área | |
|---|---|
| **Esquema** (izquierda) | Una entrada por parte, `#1`, `#2`... con el ID de la parte en la base de datos. Haz clic en una entrada para desplazarte a la parte (si aún no está cargada, primero se cargan las partes anteriores). La cabecera indica cuántas partes tiene la rama. Una parte que tiene un [resumen](summaries.md) muestra un pequeño icono de documento después de su entrada. La extensión [Chapter Marker](../extensions/chapter-marker.md) muestra aquí los títulos de los capítulos. |
| **Lorebook** (izquierda, junto al Esquema) | Una vista del lore del libro, con un cuadro de búsqueda y una edición rápida del texto de una entrada; consulta [La pestaña del lorebook](#the-lorebook-tab). |
| **Anotaciones** (izquierda, junto al Esquema) | Las [notas](annotations.md) sobre pasajes de la historia, para todo el libro. |
| **Planificador** (izquierda, junto al Esquema) | Las escenas que planeas escribir, una lista que rellenas a mano; consulta [La pestaña del planificador](#the-planner-tab). |
| **Historia** (centro) | El texto de cada parte, con su menú (☰) y sus metadatos a la derecha, y debajo la barra inferior. |
| **Barra inferior** | El botón **Resúmenes** (icono de lista) y el menú de ajustes (⚙) a la izquierda, la línea de comandos, y el botón **+** a la derecha. |

Así se ve un libro nuevo. Todas estas son pestañas que puedes organizar como quieras, la historia incluida; consulta
[Organizar las pestañas](#arranging-the-tabs). El separador entre la barra lateral y la historia se puede arrastrar, y Marginalia
recuerda dónde lo dejaste (se guarda con el libro) y vuelve a esa posición cuando abres el libro de nuevo.

La historia siempre se abre al final, en la parte más reciente. Un libro largo no se dibuja de golpe: solo se cargan las últimas partes,
y las anteriores se añaden arriba a medida que subes, sin mover la parte que estás leyendo. Las partes del otro extremo se descargan,
de modo que no permanecen cargadas más partes que las que indica el ajuste **Partes cargadas a la vez en la historia** de tu
[cuenta](../account.md) (si está vacío, se cargan todas). Si subes más deprisa de lo que llegan las partes, una marca *Cargando partes anteriores* (o *posteriores*) en el borde indica que hay más. Hacer clic en una parte del esquema, o encontrar una con la búsqueda, carga las partes que la rodean, y empezar una parte nueva te devuelve al final de la historia.

<a id="searching-the-story"></a>

### Buscar en la historia

Encima del esquema hay un campo de búsqueda para el texto de la **rama en la que estás** (la búsqueda del árbol de la historia
busca en todas las ramas). Como un libro largo no se carga de golpe, la búsqueda del navegador (Ctrl+F) no puede encontrar
texto en las partes que no están cargadas; esta sí.

- Escribe lo que buscas; la búsqueda empieza cuando dejas de escribir. Varias palabras deben estar todas en la misma parte; una frase
  va entre comillas dobles (`"old key"`), y `*` representa cualquier conjunto de caracteres (`light*house`). No importan las mayúsculas.
- Cada lugar donde aparece una palabra es una coincidencia, y se numeran a lo largo de toda la rama. Debajo del campo ves
  *Coincidencia 3 de 27, en la parte #5* y una barra con tu progreso. La historia se desplaza a la coincidencia y carga las partes que la rodean
  si no están cargadas.
- Los botones **arriba** y **abajo** van a la coincidencia anterior y a la siguiente (después de la última vuelve a la primera).
  **Enter** en el campo hace lo mismo que el botón de abajo, y **Shift+Enter** lo mismo que el de arriba.
- Todas las coincidencias de las partes cargadas se resaltan en el texto; la coincidencia en la que estás aparece con un color más intenso. Borra el
  campo para quitar el resaltado.

En el teléfono la búsqueda está en el cajón del esquema; el cajón se cierra cuando muestra una coincidencia, así que ábrelo de nuevo para la siguiente.

<a id="the-lorebook-tab"></a>

### La pestaña del lorebook

La pestaña **Lorebook** de la barra lateral muestra el lore del lorebook del libro sin salir de la historia, de modo que no tienes que cambiar de pestaña para comprobar qué dice una entrada o para corregir una palabra en ella. Se vuelve a leer cada vez que regresas a la pestaña *Historia*, así que los cambios que hiciste en la pestaña *Lorebook* del libro se ven al instante.

- El **cuadro de búsqueda** de la parte superior muestra solo las entradas que tienen el texto en su nombre, contenido, nota o filtro (no importan las mayúsculas).
- El **lápiz** de una entrada abre una ventana pequeña con su nombre, si está en uso, el filtro y la forma en que se busca, el contenido
  y la nota; **Guardar** los escribe (las etiquetas, el orden y los ajustes de activación se cambian en la pestaña *Lorebook* del libro). No se puede
  cambiar nada mientras un agente trabaja en el lorebook.
- El **botón de expandir** (⤢) abre el [editor de lorebook a pantalla completa](../lorebooks/lorebooks.md#editing-in-full-screen) (solo en escritorio): el que está junto al cuadro de búsqueda del lorebook abre el lorebook, el de una entrada abre esa entrada, y el de un sublorebook (con *Incluir sublorebooks*) abre ese lorebook. La pestaña se vuelve a cargar cuando cierras el editor.

- Cada entrada es una fila con su nombre y el lápiz; la flecha la despliega para mostrar su **contenido** (renderizado como Markdown) y el **texto que la activa** (el
  *Texto o patrón de filtrado*, con una nota cuando es una expresión regular).
- **Incluir sublorebooks** muestra el lorebook y cada uno de sus sublorebooks (y los de estos, a cualquier profundidad) como filas propias, con sus entradas
  debajo; haz clic en la flecha de un lorebook para ocultar sus entradas. Sin esta opción solo se listan las entradas del lorebook del libro.
- Las entradas y los lorebooks desactivados (que no se usan en la historia) se muestran atenuados.
- Un libro sin lorebook muestra una nota en su lugar.

![La pestaña Lorebook con dos entradas desplegadas](../../images/story-lorebook-tab.png)

Como cualquier pestaña del editor, puede arrastrarse hacia fuera y convertirse en un panel propio; consulta más abajo.
<a id="the-planner-tab"></a>

### La pestaña del planificador

La pestaña **Planificador** es una lista de las escenas por las que quieres que avance la historia, en el orden que desees. Cada escena son los datos de un turno que la escribe: un **título** para la lista, el **escenario de la escena**, el **personaje POV**, los **personajes presentes** y las **instrucciones**, los mismos cuatro campos que el formulario de nuevo turno.

![La pestaña Planificador con cuatro escenas, la primera ya escrita](../../images/planner-tab.png)

- **Añadir escena** abre un formulario para una escena nueva; se añade al final de la lista. El lápiz edita una escena, las flechas la mueven hacia arriba o hacia abajo, el cubo de la basura la elimina.
- El **+** de la fila de una escena **inserta una escena nueva** justo debajo de ella, entre esa escena y la siguiente.
- El botón de combinar de una escena **la combina con la siguiente**: se abre un formulario con los datos de ambas ya unidos (los dos títulos, los escenarios y las instrucciones uno debajo de otro, los personajes presentes de ambas, el personaje POV de la primera; cuando la segunda escena tiene otro, el formulario lo indica). Cambia lo que no encaje y pulsa **Combinar** (o <kbd>Ctrl/Cmd+Enter</kbd>): las dos escenas se convierten en una, en el lugar de la primera. Solo se hace si ambas estaban marcadas.
- **Importar desde la rama** añade una escena por cada parte de la rama en la que está el libro (desde la primera parte hasta la más reciente) que tenga datos de turno: su escenario, el personaje POV, los personajes presentes y las instrucciones, con la primera línea de las instrucciones como título. Las partes sin datos de turno se omiten, y las escenas se colocan después de las que ya tienes, sin marcar como hechas. Úsalo para planificar la reescritura de una historia que ya has escrito: importa, combina los turnos que van juntos, inserta lo que falte y luego escribe las escenas en una rama nueva o en una copia del libro.
- La casilla delante de una escena indica que está **hecha** (escrita): el título aparece tachado y la escena se atenúa. Nada lo comprueba por ti; márcala cuando hayas escrito la escena. La línea sobre la lista cuenta cuántas están hechas.
- **Haz clic en una escena** para pasar sus datos al **formulario de nuevo turno** (el formulario se abre con el escenario, el POV, los personajes presentes y las instrucciones de la escena; lo que hubiera se sustituye) y pulsa **Generar** cuando estés conforme. En el teléfono, el cajón se cierra primero.
- Las escenas se guardan con el libro, así que están en sus copias de seguridad y en sus copias. Un modelo también puede añadir escenas: el [asistente de trama](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) de la extensión Arc Outline y un [agente](../agents.md) pueden leer y cambiar el planificador con [comandos](../commands/index.md#the-planner); la lista muestra enseguida lo que añadieron.

![El formulario de una escena nueva](../../images/planner-scene-dialog.png)

![Un clic en una escena rellena el formulario de nuevo turno con sus datos](../../images/planner-new-turn.png)

Como cualquier pestaña del editor, puede arrastrarse fuera y colocarse en un panel propio; véase más abajo.

<a id="arranging-the-tabs"></a>

### Organizar las pestañas

El editor es un conjunto de pestañas: la **Historia** en sí (las partes y la barra inferior debajo de ellas), el **Esquema**, el **Lorebook** y las pestañas que añaden las [extensiones](../extensions/index.md) (Personajes, Arcos, Consulta lateral...). Arrastra una pestaña por su cabecera para reordenarlas (solo en escritorio; el teléfono mantiene una columna con el esquema en un cajón). Un libro nuevo tiene el Esquema y el Lorebook en un panel a la izquierda y la Historia en una columna propia a la derecha; puedes colocar la historia donde quieras, por ejemplo en el centro, con el esquema a un lado y el lore al otro, o encima del esquema:

- Suéltala sobre una de las franjas discontinuas que aparecen encima, entre y debajo de los paneles de una columna para **separarla** en un panel propio dentro de esa columna; los paneles de una columna se apilan unos debajo de otros.
- Suéltala sobre una de las franjas altas a la izquierda, a la derecha o entre las columnas para **separarla hacia un lado**: obtiene una columna propia, junto a las demás. Amplía el editor con los divisores para hacer sitio a más columnas.

Los paneles y las columnas están separados por divisores: arrástralos para dar a cada panel el espacio que necesite. Los tamaños se guardan con la disposición.

![La barra lateral con Personajes en una columna propia y Consulta lateral debajo](../../images/story-editor-tabs.png)

- Suéltala sobre la cabecera de otra pestaña o sobre el cuerpo de otro panel para **unirla** a ese panel (si la sueltas sobre una pestaña, se coloca antes de esa; si la sueltas sobre el cuerpo, se coloca al final). Un panel que pierde su última pestaña desaparece, y lo mismo ocurre con una columna sin paneles.

La disposición y los tamaños se guardan con el libro, así que son los mismos la próxima vez que lo abras y en cualquier navegador; la pestaña que muestra cada panel se recuerda junto con el resto de tu espacio de trabajo. Una pestaña de una extensión que no está cargada conserva su lugar hasta que la extensión vuelva.

La historia siempre está en algún sitio: su pestaña no puede cerrarse, y una pestaña que comparte panel con otras queda oculta mientras se selecciona otra. Cualquier cosa que muestre una parte devuelve la historia a primer plano: al hacer clic en una entrada del esquema, en un resultado de la búsqueda, al elegir una rama en el [árbol de la historia](branches-and-story-tree.md) o al iniciar una generación, se selecciona primero la pestaña **Historia**. Mientras una generación prepara su prompt (normalmente unos segundos) las demás pestañas están desactivadas; cuando el modelo empieza a escribir vuelven a estar disponibles, véase [más abajo](#while-a-part-is-generated).

<a id="minimizing-tabs"></a>

### Minimizar pestañas

Todas las pestañas excepto la de la historia tienen un pequeño botón **⌄** (chevron) junto a su nombre. Lo **minimiza**: la pestaña sale de su panel (un panel cuyas pestañas están todas minimizadas desaparece por el momento) y aparece un icono redondo de ella, un círculo de color con un dibujo (una lista para el esquema, un libro para el lorebook, una burbuja de diálogo para las anotaciones, una lupa para Consulta lateral, etc.; una pestaña de extensión que no tiene dibujo recibe una o dos letras), en una barra en el borde del editor. Pasa el cursor sobre el icono para ver de qué pestaña se trata. No se cierra nada: la pestaña conserva lo que estaba mostrando. Haz clic en el icono y la pestaña vuelve al panel, a su lugar entre las pestañas, y se muestra.

La barra está presente mientras haya alguna pestaña minimizada. El botón de su parte superior (las flechas circulares) envía la barra al siguiente lado del editor, en el sentido de las agujas del reloj: izquierda, arriba, derecha, abajo. La historia no puede minimizarse, así que siempre hay algo en pantalla.

![Pestañas minimizadas en la barra del borde del editor](../../images/story-minimized-tabs.png)

Los iconos siguen el orden alfabético de los nombres de las pestañas hasta que lo cambies: arrastra un icono sobre otro y ocupará su lugar. Qué pestañas están minimizadas, dónde está la barra y el orden de los iconos se guardan con el libro, junto con la disposición y los tamaños.

<a id="the-default-arrangement"></a>

### La disposición predeterminada

Un libro nuevo se abre con la disposición estándar (el esquema, el lorebook y las anotaciones a la izquierda, la historia a la derecha). Para que tu propia disposición sea la estándar, coloca las pestañas como te guste (paneles, tamaños, pestañas minimizadas, lado de la barra) y elige **Guardar la disposición como mi predeterminada** en el menú de ajustes (⚙) de la historia. Todo libro que no tenga una disposición propia (un libro que no has reorganizado) se abrirá entonces con ella; un libro que has organizado conserva la suya. La predeterminada se guarda en tus ajustes de usuario.

Para volver a la disposición predeterminada, elige **Restaurar la disposición predeterminada** en el menú de ajustes (⚙) de la historia y confirma. Es la que guardaste, o la estándar si no guardaste ninguna. Solo devuelve las pestañas a su sitio (y olvida los tamaños y lo que está minimizado): tus textos, la pestaña que muestra cada panel y las pestañas de las extensiones se conservan, estas últimas en el primer panel.
<a id="writing-the-next-part"></a>

## Escribir la siguiente parte

1. Haz clic en **+** en la esquina inferior derecha. Se abre el panel *Instrucciones del nuevo turno*.
2. Rellena los campos. Todos son opcionales, pero el modelo necesita al menos algo de orientación:

   | Campo | |
   |---|---|
   | **Ambientación de la escena** | Dónde y cuándo transcurre la parte, p. ej. *El faro, una noche de tormenta a finales de otoño*. |
   | **Personaje del punto de vista** | Desde el punto de vista de quién se escribe la parte. |
   | **Personajes presentes** | Quién está en la escena y en qué estado. |
   | **Instrucciones** | Qué debe ocurrir en esta parte. |

   *Ambientación de la escena*, *Personaje del punto de vista* y *Personajes presentes* se rellenan a partir de la última parte, así que solo cambias
   lo que es distinto. *Instrucciones* empieza vacío cada vez.
3. Haz clic en **Generar**.

![El panel Instrucciones del nuevo turno](../../images/new-turn-instructions.png)

La nueva parte aparece al final y se va completando mientras el modelo la escribe. Para los [modelos de razonamiento](../inference-providers.md#reasoning-models)
el razonamiento aparece primero, bajo **Ver el razonamiento**.

Mientras se genera una parte, el botón **+** se convierte en un botón rojo **Detener**. Detener conserva el texto escrito hasta
ese momento como parte.

<a id="while-a-part-is-generated"></a>

### Mientras se genera una parte

Durante los primeros segundos, mientras se compone el prompt a partir del libro, toda la ventana del libro espera. En cuanto el modelo empieza a
escribir, solo quedan retenidas la nueva parte y las partes anteriores a ella:

- **La nueva parte** no se puede tocar hasta que termine: ni editar, ni menú.
- **Las partes anteriores** se mantienen donde están: puedes editarlas, anotarlas y redactarlas de nuevo, pero no borrarlas, fusionarlas ni ramificarlas, y
  la historia permanece en su rama (no hay swipe ni cambio de rama). Un cambio en una parte anterior, o en el lore, no llega a la parte
  que se está escribiendo: su prompt ya se compuso. Sí cuenta para lo que venga después.
- **Todo lo demás está libre**: el lore, los ajustes y el prompt del libro, las pestañas de la barra izquierda (consulta paralela, esquema del arco, notas...),
  la barra de comandos. Una petición al modelo espera una conexión libre del modelo si la generación tiene la única.
- **En espera**: un segundo turno, buscar y reemplazar, una importación en el libro y la eliminación masiva. Las pestañas de árbol y de copias de seguridad también esperan.

Lo que la ventana deja en gris también se rechaza cuando un comando, un agente o una extensión lo pide: Marginalia indica qué lo impide
(*Esto está en uso: …*).

**Salir del libro detiene la generación.** El botón de salida pregunta antes; *Sí* equivale a **Detener**: lo que llegó se conserva. Cerrar
la pestaña del navegador hace lo mismo, y el navegador pregunta antes de cerrar o recargar la pestaña mientras se genera una parte. Una pérdida breve
de la red, o un portátil cerrado un minuto, no es salir: la página vuelve y muestra lo que llegó mientras tanto.

Cuando el modelo devuelve un error (servidor no accesible, clave de API incorrecta, contexto demasiado largo...), el error se muestra.
Si todavía no llegó ningún texto, la historia queda como estaba: una parte nueva o un swipe se elimina, y una parte regenerada recupera
su texto anterior. El texto que ya llegó antes del error se conserva, como con **Detener**. Soluciona el problema y
vuelve a generar.

Cómo llegan los campos al prompt lo determina el [Prompt de usuario](prompts.md#user-prompt) del libro. Con el prompt de usuario
predeterminado, el modelo los recibe como *Ubicación y hora actuales*, *Personaje del punto de vista*,
*Personajes presentes y estado* y *Especificaciones de la trama para la siguiente parte*.

!!!
El texto se escribe en Markdown: `*italics*`, `**bold**`, `# Heading` y así sucesivamente se muestran con formato. Una parte con un encabezado
`#` es un capítulo para la extensión [Marcador de capítulos](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Redactar las instrucciones

¿No sabes qué debe pasar después? **Redactar instrucciones**, debajo del campo *Instrucciones* del panel, permite que el modelo escriba las instrucciones en tu lugar, como lo harías tú. Se abre una ventana con:

| | |
|---|---|
| **Orientación** | De qué debe tratar la parte: una idea vaga, una dirección («ella lo confronta por la carta»), o nada, y el modelo continúa la historia por donde le lleve. Empieza con lo que ya escribiste en *Instrucciones*. |
| **Plan de la historia** | Solo cuando algo lo proporciona. El [Esquema del arco](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter) lo hace, con los datos del capítulo actual. Solo lectura; el modelo lo sigue. |

**Redactar instrucciones** pone en marcha el modelo y va volcando la respuesta en **Instrucciones redactadas**, que puedes cambiar o vaciar
antes de continuar; **Redactar de nuevo** lo intenta otra vez, **Detener** conserva lo que haya llegado. No se cambia nada hasta que haces clic en
**Usar como instrucciones**: el texto sustituye entonces a las *Instrucciones* del panel (los demás campos no se tocan) y el panel se abre
de nuevo, listo para **Generar**.

El modelo recibe la historia hasta ahora (la parte más antigua como [resúmenes](summaries.md), como en una generación), la ambientación de la escena, el punto de vista y
los personajes presentes que están en el panel, el lore de la última parte, tus últimas instrucciones como ejemplos de cómo las escribes, tu orientación y el
plan. No ve las entradas de tu lorebook que activaría un nuevo turno. La petición es el [prompt de suplantación](prompts.md#impersonate-prompt). El modelo es el del libro; redactar consume tokens como cualquier petición.

![Redactar las instrucciones del siguiente turno](../../images/impersonate-dialog.png)

El mismo efecto tiene el comando [`/impersonate`](../commands/index.md), que devuelve el texto. Los [agentes](../agents.md) no lo reciben, ya que escriben
la historia con instrucciones propias.

<a id="the-settings-menu"></a>

## El menú de ajustes

El menú de ajustes (⚙) de la barra inferior agrupa lo que actúa sobre toda la historia. **Buscar y reemplazar** está arriba; el resto está en submenús
(también en el teléfono, salvo los elementos solo de escritorio):

| Submenú | Elementos |
|---|---|
| **Apariencia** | **Cambiar los estilos del texto**, **Resaltar el texto generado** y **Anotaciones en el margen** (solo escritorio, el último) |
| **Análisis** | **Anotar un intervalo de partes con IA**, **Crear resúmenes**, **Tomar notas de lore con un agente**, **Anotación automática**, **Tokens de la rama**, **Uso del lore**, **Estadísticas** |
| **Importar y exportar** | **Importar**, **Exportar la historia** (y los elementos de extensiones que importan o exportan, como **Exportar instrucciones**) |
| **Limpieza** | **Borrar el historial del libro**, **Vaciar la papelera del historial**, **Eliminación masiva** |
| **Disposición** (solo escritorio) | **Guardar la disposición como mi predeterminada**, **Restaurar la disposición predeterminada** |

Los elementos que traen las [extensiones](../extensions/index.md) están en el submenú que les corresponde, o al final del menú. Los atajos de los elementos están en
*Ajustes → Atajos de teclado*, donde un elemento se nombra con su submenú (*Análisis › Estadísticas*). Las secciones siguientes indican dónde está cada elemento.
<a id="the-part-menu"></a>

## El menú de la parte

Cada parte tiene un menú (☰) en la esquina superior derecha. **Editar**, **Descartar los cambios**, **Regenerar**, **Variante**, **Reescribir**, **Historial** e **Imágenes** están en el propio menú, **Eliminar** es su último elemento, y el resto está en cuatro submenús: **Anotaciones** (*Anotar la parte*, *Anotar con IA*), **Resumen** (*Generar resumen* / *Ver resumen*, *Eliminar resumen*), **Inspeccionar** (*Mostrar el prompt*, *Comparar prompt*, *Uso de tokens*, *Uso del lore*) y **Ramas y fusión** (*Crear una rama*, *Combinar con la parte anterior*). Un submenú sin nada para la parte no se muestra. El Revisor añade su submenú **Revisar** después de *Resumen*. Los elementos:

| Elemento | |
|---|---|
| **Editar** | Convierte el texto en un campo editable. **Guardar** (el mismo elemento del menú) lo guarda. Iniciar una generación o salir de la pestaña también guarda las ediciones abiertas. |
| **Descartar los cambios** | Solo mientras el texto está editado: cierra el campo sin guardar, la parte conserva el texto con el que se guardó por última vez. Si cambiaste el texto, se te pregunta antes. |
| **Regenerar** | Vuelve a escribir la última parte, sustituyendo su texto. Solo en la última parte. |
| **Variante** | Escribe otra versión de la última parte y conserva la anterior como una rama separada. Solo en la última parte. Véase [Ramas y árbol de la historia](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Reescribir** | Permite que el modelo vuelva a escribir esta parte a partir de tus instrucciones, en cualquier parte, véase [más abajo](#redrafting-a-part). No mientras el texto está editado. |
| **Historial (n)** | Las [versiones del texto](version-history.md) de la parte, con el número de versiones: compararlas, restaurar una o darle nombre a una. No mientras el texto está editado. |
| **Anotar la parte** | Crea una [nota](annotations.md#making-an-annotation) sobre toda la parte. Para anotar palabras, selecciónalas y haz clic derecho (**Anotar la selección**). |
| **Anotar con IA** | Permite que un modelo [revise la parte](annotations.md#letting-a-model-annotate) y proponga notas. No mientras el texto está editado. Para varias partes a la vez usa **Anotar un intervalo de partes con IA** en el menú de ajustes (⚙), y para cada parte nueva **Anotación automática** (véase [Anotaciones](annotations.md#annotating-every-new-part-automatically)). |
| **Crear una rama** | Inicia una nueva rama desde esta parte, véase [Ramas y árbol de la historia](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Combinar con la parte anterior** | Añade el texto de esta parte a la parte anterior, véase [más abajo](#merging-a-part-into-the-previous-one). No está disponible en la primera parte. |
| **Mostrar el prompt** | Muestra el prompt exacto que se envió al modelo para esta parte. |
| **Uso de tokens** | Muestra cómo se usó el contexto del modelo para esta parte: un gráfico de las secciones del prompt (instrucciones del modelo, lore, resúmenes, historia hasta el momento, tus instrucciones, espacio reservado para la respuesta) y el tamaño de cada mensaje enviado al modelo. Si parte de la historia no cupo, indica cuántas partes se dejaron fuera. Las partes escritas antes de que esto se registrara no tienen gráfico. |
| **Uso del lore** | Enumera las entradas del [libro de lore](../lorebooks/index.md) que entraron en el prompt de esta parte, con los tokens que costó cada una y lo que la encontró (el prompt, o el contenido de otra entrada cuando el libro de lore analiza más a fondo). Las partes escritas a mano, las escritas antes de que esto se registrara o las escritas para un libro sin libro de lore indican que no se registró nada. Para todo el libro usa **Uso del lore** en el menú de ajustes (⚙), véase [más abajo](#which-lore-entries-were-used). |
| **Imágenes** | Adjunta imágenes a la parte, véase [Imágenes](#images). |
| **Generar resumen** / **Ver resumen** | Resume la historia hasta esta parte, o muestra el resumen, véase [Resúmenes](summaries.md). |
| **Eliminar resumen** | Quita el resumen de esta parte. Para un [metaresumen](summaries.md#meta-summaries) pregunta si deshacerlo o eliminarlo por completo. |
| **Eliminar** | Elimina la parte, véase [más abajo](#deleting-a-part). |

*Regenerar* y *Variante* usan los detalles del turno de la última parte (véase [más abajo](#turn-details)). Para cambiar lo que
debe ocurrir, edítalos primero.

![El menú de una parte, con el submenú Inspeccionar abierto](../../images/story-part-menu.png)

![Uso de tokens de una parte](../../images/token-usage.png)

<a id="part-metadata"></a>

## Metadatos de la parte

Junto a cada parte:

| | |
|---|---|
| **Orden #n \| ID en la BD: m** | La posición de la parte en la rama, y su ID en la base de datos. |
| **Modelo** | El proveedor de inferencia que escribió la parte. |
| **Tokens: x (y)** | `x` tokens de texto en la parte, `y` tokens en el prompt que la produjo. |
| **Palabras** | Palabras en la parte. |
| **Detalles del turno** | Las instrucciones a partir de las cuales se escribió la parte. |

<a id="turn-details"></a>

### Detalles del turno

**Detalles del turno** abre la *Ambientación de la escena*, el *Personaje del punto de vista*, los *Personajes presentes* y las *Instrucciones* a partir de las cuales se escribió la parte. Para la última parte se pueden cambiar (los cambios se guardan de inmediato) y los usan *Regenerar* y *Variante*. Para las partes anteriores son de solo lectura.

![Detalles del turno de una parte](../../images/turn-details.png)

<a id="editing-the-text"></a>

## Editar el texto

El texto generado es tuyo para cambiarlo. Elige **Editar** en el menú de la parte, cambia el texto y elige **Guardar**. Las partes editadas se envían al modelo tal como las editaste.

Editar una parte que cubre un [resumen](summaries.md) vuelve obsoleto ese resumen; Marginalia lo advierte en la siguiente generación y descarta el resumen. Para que esto no ocurra por sorpresa, **Guardar** revisa primero el texto nuevo: cuando dejaría obsoletos resúmenes, aparece una ventana que indica cuántos son y ofrece **Volver a la edición** (no se guarda nada, el editor sigue abierto con tu texto), **Guardar de todos modos** (los resúmenes quedan obsoletos) y **Guardar y revalidar los resúmenes** (el texto se guarda y los resúmenes siguen válidos, para un cambio que no altera lo que dicen, como una errata). La comprobación se hace al usar **Guardar** (el menú, el atajo y el botón del editor); los guardados automáticos, por ejemplo al iniciar una generación con el editor abierto, no preguntan.

<a id="keyboard-shortcuts"></a>

## Atajos de teclado

En un ordenador (el [diseño para teléfono](../phone.md) no tiene ninguno) el ciclo de escritura funciona con el teclado:

| Tecla | |
|---|---|
| `+` | Abre el panel *Instrucciones del nuevo turno* con el cursor en *Instrucciones*. |
| `Ctrl+Enter` | Genera desde el panel abierto. `Esc` lo cierra; el teclado vuelve a la historia. |
| `Ctrl+.` | Detiene la generación en curso. |
| `s` / `r` | **Variante** / **Regenerar** la última parte. |
| `e` | Abre el editor de la parte que más se ve en la pantalla (la última parte si ninguna). |
| `Ctrl+S` | Guarda la parte que se está editando (también con el cursor en su texto). |
| `/` | Lleva el cursor a la barra de comandos (primero se muestra la pestaña de la historia). |
| `Ctrl+Shift+F` | Abre la pestaña *Esquema* y lleva el cursor a la búsqueda de la rama (`Enter` / `Shift+Enter` van a la siguiente / anterior coincidencia). |
| `Ctrl+Shift+H` | Abre la ventana *Buscar y reemplazar* del libro. |
| `a` | **Anotar la selección**: crea una nota sobre el texto seleccionado en una parte. |
| `Alt+PageDown` / `Alt+PageUp` | Muestra la siguiente / anterior pestaña, recorriendo las pestañas de todas las hojas en el orden del diseño y volviendo a empezar. |

Una tecla sin `Ctrl`, `Alt` o `Meta` (como `+`, `s`, `r`, `e`, `a`, `/`) funciona solo cuando ningún campo de texto tiene el foco, para que nunca estorbe al escribir. Los atajos funcionan en la pestaña de la historia del libro abierto y no mientras otra ventana esté abierta encima. Los botones muestran su tecla en la descripción emergente, y el menú de la última parte la muestra después de *Editar*, *Regenerar* y *Variante*. En un Mac `Cmd` funciona donde una tecla dice `Ctrl` (salvo que hayas asignado esa tecla `Cmd` a otra acción). Puedes cambiar cada tecla en [Ajustes → Atajos de teclado](../account.md#keyboard-shortcuts).
<a id="deleting-a-part"></a>

## Eliminar una parte

**Eliminar** quita la parte de la historia. Las partes que la seguían se mantienen y continúan desde la parte anterior a ella, así que eliminar una parte en medio de la historia simplemente la saca. Eliminar la última parte hace que la parte anterior pase a ser el nuevo final de la rama.

<a id="deleting-many-parts-at-once"></a>

### Eliminar muchas partes a la vez

El menú de ajustes (⚙) tiene **Eliminación masiva**. Al activarlo, aparece una casilla a la izquierda de cada parte y se muestra una barra debajo de la historia con el número de partes marcadas y un botón **Eliminar**. Marca las partes que quieres quitar (las marcas se conservan mientras haces scroll, ya que la historia carga solo una ventana de partes); **Shift + clic** en una casilla marca (o desmarca) todas las partes comprendidas entre ella y la que pulsaste antes; una parte marcada se tiñe de rojo. Luego pulsa **Eliminar**: tras una confirmación, las partes se eliminan una a una mientras se muestra una barra de progreso que no se puede detener. Se eliminan igual que se elimina una sola parte: las partes que seguían a una parte eliminada continúan desde la parte anterior a ella, y las partes eliminadas van a la papelera. Después se vuelve a mostrar la historia y la eliminación masiva se desactiva. **Cancelar** en la barra, o elegir **Eliminación masiva** de nuevo, la desactiva sin eliminar nada. Iniciar una generación también la desactiva. Una parte marcada sobre la que trabaja una ventana o una traducción se queda en la historia, y Marginalia indica qué la está usando.

![Eliminación masiva con algunas partes marcadas](../../images/story-mass-delete.png)
<a id="redrafting-a-part"></a>

## Rediseñar una parte

**Regenerar** y **Variante** trabajan solo sobre la última parte y la escriben desde cero. **Reescribir** funciona con cualquier parte, también con una antigua en medio de la historia, y conserva lo que no pides cambiar. Elige **Reescribir** en el menú de la parte:

1. Escribe lo que debe cambiar en **Instrucciones para la reescritura**: *añade más diálogo entre los dos*, *alarga la pelea*, *elimina la explicación del mapa*, *mantén el final tal como está*. Si las instrucciones están vacías, la parte se vuelve a escribir con los mismos hechos.
2. Elige **Extensión** si la parte debe quedar **Más corto** o **Más largo** (por defecto, **La misma extensión**): se añade una frase que lo pide después de tus instrucciones (véase [Redraft Shorter Prompt and Redraft Longer Prompt](prompts.md#redraft-shorter-and-longer)).
   Elige **Reescribir**. El texto va apareciendo en **Texto reescrito**; **Detener** lo termina antes. Elige **Reescribir de nuevo** para probar otra vez, con otras instrucciones si quieres. **Diferencias** muestra, palabra por palabra, lo que ha cambiado respecto a la parte. Cuando la parte tiene [anotaciones](annotations.md) abiertas, **Deal with the open annotations of the part** (marcada desde el principio) también las entrega al modelo, y **Aplicar** las marca como resueltas.
3. El texto reescrito es un campo que todavía puedes editar (su icono de esquina lo hace crecer para ajustarse a todo el texto), y **Diferencias** sigue tus ediciones. **Aplicar** reemplaza el texto de la parte por él; **Cerrar** lo descarta. Nada del libro cambia antes de **Aplicar**.

![La ventana de reescritura: las instrucciones, el texto nuevo y las diferencias](../../images/redraft-dialog.png)

El modelo recibe las [instrucciones de reescritura](prompts.md#redraft-prompt): la parte, tus instrucciones, la historia anterior a la parte (la historia más antigua como [resúmenes](summaries.md) cuando hay resúmenes válidos, el resto como texto, la parte más reciente primero cuando no cabe todo), tanta historia posterior a la parte como quepa (hasta un cuarto del espacio disponible), el lore que los libros de lore dan ahora a esa parte (calculado de nuevo para el turno de la parte, de modo que una edición de una entrada llega a la reescritura; el lore con el que se escribió la parte se usa solo cuando el libro no tiene libro de lore), el punto de vista, el tiempo verbal y el estilo del libro, y el escenario, el personaje del punto de vista, los personajes presentes e instrucciones del turno del que procede la parte. La historia posterior a la parte está para que el texto nuevo siga enlazando con ella: los hechos en los que confían las partes siguientes deben mantenerse.

La parte conserva su lugar, su resumen, sus imágenes y cómo se generó; el texto, sus palabras y sus tokens se reemplazan, y la parte cuenta como editada. Aplicar la reescritura es una edición, igual que **Guardar** en el editor, así que los [resúmenes](summaries.md) que describen el texto antiguo quedan desactualizados, y Marginalia indica cuántos. Un resumen de la propia parte es uno de ellos.

Cuando un resumen describe la parte (la parte tiene uno, o una parte más reciente de la rama lo tiene), la ventana muestra la casilla **Revalidate the summaries this redraft makes outdated**, desmarcada al principio. Márcala cuando los resúmenes sigan diciendo lo correcto (la reescritura cambió la redacción, no lo que ocurre): **Aplicar** entonces vuelve a calcular su hash y siguen siendo válidos. Déjala desmarcada cuando cambiaron los hechos, y haz los resúmenes de nuevo.

La ventana no bloquea la historia, véase [Trabajar junto a la ventana](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Trabajar junto a las ventanas de reescritura y de anotación

En un ordenador, las ventanas **Reescribir** y **Anotar con IA** se sitúan a la derecha de la historia y la dejan utilizable: puedes leer, desplazarte y editar la historia mientras el modelo escribe. Arrastra una ventana por su título para moverla, o por su borde para cambiar su tamaño. La flecha (⌄) del título pliega una ventana en el [panel de ejecuciones](#the-panel-of-the-runs) situado abajo a la izquierda de la página: el modelo sigue trabajando, **Abrir** (o un clic en la entrada) vuelve a mostrar la ventana, y **Detener** detiene el modelo (cuando ha terminado, **Cerrar** cierra la ventana). En un teléfono las ventanas siguen cubriendo la historia, pero se pliegan de la misma manera.

![La ventana de reescritura junto a la historia mientras el modelo escribe, y la ejecución en el panel de abajo a la izquierda](../../images/redraft-beside-story.png)

**Cerrar y Detener son dos cosas distintas.** El propio **Detener** de la ventana detiene el modelo, y la ventana se queda con lo que llegó. **Cerrar** mientras el modelo trabaja no lo detiene: la ventana se pliega en el panel y el modelo sigue en su cola; su respuesta está en la ventana cuando la vuelves a abrir. Cierra una ventana cuyo modelo ya terminó, o **Cerrar** en el panel, y desaparece; cerrar el libro o la pestaña del navegador detiene también una ventana plegada. Lo mismo vale para las demás ventanas que esperan a un modelo: un **resumen** (y *Crear resúmenes*, *Reescribir todos los resúmenes*), el [Revisor](../extensions/reviewer.md), la descripción del libro, los generadores de lore y de esquema, los generadores de imágenes y de manga, y el redactor de personas de Lectores beta. Las ventanas *Estudio* y *Convertir* del audiolibro tampoco bloquean la página; cerrarlas nunca detuvo la conversión.

Mientras una ventana está abierta (o plegada) sobre una parte, la parte muestra lo que hace junto a su menú (*Reescribiendo · El libro*), y **Regenerar**, **Variante**, **Reescribir**, **Anotar con IA**, **Fusionar con la anterior** y **Eliminar** están desactivados para esa parte, de modo que la ventana no pierde su parte. Editar la parte sigue siendo posible. Cerrar el libro cierra sus ventanas y detiene sus modelos. Una parte en la que trabaja un [traductor](../extensions/real-language-translator.md) muestra *Traduciendo el habla de la parte...* de la misma manera, y dos traductores nunca trabajan a la vez sobre una misma parte: el segundo indica que la parte está en uso.

- Cuando **Aplicas** una reescritura de la parte completa y la parte se cambió después de abrir la ventana (la editaste entretanto), Marginalia pregunta primero: **Diferencias** compara entonces la parte tal como está ahora con la reescritura, para que veas lo que se reemplazaría; elige **Aplicar de todos modos** o **Cancelar**. Una reescritura que [se aplica sola](annotations.md#annotating-every-new-part-automatically) no hace esto cuando la parte se cambió: la ventana espera a que decidas.
- Cuando la parte se eliminó antes de que **Apliques** la reescritura o añadas las anotaciones, la ventana lo indica, no escribe nada y se cierra.

<a id="the-panel-of-the-runs"></a>

### El panel de ejecuciones

Todo lo que trabaja ahora mismo con un modelo sobre tus libros aparece listado abajo a la izquierda de cada página: una generación, una reescritura o una anotación, una traducción por el traductor [Real Language](../extensions/real-language-translator.md) o [Conlang](../extensions/conlang-translator.md), una reescritura de los resúmenes, el Knowledge Tracker leyendo la historia, una conversión de [Audiolibro](../extensions/audiobook.md), o un comando de la barra de comandos que tarda (*Redactar instrucciones*, un resumen...: su ventana se sitúa al costado y se pliega como las demás). También aparecen las ventanas en las que esperas una respuesta mientras el modelo contesta: **Suplantar**, el asistente de ayuda, una traducción rápida, el diseñador de Conlang, un chat de Side Query, Lectores beta o el asistente de trama. Lista tus ejecuciones de **todas tus páginas abiertas** (otra pestaña, el teléfono), no solo las iniciadas en esta página. Si no hay nada en marcha, no se muestra nada.

![El panel de ejecuciones abierto: la reescritura plegada en él, con el final del texto del modelo, Abrir y Detener](../../images/runs-panel.png)

- **Cerrado**, es una barra pequeña: la primera ejecución, cuántas más hay (*Traduciendo el habla de la parte... · 2 más*) y una barra en movimiento mientras un modelo trabaja. Haz clic para abrir la lista; se abre sola cuando pliegas una ventana en ella.
- **Abierto**, cada ejecución muestra qué hace, el libro, cuánto lleva en marcha, qué espera y el final de lo que escribe el modelo:
  - *Esperando al modelo: 2 peticiones antes que esta* cuando el modelo está ocupado con otras peticiones (véase *Conexiones* en [Proveedores de inferencia](../inference-providers.md));
  - *El modelo está pensando...* o *El modelo está escribiendo...*, con el final de su razonamiento o de su respuesta debajo;
  - *Te espera* cuando la ejecución espera una decisión tuya (una reescritura por aplicar, una propuesta del Knowledge Tracker por revisar, la pregunta del Inspector de prompts antes de una generación, un comando del asistente de trama que hay que permitir).
- **Detener** detiene la ejecución, esté donde esté iniciada. **Abrir**, o un clic en la entrada, muestra de nuevo una ventana plegada (solo en la página en la que se abrió) o la propia ejecución; es **Revisar** cuando la ejecución te espera. Una ejecución del Knowledge Tracker abre su libro en esta página (cuando no hay otro libro abierto aquí; si no, se te pide que cierres ese primero), cambia a la pestaña Knowledge y muestra la revisión. **Cerrar** cierra una ventana para siempre (y detiene su modelo).
- En un ordenador puedes mover el panel por su asidero (⋮ a su izquierda); se queda donde lo dejes en este navegador. En la mitad inferior de la página crece hacia arriba al abrirse, como en su esquina, y siempre se mantiene dentro de la página. Cuando ocupa el lugar del botón redondo del [asistente de ayuda](../administration/help-assistant.md) (sobre un libro, ambos están en la esquina inferior izquierda), el botón se aparta y vuelve cuando el lugar queda libre.

Cuando una ejecución que iniciaste termina después de cerrarse su página, otra página tuya abierta envía su [notificación](../account.md#notifications) (para las ejecuciones que la tienen, como la reescritura de los resúmenes).

<a id="redrafting-only-a-passage"></a>

### Reescribir solo un pasaje

Para reescribir unas pocas frases y dejar el resto de la parte intacto, selecciónalas en el texto de la parte y haz **clic derecho** (una pulsación larga en un teléfono): el menú tiene **Reescribir la selección**. (El menú ☰ de la parte no se ofrece para esto, porque abrirlo puede perder la selección en algunos navegadores.) La ventana funciona como arriba con dos diferencias: muestra el **Texto seleccionado**, y la reescritura es solo el texto que lo reemplaza. El modelo recibe la parte completa con la selección marcada, y la historia y el lore alrededor, de modo que el pasaje nuevo se una a las frases anteriores y posteriores. **Diferencias** compara la selección con el texto reescrito; **Aplicar** pone el texto reescrito en el lugar de la selección y conserva el resto de la parte, incluido el espacio o el salto de línea en los bordes de la selección. Con [anotaciones](annotations.md) abiertas, la casilla solo ofrece las que tienen un pasaje que se solapa con la selección. Si la parte se cambió después de que seleccionaras el texto (en otra ventana), **Aplicar** lo rechaza y tienes que volver a seleccionarlo. Mientras la parte se edita, se usa la selección del editor, y antes se guarda la edición.

<a id="what-follows-a-generated-part"></a>

### Lo que sigue a una parte generada

Cuando una parte que generaste (desde la página o desde la barra de comandos) está completa, el libro puede hacer más con ella por sí mismo, en este orden:

1. **Anotar**: la [anotación automática](annotations.md#annotating-every-new-part-automatically) revisa la parte.
2. **Reescribir**: la parte se vuelve a escribir para atender las notas.
3. **Traducir**: los traductores [Real language](../extensions/real-language-translator.md) y [Conlang](../extensions/conlang-translator.md) traducen sus discursos.
4. **Knowledge**: el [Knowledge Tracker](../extensions/knowledge-tracker.md) la lee.
5. **Audio**: el [Audiolibro](../extensions/audiobook.md) la convierte en audio.

Solo se ejecutan los pasos que el libro tiene activados. Se ejecutan en el servidor, así que la página puede cerrarse, y forman una sola entrada en el [panel de ejecuciones](#the-panel-of-the-runs): *Tras la parte 14: Anotar ✓ · Reescribir … · Traducir*, con el texto del modelo y un solo **Detener**. Lo que cambia el texto va primero, de modo que el tracker y el audio reciben el texto final. Un paso que te espera (notas o una reescritura que elegir) abre su ventana, y la entrada tiene **Revisar** para abrirla en otra página.

- **Detener**, cerrar una ventana que te espera, o un paso que falla la termina ahí: lo ya hecho se conserva, lo demás no se ejecuta, y un mensaje indica qué queda pendiente, para hacerlo a mano (cada paso tiene su propia acción sobre la parte).
- Mientras se ejecuta la **reescritura**, la siguiente parte no se puede generar (se te dirá qué ejecución lo impide); mientras se ejecutan los demás pasos, sí se puede.
- La parte no se puede eliminar hasta que su texto sea definitivo; volver a generarla termina lo que seguía a su versión anterior.
- Un [agente](../agents.md) realiza estos pasos mediante sus propios comandos: nada sigue a una parte que generó un agente.
<a id="merging-a-part-into-the-previous-one"></a>

## Fusionar una parte con la anterior

Un modelo que se detuvo a mitad de una frase (el límite de respuesta, una conexión cortada) no puede recibir la orden de «continuar» el mismo texto de forma fiable. En su lugar, escribe la parte siguiente (*continúa desde donde se detuvo la última parte*) y une las dos con **Combinar con la parte anterior** en el menú de la segunda. Se abre una revisión:

- **Unir los textos con**: una línea vacía, un salto de línea, un espacio o nada. Viene preseleccionado según el final del texto anterior: un párrafo después de un texto que terminó una frase, y un espacio después de uno que quedó cortado.
- La **ambientación de la escena**, el **personaje de punto de vista**, los **personajes presentes** y las **instrucciones** de ambos turnos, combinados en campos que puedes editar. Lo que los dos turnos tienen en común al principio se conserva una sola vez («Escribe el duelo. Que sea breve» y «Escribe el duelo. Añade lluvia» dan el primero, y «Añade lluvia» en la línea siguiente).
- **Datos**: lo que conservan las dos partes, valor por valor (por ejemplo, las [variables](../templates/macros.md) de la historia y los datos de las extensiones), indicando si el valor es igual, distinto o existe solo en una de las partes. La parte fusionada conserva el valor de la **parte posterior** (es donde se encuentra ahora la historia), salvo que elijas para ese dato el valor de la parte anterior o que lo dejes fuera. Los valores iguales quedan ocultos a menos que los pidas.

![La revisión antes de fusionar una parte con la anterior](../../images/part-merge-dialog.png)

La parte anterior conserva su lugar, su resumen y cómo se generó (prompt, modelo, tiempos); recibe el texto unido, el razonamiento unido y las imágenes, y sus palabras y tokens se vuelven a contar. La parte que fusionaste se elimina (queda en la [papelera](../trash.md)) y lo que venía después continúa desde la parte anterior. Los resúmenes que abarcan las dos partes quedan desactualizados, igual que tras una edición. El [historial](version-history.md#backups-copies-and-the-trash) de la parte que se fusionó se elimina, y la parte superviviente tiene el texto unido como una versión **Combinada**. No se puede fusionar una parte si es la primera del libro, ni si la parte anterior tiene otras continuaciones (variantes de la parte): elimínalas o conviértelas antes en una rama.

<a id="images"></a>

## Imágenes

Una parte puede tener imágenes: un mapa, el retrato de un personaje, una ilustración. Elige **Imágenes** en el menú de la parte para abrir la lista de imágenes de esa parte.

![El diálogo de Imágenes de una parte](../../images/images-dialog.png)

- **Añadir imagen** (o arrastra un archivo sobre él) sube una imagen PNG, JPEG, GIF o WebP de hasta 10 MB. Las imágenes WebP se convierten a PNG al subirlas, porque los archivos de Word y PDF no pueden contener WebP.
- **Pie de imagen** se muestra debajo de la imagen. Puede estar vacío.
- **Posición en la parte** es una cuadrícula de 3x3. La columna es el lado del texto (izquierda, centro, derecha), y la fila es el lugar dentro de él: arriba es antes del texto, el medio es en mitad del texto, abajo es después de él. Haz clic en un cuadrado para elegirlo.
- **El texto rodea la imagen** permite que el texto corra alrededor de la imagen en lugar de quedar encima y debajo. A la izquierda o a la derecha, el texto discurre junto a la imagen. En la columna central el texto queda a ambos lados: unos cuantos bloques del texto (unos 400 caracteres, dos bloques como mínimo) en esa fila se convierten en dos columnas, la primera parte a la izquierda de la imagen y el resto a la derecha, de modo que el texto se lee bajando por la columna izquierda y continúa en la derecha. Dos imágenes en el mismo lugar se colocan una encima de otra entre las columnas. Cuando el texto no tiene espacio para dos columnas (solo un párrafo, o los bloques ya están ocupados por la franja de arriba), la imagen ocupa un bloque propio, centrada.
- **Ancho** es el ancho de la imagen como porcentaje del ancho del texto. **Automático** mantiene el tamaño de la imagen (nunca más ancha que el texto; una imagen con texto a ambos lados ocupa el 30 %). Una imagen junto a la que corre el texto mide como máximo el 60 %, y una con texto a ambos lados como máximo el 40 %, para que el texto conserve algo de espacio. La altura sigue las proporciones de la imagen.
- El botón de papelera quita la imagen de la parte.

![La posición de una imagen: en el centro, con el texto rodeándola](../../images/images-position.png)

**Vista previa como en el visor** (un botón del diálogo, y la misma entrada en el menú de la parte cuando tiene imágenes) muestra la parte tal como la presenta el [visor](publishing.md), con el estilo de texto del libro: las imágenes donde las colocaste, y un interruptor para una pantalla ancha o estrecha (de teléfono). Usa el texto del editor tal como está ahora, guardado o no.

![La vista previa: el texto en dos columnas con la imagen entre ellas](../../images/images-preview.png)

El propio editor de la historia no usa la posición: muestra las imágenes debajo del texto de la parte, en forma de lista. La posición la usan el visor y las [exportaciones](export.md#images).

El lugar del texto donde va una imagen no depende de números guardados con ella: el texto se corta entre sus bloques (párrafos, listas, citas y bloques de código nunca se cortan), de modo que las imágenes siguen donde deben cuando editas el texto. El centro es el punto entre bloques más cercano a la mitad del texto; una imagen junto a la que corre el texto en la parte inferior se coloca antes del último bloque, para que al menos ese bloque discurra a su lado. Las imágenes en el mismo lugar conservan el orden de la lista; las que están una al lado de otra en el mismo lugar (y no dejan que el texto corra alrededor) se colocan en fila cuando caben. PDF y Word hacen eso con las imágenes que tienen un ancho.

Las imágenes **no forman parte del texto**. El modelo nunca las ve, no están en el prompt, no cuentan como tokens y no cambian los [resúmenes](summaries.md). Tampoco son Markdown: un enlace `![](...)` escrito en el texto no es una imagen de la parte; usa **Imágenes** en su lugar.

Conviene saber:

- **Regenerar** conserva las imágenes de la parte, también cuando la generación falla. **Variante** escribe una parte nueva (una versión nueva junto a la anterior), que empieza sin imágenes. **Crear una rama** copia las imágenes a la parte nueva.
- Las imágenes pertenecen a tu cuenta. En el [visor](publishing.md), los lectores de un libro publicado también las ven, a menos que desmarques **Mostrar las imágenes a los lectores** en la pestaña *Información* del libro.
- Una [copia de seguridad](backups.md) recuerda qué imágenes tiene una parte, pero no los archivos de las imágenes. Restaurar una copia en otra cuenta o en otra instalación deja las partes sin sus imágenes.
- Quitar una imagen de una parte solo la separa de esa parte. El archivo sigue existiendo y aparece en [Recursos](../resources.md), donde puedes descargarlo, reemplazarlo o eliminarlo. Eliminarlo ahí lo quita de todas las partes que lo usan. El archivo en sí permanece en la carpeta de datos.

<a id="summaries"></a>

## Resúmenes

El botón **Resúmenes** de la barra inferior (a la izquierda del menú de ajustes ⚙) abre la vista general de los resúmenes de la rama, donde se pueden editar, eliminar y fusionar en metaresúmenes. La información del botón muestra cuántos tokens ocupan en el prompt los resúmenes en uso. Consulta [Resúmenes](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Cuánto contexto ocupa la historia

El menú de ajustes (⚙) tiene **Tokens de la rama**. Muestra, antes de generar nada, cuánto ocupa la historia de la rama activa en el prompt: una barra para la historia **sin resúmenes** (todas las partes) y **con resúmenes** (los resúmenes y las partes posteriores al más reciente), y una tabla con las partes, los tokens y la proporción del **presupuesto del prompt** (el contexto del modelo del libro menos el espacio reservado para la respuesta). Solo cuentan los resúmenes que todavía describen la historia; los que ya no coinciden se nombran, y se descartan al generar una parte. La ambientación, la plantilla del sistema y las instrucciones del turno ocupan espacio del mismo presupuesto. Sin un modelo con tamaño de contexto, la tabla no muestra proporciones.

![Tokens de la rama](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Qué entradas del lore se usaron

**Uso del lore** en el menú de ajustes (⚙, **Análisis**) indica qué entradas de los lorebooks del libro usaron las generaciones y cuáles no usó nunca,
así que un lorebook se puede depurar según lo que hace y no por suposiciones. Cada generación registra qué entradas entraron en su prompt
(y los tokens de cada una); la ventana suma esos datos sobre las partes:

- **Partes** elige qué se cuenta: la **Rama activa**, o **Todas las ramas**, cada rama y cada alternativa del libro.
- **Desde la parte** y **Hasta la parte** limitan el rango, numerado como en el esquema (la primera parte de una rama es la 1; en todas las ramas el número es la profundidad de la parte en el árbol); déjalos vacíos para el inicio y el final.
- **Mostrar** mantiene las entradas que estaban **Nunca usada**, **A veces usada** o **Siempre usada** en las partes contadas.

![La ventana Uso del lore](../../images/lore-usage.png)

La tabla tiene una línea por cada entrada de los lorebooks del libro, la más usada primero: su lorebook, si se usó nunca, a veces o siempre,
en cuántas partes (de las partes que tienen registro, con el porcentaje), los tokens que costó en todas ellas y por uso, y la primera y la
última parte que la usaron. Haz clic en un encabezado para ordenar. Una entrada que se usó antes pero que ahora no está en los lorebooks del libro (borrada, movida, o
cuyo lorebook está desactivado o ya no es del libro) aparece en la lista marcada como *ya no está en el lorebook*; una borrada no tiene nombre.

Solo se cuentan las partes que tienen registro: una parte escrita a mano, escrita antes de que Marginalia guardara este registro, o escrita cuando el libro no tenía
lorebook no tiene ninguno, y la ventana indica cuántas son. Las entradas *Siempre usada* podrían ser entradas constantes; las *Nunca usada* son candidatas a eliminarse o
a un filtro mejor; [**Vista previa de la activación**](../lorebooks/entries.md#preview-activation) en el editor del lorebook muestra por qué una entrada no se activa ahora.

<a id="text-styles"></a>

## Estilos de texto

El menú de ajustes (⚙) de la barra inferior tiene **Apariencia → Cambiar los estilos del texto**, un menú con una entrada por estilo, ordenadas por nombre.
Para saber cómo crear un estilo, consulta la [guía de estilos](styling-guide.md).
Un estilo decide cómo se ven los textos de las partes: tipografía, tamaño, colores, alineación, encabezados... La aplicación trae dos
estilos que no se pueden cambiar: **Normal** (el aspecto de la aplicación) y **Libro** (un estilo de lectura parecido a un libro, el que usa el
[visor](publishing.md)), y puedes crear el tuyo en [Ajustes → Estilos](../account.md#styles).

- La primera entrada, **Predeterminado (...)**, sigue tu *estilo predeterminado* (el marcado en *Ajustes → Estilos*, Normal si no elegiste ninguno). Un libro nuevo no tiene estilo propio y muestra ese.
- Elegir otra entrada guarda el estilo **en el libro**, de modo que reemplaza el predeterminado solo para ese libro; la entrada marcada es la que tiene ahora el libro. **Predeterminado** anula esa elección.
- Un estilo que borras pasa a ser tu predeterminado en los libros que lo usaban.

![Apariencia, Cambiar los estilos del texto: los estilos entre los que elegir](../../images/story-style-menu.png)

En el estilo **Libro** los párrafos están justificados y las palabras largas se separan automáticamente con guiones, usando el diccionario del
**Idioma** del libro (que se define en la pestaña Acerca de, [Acerca de](managing-books.md#the-about-tab)). La separación en sílabas la hace el navegador;
en unos pocos idiomas poco comunes no tiene diccionario y el texto simplemente no se separa.

Los libros que tenían activado el antiguo interruptor *Cambiar los estilos del texto* siguen mostrando el estilo Libro hasta que elijas otra entrada.

<a id="highlighting-generated-text"></a>

## Resaltar el texto generado

El menú de ajustes (⚙) también tiene **Resaltar el texto generado**, una opción que se puede marcar. Mientras está activada, las palabras que escribió un modelo
se resaltan en todas las partes, para que lo que añadiste o reescribiste destaque. Marginalia sigue el texto a través de las
[versiones de la parte](version-history.md): cada versión se superpone a la anterior, y las palabras que tienen en común conservan
a quien las escribió, así que una frase que escribiste dentro de un texto generado sigue siendo tuya por muchas veces que el modelo reescriba el resto. Una parte
que nunca editaste se resalta entera; una parte que escribiste tú no tiene nada resaltado. Lo que escribe un agente cuenta como del modelo.
La opción no se guarda: al abrir de nuevo la página vuelve a estar desactivada.

Una parte cuyo [historial está desactivado](version-history.md) no tiene versiones que seguir: se compara con el texto que generó el modelo
por primera vez. El resaltado también funciona en el [teléfono](../phone.md).

El mismo recuento da la proporción: la pestaña **Info** del libro tiene **Autoría de la rama** con un botón **Calcular**, que indica
qué porcentaje de las palabras de la rama activa escribiste tú y qué porcentaje generó un modelo, y
[la exportación](export.md) puede terminar el archivo con un resumen de ello.

![Texto generado resaltado y el menú de ajustes](../../images/highlight-generated.png)

<a id="commands"></a>

## Comandos

Junto al menú debajo de la historia hay una **línea de comandos**: escribe `/` para ver los comandos, elige uno y pulsa **Enter**
(o el botón de reproducción) para ejecutarlo. Lo que devuelve un comando se muestra en una pequeña ventana encima de la línea. Consulta
[Comandos](../commands/index.md) para saber cómo escribir los argumentos, qué muestra la ayuda y cómo detener un comando que tarda
mucho. Por ahora la línea de comandos solo está en la pestaña de la historia del diseño de escritorio.
<a id="search-and-replace"></a>

## Buscar y reemplazar

El menú de ajustes (⚙) también tiene **Borrar el historial del libro** y **Vaciar la papelera del historial**, véase [Historial de versiones](version-history.md#deleting-versions).

El menú de ajustes (⚙) también tiene **Buscar y reemplazar**, que cambia una palabra, un nombre o una frase en toda la historia de
una vez. La ventana tiene los campos **Buscar** y **Reemplazar por** y estas opciones:

![El menú de ajustes con Buscar y reemplazar](../../images/story-settings-menu.png)

![La ventana de Buscar y reemplazar con una búsqueda escrita](../../images/search-replace-dialog.png)

| Opción | Por defecto | Significado |
|---|---|---|
| **Buscar con expresión regular** | desactivada | El texto de búsqueda es una [expresión regular](../regular-expressions.md) y el reemplazo puede usar sus grupos (`$1`). Si no, el texto se busca tal como se escribe. |
| **Distinguir mayúsculas y minúsculas** | activada | Un texto se encuentra solo en la forma en que lo escribiste (*Juan* no encuentra *juan*). Desactivada: no importan las mayúsculas. Bloqueada mientras **Buscar con expresión regular** está activada. |
| **Buscar en los resúmenes** | activada | Los [resúmenes](summaries.md) también se buscan y se cambian. |
| **Buscar en todas las ramas** | desactivada | Se buscan todas las [ramas](branches-and-story-tree.md); si no, solo la activa. |
| **Revalidar todos los resúmenes** | activada | Después del reemplazo, cada [resumen](summaries.md) del libro vuelve a ser válido, véase más abajo. |

Con una expresión regular, las mayúsculas se toman siempre tal como se escriben; empieza con `(?i)` para ignorarlas. Pulsa **Buscar**
(el botón a la derecha de los campos; en el teléfono, debajo de ellos). La barra de progreso muestra cuánto falta y **Detener**
junto a ella la interrumpe. Una búsqueda no cambia nada.

La tabla enumera cada parte de la historia y cada resumen que tiene alguna coincidencia, con su tipo e id. Abre una fila para ver las
coincidencias que contiene: el texto alrededor de la coincidencia tal como está y tal como quedará después del reemplazo. Una coincidencia o un reemplazo
demasiado largos para mostrarse se escriben como *[texto buscado]* o *[texto de reemplazo]*.

![Las coincidencias de una búsqueda, con la primera parte abierta](../../images/search-replace-found.png)

Pulsar **Enter** en el campo **Buscar** o **Reemplazar por** hace lo mismo que **Buscar**.

Cuando la búsqueda termina, los campos y las opciones quedan bloqueados y el botón **Reemplazar** se activa. **Buscar de nuevo** olvida
lo encontrado y desbloquea la ventana, para que puedas cambiar la búsqueda. **Reemplazar** cambia la historia: la ventana queda completamente
bloqueada (ni **Detener** funciona) hasta que termina, y luego vuelve a estar vacía. Las partes reemplazadas se marcan como
editadas, como si las hubieras editado tú mismo.

Un reemplazo en el texto que describe un [resumen](summaries.md#when-summaries-become-outdated) dejaría el resumen desactualizado.
Con **Revalidar todos los resúmenes** activada, Marginalia vuelve a calcular el valor de comprobación de cada resumen del libro (los de
todas las ramas, también los que el reemplazo no tocó y los que ya estaban desactualizados), así que todos son válidos
después. Eso también vuelve válido un resumen desactualizado sin mirar su texto, así que úsalo cuando sepas que los resúmenes siguen diciendo lo correcto.

Con la opción desactivada, solo se actualizan los resúmenes que cubre la búsqueda. Los que no cubre (resúmenes de
ramas que no se buscan, que comparten sus partes anteriores con la rama activa; o todos ellos cuando los resúmenes no se buscan) quedan
desactualizados. Si hay algún resumen así, Marginalia te dice cuántos son y pregunta si continuar. Un
resumen que ya estaba desactualizado sigue así.

![La advertencia sobre resúmenes que quedarían desactualizados](../../images/search-replace-warning.png)

En el [diseño para teléfono](../phone.md) la misma ventana tiene los campos y los botones uno debajo de otro, y la tabla tiene una
columna que muestra el tipo y el id, y debajo de ellos la coincidencia y el reemplazo.

El [Resumen de resúmenes](summaries.md#search-and-replace-in-the-summaries) tiene la misma ventana, solo para los resúmenes.

La misma búsqueda y reemplazo puede ejecutarse desde la [línea de comandos](../commands/index.md#commands-that-come-with-marginalia)
con `/search-replace-parts`; pregunta antes de reemplazar.

<a id="importing-into-the-book"></a>

## Importar en el libro

El menú de ajustes (⚙) tiene **Importar** (en **Importar y exportar**). Es la ventana de la pestaña Libros que crea un libro nuevo a partir de un archivo (véase [Gestionar libros](managing-books.md#creating-a-book)), pero para el libro en el que estás: las partes se **añaden a la
historia** en su lugar. El **Origen** es uno de estos: el archivo zip de archivos `.md`, un documento (Word, LibreOffice, RTF, Markdown, texto plano,
EPUB), el zip de una bóveda de Obsidian o el zip de un proyecto de Scrivener; cada uno tiene el mismo texto y las mismas opciones que allí
(las palabras por parte y el nivel de título de los capítulos), y una línea indica lo que hará la importación. Los chats de SillyTavern
y de LM Studio no se ofrecen: son un árbol de ramas, que no tiene sitio en medio de una historia.

**Colocar las partes** indica dónde van:

- **Al final de la historia**: después de la última parte de la rama activa (la primera parte de un libro vacío).
- **Antes de una parte** o **Después de una parte**: elige la parte de la lista (los números son los lugares en la rama activa, con el
  inicio del texto). Las partes se colocan entre dos partes: todo lo que seguía al lugar, todas sus ramas, pasa a seguir a las nuevas
  partes, y ninguna parte se cambia ni se vuelve a generar. El final de la historia se queda donde estaba (la historia es esas partes más larga). 
  Los resúmenes que cubren el lugar quedan desactualizados, ya que describen la historia sin las nuevas partes: la ventana de
  [resúmenes](summaries.md) los muestra y los vuelve a generar.

![La ventana de importación de un libro](../images/book-import-into.png)

Las partes de un archivo son los ficheros en el orden de sus nombres, comparando los números dentro de los nombres como números (`2.md`
antes que `10.md`), también en carpetas del archivo. Se omiten las carpetas, los archivos ocultos (`.name`), la carpeta `__MACOSX` y los archivos que no son
`.md`. El archivo es el texto de la parte tal cual, no se envía al modelo ni se cambia; todas las partes cuentan como escritas por ti.

La importación muestra una barra de progreso con el número de partes hechas. **Detener** la termina después de la parte que se está guardando: las partes
que ya se añadieron se quedan en la historia (la importación no se deshace), y un mensaje indica cuántas se importaron.

<a id="after-an-import"></a>

### Después de una importación

Cuando las partes están en el libro, aparece una ventana **Importación terminada** que ofrece las dos cosas que hacen más fácil escribir una historia larga (un libro que
acaba de importarse como nuevo se abre primero, y la ventana aparece delante de él). Ambas también están en el menú de ajustes (⚙, **Análisis**), así que
pueden hacerse más tarde o de nuevo:

- **Crear resúmenes...** escribe los resúmenes de lo importado, véase [Crear resúmenes](summaries.md#create-summaries). Hazlo primero: un agente
  bloquea el libro mientras trabaja.
- **Tomar notas de lore con un agente...** inicia un [agente](../agents.md) que lee la historia y escribe los personajes, lugares, cosas y
  términos que encuentra como entradas del lorebook del libro, véase [Notas de lore](../agents.md#lore-notes-of-an-imported-book).

**Ahora no** cierra la ventana y no hace ninguna de las dos.

![La ventana después de una importación](../../images/import-finished.png)

La misma importación en un lorebook se describe en [Importar y exportar](../lorebooks/import-export.md#importing-an-archive-of-text-files).

<a id="exporting-the-story"></a>

## Exportar la historia

El menú de ajustes (⚙) también tiene **Exportar la historia**, que guarda la rama activa como un archivo de texto, Markdown, HTML, Word, PDF o EPUB.
Véase [Exportar un libro](export.md).

<a id="what-the-model-gets"></a>

## Lo que recibe el modelo

Para cada parte nueva, Marginalia envía:

1. La **plantilla maestra** como mensaje de sistema: rol, entradas del [lorebook](../lorebooks/index.md), punto de vista, tiempo verbal, estilo y [resúmenes](summaries.md). El prompt de jailbreak del
   [proveedor de inferencia](../inference-providers.md#general-settings), si está activado, va primero.
2. Las partes anteriores de la rama activa como respuestas previas del propio modelo, tantas como quepan en el
   [límite de contexto](../protocols.md#limits), de la más reciente a la más antigua. Las partes más antiguas que no caben se dejan fuera (salvo que un
   [resumen](summaries.md) las cubra).
3. El **prompt de usuario** con las instrucciones para la nueva parte.

*Mostrar el prompt* en cualquier parte muestra exactamente lo que se envió. Véase [Prompts del libro](prompts.md) para las plantillas.
