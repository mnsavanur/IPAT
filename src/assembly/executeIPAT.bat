@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.10

if not defined JAVA_HOME (
	echo Edit batch file and set JAVA_HOME with JDK 1.8.* installation path
	goto :EXIT
)

set classpath=.\libs\*
rem set classpath=.\libs\*;%CLASSPATH%;.\*
echo %classpath%

java -classpath %CLASSPATH% -Dlog4j.configuration=.\dependencies\log4j.properties com.lti.base.MainClass Projects\GuideWire\TestConfig.xlsx


:EXIT
pause





