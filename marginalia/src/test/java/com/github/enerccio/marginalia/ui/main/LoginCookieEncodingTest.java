package com.github.enerccio.marginalia.ui.main;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginCookieEncodingTest {

    @Test
    void loginRoundTripsAndFitsInACookieValue() {
        for (String login : new String[]{"John Smith", "a,b;c\"d\\e", "Žaneta", "山田 太郎", "a+b", "100%", "plain"}) {
            String encoded = LoginCheckRoute.encodeCookieLogin(login);
            assertTrue(encoded.chars().allMatch(c -> c > 32 && c < 127 && ",;\"\\ ".indexOf(c) < 0),
                    "not a valid cookie value: " + encoded);
            assertEquals(login, LoginCheckRoute.decodeCookieLogin(encoded));
        }
    }

    @Test
    void plainLoginIsUnchanged() {
        assertEquals("plain", LoginCheckRoute.encodeCookieLogin("plain"));
    }

    @Test
    void malformedCookieIsKeptAsIs() {
        assertEquals("100%", LoginCheckRoute.decodeCookieLogin("100%"));
    }
}
