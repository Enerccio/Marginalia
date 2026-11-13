---
label: Revisor
order: 2
---

# Revisor

Permite que el modelo revise lo que has escrito: reacciones de lectores, una crítica de editor, una comprobación de continuidad, o lo que le pidas en el prompt. Las revisiones se guardan con la parte, junto a la historia, y nunca pasan a formar parte del libro.

![Una revisión de una parte de la historia](../../images/plugin-reviewer.png)

<a id="reviewing-a-part"></a>

## Revisar una parte

El menú (☰) de cada parte en el [editor de historia](../books/story-editor.md) incluye un submenú **Revisar**:

| Elemento | |
|---|---|
| **View / Generate Review** | Abre el diálogo de revisión. Si la parte aún no tiene revisión, se genera una de inmediato. |
| **Advanced Options** | Abre las [opciones avanzadas](#advanced-options) y luego genera una revisión con ellas. |
| **Delete Review** | Elimina todas las revisiones de la parte. |

En el diálogo de revisión:

- **‹** / **›** recorren las revisiones de la parte; **›** en la última revisión genera una nueva;
- **Stop generating** interrumpe la revisión que se está escribiendo;
- haz clic en el texto de una revisión para editarlo; se guarda al hacer clic fuera del campo;
- *Thinking Process* muestra el razonamiento de los [modelos de razonamiento](../inference-providers.md#reasoning-models);
- **Cerrar** cierra el diálogo.

<a id="what-the-model-gets"></a>

## Qué recibe el modelo

Por defecto, la revisión usa el **mismo prompt con el que se escribió la parte** (*Use standard prompt info*): el prompt del sistema con el lore y los resúmenes, las partes anteriores y, por último, la propia parte, y al final el prompt de revisión. El modelo revisa la parte con el contexto completo de la historia.

El **pre-prompt** del perfil, si está definido, va primero como mensaje de sistema.

<a id="advanced-options"></a>

## Opciones avanzadas

| Opción | |
|---|---|
| **Prompt Override** | Un prompt de revisión distinto para esta revisión. Comienza con el prompt del perfil. |
| **Use standard prompt info** | Activada: el prompt con el que se escribió la parte, como se ha descrito arriba. Desactivada: solo las partes de la historia, con las opciones siguientes. |
| **Include Lorebook** | (solo desactivada) Añade el lore que estaba activo cuando se escribió la parte. |
| **Con resúmenes** | (solo desactivada) Envía primero los [resúmenes](../books/summaries.md) de la historia y después las partes posteriores al resumen más reciente, igual que una generación: la parte con el resumen más reciente y todo lo anterior se representan con los resúmenes. Los resúmenes ocupan espacio del límite de tokens primero, y las partes reciben el resto, empezando por las más nuevas. No se envía un resumen cuya historia se modificó después de crearlo (la revisión no lo elimina). Desactivada: sin resúmenes, tantas de las partes más recientes como quepan. |
| **Send original text** | Solo se muestra cuando hay cargado un traductor ([Real Language Translator](real-language-translator.md) o [Conlang Translator](conlang-translator.md)). Desactivada: la revisión lee el inglés que hay detrás del habla traducida, como hacen todos los modelos. Activada: la parte revisada (y, con *Use standard prompt info* desactivada, las partes anteriores) se envía tal como está almacenada, con el habla en la traducción. El prompt con el que se escribió la parte (*Use standard prompt info* activada) se construyó a partir del inglés y así se mantiene. |
| **Token Limit** | (solo desactivada) Cuántos tokens de historia enviar: las partes hasta la revisada inclusive, empezando por las más nuevas, tantas como quepan. Parte del espacio reservado para el prompt del modelo. |

Con *Use standard prompt info* desactivada, el diálogo muestra el tamaño de la petición (*Total tokens*). **Generar** abre el diálogo de revisión con estas opciones; las revisiones que se generen después en ese diálogo también las usarán.

![Opciones avanzadas de revisión](../../images/plugin-reviewer-advanced.png)

<a id="profiles"></a>

## Perfiles

Los ajustes del revisor están en **Ajustes → Extension Settings → Reviewer Settings**. Se organizan en **perfiles**:

![Los ajustes del revisor](../../images/plugin-reviewer-settings.png)

| Campo | |
|---|---|
| **Profile** | El perfil que se edita, y el que se usa para todas las revisiones. |
| **New**, **Renombrar**, **Eliminar** | Gestionan los perfiles. El perfil *_Default* no se puede renombrar ni eliminar. |
| **Reviewer Model** | El [proveedor de inferencia](../inference-providers.md) que escribe las revisiones. Vacío: el modelo del libro. |
| **Reviewer Protocol** | El [protocolo](../protocols.md). Vacío: el protocolo del libro. |
| **Review Pre-Prompt (System)** | Un mensaje de sistema que se envía primero, por ejemplo: *Eres un editor de ficción exigente.* |
| **Review Post-Prompt (User)** | La petición de revisión, que se envía al final. |

Haz clic en **Guardar** en la parte superior de la página *Ajustes* para conservar los cambios.

El post-prompt predeterminado simula una discusión en un foro de relatos: un hilo inventado con treinta respuestas de lectores distintos, positivas y negativas. Otras ideas:

- *Enumera los errores de continuidad de la última parte en relación con la historia anterior. Cita los pasajes.*
- *Como editor de estilo, señala los clichés, las repeticiones y los verbos débiles de la última parte, con sugerencias.*
- *Describe cómo se sentiría un lector que llega por primera vez al final de esta parte, y qué espera que pase después.*

Crea un perfil para cada tipo de revisión y selecciona el que quieras antes de revisar. Un modelo más barato para las revisiones ahorra costes: define ese modelo como *Reviewer Model* del perfil.

<a id="storage"></a>

## Almacenamiento

Las revisiones se guardan con la parte de la historia, así que forman parte de las [copias de seguridad](../books/backups.md) y de las copias del libro. [Branch Story](../books/branches-and-story-tree.md#branching-from-an-earlier-part) no las copia. Los perfiles se guardan en tus ajustes de usuario.
