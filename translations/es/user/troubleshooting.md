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

Al informar de un problema, incluya el texto de la versión de la esquina inferior izquierda de la página (por ejemplo
`Marginalia 1.5.0 (93ec118)`: la versión y el commit de Git de la compilación).

<a id="installing-and-starting"></a>

## Instalación e inicio

**macOS dice que la aplicación «está dañada» o que «no se puede abrir».**
La aplicación todavía no está firmada. Haga clic con el botón derecho en ella y elija *Open*, o ejecute
`xattr -dr com.apple.quarantine /Applications/Marginalia.app`. Consulte [Aplicación de escritorio](getting-started/desktop-app.md).

**Windows SmartScreen bloquea la aplicación.**
Haga clic en *More info* → *Run anyway*.

**El navegador abre un puerto distinto de 8765.**
El puerto 8765 estaba ocupado, así que la aplicación de escritorio eligió uno libre. Use **Open Marginalia** en el menú de la bandeja, o inícielo con
`--port` para fijar el puerto.

**«Marginalia did not start in time».**
La aplicación de escritorio no pudo iniciar su servidor. El registro indica el motivo: a menudo otro programa tiene bloqueados los archivos, o una extensión está rota (consulte [más abajo](#extensions)).

**Docker: el puerto 8080 ya está en uso.**
Cambie la parte izquierda de la asignación de puertos en `docker-compose.yml`, por ejemplo `"8081:8080"`, y abra el puerto 8081.

**Marginalia va lento o se detiene con «OutOfMemoryError».**
Aumente la memoria: `-Xmx2g` (o más) en `JAVA_TOOL_OPTIONS` en `docker-compose.yml`.

<a id="logging-in"></a>

## Inicio de sesión

**«Save login» no mantiene la sesión iniciada.**
Los inicios de sesión guardados requieren HTTPS en un servidor. Coloque Marginalia detrás de un proxy inverso con HTTPS; consulte
[Servidor (Docker)](getting-started/docker-server.md#https-and-reverse-proxy).

**Olvidé mi contraseña.**
Un administrador puede asignar una nueva, o borrarla para que pueda iniciar sesión con una contraseña vacía y establecer una nueva usted mismo,
en *Admin → Users* (consulte [Restablecer una contraseña olvidada](administration/users.md#resetting-a-forgotten-password)).
No hay restablecimiento de contraseña por correo electrónico.

**Nadie puede iniciar sesión como administrador.**
Sin un administrador, la contraseña solo puede restablecerse en la base de datos:

1. Detenga Marginalia y copie `marginalia.sqlite` de la [carpeta de datos](administration/index.md#the-data-folder) como copia de seguridad.
2. Elimine la contraseña del administrador con la herramienta `sqlite3`:
   ```sh
   sqlite3 marginalia.sqlite "UPDATE users SET passwordHash = NULL WHERE login = 'admin';"
   ```
   (use el nombre de usuario correcto).
3. Inicie Marginalia, entre con ese nombre de usuario y una contraseña **vacía**, y enseguida asigne una nueva contraseña con
   **Cambiar la contraseña**.

**«Server connection lost» o la página no para de recargarse.**
Detrás de un proxy inverso, el proxy debe reenviar WebSockets y permitir peticiones largas; consulte el ejemplo de nginx en
[Servidor (Docker)](getting-started/docker-server.md#https-and-reverse-proxy). Con la aplicación de escritorio, probablemente la aplicación se cerró; vuelva a iniciarla.

**El espacio de trabajo congela el navegador al abrirse.**
Marginalia vuelve a abrir lo que tenía abierto al salir, así que un libro muy largo (por ejemplo, uno importado desde un archivo) puede congelar la página antes de que pueda cambiar nada. Abra `/reset` en su dirección de Marginalia (por ejemplo
`https://marginalia.example.com/reset`) e inicie sesión: el estado guardado del espacio de trabajo (libros abiertos, pestañas seleccionadas, filtros) se olvida y entra en un espacio de trabajo vacío. Después, establezca **Partes cargadas a la vez en la historia** en su [cuenta](account.md) antes de abrir el libro de nuevo.

<a id="models-and-generation"></a>

## Modelos y generación

**«Failed to download model list».**
Compruebe que la URL termina en `/v1`, que la clave de API es correcta y que el servidor del modelo está en marcha. Algunos servicios no listan sus modelos; en ese caso, escriba el ID del modelo. Consulte [Proveedores de inferencia](inference-providers.md#per-type-settings).

**Docker: no se puede acceder a un servidor de modelos de la misma máquina.**
Dentro del contenedor, `localhost` es el propio contenedor. Use `http://host.docker.internal:<puerto>/v1` y haga que el servidor de modelos escuche en todas las direcciones; consulte [Modelos en la misma máquina](getting-started/docker-server.md#models-on-the-same-machine).

**«Book is missing model.» / «Book is missing protocol.»**
Selecciónelos en la pestaña *About* del libro. Establezca *Default Model* y *Default Protocol* en *Settings* para que los libros nuevos los tengan.

**«Contextual limit not sufficient.»**
El prompt sin ninguna historia ya es mayor que el espacio que deja el contexto después de la respuesta. Aumente *Max Context Size* del proveedor (o *Max Context Tokens* del protocolo), reduzca *Max Response Tokens*, o acorte las plantillas, las entradas del lorebook o los resúmenes. Consulte [Límites](protocols.md#limits).

**El modelo indica que el contexto es demasiado largo.**
El recuento de tokens de Marginalia es algo impreciso con algunos modelos. Reduzca *Max Context Size* unos cientos de tokens. Antes de la petición aparece un aviso con los tamaños estimados cuando el prompt ya parece demasiado grande.

**«The provider did not answer in time», «The provider is limiting requests», «The provider rejected the API key»...**
Marginalia explica en lenguaje sencillo los fallos de la API del modelo; consulte
[Mensajes de error](inference-providers.md#error-messages). *Probar la conexión* en el cuadro del proveedor comprueba la URL, la clave y el modelo sin necesidad de un libro. El tiempo de espera y los reintentos se configuran por proveedor.

**El texto se detiene a mitad de una frase.**
Se alcanzó el límite de la respuesta. Aumente *Max Response Tokens* (o *Max Reply Tokens* del protocolo): los modelos de razonamiento necesitan muchos más, porque el razonamiento también cuenta.

**Las peticiones fallan con el razonamiento activado.**
La API no admite `reasoning_effort`. Desactive *Activar el razonamiento* en el proveedor.

**«The summaries of the messages with IDs ... are outdated ... Continue generation and delete them?»**
Se editó o eliminó una parte cubierta por un resumen. Responda *Sí* para eliminar los resúmenes obsoletos y continuar, y después genere otros nuevos. Responda *No* para detenerse y conservarlos: aparecen marcados en la vista de resúmenes, donde puede revalidar los que aún dicen lo correcto. Consulte [Cuándo se quedan obsoletos los resúmenes](books/summaries.md#when-summaries-become-outdated).

**El prompt contiene la palabra «Error».**
Una plantilla usa una variable o macro que no existe, a menudo por una errata como `instruction` en lugar de `instructions`. El campo de la plantilla muestra debajo los nombres disponibles. Consulte [Erratas](templates/templates.md#typos).

## Lorebooks

**Una entrada no se usa.** Revise las condiciones de [Activación](lorebooks/entries.md#activation):

- ¿Está activada la entrada? ¿Está activado su lorebook, y cada sublorebook por el que se llega hasta ella?
- ¿Tiene etiquetas? Entonces el libro necesita una de ellas (las etiquetas del libro están en la pestaña *About*). Las etiquetas negativas la excluyen.
- ¿El propio lorebook tiene **Etiquetas**? Se aplican a todas sus entradas.
- ¿Tiene un filtro? Los filtros solo ven las **instrucciones de la parte que se está escribiendo**, no la historia anterior: mencione al personaje en *Personajes presentes* o en las instrucciones.
- ¿El filtro es una expresión regular? Si no es válida, nunca coincide.

**Mostrar el prompt** en una parte generada muestra qué lore se envió.

<a id="prompts-and-settings"></a>

## Prompts y ajustes

**No puedo cambiar a otra pestaña.**
La pestaña *Settings* tiene cambios sin guardar. Haga clic en **Guardar** o en **Descartar los cambios**.

**Mi prompt de resumen no se usa.**
El prompt de resumen propio del libro (pestaña *Prompts*) tiene prioridad sobre el *Prompt de resumen predeterminado* de *Ajustes*; borre el campo del libro para usar el predeterminado. Los metarresúmenes tienen su propio prompt, el *Prompt de metarresumen predeterminado*.

**Las macros en Style, Point of View o Tense no funcionan.**
Estos campos son texto simple; solo las plantillas procesan macros. Ponga la macro en la plantilla maestra o en el prompt del usuario.

<a id="backups"></a>

## Copias de seguridad

**Restauré una copia de seguridad de la base de datos, pero no cambió nada.**
La restauración se aplica en el siguiente inicio. Reinicie Marginalia. Consulte [Restauración](administration/database-backups.md#restoring).

**Las copias de seguridad de los libros no están tras cambiar de ordenador.**
Las copias de seguridad de los libros son archivos, no forman parte de la base de datos. Copie también la carpeta `data`; consulte
[La carpeta de datos](administration/index.md#the-data-folder).

<a id="extensions"></a>

## Extensiones

**Una extensión instalada no aparece.**
Cierre y vuelva a abrir el libro, o recargue la página.

**Una extensión no se carga y no aparece en Admin → Extensions.**
Detenga Marginalia, elimine su JAR de la carpeta `extensions` e inícielo de nuevo. Consulte
[Cuando falla una extensión](administration/extensions.md#when-an-extension-fails).

**Una extensión fue rechazada o dejó de funcionar tras actualizar Marginalia.**
Las extensiones se compilan para una versión concreta de Marginalia y se comprueban al cargarse. Abra el informe (Admin → Extensions → *Verificación*) y cargue la versión de la extensión que corresponda a su Marginalia. Consulte [Cuando falla una extensión](administration/extensions.md#when-an-extension-fails).

**Tras actualizar una extensión, sigue ejecutándose la versión anterior.**
Descargue primero la versión antigua y después cargue la nueva, o reinicie Marginalia.
<a id="faq"></a>

## Preguntas frecuentes

**¿Se envía mi escritura a algún sitio?**
Solo a los proveedores de inferencia que configures: el prompt de cada parte (y las peticiones de conteo de tokens) va a la API de ese proveedor. Con un modelo que se ejecuta en tu propio ordenador, nada sale de él. Marginalia no tiene telemetría.

**¿Qué modelo debo usar?**
Cualquier modelo que escriba buena ficción y tenga un contexto lo bastante grande; un contexto de 32 000 tokens o más es cómodo para libros largos. Prueba varios con el mismo libro: cambiar de proveedor es un clic en la pestaña *Información*.

**¿Pueden trabajar varias personas en un mismo libro?**
No. Cada libro pertenece a un único usuario. Otros usuarios pueden leerlo cuando se [publica](books/publishing.md).

**¿Puedo usar Marginalia sin conexión?**
Sí, con un modelo que se ejecute en local (llama.cpp, LM Studio, Ollama...). Marginalia en sí no necesita conexión a internet.

**¿Cómo saco mi libro de Marginalia?**
[Exporta la historia](books/export.md) como texto, HTML, Word, PDF o EPUB desde el menú de ajustes (⚙) de la pestaña *Historia*.
Para conservarlo todo, incluidas todas las ramas, [exporta una copia de seguridad del libro](books/backups.md#exporting-and-importing) (JSON).
También puedes leerlo en el [visor](books/publishing.md).

**¿Funciona en el teléfono?**
El [visor](books/publishing.md) está pensado para teléfonos, y Marginalia se puede añadir a la pantalla de inicio como si fuera una aplicación (*Instalar aplicación* / *Añadir a la pantalla de inicio* en el menú del navegador). El editor tiene una [disposición para teléfono](phone.md) cuando la ventana del navegador es más estrecha que 768 píxeles. No tiene página de administración, el editor de lorebook está reducido y no funcionan todas las extensiones, así que resulta más práctico para escribir y leer; la configuración y la administración son más cómodas en una pantalla más grande.

**¿Hay tema claro o otro idioma?**
Sí a ambas cosas: el botón con el sol / la luna junto al selector de idioma cambia el tema (oscuro por defecto), y el selector de idioma ofrece japonés y los idiomas que traen las extensiones (véase [Cuenta](account.md#theme)).
