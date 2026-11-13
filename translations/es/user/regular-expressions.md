---
label: Expresiones regulares
order: 935
icon: search
---

# Expresiones regulares

Una expresión regular (*regex*) es un patrón breve que describe un texto en lugar de escribirlo literalmente: «la palabra Harry, pero no Harrys», «dos palabras cualesquiera entre comillas», «tres o más espacios seguidos». Marginalia las usa en dos lugares:

- **Buscar y reemplazar** de la [historia](books/story-editor.md#search-and-replace) y de un
  [libro de lore](lorebooks/entries.md#search-and-replace), y los [comandos](commands/index.md) correspondientes, cuando **Buscar con expresión regular** está activado. El patrón encuentra el texto, y el reemplazo puede reutilizar partes de él.
- El filtro **Expresión regular** de una [entrada del libro de lore](lorebooks/entries.md#activation), que decide cuándo la entrada entra en el prompt.

El dialecto es el de Java. Esta página muestra lo que más vas a usar; la referencia completa está enlazada al final.

!!!
No necesitas expresiones regulares para palabras sueltas. Con **Buscar con expresión regular** desactivado, el texto se busca exactamente como lo escribes, de modo que un `.` o un `(` en él son solo un punto o un paréntesis. Activa la expresión regular únicamente cuando un texto fijo no basta.
!!!

<a id="the-building-blocks"></a>

## Los componentes básicos

| Escribe | Coincide con | Ejemplo |
|---|---|---|
| una letra o un dígito | ese carácter | `cat` encuentra *cat* en *concatenate* |
| `.` | cualquier carácter (salvo un salto de línea) | `c.t` encuentra *cat*, *cot*, *c7t* |
| `\d` `\w` `\s` | un dígito, una letra/dígito/`_`, un espacio o un salto de línea | `\d\d` encuentra *42* |
| `[abc]` `[a-z]` | uno de los caracteres indicados, o un rango | `gr[ae]y` encuentra *grey* y *gray* |
| `[^abc]` | un carácter que *no* está indicado | `[^"]` es cualquier cosa que no sea una comilla |
| `\b` | el límite de una palabra | `\bcat\b` encuentra *cat* pero no *concatenate* |
| `^` `$` | el inicio y el final de una línea | `^Capítulo` encuentra *Capítulo* al inicio de una línea |
| `x?` `x*` `x+` | `x` cero o una vez, cualquier número de veces, al menos una vez | `colou?r` encuentra *color* y *colour* |
| `x{2,4}` | `x` de dos a cuatro veces | `\.{3}` son tres puntos seguidos |
| `\` | el carácter siguiente se toma literalmente | `\.` es un punto, `\?` un signo de interrogación, `\(` un paréntesis |

Los paréntesis forman un **grupo**, y una barra vertical dentro de un grupo significa «o»: `(Mr|Mrs) Dursley` encuentra *Mr Dursley* y *Mrs Dursley*, y `\b(Voldemort|You-Know-Who)\b` encuentra cualquiera de los dos nombres. Un `?`, `*` o `+` después de un grupo se aplica a todo él: `(very )+good` encuentra *very good* y *very very good*.

Los caracteres `. ? * + ^ $ | \ ( ) [ ] { }` tienen un significado propio. Para buscar uno de ellos tal cual, escribe un `\` delante: para encontrar *¡Really?!* escribe `Really\?!`.

Por defecto, una expresión regular no ignora nada: *harry* no encuentra *Harry*. Pon `(?i)` al principio para ignorar las mayúsculas: `(?i)harry` encuentra *Harry*, *HARRY* y *harry*. (Las ventanas de Buscar y reemplazar bloquean **Distinguir mayúsculas y minúsculas** mientras la expresión regular está activa, así que así se desactiva allí; el filtro **Expresión regular** del libro de lore ignora siempre las mayúsculas.)

Una coincidencia de longitud cero (un patrón como `a*` que puede no coincidir con nada) es ignorada por Buscar y reemplazar.

<a id="typical-uses-in-a-story"></a>

## Usos típicos en una historia

Cada fila es algo que puedes querer hacer con un libro. **Buscar** es lo que escribes en *Buscar*, **Reemplazar** lo que escribes en *Reemplazar por*.

| Lo que quieres | Buscar | Reemplazar |
|---|---|---|
| Renombrar a un personaje, pero no parte de una palabra más larga (*Harry* pero no *Harrys* ni *Harrison*) | `\bHarry\b` | `Henry` |
| Renombrar, sin importar las mayúsculas | `(?i)\bharry\b` | `Henry` |
| Renombrar un nombre y su posesivo de una vez | `\bHarry('s)?\b` | `Henry$1` |
| Convertir las comillas rectas en curvas | `"([^"]*)"` | `“$1”` |
| Tres puntos en un solo carácter de puntos suspensivos | `\.{3}` | `…` |
| Dos o más espacios en uno | ` {2,}` | ` ` (un espacio) |
| Quitar los espacios al final de las líneas | ` +$` | (vacío) |
| Un guion largo hecho con dos guiones | ` ?-- ?` | ` — ` |
| Cambiar *Apellido, Nombre* a *Nombre Apellido* | `(\w+), (\w+)` | `$2 $1` |
| Localizar dónde se usa una palabra, sin cambiar nada | el patrón | (deja vacío y pulsa solo **Buscar**) |

Para que varias grafías sean un mismo nombre, busca `\b(Voldemort|You-Know-Who|He-Who-Must-Not-Be-Named)\b` y reemplázalo por `Tom Riddle`.

<a id="taking-the-match-into-the-replacement"></a>

### Llevar la coincidencia al reemplazo

Todo lo que una expresión regular pone entre paréntesis es un **grupo**, numerado de izquierda a derecha según su paréntesis de apertura: el reemplazo `$1` es el texto del primer grupo, `$2` el del segundo, `$0` toda la coincidencia. En `(\w+), (\w+)` el primer grupo es la palabra antes de la coma y el segundo la palabra después, así que `$2 $1` las intercambia: *Potter, Harry* pasa a ser *Harry Potter*.

Puedes dar nombre a un grupo, `(?<apellido>\w+), (?<nombre>\w+)`, y usar el nombre en el reemplazo: `${nombre} ${apellido}`.

Un `$` o un `\` que deba quedar como tal en el reemplazo hay que escaparlo: para poner *$5* en el texto, escribe `\$5`. Sin expresión regular, el reemplazo siempre se toma exactamente como lo escribes.

Si el patrón está mal (un paréntesis que nunca se cierra) o el reemplazo nombra un grupo que el patrón no tiene, una notificación lo indica y no pasa nada; una búsqueda nunca cambia nada.

<a id="match-only-in-a-context"></a>

### Coincidir solo en un contexto

Un *lookahead* `(?=...)` y un *lookbehind* `(?<=...)` miran el texto alrededor de la coincidencia sin tomarlo, así que solo se reemplaza la parte que queda fuera de ellos:

| Buscar | Encuentra |
|---|---|
| `Harry(?= Potter)` | *Harry* solo cuando *Potter* va detrás (y deja *Potter* intacto) |
| `(?<=Profesor )McGonagall` | *McGonagall* solo después de *Profesor* |
| `Harry(?! Potter)` | *Harry* no seguido de *Potter* |

<a id="lazy-and-greedy"></a>

### Perezoso y voraz

`*` y `+` toman todo lo que pueden: en `Dijo "sí" y "no"` el patrón `".*"` coincide con todo `"sí" y "no"`. Añade un `?` (`".*?"`), o indica lo que no está permitido (`"[^"]*"`), para parar en la primera comilla de cierre.

La búsqueda trata cada línea por separado: `.` no cruza un salto de línea, y `^` y `$` coinciden con el inicio y el final de cada línea de un fragmento, no solo del texto completo. Para coincidir con un salto de línea escribe `\n`.

<a id="in-a-lorebook-entry-filter"></a>

## En el filtro de una entrada del libro de lore

El filtro de una entrada del libro de lore configurado en **Expresión regular** se compara con la petición del fragmento, en cualquier parte de ella. Ignora las mayúsculas, y allí `.` sí coincide con los saltos de línea, así que `varro.*puerto` encuentra a Varro y el puerto aunque estén en líneas distintas. Filtros típicos:

- `\bvarro\b` - Varro como palabra completa.
- `\b(varro|capitán)\b` - Varro o el capitán.
- `varro.*(puerto|muelles)` - Varro y luego, más adelante, el puerto o los muelles.
- `dragones?` - *dragón* o *dragones*.
- `(?<!no )magia` - *magia*, salvo que vaya justo después de *no*.

Una expresión regular no válida nunca activa la entrada, y el motivo se escribe en el registro. Consulta [entradas y activación](lorebooks/entries.md#activation) para el resto de cómo se elige una entrada.

<a id="in-the-commands"></a>

## En los comandos

[`/search-replace-parts`](commands/index.md#commands-that-come-with-marginalia) y `/search-replace-lore-entries` reciben la expresión regular como tercer argumento (`true`), después de la búsqueda y el reemplazo. La línea de comandos se divide por espacios, así que un patrón o un reemplazo con espacios va entre comillas dobles. Una barra invertida delante de una letra se pasa tal cual (`\b`, `\d`), pero `\\` se convierte en una sola barra invertida y `\"` en una comilla, así que una barra invertida literal en el patrón es `\\\\`.

```
/search-replace-parts "\bHarry\b" Henry true
/search-replace-parts "(\w+), (\w+)" "$2 $1" true
/search-replace-parts "(?i)\bharry\b" Henry true
```

Los comandos no muestran una lista de coincidencias: te preguntan si quieres reemplazar el número de coincidencias encontradas. Si no estás seguro de un patrón, pruébalo primero en la ventana **Buscar y reemplazar**, que muestra cada coincidencia tal como está y tal como quedará.

<a id="tips"></a>

## Consejos

- Prueba un patrón con **Buscar** primero y abre algunas de las filas: la ventana muestra cada coincidencia con su contexto, tal como está y tal como quedará. Reemplaza solo cuando el resultado sea el que querías.
- Empieza sencillo y añade al patrón poco a poco; un patrón que encuentra demasiado suele necesitar `\b` alrededor de una palabra, o `?` para frenar un `*`.
- Una [copia de seguridad](books/backups.md) antes de un reemplazo grande cuesta poco (`/take-backup`), y un reemplazo no se puede detener una vez empezado.
- Resúmenes modificados: cuando el reemplazo toca el texto del que se hizo un [resumen](books/summaries.md), la ventana avisa de que el resumen quedaría desactualizado; consulta [Buscar y reemplazar](books/story-editor.md#search-and-replace).

<a id="further-reading"></a>

## Lectura adicional

Esta página cubre lo básico. La sintaxis completa de Java, con cada construcción, indicador y clase de caracteres, está en la
[documentación de `java.util.regex.Pattern`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Pattern.html),
y la sintaxis de los reemplazos (`$1`, `${name}`, `\$`) en
[`Matcher.appendReplacement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/regex/Matcher.html#appendReplacement(java.lang.StringBuilder,java.lang.String)).
