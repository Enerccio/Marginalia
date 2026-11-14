---
label: Referencia de macros
order: 90
---

# Referencia de macros

Marginalia entiende las macros de [SillyTavern](https://docs.sillytavern.app/usage/core-concepts/macros/), así que los prompts y la información de mundo escritos para SillyTavern funcionan sin cambios. Las macros funcionan en todas las [plantillas](templates.md): plantilla maestra, prompt del usuario, prompt de resumen y entradas del lorebook.

{%{

<a id="syntax"></a>

## Sintaxis

| Forma | Ejemplo |
|---|---|
| sin argumentos | `{{user}}` |
| argumentos separados por `::` | `{{setvar::mood::nervous}}` |
| primer argumento tras un espacio | `{{getvar mood}}`, `{{setvar greeting::Hello}}` |
| `:` único heredado | `{{getvar:mood}}`, `{{random:north,south}}` |
| macros anidadas | `{{getvar::{{user}}_mood}}` |
| con ámbito (el contenido como último argumento) | `{{setvar backstory}}Long text...{{/setvar}}` |

- Los nombres de las macros no distinguen mayúsculas: `{{User}}` y `{{USER}}` funcionan.
- Los espacios alrededor de los nombres y los argumentos se ignoran: `{{ getvar :: mood }}`.
- Los `::` extra van al último argumento: `{{setvar::x::a::b}}` asigna `x` a `a::b`.
- El contenido con ámbito se evalúa primero, luego se le quita la sangría y se recorta. Con `#` después de las llaves,
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
| `{{scenario}}` | La ambientación de la escena. |
| `{{input}}` | Las instrucciones de la parte. |

Los marcadores de SillyTavern `<USER>`, `<BOT>`, `<CHAR>`, `<GROUP>` y `<CHARIFNOTGROUP>` (en cualquier caso) también funcionan.

Con el personaje POV *Alice* y los personajes presentes *Alice, Bob, Carol*:
`{{user}}` → *Alice*, `{{group}}` → *Alice, Bob, Carol*, `{{notChar}}` → *Bob, Carol*.

<a id="story-history"></a>

## Historia de la narración

| Macro | Resultado |
|---|---|
| `{{lastMessage}}` | El texto de la última parte de la rama activa. Alias `{{lastCharMessage}}`. |
| `{{lastMessageId}}` | La posición de la última parte, contando desde 0. |
| `{{lastUserMessage}}` | Las instrucciones de la última parte. |
| `{{summary}}` | El [resumen](../books/summaries.md) más reciente de la rama activa. |
| `{{closest-message}}`, `{{closest-message::n}}` | La parte más cercana a la que se está escribiendo: la última, o la *n*-ésima contando hacia atrás desde ella (`::2` es la anterior a la última). Vacía si no existe tal parte. Alias `{{closestMessage}}`. |
| `{{closest-summary}}`, `{{closest-summary::n}}` | Lo mismo para los resúmenes de la rama: el más reciente, o el *n*-ésimo contando hacia atrás. Alias `{{closestSummary}}`. |
| `{{current-message}}` | El texto de la parte que *Regenerar* o una alternativa (swipe) reemplaza; vacía para una parte nueva. Alias `{{currentMessage}}`. |

Para la primera parte de un libro están vacías. Para *Regenerar*, la "última parte" es la anterior a la parte regenerada.

El número de `closest-message` y `closest-summary` puede ser una macro, así que puede venir de una variable:
`{{setvar::back::3}}{{closest-message::{{getvar::back}}}}` es la tercera parte desde el final. Un número que no sea un entero positivo no da nada. `closest-summary` cuenta los resúmenes de las partes de la rama, incluido un [metaresumen](../books/summaries.md) y los resúmenes que sustituye; `{{summary}}` es lo mismo que `{{closest-summary}}`.

<a id="date-and-time"></a>

## Fecha y hora

| Macro | Resultado de ejemplo |
|---|---|
| `{{time}}` | `2:30 PM` |
| `{{time::UTC+2}}` | `4:30 PM` - la hora con un desplazamiento UTC (`UTC-5:30` también funciona) |
| `{{date}}` | `March 15, 2026` |
| `{{weekday}}` | `Sunday` |
| `{{isodate}}` | `2026-03-15` |
| `{{isotime}}` | `14:30` |
| `{{datetimeformat::YYYY-MM-DD HH:mm}}` | `2026-03-15 14:30` - [tokens de formato de moment.js](https://momentjs.com/docs/#/displaying/format/); el texto entre `[brackets]` se imprime tal cual: `{{datetimeformat::dddd [the] D. MMMM}}` → `Sunday the 15. March` |
| `{{idleDuration}}` | `2 hours` - tiempo desde que se escribió la última parte; `just now` para la primera parte. Alias `{{idle_duration}}`. |
| `{{timeDiff::2026-01-01 12:00::2026-01-01 10:00}}` | `in 2 hours` - la diferencia entre dos momentos; `now`, las fechas (`2026-01-08`) y las horas ISO funcionan. Los valores desconocidos no dan nada. |

Las horas están en la zona horaria del equipo o servidor donde se ejecuta Marginalia, y siempre en inglés. Para la [versión Docker](../getting-started/docker-server.md), configura la zona horaria con la variable de entorno `TZ`, p. ej.
`TZ=Europe/Prague`.

## Variables

Las variables guardan valores entre partes: contadores, el estado de ánimo de un personaje, cosas que la historia ya ha establecido. Hay dos tipos:

- **local**: pertenece a la rama de la historia; cada parte recuerda sus valores, y una parte nueva empieza con los valores de la parte anterior;
- **global**: pertenece al libro y se comparte entre todas las ramas.

Consulta [Qué se guarda entre partes](templates.md#what-is-kept-between-parts).

| Local | Global | |
|---|---|---|
| `{{getvar::name}}` | `{{getglobalvar::name}}` | El valor; vacío si no está definida. |
| `{{setvar::name::value}}` | `{{setglobalvar::name::value}}` | Asigna el valor. No muestra nada. |
| `{{addvar::name::value}}` | `{{addglobalvar::name::value}}` | Suma un número, o añade texto cuando alguno de los dos no es un número. No muestra nada. |
| `{{incvar::name}}` | `{{incglobalvar::name}}` | Suma 1 y muestra el nuevo valor. Un valor que no es un número empieza desde 0. |
| `{{decvar::name}}` | `{{decglobalvar::name}}` | Resta 1 y muestra el nuevo valor. |
| `{{hasvar::name}}` | `{{hasglobalvar::name}}` | `true` o `false`. |
| `{{deletevar::name}}` | `{{deleteglobalvar::name}}` | Elimina la variable. Alias `flushvar` / `flushglobalvar`. |

Los números se muestran sin ceros finales: `1.50` más `1.5` es `3`.

```handlebars
{{setvar::hp::10}}{{addvar::hp::5}}HP: {{getvar::hp}}      → HP: 15
{{setvar::items::a rope}}{{addvar::items::, a lamp}}{{getvar::items}}   → a rope, a lamp
```

<a id="shorthands"></a>

### Atajos

`{{.name}}` es una variable local, `{{$name}}` una global. Por sí solo, un atajo muestra el valor. Con un operador:

| Atajo | Efecto | Muestra |
|---|---|---|
| `{{.hp = 20}}` | asigna el valor | nada |
| `{{.hp += 5}}`, `{{.hp -= 5}}` | suma / resta (`+=` añade texto al texto) | nada |
| `{{.turn++}}`, `{{.turn--}}` | suma / resta 1 | el nuevo valor |
| `{{.hp == 17}}`, `{{.hp != 17}}` | compara | `true` / `false` |
| `{{.hp > 10}}`, `>=`, `<`, `<=` | compara números (`false` para texto) | `true` / `false` |

Alternativas y valores por defecto:

- `{{.name || Guest}}` muestra *Guest* cuando la variable está vacía o es falsy (`0`, `false`, `no`, `off`);
- `{{.name ?? Guest}}` muestra *Guest* solo cuando la variable no está definida en absoluto;
- `{{.title ||= Wanderer}}` asigna *Wanderer* a la variable si está vacía o es falsy, y luego la muestra;
- `{{.title ??= Wanderer}}` la asigna solo cuando no está definida en absoluto, y luego la muestra.

Los valores pueden contener macros: `{{.log += -> {{user}}}}`.

Por ejemplo, un contador de partes al principio del prompt del usuario (`+=` no muestra nada, y `{{trim}}` elimina la línea vacía que deja):

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
| un nombre de macro - `{{if user}}` | la macro da un valor |
| una variable - `{{if .mood}}`, `{{if $chapter}}` | la variable tiene un valor |
| una variable de plantilla - `{{if sceneSetting}}` | la variable tiene un valor |
| una comparación - `{{if .hp > 20}}` | la comparación es verdadera |
| cualquier texto - `{{if yes}}` | siempre, salvo para los valores falsy |
| negación - `{{if !user}}`, `{{if !.mood}}` | lo contrario |

Son falsy un valor vacío y `0`, `false`, `no`, `off` (en cualquier caso). Todo lo demás es verdadero.

- La forma corta `{{if user::text}}` muestra *texto* cuando la condición es verdadera.
- Solo se evalúa la rama que se toma: un `{{setvar}}` en la otra rama no se ejecuta.
- Los bloques `{{if}}` se recortan y se les quita la sangría como las macros con ámbito, así que pueden tener sangría para mejorar la legibilidad. `{{#if}}`
  (el [Handlebars if](templates.md#conditions)) conserva los espacios en blanco.
- Un `{{if}}` sin `{{/if}}` se muestra como texto.
<a id="expressions"></a>

## Expresiones

`{{eval::expression}}` devuelve `true` o `false`. Úsalo cuando haga falta una comparación que `{{if}}` no puede hacer: con los valores de la plantilla (no solo con variables), unidos con `and`, `or` y `not`:

```handlebars
{{eval::povCharacter == "Fiora" and presentCharacters has "Mara"}}
{{if {{eval::.hp < 10 or not .armed}} }}Fiora is in danger.{{/if}}
```

| En una expresión | Significado |
|---|---|
| `"text"`, `'text'` | Un texto. **Un texto siempre va entre comillas**: una palabra sin comillas es un nombre. |
| `12`, `-3.5`, `true`, `false` | Un número o un valor de verdad. |
| `povCharacter`, `presentCharacters`, `sceneSetting`... | Un valor de la plantilla, como en `{{povCharacter}}`. Una macro sin argumentos también funciona (`user`, `summary`, `lastMessage`). |
| `.mood`, `$chapter` | Una [variable](#variables) local o global; vacía cuando no está definida. |
| `( )` | Agrupación. |

Comparaciones:

| Operador | Es verdadero cuando |
|---|---|
| `==`, `!=` | Igual, distinto. Los números se comparan por valor (`5 == 5.0`); cualquier otra cosa, exactamente, así que `"Fiora" == "fiora"` es falso. |
| `~=` | Igual, sin distinguir mayúsculas. |
| `<`, `<=`, `>`, `>=` | Los números están en ese orden. Falso cuando un lado no es un número. |
| `contains` | El texto de la izquierda contiene el texto de la derecha, sin distinguir mayúsculas: `presentCharacters contains "ma"` también es verdadero para *Mara*. |
| `has` | La lista de la izquierda, separada por comas, punto y coma o saltos de línea, tiene el elemento de la derecha, sin distinguir mayúsculas ni los espacios alrededor de los elementos: `presentCharacters has "Mara"` no es verdadero para *Marati*. |

Combinación: `and` (`&&`), `or` (`||`), `not` (`!`); `not` tiene la mayor precedencia, luego `and` y después `or`. Un valor por sí solo es verdadero salvo que esté vacío, `0`, `false`, `no` o `off`. Se permiten macros dentro de un texto (`"{{user}}"`) y se procesan primero.

Una expresión no válida imprime `Error`: un error de sintaxis, una palabra sin comillas que no es un nombre (`povCharacter == Fiora`), un nombre desconocido. Se comprueban todos los nombres, también en la parte de un `and` / `or` que no llegaría a usarse, para que una errata no pase desapercibida. En una [condición del lorebook](../lorebooks/entries.md#a-condition-for-an-entry) un `Error` hace que la entrada no se use, y la vista previa lo indica.

<a id="randomness"></a>

## Aleatoriedad

| Macro | Resultado |
|---|---|
| `{{random::red::green::blue}}` | Una de las opciones, distinta cada vez. También `{{random:red,green,blue}}` y `{{random}}red, green{{/random}}`. |
| `{{pick::sword::axe::bow}}` | Una de las opciones, pero siempre **la misma** para el mismo libro y el mismo lugar de la plantilla. Úsala para cosas que deben mantenerse coherentes, como un nombre elegido al azar. |
| `{{roll::2d6+3}}` | Una tirada de dados: `1d20`, `3d4-10`, `d100`; también `{{roll d100}}`, `{{roll:6}}` (un dado de seis caras). Las tiradas no válidas no dan nada. |

<a id="generation-info"></a>

## Información de generación

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
| `{{command::name}}`, `{{command::name::argument::argument}}` | Lo que responde un [comando](../commands/index.md), como texto. Solo se puede usar un comando hecho para macros (solo lee; una extensión lo indica, por ejemplo `{{command::character-tracker-map}}` del [Character Tracker](../extensions/character-tracker.md)); cualquier otro nombre imprime `Error`. Un comando que falla no imprime nada. |

El comando se ejecuta cuando la generación llega a la macro (no se ejecuta nunca si está en una rama de un `{{if}}` que no se toma) para el libro que se genera, y solo en los prompts de una generación (el prompt del usuario, las entradas del lorebook y la plantilla maestra); en otros lugares la macro no imprime nada. La respuesta se guarda junto con la parte de la que continúa la nueva parte: la siguiente generación desde la misma parte, y las demás plantillas de la misma generación, la usan en lugar de volver a ejecutar el comando. **Regenerar** y **swipe** vuelven a pedir el comando, y también lo hace cada parte nueva, ya que continúa desde una nueva última parte.

<a id="utility"></a>

## Utilidades

| Macro | Resultado |
|---|---|
| `{{newline}}`, `{{newline::2}}` | Uno o más saltos de línea. |
| `{{space}}`, `{{space::4}}` | Uno o más espacios. |
| `{{trim}}` | Elimina los saltos de línea que la rodean. Útil después de líneas que solo definen variables. |
| `{{noop}}` | Nada. |
| `{{join::separator::a::b::c}}` | Los textos después del primero unidos por el primero como separador, en cualquier cantidad (también macros): `{{join::-::{{user}}::{{scenario}}}}`. Los textos vacíos se omiten, así que una macro opcional no deja separadores sobrantes. Los espacios alrededor de un argumento escrito se eliminan; por eso, para un separador con espacios usa una macro: `{{join::{{space}}::a::b}}`, `{{join::{{newline}}::a::b}}`. |
| `{{reverse::text}}` | El texto al revés. |
| `{{// comment}}` | Un comentario, que se elimina. `{{//}}` ... `{{///}}` comenta todo lo que hay entre ellos, incluidas las macros. |
| `\{\{user\}\}` | Imprime `{{user}}` literalmente. |

<a id="sillytavern-only-macros"></a>

## Macros solo de SillyTavern

Las macros que solo tienen sentido en los chats de SillyTavern (personas y campos de la tarjeta de personaje (`{{persona}}`, `{{personality}}`, `{{charPrompt}}`, `{{mesExamples}}`...), secuencias del modo instruct (`{{instructUserPrefix}}`...), notas del autor, IDs de swipe, `{{outlet}}` y similares) se reconocen e imprimen **nada**, para que la información del mundo importada no las filtre al prompt. `{{isMobile}}` y `{{hasExtension}}` imprimen `false`.

Una macro que Marginalia no conoce:

- con argumentos (`{{fooBar::x}}`) se imprime tal cual, como hace SillyTavern;
- sin argumentos (`{{fooBar}}`) se imprime como **`Error`**, igual que una [variable de plantilla](templates.md#typos) desconocida.

}%}
