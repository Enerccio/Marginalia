---
label: Estadísticas de escritura
order: 75
---

# Estadísticas de escritura

Marginalia lleva la cuenta de lo que pasa con tus libros, día a día: cuántas palabras escribiste tú y cuántas escribieron los modelos, cuánto tiempo estuviste escribiendo, cuántos tokens se enviaron a los modelos y cuánto creció el libro. La ventana **Estadísticas** lo muestra para un libro o para todos, en forma de números, gráficos y un **calendario** de los días en que trabajaste, con tu **racha** y un **objetivo diario**.

Los números son solo para ti: cada cuenta ve sus propios libros. El recuento empieza con la versión 1.6.0; lo que pasó antes se [reconstruye a partir del historial de las partes](#days-before-the-statistics) lo mejor posible.

<a id="opening-the-window"></a>

## Abrir la ventana

- **Un libro:** el menú de ajustes (los engranajes bajo la historia) tiene **Estadísticas**. La ventana cuenta ese libro.
- **Todos los libros:** la lista de libros tiene **Estadísticas** en su menú **Gestionar**. La ventana suma todos tus libros y tiene dos pestañas más, **Libros** y **Por libro**.

Ambas ventanas funcionan igual. Arriba eliges el **Período** (*Hoy*, *Ayer*, *Últimos 7 días*, *Últimos 30 días*, *Este mes*, *Todo el tiempo*, o *Personalizada* con una fecha **Desde** y una **Hasta**) y **Contado para**: **Todo**, **Yo** (tú y los modelos que pediste) o **Agentes** (lo que hicieron los [agentes](../agents.md), que se mantiene aparte). Los días son los del reloj del servidor.

![Las estadísticas de un libro: los días](../../images/statistics-book.png)

Debajo de la elección están los totales del período y luego las pestañas.

<a id="what-is-counted"></a>

## Qué se cuenta

| | |
|---|---|
| **Palabras escritas** | Palabras que escribiste tú mismo: el texto que tecleaste en una parte, en el editor de la historia o en cualquier otro lugar donde edites una parte. |
| **Palabras generadas** | Palabras que los modelos escribieron para el libro. |
| **Palabras eliminadas** | Palabras que desaparecieron: texto que cortaste de una parte, o una parte que borraste. |
| **Partes creadas**, **Partes eliminadas**, **Ediciones** | Cuántas partes se crearon y se borraron, y cuántas veces guardaste una parte modificada. |
| **Tokens enviados**, **Tokens generados**, **Solicitudes** | Lo que se envió a los modelos para este libro (todo el prompt cada vez) y lo que volvió, y cuántas solicitudes hicieron falta. |
| **Abierta** | Cuánto tiempo estuvo el libro abierto en tu pantalla. |
| **Edición** | Cuánto tiempo estuviste tecleando en una parte. Una pausa larga detiene el reloj. |
| **Generando** | Cuánto tiempo trabajó un modelo para el libro. |
| **Agentes en ejecución** | Cuánto tiempo trabajó un agente en el libro. |
| **Growth** | Cuánto creció (o se redujo) el libro durante el período, en palabras. |

Los tiempos son el reloj real. Cuando hay dos libros abiertos a la vez, o tres agentes funcionando juntos, el total cuenta ese tramo de tiempo una sola vez, así que las líneas de los libros pueden sumar más que el total (la ventana lo indica). Las palabras y los tokens sí suman.

Importar un libro o un texto (un archivo, un chat de SillyTavern), copiar un libro y restaurar una copia de seguridad no cuentan nada: el texto no se escribió aquí. Lo que hacen los agentes se cuenta de su lado (**Agentes**), frente al libro original, aunque trabajen sobre una copia; incluye el trabajo que se descartó o que nunca se fusionó con el libro.

<a id="the-tabs"></a>

## Las pestañas

| Pestaña | |
|---|---|
| **Días** | Una fila por cada día con algo: todos los números de arriba, del más reciente al más antiguo. Haz clic en un encabezado para ordenar. |
| **Palabras** | Un gráfico de las palabras escritas, generadas y eliminadas por día. Un período largo se agrupa por semana o por mes. |
| **Tiempo** | Un gráfico de las horas abierto, editando, generando y con un agente en ejecución. |
| **Tokens** | Un gráfico de los tokens enviados y generados. |
| **Tamaño** | El tamaño del libro al final de cada día y cuánto creció cada día, en **Palabras** o en **Tokens**. |
| **Libros** | (todos los libros) Una fila por cada libro, también los que están en la [papelera](../trash.md) o ya no existen, con su carpeta, sus números, su tiempo, su crecimiento y su parte. **Mostrar los libros eliminados** oculta los que están en la papelera y los que ya no existen. Un clic en un libro lo abre. |
| **Por libro** | (todos los libros) Los libros como barras, según la cifra que elijas en **Gráfico por**. Se muestran los diez más grandes y el resto es una sola barra, *Otros*. |
| **Calendario** | Los días en que trabajaste, tus rachas y tu objetivo, ver abajo. |

![El tiempo por día](../../images/statistics-time.png)

El tamaño de un libro se mide un momento después de cambiar, a partir de las palabras y los tokens que han guardado las partes. Los tokens de una parte antigua son los que se contaron cuando se escribió, así que cambiar el modelo del libro no altera ningún número.

<a id="the-calendar"></a>

## El calendario

La pestaña **Calendario** muestra un pequeño cuadrado por cada día del último año (53 semanas, 16 en un teléfono), una semana por columna y el día más reciente en la esquina inferior derecha, marcado con un marco. Cuanto más oscuro el cuadrado, más hiciste ese día. Pon el puntero sobre un día para ver la fecha, las palabras que escribiste, el tiempo que editaste y la última vez que lo trabajaste (un `~` tras un día reconstruido a partir del historial).

![El calendario de todos los libros, con el objetivo del día](../../images/statistics-global-calendar.png)

Un día cuenta como **trabajado** cuando escribiste palabras, editaste una parte, creaste una parte o pasaste tiempo editando. Lo que hicieron los agentes, las palabras que solo se generaron y abrir un libro no cuentan: el calendario trata sobre *tu* escritura. Un cuadrado que solo es un poco más oscuro que uno vacío es un día en que trabajaste, pero escribiste poco.

Elige cómo se sombrean los cuadrados con **Palabras escritas** o **Edición** (minutos de edición). El calendario siempre muestra las últimas semanas, sea cual sea el **Período** elegido encima.

Encima del calendario:

| | |
|---|---|
| **Racha actual (días)** | Los días seguidos en que trabajaste, contados hacia atrás desde hoy. Un hoy en el que aún no has trabajado no la rompe: la racha entonces se cuenta desde ayer. |
| **Racha más larga (días)** | La serie de días seguidos más larga, de todos los tiempos. |
| **Días de trabajo** | Cuántos de los días mostrados en los cuadrados trabajaste. |
| **Racha de objetivos (días)**, **Racha de objetivos más larga (días)** | Como las rachas de arriba, para los días que alcanzaron tu objetivo diario (cuando estableces uno, en la ventana de todos los libros). |

<a id="the-daily-goal"></a>

### El objetivo diario

Establece un objetivo en la pestaña **Ajustes**, en los ajustes generales:

| | |
|---|---|
| **Objetivo diario: palabras escritas** | Las palabras que escribes en un día, sumando todos tus libros. Las palabras que generan los modelos no cuentan. |
| **Objetivo diario: minutos de edición** | Los minutos que editas en un día, sumando todos tus libros. |

![Los objetivos en los Ajustes](../../images/settings-daily-goal.png)

Deja un campo vacío para no tener objetivo. Con ambos establecidos, un día alcanza el objetivo cuando llega a **los dos**. La ventana de **todos los libros** muestra entonces cuánto llevas hoy en forma de dos barras (*Hoy: 310 de 500 palabras*), el sombreado del calendario sigue al objetivo (un día que lo alcanzó es el más oscuro), y la **Racha de objetivos** cuenta los días que lo alcanzaron. Sin objetivo, el sombreado sigue a tu día más intenso del calendario. La ventana de un solo libro no tiene objetivo (un objetivo para todos los libros significa poco para uno solo): sus cuadrados se sombrean frente al día más intenso de ese libro.

Cuando un día no tiene tiempo registrado (véase abajo) y el calendario se sombrea por **Edición**, solo se marca como día trabajado.

![Las estadísticas de todos los libros en un teléfono](../../images/phone-statistics.png)

<a id="days-before-the-statistics"></a>

## Los días antes de las estadísticas

Las estadísticas no se guardaban antes de la versión 1.6.0. Cuando se actualizó Marginalia, reconstruyó los días anteriores a partir del historial de las partes: la primera versión de una parte es una parte creada con las palabras que tenía, una posterior es una edición, y una parte que ya no existe es un borrado. Estos días son una estimación. Se marcan con un `~` en la lista de días y la ventana indica cuántos son; los tokens de un día reconstruido son solo un mínimo. El **tiempo** no se puede reconstruir, porque nadie lo registró: la ventana indica desde qué día está registrado (*El tiempo se registra desde ...*), los días anteriores muestran un guion y se excluyen del gráfico de tiempo. El **calendario** cuenta un día reconstruido como trabajado, como cualquier otro.
<a id="privacy-clearing-not-recording-keeping-less"></a>

## Privacidad: borrar, no registrar, conservar menos

Las estadísticas indican cuándo trabajaste en un libro y durante cuánto tiempo, y conservan el nombre y la carpeta de cada libro, también de los libros que borraste. Son tuyas, y decides qué queda de ellas.

![Record writing statistics and Keep statistics for, in the Settings](../../images/settings-statistics-privacy.png)

- **Borrar las estadísticas de este libro** (botón en la ventana de un libro) y **Borrar todas las estadísticas** (en la ventana de todos los libros) eliminan para siempre los recuentos, los tiempos y los tamaños, previa confirmación. Los libros no se modifican. El recuento continúa después, desde cero.
- En la lista de libros, un libro que está en la papelera o ya no existe tiene **Olvidar**, que elimina solo sus estadísticas.
- **Registrar las estadísticas de escritura** en los [ajustes](../account.md) desactiva el recuento de tus libros. Lo que se registró se conserva hasta que lo borres; la ventana indica que no se cuenta nada nuevo. Un administrador también puede desactivar el registro para todos, con la misma nota.
- **Keep statistics for (months)** en los ajustes elimina los días más antiguos que ese plazo, una vez al día y al guardar. Vacío o 0 conserva todo. El administrador puede fijar un límite para todos, y se usa el más corto de los dos. De los tamaños de un libro se conserva el último anterior al límite, para que aún se pueda calcular el crecimiento del primer día que se mantiene.

El [administrador](../administration/statistics.md) puede borrar las estadísticas de un usuario o de todos los usuarios.

<a id="commands"></a>

## Comandos

Dos [comandos](../commands/index.md) leen los mismos números: `/get-book-statistics [range] [side]` para el libro abierto, día a día, y `/get-statistics [range] [by] [side]` para todos tus libros, por día o por libro. El rango es `today`, `yesterday`, `last-7-days`, `last-30-days`, `this-month`, `all-time`, un día (`2026-03-14`) o dos días (`2026-03-01..2026-03-14`). Un [agente](../agents.md) también puede usarlos.
