---
label: Resúmenes
order: 60
---

# Resúmenes

Un modelo solo puede leer una cantidad limitada de texto a la vez (su contexto). Cuando un libro crece más allá de ese límite, Marginalia omite las partes más antiguas, y el modelo olvida lo que ocurrió en ellas. Los **resúmenes** conservan esa historia en el prompt en forma condensada.

<a id="how-summaries-work"></a>

## Cómo funcionan los resúmenes

Un resumen pertenece a una parte y abarca **esa parte y todas las partes anteriores**, hasta la parte previa que tenga resumen:

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
            [S1]            [S2]
└──── S1 ────┘└──── S2 ──────┘ enviadas completas ...
```

Aquí *S1* (en la parte #4) abarca las partes #1-#4 y *S2* (en la parte #8) abarca las partes #5-#8.

Al generar la siguiente parte:

- todos los resúmenes de la rama activa se incluyen en la plantilla maestra, del más antiguo al más reciente, en el bloque `{%{{{summaries}}}%}` (*Summary of story so far* en la plantilla por defecto);
- las partes cubiertas por resúmenes **ya no** se envían;
- las partes posteriores al resumen más reciente (#9 y #10 en el ejemplo anterior) se envían completas, tantas como quepan.

Los resúmenes se crean solo cuando los pides; Marginalia no resume por su cuenta.

<a id="creating-a-summary"></a>

## Crear un resumen

1. Elige la parte que cierra un tramo de la historia que quieres condensar, por ejemplo, la última parte de un capítulo.
2. En su menú (☰) elige **Generar resumen**.
3. El diálogo de resumen muestra el resumen mientras el modelo lo escribe (y su razonamiento, en el caso de los modelos de razonamiento). **Detener** lo interrumpe y descarta el resumen. El diálogo no bloquea la historia: **Cerrar** mientras el modelo escribe lo lleva al [panel de ejecuciones](story-editor.md#the-panel-of-the-runs) y el resumen sigue generándose (la parte no se puede eliminar ni fusionar mientras tanto); ábrelo de nuevo desde allí para leerlo.
4. Cuando termine, cierra el diálogo con **Salir**.

![Un resumen en generación](../../images/summary-dialog.png)

El menú de la parte ahora tiene **Ver resumen**, que vuelve a abrir el resumen, y **Eliminar resumen**, que lo borra. Para cambiar un resumen, edítalo en el [resumen general de resúmenes](#the-summaries-overview), o elimínalo y vuelve a generarlo, quizá con un [prompt de resumen](#the-summary-prompt) distinto.

El resumen lo escribe el modelo del libro, con el [protocolo](../protocols.md) del libro, y debe caber en el [límite de contexto](../protocols.md#limits) junto con el texto de las partes que cubre. Para resumir un tramo largo con un modelo pequeño, resúmelo en pasos más pequeños.
<a id="the-summaries-overview"></a>

## Vista general de los resúmenes

El botón **Resúmenes** (el icono de lista, a la izquierda del menú de ajustes ⚙) en la barra inferior del editor de la historia abre la vista general de todos los resúmenes de la rama activa. Pase el cursor sobre el botón para ver cuántos tokens ocupan los resúmenes en uso en el prompt.

![El botón Resúmenes con los tokens de los resúmenes en uso](../../images/summaries-menu-button.png)

El diálogo enumera los resúmenes que enviará la siguiente generación, del más antiguo al más reciente, y la suma de sus tokens en la parte superior.

![La vista general de los resúmenes](../../images/summaries-dialog.png)

| Columna | |
|---|---|
| **ID** | ID en la base de datos del resumen. Un `-` marca un resumen que fue [reemplazado](#meta-summaries) por un metaresumen. |
| **ID del mensaje** | ID en la base de datos de la parte a la que pertenece el resumen (el mismo número que el esquema del editor de la historia muestra como *DB ID*). |
| **Orden** | Posición de esa parte en la rama; `1` es la primera parte. |
| **Contenido** | El razonamiento del modelo (plegado, si era un modelo de razonamiento) y el texto del resumen, en un área con desplazamiento de la misma altura para todos los resúmenes. |
| **Tokens** | Tamaño del texto del resumen. |
| **Seleccionar** | Marca el resumen para un [metaresumen](#creating-a-meta-summary). |
| icono de papelera | Elimina el resumen. |

Los resúmenes que se fusionan en un metaresumen se muestran debajo de él, véase [más abajo](#meta-summaries). No tienen casilla **Seleccionar**, ni icono de papelera, ni lápiz: solo se pueden cambiar los resúmenes que están en uso.

La barra de menú sobre la tabla tiene **Buscar y reemplazar** (véase [más abajo](#search-and-replace-in-the-summaries)), **Crear resúmenes...** (véase [más abajo](#create-summaries)), **Volver a resumir todos los resúmenes** (véase [más abajo](#resummarize-all-summaries)), **Exportar resúmenes** e **Importar resúmenes** (véase [Exportar e importar](#export-and-import-of-the-summaries)); las [extensiones](../extensions/index.md) pueden añadir herramientas a ella.

<a id="editing-a-summary"></a>

### Editar un resumen

Haga clic en el lápiz a la derecha del contenido de un resumen. El texto se convierte en un área de texto del mismo tamaño. Haga clic en el check verde para guardar el cambio, o en el icono de papelera rojo para descartarlo. El recuento de tokens se vuelve a calcular después de guardar.

![Editar un resumen](../../images/summary-edit.png)

Editar un resumen cambia lo que recibe el modelo, nada más: el resumen sigue siendo válido. Solo se puede editar el texto, no el razonamiento.

<a id="search-and-replace-in-the-summaries"></a>

### Buscar y reemplazar en los resúmenes

**Buscar y reemplazar** en la barra de menú de la vista general cambia un nombre o una frase solo en los resúmenes, por ejemplo cuando se ha renombrado un personaje, y deja la historia como está. Es la [búsqueda y reemplazo de la historia](story-editor.md#search-and-replace) con los mismos campos y opciones, salvo que no hay **Buscar en los resúmenes** (los resúmenes son lo único en lo que busca). La tabla enumera cada resumen con una coincidencia, y las coincidencias dentro de él.

También busca en los resúmenes que un metaresumen representa y en el resumen que un metaresumen reemplazó (se muestran debajo del metaresumen y vuelven cuando se [deshace](#unwinding-a-meta-summary)): una coincidencia en ellos aparece listada junto al metaresumen que los contiene.

El texto de los resúmenes fusionados forma parte de lo que hace válido a un metaresumen, así que el reemplazo vuelve a calcular el valor de comprobación de esos metaresúmenes y siguen siendo válidos. **Revalidar todos los resúmenes** (activado por defecto) hace lo mismo para cada resumen del libro, también para los que el reemplazo no tocó y para los que ya estaban desactualizados; véase [la búsqueda en la historia](story-editor.md#search-and-replace). Con esta opción desactivada, un metaresumen de una rama que no se busca (**Buscar en todas las ramas** desactivado) quedaría desactualizado, y la ventana le indica cuántos son y le pregunta si quiere continuar.

<a id="create-summaries"></a>

### Crear resúmenes

**Crear resúmenes...** (también en el menú de ajustes ⚙ de la historia, **Análisis**, y se ofrece tras una [importación](story-editor.md#after-an-import)) escribe de una vez los resúmenes de una historia larga, por ejemplo de un libro que ha importado: divide la historia en bloques y hace un resumen de cada uno, uno tras otro, empezando por el más antiguo. Un resumen cubre las partes hasta la parte posterior al resumen anterior, como lo hace el resumen de una sola parte, de modo que los bloques se suceden sin hueco.

- **Dónde empieza.** Solo las partes **posteriores al resumen más reciente** reciben resúmenes (toda la historia, si no hay ninguno). Los resúmenes existentes se ignoran: no se vuelven a escribir, no se cambian y no deciden dónde terminan los nuevos bloques. Para volver a escribir los antiguos, use [Volver a resumir todos los resúmenes](#resummarize-all-summaries).
- **Dividir la historia por** indica dónde termina un bloque:
  - **Número de tokens**: después de la parte que lleva el bloque hasta **Tokens por resumen** (el corte se redondea al final de esa parte; un bloque tiene al menos esa cantidad de tokens, las partes que haga falta).
  - **Capítulos**: antes de una parte que inicia un capítulo, de modo que el resumen llega hasta la parte anterior a ella. Un capítulo es una parte que empieza con un encabezado (el nivel de encabezado más superficial con el que empieza una parte: `#` en un libro creado con la [importación](managing-books.md#importing-a-finished-manuscript)). **Mínimo de tokens antes de que un capítulo cierre un resumen** evita que los capítulos cortos tengan resumen propio: cuando el bloque acumulado tiene menos tokens, el capítulo que empieza no cierra el bloque y se resume junto con el siguiente.
  - **Número de partes**: después de cada **Partes por resumen** partes.
- **Hasta la parte** detiene el rango en una parte (vacío es el final de la historia). **Resumir también el resto al final** da un resumen a las partes posteriores al último corte, que son menos que un bloque (con capítulos, solo cuando tienen el mínimo de tokens); desactívelo para dejar sin resumen las partes más recientes, como están mientras escribe.
- Un bloque nunca es más largo de lo que el modelo puede leer (80 % del contexto sin la respuesta): termina antes de la parte que lo excedería. Una sola parte más larga que eso falla con el error habitual.

![Crear resúmenes: por capítulos, con un mínimo de 500 tokens](../../images/summaries-create.png)

Una línea bajo las opciones indica cuántos resúmenes escribirá, para cuántas partes y tokens. **Iniciar** abre la ventana con el número del resumen que se escribe y su texto según va llegando. La ventana se cierra cuando se escribe el último.

![La ventana mientras se escriben los resúmenes](../../images/summaries-create-progress.png)

Cada resumen se guarda en cuanto se escribe: **Detener**, o una petición que falla, deja los resúmenes anteriores, y **Crear resúmenes** continúa desde ahí la próxima vez (empieza después del resumen más reciente). El libro no debe estar bloqueado por un [agente](../agents.md#the-book-is-locked); cada resumen es una petición al modelo del libro.

<a id="resummarize-all-summaries"></a>

### Volver a resumir todos los resúmenes

Después de cambiar el prompt de resumen (o el prompt de metaresumen), los resúmenes que ya tiene siguen usando el anterior. **Volver a resumir todos los resúmenes** los vuelve a escribir todos con el modelo del libro, para que tengan el mismo estilo. Pregunta primero y dice cuántos resúmenes va a escribir: una petición por cada uno.

![La pregunta antes de volver a escribir los resúmenes](../../images/summaries-resummarize-confirm.png)

- Empieza por el resumen **más antiguo** y continúa hasta el más reciente.
- Un **metaresumen** se escribe después de los resúmenes que representa (los que se muestran debajo de él, los metaresúmenes internos antes que los externos), a partir de sus textos **nuevos**, de modo que un metaresumen nunca se hace con texto antiguo.
- Cada resumen conserva su lugar en su parte. El valor de comprobación de cada uno se calcula de nuevo a partir de la historia tal como está ahora, de modo que un resumen que estaba **desactualizado** queda válido después (el modelo leyó el texto actual de las partes).
- La ventana muestra el número del resumen que se escribe y su texto según va llegando. Los textos nuevos se guardan solo cuando se termina el **último**: si pulsa **Detener**, o falla una petición (el prompt no cabe en el contexto, el modelo no responde nada), cada resumen queda como estaba.

![La ventana mientras se vuelven a escribir los resúmenes](../../images/summaries-resummarize.png)

Los resúmenes de otras ramas no cambian. Asegúrese de que el modelo no esté ocupado y de que ningún agente trabaje en el libro. Las ediciones que haya hecho a mano en los textos se reemplazan; exporte antes los resúmenes si quiere conservarlos.

<a id="export-and-import-of-the-summaries"></a>

### Exportar e importar los resúmenes

**Exportar resúmenes** descarga los resúmenes en uso de la rama activa como un único archivo de texto (`<libro>-summaries.md`), del más antiguo al más reciente, cada uno bajo una línea como `<!-- summary part="..." order="3" -->`. Edite los textos en cualquier editor, conserve las líneas de encabezado y devuelva el archivo con **Importar resúmenes**: elija el archivo, la tabla indica qué ocurrirá con cada resumen, y no se escribe nada hasta que pulse **Importar**.

| Estado | |
|---|---|
| **Cambiará** | La parte que nombra el encabezado tiene un resumen en uso y su texto es distinto. |
| **Cambiará (por posición)** | Véase más abajo. |
| **Sin cambios** | El texto es el que ya tiene el resumen. |
| **Parte no encontrada** | La rama no tiene ningún resumen en uso para ese encabezado. |
| **Texto vacío** | El texto bajo el encabezado está vacío; una importación nunca vacía un resumen. |

![La ventana de importación con un archivo editado](../../images/summaries-import.png)

Un encabezado nombra la parte por su ID, así que un archivo devuelve los resúmenes al libro del que procede. Para mover los resúmenes a otro libro (una copia tiene partes con otros IDs), marque **Emparejar por posición**: un resumen cuya parte no está en esta rama pasa al resumen de la parte en la misma posición (el `order` del encabezado; la raíz es 1). Revise antes la tabla: una posición solo encaja cuando las dos historias tienen la misma forma.

El archivo contiene los resúmenes que usa la generación; un metaresumen es uno de ellos. Los resúmenes que representa un metaresumen no están en él y nunca se cambian: su texto forma parte de lo que hace válido al metaresumen (véase [Metaresúmenes](#meta-summaries)), así que cambiarlos lo dejaría desactualizado. Una importación cambia solo el texto y el recuento de tokens de un resumen, lo mismo que hace el lápiz, de modo que ningún resumen queda desactualizado por ella. Se rechaza mientras un agente trabaja en el libro.

<a id="deleting-a-summary"></a>

### Eliminar un resumen

El icono de papelera de un resumen hace lo mismo que **Eliminar resumen** en el menú de la parte. En el caso de un metaresumen, pregunta qué hacer; véase [Deshacer un metaresumen](#unwinding-a-meta-summary).
<a id="meta-summaries"></a>

## Metarresúmenes

Los resúmenes permanecen en el prompt para siempre, así que un libro largo llena el contexto con ellos. Un **metarresumen**
condensa varios resúmenes en uno: es un resumen de resúmenes.

```
#1  #2  #3  #4  #5  #6  #7  #8  #9  #10
        [S1]        [S2]        [S3]
        └─────────── M ─────────────┘
```

Aquí *M* fusiona *S1*, *S2* y *S3*. Pertenece a la parte del resumen fusionado más reciente (#9 arriba, que tenía *S3*)
y sustituye a ese resumen en la parte. A partir de entonces:

- solo *M* entra en el bloque `{%{{{summaries}}}%}`, en lugar de *S1*, *S2* y *S3*;
- *S1* y *S2* permanecen en sus partes, pero no se envían. Se muestran debajo de *M* en la vista general;
- *S3*, el resumen que *M* sustituyó, se conserva dentro de *M*, mostrado debajo de él con una insignia **Reemplazado**.

Las partes cubiertas por los resúmenes fusionados siguen sin enviarse completas; un metarresumen no cambia lo que cubren
los resúmenes, solo la longitud del texto que se usa para ellos.

<a id="creating-a-meta-summary"></a>

### Crear un metarresumen

1. Abre la [vista general de resúmenes](#the-summaries-overview).
2. Marca la casilla **Seleccionar** de los resúmenes que quieres fusionar. Al menos dos. Marca solo los resúmenes en uso (los
   del nivel superior de la cuadrícula).
3. Haz clic en **Crear metarresumen**.

Marginalia fusiona todo desde el resumen marcado más reciente hasta el más antiguo, incluidos los resúmenes intermedios
que no estaban marcados. El diálogo de metarresumen funciona como el diálogo de resumen: muestra el texto según lo escribe
el modelo, y **Detener** lo cancela sin cambiar nada. Al terminar, cierra el diálogo con **Salir** y la vista general
mostrará el resultado.

![Resúmenes marcados y metarresumen en generación](../../images/meta-summary-create.png)

Un metarresumen también puede fusionar metarresúmenes: márcalos como cualquier otro resumen. Los fusionados se muestran
debajo del nuevo, con sus propios resúmenes fusionados debajo de ellos.

Los resúmenes a fusionar y la petición para el modelo deben caber en el [límite de contexto](../protocols.md#limits), así
que con un modelo pequeño fusiona unos pocos resúmenes a la vez.

<a id="unwinding-a-meta-summary"></a>

### Deshacer un metarresumen

Eliminar un metarresumen (el icono de papelera en la vista general, o **Eliminar resumen** en el menú de la parte) pregunta:

| Botón | |
|---|---|
| **Deshacer** | Elimina el metarresumen y devuelve a la parte el resumen que sustituyó. Los resúmenes fusionados en un metarresumen formado por metarresúmenes vuelven de un nivel en un nivel, tal como se crearon. |
| **Eliminar** | Elimina el metarresumen y deja la parte sin resumen. Los resúmenes que fusionaba vuelven a estar en uso (pero el que sustituyó se pierde). |
| **Cancelar** | No hace nada. |

Después de deshacer, los resúmenes que estaban fusionados vuelven a estar en uso y la vista general los muestra en el nivel superior.

![Eliminar un metarresumen](../../images/meta-summary-delete.png)

<a id="the-meta-summary-prompt"></a>

### El prompt de metarresumen

La petición para un metarresumen es el **Prompt de metarresumen predeterminado** del libro (en la pestaña [Prompts](prompts.md), o
el predeterminado de *Configuración → Plantillas*). Recibe dos variables:

| Variable | |
|---|---|
| `{%{{{summaryBlocks}}}%}` | Los resúmenes a fusionar, del más antiguo al más reciente, separados por una línea vacía. |
| `{%{{{backgroundLore}}}%}` | Las entradas del lorebook que estaban activas cuando se escribió la parte del metarresumen. |

El prompt integrado pide encadenar los eventos en entradas más breves —aproximadamente la mitad de tamaño— sin perder
nombres, objetos, acuerdos ni hilos de trama sin resolver, y sin añadir nada nuevo.

<a id="when-summaries-become-outdated"></a>

## Cuándo los resúmenes quedan desactualizados

Un resumen solo es válido para el texto exacto del que se hizo. Cuando [editas](story-editor.md#editing-the-text) o
[eliminas](story-editor.md#deleting-a-part) una parte cubierta por un resumen, este deja de coincidir.

Los resúmenes desactualizados no se envían al modelo. Marginalia lo comprueba al generar la siguiente parte y pregunta
*The summaries of the messages with IDs: [...] are outdated ... Continue generation and delete them?* Responde **Sí** para
eliminarlos y generar; las partes que cubrían se envían completas de nuevo (en lo que quepa) hasta que vuelvas a resumirlas.
Responde **No** para detenerte: no se elimina nada y puedes ocuparte antes de los resúmenes.

En la [vista general de resúmenes](#the-summaries-overview), un resumen desactualizado tiene un **fondo rojo** y una etiqueta
**Desactualizado**, y sus tokens no se cuentan en la suma de arriba. Si todavía dice lo correcto (por ejemplo, corregiste una
errata), pulsa el botón de **comprobar** de su fila para **revalidarlo**, o elige **Revalidar todos los resúmenes** en la barra
de menús para revalidar todos los desactualizados a la vez. Si no, elimínalo o genéralo de nuevo. Un metarresumen se comprueba
como cualquier resumen; los resúmenes que fusiona forman parte de él y nunca quedan desactualizados por sí solos. No se puede
revalidar mientras un agente trabaja en el libro.

Cuando guardas la [edición de una parte](story-editor.md#editing-the-text) que dejaría resúmenes desactualizados, Marginalia
te avisa primero y te deja volver a editar, **Guardar de todos modos** o guardar y revalidar los resúmenes.

![La pregunta cuando un guardado deja resúmenes desactualizados](../../images/summaries-save-warning.png)

Después de **Guardar de todos modos**, la vista general de resúmenes muestra el resumen como desactualizado (en rojo, con la etiqueta
**Desactualizado**, y sin contarse en la suma de arriba):

![Un resumen desactualizado en la vista general](../../images/summaries-outdated.png)

Una [búsqueda y reemplazo](story-editor.md#search-and-replace) en la historia mantiene válidos los resúmenes que cubre y avisa de
los que quedarían desactualizados. La [búsqueda y reemplazo de los resúmenes](#search-and-replace-in-the-summaries) mantiene válidos
los metarresúmenes.

Un metarresumen queda desactualizado cuando se edita o elimina una parte que cubre, pero también cuando se elimina un resumen que fusionaba.
(Por eso los resúmenes fusionados no se pueden editar uno a uno: el cambio dejaría desactualizado el metarresumen. La
[búsqueda y reemplazo](#search-and-replace-in-the-summaries) recalcula el valor de comprobación del metarresumen.) Quitar un
metarresumen desactualizado es lo mismo que deshacerlo: vuelve el resumen que sustituyó, y también se comprueba. Si está
desactualizado, se quita de la misma forma.

Cuando [haces una rama](branches-and-story-tree.md#branching-from-an-earlier-part) desde una parte con resumen, la nueva rama
recibe una copia del resumen. Un metarresumen copiado conserva lo que sustituyó, así que puede deshacerse en la nueva rama.

<a id="the-summary-prompt"></a>

## El prompt de resumen

La petición para un resumen es el **Prompt de resumen predeterminado** del libro (en la pestaña [Prompts](prompts.md), o el
predeterminado de *Configuración → Plantillas*). Recibe dos variables:

| Variable | |
|---|---|
| `{%{{{text}}}%}` | El texto de las partes a resumir, del más antiguo al más reciente. |
| `{%{{{backgroundLore}}}%}` | Las entradas del lorebook que estaban activas cuando se escribió la parte resumida. |

El prompt integrado pide un registro compacto de eventos, agrupado por lugar: bajo un encabezado `[LOCATION / SCENE: Name]`,
un evento por línea como *quién hace qué -> qué se sigue de ello* (la intención solo cuando el texto la muestra), sin títulos,
números ni ids para los eventos y sin la atmósfera. Está pensado para el modelo, no para un lector, y ocupa aproximadamente una
cuarta parte del tamaño del texto que sustituye. El prompt de metarresumen espera el mismo formato y une las líneas vecinas en
cadenas causales. Cámbialo si prefieres otro formato o longitud; un resumen hecho antes del cambio conserva su forma antigua
hasta que lo vuelvas a hacer.

<a id="tips"></a>

## Consejos

- Resume en pausas naturales: al final de un capítulo o de una escena.
- Cuando los resúmenes empiecen a ocupar demasiado contexto (la ayuda del botón **Resúmenes** indica cuánto), fusiona los más
  antiguos en un metarresumen. Repítelo más adelante con el metarresumen y los resúmenes posteriores.
- Los resúmenes permanecen en el prompt para siempre. Con muchos, el prompt puede llenar el contexto por sí solo, y la generación
  se detiene con *Contextual limit not sufficient.* En ese caso acórtalos (un prompt de resumen más conciso), fusiónalos en
  [metarresúmenes](#meta-summaries) o aumenta el límite de contexto.
- Guarda los datos que nunca deben olvidarse (nombres, relaciones, reglas del mundo) en el [lorebook](../lorebooks/index.md)
  en lugar de confiar en los resúmenes.
- Revisa lo que recibió el modelo con **Mostrar el prompt** en la siguiente parte.
