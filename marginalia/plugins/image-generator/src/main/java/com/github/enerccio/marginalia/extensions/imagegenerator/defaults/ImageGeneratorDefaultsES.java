package com.github.enerccio.marginalia.extensions.imagegenerator.defaults;

/** The built-in prompt texts of the Image Generator in Spanish; the image prompt itself is still asked for in English. */
public class ImageGeneratorDefaultsES extends ImageGeneratorDefaults {

    @Override
    public String tagsInstruction() {
        return "Escribes prompts para un modelo de generación de imágenes. A partir de la escena de abajo, escribe un prompt que describa la imagen de su momento más visual: quién está (aspecto, ropa, postura), dónde, la luz y el ambiente. Usa frases cortas separadas por comas, sin oraciones, sin nombres de personas (descríbelas), sin texto en la imagen. Escribe el prompt en inglés, sea cual sea el idioma de la escena. Responde solo con el prompt.";
    }

    @Override
    public String sentencesInstruction() {
        return "Escribes prompts para un modelo de generación de imágenes que entiende el lenguaje natural. A partir de la escena de abajo, escribe un prompt para la imagen de su momento más visual: un solo párrafo de tres a seis frases descriptivas sencillas. Di quién está (edad, aspecto, ropa, postura, expresión) y qué hace, dónde ocurre, cómo está encuadrada la imagen (primer plano, plano general, desde qué lado), la luz y el ambiente. Describe a las personas por su aspecto, nunca por su nombre; describe solo lo que se ve; sin texto ni letras en la imagen; sin listas, etiquetas ni títulos. Escribe el prompt en inglés, sea cual sea el idioma de la escena. Responde solo con el prompt.";
    }

    @Override
    public String sceneSetting() {
        return "Ambientación de la escena";
    }

    @Override
    public String pointOfView() {
        return "Punto de vista";
    }

    @Override
    public String charactersPresent() {
        return "Personajes presentes";
    }

    @Override
    public String text() {
        return "Texto";
    }

    @Override
    public String detailsHeader() {
        return "Detalles que la imagen debe seguir";
    }

    @Override
    public String styleHeader() {
        return "La imagen se hace en este estilo. Empieza el prompt con él y mantén cada frase fiel a él: sin términos de fotografía ni de cámara (foto, cinematográfico, lente, profundidad de campo) salvo que el estilo sea una fotografía, y sin otro medio";
    }
}
