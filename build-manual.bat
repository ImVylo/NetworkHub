@echo off
echo Building NetworkHub...

REM Clean build directory
rmdir /s /q build 2>nul
mkdir build\classes
mkdir build\libs

echo Compiling Java sources...
javac -source 17 -target 17 -encoding UTF-8 -Xlint:none -cp "..\..\HytaleServer.jar;lib\*" -d build\classes @sources.txt 2>&1 | findstr /V "module not found"

if %ERRORLEVEL% neq 0 (
    echo Compilation failed with errors. Attempting to continue...
)

echo Copying resources...
xcopy /E /I /Y src\main\resources build\classes

echo Creating JAR...
cd build\classes
jar cvf ..\libs\NetworkHub-1.0.0.jar .
cd ..\..

REM Extract and include dependencies
cd build\classes
for %%f in (..\..\lib\*.jar) do jar xf "%%f"
cd ..\..

REM Recreate JAR with dependencies
cd build\classes
jar cvf ..\libs\NetworkHub-1.0.0.jar .
cd ..\..

echo Build complete! JAR location: build\libs\NetworkHub-1.0.0.jar
pause
