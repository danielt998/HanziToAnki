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
@rem  HanziToAnki startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and HANZI_TO_ANKI_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

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

set CLASSPATH=%APP_HOME%\lib\HanziToAnki-1.0.0-plain.jar;%APP_HOME%\lib\tika-core-2.9.2.jar;%APP_HOME%\lib\annotations-24.1.0.jar;%APP_HOME%\lib\spring-boot-starter-web-3.1.0.jar;%APP_HOME%\lib\spring-boot-starter-json-3.1.0.jar;%APP_HOME%\lib\spring-boot-starter-3.1.0.jar;%APP_HOME%\lib\spring-boot-starter-logging-3.1.0.jar;%APP_HOME%\lib\logback-classic-1.4.7.jar;%APP_HOME%\lib\log4j-to-slf4j-2.20.0.jar;%APP_HOME%\lib\jul-to-slf4j-2.0.7.jar;%APP_HOME%\lib\slf4j-api-2.0.11.jar;%APP_HOME%\lib\commons-io-2.15.1.jar;%APP_HOME%\lib\spring-boot-starter-tomcat-3.1.0.jar;%APP_HOME%\lib\spring-webmvc-6.0.9.jar;%APP_HOME%\lib\spring-web-6.0.9.jar;%APP_HOME%\lib\spring-boot-autoconfigure-3.1.0.jar;%APP_HOME%\lib\spring-boot-3.1.0.jar;%APP_HOME%\lib\jakarta.annotation-api-2.1.1.jar;%APP_HOME%\lib\spring-context-6.0.9.jar;%APP_HOME%\lib\spring-aop-6.0.9.jar;%APP_HOME%\lib\spring-beans-6.0.9.jar;%APP_HOME%\lib\spring-expression-6.0.9.jar;%APP_HOME%\lib\spring-core-6.0.9.jar;%APP_HOME%\lib\snakeyaml-1.33.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.15.0.jar;%APP_HOME%\lib\jackson-module-parameter-names-2.15.0.jar;%APP_HOME%\lib\jackson-annotations-2.15.0.jar;%APP_HOME%\lib\jackson-core-2.15.0.jar;%APP_HOME%\lib\jackson-datatype-jdk8-2.15.0.jar;%APP_HOME%\lib\jackson-databind-2.15.0.jar;%APP_HOME%\lib\tomcat-embed-websocket-10.1.8.jar;%APP_HOME%\lib\tomcat-embed-core-10.1.8.jar;%APP_HOME%\lib\tomcat-embed-el-10.1.8.jar;%APP_HOME%\lib\micrometer-observation-1.10.7.jar;%APP_HOME%\lib\spring-jcl-6.0.9.jar;%APP_HOME%\lib\micrometer-commons-1.10.7.jar;%APP_HOME%\lib\logback-core-1.4.7.jar;%APP_HOME%\lib\log4j-api-2.20.0.jar;%APP_HOME%\lib\fastdoubleparser-0.8.0.jar


@rem Execute HanziToAnki
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %HANZI_TO_ANKI_OPTS%  -classpath "%CLASSPATH%" Launcher %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable HANZI_TO_ANKI_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%HANZI_TO_ANKI_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
