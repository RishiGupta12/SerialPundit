::!/bin/bash
::
:: This file is part of SerialPundit.
:: 
:: Copyright (C) 2014-2018, Rishi Gupta. All rights reserved.
::
:: The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
:: General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
:: license for commercial use of this software. 
::
:: The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
:: without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::


:: build and run application using sp from shell script
@Echo OFF

set CURDIR=%~dp0

if not exist "%CURDIR%sp-core.jar:sp-tty.jar" goto ERROR
if not exist "%CURDIR%test\FindPorts.java" goto ERROR

javac -cp "%CURDIR%sp-core.jar:sp-tty.jar" %CURDIR%test\FindPorts.java

java -cp ".;%CLASSPATH%;%CURDIR%;%CURDIR%sp-core.jar:sp-tty.jar" test.FindPorts

Exit /b

:ERROR
echo Either sp-core.jar,sp-tty.jar or test\FindPorts.java file is not present in current directory.

