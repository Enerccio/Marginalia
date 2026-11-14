---
label: Expresiones regulares
order: 935
icon: search
---

# Expresiones regulares

Una expresión regular (*regex*) es un patrón breve que describe un texto en lugar de escribirlo literalmente: "la palabra Harry, pero no Harrys", "dos palabras cualesquiera entre comillas", "tres o más espacios seguidos". Marginalia las usa en dos lugares:

- **Buscar y reemplazar** de la [historia](books/story-editor.md#search-and-replace) y de un [lorebook](lorebooks/entries.md#search-and-replace), y los [comandos](commands/index.md) correspondientes, cuando **Buscar con expresión regular** está activado. El patrón encuentra el texto y el reemplazo puede reutilizar partes de él.
- El filtro **Expresión regular** de una [entrada de lorebook](lorebooks/entries.md#activation), que decide cuándo la entrada entra en el prompt.

La sintaxis es la de Java. Esta página muestra lo que más vas a usar; la referencia completa está enlazada al final.

!!!
No necesitas expresiones regulares para palabras sencillas. Con **Buscar con expresión regular** desactivado, el texto se busca exactamente como lo escribes, así que un `.` o un `(` en él es solo un punto o un corchete. Activa la expresión regular solo cuando un texto fijo no sea suficiente.
!!!

<a id="the-building-blocks"></a>

## Los bloques básicos

| Escribe | Coincide con | Ejemplo |
|---|---|---|
| una letra o un dígito | ese carácter | `cat` encuentra *cat* en *concatenate* |
| `.` | cualquier carácter (salvo un salto de línea) | `c.t` encuentra *cat*, *cot*, *c7t* |
| `\d` `\w` `\s` | un dígito, una letra/dígito/`_`, un espacio o un salto de línea | `\d\d` encuentra *42* |
| `[abc]` `[a-z]` | uno de los caracteres indicados, o un rango | `gr[ae]y` encuentra *grey* y *gray* |
| `[^abc]` | un carácter que *no* esté en la lista | `[^"]` es cualquier cosa menos una comilla |
| `\b` | el borde de una palabra | `\bcat\b` encuentra *cat* pero no *concatenate* |
| `^` `$` | el inicio y el final de una línea | `^Chapter` encuentra *Chapter* al inicio de una línea |
| `x?` `x*` `x+` | `x` cero o una vez, cualquier número de veces, al menos una vez | `colou?r` encuentra *color* y *colour* |
| `x{2,4}` | `x` de dos a cuatro veces | `\.{3}` son tres puntos seguidos |
| `\` | el carácter siguiente se toma literalmente | `\.` es un punto, `\?` un signo de interrogación, `\(` un corchete |

Los paréntesis forman un **grupo**, y una barra vertical dentro de un grupo significa "o": `(Mr|Mrs) Dursley` encuentra *Mr Dursley* y *Mrs Dursley*, y `\b(Voldemort|You-Know-Who)\b` encuentra cualquiera de los dos nombres. Un `?`, `*` o `+` después de un grupo se aplica a todo él: `(very )+good` encuentra *very good* y *very very good*.

Los caracteres `. ? * + ^ $ | \ ( ) [ ] { }` tienen un significado propio. Para buscar uno de ellos como tal, escribe una `\` delante: para encontrar *Really?!* escribe `Really\?!`.

Por defecto, una expresión regular no ignora nada: *harry* no encuentra *Harry*. Pon `(?i)` al principio para ignorar las mayúsculas: `(?i)harry` encuentra *Harry*, *HARRY* y *harry*. (Las ventanas de Buscar y reemplazar bloquean **Distinguir mayúsculas y minúsculas** mientras la expresión regular está activa, así que es aquí donde la desactivas; el filtro **Expresión regular** del lorebook siempre ignora las mayúsculas).

Una coincidencia de longitud cero (un patrón como `a*` que no puede coincidir con nada) se ignora en Buscar y reemplazar.

<a id="typical-uses-in-a-story"></a>

## Usos típicos en una historia

Cada fila es algo que quizá quieras hacer con un libro. **Buscar** es lo que escribes en *Buscar*, y **Reemplazar** lo que escribes en *Reemplazar por*.

| Lo que quieres | Buscar | Reemplazar |
|---|---|---|
| Renombrar a un personaje, pero no parte de una palabra más larga (*Harry* pero no *Harrys* ni *Harrison*) | `\bHarry\b` | `Henry` |
| Renombrar, sin importar las mayúsculas | `(?i)\bharry\b` | `Henry` |
| Renombrar un nombre y su posesivo de una vez | `\bHarry('s)?\b` | `Henry$1` |
| Convertir las comillas rectas en curvas | `"([^"]*)"` | `“$1”` |
| Tres puntos en un solo carácter de puntos suspensivos | `\.{3}` | `…` |
| Dos o más espacios en uno | ` {2,}` | ` ` (un espacio) |
| Eliminar los espacios al final de las líneas | ` +$` | (vacío) |
| Un guion largo formado por dos guiones | ` ?-- ?` | ` — ` |
| Cambiar *Apellido, Nombre* por *Nombre Apellido* | `(\w+), (\w+)` | `$2 $1` |
| Encontrar dónde se usa una palabra, sin cambiar nada | el patrón | (déjalo vacío y pulsa solo **Buscar**) |

Para que varias grafías cuenten como un mismo nombre, busca `\b(Voldemort|You-Know-Who|He-Who-Must-Not-Be-Named)\b` y reemplázalo por `Tom Riddle`.

<a id="taking-the-match-into-the-replacement"></a>

### Llevar la coincidencia al reemplazo

Todo lo que una expresión regular pone entre paréntesis es un **grupo**, numerado de izquierda a derecha según su paréntesis de apertura: el reemplazo `$1` es el texto del primer grupo, `$2` el del segundo, y `$0` toda la coincidencia. En `(\w+), (\w+)` el primer grupo es la palabra antes de la coma, el segundo la palabra después, así que `$2 $1` las intercambia: *Potter, Harry* se convierte en *Harry Potter*.

Puedes dar nombre a un grupo, `(?<last>\w+), (?<first>\w+)`, y usar ese nombre en el reemplazo: `${first} ${last}`.

Un `$` o una `\` que deba quedar tal cual en el reemplazo hay que escaparlo: para poner *$5* en el texto, escribe `\$5`. Sin expresión regular, el reemplazo siempre se toma exactamente como lo escribes.

Si el patrón está mal (un paréntesis que nunca se cierra) o el reemplazo nombra un grupo que el patrón no tiene, aparece una notificación que lo indica y no ocurre nada; una búsqueda nunca cambia nada.

<a id="match-only-in-a-context"></a>

### Coincidir solo en un contexto

Un *lookahead* `(?=...)` y un *lookbehind* `(?<=...)` miran el texto alrededor de la coincidencia sin incluirlo, así que solo se reemplaza la parte que queda fuera de ellos:

| Buscar | Encuentra |
|---|---|
| `Harry(?= Potter)` | *Harry* solo cuando *Potter* va detrás (y deja *Potter* intacto) |
| `(?<=Professor )McGonagall` | *McGonagall* solo después de *Professor* |
| `Harry(?! Potter)` | *Harry* no seguido de *Potter* |

<a id="lazy-and-greedy"></a>

### Perezoso y voraz

`*` y `+` toman todo lo que pueden: en `He said "yes" and "no"` el patrón `".*"` coincide con todo el `"yes" and "no"`. Añade un `?` (`".*?"`), o indica lo que no se permite (`"[^"]*"`), para que se detenga en la primera comilla de cierre.

La búsqueda trata cada línea por separado: `.` no cruza un salto de línea, y `^` y `$` coinciden al inicio y al final de cada línea de una parte, no solo del texto completo. Para coincidir con un salto de línea escribe `\n`.

<a id="in-a-lorebook-entry-filter"></a>

## En el filtro de una entrada de lorebook

El filtro de una entrada de lorebook configurada en **Expresión regular** se compara con la petición de la parte, en cualquier punto de ella. Ignora las mayúsculas, y `.` sí coincide con los saltos de línea en este caso, así que `varro.*harbor` encuentra Varro y el puerto incluso en líneas distintas. Filtros típicos:

- `\bvarro\b` - Varro como palabra completa.
- `\b(varro|captain)\b` - Varro o el capitán.
- `varro.*(harbor|docks)` - Varro y, más adelante, el puerto o los muelles.
- `dragons?` - *dragon* o *dragons*.
- `(?<!no )magic` - *magic*, salvo cuando va justo después de *no*.

Una expresión regular no válida nunca activa la entrada, y el motivo se escribe en el registro. Consulta [entradas y activación](lorebooks/entries.md#activation) para el resto de cómo se elige una entrada.

<a id="in-the-commands"></a>

## En los comandos

[`/search-replace-parts`](commands/index.md#commands-that-come-with-marginalia) y `/search-replace-lore-entries` reciben la expresión regular como tercer argumento (`true`), después de la búsqueda y del reemplazo. La línea de comandos se divide por espacios, así que un patrón o un reemplazo con espacios va entre comillas dobles. Una barra invertida delante de una letra se pasa tal cual (`\b`, `\d`), pero `\\` se convierte en una sola barra invertida y `\"` en una comilla, así que una barra invertida literal en el patrón se escribe `\\\\`.

```
/search-replace-parts "\bHarry\b" Henry true
/search-replace-parts "(\w+), (\w+)" "$2 $1" true
/search-replace-parts "(?i)\bharry\b" Henry true
```

Los comandos no muestran una lista de coincidencias: te preguntan si quieres reemplazar el número de coincidencias encontradas. Si no estás seguro de un patrón, pruébalo primero en la ventana **Buscar y reemplazar**, que muestra cada coincidencia tal como está y tal como quedará.

<a id="tips"></a>

## Consejos

- Prueba un patrón primero con **Buscar** y abre algunas de las filas: la ventana muestra cada coincidencia con su contexto, tal como está y tal como quedará. Reemplaza solo cuando el resultado sea el que querías.
- Empieza simple y añade al patrón paso a paso; un patrón que encuentra demasiado suele necesitar `\b` alrededor de una palabra, o `?` para detener un `*`.
- Una [copia de seguridad](books/backups.md) antes de un reemplazo grande cuesta poco (`/take-backup`), y un reemplazo no se puede detener una vez ha empezado.
- Resúmenes modificados: cuando el reemplazo afecta al texto del que se generó un [resumen](books/summaries.md), la ventana avisa de que el resumen quedaría desactualizado; consulta [Buscar y reemplazar](books/story-editor.md#search-and-replace).

<a id="further-reading"></a>

## Más información

Esta página cubre lo básico. La sintaxis completa de Java, con todas las construcciones, indicadores y clases de caracteres, está en la [documentación de `java.util.regex.Pattern`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html), y la sintaxis del reemplazo (`$1`, `${name}`, `\$`) en [`Matcher.appendReplacement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Matcher.html#appendReplacement(java.lang.StringBuilder,java.lang.String)).
