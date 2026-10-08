@echo off
rem Starts Marginalia on the bundled Java runtime. Options: marginalia --help
rem Extra JVM options can be passed in MARGINALIA_JAVA_OPTS (e.g. -Xmx2g).
setlocal
set "DIR=%~dp0.."
"%DIR%\runtime\bin\java.exe" -Djdk.attach.allowAttachSelf=true -XX:+EnableDynamicAgentLoading --enable-native-access=ALL-UNNAMED %MARGINALIA_JAVA_OPTS% -jar "%DIR%\app\marginalia-launcher.jar" %*
