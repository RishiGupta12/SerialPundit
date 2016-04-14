- Demonstrates how to execute a Java program from a shell script.
- Demonstrates how to integrate this library quickly and execute it from script.

See build-and-run.sh script in operating system specific folder.

###For Linux
---------------------
```
$./build-and-run.sh
```

#####Directory structure of files
/home/john/scm   
/home/john/scm/FindPorts.java   
/home/john/scm/scm-x.x.x.jar   

#####Compile
$ cd /home/john/scm
$ javac -cp ./scm-x.x.x.jar FindPorts.java

#####Execute
$ java -classpath .:scm-x.x.x.jar FindPorts


###For Windows
---------------------
```
C:\Users\xyz>build-and-run.bat
```

#####Directory structure of files
D:\scm   
D:\scm\FindPorts.java   
D:\scm\scm-x.x.x.jar   

#####Compile
C:\Users\om>D:   
D:\scm>javac -cp .\scm-x.x.x.jar FindPorts.java   

#####Execute
D:\scm>java -cp D:\scm\scm-x.x.x.jar;. FindPorts

