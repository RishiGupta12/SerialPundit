##Packing sp-x.x.x.jar in an application jar

This note describes how to pack any sp-xxx.jar file into an application jar file for an easy deployment or distribution purpose.

- Download latest sp-xxx.jar from repository.
  https://github.com/RishiGupta12/SerialPundit/tree/master/prebuilt-release
  
- Build you application in Eclipse IDE including sp-xxx.jar as an external jar dependency.
  Java Build Path -> Libraries -> Add External JARs..
  
- Right click on your Eclipse project and select Export. A windows will get opened. Then;
  Java -> Runnable JAR file and click next.
  Select a launch configuration of your choice and Package required libraries into generated jar option.
  Click finish.
  
  The generated application jar will contain sp-xxx.jar file. The Eclipse will automatically set classpath and its loaders.
  
  Eclipse will automatically configure correct classpath internally.
  
- To run application (Linux) run following command.
```
$ java -jar MyApplication.jar
```
  
