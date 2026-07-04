@echo off
echo Compiling source code...
call ant compile
if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b %errorlevel%
)
echo Launching game...
java -cp "classes;lib/*" baba.engine.Main
