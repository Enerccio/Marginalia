---
label: Libros de lore
order: 950
icon: archive
---

# Libros de lore

!!!
El [diseño para teléfono](../phone.md#lorebooks) tiene el mismo editor en una sola columna: los botones de los libros de lore están en un menú, los ajustes del libro de lore están en una sección que se abre y una entrada se abre en un formulario. Solo algunas cosas se quedan para el escritorio; consulta la lista allí.
!!!

Un libro de lore guarda lo que el modelo debe saber sobre tu mundo: personajes, lugares, historia, reglas, terminología. Cada
pieza de conocimiento es una **entrada**. Antes de cada parte generada, Marginalia decide qué entradas son relevantes y las
incluye en el prompt, de modo que el modelo sabe quién es el capitán Varro cuando entra en la escena, sin que cada entrada
ocupe espacio en todos los prompts.

![El editor de libros de lore](../../images/lorebook.png)

<a id="how-it-fits-together"></a>

## Cómo encaja todo

```
Libro ──usa──▶ Libro de lore "Saga" ──subLibros de lore──▶ "Personajes", "Lugares", "Magia"
                   │                                   │
                 entradas                            entradas
```

- Un **libro** usa un libro de lore (en su pestaña *Lorebook*).
- Un libro de lore puede incluir otros libros de lore como **subLibros de lore**. Sus entradas también participan, así que
  puedes guardar material compartido (un mundo usado por varios libros, un reparto de personajes) en su propio libro de lore
  e incluirlo donde haga falta.
- Cada **entrada** tiene el texto que va al prompt y las condiciones que deciden cuándo se usa.

<a id="when-an-entry-is-used"></a>

## Cuándo se usa una entrada

Para cada parte generada, una entrada se **activa** cuando se cumplen todas estas condiciones:

1. la entrada y su libro de lore están habilitados;
2. sus **etiquetas** coinciden con las etiquetas del libro (si tiene alguna);
3. su **filtro** coincide con las instrucciones de la parte (si tiene uno) o, cuando la entrada mira dentro de la historia, también con las partes más recientes de la historia.

Las entradas activadas se insertan según su **orden**, en el bloque de lore del prompt de sistema o justo antes de la
petición de la parte. Consulta [Entradas y activación](entries.md).

Las entradas sin etiquetas y sin filtro están siempre activas: úsalas para lo que el modelo debe saber siempre.

<a id="lorebooks-and-other-features"></a>

## Libros de lore y otras funciones

- Las entradas de los libros de lore pueden usar [plantillas y macros](../templates/index.md); la información de mundo de SillyTavern sigue funcionando.
- Se puede importar [la información de mundo de SillyTavern](import-export.md#importing-sillytavern-world-info).
- Las [copias de seguridad de libros](../books/backups.md) contienen el libro de lore del libro con sus subLibros de lore.
- La extensión [Lore Generator](../extensions/lore-generator.md) escribe el contenido de una entrada con el modelo, a partir del lore que ya tienes.
- La extensión [Lorebook VCS](../extensions/lorebook-vcs.md) mantiene un historial de revisiones de las entradas.

<a id="in-this-section"></a>

## En esta sección

- [Crear libros de lore](lorebooks.md): el editor de libros de lore, subLibros de lore, usar un libro de lore en un libro
- [Entradas y activación](entries.md): campos de la entrada, etiquetas, filtros, orden e inserción
- [Importación y exportación](import-export.md): archivos de libros de lore de Marginalia e información de mundo de SillyTavern
