::!/bin/bash
::
:: This file is part of SerialPundit project and software.
:: 
:: Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
::
:: The SerialPundit software is DUAL licensed. It is made available under the terms of the GNU Affero 
:: General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
:: license for commercial use of this software. 
::
:: The SerialPundit software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
:: without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::


:: build and run application using scm from shell script

@Echo OFF

set CURDIR=%~dp0

if not exist "%CURDIR%scm-1.0.4.jar" goto ERROR
if not exist "%CURDIR%FindPorts.java" goto ERROR

javac -cp "%CURDIR%scm-1.0.4.jar" %CURDIR%FindPorts.java

java -cp %CURDIR%scm-1.0.4.jar;%CURDIR% FindPorts

Exit /b

:ERROR
echo Either scm-1.0.4.jar or FindPorts.java file is not present in current directory.

