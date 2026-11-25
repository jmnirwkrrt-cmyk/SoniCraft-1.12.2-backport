#!/bin/bash
set -eu
J=tools/zulu8.84.0.15-ca-jdk8.0.442-linux_x64/bin
CP='sonicraft112/build/_renameJarInPlace_7/output.jar:social42/deps/p41-mcp.jar:social42/deps/forge-mcp.jar:tools/geckolib-forge-1.12.2-3.0.31.jar:tools/gradle-4.9/lib/*:tools/gradle-4.9/lib/plugins/*'
mkdir -p social42/classes
$J/javac -source 8 -target 8 -cp "$CP" -d social42/classes social42/src/org/sonicraft/backport/*.java
