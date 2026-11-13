---
label: Plantillas de prompt
order: 100
---

# Plantillas de prompt

Las plantillas usan la sintaxis de [Handlebars](https://handlebarsjs.com/guide/), ampliada con las
[macros](macros.md) de SillyTavern. Esta página explica las variables que recibe cada plantilla y la parte de la sintaxis de Handlebars.

{%{

## Variables

Una variable entre llaves dobles se sustituye por su valor: `{{instructions}}` se convierte en las instrucciones que escribiste para
la parte. Un valor vacío no imprime nada.

<a id="variables-of-each-template"></a>

### Variables de cada plantilla

| Plantilla | Variables |
|---|---|
| **Plantilla principal** | `{{backgroundLore}}` entradas de lore activadas (*En el bloque de lore*), `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}`, `{{summaries}}` todos los resúmenes de la rama, `{{annotations}}` las [notas](../books/annotations.md#giving-a-note-to-the-model) que marcaste para dárselas al modelo, en las partes del prompt |
| **User Prompt** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}`: las instrucciones del turno |
| **Summary Prompt** | `{{text}}` las partes que hay que resumir, `{{backgroundLore}}` lore activo cuando se escribió la parte resumida |
| **Meta Summary Prompt** | `{{summaryBlocks}}` los resúmenes que hay que fusionar, del más antiguo al más reciente, `{{backgroundLore}}` lore activo cuando se escribió la parte del meta-resumen |
| **Lorebook entry** | `{{sceneSetting}}`, `{{povCharacter}}`, `{{presentCharacters}}`, `{{instructions}}`, `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` |

<a id="variables-available-everywhere"></a>

### Variables disponibles en todas partes

Además de sus propias variables, cada plantilla puede usar:

| Variable | |
|---|---|
| `{{povCharacter}}`, `{{sceneSetting}}`, `{{presentCharacters}}`, `{{instructions}}` | Las instrucciones del turno de la parte que se escribe (en un resumen, de la parte resumida). |
| `{{narrativePov}}`, `{{narrativeTense}}`, `{{style}}` | Los ajustes narrativos del libro. |
| `{{manuscriptName}}`, `{{manuscriptDescription}}` | El nombre y la descripción del libro. |

La ventana de ayuda (ⓘ) de cada campo de plantilla también las enumera.

<a id="typos"></a>

### Erratas

Una variable que no existe se imprime como la palabra **`Error`**: `{{instruction}}` (sin la *s*) mete *Error* en
el prompt. Los campos de plantilla (*Prompts* del libro, *Ajustes*, entradas de lore) muestran esos nombres debajo del campo como
*Unknown names (sent to the model as "Error")*. Es solo una advertencia: la plantilla se guarda igualmente, así que corrige los nombres
que aparecen. Las variables y las secciones se comprueban en todas partes, también dentro de ramas `{{#if}}` que no se imprimen. Los nombres
usados solo como condición no se señalan, porque no imprimen *Error*: en `{{#if name}}` un nombre desconocido es
falso; en la forma de SillyTavern `{{if name}}` se toma como texto plano, que es verdadero.

<a id="sections"></a>

## Secciones

Una sección imprime su contenido solo cuando la variable tiene valor. Dentro, `{{.}}` es el valor:

```handlebars
{{#sceneSetting}}Current Location & Time: {{.}}{{/sceneSetting}}
```

Con *Ambientación de la escena* vacía, la línea entera desaparece: las plantillas predeterminadas lo usan en todas partes, así que el prompt
no tiene encabezados vacíos.

Una **sección invertida** imprime su contenido solo cuando la variable está vacía:

```handlebars
{{^povCharacter}}Write from an omniscient narrator's point of view.{{/povCharacter}}
```

<a id="conditions"></a>

## Condiciones

`{{#if}}` con un `{{else}}` opcional:

```handlebars
{{#if presentCharacters}}
Characters present: {{presentCharacters}}
{{else}}
The POV character is alone.
{{/if}}
```

`{{#unless x}}...{{/unless}}` es lo contrario de `{{#if x}}`. La forma de SillyTavern `{{if x}}...{{/if}}` también funciona y
tiene más opciones (negación, variables, comparaciones); véase [Condicionales](macros.md#conditionals).

Las macros sin argumentos se pueden usar como variables en secciones y condiciones:

```handlebars
{{#lastMessage}}The story so far ends with: {{.}}{{/lastMessage}}
{{#if summary}}...{{/if}}
```

<a id="comments-and-escaping"></a>

## Comentarios y escape

| Sintaxis | Resultado |
|---|---|
| `{{!-- a comment --}}`, `{{! short comment }}` | Se eliminan de la salida. Úsalos para anotar tus plantillas. |
| `\{{instructions}}` | Imprime `{{instructions}}` literalmente en lugar del valor. |
| `{{{instructions}}}` | Igual que `{{instructions}}`. En Marginalia no se aplica escape HTML a nada, así que `<`, `&` y las comillas se imprimen tal cual. |

El comentario de SillyTavern `{{// ...}}` y el escape `\{\{...\}\}` también funcionan; véase [Utilidades](macros.md#utility).

<a id="order-of-evaluation"></a>

## Orden de evaluación

Para una parte generada, las plantillas se procesan en este orden:

1. la plantilla **User Prompt**;
2. las **entradas de lore** que se activaron, en su orden;
3. la **plantilla principal**.

Todas comparten un mismo conjunto de [variables](macros.md#variables): un valor definido con `{{setvar}}` en la plantilla **User Prompt** puede leerse en las entradas de lore y en la plantilla principal; un valor definido en una entrada de lore es visible para las entradas posteriores y para la plantilla principal, pero no para la plantilla **User Prompt**, que ya se procesó.

Los filtros de las entradas de lore ven el prompt de usuario ya procesado, así que una macro en él puede generar texto que active entradas.

<a id="what-is-kept-between-parts"></a>

## Qué se conserva entre partes

| | |
|---|---|
| **Variables locales** (`{{setvar}}`, `{{.name}}`) | Se guardan con la parte generada. La siguiente parte empieza con los valores de la última parte de la rama activa, así que cada rama tiene sus propios valores. *Regenerar* empieza de nuevo con los valores de la parte anterior. |
| **Variables globales** (`{{setglobalvar}}`, `{{$name}}`) | Se guardan con el libro y se comparten entre todas las ramas. *Regenerar* o borrar una parte no las deshacen. |
| **Resúmenes** | Pueden leer variables, pero sus cambios se descartan. |

`{{pick}}` da siempre el mismo resultado para el mismo libro; `{{random}}`, uno nuevo cada vez.

<a id="writing-your-own-templates"></a>

## Escribir tus propias plantillas

Parte de la predeterminada: abre la ventana de ayuda (ⓘ) y pulsa **Insertar la plantilla predeterminada**, y después cámbiala. Conserva:

- el bloque `{{#backgroundLore}}` de la plantilla principal: sin él, las entradas de lore no se usan (solo llegan al modelo las que tienen *Antes del prompt de usuario`);
- el bloque `{{#summaries}}` de la plantilla principal: sin él, los resúmenes se pierden;
- `{{instructions}}` en la plantilla **User Prompt**: sin él, el modelo no ve lo que debe ocurrir;
- `{{text}}` en la plantilla **Summary Prompt**: sin él, no hay nada que resumir.

}%}
