---
label: Plantillas y macros
order: 940
icon: code
---

# Plantillas y macros

Todo lo que Marginalia envía al modelo se construye a partir de **plantillas**: textos con marcadores de posición que se rellenan para cada parte generada. Puedes modificarlas todas, desde el marco del prompt hasta una sola entrada del libro de lore.

{%{
```handlebars
Write the next section of the story.
{{#povCharacter}}Point of View Character: {{.}}{{/povCharacter}}
{{instructions}}
```
}%}

<a id="where-templates-are-used"></a>

## Dónde se usan las plantillas

| Plantilla | Se configura en | Se convierte en |
|---|---|---|
| **Plantilla principal** | [Prompts](../books/prompts.md) del libro, *Settings → Templates* | el mensaje de sistema: rol, lore, estilo, resúmenes |
| **User Prompt** | Prompts del libro, *Settings → Templates* | la petición para la parte que se está escribiendo |
| **Summary Prompt** | Prompts del libro, *Settings → Templates* | la petición para un [resumen](../books/summaries.md) |
| **Meta Summary Prompt** | Prompts del libro, *Settings → Templates* | la petición para un [metaresumen](../books/summaries.md#meta-summaries) |
| **Contenido de la entrada de lorebook** | [entradas de lorebook](../lorebooks/entries.md) | el lore que se inserta en el prompt |

*Point of View*, *Tense* y *Style* son texto plano, no plantillas.

<a id="two-kinds-of-placeholders"></a>

## Dos tipos de marcadores de posición

Las plantillas combinan dos sintaxis:

- **Handlebars** - variables como {%{`{{instructions}}`}%} y secciones como
  {%{`{{#sceneSetting}}...{{/sceneSetting}}`}%} que solo se imprimen cuando el valor no está vacío. Consulta
  [Plantillas de prompt](templates.md).
- **Macros de SillyTavern** - {%{`{{user}}`, `{{random::a::b}}`, `{{setvar::name::value}}`, `{{if ...}}`}%} y el resto de
  macros que conocen los usuarios de SillyTavern, para que los lorebooks y prompts de SillyTavern sigan funcionando. Consulta
  [Referencia de macros](macros.md).

Ambas funcionan en todas las plantillas y se pueden mezclar libremente.

<a id="help-in-the-editor"></a>

## Ayuda en el editor

Cada campo de plantilla tiene un menú emergente de sugerencias:

- **Insertar la plantilla predeterminada** copia la predeterminada en el campo;
- **Variables de plantilla disponibles** enumera las variables de esa plantilla, incluidas las disponibles en todas partes;
- **Macros disponibles** enumera todas las macros con una breve descripción.

![El menú emergente de sugerencias de un campo de plantilla](../../images/template-hints.png)

Una plantilla con un error de sintaxis (una sección sin cerrar, por ejemplo) no se puede guardar en los prompts del libro ni en Settings; en las entradas de lorebook sí se guarda, se marca como no válida y se usa como texto plano hasta que se corrija.

Las variables desconocidas (normalmente erratas) no impiden guardar; el campo las lista debajo como advertencia, consulta [Erratas](templates.md#typos).

**Mostrar el prompt** en el menú de una parte generada muestra el prompt tal como se envió; es la mejor forma de comprobar lo que produjo una plantilla.

<a id="in-this-section"></a>

## En esta sección

- [Plantillas de prompt](templates.md) - variables de cada plantilla, secciones, condiciones, orden de evaluación
- [Referencia de macros](macros.md) - todas las macros, variables, condicionales y compatibilidad con SillyTavern
