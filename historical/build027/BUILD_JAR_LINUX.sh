#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
echo "Building SoniCraft Legacy 0.1.27 (Minecraft 1.12.2)"
echo "JDK 8 and access to Forge/Gradle dependencies are required."
java -version
./gradlew --no-daemon --stacktrace build batch019Check batch020Check batch021Check batch022Check batch023Check batch024Check batch025Check batch026Check batch027Check
find build/libs -maxdepth 1 -type f -name '*.jar' -printf '%f\n'
