# Relevamiento de la interfaz

Relevado el 2026-11-01 recorriendo el flujo a mano desde `https://um.edu.ar`.
Los identificadores numéricos que aparecen acá sirven solo para confirmar que se trata de la
actividad correcta; el test no los usa para navegar ni para seleccionar elementos.

## 1. Sitio institucional (`um.edu.ar`)

| Punto | Observación |
| --- | --- |
| Redirección | `https://um.edu.ar` redirige a `https://www.um.edu.ar/`. |
| Carga | El evento `load` tarda ~8 s (video de portada de ~3 MB). Conviene esperar `DOMCONTENTLOADED` y luego el menú. |
| Menú | Navegación principal con desplegables `Institucional`, `UM+`, `Carreras`, `Estudiantes`, `Docentes` (enlaces con `data-bs-toggle="dropdown"`). |
| Enlace al campus | **"Cátedras Virtuales"**. Aparece 6 veces en el HTML: barra superior, menú `Estudiantes`, menú `Docentes` y versiones móviles ocultas. |
| Camino elegido | Abrir el menú **"Estudiantes"** → enlace **"Cátedras Virtuales"** dentro de ese desplegable (`ul#menu-estudiantes`). |
| Pestaña | El enlace de `Estudiantes` tiene `target="_blank"`: **abre una pestaña nueva** → `waitForPopup`. (El de `Docentes` abre en la misma pestaña.) |

## 2. Login (UMVirtual, Moodle)

| Punto | Observación |
| --- | --- |
| Plataforma | Moodle, título "Entrar al sitio \| UMVirtual". |
| Usuario | Campo con `<label>` **"Nombre de usuario"** (`#username`). Formato: `inicial_nombre.apellido`, sin dominio. |
| Contraseña | Campo con `<label>` **"Contraseña"** (`#password`). Hay además un botón "Alternar sensibilidad" (mostrar/ocultar). |
| Botón | **"Acceder"** (`type="submit"`). |
| MFA / CAPTCHA | No se observaron. |
| Tras el login | Redirige a la **Página Principal** del campus (no al Área personal). |
| Indicador de sesión | Botón **"Menú de usuario"** en la barra superior. (El saludo "¡Hola, …!" depende de la cuenta y no se usa.) |

## 3. Acceso a la materia

| Punto | Observación |
| --- | --- |
| Navegación | Ítem de menú **"Mis cursos"** (`role=menuitem`) en la barra superior. |
| Carga | La lista de cursos se carga por AJAX después de la página. |
| Vista | Filtro "Todos", orden por nombre, vista "Tarjeta". Estas preferencias se guardan **por usuario** en Moodle. |
| Materia | Enlace **"Ingeniería de Software Aplicada (2080)"** (nombre exacto). Una sola comisión. |
| Ambigüedad | Existe otro curso **"INGENIERIA DE SOFTWARE"**: usar coincidencia exacta. Cada tarjeta tiene dos enlaces al curso; sólo uno tiene el nombre exacto. |
| Encabezado del curso | `h1` **"Ingeniería de Software Aplicada (2080)"**. |
| Alternativa | Buscador **"Buscar cursos"** en la Página Principal (no depende de las preferencias de "Mis cursos"). |

## 4. Sección y encuesta

| Punto | Observación |
| --- | --- |
| Sección | **"Ejercicio para Test Automático"**. Es la última sección con contenido; después siguen "Seguridad API - GraphQL" y "Logs y Programación Orientada a Aspectos", ambas sin actividades. |
| Colapso | Cada sección tiene un botón con el nombre de la sección y `aria-expanded`. El estado se guarda por usuario: expandir solo si `aria-expanded="false"`. |
| Actividad | **"Resolví el test en Cypress"**, tipo **Encuesta** (plugin *Questionnaire*, no *Feedback*). Corresponde al id `210589`. |
| Atención | El índice lateral del curso (oculto) tiene otro enlace con el mismo nombre: acotar a la sección. |
| Título de la actividad | `h1`/título **"Resolví el test en Cypress"**. |
| Intentos | **"Intentos permitidos: Sin límite"** → la encuesta admite varias respuestas; la prueba es repetible. |
| Botón para responder | **"Responda a las preguntas..."**: existe como `link` en el cuerpo y como `menuitem` en la navegación secundaria. Usar el `link`. |

## 5. Formulario

Una sola página, formulario `#phpesp_response`. Cada pregunta es un `<fieldset class="qn-container">` cuyo
`<legend>` le da nombre accesible (rol `group`).

| # | Grupo (legend) | Tipo | Opciones | Obligatoria |
| --- | --- | --- | --- | --- |
| 1 | "Pregunta #1 ¿Es Cypress una herramienta para escribir test E2E?" | Radio | `SI`, `NO` | Sí |
| 2 | "Pregunta #2 ¿puedes seleccionar el tercer elemento de la lista?" | Lista desplegable | `Elegir...` (vacía), `Primero`, `Segundo`, `Tercero` | Sí |

Notas:

- Los radios tienen `<label>` "SI" / "NO" y `aria-label` con la pregunta + la opción. El `aria-label`
  prevalece: `getByLabel("SI")` no encuentra nada; hay que usar `getByRole(RADIO)` con el nombre completo
  (por ejemplo "¿Es Cypress una herramienta para escribir test E2E? SI").
- El nombre accesible calculado del desplegable es "Elegir..." (su `<label>` no se asocia de forma fiable): localizarlo como `combobox` dentro del grupo de la pregunta 2.
- Botón de envío: **"Enviar encuesta"** (`input type="submit"`).

## 6. Después del envío

Observado en la primera ejecución del test que envió la encuesta (2026-11-01).

| Punto | Observación |
| --- | --- |
| Confirmación | Encabezado **"Gracias por realizar esta encuesta"** y botón **"Continuar"**, en la misma página del formulario. |
| Navegación de la encuesta | Aparecen los ítems **"Ver su(s) respuesta(s)"** y **"Ver todas las respuestas"** (`role=menuitem`). |
| Vista de respuestas (1 respuesta) | "Ver su(s) respuesta(s)" muestra directamente "Encuestado", "Enviado el: …" y el mismo formulario **deshabilitado** con lo registrado: radio `SI` marcado y `Tercero` seleccionado. Los grupos y nombres accesibles son los mismos que en el formulario. |
| Vista de respuestas (2 o más) | "Ver su(s) respuesta(s)" abre un **Resumen** ("Sus resultados para N respuestas", tablas por pregunta) con los enlaces "Respuestas individuales" y "Todas sus respuestas". **"Respuestas individuales"** muestra por defecto la respuesta **más reciente** (paginador "Anterior \| 1 \| 2"), con el mismo formulario deshabilitado. |
