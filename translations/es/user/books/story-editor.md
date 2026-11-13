---
label: Editor de la historia
order: 90
---

# Editor de la historia

La pestaña **Historia** de un libro es donde escribes. Muestra las partes de la [rama activa](branches-and-story-tree.md)
desde el principio hasta el final.

![El editor de la historia](../../images/hero-story-editor.png)

Esta página describe la disposición de escritorio. En el teléfono la historia ocupa una sola columna y el esquema está en
un cajón; consulta [Usar Marginalia en el teléfono](../phone.md#the-story).

| Área | |
|---|---|
| **Esquema** (izquierda) | Una entrada por parte, `#1`, `#2`... con el ID de la parte en la base de datos. Haz clic en una entrada para desplazarte a la parte (las partes anteriores se cargan primero si aún no están cargadas). La cabecera muestra cuántas partes tiene la rama. Una parte que tiene un [resumen](summaries.md) muestra un pequeño icono de documento después de su entrada. La extensión [Chapter Marker](../extensions/chapter-marker.md) muestra aquí los títulos de los capítulos. |
| **Lorebook** (izquierda, junto al Esquema) | Una vista del lore del libro, con un cuadro de búsqueda y una edición rápida del texto de una entrada; consulta [La pestaña del lorebook](#the-lorebook-tab). |
| **Anotaciones** (izquierda, junto al Esquema) | Las [notas](annotations.md) sobre pasajes de la historia, para todo el libro. |
| **Planificador** (izquierda, junto al Esquema) | Las escenas que planeas escribir, una lista que rellenas a mano; consulta [La pestaña del planificador](#the-planner-tab). |
| **Historia** (centro) | El texto de cada parte, con su menú (☰) y sus metadatos a la derecha, y debajo la barra inferior. |
| **Barra inferior** | El botón **Resúmenes** (icono de lista) y el menú de ajustes (⚙) a la izquierda, la línea de comandos y el botón **+** a la derecha. |

Así se ve un libro nuevo. Todas estas son pestañas que puedes organizar como quieras, incluida la historia; consulta
[Organizar las pestañas](#arranging-the-tabs). La barra divisoria entre la barra lateral y la historia se puede arrastrar,
y Marginalia recuerda dónde la dejaste (se guarda con el libro) y vuelve a ese punto cuando abres el libro de nuevo.

La historia siempre se abre al final, en la parte más reciente. Un libro largo no se dibuja de golpe: solo se cargan las
últimas partes, y las anteriores se añaden arriba a medida que subes, sin mover la parte que estás leyendo. Las partes del
otro extremo se descargan, de modo que no permanecen cargadas más partes que las indicadas en el ajuste **Partes cargadas a la vez en la historia**
de tu [cuenta](../account.md) (si está vacío, se cargan todas). Si desplazas más rápido de lo que llegan las partes, una marca
*Cargando partes anteriores* (o *posteriores*) en el borde indica que hay más. Al hacer clic en una parte del esquema, o al buscar una
con el buscador, se cargan las partes que la rodean, y al empezar una parte nueva vuelves al final de la historia.

<a id="searching-the-story"></a>

### Buscar en la historia

Encima del esquema hay un campo de búsqueda para el texto de la **rama en la que estás** (la búsqueda del árbol de la historia
busca en todas las ramas). Como un libro largo no se carga de golpe, la búsqueda del navegador (Ctrl+F) no puede encontrar
texto en las partes que no están cargadas; esta sí puede.

- Escribe lo que buscas; la búsqueda empieza cuando dejas de escribir. Varias palabras deben estar todas en la misma parte, una frase
  va entre comillas dobles (`"llave antigua"`) y `*` representa cualquier secuencia de caracteres (`faro*`). No importan las mayúsculas.
- Cada lugar donde aparece una palabra es una coincidencia, y se numeran en toda la rama. Debajo del campo ves
  *Coincidencia 3 de 27, en la parte #5* y una barra con tu posición. La historia se desplaza a la coincidencia y carga las partes
  que la rodean si no están cargadas.
- Los botones **arriba** y **abajo** van a la coincidencia anterior y a la siguiente (después de la última vuelve a la primera).
  **Intro** en el campo hace lo mismo que el botón de abajo, **Mayús+Intro** lo mismo que el de arriba.
- Todas las coincidencias de las partes cargadas aparecen resaltadas en el texto, y la coincidencia en la que estás, con un color más intenso.
  Borra el campo para quitar el resaltado.

En el teléfono la búsqueda está en el cajón del esquema; el cajón se cierra cuando muestra una coincidencia, así que ábrelo de nuevo para la siguiente.

<a id="the-lorebook-tab"></a>

### La pestaña del lorebook

La pestaña **Lorebook** de la barra lateral muestra el lore del lorebook del libro sin salir de la historia, de modo que no tienes
que cambiar de pestaña para comprobar lo que dice una entrada o para corregir una palabra en ella. Se vuelve a leer cada vez que regresas a la pestaña *Historia*, así que
los cambios que hiciste en la pestaña *Lorebook* del libro se muestran al momento.

- El **cuadro de búsqueda** de arriba muestra solo las entradas que tienen el texto en su nombre, contenido, nota o filtro (no importan las mayúsculas).
- El **lápiz** de una entrada abre una pequeña ventana con su nombre, si está en uso, el filtro y la forma en que se busca, el contenido
  y la nota; **Guardar** los escribe (las etiquetas, el orden y los ajustes de activación se cambian en la pestaña *Lorebook* del libro). No se puede
  cambiar nada mientras un agente trabaja en el lorebook.
- El **botón de expandir** (⤢) abre el [editor de lorebook a pantalla completa](../lorebooks/lorebooks.md#editing-in-full-screen) (solo en escritorio): el que está junto
  al cuadro de búsqueda abre el lorebook, el de una entrada abre esa entrada, y el de un sublorebook (con *Incluir sublorebooks*) abre ese lorebook. La pestaña se vuelve a cargar cuando cierras el editor.

- Cada entrada es una fila con su nombre y el lápiz; la flecha la abre para mostrar su **contenido** (renderizado como Markdown) y el **texto que la activa** (el
  *Texto o patrón de filtrado*, con una nota cuando es una expresión regular).
- **Incluir sublorebooks** muestra el lorebook y cada uno de sus sublorebooks (y los de estos, a cualquier profundidad) como filas propias, con sus entradas
  debajo; haz clic en la flecha de un lorebook para ocultar sus entradas. Sin esta opción solo se listan las entradas del lorebook del libro.
- Las entradas y los lorebooks desactivados (que no se usan en la historia) se muestran atenuados.
- Un libro sin lorebook muestra una nota en su lugar.

![La pestaña Lorebook con dos entradas abiertas](../../images/story-lorebook-tab.png)

Como cualquier pestaña del editor, puede arrastrarse hacia un panel propio; consulta más abajo.
<a id="the-planner-tab"></a>

### La pestaña del planificador

La pestaña **Planificador** es una lista de las escenas por las que quieres que avance la historia, en el orden que quieras. Cada escena son los datos de un turno que la escribe: un **título** para la lista, el **escenario de la escena**, el **personaje de punto de vista**, los **personajes presentes** y las **instrucciones**, los mismos cuatro campos que el formulario del nuevo turno.

![La pestaña Planificador con cuatro escenas, la primera ya escrita](../../images/planner-tab.png)

- **Añadir escena** abre un formulario para una escena nueva; se añade al final de la lista. El lápiz edita una escena, las flechas la mueven arriba o abajo, el cubo de la basura la elimina.
- El **+** de la fila de una escena **inserta una escena nueva** justo debajo de ella, entre esa escena y la siguiente.
- El botón de combinar de una escena **la combina con la siguiente**: se abre un formulario con los datos de ambas ya unidos (los dos títulos, los escenarios y las instrucciones uno debajo de otro, los personajes presentes de las dos y el personaje de punto de vista de la primera; cuando la segunda escena tiene otro, el formulario lo indica). Cambia lo que no encaje y pulsa **Combinar** (o <kbd>Ctrl/Cmd+Enter</kbd>): las dos escenas se convierten en una, en el lugar de la primera. Solo se hace si ambas estaban escritas.
- **Importar desde la rama** añade una escena por cada parte de la rama en la que está el libro (desde la primera parte hasta la más reciente) que tenga datos de turno: su escenario, el personaje de punto de vista, los personajes presentes y las instrucciones, con la primera línea de las instrucciones como título. Las partes sin datos de turno se dejan fuera, y las escenas se añaden después de las que ya tienes, sin marcar como hechas. Úsalo para planificar la reescritura de una historia que ya has escrito: importa, combina los turnos que pertenecen juntos, inserta lo que falte y después escribe las escenas en una rama nueva o en una copia del libro.
- La casilla delante de una escena indica que está **hecha** (escrita): el título aparece tachado y la escena se atenúa. Nada lo comprueba por ti; márcala cuando hayas escrito la escena. La línea sobre la lista cuenta cuántas están hechas.
- **Haz clic en una escena** para pasar sus datos al **formulario del nuevo turno** (el formulario se abre con el escenario, el punto de vista, los personajes presentes y las instrucciones de la escena; lo que hubiera se sustituye) y pulsa **Generar** cuando estés conforme. En el teléfono, el cajón se cierra primero.
- Las escenas se guardan con el libro, así que están en sus copias de seguridad y en sus copias. Un modelo también puede añadir escenas: el [asistente de trama](../extensions/arc-outline.md#developing-the-plot-in-a-conversation) de la extensión Arc Outline y un [agente](../agents.md) pueden leer y cambiar el planificador con [comandos](../commands/index.md#the-planner); la lista muestra al momento lo que hayan añadido.

![El formulario de una escena nueva](../../images/planner-scene-dialog.png)

![Un clic en una escena rellena el formulario del nuevo turno con sus datos](../../images/planner-new-turn.png)

Como cualquier pestaña del editor, puede arrastrarse fuera y convertirse en un panel propio; véase más abajo.

<a id="arranging-the-tabs"></a>

### Organizar las pestañas

El editor es un conjunto de pestañas: la **Historia** en sí (las partes y la barra inferior bajo ellas), el **Esquema**, el **Lorebook** y las pestañas que añaden las [extensiones](../extensions/index.md) (Personajes, Arcos, Consulta lateral...). Arrastra una pestaña por su cabecera para reordenarlas (solo en escritorio; el teléfono mantiene una columna con el esquema en un cajón). Un libro nuevo tiene el Esquema y el Lorebook en un panel a la izquierda y la Historia en una columna propia a la derecha; puedes poner la historia donde quieras, por ejemplo en el centro, con el esquema a un lado y el lore al otro, o encima del esquema:

- Suéltala sobre una de las franjas discontinuas que aparecen encima, entre y debajo de los paneles de una columna para **separarla** en un panel propio dentro de esa columna; los paneles de una columna se apilan unos debajo de otros.
- Suéltala sobre una de las franjas altas de la izquierda, la derecha o entre las columnas para **separarla a un lado**: tendrá una columna propia, junto a las demás. Amplía el editor con los divisores para hacer sitio a más columnas.

Los paneles y las columnas están separados por divisores: arrástralos para dar a cada panel el espacio que necesite. Los tamaños se guardan con la disposición.

![La barra lateral con Personajes en una columna propia y Consulta lateral debajo](../../images/story-editor-tabs.png)

- Suéltala sobre la cabecera de otra pestaña o sobre el cuerpo de otro panel para **unirla** a ese panel (si la sueltas sobre una pestaña, queda antes de esa pestaña; si la sueltas sobre el cuerpo, queda al final). Un panel que pierde su última pestaña desaparece, y también desaparece una columna sin paneles.

La disposición y los tamaños se guardan con el libro, así que son los mismos la próxima vez que lo abras y en cualquier navegador; la pestaña que muestra cada panel se recuerda junto con el resto de tu espacio de trabajo. Una pestaña de una extensión que no esté cargada conserva su sitio hasta que la extensión vuelva.

La historia siempre está en algún sitio: su pestaña no se puede cerrar, y una pestaña que comparte panel con otras queda oculta mientras se selecciona otra. Cualquier cosa que muestre una parte devuelve la historia a primer plano: al hacer clic en una entrada del esquema, en un resultado de la búsqueda, al elegir una rama en el [árbol de historias](branches-and-story-tree.md) o al empezar una generación, se selecciona primero la pestaña **Historia**. Mientras una generación prepara su prompt (normalmente unos segundos) las demás pestañas están deshabilitadas; cuando el modelo empieza a escribir vuelven a estar disponibles, véase [más abajo](#while-a-part-is-generated).

<a id="minimizing-tabs"></a>

### Minimizar pestañas

Cada pestaña, salvo la de la historia, tiene un pequeño botón **⌄** (chevron) junto a su nombre. Sirve para **minimizar** la pestaña: la pestaña sale de su panel (un panel cuyas pestañas están todas minimizadas desaparece de momento) y aparece un icono redondo de ella, un círculo de color con un dibujo (una lista para el esquema, un libro para el lorebook, una burbuja de diálogo para las anotaciones, una lupa para Consulta lateral, etc.; una pestaña de una extensión que no tenga dibujo recibe una o dos letras), en una barra en el borde del editor. Pasa el ratón sobre el icono para ver qué pestaña es. No se cierra nada: la pestaña conserva lo que estaba mostrando. Haz clic en el icono y la pestaña vuelve al panel, al lugar entre las pestañas donde estaba, y se muestra.

La barra está presente mientras haya alguna pestaña minimizada. El botón de su parte superior (las flechas circulares) envía la barra al siguiente lado del editor, en el sentido de las agujas del reloj: izquierda, arriba, derecha, abajo. La historia no se puede minimizar, así que siempre hay algo en pantalla.

![Pestañas minimizadas en la barra del borde del editor](../../images/story-minimized-tabs.png)

Los iconos están en el orden alfabético de los nombres de las pestañas hasta que lo cambies: arrastra un icono sobre otro y ocupará el lugar de este. Qué pestañas están minimizadas, dónde está la barra y el orden de los iconos se guardan con el libro, junto con la disposición y los tamaños.

<a id="the-default-arrangement"></a>

### La disposición predeterminada

Un libro nuevo se abre con la disposición estándar (el esquema, el lorebook y las anotaciones a la izquierda, la historia a la derecha). Para hacer de tu disposición la estándar, ajusta las pestañas como quieras (paneles, tamaños, pestañas minimizadas, lado de la barra) y elige **Guardar la disposición como mi predeterminada** en el menú de ajustes (⚙) de la historia. Todo libro que no tenga disposición propia (un libro que no has reorganizado) se abrirá entonces con ella; un libro que hayas organizado conserva la suya. La predeterminada se guarda en tus ajustes de usuario.

Para volver a la disposición predeterminada, elige **Restaurar la disposición predeterminada** en el menú de ajustes (⚙) de la historia y confírmalo. Es la que guardaste, o la estándar si no guardaste ninguna. Solo devuelve las pestañas a su sitio (y olvida los tamaños y lo que esté minimizado): tus textos, la pestaña que muestra cada panel y las pestañas de las extensiones se mantienen, estas últimas en el primer panel.
<a id="writing-the-next-part"></a>

## Escribir la siguiente parte

1. Haz clic en **+** en la esquina inferior derecha. Se abre el panel *Instrucciones del nuevo turno*.
2. Rellena los campos. Todos son opcionales, pero el modelo necesita al menos algo de orientación:

   | Campo | |
   |---|---|
   | **Ambientación de la escena** | Dónde y cuándo transcurre la parte, p. ej. *El faro, una noche de tormenta a finales de otoño*. |
   | **Personaje del punto de vista** | Desde el punto de vista de quién se escribe la parte. |
   | **Personajes presentes** | Quién está en la escena, y en qué estado. |
   | **Instrucciones** | Qué debe ocurrir en esta parte. |

   *Ambientación de la escena*, *Personaje del punto de vista* y *Personajes presentes* se rellenan con la última parte, así que solo cambias
   lo que sea distinto. *Instrucciones* empieza vacío cada vez.
3. Haz clic en **Generar**.

![El panel Instrucciones del nuevo turno](../../images/new-turn-instructions.png)

La nueva parte aparece al final y se va rellenando mientras el modelo la escribe. Para [modelos de razonamiento](../inference-providers.md#reasoning-models)
el razonamiento aparece primero, en **Ver el razonamiento**.

Mientras se genera una parte, el botón **+** se convierte en un botón rojo **Detener**. Detener conserva el texto escrito hasta
ese momento como parte.

<a id="while-a-part-is-generated"></a>

### Mientras se genera una parte

Durante los primeros segundos, mientras el prompt se construye a partir del libro, toda la ventana del libro espera. En cuanto el modelo empieza
a escribir, solo quedan retenidas la nueva parte y las partes anteriores a ella:

- **La nueva parte** no se puede tocar hasta que termine: ni editar ni abrir su menú.
- **Las partes anteriores** siguen en su sitio: puedes editarlas, anotarlas y redactarlas de nuevo, pero no borrarlas, fusionarlas ni ramificarlas, y
  la historia se mantiene en su rama (no hay deslizamiento ni cambio de rama). Un cambio en una parte anterior, o en el lore, no llega a la parte
  que se está escribiendo: su prompt ya se había construido. Cuenta para lo que venga después.
- **Todo lo demás está libre**: el lore, los ajustes y el prompt del libro, las pestañas de la barra izquierda (consulta lateral, esquema del arco, notas...),
  la barra de comandos. Una petición al modelo espera una conexión libre con el modelo si la generación tiene la única.
- **En espera**: un segundo turno, buscar y reemplazar, una importación en el libro y la eliminación masiva. Las pestañas de árbol y de copias de seguridad también esperan.

Lo que la ventana atenúa también se rechaza cuando un comando, un agente o una extensión lo solicita: Marginalia indica qué lo impide
(*Esto está en uso: …*).

**Salir del libro detiene la generación.** El botón de salida pregunta antes; *Sí* equivale a **Detener**: lo que llegó se conserva. Cerrar
la pestaña del navegador hace lo mismo, y el navegador pregunta antes de cerrar o recargar la pestaña mientras se genera una parte. Una pérdida breve
de red, o un portátil cerrado un minuto, no cuenta como salir: la página vuelve y muestra lo que llegó mientras tanto.

Cuando el modelo devuelve un error (servidor no accesible, clave de API incorrecta, contexto demasiado largo...), el error se muestra.
Si todavía no llegó texto, la historia queda como estaba: una parte nueva o un deslizamiento se elimina, y una parte regenerada recupera
su texto anterior. El texto que llegó antes del error se conserva, como con **Detener**. Soluciona el problema y
genera de nuevo.

La forma en que los campos entran en el prompt la define el [Prompt de usuario](prompts.md#user-prompt) del libro. Con el prompt de usuario
por defecto, el modelo los recibe como *Ubicación y tiempo actuales*, *Personaje del punto de vista*,
*Personajes presentes / estado* y *Especificaciones de la trama para la siguiente parte*.

!!!
El texto se escribe en Markdown: `*cursiva*`, `**negrita**`, `# Título` y demás se muestran con formato. Una parte con un título `#`
es un capítulo para la extensión [Marcador de capítulo](../extensions/chapter-marker.md).
!!!

<a id="drafting-the-instructions"></a>

### Redactar las instrucciones

¿No sabes qué debería pasar después? **Redactar instrucciones** debajo del campo *Instrucciones* del panel permite que el modelo escriba las instrucciones en tu lugar, como lo harías tú. Se abre una ventana con:

| | |
|---|---|
| **Orientación** | De qué debería tratar la parte: una idea general, una dirección («ella lo confronta por la carta»), o nada, y el modelo continúa la historia por donde lleve. Empieza con lo que ya escribiste en *Instrucciones*. |
| **Plan de la historia** | Solo cuando algo lo proporciona. El [Esquema del arco](../extensions/arc-outline.md#drafting-the-instructions-for-the-current-chapter) lo hace, con los datos del capítulo actual. Solo lectura; el modelo lo sigue. |

**Redactar instrucciones** inicia el modelo y va volcando la respuesta en **Instrucciones redactadas**, que puedes cambiar o vaciar
antes de continuar; **Redactar de nuevo** lo intenta una vez más, **Detener** conserva lo que haya llegado. No se cambia nada hasta que haces clic en
**Usar como instrucciones**: el texto sustituye entonces las *Instrucciones* del panel (los demás campos no se tocan) y el panel se abre
de nuevo, listo para **Generar**.

El modelo recibe la historia hasta ahora (la parte más antigua como [resúmenes](summaries.md), como en una generación), la ambientación de la escena, el punto de vista y los
personajes presentes que estén en el panel, el lore de la última parte, tus últimas instrucciones como ejemplos de cómo las escribes, tu orientación y el
plan. No ve las entradas de tu libro de lore que activaría un nuevo turno. La petición es el
[prompt de suplantación](prompts.md#impersonate-prompt). El modelo es el del libro; redactar consume tokens como cualquier petición.

![Redactar las instrucciones del siguiente turno](../../images/impersonate-dialog.png)

Lo mismo hace el comando [`/impersonate`](../commands/index.md), que devuelve el texto. Los [agentes](../agents.md) no lo reciben, escriben
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

Los elementos que traen las [extensiones](../extensions/index.md) están en el submenú que les corresponde, o al final del menú. Las teclas de los elementos están en
*Ajustes → Atajos de teclado*, donde un elemento se nombra con su submenú (*Análisis › Estadísticas*). Las secciones siguientes indican dónde está cada uno.
<a id="the-part-menu"></a>

## La menú de la parte

Cada parte tiene un menú (☰) en su esquina superior derecha. **Editar**, **Descartar los cambios**, **Regenerar**, **Variante**, **Reescribir**, **Historial** e **Imágenes** están en el propio
menú, **Eliminar** es su último elemento, y el resto está en cuatro submenús: **Anotaciones** (*Anotar la parte*, *Anotar con IA*), **Resumen**
(*Generar resumen* / *Ver resumen*, *Eliminar resumen*), **Inspeccionar** (*Mostrar el prompt*, *Comparar prompt*, *Uso de tokens*, *Uso del lore*) y **Ramas y fusión** (*Crear una rama*,
*Combinar con la parte anterior*). Un submenú sin nada para la parte no se muestra. El Revisor añade su submenú **Revisar** después de *Resumen*. Los elementos:

| Elemento | |
|---|---|
| **Editar** | Convierte el texto en un campo editable. **Guardar** (el mismo elemento del menú) lo guarda. Iniciar una generación o salir de la pestaña también guarda las ediciones abiertas. |
| **Descartar los cambios** | Solo mientras el texto está en edición: cierra el campo sin guardar, la parte conserva el texto con el que se guardó por última vez. Si cambiaste el texto, se te pregunta antes. |
| **Regenerar** | Escribe de nuevo la última parte, reemplazando su texto. Solo en la última parte. |
| **Variante** | Escribe otra versión de la última parte y conserva la anterior como una rama separada. Solo en la última parte. Consulta [Ramas y árbol de la historia](branches-and-story-tree.md#trying-another-version-of-the-last-part). |
| **Reescribir** | Permite que el modelo vuelva a escribir esta parte a partir de tus instrucciones, en cualquier parte, consulta [más abajo](#redrafting-a-part). No mientras el texto está en edición. |
| **Historial (n)** | Las [versiones del texto](version-history.md) de la parte, con el número de versiones: compararlas, restaurar una o darle nombre a una. No mientras el texto está en edición. |
| **Anotar la parte** | Crea una [nota](annotations.md#making-an-annotation) sobre la parte entera. Para anotar palabras, selecciónalas y haz clic derecho (**Anotar la selección**). |
| **Anotar con IA** | Permite que un modelo [revise la parte](annotations.md#letting-a-model-annotate) y proponga notas. No mientras el texto está en edición. Para varias partes a la vez usa **Anotar un intervalo de partes con IA** en el menú de ajustes (⚙), y para cada parte nueva **Anotación automática** (consulta [Anotaciones](annotations.md#annotating-every-new-part-automatically)). |
| **Crear una rama** | Inicia una nueva rama desde esta parte, consulta [Ramas y árbol de la historia](branches-and-story-tree.md#branching-from-an-earlier-part). |
| **Combinar con la parte anterior** | Añade el texto de esta parte a la parte que la precede, consulta [más abajo](#merging-a-part-into-the-previous-one). No disponible en la primera parte. |
| **Mostrar el prompt** | Muestra el prompt exacto que se envió al modelo para esta parte. |
| **Uso de tokens** | Muestra cómo se usó el contexto del modelo para esta parte: un gráfico de las secciones del prompt (instrucciones del modelo, lore, resúmenes, historia hasta el momento, tus instrucciones, espacio reservado para la respuesta) y el tamaño de cada mensaje enviado al modelo. Si parte de la historia no cabía, indica cuántas partes se dejaron fuera. Las partes escritas antes de que esto se registrara no tienen gráfico. |
| **Uso del lore** | Enumera las entradas del [libro de lore](../lorebooks/index.md) que entraron en el prompt de esta parte, con los tokens que costó cada una y lo que la encontró (el prompt, o el contenido de otra entrada cuando el libro de lore busca más a fondo). Las partes escritas a mano, escritas antes de que esto se registrara o escritas para un libro sin libro de lore indican que no se registró nada. Para todo el libro usa **Uso del lore** en el menú de ajustes (⚙), consulta [más abajo](#which-lore-entries-were-used). |
| **Imágenes** | Adjunta imágenes a la parte, consulta [Imágenes](#images). |
| **Generar resumen** / **Ver resumen** | Resume la historia hasta esta parte, o muestra el resumen, consulta [Resúmenes](summaries.md). |
| **Eliminar resumen** | Quita el resumen de esta parte. Para un [meta-resumen](summaries.md#meta-summaries) pregunta si deshacerlo o eliminarlo por completo. |
| **Eliminar** | Elimina la parte, consulta [más abajo](#deleting-a-part). |

*Regenerar* y *Variante* usan los detalles del turno de la última parte (consulta [más abajo](#turn-details)). Para cambiar lo que
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
| **Detalles del turno** | Las instrucciones desde las que se escribió la parte. |

<a id="turn-details"></a>

### Detalles del turno

**Detalles del turno** abre la *Ambientación de la escena*, el *Personaje del punto de vista*, los *Personajes presentes* y las *Instrucciones* desde las que se escribió la
parte. Para la última parte pueden cambiarse (los cambios se guardan de inmediato) y los usan *Regenerar* y
*Variante*. Para las partes anteriores son de solo lectura.

![Detalles del turno de una parte](../../images/turn-details.png)

<a id="editing-the-text"></a>

## Editar el texto

El texto generado es tuyo para cambiarlo. Elige **Editar** en el menú de la parte, cambia el texto y elige **Guardar**. Las
partes editadas se envían al modelo tal como las editaste.

Editar una parte que cubre un [resumen](summaries.md) deja ese resumen desactualizado; Marginalia lo advierte en la siguiente generación y descarta el resumen. Para que esto no ocurra por sorpresa, **Guardar** revisa primero el nuevo texto: cuando dejaría resúmenes desactualizados, aparece una ventana que indica cuántos son y ofrece **Volver a la edición** (no se guarda nada, el editor queda abierto con tu texto), **Guardar de todos modos** (los resúmenes quedan desactualizados) y **Guardar y revalidar los resúmenes** (el texto se guarda y los resúmenes siguen válidos, para un cambio que no altera lo que dicen, como una errata). La comprobación se hace al usar **Guardar** (el menú, el atajo y el botón del editor); los guardados automáticos, por ejemplo al iniciar una generación con el editor abierto, no preguntan.

<a id="keyboard-shortcuts"></a>

## Atajos de teclado

En un ordenador (la [vista de teléfono](../phone.md) no los tiene) el ciclo de escritura funciona con el teclado:

| Tecla | |
|---|---|
| `+` | Abre el panel *Instrucciones del nuevo turno* con el cursor en *Instrucciones*. |
| `Ctrl+Enter` | Genera desde el panel abierto. `Esc` lo cierra; el teclado vuelve a la historia. |
| `Ctrl+.` | Detiene la generación en curso. |
| `s` / `r` | **Variante** / **Regenerar** la última parte. |
| `e` | Abre el editor de la parte que más se ve en pantalla (la última parte si ninguna). |
| `Ctrl+S` | Guarda la parte que se está editando (también mientras el cursor está en su texto). |
| `/` | Coloca el cursor en la barra de comandos (primero se muestra la pestaña de la historia). |
| `Ctrl+Shift+F` | Abre la pestaña *Esquema* y coloca el cursor en la búsqueda de la rama (`Enter` / `Shift+Enter` van a la siguiente / anterior coincidencia). |
| `Ctrl+Shift+H` | Abre la ventana *Buscar y reemplazar* del libro. |
| `a` | **Anotar la selección**: crea una nota sobre el texto seleccionado en una parte. |
| `Alt+AvPág` / `Alt+RePág` | Muestra la siguiente / anterior pestaña, recorriendo las pestañas de todas las hojas en el orden de la disposición y volviendo al inicio. |

Una tecla sin `Ctrl`, `Alt` o `Meta` (como `+`, `s`, `r`, `e`, `a`, `/`) funciona solo cuando ningún campo de texto tiene el foco, para que nunca interfiera al escribir. Los atajos funcionan en la pestaña de la historia del libro abierto y no mientras otra ventana esté abierta encima. Los botones muestran su tecla en la información emergente, y el menú de la última parte la muestra después de *Editar*, *Regenerar* y *Variante*. En un Mac, `Cmd` funciona donde una tecla dice `Ctrl` (salvo que hayas asignado esa tecla `Cmd` a otra acción). Puedes cambiar cada tecla en [Ajustes → Atajos de teclado](../account.md#keyboard-shortcuts).
<a id="deleting-a-part"></a>

## Eliminar una parte

**Delete** elimina la parte de la historia. Las partes que venían después se mantienen y continúan desde la parte anterior, así que
eliminar una parte en medio de la historia solo la quita. Eliminar la última parte hace que la parte anterior pase a ser el nuevo
final de la rama.

Nota: la etiqueta **Delete** aparece en la aplicación como **Eliminar**.

<a id="deleting-many-parts-at-once"></a>

### Eliminar varias partes a la vez

El menú de ajustes (⚙) tiene **Eliminación masiva**. Al activarlo aparece una casilla a la izquierda de cada parte y una barra debajo
de la historia con el número de partes marcadas y un botón **Eliminar**. Marca las partes que quieres eliminar (las marcas se conservan
al desplazarte, porque la historia carga solo una ventana de partes); **Mayús + clic** en una casilla marca (o desmarca) todas las partes
comprendidas entre ella y la que pulsaste antes; una parte marcada se muestra con un tono rojo. Después pulsa **Eliminar**: tras una confirmación,
las partes se eliminan una a una detrás de una barra de progreso que no se puede detener. Se eliminan igual que se elimina una sola parte: las partes que
seguían a una parte eliminada continúan desde la parte anterior, y las partes eliminadas van a la papelera. Después se vuelve a mostrar la historia y la
eliminación masiva se desactiva. **Cancelar** en la barra, o elegir **Eliminación masiva** de nuevo, la desactiva sin eliminar nada. Iniciar una generación
también la desactiva. Una parte marcada que una ventana o una traducción esté usando permanece en la historia, y Marginalia indica qué la está usando.

![Eliminación masiva con algunas partes marcadas](../../images/story-mass-delete.png)
<a id="redrafting-a-part"></a>

## Rehacer una parte

**Regenerar** y **Variante** trabajan solo sobre la última parte y la escriben desde cero. **Reescribir** trabaja sobre cualquier parte, también una antigua en medio de la historia, y conserva lo que no pides cambiar. Elige **Reescribir** en el menú de la parte:

1. Escribe lo que debe cambiar en **Instrucciones para la reescritura**: *añade más diálogo entre los dos*, *haz la pelea más larga*, *elimina la explicación del mapa*, *mantén el final como está*. Si las instrucciones están vacías, la parte se vuelve a escribir con los mismos hechos.
2. Elige **Extensión** si la parte debe quedar **Más corto** o **Más largo** (por defecto, **La misma extensión**): se añade una frase que lo pide después de tus instrucciones (ver [Prompt de reescritura más corta y más larga](prompts.md#redraft-shorter-and-longer)). Elige **Reescribir**. El texto aparece en **Texto reescrito** y **Detener** lo interrumpe antes de tiempo. Elige **Reescribir de nuevo** para intentarlo otra vez, con otras instrucciones si quieres. **Diferencias** muestra qué ha cambiado respecto a la parte, palabra por palabra. Si la parte tiene [anotaciones](annotations.md) abiertas, **Atender las anotaciones abiertas de la parte** (marcada por defecto) también se las entrega al modelo, y **Aplicar** las marca como resueltas.
3. El texto reescrito es un campo que puedes seguir editando (su icono de esquina lo hace crecer para mostrar todo el texto), y **Diferencias** sigue tus cambios. **Aplicar** sustituye el texto de la parte por él; **Cerrar** lo descarta. Nada del libro cambia hasta que pulses **Aplicar**.

![La ventana de reescritura: las instrucciones, el texto nuevo y las diferencias](../../images/redraft-dialog.png)

El modelo recibe los [prompts de reescritura](prompts.md#redraft-prompt): la parte, tus instrucciones, la historia anterior a la parte (la historia más antigua como [resúmenes](summaries.md) cuando hay resúmenes válidos, el resto como texto, empezando por la parte más reciente si no cabe todo), tanta historia posterior a la parte como quepa (hasta un cuarto del espacio disponible), el lore que los libros de lore dan ahora a esa parte (calculado de nuevo para el turno de la parte, de modo que una edición de una entrada llega a la reescritura; el lore con el que se escribió la parte solo se usa si el libro no tiene libro de lore), el punto de vista, el tiempo verbal y el estilo del libro, y la escena, el personaje de punto de vista, los personajes presentes y las instrucciones del turno del que procede la parte. La historia posterior a la parte está para que el nuevo texto siga conduciendo a ella: los hechos en que se apoyan las partes siguientes deben mantenerse.

La parte conserva su lugar, su resumen, sus imágenes y cómo se generó; el texto, sus palabras y sus tokens se sustituyen, y la parte cuenta como editada. Aplicar la reescritura es una edición, como **Guardar** en el editor, así que los [resúmenes](summaries.md) que describen el texto antiguo quedan desactualizados, y Marginalia indica cuántos. Un resumen de la propia parte es uno de ellos.

Cuando un resumen describe la parte (la parte tiene uno, o lo tiene una parte más nueva de la rama), la ventana muestra la casilla **Revalidar los resúmenes que esta reescritura deja desactualizados**, desmarcada al principio. Márcala cuando los resúmenes sigan diciendo lo correcto (la reescritura cambió la redacción, no lo que pasa): al **Aplicar**, se recalcula su hash y siguen siendo válidos. Déjala desmarcada cuando hayan cambiado los hechos, y haz los resúmenes de nuevo.

La ventana no bloquea la historia; ver [Trabajar junto a las ventanas de reescritura y anotación](#working-beside-the-redraft-and-annotation-windows).

<a id="working-beside-the-redraft-and-annotation-windows"></a>

### Trabajar junto a las ventanas de reescritura y anotación

En un ordenador, las ventanas **Reescribir** y **Anotar con IA** se colocan a la derecha de la historia y la dejan utilizable: puedes leer, desplazarte y editar la historia mientras el modelo escribe. Arrastra una ventana por su título para moverla, o por su borde para cambiar su tamaño. La flecha (⌄) del título pliega la ventana en el [panel de ejecuciones](#the-panel-of-the-runs), abajo a la izquierda de la página: el modelo sigue trabajando, **Abrir** (o un clic en la entrada) vuelve a mostrar la ventana, y **Detener** detiene el modelo (cuando ha terminado, **Cerrar** cierra la ventana). En un teléfono, las ventanas siguen tapando la historia, pero se pliegan igual.

![La ventana de reescritura junto a la historia mientras el modelo escribe, y la ejecución en el panel de abajo a la izquierda](../../images/redraft-beside-story.png)

**Cerrar y Detener son dos cosas distintas.** El propio **Detener** de la ventana detiene el modelo, y la ventana se queda con lo que llegó. **Cerrar** mientras el modelo trabaja no lo detiene: la ventana se pliega en el panel y el modelo sigue en su cola; su respuesta estará en la ventana cuando la vuelvas a abrir. Cierra una ventana cuyo modelo ha terminado, o ciérrala en el panel, y desaparece; cerrar el libro o la pestaña del navegador también detiene una ventana plegada. Lo mismo vale para las otras ventanas que esperan a un modelo: un **resumen** (y *Crear resúmenes*, *Reescribir todos los resúmenes*), el [Revisor](../extensions/reviewer.md), la descripción del libro, los generadores de lore y de esquema, los generadores de imágenes y de manga, y el redactor de personajes de Lectores beta. Las ventanas *Estudio* y *Convertir* del audiolibro tampoco bloquean la página; cerrarlas nunca detuvo la conversión.

Mientras una ventana está abierta (o plegada) sobre una parte, la parte muestra lo que hace a continuación junto a su menú (*Reescribiendo · El libro*), y **Regenerar**, **Variante**, **Reescribir**, **Anotar con IA**, **Fusionar con la anterior** y **Eliminar** están desactivados para esa parte, de modo que la ventana no pierde su parte. Editar la parte sigue siendo posible. Cerrar el libro cierra sus ventanas y detiene sus modelos. Una parte en la que trabaja un [traductor](../extensions/real-language-translator.md) muestra *Traduciendo el habla de la parte...* de la misma manera, y dos traductores nunca trabajan sobre una misma parte a la vez: el segundo indica que la parte está en uso.

- Cuando **Aplicas** la reescritura de una parte completa y la parte cambió después de abrir la ventana (la editaste mientras tanto), Marginalia pregunta antes: **Diferencias** compara entonces la parte tal como está ahora con la reescritura, para que veas lo que se sustituiría; elige **Aplicar de todos modos** o **Cancelar**. Una reescritura que [se aplica sola](annotations.md#annotating-every-new-part-automatically) no hace esto cuando la parte cambió: la ventana espera a que decidas tú.
- Cuando la parte se eliminó antes de que **Aplicaras** la reescritura o añadieras las anotaciones, la ventana lo indica, no escribe nada y se cierra.

<a id="the-panel-of-the-runs"></a>

### El panel de ejecuciones

Todo lo que trabaja ahora mismo con un modelo sobre tus libros aparece en la parte inferior izquierda de cada página: una generación, una reescritura o una anotación, una traducción del traductor [Real Language](../extensions/real-language-translator.md) o [Conlang](../extensions/conlang-translator.md), una reescritura de los resúmenes, la lectura de la historia por el Rastreador de conocimiento, una conversión de [Audiolibro](../extensions/audiobook.md), un comando de la barra de comandos que tarda (*Redactar instrucciones*, un resumen...: su ventana se queda a un lado y se pliega como las demás). También aparecen las ventanas en las que esperas una respuesta mientras el modelo responde: **Suplantar**, el asistente de ayuda, una traducción rápida, el diseñador de Conlang, un chat de Consulta lateral, Lectores beta o el asistente de la trama. Lista tus ejecuciones de **todas tus páginas abiertas** (otra pestaña, el teléfono), no solo las iniciadas en esta página. Si no hay nada en marcha, no se muestra nada.

![El panel de ejecuciones abierto: la reescritura plegada en él, con el final del texto del modelo, Abrir y Detener](../../images/runs-panel.png)

- **Cerrado**, es una pequeña barra: la primera ejecución, cuántas más hay (*Traduciendo el habla de la parte... · 2 más*) y una barra en movimiento mientras un modelo trabaja. Haz clic para abrir la lista; se abre sola cuando pliegas una ventana en él.
- **Abierto**, cada ejecución muestra lo que hace, el libro, cuánto tiempo lleva, lo que espera y el final de lo que escribe el modelo:
  - *Esperando al modelo: 2 peticiones antes que esta* cuando el modelo está ocupado con otras peticiones (ver *Conexiones* en [Proveedores de inferencia](../inference-providers.md));
  - *El modelo está pensando...* o *El modelo está escribiendo...*, con el final de su pensamiento o de su respuesta debajo;
  - *Te espera* cuando la ejecución espera una decisión tuya (una reescritura por aplicar, una propuesta del Rastreador de conocimiento por revisar, la pregunta del Inspector de prompts antes de una generación, un comando del asistente de la trama que hay que permitir).
- **Detener** detiene la ejecución, esté donde esté iniciada. **Abrir**, o un clic en la entrada, muestra de nuevo una ventana plegada (solo en la página en la que se abrió) o la propia ejecución; si la ejecución espera por ti, el botón es **Revisar**. Una ejecución del Rastreador de conocimiento abre su libro en esta página (cuando no hay otro libro abierto aquí; si lo hay, se te pide que cierres ese primero), cambia a la pestaña Conocimiento y muestra la revisión. **Cerrar** cierra una ventana definitivamente (y detiene su modelo).
- En un ordenador puedes mover el panel por su agarre (⋮ a su izquierda); se queda donde lo dejes en este navegador. En la mitad inferior de la página crece hacia arriba al abrirlo, como en su esquina, y siempre se mantiene dentro de la página. Cuando ocupa el lugar del botón redondo del [asistente de ayuda](../administration/help-assistant.md) (sobre un libro, ambos están en la esquina inferior izquierda), el botón se aparta y vuelve cuando el lugar queda libre.

Cuando una ejecución que iniciaste termina después de que se cerrara su página, otra página abierta tuya envía su [notificación](../account.md#notifications) (para las ejecuciones que la tienen, como la reescritura de los resúmenes).

<a id="redrafting-only-a-passage"></a>

### Reescribir solo un fragmento

Para reescribir unas pocas frases y dejar el resto de la parte intacto, selecciónalas en el texto de la parte y haz **clic derecho** (una pulsación larga en un teléfono): el menú tiene **Reescribir la selección**. (El menú ☰ de la parte no se ofrece para esto, porque al abrirlo se puede perder la selección en algunos navegadores.) La ventana funciona como arriba con dos diferencias: muestra el **Texto seleccionado**, y la reescritura es solo el texto que lo sustituye. El modelo recibe la parte completa con la selección marcada, y la historia y el lore alrededor, de modo que el nuevo fragmento se una a las frases anteriores y posteriores. **Diferencias** compara la selección con el texto reescrito; **Aplicar** pone el texto reescrito en el lugar de la selección y conserva el resto de la parte, incluidos el espacio o el salto de línea en los bordes de la selección. Con [anotaciones](annotations.md) abiertas, la casilla ofrece solo las que se solapan con la selección. Si la parte cambió después de que seleccionaras el texto (en otra ventana), **Aplicar** lo rechaza y tienes que volver a seleccionarlo. Mientras la parte está en edición se usa la selección del editor, y antes se guarda la edición.

<a id="what-follows-a-generated-part"></a>

### Lo que sigue a una parte generada

Cuando una parte que generaste (desde la página o desde la barra de comandos) está completa, el libro puede hacer más con ella por sí mismo, en este orden:

1. **Anotar**: la [anotación automática](annotations.md#annotating-every-new-part-automatically) revisa la parte.
2. **Reescribir**: la parte se vuelve a escribir para atender las notas.
3. **Traducir**: los traductores [Real Language](../extensions/real-language-translator.md) y [Conlang](../extensions/conlang-translator.md) traducen sus discursos.
4. **Conocimiento**: el [Rastreador de conocimiento](../extensions/knowledge-tracker.md) la lee.
5. **Audio**: el [Audiolibro](../extensions/audiobook.md) la convierte en audio.

Solo se ejecutan los pasos que el libro tiene activados. Se ejecutan en el servidor, así que la página puede cerrarse, y forman una sola entrada en el [panel de ejecuciones](#the-panel-of-the-runs): *Tras la parte 14: Anotar ✓ · Reescribir … · Traducir*, con el texto del modelo y un único **Detener**. Lo que cambia el texto va primero, de modo que el rastreador y el audio reciben el texto final. Un paso que espera por ti (notas o una reescritura que elegir) abre su ventana, y la entrada tiene **Revisar** para abrirla en otra página.

- **Detener**, cerrar una ventana que espera por ti, o un paso que falla hace que termine ahí: lo ya hecho se conserva, el resto no se ejecuta, y un mensaje indica lo que queda por hacer a mano (cada paso tiene su propia acción sobre la parte).
- Mientras la **reescritura** se ejecuta, no se puede generar la siguiente parte (se te indicará qué ejecución lo impide); mientras se ejecutan los otros pasos, sí.
- La parte no se puede eliminar hasta que su texto sea definitivo; volver a generarla termina lo que seguía a su versión anterior.
- Un [agente](../agents.md) hace estos pasos mediante sus propios comandos: nada sigue a una parte que generó un agente.
<a id="merging-a-part-into-the-previous-one"></a>

## Fusionar una parte con la anterior

Un modelo que se detuvo a mitad de una frase (el límite de respuesta, una conexión cortada) no puede recibir la orden de «continuar» el mismo texto de forma fiable. En su lugar, escribe la siguiente parte (*continúa donde se detuvo la última parte*) y une las dos con **Combinar con la parte anterior** en el menú de la segunda. Se abre una revisión:

- **Unir los textos con**: una línea vacía, un salto de línea, un espacio o nada. Viene preseleccionado según el final del texto anterior: un párrafo después de un texto que terminó una frase, y un espacio después de uno que quedó cortado.
- La **configuración de la escena**, el **personaje POV**, los **personajes presentes** y las **instrucciones** de ambos turnos, combinados en campos que puedes editar. Lo que los dos turnos tienen en común al principio se conserva una sola vez («Escribe el duelo. Que sea breve» y «Escribe el duelo. Añade lluvia» dan la primera línea, y «Añade lluvia» en la siguiente).
- **Datos**: lo que conservan las dos partes, valor a valor (por ejemplo, las [variables](../templates/macros.md) de la historia y los datos de las extensiones), indicando si el valor es el mismo, distinto o solo está en una de las partes. La parte fusionada conserva el valor de la **parte posterior** (es donde se encuentra ahora la historia), a menos que elijas para él el valor de la parte anterior o lo dejes fuera. Los valores iguales se ocultan salvo que los pidas.

![La revisión antes de fusionar una parte con la anterior](../../images/part-merge-dialog.png)

La parte anterior conserva su lugar, su resumen y cómo se generó (prompt, modelo, tiempos); recibe el texto unido, el razonamiento unido y las imágenes, y sus palabras y tokens se vuelven a contar. La parte que fusionaste se elimina (queda en la [Papelera](../trash.md)) y lo que la seguía continúa a partir de la parte anterior. Los resúmenes que cubren las dos partes quedan desactualizados, como después de una edición. El [historial](version-history.md#backups-copies-and-the-trash) de la parte fusionada se elimina, y la parte que sobrevive tiene el texto unido como una versión **Combinada**. No se puede fusionar una parte cuando es la primera del libro, ni cuando la parte anterior tiene otras continuaciones (variantes de la parte): elimínalas o conviértelas primero en una rama.

<a id="images"></a>

## Imágenes

Una parte puede tener imágenes: un mapa, el retrato de un personaje, una ilustración. Elige **Imágenes** en el menú de la parte para abrir la lista de imágenes de la parte.

![El diálogo de imágenes de una parte](../../images/images-dialog.png)

- **Añadir imagen** (o soltar un archivo encima) sube una imagen PNG, JPEG, GIF o WebP de hasta 10 MB. Las imágenes WebP se convierten a PNG al subirlas, porque los archivos de Word y PDF no pueden contener WebP.
- **Pie de imagen** se muestra debajo de la imagen. Puede estar vacío.
- **Posición en la parte** es una cuadrícula de 3x3. La columna es el lado del texto (izquierda, centro, derecha), y la fila es el lugar dentro de él: arriba es antes del texto, en medio es a mitad del texto, abajo es después de él. Haz clic en un cuadrado para elegirlo.
- **El texto rodea la imagen** hace que el texto corra alrededor de la imagen en lugar de ir encima y debajo de ella. A la izquierda o a la derecha, el texto corre junto a la imagen. En la columna central, el texto queda a ambos lados: unos pocos bloques del texto (unos 400 caracteres, dos bloques como mínimo) en esa fila pasan a dos columnas, la primera parte a la izquierda de la imagen y el resto a la derecha, de modo que el texto se lee bajando por la columna izquierda y continúa en la derecha. Dos imágenes en el mismo lugar van una encima de otra entre las columnas. Cuando el texto no tiene espacio para dos columnas (un único párrafo, o los bloques ya están ocupados por la franja de arriba), la imagen ocupa un bloque propio, centrado.
- **Ancho** es el ancho de la imagen como porcentaje del ancho del texto. **Automático** mantiene el tamaño de la imagen (nunca más ancha que el texto; una imagen con texto a ambos lados ocupa el 30 %). Una imagen junto a la que corre el texto ocupa como máximo el 60 %, y una con texto a ambos lados como máximo el 40 %, para que el texto conserve algo de espacio. La altura sigue las proporciones de la imagen.
- El botón de papelera quita la imagen de la parte.

![La posición de una imagen: en el centro, con el texto rodeándola](../../images/images-position.png)

**Vista previa como en el visor** (un botón del diálogo, y la misma entrada en el menú de la parte cuando tiene imágenes) muestra la parte tal como la muestra el [visor](publishing.md), con el estilo de texto del libro: las imágenes donde las colocaste, y un interruptor para una pantalla ancha o estrecha (teléfono). Usa el texto del editor tal como está ahora, guardado o no.

![La vista previa: el texto en dos columnas con la imagen entre ellas](../../images/images-preview.png)

El propio editor de la historia no usa la posición: muestra las imágenes debajo del texto de la parte, como una lista. La posición la usan el visor y las [exportaciones](export.md#images).

El lugar del texto donde va una imagen no depende de números guardados con ella: el texto se corta entre sus bloques (párrafos, listas, citas y bloques de código nunca se cortan), así que las imágenes siguen donde deben cuando editas el texto. El centro es el lugar entre bloques más cercano al centro del texto; una imagen junto a la que corre el texto en la parte inferior se coloca antes del último bloque, para que al menos ese bloque corra junto a ella. Las imágenes en el mismo lugar mantienen el orden de la lista; las que están una al lado de otra en el mismo lugar (y no dejan que el texto corra alrededor) se colocan en fila cuando caben. PDF y Word hacen eso con las imágenes que tienen ancho.

Las imágenes **no forman parte del texto**. El modelo nunca las ve, no están en el prompt, no cuentan como tokens y no cambian los [resúmenes](summaries.md). Tampoco son Markdown: un enlace `![](...)` escrito en el texto no es una imagen de la parte; usa **Imágenes** en su lugar.

Bueno saber:

- **Regenerar** conserva las imágenes de la parte, también cuando la generación falla. **Variante** escribe una parte nueva (una versión nueva junto a la anterior), que empieza sin imágenes. **Crear una rama** copia las imágenes a la nueva parte.
- Las imágenes pertenecen a tu cuenta. En el [visor](publishing.md), los lectores de un libro publicado también las ven, salvo que desmarques **Mostrar las imágenes a los lectores** en la pestaña *Información* del libro.
- Una [copia de seguridad](backups.md) recuerda qué imágenes tiene una parte, pero no los archivos de las imágenes. Restaurar una copia en otra cuenta o en otra instalación deja las partes sin sus imágenes.
- Quitar una imagen de una parte solo la saca de la parte. El archivo se conserva y sigue listado en [Recursos](../resources.md), donde puedes descargarlo, reemplazarlo o eliminarlo. Eliminarlo allí lo quita de todas las partes que lo usan. El archivo en sí se conserva en la carpeta de datos.

<a id="summaries"></a>

## Resúmenes

El botón **Resúmenes** de la barra inferior (a la izquierda del menú de ajustes ⚙) abre la vista general de los resúmenes de la rama, donde pueden editarse, eliminarse y fusionarse en metarresúmenes. La descripción emergente del botón muestra cuántos tokens ocupan en el prompt los resúmenes en uso. Consulta [Resúmenes](summaries.md#the-summaries-overview).

<a id="how-much-of-the-context-the-story-takes"></a>

## Cuánto del contexto ocupa la historia

El menú de ajustes (⚙) tiene **Tokens de la rama**. Muestra, antes de generar nada, cuánto ocupa la historia de la rama activa en el prompt: una barra para la historia **sin resúmenes** (todas las partes) y **con resúmenes** (los resúmenes y las partes posteriores al más reciente), y una tabla con las partes, los tokens y la proporción del **presupuesto del prompt** (el contexto del modelo del libro menos el espacio para la respuesta). Solo cuentan los resúmenes que todavía describen la historia; los que ya no coinciden se nombran, y se descartan al generar una parte. El lore, la plantilla del sistema y las instrucciones del turno ocupan espacio del mismo presupuesto. Sin un modelo con tamaño de contexto, la tabla no muestra proporciones.

![Tokens de la rama](../../images/branch-tokens.png)
<a id="which-lore-entries-were-used"></a>

## Qué entradas del lore se usaron

**Uso del lore** en el menú de ajustes (⚙, **Análisis**) indica qué entradas del lorebook del libro usaron las generaciones y cuáles no se usaron nunca,
para poder podar un lorebook según lo que hace y no por suposición. Cada generación registra qué entradas entraron en su prompt
(y los tokens de cada una); la ventana suma esos datos de las partes:

- **Partes** elige qué se cuenta: la **Rama activa**, o **Todas las ramas**, todas las ramas y todas las variantes del libro.
- **Desde la parte** y **Hasta la parte** limitan el rango, numerado como en el esquema (la primera parte de una rama es la 1; en todas las ramas el número es la profundidad de la parte en el árbol); déjelos vacíos para el principio y el final.
- **Mostrar** conserva las entradas que fueron **Nunca usada**, **A veces usada** o **Siempre usada** en las partes contadas.

![La ventana de Uso del lore](../../images/lore-usage.png)

La tabla tiene una línea por cada entrada de los lorebooks del libro, con las más usadas primero: su lorebook, si se usó nunca, a veces o siempre,
en cuántas partes (de las partes que tienen registro, con el porcentaje), los tokens que costó en todas ellas y por uso, y la primera y la
última parte que la usaron. Haga clic en un encabezado para ordenar. Una entrada que se usó antes pero que ya no está en los lorebooks del libro (borrada, movida, o
cuyo lorebook está desactivado o ya no es del libro) aparece en la lista marcada como *ya no está en el lorebook*; una borrada no tiene nombre.

Solo se cuentan las partes que tienen registro: una parte escrita a mano, escrita antes de que Marginalia guardara este registro, o escrita cuando el libro no tenía
lorebook no tiene ninguno, y la ventana indica cuántas son. Las entradas *Siempre usada* podrían ser entradas constantes; las *Nunca usada* son candidatas a eliminarse o
a un filtro mejor. [**Vista previa de la activación**](../lorebooks/entries.md#preview-activation) en el editor del lorebook muestra por qué una entrada no se activa ahora.

<a id="text-styles"></a>

## Estilos de texto

El menú de ajustes (⚙) de la barra inferior tiene **Apariencia → Cambiar los estilos del texto**, un menú con una entrada por estilo, ordenadas por nombre.
Para saber cómo crear un estilo, consulte la [guía de estilos](styling-guide.md).
Un estilo decide cómo se ve el texto de las partes: tipografía, tamaño, colores, alineación, encabezados... La aplicación trae dos
estilos que no se pueden cambiar: **Normal** (el aspecto de la aplicación) y **Book** (un estilo de lectura parecido a un libro, el que usa el
[visor](publishing.md)), y puede crear los suyos propios en [Ajustes → Estilos](../account.md#styles).

- La primera entrada, **Default (...)**, sigue su *estilo predeterminado* (el marcado en *Ajustes → Estilos*; Normal si no eligió ninguno). Un libro nuevo no tiene estilo propio y lo muestra así.
- Elegir otra entrada guarda el estilo **en el libro**, de modo que sustituye al predeterminado solo para ese libro; la entrada marcada es la que tiene el libro ahora. **Default** deshace la elección.
- Un estilo que borre pasa a su predeterminado en los libros que lo usaban.

![Apariencia, Cambiar los estilos del texto: los estilos para elegir](../../images/story-style-menu.png)

En el estilo **Book** los párrafos están justificados y las palabras largas se separan en sílabas automáticamente, usando el diccionario del
**Idioma** del libro (que se define en la pestaña Acerca de, [véase aquí](managing-books.md#the-about-tab)). La separación en sílabas la hace el navegador;
para unos pocos idiomas poco comunes no hay diccionario y el texto simplemente no se separa.

Los libros que tenían activado el antiguo interruptor de *Cambiar los estilos del texto* siguen mostrando el estilo Book hasta que elija otra entrada.

<a id="highlighting-generated-text"></a>

## Resaltar el texto generado

El menú de ajustes (⚙) también tiene **Resaltar el texto generado**, una opción que se puede marcar. Mientras está activada, las palabras que escribió un modelo se resaltan en todas las partes,
para que lo que añadió o reescribió destaque. Marginalia sigue el texto a través de las [versiones de la parte](version-history.md): cada versión se superpone a la anterior, y las palabras que tienen en común
conservan a quien las escribió, de modo que una frase que usted escribió dentro de un texto generado sigue siendo suya aunque el modelo reescriba el resto las veces que sea. Una parte
que nunca editó se resalta entera; una parte que escribió usted mismo no tiene nada resaltado. Lo que escribe un agente cuenta como del modelo. La opción no se guarda: al abrir de nuevo la página vuelve a estar desactivada.

Una parte cuyo [historial está desactivado](version-history.md) no tiene versiones que seguir: se compara con el texto que el modelo generó
por primera vez. El resaltado funciona también en el [teléfono](../phone.md).

El mismo recuento da la proporción: la pestaña **Info** del libro tiene **Autoría de la rama** con un botón **Calcular**, que indica
qué porcentaje de las palabras de la rama activa escribió usted y cuál generó un modelo, y
[la exportación](export.md) puede terminar el archivo con una declaración de ello.

![Texto generado resaltado y el menú de ajustes](../../images/highlight-generated.png)

<a id="commands"></a>

## Comandos

Junto al menú de debajo de la historia hay una **línea de comandos**: escriba `/` para ver los comandos, elija uno y pulse **Enter**
(o el botón de reproducir) para ejecutarlo. Lo que devuelve un comando se muestra en una pequeña ventana encima de la línea. Consulte
[Comandos](../commands/index.md) para saber cómo escribir argumentos, qué muestra la ayuda y cómo detener un comando que tarda
un rato. La línea de comandos solo está, por ahora, en la pestaña de la historia del diseño de escritorio.
<a id="search-and-replace"></a>

## Buscar y reemplazar

El menú de ajustes (⚙) también tiene **Borrar el historial del libro** y **Vaciar la papelera del historial**, véase [Historial de versiones](version-history.md#deleting-versions).

El menú de ajustes (⚙) también tiene **Buscar y reemplazar**, que cambia una palabra, un nombre o una frase en toda la historia de una vez. La ventana tiene los campos **Buscar** y **Reemplazar por** y estas opciones:

![El menú de ajustes con Buscar y reemplazar](../../images/story-settings-menu.png)

![La ventana Buscar y reemplazar con una búsqueda escrita](../../images/search-replace-dialog.png)

| Opción | Por defecto | Significado |
|---|---|---|
| **Buscar con expresión regular** | desactivada | El texto de búsqueda es una [expresión regular](../regular-expressions.md) y el reemplazo puede usar sus grupos (`$1`). Si no, el texto se busca tal como se escribe. |
| **Distinguir mayúsculas y minúsculas** | activada | Un texto se encuentra solo en la combinación de mayúsculas y minúsculas que escribiste (*John* no encuentra *john*). Desactivada: no importan las mayúsculas. Bloqueada mientras **Buscar con expresión regular** está activada. |
| **Buscar en los resúmenes** | activada | Los [resúmenes](summaries.md) también se buscan y se cambian. |
| **Buscar en todas las ramas** | desactivada | Se buscan todas las [ramas](branches-and-story-tree.md); si no, solo la activa. |
| **Revalidar todos los resúmenes** | activada | Después del reemplazo, todos los [resúmenes](summaries.md) del libro vuelven a ser válidos, véase más abajo. |

Con una expresión regular, las mayúsculas se toman siempre tal como se escriben; empieza con `(?i)` para ignorarlas. Pulsa **Buscar** (el botón a la derecha de los campos; en el teléfono, debajo de ellos). La barra de progreso indica cuánto falta y **Detener**, junto a ella, la interrumpe. Una búsqueda no cambia nada.

La tabla muestra cada parte de la historia y cada resumen que tiene alguna coincidencia, con su tipo e id. Abre una fila para ver las coincidencias que contiene: el texto alrededor de la coincidencia tal como está y tal como quedará después del reemplazo. Una coincidencia o un reemplazo demasiado largos para mostrarse se escriben como *[texto buscado]* o *[texto de reemplazo]*.

![Las coincidencias de una búsqueda, con la primera parte abierta](../../images/search-replace-found.png)

Pulsar **Enter** en el campo **Buscar** o **Reemplazar por** equivale a pulsar **Buscar**.

Cuando la búsqueda termina, los campos y las opciones se bloquean y el botón **Reemplazar** se activa. **Buscar de nuevo** olvida lo encontrado y desbloquea la ventana, para que puedas cambiar la búsqueda. **Reemplazar** cambia la historia: la ventana se congela por completo (ni **Detener** funciona) hasta que termina, y entonces queda vacía de nuevo. Las partes reemplazadas se marcan como editadas, como si las hubieras editado tú mismo.

Un reemplazo en el texto que describe un [resumen](summaries.md#when-summaries-become-outdated) dejaría el resumen desactualizado. Con **Revalidar todos los resúmenes** activada, Marginalia vuelve a calcular el valor de comprobación de cada resumen del libro (los de todas las ramas, también los que el reemplazo no tocó y los que ya estaban desactualizados), de modo que todos quedan válidos después. Eso también vuelve válido un resumen desactualizado sin mirar su texto, así que úsala cuando sepas que los resúmenes siguen diciendo lo correcto.

Con la opción desactivada, solo se actualizan los resúmenes que cubre la búsqueda. Los que no cubre (resúmenes de ramas que no se buscan, que comparten sus partes anteriores con la rama activa; o todos ellos cuando los resúmenes no se buscan) quedan desactualizados. Si hay resúmenes así, Marginalia te dice cuántos son y pregunta si seguir. Un resumen que ya estaba desactualizado sigue así.

![La advertencia sobre resúmenes que quedarían desactualizados](../../images/search-replace-warning.png)

En el [diseño para teléfono](../phone.md), la misma ventana tiene los campos y los botones uno debajo de otro, y la tabla tiene una columna que muestra el tipo y el id, y debajo de ellos la coincidencia y el reemplazo.

El [Resumen de resúmenes](summaries.md#search-and-replace-in-the-summaries) tiene la misma ventana, solo para los resúmenes.

La misma búsqueda y reemplazo puede lanzarse desde la [línea de comandos](../commands/index.md#commands-that-come-with-marginalia) con `/search-replace-parts`; pregunta antes de reemplazar.

<a id="importing-into-the-book"></a>

## Importar en el libro

El menú de ajustes (⚙) tiene **Importar** (bajo **Importar y exportar**). Es la ventana de la pestaña Libros que crea un libro nuevo a partir de un archivo (véase [Gestionar libros](managing-books.md#creating-a-book)), pero para el libro en el que estás: las partes se **añaden a la historia**. El **Origen** es uno de estos: el archivo zip de archivos `.md`, un documento (Word, LibreOffice, RTF, Markdown, texto plano, EPUB), el zip de una bóveda de Obsidian o el zip de un proyecto de Scrivener; cada uno tiene el mismo texto y las mismas opciones que allí (las palabras por parte y el nivel de título de los capítulos), y una línea indica lo que hará la importación. Los chats de SillyTavern y de LM Studio no se ofrecen: son un árbol de ramas, que no tiene sitio en medio de una historia.

**Colocar las partes** indica dónde van:

- **Al final de la historia**: después de la última parte de la rama activa (la primera parte de un libro vacío).
- **Antes de una parte** o **Después de una parte**: elige la parte de la lista (los números son los lugares en la rama activa, con el inicio del texto). Las partes se colocan entre dos partes: todo lo que seguía al lugar, todas sus ramas, sigue a las partes nuevas, y ninguna parte se modifica ni se vuelve a generar. El final de la historia se queda donde estaba (la historia es esas partes más larga). Los resúmenes que cubren el lugar quedan desactualizados, ya que describen la historia sin las partes nuevas: la ventana de los [resúmenes](summaries.md) los muestra y los vuelve a hacer.

![La ventana de importación de un libro](../images/book-import-into.png)

Las partes de un archivo son los ficheros en el orden de sus nombres, comparando los números dentro de los nombres como números (`2.md` antes que `10.md`), también en las carpetas del archivo. Se omiten las carpetas, los ficheros ocultos (`.nombre`), la carpeta `__MACOSX` y los ficheros que no son `.md`. El fichero es el texto de la parte tal cual, no se envía al modelo ni se cambia; todas las partes cuentan como escritas por ti.

La importación muestra una barra de progreso con el número de partes hechas. **Detener** la termina después de la parte que se está guardando: las partes que ya se añadieron se quedan en la historia (la importación no se deshace), y un mensaje indica cuántas se importaron.

<a id="after-an-import"></a>

### Después de una importación

Cuando las partes están en el libro, aparece una ventana **Importación terminada** que ofrece las dos cosas que hacen más fácil escribir una historia larga (un libro recién importado como nuevo se abre primero, y la ventana queda delante de él). Ambas también están en el menú de ajustes (⚙, **Análisis**), así que pueden hacerse más tarde o de nuevo:

- **Crear resúmenes...** escribe los resúmenes de lo importado, véase [Crear resúmenes](summaries.md#create-summaries). Hazlo primero: un agente bloquea el libro mientras trabaja.
- **Tomar notas de lore con un agente...** lanza un [agente](../agents.md) que lee la historia y escribe los personajes, lugares, cosas y términos que encuentra como entradas del lorebook del libro, véase [Notas de lore](../agents.md#lore-notes-of-an-imported-book).

**Ahora no** cierra la ventana y no hace ninguna de las dos cosas.

![La ventana después de una importación](../../images/import-finished.png)

La misma importación en un lorebook se describe en [Importar y exportar](../lorebooks/import-export.md#importing-an-archive-of-text-files).

<a id="exporting-the-story"></a>

## Exportar la historia

El menú de ajustes (⚙) también tiene **Exportar la historia**, que guarda la rama activa como un fichero de texto, Markdown, HTML, Word, PDF o EPUB. Véase [Exportar un libro](export.md).

<a id="what-the-model-gets"></a>

## Lo que recibe el modelo

Para cada parte nueva, Marginalia envía:

1. La **plantilla maestra** como mensaje de sistema: rol, entradas del [lorebook](../lorebooks/index.md), punto de vista, tiempo verbal, estilo y [resúmenes](summaries.md). El prompt de jailbreak del [proveedor de inferencia](../inference-providers.md#general-settings), si está activado, va primero.
2. Las partes anteriores de la rama activa como respuestas anteriores del propio modelo, tantas como quepan en el [límite de contexto](../protocols.md#limits), de la más nueva a la más antigua. Las partes más antiguas que no quepan se dejan fuera (salvo que un [resumen](summaries.md) las cubra).
3. El **prompt de usuario** con las instrucciones para la nueva parte.

*Mostrar el prompt* en cualquier parte muestra exactamente lo que se envió. Véase [Prompts del libro](prompts.md) para las plantillas.
