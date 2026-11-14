package com.github.enerccio.marginalia.extensions.loregenerator.service;

/**
 * The built-in prompts of the Lore Generator in Spanish.
 */
public class DefaultsES extends Defaults {

    @Override
    public String compactionPrompt() {
        return """
[Instrucción del sistema: eres un motor experto de compactación de datos. Tu única tarea es absorber el texto en bruto que se da abajo y convertirlo en una entrada de archivo de lore ultradensa y legible por máquina para la memoria de fondo de un LLM.]

REGLAS CRÍTICAS:

1. Elimina el 100 % del relleno atmosférico, la prosa florida, la preparación narrativa, el formato propio del mundo (como encabezados de informes o despedidas) y el adorno retórico.
2. Extrae solo los hechos duros: reglas, mecánicas espaciales, demografía, rasgos físicos, identificadores, motivaciones y límites funcionales.
   - EXCEPCIÓN NARRATIVA: Si el texto fuente contiene una anécdota, un suceso del pasado o una narración histórica, NO la borres. Comprímela en un bloque «[Precedente histórico]» o «[Núcleo psicológico]». Quita la prosa narrativa, pero conserva la dinámica causal exacta, el trauma, el rencor o el desencadenante de conducta que creó ese suceso. Incluye los detalles necesarios en los precedentes históricos y conserva TODOS los precedentes históricos.
3. Estructura toda la salida con atributos markdown claros en negrita y viñetas limpias y minimalistas.
4. Mantén una precisión absoluta en las descripciones espaciales o físicas (p. ej., el lado concreto de una cara, disposiciones geométricas precisas, cifras exactas).
5. Devuelve SOLO el bloque markdown en bruto. No incluyas introducción, cierre ni confirmación conversacional. Empieza directamente con el primer bloque de atributos.
6. IMPORTANTE: ¡Devuélvelo en un bloque de código markdown copiable entre triples comillas invertidas!
7. NO resumas información que ya es lore existente.

==== INICIO DEL MATERIAL FUENTE EN BRUTO ====
{{compactionData}}
==== FIN DEL MATERIAL FUENTE EN BRUTO ====
""";
    }

    @Override
    public String generationPrompt() {
        return """
[ Eres un experto en construcción de mundos y un maestro de la prosa muy descriptiva y atmosférica. Tu tarea es elaborar una entrada de lore detallada basada estrictamente en los parámetros que se dan abajo. Usa imágenes vívidas y un lenguaje rico, asegurándote de que cada detalle esencial quede integrado con naturalidad. ]

### Parámetros de entrada
- Tema: [Inserta el nombre del tema]
- Detalles esenciales que entretejer: [Inserta detalles, hechos o mecánicas concretos]
- Extensión / alcance: [p. ej., Análisis profundo (más de 800 palabras), Viñeta media (400 palabras) o Microlore (150 palabras)]
- Estructura requerida: [Indica exactamente qué secciones escribir, o escribe «A tu elección: genera al menos X secciones temáticas distintas»]

### Pautas de escritura
1. Tono y estilo: rico, evocador y atmosférico. Usa metáforas elegantes y una narración inmersiva adecuada al escenario (fantasía / ciencia ficción / actualidad).
2. Ritmo y profundidad: desarrolla mucho el lore. No te limites a enunciar un hecho; dedica espacio a la historia, al impacto cultural, a la experiencia sensorial y a los rumores que lo rodean. Ajústate a la extensión pedida.
3. Estructura: debes seguir la estructura requerida indicada arriba. Usa encabezados markdown descriptivos para cada sección.
4. Integración de los hechos: envuelve los detalles esenciales en una prosa suntuosa con naturalidad. Asegúrate de que sigan siendo lo bastante claros bajo las imágenes para que un LLM pueda extraerlos después.
5. Elección de nombres: si necesitas inventar un nombre, elige nombres poco comunes que no provoquen confusión de contexto con las entradas de lore ya generadas.

### Salida
Entrega solo la entrada de lore estilizada. No incluyas relleno conversacional de introducción ni de cierre.
""";
    }
}
