---
label: Historial de prompts
order: 5
---

# Historial de prompts

Muestra lo que realmente se envió al modelo y lo que respondió, para depurar un plugin o revisar lo que hay en el prompt de cualquier cosa que no sea una generación normal.

<a id="how-it-works"></a>

## Cómo funciona

La extensión guarda las últimas **20 peticiones** enviadas a los modelos en tu sesión: los mensajes que se enviaron, la respuesta y
el razonamiento del modelo (cuando el proveedor lo envía). Se guardan todo tipo de peticiones, también las generaciones. Añade el panel **Prompt history** a *Settings → Extension
Settings*; el botón **Show prompt history** abre la lista, con la petición más reciente primero, con:

- la **fecha** de la petición,
- el **modelo**: el nombre de la IA y el id del modelo,
- el **tipo de petición**: `generation` (una parte nueva, un swipe, una regeneración), `annotate`, `summary`, `impersonate`,
  `redraft`, `agent`, `help`, o el nombre del tipo de trabajo propio de una extensión,
- el estado (running, completed, cancelled, failed) y cuánto tardó.

Elige una petición para leerla en las pestañas bajo la lista: **Request** (cada mensaje bajo su rol), **Reasoning**
y **Response** (la respuesta, las funciones que llamó el modelo y el error de una petición fallida). **Refresh**
recarga la lista y **Clear** la vacía. Una petición aparece en la lista en cuanto se envía: mientras se ejecuta, su respuesta y su razonamiento crecen en el diálogo a medida que el modelo los escribe.

![El historial de las peticiones](../../images/plugin-prompt-history.png)

Para un [agente](../agents.md), el pie de su consola tiene el botón **Prompt history**: abre la misma lista con las
peticiones de esa ejecución.

<a id="notes"></a>

## Notas

- El historial se guarda solo en memoria, comprimido para ocupar poco espacio. Se pierde al cerrar la sesión o al reiniciar el servidor, y no forma parte de una copia de seguridad.
- Cada sesión del navegador tiene su propia lista. Las peticiones de un agente no están en ella: se guardan por ejecución (20 por ejecución, para las 10 últimas ejecuciones) y se muestran desde la consola de la ejecución.
- Los mensajes se muestran completos, incluido el texto de la historia.
