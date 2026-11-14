---
label: Primeros pasos
order: 990
icon: rocket
---

# Primeros pasos

Marginalia se ejecuta en tu navegador. Puedes instalarlo de dos maneras:

| | [Aplicación de escritorio](desktop-app.md) | [Servidor (Docker)](docker-server.md) |
|---|---|---|
| Ideal para | Una persona en un ordenador | Varios usuarios, o acceso desde otros dispositivos |
| Instalación | Descargar y ejecutar, nada más que instalar | Docker y Docker Compose |
| Dirección | `http://127.0.0.1:8765` (solo tu ordenador) | Puerto `8080`, normalmente detrás de un proxy inverso con HTTPS |
| Datos | `~/.marginalia` | `./data` junto a `docker-compose.yml` |

Ambas versiones son la misma aplicación con las mismas funciones: libros, libros de consulta (lorebooks), usuarios, copias de seguridad y extensiones.
Para escribir desde un teléfono, ejecuta la versión de servidor y ábrela en el navegador del teléfono: consulta
[Usar Marginalia en un teléfono](../phone.md).

Marginalia no tiene un modelo propio. Necesita una **API compatible con OpenAI** para escribir texto: un servicio en la nube (OpenAI,
OpenRouter...) o un modelo que se ejecute en tu propio ordenador (llama.cpp, LM Studio, Ollama, vLLM...). Ten preparadas la dirección de la API y una clave de API (un servidor local que no necesita clave funciona sin ella, consulta [Añadir un proveedor](../inference-providers.md#adding-a-provider)).

<a id="steps"></a>

## Pasos

1. Instala Marginalia: [aplicación de escritorio](desktop-app.md) o [servidor](docker-server.md).
2. [Crea la cuenta de administrador](first-start.md) en el primer inicio.
3. Sigue el [inicio rápido](quick-start.md): conecta un modelo, crea un protocolo y escribe la primera parte de un libro.
4. Opcional: abre el [libro de ejemplo](example-book.md), una historia terminada con resúmenes, anotaciones y traducciones, para ver cómo es un libro largo.
