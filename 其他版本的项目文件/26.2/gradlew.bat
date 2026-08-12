@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Gradle test workers cannot load classes from this repository's non-ASCII
@rem absolute path. Run this build through a temporary ASCII-only drive mapping.
for %%i in ("%APP_HOME%") do set REAL_APP_HOME=%%~fi
set STARDEW_BUILD_DRIVE=S:
if exist "%STARDEW_BUILD_DRIVE%\" (
    echo ERROR: Temporary build drive %STARDEW_BUILD_DRIVE% is already in use. 1>&2
    set GRADLE_EXIT_CODE=1
    goto fail
)
subst %STARDEW_BUILD_DRIVE% "%REAL_APP_HOME%"
if %ERRORLEVEL% neq 0 (
    echo ERROR: Could not create temporary build drive %STARDEW_BUILD_DRIVE%. 1>&2
    set GRADLE_EXIT_CODE=1
    goto fail
)
set STARDEW_SUBST_ACTIVE=1
set APP_HOME=%STARDEW_BUILD_DRIVE%\
cd /d "%APP_HOME%"

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=


@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*
set GRADLE_EXIT_CODE=%ERRORLEVEL%

:end
if defined STARDEW_SUBST_ACTIVE (
    subst %STARDEW_BUILD_DRIVE% /d
    set STARDEW_SUBST_ACTIVE=
)
if "%GRADLE_EXIT_CODE%"=="0" goto mainEnd

:fail
if defined STARDEW_SUBST_ACTIVE subst %STARDEW_BUILD_DRIVE% /d
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if defined GRADLE_EXIT_CODE (set EXIT_CODE=%GRADLE_EXIT_CODE%) else (set EXIT_CODE=%ERRORLEVEL%)
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
