#!/bin/sh
# Ejecuta las pruebas automáticas de las reglas de las 3 historias
set -e
rm -rf out && mkdir -p out
javac -encoding UTF-8 -d out src/tutoralerta/*.java
javac -encoding UTF-8 -cp out -d out pruebas/PruebasLogica.java
java -cp out PruebasLogica
