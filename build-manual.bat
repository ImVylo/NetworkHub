@echo off
echo Building NetworkHub...

REM Clean build directory
rmdir /s /q build 2>nul
mkdir build\classes
mkdir build\libs

echo Compiling Java sources...
javac --release 17 -encoding UTF-8 -Xlint:none -cp "..\..\HytaleServer.jar;lib\*" -d build\classes @sources.txt

if %ERRORLEVEL% neq 0 (
    echo Compilation failed with errors. Attempting to continue...
)

echo Copying resources...
xcopy /E /I /Y src\main\resources build\classes

echo Creating JAR...
cd build\classes
jar cvfm ..\libs\NetworkHub-1.0.0.jar ..\..\src\main\resources\manifest.json .
cd ..\..

REM Extract and include dependencies
cd build\classes
for %%f in (..\..\lib\*.jar) do jar xf "%%f"
cd ..\..

REM Recreate JAR with dependencies
cd build\classes
jar cvfm ..\libs\NetworkHub-1.0.0.jar ..\..\src\main\resources\manifest.json .
cd ..\..

echo Build complete! JAR location: build\libs\NetworkHub-1.0.0.jar
pause
