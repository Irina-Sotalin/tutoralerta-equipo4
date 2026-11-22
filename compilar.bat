@echo off
REM Compila el proyecto y genera dist\TutorAlerta.jar (necesita JDK 11 o superior)
if exist out rmdir /s /q out
mkdir out
if not exist dist mkdir dist
javac -encoding UTF-8 -d out src\tutoralerta\*.java || goto error
echo Main-Class: tutoralerta.Main> out\manifest.txt
jar cfm dist\TutorAlerta.jar out\manifest.txt -C out tutoralerta || goto error
echo Listo: dist\TutorAlerta.jar
goto fin
:error
echo Hubo un error al compilar. Revise que el JDK este instalado (javac -version).
:fin
pause
