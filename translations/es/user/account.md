---
label: Cuenta y ajustes
order: 920
icon: person
---

# Cuenta y ajustes

Todo lo que creas en Marginalia pertenece a tu cuenta: libros, lorebooks, etiquetas, proveedores de inferencia, protocolos y ajustes. Otros usuarios no los ven (salvo los libros que [publiques](books/publishing.md)), y tú no ves los de ellos.

<a id="your-account"></a>

## Tu cuenta

Los botones de la parte inferior del espacio de trabajo:

| Botón | |
|---|---|
| **Language** | El idioma de la interfaz de usuario, véase [Idioma](#language). |
| botón sol / luna | Junto al idioma: cambia entre el tema oscuro y el claro, véase [Tema](#theme). |
| **Change Password** | Cambia tu contraseña, tu nombre de usuario y tu nombre completo, véase más abajo. |
| **Logout** | Cierra tu sesión en este dispositivo. |
| **Admin** | Solo para administradores, véase [Administración](administration/index.md). |

<a id="changing-the-password"></a>

### Cambiar la contraseña

**Change Password** abre un diálogo con **Username**, **Full Name**, **Current Password**, **Password** y **Repeat password**:

- introduce tu **contraseña actual** en cada cambio (déjala vacía si tu cuenta no tiene contraseña);
- para cambiar la contraseña, escribe la nueva en los dos campos de contraseña;
- deja los dos campos de contraseña vacíos para cambiar solo el nombre y mantener la contraseña;
- marca **Log out everywhere else (other browsers and all saved logins)** para cerrar la sesión de tu cuenta en todos los demás dispositivos sin cambiar la contraseña, véase más abajo;
- el nombre de usuario no puede estar usado por otra cuenta.

Pulsa **OK** para guardar. Si la contraseña actual es incorrecta, no se guarda nada.

Cambiar la contraseña termina todos los inicios de sesión guardados (*Save login*), en todos los dispositivos, incluido este: tendrán que volver a iniciar sesión con la nueva contraseña. Tus otras sesiones abiertas en ese momento (un navegador en otro dispositivo que todavía muestre el espacio de trabajo) también se terminan; solo se mantiene la que estás usando. Si un dispositivo se ha perdido o tu contraseña se ha filtrado, cambiarla cierra la sesión en ese dispositivo.

**Log out everywhere else** hace lo mismo sin una contraseña nueva: se olvida cada inicio de sesión guardado de tu cuenta y se termina cada otra sesión abierta. Úsalo si dejaste la sesión iniciada en algún sitio, por ejemplo en un ordenador que no es tuyo. Necesita tu contraseña actual, como cualquier otro cambio de este diálogo. La sesión en la que estás se mantiene, pero su inicio de sesión guardado también se olvida, así que este navegador también pedirá la contraseña la próxima vez.

<a id="language"></a>

### Idioma

El selector **Language** situado encima de los botones cambia la interfaz de usuario a otro idioma. La elección se guarda en tu cuenta: la página se recarga con el nuevo idioma, y también lo hace cada otra página que tengas abierta (incluso en otro dispositivo). Además, se usa de nuevo la próxima vez que inicies sesión. La pantalla de inicio de sesión siempre aparece en el idioma predeterminado.

La lista contiene solo los idiomas instalados. Inglés, japonés, francés, español, alemán, coreano e italiano vienen con Marginalia; un administrador puede añadir más cargando una [extensión](extensions/index.md), por ejemplo la [localización al chino](extensions/chinese-localization.md). Los textos que un idioma no traduce se muestran en inglés. Las extensiones incluidas traen sus propios textos en japonés, chino, francés, español, alemán, coreano e italiano (las versiones en chino se muestran cuando se carga la localización al chino). Las fechas se escriben como lo hace ese idioma. Tus libros y sus textos no se traducen, solo la interfaz.

![El selector de idioma](../images/language-selector.png)

![El espacio de trabajo en japonés](../images/ui-japanese.png)

<a id="theme"></a>

### Tema

El botón junto al selector **Language** cambia la interfaz de usuario entre el tema oscuro (el predeterminado) y el claro. La elección se guarda en tu cuenta y se usa de nuevo la próxima vez que inicies sesión, en cualquier dispositivo; una página que ya esté abierta en otra pestaña cambia al recargarla. Los gráficos y el mapa de relaciones toman los colores del tema cuando se abren.

![El espacio de trabajo con el tema claro](../images/theme-light.png)

<a id="logging-in"></a>

### Iniciar sesión

**Save login** en la pantalla de inicio de sesión mantiene tu sesión iniciada en este dispositivo durante 30 días (necesita HTTPS en un servidor, véase [Servidor (Docker)](getting-started/docker-server.md#https-and-reverse-proxy)). **Logout** la termina.

Una cuenta puede tener una contraseña vacía; en ese caso inicias sesión solo con el nombre de usuario. Si un administrador borró tu contraseña olvidada, inicia sesión así y establece una nueva con **Change Password** de inmediato. Eso es útil para la aplicación de escritorio en tu propio ordenador, pero nunca lo hagas en un servidor al que otros puedan acceder.

<a id="settings"></a>

## Ajustes

La pestaña **Settings** contiene tus valores predeterminados para todos tus libros.

![La pestaña Settings](../images/settings-defaults.png)

En la parte superior hay tres botones:

- **Import Backup as New Book**: crea un libro a partir de un archivo de copia de seguridad, véase [Copias de seguridad de libros](books/backups.md#importing-a-backup-as-a-new-book);
- **Discard Changes**: descarta los cambios no guardados y vuelve a cargar los ajustes guardados;
- **Save**: guarda los ajustes.

!!!info Unsaved changes
Los ajustes no se guardan automáticamente. En cuanto cambias un campo (incluidos los ajustes de extensiones), aparece *Unsaved changes* arriba y las demás pestañas, **Admin** y **Logout** quedan bloqueadas. Pulsa **Save** o **Discard Changes** para desbloquearlas.
!!!

<a id="general-settings"></a>

### Ajustes generales

| Campo | |
|---|---|
| **Default Model** | El [proveedor de inferencia](inference-providers.md) con el que empiezan los libros nuevos. |
| **Default Protocol** | El [protocolo](protocols.md) con el que empiezan los libros nuevos. |
| **Default model for annotating**, **Default protocol for annotating** | El modelo que [anota una parte](books/annotations.md#letting-a-model-annotate) cuando no eliges otro para la ejecución. Vacío es el modelo del libro. |
| **Backup Strategy** | [Copias de seguridad](books/backups.md#automatic-backups) automáticas para los libros sin estrategia propia: *No Backups*, *After N Messages* o *After N Minutes*, con el número junto a ella. |
| **Versions kept per part** | Cuántas versiones anteriores del texto de una parte conserva el [historial](books/version-history.md#how-many-versions-are-kept) para todos tus libros, salvo que una carpeta, un libro o el administrador lo limite más. Vacío no pone límite aquí; **0** no conserva ningún historial. |
| **Parts loaded at once in the story** | Cuántas partes de un libro mantienen cargadas la [historia](books/story-editor.md) y el [visor](books/publishing.md). La historia empieza con las últimas partes, y el visor, donde dejaste de leer (o con las primeras partes). Las partes anteriores y posteriores se cargan al desplazarte, y las que quedan lejos de ti se descargan de nuevo. Vacío carga todas las partes a la vez. |
| **Daily goal: words written**, **Daily goal: minutes of editing** | La meta de un día para el calendario de las [estadísticas de escritura](books/statistics.md#the-daily-goal): las palabras que escribes tú mismo y los minutos que editas, en todos tus libros. Vacío significa que no hay meta. |
| **Record writing statistics**, **Keep statistics for (months)** | Si se cuentan tus [estadísticas de escritura](books/statistics.md#privacy-clearing-not-recording-keeping-less) y cuántos meses se conservan (vacío o 0: todos). Un administrador puede desactivar el registro para todos y establecer un plazo más corto. |

*Default Model* y *Default Protocol* solo se usan al crear un libro; cambiarlos no modifica los libros existentes. La extensión [Lore Generator](extensions/lore-generator.md) no tiene libro y genera con ellos. La estrategia de copias de seguridad se aplica a todos los libros sin *Per manuscript override*, a partir de la siguiente parte generada.
<a id="templates"></a>

### Plantillas

Los valores predeterminados de los [prompts del libro](books/prompts.md):

| Campo | |
|---|---|
| **Prompt language** | El idioma de los prompts integrados: inglés (el predeterminado), japonés, francés, español, alemán, italiano, coreano o chino, cuando se carga la [localización al chino](extensions/chinese-localization.md). Consulte [Prompt language](#prompt-language) más abajo. |
| **Master Template** | El marco del prompt, consulte [Master template](books/prompts.md#master-template). |
| **Point of View (POV)**, **Tense** | La voz narrativa y el tiempo verbal. |
| **Style** | Pautas de estilo y tono. |
| **Default User Prompt** | La solicitud para cada parte, consulte [User prompt](books/prompts.md#user-prompt). |
| **Default Summary Prompt** | La solicitud para un [resumen](books/summaries.md#the-summary-prompt). |
| **Default Meta Summary Prompt** | La solicitud para un [metaresumen](books/summaries.md#the-meta-summary-prompt), que combina resúmenes. |
| **Redraft System Prompt**, **Redraft Prompt** | El mensaje de sistema y el último mensaje de una [reescritura](books/story-editor.md#redrafting-a-part) de una parte, consulte [Redraft prompt](books/prompts.md#redraft-prompt). |
| **Annotate System Prompt**, **Annotate Prompt** | El mensaje de sistema y el último mensaje de la solicitud para que un modelo [anote](books/annotations.md#letting-a-model-annotate) una parte, consulte [Annotate prompt](books/prompts.md#annotate-prompt). |
| **Annotate Instructions** | Lo que busca el modelo al anotar, para los libros y carpetas que no definen nada, consulte [Annotate prompt](books/prompts.md#annotate-prompt). |
| **Impersonate Prompt** | La solicitud para que un modelo [redacte las instrucciones](books/story-editor.md#drafting-the-instructions) del siguiente turno en su lugar, consulte [Impersonate prompt](books/prompts.md#impersonate-prompt). |
| **Compaction prompt of agents** | Solo para usuarios que pueden usar [agentes](agents.md). Lo que se pregunta a sus agentes cuando los [`/compact`](agents.md#running-a-command-yourself), si su propio prompt de compactación está vacío; vacío aquí significa el que estableció su administrador, o si no, el integrado. |
| **Instructions of agents (agents.md)** | Solo para usuarios que pueden usar [agentes](agents.md). Sus instrucciones para los agentes que no tienen las suyas propias, añadidas a su prompt en cada ejecución. Vacío significa ninguna. |
| **Message to a URL when an agent needs you**, **Format of the message**, **Also when the application is open**, **Send a test message** | Solo para usuarios con el permiso [*Notifications*](administration/permissions.md) (y no solo para quienes usan [agentes](agents.md)), en la pestaña *General*, y solo si su administrador lo permite. Dónde enviar un mensaje breve cuando un agente pregunta algo, espera su respuesta, termina o falla, y usted tiene la aplicación cerrada: consulte [When you are not there](agents.md#when-you-are-not-there). |
| **System prompt of agents** | Solo para usuarios que pueden usar [agentes](agents.md). Lo que se indica a sus agentes sobre cómo ser cuando su propio prompt de sistema está vacío; vacío aquí significa el que estableció su administrador, o si no, el integrado. Texto plano, no una plantilla. |

![La pestaña Templates](../images/settings-templates.png)

Más abajo, para un usuario que puede usar agentes, están los prompts de los agentes:

![Los prompts de los agentes en la pestaña Templates](../images/settings-templates-agents.png)

Cada campo muestra en gris el valor predeterminado integrado mientras está vacío, y tiene la ventana emergente de sugerencias con **Insert Default Template** y las [variables y macros](templates/index.md) disponibles.

#### Prompt language

**Prompt language** elige el idioma de los prompts integrados: lo que representa un campo vacío (el texto gris cambia en cuanto elige otro idioma), y, por tanto, lo que se envía al modelo para la generación, los resúmenes, la reescritura, las anotaciones y la suplantación. Los prompts de los complementos que tienen los suyos propios (Arc Outline, Beta Readers, Book Description Generator, Lore Generator, Manga Generator, SillyTavern Converter, Knowledge Tracker, Audiobook) y los de los agentes también lo siguen. Es independiente del idioma de la interfaz: puede usar la aplicación en inglés y enviar prompts en japonés. Un modelo sigue mejor las instrucciones en el idioma de la historia, así que elija el idioma en que están escritos sus libros. Solo cambian los prompts integrados: un prompt que usted escribió, aquí o en un libro o carpeta, se mantiene tal cual. Un [agente](agents.md) lee sus prompts y avisos en su propio idioma (*Language* en la configuración del agente), no en este.

Estos valores predeterminados se aplican a **todo libro cuyo campo propio esté vacío**, de inmediato, no solo a los libros nuevos. Un libro que define su propio valor en su pestaña *Prompts* lo conserva. Así que:

- establezca aquí lo que comparten todos sus libros;
- establezca en la pestaña *Prompts* de un libro lo que es especial para ese libro;
- vacíe el campo de un libro para devolverlo al predeterminado de *Settings*.

<a id="styles"></a>

### Estilos

Los estilos de texto de sus libros: cómo se ven el texto de las partes en la historia y para el lector. La pestaña lista los estilos **Normal** y **Book**, que trae la aplicación y no se pueden cambiar (están marcados como *built in*), y los suyos propios. Seleccione un estilo para editarlo a la derecha; los botones debajo de la lista:

- **Duplicate**: crea un estilo propio a partir del seleccionado (también de uno integrado: así se empieza), llamado *... (copy)*, que luego puede renombrar y cambiar;
- **Delete**: elimina un estilo suyo; un estilo integrado no se puede eliminar;
- **Set as default**: hace que el estilo sea el que usan los libros, a menos que [elijan otro](books/story-editor.md#text-styles) en el menú de configuración de la historia. El estilo predeterminado está marcado como *default* en la lista.
- **Export** e **Import**: guardan el estilo seleccionado como un archivo `.style.json`, y añaden los estilos de ese archivo a los suyos (no se guardan hasta que guarde la configuración). Consulte [Share a style](books/styling-guide.md#share-a-style-export-and-import).

La [guía de estilos](books/styling-guide.md) repasa cada opción y tiene un estilo de ejemplo para descargar.

![La pestaña Styles, con el estilo horror de la guía de estilos seleccionado](../images/settings-styles.png)

Para un estilo propio, el formulario de la derecha tiene un **Name**, la casilla **For exports (not offered for books)** y dos pestañas. Un estilo es para libros o para exportaciones: un estilo para exportaciones se ofrece en el [diálogo de exportación](books/export.md#print-styles) (la aplicación trae cuatro, los aspectos de las exportaciones a HTML, EPUB, PDF y Word) y no puede ser el estilo predeterminado de los libros, y un estilo para libros no se ofrece en el diálogo de exportación. La lista los marca como *(export)*.

- **Options**: en grupos: *Text* (fuente, tamaño, altura de línea, espaciado de letras y palabras, color, alineación, guionado, el espacio entre párrafos, la **first line indent** de los párrafos y una **drop cap** después de los encabezados), *Page* (el ancho de la columna de texto, centrada, y el **background color**, que rellena toda el área detrás del texto), *Emphasis, links and code* (los colores del texto en cursiva y en negrita y de los enlaces, la fuente del código), *Quotes*, *Headings* (fuente, tamaño del primer nivel, color, alineación, versalitas) y el símbolo y el color del *Scene break* (`---`). Una casilla admite cualquier valor CSS (`1.1rem`, `#336699`, `var(--lumo-body-text-color)`); una casilla vacía mantiene el aspecto de la aplicación. Las casillas de fuente ofrecen pilas de fuentes comunes, y aceptan las suyas propias.
- **Custom CSS**: CSS propio, añadido después de las opciones y aplicado dentro del estilo, de modo que una regla que escriba solo afecta al texto con este estilo: `.chat-message-markdown p { text-indent: 1.5em !important; }` sangra los párrafos, y las declaraciones simples (`font-size: 1.2rem;`) se aplican a todo el texto. Debajo, **Generated CSS** muestra la hoja de estilos que generan las opciones (solo lectura). Las llaves `{ }` deben coincidir, o el estilo no se guarda.

![La pestaña Custom CSS de un estilo](../images/settings-styles-css.png)

Debajo del formulario, **Preview** muestra un texto de muestra con el estilo, y sigue cada cambio a medida que escribe.

Como las demás pestañas, la pestaña Styles se guarda con el **Save** de la configuración, y **Discard Changes** también descarta sus cambios: duplicar, editar, eliminar y establecer el predeterminado solo cambian la copia de trabajo, y *Unsaved changes* (y el bloqueo) aparece en cuanto hace cualquiera de ellos. Un estilo sin nombre, o un CSS propio cuyas llaves no coinciden, se rechaza al guardar.
<a id="exports"></a>

### Exportaciones

Con qué se exporta cada formato de archivo: la pestaña tiene una sección para cada formato que tiene algo que configurar (**Plain text**, **Markdown**, **HTML**, **Word**, **PDF**, **EPUB**, **Markdown with front matter**, **Obsidian vault**; haga clic en su nombre para abrirla).

- **Print style** (HTML, Word, PDF y EPUB, los formatos con fuentes y colores) - el estilo que el [diálogo de exportación](books/export.md) preselecciona para el formato. Empieza como el aspecto que la aplicación trae para el formato (**HTML page**, **PDF book**...); la lista contiene los [estilos para exportaciones](#styles), los integrados y los suyos. Elija otro para exportar ese formato con él cada vez, hasta que cambie la elección en el diálogo para una exportación.
- **Front matter** (Markdown with front matter y Obsidian vault) - el bloque YAML que inicia el archivo, con `{{title}}`, `{{author}}`, `{{language}}` y `{{exportDate}}`. Vacío es el bloque predeterminado.
- **Title page** (todos los formatos excepto los de otras herramientas; el Obsidian vault la tiene en la nota del libro) - la plantilla de la portada, una plantilla Handlebars que escribe Markdown, con `{{title}}` y `{{author}}` (el icono de información del campo las enumera). Vacía es la predeterminada: el título como encabezado y el autor en cursiva debajo. **Fill default** del cuadro de información le da como punto de partida la plantilla predeterminada.
- **Authorship statement** (todos los formatos excepto el chat de SillyTavern) - el texto que la casilla *Include authorship statement* de la exportación añade al final: `{{title}}`, `{{author}}`, `{{totalWords}}`, `{{writtenWords}}`, `{{generatedWords}}`, `{{writtenPercent}}`, `{{generatedPercent}}` y `{{models}}` (los modelos separados por comas, vacío cuando no hay ninguno). Vacío es el texto predeterminado.

Estas son sus versiones: un libro o una carpeta pueden tener las suyas, véase [Plantilla de portada](books/export.md#title-page-template). La pestaña se guarda con **Save** de la configuración y **Discard Changes** descarta sus cambios, como en las demás. Una plantilla que no sea válida se rechaza al guardar. Un estilo que cree en la pestaña Styles se puede elegir aquí después de guardar.

![La pestaña Exports, con la sección HTML abierta](../images/settings-exports.png)

<a id="tags"></a>

### Etiquetas

Una tabla de todas sus etiquetas (las que asigna a libros, lorebooks y entradas de lorebook) con el número de objetos a los que está relacionada cada etiqueta. Haga clic en el encabezado de una columna para ordenar por ella. A diferencia de las otras pestañas, actúa de inmediato: no forma parte de los cambios sin guardar y no hay nada que guardar.

![La pestaña Tags](../images/settings-tags.png)

| Botón | |
|---|---|
| **Rename** (pencil) | Pide el nuevo nombre de la etiqueta; cada objeto conserva la etiqueta con el nuevo nombre. |
| **Delete** (trash can) | Pide confirmación y luego elimina la etiqueta y la quita de todos los objetos que la tienen. |

Si el nuevo nombre es el de otra etiqueta (los nombres se comparan sin distinguir mayúsculas), renombrar haría que las dos etiquetas fueran la misma, así que se le pregunta si quiere hacer **merge**. Merge traslada todo lo etiquetado con la etiqueta renombrada a la etiqueta existente y elimina la renombrada; un objeto que tenga ambas acaba con la etiqueta existente una sola vez. Cancel y no cambia nada, la etiqueta conserva su nombre anterior.

![Pregunta si fusionar dos etiquetas](../images/tag-merge.png)

<a id="keyboard-shortcuts"></a>

### Atajos de teclado

Las teclas de los [atajos del editor de historias](books/story-editor.md#keyboard-shortcuts) (funcionan en el editor de historias en un ordenador; la [disposición para teléfono](phone.md) no tiene ninguno). La tabla enumera cada acción, también las que traen las [extensiones](extensions/index.md), con su tecla, y las teclas de los botones de la ventana [Organise](books/folders.md#organise) de la lista de libros. Cada elemento del menú de configuración de la historia, del menú de una parte y del menú contextual del clic derecho de su texto es aquí una acción (la mayoría no tiene tecla hasta que se la asigne), y los menús muestran la tecla junto al elemento.

| Botón | |
|---|---|
| **Record** | Pulse la nueva combinación de teclas (primero los modificadores o todos juntos, p. ej. `Ctrl+Shift+K`); un clic en cualquier otro lugar cancela. |
| **Reset to default** | Devuelve a la acción su tecla predeterminada. |
| **Clear** | La acción no tiene tecla. |

Una nota en rojo indica una tecla que comparten dos acciones, una tecla que el navegador se reserva para sí (`Ctrl+N`, `Ctrl+T`, `Ctrl+W` y similares; la página nunca las recibe) y `Alt` con una letra (escribe un carácter en algunos teclados, p. ej. macOS). Una tecla sin `Ctrl`, `Alt` o `Meta` funciona solo cuando ningún campo de texto tiene el foco. Las teclas se guardan con **Save** como los demás ajustes y se aplican de inmediato en las historias abiertas.

![Configuración, Atajos de teclado](../images/settings-shortcuts.png)

<a id="notifications"></a>

### Notificaciones

Una notificación de escritorio del navegador cuando un modelo termina, para que pueda dejar la pestaña mientras trabaja. La pestaña tiene una casilla **Use desktop notifications**, los botones de abajo y una lista de lo que notifica:

- **Story generation**, **Annotate with AI**, **Annotate with AI (automatic)**, **Redraft**, **Redraft (automatic)**, **Draft Instructions**, **Summary**, **Resummarize all summaries** y **Help assistant**, y lo que añaden las [extensiones](extensions/index.md), con el nombre de la extensión (*Reviewer: generate a review*, *Lore Generator: generate lore*, *Image Generator: generate the image*, *Arc Outline: generate chapters*...). Las entradas de *Manga Generator* marcadas *(automatic)* son su modo continuo.
- Las entradas marcadas *(automatic)* son las ejecuciones que el libro inicia por sí mismo, la [anotación automática](books/story-editor.md) tras una generación y el redraft que la sigue. Se eligen aparte del diálogo que usted abre: con **Annotate with AI** marcada y **Annotate with AI (automatic)** no, la anotación automática no notifica; con solo la entrada automática marcada, solo la anotación automática notifica.
- Una ejecución que termina con un error también notifica (*… failed*); una que detiene usted mismo no.

La casilla activa las notificaciones para su cuenta y habilita dos botones. **Register this browser** pide al navegador el permiso (desde ese clic); la línea bajo los botones indica lo que dice este navegador: permitido, bloqueado (permítalo en la configuración de sitios del navegador) o todavía no preguntado. El permiso pertenece al navegador, no a su cuenta: cada navegador o dispositivo en el que quiera notificaciones debe registrarse una vez; la casilla y la lista son las mismas en todas partes. **Send a test notification** muestra una de inmediato, también mientras la página está al frente.

El campo **Filter** reduce la lista por nombre; **Check shown** y **Uncheck shown** actúan sobre las entradas que muestra y dejan las demás como están. Una notificación se muestra solo mientras no está mirando la página (otra pestaña, o la ventana no está al frente); al hacer clic en ella vuelve la página.

Las notificaciones necesitan una dirección `https://` o `localhost`: en una dirección `http://` simple de un servidor de su red el navegador no las ofrece, y la pestaña lo indica. Una pestaña que el navegador ha puesto en reposo (normalmente en un teléfono) tampoco puede mostrarlas. La pestaña se guarda con **Save** de la configuración, como las demás.

<a id="phrase-watch-list"></a>

### Lista de frases vigiladas

Su [lista de frases vigiladas](books/annotations.md#watching-for-phrases), que usan todos los libros (las carpetas y los libros añaden las suyas), y las **annotation templates** de las que eligen sus entradas y las de las carpetas y libros: lo que dice la nota de una frase encontrada, su tipo y si se entrega al modelo. Se guarda con **Save** de la configuración, como las demás pestañas.

![La lista de frases vigiladas en la configuración](../images/settings-phrase-watch.png)
<a id="api-keys"></a>

### Claves de API

Claves para programas y scripts que usan Marginalia a través de su API HTTP (`/api/v1`): un programa envía la clave en la cabecera `Authorization: Bearer <key>` y puede hacer lo que tú podrías hacer en tus libros. Solo para administradores y usuarios con el permiso [*API access*](administration/permissions.md); sin él, la pestaña lo indica.

![La pestaña de claves de API](../images/settings-api-keys.png)

![Una clave nueva, mostrada una vez](../images/api-key-created.png)

**New API key** pide un **Name** (quién usa la clave, para saber cuál revocar), el **Access** y cuándo **Expires** (tras un número de días, o nunca si se deja vacío). El acceso es cualquier combinación de:

| Access | |
|---|---|
| **Read** | Tus libros y los comandos que solo leen. |
| **Write** | Todos los comandos, también los que cambian o borran; incluye *Read*. |
| **Update the installation** | Solo para administradores: la [actualización](administration/updates.md) de la instalación (su estado, su registro, iniciarla). |
| **Manage the extensions** | Solo para administradores: instalar, actualizar, detener y eliminar [extensiones](administration/extensions.md). |

Toda clave puede leer la versión de Marginalia. Da a una clave solo lo que necesita su programa: un script que instala plugins solo necesita *Manage the extensions*, y esa clave no puede tocar tus libros. La actualización y las extensiones dejan de funcionar para una clave cuando su propietario deja de ser administrador; su otro acceso se mantiene.

La clave se muestra una sola vez, justo después de crearla: cópiala entonces, porque Marginalia solo guarda una huella de ella y no puede volver a mostrarla. La lista muestra tus claves con el inicio de cada una, cuándo se creó, cuándo expira y cuándo se usó por última vez; el botón de la papelera **revoca** una clave, y los programas que la usan pierden el acceso de inmediato. Una clave también deja de funcionar cuando expira y cuando un administrador le quita el permiso. Las claves se guardan al crearlas o revocarlas, no con el **Save** de los ajustes.

**La página de la API**: abre `/api/v1/` de tu Marginalia en un navegador (por ejemplo `https://marginalia.example.org/api/v1/`) para ver cada llamada, sus argumentos y sus respuestas. Pega una clave en **API key** arriba: la página entonces también lista cada comando que esa clave puede ejecutar, cada uno con sus argumentos, y **Try it out** → **Execute** envía la llamada con esa clave y muestra la respuesta. Las llamadas son reales: un comando cambia tu libro, una actualización reinicia el servidor. La página guarda la clave solo mientras la pestaña está abierta, nunca en el disco.

Para desarrolladores, las llamadas se describen en el [manual para desarrolladores](../developer/api.md).

<a id="extension-settings"></a>

### Ajustes de extensiones

Ajustes de las [extensiones](extensions/index.md) instaladas, una sección por extensión (por ejemplo *Reviewer Settings* y *SideQuery Settings*). Se guardan con el mismo botón **Save**. La pestaña Extension Settings está vacía cuando no hay ninguna extensión con ajustes instalada.

![La pestaña Extension Settings con los ajustes de Lore Generator](../images/settings-extensions.png)
