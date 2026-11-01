package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaIcons;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The icon of a persona is a PNG that fits the limit, and a persona without one gets a circle with the first letters of its name.
 */
class PersonaIconsTest {

    private static byte[] png(int width, int height) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", out);
        return out.toByteArray();
    }

    private static BufferedImage decoded(String icon) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(icon)));
    }

    @Test
    void aLargeImageIsScaledDownKeepingItsProportions() throws Exception {
        BufferedImage icon = decoded(PersonaIcons.createIcon(png(1000, 500)));

        assertEquals(PersonaIcons.MAX_ICON_SIZE, icon.getWidth());
        assertEquals(PersonaIcons.MAX_ICON_SIZE / 2, icon.getHeight());
    }

    @Test
    void aSmallImageIsKept() throws Exception {
        BufferedImage icon = decoded(PersonaIcons.createIcon(png(40, 30)));

        assertEquals(40, icon.getWidth());
        assertEquals(30, icon.getHeight());
    }

    @Test
    void somethingThatIsNotAnImageIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> PersonaIcons.createIcon("not an image".getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalArgumentException.class, () -> PersonaIcons.createIcon(new byte[0]));
    }

    @Test
    void aPersonaWithAnIconShowsIt() throws Exception {
        Persona persona = new Persona("Athena", "text");
        persona.setIcon(PersonaIcons.createIcon(png(20, 20)));

        assertEquals("data:image/png;base64," + persona.getIcon(), PersonaIcons.source(persona));
    }

    @Test
    void aPersonaWithoutAnIconGetsACircleWithTheFirstLettersOfItsName() {
        Persona athena = new Persona("Athena", "text");
        Persona twoWords = new Persona("Sable Quick", "text");

        assertEquals("At", PersonaIcons.letters(athena));
        assertEquals("SQ", PersonaIcons.letters(twoWords));
        String source = PersonaIcons.source(athena);
        assertTrue(source.startsWith("data:image/svg+xml;base64,"));
        String svg = new String(Base64.getDecoder().decode(source.substring(source.indexOf(',') + 1)), StandardCharsets.UTF_8);
        assertTrue(svg.contains(">At</text>"));
        // the same persona has the same circle, another one has its own color
        assertEquals(source, PersonaIcons.source(athena));
        assertNotEquals(source, PersonaIcons.source(new Persona("Athena", "text")));
    }

    @Test
    void aStoredIconThatIsNotBase64FallsBackToTheCircle() {
        Persona persona = new Persona("Vesper", "text");
        persona.setIcon("<script>alert(1)</script>");

        assertTrue(PersonaIcons.source(persona).startsWith("data:image/svg+xml;base64,"));
    }

    @Test
    void aNameWithoutLettersGetsAQuestionMark() {
        assertEquals("?", PersonaIcons.letters(new Persona("", "text")));
        assertEquals("?", PersonaIcons.letters(new Persona(null, "text")));
    }
}
