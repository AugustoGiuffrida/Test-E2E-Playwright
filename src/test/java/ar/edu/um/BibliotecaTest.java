package ar.edu.um;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class BibliotecaTest {

    private static final String URL_BIBLIOTECA = "https://www.um.edu.ar/biblio/";

    private static final String ENLACE_PORTAL = "Ir al Portal de Biblioteca UM";
    private static final String ENLACE_CATALOGO = "Consultá nuestro catálogo";
    private static final String ENLACE_BUSQUEDA_AVANZADA = "Búsqueda avanzada";
    private static final String BOTON_BUSCAR = "Buscar";
    private static final String LIBRO = "UML y Patrones";
    private static final String CANTIDAD_RESULTADOS = "Su búsqueda retornó";
    private static final String TABLA_RESULTADOS = "Resultados";
    private static final String TITULO_LIBRO = "UML y patrones";
    private static final String VERIFICACION_FALLIDA = "Verificación fallida";
    private static final Pattern ISBN_LIBRO = Pattern.compile("^84-?205-?3438-?2$");

    @Test
    void encuentraElLibroUmlYPatrones() {
        boolean headless = Boolean.getBoolean("headless");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(headless).setSlowMo(1000));
            try {
                BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions().setViewportSize(1280, 800));
                try {
                    Page page = context.newPage();

                    // 1. Entrar a la pagina de la biblioteca
                    page.navigate(URL_BIBLIOTECA,
                        new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                    abrirCatalogo(page);       // pasos 2 a 4
                    buscarLibro(page);         // pasos 5 y 6
                    verificarResultados(page); // pasos 7 a 10
                    verificarIsbn(page);       // paso 11

                    // 12. Dejar la ficha en pantalla unos segundos antes de cerrar. Es solo
                    //     para mirarla: las verificaciones ya terminaron y no dependen de esto
                    page.waitForTimeout(5000);
                } finally {
                    context.close();
                }
            } finally {
                browser.close();
            }
        }
    }

    private static void abrirCatalogo(Page page) {
        // 2. Hacer clic en "Ir al Portal de Biblioteca UM"
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(ENLACE_PORTAL).setExact(true)).click();

        // 3. Hacer clic en "Consulta nuestro catalogo"
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(ENLACE_CATALOGO).setExact(true)).click();

        // 4. Confirmar que ya se esta en el catalogo: el portal tambien tiene un cuadro de
        //    texto (busqueda de noticias) y no hay que escribir ahi
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(ENLACE_BUSQUEDA_AVANZADA).setExact(true))).isVisible();
    }

    private static void buscarLibro(Page page) {
        // 5. Escribir el titulo en el cuadro de busqueda (no tiene etiqueta; es el unico
        //    cuadro de texto del catalogo)
        page.getByRole(AriaRole.TEXTBOX).fill(LIBRO);

        // 6. Hacer clic en "Buscar"
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(BOTON_BUSCAR).setExact(true)).click();
    }

    private static void verificarResultados(Page page) {
        Locator cantidad = page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(CANTIDAD_RESULTADOS).setLevel(1));

        // 7. Esperar los resultados o el rechazo de la verificacion anti-bots. El catalogo
        //    pasa la busqueda por esa verificacion, que puede tardar unos segundos y que
        //    rechaza a los navegadores automatizados. La prueba no la elude: si la rechaza,
        //    guarda una captura y falla informandolo.
        Locator rechazo = page.getByText(VERIFICACION_FALLIDA);
        assertThat(cantidad.or(rechazo).first())
            .isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(15_000));
        if (rechazo.isVisible()) {
            page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("target", "evidencia", "biblioteca-rechazo.png"))
                .setFullPage(true));
            fail("El catálogo de la biblioteca rechazó la búsqueda con su verificación anti-bots ("
                + rechazo.first().innerText().trim() + "). La prueba no intenta eludirla.");
        }

        // 8. Comprobar el encabezado "Su busqueda retorno N resultados."
        assertThat(cantidad).isVisible();

        // 9. Comprobar que en la tabla de resultados haya un titulo con "UML y patrones" y
        //    guardar la captura
        Locator resultados = page.getByRole(AriaRole.TABLE,
            new Page.GetByRoleOptions().setName(TABLA_RESULTADOS).setExact(true));
        Locator libro = resultados.getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(TITULO_LIBRO)).first();
        assertThat(libro).isVisible();

        page.screenshot(new Page.ScreenshotOptions()
            .setPath(Paths.get("target", "evidencia", "biblioteca-resultados.png"))
            .setFullPage(true));

        // 10. Abrir la ficha del libro
        libro.click();
    }

    private static void verificarIsbn(Page page) {
        // 11. Comprobar en la ficha que el ISBN sea el de UML y patrones y guardar la captura.
        //     En la ficha, Koha marca el ISBN con el atributo property="isbn"
        assertThat(page.locator("[property='isbn']").first()).hasText(ISBN_LIBRO);

        page.screenshot(new Page.ScreenshotOptions()
            .setPath(Paths.get("target", "evidencia", "biblioteca-ficha.png"))
            .setFullPage(true));
    }
}
