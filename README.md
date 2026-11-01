# Test-E2E-Playwright

Prueba end-to-end con **Playwright para Java** y **JUnit 5** que responde la encuesta
"Resolví el test en Cypress" de la materia **Ingeniería de Software Aplicada (2080)** en las
Cátedras Virtuales de la Universidad de Mendoza.

La prueba parte de `https://um.edu.ar` (la única URL escrita en el código) y hace todo el
recorrido desde la interfaz, como lo haría un alumno:

1. Abre el menú **Estudiantes** → **Cátedras Virtuales** (se abre en una pestaña nueva).
2. Inicia sesión en UMVirtual (Moodle) y verifica que aparezca el **Menú de usuario**.
3. Entra a **Mis cursos** → **Ingeniería de Software Aplicada (2080)** y verifica su encabezado.
4. Expande, si hace falta, la sección **Ejercicio para Test Automático** y abre la encuesta.
5. Pulsa **Responda a las preguntas...**, responde **SI** y **Tercero** y comprueba ambas respuestas.
6. Pulsa **Enviar encuesta** y verifica el mensaje **"Gracias por realizar esta encuesta"**.
7. Entra a **Ver su(s) respuesta(s)** y comprueba que la respuesta más reciente registró SI y Tercero.

Los nombres de cada control salen del relevamiento de la interfaz real:
[docs/relevamiento.md](docs/relevamiento.md).

## Estructura

```text
pom.xml                                     Dependencias y plugins con versiones fijas
mvnw, mvnw.cmd, .mvn/                       Maven Wrapper (Maven 3.10.0)
run.sh                                      Carga .env, ejecuta el test y retira las credenciales
.env.example                                Formato de las variables de entorno
src/test/java/ar/edu/um/EncuestaTest.java   La prueba
docs/relevamiento.md                        Controles reales de cada pantalla
```

| Componente | Versión |
| --- | --- |
| Java (`maven.compiler.release`) | 17 |
| Playwright para Java | 1.64.0 |
| JUnit Jupiter | 5.14.4 |
| maven-compiler-plugin | 3.16.0 |
| maven-surefire-plugin | 3.6.1 |
| exec-maven-plugin | 3.6.4 |

## Requisitos

- **JDK 17 o superior.** Maven no hace falta: el wrapper `./mvnw` lo descarga la primera vez.
- Conexión a Internet.
- Una cuenta de UMVirtual **matriculada** en Ingeniería de Software Aplicada (2080). Una cuenta
  que inicia sesión pero no está matriculada no ve la materia, y la prueba falla al buscarla.

Si no tenés un JDK, una forma de instalarlo sin permisos de administrador es
[SDKMAN](https://sdkman.io/):

```bash
curl -s "https://get.sdkman.io" | bash
```

```bash
sdk install java 21.0.5-tem
```

## Preparación

Instalar el Chromium que usa Playwright (una sola vez):

```bash
./mvnw test-compile exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.classpathScope=test -Dexec.args="install chromium"
```

En Linux, si faltan dependencias del sistema, usar `-Dexec.args="install --with-deps chromium"`
(puede pedir permisos de administrador).

## Credenciales

La prueba lee el usuario y la contraseña de las variables de entorno `UM_USERNAME` y
`UM_PASSWORD`. Si falta alguna, falla antes de abrir el navegador con
`Falta la variable de entorno: ...`. Los valores nunca se imprimen.

- Usuario: `inicial_nombre.apellido`, sin `@alumno.edu.ar`.
- Contraseña: la del correo institucional.

No se observó MFA ni CAPTCHA en el inicio de sesión, por lo que la prueba puede correr de forma
desatendida.

### Opción A: archivo `.env` y `run.sh`

```bash
cp .env.example .env && chmod 600 .env
```

Completar `.env` con comillas simples (una contraseña que contenga `'` no funciona con este
formato; en ese caso usar la opción B):

```text
UM_USERNAME='usuario'
UM_PASSWORD='contraseña'
```

`.env` está en `.gitignore` y nunca se versiona. Guarda la contraseña en texto plano: conviene
borrarlo cuando ya no haga falta.

### Opción B: ingresarlas en la terminal

```bash
read -r -p "Usuario UM: " UM_USERNAME
read -r -s -p "Contraseña UM: " UM_PASSWORD
printf '\n'
export UM_USERNAME UM_PASSWORD
```

En integración continua, definir las mismas variables desde el gestor de secretos del entorno.

## Ejecución

Con la opción A:

```bash
./run.sh
```

`run.sh` carga `.env`, carga SDKMAN si `java` no está en el `PATH`, ejecuta la prueba y hace
`unset UM_USERNAME UM_PASSWORD` al terminar, aunque falle. Los argumentos extra se pasan a Maven.

Con la opción B:

```bash
./mvnw -Dtest=EncuestaTest test
```

```bash
unset UM_USERNAME UM_PASSWORD
```

Por defecto el navegador se ve. Para correr sin ventana:

```bash
./run.sh -Dheadless=true
```

## Resultado y evidencia

- Resultado de Maven en la consola y en `target/surefire-reports/`.
- Captura de la confirmación en `target/evidencia/confirmacion.png`, tomada después de verificar
  el mensaje "Gracias por realizar esta encuesta".

`target/` no se versiona. Antes de compartir capturas o reportes, revisarlos: pueden mostrar
datos de la cuenta.

## Repetir la prueba

- **Cada ejecución envía una respuesta real** a la encuesta.
- La encuesta tiene **intentos ilimitados**, así que la prueba se puede repetir con la misma
  cuenta sin restablecer nada.
- La prueba no depende de respuestas anteriores: lo que acredita el envío es el mensaje de
  confirmación que aparece tras el envío de esa misma ejecución.
- Moodle guarda algunas preferencias por usuario (si la sección está colapsada, cuántas
  respuestas hay). La prueba contempla ambos casos: expande la sección solo si está colapsada y,
  si hay varias respuestas, entra a **Respuestas individuales**, que muestra la más reciente.
- No hay reintentos automáticos configurados: un envío puede quedar guardado aunque falle una
  comprobación posterior, y repetirlo a ciegas registraría respuestas de más.
- Si la cátedra cambia la encuesta a un solo intento, una cuenta que ya respondió no debería
  poder volver a responder, y la prueba fallaría al buscar **Responda a las preguntas...**
  (comportamiento no verificado). Haría falta otra cuenta habilitada o que la cátedra
  restablezca el intento.
