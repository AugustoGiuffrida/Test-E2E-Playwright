# Test-E2E-Playwright: biblioteca

Prueba end-to-end con **Playwright para Java** y **JUnit 5** para la consigna de la firma de
regularidad: buscar el libro *UML y Patrones* en la biblioteca de la Universidad de Mendoza y
comprobar que exista. No necesita credenciales.

Recorrido:

1. Entra a `https://www.um.edu.ar/biblio/` (la dirección que indica la cátedra).
2. **Ir al Portal de Biblioteca UM** → **Consultá nuestro catálogo** (catálogo Koha,
   `biblioteca.um.edu.ar`). Antes de escribir confirma que ya está en el catálogo, porque el
   portal también tiene un cuadro de texto (búsqueda de noticias).
3. Escribe **UML y Patrones** en el cuadro de búsqueda (no tiene etiqueta; es el único cuadro
   de texto del catálogo) y pulsa **Buscar**.
4. Verifica el encabezado "Su búsqueda retornó N resultados." y que la tabla **Resultados**
   tenga un título que empiece con "UML y patrones".
5. Abre la ficha de ese libro y verifica que el ISBN sea el de *UML y patrones* 2a ed.
   (84-205-3438-2). El catálogo lo tiene cargado con guiones en un registro y sin guiones en
   otro (8420534382); la prueba acepta las dos formas.

En el código, cada acción está numerada en un comentario (pasos 1 a 11).

## Estructura

```text
pom.xml                                       Dependencias y plugins con versiones fijas
mvnw, mvnw.cmd, .mvn/                         Maven Wrapper (Maven 3.10.0)
run.sh                                        Ejecuta la prueba
src/test/java/ar/edu/um/BibliotecaTest.java   La prueba
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

## Ejecución

```bash
./run.sh
```

`run.sh` carga SDKMAN si `java` no funciona (en macOS, `/usr/bin/java` existe aunque no haya un
JDK configurado) y ejecuta `./mvnw -Dtest=BibliotecaTest test`.
Los argumentos extra se pasan a Maven. Por defecto el navegador se ve; para correr sin ventana:

```bash
./run.sh -Dheadless=true
```

## Resultado y evidencia

- Resultado de Maven en la consola y en `target/surefire-reports/`.
- Si la búsqueda funciona: captura de los resultados en
  `target/evidencia/biblioteca-resultados.png` y de la ficha del libro con el ISBN en
  `target/evidencia/biblioteca-ficha.png`.
- Si el catálogo rechaza la búsqueda: captura del rechazo en
  `target/evidencia/biblioteca-rechazo.png`.
- Si en 15 segundos no aparecen ni los resultados ni el rechazo: captura de lo que quedó en
  pantalla en `target/evidencia/biblioteca-espera.png`, y el mensaje de error indica la
  dirección de la página.

`target/` no se versiona.

## Limitación: verificación anti-bots del catálogo

El catálogo puede pasar la búsqueda por una verificación anti-bots (`/verify-captcha`). Con un
navegador común se resuelve sola en unos segundos ("Verificando acceso…"). El 2026-11-01, a
Chromium controlado por Playwright le respondía **"Verificación fallida, por favor intentá de
nuevo"** y no mostraba resultados. El 2026-11-12 la búsqueda llegó directo a los resultados y
la prueba pasó con Chromium sin ventana.

La prueba **no intenta eludir** esa verificación. Espera hasta 15 segundos los resultados o el
rechazo; si el catálogo la rechaza, guarda la captura del rechazo y falla con este mensaje:

```text
El catálogo de la biblioteca rechazó la búsqueda con su verificación anti-bots
(Verificación fallida, por favor intentá de nuevo). La prueba no intenta eludirla.
```

Si el catálogo vuelve a rechazar a los navegadores automatizados, la prueba no puede pasar de
forma automatizada; hay que consultarlo con la cátedra.

## Detalle: el encabezado de resultados

El encabezado "Su búsqueda retornó N resultados." contiene un enlace RSS ("Suscribirse a esta
búsqueda"), y ese texto también forma parte del nombre accesible del encabezado. Por eso la
expresión regular de la prueba exige que el nombre **empiece** con "Su búsqueda retornó N
resultados." y no que termine ahí.
