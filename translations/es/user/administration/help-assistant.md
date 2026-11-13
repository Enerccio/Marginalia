---
label: Asistente de ayuda
order: 60
---

# Asistente de ayuda

El asistente de ayuda es un chat que responde preguntas sobre Marginalia a partir de su manual. Un administrador elige qué modelo usa y concede el permiso **Asistente de ayuda** a los usuarios que pueden preguntarle, en sus [permisos](permissions.md) (los administradores siempre pueden); no necesitan un modelo propio. Los usuarios que existían cuando se introdujeron los permisos lo recibieron si el asistente estaba activado en ese momento; los usuarios nuevos no tienen ninguno.

Cuando un usuario puede usarlo, ve un botón redondo con un robot en la esquina inferior derecha de la ventana. Abre el chat; el panel indica qué modelo responde y avisa cuando se aplica el límite del usuario (véase [Quién paga](#who-pays)). El modelo busca las respuestas en el manual (puede buscar en él y leer sus páginas), cita las páginas que ha usado como enlaces al [manual publicado](https://enerccio.github.io/Marginalia/) (se abren en una pestaña nueva) y explica: no cambia nada y no ve los libros del usuario salvo que este lo permita (véase [Tus libros](#your-books)). También puede revisar qué ha fallado cuando algo falla (véase [Cuando algo sale mal](#when-something-goes-wrong)) y llevar al usuario a algún sitio, si lo permites (véase [Indicar el camino](#showing-the-way)). La conversación se conserva hasta que el usuario cierra la sesión (cerrar el panel o recargar la página no la borra); el botón de escoba la vacía. Lee el manual en el idioma de la interfaz del usuario cuando el manual está traducido a ese idioma, y los enlaces llevan al manual en ese idioma; una página que no está traducida se lee en inglés.

![El asistente de ayuda respondiendo una pregunta](../../images/help-assistant.png)

El botón aparece cuando un usuario abre Marginalia, y el modelo y el aviso de límite se leen cada vez que se abre el panel; si retiras el permiso o eliminas el modelo, los usuarios que ya tienen la página abierta pierden el botón al recargarla (o al abrir el panel).

<a id="settings"></a>

## Ajustes

Abre **Admin → Asistente de ayuda**:

![La pestaña Asistente de ayuda](../../images/admin-help-assistant.png)

| Ajuste | |
|---|---|
| **Modelo** | Uno de *tus propios* [modelos](../inference-providers.md), compartido o no con los usuarios. Los modelos que comparten otros administradores no aparecen en la lista. |
| **Permitir leer los datos de administración** | Permite que el asistente lea el estado de las extensiones y sus registros, las copias de seguridad de la base de datos y el resto de datos de la página Admin *para un administrador que se lo pida*, véase [Datos de administración](#administration-data). Desactivado por defecto. |
| **Prompt para administradores**, **Prompt para el resto de usuarios** | Las instrucciones que recibe el modelo con cada pregunta; véase [Prompts](#prompts). |

Dos ajustes dependen del usuario y están en su [permiso](permissions.md#settings-of-a-permission) **Asistente de ayuda** (ábrelo con el botón de llave en **Admin → Users**; ambos están desactivados por defecto):

| Ajuste del permiso | |
|---|---|
| **Aplicar límites** | Solo para un modelo compartido, véase [Quién paga](#who-pays). |
| **Permitir navegar por la interfaz** | Permite que el asistente abra una sección, seleccione una pestaña o abra el diálogo para añadir algo cuando un usuario le pide que le muestre dónde está algo, véase [Indicar el camino](#showing-the-way). |

A un administrador que no tenga ese permiso se le responde con ambos desactivados.

Los ajustes se guardan con **Guardar**. El asistente pide la respuesta al modelo sin un [protocolo](../protocols.md), así que se aplican los ajustes del propio modelo. Deja de funcionar por sí solo cuando se elimina el modelo o cuando su propietario deja de ser administrador, de modo que un modelo que pertenece a un usuario normal nunca se entrega a todos.
<a id="what-the-assistant-sees-of-the-screen"></a>

## Lo que el asistente ve de la pantalla

El modelo puede ver lo que el usuario está haciendo en ese momento (se le indica cuando una pregunta trata sobre «aquí» o «esto»): qué sección, pestaña o diálogo está abierto, como una breve descripción de cada parte de la pantalla; por ejemplo, el título del libro abierto y su pestaña abierta, o el título de un diálogo abierto. Las pestañas que no están abiertas solo aparecen en la lista, y el modelo las lee únicamente cuando la pregunta lo requiere. Para un formulario abierto también recibe qué está mal en ese momento: los campos que el formulario marca como no válidos con el mensaje que muestra, los campos obligatorios que están vacíos y lo que el formulario comprueba entre campos (en el caso de un modelo: la respuesta debe ser menor que el contexto). Solo recibe los *nombres* de los campos, nunca lo que se escribe en ellos. Nunca recibe el texto de una historia, de una entrada de un libro de consulta, una clave de API ni una contraseña.

![El asistente explicando por qué el formulario de un modelo nuevo todavía no se puede guardar](../../images/help-assistant-form.png)

<a id="your-books"></a>

## Tus libros

El panel del asistente tiene la casilla **Permitir que el asistente consulte mis libros**, desactivada por defecto y configurada por cada usuario para sí mismo (se guarda en sus ajustes). Un aviso debajo explica lo que significa: el asistente lee los nombres de las carpetas, los títulos, las descripciones y los ajustes de escritura de los libros, y busca en su texto, pero nunca lee el texto de una historia; y lo que lee se envía a la IA que responde, que es el modelo que elegiste más arriba, así que díselo a los usuarios si es un servicio que no controlas.

![El panel del asistente con la casilla para los libros](../../images/help-assistant-books.png)

Cuando está activada, el modelo recibe tres funciones, todas de solo lectura y solo para los libros del usuario que pregunta:

| Función | |
|---|---|
| `list_folder` | Las subcarpetas de una carpeta, con el número de libros que contiene cada una, y sus libros con título y descripción. Una ruta como `Sci-fi/Expanse` nombra la carpeta; si no se indica ninguna, se trata del nivel superior. |
| `search_books` | Encuentra libros por las palabras del título o de la descripción, o por el texto de la historia, opcionalmente dentro de una carpeta («¿dónde está mi libro sobre amantes verdaderos?»). Indica dónde estaba la coincidencia y en qué carpeta; el texto encontrado no se devuelve. |
| `get_book_settings` | El idioma, el punto de vista, el tiempo verbal y el estilo de un libro y de dónde procede cada uno (el libro, una carpeta, los ajustes del usuario o el valor predeterminado integrado). Los prompts solo se indican como *un texto personalizado* o *el texto integrado*, junto con su origen. |

No puede cambiar, mover ni crear nada. Las listas se recortan (cien entradas, veinte coincidencias) y las descripciones se acortan.
<a id="when-something-goes-wrong"></a>

## Cuando algo sale mal

Cuando un usuario dice que algo ha fallado («me sale un error», «no se guarda», «el modelo no responde»), el asistente
puede leer lo que Marginalia le mostró: los últimos errores, las solicitudes fallidas al proveedor del modelo (clave incorrecta, modelo
inexistente, límite alcanzado, tiempo de espera agotado...), los avisos y los mensajes de los formularios que no se pudieron
guardar, del más reciente al más antiguo y con el tiempo transcurrido desde cada uno. Explica qué significa el mensaje y qué
hay que corregir y, si hay un formulario abierto, indica qué campos cambiar.

Lo que se anota, y dónde:

* todo lo que se muestra al usuario como error o aviso en una notificación, y el mensaje de una validación fallida, se guarda
  durante la sesión (los últimos 20);
* los fallos inesperados (los que muestran un cuadro con la traza de la pila) y las solicitudes fallidas a un proveedor también
  los guarda la pestaña del navegador (los últimos tres, en su almacenamiento de sesión), así que siguen ahí tras recargar la
  página o tras caducar la sesión.

El asistente recibe el texto que vio el usuario y, en el caso de un fallo, los nombres y mensajes de las excepciones, sin la traza
de la pila. Cada texto se corta a 400 caracteres y se elimina lo que parece una clave, un token o una contraseña, pero el texto de un
mensaje puede seguir conteniendo una parte de lo que dijo el proveedor o el sistema, por ejemplo la respuesta de un proveedor a una
solicitud. El modelo que responde es el que tú elegiste, que puede ser un proveedor externo: ten esto en cuenta al [elegirlo](#settings).

<a id="showing-the-way"></a>

## Mostrar el camino

Con **Permitir navegar por la interfaz** activado en el permiso del usuario, el asistente puede hacer algunas cosas en la propia ventana cuando un usuario se lo pide
(«enséñame dónde están los lorebooks», «llévame a las copias de seguridad», «¿cómo añado un modelo?»): abrir una sección (Libros, Lorebooks, Ajustes, ...), la página de
Administración (solo la tienen los administradores), seleccionar una pestaña de los Ajustes, de la página de Administración o del editor de historias, o pulsar el botón que
abre el diálogo para añadir un libro, un modelo o un protocolo (el diálogo solo se abre: no se crea nada hasta que el usuario lo rellena y lo confirma; no se ofrece mientras hay otro diálogo abierto). Le dice al usuario lo que ha hecho.
No puede hacer nada más: no pulsa otros botones, no escribe, no guarda, no borra, no genera, no cierra un diálogo ni cierra la sesión, y un usuario que
no puede abrir algo no puede hacer que el asistente lo abra por él. No se mueve por iniciativa propia, solo cuando se le pide.
Cuando la navegación está bloqueada (la sección abierta tiene cambios sin guardar, un libro se está generando) no puede moverse.

![El asistente abrió la pestaña Copias de seguridad de la base de datos a petición del usuario](../../images/help-assistant-navigate.png)

Permitirlo también le indica al modelo los nombres de los elementos de la pantalla y lo que ofrecen; es el mismo tipo de información que en [Lo que el asistente ve de la pantalla](#what-the-assistant-sees-of-the-screen).
<a id="administration-data"></a>

## Datos de administración

Con **Permitir leer los datos de administración** activado, el asistente puede leer los datos de la página de Administración cuando un administrador se lo pide («¿por qué no funciona Side Query?», «¿cuándo fue la última copia de seguridad?»), y a nadie más; un usuario normal nunca los obtiene, sea cual sea el valor de la opción, y se vuelve a comprobar cada vez que se leen.

| Tema | Qué lee |
|---|---|
| Extensiones | Cada [extensión](../extensions/index.md) instalada con su versión, si está activa, si la has detenido tú o si no ha podido arrancar, y el resultado de su verificación |
| El registro de una extensión | Las últimas líneas de su registro (40 por defecto, 200 como máximo), lo mismo que **Administración → Extensiones → Registro** |
| Usuarios | Cuántos usuarios hay, cuántos son administradores y cuántos están bloqueados; no muestra nombres |
| Copias de seguridad de la base de datos | El tamaño de la base de datos, cuántas copias de seguridad hay y cuándo se hizo la más reciente, la programación y si hay una restauración pendiente |
| Limpieza | Lo que eliminaría una [limpieza](cleanup.md) (no se elimina nada) y lo que la bloquea |
| Aplicación | La versión de Java, la memoria en uso y cuánto tiempo lleva en marcha la aplicación |

Es solo de lectura: el asistente no puede iniciar, detener ni eliminar nada allí. Ten en cuenta que todo ello se envía al modelo que responde, que puede ser un proveedor externo, y que el registro de una extensión puede contener texto que la propia extensión escribió (cada línea de registro se corta a 400 caracteres y se eliminan las claves, tokens y contraseñas, pero no se hace nada más con ella). Por eso la opción está desactivada hasta que tú la actives.

## Prompts

Hay dos prompts: uno para los administradores (que pueden preguntar por la página de Administración) y otro para el resto de usuarios (a quienes se les indica que la administración es cosa de administradores). Ambos se rellenan con los valores por defecto; cambia el texto y guarda, y **Restablecer valor por defecto** vuelve a poner el valor por defecto. Un prompt que está vacío o es igual al valor por defecto se guarda como «el valor por defecto», así que mejora con cada nueva versión de Marginalia; uno que hayas modificado es tuyo y tendrás que mantenerlo al día.

`{{manualIndex}}` en un prompt se sustituye por la lista de las páginas del manual (ruta, título y de qué trata cada página), que es como el modelo sabe qué puede leer. Si lo omites, el modelo solo encuentra páginas buscando. Mantén las instrucciones para consultar el manual y para decirlo cuando el manual no cubra una pregunta; de lo contrario, las respuestas serán menos fiables. Las reglas de las herramientas que leen errores, se mueven por la interfaz y leen los datos de administración las añade Marginalia al prompt (no tienes que mantenerlas tú), y solo para las herramientas que están permitidas.
<a id="who-pays"></a>

## Quién paga

Los usuarios nunca ven el modelo ni su clave de API; sus preguntas pasan por la aplicación.

| Modelo | **Aplicar límites** (permiso del usuario) | Qué ocurre |
|---|---|---|
| Compartido | activado | Una pregunta es una petición como cualquier otra al modelo compartido: se comprueba el [límite](../inference-providers.md#limits-of-shared-providers) que has fijado para el usuario y los tokens se suman al consumo del usuario. Un usuario que ha alcanzado su límite (o que no puede usar el modelo) no recibe respuesta. |
| Compartido | desactivado | Paga el propietario del modelo. Se ignora el límite del usuario y no se cuenta nada para él. |
| No compartido | (sin efecto) | Paga el propietario del modelo: sin límite, y no se cuenta nada para el usuario. El modelo solo lo usa el asistente. |

Tus propias preguntas cuentan del mismo modo: para ti, el modelo es uno más.
