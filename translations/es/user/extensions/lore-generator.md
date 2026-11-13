---
label: Generador de lore
order: 10
---

# Generador de lore

Permite que el modelo escriba el contenido de una [entrada de lorebook](../lorebooks/entries.md) por ti. Lee el lore que ya tienes, para que la nueva entrada encaje con el mundo, y compacta el resultado en la forma breve y densa que mejor funciona en las entradas de lore.

!!!
El generador de lore funciona en el [diseño para teléfono](../phone.md#extensions): el botón de varita está en la fila de una entrada, y la ventana muestra el prompt arriba y el resultado debajo.
!!!

<a id="opening-the-generator"></a>

## Abrir el generador

Con la extensión instalada, cada fila del editor de lorebooks tiene un botón de varita (**Generate lore entry**). Abre el generador para esa entrada. Si la entrada ya tiene contenido, el diálogo lo indica en la parte superior: copiar el resultado a la entrada reemplaza el contenido.

El [editor a pantalla completa](../lorebooks/lorebooks.md#editing-in-full-screen) también tiene la varita: en cada fila de su lista de entradas, y como **Generate lore entry** en la fila del nombre de una pestaña de entrada. Lo que copias a la entrada aparece de inmediato en su pestaña. Si se abre desde un libro (su pestaña *Lorebook*, o la pestaña de lorebook de la historia), el editor conoce el libro, así que el libro paga al modelo igual que en la página del lorebook.

El generador empieza con tu **Modelo predeterminado** y tu **Protocolo predeterminado** de *Ajustes*, ver [Cuenta y ajustes](../account.md). Las listas **Modelo** y **Protocolos** bajo el encabezado del diálogo permiten usar otro modelo o protocolo solo para esta generación (la elección no se guarda); el protocolo es opcional. Sin modelo, solo muestra una advertencia.

![El generador de lore](../../images/plugin-loregenerator.png)

<a id="what-the-model-sees"></a>

## Qué ve el modelo

Las dos opciones de la parte superior del diálogo deciden qué lore se incluye en la solicitud:

| Opción | |
|---|---|
| **Current Lorebook** (predeterminado) / **All Lore** | Solo las entradas del lorebook en el que está la entrada, ignorando sus [sub-lorebooks](../lorebooks/lorebooks.md); o las entradas de ese lorebook junto con todos sus sub-lorebooks. |
| **Only up to position (n)** (predeterminado) / **All entries** | *n* es el orden de la entrada. Solo se muestran las entradas que van antes (un orden menor, o el mismo orden y creadas antes), así el modelo construye sobre lo que ya existe y no recibe lore escrito después de la entrada. *All entries* muestra todo. |

La entrada que estás generando nunca se incluye, con o sin contenido. Se ignoran si otras entradas o lorebooks están activados, sus etiquetas y filtros, y su modo de inserción: lo que eliges aquí es lo que se envía, en el orden en que las entradas se insertan en un prompt. Las plantillas de las entradas se procesan como de costumbre.

## Prompts

El prompt de generación es lo que pides al modelo que escriba. Los prompts aparecen en el cuadro junto a las opciones, en la misma fila superior; el texto del seleccionado está en el área de texto de la izquierda, y puedes cambiarlo para una sola generación sin guardarlo. Las dos mitades del diálogo están separadas por un divisor que puedes arrastrar.

| Botón | |
|---|---|
| **Guardar** | Guarda el texto con el nombre seleccionado. |
| **Save as** | Pide un nombre y guarda el texto como un prompt nuevo (o reemplaza uno con ese nombre, tras confirmar). |
| **Eliminar** | Elimina el prompt seleccionado. Al borrar el último, vuelve el prompt integrado `DEFAULT`. |

El prompt integrado tiene marcadores entre corchetes (el tema, los detalles a incluir, la longitud, la estructura): rellénalos antes de ejecutarlo. Los prompts pertenecen a tu cuenta, y el último que usaste vuelve a estar seleccionado la próxima vez que se abre el diálogo.

<a id="generation-and-compaction"></a>

## Generación y compactación

El lado derecho tiene dos pestañas.

**Generación**: **Run Generation** envía el lore seguido del prompt de generación. La respuesta aparece a medida que se escribe, con el razonamiento del modelo (cuando lo da) en una sección *Thinking Process* encima. **Detener** (en lugar de *Run Generation*) lo termina antes; lo escrito hasta ese momento se conserva.

**Compactación**: disponible cuando hay una generación. **Run Compaction** envía el mismo lore seguido del prompt de compactación, con el texto generado dentro, y el modelo lo condensa en una entrada compacta. El prompt de compactación se configura en *Ajustes → Extension Settings → Lore Generator*; si está vacío, se usa el integrado (quita la prosa y conserva los datos). Volver a generar descarta la compactación.

![La compactación de una entrada generada](../../images/plugin-loregenerator-compaction.png)

Mientras el modelo trabaja, todos los controles están bloqueados salvo **Detener**; tampoco puedes cerrar el diálogo.

<a id="remembered-per-entry"></a>

### Recordado por entrada

Al pulsar **Copy to Lore Entry**, las opciones de esa generación se guardan con la entrada: el texto del prompt, de qué prompt guardado procede, las elecciones de lore y entradas, y las categorías de nombres marcadas. Al abrir de nuevo el generador para esa entrada, se restauran, con prioridad sobre el prompt que usaste por última vez y sobre tus categorías de nombres guardadas. Una entrada que nunca se generó se abre con los valores habituales. Las opciones pertenecen a la entrada, así que viajan con las copias de seguridad y las exportaciones del lorebook.

<a id="tools"></a>

### Herramientas

Cuando hay instalado un conjunto de [herramientas para el modelo](tools.md) (nombres del [Generador de nombres](name-generator.md) o del [Proveedor de nombres](name-provider.md), lugares del [Proveedor de lugares](place-provider.md)), la fila superior tiene un botón **Herramientas**. Abre la ventana donde eliges qué conjuntos, y de los que tienen categorías, qué categorías, puede usar el modelo; ver [Herramientas para el modelo](tools.md) para saber cómo funciona. Mientras algo esté marcado, el modelo puede llamar a las herramientas durante la generación (no la compactación), y la pista de cada conjunto que recibe se añade al final del prompt de generación. La elección se guarda con tu cuenta y con la entrada.

<a id="attachments"></a>

### Adjuntos

Bajo el prompt puedes añadir archivos para que el modelo los vea: imágenes de referencia, un mapa, una hoja de personaje. Suéltalos en el área de subida, elígelos, o pégalos en el prompt con Ctrl+V (una captura de pantalla o un archivo copiado se convierte en adjunto; el texto pegado se inserta como de costumbre). Hasta 20 MB cada uno. Se listan con su nombre, tipo y tamaño, y el botón de papelera elimina uno. Se envían con la generación, no con la compactación, y solo puedes añadirlos y eliminarlos mientras no haya ninguna generación en curso.

Que el modelo pueda usarlos depende del modelo y del [proveedor de inferencia](../inference-providers.md): las imágenes se envían como imágenes, el audio solo como WAV o MP3, y los demás archivos como archivos. Cuando el proveedor rechaza un adjunto, recibes un mensaje y no se genera nada.

<a id="copying-the-result"></a>

## Copiar el resultado

**Copy to Lore Entry** está disponible cuando la compactación tiene un resultado. El texto compactado pasa a ser el **content** de la entrada, y el texto generado antes de la compactación va a su **note**. Ambos se reemplazan. Si la compactación es un solo bloque de código (el prompt integrado lo pide, para que el texto sea fácil de copiar), se quita el bloque de código que la envuelve. El lorebook se actualiza y el diálogo se queda abierto; **Salir** lo cierra.
<a id="commands"></a>

## Comandos

El generador también funciona sin el diálogo, como tres [comandos](../commands/index.md) para la línea de comandos y para
[agentes](../agents.md). Hacen los mismos dos pasos que el diálogo y, como el diálogo, no guardan nada: devuelven
el texto, y ponerlo en una entrada (lo que hace **Copy to Lore Entry**) depende de quien llama, así que los tres solo leen. El
lorebook y la entrada se indican por número, como en `/list-lorebooks` y `/list-lore-entries`; el modelo es tu modelo
y protocolo predeterminados.

| Comando | Qué hace |
|---|---|
| `/list-lore-templates [template]` | Lista tus prompts de generación guardados (nombre, longitud, inicio del texto) y el usado por última vez. Con un nombre devuelve el texto completo de ese prompt. |
| `/generate-lore-entry [prompt [template [lorebook [entry [order [scope [limit [images [compact]]]]]]]]]` | Genera un texto con el lore del lorebook, lo compacta (como `/compact-lore-entry`) y devuelve el texto compactado; con `compact` `false` devuelve el texto generado tal cual. El texto enviado es una plantilla seguida del prompt de quien llama (ver más abajo). Con `images` el modelo también mira imágenes, como con los adjuntos del diálogo. |
| `/compact-lore-entry text [lorebook [entry [order [scope [limit]]]]]` | Compacta el texto con el mismo lore y devuelve el resultado, sin el bloque de código alrededor. |

La entrada para la que es el texto decide qué lore ve el modelo. Indica su identificador o su número como `entry` (y el `lorebook` en el que está, por identificador o número, cuando des el número) y el lore es el mismo que en
el diálogo: con `limit` `UP_TO_POSITION` (el predeterminado) las entradas anteriores a ella, nunca la propia entrada. Una entrada que todavía no existe no tiene número: indica el `order` que tendrá en su lugar (el `order` de `/list-lore-entries`, el menor va primero) y el modelo ve las entradas anteriores a ese orden; sin ninguno de los dos, el texto se escribe para una entrada nueva después de todas las demás.
`scope` es `CURRENT_LOREBOOK` (el predeterminado) o `ALL_LORE`, `limit` es `UP_TO_POSITION` o `ALL_ENTRIES`, como en el diálogo.

La calidad de una generación depende de su prompt, así que el comando siempre parte de uno de tus prompts de generación guardados (ver
[Prompts](#prompts)): el indicado en `template`, o el usado por última vez si no se indica ninguno. El `prompt` de quien llama
va después bajo el encabezado *Task for this entry* y es donde van el tema, los datos, la longitud y las secciones, lo que pida la plantilla. `template` `none` envía solo el prompt, para quien llama que haya escrito el prompt completo por sí mismo, por ejemplo tras obtener el texto de una plantilla con `/list-lore-templates` y editarlo. Un comando
que se llama con una plantilla y sin prompt envía solo la plantilla.

Las `images` de `/generate-lore-entry` son las imágenes que el diálogo adjuntaría a la generación. Un [agente](../agents.md) pasa una lista de
rutas de archivos de imagen de su espacio de trabajo (`/refs/map.png`; como máximo 8, PNG, JPEG, GIF o WebP, reducidas cuando son grandes), que colocas allí con
**Upload file** en la pestaña Workspace. La barra de comandos no tiene forma de nombrar una imagen, así que `images` es para agentes. Es tu modelo predeterminado el que mira las
imágenes: si no admite imágenes, el comando falla y lo indica. Solo la generación toma imágenes, como en el diálogo; la compactación trabaja solo con el texto.

El parámetro `compact` de `/generate-lore-entry` es `true` por defecto: el comando genera y compacta en una sola llamada, así que un [agente](../agents.md) necesita un único comando y no puede olvidar el segundo paso. Pasa `compact` `false` para obtener el texto generado largo (por ejemplo para revisarlo o editarlo), y ejecuta `/compact-lore-entry` sobre él más tarde.

El modelo no recibe herramientas aquí. Quien llama que quiera nombres los pide antes, con `/generate-names` del [Generador de nombres](name-generator.md) o `/pick-real-names` del [Proveedor de nombres](name-provider.md), y escribe en el prompt los que le gusten. Un agente también puede tomar el texto de una plantilla de `/list-lore-templates`, cambiarlo y pasar el
resultado como prompt.

<a id="storage"></a>

## Almacenamiento

Los prompts y el prompt de compactación se guardan con tus ajustes, así que las [copias de seguridad de la base de datos](../administration/database-backups.md)
los incluyen. No se guarda nada con el lorebook; el texto generado existe solo en el diálogo hasta que lo copias.
