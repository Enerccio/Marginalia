---
label: Inicio rápido
order: 70
---

# Inicio rápido

Desde una instalación nueva hasta la primera parte generada de una historia. Supone que Marginalia está
[instalado](index.md) y que has iniciado sesión como [administrador](first-start.md).

<a id="1-connect-a-model"></a>

## 1. Conectar un modelo

1. Abre la pestaña **Proveedores de inferencia** y haz clic en **Añadir proveedor de inferencia**.
2. Introduce un **Nombre**, por ejemplo `OpenRouter - Claude`.
3. En **Ajustes generales**, establece **Tamaño máximo del contexto** con el tamaño de contexto del modelo, por ejemplo `32000`, y
   **Máximo de tokens de respuesta** con la parte más larga que quieras, por ejemplo `2000`.
4. En **Ajustes por tipo**, introduce la **OpenAI URL** (termina en `/v1`) y la **Clave de API** (déjala vacía para un servidor local que no tenga clave), haz clic en **Actualizar** y
   elige el **Modelo**.
5. Haz clic en **Aceptar**.

Los campos se describen en [Proveedores de inferencia](../inference-providers.md), con las URL de los servicios más comunes.

<a id="2-create-a-protocol"></a>

## 2. Crear un protocolo

1. Abre la pestaña **Protocolos** y haz clic en **Añadir protocolo**.
2. Introduce un **Nombre**, por ejemplo `Default`.
3. Deja vacíos **Máximo de tokens de contexto** y **Máximo de tokens de respuesta**; se usan los límites del proveedor de inferencia.
4. Haz clic en **Aceptar**.

Consulta [Protocolos](../protocols.md).

<a id="3-set-the-defaults"></a>

## 3. Establecer los valores predeterminados

Abre la pestaña **Ajustes** y elige el nuevo proveedor como **Modelo predeterminado** y el nuevo protocolo como
**Protocolo predeterminado**. Haz clic en **Guardar**. Los libros nuevos empezarán entonces con ambos, así que no tendrás que seleccionarlos cada vez.

![Modelo y protocolo predeterminados en Ajustes](../../images/settings-defaults.png)

<a id="4-create-a-book"></a>

## 4. Crear un libro

1. Abre la pestaña **Libros** y haz clic en **Añadir libro**.
2. Introduce el nombre del libro. El libro se abre.
3. En la pestaña **Información**, comprueba que **Modelo** y **Protocolos** están establecidos (vienen de tus valores predeterminados), y añade una
   **Descripción** de la historia si lo deseas.

<a id="5-write-the-first-part"></a>

## 5. Escribir la primera parte

1. Abre la pestaña **Historia**.
2. Haz clic en **+** en la esquina inferior derecha. Se abre el panel *Instrucciones del nuevo turno*.
3. Describe en **Instrucciones** lo que debería ocurrir, por ejemplo:
   > Una farera en una isla remota encuentra un mensaje en una botella dirigido a ella, fechado cincuenta años en el futuro. Preséntala a ella y a la isla. Unas 800 palabras.

   *Ambientación de la escena*, *Personaje del punto de vista* y *Personajes presentes* son opcionales; rellénalos para dar más orientación al modelo.
4. Haz clic en **Generar**. El texto aparece mientras el modelo lo escribe. El botón rojo **Detener** (en lugar de **+**)
   interrumpe la generación.

![La primera parte generada](../../images/quick-start-first-part.png)

Haz clic en **+** de nuevo para la siguiente parte. Cada parte puede editarse, regenerarse o cambiarse por una versión alternativa desde
su menú (☰); consulta [Editor de la historia](../books/story-editor.md) y
[Ramas y árbol de la historia](../books/branches-and-story-tree.md).

<a id="next-steps"></a>

## Próximos pasos

- Describe tu mundo y tus personajes en un [libro de lore](../lorebooks/index.md).
- Ajusta el estilo, el punto de vista y el tiempo verbal en los [prompts](../books/prompts.md) del libro.
- [Resume](../books/summaries.md) las partes anteriores a medida que el libro crece.
- Configura las [copias de seguridad del libro](../books/backups.md).
