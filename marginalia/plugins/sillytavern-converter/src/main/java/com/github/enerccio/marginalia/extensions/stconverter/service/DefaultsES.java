package com.github.enerccio.marginalia.extensions.stconverter.service;

/**
 * The default prompt of the wand in Spanish; the JSON fields are the English ones.
 */
public class DefaultsES extends Defaults {

    @Override
    public String prompt() {
        return """
[ Eres un editor de historias con experiencia. El autor está reescribiendo un chat de rol como novela, turno a turno: para cada turno, un escritor recibe el escenario, el personaje del punto de vista, los personajes presentes y las instrucciones de lo que ocurre, y escribe el texto del turno. Tu tarea es leer el chat de abajo y dividirlo en esos turnos. ]

### Lore de fondo
{{backgroundLore}}

### El chat
Los mensajes de abajo son el chat en el orden en que se escribieron, los mensajes {{firstMessage}} a {{lastMessage}}. Cada uno está bajo su número y el nombre de quien lo escribió. Un mensaje del usuario es lo que hace o dice el personaje del usuario; un mensaje de un personaje es la respuesta de la historia.

{{chat}}

{{#if previousTurn}}
### Dónde está la historia
Los turnos anteriores al mensaje {{firstMessage}} ya están hechos. El último de ellos tiene estos datos; continúa a partir de ellos. Mantén el mismo personaje del punto de vista y la misma escena (lugar, momento, personajes presentes) hasta que el chat los cambie, y no vuelvas a contar lo que ya cuentan.

{{previousTurn}}

{{/if}}
### Pautas
1. Divide el chat en turnos en sus puntos de inflexión naturales (un cambio de lugar, de momento, de punto de vista o de situación, o cuando termina una parte de la acción), en el orden en que se escribieron los mensajes. Un turno se compone de uno o más mensajes consecutivos; entre todos los turnos cubren cada mensaje de {{firstMessage}} a {{lastMessage}} exactamente una vez, sin dejar ninguno fuera y sin ninguno en dos turnos. Prefiere turnos lo bastante largos para ser una escena de la historia, no una sola línea de diálogo.
2. Indica de qué mensajes se compone un turno con "firstMessage" y "lastMessage" (los números de los encabezados).
3. "sceneSetting": dónde y cuándo ocurre el turno, quién y qué hay allí y qué importa de la escena. Tómalo del chat y del lore de fondo; no inventes nada que el chat no respalde.
4. "povCharacter": el único personaje desde el que se escribe el turno, con el nombre que se usa en el chat. Mantén el mismo personaje de un turno a otro salvo que la historia cambie de verdad el punto de vista.
5. "presentCharacters": los nombres de los personajes que participan o están en la escena, separados por comas.
6. "instructions": lo que el escritor debe escribir para el turno, como instrucciones: los sucesos en el orden en que ocurren, lo que los personajes hacen, dicen y sienten, los detalles del chat que deben conservarse, cómo termina el turno. Todo lo que cuentan los mensajes debe estar ahí, para que el escritor pueda escribir el turno sin ver el chat. No continúes la historia, no cambies lo que ocurrió, respeta el lore de fondo.

### Salida
Responde solo con el objeto JSON que pide el formato de respuesta: la lista de "turns" con los campos de arriba. Sin introducción ni comentarios.
""";
    }
}
