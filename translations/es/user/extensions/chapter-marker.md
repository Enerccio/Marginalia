---
label: Marcador de capítulo
order: 20
---

# Marcador de capítulo

Convierte el **Esquema** del [editor de la historia](../books/story-editor.md) en un índice de contenidos.

![Capítulos en el esquema](../../images/plugin-chaptermarker.png)

<a id="how-it-works"></a>

## Cómo funciona

Cuando el texto de una parte contiene un encabezado Markdown (una línea que empieza por `#`), la entrada de la parte en el esquema muestra el encabezado en lugar de la etiqueta habitual `#n (DB ID: m)`, resaltado:

```
# Chapter 3: The Harbour

Rain had been falling on Ostra since dawn...
```

aparece en el esquema como **5. Chapter 3: The Harbour**. El número es la posición de la parte dentro de la rama, seguido del encabezado. Las partes sin encabezado conservan su etiqueta normal.

- El encabezado puede estar en cualquier línea de la parte; si hay varios, se usa el primero.
- Funciona cualquier nivel de encabezado (`#`, `##`, `###`...).
- El esquema se actualiza en cuanto guardas una [edición](../books/story-editor.md#editing-the-text): añade un encabezado para empezar un capítulo y elimínalo para volver a unir la parte con la anterior.

<a id="usage"></a>

## Uso

No hay nada que configurar. Puedes escribir tú mismo el encabezado en la parte o pedirle al modelo que lo incluya en las instrucciones, por ejemplo: *Empieza un nuevo capítulo titulado "The Harbour" con un encabezado Markdown.*

!!!
La [plantilla de usuario](../books/prompts.md#user-prompt) predeterminada indica al modelo que no escriba títulos ni encabezados de capítulo.
Si quieres que el modelo los escriba, indícalo explícitamente en las instrucciones o cambia esa regla en la plantilla de usuario.
!!!

Los encabezados forman parte del texto de la historia, así que también se envían al modelo y se muestran en el [visor](../books/publishing.md) y en las copias de seguridad.

La misma regla identifica los capítulos en el índice de contenidos de los [libros exportados](../books/export.md#table-of-contents).
