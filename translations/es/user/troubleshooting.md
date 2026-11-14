---
label: Solución de problemas y preguntas frecuentes
order: 900
icon: question
---

# Solución de problemas y preguntas frecuentes

<a id="where-are-the-logs"></a>

## ¿Dónde están los registros?

La mayoría de los problemas dejan rastro en el registro:

| Instalación | Registro |
|---|---|
| Aplicación de escritorio | `~/.marginalia/desktop/logs/marginalia.log` (`%USERPROFILE%\.marginalia\desktop\logs` en Windows); la ejecución anterior está en `marginalia.log.1` |
| Docker | `docker compose logs -f marginalia` |

Los cuadros de error de la aplicación (*Error interno del servidor*) también muestran el error; el registro contiene todos los detalles.

Al informar de un problema, incluye el texto de la versión de la esquina inferior izquierda de la página (por ejemplo
`Marginalia 1.5.0 (93ec118)`: la versión y el commit de Git de la compilación).

<a id="installing-and-starting"></a>

## Instalación y arranque

**macOS dice que la aplicación "está dañada" o que "no se puede abrir".**
La aplicación todavía no está firmada. Haz clic con el botón derecho sobre ella y elige *Abrir*, o ejecuta
`xattr -dr com.apple.quarantine /Applications/Marginalia.app`. Consulta [Aplicación de escritorio](getting-started/desktop-app.md).

**Windows SmartScreen bloquea la aplicación.**
Haz clic en *Más información* → *Ejecutar de todas formas*.

**El navegador abre un puerto distinto del 8765.**
El puerto 8765 estaba ocupado, así que la aplicación de escritorio eligió uno libre. Usa **Open Marginalia** en el menú de la bandeja, o inícialo con
`--port` para fijar el puerto.

**"Marginalia did not start in time".**
La aplicación de escritorio no pudo iniciar su servidor. El registro indica el motivo: a menudo, otro programa tiene bloqueados los archivos, o hay una extensión defectuosa (consulta [más abajo](#extensions)).

**Docker: el puerto 8080 ya está en uso.**
Cambia la parte izquierda de la asignación de puertos en `docker-compose.yml`, por ejemplo `"8081:8080"`, y abre el puerto 8081.

**Marginalia va lento o se detiene con "OutOfMemoryError".**
Aumenta la memoria: `-Xmx2g` (o más) en `JAVA_TOOL_OPTIONS` en `docker-compose.yml`.

<a id="logging-in"></a>

## Inicio de sesión

**"Save login" no mantiene la sesión iniciada.**
Los inicios de sesión guardados requieren HTTPS en un servidor. Coloca Marginalia detrás de un proxy inverso con HTTPS; consulta
[Servidor (Docker)](getting-started/docker-server.md#https-and-reverse-proxy).

**Olvidé mi contraseña.**
Un administrador puede asignar una nueva, o borrarla para que puedas iniciar sesión con una contraseña vacía y establecer una nueva tú mismo, en *Admin → Users* (consulta [Restablecer una contraseña olvidada](administration/users.md#resetting-a-forgotten-password)).
No existe restablecimiento de contraseña por correo electrónico.

**Ya nadie puede iniciar sesión como administrador.**
Sin un administrador, la contraseña solo se puede restablecer en la base de datos:

1. Detén Marginalia y copia `marginalia.sqlite` de la [carpeta de datos](administration/index.md#the-data-folder) como
   copia de seguridad.
2. Elimina la contraseña del administrador con la herramienta `sqlite3`:
   ```sh
   sqlite3 marginalia.sqlite "UPDATE users SET passwordHash = NULL WHERE login = 'admin';"
   ```
   (usa el nombre de usuario correcto).
3. Inicia Marginalia, inicia sesión con ese nombre de usuario y una contraseña **vacía**, y de inmediato establece una nueva contraseña con
   **Cambiar la contraseña**.

**"Server connection lost" o la página se recarga sin parar.**
Detrás de un proxy inverso, el proxy debe reenviar los WebSockets y permitir solicitudes largas; consulta el ejemplo de nginx en
[Servidor (Docker)](getting-started/docker-server.md#https-and-reverse-proxy). Con la aplicación de escritorio, probablemente se cerró;
vuelve a iniciarla.

**El espacio de trabajo congela el navegador al abrirse.**
Marginalia vuelve a abrir lo que tenías abierto cuando saliste, así que un libro muy largo (por ejemplo, uno importado desde un archivo) puede congelar la página antes de que puedas cambiar nada. Abre `/reset` en tu dirección de Marginalia (por ejemplo
`https://marginalia.example.com/reset`) e inicia sesión: el estado guardado del espacio de trabajo (libros abiertos, pestañas seleccionadas,
filtros) se olvida y acabas en un espacio de trabajo vacío. Después, define **Partes cargadas a la vez en la historia** en tu
[cuenta](account.md) antes de volver a abrir el libro.

<a id="models-and-generation"></a>

## Modelos y generación

**"Failed to download model list".**
Comprueba que la URL termine en `/v1`, que la clave de API sea correcta y que el servidor del modelo esté en marcha. Algunos servicios no listan sus modelos; en ese caso, escribe el ID del modelo. Consulta [Proveedores de inferencia](inference-providers.md#per-type-settings).

**Docker: no se puede acceder a un servidor de modelos de la misma máquina.**
Dentro del contenedor, `localhost` es el propio contenedor. Usa `http://host.docker.internal:<port>/v1` y haz que el
servidor del modelo escuche en todas las direcciones; consulta [Modelos en la misma máquina](getting-started/docker-server.md#models-on-the-same-machine).

**"Book is missing model." / "Book is missing protocol."**
Selecciónalos en la pestaña *About* del libro. Define *Modelo predeterminado* y *Protocolo predeterminado* en *Ajustes* para que los libros nuevos los reciban.

**"Contextual limit not sufficient."**
El prompt sin nada de historia ya es más grande que el espacio que deja el contexto después de la respuesta. Aumenta
*Tamaño máximo del contexto* del proveedor (o *Máximo de tokens de contexto* del protocolo), reduce *Máximo de tokens de respuesta*, o acorta
las plantillas, las entradas del lorebook y los resúmenes. Consulta [Límites](protocols.md#limits).

**El modelo indica que el contexto es demasiado largo.**
El recuento de tokens de Marginalia es algo impreciso con algunos modelos. Reduce *Tamaño máximo del contexto* en unos cientos de tokens. Antes de la solicitud aparece una advertencia con los tamaños estimados cuando el prompt ya parece demasiado grande.

**"The provider did not answer in time", "The provider is limiting requests", "The provider rejected the API key"...**
Marginalia explica en lenguaje sencillo los fallos de la API del modelo; consulta
[Mensajes de error](inference-providers.md#error-messages). *Probar la conexión* en el diálogo del proveedor comprueba la URL, la clave y el
modelo sin necesidad de un libro. El tiempo de espera y los reintentos se configuran por proveedor.

**El texto se detiene a mitad de una frase.**
Se alcanzó el límite de respuesta. Aumenta *Máximo de tokens de respuesta* (o *Máximo de tokens de respuesta* del protocolo) — los modelos de razonamiento necesitan muchos más, porque el razonamiento también cuenta.

**Las solicitudes fallan cuando el razonamiento está activado.**
La API no admite `reasoning_effort`. Desactiva *Activar el razonamiento* en el proveedor.

**"The summaries of the messages with IDs ... are outdated ... Continue generation and delete them?"**
Se editó o se eliminó una parte cubierta por un resumen. Responde *Sí* para eliminar los resúmenes desactualizados y continuar; después, genera otros nuevos. Responde *No* para detenerte y conservarlos: aparecen marcados en la vista general de resúmenes, donde puedes revalidar los que todavía dicen lo correcto. Consulta [Cuándo los resúmenes quedan desactualizados](books/summaries.md#when-summaries-become-outdated).

**El prompt contiene la palabra "Error".**
Una plantilla usa una variable o macro que no existe, a menudo por una errata como `instruction` en lugar de `instructions`.
El campo de la plantilla muestra debajo los nombres válidos. Consulta [Erratas](templates/templates.md#typos).

## Lorebooks

**Una entrada no se usa.** Revisa las condiciones de [Activación](lorebooks/entries.md#activation):

- ¿Está activada la entrada? ¿Está activado su lorebook, y cada sublorebook del camino hasta ella?
- ¿Tiene etiquetas? Entonces el libro necesita una de ellas (las etiquetas del libro están en la pestaña *About*). Las etiquetas negativas la excluyen.
- ¿El propio lorebook tiene **Etiquetas**? Se aplican a todas sus entradas.
- ¿Tiene un filtro? Los filtros solo ven las **instrucciones de la parte que se está escribiendo**, no la historia anterior:
  menciona al personaje en *Personajes presentes* o en las instrucciones.
- ¿El filtro es una expresión regular? Si no es válida, nunca coincide.

**Mostrar el prompt** en una parte generada muestra qué lore se envió.

<a id="prompts-and-settings"></a>

## Prompts y ajustes

**No puedo cambiar a otra pestaña.**
La pestaña *Ajustes* tiene cambios sin guardar. Haz clic en **Guardar** o en **Descartar los cambios**.

**Mi prompt de resumen no se usa.**
El prompt de resumen propio del libro (pestaña *Prompts*) tiene prioridad sobre el *Prompt de resumen predeterminado* de *Ajustes*; borra el campo del libro para usar el predeterminado. Los metarresúmenes tienen su propio prompt, el *Prompt de metarresumen predeterminado*.

**Las macros en Style, Point of View o Tense no funcionan.**
Estos campos son texto simple; solo las plantillas procesan macros. Colócala en la plantilla maestra o en el prompt de usuario.

<a id="backups"></a>

## Copias de seguridad

**Restauré una copia de seguridad de la base de datos, pero no cambió nada.**
La restauración se aplica en el siguiente arranque. Reinicia Marginalia. Consulta [Restaurar](administration/database-backups.md#restoring).

**Las copias de seguridad de los libros no aparecen tras cambiar de ordenador.**
Las copias de seguridad de los libros son archivos, no forman parte de la base de datos. Copia también la carpeta `data`; consulta
[La carpeta de datos](administration/index.md#the-data-folder).

<a id="extensions"></a>

## Extensiones

**Una extensión instalada no aparece.**
Cierra y vuelve a abrir el libro, o recarga la página.

**Una extensión no se carga y no aparece en Admin → Extensions.**
Detén Marginalia, elimina su JAR de la carpeta `extensions` y vuelve a arrancar. Consulta
[Cuando una extensión falla](administration/extensions.md#when-an-extension-fails).

**Una extensión fue rechazada o dejó de funcionar tras actualizar Marginalia.**
Las extensiones se compilan para una versión concreta de Marginalia y se comprueban al cargarse. Abre el informe (Admin → Extensions → *Verificación*) y carga la versión de la extensión que corresponda a tu Marginalia. Consulta
[Cuando una extensión falla](administration/extensions.md#when-an-extension-fails).

**Tras actualizar una extensión, sigue ejecutándose la versión antigua.**
Descarga primero la versión antigua y luego carga la nueva, o reinicia Marginalia.
<a id="faq"></a>

## Preguntas frecuentes

**¿Se envía mi escritura a algún sitio?**
Solo a los proveedores de inferencia que configures: el prompt de cada parte (y las peticiones de conteo de tokens) va a la API de ese proveedor. Con un modelo que se ejecuta en tu propio ordenador, nada sale de él. Marginalia no tiene telemetría.

**¿Qué modelo debería usar?**
Cualquier modelo que escriba bien ficción y tenga un contexto suficientemente grande; un contexto de 32 000 tokens o más es cómodo para libros largos. Prueba varios con el mismo libro: cambiar de proveedor es un clic en la pestaña *Información*.

**¿Pueden trabajar varias personas en un mismo libro?**
No. Cada libro pertenece a un único usuario. Otros usuarios pueden leerlo cuando se [publica](books/publishing.md).

**¿Puedo usar Marginalia sin conexión?**
Sí, con un modelo que se ejecute en local (llama.cpp, LM Studio, Ollama...). Marginalia en sí no necesita conexión a internet.

**¿Cómo saco mi libro de Marginalia?**
[Exporta la historia](books/export.md) como texto, HTML, Word, PDF o EPUB desde el menú de ajustes (⚙) de la pestaña *Historia*.
Para conservarlo todo, incluidas todas las ramas, [exporta una copia de seguridad del libro](books/backups.md#exporting-and-importing) (JSON).
También puedes leerlo en el [visor](books/publishing.md).

**¿Funciona en el móvil?**
El [visor](books/publishing.md) está pensado para móviles, y Marginalia puede añadirse a la pantalla de inicio como una aplicación
(*Instalar aplicación* / *Añadir a pantalla de inicio* en el menú del navegador). El editor tiene una [disposición para móvil](phone.md) cuando la ventana del navegador es más estrecha que 768 píxeles. No tiene página de administración, tiene un editor de lorebook reducido y no todas las extensiones, así que funciona mejor para escribir y leer; la configuración y la administración son más cómodas en una pantalla grande.

**¿Hay un tema claro o otro idioma?**
Sí a ambas cosas: el botón del sol / la luna junto al selector de idioma cambia el tema (oscuro por defecto), y el selector de idioma ofrece japonés y los idiomas que añaden las extensiones (véase [Cuenta](account.md#theme)).
