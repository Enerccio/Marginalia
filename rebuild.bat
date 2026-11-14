@echo off
setlocal

rem Windows version of rebuild.sh: updates the checkout (the branch it is on), builds the plugins and the image and
rem restarts the container. The application keeps running while the new version is built, so a failed build leaves
rem the old version up.
rem
rem Usage: rebuild.bat [--force] [--stop-first]
rem --force       rebuilds and restarts even when there are no new commits.
rem --stop-first  stops the container before the build (the order of older versions), for a computer that does not
rem               have the memory for the application and the build together.
set FORCE=0
set STOP_FIRST=0

:args
if "%~1"=="" goto :args_done
if "%~1"=="--force" (
    set FORCE=1
) else if "%~1"=="--stop-first" (
    set STOP_FIRST=1
) else (
    echo Usage: %~nx0 [--force] [--stop-first]
    exit /b 1
)
shift
goto :args
:args_done

cd /d "%~dp0" || exit /b 1

rem Fetch the latest commit info of the branch's remote
git fetch || goto :fail_fetch

rem Compare current local commit with remote upstream commit (the upstream of the branch that is checked out)
set LOCAL=
set REMOTE=
for /f %%i in ('git rev-parse HEAD') do set LOCAL=%%i
for /f %%i in ('git rev-parse @{u}') do set REMOTE=%%i
if "%REMOTE%"=="" goto :fail_upstream

rem The commit the running container was built from, written at the end of a successful run. It differs from the
rem checkout after a build that failed: the next run then builds again although there is nothing new to pull.
set DEPLOYED_FILE=.rebuild-deployed
set DEPLOYED=
if exist "%DEPLOYED_FILE%" set /p DEPLOYED=<"%DEPLOYED_FILE%"
if "%DEPLOYED%"=="" set DEPLOYED=%LOCAL%

if "%LOCAL%"=="%REMOTE%" if "%DEPLOYED%"=="%LOCAL%" if "%FORCE%"=="0" (
    echo No updates found in Git repository. Containers remain running.
    exit /b 0
)

if "%FORCE%"=="1" (
    echo Forced rebuild. Updating services...
) else if "%LOCAL%"=="%REMOTE%" (
    echo The last build did not finish. Updating services...
) else (
    echo New changes detected. Updating services...
)

if "%STOP_FIRST%"=="1" docker compose down

rem Remember what runs now, so that a build that fails after the pull is made again by the next run
>"%DEPLOYED_FILE%" echo %DEPLOYED%

rem Pull the new code
git pull || goto :fail_pull

rem Build the plugins in a container (compose file in marginalia\plugins, jars end up in its build folder)
set PLUGINS_COMPOSE=marginalia\plugins\docker-compose.yml
docker compose -f "%PLUGINS_COMPOSE%" build || goto :fail_plugins
docker compose -f "%PLUGINS_COMPOSE%" run --rm plugins || goto :fail_plugins

rem Build the image of the application; the old container still runs
docker compose build || goto :fail_image

rem Stop the old container, overwrite the installed plugins (they are loaded on the application start) and start
rem the new one
docker compose down
if not exist data\extensions mkdir data\extensions
copy /y marginalia\plugins\build\*.jar data\extensions\ >nul || goto :fail_copy
docker compose up -d || goto :fail_up

for /f %%i in ('git rev-parse HEAD') do >"%DEPLOYED_FILE%" echo %%i
exit /b 0

:fail_fetch
echo Git fetch failed
exit /b 1

:fail_upstream
echo The branch has no upstream to update from
exit /b 1

:fail_pull
echo Git pull failed
exit /b 1

:fail_plugins
echo Plugin build failed
exit /b 1

:fail_image
echo Image build failed
exit /b 1

:fail_copy
echo Copying plugins failed
exit /b 1

:fail_up
echo Starting the container failed
exit /b 1
