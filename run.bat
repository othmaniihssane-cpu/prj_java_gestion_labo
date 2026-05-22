@echo off
if not exist out mkdir out
if not exist out\view mkdir out\view

echo Compiling Java code...
javac --module-path "lib\javafx-sdk-24.0.1\lib" --add-modules javafx.controls,javafx.fxml -d out -cp "lib\mysql-connector-j-9.5.0.jar;src" src\Main.java src\controller\*.java src\dao\*.java src\model\*.java src\util\*.java

IF %ERRORLEVEL% NEQ 0 (
    echo Compilation failed.
    exit /b %ERRORLEVEL%
)

echo Copying resources...
copy src\view\*.fxml out\view\ >nul
copy src\view\*.css out\view\ >nul

echo Launching application...
java --module-path "lib\javafx-sdk-24.0.1\lib" --add-modules javafx.controls,javafx.fxml -cp "out;lib\mysql-connector-j-9.5.0.jar" Main
