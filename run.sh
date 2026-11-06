#!/usr/bin/env bash
# Ejecuta BibliotecaTest. No necesita credenciales.
# Los argumentos extra se pasan a Maven, por ejemplo: ./run.sh -Dheadless=true
set -euo pipefail

cd "$(dirname "$0")"

# JDK instalado con SDKMAN y no cargado en esta shell. Se comprueba que java funcione y no
# solo que exista: macOS trae un /usr/bin/java que, sin un JDK configurado, solo muestra
# "Unable to locate a Java Runtime"
if ! java -version >/dev/null 2>&1 && [[ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]]; then
    set +u
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    set -u
fi

./mvnw -Dtest=BibliotecaTest "$@" test
