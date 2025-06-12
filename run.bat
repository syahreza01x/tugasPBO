@echo off
setlocal

REM Pastikan path ke JAR benar
set CP=lib\mysql-connector-j-9.3.0.jar;.

javac -cp "%CP%" -d . main.java model\*.java view\*.java controller\*.java
java -cp "%CP%;" Main

endlocal