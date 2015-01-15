####Common steps
------

1. Get the project by either cloning repository or downloading it.
- Clone using command : git clone https://github.com/RishiGupta12/serial-com-manager.git
- Download project by clicking 'Download Zip' button on this page https://github.com/RishiGupta12/serial-com-manager
2. Install eclipse IDE for java developers from here https://eclipse.org/downloads. Select 32 bit or 64 bit as per your system and OS.
3. Install CDT plugin from here https://eclipse.org/cdt/downloads.php

####Building java library
------

1. Start eclipse selecting workspace folder as per your choice.

2. Import this java project into workspace.
   File->Import->General->Existing Projects Into Workspace->Select root directory:
   Then browse to the location where this project exist and select com.embeddedunveiled.serial folder.
   
3. Build the project.
   Select project folder and then from menu, Project->Build All.
   This will create class files in bin folder.
   
4. Export jar file that will be imported by applications using this library.
   Select project folder and then right click on it.
   Export->Java->JAR File, then click 'Next>' button.
   Select the export destination which is the place where this jar will be placed on file system.
   Click Browse and give name for example scm and click Finish button.
