package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * The built-in prompts of the Audiobook in Spanish; the JSON fields and their values are the English ones.
 */
public class AudiobookDefaultsES extends AudiobookDefaults {

    @Override
    public String labelTemplate() {
        return """
            Etiquetas los fragmentos de una historia para un audiolibro con varias voces. Los fragmentos están numerados; N es narración, D es discurso directo. {{#if characters}}Los personajes conocidos hasta ahora son: {{characters}}. Pueden hablar otros; nómbralos como lo hace el texto.{{else}}No se conocen: nombra a quien habla como lo nombra el texto.{{/if}}
            Para cada fragmento responde con:
            - "id": el número del fragmento;
            - "speaker": para D, el personaje que lo dice, deducido del contexto (un nombre en la narración de alrededor, un vocativo, quién responde a quién). Para N es "Narrator", salvo cuando el contexto muestra que el texto son las propias palabras de un personaje: una carta, una nota, un mensaje, un letrero o una inscripción que un personaje escribió y que la historia cita, o los pensamientos de un personaje. Entonces nombra a ese personaje (quien escribe, una firma, quien piensa), también durante varios párrafos seguidos. La cursiva (marcas como *...* o _..._) es solo una pista: el mismo formato se usa también para el énfasis, los sonidos y las palabras extranjeras, que lee el narrador. En caso de duda es "Narrator";
            - "delivery": cómo debe leerse ({{deliveries}}) e "intensity" de 0.0 a 1.0;
            - "tagged_text": para N, una cadena vacía. Para D, el fragmento palabra por palabra, sin cambios, salvo que puedes insertar una etiqueta de sonido donde el contexto pida claramente ese sonido del personaje que habla, y rara vez (como mucho una etiqueta por cada {{wordsPerTag}} palabras de todo el texto). Etiquetas permitidas: {{tags}}, escritas exactamente así. No cambies, añadas ni quites ninguna otra palabra. Para cada fragmento sin etiqueta usa una cadena vacía: nunca copies el texto de un fragmento que no tiene etiqueta.
            Los fragmentos marcados "context" ya se etiquetaron: léelos para entender quién habla, no los etiquetes.
            Responde solo con el JSON, una entrada por fragmento, en orden.""";
    }
}
