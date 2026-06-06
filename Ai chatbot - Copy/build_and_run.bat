@echo off
echo ===================================
echo   Building AI Chatbot Project
echo ===================================
echo.

if not exist bin mkdir bin

echo Compiling Java source files...
javac -d bin src/chatbot/nlp/*.java src/chatbot/bot/*.java src/chatbot/gui/*.java src/chatbot/*.java

:: Check if compilation was successful
if %ERRORLEVEL% equ 0 (
    echo.
    echo Compilation successful! 
    echo Starting AI Chatbot...
    echo.
    java -cp bin chatbot.Main
) else (
    echo.
    echo Compilation failed. Please check the errors above.
)

pause
