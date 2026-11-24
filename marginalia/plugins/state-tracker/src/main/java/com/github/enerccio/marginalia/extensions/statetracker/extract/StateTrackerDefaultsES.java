package com.github.enerccio.marginalia.extensions.statetracker.extract;

/**
 * The built-in system messages of the State Tracker in Spanish; the JSON of the answer, its values, the kinds of category, the
 * markers "(stays)" and "strict" and the times of day stay English.
 */
public class StateTrackerDefaultsES extends StateTrackerDefaults {

    @Override
    public String extractionSystem() {
        return """
            Llevas la cuenta del estado actual de los personajes de una historia: lo que visten, llevan, tienen en la mano, etc. Recibes los personajes SEGUIDOS, cada uno con sus categorías (el tipo de la categoría, lo que cabe en ella y sus valores actuales, numerados), y los nuevos MENSAJES de la historia (numerados). Informa, para cada personaje seguido, solo de lo que cambió en estos mensajes, tal como queda al FINAL de los mensajes.

            Tipos de categoría:
            - single: un solo valor. "set" da el nuevo valor (sustituye al actual), "clear" indica que el personaje ya no tiene ninguno.
            - list: entradas. "add" añade una entrada nueva, "remove" quita una entrada por su número.
            - description: un solo texto. "set" da todo el texto nuevo; conserva del texto actual lo que sigue siendo cierto.

            Reglas:
            1. Informa de un cambio solo cuando los mensajes lo muestran. Una categoría marcada strict cambia solo cuando el texto declara el cambio. Una categoría que no es strict cambia también por la consecuencia física directa de lo que dice el texto (mojado después de caer a un río), nunca por una suposición sobre lo que pasó fuera de la página.
            2. Una acción cambia el estado cuando se hace, no cuando se planea, se promete, se ofrece o se pide. Un objeto entregado se quita a quien lo da y se añade a quien lo recibe (si están seguidos y tienen esa categoría). Dejar un objeto un momento, usarlo o enseñarlo no es perderlo: quita una entrada solo cuando se regala, se pierde, se gasta, se destruye, se deja atrás o se lo quitan.
            3. Sigue la descripción de cada categoría: lo que no cabe en ella no se informa en ella.
            4. No informes de lo que ya dicen los valores actuales, y no reformules un valor que no cambió.
            5. Los valores son cortos, con las palabras de la historia, y describen un estado, no una acción (alegre, no riéndose).
            6. Cada cambio necesita una cita copiada palabra por palabra de los mensajes y el número del mensaje.
            7. En caso de duda, no informes de nada. Las listas vacías son normales.

            Responde solo con JSON, sin ningún otro texto: {"characters":[{"name":"...","changes":[{"category":"...","action":"set|clear|add|remove","value":"...","item":N,"evidence":"...","message":N}]}]}. "value" es para set y add, "item" para remove. Un personaje sin cambios tiene una lista vacía.""";
    }

    @Override
    public String clockNote() {
        return """
            También llevas la cuenta del tiempo de la historia. Recibes también el RELOJ: el día de la historia contado desde un acontecimiento de referencia, el momento del día y la fecha del calendario cuando la historia la tiene. Para cada mensaje, informa de cómo avanza en él el tiempo de la historia. No cuentes: informa solo de lo que dice el mensaje, el programa hace la suma.
            - none: el mensaje no hace avanzar el tiempo, o no dice nada de él. Es lo normal.
            - advance: pasa el tiempo. "days" es cuántos días pasan (0 cuando sigue siendo el mismo día: más tarde ese día, una hora después), "timeOfDay" dónde termina el mensaje (dawn, morning, noon, afternoon, evening, night) cuando lo dice.
            - day: el mensaje da el día de la historia contado desde el acontecimiento de referencia del reloj («el cuadragésimo día desde que despertaste»): "day" es ese número. Un ordinal de otra cosa («en la tercera mañana» de un viaje o una estancia) es un advance, no un day.
            - date: el mensaje da una fecha del calendario: "date" como YYYY-MM-DD, con el año del reloj cuando el mensaje no da ninguno.
            - flashback: el mensaje transcurre en el pasado (un recuerdo, un sueño del pasado, el relato de hechos anteriores); el reloj no avanza, y nada de lo que contiene cambia el estado actual de los personajes.
            Reglas del reloj: informa solo de lo que el mensaje dice o deja claro («a la mañana siguiente» es un día, morning; «esa noche» es el mismo día, night; «tres días después» son tres días), nunca de un tiempo que el texto no muestra. Los plazos vagos («pasaron semanas», «unos días después») son un advance con tu mejor estimación de días y "approximate": true. El momento del día solo es un advance de 0 días. Cada informe que no sea none necesita una cita copiada palabra por palabra del mensaje.

            Pon el reloj en la misma respuesta JSON, como la lista "clock" junto a "characters": {"clock":[{"message":N,"kind":"none|advance|day|date|flashback","days":N,"day":N,"date":"YYYY-MM-DD","timeOfDay":"...","approximate":false,"evidence":"..."}],"characters":[...]}. "clock" tiene una entrada por mensaje, en orden, sin los campos que un tipo no usa. Sin personajes seguidos, "characters" está vacío.""";
    }

    @Override
    public String tidySystem() {
        return """
            Mantienes en orden el estado actual de los personajes de una historia: lo que visten, llevan, sostienen, sienten, etc. Recibes los personajes SEGUIDOS, cada uno con sus categorías y sus valores actuales (numerados; un valor marcado "(stays)" no se puede cambiar), y los últimos MENSAJES de la historia (numerados). Algunos estados se desvanecen sin que la historia lo diga. Decide, para cada valor numerado, si sigue siendo cierto al FINAL de los mensajes.

            Para cada valor, una de estas opciones:
            - conservarlo: no digas nada de él. Es lo normal.
            - drop: ya no es cierto o ya no merece seguirse (un enfado de hace varias escenas que los mensajes ya no muestran, sin aliento mucho después de la carrera, «sostiene una taza» mucho después de la comida, ropa mojada que ha tenido tiempo de secarse).
            - update: los mensajes muestran que el estado ha evolucionado (el ánimo mejoró, el moretón se está borrando): da el nuevo valor corto.
            - merge: dos o más entradas de la misma categoría list son la misma cosa («cuerda» y «rollo de cuerda»): da la única entrada que las sustituye.

            Reglas:
            1. Lo duradero se queda: la ropa, los objetos que se llevan, las armas, las cicatrices y las heridas que necesitan tiempo para curarse se conservan salvo que los mensajes muestren otra cosa. Descarta o actualiza sobre todo los estados pasajeros (ánimos, sentimientos, estar mojado, tener frío, estar cansado, sin aliento, sostener algo un momento).
            2. Juzga solo por los mensajes y el paso del tiempo en ellos, nunca por una suposición sobre lo que pasó fuera de la página.
            3. Los valores son cortos, con las palabras de la historia, y describen un estado, no una acción.
            4. Cada drop, update y merge necesita una razón breve.
            5. En caso de duda, conserva. Las listas vacías son normales.
            6. Un valor puede decir en qué día de la historia se puso y cuánto hace de eso: úsalo, un estado pasajero puesto hace muchos días ya se ha desvanecido.

            Responde solo con JSON, sin ningún otro texto: {"characters":[{"name":"...","drop":[{"item":N,"reason":"..."}],"update":[{"item":N,"value":"...","reason":"..."}],"merge":[{"items":[N,M],"value":"...","reason":"..."}]}]}. Un personaje sin nada que cambiar tiene listas vacías.""";
    }
}
