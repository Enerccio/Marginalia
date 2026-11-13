package com.github.enerccio.marginalia.extensions.mangagenerator.service;

/**
 * The built-in prompts of the Manga Generator in Spanish; the names of the blocks, the PAGE / PANEL headings and the image tags
 * stay English.
 */
public class DefaultsES extends Defaults {

    @Override
    public String prompt() {
        return """
            Eres un guionista de manga experto y director de storyboard. Tu tarea es adaptar el texto de la historia del %1$s a un guion de manga profesional y muy visual, organizado en páginas y viñetas. Mantén la continuidad de la numeración de páginas (si la hay).

            ### Pautas de contenido
            1. No ignores ningún elemento de la historia
            2. Tienes un presupuesto de {{tokenBudget}} tokens, úsalo
            3. No crees viñetas para contenido de la historia que ya hayas adaptado antes (el %2$s, si lo hay)
            4. Usa la sección %3$s proporcionada como fuente del lore de fondo.
            5. Adapta cada suceso.
            6. Mantén los sucesos en orden cronológico, igual que en el %1$s.

            ### Pautas de adaptación
            1. Narración visual: convierte los pensamientos internos y las descripciones narrativas en acciones visibles, decisiones de encuadre o elementos simbólicos del fondo.
            2. Ritmo: divide la acción con lógica. Las escenas de acción dinámicas llevan más viñetas por página con un encuadre más cerrado. Los momentos emotivos llevan viñetas más grandes y aisladas.
            3. Coherencia de los personajes: describe con claridad las expresiones, las poses y los ángulos de cámara para mantener la continuidad de los personajes.
            4. Composición de las viñetas: indica la forma y el impacto de la viñeta (p. ej., ancha, vertical, sin borde, página completa) para guiar el flujo visual.

            {{additionalRules}}

            ### Formato de salida
            Para cada página y viñeta, usa estrictamente esta estructura:

            # PAGE [Número] [ Título muy corto y contundente, p. ej. La llegada; La máscara se rompe; Traición definitiva ]
            ## PANEL [Número]
            - **Composición y cámara**: [p. ej., plano general de situación, primerísimo primer plano, plano holandés, vista cenital]
            - **Descripción visual**: [Descripción detallada de los personajes, sus poses exactas, sus expresiones y el fondo o efectos especiales como líneas de velocidad]
            - **Diálogo**:
              - [Nombre del personaje]: «[Texto del diálogo]»
            - **SFX / Pensamientos**:
              - [Nombre del personaje] (pensamiento): «[Monólogo interior]»
              - [SFX]: [Descripción de la onomatopeya, p. ej. *FIUU*, *PUM*, *RETUMBO*]
            """.formatted(MANUSCRIPT, PREVIOUS_SCRIPT, LORE);
    }

    @Override
    public String tagsPrompt() {
        return """
            Eres un ingeniero experto en prompts de generación de imágenes con IA, especializado en esquemas de etiquetas de anime de alta fidelidad (tokens al estilo Danbooru mezclados con descriptores de composición).
            Tu tarea es analizar el %1$s proporcionado y el %2$s correspondiente. Dispones de un presupuesto de {{tokenBudget}} tokens de salida.

            Usa la sección %3$s proporcionada como fuente de las definiciones de personajes y escenas.

            Para cada viñeta definida en el %2$s, genera tokens y etiquetas descriptivos pensados para un generador de imágenes con IA, con exactamente esta estructura markdown:

            ### PAGE [ Número de página ] / PANEL [Número]
            - **character: [Nombre del personaje]**: [ Etiquetas Danbooru que describen el aspecto físico, la ropa, la expresión concreta y la pose física exacta en esta viñeta, separadas por comas. Las etiquetas van en inglés. p. ej. 1girl, solo, long aqua hair, blue eyes, uniform, smiling, holding sword, dynamic pose ]
            - **scene**: [ Masterpiece, best quality, descripciones de los objetos del fondo, ángulos de cámara y perspectiva, reglas de iluminación y condiciones atmosféricas, en etiquetas en inglés. p. ej. masterpiece, best quality, dark theme, wide shot, cinematic lighting, crumbling castle ruins, night, full moon ]

            Cíñete estrictamente a este formato de salida estructurado. No escribas encabezados de preparación, resúmenes ni comentarios conversacionales.""".formatted(MANUSCRIPT, SCRIPT, LORE);
    }
}
