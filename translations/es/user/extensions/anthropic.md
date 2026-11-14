---
label: Anthropic Claude
order: 25
---

# Anthropic Claude

Añade los modelos Claude de Anthropic como [proveedor de inferencia](../inference-providers.md), a través de la API propia de Anthropic en lugar de una compatible con OpenAI.

Claude también es accesible mediante agregadores como OpenRouter con el proveedor normal *Compatible con OpenAI*. Esta extensión se comunica directamente con la API de Claude (el SDK de Anthropic), lo que ofrece cosas que un endpoint compatible no ofrece: los **resúmenes del pensamiento del modelo** mostrados como razonamiento, el esfuerzo de los modelos Claude 4.6 y posteriores, y los recuentos exactos de tokens que informa y cuenta Anthropic.

!!!warning Tu texto de la historia se envía a Anthropic
Cada solicitud de una IA que usa este proveedor (el prompt con la historia, las entradas del lorebook y los resúmenes, las respuestas del modelo, los archivos e imágenes que un agente o un comando envía) se envía a los servidores de Anthropic (`api.anthropic.com`, o el proxy que hayas configurado). Las condiciones y la política de privacidad de la API de Anthropic determinan qué ocurre con ella allí (conservación, salvaguardas que pueden rechazar una solicitud). Los otros proveedores envían al servidor que elegiste, que puede ser el tuyo propio. No uses este proveedor para texto que no puedas enviar a Anthropic. En un servidor compartido con otros usuarios, un administrador que comparte una IA de este proveedor envía a Anthropic el texto de la historia de todos los que la usan.
!!!

<a id="installing"></a>

## Instalación

Carga la extensión en *Admin → Extensiones*, como se describe en [Gestión de extensiones](../administration/extensions.md). El JAR es grande (unos 36 MB): contiene el SDK de Anthropic y las bibliotecas que solo necesita el SDK. Nada más en Marginalia usa estas bibliotecas.

Una vez cargada, la lista **Tipo** del diálogo del proveedor de inferencia tiene una entrada nueva, **Extensión**, y la lista **Proveedor** en la parte superior de la pestaña *Ajustes por tipo* ofrece **Anthropic Claude** (se elige por ti mientras sea el único). Sin una extensión cargada que proporcione IAs, **Extensión** no se ofrece.

<a id="adding-a-claude-provider"></a>

## Añadir un proveedor de Claude

1. Obtén una clave de API en la [Consola de Claude](https://platform.claude.com).
2. En la pestaña **Proveedores de inferencia** haz clic en **Añadir proveedor de inferencia**, introduce un **Nombre**, elige el tipo **Extensión** y el proveedor **Anthropic Claude**.
3. Rellena los *Ajustes generales* (tamaño del contexto, tamaño de la respuesta, [razonamiento](#reasoning)) y los ajustes del proveedor en la pestaña *Ajustes por tipo*, y después haz clic en **Probar la conexión**.

| Campo | |
|---|---|
| **Clave de API** | La clave de la Consola de Claude. Se guarda cifrada y no se vuelve a mostrar; al editar el proveedor, haz clic en **Restablecimiento** para introducir otra. |
| **Modelo** | El modelo que se usará, por ejemplo `claude-opus-5-5` o `claude-sonnet-5-5`. **Actualizar** lista los modelos que puede usar tu clave; puedes escribir un nombre que no esté en la lista. |
| **URL** | Opcional. Si está vacío se usa la API de Anthropic (`api.anthropic.com`); indícala para enviar las solicitudes a través de un proxy. |
| **Tiempo de espera (segundos)** | Cuánto esperar a que Anthropic responda, y a más texto una vez que empieza a escribir. El valor por defecto es 300. |
| **Reintentos** | Cuántas veces se repite una solicitud cuando Anthropic responde con un límite de uso (429) o un error del servidor (5xx, también 529 *overloaded*), o no se puede contactar con él. El valor por defecto es 2; 0 desactiva los reintentos. |

**Probar la conexión** lista los modelos y pide al modelo elegido unos pocos tokens, con los valores del diálogo (antes de guardarlos).

![Los ajustes por tipo del proveedor Anthropic Claude](../../images/plugin-anthropic.png)

![La lista de proveedores de inferencia con un proveedor de Claude](../../images/plugin-anthropic-list.png)

<a id="reasoning"></a>

## Razonamiento

Los ajustes de razonamiento son los ajustes generales del proveedor, como en cualquier proveedor:

- Con **Razonamiento habilitado** desactivado no se envía nada sobre el pensamiento. Los modelos que piensan por defecto (Opus 5.5 no se puede configurar para que no lo haga) sí piensan, y su pensamiento no se muestra; los demás no piensan.
- Con él activado, el modelo envía resúmenes de su pensamiento y se muestran como el razonamiento de la parte de la historia. Para Claude 4.6 y posteriores, el **Esfuerzo de razonamiento** es el esfuerzo del modelo (bajo, medio, alto) con pensamiento adaptativo: el modelo decide cuánto pensar. Para los modelos anteriores (Claude 4.5 y anteriores, entre ellos Haiku 4.5) es un presupuesto de 1 024, 8 192 o 24 576 tokens de pensamiento. **Ninguno** equivale a desactivado.

Los tokens de pensamiento cuentan como tokens generados y se toman del tamaño de respuesta que hayas configurado: si el modelo lo usa todo para pensar, la parte de la historia llega vacía y el error indica que el tamaño de la respuesta se agotó. Aumenta **Máximo de tokens de respuesta** o reduce el esfuerzo. Claude necesita un tamaño de respuesta en cada solicitud: cuando ni el proveedor ni el protocolo lo establecen, se usan 16 000.

<a id="what-works"></a>

## Qué funciona

Todo lo que necesita una historia funciona igual que con un proveedor compatible con OpenAI: streaming, detener una generación, resúmenes, [extensiones que llaman al modelo](index.md), funciones que el modelo puede invocar (el [Generador de nombres](name-generator.md) ofrece algunas) e imágenes (JPEG, PNG, GIF, WebP), archivos PDF y de texto adjuntos a un mensaje. Los tokens que usa un usuario se cuentan en las columnas **Tokens enviados** y **Tokens generados** a partir de los números que informa Anthropic (los tokens leídos de la caché y los escritos en ella cuentan como enviados), y se aplican los [límites](../inference-providers.md#limits-of-shared-providers) de un proveedor compartido.

Los recuentos de tokens que se muestran en la historia (el tamaño de una parte, el prompt) los calcula Anthropic (la solicitud `count_tokens` del modelo), una vez por cada texto: las respuestas se recuerdan. Cuando no se puede consultar a Anthropic (sin clave, sin red, un error), Marginalia estima el recuento por sí mismo durante un minuto y después vuelve a intentarlo; un texto que se guarda o edita mientras hay otro guardado en curso también se estima.

Hay dos cosas que difieren de otros proveedores:

- **La temperatura, el top p y las penalizaciones del protocolo no se envían.** La API actual de Claude las rechaza; el modelo muestrea con su propia configuración. Se usa el tamaño de respuesta del protocolo.
- Una conversación tiene que empezar y terminar con un mensaje del usuario, y Claude no continúa una respuesta. El prompt de un libro siempre lo cumple; una solicitud de una extensión que no lo cumpla se rechaza con un error de "bad request".

<a id="when-the-extension-is-not-loaded"></a>

## Cuando la extensión no está cargada

Los proveedores que creaste siguen en la lista cuando la extensión se descarga, con el tipo *Extensión: anthropic*, y vuelven a funcionar cuando la extensión se carga. Mientras no esté cargada, un libro que use uno de esos proveedores no puede generar: el error te indica que cargues la extensión o elijas otro proveedor. El proveedor se puede seguir abriendo y sus ajustes generales cambiarse; los ajustes de Claude solo se muestran cuando la extensión está cargada, y no se modifican.

<a id="not-supported"></a>

## No soportado

- **Amazon Bedrock, Google Vertex AI y Microsoft Foundry.** Solo se admiten claves de API de la API de Claude.
- La caché de prompts y las herramientas que se ejecutan en los servidores de Anthropic (búsqueda web, ejecución de código).
