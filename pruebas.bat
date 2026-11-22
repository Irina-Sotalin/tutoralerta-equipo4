@echo off
if exist out rmdir /s /q out
mkdir out
javac -encoding UTF-8 -d out src\tutoralerta\*.java || goto fin
javac -encoding UTF-8 -cp out -d out pruebas\PruebasLogica.java || goto fin
java -cp out PruebasLogica
:fin
pause
