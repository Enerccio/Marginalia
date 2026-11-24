---
label: xAI Grok
order: 26
---

# xAI Grok

Añade los modelos Grok de xAI como un [proveedor de inferencia](../inference-providers.md), a través de la API propia de xAI.

Grok también es accesible con el proveedor normal *OpenAI Compatible* y la URL `https://api.x.ai/v1`. Esta extensión se comunica directamente con la API de xAI, lo que permite mostrar el razonamiento como tal, los recuentos exactos de tokens que informa xAI (incluidos los tokens de razonamiento) y una lista de modelos y una prueba de conexión hechas para este servicio.

!!!warning Tu texto de historia se envía a xAI
Cada solicitud de una IA que usa este proveedor (el prompt con la historia, las entradas del lorebook y los resúmenes, las respuestas del modelo, las imágenes que envía un agente o un comando) se envía a los servidores de xAI (`api.x.ai`, o al proxy que hayas configurado). Las condiciones de uso y la política de privacidad de xAI para la API determinan lo que ocurre allí con ellas. Los demás proveedores envían al servidor que hayas elegido, que puede ser el tuyo propio. No uses este proveedor para texto que no puedas enviar a xAI. En un servidor compartido con otros usuarios, un administrador que comparta una IA de este proveedor envía a xAI el texto de la historia de todos los que la usen.
!!!

<a id="installing"></a>

## Instalación

Carga la extensión en *Admin → Extensions*, como se describe en [Gestión de extensiones](../administration/extensions.md). Una vez cargada, la lista **Type** del diálogo del proveedor de inferencia tiene la entrada **Extension**, y la lista **Provider** en la parte superior de la pestaña *Per Type Settings* ofrece **xAI Grok**.

<a id="adding-a-grok-provider"></a>

## Añadir un proveedor Grok

1. Obtén una clave de API en la [xAI Console](https://console.x.ai).
2. En la pestaña **Inference Providers** haz clic en **Add Inference Provider**, escribe un **Name**, elige el tipo **Extension** y el proveedor **xAI Grok**.
3. Rellena los *General Settings* (tamaño del contexto, tamaño de la respuesta, [razonamiento](#reasoning)) y los ajustes del proveedor en la pestaña *Per Type Settings*, y luego haz clic en **Test Connection**.

![Un proveedor Grok](../../images/plugin-grok.png)

![El proveedor Grok en la lista](../../images/plugin-grok-list.png)

| Field | |
|---|---|
| **Api Key** | La clave de la xAI Console. Se guarda cifrada y no se vuelve a mostrar; al editar el proveedor, haz clic en **Reset** para introducir otra. |
| **Model** | El modelo a usar, p. ej. `grok-4` o `grok-3-mini`. **Refresh** muestra los modelos que tu clave puede usar; puedes escribir un nombre que no esté en la lista. |
| **URL** | Opcional. Si está vacío usa la API de xAI (`https://api.x.ai/v1`); indícala para enviar las solicitudes a través de un proxy. |
| **Timeout (seconds)** | Cuánto esperar a que xAI responda, y cuánto esperar más texto una vez que empieza a escribir. El valor predeterminado es 300. |
| **Retries** | Cuántas veces se repite una solicitud cuando xAI responde con un límite de peticiones (429) o un error del servidor (5xx), o cuando no se puede contactar con él. El valor predeterminado es 2; 0 desactiva los reintentos. |
| **Send the reasoning effort** | *Automatic* envía el esfuerzo solo a `grok-3-mini`, *Always* a todos los modelos, *Never* a ninguno. |

**Test Connection** lista los modelos y pide al elegido unos pocos tokens, con los valores del diálogo (antes de guardarlos).

<a id="reasoning"></a>

## Razonamiento

Los ajustes de razonamiento son los de los ajustes generales del proveedor, como en cualquier otro proveedor. Con el razonamiento activado, el razonamiento que muestra un modelo llega como razonamiento de la parte de la historia. El esfuerzo solo se envía a los modelos que lo aceptan (`grok-3-mini`: *low*, o *high* para medium y high); los demás modelos Grok razonan como lo hacen por defecto y rechazan el parámetro.

<a id="what-works"></a>

## Qué funciona

El streaming, la detención, los esquemas JSON, las funciones que el modelo puede llamar, las imágenes (JPEG, PNG) y los archivos de texto como adjuntos, los límites de las IA compartidas y el recuento de uso funcionan igual que con el proveedor compatible con OpenAI. El uso es el que informa xAI; los tokens de razonamiento cuentan como tokens generados. La temperatura, top p y las penalizaciones del [protocolo](../protocols.md) se envían cuando están activadas; algunos modelos de razonamiento rechazan las penalizaciones. No se admiten las herramientas del lado del servidor de xAI (búsqueda en vivo, ejecución de código) ni la generación de imágenes o vídeo.
