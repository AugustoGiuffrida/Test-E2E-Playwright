package ar.edu.um;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class EncuestaTest {

    private static final String URL_INICIO = "https://um.edu.ar";

    // Nombres visibles relevados en docs/relevamiento.md
    private static final String MENU_ESTUDIANTES = "Estudiantes";
    private static final String ENLACE_CATEDRAS = "Cátedras Virtuales";
    private static final String CAMPO_USUARIO = "Nombre de usuario";
    private static final String CAMPO_CONTRASENA = "Contraseña";
    private static final String BOTON_ACCEDER = "Acceder";
    private static final String MENU_USUARIO = "Menú de usuario";
    private static final String MIS_CURSOS = "Mis cursos";
    private static final String MATERIA = "Ingeniería de Software Aplicada (2080)";
    private static final String SECCION = "Ejercicio para Test Automático";
    private static final String ENCUESTA = "Resolví el test en Cypress";
    private static final String ENLACE_RESPONDER = "Responda a las preguntas...";
    private static final String PREGUNTA_1 = "¿Es Cypress una herramienta para escribir test E2E?";
    private static final String RESPUESTA_1 = "SI";
    private static final String PREGUNTA_2 = "¿puedes seleccionar el tercer elemento de la lista?";
    private static final String RESPUESTA_2 = "Tercero";
    private static final String BOTON_ENVIAR = "Enviar encuesta";
    private static final String MENSAJE_CONFIRMACION = "Gracias por realizar esta encuesta";
    private static final String VER_RESPUESTAS = "Ver su(s) respuesta(s)";
    private static final String RESPUESTAS_INDIVIDUALES = "Respuestas individuales";

    @Test
    void respondeLaEncuesta() {
        String username = requiredEnv("UM_USERNAME");
        String password = requiredEnv("UM_PASSWORD");
        boolean headless = Boolean.getBoolean("headless");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(headless));
            try {
                BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions().setViewportSize(1280, 800));
                try {
                    Page page = context.newPage();
                    // La portada carga un video pesado: alcanza con el DOM para usar el menú
                    page.navigate(URL_INICIO,
                        new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                    Page campus = abrirCatedrasVirtuales(page);
                    iniciarSesion(campus, username, password);
                    abrirMateria(campus);
                    abrirEncuesta(campus);
                    responderPreguntas(campus);
                    enviarYVerificar(campus);
                } finally {
                    context.close();
                }
            } finally {
                browser.close();
            }
        }
    }

    private static Page abrirCatedrasVirtuales(Page page) {
        Locator menuEstudiantes = page.getByRole(AriaRole.LISTITEM).filter(
            new Locator.FilterOptions().setHas(page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName(MENU_ESTUDIANTES).setExact(true))));

        menuEstudiantes.getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(MENU_ESTUDIANTES).setExact(true)).click();

        // El enlace del menú Estudiantes abre el campus en una pestaña nueva
        return page.waitForPopup(() ->
            menuEstudiantes.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName(ENLACE_CATEDRAS).setExact(true)).click());
    }

    private static void iniciarSesion(Page campus, String username, String password) {
        campus.getByLabel(CAMPO_USUARIO, new Page.GetByLabelOptions().setExact(true)).fill(username);
        campus.getByLabel(CAMPO_CONTRASENA, new Page.GetByLabelOptions().setExact(true)).fill(password);
        campus.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(BOTON_ACCEDER).setExact(true)).click();

        assertThat(campus.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(MENU_USUARIO).setExact(true))).isVisible();
    }

    private static void abrirMateria(Page campus) {
        campus.getByRole(AriaRole.MENUITEM,
            new Page.GetByRoleOptions().setName(MIS_CURSOS).setExact(true)).click();

        // La lista de cursos se carga por AJAX; el clic espera a que aparezca el enlace
        campus.getByRole(AriaRole.MAIN).getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(MATERIA).setExact(true)).click();

        assertThat(campus.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(MATERIA).setExact(true).setLevel(1))).isVisible();
    }

    private static void abrirEncuesta(Page campus) {
        Locator contenido = campus.getByRole(AriaRole.MAIN);
        Locator alternarSeccion = contenido.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName(SECCION).setExact(true));

        // Moodle recuerda por usuario si la sección quedó colapsada
        if ("false".equals(alternarSeccion.getAttribute("aria-expanded"))) {
            alternarSeccion.click();
        }
        assertThat(alternarSeccion).hasAttribute("aria-expanded", "true");

        // El locator de setHas se evalúa dentro de cada <li>, por eso no parte de contenido
        Locator seccion = contenido.getByRole(AriaRole.LISTITEM).filter(
            new Locator.FilterOptions().setHas(campus.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(SECCION).setExact(true))));
        seccion.getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(ENCUESTA)).click();

        assertThat(campus.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(ENCUESTA).setExact(true).setLevel(1))).isVisible();

        campus.getByRole(AriaRole.MAIN).getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(ENLACE_RESPONDER).setExact(true)).click();
    }

    private static void responderPreguntas(Page campus) {
        Locator opcion1 = opcionPregunta1(campus);
        opcion1.check();

        Locator lista2 = listaPregunta2(campus);
        lista2.selectOption(new SelectOption().setLabel(RESPUESTA_2));

        assertThat(opcion1).isChecked();
        assertThat(lista2.locator("option:checked")).hasText(RESPUESTA_2);
    }

    private static void enviarYVerificar(Page campus) {
        campus.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(BOTON_ENVIAR).setExact(true)).click();

        // Este encabezado solo aparece como resultado del envío de esta ejecución
        assertThat(campus.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(MENSAJE_CONFIRMACION).setExact(true))).isVisible();
        campus.screenshot(new Page.ScreenshotOptions()
            .setPath(Paths.get("target", "evidencia", "confirmacion.png"))
            .setFullPage(true));

        campus.getByRole(AriaRole.MENUITEM,
            new Page.GetByRoleOptions().setName(VER_RESPUESTAS).setExact(true)).click();

        // Con una sola respuesta Moodle la muestra directamente; con varias abre un resumen
        // y "Respuestas individuales" muestra la más reciente
        Locator respuestasIndividuales = campus.getByRole(AriaRole.MAIN).getByRole(AriaRole.LINK,
            new Locator.GetByRoleOptions().setName(RESPUESTAS_INDIVIDUALES).setExact(true));
        assertThat(respuestasIndividuales.or(opcionPregunta1(campus))).isVisible();
        if (respuestasIndividuales.isVisible()) {
            respuestasIndividuales.click();
        }

        // La vista de la respuesta reutiliza el formulario, deshabilitado, con lo registrado
        assertThat(opcionPregunta1(campus)).isChecked();
        assertThat(listaPregunta2(campus).locator("option:checked")).hasText(RESPUESTA_2);
    }

    // Cada radio tiene aria-label "pregunta + opción", que prevalece sobre su <label> visible
    private static Locator opcionPregunta1(Page campus) {
        return campus.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName(PREGUNTA_1))
            .getByRole(AriaRole.RADIO,
                new Locator.GetByRoleOptions().setName(PREGUNTA_1 + " " + RESPUESTA_1).setExact(true));
    }

    private static Locator listaPregunta2(Page campus) {
        return campus.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName(PREGUNTA_2))
            .getByRole(AriaRole.COMBOBOX);
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno: " + name);
        }
        return value;
    }
}
