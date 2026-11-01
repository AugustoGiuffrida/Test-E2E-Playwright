#!/usr/bin/env bash
# Carga las credenciales desde .env, ejecuta EncuestaTest y las retira al terminar.
# Los argumentos extra se pasan a Maven, por ejemplo: ./run.sh -Dheadless=true
set -euo pipefail

cd "$(dirname "$0")"

if [[ ! -f .env ]]; then
    echo "No existe .env. Crealo a partir del ejemplo:" >&2
    echo "  cp .env.example .env && chmod 600 .env" >&2
    exit 1
fi

# JDK instalado con SDKMAN y no cargado en esta shell
if ! command -v java >/dev/null 2>&1 && [[ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]]; then
    set +u
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    set -u
fi

trap 'unset UM_USERNAME UM_PASSWORD' EXIT

set -a
source .env
set +a

./mvnw -Dtest=EncuestaTest "$@" test
