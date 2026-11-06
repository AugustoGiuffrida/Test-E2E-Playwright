#!/usr/bin/env bash
# Ejecuta BibliotecaTest. No necesita credenciales.
# Los argumentos extra se pasan a Maven, por ejemplo: ./run.sh -Dheadless=true
set -euo pipefail

cd "$(dirname "$0")"

# JDK instalado con SDKMAN y no cargado en esta shell
if ! command -v java >/dev/null 2>&1 && [[ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]]; then
    set +u
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    set -u
fi

./mvnw -Dtest=BibliotecaTest "$@" test
