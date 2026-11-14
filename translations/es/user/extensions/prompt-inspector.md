---
label: Inspector de prompt
order: 4
---

# Inspector de prompt

Muestra el prompt de una generación antes de enviarse al modelo y pregunta si debe enviarse.

<a id="how-it-works"></a>

## Funcionamiento

La extensión añade **Inspect prompt before sending** al grupo *Análisis* del menú de ajustes (⚙) del
[editor de historias](../books/story-editor.md). Es un elemento con casilla de verificación: elígelo para activar la inspección y vuelve a elegirlo para desactivarla.
La opción es un ajuste de tu usuario y se aplica a todos tus libros.

Mientras está activada, una generación (una parte nueva, un swipe, una regeneración) se detiene justo antes de la petición y abre un diálogo,
**¿Enviar este prompt al modelo?**. El texto es el prompt tal como lo recibirá el modelo, después de que todas las demás extensiones lo hayan
modificado: cada mensaje con su rol - `[SYSTEM]` (la plantilla, el lore y los resúmenes), las partes anteriores de la historia como mensajes `[USER]` y `[ASSISTANT]`, y las instrucciones del último turno. Las imágenes que se envían junto con el texto se cuentan al final de su mensaje.

![El prompt antes de enviarse](../../images/plugin-prompt-inspector.png)

- **Enviar** envía la petición y la generación continúa como de costumbre.
- **Cancelar** detiene la generación. La parte que se había iniciado para ella se elimina de nuevo (una parte regenerada recupera su texto anterior), igual que cuando una generación falla antes de que llegue ningún texto.

<a id="notes"></a>

## Notas

- El prompt se construye primero, como en cualquier generación, y se te pregunta como último paso antes de la petición. Si un resumen
  ha dejado de ser válido, la pregunta habitual sobre continuar sin él aparece antes de mostrar el prompt. No se hace ninguna petición al modelo
  hasta que respondas.
- Solo se retienen las generaciones iniciadas desde el editor de historias. [Agentes](../agents.md) y los comandos no tienen una página donde mostrar el prompt, y sus generaciones continúan sin preguntar.
- El diálogo puede moverse y redimensionarse, y el texto puede seleccionarse y copiarse. Hay que responderlo: Esc y un clic fuera no lo cierran. Si cierras la página con el diálogo abierto, se considera *Cancelar*: la generación se detiene y no se envía nada, en cuanto el servidor detecta que la página ya no está (puede tardar un poco).
- No se pregunta a otros usuarios, y el interruptor se conserva al recargar la página.
