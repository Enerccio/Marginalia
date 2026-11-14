---
label: Guía de usuario
icon: person
expanded: true
order: 900
---

# Guía de usuario

Esta parte del manual está pensada para quienes escriben con Marginalia y para quienes lo administran para otras personas. Explica
cada pantalla y cada ajuste, desde la instalación de Marginalia y la conexión de un modelo hasta la ramificación de una historia, la creación de un
libro de lore y la copia de seguridad de la base de datos. No necesitas saber nada de programación; unas páginas sobre plantillas
profundizan en el tema para quienes quieran ajustar con precisión lo que se envía al modelo.

Si es tu primera vez, empieza por [Primeros pasos](getting-started/index.md): te lleva desde la instalación hasta la primera
parte generada de un libro en pocos minutos.

<a id="how-marginalia-works"></a>

## Cómo funciona Marginalia

Escribes una historia junto con un modelo de lenguaje grande. Para cada **parte** de la historia indicas lo que debe pasar -
la escena, el punto de vista, quién está presente, unas frases de instrucciones- y el modelo escribe la prosa.
Antes de cada parte, Marginalia construye el prompt para el modelo a partir de:

```mermaid
flowchart LR
    instr["Your instructions<br/>for the next part"] --> prompt
    story["The story so far<br/>(active branch)"] --> prompt
    sum["Summaries<br/>of older parts"] --> prompt
    lore["Lorebook entries<br/>relevant now"] --> prompt
    \style["Book prompts<br/>POV, tense, style"] --> prompt
    prompt["Prompt<br/>(templates)"] --> model["Model<br/>(inference provider + protocol)"]
    model --> part["New part"]
```

Todo lo que aparece en este esquema se puede ajustar, y la guía de usuario está organizada siguiendo ese orden.

<a id="core-concepts"></a>

## Conceptos básicos

| Concepto                | Qué es                                                                                                                                                                 | Más información                                           |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| **Libro**               | Una historia con todo lo que le pertenece: partes, prompts, libro de lore, resúmenes, copias de seguridad.                                                             | [Libros](books/index.md)                                   |
| **Parte**               | Un fragmento de la historia, normalmente una respuesta del modelo. Puedes editarla, regenerarla o borrarla.                                                           | [Editor de historia](books/story-editor.md)                     |
| **Rama**                | Las partes forman un árbol: puedes escribir otra versión de una parte o continuar de forma distinta desde cualquiera anterior. La *rama activa* es la que ves y la que lee el modelo. | [Ramas y árbol de la historia](books/branches-and-story-tree.md) |
| **Libro de lore**       | Notas sobre el mundo y los personajes. Cada **entrada** se incluye en el prompt solo cuando es relevante, por ejemplo cuando aparece el nombre de un personaje.       | [Libros de lore](lorebooks/index.md)                           |
| **Resumen**             | Una versión condensada de las partes antiguas, para que un libro largo siga cabiendo en lo que el modelo puede leer de una vez.                                      | [Resúmenes](books/summaries.md)                                |
| **Metarresumen**        | Un resumen de resúmenes: varios resúmenes fusionados en un texto más corto, para liberar contexto.                                                                    | [Resúmenes](books/summaries.md#meta-summaries)            |
| **Plantilla**           | El texto con marcadores y macros a partir del cual se construye el prompt. Cada libro tiene sus propios prompts, con valores predeterminados en *Ajustes*.           | [Plantillas y macros](templates/index.md)                  |
| **Proveedor de inferencia** | La conexión con un modelo: dirección de la API, clave de API, modelo y sus límites.                                                                              | [Proveedores de inferencia](inference-providers.md)             |
| **Protocolo**           | Ajustes de generación: temperatura, top P, penalizaciones y límites opcionales. El proveedor indica *qué* modelo escribe; el protocolo, *cómo*.                      | [Protocolos](protocols.md)                                 |
| **Carpeta**             | Un lugar para los libros, anidable tanto como quieras. También puede contener ajustes (idioma, estilo, prompts) que heredan los libros que contiene.                  | [Carpetas](books/folders.md)                               |
| **Etiqueta**            | Una marca en libros y libros de lore; las etiquetas también deciden qué entradas del libro de lore usa un libro.                                                      | [Gestionar libros](books/managing-books.md)                 |
| **Extensión**           | Un complemento que añade funciones, como marcadores de capítulo o revisiones con IA.                                                                                   | [Extensiones](extensions/index.md)                         |
| **Comando**             | Una pequeña acción que ejecutas escribiendo `/name` en la línea de comandos junto al menú de la historia; las extensiones añaden los suyos.                              | [Comandos](commands/index.md)                             |
| **Agente**              | Un LLM que trabaja por su cuenta en un libro, mediante los comandos, sobre una copia del libro; revisas lo que hizo y lo aceptas o lo descartas.                      | [Agentes](agents.md)                                       |

Todo lo que creas pertenece a tu cuenta. Los demás usuarios de la misma instalación de Marginalia no ven tus libros, libros de lore,
proveedores ni ajustes; solo los libros que decidas [publicar](books/publishing.md), y los libros de lore y protocolos que decidas
[compartir](lorebooks/lorebooks.md#sharing-a-lorebook) (otros pueden importar una copia; los [protocolos](protocols.md#sharing-a-protocol) también).

<a id="the-workspace"></a>

## El espacio de trabajo

Después de iniciar sesión verás el espacio de trabajo. Las pestañas de la izquierda son las áreas principales de Marginalia, cada una marcada con un pequeño icono:

![El espacio de trabajo](../images/workspace.png)

| Pestaña                 |                                                                                                             |
|-------------------------|-------------------------------------------------------------------------------------------------------------|
| **Libros**              | Tus libros. Ábrelo para escribir. Consulta [Libros](books/index.md).                                                 |
| **Libros de lore**      | Todos tus libros de lore y sus entradas. Consulta [Libros de lore](lorebooks/index.md).                                  |
| **Ajustes**             | Tus valores predeterminados: modelo, protocolo y los prompts predeterminados para los libros nuevos. Consulta [Cuenta y ajustes](account.md). |
| **Protocolos**          | Ajustes de generación. Consulta [Protocolos](protocols.md).                                                         |
| **Proveedores de inferencia** | Conexiones con modelos. Consulta [Proveedores de inferencia](inference-providers.md).                                   |
| **Recursos**            | Los archivos que has subido, como imágenes de partes. Consulta [Recursos](resources.md).                           |
| **Papelera**            | Lo que has eliminado, hasta que un administrador lo limpie. Restáuralo desde aquí. Consulta [Papelera](trash.md).            |
| **Agentes**             | (Experimental) Tus agentes, sus ejecuciones y los resultados por revisar. Solo para usuarios que pueden usar agentes. Consulta [Agentes](agents.md). |

En la parte inferior están **Cambiar la contraseña**, **Cerrar sesión** y, para los administradores, **Administración**. Las extensiones pueden añadir más pestañas
y botones.

En una pantalla estrecha (menos de 768 píxeles) obtienes en su lugar una [vista de teléfono](phone.md) más sencilla: las pestañas están en un cajón (incluida Administración)
y las ventanas ocupan una sola columna. La vista se elige al cargar la página: después de girar el dispositivo o cambiar el tamaño de la ventana,
recarga la página para obtener la otra.

<a id="sections-of-this-guide"></a>

## Secciones de esta guía

| Sección                                       | Para                                                                                                                                                                                                                  |
|-----------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Primeros pasos](getting-started/index.md)   | Instalar la [aplicación de escritorio](getting-started/desktop-app.md) o un [servidor](getting-started/docker-server.md), el [primer arranque](getting-started/first-start.md) y un [inicio rápido](getting-started/quick-start.md). |
| [Proveedores de inferencia](inference-providers.md) | Conectar modelos en la nube y locales, límites de contexto, modelos de razonamiento.                                                                                                                                 |
| [Recursos](resources.md)                     | Los archivos que has subido: descargarlos, reemplazarlos y eliminarlos.                                                                                                                                                           |
| [Papelera](trash.md)                             | Libros, libros de lore y otros objetos eliminados, y cómo restaurarlos.                                                                                     |
| [Protocolos](protocols.md)                     | Temperatura, top P, penalizaciones y límites por protocolo.                                                                                                                                                               |
| [Libros](books/index.md)                       | Gestionar libros (clonar, etiquetas, importar desde un archivo), el editor de historia (buscar y reemplazar), ramas, prompts del libro, resúmenes, copias de seguridad y publicación.                                                                                                                         |
| [Libros de lore](lorebooks/index.md)               | Crear libros de lore, entradas y cómo se activan; importar y exportar (incluido SillyTavern).                                                                                                                   |
| [Plantillas y macros](templates/index.md)      | Qué se envía al modelo y cómo cambiarlo; referencia de macros.                                                                                                                                                 |
| [Extensiones](extensions/index.md)             | Las extensiones incluidas: Author's Note, Chapter Marker, Gemma 4 tools fix, Google Gemini, Instruction Exporter, Lore Generator, Lorebook VCS, Name generator, Name provider, Place provider, Reviewer, Side Query, Arc Outline, Character Tracker y la localización al chino.                                                                                                                                          |
| [Agentes](agents.md) | (Experimental) Dejar que un modelo trabaje por su cuenta en un libro: agentes, la consola, revisar y aplicar el resultado, límites. |
| [Comandos](commands/index.md) | La línea de comandos junto al menú de la historia: ejecutar comandos, escribir argumentos, detener un comando largo. |
| [Expresiones regulares](regular-expressions.md) | Las expresiones regulares de buscar y reemplazar y de los filtros, y el límite de tiempo. |
| [Cuenta y ajustes](account.md)              | Tu contraseña, nombre de usuario y ajustes predeterminados.                                                                                                                                                                       |
| [Administración](administration/index.md)     | Para administradores: usuarios y sus permisos, copias de seguridad de la base de datos, limpieza, instalación de extensiones, el asistente de ayuda y agentes.                                                                                                                                         |
| [Usar Marginalia en un teléfono](phone.md)       | La vista de teléfono: en qué se diferencia del escritorio, qué falta, qué extensiones funcionan.                                                                                                                           |
| [Solución de problemas y preguntas frecuentes](troubleshooting.md)   | Registros, problemas comunes y sus soluciones.                                                                                                                                                                           |

<a id="conventions"></a>

## Convenciones

- Los nombres de pestañas, botones y campos van en **negrita** o *cursiva*, escritos tal como aparecen en Marginalia, p. ej.
  *Administración → Usuarios*.
- `~/.marginalia` es la carpeta de datos en tu directorio personal (`%USERPROFILE%\.marginalia` en Windows); en un servidor Docker
  es la carpeta `./data` junto a `docker-compose.yml`.
- La mayoría de los cambios se guardan en cuanto los haces. La excepción es la pestaña *Ajustes*, que tiene un botón **Guardar**.
