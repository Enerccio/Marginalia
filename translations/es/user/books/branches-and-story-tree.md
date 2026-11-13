---
label: Ramas y árbol de la historia
order: 80
---

# Ramas y árbol de la historia

Marginalia nunca te obliga a descartar una versión. Cada alternativa que pruebas se guarda como una **rama** de la historia, y puedes volver a cualquiera de ellas.

<a id="how-the-story-tree-works"></a>

## Cómo funciona el árbol de la historia

Cada parte continúa la parte anterior. Cuando una parte tiene más de una continuación, la historia se divide: cada camino desde la primera parte hasta un final es una rama.

```
#1 ─ #2 ─ #3 ─ #4 ─ #5          ← branch A
           └─ #4' ─ #5' ─ #6'   ← branch B (active)
```

La **rama activa** es la que muestra y continúa el [editor de la historia](story-editor.md). Solo sus partes se envían al modelo; las demás ramas no influyen en la historia.

Hay tres formas de crear una rama nueva:

| | Qué pasa | La versión anterior |
|---|---|---|
| **Variante** (última parte) | El modelo escribe otra versión de la última parte. | Se mantiene como una rama separada. |
| **Crear una rama** (cualquier parte) | La historia continúa desde esta parte en una rama nueva. | Las partes posteriores se quedan en la rama anterior. |
| **Regenerar** (última parte) | El modelo vuelve a escribir la última parte, en el mismo lugar. | Se reemplaza; no se crea ninguna rama. |

*Variante* y *Regenerar* solo están en el menú de la última parte; *Crear una rama* está en el menú de todas las partes.

<a id="trying-another-version-of-the-last-part"></a>

## Probar otra versión de la última parte

Cuando no te gusta la última parte, elige **Variante** en su menú. El modelo vuelve a escribir la parte con los mismos [detalles del turno](story-editor.md#turn-details), y la nueva versión pasa a ser el final de la rama activa. La versión anterior se conserva como rama propia; puedes volver a ella en la [vista de ramas](#the-branch-view).

Para cambiar lo que debe ocurrir antes de volver a intentarlo, abre **Detalles del turno** de la última parte y edita las instrucciones.

Usa **Regenerar** solo si no necesitas la versión anterior: sobrescribe la parte.

<a id="branching-from-an-earlier-part"></a>

## Crear una rama desde una parte anterior

Para llevar la historia en otra dirección desde un punto anterior, abre el menú de esa parte y elige **Crear una rama**. Marginalia copia la parte en una rama nueva y la convierte en el final de la rama activa, así que el editor de la historia ahora termina con esa parte. Continúa con **+** como de costumbre.

Las partes que venían después no se pierden: se quedan en la rama anterior.

Los [Resúmenes](summaries.md) de la parte copiada se copian con ella.

<a id="the-branch-view"></a>

## La vista de ramas

La pestaña **Vista de ramas** muestra el árbol completo de la historia. Cada parte es una tarjeta con su fecha, escenario, personajes presentes e instrucciones; pasa el cursor sobre una tarjeta para ver todos sus detalles.

![El árbol de la historia](../../images/story-tree.png)

- Las partes de la **rama activa** están resaltadas, y su última parte está marcada.
- **Haz clic en la última parte de otra rama** para activar esa rama. El libro cambia a la pestaña *Historia* y la muestra. Solo se puede hacer clic en los finales de las ramas; para continuar desde una parte del medio, usa [Crear una rama](#branching-from-an-earlier-part).
- Los tramos largos sin ninguna bifurcación (más de 20 partes) se agrupan en un bloque que muestra las primeras diez y las últimas diez partes. Haz clic en el bloque para mostrar todas las partes.

<a id="searching-all-branches"></a>

### Buscar en todas las ramas

El cuadro encima del árbol busca en el texto de **todas las ramas** del libro, no solo en la activa. Escribe lo que buscas y pulsa **Enter** o haz clic en **Buscar**; el cuadro recuerda tus últimas búsquedas, y puedes elegir una de la lista para repetirla. La **x** del cuadro borra la búsqueda.

- Una parte coincide cuando su texto contiene **todas las palabras** que escribiste, en cualquier orden: `lighthouse key` encuentra las partes que tienen ambas palabras. Pon las palabras entre comillas dobles para buscar una frase: `"lighthouse key"`.
- Usa `*` para cualquier conjunto de caracteres: `light*` encuentra *lighthouse* y *lightning*. Los demás caracteres, incluidos `%`, `_` y `\`, se buscan tal cual.
- No importan las mayúsculas ni las minúsculas. En las letras con tilde o diacríticos, una palabra escrita en minúsculas también encuentra la misma palabra en MAYÚSCULAS o con mayúscula inicial (`žluťoučký` encuentra *Žluťoučký* y *ŽLUŤOUČKÝ*).
- Solo se busca en el texto de las partes, no en las instrucciones, el escenario ni el libro de personajes y lugares. Las partes que eliminaste no se buscan.

![La vista de ramas con una búsqueda](../../images/story-tree-search.png)

La casilla **Incluir versiones** junto al cuadro también busca en las [versiones anteriores del texto](version-history.md#searching-the-old-text) de las partes; una parte que tiene las palabras solo en una versión antigua también se resalta, indica qué versión es y muestra **Show in history** junto a **Mostrar en la historia**.

Cada parte que coincide se **resalta** en naranja y muestra el texto alrededor de la coincidencia; la línea encima del árbol indica cuántas partes se encontraron. Los tramos largos que normalmente estarían agrupados se abren cuando contienen una coincidencia. La búsqueda se mantiene hasta que la borres o cierres la ventana del libro, y el resaltado se actualiza cuando vuelves a la pestaña.

Haz clic en **Mostrar en la historia** en una parte resaltada para leerla:

- Si la parte está en la rama activa, el libro cambia a la pestaña *Historia* y se desplaza hasta ella.
- Si está en otra rama, esa rama pasa primero a ser la rama activa. Termina en el final de rama más cercano por debajo de la parte encontrada (el más reciente si hay dos), de modo que el editor de la historia muestra la historia hasta la parte encontrada y lo que vino después. Luego el libro cambia a la pestaña *Historia* y se desplaza hasta la parte, que parpadea brevemente.

Cambiar de rama es lo mismo que hacer clic en el final de una rama en el árbol: no se pierde nada, la rama anterior se mantiene tal como estaba y puedes volver a ella desde el árbol.

<a id="deleting-parts-and-branches"></a>

## Eliminar partes y ramas

**Eliminar** en el menú de una parte borra solo esa parte. Las partes posteriores suben de posición y continúan desde la parte anterior. Para quitar una rama completa, elimina sus partes desde el final.

Los recuentos de palabras y tokens de la pestaña *Información* muestran el libro entero (todas las ramas) y la rama activa.
