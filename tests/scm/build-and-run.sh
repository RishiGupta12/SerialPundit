#!/bin/bash
# build and run application using scm from shell script
cd /home/r/ws-host-uart/serial-communication-manager/tests/scm
javac -cp ./scm-1.0.4.jar FindPorts.java
java -classpath .:scm-1.0.4.jar FindPorts
