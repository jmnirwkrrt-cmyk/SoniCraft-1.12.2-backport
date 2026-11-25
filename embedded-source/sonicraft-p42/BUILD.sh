#!/bin/bash
# Run with Java 8 javac/java/jar on PATH.
# Args: P41.jar mapped-forge.jar geckolib.jar mcp-to-srg.tsrg tools-classpath output.jar
# tools-classpath: ASM 6 core/tree/commons/analysis, Guava and the normal Forge compile dependencies.
set -eu
BASE="$1"; MC="$2"; GECKO="$3"; MAP="$4"; DEPS="$5"; OUT="$6"
ROOT="$(cd "$(dirname "$0")" && pwd)"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$WORK/classes" "$WORK/tool" "$WORK/patch"
javac -source 8 -target 8 -cp "$DEPS" -d "$WORK/tool" "$ROOT/Remap.java" "$ROOT/Patch.java" "$ROOT/LinkCheck.java"
TOOL="$WORK/tool:$DEPS"
java -cp "$TOOL" Remap "$BASE" "$WORK/base-mcp.jar" "$MAP" reverse
javac -source 8 -target 8 -cp "$MC:$WORK/base-mcp.jar:$GECKO:$DEPS" -d "$WORK/classes" "$ROOT"/src/org/sonicraft/backport/*.java
jar cf "$WORK/helper-mcp.jar" -C "$WORK/classes" .
java -cp "$TOOL" Remap "$WORK/helper-mcp.jar" "$WORK/helper-srg.jar" "$MAP" forward "$MC" "$WORK/base-mcp.jar" "$WORK/helper-mcp.jar"
java -cp "$TOOL" Remap "$MC" "$WORK/mc-srg.jar" "$MAP" forward "$MC"
java -cp "$TOOL" Patch "$BASE" "$WORK/patch"
jar cf "$WORK/patch.jar" -C "$WORK/patch" .
java -cp "$TOOL" LinkCheck "$WORK/helper-srg.jar" "$WORK/mc-srg.jar" "$BASE" "$GECKO"
java -cp "$TOOL" LinkCheck "$WORK/patch.jar" "$WORK/mc-srg.jar" "$BASE" "$WORK/helper-srg.jar" "$GECKO"
python3 "$ROOT/pack.py" "$BASE" "$OUT" "$WORK/helper-srg.jar" "$WORK/patch/org/sonicraft/backport/LegacyCharacter.class"
