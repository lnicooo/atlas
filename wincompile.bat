@ECHO OFF
GOTO lbl%1

:lbl
:lblAtlasPackage
@ECHO ON

@ECHO OFF
IF NOT "%~1"=="" GOTO End
:lbl.
@ECHO ON
javac -classpath .\ *.java
@ECHO OFF
IF NOT "%~1"=="" GOTO End
:lblHefestus
@ECHO ON
javac -classpath .\ Hefestus\*.java
@ECHO OFF
IF NOT "%~1"=="" GOTO End
:lblHermesCRC
@ECHO ON
javac -classpath .\ HermesCRC\*.java
@ECHO OFF
IF NOT "%~1"=="" GOTO End
:lblHermesSR
@ECHO ONjavac
javac -classpath .\ HermesSR\*.java
@ECHO OFF
IF NOT "%~1"=="" GOTO End
:lblHermesTU
@ECHO ON
javac -classpath .\ HermesTU\*.java
@ECHO OFF
IF NOT "%~1"=="" GOTO End
:lblHermesTB
@ECHO ON
javac -classpath .\ HermesTB\*.java
@ECHO OFF
IF NOT "%~1"=="" GOTO End
:lblJupiter
@ECHO ON
javac -classpath .\ Jupiter\*.java
@ECHO OFF
IF NOT "%~1"=="" GOTO End
:lblMaia
@ECHO ON
javac -classpath .\ Maia\*.java
@ECHO OFF
IF NOT "%~1"=="" GOTO End
:lblTrafficMbps
@ECHO ON
javac -classpath .\ TrafficMbps\*.java
@ECHO OFF
IF NOT "%~1"=="" GOTO End
:lblTrafficMeasurer
@ECHO ON
javac -classpath .\ TrafficMeasurer\*.java
@ECHO OFF
IF NOT "%~1"=="" GOTO End
:lblTester
@ECHO ON
javac -classpath .\ Tester\*.java
@ECHO OFF
:End
