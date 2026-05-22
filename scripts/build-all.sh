#!/usr/bin/env bash
# Build all Spring services (JARs) then docker images.
# Requires: JDK 21 (set JAVA_HOME if needed), Maven, Docker.

set -euo pipefail

cd "$(dirname "$0")/.."

# Auto-detect JDK 21 on macOS if not set
if [[ -z "${JAVA_HOME:-}" ]] && command -v /usr/libexec/java_home >/dev/null 2>&1; then
  if /usr/libexec/java_home -v 21 >/dev/null 2>&1; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
    export PATH="$JAVA_HOME/bin:$PATH"
    echo ">>> JAVA_HOME=$JAVA_HOME"
  fi
fi

echo "==> 1) Build Maven (Spring services)"
mvn -B -pl gateway,auth-service,chat-service,token-service,document-service,payroll-service \
    -am package -DskipTests

echo "==> 2) Build Docker images"
docker compose --profile app build

echo
echo "✅ Build OK. Démarrer maintenant avec :"
echo "   docker compose --profile app up -d"

