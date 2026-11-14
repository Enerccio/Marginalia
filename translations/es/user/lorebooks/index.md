---
label: Lorebooks
order: 950
icon: archive
---

# Lorebooks

!!!
El [diseño para el teléfono](../phone.md#lorebooks) tiene el mismo editor en una sola columna: los botones de los lorebooks están en un menú, los ajustes del
lorebook están en una sección que se abre y una entrada se abre en un formulario. Solo algunas cosas se han quedado en el escritorio, consulta la lista de allí.
!!!

Un lorebook guarda lo que el modelo debe saber sobre tu mundo: personajes, lugares, historia, reglas, terminología. Cada
fragmento de conocimiento es una **entrada**. Antes de cada parte generada, Marginalia decide qué entradas son relevantes y las
incluye en el prompt, así el modelo sabe quién es el capitán Varro cuando entra en la escena, sin que cada entrada ocupe espacio en todos los prompts.

![El editor de lorebooks](../../images/lorebook.png)

<a id="how-it-fits-together"></a>

## Cómo encajan las piezas

```
Book ──uses──▶ Lorebook "Saga" ──sub lorebooks──▶ "Characters", "Places", "Magic"
                   │                                   │
                 entries                             entries
```

- Un **libro** usa un lorebook (en su pestaña *Lorebook*).
- Un lorebook puede incluir otros lorebooks como **sub lorebooks**. Sus entradas también participan, así que puedes guardar material compartido (un mundo usado por varios libros, un reparto de personajes) en su propio lorebook e incluirlo donde haga falta.
- Cada **entrada** tiene el texto que va al prompt y las condiciones que deciden cuándo se usa.

<a id="when-an-entry-is-used"></a>

## Cuándo se usa una entrada

Para cada parte generada, una entrada se **activa** cuando se cumplen todas estas condiciones:

1. la entrada y su lorebook están habilitados;
2. sus **etiquetas** coinciden con las etiquetas del libro (si tiene alguna);
3. su **filtro** coincide con las instrucciones de la parte (si tiene uno) o, cuando la entrada mira la historia, también las partes más recientes de la historia.

Las entradas activadas se insertan en su **orden**, en el bloque de lore del prompt del sistema o justo antes de la
petición de la parte. Consulta [Entradas y activación](entries.md).

Las entradas sin etiquetas y sin filtro están siempre activas: úsalas para lo que el modelo debe saber siempre.

<a id="lorebooks-and-other-features"></a>

## Lorebooks y otras funciones

- Las entradas de lorebook pueden usar [plantillas y macros](../templates/index.md); la información de mundo de SillyTavern sigue funcionando.
- La [información de mundo de SillyTavern](import-export.md#importing-sillytavern-world-info) se puede importar.
- Las [copias de seguridad de libros](../books/backups.md) contienen el lorebook del libro con sus sub lorebooks.
- La extensión [Lore Generator](../extensions/lore-generator.md) escribe el contenido de una entrada con el modelo, a partir del lore que ya tienes.
- La extensión [Lorebook VCS](../extensions/lorebook-vcs.md) guarda un historial de revisiones de las entradas.

<a id="in-this-section"></a>

## En esta sección

- [Crear lorebooks](lorebooks.md): el editor de lorebooks, sub lorebooks, usar un lorebook en un libro
- [Entradas y activación](entries.md): campos de la entrada, etiquetas, filtros, orden e inserción
- [Importación y exportación](import-export.md): archivos de lorebook de Marginalia e información de mundo de SillyTavern
