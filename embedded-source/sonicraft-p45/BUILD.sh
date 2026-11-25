#!/bin/bash
# Java 8 tools on PATH. Args: P44.jar mapped-forge.jar geckolib.jar MCP-to-SRG.tsrg tools-classpath output.jar
set -eu
BASE="$1"; MC="$2"; GECKO="$3"; MAP="$4"; DEPS="$5"; OUT="$6"
ROOT="$(cd "$(dirname "$0")" && pwd)"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
mkdir -p "$WORK/classes" "$WORK/tool" "$WORK/patch"
javac -source 8 -target 8 -cp "$DEPS" -d "$WORK/tool" "$ROOT/Remap.java" "$ROOT/Patch45.java" "$ROOT/LinkCheck.java"
TOOL="$WORK/tool:$DEPS"
java -cp "$TOOL" Remap "$BASE" "$WORK/base-mcp.jar" "$MAP" reverse
javac -sourcepath "$ROOT/src" -source 8 -target 8 -cp "$MC:$WORK/base-mcp.jar:$GECKO:$DEPS" -d "$WORK/classes" "$ROOT"/src/org/sonicraft/backport/*.java
javac -source 8 -target 8 -cp "$WORK/classes" -d "$WORK/tool" "$ROOT/TransformTest.java"
java -cp "$WORK/classes:$WORK/tool" TransformTest
jar cf "$WORK/helper-mcp.jar" -C "$WORK/classes" .
java -cp "$TOOL" Remap "$WORK/helper-mcp.jar" "$WORK/helper-srg.jar" "$MAP" forward "$MC" "$WORK/base-mcp.jar" "$WORK/helper-mcp.jar"
java -cp "$TOOL" Patch45 "$WORK/base-mcp.jar" "$WORK/patch"
jar cf "$WORK/patch-mcp.jar" -C "$WORK/patch" .
java -cp "$TOOL" Remap "$WORK/patch-mcp.jar" "$WORK/patch-srg.jar" "$MAP" forward "$MC" "$WORK/base-mcp.jar" "$WORK/helper-mcp.jar"
java -cp "$TOOL" Remap "$MC" "$WORK/mc-srg.jar" "$MAP" forward "$MC"
java -cp "$TOOL" LinkCheck "$WORK/helper-srg.jar" "$WORK/mc-srg.jar" "$BASE" "$GECKO" "$WORK/patch-srg.jar"
java -cp "$TOOL" LinkCheck "$WORK/patch-srg.jar" "$WORK/mc-srg.jar" "$BASE" "$GECKO" "$WORK/helper-srg.jar"
python3 "$ROOT/pack.py" "$BASE" "$OUT" "$WORK/helper-srg.jar" "$WORK/patch-srg.jar"
python3 "$ROOT/verify.py" "$OUT"
