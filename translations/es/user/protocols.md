---
label: Protocolos
order: 970
icon: sliders
---

# Protocolos

Un protocolo es un conjunto de ajustes de generación: los parámetros de muestreo (temperatura, top P y penalizaciones) y, de forma opcional, sus propios límites de contexto y de respuesta. El [proveedor de inferencia](inference-providers.md) indica *qué* modelo escribe; el protocolo indica *cómo*.

Los protocolos son independientes de los proveedores, así que puedes guardar varios, por ejemplo uno prudente y otro creativo, y cambiar un libro de uno a otro sin tocar la conexión. Como los proveedores, los protocolos pertenecen a tu cuenta; puedes [compartir](#sharing-a-protocol) uno para que otros usuarios importen una copia.

![La pestaña Protocolos](../images/protocols-list.png)

<a id="creating-a-protocol"></a>

## Crear un protocolo

Abre la pestaña **Protocolos** y haz clic en **Añadir protocolo**. Los protocolos existentes se editan con el botón del lápiz y se eliminan con el botón de la papelera. El botón de clonar (dos hojas) crea una copia de un protocolo, con todos sus ajustes, bajo un nombre que confirmas (el original con *(Copia)*). Es útil para probar otros ajustes de muestreo sin perder los que funcionan.

**Importar de otro usuario** (encima de la lista) muestra los protocolos que otros usuarios [comparten](#sharing-a-protocol) e importa una copia de uno de ellos.

![El diálogo del protocolo](../images/protocol-dialog.png)

| Campo | |
|---|---|
| **Nombre** | Tu nombre para el protocolo, por ejemplo `Creative` o `Long context`. |
| **Tipo** | El formato de la petición. Actualmente solo **Chat Completion**. |
| **Máximo de tokens de contexto** | Opcional. Total de tokens para el prompt y la respuesta de los libros que usan este protocolo. Si está vacío, se usa el **Tamaño máximo del contexto** del proveedor. |
| **Máximo de tokens de respuesta** | Opcional. La respuesta más larga. Si está vacío, se usa el **Máximo de tokens de respuesta** del proveedor. |
| **Temperatura** | Aleatoriedad del texto. Un valor bajo (alrededor de `0.7`) es más concentrado y predecible; uno alto (`1.0` o más) es más variado y sorprendente. |
| **Top P** | Muestreo por núcleo: el modelo elige solo entre las palabras más probables cuyas probabilidades suman este valor (`0`-`1`). `0.9`-`0.95` es una opción habitual. |
| **Penalización por frecuencia** | Penaliza las palabras según las veces que ya han aparecido, lo que reduce la repetición. Normalmente `0`-`1`. |
| **Penalización por presencia** | Penaliza las palabras que ya han aparecido, aunque sea una vez, lo que empuja al modelo hacia temas nuevos. Normalmente `0`-`1`. |
| **Compartir con otros usuarios** | Si está marcado, otros usuarios pueden ver el protocolo e importar una copia; consulta [más abajo](#sharing-a-protocol). Desactivado en un protocolo nuevo. |

Cada parámetro de muestreo tiene una casilla **Enable**. Un parámetro que no está activado no lo establece Marginalia, y el modelo usa su propio valor predeterminado. Activa solo los que quieras cambiar.

<a id="sharing-a-protocol"></a>

## Compartir un protocolo

Marca **Compartir con otros usuarios** en el diálogo del protocolo y todos los demás usuarios de esta instalación verán el protocolo en **Importar de otro usuario** (el botón junto a **Añadir protocolo**), con su nombre y su autor (tu nombre completo, o tu usuario de acceso si no tienes uno). Pueden abrir una fila para ver sus ajustes y hacer clic en **Importar**, lo que crea un protocolo propio con los mismos ajustes y nombre. Compartir es solo de lectura: nadie más puede cambiar, eliminar ni usar tu protocolo, y la copia es privada y no está conectada con la tuya; lo que uno de los dos cambie después no llega al otro. Si lo desmarcas, el protocolo desaparece de su lista; las copias que ya hicieron siguen siendo suyas.

<a id="limits"></a>

## Límites

Los límites del [proveedor de inferencia](inference-providers.md#general-settings) se aplican siempre. Un protocolo puede reducirlos, cada uno por separado, pero nunca superar los del proveedor:

| | Valor del protocolo definido | Valor del protocolo vacío |
|---|---|---|
| Contexto | **Máximo de tokens de contexto**, o el **Tamaño máximo del contexto** del proveedor si este es menor | **Tamaño máximo del contexto** del proveedor |
| Respuesta | **Máximo de tokens de respuesta**, o el **Máximo de tokens de respuesta** del proveedor si este es menor | **Máximo de tokens de respuesta** del proveedor |

Un protocolo con **Máximo de tokens de contexto** de 128 000 usado con un proveedor de 32 000 obtiene 32 000: el proveedor es lo que permite el servicio (o el administrador que lo comparte).

Marginalia reserva espacio libre para la respuesta y llena el resto del contexto con el prompt: plantillas, entradas del lorebook, resúmenes y todo lo que quepa de la historia. Con un contexto de 32 000 tokens y respuestas de hasta 2 000 tokens, el prompt tiene 30 000 tokens. El límite de respuesta también se envía a la API (`max_completion_tokens`).

Usa los límites del protocolo para que los prompts sean más cortos que lo que admite el modelo, por ejemplo para abaratar y acelerar libros largos con un modelo en la nube de contexto grande, o para acortar las respuestas en algunos libros. Los límites del protocolo también se usan para los [resúmenes](books/summaries.md) de los libros que usan este protocolo.

Deja ambos vacíos salvo que los necesites: se aplicarán entonces los valores del proveedor, y si cambias el proveedor de un libro, sus límites lo acompañan.

<a id="using-a-protocol-in-a-book"></a>

## Usar un protocolo en un libro

Cada libro tiene su protocolo en el campo **Protocolos** de su pestaña **Información**. Para usar un protocolo en todos los libros nuevos, selecciónalo como **Protocolo predeterminado** en la pestaña **Ajustes**. Un libro sin protocolo no puede generar y muestra *Al libro le falta el protocolo.*

<a id="suggested-settings"></a>

## Ajustes sugeridos

Puntos de partida; ajústalos para tu modelo:

| Uso | Temperatura | Top P | Penalización por frecuencia | Penalización por presencia |
|---|---|---|---|---|
| Prosa equilibrada | `0.8` | `0.95` | desactivada | desactivada |
| Creativo, variado | `1.0` | `0.95` | `0.3` | `0.3` |
| Coherente, cercano a las instrucciones | `0.6` | `0.9` | desactivada | desactivada |

Los modelos de razonamiento suelen ignorar o rechazar los parámetros de muestreo; déjalos desactivados para esos modelos.
