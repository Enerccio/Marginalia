package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

/**
 * The built-in system messages of the Knowledge Tracker in Spanish; the JSON of the answer, its values ("knows",
 * "does-not-know"), the tool name and the marker "learned in part N" stay English.
 */
public class KnowledgeTrackerDefaultsES extends KnowledgeTrackerDefaults {

    @Override
    public String extractionSystem() {
        return """
            Llevas la cuenta de lo que saben los personajes de una historia. Recibes el mundo (el lorebook, si se da), los personajes SEGUIDOS con los elementos numerados de lo que cada uno sabe o no sabe ahora, y los nuevos MENSAJES de la historia (numerados). Indica, para cada personaje seguido, solo lo que cambió en estos mensajes.

            Reglas:
            1. add: conocimiento que el personaje ADQUIERE en estos mensajes: lo que vio, oyó, leyó, le contaron o dedujo por sí mismo. No lo que sabe el lector, no lo que solo dice el narrador, no lo que saben otros personajes.
            2. NO añadas conocimiento general que un habitante de ese mundo tiene de todos modos (quién es el rey para un súbdito del reino, las costumbres comunes, el idioma, la geografía de su propio hogar), ni nada que se derive del propio personaje tal como lo describe el texto del mundo (su especie, su cuerpo, sus capacidades, su historia, su familia, su oficio).
            3. NO añadas nada que ya digan los elementos del personaje.
            4. retract: el número de un elemento que los mensajes contradicen claramente (por ejemplo «nunca ha visto a X» después de que se conozcan). Solo cuando el texto muestra la contradicción. Un elemento que dice que un personaje no sabe Y solo se retira si el personaje se entera de Y en estos mensajes. Un elemento que dice más de una cosa solo se retira cuando los mensajes lo contradicen por completo; cuando solo se contradice una parte, deja el elemento y añade lo que se aprendió. Juzga un elemento por cómo TERMINAN los mensajes: cuando más adelante en estos mensajes al personaje se le cuenta, se le recuerda o vuelve a deducir lo que dice el elemento (pregunta quién es alguien y luego se lo dicen), el elemento es cierto al final, así que no lo retires ni lo vuelvas a añadir.
            5. Nunca hagas suposiciones. En caso de duda, no indiques nada. Un personaje que no está presente, o que no ve ni oye un suceso (pensamientos privados, susurros, una carta cerrada), no aprende nada de él.
            6. Cada cambio necesita una cita de prueba copiada de los mensajes y el número del mensaje.
            7. Registra solo conocimiento que pueda importar más adelante en la historia: secretos, identidades, relaciones y sentimientos revelados, planes, promesas, descubrimientos y hechos que otro personaje no sabe. NO registres sucesos que todos los personajes presentes vieron u oyeron juntos (el propio texto de la historia los contiene), charla trivial, bromas, estados de ánimo, el tiempo, objetos y detalles del escenario, lo que hizo el propio personaje, ni el mismo hecho para varios personajes. Como mucho 3 adiciones por personaje, normalmente 0 o 1. Cuando nada importa, no indiques nada.

            Responde solo con JSON, sin otro texto: {"characters":[{"name":"...","retract":[{"item":N,"evidence":"...","message":N}],"add":[{"kind":"knows|does-not-know","text":"...","evidence":"...","message":N}]}]}. Un personaje sin cambios tiene listas vacías.""";
    }

    @Override
    public String readingNote() {
        return """
            También puedes llamar a read_part con el número de una parte anterior de la historia. Los elementos que se aprendieron en una parte anterior lo indican ("learned in part N") y los mensajes dicen qué parte son. Cuando un elemento procede de oídas, de un rumor, de un plan o de una presentación y estos mensajes parecen confirmarlo, repetirlo o contradecirlo, o el elemento por sí solo no te dice qué sabía el personaje en ese momento, lee primero esa parte y luego decide, en lugar de adivinar. No leas partes por ningún otro motivo (como mucho %d). Tu respuesta final sigue siendo solo el JSON.""".formatted(PartReader.MAX_READS);
    }

    @Override
    public String tidySystem() {
        return """
            Ordenas lo que saben los personajes de una historia. Recibes los personajes SEGUIDOS con los elementos numerados que saben o no saben, los elementos que se quedan como están (solo para comparar) y las últimas partes de la historia como contexto. Los elementos se envían a un modelo de escritura para que no dé a un personaje conocimientos que no puede tener; cada elemento ocupa espacio y debe merecerlo.

            Reglas:
            1. drop: quita un elemento que no merece conservarse: una trivialidad (un detalle de la escena, charla trivial, una broma, un estado de ánimo, un objeto), un suceso que todos los personajes de la escena vieron juntos (la historia ya lo contiene), algo que los sucesos posteriores dejaron atrás, o algo que dice lo mismo que otro elemento o que un elemento que se queda.
            2. merge: une dos o más elementos sobre lo mismo en un solo elemento que conserve todos sus hechos. No añadas nada que no esté en los elementos.
            3. NUNCA quites ni unas un secreto, una identidad, una relación, un sentimiento revelado, un plan, una promesa, un descubrimiento ni nada que otro personaje no sepa, aunque las últimas partes no lo mencionen: que un elemento no haya salido últimamente no es motivo para quitarlo.
            4. En caso de duda, consérvalo. Indica solo aquello de lo que estés seguro; las listas vacías son normales.
            5. Cada drop y cada merge necesita un motivo breve.

            Responde solo con JSON, sin otro texto: {"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"merge":[{"items":[N,N],"kind":"knows|does-not-know","text":"...","reason":"..."}]}]}. Un personaje sin nada que hacer tiene listas vacías.""";
    }
}
