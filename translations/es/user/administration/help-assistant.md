---
label: Asistente de ayuda
order: 60
---

# Asistente de ayuda

El asistente de ayuda es un chat que responde a preguntas sobre Marginalia a partir de su manual. Un administrador elige qué
modelo usa y concede el permiso **Asistente de ayuda** a los usuarios que pueden consultarlo, en sus
[permisos](permissions.md) (los administradores siempre pueden); no necesitan un modelo propio. Los usuarios que existían
cuando se introdujeron los permisos lo obtuvieron si el asistente estaba activado en ese momento; los usuarios nuevos no tienen ninguno.

Cuando un usuario puede usarlo, ve un botón redondo con un robot en la esquina inferior derecha de la ventana. Al pulsarlo se abre el chat; el
panel indica qué modelo responde y avisa cuando se aplica el límite del usuario (véase [Quién paga](#who-pays)). El modelo busca las respuestas en el manual (puede buscar en él y leer sus páginas), indica las páginas que ha usado como enlaces al
[manual publicado](https://enerccio.github.io/Marginalia/) (se abren en una pestaña nueva) y explica. No cambia nada y no ve los libros del usuario salvo que el usuario lo permita (véase [Tus libros](#your-books)). También puede revisar qué ha fallado cuando algo da error (véase
[Cuando algo sale mal](#when-something-goes-wrong)) y llevar al usuario a otro lugar, si usted lo permite (véase
[Indicar el camino](#showing-the-way)). La conversación se conserva hasta que el usuario cierra la sesión (cerrar el panel
o recargar la página no la borra); el botón de la escoba la borra.

![El asistente de ayuda respondiendo a una pregunta](../../images/help-assistant.png)

El botón aparece cuando un usuario abre Marginalia. El modelo y el aviso de límite se leen cada vez que se abre el panel;
si retira el permiso o elimina el modelo, los usuarios que ya tienen la página abierta pierden el botón al recargarla
(o al abrir el panel).

<a id="settings"></a>

## Ajustes

Abra **Admin → Asistente de ayuda**:

![La pestaña Asistente de ayuda](../../images/admin-help-assistant.png)

| Ajuste | |
|---|---|
| **Modelo** | Uno de *sus propios* [modelos](../inference-providers.md), compartido con los usuarios o no. No aparecen en la lista los modelos que comparten otros administradores. |
| **Permitir leer los datos de administración** | Permite al asistente leer el estado de las extensiones y sus registros, las copias de seguridad de la base de datos y los demás datos de la página Admin *cuando un administrador se lo pide*; véase [Datos de administración](#administration-data). Desactivado por defecto. |
| **Prompt para administradores**, **Prompt para el resto de usuarios** | Las instrucciones que el modelo recibe con cada pregunta; véase [Prompts](#prompts). |

Dos ajustes dependen del usuario y están en su permiso **Asistente de ayuda** [permiso](permissions.md#settings-of-a-permission) (se abre con el
botón de la llave en **Admin → Usuarios**; ambos están desactivados por defecto):

| Ajuste del permiso | |
|---|---|
| **Aplicar límites** | Solo para un modelo compartido; véase [Quién paga](#who-pays). |
| **Permitir navegar por la interfaz** | Permite al asistente abrir una sección, seleccionar una pestaña o abrir el diálogo para añadir algo cuando un usuario le pide que le muestre dónde está algo; véase [Indicar el camino](#showing-the-way). |

A un administrador que no tenga ese permiso se le responde con ambos desactivados.

Los ajustes se guardan con **Guardar**. El asistente pide la respuesta al modelo sin un [protocolo](../protocols.md), así que se aplican los ajustes del propio modelo. Deja de funcionar por sí solo cuando se elimina el modelo
o cuando su propietario deja de ser administrador, de modo que un modelo que pertenece a un usuario normal nunca se entrega a
todo el mundo.

<a id="what-the-assistant-sees-of-the-screen"></a>

## Lo que el asistente ve de la pantalla

El modelo puede ver lo que el usuario está haciendo en ese momento (se le indica cuando una pregunta trata de «aquí» o «esto»): qué
sección, pestaña o diálogo está abierto, con una breve descripción de cada parte de la pantalla; por ejemplo, el título del
libro abierto y su pestaña abierta, o el título de un diálogo abierto. Las pestañas que no están abiertas solo aparecen en una lista, y se leen
únicamente si la pregunta lo necesita. De un formulario abierto recibe también lo que falla en ese momento: los campos que el formulario marca como
no válidos con el mensaje que muestra, los campos obligatorios vacíos y lo que el formulario comprueba entre campos (en un modelo: la respuesta debe
ser menor que el contexto). Solo recibe los *nombres* de los campos, nunca lo que se escribe en ellos. Nunca recibe el texto de una historia, de una entrada del libro de referencia, una clave de API ni una contraseña.

![El asistente explicando por qué no se puede guardar todavía el formulario de un modelo nuevo](../../images/help-assistant-form.png)

<a id="your-books"></a>

## Tus libros

El panel del asistente tiene la casilla **Permitir que el asistente consulte mis libros**, desactivada por defecto y que cada usuario activa
para sí mismo (se guarda en sus ajustes). Un aviso debajo explica qué significa: el asistente lee los nombres de las carpetas, los títulos,
las descripciones y los ajustes de escritura de los libros y busca en su texto, nunca lee el texto de una historia, y lo que lee se
envía a la IA que responde, que es el modelo que ha elegido más arriba; avise de ello a los usuarios si es un servicio que usted no controla.

![El panel del asistente con la casilla para los libros](../../images/help-assistant-books.png)

Cuando está activada, el modelo recibe tres funciones, todas de solo lectura y solo para los libros del usuario que pregunta:

| Función | |
|---|---|
| `list_folder` | Las subcarpetas de una carpeta, con el número de libros que contiene cada una, y sus libros con título y descripción. Una ruta como `Sci-fi/Expanse` indica la carpeta; sin ruta, el nivel superior. |
| `search_books` | Encuentra libros por las palabras del título o de la descripción, o por el texto de la historia, opcionalmente dentro de una carpeta («¿dónde está mi libro sobre amantes verdaderos?»). Indica dónde estaba la coincidencia y en qué carpeta; el texto encontrado no se devuelve. |
| `get_book_settings` | El idioma, el punto de vista, el tiempo verbal y el estilo de un libro, y de dónde procede cada uno (el libro, una carpeta, los ajustes del usuario o el valor predeterminado integrado). De los prompts solo se indica que son *un texto personalizado* o *el texto integrado*, con su procedencia. |

No puede cambiar, mover ni crear nada. Las listas se recortan (cien entradas, veinte coincidencias) y las descripciones se acortan.

<a id="when-something-goes-wrong"></a>

## Cuando algo sale mal

Cuando un usuario dice que algo ha fallado («me sale un error», «no se guarda», «el modelo no responde»), el
asistente puede leer lo que Marginalia le mostró: los últimos errores, las peticiones fallidas al proveedor del modelo (clave incorrecta, modelo inexistente, límite alcanzado, tiempo de espera agotado...), las advertencias y los mensajes de los formularios que no se pudieron guardar, del más reciente al más antiguo y con el tiempo transcurrido desde cada uno. Explica lo que significa el mensaje y qué corregir, y, en un formulario abierto, indica qué campos cambiar.

Lo que se registra, y dónde:

* todo lo que se muestra al usuario como error o advertencia en una notificación, y el mensaje de una validación fallida, se guarda
  durante la sesión (los últimos 20);
* los fallos inesperados (los que muestran un diálogo con la traza de la pila) y las peticiones fallidas a un proveedor también se guardan en la pestaña del navegador
  (los últimos tres, en su almacenamiento de sesión), así que siguen ahí tras recargar la página o tras caducar la sesión.

El asistente recibe el texto que vio el usuario y, en caso de fallo, los nombres y mensajes de las excepciones, sin la traza de la pila.
Cada texto se corta a 400 caracteres y se elimina lo que parece una clave, un token o una contraseña, pero el texto de un mensaje
puede seguir conteniendo una parte de lo que un proveedor o el sistema respondió, por ejemplo la respuesta de un proveedor a una petición. El modelo que responde es el que usted eligió, que puede
ser un proveedor externo: téngalo en cuenta al [elegirlo](#settings).
<a id="showing-the-way"></a>

## Guiar por la interfaz

Con **Permitir navegar por la interfaz** activado en el permiso del usuario, el asistente puede hacer algunas cosas en la propia ventana cuando el usuario se lo pide
("muéstrame dónde están los lorebooks", "llévame a las copias de seguridad", "¿cómo añado un modelo?"): abrir una sección (Books, Lorebooks, Settings, ...), la página Admin
(solo la tienen los administradores), seleccionar una pestaña de Settings, de la página Admin o del editor de la historia, o pulsar el botón que
abre el diálogo para añadir un libro, un modelo o un protocolo (el diálogo solo se abre, no se crea nada hasta que el usuario lo rellena y lo confirma; no se ofrece mientras hay otro diálogo abierto). El asistente le indica al usuario lo que ha hecho.
No puede hacer nada más: no pulsa otros botones, no escribe, no guarda, no borra, no genera, no cierra un diálogo ni cierra la sesión, y un usuario que
no puede abrir algo tampoco puede hacer que el asistente lo abra por él. No se mueve por iniciativa propia, solo cuando se lo piden.
Cuando la navegación está bloqueada (la sección abierta tiene cambios sin guardar, o se está generando un libro), no puede moverse.

![El asistente abrió la pestaña Database Backups a petición del usuario](../../images/help-assistant-navigate.png)

Permitirlo también le indica al modelo los nombres de los elementos de la pantalla y lo que ofrecen; es el mismo tipo de información que en [Lo que el asistente ve de la pantalla](#what-the-assistant-sees-of-the-screen).

<a id="administration-data"></a>

## Datos de administración

Con **Permitir leer los datos de administración** activado, el asistente puede leer los datos de la página Admin para un administrador que se lo pregunte
("¿por qué no funciona Side Query?", "¿cuándo fue la última copia de seguridad?"), y para nadie más; un usuario normal
nunca los recibe, independientemente de la configuración, y se vuelve a comprobar cada vez que se leen.

| Tema | Qué lee |
|---|---|
| Extensions | Cada [extensión](../extensions/index.md) instalada con su versión, si está activa, si la has detenido tú o si no ha podido arrancar, y el resultado de su verificación |
| El registro de una extensión | Las últimas líneas de su registro (40 por defecto, 200 como máximo), igual que **Admin → Extensions → Log** |
| Users | Cuántos usuarios hay, cuántos son administradores y cuántos están bloqueados; no muestra nombres |
| Database backups | El tamaño de la base de datos, cuántas copias de seguridad hay y cuándo se hizo la más reciente, la programación y si hay una restauración pendiente |
| Cleanup | Lo que eliminaría una [limpieza](cleanup.md) (no se elimina nada) y lo que la bloquea |
| Application | La versión de Java, la memoria en uso y cuánto tiempo lleva en marcha la aplicación |

Es de solo lectura: el asistente no puede iniciar, detener ni eliminar nada allí. Ten en cuenta que todo esto se envía al modelo que
responde, que puede ser un proveedor externo, y que el registro de una extensión puede contener texto que la extensión escribió (una
línea del registro se corta a 400 caracteres y se eliminan las claves, los tokens y las contraseñas, pero no se hace nada más con ella). Por eso el ajuste está desactivado hasta que
lo actives.

## Prompts

Hay dos prompts: uno para los administradores (que pueden preguntar por la página Admin) y otro para el resto (a quienes se les
indica que la administración es cosa de los administradores). Ambos se rellenan con los valores por defecto; cambia el texto y guarda, y **Reset
to default** restaura el valor por defecto. Un prompt que está vacío o es igual al valor por defecto se guarda como «el valor por defecto», así que
mejora con una nueva versión de Marginalia; si lo has cambiado, es tuyo y tendrás que mantenerlo actualizado.

`{{manualIndex}}` en un prompt se sustituye por la lista de las páginas del manual (ruta, título y de qué trata cada página), que es como
el modelo sabe qué puede leer. Si lo omites, el modelo solo encuentra páginas mediante búsqueda. Mantén las instrucciones para consultar
el manual y para indicar cuando no cubre una pregunta; de lo contrario, las respuestas serán menos fiables. Las reglas
de las herramientas que leen errores, se desplazan por la interfaz y leen los datos de administración las añade Marginalia al prompt
(no tienes que mantenerlas tú), y solo para las herramientas que están permitidas.

<a id="who-pays"></a>

## Quién paga

Los usuarios nunca ven el modelo ni su clave de API; sus preguntas pasan por la aplicación.

| Modelo | **Aplicar límites** (permiso del usuario) | Qué ocurre |
|---|---|---|
| Compartido | activado | Una pregunta es una petición como cualquier otra al modelo compartido: se comprueba el [límite](../inference-providers.md#limits-of-shared-providers) que has fijado para el usuario y los tokens se suman al consumo del usuario. A un usuario que ha alcanzado su límite (o que no puede usar el modelo) no se le responde. |
| Compartido | desactivado | Paga el propietario del modelo. El límite del usuario se ignora y no se cuenta nada para el usuario. |
| No compartido | (sin efecto) | Paga el propietario del modelo: sin límite, y no se cuenta nada para el usuario. El modelo solo lo usa el asistente. |

Tus propias preguntas cuentan del mismo modo; para ti, el modelo es uno más.
