#!/bin/bash
set -eu
J=tools/zulu8.84.0.15-ca-jdk8.0.442-linux_x64/bin
CP='sonicraft112/build/_renameJarInPlace_7/output.jar:p43/base-mcp.jar:tools/geckolib-forge-1.12.2-3.0.31.jar:tools/gradle-4.9/lib/*:tools/gradle-4.9/lib/plugins/*'
mkdir -p p43/classes
$J/javac -sourcepath p43/src -source 8 -target 8 -cp "$CP" -d p43/classes p43/src/org/sonicraft/backport/*.java
