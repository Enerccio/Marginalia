---
label: Proveedores de inferencia
order: 980
icon: server
---

# Proveedores de inferencia

Un proveedor de inferencia es una conexión a un modelo: la dirección de la API, la clave de API, el modelo y sus límites. Cada libro usa un proveedor. Puedes tener tantos como quieras, por ejemplo un modelo grande en la nube para la historia y un modelo pequeño local para experimentos, y cambiar un libro entre ellos en cualquier momento.

Los proveedores pertenecen a tu cuenta. Otros usuarios no los ven ni ven sus claves de API. Las claves de API se guardan cifradas (consulta [La carpeta de datos](administration/index.md#the-data-folder)).

Añadir un proveedor requiere el permiso **Crear modelos propios**, que te concede un administrador (los administradores siempre lo tienen, consulta [Permisos](administration/permissions.md)). Sin él solo puedes usar los proveedores que los administradores [comparten](#shared-providers) contigo; el botón **Añadir proveedor de inferencia** no aparece.

![La pestaña de proveedores de inferencia](../images/inference-providers-list.png)

<a id="supported-apis"></a>

## APIs compatibles

Marginalia funciona con cualquier API que implemente la interfaz **OpenAI Chat Completions** (`/v1/chat/completions`, con streaming). La mayoría de los servicios y servidores locales lo hacen:

| Servicio | URL de OpenAI |
|---|---|
| OpenAI | `https://api.openai.com/v1` |
| OpenRouter | `https://openrouter.ai/api/v1` |
| LiteLLM proxy | `http://localhost:4000/v1` |
| llama.cpp (`llama-server`) | `http://localhost:8080/v1` |
| LM Studio | `http://localhost:1234/v1` |
| Ollama | `http://localhost:11434/v1` |
| vLLM | `http://localhost:8000/v1` |

Los puertos son los predeterminados de cada servidor; usa los que tu servidor utiliza realmente.

!!!warning Puerto 8080
La versión Docker de Marginalia también usa el puerto 8080. Si ejecutas llama.cpp en la misma máquina, inícialo en otro puerto (`llama-server --port 8081`).
!!!

!!!
Con la [versión Docker](getting-started/docker-server.md), `localhost` se refiere al contenedor de Marginalia. Usa `http://host.docker.internal:<puerto>/v1` para un servidor de modelos en la misma máquina, consulta [Modelos en la misma máquina](getting-started/docker-server.md#models-on-the-same-machine).
!!!
<a id="adding-a-provider"></a>

## Añadir un proveedor

Abre la pestaña **Proveedores de inferencia** y haz clic en **Añadir proveedor de inferencia**. Los proveedores existentes se editan con el botón del lápiz y se eliminan con el botón de la papelera. El botón de clonar (dos hojas) hace una copia de un proveedor con un nombre que confirmas (el original con *(Copy)*): la copia tiene la misma dirección, modelo, **Clave de API** (o los ajustes de la extensión), razonamiento, límites de tokens y parámetros, así que un segundo modelo en el mismo servicio solo necesita un nombre y un clic. La copia no se comparte y no tiene límites ni uso propios. Solo puedes clonar tus propios proveedores (no uno compartido por un administrador, cuya clave no debes ver) y únicamente cuando tienes permiso para crear proveedores.

En la parte superior del diálogo:

| Campo | |
|---|---|
| **Nombre** | Tu nombre para el proveedor, que se muestra cuando eliges un modelo para un libro. Incluye el modelo, por ejemplo `OpenRouter - Claude Sonnet`. |
| **Tipo** | El tipo de API: **Compatible con OpenAI**, o **Extensión** para un proveedor que aporta una [extensión](extensions/index.md) (solo se ofrece mientras esa extensión esté cargada, por ejemplo [Google Gemini](extensions/google-genai.md) o [Anthropic Claude](extensions/anthropic.md)). El tipo no se puede cambiar una vez creado el proveedor. |
| **Proveedor** | Solo para el tipo **Extensión**: de qué extensión es este proveedor. Sus propios ajustes rellenan la pestaña *Ajustes por tipo*. |

<a id="per-type-settings"></a>

### Ajustes por tipo

![Ajustes por tipo con la lista de modelos cargada](../images/inference-provider.png)

La pestaña es más alta que el diálogo y se desplaza. Los campos inferiores:

![Tiempo de espera, reintentos, parámetros adicionales y Probar la conexión](../images/inference-provider-2.png)

| Campo | |
|---|---|
| **URL de OpenAI (termina en /v1)** | La dirección base de la API, véase [la tabla de arriba](#supported-apis). Sin `/chat/completions`. |
| **Clave de API** | La clave del servicio. Un servidor local normalmente no la comprueba: deja el campo vacío y Marginalia envía el marcador `none`. |
| **Modelo** | El modelo que se usará. Haz clic en **Actualizar** para cargar la lista de modelos desde la API, o escribe tú el ID del modelo (por ejemplo `anthropic/claude-sonnet-4.5` en OpenRouter). |
| **Tiempo de espera (segundos)** | Cuánto tiempo se espera a que la API responda, y a más texto una vez que empezó a escribir. El valor predeterminado es 300. Los modelos de razonamiento pueden necesitar minutos antes del primer texto; una petición que no recibe nada durante este tiempo falla con un mensaje de tiempo agotado (el razonamiento de un modelo cuenta como texto, así que un modelo que sigue razonando no se corta). Un cambio lo usa la siguiente petición, también la de un agente que esté trabajando. |
| **Reintentos** | Cuántas veces se repite una petición cuando la API responde con un límite de uso (429) o un error del servidor (5xx), o cuando no se puede contactar con ella. La espera entre intentos crece, y se respeta la cabecera `Retry-After` de la API. El valor predeterminado es 2; 0 desactiva los reintentos. Solo se reintenta el inicio de una petición: el texto que ya llegó nunca se escribe dos veces. |
| **Tokenizador** | Cómo cuenta Marginalia los tokens de los textos para este proveedor. **Automático** (el predeterminado) prueba uno tras otro los endpoints de tokenización del servidor (OpenAI, llama.cpp, LiteLLM) y usa el primero que responda, y una estimación local si ninguno responde. Elige uno para usar solo ese, o **LOCAL_TOKKIT** para la estimación local, que nunca llama al servidor (útil cuando contar haría que el servidor cargara otro modelo). Un tokenizador de un plugin también aparece aquí; recibe la dirección, el modelo y el texto, nunca la clave de API, y solo cuando lo eliges. **Probar tokenizador** cuenta un texto de muestra con lo que el proveedor probaría y muestra qué respondió cada uno. Al crear un proveedor, se preselecciona un tokenizador de plugin que encaje con la dirección, hasta que elijas tú. |
| **Reenviar el razonamiento** | Si el modelo recibe de vuelta su propio razonamiento en la siguiente petición, véase [Reenviar el razonamiento](#send-the-thinking-back). Solo para proveedores compatibles con OpenAI: los proveedores de Anthropic y Google envían por sí mismos lo que sus API necesitan. |
| **Parámetros adicionales** | Un objeto JSON con campos extra para cada petición a este proveedor, por ejemplo `{"top_k": 40, "min_p": 0.05}` para un servidor local, o `{"provider": {"order": ["anthropic"]}}` para OpenRouter. Un campo definido aquí gana al mismo ajuste del [protocolo](protocols.md) (por ejemplo `temperature`) o al límite de respuesta (`max_completion_tokens`). Debe ser un objeto JSON válido o estar vacío. |

Al editar un proveedor existente, la clave de API no se vuelve a mostrar. Se conserva tal cual, salvo que hagas clic en **Restablecer la clave de API** e introduzcas una nueva.

**Actualizar** pide a la API su lista de modelos (`/v1/models`) con la URL y la clave del formulario. Si falla con *Failed to download model list*, revisa la URL (debe terminar en `/v1`), la clave y que el servidor esté en marcha. Algunos servicios no listan modelos; escribe entonces el ID del modelo.

**Probar la conexión** comprueba los valores del formulario sin guardarlos: pide a la API su lista de modelos y luego una respuesta de un solo token del **Modelo** seleccionado, sin reintentos. Un mensaje de éxito significa que la URL, la clave y el modelo funcionan juntos. Un fallo indica la causa (véase [Mensajes de error](#error-messages)). Algunos servicios no tienen lista de modelos; en ese caso la prueba solo necesita la respuesta de un token.

<a id="general-settings"></a>

### Ajustes generales

![Ajustes generales de un proveedor de inferencia](../images/inference-provider-general.png)

| Campo | |
|---|---|
| **Tamaño máximo del contexto** | Obligatorio. Cuántos tokens acepta el modelo en total, entre prompt y respuesta. |
| **Máximo de tokens de respuesta** | Obligatorio. La respuesta más larga que puede escribir el modelo. Se envía a la API como `max_completion_tokens`, y esa cantidad del contexto se reserva para la respuesta. Debe ser menor que *Tamaño máximo del contexto*. |
| **Conexiones** | Cuántas peticiones pueden estar en curso hacia este modelo a la vez: todas, de todos los usuarios y de todo tipo (tu generación, una consulta auxiliar, una traducción, una anotación, [agentes](agents.md#one-model-several-agents), plugins). Las demás esperan su turno, en el orden en que llegaron; una generación que tiene que esperar muestra *Waiting for the model: N requests before this one*, y **Detener** la saca de la cola. **1** (el predeterminado) para un servidor local con una sola ranura (una GPU); un modelo en la nube que responde varias peticiones a la vez puede tener más, para que una consulta auxiliar se ejecute junto a una generación. |
| **Enviar las imágenes del lore** | Envía junto con su texto las [imágenes de las entradas del lorebook](lorebooks/lorebooks.md#editing-in-full-screen) que usa un turno. Desactivado por defecto: actívalo solo para un modelo que acepte imágenes; un modelo sin visión rechaza la petición y el turno falla. Las imágenes de las entradas del bloque de lore van en un mensaje propio justo después del mensaje del sistema (con una respuesta breve del modelo), las demás con tu prompt. |
| **Imágenes en el mensaje del sistema** | (Compatible con OpenAI, con *Enviar las imágenes del lore*) Pone las imágenes del bloque de lore en el propio mensaje del sistema, junto al texto del lore. Solo para un servidor y una plantilla de chat que acepten imágenes ahí; la mayoría no lo hace y la petición falla. |
| **Necesita jailbreak**, **Prompt de jailbreak** | Un prompt extra para modelos que se niegan a escribir determinado contenido. Si está marcado, el prompt de jailbreak se coloca al principio absoluto del prompt. |
| **Activar el razonamiento**, **Esfuerzo de razonamiento** | Para modelos de razonamiento ("thinking"), véase [Modelos de razonamiento](#reasoning-models). |

Marginalia reserva para la respuesta los **Máximo de tokens de respuesta** del contexto y rellena el resto con el prompt: las plantillas, las entradas del [lorebook](lorebooks/index.md) y los [resúmenes](books/summaries.md) se cuentan dentro, y las partes más antiguas de la historia que no caben se dejan fuera. Con un contexto de 32 000 tokens y respuestas de hasta 2 000 tokens, el prompt dispone de 30 000 tokens.

Toma el **Tamaño máximo del contexto** de la documentación del servicio, y ponlo un poco más bajo: los recuentos de tokens no siempre son exactos, y una petición que supera el límite real falla. Un [protocolo](protocols.md) puede bajar ambos límites para algunos libros, pero no subirlos.
<a id="error-messages"></a>

## Mensajes de error

Cuando la API rechaza una petición, Marginalia indica qué hay que corregir en lugar de mostrar un error interno, seguido de lo que la propia API respondió:

![Un error del proveedor mostrado al usuario](../images/error-dialog.png)

| Mensaje | Causa |
|---|---|
| *The provider rejected the API key.* | **Clave de API** incorrecta, caducada o ausente. |
| *The API key is not allowed to use this model or endpoint.* | La clave es válida, pero no tiene acceso al modelo. |
| *The provider does not know the configured model.* | **Modelo** incorrecto; usa **Actualizar** para ver los nombres que conoce la API. |
| *Nothing was found at the configured URL.* | **OpenAI URL** incorrecta; normalmente termina en `/v1`. |
| *The prompt does not fit into the context of the model.* | **Tamaño máximo del contexto** es mayor de lo que el modelo acepta de verdad. Redúcelo. |
| *The provider is limiting requests.* | Límite de peticiones o cuota agotada, incluso después de los reintentos. |
| *The provider failed to process the request.* | El servicio tiene un problema (5xx), incluso después de los reintentos. |
| *The provider did not answer in time.* | Aumenta **Tiempo de espera (segundos)** o revisa el servidor. |
| *Cannot connect to the provider.* | El servidor no está en marcha o Marginalia no puede alcanzarlo, o la URL tiene un error tipográfico. |
| *The provider rejected the request.* | Otro rechazo, a menudo un campo no admitido en **Parámetros adicionales** o un razonamiento que el modelo no soporta. |

<a id="warning-before-sending"></a>

## Aviso antes de enviar

Marginalia llena el contexto solo hasta **Tamaño máximo del contexto** menos **Máximo de tokens de respuesta**. Si el prompt terminado sigue siendo más grande que eso (por ejemplo, porque una extensión añadió texto, o el recuento de tokens no fue exacto), aparece un aviso con los tamaños estimados antes de enviar la petición. La petición se envía igualmente; la API puede rechazarla o recortar el prompt.

![El aviso sobre un prompt que puede no caber](../images/context-warning.png)

<a id="reasoning-models"></a>

## Modelos de razonamiento

Los modelos de razonamiento piensan antes de responder. Con **Activar el razonamiento** marcado, Marginalia envía el **Esfuerzo de razonamiento** elegido (*Ninguno*, *Bajo*, *Medio* o *Alto*) a la API como `reasoning_effort`. Más esfuerzo suele significar un texto mejor planificado, pero respuestas más lentas y más caras. No todas las API admiten este parámetro; si las peticiones fallan con el razonamiento activado, desactívalo.

Cuando el modelo devuelve su razonamiento (como `reasoning_content` o `reasoning`, según el servidor), se muestra encima de la parte generada, en **Ver el razonamiento**. El razonamiento no forma parte de la historia y no se reenvía al modelo, salvo que actives **Reenviar el razonamiento** (más abajo).

<a id="send-the-thinking-back"></a>

### Reenviar el razonamiento

**Reenviar el razonamiento** (en *Ajustes por tipo* de un proveedor compatible con OpenAI) indica si el razonamiento del modelo se le devuelve, como `reasoning_content` de su mensaje anterior, en la siguiente petición:

| Valor | |
|---|---|
| **Ninguno** (predeterminado) | Nunca. Es lo adecuado para la mayoría de modelos y servidores, y para las API que rechazan un campo desconocido. |
| **Entre llamadas a herramientas** | Solo mientras continúen las llamadas a herramientas de una misma respuesta: el modelo que llamó a una herramienta recibe su propio razonamiento junto con el resultado. Un mensaje nuevo tuyo empieza sin el razonamiento anterior. Esto es lo que pide Google para Gemma 4 (*thoughts must not be removed between the function calls*, pero eliminados entre turnos). |
| **Siempre** | También en las respuestas anteriores que tienen razonamiento, cuando el chat lo conserva (el asistente de trama del Esquema del arco lo hace). Un servidor cuya plantilla descarta por sí misma el razonamiento antiguo, como la de Gemma 4, lo ignora. |

Se aplica al bucle de herramientas (el asistente de trama, los generadores que usan herramientas) y a los agentes: un agente recibe el razonamiento de sus rondas anteriores mientras continúen las llamadas a herramientas de un mismo turno (el razonamiento se guarda con el mensaje en la consola, no en el mensaje almacenado). El servidor tiene que aceptar el campo: llama.cpp lo acepta (lee `reasoning_content`, no `reasoning`), también detrás de LiteLLM; un proveedor que rechaza campos desconocidos en los mensajes falla, en ese caso deja la opción en *Ninguno*.

![Razonamiento de una parte generada](../images/reasoning-view.png)

El razonamiento cuenta dentro de la respuesta: da a los modelos de razonamiento un **Máximo de tokens de respuesta** mayor, o el texto puede cortarse después de un razonamiento largo.

<a id="token-counting"></a>

## Recuento de tokens

Marginalia cuenta tokens para decidir cuánta parte de la historia cabe en el prompt. Pregunta al proveedor cuando puede y prueba estos métodos en orden:

1. el endpoint de recuento de tokens de OpenAI (`/v1/responses/input_tokens`),
2. el endpoint `/tokenize` de llama.cpp,
3. el endpoint `/tokenize` de LiteLLM,
4. el endpoint `/v1/messages/count_tokens` de LiteLLM (para modelos de Anthropic),
5. una estimación local con el tokenizador de OpenAI.

Se usa desde entonces el primer método que funciona. La estimación local es cercana para la mayoría de modelos, pero no exacta, y es otra razón para mantener el **Tamaño máximo del contexto** un poco por debajo del límite real. Los proveedores del tipo *Extensión* cuentan según indique su extensión; los de [Google Gemini](extensions/google-genai.md) y [Anthropic Claude](extensions/anthropic.md) piden al proveedor que cuente.

<a id="prompt-cache"></a>

## Caché del prompt

Cada petición que Marginalia hace para un tipo de trabajo (una parte nueva, una anotación, un rehacer, un resumen...) lleva la cabecera `X-Session-ID`, un hash que es el mismo para el mismo usuario y el mismo tipo. Un servidor o proxy que mantiene una caché del prompt puede usarla para encontrar la caché de la petición anterior de ese tipo. Un servidor que no la conoce la ignora. Consulta [Caché del prompt e id de sesión](prompt-cache.md), con un ejemplo para una ranura de llama.cpp guardada en disco.

<a id="shared-providers"></a>

## Proveedores compartidos

En una instalación con varios usuarios, un administrador puede **compartir** un proveedor: marca **Compartido con todos los usuarios** en el diálogo del proveedor (la casilla solo aparece para los administradores). Entonces, cada usuario lo encuentra en la lista **Modelo** de sus libros y en su pestaña **Proveedores de inferencia**, marcado como *Compartido* **Sí**. Pueden usarlo, pero no abrirlo, ver su dirección o su clave de API, cambiarlo ni borrarlo; solo su propietario puede hacerlo. Los libros, las copias de seguridad y las restauraciones que se refieren a un proveedor compartido siguen funcionando para todos los que lo pueden ver.

![Un proveedor compartido en la pestaña Proveedores de inferencia de un usuario normal](../images/provider-shared-user.png)

Compartir un proveedor permite a los demás usuarios gastar la cuota del propietario en el servicio. Para impedir que los usuarios añadan proveedores propios, de modo que solo puedan usar los compartidos, consulta [Usuarios y proveedores](administration/users.md#users-and-providers).
<a id="limits-of-shared-providers"></a>

## Límites de los proveedores compartidos

Un administrador puede limitar lo que cada usuario puede hacer con un proveedor compartido. En la pestaña **Proveedores de inferencia**, el administrador que es dueño de un proveedor compartido tiene un botón **Límites** (un usuario con un candado) en su fila, que abre una tabla con los límites establecidos. **Añadir límite** crea uno nuevo: se elige el usuario y un tipo de límite.

![Los límites de un proveedor compartido](../images/provider-limits.png)

| Tipo de límite | Significado |
|---|---|
| Sin límite | No hay límite (lo mismo que no tener ninguno). |
| Límite de tokens | El usuario puede usar hasta la cantidad indicada de tokens por periodo. |
| No disponible | El usuario no puede usar el proveedor; cualquier cosa que enviara una petición a él falla. |

Un **límite de tokens** tiene un **reinicio** (diario, semanal desde el lunes o mensual; los periodos son de calendario, en la zona horaria del servidor) y tres límites que pueden dejarse vacíos para no tener límite: los tokens **enviados**, los tokens **generados** y ambos juntos (**en total**). El que se alcance primero detiene al usuario hasta el siguiente reinicio. Si se establecen los tres, el límite de enviados más el de generados no puede ser inferior al total (el total nunca podría alcanzarse), o el diálogo se niega a guardar. Por ejemplo, un total de 100 000 con 100 000 enviados y 20 000 generados es correcto; un total de 100 000 con 20 000 enviados y 10 000 generados no lo es.

![Añadir un límite](../images/provider-limit-edit.png)

Nada se reinicia por un temporizador: los tokens usados empiezan desde cero cuando el usuario usa el proveedor (o consulta sus límites) por primera vez en un nuevo periodo. El tamaño de una petición se conoce antes de enviarla, así que una petición cuyo prompt llevaría al usuario por encima del límite de **enviados** o de **en total** se rechaza (el prompt se mide con la estimación propia de Marginalia, que puede diferir un poco del recuento del servicio). Los tokens **generados** no se conocen de antemano: se cuentan al terminar la petición, así que al usuario se le detiene solo cuando se alcanza el límite, y la última respuesta puede superarlo. Los usuarios sin un límite en la tabla no tienen límite.

Para evitar que los tokens generados superen el límite, marca **Hard limit** (solo disponible con un límite de tokens generados). Una petición reserva espacio para la respuesta más grande que permite (el **Max Response Size** del proveedor, o el menor de los dos del protocolo), y con un límite estricto se rechaza cuando esa reserva no cabe en lo que queda del límite de generados. Así, un límite estricto menor que el tamaño de respuesta del proveedor rechaza todas las peticiones; la tabla y la información del usuario muestran *estricto* junto a un límite así. El límite estricto solo afecta a los tokens generados: el límite **en total** aún puede superarse con la última respuesta.

Todos los usuarios ven los proveedores compartidos en su pestaña como filas de solo lectura. El botón **info** (el círculo con una *i*) de un proveedor compartido muestra su propio límite: lo que ha usado en este periodo, los límites y cuándo se reinician. Cuando se alcanza un límite, el editor de historias, los resúmenes y las extensiones muestran un error en lugar de enviar la petición.

![Los límites que ve un usuario, con lo usado en este periodo](../images/provider-limit-info.png)

<a id="usage"></a>

## Uso

La pestaña **Proveedores de inferencia** muestra, por cada proveedor, los **Tokens enviados** (el prompt, "prefill") y los **Tokens generados** (la respuesta, incluido el razonamiento) que has usado con él, sumados en todos tus libros, resúmenes y extensiones. Un proveedor compartido tiene un contador propio para cada usuario, así que solo ves tu parte.

Los administradores también tienen un botón **Uso por usuario** en la pestaña, que abre una tabla con lo que ha usado cada usuario de cada proveedor (tokens enviados, tokens generados, peticiones y la hora de la última).

![Uso por usuario](../images/provider-usage.png)

Las cifras son las que informa el servicio. Un servicio que no las informa (o una petición que detienes a mitad) se cuenta con la estimación local descrita en [Recuento de tokens](#token-counting), así que esas cifras son aproximadas. Una petición que falla antes de que llegue ningún texto no se cuenta. Salvo que un administrador haya establecido un [límite](#limits-of-shared-providers) para ti en un proveedor compartido, los recuentos son solo informativos.

!!!
Marginalia pide al servicio que informe de los recuentos de tokens (`stream_options` con `include_usage`). Para cambiarlo en un proveedor, añade tus propios `stream_options` en sus **Parámetros adicionales**, por ejemplo `{"stream_options": {"include_usage": false}}`: sustituye lo que envía Marginalia, y se usa la estimación local.
!!!

<a id="using-a-provider-in-a-book"></a>

## Usar un proveedor en un libro

Cada libro tiene su modelo en el campo **Modelo** de su pestaña **Información**. Para usar un proveedor en todos los libros nuevos, selecciónalo como **Modelo predeterminado** en la pestaña **Ajustes**. Cambiar el proveedor de un libro surte efecto con la siguiente parte generada; las partes escritas antes se mantienen como están.

<a id="troubleshooting"></a>

## Solución de problemas

| Problema | Qué revisar |
|---|---|
| *No se pudo descargar la lista de modelos* | La URL termina en `/v1`, la clave de API, que el servidor está en marcha y que Marginalia puede acceder a él. Escribe el ID del modelo si el servicio no tiene lista de modelos. |
| *El proveedor no respondió a tiempo.* | Aumenta el **Tiempo de espera (segundos)** del proveedor; los modelos de razonamiento pueden necesitar minutos. |
| *El proveedor está limitando las peticiones.* | Espera, o aumenta los **Reintentos**. |
| *Al libro le falta el modelo.* | El libro no tiene proveedor. Selecciona uno en **Información → Modelo**. |
| *El límite de contexto no es suficiente.* | El prompt sin ninguna historia no cabe en el contexto menos los tokens de respuesta. Aumenta el **Tamaño máximo del contexto** (o el **Máximo de tokens de contexto** del protocolo), reduce el **Máximo de tokens de respuesta**, o acorta las plantillas y las entradas del lorebook. |
| El texto se corta a mitad | El **Máximo de tokens de respuesta** (o el **Max Reply Tokens** del protocolo) es demasiado bajo, sobre todo en los modelos de razonamiento. |
| Errores de la API con el razonamiento activado | La API no admite `reasoning_effort`. Desactiva **Activar el razonamiento**. |
