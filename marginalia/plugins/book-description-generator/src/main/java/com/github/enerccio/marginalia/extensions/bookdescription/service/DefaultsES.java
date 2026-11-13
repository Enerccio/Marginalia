package com.github.enerccio.marginalia.extensions.bookdescription.service;

import com.github.enerccio.marginalia.extensions.bookdescription.model.OutputFormat;

/**
 * The built-in prompts of the Book Description Generator in Spanish; the JSON fields and the Markdown heading of the answer are
 * the ones the plugin reads.
 */
public class DefaultsES extends Defaults {

    @Override
    public String descriptionPrompt() {
        return """
[ Eres un editor de libros con experiencia que escribe los títulos y las sinopsis de contracubierta. Tu tarea es leer una historia ya escrita y dar al libro al que pertenece un nombre y una descripción. ]

### El libro ahora
Nombre: {{bookName}}
{{#if bookDescription}}Descripción: {{bookDescription}}{{/if}}

### La historia
{{#if fromNotes}}La historia es demasiado larga para darla completa. Abajo están las notas que se tomaron de ella, parte por parte, en el orden en que se lee.{{else}}Abajo está la historia tal como se escribió, en el orden en que se lee. Una parte puede darse como el resumen que sustituye a los mensajes que abarca.{{/if}}

{{story}}

### Pautas
1. Describe el libro que realmente existe. No inventes sucesos, personajes ni lugares y no continúes la historia.
2. El nombre es corto y fácil de recordar, en el idioma de la historia. No desvela el final. No repitas sin más el nombre que tiene ahora el libro, salvo que de verdad sea el mejor.
3. La descripción es lo que ve un lector que hojea una biblioteca: presenta el escenario y los personajes principales, plantea el conflicto central y lo que está en juego, y da ganas de seguir leyendo. Dos o tres párrafos cortos. No destripes el final, no enumeres los capítulos, no te dirijas al lector de «tú».
4. Escribe en el idioma de la historia, con el tono de la historia.
""";
    }

    @Override
    public String notesPrompt() {
        return """
[ Eres un analista de historias minucioso. Tu tarea es leer una parte de una historia larga y escribir notas que alguien que no la ha leído pueda usar para escribir la descripción de todo el libro. ]

### El libro ahora
Nombre: {{bookName}}
{{#if bookDescription}}Descripción: {{bookDescription}}{{/if}}

### La historia, parte {{part}} de {{parts}}
{{story}}

### Pautas
1. Escribe notas densas sobre lo que realmente ocurre en esta parte, en el orden en que ocurre. No inventes nada y no continúes la historia.
2. Nombra a los personajes (con los nombres que usa la historia), quiénes son y qué quieren, los lugares, el conflicto central de esta parte y cómo termina esta parte.
3. Añade el género, el tono y los temas que se ven en esta parte.
4. Conserva lo que necesita una sinopsis y omite los detalles menores. Usa párrafos cortos o viñetas, sin encabezados.
5. Responde solo con las notas. Sin introducción ni comentarios.
""";
    }

    @Override
    public String outputInstructions(OutputFormat format) {
        return switch (format) {
            case AUTO, JSON -> "### Salida\nResponde solo con el objeto JSON que pide el formato de respuesta: el \"name\" del libro y su \"description\". Sin introducción ni comentarios.";
            case JSON_NO_SCHEMA -> "### Salida\nResponde solo con un objeto JSON exactamente de esta forma, nada antes y nada después:\n"
                    + "{\"name\": \"<el nombre del libro>\", \"description\": \"<la descripción del libro>\"}";
            case MARKDOWN -> "### Salida\nResponde en Markdown, nada más: la primera línea es el nombre del libro como encabezado (# Nombre del libro), "
                    + "todo lo que va debajo es la descripción del libro. Sin introducción ni comentarios.";
        };
    }
}
