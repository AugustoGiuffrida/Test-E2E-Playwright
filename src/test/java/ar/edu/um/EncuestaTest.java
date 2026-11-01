package ar.edu.um;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class EncuestaTest {

    private static final String URL_INICIO = "https://um.edu.ar";

    @Test
    void respondeLaEncuesta() {
        String username = requiredEnv("UM_USERNAME");
        String password = requiredEnv("UM_PASSWORD");
        boolean headless = Boolean.getBoolean("headless");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(headless));
            try {
                BrowserContext context = browser.newContext();
                try {
                    Page page = context.newPage();
                    page.navigate(URL_INICIO);
                    assertThat(page).hasURL(Pattern.compile("^https://(www\\.)?um\\.edu\\.ar/"));

                    // Pendiente de relevamiento: cátedras virtuales, login, materia, encuesta y envío.
                } finally {
                    context.close();
                }
            } finally {
                browser.close();
            }
        }
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno: " + name);
        }
        return value;
    }
}
