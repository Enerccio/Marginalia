---
label: Google Gemini
order: 14
---

# Google Gemini

Añade los modelos Gemini de Google como [proveedor de inferencia](../inference-providers.md), a través de la API propia de Google en lugar de una compatible con OpenAI.

Google también ofrece un endpoint compatible con OpenAI, que funciona con el proveedor normal *Compatible con OpenAI*. Esta extensión se comunica directamente con la API de Gemini (el SDK de Google Gen AI), lo que aporta lo que el endpoint compatible no ofrece: los **resúmenes del razonamiento del modelo** mostrados como razonamiento, el nivel de pensamiento de Gemini 3 y el presupuesto de pensamiento de los modelos anteriores, los filtros de seguridad y los recuentos exactos de tokens que informa Google.

!!!warning Tu texto de historia se envía a Google
Cada petición de una IA que usa este proveedor (el prompt con la historia, las entradas del lorebook y los resúmenes, las respuestas del modelo, los archivos e imágenes que envía un agente o un comando) se envía a los servidores de Google (`generativelanguage.googleapis.com`, o el proxy que configures). Las condiciones de Google y su política de privacidad para la API de Gemini determinan qué ocurre con ese texto allí (retención, si se usa para mejorar los modelos; esto depende del plan de tu clave de API). Los demás proveedores envían al servidor que elijas, que puede ser el tuyo propio. No uses este proveedor para texto que no puedas enviar a Google. En un servidor compartido con otros usuarios, un administrador que comparta una IA de este proveedor envía a Google el texto de historia de todos los que la usen.
!!!

<a id="installing"></a>

## Instalación

Carga la extensión en *Admin → Extensiones*, como se describe en [Gestionar extensiones](../administration/extensions.md).
El JAR es grande (unos 14 MB): contiene el SDK de Google y las bibliotecas que solo necesita el SDK. Nada más en Marginalia
usa esas bibliotecas.

Una vez cargada, la lista **Tipo** del diálogo del proveedor de inferencia tiene una nueva entrada, **Extensión**, y la lista **Proveedor**
en la parte superior de la pestaña *Ajustes por tipo* ofrece **Google Gemini** (se elige por ti mientras sea el único). Sin una extensión cargada que proporcione IAs, **Extensión** no aparece.

<a id="adding-a-gemini-provider"></a>

## Añadir un proveedor de Gemini

1. Obtén una clave de API en [Google AI Studio](https://aistudio.google.com/apikey).
2. En la pestaña **Proveedores de inferencia**, haz clic en **Añadir proveedor de inferencia**, escribe un **Nombre**, elige el tipo **Extensión** y
   el proveedor **Google Gemini**.
3. Rellena los *Ajustes generales* (tamaño de contexto, tamaño de respuesta, [razonamiento](#reasoning)) y los ajustes del
   proveedor en la pestaña *Ajustes por tipo*, y después haz clic en **Probar la conexión**.

![Los ajustes generales de un proveedor de tipo Extensión](../../images/plugin-googlegenai-general.png)

![Los ajustes por tipo del proveedor Google Gemini](../../images/plugin-googlegenai.png)

El proveedor aparece después en la lista como cualquier otro, con el nombre del proveedor en la columna *Tipo*:

![La lista de proveedores de inferencia con un proveedor Google Gemini](../../images/plugin-googlegenai-list.png)

| Campo | |
|---|---|
| **Clave de API** | La clave de Google AI Studio. Se guarda cifrada y no se vuelve a mostrar; al editar el proveedor, haz clic en **Restablecimiento** para introducir otra. |
| **Modelo** | El modelo a usar, p. ej. `gemini-2.5-flash` o `gemini-3-pro-preview`. **Actualizar** lista los modelos que tu clave puede usar para escribir texto; puedes escribir un nombre que no esté en la lista. |
| **URL** | Opcional. Si está vacío se usa la API de Google (`generativelanguage.googleapis.com`); configúrala para enviar las peticiones a través de un proxy. |
| **Tiempo de espera (segundos)** | Cuánto esperar a que Google responda, y a más texto una vez que empezó a escribir. El valor por defecto es 300. |
| **Reintentos** | Cuántas veces se repite una petición cuando Google responde con un límite de uso (429) o un error del servidor (5xx), o no se puede contactar. El valor por defecto es 2; 0 desactiva los reintentos. |
| **Desactivar los filtros de seguridad** | Envía `BLOCK_NONE` para las categorías de seguridad ajustables (acoso, discurso de odio, contenido sexual explícito, contenido peligroso). La ficción a menudo choca con los filtros. Google bloquea igualmente algún contenido, pase lo que pase. |

**Probar la conexión** lista los modelos y pide al elegido unos pocos tokens, con los valores del diálogo (antes de guardarlos).

<a id="reasoning"></a>

## Razonamiento

Los ajustes de razonamiento son los de los ajustes generales del proveedor, como en cualquier otro proveedor:

- Con **Razonamiento habilitado** desactivado, el modelo piensa como lo hace por defecto y su pensamiento no se muestra.
- Con él activado, el modelo envía los resúmenes de su pensamiento y se muestran como el razonamiento de la parte de la historia. **Esfuerzo de razonamiento** fija cuánto piensa: en los modelos Gemini 3 es el nivel de pensamiento (bajo, medio, alto); en los modelos anteriores, un presupuesto de 1 024, 8 192 o 24 576 tokens de pensamiento. **Ninguno** equivale a desactivado.

Los tokens de pensamiento cuentan como tokens generados y se toman del tamaño de respuesta que fijaste: si el modelo lo gasta todo en pensar, la parte de la historia vuelve vacía y el error indica que se agotó el tamaño de respuesta. Sube **Máximo de tokens de respuesta** o baja el esfuerzo.

<a id="what-works"></a>

## Lo que funciona

Todo lo que necesita una historia funciona igual que con un proveedor compatible con OpenAI: streaming, detener una generación, resúmenes, [extensiones que llaman al modelo](index.md), funciones que el modelo puede invocar (el [Generador de nombres](name-generator.md) ofrece algunas), e imágenes y archivos adjuntos a un mensaje. Los tokens que usa un usuario se cuentan en las columnas **Tokens enviados** y **Tokens generados** a partir de las cifras que informa Google, y se aplican los [límites](../inference-providers.md#limits-of-shared-providers) de un proveedor compartido.

Los recuentos de tokens que se muestran en la historia (el tamaño de una parte, el prompt) los calcula Google (la petición `countTokens` del modelo), una vez por cada texto: las respuestas se recuerdan. Cuando no se puede consultar a Google (sin clave, sin red, un error), Marginalia estima el recuento por sí misma durante un minuto y después vuelve a intentar con Google; un texto que se guarda o edita mientras otro guardado está en curso también se estima. La estimación difiere un poco de lo que factura Google.

<a id="when-the-extension-is-not-loaded"></a>

## Cuando la extensión no está cargada

Los proveedores que creaste permanecen en la lista cuando la extensión se descarga, con el tipo *Extensión: google-genai*, y vuelven a funcionar cuando la extensión se carga. Mientras no esté cargada, un libro que use uno de esos proveedores no puede generar: el error te indica que cargues la extensión o elijas otro proveedor. El proveedor sigue pudiendo abrirse y sus ajustes generales cambiarse; los ajustes de Gemini solo se muestran cuando la extensión está cargada, y no se tocan.

<a id="not-supported"></a>

## No soportado

- **Vertex AI** (Google Cloud con un proyecto y credenciales). Solo se admiten claves de API de la API de Gemini.
- Generación de imágenes y otros tipos de salida de Gemini: solo se escribe texto.
