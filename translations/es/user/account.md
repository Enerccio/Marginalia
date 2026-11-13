---
label: Cuenta y ajustes
order: 920
icon: person
---

# Cuenta y ajustes

Todo lo que creas en Marginalia pertenece a tu cuenta: libros, lorebooks, etiquetas, proveedores de inferencia, protocolos
y ajustes. Otros usuarios no los ven (salvo los libros que [publiques](books/publishing.md)), y tú no ves los suyos.

<a id="your-account"></a>

## Tu cuenta

Los botones de la parte inferior del espacio de trabajo:

| Botón | |
|---|---|
| **Idioma** | El idioma de la interfaz, ver [Idioma](#language). |
| botón sol / luna | Junto al idioma: cambia entre el tema oscuro y el claro, ver [Tema](#theme). |
| **Cambiar la contraseña** | Cambia tu contraseña, nombre de usuario y nombre completo, ver abajo. |
| **Cerrar sesión** | Cierra tu sesión en este dispositivo. |
| **Administración** | Solo para administradores, ver [Administración](administration/index.md). |

<a id="changing-the-password"></a>

### Cambiar la contraseña

**Cambiar la contraseña** abre un diálogo con tu **Nombre de usuario**, **Nombre completo**, **Contraseña actual**, **Contraseña** y **Repetir la contraseña**:

- introduce tu **contraseña actual** para cada cambio (déjalo vacío si tu cuenta no tiene contraseña);
- para cambiar la contraseña, escribe la nueva en los dos campos de contraseña;
- deja los dos campos de contraseña vacíos para cambiar solo el nombre y conservar la contraseña;
- marca la casilla **Log out everywhere else (other browsers and all saved logins)** para cerrar la sesión de tu cuenta en todos los demás dispositivos sin cambiar la contraseña, ver abajo;
- el nombre de usuario no puede estar usado por otra cuenta.

Haz clic en **Aceptar** para guardar. Si la contraseña actual es incorrecta, no se guarda nada.

Cambiar la contraseña cierra todas las sesiones guardadas (*Mantener la sesión iniciada*) en todos los dispositivos, incluido este: hay que volver a iniciar sesión con la nueva contraseña. También se cierran las demás sesiones abiertas en este momento (un navegador en otro dispositivo que todavía muestra el espacio de trabajo); solo sigue abierta la que estás usando. Si perdiste un dispositivo o se filtró tu contraseña, al cambiarla se cierra la sesión en ese dispositivo.

**Log out everywhere else** hace lo mismo sin cambiar la contraseña: se olvida cada sesión guardada de tu cuenta y se cierra cada otra sesión abierta. Úsala si dejaste la sesión iniciada en algún sitio, por ejemplo en un ordenador que no es tuyo. Necesita tu contraseña actual como cualquier otro cambio de este diálogo. La sesión en la que estás se mantiene, pero también se olvida su sesión guardada, así que este navegador también pedirá la contraseña la próxima vez.

<a id="language"></a>

### Idioma

El selector **Idioma** sobre los botones cambia la interfaz a otro idioma. La elección se guarda en tu cuenta: la página se recarga con el nuevo idioma, y lo mismo hace cada otra página que tengas abierta (también en otro dispositivo), y se usa de nuevo la próxima vez que inicies sesión. La pantalla de inicio de sesión siempre está en el idioma predeterminado.

La lista contiene solo los idiomas instalados. Inglés, japonés, francés, español, alemán, coreano e italiano vienen con Marginalia; un administrador puede añadir más cargando una [extensión](extensions/index.md), por ejemplo la [localización al chino](extensions/chinese-localization.md). Los textos que un idioma no traduce se muestran en inglés. Las extensiones incluidas traen sus propios textos en japonés, chino, francés, español, alemán, coreano e italiano (los textos en chino aparecen cuando se carga la localización al chino).
Las fechas se escriben como lo hace ese idioma. Tus libros y sus textos no se traducen, solo la interfaz.

![El selector de idioma](../images/language-selector.png)

![El espacio de trabajo en japonés](../images/ui-japanese.png)

<a id="theme"></a>

### Tema

El botón junto al selector **Idioma** cambia la interfaz entre el tema oscuro (el predeterminado) y el claro. La elección se guarda en tu cuenta y se usa de nuevo la próxima vez que inicies sesión, en cualquier dispositivo; una página que ya esté abierta en otra pestaña cambia al recargarla. Los gráficos y el mapa de relaciones toman los colores del tema al abrirse.

![El espacio de trabajo con el tema claro](../images/theme-light.png)

<a id="logging-in"></a>

### Iniciar sesión

**Mantener la sesión iniciada** en la pantalla de inicio de sesión mantiene tu sesión iniciada en este dispositivo durante 30 días (en un servidor necesita HTTPS, ver [Servidor (Docker)](getting-started/docker-server.md#https-and-reverse-proxy)). **Cerrar sesión** la termina.

Una cuenta puede tener una contraseña vacía; en ese caso inicias sesión solo con el nombre de usuario. Si un administrador borró tu contraseña olvidada, inicia sesión así y establece una nueva con **Cambiar la contraseña** enseguida. Eso resulta práctico para la aplicación de escritorio en tu propio ordenador, pero nunca lo hagas en un servidor al que puedan acceder otros.

<a id="settings"></a>

## Ajustes

La pestaña **Ajustes** contiene tus valores predeterminados para todos tus libros.

![La pestaña Ajustes](../images/settings-defaults.png)

Arriba hay tres botones:

- **Importar copia de seguridad como libro nuevo** - crea un libro a partir de un archivo de copia de seguridad, ver
  [Copias de seguridad de libros](books/backups.md#importing-a-backup-as-a-new-book);
- **Descartar los cambios** - descarta los cambios sin guardar y vuelve a cargar los ajustes guardados;
- **Guardar** - guarda los ajustes.

!!!info Unsaved changes
Los ajustes no se guardan automáticamente. En cuanto cambias un campo (también los ajustes de extensiones), aparece *Unsaved changes* arriba y las demás pestañas, **Administración** y **Cerrar sesión** quedan bloqueadas. Haz clic en **Guardar** o **Descartar los cambios** para desbloquearlas.
!!!

<a id="general-settings"></a>

### Ajustes generales

| Campo | |
|---|---|
| **Modelo predeterminado** | El [proveedor de inferencia](inference-providers.md) con el que empiezan los libros nuevos. |
| **Protocolo predeterminado** | El [protocolo](protocols.md) con el que empiezan los libros nuevos. |
| **Modelo predeterminado para anotar**, **Protocolo predeterminado para anotar** | El modelo que [anota una parte](books/annotations.md#letting-a-model-annotate) cuando no eliges otro para la ejecución. Vacío es el modelo del libro. |
| **Estrategia de copia de seguridad** | [Copias de seguridad de libros](books/backups.md#automatic-backups) automáticas para los libros sin estrategia propia: *Sin copias de seguridad*, *Tras N mensajes* o *Tras N minutos*, con el número junto a ella. |
| **Versiones conservadas por parte** | Cuántas versiones anteriores del texto de una parte conserva el [historial](books/version-history.md#how-many-versions-are-kept) para todos tus libros, salvo que una carpeta, un libro o el administrador lo limiten más. Vacío no pone límite aquí; **0** no conserva historial. |
| **Partes cargadas a la vez en la historia** | Cuántas partes de un libro mantienen cargadas la [historia](books/story-editor.md) y el [visor](books/publishing.md). La historia empieza con las últimas partes, el visor donde dejaste de leer (o con las primeras partes); las partes anteriores y posteriores se cargan al desplazarte y las que quedan lejos de ti se descartan de nuevo. Vacío carga todas las partes a la vez. |
| **Objetivo diario: palabras escritas**, **Objetivo diario: minutos de edición** | El objetivo de un día para el calendario de las [estadísticas de escritura](books/statistics.md#the-daily-goal): las palabras que escribes tú y los minutos que editas, en todos tus libros. Vacío es sin objetivo. |
| **Registrar las estadísticas de escritura**, **Keep statistics for (months)** | Si se cuentan tus [estadísticas de escritura](books/statistics.md#privacy-clearing-not-recording-keeping-less) y cuántos meses se conservan (vacío o 0: todos). Un administrador puede desactivar el registro para todos y poner un plazo más corto. |

*Modelo predeterminado* y *Protocolo predeterminado* se usan solo al crear un libro; cambiarlos no modifica los libros existentes. La extensión [Lore Generator](extensions/lore-generator.md) no tiene libro y genera con ellos. La estrategia de copia de seguridad se aplica a todos los libros sin *Ajuste propio de cada manuscrito*, a partir de su próxima parte generada.
<a id="templates"></a>

### Plantillas

Los valores predeterminados de los [prompts del libro](books/prompts.md):

| Campo | |
|---|---|
| **Idioma de los prompts** | El idioma de los prompts integrados: inglés (el predeterminado), japonés, francés, español, alemán, italiano, coreano o chino, cuando la [localización al chino](extensions/chinese-localization.md) está cargada. Véase [Idioma de los prompts](#prompt-language) más abajo. |
| **Plantilla principal** | El marco del prompt, véase [Plantilla principal](books/prompts.md#master-template). |
| **Punto de vista (POV)**, **Tiempo verbal** | La voz narrativa y el tiempo verbal. |
| **Estilo** | Directrices de estilo y tono. |
| **Prompt de usuario predeterminado** | La petición para cada parte, véase [Prompt de usuario](books/prompts.md#user-prompt). |
| **Prompt de resumen predeterminado** | La petición para un [resumen](books/summaries.md#the-summary-prompt). |
| **Prompt de metarresumen predeterminado** | La petición para un [metarresumen](books/summaries.md#the-meta-summary-prompt), que fusiona resúmenes. |
| **Prompt del sistema de reescritura**, **Prompt de reescritura** | El mensaje de sistema y el último mensaje de una [reescritura](books/story-editor.md#redrafting-a-part) de una parte, véase [Prompt de reescritura](books/prompts.md#redraft-prompt). |
| **Prompt del sistema de anotación**, **Prompt de anotación** | El mensaje de sistema y el último mensaje de la petición a un modelo para [anotar](books/annotations.md#letting-a-model-annotate) una parte, véase [Prompt de anotación](books/prompts.md#annotate-prompt). |
| **Instrucciones de anotación** | Lo que busca el modelo al anotar, para los libros y carpetas que no definen nada, véase [Prompt de anotación](books/prompts.md#annotate-prompt). |
| **Prompt de suplantación** | La petición para que un modelo [redacte las instrucciones](books/story-editor.md#drafting-the-instructions) del siguiente turno en tu lugar, véase [Prompt de suplantación](books/prompts.md#impersonate-prompt). |
| **Prompt de compactación de los agentes** | Solo para usuarios que pueden usar [agentes](agents.md). Lo que se pide a tus agentes cuando los [`/compact`](agents.md#running-a-command-yourself), si su propio prompt de compactación está vacío; vacío aquí significa el que estableció tu administrador, si no, el integrado. |
| **Instrucciones de los agentes (agents.md)** | Solo para usuarios que pueden usar [agentes](agents.md). Tus instrucciones para los agentes que no tienen las suyas, añadidas a su prompt en cada ejecución. Vacío significa ninguna. |
| **Mensaje a una URL cuando un agente te necesita**, **Formato del mensaje**, **También cuando la aplicación está abierta**, **Enviar un mensaje de prueba** | Solo para usuarios con el permiso [*Notificaciones*](administration/permissions.md) (y no solo quienes usan [agentes](agents.md)), en la pestaña *General*, y solo si tu administrador lo permite. Dónde enviar un mensaje breve cuando un agente pregunta algo, espera por ti, termina o falla y tienes la aplicación cerrada: véase [Cuando no estás](agents.md#when-you-are-not-there). |
| **Prompt del sistema de los agentes** | Solo para usuarios que pueden usar [agentes](agents.md). Lo que se indica a tus agentes que sean cuando su propio prompt del sistema está vacío; vacío aquí significa el que estableció tu administrador, si no, el integrado. Texto plano, no una plantilla. |

![La pestaña Plantillas](../images/settings-templates.png)

Más abajo, para un usuario que puede usar agentes, están los prompts de los agentes:

![Los prompts de los agentes en la pestaña Plantillas](../images/settings-templates-agents.png)

Cada campo muestra en gris el valor integrado mientras está vacío, y tiene el menú de sugerencias con
**Insertar la plantilla predeterminada** y las [variables y macros](templates/index.md) disponibles.

<a id="prompt-language"></a>

#### Idioma de los prompts

**Idioma de los prompts** elige el idioma de los prompts integrados: lo que representa un campo vacío (el texto gris
cambia en cuanto eliges otro idioma), y, por tanto, lo que se envía al modelo para la generación, los resúmenes, la reescritura, las anotaciones
y la suplantación. Los prompts de los complementos que tienen los suyos (Arc Outline, Beta Readers, Book Description Generator,
Lore Generator, Manga Generator, SillyTavern Converter, Knowledge Tracker, Audiobook) y los de los agentes también lo siguen.
Es independiente del idioma de la interfaz: puedes usar la aplicación en inglés y enviar prompts en japonés.
Un modelo sigue mejor las instrucciones en el idioma de la historia, así que elige el de tus libros. Solo cambian los
prompts integrados: un prompt que tú hayas escrito, aquí o en un libro o carpeta, se queda como está. Un [agente](agents.md)
lee sus prompts y avisos en su propio idioma (*Idioma* en los ajustes del agente), no en este.

Estos valores predeterminados se aplican a **todos los libros cuyo propio campo esté vacío**, de inmediato, no solo a los libros nuevos. Un libro que
define su propio valor en su pestaña *Prompts* lo conserva. Así que:

- define aquí lo que comparten todos tus libros;
- define en la pestaña *Prompts* de un libro lo que es especial para ese libro;
- vacía el campo de un libro para volver al predeterminado de *Ajustes*.

<a id="styles"></a>

### Estilos

Los estilos de texto de tus libros: cómo se ven el texto de las partes en la historia y en el lector. La pestaña lista los estilos **Normal**
y **Book**, que trae la aplicación y no se pueden cambiar (están marcados como *integrado*), y los tuyos. Selecciona un estilo para editarlo a la derecha; los botones debajo de la lista:

- **Duplicar** - crea un estilo propio a partir del seleccionado (también de uno integrado: así es como se empieza), llamado
  *... (copia)*, que luego puedes renombrar y cambiar;
- **Eliminar** - elimina un estilo tuyo; un estilo integrado no se puede eliminar;
- **Establecer como predeterminado** - hace que el estilo sea el que usan los libros, salvo que [elijan otro](books/story-editor.md#text-styles) en el menú de ajustes de la historia. El estilo predeterminado aparece marcado como *predeterminado* en la lista.
- **Exportar** e **Importar** - guardan el estilo seleccionado como un archivo `.style.json`, y añaden los estilos de ese archivo a los tuyos (no se guardan
  hasta que guardes los ajustes). Véase [Compartir un estilo](books/styling-guide.md#share-a-style-export-and-import).

La [guía de estilos](books/styling-guide.md) repasa cada opción y tiene un estilo de ejemplo para descargar.

![La pestaña Estilos, con el estilo de terror de la guía de estilos seleccionado](../images/settings-styles.png)

Para un estilo propio, el formulario de la derecha tiene un **Nombre**, la casilla **Para exportaciones (no se ofrece para los libros)** y dos pestañas. Un estilo es
para libros o para exportaciones: un estilo para exportaciones se ofrece en el [cuadro de diálogo de exportación](books/export.md#print-styles) (la aplicación trae
cuatro, los aspectos de las exportaciones a HTML, EPUB, PDF y Word) y no puede ser el estilo predeterminado de los libros, y un estilo para libros no se ofrece
en el cuadro de diálogo de exportación. La lista los marca como *(exportación)*.

- **Opciones** - en grupos: *Texto* (fuente, tamaño, altura de línea, espaciado de letras y palabras, color, alineación, separación silábica, el espacio
  entre párrafos, la **sangría de primera línea** de los párrafos y una **letra capitular** tras los títulos), *Página* (el ancho de la columna
  de texto, centrada, y el **color de fondo**, que rellena toda el área detrás del texto), *Énfasis, enlaces y código* (los colores del texto en cursiva y en negrita y de los enlaces, la
  fuente del código), *Citas*, *Títulos* (fuente, tamaño del primer nivel, color, alineación, versalitas) y el símbolo y
  color del *Cambio de escena* (`---`). Un campo admite cualquier valor CSS (`1.1rem`, `#336699`, `var(--lumo-body-text-color)`); un campo vacío
  mantiene el aspecto de la aplicación. Los campos de fuente ofrecen pilas de fuentes habituales, y aceptan las tuyas.
- **CSS personalizado** - CSS propio, añadido después de las opciones y aplicado dentro del estilo, de modo que una regla que escribes solo afecta al
  texto con este estilo: `.chat-message-markdown p { text-indent: 1.5em !important; }` sangra los párrafos; las declaraciones simples
  (`font-size: 1.2rem;`) se aplican a todo el texto. Debajo, **CSS generado** muestra la hoja de estilos que crean las opciones (solo lectura).
  Las llaves `{ }` deben coincidir, o el estilo no se guarda.

![La pestaña CSS personalizado de un estilo](../images/settings-styles-css.png)

Debajo del formulario, la **Vista previa** muestra un texto de muestra con el estilo, y sigue cada cambio mientras escribes.

Como las demás pestañas, la pestaña Estilos se guarda con **Guardar** de los ajustes, y **Descartar los cambios** también descarta sus cambios:
duplicar, editar, eliminar y establecer el predeterminado solo cambian la copia de trabajo, y *Cambios sin guardar* (y el bloqueo)
aparece en cuanto haces cualquiera de ellos. Un estilo sin nombre, o un CSS personalizado cuyas llaves no coinciden, se rechaza al guardar.
<a id="exports"></a>

### Exportaciones

Con qué formato se exporta cada archivo: la pestaña tiene una sección para cada formato que tiene algo que configurar (**Texto plano**, **Markdown**, **HTML**,
**Word**, **PDF**, **EPUB**, **Markdown con front matter**, **Vault de Obsidian**; haga clic en su nombre para abrirla).

- **Estilo de impresión** (HTML, Word, PDF y EPUB, los formatos con tipografías y colores): el estilo que el [cuadro de diálogo de exportación](books/export.md) preselecciona
  para el formato. Empieza con el aspecto que la aplicación trae para el formato (**Página HTML**, **Libro en PDF**...); la lista contiene los
  [estilos para exportaciones](#styles), los integrados y los suyos. Elija otro para exportar ese formato con él siempre, hasta que cambie la
  elección en el cuadro de diálogo para una exportación.
- **Front matter** (Markdown con front matter y el vault de Obsidian): el bloque YAML que inicia el archivo, con `{{title}}`, `{{author}}`,
  `{{language}}` y `{{exportDate}}`. Vacío es el bloque predeterminado.
- **Página de título** (todos los formatos excepto los destinados a otras herramientas; el vault de Obsidian la tiene en la nota del libro): la plantilla de la página de título, una plantilla Handlebars que escribe Markdown, con `{{title}}` y
  `{{author}}` (el icono de información del campo los enumera). Vacío es la plantilla predeterminada: el título como encabezado y el autor en cursiva debajo.
  **Rellenar predeterminada** en el mensaje de información le parte de la predeterminada.
- **Declaración de autoría** (todos los formatos excepto el chat de SillyTavern): el texto que la casilla *Incluir la declaración de autoría* de la exportación
  añade al final: `{{title}}`, `{{author}}`, `{{totalWords}}`, `{{writtenWords}}`, `{{generatedWords}}`, `{{writtenPercent}}`,
  `{{generatedPercent}}` y `{{models}}` (los modelos separados por comas, vacío cuando no hay ninguno). Vacío es el texto predeterminado.

Estas son sus versiones: un libro o una carpeta puede tener las suyas, vea [Plantilla de la página de título](books/export.md#title-page-template). La pestaña se guarda con **Guardar** de los ajustes y **Descartar los cambios** descarta sus cambios, igual que las demás. Una plantilla que no es válida
se rechaza al guardar. Un estilo que cree en la pestaña Estilos se puede elegir aquí después de guardar.

![La pestaña Exportaciones, con la sección HTML abierta](../images/settings-exports.png)

<a id="tags"></a>

### Etiquetas

Una tabla de todas sus etiquetas (las que asigna a libros, lorebooks y entradas de lorebook) con el número de objetos a los que
está asociada cada etiqueta. Haga clic en el encabezado de una columna para ordenar por ella. A diferencia de las demás pestañas, actúa al momento: no forma parte de los
cambios sin guardar y no tiene nada que guardar.

![La pestaña Etiquetas](../images/settings-tags.png)

| Botón | |
|---|---|
| **Renombrar** (lápiz) | Pide el nuevo nombre de la etiqueta; cada objeto conserva la etiqueta con el nuevo nombre. |
| **Eliminar** (papelera) | Pide confirmación y luego elimina la etiqueta y la quita de todos los objetos que la tienen. |

Si el nuevo nombre es el de otra etiqueta (los nombres se comparan sin distinguir mayúsculas), renombrar haría que las dos etiquetas fueran
la misma, así que se le pregunta si desea **fusionarlas**. Fusionar mueve todo lo etiquetado con la etiqueta renombrada a la
etiqueta existente y elimina la renombrada; un objeto que tiene ambas termina con la etiqueta existente una sola vez. Cancelar y no
cambia nada: la etiqueta conserva su nombre anterior.

![Pregunta para fusionar dos etiquetas](../images/tag-merge.png)

<a id="keyboard-shortcuts"></a>

### Atajos de teclado

Las teclas de los [atajos del editor de historias](books/story-editor.md#keyboard-shortcuts) (funcionan en el
editor de historias en un ordenador; el [diseño para teléfono](phone.md) no tiene ninguno).
La tabla enumera cada acción, también las que traen las [extensiones](extensions/index.md), con su tecla, y las teclas de los botones de la ventana [Organizar](books/folders.md#organise) de la lista de libros. Cada elemento del menú de ajustes de la historia, del menú de una parte y del menú del clic derecho de su texto es aquí una acción (la mayoría no tiene tecla hasta que le asigna una), y los menús muestran la tecla junto al elemento.

| Botón | |
|---|---|
| **Grabar** | Pulse la nueva combinación de teclas (modificadores primero o a la vez, p. ej. `Ctrl+Shift+K`); un clic en cualquier otro lugar cancela. |
| **Restablecer el valor predeterminado** | Devuelve a la acción su tecla predeterminada. |
| **Vaciar** | La acción no tiene tecla. |

Una nota en rojo indica una tecla que dos acciones comparten, una tecla que el navegador se reserva para sí (`Ctrl+N`, `Ctrl+T`, `Ctrl+W` y similares,
la página nunca las recibe) y `Alt` con una letra (en algunos teclados escribe un carácter, p. ej. en macOS). Una tecla sin
`Ctrl`, `Alt` o `Meta` funciona solo cuando ningún campo de texto tiene el foco. Las teclas se guardan con **Guardar**, como los demás
ajustes, y se aplican al momento en las historias abiertas.

![Ajustes, Atajos de teclado](../images/settings-shortcuts.png)

<a id="notifications"></a>

### Notificaciones

Una notificación de escritorio del navegador cuando un modelo termina, para que pueda dejar la pestaña mientras trabaja. La pestaña tiene una casilla
**Usar notificaciones de escritorio**, los botones de abajo y una lista de lo que notifica:

- **Generación de la historia**, **Anotar con IA**, **Anotar con IA (automático)**, **Reescribir**, **Reescribir (automático)**, **Redactar instrucciones**,
  **Resumen**, **Volver a resumir todos los resúmenes** y **Asistente de ayuda**, y lo que añaden las [extensiones](extensions/index.md), con el nombre de la extensión
  (*Reviewer: generate a review*, *Lore Generator: generate lore*, *Image Generator: generate the image*, *Arc Outline: generate chapters*...).
  Las entradas de *Manga Generator* marcadas *(automático)* son su modo continuo.
- Las entradas marcadas *(automático)* son las ejecuciones que el libro inicia por sí mismo: la [anotación automática](books/story-editor.md) tras una generación
  y la reescritura que la sigue. Se eligen aparte del cuadro de diálogo que abre usted: con **Anotar con IA** marcada y
  **Anotar con IA (automático)** no, la anotación automática es silenciosa; con solo la entrada automática marcada, solo la anotación
  automática notifica.
- Una ejecución que termina con un error también notifica (*… falló*); una que detiene usted mismo no.

La casilla activa las notificaciones para su cuenta y habilita dos botones. **Registrar este navegador** pide al navegador el
permiso (desde ese clic); la línea bajo los botones indica lo que dice este navegador: permitido, bloqueado (permítalo en los ajustes de
sitio del navegador) o aún no preguntado. El permiso pertenece al navegador, no a su cuenta: cada navegador o dispositivo en el que quiera
notificaciones debe registrarse una vez; la casilla y la lista son las mismas en todas partes. **Enviar una notificación de prueba** muestra una al
momento, también mientras la página está en primer plano.

El campo **Filtro** reduce la lista por nombre; **Marcar las mostradas** y **Desmarcar las mostradas** actúan sobre las entradas que muestra y dejan
las demás como están. Una notificación solo se muestra mientras no está mirando la página (otra pestaña, o la ventana no está en primer plano); hacer clic en ella
devuelve la página.

Las notificaciones necesitan una dirección `https://` o `localhost`: en una dirección `http://` plana de un servidor de su red el navegador
no las ofrece, y la pestaña lo indica. Una pestaña que el navegador ha puesto en reposo (normalmente en un teléfono) tampoco puede mostrarlas. La pestaña se guarda con
**Guardar** de los ajustes, como las demás.

<a id="extension-settings"></a>

### Ajustes de extensiones

Ajustes de las [extensiones](extensions/index.md) instaladas, una sección por extensión (por ejemplo *Reviewer Settings*
y *SideQuery Settings*). Se guardan con el mismo botón **Guardar**. La pestaña está vacía cuando no hay ninguna extensión con
ajustes instalada.

![La pestaña Ajustes de extensiones con los ajustes de Lore Generator](../images/settings-extensions.png)
