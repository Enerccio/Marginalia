---
label: Proveedores de inferencia
order: 980
icon: server
---

# Proveedores de inferencia

Un proveedor de inferencia es una conexión con un modelo: la dirección de la API, la clave de API, el modelo y sus límites. Cada libro usa un proveedor. Puedes tener todos los que quieras, por ejemplo un modelo grande en la nube para la historia y uno pequeño y local para experimentos, y cambiar un libro de uno a otro en cualquier momento.

Los proveedores pertenecen a tu cuenta. Otros usuarios no los ven ni ven sus claves de API. Las claves de API se guardan cifradas (véase [La carpeta de datos](administration/index.md#the-data-folder)).

Añadir un proveedor requiere el permiso **Create own models**, que te concede un administrador (los administradores siempre lo tienen, véase [Permisos](administration/permissions.md)). Sin él solo puedes usar los proveedores que los administradores te [comparten](#shared-providers); el botón **Add Inference Provider** no aparece.

![La pestaña Inference Providers](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## APIs compatibles

Marginalia funciona con cualquier API que implemente la interfaz **OpenAI Chat Completions** (`/v1/chat/completions`, con streaming). La mayoría de servicios y servidores locales lo hacen:

| Servicio | URL de OpenAI |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM proxy | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

Los puertos son los predeterminados de cada servidor; usa los que tu servidor realmente utiliza.

!!!warning Puerto 8080
La versión Docker de Marginalia también usa el puerto 8080. Si ejecutas llama.cpp en la misma máquina, inícialo en otro puerto (`llama-server --port 8081`).
!!!

!!!
Con la [versión Docker](getting-started/docker-server.md), `localhost` se refiere al contenedor de Marginalia. Usa `http://host.docker.internal:<port>/v1` para un servidor de modelos en la misma máquina, véase [Modelos en la misma máquina](getting-started/docker-server.md#models-on-the-same-machine).
!!!
<a id="adding-a-provider"></a>

## Añadir un proveedor

Abre la pestaña **Inference Providers** y haz clic en **Add Inference Provider**. Los proveedores existentes se editan con el botón del lápiz y se eliminan con el botón de la papelera. El botón de clonar (dos hojas) crea una copia del proveedor con un nombre que tú confirmas (el del original seguido de *(Copy)*). La copia tiene la misma dirección, modelo, **API key** (o los ajustes de la extensión), razonamiento, límites de tokens y parámetros, así que para usar un segundo modelo del mismo servicio basta con un nombre y un clic. La copia no se comparte y no tiene límites ni uso propios. Solo puedes clonar tus propios proveedores (no uno compartido de un administrador, cuya clave no debes ver) y solo cuando tengas permiso para crear proveedores.

En la parte superior del diálogo:

| Campo | |
|---|---|
| **Name** | Tu nombre para el proveedor, que se muestra al elegir un modelo para un libro. Incluye el modelo, p. ej. `OpenRouter - Claude Sonnet`. |
| **Type** | El tipo de API: **OpenAI Compatible**, o **Extension** para un proveedor que aporta una [extensión](extensions/index.md) (solo se ofrece mientras esa extensión está cargada, p. ej. [Google Gemini](extensions/google-genai.md) o [Anthropic Claude](extensions/anthropic.md)). El tipo no se puede cambiar después de crear el proveedor. |
| **Provider** | Solo para el tipo **Extension**: de qué extensión es este proveedor. Sus propios ajustes rellenan la pestaña *Per Type Settings*. |

### Per Type Settings

![Per Type Settings con la lista de modelos cargada](../images/inference-provider.png)

La pestaña es más alta que el diálogo y se desplaza. Los campos inferiores:

![Timeout, retries, additional parameters y Test Connection](../images/inference-provider-2.png)

| Campo | |
|---|---|
| **OpenAI URL (ends with /v1)** | La dirección base de la API, ver [la tabla de arriba](#supported-apis). Sin `/chat/completions`. |
| **Api Key** | La clave del servicio. Un servidor local normalmente no la comprueba: deja el campo vacío y Marginalia envía el marcador `none`. |
| **Model** | El modelo que se usará. Haz clic en **Refresh** para cargar la lista de modelos desde la API, o escribe tú el ID del modelo (p. ej. `anthropic/claude-sonnet-4.5` en OpenRouter). |
| **Timeout (seconds)** | Cuánto tiempo esperar a que la API responda, y a más texto una vez que empieza a escribir. El valor por defecto es 300. Los modelos de razonamiento pueden tardar minutos antes del primer texto; una petición que no recibe nada durante este tiempo falla con un mensaje de tiempo agotado (el razonamiento de un modelo cuenta como texto, así que un modelo que sigue pensando no se corta). Un cambio se usa en la siguiente petición, también la de un agente que esté trabajando. |
| **Retries** | Cuántas veces se repite una petición cuando la API responde con un límite de peticiones (429) o un error del servidor (5xx), o cuando no se puede contactar con ella. La espera entre intentos crece, y se respeta una cabecera `Retry-After` de la API. El valor por defecto es 2; 0 desactiva los reintentos. Solo se reintenta el inicio de una petición: el texto que ya llegó nunca se escribe dos veces. |
| **Tokenizer** | Cómo cuenta Marginalia los tokens de los textos para este proveedor. **Automatic** (el valor por defecto) prueba uno tras otro los endpoints de tokenización del servidor (OpenAI, llama.cpp, LiteLLM) y usa el primero que responda, y una estimación local si ninguno lo hace. Elige uno para usar solo ese, o **LOCAL_TOKKIT** para la estimación local, que nunca llama al servidor (útil cuando contar obliga al servidor a cargar otro modelo). Un tokenizador de un plugin también aparece aquí; recibe la dirección, el modelo y el texto, nunca la clave de la API, y solo cuando lo eliges. **Test tokenizer** cuenta un texto de muestra con lo que intentaría el proveedor y muestra lo que respondió cada uno. Al crear un proveedor, se preselecciona un tokenizador de plugin que encaje con la dirección, hasta que elijas tú. |
| **Send the thinking back** | Si el modelo recibe de vuelta su propio razonamiento en la siguiente petición, ver [Send the thinking back](#send-the-thinking-back). Solo para proveedores compatibles con OpenAI: los proveedores de Anthropic y Google envían por sí mismos lo que necesitan sus API. |
| **Additional Parameters** | Un objeto JSON con campos extra para cada petición a este proveedor, p. ej. `{"top_k": 40, "min_p": 0.05}` para un servidor local, o `{"provider": {"order": ["anthropic"]}}` para OpenRouter. Un campo definido aquí gana al mismo ajuste del [protocolo](protocols.md) (p. ej. `temperature`) o al límite de respuesta (`max_completion_tokens`). Debe ser un objeto JSON válido o estar vacío. |

Al editar un proveedor existente, la API key no se vuelve a mostrar. Se mantiene tal cual, a no ser que hagas clic en **Reset Api Key** e introduzcas una nueva.

**Refresh** pide a la API su lista de modelos (`/v1/models`) con la URL y la clave del formulario. Si falla con *Failed to download model list*, revisa la URL (debe terminar en `/v1`), la clave y que el servidor esté en marcha. Algunos servicios no listan los modelos; escribe entonces el ID del modelo.

**Test Connection** comprueba los valores del formulario sin guardarlos: pide a la API su lista de modelos y luego una respuesta de un solo token del **Model** seleccionado, sin reintentos. Un mensaje de éxito significa que la URL, la clave y el modelo funcionan juntos. Un fallo indica la causa (ver [Mensajes de error](#error-messages)). Algunos servicios no tienen lista de modelos; en ese caso la prueba solo necesita la respuesta de un token.

### General Settings

![General Settings de un proveedor de inferencia](../images/inference-provider-general.png)

| Campo | |
|---|---|
| **Max Context Size** | Obligatorio. Cuántos tokens acepta el modelo en total, entre prompt y respuesta. |
| **Max Response Tokens** | Obligatorio. La respuesta más larga que puede escribir el modelo. Se envía a la API como `max_completion_tokens`, y esta cantidad del contexto se reserva para la respuesta. Debe ser menor que *Max Context Size*. |
| **Connections** | Cuántas peticiones pueden estar en curso hacia este modelo a la vez: todas, de todos los usuarios y de cualquier tipo (tu generación, una consulta auxiliar, una traducción, una anotación, [agentes](agents.md#one-model-several-agents), plugins). Las demás esperan su turno, en el orden en que llegaron; una generación que tiene que esperar muestra *Waiting for the model: N requests before this one*, y **Stop** la saca de la cola. **1** (el valor por defecto) para un servidor local con un solo slot (una GPU); un modelo en la nube que responde varias peticiones a la vez puede tener más, para que una consulta auxiliar pueda correr junto a una generación. |
| **GPU lock** | Solo cuando el administrador ha configurado [GPUs](administration/gpus.md): el bloqueo de la GPU en la que se ejecuta este modelo, cuando la tarjeta se comparte con otros modelos o con servidores de imagen y voz. Cada petición espera entonces su turno en la GPU (contando las peticiones anteriores) y, si la GPU tiene cargado otra cosa, la libera y carga este modelo con los pasos del bloqueo primero. Vacío: sin bloqueo. |
| **Multimodal (accepts images)** | El modelo entiende imágenes, así que las [imágenes de las entradas del lorebook](lorebooks/lorebooks.md#editing-in-full-screen) que usa un turno se envían junto con su texto. Desactivado por defecto: actívalo solo para un modelo que acepte imágenes, porque un modelo sin visión rechaza la petición y el turno falla. Las imágenes de las entradas del bloque de lore van en un mensaje propio justo después del prompt del sistema (con una respuesta breve del modelo), y las demás con tu prompt. |
| **Image placement** | (con *Multimodal*) Dónde van las imágenes. *In the system message* (solo compatible con OpenAI): las imágenes del bloque de lore van dentro del propio mensaje del sistema, junto al texto del lore; solo para un servidor y una plantilla de chat que las acepten ahí, la mayoría no lo hace y la petición falla. *Before the text of the user message*: cada imagen va antes del texto de su mensaje, como lo quiere Gemma 4. *After the text of the user message* (el valor por defecto): después. El audio y los archivos siempre van después del texto. |
| **Function definitions** | Dónde lee el modelo las funciones que puede llamar (en [agentes](agents.md), consultas auxiliares y plugins que ofrecen funciones). *In the system message* (el valor por defecto): solo como las herramientas de la petición, que la plantilla de chat del modelo coloca en el turno del sistema. *Before* / *After the text of the user message*: además, escritas al principio o al final del último mensaje del usuario. Las herramientas se envían siempre, porque el servidor las necesita para reconocer una llamada. |
| **Function call rule**, **Rule placement** | Una regla que se añade a cada petición que ofrece funciones, para modelos que escriben una llamada como texto en su respuesta en lugar de hacerla, o que dejan de llamar. Por ejemplo: *You act only through function calls. Never write a call as text in your reply; to use a function, call it.* La ubicación indica dónde va: el mensaje del sistema, o el principio o el final del último mensaje del usuario (el valor por defecto); Gemma 4 la sigue mucho mejor en el mensaje del usuario. Vacío: sin regla. |
| **Needs Jailbreak**, **Jailbreak Prompt** | Un prompt extra para modelos que se niegan a escribir algunos contenidos. Si está marcado, el prompt de jailbreak se pone al principio absoluto del prompt. |
| **Enable Reasoning**, **Reasoning Effort** | Para modelos de razonamiento ("thinking"), ver [Modelos de razonamiento](#reasoning-models). |

Marginalia reserva **Max Response Tokens** del contexto para la respuesta y rellena el resto con el prompt: las plantillas, las entradas del [lorebook](lorebooks/index.md) y los [resúmenes](books/summaries.md) se cuentan dentro, y se dejan fuera las partes más antiguas de la historia que no caben. Con un contexto de 32 000 tokens y respuestas de hasta 2 000 tokens, el prompt dispone de 30 000 tokens.

Toma **Max Context Size** de la documentación del servicio, y ponlo un poco más bajo: los recuentos de tokens no siempre son exactos, y una petición que supera el límite real falla. Un [protocolo](protocols.md) puede reducir ambos límites para algunos libros, pero no aumentarlos.
<a id="error-messages"></a>

## Mensajes de error

Cuando la API rechaza una solicitud, Marginalia indica qué hay que corregir en lugar de mostrar un error interno, seguido de lo que respondió la propia API:

![Un error del proveedor mostrado al usuario](../images/error-dialog.png)

| Mensaje | Causa |
|---|---|
| *The provider rejected the API key.* | Clave **Api Key** incorrecta, caducada o ausente. |
| *The API key is not allowed to use this model or endpoint.* | La clave es válida, pero no tiene acceso al modelo. |
| *The provider does not know the configured model.* | **Model** incorrecto; usa **Refresh** para ver los nombres que conoce la API. |
| *Nothing was found at the configured URL.* | **OpenAI URL** incorrecto; normalmente termina en `/v1`. |
| *The prompt does not fit into the context of the model.* | **Max Context Size** es mayor de lo que el modelo acepta realmente. Redúcelo. |
| *The provider is limiting requests.* | Límite de peticiones o cuota agotada, incluso tras los reintentos. |
| *The provider failed to process the request.* | El servicio tiene un problema (5xx), incluso tras los reintentos. |
| *The provider did not answer in time.* | Aumenta **Timeout (seconds)** o revisa el servidor. |
| *Cannot connect to the provider.* | El servidor no está en marcha o no es accesible desde Marginalia, o la URL tiene una errata. |
| *The provider rejected the request.* | Otro rechazo, a menudo un campo no admitido en **Additional Parameters** o un razonamiento que el modelo no soporta. |

<a id="warning-before-sending"></a>

## Advertencia antes de enviar

Marginalia llena el contexto solo hasta **Max Context Size** menos **Max Response Tokens**. Si el prompt terminado sigue siendo mayor (por ejemplo, porque una extensión añadió texto o el conteo de tokens fue inexacto), antes de enviar la solicitud aparece una advertencia con los tamaños estimados. La solicitud se envía de todos modos; la API puede rechazarla o recortar el prompt.

![La advertencia sobre un prompt que quizá no quepa](../images/context-warning.png)

<a id="reasoning-models"></a>

## Modelos de razonamiento

Los modelos de razonamiento piensan antes de responder. Con **Enable Reasoning** marcado, Marginalia envía el **Reasoning Effort** elegido (*None*, *Low*, *Medium* o *High*) a la API como `reasoning_effort`. Más esfuerzo suele significar un texto mejor planificado, pero respuestas más lentas y más caras. No todas las API admiten este parámetro; si las solicitudes fallan con el razonamiento activado, desactívalo.

Cuando el modelo devuelve su razonamiento (como `reasoning_content` o `reasoning`, según el servidor), se muestra encima de la parte generada, en **View Reasoning**. El razonamiento no forma parte de la historia y no se devuelve al modelo, a menos que actives **Send the thinking back** (más abajo).

### Send the thinking back

**Send the thinking back** (en *Per Type Settings* de un proveedor compatible con OpenAI) indica si el razonamiento del modelo se le devuelve, como `reasoning_content` de su mensaje anterior, en la siguiente solicitud:

| Valor | |
|---|---|
| **None** (predeterminado) | Nunca. Adecuado para la mayoría de modelos y servidores, y para las API que rechazan un campo desconocido. |
| **Between tool calls** | Solo mientras continúan las llamadas a herramientas de una misma respuesta: el modelo que llamó a una herramienta recibe de vuelta su propio razonamiento junto con el resultado. Un mensaje nuevo tuyo empieza sin el razonamiento anterior. Es lo que Google pide para Gemma 4 (*los pensamientos no deben eliminarse entre las llamadas a funciones*, pero sí entre turnos). |
| **Always** | También con las respuestas anteriores que tienen razonamiento, cuando el chat lo conserva (lo hace el asistente de trama de Arc Outline). Un servidor cuya plantilla descarta por sí solo el razonamiento antiguo, como la de Gemma 4, lo ignora. |

Se aplica al bucle de herramientas (el asistente de trama, los generadores que usan herramientas) y a los agentes: un agente recupera el razonamiento de sus rondas anteriores mientras continúen las llamadas a herramientas de un mismo turno (el razonamiento se guarda con el mensaje en la consola, no en el mensaje almacenado). El servidor tiene que aceptar el campo: llama.cpp lo hace (lee `reasoning_content`, no `reasoning`), también detrás de LiteLLM. Un proveedor que rechaza campos desconocidos en los mensajes falla; en ese caso, deja la opción en *None*.

![Razonamiento de una parte generada](../images/reasoning-view.png)

El razonamiento cuenta para la respuesta: da a los modelos de razonamiento un **Max Response Tokens** mayor, o el texto puede cortarse tras un razonamiento largo.

<a id="token-counting"></a>

## Conteo de tokens

Marginalia cuenta tokens para decidir cuánta parte de la historia cabe en el prompt. Pide al proveedor que lo haga cuando puede, y prueba estos métodos en orden:

1. el endpoint de conteo de tokens de OpenAI (`/v1/responses/input_tokens`),
2. el endpoint `/tokenize` de llama.cpp,
3. el endpoint `/tokenize` de LiteLLM,
4. el endpoint `/v1/messages/count_tokens` de LiteLLM (para modelos de Anthropic),
5. una estimación local con el tokenizador de OpenAI.

Desde entonces se usa el primer método que funcione. La estimación local es cercana para la mayoría de modelos, pero no exacta, lo que es otra razón para mantener **Max Context Size** un poco por debajo del límite real. Los proveedores del tipo *Extension* cuentan según indique su extensión; los de [Google Gemini](extensions/google-genai.md) y [Anthropic Claude](extensions/anthropic.md) piden al proveedor que cuente.

<a id="prompt-cache"></a>

## Caché de prompts

Cada solicitud que Marginalia hace para un tipo de trabajo (una parte nueva, una anotación, una nueva redacción, un resumen...) lleva la cabecera `X-Session-ID`, un hash que es el mismo para el mismo usuario y tipo de trabajo. Un servidor o proxy que mantenga una caché de prompts puede usarlo para encontrar la caché de la solicitud anterior de ese tipo. Un servidor que no lo conoce lo ignora. Véase [Caché de prompts e identificador de sesión](prompt-cache.md), con un ejemplo para una ranura de llama.cpp guardada en disco.

<a id="shared-providers"></a>

## Proveedores compartidos

En una instalación con varios usuarios, un administrador puede **compartir** un proveedor: marca **Shared with all users** en el diálogo del proveedor (la casilla solo aparece para los administradores). Entonces, cada usuario lo encuentra en la lista **Model** de sus libros y en su pestaña **Inference Providers**, marcado como *Shared* **Yes**. Pueden usarlo, pero no abrirlo, ver su dirección o su clave de API, cambiarlo ni eliminarlo; solo su propietario puede hacerlo. Los libros, copias de seguridad y restauraciones que hacen referencia a un proveedor compartido siguen funcionando para todos los que lo ven.

![Un proveedor compartido en la pestaña Inference Providers de un usuario normal](../images/provider-shared-user.png)

Compartir un proveedor permite a los demás usuarios gastar la cuota del propietario en el servicio. Para impedir que los usuarios añadan sus propios proveedores, de modo que solo puedan usar los compartidos, véase [Usuarios y proveedores](administration/users.md#users-and-providers).
<a id="limits-of-shared-providers"></a>

## Límites de los proveedores compartidos

Un administrador puede limitar lo que cada usuario puede hacer con un proveedor compartido. En la pestaña **Inference Providers**, el administrador que es propietario de un proveedor compartido tiene un botón **Limits** (un usuario con un candado) en su fila, que abre una tabla con los límites establecidos. **Add limit** crea uno nuevo: elige el usuario y un tipo de límite.

![Los límites de un proveedor compartido](../images/provider-limits.png)

| Tipo de límite | Significado |
|---|---|
| Unlimited | Sin límite (lo mismo que no tener ninguno). |
| Token limit | El usuario puede usar hasta el número indicado de tokens por periodo. |
| Not available | El usuario no puede usar el proveedor; cualquier cosa que enviaría una solicitud a él falla. |

Un **token limit** tiene un **reset** (diario, semanal desde el lunes o mensual; los periodos son de calendario, en la zona horaria del servidor) y tres límites que pueden dejarse vacíos para no tener límite: los tokens **sent**, los tokens **generated** y ambos juntos (**in total**). El que se alcance primero detiene al usuario hasta el siguiente reinicio. Si se establecen los tres, la suma de los límites de **sent** y **generated** no puede ser menor que el de **in total** (de lo contrario, el total nunca se podría alcanzar); si no, el diálogo se niega a guardar. Por ejemplo, un total de 100 000 con 100 000 de **sent** y 20 000 de **generated** es correcto; un total de 100 000 con 20 000 de **sent** y 10 000 de **generated** no lo es.

![Añadir un límite](../images/provider-limit-edit.png)

Nada se reinicia con un temporizador: los tokens usados empiezan desde cero cuando el usuario usa el proveedor (o consulta sus límites) por primera vez en un nuevo periodo. El tamaño de una solicitud se conoce antes de enviarla, así que una solicitud cuyo prompt haría que el usuario superara el límite **sent** o **in total** se rechaza (el prompt se mide con la estimación propia de Marginalia, que puede diferir un poco del recuento del servicio). Los tokens **generated** no se conocen de antemano: se cuentan al terminar la solicitud, así que el usuario se detiene solo cuando se alcanza el límite, y la última respuesta puede superarlo. Los usuarios sin un límite en la tabla no tienen límite.

Para evitar que los tokens generados superen el límite, marca **Hard limit** (solo disponible con un límite de tokens generados). Una solicitud reserva espacio para la respuesta más grande que permite (el **Max Response Size** del proveedor, o el menor de los dos del protocolo), y con un límite estricto se rechaza cuando esa reserva no cabe en lo que queda del límite de generados. Por tanto, un límite estricto menor que el tamaño de respuesta del proveedor rechaza todas las solicitudes; la tabla y la información del usuario muestran *hard* junto a un límite así. El límite estricto solo cubre los tokens generados: el límite **in total** aún puede superarse con la última respuesta.

Cada usuario ve los proveedores compartidos en su pestaña como filas de solo lectura. El botón **info** (el círculo con una *i*) de un proveedor compartido muestra su propio límite: lo que usó en este periodo, los límites y cuándo se reinician. Cuando se alcanza un límite, el editor de la historia, los resúmenes y las extensiones muestran un error en lugar de enviar la solicitud.

![Los límites que ve un usuario, con lo usado en este periodo](../images/provider-limit-info.png)

<a id="usage"></a>

## Uso

La pestaña **Inference Providers** muestra, por proveedor, los **Tokens sent** (el prompt, o "prefill") y los **Tokens generated** (la respuesta, incluido el razonamiento) que has usado con él, sumados en todos tus libros, resúmenes y extensiones. Un proveedor compartido tiene un recuento propio para cada usuario, así que solo ves tu parte.

Los administradores también tienen un botón **Usage per user** en la pestaña, que abre una tabla con lo que cada usuario ha usado de cada proveedor (tokens enviados, tokens generados, solicitudes y la hora de la última).

![Uso por usuario](../images/provider-usage.png)

Las cifras son las que informa el servicio. Un servicio que no las informa (o una solicitud que detienes a mitad) se cuenta con la estimación local descrita en [Conteo de tokens](#token-counting), así que esas cifras son aproximadas. Una solicitud que falla antes de llegar cualquier texto no se cuenta. Salvo que un administrador haya establecido un [límite](#limits-of-shared-providers) para ti en un proveedor compartido, los recuentos son solo informativos.

!!!
Marginalia pide al servicio que informe los recuentos de tokens (`stream_options` con `include_usage`). Para cambiarlo en un
proveedor, pon tu propio `stream_options` en sus **Additional Parameters**, por ejemplo
`{"stream_options": {"include_usage": false}}`: sustituye lo que Marginalia envía, y se usa la estimación local.
!!!

<a id="using-a-provider-in-a-book"></a>

## Uso de un proveedor en un libro

Cada libro tiene su modelo en el campo **Model** de su pestaña **About**. Para usar un proveedor en todos los libros nuevos, selecciónalo como **Default Model** en la pestaña **Settings**. Cambiar el proveedor de un libro surte efecto con la siguiente parte generada; las partes escritas antes se mantienen como están.

<a id="troubleshooting"></a>

## Solución de problemas

| Problema | Qué revisar |
|---|---|
| *Failed to download model list* | Que la URL termine en `/v1`, la clave de API, que el servidor esté en marcha y sea accesible desde Marginalia. Escribe el ID del modelo si el servicio no tiene lista de modelos. |
| *The provider did not answer in time.* | Aumenta **Timeout (seconds)** del proveedor; los modelos de razonamiento pueden necesitar minutos. |
| *The provider is limiting requests.* | Espera, o aumenta **Retries**. |
| *Book is missing model.* | El libro no tiene proveedor. Selecciona uno en **About → Model**. |
| *Contextual limit not sufficient.* | El prompt sin ninguna historia no cabe en el contexto menos los tokens de respuesta. Aumenta **Max Context Size** (o **Max Context Tokens** del protocolo), reduce **Max Response Tokens**, o acorta las plantillas y las entradas del lorebook. |
| El texto se corta a mitad | **Max Response Tokens** (o **Max Reply Tokens** del protocolo) es demasiado bajo, sobre todo para modelos de razonamiento. |
| Errores de la API con el razonamiento activado | La API no admite `reasoning_effort`. Desactiva **Enable Reasoning**. |
