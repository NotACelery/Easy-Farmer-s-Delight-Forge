@echo off
setlocal EnableExtensions
cd /d "%~dp0"

echo Cleaning obsolete pre-regularization files for the 1.4.0 development line...

if exist "src\main\java\dev\celerbi\easyfarmersdelightcompat\compat\jade" (
    rmdir /s /q "src\main\java\dev\celerbi\easyfarmersdelightcompat\compat\jade"
    if errorlevel 1 exit /b 1
)

if exist "src\main\resources\data\easyfarmersdelightcompat\tags\item\cutter_logs.json" (
    del /q "src\main\resources\data\easyfarmersdelightcompat\tags\item\cutter_logs.json"
    if errorlevel 1 exit /b 1
)

if exist "src\main\resources\data\easyfarmersdelightcompat\tags\items\cutter_logs.json" (
    del /q "src\main\resources\data\easyfarmersdelightcompat\tags\items\cutter_logs.json"
    if errorlevel 1 exit /b 1
)

echo Obsolete pre-regularization files cleaned.
endlocal & exit /b 0
