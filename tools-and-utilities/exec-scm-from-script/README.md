These examples demonstrates :
- How to execute a Java program from shell script.
- How to integrate this library quickly and execute it from script.

See build-and-run.sh script in operating system specific folder. Steps executed by these scripts are as follows:

###For Linux
---------------------

#####Directory structure of files
/home/john/scm
/home/john/scm/FindPorts.java
/home/john/scm/scm-1.0.3.jar

#####Compile
$ cd /home/john/scm
$ javac -cp ./scm-1.0.3.jar FindPorts.java

#####Execute
$ java -classpath .:scm-1.0.2.jar FindPorts


###For Windows
---------------------

#####Directory structure of files
D:\scm
D:\scm\FindPorts.java
D:\scm\scm-1.0.3.jar

#####Compile
C:\Users\om>D:
D:\scm>javac -cp .\scm-1.0.3.jar FindPorts.java

#####Execute
D:\scm>java -cp D:\scm\scm-1.0.3.jar;. FindPorts

