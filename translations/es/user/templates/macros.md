---
label: Referencia de macros
order: 90
---

# Referencia de macros

Marginalia entiende las macros de [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/), así que
los prompts y las entradas de world info escritos para SillyTavern funcionan sin cambios. Las macros funcionan en todas las
[plantillas](templates.md): plantilla maestra, prompt de usuario, prompt de resumen y entradas de lorebook.

{%{

<a id="syntax"></a>

## Sintaxis

| Forma | Ejemplo |
|---|---|
| sin argumentos | `{{user}}` |
| argumentos separados por `::` | `{{setvar::mood::nervous}}` |
| primer argumento tras un espacio | `{{getvar mood}}`, `{{setvar greeting::Hello}}` |
| `:` simple (formato antiguo) | `{{getvar:mood}}`, `{{random:north,south}}` |
| macros anidadas | `{{getvar::{{user}}_mood}}` |
| con ámbito (el contenido como último argumento) | `{{setvar backstory}}Long text...{{/setvar}}` |

- Los nombres de las macros no distinguen mayúsculas: `{{User}}` y `{{USER}}` funcionan.
- Se ignoran los espacios alrededor de nombres y argumentos: `{{ getvar :: mood }}`.
- Los `::` sobrantes se añaden al último argumento: `{{setvar::x::a::b}}` asigna a `x` el valor `a::b`.
- El contenido con ámbito se evalúa primero y después se le quita la sangría común y se recorta. Si se pone `#` después de las llaves,
  `{{#setvar name}}...{{/setvar}}`, los espacios en blanco se conservan exactamente.

<a id="names-and-participants"></a>

## Nombres y participantes

Marginalia no tiene personas ni fichas de personaje; los nombres vienen de las instrucciones de turno de la parte que se está escribiendo.

| Macro | Resultado |
|---|---|
| `{{user}}` | El personaje POV. |
| `{{char}}` | El personaje POV. Alias `{{charIfNotGroup}}`. |
| `{{group}}` | Los personajes presentes, o el personaje POV si no se indica ninguno. Alias `{{groupNotMuted}}`. |
| `{{notChar}}` | Los personajes presentes excepto el personaje POV (separados por coma o por línea, unidos con `, `). |
| `{{description}}` | La descripción del libro. |
| `{{scenario}}` | El escenario de la escena. |
| `{{input}}` | Las instrucciones de la parte. |

Las marcas de SillyTavern `<USER>`, `<BOT>`, `<CHAR>`, `<GROUP>` y `<CHARIFNOTGROUP>` (en cualquier mayúscula o minúscula) también funcionan.

Con el personaje POV *Alice* y los personajes presentes *Alice, Bob, Carol*:
`{{user}}` → *Alice*, `{{group}}` → *Alice, Bob, Carol*, `{{notChar}}` → *Bob, Carol*.

<a id="story-history"></a>

## Historial del relato

| Macro | Resultado |
|---|---|
| `{{lastMessage}}` | El texto de la última parte de la rama activa. Alias `{{lastCharMessage}}`. |
| `{{lastMessageId}}` | La posición de la última parte, contando desde 0. |
| `{{lastUserMessage}}` | Las instrucciones de la última parte. |
| `{{summary}}` | El [resumen](../books/summaries.md) más reciente de la rama activa. |
| `{{closest-message}}`, `{{closest-message::n}}` | La parte más cercana a la que se está escribiendo: la última, o la *n*-ésima contando hacia atrás desde ella (`::2` es la anterior a la última). Vacío si no hay tal parte. Alias `{{closestMessage}}`. |
| `{{closest-summary}}`, `{{closest-summary::n}}` | Lo mismo para los resúmenes de la rama: el más reciente, o el *n*-ésimo contando hacia atrás. Alias `{{closestSummary}}`. |
| `{{current-message}}` | El texto de la parte que *Regenerar* o un swipe reemplaza; vacío para una parte nueva. Alias `{{currentMessage}}`. |

Para la primera parte de un libro están vacías. En *Regenerar*, la «última parte» es la anterior a la parte regenerada.

El número de `closest-message` y `closest-summary` puede ser una macro, así que puede venir de una variable:
`{{setvar::back::3}}{{closest-message::{{getvar::back}}}}` es la tercera parte desde el final. Un número que no sea un entero
positivo no da nada. `closest-summary` cuenta los resúmenes de las partes de la rama, incluidos un [metarresumen](../books/summaries.md)
y los resúmenes que sustituye; `{{summary}}` es lo mismo que `{{closest-summary}}`.

<a id="date-and-time"></a>

## Fecha y hora

| Macro | Resultado de ejemplo |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - la hora con un desfase UTC (`UTC-5:30` también funciona) |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [tokens de formato de moment.js](https://momentjs.com/docs/#/displaying/format/); el texto entre `[brackets]` se imprime tal cual: `{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - tiempo desde que se escribió la última parte; `just now` para la primera parte. Alias `{{idle_duration}}`. |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - la diferencia entre dos momentos; funcionan `now`, fechas (`2026-01-08`) y horas ISO. Los valores desconocidos no dan nada. |

Las horas se calculan en la zona horaria del equipo o servidor donde se ejecuta Marginalia, y siempre en inglés. Para la
[versión Docker](../getting-started/docker-server.md), ajusta la zona horaria con la variable de entorno `TZ`, por ejemplo
`TZ=Europe/Prague`.

## Variables

Las variables guardan valores entre partes: contadores, el estado de ánimo de un personaje, cosas que la historia ya ha establecido. Hay dos tipos:

- las variables **locales** pertenecen a la rama de la historia: cada parte recuerda sus valores, y una parte nueva empieza con los valores de la parte anterior;
- las variables **globales** pertenecen al libro y las comparten todas las ramas.

Consulta [Qué se conserva entre partes](templates.md#what-is-kept-between-parts).

| Local | Global | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | El valor; vacío si no está definida. |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | Asigna el valor. No imprime nada. |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | Suma un número, o añade texto si alguno de los dos no es un número. No imprime nada. |
| `{{incvar::name}}` | `{{incglobalvar::name}}` | Suma 1 e imprime el nuevo valor. Un valor que no es un número empieza desde 0. |
| `{{decvar::name}}` | `{{decglobalvar::name}}` | Resta 1 e imprime el nuevo valor. |
| `{{hasvar::name}}` | `{{hasglobalvar::name}}` | `true` o `false`. |
| `{{deletevar::name}}` | `{{deleteglobalvar::name}}` | Elimina la variable. Alias `flushvar` / `flushglobalvar`. |

Los números se imprimen sin ceros finales: `1.50` más `1.5` da `3`.

```handlebars
{{setvar::hp::10}}{{addvar::hp::5}}HP: {{getvar::hp}}      → HP: 15
{{setvar::items::a rope}}{{addvar::items::, a lamp}}{{getvar::items}}   → a rope, a lamp
```

<a id="shorthands"></a>

### Atajos

`{{.name}}` es una variable local, `{{$name}}` una global. Por sí solo, un atajo imprime el valor. Con un operador:

| Atajo | Efecto | Imprime |
|---|---|---|
| `{{.hp = 20}}` | asigna el valor | nada |
| `{{.hp += 5}}`, `{{.hp -= 5}}` | suma / resta (`+=` añade texto a un texto) | nada |
| `{{.turn++}}`, `{{.turn--}}` | suma / resta 1 | el nuevo valor |
| `{{.hp == 17}}`, `{{.hp != 17}}` | compara | `true` / `false` |
| `{{.hp > 10}}`, `>=`, `<`, `<=` | compara números (`false` para texto) | `true` / `false` |

Valores alternativos y por defecto:

- `{{.name || Guest}}` imprime *Guest* cuando la variable está vacía o es falsa (`0`, `false`, `no`, `off`);
- `{{.name ?? Guest}}` imprime *Guest* solo cuando la variable no está definida;
- `{{.title ||= Wanderer}}` asigna *Wanderer* a la variable si está vacía o es falsa, y luego la imprime;
- `{{.title ??= Wanderer}}` la asigna solo si no está definida, y luego la imprime.

Los valores pueden contener macros: `{{.log += -> {{user}}}}`.

Por ejemplo, un contador de partes al principio del prompt de usuario (`+=` no imprime nada, y `{{trim}}` elimina la línea vacía que deja):

```handlebars
{{.part += 1}}{{trim}}
This is part {{.part}} of the story.
```

<a id="conditionals"></a>

## Condicionales

```handlebars
{{if presentCharacters}}
  Present: {{presentCharacters}}
{{else}}
  {{user}} is alone.
{{/if}}
```

La condición puede ser:

| Condición | Es verdadera cuando |
|---|---|
| un nombre de macro - `{{if user}}` | la macro devuelve un valor |
| una variable - `{{if .mood}}`, `{{if $chapter}}` | la variable tiene un valor |
| una variable de plantilla - `{{if sceneSetting}}` | la variable tiene un valor |
| una comparación - `{{if .hp > 20}}` | la comparación es cierta |
| cualquier texto - `{{if yes}}` | siempre, salvo los valores falsos |
| una negación - `{{if !user}}`, `{{if !.mood}}` | lo contrario |

Son falsos un valor vacío y `0`, `false`, `no`, `off` (sin distinguir mayúsculas). Todo lo demás es verdadero.

- La forma corta `{{if user::text}}` imprime *text* cuando la condición es verdadera.
- Solo se evalúa la rama que se toma: un `{{setvar}}` en la otra rama no se ejecuta.
- Los bloques `{{if}}` se tratan como las macros con ámbito: se les quita la sangría común y se recortan, así que pueden sangrarse para facilitar la lectura. `{{#if}}`
  (el [if de Handlebars](templates.md#conditions)) conserva los espacios en blanco.
- Un `{{if}}` sin `{{/if}}` se imprime como texto.
<a id="expressions"></a>

## Expresiones

`{{eval::expresión}}` devuelve `true` o `false`. Úsala cuando necesites una comparación que `{{if}}` no puede hacer: sobre los valores
de la plantilla (no solo variables), unidos con `and`, `or` y `not`:

```handlebars
{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}
{{if {{eval::.hp < 10 or not .armed}} }}Fiora está en peligro.{{/if}}
```

| En una expresión | Significado |
|---|---|
| `"texto"`, `'texto'` | Un texto. **Un texto siempre va entre comillas**: una palabra sin comillas es un nombre. |
| `12`, `-3.5`, `true`, `false` | Un número o un valor de verdad. |
| `povCharacter`, `presentCharacters`, `sceneSetting`... | Un valor de la plantilla, como en `{{povCharacter}}`. Una macro sin argumentos también funciona (`user`, `summary`, `lastMessage`). |
| `.mood`, `$chapter` | Una [variable](#variables) local o global; vacía cuando no está definida. |
| `( )` | Agrupación. |

Comparaciones:

| Operador | Verdadero cuando |
|---|---|
| `==`, `!=` | Iguales, distintos. Los números se comparan por valor (`5 == 5.0`); lo demás, exactamente, así que `"Fiora" == "fiora"` es falso. |
| `~=` | Iguales, ignorando mayúsculas y minúsculas. |
| `<`, `<=`, `>`, `>=` | Los números están en ese orden. Falso cuando uno de los lados no es un número. |
| `contains` | El texto de la izquierda contiene el texto de la derecha, ignorando mayúsculas y minúsculas: `presentCharacters contains "ma"` también es verdadero para *Mara*. |
| `has` | La lista de la izquierda, separada por comas, punto y coma o saltos de línea, tiene el elemento de la derecha, ignorando mayúsculas y minúsculas y los espacios alrededor de los elementos: `presentCharacters has "Mara"` no es verdadero para *Marati*. |

Unión: `and` (`&&`), `or` (`||`), `not` (`!`); `not` tiene la mayor precedencia, luego `and`, y por último `or`. Un valor por sí solo es verdadero salvo que esté vacío,
sea `0`, `false`, `no` u `off`. Se permiten macros dentro de un texto (`"{{user}}"`) y se evalúan primero.

Una expresión no válida imprime `Error`: un error de sintaxis, una palabra sin comillas que no es un nombre (`povCharacter == Fiora`), un nombre desconocido.
Se comprueban todos los nombres, también en la parte de un `and` / `or` que no llegaría a evaluarse, así que una errata no puede pasar desapercibida. En una
[condición de lorebook](../lorebooks/entries.md#a-condition-for-an-entry), un `Error` hace que la entrada no se use, y la vista previa lo indica.

<a id="randomness"></a>

## Aleatoriedad

| Macro | Resultado |
|---|---|
| `{{random::rojo::verde::azul}}` | Una de las opciones, distinta cada vez. También `{{random:rojo,verde,azul}}` y `{{random}}rojo, verde{{/random}}`. |
| `{{pick::espada::hacha::arco}}` | Una de las opciones, pero siempre **la misma** para el mismo libro y el mismo lugar de la plantilla. Úsala para cosas que deben mantenerse coherentes, como un nombre elegido al azar. |
| `{{roll::2d6+3}}` | Una tirada de dados: `1d20`, `3d4-10`, `d100`; también `{{roll d100}}`, `{{roll:6}}` (un dado de seis caras). Las tiradas no válidas no dan nada. |

<a id="generation-info"></a>

## Información de la generación

| Macro | Resultado |
|---|---|
| `{{model}}` | El nombre del [proveedor de inferencia](../inference-providers.md) del libro. |
| `{{maxContextTokens}}` | El límite de contexto en tokens (del protocolo o del proveedor, ver [Límites](../protocols.md#limits)). Alias `{{maxContext}}`. |
| `{{maxResponseTokens}}` | El límite de respuesta en tokens. Alias `{{maxResponse}}`. |
| `{{maxPrompt}}` | Contexto menos respuesta: el espacio disponible para el prompt. |
| `{{lastGenerationType}}` | `normal` para una parte nueva, `regenerate`, `swipe`, o `quiet` en los resúmenes. |

<a id="commands"></a>

## Comandos

| Macro | Resultado |
|---|---|
| `{{command::nombre}}`, `{{command::nombre::argumento::argumento}}` | Lo que responde un [comando](../commands/index.md), como texto. Solo se puede usar un comando hecho para macros (solo lee; una extensión lo indica, por ejemplo `{{command::character-tracker-map}}` del [Character Tracker](../extensions/character-tracker.md)); cualquier otro nombre imprime `Error`. Un comando que falla no imprime nada. |

El comando se ejecuta cuando la generación llega a la macro (nunca, si está en una rama de un `{{if}}` que no se toma),
para el libro que se genera, y solo en los prompts de una generación (el prompt del usuario, las entradas del lorebook y la plantilla
maestra). En otros lugares, la macro no imprime nada. La respuesta se guarda con la parte de la que continúa la nueva parte: la siguiente generación desde la misma parte,
y las otras plantillas de la misma generación, la usan en lugar de volver a ejecutar el comando.
**Regenerar** y **swipe** vuelven a pedir el comando, y también lo hace cada parte nueva, ya que continúa desde una nueva última parte.

<a id="utility"></a>

## Utilidades

| Macro | Resultado |
|---|---|
| `{{newline}}`, `{{newline::2}}` | Uno o más saltos de línea. |
| `{{space}}`, `{{space::4}}` | Uno o más espacios. |
| `{{trim}}` | Elimina los saltos de línea que la rodean. Útil después de líneas que solo asignan variables. |
| `{{noop}}` | Nada. |
| `{{join::separador::a::b::c}}` | Los textos después del primero, unidos con el primero entre ellos, en cualquier cantidad (también macros): `{{join::-::{{user}}::{{scenario}}}}`. Los textos vacíos se omiten, así que una macro opcional no deja separadores sobrantes. Se quitan los espacios alrededor de un argumento escrito, así que para un separador con espacios usa una macro: `{{join::{{space}}::a::b}}`, `{{join::{{newline}}::a::b}}`. |
| `{{reverse::texto}}` | El texto al revés. |
| `{{// comentario}}` | Un comentario, que se elimina. `{{//}}` ... `{{///}}` comenta todo lo que hay entre ellos, incluidas las macros. |
| `\{\{user\}\}` | Imprime `{{user}}` literalmente. |

<a id="sillytavern-only-macros"></a>

## Macros solo de SillyTavern

Las macros que solo tienen sentido en chats de SillyTavern (personas y campos de la ficha del personaje, como `{{persona}}`,
`{{personality}}`, `{{charPrompt}}`, `{{mesExamples}}`...), las secuencias del modo instrucción (`{{instructUserPrefix}}`...),
las notas del autor, los ID de swipe, `{{outlet}}` y similares, se reconocen e imprimen **nada**, para que el world info importado
no filtre esas macros en el prompt. `{{isMobile}}` y `{{hasExtension}}` imprimen `false`.

Una macro que Marginalia no conoce:

- con argumentos (`{{fooBar::x}}`) se imprime tal cual, como lo hace SillyTavern;
- sin argumentos (`{{fooBar}}`) se imprime como **`Error`**, igual que una
  [variable de plantilla](templates.md#typos) desconocida.

}%}
