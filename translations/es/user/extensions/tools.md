---
label: Herramientas para el modelo
order: 0
---

# Herramientas para el modelo

Algunas extensiones dan al modelo **herramientas**: funciones que puede invocar mientras escribe, para obtener nombres, buscar lugares reales y
cosas similares, en lugar de inventarlos. Las herramientas de una extensión forman un **conjunto**; los conjuntos disponibles son:

| Conjunto | Extensión | Qué puede hacer el modelo con él |
|---|---|---|
| Generador de nombres | [Generador de nombres](name-generator.md) | Obtener nombres inventados que suenen a una categoría (lugares, deidades, apellidos de un país...). |
| Proveedor de nombres | [Proveedor de nombres](name-provider.md) | Obtener nombres reales con su ortografía original. |
| Proveedor de lugares | [Proveedor de lugares](place-provider.md) | Buscar lugares reales, qué hay alrededor y a qué distancia están entre sí. |

Un diálogo que ejecuta el modelo con herramientas tiene un botón **Herramientas**: los generadores del [Generador de lore](lore-generator.md) y del
[Esquema de arco](arc-outline.md) (no su importación de historias, cuyos nombres ya están en la historia). El botón solo se muestra
mientras haya instalado al menos un conjunto, y cuando has elegido algo indica cuántas cosas están activas: **Herramientas (3)**.

<a id="choosing-the-tools"></a>

## Elegir las herramientas

**Herramientas** abre una ventana con una sección para cada conjunto, bajo el nombre del conjunto, con el número de elementos marcados en él.
Una sección con algo marcado empieza abierta. Lo que hay en una sección depende del conjunto:

- Un conjunto con **elementos** (el Generador de nombres y el Proveedor de nombres tienen uno por cada categoría o lista de nombres) muestra una tabla
  con una casilla por elemento y un **campo de filtro propio** encima: escribe una parte de un nombre para mostrar solo los elementos que coinciden. Los
  elementos marcados permanecen siempre en la tabla, sea cual sea el filtro. **Marcar las mostradas** marca todos los elementos que la tabla
  muestra en ese momento, **Desmarcar las mostradas** los desmarca, y una línea debajo indica cuántos están marcados. El modelo recibe solo los
  elementos marcados: puede listarlos y pedir nombres de ellos, y de ningún otro.
- Un conjunto **sin elementos** (el Proveedor de lugares) está activado o desactivado: **Usar este conjunto**.

![La ventana de Herramientas con las categorías del Generador de nombres filtradas por "Japan"](../../images/tools-dialog.png)

Cada sección tiene también una **Indicación para el modelo**. Es un texto breve que se añade al final de la indicación cuando el modelo recibe el
conjunto, y que le dice cuándo y cómo usar las herramientas (para los nombres: llamar a las herramientas en lugar de inventar nombres, elegir entre
lo que devuelven, y mantener un nombre real tal como se devolvió). El texto integrado se muestra en gris en el campo; escribe el tuyo para reemplazarlo,
o deja el campo vacío para usar el integrado.

![Un conjunto activado, con su indicación para el modelo abierta; las otras dos secciones cerradas con el número de elementos marcados](../../images/tools-hint.png)

El modelo recibe los conjuntos que tienen algo marcado: si no hay nada marcado en ninguno, no hay herramientas. La elección se guarda con
tu cuenta, así que es la misma la próxima vez; el Generador de lore guarda además la elección con cada entrada que generó, y abre esa entrada con ella.
Un conjunto que se descargó se omite, y un elemento marcado que la extensión ya no tiene se ignora. Las extensiones pueden cargarse y descargarse
mientras Marginalia se ejecuta; el botón se actualiza en consecuencia.

Si dos conjuntos tuvieran una función con el mismo nombre, la generación no empezaría e indicaría cuál; los conjuntos que vienen con
Marginalia no lo hacen.

<a id="as-commands"></a>

## Como comandos

Los conjuntos son también comandos de la [barra de comandos](../commands/index.md) y de los [agentes](../agents.md) (`/pick-real-names`,
`/generate-names`, `/search-places`...), así que lo que el modelo recibe aquí y lo que un agente puede invocar es el mismo conjunto con el mismo
nombre.

<a id="from-an-older-version"></a>

## De una versión anterior

El botón **Name tools** pasó a llamarse **Herramientas**. Las categorías que tenías marcadas están marcadas en los conjuntos del Generador de nombres y del
Proveedor de nombres, y las indicaciones que escribiste en *Configuración → Ajustes de extensiones* (*Name generator hint*, *Name provider hint*) son
ahora las indicaciones de esos dos conjuntos en esta ventana; los campos de la configuración han desaparecido.
