#!/bin/sh
# Compila el proyecto y genera dist/TutorAlerta.jar (necesita JDK 11 o superior)
set -e
rm -rf out && mkdir -p out dist
javac -encoding UTF-8 -d out src/tutoralerta/*.java
printf 'Main-Class: tutoralerta.Main\n' > out/manifest.txt
jar cfm dist/TutorAlerta.jar out/manifest.txt -C out tutoralerta
echo "Listo: dist/TutorAlerta.jar"
