---
label: Inicio
icon: home
order: 1000
---

# Marginalia

Marginalia es una herramienta de escritura autoalojada para ficción de larga extensión escrita en colaboración con modelos de lenguaje de gran tamaño. Tú escribes las instrucciones para cada parte de la historia, el modelo escribe la prosa, y Marginalia mantiene en orden todo lo que la rodea: la historia en forma de árbol con ramificaciones, los libros de lore del mundo, los resúmenes que permiten que las obras largas quepan en el contexto del modelo, y los ajustes de prompt que dan forma al estilo.

Se ejecuta en el navegador: como aplicación de escritorio en tu propio ordenador, o como un pequeño servidor para ti y tus amigos, y funciona con cualquier API compatible con OpenAI: OpenAI, OpenRouter, LiteLLM, llama.cpp, LM Studio, Ollama, vLLM y otras.

![El editor de historias](images/hero-story-editor.png)

[!button variant="primary" text="Empezar"](user/getting-started/index.md)
[!button text="Descargar"](https://github.com/Enerccio/Marginalia/releases)
[!button text="Código fuente en GitHub"](https://github.com/Enerccio/Marginalia)

<a id="what-it-does"></a>

## Qué hace

- **Libros con historias ramificadas** – regenera o intercambia una parte, ramifica desde cualquier punto, muévete por el árbol de la historia, edita el texto generado. Consulta [Libros](user/books/index.md).
- **Libros de lore** – notas sobre el mundo y los personajes que se incluyen en el prompt solo cuando importan: por etiquetas del libro, filtros de texto o expresiones regulares, libros de lore anidados. Se puede importar información de mundo de SillyTavern. Consulta [Libros de lore](user/lorebooks/index.md).
- **Plantillas y macros** – los prompts son plantillas de Handlebars con macros compatibles con SillyTavern, de modo que el lore importado sigue funcionando. Consulta [Plantillas y macros](user/templates/index.md).
- **Resúmenes** – las partes más antiguas se condensan para que las obras largas quepan en la ventana de contexto. Consulta [Resúmenes](user/books/summaries.md).
- **Cualquier modelo compatible con OpenAI** – incluidos los modelos de razonamiento, con límites por modelo y ajustes de generación intercambiables. Consulta [Proveedores de inferencia](user/inference-providers.md) y [Protocolos](user/protocols.md).
- **Copias de seguridad** – copias por libro con restauración y copia, y copias completas de la base de datos programadas. Consulta [Copias de seguridad de libros](user/books/backups.md) y [Copias de seguridad de la base de datos](user/administration/database-backups.md).
- **Varios usuarios** – cada uno con sus propios libros, lore y modelos; los administradores gestionan usuarios, copias de seguridad y limpieza. Consulta [Administración](user/administration/index.md).
- **Libros publicados** – una vista de lectura de solo lectura que puedes compartir. Consulta [Publicación y visor](user/books/publishing.md).
- **Búsqueda y reemplazo, comandos** – busca y reemplaza en la historia y en los libros de lore, y una línea de comandos junto al menú de la historia con comandos que leen y modifican el libro. Consulta [Comandos](user/commands/index.md) y [Expresiones regulares](user/regular-expressions.md).
- **Agentes** – un modelo que trabaja sobre un libro por su cuenta mediante los comandos, sobre una copia: lo observas, hablas con él en una consola y revisas sus cambios antes de que lleguen al libro. Consulta [Agentes](user/agents.md).
- **Extensiones** – los plugins añaden funciones en tiempo de ejecución: marcadores de capítulo, historial del libro de lore, revisiones con IA, preguntas paralelas al modelo. Consulta [Extensiones](user/extensions/index.md).

<a id="which-guide-do-you-need"></a>

## ¿Qué guía necesitas?

|                                           |                                                                                                                                   |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **[Guía de usuario](user/index.md)**      | Quieres instalar Marginalia, ejecutarlo para otros o escribir con él. Empieza por [Primeros pasos](user/getting-started/index.md). |
| **[Guía de desarrollo](developer/index.md)** | Quieres compilar Marginalia desde el código fuente, modificarlo, escribir una extensión o entender cómo funciona por dentro.       |

<a id="quick-links"></a>

## Enlaces rápidos

| Quiero...                      |                                                          |
|--------------------------------|----------------------------------------------------------|
| Instalarlo en mi ordenador     | [Aplicación de escritorio](user/getting-started/desktop-app.md)       |
| Ejecutarlo en un servidor      | [Servidor (Docker)](user/getting-started/docker-server.md) |
| Conectar un modelo             | [Proveedores de inferencia](user/inference-providers.md)       |
| Escribir la primera parte de un libro | [Inicio rápido](user/getting-started/quick-start.md)       |
| Usar mis libros de lore de SillyTavern | [Importar y exportar](user/lorebooks/import-export.md)       |
| Solucionar un problema         | [Solución de problemas y preguntas frecuentes](user/troubleshooting.md)         |
| Escribir una extensión         | [Desarrollo de plugins](developer/plugins/index.md)         |
| Informar de un error o contribuir | [Contribuir](developer/contributing.md)                |

<a id="about"></a>

## Acerca de

Marginalia es software de código abierto bajo la [Licencia MIT](https://github.com/Enerccio/Marginalia/blob/master/LICENSE).
Las imágenes usadas en las capturas de pantalla de este manual se acreditan en la página de [Créditos](credits.md).
Las versiones están en [GitHub](https://github.com/Enerccio/Marginalia/releases); los errores y las ideas van al [gestor de incidencias](https://github.com/Enerccio/Marginalia/issues).
