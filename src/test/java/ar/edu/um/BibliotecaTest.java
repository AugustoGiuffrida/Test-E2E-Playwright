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
    private static final Pattern CANTIDAD_RESULTADOS = Pattern.compile("^Su búsqueda retornó \\d+ resultados?\\.$");
    private static final String TABLA_RESULTADOS = "Resultados";
    private static final Pattern TITULO_LIBRO = Pattern.compile("^UML y patrones\\b", Pattern.CASE_INSENSITIVE);
    private static final String VERIFICACION_FALLIDA = "Verificación fallida";

    @Test
    void encuentraElLibroUmlYPatrones() {
        boolean headless = Boolean.getBoolean("headless");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(headless));
            try {
                BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions().setViewportSize(1280, 800));
                try {
                    Page page = context.newPage();
                    page.navigate(URL_BIBLIOTECA,
                        new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                    abrirCatalogo(page);
                    buscarLibro(page);
                    verificarResultados(page);
                } finally {
                    context.close();
                }
            } finally {
                browser.close();
            }
        }
    }

    private static void abrirCatalogo(Page page) {
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(ENLACE_PORTAL).setExact(true)).click();
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(ENLACE_CATALOGO).setExact(true)).click();

        // El portal también tiene un cuadro de texto (búsqueda de noticias):
        // hay que confirmar que ya se está en el catálogo antes de escribir
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(ENLACE_BUSQUEDA_AVANZADA).setExact(true))).isVisible();
    }

    private static void buscarLibro(Page page) {
        // El cuadro de búsqueda del catálogo no tiene etiqueta; es el único cuadro de texto
        page.getByRole(AriaRole.TEXTBOX).fill(LIBRO);
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(BOTON_BUSCAR).setExact(true)).click();
    }

    private static void verificarResultados(Page page) {
        Locator cantidad = page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(CANTIDAD_RESULTADOS).setLevel(1));

        // El catálogo pasa la búsqueda por una verificación anti-bots que puede tardar unos
        // segundos y que rechaza a los navegadores automatizados. La prueba no la elude:
        // si la rechaza, falla informándolo.
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
        assertThat(cantidad).isVisible();

        Locator resultados = page.getByRole(AriaRole.TABLE,
            new Page.GetByRoleOptions().setName(TABLA_RESULTADOS).setExact(true));
        assertThat(resultados.getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(TITULO_LIBRO)).first()).isVisible();

        page.screenshot(new Page.ScreenshotOptions()
            .setPath(Paths.get("target", "evidencia", "biblioteca.png"))
            .setFullPage(true));
    }
}
