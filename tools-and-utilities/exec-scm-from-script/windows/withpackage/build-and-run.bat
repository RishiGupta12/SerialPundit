::
:: Author : Rishi Gupta
:: 
:: This file is part of 'serial communication manager' library.
:: Copyright (C) <2014-2016>  <Rishi Gupta>
::
:: This 'serial communication manager' is free software: you can redistribute it and/or modify
:: it under the terms of the GNU Affero General Public License as published by the Free Software 
:: Foundation, either version 3 of the License, or (at your option) any later version.
::
:: The 'serial communication manager' is distributed in the hope that it will be useful,
:: but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
:: A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
::
:: You should have received a copy of the GNU Affero General Public License
:: along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::


:: build and run application using scm from shell script
@Echo OFF

set CURDIR=%~dp0

if not exist "%CURDIR%scm-1.0.4.jar" goto ERROR
if not exist "%CURDIR%test\FindPorts.java" goto ERROR

javac -cp "%CURDIR%scm-1.0.4.jar" %CURDIR%test\FindPorts.java

java -cp ".;%CLASSPATH%;%CURDIR%;%CURDIR%scm-1.0.4.jar" test.FindPorts

Exit /b

:ERROR
echo Either scm-1.0.4.jar or test\FindPorts.java file is not present in current directory.

